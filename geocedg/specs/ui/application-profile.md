# GeoCeDG Desktop application profile

- Status: Stable G2 foundation; live schema-v2 G9U1 implementation `PASS — AUTHOR APPROVED`
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

One internal diagnostic dispatch precedes all of this
(PRE-G9B-R6-plus-E1-X1). When the first argument is exactly
`--classic-diagnostic`, the entry point removes only that marker, keeps every
other argument unchanged, adds `--showSplash=false` and the isolated
`classic-diagnostic.properties` beside the GeoCeDG preference file only when
the caller gave no splash or settings argument, starts the unchanged upstream
`org.geogebra.desktop.GeoGebra3D.main` and returns without creating any GeoCeDG
configuration, profile, preference selection or frame. The marker is launcher
protocol of the separate diagnostic Classic process (a packaged GeoCeDG starts
the executable named by `jpackage.app-path` with it); it is not a construction
command, a document feature or a persisted setting, and it has no meaning in any
other position.

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

The central build/package provenance declares semantic version `1.1.0` (the single
authority is `packaging/windows/package.yml`; see the Windows packaging contract);
product surfaces render that exact version as `GeoCeDG 1.1.0`, with no truncated
major.minor abbreviation. The frame title appends
the current filename when present. About consumes the same version authority,
records the GeoGebra baseline and `Manuel Prado-Velasco, Universidad de Sevilla`,
and retains required upstream credits/licenses.

### About content (PM-6, PM-7, PM-9)

Normative since `PRE-G9B-R6-plus-G` (transcription of the author-approved product
metadata contract; the
[design record](../../../docs/architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md)
stays the historical design authority, and this section prevails over it).

The existing `geocedg.help.about` action and its read-only text dialog are kept; no
new framework or window type exists. Its texts live in the live application
profile, the single runtime authority for UI texts, in English and Spanish, and the
content order is the same in both languages:

1. `GeoCeDG <version>`;
2. the canonical version date in ISO form, locale-neutral, when the build declares
   one (`1.1.0`: `2026-10-15`);
3. the build identity `Build: <first 12 hex digits> (clean)`, `(modified)` or
   `(state unavailable)`, or `Build: unavailable` without a commit; never next to a
   date and never a wall-clock build time;
4. the distribution condition of this copy (Windows packaging contract): the verbatim
   marker and a localized meaning for a verified `INTERNAL` or `NC` copy, a
   development-build statement, or a not-verified statement; every one except `NC`
   says "not for redistribution";
5. the product description (`Workspace.About`);
6. the GeoGebra upstream baseline (`GeoGebraConstants.VERSION_STRING`);
7. the author line;
8. the licensing summary.

The licensing summary keeps two kinds of statement apart. For the **composite
product**: GeoCeDG is a composite work and no single licence covers the whole
product; the distribution notice of this copy, or the statement that a development
or unverified copy has none. For its **components**: GeoCeDG-authored software,
EUPL-1.2; GeoCeDG-authored documentation and ordinary artwork, CC BY 4.0 with the
recorded exceptions, the name and official marks being governed separately by the
brand policy; inherited GeoGebra source code keeps its EUPL-1.2 grant, and GeoGebra
Language Files, Materials and the other third-party components keep their own terms.
The summary ends with the route to the full texts: `app/legal/` and the notice in
`app/` in a package, or the repository files `LICENSE`, `NOTICE.md`,
`THIRD_PARTY.md`, `LICENSES/`, `docs/LICENSE.md` and the component matrix otherwise.

The About surface never labels the product as licensed solely under EUPL-1.2 or as
Open Source or Free Software, never presents a component's permission as the
licence of the complete binary, never gives the upstream GeoGebra licence URL as the
complete authority and never reproduces full legal texts. The summary is
presentation; the files remain the authority, and a focused test keeps each
summarized fact present in its authority file.

Classic keeps its inherited About, menu and title (`GeoGebraMenuBar`, `HelpMenuD` and
`MacApplicationListener` are not modified for it): its Help route still reaches the
inherited dialog and receives no GeoCeDG metadata.

## Live schema-v2 G9U1 author-approved profile

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

The final micro-presentation successor also makes the ordered `toolbar_groups`
array the exact left-to-right toolbar authority. Its eleven groups are Move,
Point/intersection, Lines/vectors, Polygons, Derived constructions,
Circles/conics, Semantic curves, Metrics, Transformations,
Parameters/drivers and Navigation. Fixed Angle and Tangent are toolbar-only
members of Derived constructions while retaining their existing menu taxonomy.
The two mixed flyouts render with the normal compact active-action icon and
arrow; their last-used selection is transient UI state. An iconless installed
user tool uses a deterministic in-memory monogram, while an assigned PNG keeps
precedence. None of these presentation states belongs to a construction.

File and Edit use direct actions plus separators. View includes bounded host
view controls. Construction owns object-producing groups, including semantic
curves and annotations/media. Options reuses host Algebra display/sort,
rounding, labeling, font and save-settings state. Automation hosts dynamic
installed tools outside the stable catalog. Product Continuity remains OFF and
the GeoCeDG locale surface remains EN/ES. A deterministic in-memory version-1
adapter remains compatibility input; no user file is rewritten merely because
the live product profile is version 2.
