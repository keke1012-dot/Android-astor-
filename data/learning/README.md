# ASTOR — Première préparation mémoire et apprentissage

Préparé le 12 octobre 2026. Sources : échanges visibles et contexte fourni, pas un export complet des conversations. Aucun modèle entraîné ; aucun gain de qualité mesuré. Pas de service d’entraînement externe contacté.

## Contenu

- `memory_candidates.json` : 14 souvenirs proposés, avec provenance et statut. À relire ; ce schéma de préparation n’est pas accepté automatiquement par l’application actuelle.
- `memory_commands.txt` : commandes utilisables dans la mémoire personnelle existante. Déverrouiller le coffre, envoyer une commande, vérifier la proposition, puis confirmer. Ne pas coller tout le fichier d’un coup. Les souvenirs de l’application précédente ne sont pas transférés automatiquement à la candidate séparée.
- `train.jsonl` : 24 exemples synthétiques corrigés pour français, compréhension, incertitude et limites des outils. Format messages ; retirer les métadonnées pour un moteur qui exige uniquement le champ messages. Aucune donnée personnelle historique injectée dans ce jeu.
- `evaluation.json` : 8 cas réservés à l’évaluation. Ne pas les inclure dans l’entraînement. La séparation des questions exactes ne garantit pas une indépendance sémantique complète avec un corpus aussi petit.
- `manifest.json` : nombres, statut et empreintes des fichiers initiaux.

## Suite de l’entraînement

1. Relire les exemples et fournir l’export complet si une extraction de conversations réelles est souhaitée. Exclure secrets et conversations non pertinentes ; conserver une provenance.
2. Étendre le jeu avec des exemples variés. Cette première sélection est un point de départ, insuffisant pour décider d’un entraînement utile.
3. Choisir une machine adaptée, le modèle source non quantifié et un budget. Ne pas entraîner directement le fichier GGUF utilisé par l’application. Une adaptation LoRA/QLoRA dépend du moteur d’entraînement choisi ; les dépendances et la compatibilité restent à vérifier.
4. Mesurer le modèle actuel sur un jeu plus large, puis lancer l’adaptation. Utiliser uniquement le fichier d’entraînement, garder l’évaluation à part.
5. Comparer justesse, compréhension et honnêteté sur les mêmes cas, sans fournir les réponses attendues au modèle. Faire aussi des tests nouveaux pour éviter de confondre mémorisation et amélioration.
6. Si les résultats sont meilleurs et les contrôles passent, convertir et quantifier le modèle dans un format compatible avec le moteur natif, vérifier l’empreinte, puis tester RAM, chauffe et latence sur le téléphone.

La mémoire donne du contexte ; l’entraînement change les poids. Un entraînement stylistique ne garantit pas des connaissances exactes. Aucun objectif de justesse scientifique n’est validé par ces exemples.

## Validation réalisée

`python3 scripts/validate_learning_data.py` : vérification de structure, doublons, séparation des questions, provenance, empreintes et motifs courants de secrets. Ce contrôle ne remplace pas une relecture humaine et ne garantit pas l’absence de toute donnée sensible dans un futur export.
