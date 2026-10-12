# PRE-G9B-R6-plus-F3 author design closeout record

```text
RECORD_KIND              = AUTHOR_DECISIONS (design approval: Q1–Q7, DQ-F3-1..24, P1–P7)
ACTIVITY                 = PRE-G9B-R6-plus-F3-AUTHOR-DESIGN-CLOSEOUT
DECISION_DATE            = 2026-10-13
APPROVED_DESIGN_BASE     = 9d130b43fbeb731a25d5dcf6413ed0dd0439f61e
                           tree daec52cd9f2cfe395bfac26b9556a11276323a9f
                           parent e479b76d47c4e62baf9a4cf511ec06268c5b5ab3
                           STATIC verification-d975299fbf334c05b98e3aec5b9006b9
                           ACCEPTED / COMPLETE
PUBLISHED_MAIN           = e70718fec3612814144cd405d46f6c08864ceadd

PRE-G9B-R6-plus-F3 DESIGN          = DESIGN — AUTHOR APPROVED
PRE-G9B-R6-plus-F3 IMPLEMENTATION  = NOT AUTHORIZED
F3 TECHNICAL VERIFICATION          = NOT EXECUTED
F3 AUTHOR SMOKE                    = NOT EXECUTED
F3 PRODUCT CLOSEOUT                = NOT PERFORMED
F3 PUBLICATION                     = NOT AUTHORIZED
authorApproved           = true    (F3 DESIGN ONLY)
selfApproved             = false
implementationAuthorized = false
publicationAuthorized    = false
productPassClaimed       = false
activityClass            = DOCUMENTATION_STATUS_ONLY (STATIC)
```

This record preserves, versioned, the author's approval of the F3 design given
on 2026-10-13. It is the authority for the approved F3 design and for the
amendments `P1`–`P7`. It is **not** the authority for any product
implementation, verification result, smoke, product closeout or publication;
each of those is a separate author act. Machine-readable mirror:
[`pre-g9b-r6-plus-f3-author-design-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-author-design-closeout.json).

## 1. Author authorization (verbatim essentials)

The author instruction `PRE-G9B-R6-plus-F3-AUTHOR-DESIGN-CLOSEOUT` states: "I
approve the restricted F3 design represented by the final audit-reconciled
documentary candidate, incorporating Q1–Q7, the independent audit's accepted
conditions, and the mandatory implementation safety gates. This authorization
is limited to approval of the F3 design and its governing contracts." It does
not authorize product implementation, kernel or shared changes, execution of
the F3 implementation prompt, publication, or promotion of a technical
implementation to PASS.

## 2. Entry gate (2026-10-13)

| Check | Result |
|---|---|
| HEAD, tree, parent | `9d130b43…`, `daec52cd…`, `e479b76d…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` → `6c07e01a` → `6e41bf44` → `e479b76d` → `9d130b43` |
| `main`, `origin/main`, live remote `main` | `e70718fe` |
| receipt | `verification-d975299f…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `9d130b43`/`daec52cd` |
| worktree; product delta since `main` | clean; none |
| independent audit archived | yes; tracked copy SHA-256 `b22629ef…5fbd41` (committed blob verified) |
| sheet v6.0, `P1`–`P7`, design documents, risk register, canonical prompt | present |
| governance | design-approval precedent (E3 `ff560991`): candidate status promoted to `DESIGN — AUTHOR APPROVED`, author-decisions record and JSON, prompt reconciled and kept `PREPARED — NOT AUTHORIZED` |
| branch | `phase/pre-g9b-r6-plus-f3-author-design-closeout` (local, from `9d130b43`) |
| result | **PASS** |

## 3. Approved design set and precedence

| Rank | Document | Status after this record |
|---|---|---|
| 1 | this record (decisions and the texts `P1`–`P7`) | authority |
| 2 | [reconciled amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals_reconciled.md) `P1`–`P7` | **APPROVED** as recorded in §6 |
| 3 | [recovery design addendum successor](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_successor_candidate.md) | **DESIGN — AUTHOR APPROVED** |
| 4 | [restricted insertion design successor](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md) (recovery §2–§3 superseded by rank 3) | **DESIGN — AUTHOR APPROVED** |
| 5 | [restricted insertion design candidate](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md) | **DESIGN — AUTHOR APPROVED** (lower ranks yield on conflict) |
| 6 | [F3-PREP design candidate](../architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md) §3, §4, §8, §9 by reference; [conservative design](../architecture/pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md) §9 tests by reference | approved by reference only |
| — | [first recovery design addendum](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_candidate.md), earlier assessments, reports, decision sheets v1–v5 | history; not approved as design |

The [Final F3 Author Decision Sheet v6.0](pre_g9b_r6_plus_f3_final_author_decision_sheet_v6.md)
is the preparation document whose recommendations are disposed of here. No
normative specification exists yet for F3; the specification
`geocedg/specs/ui/document-insertion.md` stays the first deliverable of the
implementation (canonical prompt) and is not created by this record.

## 4. Q1–Q7 decisions

| Q | DQ | Decision |
|---|---|---|
| Q1 | `DQ-F3-21` | **APPROVED — K1 corrected with mandatory safety gates**: K1-S (admitted non-identity payloads); K1-R-H (admitted identity payloads); original-command history transfer without lossy copying; GeoCeDG-owned undo-manager subclass; generation barrier; conservative pending-store accounting C1; complete verified native recovery snapshot; destination UUID preserved; certified postconditions; declared session reset when needed; S when guarantees are not established; serious-failure isolation with exactly three exits and no export of the uncertified state. The withdrawn K1-R procedure and any lossy history transfer are **not** approved. |
| Q2 | `DQ-F3-5`, `-6`, `-13`, `-14`, `-18` | **APPROVED — R with L1 and strict admission** (`P1` items 3–4, `P4`) |
| Q3 | `DQ-F3-15`, `-22`, `-24` | **APPROVED — M1 / MU-1 / identity + macro rejection** (`P3`) |
| Q4 | `DQ-F3-7`, `-16` | **APPROVED — independent insertion buffer; `P2` amends `DQ-D1-7`** |
| Q5 | `DQ-F3-8`, `-9`, `-17`, `-23` | **APPROVED — T0 / "Apply Styles…" / "Aplicar estilos…"** (`P5`, `P1` items 2 and 5) |
| Q6 | `DQ-F3-20` | **APPROVED — C20-1 closure policy** |
| Q7 | `DQ-F3-1`–`-4`, `-10`–`-12`, `-19` | **APPROVED — N1, A1, X1, OFF-B, H-1/H-3, F-1, U0; `INTEGRATED_PHASE` target** |

### 4.1 Q1 recorded conditions

- Outcome categories: REJECTED BEFORE MUTATION; FAILED — RECOVERY CERTIFIED;
  FAILED — RECOVERY CERTIFIED WITH DECLARED SESSION RESET; SERIOUS RECOVERY
  FAILURE — STATE NOT CERTIFIED. Compensating recovery is not full
  runtime-instance atomicity.
- A failed insertion must not leave partial geometry, degrade algorithms,
  corrupt the DAG, alter surviving durable identities, lose Undo/Redo history,
  grouping or label-associated behavior, let rejected geometry reappear, leave
  unintended macros, change document identity or units, or report a certified
  rollback without verified postconditions.
- The inherited successful insertion clears the selection and activates the
  Move tool; so does a certified failure (`P7`).
- Native recovery snapshot: captured before mutation; complete native archive
  (macros, images, library JavaScript, construction defaults, GUI settings,
  hidden layers as applicable); original UUID; native preflight; provenance and
  ownership; retained through recovery and isolation; released only at the
  terminal outcome (success, certified recovery, or completion of the user's
  isolation exit); never the undo snapshot; never removed by premature
  unconditional cleanup; produced through the public native writer
  (`writeGeoGebraFile(OutputStream, false)`), not through private inherited
  methods; capture or validation failure rejects before mutation.
- Accounting C1 invariant: `pending == 0 AND !degraded ⇒ no relevant pending
  store can still mutate protected history`, to be proven across scheduling,
  completion, cancellation, generation mismatch, exceptions and stale
  notifications; over-counting acceptable, under-counting not; no automatic
  switch to C2 or C3.
- Document identity: successful insertion keeps the destination UUID; the
  recovery copy keeps the original UUID, uses a different path, never
  overwrites automatically, is written atomically and validated after writing,
  and does not change the session's current-file association. Ordinary Ctrl+V
  stays outside F3.
- Isolation: no editing, ordinary saving, scripting, animation-driven change,
  API mutation, clipboard insertion, Undo/Redo or authoritative export of the
  uncertified state; Reopen and Close never show an inherited save-changes
  prompt; any future forensic export needs separate authorization; effective
  isolation is an implementation and verification gate, not a verified
  capability.

## 5. Final disposition of `DQ-F3-1` to `DQ-F3-24`

| DQ | Subject | Final author disposition | Q |
|---|---|---|---|
| `DQ-F3-1` | File → Insert structure and action identities | N1: group `file-insert`; `document.insert-file`, `document.apply-styles` | Q7 |
| `DQ-F3-2` | chooser architecture | A1: GeoCeDG Open override and insertion chooser; never `AppD.insertFile` / `applyTemplate` | Q7 |
| `DQ-F3-3` | containers and fallback | X1: one `.cedg`/`.ggb` filter; stem → `.cedg` then `.ggb`; single selection | Q7 |
| `DQ-F3-4` | OFF in the GeoCeDG UI | OFF-B: no filter; explicit `.off` refused with a message; internal OFF untouched | Q7 |
| `DQ-F3-5` | labels and conflicts | L1 deterministic renaming; no overwrite; no label dialog | Q2 |
| `DQ-F3-6` | undo, cancellation, partial failure | one undo step; failures per `DQ-F3-18`/`-21` | Q2 |
| `DQ-F3-7` | `D1` provenance | through the operation-scoped buffer; no scaling | Q4 |
| `DQ-F3-8` | style operation invariants | T0 per `P5` | Q5 |
| `DQ-F3-9` | layers under the style operation | layers never touched | Q5 |
| `DQ-F3-10` | helper, recent list, clipboard | H-1, H-3; independent buffer | Q7 |
| `DQ-F3-11` | Classic and legacy profile | F-1: Classic unchanged; v1 fallback inherited | Q7 |
| `DQ-F3-12` | architecture and class | `INTEGRATED_PHASE` target (PHASE + INTEGRATION), frozen at implementation authorization | Q7 |
| `DQ-F3-13` | Insert File contract | `P1` items 3–4 (reconciled) | Q2 |
| `DQ-F3-14` | admissibility matrix | `P4` | Q2 |
| `DQ-F3-15` | macros | M1 per `P3` | Q3 |
| `DQ-F3-16` | `DQ-D1-7` amendment | `P2` | Q4 |
| `DQ-F3-17` | style contract | `P5` | Q5 |
| `DQ-F3-18` | atomicity mechanism | admission + strict dry run + commit + strict verification; recovery per `DQ-F3-21` | Q2 |
| `DQ-F3-19` | optional inherited corrections | U0 only; none selected | Q7 |
| `DQ-F3-20` | debt closure | C20-1 (§8) | Q6 |
| `DQ-F3-21` | commit-time recovery | K1 corrected (§4.1, `P6`, `P7`, recovery design addendum successor) | Q1 |
| `DQ-F3-22` | Undo of transferred macros | MU-1 | Q3 |
| `DQ-F3-23` | name of the style operation | "Apply Styles…" / "Aplicar estilos…" | Q5 |
| `DQ-F3-24` | identity + macros | rejection before mutation with diagnostic | Q3 |

Each `DQ-F3-*` has exactly one final disposition.

## 6. Amendments `P1`–`P7`

| Text | Disposition | Target |
|---|---|---|
| `P1` | **APPROVED** — items 1–5 replace `F3-D2` items 1–5 (item 2 with the S1 name "Apply Styles… / Aplicar estilos…"; item 4 as reconciled); `F3-D2` items 6–9 unchanged | `F3-D2` ([F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md), kept unchanged as history; this record is the amending authority) |
| `P2` | **APPROVED** — replaces the text of `DQ-D1-7` for Insert File; ordinary Copy, Cut, Paste, the window buffer and the global clipboard unchanged; no other D1 semantics affected | `DQ-D1-7` ([D1 prompt closeout record](pre_g9b_r6_plus_d1_prompt_closeout_record.md) line 105, kept unchanged as the historical approval; this record is the amending authority) |
| `P3` | **APPROVED** — M1 contract | F3 contract |
| `P4` | **APPROVED** — admissibility | F3 contract |
| `P5` | **APPROVED** — T0 style operation | F3 contract |
| `P6` | **APPROVED** — failure and recovery diagnostics (reconciled) | F3 contract |
| `P7` | **APPROVED** — recovery and isolation contract | F3 contract |

Scope check: no proposal exceeds the authorization. Interpretation notes,
not discrepancies: `P3` reuses any macro with an equal product definition
digest (the curated tools are its principal case), as the author approved
"the reconciled P3 contract"; the snapshot is "released only after the
corresponding terminal outcome", where the terminal outcome of a serious
failure is the completion of the user's isolation exit.

## 7. Binding implementation stop conditions

FR-F3-1 Undo accounting · FR-F3-2 History semantics · FR-F3-3 Native recovery
snapshot · FR-F3-4 Serious-failure isolation · FR-F3-5 EDT safety · FR-F3-6
Document identity · FR-F3-7 Semantic admission · FR-F3-8 Compatibility — as
worded in the canonical prompt. They are blocking execution constraints: on
failure the implementation stops, preserves evidence and requests a new author
decision. The independent audit's gates G1–G7 and its not-established items
NE1–NE2 are implementation gates under them.

## 8. Debt policy and deferred items (`DQ-F3-20`, C20-1)

`DEBT-R6PLUS-FILE-INSERT-SURFACE` closes only when F3 has completed its
implementation, technical verification and author closeout; this design
approval does **not** close it. Deferred items, each a named debt to be opened
at the F3 product closeout with explicit scope and owner, none authorized:

| Deferred item | Scope |
|---|---|
| `DEFER-F3-TEMPLATE-DEFAULTS` | construction defaults and labeling style in the style operation |
| `DEFER-F3-M2` | broader macro transfer beyond M1 |
| `DEFER-F3-SCRIPTS` | payloads with scripts |
| `DEFER-F3-3D` | 3D objects and spatial bindings |
| `DEFER-F3-UNLISTED-TYPES` | types outside `P4` |
| `DEFER-F3-IDENTITY-MACROS` | combined identity-bearing + macro import |
| `DEFER-F3-FORENSIC-EXPORT` | export of an uncertified state for diagnosis |

## 9. Verification status and obligations

No F3 test, PHASE or INTEGRATION run has been executed for F3; the independent
audit (`GO WITH CONDITIONS`) is review evidence, not a PASS. The target class
is `INTEGRATED_PHASE` (PHASE + INTEGRATION); the executable class and its
verification contract are frozen at the separate implementation-authorization
gate under current governance. The plan covers restricted insertion, L1, M1,
Locus V2, Spline V2, native dimensions, IsoABorder, persistent identities,
document lifecycle, native archive and recovery, hidden layers, D1 units, the
independent clipboard, document UUID, complete Undo/Redo semantics,
asynchronous store accounting, EDT execution, F2 memory, script and API
mutation routes, isolation, factory lifecycle, injected failures and Classic
compatibility, using the existing `AppGeoCeDG`-based suites named in the
[audit reconciliation report](pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md)
§9 and the new focused tests of the recovery design addendum successor §6. The
gates FOCUSED TESTS, PHASE, INTEGRATION, AUTHOR SMOKE, AUTHOR CLOSEOUT and
PUBLICATION stay separate; automated success never replaces author smoke or
approval.

## 10. Exclusions

Not authorized by this record: product implementation; kernel, shared or
inherited changes; execution or authorization of the canonical F3 prompt;
permanent tests; verification-infrastructure changes; publication, push,
merge, tag; any F3 PASS. The next task is
`PRE-G9B-R6-plus-F3-IMPLEMENTATION-AUTHORIZATION`, which must name the exact
approved base, freeze the executable class and explicitly authorize the
bounded implementation.
