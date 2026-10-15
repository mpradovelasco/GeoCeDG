# GeoCeDG Windows packaging contract

- Status: Stable G4 infrastructure, as amended by the approved G9U0-R2,
  PRE-G9B-P1, PRE-G9B-R6-plus-E1-P and PRE-G9B-R6-plus-G implementations
- Platform: Windows x64 only
- Distribution status: the repository default profile is `INTERNAL` (internal
  evaluation, not for redistribution); `NC` is the approved non-commercial
  packaging profile; `COMMERCIAL` is not authorized. Building a package of any
  profile is not a public release (section "Release gate").
- Decisions: `docs/adr/0004-standalone-windows-packaging.md` and
  `docs/adr/0016-native-geocedg-document-identity.md`

## Inputs and identity

`packaging/windows/package.yml` is the source of truth for product identity,
entry point, JVM options, native filtering, output kinds, file association,
and distribution marker. It is JSON-compatible YAML under ADR 0002.

The only application input is the `lib/` directory produced by
`:desktop:desktop:installDist`. Generated start scripts are not packaged
because they select the upstream Classic launcher. Packaging selects
`org.geocedg.desktop.GeoCeDG` directly and preserves all four JVM options used
by the validated Desktop Gradle tasks.

The profile also names one versioned GeoCeDG-owned Windows ICO below the
Desktop application-resource tree. The builder passes it to the `app-image`
invocation through `jpackage --icon`; MSI and EXE are then derived from that
same application image. No copied packaging icon or inherited GeoGebra icon is
an authority. The author source, byte-exact promoted source, deterministic
derivation contract, sizes and hashes live in
`geocedg/resources/assets-manifest.yml`.

The tracked source and derivative blobs, together with their raw hashes, are
the package-input authority. Normal packaging validates those blobs and the ICO
container directly and does not rerun a platform image encoder. Exact derivative
regeneration is a separate provenance check restricted to the accepted
PowerShell/.NET/System.Drawing/Windows runtime recorded in the asset manifest.

`PRE-G9B-R6-plus-E1-P` adds one declared non-JAR input: the curated GGT
library of `models/curated/ggt-library/` (`library-manifest.json` and the
`tools/<command>.ggt` files it marks `shipped`; contract in
`geocedg/specs/legacy/curated-ggt-library.md`). Its admission authority is the
curated GGT library rights record
(`geocedg/validation/pre-g9b-r6-plus/curated-ggt-library-rights-record.json`,
mirror of `docs/licensing/curated-ggt-library-rights-record.md`), whose
version must equal the manifest's `rightsRecord.version`. The library is read
as-is: packaging never regenerates, rewrites or filters its content.

## Pipeline

`tools/release/build-windows-package.ps1` performs these reproducible stages:

1. validate the package manifest and workstation toolchain;
2. invoke the repository Gradle wrapper for `installDist` unless explicitly
   told to reuse a previously validated layout;
3. copy runtime JARs to an isolated staging directory while rejecting all
   non-Windows native variants;
4. add the selected profile's distribution notice and the legal-status records, the installed distribution
   record `geocedg-distribution.json` (section "Product metadata and the
   installed distribution record") and, after the `legal/`
   copy, the curated GGT library at `ggt-library/` (app-image path
   `app/ggt-library/`, the location the bundled tool catalog reads) when the
   rights record authorizes the selected profile;
5. create a self-contained `app-image` with JDK 25 `jpackage`;
6. derive a ZIP with normalized entry ordering and timestamps when requested;
7. derive MSI and/or EXE through pinned WiX 5.0.2 and its pinned Util/UI
   extensions, with the native `.cedg` association;
8. emit CycloneDX SBOM, build manifest, and SHA-256 checksums.

All outputs are regenerable and ignored below `artifacts/packaging/windows/`.
The script validates an output root before clearing only that dedicated
directory.

Normalization makes the ZIP deterministic for an identical app-image. JDK 25
`jlink` can nevertheless vary `runtime/lib/modules` between equivalent
app-image builds, so the contract records per-build hashes and does not promise
byte-identical ZIP, MSI, or EXE binaries.

## Native document association

MSI and EXE installers register `.cedg` as the GeoCeDG-owned Windows document
extension. They do not claim `.ggb`, which remains a compatibility/input
format. The app-image and portable ZIP do not register any file association.
The build script supplies `--file-associations` only while creating MSI/EXE
installers.

JDK 25 `jpackage` rejects a file-association group without both an extension
and a MIME value. The package profile therefore records
`application/x-geocedg-cedg` as the narrow internal, unregistered value required
by this Windows implementation. This operational value does not claim the
upstream `application/vnd.geogebra.file` identity, does not establish a public
or cross-platform MIME contract, and does not alter document semantics.

JDK 25/WiX generates the ProgID owned by the GeoCeDG installer from the
GeoCeDG launcher, native extension and owned description. GeoCeDG does not add
a parallel registry or custom WiX authority. Generated MSI inspection must
prove the resulting ProgID, `.cedg` extension, internal MIME record and open
verb to `GeoCeDG.exe`, together with the absence of a `.ggb` claim.

## Required exclusions

The pipeline fails if the result contains PDFs, any `.ggb` model,
`Templatev7.ggb`, repository documentation, or non-Windows native JARs. It
does not traverse `docs/`, `models/`, or other repository knowledge stores;
the only exception is the declared curated GGT library input above, read from
its fixed path.

A `.ggt` file is admitted only if all of the following hold, and every other
`.ggt` anywhere in the app-image fails the build verification:

1. location: it lies directly below `app/ggt-library/tools/`;
2. membership: it is a `shipped` member of the staged `library-manifest.json`,
   which is byte-identical to the canonical repository manifest, and the staged
   library contains exactly that manifest and those shipped files;
3. hash: its SHA-256 equals the canonical manifest value;
4. rights: the rights record lists its command and authorizes the built
   distribution profile.

Profiles: `INTERNAL` and `NC` include the same library; `COMMERCIAL` excludes
it (the builder stages nothing for a profile the rights record does not
authorize, and `COMMERCIAL` additionally fails closed before any build work).
GGT content is never placed or searched inside a JAR; JAR entries are not
inspected for it. The stable G2 toolbar and the G3 Laboratory are not modified
by packaging.

## Verification

`tools/agent/verify-packaging.ps1` validates durable contracts by default.
`-CheckToolchain` additionally requires Java 25 `jpackage`, a compatible .NET
SDK, and WiX 5.0.2. `-RequireArtifacts` validates a generated full package set,
its profile marker and notice, exclusions, SBOM, build manifest, hashes, and the
decompiled MSI association. Static verification also proves that the package
profile, schema and Java properties agree and that association arguments occur
only in the installer path.

The packaging product leaf (`tools/agent/checks/packaging-product.ps1`) checks
the four `.ggt` admission rules as separate subcontracts so that a failure
names its rule: `packaging.portable-boundary` (location and every `.ggb`/PDF),
`packaging.ggt-library.membership`, `packaging.ggt-library.hashes` and
`packaging.ggt-library.rights`; `packaging.ggt-library.build-manifest`
verifies the `ggt_library` record of `build-manifest.json`, which is present
exactly when the built profile admits the library. The SBOM lists each staged
tool as a file component with its SHA-256.

The composed authority always runs the static packaging gate. Use
`tools/agent/verify.ps1 -VerifyPackagingArtifacts` after generating all
artifacts to include toolchain and artifact checks.

Prerequisite setup is a separate operational responsibility.
`tools/bootstrap/install-packaging-prerequisites.ps1` owns focused inspection
and the explicit installation of the approved .NET 8 SDK, pinned WiX 5.0.2,
and pinned Util/UI extensions. The bootstrap
`-InstallPackagingPrerequisites` action delegates to it before any remote,
build, or repository-verification phase and then exits. It never installs a
JDK. Setup success is not repository acceptance; `tools/agent/verify.ps1`
remains the composed authority, while `verify-packaging.ps1 -CheckToolchain`
is the focused toolchain diagnosis.

## Distribution profiles

`PRE-G9B-P1` separates product metadata from distribution policy inside the same
profile document and the same builder. `distribution.default_profile` pins
`INTERNAL`, and `distribution.profiles` declares exactly three profiles:

| Profile | Status | Redistributable | Artifact tag | Notice |
|---|---|---|---|---|
| `INTERNAL` | `internal-evaluation` | no | `internal` | `INTERNAL_EVALUATION_ONLY.txt` |
| `NC` | `public-non-commercial` | yes | `nc` | `NC_DISTRIBUTION_NOTICE.txt` |
| `COMMERCIAL` | `not-authorized` | no | `commercial` | none; blocked |

Distribution profiles carry no version. `application.version` remains the single
product-version authority, and the Gradle guard requiring exactly one semantic
version in this file is what keeps that invariant enforceable. Since
`PRE-G9B-R6-plus-G` the same object also holds the canonical version date
(`application.version_date`, below).

`build-windows-package.ps1 -DistributionProfile <ID>` selects the profile; the
repository default is `INTERNAL`, so nothing is promoted by omission. The
selected profile supplies the notice, the association properties and
description, the distribution marker and the artifact tag. `COMMERCIAL` fails
closed before any build work and names its pending external terms. The schema
rejects a forged commercial promotion, a redistributable profile without its
licensing authority and notice, a repository default that is not
internal-evaluation, and any fourth profile.

Artifact verification follows the built profile: `packaging-product.ps1` reads
the generated `build-manifest.json` and derives the expected artifact tag,
notice, marker, MSI association description and redistribution string from it.

### Profile-neutral legal documents

The general legal documents shipped by every profile (`LICENSE`, `NOTICE.md`,
`THIRD_PARTY.md`, `LICENSES/README.md`, `LICENSES/manifest.json`) state the
licensing scope of the composition and must stay profile-neutral. The
distribution condition belongs to the selected profile's notice, which is the
only document that asserts it. The builder refuses a redistributable build
whose staged general documents still carry the internal-evaluation marker, and
`packaging-product.ps1` asserts the same invariant in both directions: each
notice carries exactly its own marker, and the staged bundle never contradicts
the built profile.

The dated PRE-G9B-D1 evidence records (`component-audit.json`,
`source-access-manifest.json`, `assets-manifest.yml`) keep the distribution
marker of their own evidence date. They are versioned evidence, not conditions
of a later build, and the NC notice states that precedence explicitly.

## Product metadata and the installed distribution record

Normative since `PRE-G9B-R6-plus-G` (transcription of the author-approved product
metadata contract PM-1 to PM-5, PM-8, PM-10 and PM-12; the
[design record](../../../docs/architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md)
stays the historical design authority, and this section prevails over it).

**Identity, version and version date (PM-1 to PM-3).**

- `application.name` (schema constant `GeoCeDG`) is the product name; the Desktop
  constant must equal it.
- `application.version` is the only product-version authority, in the form
  `MAJOR.MINOR.PATCH`, always shown in full. No product source holds a literal
  version; tests and user guides may quote it and change with it.
- `application.version_date` is required: an ISO 8601 calendar date `YYYY-MM-DD`
  designated by the author for that version. It is adopted when the author approves
  that exact verified version; it is not the approval, build, commit, publication or
  release date and is never computed from a clock, a commit, a tag or a file time.
  `1.0.0` has no version date. `1.1.0` carries `2026-10-15`.
- The package profile uses `schema_version` `2` (`$id` `package-profile-v2`), which
  requires `version_date`. The builder and the packaging check accept exactly version
  `2` and fail closed on any other. A profile at an earlier commit stays valid under
  its own version-1 schema.
- The Desktop Gradle build requires exactly one `"version"` and exactly one
  `"version_date"` key, a valid calendar date and package-profile schema `2`.

**Build provenance (PM-4, PM-5).** The generated resource
`geocedg-build-provenance.properties` inside `desktop.jar` has schema `3`. It holds
the version and its date, the canonical package-profile digest, the `id`, `status`,
`marker` and `redistributable` of every declared profile, and the repository commit,
state (`CLEAN`, `DIRTY`, `UNAVAILABLE`) and resolution source. It is written in
ASCII and is a pure function of `package.yml` and the commit identity; no clock is
read. The Desktop loader accepts schema `3` only, with exactly these keys. The
resource is internal to `desktop.jar` and is not a document format.

**Canonical package-profile digest.** `package_profile_sha256` is the SHA-256 of the
strict UTF-8 bytes of `packaging/windows/package.yml` after every `CRLF` and lone
`CR` is replaced by `LF`. The Gradle build embeds it, the builder computes it the
same way, and both must agree.

**Installed distribution record (PM-5).** The builder writes
`app/geocedg-distribution.json` into the jpackage input next to the profile notice,
so it is part of the app-image, the ZIP, the installers and
`app-image.SHA256SUMS.txt`. It is derived only from the selected, validated profile
and is bound to the provenance read from the staged `desktop.jar`: the builder
refuses to package unless the embedded schema is `3` and the embedded version,
version date and digest equal the package profile and the embedded commit equals
the repository `HEAD`. The record has exactly these members:

| Member | Value |
|---|---|
| `schema_version` | `1` |
| `record_kind` | `geocedg-distribution-record` |
| `profile_id` | the selected profile, exactly `INTERNAL` or `NC` |
| `status`, `redistributable`, `marker` | the selected profile's declared values |
| `notice_file` | the file name of the selected profile's notice |
| `notice_sha256` | SHA-256 of the staged notice |
| `application` | `name`, `version`, `version_date` of the package profile |
| `source_revision` | the 40- or 64-digit commit embedded in `desktop.jar` |
| `package_profile_sha256` | the canonical digest |

`desktop.jar` stays profile-neutral: one JAR serves every profile.
`build-manifest.json` adds `application.version_date` and a `distribution_record`
object (`path`, `sha256`, `profile_id`, `package_profile_sha256`).

**Runtime states.** The Desktop runtime locates the record only through the
`jpackage.app-path` launcher property and decides the condition from its verified
content:

- `DEVELOPMENT` — no jpackage launcher (Gradle runs, tests, a source checkout);
- `INTERNAL` or `NC` — the record is a regular file of at most 16 KiB, strict UTF-8
  and strict JSON with exactly the members above; it names a declared profile that is
  packageable (`INTERNAL` not redistributable, `NC` redistributable, never
  `not-authorized`) whose status, marker and flag equal the embedded table; its
  application, version date, source revision and digest equal the embedded values;
  and the notice it names exists beside it with the recorded hash;
- `UNVERIFIED` — a launcher, but the record is missing, malformed, inconsistent,
  unbound or names a profile not authorized for packaging, including `COMMERCIAL`.

No state is derived from a notice-file name, an installation path, an environment
variable, a label or an artifact name, and every failure is non-redistributable.
These are consistency checks between artifacts of one GeoCeDG pipeline: they give no
cryptographic authenticity or tamper resistance and confer no licensing permission.
The packaged notice and the licensing documents remain the legal authority. No signed
metadata, online validation or DRM exists.

**Other surfaces (PM-8, PM-12).** The window title and the guide-window title keep
the form `GeoCeDG <version>` (and `— <file>`); jpackage `--app-version` and the
artifact names read `application.version`; the installers keep `upgrade_uuid`, so a
newer version upgrades an older one; the user guides quote the canonical version.
No document, XML, preference or unit format changes, and the DXF fidelity sidecar
keeps its schema.

**Packaging check.** `packaging-product.ps1` adds `packaging.version-date`
(the required valid date under schema `2`) and, with artifacts,
`packaging.distribution-record` (the installed record against the built profile,
the package profile, the staged notice, the canonical digest and the manifest
revision) and `packaging.distribution-record.build-manifest` (the manifest records
the exact record hash, the profile, the digest, the version and the version date).

## Release gate

Successful execution proves packaging capability only. The repository default
artifact remains:

`INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`

`PRE-G9B-D1` approved PROFILE NC for the exact audited composition and
`PRE-G9B-P1` added it as the explicit `NC` packaging profile, so an `NC` build is
technically and contractually producible. The profile's `redistributable` flag is
the licence condition that a copy of that profile carries; it is not an
authorization to publish. Building any package, `INTERNAL` or `NC`, is not
publication: a GitHub release, a tag, binary distribution or any third-party
contact remains a separate explicit author decision. PROFILE COMMERCIAL remains
`PENDING EXTERNAL TERMS / NOT AUTHORIZED`.
