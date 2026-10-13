package fr.astor.hades;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

/** One explicit approved request, no retries, credentials never passed to the WebView. */
public final class ElevenSpeech {
    public interface Callback { void state(String message, boolean configured, boolean busy); }
    private final Context context;
    private final ElevenCredentials credentials;
    private final Callback callback;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final File directory;
    private volatile Job current;
    private boolean closed;
    private MediaPlayer player;
    private AudioFocusRequest focus;
    private static final class Job {
        final File audio;
        volatile HttpsURLConnection connection;
        volatile Future<?> task;
        Runnable timeout;
        Job(File directory) { audio = new File(directory, java.util.UUID.randomUUID() + ".mp3"); }
    }
    public ElevenSpeech(Context context, ElevenCredentials credentials, Callback callback) {
        this.context = context.getApplicationContext(); this.credentials = credentials; this.callback = callback;
        directory = new File(this.context.getCacheDir(), "astor-eleven-audio");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Audio storage unavailable");
        File[] files = directory.listFiles();
        if (files != null) for (File file : files) if (file.isFile()) file.delete();
    }
    public boolean busy() { return current != null; }
    public void state(String message) { if (!closed) callback.state(message, credentials.configured(), busy()); }
    public void speakApproved(String approvedText) {
        if (closed || busy()) return;
        String text;
        try { text = ElevenPolicy.text(approvedText); } catch (IllegalArgumentException error) { state("Choisissez un texte de 1 à 800 caractères."); return; }
        if (!credentials.configured()) { state("Configurez votre clé API ElevenLabs sur ce téléphone."); return; }
        Job job = new Job(directory); current = job;
        job.timeout = () -> fail(job, "Lecture arrêtée après le délai maximal. Un envoi déjà accepté peut avoir consommé des crédits.");
        main.postDelayed(job.timeout, 120_000);
        state("Envoi du texte confirmé à ElevenLabs…");
        job.task = worker.submit(() -> {
            HttpsURLConnection connection = null;
            try {
                String key;
                try { key = credentials.read(); }
                catch (Exception error) { throw new Failure("Clé indisponible ou validation biométrique expirée. Verrouillez puis déverrouillez ASTOR ; reconfigurez la clé si nécessaire."); }
                if (current != job || Thread.currentThread().isInterrupted()) return;
                connection = (HttpsURLConnection)new URL(ElevenPolicy.ENDPOINT).openConnection(); job.connection = connection;
                connection.setInstanceFollowRedirects(false); connection.setRequestMethod("POST");
                connection.setConnectTimeout(10_000); connection.setReadTimeout(20_000); connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setRequestProperty("xi-api-key", key);
                connection.setRequestProperty("Content-Type", "application/json"); connection.setRequestProperty("Accept", "audio/mpeg");
                connection.setRequestProperty("User-Agent", "AstorAndroid/0.2.9.8 (user-approved speech)");
                JSONObject payload = new JSONObject(); payload.put("text", text); payload.put("model_id", ElevenPolicy.MODEL);
                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(body.length);
                if (current != job || Thread.currentThread().isInterrupted()) return;
                try (java.io.OutputStream output = connection.getOutputStream()) {
                    if (current != job || Thread.currentThread().isInterrupted()) return;
                    output.write(body);
                }
                int status = connection.getResponseCode();
                if (status != 200) throw new Failure(ElevenPolicy.error(status));
                String type = connection.getContentType();
                if (type == null || !(type.toLowerCase(java.util.Locale.ROOT).startsWith("audio/mpeg")
                        || type.toLowerCase(java.util.Locale.ROOT).startsWith("application/octet-stream")))
                    throw new Failure("Réponse vocale invalide. Aucun audio n’a été lu.");
                if (connection.getContentLengthLong() > ElevenPolicy.MAX_AUDIO_BYTES) throw new Failure("Audio trop volumineux.");
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                long deadline = System.nanoTime() + 40_000_000_000L;
                try (InputStream stream = connection.getInputStream()) {
                    byte[] buffer = new byte[8192]; int count;
                    while ((count = stream.read(buffer)) != -1) {
                        if (current != job || Thread.currentThread().isInterrupted()) return;
                        if (System.nanoTime() > deadline || data.size() + count > ElevenPolicy.MAX_AUDIO_BYTES)
                            throw new Failure("Téléchargement audio trop long ou trop volumineux.");
                        data.write(buffer, 0, count);
                    }
                }
                byte[] audio = data.toByteArray();
                if (!ElevenPolicy.mp3(audio)) throw new Failure("Le service n’a pas fourni un MP3 valide.");
                if (current != job || Thread.currentThread().isInterrupted()) return;
                Files.write(job.audio.toPath(), audio);
                main.post(() -> { if (!closed && current == job) play(job); else job.audio.delete(); });
            } catch (Failure error) { main.post(() -> fail(job, error.safeMessage)); }
            catch (Exception error) { main.post(() -> fail(job, "Connexion vocale impossible. Aucun nouvel essai automatique ; vous pouvez utiliser la voix locale.")); }
            finally {
                if (connection != null) connection.disconnect(); job.connection = null;
                if (current != job || Thread.currentThread().isInterrupted()) job.audio.delete();
            }
        });
    }
    private static final class Failure extends Exception {
        final String safeMessage;
        Failure(String message) { safeMessage = message; }
    }
    private void play(Job job) {
        try {
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(change -> { if (change < 0 && current == job) cancel("Lecture interrompue par une autre application."); }, main).build();
            if (manager == null || manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                cancel("Audio occupé. Réessayez lorsque le téléphone est disponible."); return;
            }
            MediaPlayer owned = new MediaPlayer(); player = owned;
            owned.setAudioAttributes(attributes); owned.setVolume(0.65f, 0.65f); owned.setDataSource(job.audio.getAbsolutePath());
            owned.setOnPreparedListener(p -> {
                if (player != p || current != job || closed) return;
                try {
                    if (p.getDuration() > 90_000) { cancel("Audio trop long. Choisissez une phrase plus courte."); return; }
                    p.start(); state("ASTOR · lecture avec votre voix ElevenLabs.");
                } catch (RuntimeException error) { fail(job, "Impossible de lire cet audio."); }
            });
            owned.setOnCompletionListener(p -> { if (player == p && current == job) cancel("Lecture terminée."); });
            owned.setOnErrorListener((p, what, extra) -> { if (player == p && current == job) cancel("Impossible de lire cet audio."); return true; });
            owned.prepareAsync();
        } catch (Exception error) { fail(job, "Impossible de lire cet audio."); }
    }
    private void fail(Job job, String message) { if (current == job) cancel(message); }
    public void cancel(String message) {
        Job old = current; current = null;
        if (old != null) {
            main.removeCallbacks(old.timeout); if (old.task != null) old.task.cancel(true);
            HttpsURLConnection connection = old.connection; if (connection != null) connection.disconnect(); old.audio.delete();
        }
        if (player != null) { player.release(); player = null; }
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null && focus != null) manager.abandonAudioFocusRequest(focus);
        focus = null; if (message != null) state(message);
    }
    public void close() { closed = true; cancel(null); worker.shutdownNow(); }
}
