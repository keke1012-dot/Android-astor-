# Empreinte vocale — chantier à préparer

Statut : non implémentée, aucun accès vocal autorisé dans Astor 0.1.7.

Objectif demandé : déverrouiller par l’identité vocale du propriétaire, avec une protection comparable au besoin réel du coffre. Dire une phrase correcte ne prouve pas l’identité.

1. Définir le risque accepté : enregistrement, imitation, clonage vocal, bruit et erreur d’identification. Choisir si la voix sert seulement à lancer la fenêtre biométrique ou devient une alternative d’accès ; cette dernière exige une validation plus forte.
2. Enrôlement local après validation biométrique, plusieurs échantillons et consentement explicite. Prévoir la suppression du modèle et la révocation.
3. Étudier un modèle de vérification de locuteur et une détection de rejeu/synthèse. Une phrase aléatoire réduit certains rejeux simples ; elle ne garantit pas la résistance au clonage.
4. Protéger le modèle vocal localement et éviter les enregistrements permanents ou transmis. Demander le microphone uniquement pour une fonction volontaire ; pas d’écoute en arrière-plan par défaut.
5. Mesurer fausses acceptations et faux rejets sur plusieurs voix, appareils, environnements et attaques, avec un jeu de test distinct de l’enrôlement. Fixer les seuils avant d’activer un accès vocal.
6. En cas de doute, fermer l’accès ; conserver la biométrie comme méthode de référence. Ne pas présenter un prototype vocal comme une certification Android ou Samsung.

Aucune date de livraison ou efficacité contre le clonage ne peut être annoncée avant ces essais. Premier prototype conseillé : la commande vocale déclenche la biométrie sans ouvrir elle-même le coffre.

11 octobre 2026 : l’utilisateur a choisi de commencer par parler à Astor. La 0.1.7 ajoute la dictée française sur moteur local Android, après biométrie. Elle ne crée aucun modèle de locuteur et ne reconnaît pas l’identité vocale. Le chantier d’empreinte vocale reste distinct et non implémenté.
