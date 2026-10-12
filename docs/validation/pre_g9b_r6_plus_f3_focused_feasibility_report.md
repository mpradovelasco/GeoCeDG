# PRE-G9B-R6-plus-F3 — focused feasibility report (risk closeout)

- Activity: `PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction of
  2026-10-12; AUTHOR AUTHORIZED — FOCUSED CHARACTERIZATION, TECHNICAL RISK
  RESOLUTION AND DOCUMENTARY DESIGN RECONCILIATION ONLY)
- Status: **FOCUSED CHARACTERIZATION COMPLETE — DOCUMENTARY CANDIDATE —
  PENDING AUTHOR REVIEW**
- Base: `T_F3_RECONCILIATION` `0eb50311d55a099de70e3f222b885b31685ac0df`,
  tree `061e08f1a9d821cf642827afaf52d802629b940a` (STATIC
  `verification-090da84e071846e793c02b36f2207bce` `ACCEPTED / COMPLETE`)
- Ancestry: `e70718fe` (published `main`) → `480c9729` (`T_F3_PREP`) →
  `0eb50311` (`T_F3_RECONCILIATION`); none amended
- Historical evidence (unchanged): the
  [preparation characterization report](pre_g9b_r6_plus_f3_preparation_characterization_report.md),
  the [reconciliation feasibility report](pre_g9b_r6_plus_f3_reconciliation_feasibility_report.md)
  and the [conservative-scope assessment](pre_g9b_r6_plus_f3_conservative_scope_assessment.md)
- Companions: [risk closeout candidate](pre_g9b_r6_plus_f3_risk_closeout_candidate.md);
  [focused author-decision preparation record](pre_g9b_r6_plus_f3_focused_author_decision_preparation_record.md);
  [restricted insertion design candidate](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md);
  JSON mirror [`pre-g9b-r6-plus-f3-focused-risk-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-focused-risk-closeout.json)
- `selfApproved = false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`

Author directions recorded (2026-10-12; not approvals of a matrix, mechanism,
`D1` change or implementation): Restricted Native Insertion **R**; macro
objective **M1**; safe renaming **L1**, no overwrite; an **independent
insertion buffer**; **high priority for specialized GeoCeDG objects**; limited
**T0** style application; **U0** where reliable.

Evidence classes: **E** executed scratch probe of this activity (`RK-*`),
**S** source at the base, **H** earlier F3 probes, **I** inferred. Path
abbreviations follow the earlier reports (`CPD` = `desktop/util/CopyPasteD.java`,
`AGC` = `geocedg-desktop/AppGeoCeDG.java`).

## G0 — Entry gate (2026-10-12)

| Check | Result |
|---|---|
| live remote `main`, local `main`, `origin/main` | `e70718fec3612814144cd405d46f6c08864ceadd`, tree `26817bfc6a72b378e4babc1cd08efd0d734473c9` |
| `T_F3_PREP` | `480c972972e90514e88421194a792898c916834e`, tree `166caf6c7802c37db03060a2fd07cd71d28daa17`, parent `e70718fe` |
| `T_F3_RECONCILIATION` | `0eb50311d55a099de70e3f222b885b31685ac0df`, tree `061e08f1a9d821cf642827afaf52d802629b940a`, parent `480c9729` |
| reconciliation receipt | `verification-090da84e071846e793c02b36f2207bce`, `ACCEPTED / COMPLETE`, bound to that commit and tree |
| worktree | clean |
| product delta `e70718fe..0eb50311` under `source/`, `apps/`, `tools/`, `geocedg/specs/`, `AGENTS.md`, `CLAUDE.md` | none |
| F3 implementation | not authorized; no F3 product code |
| branch | `phase/pre-g9b-r6-plus-f3-focused-risk-closeout` (local), from `0eb50311` |

## G1 — Method

One scratch JUnit class `F3RiskProbeTest` (eleven methods) in the Desktop test
source set through the F3-PREP Gradle init script, never tracked; extensions
`G9U1TestApp.Lifecycle`, `EmptyUnitPreferences`, `DesktopLogger`; isolated
preferences. It drives a **scratch prototype** of the restricted insertion that
uses only existing services and never calls `AppD.insertFile`:

1. archive preflight (`DocumentArchivePreflight.validate`) and a hidden helper
   from `AppGeoCeDG.newAppForTemplateOrInsertFile()`;
2. admission on the helper: empty source, label conflicts, macros (M0, M1
   with the **transitive** macro closure, M1ID with identical-definition reuse),
   collisions, identity records;
3. optional **dry run** on a scratch host loaded with the target XML;
4. commit through a dedicated `CopyPasteD` instance (`GeoCeDGCopyPaste`, or a
   scratch subclass for fault injection or for not re-importing reused macros);
5. **strict verification**: count, pairwise object class **and** parent
   algorithm class, internal closure;
6. on failure, compensation by `UndoManager.restoreCurrentUndoInfo()` and
   explicit removal of transferred macros;
7. on success, one undo point only when the inherited paste did not already
   store one;
8. release of the helper's frame, dock-manager AWT listener and key dispatcher.

**Fault injection** (executed, not inferred): a proxy `View` attached to the
target kernel that throws on the n-th `add`; scratch `CopyPasteD` subclasses
that drop the copied macros (`CLEAR_MACROS`), append an element with an unknown
command (`BROKEN_ELEMENT`), rename one copied command (`CorruptCopyPaste`) or
throw from `onPasteCompleted` after label handling (`THROW_AFTER_RENAME`);
orchestration-stage faults during verification and before the undo store.

**Non-mutation certificate** compared, separately, the serializable state
(`getXML()` hash, construction listing with layers and dependencies, macros,
units, drawing scale, working and hidden layers, file, saved flag, undo
history, window buffer content and provenance, recent list, persistent ids) and
the runtime state (exact instance per label, F2 reference memory, selection,
IsoABorder export link, helper registrations).

## G2 — Gate R1: atomicity and rollback

### Executed fault matrix (`RK-atomicity`, `RK-identity-fault`, `RK-round5`)

| Case | Boundary | Payload | Outcome | Serializable state | Runtime state |
|---|---|---|---|---|---|
| A0 | before any mutation (M0 admission) | macros | REJECTED | unchanged | unchanged (9/9 instances) |
| A6d | dry run detects an unknown command | identity | REJECTED before commit | unchanged | unchanged |
| A1, A2 | view fault at add 1 / add 6 | identity (Locus V2) | inherited rollback | XML byte-identical | **9/9 instances replaced**; F2 memory cleared; selection cleared; saved → false |
| I-fault-add1, -add5 | view fault | identity, larger | inherited rollback | XML byte-identical | same as A1 |
| A6 | unknown command in the fragment | identity | inherited rollback | XML byte-identical | same as A1 |
| A9 | as A2, **no** compensation | identity | inherited rollback alone | XML byte-identical | same as A1 |
| A5 | exception after label handling | identity | compensation | XML byte-identical | same as A1, **plus an undo entry left by the paste's own `storeAddGeo`** |
| A7, A8 | fault during verification / before the undo store | identity | compensation | XML byte-identical | same as A5 |
| A4, A10, V3 | view fault at add 1 | **no identity** | **fault absorbed; insertion completes** | complete construction | the faulting view missed one notification |
| V1 (lax) | command renamed in the fragment | no identity | **silent degradation**: `m` (Midpoint) inserted as a free point; count and closure pass | — | — |
| V1 (strict) | same | no identity | FAILED by the pairwise algorithm check → compensation | XML byte-identical | instances replaced, F2 memory cleared |
| V2 / A3 | copied macros dropped | no identity | **silent degradation**: macro output `N` inserted free; strict check fails → compensation | XML byte-identical | as V1 strict |
| V7 | macro transferred, then strict failure | no identity | compensation **with** macro cleanup | XML byte-identical; macros restored | as V1 strict |
| V7 | same, **without** macro cleanup | no identity | compensation | XML byte-identical | **macro `MyMid` leaked** — invisible to XML equality |
| X-iso-link | view fault after a target IsoABorder link | identity | inherited rollback | — | **export link lost** (`lost=true`) |

### Mechanism comparison

| Mechanism | Guaranteed (E) | Not guaranteed |
|---|---|---|
| **A** — preflight plus compensating `restoreCurrentUndoInfo()` | the serializable target state is restored byte-identically; transferred macros removed only by explicit cleanup | runtime identity: every target object is rebuilt; exact-instance session associations (F2 memory, IsoABorder export link) are lost; selection lost; saved flag false; an undo entry stored by an identity-bearing paste survives (A5, A7, A8); non-drawable numbers and lists are reassigned to the highest used layer on reload |
| **B** — staged dry run on a scratch host, then commit | every deterministic content failure observed (unknown command, missing macro, renamed command) is caught before any target mutation (A6d; V1/V2 failures are produced by the same deterministic path) | failures that occur only in the real target (environmental faults such as a throwing view, resource exhaustion) |
| **C** — restricted admissibility | rejection before mutation for every excluded class (A0, macro collisions, mixed identity + macros, empty, unreadable) | — |

Ordinary undo is **not** equivalent to a transaction rollback: it rebuilds the
construction from XML; the probe shows what that loses.

### Result

```text
ATOMICITY = PROVEN FOR DEFINED SUBSET
SUBSET    = every rejection by admission (C) and every deterministic content
            failure detected by a strict dry run (B): no serializable and no
            runtime change of the target (except the recent-file publication of
            the helper load, removed by DQ-F3-10 H-1)
RESIDUAL  = a failure that occurs only during the real commit after a passing dry
            run: compensating rollback (A) restores the serializable state but
            rebuilds the target and resets session associations; one inherited
            undo entry survives for identity-bearing payloads (OPEN)
```

Further executed facts: a successful insertion keeps all target instances
(9/9), the F2 memory (`HELD`), the pre-existing persistent ids and an existing
IsoABorder export link; it clears the selection (inherited paste) and marks the
document unsaved. The inherited identity-bearing paste stores its own undo
point; a non-identity paste does not. With a store only when the paste did not
store, Undo restores byte-identical XML in both cases (S1, S2). The UndoManagerD
store runs on a thread, so its failure is not synchronously observable (S:
`desktop/main/undo/UndoManagerD.java:58-69`).

## G3 — Gate R2: macros (M1)

| Case | Result (E) |
|---|---|
| simple macro, empty target | transferred **once**; `N` bound to the transferred macro, value preserved; Undo removes the objects **but not the macro**; Redo and reopen correct |
| nested macro (`Outer` uses `MyMid`), empty target | only `Outer` transferred; `N2`, `K` correct; reopen correct |
| nested, target has a **different** `MyMid`, inner closure checked | REJECTED before mutation |
| nested, inner closure **not** checked (V4) | **`N2` = (1.33, 3) instead of (2, 1)**: `Outer`'s inner call binds to the target's `MyMid` — the transitive closure check is required |
| different definition, same name | REJECTED before mutation |
| mixed identity records + macros | inherited refusal (`CPD:588-602`) inserts **nothing**, silently; the document is marked unsaved (`M-mixed`) |
| raw macro XML of two independently built identical macros | equal only if built identically in fresh hosts; different internal labels → different XML |
| curated library `CirclebyD`, source and target | product digest (`GeoCeDGUserToolLibrary.definitionDigest`, S `:1166-1181`) **equal**; raw XML equal |
| identical reuse, inherited buffer (M1ID) | inserts, but transfers a renamed duplicate (`CirclebyD1`, `CirclebyD2`, …) |
| identical reuse, product-style buffer that drops reused macros (V5) | three repeated insertions: **no duplicate**, every `N` bound to `MyMid`, reopen correct |
| unused source macros | not transferred (direct-parent collection, S `InternalClipboard.java:335-361`) |
| macro transferred, then failure (V7) | cleanup removes it; without cleanup it leaks |
| malformed macro definition | not executed separately; such archives fail preflight or helper load (H, `RP-failures`) → NOT ESTABLISHED as a distinct case |

```text
M1 = READY WITH EXPLICIT RESTRICTIONS
  transitive closure check; reject different definitions; identical reuse only
  by the product definition digest with the transfer of the reused macro
  suppressed; reject identity-bearing payloads that need macros; transferred
  macros are not removed by Undo (author decision DQ-F3-22); explicit macro
  cleanup on any failure
```

## G4 — Gate R3: safe renaming (L1)

`RK-labels` (two identical runs, three rounds each) and the specialized runs:
conflicts on `a`, `A`, `B`, `f`, `sg` and more; dependent chains `f → g →
P=Point(g)`, multiple references (`Polygon`, `Circle`), a boolean show
condition, a dynamic text, a macro object (M1), a linear dimension, Locus V2
objects (`RK-specialized`) and IsoABorder (`RK-dynamic`). Results (E): closure
internal in every round; target instances and dependents untouched; renamed
labels **deterministic** across runs; inserted condition bound to the inserted
boolean; reopen without differences; Undo back to the identical XML. Repeated
rounds under strict M1 are rejected by the identical-macro collision; under
M1ID with reuse (V5) the second and third rounds insert `N_{1}`, `N_{2}`
correctly. Not remapped (S/H): labels inside static strings and JavaScript;
GGBScript is rewritten (H).

```text
L1 = CLOSED WITH EXPLICIT SCOPE RESTRICTION (no scripts; no label-bearing static
     strings relied upon)
```

## G5 — Gate R4: specialized GeoCeDG objects

Source (`RK-specialized`, units m): Locus V2 `L`, `T = Translate(L, v)`,
`P = Point(T, branch, 0.5)`, `M = LocusLength(T)`, `Rt = Intersect(T, x = 0)`,
token-selected intersection point `X`, Spline V2 `S` with `lenS`,
`AlignedDimension`, `LinearDimension`; and (`RK-dynamic`, `RK-round5`)
`IsoABorder`. Targets: empty and label-conflicting `mm` documents.

| Type | Construction | Dynamic dependencies | Identity | Undo/redo | Reopen | Readiness |
|---|---|---|---|---|---|---|
| Locus V2 (incl. `Translate`, semantic point, metric, intersection result, token-selected point) | 34/34, closure internal | identical to the source after the same edits (`dy`, `A`) | **new** persistent ids; tokens and ledger remapped consistently; target ids kept | back to identical XML; redo | ids equal, 0 semantic differences | SUPPORTED WITH CONDITIONS |
| Spline V2 | as above | `lenS` identical after the same edit | new ids | as above | as above | SUPPORTED WITH CONDITIONS |
| LinearDimension | 5 outputs, closure internal | identical after edits (`RK-dynamic`) | not identity-bearing | as above | as above | SUPPORTED WITH CONDITIONS |
| AlignedDimension | 5 outputs | identical after edits | not identity-bearing | as above | as above | SUPPORTED WITH CONDITIONS |
| IsoABorder | 3 outputs, closure internal | identical after `Ps` and `n` edits (A3 → A4) | not identity-bearing; **no export link** is created for the inserted sheet; a target link survives a successful insertion, is lost by rollback or Undo (spec §11.3) | Undo identical XML | correct | SUPPORTED WITH CONDITIONS |
| free 3D point | inserts (H) | — | — | — | — | REJECTED (F3 excludes 3D import) |
| projection bindings, spatial objects (non-GEO identity kinds) | no product creation route (S, `g9-spatial-projection-semantics.md:600-604`) | — | — | — | — | REJECTED (out of scope) |

Diagnoses: the earlier `AlignedDimension` failure was a **fixture label
collision** (Spline V2's promoted degree had taken `d`); with `ad` it inserts.
The `revision=` difference is the in-memory per-instance semantic revision
counter of `GeoLocusV2` (S `AlgoLocusV2.java:93-101`, not serialized), restarted
by every reload of the source itself (`X-revision-on-plain-reopen`): benign
lifecycle behavior. Dimension and sheet texts are re-presented in the target
unit (`2.24 m` → `2.24 mm`; numbers unchanged): the target-document
interpretation of `unit-system.md` §14, covered by the `D1` notice; no scaling.
Locally admissible intersection roots and global completeness are kept as the
kernel recomputes them in the target (`completeness=NOT_ESTABLISHED` preserved).
Runtime feature flags do not gate paste (H `RF-1`): admission must check that
the target allows Locus V2 creation.

## G6 — Gate R5: independent buffer and `D1`

`RK-buffer`, `RK-success` (E): two windows with their own buffers (`mm`, `cm`);
an insertion of an `m` document into window 1 shows the `D1` notice; **both
windows' buffers and provenances are unchanged**; the JVM-global
`InternalClipboard` containers are unchanged; window 1's private
`duplicateLabels` stays `null`; Ctrl+V pastes `R_{1}` in window 1 and `W_{1}` in
window 2; a failed insertion leaves provenance and notice unchanged; an
unspecified source shows no notice; a macro-dependent insertion behaves alike.
`CopyPasteD` has no static fields; `InternalClipboard`'s static containers are
not touched by the `CopyPasteD` route (E/S). The dedicated instance is dropped
after the operation; nothing else retains it (S).

```text
F3R-CLIPBOARD-D1 = CLOSED — EVIDENCE SUPPORTED (amendment of DQ-D1-7 required)
```

## G7 — Gate R6: T0

`RK-t0` (E): with the whitelist (colour, line style, line thickness, point
style, point size, fill opacity) taken from the template's default of the
object's default type: points changed colour and size only; geometry, labels,
layers, captions, offsets, fixed flags, visibility, conditions, colour
functions, units, defaults and labeling style unchanged; one undo point, Undo
byte-identical, Redo reapplies, document unsaved; selection scope changes only
the selected object. **Native-dimension outputs were restyled** (line thickness
2 → 5) when not excluded: their style is algorithm- or preference-initialized
per-object style that "the document style wins" (S
`native-dimensions.md` §4.2, §9.1) — they must be excluded. Locus V2 family and
IsoABorder outputs (algorithm-set style, spec §3) are excluded by the same rule.
Failed template loads: T0 never runs without a successful preflight and helper
load (H `RP-failures`); the inherited path applied factory defaults (H).

## G8 — Gate R7: architecture

All restrictions above are implementable with **U0**: a GeoCeDG orchestration
class; a GeoCeDG-owned insertion buffer subclass of `CopyPasteD` (protected
`copiedXML` and `copiedMacros`, public `insertFrom` and `pasteFromXML`) that
records provenance through the existing `D1` path and drops reused macros; the
product macro digest; the helper overrides of the F3-PREP design. No inherited
or shared change is necessary. Optional U1 corrections are listed in the risk
candidate.

## G9 — Probe record

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.risk-atomicity.txt` | G2 A0–A10 | `75be00b7d8c6a08b29d0bb284f867246b8759b0c44ff4d6a4d36b1f5b754f93e` |
| `f3-probe.risk-identity-fault.txt` | G2 identity faults | `850772500ea291ad9a42c2641d68ff3d92271d89a117ffaa89076e6c97d9fd2b` |
| `f3-probe.risk-success.txt` | G2 success certificate, undo | `d1a21e0fc6a38566be2b8450df96c2fc59911ba8ac0a0ecd4ceb1d3d037fb521` |
| `f3-probe.risk-macros.txt` | G3 | `cb137df976e64e8382283bbc0763bc83cf53994a0044ed10eb707ea0a64ec483` |
| `f3-probe.risk-curated.txt` | G3 curated library | `048b9a3a0470e2aa6db09494e6a1b149397ec0032a67b5b867e59b54037dda41` |
| `f3-probe.risk-labels.txt` | G4 | `cfef0fc1821c8f4f9a603ff89e45df4be5a5304ca0b0e7be121d63e0eae9ebd0` |
| `f3-probe.risk-specialized.txt` | G5 | `550803766d68b1f216d6a01608cd79dee98d6699c420b9d41162ae8fc09b642b` |
| `f3-probe.risk-buffer.txt` | G6 | `84c6a25208849aaf8311ee67fc4dadad2c47860cec588ad5ce8f9bed63f7d291` |
| `f3-probe.risk-t0.txt` | G7 | `51253a82b0524c7b4379915e26b1ebfd7019e255f400529c4d41d86ac42a98b5` |
| `f3-probe.risk-round5.txt` | G2 V1–V3, V7; G3 V4, V5; G5 IsoABorder | `ad5a9b95f6c0e2b43b1c41dfc70b501f561b8778092770415626116a91b2606f` |
| `f3-probe.risk-dynamic.txt` | G5 dimensions and sheet under edits | `a4fc4cbba671eb0016234b051f7922882961395c4f9daa634b157a8b52df3d63` |
| probe source (run 6) | `F3RiskProbeTest.java` | `cf7a765f06672ab6ac51c10ca364a99f63bc093af556270e4e0167900c2d0c8d` |
| probe source (run 7) | + V7 | `0308f9e5feffbf33f98ce6ddef2ec2b56885b7c89362f9fa3b2ccf95b31000c9` |
| probe source (run 8) | + dynamic test | `4c58cf24b6f110674135b164a94ec53ad93afb745920a1d6a9849aca4e3e352a` |

Runs: run 1 hung on a modal command-error dialog raised by a fixture (thread
dump; JVM stopped); run 2 four failures (fixture labels beginning `mm…` do not
evaluate); run 3 one failure (a source mixing identity records and macros
inserted nothing — recorded as a finding); run 4 nine tests passed; run 6 the
full class, ten tests, exit 0 (all outputs except `round5` and `dynamic`); run 7
`r7Round5` with V7, exit 0; run 8 `r8DynamicDimensionsAndSheet`, exit 0. Then
`:desktop:desktop:compileTestJava` without the init script (exit 0), probe
test-result XML and compile-stash copies removed, `git status` clean.
`NOT ESTABLISHED — ENVIRONMENT`: none; the asynchronous undo-store failure is a
design limitation, not an environment one.
