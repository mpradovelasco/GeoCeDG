# G9U1 final micro presentation adjustments — technical candidate

Status: `FINAL TECHNICAL CANDIDATE PREPARATION — AUTHOR CLOSEOUT PENDING`

The author functionally accepted technical checkpoint
`34ffdd9af5f94ded2765e7d495ee66543d4d751f` and authorized only the bounded
presentation adjustments recorded here. That commit remains immutable. This
successor changes no geometric kernel, scientific contract, construction
persistence, macro identity, Locus V2/Spline V2 semantics, R6 interaction
semantics or stable action inventory.

## Final native-toolbar visual normalization successor

The author subsequently accepted technical checkpoint
`e4ef3d48ea95a0c3243e57dfc703b539d455c33e` as functionally correct. That
published commit remains immutable. The final successor is limited to the
remaining Swing presentation mismatch: mixed Semantic Curves/Navigation
flyouts and pinned user-tool buttons were sized with the local
`scaledIconSize + 12` approximation, while an actual upstream
`ModeToggleMenuD`/`ToolToggleButton` derives its complete geometry from the
scaled icon and native border policy. At the 48-pixel HiDPI test icon size this
made the adapters 60 pixels rather than the native 66 pixels and allowed their
container/alignment contract to differ.

The live first native toolbar button is now the presentation reference for the
two mixed flyouts and the pinned user-tool projection. The adapters copy its
preferred/minimum/maximum size, margin, border, alignment and standard Swing
button presentation properties; mixed flyouts are hosted in the same
horizontal `JPanel`/`BoxLayout` structure as `ModeToggleMenuD`. Their own action,
model, icon and transient last-used state remain independent. Native arrow
placement is derived from the reference icon and button footprint rather than
from an unrelated fixed button size. A separately constructed genuine
`ModeToggleMenuD` supplies the same reference only for isolated user-tool tests
or surfaces that have not yet been attached to the live toolbar.

Structural Swing tests compare the adapters against the actual native button at
HiDPI, including dimensions, margin, border, alignment, component orientation
and top-level row bounds. They repeat the comparison after a mixed-flyout or
user-tool group changes its active action. No screenshot or pixel-coordinate
golden becomes authority. The schema-v2 profile, its exact eleven-group order,
52 toolbar projections and 110 stable action IDs are byte-unchanged from
`e4ef3d48...`.

## Bounded disposition

| Requirement | Implemented presentation contract | Evidence boundary |
|---|---|---|
| Exact primary-toolbar order | The one schema-v2 profile selects, in order, Move; Point/intersection; Lines/vectors; Polygons; Derived; Circles/conics; Semantic curves; Metrics; Transformations; Parameters/drivers; Navigation. | Exact profile and rendered-toolbar order are compared; the 110 action IDs remain unchanged. |
| Toolbar-only membership | `parameter.fixed-angle` and `relation.tangent` occur only in Derived on the toolbar. Their Construction-menu groups remain Parameters and Relations respectively. | Exact group membership and global toolbar uniqueness are checked. |
| Semantic Curves and Navigation | The existing upstream `ModeToggleMenuD` remains the native-mode flyout. Because each of these two groups mixes host modes with a registered non-mode action, one bounded Desktop presentation adapter supplies the same compact icon/arrow footprint, invokes the same registry `Action`, and retains the last selected action as the visible icon. | The adapter owns only transient UI state. It creates no action, command, mode or document state. |
| Help and Classic diagnostic | Help is ordered Input Help, Current Tool Help, Command List, User Guide, Keyboard Shortcuts, About. The unchanged Classic diagnostic action is in File immediately after Open Recent. | Menu projection remains profile-owned; diagnostic process/preferences isolation is unchanged. |
| Persistent user-tool fallback | A validated custom PNG still wins. Without one, an in-memory deterministic monogram uses the first meaningful letter of the visible command, with normal toolbar dimensions. A grouped button follows its active command. | The monogram is not persisted, does not enter `.cedg`, does not alter macro/tool identity and is not a product asset. |
| Input Help and product invariants | Input Help remains outside the tool groups at the far right. EN/ES, Continuity OFF, Classic isolation and all previously validated macro/`.cedg` behavior remain unchanged. | Focused structure, localization and lifecycle tests plus the canonical G9U1 gates remain required. |

## Verification contract

The changed cohort requires fresh deterministic focused A/B, G9U1 PHASE,
COMPOSED and clean FULL evidence under the current verification authority.
Receipts for `34ffdd9a...` and `e4ef3d48...` remain historical and are not
acceptance evidence for this successor. The author-owned re-smoke checklist is
unchanged and is not agent execution evidence. No PASS tag, main promotion or
author approval is created by this technical iteration.

`BOOTSTRAP_IMPACT = NO_CHANGE_REQUIRED`: no workstation, toolchain or build
prerequisite changes. `GUIDE_IMPACT = NO_CHANGE_REQUIRED`: this successor only
normalizes the already documented controls to the native Swing toolbar
footprint and changes no user route or operation.
