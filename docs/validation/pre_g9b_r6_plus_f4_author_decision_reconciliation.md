# PRE-G9B-R6-plus-F4 — author-decision reconciliation

```text
TASK                 = PRE-G9B-R6-plus-F4-AUTHOR-DECISION-RECONCILIATION
CLASS                = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
BASE                 = T_F4_PREP 089fc2205300559c572c12f0cd32326abd0c2b5e
                       tree 61fa86b01d66f129e3bc52e8e93d05959530bed6, parent 847bb20a
                       (STATIC verification-1a0c0f4e41b44767a3e40a5b3e8dc367 ACCEPTED /
                       COMPLETE)
F4 AUTHOR DECISIONS  = PREPARED — PENDING FINAL AUTHOR DISPOSITION
F4 DESIGN            = PROPOSED — PENDING AUTHOR REVIEW
F4 IMPLEMENTATION    = NOT AUTHORIZED
PRODUCT CHANGE       = NONE
selfApproved = false; implementationAuthorized = false; publicationAuthorized = false
```

Sources: [preparation and characterization report](pre_g9b_r6_plus_f4_preparation_characterization_report.md),
[author-decision preparation](pre_g9b_r6_plus_f4_author_decision_preparation_record.md)
(`DQ-F4-1` to `DQ-F4-9`), [risk and debt register](pre_g9b_r6_plus_f4_risk_and_debt_register.md),
[reconciled design candidate](../architecture/pre_g9b_r6_plus_f4_reconciled_design_candidate.md),
the combined prompt (now superseded, kept as preparation evidence) and the mirror
`pre-g9b-r6-plus-f4-preparation.json`; the POST-E2-P1-R2, POST-E2-P4 and E3 closeout
records; `physical-pdf-style-sizes.md`; ADR 0035; `verification-levels.md` §12.8; the
mini-track plan §12. Successor design:
[two-track design successor candidate](../architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md);
prompts [F4-A](../../.github/prompts/tasks/pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md)
and [F4-D](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md);
mirror [`pre-g9b-r6-plus-f4-author-decision-reconciliation.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-author-decision-reconciliation.json).

## 1. Entry gate

| Check | Result |
|---|---|
| HEAD, tree, parent | `089fc220…`, `61fa86b0…`, `847bb20a…` |
| ancestry | linear from the published F3 closeout `847bb20a` (one commit) |
| local, tracking and live remote `main` | `847bb20a593e8092eabf80bd9d7c62ed7c21de08` |
| worktree | clean |
| F4-PREP STATIC | `verification-1a0c0f4e…` `ACCEPTED / COMPLETE`, bound to `089fc220` / `61fa86b0` |
| product changes in F4-PREP | none (no path under `source/`, `apps/`, `tools/`) |
| F3 records, specifications, ADRs | unchanged since `847bb20a` |
| F4-PREP package | present and read in full, including all nine `DQ-F4` |
| result | **PASS**; branch `phase/pre-g9b-r6-plus-f4-reconciliation` at `089fc220` |

## 2. Author directions (transcribed)

The author accepts the general findings of F4-PREP and gives these preferred
directions for reconciliation (author instruction of 2026-10-13); they are not a final
approval of each `DQ-F4`:

1. include the compact style-bar numeric thickness input;
2. close the AutoCAD 2026 DXF recheck without additional product changes;
3. defer physical PDF text sizing until a new presentation contract is designed;
4. include the minimal correction of the confirmed FreeHEP embedded-font failure;
5. prefer separating the style-bar and FreeHEP corrections into independently
   verifiable implementation tracks;
6. reassess the verification class of each track, especially the inherited FreeHEP
   modification (default proposal for F4-D: `GLOBAL_IMPACT`, requiring `FINAL`);
7. avoid changes to GeoCeDG geometry, the shared kernel and previously approved
   functionality.

**Option naming.** The author's architecture labels are used from here on and are the
inverse of the preparation's: author **A1** (GeoCeDG-owned Desktop implementation, no
inherited style-bar change) corresponds to preparation option *A-2* extended to
Graphics 2; author **A2** (minimal Classic-safe inherited hook) is preparation option
*A-1*.

## 3. Source facts added by this reconciliation

Targeted reading of the published base (no new characterization):

- **F4-A seams for A1.** `EuclidianViewD.newEuclidianStyleBar()` is protected
  (`EuclidianViewD.java:883`); Graphics 1 is the GeoCeDG-owned `GeoCeDGEuclidianView`
  (`AppGeoCeDG.java:1664-1665`); Graphics 2 is created by the inherited, protected
  `GuiManager3D.newEuclidianView` (`GuiManager3D.java:258`), which the GeoCeDG-owned
  `GuiManagerGeoCeDG extends GuiManager3D` (`GuiManagerGeoCeDG.java:39`) can override.
  `EuclidianStyleBarD.createButtons()` is protected (`:508`) and runs inside the
  inherited constructor (`setLabels` → `initGUI`, `:181-216`, `:392-394`, `:1589-1593`)
  and again on font and language changes (`reinit`, `:1652-1662`); every run recreates
  `btnLineStyle`. A GeoCeDG subclass must therefore re-attach the field on every
  `createButtons()` and must not rely on its own instance fields while the inherited
  constructor runs. The popup field `myPopup` is private (`PopupMenuButtonD.java:65`),
  but a component added through the public `addPopupMenuItem` (`:277`) lives inside it,
  so a focus fallback can reach the popup through the component ancestry without an
  inherited change.
- **F4-D perimeter.** `AbstractCharTable.toName(char)` is reached only through
  `AbstractCharTable.toEncoding(char)` (`:15-31`), whose `catch (Exception)` does not
  catch the `StackOverflowError`; `toEncoding(char)` is called only by
  `FontUtilities.showString` (`FontUtilities.java:130`, `:138`), which is called only by
  `PDFGraphics2D.showCharacterCodes` (`PDFGraphics2D.java:1113-1115`), reached from
  `writeString` (`:833-850`) only when text is written as text, not as shapes. Every
  other `toName` call in FreeHEP uses the `int` or `Character` overloads. Production
  `PDFGraphics2D` instances are created only by the GeoCeDG `PictureExportService.writePDF`
  (`:177`) and the Classic `GraphicExportDialog.exportPDF` (`:1099`). No code under
  `source/shared`, `source/web`, `tools` or the Python validators references FreeHEP.
  Hence every execution that can reach the corrected line ends today in
  `StackOverflowError`; no execution that succeeds today can change.

## 4. Verification-class assessment (`verification-levels.md` §12.8)

§12.8: the class "selects the minimum acceptance coverage; it is not a hint that an
agent may increase by prudence", and it is frozen with the impact facts that select it.
Precedents (mini-track plan §12): `GLOBAL_IMPACT` for shared document serialization read
on every load (`A-2`, `D1`); `INTEGRATED_PHASE` for shared export paint paths consumed by
Classic and GeoCeDG (`B`, `C`) and for inherited Desktop hosts (POST-E2-P1-R2);
`BOUNDED_PHASE` for GeoCeDG-owned Desktop presentation (`F1`, `F2`).

| Track | Actual affected code | Classic impact | Proposed class | Required verification | Rationale |
|---|---|---|---|---|---|
| F4-A with **A1** (recommended) | new GeoCeDG style-bar subclass; `GeoCeDGEuclidianView.newEuclidianStyleBar`; a `GuiManagerGeoCeDG` Graphics 2 view factory; reuse of `GeoCeDGLineThicknessField`; EN/ES text; guide; no inherited diff | none: Classic never constructs GeoCeDG views or the GeoCeDG GUI manager | **`BOUNDED_PHASE`** | registered `PHASE` (new F4-A tests, `PostE2P1R2SmokeFollowUpTest`, workspace/profile pins); real-window focus gate; Classic containment test; author smoke | GeoCeDG-owned presentation only (`F1`/`F2` precedent); escalate if any inherited change appears |
| F4-A with A2 (alternative) | the same plus a profile-gated hook in the inherited `EuclidianStyleBarD`, shared by Classic, Graphics 2, plane and 3D bars | gated, but the inherited class changes | `INTEGRATED_PHASE` | registered `PHASE` + `INTEGRATION` | inherited host consumed by Classic (POST-E2-P1-R2 precedent) |
| F4-D | one line of vendored FreeHEP `AbstractCharTable` in the Desktop module; reachable only through PDF text written as text, from two production entry points | Classic's embedded-font PDF route is corrected identically; no other Classic output can change | **evidence-based: `INTEGRATED_PHASE`**; author default: `GLOBAL_IMPACT` | `INTEGRATED_PHASE`: registered `PHASE` (new F4-D tests and the seven existing PDF/export test classes) + `INTEGRATION` on the frozen candidate (complete Desktop and shared suites, Classic included). `GLOBAL_IMPACT`: one `FINAL` campaign | no serialization, kernel, shared-module or verifier impact; the changed executions are exactly those that fail today (§3); the concrete integration coverage is the two PDF entry points and every FreeHEP output |

The one-line size of the change is not used as an argument; the class rests on the
reachability evidence of §3. `GLOBAL_IMPACT` remains admissible as an author decision;
neither class is frozen here.

## 5. Decisions `DQ-F4-1` to `DQ-F4-9` and the additional candidate `DQ-F4-10`

Each entry: (1) original question, (2) previous recommendation, (3) author direction,
(4) alternatives, (5) evidence, (6) proposed final decision, (7) architectural
consequences, (8) verification obligations, (9) residual risks, (10) further author
judgment.

### DQ-F4-1 — F4 scope

1. *F4 scope* (preparation record): which of F4-A to F4-D F4 implements.
2. F4 implements F4-A and F4-D; F4-B closed; F4-C deferred.
3. Directions 1–5.
4. F4-A only; F4-D only; also C-1 (physical labels).
5. Characterization report §4–§7.
6. **"F4 comprises two independently authorizable implementation tracks, F4-A (compact
   style-bar thickness entry) and F4-D (minimal correction of the inherited FreeHEP
   embedded-font failure). F4-B is closed as resolved for AutoCAD 2026 without product
   change. TD-PDF-1 is deferred outside F4."**
7. Two candidates, no kernel or geometry change.
8. Per track (§4).
9. None beyond the tracks'.
10. No.

### DQ-F4-2 — host of the style-bar field

1. *Host of the style-bar field*: inherited hook, GeoCeDG subclass, or defer.
2. Preparation recommended the inherited hook (prep *A-1*, now author **A2**).
3. Prefer A1 if feasible without substantial duplication or fragile integration; A2 only
   with a clear architectural advantage.
4. A2; A1 for Graphics 1 only; defer.
5. §3 (seams, lifecycle); A2's only advantage is uniform coverage of plane and 3D style
   bars, which the GeoCeDG profile does not need; A1 needs one small subclass and two
   factory overrides, no duplication of the field or of the style-bar logic.
6. **"F4-A uses A1: a GeoCeDG subclass of the inherited Graphics style bar, installed by
   the GeoCeDG Graphics 1 view and by a GeoCeDG Graphics 2 view factory, attaches the
   existing `GeoCeDGLineThicknessField` to the line-style popup after every
   `createButtons()`; no inherited style-bar, popup or slider source changes. A2 is not
   authorized."**
7. Graphics 2 becomes a GeoCeDG-created `EuclidianViewFor3DD` subclass that only
   overrides its style-bar factory; plane and 3D bars unchanged.
8. Field re-attached after language and font changes; Graphics 1 and 2 covered;
   Classic containment.
9. `F4R-G2-VIEW-CLASS`: code testing the exact Graphics 2 view class (none known) would
   see the subclass.
10. **Yes**: confirm Graphics 2 coverage (recommended) or Graphics 1 only (`F1`/`F2`
    precedent).

### DQ-F4-3 — keyboard-focus gate and fallback

1. *Keyboard-focus fallback (`FG-F4A-1`)*: what is admissible if the non-focusable popup
   blocks typing.
2. A profile-gated focusability of that one popup.
3. Mandatory real-window gate (focus, digits and editing, commit / cancel / focus loss,
   popup usable, slider functional, no accelerator conflict, Classic unchanged); a
   synthetic event probe alone is insufficient; otherwise
   `STOP — F4-A FOCUS FEASIBILITY BLOCKED`.
4. Stop without fallback; field outside the popup.
5. Popup and button non-focusable (`PopupMenuButtonD.java:155`, `159`); popup reachable
   through the component ancestry (§3).
6. **"Before definitive F4-A implementation, a real-window gate (native input events
   through `java.awt.Robot` in the GeoCeDG window, confirmed by the author smoke)
   must show the seven gate properties. Under A1 the only admissible fallback is a
   GeoCeDG-owned focusability of the popup that contains the field, reached through the
   component ancestry while the field is attached. Otherwise:
   `STOP — F4-A FOCUS FEASIBILITY BLOCKED`, without changing the architecture."**
7. No inherited change even with the fallback.
8. Gate evidence recorded before tests are written against it.
9. `F4R-POPUP-FOCUS` (open until the gate).
10. No.

### DQ-F4-4 — behaviour of the style-bar field

1. *Behaviour of the style-bar field*.
2. Identical to the Properties field; inherited couplings kept.
3. Preserve slider, synchronization, property, range and validation, multi-selection,
   Undo/Redo, persistence; no duplicate state.
4. Thickness-only application; decimals.
5. `GeoCeDGLineThicknessField` (integers in the slider range, Enter or focus loss,
   refusal restores, one undo point); `applyLineStyle` applies the selected line type
   with the thickness; the style bar shows the first target's value.
6. **"The style-bar field behaves exactly as the approved Properties field; an accepted
   entry is equivalent to moving the style-bar slider to that value (same targets,
   including multi-selection and creation-mode defaults, and the same inherited line-type
   coupling) and stores one undo step; no new state or serialized field."**
7. None beyond the field.
8. Parity tests with the slider; Undo/Redo; save and reopen.
9. `F4R-LINETYPE-COUPLING`, `F4R-DEFAULTS-UNDO` (inherited, documented).
10. No.

### DQ-F4-5 — correction of the embedded-font route

1. *Correction of the embedded-font route*.
2. D-1, one-line inherited correction.
3. Include the minimal correction; an explicitly considered exception to the
   minimal-upstream-change policy; no FreeHEP update, replacement or refactoring.
4. D-2 (remove the choice in GeoCeDG); D-3 (defer).
5. Characterization report §7 (reproduction, upstream `c4fd03ce`, scratch feasibility);
   §3 here (perimeter).
6. **"F4-D changes only `AbstractCharTable.toName(char)` to delegate to
   `toName(Character.valueOf(c))`, restoring the delegation broken by upstream
   `c4fd03ce`; registered in `docs/upstream/modified-files.yml` with provenance; it
   applies to GeoCeDG and Classic; no other FreeHEP, exporter or dialog change."**
7. Inherited vendored code in the Desktop module.
8. The nine obligations of §6.
9. `F4R-EMBED-FIDELITY`, `F4R-READER-COMPAT`.
10. No.

### DQ-F4-6 — DXF / AutoCAD disposition

1. *DXF / AutoCAD disposition*.
2. Close for AutoCAD 2026; other versions a coverage gap.
3. Direction 2.
4. Keep open until another version is checked.
5. E3-R1 author smoke; F4-PREP re-check (eight G5 families, both routes, AUDIT clean).
6. **"`F4-B = CLOSE — RESOLVED FOR AUTOCAD 2026`. No DXF product change. AutoCAD 2024
   and other releases are not rechecked: a coverage limitation, not a demonstrated
   defect; no universal AutoCAD compatibility is claimed. Function graphs stay outside the
   export contract; no exact `SPLINE` is introduced; approximate DXF representations
   never become CeDG geometry. E3-R1 is not reopened."**
7. None.
8. None.
9. `F4B-VERSION-COVERAGE` (limitation).
10. No.

### DQ-F4-7 — structure and verification class

1. *Structure and verification class*: one F4 or two tracks; class.
2. One F4, `INTEGRATED_PHASE`.
3. Directions 5–6: two tracks; F4-D default `GLOBAL_IMPACT`.
4. One combined F4; `GLOBAL_IMPACT` or `INTEGRATED_PHASE` for F4-D; `INTEGRATED_PHASE`
   for F4-A.
5. §4.
6. **"F4-A and F4-D are separate tracks, each with its own base, scope, class, stop
   conditions, candidate, evidence, author smoke and approval gate; approval of one never
   authorizes the other. F4-A (A1): `BOUNDED_PHASE`. F4-D: `INTEGRATED_PHASE`
   (registered PHASE + INTEGRATION), on the reachability evidence of §3 — or
   `GLOBAL_IMPACT` (FINAL) if the author keeps the conservative default. Each class is
   frozen at that track's implementation authorization."**
7. Two phase selections and two candidates.
8. Per §4.
9. None.
10. **Yes**: the F4-D class.

### DQ-F4-8 — `TD-PDF-1` disposition

1. *TD-PDF-1 disposition*.
2. Reclassify as contract-design debt outside F4.
3. Direction 3; preserve the distinctions; S3 unchanged; the current output is not
   declared a desirable professional standard.
4. C-0 close with a guide explanation; C-1 in F4.
5. Characterization report §6.
6. **"`TD-PDF-1 = CHARACTERIZED — NEW PHYSICAL TEXT PRESENTATION CONTRACT REQUIRED —
   IMPLEMENTATION NOT AUTHORIZED`. Owner: a separate presentation-contract design
   activity to be named by the author (until then the G structured debt register). F4
   changes no text size, label offset or S3 rule."** Minimum design questions:
   (a) which classes get a physical size — geometric labels, independent texts, native
   dimension values; (b) the reference of the size — paper points at the drawing scale
   versus model units; (c) PDF only or screen too, and the relation to S3; (d) label
   offsets; (e) default physical sizes and the user's control; (f) consistency with SVG,
   EMF, print and LaTeX; (g) compatibility of existing documents.
7. None in F4.
8. None in F4.
9. Small text in physical PDFs of millimetre documents remains.
10. No.

### DQ-F4-9 — records

1. *Records*.
2. Reclassify TD-PDF-2; register three new observations.
3. Direction 4 and §6.4: the xref warning is a separate inherited observation.
4. Correct the two stale headers in a separate documentary task.
5. Characterization report §7, §9.
6. **"`TD-PDF-2` becomes `CONFIRMED — INHERITED PRODUCT DEFECT — OWNED BY F4-D`.
   `OBS-F4P-FREEHEP-XREF-SIZE` is registered as a separate pre-existing inherited
   observation, not attributed to and not corrected by F4-D; F4-D does not claim to
   remove other PDF-format limitations. `OBS-F4P-S3-SPEC-HEADER` and
   `OBS-F4P-ADR0035-CONSEQUENCE-LINE` are registered for the G structured register
   without editing the approved documents."**
7. None.
8. None.
9. None.
10. No.

### DQ-F4-10 (additional candidate) — sequencing, publication and aggregate closeout

The nine questions do not cover how two independent tracks are published and closed.

1. *How are two independent F4 tracks sequenced, published and aggregated?*
2. — (new).
3. Direction 5: each track has its own approval gate; publication and the relationship
   to an F4 aggregate closeout must be designed.
4. Parallel candidates from the same base, the later one published by a net-delta replay
   with a new exact-SHA approval; one aggregate approval of both.
5. Repository practice: exact-SHA non-force fast-forward publication; net-delta replay
   for sibling candidates.
6. **"The tracks are sequential: F4-D first (confirmed user-facing failure, no
   feasibility gate), then F4-A on the published closeout of F4-D. Each track closes and
   publishes through its own author closeout. After the second track, a documentary F4
   aggregate closeout records F4 complete (F4-A and F4-D approved, F4-B closed, F4-C
   deferred). Approval or publication of one track never authorizes the other."**
7. The second track's base is the first track's published closeout.
8. None beyond each track's.
9. If F4-A's gate blocks, F4 can close with F4-D only by an explicit author decision.
10. **Yes**: the order (F4-D first recommended) and whether a separate aggregate
    closeout is wanted.

## 6. F4-D verification obligations

A future F4-D candidate must establish separately: (1) the `StackOverflowError` is
eliminated; (2) a complete PDF is produced (no empty file, `%%EOF`, page and content
present); (3) the expected font program is embedded (font dictionary, `FontFile`, Type 1
for the configured `EMBED_FONTS_AS`); (4) glyphs render correctly (text extraction and
raster comparison against the text-as-shapes rendering of the same scene); (5) reader
compatibility (Ghostscript plus at least one other reader in the author smoke; a
successful Ghostscript rendering is not proof of structural validity); (6) the default
text-as-shapes PDFs byte-identical to the base; (7) Classic: text-as-shapes unchanged,
embedded-font route corrected; (8) SVG with and without embedded fonts byte-identical;
(9) no change to any other PDF output (physical stroke and marker sizes, page box,
legacy route). The xref warning is out of scope.

## 7. Risks and debts (update of the preparation register)

| Item | State proposed by this reconciliation |
|---|---|
| `F4R-POPUP-FOCUS` | open; mandatory real-window gate before definitive F4-A implementation (`DQ-F4-3`) |
| `F4R-G2-VIEW-CLASS` | new, low; A1 changes the class of the Graphics 2 view object |
| `F4R-A1-REBUILD` | new; the field must be re-attached after every `createButtons()` (language and font changes) and must not rely on subclass fields during the inherited constructor |
| `F4R-INHERITED-FREEHEP` | the F4-D change is inherited vendored code shared with Classic; perimeter bounded by §3 |
| `F4R-CLASSIC-PERIMETER` | F4-A (A1): none; F4-D: Classic embedded-font PDF corrected, every other Classic output unchanged |
| `OBS-F4P-FREEHEP-XREF-SIZE` | registered, separate, inherited; not in F4-D |
| F4-B | `CLOSE — RESOLVED FOR AUTOCAD 2026`; other versions a coverage limitation |
| `TD-PDF-1` | `CHARACTERIZED — NEW PHYSICAL TEXT PRESENTATION CONTRACT REQUIRED — IMPLEMENTATION NOT AUTHORIZED` |
| `TD-PDF-2` | `CONFIRMED — INHERITED PRODUCT DEFECT — OWNED BY F4-D` |

All other entries of the preparation register are unchanged. F3 debts are not moved.

## 8. Prompts

The combined prompt `pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md` is
kept as preparation evidence and marked superseded. Its successors are the F4-A and F4-D
prompts, both `PREPARED — NOT AUTHORIZED` and validated against the task prompt
contract.
