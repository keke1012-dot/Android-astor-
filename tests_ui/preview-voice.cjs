const fs=require('fs'),http=require('http'),assert=require('assert');
const {chromium}=require('playwright');
(async()=>{
 const html=fs.readFileSync(require('path').join(__dirname,'../app/src/main/assets/index.html'));
 const server=http.createServer((q,r)=>{const file=q.url.split('?')[0];if(file==='/entry-hub.js'||file==='/entry-hub.css'){r.setHeader('Content-Type',file.endsWith('.js')?'text/javascript':'text/css');return r.end(fs.readFileSync(require('path').join(__dirname,'../app/src/main/assets',file.slice(1))));}r.setHeader('Content-Type','text/html');r.end(html)});await new Promise(r=>server.listen(0,'127.0.0.1',r));let browser;
 try{
 browser=await chromium.launch({headless:true,args:['--no-sandbox']});const page=await browser.newPage({viewport:{width:393,height:852},deviceScaleFactor:1});const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.emulateMedia({reducedMotion:'reduce'});await page.goto('http://127.0.0.1:'+server.address().port);await page.locator('#voiceQuickAccess').click();
 fs.mkdirSync('voice-previews',{recursive:true});
 for(const width of [393,320]){
 await page.setViewportSize({width,height:852});
 await page.evaluate(()=>window.ASTOR_SPEECH.state('Voix française locale prête.',[{name:'fr-voix-masculine-locale',lang:'fr-FR'}],true,false,true,'fr-voix-masculine-locale','jarvis'));
 await page.locator('#nativeSpeechPanel').screenshot({path:'voice-previews/voice-ready-'+width+'.png'});
 assert.equal(await page.locator('#nativeSpeechBadge').textContent(),'Prête');
 await page.evaluate(()=>window.ASTOR_SPEECH.state('ASTOR · lecture locale en cours.',[],true,false));
 assert.equal(await page.locator('#nativeSpeechPanel').getAttribute('data-state'),'speaking');
 await page.locator('#nativeSpeechPanel').screenshot({path:'voice-previews/voice-speaking-'+width+'.png'});
 await page.evaluate(()=>window.ASTOR_SPEECH.state('Mode muet actif.',[],true,true));
 assert.equal(await page.locator('#nativeSpeechPanel').getAttribute('data-state'),'muted');assert(await page.locator('#nativeSpeechTest').isDisabled());
 await page.locator('#nativeSpeechPanel').screenshot({path:'voice-previews/voice-muted-'+width+'.png'});
 await page.evaluate(()=>window.ASTOR_SPEECH.state('Voix française locale prête.',[{name:'voix locale',lang:'fr-FR'}],true,false));
 await page.locator('.voice-personalize summary').click();
 assert(await page.locator('#nativeSpeechVoice').isVisible());
 await page.locator('#nativeSpeechPanel').screenshot({path:'voice-previews/voice-options-'+width+'.png'});
 await page.locator('.voice-personalize summary').click();
 assert(!await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),'Horizontal overflow');
 for(const id of ['nativeSpeechTest','nativeSpeechRead','nativeSpeechStop','nativeSpeechInit']){const box=await page.locator('#'+id).boundingBox();assert(box&&box.height>=44,'Small touch target: '+id);}
 }
 assert.equal(errors.length,0,errors.join(';'));console.log('PASS: voice mobile layout at 393/320 px, controls, state, mute, personalization and no overflow');
 }finally{if(browser)await browser.close();server.close();}
})().catch(e=>{console.error(e.message);process.exit(1)});
