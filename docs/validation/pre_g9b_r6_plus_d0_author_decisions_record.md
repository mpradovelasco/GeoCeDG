# PRE-G9B-R6-plus-D0 author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS
DECISION_DATE            = 2026-10-02
DECIDED_ON_BASE          = 0ef616bb7bb8efd3e4f19762f38936ff4742d1c3
                           tree be7ef7acbc3503002f3dac2bc33f047795f7a644
                           (P_R6PLUS_B, published B closeout)

selfApproved             = false
implementationAuthorized = false   (D0, D1 and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the author decisions of 2026-10-02 for
`PRE-G9B-R6-plus-D0`. It is the authority for them. Its machine-readable mirror
is
[`pre-g9b-r6-plus-d0-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-author-decisions.json).

The author stated that these decisions **do not authorize the execution of
`D0` or `D1`**. They answer the open unit decisions `AQ-U2` to `AQ-U6` of the
[P0 candidate report](pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15 and the undoability question of the
[D0 unit-system inputs](../architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md)
§8. `AQ-U1` stays as resolved on 2026-10-01. Every other open decision of
that report stays open. The frozen P0 candidate report and the D0 inputs are
not amended. Where this record differs from them, this record prevails.

The same instruction authorized only the documentary preparation of the
canonical `D0` prompt, this record and the status updates they strictly
require.

## Author-fixed semantics restated

These were fixed by the author in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md) §4 and are
confirmed by this instruction. `D0` preserves them; it does not reopen them.

- `constructionUnit` is the physical meaning of one numerical model unit.
  Changing it is a physical reinterpretation, never scaling:

  ```text
  change constructionUnit != geometric scaling
  change constructionUnit != coordinate transformation
  change constructionUnit != redefine
  ```

- A legacy document without unit metadata has
  `constructionUnit = UNSPECIFIED_MODEL_UNIT`. Centimetres are never inferred
  from `printingScale`, zoom, DPI, the viewport or the historical behavior of
  an exporter.
- `presentationUnit` expresses dimensional presentation quantities.
- `drawingScale` is a dimensionless engineering ratio.
- `AQ-U1` stays: the printed or exported physical dimension is invariant under
  an equivalent change of `presentationUnit`.

## Decisions

### `AQ-U2` — dimensional presentation scope

- In the first version, only the native dimension objects that `E2` delivers
  present physical units.
- `Distance`, `Length`, `LocusLength` and every other existing numeric result
  stay values in model units, with no physical suffix.
- General `GeoNumeric` objects are not turned into dimensional quantities.
- Areas, volumes and other powers of the unit are outside `D0` v1.
- Angles stay under their own, independent `angleUnit` contract.

### `AQ-U3` — admitted units, custom unit, tokens and forward compatibility

Physical construction units admitted in v1: `mm`, `cm`, `m` and `usm`, plus the
non-physical state `UNSPECIFIED_MODEL_UNIT`.

Exact standard factors relative to the metre:

```text
mm = 10^-3 m
cm = 10^-2 m
m  = 1 m
```

**`usm`.** One document-scoped user-defined physical unit. Its durable
semantic identity is the internal token `usm`. A valid definition is

```text
1 usm = k m      with k finite and k > 0
```

- The user must explicitly define the factor before `usm` can become the
  effective construction or presentation unit. An absent or invalid factor
  never falls back to an inferred value and never turns `usm` into
  `UNSPECIFIED_MODEL_UNIT`.
- The unit may carry presentation metadata such as a name and a symbol. They
  are presentation metadata, not semantic identity. Renaming the unit without
  changing its factor does not change its physical semantics.
- Changing the factor while `usm` is active is a physical reinterpretation of
  the document, exactly like changing `constructionUnit` between standard
  units: no coordinate changes, no geometry is scaled, no DAG dependency
  changes, no object identity changes, the operation is undoable, and the
  dependent dimensional presentation and export interpretation update
  accordingly.
- An invalid attempted factor change is rejected and the previous valid
  definition stays active.
- Only one `usm` definition exists per document in v1. `D0` and `D1` introduce
  no arbitrary registry of custom units.
- The same document-scoped definition may be used by `presentationUnit`:

  ```text
  constructionUnit = usm, no explicit presentationUnit
      -> effective presentationUnit = usm
  ```

  A standard construction unit may use `usm` as its presentation unit when a
  valid `usm` definition exists.

**Serialized tokens.** Exactly `mm`, `cm`, `m` and `usm`.
`UNSPECIFIED_MODEL_UNIT` is represented by the absence of construction-unit
metadata, never by a serialized physical-unit token.

**`usm` factor serialization.** When `usm` is referenced, its metre factor is
serialized in the same flat, versioned, self-closing GeoCeDG unit element, in a
locale-independent, deterministic, round-trip-reproducible lexical form.
Conceptually:

```text
<geocedgUnits version="1" construction="usm" presentation="cm" usmMetersPerUnit="0.0254" .../>
```

The exact element and attribute names are a `D0` normative decision. The
custom-unit definition is never placed inside `geocedgSpatial`.

**Forward compatibility.** Older GeoCeDG builds and Classic stay lenient.
Unknown unit metadata may be ignored and lost on re-save. `D0` documents this
forward-compatibility limitation explicitly.

### `AQ-U4` — cross-document copy and paste

Copy and paste move numeric geometry, not physical lengths. They never convert
coordinates and never scale geometry because of a unit difference.

| Source and target | Behavior |
|---|---|
| both physical, different effective metre factors | accept; keep every numeric value exactly; produce a non-blocking notice; no implicit conversion |
| both physical, equal effective metre factors | accept; keep values; no notice |
| one or both `UNSPECIFIED_MODEL_UNIT` | accept; keep values; infer no unit; no scaling |

- Compatibility and warning logic compares the **effective metre factors**,
  not only the tokens. Two `usm` documents with different factors are
  physically different and produce the same non-blocking notice as any other
  differing physical units.
- The notice is not part of the geometry, and it is not part of the paste's
  undo beyond the paste's own normal behavior.

### `AQ-U5` — `drawingScale`

```text
drawingScale = SESSION
default      = 1:1
```

Not serialized, not part of undo, reset on New and Open. It is export and
presentation state, not geometric semantics.

### `AQ-U6` — unspecified construction unit

While `constructionUnit = UNSPECIFIED_MODEL_UNIT`:

- no engineering-scale physical export exists;
- the user must declare a physical unit before using an engineering drawing
  scale with physical meaning, `IsoABorder` or any other operation that must
  convert model units into a physical magnitude;
- there is no implicit `1 unit = 1 cm` fallback;
- dimensions show only the bare model value, with no suffix.

`UNSPECIFIED_MODEL_UNIT` is the only state in which no physical model-unit
interpretation exists. A valid `usm` is a declared physical construction unit
and enables engineering-scale export and `IsoABorder` exactly like `mm`, `cm`
or `m`. An undefined `usm` cannot be selected and does not enable physical
export.

### Undo semantics

| Change | Undo |
|---|---|
| `constructionUnit` | one undoable document operation |
| `presentationUnit` | one undoable document operation |
| `usm` factor while `usm` is active | undoable (`AQ-U3`) |
| `drawingScale` | not undoable |
| the user preference used as the default for new documents | no document undo |

Undo and redo restore the unit and the dimensional presentation coherently,
without changing identity or geometry.

### Unit-independence invariant

`D0` declares normatively that `constructionUnit`, `presentationUnit` and
`drawingScale` **must not** alter live:

```text
coordinates; authoritative numeric geometry; DAG topology; dependencies;
semantic identities; construction order; geometric object identity;
LocusV2 branch/component identity; LocusV2 semantics; SplineV2 semantics;
parameter domains; numeric geometric outputs
```

The only permitted live consumers are the presentation strings of dimensions,
status and presentation UI, and export adapters.

A tool may read the unit or the scale **once, at creation**, to compute an
explicit input in model units. From then on that value is an ordinary
construction parameter and does not follow later unit changes. No live
geometric dependency on units is permitted.

### Persistence design target for `D1`

`D0` normatively specifies, and does not implement, a flat, versioned,
self-closing GeoCeDG element under `<construction>`, conceptually
`<geocedgUnits version="1" construction="mm" presentation="cm"/>`, with its
final names fixed by `D0`. Requirements:

- lazy writing;
- absence means `UNSPECIFIED_MODEL_UNIT`;
- no automatic writing for legacy documents;
- reset in `clearConstruction`, not because of absence during parse;
- paste does not reset the target unit;
- macro constructions neither write nor consume the element;
- a flat element without children;
- never in the preferences XML;
- a coherent undo snapshot;
- legacy byte identity when there are no units.

### Export foundation

`D0` produces the normative amendment of
[`geometry-export-foundation.md`](../../geocedg/specs/export/geometry-export-foundation.md)
for this future contract:

- geometry and model coordinates stay unconverted;
- DXF `$INSUNITS` derives from `constructionUnit` in `C`/`D1`;
- `$INSUNITS = 0` while the unit is unspecified;
- screen scale, `printingScale` and DPI are never reinterpreted as the model
  unit.

`D0` does not modify the exporter.

## `D0` outputs fixed by the author

Once authorized, `D0` produces:

1. an ADR on unit semantics and persistence ownership;
2. a versioned normative specification of the unit system;
3. the amendment of the geometry export foundation;
4. invariant, persistence and compatibility matrices;
5. `AQ-U1` to `AQ-U6` traceability;
6. an explicit contract for `D1`, `C`, `E2` and `E3`;
7. a candidate report and its machine-readable mirror.

No product code, Java, serialization implementation, GUI, status bar,
exporter, native dimension or `IsoABorder`.

## Verification class frozen for `D0`

```text
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY
PLANNED_ACCEPTANCE  = STATIC on the exact documentary candidate
PHASE, INTEGRATION, FINAL = not run for D0, unless its scope changes before
                            the freeze
```

## What stays open

- The additional decisions identified by the preparation, listed in the
  [canonical `D0` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md)
  under *Decisions requested before authorization*. The prompt fixes a
  default contract for each; the authorizing instruction may confirm or
  replace it.
- `AQ-E3a`, `AQ-E3c` and `AQ-E4` (owned by `E3` and `E2`), and every other open
  decision of the P0 candidate report §15.

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B    = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0   = PREPARED — NOT AUTHORIZED   (next operational subphase;
                       canonical prompt prepared)
PRE-G9B-R6-plus-D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                  = NOT AUTHORIZED
```
