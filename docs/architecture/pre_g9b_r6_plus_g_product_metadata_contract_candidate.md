# PRE-G9B-R6-plus-G — product metadata contract candidate

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR APPROVAL — NOT NORMATIVE** |
| Requirement served | `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` (author-approved requirement of 2026-10-07, [E1 preparation closeout record](../validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md) §5) |
| Decisions it prepares | `DQ-G-2` (version and version-date source), `DQ-G-3` (distribution-condition source), `DQ-G-4` (`1.0.0` → `1.1.0` lifecycle) of the [G author-decision reconciliation](pre_g9b_r6_plus_g_author_decision_reconciliation.md) |
| Prepared on | published `main` `abe12893b74f574c248117fbe873633a449e23e0`, tree `ba4298c5658bf3f897455db5a49a76fea7105e72` |
| Normative inputs used, not restated | [Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md) ("Inputs and identity", "Distribution profiles", "Profile-neutral legal documents"); [application profile](../../geocedg/specs/ui/application-profile.md) (version and About paragraph); [`LICENSE`](../../LICENSE); [`NOTICE.md`](../../NOTICE.md); [`THIRD_PARTY.md`](../../THIRD_PARTY.md); [`docs/LICENSE.md`](../LICENSE.md); [component matrix](../licensing/component-matrix.md); [PROFILE NC](../licensing/geocedg_profile_nc.md); [PROFILE COMMERCIAL](../licensing/geocedg_profile_commercial.md) |
| Implementation | **NOT AUTHORIZED.** No product, build, packaging, test, schema or pin change is made or authorized by this document. |
| `selfApproved` | `false` |

This document is a design candidate. It becomes a contract only through an explicit
author decision on `DQ-G-2` to `DQ-G-4`. After that decision, the G implementation
transcribes it into the normative documents listed in §9; until then the approved
packaging and application-profile contracts stay unchanged.

## 1. Purpose and scope

The contract fixes one authority for each fact the GeoCeDG About surface shows:

1. product identity;
2. canonical version;
3. canonical version date;
4. build identity, kept distinct from the version date;
5. distribution condition of the running copy;
6. licensing presentation and the route to the licensing authorities.

It also fixes how each fact reaches:
- the About dialog;
- the window title;
- the guide window;
- the packaged artifacts.

Finally it fixes the lifecycle by which `1.1.0` and its date become canonical.

Out of scope:
- geometry, kernel, document serialization and export semantics;
- Classic's About;
- public release, tags, binary publication and `PROFILE COMMERCIAL`;
- any legal conclusion. The About surface summarizes recorded facts and points to the
  licensing authorities. It is not a licence grant.

## 2. Characterized current state (`abe12893`)

| Fact | Current authority and evidence |
|---|---|
| Product name | `packaging/windows/package.yml:6` `"name": "GeoCeDG"`, a schema constant (`package-profile.schema.json`, `application.name` `const`). Java repeats it as `PRODUCT_NAME` in `GeoCeDGProductInfo.java:13`. |
| Canonical version | `package.yml:7` `"version": "1.0.0"`. The packaging contract makes `application.version` "the single product-version authority" (`windows-packaging.md:165-167`). P0 asserts that exactly one `"version"` key exists (`PreG9BP0PresentationThemeTest.java:416-431`). |
| Build propagation | `source/desktop/desktop/build.gradle.kts:88-100` extracts exactly one semantic version from `package.yml`, or the build fails. Lines `107-126` generate `geocedg-build-provenance.properties` (schema `2`: `application.version`, `repository.commit`, `repository.state`, `resolution.source`). No clock is read. |
| Runtime consumers | `GeoCeDGBuildProvenance.load()` accepts schema `2` only (`GeoCeDGBuildProvenance.java:103-106`). `GeoCeDGProductInfo` derives `applicationTitle()` and `windowTitle()` (`:21-36`). These feed: the frame title (`GeoCeDGFrame.java:23,47,50`); the guide window (`GeoCeDGGuideWindow.java:197`); the About text and dialog (`GeoCeDGActionRegistry.java:450-451,685-709`); the DXF fidelity sidecar (`DxfFidelityManifestWriter.java:233-236`). |
| Version date | **Absent** from every authority. |
| Build identity | Repository commit, state and resolution source in the provenance resource. Not shown in About. |
| Distribution condition | The builder selects a profile, `INTERNAL` by default and never inferred from the environment (`tools/release/build-windows-package.ps1:361-408`). It refuses `COMMERCIAL` before any build work (`:377-382`). It stages the profile's notice file into `app/` (`:620`) and the profile-neutral legal bundle into `app/legal/` (`:621-636`). It passes the marker as the jpackage description and copyright (`:668-669`). The selected profile is recorded only in `build-manifest.json`, outside the app-image (`:1051-1165`). **The running application cannot read its distribution condition.** |
| Install-relative resources | Precedent `GeoCeDGBundledToolCatalog.defaultDirectory()` (`:80-90`) locates `app/ggt-library` through `jpackage.app-path`. It reads an all-or-nothing verified record and never writes. |
| About content | `geocedg.help.about` shows: the title; `Workspace.About` (still "an author-review implementation candidate"); the GeoGebra baseline; the author line; one sentence pointing to `LICENSE`, `NOTICE.md` and `THIRD_PARTY.md` (`apps/geocedg/application-profile.yml:5787-5802`). Test: `G9U1ActionRegistryTest.java:98-107`. |
| Classic About | Inherited `GeoGebraMenuBar.showAboutDialog` (`GeoGebraMenuBar.java:282`, reached from `HelpMenuD.java:135`); not touched by GeoCeDG. |
| Version literals outside the authority | Tests: `GeoCeDGProfileTest.java:70-74`, `PreG9BP0PresentationThemeTest.java:423,437-441`, `PreG9BP1PublicSurfaceTest.java:149,291`. User guides: EN `:4,65,83,2131`; ES `:4,67,86,2234`; index `:9`. The guides are packaged as byte copies. |
| Pins | `verification-static-contracts.json:8-9` pins the bytes of `package.yml` and of `package-profile.schema.json`. |

The licensing facts the About surface must summarize are already recorded in four places:
- `LICENSE` §1 (EUPL-1.2 for GeoCeDG-authored software), §2–§3 (CC BY 4.0 for
  documentation and ordinary artwork, with exceptions and the brand boundary) and §4
  (inherited GeoGebra material);
- `NOTICE.md`, whose "Distribution condition" paragraph is profile-neutral and defers to
  the packaged notice;
- `docs/LICENSE.md`;
- the component matrix: "The complete binary is composite and cannot be labelled solely
  EUPL or globally Open Source/Free Software".

No new licensing fact is needed. Stop condition 7 of the G prompt therefore does not
trigger.

## 3. Alternatives for the version and version-date authority (`DQ-G-2`)

**A — extend the package-profile contract.** Add `application.version_date` to
`package.yml`, beside `application.version`.

**B — GeoCeDG-owned product-metadata resource.** A new resource (for example
`geocedg/resources/product/product-metadata.json`) holds the version and the date. A
deterministic check keeps `package.yml` consistent with it.

**C — derive the date from Git or from the build.** Use the commit date, a tag date or
the build time. Rejected outright:
- Git metadata is not product metadata;
- no tag is authorized;
- a commit date is not the date of the author's promotion;
- a build time breaks reproducibility and is exactly what the requirement says the
  version date is not.

| Criterion | A — extend `package.yml` | B — separate resource + check |
|---|---|---|
| Single source of truth | One document holds version and date. It is already "the source of truth for product identity" (`windows-packaging.md`, Inputs and identity). | Either two declared versions (`package.yml` for jpackage, the resource for the product) or `package.yml` loses its version. The first is a second copy that a check must reconcile. The second contradicts the approved single-authority clause. |
| Independently editable copies | None. The date sits next to the version in the same reviewed object. | The version exists twice. A check catches divergence, but only when it runs. |
| Normative impact | Additive amendment of `windows-packaging.md` (one field) and of the package-profile schema (one required property). | Amendment of the single-authority clause, a new schema, a new pinned input, and reworked P0 single-authority tests. |
| Static contract | Re-pin `package.yml` and the schema through the official catalog mechanism (phase-local, as P0 and E1-P did). | Same re-pins, plus a new pinned file and a new consistency check. |
| Java / application layer | `GeoCeDGBuildProvenance` gains one read-only field from the generated resource. | A new loader for a second resource, or a second Gradle-generated file. |
| Gradle / packaging | A second guarded extraction in the existing provenance task. The builder already reads `package.yml`. | A new Gradle input and generator. The builder reads two files or keeps its own version. |
| INTERNAL / NC consistency | Profiles carry no version (`windows-packaging.md`), so one value serves both. | Same, but through two files. |
| Determinism | Unchanged: no clock, no Git date. | Unchanged. |
| Migration and compatibility | One required field added. Older builders never read new `package.yml` revisions. No document-format effect. | Larger: P0/P1 tests, guard and builder change their authority. |
| Required evidence | Focused tests, STATIC re-pins, PACKAGING for the INTERNAL and NC sets. | The same, plus the consistency check and its pin. |

**Recommendation: A.** It is the least invasive option. It keeps the approved
single-authority invariant and adds no duplicate. Readiness option (b) is not
recommended: its only advantage is separating product metadata from a Windows-specific
document, and the approved packaging contract already places product identity there.

## 4. Contract candidate

### PM-1 Product identity

`package.yml` `application.name` (schema constant `GeoCeDG`) is the product name. The
Java constant stays. A focused test asserts that it equals the package-profile value. No
UI label is an identity authority.

### PM-2 Canonical version

`package.yml` `application.version` remains the only product-version authority. The form
is `MAJOR.MINOR.PATCH`, always shown in full. The Gradle guard keeps requiring exactly one
`"version"` key. No product source holds a literal version. Tests and user guides may
quote the canonical value. They are updated in the same commit as the authority, and
their pins are the existing P0/P1 assertions.

### PM-3 Canonical version date

- **Field.** New required `application.version_date`, an ISO 8601 calendar date
  `YYYY-MM-DD`. The schema gets a pattern; the Gradle guard checks for a valid calendar
  date and exactly one `"version_date"` key.
- **Semantics.** It is the date of the author-approved promotion of `application.version`,
  as designated by the author (§6). It is never computed from a clock, a commit, a tag or
  a file time. It is never a build date.
- **Generation.** The generated provenance resource carries it as
  `application.version.date`. Schema `2` → `3`; the loader accepts `3` only, fail-closed,
  as today for `2`.
- **Unchanged consumers.** The DXF fidelity sidecar keeps its schema and fields. Its
  `version` value follows PM-2.
- **Value for `1.0.0` (`DQ-G-4`, sub-decision b).**
  - Evidence points to 2026-09-16: P0, which adopted `1.0.0`, was closed in `1c97e780`
    on that date. The P0 report states the adoption but no calendar date.
  - If the author confirms that date, G introduces the field with `1.0.0` /
    `2026-09-16`, so every intermediate commit tells the truth.
  - Otherwise the field is introduced only by the version-transition commit of §6.

### PM-4 Build identity

The build identity is the existing provenance:
- repository commit;
- repository state: `CLEAN`, `DIRTY` or `UNAVAILABLE`;
- resolution source.

The meaning is unchanged. About shows it as `Build: <first 12 hex digits> (clean)`, or
`(modified)` / `unavailable`, and never next to a date. No wall-clock build date is
added, because the build stays reproducible. The version date and the build identity
are different lines with different labels.

### PM-5 Distribution condition (`DQ-G-3`)

**Authority.** The builder writes the packaging provenance into the copy itself:
`app/geocedg-distribution.json`. It is staged in the jpackage input root next to the
notice. It is therefore part of the app-image, the ZIP, the installers and
`app-image.SHA256SUMS.txt`. The builder derives it only from the selected
`package.yml` profile it has already validated, never from the environment:

```json
{
  "schema_version": 1,
  "record_kind": "geocedg-distribution-record",
  "profile_id": "INTERNAL | NC",
  "status": "<profile status>",
  "redistributable": false,
  "marker": "<profile marker>",
  "notice_file": "<profile notice file name>",
  "notice_sha256": "<sha-256 of the staged notice>",
  "application": { "name": "GeoCeDG", "version": "<PM-2>", "version_date": "<PM-3>" },
  "source_revision": "<40-hex commit embedded in desktop.jar>",
  "package_profile_sha256": "<sha-256 of package.yml>"
}
```

**Profile table.** The Gradle provenance task embeds in the generated resource each
declared profile's `id`, `status`, `marker` and `redistributable`, read from
`package.yml`. Java therefore holds no second literal copy of a marker.

**Runtime states.** Exactly four:

| State | Condition | About shows |
|---|---|---|
| `INTERNAL` | Launched by a jpackage launcher (`jpackage.app-path`). The record exists. It is valid, bounded in size and strictly parsed. It names a declared profile whose status, marker and `redistributable` equal the embedded table. Its `application` equals PM-2/PM-3. Its `source_revision` equals the embedded commit. The notice exists in `app/` with the recorded hash. | the INTERNAL marker verbatim, a localized "internal evaluation — not for redistribution", and the notice path |
| `NC` | Same conditions, profile `NC` | the NC marker verbatim, a localized "non-commercial distribution under PROFILE NC", and the notice and `app/legal/` paths |
| `DEVELOPMENT` | No jpackage launcher: Gradle `runGeoCeDG`, tests, a source checkout | localized "development build — not a distribution; not for redistribution" |
| `UNVERIFIED` | A jpackage launcher, but the record is missing, malformed, inconsistent or unbound. Or it names a profile that is not authorized for packaging. | localized "distribution condition not verified — not for redistribution" |

**What decides the state.**
- `jpackage.app-path` only *locates* the record, as the curated-library catalog already
  does. The condition comes from the record's verified content.
- No state is ever derived from a notice-file name, an installation path, an
  environment variable, a UI label or the artifact name.
- Every failure leads to a non-redistributable state.

**`COMMERCIAL`.** It is never shown as authorized. The builder refuses it before staging.
A record naming it, or naming any profile whose status is `not-authorized`, yields
`UNVERIFIED`. No runtime path can produce a redistributable commercial state.

**Packaging evidence.** `build-manifest.json` adds `application.version_date` and the
record's hash. The packaging check verifies the record against the manifest for the
built profile. This is a phase-local extension of `packaging.product`, under the E1-P
precedent (`DQ-G-1`).

Rejected alternatives for the source:
- **A per-profile JAR** (a Gradle property baked into `desktop.jar`). It breaks the
  profile-neutral main JAR, the hash-joined component audit and the single build per
  composition.
- **A jpackage `--java-options -D…` property.** It cannot bind the marker, the notice,
  the version and the revision, and the launcher environment can override it.
- **Inferring from the notice file present in `app/`.** That is inference from a file
  name, which is prohibited.

### PM-6 Licensing presentation

Localized texts in the live application profile, the single runtime authority for UI
texts (EN and ES), in this order:

1. GeoCeDG is a composite work; no single licence covers the whole product.
2. GeoCeDG-authored software: EUPL-1.2.
3. GeoCeDG-authored documentation and ordinary artwork: CC BY 4.0, with recorded
   exceptions. The GeoCeDG name and official marks are governed separately by the brand
   policy.
4. Inherited GeoGebra source code keeps its EUPL-1.2 grant. GeoGebra Language Files,
   Materials and the other third-party components keep their own terms.
5. The distribution condition of this copy (PM-5) and where its notice is.
6. Where the full texts are:
   - in a package: `app/legal/` (`LICENSE`, `NOTICE.md`, `THIRD_PARTY.md`, `LICENSES/`)
     and `app/<notice>`;
   - in development: the repository files of the same names, plus `docs/LICENSE.md` and
     the component matrix.

The About surface never:
- labels the product as licensed solely under EUPL-1.2, or as Open Source or Free
  Software;
- gives the upstream GeoGebra licence URL as the complete authority;
- reproduces full legal texts.

A focused consistency test asserts:
- each summarized fact still appears in its authority file (`LICENSE` §1/§2/§4,
  `NOTICE.md` "Distribution condition", `docs/LICENSE.md`);
- every route it names exists in the repository, or in the packaged `app/legal/`
  inventory under PACKAGING.

The summary is presentation; the files remain the authority.

### PM-7 About surface

The existing `geocedg.help.about` action and its read-only text dialog are kept: no new
framework or window type. The content order is the same in EN and ES:

1. `GeoCeDG <version>`;
2. version date (ISO form, locale-neutral);
3. build identity (PM-4);
4. distribution condition (PM-5);
5. the product description, with `Workspace.About` rewritten without "author-review
   implementation candidate";
6. GeoGebra upstream baseline (`GeoGebraConstants.VERSION_STRING`, unchanged);
7. the author line (unchanged);
8. licensing (PM-6).

The dialog title stays `applicationTitle()`.

### PM-8 Other surfaces

- The window title and the guide-window title keep the form `GeoCeDG <version>` (and
  `— <file>`).
- jpackage `--app-version` and the artifact names keep reading PM-2.
- The installers keep `upgrade_uuid`, so `1.1.0` upgrades `1.0.0`.
- The user guides quote the canonical version and are updated with it.

### PM-9 Classic preservation

Classic keeps the inherited About, menu and title. None of these is modified:
`GeoGebraMenuBar`, `HelpMenuD`, `MacApplicationListener`. Evidence:
- the upstream-boundary check;
- a focused Desktop test that the Classic help route still reaches the inherited dialog
  and receives no GeoCeDG metadata;
- the author smoke.

### PM-10 Determinism and reproducibility

- The generated resource is a pure function of `package.yml` and the commit identity.
- The record is a pure function of the selected profile, `package.yml`, the commit and
  the notice bytes.
- Nothing reads a clock.
- `build-manifest.json` keeps its existing `source_timestamp` (the commit time, for
  normalized archives). The About surface does not show it.

### PM-11 Validation obligations for G

Focused Desktop tests:

| Area | Assertions |
|---|---|
| Single authority (PM-1 to PM-3) | name, version and date equal `package.yml`; exactly one `version` and one `version_date` key; valid calendar date |
| Schema `3` | the provenance loader rejects schema `2` |
| Build identity (PM-4) | rendering for `CLEAN`, `DIRTY` and `UNAVAILABLE` |
| Distribution states (PM-5) | each of the four states, from fixture records in a temporary directory; `COMMERCIAL` and `not-authorized` records give `UNVERIFIED`; mismatched version, date, revision or notice hash gives `UNVERIFIED` |
| About content (PM-6, PM-7) | content and order in EN and ES; the licensing consistency test |
| Classic (PM-9) | Classic About unchanged |
| Existing pins | `GeoCeDGProfileTest`, P0 and P1 version assertions updated in the version-transition commit only |

Other required evidence:
- STATIC with the re-pinned `package.yml` and schema;
- PACKAGING `-CheckToolchain`;
- PACKAGING `-VerifyPackagingArtifacts` on an INTERNAL set and an NC set built from the
  exact candidate;
- a `COMMERCIAL` attempt that refuses and leaves the output root unchanged (E1-P recipe);
- the author smoke: About in EN and ES on a development run, an INTERNAL app-image and an
  NC app-image or ZIP; Classic About.

### PM-12 Compatibility

- No document-format, XML, preference or unit change.
- The provenance resource is internal to `desktop.jar`: schema `3` is not a document
  format.
- The DXF sidecar schema is unchanged.
- An older builder never meets a newer `package.yml`: both live in the same commit.

## 5. Readiness proposals reconciled

| Readiness proposal | This candidate |
|---|---|
| `DQ-G-2` (b) "a GeoCeDG product-metadata resource … recommended (b)" | Replaced by A (§3): (b) duplicates or relocates the approved single version authority. |
| `DQ-G-3` "packaging writes the profile id into the build provenance; development builds show INTERNAL" | Refined. The build provenance in `desktop.jar` is profile-neutral, and one JAR serves both profiles. The profile is therefore written into an install-level record bound to that JAR (PM-5). Development builds show a distinct, conservative `DEVELOPMENT` condition rather than impersonating a packaged `INTERNAL` copy. |
| `DQ-G-4` "the G candidate carries `1.1.0` and the proposed date" | Kept, with a precise lifecycle, the date-designation rule and stop conditions (§6). |

## 6. Version lifecycle `1.0.0` → `1.1.0` (`DQ-G-4`)

### 6.1 The tension and why it cannot be avoided

Approval names an exact commit. Its bytes must already contain whatever version the
approved product shows. The literal reading "every build shows `1.0.0` until approval"
can therefore be met by no mechanism that also approves exactly what was verified.

A post-approval version commit does not help:
- it still shows `1.1.0` before *its own* approval;
- it changes productive bytes after acceptance: the generated resource in
  `desktop.jar`, the guides, `--app-version`;
- so it needs a fresh campaign, or it ships unverified bytes.

The coherent reading, proposed for author confirmation, is the following. The canonical
visible version is the version of the approved and published product line. That is
`1.0.0` at every approved or published state until G is approved. A `1.1.0` that exists
only in an unapproved, unpublished G candidate is a proposal under review, not a
canonical or released version.

The template rule for frozen candidates (`task-template.prompt.md`, "Acceptance and
closeout") already forbids encoding approval state in candidate bytes. The approval
status of `1.1.0` therefore lives only in the author's closeout record, never in product
bytes.

### 6.2 Mechanisms compared

| Mechanism | Productive bytes after acceptance | Verification | Shows an unapproved version as released | Verdict |
|---|---|---|---|---|
| **M1 — approve what was verified.** The candidate carries `1.1.0` and the designated date. Approval adopts them. | none | one frozen campaign on `T` | No. The copies are unpublished, carry the `DEVELOPMENT` or `INTERNAL` condition, and no release is implied. | **recommended** |
| **M2 — post-approval version commit.** Approve `T₁` at `1.0.0`, then commit `1.1.0` and the date. | yes (`desktop.jar` resource, guides, installers) | either unverified bytes (rejected) or a second complete campaign on `T₂`. Approving `T₁` would not be G approval. | It shows `1.1.0` in `T₂` before `T₂`'s approval anyway. | rejected: the cost doubles and the tension remains |
| **M3 — candidate metadata with an approval flag and later activation.** The product reads an "activated" state from a documentary file or a later record. | yes: build output changes without a productive source change | none for the activation delta | The approval state is encoded in product inputs. | rejected: violates the frozen-candidate rule and hides a productive change |

### 6.3 Lifecycle contract (M1)

| Stage | Version / date in product bytes | Canonical? | Observable where |
|---|---|---|---|
| L0 Published base (`abe12893`) | `1.0.0` / none | yes | published product |
| L1 G development commits | `1.0.0` / `2026-09-16` if confirmed (PM-3), else none | `1.0.0` yes | local development builds (`DEVELOPMENT`) |
| L2 Version-transition commit: the last productive commit; `package.yml`, guides and version tests change together | `1.1.0` / `D` | **no — proposed** | local candidate builds only |
| L3 Verified technical candidate `T` (contains L2) | `1.1.0` / `D` | **no — proposed** | the frozen campaign, INTERNAL and NC builds of `T` for PACKAGING and author smoke; never published |
| L4 Author approval of `T` (`G = PASS — AUTHOR APPROVED`) | unchanged bytes | **yes, from the approval** | — |
| L5 Documentary closeout `C` (descends from `T`) | unchanged bytes. `git diff --name-only T C` touches no product, build, packaging, test or verification path. | yes | — |
| L6 Publication (separate exact-SHA instruction) | unchanged bytes | yes | published `main` shows `1.1.0` / `D` |
| L7 Public release (separate decision: tag, binaries, redistribution) | — | — | not implied by any stage above |

**Date designation rule.**
- `D` is fixed by the author in writing before `T` is frozen: in the G implementation
  authorization or in a later instruction naming `D`.
- `D` is the date the author designates for the promotion. Approval of `T` adopts it as
  the version date of `1.1.0`.
- Approval may come after `D`. The closeout then records `VERSION_DATE = D (adopted)`
  explicitly.
- If the author wants another date, or another version, the only route is a
  metadata-only successor `T′`, with its own complete frozen campaign. Evidence never
  carries across commits.

`1.1.0` and `D` become:
- **observable** from L2, and only in unpublished candidate builds;
- **authoritative** at L4;
- **published** at L6.

They are never "released" by any of these stages.

**Stop conditions.**
- Any product, build or packaging path changes after `T`.
- `D` is undesignated at freeze time.
- The approval does not adopt `D`, and no successor is authorized.
- A version other than `1.1.0` appears.
- A tag, release or `COMMERCIAL` artifact is requested inside G.
- The candidate report encodes approval state.

## 7. Required author dispositions

| Id | Disposition requested |
|---|---|
| `DQ-G-2` | approve Alternative A (PM-1 to PM-4), or choose B with the single-authority amendment it requires |
| `DQ-G-3` | approve PM-5: the install-level distribution record, the four runtime states, and `DEVELOPMENT` as the conservative development condition |
| `DQ-G-4` a | approve M1 and the stages of §6.3, including the reading of "visible version" in §6.1 |
| `DQ-G-4` b | confirm or reject `2026-09-16` as the version date of `1.0.0` (PM-3) |
| `DQ-G-4` c | designate `D` for `1.1.0` before the G candidate is frozen (now, or by a later instruction) |
| PM-6, PM-7 | approve the licensing wording plan and the About order |

## 8. Risks

| Risk | Handling |
|---|---|
| Builder and packaging-check changes touch operational verification infrastructure | E1-P precedent. A contract addition inside `packaging.product`, with PACKAGING evidence for both profiles and `infra.contract-boundary`. A change to `verify.ps1`, the registry or inventory schemas, an adapter, the receipt or closeout flow, or an `infra.*` check is global and stops G for a `FINAL` escalation request (`DQ-G-1`). |
| The distribution record is removed or edited in an installation | It yields `UNVERIFIED`, a non-redistributable state. The legal condition stays in the packaged notice. |
| An NC copy is shown as `UNVERIFIED` after legitimate re-packaging by a third party | Accepted. Only the GeoCeDG builder produces a verified record. |
| `1.1.0` circulates before approval | Candidate builds are never published. Their condition line says `DEVELOPMENT` or `INTERNAL`. |

## 9. Normative transcription at G (after approval only)

| Document | Change |
|---|---|
| `geocedg/specs/packaging/windows-packaging.md` | add `version_date`, the distribution record and the build-manifest field |
| `geocedg/specs/ui/application-profile.md` | About content paragraph (PM-6, PM-7) |
| `geocedg/specs/operations/package-profile.schema.json` | one required property |
| static-contract pins | re-pinned through the official mechanism |
| `docs/upstream/modified-files.yml` | purpose of `source/desktop/desktop/build.gradle.kts` |
| this candidate | either becomes a new normative specification or is transcribed into the two documents above, as the author chooses |
