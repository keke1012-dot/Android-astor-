# ASTOR 0.2.6 — Patch A : protocole d'instrumentation

Branche isolée : `feature/astor-0.2.6-optimisation`. Ne pas modifier RC1, main ou les sauvegardes.

## Points vérifiés dans le code
- Java : `LocalLlm.generate` utilise un exécuteur mono-thread et appelle `nativeGenerate`.
- C++ : `astor_local.cpp` initialise le backend, charge le modèle si nécessaire, tokenise, crée un contexte, exécute le préremplissage puis une boucle de 128 tokens maximum.
- Paramètres actuels : `n_ctx=1024`, `n_batch=32`, `n_ubatch=32`, `n_threads=4`.
- Le plafond de 128 tokens est bien dans le moteur C++ : ne pas le modifier pendant Patch A.

## Mesures à implémenter (sans texte du prompt)
Horloge monotone : Java `SystemClock.elapsedRealtimeNanos()` et C++ `std::chrono::steady_clock`; ne jamais soustraire directement des valeurs d'horloges différentes.

Java : `request_received`, `worker_started`, `jni_enter`, `jni_return`, `ui_callback_queued`, `ui_callback_rendered`.
C++ : `native_enter`, `model_loaded`, `tokenized`, `context_ready`, `prefill_done`, `first_token_sampled`, `native_done`.

Calculer les durées à l'intérieur de chaque couche puis rapprocher les segments par un identifiant aléatoire de requête. Ne journaliser ni prompt, ni réponse, ni chemin local du modèle, ni données privées. Journaliser uniquement durées, modèle (catégorie 0.6B/1.7B), nombres de tokens et codes d'erreur non sensibles. Logs désactivés par défaut en production ; activation explicite sur build de diagnostic.

## Validation
- Trois essais identiques sur Qwen3 1.7B, appareil Samsung Android 16.
- Comparer les médianes et la moyenne à la référence 8/8/8 secondes.
- Vérifier annulation, timeout, fonctionnement hors connexion, stabilité et absence de fuite des contenus.
- Aucun changement de paramètres d'inférence avant la mesure de référence instrumentée.
