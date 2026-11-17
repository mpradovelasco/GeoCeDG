# GeoCeDG 1.2.0 — P1 correction / P2 scientific gate

Author scope: one corrective P1 unit and one diagnostic-only P2 unit. Base
0623085a3e5ff063b9a1a87d944699fbd3cd7f9d, tree
9fabb3182457e846d4fa7c84b0e31bc00e73768a. Protected S, B0, prior budgets and
all previous evidence remain unchanged. No PHASE/INTEGRATION/FINAL/PACKAGING,
inventory changes, new functionality, optimizations or publication are authorized.
P2 stops at author review and changes no scientific test or semantic contract.

## P1 design before code

Classification: causal debugging / implementation, shared legacy-locus sampling
and view-cache consumer. Source of truth: Kernel bounds and AlgoLocusND per-view
cache, not rendered geometry as V2 metric authority. Kernel C correctly retains
xmax at array growth and must remain unchanged.

The unchanged N09 test reads legacy Perimeter as a sampled chord sum (public
surface legacy boundary and upstream impact section7). Its RED on the base is
round[Perimeter[loc]]=0 instead of399996. This historical value is a compatibility
oracle, not certified length of an unbounded V2 locus.

Hypothesis established statically: AlgoLocusND.updateScreenBorders combines a
state-mutating call with changed || updateScreenBorders(i). Once the first view
changes, later visible views are not updated. Their cached bounds/connection
thresholds remain stale/zero. A 2D AlgoLocus visible also in3D can consequently
alter adaptive legacy sampling and report zero chord length. Perimeter sums
stored point distances regardless of LINE_TO; RED/GREEN establishes the oracle
restoration, not a LINE_TO filtering claim. C exposes this consumer
bug by preserving nonzero maxima; reverting C would mask it and break R3 again.

Invariant: every visible available view is refreshed once during this loop;
changed is the OR of every return value. Preserve geometry, sample budgets,
tolerances, event/compute policy, public commands, FILE/XML, metric guarantees
and all C/R10 guards. Evaluate the update unconditionally before accumulating OR.
Small coherent set: AlgoLocusND one expression plus change marker; a new direct
multi-view regression test; the upstream modification record; this design.
No Perimeter-specific branch or oracle edit.

Validation: unchanged N09; direct actual AlgoLocus cache tests with visibility
masks EV1+EV2, EV1+EV3 and allthree (initial/update/no-change, hidden-slot stability);
adjacent legacy Perimeter/Length/Locus checks; C shared cache-growth/MEMORY tests;
original five R3 scenarios and R10 consumers. Independent reviewer checks patch,
contracts and saved RED/GREEN evidence. A successful focus is not global acceptance.

Bootstrap impact: NO CHANGE — Java/Gradle/Windows prerequisites and invocation
contracts unchanged. GUIDE_IMPACT: NONE — restores existing legacy behavior,
adds no public workflow. Verification infrastructure impact: new regression tests
only; selection/catalog regeneration remains outside this authorization. Required
future level: FINAL for the integrated kernel candidate, explicitly not run here.

## P2 planned diagnostic protocol

Read ADR0037/0038, existence/components, conditioned estimation, hybrid transfer,
cycle semantics, PRE-G9B-CU closeouts and1.2.0 amendments. Compare identical eight
scenarios in B0(4fc0343a), S(0ae033ff) and successful P1 (otherwise base0623085a).
Use fresh sparse no-checkout diagnostic clones with CLAUDE exclusions verified
before materialization, or independently fingerprinted existing frozen runtimes.
Never modify protected sources, historical outputs or original cone.

Record preconditions, result/typed cause, normative approval/date, first relevant
B0-to-S change and classification per case. Baseline absence is NOT_APPLICABLE
with justification, never simulated via baseline product changes. Group by proved
root mechanism, propose minimum future product/test correction and regression
scope; use NOT_ESTABLISHED plus a resolving experiment when necessary. No P2
semantic/test/oracle correction is authorized. Deliver P2-GATE for author decision.
