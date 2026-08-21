# ADR 0016: Native GeoCeDG document identity

- Status: **Accepted**
- Date: 2026-08-21
- Accepted: 2026-08-21, G9U0-R2 planning/design closeout
- Phase: G9U0-R2 planning/design `PASS — AUTHOR APPROVED`;
  implementation not authorized / not started
- Normative contract: `geocedg/specs/ui/native-document-identity.md`

## Context

GeoCeDG is already a distinct product profile, but its normal Desktop Save As,
open filters and Windows package association still use `.ggb`. Public G9U0
objects are persisted natively by the fork, and external upstream applications
are not guaranteed to understand those types. Using the upstream extension as
both GeoCeDG's native identity and its compatibility boundary obscures that
product distinction.

Repository evidence does not show a need for a new container or XML dialect.
The Desktop loader delegates a file stream to the same ZIP/XML reader after
extension routing; the writer writes the existing GeoGebra archive to whatever
`File` it receives. `AppConfigGeoCeDG` deliberately supplies `classic` to the
XML header, and the accepted G9 persistence contracts version GeoCeDG semantic
types inside that existing machinery.

The workspace-v2 phase G9U1 must not bake `.ggb`-native assumptions into a new
profile/action implementation before the document identity is decided.

## Decision

1. Use `.cedg` as the native GeoCeDG document extension.
2. Keep `.ggb` as a supported compatibility/input boundary, not the normal
   native Save/Save As output.
3. Preserve the validated ZIP/XML serialization internals and
   `app_code: classic` initially. Extension identity is not an XML semantic
   version and does not create geometric meaning.
4. Let the extension select I/O routing only. Never infer, mint or migrate
   semantic identity from a filename.
5. Route Save on a compatibility `.ggb` through native Save As to a distinct
   `.cedg`; never overwrite or delete the `.ggb` source.
6. Support `.cedg` and `.ggb` in open/reopen/recent/direct-open routes, with
   host-appropriate case handling and fail-closed corrupt-document behavior.
7. Require the separate GeoCeDG Classic diagnostic process to open and preserve
   supported `.cedg` documents through the shared kernel without enabling
   disabled creation or changing its default new-document identity merely
   because it can preserve `.cedg`. Do not promise external upstream `.cedg`
   open or hide unsupported GeoCeDG types through lossy conversion.
8. Associate `.cedg`, not `.ggb`, in future GeoCeDG MSI/EXE installers.
   Use a GeoCeDG-owned ProgID; do not freeze a MIME value unless actual
   implementation requires and justifies one. Portable artifacts remain
   association-free, Windows is the only validated association platform in
   this phase, and public redistribution remains separately blocked.
9. Make author-approved G9U0-R2 implementation PASS an explicit prerequisite
   for any G9U1 execution authorization. Passing G9U0-R2 implementation will
   not authorize G9U1 by itself.

## Relationship to existing decisions

This ADR:

- close the file-identity part of ADR 0001's deferred question while retaining
  ADR 0001 decision 7 (`classic` app code);
- supersede only ADR 0004 decision 4's `.ggb` installer association with the
  `.cedg` association; all other G4 packaging and redistribution constraints
  remain in force;
- leaves ADR 0012 Accepted, but supersedes any document-identity reading of its
  decisions 5/7, `.ggb`-perspective consequence or acceptance record that
  would make `.ggb` GeoCeDG's future native extension; `.ggb` remains readable
  compatibility input, `.cedg` becomes native after implementation, and
  workspace preferences remain outside document semantics; and
- refine ADR 0013's Classic/save boundary without changing any Locus V2
  mathematical, identity, token or compatibility semantics.

Historical accepted ADRs and reports remain unchanged. The supersession above
is limited to document identity; ADR 0012's workspace names, manifest authority,
separate Classic process, presentation purity and G9B independence remain
Accepted.

## Consequences

- GeoCeDG obtains an explicit native document identity without forking the
  validated serialization engine.
- A `.ggb` open is non-destructive and produces a native `.cedg` only through
  an explicit save destination.
- File chooser, direct-open, recent-file, diagnostic Classic and packaging
  routes require coordinated product-layer changes and tests.
- The current blanket native-save compatibility warning must become
  destination-aware; a native `.cedg` save is not an external-upstream export.
- Existing `.ggb` fixtures remain compatibility evidence and must not be
  renamed or resaved as a migration shortcut.
- G9U1 must use a superseding canonical prompt/contract revision after this
  gate closes; its frozen G9P prompt remains historical evidence.

## Alternatives considered

### Continue using `.ggb` as the native extension

Rejected because it conflates GeoCeDG's native persisted types
with the external compatibility/input boundary and leaves G9U1 to entrench that
ambiguity.

### Introduce a new ZIP/XML dialect or `app="geocedg"`

Rejected absent repository evidence. It expands compatibility and migration
risk without being necessary to establish a product extension. A future need
would require a separate ADR and migration matrix.

### Infer GeoCeDG documents from their filename

Rejected. A suffix may route I/O but cannot establish semantic type, maturity,
identity or migration.

### Keep `.ggb` as a second normal Save As output

Rejected for this bounded gate. A compatibility export needs explicit fidelity
and downgrade policy; treating it as ordinary Save would permit silent loss or
false upstream compatibility.

### Claim both `.cedg` and `.ggb` Windows associations

Rejected. Accepting `.ggb` input does not authorize GeoCeDG to
take ownership of the upstream extension on a workstation.

## Acceptance record and implementation gate

The author accepted the `.cedg`/`.ggb` policy, retention of `app_code: classic`,
non-destructive normal GeoCeDG native save from `.ggb`, the narrower GeoCeDG
Classic preservation boundary, GeoCeDG-owned `.cedg` Windows association and
the decision not to freeze an arbitrary MIME string. This acceptance approves
the planning/design authority; it does not start implementation.

Implementation requires a separate explicit invocation of the canonical
G9U0-R2 prompt and must stop at `IMPLEMENTATION CANDIDATE — PENDING AUTHOR
REVIEW`. Only a later explicit author decision may close G9U0-R2 implementation
as PASS.
