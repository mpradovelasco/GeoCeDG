# G9A1 spatial identity and persistence foundation report

**Status: IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**

**Author approved:** `false`

**Execution date:** 2026-08-17

**Entry branch:** `feature/g9a1-spatial-identity-persistence-foundation`

**Approved entry commit:** `001f7920a1154b09a22b54c190f7bc5f94b48e90`

This is the implementation-candidate report for the explicitly authorized
G9A1 phase. The candidate source, tests, fixtures, verifier, inventory, and
machine-readable evidence now exist. The focused first run, deterministic
rerun, and final composed candidate authority all completed successfully. This
is not an approval, does not close G9A1, and authorizes neither G9A2 nor any
later G9 phase.

## 1. Authority and entry disposition

The canonical task authority is
`.github/prompts/tasks/g9a1-spatial-identity-persistence-foundation.prompt.md`.
Its canonical-LF SHA-256 was verified as:

```text
50c665a399b7b6290b8dcf86cc2326bb78202d85d7b52b130fd8ebf2980127e1
```

The entry audit established all mandatory gates before source implementation:

| Gate | Entry evidence | Disposition |
|---|---|---|
| Repository and branch | repository root and exact feature branch inspected from disk | satisfied |
| Approved baseline | `HEAD`, local `main`, `origin/main`, and the feature ref were `001f7920a1154b09a22b54c190f7bc5f94b48e90`; branch divergence was `0/0` | satisfied |
| Clean entry state | no tracked or untracked worktree changes before execution | satisfied |
| Canonical prompt | canonical-LF hash matched the required value above; checked-out CRLF bytes were not rewritten | satisfied |
| G9P authority | final G9P disposition and author approval present | satisfied |
| Spatial specification | `geocedg/specs/spatial/g9-spatial-projection-semantics.md` is normative and author approved | satisfied |
| Accepted decisions | ADR 0010 and ADR 0011 are Accepted | satisfied |
| Prior semantic gates | required G6, G7, and G8 authorities are green | satisfied |
| G9O1 operational predecessor | PASS — AUTHOR APPROVED; recorded as operational advice, not an A1 semantic dependency | satisfied |
| Later-phase authority | no G9A2-or-later implementation authorization exists | confirmed |

The full entry authority was then executed without `-SkipBuild`:

```powershell
.\tools\agent\verify.ps1 -KeepBuildOutputs `
    -LogDirectory artifacts\g9a1\entry\composed
```

| Result | Value |
|---|---|
| Exit code | `0` |
| Result | PASS |
| Log root | `artifacts/g9a1/entry/composed/` |
| Evidence kind | real repository build/test execution; generated logs are evidence, not source authority |

The retained log tree contains the workstation and shared/desktop compilation
records plus the G5, G6/G6R, G7A/G7B, G8A/G8B/G8C/G8C1/G8C2, G9P, and G9O1
sub-gate logs produced by the composed authority.

## 2. Authorized implementation boundary

G9A1 owns a construction-scoped, shared document/kernel substrate for durable,
typed identity and persistence. The candidate may add IDs and inert records for
participating geos, spatial objects, projection frames, projection systems,
diagram maps, frame relations, and bindings. It may make the smallest necessary
changes to XML, copy, undo, macro instantiation, delete/clear, and explicit
redefine lifecycle seams.

The affected architectural layers are expected to be:

- a GeoCeDG-owned shared identity package containing typed IDs, inert records,
  registry transactions, compatibility SPI, and diagnostics;
- shared `Construction`/`GeoElement` ownership and lifecycle hooks;
- shared construction XML parsing/writing and explicit load-purpose plumbing;
- shared and desktop clipboard closure/remap integration;
- host algebra/redefine context propagation through every replacement route;
- shared-JRE tests and real XML fixtures;
- focused validation, upstream-modification inventory, evidence, and normative
  candidate records.

No projection frame, map, or relation is evaluated in A1. The phase must not
reconstruct/reproject geometry, publish a certificate or spatial payload, make
the 3D view authoritative, add a command or GUI, change Locus or DXF, infer a
legacy relation, introduce migration inference, or implement G9A2 behavior.

## 3. Candidate architecture contract

One `SpatialIdentityRegistry` belongs to one `Construction`; it is not global
frontend state and does not schedule geometric work. `AlgoElement` remains the
only construction dependency scheduler. Opaque kind-correct IDs are independent
of `ceID`, labels, coordinates, layers, Java instances, construction/XML order,
and output position. Ordinary legacy geos receive no ID unless an explicit
participation transaction assigns one.

The version-one records are deliberately inert. They retain typed identities,
references, provider/family/schema/version data, authority and binding roles,
stable output role/cardinality, definition/topology/source revisions,
provenance/copy lineage, and structured lifecycle diagnostics. They contain no
transform result, reconstructed geometry, cached certificate, or competing
dependency graph.

The chosen XML contract is an optional `geocedgId="geo:<token>"` attribute on a
participating full-construction or semantic-clipboard element and one flat
`<geocedgSpatial version="1">` construction section. Records are written in a
deterministic kind/ID order and all references are stable IDs. Style XML and
generic style-copy output exclude both identity attachments and semantic
records.

## 4. XML and load lifecycle

Loading is a two-stage atomic operation. The parser first registers typed
declarations and unresolved references without label lookup. It then validates
versions, syntax, cross-kind uniqueness, required fields, and closure before
publishing one complete registry graph. Supported but missing references remain
explicitly `BROKEN`; no reference is repaired from a label, coordinate, order,
layer, or visual coincidence.

Every parse has an explicit purpose and policy:

| Purpose | Required policy |
|---|---|
| `NATIVE_OR_UNDO_RESTORE` | restore exact serialized IDs and revisions; reject native collisions rather than remap |
| `CLIPBOARD_IMPORT` | require a whole semantic closure and remap every declaration/reference through one transaction-local table |
| `REDEFINE_REBUILD` | restore unaffected IDs and apply only the pre-authorized target decision |
| `ROLLBACK_RESTORE` | restore the exact prior snapshot without replaying redefine intent |
| `GENERIC_MERGE` | reject identity-bearing input when no explicit import policy exists |

Malformed or unsupported input must not partially mutate the registry. Legacy
XML without the GeoCeDG section loads unchanged and unassociated. A future
section is rejected with an explicit unsupported-version diagnostic; safe
opaque preservation is deferred to a separately reviewed lifecycle-hardening
path and is never inferred, activated, or silently downgraded in A1.

## 5. Copy, undo, macro, and deletion lifecycle

Copy expands a participating selection to its complete semantic component
before ordinary DAG predecessor collection. Shared and desktop clipboards must
use the same closure exporter. Every paste allocates one fresh typed-ID map,
rewrites all internal references atomically, and records copy lineage. Repeated
paste cannot reuse a prior mapping, and the desktop fast path cannot bypass the
semantic closure.

Until an action-undo payload can carry a complete identity closure and an
explicit restore purpose, participating mutations require full XML snapshot
undo. Reopen, undo, and redo restore exact IDs even though Java instances and
`ceID` values may differ.

Macro definitions are templates, not live document identities. Every
invocation freshly remaps its complete semantic closure through the host
template-to-instance geo map. An incomplete closure fails rather than retaining
a cross-construction reference.

Deleting a participating geo retires its ID and the owned/dependent inert
closure according to the transaction policy. Recreating the same label, type,
coordinate, or construction position receives a fresh identity. Clearing the
live construction clears its active registry graph but retains a bounded
Construction-instance issued-token ledger. Exact native/undo/redefine/rollback
loads may reactivate the same typed IDs; allocation and ordinary registration
cannot reuse them. Undo of deletion is exact snapshot restore, not heuristic
recreation.

## 6. Explicit redefine transaction

`changeGeoElement` must capture an immutable context from the explicit old
target before parsing and propagate it through evaluation and every
`Construction.replace` route. A registered provider inspects the actual result
and returns exactly one decision before destructive mutation:

- `RETAIN` only for the admitted single targeted stable output, with matching
  provider, semantic family, schema/version, authority, binding role, stable
  output role, cardinality, and a topology-preserving proposal;
- `FRESH` for an explicitly selected true replacement, retiring the old closure
  and transferring no binding implicitly; or
- `REJECT` for an incompatible, ambiguous, multi-output, topology-changing,
  missing-context, or missing-provider proposal.

Composite vector/rigid-polygon operations that would rewrite multiple targets
reject before creating helper algorithms when any rewritten target
participates. Multi-target continuity is not admitted by G9A1.

The same transaction boundary covers in-place `set`, soft redefine, new-instance
replacement, circular update, collected redefine, and XML rebuild. A rebuild
may use a serialization overlay for the already-decided ID, but resolution and
commit remain ID-based. A failed route restores a context-free prior snapshot.
Host `GeoClass`, command equality, labels, coordinates, indices, output ordinals,
and Java references are never sufficient compatibility evidence.

## 7. Diagnostics and forbidden-authority audit

Candidate diagnostics must expose deterministic per-kind counts for allocation,
exact restore, remap, collision, unresolved references, retirement, load/copy/
delete/redefine commit and rollback, `RETAIN`/`FRESH`/`REJECT`, definition and
topology revision changes, missing target context, and multi-output rejection.

The following authority-attempt/evaluation counters are hard-zero G9A1 gates:

- label, coordinate, construction-order, XML-position, layer, output-index, or
  Java-reference identity transfer;
- viewport, DPI, camera, renderer, or screen authority reads;
- projection, diagram-map, frame-relation, reconstruction, reprojection,
  certificate, or spatial-payload evaluation/publication; and
- any registry-owned scheduling or hidden recomputation.

## 8. Requirement-to-evidence matrix

The implementation column records the present source/test candidate and the
completed authoritative candidate executions. `PASS` here is a verification
result only; it is not an author-approval claim.

| Requirement | Planned evidence | Candidate result |
|---|---|---|
| Typed construction-scoped registry; no automatic legacy association | registry/unit tests plus legacy XML fixture | implemented; **PASS** |
| Typed ID syntax, bounded collision policy, cross-kind uniqueness | allocation/collision/malformed XML tests | implemented; **PASS** |
| Deterministic flat XML and forward-reference resolution | round-trip, reversed-order fixture, repeated serialization comparison | real host parser; **PASS** |
| Native/reopen/undo exact restoration | save/reload and undo/redo lifecycle tests | implemented; **PASS** |
| Broken/future/malformed compatibility behavior | real XML fixtures with explicit diagnostics and atomicity assertions | section/record versions and pre-parse snapshot restoration; **PASS** |
| Shared and desktop whole-closure copy/remap | system/map/relation/binding closure and repeated-paste tests | one atomic map per paste; **PASS** |
| Macro invocation allocates a fresh complete closure | macro template/instance lifecycle test | rejection cleanup included; **PASS** |
| Rename, recompute, temporary invalidity, and reordering preserve identity | lifecycle tests and counter snapshots | inert identity boundary; **PASS** |
| Delete/recreate receives fresh identity | deletion, recreation, tombstone, and undo restoration tests | implemented; **PASS** |
| Compatible redefine retains only by approved provider predicate | every-host-route redefine tests | general, soft, no-child, collected, XML-rebuild, parametric, attach/detach, and rejection routes; **PASS** |
| True replacement is fresh; incompatible/missing-context/multi-output paths reject atomically | replacement/rejection/rollback tests | `FRESH` additionally requires explicit replacement intent; **PASS** |
| No regression in upstream redefine behavior | unchanged `org.geogebra.common.kernel.commands.RedefineTest` | unchanged source; 55 tests; **PASS** |
| Forbidden G9A2 and heuristic-authority counters remain zero | focused diagnostics assertions and scope audit | all required counters zero; **PASS** |
| Source-boundary inventory is complete | `docs/upstream/modified-files.yml` validation | 54 productive + 7 test + 7 fixture paths registered; **PASS** |
| Deterministic focused rerun and clean generated-state handling | focused verifier executed twice from controlled state | both exit `0`; **PASS** |
| Composed authority remains green without `-SkipBuild` | final `tools/agent/verify.ps1` execution | exit `0`; **PASS** |

## 9. Candidate file and validation evidence

The machine-readable evidence contains the exact path sets. The grouped list
below is complete for the present candidate; the verifier compares it against
the entry baseline and rejects additions or omissions.

### Changed paths

- 37 added files under
  `source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/`.
- 17 minimally modified host lifecycle seams:
  `CopyPasteD.java`, `ConsElementXMLHandler.java`, `MyXMLHandler.java`,
  `MyXMLio.java`, `Construction.java`, `Kernel.java`, `Macro.java`,
  `AlgoDispatcher.java`, `AlgoMacro.java`, `AlgebraProcessor.java`,
  `EvalInfo.java`, `ParametricProcessor.java`, `GeoElement.java`,
  `PolygonFactory.java`, `UndoManager.java`,
  `UndoableDeletionExecutor.java`, and `InternalClipboard.java`.
- Seven focused test classes under
  `source/shared/common-jre/src/test/java/org/geocedg/common/spatial/`.
- Seven XML fixtures under
  `source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/`:
  `complete-forward-closure.xml`, `cross-kind-token.xml`, `duplicate-id.xml`,
  `future-version.xml`, `legacy-no-identities.xml`, `malformed-id.xml`, and
  `missing-reference.xml`.
- Eleven supporting paths: the two architecture records, roadmap, upstream
  inventory, evidence JSON and canonical-LF hash sidecar, this report,
  experimental feature manifest, historical G8A follow-on delegation, focused
  verifier, and composed verifier.

The exact 79-path partition is
`sourceBoundary.productivePaths`, `testPaths`, `fixturePaths`, and
`supportingPaths` in
`docs/validation/g9a1_spatial_identity_evidence.json`.

### Focused suites and counts

| Suite | Tests | Result | Purpose |
|---|---:|---|---|
| `G9A1SpatialIdentityIdTest` | 5 | **PASS** | typed syntax and kind separation |
| `G9A1SpatialIdentityLifecycleTest` | 6 | **PASS** | rename/recompute, delete, undo/redo, clipboard |
| `G9A1SpatialIdentityMacroTest` | 2 | **PASS** | fresh invocation closure and rejection cleanup |
| `G9A1SpatialIdentityRedefineHostTest` | 12 | **PASS** | real host redefine routes |
| `G9A1SpatialIdentityRegistryTest` | 15 | **PASS** | allocation, closure, collision, retirement, authority |
| `G9A1SpatialIdentityXmlTest` | 11 | **PASS** | real host XML, compatibility, atomicity |
| `G9A1SpatialRedefineTransactionTest` | 11 | **PASS** | provider decision and transaction invariants |
| **Focused G9A1 subtotal** | **62** | **PASS** | exact focused class set |
| Upstream `RedefineTest` | 55 | **PASS** | unchanged host redefine compatibility |
| **Combined** | **117** | **PASS** | 0 failures, 0 errors, 0 skipped |

### Validation commands

| Gate | Exact command | Exit code | Log/evidence path | Result |
|---|---|---:|---|---|
| Focused G9A1, first run | `.\tools\agent\verify-g9a1-spatial-identity.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9a1\candidate\focused` | `0` | `artifacts/g9a1/candidate/focused/` | **PASS** |
| Focused G9A1, deterministic rerun | `.\tools\agent\verify-g9a1-spatial-identity.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9a1\candidate\focused-rerun` | `0` | `artifacts/g9a1/candidate/focused-rerun/` | **PASS** |
| `git diff --check` / source-boundary audit | included by the focused and composed authorities | `0` | verifier output | **PASS** |
| Final composed authority, no `-SkipBuild` | `.\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9a1\candidate\composed-final` | `0` | `artifacts/g9a1/candidate/composed-final/` | **PASS** |
| Sealed evidence/static integrity | `.\tools\agent\verify-g9a1-spatial-identity.ps1 -SkipBuild -KeepBuildOutputs -LogDirectory artifacts\g9a1\candidate\sealed-static` | `0` | `artifacts/g9a1/candidate/sealed-static/` | **PASS** |

### Machine-readable evidence

```text
Evidence JSON: docs/validation/g9a1_spatial_identity_evidence.json
Evidence canonical-LF SHA-256: docs/validation/g9a1_spatial_identity_evidence.sha256
Evidence hash: e5191804969285779d3f31fc0117a819d3176274640aa6a5a5aa817c3be464c7
Candidate HEAD: 001f7920a1154b09a22b54c190f7bc5f94b48e90
Commit/tag/push: not required or authorized; none performed
Final worktree status: implementation candidate pending author review; baseline HEAD unchanged
```

## 10. Scope deviations, remaining risks, and author gate

**Scope deviations:** none. The focused and composed authorities confirmed the
exact 79-path candidate and all hard-zero scope counters.

**Remaining risks:** unknown future section/record versions are rejected
atomically rather than preserved opaquely. Participating add/paste/delete
operations intentionally select full XML snapshots until a separately reviewed
identity-aware action-undo format exists. Identity-bearing cross-application
paste combined with copied macros is rejected atomically pending a reviewed
combined import policy. Composite vector/rigid-polygon rewrites reject when a
participating target would require multi-target continuity. The records are
inert: all projection evaluation, reconstruction, certificate/payload
publication, public surfaces, migration policy, and G9A2-or-later behavior
remain deferred and unauthorized. The issued-token ledger is deliberately
scoped to the live `Construction` instance and may therefore retain bounded
historical tokens across document clears; this is stricter than per-file
uniqueness.

Two preliminary candidate composed attempts exposed compatibility assumptions
in historical verification, not source/test regressions: G8A was incorrectly
scoping its frozen authority against the live worktree, and the G8C family
required its approved G8 closeout status tokens to remain explicitly visible.
The G8A verifier now uses the frozen G8 anchor, and the roadmap distinguishes
the historical G8 snapshot from the current G9A1 candidate state. Focused
static smokes and the final no-`SkipBuild` composed authority passed after these
bounded verifier/document corrections.

Final disposition remains:

```text
G9A1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
authorApproved = false
G9A2 AND ALL LATER G9 PHASES = NOT AUTHORIZED
```

Only an explicit author review may replace this disposition with a closeout
decision. Passing focused or composed validation alone does not authorize the
next phase.
