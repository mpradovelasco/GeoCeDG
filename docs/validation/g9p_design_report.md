# G9P integrated design and characterization report

| Field | Result |
|---|---|
| Date | 2026-08-16 |
| Branch/head at G9P/R1 entry | `feature/g9-spatial-semantics-design` / `667e1cfa6f368c693118ed2444159f1aa641d2fe` |
| G8 entry authority | `geocedg-g8-pass` -> `e7810171179825a03b22d8c6eba28c672f468281`; **PASS — AUTHOR APPROVED** |
| G9P-R1 | **PASS — AUTHOR APPROVED** |
| G9P | **PASS — AUTHOR APPROVED** |
| Productive G9 | **NOT STARTED** |
| Specifications / ADRs | six **NORMATIVE / AUTHOR APPROVED** / ADR 0010–0015 **Accepted** |
| G9O1 | **AUTHORIZED / NOT STARTED** |

## 1. Scope and method

G9P was a planning, audit, documentation, schema/configuration, and verification
task. It inspected current source and tests, the four immutable author GGBs,
the author screenshot/note/prompt, `Templatev7`, the 2022 spatial-association
proof of concept, current guides/roadmap, and the G5–G8 executable authorities.
No productive spatial, public command, GUI workspace, DXF approximation,
persistence, or bundle generator was implemented. G9P-R1 subsequently refined
only projection-system/diagram semantics, source-backed redefine identity,
one-dimensional public Locus generators, dependency topology and their
document/prompt/verification propagation.

The [integrated plan](../architecture/g9p_integrated_plan.md) consolidates the
architecture and sequence; the author-approved
[final decision package](g9p_author_decisions.md) keeps every deferred
implementation choice explicit.

## 2. Repository and entry gate

At entry the worktree was clean. Local `main` equalled the G8 peeled commit;
`origin/main` unexpectedly pointed to the G9P reference-input head and is
recorded without mutation. The branch tracked
`origin/feature/g9-spatial-semantics-design` at `0/0` divergence. Its seven
commits/files beyond G8 were exactly the supplied G9P inputs.

The G8 annotated tag object
`fed1bfbeea77a48acce285429b397eda77054df1` is present, peels to
`e7810171179825a03b22d8c6eba28c672f468281`, and is an ancestor of the G9P head.

The initial composed run failed in the restricted Windows environment when the
Gradle/Kotlin daemon attempted user-profile access. Managed escalation was used
before classifying any defect. The escalated run reproduced operational,
workstation, legacy, G5, all G6, G7A/G7B, and G8A/G8B/G8C design/G8C1/G8C2
functional gates. It then stopped in baseline `git diff --check` on three
trailing spaces already present in the immutable author note. The bytes were not
edited. A path-specific `.gitattributes` exception now preserves that evidence
while exact raw hashes remain enforced.

Historical G7/G8 evidence manifests are no longer evaluated against mutable
living guides. A shared byte-safe helper verifies the annotated G8 anchor,
requires HEAD ancestry, requires each current historical manifest to match its
frozen canonical-LF blob, and validates all referenced blobs at the frozen G8
commit. Current G9P files are validated separately. No historical manifest was
rewritten.

## 3. Reference-input characterization

The [machine manifest](../references/cedg/models/g9p/g9p-reference-inputs.json)
records sizes, hashes, ZIP entries, XML/layout, macros, scripts, command/object
counts, dependency-depth lower bounds, workflows, workarounds, native G6–G8
replacement candidates, G9 dependencies, and model-local content. The
[workflow audit](../references/cedg/models/g9p/g9p-reference-workflow-audit.md)
interprets those facts without treating the files as normative behavior.

All four G9P GGBs and `Templatev7` are Classic 5-format archives with 22
members, the same 24 named macros, 16 shared icon resources, and 19 top-level
toolbar groups (12 broad Classic and seven custom). Their document layouts use
Algebra + Graphics, bottom input, contextual toolbar help, layer-based sheet/
geometry organization, and saved document perspectives. Loading a file applies
that document state; it does not mutate the GeoCeDG profile manifest.

Across the four models, the dominant commands are Intersect (209), Line (112),
Segment (66), Circle (45), Point (35), Ray (22), Rotate (20), Locus (16),
Tangent (13), and Mirror (13). Sixteen legacy sampled loci occur, but no direct
locus intersection consumes them; procedures construct indirect intersections.
The embedded `postLocus`, `listLength`, and `listLength12` macros are not invoked
by these models. Model-specific scripts/macros remain evidence or laboratory
content, not stable toolbar candidates.

## 4. Spatial result

The normative [spatial specification](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md)
uses typed durable spatial/frame/binding IDs and a construction-scoped registry;
it never reuses labels, coordinates, order, XML position, revision, or transient
`ceID`. Authority is hybrid by explicit roles but acyclic: exactly one edit
authority exists per object/revision, with projection-defined point as the first
pilot and all other projections/3D views derived.

R1 separates intrinsic projection coordinates from the common CeDG diagram:

```text
x --pi_i--> q_i --delta_i--> p_i
```

The durable `ProjectionSystem` proposal owns diagram maps, frame relations,
hinges/change-of-plane provenance, orientation and a system certificate. A
defining diagram geo is pulled back through `delta_i^-1` before type-specific
intrinsic sufficiency; intrinsic and diagram reprojection residuals are both
reported. A coherent common-diagram gauge change cannot change sufficiency,
and the viewport transform remains outside every semantic revision.

Source characterization also disproved a blanket “ordinary redefine means new
identity” rule. Current host paths range from in-place `set`, identical/soft
recompute and no-child instance replacement to a full XML construction rebuild.
R1 therefore preserves a durable ID only for ordinary recomputation or an
explicit target-based, provider/type/schema/role-compatible redefine
transaction. True replacement, incompatible redefine, delete/recreate and copy
receive fresh IDs; undo/reopen restore serialized IDs even though Java instances
may change. Labels, coordinates, indices and XML position never transfer ID.

Sufficiency remains type- and configuration-specific constructive
reconstruction plus reprojection, rank/nondegeneration, correspondence,
guarantee, projection-system and consistency evidence. Status axes remain
separate; failure publishes no stale spatial value. G9A stays split into A1
identity/system persistence, A2 system evaluation + point pilot, and A3 hostile
lifecycle/redefine/migration. G9B covers staged primitive schemas independently
of GUI completion; G9C stops at the constructive projective boundary needed by
CeDG rather than a CAD feature tree.

## 5. Public surface and workspaces

Current G6–G8 functionality is internal, nonpersistent, and default-off.
`CmdLocus`, `CmdLength`, and `CmdIntersect` remain legacy/public; the internal
V2 factory receives an injected Java evaluator and V2/rich geos do not yet have
complete XML/copy/set behavior. G9U0 therefore requires a reconstructible
dependency-slice evaluator and persistence before visible creation.

The refined surface keeps legacy `Locus` unchanged and replaces a slider/segment
type list with a typed one-dimensional semantic generator. One family provides
scalar state `u -> t(u)` from one explicit driving coordinate/domain; another
provides a semantic point on a segment, circle, circular arc or an explicit
Locus V2 branch/component. Durable preimage address is separate from
revision/component/currentness evidence, so self-intersection, repeated
traversal, disconnected branches and periodic seams do not collapse by
coordinate. `L1 -> point -> L2` is initial scope, with normal-DAG cycle rejection
and evaluation-session reentry only as defense in depth.

Rich metric and intersection results remain authoritative. A standard
`Length[GeoLocusV2]` scalar must be exposed only as a guarded child/reuser when
the rich result is scalar-admissible; legacy `Length[GeoLocus]` remains unchanged.
Token-selected point materialization stays separate from rich intersection
calculation. G9P does not freeze mapped-scalar command spelling: G9U0 must
inspect actual GeoGebra overload conventions and present its selected public
surface for author review. GeoCeDG Classic must preserve/recompute native
V2/rich objects and identities; external upstream open is unsupported when the
distribution does not know the persisted types, and silent downgrade is
forbidden.

The normative profile schema-v2 design makes the application profile the single source
for named workspaces, view/dock layout, bottom input, toolbar help, actions,
maturity/feature gates, icons and localization. `CeDG Construction` exposes
approved native actions in task-oriented groups. `CeDG Dihedral Procedures` is
designed but blocked until global G9 author approval.

## 6. DXF result

The normative [DXF specification](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
retains every exact G5 mapping and classifies each source component independently
as exact, approximate, unsupported, or invalid. The conservative non-native
baseline is deterministic semantic-domain adaptive `LWPOLYLINE`, with gaps and
constructive multiplicity preserved, count-based work limits, and estimated vs
certified error reported honestly.

Approximation remains an ephemeral export DTO: it never becomes a GeoElement,
construction step, `.ggb` object, render cache, or viewport-dependent result.
A deterministic JSON sidecar with source/revision/component provenance, actual
handles, fidelity, domains, tolerance/guarantee, work, warnings, omissions and
the DXF hash is mandatory whenever an operation reduces fidelity; it is optional
for wholly exact output. Partial output is strict-reject by default and any
future partial option requires explicit intent, warning and sidecar. `SPLINE`
and implicit contouring are deferred to separate evidence-backed gates.

## 7. Documentation and bundles

The user guide is restored to current observable G8 behavior and routes deep
mathematics/development/agent workflow to dedicated guides. The normative
documentation-maintenance contract assigns document roles and requires an
explicit guide-impact/traceability gate.

The normative bundle package contains schema and profiles only. The authorized,
not-started G9O1 generator
will enumerate the Git index, apply ownership precedence, exclude generated/
untracked/restricted content by default, preserve raw and canonical hashes,
record baseline/current provenance, reject dirty trees by default, use fixed
ordering/archive metadata, enforce path/size/token budgets, and chunk at
semantic file/class/section boundaries. No `tools/knowledge` implementation or
generated bundle was created by G9P.

## 8. Dependency topology and recommended schedule

```text
                                      +--> G9B --> G9C -----------+
                                      |                            |
G9A1 --> G9A2 --> G9A3 ---------------+                            +--> G9 closeout --> G9U2
                                      |                            |
                                      +--> G9U0 -------+           |
                                                       +--> G9U1 --+
G5 + G6-G8 internal authority -----------> G9X1 ------+
```

G9O1 is recommended first and contributes to global operational closeout, but
is not a semantic dependency of G9A1. U0 requires the completed A3 lifecycle.
X1 has no hard U0 dependency because it can consume internal semantic snapshots
and disclose identity scope; U0-before-X1 remains the recommended integration
order. U1 integrates both product actions. G9B depends on A3, never U1.

The low-conflict recommended schedule remains `O1; A1; A2; A3; U0; X1; U1;
B; C; closeout; U2`; semicolons are scheduling preferences, not semantic
arrows. The kernel and product tracks may proceed independently after A3 under
explicit file ownership.
The global closeout item is an author/verifier review action, not an
implementation phase, so it has no separate productive prompt and may add no
new semantics.

## 9. Durable artifact index

- Spatial: normative spec, Accepted ADRs 0010/0011, projection-system/diagram,
  semantic/sufficiency/persistence architectures, source-backed redefine
  characterization, validation/counter plan, and scientific traceability.
- Public/frontend: reference manifest/audit, normative workspace and Locus
  contracts, Accepted ADRs 0012/0013, architectures, and combined validation matrix.
- DXF: normative spec, Accepted ADR 0014, architecture, and validation/benchmark plan.
- Documentation/operations: normative maintenance and bundle contracts,
  schema/profiles, Accepted ADR 0015, two architectures,
  mathematical/developer/agent guides, and current
  cross-capability traceability.
- Governance: living roadmap, integrated plan, author decisions, this report,
  11 canonical prompt records, prompt catalog/hashes, design evidence, integrity
  manifest, historical-evidence helper, focused G9P verifier, and composed
  verifier integration.

The exact current file set and canonical-LF hashes are recorded in
`geocedg/validation/g9p/g9p-evidence.sha256`. Reference binaries retain separate
raw-byte hashes in their manifest.

## 10. Verification contract and generated logs

Closeout runs:

```powershell
.\tools\agent\verify-operational.ps1
.\tools\agent\verify-g9p-design.ps1 -LogDirectory artifacts\g9p\focused
.\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9p\composed
git diff --check
git diff --cached --check
```

The composed command deliberately has no `-SkipBuild`. Generated logs remain
ignored evidence and are not inserted into this self-hashed package after the
final run; exact exit codes and log paths are reported in the task handoff.
The focused gate checks immutable reference hashes/archive structure, normative
statuses, prompt hashes/headings, JSON/schema/config parsing, Markdown links,
all 306 frozen G7/G8 hash entries, current integrity, scope, PowerShell parsing,
and absence of productive/generated changes.

## 11. Blockers and decisions

No technical contradiction prevents the approved architecture. G9O1 is
authorized and not started; all other productive work remains blocked on its
own explicit phase authorization and entry gate. G9U2 additionally requires
global `G9 PASS — AUTHOR APPROVED`.

```text
G8 = PASS — AUTHOR APPROVED
G9P-R1 = PASS — AUTHOR APPROVED
G9P = PASS — AUTHOR APPROVED
G9 SPECIFICATIONS = NORMATIVE / AUTHOR APPROVED
ADR 0010–0015 = ACCEPTED
G9A / G9B / G9C = DESIGNED — NOT AUTHORIZED
G9O1 = AUTHORIZED — NOT STARTED
G9U0 / G9X1 / G9U1 = DESIGNED — NOT AUTHORIZED
G9U2 = DESIGNED — BLOCKED ON THE APPROVED G9 GATE
G9 PRODUCTIVE IMPLEMENTATION = NOT STARTED
```
