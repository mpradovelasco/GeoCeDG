# POST-E2 planning — author decision record

```text
POST-E2 PLANNING    = PLANNING PASS — AUTHOR APPROVED
P1 CHARACTERIZATION = AUTHORIZED
P1 IMPLEMENTATION   = NOT AUTHORIZED
P2–P4 EXECUTION     = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (the POST-E2 planning document only)
passClaimed               = true    (planning only)
implementationAuthorized  = false   (P1–P4 implementation, E3, F1, F2, F3, G)
productPhaseEffect        = NONE
```

This record preserves the explicit author decision of 2026-10-08, given in
writing in the session ("Te autorizo a ejecutar el siguiente prompt", "POST-E2 —
Author Approval of Planning and Authorization of P1 Characterization"), with the
exact identities behind it. It is the sole authority for the approval of the
POST-E2 planning. Its machine-readable mirror is
[`post-e2-planning-author-decision.json`](../../geocedg/validation/post-e2/post-e2-planning-author-decision.json).

## Approved planning artifact

```text
planning document = docs/architecture/post_e2_planning_proposal.md
initial commit    = 5c4a1879f78bba7aaca8f4dbbb52b4d49ca9aad5
amended and published (P2 first-argument amendment)
                  = 694354e86971e2a6335d249dd92aca1039becf5e
                    tree b6e0dee747611fa89367f0e6af92130c197f3417 (main)
E2 baseline       = 86fdc9086d68f21ea67f360ff104679dace74223 (E2 closeout)
                    approved candidate 3a7246614a6ef040d84a1440c4c10b3f93be5776
                    tree 00b5230d19debea5598299906f846f52b4d6e9d7
```

The published document stays in Git history as the reviewed text. This
closeout amends it only with the approved P1 experiment families and the status
lines.

## Scope of the approval

Approved: the four independent workstreams (P1 dimension gap/overshoot and
physical units; P2 unexpected zoom during Algebra Input; P3 interactive access
to document macros; P4 dragging typed native dimensions), the execution order
`P1 → P2 → P3 → P4`, the characterization-first method, the proposed
architectural boundaries, the requirement of a separate implementation
authorization, and the retention of the `E2` geometric, numerical and
persistence contracts.

Not approved: the preliminary root-cause hypotheses stay hypotheses, and the
design options stay candidates, not normative implementation decisions.

## Required P1 amendment (incorporated)

- Experiment A, identical numerical geometry: `A=(0,0)`, `B=(40,0)` under
  `mm`, `cm` and `m`.
- Experiment B, physically equivalent geometry: 40 mm as `B=(40,0)` (`mm`),
  `B=(4,0)` (`cm`), `B=(0.04,0)` (`m`); paper-space equivalence of `Gap` and
  `Overshoot`; screen comparisons under a normalized viewport, never raw pixels
  per model unit.
- `1:1` and `1:10` drawing scales and three zoom levels kept.

## Authorization of P1 characterization

```text
PHASE              = POST-E2-P1
ACTIVITY           = CHARACTERIZATION ONLY
VERIFICATION CLASS = BOUNDED_PHASE
ACCEPTANCE         = PHASE
PRODUCT            = NOT AUTHORIZED (no native-dimension change; scratch probes and
                     non-product diagnostic instrumentation authorized)
PUBLICATION        = planning closeout only; the P1 candidate is not pushed or merged
```

P1 starts from the published planning-closeout commit, on an isolated local
branch, and stops for author review.
