const fs=require('fs'),path=require('path');
let harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
harness=harness.replace('vm.createContext(env);',"const calls=[];let deferred=false;win.AstorLocalNative={state(){},download(){},cancel(){},generate(id,prompt){calls.push({id,prompt});if(!deferred)win.ASTOR_LOCAL_AI.result(id,'Réponse simulée pour le test du pont natif.','');}};vm.createContext(env);");
new Function('require','__dirname',harness+String.raw`
(async()=>{
 win.ASTOR_LOCAL_AI.state('Modèle conservé, moteur indisponible',false,true);assert(nodes.downloadLocalModel.disabled);assert(!win.ASTOR_LOCAL_AI.ready);assert(win.ASTOR_LOCAL_AI.installed);win.ASTOR_LOCAL_AI.state('Léger disponible',true,true,false,true,true);assert(!nodes.downloadLocalModel.disabled);assert(nodes.useLightLocalModel.disabled);assert(nodes.useLightLocalModel.checked);win.ASTOR_LOCAL_AI.state('Deux modèles disponibles',true,true,true,true,false);assert(nodes.downloadLocalModel.disabled);assert(!nodes.useLightLocalModel.disabled);assert(!nodes.useLightLocalModel.checked);win.ASTOR_LOCAL_AI.state('Prêt',true);assert(nodes.enableLocalAI.checked);assert(nodes.downloadLocalModel.disabled);assert.match(nodes.welcomeMessage.textContent,/disponible pour discuter/);assert.doesNotMatch(nodes.welcomeMessage.textContent,/Téléchargez/);
 assert.match(await send('Explique les trous noirs simplement'),/IA locale.*Réponse simulée/s);assert.equal(calls.length,1);assert.match(calls[0].prompt,/You are ASTOR/);
 assert.match(await send('Quelle est la devise de Tempest ?'),/Ne crains pas la tempête/);assert.equal(calls.length,1);
 await send('statut');await send('urgence');assert.equal(calls.length,1);
 nodes.passphrase.value='Phrase fictive IA 2026';await nodes.unlockMemory.click();assert(!nodes.usePersonalForAI.disabled);assert(!nodes.usePersonalForAI.checked);
 await send('Retiens que mon projet s’appelle PROJET_CONFIDENTIEL');await send('Confirme le souvenir');
 await send('Décris un volcan');assert(!calls.at(-1).prompt.includes('PROJET_CONFIDENTIEL'));
 nodes.usePersonalForAI.checked=true;await send('Décris une comète');assert(calls.at(-1).prompt.includes('PROJET_CONFIDENTIEL'));
 nodes.usePersonalForAI.checked=false;await send('Décris une planète');assert(!calls.at(-1).prompt.includes('PROJET_CONFIDENTIEL'));
 const before=calls.length;await send('Quel est le nom de mon projet ?');assert.equal(calls.length,before);
 await send('Retiens que mon code PIN est 1234');assert.equal(calls.length,before);
 const stored=storage.get('astor.v34.mnemosyne');await send('Parle-moi du ciel');assert.equal(storage.get('astor.v34.mnemosyne'),stored);
 nodes.enableLocalAI.checked=false;assert.doesNotMatch(await send('Décris une galaxie'),/Réponse simulée/);assert.equal(calls.length,before+1);
 nodes.enableLocalAI.checked=true;deferred=true;const waiting=send('Parle-moi des étoiles');assert(nodes.sendMessage.disabled);nodes.lockMemory.click();await waiting;assert.equal(nodes.chatlog.children.length,0);assert(!nodes.usePersonalForAI.checked);assert(nodes.usePersonalForAI.disabled);
 const stale=calls.at(-1);win.ASTOR_LOCAL_AI.result(stale.id,'Réponse périmée','');assert.equal(nodes.chatlog.children.length,0);
 assert.equal(network,0);console.log('PASS: routage IA locale, commandes prioritaires, coffre inchangé, souvenirs uniquement sur opt-in, contexte privé non réutilisé, annulation et réponses périmées.');
})().catch(e=>{console.error(e);process.exit(1)});
`)(require,__dirname);
