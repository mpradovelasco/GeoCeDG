# Independent application presentation sizing

- Phase: `PRE-G9B-S4` with corrective continuation `PRE-G9B-S4-R1`
- Status: **NORMATIVE IMPLEMENTATION CONTRACT — AUTHOR AUTHORIZED**
- Layer: Desktop application/frontend presentation
- Persistence: GeoCeDG user preferences; never construction XML
- Classic effect: behavior-neutral

## 1. Ownership

S4/R1 owns six independent effective sizes:

| Preference | Presentation owner | Fresh default |
|---|---|---|
| General UI font | residual inherited Desktop GUI typography: Input Bar entry/help, toolbar-right command help, ordinary/redefine/Text dialogs and comparable controls without a specific owner | 12 pt |
| menu font | GeoCeDG application menu bar and its menu items | 14 pt |
| toolbar icon | GeoCeDG main toolbar native, profile-flyout and persistent user-tool buttons | 28 px |
| Algebra font | Algebra tree, renderer/editor and Algebra helper presentation | 12 pt |
| Construction Protocol font | protocol table/header/navigation/context presentation | 12 pt |
| Graphics font | shared Graphics/Graphics2 axes, coordinates and view-owned object labels | 12 pt |

The ownership invariant is:

```text
construction-object font
    != general UI font
    != menu font
    != toolbar icon size
    != Algebra font
    != Construction Protocol font
    != Graphics UI font
```

Graphics object labels are admitted because inherited `EuclidianView` already
owns their presentation font. Construction `GeoText` is excluded: its logical
font remains the S2 application/construction base, and its S3 world-view scale
remains `xscale / EuclidianView.SCALE_STANDARD`.

## 2. Preference model

Use one GeoCeDG-specific Desktop model backed by the existing
`GeoGebraPreferencesD` user-preference service. Each category has a separate
versioned key and is validated against an explicit supported value list.
General UI, Menu, Algebra and Construction Protocol use the inherited menu-size list. Graphics
uses the application-size list, which adds the S2 10 pt value needed to preserve
an existing 10 pt effective view size.
Toolbar icons use bounded native/scaled pixel sizes supported by
`ImageManagerD`, including 28 px. Toolbar size controls toolbar image-resource
selection and rendering only; application-wide dialog/style-bar icons continue
to use the inherited general GUI-icon derivation. The implementation must
continue to select existing native/scalable sources before responsive scaling.

No category is inferred from another after initialization. Graphics and
Graphics2 intentionally share one preference; no per-view persistent setting is
introduced.

## 3. Defaults and migration

Initialization distinguishes three states. A fresh profile has neither any S4
key nor legacy saved user-preferences XML; each missing category receives and
materializes its approved fresh default. A valid category key is an explicit
persisted value and is preserved. A missing or malformed category in an
existing profile captures only that category's effective inherited value,
normalizes it to the closest supported value and materializes it. Thus an S4
candidate profile adding the new General UI key migrates its existing effective
GUI font rather than being resized unexpectedly.

Never overwrite another valid category or reinterpret one preference as the
source of another. This is presentation-preference initialization, not a
construction migration.

Fresh GeoCeDG defaults are General UI 12 pt, Menu 14 pt, toolbar 28 px,
Algebra 12 pt, Construction Protocol 12 pt and Graphics 12 pt. Classic
continues to use the inherited global GUI/application font and icon behavior.

## 4. Live application

Each accepted value is persisted before its live refresh:

- General UI updates the inherited Desktop GUI font and refreshes residual
  consumers including Input Bar/help, toolbar command help and open dialogs;
- menu updates rebuild/re-font only the menu presentation;
- toolbar icon updates set the explicit maximum icon pixels and rebuild the
  toolbar from native/scalable resources;
- Algebra updates only the Algebra presentation components;
- Construction Protocol updates only existing protocol/navigation components;
- Graphics updates both existing 2D Euclidian views through their view-font
  seam.

The precedence rule is `specific S4 owner > General UI fallback`; a General UI
refresh may revisit components but must reapply the independent specific sizes.
No setter calls `setUnsaved()`, stores an undo point, changes construction XML,
alters an object's mathematical properties or changes another S4 value.

## 5. UI and compatibility

GeoCeDG replaces the inherited single mixed GUI-font row in Advanced options
with one compact six-row presentation-size panel whose descriptors are left
aligned and whose controls share one value column. Labels are explicitly:
`General UI font size`, `Menu font size`, `Toolbar icon size`, `Algebra font size`,
`Construction Protocol font size`, and `Graphics font size` (with equivalent
Spanish product text). The inherited Advanced options surface and dialog
lifecycle remain authoritative.

The construction-owned host control is labelled `Text font size`. It continues
to set the ordinary and LaTeX construction-Text base font only. The Text dialog
retains Apply/OK/Cancel/Help: Apply commits and remains open, OK commits and
closes, and Cancel discards the unapplied draft and closes. Button height is
derived coherently from normal Swing preferred sizes; toolbar icon size never
controls the Help icon.

The underlying Desktop hooks return their historical values for all non-GeoCeDG
applications. Existing `.ggb`/`.cedg` files require no schema or migration and
load independently of these preferences.

## 6. Validation

Stable model/component tests cover fresh/default, explicit/migrated load,
per-category persistence, setter isolation, live menu/toolbar/Algebra/protocol/
Graphics refresh, General UI residual consumers, Text-dialog controls,
persistent-tool alignment at supported sizes/scales, absence of construction
dirty state, Classic-neutral hooks, S2 10 pt separation and S3 logical-font/zoom behavior. Screenshot and
pixel-perfect rendering tests are not acceptance authority.
