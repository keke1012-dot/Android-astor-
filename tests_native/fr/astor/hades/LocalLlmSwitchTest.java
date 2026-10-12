package fr.astor.hades;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Checks both actual models can be selected without restarting the app. */
public final class LocalLlmSwitchTest {
    private static String ask(LocalLlm engine) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> answer = new AtomicReference<>("");
        AtomicReference<String> error = new AtomicReference<>("");
        engine.generate("<|im_start|>system\nAnswer in French, briefly.<|im_end|>\n<|im_start|>user\nExplique ce qu’est un trou noir.<|im_end|>\n<|im_start|>assistant\n<think>\n\n</think>\n\n", new LocalLlm.Listener() {
            public void state(String message, int percent) {}
            public void done(String text, String problem) { answer.set(text); error.set(problem); done.countDown(); }
        });
        if (engine.selectLight(!engine.usingLight())) throw new AssertionError("Changed model during calculation");
        if (!done.await(120, TimeUnit.SECONDS)) throw new AssertionError("Calculation timed out");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (engine.isBusy() && System.nanoTime() < deadline) Thread.sleep(10);
        if (engine.isBusy() || !error.get().isEmpty() || answer.get().isBlank()) throw new AssertionError("Calculation failed: " + error.get());
        return answer.get();
    }
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("astor-switch-");
        Path directory = Files.createDirectories(root.resolve("local-model"));
        Path light = directory.resolve("qwen3-0.6b-q8.gguf"), upgrade = directory.resolve("qwen3-1.7b-q4km.gguf");
        Files.createLink(light, Path.of(args[0])); Files.createLink(upgrade, Path.of(args[1]));
        LocalLlm engine = new LocalLlm(root.toFile());
        if (engine.usingLight() || !engine.ready()) throw new AssertionError("Upgrade not selected");
        String first = ask(engine);
        if (!engine.selectLight(true)) throw new AssertionError("Cannot select light model");
        String second = ask(engine);
        if (first.equals(second)) throw new AssertionError("Both model paths produced identical text");
        if (!engine.selectLight(false)) throw new AssertionError("Cannot return to upgrade");
        ask(engine); engine.close();
        if (!Files.exists(light) || !Files.exists(upgrade)) throw new AssertionError("Closing removed model files");
        Files.delete(light); Files.delete(upgrade); Files.delete(directory); Files.delete(root);
        System.out.println("PASS: actual upgraded/light/upgraded inference, busy selection rejected, both files retained");
    }
}
