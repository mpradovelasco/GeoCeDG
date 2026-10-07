# PRE-G9B-R6-plus-E1-L closeout record

```text
PRE-G9B-R6-plus-E1-L    = PASS — AUTHOR APPROVED
AUTHOR_SMOKE            = PASS
AUTHOR_SMOKE_FUNCTIONAL = PASS   (R0)
AUTHOR_SMOKE_R1         = PASS   (R1 hover tips)
PRE-G9B-R6-plus-E1-P    = PREPARED — NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (E1-L only, technical candidate T_R6PLUS_E1_L = R1)
passClaimed               = true    (E1-L only)
implementationAuthorized  = false   (E1-P, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B)

PRODUCT CHANGE                = YES (R1 itself: UX TEXT ONLY)
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
DOCUMENT FORMAT CHANGE        = NONE
ACTION CATALOG CHANGE         = NONE
PACKAGING CHANGE              = NONE
MACRO DEFINITION CHANGE (R1)  = NONE
newHeavyVerification          = NONE (closeout)
```

This record preserves the explicit author disposition of 2026-10-07 on
`PRE-G9B-R6-plus-E1-L`, given in the session as a typed authorization of the
closeout instruction ("Autorizo el siguiente prompt"), with the exact identities
behind it. The closeout changes only status documentation and promotes the two
normative documents prepared by the phase; it changes no product, test, build,
packaging, verifier, registry or schema file. `E1` as a whole is not complete:
`E1-P` remains.

## 1. Provenance chain

```text
P_R6PLUS_C_X1    = 23b971f5a967958f93e745ca8c39fce1162706e6  (published base)
T_R6PLUS_E1_PREP = 0ce52a965bcf8b9de37a1572738c0ad79db52354  tree e500e80fddf957078249383ae61e28e6f707d455
AUTHORIZATION    = 904778b34704d1f395e70e27f265914dc3ffb500  (author decisions, rights record v1, split prompts)
GOVERNANCE       = 942aebe0f9c83afb3952d62e87783e92a7125bd7  (AGENTS.md §3.2 models/curated/)
E1-L R0          = 1aa05d677910b2d88101f11958d3d15b5f21960b  tree c2d6a0655d0ccd12c67ca00291da8dfd94e6fefd
                   TECHNICALLY ACCEPTED (PHASE verification-5ab86499f50843f49c41cc3ab1aeb849,
                   R0 evidence only) — SUPERSEDED BY R1; historical evidence only
E1-L R1          = 5f34d181b4f03412acfbdbeedb3fd7931de02ed9  tree f1f060703804ca99a7e8522de97a123becaabae6
T_R6PLUS_E1_L    = R1 (the approved technical candidate)
CLOSEOUT         = the commit that adds this record, child of R1
```

No commit of the chain is amended or rewritten.

## 2. Acceptance evidence (R1)

| Evidence | Result |
|---|---|
| `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L` on `5f34d181` | `ACCEPTED / COMPLETE`, 4/4, run `verification-1c7d88a3c63f4b8aacd6ae29d5b3a32a`, plan `f3f8c969…`, result `4d004222…` |
| focal and adjacent tests | pass |
| development `INFRA_UNIT` | 22/22, `verification-6933015f9eae48bd80df30626ea12e34` |
| Checkstyle, upstream boundary, `git diff --check` | no new finding; OK (970 files); clean |
| `final.desktop` for R1 | not rerun, accepted by the author: no test method added, removed or renamed; JUnit identities and pins unchanged; no registry, selection or verifier input moved; curated bytes and macro/catalog identity unchanged; the registered PHASE reran on R1 |

The technical acceptance stays bound to `5f34d181`; the closeout commit is not
a substitute candidate.

## 3. Author disposition

- `AUTHOR_SMOKE_FUNCTIONAL = PASS` (R0), `AUTHOR_SMOKE_R1 = PASS`;
  `PRE-G9B-R6-plus-E1-L = PASS — AUTHOR APPROVED`.
- Approved functionality: exactly the twelve curated tools (`SquarebyDiagonal`,
  `CirclebyD`, `circArcbyAngle`, `ellipseLength12`, `IFPositiveSelectPoint`,
  `EllipseAxis`, `conj2mainAxesEllipse`, `pointJump`, `relCoor`,
  `translationCoor`, `DuctSymbol`, `SymmSymbol`); `directDimension`,
  `axisDimension`, `sheetISOAnLand` and `sheetISOAnVert` stay excluded;
  provenance source `models/legacy/template-v7/`; library
  `models/curated/ggt-library/`; icon sources
  `geocedg/resources/icons/ggt-library/`; no new legacy package.
- Approved runtime contract: read-only, installation-relative, fail-closed on
  integrity failure, no first-run seed, non-destructive to the user-tool store;
  user packages take precedence; bundled pins are separate user-side state; no
  geometry or kernel authority in the catalog.
- Approved visual and UX contract: black, GeoGebra `lineThickness` 3, role line
  types preserved, point and text styles unchanged; tool names and macro
  identities unchanged; short hover tip = concise bilingual description of what
  the tool constructs or computes; extended help = validity-domain and CeDG
  semantic qualifications (the `pointJump`, `relCoor`, `translationCoor` caveat
  belongs there).
- Rights: [rights record version 1](../licensing/curated-ggt-library-rights-record.md)
  preserved (author sole rights holder; derivative modification, inclusion,
  `INTERNAL` and `NC` authorized; `COMMERCIAL` not authorized / pending;
  definitions EUPL-1.2, owned icons CC BY 4.0; no rights inferred for excluded
  GeoGebra artwork or unrelated `Templatev7` contents).
- Feature: `cedg.library.curated-ggt`, `experimental`, enabled by default;
  promotion to `stable` remains a future author decision (`G` or another
  authorized phase).

## 4. Normative promotion

| Document | Status after this closeout |
|---|---|
| `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md` | `ACCEPTED — AUTHOR APPROVED` |
| `geocedg/specs/legacy/curated-ggt-library.md` | `NORMATIVE / AUTHOR APPROVED` (version 1.0, including the R1 hover-tip rule) |

Only their status lines change; their committed contents correspond to R1 and
the author decisions and are not broadened.

## 5. State preserved

- `PRE-G9B-R6-plus-E1-P = PREPARED — NOT AUTHORIZED`; its canonical prompt is
  published as preparation state only. `G` keeps depending on `E1-P` unless the
  author re-plans it.
- `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` (owner `G`): unchanged; visible version
  `1.0.0` until `PRE-G9B-R6-plus-G = PASS — AUTHOR APPROVED`, then `1.1.0`;
  that promotion authorizes no tag, release, binary publication or commercial
  distribution.
- `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`: `E2`-owned constraint,
  unchanged.

## 6. Publication

Authorized by the same instruction: an ordinary non-force fast-forward of
`main` from `23b971f5` through the chain above to the closeout commit; no merge
commit, no force push, no tag, no release. The live remote `main` is verified
independently after the push; that verification is reported with the closeout,
outside this artifact, which is part of the published commit.
