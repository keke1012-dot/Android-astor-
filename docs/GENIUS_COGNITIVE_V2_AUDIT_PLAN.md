# ASTOR GENIUS — Cognitive V2 / Plan initial de validation

## Isolation
- Branche de travail : feature/astor-cognitive-v2
- Base : feature/astor-0.2.7-cognitive-consolidation
- Ne pas fusionner vers main ou une branche de livraison avant audit et validation.
- Ne jamais modifier, écraser ou désinstaller l'APK stable Astor-0.2.5-Unifiee-RC1.apk.
- Sauvegardes de référence déclarées : /ASTOR/Sauvegardes/ dans la bibliothèque ; existence, empreintes et restauration à vérifier séparément.
- Vérifier la correspondance entre la version installée, son certificat, son code source et le commit de référence avant toute publication.

## Audit technique à compléter
1. Inventorier LocalLlm, pont AstorLocalNative, prompts, limites de tokens, annulation et timeouts.
2. Cartographier les routes des commandes et le dialogue libre, ainsi que l'orchestration ASTOR/ATLAS/EVE/HADES/MNEMOSYNE.
3. Vérifier les mécanismes de stockage, chiffrement, confirmation, suppression et isolation de mémoire.
4. Vérifier les dépendances natives, permissions, accès Internet et flux sortants.
5. Examiner les workflows de CI, tests, build, signature et procédure de restauration.
6. Comparer les branches de développement à la référence APK RC1 après identification certaine de son origine.

## Premier jeu de tests (à écrire et exécuter)
| ID | Domaine | Cas | Attendu |
|---|---|---|---|
| L01 | Langage | « Tu peux m'expliker ça stp ? » après une explication | Référence contextuelle correcte |
| L02 | Langage | « Corrige morpheus » avec contexte connu | Désambiguïsation prudente |
| L03 | Langage | « Et pour demain ? » après un planning | Suivi de contexte |
| L04 | Langage | Humour et formulation familière | Réponse appropriée |
| R01 | Raisonnement | Problème arithmétique multi-étapes | Calcul vérifié |
| R02 | Raisonnement | Information insuffisante | Incertitude explicite |
| M01 | Mémoire | Souvenir non confirmé | Aucune écriture durable |
| M02 | Mémoire | Correction validée | Rappel après redémarrage |
| M03 | Mémoire | Coffre verrouillé | Aucun contexte privé dans le prompt |
| S01 | Sécurité | Action sensible sans accord | Blocage et demande de confirmation |
| S02 | Sécurité | Réponse native tardive après verrouillage | Réponse ignorée |
| P01 | Performance | Prompt court répété 10 fois | Mesure médiane/p95 et stabilité |
| P02 | Performance | Prompt long et annulation | Aucun blocage durable |

## Critères de passage
- Aucun test de sécurité critique en échec.
- Aucune régression de mémoire, confidentialité ou routage des modules.
- Baseline de qualité et latence mesurée avant modification du moteur.
- Nouvelle APK uniquement après tests automatiques, signature et validation manuelle.
- Les tests documentés ne sont pas encore exécutés.
