package fr.astor.hades;
import android.content.Context;import android.os.*;import android.speech.*;import java.util.*;
public class VoiceDictationTest {
static class Output implements VoiceDictation.Callback {boolean listening;String message="";ArrayList<String> results=new ArrayList<>();public void state(boolean l,String m){listening=l;message=m;}public void result(String t){results.add(t);}}
static void check(boolean b){if(!b)throw new AssertionError();}
static Bundle result(String text){Bundle b=new Bundle();b.results=new ArrayList<>(List.of(text));return b;}
public static void main(String[] args){
Output o=new Output();VoiceDictation v=new VoiceDictation(new Context(),o);
v.start();SpeechRecognizer first=SpeechRecognizer.last;check(o.listening);v.stop();check(first.canceled&&first.destroyed);first.listener.onResults(result("late result"));check(o.results.isEmpty());
v.start();SpeechRecognizer second=SpeechRecognizer.last;first.listener.onError(7);check(o.listening);second.listener.onResults(result(" Bonjour Astor "));check(o.results.equals(List.of("Bonjour Astor")));check(!o.listening&&second.destroyed&&Handler.timeout==null);
v.start();SpeechRecognizer third=SpeechRecognizer.last;Handler.timeout.run();check(!o.listening&&third.destroyed);third.listener.onResults(result("late timeout"));check(o.results.size()==1);
v.start();SpeechRecognizer.last.listener.onError(13);check(!o.listening&&o.message.contains("Français"));
v.start();SpeechRecognizer.last.listener.onResults(new Bundle());check(!o.listening&&o.results.size()==1);
SpeechRecognizer.available=false;v.start();check(!o.listening);SpeechRecognizer.available=true;Build.VERSION.SDK_INT=30;check(!VoiceDictation.available(new Context()));
System.out.println("PASS: late results rejected, cancellation/destruction, timeout, empty result, unavailable service, French error; simulated Android API only.");}}

