# PRE-G9B-R6-plus-D1 author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS (preparation instruction)
DECISION_DATE            = 2026-10-02
DECIDED_ON_BASE          = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c
                           tree 83b32b6cb579dc28162f990160c3eef0b1928be6
                           (P_R6PLUS_D0, published D0 closeout)

selfApproved             = false
authorApproved           = false   (no D1 candidate exists)
implementationAuthorized = false   (D1 and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the decisions that the author's instruction
of 2026-10-02 fixed for `PRE-G9B-R6-plus-D1`. It is the authority for them.
Its machine-readable mirror is
[`pre-g9b-r6-plus-d1-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-author-decisions.json).

The instruction authorized only the documentary preparation of the canonical
`D1` prompt, a characterization report and its mirror, this record and the
status updates they strictly require. It stated that it **does not authorize
the implementation of `D1`**: execution needs a further explicit author
authorization that names the exact prepared candidate or base. The instruction
opens no unit semantics: the `D0` authorities below govern, and this record
does not restate them.

## Normative authorities and precedence

The author named as the principal normative authorities for `D1`:

- [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md),
  `ACCEPTED — AUTHOR APPROVED`;
- [`geocedg/specs/units/unit-system.md`](../../geocedg/specs/units/unit-system.md)
  v1.0, `NORMATIVE / AUTHOR APPROVED`;
- the [D0 closeout record](pre_g9b_r6_plus_d0_closeout_record.md);
- the `D0` author-decision records
  ([`AQ-U2`–`AQ-U6`](pre_g9b_r6_plus_d0_author_decisions_record.md),
  [`DQ-D0-1`–`DQ-D0-13`](pre_g9b_r6_plus_d0_prompt_closeout_record.md));
- live code, tests, build and serialization at the exact base.

The specification controls over the `P0` recommendations and over older
planning text wherever they differ.

## Verification class frozen for `D1`

```text
VERIFICATION_CLASS  = GLOBAL_IMPACT
PLANNED_ACCEPTANCE  = FINAL on one exact frozen candidate
FINAL during preparation = not run
```

The class is not downgraded because focal tests pass. Before `FINAL`, all
applicable focal and integration evidence is required.

## Product boundary

`D1` implements only the slice the `D0` consumer contract (specification §18.1)
and the `D0` closeout assign to it:

1. shared document-level unit state;
2. persistence, parser and writer for `<geocedgUnits>`;
3. undo, redo, redefine and rollback behavior;
4. fail-closed handling of recognized invalid or future unit metadata;
5. new-document unit defaults and preferences;
6. the document-scoped `usm` definition and lifecycle;
7. copy/paste unit provenance and the non-blocking mismatch notice;
8. GeoCeDG status integration for the construction and presentation units;
9. the user-facing unit configuration needed to operate the above;
10. deterministic tests and global compatibility evidence.

`D1` does not implement, and no opportunistic work anticipates:

| Excluded | Owner |
|---|---|
| `drawingScale` holder | `C` |
| engineering-scale export | `C` |
| DXF `$INSUNITS` changes | `C` |
| G9X1 specification or verifier changes | `C` |
| native dimensions | `E2` |
| `IsoABorder` | `E3` |
| layer-domain widening and persistence | `A-2` |
| GGT library | `E1` |
| orientation cue | `F1` |
| authoring memory | `F2` |

Because `C` owns `drawingScale`, the conditional `D1` clauses of specification
§18.1 and §19.1 that depend on a `D1` holder do not apply (`D0` closeout,
finding 4).

## Architecture rule

The kernel/shared versus Desktop decision is made before any implementation
path is proposed:

- the shared kernel-document layer owns the unit semantic state, the
  effective-unit function, persistence, lifecycle, the undo-visible document
  state and the canonical conversions and factors;
- Desktop owns only the configuration UI, the portable user defaults, the
  status presentation and the orchestration of the non-blocking paste notice;
- no geometry algorithm reads units during `compute()`;
- no second semantic unit graph exists, and the state is not duplicated in
  Desktop or Python.

## Mandatory characterization before authorization

The preparation must characterize, from live code and scratch-only probes,
and the canonical prompt must encode: the state owner and its lifecycle; the
XML persistence seams; the fail-closed transaction and restore; undo, redo and
redefine; the canonical binary64 representation on the supported JDKs; the
preference mechanism and New/Open behavior; the `usm` UI and lifecycle; status
integration; clipboard provenance including two windows; the
unit-independence invariant; legacy and forward compatibility. The `D0`
closeout makes the fail-closed restore, the binary64 determinism and the
two-window clipboard probe mandatory.

If the existing load architecture cannot support fail-closed unit parsing
without material broadening, the future implementation stops and reports
before changing product.

## Stop conditions fixed by the author

The future `D1` implementation stops and asks for author review if
characterization or implementation shows that correctness requires:

- serializing units outside the `D0`-approved construction element;
- changing document geometry or coordinates on unit changes;
- making units live dependencies of general algorithms;
- embedding `drawingScale` in document serialization;
- changing `C`-owned exporters, specifications or verifiers;
- using `geocedgSpatial` as unit storage;
- silently accepting malformed or future recognized unit metadata;
- scaling geometry during paste;
- introducing a general multi-custom-unit registry;
- changing Classic/Web behavior beyond upstream-identical shared defaults;
- changing the normative `D0` contract.

## Preparation status required

```text
PRE-G9B-R6-plus-D1       = PREPARED — NOT AUTHORIZED
implementationAuthorized = false
selfApproved             = false
authorApproved           = false
passClaimed              = false
```

## What stays open

- The decisions requested by the canonical
  [`D1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md)
  under *Decisions requested before authorization*. The prompt fixes a default
  contract for each; none is an author decision until the author makes it.
- The `D0` closeout findings owned elsewhere: the forward-compatibility
  warning in user documentation (`G`, finding 7) and the possible
  reclassification of `C` (finding 6).

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B    = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D1   = PREPARED — NOT AUTHORIZED   (next operational subphase;
                       canonical prompt prepared)
PRE-G9B-R6-plus-A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                  = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.
