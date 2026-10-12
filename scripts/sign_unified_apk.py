"""Sign a unified APK using the existing private key archive, without printing secrets."""
import argparse,os,re,subprocess,tempfile,zipfile
from pathlib import Path
EXPECTED='9716ec2375cbd8a340b9e8d4b6a0517a62aba0b9185420115ad4f0d96e3a2005'
PACKAGE='fr.astor.hades.unified'
p=argparse.ArgumentParser();p.add_argument('--apk',required=True,type=Path);p.add_argument('--key-archive',required=True,type=Path);p.add_argument('--apksigner',required=True,type=Path);p.add_argument('--out',required=True,type=Path);a=p.parse_args()
if a.out.exists():raise SystemExit('Output already exists; refusing to replace it.')
with zipfile.ZipFile(a.apk) as z:
 m=z.read('AndroidManifest.xml')
 if PACKAGE.encode() not in m and PACKAGE.encode('utf-16-le') not in m:raise SystemExit('Unexpected APK identity; refused.')
with tempfile.TemporaryDirectory(prefix='astor-private-signing-') as private:
 key=Path(private)/'key.p12'
 with zipfile.ZipFile(a.key_archive) as z:
  password=z.read('password.txt').decode().strip()
  if z.read('CERTIFICAT_SHA256.txt').decode().strip().lower()!=EXPECTED:raise SystemExit('Private archive certificate does not match the unified policy.')
  key.write_bytes(z.read('astor-memory.p12'));key.chmod(0o600)
 env=os.environ.copy();env['ASTOR_SIGNING_PASSWORD']=password
 cmd=['java','-jar',str(a.apksigner),'sign','--ks',str(key),'--ks-key-alias','astor-memory','--ks-pass','env:ASTOR_SIGNING_PASSWORD','--key-pass','env:ASTOR_SIGNING_PASSWORD','--out',str(a.out),str(a.apk)]
 result=subprocess.run(cmd,env=env,capture_output=True,text=True)
 if result.returncode: a.out.unlink(missing_ok=True);raise SystemExit('Private signing failed; no APK delivered.')
 result=subprocess.run(['java','-jar',str(a.apksigner),'verify','--verbose','--print-certs',str(a.out)],capture_output=True,text=True)
 values=re.findall(r'certificate SHA-256 digest:\s*([0-9a-fA-F:]+)',result.stdout)
 if result.returncode or not any(v.replace(':','').lower()==EXPECTED for v in values):
  a.out.unlink(missing_ok=True);raise SystemExit('Signed certificate verification failed; no APK delivered.')
print('Unified APK signed and verified with the fixed private certificate.')
