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
- An explainer that ends in a chain you can break yourself: a sample record with
  buttons that reword, delete and reorder its entries, with the verdict coming
  from the same `verify()` a real record goes through. Shown on first launch and
  reachable afterwards from **How this works**.
- Small, medium and large photo sizes above the entry list, remembered between
  sessions. Small is a row with a thumbnail rather than a shrunken card, and the
  seal line and tick are kept at every size.

### Notes

- No `INTERNET` permission, so the app cannot make a network request at all. No
  `CAMERA` permission either; photos come from the system camera. Cloud backup
  and device transfer are disabled.
- The hash field separator is ASCII 31, written as an escape. The first build
  used a literal NUL typed into the source, which was invisible in an editor and
  disagreed with the rule the export file printed. A test now pins it.

[1.0.0]: https://github.com/royalpinto007/Holdfast/releases/tag/v1.0.0
