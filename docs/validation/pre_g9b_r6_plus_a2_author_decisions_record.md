# PRE-G9B-R6-plus-A-2 author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS (preparation instruction)
DECISION_DATE            = 2026-10-03
DECIDED_ON_BASE          = 2941ddf2f2712340e831d292bdc2687b879850d6
                           tree 158184b150a42e6dcae2946c39620d92cbfcd888
                           (P_R6PLUS_D1, published D1 closeout)
PREPARATION_BASE         = f467eb4563de7a7eb162da1b74954f9f847fa9d3
                           tree ece51c96117d6cbd55bc200d29c5240686516da9
                           (T_R6PLUS_F3_PLAN, the local F3 planning commit)

selfApproved             = false
authorApproved           = false   (no A-2 candidate exists)
implementationAuthorized = false   (A-2, F3 and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the decisions that the author's instruction
of 2026-10-03 fixed for the preparation of `PRE-G9B-R6-plus-A-2`. It is the
authority for them. Its machine-readable mirror is
[`pre-g9b-r6-plus-a2-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-author-decisions.json).

The instruction authorized only the documentary preparation of the canonical
`A-2` prompt, a characterization report and its mirror, this record and the
status updates they strictly require, on the exact preparation base above. It
stated that it **does not authorize the implementation of `A-2`**: execution
needs a further explicit author instruction that names the exact prepared
candidate or base. The instruction resolves no open layer decision. The `A`
decisions `AQ-L1a`, `AQ-L1b`, `AQ-L1c`, `AQ-L3`, `AQ-L5`, `AQ-L7` and `AQ-L8`
of the [A author-decision record](pre_g9b_r6_plus_a_author_decisions_record.md)
govern; this record does not restate them differently.

## Authorities named for `A-2`

- `AQ-L1a`, `AQ-L1b`, `AQ-L1c`, `AQ-L3` and `AQ-L7(3)` of the
  [A author-decision record](pre_g9b_r6_plus_a_author_decisions_record.md);
- the accepted `A-1` implementation and its
  [closeout record](pre_g9b_r6_plus_a1_closeout_record.md);
- live source and serialization at the exact base.

`A-2` does not reopen accepted `A-1` semantics unless persistence or the domain
widening requires an explicit compatibility decision, which the canonical
prompt then requests from the author.

## Verification class

```text
VERIFICATION_CLASS  = GLOBAL_IMPACT   (author-accepted 2026-10-02, kept)
PLANNED_ACCEPTANCE  = FINAL on one exact frozen candidate
FINAL during preparation = not run
```

The class is not downgraded because individual edits appear small.

## Normative scope

`A-2` implements only:

1. the GeoCeDG layer-domain widening from `0..9` to `0..99`;
2. `L_MAX = 99` as a current product/configuration limit, not a permanent CeDG
   conceptual limit;
3. document-wide persistent hidden-layer state;
4. the compatibility behavior and serialization that persistence requires;
5. the interaction between persistent hidden layers and the `A-1` layer
   workspace;
6. the tests and compatibility evidence its `GLOBAL_IMPACT` class requires.

Author decisions restated by the instruction, already authoritative through
the `A` record:

```text
GeoCeDG layer domain = 0..99          Classic layer domain = 0..9
hiddenLayers         = DOCUMENT_PRESENTATION, document-wide, persistent, not undoable
effectiveVisible(object) = objectVisible(object) AND NOT layerHidden(object.layer)
```

Changing hidden layers never changes object visibility, geometry, the DAG,
identity or numeric values.

## Mandatory characterization before authorization

The preparation must characterize, from live code and scratch-only probes, and
the canonical prompt must encode (`A2-C1` to `A2-C12`): the layer-domain
authority map; GeoCeDG versus Classic separation; the `<layer val>` lifecycle
and older-reader loss; the persistent hidden-layer owner; the persistence
shape, re-characterized against the `D1` serialization architecture; the
persistence-without-undo mechanism over every writer and snapshot route; the
New and Open lifecycle; the cases of `AQ-L7(3)` as author decision questions
`DQ-A2-*`; effective visibility after reopen, including
`OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`; the classification of the `A-1`
observations; the compatibility corpus; and the invariants.

If the architecture cannot distinguish product bounds without a material
upstream or kernel redesign, if the persistence shape differs materially from
the accepted design candidate, or if persistence without undo cannot be
achieved without widening the undo architecture materially, the preparation
stops for author review.

## Forbidden scope fixed by the author

`F3` (File → Insert); `C`-owned exporters; `E1`; `E2`; `E3`; `F1`; `F2`;
`drawingScale`; unit-system redesign; the hierarchy, roles, locking, filters and
named layer states of `G11`; per-view persistent layer state beyond the
document-wide hidden set; rewriting object visibility to emulate hidden
layers; changing `ShowLayer`/`HideLayer` semantics; the unrelated rollback GUI
debt; opportunistic 3D-layer behavior unless the domain widening makes a
minimal compatibility change unavoidable and the prompt characterizes it.

## Stop conditions fixed by the author

The future `A-2` implementation stops and asks for author review if
correctness requires:

- widening Classic to `0..99`;
- changing object visibility to persist hidden layers;
- making hidden layers geometric or DAG state;
- making hidden-layer toggles undoable;
- serializing `workingLayer`;
- persisting per-view hidden sets instead of the document-wide set;
- changing `C`-owned exporters;
- changing unit semantics;
- a material restructuring of the general undo architecture;
- changing the accepted `AQ-L1a`, `AQ-L1c`, `AQ-L3` or `AQ-L7` decisions;
- absorbing `F3` or rollback-debt work.

## Preparation status required

```text
PRE-G9B-R6-plus-A-2      = PREPARED — NOT AUTHORIZED
implementationAuthorized = false
selfApproved             = false
authorApproved           = false
passClaimed              = false
```

## What stays open

- The decisions requested by the canonical
  [`A-2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md)
  under *Decisions requested before authorization* (`DQ-A2-1` to `DQ-A2-11`),
  among them `AQ-L7(3)` as `DQ-A2-1`. The prompt fixes a default contract for
  each; none is an author decision until the author makes it.
- The DXF hidden-layer policy, in `C`.

## Authorization state

```text
PRE-G9B-R6-plus-P0, A-1, B, D0, D1 = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-2  = PREPARED — NOT AUTHORIZED   (next operational subphase;
                       canonical prompt prepared)
PRE-G9B-R6-plus-F3   = PLANNED — NOT AUTHORIZED
PRE-G9B-R6-plus-C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                  = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.
