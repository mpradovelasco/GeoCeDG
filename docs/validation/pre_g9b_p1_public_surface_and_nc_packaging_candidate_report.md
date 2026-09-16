# PRE-G9B-P1 public surface and PROFILE NC packaging readiness

## Candidate identity and authority

- Published entry before P1: `1c97e78057c5fc706a454441201c7102a7ea3af6`,
  tree `b0a150037ca45fb166e5b6a02e221e5ca071c0e1`.
- Required predecessors: `PRE-G9B-D1 = PASS — AUTHOR APPROVED`,
  `PROFILE NC = APPROVED`, `PRE-G9B-P0 = PASS — AUTHOR APPROVED`.
- Branch: `feature/pre-g9b-p1-public-surface-and-nc-packaging`.
- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
- `selfApproved=false`, `authorApproved=false`, `passClaimed=false`.
- Validation matrix: [pre_g9b_p1_validation_matrix.md](pre_g9b_p1_validation_matrix.md).

P1 changes **policy and distribution packaging only**. No geometric semantics,
no serialization, no identity, no version, and nothing closed by P0.

## Entry gates verified from the versioned authorities

| Gate | Observed |
|---|---|
| `PRE-G9B-D1` | `PASS — AUTHOR APPROVED` (roadmap gate table) |
| `PROFILE NC` | `APPROVED` 2026-09-16 |
| `PRE-G9B-P0` | `PASS — AUTHOR APPROVED` |
| Version | `1.0.0`, `upgrade_uuid b52d8e6d-3996-4bc5-b9ba-4f51f73c6e44` |
| Distribution | `internal-evaluation`, redistribution blocked |
| `PRE-G9B-P1` | `DESIGNED — PROMOTION NOT YET AUTHORIZED` |

## Decisions materialized

### Locus V2 / SplineV2 promoted

Both surfaces are normal GeoCeDG behavior. The defaults are named constants,
`AppConfigGeoCeDG.DEFAULT_LOCUS_V2_CREATION_ENABLED` and
`DEFAULT_EXTENDED_DXF_ENABLED`, consumed by `AppGeoCeDG.createConfig`. There is
no separate SplineV2 flag: `cedg.locus.v2` covers both, and the profile schema
forbids a second runtime flag.

`RuntimeFeatureService`, `mayCreateLocusV2`, `requireLocusV2Access`, the
preservation context and every kernel gate are **unchanged**. Promotion moved
the default only.

### `--enableLocusV2` retained

`--enableLocusV2` and `--enableExtendedDxf` remain accepted syntax and are now
**bidirectional** overrides: absent means the product default, and an explicit
`=false` still starts GeoCeDG without that surface. Neither argument changes
durable identity, serialization or geometric meaning. The existing ON/OFF
semantics were characterized before being changed: the flag controls command
visibility, dispatch, tool modes, dialogs, actions and user-tool validation, and
is deliberately **not** consulted for reconstruction, which remains governed by
`Construction.isFileLoading()`.

### G9X1 extended DXF promoted, independently

`cedg.export.dxf.extended` is default-on for GeoCeDG and remains a separate
decision from Locus V2; all four combinations are expressible and asserted. The
G9X1 contract itself — neutral export geometry, exact G5 paths, fidelity
classification, approximation semantics, branch/component/domain authority,
typed preflight, strict failure, sidecar, diagnostics, determinism and viewport
independence — is untouched. No DXF `SPLINE` entity and no new fidelity were
introduced.

### Capabilities still gated

Only the two authorized features moved. `cedg.laboratory.legacy`,
`cedg.spatial.semantics` and the `gated-g9u2` workspace keep their gates, and a
focal test asserts the whole experimental inventory rather than the two promoted
entries alone. `cedg.export.dxf.2d` was already default-on before P1.

## Classic containment

Containment is preserved **by construction**: `mayCreateLocusV2` still requires
`instanceof AppConfigGeoCeDG`, so no non-GeoCeDG configuration can reach the
promoted policy, and `isExtendedDxfEnabled` returns false for any other config.
Classic keeps its own launcher, process, preferences file and upstream defaults.
`AppConfigGeoCeDG` is GeoCeDG-only by definition, so changing its defaults cannot
leak. The default flip was deliberately **not** applied inside
`RuntimeFeatureService`, which would have leaked.

## Historical reconstruction

Creation policy and reconstruction remain separate contracts. The feature-off
archive suites still prove that a GeoCeDG instance with creation explicitly
disabled loads, recomputes, re-serializes byte-identically and preserves
persistent identities for native V2 and SplineV2 documents. Those routes were
updated to state the override explicitly instead of relying on the old default,
which is the only reason they changed. `.cedg` remains the native document
identity and `.ggb` remains compatibility input; installers claim `.cedg` and
never `.ggb`.

## P0 remains intact

The P0 suite is unchanged and green: version exactly `1.0.0`, three themes with
`Original` default, paint-only canvas, `Layout & Presentation` and the
presentation sizes. `upgrade_uuid` is unchanged.

## Distribution profile design

Product metadata and distribution policy are now separate, without a second
product-version authority. `packaging/windows/package.yml` still declares
exactly one `application.version`, so the Gradle guard requiring exactly one
semantic version still holds — that guard is what makes the single-authority
invariant enforceable rather than merely documented.

The change is **additive**. `distribution.status`, `.marker` and
`.public_redistribution` remain the repository default and still say
internal-evaluation with redistribution blocked, so every pre-existing assert
keeps passing. Alongside them, `distribution.default_profile` pins `INTERNAL`
and `distribution.profiles` declares three profiles:

| Profile | Status | Redistributable | Tag | Notice |
|---|---|---|---|---|
| `INTERNAL` | `internal-evaluation` | no | `internal` | `INTERNAL_EVALUATION_ONLY.txt` |
| `NC` | `public-non-commercial` | yes | `nc` | `NC_DISTRIBUTION_NOTICE.txt` |
| `COMMERCIAL` | `not-authorized` | no | `commercial` | — (blocked) |

The schema enforces the policy instead of documenting it. Six negative cases
were exercised against a real JSON-Schema validator and all six were rejected:
COMMERCIAL flipped to redistributable, COMMERCIAL status forged to public,
COMMERCIAL blocking terms removed, a redistributable profile missing its
licensing authority, the repository default flipped off internal-evaluation, and
a smuggled fourth profile. A commercial release therefore cannot be forged in
the profile document.

## Single version authority

`packaging/windows/package.yml` `application.version = 1.0.0` remains the only
product-version authority. Distribution profiles carry no version. About, title,
provenance, package manifests, filenames and installers all still derive from it,
and `application.upgrade_uuid` is byte-identical. P0 remains the phase
responsible for the version; P1 changed none of it.

## Commands

```powershell
# INTERNAL (repository default; no profile argument needed)
.\tools\release\build-windows-package.ps1 -Target All

# PROFILE NC redistributable candidate
.\tools\release\build-windows-package.ps1 -DistributionProfile NC -Target All

# COMMERCIAL: expected to fail closed, naming the pending external terms
.\tools\release\build-windows-package.ps1 -DistributionProfile COMMERCIAL -Target All

# Bounded licensing-impact delta against the D1 baseline
.\tools\release\licensing-impact-delta.ps1 -ResultPath <result.json> -SummaryPath <summary.txt>
```

The COMMERCIAL attempt exits 1 before any build work with:

```text
Distribution profile COMMERCIAL is not authorized. Status: not-authorized.
Pending external terms: GeoGebra License and Collaboration Agreement for the
retained GeoGebra Materials; OpenGeoProver rightsholder clarification or
commercial permission
```

## Generated outputs

A full NC set and a full INTERNAL set were both generated and verified.

| Output | NC | INTERNAL |
|---|---|---|
| app-image | `app-image/GeoCeDG/GeoCeDG.exe` | same |
| ZIP | `GeoCeDG-1.0.0-windows-x64-nc.zip` | `...-internal.zip` |
| MSI | `packages/GeoCeDG-1.0.0-windows-x64-nc.msi` | `...-internal.msi` |
| EXE | `packages/GeoCeDG-1.0.0-windows-x64-nc.exe` | `...-internal.exe` |
| notice in app | `NC_DISTRIBUTION_NOTICE.txt` | `INTERNAL_EVALUATION_ONLY.txt` |
| SBOM | `geocedg-windows.cdx.json`, CycloneDX 1.5 | same |
| manifest | `build-manifest.json` incl. `distribution_profile` | same |
| legal bundle | 9 items under `app/legal` incl. the full `LICENSES/` tree | same |
| hashes | `SHA256SUMS.txt`, `app-image.SHA256SUMS.txt` | same |

NC composition matched D1 exactly: 51 staged JARs, 46 fonts, 0 unknown SBOM
versions, 6 excluded non-Windows native JARs, `unresolved_payload_count = 0`.

The decompiled NC MSI proved, through the WiX-5 registry-row projection, one
`.cedg` extension, **zero** `.ggb` claim, one internal MIME and zero upstream
MIME, a GeoCeDG-owned ProgId, one `open` verb, and a launcher target of
`GeoCeDG.exe`.

Byte reproducibility of MSI/EXE is **not** claimed. Composition, manifests,
source graph and per-build hashes are deterministic; `jlink`/`jpackage`/WiX
introduce variable metadata, as the packaging contract already records.

## Licensing-impact delta

`tools/release/licensing-impact-delta.ps1` is bounded and consumes the D1
evidence as its baseline, applying the overlay's own declared rule: the overlay
value when `component_id` is present, otherwise the immutable audit candidate.
It emits machine-readable JSON plus a short human summary and fails closed.

Three hazards were handled explicitly rather than silently, each discovered by
testing against real data:

1. Two overlay ids have **no audit counterpart** (`runtime:temurin-25.0.4+7`,
   `installer-tool:wix-5.0.2`). They are aliased, so a naive join cannot drop the
   Temurin legal tree or the WiX MS-RL payload.
2. Gradle resolves **non-Windows natives that packaging excludes**. They are
   filtered by the profile's own exclusion patterns, because a component that is
   not redistributed carries no redistribution obligation.
3. Several audited entries carry a `resolved transitive:` placeholder coordinate
   or a classifier segment, so **raw coordinate comparison is unsound**.
   Source-identity drift is asserted only where the audit recorded a real Maven
   coordinate; SHA-256 byte identity remains the authoritative signal, matching
   the join the builder itself uses.

Observed behavior:

| Scenario | Result |
|---|---|
| Unchanged composition | `NO_MATERIAL_LICENSING_DELTA`, exit 0 |
| Own project JAR rebuilt | informational `CHANGED_BYTES`, not material |
| Added external component | `ADDED`, material, exit 1 |
| Changed third-party bytes | `CHANGED_BYTES`, material, exit 1 |
| Changed source identity | `CHANGED_SOURCE_IDENTITY`, material, exit 1 |

The own-code case is demonstrated live rather than synthetically: `common.jar`
really was rebuilt by this phase's source change and is correctly classified as
provenance only.

## Tests and gates

| Run | Result |
|---|---|
| Full desktop suite | 1466 tests, 0 failures, 0 errors |
| Full shared suite | 6678 tests, 0 failures, 0 errors |
| `PreG9BP1PublicSurfaceTest` | 18 tests, 0 failures (13 original plus 5 added by the continuation) |
| `PACKAGING -CheckToolchain` | `ACCEPTED / COMPLETE`, 0 diagnostics |
| `PACKAGING -VerifyPackagingArtifacts` (NC set) | `ACCEPTED / COMPLETE`, 0 diagnostics |
| `PACKAGING -VerifyPackagingArtifacts` (INTERNAL set) | `ACCEPTED / COMPLETE`, 0 diagnostics |
| `STATIC` | `ACCEPTED / COMPLETE`, 1 pre-existing recovery-protocol diagnostic |
| `INFRA_UNIT` | `ACCEPTED / COMPLETE`, 0 diagnostics |

Six tests encoded the old default-off contract and were updated to assert the
promoted contract; every feature-off route now states the override explicitly
rather than relying on a default. The live-manifest guard in
`verify-locus-v2.ps1` was updated; the adjacent **frozen G6R historical
evidence** assertion was deliberately left untouched.

- `GUIDE_IMPACT = UPDATED`.
- `BOOTSTRAP IMPACT = NO_CHANGE_REQUIRED`: no workstation prerequisite, JDK,
  toolchain version, Gradle behavior or packaging prerequisite changed. The
  pinned toolchain (Gradle Java 22, desktop Java 25, WiX 5.0.2) is unchanged and
  was exercised end to end.
- Verification impact: the registry, inventory and static catalog changed, so
  `INFRA_UNIT` was required and rerun, and the catalog hashes were repinned.

## Continuation: legal-bundle reconciliation and user-guide audit

Authorized after this candidate obtained `PHASE PRE-G9B-P1` and `FINAL`, as a
strictly bounded correction. It reopens no D1 investigation and redesigns no
packaging, Locus V2, SplineV2 or DXF behavior. The two items previously left
open here are resolved by it.

### What was reconciled

`LICENSE`, `NOTICE.md`, `THIRD_PARTY.md` and `LICENSES/README.md` no longer
assert `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`. Each now states that it
is profile-neutral and delegates the distribution condition to the notice
packaged with the build — `INTERNAL_EVALUATION_ONLY.txt` for PROFILE INTERNAL,
`NC_DISTRIBUTION_NOTICE.txt` for PROFILE NC — while keeping every professional
caveat D1 recorded: final author and legal review remain required, the payload
docket closure is not release approval, OpenGeoProver still needs clarification
before a commercial profile, and commercial distribution is not authorized.
`LICENSES/manifest.json` carries the same neutral marker and re-pins the
reconciled `LICENSES/README.md` by hash.

`build-manifest.legal_bundle.public_profile` now reports the licensing profile
of the build (`PROFILE NC` for NC, `NOT APPLICABLE — INTERNAL EVALUATION BUILD`
for INTERNAL). The D1 fact it used to overload is preserved as a separate
`approved_composition_profile` field.

### What was deliberately preserved

- `geocedg/validation/pre-g9b-d1/component-audit.json` — immutable D1 factual
  closure.
- `geocedg/resources/source-access-manifest.json` and
  `geocedg/resources/assets-manifest.yml` — dated D1 evidence whose
  `distribution_marker` records the package state at its own evidence date.
- Historical D1, G4 and P0 reports, which correctly record that the package was
  `internal-evaluation` at the time they were written.

Because the three evidence records ship inside the bundle,
`NC_DISTRIBUTION_NOTICE.txt` now states the precedence explicitly: the notice
states the condition of the copy, the general documents defer to it, and the
dated evidence marker is historical and does not restrict the copy. Re-dating
those evidence records instead would be a D1-evidence decision and was not
taken.

### Fail-closed in both directions

The builder refuses a redistributable build whose staged general legal
documents still carry the internal marker. `packaging-product.ps1` replaced its
four "document must contain the internal marker" contracts with the profile
invariant: no general document asserts a distribution condition, each notice
carries exactly its own marker and not the other's, and the staged bundle never
contradicts the built profile.

### User-guide audit

`docs/user/geocedg_user_guide.md` is the single tracked authority; Gradle
`processResources` copies it verbatim into
`org/geocedg/desktop/geocedg_user_guide.md`, and a test now asserts byte
identity, so no second manual copy can appear. Stale default-off and
`--enableLocusV2=true` / `--enableExtendedDxf=true` opt-in wording was replaced
by the promoted defaults, the retained bidirectional overrides, their
independence and the Classic boundary. The maturity vocabulary row was aligned
with `AGENTS.md`, which defines `experimental` as "integrated behind a feature
flag" without requiring default-off. The G9X1 section now states explicitly
that exact DXF `SPLINE` is **NOT IMPLEMENTED**, that the current `SplineV2`
representation is an approximate export-only `LWPOLYLINE`, that approximation
follows from geometric semantics and never from render, viewport, zoom or DPI,
and that `ESTIMATED_ERROR` is not a certified global error bound. The remaining
"disabled by default" statements in the guide belong to `cedg.laboratory.legacy`
and the nonpublic G8C1 kernel and are still correct.

## Known limitations

- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open and separate. No P1
  test reproduced it within P1 scope.
- MSI/EXE byte reproducibility is not claimed. Only the app-image and the
  normalized ZIP are timestamp-normalized.
- No interactive GUI smoke was run for P1 or for this continuation; the author's
  manual smoke remains pending.

## Governance disposition

```text
PRE-G9B-P1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
GeoCeDG version = 1.0.0
PROFILE NC = APPROVED / PACKAGE-READY
PROFILE COMMERCIAL = NOT AUTHORIZED
selfApproved=false
authorApproved=false
passClaimed=false
```

No GitHub release, tag, binary publication or third-party contact was made. D1
remains closed for PROFILE NC; G9B, G9C, G9U2, further G12 and productive G10
remain untouched and unauthorized.
