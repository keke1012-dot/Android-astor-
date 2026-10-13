package fr.astor.hades;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Versioned authenticated envelope; caller owns the non-exportable storage key. */
public final class CredentialEnvelope {
    private CredentialEnvelope() {}
    public static String seal(String value, SecretKey key, byte[] aad) throws Exception {
        if (!ElevenPolicy.validKey(value)) throw new IllegalArgumentException("Invalid credential");
        byte[] plaintext = value.getBytes(StandardCharsets.UTF_8);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key); cipher.updateAAD(aad);
            return "v1." + Base64.getEncoder().encodeToString(cipher.getIV()) + "."
                    + Base64.getEncoder().encodeToString(cipher.doFinal(plaintext));
        } finally { Arrays.fill(plaintext, (byte)0); }
    }
    public static String open(String record, SecretKey key, byte[] aad) throws Exception {
        if (record == null || record.length() > 1024) throw new IllegalArgumentException("Invalid credential record");
        String[] parts = record.split("\\.", -1);
        if (parts.length != 3 || !"v1".equals(parts[0])) throw new IllegalArgumentException("Invalid credential envelope");
        byte[] nonce = Base64.getDecoder().decode(parts[1]), ciphertext = Base64.getDecoder().decode(parts[2]);
        if (nonce.length != 12 || ciphertext.length < 16 || ciphertext.length > 512) throw new IllegalArgumentException("Credential bounds");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, nonce)); cipher.updateAAD(aad);
        byte[] plaintext = cipher.doFinal(ciphertext);
        try {
            String value = new String(plaintext, StandardCharsets.UTF_8);
            if (!ElevenPolicy.validKey(value)) throw new IllegalArgumentException("Invalid credential");
            return value;
        } finally { Arrays.fill(plaintext, (byte)0); }
    }
}
