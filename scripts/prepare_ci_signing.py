"""Prepare private signing material from CI secrets without printing secrets."""
import base64, os
from pathlib import Path

def java_property(value):
    return ''.join('\\u%04x' % ord(c) if ord(c)>126 else {'\\':'\\\\','\n':'\\n','\r':'\\r','\t':'\\t',' ':'\\ ',':':'\\:','=':'\\='}.get(c,c) for c in value)

names=['ASTOR_KEYSTORE_B64','ASTOR_STORE_PASSWORD','ASTOR_KEY_ALIAS','ASTOR_KEY_PASSWORD']
if any(not os.environ.get(name) for name in names):
    raise SystemExit('Required private signing secrets are missing.')
private=Path(os.environ['RUNNER_TEMP'])/'astor-signing'
private.mkdir(mode=0o700,exist_ok=True)
key=private/'prototype.keystore';key.write_bytes(base64.b64decode(os.environ['ASTOR_KEYSTORE_B64'],validate=True));key.chmod(0o600)
props=private/'signing.properties'
props.write_text('\n'.join(k+'='+java_property(v) for k,v in {'storeFile':str(key),'storePassword':os.environ['ASTOR_STORE_PASSWORD'],'keyAlias':os.environ['ASTOR_KEY_ALIAS'],'keyPassword':os.environ['ASTOR_KEY_PASSWORD']}.items())+'\n');props.chmod(0o600)
with open(os.environ['GITHUB_ENV'],'a') as env:env.write('ASTOR_SIGNING_PROPERTIES='+str(props)+'\n')
print('Private signing configuration prepared.')
