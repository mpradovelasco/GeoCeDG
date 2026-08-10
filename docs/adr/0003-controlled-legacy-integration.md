# ADR 0003: Controlled legacy CeDG resource integration

- Status: **Accepted for G3**
- Date: 2026-08-10
- Scope: legacy resources, scientific references, and opt-in Laboratory access
- Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`

## Context

The author supplied a historical CeDG template, scientific publications, and a
book that documents terminology and working methods. `Templatev7.ggb` contains
24 embedded tools, a custom toolbar, and JavaScript. These materials are primary
legacy evidence but do not have approved stable product contracts.

G1 already defines JSON-compatible YAML, feature maturity, model manifests, and
composed verification. G2 provides a stable GeoCeDG toolbar and a separate
Classic diagnostic launcher. G3 must use those boundaries rather than create a
parallel application or reinterpret legacy XML as new geometry.

## Decision

1. Preserve every original imported artifact byte-for-byte under
   `models/legacy/<resource>/original/` and record SHA-256.
2. Keep curated provenance and architectural interpretation separate from a
   deterministic derived inventory.
3. Extend the version 1 model schema only with optional legacy fields; existing
   G1 manifests retain their meaning.
4. Use `tools/legacy/ingest.ps1` for safe copy, inspection, regeneration, and
   comparison. It never silently overwrites a different original.
5. Make the first CeDG Laboratory an explicit PowerShell loader over the
   existing GeoCeDG and Classic launchers. It loads only registered and
   hash-valid resources and is disabled unless invoked.
6. Preserve the complete `Templatev7` toolbar string, groups, subgroups, and
   tool order as legacy metadata. Do not add those tools to the G2 toolbar.
7. Register the public GeoGebra book as a metadata-only external corpus. Its 71
   indexed models are not downloaded and are not build dependencies.
8. Treat scientific PDFs as local reference records. Their recorded rights
   status controls future redistribution review; G3 does not republish or
   transform their content.
9. Add a G3 verifier to `tools/agent/verify.ps1`; keep the composed verifier as
   the top-level authority.

The precise contract is
`geocedg/specs/legacy/controlled-integration.md`.

## Consequences

- The historical workflow can be inspected and compared reproducibly.
- Stable GeoCeDG and Classic behavior remain unchanged.
- Derived inventories can be regenerated after future resource additions.
- Rights uncertainty cannot be hidden by copying a file into `models/`.
- No legacy macro becomes a Java command, DSL procedure, or kernel feature in
  G3.

## Rejected alternatives

### Extract every macro as a standalone `.ggt`

Rejected for G3 because it fragments the primary artifact, can change macro
dependencies and icons, and is unnecessary for inspecting the proven toolbar
workflow.

### Add all macros to the stable G2 toolbar

Rejected because maturity and future toolbar placement are separate decisions.

### Download the complete public book

Rejected because it would create unnecessary rights, provenance, storage, and
remote-availability coupling.

### Reimplement locus-length tools now

Rejected because those macros approximate length from sampled points. Native
Locus semantics belong to G6-G8 and require their approved kernel contract.

### Adopt the prior JavaScript object/view association proof of concept

Rejected as production architecture because it uses model lists and names. It
is retained only as research evidence for the future spatial semantics gate.

## Validation

- schema and catalog validation;
- original hash and ZIP/XML structure checks;
- deterministic `Templatev7` inventory regeneration;
- Laboratory resolution and actual resource load;
- exact preservation of the seven legacy custom toolbar groups;
- exact preservation of the G2 stable toolbar;
- GeoCeDG and Classic launch regression;
- composed verification, benchmark, whitespace, and output cleanup.
