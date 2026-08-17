# Deterministic knowledge bundle architecture

- Status: author-approved architecture under a **NORMATIVE / AUTHOR APPROVED** contract
- Phase: G9O1 — **PASS — AUTHOR APPROVED**
- Productive generator: `tools/knowledge/build-knowledge-bundle.ps1`
- Contract: `geocedg/specs/operations/knowledge-bundles.md`
- Accepted ADR: `docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md`

## Component design

```mermaid
flowchart LR
    C["Git commit and configuration"] --> I["Candidate inventory"]
    B["Pinned upstream baseline"] --> O["Ownership classifier"]
    M["Modified-file inventory"] --> O
    I --> O
    O --> S["Profile selector"]
    S --> N["UTF-8/LF normalizer and chunk planner"]
    N --> F["Manifest and file set"]
    F --> A["Stable archive writer"]
    A --> V["Independent verifier"]
```

All components are operational services outside the kernel. G9P defined their
contracts; the approved G9O1 implementation realizes them without changing
application or kernel runtime behavior.

## Ownership engine

Classification first applies explicit restricted/generated exclusions, then
native ownership, then modified-upstream evidence, and finally explicitly
allowed unchanged upstream reference. The engine records every evidence source
used. Conflicting classification fails closed.

The baseline comparison is blob-based at the pinned GeoGebra commit. A modified
upstream entry retains the complete current file, baseline blob SHA, change
summary and optional derived unified diff.

## Selection and ordering

Profiles are declarative input. Selection results are sorted by normalized
repository-relative path and then by declared reading-order rank. A profile may
add related tests/specs but may not override restricted/generated exclusions
without a separately recorded explicit authorization.

## Content pipeline

Text is read as bytes, decoded as strict UTF-8, stripped of an optional UTF-8
BOM and normalized to LF. The manifest retains both raw and canonical hashes.
Restricted/binary paths are excluded by the implemented default profiles; a
future explicit binary policy would require separate authorization. No variable
timestamp, local absolute path or host-specific path separator enters
deterministic content.

Chunk planning respects complete files and semantic boundaries. A continuation
contains the complete source hash and exact range, so chunks cannot be mistaken
for independent source.

## Manifest and archive

The manifest is written before the archive, then independently re-read. The
bundle ID is derived from schema version, commit and canonical configuration.
Archive entries use stable path order, permissions and timestamp metadata. A
dirty bundle includes a canonical diff hash and is visibly non-release evidence.

## Verification

`tools/knowledge/verify-knowledge-bundle.ps1` independently recomputes:

- fixed commit/freshness and clean/dirty policy;
- ownership from baseline/inventory/history;
- exclusions and license/provenance;
- canonical file and configuration hashes;
- bundle ID, order, chunks and budgets;
- stable archive metadata and byte reproducibility.

It does not import or trust the generator implementation. The fixture suite and
`tools/agent/verify-knowledge-bundles.ps1` exercise deterministic reruns,
membership, ownership, hashes, dirty state, exclusions, unsafe paths, budgets
and freshness. Generated outputs stay below ignored `artifacts/knowledge/`.
