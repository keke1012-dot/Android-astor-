const fs=require('fs'),assert=require('assert'),path=require('path');
const read=p=>fs.readFileSync(path.join(__dirname,p),'utf8');
const manifest=read('app/src/main/AndroidManifest.xml'),activity=read('app/src/main/java/fr/astor/hades/PhoneAccessActivity.java'),listener=read('app/src/main/java/fr/astor/hades/PhoneNotificationListener.java');
assert.match(manifest,/<activity android:name="\.PhoneAccessActivity" android:exported="false"/);
assert.match(manifest,/android:name="\.PhoneNotificationListener"[^>]+android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"/);
assert.match(manifest,/android.permission.READ_CALENDAR/);
for(const forbidden of ['WRITE_CALENDAR','QUERY_ALL_PACKAGES','READ_CONTACTS','READ_SMS','CALL_PHONE','MANAGE_EXTERNAL_STORAGE','BIND_ACCESSIBILITY_SERVICE'])assert(!manifest.includes('android.permission.'+forbidden),forbidden);
for(const forbidden of ['onNotificationPosted','onNotificationRemoved','cancelNotification','getContentIntent','send()','FileOutputStream','getActiveNotifications()']){
 if(forbidden==='getActiveNotifications()'){assert.equal(listener.split(forbidden).length-1,1);continue;}
 assert(!listener.includes(forbidden),forbidden);
}
assert.match(listener,/getBoolean\("notifications", false\)/);
assert.match(activity,/requestGeneration\+\+/);assert.match(activity,/PhoneAccessPolicy.allowed/);
assert.match(activity,/Intent.ACTION_INSERT/);assert(!activity.includes('getContentResolver().insert'));
assert.match(activity,/BIOMETRIC_STRONG/);assert.match(activity,/FLAG_SECURE/);
assert.match(activity,/if\(!externalFlow&&!isChangingConfigurations\(\)\)finish\(\)/);
assert(!/https?:|fetch\(|AstorLocalNative|CognitiveEngine|LocalLlm/.test(activity+listener));
console.log('PASS: N2 native gates, protected listener, minimal permissions, user-confirmed calendar handoff and no AI/network data transfer');
