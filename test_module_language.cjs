const fs=require('fs'),vm=require('vm'),assert=require('assert'),{webcrypto}=require('crypto');
const html=fs.readFileSync(require('path').join(__dirname,'app/src/main/assets/index.html'),'utf8');
class Node{constructor(){this.value='';this.textContent='';this.checked=false;this.disabled=false;this.style={};this.children=[];this.events={};this.dataset={};this.src='';const values=new Set();this.classList={add:x=>values.add(x),remove:x=>values.delete(x),contains:x=>values.has(x),toggle:(x,b)=>{b??=!values.has(x);b?values.add(x):values.delete(x);return b}}}addEventListener(n,f){(this.events[n]??=[]).push(f)}async fire(n,e={}){for(const f of this.events[n]||[])await f(e)}append(n){this.children.push(n)}replaceChildren(){this.children=[]}setAttribute(){}removeAttribute(){}pause(){}load(){}focus(){}click(){return this.onclick?.()} }
const nodes={};for(const [,id]of html.matchAll(/id="([^"]+)"/g))nodes[id]=new Node();nodes.memory.disabled=true;nodes.aiEndpoint.value='/api/chat';const doc=new Node();doc.body=new Node();doc.getElementById=id=>nodes[id];doc.createElement=()=>new Node();doc.querySelector=()=>new Node();const modules=['ASTOR','EVE','HADES','ATLAS','ORPHEUS','TEMPEST'].map(name=>{const n=new Node();n.dataset.module=name;return n});doc.querySelectorAll=()=>modules;
const win=new Node();win.location={};win.isSecureContext=true;win.crypto=webcrypto;const storage=new Map();let network=0;const env={document:doc,window:win,crypto:webcrypto,localStorage:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k)},TextEncoder,TextDecoder,URL,Event,AbortController,location:{protocol:'https:',origin:'https://appassets.androidplatform.net',href:'https://appassets.androidplatform.net/assets/index.html',hostname:'appassets.androidplatform.net'},setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>1,requestAnimationFrame:f=>f(),confirm:()=>true,fetch:()=>{network++;throw Error('network forbidden')}};win.dispatchEvent=e=>win.fire(e.type);vm.createContext(env);for(const[tag,js]of html.matchAll(/<script[^>]*>(.*?)<\/script>/gs)){if(!tag.includes('id="androidFluidUi"'))vm.runInContext(js.replace("function recognizeIntent(raw){","window.__parse=recognizeIntent;function recognizeIntent(raw){").replace("const shortContext=createShortContext(normalizeInput);","window.__makeContext=createShortContext;window.__normalizeInput=normalizeInput;const shortContext=createShortContext(normalizeInput);"),env);}
const event={preventDefault(){}};const send=async s=>{nodes.message.value=s;await nodes.chatForm.onsubmit(event);return nodes.chatlog.children.at(-1)?.textContent};
(async()=>{
 for(const request of ['Passe-moi Ève','Je veux parler avec Eve','Peux-tu parler avec Ève ?','Appelle Ev','Passe sur Ève'])assert.match(await send(request),/^EVE.*Choisi pour cette session/s,request);
 assert.match(await send('Ève, tu me reçois ?'),/^EVE.*Je t’écoute/s);
 assert.match(await send('Peux-tu demander à Atlas de vérifier cette affirmation : tout est toujours vrai ?'),/^ATLAS.*ATLAS V2/s);
 assert.match(await send('Atlasse, vérifie : aucun test ne peut échouer'),/^ATLAS.*ATLAS V2/s);
 assert.match(await send('Tempeste, analyse : une seule solution existe'),/^TEMPEST.*Grille de contradiction/s);
 assert.match(await send('Passe-moi Hadès'),/^HADES.*Choisi pour cette session/s);
 assert.match(await send('Hadès, tu me reçois ?'),/^HADES.*Je te reçois/s);
 assert.match(await send('Orphée, bonjour'),/^ORPHEUS.*Je t’écoute/s);
 assert.match(await send('Eve, qui es-tu ?'),/^EVE.*EVE : Pédagogie/s);
 assert.match(await send('Astor, demande à Atlas de vérifier : tout est vrai'),/^ATLAS.*ATLAS V2/s);
 assert.match(await send('Eve, Atlas vérifie cette phrase'),/À quel module/s);
 assert.match(await send('Eve, analyse : tout est vrai'),/EVE.*vérification critique/s);
 await send('Je veux apprendre anglais');await send('Passe Atlas');assert.doesNotMatch(await send('Je débute'),/garde l’anglais/);
 const parse=win.__parse;
 for(const input of ['Ne passe pas à Eve','Je parle d’Atlas dans mon livre','Atlas est un personnage','Je voudrais désactiver Hades','Le nom Eve me plaît'])assert.notEqual(parse(input).name,'module',input);
 assert.equal(parse('Atlas est un personnage').addressedModule,undefined);
 for(const input of ['Eve, ne verrouille pas le hub','Atlas, ce n’est pas une urgence']){const intent=parse(input);assert(!['lock','emergency'].includes(intent.name),input);}
 nodes.enableModuleATLAS.checked=false;await nodes.enableModuleATLAS.fire('change');assert.match(await send('Atlas, vérifie : tout est vrai'),/ATLAS est désactivé/);
 nodes.enableModuleATLAS.checked=true;await nodes.enableModuleATLAS.fire('change');
 nodes.enableModuleHADES.checked=false;await nodes.enableModuleHADES.fire('change');assert.match(await send('Eve, au secours'),/Hades est désactivé/);
 nodes.enableModuleHADES.checked=true;await nodes.enableModuleHADES.fire('change');
 for(const request of ['Morpheus','Ouvre Morphée','Je veux noter ma nuit']){assert.match(await send(request),/MORPHÉE : ouverture demandée/);assert.equal(env.location.href,'astor-native://morphee');}
 assert.notEqual(parse('Ne ferme pas Morphée').name,'morphee');
 assert.equal(network,0);assert.equal(storage.size,0);
 console.log('PASS: module addresses, accents, aliases, delegation, disabled modules, negations, mentions and Morphee commands');
})().catch(e=>{console.error(e);process.exit(1)});
