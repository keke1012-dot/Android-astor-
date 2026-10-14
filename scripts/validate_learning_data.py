"""Validate the prepared data pack without accessing or training a model."""
import hashlib,json,re
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'data'/'learning'
manifest=json.loads((root/'manifest.json').read_text())
for name,sha in manifest['files'].items():
 assert hashlib.sha256((root/name).read_bytes()).hexdigest()==sha, name
rows=[json.loads(line) for line in (root/'train.jsonl').read_text().splitlines()]
evaluation=json.loads((root/'evaluation.json').read_text())
mem=json.loads((root/'memory_candidates.json').read_text())
assert len(rows)==manifest['training_examples']
assert len(evaluation['cases'])==manifest['evaluation_cases']
assert len(mem['entries'])==manifest['memory_candidates']
assert len({r['id'] for r in rows})==len(rows)
assert evaluation['holdout'] and evaluation['do_not_train_on_this_file']
questions={r['messages'][1]['content'].casefold().strip() for r in rows}
assert not questions.intersection(c['question'].casefold().strip() for c in evaluation['cases'])
for r in rows:
 assert [m['role'] for m in r['messages']]==['system','user','assistant']
 assert all(isinstance(m['content'],str) and m['content'].strip() for m in r['messages'])
 assert r['source']=='synthetic_corrected_example'
 assert not re.search(r'sk-(?:proj-)?[A-Za-z0-9_-]{16,}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}',json.dumps(r))
assert all(e['source'] and not e['include_in_training'] for e in mem['entries'])
assert not manifest['model_weights_changed']
print(f"PASS: {len(rows)} training examples, {len(evaluation['cases'])} held-out cases, {len(mem['entries'])} memory candidates. No training executed.")
