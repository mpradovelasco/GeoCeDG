# PRE-G9B-R6-plus-E1-P — curated GGT library packaging and distribution inclusion

**CANONICAL PROMPT — PREPARED — UNEXECUTED AND NOT AUTHORIZED.**

The author's instruction of 2026-10-07 split `E1` into `E1-L` and `E1-P`,
fixed the planned `E1-P` packaging contract, packaging profiles and
verification class, and stated that **`E1-P` implementation is not
authorized**. The decisions are recorded in the
[E1 preparation closeout, author decisions and E1-L authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md)
(`DQ-E1-3`, `DQ-E1-12`, `DQ-E1-13`, `DQ-E1-16`) and the
[curated GGT library rights record](../../../docs/licensing/curated-ggt-library-rights-record.md)
(version 1). The existence of this file is not authorization. Execution needs
a new explicit author instruction naming `E1-P`, its exact base and any
remaining choice; that instruction may amend this prompt to the authorized
state as its first tracked edit.

This file is an execution contract, not a second policy document: the
verification classes are defined once in
`geocedg/specs/operations/verification-levels.md` §12.8, the packaging
contract once in `geocedg/specs/packaging/windows-packaging.md`, the library
contract once in `geocedg/specs/legacy/curated-ggt-library.md` (an `E1-L`
deliverable), the rights once in the rights record.

```text
PRE-G9B-R6-plus-E1-P =
PREPARED — NOT AUTHORIZED

identifier               = PRE-G9B-R6-plus-E1-P (author-accepted, DQ-E1-1)
registry phase id        = PRE-G9B-R6-PLUS-E1-P
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PLANNED_CLASS            = OPERATIONAL_VERIFICATION_INFRASTRUCTURE, impact PHASE_LOCAL
                           (DQ-E1-3; frozen only by the authorizing instruction)
PHASE_KIND               = PACKAGING / RELEASE LAYER; NO PRODUCT BEHAVIOR, NO GEOMETRY,
                           NO DOCUMENT FORMAT
DEPENDS_ON               = PRE-G9B-R6-plus-E1-L = PASS — AUTHOR APPROVED (hard);
                           rights record version 1 or a later author-approved version
G_DEPENDENCY             = G depends on E1-P; only a separate author decision may
                           re-plan that (DQ-E1-16)
```

<!-- geocedg-field: objective -->
## Objective

Make the packaged GeoCeDG product carry the approved curated GGT library at
exactly `app/ggt-library/`, where the `E1-L` bundled catalog reads it, while
the packaging contract stays fail-closed for every other `.ggt` and every
`.ggb`:

1. amend `geocedg/specs/packaging/windows-packaging.md` "Required exclusions":
   `.ggt` files are admitted only below `app/ggt-library/`, and each admitted
   file must be a shipped member of the staged `library-manifest.json`, have
   the exact expected SHA-256 and have an approved rights/provenance
   disposition in the rights record for the built distribution profile; every
   other `.ggt` stays forbidden; every `.ggb` stays forbidden; PDFs,
   `Templatev7.ggb`, repository documentation and non-Windows natives stay
   forbidden; no GGT content is hidden inside a JAR;
2. stage the shipped curated files and the manifest from
   `models/curated/ggt-library/` into `input/ggt-library/` of
   `tools/release/build-windows-package.ps1`, after the `legal/` copy and before
   `jpackage`, preserving the `E1-L` layout (`library-manifest.json`,
   `tools/<command>.ggt`); record them in the SBOM and the build manifest;
3. amend `packaging.portable-boundary` in
   `tools/agent/checks/packaging-product.ps1` to exclude exactly the admitted
   set from the forbidden scan, add a `packaging.ggt-library` subcontract
   (set equality between staged files, staged manifest, repository manifest
   and rights disposition; exact hashes), and name the new sources in
   `packaging.required-files`;
4. add positive and negative fixtures to
   `tools/agent/tests/verification-contract-boundary.Tests.ps1`
   (`infra.contract-boundary`): admitted shipped file; unlisted `.ggt`; hash
   mismatch; `.ggt` outside `app/ggt-library/`; listed but not shipped; rights
   not approved for the profile; `.ggb` anywhere; existing `.pdf`/`.ggb` cases
   unchanged;
5. update the licensing records the rights record requires (`NOTICE.md`,
   `THIRD_PARTY.md`, `docs/licensing/component-matrix.md`, the
   `assets-manifest.yml` exclusion wording, `LICENSES/manifest.json` only if a
   licence text is added);
6. run the packaging smoke that `AGENTS.md` §14 requires.

Packaging profiles (`DQ-E1-12`):

```text
PROFILE INTERNAL   = library included
PROFILE NC         = library included; redistributable under the approved NC profile
PROFILE COMMERCIAL = library excluded; profile remains not authorized
```

`INTERNAL` and `NC` carry the same functional library. No profile-dependent
geometry or tool behavior.

Invariant:

```text
INV-E1P-1  The app-image contains a .ggt if and only if it is a shipped,
           hash-matching member of the canonical curated library with an approved
           rights disposition for the built profile, located below
           app/ggt-library/; any other .ggt or any .ggb anywhere in the app-image
           fails packaging.portable-boundary.
```

<!-- geocedg-field: implementation_base -->
## Implementation base

Named exactly by the authorizing instruction: the published `E1-L` closeout
(`PRE-G9B-R6-plus-E1-L = PASS — AUTHOR APPROVED — PUBLISHED`), or a later
published commit the author names. Entry gate: local `main`, `origin/main` and
the live remote `main` agree with the named base; clean worktree; the rights
record version named by the instruction exists unchanged.

## Authority and evidence hierarchy

1. `AGENTS.md`, current code, build and packaging contracts at the base.
2. `verification-levels.md` and the typed registry.
3. The author records: authorization record, rights record, `E1-L` closeout.
4. The `E1` characterization report and the `E1-L` candidate report (evidence).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- `geocedg/specs/packaging/windows-packaging.md` (the admission amendment only).
- `tools/release/build-windows-package.ps1`; `packaging/windows/**` only where
  staging needs a declared input.
- `tools/agent/checks/packaging-product.ps1`;
  `tools/agent/tests/verification-contract-boundary.Tests.ps1`.
- Licensing records: `NOTICE.md`, `THIRD_PARTY.md`,
  `docs/licensing/component-matrix.md`, `geocedg/resources/assets-manifest.yml`
  exclusion wording, `LICENSES/**` with `LICENSES/manifest.json` if needed.
- Phase registration, registry-shape pins, candidate report, evidence, roadmap
  and mini-track status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Arbitrary JAR-entry inspection or any broadening of the boundary beyond the
  explicit `app/ggt-library/` payload; GGT content inside a JAR.
- Admitting any `.ggb`, any `.ggt` outside `app/ggt-library/`, any non-shipped
  or non-manifest `.ggt`, or any tool without an approved rights disposition.
- The library content itself (`models/curated/**`, icons), the bundled-catalog
  code and every product behavior (`E1-L` owns them); `models/legacy/**`.
- `PROFILE COMMERCIAL` inclusion or authorization.
- Kernel, document format, Desktop UI, action catalog.
- `E2`, `E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7`, `G9B`; the About surface and
  any product-version change (`ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`, owner `G`).
- Any legal conclusion; publication of any build.

## Architectural placement

Packaging/release layer (`AGENTS.md` §4 "Own installer") plus the packaging
product leaf and its verification-core fixtures.

## Required design/specification

The amendment of `windows-packaging.md` above; no other specification or ADR is
created by the agent.

## Geometric invariants and degeneracies

Not applicable: no geometric object, algorithm or dependency changes.

## Compatibility and serialization

```text
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE for documents; packaging records gain the
                                library payload
```

<!-- geocedg-field: required_checks -->
## Required tests and commands

Planned evidence (`DQ-E1-3`), on one clean immutable committed candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-P -LogDirectory <log root>
tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -VerifyPackagingArtifacts
                       -PackagingArtifactRoot <artifact set root> -LogDirectory <log root>
                       (once for INTERNAL and once for NC)
```

Packaging smoke on the built app-image and installer: install or portable run,
launch, the "GeoCeDG library" section lists exactly the shipped tools with
owned icons, activate and use one, save and reopen, uninstall leaves the user
store and the bundled-pin sidecar intact. `FINAL` is not planned while `E1-P`
stays within this boundary; a need for it is a
`VERIFICATION_ESCALATION_REQUEST`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction naming
`E1-P` and its exact base may authorize local implementation, the planned
verification and one frozen technical candidate for author review. It never
authorizes self-approval, author smoke by the agent, a class change, a legal
conclusion or any later subphase.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Building packaging artifacts for
verification is not publication. Push, merge, promotion, tag, release, installer
or binary publication and any distribution of the library each need a separate
explicit author instruction naming the exact candidate SHA.

## Stop conditions

Stop and report when the rights record does not cover a shipped tool for a
built profile; when the admission cannot stay fail-closed for every other
`.ggt`/`.ggb`; when the library would have to be hidden in a JAR; when the
change needs global verification-infrastructure changes (class escalation);
when licensing is unclear (`AGENTS.md` §16); or when `E1-L` is not
`PASS — AUTHOR APPROVED`.
