# ADR 0004: standalone Windows packaging for internal evaluation

- Status: **Accepted for G4 technical validation**
- Date: 2026-08-10
- Scope: Windows packaging and release infrastructure only
- Public redistribution: **Blocked pending license and asset approval**

## Context

GeoCeDG must run independently of a developer checkout and of an externally
installed Java runtime. The inherited Desktop distribution contains the
required application JARs, but its generated start scripts select the upstream
Classic launcher and its runtime classpath includes native JARs for Windows,
Linux, and macOS. The upstream installer and branding resources are outside
the approved G4 scope.

The validated Desktop launcher is `org.geocedg.desktop.GeoCeDG`. Desktop is
compiled and run with Java 25, while Gradle itself uses Java 22. JDK 25.0.4
provides `jpackage`; Windows MSI/EXE generation was validated against WiX
Toolset 5.0.2, its Util/UI extensions 5.0.2, and the existing .NET 8 SDK.

## Decision

1. Packaging remains outside the upstream Gradle build. The release script
   invokes `:desktop:desktop:installDist` through the repository wrapper and
   consumes only its `lib/` application layout.
2. The staging step excludes non-Windows native JARs. It never consumes the
   generated Classic start scripts.
3. JDK 25 `jpackage` creates one GeoCeDG `app-image`. A portable ZIP with
   normalized entry ordering and timestamps is derived from that image. MSI
   and EXE installers are derived from the same image through WiX 5.0.2.
4. The `.ggb` association is declared only for MSI/EXE installers. It does not
   alter development execution or the portable ZIP.
5. Every generated artifact carries the exact marker
   `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`. No upstream logo or custom
   icon is supplied; the provisional package identity is textual.
6. Generated packages, hashes, manifests, and SBOMs live below ignored
   `artifacts/packaging/windows/`. Durable source authority remains in
   `packaging/`, `tools/release/`, specifications, and this ADR.
7. `tools/agent/verify-packaging.ps1` is the focused gate and is subordinated
   to `tools/agent/verify.ps1`. CI checks the durable contracts but does not
   build installers or install workstation software.
8. `bootstrap-windows.ps1` detects packaging prerequisites. Its default is
   read-only with respect to system software; installation of the minimum
   .NET/WiX prerequisites is explicit and opt-in.

## Package composition boundary

The package contains the resolved Desktop runtime JARs, a linked Java 25
runtime, the internal-evaluation notice, and the current legal-status records.
It excludes repository documentation, scientific PDFs, legacy/research
models including `Templatev7.ggb`, Gradle sources, and non-Windows native
JARs. Embedded upstream resources inside runtime JARs remain subject to the
component audit and therefore block public redistribution.

## Consequences

- GeoCeDG can be built, launched, installed, and uninstalled independently of
  the upstream installer and of a workstation Java installation.
- Package binaries are reproducible as a scripted process. The ZIP normalizes
  entry ordering and timestamps, but JDK 25 `jlink` may vary
  `runtime/lib/modules` between equivalent runs. End-to-end byte identity is
  therefore not promised for ZIP, MSI, or EXE; hashes record each exact run.
- Technical packaging success does not grant redistribution rights. A public
  release requires a human-approved project license, complete dependency and
  asset notices, owned branding assets, and a fresh trademark/license review.
- No geometry, kernel, `Locus`, serialization, or G5 behavior changes.

## Rejected alternatives

- Modifying the upstream application plugin or its Classic start scripts:
  unnecessary coupling to the inherited build.
- Reusing an upstream GeoGebra installer or logo: outside the approved rights
  and branding boundary.
- Bundling WiX in the repository: unnecessary when a pinned global .NET tool
  is available and detectable.
- Making packaging a mandatory CI build: it would mix workstation/release
  responsibilities with the existing source verification gate.
