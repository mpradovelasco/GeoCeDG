# Controlled integration of legacy CeDG resources

- Status: accepted for G3
- Contract version: 1
- Authority: `docs/adr/0003-controlled-legacy-integration.md`

## Purpose and boundaries

G3 preserves and characterizes existing CeDG resources without promoting them
to stable product behavior. It introduces no geometric definition, kernel
command, serialization rule, Locus change, projection association, or G4
packaging behavior.

The maturity path is `legacy -> research -> experimental -> stable` or
`experimental -> deprecated`. Each transition requires an explicit review; the
presence of a macro in a historical toolbar is never a promotion decision.

## Resource layers

Every imported resource separates these layers:

1. `original/`: byte-identical author-supplied or externally obtained artifact;
2. `manifest.yml`: curated provenance, compatibility, rights, maturity, and
   load policy;
3. `curation.yml`: reviewable scientific and architectural interpretation;
4. `derived/`: deterministic inventory or normalized evidence generated from
   the original plus curation;
5. regression metadata, only after a governing geometric specification and
   verifiable expected results exist.

The original artifact is immutable and identified by SHA-256. XML inside a
`.ggb` or `.ggt` is inspection evidence, not a replacement geometric authority.

## Deterministic ingest

`tools/legacy/ingest.ps1` is the ingestion and inspection entry point. It:

- accepts only `.ggb`, `.ggt`, `.js`, and `.ggs` resources;
- copies bytes without transformation when `-Import` is explicit;
- refuses to overwrite a different original;
- records the source hash and container metadata;
- inventories embedded macros, ordered inputs/outputs, command dependencies,
  scripts, and historical toolbar positions;
- combines structural facts with a separate checked-in curation source;
- supports a read-only `-Check` mode that compares regenerated inventory with
  the committed derived artifact.

Manifests and curation remain durable sources. The generated inventory is
reproducible evidence and must not be edited by hand.

## CeDG Laboratory

`tools/legacy/open-laboratory.ps1` resolves only registered, hash-valid,
non-default legacy/research/experimental resources. The default route opens the
resource in the G2 GeoCeDG launcher; `-Classic` uses the preserved upstream
diagnostic launcher. `-ValidateOnly` performs no graphical launch.

The Laboratory is opt-in and experimental. Loading `Templatev7.ggb` lets its
document-owned historical toolbar appear in that document context. It does not
change `apps/geocedg/application-profile.yml`, does not enable a legacy tool by
default, and does not define the future GeoCeDG toolbar architecture.

## Promotion and regression

Promotion requires, in order:

1. preserved provenance and rights review;
2. characterized inputs, outputs, validity domain, degeneracies, and dynamic
   behavior;
3. an architectural placement decision;
4. an approved specification for any semantic behavior;
5. deterministic tests and model evidence;
6. an explicit maturity and feature-manifest change.

Recommendations use four non-executing categories:

- remain an external/legacy tool;
- consider a future high-level GeoCeDG command;
- consider a future DSL procedure after the DSL gate;
- require future kernel design because semantics or dependency behavior are
  involved.

G3 recommendations authorize none of those migrations.

The public GeoGebra book index is metadata-only. Remote resources are not build
dependencies, and no model is downloaded merely because it is a regression
candidate.
