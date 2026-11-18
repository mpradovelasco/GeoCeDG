# JUMP-CONTEXT-SQRT — focal reconstruction boundary

Author authorization: one initial correction, at most one internal corrective
iteration and independent focal review. Base d0b2c3f4210d578e489ac78bdd5c5204628081f6
/ tree abfc0f1f55674cbe65b845a95c3ed2732e294fa4. Scope is the pending SQRT_SHORT
case only; five P2 failures and B/C/global gates remain outside.

## Demonstrated cause

A single-case reproduction records reconstruction-fidelity-not-established:
R:STRUCTURE:live=definition-shape:copy=definition-shape, compared3/restorations0.
The test manually constructs a Cartesian AlgoDependentPoint with a SQRT_SHORT
expression enclosing sin(4*pi*x(P)) on a unit segment. The XML serializer prints
both SQRT_SHORT and SQRT as sqrt(...); the parser maps sqrt to SQRT. The exact
shape comparison rejects the differing operation. captureV3 returns before
calling the program capturer. There is no captured public program to inspect.

The shape/fidelity gate predates JUMP-CONTEXT. Its refusal is not a regression of
the new scalar dispatch. The contexts are nevertheless mathematically material:
SQRT_SHORT's generic fallback evaluates the nested sine through MyDouble, while
SQRT's evaluateDouble route uses Math.sin. At the binary64 pi argument the first
can yield zero and the second a positive value. Display/serialized spelling is
not a proof of equivalence. No tolerance or exception is appropriate here.

## Correction and contracts preserved

SQRT_SHORT belongs to the scalar syntax recognized by the local program
capturer, conditional on the existing full reconstruction/identity guards for a
public LocusV2. The test incorrectly assumed that syntactic support implied a
faithfully reconstructible public slice. It must retain the public typed refusal.

Keep all original scientific assertions: the live short-root value0 and fast
sqrt(Math.sin(pi)) contrast stay in the public test, together with explicit
reparse-operation, dependency, fidelity discrepancy, failed evaluation and absent
certificate/length checks. Move the generic SCALAR_SIN/no-SCALAR_SIN_DOUBLE
assertions into a separate positive unit capture of the native expression in the
existing capture-test fixture. Check positive/zero/negative radicands and a
threshold-crossing interval there. A local typed IR is not a certificate for the
unfaithful public slice. All80 original case identities are retained; this adds
one case, not a replacement or omission.

No product source, contract, arithmetic, identity, DAG, XML, fidelity gate, metric
policy or tolerance changes. In particular do not normalize operation identity,
restore a different expression, or certify by matching displayed values. Applicable
authorities: certified-construction-model sections2/3/10.1/13.2/13.4 and semantic
contract v1.3 reconstruction fidelity. No new scientific capacity is needed.
The historical BLOCKED disposition in the preceding candidate documents remains
a record of that candidate; this unit's later disposition is recorded here.

## Focus and impact

First reproduce exclusively the failing method, with typed diagnostics. Then run
all80 JUMP-CONTEXT cases (same15 selectors, no exclusions added) plus the positive
unit case; compare case identities with previous R1 XML. Use the canonical Gradle
evidence producer. Independent review must check the assertion mapping and no
promotion of the rejected slice. No PHASE, INTEGRATION, FINAL or PACKAGING.

Layer: test fixture/validation; two test files, provenance purpose records and
this design. Bootstrap NO CHANGE: same toolchain/dependencies. GUIDE_IMPACT:
no product behavior/capability or user workflow changes; ES/EN guides unchanged.
No generated inventories or historical evidence changes.

## Status

**JUMP-CONTEXT — FOCAL VERIFIED — P2 CONSOLIDATION PENDING.**
The initial test-only correction passed81 cases, with0 failures/errors/skips.
All80 preceding case identities are present; the only added case is the native
short-root IR/domain control. No corrective implementation iteration was needed.
The independent focal review and command/XML evidence are recorded in the new
JUMP-CONTEXT-SQRT dossier; no product acceptance or global verification follows.
All production and normative source bytes remain unchanged from the protected
baseline. The previous checkpoint's historical BLOCKED records are preserved.
The five older P2 failures remain outside this unit, unchanged and unexecuted.
