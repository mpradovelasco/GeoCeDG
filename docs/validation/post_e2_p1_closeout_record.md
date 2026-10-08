# POST-E2-P1 closeout record

```text
POST-E2-P1 = PASS — AUTHOR APPROVED   (characterization approved; no implementation)
```

```text
selfApproved              = false
authorApproved            = true    (the P1 characterization only)
passClaimed               = true    (P1 characterization only)
implementationAuthorized  = false   (Option B, P1-R1 correction, P2–P4, E3, F1, F2,
                                     F3, G)
candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE
```

This record preserves the explicit author decision of 2026-10-08, given in
writing in the session ("Continúa con la siguiente tarea", "POST-E2-P1 — Author
Approval, Publication and P1-R1 PDF Stroke-Width Characterization"), with the
exact identities behind it. It is the sole authority for the approval of
`POST-E2-P1`. Its mirror is
[`post-e2-p1-closeout.json`](../../geocedg/validation/post-e2/post-e2-p1-closeout.json).
The [characterization report](post_e2_p1_characterization_report.md), its
[evidence](../../geocedg/validation/post-e2/post-e2-p1-characterization-evidence.json)
and its [measured rows](../../geocedg/validation/post-e2/post-e2-p1-measurements.json)
record only frozen state and are not amended.

## Entry verification

```text
live remote main = origin/main = local main
                 = ed3332b5f06c83e63788b32a79fe703b13aa4375 (planning closeout)
branch           = phase/post-e2-p1-characterization, clean
history          = ed3332b5 → 02d84945 (characterization candidate); linear
```

## Author decision

```text
POST-E2-P1 candidate = 02d84945e08c54883683110c2664e5174f76bd96
                       tree b918d96ca174c92f66ea25e0461a72df9c501b60
PHASE                = verification-0f33ef67bda04e9e913a267169bc9ad3, ACCEPTED / COMPLETE, 3/3
STATIC               = verification-5ff0fe3bdfe140b3b7addc09a887f6db, ACCEPTED / COMPLETE, 3/3
classification       = P1-INTENDED-PHYSICAL-CAPTURE + P1-DEFAULT-UX-LIMITATION (accepted)
AUTHOR_APPROVAL      = POST-E2-P1 = PASS — AUTHOR APPROVED
```

Accepted conclusions: `Gap` and `Overshoot` are converted correctly from the
1 mm / 2 mm paper-space defaults into model units; the captured parameters are
independent of later viewport changes; PDF and PGF/TikZ geometric measurements
satisfy the tolerance; the large screen appearance in `mm` documents results
from the relationship between model units, physical units and the initial view
scale; no conversion defect; not a regression of the `E2` presentation
follow-up. No change of `E2` geometry or unit semantics is needed.

## Design disposition

- **Option A — documentation: approved and applied.** User guide §9.5 (EN/ES)
  now states that, with a physical unit, the defaults are paper lengths captured
  once, not screen distances.
- **Option B — configurable physical defaults: retained as a preferred future
  enhancement; not authorized.** Governing principles if designed later:
  application-level creation preferences, explicit paper-space sizes, one-time
  conversion into model units, no live viewport dependency, existing dimensions
  unchanged, no new command arguments. No numeric range or increment is
  normative without a later author decision.
- **Option C — automatic screen- or length-proportional adjustment: not
  adopted.**
- **Physical zoom aid:** future design possibility only; not authorized.

## Observation registered

```text
OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM = REGISTERED — CHARACTERIZATION
    OWNER POST-E2-P1-R1 (authorized; characterization and design only)
```

The P1 measurements suggest that the physical PDF stroke width varies with the
view zoom at export time while the exported geometry stays correct.
Registration establishes neither its root cause, its affected population nor a
normative solution.

## Closeout verification

The closeout commit is a documentary descendant of the approved candidate. It
changes one user-guide sentence per edition, so `STATIC`, `INFRA_UNIT` and the
`PHASE` selections that run the guide tests are run on it; their results and the
publication are reported outside this record.

## Next activity

```text
NEXT = POST-E2-P1-R1 — PDF stroke-width characterization and design
       (authorized; product correction NOT AUTHORIZED)
P2, P3, P4, E3, F1, F2, F3, G = NOT AUTHORIZED
```
