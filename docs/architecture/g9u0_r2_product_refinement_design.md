# G9U0-R2 — pre-G9U1 product / document refinement design authority

| Field | Value |
|---|---|
| Status | **PLANNING / DESIGN = PASS — AUTHOR APPROVED** |
| Date | 2026-08-21 |
| Repository head inspected | `a107410c8b77b1de2cc188ecbb21d1a965a892cc` |
| Entry authority | G9U0-R1 and G9X1 `PASS — AUTHOR APPROVED` |
| Planning closeout tag | annotated `geocedg-g9u0-r2-planning-pass`; distinct from the reserved future implementation tag |
| Implementation | **authorization required / not started** |
| Approval provenance | author decision; planning self-approval `false` |

## 1. Outcome and selected identifier

The smallest identifier consistent with the living GeoCeDG conventions is
**G9U0-R2**.

| Candidate | Disposition | Reason |
|---|---|---|
| refinement of G9U0: `G9U0-R2` | **selected** | `G7A-R1`, `G8B-R1`, `G9P-R1` and `G9U0-R1` establish `-R<n>` for a bounded refinement of an existing gate; R2 is the next unused identifier and completes the public-product boundary without renumbering later phases |
| independent pre-U1 family | rejected | no existing roadmap family supplies a smaller meaningful identifier; creating `G9D`, `G9F` or a decimal phase would add a new track for two bounded integration concerns |
| `G9U1A` or another U1 slice | rejected | `G9U1A` already names the approved schema/compiler slice inside the future workspace contract; using it would make this work part of G9U1 and risk implying its authorization |
| `G9U1-R1` | rejected | G9U1 has not executed or closed, so it has no result to refine |
| `G9X2` | rejected | native documents and Locus presentation are not extensions of the DXF export track |

The `R2` suffix records lineage; it does not make this an informal amendment.
G9U0-R2 is a formal independent execution gate with its own normative specs,
Accepted ADR, prompt, focused verifier, evidence, implementation-candidate review and
author PASS decision. Its entry order also requires the already closed G9X1.

## 2. Exact roadmap insertion and ordering

The approved design changes the product-track sequence to:

```text
G9U0-R1 PASS --+
                 +--> G9U0-R2 --> G9U1
G9X1 PASS -------+      (new gate)

G9A3 PASS --> G9B --> G9C -----------------------> G9 global closeout
                      (independent of R2/U1 execution)
```

The low-conflict schedule becomes:

```text
G9O1; G9A1; G9A2; G9A3; G9U0; G9U0-R1; G9X1;
G9U0-R2; G9U1; G9B; G9C; G9 global closeout; G9U2
```

Semicolons are execution order, not automatically semantic arrows. The new
hard product gate is:

```text
G9U0-R2 IMPLEMENTATION PASS — AUTHOR APPROVED
  is required before an author may authorize G9U1 execution
```

G9U0-R2 implementation PASS would not itself authorize or start G9U1. G9B and G9C remain
separately authorizable from their approved G9A3/kernel contracts, G9U2 remains
blocked on global G9 PASS, and productive G10 remains unauthorized.

## 3. Dependencies, entry gate and exclusions

### Required entry state

- `HEAD`, `main` and `origin/main` must resolve to the author-selected clean
  base when the future prompt is invoked.
- G9U0, G9U0-R1 and G9X1 must reproduce as `PASS — AUTHOR APPROVED` from their
  frozen authorities.
- G9A1–A3 and G9A durable identity/persistence regressions must remain green.
- Accepted ADR 0016 and both normative G9U0-R2 specifications must remain the
  approved design authority.
- a separate author instruction must invoke the canonical implementation
  prompt; design approval alone does not start implementation.

G9X1 is a mandatory phase-order/product-integration predecessor, not the
semantic authority for documents or Locus styles. The Locus presentation work
depends semantically on G9U0-R1; the native document work depends on the G2/G4
product boundary and the G9A/G9U0 persistence substrate.

### Included scope

1. Ordinary Locus V2 visual style, show/hide, applicable label presentation,
   Properties and selection/highlight integration.
2. Render continuity through crossings with unrelated graphical objects.
3. Native `.cedg` document I/O identity and non-destructive `.ggb` input
   transition.
4. GeoCeDG Classic `.cedg` preservation without creation/default-identity
   expansion, recent/direct-open and Windows-only association behavior required
   by that identity.
5. Focused and composed verification, evidence, upstream-impact registration
   and user/developer guide closeout for the future implementation.

### Excluded scope

- all mathematical Locus V2 semantics, metrics and intersections;
- generic `Path` promotion or legacy Locus changes;
- a parallel style model or semantic render cache;
- a new ZIP/XML format or `app` code without a separate accepted decision;
- a `.ggb` compatibility exporter or lossy downgrade path;
- workspace/profile schema v2, action catalog or G9U1 implementation;
- G9B, G9C, G9U2, productive G10 or public-release authorization; and
- any guarantee that an external upstream distribution opens GeoCeDG-only
  persisted types.

## 4. Current repository evidence

### Locus V2 presentation seam

The current source already has the ordinary visual-style substrate:

- `GeoLocusV2` extends `GeoElement`;
- its supported copy shell receives `setVisualStyle(this)`;
- public XML delegates to `GeoElement.getXML(...)`;
- `DrawLocusV2` calls `updateStrokes(locus)`, resets its path with the locus
  thickness and draws using the ordinary object color and stroke; and
- `LocusRenderCache2D` keys derived data by semantic revision and view policy,
  then starts subpaths from branches, valid components and invalid evaluation,
  not from other graphical objects.

The missing Properties exposure is a narrow host capability mismatch:
`GeoElement.showLineProperties()` defaults to `isPath()`, while the approved
Locus V2 contract deliberately excludes generic `Path`. That evidence supports
a bounded applicability seam; it does not support changing semantic type.

Existing render-separation tests already show that changing tessellation/view
policy preserves semantic revision and that disconnected valid components
produce separate subpaths. G9U0-R2 adds the ordinary-style and unrelated-object
crossing cases without altering those foundations.

### Document and serialization seam

The current extension is centralized incompletely but the archive machinery is
already suffix-independent:

- `FileExtensions` has `GEOGEBRA("ggb")` and no GeoCeDG document value;
- Desktop Save As uses only `FileExtensions.GEOGEBRA`;
- open dialogs, drag/drop and omitted-extension fallback enumerate `.ggb`;
- command-line/direct open eventually passes an ordinary file stream to
  `loadXML`;
- `GFileHandler` reads ZIP/XML content from that stream;
- `MyXMLioJre.writeGeoGebraFile(File)` writes the normal ZIP archive to the
  supplied path without branching on its suffix;
- `AppConfigGeoCeDG.getAppCode()` returns `classic`, and `MyXMLio` writes that
  value into the XML header; and
- Windows package sources currently declare a `.ggb` association with the
  upstream MIME value.

This is affirmative evidence for retaining the validated archive/XML internals
and changing only product-level routing/identity. It is not evidence for a new
serializer.

## 5. Architectural contract

### Ownership

| Concern | Authority/owner | Must not own |
|---|---|---|
| Locus color/thickness/line type/visibility/label | ordinary `GeoElement` visual style and presentation | semantic definition/revision or parallel GeoCeDG style |
| selection/highlight | ordinary transient host/view presentation | persisted style, identity or render-subpath authority |
| Properties applicability | narrow presentation capability consumed by existing property models | generic path membership |
| render vertices/subpaths | `LocusRenderCache2D`, derived from semantic components and view policy | metric/intersection/identity truth or other drawables |
| `.cedg`/`.ggb` save state | GeoCeDG application/document I/O policy | geometry, IDs or XML semantic migration |
| ZIP/XML content | existing validated shared/JRE serialization machinery | filename/product association policy |
| recent/direct open | Desktop/application adapters | semantic type inference |
| Windows association | GeoCeDG package profile and release scripts | archive parser or geometric semantics |
| workspace v2 | later G9U1 manifest/controller | native-document or Locus semantic authority |

### Locus V2 contract

The normative details live in
`geocedg/specs/locus/locus-v2-presentation.md`. In summary:

- reuse ordinary visual-style authority;
- expose only supported curve-style controls;
- reuse ordinary show/hide, applicable label and selection/highlight behavior;
- keep style presentation-only and semantic revision/generator/domain/branch/
  metric/intersection/token/DAG authority invariant;
- preserve styles through normal XML, copy and undo/redo contracts;
- keep tessellation derived and disposable;
- keep `startsSubpath` independent of unrelated objects; and
- distinguish semantic gaps/components from clipping, z-order, overdraw and
  intentional dash gaps.

### Native document contract

The normative details live in
`geocedg/specs/ui/native-document-identity.md`. The required policy is:

```text
.cedg = native GeoCeDG Save/Save As/reopen identity
.ggb  = compatibility/input identity
archive/XML/app code = current validated machinery (`classic`)
filename = I/O routing only, never semantic inference
```

An unsaved document defaults to `.cedg`. Save on a native `.cedg` continues on
that path. Save on an opened `.ggb` must invoke native Save As and cannot write
the source. On success the new `.cedg` becomes current; on cancel/failure the
source and in-memory construction remain unchanged.

The separate GeoCeDG Classic diagnostic route must open and preserve an opened
`.cedg` without downgrade or creation enablement. That preservation capability
does not change Classic's default new-document identity or impose the normal
GeoCeDG `.ggb`-to-`.cedg` Save As transition on that diagnostic route.

## 6. Why `app_code: classic` should remain

The approved answer is **retain it unchanged**.

The extension supplies product/document identity without requiring a new XML
semantic discriminator. Existing readers already interpret `classic`; the
GeoCeDG semantic types carry their own versioned XML contracts; and a new app
code would add parser/config/migration surface without solving a demonstrated
problem. ADR 0001 explicitly required a separate compatibility decision before
changing it. Accepted ADR 0016 makes that decision explicit: introduce the
native extension while preserving the app code.

If future implementation uncovers content that cannot be represented or
reloaded under the existing header, that is a STOP condition and evidence for
a new ADR, not permission for G9U0-R2 to improvise a format.

## 7. Normative, explanatory and evidence effects

### Approved normative authorities

- Accepted ADR 0016;
- normative native-document specification;
- normative bounded Locus V2 presentation specification; and
- the approved workspace supersession record making G9U0-R2 implementation
  PASS an entry condition and replacing `.ggb`-native wording.

These are normative design authorities, not implementation claims. Existing
accepted G9U0, G9U0-R1, G9X1, G9A and mathematical Locus contracts remain
unchanged.

### Explanatory/living documentation

- this design and the living roadmap record the approved planning/order;
- workspace and documentation architecture record the approved future boundary;
- `g9p_integrated_plan.md` receives a post-closeout authority addendum rather
  than a rewrite of its approved historical plan; and
- current user/developer guides continue to describe observable `.ggb`
  behavior until G9U0-R2 is implemented and approved.

### Future implementation evidence

Generated logs, screenshots, sample documents, registry probes and evidence
JSON will prove an implementation candidate. They will not become source
authority and may not claim author approval.

## 8. Exact G9U1 normative supersession required

The accepted workspace design remains authoritative for presentation, but the
following portions cannot govern G9U1 execution unchanged:

| Current authority | Portion | Required successor wording/effect |
|---|---|---|
| Accepted ADR 0012 | decisions 5 and 7, `.ggb`-perspective consequence and acceptance record | ADR 0012 remains Accepted for separate Classic/presentation-only document layout; Accepted ADR 0016 supersedes only any reading that `.ggb` remains GeoCeDG's future native identity and adds R2 implementation PASS as a U1 entry gate; do not rewrite the historical ADR |
| `geocedg/specs/ui/cedg-workspaces.md` | approved supersession record, metadata/entry gate and §12 staging | require `G9U0-R2 IMPLEMENTATION PASS — AUTHOR APPROVED` before G9U1 implementation, followed by separate U1 authorization |
| same | §4 schema sketch and §4.3 item 6/7 | clarify that unchanged `serialization` means unchanged ZIP/XML and `app_code: classic`, not continued `.ggb` native identity |
| same | §4.3 sentence “migration must not write user files or `.ggb` files” | generalize to no document-file writes during profile migration, covering native `.cedg` and compatibility `.ggb` |
| same | §9 Persistence and document layout | replace `.ggb`-native assumptions with native `.cedg`; retain `.ggb` document layout as compatibility input; keep workspace preferences outside document semantics |
| same | §10 Classic boundary | require direct `.cedg` open/preservation with no downgrade or creation enablement, without changing Classic's default new-document identity merely because it accepts `.cedg` |
| same | §13 closeout decisions | add native-document behavior and R2 entry evidence without changing workspace purity |
| frozen G9U1 canonical prompt | Mandatory entry gate / Hard dependencies / Recommended predecessor | future successor adds G9U0-R2 implementation author-approved PASS after G9X1 and makes clear it does not auto-authorize U1 |
| same prompt | Compatibility and serialization | define `.cedg` native, `.ggb` input, unchanged archive/app code and no source overwrite/downgrade |
| same prompt | Required tests and commands | replace “saved GGB precedence” as the sole document case with `.cedg` native plus `.ggb` compatibility scenarios |
| `docs/validation/g9_public_workspace_validation_matrix.md` | E-10, U1-W09/U1-W10/U1-W11/U1-W17 and U1 exit | require the R2 implementation gate; test native/compatibility layouts and the narrower Classic preservation/default-identity boundary |

The current G9U1 prompt is hash-listed in the frozen G9P catalog and must not be
silently edited as historical evidence. Before G9U1 execution, publish an
explicit superseding canonical prompt (planned path
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r2.prompt.md`)
and update the living prompt authority without modifying the frozen G9P record.

## 9. Operational-layer integration

### Canonical implementation prompt

The new future prompt is
`.github/prompts/tasks/g9u0-r2-product-refinement.prompt.md`. It is checked in
as an author-approved canonical prompt whose execution is unexecuted and
unauthorized. It requires accepted
specs/ADR, a clean authorized entry base, focused/composed reproduction and
explicit STOP conditions. It terminates at:

```text
G9U0-R2 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false
G9U1 = DESIGNED — NOT AUTHORIZED
```

### Future focused verifier

Planned executable authority:

```text
tools/agent/verify-g9u0-r2-product-refinement.ps1
```

Responsibilities:

1. verify the approved prompt/spec/ADR blobs from the annotated
   `geocedg-g9u0-r2-planning-pass` authority and its required ancestor/tag
   state;
2. enforce the exact modified-file inventory and reject scope outside R2;
3. run the focused shared/Desktop style, render and document-I/O tests;
4. run Windows association/package static tests and executable packaging probes
   when artifacts/toolchain are explicitly requested;
5. rerun the focused scenarios twice and compare canonical evidence;
6. run G9U0-R1, historical G9U0, G9X1, G5, relevant G9A and legacy Locus
   regressions;
7. assert no semantic revision/metric/intersection drift and no `.ggb` source
   mutation;
8. validate candidate booleans (`selfApproved=false`,
   `authorApproved=false`, `passClaimed=false`); and
9. emit logs only under the requested `artifacts/g9u0-r2/...` root.

### Future composed integration

`tools/agent/verify.ps1` must gain the R2 verifier variable and one paired-
artifact integration block **after the existing G9X1 block and before any
future G9U1 block**. The block must require all planned evidence/support files
or none, reject partial integration, and follow the existing inventory-state
pattern:

- `OPEN_PENDING_IMPLEMENTATION_FREEZE`: run static/scaffold validation only and
  explicitly say the productive gate was not executed;
- `FROZEN`: require recorded focused PASS, matching deterministic rerun,
  regressions, Checkstyle/static/diff PASS, then execute the focused verifier;
- any other state: fail closed.

The composed verifier must not interpret the presence of planning files or a
green scaffold as R2 PASS. A future G9U1 composed block must refuse to execute
unless R2 implementation's author-approved closeout authority is present.

This planning closeout also applies one bounded compatibility correction to
the existing historical G9U0 verifier: its G9P-era validation-matrix hash is
checked against the matrix blob at the approved G9U0 promotion commit, using
the repository's existing frozen-evidence helper. The approved hash and G9U0
evidence remain unchanged while the single living matrix can acquire the R2
design rows. This is not an R2 verifier, product test or dummy PASS gate, and
`tools/agent/verify.ps1` receives no planning-only R2 execution block.

## 10. Validation matrix and deterministic evidence

The complete author-approved design matrix is integrated into the existing
`docs/validation/g9_public_workspace_validation_matrix.md`; no second matrix or
verifier architecture is created.

Deterministic rerun compares:

- semantic/durable IDs and revisions;
- branch/component and `startsSubpath` evidence;
- supported persistent visual-style, visibility and applicable label XML fields;
- normalized ZIP entry names/order-independent entry hashes and canonical XML;
- `.ggb` source hash before/after native save;
- recent/direct-open state transitions;
- test/scenario counts, counters and typed diagnostics; and
- Windows package profile/association output when applicable.

It must not demand byte-identical ZIP archives merely because filenames differ
or entry timestamps are not normalized. Any byte-identity claim requires its
own proven writer policy.

### Manual author smoke plan

After automated candidate verification, the author should:

1. launch GeoCeDG with the existing explicit Locus V2 feature opt-in;
2. create the approved circle-driven Locus V2 workflow;
3. open Properties and change color, thickness, line type, show/hide and
   applicable label presentation; exercise selection/highlight;
4. move the construction and confirm continuous rendering;
5. cross the locus with a line and then a circle/conic, inspecting that no
   artificial gap/subpath appears;
6. undo/redo a style change and copy the locus where supported;
7. Save As with an omitted extension, confirm `.cedg`, reopen and inspect
   style, Locus/rich/token and identity preservation;
8. open a copied legacy `.ggb`, invoke Save, choose a new `.cedg`, and compare
   the source hash/path before and after;
9. open/save/reopen `.cedg` in GeoCeDG Classic diagnostic mode, confirm its
   default new-document identity and creation policy remain unchanged, and
   verify a corrupt `.cedg` fails without replacing the live document; and
10. when an installer artifact is explicitly built, inspect `.cedg` shell open
    and confirm GeoCeDG did not claim `.ggb` association.

Manual smoke results are author evidence. An agent may prepare the checklist
and logs but may not mark the smoke or R2 implementation approved.

## 11. Exact artifacts

### Files created/updated by this planning closeout

- `docs/roadmap/geocedg_roadmap.md`;
- `docs/architecture/g9u0_r2_product_refinement_design.md`;
- `docs/architecture/g9p_integrated_plan.md` (post-closeout addendum only);
- `docs/architecture/cedg_workspace_architecture.md`;
- `docs/architecture/geocedg_documentation_architecture.md`;
- `docs/adr/0016-native-geocedg-document-identity.md`;
- `geocedg/specs/locus/locus-v2-presentation.md`;
- `geocedg/specs/ui/native-document-identity.md`;
- `geocedg/specs/ui/cedg-workspaces.md` (approved supersession record only);
- `geocedg/specs/README.md`;
- `docs/validation/g9_public_workspace_validation_matrix.md`;
- `docs/validation/g9_documentation_bundle_traceability.md`; and
- `.github/prompts/tasks/g9u0-r2-product-refinement.prompt.md`; and
- `tools/agent/verify-g9u0-locus-v2-public-surface.ps1` (historical matrix
  authority resolved from the approved G9U0 commit, with no hash/evidence
  rewrite).

No Java, Desktop runtime, application manifest, packaging source, future R2
focused verifier, evidence JSON, user guide or developer guide is changed by
planning. The one existing-verifier edit above preserves historical authority
while permitting the approved living-matrix extension.

### Planned future implementation/candidate artifacts

- `tools/agent/verify-g9u0-r2-product-refinement.ps1`;
- the R2 integration block in `tools/agent/verify.ps1`;
- `geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-scenarios.json`;
- `geocedg/validation/g9u0-r2/g9u0-r2-product-refinement-evidence.json`;
- `geocedg/validation/g9u0-r2/g9u0-r2-evidence.sha256`;
- `geocedg/validation/g9u0-r2/g9u0-r2-document-compatibility-corpus.json`;
- `geocedg/validation/g9u0-r2/g9u0-r2-document-compatibility-corpus.sha256`;
- `docs/architecture/g9u0_r2_product_refinement_implementation.md`;
- `docs/validation/g9u0_r2_product_refinement_implementation_candidate_report.md`;
- future implementation source/tests/resources/package-profile changes within
  the approved boundary;
- updated `docs/upstream/modified-files.yml` entries for every added/modified
  `source/` path and its accepted authority;
- updated user and developer guides after behavior is genuinely observable;
  and
- the superseding G9U1 canonical prompt named in section 8, only after R2
  implementation closeout.

Generated logs belong under `artifacts/g9u0-r2/` and are not durable source.

## 12. Modified-files and upstream-impact policy

Planning changes only GeoCeDG-owned documentation/spec/prompt paths plus the
bounded historical-verifier correction recorded above. No inherited `source/`
path changes, so `docs/upstream/modified-files.yml` is not modified.

Future implementation must inventory every path relative to the authorized
base, distinguish inherited-upstream modifications from GeoCeDG-owned additions
and register every `source/` path in the existing upstream-impact record. The
expected minimal seams are the existing file-extension/open/save adapters,
GeoCeDG product override, Locus presentation applicability, focused tests and
package profile; broad upstream refactors, renames and serializer rewrites are
forbidden.

## 13. Documentation closeout obligations

Only after a verified implementation candidate exists must closeout update:

- the user guide's Save/Save As/open/recent/Classic/file-association instructions
  and Locus Properties/style behavior;
- the developer guide's file-extension routing, source seams, unchanged ZIP/XML
  and app-code boundary, tests and verifier invocation;
- repository map/traceability where new durable sources require it; and
- packaging documentation for `.cedg` association.

Until then, the living user guide correctly describes the observable `.ggb`
product and must not claim `.cedg` exists.

## 14. Author decisions incorporated and next required decision

The planning closeout records the author's explicit decisions:

1. `G9U0-R2 — PRE-G9U1 PRODUCT / DOCUMENT REFINEMENT` and its exact order are
   approved;
2. ADR 0016 and both R2 specifications are approved normative authorities;
3. `.cedg` is the future native GeoCeDG identity and `.ggb` compatibility input;
4. `app_code: classic` and the validated ZIP/XML internals remain;
5. GeoCeDG Classic must preserve `.cedg`, without thereby changing its default
   new-document identity or enabling disabled creation;
6. Windows MSI/EXE uses a GeoCeDG-owned `.cedg` ProgID, with no arbitrary MIME
   string frozen and no non-Windows association validation claim; and
7. planning/design is PASS while productive implementation remains not started
   and unauthorized.

The sole next phase decision is a separate author authorization to execute the
canonical G9U0-R2 implementation prompt. This closeout does not supply it.

## 15. Terminal state

```text
G9U0-R2 PLANNING / DESIGN = PASS — AUTHOR APPROVED
planningSelfApproved = false
planningAuthorApproved = true

G9U0-R2 IMPLEMENTATION = AUTHORIZATION REQUIRED — NOT STARTED
implementationAuthorized = false
implementationStarted = false
implementationSelfApproved = false
implementationPassClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

STOP. Await separate author authorization for G9U0-R2 implementation.
