# PRE-G9B-R6-plus-F2 — authoring-memory refinements

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F2-PREP` at the author's
instruction of 2026-10-11 ("Authoring-Memory Refinements — Characterization and
Design Preparation"), given after `PRE-G9B-R6-plus-F1` was closed and
published. That instruction authorized characterization, design
reconciliation, author-decision preparation and this prompt only. It stated
that **F2 implementation is not authorized**, that the `P0` F2 design
candidate is evidence and not authority, and that no author decision may be
marked resolved without an explicit prior author decision. The evidence behind
the summary below is in the
[F2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f2_preparation_characterization_report.md)
(sections `K0`–`K16`); the proposed design, the alternatives and the test
matrix are in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md);
the questions, alternatives and recommendations are in the
[author-decision preparation record](../../../docs/validation/pre_g9b_r6_plus_f2_author_decision_preparation_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F2` and the exact prepared candidate or base, and that
disposes of `DQ-F2-1` to `DQ-F2-10`. That instruction may authorize, as the
first tracked edit of the phase, an amendment of this prompt to the authorized
state, following the `F1` precedent. This file is an execution contract, not a
second policy document: the state classes are stated once in
`geocedg/specs/units/unit-system.md` §17.2; the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the lifecycle and
route evidence once in the characterization report; the proposed semantics
once in the reconciled design, whose section numbers are cited below. Where
this prompt names a design value that no durable specification holds yet, the
value is a proposal until the author disposes of the matching `DQ-F2-*`, and
the first deliverable of `F2` turns it into the durable specification named
under *Required design/specification*.

```text
PRE-G9B-R6-plus-F2 =
PREPARED — NOT AUTHORIZED

PREPARATION ACTIVITY     = PRE-G9B-R6-plus-F2-PREP (DOCUMENTATION_STATUS_ONLY)
CHARACTERIZATION         = COMPLETE (2026-10-11, on P_F1 24985f14)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP AUTHORING-MODE ORCHESTRATION IN THE GEOCEDG
                           GRAPHICS VIEW 1 CONTROLLER (session memory for
                           Parallel and Perpendicular; tool-only hidden angle
                           through a GeoCeDG controller companion); no kernel,
                           command, algorithm, serialization or inherited-class
                           change
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED (hard)
                           PRE-G9B-R6-plus-F1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (recommended predecessor, no coupling)
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PROPOSED CLASS           = BOUNDED_PHASE — NOT FROZEN (DQ-F2-10)
PRECONDITION             = author dispositions of DQ-F2-1 to DQ-F2-10
WORKING-LAYER ENTRY      = NOT INCLUDED unless DQ-F2-9 includes it
NEXT_SUBPHASE            = none implied; F3, F4, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-fixed authoring-memory refinements (mini-track plan §2),
subject to the `DQ-F2` dispositions:

1. **Parallel and perpendicular memory** (design §3–§6). Each tool keeps an
   independent, session-only remembered reference. Point first: reuse the
   valid remembered reference. Reference first, then a point: create with the
   new reference and replace the memory. Nothing valid remembered: today's
   flow, which requests the reference. Proposed mechanism: release-time
   seeding in the existing GeoCeDG `switchModeForProcessMode` override and
   read-back of the reference actually used from the created algorithm's
   `input[1]`; currentness by exact-instance membership (design §3.2), never by
   `isInConstructionList`, `isLabelSet`, a stored label or position.
2. **Angle with Given Size hidden by default** (design §7). The angle created
   by the tool is hidden in the Graphics Views, the rotated point stays
   visible, the user can show the angle later; proposed mechanism: a GeoCeDG
   subclass of `EuclidianControllerFor3DCompanion` returned by the GeoCeDG
   controller's `newCompanion()`, whose `createAngle(p1, p2, a, clockWise)`
   hides the returned angle before the inherited undo store.
3. The tests of design §10 and the durable specification of *Required
   design/specification*.

### Author-fixed targets and open questions

| Id | Subject | Recommendation (pending) |
|---|---|---|
| `DQ-F2-1` | persistence across tool changes | survive tool changes |
| `DQ-F2-2` | independent slots and replacement | two slots, replaced on successful creation |
| `DQ-F2-3` | first-use gestures, missing reference | point-first includes empty space; #2610 unchanged |
| `DQ-F2-4` | admissible types and hit priority | accepted references with a labelled 2D output; upstream priority |
| `DQ-F2-5` | identity, currentness, reset events | identity scan; suspend undefined or hidden; lazy plus document-boundary clearing; undo forgets |
| `DQ-F2-6` | cancellation, preview, success, undo | release-time seeding; no preview; no indicator |
| `DQ-F2-7` | tool-only angle hook | GeoCeDG companion `createAngle` override |
| `DQ-F2-8` | angle visibility, persistence, recovery | hidden in Graphics Views only; ordinary persistence |
| `DQ-F2-9` | working-layer direct entry | keep independent (not in F2) |
| `DQ-F2-10` | architecture and class | GV1 only; `BOUNDED_PHASE` |

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_F1                  = 24985f14c565b9ba6282ec80fa0cb7be20a2e459
                        (published F1 closeout, docs/validation/pre_g9b_r6_plus_f1_closeout_record.md)
tree                  = 1c683ebb3e303b7692915feae3417cf51e4cce22
preparation candidate = the local commit of phase/pre-g9b-r6-plus-f2-prep
                        that contains this file (documentation only)
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base (or that the
authorized preparation candidate is its only delta), and that the worktree is
clean; otherwise it stops and reports. It then confirms that every planned
change lies in the scope frozen by the `DQ-F2-10` disposition.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` §17.2 and `verification-levels.md`; the author dispositions
of `DQ-F2-1`–`DQ-F2-10`; this prompt; then the reconciled design candidate, the
characterization report and its JSON mirror; then the `P0` F2 candidate, the
`P0` report finding 18 and probe P6, and older evidence. Probe outputs,
reports and screenshots are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones; the implementation re-establishes each at its
base. All under the `DQ-F2` dispositions.

**Desktop (`source/desktop/desktop`), GeoCeDG-owned code only.**

- `GeoCeDGEuclidianController`: for `MODE_PARALLEL` and `MODE_ORTHOGONAL`
  only, release-time seeding before the existing `super` call of
  `switchModeForProcessMode` and the read-back of the used reference after it;
  the memory field (not reset in `setMode` if `DQ-F2-1` keeps it); the
  `newCompanion()` override.
- A new session helper for the two slots (proposed
  `GeoCeDGLineReferenceMemory`) with the currentness predicate of design §3.2.
- A new GeoCeDG companion class extending `EuclidianControllerFor3DCompanion`,
  overriding only `createAngle(GeoPointND, GeoPointND, GeoNumberValue,
  boolean)`; it reads no controller field.
- Document-boundary clearing through the existing `AppGeoCeDG` New/Open/
  replacement overrides, if `DQ-F2-5` adopts it.
- Only if `DQ-F2-10` chooses GV2 coverage: a GeoCeDG GV2 controller subclass of
  `EuclidianControllerFor3DD` and a `GuiManagerGeoCeDG.newEuclidianView`
  override.
- Only if `DQ-F2-9` includes it: `GeoCeDGWorkingLayerChooser.dialog`, its EN and
  ES texts in `apps/geocedg/application-profile.yml`, and the two pinned A-2
  chooser tests.
- Desktop tests for design §10.

**Documentation and verification.**

- The durable specification of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The phase registration `PRE-G9B-R6-PLUS-F2` (selection, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, through the official mechanism.
- The EN and ES user guides (the tool behavior; the GV2 limitation if
  `DQ-F2-10` keeps GV1 only) and their outline pins, if touched.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, `EuclidianController`,
  companions, `DialogManager`, algorithms, `AlgoDispatcher`, commands,
  `CmdAngle`, XML handlers).
- Overriding or modifying `EuclidianController.parallel(Hits, boolean)`
  (inherited and final) or any other inherited class; an inherited-class
  change is an authorization blocker.
- A new command, alias, syntax, algorithm, `GeoElement` type or construction
  element; any change to what `Line`, `PerpendicularLine`, `Angle`, scripts, API
  or Algebra input create.
- Durable reference IDs, document metadata, new kernel identity, persistent
  reference associations, label-based resolution of a remembered reference, or
  serialization of the memory; an undo point for remembering.
- New XML elements or attributes; any document, macro or preferences XML change
  beyond ordinary objects and ordinary visibility.
- Using `isInConstructionList`, `Construction.isInConstructionList`,
  `isLabelSet` or `getConstruction()` as a currentness test.
- Suppressing or changing the issue #2610 point-on-path gesture, the preview,
  hit priority or the accepted reference types beyond the `DQ-F2-4`
  disposition.
- Graphics View 2 or any other view unless `DQ-F2-10` includes it; the
  working-layer entry unless `DQ-F2-9` includes it.
- Changes to Classic or upstream application behavior.
- `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than an authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop application/controller layer only (`AGENTS.md` §4): authoring-mode
orchestration that feeds unchanged upstream tool code and sets an ordinary
object property. The memory is `SESSION` state of the GeoCeDG controller;
the hidden angle is ordinary `DOCUMENT` visibility. Kernel, export adapters
and Python: unchanged.

## Required design/specification

First deliverable of the phase, before product code:
`geocedg/specs/ui/authoring-memory.md` (proposed path) — the durable contract
(targets, slots and ownership, currentness predicate, state-transition matrix,
gestures, admissible types, cancellation and undo, the hidden angle and its
persistence, view coverage, invariants), from the reconciled design and the
author dispositions, status `PROPOSED` until the author approves it. No ADR is
expected (no seam change beyond the approved design); one is required if a
disposition introduces a new architectural seam.

## Geometric invariants and degeneracies

- Design §2 invariants.
- The created objects are those the unchanged upstream tools create for the
  same point and reference (algorithm class and inputs identical).
- A remembered reference is reused only while it is the identical current
  instance (design §3.2); undefined or hidden references follow the
  `DQ-F2-5` disposition; nothing is guessed from labels or positions.
- The hidden angle keeps its value, orientation (`AngleStyle`, clockwise
  handling) and dependencies.

## Compatibility and serialization

- Documents written by F2 contain no memory trace; a tool-created angle is
  saved with the ordinary `<show object="false">` and opens hidden in older
  GeoCeDG and Classic.
- Commands, scripts and documents written before F2 behave as before.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations are design §10: `T-F2-PAR-POINT-FIRST`,
`T-F2-PERP-POINT-FIRST`, `T-F2-REF-FIRST-REPLACES`, `T-F2-NO-MEMORY`,
`T-F2-SLOTS-INDEPENDENT`, `T-F2-EMPTY-SPACE`, `T-F2-2610-UNCHANGED`,
`T-F2-TYPES`, `T-F2-LIFECYCLE`, `T-F2-CANCEL`, `T-F2-PREVIEW-NO-CREATION`,
`T-F2-UNDO`, `T-F2-NO-SERIALIZATION`, `T-F2-COMMANDS-UNCHANGED`,
`T-F2-ANGLE-HIDDEN`, `T-F2-ANGLE-UNDO-REDO`, `T-F2-ANGLE-PERSISTENCE`,
`T-F2-ANGLE-USER-SHOW`, `T-F2-ANGLE-COMMAND-UNCHANGED`, `T-F2-VIEWS`,
`T-F2-CLASSIC-UNCHANGED`, the `T-F2-WL-*` set only if `DQ-F2-9` includes the
working-layer entry, and the author smoke `T-SMOKE` (not performed by the
agent). Gesture tests drive the real GV1 controller with press/release events;
undo assertions use the synchronous undo baseline and store listeners.

Commands (PowerShell; log directories fresh, under `artifacts\agent\`):

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F2 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F2 -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-F2` covers at least
`compile.desktop.semantic` and `junit.desktop.pre-g9b-r6-plus-f2.semantic`
(plus `workspace-profile.product` and the A-2 chooser test classes if
`DQ-F2-9` includes the working-layer entry). `PHASE` runs once on the frozen
clean commit and tree. No `INTEGRATION` or `FINAL` under `BOUNDED_PHASE`. A
run above the frozen class fails with `VERIFICATION_ESCALATION_REQUEST` and
waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`F2` on an exact base with the `DQ-F2` dispositions; it then authorizes local
implementation on a local branch, local commits, focused tests, the phase
registration and its official catalog updates, development `INFRA_UNIT` and
`STATIC` runs, the frozen acceptance run, and one frozen technical candidate
for author review and author smoke. It never authorizes self-approval, author
smoke by the agent, a run above the frozen class, or a change of the class; a
proven inability of the frozen class to cover the change is reported as a
reclassification proposal, never applied silently.

`F3`, `F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every
open observation and enhancement not named here
(`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` unless `DQ-F2-9` includes it). Author
approval is never created by technical verification.

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
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification; the Desktop, registry and guide deltas with their tests
  (and the profile delta only if `DQ-F2-9` includes the working-layer entry).
- A candidate report with the entry gate, report `K3`–`K13` re-established
  with every correction, the `DQ-F2` dispositions applied, test evidence, the
  exact commands, exit codes, report paths and verdicts, and an author-smoke
  checklist: Parallel with a line, then two further points (memory reused),
  then a new segment and a point (memory replaced); the same for Perpendicular
  while Parallel keeps its own reference; switch to another tool and back;
  Escape; empty-space click with a memory; click a second line right after a
  first (issue #2610 point on the line); undo after a creation (memory
  forgotten); delete and redefine the remembered line; New and Open; a vector
  and a linear function as references; Angle with Given Size from two points
  and from a segment, clockwise and counter-clockwise (angle hidden, rotated
  point visible), show it from the Algebra View, undo/redo, save and reopen;
  `Angle(B, A, 40°)` typed (visible); GV2 per `DQ-F2-10`; in EN and ES.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- a target needs a change in `source/shared/**` or in an inherited class, or
  an override of `parallel(Hits, boolean)`;
- the reference actually used cannot be read back from the created
  algorithm, or currentness cannot be established by exact-instance identity;
- release-time seeding changes the issue #2610 gesture, the preview, hit
  priority or the accepted types;
- the companion seam is not reached by the tool, or is reached by a command,
  script, API call, macro or reload;
- the memory would need a durable identity, label resolution, document
  metadata or serialization;
- the hidden angle would need a new XML element or attribute;
- the frozen class cannot cover a required change.
