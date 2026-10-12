# Tests Android · Astor 0.1.9

Statut initial : à tester sur appareil. Aucun succès matériel n’est déclaré à l’avance.

1. Au lancement, seul l’écran d’accès doit apparaître. Annuler la fenêtre Android : aucun accès au Hub.
2. Essayer une empreinte non reconnue : le Hub reste fermé. Utiliser l’empreinte enregistrée : le Hub apparaît.
3. Fermer puis rouvrir : aucune option code, schéma ou mot de passe ne doit ouvrir Astor. Le bouton Annuler laisse le Hub fermé. Après déverrouillage du téléphone par son code, Astor exige encore sa propre validation biométrique.
4. Passer à une autre application puis revenir : nouveau déverrouillage requis, notes masquées et conversation effacée.
5. Éteindre/rallumer l’écran, faire pivoter le téléphone, fermer puis relancer : aucun retour direct au Hub ouvert.
6. Envoyer « Je veux apprendre l’anglais », puis « verrouille ». Réouvrir avec Android : l’échange précédent doit avoir disparu.
7. Créer une note NON sensible avec une phrase de test de 12 caractères minimum, enregistrer et fermer. Après réauthentification Android, le coffre reste verrouillé. La bonne phrase du coffre retrouve la note, une mauvaise phrase échoue.
8. Laisser le Hub cinq minutes sans interaction : l’accès doit se fermer.
9. Désactiver le module HADES et envoyer « urgence » : réponse de secours ASTOR. Fermer l’application : l’authentification native doit rester requise.
10. Vérifier les routes apprentissage → EVE, analyse → ATLAS, urgence → HADES, pause musique → ORPHEUS ; désactivation et réactivation de chaque module.
11. Cliquer un lien YouTube : demander confirmation, puis ouvrir le navigateur et verrouiller ASTOR. Au retour, nouveau déverrouillage.
12. Sur un appareil de test sans biométrie forte enregistrée : aucun Hub accessible, même si un code du téléphone existe. Ne pas retirer ses empreintes sur le téléphone principal pour ce test. Capteur indisponible ou blocage : accès fermé.
13. Lancer l’authentification puis appuyer sur Accueil ; revenir. Aucun Hub ne doit apparaître sans une validation Android terminée. Une validation interrompue est annulée ; une ancienne réponse ne doit pas ouvrir le Hub au retour.

Les empreintes ne sont pas copiées dans ASTOR. Les captures sont bloquées : rapporter les résultats par texte. Conserver V3.8.2.1 tant que ces tests ne sont pas terminés.

## Contrôles de fluidité et de mise à jour

- Installer Astor.apk comme mise à jour de 0.1.0 à 0.1.6, puis retrouver la note déjà enregistrée après déverrouillage du coffre.
- Vérifier le nom Astor sur l’écran d’accueil.
- Au lancement, la fenêtre biométrique s’affiche ; l’annuler laisse l’accès fermé sans boucle de demandes.
- Passer entre Accueil, Notes, Systèmes, Atlas et Média. Un seul panneau principal s’affiche à la fois ; revenir à Accueil conserve l’échange dans la session actuelle.
- Utiliser Statut, Heure et Aide depuis la console.
- Ouvrir le clavier et envoyer une phrase ; vérifier que la saisie et le bouton Envoyer restent accessibles.
- Utiliser Verrouiller dans l’en-tête ; réouvrir doit retrouver un coffre fermé et une conversation effacée.
- Refaire les contrôles d’empreinte, de refus de code, d’arrière-plan et d’inactivité.

## Musique d’entrée

- Téléphone en mode sonore, aucune musique externe active : annuler la demande biométrique. Aucun son d’entrée et aucun Hub ne doivent apparaître.
- Déverrouiller par empreinte : le thème joue une seule fois, pendant environ huit secondes, après apparition du Hub.
- Changer d’onglet pendant la lecture : aucun second départ.
- Pendant la lecture, passer sur une autre application : arrêt immédiat, aucun son en arrière-plan et nouveau déverrouillage requis au retour.
- Pendant la lecture, utiliser Verrouiller : arrêt immédiat.
- Annuler la fenêtre biométrique puis ouvrir Paramètres Astor, désactiver Musique d’entrée et Enregistrer. Déverrouiller et relancer : aucun thème ; la préférence doit rester désactivée. Réactiver et retester.
- En mode silencieux ou vibreur : aucune musique d’entrée.
- Avec une musique déjà active dans une autre application : aucune musique d’entrée ; la musique externe doit continuer.
- Refaire l’accès biométrique, le retour d’arrière-plan et le déverrouillage des notes. L’ajout audio ne doit pas changer ces protections.

## Paramètres et logo

- Vérifier le nouveau A coloré dans l’icône Astor du lanceur. Le lanceur peut garder temporairement une ancienne icône en cache ; ne pas effacer les données Astor pour la rafraîchir.
- Ouvrir Paramètres depuis le Hub ; vérifier que le dialogue peut défiler sur petit écran et que Enregistrer/Annuler restent accessibles.
- Modifier volume, texte et animations puis Annuler : aucun changement conservé. Refaire et Enregistrer : paramètres appliqués.
- Relancer Astor et rouvrir Paramètres : retrouver chaque choix.
- Taille 120 % : vérifier les boutons de navigation, la saisie et les notes sur le téléphone.
- Animations désactivées : anneaux et apparition des messages restent fixes ; réactiver et vérifier.
- Choisir 1 minute, Enregistrer puis laisser le Hub sans interaction pendant une vraie minute : accès fermé. Déverrouiller et retrouver les notes chiffrées avec la phrase secrète.
- Ouvrir Paramètres puis passer sur une autre application : panneau fermé, nouvelle biométrie obligatoire au retour.
- Depuis l’écran verrouillé, ouvrir Paramètres puis Enregistrer : aucun accès au Hub sans biométrie.
- Répéter les tests d’annulation biométrique, d’accès par empreinte, d’arrière-plan et de récupération des notes.

## Logo animé

- Annuler la fenêtre biométrique : logo visible dans l’écran d’accès, anneau mobile et A stable. Le Hub reste fermé.
- Déverrouiller : même logo animé dans le noyau du Hub.
- Couper Animations dans Paramètres puis Enregistrer : anneau immobile dans le Hub et sur l’écran d’accès après Verrouiller.
- Réactiver : rotation douce, aucune double animation ; relancer et vérifier le réglage conservé.
- Passer en arrière-plan puis revenir : biométrie obligatoire, animation reprise uniquement au premier plan.
- Désactiver les animations dans Android : vérifier le comportement sur ce modèle.
- L’icône de l’écran d’accueil doit rester fixe.

## Nouvelle interface

- Vérifier la barre de navigation en bas et les icônes Paramètres et Verrouiller en haut.
- Avec le clavier ouvert, envoyer un message sans que la navigation masque le bouton.
- À 120 % de texte, faire défiler chaque vue et atteindre tous les contrôles.
- Vérifier le nouvel écran d’accès et le dialogue de paramètres sur le Samsung.

## Dictée — à valider sur Samsung

- Avant le premier toucher du micro : aucun indicateur d’écoute ni demande de permission.
- Refuser Microphone : accès biométrique inchangé, clavier utilisable, message clair après toucher du micro.
- Autoriser : si le Hub se verrouille pendant la demande système, réauthentifier et retoucher le micro ; aucune écoute ne doit reprendre seule.
- Dire « Je veux apprendre l’anglais » : vérifier la transcription, corriger au besoin, toucher Envoyer ; route EVE locale.
- Avec un brouillon existant, dicter une seconde phrase : la première reste présente.
- Annuler pendant l’écoute, changer de vue ou verrouiller : indicateur Android du micro doit disparaître ; aucune transcription tardive ne doit remplir une nouvelle session.
- Pendant l’écoute, passer en arrière-plan ou éteindre l’écran : micro arrêté et nouveau déverrouillage requis.
- Silence prolongé et délai maximal 20 secondes : écoute arrêtée et nouvel essai manuel possible.
- Moteur français local indisponible : erreur, aucun repli distant, clavier accessible.
- Essayer en mode avion avec le modèle français déjà installé.
- Révoquer Microphone dans Android : plus de dictée, mais accès par biométrie et clavier conservés.
- Clavier affiché et texte à 120 % : micro, saisie et bouton d’envoi accessibles.

Cette livraison ne confirme pas la présence d’un moteur français local sur ce Samsung : noter le message exact si le micro ne peut pas démarrer.

## Correction de la commande vocale Statut

Sur une saisie vide, les transcriptions courtes « statue », « statut », « statuts », « status » et leur forme précédée d’« Astor » sont reconnues comme la commande Statut. La saisie affiche Statut pour relecture ; l’envoi reste manuel. Une phrase comme « la statue de la liberté » ou une dictée ajoutée à un brouillon ne subit pas cette correction. La précision acoustique du moteur Android n’est pas modifiée.

Essai de cette correction : sur une saisie vide, dire « statut » ou « Astor statut ». Si Android transcrit « statue », la saisie doit montrer Statut. Toucher Envoyer doit produire le statut. Puis dicter « la statue de la liberté » : cette phrase doit rester intacte.

## Vocabulaire local étendu

Commandes formulées naturellement et avec politesse, présentation des rôles, choix explicite d’un module, mots supplémentaires pour l’apprentissage et l’organisation. « Peux-tu me donner l’heure ? », « Montre mes notes », « Passe à Eve », « Contrôle cette affirmation » et « Changeons de sujet » sont compris. Le vocabulaire est commun aux demandes écrites et aux transcriptions vocales. Aucune modification du moteur acoustique Android, aucun entraînement automatique ni conversation IA libre.

Les commandes sont reconnues par familles exactes, sans recherche approximative générale. Les notes exigent toujours leur phrase secrète. Les modules désactivés restent bloqués. La négation d’une commande n’exécute pas cette commande. Aucun accès vocal au coffre, aucune suppression de notes ajoutée. La réponse Orpheus rappelle la disponibilité réelle du lecteur intégré.

Voir VOCABULAIRE_FR.md pour les exemples et TESTS_ANDROID.md pour les essais sur téléphone. La biométrie, le coffre, le microphone à activation tactile et l’envoi manuel sont conservés.

### Essais de vocabulaire

- Dire « Peux-tu me donner l’heure ? », relire puis envoyer : heure locale.
- « Montre mes notes » : état du coffre, aucun déverrouillage sans phrase.
- « Passe à Eve » puis « Je voudrais réviser l’anglais » : Eve. Désactiver Eve puis répéter : réponse ASTOR indiquant le blocage.
- « Peux-tu vérifier cette affirmation : un seul test prouve que tout est fiable » : Atlas.
- « Ne verrouille pas le hub » : aucune fermeture. « Verrouille le coffre » : coffre fermé.
- « Changeons de sujet » : contexte temporaire effacé, coffre inchangé.

## Parcours de conversation à valider

1. Installer en mise à jour, sans désinstaller. Déverrouiller par empreinte et vérifier une note de test déjà enregistrée.
2. Envoyer « Je veux apprendre l’anglais », « Je débute », « Donne-moi un exercice ». Eve doit conserver l’anglais et proposer un exercice de bases.
3. Passer par Notes puis revenir dans Accueil : « Plus simple » doit rester lié à l’anglais.
4. Demander « Un indice », « Je ne sais pas », « La correction » ; comparer à l’exercice affiché. Pour répondre, utiliser le mot manquant ou « Réponse : … ».
5. Demander « Plus difficile », puis « Un autre exercice » : difficulté et consigne doivent changer dans la petite banque disponible.
6. Envoyer « Je veux apprendre l’espagnol » : les demandes suivantes portent sur l’espagnol.
7. « Changeons de sujet », puis « Plus simple » : Astor doit demander de préciser le sujet.
8. Désactiver Eve et répéter le parcours : blocage, aucun exercice Eve.
9. Verrouiller puis réouvrir : « Plus simple » ne doit pas retrouver la leçon. Vérifier le même effacement après passage en arrière-plan et expiration.
10. Essayer au clavier puis via le micro, toujours après relecture de la transcription. Vérifier que la dictée, le coffre et la biométrie fonctionnent comme auparavant.

Aucun profil scolaire permanent ou historique d’apprentissage n’est créé.
