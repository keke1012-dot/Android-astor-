# ASTOR Unifiée 0.2.9.1 — nouvelle signature durable

Le rappel Learning utilise la question réelle séparée du prompt technique, en génération simple comme cognitive. Les signes arithmétiques sont conservés pour éviter de confondre les questions. La validation, le refus ou l'annulation sont affichés après confirmation Android. Les corrections restent temporaires, sont effacées au verrouillage et ne sont pas conservées dans l'historique générique.

À la demande de l'utilisateur du 13 octobre 2026, une nouvelle clé RSA-3072, certificat SHA256withRSA et conteneur privé PKCS12 chiffré, remplace la signature de test de la 0.2.9. La clé privée et le mot de passe ne figurent jamais dans Git ni dans les artefacts publics. Le certificat public est référencé dans security/UNIFIED_CERTIFICATE_SHA256.txt. scripts/verify_unified_signature.py refuse toute APK signée avec une autre identité.

Identifiant maintenu : fr.astor.hades.unified.test.unified029. versionCode 29, versionName 0.2.9.1-unifiee. Le changement initial de clé ne permet pas une mise à jour directe de la 0.2.9. Exporter les données chiffrées avant toute désinstallation, vérifier la sauvegarde, puis remplacer la 0.2.9 et restaurer dans l'application vide. Les modèles devront être réinstallés. Garder la sauvegarde de la clé et son mot de passe séparément pour les prochaines signatures.

Les mises à jour ultérieures doivent conserver cet identifiant, cette clé, un versionCode croissant et être vérifiées avant livraison. Compiler une release non signée via -PastorUnifiedCandidate, signer dans un environnement privé avec le conteneur PKCS12, puis exécuter le vérificateur de signature. Ne pas utiliser les clés debug éphémères. La RC1 et les branches stables restent intactes.
