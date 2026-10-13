# PRE-G9B-R6-plus-F3-R2 — Apply Styles author smoke: root-cause report

```text
TASK                 = PRE-G9B-R6-plus-F3-R2-APPLY-STYLES-CORRECTION
CORRECTIVE BASE      = c4dffe60e5721bfeba21b2c59390286b5382d855
                       tree 8c9dd493db2bcd50ae3679c8375277252f1c5424 (F3-R1 candidate; frozen,
                       unchanged)
AUTHOR_SMOKE (T0)    = FAIL — INCOMPLETE T0 PROPERTY TRANSFER (author report)
EVIDENCE CLASS       = runtime evidence on the corrective base (real product window in the
                       test JVM, real author files, both directions, live and leftover
                       selections) and source evidence; no inherited or shared code changed
FINDINGS             = one defect inside P5 (corrected); one discrepancy between the
                       author's expectation and the approved P5 (author decision required)
selfApproved         = false
```

## 1. Author documents (read-only evidence)

The originals in `C:\Temp` were hashed, copied to a disposable scratch directory and
never written, renamed or committed.

| File | Size | Last write | SHA-256 |
|---|---|---|---|
| `C:\Temp\EstilosOrigen.cedg` | 13486 | 2026-10-13 10:58:26 | `9e5f3ca9810f1b0c8f51e00622bb114aae50109dacd015ab9fdaa9168bf4cd1d` |
| `C:\Temp\EstilosDestino.cedg` | 14517 | 2026-10-13 11:00:37 | `1558fd01daa6ddfe1fd80473648336fd3e75eb7cef473d3421d4d4b1fc9c33d7` |

Both archives hold `geogebra.xml`, `geogebra_defaults2d.xml`, `geogebra_defaults3d.xml`,
`geogebra_javascript.js` and `geogebra_thumbnail.png`. Both carry the same document
UUID `1b91e315-80e3-4b1e-952e-1575602f5034` (one was saved from the other; T0 neither
reads nor changes it). Units: version 2 with no length unit. No macros, scripts or
identity records.

The two **construction-default files** (`geogebra_defaults2d.xml`, 9786 bytes, and
`geogebra_defaults3d.xml`) are **byte-identical**, and their point, segment and conic
defaults equal the factory defaults of a new GeoCeDG document:

| Default type | Colour | Thickness | Line type | Point size / style | Fill opacity |
|---|---|---|---|---|---|
| free point (10) | (77,77,255) | 5 | full | 5 / dot | 0 |
| segment (21) | (0,0,0) | 5 | full | — | 0 |
| conic (40) | (0,0,0) | 5 | full | — | 0 |

## 2. Objects and correspondence

Both documents contain the same construction:

| Label | Type | Definition | Dependency | `P5` eligible | Default type |
|---|---|---|---|---|---|
| `A` | point | `(1.6, 2.44)` | free | yes | free point |
| `B` | point | `(3.54, 2.94)` | free | yes | free point |
| `c` | conic (circle) | `Circle(A, B)` | dependent | yes | conic |
| `C` | point | `(1.6, 4.98)` | free | yes | free point |
| `D` | point | `(5.24, 4.84)` | free | yes | free point |
| `f` | segment | `Segment(C, D)` | dependent | yes | segment |

Object styles (serialized explicitly in `geogebra.xml`):

| Object | EstilosOrigen | EstilosDestino |
|---|---|---|
| `A` | (0,100,0), size 5, dot | (0,0,0), size 5, dot |
| `B` | (255,0,255), size 5, **cross** | (0,0,0), size 5, dot |
| `c` | (0,0,255), thickness 2, full | (0,0,0), thickness 5, full |
| `C`, `D` | (77,77,255), size 5, dot | (0,0,0), size 5, dot |
| `f` | (255,0,0), thickness 2, full | (0,0,0), thickness **8**, full |

**The approved T0 correspondence is by default type, not by object.** `P5` (author
design closeout, Q5; `DQ-F3-17` T0) copies the six properties "from the source
document's construction defaults of the object's default type". The implementation
(`GeoCeDGStyleApplication.apply`) reads, for each target object, the source default of
the target object's default type
(`source.getDefaultGeo(target.getDefaultType(geo))`). The source's objects, their
labels and their styles are never read; no label matching exists. Every object above
has exactly one corresponding source default, so the correspondence is unique and
valid under `P5`.

## 3. Diagnostic matrix

"P5 value" is the value `P5` requires (the source construction default). "Actual" was
measured on the corrective base in the real product window with nothing selected in
the open drawing. "Author expectation" is the style of the same-labelled object in
the source document, as described by the smoke.

### Direction A — open EstilosDestino, Apply Styles from EstilosOrigen

| Object | Property | Source object | P5 value (source default) | Target before | Actual (F3-R1) | Author expectation | Classification |
|---|---|---|---|---|---|---|---|
| `f` | colour | (255,0,0) | (0,0,0) | (0,0,0) | (0,0,0), unchanged | red | CONTRACT-CORRECT: the object style is outside `P5` |
| `f` | thickness | 2 | 5 | 8 | **5** | 2 | contract-correct; visible change by coincidence |
| `f` | line style | full | full | full | full | full | — |
| `B` | colour | (255,0,255) | (77,77,255) | (0,0,0) | **(77,77,255)** | magenta | contract-correct; visible change |
| `B` | point style | cross | dot | dot | dot, unchanged | cross | CONTRACT-CORRECT: the object style is outside `P5` |
| `B` | point size | 5 | 5 | 5 | 5 | 5 | — |
| `A` | colour | (0,100,0) | (77,77,255) | (0,0,0) | **(77,77,255)** | green | contract-correct |
| `C`, `D` | colour | (77,77,255) | (77,77,255) | (0,0,0) | **(77,77,255)** | (77,77,255) | coincide |
| `c` | colour | (0,0,255) | (0,0,0) | (0,0,0) | unchanged | blue | CONTRACT-CORRECT |
| `c` | thickness | 2 | 5 | 5 | unchanged | 2 | CONTRACT-CORRECT |
| `c` | line style, fill opacity | full, 0 | full, 0 | full, 0 | unchanged | — | — |

Result: `restyled = 5` (`A`, `B`, `C`, `D`, `f`); one undo step; Undo, Redo and
save/reopen exact. This is exactly the author's Direction A observation table:
segment thickness "transferred" (8 → 5), segment colour not, point colour
"transferred", point shape not, circle "no visible transfer".

### Direction B — open EstilosOrigen, Apply Styles from EstilosDestino

| Object | Property | Source object | P5 value | Target before | Actual, fresh selection state | Actual after a selection in the closed document | Author expectation |
|---|---|---|---|---|---|---|---|
| `A` | colour | (0,0,0) | (77,77,255) | (0,100,0) | (77,77,255) | **unchanged** | black |
| `B` | colour / style | (0,0,0) / dot | (77,77,255) / dot | (255,0,255) / cross | (77,77,255) / dot | **unchanged** | black / dot |
| `c` | colour / thickness | (0,0,0) / 5 | (0,0,0) / 5 | (0,0,255) / 2 | (0,0,0) / 5 | **unchanged** | black / 5 |
| `C`, `D` | colour | (0,0,0) | (77,77,255) | (77,77,255) | unchanged | unchanged | black |
| `f` | colour / thickness | (0,0,0) / 8 | (0,0,0) / 5 | (255,0,0) / 2 | (0,0,0) / 5 | **unchanged** | black / 8 |

With a fresh selection state: `restyled = 4`, as `P5` requires. After the user had
selected objects in the previously open document (here `f`, `B`, `c` of
EstilosDestino) and then opened EstilosOrigen with Open, Open Recent or New, the same
operation reported success with `restyled = 0`, stored no undo step and changed
nothing visible: **MATCH FAILURE (scope)**, the defect of §5 RC-T0-1. This is the
author's "ineffective in one direction".

### Selection variant (`f`, `B`, `c` selected in the open drawing)

Direction A: `B` and `f` restyled, `c` unchanged (equal to the default), `A`, `C`, `D`
unchanged. Direction B: `B`, `c`, `f` restyled. Undo, Redo and reopen exact in both.

## 4. Reproduction on the corrective base

- Real-file probe (product window, disposable copies): §3. Source copies unchanged
  after every run (hashes re-checked); no window left registered.
- Leftover-selection probe: the inherited selection keeps the objects of the
  previously open document after `GuiManagerD.loadFile` (Open), `LoadFileListener`
  (Open Recent) and `fileNew` (New), in GeoCeDG **and in Classic**: they are
  `isSelected()` and `isLabelSet()` but no longer resolve in the kernel
  (`lookupLabel(label) != geo`). Undo and Redo, by contrast, re-resolve the selection
  by name (`recallSelectedGeosNames`).
- Permanent regression `PreG9BR6PlusF3R2StylesTest` run on a `git archive` of
  `c4dffe60` with only the test added: the property matrix (T0-R2-01 to -08, both
  directions, persistence), the mixed selection with Undo/Redo (T0-R2-09, -10) and the
  repaint/repeat test **pass**; the two leftover-selection tests **fail**:
  `f3r2s1… open ==> expected: <6> but was: <1>` (the detached segment was restyled,
  the drawing not) and `f3r2s2… expected: <0> but was: <1>` (a detached object
  restyled, an undo step stored and the drawing marked unsaved with no visible
  change).

## 5. Root causes

**RC-T0-1 — Leftover selection taken as the scope (defect inside `P5`; corrected).**
`P5`: "to the selected objects, or to every object when nothing is selected".
`GeoCeDGStyleApplication.apply` took the scope from
`getSelectionManager().getSelectedGeos()` without checking that the objects belong to
the open drawing. The inherited Open, Open Recent and New routes do not clear that
list, so after selecting objects in one document and opening another, the operation
restyled objects of the closed document (invisible, outside the drawing) instead of
the open drawing, reported success, and either did nothing visible or stored an undo
step and marked the document unsaved without any visible change. Source:
`GeoCeDGStyleApplication.java` (scope built from the raw selection, corrective base
lines 96–99). Classification: **MATCH FAILURE (target scope)**. It explains the
direction dependence: the effect depends only on the order in which the author
selected and opened documents.

**RC-T0-2 — The author expects per-object transfer; `P5` transfers construction
defaults (discrepancy; author decision required).** The approved contract reads the
source's construction defaults of each object's default type. The two author
documents have identical construction defaults equal to the factory defaults; the
author's red segment, magenta cross point, green point and blue circle are styles of
**objects**, which `P5` never reads. Hence, with these files, Apply Styles gives the
same result in both directions (every eligible object takes the factory default
style of its type), and the partial pattern of the smoke is exactly the set of
properties where a target object differed from the factory default. No property was
silently dropped: every applicable property was compared and written. Classification
per property: **CONTRACT-CORRECT** (the source object styles are outside `P5`).
Meeting the expectation (segment red, point cross, circle blue and thin) requires a
per-object correspondence (for example by label and type), which is a redesign of T0
and a change of `P5`, both outside this authorization (stop condition FR-F3R2-6).

**RC-T0-3 — Misleading help text (corrected).** The in-product short help said "Apply
the object styles of a GeoCeDG document" / "Aplica los estilos de objeto de un
documento GeoCeDG", which invites the per-object reading of RC-T0-2. The guide's "from
the default styles of a document" was accurate but did not say that existing objects
are not read.

### Questions of the investigation

| Question | Answer |
|---|---|
| 1 matching | by default type (`P5`); unique and valid for every object; the source objects are not matched by design |
| 2 effective source read | correct: the helper's loaded defaults equal `geogebra_defaults2d.xml` (and the factory defaults) |
| 3 omitted/default XML values | not involved: all six properties are explicit in both files; a default value (black, dot, 5) is transferred like any other (Direction B) |
| 4 applicability by actual type | correct: points get point size and style; segments and circles line properties; fill opacity copied for all |
| 5 colour semantics | correct (`setObjColor` with the default's colour) |
| 6 point style API | correct (`GeoPoint.setPointStyle`); cross → dot transferred in Direction B |
| 7 circle excluded? | no: `c` is eligible; unchanged in Direction A only because it already equals the default |
| 8 line properties filtered? | no |
| 9 change detection | exact value comparison; equal values (black = black) are correctly skipped, unequal ones written |
| 10 render and persistence | yes: pixel test, save/reopen and Undo/Redo XML equality |
| 11 source loading defaults | the source's own defaults are used, never the target's |
| 12 direction | same code both ways; the visible difference comes from the targets' distance to the identical defaults and, separately, from RC-T0-1 |
| 13 selection scope | yes: RC-T0-1 |
| 14 earlier operation / host | yes: a selection made before Open, Open Recent or New (RC-T0-1); helper hosts are released (no window left) |

No READ, APPLICABILITY, CHANGE-DETECTION, WRITE, RENDER or PERSISTENCE failure was
found.

## 6. Correction rationale

RC-T0-1 is corrected in GeoCeDG code only: the scope keeps a selected object only
while the drawing's kernel resolves its label to that same object; when none remains,
every object is restyled. The inherited selection itself is not changed (clearing it
on Open would modify inherited behaviour for every consumer). RC-T0-3 is corrected by
clarifying the help text and the guide. RC-T0-2 is not changed: `P5` stays as approved.

## 7. Residual risks

- Other consumers of the inherited selection after Open, Open Recent or New (Delete,
  copy, properties, style bar) were not assessed; T0 no longer depends on it
  (`OBS-F3R2-INHERITED-STALE-SELECTION`).
- A runtime exception in the middle of the restyle loop would be reported as "nothing
  changed" while earlier objects were already restyled without an undo step; never
  observed, pre-existing, outside this correction (`OBS-F3R2-PARTIAL-RESTYLE`).
- An application that changes nothing gives no message (approved no-op behaviour;
  `OBS-F3R2-SILENT-NO-OP`).
