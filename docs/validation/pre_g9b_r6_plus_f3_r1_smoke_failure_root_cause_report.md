# PRE-G9B-R6-plus-F3-R1 — author smoke failures: root-cause report

```text
TASK                 = PRE-G9B-R6-plus-F3-R1-SMOKE-FAILURE-CORRECTION
ORIGINAL CANDIDATE   = 165471154cd16e8875994a21eb49fbc2f828216b
                       tree 8345c54df55452ffc598c431db28e67298076589 (frozen; unchanged)
AUTHOR_SMOKE (F3)    = FAIL — BLOCKING (author report, SF-F3-1 to SF-F3-5)
EVIDENCE CLASS       = runtime evidence on the original candidate (real product window in
                       the test JVM, real author files, child-JVM window close) and source
                       evidence; no inherited or shared code changed
selfApproved         = false
```

## 1. Author documents (read-only evidence)

The originals in `C:\Temp` were hashed, copied to a disposable scratch directory and
never written, renamed or committed.

| File | Size | Last write | SHA-256 |
|---|---|---|---|
| `C:\Temp\CircleM.cedg` | 17963 | 2026-10-13 08:19:53 | `25fc9f3adad9c87661798547420bf54a43522d1ab5af3fa4f78219190cf82a43` |
| `C:\Temp\LocusV2.cedg` | 21072 | 2026-10-13 08:20:44 | `4f8d290830b7afc140e29136ba65e9b3d91d66c6f9fcb30326f932db12780ead` |

The smoke names `locusV2.cedg`; the file on disk is `LocusV2.cedg` (Windows names are
case-insensitive). `CircleM` is `CircleM.cedg`.

| Document | Units | Construction | Identity records | Macros | Scripts |
|---|---|---|---|---|---|
| CircleM | `mm` | `A`, `B`, `f = Line(A, B)`, `A_{1}`, `B_{1}`, `c = Circle(A_{1}, B_{1})` | none | none | none (empty library JavaScript) |
| LocusV2 | none | `A`, `B`, `c = Circle(A, B)`, `C = Point(c)`, `f = Line(C, xAxis)`, `D = Intersect(f, yAxis)`, `E = Midpoint(D, C)`, `a = LocusV2(E, C)` | `c`, `C`, `E`, `a` (`geocedgSpatial` v1; families CONIC, POINT, POINT, LOCUS_V2) | none | none |

Label collisions between them: `A`, `B`, `c`, `f`. Every object of LocusV2 is in an
admitted row of `P4` (elementary points, circle, line, intersection, midpoint; the
Locus V2 family with its identity participants); the source needs no macro.

## 2. Reproduction on the original candidate

A scratch probe started the real product (`GeoCeDG.main`, isolated settings file) in a
shown window, opened disposable copies of the author files through the ordinary load
route, ran Insert File on the event dispatch thread, used Open Recent
(`LoadFileListener`, the menu's own listener) and New, and recorded the static
application-window list (`GeoGebraFrame.getInstances()`) after every step.

| Step | Observation (original candidate) |
|---|---|
| start | 1 registered window (the user window) |
| open CircleM, Insert LocusV2 | `REJECTED_BEFORE_MUTATION`: "LocusV2.cedg was not inserted: the trial insertion failed (closure f)"; registered windows **1 → 4**: three hidden, never shown frames whose applications are insertion helpers (two with current file `LocusV2.cedg`, one the dry-run scratch host); the static active window became the last helper frame |
| Open Recent `LocusV2.cedg` | the user window keeps `CircleM.cedg`: the hidden helper frame holding `LocusV2.cedg` was found and "focused" instead of loading |
| New | user title `GeoCeDG 1.0.0 (1)` (instance count > 1, user window index 0) |
| open LocusV2, Insert CircleM | `INSERTED` (labels renamed), registered windows +3 again; Open Recent `CircleM.cedg` then redirected to a hidden helper frame |
| three repeated attempts | windows 7 → 10 → 13 → 16 (+3 per attempt) |
| helper step trace | a helper registers its frame **at construction** (`newInsertionHelper`), before any load; release removed the dock manager and key dispatcher but not the frame |
| exit decision | 17 registered windows besides the user window, so closing it never reaches `AppD.exit(0)` |

The permanent shown-window test reproduced the exit failure in a child JVM on the
original candidate: after the real `WINDOW_CLOSING` of the only visible window, 3
windows stayed registered, `AWT-EventQueue-0`, `AWT-Shutdown` and `DestroyJavaVM` were
alive and the process did not end (the test watchdog had to halt it with code 3).

## 3. Root causes

**RC-1 Auxiliary lifecycle (SF-F3-1 aftermath, SF-F3-2, SF-F3-3, SF-F3-4).** Each
hidden helper (`AppGeoCeDG.newInsertionHelper`, three per insertion: source, dry-run
scratch and dry-run source) is an application with a GUI; constructing it creates a
`GeoGebraFrame`, whose constructor adds it to the static window list and makes it the
active instance. The inherited `AppD.insertFile` disposes that frame after use ("this
is also needed to make it possible to load the same file once again"). The F3
`releaseInsertionHelper` deliberately avoided `getFrame()` (which creates a frame) and
therefore never disposed the frame already registered. Consequences, each shown above:

- `GeoGebraFrame.getInstanceWithFile` returns a hidden helper frame whose application
  still holds the source as current file, so `LoadFileListener` (Open Recent) and
  `doOpenFiles` focus it instead of loading: **SF-F3-2**;
- `GuiManagerD.updateFrameTitle` appends the instance number when more than one window
  is registered: **SF-F3-3** (it disappears while a file is current only because the
  title then shows the file name);
- `GeoGebraFrame.setVisible(false)` calls `AppD.exit(0)` only when the window list is
  empty; with hidden helpers registered, closing the last visible window only hides it,
  the frame's AWT peer keeps the non-daemon event-dispatch thread alive and the JVM, and
  with it the Gradle `runGeoCeDG` task, never ends: **SF-F3-4**. Gradle itself is not
  defective.

The leak does not depend on the insertion outcome (rejected and successful insertions
each add three windows), which is why both directions were followed by abnormal
behaviour (**SF-F3-5**). The focused F3 tests did not see it: their embedded hosts never
show windows and the shared test lifecycle extension silently disposes every frame a
test creates.

**RC-2 Locus V2 rejection (SF-F3-1).** The rejection came from the strict dry run, stage
SEMANTIC VERIFICATION, class "closure": `f = Line(C, xAxis)` and `D = Intersect(f,
yAxis)` take the built-in axes as inputs. The closure check required every labeled
input of an inserted algorithm to be an inserted object; the axes are construction
constants (`GeoAxis.isLabelSet()` is always true) present in every document, never
objects of the target, so a valid, admitted construction was rejected. Admission and
the matrix were right; the verification was wrong. The reverse direction succeeded
because CircleM has no axis dependency. Diagnostic classification: **DRY-RUN
VALIDATION FAILED — closure check defect for built-in axes**; outcome after
correction: **SUPPORTED — CORRECTED**.

**RC-3 Diagnostics.** Unexpected exceptions before mutation were reported as "it is not
a valid GeoCeDG document" and an exception inside the dry run as "commit", losing the
cause (the exception was kept in the result, not in the message).

No evidence was found of a stuck isolation, loading or disabled-action state: after
every probed attempt `isInsertionIsolated()` was false and the document was saved.

## 4. Corrections (GeoCeDG Desktop only)

| Root cause | Correction |
|---|---|
| RC-1 | `AppGeoCeDG.releaseInsertionHelper` now also unregisters every `GeoGebraFrame` whose application is the helper (`dispose()`, which only removes a never-shown frame from the static list, as the inherited `insertFile` does), clears the helper's current file and restores the window that was active before the helper was created (recorded by `newInsertionHelper`); each cleanup step is guarded separately. Legitimate windows are never touched; the static list is never cleared wholesale |
| RC-2 | `GeoCeDGDocumentInsertion.verify` closure: a labeled input outside the insertion is accepted only when it is the target's x-axis (or y-axis) and the source algorithm used the source's x-axis (or y-axis) at the same position; any other outside input still fails (`closure`) |
| RC-3 | rejections keep the stage and the exception class and message (`Insert.Reject.Internal`, `commit: <exception>`) |

## 5. Results after correction

Same probe and real files on the corrected tree: CircleM → Insert LocusV2 **INSERTED**;
LocusV2 → Insert CircleM **INSERTED** with conflicting labels renamed; registered
windows stay **1** after every insertion, Open Recent loads `LocusV2.cedg` and
`CircleM.cedg` into the user window, New shows `GeoCeDG 1.0.0`, three repeated
insertions leave 1 window, the helper trace shows the helper frame unregistered by
release, and closing the user window would reach `AppD.exit(0)`. In the child-JVM test
the process ends with code 0 after the real window close, for both directions.

## 6. Remaining uncertainties

- The child-JVM test uses the product entry point `GeoCeDG.main` and the real window
  close; it runs with the test JVM's launcher and arguments, not the `runGeoCeDG`
  Gradle task (Temurin 25 launcher, `desktopJvmArgs`). The cause (no `AppD.exit`) is
  launcher-independent; the author smoke confirms the Gradle route.
- The macOS sandbox file-dialog branch and `MacApplicationListener` (which reads the
  active window) were not exercised.
