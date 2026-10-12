# Test Java des bornes des paramètres

Avec JDK 17, depuis la racine du projet :

```sh
mkdir -p /tmp/astor-settings-test
javac -d /tmp/astor-settings-test app/src/main/java/fr/astor/hades/PreferencePolicy.java tests_native/fr/astor/hades/PreferencePolicyTest.java
java -cp /tmp/astor-settings-test fr.astor.hades.PreferencePolicyTest
```

Ce test ne lance pas les widgets Android. Utiliser TESTS_ANDROID.md pour les essais sur téléphone.
