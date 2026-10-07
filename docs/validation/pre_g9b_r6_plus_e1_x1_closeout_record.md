# PRE-G9B-R6-plus-E1-X1 closeout record

```text
PRE-G9B-R6-plus-E1-X1 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE          = PASS
OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER = RESOLVED IN PRE-G9B-R6-plus-E1-X1 — AUTHOR APPROVED
OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION     = OPEN — NEXT AUTHORIZED ACTIVITY IS E1-P-X1
```

```text
selfApproved              = false
authorApproved            = true    (E1-X1 only, technical candidate T_R6PLUS_E1_X1)
passClaimed               = true    (E1-X1 only)
implementationAuthorized  = false   (E1-P-X1 implementation, E2, E3, F1, F2, F3, G,
                                     PRE-G9B-R7, G9B and every other open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE             = YES — DIAGNOSTIC LAUNCH ROUTE
PACKAGING PRODUCT CHANGE   = NONE
SERIALIZATION CHANGE       = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
DOCUMENT FORMAT CHANGE     = NONE
KERNEL CHANGE              = NONE
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-07 on
`PRE-G9B-R6-plus-E1-X1`, given in the session after the author's manual smoke
of the frozen technical candidate and confirmed in writing ("Autorizo el
siguiente cierre y publicación"), with the exact identities behind it. It
changes no product, test, build, packaging, verifier, registry, schema,
specification, ADR or upstream file and does not modify any frozen commit.
Besides this record and its machine-readable mirror,
[`pre-g9b-r6-plus-e1-x1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-x1-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_e1_x1_candidate_report.md), its evidence
mirror, the [authorization record](pre_g9b_r6_plus_e1_x1_authorization_record.md),
the canonical prompt, the
[characterization report](pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md)
and the [R1 launcher comparison report](pre_g9b_r6_plus_e1_x1_r1_launcher_comparison_report.md)
record only frozen state and are not amended; Candidate A remains in them as
characterization history. The `E1`, `E1-L` and `E1-P` closeout records are not
rewritten. This record is the sole authority for the author approval of
`E1-X1`.

## Entry verification

```text
live remote main = origin/main = local main
                 = fdc1ade6e7ab707bbc919d337bc48d1abce90a1b   (P_R6PLUS_E1)
                   tree cc613c0337d3d75fdf14e55d20db5cb896af2b52
branch           = phase/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher, clean
history          = fdc1ade6 → b6172af4f319d297aa5780c9f516b310254d3e90 (initial characterization)
                            → b3c78e6d6564b0400d1fea7cda55bb480778716c (A-versus-C micro-characterization)
                            → 51bb677e8b61c149dd3ae1e1c6a491e53732d5dc (author authorization)
                            → 7fd6adbbdc6dcf7cc34b795c0225522744ff7397 (implementation)
                            → b12ab32c3a4dcf42941617e95d06a92bfb3b2955 (frozen technical candidate)
                   linear; no commit amended, squashed or rewritten
```

## Author disposition

```text
T_R6PLUS_E1_X1   = b12ab32c3a4dcf42941617e95d06a92bfb3b2955
                   tree 34fa96d9ddb37ea5a3a8addeb04e4b920d322b43
SELECTED DESIGN  = CANDIDATE C — PRIMARY LAUNCHER EARLY CLASSIC DISPATCH
AUTHOR_SMOKE     = PASS
AUTHOR_APPROVAL  = PRE-G9B-R6-plus-E1-X1 = PASS — AUTHOR APPROVED
selfApproved     = false
```

The author smoke-tested the frozen candidate manually and confirms: **Open
Classic diagnostic session** works from the packaged product and opens genuine
Classic in a separate process; GeoCeDG remains usable; **Legacy laboratory**
opens `Templatev7.ggb` without hanging; diagnostic preference isolation is
preserved; no active GeoCeDG construction is silently transferred or
reassigned.

### `DQ-E1X1-4`

The author accepts the implemented narrow dispatch behavior and freezes the
private marker `--classic-diagnostic`: internal launcher protocol, not a public
GeoCeDG construction command; recognized only as the exact first argument; the
marker is removed before delegation to Classic; the remaining arguments are
preserved; `--showSplash=false` and the isolated settings file are ensured only
when absent; the product routes already pass both explicitly. No broader
command-line API is implied.

### Approved semantics

- **Packaged execution.** With `jpackage.app-path` present, the separate
  diagnostic Classic process is started through the running packaged
  `GeoCeDG.exe`; `java.home`, `PATH`, the working directory and the repository
  path are never fallback authorities; an invalid path fails explicitly.
- **Development execution.** Without `jpackage.app-path`, the explicit Java
  route to `org.geogebra.desktop.GeoGebra3D` propagates only the
  `--add-exports`, `--add-opens` and `--enable-native-access` families; arbitrary
  parent JVM flags are not child authority.
- **Child streams.** `stdout = Redirect.DISCARD`, `stderr = Redirect.DISCARD`;
  no default pipes, no `inheritIO`, no persistent log file.
- **Preferences.** Open Classic → `classic-diagnostic.properties`; Legacy
  laboratory → `laboratory.properties`; GeoCeDG → `preferences.properties`;
  three distinct authorities.

## Observation dispositions

```text
OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER = RESOLVED IN PRE-G9B-R6-plus-E1-X1 — AUTHOR APPROVED
    pre-existing (since b49219408, G9U1); not caused by E1, E1-L or E1-P
X1-F2 (child stdout/stderr pipe blockage)                         = RESOLVED IN E1-X1
X1-F3 (development child missing JVM access/native options)        = RESOLVED IN E1-X1

OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION = OPEN
    PRE-EXISTING AT X1 BASE
    INTRODUCED BY THE LEGITIMATE E1-P PACKAGING EVOLUTION (dcda4075)
    NOT CAUSED BY E1-X1 — NOT BLOCKING E1-X1 — MUST BE RESOLVED BEFORE E2
    PreG9BR6PlusE1LCuratedLibraryTest.e1l09 still expects build-windows-package.ps1
    not to contain "ggt-library", contradicting the approved E1-P packaging contract
final.desktop = KNOWN RED AT X1 BASE DUE TO THIS STALE ASSERTION
```

No global clean verification is claimed until `PRE-G9B-R6-plus-E1-P-X1`
resolves it. The `final.desktop` inventory selection keeps its base values
because the official updater rejects failing executed evidence.

## Packaging disposition

```text
PACKAGING PRODUCT CHANGE = NONE
```

No secondary launcher, secondary `.cfg`, shortcut, file association, SBOM
launcher entry or ADR 0004 change. The canonical packaging pipeline is
unchanged; the technical smoke showed `INTERNAL` and `NC` compatibility and the
packaging contract stayed `CONTRACT_SATISFIED` 42/42 for both.

## Accepted evidence

```text
PHASE PRE-G9B-R6-plus-E1-X1 on T_R6PLUS_E1_X1
    verification-1286494a11e74e87be799868dc2bfcd3, exit 0, ACCEPTED / COMPLETE, 3/3
    plan   59ec02958e51287e927a7088d2db0f62a21493e0963b0ddb3eb05703e2595a74
    result 64cae5b555b522361f185599704b2f33fd5333c227f8cbb6b38c6f7e08b5314f
STATIC on T_R6PLUS_E1_X1
    verification-4be6d668dcd540cbbb3b1f638e73f1de, ACCEPTED / COMPLETE, 3/3
    (standing governance FINDING and historical-consistency UNAVAILABLE)
development INFRA_UNIT   verification-701a3e0a393f4cbf80b4573834d016ad, 22/22
focal unit tests 17/17; process tests 2/2; phase selection 43/43
packaged technical smoke PASS (INTERNAL and NC app-images and ZIPs)
```

No `FINAL` or `INTEGRATION` was run for this closeout. The closeout commit is a
documentary descendant of `T_R6PLUS_E1_X1`, not a replacement technical
candidate; its `STATIC` run and the publication are reported outside this
record, which cannot name its own commit.

## Next activity

After publication, the only next authorized activity is
`PRE-G9B-R6-plus-E1-P-X1`: reconcile the stale `E1-L` packaging-negation
assertion and restore `final.desktop` to a clean baseline. This record does not
authorize its implementation. `E2` is not started and not authorized.
