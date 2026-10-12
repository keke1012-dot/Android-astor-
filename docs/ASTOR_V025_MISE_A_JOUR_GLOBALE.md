# ASTOR 0.2.5 — Mise à jour globale (travail en cours)

Base: 0.2.4. Branche isolée de main. Aucun APK installé ni mise en production.

## Périmètre unique
1. ASTOR/EVE : qualité du français, compréhension des reformulations, réponses naturelles, incertitude explicite.
2. ATLAS : jeux de tests factuels et contradictoires, citations des sources locales, refus de conclure sans preuve.
3. MNÉMOSYNE : correction du routage des questions sur le décideur final et la devise de Tempest, sans perte des souvenirs.
4. HADES : vérification du verrouillage biométrique, coffre chiffré, sauvegarde exportable et restauration testée, non-régression.
5. ANDROID : mesurer RAM, chauffe, latence et interruptions sur Galaxy A54; conserver Qwen3 0.6B et 1.7B avec retour au léger.
6. TEMPEST : scénarios adversariaux, sources contradictoires, régression et possibilité de retour arrière.

## Constats préliminaires du code
- astor_local.cpp: n_ctx=1024, limite de génération=128 tokens, n_threads=4, délai d'annulation=90s. Ne pas augmenter ces valeurs sans mesures de mémoire et latence.
- AndroidManifest.xml: allowBackup=false, usesCleartextTraffic=false, USE_BIOMETRIC et RECORD_AUDIO déclarés. Une permission ne prouve pas l'application effective de la politique de sécurité.
- GitHub Actions build.yml: tests JS/Java, compilation, lint, signature historique. Vérifier aussi les tests natifs et les essais réels sur appareil.
- Aucune preuve actuelle d'une amélioration générale de la justesse du modèle 1.7B.

## Conditions bloquantes avant APK final
- Copie sûre et restauration réelle des données vérifiées, sans exposer de secrets.
- 50 questions de référence (science, contexte ASTOR, français imparfait, inconnues, contradictions), comparaison reproductible avec 0.2.4.
- Vérifier commandes, dictée, biométrie, coffre, Mnémosyne, réouverture et migration.
- Mesurer latence, mémoire et température sur appareil; documenter les limites.
- Construire et signer avec la clé historique; vérifier certificat et tests CI.
- Ne fusionner dans main et ne proposer l'installation qu'après validation humaine.

Statut : cadrage commité; implémentation, compilation et validation encore à faire.
