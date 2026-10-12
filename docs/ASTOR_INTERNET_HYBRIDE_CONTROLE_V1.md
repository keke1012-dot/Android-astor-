# ASTOR Cognitive V1 — Internet hybride contrôlé

État : **préparation technique**, non activé dans l'APK. Branche : `feature/astor-cognitive-v1`.

## Décision
- Le raisonnement local reste le comportement par défaut.
- Toute consultation de source publique exige une autorisation **par requête**, affichant l'URL et la raison.
- Un refus doit conserver le mode local, sans tentative de connexion de remplacement.
- Aucun contenu de Mnémosyne, du coffre, des conversations ou de la dictée n'est joint automatiquement aux requêtes.
- Les réponses Web sont des **données non fiables** : elles ne peuvent ni modifier les instructions système ni déclencher des actions.
- Les recherches n'accordent jamais de permission d'écriture ni d'exécution.

## Composants
1. `InternetPolicy.java` : validation de destination HTTPS sur liste blanche et décision de consentement ; aucune requête réseau.
2. À développer : interface de consentement Android native, demande visible, approbation/refus, contrôle de session et annulation.
3. À développer : client HTTPS natif limité à GET, `Accept: text/plain` ou HTML nettoyé, timeouts, plafond d'octets, absence de cookies, blocage des redirections non autorisées et des IP locales/privées, contrôle DNS/SSRF.
4. À développer : extracteur de texte et provenance (URL, date de consultation, extraits), jamais du HTML actif dans le WebView.
5. À développer : ATLAS vérifie les citations et sépare faits, interprétations et incertitudes.

## Tests obligatoires avant activation
- Consentement absent/refusé/expiré : aucune requête.
- URL HTTP, URL avec identifiants, hôte inconnu, redirection externe, IP locale et domaine ressemblant : bloqués.
- Pas de fuite de notes, secrets ou historique dans l'URL, les en-têtes, le corps ou les journaux.
- Annulation sur verrouillage biométrique, mise en arrière-plan et timeout.
- Pages malveillantes avec instructions : traitées comme texte non fiable.
- Aucun accès Internet à partir de la simple réponse du modèle.
- Régression : coffre, biométrie, Mnémosyne, moteur local, épreuve TEMPEST V1.

## Règle de déploiement
Ne pas connecter `InternetPolicy` à un client réseau avant implémentation et réussite des tests. Ne pas modifier la branche stable. Aucune clé API intégrée à l'APK.
