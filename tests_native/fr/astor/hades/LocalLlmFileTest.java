package fr.astor.hades;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;

/** Tests persisted file discovery separately from the Android native engine. */
public final class LocalLlmFileTest {
    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("astor-model-state-").toFile();
        File directory = new File(root, "local-model"); directory.mkdir();
        File model = new File(directory, "qwen3-0.6b-q8.gguf");
        LocalLlm first = new LocalLlm(root);
        if (first.installed()) throw new AssertionError("Missing model marked installed");
        try (RandomAccessFile output = new RandomAccessFile(model, "rw")) { output.setLength(100); }
        if (first.installed()) throw new AssertionError("Incomplete model marked installed");
        try (RandomAccessFile output = new RandomAccessFile(model, "rw")) { output.setLength(LocalLlm.MODEL_SIZE); }
        if (!first.installed()) throw new AssertionError("Complete persisted model not found");
        first.cancel(); first.close();
        LocalLlm reopened = new LocalLlm(root);
        if (!reopened.installed()) throw new AssertionError("Model lost across reopen");
        reopened.cancel(); reopened.close();
        if (!model.isFile() || model.length() != LocalLlm.MODEL_SIZE) throw new AssertionError("Lock deleted model");
        model.delete(); directory.delete(); root.delete();
        System.out.println("PASS: missing/incomplete model, completed file detection, reopen and cancellation preserve model");
    }
}
