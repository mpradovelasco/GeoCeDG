# Objective

Maintain the native neutral 2D export boundary and experimental DXF exporter.

# Authority and evidence hierarchy

Follow `AGENTS.md`, ADR 0005, the G5 export specification, the roadmap, and
executable verification authority in that order.

# Scope

Read-only 2D geometry adaptation, neutral export entities, DXF encoding,
GeoCeDG-only UI access, regression evidence, and user documentation.

# Explicitly forbidden scope

Do not change kernel geometry semantics, `Locus`, `.ggb` serialization, 3D
projection semantics, advanced layers, G6, or Classic default behavior.

# Architectural placement

Only `GeoElementGeometryExportAdapter` may inspect GeoGebra geometry. Format
writers consume `GeometryExportModel`; dialogs and file I/O remain Desktop
concerns.

# Required design/specification

Apply `docs/adr/0005-neutral-2d-geometry-export.md` and
`geocedg/specs/export/geometry-export-foundation.md`.

# Geometric invariants and degeneracies

Preserve model coordinates, distance, radius, orientation, closure, and
unbounded direction. Reject undefined/non-finite/degenerate inputs. Never use
zoom or viewport as geometric data and never approximate silently.

# Compatibility and serialization

Export is read-only. Preserve Classic launch/menu behavior and existing `.ggb`
serialization. The current integer layer mapping is not a future layer model.

# Required tests and commands

Run focused adapter/writer tests, semantic DXF regression, shared/Desktop
build and checkstyle, GeoCeDG manual export, Classic launch regression,
composed verification with benchmarks, `git diff --check`, and cleanup.

# Required artifacts

Maintain the feature manifest, regression case, focused verifier, architecture
decision/specification, user guide, upstream modification record, and G5 report.

# Stop conditions

Stop before any geometry-semantic, Locus, 3D-semantic, serialization, Classic
compatibility, or unreviewed external-dependency change.
