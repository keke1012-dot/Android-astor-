# Validation de tous les modules ASTOR

Branche isolée depuis main. Ne pas modifier RC1, main, APK installée ni sauvegardes.

## Contrôles obligatoires
- Inventorier les modules réellement présents dans l'APK et dans le code : ASTOR, HADES, MNÉMOSYNE, ATLAS, EVE, MORPHÉE, ORPHEUS, TEMPEST, voix.
- Vérifier chaque module : affichage dans le Hub, initialisation, commande en français naturel, exécution réelle, erreurs et retour d'état.
- Tester communication intermodules, mémoire durable, permissions Android niveau 2, fonctionnement hors ligne, audio et médias.
- MORPHÉE : examiner feature/astor-morphee-v1 ; ne pas prétendre intégré sans preuve.
- Examiner séparément les branches alertes HADES, langage modules, voix UI et voix locale ; intégrer par changements ciblés après comparaison.
- Tests unitaires, UI, sécurité, performances, instrumentation et non-régression sur Samsung Android 16.
- Documenter pour chaque module : opérationnel / partiel / absent / non testé, avec preuve et procédure de reproduction.
- Construire une APK candidate séparée, vérifier signature et installation avant toute proposition de promotion.

## Critère de sortie
Tous les modules attendus accessibles, tests automatisés réussis, tests terrain confirmés, aucune régression de la version stable.