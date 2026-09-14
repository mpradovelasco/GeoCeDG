# Independent application presentation sizing

- Phase: `PRE-G9B-S4`
- Status: **NORMATIVE IMPLEMENTATION CONTRACT — AUTHOR AUTHORIZED**
- Layer: Desktop application/frontend presentation
- Persistence: GeoCeDG user preferences; never construction XML
- Classic effect: behavior-neutral

## 1. Ownership

S4 owns five independent effective sizes:

| Preference | Presentation owner | Default/fallback source |
|---|---|---|
| menu font | GeoCeDG application menu bar and its menu items | inherited effective GUI/menu font |
| toolbar icon | GeoCeDG main toolbar native and profile-flyout buttons | inherited effective rendered icon pixels |
| Algebra font | Algebra tree, renderer/editor and Algebra helper presentation | inherited effective Desktop plain font |
| Construction Protocol font | protocol table/header/navigation/context presentation | inherited effective Desktop plain font |
| Graphics font | shared Graphics/Graphics2 axes, coordinates and view-owned object labels | inherited effective Euclidian view font |

The ownership invariant is:

```text
construction-object font
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
versioned key and is validated against an explicit supported value list. Menu,
Algebra and Construction Protocol use the inherited menu-size list. Graphics
uses the application-size list, which adds the S2 10 pt value needed to preserve
an existing 10 pt effective view size.
Toolbar icons use bounded native/scaled
pixel sizes supported by `ImageManagerD`; the implementation must continue to
select the existing 32- or 64-pixel source resource before responsive scaling.

No category is inferred from another after initialization. Graphics and
Graphics2 intentionally share one preference; no per-view persistent setting is
introduced.

## 3. Defaults and migration

When all S4 keys are absent, the first S4-capable startup captures the inherited
effective values after normal preference initialization and materializes them into
the five independent keys. Existing explicit GUI/menu preferences therefore
remain observable, and the toolbar retains its already rendered size.

For a missing or malformed individual key, capture only that category's
inherited effective value, normalize it to the closest supported value, and
materialize the repaired key. Never overwrite another valid S4 category. This
is a one-time presentation-preference fallback, not a construction migration.

Fresh default GeoCeDG appearance remains 16 pt for menu, Algebra, Construction
Protocol and Graphics UI, with 32-pixel toolbar icons. Classic continues to use
the inherited global GUI/application font and icon behavior.

## 4. Live application

Each accepted value is persisted before its targeted live refresh:

- menu updates rebuild/re-font only the menu presentation;
- toolbar icon updates set the explicit maximum icon pixels and rebuild the
  toolbar from native/scalable resources;
- Algebra updates only the Algebra presentation components;
- Construction Protocol updates only existing protocol/navigation components;
- Graphics updates both existing 2D Euclidian views through their view-font
  seam.

No setter calls `setUnsaved()`, stores an undo point, changes construction XML,
alters an object's mathematical properties or changes another S4 value.

## 5. UI and compatibility

GeoCeDG replaces the inherited single mixed GUI-font row in Advanced options
with one compact five-row presentation-size panel. Labels are explicitly:
`Menu font size`, `Toolbar icon size`, `Algebra font size`,
`Construction Protocol font size`, and `Graphics font size` (with equivalent
Spanish product text). The inherited Advanced options surface and dialog
lifecycle remain authoritative.

The underlying Desktop hooks return their historical values for all non-GeoCeDG
applications. Existing `.ggb`/`.cedg` files require no schema or migration and
load independently of these preferences.

## 6. Validation

Stable model/component tests cover valid/invalid load, one-time fallback,
per-category persistence, setter isolation, live menu/toolbar/Algebra/protocol/
Graphics refresh, absence of construction dirty state, Classic-neutral hooks,
S2 10 pt separation and S3 logical-font/zoom behavior. Screenshot and
pixel-perfect rendering tests are not acceptance authority.
