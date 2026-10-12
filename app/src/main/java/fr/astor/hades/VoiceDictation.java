package fr.astor.hades;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;

/** Short, user-initiated on-device transcription. Never authenticates or submits. */
final class VoiceDictation {
    interface Callback {
        void state(boolean listening, String message);
        void result(String text);
    }
    private final Context context;
    private final Callback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private long generation;
    private final Runnable timeout = () -> finish("Dictée terminée après 20 secondes. Réessayez avec une phrase courte.");

    VoiceDictation(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    static boolean available(Context context) {
        return Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context);
    }

    void start() {
        stop();
        final long current = generation;
        try {
            if (!available(context)) {
                callback.state(false, "Dictée locale indisponible. Android 12 ou plus et un moteur vocal local sont nécessaires.");
                return;
            }
            recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context);
            recognizer.setRecognitionListener(new RecognitionListener() {
                private boolean valid() { return current == generation && recognizer != null; }
                @Override public void onReadyForSpeech(Bundle params) {
                    if (valid()) callback.state(true, "Parlez en français. Touchez de nouveau le micro pour annuler.");
                }
                @Override public void onBeginningOfSpeech() { }
                @Override public void onRmsChanged(float rmsdB) { }
                @Override public void onBufferReceived(byte[] buffer) { /* No recording retained. */ }
                @Override public void onEndOfSpeech() {
                    if (valid()) callback.state(true, "Transcription en cours… Touchez le micro pour annuler.");
                }
                @Override public void onError(int error) {
                    if (!valid()) return;
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
                        case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                            message = "Français local indisponible. Vérifiez les langues hors ligne du moteur vocal dans Android.";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Microphone non autorisé. Vérifiez l’autorisation Microphone d’Astor dans Android.";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "Aucune phrase reconnue. Touchez le micro et réessayez.";
                            break;
                        default:
                            message = "Dictée locale indisponible pour le moment. Réessayez ou utilisez le clavier.";
                    }
                    finish(message);
                }
                @Override public void onResults(Bundle results) {
                    if (!valid()) return;
                    ArrayList<String> matches = results == null ? null
                            : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String text = matches == null || matches.isEmpty() || matches.get(0) == null
                            ? "" : matches.get(0).trim();
                    stop();
                    if (text.isEmpty()) callback.state(false, "Aucune phrase reconnue. Réessayez.");
                    else {
                        callback.result(text.substring(0, Math.min(text.length(), 5000)));
                        callback.state(false, "Relisez votre demande, puis touchez Envoyer.");
                    }
                }
                @Override public void onPartialResults(Bundle partialResults) { }
                @Override public void onEvent(int eventType, Bundle params) { }
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            callback.state(true, "Préparation du micro… Touchez de nouveau pour annuler.");
            handler.postDelayed(timeout, 20_000L);
            recognizer.startListening(intent);
        } catch (RuntimeException error) {
            finish("Impossible d’ouvrir la dictée locale. Vérifiez le microphone et le moteur vocal Android.");
        }
    }

    private void finish(String message) {
        stop();
        callback.state(false, message);
    }

    void stop() {
        ++generation; // Invalidate results already queued before cancellation/background.
        handler.removeCallbacks(timeout);
        SpeechRecognizer old = recognizer;
        recognizer = null;
        if (old != null) {
            try { old.cancel(); } catch (RuntimeException ignored) { }
            try { old.destroy(); } catch (RuntimeException ignored) { }
        }
    }
}
