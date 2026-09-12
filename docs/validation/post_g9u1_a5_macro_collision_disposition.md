# POST-G9U1-A5 installed/document-local macro collision disposition

- Status: **PASS — AUTHOR APPROVED**
- Base commit: `a53c1c9923273cd2025338bd35784438d5fb6d75`
- Base tree: `7f17eb4934d5626c3f0bba94e50d201a80fa00f6`
- Initial implementation candidate: `8b4464feac5759fc6ca086adf79aadad0d968ef7`
- Author-approved corrected implementation: `a3c8831b0569f72d1ccaf1d2eaf18e9417975377`
- Author-approved corrected tree: `f6828ce18726710e8c2b6823d6fd638dc54106ba`
- Product implementation: **bounded collision correction**
- Self approval: **false**

## Disposition

```text
A5_DISPOSITION = CLOSE_AS_ABSORBED
A5_PRODUCT_CHANGE_REQUIRED = false
A5_TECHNICAL_DEBT_CLOSED = true
A3_FRONTEND_LIVING_STATUS_RECONCILED = true
```

Review of the initial disposition exposed one precision defect: fail-closed
prevented activation of a different installed definition, but the binding
authority remained implicit in the host's name map and installation itself could
be rejected because of the active document. The initial implementation candidate
corrected that semantic rule. Its final audit then found two lifecycle defects:
Tool Manager reconstruction discarded explicit document authority, and a failed
`.ggb` load could publish partial replacement state. The corrected implementation
resolves both defects without erasing their historical finding, and the author
explicitly approved that exact corrected commit.

## Normative collision rule

When an embedded document macro and an installed persistent tool expose the same
command name but represent distinct definitions, the active document macro is
authoritative for that document. The persistent tool is disabled in that
conflicting document context and must not substitute for the embedded macro.
Command invocation and any toolbar presentation of the document macro resolve to
the embedded macro binding. The conflict does not uninstall or globally redefine
the persistent tool.

The versioned definition digest establishes whether definitions are equal or
different. The explicit `MacroManager` command-authority binding retains the
chosen current execution handle against later same-name registration; the handle
is reconstructed from the embedded definition on reopen and is not durable
identity by Java-reference equality. Native macro-mode indices remain transient
presentation addresses only.

## Candidate validation matrix

| Contract | Evidence |
|---|---|
| explicit command authority survives a later same-name registration | `PostG9U1A5MacroCommandAuthorityTest` |
| distinct installed definition is contextually unavailable while the embedded macro remains command authority | `G9U1UserToolLibraryTest.mismatchedOrPartialEmbeddedPackageFailsClosedWithoutRenameOrReplacement` |
| command and native macro-mode invocation execute the same embedded definition | the same Desktop lifecycle test, asserted through `AlgoMacro.getMacro()` |
| existing construction, save/reopen and reconstructed toolbar execution remain bound to the embedded definition | the same Desktop lifecycle test plus `G9U1MacroNativeArchivePersistenceTest` |
| installed state survives the conflict and is usable in a clean application context | the same Desktop lifecycle test |
| no-collision, exact-equivalence and partial-package behavior remain unchanged | complete `G9U1UserToolLibraryTest` and native-archive persistence selection |
| Tool Manager rename/reorder preserves authority only for the same surviving macro and clears deleted/reused-name authority | `PostG9U1A5MacroCommandAuthorityTest.toolManagerRenameAndReorderPreserveExplicitDocumentAuthority` |
| failed `.ggb` compatibility load cannot replace the prior document or publish partial macro authority | `G9U1UserToolLibraryTest.failedGgbConstructionLoadDoesNotPublishPartialDocumentMacroAuthority` |

The bounded canonical acceptance selector is `PHASE -Phase POST-G9U1-A5`.
It contains two shared-kernel authority tests and the three directly affected
Desktop lifecycle classes; registration is additive and does not change profile
or evidence semantics.

## Final corrected-candidate evidence

```text
INFRA_UNIT_RUN = verification-f5f4730dfa4f4819b95ea874fc6038da
INFRA_UNIT_PLAN = aafb62532459cf22662a072babf01054a1b8874b50600526f401529fb6ed2ab6
INFRA_UNIT_RESULT = c8a1dcddf805227e08e96d64b1c8ae06481810a79ca5f7b42358610f78c91dd1
INFRA_UNIT_ACCEPTANCE = ACCEPTED / COMPLETE
INFRA_UNIT_DIAGNOSTICS = 0

PHASE_RUN = verification-45904ab9782247b19f22c90b7d23b34c
PHASE_PLAN = 87aa81894d7a72e0697b322f747e447a43932a617821656d7ea9bb8891af124a
PHASE_RESULT = beb54ccd2fac9234f45453d07f327da68e135f2ca29cf6911d38b28b11703aec
PHASE_ACCEPTANCE = ACCEPTED / COMPLETE
PHASE_TESTS = 58
PHASE_FAILURES = 0
PHASE_ERRORS = 0
PHASE_SKIPPED = 0
PHASE_DIAGNOSTICS = 0
```

Focused shared A5 tests, the corrected failed-`.ggb` lifecycle regression,
Desktop A5 plus G9U1 persistence/lifecycle regressions, shared/Desktop
Checkstyle and `git diff --check` also passed on the corrected candidate. This
evidence remains attributed to that implementation commit; the documentary
author-approval closeout does not reinterpret it as execution on a later commit.

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
| Partial/unequal fail closed | A different installed definition remains stored but is unavailable in the conflicting context; activation cannot replace the explicit embedded binding. Partial packages remain unavailable as one atomic package | Established by the A5 candidate |
| Portable reopen without package | `ellipseAxisInvocationSurvivesNativeSaveUndoAndTwoReopens` removes the installed package, reopens the native document twice and retains a defined `AlgoMacro` result and its independent CeDG identity graph | Established |
| Undo/redo and ordinary reconstruction | The same native-archive test covers save after invocation, undo, save/reopen after undo, redo and dynamic recomputation | Established |
| No inferred equivalence authority | Code compares original-byte and parsed-definition digests; no label, coordinate, proximity, construction-index, XML-position or visual-equality fallback participates | Established |
| Tool Manager lifecycle | Reorder captures authority by transaction-local object identity and restores it only for the same surviving macro; ordinary removal still clears authority | Established by the corrected A5 candidate |
| Failed compatibility load | GeoCeDG opts `.ggb` into the existing preflight/snapshot/rollback document transaction; Classic keeps its inherited load path | Established by the corrected A5 candidate |

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
EXISTING_PRODUCT_DEFECT_FOUND = true
EXISTING_PRODUCT_DEFECTS_RESOLVED = true
PRODUCT_CODE_CHANGED = true
GUIDE_IMPACT = YES
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

The Round-3 digest and embedded-definition contract remain authoritative. The
author-approved corrected implementation makes command binding explicit and
closes A5, including the two final-audit lifecycle corrections. Neither optional
residual idea is a dependency of A7, G9B, G9C, global G9 closeout or G9U2. This
closeout creates no ADR and authorizes none of those phases.
