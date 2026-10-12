# ASTOR 0.1.9.1 — Corrections du coffre

11 octobre 2026. Base : Astor Android 0.1.9-conversation.

## Résultat

Les deux anomalies A1/A2 de l’audit initial ont été corrigées et leurs cas de reproduction sont couverts par des tests. L’APK signé est prêt pour une mise à jour du prototype ; sa validation sur Samsung reste à effectuer.

- Un coffre absent est distingué d’un coffre illisible ou inaccessible. Les erreurs bloquent la création et les octets existants restent intacts.
- Version, dérivation, sel, IV et tableaux d’octets sont contrôlés avant utilisation. Un format inconnu est refusé sans remplacement.
- Ouvrir un coffre existant ne le réécrit plus. Une mauvaise phrase ne modifie pas le coffre.
- Chaque opération asynchrone possède un identifiant invalidé au verrouillage et à la suppression. Cet identifiant est contrôlé après les calculs cryptographiques et avant affichage ou écriture.
- Clé, sel et notes d’une opération restent dans ses variables locales. Les déverrouillages et sauvegardes simultanés sont bloqués ; les callbacks périmés n’affectent pas une nouvelle session.
- Une modification du stockage pendant une opération est détectée ; le coffre modifié n’est pas écrasé.
- Les notes anciennes ne sont supprimées qu’après réussite de leur migration chiffrée. Une opération interrompue ne recrée pas un coffre supprimé.

Identifiants du stockage, format AES-GCM/PBKDF2 et phrase existante conservés. Sources natives, manifeste et règles de sauvegarde identiques à la base 0.1.9. Aucun accès réseau ou moteur IA ajouté.

## Vérifications effectuées

| Vérification | Résultat |
| --- | --- |
| test_web.cjs | Réussite : conversation, exercices, routage, coffre, verrouillage, zéro fetch dans les parcours testés. |
| test_vault_security.cjs | Réussite : coffres invalides, erreurs de lecture, compatibilité, mauvaise phrase, migration, annulation, interruptions de dérivation/déchiffrement/chiffrement, suppression, doubles clics, callbacks périmés, conflits de stockage, échec de sauvegarde. |
| tests_ui/test-ui.cjs | Réussite : cinq vues, commandes, dictée simulée, conservation du sujet, aucune erreur JavaScript. |
| PreferencePolicyTest | Réussite : bornes des réglages et verrouillage obligatoire. |
| VoiceDictationTest | Réussite avec doubles Android : résultats tardifs, arrêt, délai, erreurs. |
| assembleRelease et lintRelease | Réussite hors ligne. Lint : 19 avertissements, zéro erreur ; avertissements préexistants conservés. |
| apksigner | Signature v2 valide, certificat identique à celui de 0.1.9. |
| APK / sources | HTML embarqué identique au fichier corrigé. Sources natives et protections natives inchangées. |

Les outils de compilation et les dépendances de test ont été retrouvés dans le cache de la session de développement précédente après l’audit initial. Les vérifications ci-dessus ont donc pu être relancées.

APK : applicationId `fr.astor.hades.prototype`, versionCode `12`, versionName `0.1.9.1-coffre`, Android minimum API 30, cible API 35. Permissions biométrie et microphone uniquement ; variante release non débogable.

SHA-256 APK : `62bd8259a83ff71f4a50a5884221881ea7d211639724e5861cdd2e4f9df9d809`.

Certificat SHA-256 : `92c2176cba95abcf1257d4fe7fef6233743a7f72accf94cde17dc906afda2151`.

## Installation et validation Samsung

1. Installer Astor.apk par-dessus l’application existante, sans la désinstaller et sans vider ses données. Même identifiant et certificat, versionCode supérieur : conditions de mise à jour compatibles. Aucun téléphone n’a été utilisé dans cette session.
2. Déverrouiller Astor avec la biométrie, puis le coffre avec la phrase habituelle. Vérifier les notes existantes.
3. Ajouter une note fictive, enregistrer, refermer et rouvrir le coffre ; vérifier la note.
4. Vérifier le verrouillage pendant une ouverture, le retour Android, l’arrière-plan et le retour à l’application. Le texte des notes doit rester masqué tant que le coffre n’a pas été rouvert.
5. Vérifier le microphone, son annulation et le parcours d’apprentissage habituel.

Les tests de corruption et de suppression ont utilisé uniquement des données fictives dans le harnais. Ne pas corrompre le vrai coffre du téléphone pour les reproduire. La correction prévient le remplacement d’un coffre endommagé ; elle ne récupère pas les données déjà perdues.

## Sauvegardes et signature

La sauvegarde 0.1.9 d’origine demeure conservée. Le nouveau pack inclut les sources corrigées, les tests, ce bilan, l’APK corrigé et l’APK 0.1.9 précédent. Une réinstallation d’une version plus ancienne peut être refusée par Android ; ne pas désinstaller pour contourner ce refus.

La clé historique est réutilisée pour signer cette mise à jour, mais elle et ses mots de passe sont exclus du nouveau pack de sources. La configuration de compilation charge désormais un fichier privé indiqué par ASTOR_SIGNING_PROPERTIES. Les anciennes archives contenant la clé restent à accès restreint ; cette correction ne révoque pas les copies déjà présentes. La signature reste celle du prototype, sans publication Play Store.

Pas de déploiement distant, de test matériel, de certification de sécurité ou de validation Samsung réalisé. Prochaine étape : validation sur Samsung, puis reprise du cerveau conversationnel.
