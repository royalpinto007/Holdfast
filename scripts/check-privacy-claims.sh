#!/usr/bin/env bash
# The privacy claims on the store listing, as a build gate.
#
# Every line below is something that would quietly turn Holdfast into an app
# that can phone home or lose a record. They fail the build rather than waiting
# for somebody to spot them in review.
set -euo pipefail

cd "$(dirname "$0")/.."
fail=0
note() { printf '%-46s %s\n' "$1" "$2"; }

if grep -qE 'android\.permission\.(INTERNET|ACCESS_NETWORK_STATE)' app/src/main/AndroidManifest.xml; then
  note "network permission" "PRESENT, must not be"
  fail=1
else
  note "network permission" "absent"
fi

if grep -rqE 'HttpURLConnection|OkHttp|Retrofit|java\.net\.(URL|Socket)|okhttp' app/src/main/java/; then
  note "networking code" "PRESENT, must not be"
  fail=1
else
  note "networking code" "none"
fi

if grep -q 'android:permission.CAMERA' app/src/main/AndroidManifest.xml 2>/dev/null; then
  note "camera permission" "PRESENT, the system camera is used instead"
  fail=1
else
  note "camera permission" "absent"
fi

if grep -q 'android:allowBackup="false"' app/src/main/AndroidManifest.xml; then
  note "cloud backup" "disabled"
else
  note "cloud backup" "ENABLED, cases could leave the device"
  fail=1
fi

# The separator is part of the on-disk format. Changing it would make every
# record already exported fail to verify, silently.
if grep -q 'FIELD_SEP = "\\u001F"' app/src/main/java/dev/holdfast/app/Chain.kt; then
  note "hash field separator" "pinned to the documented value"
else
  note "hash field separator" "CHANGED, existing records would not verify"
  fail=1
fi

exit "$fail"
