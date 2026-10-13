# ASTOR 0.2.9.7 — Accès ElevenLabs visible

Correction d'interface depuis la 0.2.9.6 (commit 670a356719bd12f218d7c3f2c66162cc5c1ae666). Identité Android et certificat conservés ; versionCode 35.

Le bouton ElevenLabs apparaît dans les commandes rapides d'Accueil. Il ouvre le panneau vocal et place Configurer ElevenLabs au début, avant les réglages de la voix Android locale. Le panneau n'est plus limité à une petite zone défilante interne. La voix locale dispose d'un volet séparé. Muet et Arrêter restent accessibles pour la voix en ligne. L'en-tête affiche le numéro 0.2.9.7 au lieu du libellé historique 0.2.8.

Le moteur vocal ElevenLabs, l'identifiant choisi, le stockage chiffré de la clé, les confirmations d'envoi, la mémoire et les permissions sont inchangés. Aucun appel API réel n'est effectué pendant la construction ; la voix sera testée après saisie de la clé sur le téléphone.

Validation : tests d'ouverture par le bouton ElevenLabs, présence du bouton Configurer au début du panneau, suppression du défilement interne, volet local séparé, arrêt de lecture, et suite de non-régression ; compilation/lint Android et signature compatible avant livraison.
