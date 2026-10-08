# GeoCeDG unit system

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR APPROVED** (decision of 2026-10-02 on the exact documentary candidate `T_R6PLUS_D0` `8383a153dc2228fc08b4e00a0c92ef8025608916`, tree `442e27fcafea223af426b4ee65102a8a5d90be8c`; [D0 closeout record](../../../docs/validation/pre_g9b_r6_plus_d0_closeout_record.md)); implementation not authorized |
| Version | `1.0` (author approved); amendment `1.1` (§20, dimension unit suffix policy) **AUTHOR APPROVED** (decision of 2026-10-08 on the exact candidate `T_R6PLUS_E2` revised `3a7246614a6ef040d84a1440c4c10b3f93be5776`, tree `00b5230d19debea5598299906f846f52b4d6e9d7`; [E2 closeout record](../../../docs/validation/pre_g9b_r6_plus_e2_closeout_record.md)) |
| Owner phase | `PRE-G9B-R6-plus-D0` (normative design; no implementation) |
| Implementing phases | `D1` (document state, persistence, undo, defaults, paste notice, status), `C` (export), `E2` (native dimensions), `E3` (`IsoABorder`) |
| Decision | [ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md) (`ACCEPTED — AUTHOR APPROVED`) |
| Governing author decisions | [mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md) §4; [D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md); [D0 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md) |
| Serialization effect of this document | none; it specifies the contract `D1` implements |

## 0. Status and normative language

This specification was accepted by the author on 2026-10-02 and is normative;
the clauses below are unchanged from the approved candidate, and acceptance
changed only the status markers. Acceptance authorizes no implementation: no
product behavior may be claimed against it until the implementing phases
deliver it, and the in-force export rules of §15.1 stay unchanged until `C`.
Status marker (2026-10-04): `C` delivers §13 and §15 in a technical candidate
pending author review (§15).
Amendment marker (2026-10-08): the author decision of the `E2` author smoke
follow-up ([record](../../../docs/validation/pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md))
authorizes a bounded versioned extension of the unit state, specified in §20 as
amendment `1.1`. Status marker (2026-10-08): the author approved amendment `1.1`
with `E2` on the exact candidate `T_R6PLUS_E2` revised `3a7246614a6ef040d84a1440c4c10b3f93be5776`, tree `00b5230d19debea5598299906f846f52b4d6e9d7`
([E2 closeout record](../../../docs/validation/pre_g9b_r6_plus_e2_closeout_record.md)); §20 is normative and its text
is unchanged from that candidate. The clauses §1–§19 keep their approved
`1.0` text unchanged as the historical evidence of the 2026-10-02 decision;
each clause that §20 amends carries an amendment pointer, and §20 prevails over
the clause it amends.

`MUST`, `MUST NOT`, `SHOULD` and `MAY` have their usual normative meaning. Each
numbered clause is cited as `§n.m`. The author decisions listed in the header
govern; where this specification and an author decision differ, the decision
prevails and this specification is defective. The rules here are stated once;
the ADR, the export pointer, the candidate report, the roadmap and the plan
reference them and do not restate them.

## 1. Scope

In scope for version 1:

- the physical meaning of one numerical model unit of a document
  (`constructionUnit`) and the unit used to express dimensional presentation
  quantities (`presentationUnit`);
- one document-scoped user-defined physical unit (`usm`);
- the dimensionless engineering drawing scale (`drawingScale`);
- persistence, undo, defaults for new documents, copy and paste, forward
  compatibility;
- the relation to dimensional presentation and to physical export.

Out of scope for version 1: areas, volumes and other powers of the length
unit; angles (they keep their own `angleUnit` contract); presentation of any
existing numeric result (`Distance`, `Length`, `LocusLength`, general
`GeoNumeric`) with physical units; a registry of several custom units; unit
conversion of geometry; STL, Collada and the 3D AR unit, which keep their own
output units and are outside the export surface; the axis `unitLabel`
(§16.3).

## 2. Terms

| Term | Meaning |
|---|---|
| model unit | one unit of the numerical coordinates and lengths stored and computed by the construction |
| `constructionUnit` | the physical length that one model unit represents, or `UNSPECIFIED_MODEL_UNIT` |
| `presentationUnit` | the physical unit in which dimensional presentation quantities are expressed |
| physical unit | one of `mm`, `cm`, `m`, `usm` |
| metre factor `f(u)` | the length of one `u` in metres |
| effective units | the result of the effective-unit function (§5) |
| physical meaning | the effective construction metre factor; two documents have the same physical meaning when both are physical and their factors are equal (§5.4) |
| physical operation | any operation that converts model units into a physical magnitude: engineering-scale export, `IsoABorder`, creation-time capture of a physical quantity, dimensional presentation with a suffix |
| live consumer | code that reads the unit state each time it produces output (§6.2) |
| creation-time capture | a single read of the unit state or `drawingScale` by a tool when it creates objects (§6.3) |

## 3. Units

### 3.1 Tokens

The physical-unit tokens are exactly, case-sensitively:

```text
unit-token = "mm" / "cm" / "m" / "usm"
```

`UNSPECIFIED_MODEL_UNIT` is a state, not a token. It is never serialized as a
token; it is represented by the absence of a construction-unit selection
(§8).

### 3.2 Built-in units

The built-in units have exact SI definitions:

```text
f(mm) = 10^-3 m        f(cm) = 10^-2 m        f(m) = 1 m
```

Computation uses binary64. The binary64 value used for a built-in factor is
the binary64 nearest to its exact SI value (round to nearest, ties to even),
written `fb(mm)`, `fb(cm)`, `fb(m)`. The SI definition stays exact; the
binary64 value is only its computational representation.

### 3.3 `UNSPECIFIED_MODEL_UNIT`

`UNSPECIFIED_MODEL_UNIT` is the only state in which no physical model-unit
interpretation exists. It is the state of every legacy document without unit
metadata and of every new document unless a default declares a physical unit
(§10). In that state there is no effective presentation unit, no physical
operation is available, and dimensions show the bare model value without a
suffix. `cm`, or any other unit, is never inferred from `printingScale`, zoom,
DPI, the viewport, LaTeX defaults or the historical behavior of an exporter.

## 4. The user-defined unit `usm`

### 4.1 Definition and identity

A document has at most one `usm` definition in version 1. A definition is

```text
1 usm = k m      k a finite binary64, k > 0
```

Its semantic identity is the durable token `usm` plus its declared
`metersPerUnit` value `k`. Name and symbol (§4.4) are presentation metadata,
not identity.

`k` is a binary64 value. It is never described as symbolically exact beyond
that stored binary64 value, even when it was entered as a short decimal.

### 4.2 Validity

A factor is valid when it is a binary64 that is finite and greater than zero.
Zero, negative values, NaN, infinities, and values that are not parseable
under §8.4 are invalid. An attempt to define or change the factor with an
invalid value is rejected, and the previous state, including a previous valid
definition, stays unchanged.

### 4.3 Lifecycle

```text
usm state := NONE | DEFINED(k, name?, symbol?)
```

| Operation | Precondition | Effect | Undo |
|---|---|---|---|
| define | `NONE`; valid `k` | `DEFINED(k, name?, symbol?)` | one undo point |
| change factor | `DEFINED`; valid `k'` ≠ `k` | `k := k'`; when `usm` is the effective construction unit this is a physical reinterpretation (§7.2) | one undo point |
| rename / set or clear symbol | `DEFINED` | name or symbol changed; physical semantics unchanged | one undo point |
| remove | `DEFINED`; neither the construction nor the explicit presentation unit is `usm` | `NONE` | one undo point |
| New, Open | — | the new or opened document's own state | none (§7.4) |

- `usm` can be selected as construction or presentation unit only while it is
  `DEFINED`. An undefined `usm` cannot be selected and never falls back to an
  inferred value or to `UNSPECIFIED_MODEL_UNIT`.
- A `DEFINED` `usm` persists while the unit element exists (§8.6), including
  while neither unit selects it, so the user can switch away from it and back
  without losing its definition.
- A user interface MAY combine *define* with the selection of `usm` as a unit
  in one operation; it is then one undo point.
- *remove* is permitted, not required: `D1` MAY omit it from the v1 user
  interface.
- `usm` is never a user-preference default (§10).

### 4.4 Name and symbol

- `name`: optional human-readable name. `symbol`: optional presentation
  symbol.
- Each, when present, is a string of 1 to 64 Unicode scalar values, with no
  control character (general category `Cc`) and no leading or trailing white
  space.
- The dimension suffix (§14) is the symbol; without a symbol it is the
  literal `usm`. The name is used only for display in unit choosers.
- Neither takes part in physical-meaning comparison, paste provenance or
  export semantics.

## 5. Effective units

### 5.1 Stored state

```text
UnitState := { c : unit-token?          explicit construction-unit selection
               p : unit-token?          explicit presentation-unit selection
               usm : NONE | DEFINED(k, name?, symbol?) }

EMPTY = { c absent, p absent, usm NONE }
```

State constraints, which every operation preserves and every reader checks:

1. `p` present ⇒ `c` present (`DQ-D0-2`);
2. `c = usm` or `p = usm` ⇒ `usm` is `DEFINED`.

### 5.2 Effective-unit function

One pure function of the stored state, used by every consumer; no consumer
derives a unit on its own:

```text
effC(S) = S.c                if S.c present
        = UNSPECIFIED_MODEL_UNIT otherwise

effP(S) = NONE               if effC(S) = UNSPECIFIED_MODEL_UNIT
        = S.p                if S.p present
        = effC(S)            otherwise

fb(usm) = k                  of S.usm
```

### 5.3 Conversions

For a model length `L` (binary64, model units), with `effC` and `effP`
physical:

```text
display(L)  = L · fb(effC) / fb(effP)           quantity expressed in effP
physical(L) = L · fb(effC)                      metres
```

With `effC = UNSPECIFIED_MODEL_UNIT`, neither exists. A result that is not
finite (overflow) is a presentation failure state (§14.3), never an exception,
a silent infinity or a change of `L`.

### 5.4 Physical-meaning equality

Two effective construction units `u1`, `u2` have the same physical meaning
exactly when both are physical and `fb(u1) = fb(u2)` as binary64 values, with
`fb(usm)` the stored `k`. Hence two `usm` definitions are equal when their
binary64 factors are equal; a `usm` with `k = fb(mm)` has the same physical
meaning as `mm`; no tolerance is applied. `UNSPECIFIED_MODEL_UNIT` has no
physical meaning and is equal to nothing, including itself.

## 6. Unit-independence invariant

### 6.1 Protected properties

`constructionUnit`, `presentationUnit`, the `usm` definition and
`drawingScale` **MUST NOT** alter live:

```text
coordinates; authoritative numeric geometry; DAG topology; dependencies;
semantic identities; construction order; geometric object identity;
LocusV2 branch/component identity; LocusV2 semantics; SplineV2 semantics;
parameter domains; numeric geometric outputs
```

No algorithm, command or construction element may register a dependency on
the unit state, read it during `compute()`, or recompute because it changed.
Changing `constructionUnit` is a physical reinterpretation, never geometric
scaling, never a coordinate transformation and never a redefine.

### 6.2 Permitted live consumers

Only these read the unit state live:

1. the presentation strings of native dimensions (`E2`, §14);
2. status and presentation UI (for example the `D1` status segments and unit
   choosers);
3. export adapters (`C`, §15).

A presentation string MAY be held in a kernel `GeoText`. A downstream object
or script that explicitly consumes that text MAY observe a changed string
after a unit change. That is a presentation dependency, not a geometric-unit
dependency. No code may parse a formatted dimension string back into geometry
or numeric semantics automatically. The invariant of §6.1 applies to
authoritative numeric geometry, not to explicitly unit-sensitive presentation
strings (`DQ-D0-9`).

### 6.3 Creation-time capture

A tool MAY read the effective units or `drawingScale` **once, when it creates
objects**, to compute an explicit input in model units (for example an `E2`
overshoot or gap, `AQ-E4`, or the `E3` model-units-per-millimetre factor,
§18.4). From then on that input is an ordinary construction parameter: it is
visible, editable and serialized like any other, and it does not follow later
unit or scale changes. A later unit change is therefore a reinterpretation,
never a recompute (`AQ-E3c` is `E3`'s decision on its consequence).

### 6.4 Model-unit notions that stay model-unit

The following existing notions are model-unit or opaque and are not governed
by the unit state in version 1:

| Notion | Location at the base |
|---|---|
| `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT` (Locus V2 metrics) | `geocedg-common/kernel/locus/metric/MetricUnit2D.java:9-10` |
| intersection residual unit `"model-coordinate"` | `geocedg-common/kernel/locus/intersection/LocusIntersectionPolicy2D.java:79`, `:129` |
| export model coordinate unit: `UNITLESS` until `C`; since the `C` technical candidate the effective construction unit with `UNITLESS` for `UNSPECIFIED_MODEL_UNIT` (§15.2) | `geocedg-common/export/GeometryExportModel.java` (`Unit`) |
| spatial frame `units` token: free text, string-equality compared | `geocedg-common/kernel/spatial/identity/ProjectionFrameRecord.java:81`, `:138-140` |

The spatial frame `units` token is an opaque identity token unrelated to
`constructionUnit`; no rule derives one from the other.

Path abbreviations: `common/` =
`source/shared/common/src/main/java/org/geogebra/common/`, `geocedg-common/` =
`source/shared/common/src/main/java/org/geocedg/common/`, `desktop/` =
`source/desktop/desktop/src/main/java/org/geogebra/desktop/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`.

## 7. Operations and undo

### 7.1 Document operations

| Operation | Precondition | Effect on `S` | Undo | Marks document modified |
|---|---|---|---|---|
| set construction unit to a physical `u` | `u ≠ usm` or `usm` `DEFINED` | `c := u` | one undo point | yes |
| set construction unit to `UNSPECIFIED_MODEL_UNIT` | `c` present | `c` and `p` removed; `usm` kept | one undo point | yes |
| set presentation unit to `u` | `c` present; `u ≠ usm` or `usm` `DEFINED` | `p := u` | one undo point | yes |
| clear explicit presentation unit | `p` present | `p` removed (presentation follows construction) | one undo point | yes |
| `usm` operations | §4.3 | §4.3 | one undo point each | yes |

- An operation whose result equals the current state creates no undo point.
- A rejected operation (invalid factor, `usm` not defined, presentation while
  the construction unit is unspecified) changes nothing and creates no undo
  point.
- A combined user action (for example define `usm` and select it) is one undo
  point.
- Setting the construction unit to `UNSPECIFIED_MODEL_UNIT` removes the
  physical interpretation and every effective presentation interpretation
  that depends on it; it does not necessarily delete the unit element, because
  a `DEFINED` `usm` stays (`DQ-D0-3`).

### 7.2 Reinterpretation

Changing the effective construction unit or its metre factor — between
physical units, to or from `UNSPECIFIED_MODEL_UNIT`, or by changing `k` while
`usm` is the effective construction unit — is a physical reinterpretation:
coordinates, numeric geometry, dependencies, order and identities stay
unchanged (§6.1); dependent dimensional presentation, status UI and the export
interpretation update. Changing only `presentationUnit` changes only how
quantities are expressed; it changes neither the physical interpretation nor
the physical output size (`AQ-U1`, §13).

### 7.3 Undo and redo

- Every document operation of §7.1 stores one undo point through the ordinary
  construction-XML snapshot, which contains the unit element (§8.1, §9).
- Undo and redo restore the unit state coherently with the restored
  construction; dimensional presentation follows. They never change object
  identity or any geometric value.
- `drawingScale`, `ExportArea` and clipboard provenance are session state;
  undo, redo, redefine rebuild and rollback restore leave them unchanged.
- Changing a user preference (§10) creates no document undo point.

### 7.4 New and Open

New and Open replace the unit state with the state of the new or opened
document (§9). They never carry a unit state from the previous document.

## 8. Persistence

### 8.1 Owner and placement

The unit state is persisted in one flat, versioned, self-closing GeoCeDG
element `geocedgUnits`, a child of the document's `<construction>`, written by
`Construction.getConstructionXML` (`common/kernel/Construction.java:1517-1550`)
immediately after `<worksheetText>` (when present) and before
`<geocedgSpatial>` (when present) and before the first construction element.
It is therefore part of the full document XML (`common/io/MyXMLio.java:316`)
and of every undo snapshot (`MyXMLio.getUndoXML`, `:155`). It is never written
for a macro construction (`common/kernel/Macro.java:175`, `:792-796`), never in
the preferences XML, and never in clipboard XML. It is never placed inside
`<geocedgSpatial>`.

### 8.2 Grammar

```text
units-element  = "<geocedgUnits" SP version-attr
                 [ SP construction-attr [ SP presentation-attr ] ]
                 [ SP factor-attr [ SP name-attr ] [ SP symbol-attr ] ]
                 "/>"

version-attr      = 'version="1"'
construction-attr = 'construction="' unit-token '"'
presentation-attr = 'presentation="' unit-token '"'
factor-attr       = 'usmMetersPerUnit="' factor-canonical '"'
name-attr         = 'usmName="' xml-escaped-text '"'
symbol-attr       = 'usmSymbol="' xml-escaped-text '"'
```

- The writer emits the attributes in exactly that order, separated by one
  space, with the XML escaping of the existing attribute writer, and emits no
  child and no text. The element is written on its own line in the same
  indentation style as its neighbours.
- `construction` is written exactly when `S.c` is present; `presentation`
  exactly when `S.p` is present (which implies `construction`).
- `usmMetersPerUnit`, `usmName` and `usmSymbol` are written exactly when
  `usm` is `DEFINED` and, for name and symbol, present.

Examples (illustrative, not fixtures):

```text
<geocedgUnits version="1" construction="mm"/>
<geocedgUnits version="1" construction="mm" presentation="cm"/>
<geocedgUnits version="1" construction="usm" presentation="cm" usmMetersPerUnit="0.0254" usmName="inch" usmSymbol="in"/>
<geocedgUnits version="1" usmMetersPerUnit="0.3048" usmSymbol="ft"/>
```

The last line is a document whose construction unit was set back to
`UNSPECIFIED_MODEL_UNIT` while a `usm` definition is kept (`DQ-D0-3`,
`DQ-D0-4`).

Amendment `1.1`: §20.3 adds version 2, which is this grammar plus the
`dimensionUnitSuffix` attribute.

### 8.3 Omission rule and legacy byte identity

The element is written exactly when `S ≠ EMPTY`, that is when `S.c` is present
or `usm` is `DEFINED`. When `S = EMPTY` nothing is written, so the
construction XML of a document that never acquired unit metadata is
byte-identical to the base build. The absence of a construction-unit
selection means `UNSPECIFIED_MODEL_UNIT`; it does not imply that the element
is absent.

Amendment `1.1`: `EMPTY` additionally requires the shown suffix policy, so a
document with the hidden policy writes the element (§20.1, §20.3); a document
that never acquired unit metadata or the hidden policy keeps this byte
identity.

### 8.4 Factor lexical form

- **Writer (canonical form).** `factor-canonical` is the string specified for
  `java.lang.Double.toString(double)` from Java SE 19 onward: the shortest
  decimal that rounds to `k`, the closest to `k` among those, in plain
  notation for `10^-3 ≤ k < 10^7` (for example `0.0254`, `1.0`, `1609.344`) and
  in computerized scientific notation otherwise (for example `2.54E-5`,
  `1.0E7`), with `.` as the decimal separator, ASCII digits and no locale
  dependence. An implementation that runs on a runtime whose
  `Double.toString` does not meet that specification MUST NOT rely on it and
  MUST produce the same strings by other means; `D1` proves this with fixed
  reference values.
- **Reader.** It accepts

  ```text
  factor-lexical = 1*DIGIT [ "." 1*DIGIT ] [ ( "E" / "e" ) [ "+" / "-" ] 1*DIGIT ]
  ```

  (ASCII only; no sign, no white space, no hexadecimal, no `NaN` or
  `Infinity`), converts the exact decimal value to binary64 by round to
  nearest, ties to even, and requires a finite result greater than zero
  (§4.2); otherwise the element is malformed (§8.7).
- **Round trip.** Writing then reading yields the same binary64; reading a
  canonical string then writing yields the same string. A valid non-canonical
  string (for example `0.02540`) is read to the same binary64 and written
  canonically on the next save; the value never changes.

### 8.5 Reset and application

- `Construction.clearConstruction` (`Construction.java:4094-4143`) resets the
  unit state to `EMPTY`, for every load purpose.
- The unit state is changed by parsing only in a **clearing load** of the
  document construction: a full document load, an undo or redo restore, a
  redefine rebuild, a rollback restore (§9). There `clearConstruction` has
  already reset the state, so an absent element yields `EMPTY`.
- The unit state is never reset because an element is absent from a parse;
  in a non-clearing parse an absent element means nothing.

### 8.6 Persistence of `usm`

A `DEFINED` `usm` is written whenever the element exists, whether or not a
unit selects it. It ends only through *remove* (§4.3) or through New or Open of
another document.

### 8.7 Reading: fail closed

A GeoCeDG build that implements this specification (every build from `D1`
onward) recognizes the `geocedgUnits` family and **fails closed**: the
document is not opened, and an explicit user-visible load error names the
defect, when the document `<construction>` contains:

1. a `geocedgUnits` element whose `version` is an integer greater than the
   highest version the build supports;
2. a recognized version-1 element that is structurally malformed: a missing
   or non-integer `version`; an attribute outside §8.2; a token outside §3.1;
   an invalid factor (§8.4); a name or symbol outside §4.4; a name or symbol
   without a factor; a child element or non-white-space text; an attribute
   that violates a state constraint of §5.1;
3. a selected `usm` (`construction` or `presentation`) without a valid factor;
4. more than one `geocedgUnits` element, or any other case in which the
   physical semantics cannot be established unambiguously.

The document is never opened with its unit metadata silently ignored. The
failure is an open failure as a whole: no partially loaded document is
presented as the opened file, the file on disk is not modified, and the
application state after the failure follows the existing rejected-parse
restore precedent of `common/io/MyXMLio.java:425-447`, which `D1`
characterizes and extends. The element is accepted at any child position of
the document `<construction>`; its position carries no meaning for a reader.

Amendment `1.1`: the highest supported version becomes 2, and §20.3 states the
version-2 malformations.

### 8.8 Ignored contexts

- In a **macro** construction (a `MacroKernel` parse, `common/io/MyXMLHandler.java:2575` precedent) the element is never written and, if present, is
  ignored without being applied or validated, with a log diagnostic.
- In a **non-clearing** parse (paste, scripting or API XML insertion,
  `evalXML`) the element is never applied and never changes the target state;
  if present, it is ignored with a log diagnostic. Paste never resets the
  target unit.

### 8.9 Older readers (forward-compatibility limitation)

GeoCeDG builds before `D1` and Classic GeoGebra do not recognize the element.
At the base they log `unknown tag in <construction>` and continue in
construction mode (`common/io/MyXMLHandler.java:3088-3094`), load the rest of
the document, and do not write the element on re-save. A document saved by
such a build therefore **loses its unit metadata silently**. This is an
accepted, explicit limitation of version 1 (`AQ-U3`, `DQ-D0-7`). The element
is flat so that such readers see no misplaced child content. Unit metadata is
never moved into `<geocedgSpatial>` to make old readers fail closed.

Amendment `1.1`: builds that implement only version 1 fail closed on a
version-2 element (§20.3).

## 9. Construction-XML routes

| Route | Base evidence | Unit element |
|---|---|---|
| save (`.cedg`) / full document XML | `MyXMLio.java:307-316` | written per §8.3 |
| full document load (Open, startup with a file, `setXML` API) | `MyXMLio.java:415-417`; `common/plugin/GgbAPI.java:1474-1477` | clearing load: reset, then read and validated (§8.7) |
| undo / redo restore | Desktop `UndoManagerD.loadUndoInfo` → `doParseXML(…, true, …)`; common `Construction.processXML` `:4230-4241`; snapshot `MyXMLio.getUndoXML` `:139-168` | written in the snapshot; clearing load restores it |
| redefine rebuild | `Construction.java:2099`, `:4177-4184` | written in `getCurrentUndoXML`; clearing load restores it |
| rollback restore (redefine rollback, paste rollback, rejected spatial parse) | `Construction.java:2673-2695`; `common/util/InternalClipboard.java:660-669` (snapshot from `app.getXML()`, `:623`); `MyXMLio.java:504-516` | in the rollback XML; clearing load restores it |
| File → New, new window, startup without a file | `desktop/main/AppD.java:1149-1177` (`clearConstruction` `:1154`, preferences XML `:1174`) | reset by `clearConstruction`; then §10 defaults |
| paste (internal clipboard) | wrapped in a bare `<construction>`, non-clearing (`InternalClipboard.java:626-640`) | never in clipboard XML; ignored if present (§8.8); provenance per §11 |
| scripting / API XML insertion (`evalXML`) | non-clearing (`GgbAPI.java:169-179`; `common/main/App.java:5126`) | ignored (§8.8) |
| macro construction write and read, user tools (`.ggt`), `addMacroXML` | `Macro.java:175`, `:792-796`; `App.java:3664-3670` | never written; ignored (§8.8) |
| preferences XML | `MyXMLio.java:346-358` (no construction) | never written |
| DXF staleness fingerprint | `geocedg-common/export/G9X1GeometryExportAdapter.java` (construction fingerprint and export-context guard) | not part of it at the base; the `C` technical candidate adds the unit state to the currentness check (§15.3) |

`D1` re-establishes every row at its own base and adds any further route it
finds; a route that cannot keep these rules is a `D1` stop condition.

## 10. Defaults for new documents

- User preferences MAY define a default construction unit and a default
  presentation unit for **new blank documents**:
  - construction-unit default ∈ { `UNSPECIFIED_MODEL_UNIT`, `mm`, `cm`, `m` };
  - presentation-unit default ∈ { none, `mm`, `cm`, `m` }.

  `usm` is never a default, because its definition is document-scoped. An
  unset preference means `UNSPECIFIED_MODEL_UNIT` and none.
- The defaults apply only when a new blank document is created (File → New,
  a new window, application start without a file), after `clearConstruction`
  and after the preferences XML has been re-applied. They are **never**
  applied to an opened document, nor on undo, redo, redefine rebuild,
  rollback, paste or insertion.
- Application: with a physical construction default `u`, `S := { c = u,
  p = presentation default if any, usm NONE }`. With an unspecified
  construction default, `S := EMPTY`; any presentation default stays inert and
  creates no physical document semantics (`DQ-D0-1`, `DQ-D0-2`).
- Applying defaults creates no undo point and does not mark the document
  modified; a physical state so created is a declaration and is written on
  save like any other.
- Changing a preference affects only documents created afterwards; it is not
  a document operation and has no document undo.
- The preference keys follow the `geocedg.<area>.<name>.v1` precedent
  (`geocedg-desktop/GeoCeDGPresentationPreferences.java:23-29`); `D1` fixes
  their names. They live in the GeoCeDG properties store, never in the
  preferences XML or in a document.
- Amendment `1.1`: a third default, the dimension unit suffix policy of new
  blank documents, follows the same rules (§20.5).

## 11. Copy and paste

- Copy and paste move numeric geometry, not physical lengths. Paste never
  converts coordinates, never scales geometry and never changes either
  document's unit state (`AQ-U4`).
- **Provenance.** At copy (and cut) time the source's effective construction
  unit and metre factor, or `UNSPECIFIED_MODEL_UNIT`, are recorded as
  transient provenance beside the internal copy buffer. Provenance is never
  geometry, construction XML, serialized clipboard content or document state.
  It is replaced with the buffer and cleared with it (`desktop/util/CopyPasteD.java:607-616`).
- **Notice.** After a paste from that buffer, when provenance is available,
  both the source and the target are physical and their physical meanings
  differ (§5.4), a non-blocking notice states both units. It requires no
  decision, is not part of the geometry, and creates no undo point; undoing
  the paste undoes only the paste.
- When provenance is unavailable (external text, scripting or API insertion,
  a buffer from another window or an earlier build), or either side is
  `UNSPECIFIED_MODEL_UNIT`, the paste proceeds unchanged, with no notice and
  no inference.
- Paste semantics never depend on provenance.

## 12. `drawingScale`

```text
drawingScale = a:b        a, b positive integers
meaning      = physical output length / physical model length = a / b
               1:1 full size     1:2 half size     2:1 double size
normal form  = (a/g):(b/g)  with g = gcd(a, b)
default      = 1:1
```

- `SESSION`: shared by the applicable export context of the current
  application/document window; reset to `1:1` on New and Open; never
  serialized; never part of undo; unchanged by undo, redo, redefine rebuild
  and rollback.
- It is export and presentation state, never geometric semantics (§6.1).
- It has a physical meaning only with a physical construction unit. With
  `UNSPECIFIED_MODEL_UNIT` it is not offered as an engineering scale.
- `C` and `E3` MAY bound the integers a user can enter; the semantics is
  defined for every positive integer pair. Its notation is `a:b` in normal
  form.

## 13. Physical output

With a physical effective construction unit `c` and `drawingScale` `a:b`, a
model length `L` has the output length

```text
output(L) = L · fb(c) · a / b        metres on the output medium
```

independent of `presentationUnit`. `presentationUnit` only expresses output
and dimensional quantities: a quantity `q` metres is expressed as
`q / fb(effP)` in `effP`. It is never a second scale factor.

```text
c = cm, L = 10, 1:2   ->  output = 0.05 m = 5 cm = 50 mm
c = mm, L = 100, 1:2  ->  output = 0.05 m = 50 mm
changing only presentationUnit changes none of these lengths      (AQ-U1)
```

**Supersession.** This clause supersedes the living-plan wording
`physical model-to-output unit = presentationUnit` (mini-track plan §4.4) and
the `P0` input statement that `presentationUnit` expresses "dimensional and
export quantities" in a way that would affect output size
([D0 unit-system inputs](../../../docs/architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md)
§1). The physical relation derives from `constructionUnit + drawingScale`.

**Unspecified unit.** With `UNSPECIFIED_MODEL_UNIT` no output is an
engineering-scale output and none may be labelled or interpreted as `1:n`
(`DQ-D0-11`). Picture and vector export MAY keep a clearly non-physical
legacy/device-scale mode where existing functionality requires it. There is
no `1 model unit = 1 cm` fallback. `printingScale`, zoom, DPI and the viewport
are never a model-unit authority.

## 14. Dimensional presentation

### 14.1 Scope

In version 1 only the native dimension objects delivered by `E2` present
physical units. `Distance`, `Length`, `LocusLength` and every other existing
numeric result stay bare model values without a suffix; no general
`GeoNumeric` becomes a dimensional quantity (`AQ-U2`).

### 14.2 Number and text

A native dimension keeps a stable numeric output equal to the model-unit
measure, which never depends on the unit state. Its presentation text is

```text
effC physical : format(display(L)) + " " + suffix(effP)
effC unspecified : format(L)                             (no suffix)

suffix(mm) = "mm"   suffix(cm) = "cm"   suffix(m) = "m"
suffix(usm) = usmSymbol, or "usm" without a symbol
```

`format` is the kernel's ordinary number formatting with the current rounding
settings; the unit system adds no rounding rule, no input parsing and none of
the other effects of `angleUnit`. The text updates when the effective units
or `k` change, without recomputing any geometry.

Amendment `1.1`: the suffix is subject to the document's dimension unit
suffix policy (§20.2); the text above is the shown policy.

### 14.3 Failure state

When `display(L)` is not finite, the text shows a defined presentation failure
marker chosen by `E2`; the numeric output is unchanged.

## 15. Export amendment contract (owner `C`)

Implementation status: `PRE-G9B-R6-plus-C` implements §13 and §15.2 and
amends the export specifications, the verifier pins and the sidecar of §15.3
in one technical candidate pending author review (2026-10-04). This status
marker changes no clause of this section.

### 15.1 Rules in force before `C`

Until `C` implemented this section, the following rules were in force; they
remain the historical baseline of the G5 and G9X1 evidence:

- [`geometry-export-foundation.md`](../export/geometry-export-foundation.md)
  (`Status: Experimental`, under ADR 0005 accepted for the G5 experimental
  implementation): source unit `UNITLESS`, identity transform, `$INSUNITS = 0`,
  and the G5 PASS clause `$INSUNITS = 0`. This specification does not promote
  it.
- [`dxf-curve-fidelity-and-approximation.md`](../export/dxf-curve-fidelity-and-approximation.md)
  (`NORMATIVE / AUTHOR APPROVED`): unitless coordinates, `$INSUNITS=0`,
  unitless dialog, sidecar units; the G9X1 verifier evidence check
  `fidelityContract.units == "UNITLESS"`
  (`tools/agent/verify-g9x1-extended-dxf.ps1:449`). `D0` does not edit them.
- Picture export after `B` sizes physical output through `printingScale`
  (`geocedg-desktop/export/PictureExportService.java:180-185`,
  `geocedg-desktop/export/ExportViewport.java:120-127`). `D0` does not alter
  it.

### 15.2 Future rules

| Area | Future rule |
|---|---|
| coordinates | every exporter writes model coordinates numerically unchanged; no unit conversion of geometry |
| DXF `$INSUNITS` | from the effective construction unit: `mm` → `4`, `cm` → `5`, `m` → `6` (the AutoCAD DXF header codes; `C` verifies them against its DXF reader); `UNSPECIFIED_MODEL_UNIT` → `0` |
| DXF with `usm` | `$INSUNITS = 0`; an explicit custom-unit warning; the paired GeoCeDG sidecar is **required** for that export and records at least the token `usm` and the canonical `metersPerUnit` (§8.4); the core DXF alone stays unitless and the sidecar carries the declared physical meaning for a GeoCeDG-aware consumer |
| neutral export model | its unit field carries the effective construction unit and, for `usm`, its factor; `UNITLESS` remains the value for `UNSPECIFIED_MODEL_UNIT` |
| picture formats | physical size from §13: `output(L)` metres, with PDF points `= metres · 72 / 0.0254` and raster pixels `= metres · dpi / 0.0254`; with `UNSPECIFIED_MODEL_UNIT` the non-physical legacy/device-scale mode, labelled as such |
| LaTeX (PSTricks, PGF/TikZ, Asymptote) | with a physical unit, `xunit = yunit = fb(c) · 100 · a / b` centimetres per model unit; with `UNSPECIFIED_MODEL_UNIT` the explicit non-physical device parameter, never presented as a model unit or an engineering scale |
| scale wording | engineering notation `a:b` replaces "Scale in cm" for physical documents |
| authority | screen scale, `printingScale`, zoom, DPI and the viewport are never reinterpreted as the model unit |

### 15.3 Obligations of `C`

`C` amends, together and consistently in one authorized change: the applicable
DXF normative specification (`dxf-curve-fidelity-and-approximation.md`); the
geometry export foundation where appropriate, including its G5 PASS clause;
the G9X1 and future verification pins and evidence contracts; the exporter
behavior; the fidelity and sidecar unit metadata; and the DXF staleness
fingerprint, which must include the unit state. It also owns the
`OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` observation of the
[B closeout record](../../../docs/validation/pre_g9b_r6_plus_b_closeout_record.md).
No intermediate state may leave a specification describing behavior the
product or the verifier does not have.

## 16. Compatibility

### 16.1 Documents

Legacy `.cedg` documents and Classic `.ggb` inputs share the same ZIP/XML
format (`geocedg/specs/ui/native-document-identity.md:14-17`, `:77-85`).
Without the element they load as `EMPTY`; nothing is written until unit
metadata is acquired, and they re-save byte-identically as far as the unit
system is concerned. No migration exists or is needed. GeoCeDG saves only
`.cedg` (`geocedg-desktop/AppGeoCeDG.java:795-801`).

### 16.2 Rejected owners

`<kernel>`, a `<construction>` attribute, `<euclidianView>` and
`<geocedgSpatial>` are rejected owners; the reasons are recorded in
[ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md).

### 16.3 Axis unit labels

The per-axis `unitLabel` (`common/main/settings/EuclidianSettings.java:1197`)
stays an independent view presentation facility. It is not
`constructionUnit`, not `presentationUnit`, not a dimensional authority and
not an export-unit authority; neither `D0` nor `D1` synchronizes it.

## 17. Matrices

Amendment `1.1`: §20.6 adds the rows of the dimension unit suffix policy.

### 17.1 Invariant matrix

`=` unchanged; `↻` updates (presentation only); `—` not applicable.

| Operation | coordinates and numeric geometry | DAG, dependencies, order | identities (objects, Locus V2 branches/components) | Locus V2, Spline V2 semantics and parameter domains | dimension numbers | dimension texts | status UI | export interpretation | unit state |
|---|---|---|---|---|---|---|---|---|---|
| set physical construction unit | = | = | = | = | = | ↻ | ↻ | ↻ | changed |
| set construction unit to unspecified | = | = | = | = | = | ↻ (bare) | ↻ | ↻ (non-physical) | changed; `usm` kept |
| change presentation unit | = | = | = | = | = | ↻ | ↻ | = (size, `AQ-U1`) | changed |
| define `usm` | = | = | = | = | = | = unless selected | = unless selected | = unless selected | changed |
| change `k`, `usm` active | = | = | = | = | = | ↻ | ↻ | ↻ | changed |
| change `k`, `usm` inactive | = | = | = | = | = | = | = | = | changed |
| rename / symbol | = | = | = | = | = | ↻ if `effP = usm` | ↻ | = | changed |
| remove `usm` | = | = | = | = | = | = | = | = | changed |
| change `drawingScale` | = | = | = | = | = | = | ↻ | ↻ | — |
| undo / redo of a unit operation | = | = | = | = | = | ↻ | ↻ | ↻ | restored |
| paste | per paste semantics, never scaled | per paste | per paste | per paste | = | = | notice only | = | = (target) |
| load / New | the document's own | — | — | — | — | — | ↻ | — | the document's own / defaults |
| redefine rebuild, rollback | = | per redefine | per redefine | per redefine | = | = | = | = | = (restored from XML) |

### 17.2 Persistence, undo and lifecycle matrix

| State | Class | Owner | Written by | Read by | Reset | Undo | Preference |
|---|---|---|---|---|---|---|---|
| construction unit `c` | `DOCUMENT_SEMANTIC` | shared document | `geocedgUnits@construction` | clearing loads | `clearConstruction` | one point per change | default for new documents only |
| explicit presentation unit `p` | `DOCUMENT_PRESENTATION` | shared document | `geocedgUnits@presentation` | clearing loads | `clearConstruction`; removed with `c` | one point per change | default for new documents only, inert without `c` |
| `usm` factor `k` | `DOCUMENT_SEMANTIC` | shared document | `geocedgUnits@usmMetersPerUnit` | clearing loads | `clearConstruction` | one point per change | never |
| `usm` name, symbol | `DOCUMENT_PRESENTATION` | shared document | `@usmName`, `@usmSymbol` | clearing loads | `clearConstruction` | one point per change | never |
| effective units | `DERIVED_TRANSIENT` | effective-unit function | never | — | — | follows the stored state | — |
| new-document defaults | `USER_PREFERENCE` | GeoCeDG properties store | preference store | New blank document | — | none | itself |
| `drawingScale` | `SESSION` | application/document window export context | never | export | New, Open | none | none in v1 |
| clipboard provenance | `SESSION` | Desktop copy buffer | never | paste | with the buffer | none | — |
| dimension text string | `DERIVED_TRANSIENT` presentation of an `E2` output | `E2` | per `E2`'s object contract | — | — | follows the restored state | — |
| status segments | `DERIVED_TRANSIENT` | `D1` | never | — | — | — | — |

### 17.3 Compatibility matrix

| Case | Behavior |
|---|---|
| legacy `.cedg` without the element | `EMPTY` (`UNSPECIFIED_MODEL_UNIT`); byte-identical re-save; nothing written |
| Classic `.ggb` input without the element | same; saved only as `.cedg` |
| `.cedg`/`.ggb` with a valid version-1 element | state read exactly |
| older GeoCeDG or Classic reading a new document | element ignored and logged; rest loaded; lost on re-save (§8.9) |
| a build implementing this specification reading a newer `version` | fail closed (§8.7) |
| malformed version-1 element; selected `usm` without factor; duplicate element | fail closed (§8.7) |
| element inside a macro construction or a `.ggt` | ignored (§8.8) |
| element inside pasted or `evalXML` content | ignored; target state unchanged (§8.8) |
| paste between documents with different physical meanings, provenance available | numbers unchanged; non-blocking notice (§11) |
| paste with either side unspecified, or without provenance | numbers unchanged; no notice (§11) |
| two windows | each window has its own copy buffer at the base (`desktop/main/AppD.java:355`, `:5037-5043`); a cross-window route without provenance gives no notice |
| undo snapshot, redefine rebuild, rollback | state carried by the XML; restored exactly |
| preferences XML | never contains unit state |

## 18. Consumer contracts

Each consumer implements the clauses listed, re-establishes the cited seams at
its own base, and produces the listed evidence. `D0` sets no verification
class for any of them.

### 18.1 `D1` — unit state, persistence and status

- Implement §3–§5 (state, constraints, effective-unit function), §7
  (operations, undo, reinterpretation), §8 (element, grammar, canonical
  factor, omission rule, reset, fail-closed reading, ignored contexts), §9
  (every route), §10 (defaults), §11 (provenance and notice), §12 lifetime of
  `drawingScale` if `D1` introduces its holder, and the status segments for
  both units (§6.2).
- Provide a notification by which presentation consumers refresh after a unit
  change without any construction recompute.
- Evidence: byte identity of the construction XML for documents without unit
  metadata across a representative corpus and the canonical models; round
  trip of every grammar case of §8.2, including `DQ-D0-3` and `DQ-D0-4`
  states; canonical factor strings for fixed reference values, independent of
  the runtime (§8.4); fail-closed cases of §8.7 with the restore behavior;
  ignored contexts of §8.8; undo, redo, redefine rebuild and rollback
  carrying the state; defaults applied only to new blank documents; paste
  provenance and notice cases of §11; the invariant of §6.1 checked by
  comparing construction values before and after every operation of §7.1;
  preferences XML free of unit state.

### 18.2 `C` — export

Status: implemented by the `PRE-G9B-R6-plus-C` technical candidate pending
author review (2026-10-04).

- Implement §13 and §15.2; discharge §15.3 in one coherent change.
- Evidence: physical output sizes for each unit and scale; invariance under
  `presentationUnit` (`AQ-U1`); DXF `$INSUNITS` for every unit and the `usm`
  warning and sidecar; unchanged model coordinates; the non-physical mode for
  `UNSPECIFIED_MODEL_UNIT` without `1:n` labelling; LaTeX `xunit`; the
  amended specifications and verifier pins in the same candidate.

### 18.3 `E2` — native dimensions

- Implement §14: a stable model-unit number separate from the presentation
  text; the suffix rules; bare values when unspecified; the failure marker; no
  parsing of the text; overshoot and gap as creation-time captured model-unit
  inputs (§6.3, `AQ-E4`).
- Evidence: numbers unchanged and texts updated across every operation of
  §17.1; undo and redo coherence; serialization of the captured inputs.
- Amendment `1.1`: the revised `E2` candidate also implements §20 (§20.7).

### 18.4 `E3` — `IsoABorder`

- The tool captures at creation the model-units-per-millimetre factor
  `u = 10^-3 / fb(effC)` (for `usm`, `10^-3 / k` in binary64) and the
  `drawingScale` pair, as ordinary construction inputs (§6.3); the command
  never reads the unit state or `drawingScale` live.
- With `UNSPECIFIED_MODEL_UNIT` the tool is unavailable until a physical unit
  is declared (`AQ-U6`); there is no fallback factor.
- A valid `usm` enables it exactly like `mm`, `cm` or `m`.
- `AQ-E3a` (scale input form) and `AQ-E3c` (consequence of a later unit
  change) stay `E3`'s author decisions.

## 19. Traceability

### 19.1 Author-fixed semantics and `AQ-U1` to `AQ-U6`

| Decision | Clauses | Implementing phase | Evidence owner |
|---|---|---|---|
| plan §4.1 `constructionUnit` = physical meaning; change ≠ scaling, coordinate transformation, redefine | §2, §6.1, §7.2 | `D1` | `D1` (§18.1) |
| plan §4.2 legacy = `UNSPECIFIED_MODEL_UNIT`; `cm` never inferred | §3.3, §8.3, §16.1 | `D1` | `D1` |
| plan §4.3 `presentationUnit` for dimensional presentation | §5.2, §14 | `D1`, `E2` | `E2` |
| plan §4.4 (as superseded) | §13 | `C` | `C` |
| `AQ-U1` output size invariant under `presentationUnit` | §7.2, §13 | `C` | `C` |
| `AQ-U2` presentation scope | §1, §14.1 | `E2` | `E2` |
| `AQ-U3` unit set, `usm`, tokens, serialization, lenient older readers | §3, §4, §8.2–§8.4, §8.9 | `D1` | `D1` |
| `AQ-U4` copy and paste | §5.4, §11 | `D1` | `D1` |
| `AQ-U5` `drawingScale` `SESSION`, `1:1` | §12 | `C`, `E3` (and `D1` if it holds it) | `C` |
| `AQ-U6` unspecified unit | §3.3, §13, §15.2, §18.4 | `C`, `E2`, `E3` | each |
| undo semantics | §7.3, §17.2 | `D1` | `D1` |
| unit-independence invariant | §6 | `D1`, `C`, `E2`, `E3` | each |
| persistence design target | §8, §9 | `D1` | `D1` |
| export foundation | §15 | `C` | `C` |

### 19.2 `DQ-D0-1` to `DQ-D0-13`

| Disposition | Clauses |
|---|---|
| `DQ-D0-1` new-document defaults | §10 |
| `DQ-D0-2` no presentation without a physical construction unit | §5.1, §5.2, §7.1, §8.2, §10 |
| `DQ-D0-3` explicit return to unspecified; element omission rule | §7.1, §8.2, §8.3 |
| `DQ-D0-4` unused `usm` persists | §4.3, §8.6 |
| `DQ-D0-5` name and symbol | §4.4, §14.2 |
| `DQ-D0-6` binary64 factor, canonical shortest decimal, equality | §3.2, §4.1, §4.2, §5.4, §8.4 |
| `DQ-D0-7` fail closed; older readers lenient | §8.7, §8.9 |
| `DQ-D0-8` clipboard provenance | §11 |
| `DQ-D0-9` dimension text as presentation | §6.2, §14.2 |
| `DQ-D0-10` DXF and `usm` | §15.2, §15.3 |
| `DQ-D0-11` export with unspecified unit | §13, §15.2 |
| `DQ-D0-12` `drawingScale` pair | §12, §13 |
| `DQ-D0-13` axis unit labels | §16.3 |

## 20. Amendment 1.1 — dimension unit suffix policy

Status: **NORMATIVE / AUTHOR APPROVED** (decision of 2026-10-08 on the exact
candidate `T_R6PLUS_E2` revised `3a7246614a6ef040d84a1440c4c10b3f93be5776`, tree `00b5230d19debea5598299906f846f52b4d6e9d7`;
[E2 closeout record](../../../docs/validation/pre_g9b_r6_plus_e2_closeout_record.md); acceptance changed only the status
markers). Source: the
author decision of 2026-10-08 on the `E2` author smoke follow-up
([record](../../../docs/validation/pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md),
part B2), which authorizes the bounded versioned Boolean extension of
`geocedgUnits` stated here and nothing broader. Version `1.0` (§1–§19) stays the
approved historical text of the 2026-10-02 decision; this section amends it.

### 20.1 State

- The unit state `S` gains one field, the **dimension unit suffix policy**
  `σ ∈ { shown, hidden }`, of class `DOCUMENT_PRESENTATION`. It is not unit
  metadata: it changes no effective unit (§5.2), no conversion (§5.3), no
  number, no geometry, no export size and none of the properties of §6.1.
- *Unit metadata* means `S.c` present, `S.p` present or `usm` `DEFINED`.
  `EMPTY` is the state with no unit metadata and `σ = shown`; §5.1 and §7.1
  apply to the unit metadata unchanged, and every unit operation keeps `σ`.
- Changing `σ` is a document operation with exactly one undo point (§7.3);
  when the document units dialog changes `σ` together with unit metadata, the
  whole dialog commit is one undo point.
- `σ` alone never makes a document save-relevant (the `DQ-D1-5` rule applies
  to unit metadata only): an untouched new document is not modified because
  of its policy, while every saved document carries its policy.

### 20.2 Presentation (amends §14.2 and §14.3)

```text
effC physical,    σ = shown  : format(display(L)) + " " + suffix(effP)   (§14.2)
effC physical,    σ = hidden : format(display(L))
effC unspecified, either σ   : format(L)                                 (§14.2)
failure (§14.3),  σ = shown  : marker + " " + suffix(effP)
failure (§14.3),  σ = hidden : marker
```

The number shown stays `display(L)` in the effective presentation unit; the
stable numeric output stays the model-unit measure `L`. A change of `σ`
refreshes the dimension texts through the presentation notification of §18.1
without recomputing any geometry. Picture and supported LaTeX exports show the
same text as the view.

### 20.3 Persistence (amends §8.2, §8.3, §8.7 and §8.9)

```text
units-element-v2 = "<geocedgUnits" SP 'version="2"'
                   [ SP construction-attr [ SP presentation-attr ] ]
                   [ SP factor-attr [ SP name-attr ] [ SP symbol-attr ] ]
                   SP suffix-attr "/>"

suffix-attr      = 'dimensionUnitSuffix="' ( "hidden" / "shown" ) '"'
```

- **Writer.** The element is written exactly when `S ≠ EMPTY` (§20.1). With
  `σ = hidden` it is version 2 with `dimensionUnitSuffix="hidden"` as the last
  attribute; with `σ = shown` it is exactly the version-1 element of §8.2. The
  writer never emits `"shown"`. Examples:
  `<geocedgUnits version="2" construction="mm" dimensionUnitSuffix="hidden"/>`
  and, for a document without unit metadata,
  `<geocedgUnits version="2" dimensionUnitSuffix="hidden"/>`.
- **No migration.** A document read with `σ = shown` (no element, or a
  version-1 element) is written with the same bytes as under version `1.0`;
  nothing upgrades a version-1 element or a legacy document on load or re-save.
- **Reader.** A missing element in a clearing load yields `EMPTY`, hence
  `σ = shown`, the historical suffix-visible behavior of documents that carry
  no policy. A version-1 element yields `σ = shown`; a version-1 element with
  `dimensionUnitSuffix` is malformed. A version-2 element requires
  `dimensionUnitSuffix` with the value `hidden` or `shown`; otherwise it is
  malformed. Every other rule of §8.2–§8.8 applies to version 2 unchanged;
  version 3 or higher fails closed (§8.7 item 1).
- **Older readers.** A build that implements only version 1 (`D1` up to and
  including the original `E2` candidate `878ff66b`) fails closed on a
  version-2 element as a newer version (§8.7 item 1), so a document with the
  hidden policy, which by §20.5 includes new documents by default, is not
  opened by such a build. Builds before `D1` and Classic GeoGebra ignore the
  element as in §8.9 and lose the unit metadata and the policy on re-save.
  This is an explicit forward-compatibility limitation of the amendment,
  submitted for author review.

### 20.4 Routes

Every route of §9 carries `σ` with the element: undo and redo, redefine
rebuild and rollback restore it exactly; the ignored contexts of §8.8 (macro
constructions, paste, `evalXML`) never write, read or change it; the
preferences XML never contains it; `clearConstruction` resets it to `shown`
with the rest of the state.

### 20.5 Defaults (amends §10)

- The preference `geocedg.units.new-document-dimension-unit-suffix.v1` with
  the values `shown` and `hidden` sets `σ` of new blank documents; unset or
  any other value means `hidden`.
- It is applied with the other §10 defaults, only to new blank documents,
  never to an opened document or on undo, redo, redefine, rollback, paste or
  insertion; with an unspecified construction default the state is "no unit
  metadata, `σ` = the default". Applying it creates no undo point and does not
  mark the document modified. Changing the preference affects only documents
  created afterwards.

### 20.6 Matrix rows (amend §17)

| §17.1 operation | coordinates and numeric geometry | DAG, dependencies, order | identities | Locus V2, Spline V2 | dimension numbers | dimension texts | status UI | export interpretation | unit state |
|---|---|---|---|---|---|---|---|---|---|
| change dimension unit suffix policy | = | = | = | = | = | ↻ (suffix only) | = | = (size); texts as in the view | `σ` changed |

| §17.2 state | Class | Owner | Written by | Read by | Reset | Undo | Preference |
|---|---|---|---|---|---|---|---|
| dimension unit suffix policy `σ` | `DOCUMENT_PRESENTATION` | shared document | `geocedgUnits@dimensionUnitSuffix` (version 2, hidden only) | clearing loads | `clearConstruction` (to shown) | one point per change | default for new documents only |

| §17.3 case | Behavior |
|---|---|
| document without the element, or with a version-1 element | `σ = shown`; version-1 bytes on re-save |
| version-2 element read by a build implementing `1.1` | state and `σ` read exactly |
| version-2 element read by a build implementing only `1.0` | fail closed (§8.7 item 1) |
| version-1 element with `dimensionUnitSuffix`; version 2 without it or with another value | fail closed (§8.7 item 2) |

### 20.7 Consumer contract

The revised `E2` candidate implements §20.1–§20.6 in the `D1` state,
persistence and defaults seams and in the `E2` dimension presentation.
Evidence: the presentation cases of §20.2 for every effective unit, `usm` with
and without a symbol, the unspecified unit and the failure marker; the writer
and reader cases of §20.3, including version-1 byte identity and the version-2
malformations; undo and redo of a policy change; save and reopen with and
without unit metadata; an opened document without a policy keeping the suffix;
defaults applied only to new blank documents and not save-relevant; equal texts
in the view and in a LaTeX export.

### 20.8 Traceability

| Decision | Clauses |
|---|---|
| author decision of 2026-10-08, part B2 (default hidden for new documents, per-document policy, application preference for new documents only, historical behavior without a policy, no silent migration, invariants and exports preserved) | §20.1–§20.7 |
| `DQ-D1-5` save relevance | §20.1 |
| `DQ-D0-7` fail closed | §20.3 |
| `DQ-D0-9` dimension text as presentation | §20.2 |
