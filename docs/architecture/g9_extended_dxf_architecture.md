# G9X1 extended DXF architecture

**Status: G9X1 = PASS — AUTHOR APPROVED. The approved implementation remains
experimental, default-off and strictly outside geometric authority.**

The author-closeout evidence and frozen focused scenario authority live in
`geocedg/validation/export/g9x1/`; the execution report is
`docs/validation/g9x1_extended_dxf_implementation_candidate_report.md`.

## Placement

G9X1 remains an export-service concern. Kernel-owned sources expose immutable
semantic snapshots and evaluators; the export layer selects representations and
builds ephemeral approximations; the desktop layer collects an explicit request
and presents preflight. The DXF writer only encodes an already validated neutral
model.

```text
kernel geometry + semantic revision
              |
              v
  source adapter / snapshot ---- stale-revision guard
              |
              v
  exact mapper or adaptive approximation builder
              |
              v
  component outcomes + GeometryExportModel v2
              |
              +----> preflight decision
              |
              v
  DxfExporter -> DxfEncodingResult -> conditional manifest -> safe promotion
```

This preserves the G5 seam and avoids hidden construction objects. Approximation
is parameter-domain based and has no dependency on views or render caches.

## Current gaps and approved owners

| Gap | Approved owner |
|---|---|
| production always marks exact | shared GeoCeDG export adapter/result model |
| writer ignores fidelity | validated neutral model plus sidecar; writer stays format-only |
| label/ordinal source IDs | truthful `id_scope`; use durable ID only after its owning phase supplies one |
| no branch/component address | semantic snapshot and component outcome |
| no deterministic budgets | adaptive builder request/work ledger |
| no preflight | desktop-independent preflight model, rendered by desktop controller |
| direct one-file write | paired-output service and filesystem port |
| no build provenance | build-time embedded GeoCeDG revision provider |

## Failure and transaction semantics

Evaluation occurs against one captured source revision. A revision change before
publication yields `STALE_SOURCE_REVISION`; no mixed-revision bundle is written.
Failure to satisfy requested evidence produces an explicit invalid/unsupported/
work-limit outcome, not a coarse silent fallback. Strict mode writes nothing if
any requested component is not admissible. Partial output is disabled by
default; any future explicit partial mode requires user intent, a visible
warning, and writes every omission into a mandatory sidecar.

When a sidecar is mandatory or requested, both destinations are
collision-checked before work. Temporary files live in the target directory.
After byte/hash/schema validation, the service promotes the DXF and then its
manifest under a recoverable rollback protocol and reports the exact final
state. The manifest hashes the DXF, so interrupted or externally mixed pairs
are detectable. A sidecar is mandatory for every approximation, omission,
partial/unsupported-not-exported component, work-limit termination, or other
fidelity reduction; an entirely exact output may omit it under product policy.

## Upstream impact

Expected changes are additive under `org.geocedg.common.export` and the existing
GeoCeDG desktop controller. Minimal edits to current exporter DTO seams may be
required. No generic GeoGebra command, kernel curve meaning, renderer,
serialization factory, or Classic menu needs to change. Any unexpected need to
place approximation in upstream geometric algorithms is a stop condition.

## Phase dependency

G9P-R1 distinguishes semantic dependency from the recommended integration
schedule. G9X1 requires the G5 export contract and the approved internal G6-G8
semantic evaluator/snapshot contracts for Locus V2; it does not need a public
command, toolbar or `.ggb` factory to build and validate a read-only adapter.
Consequently G9U0 is not a hard semantic dependency.

Running G9U0 before G9X1 is still recommended: it supplies the normal
public/persistent source used by product acceptance and avoids validating only
test/internal entry points. Where a source participates in the future G9A1
registry, X1 may use its durable ID. For every other G5-compatible or internal
source, the sidecar declares `id_scope` as `persistent` or
`construction-revision` and never implies cross-save identity that the adapter
cannot prove. The global G9 closeout integrates the approved U0 and X1 evidence;
failure of one does not retroactively invalidate the other's geometric
contract. Approved typed curve families may use their approved approximation
strategy as normal export behavior only with explicit approximate identification
in preflight and the completion report.
