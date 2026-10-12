// Exact command families. No fuzzy match, model training, or mutation of free text.
const localVocabulary=(()=>{
 function commandText(raw){
  let t=normalizeInput(raw);
  t=t.replace(/^(?:bonjour|salut|bonsoir) astor /,'').replace(/^astor /,'');
  t=t.replace(/^(?:s il te plait|s il vous plait) /,'');
  t=t.replace(/^(?:est ce que tu peux|est ce que vous pouvez|peux tu|pourrais tu|pouvez vous|pourriez vous|tu peux|je voudrais que tu|j aimerais que tu) /,'');
  t=t.replace(/^astor /,'').replace(/ (?:s il te plait|s il vous plait|stp|svp)$/,'').trim();
  t=t.replace(/^me (donner|dire|montrer|presenter) /,(_,verb)=>verb+' moi ');
  const verbs={donner:'donne',dire:'dis',afficher:'affiche',montrer:'montre',lister:'liste',presenter:'presente',expliquer:'explique',ouvrir:'ouvre',consulter:'consulte',verrouiller:'verrouille',fermer:'ferme',refermer:'referme',selectionner:'selectionne',choisir:'choisis',preciser:'precise',developper:'developpe',continuer:'continue',oublier:'oublie',aider:'aide'};
  return t.replace(/^[a-z]+/,word=>verbs[word]||word);
 }
 const families={
  status:['statut','etat','quel est ton etat','quel est ton statut','comment va le systeme','etat du systeme','donne moi ton statut','donne ton statut','donne moi le statut','affiche ton statut','affiche le statut','montre ton statut','montre moi ton statut','fais le point','tu es la','es tu la','tu fonctionnes','es tu disponible'],
  time:['heure','quelle heure est il','il est quelle heure','quelle heure il est','quelle heure est ce','donne moi l heure','donne l heure','dis moi l heure','dis l heure','affiche l heure','montre moi l heure','tu as l heure','dis moi quelle heure il est','dis moi quelle heure est il'],
  help:['aide','help','aide moi','besoin d aide','j ai besoin d aide','que peux tu faire','qu est ce que tu sais faire','qu est ce que tu peux faire','quelles sont tes commandes','liste des commandes','liste les commandes','montre les commandes','explique tes fonctions','comment ca marche','comment utiliser astor'],
  memory:['memoire','mes notes','mon coffre','ouvre la memoire','ouvre ma memoire','ouvre le coffre','ouvre mon coffre','ouvre mes notes','consulte la memoire','consulte mes notes','montre la memoire','montre moi la memoire','montre ma memoire','montre moi ma memoire','montre mes notes','affiche mes notes','montre moi mes notes','montre moi le coffre','montre moi mon coffre','etat de la memoire','etat du coffre'],
  modules:['modules','module','liste les modules','liste des modules','quels sont les modules','tes modules','tes systemes','liste les systemes','montre tes modules','presente tes modules','qui compose ton equipe','ton equipe'],
  resetContext:['oublie le sujet','nouveau sujet','efface le contexte','changeons de sujet','on change de sujet','repartons de zero','recommencons la discussion','oublie notre conversation','efface le contexte de discussion'],
  emergency:['urgence','au secours','sos','danger immediat','j ai besoin de secours'],
  lock:['verrouille','verrouiller','verrouille le hub','verrouille astor','verrouillage','ferme le hub','ferme astor','ferme la session','verrouille la session','verrouille l application','verrouille le systeme'],
  lockMemory:['verrouille la memoire','verrouiller la memoire','verrouille ma memoire','verrouille le coffre','verrouille mon coffre','referme le coffre','referme la memoire'],
  clarification:['et pourquoi','pourquoi','continue','explique ca','explique cela','et ensuite','pour quelle raison','comment ca','developpe','developpe un peu','peux tu preciser','precise','la suite','et apres'],
  identity:['qui es tu','presente toi','quel est ton role','qui est astor'],
  security:['niveau de securite','quel est ton niveau de securite','affiche ton niveau de securite','comment sont protegees mes notes','comment est protege le coffre'],
  voiceHelp:['tu m entends','est ce que tu m entends','comment utiliser le micro','comment te parler','comment fonctionne la dictee']
 };
 const commands=new Map();Object.entries(families).forEach(([name,phrases])=>phrases.forEach(phrase=>commands.set(phrase,name)));
 function intent(raw){
  const t=commandText(raw),name=commands.get(t);
  if(name)return {name};
  const module=t.match(/^(?:(?:passe a|passe sur|parle avec|parle a|selectionne|choisis|module) )?(astor|eve|hades|atlas|orpheus|orphee)$/);
  if(module)return {name:'module',module:module[1]==='orphee'?'ORPHEUS':module[1].toUpperCase()};
  if(/^(bonjour|salut|bonsoir|hello|coucou)( astor)?$/.test(t))return {name:'discussion',text:t};
  if(/^(merci|merci beaucoup|merci bien|c est gentil|super merci)( astor)?$/.test(t))return {name:'discussion',text:'merci'};
  return null;
 }
 function analysis(raw){
  const m=String(raw).match(/^\s*(?:astor[\s,:]+)?(?:(?:s['’]il te pla[iî]t|s['’]il vous pla[iî]t)[,\s]+)?(?:(?:peux[- ]tu|pourrais[- ]tu|pouvez[- ]vous|pourriez[- ]vous|tu peux|est[- ]ce que (?:tu peux|vous pouvez)|je voudrais que tu|j[’']aimerais que tu)\s+)?(?:analyse[rz]?|v[ée]rifi(?:e|er)|examin(?:e|er)|contr[oô]l(?:e|er))\b\s*(?:(?:cette|cet|ce)\s*(?:affirmation|raisonnement|hypoth[èe]se|id[ée]e)\s*)?[:—-]?\s*(.*)$/is);
  return m?{name:'analysis',claim:m[1].trim()}:null;
 }
 return {intent,analysis,commandText};
})();
