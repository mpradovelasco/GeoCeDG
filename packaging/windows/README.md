# Windows packaging

This directory contains durable inputs for the G4 Windows pipeline. Generated
outputs belong under ignored `artifacts/packaging/windows/`.

From the repository root:

```powershell
.\tools\release\build-windows-package.ps1 -Target AppImage
.\tools\release\build-windows-package.ps1 -Target All
```

The first command builds a self-contained application image. The second also
creates a normalized portable ZIP plus MSI and EXE installers. WiX 5.0.2 is
required only for MSI/EXE. Per-build hashes are authoritative; `jlink` output
is not guaranteed to be byte-identical between equivalent runs. Every output
is marked `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`.

The package profile, focused verifier, and governing decision are respectively
`package.yml`, `tools/agent/verify-packaging.ps1`, and
`docs/adr/0004-standalone-windows-packaging.md`.

`NuGet.Config` is scoped to this directory and permits only official
`WixToolset.*` extension packages from nuget.org. It lets the opt-in bootstrap
install the pinned Util/UI extensions without changing a user's NuGet source
configuration.
