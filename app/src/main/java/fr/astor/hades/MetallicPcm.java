package fr.astor.hades;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Bounded local PCM processing; no Android APIs and no network. */
public final class MetallicPcm {
    private MetallicPcm() {}
    public static final class Wave {
        public final int rate;
        public final float[] samples;
        Wave(int rate, float[] samples) { this.rate = rate; this.samples = samples; }
    }
    private static String tag(byte[] data, int start) {
        return new String(data, start, 4, StandardCharsets.US_ASCII);
    }
    public static Wave decode(byte[] bytes) {
        if (bytes.length < 44 || bytes.length > 24_000_000
                || !tag(bytes, 0).equals("RIFF") || !tag(bytes, 8).equals("WAVE"))
            throw new IllegalArgumentException("Unsupported wave");
        ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int format = 0, channels = 0, rate = 0, bits = 0, block = 0, dataStart = -1, dataLength = 0;
        for (int p = 12; p <= bytes.length - 8;) {
            long size = Integer.toUnsignedLong(b.getInt(p + 4));
            if (size > bytes.length - p - 8) throw new IllegalArgumentException("Wave chunk bounds");
            if (tag(bytes, p).equals("fmt ") && size >= 16) {
                format = Short.toUnsignedInt(b.getShort(p + 8));
                channels = Short.toUnsignedInt(b.getShort(p + 10));
                rate = b.getInt(p + 12);
                block = Short.toUnsignedInt(b.getShort(p + 20));
                bits = Short.toUnsignedInt(b.getShort(p + 22));
            } else if (tag(bytes, p).equals("data")) { dataStart = p + 8; dataLength = (int)size; }
            long next = p + 8L + size + (size & 1);
            if (next > bytes.length) break;
            p = (int)next;
        }
        int width = bits / 8;
        if (dataStart < 0 || rate < 8000 || rate > 96000 || channels < 1 || channels > 2
                || !((format == 1 && (bits == 8 || bits == 16)) || (format == 3 && bits == 32))
                || block != channels * width || dataLength == 0 || dataLength % block != 0)
            throw new IllegalArgumentException("Unsupported PCM");
        int frames = dataLength / block;
        if (frames > rate * 60) throw new IllegalArgumentException("Audio too long");
        float[] samples = new float[frames];
        for (int i = 0; i < frames; i++) {
            double sum = 0;
            for (int c = 0; c < channels; c++) {
                int at = dataStart + i * block + c * width;
                double x = format == 3 ? b.getFloat(at) : bits == 16 ? b.getShort(at) / 32768.0
                        : ((bytes[at] & 255) - 128) / 128.0;
                if (!Double.isFinite(x)) throw new IllegalArgumentException("Invalid PCM");
                sum += Math.max(-1, Math.min(1, x));
            }
            samples[i] = (float)(sum / channels);
        }
        return new Wave(rate, samples);
    }
    public static float[] process(float[] input, int rate, boolean metallic) {
        return process(input, rate, metallic ? "metal" : "grave");
    }
    public static float[] process(float[] input, int rate, String profile) {
        if (!("metal".equals(profile) || "grave".equals(profile) || "jarvis".equals(profile)))
            throw new IllegalArgumentException("Unknown profile");
        if (rate < 8000 || rate > 96000 || input.length == 0 || input.length > rate * 60)
            throw new IllegalArgumentException("Audio bounds");
        for (float x : input) if (!Float.isFinite(x) || Math.abs(x) > 1)
            throw new IllegalArgumentException("Invalid PCM");
        if ("grave".equals(profile)) return input.clone();
        final boolean assistant = "jarvis".equals(profile);
        final double factor = assistant ? 0.97 : 0.84, amount = assistant ? 0.08 : 0.45;
        float[] out = new float[(int)Math.ceil(input.length / factor)];
        double highAlpha = 1 / (1 + 2 * Math.PI * 75 / rate);
        double lowAlpha = 1 - Math.exp(-2 * Math.PI * (assistant ? 6500 : 3800) / rate);
        int delay = Math.max(1, (int)Math.round(rate * (assistant ? 0.015 : 0.024)));
        double previous = 0, high = 0, low = 0, peak = 0;
        for (int i = 0; i < out.length; i++) {
            if (Thread.currentThread().isInterrupted()) throw new IllegalStateException("Cancelled");
            double position = i * factor;
            int index = (int)position;
            double fraction = position - index;
            double sample = input[Math.min(index, input.length - 1)] * (1 - fraction)
                    + input[Math.min(index + 1, input.length - 1)] * fraction;
            high = highAlpha * (high + sample - previous); previous = sample;
            low += lowAlpha * (high - low);
            double shaped = Math.tanh(low * (1 + amount * 3)) / (1 + amount);
            double modulation = (1 - amount * 0.28) + amount * 0.28 * Math.sin(2 * Math.PI * 47 * i / rate);
            out[i] = (float)(shaped * modulation + (i >= delay ? out[i - delay] * amount * 0.2 : 0));
            peak = Math.max(peak, Math.abs(out[i]));
        }
        if (peak > 0.8) for (int i = 0; i < out.length; i++) out[i] *= (float)(0.8 / peak);
        int fade = Math.min((int)Math.round(rate * 0.008), out.length / 2);
        for (int i = 0; i < fade; i++) { out[i] *= (float)i / fade; out[out.length - 1 - i] *= (float)i / fade; }
        return out;
    }
    public static byte[] encode(float[] samples, int rate) {
        if (rate < 8000 || rate > 96000 || samples.length > rate * 75)
            throw new IllegalArgumentException("Wave bounds");
        ByteBuffer b = ByteBuffer.allocate(44 + samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(36 + samples.length * 2);
        b.put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16).putShort((short)1).putShort((short)1);
        b.putInt(rate).putInt(rate * 2).putShort((short)2).putShort((short)16);
        b.put("data".getBytes(StandardCharsets.US_ASCII)).putInt(samples.length * 2);
        for (float x : samples) {
            if (!Float.isFinite(x)) throw new IllegalArgumentException("Invalid PCM");
            x = Math.max(-1, Math.min(1, x));
            b.putShort((short)Math.round(x * (x < 0 ? 32768 : 32767)));
        }
        return b.array();
    }
}
