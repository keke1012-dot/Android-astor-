# ASTOR 0.2.7 Internet S2 — candidat de test

Base source : feature/astor-0.2.6-optimisation, commit 5551dce913335e087f94be9759eeac79a1eda5cd.
Le batch 64 est activé comme sur le diagnostic S2. Cette version n'est pas une modification de la RC1.

Fonctions : recherche Wikipédia intégrée (trois extraits avec leurs sources), recherche Web Google dans le navigateur externe, activation limitée à la session, aperçu natif de la seule requête avant envoi, annulation et effacement au verrouillage. Le moteur local fonctionne indépendamment d'Internet.

Limites : aucun accès autonome à tous les sites, aucune synthèse automatique des pages par Qwen, aucun apprentissage ou ajout de souvenirs à partir du Web. Les résultats externes peuvent être inexacts ou anciens. Les liens Wikipédia apparaissent en texte. Le bouton Web ouvre le navigateur et verrouille ASTOR.

Protection : endpoint Wikipédia HTTPS fixe, pas de redirection suivie, pas de scripts distants dans la WebView, requête de 300 caractères maximum, délais réseau et limite de taille, rejet des callbacks tardifs. Aucun coffre, historique ou souvenir n'est joint aux recherches. Le fournisseur reçoit la requête et les métadonnées réseau usuelles.

Livraison CI : APK de test séparé fr.astor.hades.unified.test.internet027, versionCode 25. Cette installation ne remplace pas l'application actuelle et ne reprend pas automatiquement son modèle ou son coffre. La signature de test ne démontre pas une continuité avec l'APK existant. Une véritable mise à jour sur place devra être signée avec sa clé historique vérifiée.

Validation locale : tests JavaScript conversation, coffre, Mnémosyne, mémoire durable, transfert, IA locale et Internet réussis ; gardes statiques réussis. Java et Android à compiler sur GitHub (JDK absent dans l'environnement local). Validation du réseau réel, de la biométrie et de l'affichage sur Samsung encore à faire.

Test téléphone : activer Internet ; rechercher « télescope James Webb » ; confirmer ; vérifier trois extraits et sources. Annuler une seconde recherche. Désactiver Internet. Vérifier les commandes locales. Rechercher sur le Web, revenir et vérifier que la biométrie est demandée. Confirmer que les notes existantes restent accessibles dans l'application d'origine.
