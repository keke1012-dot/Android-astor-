package fr.astor.hades;
public final class MetallicPcmTest {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) {
        int rate = 16000;
        float[] input = new float[rate];
        for (int i = 0; i < input.length; i++) input[i] = (float)(0.6 * Math.sin(2 * Math.PI * 200 * i / rate));
        MetallicPcm.Wave decoded = MetallicPcm.decode(MetallicPcm.encode(input, rate));
        check(decoded.rate == rate && decoded.samples.length == rate);
        check(Math.abs(decoded.samples[10] - input[10]) < 0.0001);
        float[] output = MetallicPcm.process(decoded.samples, rate, true);
        check(output.length == (int)Math.ceil(rate / 0.84));
        for (float x : output) check(Float.isFinite(x) && Math.abs(x) <= 0.800001);
        check(MetallicPcm.decode(MetallicPcm.encode(output, rate)).samples.length == output.length);
        for (float x : MetallicPcm.process(new float[rate], rate, true)) check(x == 0);
        check(MetallicPcm.process(input, rate, false).length == input.length);
        try { MetallicPcm.decode(new byte[44]); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        try { MetallicPcm.process(new float[]{Float.NaN}, rate, true); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        try { MetallicPcm.process(new float[rate * 61], rate, true); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        byte[] broken = MetallicPcm.encode(input, rate); broken[40] = (byte)255; broken[41] = (byte)255; broken[42] = (byte)255; broken[43] = (byte)127;
        try { MetallicPcm.decode(broken); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        System.out.println("PASS: French voice PCM bounds, wave format, metallic treatment, silence and peak ceiling");
    }
}
