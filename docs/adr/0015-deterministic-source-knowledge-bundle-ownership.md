# ADR 0015: Deterministic source and knowledge bundle ownership

- Status: **Accepted**
- Date: 2026-08-16
- Scope: documentation maintenance and future G9O1 bundle generation
- Productive implementation: G9O1 **PASS — AUTHOR APPROVED**

## Context

GeoCeDG combines project-native source, narrowly modified upstream GeoGebra
files, unchanged upstream reference code, generated artifacts and scientific or
UI assets with different provenance. A useful source/knowledge bundle cannot
classify those reliably from folder names alone. It must also allow living
documentation to advance without invalidating immutable G7/G8 evidence.

The G9P task requires a complete bundle design but expressly forbids a
productive generator. Historical phase manifests already hash files that later
become living documents. Revalidating those hashes against current `HEAD`
conflates two different claims.

## Decision

1. Classify each path as `GEOCEDG_NATIVE`, `UPSTREAM_MODIFIED`,
   `UPSTREAM_UNCHANGED_REFERENCE`, `GENERATED`, or
   `THIRD_PARTY_OR_RESTRICTED` using explicit exclusions, namespace/path,
   modified-file inventory, pinned-baseline Git comparison and history.
2. Include only native and upstream-modified source by default. Unchanged
   upstream reference requires an explicit profile rule; generated and
   restricted material is excluded.
3. Treat a bundle as generated evidence with a versioned manifest, never as
   source authority.
4. Require deterministic UTF-8/LF content, stable ordering/archive metadata,
   clean-tree default, explicit dirty mode, budgets and freshness against
   `HEAD`.
5. Preserve complete upstream-modified files and their baseline blob identities;
   optional diffs supplement rather than replace complete source.
6. Freeze historical G8 evidence at annotated tag object
   `fed1bfbeea77a48acce285429b397eda77054df1`, peeled commit
   `e7810171179825a03b22d8c6eba28c672f468281`. Verify its manifests and named
   files from tag blobs and require current `HEAD` to descend from that commit.
7. Validate current living documentation separately. Never rewrite a historical
   hash manifest merely because a living guide changed.
8. Defer generator and focused verifier implementation to an independently
   authorized G9O1 phase.
9. Execute G9O1 first as the recommended operational predecessor for later G9
   work, but do not make `G9O1 PASS` a semantic dependency of G9A1. A bundle or
   guide failure blocks the applicable reproducibility/global-closeout gate; it
   does not make spatial identity or projection mathematics undefined.

## Consequences

The design provides deterministic provenance and prevents later documentation
maintenance from falsifying historical evidence. It requires explicit handling
of inventory/Git disagreement, strict encoding, archive normalization and
license exclusions. Some useful upstream or scientific content will be
referenced rather than copied.

The R1 dependency clarification permits G9A1 to start from its approved
semantic and frozen-G8 authorities even if operational scheduling is changed,
while retaining G9O1 evidence as part of the integrated G9 release closeout.

## Alternatives

### Include all files changed from upstream

Rejected because it includes generated/restricted material and misses native
files without an upstream counterpart.

### Use only GeoCeDG-owned directory names

Rejected because controlled upstream modifications are essential and ownership
cannot be proven from location alone.

### Recompute old evidence hashes from current files

Rejected because that rewrites history and makes an old phase claim depend on
later documentation edits.

### Build one monolithic knowledge document

Rejected because it destroys file boundaries, exceeds practical budgets and
obscures provenance.

## Acceptance record and implementation gate

G9P closeout accepted ownership precedence, dirty-tree policy, budgets,
historical anchor, profile scope and the operational-versus-semantic dependency
boundary. The related specifications are **NORMATIVE / AUTHOR APPROVED**.
G9O1 is implemented under the operational tooling layer and is **PASS — AUTHOR
APPROVED**. This closeout record does not amend the Accepted decision or create
a semantic dependency on G9A1. G9A1 is separately authorized and not started.
