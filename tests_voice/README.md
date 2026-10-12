# Tests du contrôleur de dictée

Les classes android de ce dossier sont des doubles de test, pas une implémentation Android. Elles ne sont jamais compilées dans l’APK. Elles vérifient l’invalidation des résultats tardifs, la destruction, le délai et les erreurs. Elles ne mesurent ni la précision vocale ni le comportement réel du moteur Samsung.

Compiler VoiceDictation.java avec ces fichiers Java dans un dossier temporaire, puis exécuter fr.astor.hades.VoiceDictationTest avec Java 17.

