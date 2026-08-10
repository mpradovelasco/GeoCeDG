# Upstream module map at the GeoCeDG baseline

Status: source characterization
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
GeoGebra: `5.4.928.0`

This map describes the checked-out source. Recommendations are explicitly
marked and do not authorize implementation.

## Composite topology

The root does not declare ordinary subprojects. It composes four included
builds (`settings.gradle.kts:1-6`):

```text
root composite
├─ source/build-logic
├─ source/shared
├─ source/desktop ── includes source/shared
└─ source/web ────── includes source/shared
```

The repeated project name in `:desktop:desktop:run` is intentional: the first
`desktop` selects the included build and the second selects its application
project. From `source/desktop`, the same project is addressed as
`:desktop:run`.

## Included builds and projects

| Build | Declared projects | Responsibility and evidence |
|---|---|---|
| `source/build-logic` | convention plugins | Shared Gradle conventions, task configuration, code-quality plugins, and version extraction. Included through each build's `pluginManagement`. |
| `source/shared` | `canvas-base`, `common`, `common-jre`, `ggbjdk`, `giac-jni`, `xr-base`, `renderer-base`, `editor-base`, `keyboard-base`, `keyboard-scientific` | Cross-frontend kernel and supporting libraries; declarations are at `source/shared/settings.gradle.kts:21-30`. It also includes `source/openrewrite` at line 31. |
| `source/desktop` | `canvas-desktop`, `desktop`, `editor-desktop`, `renderer-desktop`, `jogl2` | Swing/AWT Desktop frontend and JOGL adapter; declarations are at `source/desktop/settings.gradle.kts:21-26`. The included shared build enables local dependency substitution. |
| `source/web` | `canvas-web`, `carota-web`, `editor-web`, `gwt-generator`, `gwtutil`, `keyboard-web`, `renderer-web`, `uitest`, `web`, `web-common`, `web-dev` | GWT/J2CL-facing web frontend and support; declarations are at `source/web/settings.gradle.kts:24-35`. It also includes the shared build. |

The shared and frontend builds use the same version catalog at
`gradle/libs.versions.toml` and resolve GeoGebra releases plus Maven Central
(`source/shared/settings.gradle.kts:9-18`,
`source/desktop/settings.gradle.kts:9-18`, and
`source/web/settings.gradle.kts:12-21`).

## Architectural ownership

| Concern | Current source owner |
|---|---|
| Kernel, construction graph, commands, algorithms, geos, XML contracts | `source/shared/common` |
| JRE implementations and main shared test fixtures/tests | `source/shared/common-jre` |
| 3D kernel types, algorithms, view abstractions, and common renderer contracts | `source/shared/common/src/main/java/org/geogebra/common/geogebra3D` |
| Desktop application, Swing UI, print/export UI | `source/desktop/desktop` |
| Desktop AWT/canvas bridge | `source/desktop/canvas-desktop` |
| Desktop formula renderer and editor adapters | `source/desktop/renderer-desktop`, `source/desktop/editor-desktop` |
| JOGL implementation | `source/desktop/jogl2` |
| Web common application/view code | `source/web/web-common` |
| Full web application and multipage Notes UI | `source/web/web` |
| Renderer-neutral formula code | `source/shared/renderer-base` |

The 3D capability is not a separate Gradle project: its semantic types and
common view code are compiled in `common`; Desktop supplies the concrete JOGL
implementation.

## Desktop build surface

`source/desktop/desktop/build.gradle.kts` applies Gradle's `application` plugin
at lines 5-11, sets `org.geogebra.desktop.GeoGebra3D` as the main class at
lines 75-81, and requests Java 25 for `run` at lines 20-24. Its local project
dependencies include `canvas-desktop`, `editor-desktop`, and `jogl2`, while
`org.geogebra:common`, `common-jre`, and `giac-jni` are substituted from the
included shared build (`source/desktop/desktop/build.gradle.kts:29-41`).

Canonical tasks from the root:

| Purpose | Selector |
|---|---|
| Shared renderer prerequisites | run from `source/shared`: `..\..\gradlew.bat :canvas-base:compileJava :renderer-base:compileJava` |
| Desktop compilation | `:desktop:desktop:compileJava` |
| Desktop launch | `:desktop:desktop:run` |
| GeoCeDG G2 launch | `:desktop:desktop:runGeoCeDG` |
| Desktop unit tests | `:desktop:desktop:test` |
| Installed application distribution | `:desktop:desktop:installDist` |
| ZIP/TAR distributions | `:desktop:desktop:distZip`, `:desktop:desktop:distTar` |

The application plugin also exposes `startScripts`; upstream adds `debugJars`
at `source/desktop/desktop/build.gradle.kts:121-135`. No tracked `jpackage` or
installer task exists at this baseline. **Recommendation:** after a GeoCeDG
launcher/profile and licensing-approved resources exist, use the output of
`installDist` as the clean input directory for a separate `jpackage` task.

## Test topology

The upstream guide identifies unit, component, integration, and external web
UI examples (`doc/dev/Testing.md:1-16`). Main checked-in locations are:

- `source/shared/common-jre/src/test/java` and
  `source/shared/common-jre/src/testFixtures/java`;
- `source/desktop/desktop/src/test/java` and `src/e2eTest`;
- module-local renderer/editor tests;
- `source/web/web/src/test/java`.

`common-jre` uses JUnit Jupiter, test fixtures, and JaCoCo
(`source/shared/common-jre/build.gradle.kts:1-40,53-86`). Desktop enables the
JUnit Platform and Vintage compatibility
(`source/desktop/desktop/build.gradle.kts:55-72`).

## Scaffold decision

The only directories needed for this characterization are `docs/upstream`,
`docs/architecture`, `docs/licensing`, `docs/validation`, `docs/adr`, and
`tools/agent`. They contain durable evidence or the executable baseline gate.

The following roadmap directories remain deferred because Git does not track
empty directories and their governing formats or implementations are not yet
approved: `geocedg/specs`, `geocedg/features`, `geocedg/resources`,
`geocedg/validation`, `models`, `apps`, `python`, `packaging`,
`tools/benchmark`, `benchmarks`, and `artifacts`. No root wrapper or new
`.gitignore` entry is justified: the PowerShell verifier is the authorized
Windows entry point, writes logs to the OS temporary directory, and removes
only output directories it created.
