# POST-E2-P3 — interactive document-macro access: readiness gate and design

```text
STATE              = DESIGN — bound to the P3 technical candidate (author approval PENDING)
BASE               = POST-E2-P2-R1 closeout 9bdcdf834814ce7eeb16ba189283208415fd1e7d
                     tree 06831f900e728900c5fc92b48d4f85e3c10fe759
VERIFICATION CLASS = INTEGRATED_PHASE (PHASE + INTEGRATION)
READINESS GATE     = A — existing E2 evidence sufficient, completed by two focused
                     checks (§1.2); no separate characterization phase
OBSERVATION        = OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX
selfApproved       = false
```

## 1. Readiness gate

### 1.1 Reused E2 evidence (still valid at the base)

The E2 smoke follow-up, part A ([report](../validation/pre_g9b_r6_plus_e2_smoke_followup_report.md)),
established on `Templatev7.ggb`: 24 of 24 macros read and registered, document
command authority bound, invocation by command name, the stored document toolbar,
the profile-compiled product toolbar (ADR 0012), the library-only User tools menu,
the existing management route (Document tools (local only)…) and save/reopen
persistence. The source paths it relied on are unchanged at the base:
`GeoCeDGUserTools.populate` (installed and bundled entries only),
`GeoCeDGUserToolLibrary.activate/select`, `Kernel.getMacro/getMacroID`,
`EuclidianController` with `ModeMacro`. These results are not repeated.

### 1.2 Focused checks (scratch probes, embedded product host)

1. **Activation of every Templatev7 macro.** For each of the 24 live macros,
   `app.setMode(MACRO_MODE_ID_OFFSET + kernel.getMacroID(macro))` makes the
   controller's `ModeMacro` hold exactly that `Macro` object; each is owned by the
   current kernel and holds command authority. Input types: points, lines, conics,
   lists, loci, numbers and angles — all handled by `ModeMacro` (objects by
   selection in the Graphics or Algebra view, a missing point by click, numbers
   and angles by its dialog). No input type is outside the existing contract.
2. **Kernel macro lifetime (upstream, unchanged).** File → New keeps the kernel
   macros, and saving the new document writes them (a fresh window reopens 24);
   Open replaces them with the opened document's (0 for a document without
   macros, 1 for a document with one); reset and `clearConstruction` keep them.
   The live kernel macro set is therefore exactly the set the current document
   would save.

### 1.3 Gate questions

| # | Question | Answer |
|---|---|---|
| 1 | representation of document macros | kernel `Macro` objects (`Kernel.getAllMacros`), one per command name, bound as command authority |
| 2 | registration of tool modes | none stored: the mode is `MACRO_MODE_ID_OFFSET + kernel.getMacroID(macro)`, derived from the live object |
| 3 | controller activation | `app.setMode(mode)` → `EuclidianController` → `ModeMacro` (§1.2 1), the mechanism the library already uses (`GeoCeDGUserToolLibrary.select`) |
| 4 | visibility and availability | `isShowInToolBar` concerns the legacy toolbar only; availability = the object is the live macro of its command in the current kernel |
| 5 | document vs library entries | the library records the macros it registered and adopts equivalents by definition digest; `presents(macro)` (new) answers whether an available installed or bundled entry presents a live macro |
| 6 | document changes | the menu is rebuilt on every opening from the live kernel; §1.2 2 fixes the meaning across New and Open |
| 7 | conflicts and equivalence | existing digest rule unchanged: equivalent → the installed entry is the only visible choice; same name, different definition → installed entry unavailable, document macro remains the authority |
| 8 | all 24 activate | yes (§1.2 1) |
| 9 | without toolbar reconstruction or registration change | yes: a menu entry and an existing mode switch |

## 2. Design

`GeoCeDGUserTools.populate` (GeoCeDG-owned) ends with a **Document tools**
submenu, also when the installed library cannot be read:

- entries = live kernel macros of the current document, in kernel order,
  excluding those the library presents (`GeoCeDGUserToolLibrary.presents`: registered
  by the library in this session, or named by an available installed or bundled
  entry); empty → a disabled "This document has no tools";
- label = tool name (or command name); equal labels get the command name
  appended; tooltip = command name and tool help; the entry stores the command
  name as a client property for tests and accessibility;
- action: re-check that the entry's `Macro` is still the live, document-presented
  macro of its command in the current kernel, then
  `app.setMode(MACRO_MODE_ID_OFFSET + getMacroID(macro))`; otherwise a warning
  ("This document tool is no longer available…") and no mode change.

Not changed: macro loading, registration, execution, command resolution,
serialization, the DAG, kernel algorithms, the product toolbar and its profile,
installed, bundled and pinned tools, Classic. No mode number is stored; no name or
toolbar position is used as identity.

## 3. Ownership

| File | Change |
|---|---|
| `GeoCeDGUserTools` | Document tools submenu, document-macro filter, guarded activation; the library-failure branch no longer returns before it |
| `GeoCeDGUserToolLibrary` | identity set of macros it registered (`hostRegistered`, pruned with `activated`), `isLibraryRegistered`, `presents` |
| `apps/geocedg/application-profile.yml` | four EN/ES texts |

## 4. Scope limits kept

No toolbar reconstruction or pinning of document macros, no persistence of product
tool registrations, no import into the installed library, no Classic change, no
kernel macro extension, no general macro management. File → New keeping the
macros is the unchanged GeoGebra lifecycle; the menu shows them because the new
document would save them.
