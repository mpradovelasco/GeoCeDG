# PRE-G9B-R7 — author closeout record

```text
PRE-G9B-R7                    = PASS — AUTHOR APPROVED
R7 AUTHOR SMOKE               = PASS
R7 FINAL                      = ACCEPTED / COMPLETE (41/41)
R7 CANONICAL RECEIPT          = AUTHENTICATED
APPROVED TECHNICAL CANDIDATE  = 0120cc40c4c49bc159e930dc42d1fe252467c7bf
                                tree 21d563cfc93cea67b0e202957074944f650461c5
                                parent b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513 (published base)
TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER = OPEN — CORRECTION DEFERRED — AUTHOR DECIDED
G11 / G12 / G15               = PENDING — REVISED FUTURE SCOPE
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15
COMMERCIAL POLICY STATUS RECONCILIATION = AUTHOR ACCEPTED — DOCUMENTARY ONLY
G9B / G9C / G9U2 / PRODUCTIVE G10 = NOT AUTHORIZED
PUBLICATION / PUBLIC RELEASE  = NOT AUTHORIZED
selfApproved                  = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-R7 FINAL AUTHOR APPROVAL,
DECISION ADOPTION AND DOCUMENTARY CLOSEOUT" (2026-10-15), which adopts the final decision
package of the R7 decision review, including the modifications identified in that review.
It is the sole authority for the author's approval of R7. The frozen candidate's
[candidate report](pre_g9b_r7_candidate_report.md) and evidence mirror keep
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. This closeout descends linearly from the
candidate and changes documentation and mirrors only; FINAL is not rerun. Machine-readable
mirror: [`pre-g9b-r7-closeout.json`](../../geocedg/validation/pre-g9b-r7/pre-g9b-r7-closeout.json).

## 1. Author decision

| Field | Value |
|---|---|
| Instruction | "GeoCeDG — PRE-G9B-R7 FINAL AUTHOR APPROVAL, DECISION ADOPTION AND DOCUMENTARY CLOSEOUT" (2026-10-15) |
| Author smoke | `R7 AUTHOR SMOKE = PASS` (author statement) |
| Decision | approval of the exact candidate `0120cc40`, covering the accumulated scope and verification of R7; adoption of `DEC-R7-D1` to `D6` and `DEC-R7-G1` to `G9` as modified by the final review |
| Not authorized | product or test change; a new verification campaign; specification or ADR amendments; implementation of TD-R3's correction, G11, G12, G15, `ENH-B-MANUAL-EXPORT-AREA-DRAG`, G9B, G9C, G9U2 or productive G10; push, merge, tag, release or publication |

## 2. Approved candidate: identity and authentication (2026-10-15)

| Item | Value | Check |
|---|---|---|
| candidate | `0120cc40c4c49bc159e930dc42d1fe252467c7bf`, tree `21d563cfc93cea67b0e202957074944f650461c5` | exact; worktree and index clean before this closeout |
| parent | `b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513`, tree `3aef929b0b5939854d9ccd736a919b496172a30e` | direct parent; equal to local `main`, `origin/main` and live remote `main` |
| delta base → candidate | documentation, roadmap, the R7 prompt and `geocedg/validation/pre-g9b-r7/**` only | no source, test, build, packaging-input, registry or schema path |

Each result was re-authenticated with `Read-VerificationJson` and `Get-VerificationResultHash`
(recorded and recomputed hash equal; run state and journal `COMPLETED`; candidate and tree
equal to the candidate):

| Check | `run_id` | `result_hash` | Result |
|---|---|---|---|
| `STATIC` | `verification-32aafdd48c0e46fbb9b286d75b128baf` | `b2bd0e5ce5eb4c864176c03461d8bb43bfd7ff317f8a73cc6f1609253736c052` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |
| `PHASE -Phase PRE-G9B-R5-A` | `verification-67334c3e1a3e40aea8b07a0c60d5a6d6` | `dc63ca5c598cd225d6f69525c28e1ce3fe0afb3f99c066dcb6c0af5c2e7ebfca` | `ACCEPTED / COMPLETE`, 4/4 |
| `PHASE -Phase PRE-G9B-R6` | `verification-20a2060f53184486a6766cf0cee5f61b` | `e450a537c6a0e8a500e42c1e8bb9c2c225f9cdee5fec2fdf5f6ac23eeb59abb5` | `ACCEPTED / COMPLETE`, 6/6 |
| `FINAL` | `verification-58826f64bff64fbd991d20df1e480174` | `ac561fddb07ffc720d68709b8daea89aa63cd88838c455726f3e2768435e88ad` | `ACCEPTED / COMPLETE`, 41/41; plan `3d8752b9…`; Desktop 2153 tests (1 skipped), shared 6992 (10 skipped), 0 failures; O-1, O-2, O-27 did not recur |

Canonical receipt `bbef47fa023918cab3684ec164a4814112dd6a01f2d90982cbcc9403fc2af8e4` (kind
`GEOCEDG_ACCEPTANCE_RECEIPT`, file SHA-256 `0db9da4f…`; `accepted_report_hash` equal to the
FINAL result hash; plan `3d8752b9…`): `tools/agent/phase-closeout.ps1 -Action INSPECT`, exit 0,
`identity_confirmed = true`, no mismatch, no verification executed, no repository mutation.
Evidence: ignored `artifacts/agent/pre-g9b-r7-*-0120cc40/` and
`artifacts/agent/pre-g9b-r7-0120cc40-campaign-evidence/`.

Historical verdicts are unchanged: `PRE-G9B-R1`'s FINAL stays `REJECTED_SEMANTIC` with its
author baseline exception, and `PRE-G9B-R3-X1`'s source FINAL stays
`REJECTED_VERIFICATION_CORE / UNTRUSTED` with its GOV-R reconciliation receipt.

## 3. Technical-debt decisions (D1–D6)

Applied individually in the [debt reconciliation](pre_g9b_r7_debt_reconciliation.md) §8 and
its mirror.

| Decision | Author disposition |
|---|---|
| DEC-R7-D1 | `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER = OPEN — CORRECTION DEFERRED — AUTHOR DECIDED`; owner `AUTHOR — ACCOUNTABLE`. A future, separately authorized correction must investigate and preserve constructive dependencies, semantic identity, DAG invariants, persistence, undo/redo and compatibility; it is not a visual reordering. Not resolved, not an accepted limitation; no phase number assigned; no correction within R7 |
| DEC-R7-D2 | resolved by evidence, exact scopes: `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` resolved by `PRE-G9B-R3` (D3-b); `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION` resolved by the LF materialization correction `eab5a7937`, confirmed by the accepted R7 FINAL (`PostP1BilingualUserGuideTest` 16/16); `GUIDE-A1-SECTION-9.3-STALE` resolved by the approved R2-E0/E1 documentation correction (pinned test passing in the R7 FINAL); `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` resolved for newly created objects by A-1, the layer behaviour of documents created before A-1 an `ACCEPTED LIMITATION` |
| DEC-R7-D3 | the individually reviewed classification of §8.3 of the debt reconciliation; `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD = OPEN — RETAINED WITH OWNER — FUTURE MIGRATION DECISION` (author change) |
| DEC-R7-D4 | P-12 and the pinned P-13 headers: `RETAINED AS HISTORICAL PINNED HEADER — CURRENT STATUS RECORDED BY CROSS-REFERENCE`; owner author; files, pins and verifier scripts unchanged; a re-pin is a separate activity |
| DEC-R7-D5 | planning owners: G15 for O-4, O-10, O-22, O-25 and L-7 tracking; future insertion-and-styles activity for O-6, O-8; future characterization activity for O-9; future serialization owner for O-7; future verification-maintenance activity for O-19 to O-21 and O-23; the author for every other item. The author stays accountable until each unnamed activity is formally established |
| DEC-R7-D6 | `OBS-R7-G9R4-LIVING-DOC-DRIFT`: a separate bounded documentary task (it may be planned with P-18 and DEC-R7-G9, each keeping its own authorization); no normative specification is edited by this closeout. `OBS-R7-UNREGISTERED-HISTORICAL-SUITES = OPEN — RETAINED WITH OWNER` (author) |

## 4. Roadmap decisions (G1–G9)

Applied to the [roadmap](../roadmap/geocedg_roadmap.md) G11, G12 and G15 sections and recorded
in the [roadmap-review package](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md) §9.

| Decision | Author disposition |
|---|---|
| DEC-R7-G1 | approved: evidence-based delivered-slice paragraphs |
| DEC-R7-G2 | approved with modifications: revised "Trabajo"/"Salida" without deleting unresolved requirements; G15 states the `ENH-B-MANUAL-EXPORT-AREA-DRAG` UX (explicit control in the existing coordinate dialog, rectangular pointer gesture, same `ExportArea(MANUAL)`, editable fields, no second authority); G12 distinguishes model units, view transformations, drawing scale, physical output scale and future paper-space viewport scale, keeps geometric model quantities independent of zoom, DPI, camera and viewport presentation transforms, and introduces no new scale authority |
| DEC-R7-G3 | approved within its tested scope: A7 complete for the primary Desktop Graphics View; secondary 2D views, 3D and Web outside that claim |
| DEC-R7-G4 | retained as a future normative design decision: independence of drawing, physical print and viewport scales needs an approved contract; no scale authority approved now |
| DEC-R7-G5 | retained as a future normative design decision: document-persistent sheet and export-presentation semantics belong to G15 design with an ADR; session semantics unchanged |
| DEC-R7-G6 | approved: `FitLayer` owned by G12, consuming future G11 semantics |
| DEC-R7-G7 | approved: extended scale profiles and sheet sets are not deleted; contracts set in future design |
| DEC-R7-G8 | approved: decided planning owners integrated in the G11, G12 and G15 sections |
| DEC-R7-G9 | approved as future separate normative maintenance: the obsolete A7 and print-scale wording of `cedg-workspaces.md` is retained; no specification amendment within this closeout |

`G11`, `G12` and `G15` are `PENDING — REVISED FUTURE SCOPE`; none is implemented, completed
or authorized. `IsoABorder != DrawingSheet` is preserved.

## 5. Commercial-policy documentary disposition

`COMMERCIAL POLICY STATUS RECONCILIATION = AUTHOR ACCEPTED — DOCUMENTARY ONLY`. The candidate's
change to `docs/licensing/commercial-policy.md` is one paragraph of the PROFILE NC section
(4 lines added, 3 removed): PROFILE NC is the explicitly selected non-commercial packaging
profile, `INTERNAL` remains the repository default, and building a package is not a release.
COMMERCIAL stays unauthorized (package profile and the COMMERCIAL profile record);
COMMERCIAL-A, COMMERCIAL-B, the mark status and the policy boundaries are unchanged; no
licensing permission, release authorization or trademark right is inferred.

## 6. Debt state at closeout

Every accumulated `PRE-G9B-R` item has a terminal disposition or an explicit retained state
with an accountable owner (0 unowned; R6-plus register 134 entries, 0 unowned). Open
obligations kept open: TD-R3 (future correction), `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD`
(future migration decision), the retained R-track observations of the reconciliation §8.3,
the author-accountable R6-plus rows (B-1 residue, P-4, P-10, P-16, P-18, D-9, O rows), the gate
owners' rows (G11, G12, G15) and `OBS-R7-*`. The reconciliation is a phase-specific view
(`AD-R0-7`), not a global ledger.

## 7. Closeout gate evaluation

| Condition | Result |
|---|---|
| exact candidate authenticated, FINAL accepted, receipt inspected | met (§2) |
| author smoke | `PASS` (author) |
| every debt with a terminal disposition or an accountable owner | met (§3, §6) |
| roadmap states the exact status of each phase | met (roadmap 5.58) |
| R0 author decisions resolved | met (`AD-R0-1` to `AD-R0-9`) |
| explicit author decision about what follows | met (§8) |

Result: **`PRE-G9B-R7 = PASS — AUTHOR APPROVED`**. The approval concerns this completed R7
work only.

## 8. What follows

The author decides that the next operation is a separate roadmap-prioritization and
authorization decision; no productive phase is selected. Possible future work (planning
context only, none authorized): a bounded corrective activity for retained debts (TD-R3
first among them); normative documentation maintenance (P-18, DEC-R7-G9, the G9-R4 drift);
G11/G12/G15 preparation; G9B preparation; a separately decided NC release process.

## 9. Publication boundary

The publication target is the commit adding this record (descending linearly from the
candidate and from the published base `b9bdb1e1`). Its identity and `STATIC` result are
reported outside this record. Publication needs a separate author instruction naming that
SHA and authorizing a non-force fast-forward of `main`. No push, merge, rebase, tag, release,
binary publication or public or commercial distribution is authorized.
