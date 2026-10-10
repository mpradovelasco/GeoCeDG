# PRE-G9B-R6-plus-F1 — orientation cue

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F1-PREP` at the author's
instruction of 2026-10-11, given after `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`
was closed and published. That instruction authorized characterization,
design reconciliation, author-decision preparation and this prompt only. It
stated that **F1 product implementation is not authorized**, that the `P0`
design candidate is historical evidence and not authority, and that no author
decision may be marked resolved without an explicit prior author decision. The
evidence behind the summary below is in the
[F1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md)
(sections `K0`–`K17`); the proposed design, the alternatives and the test
matrix are in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md);
the questions, alternatives and recommendations are in the
[author-decision preparation record](../../../docs/validation/pre_g9b_r6_plus_f1_author_decision_preparation_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F1` and the exact prepared candidate or base, and that
disposes of `DQ-F1-1` to `DQ-F1-9`. That instruction may authorize, as the
first tracked edit of the phase, an amendment of this prompt to the authorized
state, following the `E1-L`, `E1-X1`, `E2` and `E3` precedent. This file is an
execution contract, not a second policy document: the state classes are stated
once in `geocedg/specs/units/unit-system.md` §17.2; the verification classes
once in `geocedg/specs/operations/verification-levels.md` §12.8; the
orientation evidence once in the characterization report; the proposed
semantics once in the reconciled design, whose section numbers are cited
below. Where this prompt names a design value that no durable specification
holds yet, the value is a proposal until the author disposes of the matching
`DQ-F1-*`, and the first deliverable of `F1` turns it into the durable
specification named under *Required design/specification*.

```text
PRE-G9B-R6-plus-F1 =
PREPARED — NOT AUTHORIZED

PREPARATION ACTIVITY     = PRE-G9B-R6-plus-F1-PREP (DOCUMENTATION_STATUS_ONLY)
CHARACTERIZATION         = COMPLETE (2026-10-11, on bde2827b)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP PRESENTATION-ONLY OVERLAY + DESKTOP USER
                           PREFERENCE AND ITS UI SURFACE (seam S1 proposed,
                           DQ-F1-8); no kernel, serialization, export or
                           shared-drawable change
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED (hard)
                           PRE-G9B-R6-plus-E3 = PASS — AUTHOR APPROVED
                                                (recommended predecessor, no coupling)
                           POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1
                                              = PASS — AUTHOR APPROVED — PUBLISHED
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PROPOSED CLASS           = BOUNDED_PHASE — NOT FROZEN (DQ-F1-8)
PRECONDITION             = author dispositions of DQ-F1-1 to DQ-F1-9
NEXT_SUBPHASE            = none implied; F2, F3, F4, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-fixed orientation cue (mini-track plan §2 "Orientation
display"): an optional, discreet presentation cue, preferably a hollow arrow,
for the actual direction the kernel uses for a line, segment or ray; derived
from kernel semantics, never from the viewport; changing neither geometry nor
command behavior. Subject to the `DQ-F1` dispositions:

1. A GeoCeDG-owned, read-only **cue model** that, for a geo and a view, decides
   eligibility (design §3.3, §4) and returns the world sense
   (`GeoLine.getDirection`), the screen anchor and the screen angle (design
   §5). It never mutates kernel, view or document state.
2. A **GV1 overlay** in `GeoCeDGEuclidianView.paint`, after `super.paint`,
   drawing a hollow arrowhead per eligible object (design §6, §7 S1), with a
   small GeoCeDG-owned path builder.
3. A **global option** (`DQ-F1-1`) stored as the preference of `DQ-F1-6`, with
   the UI surface of `DQ-F1-9`, repainting every GeoCeDG window on change.
4. Tests of design §10 and the durable specification of *Required
   design/specification*.

### Characterization results at the base (summary; evidence in the report)

- Accessor authority: `getDirection` = `(y, −x)`; path parameters,
  `UnitVector`/`Direction` and oriented `Angle` agree on every defined probed
  object; `getDirectionInD3` is opposite whenever there is no end point (51 of
  64 objects) — `K3`, `K4`.
- Routes classified `O-CONSTRUCTIVE`, `O-INHERITED`, `O-CONFIGURATION`,
  `O-REPRESENTATION`, `O-UNKNOWN` — `K6`; equation lines and tangents at a
  point of a conic have representation-dependent senses (`y = 2x + 1` versus
  `2x − y + 1 = 0`; `x² + y² = 4` versus `−x² − y² = −4`) — `K4`.
- `isDefined()` is true for a zero-length segment and for infinite
  coefficients; identity survives temporary invalidity; the external tangent
  and the three-point bisector jump at degenerate configurations — `K7`.
- No export, print, clipboard, CLI, LaTeX or DXF route calls
  `GeoCeDGEuclidianView.paint`; drawable-level cues would reach picture
  exports — `K8`. GV2 is an upstream `EuclidianViewFor3DD` — `K10`.
- The outline-arrow geometry exists only as a private segment-ending method —
  `K9`. The profile holds 130 actions, pinned by `GeoCeDGProfile` and eleven test
  assertions in eight classes — `K14`.

### Decisions requested before authorization

| Id | Topic |
|---|---|
| `DQ-F1-1` | global option or per-object presentation |
| `DQ-F1-2` | GV1 only or GV1 and GV2 |
| `DQ-F1-3` | screen-only or exported |
| `DQ-F1-4` | style, size, colour, placement |
| `DQ-F1-5` | eligibility per orientation class; unknown and convention-dependent routes |
| `DQ-F1-6` | preference class, key, values, default, persistence, New/Open |
| `DQ-F1-7` | suppression and overlap |
| `DQ-F1-8` | seam and verification class |
| `DQ-F1-9` | UI surface and catalog |

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_C1                  = bde2827bb21e4a28e080db6a65b4abc892071cf8
                        (published C1 closeout, docs/validation/post_e3_e2_pstricks_dimension_angle_c1_closeout_record.md §6)
tree                  = 1b74102cea5151b8a6c571cb36b96b33af6fb3c8
preparation candidate = the local commit of phase/pre-g9b-r6-plus-f1-prep
                        that contains this file (documentation only)
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base (or that the
authorized preparation candidate is its only delta), and that the worktree is
clean; otherwise it stops and reports.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` §17.2 and `verification-levels.md`; the author dispositions
of `DQ-F1-1`–`DQ-F1-9`; this prompt; then the reconciled design candidate, the
characterization report and its JSON mirror; then the `P0` F1 candidate, the
`P0` report `F17` and older evidence. Probe outputs, reports and screenshots
are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Subject to the `DQ-F1` dispositions; paths are the expected ones and the
implementation re-establishes each at its base.

**Desktop (`source/desktop/desktop`), GeoCeDG-owned code only.**

- A cue model and path builder under `org.geocedg.desktop` (new classes),
  reading `GeoLine.getDirection`, coefficients, endpoints, parent-algorithm
  class identity, drawable presence, `isDefined`, `isEuclidianVisible`,
  `isVisibleInView`, `App.isLayerShown`, line thickness, decoration, view
  transform and size.
- The overlay call in `GeoCeDGEuclidianView.paint`, after `super.paint`; no
  change of the existing export-area or intersection overlays.
- The preference class (key, values, default, test seam) following
  `GeoCeDGUnitPreferences`; its owner in `AppGeoCeDG`; repaint of every
  GeoCeDG window on change.
- The UI surface of `DQ-F1-9`: a profile action (`apps/geocedg/application-profile.yml`,
  `GeoCeDGActionRegistry` target, `execute` and `checked` cases, localized
  texts) and/or a row in an existing GeoCeDG-owned Layout & Presentation panel.
- Under `DQ-F1-2` = V12a only: a GeoCeDG GV2 view class and the
  `GuiManagerGeoCeDG` override that creates it.
- Desktop tests for design §10.

**Documentation and verification.**

- The durable specification of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- If `DQ-F1-9` adds an action: the profile count in `GeoCeDGProfile`, the test
  pins of report §K14, the approved ids in
  `tools/agent/workspace-profile-validation.ps1` and its static-contract pin.
- The phase registration `PRE-G9B-R6-PLUS-F1` (selection, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, through the official mechanism.
- The EN and ES user guides (a short description of the option) and their
  outline pins, if touched.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, algorithms, `GeoLine`, drawables,
  exporters, `EuclidianView`), unless the author disposed `DQ-F1-8` = S3 and
  froze the matching class.
- A new `GeoElement`, command, alias, syntax, algorithm, dependency, output or
  construction element; any change of construction order, labels, styles,
  layers, visibility flags, selection or hit testing.
- Any document, macro or preferences XML change; a per-object attribute; an
  undo point for the option.
- Choosing the sense from `getDirectionInD3`, `DrawLine`'s screen point order,
  labels, names, captions, definition text, coordinates, construction indices
  or visual proximity; classifying a route by anything other than algorithm
  class identity (design §3.2).
- Showing a cue for an `O-UNKNOWN` route, or for any class the author did not
  admit under `DQ-F1-5`.
- Export, print, clipboard, CLI, LaTeX or DXF participation unless `DQ-F1-3`
  says so and the class is re-frozen.
- New upstream hooks in `AppD` or `OptionsLayoutD`, or any upstream file change,
  unless authorized under `DQ-F1-9`/`DQ-F1-8`.
- Changes to Classic or upstream application behavior.
- `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop application/view layer only (`AGENTS.md` §4: a GUI presentation
consuming existing kernel semantics read-only; no dependency graph,
serialization or identity effect). The kernel remains the single authority of
the sense; the cue is `DERIVED_TRANSIENT` presentation; the option is
`USER_PREFERENCE` (or the class of `DQ-F1-6`). Export adapters: unchanged.
Python: none.

## Required design/specification

First deliverables of the phase, before product code:

1. `geocedg/specs/presentation/orientation-cue.md` (proposed path) — the
   durable contract: accessor, orientation classes and admitted routes,
   eligibility predicate, placement and rendering, option class and lifecycle,
   views, export behavior, invariants, from the reconciled design and the `DQ`
   dispositions.
2. No ADR is proposed unless the author chooses S2 or S3 under `DQ-F1-8`; then
   an ADR records the seam, status `PROPOSED` until author approval.

## Geometric invariants and degeneracies

- Construction geometry, DAG, identity, serialization, algorithms and command
  behavior unchanged (design §2).
- The drawn sense is `getDirection` for every drawn cue, for every view
  transform, zoom, pan, size and axis ratio; placement never reverses it.
- No cue for undefined, zero-direction, non-finite, hidden, hidden-layer,
  zero-thickness, not-drawn, logarithmic-axis or short-chord objects (design
  §4); no exception on any of them.
- Temporary invalidity hides the cue and creates no identity; recovery shows it
  with the then-current sense.
- Determinism: the anchor and angle are pure functions of the current object
  state, the view transform and size, and the arrow size.

## Compatibility and serialization

- Documents, macros and preferences XML byte-identical with the option on and
  off; documents saved by `F1` open unchanged in older GeoCeDG and Classic.
- The preference key is ignored by older GeoCeDG and by Classic.
- Exports byte-identical with the option on and off (if `DQ-F1-3` = X0).

<!-- geocedg-field: required_checks -->
## Required tests and commands

Proposed verification plan (frozen at authorization with `DQ-F1-8`). The
obligations are design §10: `T-F1-SENSE`, `T-F1-REVERSAL`, `T-F1-DYNAMIC`,
`T-F1-DEGENERATE`, `T-F1-DISCONTINUITY`, `T-F1-ELIGIBILITY`,
`T-F1-VISIBILITY`, `T-F1-PLACEMENT`, `T-F1-VIEWS`, `T-F1-EXPORT`,
`T-F1-NO-MUTATION`, `T-F1-PREFERENCE`, `T-F1-HIT`, `T-F1-CLASSIC`,
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

The registered phase `PRE-G9B-R6-PLUS-F1` requires at least
`compile.shared.semantic`, `compile.desktop.semantic` and
`junit.desktop.pre-g9b-r6-plus-f1.semantic` (precedent
`PRE-G9B-R6-PLUS-E1-X1`, `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`), plus
`workspace-profile.product` if `DQ-F1-9` adds an action. `PHASE` runs once on
the frozen clean commit and tree. No `INTEGRATION` or `FINAL` unless the
author freezes a higher class. A run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`F1` on an exact base with the `DQ-F1` dispositions; it then authorizes local
implementation on a local branch, local commits, focused tests, the phase
registration and its official catalog updates, development `INFRA_UNIT` and
`STATIC` runs, the frozen acceptance run, and one frozen technical candidate
for author review and author smoke. It never authorizes self-approval, author
smoke by the agent, a run above the frozen class, or a change of the class; a
proven inability of the frozen class to cover the change is reported as a
reclassification proposal, never applied silently.

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
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification; the Desktop, profile (if any), registry and guide deltas
  with their tests.
- A candidate report with the entry gate, `K3`–`K14` re-established with every
  correction, the `DQ` dispositions applied, test evidence, the exact commands,
  exit codes, report paths and verdicts, and an author-smoke checklist: enable
  the option and check `Line(A,B)` versus `Line(B,A)`, `Segment`, `Ray(A,B)`,
  `Ray(A,v)`, a parallel, a perpendicular and a perpendicular bisector; drag
  `B` through `A` and back; check that the classes excluded by `DQ-F1-5` (for
  example `y = 2x + 1`) show no cue; hide an object and hide its layer; pan,
  zoom and resize; export PNG, PDF, SVG and print with the option on (no cue
  if screen-only); restart and check the option persists; New and Open; GV2
  per `DQ-F1-2`; in EN and ES.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- the sense of an admitted route cannot be obtained from `getDirection`, or the
  route class cannot be determined from algorithm class identity;
- eligibility, placement or rendering would need a change in
  `source/shared/**`, an upstream hook or a new XML element;
- the overlay would reach an export, print or clipboard route while `DQ-F1-3`
  is screen-only;
- the option would change document, macro or preferences XML, undo state,
  hit testing or Classic behavior;
- a geometric ambiguity has no author-approved policy (`AGENTS.md` §16);
- the frozen class cannot cover a required change.
