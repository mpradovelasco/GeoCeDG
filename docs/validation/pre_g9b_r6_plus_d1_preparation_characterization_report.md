# PRE-G9B-R6-plus-D1 preparation characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (D1 canonical prompt)
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PRE-G9B-R6-plus-D1        = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`D1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md)
(sections C1–C11). The prompt holds the resulting contracts; this report does
not restate them and carries no authority of its own. The decisions of the
preparation instruction are in the
[D1 author-decision record](pre_g9b_r6_plus_d1_author_decisions_record.md).
Its machine-readable mirror is
[`pre-g9b-r6-plus-d1-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-preparation-characterization.json).
The normative authorities are
[ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md) and
the [unit specification](../../geocedg/specs/units/unit-system.md) v1.0.

## 1. Entry gate

```text
P_R6PLUS_D0          = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c
                       tree 83b32b6cb579dc28162f990160c3eef0b1928be6
local main           = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c (tree 83b32b6c…)
origin/main          = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c (tree 83b32b6c…)
live remote main     = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c (git ls-remote origin refs/heads/main)
worktree             = clean (git status --porcelain=v1: empty)
D0 state             = PASS — AUTHOR APPROVED — PUBLISHED
```

## 2. Merged-branch cleanup

The instruction asked for the inspection, and deletion with `git branch -d`
only, of two local branches. `HEAD` was first moved to `main` (same commit,
clean tree). The only worktree is the repository root, on `main`. No file
under `tools/` references either branch (`git grep`); the only tracked
mentions of `phase/pre-g9b-r6-plus-*` names are historical evidence fields in
`geocedg/validation/pre-g9b-r6-plus/*.json`, which pin no ref.

| Branch | Tip | Ancestor of `main` | `git log main..<branch>` | Worktree | Retention reason | Result |
|---|---|---|---|---|---|---|
| `phase/pre-g9b-r6-plus-b-export-surface` | `0ef616bb7` (`P_R6PLUS_B`) | yes (exit 0) | empty | none | none | `Deleted branch … (was 0ef616bb7)` |
| `phase/pre-g9b-r6-plus-d0-prompt` | `3268f9b99` (`P_R6PLUS_D0`) | yes (exit 0) | empty | none | none | `Deleted branch … (was 3268f9b99)` |

No remote branch and no other local branch was touched. Remaining local
branches: `feature/g9u1-construction-workspace-planning`,
`feature/g9u1-construction-workspace-planning-after-r6`, `main`,
`maintenance/retire-orphaned-historical-verification-suites` and the
preparation branch.

The preparation branch `phase/pre-g9b-r6-plus-d1-prompt` was created from the
exact commit `3268f9b99d20c5d9cf62f25d8066ee0a8e47765c`.

## 3. Method

- Static reading of every seam the prompt cites, at the base, with each
  citation re-checked line by line before it was written.
- Scratch-only probes. Their sources and outputs live in the session
  scratchpad; nothing was written under `source/`, and the Desktop probe
  classes were removed from the build output afterwards (a recompile of the
  test sources without the probe directory, the probe JUnit XML and the
  compile-transaction stash copies deleted). The worktree stayed clean apart
  from the documentary files of this preparation.
- No product, test, build, registry, schema, verifier, specification or ADR
  file changed. No `PHASE`, `INTEGRATION` or `FINAL` ran.

## 4. Probe `P-BINARY64`: canonical factor on the supported JDKs

**Supported runtimes.** The developer guide requires complete JDKs 17 and 25
and Java 22 to launch Gradle. Shared code compiles with toolchain 17; shared
and Desktop tests run on 17; the Desktop product launcher and the package
runtime are 25 (prompt C5).

**Method.** One single-file Java program, run with `java FactorProbe.java es-ES
400000` on each runtime below. It compares, for every value:

- the runtime's `Double.toString`;
- an exact `BigDecimal` implementation of the Java SE 19+ `Double.toString`
  specification (the decimals that round to the value, the minimal length, the
  length-1-or-2 rule, the closest decimal, even significand on a tie, the
  ties-to-even interval ends, and the plain/scientific layout rules);
- a naive writer: the first `HALF_EVEN` rounding to `n` significant digits that
  round-trips.

It also checks `Double.parseDouble` of the reference string. The default
locale was `es_ES` throughout.

| Runtime | Reference entries differing from the exact writer | Sweep values differing (of 400 000) | Exact writer, reference SHA-256 | Exact writer, sweep SHA-256 |
|---|---|---|---|---|
| Corretto 17.0.10 | 8 | 581 | `4581acb2…187841` | `5f2cec09…352303` |
| OpenJDK 17.0.8.1 | 8 | 581 | `4581acb2…187841` | `5f2cec09…352303` |
| Java 22.0.2 | 0 | 0 | `4581acb2…187841` | `5f2cec09…352303` |
| Temurin 25.0.4 | 0 | 0 | `4581acb2…187841` | `5f2cec09…352303` |

Full hashes: reference
`4581acb270d24b64840dd58e766973796263ef21f524feed04fde2fb08187841`, sweep
`5f2cec099ad0f70c4a2398e1ae676f47911c02e7e10b453876e209ba73352303` (SHA-256 of
the canonical strings, one per line, ASCII). The sweep is seeded
(`SplittableRandom(20261002)`): half uniform positive finite bit patterns, half
short decimals `m·10^e` with `1 ≤ m < 10^6`, `-12 ≤ e < 9`. On every runtime
the naive writer differed from the specification on the two `MIN_VALUE`
entries and on no sweep value, and `Double.parseDouble` round-tripped every
reference string. Probe source SHA-256:
`b947a31801d6c5a9fbbfd7d6a80af66892f428047576e0b95a81e01912fd03f5`.

**Reference table.** 48 entries, 45 distinct values. *Canonical* is the §8.4
string; the next column shows JDK 17 `Double.toString` where it differs
(`=` otherwise; JDK 22 and 25 equal the canonical column everywhere); the last
column shows the naive writer where it differs.

| # | Java expression | Raw bits | Canonical (§8.4) | JDK 17 `Double.toString` | Naive writer |
|---|---|---|---|---|---|
| 1 | `0.0254` | `0x3F9A027525460AA6` | `0.0254` | = | = |
| 2 | `0.3048` | `0x3FD381D7DBF487FD` | `0.3048` | = | = |
| 3 | `0.9144` | `0x3FED42C3C9EECBFB` | `0.9144` | = | = |
| 4 | `1609.344` | `0x409925604189374C` | `1609.344` | = | = |
| 5 | `1852.0` | `0x409CF00000000000` | `1852.0` | = | = |
| 6 | `1.0` | `0x3FF0000000000000` | `1.0` | = | = |
| 7 | `0.001` | `0x3F50624DD2F1A9FC` | `0.001` | = | = |
| 8 | `0.01` | `0x3F847AE147AE147B` | `0.01` | = | = |
| 9 | `0.1` | `0x3FB999999999999A` | `0.1` | = | = |
| 10 | `0.2` | `0x3FC999999999999A` | `0.2` | = | = |
| 11 | `0.3` | `0x3FD3333333333333` | `0.3` | = | = |
| 12 | `1.0E-6` | `0x3EB0C6F7A0B5ED8D` | `1.0E-6` | = | = |
| 13 | `2.54E-5` | `0x3EFAA242B58735EA` | `2.54E-5` | = | = |
| 14 | `1.0E-9` | `0x3E112E0BE826D695` | `1.0E-9` | = | = |
| 15 | `1.0E7` | `0x416312D000000000` | `1.0E7` | = | = |
| 16 | `Math.nextDown(1.0E7)` | `0x416312CFFFFFFFFF` | `9999999.999999998` | = | = |
| 17 | `Math.nextDown(0.001)` | `0x3F50624DD2F1A9FB` | `9.999999999999998E-4` | = | = |
| 18 | `Double.MIN_VALUE` | `0x0000000000000001` | `4.9E-324` | = | `5.0E-324` |
| 19 | `Double.MIN_NORMAL` | `0x0010000000000000` | `2.2250738585072014E-308` | = | = |
| 20 | `Double.MAX_VALUE` | `0x7FEFFFFFFFFFFFFF` | `1.7976931348623157E308` | = | = |
| 21 | `1.0E-300` | `0x01A56E1FC2F8F359` | `1.0E-300` | = | = |
| 22 | `1.0E300` | `0x7E37E43C8800759C` | `1.0E300` | = | = |
| 23 | `1.495978707E11` | `0x42416A5D2D360000` | `1.495978707E11` | = | = |
| 24 | `9.4607304725808E15` | `0x4340CE3DFB912360` | `9.4607304725808E15` | = | = |
| 25 | `3.0856775814913673E16` | `0x435B6804BE5727A2` | `3.085677581491367E16` | `3.0856775814913672E16` | = |
| 26 | `1.616255E-35` | `0x38B57BD4AD4EFA96` | `1.616255E-35` | = | = |
| 27 | `Math.scalb(1.0, -10)` | `0x3F50000000000000` | `9.765625E-4` | = | = |
| 28 | `Math.scalb(1.0, -20)` | `0x3EB0000000000000` | `9.5367431640625E-7` | = | = |
| 29 | `Math.scalb(1.0, 60)` | `0x43B0000000000000` | `1.152921504606847E18` | `1.15292150460684698E18` | = |
| 30 | `1.0E23` | `0x44B52D02C7E14AF6` | `1.0E23` | `9.999999999999999E22` | = |
| 31 | `2.0E23` | `0x44C52D02C7E14AF6` | `2.0E23` | `1.9999999999999998E23` | = |
| 32 | `0.1 + 0.2` | `0x3FD3333333333334` | `0.30000000000000004` | = | = |
| 33 | `1.0 / 3.0` | `0x3FD5555555555555` | `0.3333333333333333` | = | = |
| 34 | `Math.PI` | `0x400921FB54442D18` | `3.141592653589793` | = | = |
| 35 | `Math.E` | `0x4005BF0A8B145769` | `2.718281828459045` | = | = |
| 36 | `1.0 + Math.ulp(1.0)` | `0x3FF0000000000001` | `1.0000000000000002` | = | = |
| 37 | `0.1 + 0.7` | `0x3FE9999999999999` | `0.7999999999999999` | = | = |
| 38 | `2.82879384806159E17` | `0x438F67EA69ED3795` | `2.82879384806159E17` | `2.82879384806159008E17` | = |
| 39 | `1.18575755E-316` | `0x00000000016E3600` | `1.18575755E-316` | = | = |
| 40 | `4.8726570057E288` | `0x7BE0000000000000` | `4.8726570057E288` | `4.8726570056999995E288` | = |
| 41 | `1.0E-5` | `0x3EE4F8B588E368F1` | `1.0E-5` | = | = |
| 42 | `9.999999999999999E22` | `0x44B52D02C7E14AF6` | `1.0E23` | `9.999999999999999E22` | = |
| 43 | `8.41E21` | `0x447C7E83209E90B2` | `8.41E21` | `8.409999999999999E21` | = |
| 44 | `5.0E-324` | `0x0000000000000001` | `4.9E-324` | = | `5.0E-324` |
| 45 | `1.0E22` | `0x4480F0CF064DD592` | `1.0E22` | = | = |
| 46 | `1.0E21` | `0x444B1AE4D6E2EF50` | `1.0E21` | = | = |
| 47 | `123456789.0` | `0x419D6F3454000000` | `1.23456789E8` | = | = |
| 48 | `0.30000000000000004` | `0x3FD3333333333334` | `0.30000000000000004` | = | = |

The raw bits were recomputed independently from the canonical strings with the
.NET parser of PowerShell 7 (IEEE-correct parsing). They are evidence for the
fixture of `T-CANONICAL-FACTOR`; `D1` re-derives them in its own test run.

**Conclusion.** JDK 17 does not meet the §8.4 writer specification, so `D1`
cannot rely on `Double.toString` and must provide the deterministic canonical
writer of prompt C5. An exact `BigDecimal` selection is feasible and
runtime-independent on the supported JDKs.

## 5. Probe `P-TWO-WINDOWS`: clipboard buffers and cross-document paste

**Method.** A scratch JUnit class in the package `org.geocedg.desktop`, added to
the Desktop test source set only through a Gradle init script, run with
`gradlew.bat :desktop:desktop:test --tests …` on the test JVM (Corretto
17.0.10). Two `AppGeoCeDG` instances on the `G9U1TestApp` host; copy and paste
through the real key dispatcher (`Ctrl+C`, `Ctrl+V`). A first attempt hung in
the real "save changes?" dialog of File → New, because the paste had left the
document unsaved; that JVM was stopped and the probe set the document saved
before New (the trap is recorded in the prompt's harness rules).

| Observation | Result |
|---|---|
| `a.getCopyPaste() != b.getCopyPaste()` | `true` (both `org.geogebra.desktop.util.CopyPasteD`) |
| after `Ctrl+C` in A | A's buffer not empty; B's buffer empty |
| `Ctrl+V` in B | B's object count 1 → 1; B's construction XML unchanged (its full XML differs only in a GUI view section, not in the construction) |
| `Ctrl+V` in A (same window) | A's object count 1 → 2; no undo store within 2 s; history size unchanged |
| A: set saved, then File → New (`clearConstruction`) | `true`; A's buffer still not empty; 0 objects |
| `Ctrl+V` in A after New | 1 object, label `P` |

Result JUnit XML: 1 test, 0 failures, 0 errors (SHA-256
`7934cd29523793bf89b8ab4285301cbf34f607680d872a69a192f0fb1985446b`, kept in the
scratchpad). Probe source SHA-256:
`5bb44c6f72fa17d9e79d24d0073d13354ba5b03e4f6e9b7564719b1783f9f6d9`.

**Conclusion.** Each window owns its buffer; no Desktop route pastes one
window's buffer into another; the cross-document route is copy, New or Open,
paste in one window; an ordinary paste stores no undo point inside
`pasteFromXML`. These facts, and the static reading of `CopyPasteD`,
`InternalClipboard` and the key dispatcher, are prompt C9.

## 6. Probe `P-OLDER-READER`: the base build reading `<geocedgUnits>`

**Method.** A second scratch class in the same run. On an `AppGeoCeDG` with one
point, the document XML was given a `<geocedgUnits version="1"
construction="mm" usmMetersPerUnit="0.0254"/>` line after the `<construction>`
start tag and processed with a clearing `processXMLString`; then the same with
`version="99"`; then an `evalXML` fragment containing the element and one point.

| Observation | Result |
|---|---|
| clearing load with the element | loads; 1 object; element absent on re-save |
| clearing load with `version="99"` | loads; element absent on re-save |
| `evalXML` with the element | the point is created; no element in the document |
| log | `MyXMLHandler.startConstructionElement[3093]: unknown tag in <construction>: geocedgUnits`, once per parse |

Result: 1 test, 0 failures, 0 errors. Probe source SHA-256:
`a4adc3f2792858b8e6743c7576c0f179c6defc056711151344b4732ab624989e`.

**Conclusion.** The specification's older-reader limitation (§8.9) holds at the
base: the element is logged, ignored and lost on re-save.

## 7. Static characterization summary

The seam-by-seam results, with citations, are prompt C1–C11. The findings that
shape contracts beyond a direct reading of the specification:

| # | Finding | Prompt |
|---|---|---|
| 1 | the parse flag does not identify a clearing load: archives with macros parse `geogebra.xml` with `clearConstruction = false` and a forced purpose, so the effective purpose must be captured at document-construction start | C1 |
| 2 | `getConstructionXML` also serializes macro constructions and swallows writer exceptions | C2 |
| 3 | Desktop File → Open already preflights `.cedg` and `.ggb` on a scratch app; the `MyXMLio` restore triggers only for spatial failures; every open failure shows the generic `LoadFileFailed` | C3 |
| 4 | Desktop undo never de-duplicates snapshots; kernel settings changes create no undo point; `isSaved()` treats an unstarted construction as saved | C4 |
| 5 | JDK 17 `Double.toString` does not meet §8.4 | C5 |
| 6 | New takes its undo baseline before the preferences XML, which is itself a clearing load; startup and New Window bypass `fileNew`; the startup decision is a constructor-local variable | C6 |
| 7 | the status bar has an `addSegment` extension point but refreshes only for layer and locale; no non-blocking notice mechanism exists | C8 |
| 8 | the Desktop copy buffer is per window and never cleared; Insert File replaces it | C9 |
| 9 | the specification's §9 evidence for New window and startup, and its §11 clear point, drift from the base | prompt *Contradictions* |

## 8. Obligations and planned tests

The obligation-to-test map is the `T-*` table of the prompt's *Required tests
and commands* (27 identifiers), with the eleven requested decisions
`DQ-D1-1` to `DQ-D1-11` and their default contracts in *Decisions requested
before authorization*.

## 9. Proposed acceptance

`VERIFICATION_CLASS = GLOBAL_IMPACT` (author-frozen), one `FINAL` on one exact
frozen clean candidate after focal, adjacent, catalog and boundary evidence
(prompt *Required tests and commands*). At the base the `FINAL` profile
resolves to 68 registry nodes (`required_for_profiles` contains `FINAL` in
`geocedg/specs/operations/verification-registry.json`), including
`junit.shared.final`, `junit.desktop.final`, the packaging, compile, Python and
Checkstyle leaves and the static and infrastructure leaves; no phase selection
is needed and none is proposed.

## 10. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (documentary preparation)
SERIALIZATION CHANGE               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT                   — NO CHANGE REQUIRED (no workstation, toolchain,
                                     Gradle, Conda, packaging, download or
                                     environment change)
GUIDE_IMPACT                       = NONE
GUIDE_JUSTIFICATION                = documentary preparation of a phase prompt; no
                                     observable behavior, command, workflow or
                                     file-format contract changes
```

## 11. Changed paths of the preparation

- `.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md` (new)
- `docs/validation/pre_g9b_r6_plus_d1_author_decisions_record.md` (new)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-author-decisions.json` (new)
- `docs/validation/pre_g9b_r6_plus_d1_preparation_characterization_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-preparation-characterization.json` (new)
- `docs/roadmap/geocedg_roadmap.md` (status lines only)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The validation of the prompt contract and the `STATIC` run of the frozen
preparation candidate are reported outside this artifact, because it cannot
name its own commit.
