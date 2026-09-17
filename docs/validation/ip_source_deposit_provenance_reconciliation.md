# IP source-deposit provenance reconciliation

Date: 2026-09-17

## Purpose and boundary

This record closes the source-provenance inventory gap found by the bounded IP
source-deposit audit. It reconciles the controlled upstream boundary only; it
does not reopen PRE-G9B-P1, POST-P1-DOC-HELP, or any accepted product gate and
does not approve a release or make a legal ownership determination.

The maintenance change is limited to the upstream modification register, the
asset provenance register, and this validation record. No productive source,
test, image, resource, build, version, licensing-policy, trademark-policy, or
roadmap-state byte is changed.

## Frozen starting identity

| Item | Value |
|---|---|
| Product/version | GeoCeDG 1.0.0 |
| Starting published `main` | `455f6503c688e9f57b74adb60926f43c5f883086` |
| Starting tree | `21c03c679e23d6a2650a2ea33d76edb6df4bcf0e` |
| Pinned GeoGebra baseline | `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Baseline tag | `geogebra-baseline-5.4.928.0` |
| Repository | `https://github.com/mpradovelasco/GeoCeDG` |

The entry gate independently confirmed a clean `main`, local `main ==
origin/main == GitHub refs/heads/main`, ahead/behind `0/0`, one registered
worktree, and no merge, rebase, cherry-pick, revert, or bisect operation.

## Reproduced discrepancy

The original register contained 721 entries: 621 `added` and 100 `modified`.
An independent `git diff --name-only` from the pinned baseline to the starting
commit found 775 controlled-boundary paths. All 721 declarations were real
differences, correctly classified, unique, and supplied with non-empty
`purpose` and `authority`; 54 actual paths were undeclared.

Git object existence classified the undeclared paths as:

- 50 `added`: 24 productive Java files, 16 tests, and 10 retained branding
  source alternatives;
- 4 `modified`: `EuclidianController.java`, `ToolManagerDialogModel.java`,
  `MacroManager.java`, and `GlobalKeyDispatcher.java`.

The exact added paths and their phase-specific purposes and authorities are the
54 new records in `docs/upstream/modified-files.yml`. Their authorities are the
existing POST-G9U1-A1/A2/A3/A4/A5/A6/A7, PRE-G9B-S1/P0/P1, POST-P1-DOC-HELP,
and asset-manifest records. Git history was used to identify each introducing
commit; namespace and filename were not used as ownership evidence.

The reconciled register contains 775 entries: 671 `added` and 104 `modified`.
Direct Git path equality is exact: no undeclared differences, no declarations
without a difference, no duplicate paths, and no missing required metadata.

## Branding alternatives

`geocedg/resources/assets-manifest.yml` now inventories the ten files under
`branding/v1/source/alternatives` with exact path, byte count, dimensions, and
SHA-256. Repository history establishes that the author added the complete set
in commit `ed7558ea18b02656194458dbf58990b98bde6910`.

Only `GeoCeDG_splash_06_1197x1591.png` has stronger versioned evidence: commit
`a3afd04ce9044dcf74c14862532c64e77710b908` selected it and promoted it
byte-exactly as the source of the active startup asset. It retains the existing
`geocedg-official-mark-artwork` classification and official-product-branding
role. The other nine files remain retained alternative/source evidence and are
not assigned an active runtime or figurative-mark role.

No versioned evidence establishes the upstream creation method or conventional
human authorship of those nine alternatives. The filename containing
`ChatGPT` is recorded as a filename only and is not treated as proof of AI
generation, ownership, authorship, or license. The size variants are associated
by basename, but no exact transformation method is asserted. Those unresolved
alternatives are excluded from the original-GeoCeDG-artwork scope of the IP
source deposit. No image was regenerated, recompressed, resized, renamed, or
otherwise changed.

The GeoCeDG word-mark filing remains denominative and pending. No figurative
application or registration is inferred or created by this reconciliation.

## Root cause

The controlled-boundary register was not updated in several later accepted
implementation/documentation commits that added or modified files under
`source/`. PRE-G9B-P0 updated the register for part of its own change but also
omitted several added theme files. This was a distributed bookkeeping omission,
not evidence that the files were unchanged upstream or that their product
contracts were unapproved. The reconciliation records existing Git facts and
existing phase authorities; it changes no earlier gate outcome.

## Validation

The following commands were run against the bounded maintenance diff:

```powershell
# Independent baseline/deposit path and object-existence comparison
git diff --name-only 9b93256b7df401ff056c37b502d82df4d72b1522..455f6503c688e9f57b74adb60926f43c5f883086 -- source gradle gradle.properties settings.gradle.kts gradlew gradlew.bat doc/dev

# Canonical controlled-boundary authority
. ./tools/agent/upstream-boundary.ps1
Assert-GeoCeDGUpstreamBoundary -RepositoryRoot (Resolve-Path '.').Path -ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522

# Canonical focused profiles
pwsh -NoLogo -NoProfile -File ./tools/agent/verify.ps1 -Profile STATIC -LogDirectory C:/Temp/geocedg-ip-provenance-static
pwsh -NoLogo -NoProfile -File ./tools/agent/verify.ps1 -Profile PACKAGING -LogDirectory ./artifacts/verification/ip-provenance-packaging

git diff --check
```

Results:

- JSON-compatible manifest parse and required-field checks: `PASS`;
- independent path/classification reconciliation: `PASS`, 775/775;
- canonical upstream boundary: `PASS`, 775 registered files;
- ten alternative asset path/hash/size checks: `PASS`;
- STATIC run `verification-87f6d2c5bad64f19ba0ab8acc549d24e`: exit 0,
  `ACCEPTED / COMPLETE`; one non-blocking governance diagnostic;
- PACKAGING inspect-only run under the canonical ignored repository evidence
  root: exit 0, `ACCEPTED / COMPLETE`, zero diagnostics; product, asset,
  license-bundle, toolchain, and repository-preservation contracts satisfied;
- `git diff --check`: exit 0;
- changed-path guard: only the two provenance manifests and this record;
  no productive source, test, or resource byte changed.

An earlier PACKAGING invocation with its log root under `C:\Temp` was retained
as diagnostic evidence at `C:\Temp\geocedg-ip-provenance-packaging`. Its product,
asset, licensing, and toolchain contracts passed, but the wrapper correctly
rejected the noncanonical external task-output root as untrusted repository
preservation evidence. No project-code change was made in response; the exact
same profile then passed from the required ignored repository evidence root.

## Impact disposition

- Affected layer: provenance/governance documentation only.
- Semantic effect: none.
- Compatibility effect: none.
- Bootstrap impact: no change; no prerequisite, toolchain, environment, or
  bootstrap assumption changed.
- Infrastructure impact: no executable verifier, registry, build, or packaging
  behavior changed.
- Real-runtime evidence: not applicable; validation is static/provenance and
  inspect-only packaging authority.
- Author approval: not claimed by this maintenance record.
