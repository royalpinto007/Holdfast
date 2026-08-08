# Support

Holdfast has no account, no server and no support inbox, because it has no way
to reach one. Everything is on the device.

- **Something is broken:** open an issue.
- **A record shows as broken and you did not change it:** that is the most
  important kind of report. Include the verdict line and the entry number the
  app named, and the hashes from the export if you can. Please do not attach
  the photos.
- **Checking an export by hand:** the export states the hashing rule at the
  top. Each entry's hash is SHA-256 over its id, time in milliseconds, note,
  photo hash, place and previous hash, joined by ASCII 31, with a missing
  photo hash or place written as a single dash.

Security issues go through [SECURITY.md](SECURITY.md) instead.
