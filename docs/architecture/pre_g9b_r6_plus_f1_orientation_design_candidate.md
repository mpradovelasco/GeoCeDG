# PRE-G9B-R6-plus-F1 — orientation display: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `F1` needs its own
  explicit author authorization.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  finding 17

Path abbreviation: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`.

## 1. Author-fixed scope

An optional, discreet presentation cue, preferably a hollow arrow, for the
direction the kernel uses for a line, segment or ray. It derives from kernel
semantic orientation, never from the viewport, and changes no geometry and no
command behavior. No accessor is chosen by its name.

## 2. Semantic orientation authority (finding 17)

A `GeoLine` stores `(x, y, z)` for `x·X + y·Y + z = 0`.

**Authority:** `GeoLine.getDirection` = `(y, −x)`
(`common/kernel/geos/GeoLine.java:443-447`, `final`, inherited by `GeoSegment`
and `GeoRay`). It agrees with:

- the construction comment "we want AB to be the direction vector"
  (`common/kernel/geos/GeoVec3D.java:273`, `:296-299`);
- the path parameter of lines, segments (`[0, 1]` from start to end) and rays
  (from the start point) (`GeoLine.java:1140-1162`;
  `common/kernel/geos/GeoSegment.java:422-452`; `common/kernel/geos/GeoRay.java:179-243`);
- `UnitVector(g)` and `Direction(g)`, which are one algorithm with x = g.y,
  y = −g.x (`common/kernel/algos/AlgoUnitVectorLine.java:39-40`;
  `common/kernel/commands/BasicCommandProcessorFactory.java:64-67`);
- the oriented `Angle(g, h)` (`common/kernel/algos/AlgoAngleLines.java:111-117`).

**Not an authority:** `GeoLine.getDirectionInD3` returns `(−y, x)`, the
**opposite**, whenever the line has **no end point** (`GeoLine.java:1402-1409`).
End points are set by the two-point routes (`Line(A,B)`, `Segment(A,B)`,
`Ray(A,B)`), by polygon and polyline segments, and by macro-copied lines whose
definition has both points (`common/kernel/algos/AlgoMacro.java:468-476`). The
reversal therefore affects `Ray(A, v)`, `Line(P, v)`, parallel, perpendicular,
bisector, tangent and equation lines. The planning statement is confirmed.

| Route | Direction per the authority |
|---|---|
| `Line(A,B)`, `Segment(A,B)`, `Ray(A,B)` | `B − A` |
| `Ray(A, v)`, `Line(P, v)` | `v` |
| parallel line through `P` to `l` | direction of `l` |
| parallel line to a linear function `f(x) = m x + c` | `(−1, −m)`, i.e. towards −x (`common/kernel/geos/GeoFunction.java:2895-2917`) |
| perpendicular line to `l` | normal of `l` = `rot90(dir l)` |
| perpendicular to vector `v` | `rot90(v)` |
| `PerpendicularBisector(A, B)` | `rot90(B − A)` |
| `AngleBisector` | the bisector unit; may flip for continuity (`common/kernel/algos/AlgoAngularBisectorPoints.java:190-237`) |
| `Tangent` parallel to `g` | direction of `g` |
| `Tangent(P, conic)` with `P` on the conic | copies the polar line's coefficients; orientation UNKNOWN (reading `GeoConic.polarLine` settles it) |
| independent equation `y = 2x + 1` | from the normal form, left − right; INFERRED `(1, 2)`; depends on how the equation is written |
| redefinition | the new route's orientation; `Line(B, A)` reverses it |

## 3. Candidate

- **Cue:** a small hollow arrowhead in the object's colour, reusing the
  outline-arrow geometry of `SegmentStyle.ARROW_OUTLINE`
  (`common/euclidian/draw/DrawSegmentWithEndings.java:261-290`). Placement:
  - segment: at the midpoint;
  - ray: near the start point, inside the view;
  - line: at the visible point nearest the view centre.

  These positions are presentation-only choices; the direction itself comes
  only from `getDirection`.
- **Not shown** for undefined, zero-length or non-finite lines.
- **Implementation surface (`AQ-F1`, recommended: global and screen-only):**
  a `GeoCeDGEuclidianView.paint` overlay
  (`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianView.java:38-75`).
  It is not part of `exportPaint`, so the cue never reaches exports (PROVEN
  FROM SOURCE). The overlay exists only in Graphics 1: `GeoCeDGEuclidianView`
  is the Graphics 1 class, while Graphics 2 is the upstream
  `EuclidianViewFor3DD`
  (`source/desktop/desktop/src/main/java/org/geogebra/desktop/geogebra3D/gui/GuiManager3D.java:257-265`).
  Graphics 2 support, or an exported cue, needs drawable-level work in the
  shared `DrawLine`, `DrawRay` and `DrawSegment` code, and `F1` escalates.
- **Option state:** a global view toggle stored as a `USER_PREFERENCE` key
  (`geocedg.view.orientation-cue.v1`, portable GeoCeDG properties). Rejected:
  per-object document style (it would change documents) and `SESSION` (lost on
  restart for a preference-like setting).

## 4. Serialization

None.

## 5. Verification-class recommendation

`BOUNDED_PHASE` (registered `PHASE`): a presentation-only overlay and one
preference. It escalates if the cue is exported or persisted per object.
