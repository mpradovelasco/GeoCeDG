# PRE-G9B-R6-plus-C closeout record

```text
PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED
AUTHOR_SMOKE      = PASS WITH ACCEPTED PRE-EXISTING DEBT
```

```text
selfApproved              = false
authorApproved            = true    (C only, revision 1 technical candidate)
passClaimed               = true    (C only)
implementationAuthorized  = false   (E1, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B,
                                     the post-C Classic activity, the open
                                     observations and the enhancements below)

candidateFrozen           = true
candidateMutated          = false
SERIALIZATION CHANGE      = NONE
OUTPUT FORMAT / SCHEMA    = NONE beyond the original C candidate
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-06 on
`PRE-G9B-R6-plus-C`, with the exact identities behind it. It changes no
product, test, build, packaging, verifier, registry, schema, specification,
ADR, Classic or upstream file, and it does not modify any frozen candidate.
Besides this record and its machine-readable mirror,
[`pre-g9b-r6-plus-c-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_c_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen technical state. They are not
amended. This record is the sole authority for the author approval of `C`.

## Author disposition

```text
PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED
T_R6PLUS_C        = 6248bf0fc4208540de09d26a7755d4163055ac76
                    tree 03038ae305716408aa404fd15dd40e71bf7f5f51
AUTHOR_SMOKE      = PASS WITH ACCEPTED PRE-EXISTING DEBT
selfApproved      = false
```

The author instruction of 2026-10-06 (pasted closeout instruction, confirmed
directly in chat) approves exactly revision 1, `T_R6PLUS_C` `6248bf0f`, and
authorizes exclusively this documentary closeout and the publication of the
complete linear chain below. Revision 1 is the only candidate approved. No
additional product fix is authorized.

### Author smoke

| Item | Result |
|---|---|
| DXF hidden objects and hidden layers | `PASS`: an individually hidden object keeps group `60 = 1`; a hidden layer is written OFF; no change to `DQ-C3` |
| `C-UX-1` compact non-physical statement | `PASS` — accepted |
| `C-UX-2` default DXF file name | `PASS` — accepted: the document stem with `.dxf`, `geocedg-export.dxf` without a document file |
| normal physical export | `PASS` — regression accepted |
| Classic diagnostic "Graphics View as Picture" | empty dialog and hang **reproduced by the author**; accepted as pre-existing debt (below) |

The earlier `C` smoke results remain accepted evidence and are not repeated.

### Accepted pre-existing Classic debt

```text
OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT
  = REPRODUCED BY AUTHOR
  = PRE-EXISTING
  = NOT CAUSED BY C
  = ACCEPTED DEBT
  = NOT C-BLOCKING
  = DEFERRED TO POST-C
```

The author states and this record preserves, explicitly:

1. The author reproduced the empty dialog and hang in the Classic diagnostic
   session (File → Export → Graphics View as Picture (png, svg) ...).
2. The base-versus-candidate investigation of revision 1 (candidate report
   §21.2: 18 runs, 9 on the candidate and 9 on the base `e6135028`) establishes
   the defect as pre-existing and not caused by `C`: upstream `FileMenuD`
   builds and shows `GraphicExportDialog` on a background thread, and the same
   EDT layout exceptions occur on the base and on `C`; no `C` frame appears in
   any stack.
3. The author accepts it as non-blocking debt for `C`.
4. It is deferred, under `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`, to a
   separate post-`C` activity.
5. No Classic or upstream code was changed by `C`, by its revision 1 or by this
   closeout; `FileMenuD` is untouched.
6. Accepting `C` does not mean the Classic defect is resolved. It remains open.

The smoke is therefore not recorded as fully clean: `AUTHOR_SMOKE = PASS WITH
ACCEPTED PRE-EXISTING DEBT`.

## Candidate history

| Candidate | Commit / tree | Acceptance runs | Role |
|---|---|---|---|
| `T_R6PLUS_C_R1` (original) | `b981f8eac9dd7b320376e07345b8c1c483de927a` / `a07159fc0e8e746d6a627b7107c07cc88cafc422` | `PHASE` `verification-6c51394a67ff478e80cbe85991fe46cd` and `INTEGRATION` `verification-da457f5a4c7d4c378077ca28b66de492` (both `ACCEPTED / COMPLETE`) | historical evidence of that tree only |
| `T_R6PLUS_C` (revision 1) | `6248bf0fc4208540de09d26a7755d4163055ac76` / `03038ae305716408aa404fd15dd40e71bf7f5f51` | `PHASE` `verification-a198a69b95cf4fb69e7f3c13818f19b3` and `INTEGRATION` `verification-0bcf970971c74d1e9cb329b81b8aa0dc` | **authoritative; author approved** |

No frozen candidate was rewritten or amended.

## Approved identities

```text
P_R6PLUS_A2         = e613502831e3b412780d69424d4a1e433a4ae688
                      tree 48b00681a727ea4bd8768c3e9b0159bc01573ff0
                      published A-2 closeout; publication base
T_R6PLUS_C_PREP     = 5ad96304ae9718c296db753b4822130cf3d99b61
                      tree 1596b5153681828c9c3206b98ca9ebd31505e765
                      C preparation package (kept intact)
D_R6PLUS_C_PROMPT   = 6cb09d55e1ce9b18ef046f445da5494288fd8ecd
                      tree 00f80cc75db6de6542bb295cef813e70c9823b9c
                      reconciled preparation package; PASS — AUTHOR APPROVED
A_R6PLUS_C_PROMPT   = 75a2cf06b62afbd20f6e3a3fc0e079579a3cab2a
                      tree 06a670f8812105a48db7ef57b081cc05f8943089
                      implementation authorization record
T_R6PLUS_C_R1       = b981f8eac9dd7b320376e07345b8c1c483de927a
                      tree a07159fc0e8e746d6a627b7107c07cc88cafc422
                      original candidate; historical evidence only
T_R6PLUS_C          = 6248bf0fc4208540de09d26a7755d4163055ac76
                      tree 03038ae305716408aa404fd15dd40e71bf7f5f51
                      parent b981f8eac9dd7b320376e07345b8c1c483de927a
                      approved technical candidate (revision 1); not amended

branch              = phase/pre-g9b-r6-plus-c-2d-export-completion (local until publication)
```

The identity of this closeout commit, `P_R6PLUS_C`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `INTEGRATED_PHASE` requires the registered `PHASE` and one
`INTEGRATION` on the same exact candidate commit and tree; no `FINAL` is
required.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-a198a69b95cf4fb69e7f3c13818f19b3` — `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C -LogDirectory artifacts\agent\pre-g9b-r6-plus-c-r1-phase-6248bf0f` | `6248bf0f…` / `03038ae3…` | exit 0; `ACCEPTED / COMPLETE`; 5/5 acceptance leaves; plan `f820b8dee078e51fd3ac0e1db2be373b1cc51227690814ea47e581559f08c142`; result `ee066b3313d18c41c2cd471b75192dbfa88a61d52ebf017029be9dbf1c366949` |
| `verification-0bcf970971c74d1e9cb329b81b8aa0dc` — `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-r6-plus-c-r1-integration-6248bf0f` | `6248bf0f…` / `03038ae3…` | exit 0; `ACCEPTED / COMPLETE`; 19/19 acceptance leaves; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; result `6ee73bf06be9f24eca1ac91ac63e2b0a7e1af10eaa48df0382448f0d1fec496b`; standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`), all other diagnostics clear |

Both execution-plan hashes equal those of the historical `b981f8ea` runs. No
new `PHASE`, `INTEGRATION` or `FINAL` is required for this documentation-only
closeout.

## Accepted C result

The technical content accepted is the one recorded in the frozen
[candidate report](pre_g9b_r6_plus_c_candidate_report.md) (§2–§20) as revised
by its §21:

- physical picture, print and LaTeX sizing from the construction unit and a
  session drawing scale; a compact non-physical note whose tooltip and
  accessible description carry the full explanation (`C-UX-1`);
- LaTeX export area, visibility and semantic curves;
- DXF units, hidden layers written OFF, individually hidden objects kept with
  group `60 = 1` (`DQ-C3`), the explicit export area and sidecar schema 2; the
  DXF Save dialogs propose the document stem with `.dxf`, otherwise
  `geocedg-export.dxf` (`C-UX-2`);
- the opt-in exact EMF frame.

## Serialization and output formats

```text
SERIALIZATION CHANGE   = NONE
OUTPUT FORMAT / SCHEMA = NONE beyond the original C candidate
                         (DXF sidecar schema 2 and the DXF/LaTeX/picture
                         behaviour of b981f8ea; revision 1 changes neither)
```

## Smoke findings dispositions

| Finding | Disposition |
|---|---|
| `C-SMOKE-1` hidden circle in the DXF | `EXPECTED_BY_DQ-C3 / SMOKE_EXPECTATION_CLARIFIED` (with `EXTERNAL_READER_LIMITATION` for viewers that ignore group 60); preserved unchanged; author smoke `PASS` |
| `C-SMOKE-2` Classic picture dialog | `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`: reproduced by the author; pre-existing; not caused by `C`; accepted debt; not `C`-blocking; deferred to post-`C` |
| `C-UX-1` | implemented in revision 1; accepted by the author |
| `C-UX-2` | implemented in revision 1; accepted by the author |

## Residual observations and enhancements

| ID | Observation or enhancement | Disposition |
|---|---|---|
| `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` | Upstream `FileMenuD` builds and shows `GraphicExportDialog` off the EDT; EDT layout exceptions at the base and in `C`; the empty dialog and hang were reproduced by the author. | `REPRODUCED BY AUTHOR — PRE-EXISTING — NOT CAUSED BY C — ACCEPTED DEBT — NOT C-BLOCKING — DEFERRED TO POST-C`; not fixed; its own characterization and author authorization are required |
| `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` | AutoCAD 2024 refuses the minimal AC1015 DXF container written since G5 ("Error in APPID Table"), at the base and in `C` alike. | `PRE-EXISTING SINCE G5 — NOT C SCOPE — NOT FIXED`; preserved |
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | Session state may not reset on every non-native full-document replacement route. | unchanged: `OPEN — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT`; not absorbed by `C` |
| `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | A tool replacement reloads the document through a clearing `setXML` committed as a document transition. | unchanged: `A-2 BEHAVIOR — NOT C SCOPE — NOT FIXED`; not absorbed by `C` |
| `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE` | Stable label such as `A3 — 1:50` and a transient coordination indicator for `IsoABorder`. | `ENHANCEMENT — DOCUMENTATION ONLY — OWNER PRE-G9B-R6-plus-E3 — IMPLEMENTATION NOT AUTHORIZED` |
| Desktop test-heap pressure, `exportPDF` callback UTF-8 read, `AutoColor` JVM-global state, historical G9X1 authority scan | Pre-existing debts carried forward by the candidate report. | unchanged; not absorbed by `C` |

## Status updates in this closeout

- Roadmap 4.62: `C` `PASS — AUTHOR APPROVED — PUBLISHED` with `AUTHOR_SMOKE =
  PASS WITH ACCEPTED PRE-EXISTING DEBT` in the last closed and last executed
  phase entries, the next-gate entry, the track paragraph and the `C` row; no
  later subphase is promoted or authorized.
- Mini-track plan: the `C` state in the header, the §3 table and §14; the
  disposition of `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`.

## Post-C intent (documentation only)

The next intended work is a separate post-`C` activity addressing
`OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`, with its own characterization and
its own explicit author authorization. It is not designed, implemented or
started by this closeout, and completing `C` does not authorize it, `E1` or any
other phase.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_A2        e613502831e3b412780d69424d4a1e433a4ae688
  -> T_R6PLUS_C_PREP   5ad96304ae9718c296db753b4822130cf3d99b61 (C preparation)
  -> D_R6PLUS_C_PROMPT 6cb09d55e1ce9b18ef046f445da5494288fd8ecd (reconciled preparation)
  -> A_R6PLUS_C_PROMPT 75a2cf06b62afbd20f6e3a3fc0e079579a3cab2a (C authorization)
  -> T_R6PLUS_C_R1     b981f8eac9dd7b320376e07345b8c1c483de927a (original candidate)
  -> T_R6PLUS_C        6248bf0fc4208540de09d26a7755d4163055ac76 (revision 1, author approved)
  -> P_R6PLUS_C        (this closeout commit)
```

to `main`, by one clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_A2` immediately before
the push. Forbidden: merge commits, rebases, squashes, amends of frozen
candidates, force pushes, tags, releases, binary publication and any further
implementation. The `PUBLISHED` state below takes effect with that
fast-forward.

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-B    = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D1   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-2  = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-C    = PASS — AUTHOR APPROVED — PUBLISHED
                       AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT

PRE-G9B-R6-plus-E1   = NOT AUTHORIZED
PRE-G9B-R6-plus-E2   = NOT AUTHORIZED
PRE-G9B-R6-plus-E3   = NOT AUTHORIZED
PRE-G9B-R6-plus-F1   = NOT AUTHORIZED
PRE-G9B-R6-plus-F2   = NOT AUTHORIZED

PRE-G9B-R6-plus-F3   = PLANNED — AUTHOR APPROVED — PUBLISHED
                       IMPLEMENTATION NOT AUTHORIZED

PRE-G9B-R6-plus-G    = NOT AUTHORIZED

post-C Classic activity (OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT)
                     = NOT AUTHORIZED

PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus GLOBAL CLOSEOUT

G9B                  = NOT AUTHORIZED
```

The accepted operational order remains
`P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`. No
authorization of `E1` or of any other phase follows from this closeout: each
needs its own explicit author instruction naming its exact base. No tag or
release is authorized.
