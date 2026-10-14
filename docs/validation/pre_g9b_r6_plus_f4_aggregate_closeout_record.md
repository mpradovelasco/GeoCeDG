# PRE-G9B-R6-plus-F4 — aggregate documentary closeout record

```text
PRE-G9B-R6-plus-F4        = COMPLETE — AUTHOR APPROVED (aggregate documentary closeout;
                            not a new implementation phase)
F4-A                      = PASS — AUTHOR APPROVED — PUBLISHED
F4-B                      = CLOSED — RESOLVED FOR AUTOCAD 2026 (no other version claimed)
F4-C                      = DEFERRED — TD-PDF-1 (physical text-size presentation contract
                            outside F4; debt open)
F4-D                      = PASS — AUTHOR APPROVED — PUBLISHED
ADDITIONAL PRODUCT CHANGE = NONE
F4 AGGREGATE PUBLICATION  = PENDING SEPARATE AUTHORIZATION
PRE-G9B-R6-plus-G         = NOT AUTHORIZED (preparation authorized separately)
PRE-G9B-R6-plus GLOBAL CLOSEOUT = NOT AUTHORIZED (owned by G)
selfApproved              = false
```

## 1. Author instruction and scope

On 2026-10-14 the author authorized "GeoCeDG — F4-A PUBLICATION + F4 AGGREGATE CLOSEOUT + G
READINESS". Operation 2 authorizes the documentary aggregate closeout of
`PRE-G9B-R6-plus-F4`, conditional on the successful publication and audit of F4-A
(Operation 1, §2). It consolidates already approved results. It adds no product,
source, test, verification-infrastructure or specification change, and modifies no
accepted evidence. It approves no deferred feature and no open debt. The F4 completion
recorded here is distinct from the global `PRE-G9B-R6-plus` closeout, which belongs to G.
Mirror:
[`pre-g9b-r6-plus-f4-aggregate-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-aggregate-closeout.json).

## 2. Precondition: F4-A publication (Operation 1)

| Check | Result |
|---|---|
| preflight | technical `6baa66a5` (tree `bfc22c5d`) and closeout `07424dee` (tree `59d2e022`) re-resolved; linear chain `e968245e` → `1b944afb` → `e7b235dc` → `72f6c5af` → `6baa66a5` → `07424dee` (5 commits, no merge); WIP `9b755225` excluded; closeout documentation-only; clean tree; PHASE, INFRA_UNIT, STATIC and closeout STATIC receipts re-authenticated (result hashes recomputed) |
| push | live remote `main` re-read as `e968245e` immediately before; `git push origin 07424dee6e5293c6259e84b97ca0ea3e6e5fc72f:refs/heads/main` (non-force) → `e968245ef..07424dee6`; GitHub reported the administrator bypass of "Changes must be made through a pull request." and "Cannot change this locked branch" |
| audit | PASS: local `main` = `origin/main` = live remote `main` = `07424dee6e5293c6259e84b97ca0ea3e6e5fc72f`, tree `59d2e022a82f474fc2c2e63ada462800dc43ed81`; fast-forward from `e968245e`; 5 commits, 0 merges; no WIP published; clean; gate evidence hashes unchanged; no tag; record `artifacts/agent/f4a-publication-audit/publication-audit.json` (SHA-256 `0d385a1e15c228883abd59d17f901762f896291ac39e9ef6db0008921cb2021e`) |

```text
PRE-G9B-R6-plus-F4-A = PASS — AUTHOR APPROVED — PUBLISHED
```

## 3. Track dispositions

### 3.1 F4-A — numeric thickness editor in the compact GeoCeDG style bar

| Field | Value |
|---|---|
| state | `PASS — AUTHOR APPROVED — PUBLISHED` |
| approved implementation | `6baa66a584ceef1973c854fb63b00aa7070708b4`, tree `bfc22c5dff6a1dcb7a371030c77f695a1a6b1925` |
| author closeout | `07424dee6e5293c6259e84b97ca0ea3e6e5fc72f` ([record](pre_g9b_r6_plus_f4_a_author_closeout_record.md)) |
| scope | architecture A1; Graphics 1 and Graphics 2; E5 light-weight popup without drop shadow; slider plus numeric field on one row; Properties field and Classic unchanged; no inherited source change |
| automated evidence | PHASE `verification-528e53e44fa1400e933ead8cff52f4e5`, INFRA_UNIT `verification-0ea5a0c6b4e84831a6bb017088df73a8`, STATIC `verification-63e2bb4e88b441d3b4184ecbcfb449b4`; real-window gate `F4-A E5 FOCUS FEASIBILITY = PASS` (`artifacts/pre-g9b-r6-plus-f4-a/e5-gate-evidence/`, `SHA256SUMS` `7b64623c…`) |
| author smoke | `AUTHOR_SMOKE = PASS`, an author confirmation, separate from the automated results |
| accepted limitations | F4-A-L1 (editor disabled under a heavy-weight popup), F4-A-L2 (one redraw after a visible tooltip), F4-A-L3 (validated on Temurin 25.0.4 and FlatLaf 3.7) |
| history (not in the approved code) | STOP `e7b235dca9afc68f051faefa133e2db585bfea4c` ([report](pre_g9b_r6_plus_f4_a_focus_gate_stop_report.md), evidence `3c432fcd…`); WIP `9b755225eadf3c2040029f6bee0b455f1d5bb89c` (local, excluded); scope amendment 01 `72f6c5af` |

### 3.2 F4-B — DXF compatibility with AutoCAD

`CLOSED — RESOLVED FOR AUTOCAD 2026` (`DQ-F4-6`; [design closeout](pre_g9b_r6_plus_f4_author_design_closeout_record.md)).
The basis is the AutoCAD 2026 evidence of E3-R1, confirmed by the author, and the
F4-PREP re-check of the eight G5 entity families on `847bb20a`. That re-check covered
both routes and gave a clean AUDIT
([characterization report](pre_g9b_r6_plus_f4_preparation_characterization_report.md) §5).
AutoCAD 2024 and other releases, AutoCAD LT, DWG TrueView and other readers are **not
rechecked**. That is a coverage gap; no compatibility is claimed for them.

### 3.3 F4-C — physical PDF text size

`DEFERRED — TD-PDF-1` (`DQ-F4-8`). No defect against an approved contract. A new physical
text-size presentation contract is required, outside F4. The debt stays **open** and is
not closed here.

### 3.4 F4-D — embedded-font PDF export

| Field | Value |
|---|---|
| state | `PASS — AUTHOR APPROVED — PUBLISHED` |
| approved implementation | `6214d15a0bc986cf1d16f178c67fd76bf6e54bc2`, tree `47b634b1885a9b5650af73369caa97a955bf22f2` |
| author closeout | `e968245ef329a86108df81a49cbdb05601cefe9e` ([record](pre_g9b_r6_plus_f4_d_author_closeout_record.md)); published state recorded by `1b944afb` |
| scope | `AbstractCharTable.toName(char)` delegation (`TD-PDF-2`) and the zero-displacement Type 1 moveto in `CharstringEncoder.move()` (`TD-PDF-3`) |
| automated evidence | PHASE `verification-0d7d0418479645779c9c2690ab50b140`, INTEGRATION `verification-c1f5709d41444f6e88c801a9d4ba9189`, INFRA_UNIT `verification-1abe42a5651e4dafb469838331413042`, STATIC `verification-e9a03b359f8f43208ebec344a125ca7e`; external Ghostscript, Poppler and Xpdf evidence ([candidate report](pre_g9b_r6_plus_f4_d_candidate_report.md) §6) |
| author smoke | `AUTHOR_SMOKE = PASS`, an author confirmation |
| debts | `TD-PDF-2` and `TD-PDF-3` **CLOSED** |
| history (not in the approved code) | STOP `aae7b12ccc7e8c21eced1b10f7aa08562ae2bec2` ([report](pre_g9b_r6_plus_f4_d_implementation_stop_report.md)); WIP `6c2fc40918be3e43a1df09a454f407be85acd553` (local, excluded); scope amendment 01 `39687713` |

## 4. Evidence reconciliation

- **Published main.** Every approved F4 implementation and closeout commit (`6214d15a`,
  `e968245e`, `6baa66a5`, `07424dee`) is an ancestor of the published `main` `07424dee`.
- **WIP branches.** Neither WIP branch (`6c2fc409`, `9b755225`) is an ancestor.
- **Receipts.** The F4-A receipts were re-authenticated in Operation 1. The F4-D receipts
  were authenticated at the F4-D closeout and are not changed.
- **Author smokes.** Both are recorded as author confirmations; the agent performed
  neither.
- **No new evidence.** No verification campaign was rerun for this aggregate.

## 5. Accepted limitations and remaining debts after F4

| Id | State | Owner |
|---|---|---|
| F4-A-L1, F4-A-L2, F4-A-L3 | accepted limitations of F4-A | F4-A contract; L3 triggers renewed verification on a runtime or look-and-feel change |
| `F4R-LINETYPE-COUPLING` | retained, accepted (`DQ-F4-4`), inherited | — |
| `F4R-DEFAULTS-UNDO` | retained, documented, inherited | — |
| `TD-PDF-1` | **OPEN — DEFERRED** (contract design required) | future contract phase; G structured register |
| AutoCAD versions other than 2026 | **NOT RECHECKED — COVERAGE GAP** | optional author-controlled validation |
| `OBS-F4P-FREEHEP-XREF-SIZE` | **OPEN — INDEPENDENT** (Ghostscript repairs "incorrect xref size" on every FreeHEP PDF) | proposed for the G structured register |
| `OBS-F4P-S3-SPEC-HEADER`, `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | REGISTERED (F4-PREP), documentary | proposed for the G structured register |

Closed by F4:
- `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`, with F4-A-L1;
- `TD-PDF-2` and `TD-PDF-3`;
- `F4R-POPUP-FOCUS`, `F4R-EMBED-FIDELITY`, `F4R-SHAPES-UNCHANGED`,
  `F4R-FAILED-EXPORT-REPORTING` (for F4-D) and `F4R-CLASSIC-LEAK`.

## 6. Relation to G and the global closeout

F4 is now complete. In the accepted dependency graph of the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md) (§3.1), F4 precedes G
operationally (author planning decision of 2026-10-09: "to be proposed between F3 and
G"). It is **not** a hard semantic dependency of G, and this record introduces none. The
global `PRE-G9B-R6-plus` closeout, which `PRE-G9B-R7` requires, remains the closeout of G
and is not authorized.

## 7. Closeout verification and publication readiness

Closeout class `DOCUMENTATION_STATUS_ONLY` covers this record and its mirror, the F4 risk
and debt register §8, roadmap 5.50 and the mini-track plan. Acceptance is by the official
`STATIC` on the committed aggregate closeout, reported outside this record.

The record cannot name its own commit. Publication of that commit needs a separate author
authorization naming its exact SHA. The commit descends linearly from the published
`main` `07424dee`, so an ordinary non-force fast-forward would be possible.
