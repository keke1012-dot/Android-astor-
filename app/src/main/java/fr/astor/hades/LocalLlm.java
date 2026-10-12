package fr.astor.hades;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Fixed, checksum-verified model download. Prompts are never sent over the network. */
public final class LocalLlm {
    static final long MODEL_SIZE = 639446688L;
    static final String MODEL_SHA = "9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031";
    static final String MODEL_URL = "https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/resolve/23749fefcc72300e3a2ad315e1317431b06b590a/Qwen3-0.6B-Q8_0.gguf";
    private static final boolean AVAILABLE;
    static { boolean loaded; try { System.loadLibrary("astor_local"); loaded = true; } catch (UnsatisfiedLinkError e) { loaded = false; } AVAILABLE = loaded; }
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicLong epoch = new AtomicLong();
    private final AtomicBoolean busy = new AtomicBoolean();
    private final File directory, model;
    private volatile HttpsURLConnection transfer;
    private volatile boolean closed;
    public interface Listener { void state(String message, int percent); void done(String reply, String error); }
    public LocalLlm(File storage) { directory = new File(storage, "local-model"); model = new File(directory, "qwen3-0.6b-q8.gguf"); }
    public boolean available() { return AVAILABLE; }
    public boolean installed() { return model.isFile() && model.length() == MODEL_SIZE; }
    public boolean ready() { return AVAILABLE && installed(); }
    public void generate(String prompt, Listener listener) {
        if (closed || !AVAILABLE) { listener.done("", "Moteur local incompatible avec cet appareil."); return; }
        if (!ready()) { listener.done("", "Téléchargez d’abord le modèle local depuis Accueil."); return; }
        if (prompt.length() > 14000) { listener.done("", "Demande trop longue."); return; }
        if (!busy.compareAndSet(false, true)) { listener.done("", "Une opération locale est déjà en cours."); return; }
        long generation = epoch.get(), nativeGeneration = nativeRevision();
        worker.execute(() -> {
            try {
                if (generation != epoch.get()) return;
                listener.state("Calcul sur votre téléphone…", -1);
                byte[] result = nativeGenerate(model.getAbsolutePath(), prompt.getBytes(StandardCharsets.UTF_8), nativeGeneration);
                if (generation != epoch.get()) return;
                String answer = new String(result, StandardCharsets.UTF_8);
                if (answer.startsWith("ERREUR_LOCAL:")) listener.done("", answer.substring(13));
                else listener.done(answer, "");
            } catch (Throwable e) { if (generation == epoch.get()) listener.done("", "Le calcul local a échoué. Réessayez après avoir fermé d’autres applications."); }
            finally { busy.set(false); }
        });
    }
    private boolean current(long generation) { return !closed && generation == epoch.get(); }
    private static boolean allowed(URL url) {
        String host = url.getHost().toLowerCase(java.util.Locale.ROOT);
        return "https".equals(url.getProtocol()) && url.getUserInfo() == null && (url.getPort() == -1 || url.getPort() == 443)
                && (host.equals("huggingface.co") || host.endsWith(".huggingface.co") || host.endsWith(".hf.co"));
    }
    public void download(Listener listener) {
        if (closed || !AVAILABLE) { listener.done("", "Moteur local incompatible avec cet appareil."); return; }
        if (installed()) { listener.done("Modèle déjà installé. Aucun téléchargement nécessaire.", ""); return; }
        if (!busy.compareAndSet(false, true)) { listener.done("", "Une opération locale est déjà en cours."); return; }
        long generation = epoch.get();
        worker.execute(() -> {
            File part = new File(directory, "model.part");
            try {
                if (!current(generation)) return;
                if (!directory.isDirectory() && !directory.mkdirs()) throw new java.io.IOException();
                if (directory.getUsableSpace() < MODEL_SIZE + 64_000_000L) { listener.done("", "Espace insuffisant : libérez environ 750 Mo sur le téléphone."); return; }
                URL url = new URL(MODEL_URL);
                HttpsURLConnection connection = null;
                for (int redirects = 0; redirects < 8; redirects++) {
                    if (!allowed(url) || !current(generation)) throw new java.io.IOException();
                    connection = (HttpsURLConnection) url.openConnection(); transfer = connection;
                    connection.setInstanceFollowRedirects(false); connection.setConnectTimeout(20_000); connection.setReadTimeout(20_000);
                    connection.setRequestProperty("Accept-Encoding", "identity");
                    int status = connection.getResponseCode();
                    if (status == 200) break;
                    if (status < 300 || status > 399) throw new java.io.IOException();
                    String target = connection.getHeaderField("Location"); connection.disconnect();
                    if (target == null) throw new java.io.IOException(); url = new URL(url, target); connection = null;
                }
                if (connection == null || connection.getResponseCode() != 200) throw new java.io.IOException();
                MessageDigest digest = MessageDigest.getInstance("SHA-256"); long total = 0; int lastPercent = -1;
                try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(part)) {
                    byte[] buffer = new byte[65536]; int count;
                    while ((count = input.read(buffer)) != -1) {
                        if (!current(generation)) return;
                        total += count; if (total > MODEL_SIZE) throw new java.io.IOException();
                        output.write(buffer, 0, count); digest.update(buffer, 0, count);
                        int percent = (int) (100 * total / MODEL_SIZE);
                        if (percent != lastPercent) { lastPercent = percent; listener.state("Téléchargement du modèle : " + percent + " %", percent); }
                    }
                    output.getFD().sync();
                }
                StringBuilder hash = new StringBuilder(); for (byte b : digest.digest()) hash.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
                if (!current(generation)) return;
                if (total != MODEL_SIZE || !MODEL_SHA.equals(hash.toString())) { listener.done("", "Téléchargement incomplet ou intégrité incorrecte. Réessayez."); return; }
                Files.move(part.toPath(), model.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                listener.done("Modèle vérifié et installé. Les réponses sont maintenant calculées hors connexion.", "");
            } catch (Exception e) { if (current(generation)) listener.done("", "Téléchargement interrompu. Vérifiez la connexion et réessayez."); }
            finally { if (transfer != null) transfer.disconnect(); transfer = null; part.delete(); busy.set(false); }
        });
    }
    public void cancel() {
        epoch.incrementAndGet(); if (transfer != null) transfer.disconnect();
        if (AVAILABLE) nativeCancel();
        if (!closed && AVAILABLE) worker.execute(LocalLlm::nativeRelease);
    }
    public void close() { cancel(); closed = true; worker.shutdown(); }
    private static native byte[] nativeGenerate(String path, byte[] prompt, long revision);
    private static native long nativeRevision();
    private static native void nativeCancel();
    private static native void nativeRelease();
}
