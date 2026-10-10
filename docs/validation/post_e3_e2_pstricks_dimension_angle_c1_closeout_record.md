# POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 closeout record

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 = PASS — AUTHOR APPROVED — PUBLISHED
AUTHOR_SMOKE                           = PASS (author confirmation, focused PSTricks smoke)
PHASE                                  = ACCEPTED / COMPLETE
OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE = RESOLVED BY C1
```

```text
selfApproved              = false
authorApproved            = true    (C1 only, candidate 22a215d8)
passClaimed               = true    (C1 only)
implementationAuthorized  = false   (F1, F2, F3, F4, G and every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE    (closeout; BOUNDED_PHASE accepted by PHASE)
```

This record preserves the author decision of 2026-10-11 on
`POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`, given in writing in the session
("C1 Author Closeout and Publication", task
`POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1-CLOSEOUT`). The decision approves and
publishes C1, conditional on the author-confirmed focused smoke. This record
is the sole authority for the author approval of C1. Its machine-readable
mirror is
[`post-e3-e2-pstricks-dimension-angle-c1-closeout.json`](../../geocedg/validation/post-e3/post-e3-e2-pstricks-dimension-angle-c1-closeout.json).
The [candidate report](post_e3_e2_pstricks_dimension_angle_c1_candidate_report.md)
and its evidence mirror record frozen state and are not amended.

## 1. Entry verification

```text
live remote main = origin/main = local main = 00b525fda64968efdf973504a6426e8565931fff
                   tree 23abd1ca06e30e07a9ed00a43522ea116180379d (published POST-E3 integration)
candidate        = 22a215d8bcf5bf9a43ce881e992e37dbf0eb070b
                   tree 2f49c8872d26bfc1b7abf648b47240e8f8dacb20, direct child of 00b525fd,
                   not amended, not on the remote; worktree clean
```

## 2. Author smoke (mandatory gate)

The author confirmed in writing in the session: "Confirmo el éxito del smoke
compilando pstricks exportación con acotación oblicua". That is, a PSTricks
export containing an oblique `E2` native dimension compiled successfully,
following the focused smoke of the candidate report §7: compiled without
editing, with the value rotated along its dimension line.

```text
AUTHOR_SMOKE = PASS
```

## 3. Author decision

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 = PASS — AUTHOR APPROVED
approved candidate = 22a215d8bcf5bf9a43ce881e992e37dbf0eb070b
                     tree 2f49c8872d26bfc1b7abf648b47240e8f8dacb20
publication        = AUTHORIZED (ordinary non-force fast-forward of main to this
                     closeout commit)
selfApproved       = false
```

The approved behaviour is limited to the six-decimal deterministic
serialization of the native-dimension text rotation in PSTricks. The written
angle is the exact binary64 reading angle rounded half to even to at most six
fractional digits, in plain locale-independent notation. The author accepts the
demonstrated bound `|Δθ| ≤ 5·10⁻⁷°` (measured `4.86·10⁻⁷°` on the dimension
cases; at most `5·10⁻⁷°` over the domain sweep) and the real TeX compilation
evidence: `latex -halt-on-error`, `dvips` and `ps2pdf` in all twelve dimension
families, including the six refused before with "Number too big".

## 4. Accepted evidence

```text
PHASE POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 on 22a215d8 / 2f49c887
      verification-6fc3607353814ce49b22c15e0ed451d2, ACCEPTED / COMPLETE, 3/3,
      51 tests, 0 diagnostics; result_hash 7e57448867e92e35e555dd5db6f33ab896725afb39f0180a297b122a063dc4c3
STATIC on 22a215d8 / 2f49c887
      verification-2d52b4606a46493982571f1e0dca7bc0, ACCEPTED / COMPLETE
      (standing diagnostics only); result_hash 4a14c187005d44e325af66e4c5d9f0028cd5a11b2131322aa98181c62a1a61fd
```

PGF/TikZ, Asymptote and every non-dimension PSTricks output are byte-identical
to the base. Kernel, placement, DAG, serialization and the shared formatter are
unchanged.

## 5. Observations

| Identifier | Disposition |
|---|---|
| `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` | resolved by C1 (author approved) |
| `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | OPEN — outside C1 (host rotations such as a rotated ellipse keep the inherited formatting) |
| `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | OPEN — correction not authorized |

A non-physical PSTricks export produces a host `article` page whose compiled
PDF has two pages. This was so at the base as well. C1 does not change it, and
it is not evidence of a C1 failure; no existing document requires it to be
recorded as an observation. No new correction is introduced.

## 6. Closeout verification and publication

This closeout commit is a documentary descendant of the approved candidate,
containing the record, its mirror and status lines. It is verified with `STATIC`
and the documentary checks. The author authorizes publication of the exact
linear range:

```text
P_POST_E3      00b525fda64968efdf973504a6426e8565931fff
  -> T_C1      22a215d8bcf5bf9a43ce881e992e37dbf0eb070b (approved candidate)
  -> P_C1      (this closeout commit)
```

The publication goes to `main` by one ordinary non-force fast-forward. No tag,
release, installer publication or version change is authorized. The published
identity is reported outside this record, which cannot name its own commit.

## 7. Next activity

```text
NEXT = readiness review of PRE-G9B-R6-plus-F1 (current roadmap, prerequisites,
       approved design, pending decisions, implementation base, required
       verification) — a separate activity
F1   = NOT AUTHORIZED (no implementation from this closeout)
F2, F3, F4, G = unchanged
```
