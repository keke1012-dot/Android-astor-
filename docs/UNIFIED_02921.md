# ASTOR 0.2.9.21 Unifiée — Personnalité & Initiative V1

Intégration de la V1 testée dans l’application unifiée, à la demande de l’utilisateur qui déclare les tests concluants sur l’expérience séparée. Cette déclaration ne remplace pas les contrôles de mise à jour.

Base applicative : 991d7902648df3f0be6c529eed73a12a572102f2 ; base cumulée originale : f66fb0740b32661675e98bda6cb2e6413d90633c (Big Bang).
Branche : feature/astor-personnalite-unifiee-v1. VersionCode 49, versionName 0.2.9.21-personnalite-v1.

L’APK release conserve fr.astor.hades.unified.test.unified029 et doit être signée avec le certificat durable référencé dans security/UNIFIED_CERTIFICATE_SHA256.txt. La signature privée reste hors Git et hors CI publique. Contrôle obligatoire contre la 0.2.9.20 : mêmes identifiant, certificat et minSdk, versionCode supérieur, protections Android maintenues, bibliothèques ARM64 alignées à 16 KiB.

Personnalité et initiatives : code identique à l’expérience validée ; humour et suggestions désactivables, contradiction argumentée, propositions textuelles limitées et expirables. Aucune action native nouvelle ni permission ajoutée. Les confirmations du coffre, les permissions N2, la biométrie, la voix locale, Morphée et Mnémosyne restent inchangés.

Aucun schéma de stockage ni chemin de données modifié. Installer par-dessus la 0.2.9.20 sans désinstaller l’application. Les réglages et données de l’expérience séparée ne sont pas copiés automatiquement. RC1, expérience séparée, anciennes APK et sauvegardes restent conservées.

Limites : règles locales ciblées ; dialogue libre dépendant du modèle présent. Réglages de personnalité limités à la session ; souvenirs durables via validation explicite existante. L’installation et la conservation effective des données restent à confirmer sur le téléphone.

Validation prévue : mêmes 13 suites Node et 2 suites Java, assembleRelease/lintRelease, signature privée et vérificateur de mise à jour compatible avant livraison.

## Livraison vérifiée — 14 octobre 2026
GitHub Actions 38563460754 : succès (13 suites Node, 2 suites Java, assembleRelease et lintRelease). Signature durable, comparaison à la 0.2.9.20 et alignement 16 KiB validés. Contenu HTML de l’APK signé identique à la branche testée. APK : Astor-0.2.9.21-Unifiee-Personnalite.apk. SHA256 : 424b6061c0c2238a9be0d932053f62f915950b20222e385ee6b20709ea125ec9.
