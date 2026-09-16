# GeoCeDG PROFILE NC

Status: **APPROVED** on 2026-09-16 as the PRE-G9B-D1 deployability profile.
This is a compact navigation summary, not a second licensing authority and not
a release artifact. The current `0.9.0` package profile remains marked
`internal-evaluation`; changing that package state belongs to a separately
authorized release/promotion step.

## Approved scope

PROFILE NC is the current GeoCeDG Windows composition for public
non-commercial use, teaching, academic study and research, subject to every
applicable component term and the complete legal bundle:

- GeoCeDG-authored code and software scripts: EUPL-1.2;
- inherited GeoGebra source code: its applicable EUPL-1.2 terms, preserved
  notices and modification records;
- GeoCeDG-authored documentation and ordinary artwork: CC BY 4.0, with recorded
  exceptions;
- GeoGebra Language Files, UI images/styles and other retained Materials:
  their applicable GeoGebra and CC BY-NC-SA terms;
- 39/39 applicable external runtime JARs and 46/46 renderer fonts: retained
  with their recorded individual texts, notices and source-access duties;
- Giac `70501`: retained under the recorded GPL/GMP/MPFR composition and
  corresponding-source treatment;
- `math-cross-platform 3.6.3`: retained with its Apache-2.0, GPL-2.0 with
  Classpath Exception, and EUPL composite notice;
- OpenGeoProver `20120725`: retained for PROFILE NC with GPL/source access;
- Temurin `25.0.4+7`: retained with its runtime legal tree and source identity;
- WiX `5.0.2`: build-only tooling distinguished from the MS-RL payload embedded
  in MSI/EXE; and
- the GeoCeDG legal bundle, notices, SBOM identity and corresponding-source /
  source-access records as closed by D1.

The complete binary is a composite non-commercial product. It must not be
described globally as licensed solely under EUPL, Open Source, or Free Software
while it retains GeoGebra Materials governed by non-commercial terms.

## Branding and trademark boundary

- `GeoCeDG` word mark: OEPM application `M4403138` filed in Spain on
  `2026-09-16 12:09:08`, Nice class `09`; **registration pending**.
- Figurative mark/artwork: no versioned evidence establishes a figurative-mark
  application. Current logo, icon and splash assets remain copyright works and
  official-product branding under the asset manifest and brand policy.
- Copyright permission for artwork is independent from trademark status.
- No registered-trademark symbol is authorized or claimed.

The filing facts and redacted primary evidence are controlled by the
[trademark-registration record](trademark-registration/README.md). This summary
does not duplicate or supersede that evidence.

## Packaging boundary

D1 approves the audited composition as deployable for PROFILE NC. The current
[Windows package profile](../../packaging/windows/package.yml) still emits an
internal-evaluation `0.9.0` package, and the
[Windows builder](../../tools/release/build-windows-package.ps1) continues to
enforce that marker. D1 approval does not itself create, publish or retitle a
public binary and does not authorize P1 or version 1.0.

## Authority map

- immutable factual closure: [`component-audit.json`](../../geocedg/validation/pre-g9b-d1/component-audit.json)
- current dispositions: [`component-disposition.json`](../../geocedg/validation/pre-g9b-d1/component-disposition.json)
- corresponding source: [`source-access-manifest.json`](../../geocedg/resources/source-access-manifest.json)
- assets and branding: [`assets-manifest.yml`](../../geocedg/resources/assets-manifest.yml)
- human-readable component map: [`component-matrix.md`](component-matrix.md)
- exact legal texts: [`LICENSES/manifest.json`](../../LICENSES/manifest.json)
- notices: [`NOTICE.md`](../../NOTICE.md) and [`THIRD_PARTY.md`](../../THIRD_PARTY.md)
- final D1 closeout: [`pre_g9b_d1_closeout_report.md`](../validation/pre_g9b_d1_closeout_report.md)
