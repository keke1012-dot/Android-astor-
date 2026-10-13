package fr.astor.hades;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/** Fixed HTTPS endpoint, bounded response, no cookies, no remote scripts. */
public final class InternetLookup {
    public interface Callback { void done(String text, String error); }
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile long generation;
    private volatile HttpsURLConnection connection;
    public synchronized void cancel() {
        generation++;
        HttpsURLConnection old = connection;
        connection = null;
        if (old != null) old.disconnect();
    }
    public synchronized void search(String raw, Callback callback) {
        String endpoint = InternetPolicy.endpoint(raw);
        cancel();
        long token = generation;
        worker.execute(() -> {
            HttpsURLConnection current = null;
            try {
                current = (HttpsURLConnection) new URL(endpoint).openConnection();
                synchronized (this) { if (token != generation) return; connection = current; }
                current.setInstanceFollowRedirects(false);
                current.setConnectTimeout(7000);
                current.setReadTimeout(7000);
                current.setUseCaches(false);
                current.setRequestProperty("User-Agent", "AstorAndroid/0.2.7 (controlled encyclopedia lookup)");
                current.setRequestProperty("Accept", "application/json");
                if (current.getResponseCode() != 200) throw new IllegalStateException("Service indisponible");
                if (!current.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("application/json"))
                    throw new IllegalStateException("Réponse invalide");
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                try (InputStream stream = current.getInputStream()) {
                    byte[] buffer = new byte[4096]; int count;
                    while ((count = stream.read(buffer)) != -1) {
                        if (token != generation) return;
                        if (data.size() + count > 96000) throw new IllegalStateException("Réponse trop volumineuse");
                        data.write(buffer, 0, count);
                    }
                }
                JSONObject result = new JSONObject(data.toString(StandardCharsets.UTF_8.name()));
                if (result.has("error")) throw new IllegalStateException("Recherche indisponible");
                JSONObject query = result.optJSONObject("query");
                JSONArray pages = query == null ? null : query.optJSONArray("pages");
                StringBuilder text = new StringBuilder("WIKIPÉDIA · consultation Internet\n");
                if (pages == null || pages.length() == 0) text.append("Aucun résultat. Essayez des mots-clés plus simples.");
                else for (int i = 0; i < Math.min(3, pages.length()); i++) {
                    JSONObject page = pages.getJSONObject(i);
                    String title = page.optString("title", "Article");
                    String extract = page.optString("extract", "Extrait indisponible.");
                    text.append("\n\n").append(title.substring(0, Math.min(300, title.length())))
                        .append("\n").append(extract.substring(0, Math.min(1800, extract.length())))
                        .append("\nSource : https://fr.wikipedia.org/?curid=").append(page.getLong("pageid"));
                }
                text.append("\n\nExtraits de sources externes, à vérifier. Aucun apprentissage automatique ni enregistrement dans le coffre.");
                if (token == generation) callback.done(text.toString(), "");
            } catch (Exception error) {
                if (token == generation) callback.done("", "Connexion impossible. Vérifiez Internet et réessayez ; le mode local reste disponible.");
            } finally {
                if (current != null) current.disconnect();
                synchronized (this) { if (connection == current) connection = null; }
            }
        });
    }
    public void close() { cancel(); worker.shutdownNow(); }
}
