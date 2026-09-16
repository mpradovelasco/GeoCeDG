# Application presentation theme and Preferences consolidation

- Phase: `PRE-G9B-P0`
- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Layer: Desktop application/frontend presentation
- Persistence: GeoCeDG user preferences; never construction, document or
  preferences XML
- Classic effect: behavior-neutral

## 1. Scope and non-scope

A presentation theme is an application presentation preference of the GeoCeDG
Desktop product. It selects the colors of known presentation roles.

It does not, in any preset:

- become geometry or enter the construction dependency graph;
- change object identity, object color, style or visibility;
- change axis, grid or object-label semantics;
- change export, DXF or print output;
- add, remove or alter any serialized field in `.cedg` or `.ggb`;
- change the meaning of a background explicitly chosen by the user.

This contract authorizes exactly three closed presets. It does not authorize a
theme engine, downloadable themes, a skin DSL, a dark theme, arbitrary color
customization or a broader generic theming infrastructure.

## 2. Identity and persistence

The theme is a closed enumeration with a stable semantic identity. The persisted
value is that identity, never the localized text shown to the user.

| Theme | Persisted identity | Display text key |
|---|---|---|
| Original | `original` | `Presentation.Theme.Original` |
| Scientific Paper | `scientific-paper` | `Presentation.Theme.ScientificPaper` |
| Cool Geometry | `cool-geometry` | `Presentation.Theme.CoolGeometry` |

The single preference key is `geocedg.presentation.theme.v1`, stored in the same
GeoCeDG user-preference store as the `PRE-G9B-S4` presentation sizes. An absent,
empty, unknown or otherwise unreadable identity resolves to `Original` and the
resolved identity is materialized, so historical profiles without a theme
preference keep the historical appearance. `Original` is also the fresh default.

The preference survives application restart and document changes. Creating or
loading a document never changes it. There is no reset control on the GeoCeDG
Preferences surface, so the existing contract applies unchanged: a profile
without a stored theme resolves to `Original`.

## 3. Palette authority

Each preset owns one palette. `Original` has no palette and supplies no
override at all.

| Role | Scientific Paper | Cool Geometry |
|---|---|---|
| frame/client area | `#E5E2DC` | `#DEE4E9` |
| menu/toolbar supporting surface | `#ECE9E3` | `#E7ECF0` |
| panels | `#F5F3EE` | `#EFF3F6` |
| Graphics canvas | `#FFFDF8` | `#F8FAFC` |
| separators/borders | `#C8C4BC` | `#BBC4CC` |

The palette is the single authority; no consumer defines a second copy and no
color is derived from proximity to another color.

## 4. Chrome application

The presets are applied to known look-and-feel presentation roles of the running
GeoCeDG process, over the inherited FlatLaf light look and feel, followed by the
existing component-tree refresh. The affected roles are the frame/client
surface, the menu-bar/toolbar/popup surface, the list/tree/table panel surface,
and the separator/border/grid role.

Shared Desktop classes that historically hard-coded a presentation color consult
the GeoCeDG-owned roles `GeoCeDG.presentation.frameBackground`,
`GeoCeDG.presentation.surfaceBackground`, `GeoCeDG.presentation.panelBackground`
and `GeoCeDG.presentation.borderColor`, and keep their inherited literal when the
role is undefined. Only the GeoCeDG application installs those roles.

Selection, focus and tool-active colors are outside this contract and keep their
inherited values. Native operating-system window decoration is outside this
contract; `frame` means the GeoCeDG client area, not the system title bar.

## 5. Graphics canvas contract

The document owns `bgColor`, and that ownership is unchanged. A preset supplies
an *application presentation background* that is consulted only when a Euclidian
view clears its background for painting. It is never written into
`EuclidianSettings`, never reaches `getXML`, and therefore cannot appear in a
document or in the preferences XML.

The policy is exact and role based:

```text
paint(view) background =
    presentation background   when a non-Original theme is active
                              AND the document background equals the
                              application default background role (white)
    document background       otherwise
```

Consequently a document that carries an explicitly chosen non-default background
keeps that background under every preset, and no theme change marks a
construction as modified or rewrites a saved file. One presentation canvas serves
Graphics and Graphics2, matching the single shared Graphics preference of
`PRE-G9B-S4`.

Known limitation: a document whose background is explicitly pure white is
indistinguishable from the application default and is painted with the preset
canvas. Selecting `Original` restores the document background exactly.

## 6. Preferences consolidation

The preferences tab historically titled `Layout` is titled
`Layout & Presentation` in the GeoCeDG product through a product title hook;
Classic and every other application keep the inherited title. The tab groups, in
order: the inherited layout controls, the `PRE-G9B-S4` `Presentation sizes`
group, and the `Theme` group.

The presentation-size controls exist exactly once and only in this tab. The
Advanced tab shows neither them nor the inherited mixed GUI-font row for a
product that owns presentation sizing.

Every label is resolved through the existing GeoCeDG profile localization; no
user-visible text is hard-coded. The GeoCeDG Preferences surface has no
Apply/OK/Cancel transaction, so a valid theme selection applies and persists
live exactly like a presentation size.

## 7. Classic containment

`AppConfigGeoCeDG` deliberately reports the Classic application code, so the
application code is never a theme discriminator. Containment rests on the
product hooks overridden only by `AppGeoCeDG`, on the separate GeoCeDG launcher
and process, and on the separate GeoCeDG preference file. Classic keeps its
inherited look and feel, its inherited Advanced GUI-font row, its inherited tab
title and a null presentation background.

## 8. Validation

Focused tests cover the default and migration to `Original`, the three
selections, identity persistence and reload, palette values, installation and
restoration of every presentation role, the canvas policy for default and
explicitly chosen backgrounds, absence of document/settings mutation for
Graphics and Graphics2, unchanged object/axis/grid colors, unchanged document
XML, theme stability across document load, presence of both groups in
`Layout & Presentation`, their absence from Advanced, unchanged presentation-size
keys/defaults/behavior after the move, localization of the tab and controls,
Classic containment, and the single `1.0.0` version authority with an unchanged
upgrade identity and an unpromoted distribution state.
