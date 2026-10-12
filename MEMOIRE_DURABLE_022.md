# ASTOR Android 0.2.2 — Mnémosyne durable

Mnémosyne conserve maintenant des souvenirs personnels après confirmation explicite. Ils restent disponibles après réouverture et déverrouillage du coffre, avec correction et suppression confirmées. Les 15 fiches publiques et Tempest restent disponibles.

## Utilisation

1. Ouvrir Notes et déverrouiller ou créer le coffre avec une phrase secrète d’au moins 12 caractères.
2. Dans Accueil, dire « Retiens que mon projet s’appelle ASTOR ».
3. Relire l’aperçu, puis dire « Confirme le souvenir » ou utiliser Confirmer dans Notes. Annule ou le bouton Annuler ne conserve rien.
4. Après redémarrage, déverrouiller le coffre puis demander « Comment s’appelle mon projet ? ».

Autres commandes : Liste mes souvenirs ; Rappelle le souvenir 1 ; Cherche dans mes souvenirs : Dune ; Corrige le souvenir 1 : mon projet s’appelle ASTOR V2 ; Supprime le souvenir 1.

## Stockage et confidentialité

- Le format du coffre de notes existant reste inchangé, sans réécriture lors de son ouverture.
- La collection de souvenirs est chiffrée dans un stockage séparé avec la clé du coffre : AES-GCM, PBKDF2-SHA256, 310000 itérations.
- Aucun souvenir n’est écrit avant confirmation. Les brouillons non confirmés restent en mémoire vive.
- Le verrouillage, la sortie de l’application et la suppression du coffre effacent les souvenirs déchiffrés et les confirmations en attente. Supprimer le coffre supprime aussi la collection chiffrée des souvenirs.
- Les échanges de gestion des souvenirs ne sont pas ajoutés à l’historique destiné à une éventuelle IA distante.
- Les conflits de stockage, doubles clics, erreurs et interruptions sont contrôlés avant l’écriture.
- Limites : 200 souvenirs, 1000 caractères chacun ; reconnaissance locale de commandes définies, recherche par mots et rappel spécifique du nom du projet. Ce moteur ne comprend pas encore librement toutes les formulations.
- Aucun transfert automatique du coffre entre les applications de test séparées. La phrase secrète perdue ne peut pas être récupérée.

## Validation

Tests : test_web.cjs, test_vault_security.cjs, test_mnemosyne.cjs, test_durable_memory.cjs, tests_ui/test-ui.cjs. Ils couvrent notamment confirmation et annulation, persistance dans un nouvel environnement, correction, suppression, chiffrement, anciens coffres, interruptions, conflits et absence de réseau.

La compilation Android et le contrôle de signature doivent également réussir. La validation réelle sur téléphone reste à réaliser par l’utilisateur après installation.

## Application de test

La variante Astor Mémoire utilise l’identifiant fr.astor.hades.prototype.test.mnemosyne pour préserver les installations Astor Test et Astor Tempest déjà signées avec d’autres clés. L’APK livré est signé localement avec une clé de test privée durable, conservée séparément des sources. Les mises à jour futures doivent réutiliser cette clé et cet identifiant, puis vérifier le certificat avant installation.
