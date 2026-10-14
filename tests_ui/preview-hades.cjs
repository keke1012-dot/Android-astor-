const fs=require('fs'),http=require('http');
const {chromium}=require('playwright');
(async()=>{
 const html=fs.readFileSync(require('path').join(__dirname,'../app/src/main/assets/index.html'));
 const server=http.createServer((q,r)=>{r.setHeader('Content-Type','text/html');r.end(html)});await new Promise(r=>server.listen(0,'127.0.0.1',r));
 let browser;
 try{
 browser=await chromium.launch({headless:true,args:['--no-sandbox']});const page=await browser.newPage({viewport:{width:393,height:852},deviceScaleFactor:1});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto('http://127.0.0.1:'+server.address().port);await page.locator('#hadesTest').click();
 await page.locator('#hadesCritical').click();
 fs.mkdirSync('hades-previews',{recursive:true});await page.screenshot({path:'hades-previews/hades-mobile.png',fullPage:true});
 const bounds=await page.locator('#hadesAlert').boundingBox();const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth);
 if(errors.length||overflow||bounds.width>393)throw new Error(JSON.stringify({errors,overflow,bounds}));
 await page.setViewportSize({width:320,height:740});
 await page.screenshot({path:'hades-previews/hades-compact.png',fullPage:true});
 if(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth))throw new Error('Horizontal overflow at 320 px');
 for(const id of ['hadesOpen','hadesTest','hadesVigilance','hadesUrgent','hadesCritical','hadesRead','hadesClose']){const box=await page.locator('#'+id).boundingBox();if(!box||box.height<44)throw new Error('Small touch target: '+id);}
 console.log('Mobile DOM rendered at 393 and 320 px, no script errors or horizontal overflow, readable touch targets');
 }finally{if(browser)await browser.close();server.close();}
})().catch(e=>{console.error(e.message);process.exit(1)});
