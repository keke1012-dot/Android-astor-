const assert=require('assert'),fs=require('fs'),m=require('./math-local.js');
const html=fs.readFileSync('app/src/main/assets/index.html','utf8');
assert.equal(html.match(/<script id="astorMathEngine">\n([\s\S]*?)\n<\/script>/)[1],fs.readFileSync('math-local.js','utf8'));
function result(s){const v=m.handle(s);assert(v,s);assert.notEqual(v.kind,'error',v.text);return v;}
function exact(s,value){assert.equal(m.display(m.parse(s)[0]),value,s);}
exact('0.1+0.2','0,3');exact('17*19','323');exact('2+3*4','14');exact('(2+3)*4','20');exact('-2^2','-4');exact('(-2)^2','4');exact('2^3^2','512');exact('2^-2','0,25');exact('1/3','1/3');
assert.match(result('20 % de 80').text,/= 16$/);assert.match(result('remise de 15 % sur 80').text,/Nouveau montant : 68/);assert.match(result('augmentation de 20 % sur 80').text,/Nouveau montant : 96/);assert.match(result('96 TTC avec TVA 20 %').text,/HT : 80/);assert.match(result('partage 84 en 4').text,/Par part : 21/);assert.match(result('250 g en kg').text,/0,25 kg/);assert.match(result('convertis 250 ml en l').text,/0,25 l/);
assert.deepEqual(result('2x+3=11').solutions,['4']);assert.deepEqual(result('3x=1').solutions,['1/3']);assert.deepEqual(result('x²-5x+6=0').solutions,['2','3']);assert.deepEqual(result('(x-2)^2=0').solutions,['2']);assert.deepEqual(result('x²+1=0').solutions,[]);assert.deepEqual(result('x=x').solutions,['tous les réels']);assert.deepEqual(result('x=x+1').solutions,[]);assert.match(result('x²=2').text,/Forme exacte/);assert(Math.abs(Number(result('x²=2').solutions[1].replace(',','.'))-Math.sqrt(2))<1e-9);
for(let a=1;a<=12;a++)for(let root=-7;root<=7;root++){const b=3-a*root;assert.deepEqual(result(`${a}x+(${b})=3`).solutions,[String(root)]);}
for(let first=-5;first<=5;first++)for(let second=first;second<=5;second++){const b=-(first+second),c=first*second;const r=result(`x^2+(${b})x+(${c})=0`).solutions.map(Number).sort((a,b)=>a-b);assert.deepEqual(r,first===second?[first]:[first,second]);}
for(const s of ['calcule 1/0','calcule 0^0','calcule 2 +','calcule (2+3','calcule 2 3','x^3=1','x^0=1','1/x=1','y=2','remise de 150 % sur 80','remise de 100,00000000000000000001 % sur 80','80 - 15 %','250 g en l','calcule globalThis.pwned=1','calcule 1;alert(1)','calcule '+'('.repeat(40)+'1'+')'.repeat(40)])assert.equal(m.handle(s).kind,'error',s);
assert.equal(m.handle('Je voudrais apprendre les fractions'),null);assert.equal(m.handle('serveur ia'),null);
console.log('PASS: exact decimal arithmetic, precedence, daily percentages/units, generated linear/quadratic cases, domain restrictions and injection rejection');
