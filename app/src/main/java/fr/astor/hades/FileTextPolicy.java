package fr.astor.hades;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Local text only. Never executes markup or accepts binary as text. */
public final class FileTextPolicy {
    public static final int MAX_BYTES = 65536;
    private FileTextPolicy() {}
    public static boolean isText(String name, String mime) {
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
        String m = mime == null ? "" : mime.toLowerCase(Locale.ROOT);
        return m.startsWith("text/") || m.equals("application/json")
            || n.matches(".*\\.(txt|md|csv|tsv|json|log|xml|html|htm)$");
    }
    public static String read(InputStream stream) throws IOException {
        if (stream == null) throw new IOException("Fichier inaccessible.");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = stream.read(buffer)) != -1) {
            if (bytes.size() + n > MAX_BYTES) throw new IOException("Texte trop volumineux : maximum 64 Ko.");
            bytes.write(buffer, 0, n);
        }
        String text = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes.toByteArray())).toString();
        if (text.indexOf('\0') >= 0) throw new IOException("Fichier binaire non pris en charge.");
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }
}
