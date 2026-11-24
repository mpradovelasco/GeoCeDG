# PRE-G9B-MAINT-A2 BS-22: older readers and a BSpline document

Evidence for matrix row BS-22 (`docs/validation/pre_g9b_maint_a_n1_validation_matrix.md`) and
`geocedg/specs/curves/bspline-2d.md` section 8.4. These are probe sources, recipes and
outputs, not registered tests. The candidate report is
`docs/validation/pre_g9b_maint_a2_candidate_report.md`, section 6.

## Contents

| Path | What it is |
|---|---|
| `bspline-fixture.cedg` | native document written by the A2 candidate: `A..E`, `b=BSpline({A,B,C,D,E})`, `M=LocusLength(b)`; its `geogebra.xml` holds `<command name="BSpline">` with `<geocedgBSpline contract="bspline-2d/v1" knots="clamped-uniform/v1"/>` |
| `bspline-fixture.cedg.sha256` | SHA-256 of the fixture: `671ee7ad70ae8e930b84c3940fbc059fbc48811013f9c4830c6db3c708818426` |
| `fixture-writer-probe/`, `fixture-writer.init.gradle` | Desktop probe that writes the fixture through `GuiManagerGeoCeDG.saveAsTo` |
| `older-reader-probe/` | stand-alone probe that opens the fixture with `AppGeoCeDG.loadFile` in a released build |
| `older-reader-1.2.2.out.txt` | its output against GeoCeDG 1.2.2 |
| `base-reader-probe/`, `base-reader.init.gradle` | shared probe that loads a simulated BSpline v1 document with the reader of the base |
| `base-reader-228580e96.out.txt` | its output (system-out of the JUnit run) |

The fixture was written on 2026-11-24 by the A2 working tree after the library merge
`0207b605b`, before commit `b250bb1e9`; the writer path (command, codec, family element) is
the one committed. A zip's entry times make a new run produce different bytes; the SHA-256
identifies the file the 1.2.2 output below was obtained from.

## Recipes

Fixture writer (Desktop test task; holds the station lock for Desktop Gradle tests):

```text
gradlew.bat :desktop:desktop:test --console=plain --no-problems-report
  --init-script geocedg/validation/pre-g9b-maint-a2/bs22-older-reader/fixture-writer.init.gradle
  --tests org.geocedg.desktop.A2FixtureWriterProbeTest
  -Da2.probeSource=<absolute path of fixture-writer-probe>
  -Da2.fixtureOut=<absolute path of the output .cedg>
```

Older reader (no Gradle; the release jars are read, never modified):

1. Take the application image of the release, here
   `artifacts/packaging/windows-1.2.2-nc-candidate-4/app-image/GeoCeDG/app`. Its
   `geocedg-distribution.json` gives version 1.2.2 and `source_revision`
   `a6562fa30dc7a468b8e62e16843f1ae2f440285b`; tag `v1.2.2` (`1b54ff95d`) adds only
   documentation to that commit, so the code is the released one.
2. Compile with JDK 25: `javac -d classes -cp "<app>\*"
   older-reader-probe\org\geocedg\desktop\A2OlderReaderProbe.java`.
3. Run isolated: set `APPDATA` and `LOCALAPPDATA` to empty directories outside the repository,
   then `java -Duser.home=<iso> -Djava.io.tmpdir=<iso>\tmp --add-exports
   java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED
   --add-exports java.desktop/sun.java2d=ALL-UNNAMED --enable-native-access=ALL-UNNAMED
   -cp "classes;<app>\*" org.geocedg.desktop.A2OlderReaderProbe <fixture.cedg>
   <iso>\settings.properties`.

The probe creates `Kept=(7,7)`, opens the fixture and prints whether it loaded, whether `Kept`
survived, the object count, the typed load messages and the logged exception chain.

Base reader (shared test task on a tree whose product sources are those of `228580e96`; the
Stage 1 run used the untouched A2 worktree at `97d16d50e`, which differs from `228580e96`
only by the task prompt):

```text
gradlew.bat :shared:common-jre:test --console=plain --no-problems-report
  --init-script <path>/base-reader.init.gradle
  --tests org.geocedg.a2probe.A2BaseReaderProbeTest
  -Da2.probeSource=<absolute path of base-reader-probe>
```

## Results

GeoCeDG 1.2.2 (`older-reader-1.2.2.out.txt`): `loaded=false`; `Kept` is still present and is
the only object; no typed message (1.2.2 has no BSpline messages). Logged chain: `unknown tag in
<command>: geocedgBSpline`, `CommandNotFoundError: Unknown command : BSpline`, the dependent
`LocusLength` errors and finally `SpatialIdentityException: MALFORMED_RECORD` (construction
identity base contract disagrees with the attached geo), which rejects the whole file. No
partial document is presented as complete.

Base reader (`base-reader-228580e96.out.txt`): an unknown family element inside a SplineV2
command is only logged (`P1`, loads); a BSpline v1 document is rejected with
`MALFORMED_RECORD` and the previous construction is restored (`P2`); without the identity
section it is rejected with `GEO_ATTACHMENT_MISSING` (`P3`).
