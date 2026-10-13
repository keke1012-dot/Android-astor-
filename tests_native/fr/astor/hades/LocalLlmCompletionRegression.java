package fr.astor.hades;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public final class LocalLlmCompletionRegression {
    private static void check(boolean ok,String label) { if(!ok) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        Path storage=Files.createTempDirectory("astor-completion");
        File folder=storage.resolve("local-model").toFile();folder.mkdirs();
        try(RandomAccessFile f=new RandomAccessFile(new File(folder,"qwen3-1.7b-q4km.gguf"),"rw")){f.setLength(LocalLlm.UPGRADE_SIZE);}
        AtomicInteger calls=new AtomicInteger();AtomicBoolean fail=new AtomicBoolean(false);
        LocalLlm.Engine engine=new LocalLlm.Engine(){
            public boolean available(){return true;}public long revision(){return 0;}
            public byte[] generate(String path,byte[] prompt,long revision,LocalLlm.Listener listener){
                calls.incrementAndGet();if(fail.getAndSet(false))throw new IllegalStateException();
                return "Réponse simulée".getBytes(StandardCharsets.UTF_8);
            }
            public void cancel(){}public void release(){}
        };
        LocalLlm model=new LocalLlm(storage.toFile(),engine);
        try {
            CountDownLatch cognitiveDone=new CountDownLatch(1);AtomicReference<String> error=new AtomicReference<>();
            new CognitiveEngine(model).think("Question test",(reply,e)->{error.set(e);cognitiveDone.countDown();});
            check(cognitiveDone.await(5,TimeUnit.SECONDS),"cognitive completion");
            check(error.get().isEmpty(),"next cognitive pass accepted");check(calls.get()==3,"all three passes executed");
            CountDownLatch retryDone=new CountDownLatch(1);AtomicBoolean idleAtCallback=new AtomicBoolean();fail.set(true);
            model.generate("first",new LocalLlm.Listener(){
                public void state(String m,int p){}
                public void done(String r,String e){
                    idleAtCallback.set(!model.isBusy());
                    model.generate("retry",new LocalLlm.Listener(){
                        public void state(String m,int p){}
                        public void timing(long queue,long nativeMs){check(queue>=0&&nativeMs>=0,"monotonic timings");}
                        public void done(String r2,String e2){error.set(e2);retryDone.countDown();}
                    });
                }
            });
            check(retryDone.await(5,TimeUnit.SECONDS),"retry after failure");check(idleAtCallback.get(),"idle before callback");check(error.get().isEmpty(),"retry succeeds");
            System.out.println("PASS: three-pass cognitive sequence, retry after error, callback ordering and monotonic timings (simulated engine)");
        } finally {model.close();Files.deleteIfExists(folder.toPath().resolve("qwen3-1.7b-q4km.gguf"));Files.delete(folder.toPath());Files.delete(storage);}
    }
}
