# PRE-G9B-R6-plus-A-1 preparation package closeout record

```text
PRE-G9B-R6-plus-A-1 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-A-1                     = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (A-1 documentary preparation package only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = false   (A-1, A-2 and every later subphase)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-02 on the
documentary preparation package of `PRE-G9B-R6-plus-A-1` and the exact
identities behind it. The package is not a phase: approving it closes the
preparation, not `A-1`, and authorizes no implementation. This record changes
no product, test, build, packaging, verifier, registry, schema, specification,
ADR or serialization file, and it does not modify the frozen candidate.
Besides this record and its machine-readable mirror,
[`pre-g9b-r6-plus-a1-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a1-prompt-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

## Author disposition

```text
T_R6PLUS_A1_PROMPT = 3c5aba19b2efd4b65d6f7b7deba6d3b19611630b
AUTHOR_APPROVAL    = APPROVED
selfApproved       = false
```

The author instruction of 2026-10-02 approves the frozen candidate and
authorizes exclusively the documentary closeout and the publication of this
candidate under current governance. The approved package is:

- the canonical prompt
  [`pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md),
  which stays `PREPARED — NOT AUTHORIZED` with `implementationAuthorized = false`;
- the versioned [A author-decision record](pre_g9b_r6_plus_a_author_decisions_record.md)
  (`AQ-L1a`, `AQ-L1b`, `AQ-L1c`, `AQ-L2`, `AQ-L3`, `AQ-L4`, `AQ-L5`, `AQ-L7`,
  `AQ-L8`) and its machine-readable mirror
  [`pre-g9b-r6-plus-a-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a-author-decisions.json);
- the roadmap 4.37 and mini-track plan updates for the split of `A`.

The frozen candidate is not amended; governance does not require it. Its
prompt and decision record carry no author-approval field, so nothing in them
became stale.

## Author dispositions on the two open points

Recorded here as the author stated them; they refine how the approved package
is read and do not change its content.

1. **Historical `A` row.** The former `A` row of the plan §12 may be kept only
   as a historical record, and only if it is unequivocally marked
   "superseded by `A-1`/`A-2`" and cannot be mistaken for a current executable
   phase. This closeout relabels that row, and the historical `A` brief in plan
   §3.2, accordingly. The roadmap kept no `A` row.
2. **`D0` scheduling.** `D0` is technically independent and may be scheduled in
   parallel with `A-1` or `B`. The order
   `A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G` is the operational
   order currently chosen by the author, not an artificial dependency of `D0`.
   The `ACCEPTED ORDER` of the decision record and the `acceptedOrder` of its
   mirror are read in this sense. This closeout states it in the plan §3.1 and
   in the roadmap.

## Approved identities

```text
P_R6PLUS_P0        = 562e2bb1e77b249b9c97c2e6e06e8b123d6de900
                     tree 9f578093f2f3cba481f5630fe827fd3ef08cff61
                     published P0 closeout; base of the package and of A-1

T_R6PLUS_A1_PROMPT = 3c5aba19b2efd4b65d6f7b7deba6d3b19611630b
                     tree 2b6242d805420fad0c48c7ca8c8f338219e4c78c
                     parent 562e2bb1e77b249b9c97c2e6e06e8b123d6de900
                     approved documentary candidate; not amended

branch             = phase/pre-g9b-r6-plus-a1-prompt (local; never pushed)
```

The identity of this closeout commit, `P_R6PLUS_A1_PROMPT`, is its own commit,
which the record cannot name. It is reported with the publication.

## Accepted evidence

| Commit | Run | Result |
|---|---|---|
| `T_R6PLUS_A1_PROMPT` `3c5aba19…` | `verification-21a72bb8d7004c31ade6efe78d7d3860` | `STATIC`; exit 0; `ACCEPTED / COMPLETE`; 3/3 (`prompt.execution-safety`, `repository.boundary-safety`, `static.scientific-inputs.semantic`); plan hash `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`; result hash `410a2638b56630b382a516bd9e97f1360f66adb23d8b2912052243d9df85d234` |

The run reported the two standing diagnostics present on every `STATIC` run,
`diagnostic.governance` (`DIAGNOSTIC_FINDING`) and
`diagnostic.historical-consistency` (`DIAGNOSTIC_UNAVAILABLE`); the other four
were clear. `git diff --check` on the candidate: exit 0. The canonical A-1
prompt validated with `Test-PromptContractDocument` (`task` profile,
`execution_safe = true`). `STATIC` is not `FINAL`; no receipt is expected or
claimed. This closeout commit's own `STATIC` run is reported with the
publication.

## Status updates in this closeout

- Roadmap 4.38: the preparation package is recorded as `PASS — AUTHOR
  APPROVED` in the next-gate entry, the track paragraph and the `A-1` row;
  the `D0` scheduling disposition is stated with the order. `A-1` and `A-2`
  stay `NOT AUTHORIZED`. The last closed phase is unchanged, because the
  package is not a phase.
- Mini-track plan: the header line for `A`, the `D0` sentence of §3.1, the
  historical `A` brief of §3.2, the historical `A` row of §12 and the §14
  `A-1` state.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_P0           562e2bb1e77b249b9c97c2e6e06e8b123d6de900
  -> T_R6PLUS_A1_PROMPT 3c5aba19b2efd4b65d6f7b7deba6d3b19611630b
  -> P_R6PLUS_A1_PROMPT (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_P0` immediately
before the push. Forbidden: merge commits, rebases, squashes, amends, force
pushes, tags, releases, binary publication and any product implementation.

Once published, `P_R6PLUS_A1_PROMPT` is the natural implementation base for
`A-1`. As the prompt states, the instruction that authorizes `A-1` names the
exact base; nothing here fixes it.

## Authorization state

```text
PRE-G9B-R6-plus PLANNING   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
A-1 preparation package    = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-A-1        = NOT AUTHORIZED   (next subphase; implementationAuthorized = false)
PRE-G9B-R6-plus-A-2        = NOT AUTHORIZED   (AQ-L7 Open × persistent hidden layers first)
PRE-G9B-R6-plus-B, D0, D1, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No tag or release is authorized.
