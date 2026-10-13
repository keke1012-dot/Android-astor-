# ASTOR Unifiée 0.2.9.2 — performances et mises à jour

## Performances
Le moteur libère son état occupé avant d'appeler la fin de génération. Cela permet aux trois passes cognitives de s'enchaîner et aux nouvelles requêtes de démarrer après une erreur. Une régression Java avec moteur simulé couvre cet ordre d'exécution. S2 batch 64, quatre threads et plafond de 128 tokens restent inchangés faute de nouvelles mesures terrain.

La durée complète de chaque réponse locale réussie et la moyenne des dix dernières réponses sont affichées. L'horloge est monotone ; les erreurs et annulations ne sont pas des mesures réussies. Les durées restent uniquement en session et sont effacées au verrouillage. Aucun texte, trace de raisonnement, modèle ou durée n'est envoyé sur un serveur. Le temps inclut le contexte, les passes cognitives et la mise en file ; il ne prouve pas une accélération sur téléphone.

## Mises à jour
Identifiant permanent : fr.astor.hades.unified.test.unified029, conservant le suffixe historique. Toutes les releases utilisent cet identifiant même sans option de compilation. versionCode 30. Les builds diagnostiques ont une identité séparée.

La signature durable du 13 octobre 2026 reste inchangée. Avant livraison, verify_unified_update.py vérifie les signatures de la candidate et de la 0.2.9.1, le même identifiant, un versionCode supérieur, minSdk inchangé, débogage désactivé, sauvegarde et HTTP interdits, ARM64 et alignement des bibliothèques à 16 KiB.

Une installation Android par-dessus la 0.2.9.1 doit conserver les données et les modèles. Aucun schéma de coffre ni chemin de stockage n'est modifié. Les tests de sauvegarde/restauration passent ; l'installation par-dessus et la conservation effective des données doivent encore être confirmées sur le téléphone. Ne pas désinstaller la 0.2.9.1 pour cette mise à jour. La 0.2.9 à ancienne signature requiert la migration annoncée précédemment.

La RC1, la branche stable, les sauvegardes et le projet de serveur annulé ne sont pas modifiés. Les secrets .env et PKCS12 sont exclus de Git.
