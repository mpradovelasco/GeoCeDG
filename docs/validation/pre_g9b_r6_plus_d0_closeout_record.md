# PRE-G9B-R6-plus-D0 closeout record

```text
PRE-G9B-R6-plus-D0 = PASS — AUTHOR APPROVED
```

```text
selfApproved                          = false
authorApproved                        = true    (D0 only)
passClaimed                           = true    (D0 only)
implementationAuthorized              = false   (D1, A-2, C, E1, E2, E3, F1, F2, G,
                                                 PRE-G9B-R7, G9B)

candidateFrozen                       = true
candidateMutated                      = false
PRODUCT CHANGE                        = NONE
SERIALIZATION IMPLEMENTATION CHANGE   = NONE
newHeavyIntegration                   = NONE
newHeavyFinal                         = NONE
```

This record preserves the explicit author disposition of 2026-10-02 on
`PRE-G9B-R6-plus-D0` and the exact identities behind it. It is the sole
authority for the approval of `D0`. It changes no product, test, build,
packaging, verifier, registry, schema or serialization file and does not modify
the frozen candidate. Besides this record and its machine-readable mirror,
[`pre-g9b-r6-plus-d0-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-closeout.json),
it only:

- promotes the status lines of
  [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md)
  and of the [unit-system specification](../../geocedg/specs/units/unit-system.md)
  v1.0, whose decision text and clauses are unchanged;
- updates the status wording of the pointer note in the
  [geometry export foundation](../../geocedg/specs/export/geometry-export-foundation.md)
  and of the supersession note in the mini-track plan §4.4;
- updates status statements in the living
  [roadmap](../roadmap/geocedg_roadmap.md) and
  [mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_d0_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. They are not amended.

## Author disposition

```text
PRE-G9B-R6-plus-D0 = PASS — AUTHOR APPROVED
T_R6PLUS_D0        = 8383a153dc2228fc08b4e00a0c92ef8025608916
                     tree 442e27fcafea223af426b4ee65102a8a5d90be8c
selfApproved       = false
```

The author accepts as the normative authorities of `D0`:

- [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md),
  unit-system semantics and persistence ownership;
- [`geocedg/specs/units/unit-system.md`](../../geocedg/specs/units/unit-system.md) v1.0;
- the author dispositions `AQ-U1` to `AQ-U6`
  ([D0 author-decision record](pre_g9b_r6_plus_d0_author_decisions_record.md));
- the dispositions `DQ-D0-1` to `DQ-D0-13`
  ([closeout and authorization record](pre_g9b_r6_plus_d0_prompt_closeout_record.md));
- the matrices and normative contracts incorporated coherently in those
  sources.

The recommendations and additional decisions of the candidate report acquire
**no independent authority** by appearing in the report. They are approved
only where they are reflected coherently in the normative authorities above or
are strict consequences of them.

### Accepted design points

The author also accepts, each already carried by the specification:

| Point | Clause |
|---|---|
| an unreferenced `usm` definition may be removed explicitly, never while `constructionUnit` or `presentationUnit` selects `usm` | §4.3 |
| a `presentationUnit` user preference cannot be `usm`, because `usm` needs a document-scoped physical definition | §10 |
| the concrete DXF codes for `mm`, `cm` and `m` are verified again by `C` before it changes the contract or the exporter | §15.2 |

### Disposition of the pending findings

These bind the future phase named; they add no normative rule to `D0`.

| # | Finding | Disposition |
|---|---|---|
| 1 | fail-closed restore and the window state after a failed File → Open | mandatory characterization and test in `D1` |
| 2 | determinism of the binary64 factor across the supported JDKs | mandatory characterization and test in `D1`; if `Double.toString` does not produce identical canonical bytes on the supported corpus, `D1` provides a deterministic representation compatible with the `D0` contract (specification §8.4) |
| 3 | unit UI, `usm` definition, preferences and paste notice | `D1` |
| 4 | owner of `drawingScale` | `C`, not `D1` (the conditional `D1` clauses of specification §18.1 and §19.1 therefore do not apply) |
| 5 | clipboard between two windows | mandatory probe in `D1` |
| 6 | possible reclassification of `C` because of the G9X1 verifier change | decided when `C` is prepared |
| 7 | forward-compatibility warning in user documentation | mandatory backlog item for the integrated closeout `G`; does not block `D1` |

## Approved identities

```text
P_R6PLUS_B          = 0ef616bb7bb8efd3e4f19762f38936ff4742d1c3
                      tree be7ef7acbc3503002f3dac2bc33f047795f7a644
                      published B closeout; parent of the D0 chain

T_R6PLUS_D0_PROMPT  = 8814468101e3caef45d9f6cbad4bf563523c3478
                      tree edbe623604330e62db11ff3d3b05b7d73671dbd0
                      approved preparation package

A_R6PLUS_D0         = 804e20c08f8d25419799ed10b780483fa1670920
                      tree 63c7cfa07f892f0dbcb7cf09a53539ca4c32b807
                      authorization edit (prompt blob b6025b8c088e6dd1f0bd69a2d311d794ed882c14)

T_R6PLUS_D0         = 8383a153dc2228fc08b4e00a0c92ef8025608916
                      tree 442e27fcafea223af426b4ee65102a8a5d90be8c
                      parent 804e20c08f8d25419799ed10b780483fa1670920
                      approved documentary candidate; not amended

branch              = phase/pre-g9b-r6-plus-d0-prompt (local until publication)
```

The identity of this closeout commit, `P_R6PLUS_D0`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `DOCUMENTATION_STATUS_ONLY` required `STATIC` on the exact
candidate.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-1e336856b96b41b5b0dd41ae58556af0` — `verify.ps1 -Profile STATIC` (external log root) | `8383a153…` / `442e27fc…` | exit 0; `ACCEPTED / COMPLETE`; 3/3 (`prompt.execution-safety`, `repository.boundary-safety`, `static.scientific-inputs.semantic`); plan `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`; result `b4a7d5389bcc7a42a44025f868e9d28ebda06277dfe820bfa3dd8e4797dd499f`; standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) |

`git diff --check` over `8814468…..8383a153…`: exit 0. No `PHASE`,
`INTEGRATION` or `FINAL` run was required or performed; no receipt is expected
or claimed. This closeout commit's own `STATIC` run is reported with the
publication.

## Product and serialization

```text
PRODUCT CHANGE                      = NONE
SERIALIZATION IMPLEMENTATION CHANGE = NONE
```

The `D0` chain changes no path under `source/`, `apps/`, `packaging/`,
`tools/` or `geocedg/specs/operations/`. The persistence element
`geocedgUnits` is specified, not implemented; no reader, writer, GUI, status
bar, copy/paste, exporter or verifier changed. The G9X1 DXF fidelity contract
and the G5 rules of the export foundation stay in force unchanged until `C`.

## Status updates in this closeout

- ADR 0032: `ACCEPTED — AUTHOR APPROVED`; unit-system v1.0:
  `NORMATIVE / AUTHOR APPROVED`, implementation not authorized.
- Roadmap 4.47: `PRE-G9B-R6-plus-D0 = PASS — AUTHOR APPROVED` in the last
  closed and last executed phase entries, the next-gate entry, the track
  paragraph and the `D0` row; `D1` marked as the next operational subphase,
  `NOT AUTHORIZED`.
- Mini-track plan: the `D0` state in the header, the §3 table and §14; `D1`
  marked as the next operational subphase; the §4.4 note status.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_B          0ef616bb7bb8efd3e4f19762f38936ff4742d1c3
  -> T_R6PLUS_D0_PROMPT 8814468101e3caef45d9f6cbad4bf563523c3478
  -> A_R6PLUS_D0        804e20c08f8d25419799ed10b780483fa1670920
  -> T_R6PLUS_D0        8383a153dc2228fc08b4e00a0c92ef8025608916
  -> P_R6PLUS_D0        (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_B` immediately before
the push. Forbidden: merge commits, rebases, squashes, amends, force pushes,
tags, releases, binary publication and any implementation.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0         = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-D1         = NOT AUTHORIZED   (next operational subphase)
PRE-G9B-R6-plus-A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

This authorization ends with the publication of `D0`. `D1` needs its own
explicit author instruction naming its exact base. No tag or release is
authorized.
