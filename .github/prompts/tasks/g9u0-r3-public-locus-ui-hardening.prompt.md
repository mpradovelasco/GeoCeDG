# Objective

**G9U0-R3 — PUBLIC LOCUS V2 UI EXPOSURE HARDENING**

**CANONICAL IMPLEMENTATION AUTHORITY — AUTHORIZED ONLY BY A SEPARATE AUTHOR
DECISION. THIS FILE DOES NOT CLAIM PASS OR AUTHORIZE G9U1.**

Correct three bounded public-presentation defects after the author-approved
G9U0-R2 implementation:

1. retain the GeoCeDG product-menu actions through the inherited Desktop menu
   font/localization/rebuild lifecycle; and
2. retain the exact persistent intersection-token dependency while keeping its
   auxiliary `GeoText` out of ordinary Euclidian presentation; and
3. decouple the compact admissible-solution chooser label from an arbitrarily
   long opaque exact token so the inspector remains bounded and accessible.

# Authority and evidence hierarchy

- Start from clean `main` at
  `88801ba540cceeaeb1c2366be3c3a8d705f1b09d` unless a later author instruction
  explicitly reconciles a descendant.
- The annotated `geocedg-g9u0-r2-pass` tag must peel to
  `9694dd4c3c274f627839d0eb5d2827a7910bf0ca`, which must be an ancestor.
- The later `Consolidate BOOK-P0-post operations` commit is operational/
  editorial continuity, not competing product authority.
- Work on `feature/g9u0-r3-public-locus-ui-hardening`; require an empty index
  before implementation.

Stop before editing if the R2 tag/ancestry, clean entry or bounded source
characterization cannot be established. Current source/tests, the accepted
public-surface contract and the author-approved R2 tag outrank generated logs or
prior summaries.

# Scope

R3 contains only the Desktop product-menu lifecycle repair, public inspector
accessibility and bounded chooser presentation, exact-token auxiliary
presentation, focused/operational evidence and validated living-document
updates.

# Explicitly forbidden scope

No shared-kernel/intersection semantics, identity, token/XML lifecycle, render
geometry, workspace implementation, candidate markers, G9U1, G9B/G9C/G9U2 or
productive G10 work is authorized.

# Architectural placement

- Menu/inspector exposure belongs to Desktop/application frontend.
- Token auxiliary visibility belongs to the existing dialog/materialization
  creation seam.
- `--enableLocusV2=true` is the sole Locus V2 public opt-in. Do not introduce an
  intersection flag.
- The rich result stays non-Euclidian semantic authority.
- Exact-token points keep the rich result plus opaque token as exact normal-DAG
  inputs. Never re-solve or identify by coordinate, order, index or proximity.
- The chooser uses a localized compact label. Any displayed ordinal belongs
  only to the current dialog snapshot; it is never token identity, persistence
  authority, continuation identity or command input. The complete token may be
  shown only in a wrapping/scrolling read-only diagnostic region and must not
  determine control or dialog width.
- Cancel creates no point or token child; explicit acceptance creates exactly
  one ordinary point. `Intersect(L,T)` alone creates no persistent points.
- The helper token remains auxiliary, reconstructible, persistable and
  copy/remap capable, but not Euclidian-visible.
- No candidate-marker overlay is implemented in R3. That presentation belongs
  prospectively to G9U1.
- Do not move either fix into the kernel, solver, identity ledger, render cache
  or workspace model.

# Required design/specification

Apply `geocedg/specs/locus/locus-v2-public-ui-exposure.md` without modifying
the hash-frozen historical G9U0 public-surface authority.

Use one canonical method to populate the GeoCeDG menu. It must remain correct
after initial construction, repeated full initialization, `updateFonts()`,
repeated font refresh and localization refresh. Preserve the current independent
DXF gate and all approved Locus V2 action semantics/gates.

Use the narrow existing auxiliary-presentation API when creating the persistent
token `GeoText`; do not remove or replace that dependency.

Keep the inspector at a bounded normal-Desktop size with visible OK/Cancel,
accessible chooser naming and normal keyboard selection/confirmation. Selecting
a compact entry must pass the complete unchanged token to the existing public
operation.

# Geometric invariants and degeneracies

The rich result stays non-Euclidian semantic authority. Exact-token points keep
the rich result and opaque token as normal-DAG inputs and never re-solve or
identify by coordinate, order, index or proximity. Cancel and unsupported/
stale/ambiguous states create no child. R3 changes no branch, component,
admissibility, metric, intersection, render discontinuity or generic `Path`
contract.

# Compatibility and serialization

Keep current `.cedg`/`.ggb`, ZIP/XML, `app="classic"`, durable token, copy/remap,
undo/redo and Classic-preservation contracts unchanged. The hidden helper is
still serialized only through the already approved normal dependency model.

# Required tests and commands

Implement and execute R3-M01–R3-M06, R3-I01–R3-I11, R3-T01–R3-T03 and
R3-N01–R3-N02 from
`geocedg/validation/g9u0-r3/g9u0-r3-public-locus-ui-scenarios.json`. Preserve
metric/no-result inspector coverage as focused supporting cases. Test the real
menu action with Algebra selection, single-result discovery, deterministic
multi-result choice, confirmation/cancel, recompute, undo/redo, native
save/reopen and copy/remap.

Run the focused verifier twice with exact canonical-summary comparison, then
G9U0, G9U0-R1, G9U0-R2, G9X1, G5, relevant G9A, legacy Locus, frontend/profile/
localization, Checkstyle, both Git diff checks and the full composed verifier.
Do not weaken an existing gate. Logs belong only under ignored `artifacts/`.

# Required artifacts

Maintain one focused verifier under `tools/agent/` and insert its paired block
after R2 and before future G9U1 in `tools/agent/verify.ps1`. Freeze the scenario
inventory, evidence hashes, exact candidate paths, upstream impact, roadmap,
living traceability, architecture, candidate report and validated guide impact.
Do not rewrite historical G9U0/R1/R2 evidence.

After the complete R3 candidate is green, a planning-only successor may
supersede the historical unexecuted G9U1 prompt. It must require R3 PASS as a
future entry condition and must not execute or authorize G9U1.

# Stop conditions

Prepare, but never self-pass, the author smoke: launch with the one V2 flag;
verify menu entries before/after language/font refresh; create and select a rich
intersection result; open the actual menu inspector; cancel; accept one token;
confirm the chooser/dialog remains bounded with visible keyboard-operable
OK/Cancel even for long tokens; confirm one point and no visible opaque helper;
save/reopen `.cedg` and verify continuity.

Stop immediately if the repair requires shared-kernel semantics, new token/XML
lifecycle, a second menu authority, a candidate-marker implementation or any
G9U1 work. Never commit, push, merge, tag or self-approve without a separate
author instruction.

Terminal state:

```text
G9U0-R3 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR RE-REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```
