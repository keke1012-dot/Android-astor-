package fr.astor.hades;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
public final class CredentialEnvelopeTest {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    private interface Operation { void run() throws Exception; }
    private static void fails(Operation operation) throws Exception { boolean rejected = false; try { operation.run(); } catch (Exception expected) { rejected = true; } check(rejected); }
    public static void main(String[] args) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES"); generator.init(256);
        SecretKey key = generator.generateKey(), other = generator.generateKey();
        byte[] aad = "TEST-APP-BOUND-CREDENTIAL".getBytes(StandardCharsets.UTF_8);
        String example = "EXAMPLE_ONLY_NOT_A_REAL_API_KEY";
        String first = CredentialEnvelope.seal(example, key, aad), second = CredentialEnvelope.seal(example, key, aad);
        check(!first.equals(second)); check(!first.contains(example));
        check(CredentialEnvelope.open(first, key, aad).equals(example));
        fails(() -> CredentialEnvelope.open(first, other, aad));
        fails(() -> CredentialEnvelope.open(first, key, new byte[]{1}));
        String[] parts = first.split("\\."); byte[] ciphertext = Base64.getDecoder().decode(parts[2]); ciphertext[0] ^= 1;
        String tampered = parts[0] + "." + parts[1] + "." + Base64.getEncoder().encodeToString(ciphertext);
        fails(() -> CredentialEnvelope.open(tampered, key, aad));
        fails(() -> CredentialEnvelope.open(example, key, aad));
        fails(() -> CredentialEnvelope.open("v2." + parts[1] + "." + parts[2], key, aad));
        System.out.println("PASS: credential encryption roundtrip, fresh nonces, tamper/wrong-key/wrong-app rejection and no plaintext fallback");
    }
}
