# PRE-G9B-D1 final closeout

Status: **PASS — AUTHOR APPROVED** on 2026-09-16.

~~~text
PRE-G9B-D1 = PASS — AUTHOR APPROVED
PROFILE NC = APPROVED
PROFILE COMMERCIAL = PENDING EXTERNAL TERMS / NOT AUTHORIZED
GEOCEDG WORD MARK = APPLICATION FILED / REGISTRATION PENDING
selfApproved=false
~~~

## Scope and accepted evidence

The author completed professional review and accepted the currently versioned
interpretations, obligations and dispositions for Giac/GMP/MPFR,
math-cross-platform, OpenGeoProver for PROFILE NC, GeoGebra Materials and
Language Files, WiX, Temurin, the legal bundle and corresponding-source /
source-access handling. The accepted closure remains 39/39 applicable external
JARs and 46/46 fonts for PROFILE NC. No dependency, asset, runtime or
mathematical behavior changed during this closeout.

The accepted technical evidence remains:

- PACKAGING `verification-368265e284424ff788917717f271c28d`, `ACCEPTED /
  COMPLETE`, result hash
  `4411ce21921193183ec70b39acd991fd9a43a6081d0c80c7f4b975d1167308e2`;
- STATIC `verification-ef12293ff46d48cba5d7ea3c280fecd5`, `ACCEPTED /
  COMPLETE`, result hash
  `e7b08d57aeb99b06ef3c1d8415f2caa40bee293b0b2ea38542421910275dfc30`;
- the `.cedg` association was already correctly materialized in MSI; the
  defect was a WiX-5-unaware verifier false negative, and the corrected
  verifier deterministically inspects the compiled MSI.

The factual audit, intermediate candidates, remediation plans and dated author
decisions remain historical evidence. This report does not rewrite them.

## Trademark evolution after the checkpoint

The historical D1-HD-03 disposition on 2026-09-15 correctly recorded that a
filing was then deferred and unauthorized. A later author action superseded
that operational state: the Spanish word-mark application for `GeoCeDG` was
filed with the OEPM as `M4403138` on `2026-09-16 12:09:08`, Nice class `09`.

The filing receipt proves presentation only. Registration remains pending; no
grant or registration identifier/date is claimed. No evidence establishes a
figurative-mark application. Artwork copyright and trademark remain separate.
The authoritative filing record is
[`docs/licensing/trademark-registration/README.md`](../licensing/trademark-registration/README.md).

## Distribution boundary after D1

D1 approves deployability for the exact audited PROFILE NC composition. The
current package profile remains `0.9.0`, `internal-evaluation`, and is not
retitled or published by this closeout. P1 is the next separately authorized
decision point for public surface/version and release materialization.

Commercial distribution remains unauthorized. COMMERCIAL-A requires external
GeoGebra terms for retained Materials and separate OpenGeoProver commercial
clarification/permission. COMMERCIAL-B remains a future replacement strategy.
No third party was contacted.

## Durable authority map

- [PROFILE NC summary](../licensing/geocedg_profile_nc.md)
- [commercial summary](../licensing/geocedg_profile_commercial.md)
- [human decision docket](pre_g9b_d1_human_decision_docket.md)
- [component matrix](../licensing/component-matrix.md)
- [`component-audit.json`](../../geocedg/validation/pre-g9b-d1/component-audit.json)
- [`component-disposition.json`](../../geocedg/validation/pre-g9b-d1/component-disposition.json)
- [`assets-manifest.yml`](../../geocedg/resources/assets-manifest.yml)
- [`source-access-manifest.json`](../../geocedg/resources/source-access-manifest.json)
- [trademark policy](../licensing/trademark-policy.md)
- [filing evidence index](../licensing/trademark-registration/README.md)

`GUIDE_IMPACT = UPDATED`

`GUIDE_PATHS = docs/roadmap/geocedg_roadmap.md; docs/licensing/geocedg_profile_nc.md; docs/licensing/geocedg_profile_commercial.md`
