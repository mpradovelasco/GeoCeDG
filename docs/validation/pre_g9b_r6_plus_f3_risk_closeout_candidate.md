# PRE-G9B-R6-plus-F3 — risk register and evidence matrix (closeout candidate)

- Activity: `PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction of
  2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**; technical dispositions only,
  no author decision recorded
- Base: `T_F3_RECONCILIATION` `0eb50311d55a099de70e3f222b885b31685ac0df`,
  tree `061e08f1a9d821cf642827afaf52d802629b940a`
- Evidence: [focused feasibility report](pre_g9b_r6_plus_f3_focused_feasibility_report.md)
  (`G0`–`G9`); earlier F3 reports as history
- Decisions: [focused author-decision preparation record](pre_g9b_r6_plus_f3_focused_author_decision_preparation_record.md)
- `selfApproved = false`

Dispositions: `CLOSED — EVIDENCE SUPPORTED`, `CLOSED WITH EXPLICIT SCOPE
RESTRICTION`, `OPEN — NOT ESTABLISHED`, `BLOCKED`. A risk is closed only for
the scope its evidence covers.

## 1. Risk register

### F3R-ATOMICITY

| Field | Content |
|---|---|
| Risk | an insertion changes the target although it fails |
| Source finding | `CF-F3-1` (stale re-paste), FQ-3 (silent element skips possible outside the identity branch) |
| Required invariant | every rejected or failed insertion leaves serializable and runtime state unchanged; a successful one is one coherent undo step |
| Test | executed fault matrix A0–A10, I-faults, V1–V3, V7; success S1/S2 (feasibility report G2) |
| Observed | admission and strict dry-run rejections: no change at all; in-commit faults: serializable state restored byte-identically, but the target is rebuilt (instances replaced, F2 memory, selection and IsoABorder link lost, saved → false) and an identity-bearing paste leaves its own undo entry; in payloads without identity, element faults are absorbed or degrade objects silently, detected only by the strict pairwise check |
| Limitations | the asynchronous undo store cannot report failure synchronously; one dry-run determinism family exercised (unknown or renamed command, missing macro) |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` for the pre-commit subset; the in-commit residual is `F3R-ATOMICITY-COMMIT` |
| Residual risk | none inside the subset |
| Recommended disposition | admission + strict dry run + strict verification are mandatory parts of the contract |
| Author decision required | `DQ-F3-18` |

### F3R-ATOMICITY-COMMIT

| Field | Content |
|---|---|
| Risk | a failure that occurs only during the real commit, after a passing dry run |
| Source finding | A1/A2/A5–A9, I-faults, X-iso-link |
| Required invariant | non-mutation of serializable **and** runtime state |
| Test | executed (G2) |
| Observed | serializable restore only; session reset (instances, F2 memory, IsoABorder export link, selection); saved flag false; one surviving undo entry for identity-bearing payloads |
| Limitations | the surviving undo entry's effect when undone was not characterized; no rollback path that keeps instances exists in the inherited code |
| Status | `OPEN — NOT ESTABLISHED` (complete non-mutation not achievable with existing seams) |
| Residual risk | reachable only by non-deterministic or environmental faults after a passing dry run |
| Recommended disposition | the author accepts a disclosed compensating rollback for this residual (saved flag restored by the orchestration, user message "nothing was inserted; the document was reloaded from its state before the insertion"), and the entry gate characterizes and removes the surviving undo entry; otherwise F3 needs a shared non-rebuilding rollback (U2, reclassification) |
| Author decision required | `DQ-F3-21` |

### F3R-MACRO-M1

| Field | Content |
|---|---|
| Risk | macro transfer substitutes, duplicates or leaks macros |
| Source finding | `CF-F3-3`, `CF-F3-4`, FQ-4 |
| Required invariant | required macros transferred once, never substituted; rejection on unproven collisions; no macro left after failure |
| Test | G3 (simple, nested, collisions, identical, curated library, repeated, mixed, failure V7) |
| Observed | M1 works for direct and nested macros; the transitive closure check is necessary (V4); identical reuse works with the product digest and a buffer that drops reused macros (V5); identity + macros payloads are refused silently by the inherited paste; Undo keeps transferred macros; failure cleanup works when explicit |
| Limitations | user-defined identical macros from independent documents compare unequal (brittle definition text): only library tools and re-insertions of the same source are recognized; malformed macro definitions not executed as a distinct case |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` |
| Residual risk | some semantically identical user macros are rejected (fail closed) |
| Recommended disposition | M1 with: transitive closure; reuse only on equal product digest; reject different definitions; reject identity-bearing payloads needing macros; explicit cleanup |
| Author decision required | `DQ-F3-15`, `DQ-F3-22`, `DQ-F3-24` |

### F3R-LABEL-L1

| Field | Content |
|---|---|
| Risk | renaming rebinds an inserted dependency to a target object or loses a reference |
| Source finding | FQ-5 |
| Required invariant | closure internal; target identities kept; deterministic labels; undo, redo, reopen valid |
| Test | G4 |
| Observed | all invariants hold for the tested families, including macro objects and specialized objects; deterministic across runs |
| Limitations | labels inside static strings and JavaScript are not remapped |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (scripts rejected; no label-bearing static strings relied upon) |
| Residual risk | static texts that mention a label keep the old name (cosmetic, no dependency) |
| Recommended disposition | L1, no overwrite |
| Author decision required | `DQ-F3-5` |

### F3R-SPECIALIZED-LOCUS

| Field | Content |
|---|---|
| Risk | Locus V2 identity, tokens or results lost or inconsistent |
| Test | G5 (`RK-specialized`) |
| Observed | complete construction; new persistent ids; consistent token remapping; identical dynamic behavior; ids stable on reopen; undo/redo; `revision` difference = per-instance in-memory counter (benign) |
| Limitations | payloads combining Locus V2 and macros are refused by the inherited paste; paste ignores the Locus V2 runtime flag |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (no macros in the same payload; target must allow Locus V2 creation) |
| Author decision required | `DQ-F3-14`, `DQ-F3-24` |

### F3R-SPECIALIZED-SPLINE

| Field | Content |
|---|---|
| Observed | as Locus V2; `LocusLength` of the spline identical after the same edit |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (as Locus V2) |
| Author decision required | `DQ-F3-14` |

### F3R-SPECIALIZED-DIMENSIONS

| Field | Content |
|---|---|
| Risk | native dimensions lose value semantics or are rescaled |
| Test | G5 (`RK-specialized`, `RK-dynamic`) |
| Observed | Linear and Aligned dimensions: five outputs, closure internal, identical recomputation after edits, reopen, undo; texts re-presented in the target unit without scaling; earlier AlignedDimension failure was a fixture label collision |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (text follows the target document's units, per `unit-system.md` §14 and the `D1` notice) |
| Author decision required | `DQ-F3-14` |

### F3R-SPECIALIZED-ISOA

| Field | Content |
|---|---|
| Risk | IsoABorder geometry or its export association corrupted |
| Test | G5 (`RK-round5` V6, `RK-dynamic`) |
| Observed | three outputs, identical values and recomputation, reopen, undo; the inserted sheet is not linked to the export area (session association, spec §11.3, §13); an existing target link survives a successful insertion and is lost by rollback or Undo |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (no export link imported; the user links an inserted sheet explicitly) |
| Author decision required | `DQ-F3-14` |

### F3R-CLIPBOARD-D1

| Field | Content |
|---|---|
| Risk | insertion consumes or corrupts the user's buffer or provenance |
| Test | G6 |
| Observed | both windows' buffers and provenances unchanged; global clipboard containers unchanged; Ctrl+V correct; notice from the insertion's own provenance; failure leaves everything unchanged |
| Status | `CLOSED — EVIDENCE SUPPORTED` |
| Residual risk | none; normative amendment of `DQ-D1-7` required |
| Author decision required | `DQ-F3-16` |

### F3R-TEMPLATE-T0

| Field | Content |
|---|---|
| Test | G7 |
| Observed | only the six whitelisted properties change; one coherent undo step; selection scope; native dimension outputs restyled unless excluded |
| Status | `CLOSED WITH EXPLICIT SCOPE RESTRICTION` (exclude native-dimension, IsoABorder and Locus V2 family outputs; defaults and labeling style not applied) |
| Author decision required | `DQ-F3-17`, `DQ-F3-23` |

### F3R-ARCHITECTURE

| Field | Content |
|---|---|
| Observed | every closed restriction is implementable with U0 (orchestration, a GeoCeDG insertion-buffer subclass of `CopyPasteD`, product digest, helper overrides) |
| Status | `CLOSED — EVIDENCE SUPPORTED` for U0; U2 needed only if `DQ-F3-21` rejects the compensating residual |
| Author decision required | `DQ-F3-19`, `DQ-F3-12` |

## 2. Specialized-object evidence matrix

| Type | Construction | Dynamic dependencies | Identity | Undo/redo | Reopen | Decision readiness |
|---|---|---|---|---|---|---|
| Locus V2 | SUPPORTED | SUPPORTED | SUPPORTED (new ids, consistent tokens) | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| Locus V2 semantic point, metric, intersection result, token-selected point, `Translate` of a locus | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| Spline V2 | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| LinearDimension | SUPPORTED | SUPPORTED | not identity-bearing | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| AlignedDimension | SUPPORTED | SUPPORTED | not identity-bearing | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| IsoABorder | SUPPORTED | SUPPORTED | export link not imported | SUPPORTED | SUPPORTED | SUPPORTED WITH CONDITIONS |
| macro-generated objects (M1) | SUPPORTED | SUPPORTED | macro bound to the transferred or digest-equal macro | objects SUPPORTED; macro kept after Undo | SUPPORTED | SUPPORTED WITH CONDITIONS |
| projection bindings, spatial objects | — | — | — | — | — | REJECTED (no product creation route; spatial import outside F3) |
| 3D objects (incl. a free 3D point) | inserts | — | — | — | — | REJECTED (F3 excludes 3D import) |

Conditions are those of §1. A source whose required closure contains a
rejected or not-established type is rejected as a whole.

## 3. Summary

```text
CLOSED — EVIDENCE SUPPORTED            F3R-CLIPBOARD-D1, F3R-ARCHITECTURE (U0)
CLOSED WITH EXPLICIT SCOPE RESTRICTION F3R-ATOMICITY (pre-commit subset), F3R-MACRO-M1,
                                       F3R-LABEL-L1, F3R-SPECIALIZED-LOCUS,
                                       F3R-SPECIALIZED-SPLINE, F3R-SPECIALIZED-DIMENSIONS,
                                       F3R-SPECIALIZED-ISOA, F3R-TEMPLATE-T0
OPEN — NOT ESTABLISHED                 F3R-ATOMICITY-COMMIT (author disposition DQ-F3-21)
BLOCKED                                none
```
