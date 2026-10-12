#include <jni.h>
#include "llama.h"
#include <atomic>
#include <algorithm>
#include <chrono>
#include <mutex>
#include <string>
#include <vector>
#include <memory>

namespace {
std::atomic<uint64_t> revision{0};
std::mutex engine_mutex;
llama_model * model = nullptr;
bool initialized = false;
using Clock = std::chrono::steady_clock;
struct Abort { Clock::time_point started; uint64_t expected; };
bool abort_decode(void * data) {
    auto * state = static_cast<Abort *>(data);
    return revision.load() != state->expected || Clock::now() - state->started > std::chrono::seconds(45);
}
jbyteArray bytes(JNIEnv * env, const std::string & value) {
    jbyteArray output = env->NewByteArray(static_cast<jsize>(value.size()));
    if (output) env->SetByteArrayRegion(output, 0, value.size(), reinterpret_cast<const jbyte *>(value.data()));
    return output;
}
}

extern "C" JNIEXPORT jlong JNICALL
Java_fr_astor_hades_LocalLlm_nativeRevision(JNIEnv *, jclass) { return static_cast<jlong>(revision.load()); }

extern "C" JNIEXPORT void JNICALL
Java_fr_astor_hades_LocalLlm_nativeCancel(JNIEnv *, jclass) { revision.fetch_add(1); }

extern "C" JNIEXPORT void JNICALL
Java_fr_astor_hades_LocalLlm_nativeRelease(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> guard(engine_mutex);
    if (model) { llama_model_free(model); model = nullptr; }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_fr_astor_hades_LocalLlm_nativeGenerate(JNIEnv * env, jclass, jstring path, jbyteArray input, jlong generation) {
    std::lock_guard<std::mutex> guard(engine_mutex);
    if (revision.load() != static_cast<uint64_t>(generation)) return bytes(env, "ERREUR_LOCAL:Génération annulée.");
    if (!initialized) {
        llama_log_set([](ggml_log_level, const char *, void *) {}, nullptr);
        llama_backend_init(); initialized = true;
    }
    const char * location = env->GetStringUTFChars(path, nullptr);
    if (!location) return bytes(env, "ERREUR_LOCAL:Chemin du modèle indisponible.");
    if (!model) {
        auto params = llama_model_default_params(); params.n_gpu_layers = 0;
        uint64_t expected = static_cast<uint64_t>(generation);
        params.progress_callback = [](float, void * data) { return revision.load() == *static_cast<uint64_t *>(data); };
        params.progress_callback_user_data = &expected;
        model = llama_model_load_from_file(location, params);
    }
    env->ReleaseStringUTFChars(path, location);
    if (revision.load() != static_cast<uint64_t>(generation)) return bytes(env, "ERREUR_LOCAL:Génération annulée.");
    if (!model) return bytes(env, "ERREUR_LOCAL:Le modèle ne peut pas être chargé. Fermez les autres applications et réessayez.");
    const auto size = env->GetArrayLength(input);
    if (size <= 0 || size > 20000) return bytes(env, "ERREUR_LOCAL:Demande trop longue.");
    std::string prompt(size, '\0');
    env->GetByteArrayRegion(input, 0, size, reinterpret_cast<jbyte *>(prompt.data()));
    const auto * vocab = llama_model_get_vocab(model);
    const int n = -llama_tokenize(vocab, prompt.data(), prompt.size(), nullptr, 0, true, true);
    if (n <= 0 || n > 1850) return bytes(env, "ERREUR_LOCAL:Le contexte est trop long. Posez une question plus courte ou changez de sujet.");
    std::vector<llama_token> tokens(n);
    if (llama_tokenize(vocab, prompt.data(), prompt.size(), tokens.data(), tokens.size(), true, true) < 0)
        return bytes(env, "ERREUR_LOCAL:La demande ne peut pas être préparée.");
    auto params = llama_context_default_params();
    params.n_ctx = 2048; params.n_batch = 128; params.n_ubatch = 64;
    params.n_threads = 4; params.n_threads_batch = 4; params.no_perf = true;
    std::unique_ptr<llama_context, decltype(&llama_free)> context(llama_init_from_model(model, params), llama_free);
    if (!context) return bytes(env, "ERREUR_LOCAL:Mémoire disponible insuffisante pour le modèle.");
    Abort abort{Clock::now(), static_cast<uint64_t>(generation)}; llama_set_abort_callback(context.get(), abort_decode, &abort);
    auto sampler_params = llama_sampler_chain_default_params(); sampler_params.no_perf = true;
    std::unique_ptr<llama_sampler, decltype(&llama_sampler_free)> sampler(llama_sampler_chain_init(sampler_params), llama_sampler_free);
    llama_sampler_chain_add(sampler.get(), llama_sampler_init_greedy());
    for (int i = 0; i < n; i += 128) {
        if (abort_decode(&abort)) return bytes(env, "ERREUR_LOCAL:Calcul interrompu. Essayez une question plus courte.");
        auto batch = llama_batch_get_one(tokens.data() + i, std::min(128, n - i));
        if (llama_decode(context.get(), batch)) return bytes(env, "ERREUR_LOCAL:Calcul interrompu. Réessayez avec une question plus courte.");
    }
    std::string output;
    bool limited = false;
    for (int i = 0; i < 160; ++i) {
        if (abort_decode(&abort)) { limited = true; break; }
        llama_token token = llama_sampler_sample(sampler.get(), context.get(), -1);
        if (llama_vocab_is_eog(vocab, token)) break;
        char piece[256]; int length = llama_token_to_piece(vocab, token, piece, sizeof(piece), 0, false);
        if (length < 0) return bytes(env, "ERREUR_LOCAL:Réponse non décodable.");
        output.append(piece, length);
        auto batch = llama_batch_get_one(&token, 1);
        if (llama_decode(context.get(), batch)) { limited = true; break; }
        if (i == 159) limited = true;
    }
    if (revision.load() != static_cast<uint64_t>(generation)) return bytes(env, "ERREUR_LOCAL:Génération annulée.");
    if (output.empty()) return bytes(env, "ERREUR_LOCAL:Pas de réponse produite. Essayez une question plus courte.");
    if (limited) output += "\n[Réponse limitée pour ce prototype local.]";
    return bytes(env, output);
}
