**A record that cannot be quietly changed.**

Photograph what you may have to prove later. Every entry is hashed together
with its note, its time and the hash of the entry before it, so rewording a
note, deleting an entry, reordering two, slipping one in afterwards, swapping a
photo file or backdating an entry all show up, at the exact entry where it
happened.

### Try it before you trust it

Open the app and there is a sample record with three sealed entries and buttons
that break it: reword one, delete one, swap two. The verdict underneath comes
from the same check that runs on your own records, so you can watch it catch
each kind of tampering before you rely on it.

### It cannot send your records anywhere

Not "we don't upload them". Holdfast declares **no internet permission at all**,
so it is incapable of making a network request: no account, no sync, no cloud
backup, no crash reporting, no analytics. Android's own backup and device
transfer are switched off too.

CI fails the build if the network permission, any networking library, the backup
flag or the hash separator ever changes, and this release additionally checks
the built APK rather than only the source.

### Exports anyone can check

Handing the record over writes a plain text file that states the hashing rule at
the top and prints every hash in full. Somebody who has never installed this app
can recompute the whole chain with `sha256sum` and a text editor.

### What it does not claim

It does not make anything court-admissible and does not certify your timestamps
against an outside authority. It makes tampering detectable, which is a smaller
promise and one it keeps.

It also cannot stop somebody fabricating a record from scratch, because they
hold the phone and the file. What it stops is a record changing *after somebody
else has seen it*, which is why the export is worth handing over on the day you
make it rather than the day you need it.

### Installing

`holdfast-v1.0.0.apk` is a sideload build, signed with the Android debug key.
That is enough for Android to install it and not enough for Google Play, which
needs its own upload key. If you would rather not install a debug-signed APK,
build it yourself: `./gradlew :app:assembleRelease`.

Requires Android 8.0 or newer.
