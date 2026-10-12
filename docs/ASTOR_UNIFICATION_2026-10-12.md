# ASTOR — Application unifiée, candidate 0.2.5

## Une application

Le Hub contient déjà ASTOR, EVE, ATLAS, HADES, TEMPEST, MNÉMOSYNE et ORPHEUS. L’identité de la version release reste `fr.astor.hades.unified`, libellé « Astor Unifiée ». VersionCode 24, versionName 0.2.5-unifiee-rc1. L’Oreillette conserve son prototype séparé ; sa traduction n’est pas intégrée par cette livraison.

## Mises à jour

La première tentative de CI signée a été arrêtée : paramètres privés historiques manquants. Une archive privée de signature mémoire existante a ensuite été retrouvée ; sa clé est lisible, certificat SHA-256 9716ec2375cbd8a340b9e8d4b6a0517a62aba0b9185420115ad4f0d96e3a2005. Ce certificat est différent de la clé attendue par l’ancienne configuration.

Le workflow `unified-build.yml` fabrique un APK release non signé et aligné, qui ne doit jamais être proposé à l’installation. La signature et sa vérification sont réalisées en privé après téléchargement avec la clé mémoire sauvegardée. Aucune clé ni phrase secrète ajoutée aux sources ou au paquet public. L’identité `fr.astor.hades.unified` et cette clé doivent rester fixes pour les futures mises à jour. Les applications précédentes restent installées séparément.

L’automatisation de la signature dans GitHub n’est pas configurée. Le workflow historique manuel garde son propre contrôle et ses exigences ; il n’est pas utilisé pour la livraison unifiée.

## Migration des données

Dans Notes, une fonction prépare un texte de sauvegarde chiffré contenant les notes et souvenirs durables enregistrés. Copier entièrement ce texte et le coller dans la nouvelle application ; fournir la phrase secrète d’origine. Les brouillons non enregistrés et les conversations temporaires ne sont pas exportés. L’export ne crée pas un fichier et n’envoie rien sur Internet. Conserver le texte et la phrase séparément.

La restauration vérifie le format et déchiffre les contenus avant confirmation. Elle est réservée à une application vide pour éviter tout écrasement. Elle restaure notes et souvenirs et garde leur chiffrement et phrase d’origine. Un journal chiffré permet de terminer une écriture interrompue ; tout conflit avec des données différentes bloque l’opération.

Les anciennes applications sans cette fonction ne peuvent pas exporter par cette interface. La nouvelle application n’a pas accès à leur stockage. Elles doivent être conservées ; une migration depuis elles n’est pas encore réalisée. Ne pas désinstaller ni effacer leurs données pour préparer la migration. La fusion de plusieurs coffres existants n’est pas implémentée.

## Validation

Tests du transfert : notes et souvenirs restaurés, mauvais mot de passe, données altérées, annulation utilisateur, verrouillage pendant la dérivation, données existantes protégées, reprise après seconde écriture échouée, conflits, effacement du journal, aucun réseau. Tests du dialogue et de la mémoire conservés. Compilation et clé historique à confirmer par CI ; fonctionnement de la version unifiée sur appareil encore à confirmer.

La RC2 a été déclarée fonctionnelle sur le téléphone par l’utilisateur le 12 octobre 2026. Chauffe, RAM et latence sur appareil non mesurées. Pas de modèle réentraîné.
