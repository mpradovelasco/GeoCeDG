# PRE-G9B-R6-plus-F3 — focused author-decision preparation record

- Record version: **3.0** (successor of the
  [reconciliation record](pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md)
  v2.0 and the [preparation record](pre_g9b_r6_plus_f3_author_decision_preparation_record.md)
  v1, both unchanged and kept as history)
- Activity: `PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction of
  2026-10-12)
- Status: **DECISIONS PREPARED — PENDING FINAL AUTHOR DISPOSITION — NOTHING
  APPROVED**
- Base: `T_F3_RECONCILIATION` `0eb50311d55a099de70e3f222b885b31685ac0df`,
  tree `061e08f1a9d821cf642827afaf52d802629b940a`
- Evidence: [focused feasibility report](pre_g9b_r6_plus_f3_focused_feasibility_report.md);
  [risk closeout candidate](pre_g9b_r6_plus_f3_risk_closeout_candidate.md);
  design: [restricted insertion design candidate](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md)
- Approved planning decisions (unchanged): `F3-D1` to `F3-D6` of the
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md);
  accepted `D1` decision `DQ-D1-7` in the
  [D1 prompt closeout record](pre_g9b_r6_plus_d1_prompt_closeout_record.md)
  (line 105)
- `selfApproved = false`

## 1. Author directions of 2026-10-12 (recorded; not approvals)

R (restricted native insertion); M1 as an important objective, M0 only as a
fallback for unsupported cases; L1 without overwrite; an independent insertion
buffer (with a later explicit `DQ-D1-7` amendment); high priority for
specialized GeoCeDG objects; limited T0; U0 preferred, narrow upstream
corrections not excluded. These directions select the alternatives
investigated. They do not approve a matrix, a mechanism, a `D1` change or an
implementation; this record keeps every question pending.

## 2. Updated matrix `DQ-F3-1` to `DQ-F3-20`

| Id | v2 recommendation | Effect of the author directions and the new evidence | v3 recommendation (pending) | Evidence | Planning impact | Feasibility | Verification | Residual risk |
|---|---|---|---|---|---|---|---|---|
| `DQ-F3-1` | N1 | the second action's name changes if `DQ-F3-23` is approved | N1; second action `document.apply-styles` if `DQ-F3-23` | — | `F3-D2` naming amendment | declarative | pins | none |
| `DQ-F3-2` | A1 | unchanged | A1 | H | none | confirmed | Classic regression | duplicated Open setup |
| `DQ-F3-3` | X1 container rule | unchanged | X1 | H | item 3 reading (`DQ-F3-13`) | confirmed | failure tests | none |
| `DQ-F3-4` | OFF-B | unchanged | OFF-B | H | none | confirmed | OFF tests | none |
| `DQ-F3-5` | L1, no overwrite | L1 now executed on chains, conditions, macro and specialized objects; deterministic | **L1, no overwrite** | G4 | item 4 amendment | closed with restriction | rename tests | static strings, JavaScript |
| `DQ-F3-6` | all-or-nothing | split into pre-commit (proven) and commit residual (`DQ-F3-21`) | per `DQ-F3-18`/`-21` | G2 | item 4 amendment | proven for subset | atomicity tests | commit residual |
| `DQ-F3-7` | dedicated buffer | independent buffer proven across two windows | dedicated insertion buffer | G6 | `DQ-D1-7` amendment | closed | `D1` tests | none |
| `DQ-F3-8` | T0 | T0 executed; specialized outputs must be excluded | T0 with exclusions | G7 | item 5 amendment | closed with restriction | T0 tests | none |
| `DQ-F3-9` | moot under T0 | confirmed: T0 never touches layers | moot | G7 | none | — | layer tests | none |
| `DQ-F3-10` | H-1, H-3 | helper release (H-3) executed successfully; H-1 still needed (recent list changed in every run) | H-1, H-3 | G1, G2 | none | closed (H-3), source-proven (H-1) | helper tests | none |
| `DQ-F3-11` | F-1 | unchanged | F-1 | H | none | — | Classic, fallback | Classic keeps its defects |
| `DQ-F3-12` | `INTEGRATED_PHASE` | confirmed for U0 | `INTEGRATED_PHASE` (not frozen) | G8 | none | — | `PHASE` + `INTEGRATION` | U2 would need reclassification |
| `DQ-F3-13` | amend item 4; item 3 as containers | R confirmed as direction | **A13-1** with the R admission contract of the design §3 | G2–G5 | amendment (§3 below) | ready | — | — |
| `DQ-F3-14` | whitelist, Design R | specialized objects now individually evidenced | **matrix of the risk candidate §2** | G5 | amendment | ready | matrix tests | conditions §2 |
| `DQ-F3-15` | M0 | author objective M1; M1 executed | **M1 with restrictions** (transitive closure; digest reuse; reject different definitions; reject identity + macros); M0 only for the rejected cases | G3 | amendment | ready with restrictions | macro tests | brittle equality of user macros |
| `DQ-F3-16` | dedicated buffer | executed; amendment text prepared | **B16-1** with the amendment of §4 | G6 | `DQ-D1-7` amendment | closed | `D1` tests | none |
| `DQ-F3-17` | T0 | name question added | **T0** (six properties; specialized outputs excluded; defaults and labeling deferred) | G7 | amendment | closed with restriction | T0 tests | narrower than upstream |
| `DQ-F3-18` | admission, dry run, verification, rollback | dry run and strict verification proven necessary (V1, V2, A6d) | **A18-1 with strict pairwise verification** | G2 | none | proven for subset | atomicity tests | commit residual (`DQ-F3-21`) |
| `DQ-F3-19` | U0 only | U0 confirmed sufficient | U0 only | G8 | none | closed | — | Classic unchanged |
| `DQ-F3-20` | debt closes with approved amendments | unchanged | C20-1 | — | debt closure | — | — | deferred items named |

## 3. New decisions

### DQ-F3-21 — Commit-time failure after a passing dry run

| Alternative | Consequence |
|---|---|
| **K1 — accept a disclosed compensating rollback: serializable state restored byte-identically; session associations (exact instances, F2 memory, IsoABorder export link, selection) reset as by an Undo; the orchestration restores the saved flag and removes transferred macros; the entry gate characterizes and removes the identity-paste undo entry; the user is told "nothing was inserted" (recommended)** | bounded U0 implementation; residual reachable only by non-deterministic faults |
| K2 — require full runtime non-mutation | needs a non-rebuilding rollback in the shared clipboard/construction code (U2): reclassification review; F3 delayed |
| K3 — reduce R until no commit can fail | not achievable: environmental faults are not content-dependent |

### DQ-F3-22 — Undo of transferred macros

| Alternative | Consequence |
|---|---|
| **MU-1 — transferred macros stay registered after Undo of the insertion (inherited undo does not cover macros); Redo works because the macro remains; the guides state it (recommended)** | no new undo mechanism |
| MU-2 — remove unused transferred macros after Undo and re-add on Redo | product undo-event integration not proven; risk of breaking Redo |
| MU-3 — restrict F3 to M0 | contradicts the author objective |

### DQ-F3-23 — User-facing name of the T0 operation

"Apply Template" suggests that a template document's defaults, labeling and
styles are applied wholesale (the upstream behavior). T0 copies six visual
properties to existing objects and changes nothing else. The upstream Spanish
string for `ApplyTemplate` is already "Aplicar estilos".

| Alternative | Consequence |
|---|---|
| **S1 — "Apply Styles…" / "Aplicar estilos…" (recommended)** | accurately describes T0; amends `F3-D2` items 1–2 naming |
| S2 — keep "Apply Template…" / "Aplicar plantilla…" | familiar, but overstates the operation |

### DQ-F3-24 — Payloads needing both identity records and macros

| Alternative | Consequence |
|---|---|
| **R24-1 — reject before mutation with a diagnostic (the inherited paste refuses them silently, `CopyPasteD.java:588-602`) (recommended)** | U0; such documents stay uninsertable |
| R24-2 — U1 change of the inherited refusal | identity-bearing macro import semantics are unspecified (G9A1); shared concerns; not justified |

## 4. Proposed amendments (not applied)

### 4.1 `F3-D2`

| Item | Approved text (abridged) | Proposed amendment |
|---|---|---|
| 1–2 | File → Insert holding Insert File and Apply Template | File → Insert holding **Insert File… / Insertar archivo…** and **Apply Styles… / Aplicar estilos…** (if `DQ-F3-23` S1) |
| 3 | both accept `.cedg` and `.ggb` | unchanged as the accepted **containers**; the contents are admitted only by the admissibility contract (`DQ-F3-13`, `-14`) |
| 4 | Insert File keeps its semantics … | "Insert File inserts an admissible source construction completely or not at all. Admission (source validity, the admissibility matrix, the transitive macro closure under M1, label conflicts resolvable by safe renaming, the target's runtime features) and a strict dry run precede any mutation; existing objects are never overwritten or rebound; required macros are transferred once or reused only when their product definition digests are equal; the insertion uses an operation-scoped buffer, keeps the `D1` provenance and notice and never scales coordinates; a commit-time failure is handled per `DQ-F3-21`" |
| 5 | Apply Template applies labeling, defaults and styles … | "Apply Styles copies colour, line style, line thickness, point style, point size and fill opacity from the source document's defaults of the matching type to the selected objects, or to all objects, excluding native-dimension, IsoABorder and Locus V2 family outputs; it changes nothing else, imports no geometry, never changes the target units, and is one undoable step. Construction defaults and the labeling style are not applied in F3" |
| 6–9 | OFF, no other formats, closure before `G` | unchanged |

### 4.2 `DQ-D1-7`

Approved text (D1 prompt closeout record, line 105): Insert File receives
transient unit provenance from the inserted document because it replaces the
internal copy buffer; a later Ctrl+V from the same buffer keeps that
provenance.

Proposed amended text:

> Ordinary Copy, Cut and Paste are unchanged: the window copy buffer carries
> the source document's unit provenance, is replaced and cleared with it, and a
> later Ctrl+V of that buffer keeps it. **Document insertion (F3 Insert File)
> does not use or replace the window copy buffer.** It copies the inserted
> document through an operation-scoped insertion buffer whose provenance is the
> inserted document's unit state, recorded by the same `D1` provenance
> mechanism and consumed once by that insertion's notice; the buffer and its
> provenance are discarded when the operation ends, whether it succeeds or
> fails. Different physical construction meanings show the same non-blocking
> notice as an ordinary cross-document paste. Geometry is never rescaled,
> coordinates are never converted and the target units are never modified.
> After an insertion, Ctrl+V pastes what the user last copied, with that copy's
> provenance.

`unit-system.md` §11 already speaks of copy and cut only and needs no change;
the user-guide sentence about "a later paste of the same objects" changes with
the implementation.

## 5. What stays open

- Every `DQ-F3-*` above: pending final author disposition.
- `F3R-ATOMICITY-COMMIT` until `DQ-F3-21` is disposed.
- Entry-gate items: removal of the identity-paste undo entry after a compensated
  failure; the effect of the asynchronous undo store; malformed macro definitions
  as a distinct case.
