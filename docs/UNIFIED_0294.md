# ASTOR Unifiée 0.2.9.4 — calcul du quotidien V2

Améliore le moteur de la 0.2.9.3 sans changer la signature, l'identifiant, les permissions ou le stockage. versionCode 32. Installation par-dessus la 0.2.9.3. Tous les calculs restent locaux et prioritaires sur l'IA.

Nouveautés : multiplication numérique ASCII « 17x19 » sans égalité ; formules « Peux-tu calculer … », « pour cent » et « Résous l’équation : … » ; équations à une seule inconnue choisie parmi x, y ou t ; regroupement lisible sans coefficients nuls. Les équations à plusieurs inconnues sont refusées explicitement.

Montants : « 12,50 € + 3,20 € », « 12,50 € × 3 », « TVA 20 % sur 80 € », « remise de 15 % sur 80 € ». Les unités restent explicites : une somme en euros ne se convertit pas en unité physique. « TVA … sur … » annonce l'interprétation du montant de base comme HT ; aucun taux légal n'est choisi. Les valeurs exactes et arrondies au centime sont distinguées.

Partage monétaire équilibré : « partage 10 € en 3 » produit 1 part à 3,34 € et 2 parts à 3,33 €, total 10,00 €. Les restes de centimes sont distribués ; un montant initial non entier en centimes est d'abord arrondi et signalé. Tests de conservation du total sur des séries de montants et de nombres de parts.

Limites conservées : arithmétique rationnelle bornée, 250 caractères, équations réelles polynomiales jusqu'au degré 2, aucune division par l'inconnue ni système. Pas de compréhension garantie de toute formulation française. Les marqueurs monétaires ambiguës et les taux associés à une unité euro sont refusés.

Validation : tests de grammaire, fractions exactes, séries d'équations, partage au centime, entrées invalides ; interface avec IA activée sans appel au modèle ni réseau. Compilation/lint Android et contrôle de mise à jour signée contre la 0.2.9.3. Le fonctionnement réel et la conservation des données doivent être confirmés sur le téléphone.
