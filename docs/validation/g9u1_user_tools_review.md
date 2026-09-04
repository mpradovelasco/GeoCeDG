# G9U1 persistent user-tool review

Status: **FRONTEND REVIEW CANDIDATE — PENDING AUTHOR RE-SMOKE**.
This is application/tool-library design and validation, not macro promotion,
new kernel semantics, or G9U1 approval.

## Source and provenance inspected

The complete request section 10, root `AGENTS.md`, Templatev7 manifest,
curation and complete derived tool inventory, controlled-integration spec,
ADR 0003, G3 report, both G9P workflow reports, workspace spec and ADR 0012
were read. The original archive was read without extraction or modification;
all 22 members were streamed and SHA-256 checked, and both document/macro XML
trees parsed. The 24 macro definitions and historical seven custom groups
match the inventory. XML structure is inspection evidence, not new geometry.

- Original: `models/legacy/template-v7/original/Templatev7.ggb`, 48,149 bytes.
- SHA-256: `f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`.
- Macro XML: 221,871 bytes,
  `f3744e131a627ba5e9bef092427930e6e7c1526a6d527b43c3b4f30e04fc94f4`.
- Document XML: 15,191 bytes,
  `fc456772249bd716d0960ee9c7756a7ff1a371fcef3f97c2366e9192d8ca226d`.
- Author: Manuel Prado-Velasco; source GeoGebra 5.2.879.0 Classic Desktop.
- Manifest redistribution review remains **blocked**. Embedded images have no
  blanket source-license clearance. **Zero Template tools/icons are bundled or
  automatically installed** by this continuation.

## Classification of all 24 tools

Class 1 = native replacement; 2 = ordinary primitive convenience; 3 = future
high-level application candidate; 4 = future DSL/procedure; 5 = Laboratory or
research only; 6 = not a G9U1 spatial/procedure authority. Class 2 identifies a
possible explicit user-import route, not validation/promotion of the historical
implementation. The original curation and its negative findings stay intact.

| Historical command | Class | Current disposition |
|---|---:|---|
| SplineLength | 1 | Native `SplineV2` and `Length`; sampled sum is not semantic metric |
| sheetISOAnLand | 5 | Laboratory sheet construction, not approved native sheet service |
| sheetISOAnVert | 5 | Laboratory sheet construction, not approved native sheet service |
| directDimension | 3 | Dimensioning composition needs its own presentation/standards characterization |
| SquarebyDiagonal | 2 | Explicit user-owned planar convenience over Circle/Line/Segment |
| CirclebyD | 2 | Explicit user-owned diameter wrapper over native Circle |
| EllipseAxis | 3 | Conic convenience candidate; no new native semantic command here |
| pointJump | 4/6 | Primitive transport may be manual; no G9U2 frame/procedure authority |
| PoliLineVisibility | 5 | Drawn solid/dashed portions, never geometric visibility |
| Perimeter | 1 | Native Circumference/Perimeter; native-name collision must reject import |
| axisDimension | 3 | Dimensioning candidate; no standards promotion |
| relCoor | 4/6 | No inference of spatial frame/projection relations |
| DuctSymbol | 5 | Presentation-only Laboratory symbol; no spatial duct identity |
| SymmSymbol | 5 | Presentation-only Laboratory symbol |
| listLength | 1 | Native `Length(L)` / rich `LocusLength(L)` |
| listLength12 | 1 | Native semantic `Length(L,P,Q)` / rich between metric |
| postLocus | 1 | Native Locus V2 definition/components/intersections; no sampled filtering authority |
| ellipseVisibility | 5 | Arc presentation, not geometric visibility |
| translationCoor | 4/6 | No G9U2 transport/projection semantics through macro convenience |
| circArcbyAngle | 2 | Explicit user-owned Rotate/arc convenience |
| dummyRotate | 1 | Native Rotate; any future procedural interpretation separate |
| conj2mainAxesEllipse | 3 | Characterized conic-axis composition candidate, not promoted |
| ellipseLength12 | 2 | Explicit user-owned arc plus native Length convenience |
| IFPositiveSelectPoint | 2 | Explicit user-owned If convenience; no identity selection inference |

The source groups 13–19 respectively contain symbols, presentation visibility,
sampled/native measures, planar/conic construction, dimensions/transport,
conditional selection and sheet setup. They inform frequency/grouping only.

## Actual host seams and before/after boundary

| Concern | Existing host | Bounded GeoCeDG adaptation |
|---|---|---|
| Engine | `Macro`, `MacroKernel`, `MacroManager`, `ModeMacro`, `Kernel.useMacro` | Reuse unchanged; no alternate evaluator |
| Exchange | `MyXMLioD` / `MyXMLioJre` macro XML and `.ggt`; `AppD.saveMacroFile` | Validate bounded `.ggt` input and preserve original bytes in application library |
| Management | `ToolManagerDialogD` / `ToolManagerDialogModel` operates current document macros | Preserve host document management; add explicit persistent-library manager |
| Entry action | `automation.manage-user-tools`, target `host.tools.manage` | Reuse ID, **110 stable actions unchanged** |
| Host preference snapshot | `GeoGebraPreferencesD` and portable variant save **all** current macros | Do not use that snapshot as evidence of explicit installation; separate GeoCeDG library preference |
| Startup/new document | Host may load snapshot macros | Library entries remain available as dynamic menu/pinned choices, without injecting definitions into a blank document |
| Install/remove/pin | Not separately application-owned | Change isolated application preference only; no construction/undo/document mutation |
| Invoke | Existing macro mode and ordinary AlgoMacro dependency | Explicit user activation imports validated tool into current document; invocation creates normal host outputs |
| Document macros | `.ggb`/`.cedg` may contain local macros | Never auto-install; local collisions reject library activation, never silent rename/override |

The library stores original `.ggt` bytes plus application metadata and digest.
Dynamic entries are not schema-v2 action declarations. Pinning is application
presentation state, displayed in a separate User Tools group. Product/native,
installed, document-local and legacy/Laboratory contexts are labelled separately.

Validation must precede any active-document registration: bounded ZIP/XML,
unambiguous macro declarations, recognized/allowed dependencies, no scripts or
unapproved semantic/presentation side effects, native command and macro-name
collisions, and current feature policy. Unsupported tools fail with a reason;
file loading is not a feature-enable loophole. Detached validation uses the host
macro machinery and must leave active construction XML, IDs, macros and settings
unchanged. Before activation recheck the exact current document name bindings;
names locate host command registrations, never geometric identity.

Host Tools Manage remains explicitly document-local. Export a user-owned tool
there as `.ggt`, then explicitly import it through the persistent manager. No
Template package promotion or automatic directory scan occurs.

## Bounded evidence and remaining author review

Focused tests cover install, reload/restart, new document, explicit invocation,
pin/unpin, menu discovery, remove and another restart; document-local tools do
not auto-install; native/installed/document collisions; unsupported/feature-off
dependencies; malformed archive and metadata digest; no active construction,
document XML, saved/undo state or Classic preference mutation from library
management. The root task owns canonical DEV/PHASE/FULL execution.

The first focused desktop DEV completed successfully: **21/21 user-tool cases**,
within **76/76 desktop cases**. It exercised the application library, native
macro parser/engine and isolated preference paths, not the historical Template
macros. Log:
`artifacts/g9u1-review-round1/dev-review-01/dev/ac7bee9915184992bd8dcaa2078ea8e8/dev-gradle.log`;
user-tool XML reports zero skipped/failures/errors and 3.694 seconds. This
execution initially represented the five native-name collision
cases as one parameterized method. Afterwards, those same five cases were
expressed as five individually named `@Test` methods because the canonical G9U1
verifier binds exact method names. That test-only naming adjustment is a new
input cohort and requires the final rerun; the prior DEV is not relabelled as
having executed the later form. The final root validation report owns that link.

The implemented library is restricted to bounded native planar `.ggt` packages
without scripts or semantic/spatial objects. ZIP/XML limits, an explicit 2D type
allowlist, command policy and a post-parse 3D guard precede activation. Original
package bytes and their digest are application preferences; no embedded icon is
promoted into product assets. Macro validation uses an isolated host MacroKernel
and macro table. Tests do not write real user preferences.

Document-local definitions remain local after save/reopen. If a reopened document
already owns a macro with an installed command name, the installed entry reports
a document collision rather than replacing it. Existing document tools remain
available through the inherited document tool manager. New macro authoring can
use Classic's existing Create Tool workflow, then export a user-owned `.ggt` and
install explicitly in GeoCeDG. No alternate tool engine was added.

## Multiple-window preference transaction review

The final read-only audit found a concrete stale-cache defect: two open windows
could load the same initial library, then one window's install/pin/remove could
replace the other window's later package without rechecking its command name.
This finding is retained; earlier single-window DEV did not establish concurrent
application-library safety.

The bounded correction uses a sibling `.lock` file and a nonblocking native
`FileChannel.tryLock` around each install/pin/remove transaction. Under that lock
the complete current library is reread and validated before applying changes;
the existing atomic replacement writes only the merged current state. A busy
library fails explicitly rather than blocking the EDT or overwriting another
writer. Malformed stored data is never partially published into the live map.
Menu/manager presentation and explicit activation reread current state as well;
a tool removed by another window cannot be activated from a stale menu entry.
The lock is application preference coordination, not `.cedg` geometric state.

Three new named diagnostic tests cover cross-window command collision,
preservation of other packages/pins/removal plus removed-tool activation, and
busy-lock rejection without replacing preference bytes. A host ownership
assertion was also added to the existing native-engine test because ToolManager
edits use `Macro.getKernel()`. The diagnostic DEV at
`artifacts/g9u1-review-round1/dev-user-tools-ownership-01/dev/57e3d1cd4b4a468c98537ac8be94cb54`
ran 24 cases: 23 passed, including all three preference-transaction regressions;
the ownership assertion failed because the registered Macro retained the
detached ValidationKernel rather than the application Kernel3D. That failure
is preserved as historical evidence, not relabelled successful.

The bounded repair keeps detached validation but explicitly registers the
validated macro-only XML through the active host parser at activation. No
document construction is cleared or reloaded. A completely present, owned
package is reused; a partially present package fails before parsing, so native
collision renaming cannot silently substitute a definition. Registration checks
the complete new macro set and its host ownership. Failure removes exactly the
new objects (including unexpected renamed registrations), retaining all previous
document macros. Existing tests now check owner-directed removal/re-activation,
partial-package rejection and injected parser/registration rollback. The class
still declares 24 tests. Execution of this repaired cohort remains pending the
root's next canonical DEV; no full ToolManager editing interoperability is
claimed from the earlier invocation-only evidence.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: existing JVM, ZIP/XML and isolated
preferences are sufficient. GUIDE_IMPACT — UPDATE_REQUIRED: the quick guide
must explain explicit installation versus document-local tools, removal and
pinning, and the unchanged Laboratory/provenance boundary.
