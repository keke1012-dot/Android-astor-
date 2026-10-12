# Astor Android · 0.1.9 — suivi de conversation et exercices locaux

Mise à jour du prototype Android 0.1.0, fondée sur le Hub navigateur V3.8.2.1. Android 11 minimum.

Cette version s’affiche sous le nom Astor et son APK se nomme Astor.apk. Interface graphite, cyan et dorée, sans TARDIS. Navigation en bas : Accueil, Notes, Systèmes, Atlas et Média. Commandes rapides Statut, Heure, Aide ; noyau compact et transitions légères. Écran d’accès et paramètres natifs harmonisés. La fenêtre biométrique apparaît automatiquement au lancement et au retour de l’arrière-plan. Après annulation, elle ne se relance pas en boucle : le bouton Déverrouiller Astor permet de réessayer.

Installer Astor.apk par-dessus la version 0.1.0 à 0.1.8, sans la désinstaller. Le nom interne de l’application et la clé de signature sont identiques ; aucune migration du coffre n’est exécutée. Une sauvegarde de l’APK 0.1.0 est incluse. Android peut refuser une réinstallation d’une version antérieure : ne pas désinstaller l’application pour revenir en arrière sans sauvegarder les notes.

## Comportement prévu

- Écran fermé au lancement, avant la création du Hub.
- Déverrouillage uniquement par une biométrie forte validée par Android. Aucun accès au Hub par code, schéma ou mot de passe du téléphone. Android choisit parmi les modalités fortes disponibles ; Astor ne peut pas distinguer les différents doigts enregistrés ni garantir une modalité digitale exclusive sur tous les modèles. Ne conserver dans les réglages que ses propres biométries si l’accès doit rester personnel.
- Aucune collecte d’empreinte et aucun code Android enregistré dans ASTOR.
- Verrouillage dès que le Hub quitte le premier plan, par retour Android, par « verrouille » ou après le délai réglé de 1, 2 ou 5 minutes sans interaction.
- Une nouvelle authentification est requise après fermeture, rotation, changement d’application ou redémarrage. Les échanges temporaires disparaissent.
- La mémoire reste chiffrée par AES-GCM avec la phrase secrète existante du coffre. L’authentification Android ne remplace pas cette phrase.
- La protection d’accès native reste active même si le module conversationnel HADES est désactivé.
- Les captures d’écran du prototype sont bloquées pour protéger les notes visibles.

## Installation du prototype

Un APK de test, s’il est présent dans le pack, peut être installé sur un téléphone Android 11 ou ultérieur. Il est compilé en variante release, sans mode débogage, avec la même clé de test que les versions précédentes pour permettre la mise à jour. Ce n’est pas une publication Play Store ni une certification Samsung.

1. Conserver le fichier navigateur V3.8.2.1 et son stockage.
2. Copier l’APK sur le téléphone et l’ouvrir. Android peut demander l’autorisation d’installation pour l’application qui ouvre le fichier.
3. Ouvrir « Astor ».
4. Utiliser la fenêtre Android qui s’ouvre automatiquement ; après annulation, appuyer sur « Déverrouiller Astor » pour réessayer.
5. Faire les tests du fichier TESTS_ANDROID.md avant d’en faire votre version principale.

## Données et sauvegarde

Les notes du navigateur ne sont pas importées automatiquement dans cette application : son stockage est distinct. Elles ne sont ni remplacées ni effacées. Ne supprimer ni le stockage navigateur ni le Hub d’origine. Une migration chiffrée devra être ajoutée et validée séparément.

La sauvegarde HTML d’origine est incluse à l’identique dans `sauvegarde/`. Les identifiants de stockage et le format de coffre ne sont pas modifiés dans le Hub embarqué. Désinstaller l’application ou effacer ses données supprime son coffre local ; la copie du code ne sauvegarde pas les notes.

## Périmètre de ce premier prototype

Les commandes locales, le routage ASTOR/EVE/ATLAS/HADES/ORPHEUS et les notes chiffrées sont embarqués. Permission Microphone demandée uniquement au toucher du bouton micro, en plus de USE_BIOMETRIC. Aucune permission Internet, téléphone ou stockage partagé n’est demandée. Les requêtes Web internes sont bloquées hors du seul HTML embarqué. Les liens autorisés peuvent être ouverts dans le navigateur après confirmation et verrouillage du Hub.

Sélection d’un fichier audio, vidéo intégrée et IA distante restent désactivées. La dictée native locale est ajoutée dans 0.1.9. Leur intégration Android sera un chantier distinct. Le Hub navigateur sauvegardé conserve ses fonctions. Les réglages des modules restent temporaires et reviennent aux valeurs initiales à chaque nouvelle session.

## Recompiler

Ouvrir ce dossier dans Android Studio avec JDK 17. Utiliser Gradle 8.9, Android SDK 35 et Build Tools 34.0.0. Android Gradle Plugin est fixé à 8.7.3. AndroidX WebKit est fixé à 1.12.1.

Avec JDK 17 et un SDK configuré (le lanceur Gradle est fourni) :

```sh
./gradlew :app:assembleRelease :app:lintRelease
```

Résultat attendu : `app/build/outputs/apk/release/app-release.apk`. La clé de test créée pour ce prototype est incluse dans `signing/prototype-debug.keystore`, avec le mot de passe conventionnel de développement `android`. Conserver cette archive en privé permet de recompiler une mise à jour compatible avec cet APK. Cette clé de développement n’est pas destinée à une publication Play Store ; une signature de production devra être préparée séparément.

## Références officielles consultées le 11 octobre 2026

- https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.Builder
- https://developer.android.com/reference/android/hardware/biometrics/BiometricManager.Authenticators
- https://developer.android.com/develop/ui/views/layout/webapps/load-local-content
- https://developer.android.com/build/releases/agp-8-7-0-release-notes

Les tests sur le capteur et le cycle de vie réel restent nécessaires : une compilation réussie ne les remplace pas.

## Biométrie obligatoire et voix

L’absence d’empreinte compatible, un capteur indisponible ou un blocage biométrique laisse Astor fermé. Android peut exiger le code du téléphone pour rétablir son capteur ; cela ne permet pas directement d’ouvrir Astor. La phrase secrète du coffre reste obligatoire pour les notes. Un utilisateur connaissant le code du téléphone peut modifier les biométries enregistrées : cette version ne lie pas le coffre à un ensemble d’empreintes immuable.

L’empreinte vocale n’est pas implémentée ; aucun microphone ni modèle de voix n’est utilisé. Le plan de travail est dans EMPREINTE_VOCALE_PLAN.md. Une reconnaissance de commande vocale ne sera jamais assimilée à une authentification de locuteur.

## Musique d’entrée « Éveil — signal Astor »

Courte composition synthétique originale de 7,8 secondes, sans échantillon ni reprise de morceau existant. Le WAV stéréo est embarqué dans l’application ; aucun téléchargement ou service musical n’est utilisé.

Elle commence une seule fois à chaque ouverture du Hub après validation biométrique et chargement visible. Annuler l’authentification ne joue aucun son. Le verrouillage, le passage en arrière-plan, la fermeture et la perte d’accès audio arrêtent et libèrent le lecteur. L’échec du lecteur ne donne jamais accès au Hub et ne modifie pas les notes.

La musique est activée par défaut. Pour la désactiver, ouvrir Paramètres dans l’en-tête du Hub, ou annuler la fenêtre biométrique puis ouvrir Paramètres Astor sur l’écran d’accès. Couper Musique d’entrée et Enregistrer ; le choix persiste pour les prochaines ouvertures. Aucun son si le téléphone est en mode silencieux/vibreur ou si Android signale une musique déjà active. Les réglages sonores Android restent prioritaires. Gain du lecteur réglable de 0 à 100 %, avec 35 % par défaut ; aucun volume système n’est changé.

Le script compose_entree.py permet de recréer exactement le WAV avec Python et NumPy. La biométrie reste obligatoire et l’empreinte vocale reste non implémentée.

## Paramètres réglables

Accès : icône engrenage Paramètres dans l’en-tête du Hub ; Paramètres Astor sur l’écran d’accès après annulation de la demande biométrique. Les modifications prennent effet après Enregistrer. Annuler conserve les réglages précédents.

- Musique d’entrée : activée ou désactivée.
- Volume du thème : 0–100 %, 35 % par défaut ; 0 coupe le thème. Pendant une lecture, Enregistrer ajuste le volume ; activer la musique ne relance pas le thème dans la session actuelle.
- Verrouillage du Hub après inactivité : 1, 2 ou 5 minutes, 5 par défaut. Aucun réglage ne permet de désactiver le verrouillage ; le passage en arrière-plan verrouille toujours immédiatement. Le coffre possède aussi sa protection interne à cinq minutes.
- Taille du texte du Hub : 90, 100, 110 ou 120 %. Ce réglage concerne le contenu Web du Hub ; les écrans natifs utilisent la taille Android.
- Animations du Hub : activées ou désactivées. La préférence de réduction des animations du téléphone reste prioritaire dans le contenu Web.

Réglages stockés localement, sans notes ni données biométriques. Aucune permission supplémentaire. Le panneau se ferme au passage en arrière-plan ou au verrouillage ; les modifications non enregistrées sont perdues.

Nouvelle icône adaptative : A lumineux cyan, violet et doré, anneau segmenté et fond bleu nuit. Android et le lanceur choisissent le masque d’affichage.

## Logo animé

L’icône du lanceur Android reste fixe. Dans l’écran d’accès natif et dans le noyau du Hub, le A reste immobile tandis que l’anneau effectue une rotation douce de douze secondes. Le réglage Animations coupe ces mouvements ; les paramètres Android de réduction des animations sont respectés lorsqu’ils sont signalés par la plateforme. L’animation native est arrêtée à l’arrière-plan, à l’ouverture du Hub et à la destruction de l’activité. Aucun service en arrière-plan ajouté.

L’aperçu GIF illustre la rotation ; il ne constitue pas une capture du téléphone. Les masques et tailles du lanceur dépendent d’Android.

## Parler à Astor

1. Installer comme mise à jour et ouvrir par biométrie.
2. Dans Accueil, toucher le micro à droite de la saisie ; autoriser le microphone lorsqu’Android le demande. Cette autorisation ne déverrouille jamais Astor. Si la demande passe en arrière-plan, le Hub se ferme comme auparavant : réauthentifier puis retoucher le micro.
3. Parler en français, par exemple « Je veux apprendre l’anglais ».
4. La transcription est ajoutée au brouillon déjà présent ; relire, corriger puis toucher Envoyer. Aucun envoi automatique.
5. Retoucher le micro pendant l’écoute pour annuler. La navigation vers une autre vue annule aussi la dictée.

Moteur : createOnDeviceSpeechRecognizer, exclusivement, sur Android 12 et plus. Astor ne passe jamais au moteur générique ni à une reconnaissance distante. Le français doit être disponible localement dans le service vocal du téléphone ; une erreur claire s’affiche sinon. L’application reste installable sous Android 11, mais sa dictée locale y est indisponible. Aucun téléchargement automatique de modèle.

Session limitée à 20 secondes, sans écoute de fond. Le lecteur d’entrée est arrêté avant la dictée. Les résultats reçus après annulation ou verrouillage sont rejetés ; aucun fichier audio, empreinte vocale ou journal de transcription n’est créé par Astor. La transcription est un brouillon temporaire puis un échange temporaire si elle est envoyée. Le moteur système appartient à Android : son fonctionnement réel et les langues installées doivent être vérifiés sur l’appareil, idéalement aussi en mode avion.

Il s’agit de dictée, pas d’identification du locuteur, de déverrouillage vocal ou de réponse audio. La biométrie forte reste obligatoire. Révoquer l’autorisation Microphone dans les réglages Android laisse le clavier utilisable.

Références API : https://developer.android.com/reference/android/speech/SpeechRecognizer et https://developer.android.com/reference/android/speech/RecognizerIntent (consultées le 11 octobre 2026).

## Correction de la commande vocale Statut

Sur une saisie vide, les transcriptions courtes « statue », « statut », « statuts », « status » et leur forme précédée d’« Astor » sont reconnues comme la commande Statut. La saisie affiche Statut pour relecture ; l’envoi reste manuel. Une phrase comme « la statue de la liberté » ou une dictée ajoutée à un brouillon ne subit pas cette correction. La précision acoustique du moteur Android n’est pas modifiée.

## Vocabulaire local étendu

Commandes formulées naturellement et avec politesse, présentation des rôles, choix explicite d’un module, mots supplémentaires pour l’apprentissage et l’organisation. « Peux-tu me donner l’heure ? », « Montre mes notes », « Passe à Eve », « Contrôle cette affirmation » et « Changeons de sujet » sont compris. Le vocabulaire est commun aux demandes écrites et aux transcriptions vocales. Aucune modification du moteur acoustique Android, aucun entraînement automatique ni conversation IA libre.

Les commandes sont reconnues par familles exactes, sans recherche approximative générale. Les notes exigent toujours leur phrase secrète. Les modules désactivés restent bloqués. La négation d’une commande n’exécute pas cette commande. Aucun accès vocal au coffre, aucune suppression de notes ajoutée. La réponse Orpheus rappelle la disponibilité réelle du lecteur intégré.

Voir VOCABULAIRE_FR.md pour les exemples et TESTS_ANDROID.md pour les essais sur téléphone. La biométrie, le coffre, le microphone à activation tactile et l’envoi manuel sont conservés.

## Suivi de conversation

Eve garde le sujet d’apprentissage et le niveau choisi dans le contexte temporaire. Parcours : « Je veux apprendre l’anglais », « Je débute », « Donne-moi un exercice », « Plus simple ». Le même parcours existe pour l’espagnol. Douze exercices fixes sont inclus : deux par difficulté et par langue. Les difficultés sont bases, intermédiaire et défi ; ce ne sont pas des niveaux CECR certifiés. Le niveau change à votre demande, pas à partir d’un diagnostic automatique.

« Un indice », « Je ne sais pas », « La correction », « Pourquoi ? », « Un autre exercice » et « Plus difficile » utilisent l’exercice courant. Les réponses attendues ou leurs variantes prévues sont reconnues ; les mots courts ou une réponse introduite par « Réponse : » sont comparés à cet exercice. Le moteur ne corrige pas librement toute phrase ou toute traduction. Une bonne réponse signifie seulement qu’elle correspond à cet item. La dictée reste réglée en français : relire les réponses étrangères, les corriger ou utiliser le clavier.

Pour les autres matières, Eve demande une notion précise et peut guider avec des questions ; aucune banque libre de connaissances n’a été ajoutée. Une demande explicite d’une autre langue ouvre un nouveau sujet. « Changeons de sujet », le choix manuel d’un module et sa désactivation interrompent le suivi. Le contexte expire après cinq minutes, est limité aux quatre derniers échanges avec un état résumé du sujet courant, reste en mémoire vive et est effacé au verrouillage. Les notes chiffrées sont distinctes : aucune réponse scolaire n’est ajoutée au coffre automatiquement.

Les données ressemblant à des secrets ne sont pas utilisées pour ce suivi. Une commande comme Statut ou Urgence reste prioritaire sur une réponse d’exercice. Eve désactivée ne peut pas être réactivée par une formulation vocale. L’interface et les protections natives de 0.1.8 sont conservées.
