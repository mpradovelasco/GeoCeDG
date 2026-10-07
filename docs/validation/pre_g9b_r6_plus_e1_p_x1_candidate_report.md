# PRE-G9B-R6-plus-E1-P-X1 technical / verification candidate report

```text
TECHNICAL_CANDIDATE_STATE  = FROZEN with the candidate commit that contains this report
AUTHOR_DECISION            = NOT_RECORDED_IN_THIS_ARTIFACT

BASE                       = P_R6PLUS_E1_X1 7d132ec5696535e18419e481b8306b5aecf3fef6
                             tree 7509dcb05117eca87011e2bb82ac481592afbcad
VERIFICATION_CLASS         = BOUNDED_PHASE
PRODUCT CHANGE             = NONE
PACKAGING PRODUCT CHANGE   = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
SERIALIZATION CHANGE       = NONE
DOCUMENT FORMAT CHANGE     = NONE
KERNEL CHANGE              = NONE
FINAL.DESKTOP              = CLEAN
AUTHOR_SMOKE               = NOT REQUIRED FOR PRODUCT BEHAVIOR / AUTHOR REVIEW PENDING
selfApproved               = false
passClaimed                = false
```

This report records the bounded test-only reconciliation of
`OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION`, authorized by the author on
2026-10-07 under `PRE-G9B-R6-plus-E1-P-X1`. It is a post-closeout verification
reconciliation: `E1-L`, `E1-P`, `E1` and `E1-X1` stay `PASS — AUTHOR APPROVED —
PUBLISHED`, and `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` stays resolved
in `E1-X1`. Their records are not rewritten. The machine-readable mirror is
[`pre-g9b-r6-plus-e1-p-x1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-p-x1-candidate-evidence.json).
The candidate commit cannot name itself; its identity and the acceptance runs
on it are reported outside this artifact.

## 1. Entry gate

```text
live remote main = origin/main = local main = 7d132ec5696535e18419e481b8306b5aecf3fef6
tree 7509dcb05117eca87011e2bb82ac481592afbcad; worktree clean
branch   = phase/pre-g9b-r6-plus-e1-p-x1-stale-assertion (local)
commits  = 85e0aa4a4442e2951013e76c75b3617752778654  test correction (tree 0c0f7964933243f06f6eaf77d9588578ed47891d)
           candidate                                   registration, inventory, records
```

## 2. Changed files

| File | Change |
|---|---|
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusE1LCuratedLibraryTest.java` | `e1l09` packaging clauses reconciled (+22/−3), method identifier kept |
| `geocedg/specs/operations/verification-junit-inventory.json` | official updater: new selection, `final.desktop` refreshed, discovery evidence hashes |
| `geocedg/specs/operations/verification-registry.json` | phase selection, two nodes, catalog pin |
| `tools/agent/tests/verification-final-coverage.Tests.ps1` | registry-shape pins |
| this report, its JSON mirror, roadmap, mini-track plan | records |

No file below `source/**/src/main/**`, `models/**`, `packaging/**`, the
packaging builder or the packaging checks changes. Production-code delta:
none.

## 3. Stale assertions and the reconciled contract

`e1l09` carried **two** packaging-negation clauses from the E1-L checkpoint,
both superseded by the author-approved E1-P packaging contract:

```java
assertFalse(Files.readString(repository.resolve(
        "tools/release/build-windows-package.ps1")).contains("ggt-library"));
assertTrue(Files.readString(repository.resolve(
        "geocedg/specs/packaging/windows-packaging.md")).contains("`.ggb`/`.ggt` models"));
```

JUnit reported only the first (line 355), the first failing assertion; the
second also fails on the base, because E1-P replaced the sentence "`.ggb`/`.ggt`
models" with the controlled admission of the curated library. Both belong to the
same superseded "no packaging route" clause of the same test; no other test
failed (§5).

Why E1-P superseded them: E1-L owned the curated library content and runtime
catalog and explicitly did not own packaging; E1-P then made packaging the owner
of exactly one controlled route, `models/curated/ggt-library/` →
`app/ggt-library/`, under manifest membership, exact hash and rights/profile
admissibility, with every other `.ggt` rejected and `.ggb` still forbidden.

Reconciled test (same method, identifier kept, Javadoc records the
supersession):

- kept: no `.ggt` below `src/main/resources`; no Gradle build script mentions
  `models/curated`; 125 actions in the application profile; the
  `cedg.library.curated-ggt` feature is experimental, enabled by default and
  specified, and absent from the stable set;
- new cross-phase clause: the normative
  `geocedg/specs/packaging/windows-packaging.md` (whitespace-normalized) contains
  the controlled route — `` `models/curated/ggt-library/` ``,
  `` `app/ggt-library/` ``, "directly below `` `app/ggt-library/tools/` ``",
  the membership, hash and rights rules, "every other `` `.ggt` `` anywhere in
  the app-image fails the build verification", "any `` `.ggb` `` model" and
  "`` `COMMERCIAL` `` excludes it";
- minimal builder clause: the builder text names the canonical repository
  authority `models/curated/ggt-library` (no function name, formatting or
  algorithm is pinned).

The packaging behavior itself stays enforced by `packaging-product.ps1` and its
fixtures; the JUnit test does not duplicate it.

**Test identity choice.** The method keeps the identifier
`e1l09NoPackagingRouteNoBuildChangeAndUnchangedActionCatalog`, with a Javadoc
explaining that its "no packaging route" clause was superseded by E1-P and now
checks the specification-owned route. This is the least disruptive truthful
option: no JUnit identity changes, so no selection inventory changes for that
reason.

## 4. Focused and adjacent verification

| Check | Result |
|---|---|
| `PreG9BR6PlusE1LCuratedLibraryTest` | 9/9 PASS |
| `PreG9BR6PlusE1LBundledCatalogTest` | 10/10 PASS |
| adjacent E1-P registered phase `PHASE PRE-G9B-R6-plus-E1-P` (development, unchanged packaging) | `verification-ebf28a3edc8a45ca9f16cc9e2fc23945`, `ACCEPTED / COMPLETE`, 2/2 (`packaging.product`, `infra.contract-boundary`) |
| curated GGT files | not regenerated, not changed |

## 5. `final.desktop`

The official producer ran the full Desktop selection on the test-correction
commit `85e0aa4a` (the candidate adds no test or product change):

```text
final.desktop: COMPLETED, inner exit 0, 142 JUnit files
1 874 tests, 0 failures, 0 errors, 1 skipped
skipped = desktop::org.geogebra.CasValueExtractor::extractMockValues(String)::invocation[1/1]
          (the existing not_applicable_allowlist entry)
```

`PreG9BR6PlusE1LCuratedLibraryTest.e1l09` no longer fails. **FINAL.DESKTOP =
CLEAN.**

## 6. Inventory and pins

Official updater with clean evidence: discovery dry runs of `shared` and
`desktop` (identity counts unchanged: Desktop 1 880; only the evidence hashes
change), the executed `final.desktop` evidence and the executed new selection.

| Item | Before | After |
|---|---|---|
| `final.desktop` expected identities | 1 855 (stale since E1-X1, which could not refresh it) | 1 874 |
| `discovery.desktop` | 1 880 | 1 880 |
| `pre-g9b-r6-plus-e1-p-x1.desktop` | — | 19 (`PreG9BR6PlusE1LCuratedLibraryTest`, `PreG9BR6PlusE1LBundledCatalogTest`) |
| registry pin `junit_inventory` | `5de49549dd9d82b81ce75ff60b742e6e49c840231dfe627e5d67355edf7e5a55` (reproduced from the HEAD blob with `Get-VerificationCanonicalTextSha256`) | `eded218f82d1ae5d1df4456ec11c7794d0b522c65c7365452c65069f74c5ad79` |
| registry-shape pins | 41 selections, 49 PHASE selections | 42 selections, 50 PHASE selections, new selection count 19 |

No hash or count was entered by hand.

## 7. Registration

Phase `PRE-G9B-R6-PLUS-E1-P-X1`: `compile.shared.semantic`,
`compile.desktop.semantic`, the existing `packaging.product` leaf (agreement
with the current packaging authority) and
`junit.desktop.pre-g9b-r6-plus-e1-p-x1.semantic`. This is normal phase
registration; no verifier, check, schema, profile or static contract changes,
so the class stays `BOUNDED_PHASE` and is not
`OPERATIONAL_VERIFICATION_INFRASTRUCTURE`.

## 8. Verification summary

| Check | Result |
|---|---|
| development `INFRA_UNIT` (staged tree) | `verification-896164d2286246429abfbf174bd4eb2f`, `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -PlanOnly` | `COMPLETE`, plan `9016d9f6022ee7ea6dc0d30537a89849172fb8cdaccf1393eb7970b37870aa8d` |
| `git diff --check` | clean |
| `INFRA_UNIT`, `STATIC` and the registered `PHASE` on the candidate | run after freezing; reported outside this artifact |

## 9. Debt disposition (pending author approval)

All resolution conditions hold: corrected focal test PASS; the E1-P packaging
contract unchanged and valid (adjacent E1-P phase accepted); `final.desktop`
clean; inventory and pins reconciled from clean official evidence; no product
delta. Proposed disposition:

```text
OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION = RESOLVED IN PRE-G9B-R6-plus-E1-P-X1
                                              (technically; pending author approval)
```

## 10. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL (phase registration and routine
inventory/pin maintenance only)
GUIDE_IMPACT = NONE
PRODUCT CHANGE = NONE
```

E2 is not started and not authorized.
