# ASTOR 0.2.9.5 — Voix française native et profil métallique

Base : 0.2.9.4, commit b55217693734ed7659beff11cb5e087a994ac597. APK originale préservée. Identité Android conservée et versionCode augmenté de 32 à 33.

Dans Accueil, ouvrir « ASTOR · Voix grave et métallique », toucher Détecter, choisir une voix française installée, puis Tester. Le profil Métallique est sélectionné par défaut. Lire la dernière réponse lit son début jusqu'à 800 caractères, sur demande. Arrêter et Muet sont disponibles ; Muet est conservé dans les préférences Android. Aucune lecture automatique.

Le moteur Android fournit une synthèse française déclarée hors ligne. Le résultat WAV est préparé dans le cache privé, traité localement puis lu : abaissement de hauteur, filtre, modulation métallique, saturation légère et écho court. Ce profil est original ; il ne clone ni acteur ni personnage. Le timbre dépend du moteur installé. Si aucune voix française locale n'existe, ouvrir Paramètres voix pour télécharger les données Français ; revenir ensuite au Hub et détecter à nouveau.

Le contrôleur n'utilise pas la détection vocale du WebView. Aucune permission nouvelle. Le coffre, la mémoire et les outils Internet ne reçoivent aucun nouvel accès. Lecture réservée au document local au premier plan et déclenchée par un geste utilisateur. Les fichiers audio temporaires sont supprimés à la fin, à l'arrêt et au verrouillage, avec nettoyage des résidus au prochain démarrage du contrôleur. Arrêt également en cas de perte du focus audio. Pas de téléchargement ou service vocal distant ajouté.

Limites : texte natif limité à 1 200 caractères, WAV PCM 8/16 bits ou float32, mono/stéréo, 8–96 kHz, 60 secondes de source maximum ; arrêt global après 90 secondes. Le décodage et le traitement sont réalisés sur un thread dédié. L'APK doit être signée avec le certificat historique avant livraison. Tests de non-régression et compilation/lint à exécuter en CI. L'écoute et la détection réelle sur le téléphone restent à confirmer après installation.

Documentation : https://developer.android.com/reference/android/speech/tts/TextToSpeech et https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener.
