package fr.astor.hades;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Synthetic French questions, with identical prompts for greedy and sampled comparisons. */
public final class LocalLlmQualityTest {
    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("astor-quality-");
        Path directory = Files.createDirectories(root.resolve("local-model"));
        Path file = directory.resolve("qwen3-0.6b-q8.gguf");
        try { Files.createLink(file, Path.of(args[0])); } catch (Exception e) { Files.copy(Path.of(args[0]), file); }
        String[] questions = {
            "Explique-moi simplement ce qu’est un trou noir.",
            "Pourquoi pleut-il ?",
            "Quelle est la différence entre une étoile et une planète ?",
            "Pourquoi la glace fond-elle ?",
            "Qu’est-ce qu’une hypothèse ?",
            "Comment dit-on bonjour en anglais ?",
            "Donne une astuce pour expliquer clairement une idée.",
            "Qu’est-ce qu’un budget ?",
            "Cite deux instruments de musique.",
            "Définis simplement la mémoire temporaire."
        };
        String system = "Tu es ASTOR. Réponds en français clair, en 2 ou 3 phrases. Donne d’abord la réponse, puis une explication simple. Exprime chaque idée une seule fois. Un exemple seulement s’il aide. Rôle : coordination et discussion. L’utilisateur décide. Signale les incertitudes. Aucun accès à Internet, aux commandes du téléphone ou au coffre.";
        StringBuilder results = new StringBuilder("[\n");
        for (int i = 0; i < questions.length; ++i) {
            LocalLlm engine = new LocalLlm(root.toFile());
            CountDownLatch done = new CountDownLatch(1);
            AtomicReference<String> error = new AtomicReference<>("");
            AtomicReference<String> answer = new AtomicReference<>("");
            String prompt = "<|im_start|>system\n" + system + "<|im_end|>\n<|im_start|>user\n" + questions[i]
                + "<|im_end|>\n<|im_start|>assistant\n<think>\n\n</think>\n\n";
            long start = System.nanoTime();
            engine.generate(prompt, new LocalLlm.Listener() {
                public void state(String message, int percent) {}
                public void done(String text, String problem) { answer.set(text); error.set(problem); done.countDown(); }
            });
            if (!done.await(120, TimeUnit.SECONDS)) throw new AssertionError("Inference timed out: " + i);
            long millis = (System.nanoTime() - start) / 1_000_000;
            engine.close();
            if (!error.get().isEmpty()) throw new AssertionError(error.get());
            if (answer.get().trim().length() < 3) throw new AssertionError("Empty generated answer");
            if (i > 0) results.append(",\n");
            results.append("{\"question\":").append(quote(questions[i])).append(",\"answer\":").append(quote(answer.get()))
                .append(",\"milliseconds\":").append(millis).append("}");
            System.out.println("PASS: French generation " + (i+1) + "/" + questions.length + " · " + millis + " ms");
        }
        results.append("\n]\n");
        Files.writeString(Path.of(args[1]), results, StandardCharsets.UTF_8);
        Files.delete(file); Files.delete(directory); Files.delete(root);
    }
}
