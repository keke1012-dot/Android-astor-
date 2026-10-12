const fs=require('fs'),vm=require('vm'),assert=require('assert'),{webcrypto}=require('crypto');
const html=fs.readFileSync(require('path').join(__dirname,'app/src/main/assets/index.html'),'utf8');
class Node{constructor(){this.value='';this.textContent='';this.checked=false;this.disabled=false;this.style={};this.children=[];this.events={};this.dataset={};this.src='';const values=new Set();this.classList={add:x=>values.add(x),remove:x=>values.delete(x),contains:x=>values.has(x),toggle:(x,b)=>{b??=!values.has(x);b?values.add(x):values.delete(x);return b}}}addEventListener(n,f){(this.events[n]??=[]).push(f)}async fire(n,e={}){for(const f of this.events[n]||[])await f(e)}append(n){this.children.push(n)}replaceChildren(){this.children=[]}setAttribute(){}removeAttribute(){}pause(){}load(){}focus(){}click(){return this.onclick?.()} }
const nodes={};for(const [,id]of html.matchAll(/id="([^"]+)"/g))nodes[id]=new Node();nodes.memory.disabled=true;nodes.aiEndpoint.value='/api/chat';const doc=new Node();doc.body=new Node();doc.getElementById=id=>nodes[id];doc.createElement=()=>new Node();doc.querySelector=()=>new Node();const modules=['ASTOR','EVE','HADES','ATLAS','ORPHEUS','TEMPEST'].map(name=>{const n=new Node();n.dataset.module=name;return n});doc.querySelectorAll=()=>modules;
const win=new Node();win.location={};win.isSecureContext=true;win.crypto=webcrypto;const storage=new Map();let network=0;const env={document:doc,window:win,crypto:webcrypto,localStorage:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k)},TextEncoder,TextDecoder,URL,Event,AbortController,location:{protocol:'https:',origin:'https://appassets.androidplatform.net',href:'https://appassets.androidplatform.net/assets/index.html',hostname:'appassets.androidplatform.net'},setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>1,requestAnimationFrame:f=>f(),confirm:()=>true,fetch:()=>{network++;throw Error('network forbidden')}};win.dispatchEvent=e=>win.fire(e.type);vm.createContext(env);for(const[tag,js]of html.matchAll(/<script[^>]*>(.*?)<\/script>/gs)){if(!tag.includes('id="androidFluidUi"'))vm.runInContext(js.replace("const shortContext=createShortContext(normalizeInput);","window.__makeContext=createShortContext;window.__normalizeInput=normalizeInput;const shortContext=createShortContext(normalizeInput);"),env);}
const event={preventDefault(){}};const send=async s=>{nodes.message.value=s;await nodes.chatForm.onsubmit(event);return nodes.chatlog.children.at(-1)?.textContent};
(async()=>{
 assert(nodes.enableAI.disabled);assert(nodes.testAI.disabled);assert(nodes.voice.disabled);assert(nodes.localAudio.disabled);
 assert.match(await send('Je veux apprendre anglais'),/^EVE · mode local/);
 assert.match(await send('Analyse cette affirmation : un seul test prouve la fiabilité'),/^ATLAS · mode local/);
 assert.match(await send('urgence'),/^HADES · mode local/);
 nodes.enableModuleHADES.checked=false;await nodes.enableModuleHADES.fire('change');
 assert.match(await send('urgence'),/ASTOR : Hades est désactivé/);
 nodes.enableModuleHADES.checked=true;await nodes.enableModuleHADES.fire('change');
 assert.match(await send('pause musique'),/^ORPHEUS · mode local/);

 const phrases={
  status:['Donne-moi ton statut','Peux-tu afficher ton statut, s’il te plaît ?','Astor, tu es là ?','Bonjour Astor, peux-tu me donner ton statut ?'],
  time:['Il est quelle heure ?','Peux-tu me donner l’heure ?','Pourrais-tu me dire l’heure, s’il te plaît ?','Astor, affiche l’heure'],
  help:['J’ai besoin d’aide','Qu’est-ce que tu sais faire ?','Peux-tu expliquer tes fonctions ?'],
  memory:['Montre mes notes','Peux-tu ouvrir mon coffre ?','Affiche mes notes, stp','Pourrais-tu me montrer mes notes ?'],
  modules:['Présente tes modules','Peux-tu lister les systèmes ?','Qui compose ton équipe ?'],
  identity:['Présente-toi','Qui es-tu ?'],
  security:['Quel est ton niveau de sécurité ?','Comment sont protégées mes notes ?'],
  voiceHelp:['Tu m’entends ?','Comment utiliser le micro ?'],
  resetContext:['Changeons de sujet','Repartons de zéro'],
 };
 const expected={status:/Interface active/,time:/Heure locale/,help:/Vous pouvez dire/,memory:/Mémoire verrouillée/,modules:/ASTOR :/,identity:/Je suis Astor/,security:/biométrie forte Android/,voiceHelp:/Touchez le micro/,resetContext:/Contexte de discussion effacé/};
 for(const [family,requests] of Object.entries(phrases))for(const request of requests)assert.match(await send(request),expected[family],request);
 for(const request of ['Je voudrais réviser l’anglais','Enseigne-moi l’espagnol','Je veux comprendre les fractions'])assert.match(await send(request),/^EVE · mode local/);
 for(const request of ['Organise mon planning','Je veux définir mes priorités'])assert.match(await send(request),/^ASTOR · mode local/);
 for(const request of ['Peux-tu vérifier cette affirmation : Un seul test prouve que tout est fiable.','Contrôle cette idée : Tout est certain.','Pourriez-vous vérifier cette idée : Tout est certain.'])assert.match(await send(request),/^ATLAS · mode local/);
 for(const request of ['Passe à Ève','Peux-tu choisir Atlas ?','Parle avec Hadès','Passe sur Orphée'])assert.match(await send(request),/Choisi pour cette session/);
 nodes.enableModuleEVE.checked=false;await nodes.enableModuleEVE.fire('change');assert.match(await send('Passe à Eve'),/EVE est désactivé/);nodes.enableModuleEVE.checked=true;await nodes.enableModuleEVE.fire('change');
 for(const request of ['Ne verrouille pas le hub','Comment verrouiller le hub ?','Je vois une statue','Ce n’est pas une urgence']){await send(request);assert.notEqual(env.location.href,'astor-native://lock',request);}
 assert.match(await send('Au secours'),/^HADES · mode local/);
 assert.match(await send('Astor, aide-moi'),/Vous pouvez dire/);assert.match(await send('Et après ?'),/fonctions du Hub/);

 assert.match(await send('Je veux apprendre l’anglais'),/travaillons l’anglais/);
 assert.match(await send('Je débute'),/garde l’anglais.*bases/);
 assert.match(await send('Donne-moi un exercice'),/Exercice d’anglais.*bases/);
 assert.match(await send('Plus simple'),/Exercice d’anglais.*bases/);
 assert.match(await send('are'),/Correct pour cet exercice/);
 assert.match(await send('Un autre exercice'),/I ___ Alex/);
 assert.match(await send('is'),/pas la réponse attendue/);
 assert.match(await send('Un indice'),/Indice/);
 assert.match(await send('Je ne sais pas'),/Indice/);
 assert.match(await send('Je n’y arrive pas'),/Indice/);
 assert.match(await send('Pourquoi ?'),/verbe to be/);
 assert.match(await send('La correction'),/Réponse attendue : am/);
 assert.match(await send('Plus difficile'),/Exercice d’anglais.*intermédiaire/);
 assert.match(await send('Plus difficile'),/Exercice d’anglais.*défi/);
 assert.match(await send('Plus difficile'),/Exercice d’anglais.*défi/);
 assert.match(await send('Je débute'),/garde l’anglais.*bases/);
 assert.match(await send('Je veux apprendre l’espagnol'),/travaillons l’espagnol/);
 assert.match(await send('Je débute'),/garde l’espagnol/);
 assert.match(await send('Donne-moi un exercice'),/Yo ___ Alex/);
 assert.match(await send('soy'),/Correct pour cet exercice/);
 assert.match(await send('Changeons de sujet'),/effacé/);
 assert.match(await send('Plus simple'),/^ASTOR.*Quel sujet/s);
 assert.match(await send('Je veux apprendre l’anglais'),/anglais/);
 assert.match(await send('Organise mon planning'),/objectif/);
 assert.match(await send('Je débute'),/^ASTOR.*Quel sujet/s);
 nodes.enableModuleEVE.checked=false;await nodes.enableModuleEVE.fire('change');
 assert.match(await send('Je veux apprendre l’anglais'),/EVE est désactivé/);
 nodes.enableModuleEVE.checked=true;await nodes.enableModuleEVE.fire('change');
 assert.match(await send('Donne-moi un exercice'),/^ASTOR.*Quel sujet/s);
 let now=0;const context=win.__makeContext(win.__normalizeInput,()=>now);
 const subject=context.topic('Je veux apprendre l’anglais');context.remember({name:'topic',subject},'Je veux apprendre l’anglais','');
 let next=context.learningIntent('Je débute');assert.equal(next.name,'learningFollow');assert.match(context.learn(next),/anglais/);context.remember(next,'Je débute','');
 now=5*60*1000+1;assert.equal(context.learningIntent('Plus simple').name,'learningMissing');
 context.remember({name:'topic',subject},'anglais','');assert.equal(context.learningIntent('mon mot de passe est secret'),null);assert.equal(context.learningIntent('Plus simple').name,'learningMissing');
 assert.match(await send('Je veux apprendre l’anglais'),/anglais/);
 nodes.passphrase.value='Coffre temporaire 2026';await nodes.unlockMemory.click();
 assert(!nodes.memory.disabled);nodes.memory.value='NOTE CHIFFREE DE TEST';await nodes.saveMemory.click();
 assert(storage.get('astor.v34.vault'));assert(!storage.get('astor.v34.vault').includes('NOTE CHIFFREE'));
 await send('verrouille');assert.equal(nodes.memory.value,'');assert(nodes.memory.disabled);
 assert.equal(nodes.chatlog.children.length,0);assert.equal(env.location.href,'astor-native://lock');
 assert.equal(await send('Plus simple'),undefined);assert.equal(nodes.chatlog.children.length,0);
 assert.equal(network,0);
 console.log('PASS: Hub Android local, routes, relais HADES, coffre AES-GCM, effacement et demande de verrou natif, zéro réseau.');
})().catch(e=>{console.error(e);process.exit(1)});
