# Objective

Implement the bounded G9U0-R2 **PRE-G9U1 PRODUCT / DOCUMENT REFINEMENT** gate:
ordinary
Locus V2 visual-style/Properties/render continuity and native `.cedg` document
identity with non-destructive `.ggb` compatibility input.

**AUTHOR-APPROVED CANONICAL IMPLEMENTATION PROMPT — EXECUTION NOT AUTHORIZED /
UNEXECUTED.**

This prompt does not execute itself. It requires an explicit later author
instruction and must terminate at `IMPLEMENTATION CANDIDATE — PENDING AUTHOR
REVIEW`; it may not claim PASS or approve its own result.

# Mandatory entry gate

Before editing, require all of the following:

- author-selected clean implementation base with recorded `HEAD`, `main` and
  remote state;
- G9U0, G9U0-R1 and G9X1 reproduced from their frozen
  `PASS — AUTHOR APPROVED` authorities;
- green G9A durable identity/lifecycle, G5, legacy Locus and composed entry
  authorities;
- ADR 0016 explicitly Accepted by the author;
- `geocedg/specs/locus/locus-v2-presentation.md` and
  `geocedg/specs/ui/native-document-identity.md` explicitly promoted to
  `NORMATIVE / AUTHOR APPROVED`;
- annotated planning authority `geocedg-g9u0-r2-planning-pass` present, with
  HEAD descending from its peeled commit and the prompt/spec/ADR blobs matching
  that authority;
- an author-approved roadmap state that identifies G9U0-R2 as the next
  executable gate; and
- a separate, explicit author instruction invoking this exact canonical prompt.

If any condition is absent, stop without implementation. The author-approved
planning/design closeout is not implementation authorization.

# Hard dependencies

G9U0-R1 is the semantic/public-lifecycle dependency for Locus presentation.
The accepted G2/G4 product boundary and G9A/G9U0 persistence machinery are the
document dependency. G9X1 author-approved PASS is a mandatory execution-order
and product-integration predecessor, not document or style authority.

G9B and G9C are independent and must neither be implemented nor used to make
this gate pass. G9U1 is the successor and is not part of this implementation.

# Recommended execution predecessor

Execute only after the already closed G9X1 on a conflict-free branch such as
`feature/g9u0-r2-product-refinement`. Preserve G9O1 reproducibility and every
frozen G9U0/G9U0-R1/G9X1 artifact. This schedule does not create a G9X1-to-style
or G9X1-to-document semantic dependency.

# Global/release gate

G9U0-R2 implementation must close `PASS — AUTHOR APPROVED` before the author
may separately authorize G9U1. An R2 implementation candidate or PASS does not authorize
G9U1, G9B, G9C, G9U2, productive G10, release or redistribution.

# Authority and evidence hierarchy

Read, in order:

1. `AGENTS.md` and current repository source/tests/build;
2. the accepted G6–G9U0 Locus semantics/public persistence and G9A identity
   contracts;
3. Accepted ADR 0001, 0004, 0012, 0013 and 0016;
4. the normative G9U0-R2 presentation and native-document specs;
5. current `GeoLocusV2`, `DrawLocusV2`, `LocusRenderCache2D`, Properties/style,
   file-extension, open/save/recent/direct-open, ZIP/XML and packaging source;
6. `docs/validation/g9_public_workspace_validation_matrix.md`; and
7. `docs/architecture/g9u0_r2_product_refinement_design.md` for source routing
   and planned operational integration.

Current source and accepted contracts override examples or this prompt. Stop on
an unresolved serialization or semantic contradiction.

# Scope

Implement the smallest coherent changes for:

- ordinary `GeoElement` color, line thickness, supported line types,
  show/hide and applicable label presentation on public Locus V2;
- ordinary Properties/style applicability without generic `Path` conformance;
- normal selection/highlight through the ordinary host presentation path;
- style persistence, supported copy and applicable undo/redo;
- render continuity across unrelated line/circle/conic crossings, with genuine
  semantic component gaps preserved;
- `.cedg` as native Save/Save As/open/reopen/recent/direct-open identity;
- `.ggb` as compatibility input whose Save routes to a distinct native
  `.cedg` and never overwrites the source;
- `.cedg` open/save/reopen preservation in the separate GeoCeDG Classic
  diagnostic without changing its default new-document identity or enabling
  disabled creation;
- `.cedg`-only Windows MSI/EXE association and no portable association;
- focused tests, fixtures, evidence, modified-files registration, verifier,
  composed integration, and implementation-candidate documentation; and
- user/developer/packaging guide updates only when the implemented behavior has
  been validated and is genuinely observable.

# Explicitly forbidden scope

Do not:

- modify Locus V2 mathematical definitions, drivers, domains, parameterization,
  branches/components, metrics, intersections, tokens or revision semantics;
- implement generic `Path`, alter legacy Locus or introduce a parallel style
  model;
- use other graphical objects, z-order, clipping or screen pixels as semantic
  or subpath authority;
- introduce a new archive/XML format, XML root/app code or automatic migration
  unless separate repository evidence has first stopped the phase and the
  author accepts a new decision;
- infer GeoCeDG semantic identity from a filename;
- offer `.ggb` as ordinary native Save As output or add lossy compatibility
  conversion;
- change the GeoCeDG Classic diagnostic route's default new-document identity
  merely because it can preserve `.cedg`;
- promise external upstream support for unknown GeoCeDG persisted types;
- implement workspace/profile schema v2, G9U1, G9B, G9C, G9U2 or G10;
- rewrite historical author-approved reports, catalogs or evidence;
- copy upstream branding/assets or broaden redistribution; or
- commit, push, merge, tag or self-approve unless a separate author instruction
  explicitly authorizes that action.

# Editable boundaries

Use only the minimum shared `GeoElement`/file-extension seams, GeoCeDG Desktop
open/save adapters, product resources, Windows package profile, focused tests,
fixtures, docs/evidence/verifiers and required registered upstream-impact
entries. Preserve upstream layouts and Classic defaults outside the explicit
GeoCeDG diagnostic contract.

Any `source/` addition or modification must be recorded in
`docs/upstream/modified-files.yml` with its accepted authority and bounded
purpose. Reject broad refactors and unrelated formatting.

# Productive versus test-private permissions

The accepted visual-style and document-I/O behavior may become productive only
inside this authorized gate. Analytic render probes, corrupt archives, registry
sandboxes and compatibility fixtures remain test/evidence inputs. Do not ship a
test migration tool or treat evidence archives as product documents.

# Architectural placement

Visual style remains ordinary `GeoElement` presentation. Render tessellation is
view-owned derived data. Document extension/save state belongs to the
application/Desktop I/O boundary. ZIP/XML persistence and geometric semantics
remain shared existing authorities. Windows association belongs to packaging.
Workspace v2 remains a later consumer and owns none of these contracts.

# Required design/specification

Reuse ordinary line-style/visibility/label authority, the narrow existing
Properties capability and normal selection/highlight presentation. For every
color/thickness/line-type/visibility/label operation, prove unchanged durable
ID, semantic revision, generator, domain, branch/component structure, metric,
intersection, solution-token and normal semantic-DAG evidence.
Selection/highlight is transient presentation, not
persisted semantic authority. Keep `GeoLocusV2` non-`Path`.

Subpath topology must be derived only from semantic branches/components and
truthful invalid evaluation. Adding, moving, styling, selecting or reordering an
unrelated line/circle/conic cannot change fixed-policy render vertices or
subpath markers. Distinguish semantic gaps from clipping, ordinary overdraw,
z-order and dash gaps.

Implement the approved normal GeoCeDG document state machine:

```text
new/unsaved        --Save/Save As--> lowercase .cedg
opened .cedg       --Save-----------> same .cedg
opened .ggb input  --Save-----------> native Save As to distinct .cedg
cancel/failure     -----------------> source bytes and live document unchanged
```

Recognize mixed-case native input where host semantics require it. Route open,
drag/drop, recent, command-line/direct-open and shell-open consistently. A
corrupt `.cedg` fails closed without partial publication or current-document
replacement.

Retain the existing ZIP/XML entries, XML format and `app="classic"`. A suffix
routes I/O only. The external-upstream boundary remains explicit and no
downgrade is allowed.

# Geometric invariants and degeneracies

All approved Locus V2 semantic invariants remain unchanged. Presentation cannot
change semantic identity or revision. Genuine disconnected components, invalid
intervals, open/unbounded endpoint presentation and periodic closure keep their
existing truthful behavior. A crossing is not a degeneration. A visually
covered pixel is not a locus gap. No renderer repair may bridge an actual
semantic discontinuity.

# Compatibility and serialization

- `.cedg` and `.ggb` use the current validated ZIP/XML reader.
- Native writes retain `app_code: classic` and current semantic XML versions.
- `.ggb` is input, not a normal Save target; its source hash must remain stable
  through native save.
- GeoCeDG Classic opens and preserves/recomputes/saves an opened `.cedg` with
  the same native types while keeping separate preferences, creation policy
  and its pre-R2 default new-document identity. It must not downgrade `.cedg`.
- External upstream may reject `.cedg` or unknown types; record the boundary
  and do not convert.
- Old `.ggb`, legacy Locus, G9 durable identity and current package behavior
  must regress without silent changes beyond the accepted `.cedg` association.

# Required tests and commands

Implement every `R2-L*`, `R2-D*` and `R2-R*` row in
`docs/validation/g9_public_workspace_validation_matrix.md`.

At minimum test:

- color, thickness, line type, show/hide, applicable label presentation,
  Properties exposure, normal selection/highlight, style persistence, copy and
  applicable undo/redo;
- semantic revision/identity/domain/branch/metric/intersection invariance for
  every style mutation;
- crossing line, crossing circle/conic, unchanged render subpaths and preserved
  genuine discontinuity;
- default Save, Save As, omitted extension, reopen, mixed case, recent,
  drag/drop and direct-open;
- legacy `.ggb` open, non-destructive native save, unchanged source hash,
  cancellation and write failure;
- corrupt `.cedg`, Locus V2, rich result/token and G9 durable identity round
  trips;
- GeoCeDG Classic `.cedg` preservation/default-identity boundary and
  external-upstream messaging;
- Windows `.cedg` association and absence of a GeoCeDG `.ggb` claim where
  testable, without freezing MIME metadata absent an implementation need; and
- G9U0-R1, historical G9U0, G9X1, G5, relevant G9A, legacy Locus, packaging
  contracts and full composed verification.

Run the new focused verifier twice from identical fixtures and compare
canonical evidence. Compare normalized archive entries/canonical XML and entry
hashes; do not claim byte-identical ZIPs unless timestamps/metadata are proven
deterministic. Then run `tools/agent/verify.ps1` without weakening existing
gates. Save exact commands, exit codes and log paths.

Prepare but do not self-complete the manual author smoke checklist in the design
and validation matrix.

# Required artifacts

Produce:

- minimum source/tests/resources/package-profile changes;
- `tools/agent/verify-g9u0-r2-product-refinement.ps1`;
- the paired G9U0-R2 integration block in `tools/agent/verify.ps1`, after G9X1
  and before any G9U1 block;
- `geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-scenarios.json`;
- `geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-evidence.json`;
- `geocedg/validation/g9u0-r2/g9u0-r2-evidence.sha256`;
- `geocedg/validation/g9u0-r2/g9u0-r2-document-compatibility-corpus.json` and
  its `.sha256` manifest;
- `docs/architecture/g9u0_r2_product_refinement_implementation.md`;
- `docs/validation/g9u0_r2_product_refinement_implementation_candidate_report.md`;
- exact `docs/upstream/modified-files.yml` updates;
- current user/developer/packaging documentation reflecting only validated
  observable behavior; and
- generated logs below `artifacts/g9u0-r2/`.

The evidence/report must retain
`selfApproved=false`, `authorApproved=false`, and `passClaimed=false`.

# Stop conditions

Stop immediately and report to the author if:

- the clean/approved entry state or explicit implementation authorization is
  missing;
- the accepted spec/ADR or annotated planning authority is missing or drifted;
- ordinary Locus style requires generic `Path`, a parallel style store or a
  semantic revision change;
- crossing continuity requires consulting another drawable or merging a
  genuine semantic component;
- `.cedg` requires a new archive/XML format or app code;
- safe Save cannot keep an opened `.ggb` source byte-for-byte unchanged;
- corrupt/native open cannot fail without damaging the live document;
- filename routing would infer or migrate semantic identity;
- Classic preservation or external-boundary messaging would require downgrade;
- association/resources introduce unresolved ownership/licensing questions;
- deterministic evidence cannot be reproduced;
- an existing focused/composed regression fails after environment failures have
  been classified; or
- completion would require any G9U1, G9B, G9C, G9U2 or G10 implementation.

# Terminal declaration

After implementation and automated verification, stop with exactly this phase
meaning and wait for the author:

```text
G9U0-R2 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

Do not convert the candidate to PASS, change roadmap approval state, tag, merge,
push or continue into G9U1. STOP FOR AUTHOR REVIEW.
