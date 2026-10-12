// Uses the same local DOM/storage harness as the existing conversation tests.
const fs=require('fs'),path=require('path');
const harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
new Function('require','__dirname',harness+String.raw`
env.localStorage.getItem=k=>storage.has(k)?storage.get(k):null;
const STORE='astor.v34.vault',OLD='astor.v34.memory',PASS='Phrase fictive tests 2026';
const status=()=>nodes.memoryStatus.textContent;
const closed=()=>{assert(nodes.memory.disabled);assert.equal(nodes.memory.value,'');assert.equal(nodes.passphrase.value,'')};
const unlock=async(pass=PASS)=>{nodes.passphrase.value=pass;await nodes.unlockMemory.click()};
const reset=()=>{nodes.lockMemory.click();storage.clear();doc.body.classList.remove('astor-locked')};
async function create(notes='NOTE FICTIVE PRIVEE'){
 await unlock();assert(!nodes.memory.disabled);nodes.memory.value=notes;await nodes.saveMemory.click();
 const raw=storage.get(STORE);assert(raw);assert(!raw.includes(notes));nodes.lockMemory.click();return raw;
}
function barrier(method){
 const original=webcrypto.subtle[method];let arrived,release;
 const entered=new Promise(r=>arrived=r),held=new Promise(r=>release=r);
 webcrypto.subtle[method]=async(...args)=>{arrived();await held;return original.apply(webcrypto.subtle,args)};
 return {entered,release,restore(){webcrypto.subtle[method]=original}};
}
async function interrupt(method,action,existing=true){
 reset();if(existing)await create();
 const original=storage.get(STORE);const gate=barrier(method);
 try{
  nodes.passphrase.value=PASS;const opening=nodes.unlockMemory.click();await gate.entered;
  assert(nodes.unlockMemory.disabled);assert(nodes.saveMemory.disabled);closed();
  action();gate.release();await opening;closed();
  return original;
 }finally{gate.release();gate.restore()}
}
(async()=>{
 // Parsing/storage errors must never become a fresh empty vault.
 const valid=JSON.parse(await create());reset();
 const badRecords=['{corrupted','', 'null','[]',JSON.stringify({...valid,version:2}),JSON.stringify({...valid,kdf:'other'}),JSON.stringify({...valid,iterations:1}),JSON.stringify({...valid,salt:[1]}),JSON.stringify({...valid,iv:[1]}),JSON.stringify({...valid,ciphertext:[1]}),JSON.stringify({...valid,salt:Array(16).fill(300)})];
 for(const raw of badRecords){
  reset();storage.set(STORE,raw);storage.set(OLD,'LEGACY FICTIVE');await unlock();
  closed();assert.equal(storage.get(STORE),raw);assert.equal(storage.get(OLD),'LEGACY FICTIVE');assert.match(status(),/conservées/);
 }
 reset();const get=env.localStorage.getItem;env.localStorage.getItem=()=>{throw Error('storage denied')};
 await unlock();closed();assert(!storage.has(STORE));assert.match(status(),/Stockage inaccessible/);env.localStorage.getItem=get;
 reset();env.localStorage.getItem=k=>{if(k===OLD)throw Error('legacy storage denied');return get(k)};
 await unlock();closed();assert(!storage.has(STORE));env.localStorage.getItem=get;
 // Existing format remains compatible; opening/wrong password never rewrites it.
 reset();const raw=await create();await unlock('Mauvaise phrase tests 2026');closed();assert.equal(storage.get(STORE),raw);
 await unlock();assert.equal(nodes.memory.value,'NOTE FICTIVE PRIVEE');assert.equal(storage.get(STORE),raw);
 // Migration preserves legacy notes until encryption has succeeded.
 reset();storage.set(OLD,'ANCIENNES NOTES FICTIVES');await unlock();assert.equal(nodes.memory.value,'ANCIENNES NOTES FICTIVES');assert(!storage.has(OLD));
 nodes.lockMemory.click();await unlock();assert.equal(nodes.memory.value,'ANCIENNES NOTES FICTIVES');
 reset();storage.set(OLD,'LEGACY FICTIVE');env.confirm=()=>false;await unlock();closed();assert.equal(storage.get(OLD),'LEGACY FICTIVE');assert(!storage.has(STORE));env.confirm=()=>true;
 // Lock before/after each expensive asynchronous stage.
 for(const method of ['deriveKey','decrypt']){
  const before=await interrupt(method,()=>nodes.lockMemory.click());assert.equal(storage.get(STORE),before);
 }
 await interrupt('encrypt',()=>nodes.lockMemory.click(),false);assert(!storage.has(STORE));
 await interrupt('decrypt',()=>{doc.hidden=true;doc.fire('visibilitychange');doc.hidden=false});
 await interrupt('decrypt',()=>send('verrouille'));assert(doc.body.classList.contains('astor-locked'));
 // Deleting during unlock, migration, or save must not resurrect notes.
 await interrupt('decrypt',()=>nodes.clearMemory.click());assert(!storage.has(STORE));
 reset();storage.set(OLD,'LEGACY FICTIVE');let gate=barrier('encrypt');
 try{nodes.passphrase.value=PASS;const p=nodes.unlockMemory.click();await gate.entered;nodes.clearMemory.click();gate.release();await p;closed();assert(!storage.has(STORE));assert(!storage.has(OLD))}finally{gate.release();gate.restore()}
 for(const action of ['lock','delete']){
  reset();const before=await create();await unlock();nodes.memory.value='CHANGEMENT FICTIF';gate=barrier('encrypt');
  try{const p=nodes.saveMemory.click();await gate.entered;nodes[action==='lock'?'lockMemory':'clearMemory'].click();gate.release();await p;closed();assert.equal(storage.get(STORE),action==='lock'?before:undefined)}finally{gate.release();gate.restore()}
 }
 // A second unlock is ignored while busy; a fresh session survives an old callback.
 reset();await create();gate=barrier('decrypt');
 try{
  nodes.passphrase.value=PASS;const old=nodes.unlockMemory.click();await gate.entered;
  nodes.passphrase.value='Mauvaise phrase tests 2026';await nodes.unlockMemory.click();assert(nodes.memory.disabled);
  nodes.lockMemory.click();gate.restore();await unlock();assert(!nodes.memory.disabled);
  const before=storage.get(STORE);gate.release();await old;assert(!nodes.memory.disabled);assert.equal(nodes.memory.value,'NOTE FICTIVE PRIVEE');assert.equal(storage.get(STORE),before);
 }finally{gate.release();gate.restore()}
 // External storage change while decrypting/encrypting is preserved.
 reset();await create();gate=barrier('decrypt');
 try{nodes.passphrase.value=PASS;const p=nodes.unlockMemory.click();await gate.entered;storage.set(STORE,'CHANGED EXTERNALLY');gate.release();await p;closed();assert.equal(storage.get(STORE),'CHANGED EXTERNALLY');assert.match(status(),/changé/)}finally{gate.release();gate.restore()}
 reset();await create();await unlock();gate=barrier('encrypt');
 try{const p=nodes.saveMemory.click();await gate.entered;storage.set(STORE,'CHANGED EXTERNALLY');gate.release();await p;assert.equal(storage.get(STORE),'CHANGED EXTERNALLY');assert.match(status(),/changé/)}finally{gate.release();gate.restore()}
 // Failed writes preserve existing data and editable draft.
 reset();await create();await unlock();const before=storage.get(STORE),set=env.localStorage.setItem;nodes.memory.value='BROUILLON FICTIF';env.localStorage.setItem=()=>{throw Error('quota')};await nodes.saveMemory.click();assert.equal(storage.get(STORE),before);assert.equal(nodes.memory.value,'BROUILLON FICTIF');assert.match(status(),/Échec/);env.localStorage.setItem=set;
 assert.equal(network,0);
 console.log('PASS: coffres invalides conservés, format existant, migration, mauvais mot de passe, interruptions, suppression, doubles clics, callbacks périmés, conflits et erreurs de stockage.');
})().catch(e=>{console.error(e);process.exit(1)});
`)(require,__dirname);
