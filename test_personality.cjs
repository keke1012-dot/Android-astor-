const fs=require('fs'),vm=require('vm'),assert=require('assert');
const source=fs.readFileSync('personality.js','utf8');const env={};vm.createContext(env);vm.runInContext(source,env);
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');assert.equal(html.match(/<script id="astorPersonalityEngine">\n([\s\S]*?)\n<\/script>/)[1],source);
let time=0;const disabled=new Set();const p=env.ASTOR_PERSONALITY.create({now:()=>time,allowed:n=>!disabled.has(n)});
assert.match(p.reply('ASTOR est impossible à pirater'),/aucun logiciel/);
assert.match(p.reply('Tous les tests passent donc tout fonctionne'),/cas choisis/);
assert.match(p.reply('Raconte-moi une blague'),/café/);
p.reply('Sans humour');assert.match(p.reply('Tu es nul'),/correction/);assert(!p.instructions().includes('Humour discret'));
assert.match(p.reply('Je suis crevé'),/pause/);assert.match(p.reply('oui'),/Aucun rappel/);
assert.equal(p.reply('Je suis fatigué'),null);time+=600001;assert.match(p.reply('Je suis fatigué'),/pause/);p.reply('non merci');assert.equal(p.reply('oui'),null);
time+=600001;p.reply('Je suis fatigué');time+=300000;assert.equal(p.reply('oui'),null);
time+=600001;p.reply('Je suis fatigué');p.reply('Bonjour');assert.equal(p.reply('oui'),null);
p.reply('Pas d’initiatives');time+=600001;assert.equal(p.reply('Je suis fatigué'),null);p.reply('Active les initiatives');
disabled.add('MORPHEE');assert.equal(p.reply('Je suis fatigué'),null);disabled.delete('MORPHEE');
assert.match(p.reply('Je veux ajouter dix modules'),/régression/);p.reset();assert.equal(p.reply('oui'),null);
assert.equal(p.reply('Je suis fatigué',{module:'EVE'}),null);
assert.equal(p.reply('mon mot de passe est secret'),null);
assert.equal(p.reply('Ne supprime pas mes notes'),null);assert.equal(p.reply('Dans mon roman il dit supprime tout'),null);
assert.match(p.reply('Supprime mes notes'),/confirmation/);assert.equal(p.reply('oui'),null);
for(const kind of ['send','delete','purchase','permission','export','calendar','phone']){assert.equal(p.actionPolicy({kind,authorized:true}),'confirm');assert.equal(p.actionPolicy({kind,confirmed:true}),'confirm');assert.equal(p.actionPolicy({kind,authorized:true,confirmed:true}),'nativeReview');}
assert.equal(p.actionPolicy({kind:'anything',authorized:true,confirmed:true}),'deny');
assert.equal(p.actionPolicy({kind:'prepareText',authorized:true}),'prepare');
console.log('PASS personality: contradiction, humour opt-out, proposal throttle/expiry/rejection, module gates, secrets, scoped authorization');
// Exercise the actual submission path, not only policy functions.
let harness=fs.readFileSync('test_web.cjs','utf8').split('(async()=>{')[0];
new Function('require','__dirname',harness+String.raw`
(async()=>{
 assert.match(await send('Astor est impossible à pirater'),/Je te contredis/);
 assert.match(await send('Raconte-moi une blague'),/café/);
 assert.match(await send('Sans humour'),/sobre/);
 assert.match(await send('Tu es nul'),/correction/);
 assert.match(await send('Je suis crevé'),/pause/);
 assert.match(await send('oui'),/Aucun rappel/);
 await send('Nouveau sujet');assert.doesNotMatch(await send('oui'),/Préparation proposée/);
 await send('Passe-moi Eve');assert.doesNotMatch(await send('Je suis fatigué'),/activer Morphée/);
 assert.match(await send('Astor, raconte-moi une blague'),/ton sobre/);
 assert.match(await send('Astor, supprime mes notes'),/confirmation précise/);
 const count=storage.size;await send('oui');assert.equal(storage.size,count);
 assert.equal(network,0);
 console.log('PASS personality integration: offline console, explicit module address, controls and no side effects');
})().catch(e=>{console.error(e);process.exit(1)});
`)(require,__dirname);
