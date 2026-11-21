# Post-R1 locus validation inputs

Independent mathematical reference for the regression package
`models/regression/post-r1-locus-on-intersection`. Nothing here is GeoCeDG geometric
authority: the kernel construction is the authority, and this reference is an external
oracle that the regression tests compare it with.

| File | Content | SHA-256 |
|---|---|---|
| `oracle/geometry.js` | Independent reconstruction of the oblique cone and cylinder with a common base plane, of their intersection in elevation and plan, and of the development of the cone through the spherical support curve. Unchanged copy of the script of revision R1 of the locus-on-intersection diagnosis (2026-11-21). It uses no GeoCeDG code and no kernel graph. | `e2bd707ee3bdaf9e40ca41adec8092b608248895aae21bee281a2ac2a7d74ad7` |
| `oracle/model-inputs.json` | The saved free parameters and the few saved coordinates that the script reads, extracted from the construction XML of the author model (SHA-256 `949c2c93…f98159b`): 22 points, two radii, two saved numbers and one line. | `7ae040d9b412b79ef9b4f65e7967ea8e75c710b893e39b2b8eb68d65aa8f757a` |
| `transport-reference.tsv` | Essential contrast data: the developed contour of the hole of the finite cone (family `R`, the far root of `Ray(P, N)` with the base circle) at 73 parameters of the driver `N` (every 5 degrees, the last equal to the first), and the conjugate image that the author's original procedure also draws. Columns: `thetaN`, `devX`, `devY`, `devConjX`, `devConjY`. Every tenth row of family `R` of the script output `r1-families.tsv`. | `6fe9bde6c58f6e2a9556d7df07e0bf33f00e4588eda2b599534229a8df13f481` |

## Regeneration

Node.js is needed only to regenerate the reference; no verification profile runs it.

```powershell
node geocedg/validation/post-r1-locus/oracle/geometry.js geocedg/validation/post-r1-locus/oracle/model-inputs.json <new empty output folder>
```

The script writes `r1-families.tsv` and its other tables to the output folder. With these
inputs it reproduces byte for byte the tables of revision R1 (`r1-families.tsv` SHA-256
`9de4f5491c08f52fedd16402cbb40ad277acc2d581c570af8d8cd62d3793eb2e`); the reference above
keeps the rows of family `R` whose index is a multiple of ten.

## What the comparison establishes

`PostR1AuthorModelRegressionTest` evaluates the transported contour `trHole` of the derived
variant at the 72 distinct parameters and requires a distance to the reference of at most
`1e-6` model units (measured: `1.5e-8`), and a distance to the conjugate image above one
unit (measured: `2.8`). The tolerance is that of the estimated oriented length that gives the
developed angle, on a development of radius about 14; it is not a geometric certificate.

The agreement is pointwise agreement of a faithful evaluation with an independent formula at
the sampled parameters, for the saved configuration. It does not establish the existence
structure, the continuity or the completeness of the locus, which the kernel reports as
`NOT_ESTABLISHED`.
