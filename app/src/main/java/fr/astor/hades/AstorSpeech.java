package fr.astor.hades;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Foreground, user-triggered, offline French TTS with bounded private PCM processing. */
public final class AstorSpeech {
    public interface Callback { void state(String message, JSONArray voices, boolean ready, boolean muted); }
    private final Context context;
    private final Callback callback;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final SharedPreferences prefs;
    private final File directory;
    private final List<Voice> voices = new ArrayList<>();
    private TextToSpeech tts;
    private boolean ready, closed, muted;
    private volatile Job current;
    private MediaPlayer player;
    private AudioFocusRequest focus;
    private long sequence;
    private static final class Job {
        final String id;
        final File raw, output;
        final String profile;
        volatile Future<?> task;
        Runnable watchdog;
        Job(String id, File directory, String profile) {
            this.id = id; this.profile = profile;
            raw = new File(directory, id + "-raw.wav"); output = new File(directory, id + "-voice.wav");
        }
        void delete() { raw.delete(); output.delete(); }
    }
    public AstorSpeech(Context context, Callback callback) {
        this.context = context.getApplicationContext(); this.callback = callback;
        prefs = this.context.getSharedPreferences("astor-voice", Context.MODE_PRIVATE);
        muted = prefs.getBoolean("muted", false);
        directory = new File(this.context.getCacheDir(), "astor-voice");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Voice storage unavailable");
        File[] stale = directory.listFiles();
        if (stale != null) for (File file : stale) if (file.isFile()) file.delete();
        tts = new TextToSpeech(this.context, status -> main.post(() -> initialized(status)));
    }
    private void initialized(int status) {
        if (closed) return;
        if (status != TextToSpeech.SUCCESS) { publish("Moteur vocal Android indisponible. Vérifiez les paramètres voix."); return; }
        try {
            voices.clear();
            Set<Voice> available = tts.getVoices();
            if (available != null) for (Voice voice : available) {
                if ("fr".equals(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired()
                        && !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)) voices.add(voice);
            }
            voices.sort(Comparator.comparing((Voice v) -> !v.getName().equals(prefs.getString("selectedVoice", "")))
                    .thenComparing(v -> !VoicePreferencePolicy.masculineName(v.getName()))
                    .thenComparing(v -> !"FR".equals(v.getLocale().getCountry()))
                    .thenComparing(Voice::getName));
            if (voices.isEmpty()) { publish("Aucune voix française hors ligne installée. Ouvrez Paramètres voix pour télécharger Français."); return; }
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) {}
                @Override public void onDone(String id) { main.post(() -> synthesized(id)); }
                @Override public void onError(String id) { main.post(() -> failed(id, "Synthèse impossible. Essayez une autre voix française locale.")); }
                @Override public void onError(String id, int code) { onError(id); }
            });
            ready = true;
            publish("Voix française locale prête. Touchez Tester la voix.");
        } catch (RuntimeException error) { publish("Le moteur vocal ne fournit pas ses voix. Vérifiez les paramètres Android."); }
    }
    public void selectVoice(String name, String profile) {
        if (closed) return;
        if (voices.stream().noneMatch(v -> v.getName().equals(name))) { publish("Détectez les voix avant de choisir."); return; }
        prefs.edit().putString("selectedVoice", name).putString("profile", VoicePreferencePolicy.profile(profile)).apply();
        publish("Voix choisie enregistrée pour les prochaines réponses.");
    }
    public void refresh() {
        if (closed) return;
        if (ready) publish("Voix française locale prête.");
        else { voices.clear(); initialized(TextToSpeech.SUCCESS); }
    }
    private void publish(String message) {
        if (closed) return;
        JSONArray names = new JSONArray();
        for (Voice voice : voices) {
            JSONObject item = new JSONObject();
            try { item.put("name", voice.getName()); item.put("lang", voice.getLocale().toLanguageTag()); } catch (org.json.JSONException ignored) {}
            names.put(item);
        }
        callback.state(message, names, ready, muted);
    }
    public void speak(String text, String voiceName, String requestedProfile) {
        String profile = VoicePreferencePolicy.profile(requestedProfile);
        if (closed) return;
        if (!ready) { publish("Le moteur vocal se prépare. Réessayez dans un instant."); return; }
        if (muted) { publish("Mode muet actif."); return; }
        if (text == null || text.trim().isEmpty() || text.length() > 1200) { publish("Choisissez un texte court, de 1 à 1 200 caractères."); return; }
        Voice selected = voices.stream().filter(v -> v.getName().equals(voiceName)).findFirst().orElse(voices.get(0));
        prefs.edit().putString("selectedVoice", selected.getName()).putString("profile", profile).apply();
        stop(null);
        try {
            if (tts.setVoice(selected) != TextToSpeech.SUCCESS) { publish("Cette voix n’est pas prête. Choisissez-en une autre."); return; }
            tts.setSpeechRate("grave".equals(profile) ? 0.88f : 0.96f);
            tts.setPitch("jarvis".equals(profile) ? 0.74f : "metal".equals(profile) ? 0.9f : 0.75f);
            String id = "voice-" + (++sequence) + "-" + java.util.UUID.randomUUID();
            Job job = new Job(id, directory, profile); current = job;
            job.watchdog = () -> failed(id, "Lecture arrêtée après le délai maximal.");
            main.postDelayed(job.watchdog, 90_000);
            Bundle parameters = new Bundle();
            parameters.putString(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "false");
            parameters.putString(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS, "true");
            int result = tts.synthesizeToFile(text, parameters, job.raw, id);
            if (result != TextToSpeech.SUCCESS) failed(id, "La synthèse vocale n’a pas démarré.");
            else publish("Préparation locale de la voix ASTOR…");
        } catch (RuntimeException error) { stop("La lecture vocale a échoué."); }
    }
    private void synthesized(String id) {
        Job job = current;
        if (closed || job == null || !job.id.equals(id)) return;
        job.task = worker.submit(() -> {
            try {
                if (job.raw.length() < 44 || job.raw.length() > 24_000_000) throw new IllegalArgumentException("Audio bounds");
                MetallicPcm.Wave wave = MetallicPcm.decode(Files.readAllBytes(job.raw.toPath()));
                byte[] output = MetallicPcm.encode(MetallicPcm.process(wave.samples, wave.rate, job.profile), wave.rate);
                if (current != job || Thread.currentThread().isInterrupted()) return;
                Files.write(job.output.toPath(), output);
                job.raw.delete();
                main.post(() -> { if (!closed && current == job) play(job); else job.delete(); });
            } catch (Exception error) { main.post(() -> failed(id, "Audio incompatible ou trop long. Essayez une phrase courte ou une autre voix.")); }
            finally { if (current != job || Thread.currentThread().isInterrupted()) job.delete(); }
        });
    }
    private void play(Job job) {
        try {
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes).setOnAudioFocusChangeListener(change -> {
                        if (change < 0 && current == job) stop("Lecture interrompue par une autre application.");
                    }, main).build();
            if (manager == null || manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                stop("Audio occupé. Réessayez lorsque le téléphone est disponible."); return;
            }
            MediaPlayer owned = new MediaPlayer(); player = owned;
            owned.setAudioAttributes(attributes); owned.setVolume(0.65f, 0.65f);
            owned.setDataSource(job.output.getAbsolutePath());
            owned.setOnPreparedListener(p -> {
                if (player != p || current != job || closed || muted) return;
                try { p.start(); } catch (RuntimeException error) { failed(job.id, "La lecture a échoué."); return; }
                publish("ASTOR · lecture locale en cours.");
            });
            owned.setOnCompletionListener(p -> { if (player == p && current == job) stop("Lecture terminée."); });
            owned.setOnErrorListener((p, what, extra) -> { if (player == p && current == job) stop("La lecture audio a échoué."); return true; });
            owned.prepareAsync();
        } catch (Exception error) { stop("La lecture audio a échoué."); }
    }
    private void failed(String id, String message) { Job job = current; if (job != null && job.id.equals(id)) stop(message); }
    public void setMuted(boolean value) { muted = value; prefs.edit().putBoolean("muted", value).apply(); if (value) stop(null); publish(value ? "Mode muet actif." : "Voix réactivée."); }
    public void stop(String message) {
        Job job = current; current = null;
        if (job != null) { main.removeCallbacks(job.watchdog); if (job.task != null) job.task.cancel(true); }
        if (tts != null) try { tts.stop(); } catch (RuntimeException ignored) {}
        if (player != null) { player.release(); player = null; }
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null && focus != null) manager.abandonAudioFocusRequest(focus);
        focus = null;
        if (job != null) job.delete();
        if (message != null) publish(message);
    }
    public void close() { closed = true; stop(null); if (tts != null) { try { tts.shutdown(); } catch (RuntimeException ignored) {} tts = null; } worker.shutdownNow(); }
}
