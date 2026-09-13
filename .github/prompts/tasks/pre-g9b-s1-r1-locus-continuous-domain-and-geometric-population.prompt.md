# PRE-G9B-S1-R1 — continuous Locus domains and geometric DXF population

## Authority and scope

This is the canonical bounded refinement of `PRE-G9B-S1`. Its entry is the
local S1 characterization commit
`48312ceb070b014b68218b3ca5f5c09249c62748`, descended from the unpublished S1
implementation candidate `1221e5c5b3bb20f9ad65e7f36ac58f624beef3c0`.
Follow `AGENTS.md`, the approved G6 Locus V2 semantic contract, G5, G9X1 and the
[S1 characterization](../../../docs/architecture/pre_g9b_s1_periodic_locus_and_complete_population_characterization.md).

The author approves two directions: semantic Locus V2 export must consume
separate, finite, continuously valid components, and complete-construction DXF
must use a typed geometric population. This does not authorize sampling to
invent semantic validity, root substitution, partial output, DXF `TEXT`, native
DXF `SPLINE`, S2–P1, G9B/G9C/G9U2, further G12 or productive G10.

## Semantic stop condition

`LocusBranch2D.getValidDomainComponents()` is shared kernel authority consumed
by export, metrics and intersections. If the current producer cannot establish
global continuous validity and its boundaries without new root-continuation or
interval-certification semantics, stop the Locus subproblem. Point sampling,
render tessellation, viewport state and coordinate proximity cannot certify a
component. Report the missing kernel contract; do not create an export-local
definition of Locus validity.

## Typed population

For `COMPLETE_CONSTRUCTION`, exclude authorized non-geometric families before
strict preflight using deterministic type rules. Lists, numeric parameters,
rich Locus intersection results and Text without an approved DXF mapping are
outside population. Current selection is never filtered. Unknown or genuinely
geometric unsupported families remain eligible and therefore block the strict
request. Report the population rule and every exclusion distinctly from an
unsupported or invalid eligible source.

## Verification and boundary

Use focused G5/G9X1 shared and Desktop tests, the author fixture, checkstyle and
`git diff --check`. Register and run an R1 PHASE only after both workstreams are
truthfully complete. Before S1 smoke, the combined immutable candidate must run
exactly one `PHASE -Phase PRE-G9B-S1`. Do not publish, tag or self-approve.
