(()=>{'use strict';
if(!document.body.dataset||!document.body.dataset.view||document.body.classList.contains('astor-locked'))return;
if(typeof matchMedia!=='function')return;
const reduced=matchMedia('(prefers-reduced-motion: reduce)');if(reduced.matches)return;
const scene=document.createElement('section');scene.id='astorBigBang';scene.setAttribute('aria-label','Entrée Astor');
const cosmos=document.createElement('div');cosmos.className='bang-cosmos';cosmos.setAttribute('aria-hidden','true');
const nucleus=document.createElement('div');nucleus.className='bang-nucleus';const wave=document.createElement('div');wave.className='bang-wave';cosmos.append(nucleus,wave);
for(let i=0;i<48;i++){const star=document.createElement('i');star.className='bang-star';const angle=i*2.399963229728653,reach=145+(i%7)*34;star.style.setProperty('--x',Math.round(Math.cos(angle)*reach)+'px');star.style.setProperty('--y',Math.round(Math.sin(angle)*reach)+'px');star.style.setProperty('--delay',(660+(i%4)*30)+'ms');star.style.setProperty('--size',(i%3===0?3:2)+'px');cosmos.append(star);}
const identity=document.createElement('div');identity.className='bang-identity';const monogram=document.createElement('span');monogram.className='bang-monogram';monogram.textContent='A';monogram.setAttribute('aria-hidden','true');const title=document.createElement('h1');title.textContent='ASTOR';const caption=document.createElement('p');caption.textContent='À vos côtés.';identity.append(monogram,title,caption);
const skip=document.createElement('button');skip.id='skipBigBang';skip.type='button';skip.textContent='Passer l’entrée';scene.append(cosmos,identity,skip);
const surfaces=[document.getElementById('androidAppbar'),document.querySelector('main'),document.getElementById('androidNavigation')].filter(Boolean);const prior=surfaces.map(el=>({el,inert:el.inert}));const focused=document.activeElement;let timer;
function finish(){clearTimeout(timer);scene.remove();for(const {el,inert}of prior)el.inert=inert;document.removeEventListener('visibilitychange',visibility);window.removeEventListener('pagehide',finish);window.removeEventListener('astor-lock-request',finish);reduced.removeEventListener('change',motion);if(document.activeElement===document.body&&focused&&focused!==document.body&&focused.isConnected)focused.focus({preventScroll:true});}
function visibility(){if(document.hidden)finish();}function motion(event){if(event.matches)finish();}
skip.onclick=finish;scene.addEventListener('keydown',event=>{if(event.key==='Escape'){event.preventDefault();finish();}else if(event.key==='Tab'){event.preventDefault();skip.focus({preventScroll:true});}});
for(const {el}of prior)el.inert=true;document.body.append(scene);skip.focus({preventScroll:true});timer=setTimeout(finish,2800);
document.addEventListener('visibilitychange',visibility);window.addEventListener('pagehide',finish);window.addEventListener('astor-lock-request',finish);reduced.addEventListener('change',motion);
})();
