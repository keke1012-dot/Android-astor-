(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const chat = $('chatlog');
  if (!chat || typeof chat.before !== 'function') return;
  const panel = document.createElement('details');
  panel.id = 'nativeSpeechPanel';
  panel.style.cssText = 'flex-shrink:0;margin:8px 0;border:1px solid #285269;border-radius:12px;padding:10px;scroll-margin-top:90px';
  panel.innerHTML = '<summary style="cursor:pointer;color:#a3eeff">◈ Voix ASTOR · hors ligne</summary>'
    + '<p class="hint">Voix française posée, avec une légère résonance numérique. Sans compte ni clé API.</p>'
    + '<div class="toolbar"><button type="button" id="nativeSpeechInit">Détecter les voix</button>'
    + '<button type="button" id="nativeSpeechSettings">Paramètres voix</button></div>'
    + '<label for="nativeSpeechVoice">Voix française installée</label><select id="nativeSpeechVoice" aria-label="Voix française"><option value="">Détection nécessaire</option></select>'
    + '<label for="nativeSpeechMode">Profil</label><select id="nativeSpeechMode"><option value="jarvis">Assistant · inspiré de JARVIS</option><option value="metal">Métallique · science-fiction</option><option value="grave">Grave · sans modulation</option></select>'
    + '<div class="toolbar"><button type="button" id="nativeSpeechTest" disabled>Tester la voix</button>'
    + '<button type="button" id="nativeSpeechRead" disabled>Lire la dernière réponse</button>'
    + '<button type="button" id="nativeSpeechStop">Arrêter</button></div>'
    + '<label class="hint"><input type="checkbox" id="nativeSpeechMute"> Mode muet</label>'
    + '<p class="hint">Lecture du début de la réponse, jusqu’à 800 caractères. Le timbre dépend de la voix choisie.</p>'
    + '<p id="nativeSpeechStatus" class="statusline" role="status" aria-live="polite">Préparation de la voix française locale. Si nécessaire, touchez Détecter les voix.</p>';
  chat.before(panel);
  const nav = (action, args = {}) => {
    if (document.body.classList.contains('astor-locked')) return;
    const query = new URLSearchParams(args).toString();
    location.href = 'astor-native://' + action + (query ? '?' + query : '');
  };
  const actions = $('androidQuickActions');
  if (actions) {
    const quick = document.createElement('button'); quick.id = 'voiceQuickAccess'; quick.type = 'button';
    quick.textContent = 'Voix ASTOR'; quick.setAttribute('aria-controls', 'nativeSpeechPanel'); quick.setAttribute('aria-expanded', 'false');
    quick.onclick = () => {
      if (document.body.classList.contains('astor-locked')) return;
      panel.open = true; quick.setAttribute('aria-expanded', 'true');
      panel.scrollIntoView?.({behavior:'smooth', block:'start'});
      $('nativeSpeechInit').focus({preventScroll:true});
    };
    panel.addEventListener('toggle', () => quick.setAttribute('aria-expanded', String(panel.open)));
    actions.append(quick);
  }
  $('nativeSpeechInit').onclick = () => nav('tts-init');
  $('nativeSpeechSettings').onclick = () => nav('tts-settings');
  $('nativeSpeechMode').value = 'jarvis';
  const speak = text => nav('tts-speak', {text, voice:$('nativeSpeechVoice').value, mode:$('nativeSpeechMode').value});
  $('nativeSpeechTest').onclick = () => speak('Bonsoir Kellian. Je suis Astor. Tous les systèmes sont prêts. Je suis à votre écoute.');
  $('nativeSpeechRead').onclick = () => {
    const bubbles = [...chat.querySelectorAll('.bubble:not(.user)')];
    const text = bubbles.at(-1)?.textContent?.trim();
    if (!text) { $('nativeSpeechStatus').textContent = 'Aucune réponse à lire pour le moment.'; return; }
    speak(text.length > 800 ? text.slice(0, 800) + '…' : text);
  };
  $('nativeSpeechStop').onclick = () => nav('tts-stop');
  $('nativeSpeechMute').onchange = () => nav('tts-mute', {enabled:$('nativeSpeechMute').checked ? '1' : '0'});
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
    $('nativeSpeechMute').checked = muted === true;
    $('nativeSpeechTest').disabled = ready !== true || muted === true;
    $('nativeSpeechRead').disabled = ready !== true || muted === true;
  }});
  window.addEventListener('astor-lock-request', () => {
    $('nativeSpeechTest').disabled = true; $('nativeSpeechRead').disabled = true;
  });
})();
