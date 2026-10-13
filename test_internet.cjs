const fs=require('fs'),path=require('path');
let harness=fs.readFileSync(path.join(__dirname,'test_web.cjs'),'utf8').split('(async()=>{')[0];
new Function('require','__dirname',harness+String.raw`
(async()=>{
 assert(nodes.searchWikipedia.disabled);assert(nodes.searchWebBrowser.disabled);
 nodes.internetQuery.value='Webb';nodes.searchWikipedia.click();assert(!env.location.href.includes('astor-native://internet?'));
 nodes.enableInternet.checked=true;await nodes.enableInternet.fire('change');assert(!nodes.searchWikipedia.disabled);
 nodes.internetQuery.value='';nodes.searchWikipedia.click();assert.match(nodes.internetStatus.textContent,/Saisissez/);
 nodes.memory.value='COFFRE_PRIVE';nodes.passphrase.value='PHRASE_PRIVEE';nodes.message.value='HISTORIQUE_PRIVE';
 nodes.internetQuery.value='Webb & espace';nodes.searchWikipedia.click();
 const uri=new URL(env.location.href);assert.equal(uri.hostname,'internet');assert.equal(uri.searchParams.get('q'),'Webb & espace');assert.equal(uri.searchParams.get('action'),'wiki');
 assert(!uri.href.includes('PRIVE'));assert(nodes.searchWikipedia.disabled);assert(!nodes.cancelInternet.hidden);
 win.ASTOR_INTERNET.result('999','réponse périmée','');assert.equal(nodes.internetResults.textContent,'');
 win.ASTOR_INTERNET.result(uri.searchParams.get('id'),'<script>texte externe</script>','');assert.equal(nodes.internetResults.textContent,'<script>texte externe</script>');assert(!nodes.searchWikipedia.disabled);
 nodes.searchWikipedia.click();const stale=new URL(env.location.href).searchParams.get('id');nodes.cancelInternet.click();assert.equal(env.location.href,'astor-native://internet-cancel');
 win.ASTOR_INTERNET.result(stale,'résultat tardif','');assert.notEqual(nodes.internetResults.textContent,'résultat tardif');
 nodes.searchWikipedia.click();nodes.enableInternet.checked=false;await nodes.enableInternet.fire('change');assert(nodes.searchWikipedia.disabled);assert.equal(nodes.internetQuery.value,'');assert.equal(nodes.internetResults.textContent,'');
 nodes.enableInternet.checked=true;await nodes.enableInternet.fire('change');nodes.internetQuery.value='Mars';nodes.searchWebBrowser.click();assert.equal(new URL(env.location.href).searchParams.get('action'),'browser');
 await nodes.lockMemory.fire('click');assert(!nodes.enableInternet.checked);assert.equal(nodes.internetQuery.value,'');assert(nodes.searchWikipedia.disabled);
 assert.equal(network,0);console.log('PASS: Internet opt-in, explicit isolated query, text-only results, cancellation, stale results and lock reset.');
})().catch(e=>{console.error(e);process.exit(1)});
`)(require,__dirname);
