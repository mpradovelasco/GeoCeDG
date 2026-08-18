# G9A3 spatial lifecycle and migration report

**Status:** IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW

**Author approved:** `false`

**Pass claimed:** `false`

**Entry branch:** `feature/g9a3-spatial-lifecycle-migration`

This report is the implementation-candidate record for the separately
authorized G9A3 phase. It must not be interpreted as G9A3 approval or as
authorization for G9B, G9C, G9U0, G9X1, G9U1, G9U2, DXF, productive G10 work or
any other later phase.

## 1. Authority and boundary

G9A3 is governed by:

- `.github/prompts/tasks/g9a3-spatial-lifecycle-migration.prompt.md`;
- `geocedg/specs/spatial/g9-spatial-projection-semantics.md`;
- Accepted ADR 0010 and ADR 0011;
- `docs/validation/g9_spatial_validation_and_benchmark_plan.md`;
- the author-approved G9A1 and G9A2 implementation state; and
- `docs/architecture/g9a3_spatial_lifecycle_migration_design.md`.

The branch entry is
`1efa338414cdbe76cbb913bbb45ea26c7108bba3`. The canonical prompt's
canonical-LF SHA-256 is
`f12d9f66eb4f2f9df8afe715f9f9039e8a45e8a377a08491e79252891f6f7651`.

The invoking author decision crosses only the prompt's pre-execution G9A3 gate.
The candidate remains limited to the existing POINT / `PROJECTION_DEFINED`
pilot and its identity, persistence, lifecycle and explicit-association seams.

## 2. Required semantic result

The implementation candidate establishes the following bounded semantics:

- one construction-confined prospective lifecycle transaction for record,
  attachment, resolution and normal-DAG changes;
- atomic binding, system, map and relation mutation;
- complete copy closure with fresh deterministic remapping, plus only the
  explicitly declared same-construction external-reference exception;
- exact undo/redo/reopen identity graphs and deterministic invalid recovery;
- explicit target-based compatible redefine across every admitted host route;
- provider-owned stable output-role continuity with exact cardinality and
  rollback for all ambiguous or unsupported groups;
- true replacement with fresh identity;
- legacy files remaining unassociated until a complete explicit typed request;
- strict malformed/future-version behavior with no silent repair; and
- native GeoCeDG/GeoCeDG-Classic preservation separated from the unsupported
  external-upstream boundary.

Invalid or underdetermined current geometry preserves durable identity and
publishes no stale spatial payload. Migration and recovery never infer identity
from label, coordinates, proximity, construction/XML order, output ordinal,
layer, view state or Java reference equality.

## 3. Validation architecture

The machine-readable scenario authority is
`docs/validation/g9a3_spatial_lifecycle_scenarios.json`. It currently defines
72 lifecycle scenarios partitioned across mutation, copy, redefine, snapshot,
XML, explicit migration, native compatibility and authority instrumentation.
The scenario and source-to-test mapping is frozen at exactly 72 G9A3 tests. The
focused selector also includes exactly 181 inherited G9A1/G9A2/upstream redefine
regressions, for an expected combined total of 253. Execution outcomes remain
separate evidence and are not inferred from these frozen source counts.

The compatibility corpus is
`docs/validation/g9a3_spatial_compatibility_corpus.json`, sealed together with
its exact fixture targets by
`docs/validation/g9a3_spatial_compatibility_corpus.sha256`. Its 13 exact entries
and canonical-LF/binary hashes are frozen. New corrupt/future
inputs are test-private. The existing G9A2 point model is reused without
resaving as the canonical native spatial document.

The exact candidate path partition, frozen test-class counts, approval
disposition, execution records, hard-zero requirements, scope audit and residual
risks are recorded in
`docs/validation/g9a3_spatial_lifecycle_evidence.json`, sealed by its
canonical-LF SHA-256 sidecar. The evidence remains candidate-only:
`authorApproved=false` and `passClaimed=false`.

The frozen inventory is exactly 81 paths: 35 modified and 46 new, partitioned as
46 productive paths, 12 test paths, two test-support paths, nine XML fixtures,
two corpus artifacts, five validation artifacts and five supporting
architecture/roadmap/inventory/verifier paths. All remain unstaged.

Every hostile operation must compare a canonical graph snapshot containing:

- sorted typed record IDs and exact serialized record fields;
- live references, resolution state and attachment presence;
- system/object certificate axes, revision tuple and payload presence;
- deterministic spatial-section serialization; and
- separate positive transaction counters and hard-zero authority counters.

## 4. Compatibility disposition

The detailed compatibility matrix is
`docs/validation/g9a3_spatial_compatibility_matrix.md`.

GeoCeDG Classic means the fork Classic process using `AppConfigDefault` and the
same shared kernel. Supported native records must parse, recompute, save and
reopen exactly there while public creation remains unavailable. It must never be
used as evidence for behavior of an external upstream distribution.

External upstream behavior is characterized as unsupported-open. The candidate
adds no converter, downgrade, hidden resave or inferred migration workaround.
The project-authored no-spatial loss-shape fixture is not represented as output
captured from an external runtime.

## 5. Focused and composed verification

The final focused authority completed successfully:

```powershell
.\tools\agent\verify-g9a3-spatial-lifecycle.ps1 -KeepBuildOutputs `
  -LogDirectory artifacts\g9a3\candidate\focused-final-green
```

It ran the G9A1, G9A2 and G9A3 spatial tests plus upstream
`org.geogebra.common.kernel.commands.RedefineTest`, relevant Checkstyle, exact
scenario/corpus/evidence/inventory checks and both Git whitespace checks. The
result was exit code 0 and `BUILD SUCCESSFUL`: 72 G9A3 tests, 181 inherited
regressions and 253 total, with zero failures, errors or skips. Main and test
Checkstyle were clean, and the verifier reported candidate-only PASS without an
author-approval claim.

The deterministic focused rerun also completed successfully:

```powershell
.\tools\agent\verify-g9a3-spatial-lifecycle.ps1 -KeepBuildOutputs `
  -LogDirectory artifacts\g9a3\candidate\focused-deterministic-green
```

It returned exit code 0 and `BUILD SUCCESSFUL` with the identical 72 / 181 / 253
test partition, zero failures, errors or skips, clean main and test Checkstyle,
and the same candidate-only verifier outcome.

The final composed authority ran without `-SkipBuild`:

```powershell
.\tools\agent\verify.ps1 -KeepBuildOutputs `
  -LogDirectory artifacts\g9a3\candidate\composed-final-pass
```

It returned exit code 0 with terminal outcome `All GeoCeDG verification gates
passed.` The nested G9A3 authority reported `BUILD SUCCESSFUL`, the same 72 / 181
/ 253 test partition, zero failures, errors or skips, clean main and test
Checkstyle, and a candidate-only outcome. This verification result does not
constitute author approval or a G9A3 `PASS` decision.

Two earlier attempts remain only as non-authoritative diagnostics. The first,
saved under `artifacts/g9a3/candidate/focused-final`, exited before tests because
of sandbox AppData/Kotlin/generated-dependency state and is classified as an
environment failure, not a product failure. The pre-clean attempt under
`artifacts/g9a3/candidate/focused-final-escalated` ran all 253 selected tests
clean but exited 1 at Checkstyle with 48 warnings. It is superseded by the two
clean final executions and is not final authority or a product-failure claim.

Two earlier composed attempts are also retained only as non-authoritative
diagnostics. `artifacts/g9a3/candidate/composed-final` exited 1 at the G9A2
generated-evidence check before G9A3 because `core.autocrlf=true` materialized
CRLF and caused false staleness; tracked blobs and current canonical-LF outputs
were exact, and both generators subsequently passed `--check` with zero cached
diff. `artifacts/g9a3/candidate/composed-final-green` exited 1 because of an
unexpected diagnostic `__pycache__` artifact. Neither is a product-failure
claim, and both are superseded by the clean composed final pass.

## 6. Historical and living status

G9A1 and G9A2 reports, evidence and tags remain frozen historical authority and
must not be rewritten to describe G9A3. The living roadmap records the separately
authorized G9A3 implementation candidate, but does not record `PASS` before a
separate author closeout. Its historical G9A1 snapshot and the G9A2 statement
that the G9A2 closeout itself did not authorize G9A3 remain unchanged.

Per the canonical prompt, only later author approval of G9A3 can close G9A. No
later phase becomes authorized automatically.

## 7. Current evidence state

```text
G9A3 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
authorApproved = false
passClaimed = false
G9A = NOT CLOSED — PENDING G9A3 AUTHOR REVIEW
G9B / G9C / G9U / G9X1 = NOT AUTHORIZED — NOT STARTED
G10 PRODUCTIVE IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
focused execution = PASSED — 253/253, Checkstyle clean
deterministic rerun = PASSED — 253/253, Checkstyle clean
composed execution without -SkipBuild = PASSED — all gates
```

No scope deviation is recorded. The user guide was reviewed and remains
unchanged: the candidate adds no command, GUI, public workflow or capability
enabled by default. Residual boundaries remain POINT-only coverage, binary64
residual evidence rather than certified exact bounds, terminal best-effort
announcements for the non-authoritative derived adapter, and an explicitly
unsupported external-upstream open boundary with no runtime-support claim.
