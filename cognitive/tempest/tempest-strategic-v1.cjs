'use strict';
/** TEMPEST V1: deterministic, offline scenario planner. No device permissions or execution. */
const MODES = Object.freeze(['decision','red-team','duel']);
function evaluate(input) {
  if (!input || typeof input !== 'object') throw new TypeError('Input required');
  const decision = String(input.decision || '').trim();
  if (!decision || decision.length > 2000) throw new RangeError('Decision must contain 1–2000 characters');
  const mode = input.mode || 'decision';
  if (!MODES.includes(mode)) throw new RangeError('Unsupported mode');
  const assumptions = Array.isArray(input.assumptions)
    ? input.assumptions.slice(0,8).filter(x=>typeof x==='string').map(x=>x.trim().slice(0,240)).filter(Boolean)
    : [];
  const context = String(input.context || '').slice(0,2000);
  const sensitive = /\b(delete|erase|remove|wipe|supprimer|effacer|désinstaller|installer|publish|publier|payer|purchase|acheter|permission|autorisation|accès|access|fichier|file|secret|clé api|api key|token|internet|network|réseau)\b/i.test(decision+' '+context);
  return {
    version:'tempest-strategic-v1',
    mode, decision, assumptions,
    factStatus:'unverified-user-input',
    scenarios:[
      {id:'favorable',kind:'hypothesis',description:'Le résultat souhaité est atteint si les hypothèses sont exactes et les contrôles fonctionnent.'},
      {id:'intermediate',kind:'hypothesis',description:'Des contraintes imprévues exigent un ajustement, des tests ou un retour arrière.'},
      {id:'adverse',kind:'hypothesis',description:'Une dépendance ou une hypothèse échoue ; limiter les effets et conserver une voie de restauration.'}
    ],
    challenge: mode==='duel' ? 'Quelle preuve ferait changer votre décision ?' :
      mode==='red-team' ? 'Quelle défaillance pourrait invalider les protections supposées ?' :
      'Quelles hypothèses n’ont pas encore été vérifiées ?',
    controls: [
      'Distinguer faits établis, hypothèses et incertitudes.',
      'Demander une validation humaine avant toute action sensible.',
      'Tester sur une branche isolée et préserver la version stable.'
    ],
    requiresHumanApproval:sensitive,
    canExecuteActions:false,
    limitations:'Scénarios génériques, non prédictifs ; aucune recherche ni validation des faits.'
  };
}
module.exports={evaluate,MODES};
