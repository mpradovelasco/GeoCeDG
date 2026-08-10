# Third-party component status

Status: incomplete runtime inventory for technical evaluation; not legal
advice and not a redistribution clearance.

The G4 pipeline emits a CycloneDX SBOM containing every staged runtime JAR and
its SHA-256 hash. That generated inventory is evidence of exact composition,
not a substitute for license identification or attribution review.

Known component families include the inherited GeoGebra Desktop/shared
modules, ReTeX/JLaTeXMath renderer code and fonts, JOGL/GlueGen, Giac native
bindings, FlatLaf, JNA, Rhino, SVG/XML libraries, and their transitive
dependencies. Component-local notices and the factual categories already
identified are recorded in `docs/licensing/component-matrix.md`.

Before public redistribution, each SBOM component and embedded runtime asset
must be mapped to its exact license text, copyright notice, attribution and
distribution obligations. Missing or ambiguous evidence remains a blocker.

INTERNAL EVALUATION — NOT FOR REDISTRIBUTION
