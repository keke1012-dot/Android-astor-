# HADES FORTERESSE — audit initial (2026-10-13)

Statut : audit statique partiel, NON validé en production. Aucun APK stable ni RC1 modifié.

## Constatations vérifiées sur main
- Dépôt GitHub public : vérifier exposition des secrets et historique Git.
- AndroidManifest.xml : INTERNET, RECORD_AUDIO et USE_BIOMETRIC ; `allowBackup=false`, `usesCleartextTraffic=false`.
- MainActivity.java : `FLAG_SECURE`, débogage WebView désactivé, contrôle biométrique fort, WebView avec accès fichiers désactivé et interception des requêtes distantes.
- Interface : coffre annoncé AES-GCM 256 avec dérivation PBKDF2 ; l'implémentation crypto et les paramètres doivent être audités séparément.
- .gitignore exclut *.jks, *.keystore, *.private.properties, *.apk, *.gguf ; cela ne prouve pas l'absence de secrets déjà présents dans l'historique.

## Priorités avant déploiement
1. Inventorier fichiers et historique Git pour secrets, identifiants et sauvegardes sensibles. Révoquer toute clé exposée ; envisager dépôt privé (ne remplace pas rotation).
2. Auditer coffre : aléa cryptographique, sel, nonce GCM unique, PBKDF2, effacement mémoire, échecs d'authentification, gestion des sauvegardes. Envisager clés non exportables Android Keystore / StrongBox selon matériel.
3. Auditer `JavascriptInterface`, origine WebView, schémas URL, navigation, permission micro, import de fichiers, validation des chemins et contenu.
4. Réseau : politique de destinations autorisées, TLS, refus des connexions en clair, validation certificats, aucun transfert implicite de mémoire personnelle.
5. Examiner dépendances, workflows CI, signature APK et protection des branches. Ajouter tests automatiques et scans de secrets.
6. Tests sur Samsung Android 16 : verrouillage, restauration, hors-ligne, tentatives d'accès non autorisé, régression et performances.

## Garde-fous
- Ne jamais modifier RC1 ni la branche stable sans validation explicite.
- Ne jamais chiffrer, effacer ou modifier arbitrairement les fichiers externes au bac à sable de l'application.
- Aucune garantie « inviolable » ; réévaluer les risques après chaque mise à jour.
