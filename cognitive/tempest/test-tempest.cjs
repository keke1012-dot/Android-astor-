'use strict';
const assert=require('node:assert/strict');
const {evaluate}=require('./tempest-strategic-v1.cjs');
const basic=evaluate({decision:'Améliorer la compréhension naturelle'});
assert.equal(basic.scenarios.length,3);
assert.equal(basic.canExecuteActions,false);
assert.equal(basic.factStatus,'unverified-user-input');
assert.equal(basic.requiresHumanApproval,false);
assert.equal(evaluate({decision:'Accès aux fichiers Android'}).requiresHumanApproval,true);
assert.equal(evaluate({decision:'Supprimer les sauvegardes'}).requiresHumanApproval,true);
assert.match(evaluate({decision:'Tester une stratégie',mode:'duel'}).challenge,/preuve/);
assert.match(evaluate({decision:'Tester une stratégie',mode:'red-team'}).challenge,/défaillance/);
assert.throws(()=>evaluate({decision:''}),RangeError);
assert.throws(()=>evaluate({decision:'x'.repeat(2001)}),RangeError);
assert.throws(()=>evaluate({decision:'Essai',mode:'execute'}),RangeError);
assert.equal(evaluate({decision:'Essai',assumptions:['A',5,'B']}).assumptions.length,2);
const sensitiveExamples=[
  'Supprime les sauvegardes',
  'Efface mes fichiers',
  'Désactive la sécurité',
  'Envoie mes données',
  'Installe une application',
  'Donne accès à Internet',
  'Partage le token',
  'Contournons les permissions'
];
for (const decision of sensitiveExamples) {
  assert.equal(evaluate({decision}).requiresHumanApproval,true,decision);
}
assert.equal(evaluate({decision:'Analyser les risques du projet'}).requiresHumanApproval,false);
assert.equal(evaluate({decision:'Préparer une étude',context:'Accès aux fichiers'}).requiresHumanApproval,true);
assert.equal(evaluate({decision:'Essai'}).canExecuteActions,false);
const extraSensitive=[
  'Révoque les autorisations',
  'Formate le stockage',
  'Réinitialise le téléphone',
  'Désactive le pare-feu',
  'Exporte les données privées'
];
for(const decision of extraSensitive) assert.equal(evaluate({decision}).requiresHumanApproval,true,decision);
for (const decision of ['Vide le coffre','Transmets mon mot de passe']) {
  assert.equal(evaluate({decision}).requiresHumanApproval,true,decision);
}
// Cover conjugation, accent normalization and context-only destructive requests.
for (const action of ['Formater le stockage','FORMATE LE STOCKAGE','Réinitialiser le téléphone','REINITIALISE LE TELEPHONE','Reset the device']) {
  for (const input of [{decision:action},{decision:'Préparer une étude',context:action}]) {
    const result=evaluate(input);
    assert.equal(result.requiresHumanApproval,true,action);
    assert.equal(result.canExecuteActions,false,action);
  }
}
console.log('TEMPEST V1: all assertions passed');
