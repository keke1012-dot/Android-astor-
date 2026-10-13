const fs=require('fs'),path=require('path'),assert=require('assert');
const {JSDOM,VirtualConsole}=require('jsdom');const {webcrypto}=require('crypto');
let calls=[],network=0,errors=[];const vc=new VirtualConsole();vc.on('jsdomError',e=>errors.push(e.message));
const html=fs.readFileSync(path.join(__dirname,'../app/src/main/assets/index.html'),'utf8');
const dom=new JSDOM(html,{url:'https://appassets.androidplatform.net/assets/index.html',runScripts:'dangerously',pretendToBeVisual:true,virtualConsole:vc,beforeParse(w){
Object.defineProperty(w,'crypto',{value:webcrypto});Object.defineProperty(w,'isSecureContext',{value:true});w.TextEncoder=TextEncoder;w.TextDecoder=TextDecoder;w.scrollTo=()=>{};w.HTMLMediaElement.prototype.pause=function(){};w.HTMLMediaElement.prototype.load=function(){};w.fetch=()=>{network++;throw Error('Network forbidden')};
w.AstorLocalNative={state(){},cancel(){},generate(id,p){calls.push(['simple',id,p]);},cognitive(id,p){calls.push(['cognitive',id,p]);},proposeCorrection(q,c){calls.push(['correction',q,c]);}};
}});
(async()=>{const w=dom.window,$=id=>w.document.getElementById(id);await new Promise(r=>setImmediate(r));
assert.equal($('enableCognitiveAI').checked,false);w.ASTOR_LOCAL_AI.state('Prêt',true,true,true,true,false);
let p=w.ASTOR_LOCAL_AI.generate('Question courte');assert.equal(calls.at(-1)[0],'simple');w.ASTOR_LOCAL_AI.result(calls.at(-1)[1],'Réponse','');await p;
$('enableCognitiveAI').checked=true;p=w.ASTOR_LOCAL_AI.generate('Question courte');assert.equal(calls.at(-1)[0],'cognitive');w.ASTOR_LOCAL_AI.result(calls.at(-1)[1],'Synthèse','');await p;
p=w.ASTOR_LOCAL_AI.generate('x'.repeat(2201));assert.equal(calls.at(-1)[0],'simple');w.ASTOR_LOCAL_AI.result(calls.at(-1)[1],'Réponse longue','');await p;
$('learningQuestion').value='Question';$('learningCorrection').value='Correction';$('proposeLearningCorrection').click();assert.deepEqual(calls.at(-1),['correction','Question','Correction']);
assert.equal(network,0);assert.equal(errors.length,0,errors.join('\n'));console.log('PASS: unified cognitive opt-in, simple fallback for long context, correction confirmation bridge and zero network.');w.close();})().catch(e=>{console.error(e);dom.window.close();process.exit(1)});
