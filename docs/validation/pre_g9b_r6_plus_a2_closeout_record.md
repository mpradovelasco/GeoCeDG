# PRE-G9B-R6-plus-A-2 closeout record

```text
PRE-G9B-R6-plus-A-2 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE        = PASS
```

```text
selfApproved              = false
authorApproved            = true    (A-2 only; F3 planning; A-2 preparation package)
passClaimed               = true    (A-2 only)
implementationAuthorized  = false   (C, E1, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B,
                                     the open observations and the enhancement below)

candidateFrozen           = true
candidateMutated          = false
SERIALIZATION CHANGE      = YES relative to D1 / pre-A-2
                            NONE relative to A-2 revisions 2 and 3
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-04 on
`PRE-G9B-R6-plus-A-2`, on the `F3` planning reconciliation and on the `A-2`
preparation package, with the exact identities behind them. It changes no
product, test, build, packaging, verifier, registry, schema, specification or
ADR file, and it does not modify any frozen candidate. Besides this record and
its machine-readable mirror,
[`pre-g9b-r6-plus-a2-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_a2_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen technical state. They are not
amended. This record is the sole authority for the author approval of `A-2`.

## Author disposition

```text
PRE-G9B-R6-plus-A-2 = PASS — AUTHOR APPROVED
T_R6PLUS_A2         = 06a2ea6b4523a71f9f84fefca39e7d3b2b717e64
                      tree a05d84fd0e4ddcff27e15b2e09757ae28d36debb
AUTHOR_SMOKE        = PASS
selfApproved        = false
```

The author instruction of 2026-10-04 approves exactly revision 3 after a
satisfactory author smoke and authorizes exclusively this documentary closeout
and the publication of the complete linear chain below. Revision 3 is the only
candidate approved.

The final reduced author smoke, run with the author's installed preferences:

1. File → New, no user action, close: no save prompt;
2. hide a layer, close: the save prompt appears;
3. save the hidden-layer change, close: no further save prompt.

This closes the saved-state issue. The earlier successful `A-2` smoke findings
remain accepted evidence and are not repeated.

## Candidate history

| Candidate | Commit / tree | FINAL | Role |
|---|---|---|---|
| revision 1 | `e3cbdc60743b9f91490f7bb6a80f789d87a8f814` / `235869d7c127dc9d90b104cbd10a5c461ee0690c` | `verification-b7942a8be41c44728a1a6b6289ecf08a` (`ACCEPTED / COMPLETE`, receipt `65abf5d1b4f8dbba8ed15c8eac967ac1d3df88b9a6bd94dff89abfbed1e66e80`) | historical evidence only |
| revision 2 | `c1c08e9016c7c52446a3b6cd51856cdf7e67e20d` / `1c824ce7399d23acedadf251832cfd01e7bf265c` | `verification-e81bd96094fc47f080a3b234448834a9` (`ACCEPTED / COMPLETE`, receipt `1db2e4cd5b0d79faca5a8666cadfb9f524fb8c5f5b0310ba529f359c92b25fa5`) | historical evidence only |
| revision 3 | `06a2ea6b4523a71f9f84fefca39e7d3b2b717e64` / `a05d84fd0e4ddcff27e15b2e09757ae28d36debb` | `verification-814f89fabe834b78ad29f08d78729081` | **authoritative; author approved** |

No frozen candidate was rewritten or amended.

## Approved identities

```text
P_R6PLUS_D1         = 2941ddf2f2712340e831d292bdc2687b879850d6
                      tree 158184b150a42e6dcae2946c39620d92cbfcd888
                      published D1 closeout; publication base
T_R6PLUS_F3_PLAN    = f467eb4563de7a7eb162da1b74954f9f847fa9d3
                      tree ece51c96117d6cbd55bc200d29c5240686516da9
                      F3 planning reconciliation; PLANNED — AUTHOR APPROVED
T_R6PLUS_A2_PROMPT  = 0e50626f70107376e691df6dea7bfcfe06de5c0a
                      tree fefc57cece8090941ad0ac16098e58a679a290ec
                      A-2 preparation package; PASS — AUTHOR APPROVED
A_R6PLUS_A2_PROMPT  = bc7436e8d4eb8c35c9bec7ab107c7fc13f63f67c
                      tree 8df989d752f7cc3b640a40fb461f3f6a571bdccb
                      implementation authorization record
T_R6PLUS_A2_R1      = e3cbdc60743b9f91490f7bb6a80f789d87a8f814
                      tree 235869d7c127dc9d90b104cbd10a5c461ee0690c
T_R6PLUS_A2_R2      = c1c08e9016c7c52446a3b6cd51856cdf7e67e20d
                      tree 1c824ce7399d23acedadf251832cfd01e7bf265c
T_R6PLUS_A2         = 06a2ea6b4523a71f9f84fefca39e7d3b2b717e64
                      tree a05d84fd0e4ddcff27e15b2e09757ae28d36debb
                      parent c1c08e9016c7c52446a3b6cd51856cdf7e67e20d
                      approved technical candidate (revision 3); not amended

branch              = phase/pre-g9b-r6-plus-a2-layer-domain-persistence (local until publication)
```

The identity of this closeout commit, `P_R6PLUS_A2`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `GLOBAL_IMPACT` required exactly one `FINAL` on the exact
candidate commit and tree.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-814f89fabe834b78ad29f08d78729081` — `verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r6-plus-a2-rev3-final-06a2ea6b` | `06a2ea6b…` / `a05d84fd…` | exit 0; `ACCEPTED / COMPLETE`; 41/41 acceptance leaves; plan `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517`; result `a40db4dacf7ed917080d7692506e55dbf68e8bdb474b8b59034c6e5a3ef3fc45`; receipt `b492d484fa3ae9f820036927045373128606b6eee59a4e80961fe0756db0d9ea` (receipt file SHA-256 `8a810cbdc37c8d304ca398719ca6ccd57b58b71807aac09950a2b7716369288f`); standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) |

`tools/agent/phase-closeout.ps1 -Action INSPECT` on that receipt and candidate:
exit 0, `identity_confirmed = true`, no mismatch, no verification executed, no
repository mutation. `git diff --check` over `2941ddf2…..06a2ea6b…`: exit 0. No
new `FINAL` is required for this documentation-only closeout; this closeout
commit's own `STATIC` run is reported with the publication.

## Accepted A-2 result

- GeoCeDG layer domain `0..99`; Classic remains `0..9`.
- `L_MAX = 99` is a product/configuration bound, not a permanent CeDG
  conceptual limit.
- Hidden layers are `DOCUMENT_PRESENTATION`: document-wide, persistent and not
  undoable; the runtime owner remains `GeoCeDGLayerWorkspace`.
- Hidden layers do not modify object visibility, geometry, the DAG or semantic
  identity.
- The working layer remains session state.
- Open chooses the highest used non-hidden layer, otherwise the lowest
  non-hidden layer, and never silently changes the persisted hidden set.
- All-domain hidden metadata fails closed.
- Layer values outside the effective product domain keep the accepted clamp
  behavior.
- 3D renderer clamping is presentation-only and never mutates the authoritative
  layer value.
- Persistent hidden-layer edits mark the document unsaved without adding an
  undo point.
- A completed untouched New establishes a saved baseline after all automatic
  initialization.
- Base64 and non-native-transport full-document replacements commit the
  persistent hidden-layer state through the accepted document-replacement hook.
- Merge/import routes remain non-document transitions and do not consume
  hidden-layer metadata.

## Serialization

```text
SERIALIZATION CHANGE = YES   relative to D1 / pre-A-2
SERIALIZATION CHANGE = NONE  relative to A-2 revision 2 and revision 3 remediation
```

Relative to `D1` / pre-`A-2`:

1. the authoritative range of the GeoCeDG `<layer val>` expands to `0..99`;
2. a new lazy full-document element
   `<geocedgHiddenLayers version="1" layers="..."/>`.

## Accepted focal corrections

### Base64 / non-native document replacement (revision 2)

```text
A2 FOCAL REMEDIATION — ACCEPTED
```

Accepted root cause: some full-document replacement transports parsed the `A-2`
metadata but had no successful document-replacement commit seam.

Accepted solution: a shared default-no-op `documentReplacementCommitted()`
seam; a successful Base64 or non-native replacement calls it; `AppGeoCeDG`
reuses its existing document-commit logic; startup, the native transaction,
import and merge keep their distinct semantics. Base64 is a transport of the
same document format, not a separate document format.

### New-document saved baseline (revision 3)

```text
OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT
  = PRE-EXISTING — REPRODUCED ON P_R6PLUS_D1 2941ddf2 — NOT CAUSED BY A-2 —
    AUTHOR-AUTHORIZED FOCAL CORRECTION DURING A-2 REVIEW — CLOSED
```

Accepted root cause: installed preferences could make an untouched New appear
started or unsaved through effects of the host preference reload (a saved
`consStep` and the numeric a saved table view registers).

Accepted semantic correction: after a successful File → New has completed all
automatic initialization, including preferences, the `D1` defaults and the
`A-2` workspace initialization, the resulting initial state is the saved
baseline. `A-2` did not introduce this defect.

## Residual observations and enhancements

| ID | Observation or enhancement | Disposition |
|---|---|---|
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | Session state such as the `B` `ExportArea` and the `D1` transient paste notice may not reset on every non-native full-document replacement route. Not fixed in this closeout; not assigned to `F3` or `C`. | `OPEN — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT` |
| `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` | Apparent absence of a preview surface in the route the author tested. Distinct from `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`, which concerns a preview's contents. Preferred characterization owner `C`. | `NOT PROVEN A-2 REGRESSION — CHARACTERIZE BEFORE/DURING C — NOT A-2 BLOCKING` |
| `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | Keep the existing layer list/scroll and add a direct numeric entry, validated against the same configured `0..L_MAX`; both routes modify the same authoritative working-layer state. Preferred owner `F2`. | `ENHANCEMENT — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED` |
| 3D draw-order limitation | GeoCeDG layers above the renderer's distinguishable Classic-compatible range may share the same 3D draw-order coding. No model, XML or API layer value is clamped back because of it. | `KNOWN PRESENTATION LIMITATION — NOT AUTHORITATIVE LAYER SEMANTICS — NOT A-2 BLOCKING` |
| `F3` Apply Template re-layering | Apply Template may re-layer restyled objects through existing default-layer behavior ([F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md), finding 7). | `OPEN F3 CHARACTERIZATION QUESTION — F3 IMPLEMENTATION NOT AUTHORIZED` |

## F3 planning and A-2 preparation dispositions

```text
T_R6PLUS_F3_PLAN    = f467eb4563de7a7eb162da1b74954f9f847fa9d3
                      tree ece51c96117d6cbd55bc200d29c5240686516da9
PRE-G9B-R6-plus-F3  = PLANNED — AUTHOR APPROVED — PUBLISHED (with this chain)
F3 IMPLEMENTATION   = NOT AUTHORIZED
future class        = INTEGRATED_PHASE; planned acceptance PHASE + INTEGRATION
future scope        = File → Insert; Insert File; Apply Template; .cedg + .ggb;
                      OFF hidden from the GeoCeDG UI

T_R6PLUS_A2_PROMPT  = 0e50626f70107376e691df6dea7bfcfe06de5c0a
                      tree fefc57cece8090941ad0ac16098e58a679a290ec
                      PASS — AUTHOR APPROVED; published with this chain
A_R6PLUS_A2_PROMPT  = bc7436e8d4eb8c35c9bec7ab107c7fc13f63f67c
                      tree 8df989d752f7cc3b640a40fb461f3f6a571bdccb
                      remains the implementation authorization record
```

No `F3` implementation is marked `PASS`.

## Status updates in this closeout

- Roadmap 4.57: `A-2` `PASS — AUTHOR APPROVED — PUBLISHED` in the last closed
  and last executed phase entries, the next-gate entry, the track paragraph and
  the `A-2` row; the closed subphases and the `F3` planning marked as
  published; `C` marked as the next operational subphase, `NOT AUTHORIZED`.
- Mini-track plan: the `A-2` state in the header, the §3 table and §14; the
  residual observations recorded; `C` marked as the next operational subphase.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_D1        2941ddf2f2712340e831d292bdc2687b879850d6
  -> T_R6PLUS_F3_PLAN   f467eb4563de7a7eb162da1b74954f9f847fa9d3 (F3 planning)
  -> T_R6PLUS_A2_PROMPT 0e50626f70107376e691df6dea7bfcfe06de5c0a (A-2 preparation)
  -> A_R6PLUS_A2_PROMPT bc7436e8d4eb8c35c9bec7ab107c7fc13f63f67c (A-2 authorization)
  -> T_R6PLUS_A2_R1     e3cbdc60743b9f91490f7bb6a80f789d87a8f814 (candidate revision 1)
  -> T_R6PLUS_A2_R2     c1c08e9016c7c52446a3b6cd51856cdf7e67e20d (candidate revision 2)
  -> T_R6PLUS_A2        06a2ea6b4523a71f9f84fefca39e7d3b2b717e64 (revision 3, author approved)
  -> P_R6PLUS_A2        (this closeout commit)
```

to `main`, by one clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_D1` immediately before
the push. Forbidden: merge commits, rebases, squashes, amends of frozen
candidates, force pushes, tags, releases, binary publication and any further
implementation. The `PUBLISHED` states below take effect with that
fast-forward.

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-B    = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D1   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-2  = PASS — AUTHOR APPROVED — PUBLISHED

PRE-G9B-R6-plus-C    = NOT AUTHORIZED — NEXT OPERATIONAL SUBPHASE

PRE-G9B-R6-plus-E1   = NOT AUTHORIZED
PRE-G9B-R6-plus-E2   = NOT AUTHORIZED
PRE-G9B-R6-plus-E3   = NOT AUTHORIZED
PRE-G9B-R6-plus-F1   = NOT AUTHORIZED
PRE-G9B-R6-plus-F2   = NOT AUTHORIZED

PRE-G9B-R6-plus-F3   = PLANNED — AUTHOR APPROVED — PUBLISHED
                       IMPLEMENTATION NOT AUTHORIZED

PRE-G9B-R6-plus-G    = NOT AUTHORIZED

PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus GLOBAL CLOSEOUT

G9B                  = NOT AUTHORIZED
```

The accepted operational order remains
`P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`. No
authorization of `C` follows from this closeout: `C` needs its own explicit
author instruction naming its exact base. No tag or release is authorized.
