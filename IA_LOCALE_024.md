# ASTOR 0.2.4 — candidat Qwen3 1.7B et modèle léger conservé

Qwen3 1.7B, conversion GGUF Q4_K_M publiée par bartowski, est ajouté comme modèle conversationnel optionnel. Le téléchargement fixé à une révision et vérifié par SHA-256 pèse 1 282 439 584 octets. Prévoir environ 1,4 Go libres en plus de l’ancien modèle. Sources : https://huggingface.co/Qwen/Qwen3-1.7B et https://huggingface.co/bartowski/Qwen_Qwen3-1.7B-GGUF/tree/dcb19155b962dbb6389f4691a982043a8e651022.

La mise à jour ne télécharge rien automatiquement. Dans Accueil, « Télécharger le modèle amélioré » ouvre une confirmation Android. Tant que l’installation n’est pas complète et vérifiée, le modèle 0.6B déjà installé reste utilisable. Après installation, le 1.7B est choisi par défaut. La case « Utiliser le modèle léger » permet de revenir au 0.6B dans la session. Après un redémarrage, le modèle amélioré redevient le choix par défaut. Aucun fichier modèle n’est supprimé.

Les poids sont rechargés quand le chemin du modèle choisi change. Le choix est refusé pendant un calcul ou un téléchargement. Les limites de contexte, la durée maximale, l’arrêt du calcul et la libération au verrouillage restent identiques. Le coffre, ses clés et ses formats sont inchangés. L’APK conserve le même package et doit être signé par la clé stable ; installer en mise à jour sans désinstallation.

## Comparaison observée

Le fichier MODEL_COMPARISON_024.json conserve dix questions synthétiques et leurs réponses avec le même moteur, les mêmes consignes et une graine de test 42. Le 1.7B mentionne l’horizon des événements et la fusion nucléaire pour distinguer étoile et planète, là où le 0.6B donne des explications erronées. Plusieurs formulations sont plus utiles. Le candidat répond toutefois mal à « Pourquoi pleut-il ? » et présente encore des maladresses de français. Ce petit échantillon ne constitue pas une mesure générale d’exactitude.

Le calcul du candidat est plus lent sur le runner. La vitesse, la RAM et la chauffe sur le Samsung A54 ne peuvent être mesurées depuis cet environnement : il reste à les observer sur l’appareil. Le modèle reste expérimental, sans connexion de vérification des faits. Les documents de référence locaux feront l’objet d’une étape distincte après validation du modèle.

## Vérification

Tests de coffre, souvenirs, routage et interface ; tests Java de conservation des deux modèles, sélection et réouverture ; vraie génération via Java/JNI avec chacun des modèles ; bascule 1.7B → 0.6B → 1.7B sans redémarrage ; compilation ARM64 et lint Android. Les données de test sont fictives et n’utilisent aucun souvenir privé.
