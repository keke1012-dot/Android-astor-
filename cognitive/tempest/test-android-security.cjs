'use strict';
const assert=require('node:assert/strict');
const fs=require('node:fs');
const source=fs.readFileSync('app/src/main/java/fr/astor/hades/MainActivity.java','utf8');
const manifest=fs.readFileSync('app/src/main/AndroidManifest.xml','utf8');
const checks=[
  ['settings trusted gesture',/url\.equals\("astor-native:\/\/settings"\)[\s\S]{0,160}request\.hasGesture\(\)/],
  ['lock trusted gesture',/url\.equals\("astor-native:\/\/lock"\)[\s\S]{0,180}request\.hasGesture\(\)/],
  ['external trusted hub',/hub == view && resumed && HOME\.equals\(view\.getUrl\(\)\) && request\.hasGesture\(\)/],
  ['external https',/"https"\.equals\(uri\.getScheme\(\)\)/],
  ['external host allowlist',/EXTERNAL_HOSTS\.contains\(uri\.getHost\(\)\)/],
  ['external standard port',/uri\.getPort\(\) == -1/],
  ['deny web permissions',/onPermissionRequest\(PermissionRequest request\) \{ request\.deny\(\); \}/],
  ['deny remote webview requests',/403, "Blocked"/],
  ['no webview file access',/setAllowFileAccess\(false\)/],
  ['no webview content access',/setAllowContentAccess\(false\)/],
  ['deny mixed content',/MIXED_CONTENT_NEVER_ALLOW/],
  ['deny ssl errors',/onReceivedSslError[\s\S]{0,110}h\.cancel\(\)/],
  ['microphone runtime permission',/checkSelfPermission\(Manifest\.permission\.RECORD_AUDIO\)/]
];
for(const [label,re] of checks) assert.match(source,re,label);
assert.match(manifest,/android:usesCleartextTraffic="false"/);
assert.match(manifest,/android:allowBackup="false"/);
assert.match(manifest,/android:exported="true"/);
console.log('Android permission/network source invariants: 16 checks passed');
