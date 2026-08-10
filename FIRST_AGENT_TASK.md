# GeoCeDG - First Agent Task: Upstream Baseline and Repository Scaffold

## Objective

Characterize the exact GeoGebra fork baseline and prepare the minimum GeoCeDG repository scaffold, including the source map required for the later spatial-object/projection phase. Do not implement product features.

## Required authority

Read, in order:

1. root `AGENTS.md`;
2. current Git and Gradle files;
3. upstream `README.md` and `doc/dev/`;
4. `docs/roadmap/geocedg_roadmap.md`;
5. `docs/architecture/proposed_spatial_projection_semantics.md` as a non-normative research proposal;
6. current source code at the exact checked-out commit.

Do not use previous reports as a substitute for inspecting the repository.

## Scope

1. Record:
   - `git status --short --branch`;
   - remotes;
   - current branch and SHA;
   - tags containing the commit;
   - Gradle and Java versions;
   - Gradle composite/project structure.
2. Reproduce the official desktop build/run entry point.
3. Discover and document the exact extension points for:
   - desktop launcher;
   - `AppConfig` selection;
   - perspectives and dock layout;
   - toolbar construction;
   - command registration and processing;
   - shared-kernel algorithm dependencies;
   - `AlgoLocusND`, `GeoLocusND`, rendering, serialization, and path parameterization;
   - `CmdIntersect` and intersection dispatch;
   - existing 3D object classes and algorithms for points, lines, planes, curves, surfaces, quadrics, and composites;
   - coordinate systems, orthogonal projection operators, view transforms, and any current 2D/3D conversion or binding mechanism;
   - 2D and 3D view registration, rendering, selection, and serialization boundaries;
   - current layer, style, visibility, print, PDF/vector export, and page/layout mechanisms;
   - test locations and conventions;
   - benchmark/profiling insertion points and representative existing performance tests;
   - packaging/build outputs suitable for `jpackage`.
4. Create only the approved durable scaffold required by the plan.
5. Create:
   - `UPSTREAM.md`;
   - `docs/upstream/BASELINE_COMMIT.txt`;
   - `docs/architecture/upstream_module_map.md`;
   - `docs/architecture/geocedg_extension_points.md`;
   - `docs/architecture/spatial_projection_extension_points.md`;
   - `docs/licensing/component-matrix.md`;
   - `docs/validation/baseline_report.md`;
   - `docs/adr/0001-geocedg-product-profile.md` as **Proposed**, not Accepted;
   - `tools/agent/verify-baseline.ps1`;
   - a root verification wrapper only if justified.
6. Add `.gitignore` entries for generated GeoCeDG artifacts only when needed.
7. Run all verification that can be completed from the checked-out sources and record exact commands and exit codes.

## Explicitly forbidden scope

Do not:

- modify Locus semantics;
- add `SpatialObject3D`, projection bindings, canonical certificates, visibility algorithms, or 3D conversion;
- add a command;
- change toolbar behavior;
- create GeoCeDG branding;
- import any `.ggb`, `.ggt`, or GeoGebraScript tool;
- add DXF;
- create an installer;
- rename Java packages;
- remove upstream modules;
- perform global formatting;
- update dependencies;
- merge a newer upstream commit;
- claim any license conclusion not directly supported by the checked-out files or current official license text.

## Required design findings

The report must distinguish facts from recommendations and answer:

1. Is the checked-out GitHub repository a mirror, and what does that imply for contribution/upstream tracking?
2. What Java toolchain does the exact desktop `run` task request?
3. Which modules provide the shared kernel, desktop frontend, web frontend, and 3D functionality?
4. Where can a GeoCeDG application profile be introduced with the smallest upstream diff?
5. How is the default toolbar represented and parsed?
6. How are user macros/tools appended?
7. Which files/classes implement Locus generation, storage, path behavior, rendering, and XML persistence?
8. Which current Locus decisions depend on screen scale or sampled point order?
9. Which intersection pairs are currently dispatched explicitly, and is Locus supported?
10. Which kernel classes represent 3D points, lines, planes, curves, surfaces, quadrics, and composite objects?
11. How are coordinate systems and orthogonal projections represented, and which code maps between model and 2D/3D views?
12. Does the current kernel contain any durable relation between a 3D `GeoElement` and one or more 2D projected `GeoElement`s? If not, where is the least invasive insertion point?
13. How are stable object identities and 3D/view data serialized in `.ggb` XML?
14. Which current classes own layers, style, visibility, selection locking, and per-view display state?
15. Which code paths produce PDF, SVG, raster, or print output, and how are page dimensions and scales represented?
16. Which classes own the 3D view and renderer, and what adapter boundary could consume future kernel-owned spatial objects without duplicating geometry?
17. What existing benchmark, profiler, timing, or performance-test infrastructure can be reused?
18. What build artifact is the cleanest input for a future `jpackage` task?
19. Which source-tree assets are EUPL code and which are under separate terms?
20. Which scaffold directories are genuinely needed now, and which should remain deferred?

The spatial findings are source characterization only. Do not propose a final class hierarchy before documenting the actual upstream contracts.

## Verification script contract

`tools/agent/verify-baseline.ps1` shall:

- fail on the first failed required gate;
- print the repository SHA and Java/Gradle versions;
- expose an optional non-gating baseline timing mode if an existing upstream mechanism can be reused without adding dependencies;
- be deterministic;
- use repository-relative paths;
- not install software silently;
- not modify tracked files;
- run `git diff --check`;
- record no generated output inside tracked source paths;
- return non-zero on failure.

The script may expose optional switches for expensive tests, but its default must perform a meaningful baseline gate.

## Deliverable format

Return:

1. concise status;
2. files created or modified;
3. exact source findings with paths and line references;
4. commands executed and exit codes;
5. gates passed/failed/not run;
6. licensing facts and unresolved questions;
7. proposed next task;
8. `git status --short`.

## Stop conditions

Stop and report without speculative edits if:

- the baseline desktop task does not build;
- the Java toolchain cannot be resolved;
- repository state is not clean before work;
- the checked-out source license is inconsistent;
- the requested scaffold would collide with upstream modules;
- a destructive Git action would be required.

No functional GeoCeDG implementation is authorized in this task.
