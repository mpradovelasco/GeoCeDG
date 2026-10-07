# PRE-G9B-R6-plus-E1-P and PRE-G9B-R6-plus-E1 closeout record

```text
PRE-G9B-R6-plus-E1-P   = PASS — AUTHOR APPROVED
AUTHOR_SMOKE_E1P       = PASS WITH ACCEPTED PRE-EXISTING DEBT
PRE-G9B-R6-plus-E1-L   = PASS — AUTHOR APPROVED — PUBLISHED (unchanged)
PRE-G9B-R6-plus-E1     = PASS — AUTHOR APPROVED (aggregate of E1-L and E1-P)
OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER
                       = OPEN — PRE-EXISTING — CHARACTERIZATION AUTHORIZED NEXT
```

```text
selfApproved              = false
authorApproved            = true    (E1-P technical candidate T_R6PLUS_E1_P; aggregate E1)
passClaimed               = true    (E1-P and aggregate E1 only)
implementationAuthorized  = false   (E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B, and any
                                     correction of OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER)

PACKAGING CHANGE              = YES
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
DOCUMENT FORMAT CHANGE        = NONE
E1-L LIBRARY CONTENT CHANGE   = NONE
ACTION CATALOG CHANGE         = NONE
newHeavyVerification          = NONE (closeout)
```

This record preserves the explicit author disposition of 2026-10-07 on
`PRE-G9B-R6-plus-E1-P` and on the parent `PRE-G9B-R6-plus-E1`, given in the
session as a typed instruction to execute the closeout ("Ejecuta la siguiente
tarea"), with the exact identities behind it. The closeout changes only status
documentation; it changes no product, test, build, packaging, verifier,
registry or schema file.

## 1. Provenance chain

```text
P_R6PLUS_E1_L   = 47b39e5f6d51e51b8d08ec5622305f11c1dab10f  tree d48c86c6f65b642a53609fb15f917bc587ff2ce7
                  (published base; E1-L = PASS — AUTHOR APPROVED — PUBLISHED)
AUTHORIZATION   = 6bce8cf56f064f1e347c6362d5d6238ea1ae4782  tree 94198c3dd9a53b623c17c73997923eb48ad99581
T_R6PLUS_E1_P   = dcda4075843766e8c384c176007c5edb776357c2  tree 90eb1c73a11b423c6e08352f3b31702f627bfb18
                  (the approved technical candidate)
CLOSEOUT        = the commit that adds this record, child of T_R6PLUS_E1_P
```

No commit of the chain is amended or rewritten. The technical acceptance stays
bound to `dcda4075`; the closeout commit is not a substitute candidate.

## 2. Acceptance evidence (frozen candidate `dcda4075`)

| Evidence | Result |
|---|---|
| `PHASE PRE-G9B-R6-plus-E1-P` | `verification-81ffec9c231b4bc197d4c9ae964d59e9`, `ACCEPTED / COMPLETE`, 2/2 |
| `INFRA_UNIT` | `verification-ace3de8716e74e2299c356b75c767141`, `ACCEPTED`, 22/22 |
| `PACKAGING -CheckToolchain` | `verification-e3c98ad176ef41f089dd0539197e8600`, `ACCEPTED`, 3/3 |
| `PACKAGING -VerifyPackagingArtifacts`, INTERNAL set built from `dcda4075` | `verification-a40cda69fff14f6fb26027e67e71b176`, `ACCEPTED`, packaging product 42/42 |
| `PACKAGING -VerifyPackagingArtifacts`, NC set built from `dcda4075` | `verification-ce43e964f87d4531a67cf4bb64b86eb7`, `ACCEPTED`, packaging product 42/42 |
| technical packaging smoke (agent; INTERNAL app-image, NC app-image, NC portable ZIP) | `PASS` |
| `COMMERCIAL` build attempt | refused before any build work; output root unchanged |

No `FINAL` was required (class `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`,
`PHASE_LOCAL`). Pre-freeze evidence is in the
[candidate report](pre_g9b_r6_plus_e1_p_candidate_report.md).

## 3. Author disposition

- `AUTHOR_SMOKE_E1P = PASS WITH ACCEPTED PRE-EXISTING DEBT`;
  `PRE-G9B-R6-plus-E1-P = PASS — AUTHOR APPROVED`.
- Accepted by the author's smoke: the INTERNAL library present and functional;
  the NC library present and functional; exactly the twelve approved tools;
  icons and hover tips correct; tool activation, pinning, and save/reopen work;
  no `E1-L` content regression observed.
- The accepted debt (§5) is unrelated to the GGT packaging functionality.

## 4. Approved packaging contract

```text
PROFILE INTERNAL   = curated GGT library included
PROFILE NC         = the same curated GGT library included
PROFILE COMMERCIAL = library excluded; the profile remains not authorized
```

The packaged library is exactly `library-manifest.json` and the twelve approved
`.ggt` tools at `app/ggt-library/`, admitted per shipped manifest member,
exact SHA-256 and rights disposition for the built profile (rights record
version 1). The fail-closed rules for every other `.ggt`, every `.ggb` and the
other forbidden payload are preserved
(`geocedg/specs/packaging/windows-packaging.md`, `packaging-product.ps1`). No
package or installer is published by this closeout.

## 5. Pre-existing debt registered

```text
OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER =
  PRE-EXISTING
  REPRODUCED BY AUTHOR IN A MID-SEPTEMBER GEOCEDG BUILD
  NOT CAUSED BY E1-P
  NOT CAUSED BY E1-L
  NOT BLOCKING E1-P
  NOT RESOLVED
```

Observed in the packaged product: opening the Classic diagnostic session fails
when the child process is launched (`CreateProcess error=2`); the attempted
executable is equivalent to `<packaged java.home>\bin\java`. Current source:
`GeoCeDGActionRegistry.openDiagnostic(...)` derives the secondary JVM launcher
from `System.getProperty("java.home")/bin/javaw.exe`, falling back to
`System.getProperty("java.home")/bin/java`; the packaged jpackage runtime does
not provide a child Java launcher at that assumed path. This is not an `E1-P`
regression and is not fixed here. The author authorizes, after the `E1`
publication, only its focal characterization as the next activity; no
correction is authorized. The characterization is not part of this closeout.

## 6. Aggregate `PRE-G9B-R6-plus-E1`

`E1` was split by the author on 2026-10-07 into two acceptance units:

| Unit | State |
|---|---|
| `PRE-G9B-R6-plus-E1-L` | `PASS — AUTHOR APPROVED — PUBLISHED` (`T_R6PLUS_E1_L` `5f34d181`, [closeout record](pre_g9b_r6_plus_e1_l_closeout_record.md)) |
| `PRE-G9B-R6-plus-E1-P` | `PASS — AUTHOR APPROVED` (`T_R6PLUS_E1_P` `dcda4075`, this record) |

With both units closed, `PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED`. The
Classic diagnostic observation does not reopen `E1`: it is proven
pre-existing and outside `E1-L`/`E1-P` semantics, and it stays an open
mini-track debt. Preserved unchanged: `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`
(`E2`-owned constraint); `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` (owner `G`;
visible version `1.0.0` until `G = PASS — AUTHOR APPROVED`, then `1.1.0`);
the curated-library feature `cedg.library.curated-ggt` stays `experimental`.
`G` depended on `E1-L` and `E1-P`; both are now closed. `E2` is not started and
not authorized.

## 7. Publication

Authorized by the same instruction: an ordinary non-force fast-forward of
`main` from `47b39e5f` through `6bce8cf5` and `dcda4075` to the closeout
commit; no merge commit, no force push, no tag, no release, no binary
publication. The live remote `main` is verified independently after the push;
that verification is reported with the closeout, outside this artifact, which
is part of the published commit.
