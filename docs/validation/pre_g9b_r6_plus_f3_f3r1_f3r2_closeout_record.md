# PRE-G9B-R6-plus-F3 / F3-R1 / F3-R2 joint closeout record

```text
PRE-G9B-R6-plus-F3        = PASS — AUTHOR APPROVED (with its corrective successors F3-R1
                            and F3-R2; one approved implementation)
AUTHOR_SMOKE              = PASS WITH ACCEPTED CONTRACT CLARIFICATION (author confirmation)
APPROVED IMPLEMENTATION   = 2d7b2cae23d5229f889877f22924b3669f98b755
                            tree 667308b912c73e63d9b02c9a96b0ab1beab7cd50
                            parent c4dffe60e5721bfeba21b2c59390286b5382d855
INTEGRATED_PHASE          = ACCEPTED / COMPLETE (PHASE PRE-G9B-R6-PLUS-F3 3/3 and
                            INTEGRATION 19/19 on the approved implementation)
APPLY STYLES (T0)         = SOURCE CONSTRUCTION DEFAULTS BY OBJECT TYPE;
                            PER-OBJECT TRANSFER NOT AUTHORIZED
F3 SPECIFICATION          = NORMATIVE / AUTHOR APPROVED (document-insertion.md 1.2)
F3 ORIGINAL CANDIDATE     = 16547115 — FROZEN — HISTORICAL EVIDENCE
F3-R1 CANDIDATE           = c4dffe60 — FROZEN — HISTORICAL EVIDENCE (corrective successor
                            accepted as part of the final implementation)
DEBT-R6PLUS-FILE-INSERT-SURFACE
                          = CLOSED — FULFILLED BY F3 (C20-1)
PUBLICATION               = AUTHORIZED — CONTROLLED NON-FORCE FAST-FORWARD OF main,
                            subject to the publication gate (§12); the outcome is reported
                            outside this record
F4                        = PLANNING INTENT RECORDED — PREPARATION NEXT —
                            IMPLEMENTATION NOT AUTHORIZED
G                         = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (F3 with F3-R1 and F3-R2 only, implementation 2d7b2cae)
passClaimed               = true    (F3 only)
implementationAuthorized  = false   (F4, G, deferred F3 items, every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE    (closeout; INTEGRATED_PHASE accepted on 2d7b2cae)
closeoutClass             = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
```

## 1. Author instruction and scope

On 2026-10-13 the author authorized in writing "GeoCeDG —
PRE-G9B-R6-plus-F3-F3R1-F3R2-AUTHOR-CLOSEOUT" (joint F3/F3-R1/F3-R2 product
approval, normative promotion and controlled publication; task class
`DOCUMENTATION_STATUS_ONLY`; new product implementation not authorized). This
record is the sole authority for the author approval of F3. The agent records the
decision; it does not approve its own work. Machine-readable mirror:
[`pre-g9b-r6-plus-f3-f3r1-f3r2-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-f3r1-f3r2-closeout.json).

The author approves: (1) the final F3 implementation including its F3-R1 and F3-R2
corrections; (2) the approved restricted-insertion contract; (3) the final Apply
Styles T0 behaviour; (4) the documented compatibility restrictions; (5) the
retained, explicitly classified non-blocking observations; (6) the promotion of
the final product specification; (7) the documentary product closeout; (8)
controlled publication by an ordinary non-force fast-forward under the gates of
§12. The authorization does not cover F4 implementation, G, unrelated debts or new
product functionality.

## 2. Entry verification

```text
PUBLISHED BASE        = e70718fec3612814144cd405d46f6c08864ceadd
                        (local main = origin/main = live remote main, fetched)
LINEAR CHAIN          = e70718fe → 480c9729 → 0eb50311 → 8b41af11 → 3e24ed3a →
                        6c07e01a → 6e41bf44 → e479b76d → 9d130b43 → 108d9cb6 →
                        bc1c9ac6 → 16547115 → c4dffe60 → 2d7b2cae (13 commits, one
                        parent each, no merge; every tree re-resolved)
WORKTREE              = clean; no stash; every local F3 branch is an ancestor of
                        2d7b2cae
SCOPE                 = no change under source/shared or inherited
                        org/geogebra sources in e70718fe..2d7b2cae
```

| Commit | Role | Tree |
|---|---|---|
| `480c972972e90514e88421194a792898c916834e` | F3 preparation | `166caf6c7802c37db03060a2fd07cd71d28daa17` |
| `0eb50311d55a099de70e3f222b885b31685ac0df` | conservative reconciliation | `061e08f1a9d821cf642827afaf52d802629b940a` |
| `8b41af11316f90d7a068ce6af4859213240ce6a6` | focused risk characterization | `8be60e86e09f957f1735990b966190bd44ffab2a` |
| `3e24ed3aca422eef4e844a6c1230a3a3fc599c21` | author decision reconciliation | `65e282431a6ece824c783e8217bed74b85b79e68` |
| `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6` | `DQ-F3-21` recovery safety gate | `8f2e141bcb1311051b14d8efb3d7289b15e95212` |
| `6e41bf4426b9bdcb137f42e783038b02827ff3bd` | `DQ-F3-21` corrective feasibility | `8b614863de45a7fccb93ec751871498c8ee7bbd1` |
| `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3` | Undo history integrity gate | `749005c525af0d54724fa9acc1608bde2420253a` |
| `9d130b43fbeb731a25d5dcf6413ed0dd0439f61e` | independent audit reconciliation (approved design base) | `daec52cd9f2cfe395bfac26b9556a11276323a9f` |
| `108d9cb6591aef92135a95ec9bf71482fcd78e7f` | F3 design author closeout | `45644a6fabdb5e3b4cb23df063761f57d3fdf133` |
| `bc1c9ac6f6801e88437824ddcb981cb54d17eeb0` | implementation authorization | `e2ae0fbf26fe4ba8f33222a6118abc13d7735517` |
| `165471154cd16e8875994a21eb49fbc2f828216b` | original F3 technical candidate (historical) | `8345c54df55452ffc598c431db28e67298076589` |
| `c4dffe60e5721bfeba21b2c59390286b5382d855` | F3-R1 corrective candidate (historical) | `8c9dd493db2bcd50ae3679c8375277252f1c5424` |
| `2d7b2cae23d5229f889877f22924b3669f98b755` | **F3-R2 — final approved implementation** | `667308b912c73e63d9b02c9a96b0ab1beab7cd50` |

Authorities present: [design closeout record](pre_g9b_r6_plus_f3_author_design_closeout_record.md)
(Q1–Q7, `DQ-F3-1` to `DQ-F3-24`, `P1`–`P7`, FR-F3-1 to FR-F3-8);
[implementation authorization record](pre_g9b_r6_plus_f3_implementation_authorization_record.md);
[F3 candidate report](pre_g9b_r6_plus_f3_candidate_report.md);
[F3-R1 root cause](pre_g9b_r6_plus_f3_r1_smoke_failure_root_cause_report.md),
[corrective report](pre_g9b_r6_plus_f3_r1_corrective_candidate_report.md) and
[reconciliation](pre_g9b_r6_plus_f3_r1_risk_and_observation_reconciliation.md);
[F3-R2 root cause](pre_g9b_r6_plus_f3_r2_apply_styles_root_cause_report.md),
[corrective report](pre_g9b_r6_plus_f3_r2_corrective_candidate_report.md) and
[reconciliation](pre_g9b_r6_plus_f3_r2_risk_and_observation_reconciliation.md). They
remain historical evidence and are not amended.

## 3. Corrective lineage

| Candidate | Outcome |
|---|---|
| F3 original `16547115` | technical PHASE/INTEGRATION/STATIC accepted; **author smoke FAIL — BLOCKING** (hidden helper windows stayed registered, Open Recent redirected, title "(1)", no normal termination, axis-dependent Locus V2 rejected); historical |
| F3-R1 `c4dffe60` | corrective successor; lifecycle and insertion failures resolved (author smoke PASS for that behaviour); Apply Styles smoke gave an apparent mismatch; historical |
| F3-R2 `2d7b2cae` | corrective successor; stale-selection scope corrected inside T0; help and guide clarified; **final approved implementation** |

The earlier candidates are not separately approved implementations; their receipts
stay bound to them and certify nothing else.

## 4. Accepted verification evidence

Re-read from the receipts and bound to the exact identities:

| Profile | Run | Verdict | Result hash |
|---|---|---|---|
| `PHASE PRE-G9B-R6-PLUS-F3` | `verification-41757b11983946cd814dd2e5d2a2d3f2` | `ACCEPTED / COMPLETE` 3/3 (`compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-f3.semantic` — 472 tests, `workspace-profile.product`); 0 diagnostics | `65a0c211b6c6e9a3f459170f7a46d50d19ea17d13eff6d23a147a96039fc270e` |
| `INTEGRATION` | `verification-50b2fb42133c42edacbdce4920ebb86f` | `ACCEPTED / COMPLETE` 19/19, 0 untrusted; standing `diagnostic.governance` finding and `diagnostic.historical-consistency` unavailable | `8ab032983b9f38a32104b91f536fc3c6ce661efc70db9252cfefccc1b31cf3d1` |
| `STATIC` | `verification-a001b313cc894dc7b8cd5c5fbcc7bd1e` | `ACCEPTED / COMPLETE` 3/3; the same two standing diagnostics | `8b4301ed011124bf752b3cc2d465ac5822586ee7116b5304e4b313366c73e6ff` |

All three name candidate `2d7b2cae23d5229f889877f22924b3669f98b755` and tree
`667308b912c73e63d9b02c9a96b0ab1beab7cd50`; receipts in
`artifacts/agent/pre-g9b-r6-plus-f3-r2-{phase,integration,static}/`. The executed
producer for `final.desktop` (evidence in
`artifacts/agent/pre-g9b-r6-plus-f3-r2-inventory/final.desktop/`) recorded 2099
tests, 0 failures, 1 skipped; INTEGRATION re-executed the Desktop suite on the exact
commit. No heavy verification is repeated for this documentary closeout.

## 5. Author smoke disposition

```text
AUTHOR_SMOKE = PASS WITH ACCEPTED CONTRACT CLARIFICATION
```

- Original F3: FAIL (§3), historical evidence; its verification results are not
  changed.
- F3-R1: the author repeated the smoke; the lifecycle and insertion failures were
  resolved; Apply Styles then showed an apparent mismatch between expected and
  observed style transfer.
- F3-R2: the investigation established that `P5` transfers construction-default
  styles, that the author's first test compared individual object styles, that both
  real documents had identical construction defaults (so several expected
  individual differences were outside the approved operation), and an independent
  stale-selection defect, corrected inside T0 with focused tests and accepted
  verification.
- Final: the author explicitly accepts the implemented construction-default
  semantics and confirms that the functionality meets the intended F3 scope.

The agent did not perform the smoke. No claim is made that the author executed
every optional checklist item or used the generated alternative-defaults document.

## 6. Final `P5` clarification

Apply Styles copies colour, line style, line thickness, point style, point size
and fill opacity from the **source document's construction defaults of each
object's default type** to the selected objects of the open drawing, or to every
object when none is selected, with the approved exclusions (native dimensions,
IsoABorder, Locus V2 family outputs). It does not transfer the individual styles
of source objects and does not match objects of the two constructions by label,
coordinates, definition or semantic identity. The earlier per-object expectation
was a misunderstanding during smoke preparation, not a product requirement and not
a defect; `F3R2-T0-CONTRACT-DISCREPANCY` is **RESOLVED BY AUTHOR CLARIFICATION —
P5 UNCHANGED**. No per-object extension is authorized.

## 7. Approved implementation scope

1. **Restricted native insertion (R, L1).** Insertion into the current document,
   all or nothing, after admission (`P4` matrix, exact class and parent-algorithm
   family) and a strict dry run; deterministic label renaming; fresh durable
   identities for Locus V2 and Spline V2 participants; no replacement or rebinding
   of existing objects; no degradation into free objects; units and `D1`
   provenance through the operation-scoped buffer, never rescaled; one Undo step;
   safe rejection. Specialized rows: Locus V2 family and Spline V2 (target allows
   Locus V2, no macros), Linear and Aligned dimensions (texts in target units),
   IsoABorder (export link not imported). General 3D and spatial import excluded.
2. **Macros (M1, MU-1).** Transitive closure transfer, digest-equal reuse,
   collision rejection, binding verification, rejection of identity + macros,
   transferred macros kept after Undo of a successful insertion.
3. **Recovery.** K1-S; K1-R-H guarded reload; history-preserving barrier; C1
   conservative asynchronous-store accounting; verified native recovery snapshot;
   UUID preservation; certification; serious-failure isolation with three exits.
4. **Apply Styles T0** as in §6; Undo/Redo, persistence and non-interference with
   geometry, labels, layers and units.
5. **F3-R1 corrections.** Hidden helper windows released; Open Recent, title and
   normal termination restored; built-in axis inputs admitted in the closure check;
   rejections name stage and cause.
6. **F3-R2 corrections.** Apply Styles ignores objects of a closed document left in
   the inherited selection; the scope is the open drawing's selection, else every
   object; help and guide state that default styles, not existing object styles, are
   applied.

Compatibility restrictions accepted: Classic keeps the inherited dialogs and
behaviour; OFF is not offered in the GeoCeDG UI; the inherited `AppD.insertFile`,
`applyTemplate` and `TemplateHelper` are not used.

## 8. Stop-condition dispositions

| Condition | Disposition | Evidence |
|---|---|---|
| FR-F3-1 accounting | SATISFIED | F3 candidate report §2, §7 (`f3a03`–`f3a09`, `f3i13`); unchanged by F3-R1/F3-R2 |
| FR-F3-2 history semantics | SATISFIED | `f3a10`, `f3a11`, `f3r01`–`f3r04`, `f3i01` |
| FR-F3-3 native snapshot | SATISFIED | `f3s01`–`f3s04`, `f3i14`, `f3r05` |
| FR-F3-4 isolation | SATISFIED for every in-process route and the three exits; real-window modality, its close button and an OS file drop not separately evidenced (accepted verification limitation, §9) | `f3r05`–`f3r07` |
| FR-F3-5 EDT | SATISFIED | `f3i12` |
| FR-F3-6 document identity | SATISFIED | `f3i01`, `f3i07`, `f3i15`, `f3s02`, `f3r03`, `f3r05`, `f3r1a1`, `f3r2m1` |
| FR-F3-7 semantic admission | SATISFIED (strengthened by the F3-R1 per-axis closure pairing) | §3 matrix; `f3r1a1`, `f3r1a2` |
| FR-F3-8 compatibility | SATISFIED | no shared, inherited or Classic change; upstream boundary in INTEGRATION |
| FR-F3R1-1 to FR-F3R1-5 | SATISFIED (lifecycle, Open Recent, Locus V2 axis admission, termination, regression); the author's F3-R1 smoke found the lifecycle failures, including termination, resolved | F3-R1 corrective report §5 |
| FR-F3R2-1, -2, -4, -5 | SATISFIED | F3-R2 corrective report §5 |
| FR-F3R2-3 complete transfer | FAILED on `c4dffe60` (RC-T0-1), SATISFIED on `2d7b2cae` | `f3r2s1`, `f3r2s2` |
| FR-F3R2-6 contract discrepancy | RESOLVED BY AUTHOR CLARIFICATION — `P5` unchanged | §6 |

Design-register risks ([register](pre_g9b_r6_plus_f3_risk_register_design_closeout.md)):
the implementation gates and product-verification obligations
(`F3R-UNDO-PENDING-ACCOUNTING`, `-UNDO-ATTACH-RACE`, `-RECOVERY-SOURCE-CAPTURE`,
`-NATIVE-RECOVERY-SNAPSHOT`, `-EDT`, `-FACTORY-LIFECYCLE`,
`-EXCEPTION-PRESERVATION`, `-SPECIALIZED-*`, `-CLIPBOARD-D1`) are **CLOSED — PRODUCT
VERIFIED AND AUTHOR APPROVED** on `2d7b2cae` through the mapped stop conditions;
`F3R-RECOVERY-FAILURE` (containment) is **CLOSED WITH ACCEPTED VERIFICATION
LIMITATION** (§9); the entries closed with an explicit scope restriction keep that
restriction; the deferred row becomes the named debts of §10.

## 9. Observations

| Item | Status | Evidence | Caused by F3 | Affects approved behaviour | Accepted restriction | Owner | Blocks closeout |
|---|---|---|---|---|---|---|---|
| `OBS-F3-PROTECTED-LABELS` | OPEN — RETAINED | spec §9.1; F3 candidate report §8 | no (inherited construction copy, exposed by insertion) | no: session-only label reservation, never serialized, cleared at New/Open/reload; K1-S keeps it | yes | G debt register | no |
| `OBS-F3-TEXT-THICKNESS` | OPEN — RETAINED | spec §9.2 | no (inherited: text line thickness not serialized) | no (affects Undo, Redo and reopen equally) | yes | G debt register | no |
| `OBS-F3R2-INHERITED-STALE-SELECTION` | OPEN — RETAINED — NOT CHARACTERIZED BEYOND T0 | F3-R2 root cause §4; reproduced in Classic | no (inherited selection manager) | no: Apply Styles corrected; other consumers (Delete, copy, properties, style bar) not assessed; no regression proven | yes | characterization candidate, not authorized; G debt register | no |
| `OBS-F3R2-PARTIAL-RESTYLE` | OPEN — RETAINED — NEVER OBSERVED | F3-R2 root cause §7 | F3 code path (theoretical) | not observed; an exception inside the restyle loop would mis-report "nothing changed" | yes | G debt register | no |
| `OBS-F3R2-SILENT-NO-OP` | RETAINED — APPROVED BEHAVIOUR | F3-R2 reconciliation §3 | — | no (approved no-op behaviour) | yes | enhancement candidate, not authorized | no |
| `OBS-F3R2-SHARED-UUID` | CLOSED — INFORMATIONAL | author documents share a UUID | no | no | — | — | no |
| isolation in a real window (spec §9.3) | ACCEPTED VERIFICATION LIMITATION | FR-F3-4 | — | contract verified in process | yes | — | no |
| `OBS-F3R1-TEST-LIFECYCLE-MASKING` | CLOSED — ADDRESSED IN TESTS | F3-R1 tests count windows; child-JVM window test | — | no | — | — | no |
| `OBS-F3R1-LAUNCHER-ROUTE` | CLOSED — AUTHOR OBSERVATION | the author's F3-R1 smoke found the termination failure resolved | — | no | — | — | no |
| F3-R1 findings (`F3-SF-HELPER-WINDOW-LEAK`, `F3-SF-LOCUSV2-AXIS-CLOSURE`, `F3-SF-GENERIC-DIAGNOSIS`) | CLOSED — CORRECTED AND AUTHOR APPROVED | F3-R1 reports | F3 (original candidate) | corrected | — | — | no |
| F3-R2 findings (`F3R2-T0-STALE-SELECTION`, `F3R2-T0-MISLEADING-HELP`) | CLOSED — CORRECTED AND AUTHOR APPROVED | F3-R2 reports | F3 (T0) / help text | corrected | — | — | no |
| `F3R2-T0-CONTRACT-DISCREPANCY` | RESOLVED BY AUTHOR CLARIFICATION | §6 | — | no | `P5` unchanged | — | no |

No existing evidence establishes an unresolved violation of the approved F3
contract or a severe data-integrity defect. The inherited stale selection is not
claimed corrected globally.

## 10. Debt disposition (C20-1)

`DEBT-R6PLUS-FILE-INSERT-SURFACE` = **CLOSED — FULFILLED BY F3** on `2d7b2cae`. The
deferred items of the design closeout (§8 there) are opened as independent named
debts, all **OPEN — NOT AUTHORIZED**, owner to be assigned by the G structured debt
register (none is implemented or closed here):

| Debt | Scope |
|---|---|
| `DEFER-F3-TEMPLATE-DEFAULTS` | the style operation also adopting construction defaults and the labeling style (T1; needs an undo design) — not a per-object transfer |
| `DEFER-F3-M2` | macro transfer beyond M1 |
| `DEFER-F3-SCRIPTS` | sources with scripts |
| `DEFER-F3-3D` | 3D objects and spatial bindings |
| `DEFER-F3-UNLISTED-TYPES` | types outside `P4` (static texts, arcs, sectors, parabolas, hyperbolas, images, buttons, input boxes, CAS cells, independent angles, others) |
| `DEFER-F3-IDENTITY-MACROS` | combined identity-bearing and macro import |
| `DEFER-F3-FORENSIC-EXPORT` | export of an uncertified state for diagnosis |

## 11. Specification promotion

[`geocedg/specs/ui/document-insertion.md`](../../geocedg/specs/ui/document-insertion.md)
moves from `PROPOSED` to `NORMATIVE / AUTHOR APPROVED`, version `1.2`, without any
new requirement. It describes the implementation verified on `2d7b2cae`: the
surface (N1, A1, X1, OFF-B, helpers), restricted insertion R with the `P4` matrix,
L1, the dry run and strict verification (axes as construction constants), M1/MU-1,
the operation-scoped buffer and `D1` (`P2`), the transaction, recovery and identity
contract (`P6`, `P7`), the native snapshot, isolation, Apply Styles T0 (`P5`,
§8.1–§8.3) and the observations. The status block, §0 and observation §9.4 (now
the recorded clarification) are updated; no requirement text changes. Q1–Q7,
`P1`–`P7` and the `DQ-D1-7` amendment (`P2`) stay as approved; the design records
and corrective reports stay frozen. No ADR is required.

## 12. Closeout verification and publication

Closeout class `DOCUMENTATION_STATUS_ONLY`: this record and its mirror, the
specification promotion, roadmap 5.35 and the mini-track plan; no product, test,
catalog, registry or prompt change. Acceptance by official `STATIC` on the
documentary successor of `2d7b2cae` (reported outside this record, which cannot
name its own commit). PHASE and INTEGRATION are not repeated.

The author authorizes publication of the verified linear chain `e70718fe` → … →
`2d7b2cae` → this closeout commit by an ordinary non-force fast-forward of `main`,
after the publication gate: fetched live remote `main`; approved candidate
unchanged; closeout accepted by `STATIC`; exact ancestry and commit set; no WIP or
unrelated commit; no divergence; fast-forward possible. No force, rebase, merge of
unrelated work, tag, release, binary or version change. Closeout acceptance and
publication are distinct facts: this record states `PASS — AUTHOR APPROVED`; the
published state is established only by the remote `main` after the push and is
recorded by the next documentary update.

## 13. Next activity and F4 handoff

```text
F4   = PLANNING INTENT RECORDED — PREPARATION NEXT — IMPLEMENTATION NOT AUTHORIZED
G    = NOT AUTHORIZED (unchanged)
```

The next separate task is `PRE-G9B-R6-plus-F4-PREP`. F4 has only planning intent
(recorded 2026-10-09): `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`, the DXF
compatibility recheck with AutoCAD, `TD-PDF-1` and `TD-PDF-2`. The preparation must
establish the current debt state of each, distinguishing the resolved AutoCAD 2026
evidence (`OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` resolved by E3-R1 for AutoCAD
2026, author confirmed) from compatibility not rechecked for other versions, must
not reopen resolved DXF defects without new evidence, and must not assume that all
four items need implementation. F4 has no approved implementation contract or
frozen verification class. Sequence: F3 closeout → controlled publication → F4
preparation and characterization → F4 author decisions → F4 design approval →
separate F4 implementation authorization. No phase advances automatically; no
further F3 product correction starts without new evidence and authorization.
