# ASTOR Android — Mnémosyne 0.2.1.1

Sources corrigées du prototype Android local : conversation, coffre chiffré,
dictée Android, modules Eve, Atlas, Hades et mémoire Mnémosyne.

## Télécharger l'APK de test

Ouvrir **Actions → ASTOR Test APK → dernière exécution réussie**.
Dans **Artifacts**, télécharger **ASTOR-Test-0.2.1.1-APK**, extraire le ZIP,
puis ouvrir **Astor-Test-0.2.1.1.apk** sur Android.

Cette application s'appelle **Astor Test** et utilise un identifiant séparé
(`fr.astor.hades.prototype.test`). Elle ne remplace pas l'Astor déjà installé
et ne récupère pas ses notes. Elle est signée avec une clé de test générée
par le runner ; cette clé peut changer entre compilations. Ne pas conserver
de données irremplaçables dans cette édition de test.

Le workflow se lance automatiquement à chaque modification de `main`.
Les tests de conversation, coffre, Mnémosyne, interface et Java doivent passer
avant la compilation et le contrôle de signature. Le test matériel Android
reste à effectuer.

## Mise à jour de l'application existante

Le workflow manuel **ASTOR signed Android update** conserve l'identifiant
original et vérifie le certificat historique. Il nécessite les secrets GitHub
`ASTOR_KEYSTORE_B64`, `ASTOR_STORE_PASSWORD`, `ASTOR_KEY_ALIAS`,
`ASTOR_KEY_PASSWORD`. Aucune clé ni aucun mot de passe n'est fourni dans ce dépôt.
Ne pas désinstaller l'Astor existant pour contourner un conflit de signature.

## Validation locale du dépôt

Les trois suites Node de conversation, coffre et Mnémosyne ont été exécutées
avec succès avant l'import. Les suites UI, Java, compilation et lint sont
exécutées par GitHub Actions. Consulter le résultat de l'exécution pour savoir
si le nouvel APK est disponible ; les documents historiques ne prouvent pas
la réussite de cette compilation.

Les anciens APK, journaux et sauvegardes historiques restent dans les archives
du projet et ne sont pas importés dans ce dépôt.
