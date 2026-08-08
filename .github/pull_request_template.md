## What this changes

<!-- One or two sentences. -->

## Why

<!-- The problem, not the patch. -->

## Checks

- [ ] `./gradlew :app:testDebugUnitTest` passes
- [ ] `./scripts/check-privacy-claims.sh` passes
- [ ] Built and run on a device or emulator, not just compiled

## If this touches the chain

Changing `Chain.kt` changes what an already-exported record verifies against,
so records people are holding today would stop checking out.

- [ ] The hashing rule and the field separator are unchanged, **or** the change
      is deliberate and the version and export header say so
- [ ] The tampering tests still cover all six cases: reword, delete, reorder,
      insert, swap photo, backdate
