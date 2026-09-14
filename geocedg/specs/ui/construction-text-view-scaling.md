# Construction Text view scaling

- Phase: `PRE-G9B-S3`
- Status: `IMPLEMENTATION AUTHORIZED — CANDIDATE PENDING REVIEW`
- Scope: GeoCeDG Euclidian presentation only
- Semantic-format change: none
- Self approval: `false`

## 1. Inherited modes

`GeoText.isAbsoluteScreenLocActive()` is the existing authoritative placement
mode discriminator.

| Mode | Inherited placement | S3 behavior |
|---|---|---|
| World-relative Text with a start point | start point is transformed from world coordinates; label offsets remain screen offsets | scale glyphs and all drawable bounds per view |
| Non-absolute Text without a start point | inherited origin-relative fallback plus label offsets | scale glyphs and all drawable bounds per view |
| Absolute-screen Text | stored screen coordinates bypass the world transform | preserve inherited screen-space glyph size |
| Ordinary, multiline or LaTeX content | content/style selects the inherited renderer; placement mode is independent | apply the same effective font rule when world-relative |

Text background, highlight/frame-equivalent presentation and selection use the
same `DrawText` label rectangle as glyph layout. They are not separate modes.
Copy/redefine/save preserve the existing placement flag, start point, content,
font multiplier and style; S3 adds no copied or serialized field.

## 2. Applicability

S3 applies exactly when both conditions hold:

1. the application configuration enables GeoCeDG construction-Text view
   scaling; and
2. `GeoText.isAbsoluteScreenLocActive()` is `false`.

The first condition contains the behavior to GeoCeDG. Its default for all
existing upstream/Classic configurations is false. The second preserves the
intentional screen-fixed mode.

## 3. Scalar scale law

Let `fLogical` be the existing result of
`GeoText.getFontSize(view.getFontSize())`. S2 owns the base construction font
and the existing `GeoText` multiplier that produce `fLogical`.

For an applicable drawable in Euclidian view `V`:

```text
z(V)       = V.getXscale() / EuclidianView.SCALE_STANDARD
fEffective = fLogical * z(V)
```

`EuclidianView.SCALE_STANDARD` is the inherited standard of 50 pixels per world
unit. `xscale` is the inherited primary Euclidian `scale`: it is the value named
`scale` by view events/settings and the primary value in coordinate-system
persistence, while `yscale` records the independent vertical scale/ratio.
Therefore `xscale / SCALE_STANDARD` is an existing deterministic convention,
not a new average.

When `xscale != yscale`, the scalar remains `xscale / SCALE_STANDARD` for both
glyph axes. Glyphs are never anisotropically transformed. Ordinary zoom changes
both scales proportionally and therefore changes text proportionally; changing
only the axes ratio does not redefine the horizontal reference scale.

No creation-time baseline, viewport history, DPI normalization, object field,
semantic clamp or serialized scale is introduced. Platform font rasterization
may quantize very small or fractional effective sizes; layout and hit bounds
must use the same derived font instance, so they remain coherent.

## 4. Rendering and interaction

Every applicable `DrawText` recalculates its effective font during the existing
per-view drawable update triggered by a coordinate-system change. A font change
must synchronously remeasure the label rectangle before hit testing; relying on
a later paint to repair the rectangle is insufficient.

Ordinary indexed text, multiline layout, bold/italic, serif/sans, color,
background and alignment continue through inherited code using the effective
font. LaTeX receives that same font. The inherited LaTeX cache key includes the
font size, so a changed effective size selects/regenerates a resolution-aware
entry rather than scaling a stale raster.

Each Euclidian view owns its `DrawText` and label rectangle. The same `GeoText`
may consequently have different pixel bounds in Graphics and Graphics2 while
retaining one construction identity, content, anchor and logical font.

## 5. Semantic and lifecycle invariants

Zoom may change only per-view presentation. It must not change:

- `GeoText` identity, content, start point or dependencies;
- anchor world coordinates or label offsets;
- logical/base font size, font multiplier or style;
- construction order, DAG, undo identity or document modified state;
- Text XML or introduce a new `.cedg`/`.ggb` field.

Equivalent content reopened with an equivalent Euclidian coordinate system
derives the same effective visual size. Absolute-screen Text and every
non-GeoCeDG configuration retain inherited behavior.

## 6. Validation seam

Focused tests measure the drawable font and label rectangle rather than compare
screenshots. They cover proportional zoom in/out, anchor stability, hit bounds,
ordinary/multiline/LaTeX content, backgrounds, nonuniform scales,
absolute-screen containment, independent views, unchanged XML/logical font,
Classic containment and retained S2 10 pt ownership. A deterministic manual
construction is smoke evidence only and is not a normative visual baseline.
