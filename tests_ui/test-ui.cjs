const fs=require('fs'),assert=require('assert'),path=require('path');
const {JSDOM,VirtualConsole}=require('jsdom');
const {webcrypto}=require('crypto');
const html=fs.readFileSync(path.join(__dirname,'../app/src/main/assets/index.html'),'utf8').replaceAll("location.href='astor-native://activity'","window.__nativeActivityRequested=true");
const errors=[];const vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
const dom=new JSDOM(html,{url:'https://appassets.androidplatform.net/assets/index.html',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc,beforeParse(w){
 Object.defineProperty(w,'crypto',{value:webcrypto});w.TextEncoder=TextEncoder;w.TextDecoder=TextDecoder;w.confirm=()=>true;w.scrollTo=()=>{};
 w.HTMLMediaElement.prototype.pause=function(){};w.HTMLMediaElement.prototype.load=function(){};w.HTMLMediaElement.prototype.play=function(){return Promise.resolve()};
 w.fetch=()=>{throw Error('Network forbidden')};
}});
const d=dom.window.document,$=id=>d.getElementById(id);
const tick=()=>new Promise(r=>setImmediate(r));
(async()=>{
 const panels=['androidMemoryPanel','androidModulesPanel','atlasV2Panel','mediaPanel'];
 assert(!d.querySelector('.workspace').hidden);panels.forEach(id=>assert($(id).hidden));
 for(const key of ['memory','modules','atlas','media','console']){
  $('androidTab_'+key).click();
  const pressed=[...d.querySelectorAll('#androidNavigation button')].filter(b=>b.getAttribute('aria-pressed')==='true');
  assert.equal(pressed.length,1);assert.equal(pressed[0].id,'androidTab_'+key);
  assert.equal([...d.querySelector('main').children].filter(el=>!el.hidden).length,1);
 }
 assert($('androidMemoryPanel').contains($('passphrase')));assert($('androidMemoryPanel').contains($('memory')));
 assert($('androidModulesPanel').contains($('enableModuleHADES')));
 for(const b of d.querySelectorAll('#androidQuickActions button')){b.click();await tick();assert.equal($('message').value,'');assert($('chatlog').children.length>0);}
 $('message').value='Je veux apprendre l’anglais';$('chatForm').requestSubmit();await tick();
 assert.match($('chatlog').lastElementChild.textContent,/EVE · mode local/);
 $('androidTab_memory').click();assert(!d.querySelector('.workspace').hidden===false);
 $('androidTab_console').click();assert.match($('chatlog').lastElementChild.textContent,/EVE · mode local/);
 assert(d.querySelector('.astor-live-logo .astor-logo-orbit'));assert(!d.querySelector('.core .letter'));
 assert($('androidSettings'));assert.equal($('androidSettings').getAttribute('aria-label'),'Paramètres');
 assert.equal($('message').getAttribute('enterkeyhint'),'send');assert($('aiEndpoint').closest('.security').hidden);
 assert.equal($('androidNavigation').parentElement,d.body);
 assert.equal($('androidTab_console').textContent,'Accueil');assert.equal($('androidTab_modules').textContent,'Systèmes');
 assert.equal(d.querySelectorAll('.service-links a').length,2);
 assert(!$('voice').disabled);assert.equal($('voice').getAttribute('aria-label'),'Dicter en français');
 const before=$('chatlog').children.length;$('message').value='Mon brouillon';
 dom.window.astorVoice.state(true,'Écoute locale');assert($('message').readOnly);assert($('chatForm').querySelector('[type=submit]').disabled);
 dom.window.astorVoice.result('une phrase \" avec <script>');dom.window.astorVoice.state(false,'Relisez');
 assert.equal($('message').value,'Mon brouillon une phrase \" avec <script>');assert.equal($('chatlog').children.length,before);assert(!$('message').readOnly);assert(!$('chatForm').querySelector('[type=submit]').disabled);
 for(const spoken of ['statue','Statut','statuts','status','Astor, statue !','ASTOR statut.']){$('message').value='';dom.window.astorVoice.result(spoken);assert.equal($('message').value,'Statut');}
 for(const spoken of ['une statue','la statue de la liberté','statue de bronze']){$('message').value='';dom.window.astorVoice.result(spoken);assert.equal($('message').value,spoken);}
 $('message').value='Je vois une';dom.window.astorVoice.result('statue');assert.equal($('message').value,'Je vois une statue');
 $('message').value='';dom.window.astorVoice.result('statue');const count=$('chatlog').children.length;assert.equal($('chatlog').children.length,count);$('chatForm').requestSubmit();await tick();assert.match($('chatlog').lastElementChild.textContent,/Interface active|Accès Android|mémoire/i);

 for(const request of ['Je veux apprendre l’anglais','Je débute']){$('message').value=request;$('chatForm').requestSubmit();await tick();}
 $('androidTab_memory').click();$('androidTab_console').click();
 dom.window.astorVoice.result('Donne-moi un exercice');assert.equal($('message').value,'Donne-moi un exercice');$('chatForm').requestSubmit();await tick();assert.match($('chatlog').lastElementChild.textContent,/Exercice d’anglais.*bases/s);
 $('message').value='Plus simple';$('chatForm').requestSubmit();await tick();assert.match($('chatlog').lastElementChild.textContent,/Exercice d’anglais.*bases/s);
 assert(!errors.length,errors.join('\n'));
 console.log('PASS: navigation 5 vues, conservation des contrôles et de la conversation, commandes rapides, routage EVE, absence d’erreurs JavaScript.');
 dom.window.close();
})().catch(e=>{console.error(e);dom.window.close();process.exit(1)});
