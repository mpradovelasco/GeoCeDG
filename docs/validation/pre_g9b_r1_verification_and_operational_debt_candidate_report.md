# PRE-G9B-R1 verification and bounded operational debt candidate report

## Candidate identity and authority

```text
PHASE                    = PRE-G9B-R1
PHASE_KIND               = VERIFICATION_AND_BOUNDED_OPERATIONAL_DEBT
CHANGE_ROUTE             = ORDINARY
VERIFICATION_CLASS       = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
frozenAtPhaseStart       = true
PRODUCT_PHASE_EFFECT     = NONE
selfApproved             = false
authorApproved           = false
passClaimed              = false
state                    = CANDIDATE — PENDING AUTHOR REVIEW
```

Implementation base: commit `78983cab36ca0cafbb63b3b1bec7392bce5c0424`, tree
`373db9bcafec6b569395a3e368f88a813e4790d9`, branch
`feature/pre-g9b-r1-verification-and-operational-debt`, worktree clean at entry,
`origin/main` at the same commit.

This report is evidence, not authority, and technical acceptance never creates
author approval. Machine-readable evidence:
[`geocedg/validation/pre-g9b-r1/pre-g9b-r1-verification-evidence.json`](../../geocedg/validation/pre-g9b-r1/pre-g9b-r1-verification-evidence.json).

## Slice 1 — `TD-VERIFY-RECEIPT-RECOVERY`

`verify.ps1` produced no acceptance receipt, so a FINAL campaign left no durable,
identity-bound artifact behind. The receipt module itself was already complete:
`New-VerificationAcceptanceReceipt` already refused a report that was not
`COMPLETED` / `ACCEPTED` / `COMPLETE`, refused an invalid result hash, refused any
untrusted or not-run check, refused a coverage-set mismatch and refused a profile
the report did not establish, and `Write-VerificationAcceptanceReceipt` already
refused to overwrite. What was missing was a caller and two of the four identity
arguments.

R1 therefore adds **no new gating and no second acceptance system**. It adds the
missing producer and the missing call:

- `Get-GeoCeDGVerificationAuthorityIdentity` in
  [repository-input-identity.ps1](../../tools/agent/repository-input-identity.ps1),
  which derives the checker cohort from the same exact-commit tracked identity
  ADR 0024 already defines;
- receipt emission in [verify.ps1](../../tools/agent/verify.ps1), guarded on a
  `FINAL` report that is `COMPLETED` / `ACCEPTED` / `COMPLETE`.

### The four identities

The architecture already distinguishes plan, checker, command and consumed-input
identity. R1 supplies the two that had no producer, under the author's disposition:

| Identity | Definition in R1 |
|---|---|
| `execution_plan_hash` | unchanged — the resolved plan |
| `command_identity` | unchanged — per-check argv and environment |
| `checker_identity_hash` | **Option D** — tracked blobs under `tools/agent/`, plus the typed registry and the governing verification schemas |
| `input_identity_hash` | unchanged existing complete tracked-repository identity |

The checker cohort is canonicalized as `path`, Git mode and blob OID per entry
under ordinal ordering, hashed with SHA-256, and tagged
`EXECUTABLE_VERIFICATION_AUTHORITY_V1`. A missing governing path or an empty
cohort is an error, never a silent pass.

The overcoverage is intentional and author-accepted: `tools/agent/` contains
files that do not participate in every run, so the checker identity can change
when a given run's behaviour did not. Per the author's disposition, false
invalidation is preferable to silently retaining the same checker identity after
verification authority has changed.

At the base commit the two identities were
`checker_identity_hash = 2967272ba6f1f5928430658f05d401d8068e7665b9ccdb0a2b4889e65f459752`
over 110 blobs and
`input_identity_hash = 056d81e6a537293c10efdd00a4ec90feabcec4037023afb19250594e523c220e`
over 11 575 blobs. Both necessarily change at the candidate commit; the FINAL
receipt binds the candidate's own values.

Three focal cases were added to
[verification-receipt.Tests.ps1](../../tools/agent/tests/verification-receipt.Tests.ps1):
that the two identities are deterministic and distinct, that the cohort is
exactly the declared verification authority, and that a receipt claims only the
profile the accepted report established.

## Slice 2 — `TD-P1-PACKAGING-SUMMARY`

[build-windows-package.ps1](../../tools/release/build-windows-package.ps1) printed
a public-redistribution line unconditionally, so an `INTERNAL` build reported the
`NC` profile's redistribution status. The summary is now derived from the
selected profile's own `redistributable` flag:

| Selected profile | Printed summary |
|---|---|
| `NC` | `ALLOWED UNDER PROFILE NC` |
| `INTERNAL` | `BLOCKED PENDING LICENSE/ASSET APPROVAL` |

Both were observed directly. No licensing, trademark or profile semantics
changed; only the reporting of an already-decided profile attribute changed.

Because the changed file is itself pinned by `packaging.source-authority`, its
pin was repinned to the new canonical-LF SHA-256
`2b158b0fe433ff8b27fce4f711435256e430ad4021ecb9bdf2f285e087ac0281`, and the
coupled registry catalog pin was updated with it. `TD-P1-PACKAGING-SUMMARY` is
recorded as closed in
[the operations manual](../developer/geocedg_operations_manual.md) with the
identifier still named, so the existing bilingual guide regression continues to
resolve it.

## Slice 3 — `TD-G9X1-HISTORICAL-PIN`

[verify-g9x1-extended-dxf.ps1](../../tools/agent/verify-g9x1-extended-dxf.ps1)
carried a bare historical specification hash with no statement of what had
superseded it. It now states the supersession explicitly and **proves** the
historical hash at run time from the frozen blob rather than asserting it as a
literal:

```text
G9X1 specification authority: historical e5de67f5… frozen at f528b2dc…;
successor f2a0cacc… superseded by e5260fc7… (PRE-G9B-S1-R1).
```

The historical hash was **not** replaced with the current hash. The retired
`docs/user/geocedg_user_guide.md` assertions were retargeted to the two official
editions, matching on stable technical literals, the `dxf-export` section marker
and the `--enableExtendedDxf=false` flag rather than on translated prose, with
one language-specific default sentence matched on whitespace-normalized text
because it wraps across a line break in the Spanish edition.

## Pre-existing conditions found during R1

Three conditions were discovered during R1 execution. **None was caused by R1.**
Each was reported to the author before any change was made, and each was applied
only under an explicit author disposition.

### 1. `assets-manifest.yml` pin drift — the only FINAL blocker

`INFRA_UNIT` returned `REJECTED_VERIFICATION_CORE` at the base commit.
`packaging.source-authority` pinned `packaging/assets-manifest.yml` at
`4d6f2851…` while the blob hashed
`c3c80f30943b3179e76ee2a417cfa0f739d18099ec46a649f38160b447202ee8`.

Provenance was established by evaluating all 17 static-contract pins against
their HEAD blobs; exactly one drifted. The origin is commit `0d13434cf`
*"docs: reconcile controlled source provenance"*, which added a 162-line
`retained_branding_source_alternatives` section to the manifest without repinning
the coupled verification contract.

Because a repin is a licensing-adjacent disposition, it was not applied
unilaterally. Under the author's authorization it was applied as
verification-authority reconciliation only: `assets-manifest.yml` itself was not
modified, and no licensing, trademark or profile semantics changed. `INFRA_UNIT`
is `ACCEPTED` / `COMPLETE` after the repin.

### 2. G9X1 living-guide assertions

The verifier asserted six fragments against `docs/user/geocedg_user_guide.md`,
which POST-P1-DOC-HELP had converted into a 2 015-character index while splitting
the living guide into `_en` and `_es` editions; all six were absent at HEAD. This
affected the standalone G9X1 verifier only and was not a FINAL blocker. Retargeted
under author authorization; no guide content was modified.

### 3. `dxf-authority-validation.ps1` substring rule

[dxf-authority-validation.ps1](../../tools/agent/dxf-authority-validation.ps1)
tested the view prohibition with a naive `Contains('EuclidianView')`. After the
two approved presentation-only fragments were correctly stripped — both still
matched exactly once — **zero references to the `EuclidianView` type remained**,
yet 13 substring matches survived inside ordinary method identifiers introduced
by PRE-G9B-P0/P1 presentation work in commit `e1672b22e`:

| Identifier | Occurrences |
|---|---|
| `getEuclidianView1()` | 4 |
| `getActiveEuclidianView()` | 3 |
| `getEuclidianViewFontSize` | 2 |
| `hasEuclidianView2EitherShowingOrNot` | 2 |
| `getEuclidianView2(1)` | 2 |

This was confirmed **not** to be a FINAL blocker before any change was proposed.
No registry node anywhere references `dxf-authority-validation.ps1` or
`verify-g9x1-extended-dxf.ps1`; FINAL requires 67 nodes, none of them among them;
`Get-GeoCeDGAcceptancePhaseIntegration` declares an integration for
`VERIFICATION-INFRASTRUCTURE` only and throws for every other phase, `G9X1`
included; and although `verify-g9u0-r2-product-refinement.ps1` lists the G9X1
verifier among its historical verifiers, that script is itself outside the
registry and the registry's `G9U0-R2` phase selection requires only ordinary
compile, junit, python and static nodes.

Under the author's disposition, the normative G9X1 prohibition concerns use of
`EuclidianView` — viewport or presentation state — as DXF geometric authority,
and does not prohibit ordinary Java method identifiers whose names merely contain
the substring. The rule now matches the view prohibition as a case-sensitive
whole token, `\bEuclidianView\b`. The guard, the approved-fragment stripping and
every other prohibited token are unchanged, and G9X1 product semantics, DXF
behaviour and historical evidence are untouched.

Four focal cases were added to
[dxf-authority-validation.Tests.ps1](../../tools/agent/tests/dxf-authority-validation.Tests.ps1)
pinning both directions: a declared type reference, a qualified type reference
and a type cast are still rejected, and a probe exercising all four method
identifiers is accepted.

## Validation and operational impact

Four focused gates, run in the authorized order against the working tree while
`HEAD` was still the base commit, so each binds
`candidate_commit = 78983cab…`. These are pre-candidate evidence; the FINAL
campaign is the candidate-bound acceptance evidence.

| # | Gate | Exit | Result |
|---|---|---|---|
| 1 | `tools/agent/tests/verification-receipt.Tests.ps1` | 0 | 14 cases, 69 assertions, 0 failures |
| 2 | `verify.ps1 -Profile STATIC` | 0 | `ACCEPTED` / `COMPLETE`, run `verification-b50f57dd59f54368aac69f9b96d57a57`, required 3/3, untrusted 0, not-run 0 |
| 3 | `verify.ps1 -Profile INFRA_UNIT` | 0 | `ACCEPTED` / `COMPLETE`, run `verification-fd077491803d45849f5390aba32d026b`, required 21/21, untrusted 0, not-run 0 |
| 4 | `verify-g9x1-extended-dxf.ps1 -SkipBuild` | 0 | boundary mode `TAGGED_DESCENDANT`; DXF authority 61/61, SHA-256 `6892afe3…` |

STATIC's single diagnostic finding is `diagnostic.governance`, which emits
`DIAGNOSTIC_FINDING` unconditionally with a constant cause, and
`diagnostic.historical-consistency`, which emits `DIAGNOSTIC_UNAVAILABLE`
unconditionally. Neither says anything about this candidate.

Gate 4's own `G9X1 = PASS — AUTHOR APPROVED` line reports the historical G9X1
gate status the script was written to assert. It is not a PRE-G9B-R1 approval
claim, and R1 claims none.

```text
PRODUCT_PHASE_EFFECT               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = NONE
KERNEL / GEOMETRY IMPACT           = NONE
SERIALIZATION IMPACT               = NONE
LICENSING / TRADEMARK SEMANTICS    = NONE
```

No kernel, geometry, Locus V2, intersection, metric, spatial, redefine or
serialization path was touched. No user-facing behaviour changed, so the user
guide needs no update; the G9X1 verifier was retargeted to the existing `_en` and
`_es` editions and asserts their existing content.

## Authorization state

`PRE-G9B-R1-R1` through `R7`, `R2-E0`, `R2-E1`, `G9B`, `G9C`, `G9U2`, productive
`G10` and further `G12` remain unauthorized. No successor phase is authorized.
Branch publication, merge, promotion to `main`, tag, release, binary publication
and commercial distribution are all outside this phase and each requires a
separate explicit author instruction naming the exact candidate SHA.

The phase stops with a candidate pending author review. Author approval is an
explicit decision naming the exact accepted commit.
