# Security Policy

## Reporting

Do not open a public issue for a security problem. Use GitHub's
[security advisory form](https://github.com/royalpinto007/Holdfast/security/advisories/new).

## Scope

People will use Holdfast on records that matter to them in a dispute. That sets
the bar for what counts as serious here.

- **Anything that lets a record be altered without `verify()` catching it.**
  This is the whole product. A second preimage, a field-boundary collision, an
  entry that can be inserted or reordered while still verifying: highest
  severity, no exceptions.
- **Anything that gets data off the device.** There is no network permission, so
  a way to exfiltrate through an intent, a content provider, a shared cache file
  or the exported FileProvider is a real finding.
- **Anything that makes `verify()` report Intact when it should not.** A false
  pass is far worse than a false failure, because the user acts on it.
- **Anything that lets another app read the cases**, including through the
  FileProvider, which is meant to expose only an explicitly written export.

Out of scope: the app does not claim legal admissibility, court acceptance, or
that a timestamp is attested by anybody. It claims tampering is detectable.
Reports that it "is not real evidence" are describing the design, not a bug.
