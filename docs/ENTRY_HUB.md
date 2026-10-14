# Accueil ASTOR Unifiée 0.2.9.19

Accueil local avec date du téléphone, cartes Morphée, Hades, voix et téléphone N2, raccourcis Notes, Modules et Atlas. Le fichier de conversation se trouve dans un panneau repliable. Les réglages vocaux et l’IA locale restent repliables.

Le raccourci N2 exige une session de hub valide et un geste utilisateur. PhoneAccessActivity conserve sa propre authentification biométrique et ses autorisations explicites ; aucune capacité n’est activée par ce raccourci. Morphée conserve sa validation indépendante. Hades respecte sa disponibilité pour la session.

Les styles et le script du hub sont intégrés dans index.html pour respecter le chargement strict de MainActivity. entry-hub.css et entry-hub.js sont les sources correspondantes : les maintenir synchronisés lors des modifications.

Validation automatisée : compilation release et lint Android, régressions du coffre/modules/Hades/N2/voix, rendu Chromium à 320, 393 et 760 pixels, cibles tactiles et absence de débordement horizontal. Les essais biométriques et les transitions entre activités sur téléphone physique restent à effectuer.
