"""Measure repeated five-word sequences; does not claim semantic correctness."""
import json
import re
import sys
from collections import Counter
from pathlib import Path

baseline=json.loads(Path(sys.argv[1]).read_text())
quality=json.loads(Path(sys.argv[2]).read_text())
def score(answer):
    words=re.findall(r"[\wÀ-ÿ]+",answer.lower())
    grams=Counter(tuple(words[i:i+5]) for i in range(max(0,len(words)-4)))
    return {'words':len(words),'repeated_5grams':sum(n-1 for n in grams.values()),'limited':'Réponse limitée' in answer}
result=[]
for old,new in zip(baseline,quality,strict=True):
    assert old['question']==new['question']
    result.append({'question':new['question'],'baseline':{**old,**score(old['answer'])},'quality':{**new,**score(new['answer'])}})
summary={label:{'repeated_5grams':sum(x[label]['repeated_5grams'] for x in result),'limited_answers':sum(x[label]['limited'] for x in result),'mean_milliseconds':round(sum(x[label]['milliseconds'] for x in result)/len(result))} for label in ['baseline','quality']}
report={'note':'10 synthetic questions, fixed host sampling seed 42, identical French instructions. Repetition metrics do not establish factual accuracy or phone speed.','summary':summary,'answers':result}
Path(sys.argv[3]).write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(summary,ensure_ascii=False))
