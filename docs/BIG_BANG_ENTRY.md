# Entrée Big Bang — ASTOR Unifiée 0.2.9.20

Après validation biométrique, à chaque création du hub, une séquence locale de 2,8 secondes forme une singularité, projette 48 étoiles puis révèle ASTOR. Aucun son ni accès réseau. Bouton Passer et touche Échap, retour immédiat au hub. Les surfaces du hub sont temporairement inertes ; leur état antérieur est restauré. L’entrée est annulée au verrouillage, lorsque l’application est masquée, à la fermeture ou si la réduction des animations est activée. Elle est entièrement omise avec prefers-reduced-motion.

Les sources big-bang-entry.js/css sont intégrées dans index.html pour respecter la liste stricte des ressources locales autorisées. Les maintenir synchronisées.

Validation : rendu Chromium mobile, fin automatique, passer, Échap, verrouillage, arrière-plan, réduction des animations et restauration de l’accès aux commandes ; compilation release et lint Android, régressions voix/mémoire/modules. Validation des performances sur téléphone physique à effectuer.
