# G9U0 public Locus V2 implementation and migration report

- Status: **G9U0 = PASS — AUTHOR APPROVED**
- Author approval claimed: **yes**
- PASS claimed: **yes**
- Productive source implemented: **yes, as an approved experimental surface**
- Tests executed: **yes — 93/93 twice, with zero failures/errors/skips**

## Entry authority

| Item | Value |
|---|---|
| Entry commit | `d4de0e480b0a6439c940a0f6e0cfde51c5e56bd2` |
| G9A3/G9A | `PASS — AUTHOR APPROVED` |
| Branch | `feature/g9u0-locus-v2-public-surface` |
| Prompt LF SHA-256 | `160dae2e7dd56fc51fa8910ee9ddecceb2cffa0a3594a655393de264f29cbdfe` |
| Public spec LF SHA-256 | `5fd65d1f0ce629b063afb196b946157b9ee9d6d3b778ac9c158aa26d886bdb72` |
| ADR 0013 LF SHA-256 | `a67352f064c3c9a89adc94cbe4ad025577e440fb9b9d24bace4634d68e52220c` |
| Validation matrix LF SHA-256 | `73e8b13f86f92a4a0987869d63b60c5204f90de673afe45d11ab220c7e5cf9c9` |

## Author-approved G9U0 result

The validation package fixes 93 required scenarios: 81 shared-kernel and 12
desktop/frontend. Each scenario maps bidirectionally to exactly one `@Test`
method. The source package now contains the selected public commands, typed
providers, reconstructible evaluators, rich result adapters, durable semantic
positions and exact-token ledger, together with the experimental frontend.
The evidence inventory is frozen at 114 exact candidate paths: 78 productive,
10 tests, 1 test-support path, 7 fixtures, 0 models, 2 corpus paths,
6 validation paths and 10 supporting paths. Focused and deterministic execution
both passed 93/93 scenarios, and the composed authority passed without
`-SkipBuild`. Those execution directories retain the original implementation-
candidate provenance; the author has now reviewed that evidence and approved
G9U0 as PASS.

The approved G6–G8 evidence remains historical. The composed verifier changes
the old G6 no-public-surface expectation only in the presence of this dedicated
phase-aware G9U0 authority; it does not weaken the frozen semantic gates.

Rich completeness rows that productive G8C1 cannot soundly establish remain
test-private: I03/I04/I08/I09 use the approved G8 analytic fixtures/factories.
Public AlgebraProcessor scenarios preserve `NOT_ESTABLISHED` Option B and never
manufacture `COMPLETE` through expression-DAG or binary64 inference.

## Approved experimental API disposition

The author-approved G9U0 surface keeps `Locus` unchanged and selects `LocusV2[Q,P]`,
`LocusV2[Q,s,D]` and `LocusV2[Q,t,s,D]`. `D` is the explicit typed domain list
`{periodic,{a,b,includeA,includeB},...}`. It adds rich `LocusLength`, guarded
standard `Length`, general `Intersect`, `Point[L,branchKey,u]` semantic points
and `Intersect[R,token]` exact-token points. The accepted mapped-scalar spelling
is an experimental, default-off API, not a stable/default API.

## Migration and compatibility boundary

There is no automatic migration from legacy locus objects. Old files remain
legacy. Native V2 objects will be reconstructed only from versioned typed
inputs and durable IDs. Unknown versions, broken references and unsupported
external-upstream open remain explicit failures; no sampled, list, label or
coordinate downgrade is authorized.

The frozen 13-entry corpus contains six immutable legacy controls, one
declarative public-provider/query coverage manifest, five hostile/future
declarations and one ordinary external-upstream boundary control. The coverage
manifest is not native GeoGebra construction XML. Its text hash and the hostile
fixture hashes are frozen with the candidate source. Native save/reopen and
hostile rejection evidence was produced by focused execution against the
implemented command/XML grammar. This is GeoCeDG shared-kernel/host evidence;
no execution in an external upstream GeoGebra distribution is claimed.

## Verification state

| Gate | State |
|---|---|
| static source/evidence/inventory verifier (`-SkipBuild`) | PASS |
| 93 focused scenarios | PASS — 93/93; 81 shared + 12 desktop; 0 failures/errors/skips |
| deterministic focused rerun | PASS — exact same totals and zero outcomes |
| composed without `-SkipBuild` | PASS — exit 0; `ALL_GEOCEDG_VERIFICATION_GATES_PASSED` |
| Checkstyle | PASS — shared main/test and desktop main clean in both focused runs |
| exact path inventory | FROZEN — 114 paths; 61 modified, 53 new |
| author review | PASS — AUTHOR APPROVED |

Saved execution evidence:

- focused: `artifacts/g9u0/candidate/focused-final-pass-escalated`;
- deterministic rerun:
  `artifacts/g9u0/candidate/focused-deterministic-pass-escalated`;
- composed: `artifacts/g9u0/candidate/composed-final-pass`.

## User-guide review

The user guide was updated because G9U0 has an observable
experimental command/tool surface. It records the explicit opt-in, selected
syntax, rich-result and Option-B behavior, supported provider/target boundary,
GeoCeDG Classic preservation policy and external-upstream warning. Author
approval closes G9U0 but does not describe the surface as stable or default-on.

## Durable identity disposition

The G9U0 implementation reuses the G9A lifecycle registry through construction-defined
`GeoIdentityRecord` entries with the explicit `NOT_APPLICABLE` projection role.
It does not invent a Locus-local label/coordinate ID store or pretend that a
Locus is a spatial object. The focused and composed gates passed atomic
publication, copy-source provenance, redefine, undo and reopen regressions.
Those original results retain their candidate provenance and now support the
author-approved G9U0 PASS decision.

## Residual and deferred boundaries

- A public root token is retained only when the explicit continuation relation,
  provider/target contracts and exact semantic preimage address remain current.
  A moving canonical parameter receives a new incarnation because G9U0 has no
  approved cross-revision homotopy/interval certificate. The parameter remains
  revision evidence and is never encoded into the opaque token. This
  conservative reselection boundary avoids coordinate/proximity/order inference.
- Verified roots whose continuation identity is not established have only
  revision-local internal handles. They are neither persisted nor accepted by
  `Intersect[R,token]`, and the inspector exposes no such handle as a semantic
  token.
- The productive single-target adapter establishes local Option-B admissibility
  but does not claim global completeness or analytic overlap enumeration. Public
  locus-pair results remain rich-only unless exhaustive rectangle isolation and
  uniqueness evidence is present; no pair exact-token point is manufactured.
- The experimental GeoCeDG action uses the owned SVG and localized text fallback;
  it is not installed in the Classic/default toolbar. Raster toolbar variants are
  deferred to the separately authorized workspace phase if that phase requires
  them.

## Scope

No G9X1, G9U1, G9B, G9C, G9U2 or productive G10 behavior is implemented or
authorized by this report.

**NO LATER G9 OR G10 IMPLEMENTATION WAS EXECUTED.**
