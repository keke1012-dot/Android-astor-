package fr.astor.hades;
import java.time.ZoneId;
public final class PhoneAccessPolicyTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args){
        check(PhoneAccessPolicy.allowed(true,true,true,4,4));
        check(!PhoneAccessPolicy.allowed(false,true,true,4,4));
        check(!PhoneAccessPolicy.allowed(true,false,true,4,4));
        check(!PhoneAccessPolicy.allowed(true,true,false,4,4));
        check(!PhoneAccessPolicy.allowed(true,true,true,4,5));
        check(PhoneAccessPolicy.notificationText("Votre code est 123456").equals("Contenu sensible masqué"));
        check(!PhoneAccessPolicy.notificationText("Connexion 987654").contains("987654"));
        check(PhoneAccessPolicy.notificationText("Bonjour").equals("Bonjour"));
        long start=PhoneAccessPolicy.eventStart("2026-10-15","09:00",ZoneId.of("Europe/Paris"));
        check(PhoneAccessPolicy.eventEnd(start,60)-start==3_600_000L);
        boolean invalid=false;try{PhoneAccessPolicy.eventEnd(start,0);}catch(IllegalArgumentException expected){invalid=true;}check(invalid);
        invalid=false;try{PhoneAccessPolicy.feature("contacts");}catch(IllegalArgumentException expected){invalid=true;}check(invalid);
        System.out.println("PASS: N2 foreground/biometric gates, revocation, stale callbacks, masking and event limits");
    }
}
