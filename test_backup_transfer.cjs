const fs=require('fs'),path=require('path');
let harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
harness=harness.replace('const storage=new Map();','const storage=sharedStorage;');
const create=new Function('require','__dirname','sharedStorage',harness+`return {nodes,env,win,storage,send,assert,network:()=>network};`);
const PASS='Phrase sauvegarde tests 2026',VAULT='astor.v34.vault',MEM='astor.v34.mnemosyne',JOURNAL='astor.v34.restore-journal';
(async()=>{
 const source=create(require,__dirname,new Map()),assert=source.assert;
 source.nodes.passphrase.value=PASS;await source.nodes.unlockMemory.click();source.nodes.memory.value='NOTE À TRANSFÉRER';await source.nodes.saveMemory.click();
 await source.send('Retiens que mon projet s’appelle TRANSFERT');await source.send('Confirme le souvenir');
 source.nodes.exportBackup.click();const raw=source.nodes.backupText.value;
 assert(raw.includes('astor.encrypted-backup.v1'));assert(!raw.includes('NOTE À TRANSFÉRER'));assert(!raw.includes('TRANSFERT'));assert(!raw.includes(PASS));
 const target=create(require,__dirname,new Map());
 const restore=async(e,b=raw,pass=PASS)=>{e.nodes.backupText.value=b;e.nodes.backupPassphrase.value=pass;await e.nodes.restoreBackup.click();assert.equal(e.nodes.backupPassphrase.value,'');};
 await restore(target,raw,'Phrase incorrecte tests 2026');assert(!target.storage.has(VAULT));assert(!target.storage.has(MEM));
 await restore(target,'{}');assert(!target.storage.has(VAULT));
 const b=JSON.parse(raw),m=JSON.parse(b.memories);m.ciphertext[0]^=1;b.memories=JSON.stringify(m);
 await restore(target,JSON.stringify(b));assert(!target.storage.has(VAULT));
 target.env.confirm=()=>false;await restore(target);assert(!target.storage.has(VAULT));target.env.confirm=()=>true;
 await restore(target);assert.equal(target.storage.get(VAULT),source.storage.get(VAULT));assert.equal(target.storage.get(MEM),source.storage.get(MEM));assert(!target.storage.has(JOURNAL));
 target.nodes.passphrase.value=PASS;await target.nodes.unlockMemory.click();assert.equal(target.nodes.memory.value,'NOTE À TRANSFÉRER');assert.match(await target.send('Quel est le nom de mon projet ?'),/TRANSFERT/);
 target.nodes.lockMemory.click();const before=target.storage.get(VAULT);await restore(target);assert.equal(target.storage.get(VAULT),before);assert.match(target.nodes.backupStatus.textContent,/application vide/);
 // A failed second write keeps a recoverable encrypted journal; reopening completes it.
 const partial=create(require,__dirname,new Map()),set=partial.env.localStorage.setItem;
 partial.env.localStorage.setItem=(k,v)=>{if(k===MEM)throw Error('quota');set(k,v);};
 await restore(partial);assert(partial.storage.has(JOURNAL));assert(partial.storage.has(VAULT));assert(!partial.storage.has(MEM));partial.env.localStorage.setItem=set;
 const reopened=create(require,__dirname,partial.storage);reopened.nodes.passphrase.value=PASS;await reopened.nodes.unlockMemory.click();assert.equal(reopened.nodes.memory.value,'NOTE À TRANSFÉRER');assert.equal(reopened.storage.get(MEM),source.storage.get(MEM));assert(!reopened.storage.has(JOURNAL));
 // A conflict is never overwritten by journal recovery.
 const conflictStore=new Map([[JOURNAL,raw],[VAULT,'OTHER']]),conflict=create(require,__dirname,conflictStore);conflict.nodes.passphrase.value=PASS;await conflict.nodes.unlockMemory.click();assert.equal(conflictStore.get(VAULT),'OTHER');assert(conflictStore.has(JOURNAL));
 conflict.nodes.clearMemory.click();assert(!conflictStore.has(JOURNAL));assert(!conflictStore.has(VAULT));
 // Lock while deriving the source key must cancel restoration.
 const cancel=create(require,__dirname,new Map()),subtle=cancel.env.crypto.subtle,derive=subtle.deriveKey;let release,entered;
 const gate=new Promise(r=>entered=r),held=new Promise(r=>release=r);subtle.deriveKey=async(...args)=>{entered();await held;return derive.apply(subtle,args);};
 try{const pending=restore(cancel);await gate;cancel.nodes.lockMemory.click();release();await pending;assert(!cancel.storage.has(VAULT));assert(!cancel.storage.has(JOURNAL));}finally{release();subtle.deriveKey=derive;}
 assert.equal(source.network()+target.network()+partial.network()+reopened.network()+conflict.network()+cancel.network(),0);
 console.log('PASS: encrypted transfer, notes and memories restored, wrong password, invalid data, cancellation, existing data protected, partial-write recovery and conflicts.');
})().catch(e=>{console.error(e);process.exit(1)});
