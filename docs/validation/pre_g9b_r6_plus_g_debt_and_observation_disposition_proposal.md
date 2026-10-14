# PRE-G9B-R6-plus-G — debt and observation disposition proposal (C)

| Field | Value |
|---|---|
| Status | **G DEBT DISPOSITION PLAN — REVISION 2 — FRAMEWORK AND PLANNING OWNERS ADOPTED BY THE AUTHOR — PER-ITEM DISPOSITIONS PENDING AUTHOR DECISION** (`DQ-G-6`; feeds `DQ-G-5`, `DQ-G-8`, `DQ-G-9`) |
| Part of | the [G author decision reconciliation](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md) (2026-10-14) |
| Base | published `main` `abe12893b74f574c248117fbe873633a449e23e0`, tree `ba4298c5658bf3f897455db5a49a76fea7105e72` |
| Method | read-only re-derivation of every item in [readiness package](../architecture/pre_g9b_r6_plus_g_readiness_package.md) §5. Each item was traced to: the latest author-approved closeout or decision record; the mini-track plan §14 register; the JSON mirrors; and, where relevant, the approved code at `abe12893`. A further search of 58 R6-plus, POST-E2 and POST-E3 records found the open items the readiness package missed. Only git read commands and searches were run. |
| Nature | **A proposal.** This document closes, waives, approves, implements and re-owns nothing. Categories, owners and blocking flags are proposals until the author decides them. `selfApproved = false`. |

## Revision 2 — author planning directions (2026-10-14)

Revision 1 is `ccb4ef34`. The author instruction "GeoCeDG — PRE-G9B-R6-plus-G FINAL
AUTHOR DESIGN RECONCILIATION" (2026-10-14) gives four directions. They are recorded in the
[G author design decision record](pre_g9b_r6_plus_g_author_design_decision_record.md).

1. **Inventory and framework adopted.** The inventory of 134 entries and its categorization
   framework (B, P, D, O, L, R and the three blocking columns) are adopted as the
   structure of the G register. This is **not** blanket approval of any per-item
   disposition.
2. **Planning owners decided.** The author decides the following preferred future owners.
   They are planning ownership decisions only. They authorize no implementation and
   change no phase's approved scope. They supersede the revision-1 "Proposed owner" cell
   of the rows named:

   | Row | Item | Planning owner (author decision) |
   |---|---|---|
   | D-8 | `TD-PDF-1` | **G15**, preceded by a specific physical-presentation contract-design activity |
   | P-2 | `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | **G11** |
   | P-3 | `ENH-B-MANUAL-EXPORT-AREA-DRAG` | **G15** (preferred) |
   | D-10 | POST-E2-P1 physical zoom aid | **G12** |
   | D-1 to D-7 | `DEFER-F3-*` | a future **author-named insertion and styles activity** |
   | O-1, O-2 | full-suite instability observations | **G monitoring** (G's INTEGRATION run) with **`PRE-G9B-R7` follow-up** |

   P-2 and P-3 keep category **P**. Their owner is now decided, but their disposition
   (implementation scope) remains the owner phase's own future decision.
3. **No silent promotion.** For every other pending item, an explicit author disposition
   is still required. A row whose revision-1 *proposed* category differs from its
   *recorded* status keeps its recorded status until the author decides it. This covers:
   - P → R and O → L proposals;
   - the five F4 reconciliation risks;
   - the E2 and E3 rows whose closeout is silent.

   In particular, §0.2's re-categorizations are proposals marked
   `PROPOSED — AUTHOR DISPOSITION REQUIRED`. Previously approved closures and accepted
   limitations stay as recorded.
4. **Mandatory pre-global-closeout dispositions preserved.** These two items cannot be
   closed before their G characterization evidence and an explicit author disposition:
   - B-1 `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`;
   - B-2 `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`.

The narrowest truthful status proposed for the "closeout silent" rows is given in the
[reconciliation record](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md)
§7.3 (class 2). This includes the E2 naming collision, the E3 unit gate, the `ExportArea`
link and the five F4 risks.

## How to read this proposal

- **Line numbers.** They refer to the committed `abe12893` blobs. The documentary
  candidate that carries this file adds lines to the mini-track plan:
  - one after `:100`;
  - four after `:1663` (the F4 aggregate publication);
  - five after `:1757`.

  In that candidate, plan lines shift by +1 from `:101`, by +5 from `:1664` and by +10
  from `:1758`. Revision 2 adds one more line after `:100` and five more after `:1757`:
  +2 from `:101`, +6 from `:1664`, +16 from `:1758`. The roadmap line numbers do not
  change.
- **Live remote.** It was verified by the reconciliation (`git ls-remote`, §1.1 there).
- **Category rule.** Category **L** is used only where an author-approved record accepts the
  behaviour. An item whose only record is a candidate or preparation report that says
  "none (upstream)" or "no action proposed" stays **O**.
- **Blocking columns.**
  - **Entry:** the item must be decided before G implementation starts. No item is
    blocking here, which is consistent with readiness §5.
  - **G done:** G cannot reach its closeout without substantive work, or without an
    explicit author disposition or owner on the item.
  - **Global:** the item must be dispositioned before the global `PRE-G9B-R6-plus` closeout,
    which is G's own closeout.

  Independently, `PRE-G9B-R7` accepts only if every debt item has a terminal disposition
  or an explicit retention with an owner, and it stops on an open item with no owner
  (`docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md:1263,1267`).
  Every non-R row therefore needs an owner by R7 at the latest.
- **Owners.** Proposed owners are roadmap gates whose recorded scope covers the item, or
  "author" where no gate does.

### Global-closeout mandatory dispositions (summary)

| Item | Why mandatory | Proposed route |
|---|---|---|
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (B-1) | A-2 closeout `:195` | `DQ-G-5` option A: characterize in G, then dispose |
| `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` (B-2) | **the author-approved C prompt closeout `:127-141` also records "MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT"**; the readiness package classified it P only | characterize with B-1; separate disposition |
| `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` (B-3) | mandatory G input (E1 preparation closeout §5) | implemented by G under `DQ-G-2` to `DQ-G-4` |
| items delegated by approved closeouts to "the G structured debt register" (`DEFER-F3-*`, `OBS-F3-*`, `OBS-F3R2-*`; F3 closeout `:250-252`) | the approved closeout assigns the owner choice to G | an owner per row (§3, §4) |

## Path aliases

| Alias | Path (repository-relative) |
|---|---|
| RP | `docs/architecture/pre_g9b_r6_plus_g_readiness_package.md` |
| PL | `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` |
| RM | `docs/roadmap/geocedg_roadmap.md` |
| R7D | `docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md` |
| V/ | `docs/validation/` (e.g. `V/a2-CO` = `pre_g9b_r6_plus_a2_closeout_record.md`; `-CO` = `_closeout_record.md`, `-AD` = `_author_decisions_record.md`, `-cand` = `_candidate_report.md`, `-prep` = `_preparation_characterization_report.md`, `-PCO` = `_prompt_closeout_record.md`) |
| F4-AGG | `V/pre_g9b_r6_plus_f4_aggregate_closeout_record.md` |
| F4-RDR | `V/pre_g9b_r6_plus_f4_risk_and_debt_register.md` |
| F4-REC | `V/pre_g9b_r6_plus_f4_author_decision_reconciliation.md` |
| F3-CO | `V/pre_g9b_r6_plus_f3_f3r1_f3r2_closeout_record.md` |
| PE3-REC | `V/post_e3_followups_author_reconciliation_record.md` |
| J/ | `geocedg/validation/pre-g9b-r6-plus/` |
| S/ | `source/desktop/desktop/src/main/java/org/geocedg/desktop/` |
| T/ | `source/desktop/desktop/src/test/java/org/geocedg/desktop/` |

## Legend

Identifiers: `RP R-n` is readiness finding R-n (RP:59-68). `B-n`, `P-n`, `D-n`, `O-n`, `L-n` and `R-n` are rows of this register. **P** = PENDING_AUTHOR_DISPOSITION; **D** = DEFERRED_WITH_OWNER; **O** = OPEN_NONBLOCKING; **L** = ACCEPTED_LIMITATION; **R** = RESOLVED_BY_APPROVED_IMPLEMENTATION. "R (by disposition)" marks an item that an approved record closed without code, for example NOT_A_DEFECT or D0.

Rule applied: **L** is used only where an author-approved record accepts the behaviour. An item whose only record is a candidate or preparation report saying "none (upstream)" or "no action proposed" stays **O**.

Blocking columns:
- **Entry**: must be resolved or decided before G implementation starts. RP:149 says "No debt or observation blocks **G entry**".
- **G done**: G cannot reach its closeout without substantive work or an explicit author disposition on the item. "Owner" means an author-approved record has delegated the owner assignment to the G register, so only a register entry with an owner is required.
- **Global**: must be dispositioned before the global `PRE-G9B-R6-plus` closeout, which is G's own closeout (F4-AGG:14, F4-AGG:119-122).

Independently of these columns, `PRE-G9B-R7` accepts only if "every debt item has a recorded terminal disposition or an explicit retention with an owner", and it stops on "an open debt item with no owner" (R7D:1263, R7D:1267). Every row not marked R therefore needs an owner by R7 at the latest.

Owner gates (roadmap quotes):
- G11 "jerarquía, roles, bloqueo y filtros; visibilidad por vista; estados guardables; integración con DXF, PDF y modelos heredados" (RM:3814-3817)
- G12 "zoom centrado en cursor, ZoomWindow explícito ...; vistas nombradas y zoom previo; separación modelo/vista/dibujo/impresión" (RM:3851-3854)
- G14 "adaptador desde `SpatialObject3D` ...; política de solo lectura inicial" (RM:3884-3887)
- G15 "`DrawingSheet`, marcos y cajetines; escalas físicas y viewports; PDF vectorial con tamaño exacto; SVG y PNG; pruebas dimensionales" (RM:3899-3903)
- PRE-G9B-R7 "integrated verification of the accumulated candidate; reconciliation of every debt record opened, retained or closed by the track" (R7D:1261), "preparación integrada y cierre PRE-G9B" (RM:3404)
- Plan §1.1 on ownership left with the gates: G11 keeps "hierarchy, roles, locking, filters, per-view states, named layer states"; G15 keeps "paper space, viewports, title blocks as a document service, sheet sets" (PL:154-156)

---

## 0. Summary

### 0.1 Contradictions found

1. **`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` is missing its global-closeout clause** (category change P → B).
   - The author-approved C preparation closeout records "POST-A2 LIFECYCLE DEFECT / OBSERVATION — NOT C BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT" (`V/pre_g9b_r6_plus_c_prompt_closeout_record.md:129-133`). It adds: "It must not be forgotten at the global `G` closeout" (:139-141).
   - The JSON mirror agrees (`J/pre-g9b-r6-plus-c-prompt-closeout.json:63`).
   - Against it: the plan register reads "A-2 BEHAVIOR — NOT C SCOPE — NOT FIXED — AUTHOR DISPOSITION" (PL:1774-1778). The readiness classifies it **P** only (RP:163). The readiness JSON lists only NONNATIVE and ABOUT under `blockingGlobalCloseout` (`J/pre-g9b-r6-plus-g-readiness.json:56`).
   - The C closeout says "unchanged" (`V/c-CO:193`), so it does not contradict the disposition.
   - **Prevails:** the C prompt closeout, the author-approved record that disposed of the item. The item must be dispositioned before the global closeout.

2. **E3 unit gate and ExportArea link: "OPEN … NOT RESOLVED" against the approved code.**
   - The E3 author-decision record (design time) reads "`OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | OPEN — addressed in design by the identity-only validity test (`DQ-E3-9`) — NOT RESOLVED" and "`OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | OPEN — gate designed by `DQ-E3-7` — NOT RESOLVED" (`V/e3-AD:381`, `:384`; `J/pre-g9b-r6-plus-e3-author-decisions.json:58,61`).
   - The approved E3/E3-R1 code implements both. The identity-only link is at `S/export/ExportAreaSession.java:34-37`, `:227-242` (`validateLink`) and `:254-271` (`isLive`, `==` scan of `getGeoSetConstructionOrder()`). The unit gate is at `S/GeoCeDGIsoABorderTool.java:54-80` and `S/GeoCeDGActionRegistry.java:224-235`.
   - Both are present unchanged in `7cd50116` (git show).
   - They are tested by `T/PreG9BR6PlusE3IsoABorderDesktopTest.java:103` (`unitGateBlocksCreationAndOffersDocumentUnits`) and `:118` (`explicitLinkLifecycleByIdentity`).
   - They are normative in `geocedg/specs/sheets/iso-a-border.md` §11.3 (:412-429) and §12.1 (:453-456). E3-CO:166 promoted that specification to `NORMATIVE / AUTHOR APPROVED`, "including ... `ExportArea` interaction".
   - The E3 closeout observation table (E3-CO:178-189) does not name either observation.
   - **Prevails:** the approved code and normative specification (AGENTS.md §2: code and approved specs outrank validation records). Proposed R; the label needs reconciling under DQ-G-8 and the author should confirm, because no closeout records the disposition.

3. **`OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` still shows "publication pending".**
   - PL:1812-1813 reads "CORRECTION PASS — AUTHOR APPROVED (2026-10-10, 28e9aebe; integrated as 718c4081; publication pending)".
   - Against it, the same plan reads "POST-E3-INTEGRATION = AUTHOR APPROVED — PUBLISHED (2026-10-10; main = 00b525fd" (PL:1416). `718c4081` and `00b525fd` are both ancestors of published `main`.
   - **Prevails:** the published state. Only the label drift is left (RP R-9).
   - The same drift affects the E3 headline: PL:1345-1350 says "publication NOT AUTHORIZED" (RP R-1).

4. **`OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` still shows "PENDING AUTHOR REVIEW".**
   - PL:1919-1922 reads "COEXISTENCE VALIDATED IN THE E2 TECHNICAL CANDIDATE ... — PENDING AUTHOR REVIEW".
   - The E2 closeout (`V/e2-CO:4`) approves that candidate. Its report proposes "naming side resolved by `DQ-E2-7`; coexistence validated (`K15`); technically resolved, pending author review" (`V/e2-cand:300`).
   - The E2 closeout observation block (`V/e2-CO:105-117`) does not name the item.
   - Approved code: `Commands.java:155,158` registers `AlignedDimension` and `LinearDimension`, and no `DirectDimension` or `AxisDimension` is registered. `T/PreG9BR6PlusE2NativeDimensionDesktopTest.java:163,1212-1216` covers coexistence.
   - **Prevails:** the approved implementation. The author should confirm the label because no closeout states it.
   - The residual "whole-library failure mode on a future native collision stays pre-existing" (PL:1914-1918) belongs to the USER-LIBRARY row (O-section).

5. **`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` owner.**
   - Several records still name F2: "(preferred owner F2)" (PL:1779-1780); "direct working-layer entry stays with F2" (PL:1739-1740); "stays assigned to `F2`" (`V/post_e2_p4_closeout_record.md:89-90`); `"ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY": "F2"` (`geocedg/validation/post-e2/post-e2-p4-closeout.json:39`).
   - Against them, the F2 author decision `DQ-F2-9` reads "remains a separate pending enhancement, not implemented in F2" (`V/pre_g9b_r6_plus_f2_author_decisions_record.md:216-222`). The F2/F2-R1 closeout mirror lists "working-layer direct entry" as `notApproved` (`J/pre-g9b-r6-plus-f2-f2r1-closeout.json:11`).
   - **Prevails:** the F2 author decision (later and specific). The item currently has no owner (RP R-7).

6. **`DQ-F4-6` and `DQ-F4-8` shown pending.**
   - PL:1901 reads "(DQ-F4-6 pending)"; PL:2010 reads "(DQ-F4-8 pending)".
   - Against them: the F4 design closeout records "Closed as resolved for AutoCAD 2026 only | `DQ-F4-6`" and "Deferred as `TD-PDF-1` ... | `DQ-F4-8`" (`V/pre_g9b_r6_plus_f4_author_design_closeout_record.md:40-41`). F4-AGG:7-9, :57-71 agree.
   - **Prevails:** the F4 closeouts (RP R-8).

7. **F3 Apply Template re-layering.**
   - PL:2100-2104 reads "DISPOSITION PENDING DQ-F3-9". The A-2 closeout reads "`OPEN F3 CHARACTERIZATION QUESTION`" (`V/a2-CO:199`).
   - Against them: the F3 design closeout records "`DQ-F3-9` | layers under the style operation | layers never touched" (`V/pre_g9b_r6_plus_f3_author_design_closeout_record.md:142`). F3-CO:195-197 records that "the inherited `AppD.insertFile`, `applyTemplate` and `TemplateHelper` are not used".
   - **Prevails:** the F3 closeouts. The item is moot (RP R-2).

8. **`OBS-A1-URL-OPEN` listed as "resolved".**
   - RP:221 lists it as resolved. The A-1 closeout gives it only the owner "none required" (`V/a1-CO:135`).
   - A-2 revision 2 corrected the layer-set side: "Base64 and non-native file or URL replacements commit the document hidden-layer set" (`docs/upstream/modified-files.yml:69` purpose; `V/a2-cand:473-478`).
   - The remaining session-state side (the B `ExportArea` and the D1 paste notice on API `openFile` and URL routes) was moved into `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (`V/a2-cand:731-741`), which is still open.
   - This is not a status conflict. The register should say "R for the layer part; the remainder is carried by B-1".

9. **Heap debt is listed twice with opposite categories.**
   - RP:196 lists "Desktop test-heap pressure" as O, "unchanged; not absorbed by C" (`V/c-CO:195`; `V/c_x1-CO:136-137`, both 2026-10-06).
   - RP:237 lists the same mechanism, `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION`, as R.
   - The E2 approved candidate corrected it test-only (`V/e2-cand:309`, `:385-393`: "live heap before `SvgLoadTest` 158, 162 and 158 MB ... ≈ 350 MB headroom"). The E2 closeout ran the complete `final.desktop` on the 512 MB heap (`V/e2-CO:81-82`). F3-CO:127-129 later ran `final.desktop` with 2099 tests and 0 failures.
   - **Prevails:** the later approved E2 implementation. Proposed R, with a residual monitoring note.

10. **`OBS-E2-1` is listed twice.**
    - It appears as L (RP:207, `P1-DEFAULT-UX-LIMITATION`) and as R (RP:233, "`OBS-E2-1` to `OBS-E2-4`").
    - The POST-E2-P1 closeout records "classification = P1-INTENDED-PHYSICAL-CAPTURE + P1-DEFAULT-UX-LIMITATION (accepted)". No correction was made, and Option B is not authorized (`V/post_e2_p1_closeout_record.md:45`, `:62-70`).
    - **Prevails:** the POST-E2-P1 closeout. The category is L, not R.

11. **`OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` and `OBS-A1-RECOMPUTE-OUTPUTS` are listed as "resolved".**
    - Their records read "RECORDED — `E2` unchanged; `E3` fixes its own canonical order (`DQ-E3-5`)" (`V/e3-AD:383`) and "none required" (`V/a1-CO:134`).
    - Nothing was corrected; both are documented, accepted behaviours. Proposed L. This is a categorization change, not a status conflict.

12. **Publication labels of F4 and of the readiness package.**
    - The records read "F4 AGGREGATE PUBLICATION = PENDING SEPARATE AUTHORIZATION" (F4-AGG:12) and "aggregate closeout **no**" for published `main` (RP:50). DQ-G-10 asks for this publication (RP:118).
    - Against them: the live remote `main` equals `abe12893`, a descendant of `19f8f206` (`git ls-remote`, reconciliation §1.1).
    - **Resolved:** the author resolved the publication. `DQ-G-10 = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED` (reconciliation §1.2). The remaining labels are documentary drift (reconciliation R-11; the plan and roadmap registration is applied in the candidate; closeout records stay unchanged).

Labels that drift but carry no debt (E1-P and E3 headlines, the plan's subphase table) are covered by RP R-1 (RP:59) and are not repeated here.

### 0.2 Category changes proposed against readiness §5 (each `PROPOSED — AUTHOR DISPOSITION REQUIRED`; the recorded status stands until decided)

| Item | Readiness | Proposed | Basis |
|---|---|---|---|
| `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | P | **B** | contradiction 1 |
| `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL`, `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | R "to be confirmed" | **R** (confirmed in code; label pending) | contradiction 2 |
| Desktop test-heap pressure | O | **R** (residual risk monitored) | contradiction 9 |
| `OBS-E2-1` | L and R | **L** only | contradiction 10 |
| `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER`, `OBS-A1-RECOMPUTE-OUTPUTS` | R | **L** | contradiction 11 |
| `OBS-D1-DEFAULTS-SAVE-RELEVANT` | O | **L** | consequence of the approved `DQ-D1-5`, "kept as recorded" (`V/d1-CO:149-152`; `V/d1-cand:462-465`) |
| `F4R-G2-VIEW-CLASS`, `F4R-A1-REBUILD`, `F4R-INHERITED-FREEHEP`, `F4R-CLASSIC-PERIMETER`, `F4R-READER-COMPAT` | P | **R** (author to confirm closure) | approved code and tests hold each risk (R-section) |
| `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION`, `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE` | O | **P** | records ask for G or author disposition (`V/e2-prep:531-532`) |
| `OBS-R6P-ACTION-COUNT-DOC` | P | **P**, likely already reconciled | `geocedg/specs/ui/cedg-workspaces.md:477-479` now states 133 actions |

### 0.3 Items missing from readiness §5 (added below, marked **[NEW]**)

1. Specification amendments of C and E3 that are still marked "technical candidate pending author review" inside approved specifications. E3-CO:168 says the pending C amendment "is not disposed of". **P**.
2. E3 known limitations retained by the E3 closeout: no DXF sheet label; older builds lose sheets on re-save; no title block, sheet set, paper space or viewport; `OTQ-E3-2` to `OTQ-E3-4` (E3-CO:187). **L**.
3. The host `UnicodeTeX` mapping of other characters, "unchanged; outside `E3-R1`" (E3-CO:188). **O**.
4. The standing verifier diagnostics: governance `FINDING` and historical-consistency `UNAVAILABLE` (E3-CO:189; `V/e2-CO:79-80`; F3-CO:121-122). **O**.
5. The F1 approval exclusions: routes `X1`–`X10` and Graphics View 2 (`V/f1-CO:57-62`). The readiness mentions them only in its prerequisite matrix (RP:47). **L**.
6. Native-dimensions limitations missing from RP:206: "dimension inside a user macro: bare value without unit suffix" and "`Corner` of the value text" (`geocedg/specs/dimensions/native-dimensions.md:291,293`; `V/e2-cand:310-311`). **L**.
7. POST-E2 behaviours the author accepted: macro lifetime (New keeps, Open replaces) (`V/post_e2_p3_closeout_record.md:61-64`); the AltGr physical-smoke evidence limitation (`V/post_e2_p2_r1_closeout_record.md:78`). **L**.
8. A pre-existing off-EDT construction race, recorded only in a candidate report: `PreG9BR6CapabilityMatrixTest.exportImageRows` NPE in `Component.getMaximumSize` (`V/c-cand:521-528`). This is a lower-authority record, but it bears on the RP R-6 INTEGRATION risk. **O**.

---

## 1. BLOCKING (B)

| # | Id | Source authority | Recorded status (verbatim) | Evidence | Cat | Proposed owner | Entry | G done | Global | Author decision |
|---|---|---|---|---|---|---|---|---|---|---|
| B-1 | `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | `V/a2-CO:195`; `V/c-CO:192`; `V/c_x1-CO:135`; PL:1764-1766; `J/pre-g9b-r6-plus-a2-closeout.json:69` | "OPEN — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT" | `V/a2-cand:731-741`: the export-area session and paste notice reset only in `clearConstruction`, `loadExistingFile` and `loadXML(String)`; Base64, non-native, API `openFile` and clearing `setXML` routes keep the previous state. No later record characterizes it. Partial mitigation: the E3 `ISO_A_BORDER` link is dropped on "reconstruction, reload, non-native document replacement" (`geocedg/specs/sheets/iso-a-border.md:424`). The MANUAL area, explicit producer, overlay and D1 notice are not covered. | B | PRE-G9B-R6-plus-G (focused characterization at entry), then the author: correct in G, accept as limitation, or defer with a named owner | No | Yes | Yes | DQ-G-5 (RP:113) |
| B-2 | `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | `V/pre_g9b_r6_plus_c_prompt_closeout_record.md:26-27,129-141`; `J/pre-g9b-r6-plus-c-prompt-closeout.json:61-65`; `V/c-CO:193`; `V/c_x1-CO:136`; `V/c-AD:625-626`; PL:1774-1778 | "POST-A2 LIFECYCLE DEFECT / OBSERVATION — NOT C BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT" | Never characterized. The preferred point "before or during `E1`" (C-PCO:139-140) was not used: no E1–F4 record characterizes it. F2 tests clearing `setXML` only as a slot boundary (`V/f2-cand:81`). Contradiction 1. | B | PRE-G9B-R6-plus-G (characterize with B-1; both concern A-2 document replacement) | No | Yes | Yes | New DQ, or DQ-G-5 widened: correct, accept or defer |
| B-3 | `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` | `V/pre_g9b_r6_plus_e1_prompt_closeout_record.md:78-85`; `V/e1_l-CO:111-113`; `V/e1_p-CO:127-128`; PL:2087-2097 | "AUTHOR APPROVED REQUIREMENT (2026-10-07) — OWNER PRE-G9B-R6-plus-G — MANDATORY G INPUT" | Not implemented. RP §4 and findings RP R-4 and RP R-5 (RP:62-63): no version date; no distribution profile in the build provenance. Related row O-18 (`OBS-E1P-SOURCE-REVISION`). | B | PRE-G9B-R6-plus-G | No (DQ-G-2 to DQ-G-4 must be decided before implementation, RP:150) | Yes | Yes | DQ-G-2, DQ-G-3, DQ-G-4 |

## 2. PENDING_AUTHOR_DISPOSITION (P)

| # | Id | Source authority | Recorded status (verbatim) | Evidence | Cat | Proposed owner | Entry | G done | Global | Author decision |
|---|---|---|---|---|---|---|---|---|---|---|
| P-1 | `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` | PL:1882-1889; `V/c_x1-CO:130-133`; `V/c_x1-PCO:133` (`DQ-X1-4`) | "RECORDED BY C-X1 (DQ-X1-4) — PRE-EXISTING — NOT FIXED — AUTHOR DISPOSITION" | Classic `GeoGebra.doMain` builds its frame off the EDT; 1 of 100 launches failed with an NPE (`V/c_x1-prep:480-495`). Also Print Preview, the export dialog's Save and Clipboard buttons, and `FileMenuD` worker actions. No later record. | P | Author. No roadmap gate covers Classic EDT discipline. Propose a named bounded corrective (C-X1 precedent) or L as inherited upstream behaviour. | No | No | No | Disposition and owner (DQ-G-6) |
| P-2 | `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | `V/a2-CO:197`; `V/f2-AD:216-222`; `J/pre-g9b-r6-plus-f2-f2r1-closeout.json:11`; PL:1779-1780 | "ENHANCEMENT — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED"; DQ-F2-9: "remains a separate pending enhancement, not implemented in F2" | Not implemented. Owner contradiction 5. | P | G11 (layer organization stays with G11, PL:154; RM:3814), unless the author wants a bounded slice before G9B | No | No | No | Owner or deferral (DQ-G-6); label (DQ-G-8) |
| P-3 | `ENH-B-MANUAL-EXPORT-AREA-DRAG` | `V/b-CO:112-116`; `.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md:1138` | "Not implemented in `B`; the candidate is not reopened."; C excluded it "unless separately authorized" | No later record. MANUAL is still entered by coordinates only. | P | G15 (export area and physical publication; PL:156 makes "one export-area authority" a G15 slice; RM:3900, 3907). Alternative: G12 (a `ZoomWindow`-like gesture, RM:3851). | No | No | No | Owner or deferral (DQ-G-6) |
| P-4 | `OBS-R6P-SAVE-WARNING` | `V/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md:859`; `V/pre_g9b_r6_plus_planning_candidate_report.md:334` | owner "author disposition"; "the Locus V2 save warning has no call site" | Still true at HEAD: `S/GeoCeDGExternalCompatibilityWarning.java:27` (`confirmSave`) has no production caller (git grep finds only `T/GeoCeDGProfileTest.java:108,114` using `containsNativeLocusV2`). Normative requirement: "Saving a V2-bearing file warns that an external upstream GeoGebra distribution ... may not interpret the new object" (`geocedg/specs/locus/locus-v2-public-surface.md:609-612`). Code and specification still disagree. The author may weigh the `.cedg`-only save policy. | P | Author: wire the call (bounded corrective), or amend the specification, or accept as limitation | No | No | No | Disposition. G help must not claim the warning exists. |
| P-5 | `OBS-A1-3D-VIEW` | `V/a1-CO:132`; `V/a2-cand:470-472` | owner "author"; "Hidden layers still do not apply in 3D (`OBS-A1-3D-VIEW`). Owner: author." | Never dispositioned | P | G11 ("visibilidad por vista", RM:3815); G14 as an alternative | No | No | No | Disposition and owner |
| P-6 | `OBS-A1-NON-HIT-SELECTION` | `V/a1-CO:133`; `V/a2-cand:486-487`; `J/pre-g9b-r6-plus-a2-candidate-evidence.json:151` | owner "author"; "Select All and keyboard selection still reach objects on hidden layers, now more often because sets persist. Unchanged." | Exposure grew with A-2 persistence | P | G11 ("bloqueo y filtros", RM:3814) | No | No | No | Disposition and owner |
| P-7 | `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | `V/a1-CO:130`; `J/pre-g9b-r6-plus-a2-preparation-characterization.json:175` | owner "later frontend phase"; A-2: "unaffected" | F4-A added a GV2 subclass for the style bar only (`S/GuiManagerGeoCeDG.java:113-121`); the controller is unchanged. F2 excludes GV2 (`V/f2_f2r1-CO:208`). | P | Author: a named future frontend phase (no roadmap gate covers GV2 mode parity) | No | No | No | Disposition and owner |
| P-8 | `OBS-A1-BACKGROUND-IMAGE-LAYER` | `V/a1-CO:131`; `V/b-cand:262-267` | owner "`B` / author"; B: "documented, not done" | The B closeout's accepted-observation table (`V/b-CO:104-110`) does not include it, so it was never accepted | P | G11 (layer semantics) | No | No | No | Disposition and owner |
| P-9 | `OBS-F4P-S3-SPEC-HEADER` | F4-AGG:107; F4-RDR:40; `V/f4-prep:294-296` | "REGISTERED (F4-PREP), documentary — proposed for the G structured register" | Still applies: `geocedg/specs/ui/construction-text-view-scaling.md:4` reads "`IMPLEMENTATION AUTHORIZED — CANDIDATE PENDING REVIEW`" | P | PRE-G9B-R6-plus-G (documentary) | No | No (Yes if DQ-G-9 keeps it in G) | No | DQ-G-9 |
| P-10 | `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | F4-AGG:107; F4-RDR:41 | as P-9 | Still applies: `docs/adr/0035-physical-pdf-style-sizes.md:56` reads "Point markers become `1.6 · pointSize` pt" | P | G (documentary) | No | No (as P-9) | No | DQ-G-9 |
| P-11 | `OBS-R6P-DXF-DEFAULT-DOC` | `V/planning-cand:335`; `V/p0-cand:860` | owner `G` (proposed) | Still applies: `docs/architecture/g9_extended_dxf_architecture.md:3-4` says "default-off", but `AppConfigGeoCeDGTest.java:62` asserts `DEFAULT_EXTENDED_DXF_ENABLED` is `true` | P | G (documentary) | No | No (as P-9) | No | DQ-G-9 |
| P-12 | `OBS-R6P-DXF-SPEC-HEADER` | `V/planning-cand:336`; `V/p0-cand:861` | owner `G` (proposed) | Still applies: `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:7` reads "Phase \| G9X1 design; implementation not authorized". See also P-18 (NEW-1). | P | G (documentary) | No | No (as P-9) | No | DQ-G-9 |
| P-13 | `OBS-R6P-DOC-IDENTITY-HEADER` | `V/planning-cand:338`; `V/p0-cand:863` | owner `G` (proposed) | Still applies: `geocedg/specs/ui/native-document-identity.md:7,10` reads "not authorized / not started"; `docs/adr/0016-…:6-7` likewise; `docs/adr/0029-…:3-4` reads "Proposed" | P | G (documentary) | No | No (as P-9) | No | DQ-G-9 |
| P-14 | `OBS-R6P-ACTION-COUNT-DOC` | `V/planning-cand:340`; `V/p0-cand:865` | owner `G` (proposed) | **Likely already reconciled**: `geocedg/specs/ui/cedg-workspaces.md:477-479` reads "Current live catalog (2026-10-13 ...): 133 actions. The 110-action figure in this section is the G9U1 baseline". Remaining: that line still says "technical candidate"; historical "110" at `:95,:240`. | P | G (confirm the count at the G base and close) | No | No | No | DQ-G-9 (proposed: close) |
| P-15 | `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | `V/e2-prep:536`; `V/e2-cand:307` | owner `G` (proposed); "pre-existing; unchanged; not `E2` scope" | Not re-verified in depth: the purposes at `docs/upstream/modified-files.yml:2685,2691` mention only G9U1 SplineV2 help grammar. G must check against `V/e2-prep:175` (K3). | P | G (documentary) | No | No (as P-9) | No | DQ-G-9 |
| P-16 | `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` | `V/e2-prep:531`; `V/e2-cand:307`; PL:1914-1918 | "pre-existing; a language switch can make a valid stored library unreadable"; owner "`E2` if `DQ-E2-7` changes the library policy, otherwise `G`" | `DQ-E2-7` decided naming only (PL:1914-1918: "the whole-library failure mode on a future native collision stays pre-existing"). The fallback owner is therefore G, but the readiness gives it no G scope. | P | Author: either G records it as L, or a named future user-library policy activity | No | No | No | Owner or acceptance |
| P-17 | `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE` | `V/e2-prep:532`; `V/e2-cand:307` | owner "author disposition (record only)" | Pre-existing; the `Perimeter` macro of `Templatev7.ggb` shadows the native command | P | Author (expected L) | No | No | No | Disposition |
| P-18 [NEW-1] | Undisposed C and E3 specification amendments inside approved specifications | E3-CO:168 ("the specification is not promoted and the pending `C` amendment is not disposed of") | `geometry-export-foundation.md:7-9`: "Amended by `PRE-G9B-R6-plus-C` (2026-10-04, technical candidate pending author review)"; `dxf-curve-fidelity-and-approximation.md:5`: "the version `1.1` amendment below is a `PRE-G9B-R6-plus-C` technical candidate pending author review" | Also: `geometry-export-foundation.md:91-92` and `dxf-curve-fidelity-and-approximation.md:182-183` ("`PRE-G9B-R6-plus-E3` technical candidate (not author approved"); `unit-system.md:20-21, 677-680, 837-838` (C "technical candidate pending author review"); `cedg-workspaces.md:517,525,532` (E3, F1 and F3 "technical candidate pending author review"). C, E3, F1 and F3 are all approved, but these clauses were never promoted or withdrawn. | P | G (documentary reconciliation under DQ-G-8; the promotion itself is an author act) | No | No (recommended inside G) | No | Promote or withdraw each amendment (extend DQ-G-8) |

## 3. DEFERRED_WITH_OWNER (D)

F3-CO:250-252 states that each of these debts is "**OPEN — NOT AUTHORIZED**, owner to be assigned by the G structured debt register". The mirror says the same: "owner assigned by the G structured debt register" (`J/pre-g9b-r6-plus-f3-f3r1-f3r2-closeout.json:63`). The plan register agrees (PL:1744-1747). The design-time definitions are in `V/pre_g9b_r6_plus_f3_author_design_closeout_record.md:197-205`. The approved specification excludes them: "it does not extend to ... the deferred items `DEFER-F3-*`" (`geocedg/specs/ui/document-insertion.md:6`). No roadmap gate covers document insertion, so the default proposal is a named future insertion contract phase, which the author names.

| # | Id | Source authority (definition) | Recorded status | Evidence | Cat | Proposed owner | Entry | G done | Global | Author decision |
|---|---|---|---|---|---|---|---|---|---|---|
| D-1 | `DEFER-F3-TEMPLATE-DEFAULTS` | F3-CO:256: "the style operation also adopting construction defaults and the labeling style (T1; needs an undo design) — not a per-object transfer" | "OPEN — NOT AUTHORIZED" (F3-CO:251) | Not implemented. Coupled to `F4R-DEFAULTS-UNDO` (L-5): defaults sit outside undo. | D | Named future document-insertion and styles contract phase (needs an undo design) | No | Owner | Owner | Owner (DQ-G-6) |
| D-2 | `DEFER-F3-M2` | F3-CO:257: "macro transfer beyond M1" | as D-1 | M1/MU-1 is approved (F3-CO:179-181) | D | Same future insertion phase | No | Owner | Owner | Owner |
| D-3 | `DEFER-F3-SCRIPTS` | F3-CO:258: "sources with scripts" | as D-1 | — | D | Same future insertion phase (scripting sandbox constraints apply) | No | Owner | Owner | Owner |
| D-4 | `DEFER-F3-3D` | F3-CO:259: "3D objects and spatial bindings" | as D-1 | Excluded from approved scope (F3-CO:177-178) | D | Future insertion phase after the G9 spatial closeout; G14 (RM:3884-3887) is only the read-only 3D bridge | No | Owner | Owner | Owner |
| D-5 | `DEFER-F3-UNLISTED-TYPES` | F3-CO:260: "types outside `P4` (static texts, arcs, sectors, parabolas, hyperbolas, images, buttons, input boxes, CAS cells, independent angles, others)" | as D-1 | — | D | Same future insertion phase | No | Owner | Owner | Owner |
| D-6 | `DEFER-F3-IDENTITY-MACROS` | F3-CO:261: "combined identity-bearing and macro import" | as D-1 | Rejection is approved behaviour (F3-CO:180) | D | Same future insertion phase | No | Owner | Owner | Owner |
| D-7 | `DEFER-F3-FORENSIC-EXPORT` | F3-CO:262: "export of an uncertified state for diagnosis" | as D-1 | Isolation forbids an uncertified export (F3-CO:183-184) | D | Same future insertion phase, or PRE-G9B-R7 if the author treats it as diagnostics | No | Owner | Owner | Owner |
| D-8 | `TD-PDF-1` (`OBS-POST-E2-P1-R1-PDF-TEXT-SIZE`) | F4-AGG:8-9, :67-71, :104; F4-RDR:17, :120; `V/post_e2_p1_r1_closeout_record.md:72-76` | "**OPEN — DEFERRED** (contract design required)"; owner "future contract phase; G structured register" (F4-AGG:104) | Characterized: no approved contract violated; S3 world-scaled texts and view-pixel labels (F4-RDR:17). Plan label drift (contradiction 6). | D | G15 ("PDF vectorial con tamaño exacto", RM:3901; "publicación reproducible que respeta ... tamaño físico", RM:3907), preceded by a named contract-design task | No | Owner | Owner | Owner and contract task (DQ-G-6) |
| D-9 | POST-E2-P1 Option B (configurable physical defaults) | `V/post_e2_p1_closeout_record.md:62-67` | "retained as a preferred future enhancement; not authorized" | — | D | Author-named future native-dimensions preferences enhancement (no roadmap gate covers dimensions) | No | No | No | Owner (DQ-G-6) |
| D-10 | POST-E2-P1 physical zoom aid | `V/post_e2_p1_closeout_record.md:70` | "future design possibility only; not authorized" | — | D | G12 ("separación modelo/vista/dibujo/impresión", RM:3854; "ninguna métrica depende de pantalla, zoom o DPI", RM:3860) | No | No | No | Owner (DQ-G-6) |

## 4. OPEN_NONBLOCKING (O)

| # | Id | Source authority | Recorded status (verbatim) | Evidence | Cat | Proposed owner | Entry | G done | Global | Author decision |
|---|---|---|---|---|---|---|---|---|---|---|
| O-1 | `OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS` | `V/f2_f2r1-CO:13-14,177-203`; `J/pre-g9b-r6-plus-f2-f2r1-closeout.json:46`; PL:1849-1858 | "OPEN — NON-BLOCKING FOR F2/F2-R1 — CAUSAL ATTRIBUTION NOT ESTABLISHED" | Four leftover modal "GeoGebra - Error" dialogs. Harness workaround `APPLICATION_EXCLUDE`; run 3 passed. "No test-infrastructure correction is initiated" (:202-203). Readiness risk RP R-6 (RP:64). | O | G watches it in its INTEGRATION run (RP R-6); persistent owner PRE-G9B-R7 (integrated verification, R7D:1261) | No | No | No | Characterization authorization if it recurs |
| O-2 | `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | `V/f1-CO:8-9,114,118-133`; `J/pre-g9b-r6-plus-f1-closeout.json:27`; PL:1859-1867 | "OPEN — NON-BLOCKING FOR F1 — CAUSAL ATTRIBUTION NOT ESTABLISHED" | Failed once; passed 3/3 and in a complete rerun. Evidence preserved. | O | As O-1 | No | No | No | As O-1 |
| O-3 | `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | PE3-REC:8-10, :86-88; `V/post_e3_e2_pstricks_dimension_angle_c1_closeout_record.md:96`; `V/f1-CO:116`; PL:1823-1828 | "OPEN — PRE-EXISTING UPSTREAM BEHAVIOR — USER-VISIBLE THROUGH SCRIPTING API — PRODUCT CORRECTION NOT AUTHORIZED" | English canonical names still work | O | Author: retain as inherited upstream behaviour (candidate for L) or name a future scripting/localization owner | No | No | No | Retention with owner |
| O-4 | `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | PE3-REC:14-15, :143-153; C1-CO:95; `V/f1-CO:115`; PL:1840-1843 | "OPEN — PRE-EXISTING — OUTSIDE C1 — IMPLEMENTATION NOT AUTHORIZED" | The shared formatter still writes long decimals for host rotations, e.g. a rotated ellipse | O | G15 (graphic formats, RM:3899-3903) | No | No | No | Owner |
| O-5 | `OBS-D1-ROLLBACK-CONSPROT-GUI` | `V/d1-CO:146`; `V/d1-cand:455-460`; `V/a2-cand:488-490` | "PRE-EXISTING — CHARACTERIZED — NOT CAUSED BY D1 — NOT D1 BLOCKING — IMPLEMENTATION NOT AUTHORIZED" | An A+B+C proposal exists as evidence | O | Author: a named bounded corrective; else L | No | No | No | Owner |
| O-6 | `OBS-F3-PROTECTED-LABELS` | F3-CO:230; `J/…f3-f3r1-f3r2-closeout.json:50`; `document-insertion.md:257-259`; PL:1748-1749 | "OPEN — RETAINED" ("Accepted restriction: yes"; owner "G debt register") | Inherited; session only | O (author may reclassify to L) | G register; proposed retention as an inherited limitation | No | Owner | Owner | Owner / possible L |
| O-7 | `OBS-F3-TEXT-THICKNESS` | F3-CO:231; mirror :51; `document-insertion.md:261-263` | "OPEN — RETAINED" (accepted restriction yes; owner "G debt register") | Inherited: text line thickness is not serialized | O (possible L) | G register; future serialization owner (author) | No | Owner | Owner | Owner / possible L |
| O-8 | `OBS-F3R2-PARTIAL-RESTYLE` | F3-CO:233; mirror :53 | "OPEN — RETAINED — NEVER OBSERVED" (owner "G debt register") | Theoretical F3 code path | O | Future F3 hardening activity (author) | No | Owner | Owner | Owner |
| O-9 | `OBS-F3R2-INHERITED-STALE-SELECTION` | F3-CO:232; mirror :52; PL:1750-1752; `V/f3_r2_risk_and_observation_reconciliation.md:39-42` | "OPEN — RETAINED — NOT CHARACTERIZED BEYOND T0" (owner "characterization candidate, not authorized; G debt register") | Reproduced in Classic. Other consumers (Delete, copy, properties, style bar) not assessed. | O | Named characterization activity (author) | No | Owner | Owner | Characterization authorization |
| O-10 | `OBS-F4P-FREEHEP-XREF-SIZE` | F4-AGG:106, :122; F4-RDR:39, :83, :106; `V/f4_d-CO:177` | "**OPEN — INDEPENDENT** (Ghostscript repairs 'incorrect xref size' on every FreeHEP PDF)" | Pre-existing; affects text-as-shapes and embedded PDFs alike | O | G15 (exact PDF, RM:3901) | No | No | No | Owner (proposed for the G register) |
| O-11 | AutoCAD releases other than 2026 | F4-AGG:64-65, :105; F4-RDR:121 | "**NOT RECHECKED — COVERAGE GAP**" (owner "optional author-controlled validation") | E3-R1 and F4-PREP evidence covers AutoCAD 2026 only | O | Author (optional validation) | No | No | No | None required |
| O-12 | `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED` | `V/e2-prep:535`; `V/e2-cand:307` | "pre-existing; unchanged; not `E2` scope"; owner "none (upstream; record only)" | — | O | Author (expected L) | No | No | No | Retention |
| O-13 | `OBS-R6P0-ROTATE-TEXT-POINT` | `V/p0-cand:870` | owner "none (upstream); input to `E2`" | Upstream behaviour | O | Author (expected L) | No | No | No | Retention |
| O-14 | `OBS-R6P0-CONSTRUCTION-XML-SWALLOW` | `V/p0-cand:872`; `V/d1-prep:242` | "(INFERRED)"; owner "`D1` awareness" | Not further characterized | O | PRE-G9B-R7 or author | No | No | No | Retention or characterization |
| O-15 | `OBS-R6P0-STALE-REFERENCE-FLAG` | `V/p0-cand:869`; `V/f2-prep:282` | owner "none (upstream); consumers must not rely on it" | Confirmed by F2-PREP. Approved consumers work around it (`S/export/ExportAreaSession.java:254-258`: "`isInConstructionList` answers true for replaced objects") | O | Author (expected L, with the consumer rule recorded) | No | No | No | Retention |
| O-16 | `OBS-E1P-SMOKE-READER-LOG` | `V/e1_p-cand:260-263` | "no action proposed by `E1-P`" | Pre-existing logging | O | Author | No | No | No | Retention |
| O-17 | `OBS-E1P-USERSTORE-LOCK` | `V/e1_p-cand:264-267` | "Pre-existing `G9U1`/`E1-L` behavior" | — | O | Author | No | No | No | Retention |
| O-18 | `OBS-E1P-SOURCE-REVISION` | `V/e1_p-cand:268-271` | "`source_revision` = `HEAD` without a dirty-tree flag" | Relevant to the About and provenance design (DQ-G-3, RP R-5) | O | PRE-G9B-R6-plus-G (consider within B-3) | No | No | No | Within DQ-G-3 |
| O-19 | `OBS-D1-INSTALLED-PREFERENCES-IN-TESTS` | `V/d1-CO:149-152`; `V/d1-cand:466-470` | "kept as recorded" | Test hygiene hazard | O | PRE-G9B-R7 | No | No | No | Retention |
| O-20 | `OBS-D1-INHERITED-HEADLESS-LOGGER` | `V/d1-CO:149-152`; `V/d1-cand:471-474` | "kept as recorded" | Pre-existing test-JVM hazard | O | PRE-G9B-R7 | No | No | No | Retention |
| O-21 | `OBS-D1-ANIMATING-DOCUMENT-RETENTION` | `V/d1-CO:149-152`; `V/d1-cand:478-490` | "kept as recorded" | Mitigated by the D1 helper; residual heap headroom | O | PRE-G9B-R7 | No | No | No | Retention |
| O-22 | `exportPDF` callback UTF-8 read (carried) | `V/c-CO:195`; `V/c_x1-CO:136-138`; `V/c-cand:502-504` | "unchanged; not absorbed by `C`" | Still present at HEAD: `source/desktop/desktop/src/main/java/org/geogebra/desktop/plugin/GgbAPID.java:315-320` (`Files.readString` on a binary PDF; a failure only logs and the callback is skipped) | O | G15 (PDF) or author (upstream API) | No | No | No | Owner and identifier |
| O-23 | `AutoColor` JVM-global state (carried) | `V/c-CO:195`; `V/c-cand:213-215, 505-506` | "unchanged; not absorbed by `C`" | Upstream per-JVM index; tests reset it | O | PRE-G9B-R7 (verification determinism) | No | No | No | Owner and identifier |
| O-24 | Historical G9X1 authority scan (carried) | `V/c-CO:195`; `V/c-cand:216, 507-508` | "unchanged"; "fails at the base ...; not a registry leaf" | Not acceptance-relevant | O | PRE-G9B-R7 (traceability reconciliation, R7D:1259) | No | No | No | Owner and identifier |
| O-25 [NEW-3] | Host `UnicodeTeX` mapping of other characters | E3-CO:188 | "unchanged; outside `E3-R1`" | — | O | G15 | No | No | No | Owner |
| O-26 [NEW-4] | Standing verifier diagnostics (governance `FINDING`, historical-consistency `UNAVAILABLE`) | E3-CO:189; `V/e2-CO:79-80`; F3-CO:121-122 | "pre-existing, not acceptance" | Will appear in G's INTEGRATION and STATIC runs | O | PRE-G9B-R7 | No | No | No | Retention |
| O-27 [NEW-8] | Off-EDT construction race (`PreG9BR6CapabilityMatrixTest.exportImageRows` NPE) | `V/c-cand:521-528` (candidate report only) | "not reproduced deterministically" | Same symptom class as the corrected status-bar refresh race (`V/post_e2_p1_r2_smoke_followup_report.md:205-216`) | O | G watches it in INTEGRATION (RP R-6); then PRE-G9B-R7 | No | No | No | Registration |

## 5. ACCEPTED_LIMITATION (L)

| # | Id | Source authority | Recorded status (verbatim) | Evidence | Cat | Proposed owner | Entry | G done | Global | Author decision |
|---|---|---|---|---|---|---|---|---|---|---|
| L-1 | F4-A-L1 | `V/f4_a_author_closeout_record.md:12,129`; F4-AGG:54,101; F4-RDR:102 | "**ACCEPTED LIMITATION** — editor disabled when another application-owned window overlaps the popup and Swing selects a heavy-weight popup" | Gate `owned.overlap.path` | L | F4-A contract | No | No | No | None |
| L-2 | F4-A-L2 | F4-A-CO:130; F4-RDR:103 | "**ACCEPTED LIMITATION** — one redraw when opening after a visible tooltip" | Gate `tooltip.hover.path` | L | F4-A contract | No | No | No | None |
| L-3 | F4-A-L3 | F4-A-CO:131; F4-AGG:101; F4-RDR:104 | "validated with Temurin 25.0.4 and FlatLaf 3.7; a relevant runtime or look-and-feel change requires renewed compatibility verification" | Revalidation trigger | L | F4-A contract (trigger for any runtime or LAF update, including in G packaging) | No | No | No | None |
| L-4 | `F4R-LINETYPE-COUPLING` | F4-AGG:102; F4-A-CO:142; F4-RDR:99 | "retained, accepted (`DQ-F4-4`), inherited" | — | L | — | No | No | No | None |
| L-5 | `F4R-DEFAULTS-UNDO` | F4-AGG:103; F4-A-CO:143; F4-RDR:100 | "retained, documented, inherited" | Related to D-1 | L | — | No | No | No | None |
| L-6 | F4-D retained notes (FreeHEP provenance families, inherited `FIXME`, JVM-wide Classic FreeHEP defaults, external tools) | `V/f4_d-CO:178`; `V/f4_d-cand:330-334` | "RETAINED NOTES — NON-BLOCKING" | `V/f4_d-cand:332`: "`docs/licensing/component-matrix.md` still has no FreeHEP entry (pre-existing)". This matters for B-3, whose About must state inherited component terms. | L | Licensing provenance part: G (B-3) or the author | No | No | No | Consider within B-3 |
| L-7 | `OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED` | E3-CO:8, :144-160, :186; `J/pre-g9b-r6-plus-e3-closeout.json:51-57,71`; PL:1844-1848 | "ACCEPTED NON-BLOCKING VERIFICATION LIMITATION ... independent tracking required; no owner authorized" | `asy` 3.06 cannot typeset labels in the agent environment | L | Tracking owner: G15 (graphic formats); G may retry if its environment allows | No | No | No | Tracking owner (DQ-G-6) |
| L-8 | A-2 3D draw-order limitation | `V/a2-CO:198`; PL:2098-2099 | "KNOWN PRESENTATION LIMITATION — NOT AUTHORITATIVE LAYER SEMANTICS — NOT A-2 BLOCKING" | Layers 9..99 share one 3D draw-order code (`V/a2-cand:470-472`) | L | G11 or G14 if revisited | No | No | No | None |
| L-9 | `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET` | `V/e2-CO:107`; `native-dimensions.md:290` | "DURABLE LIMITATION (unchanged)" | — | L | — | No | No | No | None |
| L-10 | `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` (DXF side) | `V/e2-cand:301` | "DXF becomes the durable limitation below" | The LaTeX side is resolved (R-section) | L | — | No | No | No | None |
| L-11 | `OBS-R6PLUS-E2-READING-FLIP-AT-VERTICAL` | `V/e2-cand:303` | "documented in the specification and guides (presentation only)" | Approved with E2 | L | — | No | No | No | None |
| L-12 | `OBS-R6PLUS-E2-VALUE-LONGER-THAN-DIMENSION-LINE` | `V/e2-cand:304`; `native-dimensions.md:292` | "arrowheads overlap on very short lines (known limitation)" | — | L | — | No | No | No | None |
| L-13 | `OBS-R6PLUS-E2-OLDER-READER-OUTPUTS-SURVIVE` | `V/e2-cand:308`; `native-dimensions.md:294`; `V/e2_smoke_followup-AD:24` | "documented"; "forward incompatibility with older readers is an accepted limitation" | — | L | — | No | No | No | None |
| L-14 | `OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS` | `V/e2-CO:105`; PL:1923-1928 | "A-INTENTIONAL-PROFILE-BEHAVIOR (classification accepted; unchanged)" | — | L | — | No | No | No | None |
| L-15 | `OBS-E2-1` / `P1-DEFAULT-UX-LIMITATION` | `V/post_e2_p1_closeout_record.md:45`; PL:1975-1978 | "classification = P1-INTENDED-PHYSICAL-CAPTURE + P1-DEFAULT-UX-LIMITATION (accepted)" | Contradiction 10. Option B is D-9. | L | — | No | No | No | None |
| L-16 | `DQ-D1-10` older-reader limitation | `V/d1-CO:151-152`; `V/d1-cand:475` | "Older readers drop the element on re-save" (kept as recorded) | — | L | — | No | No | No | None |
| L-17 | `OBS-D1-DEFAULTS-SAVE-RELEVANT` | `V/d1-CO:149-152`; `V/d1-cand:462-465` | "kept as recorded"; "by `DQ-D1-5`, a new document whose defaults set a physical unit is save-relevant" | Consequence of an author decision | L (was O) | — | No | No | No | Confirm |
| L-18 | `OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE` | `V/b-CO:108` | "accepted; not promoted to `C`" | — | L | — | No | No | No | None |
| L-19 | `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` | `V/b-CO:110`; `V/c-PCO:121` (`DQ-C14`) | "accepted"; "documented writer limitation" | — | L | (G15 if FreeHEP precision is ever revisited) | No | No | No | None |
| L-20 | `C-SMOKE-1` `EXTERNAL_READER_LIMITATION` | `V/c-CO:181` | "`EXPECTED_BY_DQ-C3 / SMOKE_EXPECTATION_CLARIFIED` (with `EXTERNAL_READER_LIMITATION` ...)" | — | L | — | No | No | No | None |
| L-21 | F2/F2-R1 platform and interaction limitations | `V/f2_f2r1-CO:205-211`; `J/…f2-f2r1-closeout.json:11` | "Accepted limitations, not claimed as supported: macOS (Command) and Linux (Ctrl) modifiers; undocked toolbars; ...; the residual risk that a future inherited single-argument `setMode(int)` caller ..." | — | L | — | No | No | No | None |
| L-22 | `OBS-F3R2-SILENT-NO-OP` | F3-CO:234; mirror :54 | "RETAINED — APPROVED BEHAVIOUR" (owner "enhancement candidate, not authorized") | — | L | Enhancement candidate (author) | No | No | No | None |
| L-23 | F3 real-window isolation (spec §9.3) | F3-CO:206, :236; `document-insertion.md:265-266` | "ACCEPTED VERIFICATION LIMITATION" | — | L | — | No | No | No | None |
| L-24 | `F3R-RECOVERY-FAILURE` | F3-CO:222-223 | "**CLOSED WITH ACCEPTED VERIFICATION LIMITATION**" | — | L | — | No | No | No | None |
| L-25 | `F3R-*` closed with explicit scope restriction (`F3R-ATOMICITY`, `-UNDO-EQUAL-CONTENT-PENDING`, `-UNDO-HISTORY-COPY-SEMANTICS`, `-UNDO-SNAPSHOT-OWNERSHIP`, `-DOCUMENT-IDENTITY`, `-SAVED-STATE`, `-REDO-PRESERVATION`, `-MACRO-M1` (MU-1), `-LABEL-L1`, `-TEMPLATE-T0`, `-INHERITED-SESSION-EFFECTS`, `-INHERITED-STORE-RACE`) | F3-CO:223-224; `V/pre_g9b_r6_plus_f3_risk_register_design_closeout.md:22-43` | "the entries closed with an explicit scope restriction keep that restriction" | — | L | — | No | No | No | None |
| L-26 | `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` | `V/e3-AD:383`; `J/pre-g9b-r6-plus-e3-author-decisions.json:60` | "RECORDED — `E2` unchanged; `E3` fixes its own canonical order (`DQ-E3-5`)" | Documented binary64 divergence; no correction | L (was R) | — | No | No | No | Confirm |
| L-27 | `OBS-A1-RECOMPUTE-OUTPUTS` | `V/a1-CO:134` | owner "none required" | No correction recorded | L (was R) | — | No | No | No | Confirm |
| L-28 [NEW-2] | E3 known limitations (no DXF sheet label; builds without `IsoABorder` lose sheets on re-save; no title block, sheet set, paper space or viewport; `OTQ-E3-2` to `OTQ-E3-4`) | E3-CO:187; `V/e3-cand:291-297` (§13); `V/e3-AD:391-393` | "retained" | Approved with E3 | L | G15 for paper space, viewports and title blocks (PL:156; RM:3899-3900) | No | No | No | None |
| L-29 [NEW-5] | F1 approval exclusions: routes `X1`–`X10` and Graphics View 2 | `V/f1-CO:57-62` | "The approval does not extend to the excluded geometric routes (`X1`–`X10`) or to Graphics View 2." | — | L | Author (future frontend; relates to P-7) | No | No | No | None |
| L-30 [NEW-6] | Native dimensions: "dimension inside a user macro" (bare value) and "`Corner` of the value text" | `native-dimensions.md:291,293`; `V/e2-cand:310-311` | "bare value without unit suffix"; "axis-aligned bounds of the rotated value" | Normative spec §11, approved with E2 | L | — | No | No | No | None |
| L-31 [NEW-7] | POST-E2-P3 macro lifetime; POST-E2-P2-R1 AltGr evidence limitation | `V/post_e2_p3_closeout_record.md:61-64`; `V/post_e2_p2_r1_closeout_record.md:78` | "The author explicitly accepts this behavior"; "a documented evidence limitation, not an open defect" | — | L | — | No | No | No | None |

## 6. RESOLVED_BY_APPROVED_IMPLEMENTATION (R)

"Closeout silent" means that the approved code or candidate demonstrates the resolution but no closeout names the item. Those rows need a DQ-G-8 label reconciliation and author confirmation. No R row blocks G entry, G completion or the global closeout, so the three columns are omitted.

| # | Id | Source authority | Recorded status (verbatim) | Evidence | Cat | Owner | Author decision |
|---|---|---|---|---|---|---|---|
| R-1 | `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | `V/e3-AD:381`; `V/e3-prep:527` (K20); RP:246 | "OPEN — addressed in design by the identity-only validity test (`DQ-E3-9`) — NOT RESOLVED" | Approved code (in `7cd50116`, unchanged to HEAD): `S/export/ExportAreaSession.java:227-242` (`validateLink` drops a stale link and sets `linkLost`); `:254-271` (`isLive`: output 0 `==` paper, parent `==` algorithm, `==` scan of `getGeoSetConstructionOrder()`); `:290-296` (`resetForDocument`). Validation runs "at every resolve and on every Graphics 1 paint" (`V/e3-cand:90-92, 158-162`). Test `T/PreG9BR6PlusE3IsoABorderDesktopTest.java:118`. Normative `iso-a-border.md:410-429` (§11.3). There is still no platform rebuild event; the design polls by identity, so "an unrelated undo also invalidates the link" (`V/e3-AD:255-257`; spec :422). The approved code addresses the need. E3-CO is silent (contradiction 2). | R (closeout silent) | — | Confirm and relabel (DQ-G-8) |
| R-2 | `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | `V/e3-AD:384`; `V/e3-prep:531`; RP:246 | "OPEN — gate designed by `DQ-E3-7` — NOT RESOLVED" | Approved code: `S/GeoCeDGIsoABorderTool.java:54-64` (`unavailableReason`: non-physical unit or overflowing `usm`) and `:72-80` (`checkUnit` offers Document Units); `S/GeoCeDGActionRegistry.java:224-235` (`isIsoAUnitGated` covers the tool mode and `export.area.iso-a`). Test `T/PreG9BR6PlusE3IsoABorderDesktopTest.java:103`. Normative `iso-a-border.md:453-456` (§12.1) and :571. Catalog text `cedg-workspaces.md:522-523`. E3-CO is silent. | R (closeout silent) | — | Confirm and relabel (DQ-G-8) |
| R-3 | `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | PE3-REC:16, :154-173; E3-CO:185, :207; PL:1803-1813 | "`POST-E3-LEGACY-INVENTORY-CASE-FIX` = `PASS — AUTHOR APPROVED`" | `tools/legacy/ingest.ps1:284` (`[hashtable]::new([StringComparer]::Ordinal)`), commit `718c4081`, an ancestor of published `main`. Plan label drift (contradiction 3). | R | — | Relabel (DQ-G-8) |
| R-4 | `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` | PL:1902-1922; `V/e2-cand:300`; `V/e2-CO:4` | plan: "COEXISTENCE VALIDATED ... — PENDING AUTHOR REVIEW" | Approved code: `Commands.java:155,158`; `T/PreG9BR6PlusE2NativeDimensionDesktopTest.java:163,1212-1216`. Residual collision mode is in P-16. Contradiction 4. | R (closeout silent) | — | Confirm and relabel |
| R-5 | Desktop test-heap pressure = `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION` | `V/c-CO:195`; `V/c_x1-CO:136-137`; `V/c-cand:509-521`; `V/e2-cand:309, 385-393` | C: "unchanged; not absorbed by `C`"; E2: "corrected test-only ... technically resolved, pending author review" | Same mechanism (≈80–90 retained hosts). E2 is approved (`V/e2-CO:4, 81-82`); F3 `final.desktop` 2099/0 (F3-CO:127-129). Residual: new host-creating tests must use the Lifecycle owner. Contradiction 9. | R (closeout silent; monitor in G INTEGRATION) | — | Confirm |
| R-6 | `F4R-G2-VIEW-CLASS` | F4-REC:214, :414 | "new, low; A1 changes the class of the Graphics 2 view object" | `S/GuiManagerGeoCeDG.java:113-121` (`Graphics2View extends EuclidianViewFor3DD`); `T/PreG9BR6PlusF4AStyleBarThicknessTest.java:225,234`. git grep finds no exact-class check against `EuclidianViewFor3DD` in source. Not realized. | R (no closeout records it) | — | Confirm closure |
| R-7 | `F4R-A1-REBUILD` | F4-REC:415 | "the field must be re-attached after every `createButtons()` ..." | `S/GeoCeDGEuclidianStyleBar.java:62-63`; `T/PreG9BR6PlusF4AStyleBarThicknessTest.java:242` (`theFieldIsAttachedAgainAfterLanguageAndFontChanges`); `V/f4_a-cand:62-64` | R (no closeout) | — | Confirm closure |
| R-8 | `F4R-INHERITED-FREEHEP` | F4-REC:416 | "the F4-D change is inherited vendored code shared with Classic; perimeter bounded by §3" | Perimeter re-checked (`V/f4_d-cand:119-129`); registered in `docs/upstream/modified-files.yml` (`V/f4_d-CO:163`); `TD-PDF-2` and `TD-PDF-3` closed (`V/f4_d-CO:172-173`) | R (no closeout) | — | Confirm closure |
| R-9 | `F4R-CLASSIC-PERIMETER` | F4-REC:417 | "F4-A (A1): none; F4-D: Classic embedded-font PDF corrected, every other Classic output unchanged" | `T/PreG9BR6PlusF4AStyleBarThicknessTest.java:265`; `T/PreG9BR6PlusF4DEmbeddedFontPdfTest.java:260`; `T/PreG9BR6PlusF4DType1OutlineTest.java:280`; F4-A-CO:144 (`F4R-CLASSIC-LEAK` CLOSED — HELD) | R (no closeout) | — | Confirm closure |
| R-10 | `F4R-READER-COMPAT` | F4-REC:277 | listed with `DQ-F4-5` risks | `V/f4_d-cand:298-300`: "Ghostscript, Poppler and Xpdf read every PDF; no reader reported an error other than the pre-existing xref repair". F4-D `AUTHOR_SMOKE = PASS` (F4-AGG:82). The xref remainder is O-10. | R (no closeout) | — | Confirm closure |
| R-11 | `OBS-R6P0-PLAN-STATUS-STALE` | `V/p0-CO:96` | "closing the observation" | Later drift is tracked separately (RP R-1, RP:59) | R | — | — |
| R-12 | `OBS-R6P-CTRL-SHIFT-D` | `V/p0-cand:864`; `V/b-cand:332` | B scope | `…ExportSurfaceTest.hiddenExportShortcutsReachOnlyTheirGeoCeDGRoutes`; `modified-files.yml:69` purpose "run only the GeoCeDG DXF action for Ctrl+Shift+D" | R (B-CO approves the candidate) | — | — |
| R-13 | `OBS-R6P0-HIDDEN-EXPORT-ROUTES` | `V/p0-cand:866`; `V/b-cand:332` | B scope | Same tests; B-CO approves the candidate | R | — | — |
| R-14 | `OBS-R6P0-CLI-EXPORT-ZERO-SIZE` | `V/p0-cand:867`; `V/b-cand:385` | B scope | B evidence | R | — | — |
| R-15 | `OBS-R6P0-LOCUSV2-SCREEN-TESSELLATION` | `V/p0-cand:871`; `V/b-cand:272, 318` | "Locus V2 is tessellated for the export resolution" | `locusV2IsTessellatedAtTheExportResolution` | R | — | — |
| R-16 | `OBS-R6P-DXF-VISIBLE-CONTRACT` | `V/c-PCO:110` (`DQ-C3`); `V/c-AD:233` | "`DQ-C3` accepted" | `V/c-CO:59` "an individually hidden object keeps group `60 = 1`" | R (by disposition and C) | — | — |
| R-17 | `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | `V/a1-cand:104`; `V/p0-cand:868` | "closes it ... for new objects" | Existing documents keep their stored layers (qualifier) | R (new objects only) | — | — |
| R-18 | `OBS-A1-URL-OPEN` | `V/a1-CO:135`; `V/a2-cand:473-478` | "none required"; A-2 revision 2 commits the set on Base64 and URL routes | The layer part is resolved by A-2. The session part (B/D1 state) is carried by B-1 (contradiction 8). | R (layer part) | — | Note the remainder in B-1 |
| R-19 | `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | `V/a2-cand:389-391`; `J/pre-g9b-r6-plus-a2-candidate-evidence.json:157` | "closed under persistent reopen" | Recorded only in the approved A-2 candidate (RP R-10) | R (closeout silent) | — | Relabel |
| R-20 | `OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT` | `V/a2-CO:177-180`; PL:1767-1770 | "AUTHOR-AUTHORIZED FOCAL CORRECTION DURING A-2 REVIEW — CLOSED" | — | R | — | — |
| R-21 | `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` | `V/c-PCO:117` (`DQ-C10`); PL:1771-1773 | "NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED" | — | R (by disposition) | — | — |
| R-22 | `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` | `V/c-PCO:116` (`DQ-C9`); `V/c-cand:499` | "resolved by `DQ-C9` within 0.01 mm" | C approved | R | — | — |
| R-23 | `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` | `V/c_x1-CO:6, 121-122`; PL:1867-1881 | "RESOLVED IN PRE-G9B-R6-plus-C-X1 — AUTHOR APPROVED" | — | R | — | — |
| R-24 | `DEBT-R6PLUS-FILE-INSERT-SURFACE` | F3-CO:18-19, :249; PL:1742-1743 | "CLOSED — FULFILLED BY F3" | — | R | — | — |
| R-25 | `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` with X1-F1, X1-F2 and X1-F3 | `V/e1_x1-CO:6, 114-117`; `V/pre_g9b_r6_plus_e1_x1_r1_launcher_comparison_report.md:41` (X1-F1 = root cause) | "RESOLVED IN PRE-G9B-R6-plus-E1-X1 — AUTHOR APPROVED"; "X1-F2 ... = RESOLVED IN E1-X1"; "X1-F3 ... = RESOLVED IN E1-X1" | — | R | — | — |
| R-26 | `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` | `V/e1_p_x1-CO:7, 78` | "RESOLVED IN PRE-G9B-R6-plus-E1-P-X1 — AUTHOR APPROVED" | — | R | — | — |
| R-27 | `OBS-E2-2` | `V/post_e2_p4_closeout_record.md:4`; PL:1944-1956 | "RESOLVED — POST-E2-P4 PASS — AUTHOR APPROVED — PUBLISHED" | — | R | — | — |
| R-28 | `OBS-E2-3` | `V/post_e2_p2_r1_closeout_record.md:4`; PL:1957-1974 | "CORRECTED — POST-E2-P2-R1 PASS — AUTHOR APPROVED — PUBLISHED" | — | R | — | — |
| R-29 | `OBS-E2-4` = `OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX` | `V/post_e2_p3_closeout_record.md:67-68`; PL:1929-1939 | "**RESOLVED** by this phase" | — | R | — | — |
| R-30 | `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM` | `V/post_e2_p1_r2_closeout_record.md:4`; PL:1979-1989 | "CORRECTED — POST-E2-P1-R2 PASS — AUTHOR APPROVED — PUBLISHED" | — | R | — | — |
| R-31 | `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` | F4-A-CO:14, :140; F4-AGG:110 | "CLOSED — FULFILLED BY F4-A (`6baa66a5`), with F4-A-L1" | — | R | — | — |
| R-32 | `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` | C1-CO:7, :94; PL:1829-1839 | "RESOLVED BY C1" | — | R | — | — |
| R-33 | `OBS-R6PLUS-E2-DEPENDENT-DRAG-MOVES-MEASURED-POINTS`; `OBS-R6PLUS-E2-LATEX-TEXT-PACKAGE`; LaTeX side of `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` | `V/e2-cand:302, 305, 301` | "resolved by the Desktop drag interception"; "avoided: plain text, no `\text`"; "LaTeX resolved for dimension outputs" | Approved with E2 | R (closeout silent) | — | — |
| R-34 | `OBS-E3-DXF-INVALID-OUTPUT`, `OBS-E3-LATEX-EXPORT` | E3-CO:126-127 | "RESOLVED BY E3-R1 — AUTHOR CONFIRMED" | — | R | — | — |
| R-35 | `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`, `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` | E3-CO:180-181 | "implemented by `E3`; approved with it"; "resolved by the approved `E3` implementation" | — | R | — | — |
| R-36 | `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY` | `V/e3-AD:382` | "DISPOSITIONED BY `DQ-E3-2`" | — | R (by disposition) | — | — |
| R-37 | `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` | PE3-REC:7, :76-82; PL:1814-1822 | "D0 — DISPOSITIONED — NOT LEGACY-LOAD SPECIFIC" | The narrower cause is O-3 | R (by disposition) | — | — |
| R-38 | `FR-2` | `V/f2_f2r1-CO:164` | "RESOLVED BY AUTHOR-APPROVED O1 REPLACEMENT" | — | R | — | — |
| R-39 | `OBS-F3R2-SHARED-UUID`; `OBS-F3R1-TEST-LIFECYCLE-MASKING`; `OBS-F3R1-LAUNCHER-ROUTE`; `F3R2-T0-CONTRACT-DISCREPANCY`; F3-R1 and F3-R2 findings | F3-CO:165-166, :235, :237-241 | "CLOSED — INFORMATIONAL" / "CLOSED — ADDRESSED IN TESTS" / "CLOSED — AUTHOR OBSERVATION" / "RESOLVED BY AUTHOR CLARIFICATION — P5 UNCHANGED" / "CLOSED — CORRECTED AND AUTHOR APPROVED" | — | R | — | — |
| R-40 | `F3R-*` product-verified (`-UNDO-PENDING-ACCOUNTING`, `-UNDO-ATTACH-RACE`, `-RECOVERY-SOURCE-CAPTURE`, `-NATIVE-RECOVERY-SNAPSHOT`, `-EDT`, `-FACTORY-LIFECYCLE`, `-EXCEPTION-PRESERVATION`, `-SPECIALIZED-*`, `-CLIPBOARD-D1`) | F3-CO:216-221 | "**CLOSED — PRODUCT VERIFIED AND AUTHOR APPROVED**" | — | R | — | — |
| R-41 | F3 Apply Template re-layering | `V/f3_author_design-CO:142`; F3-CO:195-197; PL:2100-2104; `V/a2-CO:199` | plan: "DISPOSITION PENDING DQ-F3-9"; design closeout: "layers never touched" | Moot under T0 (contradiction 7) | R (by disposition) | — | Relabel |
| R-42 | `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` (AutoCAD 2026) | E3-CO:182; F4-AGG:7, :57-65; PL:1890-1901 | "RESOLVED BY E3-R1 FOR AUTOCAD 2026"; "CLOSED — RESOLVED FOR AUTOCAD 2026" | Other versions are O-11 | R | — | Relabel DQ-F4-6 |
| R-43 | `TD-PDF-2`, `TD-PDF-3` | `V/f4_d-CO:172-173`; F4-AGG:83 | "CLOSED — RESOLVED BY F4-D" / "... SCOPE AMENDMENT 01" | — | R | — | — |
| R-44 | `F4R-POPUP-FOCUS`, `F4R-CLASSIC-LEAK` | F4-A-CO:141, :144 | "CLOSED"; "CLOSED — HELD" | — | R | — | — |
| R-45 | `F4R-EMBED-FIDELITY`, `F4R-SHAPES-UNCHANGED`, `F4R-FAILED-EXPORT-REPORTING` | `V/f4_d-CO:174-176` | "CLOSED"; "CLOSED — HELD"; "CLOSED FOR F4-D — NOT REALIZED" | The inherited generic failure reporting is unchanged and no debt was opened | R | — | — |

---

## 7. Author decisions requested (consolidated; none is decided by this proposal)

1. **DQ-G-5, widened.** Characterize and disposition B-1 and B-2 together; both are required before the global closeout. Choose a correction in G, an accepted limitation, or deferral with a named owner.
2. **DQ-G-2 to DQ-G-4** for B-3. Also consider O-18 (no dirty-tree flag) and L-6 (no FreeHEP entry in `component-matrix.md`) for the About licensing route.
3. **DQ-G-6 owners.**
   - D-1 to D-7: a named future insertion contract phase.
   - D-8 `TD-PDF-1`: G15 plus a contract-design task.
   - D-9 and D-10.
   - P-1 to P-8, P-16, P-17.
   - O-1 to O-27 (for R7, which stops on an unowned debt).
   - L-7 (tracking owner).
4. **DQ-G-8 relabels.** Contradictions 2 to 7 and 9 to 12, plus R-1 to R-5, R-18, R-19, R-41 and R-42. Confirm closure of R-6 to R-10 (the five reconciliation-level F4 risks).
5. **DQ-G-9 and P-18.** Correct, retain or close the documentary items P-9 to P-15 (P-14 looks already reconciled). Promote or withdraw the undisposed C and E3 specification amendments (P-18).
6. **DQ-G-10.** Resolved historical decision: publication completed and the exact base established (`abe12893`). No further action beyond naming the base in the G authorization.

## 8. Notes on method and limits

- The register was derived with git read commands only. The live remote state is verified separately by the reconciliation (`git ls-remote`, §1.1).
- Items absent from the readiness were looked for in every R6-plus, POST-E2 and POST-E3 closeout, decision and authorization record (58 files), and in the F4 risk register. The search grepped for identifier patterns and for OPEN, RETAINED, DEFERRED, AUTHOR DISPOSITION, NOT RECHECKED and limitation wording.
- The remaining closeout identifiers not shown as rows are covered as follows:
  - `OTQ-F1-*` and `OTQ-F2-*` were decided in the author records.
  - `F4A-R-*` are the candidate-level names of F4-A-L1 to L3 and of the approved no-shadow change.
  - `TD-G9X1-HISTORICAL-PIN` belongs to the earlier PRE-G9B-R track (R1). It is cited in R6-plus only as a precedent.
- P-15 and part of P-14 were not re-verified to the line. G must check them at its base, as DQ-G-9 (RP:117) requires.
