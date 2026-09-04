# Verification input identity semantics — operational repair

Status: **PASS — AUTHOR APPROVED**, under the author's explicit conditional operational authorization.
Product phase effect: `NONE`. Scientific contract changed: `false`.
`selfApproved=false`. This report does not grant any product-phase approval.

Authority: [ADR 0024](../adr/0024-verification-input-identity.md) and
[verification levels section 11](../../geocedg/specs/operations/verification-levels.md#11-verification-input-identities-and-evidence-preserving-repair).

## Entry and preserved failure

- Reviewed technical R1 commit: `a38d4fcde846fc97c51abc8d958de6998302c436`.
- Existing author-approved R1 closeout: `af459d856f1cdc384805f3035203acce8e6f6104`.
- Entry local main: the closeout; origin/live main:
  `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`.
- R1 technical/closeout branch was published; R1 PASS tag was absent.
- The preserved failed main-checkout AUTHOR_CLOSEOUT compared
  `tools/agent/verify-baseline.ps1`: 14,399 archived physical bytes versus 14,167
  checkout bytes, with unchanged logical content and Git authority. Precommit
  and technical-branch postcommit checks had passed. The failed real check is
  historical FAILED evidence, not rewritten by this repair.

## Bounded raw-identity audit

Line references identify the entry closeout commit, not future rewritten lines.

| Authority / entry lines | Classification | Disposition |
|---|---|---|
| `tools/agent/phase-lifecycle.ps1:266–302` | Archived execution integrity | Preserve raw inventory hash, original clean status/index binding, record schema and sealed summary checks. |
| `tools/agent/phase-lifecycle.ps1:303–314` | **Defective CROSS_CHECKOUT** | Replace later tracked-raw equality with Git tree/path/mode/blob authority plus independent materialization validation. Keep consumed-untracked evidence exact. |
| `tools/agent/phase-lifecycle.ps1:628–684` | CROSS_COMMIT target selection | Separate explicit closeout target from inspection HEAD; retain exact approved target content and ancestry. |
| `tools/agent/phase-lifecycle.ps1:392–550` | Archived artifact/hash linkage | Preserve bundle/root/receipt/JUnit/Checkstyle integrity and exact original technical commit. No new execution claim. |
| `tools/agent/verification-runtime.psm1:82–92,129–196,378–411` | SAME_RUN | Preserve loaded-module/raw file inventory/environment identity. |
| `tools/agent/verification-runtime.psm1:842,874,907,1001–1007,1070–1080` | SAME_RUN | Preserve start/module/seal/consumer/completion mutation rejection and process-owned receipts. |
| `tools/agent/verification-runtime.psm1:305–375` | External execution inputs | Preserve Gradle/JVM/configuration bytes, membership and unsupported-init rejection. |
| `tools/agent/evidence-integrity.ps1:118–169,241–305` | Frozen Git historical authority | Already reads Git blobs and the established canonical-LF manifest format; not the raw cross-checkout defect. No change needed. |
| `tools/agent/repository-generated-state.ps1` | SAME_RUN generated state | Snapshot/restoration and status ownership only; no archived tracked-raw identity comparator. No execution-lifecycle change. |
| `tools/agent/repository-state.ps1:48–87` | Repository metadata | Already reads exact commit/blob roadmap authority; no raw equivalence defect. |
| `tools/bootstrap/bootstrap-windows.ps1:432–484` and workstation helpers | Delegated verification/native evidence | No durable tracked-raw equality mechanism; current prerequisite and delegated-level contracts remain. |
| Tracked archived-reconciliation/promotion code search | Audit result | No separate tracked archived-reconciliation runner was found; historical artifact-only reconciliation is not a new executable authority. New exact-target closeout wrapper is the bounded promotion seam. |
| R1 verifier lifecycle initialization and top-level PHASE/COMPOSED call sites | Published provenance | The old AUTO path selected documentary AUTHOR_CLOSEOUT after a closeout record appeared, then required its documentary-only switch. Add explicit `PUBLISHED_REGRESSION` context authenticating pinned T/C without changing scientific live assertions, tasks, filters or result acceptance; no top-level orchestration change is needed. |

Historical prompt/binary fixture hash checks and generated summary hashes have
their own declared format/provenance contracts; they are not silently generalized
by this repair. Canonical-LF evidence hashes are not substituted for Git mode/
blob/path identity, and raw author/binary evidence is not normalized.

## Actual materialization assumptions

The entry audit inspected all 11,202 tracked paths with effective `git check-attr`:
four have `text=set`, 21 have `text=unset`, and three have `eol=lf`; none has an
effective `filter`, `working-tree-encoding` or `ident` assignment. Git modes are
11,171 `100644` and 31 `100755`. The host is Git `2.55.0.windows.3` with
`core.autocrlf=true`, `core.filemode=false`, `core.symlinks=false` and
`core.ignorecase=true`; no `core.eol`, custom attributes-file or info/attributes
override was found. System Git LFS filter definitions exist but are not assigned
to tracked paths. Effective use, not the mere existence of an unused definition,
determines whether unsupported transformation must be rejected.

The failed `verify-baseline.ps1` path has unspecified text/EOL/encoding/ident/
filter attributes. The repair does not add `.gitattributes` rules or alter this
file to force one physical representation.

## Preserved repair-pilot chronology

The following unsuccessful operational pilots remain historical evidence; none
is reclassified as a passing execution or as a product/scientific regression:

- `actual-closeout-lf-dev`: rejected the materialized developer guide because
  its reviewed indexed blob itself retains CRLF. The materialization predicate
  was corrected to distinguish indexed CRLF content from checkout conversion;
  the approved document bytes were not changed.
- `focused-a`: exposed a fixture environment-variable restoration problem.
- `focused-a2`: exposed Windows scratch-path length in the fixture harness;
  subsequent scratch work uses the ordinary short temporary directory.
- `focused-a3`: repository-identity fixtures passed 45/45 and equivalence
  fixtures passed 28/28, but lifecycle fixtures passed only 55/56. The inherited
  independent Git mode/blob oracle decoded a Unicode path with the child host's
  OEM-850 defaults. The new launcher was corrected to establish UTF-8 transport; the old
  independent oracle and its comparison remain unchanged. Complete final
  repetition subsequently passed as recorded below.
- `focused-a4`: all underlying suites passed (repository identity 45/45,
  equivalence 29/29, lifecycle 56/56), but the new aggregate wrapper failed to
  locate the lifecycle summary beneath its run-specific directory. This is a
  wrapper summary-location failure, not an underlying test failure. The
  lifecycle canonical hash is
  `4288b6782bacff4b1f66e3e7569da7c8c46a32f117f9eb80ea45fbedc9f238b6`.
  The bounded correction must accept only one exact supported direct or
  run-specific summary location and reject missing/ambiguous locations. That
  bounded correction and final aggregate A/B repetition subsequently passed.

An initial sandbox external-input comparison ran under a different account and
was classified as not comparable. Under the actual managed execution account,
all 16 external-input records and nine JDK files match the sealed cohort. No
workstation/environment requirement was changed to obtain that match.
The saved `artifacts/input-identity-repair/external-input-equivalence-final.json`
has raw SHA-256
`44bcd643818cc052d2414b5d550b40c267c6b30cee8b43ac949f373ccd4d6037`
and identifies original archived external authority
`2af4930abb37db3361b47ad4d9d878c5a049fa1cd4c129b78902ae681fb44a8e`.

## Published-regression boundary

`geocedg/validation/operations/g9s1-r1-published-regression-authority.json`
pins the historical reviewed technical and approved closeout commits. The new
`Get-GeoCeDGPhasePublishedRegressionContext` authenticates their exact seven-path
Git delta, frozen status transformation, author record and implementation
provenance. It requires the live HEAD to descend from the approved closeout.
The original 76-path candidate inventory is historical scope, not a prohibition
on separately authorized later product work.

The actual-history direct context check passed with 76 candidate paths and the
expected seven closeout paths. Its explicit result is
`historicalApprovalAuthenticated=true`, `liveScientificVerificationRequired=true`,
`DocumentaryEvidenceLinked=false`, `consumableBuildReceipt=false` and
`currentCohortEquivalentToHistoricalTechnicalExecution=false`. Parser checks for
the lifecycle helper and R1 verifier passed. This bounded check is not PHASE or
heavy execution evidence; dedicated fixture and live integration gates remain
recorded below.

## Evidence and impact gates

The previous successful technical PHASE A/B, COMPOSED and clean FULL evidence
remains bound to the reviewed technical commit above. The established linkage does not
report that those executions ran again. The operational source cohort,
identity-fixture outputs, execution-plan/impact proof and bounded live exercises
must be recorded separately.

| Gate | Current execution record |
|---|---|
| Dedicated identity/lifecycle A/B deterministic suite | PASS, exit 0, 270/270 each; `focused-a5` and `focused-b`, identical canonical summary. |
| LF/CRLF/configuration/binary/mode positive controls | PASS in both 45-case repository-identity suites. |
| Blob/mode/rename/extra-path/index/dirty/untracked/filter/encoding negatives | PASS in both repository-identity and 37-case repair-equivalence suites; hostile filters rejected before execution. |
| Evidence tampering and wrong T/C negatives | PASS in both 56-case lifecycle suites and identity/equivalence controls. |
| PowerShell parser/static and Git whitespace checks | PASS in both final canonical summaries; 37 relative documentation links including nine heading anchors resolve. |
| Actual R1 T→C and previous `verify-baseline.ps1` case | PASS for both final LF and CRLF target exercises; exact T/C and seven-path allowlist authenticated; no product execution repeated. |
| Real canonical shared-module integration | DEV exit 0; 16/16, 6.823 seconds; `live-shared-final/verification-result.json`. Scoped integration, not PHASE acceptance. |
| Real canonical Desktop integration | DEV exit 0; 3/3, 11.969 seconds; `live-desktop/verification-result.json`. Scoped integration, not PHASE acceptance. |
| Execution-plan fingerprint and fifteen-condition impact proof | PASS; identical reviewed/candidate projection over 11,192 inputs plus the complete evidence conjunction below. |
| Sealed prior scientific evidence authentication | Actual target exercises authenticated bundle `0757b2d52d3aca85f85961c48dce3b90992efed589df387d41aabc2938585418`; external-input/JDK comparison matched under the managed account. |
| `EVIDENCE_PRESERVING_VERIFIER_REPAIR` | ESTABLISHED — true; all fifteen author-required conditions proved. |
| Additional heavy verification | No new PHASE/COMPOSED/FULL campaign executed for this repair; prior sealed technical execution is linked explicitly, not relabelled. |
| Operational commit / exact changed paths | Exact 16-path inventory below; containing Git commit supplies its SHA without a self-referential report hash. |

Operational approval follows the author's explicit conditional authorization,
not agent self-approval or a fixture-generated author decision. Both complete
focused runs report `heavyEvidenceReuseApprovedByThisRunAlone=false`; the final
determination also requires the independently authenticated historical bundle,
actual closeout proofs, external/toolchain comparison and live integrations.

The canonical command was executed in fresh processes with distinct output
directories:

```powershell
.\tools\agent\verify-input-identity-repair.ps1 -LogDirectory artifacts/input-identity-repair/focused-a5
.\tools\agent\verify-input-identity-repair.ps1 -LogDirectory artifacts/input-identity-repair/focused-b
```

Each completed with exit 0 and the following suite counts: repository identity
45/45; repair equivalence 37/37; phase lifecycle 56/56; verification runtime
114/114; generated state 18/18. Each directory retains the corresponding
`repository-identity.log`, `repair-equivalence.log`, `phase-lifecycle.log`,
`verification-runtime.log`, `generated-state.log`, `execution-plan-proof.json`
and canonical summary. Both `canonical-summary.json` files have canonical-LF
SHA-256
`cc3b6352518e913eda52c0419c4185f265a673c3fbf4786ff3ec1ee36b424307`.
The policy canonical-LF SHA-256 is
`1b73a1b681104ef8b6cc8b387a489e60825a1dd94978e5c18aea06c401fed429`.

The two saved exact-target results are:

- `artifacts/input-identity-repair/actual-closeout-lf-final/author-closeout-result.json`
- `artifacts/input-identity-repair/actual-closeout-crlf-final/author-closeout-result.json`

Both are byte-identical, SHA-256
`87059915421666ff5a853b91053be793be2786aeb2bb6aa83727a7cf1026a10f`.
Both explicitly report `technicalExecutionRepeated=false`,
`productRuntimeExecuted=false` and `currentRunReceiptProduced=false`.
They authenticate the same 11,202 tracked entries and tree
`dc9a47a800dfc699568502e19248b22531d7ad6b` despite the observed 14,399-byte
mixed-CRLF versus 14,167-byte LF `verify-baseline.ps1` materializations.
The tracked identity digest is
`b3274499568a90b88b53989dcc4d04307532fc6452a5620060f14b7462114b98`.

The shared and Desktop `verification-result.json` raw SHA-256 values are,
respectively:

```text
eb5b908bc8528d37098f81b40908c6f29ada74a5b35456fa55f1abb71c2747a1
2e97a12823db3968f6c4336970a7c0c5bdd9db5ad2517075984ba22944be260e
```

The final execution-plan comparison retains 11,192 inputs and has
fingerprint
`8374d030ccfc38716143ba3d6d91b52da26a07c97f6dae89a1548f4da82b27ab`.
The two `execution-plan-proof.json` files are byte-identical, raw SHA-256
`0b714a64f36c1f3d78c650719d69f13af2790836706d40641621a5fb00854007`.
Its narrowly classified operational inventory contains 16 paths in
`geocedg/validation/operations/input-identity-repair-policy.json`. Both final
summaries attest `sameRunPhysicalInputsUnchanged=true`. The actual LF/CRLF
proofs record identical hashes for their six loaded inspection helpers; the
historical runtime and generated-state implementation were not changed.

### Fifteen-condition evidence conjunction

| Author-required condition | Established evidence |
|---|---|
| 1. Zero product Java change | Exact Git/path/mode protected-input comparison; adversarial product mutation rejected. |
| 2. Zero Desktop product/UI change | Same protected-input proof; Desktop mutation negative. |
| 3. Zero scientific-test change | Same protected-input proof; scientific-test mutation negative. |
| 4. Zero numerical reference/tolerance change | Same protected-input proof; reference mutation negative. |
| 5. Zero Gradle/build-script change | Exact retained build inputs; build/Gradle-property mutation negatives. |
| 6. Zero task/filter selection change | Unchanged retained top-level verification and scientific execution bodies; exact narrowly reviewed provenance substitutions only. |
| 7. Zero required Java/toolchain change | Retained execution authorities plus exact managed-account external/JDK comparison. |
| 8. Zero numerical-command change | Retained execution-plan inputs and explicit protected-contract comparison. |
| 9. Zero JUnit pass/fail semantics change | Runtime implementation unchanged; 114 runtime cases per run and acceptance-edit negatives. |
| 10. Zero generated-state execution lifecycle change | Implementation unchanged; 18 generated-state cases per run and protected-input mutation negative. |
| 11. Changed executable scope restricted to identity/provenance/closeout | Exact old/new replacement projection, new-function review, and rejection of disguised scientific code, old execution-function exclusion, top-level injection and unexpected helper edits. |
| 12. Successful sealed old heavy evidence bound to exact scientific cohort | Both actual T/C proofs authenticate the original bundle, raw inventories, receipts and exact reviewed commit. No new heavy execution claim. |
| 13. Complete dedicated infrastructure suite twice deterministically | 270/270 in each final run with identical canonical summary and unchanged same-run physical inputs. |
| 14. Bounded real canonical integration | Shared 16/16 and Desktop 3/3 DEV executions, exact LF/CRLF R1 closeout proofs, parser/whitespace checks and link audit. DEV remains scoped, non-acceptance evidence. |
| 15. Deterministic execution-plan/impact equivalence | Identical `8374d030…` projection over 11,192 inputs in both `execution-plan-proof.json` records; covers tasks, roots, filters, Checkstyle, numerical/reference commands, JVM/environment policy and acceptance. |

No single path allowlist, archive receipt or Boolean substitutes for this
conjunction. The new infrastructure-fixture launcher establishes UTF-8 transport
only for its isolated PowerShell children; product Gradle/JVM execution policy
is unchanged. The summary resolver correction affects infrastructure evidence
location only and is covered by direct/GUID, missing/ambiguous and linked-path
negative controls.

### Exact operational inventory and finalization

```text
AGENTS.md
docs/adr/0024-verification-input-identity.md
docs/developer/geocedg_developer_guide.md
docs/validation/verification_input_identity_report.md
geocedg/specs/operations/verification-levels.md
geocedg/validation/operations/g9s1-r1-published-regression-authority.json
geocedg/validation/operations/input-identity-repair-policy.json
tools/agent/phase-lifecycle.ps1
tools/agent/repository-input-identity.ps1
tools/agent/tests/phase-lifecycle.Tests.ps1
tools/agent/tests/repository-input-identity.Tests.ps1
tools/agent/tests/verification-repair-equivalence.Tests.ps1
tools/agent/verification-repair-equivalence.ps1
tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1
tools/agent/verify-input-identity-repair.ps1
tools/agent/verify-phase-author-closeout.ps1
```

Only this report and ADR 0024 were finalized after focused A/B. That is a
documentation-only status/evidence description delta: no executable, test,
policy, scientific or numerical input changed. The original run snapshots and
hashes remain historical; they are not regenerated as though the final prose
existed during execution. Final Git history identifies the operational commit;
the separately tagged R1 phase authority remains the existing closeout `C`.

The inherited developer-guide statements at lines 19–26 and 580 still describe
R1 as a candidate; they already exist in `C`. They are not normative phase
approval authority and were not rewritten by this operational repair. The
actual R1 decision/evidence records identify author-approved PASS.

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
EVIDENCE_PRESERVING_VERIFIER_REPAIR = true
VERIFICATION INPUT IDENTITY SEMANTICS = PASS — AUTHOR APPROVED
productPhaseEffect = NONE
scientificContractChanged = false
selfApproved = false
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: existing PowerShell/Git/JDK/Gradle/Conda prerequisites, wrapper layout,
numerical commands, downloads, packaging and generated-state execution remain
unchanged. Only identity/provenance/closeout proof is corrected; supported Git
materialization is audited under the existing workstation prerequisite.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md
```

R1 author approval remains valid and its corrected exact-target proof passed;
publication is a subsequent separately authorized operation, not claimed by
this pre-publication report. `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains
**OPEN / TRACKED**. This operational repair changes no G9U1 productive source.
