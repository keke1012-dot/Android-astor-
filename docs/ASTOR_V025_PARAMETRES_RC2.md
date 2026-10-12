# ASTOR 0.2.5 RC2 — Paramètres du dialogue

Consignes locales en français : langage simple, réponses en quelques phrases, interprétation des fautes courantes, une question précise si nécessaire, reformulation contextuelle et reconnaissance des incertitudes. Demande explicite de ne pas inventer de faits, sources ou résultats d’actions. Les consignes propres à Eve, Atlas, Hades, Orpheus et Tempest restent distinctes. Ces consignes orientent le modèle sans garantir son obéissance ou sa justesse.

Contexte local : au maximum deux paires question/réponse, 200 caractères par message et cinq minutes de validité. Budget indicatif de 2200 caractères pour le prompt : réduction de l’historique avant génération ; la question de l’utilisateur est conservée entièrement après suppression des marqueurs de dialogue. Une question dépassant à elle seule la capacité native reste refusée par le moteur. Un budget de caractères ne garantit pas un nombre de tokens.

Souvenirs : uniquement si le coffre est déverrouillé et l’autorisation de session cochée ; au maximum trois souvenirs, 400 caractères au total, limités selon la place disponible. Aucune autorisation automatique. Aucun transfert de l’ancienne application, import de corpus ou entraînement de poids réalisé par ce changement.

Limites natives conservées : contexte 1024 tokens, entrée limitée à 880 tokens, génération 128 tokens, quatre threads, délai 90 secondes. Échantillonnage conservé : température 0,6, top-k 20, top-p 0,8. Aucun gain de RAM, latence ou justesse revendiqué sans mesures.

Validation : tests locaux du dialogue, des commandes, du coffre, de Mnémosyne, de la mémoire durable et gardes de régression. Nouveaux cas : expiration du contexte, conservation de la question, paires récentes et neutralisation des marqueurs de dialogue. Compilation et tests du modèle réel confiés au workflow CI de la branche isolée ; validation téléphone encore nécessaire.
