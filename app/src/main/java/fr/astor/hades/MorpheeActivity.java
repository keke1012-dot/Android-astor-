package fr.astor.hades;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.Locale;

/** Native, offline MORPHÉE panel. Access to the diary requires current foreground strong biometrics. */
public final class MorpheeActivity extends Activity {
    private static final int CYAN = Color.rgb(124, 229, 239);
    private static final int LIGHT = Color.rgb(218, 239, 247);
    private MorpheeStore store;
    private LinearLayout column;
    private boolean foreground;
    private boolean authenticating;
    private boolean authorized;
    private long authGeneration;
    private android.os.CancellationSignal authentication;

    private void authenticateDiary() {
        if (!foreground || authenticating || authorized || isFinishing()) return;
        android.hardware.biometrics.BiometricManager manager = getSystemService(
                android.hardware.biometrics.BiometricManager.class);
        int strong = android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
        if (manager == null || manager.canAuthenticate(strong)
                != android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
            new AlertDialog.Builder(this).setTitle("MORPHÉE")
                    .setMessage("La biométrie forte est nécessaire pour ouvrir votre journal.")
                    .setPositiveButton("Retour", (dialog, which) -> finish())
                    .setOnCancelListener(dialog -> finish()).show();
            return;
        }
        authenticating = true;
        final long generation = ++authGeneration;
        authentication = new android.os.CancellationSignal();
        android.hardware.biometrics.BiometricPrompt prompt = new android.hardware.biometrics.BiometricPrompt.Builder(this)
                .setTitle("MORPHÉE · Ouvrir le journal")
                .setSubtitle("Votre journal reste privé")
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
                store = new MorpheeStore(MorpheeActivity.this);
                MorpheeScheduler.scheduleAll(MorpheeActivity.this);
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
        } else authenticateDiary();
    }

    @Override protected void onPause() {
        foreground = false;
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
        super.onDestroy();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        TextView locked = new TextView(this);
        locked.setText("MORPHÉE · Validation biométrique nécessaire");
        locked.setTextColor(LIGHT);
        locked.setBackgroundColor(Color.rgb(5, 13, 24));
        locked.setPadding(dp(22), dp(40), dp(22), dp(22));
        setContentView(locked);
    }

    private static final int BACKGROUND = Color.rgb(6, 14, 26);
    private static final int PANEL = Color.rgb(14, 27, 44);
    private static final int BORDER = Color.rgb(37, 62, 82);
    private static final int MUTED = Color.rgb(158, 180, 197);
    private static final int ACCENT = Color.rgb(99, 221, 210);
    private LinearLayout pageRoot;
    private int page;
    private JSONArray snapshot;

    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    private android.graphics.drawable.GradientDrawable shape(int fill, int stroke, int radius) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (stroke != 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(5), 0, dp(5));
        view.setLineSpacing(dp(3), 1f);
        return view;
    }

    private TextView heading(String value, int size) {
        TextView view = text(value, size, LIGHT);
        view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }

    private void label(String value) { column.addView(text(value, 13, MUTED)); }

    private LinearLayout card(String eyebrow) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(18));
        box.setBackground(shape(PANEL, BORDER, 22));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(14));
        pageRoot.addView(box, params);
        column = box;
        if (eyebrow != null) {
            TextView title = text(eyebrow, 11, ACCENT);
            title.setLetterSpacing(0.14f);
            title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
            box.addView(title);
        }
        return box;
    }

    private Button action(String title, boolean primary, View.OnClickListener click) {
        Button view = new Button(this);
        view.setText(title);
        view.setAllCaps(false);
        view.setTextSize(14);
        view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        view.setTextColor(primary ? BACKGROUND : LIGHT);
        view.setMinHeight(dp(50));
        view.setMinimumHeight(dp(50));
        view.setPadding(dp(14), dp(10), dp(14), dp(10));
        view.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Color.argb(50, 255, 255, 255)),
                shape(primary ? ACCENT : Color.rgb(22, 42, 60), primary ? 0 : BORDER, 14), null));
        view.setOnClickListener(click);
        return view;
    }

    private Button button(String title, View.OnClickListener click) {
        return addButton(title, false, click);
    }

    private Button addButton(String title, boolean primary, View.OnClickListener click) {
        Button view = action(title, primary, click);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(10);
        column.addView(view, params);
        return view;
    }

    private void metric(LinearLayout row, String name, String value, boolean first) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(14), dp(13), dp(14), dp(13));
        tile.setBackground(shape(PANEL, BORDER, 18));
        tile.addView(text(name, 11, MUTED));
        tile.addView(heading(value, 19));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1f);
        if (first) params.rightMargin = dp(10);
        row.addView(tile, params);
    }

    private void render() {
        if (!authorized || !foreground || store == null) return;
        try { snapshot = store.read(); } catch (Exception error) { snapshot = null; }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(BACKGROUND);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        getWindow().getDecorView().setSystemUiVisibility(0);
        pageRoot = new LinearLayout(this);
        pageRoot.setOrientation(LinearLayout.VERTICAL);
        pageRoot.setPadding(dp(20), dp(16), dp(20), dp(26));
        scroll.addView(pageRoot);
        setContentView(scroll);
        column = pageRoot;

        LinearLayout header = new LinearLayout(this);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 32, ACCENT);
        back.setGravity(android.view.Gravity.CENTER);
        back.setContentDescription("Retour à ASTOR");
        back.setBackground(shape(PANEL, BORDER, 16));
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(14), 0, 0, 0);
        TextView title = heading("MORPHÉE", 24);
        title.setLetterSpacing(0.10f);
        brand.addView(title);
        brand.addView(text("ASTOR  /  SOMMEIL & ÉQUILIBRE", 10, ACCENT));
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        pageRoot.addView(header);
        TextView intro = text("Un espace pour ralentir.", 14, MUTED);
        intro.setPadding(0, dp(14), 0, dp(18));
        pageRoot.addView(intro);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        tabs.setBackground(shape(PANEL, BORDER, 18));
        String[] names = {"Aperçu", "Journal", "Réglages"};
        for (int i = 0; i < names.length; i++) {
            final int destination = i;
            Button tab = action(names[i], page == i, v -> { page = destination; render(); });
            tab.setTextSize(12);
            tab.setPadding(dp(4), dp(10), dp(4), dp(10));
            tab.setContentDescription(names[i] + (page == i ? ", onglet sélectionné" : ""));
            tabs.addView(tab, new LinearLayout.LayoutParams(0, -2, 1f));
        }
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(-1, -2);
        tabParams.bottomMargin = dp(18);
        pageRoot.addView(tabs, tabParams);

        if (page == 0) renderOverview();
        else if (page == 1) renderJournal();
        else renderSettings();
        column = pageRoot;
        label("Journal privé · Données saisies par vous");
    }

    private void renderOverview() {
        card("VOTRE RYTHME");
        LinearLayout hero = new LinearLayout(this);
        hero.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout times = new LinearLayout(this);
        times.setOrientation(LinearLayout.VERTICAL);
        times.addView(heading("23:00 → 09:00", 26));
        times.addView(text("Coucher cible  ·  Réveil cible", 12, MUTED));
        hero.addView(times, new LinearLayout.LayoutParams(0, -2, 1f));
        View moon = new View(this) {
            private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);
                float cx = getWidth()/2f, cy = getHeight()/2f, radius = Math.min(cx, cy)*0.78f;
                paint.setStyle(android.graphics.Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1)); paint.setColor(BORDER);
                canvas.drawCircle(cx, cy, radius, paint);
                paint.setStyle(android.graphics.Paint.Style.FILL); paint.setColor(ACCENT);
                canvas.drawCircle(cx-dp(3), cy+dp(2), radius*0.55f, paint);
                paint.setColor(PANEL);
                canvas.drawCircle(cx+dp(6), cy-dp(5), radius*0.53f, paint);
            }
        };
        moon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        hero.addView(moon, new LinearLayout.LayoutParams(dp(64), dp(64)));
        column.addView(hero);
        label("Préparation à 22:30 · Objectifs flexibles");
        label("Préparez votre soirée à votre rythme.");
        addButton("Noter ma dernière nuit", true, v -> { page = 1; render(); });

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        metric(metrics, "MOYENNE · 7 JOURS", windowAverage(7), true);
        metric(metrics, "MOYENNE · 30 JOURS", windowAverage(30), false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(14);
        pageRoot.addView(metrics, params);

        card("DERNIÈRE NUIT NOTÉE");
        if (snapshot == null) label("Historique indisponible. Vos données sont conservées.");
        else if (snapshot.length() == 0) {
            column.addView(heading("Votre premier repère", 20));
            label("Ajoutez une nuit pour retrouver sa durée et votre énergie au réveil.");
        } else {
            try {
                JSONObject last = snapshot.getJSONObject(snapshot.length()-1);
                column.addView(heading(duration(last.getLong("minutes")), 28));
                label(last.getString("date") + "  ·  " + last.getString("sleep") + " → " + last.getString("wake"));
                label("Énergie ressentie : " + last.getInt("energy") + " / 5");
            } catch (Exception error) { label("Cette entrée ne peut pas être affichée."); }
        }
        card("TEMPS DE CONCENTRATION");
        boolean active = store.preferences().getBoolean("session_active", false);
        column.addView(heading(active ? "Session en cours" : "Une pause fait du bien", 20));
        label("Une pause proposée chaque heure pendant vos sessions de développement.");
        if (active) {
            addButton("Terminer ma session", true, v -> { MorpheeScheduler.stopSession(this); render(); });
            button("Reporter la pause de 15 min", v -> {
                MorpheeScheduler.postponeBreak(this, 15);
                Toast.makeText(this, "Pause reportée de 15 minutes", Toast.LENGTH_SHORT).show();
            });
        } else addButton("Commencer une session", true, v -> { MorpheeScheduler.startSession(this); render(); });
    }

    private String windowAverage(int days) {
        if (snapshot == null) return "Indisponible";
        long sum = 0; int count = 0;
        try {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < snapshot.length(); i++) {
                JSONObject entry = snapshot.getJSONObject(i);
                LocalDate day = MorpheePolicy.parseDate(entry.getString("date"));
                if (!day.isAfter(today) && !day.isBefore(today.minusDays(days-1))) {
                    sum += entry.getLong("minutes"); count++;
                }
            }
            return count == 0 ? "—" : average(sum, count);
        } catch (Exception error) { return "Indisponible"; }
    }

    private EditText clockField(String title, String initial, boolean date) {
        label(title);
        EditText edit = new EditText(this);
        edit.setText(initial);
        edit.setTextColor(LIGHT);
        edit.setTextSize(17);
        edit.setSingleLine(true);
        edit.setPadding(dp(14), dp(12), dp(14), dp(12));
        edit.setBackground(shape(BACKGROUND, BORDER, 12));
        edit.setFocusable(false);
        edit.setClickable(true);
        edit.setContentDescription(title + ", choisir");
        edit.setOnClickListener(v -> {
            if (date) {
                LocalDate day = MorpheePolicy.parseDate(edit.getText().toString());
                android.app.DatePickerDialog picker = new android.app.DatePickerDialog(this,
                        (view, year, month, number) -> edit.setText(LocalDate.of(year, month+1, number).toString()),
                        day.getYear(), day.getMonthValue()-1, day.getDayOfMonth());
                picker.getDatePicker().setMaxDate(System.currentTimeMillis()); picker.show();
            } else {
                java.time.LocalTime time = MorpheePolicy.parseTime(edit.getText().toString());
                new android.app.TimePickerDialog(this, (view, hour, minute) ->
                        edit.setText(String.format(Locale.FRANCE, "%02d:%02d", hour, minute)),
                        time.getHour(), time.getMinute(), true).show();
            }
        });
        column.addView(edit, new LinearLayout.LayoutParams(-1, dp(52)));
        return edit;
    }

    private void renderJournal() {
        card("NOTER UNE NUIT");
        column.addView(heading("Comment avez-vous dormi ?", 21));
        label("La date correspond au jour de l’endormissement.");
        EditText date = clockField("Date de la nuit", LocalDate.now().minusDays(1).toString(), true);
        EditText asleep = clockField("Endormissement", "23:00", false);
        EditText wake = clockField("Réveil", "09:00", false);
        label("Énergie au réveil · 1 faible, 5 élevée");
        LinearLayout energies = new LinearLayout(this);
        final int[] selected = {3};
        Button[] levels = new Button[5];
        for (int i = 0; i < 5; i++) {
            final int number = i+1;
            levels[i] = action(String.valueOf(number), number == selected[0], null);
            levels[i].setPadding(0, dp(10), 0, dp(10));
            levels[i].setContentDescription("Énergie " + number + " sur 5");
            levels[i].setSelected(number == selected[0]);
            levels[i].setOnClickListener(v -> {
                selected[0] = number;
                for (int j = 0; j < levels.length; j++) {
                    boolean chosen = j+1 == number;
                    levels[j].setSelected(chosen);
                    levels[j].setTextColor(chosen ? BACKGROUND : LIGHT);
                    levels[j].setBackground(shape(chosen ? ACCENT : Color.rgb(22, 42, 60), chosen ? 0 : BORDER, 12));
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(50), 1f);
            if (i < 4) params.rightMargin = dp(6);
            energies.addView(levels[i], params);
        }
        column.addView(energies);
        addButton("Enregistrer ma nuit", true, v -> {
            try {
                String day = date.getText().toString();
                if (MorpheePolicy.parseDate(day).isAfter(LocalDate.now()))
                    throw new IllegalArgumentException("La date ne peut pas être future");
                store.add(day, asleep.getText().toString(), wake.getText().toString(), selected[0]);
                Toast.makeText(this, "Nuit enregistrée", Toast.LENGTH_SHORT).show();
                render();
            } catch (Exception error) { showError("Enregistrement impossible : " + safeMessage(error)); }
        });
        card("VOS NUITS");
        if (snapshot == null) { label("Historique indisponible. Il ne sera pas écrasé automatiquement."); return; }
        if (snapshot.length() == 0) { label("Aucune nuit enregistrée pour le moment."); return; }
        label(snapshot.length() + " entrée(s) · Les 30 dernières sont affichées.");
        for (int i = snapshot.length()-1; i >= 0 && i >= snapshot.length()-30; i--) {
            final int index = i;
            try {
                JSONObject entry = snapshot.getJSONObject(i);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                row.setBackground(shape(BACKGROUND, BORDER, 14));
                row.addView(heading(entry.getString("date") + "  ·  " + duration(entry.getLong("minutes")), 16));
                row.addView(text(entry.getString("sleep") + " → " + entry.getString("wake")
                        + "   ·   Énergie " + entry.getInt("energy") + "/5", 12, MUTED));
                Button remove = action("Supprimer cette nuit", false, v -> new AlertDialog.Builder(this)
                        .setTitle("Supprimer cette nuit ?").setMessage(entry.optString("date"))
                        .setNegativeButton("Annuler", null)
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            try { store.delete(index); render(); }
                            catch (Exception error) { showError("Suppression impossible"); }
                        }).show());
                remove.setTextSize(12);
                row.addView(remove, new LinearLayout.LayoutParams(-1, -2));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.topMargin = dp(10);
                column.addView(row, params);
            } catch (Exception error) { label("Une entrée ne peut pas être affichée."); }
        }
    }

    private void setting(String title, String subtitle, String key) {
        Switch control = new Switch(this);
        control.setText(title);
        control.setTextSize(15);
        control.setTextColor(LIGHT);
        control.setMinHeight(dp(52));
        control.setPadding(0, dp(8), 0, dp(8));
        control.setThumbTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        control.setChecked(store.preferences().getBoolean(key, true));
        control.setOnCheckedChangeListener((view, enabled) -> {
            store.preferences().edit().putBoolean(key, enabled).apply();
            MorpheeScheduler.scheduleAll(this);
        });
        column.addView(control); label(subtitle);
    }

    private void renderSettings() {
        card("RAPPELS");
        setting("Sommeil et réveil", "22:30 préparation · 23:00 coucher · 09:00 réveil", "reminders");
        setting("Pauses de concentration", "Pendant les sessions que vous démarrez.", "breaks");
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            label("Autorisez les notifications pour recevoir vos rappels.");
            addButton("Autoriser les notifications", true, v -> requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 501));
        }
        label("Android peut retarder les rappels pour économiser la batterie.");
        card("VOTRE JOURNAL");
        column.addView(heading("Vos données restent privées", 20));
        label("Journal chiffré sur ce téléphone. Accès biométrique et fermeture après une minute d’inactivité.");
        if (snapshot != null) {
            button("Exporter mon historique", v -> exportHistory());
            Button delete = button("Effacer tout le journal", v -> new AlertDialog.Builder(this)
                    .setTitle("Effacer tout votre journal ?")
                    .setMessage("Cette suppression est définitive. Les rappels restent configurés.")
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Effacer", (dialog, which) -> { store.clearHistory(); render(); }).show());
            delete.setTextColor(Color.rgb(255, 170, 177));
        } else label("Historique illisible : aucune modification automatique.");
        label("Les moyennes proviennent de vos saisies. Aucune mesure automatique du sommeil.");
        label("MORPHÉE · Interface 1.1");
    }

    private String average(long total, int count) {
        return count == 0 ? "aucune donnée" : duration(Math.round((double) total / count));
    }
    private String duration(long minutes) {
        return String.format(Locale.FRANCE, "%d h %02d", minutes / 60, minutes % 60);
    }
    private String safeMessage(Exception error) {
        return error instanceof IllegalArgumentException ? error.getMessage() : "vérifiez vos données";
    }
    private void showError(String message) {
        new AlertDialog.Builder(this).setTitle("MORPHÉE").setMessage(message)
                .setPositiveButton("OK", null).show();
    }
    private void exportHistory() {
        if (!authorized || !foreground || store == null) return;
        try {
            JSONArray history = store.read();
            StringBuilder csv = new StringBuilder("date;endormissement;reveil;minutes;energie\n");
            for (int i = 0; i < history.length(); i++) {
                JSONObject entry = history.getJSONObject(i);
                csv.append(entry.getString("date")).append(';')
                        .append(entry.getString("sleep")).append(';')
                        .append(entry.getString("wake")).append(';')
                        .append(entry.getLong("minutes")).append(';')
                        .append(entry.getInt("energy")).append('\n');
            }
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, csv.toString());
            startActivity(Intent.createChooser(share, "Exporter mes données de sommeil"));
        } catch (Exception error) { showError("Export impossible : historique illisible"); }
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == 501) render();
    }

    @Override protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations()) finish(); // no private diary left in recents/background
    }
}
