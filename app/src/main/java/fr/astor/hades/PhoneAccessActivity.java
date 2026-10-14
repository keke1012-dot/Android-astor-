package fr.astor.hades;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.widget.*;
import android.database.Cursor;
import java.time.*;
import java.util.*;

/** Native N2 controls. Every capability is off initially and reads only follow user actions. */
public final class PhoneAccessActivity extends Activity {
    private static final int CYAN = Color.rgb(124, 229, 239);
    private static final int LIGHT = Color.rgb(218, 239, 247);
    static final String PREFS = "astor-phone-access-n2";
    private LinearLayout column;
    private boolean externalFlow;
    private String notice = "";
    private long requestGeneration;
    private final java.util.concurrent.ExecutorService worker = java.util.concurrent.Executors.newSingleThreadExecutor();
    private static final int PICK_FILE = 801, EXTERNAL = 802, CALENDAR_PERMISSION = 803;
    private android.content.SharedPreferences preferences() { return getSharedPreferences(PREFS, MODE_PRIVATE); }
    private boolean allowed(String feature) { return PhoneAccessPolicy.allowed(foreground, authorized,
            preferences().getBoolean(feature, false), requestGeneration, requestGeneration); }
    private boolean foreground;
    private boolean authenticating;
    private boolean authorized;
    private long authGeneration;
    private android.os.CancellationSignal authentication;

    private void authenticateAccess() {
        if (!foreground || authenticating || authorized || isFinishing()) return;
        android.hardware.biometrics.BiometricManager manager = getSystemService(
                android.hardware.biometrics.BiometricManager.class);
        int strong = android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
        if (manager == null || manager.canAuthenticate(strong)
                != android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
            new AlertDialog.Builder(this).setTitle("ASTOR · NIVEAU 2")
                    .setMessage("La biométrie forte est nécessaire pour ouvrir ces accès.")
                    .setPositiveButton("Retour", (dialog, which) -> finish())
                    .setOnCancelListener(dialog -> finish()).show();
            return;
        }
        authenticating = true;
        final long generation = ++authGeneration;
        authentication = new android.os.CancellationSignal();
        android.hardware.biometrics.BiometricPrompt prompt = new android.hardware.biometrics.BiometricPrompt.Builder(this)
                .setTitle("ASTOR · N2 · Accès téléphone")
                .setSubtitle("Vos accès restent sous votre contrôle")
                .setAllowedAuthenticators(strong)
                .setNegativeButton("Annuler", getMainExecutor(), (dialog, which) -> finish())
                .build();
        prompt.authenticate(authentication, getMainExecutor(),
                new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(
                    android.hardware.biometrics.BiometricPrompt.AuthenticationResult result) {
                if (generation != authGeneration || !foreground || isFinishing() || isDestroyed()
                        || result.getAuthenticationType() != android.hardware.biometrics.BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC)
                    return;
                authenticating = false;
                authentication = null;
                authorized = true;
                render();
                onUserInteraction();
            }
            @Override public void onAuthenticationError(int code, CharSequence message) {
                if (generation == authGeneration && !isDestroyed()) finish();
            }
        });
    }
    private final android.os.Handler idleHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable idleLock = this::finish;

    @Override public void onUserInteraction() {
        super.onUserInteraction();
        idleHandler.removeCallbacks(idleLock);
        idleHandler.postDelayed(idleLock, 60_000L);
    }

    @Override protected void onResume() {
        super.onResume();
        foreground = true;
        if (authorized) {
            idleHandler.removeCallbacks(idleLock);
            idleHandler.postDelayed(idleLock, 60_000L);
        } else authenticateAccess();
    }

    @Override protected void onPause() {
        foreground = false;
        requestGeneration++;
        column = null;
        authGeneration++;
        if (authentication != null) authentication.cancel();
        authentication = null;
        authenticating = false;
        authorized = false;
        idleHandler.removeCallbacks(idleLock);
        setContentView(new TextView(this));
        super.onPause();
    }

    @Override protected void onDestroy() {
        idleHandler.removeCallbacks(idleLock);
        worker.shutdownNow();
        super.onDestroy();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        TextView locked = new TextView(this);
        locked.setText("ASTOR · NIVEAU 2 · Validation biométrique nécessaire");
        locked.setTextColor(LIGHT);
        locked.setBackgroundColor(Color.rgb(5, 13, 24));
        locked.setPadding(dp(22), dp(40), dp(22), dp(22));
        setContentView(locked);
    }

    private int dp(int n) { return Math.round(getResources().getDisplayMetrics().density * n); }
    private TextView text(String value, int size) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size);
        view.setTextColor(LIGHT); view.setPadding(0, dp(8), 0, dp(8)); return view;
    }
    private android.graphics.drawable.GradientDrawable shape(int color) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(18)); d.setStroke(dp(1), Color.rgb(44,68,90)); return d;
    }
    private LinearLayout section(String title) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(18)); card.setBackground(shape(Color.rgb(14,27,44)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2); params.bottomMargin=dp(16);
        column.addView(card,params); card.addView(text(title,20)); return card;
    }
    private Button button(LinearLayout parent, String title, View.OnClickListener click) {
        Button view = new Button(this); view.setText(title); view.setAllCaps(false); view.setTextColor(CYAN);
        view.setMinHeight(dp(50)); view.setOnClickListener(click); parent.addView(view,new LinearLayout.LayoutParams(-1,-2));return view;
    }
    private void control(LinearLayout parent,String feature,String title) {
        Switch toggle = new Switch(this);toggle.setText(title);toggle.setTextColor(LIGHT);toggle.setMinHeight(dp(50));
        toggle.setChecked(preferences().getBoolean(feature,false));
        toggle.setOnCheckedChangeListener((view,enabled)->{
            if(!foreground||!authorized)return;
            PhoneAccessPolicy.feature(feature);requestGeneration++;
            preferences().edit().putBoolean(feature,enabled).apply();
            if(!enabled&&feature.equals("files"))forgetFile();
            if(feature.equals("notifications")) {
                if(!enabled)PhoneNotificationListener.unbind();
                else try { android.service.notification.NotificationListenerService.requestRebind(
                        new android.content.ComponentName(this,PhoneNotificationListener.class)); }
                catch (SecurityException error) { notice = "Autorisez l’accès aux notifications dans les réglages Android."; }
            }
            render();
        });parent.addView(toggle);
    }
    private void confirm(String feature,String title,String message,Runnable action) {
        if(!allowed(feature))return;
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setNegativeButton("Annuler",null).setPositiveButton("Continuer",(dialog,which)->{
                    if(allowed(feature))action.run();
                }).show();
    }
    private void launch(Intent intent,int request) {
        if(!foreground||!authorized)return;
        externalFlow=true;
        try { startActivityForResult(intent,request); }
        catch(android.content.ActivityNotFoundException|SecurityException error) {
            externalFlow=false;message("Aucune application compatible ou accès refusé.");
        }
    }
    private void message(String value) {
        if(!foreground||!authorized)return;
        new AlertDialog.Builder(this).setTitle("ASTOR · N2").setMessage(value).setPositiveButton("OK",null).show();
    }
    private void render() {
        if(!foreground||!authorized)return;
        requestGeneration++;
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(Color.rgb(6,14,26));
        scroll.setOnApplyWindowInsetsListener((view,insets)->{
            android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.ime());
            view.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;
        });
        column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);column.setPadding(dp(20),dp(20),dp(20),dp(30));
        scroll.addView(column);setContentView(scroll);
        column.addView(text("ACCÈS TÉLÉPHONE · N2",24));
        if (!notice.isEmpty()) column.addView(text(notice,13));
        column.addView(text("Vous choisissez chaque accès. Les lectures s’effectuent à votre demande, sans partage automatique avec l’IA.",14));
        LinearLayout files=section("Fichiers choisis");control(files,"files","Activer cet accès");
        if(allowed("files")) {
            files.addView(text("Un fichier à la fois, choisi dans le sélecteur Android. Le texte reste dans ce panneau.",13));
            button(files,"Choisir un fichier",v->launch(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE).setType("*/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),PICK_FILE));
            button(files,"Lire le fichier choisi",v->readFile(files));
            button(files,"Retirer l’accès au fichier",v->{forgetFile();render();});
        }
        LinearLayout apps=section("Applications");control(apps,"apps","Activer cet accès");
        if(allowed("apps")) {
            apps.addView(text("Ouvrir une application sélectionnée, après confirmation. Aucun contrôle de son contenu.",13));
            button(apps,"Choisir une application",v->chooseApplication());
        }
        LinearLayout calendar=section("Agenda");control(calendar,"calendar","Activer cet accès");
        if(allowed("calendar")) {
            calendar.addView(text("Consultation des 7 prochains jours sur demande. Les nouveaux événements sont validés dans l’application Agenda.",13));
            if(checkSelfPermission(Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)
                button(calendar,"Autoriser la lecture de l’agenda",v->{externalFlow=true;requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},CALENDAR_PERMISSION);});
            else button(calendar,"Consulter les 7 prochains jours",v->readCalendar(calendar));
            button(calendar,"Préparer un rendez-vous",v->eventForm());
        }
        LinearLayout notifications=section("Notifications");control(notifications,"notifications","Activer cet accès");
        if(allowed("notifications")) {
            notifications.addView(text("Android demande un accès spécial à vos notifications. ASTOR affiche les notifications actives seulement lorsque vous le demandez ; aucun historique n’est enregistré.",13));
            button(notifications,"Gérer l’autorisation Android",v->confirm("notifications","Accès aux notifications",
                    "Cette autorisation Android donne accès aux notifications des applications. Vous pouvez la retirer dans ces mêmes réglages. Le texte n’est pas affiché par défaut dans ASTOR.",
                    ()->launch(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),EXTERNAL)));
            button(notifications,"Afficher les applications qui notifient",v->readNotifications(notifications,false));
            button(notifications,"Afficher aussi le texte",v->confirm("notifications","Afficher des données privées ?",
                    "Le texte peut contenir des informations personnelles. Les codes évidents sont masqués, mais ce filtre ne garantit pas la détection de tous les secrets. Rien n’est transmis à l’IA ni enregistré.",
                    ()->readNotifications(notifications,true)));
        }
        LinearLayout privacy=section("Votre contrôle");
        privacy.addView(text("Ces contrôles concernent les accès N2. Les fonctions déjà présentes dans ASTOR gardent leurs propres réglages. Chaque accès commence désactivé. Les autorisations Android peuvent rester accordées après une désactivation ici : retirez-les aussi dans les réglages Android si nécessaire.",13));
        button(privacy,"Désactiver les quatre accès",v->new AlertDialog.Builder(this).setTitle("Désactiver les accès N2 ?")
                .setNegativeButton("Annuler",null).setPositiveButton("Désactiver",(d,w)->{
                    if(!foreground||!authorized)return;
                    requestGeneration++;forgetFile();PhoneNotificationListener.unbind();
                    android.content.SharedPreferences.Editor edit=preferences().edit();
                    for(String key:PhoneAccessPolicy.FEATURES)edit.putBoolean(key,false);edit.apply();render();
                }).show());
        button(privacy,"Autorisations de l’application",v->launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:"+getPackageName())),EXTERNAL));
        button(column,"Retour à ASTOR",v->finish());
    }
    private void forgetFile() {
        String raw=preferences().getString("file_uri","");preferences().edit().remove("file_uri").apply();requestGeneration++;
        if(!raw.isEmpty())try{getContentResolver().releasePersistableUriPermission(Uri.parse(raw),Intent.FLAG_GRANT_READ_URI_PERMISSION);}
        catch(SecurityException ignored){}
    }
    private TextView output(LinearLayout parent) {
        TextView result=text("Lecture en cours…",13);result.setTextIsSelectable(true);parent.addView(result);return result;
    }
    private boolean current(String feature,long generation) {
        return PhoneAccessPolicy.allowed(foreground,authorized,preferences().getBoolean(feature,false),generation,requestGeneration);
    }
    private void readFile(LinearLayout parent) {
        if(!allowed("files"))return;String raw=preferences().getString("file_uri","");
        if(raw.isEmpty()){message("Choisissez d’abord un fichier.");return;}
        TextView view=output(parent);long generation=requestGeneration;
        worker.execute(()->{
            String content;
            try {
                org.json.JSONObject file=SelectedFileReader.read(getContentResolver(),Uri.parse(raw));
                content=file.optString("name")+"\n"+file.optString("notice")+"\n"+file.optString("text").substring(0,Math.min(8000,file.optString("text").length()));
            }catch(Exception error){content="Fichier inaccessible : choisissez-le à nouveau.";}
            final String result=content;runOnUiThread(()->{if(current("files",generation))view.setText(result);});
        });
    }
    private void chooseApplication() {
        if(!allowed("apps"))return;
        Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list=getPackageManager().queryIntentActivities(query,0);
        list.sort(Comparator.comparing(item->item.loadLabel(getPackageManager()).toString(),String.CASE_INSENSITIVE_ORDER));
        LinkedHashMap<String,String> choices=new LinkedHashMap<>();
        for(ResolveInfo item:list)if(item.activityInfo!=null&&!item.activityInfo.packageName.equals(getPackageName()))
            choices.putIfAbsent(item.activityInfo.packageName,PhoneAccessPolicy.text(item.loadLabel(getPackageManager()).toString(),100));
        if(choices.isEmpty()){message("Aucune application ouvrable trouvée.");return;}
        List<String> packages=new ArrayList<>(choices.keySet());
        String[] names=choices.values().toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("Choisir une application").setItems(names,(dialog,index)->{
            if(!allowed("apps"))return;
            String packageName=packages.get(index);
            confirm("apps","Ouvrir "+names[index]+" ?","ASTOR se verrouillera avant le passage à cette application.",()->{
                Intent open=getPackageManager().getLaunchIntentForPackage(packageName);
                if(open==null)message("Cette application ne peut pas être ouverte.");else launch(open,EXTERNAL);
            });
        }).setNegativeButton("Annuler",null).show();
    }
    private void readCalendar(LinearLayout parent) {
        if(!allowed("calendar")||checkSelfPermission(Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)return;
        TextView view=output(parent);long generation=requestGeneration;
        worker.execute(()->{
            StringBuilder result=new StringBuilder();
            try {
                long start=System.currentTimeMillis(),end=start+7*24*60*60_000L;
                Uri.Builder builder=CalendarContract.Instances.CONTENT_URI.buildUpon();ContentUris.appendId(builder,start);ContentUris.appendId(builder,end);
                try(Cursor rows=getContentResolver().query(builder.build(),new String[]{CalendarContract.Instances.TITLE,CalendarContract.Instances.BEGIN,CalendarContract.Instances.END},null,null,CalendarContract.Instances.BEGIN+" ASC")) {
                    int count=0;
                    if(rows!=null)while(rows.moveToNext()&&count++<50) {
                        ZonedDateTime when=Instant.ofEpochMilli(rows.getLong(1)).atZone(ZoneId.systemDefault());
                        result.append(when.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))).append(" · ")
                                .append(PhoneAccessPolicy.text(rows.getString(0),200)).append('\n');
                    }
                    if(count==0)result.append("Aucun événement trouvé sur les 7 prochains jours.");
                }
            }catch(Exception error){result.setLength(0);result.append("Lecture impossible : vérifiez l’autorisation Agenda.");}
            final String content=result.toString();runOnUiThread(()->{if(current("calendar",generation))view.setText(content);});
        });
    }
    private void eventForm() {
        if(!allowed("calendar"))return;
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),dp(8),dp(20),dp(8));
        EditText title=field(form,"Titre","");EditText date=field(form,"Date (AAAA-MM-JJ)",LocalDate.now().plusDays(1).toString());
        EditText time=field(form,"Heure (HH:mm)","09:00");EditText minutes=field(form,"Durée en minutes","60");
        minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this).setTitle("Préparer un rendez-vous").setView(form).setNegativeButton("Annuler",null)
                .setPositiveButton("Vérifier",(dialog,which)->{
                    if(!allowed("calendar"))return;
                    try{
                        String name=PhoneAccessPolicy.text(title.getText().toString(),160);
                        if(name.isEmpty())throw new IllegalArgumentException("Indiquez un titre");
                        long begin=PhoneAccessPolicy.eventStart(date.getText().toString().trim(),time.getText().toString().trim(),ZoneId.systemDefault());
                        long end=PhoneAccessPolicy.eventEnd(begin,Integer.parseInt(minutes.getText().toString()));
                        confirm("calendar","Préparer cet événement ?",name+"\n"+date.getText()+" à "+time.getText()+"\nDurée : "+minutes.getText()+" min\n\nLa sauvegarde finale se fait dans Agenda.",()->
                                launch(new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(CalendarContract.Events.TITLE,name).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,begin)
                                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,end),EXTERNAL));
                    }catch(Exception error){message("Vérifiez le titre, la date, l’heure et la durée (5 à 1440 minutes).");}
                }).show();
    }
    private EditText field(LinearLayout parent,String title,String initial) {
        parent.addView(text(title,13));EditText input=new EditText(this);input.setSingleLine(true);input.setText(initial);input.setTextColor(LIGHT);parent.addView(input);return input;
    }
    private void readNotifications(LinearLayout parent,boolean includeText) {
        if(!allowed("notifications"))return;
        try{
            List<String> messages=PhoneNotificationListener.snapshot(this,includeText);
            TextView view=text(messages.isEmpty()?"Aucune notification lisible active.":String.join("\n\n",messages),13);
            view.setTextIsSelectable(true);parent.addView(view);
        }catch(Exception error){message("Accès non connecté : autorisez ASTOR dans les réglages d’accès aux notifications, puis revenez.");}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);externalFlow=false;
        if(request!=PICK_FILE||result!=RESULT_OK||data==null||data.getData()==null||!preferences().getBoolean("files",false))return;
        Uri uri=data.getData();if(!"content".equals(uri.getScheme())||(data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)==0)return;
        try{
            getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String old=preferences().getString("file_uri","");if(!old.equals(uri.toString()))forgetFile();
            preferences().edit().putString("file_uri",uri.toString()).apply();
        }catch(SecurityException error){notice="Ce fournisseur ne permet pas un accès durable. Le fichier précédent reste sélectionné ; choisissez un fichier local.";}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);externalFlow=false;
        if(foreground&&authorized)render();
    }
    @Override protected void onStop() {
        super.onStop();if(!externalFlow&&!isChangingConfigurations())finish();
    }
}
