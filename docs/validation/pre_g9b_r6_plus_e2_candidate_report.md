# PRE-G9B-R6-plus-E2 — native dimensions: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the candidate commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-E2
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E2
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)
REPRESENTATION            = β (existing GeoElement types only)
COMMANDS                  = AlignedDimension / LinearDimension (CotaAlineada / CotaLineal)
DQ-E2-6                   = ALIGNED NORMATIVE-STYLE TEXT IMPLEMENTED
PRODUCT CHANGE            = YES (two commands, two tools, the aligned value drawable,
                            bounded LaTeX output for dimension outputs)
SERIALIZATION CHANGE      = ADDITIVE ONLY (two <command> names; no new element type,
                            attribute or reader change)
GUIDE_IMPACT              = UPDATED
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
AUTHOR_SMOKE              = PENDING (required; section 16)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval. The candidate commit cannot name
itself, so its identity and the two acceptance runs made on it are reported
outside this file. The machine-readable mirror is
[`pre-g9b-r6-plus-e2-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-E2` for implementation and technical
verification on 2026-10-08, with the frozen decisions `DQ-E2-1` to `DQ-E2-11`
of the [author-decision record](pre_g9b_r6_plus_e2_author_decisions_record.md)
and the [authorization record](pre_g9b_r6_plus_e2_authorization_record.md),
which prevail over the summary of the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md).

| Identity | Value |
|---|---|
| published base `P_R6PLUS_E1_P_X1` (`main`, `origin/main` and live remote `main` at the entry gate; clean worktree) | `dc63b0e55f12eb96d3aea359db4476cbda6c89dc`, tree `8b2b5027a2119f996de8ca5af91b03ce32731a8c` |
| implementation base `B_R6PLUS_E2_IMPL` (decision freeze, not published) | `b1ee68c0f73b6c39fe8cd306e52f58bb66330c5b`, tree `7eabf6892ab00c6adc3b510e9ffac204ae13c7a8` |
| authorization record commit | `4a7043f2b5f3dd7f5ac15433060a1a69835cb8d5`, tree `82b2eff291c02c0413e37ce470dec18704ca461a` |
| implementation state used for the memory characterization (not the candidate) | `8cfb201158d5d12c3e1a4391505e36ee2fcc054a`, tree `e4b89153a2fe9d8c2bdf4fb5ce799348f1bb57c3` |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-plus-e2-native-dimensions` |

The preparation commits `6ad853c6` and `b1ee68c0` and the authorization commit
were not amended. The implementation is two commits on top of `4a7043f2`: the
implementation state `8cfb2011` that the author-authorized memory
characterization (§18) was measured on, and the candidate commit that contains
this report (test-lifecycle correction, verification data and documents). No
push, merge, tag or release was made.

## 2. What changed

### 2.1 Shared kernel

- `Commands.AlignedDimension` and `Commands.LinearDimension` (`TABLE_GEOMETRY`),
  their `CommandDispatcher` cases and `BasicCommandProcessorFactory` entries,
  and the processors `CmdAlignedDimension` (3 or 5 arguments) and
  `CmdLinearDimension` (4 or 6 arguments, line or 2D vector direction).
  `Dimension` and every existing command are unchanged.
- `org.geocedg.common.kernel.dimension` (new): `DimensionFigure2D` (pure
  geometry and the degeneracy contract), `AlgoNativeDimension` with
  `AlgoAlignedDimension` and `AlgoLinearDimension` (five outputs of existing
  types: value `GeoNumeric`, dimension line and two extension lines
  `GeoSegment`, value `GeoText`; geometry-only `compute()`; a presentation step
  outside `compute()`), `DimensionPresentation` (value string over
  `UnitState.display`, failure marker `?`, one keyed refresh listener per
  construction), `DimensionTextSource` (the text-geometry interface) and
  `package-info`.
- `DocumentUnitSystem.addListenerOnce(key, runnable)`: an idempotent keyed
  registration for the presentation refresh; unit state, persistence and
  semantics are unchanged.
- `EuclidianConstants`: modes `142` (`MODE_ALIGNED_DIMENSION`) and `143`
  (`MODE_LINEAR_DIMENSION`) with their tool texts.
- Command and menu bundles (`command*.properties`, `menu*.properties`): names,
  aliases `CotaAlineada` / `CotaLineal`, syntax, tool names, help and the
  invalid-placement message (new lines in LF; the mixed-EOL bundles keep their
  bytes).

### 2.2 Shared renderer

`org.geocedg.common.euclidian.draw.DrawDimensionText` (new) and one routing
branch in `EuclidianDraw` `case TEXT`: only the value text of a native
dimension takes the new drawable; every other text keeps `DrawText`. The
drawable paints an ordinary `DrawText` of the same text inside a rotation by the
reading angle `θ = φ − 180·⌈(φ − 90)/180⌉`, lifted by half the text height plus
2 px, and beyond the extension-line tips on the overshoot side when the value
plus 4 px at each end is longer than the projected line; hit testing, the
selection outline and the bounds follow the rotated rectangle. Nothing is
stored.

### 2.3 Desktop

- `GeoCeDGDimensionTools` (new) and `GeoCeDGEuclidianController`: the two tool
  modes (point, point, [line or vector,] placement click), creation of free
  hidden labelled `Offset`, `Overshoot` and `Gap` numbers, capture of 2 mm /
  1 mm on paper (physical unit and drawing scale) or 8 px / 4 px of the view,
  the invalid-placement notice, one undo point per creation; the Move-mode drag
  of any dimension output that writes only an independent, labelled, unlocked
  offset, one undo point on release; points hit first keep the inherited
  behavior.
- `GeoCeDGToolImageResource`, two owned SVG icons
  (`mode_geocedg_aligneddimension.svg`, `mode_geocedg_lineardimension.svg`,
  GeoCeDG-authored ordinary artwork, CC-BY-4.0 class) and their
  `assets-manifest.yml` entries.
- `apps/geocedg/application-profile.yml`: `measure.aligned-dimension` and
  `measure.linear-dimension` (experimental, product-only, semantic-command
  surface) after `measure.distance-length` in the construction-metrics group
  and the metrics-validation toolbar cluster; `GeoCeDGProfile` pin 125 → 127.
- `DimensionLatexExport` (new) and the GeoCeDG PSTricks, PGF and Asymptote
  exporters: `drawGeoSegment` and `drawText` overrides for dimension outputs
  only (both arrow endings; rotated value centred on the dimension line and
  lifted to its reading-up side).

### 2.4 Verification data and documents

Specification [`native-dimensions.md`](../../geocedg/specs/dimensions/native-dimensions.md)
and [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md)
(`PROPOSED`); EN/ES user guides (§3.2 table, new §9.5, §16.1 rows, §16.2
syntax); `geocedg/specs/ui/cedg-workspaces.md` count prose; the GGBScript
matrix (36 rows, 28 lookup probes, fixture `native-dimension`); the
regenerated R5-B fingerprint fixture (§6); `docs/upstream/modified-files.yml`
(29 purposes extended, 16 added entries); the phase registration and catalog
pins (§12); the approved-amendment allow-list of
`tools/agent/workspace-profile-validation.ps1`; the test-only lifecycle
correction of ten pre-existing Desktop test classes and `G9U1TestApp` (§18).

## 3. Deviations and decisions taken during implementation

| Item | Fact |
|---|---|
| `DrawDimensionText extends DrawText` (prompt §Allowed scope) | `DrawText` is `final`; the drawable extends `Drawable` and wraps a `DrawText` delegate of the same text. Same behavior, no upstream change to `DrawText`. |
| default labels (authorization record: "visibility and default labels documented") | a single label names the value (`d=AlignedDimension(A,B,1)` → `d`); the other outputs take default labels; XML labels name the outputs in order. The inherited multi-output rule would have produced `d_1 … d_5`. Labels of the three segments are hidden. |
| order of deliverables | the specification and ADR were written after the product code, not before it as the prompt asks; their content follows the frozen decisions and the measured implementation. |
| older readers (`K16`) | the measured archived-base behavior differs from the `K16` inference: outputs survive as independent objects, they are not absent (§8). Spec, ADR and guides state the measured behavior. |
| text bounding box | the drawable sets the text's bounding box (as `DrawText` does) to the axis-aligned bounds of the rotated value; it is view-transient, never in XML. |
| `final.desktop` heap | before the correction, 3 of 5 full Desktop runs ended with `OutOfMemoryError` in `SvgLoadTest.testLoadSvgsGGB`; the author authorized a focal characterization (§18), which proved test-lifecycle retention (M1) and a test-only correction; the Desktop test heap is unchanged (512 MB). |
| E2 GGBScript matrix rows | `alignedDimensionRows` and `linearDimensionRows` execute their rows in a fresh JVM (the E2 Desktop scenario harness); same rows, same assertions. |

## 4. Characterization re-established (`K1`–`K20`)

| Item | At implementation |
|---|---|
| `K1` governing inputs | unchanged; the authorization record is the added authority |
| `K2` upstream `Dimension` | unchanged; regression test values, alias and syntax |
| `K3` command inventory | ADR 0031 gate 564/513/486 → **566/515/488** measured; registration path as characterized |
| `K4` GGBScript matrix | derived inventory 11 → 13 commands; +36 rows, +28 probes; completeness gate and negative control pass |
| `K5` profile and pins | 125 → **127** actions, 47 → 49 native toolbar modes, 71 → 73 mode-carrying actions, 57 → 59 toolbar actions; every pinned test updated |
| `K6` mode and controller | modes 142/143 dispatched in `GeoCeDGEuclidianController` |
| `K7` direction | `GeoLine.getDirection` or vector coordinates; `getDirectionInD3` never used (test) |
| `K8` unit seam | presentation step outside `compute()`; keyed listener in `DocumentUnitSystem`; unit changes update only texts (recording view) |
| `K9` text anchoring | anchor point at the dimension-line midpoint; bounding box refreshed after alignment |
| `K10` arrow endings | `SegmentStyle.ARROW_FILLED` at both ends of the dimension line |
| `K11` β figure | implemented as characterized |
| `K12` exporters | picture via drawables; LaTeX overrides; DXF subset |
| `K13` drag | Desktop interception; `MoveGeos.moveObjects` on an output never moves `A` or `B` (test) |
| `K14` owned icons | pipeline unchanged; two icons, manifest entries, icon review test |
| `K15` legacy macro collision | no collision with the new heads; `Templatev7.ggb` and a user store with `directDimension` / `axisDimension` keep working, no byte changes (test) |
| `K16` older readers | measured on the archived base: §8 |
| `K17` α surface | not implemented |
| `K18` registry and class | `INTEGRATED_PHASE`; phase registered (§12) |
| `K19` observations | §14 |
| `K20` paths | §17 |

## 5. Aligned value re-established (`A1`–`A10`)

`A2` reading rule implemented in `DrawDimensionText.readableAngle`; `A8` seam
implemented as §2.2. The `A5` measurement is reproduced on the real drawable by
`PreG9BR6PlusE2NativeDimensionDesktopTest.alignedValueAngleSweep`: angles 0°,
30°, 44°, 45°, 46°, 89°, 90°, 91°, 135°, 179°, both input orders, both offset
signs (40 cases): worst parallel deviation **2.29°**, worst centring offset
**1.82 px**, **0** crossings of the dimension or extension lines, readable in
every case; horizontal reads at 0°, vertical at 90° (bottom to top).
`alignedValueRobustness` adds zoom, resize, anisotropic axes, `mm`, `m`, `in`
(`usm`) suffixes and a long value on a short line (all within the same bounds).
`A6`/`A9` export and residual observations are dispositioned in §11 and §14.

## 6. ADR 0031 gate and the regenerated fingerprint fixture

The R5-B fixture records the `P_DOTXML` regime (before canonical heads). It was
regenerated by the original R5-B probe, rerun from the scratchpad on the
candidate tree, and spliced so that it differs from the committed fixture only
by the additive E2 entries:

- every regime-independent summary (`tokenUniverse`, `scriptResolution`,
  `xmlExactResolution`, four `reverseTables`, four `userResolution`,
  `syntax.en.full`) equals its `P_DOTXML` hash once the lines naming the two
  commands or their aliases are removed (4, 2 or 10 lines);
- `commandNames.en` and the five dictionaries gain exactly the two names (EN) or
  the two aliases (ES);
- the `P_DOTXML`-regime Spanish blocks (`display.es`, `argumentNumberErrors.es`,
  `scripts.es`, `historicalDocument`) are kept byte for byte; the committed
  historical `.cedg` is unchanged;
- `syntax.es.text` takes the two Spanish syntax entries from the Classic
  alias-head regime, after proving that the Classic Spanish syntax without them
  equals the committed `P_DOTXML` text.

## 7. Tests

| Obligation | Evidence |
|---|---|
| `T-E2-VALUE`, `T-E2-GEOMETRY`, `T-E2-DEGENERACY`, `T-E2-PRESENTATION`, `T-E2-SERIALIZATION` (kernel) | `PreG9BR6PlusE2NativeDimensionTest` (24 tests): contract and order, labels, signatures, every direction route, sign invariance, `getDirection` versus `getDirectionInD3`, side, no flip over a 360° drag, gap and overshoot, degeneracy table, `120 mm` / `0.12 m` / `12 cm` / `0.47 in` / `usm`, failure marker, unit change updates only texts, DAG recompute, pure figure, save/reopen with byte-identical XML, undo/redo, redefine, readable angle, Classic config |
| `T-E2-TEXT-ALIGN`, `T-E2-TOOL`, `T-E2-EXPORT`, `T-E2-SERIALIZATION` (Desktop), `T-E2-COEXISTENCE`, `T-E2-PROFILE` | `PreG9BR6PlusE2NativeDimensionDesktopTest` (14 scenarios, each in a child JVM): profile, both tools, drag with one undo point, sweep, robustness, picture export at scales 1/2/3, rotated hit testing, `Corner`, LaTeX, DXF, persistence and copy, legacy macros, EN/ES names and scripts |
| `T-E2-COMMANDS` | `PreG9BR5BCanonicalCommandGateTest`, `PreG9BR5BCanonicalDisplayTest`, `PreG9BR5BR1RuntimeLanguageSwitchTest`; `SelfTest` with `GeoCeDGCommandsTest.cmdAlignedDimension` / `cmdLinearDimension`; `SyntaxLocalizationTest`; `CommandsValidationTest` |
| `T-E2-GGBSCRIPT` | `PreG9BR6CapabilityMatrixTest` (15 → 17 methods, `alignedDimensionRows`, `linearDimensionRows`) |
| `T-E2-COMPAT` | §8 (scratch probe on the archived base) |
| `T-E2-GUIDE` | `PreG9BR5AGuideOutlineTest` (103 entries), `PostP1BilingualUserGuideTest`, `PreG9BP1PublicSurfaceTest` |
| `T-SMOKE` | pending (author), §16 |

LaTeX compilation in the Desktop test with MiKTeX: `pdflatex` (PGF), `latex`
(PSTricks) and `asy` (Asymptote) all exit 0.

Complete suites: `:shared:common-jre:test` 6 951 tests, 0 failures, 10
skipped; `final.desktop` after the test-lifecycle correction: three consecutive
runs of the producer's exact Gradle invocation, 1 890 tests, 0 failures, 1
allowed skip each (§18); the executed producer evidence of every refreshed
selection is in the evidence JSON. Before the correction, two guide byte-copy
failures of one run came from a guide edit made while it ran, and 3 of 5 full
runs ended in `OutOfMemoryError` (§18).

## 8. Older readers (`T-E2-COMPAT`)

A document written by the candidate (points, a line, a segment,
`d=AlignedDimension(A,B,1)`, `e=LinearDimension(A,B,g,-1)` and
`M=Midpoint(<dimension line of d>)`) was loaded by a scratch probe on a
`git archive` of `B_R6PLUS_E2_IMPL`, in the GeoCeDG and the Classic
configurations (identical results): load errors "processing of command:
AlignedDimension(A, B, 1)" and "… LinearDimension(A, B, g, -1)"; `A`, `B`, `g`
and `s` intact; the values `d` and `e` kept as independent numbers; the six
segments and two texts present but undefined; `M` undefined; a re-save contains
neither command. The candidate reads the same document without errors in both
configurations.

## 9. GGBScript capability matrix

Rows for `AlignedDimension` (2 forms) and `LinearDimension` (4 forms) × three
surfaces × two locales, all `SUPPORTED`; 14 lookup probes per command with the
same regime verdicts as the other commands whose head equals the identity;
fixture `native-dimension`. Instance hash `91906cd7…` → `6cdecded…` re-pinned in
the static contract.

## 10. Profile, icons and allow-list

The two actions are added to the approved amendments of
`tools/agent/workspace-profile-validation.ps1` (R4/A-1/B/D1 precedent; reported
as a deviation for author review), re-pinned `92c61af0…` → `75c900b1…`;
`assets-manifest.yml` `d8375884…` → `a4c4f98b…`; the old pins were reproduced
from `HEAD` with `Get-VerificationCanonicalTextSha256`.

## 11. Export

Picture formats: complete figure with arrows and the aligned value (PNG at three
scales tested). LaTeX: arrows, rotation, centring and lift for dimension
outputs only; other output unchanged. DXF: three `LINE` entities (dimension and
extension lines), no `DIMENSION`, `TEXT` or `MTEXT`; the two numbers and the
text are reported `OUTSIDE_GEOMETRIC_POPULATION`.

## 12. Verification data

- Registry: phase `PRE-G9B-R6-PLUS-E2` (`compile.shared.semantic`,
  `compile.desktop.semantic`, `workspace-profile.product`,
  `junit.shared.pre-g9b-r6-plus-e2.semantic`,
  `junit.desktop.pre-g9b-r6-plus-e2.semantic`,
  `static.ggbscript-matrix-inputs.semantic`, `ggbscript-matrix.product`) and the
  four JUnit nodes.
- Inventory: selections `pre-g9b-r6-plus-e2.shared` and
  `pre-g9b-r6-plus-e2.desktop`, refreshed with the official updater from
  passing executed producer evidence, together with every executed selection
  whose filters cover a test class that gained methods (`final.shared`,
  `final.desktop`, `pre-g9b-r4.shared`, `pre-g9b-r6.shared`,
  `pre-g9b-r6.desktop`, `pre-g9b-r6-plus-a1.shared`,
  `pre-g9b-r6-plus-b.desktop`, `pre-g9b-r6-plus-c.desktop`) and the dry-run
  discovery of both modules; counts in the evidence JSON.
- Registry catalog pins and the registry-shape pins of
  `verification-final-coverage.Tests.ps1` (44 selections, 51 phases).
- Development runs before the commit: see the evidence JSON (`preFreeze`).

## 13. Checks

| Check | Result |
|---|---|
| Checkstyle `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | exit 0; one pre-existing warning in an untouched file (`PreG9BR6PlusA1HiddenLayerTest.java:188`, line length 101) |
| upstream boundary (`Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b`) | passes, 989 registered files |
| `git diff --check` | clean |

## 14. Observations

| Identifier | Disposition proposed in this candidate |
|---|---|
| `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` | naming side resolved by `DQ-E2-7`; coexistence validated (`K15`); technically resolved, pending author review |
| `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` | LaTeX resolved for dimension outputs; DXF becomes the durable limitation below |
| `OBS-R6PLUS-E2-DEPENDENT-DRAG-MOVES-MEASURED-POINTS` | resolved by the Desktop drag interception (test) |
| `OBS-R6PLUS-E2-READING-FLIP-AT-VERTICAL` | documented in the specification and guides (presentation only) |
| `OBS-R6PLUS-E2-VALUE-LONGER-THAN-DIMENSION-LINE` | handled by the lift beyond the extension tips; arrowheads overlap on very short lines (known limitation) |
| `OBS-R6PLUS-E2-LATEX-TEXT-PACKAGE` | avoided: plain text, no `\text` |
| `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET` | durable limitation, recorded in specification and guides |
| `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION`, `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE`, `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED`, `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | pre-existing; unchanged; not `E2` scope |
| `OBS-R6PLUS-E2-OLDER-READER-OUTPUTS-SURVIVE` (new) | measured older-reader behavior differs from `K16`'s inference (§8); documented |
| `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION` (new) | ten pre-existing Desktop test classes created embedded hosts without `G9U1TestApp.Lifecycle`, retaining about 80 hosts (≈ 4.9 MB each) in the 512 MB test JVM; corrected test-only in this candidate (§18); about 16–17 transient hosts remain, without accumulation; technically resolved, pending author review |
| dimension inside a user macro | bare value (macro unit state is empty); known limitation |
| `Corner` of the value text | axis-aligned bounds of the rotated value; bounded limitation |

## 15. Known limitations

See [specification §11](../../geocedg/specs/dimensions/native-dimensions.md#11-known-limitations).

## 16. Author smoke checklist (EN and ES)

1. Create an aligned and a linear dimension with the tools, in English and in
   Spanish (and by typing `CotaAlineada` / `CotaLineal` in Spanish).
2. Change the construction unit and the presentation unit (Document units): only
   the texts change; the value and the figure stay.
3. Rotate a dimension (move `B`) through the vertical: the value stays parallel
   and readable; the reading direction turns at the vertical.
4. Drag the dimension line (and the value) with Move: only the offset changes;
   `A` and `B` stay.
5. Undo and redo the creation and the drag (one step each).
6. Save, close and reopen: identical dimension.
7. Copy and paste a dimension with its points.
8. Open `Templatev7.ggb`; use `directDimension` / `axisDimension` and the new
   commands in the same document.
9. Export PNG, PDF and SVG (arrows and aligned value).
10. Export PSTricks, PGF/TikZ, Asymptote and DXF (DXF: lines only, no value).

## 17. Changed paths

Tracked delta of the candidate against `4a7043f2`: listed in the evidence JSON
(`changedPaths`).

## 18. Desktop memory and test-lifecycle characterization

Author instruction of 2026-10-08 (focal characterization inside the authorized
E2 implementation; no heap increase, no `SvgLoadTest` change, no reordering or
exclusion; test-only lifecycle correction if M1 is proven). Measured on the
implementation state `8cfb2011` and on `B_R6PLUS_E2_IMPL` in a detached scratch
worktree with git provenance (removed afterwards), same JDK, same Gradle
invocation, default 512 MB test heap, runs never concurrent. Mechanism (scratch
only, never committed): a JUnit Platform listener taking a live-object class
histogram (full GC) at the plan start, after every test class and immediately
before `SvgLoadTest`; a single-app probe; a reflective strong-reference search
from static fields and threads.

| Checkpoint (live MB after full GC) | base | candidate before correction | candidate after correction |
|---|---|---|---|
| A — plan start | 14 | 14 | 14 |
| B — after `PreG9BR6CapabilityMatrixTest` / the E2 Desktop class | 409 / — | 419 / 422 | 119 / 114 |
| C — immediately before `SvgLoadTest` | 468 | 480 | 163 |
| `AppGeoCeDG` / `GeoGebraFrame` / `DockManagerD` alive at C | 95 / 94 / 95 | 96 / 95 / 96 | 17 / 15 / 15 |
| `JMenuItem` / `ImageIcon` / `BufferedImage` at C | 23 269 / 34 945 / 43 693 | 24 341 / 36 509 / 45 728 | 4 002 / 6 376 / 8 259 |

- **Single app** (fresh `G9U1TestApp.create()` with its menu bar, after full
  GC): base 6 288 KB, candidate 6 368 KB per live host (+80 KB, +1.3 %: six
  menu items and their scaled icons per added action). A dropped host is
  collectible on both trees.
- **Retention (M1).** A host built with the raw constructor
  (`new AppGeoCeDG(…, new JPanel())`) and dropped is **not** collectible on
  either tree; it is held by the `KeyboardFocusManager` key-event dispatchers,
  its `DockManagerD` registered as a Toolkit AWT listener and the
  `GeoGebraFrame` instance registry; strong path found:
  `static GeoGebraFrame.activeInstance → GeoGebraFrame.app → AppGeoCeDG`. The
  same host under the existing `G9U1TestApp.Lifecycle` is collected. The
  per-class timeline attributes the accumulation to ten pre-existing classes
  without that owner: `G9U0R3InspectorWorkflowTest` (+18),
  `G9S1R1NativeArchivePersistenceTest` (+12), `GeoCeDGDocumentLifecycleTest`
  (+10), `G9U0R5NativeArchivePersistenceTest` (+9),
  `G9U0R4NativeArchivePersistenceTest` (+9),
  `G9U0R6NativeArchivePersistenceTest` (+6), `G9U0R3MenuLifecycleTest` (+6),
  `G9S1NativeArchivePersistenceTest` (+4), `locus.G9U0ToolSurfaceTest` (+4),
  `locus.LocusV2DesktopLifecycleRegressionTest` (+2). No E2 test class retains a
  host (the E2 Desktop scenarios run in child JVMs).
- **Root cause.** Test lifecycle (M1), pre-existing at the base; the candidate's
  +80 KB per retained host (about +12 MB over ~96 hosts) only consumed the last
  headroom.
- **Correction (test-only).** The ten classes use the existing per-test
  `G9U1TestApp.Lifecycle` owner (one annotation each; `G9U1TestApp` and its
  `Lifecycle` became public for the two `locus` classes). Product code,
  assertions, test order and heap are unchanged; the 59 tests of those classes
  pass.
- **Proof.** Three consecutive runs of the `final.desktop` producer invocation
  (with only a pre-`SvgLoadTest` checkpoint): 1 890 tests, 0 failures, 1
  allowed skip each; live heap before `SvgLoadTest` 158, 162 and 158 MB
  (16–17 transient hosts, no accumulation). 512 MB remains sufficient
  (≈ 350 MB headroom); no product memory optimization was required.

## 19. Final state

```text
PRE-G9B-R6-plus-E2 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
REPRESENTATION     = β
COMMANDS           = AlignedDimension / LinearDimension
DQ-E2-6            = ALIGNED NORMATIVE-STYLE TEXT IMPLEMENTED
verificationClass  = INTEGRATED_PHASE
PHASE              = run on the candidate commit; reported outside this file
INTEGRATION        = run on the candidate commit; reported outside this file
AUTHOR_SMOKE       = PENDING
selfApproved = false, authorApproved = false, passClaimed = false
```
