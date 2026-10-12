"""Pinned Qwen3 1.7B Q4_K_M conversion by bartowski; verify SHA before evaluation."""
import hashlib
import sys
import urllib.request
from pathlib import Path
url='https://huggingface.co/bartowski/Qwen_Qwen3-1.7B-GGUF/resolve/dcb19155b962dbb6389f4691a982043a8e651022/Qwen_Qwen3-1.7B-Q4_K_M.gguf'
expected='72c5c3cb38fa32d5256e2fe30d03e7a64c6c79e668ad84057e3bd66e250b24fb'
target=Path(sys.argv[1]);digest=hashlib.sha256();size=0
with urllib.request.urlopen(url,timeout=120) as response,target.open('wb') as output:
    while chunk:=response.read(1024*1024):
        output.write(chunk);digest.update(chunk);size+=len(chunk)
if size!=1282439584 or digest.hexdigest()!=expected:
    target.unlink(missing_ok=True)
    raise SystemExit('Candidate integrity mismatch')
print('PASS: candidate size and SHA-256 verified')
