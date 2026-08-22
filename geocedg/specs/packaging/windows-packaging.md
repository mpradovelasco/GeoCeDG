# GeoCeDG Windows packaging contract

- Status: Stable G4 infrastructure refined by the G9U0-R2 implementation candidate
- Platform: Windows x64 only
- Distribution status: Internal evaluation; public redistribution blocked
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

## Pipeline

`tools/release/build-windows-package.ps1` performs these reproducible stages:

1. validate the package manifest and workstation toolchain;
2. invoke the repository Gradle wrapper for `installDist` unless explicitly
   told to reuse a previously validated layout;
3. copy runtime JARs to an isolated staging directory while rejecting all
   non-Windows native variants;
4. add internal-evaluation and legal-status records;
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

The pipeline fails if the result contains PDFs, `.ggb`/`.ggt` models,
`Templatev7.ggb`, repository documentation, or non-Windows native JARs. It
does not traverse `docs/`, `models/`, or other repository knowledge stores.
The stable G2 toolbar and the G3 Laboratory are not modified by packaging.

## Verification

`tools/agent/verify-packaging.ps1` validates durable contracts by default.
`-CheckToolchain` additionally requires Java 25 `jpackage`, a compatible .NET
SDK, and WiX 5.0.2. `-RequireArtifacts` validates a generated full package set,
its internal marker, exclusions, SBOM, build manifest, hashes, and the
decompiled MSI association. Static verification also proves that the package
profile, schema and Java properties agree and that association arguments occur
only in the installer path.

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

## Release gate

Successful execution proves packaging capability only. Until the component
matrix, third-party notices, upstream runtime assets, branding, and project
license are approved, every artifact remains:

`INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`
