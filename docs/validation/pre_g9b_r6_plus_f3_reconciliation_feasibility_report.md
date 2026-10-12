# PRE-G9B-R6-plus-F3 — conservative reconciliation: focused feasibility report

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (author instruction of
  2026-10-12, "Conservative and Verifiable File Insertion Semantics";
  AUTHOR AUTHORIZED — CONSERVATIVE SCOPE REASSESSMENT, FOCUSED
  CHARACTERIZATION, DESIGN RECONCILIATION AND AUTHOR-DECISION PREPARATION ONLY)
- Status: **FOCUSED CHARACTERIZATION COMPLETE — DOCUMENTARY CANDIDATE —
  PENDING AUTHOR REVIEW**
- Base: F3 preparation candidate `480c972972e90514e88421194a792898c916834e`,
  tree `166caf6c7802c37db03060a2fd07cd71d28daa17` (STATIC
  `verification-e7bb62e990814e4791e7f1aabbdb9581` `ACCEPTED / COMPLETE`), child
  of the published `e70718fec3612814144cd405d46f6c08864ceadd`
- Historical evidence (unchanged): the
  [F3 preparation characterization report](pre_g9b_r6_plus_f3_preparation_characterization_report.md)
  (`K0`–`K15`, `CF-F3-1` to `CF-F3-12`) and its eleven probe results
- Companions: [conservative-scope assessment](pre_g9b_r6_plus_f3_conservative_scope_assessment.md);
  [conservative design candidate](../architecture/pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md);
  [author-decision reconciliation record](pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md);
  JSON mirror [`pre-g9b-r6-plus-f3-author-reconciliation.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-author-reconciliation.json)
- `selfApproved = false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`

This report answers the ten priority questions of the instruction (§16) with
the minimum additional probing. It decides nothing.

Outcome classes: **CONFIRMED**, **CONDITIONALLY FEASIBLE**, **NOT
ESTABLISHED**, **BLOCKED**. Evidence classes: **S** source at the base,
**P** new scratch probe (`RP-*`), **H** historical F3-PREP probe, **I**
inferred. Path abbreviations follow the preparation report.

## F0 — Entry gate (2026-10-12)

| Check | Result |
|---|---|
| live remote `main`, local `main`, `origin/main` | `e70718fec3612814144cd405d46f6c08864ceadd` |
| preparation candidate | `480c972972e90514e88421194a792898c916834e`, tree `166caf6c7802c37db03060a2fd07cd71d28daa17`, single child of `e70718fe` |
| preparation receipt | `verification-e7bb62e990814e4791e7f1aabbdb9581`, `ACCEPTED / COMPLETE`, bound to that commit and tree |
| worktree | clean |
| `AGENTS.md`, `CLAUDE.md` | unchanged since the base |
| F3 planning | `F3-D1` to `F3-D6` unchanged; `CF-F3-1` to `CF-F3-12` present in the preparation report; canonical prompt `PREPARED — NOT AUTHORIZED` |
| branch | `phase/pre-g9b-r6-plus-f3-reconciliation` (local), from `480c9729`; the preparation commit is preserved |

## F1 — The scratch prototype route (`RP-*`)

One scratch JUnit class (`F3ReconProbeTest`, six methods) exercised a
**scratch-only prototype** of a conservative insertion that uses only existing
product and kernel services and never calls `AppD.insertFile`:

1. extension and existence check;
2. `DocumentArchivePreflight.validate` with the product parser config
   (`desktop/io/DocumentArchivePreflight.java:36`; AGC `:1760-1766`), any
   exception counted as rejection;
3. a hidden helper from the GeoCeDG `newAppForTemplateOrInsertFile()` (AGC
   `:1332-1340`), loaded through its transactional `loadFile`;
4. an admissibility decision on the helper **before any target access**
   (empty construction, label conflicts, macro presence and macro-name
   collisions, scripts);
5. insertion with `CopyPasteD.insertFrom(helper, target, duplicates, false)`
   (CPD `:644-684`, public) on a **dedicated** `GeoCeDGCopyPaste` instance, not
   the window's buffer;
6. post-insertion verification: created objects = construction minus the
   identity set taken before; count equal to the source; pairwise class,
   parent-algorithm class, value and layer; every parent-algorithm input of a
   created object is itself created ("closure").

The prototype is evidence of feasibility, not a design: it shows which existing
services suffice. It was never placed in a tracked path.

## F2 — Answers to the priority questions

### FQ-1 — Is a useful collision-free, macro-free subset safely insertable? **CONFIRMED** (for the verified types of F3)

`RP-types`: a 40-object `.cedg` source without macros or scripts (numbers,
points, line, ray, segment, vector, circle, ellipse, a dependent function,
polygon with its edges, point on path, intersection, midpoint, angle, dynamic
text, lists, sequence, a 3D point, a boolean, Locus V2 with its metric and
intersection results, Spline V2 with its promoted list and degree, a native
linear dimension with its outputs; ten spatial-identity records) inserted into
an empty `mm` document: 40 of 40 created; classes, parent algorithms and layers
pairwise identical; **zero external inputs**; values identical except the Locus
V2 family's session `revision=` counter (not semantic); target identity records
10. Saved and reopened: 40 objects, the same values (again only the revision
counter), 10 identity records (P).

### FQ-2 — Can the supported subset be certified before mutation? **CONFIRMED** (admission) / **CONDITIONALLY FEASIBLE** (full certification)

Everything the admission needs is read from the helper before the target is
touched: object classes and parent algorithms, labels, macros
(`getMacroNumber`, `AlgoMacro` parents), scripts, identity participation. Every
rejection in `RP-labels`, `RP-macros` and `RP-failures` left `target.getXML()`
byte-identical and the macro count unchanged (P). A **dry run** on a scratch
document loaded from the target's XML produced created objects whose
definitions, values and layers equal those of the real insertion
(`RP-dryrun`, `dryEqualsRealXml = true`) (P) — certification of the commit
before mutation is feasible, but determinism was shown on one small case only.

### FQ-3 — Can insertion be made atomic and independently undoable? **CONDITIONALLY FEASIBLE**

- Validation before mutation: CONFIRMED (FQ-2).
- Commit: `insertFrom` → `pasteFromXML(target, true)` →
  `InternalClipboard.evalClipboardXMLAtomically` (`common/util/InternalClipboard.java:620-657`):
  an exception restores the pre-paste snapshot through the established
  `ROLLBACK_RESTORE` clearing parse (`:663-685`) (S). **But** outside the
  identity-bearing branch the XML handler's element errors do not throw
  (`hasErrors()` is checked only for identity-bearing payloads, `:641-647`), so
  an element the target cannot rebuild could be skipped silently (S). The
  post-insertion verification (count, pairwise class, closure) detects such a
  skip (P: it reports `INCOMPLETE`).
- Failure after commit: rollback to the state before the insertion with
  `UndoManager.restoreCurrentUndoInfo()` (`common/main/undo/UndoManager.java:225`),
  a clearing restore of the last stored undo state (S). This is the ordinary
  undo restore, whose unit, hidden-layer and spatial-identity behavior is
  established by `D1`/`A-2`/G9A (`unit-system.md` §9 undo row), not a reopening
  of the file. It replaces Java instances as every undo does; serialized
  identities (labels, spatial IDs) are restored. A forced mid-commit failure was
  **not** probed (NOT ESTABLISHED for the rollback branch itself).
- Undo: one `storeUndoInfo()` after a verified insertion; Undo restored
  `target.getXML()` **byte-identical** to the state before the insertion;
  Redo restored the inserted objects (`RP-undo`) (P). The document is unsaved
  after the insertion.

### FQ-4 — Can macro-dependent insertion be rejected before source macros modify the target? **CONFIRMED**

`RP-macros`: M0 (reject any source macro) and M1 (reject a source macro whose
command name exists in the target) both rejected before mutation (target XML
and macro count unchanged). M1 on a collision-free target imported the used
macro **once** (`MyMid`) and the inserted `N = MyMid(B, A)` kept the source
value `(5.5, 6)` with its `AlgoMacro`; the source's **unused** macro `Unused`
was **not** imported (`insertFrom` transfers only macros used by copied
objects, CPD `:513-532`) (P). The duplicate import of `CF-F3-4` disappears
because the route never calls `AppD.insertFile`. Macro **definition**
comparison (identical tools in both documents) was not probed (NOT
ESTABLISHED).

### FQ-5 — Can label conflicts be rejected or safely renamed? **CONFIRMED**

- L0: conflicts `[a, A, B]` rejected before mutation, target unchanged (P).
- L1: the same source inserted into a target holding `A`, `B`, `a` and
  `tc = Circle(A, B)`: 40 of 40 created, **zero external inputs**; the target's
  `A` keeps its instance and `tc` still depends on it; the inserted objects were
  renamed `A_{1}`, `B_{1}`, `a_{2}` and every inserted dependent refers to the
  renamed inserted inputs (`f1 = Line(A_{1}, B_{1})`, the dimension
  `LinearDimension(B_{1}, C, …)`); a GGBScript click script was rewritten
  (`SetValue(a_{2}, a_{2}+1)`) and a show-object condition kept pointing at the
  inserted boolean (P). Mechanism (S): `copyToXML` writes every copied object
  under a `CLIPBOARDmagicSTRING` prefix, so all references resolve inside the
  pasted set, and `handleLabels` renames afterwards (`common/util/CopyPaste.java:419-490`).
  Label text inside static strings or JavaScript is not rewritten (S/I).
- L2 (Overwrite) is not needed by any recommended alternative; its deletion of
  dependents stays `CF-F3-5` (H).

### FQ-6 — Can clipboard state be isolated from insertion? **CONFIRMED**

With a dedicated `GeoCeDGCopyPaste` instance: the window buffer's provenance
was unchanged (`mm` before and after), the `D1` notice was still shown for an
`m` source into an `mm` target, and a later Ctrl+V pasted the user's previous
buffer correctly as `R_{1}` (`RP-undo`) (P). The failure paths of `CF-F3-1`
cannot occur: a missing, unreadable or empty source is rejected before any
paste (`RP-failures`) (P). This **changes** the accepted `DQ-D1-7` behavior
("Insert File … replaces the internal copy buffer"; a later Ctrl+V keeps its
provenance), so it needs an explicit author amendment (new `DQ-F3-16`).

### FQ-7 — Can the Ctrl+V placeholder-label problem be fixed narrowly? **CONFIRMED**

Two routes, both shown:

- GeoCeDG-owned: the dedicated insertion buffer never sets the window
  instance's private `duplicateLabels`, so Ctrl+V labels correctly (FQ-6) (P).
- Upstream: resetting `CopyPasteD.duplicateLabels` to `null` at the end of
  `insertFrom` (simulated by reflection in `RP-narrowfix`) turns the inherited
  `CLIPBOARDmagicSTRINGA`, `…B` into `A_{1}`, `B_{1}` (P). It is a one-line
  inherited Desktop change that also corrects Classic.

### FQ-8 — Which template properties can be applied without altering layers, geometry or units? **CONFIRMED** (for the measured properties)

`RP-template` measured, on objects with layers, a hidden layer, a hidden object,
a caption, the fixed flag, a label offset, a show condition, a colour function
and a native dimension:

| Property | Inherited `TemplateHelper` | Whitelist prototype (T0) |
|---|---|---|
| colour, point size | from the defaults | from the defaults |
| line thickness, line style, point style, fill opacity | from the defaults | from the defaults |
| layer | **moved to the working layer** | unchanged |
| caption, label offset, fixed flag | **reset** | unchanged |
| show-object condition | **cleared** | unchanged |
| colour function | **cleared** | unchanged (the function still drives the displayed colour) |
| Euclidian visibility | unchanged | unchanged |
| label visibility | kept by `TemplateHelper` | unchanged |
| native dimension outputs | **restyled** (line thickness 2 → 5) | **restyled** (2 → 5) unless excluded |
| geometry, labels, units, hidden set, working layer | unchanged | unchanged |
| construction defaults | replaced | unchanged (T0) / replaced (T1) |
| labeling style | replaced (`.cedg` template) | unchanged |

(P). The T0 prototype restyles native-dimension outputs through the line
default, so GeoCeDG-specialized objects must be excluded or given an explicit
rule.

### FQ-9 — Can Apply Template obtain correct undo and saved-state behavior? **CONFIRMED** for object styles; **BLOCKED** for construction defaults and labeling style under the existing undo

With one `storeUndoInfo()` after the change: the document becomes unsaved; Undo
of T0 restored `target.getXML()` byte-identical (P). For T1 the XML was also
identical after Undo, **yet a point created afterwards still had the template's
default colour**: construction defaults are written to the archive's
`geogebra_defaults2d.xml` (`common-jre/io/MyXMLioJre.java:162`, `:379`) but are
not part of the undo XML (P/S). After the inherited `TemplateHelper` and Undo,
`getXML()` differed from the original (the labeling style is not restored),
and the defaults stayed changed (P). Changing defaults or the labeling style
therefore cannot be one coherent undoable operation without a new undo
mechanism. A failed template load with the inherited helper **applied factory
defaults** to every object (re-layered, captions and conditions cleared,
colours reset) and raised two "GeoGebra - Error" EDT dialogs (`CF-F3-12`
confirmed) (P).

### FQ-10 — Are inherited Desktop changes truly necessary? **CONFIRMED: not necessary** for the recommended contracts

The prototype used only public or package-accessible members of existing
classes (`DocumentArchivePreflight.validate`, the GeoCeDG helper factory,
`AppD.loadFile`, `CopyPasteD.insertFrom`, `GeoCeDGCopyPaste`, `storeUndoInfo`,
`restoreCurrentUndoInfo`, ordinary style setters). The remaining product-owned
needs are the helper's recent-file publication (`RP-undo`: the helper load added
one recent entry; a GeoCeDG helper override of the public `AppD.setCurrentFile`,
with the protected `currentFile`, can suppress it — S) and the helper's
global registrations (H, `K9`). An inherited change is only an option for
correcting Classic (FQ-7 upstream route).

## F3 — Additional findings

| Id | Finding | Evidence |
|---|---|---|
| `RF-1` | Runtime feature flags do not gate paste: Locus V2 and Spline V2 objects were inserted into a target started with Locus V2 creation disabled | P (`RP-types`, disabled target) |
| `RF-2` | a native-dimension text is presentation in the **target's** units: `text1` read `1.79 mm` in an `mm` target and `1.79` in a unit-less target; numbers unchanged (`unit-system.md` §14) | P |
| `RF-3` | `AlignedDimension(A, B, 1)` did not evaluate in the probe host (`LinearDimension` did); its insertion is NOT ESTABLISHED | P |
| `RF-4` | `insertFrom` marks every source label as protected in the target construction (`CPD:659-662`, `Construction.java:205`, `:4409-4410`) until the next clearing; automatic labels avoid them; not serialized | S/H |
| `RF-5` | after an Undo reload a native-dimension text reported line thickness 5 instead of 0 although `getXML()` was identical; a reload normalization, not a template effect | P |
| `RF-6` | identity-bearing payloads with copied macros are refused by `CopyPasteD.preflightClipboardXML` (`:588-602`) — M1 cannot apply to them | S |

## F4 — Open technical items

- `OTI-F3R-1` — rollback by `restoreCurrentUndoInfo()` after a forced
  mid-commit failure (not probed); and the precondition that the target's
  current state equals its last stored undo state.
- `OTI-F3R-2` — dry-run determinism beyond the small case; cost on large
  documents.
- `OTI-F3R-3` — macro definition comparison (identical tools) if M1+ is wanted.
- `OTI-F3R-4` — types not yet in the matrix (`AlignedDimension`, `IsoABorder`,
  3D lines/planes/solids, images, buttons and input boxes, CAS cells,
  JavaScript, objects bound to external files).
- `OTI-F3R-5` — Apply Template on Locus V2, Spline V2 and `IsoABorder`
  outputs.
- `OTI-F3-3` (from F3-PREP) — release of the helper's registrations.

## F5 — Probe record

Route: scratch JUnit class `org.geocedg.desktop.F3ReconProbeTest` (six methods)
in the Desktop test source set through the F3-PREP Gradle init script;
extensions `G9U1TestApp.Lifecycle`, `EmptyUnitPreferences`, `DesktopLogger`;
isolated preference file.

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.recon-types.txt` | FQ-1, FQ-2, `RF-1`–`RF-3` | `fe2330ba3f061982e6325487d2ddf9d01b0deabb8850c995ff6b1c1157423344` |
| `f3-probe.recon-labels.txt` | FQ-5 | `02a8e48a25814a3a35e68ab9db63d3bbfdd251982a4f39a038125698c14e6db8` |
| `f3-probe.recon-macros.txt` | FQ-4 | `ed4c612599755e741ffe62b107bd1aa2c8d0b78bb14a1eeedd616b9e80eeed88` |
| `f3-probe.recon-undo.txt` | FQ-3, FQ-6, failures | `47f23677c51ab91add2b02504c31b961b3454136c9a29d7154d6ee866032b912` |
| `f3-probe.recon-narrowfix.txt` | FQ-7 | `be4fc747131811c451af0a001bc95fef514eca04918ccc9e97d7dafd6d8c43b4` |
| `f3-probe.recon-template.txt` | FQ-8, FQ-9 | `5ef72a449e47d91971e2c44f2d6e263b008917fe89c4f93b9b8c7390f01b09a2` |
| probe source | `F3ReconProbeTest.java` | `652b652fb3f07e6a8e43a7c6408a13bdee814c6c235fd5e317b0f483dd51cf31` |

Runs: two compile failures of the probe (a `GColor.getRGB` call and a character
literal), one run with a failing method (the prototype did not catch the
`IOException` that `DocumentArchivePreflight.validate` throws for a non-archive;
fixed by counting any exception as rejection), then six tests, exit 0. Afterwards
`:desktop:desktop:compileTestJava` without the init script (exit 0); the probe
test-result XML and four compile-stash copies removed; `git status` clean.
