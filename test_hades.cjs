const fs=require('fs'),vm=require('vm'),assert=require('assert'),{webcrypto}=require('crypto');
const html=fs.readFileSync(require('path').join(__dirname,'app/src/main/assets/index.html'),'utf8');
class Node{constructor(){this.value='';this.textContent='';this.checked=false;this.disabled=false;this.style={};this.children=[];this.events={};this.dataset={};this.src='';const values=new Set();this.classList={add:x=>values.add(x),remove:x=>values.delete(x),contains:x=>values.has(x),toggle:(x,b)=>{b??=!values.has(x);b?values.add(x):values.delete(x);return b}}}addEventListener(n,f){(this.events[n]??=[]).push(f)}async fire(n,e={}){for(const f of this.events[n]||[])await f(e)}append(n){this.children.push(n)}replaceChildren(){this.children=[]}setAttribute(){}removeAttribute(){}pause(){}load(){}focus(){}click(){return this.onclick?.()} }
const nodes={};for(const [,id]of html.matchAll(/id="([^"]+)"/g))nodes[id]=new Node();nodes.hadesAlert.hidden=true;nodes.memory.disabled=true;nodes.aiEndpoint.value='/api/chat';const doc=new Node();doc.body=new Node();doc.getElementById=id=>nodes[id];doc.createElement=()=>new Node();doc.querySelector=()=>new Node();const modules=['ASTOR','EVE','HADES','ATLAS','ORPHEUS','TEMPEST'].map(name=>{const n=new Node();n.dataset.module=name;return n});doc.querySelectorAll=()=>modules;
const win=new Node();win.location={};win.isSecureContext=true;win.crypto=webcrypto;const storage=new Map();let network=0;const env={document:doc,window:win,crypto:webcrypto,localStorage:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k)},TextEncoder,TextDecoder,URL,Event,AbortController,location:{protocol:'https:',origin:'https://appassets.androidplatform.net',href:'https://appassets.androidplatform.net/assets/index.html',hostname:'appassets.androidplatform.net'},setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>1,requestAnimationFrame:f=>f(),confirm:()=>true,fetch:()=>{network++;throw Error('network forbidden')}};win.dispatchEvent=e=>win.fire(e.type);vm.createContext(env);for(const[tag,js]of html.matchAll(/<script[^>]*>(.*?)<\/script>/gs)){if(!tag.includes('id="androidFluidUi"'))vm.runInContext(js.replace("const shortContext=createShortContext(normalizeInput);","window.__makeContext=createShortContext;window.__normalizeInput=normalizeInput;const shortContext=createShortContext(normalizeInput);"),env);}
const event={preventDefault(){}};const send=async s=>{nodes.message.value=s;await nodes.chatForm.onsubmit(event);return nodes.chatlog.children.at(-1)?.textContent};
(async()=>{
 await send('urgence');assert.equal(nodes.hadesAlert.hidden,false);assert.equal(nodes.hadesAlert.dataset.level,'clarify');
 assert.equal(nodes.hadesBadge.textContent,'NIVEAU À CONFIRMER');
 await nodes.hadesCritical.click();assert.equal(nodes.hadesAlert.dataset.level,'critical');assert.equal(nodes.hadesDial.hidden,false);
 await nodes.hadesUrgent.click();assert.equal(nodes.hadesAlert.dataset.level,'urgent');
 await nodes.hadesVigilance.click();assert.equal(nodes.hadesDial.hidden,true);
 await nodes.hadesTest.click();assert.match(nodes.hadesBadge.textContent,/TEST LOCAL/);assert.equal(nodes.hadesDial.hidden,true);
 await nodes.hadesCritical.click();assert.match(nodes.hadesBadge.textContent,/TEST LOCAL/);assert.equal(nodes.hadesDial.hidden,true);
 let prevented=false;nodes.hadesDial.onclick({preventDefault(){prevented=true}});assert(prevented);
 nodes.nativeSpeechMute={checked:true};const before=env.location.href;await nodes.hadesRead.click();assert.equal(env.location.href,before);assert.match(nodes.hadesFeedback.textContent,/muet/);
 nodes.nativeSpeechMute.checked=false;await nodes.hadesRead.click();assert.match(env.location.href,/^astor-native:\/\/tts-speak\?text=/);
 await nodes.hadesClose.click();assert.equal(nodes.hadesAlert.hidden,true);assert.equal(nodes.hadesTitle.textContent,'');
 for(const request of ['Ce n’est pas une urgence','Ne lance pas une urgence','Comment fonctionne une alerte ?']){await send(request);assert.equal(nodes.hadesAlert.hidden,true,request);}
 await send('alerte hades');assert.equal(nodes.hadesAlert.hidden,false);
 nodes.enableModuleHADES.checked=false;await nodes.enableModuleHADES.fire('change');assert.equal(nodes.hadesAlert.hidden,true);assert.equal(nodes.hadesOpen.disabled,true);
 assert.equal(win.ASTOR_HADES.show('critical'),false);await send('urgence');assert.equal(nodes.hadesAlert.hidden,true);
 nodes.enableModuleHADES.checked=true;await nodes.enableModuleHADES.fire('change');await nodes.hadesOpen.click();assert.equal(nodes.hadesAlert.hidden,false);
 assert.equal(win.ASTOR_HADES.show('unknown'),false);
 await win.dispatchEvent(new Event('astor-lock-request')); assert.equal(nodes.hadesAlert.hidden,true);assert.equal(win.ASTOR_HADES.show('critical'),false);
 assert.equal(network,0);assert.equal(storage.size,0);
 const native=fs.readFileSync(require('path').join(__dirname,'app/src/main/java/fr/astor/hades/MainActivity.java'),'utf8');
 assert.match(native,/hades-dial112[\s\S]{0,220}request\.hasGesture\(\)/);
 assert.match(native,/Intent\.ACTION_DIAL, Uri\.parse\("tel:112"\)/);
 const manifest=fs.readFileSync(require('path').join(__dirname,'app/src/main/AndroidManifest.xml'),'utf8');assert(!manifest.includes('android.permission.CALL_PHONE'));
 console.log('PASS: Hades levels, test mode, negations, module disabled, mute, background lock, zero network/storage and dial-only route');
})().catch(e=>{console.error(e);process.exit(1)});
