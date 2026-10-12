# ASTOR 0.2.3.2 — Calcul local plus léger et diagnostic précis

Sur le téléphone, une question courte a produit « Calcul interrompu. Réessayez avec une question plus courte. ». Ce message ne distinguait pas le délai, la mémoire, le cache ou une erreur du moteur. La cause exacte sur cet appareil n’est donc pas encore confirmée.

## Changements

- Compilation native optimisée en Release, même dans l’application Android de test.
- Contexte réduit de 2048 à 1024 tokens, blocs de préparation de 128 à 32 tokens et microblocs de 64 à 32.
- Instruction système raccourcie et historique du modèle limité aux deux derniers messages, 200 caractères chacun. Les messages d’erreur du moteur ne sont pas réutilisés comme contexte.
- Réponse limitée à 128 tokens ; limite de calcul après création du contexte portée de 45 à 90 secondes. Le délai de l’interface est de 120 secondes.
- Progression affichée pendant la préparation, puis indication de rédaction. L’écran reste allumé pendant le calcul au premier plan et revient à son comportement normal après réponse ou annulation.
- Erreur détaillée : délai, mémoire, cache, code du moteur et nombre de tokens préparés ; aucun contenu de la question n’est journalisé par ce diagnostic.

Le modèle conserve le même fichier, la même taille, le même hash et le même emplacement. Le coffre et les souvenirs gardent leur stockage et leurs protections. Le verrouillage et le passage en arrière-plan continuent d’annuler les calculs.

## Test renforcé

Le test précédent appelait llama-simple. Cette version construit notre propre bibliothèque et appelle exactement LocalLlm.java puis le pont JNI utilisé par Android, avec le vrai fichier Qwen3 et plusieurs blocs de préparation. Ce test doit générer une réponse non vide avec les callbacks de progression. Il ne démontre pas encore les performances du processeur du téléphone.

Installer comme mise à jour, puis demander « Explique-moi simplement ce qu’est un trou noir. ». Si une erreur persiste, son code et son délai permettront de choisir la correction suivante.
