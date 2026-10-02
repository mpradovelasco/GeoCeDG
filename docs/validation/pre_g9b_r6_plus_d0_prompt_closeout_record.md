# PRE-G9B-R6-plus-D0 preparation package closeout and authorization record

```text
PRE-G9B-R6-plus-D0 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-D0                     = AUTHORIZED FOR EXECUTION
                                         (normative/documentary only)
```

```text
selfApproved              = false
authorApproved            = true    (D0 documentary preparation package only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = true    (D0 normative/documentary execution only;
                                     D1 and every later subphase: false)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author instruction of 2026-10-02 that
approves the documentary preparation package of `PRE-G9B-R6-plus-D0`, gives the
author dispositions on the requested decisions `DQ-D0-1` to `DQ-D0-13`, fixes
the boundaries of the existing export specifications, and authorizes the
execution of `D0`. The package is not a phase: approving it closes the
preparation, not `D0`. This record changes no product, test, build, packaging,
verifier, registry, schema, specification, ADR or serialization file, and it
does not modify the frozen candidate. Its machine-readable mirror is
[`pre-g9b-r6-plus-d0-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-prompt-closeout.json).

## Author disposition

```text
T_R6PLUS_D0_PROMPT = 8814468101e3caef45d9f6cbad4bf563523c3478
                     tree   edbe623604330e62db11ff3d3b05b7d73671dbd0
                     parent 0ef616bb7bb8efd3e4f19762f38936ff4742d1c3  (P_R6PLUS_B)
                     branch phase/pre-g9b-r6-plus-d0-prompt (local; never pushed)
AUTHOR_APPROVAL    = APPROVED, subject to the DQ-D0 dispositions below
selfApproved       = false
```

The approved package is:

- the canonical prompt
  [`pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md)
  as prepared (blob `beb4d6d97dd6c0e5d27b11d3876fa8647223e38b`);
- the versioned [D0 author-decision record](pre_g9b_r6_plus_d0_author_decisions_record.md)
  and its mirror
  [`pre-g9b-r6-plus-d0-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-author-decisions.json);
- the roadmap 4.45 and mini-track plan updates.

The accepted evidence of the package is `STATIC`
`verification-6185d9f8d57f444da3e1424d347b7ba3` on `T_R6PLUS_D0_PROMPT`: exit 0,
`ACCEPTED / COMPLETE`, 3/3 (`prompt.execution-safety`,
`repository.boundary-safety`, `static.scientific-inputs.semantic`), plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hash
`350f469a4d588af1d960a56f81cb1d09203220dfd31366db1cfc64f358fdf561`, with the two
standing diagnostics (`diagnostic.governance` `DIAGNOSTIC_FINDING`,
`diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`).

The frozen candidate is not amended. Where its prompt states a default for
`DQ-D0-1` to `DQ-D0-13`, or a characterization this record corrects, **this
record prevails**. The same instruction requires, as the first tracked edit of
the phase, the amendment of the prompt that integrates these dispositions.

## Authorization

```text
PRE-G9B-R6-plus-D0   = AUTHORIZED FOR EXECUTION
EXECUTION_BASE       = 8814468101e3caef45d9f6cbad4bf563523c3478   (T_R6PLUS_D0_PROMPT;
                       tree edbe623604330e62db11ff3d3b05b7d73671dbd0; not published)
VERIFICATION_CLASS   = DOCUMENTATION_STATUS_ONLY
PLANNED_ACCEPTANCE   = STATIC on the exact frozen documentary candidate
STOP_STATE           = PRE-G9B-R6-plus-D0 = DOCUMENTARY CANDIDATE PENDING AUTHOR REVIEW
                       selfApproved = false, authorApproved = false, passClaimed = false
```

Not authorized: product code, Java, implemented serialization, GUI,
status bar, copy/paste implementation, exporters, verifier changes that
implement `C`, native dimensions, `IsoABorder`, layer work, `PHASE`,
`INTEGRATION`, `FINAL`, push, merge to `main`, tag, release, `D1`, `A-2`, `C`,
`E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B`.

## Author dispositions on `DQ-D0-1` to `DQ-D0-13`

| ID | Outcome | Disposition |
|---|---|---|
| `DQ-D0-1` | accepted with precision | User preferences may define defaults for both `constructionUnit` and `presentationUnit`, applied only when a **new blank document** is created. A construction-unit preference may be `UNSPECIFIED_MODEL_UNIT`, `mm`, `cm` or `m`; `usm` cannot be one, because its physical definition is document-scoped. No unit preference is ever applied to an opened document. If the effective new-document construction unit is unspecified, any presentation-unit preference stays inert and creates no physical document semantics. |
| `DQ-D0-2` | accepted with precision | There is no effective document-level `presentationUnit` while `constructionUnit = UNSPECIFIED_MODEL_UNIT`. A user preference may still exist independently but is inert until a physical construction unit is declared. A presentation unit is never serialized as if it gave an unspecified model a physical meaning. |
| `DQ-D0-3` | **replaced** | The user may explicitly set `constructionUnit = UNSPECIFIED_MODEL_UNIT`, as an undoable document operation. It removes the active physical construction-unit interpretation and any effective document presentation-unit interpretation that depends on it. It does not necessarily delete the whole unit metadata element: a valid document-scoped `usm` definition may stay stored while unused. The element is omitted only when no unit metadata remains to preserve. *Absence of a construction-unit selection* = `UNSPECIFIED_MODEL_UNIT` does not imply that the element is always absent. Legacy documents that never acquired unit metadata stay byte-identical and contain no element. |
| `DQ-D0-4` | accepted | A valid `usm` definition is document metadata and persists while its metadata element exists, even when neither unit selects `usm`, so the user can switch away from and back to the same custom physical unit without silently losing its definition. |
| `DQ-D0-5` | accepted | `usm` may carry optional document-scoped presentation metadata: a human-readable name and a symbol. Neither is semantic identity. The semantic identity is the durable token `usm` plus its declared `metersPerUnit`. Without a symbol the presentation fallback is `usm`. |
| `DQ-D0-6` | accepted with an exact numerical contract | `usmMetersPerUnit = k`, `k > 0` and a finite binary64. Serialization uses a locale-independent deterministic shortest decimal that round-trips to the same binary64. Invalid, zero, negative, NaN or infinite values are rejected without replacing the previous valid definition. Two `usm` definitions are equal when their canonical binary64 semantic values are equal. The built-in units keep their exact SI definitions (`mm = 10^-3 m`, `cm = 10^-2 m`, `m = 1 m`). An arbitrary `usm` factor is never described as symbolically exact beyond its stored binary64 value. |
| `DQ-D0-7` | **replaced** | A GeoCeDG version that recognizes the `<geocedgUnits>` family **fails closed** when the element version is newer than the highest supported version, a recognized v1 element is structurally malformed, a selected `usm` lacks a valid factor, or the required physical semantics cannot be established unambiguously. The document is not opened with the unit metadata silently ignored; an explicit user-visible load error is reported. Older GeoCeDG and Classic builds that do not recognize the element may ignore it and lose it on re-save; that stays an explicit forward-compatibility limitation. Unit metadata is never moved into `geocedgSpatial` merely to make old readers fail closed. |
| `DQ-D0-8` | accepted with precision | The source unit context needed only for the `AQ-U4` non-blocking mismatch notice is transient clipboard provenance metadata stored beside the internal copy buffer. It is never geometry, construction XML, serialized clipboard element content or document semantic state. Paste semantics never depend on it: numbers are copied unchanged, with no coordinate conversion and no scaling. With reliable provenance and different physical meanings, the notice is shown; without provenance, paste proceeds unchanged, inferring and converting nothing. |
| `DQ-D0-9` | accepted with precision | A native dimension keeps a stable numeric model-unit value separate from its presentation text. Its `GeoText` presentation may change when `constructionUnit` or `presentationUnit` changes, and a downstream consumer that explicitly consumes that text may observe the changed string. That is a presentation dependency, not a geometric-unit dependency. The formatted string is never parsed back into geometry or numeric semantics automatically. The unit-independence invariant applies to authoritative numeric geometry, not to explicitly unit-sensitive presentation strings. |
| `DQ-D0-10` | **replaced** | For `mm`, `cm` and `m`, `C` may map to their native DXF `$INSUNITS` values with numerically unchanged model coordinates. For `constructionUnit = usm`, AC1015 has no native arbitrary-scale value, so the future `C` behavior is: unchanged coordinates; `$INSUNITS = 0`; an explicit custom-unit warning; the paired GeoCeDG sidecar **required** for that export, recording at least the token `usm` and the canonical `metersPerUnit`. The sidecar preserves the declared physical meaning for a GeoCeDG-aware consumer; the core DXF alone stays unitless. `D0` records this future contract only and does not modify the current G9X1 product, verifier or normative DXF fidelity specification in isolation; `C` amends the applicable DXF specification, verifier and implementation coherently. |
| `DQ-D0-11` | accepted with precision | `UNSPECIFIED_MODEL_UNIT` forbids any claim of physical engineering scale. Picture and vector export may keep a clearly non-physical legacy/device-scale mode where existing functionality requires it; such an export is never labelled or interpreted as `1:n` engineering-scale output. `IsoABorder` and every operation that needs a model-unit to physical-unit conversion stay unavailable until a physical construction unit is declared. No `1 model unit = 1 cm` fallback exists. |
| `DQ-D0-12` | accepted with precision | `drawingScale` is a positive rational pair `a:b` of positive integers, meaning `physical output / physical model = a / b` (`1:1` full size, `1:2` half size, `2:1` double size), normalized deterministically by the greatest common divisor, default `1:1`, `SESSION`, shared by the applicable export context of the current application/document window, reset on New and Open, not serialized and not undoable. |
| `DQ-D0-13` | accepted | The existing per-axis `unitLabel` stays an independent Euclidian-view presentation facility in v1. It is not `constructionUnit`, not `presentationUnit`, not a dimensional authority and not an export-unit authority. No automatic synchronization is introduced in `D0` or `D1`. |

## Existing-specification boundary

`D0` records explicitly that:

- [`geometry-export-foundation.md`](../../geocedg/specs/export/geometry-export-foundation.md)
  has `Status: Experimental`, under ADR 0005 accepted for the G5 experimental
  implementation. `D0` does not promote it to normative status because it
  defines a normative unit contract.
- [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
  is `NORMATIVE / AUTHOR APPROVED` and fixes the DXF baseline as unitless.
  `D0` does not create an inconsistent intermediate repository by changing it
  while the product and the G9X1 verifier still enforce `UNITLESS`.

`D0` defines the future unit semantics and creates an explicit obligation for
`C` to amend, together and consistently: the applicable DXF normative
specification; the geometry export foundation where appropriate; the G9X1 and
future verification pins; exporter behavior; fidelity and sidecar unit
metadata. Until `C`, the implemented DXF baseline stays unitless.

## Current Picture-export boundary

After `B`, Picture export still derives its existing physical sizing through
`printingScale`. `D0` does not alter it. `D0` establishes that `printingScale`,
zoom, DPI and the viewport are not a model-unit authority. `C` later replaces
the physical engineering-scale interpretation using the `D0`/`D1` unit
contract.

## Living-plan wording

The living-plan wording `physical model-to-output unit = presentationUnit`
(mini-track plan §4.4) is corrected or explicitly superseded by `D0`: the
normative rule derives from `constructionUnit + drawingScale`, while
`presentationUnit` controls how dimensional quantities are expressed. Physical
output size stays invariant under an equivalent presentation-unit change.

## Outputs required from `D0`

1. unit-system ADR;
2. normative unit-system specification;
3. the appropriate proposed/future amendment contract for geometry export;
4. persistence and XML grammar contract for `D1`;
5. exact `usm` grammar and lifecycle;
6. invariant matrix;
7. persistence, undo and compatibility matrix;
8. `AQ-U1`…`AQ-U6` traceability;
9. `DQ-D0-1`…`DQ-D0-13` dispositions;
10. explicit contracts handed to `D1`, `C`, `E2` and `E3`;
11. candidate report;
12. machine-readable evidence mirror.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
D0 preparation package     = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-D0         = AUTHORIZED FOR EXECUTION
PRE-G9B-R6-plus-D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.
