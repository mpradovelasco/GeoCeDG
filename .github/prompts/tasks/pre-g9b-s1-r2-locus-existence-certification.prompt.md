# PRE-G9B-S1-R2 — Locus V2 existence/component certification

## Status and authority

This is a **candidate implementation prompt**. It does not authorize product
implementation. Execution requires a later explicit author decision over the
exact approved R2 design commit.

The design entry is the unpublished R1 partial candidate
`e5260fc7dd4924f0599ac3ac2700025bf5e48330`, descended from characterization
`48312ceb070b014b68218b3ca5f5c09249c62748`. Follow `AGENTS.md`,
[proposed ADR 0027](../../../docs/adr/0027-locus-v2-existence-and-continuous-valid-components.md),
the [R2 architecture](../../../docs/architecture/pre_g9b_s1_r2_locus_existence_components_design.md),
the [normative candidate](../../../geocedg/specs/locus/locus-v2-existence-components.md)
and the [validation matrix](../../../docs/validation/pre_g9b_s1_r2_locus_existence_validation_matrix.md).

`PRE-G9B-S1-R1` remains a partial implementation and its typed DXF population
must be preserved. S2, S3, S4, D1, P1, G9B, G9C, G9U2, further G12 and
productive G10 are outside scope.

## Objective

Implement the smallest shared-kernel contract that distinguishes canonical
driver domain, existence coverage, certified continuous-valid components and
global decomposition completeness. Use the author fixture
`models/regression/pre-g9b-s1-dxf/original/TestExport1.cedg` and locus `m` as the
mandatory witness.

Do not guess `m` boundaries or component count. Its known evaluations disprove
whole-domain validity but do not prove the remaining intervals.

## Required architecture

Use the approved hybrid design:

```text
generator/provider structural facts
  + selected-root/dependency interval evidence
  -> shared revision-bound Locus certifier
  -> one immutable existence/component/completeness structure
```

Preserve the normal Construction DAG. Existing root tokens/selectors remain
identity authority. Add only interval-wide proof that the same selected root is
valid, unique and continuously identifiable. Never select by coordinates,
proximity, solver order, component order, render samples, viewport, zoom or DPI.

## Required implementation order

1. Add immutable coverage, continuous-component, boundary, completeness and
   revision/currentness values.
2. Lift direct providers to `COMPLETE` only when current contracts prove
   interval-wide validity. Separate periodic canonical-domain validation from
   dependent existence.
3. Keep dependent producers `NOT_ESTABLISHED` until interval evidence exists.
4. Add the selected-root interval capability at the root-owning semantic seam;
   the dependent point and exporter must not solve roots.
5. Migrate every consumer to an explicit local/global admissibility rule before
   exposing local components through public/shared APIs.
6. Make G9X1 approximate certified finite components independently and record
   global incompleteness in preflight/sidecar. Never bridge a gap.
7. Preserve the R1 typed geometric complete-construction population and strict
   Current selection behavior.

`getValidDomainComponents()` must not silently change from complete-domain
authority to a partial list. Retain it as a complete-only compatibility view
until all consumers are migrated.

## Local versus global rules

- Rendering may display certified local components without claiming completion.
- Component-local metrics may succeed; total locus metrics require complete
  decomposition.
- Locally admissible intersections may remain usable; empty/all-roots claims
  require domain plus solver completeness.
- Exact current point addresses may operate within a certified component;
  global nearest/route inference requires sufficient complete coverage.
- Nested/transform producers propagate incompleteness.
- DXF local-component publication requires explicit globally-incomplete
  fidelity and a mandatory sidecar; it may not be called the complete locus.

## Lifecycle and compatibility

Keep Locus/root persistent identities, branch keys, revisions, currentness,
G9A redefine/copy/remap semantics and old-file behavior. Dynamic continuous
components are revision-local unless explicit lineage is proved. Temporary
invalidity allocates no new persistent identity. Save/reopen and undo/redo
recompute certificates from durable inputs and versioned policies; sampled
caches are never persisted authority.

## Stop conditions

Stop and report before broadening scope if:

- `m` needs a general redesign of intersection/root identity;
- interval boundaries cannot be certified without point/render sampling as
  authority;
- deterministic bounded certification cannot fail as `NOT_ESTABLISHED`;
- local consumers cannot preserve the global completeness axis; or
- implementation would duplicate another current semantic authority.

Do not work around a stop by fabricating a component or weakening strict DXF.

## Validation

Implement every applicable row in the R2 validation matrix. Run focused tests,
checkstyle and `git diff --check`; add the minimal `PRE-G9B-S1-R2` phase
selection and run `INFRA_UNIT` only if executable inventory changes. Run exactly
one final `PHASE -Phase PRE-G9B-S1-R2`, then exactly one combined
`PHASE -Phase PRE-G9B-S1` before author smoke. Do not run `INTEGRATION` or
`FINAL` without a concrete governance escalation.

Create one clean local candidate. Do not publish, tag, self-approve, begin S2,
or claim phase PASS.
