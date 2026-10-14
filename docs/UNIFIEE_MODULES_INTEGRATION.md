# ASTOR Unifiée — intégration des modules (candidate)

Base technique: feature/astor-voice-ui-v1, qui contient le Hub, les classes Morphée, les tests du langage intermodules, Hades et la nouvelle interface vocale. Cette branche candidate n'est pas la RC1.

## État prouvé par inspection statique
- index.html contient des références à ASTOR, HADES, ATLAS, EVE, MNEMOSYNE, MORPHEE, ORPHEUS, TEMPEST.
- Les classes Android Morphée sont présentes dans le code source.
- Des suites de tests UI, voix, sécurité, mémoire et langage sont présentes.
- **Non prouvé** : exécution de chaque module sur le téléphone, fonctionnement complet, signature et absence de régression.

## Gate de livraison
1. Exécuter tests_ui, test_module_language.cjs, test_hades.cjs, test_mnemosyne.cjs, test_durable_memory.cjs, tests_native, tests_voice.
2. Vérifier que chaque panneau est visible et que chaque action appelle un service réel, pas seulement un message simulé.
3. Vérifier hors ligne, redémarrage, restauration mémoire, autorisations Android niveau 2, alertes, notifications Morphée, voix locale sans ElevenLabs et Orpheus via services autorisés.
4. Compiler candidate APK avec versionCode distinct, vérifier la signature et la compatibilité de mise à jour; préserver données et sauvegardes.
5. Tester sur Samsung Android 16; comparer les temps de réponse, batterie et stabilité à la RC1.
6. Ne pas fusionner vers main ni publier sans résultats documentés.
