# ASTOR 0.2.8 — accès aux fichiers V1

Candidat séparé fondé sur 0.2.7.1 Internet S2, sans modification de main, de la branche Internet ni de la RC1.

Dans Accueil → Vos fichiers → Choisir un fichier, Android demande le choix explicite d’un fichier. Au retour, Astor demande à nouveau la biométrie. Lecture seulement : aucune écriture ou suppression du fichier original. Le dernier URI choisi est conservé dans les préférences privées ; son autorisation de lecture persiste jusqu’à « Retirer l’accès ». Aucun accès global au stockage, aucune permission MANAGE_EXTERNAL_STORAGE.

Formats : texte UTF-8 TXT/MD/CSV/TSV/JSON/LOG/XML/HTML, au plus 64 Ko. Aucun balisage n’est exécuté. Aperçu limité à 4 000 caractères. Les PDF, images, APK, archives et autres binaires sont affichés par leurs métadonnées, sans extraction du contenu. Un fichier distant proposé par le sélecteur peut nécessiter un téléchargement par son fournisseur.

Pour demander une analyse, activer « Joindre un court extrait à mes questions à l’IA locale », puis poser une question. Extrait limité à 900 caractères ou moins selon le budget total de 2 200 caractères. Le modèle peut n’avoir accès qu’au début du document : une analyse complète n’est pas garantie. L’extrait est traité comme une donnée, les marqueurs de contrôle Qwen sont filtrés. Aucun contenu n’est ajouté à la recherche Internet ni automatiquement aux souvenirs.

Au verrouillage, la WebView est détruite et les callbacks en retard sont rejetés. L’autorisation du fichier reste active : au prochain déverrouillage, Astor le relit localement, mais le consentement d’analyse reste désactivé. « Retirer l’accès » libère l’autorisation Android et enlève l’URI des préférences ; cela ne supprime ni le fichier original ni les résumés déjà présents dans la conversation temporaire. Une interruption/process death pendant le sélecteur peut imposer une nouvelle sélection.

Distribution de test distincte : fr.astor.hades.unified.test.files028, versionCode 27. Elle n’importe pas automatiquement le modèle ou les données des autres APK. Ne pas désinstaller l’application d’origine pour tester. La signature debug ne prouve aucune continuité avec la clé historique.

Validation : tests JavaScript de non-régression, tests DOM fichiers (aperçu, consentement, prompt borné, format binaire, verrouillage, retrait), tests Java de la lecture bornée et du décodage UTF-8, compilation/lint/signature dans CI. Validation physique Android restant à effectuer : sélection, annulation, biométrie au retour, reprise après verrouillage, retrait/re-sélection, fichier supprimé/déplacé, fournisseur cloud, analyse locale, température et latence avec fichier.
