# PRE-G9B-R6-plus-F3 — restricted insertion: design successor candidate

- Status: **PROPOSED — PENDING AUTHOR REVIEW**
- Produced by: `PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author
  instruction of 2026-10-12; documentary)
- Base: `T_F3_RISK` `8b41af11316f90d7a068ce6af4859213240ce6a6`, tree
  `8be60e86e09f957f1735990b966190bd44ffab2a`
- Successor of (unchanged, history): the
  [restricted insertion design candidate](pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md),
  the [conservative design candidate](pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md)
  and the [F3-PREP design candidate](pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md)
  (its §3, §4, §8, §9 still apply by reference)
- Decisions: [final decision preparation record](../validation/pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md)
  v4.0; amendments: [proposals](../validation/pre_g9b_r6_plus_f3_normative_amendment_proposals.md)
- `selfApproved = false`

This successor changes the earlier candidate only where the decision
reconciliation established a fact: the recovery procedure (§3), the
store-awareness of the transaction (§2) and the conditional branches for the
pending choices (§5). All other content of the restricted candidate stands.

## 1. Ownership (U0)

| Concern | Owner |
|---|---|
| File → Insert surface, chooser, OFF policy, messages | GeoCeDG Desktop (profile, registry, `GuiManagerGeoCeDG`, `GeoCeDGDocumentPolicy`) |
| admission, dry run, verification, recovery, undo integration | GeoCeDG Desktop orchestration (`GeoCeDGDocumentInsertion`, proposed) |
| operation-scoped buffer, provenance, reused-macro suppression | GeoCeDG Desktop (`GeoCeDGInsertionCopyPaste`, proposed subclass of `CopyPasteD`) |
| helper lifecycle, recent-list rule | `AppGeoCeDG` |
| macro equivalence | existing `GeoCeDGUserToolLibrary` definition digest |
| construction copy, identities, macro semantics, undo storage | shared kernel and inherited Desktop, unchanged |

No shared or inherited change under K1 or S; K2 would move rollback and undo
storage into shared code (assessment §3).

## 2. Transaction

```text
1 ADMIT        container, preflight, helper load, matrix (P4), runtime features,
               transitive macro closure and digest rules (P3), identity + macros
               rejection, label renaming plan, no scripts            → reject: nothing changed
2 DRY RUN      same insertion on a scratch host from the target XML, strict check
                                                                       → reject: nothing changed
3 COMMIT       insertFrom(helper, target, conflicts, false) on the operation buffer
4 STORE-AWARE  pasteStored := commit returned normally AND a created object participates
               in the identity registry; if pasteStored, await that store
5 VERIFY       count; pairwise class and parent algorithm; closure; identity records;
               macro bindings
6 SUCCESS      if not pasteStored: store one undo point and await it; D1 notice; done
7 FAILURE      §3
```

## 3. Recovery (pending `DQ-F3-21`)

| Branch | Applies when | Procedure | Guarantees |
|---|---|---|---|
| K1-S | commit returned normally and not `pasteStored` | remove created objects in reverse order; remove transferred macros; restore saved flag | instances, session, undo history kept (`DQ21-Q3`) |
| K1-R | commit threw (inherited rollback already rebuilt) or `pasteStored` | if `pasteStored`: `undo()` then `pruneStateList()`; remove transferred macros; restore saved flag | serializable state and undo history kept; session reset declared (`DQ21-Q2`) |
| serious | inherited rollback threw "could not restore", awaited store missing, or a post-recovery certificate check fails | no further mutation; distinct error; mark modified | report only (`DQ21-Q5`) |
| forbidden | — | immediate `restoreCurrentUndoInfo()` after an identity-bearing commit | lets the insertion reappear (`DQ21-Q1`) |

If the author selects **S for identity-bearing payloads** instead of K1-R, step 1
rejects every payload whose closure contains an identity participant, and K1-R
is never used. If the author selects **K2**, this design is superseded by a
reclassified shared design.

## 4. Matrices

Admissibility: `P4` (risk candidate §2 as reconciled). Style operation: `P5`.
Messages: `P6`.

## 5. Conditional branches

| Pending decision | Branch A (recommended) | Branch B |
|---|---|---|
| `DQ-F3-21` | K1 refined | S for identity-bearing payloads, or K2 (reclassification) |
| `DQ-F3-22` | MU-1 | MU-2 (not established) / M0 |
| `DQ-F3-23` | `document.apply-styles`, "Apply Styles…" / "Aplicar estilos…" | `document.apply-template`, "Apply Template…" / "Aplicar plantilla…" |
| `DQ-F3-24` | reject before mutation | (no U0 alternative) |
| `DQ-F3-16` | amended `DQ-D1-7` (`P2`) | keep `DQ-D1-7` + inherited `CopyPasteD` fix |

## 6. Verification consequences

K1 or S: `INTEGRATED_PHASE` (registered `PHASE` + `INTEGRATION`), not frozen
until authorization. Added tests to the restricted candidate §5:
`T-F3R-UNDO-RACE` (reproduces `DQ21-Q1`; proves no reappearance),
`T-F3R-RECOVERY-S` (`DQ21-Q3` certificate, IsoABorder link kept),
`T-F3R-RECOVERY-R` (`DQ21-Q2` certificate and history),
`T-F3R-RECOVERY-SERIOUS` (`DQ21-Q5`, distinct message), and the selection and
Move-mode effects documented, not asserted as preserved.
