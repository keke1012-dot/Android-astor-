package fr.astor.hades;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/** API credential encrypted at rest, bound to this app and recent strong biometrics. */
public final class ElevenCredentials {
    private static final String ALIAS = "astor-elevenlabs-api-v1";
    private final SharedPreferences preferences;
    private final byte[] aad;
    public ElevenCredentials(Context context) {
        preferences = context.getSharedPreferences("astor-elevenlabs-credential", Context.MODE_PRIVATE);
        aad = ("ASTOR-ELEVENLABS-KEY-V1:" + context.getPackageName()).getBytes(StandardCharsets.UTF_8);
    }
    public boolean configured() { return preferences.contains("encrypted"); }
    private SecretKey key(boolean create) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        if (store.containsAlias(ALIAS)) return (SecretKey)store.getKey(ALIAS, null);
        if (!create) throw new IllegalStateException("Credential key unavailable");
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(900, KeyProperties.AUTH_BIOMETRIC_STRONG).build());
        return generator.generateKey();
    }
    public synchronized void save(String value) throws Exception {
        if (!ElevenPolicy.validKey(value)) throw new IllegalArgumentException("Invalid credential");
        String record = CredentialEnvelope.seal(value, key(true), aad);
        if (!preferences.edit().putString("encrypted", record).commit()) throw new IllegalStateException("Credential not saved");
    }
    public synchronized String read() throws Exception {
        return CredentialEnvelope.open(preferences.getString("encrypted", ""), key(false), aad);
    }
    public synchronized void forget() {
        if (!preferences.edit().remove("encrypted").commit()) throw new IllegalStateException("Credential not removed");
    }
}
