package fr.astor.hades;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.InputStream;
import org.json.JSONObject;

/** Only a URI returned by the Android document picker reaches this reader. */
public final class SelectedFileReader {
    private SelectedFileReader() {}
    public static JSONObject read(ContentResolver resolver, Uri uri) throws Exception {
        if (!"content".equals(uri.getScheme())) throw new IllegalArgumentException("URI non autorisée.");
        String name = "Fichier sélectionné", mime = resolver.getType(uri);
        long size = -1;
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int ni = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int si = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (ni >= 0 && !cursor.isNull(ni)) name = cursor.getString(ni);
                if (si >= 0 && !cursor.isNull(si)) size = cursor.getLong(si);
            }
        }
        JSONObject result = new JSONObject().put("name", name == null ? "Fichier" : name.substring(0, Math.min(name.length(), 200)))
            .put("mime", mime == null ? "inconnu" : mime).put("size", size).put("text", "");
        if (!FileTextPolicy.isText(name, mime)) return result.put("notice", "Informations disponibles. Lecture du contenu PDF, image ou binaire non prise en charge dans cette version.");
        if (size > FileTextPolicy.MAX_BYTES) return result.put("notice", "Texte trop volumineux : maximum 64 Ko.");
        try (InputStream stream = resolver.openInputStream(uri)) {
            result.put("text", FileTextPolicy.read(stream)).put("notice", "Texte UTF-8 lu localement. Aucun envoi sur Internet.");
        } catch (java.io.IOException e) {
            result.put("notice", "Texte illisible, trop volumineux ou non UTF-8. Aucun contenu ajouté.");
        }
        return result;
    }
}
