(function(root){'use strict';
const LIMIT=120,MAX_DIGITS=90;
function gcd(a,b){a=a<0n?-a:a;b=b<0n?-b:b;while(b){const t=a%b;a=b;b=t;}return a||1n;}
function R(n,d=1n){if(d===0n)throw Error('Division par zéro.');if(d<0n){n=-n;d=-d;}const g=gcd(n,d);n/=g;d/=g;if(String(n).length>MAX_DIGITS||String(d).length>MAX_DIGITS)throw Error('Nombre trop grand pour ce calcul local.');return {n,d};}
const zero=()=>R(0n),one=()=>R(1n);
const add=(a,b)=>R(a.n*b.d+b.n*a.d,a.d*b.d),neg=a=>R(-a.n,a.d),sub=(a,b)=>add(a,neg(b));
const mul=(a,b)=>R(a.n*b.n,a.d*b.d),div=(a,b)=>R(a.n*b.d,a.d*b.n),eq=(a,b)=>a.n===b.n&&a.d===b.d;
function decimal(s){const parts=s.split('.');return R(BigInt((parts[0]||'0')+(parts[1]||'')),10n**BigInt((parts[1]||'').length));}
function fraction(a){return a.d===1n?String(a.n):a.n+'/'+a.d;}
function display(a){let d=a.d;while(d%2n===0n)d/=2n;while(d%5n===0n)d/=5n;if(d!==1n)return fraction(a);let n=a.n<0n?-a.n:a.n;const whole=n/a.d;let rest=n%a.d,digits='';while(rest&&digits.length<30){rest*=10n;digits+=String(rest/a.d);rest%=a.d;}if(rest)return fraction(a);return (a.n<0n?'-':'')+whole+(digits?','+digits:'');}
function money(a){const negative=a.n<0n,n=negative?-a.n:a.n;const cents=(n*100n+a.d/2n)/a.d;return (negative&&cents?'-':'')+cents/100n+','+String(cents%100n).padStart(2,'0');}
const P=a=>[a,zero(),zero()];
function plus(a,b){return a.map((v,i)=>add(v,b[i]));}
function minus(a,b){return a.map((v,i)=>sub(v,b[i]));}
function times(a,b){const full=Array.from({length:5},zero);for(let i=0;i<3;i++)for(let j=0;j<3;j++)full[i+j]=add(full[i+j],mul(a[i],b[j]));if(full[3].n||full[4].n)throw Error('Cette version résout les équations jusqu’au degré 2.');return full.slice(0,3);}
function divide(a,b){if(b[1].n||b[2].n)throw Error('La division par une expression contenant x n’est pas prise en charge : son domaine doit être vérifié.');return a.map(v=>div(v,b[0]));}
function normal(raw){return String(raw).toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[×·]/g,'*').replace(/÷/g,'/').replace(/[−–]/g,'-').replace(/²/g,'^2').replace(/³/g,'^3').replace(/,/g,'.').replace(/\bfois\b/g,'*').trim();}
function prepareExpression(raw){let s=normal(raw);if(!s.includes('=')&&/^[\d\s.()+*/^x%-]+$/.test(s))s=s.replace(/(\d)\s*x\s*(?=\d|\.\d)/g,'$1*');return s;}
function parse(raw){
 const s=prepareExpression(raw);if(!s||s.length>250)throw Error('Expression vide ou trop longue (250 caractères maximum).');
 const tokens=[];let position=0;
 while(position<s.length){if(/\s/.test(s[position])){position++;continue;}const m=s.slice(position).match(/^(?:\d+(?:\.\d*)?|\.\d+|[x+*/^()%-])/);if(!m)throw Error('Notation non reconnue. Utilisez les nombres, x, +, −, ×, ÷, les parenthèses et ^.');tokens.push(m[0]);position+=m[0].length;if(tokens.length>LIMIT)throw Error('Expression trop complexe.');}
 let index=0,depth=0;const peek=()=>tokens[index];const take=()=>tokens[index++];
 function expr(){let a=term();while(peek()==='+'||peek()==='-'){const op=take(),b=term();a=op==='+'?plus(a,b):minus(a,b);}return a;}
 function term(){let a=unary();while(peek()==='*'||peek()==='/'||peek()==='x'||peek()==='('){const implicit=peek()==='x'||peek()==='(';const op=implicit?'*':take(),b=unary();a=op==='*'?times(a,b):divide(a,b);}return a;}
 function unary(){if(++depth>24)throw Error('Expression trop imbriquée.');let a;if(peek()==='+'){take();a=unary();}else if(peek()==='-'){take();a=unary().map(neg);}else a=power();depth--;return a;}
 function power(){let a=atom();if(peek()==='%'){take();a=a.map(v=>div(v,R(100n)));}if(peek()==='^'){take();const b=unary();if(b[1].n||b[2].n||b[0].d!==1n||b[0].n>10n||b[0].n< -10n)throw Error('Exposant entier requis, entre −10 et 10.');const n=Number(b[0].n);if(n===0&&(a[1].n||a[2].n))throw Error('La puissance zéro d’une expression contenant x nécessite de vérifier le domaine et n’est pas prise en charge.');if(n===0&&a.every(v=>v.n===0n))throw Error('0^0 n’est pas défini par ce calculateur.');if(n<0&&(a[1].n||a[2].n))throw Error('Puissances négatives de x non prises en charge.');let value=P(one());for(let i=0;i<Math.abs(n);i++)value=times(value,a);a=n<0?divide(P(one()),value):value;}return a;}
 function atom(){const token=take();if(token==='('){const a=expr();if(take()!==')')throw Error('Parenthèses non équilibrées.');return a;}if(token==='x')return [zero(),one(),zero()];if(token&&/^(?:\d|\.)/.test(token))return P(decimal(token));throw Error('Expression incomplète.');}
 const result=expr();if(index!==tokens.length)throw Error('Expression ambiguë ou parenthèses non équilibrées.');return result;
}
function sqrtInteger(n){if(n<0n)return null;if(n<2n)return n;let x=1n<<BigInt(Math.ceil(n.toString(2).length/2));while(true){const next=(x+n/x)/2n;if(next>=x)return x;x=next;}}
function sqrtRational(a){const n=sqrtInteger(a.n),d=sqrtInteger(a.d);return n!==null&&n*n===a.n&&d*d===a.d?R(n,d):null;}
function numeric(a){return Number(a.n)/Number(a.d);}
function approximation(n){return Number(n.toPrecision(10)).toString().replace('.',',');}
function polynomialText(c){const terms=[];for(let degree=2;degree>=0;degree--){const value=c[degree];if(!value.n)continue;const positive=R(value.n<0n?-value.n:value.n,value.d);let coefficient=degree&&eq(positive,one())?'':display(positive);if(degree&&positive.d!==1n)coefficient='('+coefficient+')';const term=coefficient+(degree===2?'x²':degree===1?'x':'');terms.push((terms.length?(value.n<0n?' − ':' + '):(value.n<0n?'−':''))+term);}return terms.join('')||'0';}
function solveEquation(raw){const source=normal(raw),letters=[...new Set(source.match(/[a-z]/g)||[])];if(letters.length>1)throw Error('Une seule inconnue est prise en charge. Les systèmes demandent un autre solveur.');const variable=letters[0]||'x';if(!['x','y','t'].includes(variable))throw Error('Utilisez une inconnue x, y ou t.');const result=solveXEquation(source.replace(/[yt]/g,'x'));const lines=result.text.split('\n');lines[0]='Équation : '+source;result.text=[lines[0],...lines.slice(1).map(line=>line.replace(/(^|[^a-z])x(?![a-z])/g,(_,prefix)=>prefix+variable))].join('\n');return result;}
function solveXEquation(raw){
 const parts=normal(raw).split('=');if(parts.length!==2||parts.some(p=>!p.trim()))throw Error('Écrivez une égalité complète, par exemple 2x + 3 = 11.');
 const left=parse(parts[0]),right=parse(parts[1]),c=minus(left,right),[constant,b,a]=c;
 const steps=['Équation : '+raw.trim(),'Après regroupement : '+polynomialText(c)+' = 0'];
 if(!a.n&&!b.n)return {kind:'equation',solutions:constant.n?[]:['tous les réels'],text:steps.join('\n')+'\n'+(constant.n?'Aucune solution.':'Égalité vraie pour tout x réel.')};
 if(!a.n){const x=div(neg(constant),b);const verified=eq(add(mul(b,x),constant),zero());if(!verified)throw Error('Vérification du résultat impossible.');return {kind:'equation',solutions:[display(x)],text:steps.join('\n')+'\n'+display(b)+'x = '+display(neg(constant))+'\nx = ('+display(neg(constant))+') / ('+display(b)+')\nx = '+display(x)+'\nVérification exacte : les deux membres sont égaux.'};}
 const delta=sub(mul(b,b),mul(R(4n),mul(a,constant)));steps.push('Δ = b² − 4ac = '+display(delta));
 if(delta.n<0n)return {kind:'equation',solutions:[],text:steps.join('\n')+'\nΔ < 0 : aucune solution réelle (solutions complexes non calculées).'};
 const square=sqrtRational(delta),denominator=mul(R(2n),a);
 if(square){const values=[div(sub(neg(b),square),denominator),div(add(neg(b),square),denominator)];const unique=eq(values[0],values[1])?values.slice(0,1):values;for(const x of unique)if(add(add(mul(a,mul(x,x)),mul(b,x)),constant).n)throw Error('Vérification du résultat impossible.');const solutions=unique.map(display);return {kind:'equation',solutions,text:steps.join('\n')+'\nx = (−b ± √Δ) / (2a)\n'+solutions.map((v,i)=>(solutions.length>1?'x'+(i+1):'x')+' = '+v).join('\n')+'\nVérification exacte : les deux membres sont égaux.'};}
 const av=numeric(a),bv=numeric(b),cv=numeric(constant),root=Math.sqrt(numeric(delta));const q=-0.5*(bv+(bv<0?-root:root));const values=[q/av,cv/q].sort((u,v)=>u-v);
 if(values.some(x=>!Number.isFinite(x)||Math.abs((av*x+bv)*x+cv)>1e-8*Math.max(Number.MIN_VALUE,Math.abs(av*x*x)+Math.abs(bv*x)+Math.abs(cv))))throw Error('Précision numérique insuffisante : gardez la forme exacte.');
 const solutions=values.map(approximation);return {kind:'equation',solutions,text:steps.join('\n')+'\nForme exacte : x = (−('+display(b)+') ± √('+display(delta)+')) / ('+display(denominator)+')\n'+solutions.map((v,i)=>'x'+(i+1)+' ≈ '+v).join('\n')+'\nValeurs approchées à 10 chiffres significatifs ; résidu numérique contrôlé avant arrondi.'};
}
function constant(raw){const p=parse(raw);if(p[1].n||p[2].n)throw Error('Une valeur de x ou une équation complète est nécessaire.');return p[0];}
const number='([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))';
function daily(s){const vat=s.match(new RegExp('^tva\\s*(?:de\\s*)?'+number+'\\s*%\\s*sur\\s*'+number+'$'));if(vat){const result=daily(vat[2]+' ht tva '+vat[1]+'%');result.text='Base interprétée comme montant HT.\n'+result.text;return result;}let m=s.match(new RegExp('^'+number+'\\s*%\\s*(?:de|sur)\\s*'+number+'$'));if(m){const v=mul(decimal(m[1]),div(decimal(m[2]),R(100n)));return {kind:'percentage',text:m[1].replace('.',',')+' % de '+m[2].replace('.',',')+' = '+display(v)};}
 m=s.match(new RegExp('^(remise|reduction|baisse|augmentation|ajoute|ajouter)\\s*(?:de\\s*)?'+number+'\\s*%\\s*(?:sur|de|a)\\s*'+number+'$'));
 if(m){const rate=decimal(m[2]),base=decimal(m[3]);if(rate.n<0n||base.n<0n)throw Error('Utilisez un taux et un montant positifs.');const down=['remise','reduction','baisse'].includes(m[1]);if(down&&rate.n>100n*rate.d)throw Error('Une remise ne peut pas dépasser 100 %.');const amount=mul(base,div(rate,R(100n))),total=down?sub(base,amount):add(base,amount);return {kind:'percentage',text:(down?'Réduction':'Augmentation')+' : '+display(amount)+'\nNouveau montant : '+display(total)+'\nArrondi au centime : '+money(total)};}
 m=s.match(new RegExp('^'+number+'\\s*(ht|ttc)\\s*(?:avec\\s*)?tva\\s*(?:de\\s*)?'+number+'\\s*%$'));
 if(m){const base=decimal(m[1]),rate=decimal(m[3]);if(base.n<0n||rate.n<0n)throw Error('Utilisez un montant et un taux positifs.');const factor=add(one(),div(rate,R(100n))),ht=m[2]==='ht'?base:div(base,factor),ttc=mul(ht,factor);return {kind:'percentage',text:'HT : '+display(ht)+'\nTVA au taux saisi : '+display(sub(ttc,ht))+'\nTTC : '+display(ttc)+'\nTTC arrondi au centime : '+money(ttc)};}
 m=s.match(new RegExp('^(?:partage|partager|repartis)\\s*'+number+'\\s*(?:en|entre|par)\\s*'+number+'$'));if(m){const count=decimal(m[2]);if(count.n<=0n||count.d!==1n)throw Error('Le nombre de parts doit être un entier positif.');const v=div(decimal(m[1]),count);return {kind:'sharing',text:'Par part : '+display(v)+'\nArrondi au centime par part : '+money(v)+' (les arrondis peuvent modifier la somme).'};}
 m=s.match(new RegExp('^(?:convertis?\\s+|convertir\\s+)?'+number+'\\s*(mm|cm|m|km|mg|g|kg|ml|cl|l)\\s*(?:en|vers)\\s*(mm|cm|m|km|mg|g|kg|ml|cl|l)$'));
 if(m){const units={mm:['length',R(1n,1000n)],cm:['length',R(1n,100n)],m:['length',one()],km:['length',R(1000n)],mg:['mass',R(1n,1000n)],g:['mass',one()],kg:['mass',R(1000n)],ml:['volume',R(1n,1000n)],cl:['volume',R(1n,100n)],l:['volume',one()]};if(units[m[2]][0]!==units[m[3]][0])throw Error('Ces unités mesurent des grandeurs différentes.');const value=div(mul(decimal(m[1]),units[m[2]][1]),units[m[3]][1]);return {kind:'conversion',text:m[1].replace('.',',')+' '+m[2]+' = '+display(value)+' '+m[3]};}
 return null;
}
function currency(s){if(!s.includes('€'))return null;if(/€\s*%|%\s*€/.test(s))throw Error('Le taux doit être un pourcentage sans unité monétaire.');if(s.includes('='))throw Error('Les équations avec unités monétaires ne sont pas prises en charge.');
 let m=s.match(new RegExp('^(?:partage|partager|repartis)\\s*'+number+'\\s*€\\s*(?:en|entre|par)\\s*'+number+'$'));
 if(m){const amount=decimal(m[1]),count=decimal(m[2]);if(amount.n<0n||count.n<=0n||count.d!==1n)throw Error('Montant positif et nombre entier positif de parts requis.');const cents=(amount.n*100n+amount.d/2n)/amount.d,low=cents/count.n,extra=cents%count.n;const groups=[];if(extra)groups.push(extra+' part(s) à '+money(R(low+1n,100n))+' €');if(count.n-extra)groups.push((count.n-extra)+' part(s) à '+money(R(low,100n))+' €');return {kind:'sharing',text:'Répartition au centime :\n'+groups.join('\n')+'\nTotal réparti : '+money(R(cents,100n))+' €'+(amount.n*100n%amount.d?'\nLe montant initial a été arrondi au centime.':'')};}
 const everyday=daily(s.replace(/\s*€/g,''));if(everyday&&everyday.kind==='conversion')throw Error('Une somme en euros ne se convertit pas en unité physique.');if(everyday)return {...everyday,text:everyday.text+'\nMontants exprimés en euros.'};
 let value;m=s.match(new RegExp('^'+number+'\\s*€\\s*([+−-])\\s*'+number+'\\s*€$'));if(m)value=m[2]==='+'?add(decimal(m[1]),decimal(m[3])):sub(decimal(m[1]),decimal(m[3]));
 if(!m){m=s.match(new RegExp('^'+number+'\\s*€\\s*([*/])\\s*'+number+'$'));if(m)value=m[2]==='*'?mul(decimal(m[1]),decimal(m[3])):div(decimal(m[1]),decimal(m[3]));}
 if(!m){m=s.match(new RegExp('^'+number+'\\s*\\*\\s*'+number+'\\s*€$'));if(m)value=mul(decimal(m[1]),decimal(m[2]));}
 if(!m){m=s.match(new RegExp('^'+number+'\\s*€$'));if(m)value=decimal(m[1]);}
 if(!value)throw Error('Précisez les unités : par exemple 12,50 € + 3,20 €, 12,50 € × 3 ou partage 10 € en 3.');
 return {kind:'calculation',text:'Valeur exacte : '+display(value)+' €\nArrondi au centime : '+money(value)+' €'};
}
function handle(raw){
 let s=normal(raw).replace(/\bpour\s*cent\b/g,'%').replace(/\beuros?\b/g,'€').replace(/^astor\s*[, :]\s*/,'').replace(/\s*[?!]$/,'').replace(/[,\s]*(?:stp|s['’]il te plait)$/,'').trim();
 s=s.replace(/^peux[- ]tu (?:me )?(calculer|resoudre)\s+/,'$1 ').replace(/^resous\s+l['’]equation\s*:?\s*/,'resous ').replace(/^resoudre\s+l['’]equation\s*:?\s*/,'resoudre ');
 const explicit=/^(?:calcule|calculer|calcul|resous|resoudre|equation|combien (?:font|fait|vaut)|combien ca fait|peux[- ]tu calculer|tu peux calculer)\b/.test(s);
 s=s.replace(/^(?:peux[- ]tu calculer|tu peux calculer|combien (?:font|fait|vaut)|combien ca fait|calculer|calcule|calcul|resoudre|resous|equation)\b\s*:?\s*/,'');
 s=prepareExpression(s);
 const detected=explicit||s.includes('€')||s.includes('=')&&/^[a-z]\s*(?:[=^+*/(-]|\d)/.test(s)||/^[\d.()+\-]/.test(s)&&/[%+*/^=]|(?:\s+x\s+)|\b(?:en|vers|ht|ttc)\b/.test(s)||/^x(?:\b|\^|²)/.test(s)&&s.includes('=')||/^(?:remise|reduction|baisse|augmentation|ajoute|ajouter|partage|partager|repartis|convertis|convertir|tva)\b/.test(s);
 if(!detected)return null;
 if(s.length>250)return {kind:'error',text:'Calcul local · Demande trop longue (250 caractères maximum).'};
 try{const cash=currency(s);if(cash)return cash;const result=daily(s);if(result)return result;if(s.includes('='))return solveEquation(s);if(s.includes('%')&&/[+\-]/.test(s))throw Error('Pour une remise, écrivez « remise de 15 % sur 80 ». Une soustraction avec % est ambiguë.');const value=constant(s);return {kind:'calculation',text:s.replace(/\./g,',')+' = '+display(value)};}catch(error){return {kind:'error',text:'Calcul local · '+error.message+'\nExemples : 17 × 19 ; 20 % de 80 ; remise de 15 % sur 80 ; 2x + 3 = 11 ; x² − 5x + 6 = 0.'};}
}
const api={handle,parse,solveEquation,display,fraction};if(typeof module!=='undefined'&&module.exports)module.exports=api;root.ASTOR_MATH=api;
})(typeof window!=='undefined'?window:globalThis);
