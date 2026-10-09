# POST-E2-P1-R2 closeout record

```text
POST-E2-P1-R2 = PASS — AUTHOR APPROVED   (physical PDF style sizes and the smoke
                                         follow-up refinements A, B, C)
```

```text
selfApproved              = false
authorApproved            = true    (the exact revised candidate below)
passClaimed               = true    (POST-E2-P1-R2 only)
implementationAuthorized  = none beyond the approved candidate; POST-E2-P2
                            characterization authorized (BOUNDED_PHASE, PHASE),
                            P2 implementation NOT AUTHORIZED; P3, P4, TD-PDF-1,
                            TD-PDF-2, E3, F1, F2, F3, G, G9B NOT AUTHORIZED
candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = YES — BOUNDED PRESENTATION (approved candidate)
GEOMETRIC SEMANTICS       = UNCHANGED
SERIALIZATION SCHEMA      = UNCHANGED
```

This record preserves the explicit author decision of 2026-10-09, given in
writing in the session ("Autorizo la siguiente tarea", "GeoCeDG — POST-E2-P1-R2
Author Approval, Publication and Authorization of POST-E2-P2 Characterization"),
with the exact identities behind it. It is the sole authority for the approval of
`POST-E2-P1-R2`. Mirror:
[`post-e2-p1-r2-closeout.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-closeout.json).
The original [candidate report](post_e2_p1_r2_candidate_report.md), the
[smoke follow-up report](post_e2_p1_r2_smoke_followup_report.md), their evidence,
measurements and raster cross-checks are immutable historical evidence and are not
amended.

## Entry verification

```text
live remote main = origin/main = local main
                 = 0bb34739168e71eb62ca099273a9920e8a61673a (P1-R1 closeout)
branch           = phase/post-e2-p1-r2-smoke-followup, clean
history          = 0bb34739 → 67815ecc (design) → 542a5adc (original candidate)
                   → 27388d60 (follow-up, first freeze) → 6dde194e (corrective,
                   approved); linear, origin/main an ancestor
```

## Author decision

```text
POST-E2-P1-R2 candidate = 6dde194eba240ed7bcc249df323a3ca6173b8a0e
                          tree 83d265149745e457063bb9efb23dfd66b55e8cfa
STATIC                  = verification-6fc51faabe804a4b9c76ea6d76655b7a, ACCEPTED / COMPLETE, 3/3
INFRA_UNIT              = verification-6c04a1c2e22e4202872923d3f095479c, ACCEPTED / COMPLETE, 22/22
PHASE                   = verification-fbc06228e7d14ef6ae8a56e6b7f322bb, ACCEPTED / COMPLETE, 3/3
INTEGRATION             = verification-a9139fffc2e5497ab4508f8c82dc2815, ACCEPTED / COMPLETE, 19/19
AUTHOR_SMOKE            = PASS WITH ACCEPTED NON-BLOCKING OBSERVATION (focused re-smoke)
AUTHOR_APPROVAL         = POST-E2-P1-R2 = PASS — AUTHOR APPROVED
```

Approved implementation:

- zoom-independent physical PDF stroke widths (`0.4 · lineThickness pt`);
- physical point-marker sizes (`0.5 pt` per marker style pixel; `s` pt markers as in
  PGF/TikZ);
- numeric line-thickness input in Object Properties → Style;
- engineering drawing scale in the GeoCeDG status bar;
- geometric semantics, document serialization and GeoGebra Classic behavior
  preserved.

The author accepts the documented residual differences between PDF and PGF marker
triangles and outlines (smoke follow-up report §1 and §5).

Historical candidates kept as evidence, not approved: the original candidate
`542a5adc7e4be15fb05985ed0a8e84817d22f795` and its runs; the first follow-up freeze
`27388d60a300d0b44173ed8fbf912643c049f3af` (INTEGRATION
`verification-1247ac65be8f4c35a6572b00fd447a31` `REJECTED_SEMANTIC`).

## Normative promotion

```text
geocedg/specs/export/physical-pdf-style-sizes.md = NORMATIVE / AUTHOR APPROVED,
                                                   version 1.0 (approved text of 0.2)
docs/adr/0035-physical-pdf-style-sizes.md        = ACCEPTED — AUTHOR APPROVED
```

Only the status lines changed; the approved technical content is preserved.

## Accepted non-blocking observation

```text
OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT
    = ENHANCEMENT — ACCEPTED NON-BLOCKING — IMPLEMENTATION NOT AUTHORIZED
```

The numeric thickness field of R2 is in Object Properties → Style but not in the
compact style-bar thickness selector, where the author considers numeric entry
most useful. Desired future behavior: keep the existing slider; add a
synchronized numeric field; reuse the existing line-thickness property and range;
keep undo/redo and style serialization; no independent thickness-setting
mechanism. Retained for a separately authorized GUI refinement; not corrected
during the R2 closeout or the P2 characterization.

## Next activity

```text
POST-E2-P2  unexpected zoom during Algebra Input
            = CHARACTERIZATION AUTHORIZED (BOUNDED_PHASE, acceptance PHASE),
              from the published R2 closeout, isolated local branch;
              product implementation NOT AUTHORIZED; publication of the P2
              candidate NOT AUTHORIZED
P3, P4      = NOT AUTHORIZED
TD-PDF-1, TD-PDF-2 = OPEN — NOT AUTHORIZED
```

## Closeout verification

Documentary closeout (record, mirror, normative promotion, planning and roadmap
status, observation registration); `STATIC` runs on the closeout commit, which is
reported outside this record with the publication.
