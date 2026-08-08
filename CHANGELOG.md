# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.0] - 2026-08-08

First release.

### Added

- Records made of sealed entries: each photo hashed together with its note, its
  time and the hash of the entry before it.
- `verify()`, which reports the earliest thing that does not hold and says why
  in plain words. It catches editing, deleting, reordering, inserting,
  backdating, and replacing a photo file.
- Export as plain text that states the hashing rule and prints every hash in
  full, so it can be checked with `sha256sum` by somebody without the app.
- A bento home: each record as a tile carrying its seal state, entry count and
  chain head.

### Notes

- No `INTERNET` permission, so the app cannot make a network request at all. No
  `CAMERA` permission either; photos come from the system camera. Cloud backup
  and device transfer are disabled.
- The hash field separator is ASCII 31, written as an escape. The first build
  used a literal NUL typed into the source, which was invisible in an editor and
  disagreed with the rule the export file printed. A test now pins it.

[1.0.0]: https://github.com/royalpinto007/Holdfast/releases/tag/v1.0.0
