# PRE-G9B-S3 author smoke checklist

- Status: `PENDING AUTHOR REVIEW`
- Self approval: `false`
- Fixture:
  [`pre-g9b-s3-text-view-scaling.ggb`](../../models/regression/pre-g9b-s3-text-view-scaling/pre-g9b-s3-text-view-scaling.ggb)
- Normative visual baseline: none; screenshots are not acceptance authority

1. Open the S3 fixture in GeoCeDG.
2. Note the size of the ordinary world-anchored Text relative to the blue
   segment.
3. Zoom in substantially and confirm that the world-anchored Text enlarges
   with the construction.
4. Zoom out substantially and confirm that it shrinks correspondingly.
5. Confirm that each Text anchor remains at the same world position.
6. Select/click the scaled ordinary Text and verify that its hit area follows
   the visible glyph bounds.
7. Repeat the zoom and hit checks for the multiline Text.
8. Repeat the zoom and hit checks for the LaTeX Text.
9. Confirm that the gray absolute-screen control retains its screen glyph size
   and screen position.
10. If Graphics2 is available, show the same Text there, give Graphics and
    Graphics2 different zooms, and confirm that each view derives its own pixel
    size from the same construction object.
11. In Text Properties, choose the S2 construction font size 10 pt and confirm
    that it acts as the logical/base size before zoom scaling.
12. Save and reopen the construction; confirm content, anchor, styles and
    logical font settings remain intact and an equivalent view scale gives an
    equivalent visual size.
13. Confirm that menu typography and toolbar icon sizes have not changed.

Record author disposition separately. This checklist does not claim smoke
PASS, approve S3, authorize publication, or authorize S4 and later phases.
