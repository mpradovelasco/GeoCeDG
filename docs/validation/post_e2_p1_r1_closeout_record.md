# POST-E2-P1-R1 closeout record

```text
POST-E2-P1-R1 = PASS — AUTHOR APPROVED   (characterization and evidence; no product
                                         correction)
```

```text
selfApproved              = false
authorApproved            = true    (the P1-R1 characterization only)
passClaimed               = true    (P1-R1 characterization only)
implementationAuthorized  = P1-R2 design and bounded implementation (separate phase,
                            author approval pending); P2–P4, TD-PDF-1, TD-PDF-2, E3,
                            F1, F2, F3, G, G9B NOT AUTHORIZED
candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE
```

This record preserves the explicit author decision of 2026-10-08, given in
writing in the session ("Ejecuta la siguiente tarea", "POST-E2-P1-R1 / P1-R2 —
Author Approval, Publication, Physical PDF Stroke Design and Bounded
Implementation"), with the exact identities behind it. It is the sole authority
for the approval of `POST-E2-P1-R1`. Mirror:
[`post-e2-p1-r1-closeout.json`](../../geocedg/validation/post-e2/post-e2-p1-r1-closeout.json).
The [characterization report](post_e2_p1_r1_characterization_report.md), its
evidence, measurements and raster cross-check are immutable historical evidence
and are not amended.

## Entry verification

```text
live remote main = origin/main = local main
                 = 64d25ba88703372c7c83e1c2fbdd59cbdc4d9aaa (P1 closeout)
branch           = phase/post-e2-p1-r1-pdf-stroke-width, clean
history          = 64d25ba8 → 2237df0a (characterization candidate); linear
```

## Author decision

```text
POST-E2-P1-R1 candidate = 2237df0aad20eea9ad38831c9a5ad2bbf2d5f345
                          tree e3526e3323bac94e825c52853b9dfc35641b847d
STATIC                  = verification-d7057531ab114ede8d964c23c7c3e2e2, ACCEPTED / COMPLETE, 3/3
PHASE                   = verification-34bb6e57a6d94d439c86e3181aa03b4a, ACCEPTED / COMPLETE, 3/3
AUTHOR_APPROVAL         = POST-E2-P1-R1 = PASS — AUTHOR APPROVED
```

Accepted findings:

```text
REAL PDF STROKE-WIDTH VARIATION
UPSTREAM-INHERITED BEHAVIOR
PHYSICAL-OUTPUT CONTRACT GAP
GEOMETRIC SEMANTICS UNAFFECTED
```

- The physical PDF line width changes with the view zoom at export, confirmed by
  PDF graphics-state analysis and independent rasterization.
- Mechanism inherited from GeoGebra: `screenStrokeWidth = lineThickness / 2`,
  `pdfPointsPerPixel = printingScale · 72 / (2.54 · xscale)`; the geometry
  compensates, style sizes do not.
- The physical-output specification governs geometric dimensions only.
- Confirmed for segments and lines, circles, dimension extension lines, the
  filled dimension-line body, dimension arrowheads and point markers; other
  curves, polygon edges and dash patterns to be covered by the corrective
  verification; text follows another rule and is excluded.

## Registered technical debts (deferred)

```text
OBS-POST-E2-P1-R1-PDF-TEXT-SIZE  (TD-PDF-1)
    = OPEN — CHARACTERIZATION REQUIRED — NOT AUTHORIZED
      ordinary and dimension text very small in physical PDFs (apparent glyph
      heights 0.23–0.37 mm); another sizing mechanism; nominal font size, glyph
      geometry, metrics, PDF transforms and ink bounds to be distinguished

OBS-POST-E2-P1-R1-PDF-EMBEDDED-FONTS-FAILS  (TD-PDF-2)
    = OPEN — UNCONFIRMED IN PRODUCT — HEADLESS TEST ENVIRONMENT OBSERVATION —
      NOT AUTHORIZED
      FreeHEP StackOverflowError (product route) and NullPointerException (Classic
      route) in the headless harness; headless and windowed behavior to be
      compared under controlled font-embedding conditions first
```

## Planned continuation (planning decision only)

```text
1. POST-E2-P1-R2  physical PDF stroke-width correction (authorized: design and
                  bounded implementation; author approval and publication pending)
2. POST-E2-P2     unexpected zoom during Algebra Input
3. POST-E2-P3     interactive document-macro access
4. POST-E2-P4     typed-dimension drag enhancement
5. PDF debts      TD-PDF-1 text size, TD-PDF-2 embedded fonts
```

Each later activity needs its own scope verification and author authorization;
P2 does not start automatically after R2, and the PDF debts are not treated
during P2–P4 unless the author changes the sequence.

## Closeout verification

Documentary closeout (record, mirror, planning and roadmap status); `STATIC` is
run on the closeout commit, which is reported outside this record with the
publication.
