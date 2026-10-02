# ADR 0032: Unit-system semantics and persistence ownership

- Status: **Proposed — `PRE-G9B-R6-plus-D0` documentary candidate**
- Date: 2026-10-02
- Phase: `PRE-G9B-R6-plus-D0` (normative design; no implementation)
- Normative contract: [GeoCeDG unit system](../../geocedg/specs/units/unit-system.md)
  (`NORMATIVE CANDIDATE — NOT AUTHOR APPROVED`, version `1.0`)
- Author decisions carried:
  [mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md) §4,
  [D0 author-decision record](../validation/pre_g9b_r6_plus_d0_author_decisions_record.md),
  [D0 preparation closeout and authorization record](../validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md)
- Design input: [D0 unit-system inputs](../architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md)
- Related: [ADR 0005](0005-neutral-2d-geometry-export.md) (neutral 2D export;
  not amended by this ADR)

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This record carries no approval of its own; acceptance is recorded only by a
separate author-decision record. The rules it decides are stated in the unit
specification, which this ADR does not restate.

## Context

GeoCeDG constructions store plain numbers. At the base
`0ef616bb7bb8efd3e4f19762f38936ff4742d1c3` no geometric computation reads a
physical unit, and no document carries one. Every centimetre in the code is an
output convention: the zoom-derived `printingScale`
(`source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianView.java:1933-1936`),
which `B` reuses for picture sizing; the LaTeX default `xunit = 1` cm; the
excluded STL and Collada formats; or a free-text axis label. DXF export writes
`$INSUNITS = 0` under the experimental geometry export foundation and the
author-approved G9X1 fidelity contract.

The author fixed the semantics (mini-track plan §4): `constructionUnit` is the
physical meaning of one model unit, and changing it is a reinterpretation, not
scaling; a legacy document is `UNSPECIFIED_MODEL_UNIT` and `cm` is never
inferred; `presentationUnit` expresses dimensional presentation;
`drawingScale` is dimensionless; output size is invariant under an equivalent
`presentationUnit` change (`AQ-U1`). On 2026-10-02 the author decided
`AQ-U2` to `AQ-U6`, the undo rules, the unit-independence invariant, the
persistence target and the thirteen dispositions `DQ-D0-1` to `DQ-D0-13`.

Two questions remain architectural: where the unit state lives and how it is
persisted, and how a document-semantic attribute can exist without becoming a
geometric dependency.

## Decision

1. **Unit state is document state, not geometry.** `constructionUnit` and the
   `usm` factor are `DOCUMENT_SEMANTIC`; `presentationUnit` and the `usm` name
   and symbol are `DOCUMENT_PRESENTATION`; `drawingScale` and clipboard
   provenance are `SESSION`; the new-document defaults are `USER_PREFERENCE`
   (specification §17.2). They belong to the shared document model, because
   every frontend must read the same physical meaning; they are not geometric
   truth (`AGENTS.md` §4).
2. **No live geometric dependency.** No algorithm, command or construction
   element depends on, reads during computation, or recomputes because of the
   unit state or `drawingScale`. Only dimension presentation strings, status
   and presentation UI, and export adapters read them live; tools may capture
   them once at creation as ordinary model-unit inputs (specification §6).
3. **One effective-unit function.** All consumers derive the effective units
   and metre factors from one pure function of the stored state
   (specification §5). Physical meaning is compared by binary64 metre factor,
   never by token or name alone.
4. **Persistence owner.** One flat, versioned, self-closing element
   `geocedgUnits`, a child of the document `<construction>`, written by
   `Construction.getConstructionXML` before `<geocedgSpatial>` and the
   elements, therefore present in the full document XML and in every undo,
   redefine and rollback snapshot; never in macros, clipboard XML or the
   preferences XML; written only when unit metadata exists, so legacy
   documents stay byte-identical (specification §8, §9).
5. **Reset in `clearConstruction`, applied only by clearing loads.** The unit
   state resets to empty in `clearConstruction` and is applied only by clearing
   loads of the document construction (full load, undo, redo, redefine
   rebuild, rollback restore); non-clearing parses (paste, `evalXML`) and macro
   parses ignore the element.
6. **Fail closed for recognizing builds; lenient older readers documented.** A
   build that implements the specification refuses to open a document whose
   unit metadata is newer, malformed or ambiguous; older GeoCeDG builds and
   Classic ignore the element and lose it on re-save, an accepted and explicit
   forward-compatibility limitation (specification §8.7, §8.9).
7. **One custom physical unit.** `usm` is one document-scoped definition
   `1 usm = k m` with `k` a finite positive binary64, serialized as the
   canonical shortest round-trip decimal; it persists while the element exists,
   even unused (specification §4, §8.4).
8. **Export stays an adapter.** Physical output derives from the effective
   construction unit and `drawingScale`; model coordinates are never
   converted. The future DXF, picture and LaTeX rules, and the obligation to
   amend the export specifications and verifier pins coherently, belong to `C`
   (specification §13, §15). Until then the in-force export rules stay
   unchanged and the experimental foundation is not promoted.

## Alternatives considered

| Alternative | Rejected because |
|---|---|
| persist in `<kernel>` | `<kernel>` is not reset per load (`common/kernel/Kernel.java:3061-3082`), is written into the preferences XML (`common/io/MyXMLio.java:345-358`) and is re-applied by File → New (`desktop/main/AppD.java:1174`); a document unit would leak into preferences and into new documents |
| an attribute of `<construction>` | unversioned; the reader keeps only `title`, `author`, `date` (`common/io/MyXMLHandler.java:2573-2595`), so older builds drop it; it is also emitted for every macro construction |
| `<euclidianView>` | per view and presentation; a construction has one physical meaning |
| inside `<geocedgSpatial>` | it would force old GeoCeDG readers to fail closed, but it mixes unit semantics into the spatial identity section, which is written only when the registry is non-empty; the author forbade it (`AQ-U3`, `DQ-D0-7`) |
| a fail-closed marker for older readers | impossible without the `<geocedgSpatial>` route; the author chose lenient older readers with documented loss |
| reset when the element is absent during parse | every paste is wrapped in a bare `<construction>` (`common/util/InternalClipboard.java:626-640`), so the target unit would reset on every paste |
| infer `cm` for legacy documents | the only "1 unit = 1 cm" is the zoom-derived `printingScale` at standard zoom; author-forbidden |
| convert coordinates on unit change or paste | that is geometric scaling; author-forbidden (plan §4.1, `AQ-U4`) |
| a live dependency of dimension objects or `IsoABorder` on the unit | a unit change would move geometry, contrary to plan §4.1 |
| make `presentationUnit` a second scale factor | contradicts `AQ-U1` |
| lenient loading in a recognizing build (ignore malformed metadata) | it would silently drop declared physical semantics and could turn a `usm` document unspecified; replaced by fail closed (`DQ-D0-7`) |
| delete the whole element when the construction unit is cleared | it would silently lose a valid `usm` definition; replaced by the omission rule (`DQ-D0-3`, `DQ-D0-4`) |
| map a `usm` equal to a DXF-coded unit to that code | the author fixed `$INSUNITS = 0` plus a required sidecar for every `usm` export (`DQ-D0-10`) |
| a registry of several custom units | out of version 1 (`AQ-U3`) |
| a decimal (non-binary64) factor semantics | the author fixed binary64 (`DQ-D0-6`) |

## Consequences

- `D1` is a shared-serialization phase: a new element read on every load and
  written on every save and undo, with fail-closed reading and a byte-identity
  obligation for legacy documents. The mini-track plan proposes
  `GLOBAL_IMPACT` for it; this ADR sets no class.
- `C` must change the export foundation, the G9X1 fidelity contract, its
  verifier pin, the exporter and the sidecar together, in one authorized
  change.
- `E2` keeps dimension numbers in model units; only its text depends on units.
  `E3` captures its unit factor at creation.
- Documents saved by a `D1` build and re-saved by an older build lose their
  units silently. Users must be told; the living guides record it (`D1` or `G`).
- The spatial frame `units` token stays unrelated to `constructionUnit`.
