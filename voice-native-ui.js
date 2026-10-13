(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const chat = $('chatlog');
  if (!chat || typeof chat.before !== 'function') return;
  const panel = document.createElement('details');
  panel.id = 'nativeSpeechPanel';
  panel.style.cssText = 'flex-shrink:0;margin:8px 0;border:1px solid #285269;border-radius:12px;padding:10px;max-height:35dvh;overflow:auto';
  panel.innerHTML = '<summary style="cursor:pointer;color:#a3eeff">◈ ASTOR · Voix grave et métallique</summary>'
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
  chat.before(panel);
  const nav = (action, args = {}) => {
    if (document.body.classList.contains('astor-locked')) return;
    const query = new URLSearchParams(args).toString();
    location.href = 'astor-native://' + action + (query ? '?' + query : '');
  };
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
