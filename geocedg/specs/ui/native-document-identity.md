# Specification: Native GeoCeDG document identity

- Status: **NORMATIVE / AUTHOR APPROVED**
- Version: 1
- Owners: GeoCeDG project owner
- Roadmap gate: G9U0-R2 planning/design `PASS — AUTHOR APPROVED`;
  implementation not authorized / not started
- Affected layer: GeoCeDG application/document I/O policy, Desktop open/save
  adapters, recent/direct-open routing and Windows packaging association
- Productive implementation: **not authorized / not started**

## Objective

Establish `.cedg` as the native GeoCeDG document extension and retain `.ggb`
as a supported compatibility/input boundary. The extension is a product and
I/O identity, not a new geometric meaning and not, by itself, a new ZIP/XML
serialization format.

## Authority and dependencies

This contract is subordinate to:

- `AGENTS.md` persistence, compatibility, licensing and upstream rules;
- the current loader/writer and serialization tests;
- the accepted G2 product profile and ADR 0001;
- the accepted G4 packaging architecture and ADR 0004;
- the G9A durable identity/lifecycle contracts;
- the G9U0/G9U0-R1 public persistence contracts; and
- Accepted ADR 0016.

The author approved this design authority at the G9U0-R2 planning closeout.
G9U1 may not be authorized for execution until G9U0-R2 implementation is
completed, validated and closed `PASS — AUTHOR APPROVED`, followed by a
separate G9U1 authorization decision.

## Definitions

- **Native document:** a GeoCeDG-owned user document whose normal path ends in
  `.cedg` and whose save lifecycle follows this specification.
- **Compatibility input:** a `.ggb` accepted for non-destructive load. Opening
  it does not promise that an external upstream application can preserve
  GeoCeDG-only object types.
- **Serialization internals:** the ZIP container, `geogebra.xml`, auxiliary
  entries, XML schemas/tags and `app` header code used by the validated current
  persistence machinery.
- **Source path:** the exact file opened by the user. It is not a semantic
  object identity and must never be used to synthesize geometric IDs.

## Core document policy

1. `.cedg` is the native GeoCeDG document extension.
2. `.ggb` remains a supported compatibility/input extension.
3. Extension identity does not alter geometric semantics, object maturity,
   feature flags, durable IDs, semantic versions, workspace state or command
   meaning.
4. A suffix may select a file chooser, open route or save policy. It may not
   infer or migrate geometric identity from a filename.
5. Renaming bytes does not transform their internal semantics. No loader may
   manufacture GeoCeDG types, IDs or migrations merely because a path ends in
   `.cedg`.
6. There is no silent `.cedg` to `.ggb` downgrade and no destructive
   `.ggb`-to-native conversion.

## Serialization and `app_code: classic`

The required initial policy is to preserve the existing validated ZIP/XML
internals. Current source evidence shows that:

- Desktop `loadXML(File, ...)` opens an input stream and delegates to the ZIP/
  XML reader after extension routing;
- `writeGeoGebraFile(File)` writes the normal ZIP/XML archive to the supplied
  file without choosing serialization from its suffix;
- `MyXMLio` writes the header `app` value supplied by the application config;
  and
- `AppConfigGeoCeDG` deliberately returns the upstream `classic` app code.

Therefore `.cedg` must initially retain `app="classic"`, the existing XML
format version and the existing archive entries. This is the least invasive
compatible policy and keeps extension identity orthogonal to semantic type
versions already persisted inside the XML.

A new archive layout, XML root/version, `app` code, MIME-driven parser or
semantic migration is outside G9U0-R2. If implementation evidence proves one
necessary, work stops for a separate compatibility decision and migration
design. A filename alone is never such evidence.

## Save and Save As state machine

This state machine governs the normal GeoCeDG product route. The separate
GeoCeDG Classic diagnostic preservation boundary is specified below and does
not inherit the new-document default merely by being able to read `.cedg`.

| Current document state | Save | Save As | Required result |
|---|---|---|---|
| unsaved/new | invoke native Save As | native Save As | chooser defaults to `.cedg`; omitted suffix becomes lowercase `.cedg` |
| opened/saved `.cedg` | save the same native path | native Save As | normal native lifecycle; successful Save As changes the current path |
| opened `.ggb` compatibility input | invoke native Save As; never overwrite the source | native Save As | write a distinct `.cedg`; cancellation or failure leaves source and current in-memory document unchanged |
| other/unknown input | fail closed or use an explicitly approved import policy | native Save As only after successful import | never infer a GeoCeDG document from the suffix alone |

Normal Save/Save As must not offer `.ggb` as a native output. A future explicit
compatibility export/copy is a separate gate with loss analysis and must not be
disguised as Save. G9U0-R2 does not authorize it.

When Save As is given a name with no extension, exactly one `.cedg` is added.
On case-insensitive hosts, `.CEDG` and mixed-case variants are recognized for
input and overwrite detection, while newly proposed filenames are normalized
to lowercase `.cedg`. A conflicting non-native suffix must not be silently
replaced without visible confirmation.

The implementation must preserve the last complete target and the current
in-memory construction on write failure. It may use the narrowest host-safe
temporary-write/replace mechanism needed to meet that result; this
specification does not declare a universal filesystem atomicity guarantee.

## Open, reopen, recent files and direct open

GeoCeDG open dialogs accept native `.cedg` first and compatibility `.ggb`
second. Reopen, drag/drop, recent-file actions, command-line/direct-open and
Windows shell launch must route both extensions to the same validated ZIP/XML
reader with the correct source classification.

The recent-file list records the actual path. Reopening a `.ggb` retains its
compatibility-input save policy. A successful native Save As adds/selects the
new `.cedg` path without modifying or deleting the source `.ggb`.

A corrupt, truncated or structurally invalid `.cedg` fails with a localized
load diagnostic. It must not partially publish a construction, fabricate
identity, replace the previously live document or rewrite the corrupt file.

## GeoCeDG Classic diagnostic boundary

The separate GeoCeDG Classic diagnostic process uses the same fork kernel and
must:

- directly open `.cedg` and compatibility `.ggb`;
- preserve supported Locus V2, rich-result/token and G9 durable identity types;
- preserve an opened `.cedg` as `.cedg` through save/reopen without downgrade;
- keep its existing separate preference namespace and creation policy; and
- never downgrade GeoCeDG types to legacy locus, lists, coordinates or sampled
  curves.

This preservation requirement does not change the diagnostic Classic route's
default new-document identity or enable disabled experimental creation. Its
new-document and `.ggb` save defaults remain the pre-R2 diagnostic policy unless
a later phase separately authorizes a change. It may not silently turn an
opened `.cedg` into `.ggb` or lose GeoCeDG-native content.

This contract does not make an external upstream GeoGebra distribution a
`.cedg` reader. External upstream remains outside the supported-open boundary
for unknown GeoCeDG types. GeoCeDG must not rename, flatten or destructively
convert a document to conceal that boundary.

## Windows packaging and file association

The future Windows package profile and MSI/EXE association must use `.cedg` as
the GeoCeDG-owned document association. App-image and portable ZIP remain
association-free. GeoCeDG open dialogs continue to accept `.ggb`, but installers
must not claim or replace the host's default `.ggb` association merely because
the format is accepted as input.

The future association needs a GeoCeDG-owned ProgID and description. No MIME
string is frozen by this contract. If the actual Windows packaging toolchain
requires MIME metadata, implementation must justify and record the narrowest
owned value without claiming the upstream GeoGebra MIME identity. Public
distribution remains blocked by the existing licensing/asset gate. No
non-Windows file-association validation claim belongs to G9U0-R2.

## Compatibility and persistence invariants

Native `.cedg` round trips must preserve, at minimum:

- ordinary and Locus V2 visual styles;
- Locus V2 generator/domain/branch semantics;
- rich metric and intersection results plus exact-token children;
- G9 durable geo/spatial/frame/system/map/relation/binding identities;
- legacy Locus and ordinary upstream construction behavior; and
- the same `classic` XML app code unless a separately accepted ADR changes it.

The `.ggb` source bytes and path remain unchanged during a compatibility-input
to native-save transition. Hash evidence must prove this. Semantic deterministic
reruns compare canonical XML, normalized archive-entry inventories and entry
content hashes; byte-identical ZIP output is required only if the writer itself
has a separately proven deterministic timestamp/metadata policy.

## Validation

The future focused verifier must implement rows `R2-D01` through `R2-D17` in
`docs/validation/g9_public_workspace_validation_matrix.md`. It must also rerun
the G9U0-R1, historical G9U0, G9X1, G5, relevant G9A, legacy Locus, packaging
contract and composed authorities, followed by the manual author smoke plan.

## Stop conditions and open decisions

Stop for author review if:

- `.cedg` requires a new ZIP/XML layout or persisted app code;
- safe native Save cannot keep an opened `.ggb` source unchanged;
- filename handling would synthesize or migrate semantic identity;
- GeoCeDG Classic cannot preserve native types without downgrade;
- external-upstream compatibility would require destructive conversion;
- Windows association would reuse an unapproved upstream product identity; or
- implementation is not separately authorized or cannot preserve this
  author-approved contract.

This specification is approved normative design authority. It makes no
`.cedg` implementation or observable-product claim.
