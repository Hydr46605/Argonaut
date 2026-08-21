# Changelog

All notable changes to Argonaut are documented here, following [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Calendar Versioning](https://calver.org/) (`YYYY.MM.MICRO`).

## [Unreleased]

### Added

- Nothing yet.

## [2026.08.1] 2026-08-28

### Added

- **Two new home-screen widgets.** A compact `Media` tile (2×1) showing the
  general average, and a tall `Voti` widget (3×3) listing the latest grades
  with dates, in the shared widget design language.
- **Widget redesign** with a consistent visual grammar across every widget.
  Primary-colored section headers, hairline dividers, grade values colored by
  pass/fail threshold, and rounded lesson-hour badges.
- **Tap-to-DidUp.** Widget taps now open the official **DidUp Famiglia** app
  when installed, falling back to Argonaut otherwise.
- **Settings → Widgets → "Mostra nome studente"** toggle that hides or shows
  the student name/class in the Media widgets.
- New black launcher icon. A solid black background with a minimal, centered
  Material 3 book glyph (with a matching monochrome variant for themed icons).

### Fixed

- **Crash on the dashboard for accounts with duplicate grades.** The grade/schedule
  grid used `subject-value-date` (and later plain indices) as item keys, which
  collided when two grades shared a subject, value and day (e.g. two `8.0` in the
  same subject); the resulting `IllegalArgumentException` crash-loop made the app
  appear to "return to login". Keys are now globally-unique and section-prefixed.
- **Oversized, page-filling spinner in the login button** while submitting; the
  progress indicator was only height-constrained and distorted into a huge
  ellipse; it is now a proper small circle.

## [2026.08.0] 2026-08-28

### Added

- **First public release.** Argonaut is a native Android app (Jetpack Compose,
  Material 3) that serves as the premium frontend for the Argos Kotlin client
  library, consuming the undocumented Argo ScuolaNext APIs.
- **Persistent login flow** with inline field validation, loading states, and
  sanitized error messages. Credentials and session tokens are encrypted at
  rest with Android Keystore-backed AES-GCM; the session survives app
  restarts until it expires or the user logs out.
- **Dashboard screen** presenting the `mediaGenerale` in a large elevated card
  with an animated number counter, a color-coded list of recent grades, the
  daily schedule, and summaries of absences and reminders. The layout adapts
  to window size classes (compact / medium / expanded).
- **Settings screen** with Material 3 preference rows (dynamic color opt-in,
  dark mode override, widget refresh frequency, and logout with confirmation).
- **About screen** with CalVer version, credits, source link, and the
  unofficial-client disclaimer.
- **Three Jetpack Glance widgets** themed from the app's Material 3 palette
  via a `WidgetThemeProvider`. A medium grade widget (average + class/name +
  last three grades), a small schedule widget (first three lessons of the
  day), and a tall bulletin widget. Updates are driven by WorkManager with a
  user-configurable period that respects Android 12+ background throttling.
- **Full design system.** Curated light/dark color schemes, refined typography
  scale, expressive shape system, dynamic color on Android 12+ as an opt-in.
- **Architecture.** Single-activity Compose Navigation app, one
  `StateFlow<UiState>` per screen, Hilt DI, repository interfaces delegating to
  Argos (wired as a local composite build), and a strict package layout.
- **Quality gates.** 43 unit tests (including a full SSO wiring integration
  test replaying Argos wire fixtures), Compose UI tests for the login flow,
  Detekt with a strict custom config, Spotless/ktlint formatting, and lint
  with warnings-as-errors. All runnable locally via `./gradlew verifyAll`.
- **CI/release workflows** for automated checks on push/PR and signed APK/AAB
  builds on CalVer tags, attached to GitHub Releases.

### Changed

- Nothing yet.

### Fixed

- Nothing yet.
