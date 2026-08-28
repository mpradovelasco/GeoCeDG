# G9U0-R3 public Locus V2 UI exposure hardening — closeout report

## Disposition

```text
G9U0-R3 = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS
manualAuthorReSmoke = PASS
manualSmokeObservation = MANUAL SMOKE — FUNCTIONALLY PASSING, UI WIDTH DEFECT FOUND

G9U1 = DESIGNED — NOT AUTHORIZED
G9U0-R4 = PROPOSED BOUNDED INVESTIGATION — NOT EXECUTED / NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

Entry is `88801ba540cceeaeb1c2366be3c3a8d705f1b09d` on
`feature/g9u0-r3-public-locus-ui-hardening`. Annotated R2 tag object
`ec92e2deb6e850bc56e61db4ad169b8af5dc0ec7` peels to
`9694dd4c3c274f627839d0eb5d2827a7910bf0ca`, an ancestor of entry. The sole
intervening commit, `Consolidate BOOK-P0-post operations`, is the already
documented operational/editorial book bridge and does not replace product
authority.

The first complete composed regression reached R2 only after G5, G6–G8,
G9P/O1/A1–A3, historical G9U0/R1 and G9X1 had passed. It then exposed an
operational-only incompatibility: the R2 verifier still accepted only its
protected checkpoint or direct closeout child. It now follows the existing R1/
G9X1 `TAGGED_DESCENDANT` pattern, requiring the immutable annotated tag object,
exact peel/commit shape and ancestry and deriving the frozen 51/26 inventory
from that sealed commit. A focused R2 static rerun, including packaging
association checks, passed before the composed authority was restarted.

## Characterization and bounded correction

The actual public command path already creates a
`GeoLocusIntersectionResult`; the rich result appears in Algebra. The inspector
action and localization exist, and direct/menu dispatch reaches
`GeoCeDGEuclidianController.inspectRichResultSelection()` and the existing
dialog. The public failure was the top-level menu lifecycle: inherited
`updateFonts()` clears every `JMenu`; upstream `BaseMenu` objects rebuild lazily,
but the ordinary GeoCeDG `JMenu` did not. It stayed visible/enabled with zero
items.

`GeoCeDGMenuBar` now owns one `populateProductMenu()` method used by initial/
full initialization and after inherited font/localization clearing. DXF keeps
its independent policy; all five Locus V2 actions still depend only on
`--enableLocusV2=true`. No intersection-specific flag exists or was added.

Explicit exact-token point materialization already uses the rich result and an
opaque token `GeoText` as normal algorithm inputs without re-solving. That helper
was auxiliary but Euclidian-visible. Its creation seam now also marks it
non-Euclidian. Token text, durable identity, DAG, persistence, copy/remap and
undo/redo semantics are unchanged.

The first real author smoke then proved the workflow functionally successful but
exposed one further presentation defect: `TokenChoice.toString()` concatenated
the complete opaque token into each combo-box label. Swing consequently used
unbounded token length to derive the combo-box and dialog preferred width,
placing OK outside the visible desktop.

The bounded correction retains the complete token in `TokenChoice` and passes
that exact string unchanged to `selectIntersectionPoint`. Only `toString()` is
decoupled: it now returns a localized transient solution ordinal plus contact
classification. The ordinal is unique only within the current inspector
snapshot and is never persisted, used for continuation, or passed as command
input. Full tokens remain in the read-only wrapping diagnostic area. The
existing 18-by-72 text viewport bounds normal dialog layout, and the chooser
retains its accessible name and keyboard behavior.

## Automated evidence

Validation results and canonical summary hashes are intentionally recorded only
after their corresponding commands finish. The corrected focused authority
covers 22 R3 cases and 17 existing frontend/profile/localization cases per run.
R3-I10 and R3-I11 use two naturally valid, deliberately long exact tokens to
prove bounded/distinct/accessibly named chooser presentation, unchanged token
materialization and save/reopen persistence, and absence of persisted ordinal
identity. The durable
machine record is
`geocedg/validation/g9u0-r3/g9u0-r3-public-locus-ui-evidence.json`; generated
logs remain below ignored `artifacts/g9u0-r3/`.

The final source boundary is the exact 23-path inventory frozen in the machine
evidence: two Desktop production paths, two focused test paths and 19 bounded
specification, documentation, prompt and operational paths. The one addition to
the earlier 22-path candidate is `tools/agent/verify-repository-state.ps1`:
its self-test fixture now copies the same committed roadmap snapshot used by the
actual-state resolver instead of mixing an uncommitted closeout roadmap with
committed HEAD. This permits the required pre-commit composed closeout while
preserving frozen revision semantics. No generated log, ignored `author-input`
file, branding asset or book-repository path is part of the change.

Corrective focused A and B pass with 39/39 tests each, zero
failures/errors/skips and Checkstyle clean. Their canonical summaries are
byte-identical at SHA-256
`b4dce28910f1a654843a96ee19a291be753928847478354997814d0ab0920a11`;
final closeout log roots are `artifacts/g9u0-r3/closeout/focused-a` and
`artifacts/g9u0-r3/closeout/focused-b`.

The corrective full composed authority then exited 0 with the exact terminal
`All GeoCeDG verification gates passed.`. It reran current G9U0 93/93, R1 6/6,
R2 62/62, G9X1 62/62, G5 10/10, G9A1 117, G9A2 64 and G9A3 253 combined,
plus the complete legacy/current Locus, packaging, frontend, baseline,
Checkstyle and operational contracts. R3 itself passed 39/39 inside composed
with the same canonical summary hash. The final closeout log root is
`artifacts/g9u0-r3/closeout/composed-final`.

Before the author smoke, focused A and B passed with 37/37 tests each, zero failures/errors/skips,
Checkstyle clean and byte-identical canonical summaries, SHA-256
`e04246197d64fabb7aea414624bb6d069d64648a0eea01d16c3d26d07b85b9f4`.
Before that durable pair, two complete green Gradle runs exposed only the new
verifier's JUnit-name spelling variants (`method()` and `method(Path)`); the
parser was bounded to stripping the XML signature suffix and both evidential
runs were repeated from scratch.

The complete composed rerun then passed with exit 0 and terminal
`All GeoCeDG verification gates passed.`. It recorded G9U0 93/93, R1 6/6,
R2 62/62, G9X1 62/62, inherited G5 10/10, G9A1 117, G9A2 64, the G9A3
combined authority 253, and the 76-test complete Locus authority (including 6
legacy characterization and 4 scientific-legacy tests). Packaging, frontend,
baseline, Checkstyle and diff/static contracts also passed. Its ignored log
root is
`artifacts/g9u0-r3/candidate/composed-pre-g9u1-rerun`.

The first composed attempt then proved that the G9U0 public-surface spec is a
hash-frozen historical authority. Its R3 prose edit was removed byte-for-byte;
no historical hash was moved. The bounded additive contract now lives at
`geocedg/specs/locus/locus-v2-public-ui-exposure.md`, while the living matrix
records the prospective U0-I13/marker clarification.

## Prospective G9U1 prompt revision

Only after that full green automation, the frozen G9P prompt
`.github/prompts/tasks/g9u1-construction-workspace.prompt.md` was left
unchanged at canonical-LF SHA-256
`502dabbac1f756e01d0f7935a337e389a3c5e26eaabf3452a6ffe953e83b6ddd`.
The future, unexecuted successor is
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r3.prompt.md`,
canonical-LF SHA-256
`46d2e8011188dd69488f52972eff558dbcb73dfd6fa6e111a9ddf515633f073e`.

The successor requires an author-approved R3 PASS plus separate G9U1
authorization. It assigns active-result finite/admissible candidate markers to
a transient presentation overlay, keeps explicit exact-token one/all-point
materialization, preserves all eleven professional action groups, and requires
a frontend-only visual identity distinct from Classic. It now reserves the two
logical roles `geocedg.brand.topbar` and `geocedg.brand.startup` in the existing
provenance manifest seam. Their intended later author sources are respectively
`helixTopBar.png` and `helixSnapshot.png`; neither file was copied, generated or
integrated. G9U1 must record independent source hashes/dimensions/alpha/
provenance, validate startup-role suitability before small icon derivation and
avoid duplicate packaging copies. G9U1 was neither executed nor authorized,
and R4 was not added as an entry dependency.

A full composed rerun over the pre-width-correction post-R3 candidate, including the
prospective successor prompt and its static contract, again exited 0 with
`All GeoCeDG verification gates passed.`. Its ignored log root is
`artifacts/g9u0-r3/candidate/composed-final`.

## Manual author smoke and re-smoke

The chronology is retained rather than rewritten:

1. the initial R3 candidate passed automated validation;
2. the author smoke confirmed the real menu, sole V2 flag, inspector route,
   Cancel/Accept behavior, hidden token helper and `.cedg` reopen, but recorded
   `MANUAL SMOKE — FUNCTIONALLY PASSING, UI WIDTH DEFECT FOUND`;
3. the bounded long-token layout correction was implemented without changing
   the exact token;
4. replacement focused, deterministic and composed automation passed; and
5. the author re-smoke passed the corrected real Desktop workflow.

The author confirms the menu remains populated through UI/font/localization
refresh, long tokens no longer move OK/Cancel or the compact selector off-screen,
one accepted solution creates exactly one ordinary point from the complete
unchanged token, Cancel creates none, the auxiliary token stays out of Graphics,
and `.cedg` save/reopen preserves the point and dynamic behavior.

This is author approval, not self-approval. It closes R3 only and does not
authorize or execute candidate markers, G9U1, G9U0-R4 or later G9/G10 work.
