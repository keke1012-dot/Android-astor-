(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const chat = $('chatlog');
  if (!chat || typeof chat.before !== 'function') return;
  const panel = document.createElement('details');
  panel.id = 'nativeSpeechPanel';
  panel.dataset.state = 'preparing';
  panel.innerHTML = "<summary class=\"voice-summary\"><span class=\"voice-symbol\" aria-hidden=\"true\">◈</span><span class=\"voice-summary-copy\"><strong>Voix ASTOR</strong><small>Français · sur votre téléphone</small></span><span id=\"nativeSpeechBadge\" class=\"voice-badge\">Préparation</span></summary>\n<div class=\"voice-content\">\n<div class=\"voice-hero\"><div><span class=\"eyebrow\">VOTRE ASSISTANT VOCAL</span><h3>Une voix à vos côtés.</h3><p>Écoutez les réponses, à votre rythme.</p></div><div class=\"voice-wave\" aria-hidden=\"true\"><i></i><i></i><i></i><i></i><i></i></div></div>\n<p id=\"nativeSpeechStatus\" class=\"voice-status\" role=\"status\" aria-live=\"polite\">Préparation de la voix française locale. Si nécessaire, touchez Actualiser les voix.</p>\n<div class=\"voice-playback\"><button type=\"button\" id=\"nativeSpeechTest\" disabled>Tester la voix</button><button type=\"button\" id=\"nativeSpeechRead\" disabled>Lire la dernière réponse</button><button type=\"button\" id=\"nativeSpeechStop\">Arrêter la lecture</button></div>\n<div class=\"voice-switches\"><label class=\"voice-switch\"><span><strong>Réponses à voix haute</strong><small>Lire les nouvelles réponses automatiquement</small></span><input type=\"checkbox\" id=\"nativeSpeechAuto\" role=\"switch\" checked></label><label class=\"voice-switch\"><span><strong>Mode muet</strong><small>Suspendre la lecture vocale</small></span><input type=\"checkbox\" id=\"nativeSpeechMute\" role=\"switch\"></label></div>\n<details class=\"voice-personalize\"><summary>Personnaliser la voix <span aria-hidden=\"true\">＋</span></summary><div class=\"voice-options\"><label for=\"nativeSpeechVoice\">Voix française installée</label><select id=\"nativeSpeechVoice\" aria-label=\"Voix française\"><option value=\"\">Détection nécessaire</option></select><label for=\"nativeSpeechMode\">Timbre de l’assistant</label><select id=\"nativeSpeechMode\"><option value=\"jarvis\">Assistant grave</option><option value=\"metal\">Métallique · science-fiction</option><option value=\"grave\">Grave · sans modulation</option></select><p>Choisissez le timbre qui vous convient. Votre sélection est conservée.</p><button type=\"button\" id=\"nativeSpeechSettings\">Réglages vocaux Android</button></div></details>\n<div class=\"voice-footer\"><button type=\"button\" id=\"nativeSpeechInit\">Actualiser les voix</button><p>Traitement local · sans compte externe<br>Lecture limitée au début de la réponse, jusqu’à 800 caractères.</p></div>\n</div>\n";
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
      $(readyForVoice && !voiceMuted ? 'nativeSpeechTest' : 'nativeSpeechInit').focus({preventScroll:true});
    };
    panel.addEventListener('toggle', () => quick.setAttribute('aria-expanded', String(panel.open)));
    actions.append(quick);
  }
  $('nativeSpeechInit').onclick = () => nav('tts-init');
  $('nativeSpeechSettings').onclick = () => nav('tts-settings');
  $('nativeSpeechMode').value = 'jarvis';
  let readyForVoice = false, voiceMuted = false, pendingReply = null, pendingTimer = null;
  const clearPending = () => { pendingReply = null; clearTimeout(pendingTimer); pendingTimer = null; };
  const flushReply = () => {
    if (!readyForVoice || !pendingReply) return;
    const text = pendingReply; clearPending();
    if (voiceMuted || !$('nativeSpeechAuto').checked || document.body.classList.contains('astor-locked')) return;
    window.AstorLocalNative?.speakReply?.(text);
  };
  window.addEventListener('astor-reply-ready', event => {
    clearPending();
    if (voiceMuted || !$('nativeSpeechAuto').checked || document.body.classList.contains('astor-locked')) return;
    if (typeof event.detail?.text !== 'string') return;
    pendingReply = event.detail.text.trim().slice(0,800);
    if (!pendingReply) return;
    pendingTimer = setTimeout(clearPending, 10000); flushReply();
  });
  const saveChoice = () => nav('tts-choice', {voice:$('nativeSpeechVoice').value, mode:$('nativeSpeechMode').value});
  $('nativeSpeechVoice').onchange = saveChoice; $('nativeSpeechMode').onchange = saveChoice;
  $('nativeSpeechAuto').onchange = () => { clearPending(); nav('tts-auto', {enabled:$('nativeSpeechAuto').checked ? '1' : '0'}); };
  const speak = text => nav('tts-speak', {text, voice:$('nativeSpeechVoice').value, mode:$('nativeSpeechMode').value});
  $('nativeSpeechTest').onclick = () => speak('Bonsoir Kellian. Je suis Astor. Voici un aperçu de ma voix locale.');
  $('nativeSpeechRead').onclick = () => {
    const bubbles = [...chat.querySelectorAll('.bubble:not(.user)')];
    const text = bubbles.at(-1)?.textContent?.trim();
    if (!text) { $('nativeSpeechStatus').textContent = 'Aucune réponse à lire pour le moment.'; return; }
    speak(text.length > 800 ? text.slice(0, 800) + '…' : text);
  };
  $('nativeSpeechStop').onclick = () => { clearPending(); panel.dataset.state = voiceMuted ? 'muted' : readyForVoice ? 'ready' : 'preparing'; $('nativeSpeechBadge').textContent = voiceMuted ? 'Muet' : readyForVoice ? 'Prête' : 'Préparation'; nav('tts-stop'); };
  $('nativeSpeechMute').onchange = () => { voiceMuted = $('nativeSpeechMute').checked; panel.dataset.state = voiceMuted ? 'muted' : readyForVoice ? 'ready' : 'preparing'; $('nativeSpeechBadge').textContent = voiceMuted ? 'Muet' : readyForVoice ? 'Prête' : 'Préparation'; clearPending(); nav('tts-mute', {enabled:voiceMuted ? '1' : '0'}); };
  window.ASTOR_SPEECH = Object.freeze({state(message, voices, ready, muted, auto = true, preferredVoice = "", profile = "jarvis") {
    if (document.body.classList.contains('astor-locked')) return;
    $('nativeSpeechStatus').textContent = String(message || 'État vocal indisponible.');
    const playing = ready === true && muted !== true && message === 'ASTOR · lecture locale en cours.';
    panel.dataset.state = muted === true ? 'muted' : playing ? 'speaking' : ready === true ? 'ready' : 'preparing';
    $('nativeSpeechBadge').textContent = muted === true ? 'Muet' : playing ? 'Lecture' : ready === true ? 'Prête' : 'Préparation';
    const select = $('nativeSpeechVoice'), previous = preferredVoice || select.value;
    select.replaceChildren();
    if (Array.isArray(voices)) for (const v of voices) {
      if (typeof v.name !== 'string' || typeof v.lang !== 'string') continue;
      const option = document.createElement('option'); option.value = v.name; option.textContent = v.name + ' · ' + v.lang;
      select.append(option);
    }
    if ([...select.options].some(o => o.value === previous)) select.value = previous;
    voiceMuted = muted === true; readyForVoice = ready === true;
    $('nativeSpeechMute').checked = voiceMuted;
    $('nativeSpeechAuto').checked = auto === true;
    if (['jarvis','metal','grave'].includes(profile)) $('nativeSpeechMode').value = profile;
    if (voiceMuted || auto !== true) clearPending();
    $('nativeSpeechTest').disabled = ready !== true || muted === true;
    $('nativeSpeechRead').disabled = ready !== true || muted === true;
    flushReply();
  }});
  window.addEventListener('pagehide', clearPending);
  window.addEventListener('astor-lock-request', () => {
    clearPending(); readyForVoice = false; panel.dataset.state = 'locked'; $('nativeSpeechBadge').textContent = 'Verrouillée';
    $('nativeSpeechTest').disabled = true; $('nativeSpeechRead').disabled = true;
  });
})();
