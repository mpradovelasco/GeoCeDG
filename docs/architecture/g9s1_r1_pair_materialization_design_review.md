# G9S1-R1 pair materialization: preimplementation review

- Status: **DESIGN BLOCKER — PENDING AUTHOR REVIEW**
- Date: 2026-09-03
- Productive implementation: **NOT STARTED**; this change contains characterization only.
- Author authorization: G9S1-R1 investigation/implementation, subject to its stop conditions.
- No phase PASS, author approval, commit, publication or G9U1 implementation is claimed.

## 1. Verified entry and preserved authority

`HEAD = main = origin/main = direct remote main` at entry:
`109f077fc5e2a40bcde45d3271eb928ee66fdfcc`, with a clean worktree and empty index.
Its operational predecessor is `2b82034dbedf6f26250ad4aefb9eead700e33e66`;
both descend from R6 `3942af594e4507e479f2c75019cef62e3d9fea6f`.
They are accepted verification/workstation descendants, not new geometric phases.

| PASS tag | Annotated object | Peel |
|---|---|---|
| `geocedg-g9s1-pass` | `ece0ca6f00299d3347e57fac38b7a28cade28644` | `de33f3a80102adb051aaa7547a72b7e97409c58c` |
| `geocedg-g9u0-r4-pass` | `0f9b303057b00d23722ad1f9d3594b4609d668a7` | `63c291464111a5bcdbca488d6639662e46c389c4` |
| `geocedg-g9u0-r5-pass` | `3712595fe2b168ba494379b6b3f0051e4122cfae` | `5952dfdbd238e71e598f4d2ca92c3e03437df41c` |
| `geocedg-g9u0-r6-pass` | `2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e` | `3942af594e4507e479f2c75019cef62e3d9fea6f` |

Local and direct-remote tag objects/peels agree. The protected G9U1 branch
`feature/g9u1-construction-workspace-planning-after-r6` remains at
`00982e7e148a634cd57ed928f322774df267d5e3` locally, in origin tracking and
directly at origin. It is neither imported nor edited here.

The applicable authorities were inspected:
[ADR 0009](../adr/0009-locus-v2-locus-intersection-pair-semantics.md),
[ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md),
[ADR 0018](../adr/0018-semantic-spline-2d-capability.md),
[Spline V2 specification](../../geocedg/specs/curves/semantic-spline-2d.md),
[numerical-method characterization](../research/g9s1_semantic_spline_numerical_methods.md),
[ADR 0020](../adr/0020-verification-levels-and-current-run-evidence.md), and
[verification levels](../../geocedg/specs/operations/verification-levels.md).
`.github/copilot-instructions.md` is absent from this checkout; no substitute
instructions were invented.

## 2. The existing barrier is intentional and has several layers

The actual pair algorithm is `AlgoLocusLocusIntersectionV2`, not the
single-locus `AlgoLocusIntersectionV2`.

1. `PiecewisePolynomialPairIntersectionCapability2D` discovers/refines roots
   using floating Bernstein bounds and Newton steps. It deliberately supplies
   `LocalPairIsolationEvidence2D.notEstablished(...)` and no continuation key.
2. `LocusPairIntersectionSolver2D` independently checks both semantic operands,
   residuals, derivatives, revisions and components. A continuation key plus
   established isolation is necessary for an established root identity.
3. `AlgoLocusLocusIntersectionV2.publishWithLedger(...)` deliberately does not
   allocate public ledger entries for these diagnostic roots.
4. `GeoLocusIntersectionResult` requires a current validated ledger entry before
   the normal exact-token point consumer may materialize the root.

Changing one admissibility Boolean cannot implement R1. The existing durable
selector factories and address proof are single-source; pair integration also
needs both-source copy proof and a pair-parent branch in persistent-context
validation. Diagnostic span-boundary and `(u,v)` strings must never be promoted
to continuation identity. Separate result owners may legitimately have different
opaque token envelopes; caller reversal under the same owner must not change
the canonical pair selector.

## 3. Numerical certification has a bounded plausible route

For smooth owning spans, set `H(u,v) = C1(u) - C2(v)`. Its Jacobian columns are
`C1'(u)` and `-C2'(v)`. An outward-enclosed Krawczyk operator with strict interior
inclusion can prove local existence and uniqueness. This proposal is based on
the inspected theorem discussion in [Rump, section 13](https://www.tuhh.de/ti3/rump/intlab/ActaNumerica2010.pdf).
It proves a root in a specified current rectangle, not a durable label or a
complete enumeration of the whole query.

Specifically, use a fixed finite preconditioner `Y`, a center `z` in `B`, and
an outward enclosure `[J](B)` of the entire Jacobian of a continuously
differentiable `H` on a domain containing `B`. The sufficient test is
`z - Y*H(z) + (I - Y*[J](B))*(B-z)` strictly inside `B`, with every operation
enclosed. Strict inclusion under these hypotheses establishes the needed
nonsingularity as well; a sampled determinant alone is not that test.

Use original semantic coefficients, outward Horner/Jacobian evaluation and a
small finite interval type. Failed enclosure, overflow or inclusion fails closed.
Trial-box expansion may seek a successful proof; expansion itself is not proof.
Java's specified basic IEEE operations and adjacent representable bounds support
this approach ([JLS 17, section 15.4](https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.4)).
No broad dependency is needed for this numerical subproblem.

The following are not substitutes:

- Existing upstream `IntervalMultiply.next/prev` return their argument unchanged;
  that code is not an outward-rounding certificate authority.
- Rounded normalized/Bernstein coefficients cannot be wrapped after conversion
  and treated as exact enclosures of the original polynomial.
- A small residual, a nonsingular sampled Jacobian, or a small discovery box
  does not prove existence/uniqueness.
- Interval Newton is a viable alternative but additionally requires a verified
  interval linear solve. Bernstein clipping can reject boxes only if all
  conversions/restrictions are enclosed. Deflation/resultants would be broader
  work; unresolved tangencies and overlap should stay rich-only.

Two additional proof boundaries need explicit handling before implementation:

**Knots/seams.** Floating spline interpolation has an engineering backward-error
test, not exact C0/C1 glue. A smooth-span theorem cannot silently cover a
piecewise knot. A root enclosure must belong to the owning span, or an exact
boundary/glue or separately justified piecewise proof is required. Toleranced
knot snapping does not prove that condition.

**R5 transforms.** The semantic evaluator computes `T(source.evaluate(u))`.
`transformPolynomialCoefficients` derives linear coefficients through differences
such as `T(1,0)-T(0,0)` and rounded expansion. Those arrays can lose a linear
coefficient after a large translation. A certifier must enclose composition
from the actual source and transformation data, not certify that rounded
surrogate as if it were the same evaluator. This is a proof-seam requirement;
this review changes no accepted transformation behavior.

## 4. Why the straightforward pair-rank design is not sufficient

This section is a direct mathematical counterexample, not a claim that every
possible intrinsic pair-cell design is impossible.
The examples are polynomial-span models at the shared
`PiecewisePolynomialLocus2D` abstraction. They are not claimed to be byte-exact
public `SplineV2` documents or reproductions of an author fixture; the public
constructor also imposes floating interpolation/boundary constraints. They
refute a universal projection-rank shortcut at the intended shared selector
layer, not a tested failure of a newly implemented public R1 feature.

### 4.1 One projection can reorder without a pair-root collision

Consider single polynomial spans

```text
C1_t(u) = (u, t),                 u in [-2, 1]
C2(v)   = (v^2 - 1, v^3 - v),     v in [-1.25, 1.25].
```

Pair roots satisfy `v^3-v=t` and `u=v^2-1`. For
`abs(t) < 2/(3*sqrt(3))` there are three simple real roots. At `t=0`,
the two outer roots are `(u,v)=(0,-1),(0,1)` and have the same positive
oriented contact determinant `3*v^2-1=2`. They are different semantic
preimages even though their image coordinates coincide. Their pair Jacobians
are nonsingular; no pair root collides, no component changes and no seam exists.

Implicit differentiation gives `dv/dt=1/2` and `du/dt=v` at those outer roots.
Thus their first-parameter order reverses through `t=0`, while second-parameter
order remains unchanged. A first-axis rank retargets; a tuple of both projection
ranks changes selector/invalidates at a regular pair root. Switching to the
other projection only for this example is not a general symmetric rule.

### 4.2 Both projection rankings can have the same defect

Let `p(z)=z^3-z`, and take single degree-four polynomial spans on `[-1.1,1.1]`:

```text
C1(u)   = (p(u), u*p(u)/2)
C2_t(v) = (v*p(v)/2 + t, p(v)).
```

At `t=0`, all and only the nine pairs `(u,v) in {-1,0,1}^2` are roots:
the equations imply `p(u)*(1-u*v/4)=0`, and `abs(u*v)<4` on this domain.
Every root is regular, with contact determinant
`p'(u)*p'(v)*(1-u*v/4) != 0`. The four outer pairs share positive contact germ.
For `s,r in {-1,1}`, their derivatives at zero are

```text
du/dt = 1 / (2*(1-s*r/4))
dv/dt = s / (4*(1-s*r/4)).
```

The u-projection order changes within each fixed s pair, and the v-projection
order changes within each fixed r pair. All nine distinct parameter-pair roots
persist locally by the implicit-function theorem. This defeats a fixed choice
of either projection and a bare pair of projection ranks. It is not a tangent
or overlap example; constructive preimage multiplicity is retained deliberately.

### 4.3 Discovery coverage is a separate prerequisite for any rank

Even a correct local uniqueness certificate does not prove rank among all roots
in a selector domain. If a group contains `a<b<c<d`, discovering `{a,c}` versus
`{b,d}` gives the same found count but different roots in slots zero and one.
Both discovered sets can have individually valid local certificates.

A rank must therefore have its own certified selector-domain coverage, not
rank among whichever roots Newton found. If cardinality is in the selector,
the whole collision group must be accounted for. This coverage can be scoped
to a legitimate intrinsic pair-cell and leave unrelated groups unresolved;
it need not imply global completeness. The semantic cell must not be invented
from discovery-box boundaries or whichever neighbours happened to be found.

## 5. Decision required before productive token publication

The task requires regular-stratum stability, multiple roots in one span pair,
symmetry, and no numerical `(u,v)` identity. ADR 0017 decision 12 explicitly
excludes transferring its one-sided rank to pairs. The simple proposed pair
ranks above do not meet all those requirements.

Two paths remain for author review:

1. **Bounded certified-chart scope (recommended first).** Design independently
   justified symmetric semantic charts plus certified coverage; materialize only
   when that chart uniquely identifies the root. Explicitly permit conservative
   dormant/identity-transition states at unresolved projected-order/chart events,
   including some events where pair roots are still individually transverse.
   This is a proposed additional materialization limitation, not silently
   reclassified geometric singularity. Detailed chart lineage and token-reuse
   rules still need proof before code; author acceptance of the limitation alone
   does not prove an algorithm correct.
2. **Preserve the requested regular-pair-stratum contract without adding
   projection/chart events.** First develop a
   stronger intrinsic algebraic/topological pair-cell identity with a proof for
   these counterexamples, certified cell coverage, knot crossings and periodic
   monodromy. Resultant/Thom-encoding or another scheme must be evaluated rather
   than assuming its sign/ordinal labels remain stable. This is additional design
   work, not a claim that a general solution cannot exist.

No certification flag, ledger activation or partial pair materialization is
implemented while this choice is unresolved. In particular, the presence of a
Krawczyk proof would not justify a guessed selector. No weaker numerical tier,
coordinate fallback or history-based continuation is proposed.

## 6. Validation scope and operational impact

The accompanying new test is characterization of rejected identity shortcuts
and the existing transformed-coefficient seam, not R1 acceptance or a change to
historical test semantics. Validation results are recorded after execution in
[the review evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-design-review-evidence.json).

Executed DEV A/B each passed **13/13**: six new design-characterization methods
and seven unchanged historical polynomial-pair controls, with zero failures,
errors or skips. Their sorted `class#method:status` LF inventories match at
SHA-256 `dc884d7abff90cff06dcc19803f3c20dfae6b51f45299dfee35edecdd87853b2`.
This is an outcome-inventory hash, not a full R1 scientific summary or proof of
new materialization. Shared test Checkstyle also exited 0.

The first sandbox DEV attempt could not resolve existing classes and Java
reported JAR `AccessDeniedException`; the unchanged command passed under managed
execution. No source fix was made for that environment failure. The first
standalone Checkstyle attempt had a PowerShell argument-quoting error; quoting
the Java property corrected invocation only. All logs remain ignored beneath
`artifacts/g9s1-r1-design-review/`.

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
```

No verifier registration, orchestration, test-selection policy, toolchain or
dependency changes occur in this stopped review. No observable/API behavior
changes, so the user/developer guides retain their current rich-only claims.
Use existing DEV wrappers for these diagnostic tests; DEV is not acceptance.
PHASE/COMPOSED/FULL product acceptance remains NOT RUN / NOT CLAIMED. If R1
resumes product implementation and registers its new PHASE authority, that
infrastructure change requires the current ADR 0020 COMPOSED and clean-output
FULL evidence, in addition to focused deterministic A/B.

## 7. Preserved boundaries

- Productive R1 code has not started; no complete implementation candidate exists.
- G9U1 design remains author-approved at its protected checkpoint; G9U1
  implementation is unauthorized and blocked until R1 PASS plus the separately
  required design reconciliation.
- R1-side future reconciliation would remove rich-only only for genuinely
  certified, uniquely selected pair roots; the protected prompt is not edited.
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED at the
  [living roadmap](../roadmap/geocedg_roadmap.md). No pair or R6 seam evidence
  automatically resolves that distinct missing native persistence test.
- No R5, G9U1, frontend, public command, persistence, ledger or Classic semantics
  have been modified. No commits, pushes, merges or tags are authorized here.
