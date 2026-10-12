# Astor 0.1.9 — suivi de conversation

## Changement

Sujet et niveau d’apprentissage transportés dans le contexte local roulant de quatre échanges. Douze exercices préparés d’anglais et d’espagnol, trois difficultés, indices, réponses attendues, correction et demande de difficulté. Aucun moteur IA distant, aucune permission nouvelle et aucune modification des protections natives. Les niveaux sont choisis par l’utilisateur ; aucune évaluation globale de compétence.

## Tests effectués

- Tests du moteur JavaScript : vocabulaire de 0.1.8, routes locales, coffre AES-GCM, effacement et fermeture native, aucun réseau.
- Parcours anglais : débutant, exercice, simplification, bonne réponse, mauvais mot, indice, « Je ne sais pas », « Je n’y arrive pas », explication, correction, autre exercice, difficultés bornées à la banque disponible.
- Changement vers l’espagnol et réponse soy ; interruption par nouveau sujet ou projet ; Eve désactivée puis réactivée sans reprise du contexte effacé.
- Horloge injectée uniquement dans le test : expiration après cinq minutes. Secrets : oubli du sujet, aucun suivi construit à partir d’une donnée sensible. Une tentative de dialogue dans le Hub verrouillé ne produit aucune réponse.
- Tests DOM JSDOM 26 : cinq vues, contrôles, conversation, routage EVE, état du micro, brouillon et envoi manuel ; une dictée simulée « Donne-moi un exercice » conserve le sujet après navigation dans Notes. Aucune erreur JavaScript.
- Écrans et mise en page de 0.1.8 conservés ; pas de nouveau contrôle graphique Chromium dans cette version. Aucun appareil ou émulateur Android utilisé.

## Limites

Les exercices sont une petite banque fixe. Les réponses sont comparées à des variantes prévues ; aucune correction générale de langue, conversation libre ou connaissance encyclopédique. La dictée réelle reste celle du moteur Android en français : les mots anglais ou espagnols peuvent être mal transcrits et nécessiter une correction avant envoi. La biométrie réelle, le microphone, les notes après mise à jour et le cycle de vie restent à vérifier sur Samsung avec TESTS_ANDROID.md. Ce travail ne constitue pas un audit de sécurité.

## Données et accès

Le suivi est uniquement en mémoire vive. Les quatre derniers échanges portent un résumé borné du sujet, du niveau et de l’exercice courant ; aucun historique plus ancien ni profil permanent n’est ajouté. Verrouillage et réinitialisation suppriment ce suivi. Le coffre garde ses identifiants et son format AES-GCM, avec sa phrase distincte de la biométrie. Le Hub ne s’ouvre qu’après BIOMETRIC_STRONG au premier plan. Microphone au toucher et envoi manuel inchangés.

## APK final

Compilation assembleRelease et lintRelease réussie. Lint : 19 avertissements, aucune erreur bloquante. Signature APK vérifiée, certificat identique à 0.1.8. Identifiant fr.astor.hades.prototype, versionCode 11, versionName 0.1.9-conversation, Android minimum 11 (API 30), cible API 35. Permissions USE_BIOMETRIC et RECORD_AUDIO seulement, APK sans indicateur debuggable. Le fichier web de l’APK correspond exactement à la source testée et contient le moteur de suivi.

SHA-256 APK : `d7eaebb6c9b4e7523538168b29d23df48e209680d2aaf4f63b447b3bcf006aa1`. Ancien APK 0.1.8 inclus dans sauvegarde/Astor_018.apk.
