# Holdfast

A record that cannot be quietly changed.

Photograph what you need to prove later. Each photo is sealed to the one before
it, so nothing can be added, removed, reworded or backdated afterwards without
it showing.

[![CI](https://github.com/royalpinto007/Holdfast/actions/workflows/ci.yml/badge.svg)](https://github.com/royalpinto007/Holdfast/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-0C0D10.svg)](app/build.gradle.kts)
[![No network](https://img.shields.io/badge/Network-none-2FBF71.svg)](#no-internet-permission)

## The problem

You move into a flat and photograph the damp patch. Eleven months later the
landlord keeps £600 of the deposit for it. You still have the photo. It changes
nothing, because a photo proves what a room looked like, not when.

Every "timestamped camera" app answers this by burning the date into the pixels.
That is a caption. Anyone can type any date into any image, and the person on the
other side of the dispute knows it.

## What Holdfast does instead

Each entry hashes its photo, its note and its time together with **the hash of
the entry before it**. That makes the record a chain:

- reword a note → its hash changes → every entry after it stops following
- delete an entry → the next one no longer follows
- reorder two → both stop following
- insert one later → the entry after it no longer follows
- swap the photo file → the sealed photo hash no longer matches the bytes
- backdate an entry → it is dated before the one it follows, and is rejected

You cannot quietly repair any of that, because repairing it means recomputing a
chain you have already exported to somebody else.

**It does not make anything court-admissible, and the app never says it does.**
It makes tampering detectable. That is a smaller and honest promise.

## No internet permission

Not "we don't upload", not "we anonymise". `AndroidManifest.xml` declares no
`INTERNET` permission, so the app is incapable of making a network request:
no sync, no backup, no crash reporting.

There is no `CAMERA` permission either. Photos come from the system camera,
which hands back one image and nothing else. Cloud backup and device transfer
are both switched off, so cases do not leave through Android either.

CI fails the build if any of that changes.

## Exports are checkable without the app

An export is a plain text file that states the hashing rule at the top and
prints every hash in full. Someone who does not have Holdfast can recompute the
chain with `sha256sum` and a text editor. A seal only anybody with the app can
verify is a logo, not a seal.

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Kotlin, Jetpack Compose, Material 3. One module, no backend, no dependencies
beyond AndroidX, Compose and kotlinx-serialization.

## Where the logic lives

- **`Chain.kt`** is the whole product claim: sealing, and `verify()`. It is pure
  Kotlin with no Android imports, which is why it can be tested exhaustively.
- **`Vault.kt`** is storage and export. JSON on disk, deliberately readable.
- **`Design.kt`** is the design system: depth as hierarchy, bento tiles, one
  signal green and one break red and nothing else.

The tests are almost entirely about tampering: each of the six ways a record can
be doctored has a test that proves it is caught, plus one that pins the field
separator after a real bug where it was an invisible NUL typed into the source.

## Licence

MIT. See [LICENSE](LICENSE).
