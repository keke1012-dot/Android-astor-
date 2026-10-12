# ASTOR 0.2.3.3 — génération en français

Le décodage greedy est remplacé par top-k 20, top-p 0,8 et température 0,6, adaptés à partir des recommandations Qwen en mode sans réflexion. Une pénalité de répétition de 1,05 et de présence de 0,1 sur 64 tokens vise à réduire les reprises sans trop perturber le français. Les consignes système courtes sont rédigées en anglais et demandent de répondre en français, sauf demande d’une autre langue, sans répétitions et en signalant les incertitudes.

Source : https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/blob/main/README.md

Une première comparaison a montré des erreurs factuelles importantes et seulement une séquence répétée évitée. Les consignes ont été simplifiées et les pénalités diminuées. Dix questions françaises synthétiques comparent les deux décodages avec les mêmes nouvelles consignes. La graine 42 est réservée au test sur ordinateur ; Android garde une graine aléatoire. Le rapport CI conserve les réponses et compte les séquences de cinq mots répétées. Cette mesure ne garantit ni la justesse factuelle, ni la vitesse sur téléphone. Le test JNI existant, les tests de conversation, de confidentialité et de mémoire, la compilation Android et le lint sont conservés.

Le modèle, son empreinte, l’emplacement de stockage, le package Android et le format du coffre restent identiques. Installer en mise à jour avec la clé stable conserve le modèle téléchargé et les souvenirs. Ne pas désinstaller l’application.
