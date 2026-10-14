package fr.astor.hades;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
public class FileTextPolicyTest {
    static void require(boolean b) { if (!b) throw new AssertionError(); }
    static void rejects(byte[] bytes) throws Exception {
        try { FileTextPolicy.read(new ByteArrayInputStream(bytes)); throw new AssertionError("Accepted invalid file"); }
        catch (IOException expected) { }
    }
    public static void main(String[] args) throws Exception {
        require(FileTextPolicy.isText("menu.CSV", "application/octet-stream"));
        require(!FileTextPolicy.isText("archive.apk", "application/octet-stream"));
        require(!FileTextPolicy.isText("photo.jpg", "image/jpeg"));
        require(!FileTextPolicy.isText("document.pdf", "application/pdf"));
        String t="\uFEFFAllergènes : œufs, blé";
        require(FileTextPolicy.read(new ByteArrayInputStream(t.getBytes(StandardCharsets.UTF_8))).equals(t.substring(1)));
        require(FileTextPolicy.read(new ByteArrayInputStream(new byte[0])).isEmpty());
        rejects(new byte[]{0,1}); rejects(new byte[]{(byte)0xff});
        rejects(new byte[FileTextPolicy.MAX_BYTES+1]);
        byte[] exact=new byte[FileTextPolicy.MAX_BYTES]; java.util.Arrays.fill(exact,(byte)'a');
        require(FileTextPolicy.read(new ByteArrayInputStream(exact)).length()==FileTextPolicy.MAX_BYTES);
        System.out.println("FileTextPolicy: 11 checks passed");
    }
}
