# PRE-G9B-R6-plus-E3 and E3-R1 closeout record

```text
PRE-G9B-R6-plus-E3-R1  = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-E3     = PASS — AUTHOR APPROVED
APPROVED_CANDIDATE     = T_R6PLUS_E3_R1 7cd501167c856eef356888ca87ed3a0c97cea55c
AUTHOR_SMOKE           = PASS WITH ACCEPTED NON-BLOCKING VERIFICATION LIMITATION
ASYMPTOTE_WITH_LABEL   = UNAVAILABLE — NOT CLAIMED PASS
PUBLICATION            = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (E3 and E3-R1, approved candidate T_R6PLUS_E3_R1 only)
passClaimed               = true    (E3 and E3-R1 only)
implementationAuthorized  = false   (F1, F2, F3, F4, G and every observation below, except
                                     the bounded POST-E3-LEGACY-INVENTORY-CASE-FIX of §8)

candidateFrozen           = true
candidateMutated          = false   (neither 257c854a nor 7cd50116 amended)
PRODUCT CHANGE            = NONE    (closeout)
SERIALIZATION CHANGE      = NONE    (closeout)
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout; FINAL not required by INTEGRATED_PHASE)
```

This record preserves the explicit author decision of 2026-10-10 on
`PRE-G9B-R6-plus-E3` and its corrective successor `PRE-G9B-R6-plus-E3-R1`,
given in writing in the session ("GeoCeDG — E3 / E3-R1 Author Closeout and
Follow-up Authorization", task `PRE-G9B-R6-plus-E3-AUTHOR-CLOSEOUT`, and the
author's clarification of the same day on the normative promotions and the
follow-up scope), with the exact identities behind it. It is the sole authority
for the author approval of `E3` and `E3-R1` and for the authorizations of §8.
Its machine-readable mirror is
[`pre-g9b-r6-plus-e3-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-closeout.json).

Under the frozen-candidate rule, the
[E3 candidate report](pre_g9b_r6_plus_e3_candidate_report.md), the
[E3-R1 characterization report](pre_g9b_r6_plus_e3_r1_characterization_report.md),
the [E3-R1 candidate report](pre_g9b_r6_plus_e3_r1_candidate_report.md) and
their evidence mirrors record only frozen state and are not amended. Besides
this record and its mirror, the closeout changes only the status markers of
§6, the three prepared task prompts of §8, and status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

## 1. Entry verification

```text
live remote main  = origin/main = ff56099184544e5988c63e0ea3339e3d0fe01fac
                    tree 1cdd1e986df785f5f16e7887300d9034e5e4a677   (P_R6PLUS_E3_DESIGN)
branch            = phase/pre-g9b-r6-plus-e3-r1-export-correction, clean, local only
history           = ff560991 → 3a577ea4 (Stage A reconciliation)
                    → 257c854a (original E3 candidate, tree b3cedf15)
                    → 7cd50116 (E3-R1 corrective successor, tree 3c51c9d3)
                    linear; 7cd50116 is a direct child of 257c854a; no commit
                    amended, squashed or rewritten
```

## 2. Author decision

### 2.1 Author smoke

| Candidate | Author result |
|---|---|
| original `E3` `257c854a` | `PASS WITH TWO BLOCKING EXPORT OBSERVATIONS`: every functional check passed except `OBS-E3-DXF-INVALID-OUTPUT` (DXF rejected by AutoCAD 2026) and `OBS-E3-LATEX-EXPORT` (invalid and incorrectly composed LaTeX) |
| `E3-R1` `7cd50116` (focused corrective smoke) | the corrected DXF opens in AutoCAD 2026; the ISO A geometry and margins are correct; the remaining manual smoke checks are satisfactory; the corrected LaTeX export compiles and shows the expected complete drawing |

```text
AUTHOR_SMOKE = PASS WITH ACCEPTED NON-BLOCKING VERIFICATION LIMITATION
               (the limitation is the Asymptote-with-label compilation of §5)
```

### 2.2 Approval

```text
PRE-G9B-R6-plus-E3-R1 = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-E3    = PASS — AUTHOR APPROVED
approved candidate    = T_R6PLUS_E3_R1 7cd501167c856eef356888ca87ed3a0c97cea55c
                        tree 3c51c9d3635c22939df6cab60259521f885eff92
original candidate    = T_R6PLUS_E3 257c854aa65fe82d316968109b9d084deefd2a49
                        tree b3cedf15b42c3a4dc5925744e83d510ea2dd4850 (historical evidence)
publication           = NOT AUTHORIZED (separate author instruction required)
selfApproved          = false
```

The approved `E3` implementation is the `E3-R1` successor. The approval
includes the satisfactory author smoke after `E3-R1`.

## 3. Evidence

### 3.1 Accepted evidence (approved candidate)

```text
PHASE PRE-G9B-R6-PLUS-E3-R1 on 7cd50116 / 3c51c9d3
      verification-c4853de46301459bafd4801a98dd6365, ACCEPTED / COMPLETE, 4/4,
      0 diagnostics; result_hash 90b3a3596f6eb99fa5b17ab01701ade6b10592a55cd4f82a89c50f2b0507899b
      artifacts\agent\pre-g9b-r6-plus-e3-r1-phase
INTEGRATION on 7cd50116 / 3c51c9d3
      verification-cf79b0af0a244c5f9bf8f1f4be5d2c0f, ACCEPTED / COMPLETE, 19/19,
      diagnostics 8 clear, 1 finding (standing governance), 1 unavailable (standing
      historical consistency); result_hash ffdb1112fbb5f46144ee72c8ec1c642a15751859ada6495fc601730a0a1dc17c
      artifacts\agent\pre-g9b-r6-plus-e3-r1-integration
final.shared 6992 and final.desktop 1985 identities executed and passing
      (inventory producers on the candidate code)
```

### 3.2 Historical evidence (original candidate, not substituted)

```text
PHASE PRE-G9B-R6-PLUS-E3 on 257c854a / b3cedf15
      verification-a243082e9be1444f910aa3d7cab15e47, ACCEPTED / COMPLETE, 7/7
      artifacts\agent\pre-g9b-r6-plus-e3-phase-257c854a
INTEGRATION on 257c854a / b3cedf15
      verification-d0e9c1b892f34061a29e4d41fb6aa0b5, ACCEPTED / COMPLETE, 19/19
      artifacts\agent\pre-g9b-r6-plus-e3-integration-257c854a
```

The four results were read from their local result files at closeout; each
names the exact commit and tree above. The `E3` runs are not evidence for the
`E3-R1` candidate.

## 4. Resolved export observations

```text
OBS-E3-DXF-INVALID-OUTPUT = RESOLVED BY E3-R1 — AUTHOR CONFIRMED (AutoCAD 2026 opens the file)
OBS-E3-LATEX-EXPORT       = RESOLVED BY E3-R1 — AUTHOR CONFIRMED (compiles; complete drawing)
```

- **DXF, historical origin.** The defect was not introduced by `E3`: the
  AC1015 container written since `G5` lacked `$HANDSEED`, most symbol tables,
  table and record handles, owners and subclass markers, the space blocks and
  the OBJECTS section. `DxfExporter` was byte-identical in `ff560991` and
  `257c854a`. The defect was already recorded as
  `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` (AutoCAD 2024, "Error in APPID
  Table", `C` closeout) and was exposed again by the `E3` smoke. `E3-R1` writes
  the complete container with comments, entity records and entity handles
  unchanged; the long leading comment was not the cause.
- **LaTeX.** `E3-R1` writes the sheet label in text mode with `\textemdash{}`
  inside the frame and composes a physical export as a page equal to the
  export area (A3 at 1:10 is 420 × 297 mm; at 1:5, 840 × 594 mm), with the
  host `UnicodeTeX` mapping unchanged.

## 5. Residual verification limitation

```text
PGF/TikZ compilation                          = VERIFIED
PSTricks compilation (E3 sheet)               = VERIFIED
Asymptote physical composition without label  = VERIFIED
Asymptote compilation with label              = UNAVAILABLE in the agent environment
                                                — NOT CLAIMED PASS
```

The installed `asy` 3.06 could not typeset any label with any TeX engine in the
agent environment. The author accepts this as a **non-blocking verification
limitation** for the `E3`/`E3-R1` closeout, without any claim that the
unverified behaviour has passed. It is registered as
`OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED`; its future
characterization or verification needs independent tracking and its own
authorization. Asymptote is not changed by this closeout.

## 6. Normative status after the decision

| Document | Status |
|---|---|
| [ISO A sheet border specification](../../geocedg/specs/sheets/iso-a-border.md) `1.0` | `NORMATIVE / AUTHOR APPROVED`, including the `E3-R1` amendment §14.1a: the geometric and constructive contract, dependencies, identity, persistence, units, scale, representation, `ExportArea` interaction and export behaviour |
| [ADR 0036](../adr/0036-iso-a-border-representation-and-export-area-producers.md) | `ACCEPTED — AUTHOR APPROVED`: R2 representation with existing types, optional inner frame, no new XML type, constructive identity and dependencies, explicit and transient participation in the `ExportArea` authority; no further architectural extension authorized |
| [G5 geometry export foundation](../../geocedg/specs/export/geometry-export-foundation.md) | global status unchanged, `Experimental`; the `E3-R1` AC1015 container paragraph alone is `AUTHOR APPROVED — IMPLEMENTED AND VERIFIED`; the specification is not promoted and the pending `C` amendment is not disposed of |

Only status markers changed (with the dates and identities above); every clause
is the text of the approved candidate and no requirement was added. Approved
product behaviour of `E3-R1` that the G5 specification describes is therefore
governed, as a whole, by the ISO A specification and by the localized G5
amendment, not by a global G5 promotion.

## 7. Observation and debt dispositions

| Identifier | Disposition |
|---|---|
| `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE` | implemented by `E3`; approved with it |
| `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` | resolved by the approved `E3` implementation (`DQ-E3-12`) |
| `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` | resolved by `E3-R1` for AutoCAD 2026 (author smoke); other AutoCAD versions not rechecked; the `F4` planning intent is unchanged |
| `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` | retained — independent; characterization authorized as `POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION` (§8) |
| `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` | retained — pre-existing on `257c854a`, not attributed to `E3-R1`; characterization authorized as `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE` (§8) |
| `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | retained — independent; bounded correction authorized as `POST-E3-LEGACY-INVENTORY-CASE-FIX` (§8) |
| `OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED` | accepted non-blocking verification limitation (§5); no owner authorized |
| `E3` known limitations (candidate report §13) | retained: DXF writes no sheet label; builds without `IsoABorder` lose sheets on re-save; no title block, sheet set, paper space or viewport; `OTQ-E3-2` to `OTQ-E3-4` |
| host `UnicodeTeX` mapping of other characters | unchanged; outside `E3-R1` |
| standing verifier diagnostics | governance `FINDING` and historical-consistency `UNAVAILABLE`; pre-existing, not acceptance |

Every other open observation and debt of the mini-track keeps its recorded
state; none is absorbed by this closeout.

## 8. Follow-up activities

The author approves three independent follow-up activities with the bounded
scopes below. They are not executed by this closeout. Each runs later as its
own task and branch, preferably after the publication of this closeout, with
its own exact baseline, evidence and status; they are never merged into one
implementation commit, and approval of a characterization never implies
approval of a proposed correction.

| Order | Identifier | Observation | Authorization | Prepared contract |
|---|---|---|---|---|
| 1 | `POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION` | `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` | `CHARACTERIZATION AND DESIGN AUTHORIZED — CORRECTION NOT AUTHORIZED` | [prompt](../../.github/prompts/tasks/post-e3-es-command-lookup-characterization.prompt.md) |
| 2 | `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE` | `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` | `CHARACTERIZATION AND CORRECTIVE DESIGN AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED` | [prompt](../../.github/prompts/tasks/post-e3-e2-pstricks-dimension-angle.prompt.md) |
| 3 | `POST-E3-LEGACY-INVENTORY-CASE-FIX` | `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | `BOUNDED IMPLEMENTATION AUTHORIZED — SUBJECT TO CHARACTERIZATION AND READINESS GATE — NO AUTOMATIC PUBLICATION` | [prompt](../../.github/prompts/tasks/post-e3-legacy-inventory-case-fix.prompt.md) |

Boundaries carried by the prompts: no change to command lookup, localization or
ADR 0031 behaviour in activity 1; no kernel-orientation change and the expected
correction in the export adapter in activity 2; `Templatev7.ggb` and every
`models/legacy/` original immutable in activity 3, with a stop if the
correction would need to change them or cannot stay bounded.

## 9. Closeout verification

The closeout commit is a documentary descendant of the approved candidate, not
a replacement candidate. Its class is `DOCUMENTATION_STATUS_ONLY`
(`verification-levels.md`): `STATIC` on the closeout commit, and the three
uncatalogued prompts validated directly with the prompt-contract parser. Those
runs are reported outside this record, which cannot name its own commit.

## 10. Publication

Publication of the implementation or of this closeout to `main` is **not
authorized**. It requires a separate explicit author instruction after the
author confirms the exact closeout commit and tree. No tag, release, binary
publication or product-version change is authorized.

## 11. Next activity

```text
NEXT   = author decision on the publication of the E3/E3-R1 closeout
THEN   = POST-E3 activities 1, 2, 3 (§8), each separately
F1: NOT AUTHORIZED
F2: NOT AUTHORIZED
F3: PLANNED — NOT AUTHORIZED
F4: PLANNING INTENT — NOT AUTHORIZED
G:  NOT AUTHORIZED
```
