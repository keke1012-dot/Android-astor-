(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const chat = $('chatlog');
  if (!chat || typeof chat.before !== 'function') return;
  const panel = document.createElement('details');
  panel.id = 'nativeSpeechPanel';
  panel.style.cssText = 'flex-shrink:0;margin:8px 0;border:1px solid #285269;border-radius:12px;padding:10px;scroll-margin-top:90px';
  panel.innerHTML = '<summary style="cursor:pointer;color:#a3eeff">◈ Voix · ElevenLabs et Android</summary>'
    + '<p class="hint">Synthèse Android locale, sur demande. La lecture s’arrête au verrouillage.</p>'
    + '<div class="toolbar"><button type="button" id="nativeSpeechInit">Détecter les voix</button>'
    + '<button type="button" id="nativeSpeechSettings">Paramètres voix</button></div>'
    + '<label for="nativeSpeechVoice">Voix française installée</label><select id="nativeSpeechVoice" aria-label="Voix française"><option value="">Détection nécessaire</option></select>'
    + '<label for="nativeSpeechMode">Profil</label><select id="nativeSpeechMode"><option value="metal">Métallique · science-fiction</option><option value="grave">Grave · sans modulation</option></select>'
    + '<div class="toolbar"><button type="button" id="nativeSpeechTest" disabled>Tester la voix</button>'
    + '<button type="button" id="nativeSpeechRead" disabled>Lire la dernière réponse</button>'
    + '<button type="button" id="nativeSpeechStop">Arrêter</button></div>'
    + '<label class="hint"><input type="checkbox" id="nativeSpeechMute"> Mode muet</label>'
    + '<p class="hint">Lecture du début de la réponse, jusqu’à 800 caractères. Le timbre dépend de la voix choisie.</p>'
    + '<p id="nativeSpeechStatus" class="statusline" role="status" aria-live="polite">Touchez Détecter pour préparer la voix française Android.</p>';
  const online = document.createElement('div');
  online.innerHTML = '<hr><strong>Voix choisie · ElevenLabs</strong>'
    + '<p class="hint" style="overflow-wrap:anywhere">wDsJlOXPqcvIUKdLXjDs · français via le modèle multilingue</p>'
    + '<p class="hint">Lecture en ligne. Le texte et les crédits utilisés seront confirmés avant chaque envoi.</p>'
    + '<div class="toolbar"><button type="button" id="elevenConfigure">Configurer ElevenLabs</button>'
    + '<button type="button" id="elevenTest" disabled>Tester ElevenLabs</button>'
    + '<button type="button" id="elevenRead" disabled>Lire avec ElevenLabs</button>'
    + '<button type="button" id="elevenForget">Retirer la clé</button>'
    + '<button type="button" id="elevenStop">Arrêter la lecture</button></div>'
    + '<p id="elevenSpeechStatus" class="statusline" role="status" aria-live="polite">Configurez votre clé dans le panneau Android. Aucun envoi automatique.</p>';
  panel.querySelector('summary').after(online);
  let elevenConfigured = false, elevenBusy = false;
  const updateElevenButtons = () => {
    const muted = $('nativeSpeechMute').checked;
    $('elevenTest').disabled = !elevenConfigured || elevenBusy || muted;
    $('elevenRead').disabled = !elevenConfigured || elevenBusy || muted;
  };
  const local = document.createElement('details');
  local.id = 'localSpeechDrawer';
  const localTitle = document.createElement('summary'); localTitle.textContent = 'Voix Android hors ligne';
  localTitle.style.cssText = 'cursor:pointer;margin:12px 0;color:#a3eeff'; local.append(localTitle);
  const muteLabel = panel.querySelector('#nativeSpeechMute').closest('label');
  for (const node of [...panel.children]) {
    if (node.tagName !== 'SUMMARY' && node !== online && node !== muteLabel) local.append(node);
  }
  panel.append(muteLabel, local);
  chat.before(panel);
  const nav = (action, args = {}) => {
    if (document.body.classList.contains('astor-locked')) return;
    const query = new URLSearchParams(args).toString();
    location.href = 'astor-native://' + action + (query ? '?' + query : '');
  };
  const actions = $('androidQuickActions');
  if (actions) {
    const quick = document.createElement('button'); quick.id = 'elevenQuickAccess'; quick.type = 'button';
    quick.textContent = 'ElevenLabs'; quick.setAttribute('aria-controls', 'nativeSpeechPanel'); quick.setAttribute('aria-expanded', 'false');
    quick.onclick = () => {
      if (document.body.classList.contains('astor-locked')) return;
      panel.open = true; quick.setAttribute('aria-expanded', 'true');
      panel.scrollIntoView?.({behavior:'smooth', block:'start'});
      $('elevenConfigure').focus({preventScroll:true});
    };
    panel.addEventListener('toggle', () => quick.setAttribute('aria-expanded', String(panel.open)));
    actions.append(quick);
  }
  $('nativeSpeechInit').onclick = () => nav('tts-init');
  $('nativeSpeechSettings').onclick = () => nav('tts-settings');
  const speak = text => nav('tts-speak', {text, voice:$('nativeSpeechVoice').value, mode:$('nativeSpeechMode').value});
  $('nativeSpeechTest').onclick = () => speak('Bonsoir Kellian. Je suis Astor. Le système est prêt. Nous avançons une étape à la fois.');
  $('nativeSpeechRead').onclick = () => {
    const bubbles = [...chat.querySelectorAll('.bubble:not(.user)')];
    const text = bubbles.at(-1)?.textContent?.trim();
    if (!text) { $('nativeSpeechStatus').textContent = 'Aucune réponse à lire pour le moment.'; return; }
    speak(text.length > 800 ? text.slice(0, 800) + '…' : text);
  };
  $('nativeSpeechStop').onclick = () => nav('tts-stop');
  $('nativeSpeechMute').onchange = () => nav('tts-mute', {enabled:$('nativeSpeechMute').checked ? '1' : '0'});
  $('elevenConfigure').onclick = () => nav('eleven-config');
  $('elevenStop').onclick = () => nav('tts-stop');
  $('elevenForget').onclick = () => nav('eleven-forget');
  $('elevenTest').onclick = () => nav('eleven-speak', {text:'Bonsoir Kellian. Je suis Astor. Le système est prêt. Nous avançons une étape à la fois.'});
  $('elevenRead').onclick = () => {
    const text = [...chat.querySelectorAll('.bubble:not(.user)')].at(-1)?.textContent?.trim();
    if (!text) { $('elevenSpeechStatus').textContent = 'Aucune réponse à lire pour le moment.'; return; }
    nav('eleven-speak', {text:text.slice(0, 800)});
  };
  window.ASTOR_ELEVEN = Object.freeze({state(message, configured, busy) {
    if (document.body.classList.contains('astor-locked')) return;
    $('elevenSpeechStatus').textContent = String(message || 'État ElevenLabs indisponible.');
    elevenConfigured = configured === true; elevenBusy = busy === true; updateElevenButtons();
  }});
  window.ASTOR_SPEECH = Object.freeze({state(message, voices, ready, muted) {
    if (document.body.classList.contains('astor-locked')) return;
    $('nativeSpeechStatus').textContent = String(message || 'État vocal indisponible.');
    const select = $('nativeSpeechVoice'), previous = select.value;
    select.replaceChildren();
    if (Array.isArray(voices)) for (const v of voices) {
      if (typeof v.name !== 'string' || typeof v.lang !== 'string') continue;
      const option = document.createElement('option'); option.value = v.name; option.textContent = v.name + ' · ' + v.lang;
      select.append(option);
    }
    if ([...select.options].some(o => o.value === previous)) select.value = previous;
    $('nativeSpeechMute').checked = muted === true; updateElevenButtons();
    $('nativeSpeechTest').disabled = ready !== true || muted === true;
    $('nativeSpeechRead').disabled = ready !== true || muted === true;
  }});
  window.addEventListener('astor-lock-request', () => {
    $('nativeSpeechTest').disabled = true; $('nativeSpeechRead').disabled = true;
    $('elevenTest').disabled = true; $('elevenRead').disabled = true;
  });
})();
