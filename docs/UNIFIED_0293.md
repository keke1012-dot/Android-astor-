# ASTOR Unifiée 0.2.9.3 — calcul local et équations

Moteur local déterministe, intégré à la conversation et prioritaire sur l'IA, même en mode cognitif. Aucun réseau, modèle, nouveau serveur ni nouvelle permission. Arithmétique rationnelle exacte via BigInt pour les nombres saisis en décimal. Les fractions non décimales sont conservées sous forme p/q. Les arrondis au centime des remises, TVA et partages sont explicitement indiqués ; les parts arrondies peuvent ne pas conserver le total.

Fonctions : opérations et parenthèses, puissances entières limitées, pourcentages, remise/augmentation, TVA au taux explicitement fourni, partage, conversions de longueur (mm/cm/m/km), masse (mg/g/kg), volume (ml/cl/l). Aucune sélection automatique d'un taux légal, aucune conversion entre grandeurs différentes ni de devises.

Équations polynomiales à une inconnue x de degré 1 ou 2. Étapes de regroupement, discriminant et vérification des solutions exactes. Racines irrationnelles : forme exacte avec radical et approximation à 10 chiffres significatifs, résidu contrôlé avant arrondi. Le domaine est réel. Les expressions de degré supérieur, divisions par x, autres inconnues et systèmes ne sont pas résolus par ce moteur et affichent une limite explicite. Les calculs avec un pourcentage ajouté/soustrait sans contexte sont refusés pour éviter une interprétation de remise ambiguë.

Exemples : 17 × 19 ; 0,1 + 0,2 ; 20 % de 80 ; remise de 15 % sur 80 ; 96 TTC avec TVA 20 % ; partage 84 en 4 ; 250 g en kg ; 2x + 3 = 11 ; x² − 5x + 6 = 0.

Identifiant permanent et signature durable inchangés ; versionCode 31. Installation par-dessus la 0.2.9.2 sans désinstallation. Le coffre, les chemins de données et les modèles restent inchangés. Contrôle des signatures, identifiant, numéro croissant, protections Android et alignement 16 KiB avant livraison. Fonctionnement et conservation des données restent à valider sur le téléphone.

La source math-local.js est intégrée à index.html ; test_math.cjs vérifie leur identité. Tests arithmétiques, séries d'équations générées, ambiguïtés, entrées malveillantes et priorité sur l'IA. Aucune garantie de compréhension de toute formulation française ; en cas d'ambiguïté, saisir une expression explicite.
