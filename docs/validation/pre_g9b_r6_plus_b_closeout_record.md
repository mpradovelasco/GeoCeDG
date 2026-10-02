# PRE-G9B-R6-plus-B closeout record

```text
PRE-G9B-R6-plus-B = PASS — AUTHOR APPROVED
AUTHOR_SMOKE      = PASS
```

```text
selfApproved              = false
authorApproved            = true    (B only)
passClaimed               = true    (B only)
implementationAuthorized  = false   (D0, D1, A-2, C, E1, E2, E3, F1, F2, G, PRE-G9B-R7, G9B)

candidateFrozen           = true
candidateMutated          = false
SERIALIZATION CHANGE      = NONE
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-02 on
`PRE-G9B-R6-plus-B` and the exact identities behind it. It changes no product,
test, build, packaging, verifier, registry, schema, specification or ADR file,
and it does not modify the frozen candidate. Besides this record and its
machine-readable mirror,
[`pre-g9b-r6-plus-b-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_b_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. They are not amended. This
record is the sole authority for the author approval of `B`.

## Author disposition

```text
PRE-G9B-R6-plus-B = PASS — AUTHOR APPROVED
T_R6PLUS_B        = 848206c413eb4f78694c9149e153f6e867eb9525
AUTHOR_SMOKE      = PASS
selfApproved      = false
```

The author instruction of 2026-10-02, confirmed directly by the author,
approves the frozen candidate with author smoke `PASS` and authorizes
exclusively the documentary closeout and the publication of `B`. The manual
smoke verified the export surface, `ExportArea`, the `MANUAL` and
`Export_1`/`Export_2` areas, the overlay, hidden layers, the regeneration of the
Save preview, disjoint areas, Locus V2, objects at an absolute screen position
and the export of texts.

The initial smoke observation that texts were not exported is closed as a
false alarm of visualization: the text is exported correctly; with a
transparent background its visibility depended on the background of the viewer
used. No product change is required.

## Approved identities

```text
P_R6PLUS_B_PROMPT = f6194f09358de9c5f8ac5051c31b3a9dc8a9491b
                    tree 9ac09c5371bce7d9b093541f232b43464073118b
                    published B preparation closeout; implementation base

A_R6PLUS_B_PROMPT = fe70c5ac6 (canonical prompt AUTHORIZED; blob d13b3cb182c0fed4b21bab30e074c697ec58f423)
A_R6PLUS_B_DQB8   = dd6d517c6 (author resolution of the DQ-B8 stop; blob 4ac70712bad82e6d5825c8fce2ed71f8b21742bc)
A_R6PLUS_B_STOPS  = 574808a04 (author resolution of the characterization stops; blob feb0fb0d2e987477045c124f51216d11130f9083)

T_R6PLUS_B        = 848206c413eb4f78694c9149e153f6e867eb9525
                    tree b611506131b3d751627f87a31e3f3502e7ea6810
                    parent 574808a04647a9627d0d68ac8a32bb2730b7f286
                    approved technical candidate; not amended

branch            = phase/pre-g9b-r6-plus-b-export-surface (local; never pushed)
```

The identity of this closeout commit, `P_R6PLUS_B`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `INTEGRATED_PHASE` required the registered PHASE selection
plus `INTEGRATION` on the same exact commit and tree.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-7f186595655b4640beb14fcaeb9f5c39` — `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-B` (external log root) | `848206c4…` / `b6115061…` | exit 0; `ACCEPTED / COMPLETE`; 7/7 acceptance leaves; plan `e98cf890c036e9b46d144543196b89d2d4d31375f00fa47a2692bc8a77ae2fd8`; result `7aebaae5ccf7cc331ee276dcfb332ba7613f4175f0e05c3e23584abff81c36cf` |
| `verification-fea933f1c0344f5f95830aab1bc80f2d` — `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/pre-g9b-r6-plus-b-integration-848206c4` | `848206c4…` / `b6115061…` | exit 0; `ACCEPTED / COMPLETE`; 19/19 acceptance leaves; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; result `2444c18ff590643c9adec49d3723900c8285f6ec8cdf221f99d61fe5c3a9a5fc`; standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) |

No invalid attempt preceded either run. `git diff --check` over
`f6194f09…..848206c4…`: exit 0. `FINAL` is not required for this class; no
receipt is expected or claimed. This closeout commit's own `STATIC` run is
reported with the publication.

## Serialization

`SERIALIZATION CHANGE = NONE`. `ExportArea`, its producers and the overlay are
`SESSION` state: no document element, no XML attribute, no undo-XML,
preferences, macro or clipboard content; documents re-save identically.

## Observations and deferred improvement

Accepted residual observations, kept as recorded by the candidate:

| ID | Observation | Disposition |
|---|---|---|
| `OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE` | Legacy `GeoLocus` coverage remains limited by its kernel/live-view sampling window; `B` does not mutate or recompute the construction to extend it. | accepted; not promoted to `C` |
| `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` | The EMF `rclFrame` (0.01 mm) keeps the FreeHEP physical quantization derived from the integer device bounds. | accepted; later owner `C` |
| `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` | The FreeHEP PDF and SVG writers print path coordinates with five significant digits; the PDF page box, transforms and rectangular clips of the opt-in page and the SVG `viewBox` are exact. | accepted |

Non-blocking improvement recorded at author smoke:

| ID | Objective | Contract |
|---|---|---|
| `ENH-B-MANUAL-EXPORT-AREA-DRAG` | Let the `MANUAL` producer also be defined by an interactive rectangle gesture similar to `ZoomWindow`, besides entering coordinates. | `interactive rectangle gesture -> world bounds -> ExportArea(MANUAL)`. The gesture is only a transient input mechanism; it must not restore `selectionRectangle` as export authority nor create a second area mechanism. Not implemented in `B`; the candidate is not reopened. |

The temporary export inconsistency documented by the candidate (PSTricks,
PGF/TikZ, Asymptote and DXF ignore the export area and still write hidden
layers until `C`) is unchanged.

## Status updates in this closeout

- Roadmap 4.44: `PRE-G9B-R6-plus-B = PASS — AUTHOR APPROVED` in the last closed
  and last executed phase entries, the next-gate entry, the track paragraph and
  the `B` row; the next operational subphase is `PRE-G9B-R6-plus-D0`,
  `NOT AUTHORIZED`.
- Mini-track plan: the `B` state in the header, the §3 table and §14; `D0`
  marked as the next operational subphase.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_B_PROMPT f6194f09358de9c5f8ac5051c31b3a9dc8a9491b
  -> A_R6PLUS_B_PROMPT fe70c5ac6
  -> A_R6PLUS_B_DQB8   dd6d517c6
  -> A_R6PLUS_B_STOPS  574808a04
  -> T_R6PLUS_B        848206c413eb4f78694c9149e153f6e867eb9525
  -> P_R6PLUS_B        (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: `origin/main` and
the live remote `main` equal `P_R6PLUS_B_PROMPT` immediately before the push.
Forbidden: merge commits, rebases, squashes, amends, force pushes, tags,
releases, binary publication and any further implementation.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0         = NOT AUTHORIZED   (next operational subphase)
PRE-G9B-R6-plus-D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

This authorization ends with the publication of `B`. `D0` needs its own explicit
author instruction naming its exact base. No tag or release is authorized.
