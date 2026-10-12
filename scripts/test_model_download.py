"""Download and verify the exact official model used by the Android prototype."""
import hashlib
import sys
import urllib.request
from pathlib import Path

url='https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/resolve/23749fefcc72300e3a2ad315e1317431b06b590a/Qwen3-0.6B-Q8_0.gguf'
expected='9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031'
target=Path(sys.argv[1]);digest=hashlib.sha256();size=0
with urllib.request.urlopen(url,timeout=120) as response,target.open('wb') as output:
    while chunk:=response.read(1024*1024):
        output.write(chunk);digest.update(chunk);size+=len(chunk)
if size!=639446688 or digest.hexdigest()!=expected:
    target.unlink(missing_ok=True)
    raise SystemExit('Model integrity mismatch')
print('PASS: official model size and SHA-256 verified')
