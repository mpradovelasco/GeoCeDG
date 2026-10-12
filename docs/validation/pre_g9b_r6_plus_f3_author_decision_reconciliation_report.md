# PRE-G9B-R6-plus-F3 — author-decision reconciliation report

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author
  instruction of 2026-10-12; documentary reconciliation with narrowly scoped
  technical checks)
- Status: **RECONCILIATION COMPLETE — PENDING FINAL AUTHOR DISPOSITION**
- Base: `T_F3_RISK` `8b41af11316f90d7a068ce6af4859213240ce6a6`, tree
  `8be60e86e09f957f1735990b966190bd44ffab2a`, parent
  `0eb50311d55a099de70e3f222b885b31685ac0df`; published `main`
  `e70718fec3612814144cd405d46f6c08864ceadd`
- Sources read: the three earlier decision records
  ([v1](pre_g9b_r6_plus_f3_author_decision_preparation_record.md),
  [v2](pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md),
  [v3](pre_g9b_r6_plus_f3_focused_author_decision_preparation_record.md)),
  the [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md),
  the focused [feasibility report](pre_g9b_r6_plus_f3_focused_feasibility_report.md)
  and [risk closeout candidate](pre_g9b_r6_plus_f3_risk_closeout_candidate.md),
  the [conservative-scope assessment](pre_g9b_r6_plus_f3_conservative_scope_assessment.md)
- New: [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md),
  [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_reconciliation.md),
  [normative amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals.md),
  [final decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md),
  [design successor](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
- `selfApproved = false`

## 1. Entry gate (2026-10-12)

| Check | Result |
|---|---|
| HEAD, tree, parent | `8b41af11…`, `8be60e86…`, `0eb50311…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` |
| `main`, `origin/main`, live remote `main` | `e70718fe` |
| receipt | `verification-adc532aa07cf4706861e6957bfea25d9`, `ACCEPTED / COMPLETE`, bound to `8b41af11`/`8be60e86` |
| worktree | clean |
| product delta since `main` (`source/`, `apps/`, `tools/`, `geocedg/specs/`, `AGENTS.md`, `CLAUDE.md`) | none |
| the eight risk-closeout artifacts | present (feasibility report, risk candidate, v3 record, restricted design, JSON, prompt, roadmap, plan) |
| F3 status | `PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED`; roadmap 5.24 "F3 FOCUSED RISK CHARACTERIZATION — DOCUMENTARY CANDIDATE" |
| branch | `phase/pre-g9b-r6-plus-f3-author-decision-reconciliation` (local), from `8b41af11` |

## 2. Method

Documentary reconciliation of all `DQ-F3-*` against the recorded evidence, plus
one narrowly scoped scratch probe (`F3DecisionProbeTest`, two methods) for the
single factual uncertainty that decides `DQ-F3-21`: how a commit-time failure
interacts with the asynchronous undo store, and whether a non-rebuilding
compensation exists under U0 (assessment §2, §6). The probe found that the
compensation procedure recorded by the focused closeout lets a failed insertion
reappear (`ER-F3R-1`).

## 3. Decision matrix

Columns: **Q** question (title as recorded); **Dir** previous author direction;
**Ev** evidence; **Res** proposed resolution; **Restr** scope restrictions;
**Unc** remaining uncertainty; **Appr** author approval needed; **Impl**
implications for `F3-D2`, `D1`, downstream.

| Id | Q | Dir | Ev | Res | Restr | Unc | Appr | Impl |
|---|---|---|---|---|---|---|---|---|
| `DQ-F3-1` | File → Insert submenu structure and action identities | small declarative surface (2026-10-12) | K3 | group `file-insert`; `document.insert-file`; second action `document.apply-styles` if `DQ-F3-23` S1, else `document.apply-template`; `geocedg.document.*` targets | catalog 131 → 133 | none | yes | `F3-D2` 1–2 naming |
| `DQ-F3-2` | GeoCeDG chooser architecture and minimal inherited seams | U0 preferred | K4; G8 | A1: GeoCeDG Open override; GeoCeDG insertion chooser; never `AppD.insertFile`/`applyTemplate` | Windows behavior; macOS sandbox branch not reproduced | none | yes | none |
| `DQ-F3-3` | `.cedg`/`.ggb` selection, default extension and fallback rules | `.cedg`/`.ggb` subject to admissibility | K4, K8 | X1: one filter; stem → `.cedg`, then `.ggb`; single selection; other extensions refused | containers only | none | yes | `F3-D2` 3 reading |
| `DQ-F3-4` | OFF file UI policy, including explicit typed extensions | exclude OFF from the GeoCeDG UI | K4 | OFF-B: no filter; refuse explicit and fallback `.off` with a message | internal OFF untouched | none | yes | none |
| `DQ-F3-5` | Insert File object, macro and label-conflict behavior | L1, no overwrite | G4 | L1 deterministic renaming; no overwrite; no label dialog | no scripts; static strings not remapped | none | yes | `F3-D2` 4 |
| `DQ-F3-6` | Insert File undo, cancellation and partial-failure policy | one coherent operation; no mutation on failure | G2; `DQ21-*` | one undo step (store only when the paste did not); failures per `DQ-F3-18`/`-21` | — | commit residual | yes | `F3-D2` 4 |
| `DQ-F3-7` | `D1` unit provenance and physical-mismatch integration | preserve D1; never scale | G6 | provenance through the operation-scoped buffer | — | none | yes | `DQ-D1-7` amendment |
| `DQ-F3-8` | Apply Template unit and geometry invariants | T0 | G7 | T0 (P5) | six properties; exclusions | none | yes | `F3-D2` 5 |
| `DQ-F3-9` | Apply Template effects on working and hidden layers | preserve layer organization | G7 | moot under T0 (layers never touched) | — | none | with `DQ-F3-17` | none |
| `DQ-F3-10` | Hidden-helper, recent-file and clipboard side effects | no unexpected session effects | G1–G2 | H-1 (no recent-list publication by helpers), H-3 (release, executed), independent buffer | — | none | yes | none |
| `DQ-F3-11` | Classic and legacy-profile compatibility | Classic unchanged | K10 | F-1: Classic unchanged; v1 fallback keeps the inherited dialog | — | none | yes | none |
| `DQ-F3-12` | Architecture, phase classification and acceptance verification | evidence-driven | G8; assessment §3 | `INTEGRATED_PHASE` (not frozen) if `DQ-F3-21` K1 or S; reclassification review if K2 | — | depends on `DQ-F3-21` | yes | class frozen at authorization |
| `DQ-F3-13` | Insert File contract and the reading of `F3-D2` items 3–4 | R | G2–G5 | A13-1 with P1 item 3–4 text | admission + dry run + strict verification | none | yes | `F3-D2` 3–4 amendment |
| `DQ-F3-14` | Admissibility matrix and exclusions | specialized objects individually, high priority | G5; `RK-dynamic` | matrix P4 | conditions per type | malformed macros as a distinct case | yes | none |
| `DQ-F3-15` | Macro support in the initial F3 | M1 objective | G3, V4, V5, V7 | M1 per P3 | transitive closure; digest reuse; reject different definitions; reject identity + macros | user macros from independent documents compare unequal (fail closed) | yes | none |
| `DQ-F3-16` | Amendment of `DQ-D1-7`: dedicated insertion buffer | independent buffer | G6 | B16-1 with P2 text | — | none | yes (normative D1 amendment) | `DQ-D1-7` |
| `DQ-F3-17` | Apply Template contract and the amendment of `F3-D2` item 5 | T0 | G7 | P5 | exclusions; defaults and labeling deferred | none | yes | `F3-D2` 5 |
| `DQ-F3-18` | Atomicity mechanism for Insert File | fail closed | G2; `DQ21-*` | admission + strict dry run + commit + strict verification; recovery per `DQ-F3-21` | — | commit residual | yes | none |
| `DQ-F3-19` | Optional inherited corrections for Classic | U0; minimal changes only if necessary | G8 | U0 only; optional Classic fixes out of F3 | — | none | yes | none |
| `DQ-F3-20` | Debt closure under an amended scope | — | — | C20-1: debt closes with the approved amendments; deferred items (template defaults and labeling, M2, scripts, 3D, unlisted types) recorded | — | none | yes | debt closure |
| `DQ-F3-21` | Commit-time failure after a passing dry run | author decides acceptability of session reset | `DQ21-Q1`–`Q5` | **K1 refined** (K1-S where possible, K1-R with await/undo/prune otherwise, serious-failure path); S per payload class only if the author excludes K1-R | immediate `restoreCurrentUndoInfo()` forbidden | recovery-failure state | **yes — blocks authorization** | P6; class |
| `DQ-F3-22` | Undo of transferred macros | — | G3 (`afterUndo.macros`, Redo correct) | MU-1: transferred macros stay registered after Undo of a successful insertion (document tools); reuse transfers nothing | failed insertion removes them; never remove a used macro | the saved document keeps an unused transferred tool after Undo + Save | yes | P3 |
| `DQ-F3-23` | User-facing name of the T0 operation | investigate "Apply Styles" | G7; upstream ES string "Aplicar estilos" | S1: "Apply Styles…" / "Aplicar estilos…" | — | none | yes | `F3-D2` 2; guides |
| `DQ-F3-24` | Payloads needing both identity records and macros | respect inherited limitation | G3 `M-mixed`; S `CopyPasteD.java:588-602` | R24-1: reject before mutation with a diagnostic | only the combination is rejected | none | yes | future extension path below |

`PENDING AUTHOR DISPOSITION` applies to every row. No recommendation above is a
decision.

## 4. Consistency checks

- `DQ-F3-6`, `-18` and `-21` describe one transaction; P6 is the single set of
  user messages; no row promises runtime atomicity for K1-R.
- `DQ-F3-15`, `-22` and `-24`: macro transfer is part of the transaction (V7
  cleanup), Undo keeps transferred tools (MU-1), and identity + macros are
  rejected before any of it.
- `DQ-F3-14` and `-21`: identity-bearing types (Locus V2 family, Spline V2) use
  K1-R; dimensions, IsoABorder and elementary types use K1-S. If the author
  rejects K1-R, `DQ-F3-14` must exclude the identity-bearing types (S), which
  contradicts the specialized-object priority — this coupling is explicit in the
  decision sheet.
- `DQ-F3-16` and `DQ-F3-7`: one provenance mechanism; the window buffer is
  unaffected by every recovery path.
- `DQ-F3-17`, `-23` and `-9`: T0 never touches layers; the name follows T0.
- `DQ-F3-12`, `-19` and `-21`: U0 and `INTEGRATED_PHASE` hold for K1 and S;
  K2 alone would change both.

## 5. `DQ-F3-24` diagnostic and extension path

User message: "*name* was not inserted: it contains objects with semantic
identities (for example Locus V2) that depend on tools (macros). GeoCeDG cannot
yet insert both together. Insert the parts separately or open the document."
Future extension (separate phase, not F3): a specification of macro import for
identity-bearing payloads in the G9A identity protocol, then a shared change of
the refusal at `CopyPasteD.java:588-602`.

## 6. What the author is asked

The minimal decision sheet is §6 of the
[final decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md).
