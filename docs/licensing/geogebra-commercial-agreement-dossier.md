# GeoCeDG — future GeoGebra commercial-agreement dossier

Status: prepared for a possible first contact; no contact is authorized or has
been made. Facts are current to 2026-09-15. This is not legal advice and does
not presume contractual terms.

## Project and provenance

GeoCeDG is an independent source-based fork oriented to Computer-Extended
Descriptive Geometry. It preserves explicit geometric constructions and the
GeoGebra dependency graph while adding a dedicated application profile,
CeDG-specific semantic capabilities, independent GeoCeDG branding, a Windows
`jpackage`/WiX pipeline, validation infrastructure, and product documentation.

- Upstream repository: `https://github.com/geogebra/geogebra`
- Pinned upstream baseline: GeoGebra 5.4.928.0,
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- Current GeoCeDG source provenance and upstream modifications:
  `docs/upstream/BASELINE_COMMIT.txt` and `docs/upstream/modified-files.yml`
- Architecture: source fork with shared Java kernel and Desktop application;
  not a wrapper around an official GeoGebra installer or web service
- Planned first platforms/formats: Windows x64 app-image, portable ZIP, MSI and
  EXE; `.cedg` is a GeoCeDG-owned file association

## Component boundary to present

GeoCeDG distinguishes:

1. GeoGebra-authored source code available under EUPL-1.2, with preserved
   notices and recorded modifications;
2. GeoGebra Language Files retained in runtime JARs;
3. upstream UI images, icons and styles retained in runtime JARs;
4. any other GeoGebra Materials incorporated into the current desktop product;
5. GeoCeDG-authored EUPL code, CC BY documentation/artwork and separate marks;
6. independent third-party JARs, natives, fonts, OpenJDK runtime and WiX
   payload handled under their own terms.

The official GeoGebra legal text identifies source code under EUPL-1.2 and
Language Files/documentation/UI image and style files under CC BY-NC-SA
4.0-or-later, while commercial use of the overall Materials/product requires a
License and Collaboration Agreement. Snapshot:
`LICENSES/GeoGebra-License-2025-11.md`; current official source:
<https://github.com/geogebra/legal/blob/main/geogebra_license.md>.

The exact retained resource/JAR closure, hashes and package locations can be
supplied from `component-audit.json`, `component-disposition.json`, the
resolved Gradle component export, SBOM and app-image hash inventory. This
dossier should attach those generated receipts only after the six independent
payload blockers are closed.

## Intended uses and markets

Initial public intent is non-commercial download and redistribution for
teaching, academic study and research. Possible future commercial scenarios
include paid institutional distribution, professional/educational deployment,
commercial training/support, sponsored development, or a commercial-capable
desktop offering. Intended users may include geometry students and teachers,
architectural/engineering educators, researchers, and practitioners using
descriptive-geometry constructions. Territories, channels and exact business
model remain negotiation inputs, not settled claims.

GeoCeDG uses its own name, launcher, icons, splash, package identifiers and
installer. GeoGebra would be referenced truthfully as the upstream source and
credited as required (“Made with GeoGebra” where applicable). GeoCeDG would not
claim endorsement or use the upstream installer. Any requested use of GeoGebra
marks should be listed expressly rather than inferred.

## Rights/permissions to discuss

- commercial distribution/use of the retained GeoGebra Language Files, UI
  images/styles and other Materials in GeoCeDG packages;
- scope for redistribution in Windows ZIP/MSI/EXE and later platforms;
- modification/localization rights and how share-alike obligations apply to
  altered Language Files/Materials;
- required notices, attribution wording and placement;
- whether screenshots, help content, store pages and training materials need
  specific permissions;
- any allowed nominative or compatibility use of GeoGebra names/marks;
- source/binary delivery, update and version-reporting requirements; and
- whether collaboration, upstreaming or support arrangements are offered.

EUPL code rights should be acknowledged separately. The agreement should not
silently replace third-party licenses or represent unrelated component
clearance by GeoGebra.

## Open negotiation questions

The official public material does not establish fees, royalties, duration,
territory, exclusivity, sublicensing, minimum commitments, support, warranties,
update rights, audit terms, termination effects, app-store permissions, or the
exact licensed material set. Each is an open question for GeoGebra and
professional review. GeoCeDG should provide projected users, revenue model,
territories and release channels only when the author has decided them.

Questions for GeoGebra:

1. Which retained resource classes and product uses require the agreement?
2. Can one agreement cover non-commercial community packages plus future paid
   institutional/professional scenarios?
3. What attribution, branding, modified-file and source-delivery language is
   required?
4. Are Language Files/UI Materials licensed for modification and
   redistribution in the proposed formats and territories?
5. What identifier/mark uses are allowed for compatibility statements?
6. What commercial, reporting, support and renewal terms would apply?
7. Is a maintained inventory/SBOM sufficient to define the licensed payload,
   or must exact versions be enumerated contractually?

## Draft first-contact email — do not send

Subject: GeoCeDG source fork — enquiry about License and Collaboration Agreement

> Dear GeoGebra licensing team,
>
> I am the author/maintainer of GeoCeDG, an independent source-based fork of
> GeoGebra for Computer-Extended Descriptive Geometry. It is based on the
> pinned GeoGebra 5.4.928.0 source revision
> `9b93256b7df401ff056c37b502d82df4d72b1522`. GeoCeDG preserves upstream
> notices, uses independent product branding and packaging, and records all
> modified upstream files.
>
> Our immediate objective is a public non-commercial Windows distribution for
> teaching, academic study and research. We also want to understand a possible
> future commercial route. The product retains GeoGebra Language Files and UI
> Materials that your current licensing guidance distinguishes from EUPL-1.2
> source code and says may require a License and Collaboration Agreement for
> commercial use.
>
> Could you advise whom we should work with and what technical/business
> information you require to discuss such an agreement? We can provide the
> exact source baseline, modification register, retained-material inventory,
> SBOM, package formats, intended users/markets and proposed attribution. We
> would particularly like to clarify the licensed material boundary, allowed
> modification/distribution formats, attribution/branding, territories,
> commercial scenarios and any collaboration or support options.
>
> This enquiry would not assume any fee, royalty, exclusivity, sublicensing,
> duration or support term. We would appreciate your proposed process and terms.
>
> Kind regards,
> [AUTHOR NAME]
> [ROLE / CONTACT]

Author authorization is required before using this draft or sending any
project evidence externally.
