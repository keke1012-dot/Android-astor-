# ASTOR 0.2.9.6 — Voix ElevenLabs choisie

Base : 0.2.9.5, commit 3631a333b020816a4e6223900c3d94091ab3dc3c. Identité Android et certificat inchangés ; versionCode 34. Anciennes APK conservées.

## Activation sur le téléphone

Dans Accueil, ouvrir « ASTOR · Voix grave et métallique », puis « Configurer ElevenLabs ». Saisir une clé API de son propre compte dans le panneau Android, avec permission de synthèse vocale. La voix `wDsJlOXPqcvIUKdLXjDs` doit être accessible à ce compte ; l'ajouter à sa bibliothèque si le service le demande. Aucun identifiant secret n'est embarqué dans l'APK.

Toucher Tester ElevenLabs, vérifier le texte exact dans la fenêtre de confirmation et choisir Envoyer et lire. Chaque demande utilise le service en ligne et peut consommer des crédits du compte. Lire avec ElevenLabs transmet seulement le début de la dernière réponse, plafonné à 800 caractères. Aucune lecture ou requête automatique. La voix choisie est utilisée sans modulation métallique supplémentaire afin de conserver son timbre. Le modèle `eleven_multilingual_v2` prend en charge le français ; la langue du texte guide la lecture.

La voix Android locale de la 0.2.9.5 reste disponible. Arrêter et Muet s'appliquent aux deux modes. Retirer la clé supprime sa configuration sur ce téléphone, sans révoquer la clé chez ElevenLabs.

## Données et limites

La clé est saisie dans un EditText natif masqué, jamais dans le HTML ni une URI. Elle est chiffrée en AES-256-GCM avec un secret Android Keystore non exportable, lié à une authentification biométrique forte récente (15 minutes). Les opérations cryptographiques sont exécutées sur un thread dédié. L'enveloppe a une version, un nonce aléatoire et des données authentifiées liées au paquet de l'application. Un enregistrement corrompu n'est pas remplacé silencieusement. La clé API est nécessairement déchiffrée temporairement en mémoire pour l'en-tête de la requête HTTPS ; elle n'est pas enregistrée dans le journal.

Endpoint fixe `api.elevenlabs.io/v1/text-to-speech/wDsJlOXPqcvIUKdLXjDs`, POST, modèle multilingue, MP3 44,1 kHz/128 kbps. Pas de redirections, cookies ajoutés, retransmissions automatiques, historique de conversation, accès au coffre ou upload microphone. La rétention du texte reçu par ElevenLabs relève de ses conditions : cette intégration n'active pas un mode de rétention nulle.

Audio limité à 10 Mo et 90 secondes ; délai global de 120 secondes. Audio reçu sauvegardé temporairement dans le cache privé puis supprimé à la fin, à l'arrêt et au verrouillage ; nettoyage des résidus au redémarrage. Passer en arrière-plan arrête le téléchargement et la lecture. Un envoi déjà accepté par le fournisseur peut rester facturé malgré une annulation locale.

## Validation

Tests de politique de requête, injection d'en-tête, bornes et format MP3 ; tests de l'enveloppe AES-GCM (aller-retour, nonces distincts, mauvaise clé, données modifiées, application différente et refus du texte en clair) ; tests UI (aucune clé dans le Hub/URI, envoi explicite, limites, muet, verrou et aucun réseau automatique), avec les tests de non-régression et la compilation/lint Android. L'accès réel au compte et l'écoute ne peuvent être validés avant saisie de la clé sur le téléphone. Aucun appel ElevenLabs authentifié n'a été exécuté pendant le développement.

Documentation : https://elevenlabs.io/docs/api-reference/text-to-speech/convert ; https://elevenlabs.io/docs/api-reference/authentication ; https://developer.android.com/privacy-and-security/keystore.
