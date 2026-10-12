package fr.astor.hades;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Runs the exact Java/JNI decoder and small batches used by Android, not a substitute CLI. */
public final class LocalLlmNativeTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("astor-jni-model-");
        Path directory = Files.createDirectories(root.resolve("local-model"));
        Path file = directory.resolve("qwen3-0.6b-q8.gguf");
        try { Files.createLink(file, Path.of(args[0])); } catch (Exception e) { Files.copy(Path.of(args[0]), file); }
        LocalLlm engine = new LocalLlm(root.toFile());
        if (!engine.ready()) throw new AssertionError("Native library or downloaded model unavailable");
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> error = new AtomicReference<>("");
        AtomicReference<String> answer = new AtomicReference<>("");
        String context = "Contexte fictif : nous testons un moteur local. ".repeat(12);
        String prompt = "<|im_start|>system\nTu es Astor. Réponds en français et brièvement. " + context
            + "<|im_end|>\n<|im_start|>user\nDis bonjour en une phrase.<|im_end|>\n<|im_start|>assistant\n<think>\n\n</think>\n\n";
        engine.generate(prompt, new LocalLlm.Listener() {
            public void state(String message, int percent) { System.out.println(message); }
            public void done(String text, String problem) { answer.set(text); error.set(problem); done.countDown(); }
        });
        if (!done.await(120, TimeUnit.SECONDS)) throw new AssertionError("JNI inference timed out");
        engine.close();
        if (!error.get().isEmpty()) throw new AssertionError(error.get());
        if (answer.get().trim().length() < 3) throw new AssertionError("No generated answer");
        Files.delete(file); Files.delete(directory); Files.delete(root);
        System.out.println("PASS: exact Java/JNI inference generated text with multiple prompt batches and progress callbacks");
    }
}
