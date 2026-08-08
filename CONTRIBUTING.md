# Contributing to Holdfast

Holdfast keeps a record that can be checked for tampering. That single job sets
every rule below.

## Build

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./scripts/check-privacy-claims.sh
```

## Four rules

**The app cannot reach the network.** There is no `INTERNET` permission and
there must never be one: not for sync, not for backup, not for crash reporting.
A record that proves nothing was altered is worth less if the app can post it
somewhere. CI fails the build if the permission or any networking code appears.

**Nothing leaves the device except by the user's own export.** `allowBackup` is
false and device transfer is excluded, so a case cannot be swept up by Android's
backup either.

**The hash rule never changes silently.** The field separator and the field
order are the on-disk format. Change either and every record anybody has already
exported stops verifying, with no way for them to tell why. If it genuinely must
change, it needs a version field on the entry and a verifier that handles both.

**Every claim in the UI must be one the code actually keeps.** The app says a
record "cannot be quietly changed" and it must not say more than that. It does
not make anything court-admissible; do not add copy that implies otherwise.

## Adding a tamper check

`verify()` reports the earliest failure and why. A new check needs:

- a test that a record doctored that way is caught
- a test that an honest record does **not** trip it
- a `reason` written for a person, not a developer: "its contents changed after
  it was sealed", not "hash mismatch at index 2"

The second test is the one that matters. A verifier that cries wolf on a real
record is worse than one check fewer, because the user learns to ignore it.

## Style

Match the surrounding code. Compose UI in `Design.kt` tokens, no hardcoded
colours or radii. Never type a control character into source: write the escape,
as `FIELD_SEP` does, after that exact bug shipped once already.
