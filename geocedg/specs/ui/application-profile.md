# GeoCeDG Desktop application profile v1

- Status: Stable for G2 frontend infrastructure
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
`getConfig()`. The launcher disables the inherited branded splash, selects the
isolated preference file unless the user supplied `--settingsFile`, and creates
`GeoCeDGFrame`/`AppGeoCeDG` instances. New windows and template helpers preserve
the same profile.

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

The upstream launcher and configuration remain unchanged. From the composite
root, `:desktop:desktop:run` launches GeoGebra Classic 5 and
`:desktop:desktop:runGeoCeDG` launches GeoCeDG. This process-level diagnostic
route avoids mixing preference namespaces or product profiles in one JVM.

## Branding and licensing

G2 adds textual branding only. The GeoCeDG launcher suppresses the upstream
splash and frame icon; it adds no logo, icon, translation, style, installer, or
other distributable asset. Inherited Classic UI strings and resources remain a
known baseline/release-audit limitation and are not asserted to be licensed for
GeoCeDG redistribution.
