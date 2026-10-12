const fs=require('fs'),path=require('path');
let harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
harness=harness.replace('vm.createContext(env);', "delete nodes['mnemosyne-status'];const mounted=[];Node.prototype.appendChild=function(n){mounted.push(n);this.append(n)};vm.createContext(env);");
new Function('require','__dirname',harness+String.raw`
(async()=>{
 const m=win.ASTOR_MNEMOSYNE;assert.equal(m.count,12);assert.equal(m.version,'3.9.4.1');
 assert.equal(m.search('Atlas')[0].id,'atlas.role');assert.equal(m.search('sécurité').length,m.search('securite').length);
 const copy=m.get('atlas.role');copy.content='changed';assert.notEqual(m.get('atlas.role').content,'changed');assert.equal(m.get('absent'),null);
 assert(mounted.some(n=>n.id==='mnemosyne-status'));
 assert.match(await send('Qui est Atlas ?'),/MNÉMOSYNE.*Atlas/s);
 assert.match(await send('Et ensuite ?'),/MNÉMOSYNE.*Contexte/s);
 await send('Changeons de sujet');assert.doesNotMatch(await send('Et ensuite ?'),/MNÉMOSYNE.*Contexte/s);
 assert.match(await send('Classe cette information : mon code PIN est 1234'),/PROTÉGÉE/);
 assert.match(await send('Mémorise mon projet de jardin'),/CONFIRMATION REQUISE/);assert.equal(storage.size,0);
 assert.match(await send('Quel est ton niveau de sécurité ?'),/biométrie forte Android/);
 await send('Qui est Atlas ?');await send('verrouille');doc.body.classList.remove('astor-locked');assert.doesNotMatch(await send('Et ensuite ?'),/MNÉMOSYNE.*Contexte/s);
 assert.equal(network,0);console.log('PASS: Mnémosyne, panneau, recherche, routage, oubli du contexte, classification protégée et absence de conservation silencieuse/réseau.');
})().catch(e=>{console.error(e);process.exit(1)});
`)(require,__dirname);
