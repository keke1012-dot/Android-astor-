const fs=require('fs'),path=require('path'),assert=require('assert');
const {chromium}=require('playwright');
(async()=>{
 const browser=await chromium.launch({headless:true});
 const html=fs.readFileSync(path.join(__dirname,'../app/src/main/assets/index.html'),'utf8');
 const out=path.join(__dirname,'../dist/ui');fs.mkdirSync(out,{recursive:true});
 for(const [width,height] of [[360,800],[393,852],[393,420]]){
  const page=await browser.newPage({viewport:{width,height},deviceScaleFactor:1});const errors=[];
  page.on('pageerror',e=>errors.push(e.message));
  await page.route('**/*',r=>r.request().url()==='https://appassets.androidplatform.net/assets/index.html'?r.fulfill({contentType:'text/html',body:html}):r.abort());
  await page.goto('https://appassets.androidplatform.net/assets/index.html');
  for(const view of ['console','internet','memory','modules','atlas','media']){
   await page.locator('#androidTab_'+view).click();
   const size=await page.evaluate(()=>({scroll:document.documentElement.scrollWidth,width:innerWidth}));
   assert(size.scroll<=size.width+1,`${view}: horizontal overflow at ${width}x${height}: ${JSON.stringify(size)}`);
   const tab=await page.locator('#androidTab_'+view).boundingBox();assert(tab.width>=44&&tab.height>=44);
   if(height>540&&(view==='console'||view==='internet'))await page.screenshot({path:path.join(out,`${view}-${width}.png`),fullPage:true});
  }
  assert.equal(errors.length,0,errors.join('\n'));await page.close();
 }
 await browser.close();console.log('PASS: rendered mobile navigation, six views, 360/393px widths, keyboard-height layout and no horizontal overflow.');
})().catch(e=>{console.error(e);process.exit(1)});
