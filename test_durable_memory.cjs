const fs=require('fs'),path=require('path');
let harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
harness=harness.replace('const storage=new Map();','const storage=sharedStorage;');
const createEnv=new Function('require','__dirname','sharedStorage',harness+String.raw`
env.localStorage.getItem=k=>storage.has(k)?storage.get(k):null;
return {nodes,doc,win,env,storage,send,webcrypto,assert,network:()=>network};
`);
const store=new Map(),a=createEnv(require,__dirname,store),{assert,webcrypto}=a;
const VAULT='astor.v34.vault',MEM='astor.v34.mnemosyne',PASS='Phrase tests Mnemosyne 2026';
const unlock=async e=>{e.nodes.passphrase.value=PASS;await e.nodes.unlockMemory.click();assert(!e.nodes.memory.disabled,e.nodes.memoryStatus.textContent);};
const barrier=method=>{const original=webcrypto.subtle[method];let arrived,release;const entered=new Promise(r=>arrived=r),held=new Promise(r=>release=r);webcrypto.subtle[method]=async(...args)=>{arrived();await held;return original.apply(webcrypto.subtle,args);};return {entered,release,restore(){webcrypto.subtle[method]=original;}};};
(async()=>{
 assert.match(await a.send('Retiens que mon projet s’appelle ASTOR'),/Déverrouillez le coffre/);assert(!store.has(MEM));
 await unlock(a);a.nodes.memory.value='ANCIENNE NOTE À CONSERVER';await a.nodes.saveMemory.click();const notes=store.get(VAULT);
 assert.match(await a.send('Retiens que mon projet s’appelle ASTOR'),/CONFIRMATION REQUISE.*mon projet s’appelle ASTOR/s);assert(!store.has(MEM));assert(!a.nodes.memoryConfirmation.hidden);
 assert.match(await a.send('Annule'),/annulée/);assert(!store.has(MEM));assert(a.nodes.memoryConfirmation.hidden);
 await a.send('Retiens que mon projet s’appelle ASTOR');assert.match(await a.send('Confirme le souvenir'),/confirmé et enregistré/);
 assert(store.has(MEM));assert(!store.get(MEM).includes('ASTOR'));assert.equal(store.get(VAULT),notes);
 assert.match(await a.send('Comment s’appelle mon projet ?'),/Souvenir personnel 1.*mon projet s’appelle ASTOR/s);
 assert.match(await a.send('Quel est le nom de mon projet ?'),/mon projet s’appelle ASTOR/);
 assert.match(await a.send('Liste mes souvenirs'),/Souvenir personnel 1/);
 assert.match(await a.send('Retiens que mon projet s’appelle ASTOR'),/déjà enregistré/);
 const count=JSON.parse(store.get(MEM)).ciphertext.length;
 assert.match(await a.send('Confirme le souvenir'),/Aucun souvenir en attente/);assert.equal(JSON.parse(store.get(MEM)).ciphertext.length,count);
 // New JavaScript environment with the same storage proves persistence across reload.
 a.nodes.lockMemory.click();const b=createEnv(require,__dirname,store);
 assert.match(await b.send('Quel est le nom de mon projet ?'),/Déverrouillez le coffre/);assert.equal(b.nodes.personalMemoryList.children.length,0);
 await unlock(b);assert.equal(b.nodes.memory.value,'ANCIENNE NOTE À CONSERVER');assert.match(await b.send('Comment s’appelle mon projet ?'),/mon projet s’appelle ASTOR/);
 assert.match(await b.send('Corrige le souvenir 1 : mon projet s’appelle ASTOR V2'),/CONFIRMATION REQUISE/);
 const before=store.get(MEM);await b.send('Annule');assert.equal(store.get(MEM),before);
 await b.send('Corrige le souvenir 1 : mon projet s’appelle ASTOR V2');await b.nodes.confirmMemory.click();assert.match(await b.send('Quel est le nom de mon projet ?'),/ASTOR V2/);
 await b.send('Retiens que mon livre préféré est Dune');await b.send('Confirme le souvenir');assert.match(await b.send('Cherche dans mes souvenirs : Dune'),/livre préféré est Dune/);
 assert.match(await b.send('Supprime le souvenir 1'),/CONFIRMATION REQUISE/);await b.send('Confirme le souvenir');assert.doesNotMatch(await b.send('Liste mes souvenirs'),/ASTOR V2/);
 b.nodes.lockMemory.click();await unlock(b);assert.doesNotMatch(await b.send('Liste mes souvenirs'),/ASTOR V2/);assert.match(await b.send('Quel est le nom de mon projet ?'),/n’est pas encore enregistré/);
 assert.equal(b.nodes.memory.value,'ANCIENNE NOTE À CONSERVER');assert.equal(store.get(VAULT),notes);
 assert.match(await b.send('Retiens que mon code PIN est 1234'),/Information sensible/);
 assert.match(await b.send('Corrige le souvenir 99 : une valeur'),/n’existe pas/);
 assert.match(await b.send('Retiens que '+ 'x'.repeat(1001)),/1000 caractères/);
 // A failed write does not lose or mutate an existing memory.
 const valid=store.get(MEM),set=b.env.localStorage.setItem;
 await b.send('Retiens que une information de test');b.env.localStorage.setItem=()=>{throw Error('quota');};
 assert.match(await b.send('Confirme le souvenir'),/Échec/);assert.equal(store.get(MEM),valid);b.env.localStorage.setItem=set;
 // Lock during encryption cancels the write and erases pending/decrypted content.
 await b.send('Retiens que une seconde information');let gate=barrier('encrypt');
 try{const saving=b.send('Confirme le souvenir');await gate.entered;b.nodes.lockMemory.click();gate.release();await saving;assert.equal(store.get(MEM),valid);assert(b.nodes.memoryConfirmation.hidden);assert.equal(b.nodes.memoryPreview.textContent,'');assert.equal(b.nodes.personalMemoryList.children.length,0);assert.equal(b.nodes.chatlog.children.length,0);}finally{gate.release();gate.restore();}
 await unlock(b);await b.send('Retiens que une troisième information');gate=barrier('encrypt');
 try{const saving=b.send('Confirme le souvenir');await gate.entered;b.nodes.cancelMemory.click();gate.release();await saving;assert.equal(store.get(MEM),valid);}finally{gate.release();gate.restore();}
 // Double confirmation adds one memory only.
 await b.send('Retiens que un souvenir unique');gate=barrier('encrypt');
 try{const saving=b.send('Confirme le souvenir');await gate.entered;assert.match(await b.send('Confirme le souvenir'),/en cours/);gate.release();await saving;assert.equal(b.nodes.personalMemoryList.children.length,2);}finally{gate.release();gate.restore();}
 // External changes are never overwritten; corrupted encrypted memory preserves both stores.
 const after=store.get(MEM);await b.send('Retiens que un autre souvenir');store.set(MEM,'EXTERNAL');assert.match(await b.send('Confirme le souvenir'),/changé/);assert.equal(store.get(MEM),'EXTERNAL');
 b.nodes.lockMemory.click();b.nodes.passphrase.value=PASS;await b.nodes.unlockMemory.click();assert(b.nodes.memory.disabled);assert.equal(store.get(VAULT),notes);assert.equal(store.get(MEM),'EXTERNAL');
 store.set(MEM,after);await unlock(b);await b.send('Retiens que un dernier souvenir');await b.send('statut');assert(b.nodes.memoryConfirmation.hidden);
 // Confirmations cannot survive leaving the app or deleting the vault.
 await b.send('Retiens que jamais conservé');await b.win.fire('pagehide');assert(b.nodes.memoryConfirmation.hidden);assert(b.nodes.memory.disabled);
 await unlock(b);await b.send('Retiens que jamais ressuscité');gate=barrier('encrypt');
 try{const saving=b.send('Confirme le souvenir');await gate.entered;b.nodes.clearMemory.click();gate.release();await saving;assert(!store.has(MEM));assert(!store.has(VAULT));}finally{gate.release();gate.restore();}
 assert.equal(a.network()+b.network(),0);
 console.log('PASS: mémoire durable chiffrée, confirmation, annulation, rappel après réouverture, correction, suppression, notes préservées, interruptions, conflits, absence de réseau.');
})().catch(e=>{console.error(e);process.exit(1)});
