# Demo record

Builds the record used in the Play Store screenshots: three drawn images and a
chain over them that the app verifies as intact.

```sh
python3 seed-demo-record.py          # writes p1.jpg p2.jpg p3.jpg, rewrites case.json

adb install -r ../../app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.holdfast.app/.MainActivity   # so the app's files dir exists
adb shell run-as dev.holdfast.app mkdir -p files/cases/3b7a91c4
for f in case.json p1.jpg p2.jpg p3.jpg; do
  adb push "$f" /data/local/tmp/$f
  adb shell "run-as dev.holdfast.app cp /data/local/tmp/$f files/cases/3b7a91c4/$f"
done
adb shell am force-stop dev.holdfast.app
adb shell am start -n dev.holdfast.app/.MainActivity
```

The photos are drawn, not photographed, so the repo carries no pictures of
anybody's flat. The chain is real: the app runs the same `verify()` over this
record as over one you seal yourself, and the head hash on screen is the one
this script prints.

`seed-demo-record.py` reimplements the hashing rule from `Chain.kt` in Python.
That duplication is deliberate, since a second implementation is a check on the
first, and it is safe to keep because divergence is loud: the app would show the
record as broken rather than quietly accept it.
