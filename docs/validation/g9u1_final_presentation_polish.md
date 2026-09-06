# G9U1 final presentation polish — technical-candidate disposition

Status: `IMPLEMENTATION ITERATION — AUTHOR CLOSEOUT NOT YET AUTHORIZED`

Entry authority is technical candidate
`56cf32c922baefeb30c7dff02dbdd5091107ea1a`. This bounded iteration changes
Desktop presentation and versioned resources only. It does not change geometric
semantics, construction persistence, the 110 stable action IDs, Classic policy,
or the author-owned re-smoke record.

## Pre-mutation disposition

| Finding | Current reproduction | Owner / authority | Classification and minimum change | Required evidence | Stop condition |
|---|---|---|---|---|---|
| Startup splash is oversized and may sit behind the launching terminal | **REPRODUCED**: the runtime derivative is `542 x 720`; `SplashWindow` calls `toFront()` before `setVisible(true)` and has no product foreground policy | Desktop startup presentation; tracked branding source plus deterministic derivative generator | **G9U1 acceptance polish**: derive a `361 x 480` splash (approximately two thirds in each dimension), preserve the byte-exact author source, and request foreground only from the GeoCeDG startup overload | source/derivative hashes and dimensions; deterministic regeneration; Classic-negative and foreground-policy tests | any need to change the author source, system-wide window policy, or startup semantics outside the product overload |
| `Semantic Locus V2` versus `Spline V2` is asymmetric | **REPRODUCED**: the catalog contains one Locus V2 creation action and one Spline V2 creation action; there is no second semantic Locus operation. The asymmetry comes from the inherited mode label | schema-v2 application profile and product action registry; Classic localization remains upstream-owned | **G9U1 acceptance polish**: profile-localize the existing Locus action as `Locus V2`, retain `Spline V2`, and create no duplicate action | exact 110-ID equality; EN/ES names/help; Classic mode text remains untouched | a second geometric operation is discovered or a common/kernel localization change would be required |
| Semantic-curve toolbar exposes Locus as a native mode and Spline as a detached product button | **REPRODUCED**: native toolbar grammar cannot mix a mode with the command action, so the generic product toolbar appends Spline separately | schema-v2 toolbar projection and Desktop presentation | **G9U1 acceptance polish**: mark the existing semantic group as one profile flyout and project Locus V2, Spline V2, and explicit-address Point on semantic curve through the same registered actions | exact group membership/order; popup action-object identity; no independent Spline button; inspector remains in the menu | any new mode, action ID, solver, or point-identity path is needed |
| Move flyout omits Move/Rotate around Point | **REPRODUCED**: `MODE_MOVE_ROTATE` is already catalogued but overflow-only | profile toolbar projection over the existing upstream mode | **G9U1 acceptance polish**: include it beside Move | group/order and duplicate tests | host mode is incompatible in GeoCeDG or requires new semantics |
| Point/intersection and navigation flyouts are incomplete | **REPRODUCED**: Point actions occupy a separate toolbar group; Attach/Detach, Zoom In/Out and Copy Visual Style are overflow-only | profile toolbar projection; existing upstream modes plus existing ZoomWindow product action | **G9U1 acceptance polish**: visually group Point, Point on Object, Attach/Detach, Intersect and Tangent; create one navigation flyout for Pan, ZoomWindow, Zoom In, Zoom Out and Copy Visual Style. Menu grouping is unchanged | group membership/order; all actions are the same registry objects; no duplicate toolbar placement; Input Help remains at the far right | any frontend identity inference or new navigation/style action is required |
| Linear construction is too concentrated/incomplete in the toolbar | **REPRODUCED**: three menu presentation groups already exist, but their toolbar subsets omit interactive actions | profile toolbar projection | **G9U1 acceptance polish**: keep three flyouts—Lines and vectors, Polygons, Derived constructions—and include every existing interactive mode in its matching group | exact parity with the three Construction menu groups | a listed entry is not an interactive host mode; document and exclude it rather than inventing a tool |
| Parameters flyout contains only Slider | **REPRODUCED**: Fixed Angle, Checkbox, Button and Input Box are declared upstream modes but overflow-only; Animation toggle is a contextual action, not an interactive creation tool | profile toolbar projection | **G9U1 acceptance polish**: include the five interactive modes and retain Animation toggle outside the toolbar | exact group and no-duplicate tests | adding Animation toggle would require treating a contextual state action as a tool |

## Frozen boundaries

- `apps/geocedg/application-profile.yml` remains the single menu/toolbar action
  authority; Java only interprets its declared projection.
- `toolbar_action_ids` is a visual projection and may group an already-declared
  action differently from its semantic Construction menu group. Every toolbar
  action remains globally unique and must still match the operational catalog.
- `profile-flyout` is reserved for a declared group that mixes native modes and
  product actions. It does not create a second action registry.
- The semantic-curve definition inspector remains menu/context inspection, not
  an interactive toolbar tool. Parameter animation remains contextual. Standard
  View and Show All Objects remain menu navigation actions.
- Input Help remains outside all tool groups at the far-right host position.
- The author sources under `branding/v1/source` remain byte-exact. Only the
  tracked derivative and deterministic generator contract may change.
- Bootstrap prerequisites are unchanged. Verification-infrastructure impact is
  limited to updating the phase verifier's static presentation/resource
  assertions and candidate evidence; the final executable cohort requires its
  own PHASE, COMPOSED and FULL evidence.
