# PRE-G9B-R6-plus-F1 — orientation cue

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F1-PREP` (author instruction of
2026-10-11) and reconciled by `PRE-G9B-R6-plus-F1-PREP-CLOSEOUT` with the
author decisions of 2026-10-11 on `DQ-F1-1` to `DQ-F1-9`
([F1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f1_author_decisions_record.md)
v1.0). The author approved the
[reconciled design](../../../docs/architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`). **The author did not authorize implementation
or publication.** The existence of this file is not authorization, and nothing
in it may be read as permission to start coding.

Execution requires a new, separate, explicit author instruction that names
`PRE-G9B-R6-plus-F1` and an exact published base, given after the author's
review of the documentary package. That instruction may authorize, as the
first tracked edit of the phase, an amendment of this prompt to the authorized
state, following the `E1-L`, `E1-X1`, `E2` and `E3` precedent. This file is an
execution contract, not a second policy document: the state classes are stated
once in `geocedg/specs/units/unit-system.md` §17.2; the verification classes
once in `geocedg/specs/operations/verification-levels.md` §12.8; the F1
semantics once in the reconciled design, whose section numbers are cited
below; the probe evidence once in the
[characterization report](../../../docs/validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md)
(frozen; corrections `ER-1`, `ER-2` in the decision record §4). The first
deliverable of F1 turns the approved design into the durable specification
named under *Required design/specification*.

```text
PRE-G9B-R6-plus-F1 =
DESIGN — AUTHOR APPROVED (2026-10-11)
CANONICAL PROMPT — PREPARED — NOT AUTHORIZED

CHARACTERIZATION         = COMPLETE (2026-10-11, on P_C1 bde2827b)
AUTHOR DECISIONS         = DQ-F1-1 to DQ-F1-9 RECORDED (2026-10-11, record v1.0)
IMPLEMENTATION           = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
authorApproved (design)  = true    (the reconciled design only; no technical candidate exists)
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP PRESENTATION-ONLY OVERLAY IN GRAPHICS VIEW 1 +
                           GLOBAL USER PREFERENCE + ONE MENU ACTION AND ONE
                           SYNCHRONIZED LAYOUT & PRESENTATION CONTROL
                           (catalog 130 -> 131); no kernel, serialization,
                           export, shared-drawable or GV2 change
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED (hard)
                           PRE-G9B-R6-plus-E3 = PASS — AUTHOR APPROVED
                                                (recommended predecessor, no coupling)
                           POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1
                                              = PASS — AUTHOR APPROVED — PUBLISHED
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
VERIFICATION CLASS       = BOUNDED_PHASE (author-accepted, DQ-F1-8), conditioned on
                           the entry gate confirming the approved scope; frozen at
                           authorization
PRECONDITIONS            = separate explicit implementation authorization on an
                           exact published base
OPEN TECHNICAL ITEMS     = OTQ-F1-1 to OTQ-F1-4 (record §3), none blocking
NEXT_SUBPHASE            = none implied; F2, F3, F4, G stay unauthorized
```

<!-- geocedg-field: objective -->
## Objective

Implement, once authorized, the approved orientation cue:

1. A GeoCeDG-owned, read-only **cue model** that, for a geo in Graphics View 1,
   applies the eligibility matrix v1 (design §3.3, `A1`–`A17` admitted,
   `X1`–`X10` excluded) and the predicate of design §4, and returns the world
   sense (`GeoLine.getDirection`), the screen anchor and angle (design §5). It
   never mutates kernel, view or document state.
2. A **Graphics View 1 overlay** in `GeoCeDGEuclidianView.paint`, after
   `super.paint`, drawing the discreet hollow 10 × 4 device-pixel triangle in
   the object's colour (design §6, §7) through a small GeoCeDG-owned path
   builder; screen-only.
3. The **global preference** `geocedg.view.orientation-cue.v1`, default OFF
   (design §8), with **one declarative menu action** (catalog 130 → 131) and
   **one synchronized control** in Layout & Presentation.
4. The tests of design §10 and the durable specification of *Required
   design/specification*.

### Author decisions this prompt implements (record v1.0 §1)

| Id | Decision |
|---|---|
| `DQ-F1-1` | one global application presentation option; no per-object attribute |
| `DQ-F1-2` | Graphics View 1 only; GV2 outside the design |
| `DQ-F1-3` | screen-only; never in print, clipboard, image, LaTeX, DXF or any export |
| `DQ-F1-4` | discreet hollow triangle, nominal 10 × 4 device pixels, object colour; presentation-only geometry and placement |
| `DQ-F1-5` | positive eligibility matrix of constructive or demonstrably inherited orientations; authority `getDirection`, not `getDirectionInD3`; no constructive sense from a directed parametrization of equation coefficients; excluded routes documented, no cue; nothing guessed |
| `DQ-F1-6` | `geocedg.view.orientation-cue.v1`, default OFF, survives restart, not serialized, New/Open leave it unchanged |
| `DQ-F1-7` | suppression of undefined, non-finite, degenerate, effectively hidden, insufficiently visible and redundantly decorated objects; inversions faithful to the kernel |
| `DQ-F1-8` | presentation-only GV1 overlay; no new `GeoElement`, kernel algorithm, persistence attribute, dependency or shared drawable change; `BOUNDED_PHASE` conditioned on the entry gate |
| `DQ-F1-9` | one declarative menu action and a synchronized Layout & Presentation control; catalog 130 → 131 without implying `INTEGRATED_PHASE`; stop on unexpected inherited-class changes |

<!-- geocedg-field: implementation_base -->
## Implementation base

To be named by the author's implementation authorization: an exact published
`main` that contains the F1 documentary package (preparation candidate
`732292e6cb9bf3a0f29367e43eb39bdc6b4ad22b`, tree
`0839dd764ffceefc2fb009155b25f6495becaae1`, and its closeout child), on top of

```text
P_C1                  = bde2827bb21e4a28e080db6a65b4abc892071cf8
tree                  = 1b74102cea5151b8a6c571cb36b96b33af6fb3c8
                        (published C1 closeout, docs/validation/post_e3_e2_pstricks_dimension_angle_c1_closeout_record.md §6)
```

**Entry gate.** Before any tracked change the agent verifies that local
`main`, `origin/main` and the live remote `main` resolve to the authorized base
and that the worktree is clean. It then confirms the approved scope for
`BOUNDED_PHASE` (`DQ-F1-8`): every planned change lies in GeoCeDG-owned
Desktop code, `apps/geocedg/application-profile.yml`, GeoCeDG tests, the
profile pins and allow-list of report `K14`, the phase registration and
documentation; the Layout & Presentation control fits an existing
GeoCeDG-owned panel (`OTQ-F1-4`); the macro resolution of `OTQ-F1-1` is
decided (admit or exclude). Any other need stops the work and is reported.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` §17.2 and `verification-levels.md`; the F1 author-decision
record v1.0; this prompt; the reconciled design; then the characterization
report, its JSON mirror and the decision preparation record; then the `P0` F1
candidate and older evidence. Probe outputs, reports and screenshots are
evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones; the implementation re-establishes each at its
base.

**Desktop (`source/desktop/desktop`), GeoCeDG-owned code only.**

- Cue model and path builder (new classes under `org.geocedg.desktop`),
  reading only: `GeoLine.getDirection`, coefficients, endpoints, the classes of
  the geo, its parent algorithm and its orienting inputs (public API),
  drawable presence, `isDefined`, `isEuclidianVisible`, `isVisibleInView`,
  `App.isLayerShown`, line thickness, decoration, GV1 transform and size.
- The overlay call in `GeoCeDGEuclidianView.paint`, after `super.paint`; the
  existing export-area and intersection overlays unchanged.
- The preference class (key, values, default OFF, test seam) following
  `GeoCeDGUnitPreferences`; its single owner in `AppGeoCeDG`; repaint of every
  GeoCeDG window on change.
- The menu action `presentation.orientation-cue` (design §8): its
  `application-profile.yml` entry and placement in `view-visibility-style`,
  `GeoCeDGActionRegistry` target, `execute` and `checked` cases, localized
  texts (EN, ES).
- One row in an existing GeoCeDG-owned Layout & Presentation panel
  (`GeoCeDGThemeOptionsPanel` or `GeoCeDGPresentationOptionsPanel`),
  synchronized with the menu checkbox.
- Desktop tests for design §10.

**Documentation and verification.**

- The durable specification of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The catalog count in `GeoCeDGProfile`, the eleven test assertions of report
  `K14`, the approved ids in `tools/agent/workspace-profile-validation.ps1` and
  its static-contract pin with the `static_contracts` registry pin.
- The phase registration `PRE-G9B-R6-PLUS-F1` (selection, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, through the official mechanism.
- The EN and ES user guides (the option) and their outline pins, if touched.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, algorithms, `GeoLine`, drawables,
  `EuclidianView`, exporters).
- A new `GeoElement`, command, alias, syntax, algorithm, dependency, output or
  construction element; any change of construction order, labels, styles,
  layers, visibility flags, selection or hit testing.
- Any document, macro or preferences XML change; a per-object attribute; an
  undo point for the option.
- Graphics View 2 or any other view; a GeoCeDG GV2 class.
- Any appearance of the cue in print, clipboard, image, LaTeX, DXF, CLI or any
  other export.
- Choosing or classifying the sense from `getDirectionInD3`, `DrawLine`'s
  screen point order, labels, names, captions, definition text, coordinates,
  construction indices or visual proximity; admitting an `X1`–`X10` route;
  smoothing, memorizing or reassigning a kernel inversion.
- Modifications of `AppD`, `OptionsLayoutD` or any other inherited class; a new
  Layout & Presentation panel.
- Changes to Classic or upstream application behavior.
- `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop application/view layer only (`AGENTS.md` §4: GUI presentation
consuming existing kernel semantics read-only; no dependency, serialization or
identity effect). The kernel remains the single authority of the sense; the
cue is `DERIVED_TRANSIENT` presentation; the option is `USER_PREFERENCE`.
Export adapters: unchanged. Python: none.

## Required design/specification

First deliverable of the phase, before product code:
`geocedg/specs/presentation/orientation-cue.md` (proposed path) — the durable
contract (authority, kernel versus intrinsic sense, eligibility matrix v1,
predicate and degenerations, placement and rendering, option and UI,
GV1-only, screen-only, invariants), from the reconciled design and the
decision record, status `PROPOSED` until the author approves it. No ADR is
required (no seam change beyond the approved design).

## Geometric invariants and degeneracies

- Design §2 invariants.
- The drawn sense is `getDirection` for every drawn cue, for every transform,
  zoom, pan, size and axis ratio; placement never reverses it.
- No cue in any row of the design §4 degeneration table; no exception there.
- Kernel inversions are shown as they occur, with no smoothing.
- Determinism: anchor and angle are pure functions of the current object
  state, the GV1 transform and size, and the arrow size.

## Compatibility and serialization

- Documents, macros and preferences XML byte-identical with the option on and
  off; documents saved by F1 open unchanged in older GeoCeDG and Classic.
- The preference key is ignored by older GeoCeDG and by Classic.
- Every export byte-identical with the option on and off.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations are design §10: `T-F1-SENSE`, `T-F1-REVERSAL`,
`T-F1-ELIGIBILITY`, `T-F1-DYNAMIC`, `T-F1-DEGENERATE`, `T-F1-VISIBILITY`,
`T-F1-PLACEMENT`, `T-F1-RENDERING`, `T-F1-VIEWS`, `T-F1-EXPORT`,
`T-F1-NO-MUTATION`, `T-F1-HIT`, `T-F1-PREFERENCE`, `T-F1-UI-SYNC`,
`T-F1-PROFILE`, and the author smoke `T-SMOKE` (not performed by the agent).
Sense assertions use the read-only cue model and `getDirection`, never pixel
inspection alone.

Commands (PowerShell; log directories fresh, under `artifacts\agent\`):

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F1 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F1 -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-F1` covers geometric admissibility,
direction, rendering, lifecycle, visibility, preferences and product-profile
regressions (`DQ-F1-8`): at least `compile.shared.semantic`,
`compile.desktop.semantic`, `workspace-profile.product` and
`junit.desktop.pre-g9b-r6-plus-f1.semantic`, the latter selecting the F1
classes and the profile classes whose pins change (report `K14`). `PHASE` runs
once on the frozen clean commit and tree. No `INTEGRATION` or `FINAL`. A run
above the frozen class fails with `VERIFICATION_ESCALATION_REQUEST` and waits
for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
F1 on an exact published base; it then authorizes local implementation on a
local branch, local commits, focused tests, the phase registration and its
official catalog updates, development `INFRA_UNIT` and `STATIC` runs, the
frozen acceptance run, and one frozen technical candidate for author review and
author smoke. It never authorizes self-approval, author smoke by the agent, a
run above the frozen class, or a change of the class; a proven inability of the
frozen class to cover the change is reported as a reclassification proposal,
never applied silently.

`F2`, `F3`, `F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does
every open observation not named here. Author approval is never created by
technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA. Acceptance evidence
never grants publication authority.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-F1      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (BOUNDED_PHASE, one PHASE)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification; the Desktop, profile, registry and guide deltas with their
  tests.
- A candidate report with the entry gate, `K3`–`K14` re-established with every
  correction, the dispositions of `OTQ-F1-1` to `OTQ-F1-4`, test evidence, the
  exact commands, exit codes, report paths and verdicts, and an author-smoke
  checklist: with the option OFF nothing changes; switch it ON from the View
  menu and check that the Layout & Presentation row follows (and the reverse);
  check `Line(A,B)` versus `Line(B,A)`, `Segment`, `Ray(A,B)`, `Ray(A,v)`, a
  parallel, a perpendicular, a perpendicular bisector, a reflected and a
  rotated line; drag `B` through `A` and back; check that `y = 2x + 1`, a
  tangent to a circle at a point, a three-point angle bisector and
  `Tangent(a, f)` show no cue; hide an object and hide its layer; pan, zoom and
  resize; open Graphics View 2 (no cue); export PNG, PDF, SVG, copy to the
  clipboard and print (no cue); restart (the option persists); New and Open
  (the option unchanged); in EN and ES.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*, or the entry
  gate cannot confirm the `BOUNDED_PHASE` scope;
- the sense of an admitted route cannot be obtained from `getDirection` or its
  `T-F1-SENSE` test fails (the row is then excluded and reported);
- a route cannot be classified from class identity through existing public
  API;
- eligibility, placement, rendering or the UI controls would need a change in
  `source/shared/**`, an inherited class, a new XML element or a broader
  product contract;
- the overlay would reach an export, print or clipboard route, or GV2;
- the option would change document, macro or preferences XML, undo state, hit
  testing or Classic behavior;
- a geometric ambiguity has no author-approved policy (`AGENTS.md` §16).
