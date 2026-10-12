(()=>{'use strict';
const $=id=>document.getElementById(id);
const icons={micro:'M9 5a3 3 0 0 1 6 0v7a3 3 0 0 1-6 0z M6 10v2a6 6 0 0 0 12 0v-2 M12 18v4 M8 22h8',console:'M5 5h14v11H9l-4 4z',memory:'M6 4h12v16H6z M9 8h6 M9 12h6 M9 16h4',modules:'M4 4h6v6H4z M14 4h6v6h-6z M4 14h6v6H4z M14 14h6v6h-6z',atlas:'M12 3l8 4v5c0 5-8 9-8 9s-8-4-8-9V7z M8 12l3 3 5-6',media:'M9 18V5l10-2v13 M9 8l10-2 M9 18c0 3-6 3-6 0s6-3 6 0 M19 16c0 3-6 3-6 0s6-3 6 0',lock:'M7 10V7a5 5 0 0 1 10 0v3 M5 10h14v11H5z M12 14v3',settings:'M12 8a4 4 0 1 0 0 8a4 4 0 1 0 0-8 M9 3h6l1 3 3 1 2 5-2 4-3 1-1 4H9l-1-4-3-1-2-4 2-5 3-1z',send:'M4 4l17 8-17 8 4-8z M8 12h13'};
function icon(key){const ns='http://www.w3.org/2000/svg';const s=document.createElementNS(ns,'svg');s.setAttribute('viewBox','0 0 24 24');s.setAttribute('aria-hidden','true');const p=document.createElementNS(ns,'path');p.setAttribute('d',icons[key]);s.append(p);return s;}
function text(tag,content,cls){const el=document.createElement(tag);el.textContent=content;if(cls)el.className=cls;return el;}
const main=document.querySelector('main'),workspace=document.querySelector('.workspace');
const modules=$('modules').closest('aside');modules.id='androidModulesPanel';
const memory=document.createElement('section');memory.id='androidMemoryPanel';memory.className='panel';
let cursor=Array.from(modules.children).find(el=>el.tagName==='H2'&&el.textContent.includes('MÉMOIRE'));
while(cursor){const next=cursor.nextElementSibling;memory.append(cursor);cursor=next;}main.append(memory);
const header=document.querySelector('header'),bar=document.createElement('div');bar.id='androidAppbar';header.before(bar);bar.append(header);
header.querySelector('strong').replaceChildren(text('span','ASTOR'),text('small','Votre espace personnel'));
const consolePanel=$('chatForm').closest('section');consolePanel.id='androidConsolePanel';
const nav=document.createElement('nav');nav.id='androidNavigation';nav.setAttribute('aria-label','Navigation principale');document.body.append(nav);
const views=[['console','Accueil',workspace],['memory','Notes',memory],['modules','Systèmes',modules],['atlas','Atlas',$('atlasV2Panel')],['media','Média',$('mediaPanel')]];
function show(name){views.forEach(([key,,panel])=>{panel.hidden=key!==name;$('androidTab_'+key).setAttribute('aria-pressed',String(key===name));});document.body.dataset.view=name;window.scrollTo({top:0,behavior:'instant'});if(name==='console')requestAnimationFrame(()=>{const log=$('chatlog');log.scrollTop=log.scrollHeight;});}
views.forEach(([key,label])=>{const b=document.createElement('button');b.id='androidTab_'+key;b.type='button';b.append(icon(key),text('span',label));b.addEventListener('click',()=>show(key));nav.append(b);});
const core=document.querySelector('.core'),logo=core.querySelector('.rings'),coreLabel=$('coreStatus'),hero=document.createElement('div');hero.className='hero-copy';hero.append(text('span','ESPACE PRIVÉ','eyebrow'),text('h1','Bienvenue chez vous.'),text('p','Un espace pour réfléchir, apprendre et garder vos idées.'));core.replaceChildren(hero,logo,coreLabel);coreLabel.textContent='LOCAL · ACCÈS PRIVÉ';logo.querySelector('svg').setAttribute('viewBox','20 20 68 68');
consolePanel.querySelector('h2').textContent='Parlons ensemble';
const welcome=$('chatlog').querySelector('.bubble');if(welcome)welcome.textContent='Bonjour. Que voulez-vous explorer aujourd’hui ? Les réponses sont locales dans cette version.';
const actions=document.createElement('div');actions.id='androidQuickActions';actions.setAttribute('aria-label','Commandes rapides');
['Statut','Heure','Aide'].forEach(command=>{const b=document.createElement('button');b.type='button';b.textContent=command;b.addEventListener('click',()=>{if(document.body.classList.contains('astor-locked'))return;$('message').value=command;$('chatForm').requestSubmit();});actions.append(b);});$('consoleRouteStatus').after(actions);
function headerAction(id,label,key,handler){const b=document.createElement('button');b.id=id;b.type='button';b.setAttribute('aria-label',label);b.title=label;b.append(icon(key));b.addEventListener('click',handler);header.append(b);}
headerAction('androidSettings','Paramètres','settings',()=>{location.href='astor-native://settings';});headerAction('androidLockNow','Verrouiller','lock',()=>window.dispatchEvent(new Event('astor-lock-request')));
$('wake').hidden=true;$('aiEndpoint').closest('.security').hidden=true;$('shareContext').closest('label').hidden=true;
$('message').setAttribute('enterkeyhint','send');$('message').setAttribute('autocomplete','off');$('message').placeholder='Parlez à Astor…';
const send=$('chatForm').querySelector('[type="submit"]');send.replaceChildren(icon('send'));send.setAttribute('aria-label','Envoyer');send.title='Envoyer';
const micro=$('voice');micro.disabled=false;micro.hidden=false;micro.onclick=()=>{location.href=micro.getAttribute('aria-pressed')==='true'?'astor-native://voice-cancel':'astor-native://voice';};micro.replaceChildren(icon('micro'));micro.setAttribute('aria-label','Dicter en français');micro.setAttribute('aria-pressed','false');micro.title='Dicter en français';
const voiceStatus=text('p','','voice-status');voiceStatus.id='androidVoiceStatus';voiceStatus.setAttribute('role','status');voiceStatus.setAttribute('aria-live','polite');$('chatForm').after(voiceStatus);
window.astorVoice={state(listening,message){micro.setAttribute('aria-pressed',String(listening));micro.setAttribute('aria-label',listening?'Annuler la dictée':'Dicter en français');micro.title=listening?'Annuler la dictée':'Dicter en français';$('message').readOnly=listening;send.disabled=listening;voiceStatus.textContent=message;},result(transcript){const input=$('message');const draft=input.value.trim();const spoken=String(transcript).trim();const command=!draft && /^(?:astor[\s,:-]+)?(?:statut|statuts|statue|status)[\s.!?]*$/iu.test(spoken);input.value=command?'Statut':(draft?draft+' ':'')+spoken;input.dispatchEvent(new Event('input',{bubbles:true}));}};
nav.addEventListener('click',()=>{if(micro.getAttribute('aria-pressed')==='true'){window.astorVoice.state(false,'Dictée annulée.');location.href='astor-native://voice-cancel';}});
const hint=voiceStatus.nextElementSibling;if(hint)hint.textContent='Vos échanges temporaires disparaissent au verrouillage.';
memory.querySelector('h2').textContent='Votre coffre privé';memory.prepend(text('span','NOTES CHIFFRÉES','eyebrow'));
memory.querySelector('label[for="passphrase"]').textContent='Phrase secrète du coffre';$('passphrase').placeholder='Au moins 12 caractères';$('unlockMemory').textContent='Ouvrir le coffre';$('lockMemory').textContent='Refermer';$('clearMemory').textContent='Supprimer le coffre';$('clearMemory').classList.add('danger-action');
const vaultHint=memory.querySelector('p.hint');if(vaultHint)vaultHint.textContent='Vos notes restent chiffrées sur ce téléphone. Gardez votre phrase secrète : elle ne peut pas être récupérée.';
modules.querySelector('h2').textContent='Une équipe, cinq rôles';modules.prepend(text('span','LES SYSTÈMES','eyebrow'));
const roles={ASTOR:['Coordonner','Organiser et vous accompagner'],EVE:['Apprendre','Comprendre et progresser'],HADES:['Protéger','Accès et repères d’urgence'],ATLAS:['Vérifier','Examiner les raisonnements'],ORPHEUS:['Écouter','Votre espace musical']};
modules.querySelectorAll('.module').forEach(b=>{const r=roles[b.dataset.module];b.append(text('strong',r[0],'module-role'),text('small',r[1],'module-detail'));});$('routingSettings').querySelector('legend').textContent='Disponibles pour cette session';
const atlas=$('atlasV2Panel');atlas.querySelector('h2').textContent='Prenez du recul';atlas.prepend(text('span','ATLAS · CONTRÔLE CRITIQUE','eyebrow'));atlas.querySelector('p').textContent='Examinez une idée, repérez les incertitudes et cherchez un contre-exemple. Cette analyse locale ne vérifie pas les faits en ligne.';
$('atlasInput').placeholder='Quelle idée souhaitez-vous examiner ?';$('atlasEvidence').placeholder='Ajoutez les sources ou les éléments que vous avez…';$('atlasAnalyze').textContent='Examiner avec Atlas';$('atlasClear').textContent='Recommencer';
const media=$('mediaPanel'),services=media.querySelector('.stack');Array.from(media.children).forEach(el=>el.hidden=true);
const mediaCard=document.createElement('div');mediaCard.className='service-links';Array.from(services.querySelectorAll('a')).forEach(a=>{a.textContent=a.href.includes('spotify')?'Ouvrir Spotify':'Ouvrir YouTube';mediaCard.append(a);});
media.append(text('span','ORPHEUS','eyebrow'),text('h2','Votre pause musicale'),text('p','Ouvrez votre service préféré dans son application ou votre navigateur. Astor se verrouille lorsque vous le quittez.','hint'),mediaCard,text('p','La musique d’entrée se règle dans Paramètres. Le lecteur intégré reste à développer. Le micro d’Accueil permet la dictée locale selon votre téléphone.','hint'));
document.querySelector('footer').hidden=true;show('console');
})();
