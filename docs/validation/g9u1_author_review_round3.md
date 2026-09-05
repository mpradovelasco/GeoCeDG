# G9U1 author-review stabilization — Round 3

Status: `TECHNICAL STABILIZATION CANDIDATE — VALIDATION/AUTHOR CLOSEOUT PENDING`

This record starts from Round-2 commit
`5f492d4ee77289d9def89aa6ed431226d2de3457`. It does not replace the
author-owned re-smoke checklist, and it does not turn any author PASS/FAIL entry
into agent-executed evidence.

## Entry evidence

- Product `main`: `f8a21a087234b18fc13741a0ac2baf80608e9022`, equal locally,
  on `origin/main`, and on the live `origin` remote.
- Round-2 branch/commit: `codex/g9u1-author-review-stabilization-2` at
  `5f492d4ee77289d9def89aa6ed431226d2de3457`, published with divergence 0/0.
- Round-3 successor branch:
  `codex/g9u1-author-review-stabilization-3` from the exact Round-2 commit.
- `Revision2.cedg`: 17,209 bytes, SHA-256
  `527f96b516afdd93923e228ad0ffe0a3fc0bebd79e30b228bec7e7455ed53ab6`.
- `Revision3.cedg`: 14,110 bytes, SHA-256
  `351955499d47d0407ab11c906da6e9b6d2ab636b0beef4e67c3edfddecccd939`.
- `helixTopBar.png`: 113,783 bytes, SHA-256
  `08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1`.
- `helixSnapshot.png`: 251,689 bytes, SHA-256
  `abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637`.

The four inputs above remain ignored author evidence. Accepted branding bytes
will be copied or deterministically derived into tracked product resources; the
author originals and saved smoke files are not edited.

## Disposition matrix before source mutation

This table preserves the initial pre-implementation diagnosis and proposed
owner exactly as design chronology. The `kesc` instrumentation hypothesis was
later superseded by the bounded startup analysis recorded below; it is not the
final Round-3 contract.

| Finding | Reproduction | Owning layer and authority | Classification / minimal correction | Required evidence and authority impact | Stop condition |
|---|---|---|---|---|---|
| Ordinary Algebra editing of `kesc` | **REPRODUCED** from the real `Revision3.cedg`. Loading outside Swing EDT and editing through double-click/F2/row editor on EDT throws the thread-confined spatial-publication instrumentation exception; direct/free-input compatible redefine succeeds and retains the same numeric ID. | Shared lifecycle/diagnostic instrumentation used by the approved G9A compatible-redefine transaction; Algebra remains the user gesture owner. | **G9U1-BLOCKER.** Make the publication-lease instrumentation valid across the application's legitimate load/edit threads without weakening G9A atomicity, identity or fail-closed checks. No Dilate or Locus/Spline semantic change. | Real archive regression for double-click, F2 and row edit; 1→0→.25→-1→1; same reference/ID/count; dependent result; undo/redo/reopen. Document the actual cause, superseding the Round-2 update-order diagnosis while retaining it historically. | Stop if correction requires loosening the G9A compatibility predicate, removing publication checks, or moving identity authority into Desktop UI. |
| Frame icon and startup image | **REPRODUCED**: both author files exist only in the primary ignored ingestion area; `AppGeoCeDG.getFrameIcon()` returns null and GeoCeDG forces host splash off. | Desktop/application resources and Windows packaging; assets manifest and licensing/provenance records. | **G9U1-ACCEPTANCE.** Promote byte-exact sources and deterministic, aspect-preserving derivatives; wire frame icon, product splash and jpackage icon. | Hash/dimension/provenance and deterministic-derivative tests; runtime resource decode; packaging profile/schema/verifier. Supersede only the earlier no-author-asset limitation. | Stop if source hashes differ, provenance cannot be recorded, derivation is nondeterministic, or public redistribution would be inferred from internal author authorization. |
| Installed persistent tool versus embedded reconstruction macro | **REPRODUCED by current lifecycle/code contract**; exact post-reopen dynamic instance is not in the in-memory installed-instance map and is treated as a conflicting local command even when definitions are equivalent. | Application macro host/library plus existing document macro persistence. | **G9U1-BLOCKER.** Add stable definition provenance/equivalence, keep installed package user-facing, keep embedded definition for portable reconstruction, and fail closed on mismatch. Do not add a macro engine. | Install/invoke/save/reopen with package present; no duplicate; uninstall/reopen portability; mismatch negative; undo/redo/save-after-undo. Update user-tool review/spec and provenance format. | Stop if equivalence would rely on label/reference/order, if `.cedg` would depend on application preferences, or if a new kernel macro format is required. |
| Semantic-curve menu and explicit semantic-point wording | **REPRODUCED** in schema-v2 profile: Locus V2 and Spline V2 are separate presentation clusters and the shared action has the old Locus-only label. | Application profile and Desktop presentation; R6 is semantic authority. | **G9U1-ACCEPTANCE.** One `Semantic curves` presentation group and generalized label/help, retaining the same action ID and command seam. | Profile/compiler/menu/EN-ES/help tests; workspace/spec/guide update. | Stop if a second point-identity implementation or stable action is needed. |
| Circles/conics label | **REPRODUCED**: the group contains circle/conic actions but retains `and curves`. | Profile presentation only. | **G9U1-ACCEPTANCE.** Rename visual group to `Circles and conics` / `Círculos y cónicas`. | Profile/localization/menu tests and docs. | Stop if inventory contains a semantic curve that makes the label false; classify it before moving. |
| Long Linear geometry group | **REPRODUCED**: one 16-action cluster is rendered as one submenu/flyout. | Profile presentation; taxonomy remains the approved 18-cluster authority. | **G9U1-ACCEPTANCE.** Add ordered presentation groups `Lines and vectors`, `Polygons`, `Derived constructions` inside the same profile; do not change stable IDs or semantic clusters. | Exact membership/order tests and menu/toolbar parity. Schema/spec update because taxonomy and visual grouping become explicit separate concepts. | Stop if the only solution is a second hard-coded toolbar/menu authority. |
| Text/Image under View | **REPRODUCED** in the presentation cluster currently emitted under View. | Profile/Desktop menu placement. | **G9U1-ACCEPTANCE.** Place both construction actions in `Annotations and media`; retain their IDs and DAG behavior. | Menu reachability/no-duplicate tests and docs. | Stop if any view-only action is accidentally moved with them. |
| Construction navigation visibility action | **REPRODUCED at adapter level**: current action toggles an aggregate host flag, while the visible control is view-specific and its label is ambiguous. Runtime verification remains required. | Desktop presentation adapter over existing host construction-protocol-navigation state. | **G9U1-ACCEPTANCE.** Use the concrete Graphics navigation-bar seam, truthful show/hide label, checked state, zero construction/undo mutation. | Live state/UI test, menu rebuild/localization test, document-count/undo invariant. | Stop if host state cannot be addressed without new layout persistence. |
| Additional compatible host views | **PARTLY REPRODUCED**: host toggle actions/state exist but schema-v2 View has no professional `Views` surface. | Desktop view/layout only; no spatial geometric authority. | **G9U1-ACCEPTANCE.** Expose only safe host visibility toggles through a profile-declared presentation group; reuse host state. | Toggle/checked-state/menu tests and default-layout invariance. | Stop for any view requiring new semantic authority or a material Classic behavior change. |
| File/Edit nesting and separators | **REPRODUCED**: each top-level menu is currently rendered from cluster submenus, including single unnecessary levels. | Same profile action authority and Desktop menu renderer. | **G9U1-ACCEPTANCE.** Add typed, ordered presentation entries/groups/separators to schema-v2; flatten File/Edit while taxonomy clusters remain intact. | Schema/compiler/no-duplicate/exact-structure tests; seven-menu invariant. | Stop if visual ordering must be hard-coded outside the profile. |
| Options professional surface | **REPRODUCED**: algebra modes are direct entries; no Sort by/font/labeling/rounding/save-settings groups; `view.properties` opens object properties and can imply selection. | Desktop preferences over existing upstream `AlgebraStyle`, Algebra sort, font, labeling, rounding and preferences persistence. | **G9U1-ACCEPTANCE.** Profile-declared Options groups; real radio/check state; reuse upstream settings; global preferences route when no explicit object selection. Keep Continuity OFF and EN/ES restriction. | State/rebuild/localization/no-undo tests; global-dialog selection-invariance negative; Classic separation. Specs/profile/schema updated. | Stop if a duplicate preference store, new geometric state, or arbitrary object selection would be required. |
| Toolbar semantic organization and Input Help | **REPRODUCED**: toolbar compiler groups by broad family rather than final Construction presentation; persistent tools are oversized text buttons. Input Help is currently outside the tool containers and must remain there. | Profile compiler and Desktop Swing toolbar/application preferences. | **G9U1-ACCEPTANCE.** Compile the toolbar from the same final semantic presentation groups; preserve native flyouts; normalize user-tool footprint; assert Input Help remains to the right. | Menu/toolbar group equivalence, sizing/group popup, Input Help layout regression. | Stop if a parallel taxonomy or new document state is introduced. |
| Optional icon for pinned persistent tool | **REPRODUCED as absent**; current preference record stores pin/group/order only. | Application preference/library UI, not `.cedg` or construction. | **G9U1-ACCEPTANCE.** PNG-only bounded ingestion; size/dimension validation; SHA-256; deterministic contain/padding derivative; application-owned storage; reference-aware cleanup. Icon never changes tool identity. | Positive/negative media tests; restart/group/order/icon persistence; unpin/remove/change cleanup; construction/undo byte invariance. | Stop if safe persistence requires document mutation or if provenance cannot distinguish payloads. |
| Input Help placement | **NOT REPRODUCED as currently broken**, but regression risk exists because toolbar restructuring changes widths/groups. | Desktop layout presentation. | **G9U1-ACCEPTANCE regression only.** Preserve the existing east/right placement; no new feature. | Component-order/layout test at normal and scaled dimensions where feasible. | Stop if correcting it requires an unrelated layout redesign. |

## Final `kesc` root-cause correction

The exception observed after loading `Revision3.cedg` was a downstream symptom,
but the proposed change to `SpatialSemanticInstrumentation` was not accepted or
retained. The final trace showed that GeoCeDG startup created the Construction
and metric owner on the launcher thread, whereas the normal Algebra gestures
run on Swing EDT.

The bounded correction belongs to the Desktop product-startup seam. The
GeoCeDG-specific three-argument `GeoGebra.doMain(...)` overload prepares the
tracked product splash first, then synchronously executes frame/application
construction and `GeoGebraFrame.init(...)` on Swing EDT. The existing
two-argument Classic launcher and the kernel's publication/thread-confinement
contract remain unchanged. No Round-3 source or test delta remains in
`SpatialSemanticInstrumentation`.

The acceptance perimeter is independent of ignored author input: a clean
deterministic fixture exercises ROW, DOUBLE_CLICK, F2 and FREE_INPUT across
`1 -> 0 -> 0.25 -> -1 -> 1`, with the same `GeoNumeric` reference/durable ID,
defined Dilate dependencies, undo/redo and native reopen. A separate diagnostic
reuses the byte-sealed ignored `Revision3.cedg` when it is available and covers
the same four routes without turning that author file into clean-clone
authority. The initial failed reproduction remains historical evidence; it is
not relabeled as execution on the corrected cohort.

## Architectural decision before implementation

The approved counts remain a taxonomy contract: 110 stable actions, 18
operational clusters and 11 professional families. Round 3 needs a separate
*presentation structure*, but not a separate authority. The schema-v2 profile
will therefore remain the sole declaration and may gain typed ordered menu and
toolbar presentation entries that reference existing stable action IDs. The
compiler must prove exact reachability, uniqueness, semantic group membership
and menu/toolbar parity. Java must consume that compiled structure rather than
encode a competing taxonomy.

No G7/G8/G9S1/R4/R5/R6 geometric contract is reopened by this disposition.
Any finding that crosses the stated stop conditions remains a blocker for author
review rather than being hidden in frontend code.
