# ASTOR 0.2.3 — Premier moteur de langage local

Le prototype utilise Qwen3 0.6B Q8_0 et llama.cpp, sans clé API ni abonnement par question. ASTOR fournit l’interface, les rôles, les commandes, les protections et la mémoire personnelle. Le modèle initial vient de Qwen ; il n’a pas été entraîné depuis zéro par ce projet.

## Installation et utilisation

- Mettre à jour Astor Mémoire avec l’APK signé par la même clé privée que 0.2.2. Ne pas désinstaller l’application.
- Dans Accueil, choisir Télécharger le modèle et confirmer le téléchargement d’environ 640 Mo. Garder l’application ouverte. L’inactivité ou le passage en arrière-plan interrompent le transfert ; un nouvel essai recommence le téléchargement.
- Après vérification, utiliser l’option de discussion locale. Commencer par une question courte en français. Le bouton Arrêter le calcul annule une génération.
- Après téléchargement, les réponses sont calculées sur le téléphone, même sans connexion. L’accès réseau natif sert uniquement à télécharger le fichier de modèle fixé dans le code. Les requêtes WebView distantes restent bloquées.

## Mémoire et rôles

- Les commandes, les urgences, les 15 fiches publiques et les opérations de souvenirs gardent leur routage déterministe.
- Les conversations libres peuvent utiliser le modèle avec le rôle du module sélectionné.
- Les souvenirs chiffrés ne sont inclus qu’après déverrouillage et activation explicite de l’option dans Notes ; jusqu’aux trois derniers souvenirs, sur 500 caractères maximum.
- Les réponses utilisant ce contexte privé ne sont pas réinjectées dans l’historique de conversation destiné au modèle. Le verrouillage efface le contexte et annule les calculs.
- Le modèle ne reçoit aucun outil de commande système ou de modification du coffre. Une réponse générée ne peut pas créer un souvenir ou exécuter une action.

## Limites et validation

- ARM64 uniquement, CPU, 4 threads, contexte de 2048 tokens, réponses limitées à 160 tokens et calcul limité après chargement à environ 45 secondes.
- Modèle de petite taille : erreurs, hallucinations, qualité variable en français et réponses parfois lentes. Aucune vérification de faits sur Internet.
- Compatibilité réelle, vitesse, mémoire disponible et chauffe sur le téléphone restent à valider par l’utilisateur.
- Les tests JavaScript vérifient le routage et la confidentialité avec un pont simulé. Un test distinct en CI télécharge le vrai modèle, vérifie son empreinte et produit une réponse sur CPU. Les tests de coffre, de mémoire durable, d’interface et de dictée restent requis, ainsi que la compilation et le lint Android.

## Versions et licences

- llama.cpp b6000, commit `4762ad7316dcdec20016ab5985fb46a27902204d`, licence MIT.
- Modèle officiel : https://huggingface.co/Qwen/Qwen3-0.6B-GGUF , licence Apache-2.0 ; revision `23749fefcc72300e3a2ad315e1317431b06b590a`.
- Fichier Qwen3-0.6B-Q8_0.gguf : 639446688 octets ; SHA-256 `9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031`.
- Copies des licences incluses dans les assets. Aucune clé de signature privée dans le dépôt ou l’APK.
