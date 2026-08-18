# G9A2 canonical spatial-point model

This package contains the newly authored deterministic G9A2 point-pilot model.
It is not a legacy or label-migrated CeDG construction.

`geogebra.xml` is the reviewed source. It defines three explicit orthographic
frames, three typed intrinsic-to-diagram maps, an oriented hinge, an auxiliary
change-of-plane relation and two defining point projections. The expected
projection-defined spatial point is `(2, 3, 5)`. Every association is carried
by a G9A1 durable ID; labels are ordinary host metadata only.

Run `python generate_model.py --check` to prove that the tracked GGB is exactly
the deterministic package of the tracked XML bytes. The manifest records the
GGB SHA-256 and both model catalogs register the case. Focused `G9A2*` tests
perform real host loading and certificate recomputation.

`generate_model.py` fixes ZIP entry order, timestamp, platform metadata and
compression. It packages bytes only and never loads, solves or resaves the
construction. The final `.ggb` is durable validation source, not ignored build
output and not a general primitive or later-phase product fixture.
