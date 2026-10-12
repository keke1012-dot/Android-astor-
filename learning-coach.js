// Curated local exercises; rolling subject state stays only in shortContext RAM.
function createLearningCoach(normalize){
 const banks={
  english:[
   [
    {id:'en-am',prompt:'Complète : I ___ Alex. (Je suis Alex.) Réponds avec le mot manquant.',answer:'am',accept:['am','i am alex','i m alex'],hint:'Avec I, le verbe commence par a…',rule:'Avec I, le verbe to be prend la forme am.'},
    {id:'en-are',prompt:'Complète : You ___ ready. (Tu es prêt.) Réponds avec le mot manquant.',answer:'are',accept:['are','you are ready','you re ready'],hint:'Avec you, le verbe commence par ar…',rule:'Avec you, le verbe to be prend la forme are.'}
   ],
   [
    {id:'en-has',prompt:'Choisis have ou has : She ___ a book.',answer:'has',accept:['has','she has a book'],hint:'Au présent, he, she et it prennent la même forme.',rule:'Au présent, on utilise has avec he, she et it.'},
    {id:'en-went',prompt:'Complète : Yesterday, I ___ to work. Utilise go au passé.',answer:'went',accept:['went','i went to work','yesterday i went to work'],hint:'Go a un passé irrégulier qui commence par w…',rule:'Le passé de go est went : I went to work.'}
   ],
   [
    {id:'en-would',prompt:'Complète : If I had time, I ___ travel more. (Situation hypothétique.)',answer:'would',accept:['would','i would travel more','if i had time i would travel more'],hint:'On exprime ici une conséquence hypothétique avec w…',rule:'Dans cette construction hypothétique : if + prétérit, puis would + verbe.'},
    {id:'en-since',prompt:'Choisis since ou for : I have lived here ___ 2020.',answer:'since',accept:['since','i have lived here since 2020'],hint:'2020 indique un point de départ, pas une durée.',rule:'Since introduit le point de départ ; for introduit une durée.'}
   ]
  ],
  spanish:[
   [
    {id:'es-soy',prompt:'Complète : Yo ___ Alex. (Je suis Alex.) Réponds avec le mot manquant.',answer:'soy',accept:['soy','yo soy alex'],hint:'Pour présenter son identité avec yo, la forme commence par s…',rule:'Pour présenter son identité : yo soy Alex, avec le verbe ser.'},
    {id:'es-estas',prompt:'Complète : Tú ___ aquí. (Tu es ici.) Réponds avec le mot manquant.',answer:'estás',accept:['estas','tu estas aqui'],hint:'Pour situer une personne, on utilise estar.',rule:'Avec tú, estar devient estás : Tú estás aquí. L’accent est sur á.'}
   ],
   [
    {id:'es-hablamos',prompt:'Complète : Nosotros ___ español. Utilise hablar au présent.',answer:'hablamos',accept:['hablamos','nosotros hablamos espanol'],hint:'Avec nosotros, les verbes en -ar se terminent par -amos.',rule:'Hablar devient hablamos avec nosotros au présent.'},
    {id:'es-estar',prompt:'Dans Estoy cansado, quel verbe est conjugué : ser ou estar ?',answer:'estar',accept:['estar','le verbe estar'],hint:'Estoy est la forme yo d’un des deux verbes.',rule:'Estoy vient de estar ; ici, la phrase exprime un état de fatigue.'}
   ],
   [
    {id:'es-viajaria',prompt:'Complète : Si tuviera tiempo, ___ más. Utilise viajar au conditionnel avec yo.',answer:'viajaría',accept:['viajaria','viajaria mas','si tuviera tiempo viajaria mas'],hint:'Le conditionnel se forme avec l’infinitif et la terminaison -ía.',rule:'Pour yo, viajar au conditionnel devient viajaría.'},
    {id:'es-estes',prompt:'Complète : Espero que tú ___ bien. Utilise estar au subjonctif présent.',answer:'estés',accept:['estes','espero que tu estes bien'],hint:'La forme tú au subjonctif se termine ici par -és.',rule:'Après espero que dans cette phrase, estar au subjonctif avec tú devient estés.'}
   ]
  ]
 };
 const labels=['bases','intermédiaire','défi'];
 const langLabel=s=>s.language==='english'?'anglais':s.language==='spanish'?'espagnol':null;
 function level(raw){
  const t=normalize(raw).replace(/ en (anglais|espagnol)$/,'');
  if(/^(je debute|je suis debutant|je suis debutante|debutant|debutante|je pars de zero|je suis novice|je suis mauvais|je suis mauvaise)$/.test(t))return 0;
  if(/^(je connais les bases|je connais quelques bases|je suis intermediaire|intermediaire)$/.test(t))return 1;
  if(/^(je suis avance|je suis avancee|avance|avancee)$/.test(t))return 2;
  return null;
 }
 function seed(raw){
  const t=normalize(raw),en=/\banglais\b/.test(t),es=/\bespagnol\b/.test(t);
  return {language:en!==es?(en?'english':'spanish'):null,level:level(raw)??0,levelChosen:level(raw)!==null,cursor:0,exercise:null};
 }
 function start(subject){
  const language=langLabel(subject);
  if(language&&subject.levelChosen)return 'Nous travaillons l’'+language+' au niveau '+labels[subject.level]+'. Dites « Donne-moi un exercice » pour commencer.';
  return language?'Nous travaillons l’'+language+'. Quel est votre niveau ? Vous pouvez dire « Je débute », puis « Donne-moi un exercice ». Ces exercices locaux couvrent les bases, un niveau intermédiaire et quelques défis.':'Quel sujet voulez-vous travailler ? Précisez par exemple l’anglais, l’espagnol ou une notion. Je proposerai un exercice local quand le sujet sera clair.';
 }
 function recognize(raw,subject){
  const t=localVocabulary.commandText(raw),n=level(t);
  if(seed(raw).language)return null; // An explicit language starts a fresh topic.
  let action=null;
  if(n!==null)action={action:'level',level:n};
  else if(/^(donne (moi )?(un |des )?exercice(s)?|propose (moi )?(un )?exercice|un exercice|exercice|fais moi un exercice|un autre|un autre exercice|encore un exercice|continue|et ensuite|et apres|la suite)$/.test(t))action={action:'exercise'};
  else if(/^(plus simple|plus facile|simplifie|c est trop dur|c est trop difficile|moins difficile)$/.test(t))action={action:'easier'};
  else if(/^(plus difficile|plus dur|complique un peu|un defi)$/.test(t))action={action:'harder'};
  else if(/^(un indice|donne (moi )?(un )?indice|aide moi|je ne sais pas|je sais pas|je n y arrive pas)$/.test(t))action={action:'hint'};
  else if(/^(corrige|corrige moi|la correction|donne (moi )?(la )?(solution|reponse)|solution|quelle est la reponse)$/.test(t))action={action:'solution'};
  else if(/^(pourquoi|et pourquoi|explique ca|explique cela|developpe|developpe un peu|precise|pour quelle raison|comment ca)$/.test(t))action={action:'explain'};
  if(action){
   if(!subject||subject.kind!=='learning')return {name:'learningMissing'};
   return {name:'learningFollow',subject:{...subject,exercise:subject.exercise?{...subject.exercise}:null},...action};
  }
  // Only a short, explicit answer to the immediately pending exercise is graded.
  if(subject?.kind==='learning'&&subject.exercise?.pending){
   const e=find(subject),candidate=t.replace(/^(?:ma reponse est|je reponds|reponse|le mot est|le mot) /,'');
   if(e&&(e.accept.includes(candidate)||/^[a-z]{1,24}$/.test(candidate)))return {name:'learningFollow',subject:{...subject,exercise:{...subject.exercise}},action:'answer',candidate};
   if(/^(ma reponse est|je reponds|reponse|le mot est|le mot) /.test(t))return {name:'learningFollow',subject:{...subject,exercise:{...subject.exercise}},action:'answer',candidate};
  }
  return null;
 }
 function find(s){return banks[s.language]?.flat().find(e=>e.id===s.exercise?.id);}
 function generate(s,lead=''){
  const language=langLabel(s);
  if(!language)return 'Quel sujet voulez-vous travailler ? Les exercices guidés disponibles couvrent l’anglais et l’espagnol. Pour un autre sujet, donnez une notion précise et un exemple à expliquer.';
  const pool=banks[s.language][s.level],e=pool[s.cursor%pool.length];s.cursor=(s.cursor+1)%2;s.exercise={id:e.id,pending:true};
  return lead+'Exercice d’'+language+' · '+labels[s.level]+'\n'+e.prompt+'\nVous pouvez demander « Un indice », « Plus simple » ou « La correction ».';
 }
 function respond(intent){
  const s=intent.subject,language=langLabel(s);
  switch(intent.action){
   case 'level':s.level=intent.level;s.levelChosen=true;s.exercise=null;return 'Je garde '+(language?'l’'+language:'ce sujet')+' comme sujet. Niveau choisi : '+labels[s.level]+'. Dites « Donne-moi un exercice » pour commencer.';
   case 'exercise':return generate(s);
   case 'easier':s.level=Math.max(0,s.level-1);return generate(s,'On simplifie. ');
   case 'harder':s.level=Math.min(2,s.level+1);return generate(s,'On augmente la difficulté dans les exercices disponibles. ');
   case 'hint':{const e=find(s);return e&&s.exercise.pending?'Indice : '+e.hint+' Essayez le mot manquant, ou dites « Réponse : … ».':'Demandez un exercice pour obtenir un indice adapté.';}
   case 'solution':{const e=find(s);if(!e)return 'Demandez un exercice avant sa correction.';s.exercise.pending=false;return 'Réponse attendue : '+e.answer+'. '+e.rule+' Expliquez la règle avec vos mots, ou demandez « Un autre exercice ».';}
   case 'explain':{const e=find(s);return e?'Dans cet exercice : '+e.rule+' Voulez-vous un autre exemple ?':'Partir de votre niveau permet de choisir une difficulté adaptée. Dites « Donne-moi un exercice » pour essayer.';}
   case 'answer':{const e=find(s);if(!e)return 'Quel exercice souhaitez-vous reprendre ?';if(e.accept.includes(intent.candidate)){s.exercise.pending=false;return 'Correct pour cet exercice : '+e.answer+'. '+e.rule+' Voulez-vous un autre exercice ou une version plus difficile ?';}return 'Ce n’est pas la réponse attendue à cet exercice. Indice : '+e.hint+' Réessayez ou demandez la correction.';}
   default:return start(s);
  }
 }
 return {seed,start,recognize,respond};
}
