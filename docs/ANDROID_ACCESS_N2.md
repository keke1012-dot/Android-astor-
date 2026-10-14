# ASTOR Android — accès N2 contrôlé

Branche : feature/astor-android-access-n2. Base : Unifiée 0.2.9.17. Candidate : 0.2.9.18.
La branche stable et les APK originales ne sont pas modifiées. Aucune permission n’est accordée par cette préparation.

## Périmètre

- Fichiers : sélection via Android et accès durable révocable à un seul fichier N2 ; aperçu du texte limité à 8 000 caractères. Le lecteur existant limite les textes UTF-8 à 64 Ko. Les sélections du Hub gardent leurs réglages séparés.
- Applications : applications ouvrables du lanceur ; sélection et confirmation avant lancement. Pas de pilotage de leur interface.
- Agenda : lecture des 7 prochains jours (50 lignes maximum) après permission READ_CALENDAR ; préparation d’un événement via ACTION_INSERT, confirmation et sauvegarde finale dans Agenda. Aucune écriture directe.
- Notifications : permission spéciale Android. Aucune extraction dans les callbacks de réception ; lecture des notifications actives après un bouton explicite, 30 lignes maximum. Par défaut, uniquement les noms de packages ; texte après confirmation. Notifications secrètes exclues ; codes évidents masqués, sans garantie de détection de tous les secrets. Aucun historique.

## Contrôles

Les quatre options N2 commencent désactivées. Biométrie forte obligatoire, capture bloquée, verrouillage après une minute d’inactivité, retrait des données visibles au passage en arrière-plan et nouvelle authentification au retour. Les résultats asynchrones périmés ou révoqués sont ignorés. Les flux externes (sélecteur, Agenda, réglages Android) restent ouverts pour recevoir le résultat, avec l’écran ASTOR vidé.

Les données de ce panneau ne sont pas transmises au moteur IA, à Internet ni aux souvenirs. Il ne collecte pas de contacts ou SMS et n’utilise pas de service d’accessibilité. La désactivation N2 ne retire pas les permissions système Agenda/notifications : les boutons ouvrent les réglages pour les retirer.

## Validation

Tests Java : autorisation, révocation, callback périmé, masquage et limites d’événement.
Contrats statiques : manifeste, permissions minimales, listener protégé, absence d’écriture directe d’événements et de transmission à l’IA/réseau.
Compilation et lint Android, non-régression du Hub, Morphée, Hades, langue et voix.

Essais Samsung à effectuer : refus/accord/retrait des permissions, annulation de sélecteur, retour avec biométrie, lancement d’application, lecture Agenda, annulation et sauvegarde d’un événement dans Agenda, activation/désactivation des notifications, écran verrouillé et persistance des réglages. Aucun essai matériel n’est prétendu exécuté par cette préparation.
