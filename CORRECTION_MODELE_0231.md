# ASTOR 0.2.3.1 — Modèle local retrouvé après réouverture

Le retour utilisateur indique qu’Astor demande de retélécharger le modèle après une fermeture. Deux défauts de présentation et d’initialisation ont été repérés dans les sources : l’accueil demandait toujours le téléchargement, et le contrôle natif initial pouvait précéder la disponibilité de la page. La cause précise sur le téléphone reste à confirmer.

Cette version republie l’état du fichier lorsque la page est entièrement chargée et après son initialisation JavaScript. L’accueil s’adapte à la disponibilité réelle du modèle. Un modèle déjà présent bloque toute nouvelle demande de téléchargement. La présence du fichier est distinguée de la disponibilité du moteur natif, pour ne pas demander inutilement un téléchargement lorsqu’un moteur est incompatible.

Pendant un téléchargement explicitement lancé, l’écran reste allumé tant que l’application est au premier plan. Le comportement normal revient à la fin du transfert ou à la fermeture. Un verrouillage manuel ou le passage en arrière-plan continue de fermer le coffre et d’annuler le transfert. Un fichier de modèle complet n’est pas supprimé par cette annulation.

Un test Java supplémentaire vérifie les fichiers absents et incomplets, puis la conservation d’un fichier complet après fermeture, annulation et création d’une nouvelle instance. Les tests de routage vérifient aussi l’accueil et le bouton de téléchargement avec un modèle présent.

Installer comme mise à jour d’Astor Mémoire sans désinstaller. Vérifier ensuite : modèle disponible, fermeture, réouverture, modèle toujours disponible et réponse générée sans nouveau téléchargement. Les souvenirs et notes utilisent le même stockage et la même phrase secrète qu’auparavant.
