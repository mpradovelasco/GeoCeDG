# PRE-G9B-P0 author smoke checklist

Status: **PENDING AUTHOR EXECUTION**. This checklist collects author evidence.
It is not an automated result and it never records author approval by itself.

Launch with:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

The presentation theme lives in
`%APPDATA%\GeoCeDG\5.4\preferences.properties` under
`geocedg.presentation.theme.v1`.

## 1. Version surfaces

- [ ] The window title reads `GeoCeDG 1.0.0`.
- [ ] **Help > About GeoCeDG** reports `GeoCeDG 1.0.0` and the unchanged GeoGebra
      baseline `5.4.928.0`.
- [ ] Nothing in the product claims a public release; the package remains
      `internal-evaluation`.

## 2. Preferences consolidation

- [ ] **Options > Preferences** shows a tab named `Layout & Presentation`
      (`Disposición y presentación` in Spanish) instead of `Layout`.
- [ ] That tab shows, in order: the inherited layout controls, the
      `Presentation sizes` group and the `Theme` group.
- [ ] The `Advanced` tab no longer shows the presentation-size controls and does
      not show the inherited single GUI-font row either.
- [ ] Each of the six presentation sizes still changes only its own surface, in
      both tabs' language settings.

## 3. Original

- [ ] Matches the previous appearance exactly; no layout regression.
- [ ] Remains the selection on a profile that never stored a theme.

## 4. Scientific Paper

- [ ] Warm, very light canvas; frame, menu/toolbar surface and panels are
      visibly distinguished from each other.
- [ ] Axes, coordinates, object labels, toolbar icons and menu text stay legible.

## 5. Cool Geometry

- [ ] Cool, very light canvas; frame, menu/toolbar surface and panels are
      visibly distinguished from each other.
- [ ] Axes, coordinates, object labels, toolbar icons and menu text stay legible.

## 6. Behaviour in all three presets

- [ ] Selecting a theme applies immediately, without restarting, and without a
      visible loss of listeners, focus or tool state. **This is the item the
      automated suite can only exercise through the panel and installer path;
      please confirm it visually.**
- [ ] Change presentation sizes and confirm they still apply live.
- [ ] Open Graphics 2 and confirm it uses the same presentation canvas.
- [ ] Open the Construction Protocol and confirm its table is readable.
- [ ] Toggle Algebra visible/hidden.
- [ ] Switch documents (new, open, close) and confirm the theme does not change.
- [ ] Restart the application and confirm the theme persisted.

## 7. Document safety

- [ ] Create objects, save a `.cedg` and a `.ggb`, reopen them and confirm the
      geometry, object colors, axes and grid are unchanged.
- [ ] Open the saved file in a text editor and confirm there is no theme field
      and that `bgColor` is unchanged.
- [ ] Set an explicit non-default background in Graphics properties and confirm
      every preset respects it.
- [ ] Known limitation to confirm as acceptable: a document whose background is
      explicitly pure white is indistinguishable from the application default
      and is painted with the preset canvas; `Original` restores it exactly.

## 8. Classic containment

- [ ] `.\gradlew.bat :desktop:desktop:run` still shows the inherited Classic
      appearance, the inherited `Layout` tab title and the inherited Advanced
      GUI-font row.

## Disposition

```text
AUTHOR SMOKE RESULT = <PASS | FINDINGS>
PRE-G9B-P0 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
selfApproved=false
```
