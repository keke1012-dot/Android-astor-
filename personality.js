/* ASTOR Personality V1: pure local dialogue policy, no tool or storage access. */
(function(root){
'use strict';
function create({now=()=>Date.now(),allowed=()=>true}={}){
 let humor=true,initiative=true,lastProposal=-Infinity,pending=null;
 const normalize=s=>String(s).normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().replace(/[’']/g,' ').replace(/[^a-z0-9 ]/g,' ').replace(/\s+/g,' ').trim();
 const protectedText=s=>/\b(pin|password|token|secret|cle api|api key|mot de passe|carte bancaire)\b|sk-[a-z0-9]/i.test(normalize(s));
 function reset(){pending=null;lastProposal=-Infinity;}
 function instructions(){return 'Élégant, franc, chaleureux. '+(humor?'Humour discret, jamais en détresse.':'Sans humour.')+' Contredis avec une raison vérifiable. '+(initiative?'Une suggestion utile, sans insister.':'Aucune initiative spontanée.')+' Prépare du texte seulement. Action sensible : confirmation spécifique dans son outil. Atlas vérifie, Eve guide, Hades protège, Mnémosyne fournit le contexte validé autorisé, Morphée accompagne le repos.';}
 // This engine never grants permission. The native executor remains authoritative.
 function actionPolicy({kind,authorized=false,confirmed=false}={}){
  if(kind==='prepareText')return authorized?'prepare':'propose';
  if(['send','delete','purchase','permission','export','calendar','phone'].includes(kind))return authorized&&confirmed?'nativeReview':'confirm';
  return 'deny';
 }
 function reply(raw,{module='ASTOR'}={}){
  const t=normalize(raw),time=now();
  if(protectedText(raw)){reset();return null;}
  if(pending&&time-pending.at>=300000)pending=null;
  if(module!=='ASTOR')return null;
  if(/^(sans humour|desactive l humour|arrete les blagues|sois serieux)$/.test(t)){humor=false;return 'Compris. Je garde un ton sobre pour cette session.';}
  if(/^(active l humour|remets l humour)$/.test(t)){humor=true;return 'Humour réactivé. Avec modération : je tiens à rester fréquentable.';}
  if(/^(pas d initiatives|desactive les initiatives|ne propose rien spontanement)$/.test(t)){initiative=false;pending=null;return 'Initiatives suspendues pour cette session. Tu gardes la main.';}
  if(/^(active les initiatives|reprends les initiatives)$/.test(t)){initiative=true;return 'Initiatives réactivées : une suggestion pertinente à la fois, sans action automatique.';}
  if(/^(non merci|laisse tomber|pas maintenant|arrete d insister)$/.test(t)){pending=null;lastProposal=time;return 'Compris. Je laisse cette proposition de côté.';}
  if(pending&&/^(oui|vas y|prepare|prepare le plan|prepare ca)$/.test(t)){
   const kind=pending.kind;pending=null;
   return kind==='sleep'?'Préparation proposée : termine la tâche en cours, note ce qui reste pour demain, puis choisis une pause adaptée. Aucun rappel ni réglage du téléphone n’a été créé.':'Plan préparé : 1. Choisir un objectif utile. 2. Vérifier la fonction existante. 3. Tester une petite modification isolée. 4. Décider après le résultat. Aucun fichier ni réglage n’a été modifié.';
  }
  if(/^(raconte (moi )?une blague|fais (moi )?une blague|un peu d humour)$/.test(t))return humor?'Mon ambition : t’aider à gagner du temps. Ton ambition : me rajouter trois modules avant le café. On négocie ?':'Tu as choisi un ton sobre. Dis « Active l’humour » pour le changer.';
  if(/^(tu es nul|t es nul|tu sers a rien)$/.test(t))return humor?'Critique reçue. Mon ego devrait survivre : montre-moi la réponse ratée, et corrigeons le fond.':'Montre-moi la réponse ratée. Je préciserai l’erreur et la correction.';
  if(allowed('ATLAS')&&/^(astor )?(est |sera |est il )?(impossible a pirater|inviolable|100 securise)$/.test(t))return 'Je te contredis : aucun logiciel ne peut garantir une sécurité absolue. Des tests réussis réduisent l’incertitude, mais ne prouvent pas l’absence de faille. Vérifions les permissions et les mises à jour.';
  if(allowed('ATLAS')&&/^(tous les tests passent donc (tout fonctionne|il n y a aucun bug)|les tests prouvent qu il n y a aucun bug)$/.test(t))return 'Je te contredis sur la conclusion : les tests couvrent des cas choisis. Ils peuvent tous réussir et laisser passer un défaut sur ton téléphone. Il faut aussi une validation sur appareil.';
  if(/^(envoie|supprime|achete|publie|exporte|donne acces)\b/.test(t)){
   pending=null;
   return allowed('HADES')?'Cette action demande un contrôle Hades et une confirmation précise dans l’outil concerné. Indique la cible et le résultat attendu ; ce message n’exécute rien.':'Hades est désactivé. Je ne prépare aucune exécution sensible. Réactive-le et utilise l’outil concerné.';
  }
  if(pending&&!/^(oui|vas y|prepare|prepare le plan|prepare ca)$/.test(t))pending=null;
  if(initiative&&time-lastProposal>=600000){
   let kind=null;
   if(allowed('ATLAS')&&/^(je veux ajouter (plein de|dix|10|encore des) modules|on ajoute tout d un coup)$/.test(t))kind='project';
   if(allowed('MORPHEE')&&/^(je suis (creve|fatigue|epuise)|j ai mal dormi)$/.test(t))kind='sleep';
   if(kind){pending={kind,at:time};lastProposal=time;return kind==='project'?'Je freine un peu : ajouter beaucoup de modules augmente les risques de régression. Je propose de consolider une fonction utile avant la suivante. Je peux préparer un petit plan de validation.':'Tu signales de la fatigue. Je propose une pause et de garder les décisions complexes pour plus tard. Je peux préparer une courte transition vers le repos, sans activer Morphée ni créer de rappel.';}
  }
  return null;
 }
 return Object.freeze({reply,reset,instructions,actionPolicy});
}
root.ASTOR_PERSONALITY=Object.freeze({create});
})(typeof window==='undefined'?globalThis:window);
