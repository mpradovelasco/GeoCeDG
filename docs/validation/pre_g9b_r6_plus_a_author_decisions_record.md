# PRE-G9B-R6-plus-A author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS
DECISION_DATE            = 2026-10-02
DECIDED_ON_BASE          = 562e2bb1e77b249b9c97c2e6e06e8b123d6de900
                           tree 9f578093f2f3cba481f5630fe827fd3ef08cff61
                           (P_R6PLUS_P0, published P0 closeout)

selfApproved             = false
implementationAuthorized = false   (A-1, A-2 and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the author decisions of 2026-10-02 for
`PRE-G9B-R6-plus-A`. It is the authority for them. Its machine-readable mirror
is
[`pre-g9b-r6-plus-a-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a-author-decisions.json).

The author stated that these decisions **do not authorize implementation**.
They answer the open decisions `AQ-L1a`, `AQ-L1b`, `AQ-L1c`, `AQ-L2`, `AQ-L3`,
`AQ-L4`, `AQ-L5`, `AQ-L7` and `AQ-L8` of the
[P0 candidate report](pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15. Every other open decision of that report stays open. The frozen P0
candidate report and the
[layer-workspace design candidate](../architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md)
are not amended. Where this record differs from that candidate, this record
prevails.

## Decisions

| ID | Author decision | Subphase |
|---|---|---|
| `AQ-L1a` | The GeoCeDG layer domain for the first extension is the bounded non-negative range `0..99`. `L_MAX = 99` is a product/configuration limit, not a permanent conceptual restriction of CeDG. Classic keeps `0..9`. | `A-2` |
| `AQ-L1b` | `A` is split into `A-1` and `A-2`. | `A-1`, `A-2` |
| `AQ-L1c` | The loss of forward compatibility with older Classic and GeoCeDG readers is accepted and must be documented explicitly: layers above 9 may be clamped to 9 by those readers and lost when they save. | `A-2` |
| `AQ-L2` | `workingLayer` is `SESSION`. New starts at layer 0. Initialization after Open must preserve the document and must not silently modify the persisted visibility state. | `A-1` |
| `AQ-L3` | Hidden layers are conceptually `DOCUMENT_PRESENTATION`: document-wide, persistent and not undoable. `A-1` implements the mechanism first with `SESSION` state; `A-2` adds persistence. Objects on hidden layers are excluded from LaTeX. The DXF policy is resolved in `C`. | `A-1`, `A-2`, `C` |
| `AQ-L4` | The working-layer mode is one-shot and returns to Move after a valid selection. | `A-1` |
| `AQ-L5` | Upstream `ShowLayer` and `HideLayer` stay unchanged and keep acting on individual object visibility. | `A-1` |
| `AQ-L7` | (1) The working layer cannot be hidden through normal interaction. (2) If the user selects as working layer a layer that is currently hidden, that layer is shown and becomes the working layer. (3) The interaction between Open and persistent hidden layers is deferred to `A-2`; it must be resolved explicitly before `A-2`, without silently altering presentation on load. | `A-1`; (3) `A-2` |
| `AQ-L8` | Paste keeps the copied objects' layers and does not move them to the working layer. | `A-1` |

## Decomposition, classes and order

```text
PRE-G9B-R6-plus-A-1 = operational workspace, working layer, Move-group mode,
                      status bar, numeric Algebra View order, effective hiding
                      in the normal views (painting and hit testing) and
                      SESSION hidden-layer state;
                      no domain widening, no new serialization, no exporter change
                      VERIFICATION_CLASS = INTEGRATED_PHASE
                      (registered PHASE + INTEGRATION)

PRE-G9B-R6-plus-A-2 = layer-domain widening to 0..99 and hidden-layer
                      persistence, with their serialization and compatibility
                      impact (AQ-L1c)
                      VERIFICATION_CLASS = GLOBAL_IMPACT (FINAL)

ACCEPTED ORDER      = A-1 -> B -> D0 -> D1 -> A-2 -> C -> E1 -> E2 -> E3 -> F1 -> F2 -> G
```

The classes are author-accepted. Each is formally frozen
(`frozenAtPhaseStart = true`) when its subphase is authorized, under
`verification-levels.md` §12.8.

## Export ownership

Fixed by the author instruction of 2026-10-02, keeping the ownership the P0
design candidate defined:

- `A-1` implements `effectiveVisible` for the normal views, through painting
  and hit testing, and for the layer workspace. It changes no exporter.
- The SVG export route belongs to `B`.
- The LaTeX exclusion of objects on hidden layers is decided (`AQ-L3`) and is
  implemented in `C`.
- DXF stays entirely outside `A-1`; its hidden-layer policy is completed in
  `C`.

Temporary consequence, to be documented by `A-1`: exports that reuse the
shared view painting (PNG, PDF, EMF, print, graphics image copy) omit objects
on hidden layers once `A-1` lands, while SVG (until `B`) and LaTeX and DXF
(until `C`) still write them.

## What stays open

- `AQ-L7` (3): the interaction between Open and persistent hidden layers,
  before `A-2`.
- The DXF hidden-layer policy, in `C`.
- Every other open decision of the P0 candidate report §15.

The working-layer value after Open, the disabled eye of the working layer, the
mode gestures and the status-bar interaction are contracts of the prepared
[`A-1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md),
not author decisions.

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = NOT AUTHORIZED    (next subphase; canonical prompt prepared)
PRE-G9B-R6-plus-A-2  = NOT AUTHORIZED
PRE-G9B-R6-plus-B, D0, D1, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                  = NOT AUTHORIZED
```
