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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.Locale;

/** Native, offline MORPHÉE panel. Launched only from the authenticated ASTOR settings. */
public final class MorpheeActivity extends Activity {
    private static final int CYAN = Color.rgb(124, 229, 239);
    private static final int LIGHT = Color.rgb(218, 239, 247);
    private MorpheeStore store;
    private LinearLayout column;
    private final android.os.Handler idleHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable idleLock = this::finish;

    @Override public void onUserInteraction() {
        super.onUserInteraction();
        idleHandler.removeCallbacks(idleLock);
        idleHandler.postDelayed(idleLock, 60_000L);
    }

    @Override protected void onResume() {
        super.onResume();
        idleHandler.removeCallbacks(idleLock);
        idleHandler.postDelayed(idleLock, 60_000L);
    }

    @Override protected void onPause() {
        idleHandler.removeCallbacks(idleLock);
        super.onPause();
    }

    @Override protected void onDestroy() {
        idleHandler.removeCallbacks(idleLock);
        super.onDestroy();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        store = new MorpheeStore(this);
        MorpheeScheduler.scheduleAll(this);
        render();
    }

    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(8), 0, dp(8));
        return view;
    }

    private void label(String value) { column.addView(text(value, 14, LIGHT)); }

    private Button button(String title, View.OnClickListener click) {
        Button view = new Button(this);
        view.setText(title);
        view.setAllCaps(false);
        view.setTextColor(CYAN);
        view.setOnClickListener(click);
        column.addView(view);
        return view;
    }

    private EditText input(String title, String initial) {
        label(title);
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setTextColor(Color.WHITE);
        edit.setHintTextColor(Color.LTGRAY);
        edit.setText(initial);
        edit.setSelectAllOnFocus(true);
        column.addView(edit);
        return edit;
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.ime());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        scroll.setBackgroundColor(Color.rgb(5, 13, 24));
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(22), dp(26), dp(22), dp(34));
        scroll.addView(column);
        setContentView(scroll);

        TextView title = text("MORPHÉE · V1.0.1", 25, CYAN);
        title.setLetterSpacing(0.10f);
        column.addView(title);
        label("SOMMEIL 23:00 → 09:00 · Préparation 22:30");
        label("Objectifs flexibles. Les durées affichées viennent uniquement de vos saisies.");

        Switch reminders = new Switch(this);
        reminders.setText("Rappels de sommeil activés");
        reminders.setTextColor(LIGHT);
        reminders.setChecked(store.preferences().getBoolean("reminders", true));
        reminders.setOnCheckedChangeListener((view, enabled) -> {
            store.preferences().edit().putBoolean("reminders", enabled).apply();
            MorpheeScheduler.scheduleAll(this);
        });
        column.addView(reminders);

        Switch breaks = new Switch(this);
        breaks.setText("Pauses de développement activées");
        breaks.setTextColor(LIGHT);
        breaks.setChecked(store.preferences().getBoolean("breaks", true));
        breaks.setOnCheckedChangeListener((view, enabled) -> {
            store.preferences().edit().putBoolean("breaks", enabled).apply();
            MorpheeScheduler.scheduleAll(this);
        });
        column.addView(breaks);

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            label("Les notifications nécessitent votre autorisation Android.");
            button("Autoriser les notifications", v -> requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 501));
        }
        label("Rappels non exacts : Android peut les retarder pour économiser la batterie.");

        boolean active = store.preferences().getBoolean("session_active", false);
        label(active ? "Session de développement : en cours" : "Session de développement : arrêtée");
        if (active) {
            button("Terminer la session", v -> { MorpheeScheduler.stopSession(this); render(); });
            button("Reporter la prochaine pause de 15 minutes", v -> {
                MorpheeScheduler.postponeBreak(this, 15);
                Toast.makeText(this, "Pause reportée", Toast.LENGTH_SHORT).show();
            });
        } else {
            button("Démarrer une session de développement", v -> {
                MorpheeScheduler.startSession(this); render();
            });
        }

        column.addView(text("JOURNAL DE SOMMEIL", 19, CYAN));
        EditText date = input("Date d'endormissement (AAAA-MM-JJ)", LocalDate.now().minusDays(1).toString());
        EditText asleep = input("Heure d'endormissement (HH:mm)", "23:00");
        EditText wake = input("Heure du réveil (HH:mm)", "09:00");
        label("Énergie ressentie au réveil (1 = basse, 5 = élevée)");
        Spinner energy = new Spinner(this);
        ArrayAdapter<String> options = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{"1", "2", "3", "4", "5"});
        energy.setAdapter(options);
        energy.setSelection(2);
        column.addView(energy);
        button("Enregistrer cette nuit", v -> {
            try {
                String day = date.getText().toString().trim();
                if (MorpheePolicy.parseDate(day).isAfter(LocalDate.now()))
                    throw new IllegalArgumentException("La date ne peut pas être future");
                store.add(day, asleep.getText().toString().trim(), wake.getText().toString().trim(),
                        energy.getSelectedItemPosition() + 1);
                render();
            } catch (Exception error) { showError("Enregistrement impossible : " + safeMessage(error)); }
        });

        try {
            JSONArray history = store.read();
            renderHistory(history);
            button("Exporter mon historique (partage volontaire)", v -> exportHistory());
            button("Effacer tout mon historique", v -> new AlertDialog.Builder(this)
                    .setTitle("Supprimer l'historique chiffré ?")
                    .setMessage("Cette suppression est définitive. Les rappels resteront configurés.")
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        store.clearHistory(); render();
                    }).show());
        } catch (Exception error) {
            label("Historique indisponible. Il ne sera pas écrasé automatiquement.");
        }
        label("Données conservées localement, chiffrées via Android Keystore, sans sauvegarde cloud.");
        label("MORPHÉE ne fournit aucun diagnostic médical.");
        button("Retour à ASTOR", v -> finish());
    }

    private void renderHistory(JSONArray history) throws Exception {
        LocalDate today = LocalDate.now();
        long total7 = 0, total30 = 0;
        int count7 = 0, count30 = 0;
        for (int i = 0; i < history.length(); i++) {
            JSONObject entry = history.getJSONObject(i);
            LocalDate day = MorpheePolicy.parseDate(entry.getString("date"));
            if (!day.isAfter(today) && !day.isBefore(today.minusDays(29))) {
                total30 += entry.getLong("minutes"); count30++;
                if (!day.isBefore(today.minusDays(6))) {
                    total7 += entry.getLong("minutes"); count7++;
                }
            }
        }
        label("Moyenne 7 jours : " + average(total7, count7)
                + " · 30 jours : " + average(total30, count30));
        label("Entrées enregistrées : " + history.length() + " (appui long pour supprimer)");
        for (int i = history.length() - 1; i >= 0 && i >= history.length() - 30; i--) {
            final int index = i;
            JSONObject entry = history.getJSONObject(i);
            String line = entry.getString("date") + " · " + entry.getString("sleep") + " → "
                    + entry.getString("wake") + " · " + duration(entry.getLong("minutes"))
                    + " · énergie " + entry.getInt("energy") + "/5";
            TextView row = text(line, 13, LIGHT);
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this).setTitle("Supprimer cette entrée ?")
                        .setMessage(line)
                        .setNegativeButton("Annuler", null)
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            try { store.delete(index); render(); }
                            catch (Exception error) { showError("Suppression impossible"); }
                        }).show();
                return true;
            });
            column.addView(row);
        }
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
