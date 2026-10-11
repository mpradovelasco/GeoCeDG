# PRE-G9B-R6-plus-F2 — author-decision preparation record

- Activity: `PRE-G9B-R6-plus-F2-PREP` (author instruction of 2026-10-11)
- Status: **QUESTIONS PREPARED — ALL PENDING — NO DECISION RECORDED**
- Base: published `main` `24985f14c565b9ba6282ec80fa0cb7be20a2e459`, tree
  `1c683ebb3e303b7692915feae3417cf51e4cce22`
- Evidence: [characterization report](pre_g9b_r6_plus_f2_preparation_characterization_report.md)
  (`K0`–`K16`); design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md);
  contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-authoring-memory.prompt.md)
- Prior author decisions on F2: the author-fixed targets only (mini-track plan
  §2; author instruction of 2026-10-11): independent session-only references
  with the point-first / reference-first / no-memory rules; Angle with Given
  Size hidden by default with the rotated point visible and the command
  unchanged. `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` names `F2` as *preferred*
  owner, which is not an inclusion decision.

This record formulates questions and recommendations. It decides nothing. A
recommendation is the agent's proposal; only an explicit author instruction
disposes of a question. `selfApproved = false`.

Each entry gives: current behavior, evidence, alternatives, recommendation,
consequences, tests (IDs of design §10) and effect on the verification class.

## DQ-F2-1 — Persistence of the memory across tool changes

**Current behavior.** No memory. A pending reference is discarded by Escape or
any tool change (report `K4` T08); `GeoCeDGSimilarityTools` resets its session
field on every `setMode` (GCEC:603-605).

**Evidence.** Report `K3`, `K7`; design §3.1, §3.3.

| Alternative | Consequence |
|---|---|
| **P1 — survive tool changes for the life of the document session (recommended)** | the user can draw with another tool and return to Parallel with the same reference; slot cleared only by staleness (§3.2) and document boundaries; the slot is not reset in `setMode` |
| P2 — cleared when leaving the tool | simplest mental model; returning to the tool asks for the reference again; reuse only within uninterrupted series |
| P3 — survive tool changes, cleared by an explicit Escape in the tool | adds a distinction between Escape and tool change that the host does not make today (Escape = `setMoveMode`) |

**Recommendation.** P1: it matches "session-only reference" and gives the
memory its value across short detours.

**Tests.** `T-F2-SLOTS-INDEPENDENT`, `T-F2-CANCEL`. **Class effect.** None.

## DQ-F2-2 — Independent slots and replacement

**Current behavior.** None remembered; both tools share the inherited
selection lists.

| Alternative | Consequence |
|---|---|
| **S1 — two independent slots, replaced on successful creation (recommended)** | Parallel never feeds Perpendicular; a reference chosen first replaces the slot only when a line is created; an abandoned reference-first gesture keeps the previous memory |
| S2 — two slots, replaced on reference selection | an Escape after selecting a new reference has already replaced the memory |
| S3 — one shared slot | contradicts the author-fixed "independent" target |

**Recommendation.** S1; the read-back from `input[1]` makes "the reference
actually used" the only source of a slot value (design §4).

**Tests.** `T-F2-REF-FIRST-REPLACES`, `T-F2-SLOTS-INDEPENDENT`.
**Class effect.** None.

## DQ-F2-3 — First-use gestures and the missing reference

**Current behavior.** Point first or reference first both work; empty space
creates a free point that waits for a reference (T06); with no reference the
point waits (T03); issue #2610 creates a point on a second clicked path (T04,
T15).

| Alternative | Consequence |
|---|---|
| **G1 — point-first includes empty space (recommended)** | with a usable memory, an empty-space click creates the free point and the line in one gesture, one undo point (as T06 today) |
| G2 — memory only for existing points | empty space creates a free point that waits for a reference even with a memory; less surprising when the user wants a bare point, but asymmetric |
| G3 — a click on another line with a memory means "point on that line, through the remembered reference" | conflicts with the author-fixed "reference first replaces"; rejected |

In every alternative a click on a reference is reference-first, the missing or
stale memory falls back to today's flow ("request the reference"), and #2610
is unchanged because seeding happens at release (report `K6` I1).

**Recommendation.** G1.

**Tests.** `T-F2-EMPTY-SPACE`, `T-F2-NO-MEMORY`, `T-F2-2610-UNCHANGED`.
**Class effect.** None.

## DQ-F2-4 — Admissible reference types and hit priority

**Current behavior.** Report `K5`: lines, segments, rays, polygon edges, 2D
vectors and linear functions for both tools; Parallel also 3D vectors;
non-linear functions are selectable but yield no labelled output.

| Alternative | Consequence |
|---|---|
| **R1 — store only accepted references that produced a labelled 2D output (recommended)** | line, segment, ray, polygon edge, 2D vector, linear function; never non-linear functions, 3D objects or failed creations; hit priority untouched (upstream `parallel`/`orthogonal` decide) |
| R2 — store whatever the tool accepted | a remembered non-linear function would silently produce nothing on every reuse; 3D references in a 2D authoring memory |
| R3 — lines and segments only | narrower than today's tool; vectors and functions would work only reference-first |

**Recommendation.** R1 — no broadening, no narrowing of the tool.

**Tests.** `T-F2-TYPES`, `T-F2-PAR-POINT-FIRST`, `T-F2-PERP-POINT-FIRST`.
**Class effect.** None.

## DQ-F2-5 — Identity, currentness and reset events

**Current behavior and evidence.** Report `K8`, `K9`: after undo, redo, Open
and New a stale instance still answers true to `isInConstructionList` and
`isLabelSet`; exact-instance membership (MB) and the exact-instance label test
(LK) reject every stale instance and accept rename, recompute and macro
outputs; compatible redefinitions replace the object; undefined keeps the
instance; client `reset()` fires on undo, redo, Open and New, but redefinitions
and deletions arrive as other events or none.

| Sub-question | Alternatives | Recommendation |
|---|---|---|
| a. currentness test | MB only; LK only; MB and LK | **MB** (GeoCeDG precedent, no false negatives); LK optional as a second guard, never as resolution |
| b. undefined reference | reuse (creates an undefined line); suspend while undefined; clear | **suspend**: keep the slot, do not reuse, request a reference; reusable when it recovers |
| c. hidden reference or hidden layer | reuse; suspend while not visible in GV1; clear | **suspend** (the user cannot see what would be reused) |
| d. clearing | lazy check at use only; lazy plus eager at document boundaries (`AppGeoCeDG` overrides); lazy plus a client `EventListener` | **lazy plus eager at document boundaries** (existing precedent; no new listener) |
| e. undo/redo | accept that undo/redo forget the memory; re-resolve by label | **accept**: re-resolution by label, durable IDs or metadata are forbidden |

**Consequences.** After any undo or redo the next point-first click asks for a
reference; this is the visible cost of the identity gate.

**Tests.** `T-F2-LIFECYCLE`, `T-F2-UNDO`. **Class effect.** None; a durable
identity would be `GLOBAL_IMPACT` and is outside F2.

## DQ-F2-6 — Cancellation, preview, success and undo

**Current behavior.** Report `K3`, `K4`: Escape discards the pending selection;
`DrawLine` previews a selected line or function before the point; one undo
point per creation; creation branches have no preview guard.

| Alternative | Consequence |
|---|---|
| **C1 — release-time seeding, no preview of the remembered reference, no indicator (recommended)** | no #2610, toggle or preview-creation hazard; one undo point; memory changes never store undo |
| C2 — pre-select the memory at tool entry to get the `DrawLine` preview | needs press-stage #2610 suppression and a mouse-move guard; the remembered reference appears selected, so a click on it toggles it; wider interaction surface |
| C3 — C1 plus a presentation-only highlight of the remembered reference in GV1 (F1-style overlay) | discoverable; adds a rendering surface and its tests |

**Recommendation.** C1; C3 can follow as a separate enhancement if the smoke
shows the memory is hard to discover.

**Tests.** `T-F2-CANCEL`, `T-F2-PREVIEW-NO-CREATION`, `T-F2-UNDO`.
**Class effect.** C1 none; C2 or C3 enlarge the bounded surface.

## DQ-F2-7 — Exact tool-only hook for the hidden angle

**Current behavior.** The angle is visible from `setDrawableNoSlider()` on the
shared path; `doAngleFixed` stores undo right after `createAngle` (report
`K10`).

| Alternative | Consequence |
|---|---|
| **H1 — GeoCeDG companion: `GCEC.newCompanion()` returns a subclass of `EuclidianControllerFor3DCompanion` overriding `createAngle(p1, p2, a, clockWise)` (recommended)** | tool path only (the 4-argument method has no other non-test caller); hidden before the undo store; both routes (two points, segment); no inherited change; GV1 only unless `DQ-F2-10` extends it |
| H2 — GeoCeDG `DialogManagerD` subclass through `dialogManagerFactory` | every view; undo stored before the dialog callback, so `createAngleFixed`/`doAngleFixed` and the dialog would have to be re-implemented (≈40 inherited lines duplicated) |
| H3 — hide after `doAngleFixed` returns | stored undo state keeps a visible angle; redo would re-show it; rejected (`ER-F2-6`) |

**Recommendation.** H1.

**Tests.** `T-F2-ANGLE-HIDDEN`, `T-F2-ANGLE-UNDO-REDO`,
`T-F2-ANGLE-COMMAND-UNCHANGED`, `T-F2-CLASSIC-UNCHANGED`.
**Class effect.** None for H1; H2 widens the duplicated inherited surface.

## DQ-F2-8 — Visibility, persistence and user recovery of the angle

**Current behavior.** Angle Euclidian-visible, algebra-visible, label visible;
saved as `<show object="true" label="true"/>`; a hidden angle reopens hidden and
can be shown again (probe).

| Alternative | Consequence |
|---|---|
| **V1 — hide only in the Graphics Views; keep label, algebra visibility and definition (recommended)** | the user finds it in the Algebra View and shows it with the ordinary controls; ordinary persisted `<show object="false">`; no new serialization |
| V2 — also hide from the Algebra View | harder to recover (needs auxiliary-object display); still ordinary properties |
| V3 — keep visible but make it auxiliary | changes the object class of display rather than visibility; not the author-fixed target |

**Recommendation.** V1. The rotated point stays exactly as today (visible).

**Tests.** `T-F2-ANGLE-PERSISTENCE`, `T-F2-ANGLE-USER-SHOW`.
**Class effect.** None.

## DQ-F2-9 — Ownership of the working-layer direct entry

**Current behavior.** List-only chooser with 100 values; validated authority
`setWorkingLayer` rejects out-of-domain values (report `K12`).

| Alternative | Consequence |
|---|---|
| I — include in F2 as a separate work item F2-WL | honors the "preferred owner F2" note; adds a Desktop dialog change, EN/ES profile texts (the `workspace-profile.product` node), two rewritten A-2 tests and one new test to the F2 phase; independent code path, so separately testable |
| **K — keep it independent, as a later bounded micro-phase after F2 (recommended)** | F2 stays a pure controller-orchestration change with no profile or A-2 test edits; the enhancement keeps its state `ENHANCEMENT — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED` and gets its own small preparation |
| D — defer to `G` or later | the enhancement waits for the track closeout reconciliation |

Common to I and K: reject (never clamp) out-of-domain entries, keep the list,
one authoritative state, A-2 contracts unchanged.

**Recommendation.** K, because nothing in targets A and B shares its code,
tests or risks; I is fully viable if the author prefers one phase.

**Tests.** If I: `T-F2-WL-ENTRY`, `T-F2-WL-REJECT`, `T-F2-WL-CANCEL`,
`T-F2-WL-HIDDEN-LAYER`, `T-F2-WL-SYNC`, plus the two rewritten A-2 tests.
**Class effect.** I keeps `BOUNDED_PHASE` but adds the profile node and the A-2
test classes to the phase selection.

## DQ-F2-10 — Architectural scope and verification class

**Current facts.** Report `K11`: the GeoCeDG controller drives GV1 only; GV2
runs `EuclidianControllerFor3DD`; Classic never constructs the GeoCeDG
controller; no inherited change is needed for GV1.

| Alternative | Consequence |
|---|---|
| **A1 — GV1 only, Desktop GeoCeDG-owned code only, `BOUNDED_PHASE` with one registered `PHASE` (recommended)** | consistent with F1 (`DQ-F1-2`); GV2 keeps upstream behavior, documented as a limitation; files: GCEC, one memory helper, one companion class, one test class, the phase registration, `modified-files.yml`, guides EN/ES, the durable specification |
| A2 — GV1 and GV2 | a GeoCeDG GV2 controller subclass and a `GuiManagerGeoCeDG.newEuclidianView` override; GV2 changes controller class; GV2 regression tests; still Desktop-only, likely `BOUNDED_PHASE` with a wider selection, or `INTEGRATED_PHASE` if GV2 regressions reach other suites |
| A3 — A1 for the memory, GV1+GV2 for the hidden angle only | inconsistent coverage between the two targets |

**Recommendation.** A1, frozen at the implementation readiness gate. Any need
for an inherited-class or shared change stops the work (design §12).

**Tests.** `T-F2-VIEWS`, `T-F2-CLASSIC-UNCHANGED`. **Class effect.** As listed.

## Summary

| Id | Recommendation | State |
|---|---|---|
| `DQ-F2-1` | P1 — survive tool changes | PENDING |
| `DQ-F2-2` | S1 — independent slots, replaced on success | PENDING |
| `DQ-F2-3` | G1 — point-first includes empty space; #2610 unchanged | PENDING |
| `DQ-F2-4` | R1 — accepted references with a labelled 2D output | PENDING |
| `DQ-F2-5` | MB identity; suspend undefined or hidden; lazy + document-boundary clearing; undo forgets | PENDING |
| `DQ-F2-6` | C1 — release-time seeding, no preview, no indicator | PENDING |
| `DQ-F2-7` | H1 — GeoCeDG companion `createAngle` override | PENDING |
| `DQ-F2-8` | V1 — hidden in Graphics Views only, ordinary persistence | PENDING |
| `DQ-F2-9` | K — working-layer entry independent, later | PENDING |
| `DQ-F2-10` | A1 — GV1 only, `BOUNDED_PHASE` | PENDING |
