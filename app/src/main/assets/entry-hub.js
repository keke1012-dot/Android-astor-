(()=>{'use strict';
if(!document.body.dataset||!document.body.dataset.view)return;
const $=id=>document.getElementById(id), workspace=document.querySelector('.workspace');
if(!workspace||!$('androidConsolePanel'))return;
const text=(tag,value)=>{const el=document.createElement(tag);el.textContent=value;return el;};
const hero=document.querySelector('.hero-copy');hero.querySelector('h1').textContent='Votre espace Astor.';hero.querySelector('p').textContent='Un échange, une idée, un moment pour vous.';
hero.querySelector('.eyebrow').textContent=new Intl.DateTimeFormat('fr-FR',{weekday:'long',day:'numeric',month:'long'}).format(new Date());
const area=document.createElement('section');area.id='entryHub';area.setAttribute('aria-labelledby','entryHubTitle');
const heading=text('h2','Que souhaitez-vous faire ?');heading.id='entryHubTitle';area.append(heading);
const grid=document.createElement('div');grid.className='hub-grid';area.append(grid);
const guarded=fn=>()=>{if(!document.body.classList.contains('astor-locked'))fn();};
const reveal=id=>{const el=$(id);if(!el)return;if(el.tagName==='DETAILS')el.open=true;el.scrollIntoView({behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'instant':'smooth',block:'center'});};
const cards=[
['hubMorphee','Morphée','Sommeil et bien-être','☾',()=>{location.href='astor-native://morphee';}],
['hubHades','Hades','Repères et alertes','◇',()=>{$('hadesOpen').click();reveal('hadesAlert');}],
['hubVoice','Voix d’Astor','Écouter et personnaliser','≋',()=>{$('voiceQuickAccess').click();}],
['hubPhone','Téléphone N2','Accès à autoriser · expérimental','↗',()=>{location.href='astor-native://phone-access';}]
];
for(const[id,title,detail,symbol,action]of cards){const b=document.createElement('button');b.id=id;b.type='button';b.className='hub-card';const mark=text('span',symbol);mark.className='hub-symbol';mark.setAttribute('aria-hidden','true');const copy=document.createElement('span');copy.append(text('strong',title),text('small',detail));b.append(mark,copy);b.addEventListener('click',guarded(action));grid.append(b);}
const shortcuts=document.createElement('div');shortcuts.className='hub-shortcuts';
for(const[label,id]of [['Mes notes','androidTab_memory'],['Les modules','androidTab_modules'],['Atlas','androidTab_atlas']]){const b=text('button',label);b.type='button';b.onclick=guarded(()=>$(id).click());shortcuts.append(b);}
const syncHades=()=>{$('hubHades').disabled=$('hadesOpen').disabled;$('hubHades').querySelector('small').textContent=$('hadesOpen').disabled?'Désactivé pour cette session':'Repères et alertes';};$('enableModuleHADES').addEventListener('change',syncHades);syncHades();
area.append(shortcuts);$('androidConsolePanel').before(area);
const file=$('selectedFilePanel');if(file){const drawer=document.createElement('details');drawer.id='hubFileDrawer';drawer.className='model-drawer';drawer.append(text('summary','Fichier pour la conversation'));file.before(drawer);drawer.append(file);for(const b of $('androidQuickActions').querySelectorAll('button'))if(b.textContent==='Fichiers')b.onclick=guarded(()=>reveal('hubFileDrawer'));}
$('androidConsolePanel').querySelector('h2').textContent='Parlons ensemble';
})();
