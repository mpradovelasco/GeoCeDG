# GeoCeDG Desktop application profile

- Status: Stable G2 foundation; live schema-v2 G9U1 implementation candidate
- Profile ID: `geocedg-desktop`
- Authority: `docs/adr/0001-geocedg-product-profile.md`
- Runtime manifest: `apps/geocedg/application-profile.yml`

## Product boundary

GeoCeDG is an independent textual product identity implemented as a dedicated
Desktop profile over the pinned GeoGebra Classic 5 runtime. The profile name is
`GeoCeDG`, its Windows application ID is `org.geocedg.desktop`, and its
preferences key is `geocedg`. The default preference file is isolated below a
`GeoCeDG` directory in the current user's application-data directory.

G2 deliberately retains the upstream `classic` application code in `.ggb`
headers. The profile ID is therefore not a new serialization app code. Changing
that persisted value requires a separate compatibility decision and is outside
G2.

## Launcher and configuration

`org.geocedg.desktop.GeoCeDG` is the Desktop entry point. It selects
`AppConfigGeoCeDG` through the constructor seam before `AppD` first calls
`getConfig()`. The launcher selects the isolated preference file unless the user
supplied `--settingsFile` and supplies only the tracked GeoCeDG startup resource.
The product-specific three-argument Desktop launch overload prepares the splash
first, then synchronously creates `GeoCeDGFrame`/`AppGeoCeDG` and initializes the
frame on Swing EDT. This keeps the Construction/metric owner on the same UI
thread used by ordinary Algebra gestures; it does not relax kernel confinement.
New windows and template helpers preserve the same profile.

The config inherits Classic kernel availability and installs no command filter
in G2. The explicit `createCommandFilter()` hook is the extension point for a
future approved policy; no command or geometric behavior is changed here.

## Initial perspective

The manifest defines a custom perspective with Algebra and the primary 2D
Euclidian view visible. Spreadsheet, CAS, properties, and 3D remain available
but closed initially. Axes and a unit axes ratio are enabled, the grid is off,
and the input panel remains visible. A perspective loaded from a document or
saved GeoCeDG preferences takes precedence over this first-run default.

## Toolbar adapter

The manifest is the single durable source for the initial toolbar. The Desktop
adapter validates and converts each ordered category's existing upstream mode
IDs to the established `|`-separated toolbar grammar. G2 adds no modes, macros,
commands, or legacy CeDG tools.

The available categories are selection/construction, primitives/incidence,
curves, intersections/locus, standard transformations, and
measurement/validation. Descriptive projections, plane changes/developments,
CeDG tools, and import/export are explicit `not-implemented` placeholders and
are not emitted into the toolbar.

## Classic diagnostic route

The upstream two-argument launcher behavior and configuration remain unchanged.
From the composite root, `:desktop:desktop:run` launches GeoGebra Classic 5 and
`:desktop:desktop:runGeoCeDG` launches GeoCeDG. This process-level diagnostic
route avoids mixing preference namespaces or product profiles in one JVM.

## Branding and licensing

G2 originally added textual branding only and suppressed the upstream splash and
frame icon. The G9U1 Round-3 candidate supersedes that presentation limitation
with author-authorized tracked GeoCeDG assets: `helixTopBar.png` supplies the
application/window and package-icon role, and `helixSnapshot.png` supplies the
startup role. Byte-exact sources and deterministic contain/center derivatives
are recorded in `geocedg/resources/assets-manifest.yml`; missing declared
resources fail validation and never fall back to upstream branding. This
internal authorization does not itself clear public redistribution. Inherited
Classic UI strings/resources remain a separate release-audit limitation.

The central build/package provenance declares semantic version `0.9.0`; product
surfaces render its concise display form `GeoCeDG 0.9`. The frame title appends
the current filename when present. About consumes the same version authority,
records the GeoGebra baseline and `Manuel Prado-Velasco, Universidad de Sevilla`,
and retains required upstream credits/licenses.

## Live schema-v2 G9U1 candidate

The live `apps/geocedg/application-profile.yml` is now schema version 2. One
110-action catalog feeds 11 professional families, 18 operational clusters, 28
ordered presentation groups, 11 primary-toolbar groups, workspaces, seven menu
sections and help/localization. Taxonomy and presentation are distinct fields
of the same authority: Java does not maintain a second action/menu/toolbar list.

The final presentation-polish successor retains 44 native toolbar modes and two
profile-declared mixed flyouts. `Semantic Curves` combines the existing Locus
V2 mode, Spline V2 command action and exact-address Point-on-semantic-curve
action; `View navigation` combines the existing Pan, ZoomWindow, Zoom In, Zoom
Out and Copy Visual Style actions. A mixed flyout is a rendering instruction for
existing registry actions, never a second action catalog. Product presentation
may override an inherited mode name (`Locus V2`) without changing the Classic
mode localization or its semantic target.

File and Edit use direct actions plus separators. View includes bounded host
view controls. Construction owns object-producing groups, including semantic
curves and annotations/media. Options reuses host Algebra display/sort,
rounding, labeling, font and save-settings state. Automation hosts dynamic
installed tools outside the stable catalog. Product Continuity remains OFF and
the GeoCeDG locale surface remains EN/ES. A deterministic in-memory version-1
adapter remains compatibility input; no user file is rewritten merely because
the live product profile is version 2.
