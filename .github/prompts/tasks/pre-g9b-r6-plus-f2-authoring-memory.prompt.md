# PRE-G9B-R6-plus-F2 — authoring-memory refinements

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — DESIGN AUTHOR APPROVED — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F2-PREP` at the author's
instruction of 2026-10-11 and reconciled by `PRE-G9B-R6-plus-F2-PREP-CLOSEOUT`
with the author decisions of 2026-10-11 on `DQ-F2-1` to `DQ-F2-10`
([F2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f2_author_decisions_record.md)
v1.0). The governing design is the
[reconciled design](../../../docs/architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); the evidence is the frozen
[F2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f2_preparation_characterization_report.md)
(`K0`–`K16`) and its JSON mirror. The design approval does not authorize the
implementation. The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F2` and the exact base (the published preparation package or
a named descendant). That instruction may authorize, as the first tracked edit
of the phase, an amendment of this prompt to the authorized state, following
the `F1` precedent. This file is an execution contract, not a second policy
document: the state classes are stated once in
`geocedg/specs/units/unit-system.md` §17.2; the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the F2 semantics once
in the reconciled design, whose section numbers are cited below; the
decisions once in the decision record. The first deliverable of F2 turns the
approved design into the durable specification named under *Required
design/specification*.

```text
PRE-G9B-R6-plus-F2 =
DESIGN — AUTHOR APPROVED (2026-10-11)
PREPARED — NOT AUTHORIZED

PREPARATION              = PRE-G9B-R6-plus-F2-PREP (DOCUMENTATION_STATUS_ONLY)
CHARACTERIZATION         = COMPLETE (2026-10-11, on P_F1 24985f14)
AUTHOR DECISIONS         = DQ-F2-1 to DQ-F2-10 RECORDED (2026-10-11, record v1.0)
OPEN TECHNICAL ITEMS     = OTQ-F2-1 to OTQ-F2-3 (none blocking; record §4)
IMPLEMENTATION           = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
authorApproved (design)  = true    (the reconciled design only)
authorApproved (implementation) = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP AUTHORING-MODE ORCHESTRATION IN THE GEOCEDG
                           GRAPHICS VIEW 1 CONTROLLER: session memory for
                           Parallel and Perpendicular (two slots, release-time
                           injection, exact-instance currentness) and a
                           GeoCeDG controller companion hiding the Angle with
                           Given Size angle by ordinary visibility; no kernel,
                           command, algorithm, XML, identity, GV2, Classic or
                           inherited-class change
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED (hard)
                           PRE-G9B-R6-plus-F1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (recommended predecessor, no coupling)
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PLANNED CLASS            = BOUNDED_PHASE — author-accepted (DQ-F2-10), conditioned
                           on the implementation entry gate; one registered PHASE
                           PRE-G9B-R6-PLUS-F2
EXCLUDED                 = ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY (DQ-F2-9)
NEXT_SUBPHASE            = none implied; F3, F4, G stay unauthorized
```

`authorApproved (implementation) = false` means that no technical candidate of
this phase has been author-approved. Technical verification never creates
author approval. Once authorized, the phase stops with one exact technically
verified candidate pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement, once authorized, the approved authoring-memory refinements:

1. **Parallel and perpendicular memory** (design §3–§5): two independent
   `SESSION` slots in the GeoCeDG Graphics View 1 controller; release-time
   injection of a usable remembered reference for a click on an existing point
   or on empty space; the slot written only from the success certificate of
   design §4.3 (the created line's algorithm and its `input[1]`, never a
   label); currentness by exact-instance membership in the construction order;
   suspension while undefined, non-linear, hidden or not shown in GV1; eager
   clearing on removal, replacement, undo/redo rebuilds, New, Open and full
   document replacement, plus the lazy check at every use; the inherited
   point-on-path, double-click, preview and hit-priority behavior preserved.
2. **Angle with Given Size hidden by default** (design §6): a GeoCeDG-owned
   subclass of `EuclidianControllerFor3DCompanion`, returned by the GeoCeDG
   controller's `newCompanion()`, whose
   `createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)` hides the
   returned angle by ordinary `setEuclidianVisible(false)` before the inherited
   undo store; rotated point visible; angle algebra-visible and recoverable.
3. The tests of design §9 and the durable specification of *Required
   design/specification*.

### Author decisions this prompt implements (record v1.0 §1)

| Id | Decision |
|---|---|
| `DQ-F2-1` | memories survive ordinary tool changes within the same document session; not serialized |
| `DQ-F2-2` | two independent slots (parallel, perpendicular); replaced only after a successful construction; cancellation or failure never replaces a valid slot |
| `DQ-F2-3` | with a valid memory, an existing point or empty space may start a construction; without one, an explicit reference is required; no invented reference or unintended geometry; point-on-path preserved; no press-time pre-seeding where it conflicts; release-time injection if the tool flows prove it correct |
| `DQ-F2-4` | 2D lines, segments, rays, polygon edges, appropriate 2D vectors and linear functions only; the reference actually used by the successful algorithm; a label is never the sole certificate; unsupported and 3D inputs keep inherited behavior and are not remembered |
| `DQ-F2-5` | exact object reference only; exact-instance membership; label lookup only corroborates; clear on removal, replacement, staleness, New, Open, document replacement and undo/redo rebuilds; keep across rename and same-instance recompute; suspend while invalid or invisible; no durable IDs, identity infrastructure or document associations |
| `DQ-F2-6` | no preview or indicator in v1; preview never updates memory; memory updates only on a committed construction; no undo point for memory |
| `DQ-F2-7` | GeoCeDG controller-companion override of `createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)`; call sites verified; tool route only; hidden before the undo store; `AlgoDispatcher.angle`, `Angle(...)`, GGBScript and shared algorithms unchanged |
| `DQ-F2-8` | angle hidden in the graphical views by ordinary visibility, valid dependent object, in the Algebra View; rotated point visible; user may show it; save, reload, undo, redo unchanged; interception only in GeoCeDG GV1; no per-view overlay; GV2 controller and Classic unchanged |
| `DQ-F2-9` | working-layer direct entry stays a separate pending enhancement; not in F2 |
| `DQ-F2-10` | Desktop-only orchestration and a GeoCeDG-owned companion; `BOUNDED_PHASE`, one `PHASE`, subject to the entry gate; no shared, command, XML, identity or inherited change; missing seams stop the work |

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_F1                  = 24985f14c565b9ba6282ec80fa0cb7be20a2e459
                        (published F1 closeout, docs/validation/pre_g9b_r6_plus_f1_closeout_record.md)
tree                  = 1c683ebb3e303b7692915feae3417cf51e4cce22
preparation candidate = 09bb63022c649cb6882440120aa121e8ecbb799f
                        tree 31d436733a71f626b412092515d23bcd87b30b0d
                        (STATIC verification-d61c1adb78414a2ab4608ddabfd078a7)
closeout candidate    = the local commit of phase/pre-g9b-r6-plus-f2-prep
                        that contains this reconciled file (documentation only),
                        direct child of 09bb6302
```

**Entry gate.** Before any tracked change the agent verifies that local
`main`, `origin/main` and the live remote `main` resolve to the base named by
the authorizing instruction (normally the published F2 preparation package),
that the worktree is clean, and that no F2 code exists. It then re-establishes
at that base: the call-site census of the four-argument `createAngle` (only
`DialogManager.doAngleFixed`); the GCEC `switchModeForProcessMode` `super`
call; the GV2 controller class; and the hooks for `OTQ-F2-1` and `OTQ-F2-2`
within GeoCeDG-owned code. It confirms that every planned change lies in the
`BOUNDED_PHASE` scope of `DQ-F2-10`. Any other need stops the work for author
review and classification.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` §17.2 and `verification-levels.md`; the F2 author-decision
record v1.0; this prompt; the reconciled design; then the characterization
report, its JSON mirror and the decision preparation record; then the `P0` F2
candidate, the `P0` report finding 18 and probe P6, and older evidence. Probe
outputs, reports and screenshots are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones; the implementation re-establishes each at its
base.

**Desktop (`source/desktop/desktop`), GeoCeDG-owned code only.**

- `GeoCeDGEuclidianController`: for `MODE_PARALLEL` and `MODE_ORTHOGONAL`
  only, release-time injection before the existing `super` call of
  `switchModeForProcessMode` and the certificate read-back after it (design
  §4); the memory field, not reset in `setMode`; the `newCompanion()`
  override.
- A new session helper for the two slots (proposed
  `GeoCeDGLineReferenceMemory`) with admissibility (design §3.2), currentness
  and suspension (§3.3) and the state machine (§3.4).
- A new GeoCeDG companion class extending `EuclidianControllerFor3DCompanion`,
  overriding only `createAngle(GeoPointND, GeoPointND, GeoNumberValue,
  boolean)` (design §6.1); it reads no controller field.
- The eager-clearing hook of `OTQ-F2-1` through public API or existing
  `AppGeoCeDG` document-boundary overrides.
- Desktop tests for design §9.

**Documentation and verification.**

- The durable specification of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The phase registration `PRE-G9B-R6-PLUS-F2` (selection, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, through the official mechanism.
- The EN and ES user guides (tool behavior; the GV1-only limitation) and their
  outline pins, if touched.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, `EuclidianController`, companions,
  `DialogManager`, algorithms, `AlgoDispatcher`, commands, `CmdAngle`, XML
  handlers, GGBScript).
- Overriding or modifying `EuclidianController.parallel(Hits, boolean)` or any
  other inherited class; an inherited-class need stops the work.
- A new command, alias, syntax, algorithm, `GeoElement` type or construction
  element; any change to what `Line`, `PerpendicularLine`, `Angle`, GGBScript,
  API or Algebra input create or show.
- Durable reference IDs, document associations, kernel identity
  infrastructure, label-based resolution, or serialization of the memory; an
  undo point for memory operations.
- New XML elements or attributes.
- `isInConstructionList`, `Construction.isInConstructionList`, `isLabelSet`,
  `getConstruction()` or a label as currentness or success certificate.
- Pre-seeding the selection at press, at mode entry or across events;
  suppressing or changing the point-on-path gesture, the double-click
  Redefine dialog, the preview or hit priority; remembering 3D, non-linear or
  other unsupported references.
- A preview or indicator of the remembered reference.
- Graphics View 2's tool controller, a GeoCeDG GV2 controller, a
  `GuiManagerGeoCeDG.newEuclidianView` override; Classic or upstream behavior.
- `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`, `application-profile.yml`, the
  working-layer chooser and the A-2 tests.
- `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than an authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop application/controller layer only (`AGENTS.md` §4): authoring-mode
orchestration feeding unchanged upstream tool code, and an ordinary object
property set on the tool route. The memory is `SESSION` state of the GeoCeDG
GV1 controller; the hidden angle is ordinary `DOCUMENT` visibility. Kernel,
export adapters and Python: unchanged.

## Required design/specification

First deliverable of the phase, before product code:
`geocedg/specs/ui/authoring-memory.md` (proposed path) — the durable contract
(targets, slots and ownership, admissibility, currentness and suspension, the
state machine, release-time injection and the success certificate, the
preserved inherited gestures, cancellation and undo, the hidden angle and its
persistence, controller scope versus object property, GV1-only coverage,
exclusion of the working-layer entry, invariants), from the reconciled design
and the decision record, status `PROPOSED` until the author approves it, with
`OTQ-F2-1` to `OTQ-F2-3` resolved. No ADR is expected; one is required if a
seam beyond the approved design becomes necessary.

## Geometric invariants and degeneracies

- Design §2 invariants.
- The created objects are those the unchanged upstream tools create for the
  same point and reference (algorithm class and inputs identical).
- A remembered reference is reused only while it is the identical current
  instance and usable (design §3.3); nothing is guessed from labels or
  positions.
- The hidden angle keeps its value, orientation (`AngleStyle`, clockwise
  handling) and dependencies.

## Compatibility and serialization

- Documents written by F2 contain no memory trace; a tool-created angle is
  saved with the ordinary `<show object="false">` and opens hidden in older
  GeoCeDG and Classic.
- Commands, scripts and documents written before F2 behave as before.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations are design §9: `T-F2-PAR-POINT-FIRST`,
`T-F2-PERP-POINT-FIRST`, `T-F2-REF-FIRST-REPLACES`, `T-F2-NO-MEMORY`,
`T-F2-SLOTS-INDEPENDENT`, `T-F2-EMPTY-SPACE`, `T-F2-2610-UNCHANGED`,
`T-F2-TYPES`, `T-F2-CERTIFICATE`, `T-F2-LIFECYCLE`, `T-F2-CANCEL`,
`T-F2-PREVIEW-NO-CREATION`, `T-F2-UNDO`, `T-F2-NO-SERIALIZATION`,
`T-F2-COMMANDS-UNCHANGED`, `T-F2-ANGLE-HIDDEN`, `T-F2-ANGLE-ROUTE-ONLY`,
`T-F2-ANGLE-UNDO-REDO`, `T-F2-ANGLE-PERSISTENCE`, `T-F2-ANGLE-USER-SHOW`,
`T-F2-ANGLE-COMMAND-UNCHANGED`, `T-F2-VIEWS`, `T-F2-CLASSIC-UNCHANGED`, and the
author smoke `T-SMOKE` (not performed by the agent). Gesture tests drive the
real GV1 controller with press/release events; undo assertions use the
synchronous undo baseline and store listeners.

Commands (PowerShell; log directories fresh, under `artifacts\agent\`):

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F2 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F2 -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-F2` covers at least
`compile.desktop.semantic` and `junit.desktop.pre-g9b-r6-plus-f2.semantic`.
`PHASE` runs once on the frozen clean commit and tree. No `INTEGRATION` or
`FINAL`. A run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt; the design approval of 2026-10-11 does
not authorize implementation. A future author instruction may authorize `F2`
on an exact base; it then authorizes local implementation on a local branch,
local commits, focused tests, the phase registration and its official catalog
updates, development `INFRA_UNIT` and `STATIC` runs, the frozen acceptance run,
and one frozen technical candidate for author review and author smoke. It
never authorizes self-approval, author smoke by the agent, a run above the
frozen class, or a change of the class; a proven inability of the frozen class
to cover the change is reported as a reclassification proposal, never applied
silently.

`F3`, `F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every
open observation and enhancement not named here, including
`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`. Author approval is never created by
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
PRE-G9B-R6-plus-F2      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (BOUNDED_PHASE, one PHASE)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification; the Desktop, registry and guide deltas with their tests.
- A candidate report with the entry gate, report `K3`–`K13` re-established
  with every correction, the resolution of `OTQ-F2-1` to `OTQ-F2-3`, test
  evidence, the exact commands, exit codes, report paths and verdicts, and an
  author-smoke checklist: Parallel with a line, then two further points
  (memory reused), then a new segment and a point (memory replaced); the same
  for Perpendicular while Parallel keeps its own reference; switch to another
  tool and back; Escape; empty-space click with and without a memory; select a
  line and click a second line (point on the second line, inherited); double
  click a line (Redefine dialog, inherited); hide, undefine and re-show the
  remembered line (suspended, then reused); undo after a creation (memory
  cleared); delete and redefine the remembered line; New and Open; a vector, a
  linear function and a polygon edge as references; Angle with Given Size from
  two points and from a segment, clockwise and counter-clockwise (angle
  hidden, rotated point visible), show it from the Algebra View, undo/redo,
  save and reopen; `Angle(B, A, 40°)` typed (visible); the tool in Graphics
  View 2 (inherited behavior); in EN and ES.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- a target needs a change in `source/shared/**` or in an inherited class, or
  an override of `parallel(Hits, boolean)`;
- the reference actually used cannot be read back with the success
  certificate of design §4.3, or currentness cannot be established by
  exact-instance membership;
- release-time injection changes the point-on-path gesture, the double-click
  behavior, the preview, hit priority or the accepted types;
- the companion is not reached by the tool, or is reached by a command,
  script, API call, macro or reload;
- the eager-clearing hook needs an inherited change;
- the memory would need a durable identity, label resolution, document
  association or serialization;
- the hidden angle would need a new XML element or attribute;
- the frozen class cannot cover a required change.
