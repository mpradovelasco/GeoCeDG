# G9X1 extended DXF author-closeout report

- Status: **G9X1 = PASS — AUTHOR APPROVED**
- Self-approved: **no**
- Author-approved: **yes**
- PASS claimed: **yes**
- Review required: **no**
- Reconciled entry commit: `f528b2dcbe4a802d3dfdb334f842e39ec7f33015`
- Original frozen entry commit: `22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0`
- Original frozen canonical-LF evidence SHA-256:
  `472de35266111f994cf9fa21c180cfb048b671ccefa8d48d10481c68f2cd8cd3`
- Pre-author-review reconciled evidence SHA-256:
  `1b3f017bd9d3dd53037a0edf6de40866de1f9143ba386101878f6d2dbec0cea9`

```text
G9X1 = PASS — AUTHOR APPROVED
selfApproved = false
authorApproved = true
passClaimed = true
```

The author reviewed the reconciled implementation candidate and approved the
bounded G9X1 closeout on 21 August 2026. This is an author decision, not agent
self-approval.

## G9U0-R1 reconciliation

The original 46-path G9X1 candidate remains frozen in
`feature/g9x1-extended-dxf-curves` at the original entry above. This candidate
was replayed independently onto the author-approved G9U0-R1 corrected main at
`f528b2dcbe4a802d3dfdb334f842e39ec7f33015`.

The pre-replay classification was 42 G9X1-only paths, 9 R1-only paths and
4 overlapping paths. Three overlaps preserve both deltas explicitly: the
living roadmap, the upstream modified-file inventory and the composed verifier.
The fourth overlap,
`tools/agent/verify-g9u0-locus-v2-public-surface.ps1`, is byte-identical to
corrected main because the R1 version already subsumes and strengthens the
frozen candidate's descendant-HEAD promotion check. The truthful reconciled
inventory is therefore 45 paths rather than adding a redundant modification.

## Authority and placement

G9X1 follows the normative DXF fidelity specification, Accepted ADR 0014, the
G5 exact-export authority and the approved G6–G9U0 semantic source contracts.
The implementation boundary is a read-only external export service:

```text
authoritative CeDG geometry
    -> immutable export snapshot and preflight
    -> exact DXF entity or explicit export-only approximation
    -> deterministic DXF and conditional fidelity sidecar
```

The reverse direction is forbidden. No approximation becomes a `GeoElement`,
enters the construction dependency graph, changes `.ggb` persistence or feeds
DXF data back into CeDG geometry. The DXF writer encodes a validated neutral
model and does not solve geometry.

## Approved contract

Fidelity is explicit per semantic component as `EXACT`, `APPROXIMATE`,
`UNSUPPORTED` or `INVALID`. Approximate outcomes record the method, requested
and achieved tolerance, guarantee and deterministic work counts. Sample/chord
evidence may claim only `ESTIMATED_ERROR`; no certified global bound is inferred
from visual similarity.

Preflight completes before destination access or output serialization. It
reports exact, approximate, unsupported, invalid and hidden components, decides
whether a sidecar is mandatory and applies strict `partialOutput=false` by
default. A failure discovered by preflight writes nothing.

Every fidelity reduction requires a deterministic UTF-8 JSON sidecar bound to
the DXF SHA-256 and actual DXF handles. Wholly exact G5-compatible output may
remain a single DXF. Paired output uses same-directory temporary files,
validation, ordered promotion and rollback. This is a recoverable paired-write
protocol; it does not claim universal two-file atomicity.

Existing exact G5 mappings remain exact and byte-compatible. Locus V2 is read
through its semantic revision, branches, components and explicit domains.
Approximation stays inside the export representation and never reads render
samples, viewport bounds, zoom or DPI.

## Focused validation authority

The frozen scenario source defines 62 G9X1 tests:

| Group | Concern | Layer | Count |
|---|---|---:|---:|
| C | executable G5 corpus and exact compatibility | shared | 4 |
| P | preflight, fidelity and strict policy | shared | 10 |
| A | deterministic bounded approximation | shared | 14 |
| L | Locus V2 branches, gaps and non-mutation | shared | 12 |
| M | deterministic sidecar and handle mapping | Desktop | 10 |
| D | paired output, rollback and injected failures | Desktop | 8 |
| S | Desktop preflight presentation contract | Desktop | 4 |
| **Total** |  | **40 shared + 22 Desktop** | **62** |

The focused authority also retains the existing 5 shared + 5 Desktop G5
regression tests and four applicable Checkstyle tasks. The final pre-commit run
at `artifacts/g9x1/closeout/precommit/focused` passed all 62 G9X1 tests
(40 shared + 22 Desktop), with zero failures, errors or skipped tests. It also
passed all 10 retained G5 regressions (5 shared + 5 Desktop) and the shared,
shared-test, Desktop and Desktop-test Checkstyle tasks. The deterministic rerun
at `artifacts/g9x1/closeout/precommit/focused-deterministic` reproduced those exact
totals with zero failures, errors or skipped tests.

## Evidence state

The scenario source remains frozen at 40 shared + 22 Desktop = 62 cases, and
its recorded execution status is `PASSED`. The reconciled implementation
inventory remains 45 paths against corrected-main entry
`f528b2dcbe4a802d3dfdb334f842e39ec7f33015`. Closeout adds only the bounded
R1-verifier compatibility update needed for the living roadmap to record G9X1
PASS while preserving the frozen R1 report. The final closeout inventory is
therefore exactly 46 paths: 15 tracked modifications and 31 new paths,
classified as 28 productive, 7 tests, 4 validation, 4 documentation and
3 supporting paths. The evidence records
all 21 authority/scope hard-zero counters as zero. Focused validation, its
deterministic rerun, retained G5 regression, all four Checkstyle tasks, the
static verifier, `git diff --check` and `git diff --cached --check` passed.
The reconciled tree also passed G9U0-R1 6/6 (4 shared + 2 Desktop) and the
historical G9U0 authority 93/93 (81 shared + 12 Desktop), at
`artifacts/g9x1/closeout/precommit/r1-focused` and
`artifacts/g9x1/closeout/precommit/g9u0-historical`. The complete no-skip-build
`tools/agent/verify.ps1` authority passed with exit code 0 and the exact terminal
outcome `All GeoCeDG verification gates passed.`; its logs are at
`artifacts/g9x1/closeout/precommit/composed`. The final author-closeout
canonical-LF evidence SHA-256 is
`a53f350812ca466ff958ccf14b428ba5184d288aa042dcf9aa90db9a03a29fd8`;
the scenario-source canonical-LF SHA-256 remains
`9cd019d63f53d3d70c48e59be94a509f97300fa0bc1c0c9d65a858ad5742219e`.

These results establish only the approved G9X1 export boundary. They do not
authorize G9U1, G9B, G9C, G9U2 or productive G10 implementation.

## Retained risks and deferred scope

- Adaptive sample/chord evidence is estimated, not a certified Hausdorff bound.
- Coordinates retain the G5 unitless contract (`$INSUNITS=0`).
- Ordinary sources have construction-revision identity unless an owning phase
  supplies a durable identifier.
- Same-directory rollback cannot promise universal two-file atomicity.
- Implicit contouring, `SPLINE`, legacy `GeoLocus` sampling, physical units,
  DXF import, 3D export and partial-output UI remain deferred.

G9U1, G9B, G9C and productive G10 work are not authorized. G9U2 remains
blocked until global G9 closeout.

**NO LATER G9 OR G10 IMPLEMENTATION WAS EXECUTED.**
