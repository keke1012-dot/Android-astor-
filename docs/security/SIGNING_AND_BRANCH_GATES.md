# ASTOR — Gate sécurité avant fusion

Branche de référence : develop/astor-0.2.5-global. Aucun déploiement automatique.

## Signature APK
- Vérifier le certificat SHA-256 de l'APK actuellement installé avec apksigner et le comparer à la valeur attendue du workflow.
- Vérifier toutes les autres procédures de signature ; ne jamais remplacer le certificat historique sans plan de migration explicite.
- Secrets de signature exclusivement dans GitHub Actions Secrets, jamais dans les fichiers suivis, logs ou artefacts.
- Échec bloquant si signature absente, certificat différent ou secrets manquants.

## Branches GitHub
Configurer dans Settings > Rules > Rulesets (ou Branch protection rules) pour main et develop/astor-0.2.5-global :
- pull request obligatoire avant fusion ; au moins une approbation si possible ; conversations résolues ;
- checks CI obligatoires après confirmation de leurs noms exacts ;
- interdiction des force-push et suppression ;
- application aux administrateurs si disponible ;
- pas de contournement non documenté.

## Validation
- Exécuter tests sécurité, Mnémosyne, restauration, TEMPEST V1 et build de test non déployé.
- Vérifier l'activation effective des règles via GitHub Settings.
- Conserver les sauvegardes et la branche stable intactes.
- Validation explicite de l'utilisateur avant toute fusion, rotation de clé, signature de distribution ou déploiement.
