# POST-E2 — planning and characterization proposal

```text
ARTIFACT_KIND            = PLANNING PROPOSAL (documentary; no product change)
BASE                     = P_R6PLUS_E2 86fdc9086d68f21ea67f360ff104679dace74223
                           (E2 PASS — AUTHOR APPROVED — PUBLISHED, 2026-10-08)
SOURCE                   = E2 closeout record, accepted observations OBS-E2-1 to OBS-E2-4
STATE                    = PLANNING PASS — AUTHOR APPROVED (2026-10-08; scope,
                           workstream separation, order and characterization-first
                           method; hypotheses stay hypotheses and design options
                           stay candidates; author decision record
                           docs/validation/post_e2_planning_author_decision_record.md)
                           earlier: PUBLISHED — PENDING AUTHOR REVIEW (694354e8)
P1 CHARACTERIZATION      = PASS — AUTHOR APPROVED (2026-10-08; P1 closeout record
                           docs/validation/post_e2_p1_closeout_record.md; Option A
                           applied; Option B retained, not authorized; Option C not
                           adopted; P1 implementation NOT AUTHORIZED)
P1-R1 (PDF stroke width)  = PASS — AUTHOR APPROVED (2026-10-08; closeout record
                           docs/validation/post_e2_p1_r1_closeout_record.md)
P1-R2 (physical PDF width) = PASS — AUTHOR APPROVED — PUBLISHED (2026-10-09; closeout
                           record docs/validation/post_e2_p1_r2_closeout_record.md;
                           physical-pdf-style-sizes 1.0 NORMATIVE, ADR 0035 ACCEPTED;
                           OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT registered,
                           not authorized)
PDF debts                 = TD-PDF-1 text size, TD-PDF-2 embedded fonts: REGISTERED,
                           NOT AUTHORIZED; planned after P2–P4
P2 (Algebra Input zoom)   = TECHNICAL CHARACTERIZATION CANDIDATE PENDING AUTHOR
                           REVIEW (2026-10-09; docs/validation/post_e2_p2_characterization_report.md:
                           printable user-assigned factor-zoom chord fired as a menu
                           accelerator; GeoCeDG-specific since POST-G9U1-A7;
                           recommended correction A, BOUNDED_PHASE); implementation
                           and publication NOT AUTHORIZED
P3–P4 EXECUTION          = NOT AUTHORIZED
implementationAuthorized = false
selfApproved             = false
PRIORITY                 = P1 → P2 → P3 → P4 (P1 and P2 first)
```

Every workstream below is independent and starts with characterization only.
Nothing here changes the approved `E2` geometric and numerical contracts
(native-dimensions §5, §6, §8.3, §8.4; unit-system §6, §14, §20): the value stays
the model-unit measure, `Gap` and `Overshoot` stay captured once as model-space
inputs, `Offset` stays the signed model-space distance, and no command signature
changes. Each implementation needs its own author authorization.

Sources: [E2 closeout record](../validation/pre_g9b_r6_plus_e2_closeout_record.md);
[native-dimensions specification](../../geocedg/specs/dimensions/native-dimensions.md);
[unit-system specification](../../geocedg/specs/units/unit-system.md);
[E2 smoke follow-up report](../validation/pre_g9b_r6_plus_e2_smoke_followup_report.md) (part A).

## Summary

| WS | Observation | Likely owner layer | Preliminary classification | Proposed class |
|---|---|---|---|---|
| P1 | OBS-E2-1 gap/overshoot under `mm` | GeoCeDG Desktop tool capture (`GeoCeDGDimensionTools`) | probable intended behavior with a poor default for screen-scale `mm` drawings; defect not excluded | characterization `BOUNDED_PHASE`; fix, if any, `INTEGRATED_PHASE` |
| P2 | OBS-E2-3 zoom-out while typing in Algebra Input | upstream Desktop key routing, or a GeoCeDG user shortcut | probable pre-existing defect; not E2 | characterization `BOUNDED_PHASE`; fix `BOUNDED_PHASE` |
| P3 | OBS-E2-4 interactive access to document macros | GeoCeDG User tools presentation | intended policy, UX gap | design `DOCUMENTATION_STATUS_ONLY`; implementation `INTEGRATED_PHASE` |
| P4 | OBS-E2-2 dragging typed dimensions | GeoCeDG Desktop controller drag gate | intended rule (§8.4); enhancement request | design `DOCUMENTATION_STATUS_ONLY`; implementation `BOUNDED_PHASE` |

## P1 — Dimension gap/overshoot and physical units (priority)

**Observation.** With `constructionUnit = mm`, tool-created extension lines sit
far from the measured points and extend well past the dimension line; not seen
with `cm` or `m`.

**Known mechanism (code reading, to be confirmed).**
`GeoCeDGDimensionTools.captureMagnitudes`
(`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDimensionTools.java:70-81`)
returns, for a physical construction unit, `PAPER_OVERSHOOT_MM = 2` and
`PAPER_GAP_MM = 1` (lines 34, 36) converted to model units through the
construction unit and the drawing scale, and ignores the zoom. At `1:1`:

| construction unit | Overshoot (model) | Gap (model) |
|---|---|---|
| `mm` | 2 | 1 |
| `cm` | 0.2 | 0.1 |
| `m` | 0.002 | 0.001 |
| unspecified | 8 px / zoom | 4 px / zoom |

A drawing in `mm` with GeoGebra's usual coordinate magnitudes (objects of a few
units, about 50 px per unit) receives marks of 50–100 px, which matches the
report; in `cm` and `m` the same paper sizes are 10× and 1000× smaller on screen.

**Questions to settle by characterization.**
1. Is the result the specified paper size at the declared drawing scale
   (intended), a drawing-scale default that does not match the user's intent, a
   viewport fallback taken by mistake, or a wrong or repeated conversion?
2. Do picture and LaTeX exports at the drawing scale show 1 mm / 2 mm on paper
   (which would confirm the contract is met and the issue is on-screen
   perception)?
3. What does the author expect: paper-true marks, screen-true marks, or marks
   proportional to the measured length?

**Reproduction.** New document; construction unit `mm`, `cm`, `m` and
unspecified; drawing scale `1:1` and `1:10`; points `(0,0)`, `(40,0)`; Aligned
Dimension tool at three zoom levels. Record the captured `Overshoot`/`Gap`
values, on-screen pixels, and exported PDF and TikZ sizes. Compare the published
`E2` base with the original `E2` candidate (`878ff66b`) to exclude a regression
from the follow-up revision.

**Approved amendment (author decision 2026-10-08): two experiment families.**

- *Experiment A — identical numerical geometry.* `A=(0,0)`, `B=(40,0)` under
  `mm`, `cm`, `m` (and unspecified): measures the effect of changing the
  physical interpretation of fixed numerical geometry (40 mm, 40 cm, 40 m).
- *Experiment B — physically equivalent geometry.* 40 mm represented as
  `B=(40,0)` in `mm`, `B=(4,0)` in `cm` and `B=(0.04,0)` in `m`: establishes
  whether the paper-space sizes of `Gap` and `Overshoot` stay equivalent under
  unit changes. Screen appearance is compared under a viewport normalized so that
  physically equivalent objects are displayed at comparable screen sizes; raw
  pixels per model unit are never compared as if they were identical physical
  viewing conditions.

Both families keep the `1:1` and `1:10` drawing scales and three zoom levels.

**Acceptance criteria for the characterization.** A table of captured values
versus the formula for every unit/scale/zoom case; exported paper sizes within
0.01 mm of 1 mm / 2 mm at the declared scale; a classification (intended,
default-choice issue, or defect) with code evidence.

**Impacts.** Persistence: none (values are already ordinary serialized
numbers). Compatibility: existing documents keep their captured values. GUI:
possibly a preference or a different default. DAG: none.

**Minimum change, if the author wants one.** Keep the capture-once model-space
contract and change only the default source: for example a per-document or
application preference for the paper sizes, or a rule that uses the view
fallback when the physical paper size is below or above a screen threshold. Any
such rule is a new author decision on native-dimensions §8.3; existing
dimensions are never rewritten.

## P2 — Unexpected zoom during Algebra Input (priority)

**Observation.** Starting to type a command in Algebra Input sometimes zooms the
Graphics view far out; seen in earlier phases; not attributed to `E2`.

**Author observation (2026-10-08, final planning amendment).** The zoom-out
occurs precisely when the first argument of a command is introduced in Algebra
Input. This is reproducible author-observed behavior, not a confirmed root
cause. The characterization therefore centres on the transition between
command-name recognition and first-argument processing, and treats every
mechanism below — including the keyboard-routing candidates — as an unproven
hypothesis:

- record the view scale (`xscale`, `yscale`), origin (`xZero`, `yZero`) and
  visible real-world bounds (`xmin`, `xmax`, `ymin`, `ymax`) immediately before
  and after that transition, for each keystroke of the reproducing sequence;
- instrument the operations that actually change the view (at least
  `EuclidianView.setCoordSystem`, `setRealWorldCoordSystem`, `setStandardView`,
  the animated zoomers and `EuclidianSettings` coordinate-system updates) with
  stack traces, so the responsible call path is observed rather than inferred;
- examine, without assuming any of them is responsible: argument resolution and
  label lookup of the typed argument, input handling and key routing from the
  field, object highlighting or selection of the referenced object, input-help
  and autocompletion callbacks, preview-related callbacks
  (`notifyUpdatePreviewFromInputBar`, `updatePreviewFromInputBar`), and viewport
  or settings updates.

The code-reading notes below remain leads only.

**What code reading excludes.** The product input preview evaluates nothing:
`ScheduledPreviewFromInputBar.run()` clears preview objects for
`AppConfigGeoCeDG`
(`source/shared/common/src/main/java/org/geogebra/common/kernel/ScheduledPreviewFromInputBar.java:168-183`),
so no preview object, slider or temporary construction element can trigger a
view change; resizing (`EuclidianView.updateSize`/`keepCenter`) never changes
the scale; no GeoCeDG listener changes the view on construction changes.

**Candidate mechanisms, ranked (to be proven).**
1. Ctrl+Z / Ctrl+Y typed in the field are forwarded to the global key handler
   and run a construction undo/redo
   (`desktop/gui/inputfield/AutoCompleteTextFieldD.java:345-350`,
   `common/main/GlobalKeyDispatcher.java:789-800`); view changes are stored as
   undo points (`GlobalKeyDispatcher.java:417`), so an undo can restore an
   earlier, zoomed-out view. Upstream Desktop.
2. Other Ctrl combinations forwarded from the field
   (`desktop/gui/inputbar/AlgebraInputD.java:378-380`): Ctrl+- zoom-out
   (`GlobalKeyDispatcher.java:871-897`) and Ctrl+M standard view
   (`GlobalKeyDispatcher.java:737-740`). Upstream Desktop.
3. A user-assigned GeoCeDG factor-zoom shortcut (default factor 10;
   `GeoCeDGNavigationShortcutPreferences.java:31`, validation 232-242) that
   accepts Shift+letter and may fire while typing a capitalized command name.
   GeoCeDG.
4. An armed Zoom Window gesture applied by the next click in the view. GeoCeDG;
   unlikely while typing.

**Reproduction.** Windowed product with an isolated settings copy and with the
author's settings copy; type a command name, then its opening bracket and the
first argument (an existing object label, a number and a new point), keystroke
by keystroke, recording the view state above at each step; also type Shift,
Ctrl+Z, Ctrl+-, Ctrl+M in the field; instrument `EuclidianView.setCoordSystem` with a
stack trace from a scratch probe; repeat on the published bases `dc63b0e5`
and `86fdc908` and on Classic.

**Acceptance criteria.** The view state before and after the
command-name-to-first-argument transition for the author's sequence, the exact
call stack of the zoom change, the owning layer, and whether Classic behaves the same;
then a test that types the reproducing sequence and asserts the view scale is
unchanged.

**Impacts.** Persistence: none. Compatibility: none. GUI: key routing from the
input field only. DAG: none.

**Minimum change.** Depends on the cause: stop forwarding the specific keys
from Algebra Input (product-scoped, Classic unchanged), or reject text-producing
combinations in the shortcut validator. No input-preview or viewport semantics
change.

## P3 — Interactive document-macro access

**State.** `OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX` (open). Characterized in
the E2 follow-up part A: the 24 `Templatev7.ggb` macros load, register, run and
persist; the profile-compiled toolbar (ADR 0012) and the library-only User tools
menu (`GeoCeDGUserTools.populate`,
`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserTools.java:197`)
do not offer them; Document tools (local only)… lists them. Intended policy, not
a defect.

**Design questions.** A "Document tools" submenu of User tools that activates
the macro mode of each document macro; relation to installed library entries
with the same name (the existing digest rule); macros with hidden or
unsupported inputs; whether pinning is allowed; Classic unchanged.

**Acceptance criteria.** Every document macro reachable interactively, with no
toolbar reconstruction, no macro registry or command resolution change, and
the library precedence rule preserved; tests on `Templatev7.ggb` and on a
document with a library-equivalent macro.

**Impacts.** Persistence: none. GUI: User tools menu. Profile allow-list and
workspace pins if a new action is added. DAG: none.

## P4 — Typed-dimension drag behavior

**State.** Intended rule: only an independent, labelled, unlocked offset is
dragged (native-dimensions §8.4;
`GeoCeDGEuclidianController.isDraggableOffset`,
`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java:312-315`).
`s=1; AlignedDimension(A,B,s)` is draggable; `AlignedDimension(A,B,1)` is not.
Documented in the user guide §9.5.

**Options.** (a) Keep the rule; offer a "make offset draggable" action that
labels the literal offset as a free number (one undo point; the command text
then names it). (b) Accept a drag on an unlabelled literal offset by redefining
the argument value (touches redefine and the DAG; higher risk). Option (a) is
the minimum change and keeps the command signature and `Offset` semantics.

**Acceptance criteria.** For (a): literal-offset dimensions become draggable
after the action, values and geometry identical before and after, one undo
point, save/reopen stable, no change for expressions or locked numbers.

## Proposed sequence

```text
1. P1 characterization (BOUNDED_PHASE, documentary + scratch probes)
2. P2 characterization (BOUNDED_PHASE, documentary + scratch probes)
3. author decisions on P1/P2; then bounded fixes if authorized
4. P3 design, then P4 design (DOCUMENTATION_STATUS_ONLY), then author decisions
```

No step is authorized by this proposal.
