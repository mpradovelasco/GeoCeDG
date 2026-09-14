# PRE-G9B-S3 Text view-scaling smoke fixture

Open `pre-g9b-s3-text-view-scaling.ggb` in GeoCeDG. The construction contains
ordinary, multiline and LaTeX world-anchored Text, a nearby blue reference
segment and a gray absolute-screen Text control. The three construction Text
objects must grow and shrink with world zoom; the gray control must retain its
screen size.

The tracked `geogebra.xml` is authoritative. `generate_model.py` packages it
with deterministic ZIP metadata and does not interpret or resave the model.
Run `python generate_model.py --check` to verify the tracked GGB bytes.

This is a manual author-smoke aid. Screenshots are not normative evidence, and
the fixture does not authorize author approval or any phase after S3.
