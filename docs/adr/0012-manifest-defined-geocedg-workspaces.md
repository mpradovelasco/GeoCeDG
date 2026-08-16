# ADR 0012: Manifest-defined GeoCeDG workspaces

- Status: **Accepted**
- Accepted: 2026-08-16
- Phase: G9P design; G9U1/G9U2 implementation not authorized
- Normative contract: `geocedg/specs/ui/cedg-workspaces.md`

## Context

ADR 0001 established a dedicated GeoCeDG profile and made
`apps/geocedg/application-profile.yml` the single durable source for its first
perspective and toolbar. The version-1 adapter currently reads one perspective
and compiles one flat list of numeric upstream mode IDs
(`GeoCeDGProfile.java:80-166`). It has no named workspace registry, shared
action metadata, runtime feature evaluation, workspace switch, or product-owned
localization/icon contract.

The G9P reference files demonstrate a professional workflow using Graphics,
Algebra, Construction Protocol, Properties, command input, contextual help,
and a much broader tool population. They also carry Templatev7's 24 legacy
macros and seven custom groups. Those document macros are valuable evidence but
cannot become stable product capability simply by copying their toolbar.

G9P must also design a later procedure-focused environment without confusing a
workspace with an upstream tool mode or making GUI state geometric authority.

The initial proposal referred generally to frames, bindings and hinges. R1
clarifies that future Dihedral actions must consume the kernel-owned
ProjectionSystem-equivalent: relative frame relations, intrinsic frame
coordinates, the map into the common CeDG diagram, declared line-of-ground/
hinge semantics, orientation/roles, change-of-plane lineage, revision,
consistency and degeneration. R1 also adds the G9U0 supported
point-on-Locus/nested-generator action to the existing professional eleven-group
Construction design and records that G9U1 is not a semantic dependency of G9B.
These are transparent refinements, not claims about the original proposal.

## Decision

1. Use **workspace** as the user-facing concept; reserve mode for interactive
   tool IDs.
2. Evolve the application profile to schema version 2 with one central action
   catalog and multiple workspaces that reference those actions.
3. Make **CeDG Construction** the default professional workspace. Its approved
   default views are Graphics, Algebra, Construction Protocol, bottom input and
   contextual help, with Properties directly accessible/floating. Preserve its
   eleven broad groups and, after G9U0 PASS, expose typed Locus creation,
   supported point-on-Locus, rich/guarded metric, rich Intersect and exact-token
   point actions as GUI clients.
4. Define **CeDG Dihedral Procedures** in the same manifest contract but keep
   it unavailable until `G9 global PASS — AUTHOR APPROVED`. Its actions
   must consume typed spatial objects, durable ProjectionSystem/frame
   relations, intrinsic-to-diagram maps, line-of-ground/hinge semantics and
   bindings and produce explicit construction steps. Visible 2D placement may
   not establish any of those relations.
5. Preserve GeoCeDG Classic as the existing separate diagnostic process/path
   and preference namespace. Expose it as a route, not an in-process workspace.
   Supported GeoCeDG V2/rich/spatial objects remain native and retain their
   IDs/tokens/bindings through save/reopen and recomputation with the same
   kernel. Never downgrade them silently. External upstream GeoGebra that does
   not implement the persisted types is an unsupported-open boundary, not a
   compatibility target for lossy conversion.
6. Make workspace switching presentation-only. It may change docks, toolbar,
   menus and contextual help; it may not filter independently authorized
   algebra commands, change semantics, or enter Construction Protocol history.
7. Store active workspace and customized layouts in GeoCeDG preferences. Treat
   a document-carried perspective as a transient **Document layout**; do not
   rewrite it into the product manifest or use it as geometric authority.
8. Declare selection grammar, maturity, feature requirements, help keys,
   localization keys, icon IDs and unavailable behavior once per action.
9. Do not reserve future mode IDs or import Template macros into the stable
   action catalog during G9P.
10. Support deterministic in-memory migration from profile schema version 1
    during a compatibility period; do not change the accepted v1 manifest until
    the separately authorized G9U1 implementation.
11. Treat G9U1 as an interaction/presentation client with no hard dependency on
    G9B, and prohibit a G9B-to-G9U1 reverse dependency. Operational execution
    order does not alter that semantic dependency graph.

## Consequences

- Toolbar, menu, help, selection and availability metadata have one durable
  source instead of scattered strings.
- The broad Construction workflow can be organized without exposing every tool
  as an individual permanent button.
- R1 point-on-Locus and scalar-generator actions fit the existing eleven groups;
  the workspace neither owns preimage/generator semantics nor becomes minimal.
- Feature policy and workspace presentation remain separate, so switching
  workspace cannot accidentally activate experimental geometry.
- Existing `.ggb` perspectives remain readable and old constructions retain
  their meaning.
- A schema/compiler, runtime feature service, action registry, workspace
  controller, preference adapter and owned resources are required before G9U1
  can pass.
- The Dihedral Procedures workspace cannot be implemented merely as macro
  automation or visible-placement inference; it depends on G9 identity,
  ProjectionSystem/frame-map/hinge, binding, sufficiency and lifecycle.
- G9B kernel work can proceed without G9U1, while G9U1 can organize already
  approved nonspatial actions without G9B. G9U2 alone retains the relevant
  global G9 dependency.

## Alternatives considered

### Add more numeric modes to the version-1 toolbar

Rejected as the target architecture. It cannot express multiple workspaces,
selection contracts, menus, localized status, runtime gates or diagnostic
routes without new hard-coded authorities.

### Copy Templatev7's toolbar and macros

Rejected. The template contains research, presentation, sheet/export and
sampled-locus workarounds with unresolved rights and maturity. A document-local
macro is not a stable command contract.

### Hard-code each workspace in Desktop Java

Rejected because it would violate the accepted manifest authority and create
drift between toolbar, menu, help and feature policy.

### Treat Classic as an in-process workspace

Rejected. The accepted separate launcher protects preference
and profile boundaries and is already validated.

### Persist workspace ID as geometric document state

Rejected. Workspace state is presentation-only; writing it into a new semantic
format is unnecessary for G9U1 and risks confusing UI state with construction
truth.

## Acceptance record and implementation gate

G9P closeout accepted the workspace names, manifest authority, GeoCeDG Classic
diagnostic policy, presentation-only switching, and global G9U2 gate. Panel
details, disabled-action UX, legacy Locus placement, and document-layout
implementation remain G9U1 review items. Acceptance authorizes the
architectural choice, not productive code. G9U1 still requires a canonical prompt, normative schema
and spec, G9U0 dependency disposition, focused tests, visual validation and the
composed verifier; it does not require G9B. G9U2 additionally requires G9 global
PASS and must consume the approved ProjectionSystem/map/hinge contract. G9U1
and G9U2 remain not authorized.
