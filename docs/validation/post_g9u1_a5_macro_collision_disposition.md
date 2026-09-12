# POST-G9U1-A5 installed/document-local macro collision disposition

- Status: **RESEARCH/DISPOSITION CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `a53c1c9923273cd2025338bd35784438d5fb6d75`
- Base tree: `7f17eb4934d5626c3f0bba94e50d201a80fa00f6`
- Product implementation: **not authorized / not changed**
- Self approval: **false**

## Disposition

```text
A5_DISPOSITION = CLOSE_AS_ABSORBED
A5_PRODUCT_CHANGE_REQUIRED = false
A5_TECHNICAL_DEBT_CLOSED = true
A3_FRONTEND_LIVING_STATUS_RECONCILED = true
```

This is a disposition candidate, not author approval. It closes the original A5
collision because the current G9U1 Round-3 contract already makes the embedded
document macro the reconstruction authority and treats the installed package as
application/presentation state. It does not implement or authorize another macro
capability.

## Current evidence

The implementation entered at Round-3 commit
`56cf32c922baefeb30c7dff02dbdd5091107ea1a` and is part of the closed,
author-approved G9U1 authority.

| Required fact | Current evidence | Finding |
|---|---|---|
| Embedded definition remains reconstruction authority | `GeoCeDGUserToolLibrary.registeredCount(...)` adopts the live embedded `Macro` object only into the transient installed-package presentation map; `remove(...)` explicitly leaves document definitions and `AlgoMacro` outputs intact | Established |
| Installed state is application preference | `GeoCeDGUserToolLibrary` stores packages separately; `AppGeoCeDG.getMacroFileAsByteArray()` emits an empty startup-preference macro snapshot, while explicit Tool Manager dependency loading remains document-local | Established |
| Raw package identity | `Package.id` is SHA-256 over the original `.ggt` bytes; the persisted `sha256` is checked against those bytes on every load | Established |
| Exact parsed-definition equivalence | Store version 3 records digest version 1 for every host-parsed macro definition | Established |
| Bounded normalization | `definitionDigest(...)` replaces only the `showInToolBar` presentation attribute; command, metadata, inputs, outputs and construction XML remain definition-bearing | Established |
| Complete equivalent adoption | `registeredCount(..., true)` adopts only when every command is present and digest-equal; `equivalentEmbeddedMacroIsAdoptedWithoutDuplicateOrReplacement` proves the same object is retained with no renamed duplicate | Established |
| Partial/unequal fail closed | `mismatchedOrPartialEmbeddedPackageFailsClosedWithoutRenameOrReplacement` proves rejection, unchanged document XML/preferences and no host-renamed replacement | Established |
| Portable reopen without package | `ellipseAxisInvocationSurvivesNativeSaveUndoAndTwoReopens` removes the installed package, reopens the native document twice and retains a defined `AlgoMacro` result and its independent CeDG identity graph | Established |
| Undo/redo and ordinary reconstruction | The same native-archive test covers save after invocation, undo, save/reopen after undo, redo and dynamic recomputation | Established |
| No inferred equivalence authority | Code compares original-byte and parsed-definition digests; no label, coordinate, proximity, construction-index, XML-position or visual-equality fallback participates | Established |

The authoritative implementation and focused evidence are:

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserToolLibrary.java`;
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`;
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1UserToolLibraryTest.java`;
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1MacroNativeArchivePersistenceTest.java`;
- `geocedg/specs/ui/cedg-workspaces.md`;
- `docs/validation/g9u1_author_review_round3.md`.

```text
ORIGINAL_COLLISION_RESOLVED = true
PORTABLE_REOPEN_AUTHORITY = EMBEDDED_DOCUMENT_MACRO
INSTALLED_PACKAGE_SEMANTIC_AUTHORITY = false
UNEQUAL_DEFINITION_POLICY = FAIL_CLOSED
EXISTING_PRODUCT_DEFECT_FOUND = false
PRODUCT_CODE_CHANGED = false
GUIDE_IMPACT = NO
ADR_REQUIRED = false
```

## Residual ideas

### Expand/detach

No current product, correctness, persistence or roadmap requirement needs an
operation that expands an `AlgoMacro` result into ordinary construction steps.
Such an operation would be a new user capability requiring a future explicit
contract for losslessness, DAG replacement, identity/lineage, persistence and
one-step undo. It is not repair work for the installed/document-local collision.

```text
EXPAND_DETACH = OPTIONAL_FUTURE_CAPABILITY_NOT_REQUIRED_FOR_A5
```

### Broader cross-version equivalence

No current phase requires equivalence beyond the exact versioned parsed-
definition digest. A stronger matcher would need a separate normative proof
contract and must retain `EQUIVALENCE_NOT_ESTABLISHED` when exact evidence is
absent. Names, outputs, coordinates, similar XML or visual similarity cannot
authorize it.

```text
CROSS_VERSION_EQUIVALENCE = OPTIONAL_FUTURE_RESEARCH_NOT_REQUIRED_FOR_A5
```

```text
EXPAND_DETACH_CURRENTLY_REQUIRED = false
CROSS_VERSION_EQUIVALENCE_CURRENTLY_REQUIRED = false
```

## Roadmap consequence

The original A5 defect is absorbed by the accepted G9U1 Round-3 implementation.
Neither optional residual idea is a dependency of A7, G9B, G9C, global G9
closeout or G9U2 in current authority. This candidate changes documentation
only, creates no ADR, and claims no product or verification-phase effect.
