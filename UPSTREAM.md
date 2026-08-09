# GeoCeDG upstream baseline

## Pinned source

GeoCeDG is based on the official GeoGebra source mirror at
`https://github.com/geogebra/geogebra.git`.

| Item | Value |
|---|---|
| Upstream commit | `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Upstream subject | `Update translation files` |
| Upstream commit date | 2026-08-05T23:17:34+02:00 |
| GeoGebra version | `5.4.928.0` |
| GeoGebra build date | `28 July 2026` |
| Annotated tag | `geogebra-baseline-5.4.928.0` |
| GeoCeDG bootstrap branch | `bootstrap/geocedg-baseline` |

The annotated tag peels directly to the upstream commit, not to a GeoCeDG
bootstrap commit. The canonical machine-readable pin is
`docs/upstream/BASELINE_COMMIT.txt`. The version is defined by
`source/shared/common/src/main/java/org/geogebra/common/GeoGebraConstants.java:29-32`.

The bootstrap branch was reconstructed by replaying only the two pre-existing
GeoCeDG documentation commits on this pin. No source from a later upstream
commit has been incorporated. Starting in G2, the minimum product-profile
overlay inside the inherited tree is recorded file by file in
`docs/upstream/modified-files.yml`. `tools/agent/verify-baseline.ps1` verifies
the pinned ancestry and requires the actual difference under `source/`, the
Gradle build, and `doc/dev/` to equal that controlled register exactly.
The exact upstream README is preserved separately as
`docs/upstream/GEOGEBRA_README.md`; its normalized Git blob must equal the
`README.md` blob stored by the baseline tag. The root `README.md` is now the
GeoCeDG onboarding entry point.

## Remotes and mirror status

| Remote | URL | Role |
|---|---|---|
| `origin` | `https://github.com/mpradovelasco/GeoCeDG` | GeoCeDG fork |
| `upstream` | `https://github.com/geogebra/geogebra.git` | pinned upstream source |

The checked-out upstream README says that the authoritative development
repository is a private GitLab instance and that GitHub is a mirror
(`docs/upstream/GEOGEBRA_README.md:4-8`). It also points contributors at
`https://git.geogebra.org/ggb/geogebra.git`
(`docs/upstream/GEOGEBRA_README.md:38-46`). Therefore:

- the GitHub remote is suitable for fetching and pinning public source;
- GeoCeDG records exact SHAs and never treats a moving GitHub branch as a
  release baseline;
- upstream contribution workflow cannot be inferred from GitHub pull requests
  alone and must be confirmed against GeoGebra's current contribution policy;
- upstream synchronization belongs on a dedicated `sync/geogebra-YYYYMMDD`
  branch and must not be mixed with GeoCeDG feature work.

## Build and runtime entry points

The root is a Gradle composite build. `settings.gradle.kts:2-6` includes build
logic plus the `shared`, `desktop`, and `web` builds. At this baseline the
canonical Desktop selectors are:

```powershell
# From the composite-build root
.\gradlew.bat :desktop:desktop:compileJava
.\gradlew.bat :desktop:desktop:run

# Equivalent direct entry from the included Desktop build
Set-Location .\source\desktop
..\..\gradlew.bat :desktop:compileJava
..\..\gradlew.bat :desktop:run
```

The wrapper is tracked only at the repository root. Gradle selects the
included build from the current working directory when that wrapper is invoked
through the relative `..\..\gradlew.bat` path.

The archived upstream README documents root `:desktop:run`
(`docs/upstream/GEOGEBRA_README.md:28-34`). That selector is stale for this
composite layout; the extra `desktop` segment is the included-build name. The
historical file remains unchanged, while the GeoCeDG root README gives the
correct selector.

The `run` task requests Java language version 25
(`source/desktop/desktop/build.gradle.kts:20-24`). The baseline verification
used Eclipse Temurin `25.0.4+7-LTS` for the application process. Gradle 9.4.1
was launched with Oracle Java 22.0.2; the wrapper version is pinned at
`gradle/wrapper/gradle-wrapper.properties:3`.

The baseline build and interactive launch evidence is recorded in
`docs/validation/baseline_report.md`.

G2 adds the explicit GeoCeDG entry without replacing the baseline route:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG # GeoCeDG profile
.\gradlew.bat :desktop:desktop:run         # Classic diagnostic baseline
```

Both tasks use the same Desktop module and Java 25 application toolchain. The
GeoCeDG launcher is an application-layer overlay; the baseline SHA and version
remain the upstream provenance authority.

## Licensing boundary

The upstream README delegates licensing to
`https://www.geogebra.org/license`
(`docs/upstream/GEOGEBRA_README.md:7-8`). Code, language files, documentation,
UI assets, fonts, installers, services, and trademarks are not assumed to
share one license. The evidence and unresolved release questions are tracked
in `docs/licensing/component-matrix.md`.

## Synchronization rule

Do not merge, cherry-pick, or copy source from commits after
`9b93256b7df401ff056c37b502d82df4d72b1522` onto the baseline branch. A future
sync must:

1. fetch `upstream` on a dedicated sync branch;
2. record the candidate SHA and upstream range;
3. review licensing and serialization changes;
4. run the full GeoCeDG verification authority;
5. merge through a reviewed integration change.
