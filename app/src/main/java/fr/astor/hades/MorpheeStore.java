package fr.astor.hades;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypted, no-backup sleep diary. Do not overwrite unreadable/corrupt records. */
final class MorpheeStore {
    static final String PREFS = "morphee-settings-v1";
    private static final String ALIAS = "astor.morphee.sleep.v1";
    private static final int IV_SIZE = 12;
    private final AtomicFile file;
    private final Context context;

    MorpheeStore(Context context) {
        this.context = context.getApplicationContext();
        file = new AtomicFile(new File(this.context.getNoBackupFilesDir(), "morphee-history-v1.enc"));
    }

    SharedPreferences preferences() {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        KeyStore.Entry entry = store.getEntry(ALIAS, null);
        if (entry instanceof KeyStore.SecretKeyEntry)
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build());
        return generator.generateKey();
    }

    JSONArray read() throws Exception {
        if (!file.getBaseFile().exists() &&
                !new File(file.getBaseFile().getPath() + ".bak").exists()) return new JSONArray();
        byte[] data;
        try (FileInputStream stream = file.openRead(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
                if (bytes.size() > 1024 * 1024) throw new IllegalStateException("Historique trop volumineux");
            }
            data = bytes.toByteArray();
        }
        if (data.length < 30 || data[0] != 1) throw new IllegalStateException("Historique illisible");
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(data, 1, iv, 0, IV_SIZE);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
        byte[] plain = cipher.doFinal(data, 1 + IV_SIZE, data.length - 1 - IV_SIZE);
        return new JSONArray(new String(plain, StandardCharsets.UTF_8));
    }

    private void write(JSONArray history) throws Exception {
        if (history.length() > 365) throw new IllegalArgumentException("Maximum 365 entrées");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // Android Keystore generates the nonce with randomized encryption enabled.
        cipher.init(Cipher.ENCRYPT_MODE, key());
        byte[] iv = cipher.getIV();
        if (iv == null || iv.length != IV_SIZE)
            throw new IllegalStateException("Paramètre de chiffrement inattendu");
        byte[] payload = cipher.doFinal(history.toString().getBytes(StandardCharsets.UTF_8));
        FileOutputStream out = null;
        try {
            out = file.startWrite();
            out.write(1);
            out.write(iv);
            out.write(payload);
            file.finishWrite(out);
        } catch (Exception error) {
            if (out != null) file.failWrite(out);
            throw error;
        }
    }

    void add(String date, String sleep, String wake, int energy) throws Exception {
        if (energy < 1 || energy > 5) throw new IllegalArgumentException("Énergie entre 1 et 5");
        long minutes = MorpheePolicy.minutesSlept(date, sleep, wake, java.time.ZoneId.systemDefault());
        JSONArray history = read();
        JSONObject entry = new JSONObject();
        entry.put("date", date);
        entry.put("sleep", sleep);
        entry.put("wake", wake);
        entry.put("minutes", minutes);
        entry.put("energy", energy);
        history.put(entry);
        write(history);
    }

    void delete(int index) throws Exception {
        JSONArray history = read();
        if (index < 0 || index >= history.length()) throw new IllegalArgumentException("Entrée introuvable");
        history.remove(index);
        write(history);
    }

    void clearHistory() { file.delete(); }
}
