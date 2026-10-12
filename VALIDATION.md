# ASTOR Android 0.2.1.1 — Mnémosyne V3.9.4.1
12 octobre 2026. SOURCES CORRIGÉES, PAS D’APK NOUVEAU.

## Changements
- Restauration du coffre corrigé de 0.1.9.1 : refus des formats invalides sans remplacement, contrôle des opérations interrompues, protection contre les conflits de stockage et les callbacks périmés.
- Restauration du verrouillage natif Android et de la liaison de dictée. Suppression du verrou web supplémentaire.
- Restauration du vocabulaire, des exercices Eve et de l’interface Android existante.
- Conservation de la classification mémoire, du récapitulatif temporaire et de Mnémosyne (12 entrées en lecture seule).
- Priorité aux commandes explicites ; recherche Mnémosyne pour les questions locales compatibles.
- Effacement du contexte Mnémosyne au verrouillage et au changement de sujet.
- Identifiant conservé : fr.astor.hades.prototype. versionCode 14, versionName 0.2.1.1-mnemosyne.
- Ancien APK déplacé dans sauvegarde avec un nom explicite : il ne contient PAS cette mise à jour.

## Vérifications de cette mise à jour
- node test_web.cjs : PASS.
- node test_vault_security.cjs : PASS.
- node test_mnemosyne.cjs : PASS (panneau simulé, recherche, routage, classification, effacement, absence d’écriture durable silencieuse et de requête réseau).
- Tests UI complets non exécutés : jsdom absent.
- Tests Java non exécutés : javac absent.
- Compilation tentée : Gradle 8.9 absent ; téléchargement bloqué par le réseau. SDK Android absent. Aucun APK compilé ni signé.
- Aucun test matériel ou installation Samsung effectué.

## Compilation et mise à jour
Dans un environnement avec JDK 17, SDK Android 35 et Gradle 8.9 : exécuter les trois tests Node, les tests UI/Java, puis ./gradlew :app:assembleRelease :app:lintRelease.
Fournir ASTOR_SIGNING_PROPERTIES via un fichier privé extérieur au projet, avec le certificat historique. Aucune clé privée n’est fournie dans cette archive.
Vérifier la signature du nouvel APK avant installation. Certificat attendu SHA-256 : 92c2176cba95abcf1257d4fe7fef6233743a7f72accf94cde17dc906afda2151.
Conserver une sauvegarde des données. Installer par-dessus ASTOR uniquement avec la même signature ; ne pas désinstaller ni effacer son stockage.
Tester ensuite biométrie, notes existantes, enregistrement et réouverture, verrouillage en arrière-plan, dictée, panneau Mnémosyne et question « Qui est Atlas ? ».

## Portée
Mnémosyne reste une base locale prédéfinie ; elle ne mémorise pas automatiquement les conversations. Les tests simulés ne prouvent pas le fonctionnement sur Samsung. Les bilans historiques concernent les anciennes versions, pas cette compilation.

## Compilation distante préparée
Le workflow .github/workflows/build.yml effectue les tests Node, UI et Java, compile et contrôle le certificat historique avant de rendre l’APK disponible. Sa syntaxe est vérifiée, mais il n’a PAS été exécuté.
Il nécessite un dépôt GitHub privé et les secrets ASTOR_KEYSTORE_B64, ASTOR_STORE_PASSWORD, ASTOR_KEY_ALIAS et ASTOR_KEY_PASSWORD, fournis par un canal sécurisé. Ne jamais ajouter la clé ou ses mots de passe au dépôt. Le script scripts/prepare_ci_signing.py prépare uniquement des fichiers temporaires privés.
La clé historique a été retrouvée dans Astor_Android_019_Projet_Sauvegarde.zip ; son certificat correspond au certificat attendu. Aucun secret n’a été ajouté à ce pack.
GitHub n’est pas encore connecté : aucune source ni clé n’a été envoyée à GitHub et aucune compilation distante n’a été lancée.
