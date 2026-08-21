# Contribuire ad Argonaut

Grazie per voler contribuire. Argonaut è un progetto piccolo e orientato allo sviluppo personale, preferisce classi piccole e focalizzate, contratti espliciti e una storia lineare e pulita.

## Regole base

- **Commit atomici e Conventional.** Prefissi `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:`, `ci:`, `build:`, `perf:`, `revert:` con scope opzionale, es. `fix(widget): gestisci la scadenza della sessione`. Niente commit WIP, niente modifiche confuse in un unico commit.
- **L'UI è una funzione pura dello stato.** Ogni schermata ha un ViewModel con un solo `StateFlow<UiState>` (sealed, `Loading` / `Success` / `Error`). I composable usano la convenzione PascalCase; detekt e ktlint sono configurati per accettarla.
- **Mai chiamare Argos da un ViewModel.** Passa sempre dalle interfacce repository applicative (`AuthRepository`, `DashboardRepository`, `SettingsRepository`) che delegano al client Argos.
- **Nessuna logica di business duplicata.** Autenticazione, HTTP e modelli vivono in Argos. Argonaut mappa i modelli Argos in modelli UI leggeri quando la presentazione lo richiede.

## Build

Prerequisiti

- JDK 17 (il progetto compila a 17 e detekt rifiuta JVM più nuove del launcher).
- Android SDK con platform 37 e build-tools 37 (`sdk.dir` in `local.properties` se `ANDROID_HOME` non è impostata).
- Il checkout di **Argos** come cartella sorella (`../Argos`). La composite build sostituisce `it.hydr4:Argos` con il checkout locale, così si compila sempre contro l'ultima versione locale di Argos.

```bash
# Gate locale completo (formattazione, detekt, test, lint, APK).
./gradlew verifyAll

# Single gate
./gradlew spotlessApply          # auto-format Kotlin e script Gradle
./gradlew :app:detekt            # analisi statica
./gradlew :app:testDebugUnitTest # test unitari JVM
./gradlew :app:lintDebug         # lint Android
```

## Release

Le release seguono Calendar Versioning (`YYYY.MM.MICRO`) e Keep a Changelog.

1. Aggiorna `CHANGELOG.md` spostando le voci di `Unreleased` sotto una nuova intestazione `## [YYYY.MM.MICRO] YYYY-MM-DD`.
2. Aggiorna `version=` in `gradle.properties` (e `argos` in `gradle/libs.versions.toml` se Argos è avanzato).
3. Commit come `chore(release): 2026.08.x` e tag `v2026.08.x`.
4. Il workflow di release compila APK/AAB firmati dai secret e li allega a una GitHub Release.

Firma locale, esegui `./gradlew createReleaseKeystore` una volta; scrive il `keystore.properties` (gitignorato) e un keystore self-signed. La CI invece decodifica il keystore dai secret.

## Testing

- Test unitari in `app/src/test` (pura JVM, `kotlinx-coroutines-test` con fake, nessuna dipendenza da Android).
- Test strumentali in `app/src/androidTest` (Hilt + Compose Test).
- Test di integrazione che riproducono le fixture Argos tramite un motore HTTP finto, così il cablaggio Argonaut→Argos è verificato senza rete.

## Codice di condotta

Sii rispettoso; vedi [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).