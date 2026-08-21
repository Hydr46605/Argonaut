# Argonaut

Argonaut è un client Android nativo per l'elettronico Argo ScuolaNext, costruito con Jetpack Compose e Material 3 sopra la libreria Kotlin [Argos](https://github.com/Hydr46605/Argos).

## Perché esiste

L'app ufficiale (DidUp) non offre widget nella home screen. Argonaut nasce per chiudere quel vuoto e porta la media, l'orario del giorno e gli ultimi voti direttamente sul desktop del telefono, aggiornati in autonomia. In più offre un accesso rapido al registro in un'interfaccia Material 3 curata, senza ricostruire nulla che già funziona.

Tutto avviene **solo sul tuo dispositivo**. Non esiste un server Argonaut, non raccogliamo nulla, non vendiamo nulla. I dati vengono scaricati direttamente dagli endpoint Argo ScuolaNext e salvati in locale, con credenziali e token crittografati via Android Keystore.

Le API di Argo sono **non documentate e soggette a modifiche**. Argonaut si appoggia al lavoro di reverse-engineering della libreria Argos e può rompersi da un giorno all'altro. Non è affiliato a Spaggiari (Argo) in alcun modo.

> **Nota sulla stabilità.** Se l'app smette di funzionare dopo un aggiornamento lato Argo, aggiorna Argos, la compatibilità viene sistemata prima nella libreria.

## Funzionalità

- **Cinque widget** temati dalla palette dell'app e aggiornati da WorkManager con cadenza configurabile in Impostazioni. Media grande, media compatta, orario di oggi, lista voti e bacheca. Un tocco apre l'app ufficiale **DidUp** se installata, con fallback ad Argonaut.
- **Login persistente e criptato.** Le credenziali e la sessione restano sul dispositivo cifrate con AES-GCM via Android Keystore. Accedi una volta e resta loggato fino a scadenza o logout esplicito.
- **Schermata principale.** Media generale con contatore animato, voti recenti colorati per soglia (sufficiente o insufficiente), orario del giorno, assenze e promemoria a colpo d'occhio. Layout adattivo a classi di finestra (compatto, medio, espanso).
- **Theme Material 3 completo.** Palette chiara e scura curate, colori dinamici opt-in su Android 12+, scala tipografica e forme espressive. Edge-to-edge con gestione corretta delle system bar.
- **Impostazioni e Informazioni.** Colori dinamici, override del tema scuro, frequenza di aggiornamento widget, mostra o nascondi nome studente nei widget, logout con conferma, versione CalVer, crediti e disclaimer.

## Installazione

Servono una build locale dalla sorgente.

```bash
# Prerequisiti (JDK 17, Android SDK platform e build-tools 37)
# e il checkout di Argos come cartella sorella (../Argos).

./gradlew :app:installDebug
```

In alternativa, l'attuale release in formato APK/AAB firmato è allegata alla [ultima GitHub Release](https://github.com/Hydr46605/Argonaut/releases).

## Sviluppo

```bash
./gradlew verifyAll   # gate completo (formato, detekt, test, lint, APK)
```

Vedi [CONTRIBUTING.md](CONTRIBUTING.md) per le regole di contribuzione e la procedura di release.

## Costruito su Argos

Il cuore della comunicazione con Argo (autenticazione, HTTP, modelli, repository) vive [nella libreria Argos](https://github.com/Hydr46605/Argos). Argonaut delega sempre ad Argos tramite interfacce repository applicative e non duplica alcuna logica di rete.

## Licenza e disclaimer

Distribuito sotto [MIT](LICENSE).

**Argonaut non è affiliato, sponsorizzato o approvato da Spaggiari / Informatica Sistemi S.p.A. o da Argo ScuolaNext.** È un client indipendente che usa endpoint pubblici non documentati. Usa il tuo account a tuo rischio e per tuo uso personale.

---

*Nota di trasparenza. Questo progetto è stato sviluppato in parte con l'ausilio di strumenti di intelligenza artificiale e coding agentico. Ogni riga è stata però revisionata e curata da un essere umano prima di essere rilasciata.*

---

## English (optional)

Argonaut is a local-only Android app for the Argo ScuolaNext register. Its focus is the home-screen widgets the official app does not provide. It shows your grade average, today's schedule and recent grades, kept fresh in the background, and offers a quick way into the register in a Material 3 UI. Credentials are stored encrypted on-device and nothing is sent anywhere. Built on the Argos Kotlin library, which ports the undocumented Argo APIs. Treat it as best-effort and subject to breakage.