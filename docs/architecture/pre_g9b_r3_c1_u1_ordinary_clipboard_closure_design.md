# PRE-G9B-R3-C1-U1 ordinary clipboard constructive closure

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PHASE                     = PRE-G9B-R3-C1-U1
KIND                      = CORRECTNESS REPAIR of inherited host clipboard behaviour; no new semantics
```

This record documents the repair of `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION`,
registered by the [`PRE-G9B-R3-C1` candidate](pre_g9b_r3_c1_clipboard_atomicity_and_closure_design.md)
and scheduled by the author before `PRE-G9B-R3-X1`. It changes no serialization,
identity format, schema version or migration and gives no ordinary geo a durable
CeDG identity. Characterization, tests and evidence are in the
[candidate report](../validation/pre_g9b_r3_c1_u1_candidate_report.md) and the
[validation matrix](../validation/pre_g9b_r3_c1_u1_validation_matrix.md).

## 1. Two layers that stay separate

| Layer | Question it answers | Owner in the code |
|---|---|---|
| **Ordinary constructive closure** | does the payload hold enough host construction to rebuild every copied dependent geo? | `CopyPaste.addPredecessorGeos`, `InternalClipboard.addAlgosDependentFromInside`, and now `InternalClipboard.addParentAlgorithmsOfCopiedGeos` |
| **CeDG semantic identity closure** | does the payload hold the durable IDs, participants, lineage and spatial records of the copied semantic component? | `InternalClipboard.addSpatialIdentityClosure`, `SpatialIdentityRegistry` |

The first layer is generic host behaviour and runs for every payload. The second
runs only for participating geos. The repair improves the first layer, uses nothing
from the second, and the second keeps working unchanged on top of it.

## 2. Root cause

Both functions below are byte-identical to the pinned upstream baseline
`9b93256b7df401ff056c37b502d82df4d72b1522`, and the probes reproduce the defect on
that baseline. The defect is inherited upstream behaviour (`UPSTREAM_REPRODUCES = true`),
not introduced by GeoCeDG.

1. `CopyPaste.addPredecessorGeos` adds every predecessor geo except constant
   elements, and adds only the parent algorithm of the *selected* geos. Its own
   comment says that axes are "dealt with later - we suppose they are always on".
2. Every other algorithm is added by `InternalClipboard.addAlgosDependentFromInside`,
   which adds an algorithm only when `consElements.containsAll(inputs)`. A
   construction constant such as `xAxis` is never in the list, so the algorithm of
   any unselected predecessor with a constant input is never added.
3. The XML is written from the construction steps, so the predecessor
   (`f = Line(C, xAxis)`) is renamed for the clipboard but never serialized.
4. On paste, the command that uses it (`Intersect(f, yAxis)`) fails to resolve its
   input. The host XML handler only records an error, and the next `<element>` of the
   same label creates the geo as **free** geometry, with the paste reporting success.

The trigger is a construction constant (`xAxis`, `yAxis` and, in 3D, the companion
constants) as an input of an *unselected predecessor's* command. A selected geo's own
algorithm is unaffected, because `addPredecessorGeos` adds it directly.

## 3. Repair

`InternalClipboard.addParentAlgorithmsOfCopiedGeos(consElements, copiedMacros)`
runs after the existing collection steps in both copy pipelines
(`InternalClipboard.copyToXMLInternal`, used by shared and Web, and
`CopyPasteD.copyToXML`, used by Desktop). For every copied labeled geo whose parent
algorithm is missing, it adds that algorithm and its missing outputs (hidden, like
the side-effect outputs upstream adds), if and only if:

- every input of the algorithm is already copied **or is a construction constant**; and
- the algorithm is not an `AlgoTableToChart`, which upstream excludes.

It has three properties that make it safe to run for every payload:

- **No over-expansion.** It adds only parents of geos that are already copied and
  never the dependents of the copied set. Enabling upstream `addAlgosDependentFromInside`
  to treat constants as satisfied would instead copy every hidden axis-based
  dependent of a copied point, which is why that alternative is rejected.
- **No dangling references.** An algorithm with an input that is neither copied nor a
  constant is not added, so the payload never names an object it does not contain.
- **Idempotent and order-independent.** It only fills gaps: when upstream already
  added an algorithm, nothing changes, so constructions without constant inputs
  produce the same payload as before.

Constants are never copied and gain no identity. They are referenced by name and
resolve in the destination construction, as upstream intends.

The helper began as the C1 identity-bearing completion (`addSpatialClosureParentAlgorithms`,
called only when the closure had identity records). The C1 debt analysis
showed the same omission in ordinary documents, so the helper is now generic and
its call is unconditional. Identity-bearing payloads receive exactly the same
completion as before, so C1 behaviour (complete closure, fresh IDs, lineage,
`INCOMPLETE_CLOSURE`, rollback) is unchanged and re-verified.

## 4. Host input classification

| Class | Inputs | Treatment |
|---|---|---|
| A. ordinary copied inputs | predecessors reachable through `getAllPredecessors`, transitive | copied as normal construction elements, as before |
| B. global host references | `Construction.isConstantElement`: `xAxis`, `yAxis` and the companion constants | referenced by name, never copied, no identity |
| C. non-reconstructible | any other input missing from the copied set | the algorithm is **not** added, so the payload never references an absent object |

No product copy path reaches class C, because predecessor closure is transitive. The
guard is defensive and is proven by a unit test of the helper on a hand-built list;
in that situation the behaviour equals the previous one, and no new failure mode or
user-visible message is introduced. Same-window fast paste
(`removeFreeNonselectedGeoNumerics`) removes free non-selected numerics **after**
the completion, so its intentional external references to same-window numerics are
unaffected.

## 5. Consumers

| Consumer | Effect |
|---|---|
| shared / headless | `copyToXMLInternal` completes the payload; covered by shared tests |
| Desktop | `CopyPasteD.copyToXML` completes both the ordinary payload and the same-window fast payload; covered by real Ctrl+C / Ctrl+V tests for both routes |
| Web | `CopyPasteW.copyToXML` calls `InternalClipboard.getTextToSave`, the same shared method; the GWT module is not built by the unit-test suite, so Web is covered by the shared path only |
| macros | `Macro.buildMacroXML` does not call the helper and is unchanged; `copiedMacros` is passed through as for the upstream method |
| groups, duplicate | unchanged; duplicate uses the same `copyToXMLInternal` |
| Insert File | copies whole constructions, where every geo is selected and already brings its parent; unchanged |

## 6. Compatibility

```text
SERIALIZATION_FORMAT_IMPACT = NONE
IDENTITY_SCHEMA_IMPACT      = NONE
MIGRATION_IMPACT            = NONE
```

A payload gains only the host commands that its renamed labels already referenced.
That is constructive correctness, not a new persistent schema. Documents without
constant-input predecessors produce the same payload as before. Historical documents
open unchanged.

## 7. Not changed

- The failure policy of ordinary XML paste: a failed command in a non-identity
  payload still only reports through the XML handler. The repair removes the cause
  for the known case and adds no new failure mode.
- `CommandNotLoadedError` behaviour on Web and `MyXMLio.doParseXML` structure remain
  as recorded by the C1 observations.
- The upstream functions `addPredecessorGeos` and `addAlgosDependentFromInside` are
  not modified.
