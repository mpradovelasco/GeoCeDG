# POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION — characterization report

```text
TASK                      = POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION
OBSERVATION               = OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP
AUTHORIZATION             = CHARACTERIZATION AND DESIGN AUTHORIZED — CORRECTION NOT AUTHORIZED
                            (author instructions of 2026-10-10; E3/E3-R1 closeout record §8)
BASELINE                  = 31286a2355cabebc69c3937442e7f15636da12ba
                            tree d223d7400b2eabacddafba874b49eae581c5c6c4 (published P_R6PLUS_E3)
VERIFICATION_CLASS        = DOCUMENTATION_STATUS_ONLY (frozen at entry, before any tracked edit)
PRODUCT_PHASE_EFFECT      = NONE
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this file
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved = false
```

Machine-readable mirror:
[`post-e3-es-command-lookup-characterization.json`](../../geocedg/validation/post-e3/post-e3-es-command-lookup-characterization.json).
Execution contract:
[canonical prompt](../../.github/prompts/tasks/post-e3-es-command-lookup-characterization.prompt.md).

## 1. Result in brief

| Question | Answer |
|---|---|
| Reproducible? | yes, deterministically, in the test harness and in the real windowed product |
| Caused by loading a legacy `.ggb`? | **no**. The observation's attribution is incorrect. A legacy load is only one of several ways to fill the reverse command table before the language changes |
| Mechanism | the reverse command table is filled lazily. A language change through `AppD.setLanguage(String)`, `AppD.setLocale(Locale)` or the scripting API `setLanguage` swaps the command bundle but does not refill a table that is already filled. `App.getReverseCommand` refills only a missing table, never a stale one, so localized names of the new language are unknown until an unrelated consumer refreshes the table |
| Product routes | correct in every scenario: the product-language action, startup with `--language=es`, and File > Open before or after the switch |
| User-visible? | only through the scripting language API (`GgbAPI.setLanguage`, JavaScript `ggbApplet.setLanguage`). After it, a Spanish command typed in the real Algebra Input gives "Comando desconocido". English names keep working and no object is created wrongly |
| Pre-existing? | yes. Identical at the published pre-`E3` baseline `ff560991`, in harness and real GUI alike. The implicated methods are upstream code unchanged since the Copybara import, with no GeoCeDG marker |
| Introduced by a phase? | no. `E2` and `E3` commands (`CotaAlineada`, `MarcoISOA`) behave like every other command |
| Test-harness artifact? | the original observation is one. The `E3` test used `setLanguage("es")`, the `String` overload, which is not the product route |
| Correction | not required for `E3`, legacy documents or the product language routes; options for the scripting route in §7; no prompt prepared |

## 2. Entry gate

```text
local main = origin/main = live remote main = 31286a2355cabebc69c3937442e7f15636da12ba
tree d223d7400b2eabacddafba874b49eae581c5c6c4; worktree clean
branch phase/post-e3-es-command-lookup-characterization created from that commit
reproduction baselines:
  published pre-E3   ff56099184544e5988c63e0ea3339e3d0fe01fac  tree 1cdd1e986df785f5f16e7887300d9034e5e4a677
                     (detached scratch worktree)
  published E3/E3-R1 the baseline above (product tree of 7cd50116)
```

## 3. Method

Scratch probes only, compiled into the Desktop test source set through a Gradle
init script. No tracked source, test or build file was changed.

| Probe | Shape |
|---|---|
| `PostE3EsLookupProbeTest.scenarios` (S1–S19) | JUnit host (`G9U1TestApp`) |
| `PostE3EsLookupProbeTest.childJvm` (C1–C5) | plain-`main` child JVM with isolated preferences, as the `E3` scenario JVM |
| `PostE3EsWindowedProbeTest` (W1–W7) | real `GeoCeDG.main` with an isolated settings file under JDK 25. The real product-language menu item and chooser are used, File > Open goes through `GuiManagerD.loadFile`, and commands are typed into the real Algebra Input. A watcher reads and closes modal dialogs |

Each scenario snapshots the localization state before any lookup: language,
lookup strategy, `isCommandChanged()`, the identities of the current and
previously used command bundles, and the size of the reverse table and its
`punto` entry. It then queries `Localization.getReverseCommand` and
`App.getReverseCommand` for `Punto`, `Distancia`, `CotaAlineada`, `MarcoISOA`
and their English heads, and types `Distancia(...)`, `Distance(...)` and
`Punto(Segmento(...))`.

## 4. Evidence matrix

All rows give identical results on the pre-`E3` baseline, except that
`MarcoISOA` / `IsoABorder` do not exist there yet.

| Dimension (prompt §Objective) | Scenarios | Result |
|---|---|---|
| 1. fresh application vs loaded document | S1 vs S2, S5; C1 vs C2 | both fail through the `String` overload; both succeed through `setLanguage(Locale)` (S9, S10). A load is not required |
| 2. File > Open vs `loadXML` | S4 vs S2; S11 vs S10; W1–W3 | identical behaviour for each language route |
| 3. language change before vs after loading | S3 (before: works) vs S2 (after: fails) | a load after the switch refills the table in the new language. A switch after the load leaves the English table |
| 4. `App.getReverseCommand` vs `Localization.getReverseCommand` | every scenario | always equal. `App.getReverseCommand` adds only `initTranslatedCommands()`, which refills a missing table, not a stale one |
| 5. dictionary initialization and invalidation | snapshots; S6; C3 | failing state: table of 535 English entries with `punto` absent, Spanish command bundle current, `isCommandChanged() = true`. `getCommandDictionary()` (S6) or the host `setLanguage(Locale)` refills it (989 entries). Any lookup of a non-command name before the switch fills the English table (C3); legacy macros are such names |
| 6. canonical English vs localized | every scenario | English heads always resolve, Spanish names only with a current table |
| 7. `E2` and `E3` commands | `CotaAlineada`, `MarcoISOA` columns | same behaviour as `Punto` and `Distancia` |
| 8. GeoCeDG vs Classic | S7, S12, S8 | same mechanism. A fresh Classic (S8) works only because its table is still empty when the language changes |
| 9. real GUI vs test harness | W1–W7 vs S/C | real GUI product routes work (W1, W2, W3). The scripting API after a load (W5), without a document (W6) or from JavaScript (W7) gives "Comando desconocido. : Distancia" and the same for `MarcoISOA`; English input keeps working. W4 works only because the File > Open after the switch refills the table |

The `E3` test check of Spanish names in a fresh application (`names()`) passed
only because of fill timing. In its child JVM the table was still empty when
`setLanguage("es")` ran (C5 works; C1, a fresh application without the `E3`
helper's steps, fails).

## 5. Root cause (confirmed)

```text
App.getReverseCommand(name)            [upstream; unchanged since the import]
  -> initTranslatedCommands()          refills only if the command bundle or subCommandDict is null
  -> Localization.getReverseCommand()  reads translateCommandTable as it is

Refill on a language change happens only through consumers that check isCommandChanged():
  getCommandDictionary(), getSubCommandDictionary(), getOfferedCommands(),
  updateCommandDictionary() (reached from AppD.setLanguage(Locale) -> setLabels()).

AppD.setLanguage(Locale)  = setLocale + updateConstructionLanguage + setLabels -> coherent
AppD.setLanguage(String)  = setLocale only                                     -> stale table
AppD.setLocale(Locale)    = bundles and fonts only                             -> stale table
GgbAPI.setLanguage(String) -> app.setLanguage(String)                          -> stale table
```

This is the lazy-refill mechanism recorded by `PRE-G9B-R5-B-R1` (corrective
candidate report §8.2), which fixed the GeoCeDG product-language action to use
`AppD.setLanguage(Locale)`. The shared lifecycle was deliberately left unchanged
then. The present observation reaches the same mechanism through entry points
that `R5-B-R1` did not use: the test harness's `String` overload and the
scripting language API.

## 6. Classification

```text
PRE-EXISTING                 = YES (ff560991 identical; upstream code)
INTRODUCED BY A PHASE        = NO (not E2, not E3, not E3-R1)
LEGACY-DOCUMENT SPECIFIC     = NO (the load is one trigger among others)
TEST-HARNESS ARTIFACT        = YES for the recorded observation (String overload)
VISIBLE TO NORMAL USERS      = NOT through the product language routes;
                               YES through the scripting language API (GgbAPI / JavaScript)
DATA LOSS OR CORRUPTION      = NONE observed (the input is refused with an error;
                               nothing is created wrongly; English names still work)
OWNING LAYER                 = upstream localization / command-dictionary lifecycle
                               (App.getReverseCommand, AppD.setLanguage(String))
```

Not established by this activity: whether a GeoCeDG user can reach the scripting
API from an ordinary document. The probe called the desktop JavaScript engine
directly; the document-script execution policy was not characterized.

## 7. Corrective design (proposal only; not authorized)

A correction is **not required** for `E3`, for legacy documents or for the
product language routes. The remaining exposure is the scripting language API.
The options for the author are:

| Option | Shape | Assessment |
|---|---|---|
| **D0 — no product change (recommended)** | Record the limitation. As a test convention, Desktop tests switch language with `setLanguage(Locale)`, the product route, not with the `String` overload or `setLocale` | no risk; matches the `R5-B-R1` placement decision |
| D1 — scripting entry | `AppD.setLanguage(String)` delegates to `setLanguage(Locale)` | brings preferences loading and the scripting API onto the coherent host route. Side effects: `setMoveMode`, `updateConstructionLanguage`, `setUnsaved` and `setLabels` now run also for a startup preference and a script call. This changes upstream API semantics and needs separate characterization of startup and script effects |
| D2 — shared lifecycle | `App.getReverseCommand` / `initTranslatedCommands` also refills when `isCommandChanged()` | smallest code change, but it is the shared command-lookup lifecycle that `R5-B-R1` kept unchanged and that this activity must not touch. It affects every lookup path including file loading |

No correction prompt is prepared. If the author prefers D1 or D2, a canonical
prompt can be prepared as a separate activity.

## 8. Changed paths

```text
A  docs/validation/post_e3_es_command_lookup_characterization_report.md
A  geocedg/validation/post-e3/post-e3-es-command-lookup-characterization.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md (this activity's status line only)
```

The roadmap is not changed by this independent candidate, so the three POST-E3
candidates stay mergeable; its status is recorded at the author's closeout.

No product, test, build, registry, inventory, localization or serialization path
changed. Scratch probes, their outputs and the baseline worktree stay in the
session scratchpad. The JSON mirror records their SHA-256.

## 9. Verification

`STATIC` on the committed candidate (`DOCUMENTATION_STATUS_ONLY`). It is
reported outside this file, which cannot name its own commit.

## 10. Final state

```text
POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION = CHARACTERIZATION COMPLETE — PENDING AUTHOR REVIEW
OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP         = CHARACTERIZED: pre-existing upstream lazy-refill
                                             lifecycle; harness route artifact; not legacy-load
                                             specific; user-visible only via the scripting
                                             language API
CORRECTION                                 = NOT REQUIRED (D0 recommended); NOT AUTHORIZED
PUBLICATION                                = NOT AUTHORIZED
selfApproved = false
```
