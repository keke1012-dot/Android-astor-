# ASTOR Personnalité & Initiative V1 — expérimentation

Base : feature/astor-big-bang-entry, f66fb0740b32661675e98bda6cb2e6413d90633c.
Branche : feature/astor-personnalite-v1. Priorité : conversation locale et fiabilité.

## Changements
- Moteur local déterministe, embarqué dans le HTML Android, sans dépendance réseau ni outil d’exécution.
- Contradiction argumentée sur deux conclusions de sécurité/fiabilité courantes, répartie légère et humour désactivable.
- Détection de fatigue déclarée et de surcharge de modules ; une proposition maximum par dix minutes. Refus respecté, proposition expirant après cinq minutes ou changement de sujet ; acceptation limitée à préparer du texte.
- Réglages de session : « Sans humour », « Active l’humour », « Pas d’initiatives », « Active les initiatives ». Les réglages ne sont pas enregistrés silencieusement.
- Consignes de personnalité intégrées au modèle local et au contrat distant existant ; question intacte et budget local de 2200 caractères préservés.
- Les commandes prioritaires, le verrouillage, les permissions natives N2 et les confirmations de Mnémosyne restent responsables de leurs opérations. Le moteur de personnalité ne délivre aucune permission ; une confirmation générique « oui » ne peut déclencher une action sensible.
- Réutilisation des souvenirs validés uniquement via l’option existante du coffre déverrouillé. Aucune nouvelle mémoire autonome, aucun agent supplémentaire.
- APK de test compilée et vérifiée : paquet fr.astor.hades.personality.v1.test, version 0.2.9.21-personnalite-v1-test. Installation parallèle ; données et modèle local séparés. Ne pas désinstaller la version existante. Export/import chiffré existant utilisable dans une application vide, sur choix explicite.

## Validation
13 suites Node réussies : personnalité (unitaire et console), guardrails, web, Hades, langage des modules, N2, coffre, Mnémosyne, mémoire durable, sauvegardes, pont IA locale, Internet et mathématiques.
Les tests utilisent un pont IA simulé : ils ne prouvent pas la qualité d’un modèle réel.
CI dédiée : tests Java Morphée et N2, suites Node, compilation/lint Android, vérification de signature et d’identité du paquet avant livraison. Aucun build stable déclenché par cette branche.

## Limites et validation téléphone
- La V1 sans modèle utilise des règles et réponses fixes ; elle ne comprend pas toutes les formulations. Ce n’est pas une IA générale ni une initiative en arrière-plan.
- L’humour et la contradiction libres dépendent du modèle installé. Aucun résultat du modèle ne reçoit le droit d’exécuter des outils.
- Morphée intervient ici sous forme de proposition textuelle. Aucun diagnostic médical, rappel, écoute ou ouverture automatique.
- Les préférences de ton sont limitées à la session. Les préférences durables restent des souvenirs explicitement confirmés et autorisés.
- Aucun test physique sur téléphone dans cette session. Vérifier installation côte à côte, modèle hors ligne, voix, refus, verrouillage et conservation des données de l’application actuelle.
- La compilation Android et les tests Java sont délégués à GitHub Actions faute de JDK/SDK Android locaux. La chaîne CI a terminé avec succès.

## Essai conseillé
1. « Astor est impossible à pirater » : contradiction avec raison.
2. « Raconte-moi une blague », puis « Sans humour » : adaptation du ton.
3. « Je suis crevé », puis « oui » : préparation textuelle, sans rappel.
4. « Supprime mes notes », puis « oui » : aucune suppression automatique.
5. Verrouiller et reprendre : proposition précédente oubliée.

RC1, autres branches, APK existantes et sauvegardes n’ont été ni modifiées ni supprimées par cette mission.

## Résultat de compilation — 14 octobre 2026
GitHub Actions 38561871332 : succès. Deux suites Java (Morphée et permissions N2), treize suites Node, assembleDebug et lintDebug réussis. Signature APK et paquet isolé vérifiés en CI ; empreintes de l’archive et de l’APK vérifiées au téléchargement. APK pour Android 11+ ARM64, signée avec une clé de test : aucune promesse de compatibilité de signature avec une future compilation. Ne pas utiliser cette expérience pour stocker l’unique copie d’une donnée.
Commit applicatif testé : 991d7902648df3f0be6c529eed73a12a572102f2.
