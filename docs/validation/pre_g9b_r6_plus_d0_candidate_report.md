# PRE-G9B-R6-plus-D0 documentary candidate report

```text
PRE-G9B-R6-plus-D0        = DOCUMENTARY CANDIDATE PENDING AUTHOR REVIEW
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

selfApproved              = false
authorApproved            = false
passClaimed               = false
VERIFICATION_CLASS        = DOCUMENTATION_STATUS_ONLY
PLANNED_ACCEPTANCE        = STATIC on this exact candidate
PRODUCT_PHASE_EFFECT      = NONE
```

This report records the execution of `PRE-G9B-R6-plus-D0` under the author's
authorization of 2026-10-02
([closeout and authorization record](pre_g9b_r6_plus_d0_prompt_closeout_record.md))
and the
[authorized canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md).
It is evidence, not authority: the unit rules are stated only in the
[unit-system specification](../../geocedg/specs/units/unit-system.md), which
this report references and does not restate. Its machine-readable mirror is
[`pre-g9b-r6-plus-d0-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-candidate-evidence.json).

## 1. Identities and entry gate

```text
P_R6PLUS_B          = 0ef616bb7bb8efd3e4f19762f38936ff4742d1c3   (published main;
                      tree be7ef7acbc3503002f3dac2bc33f047795f7a644)
T_R6PLUS_D0_PROMPT  = 8814468101e3caef45d9f6cbad4bf563523c3478   (approved preparation
                      package; tree edbe623604330e62db11ff3d3b05b7d73671dbd0;
                      not amended; IMPLEMENTATION_BASE)
A_R6PLUS_D0         = 804e20c08f8d25419799ed10b780483fa1670920   (authorization edit;
                      tree 63c7cfa07f892f0dbcb7cf09a53539ca4c32b807;
                      prompt blob b6025b8c088e6dd1f0bd69a2d311d794ed882c14)
T_R6PLUS_D0         = this documentary candidate, child of A_R6PLUS_D0; its
                      identity is reported with the STATIC run
branch              = phase/pre-g9b-r6-plus-d0-prompt (local; never pushed)
```

Entry gate at execution start: `HEAD` = `T_R6PLUS_D0_PROMPT`, tree
`edbe6236…`, parent `P_R6PLUS_B`, clean worktree; local `main` and `origin/main`
= `P_R6PLUS_B`. The authorizing instruction names the unpublished package as
the base; the prompt amendment records that.

## 2. Files of the candidate

Authorization edit `A_R6PLUS_D0` (first tracked edit):

| Path | Change |
|---|---|
| `.github/prompts/tasks/pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md` | amended to `AUTHORIZED FOR EXECUTION` with the `DQ-D0` dispositions |
| `docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md` | new: preparation approval, authorization, dispositions |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-prompt-closeout.json` | new: its mirror |

Documentary candidate `T_R6PLUS_D0`:

| Path | Change |
|---|---|
| `docs/adr/0032-unit-system-semantics-and-persistence-ownership.md` | new ADR, `PROPOSED` |
| `geocedg/specs/units/unit-system.md` | new specification, `NORMATIVE CANDIDATE — NOT AUTHOR APPROVED`, version `1.0` |
| `geocedg/specs/export/geometry-export-foundation.md` | one delimited not-in-force pointer note (10 inserted lines); no rule and no status changed |
| `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` | §4.4 supersession note; `D0` status lines |
| `docs/roadmap/geocedg_roadmap.md` | version 4.46; `D0` status in the last-executed, next-gate, track and row entries |
| `docs/validation/pre_g9b_r6_plus_d0_candidate_report.md` | this report |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-candidate-evidence.json` | its mirror |

No path under `source/`, `apps/`, `packaging/`, `tools/`,
`geocedg/specs/operations/`, `.github/prompts/` (other than the `D0` prompt in
the authorization edit), `ai-shell/`, `models/` or `artifacts/` changed.
`geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` is unchanged.

## 3. Re-characterization

The seams of the prompt's re-characterization table were established at
`P_R6PLUS_B` during the preparation. Between `P_R6PLUS_B` and
`A_R6PLUS_D0` only documentation changed (`git diff --name-only` lists seven
documentation paths), so every citation still applies. During execution the
following were re-read from source to state the route table of specification
§9 exactly:

| Fact | Evidence |
|---|---|
| the undo snapshot and the full document XML both call `getConstructionXML` | `common/io/MyXMLio.java:155`, `:316` |
| paste is a non-clearing parse; paste rollback re-reads `app.getXML()` with a clearing load | `common/util/InternalClipboard.java:623`, `:640`, `:669` |
| `evalXML` wraps in a bare `<construction>` and parses without clearing; `setXML` clears | `common/plugin/GgbAPI.java:169-179`, `:1474-1477`; `common/main/App.java:5126` |
| redefine rollback re-reads `getCurrentUndoXML` with a clearing load | `common/kernel/Construction.java:2673-2695` |
| rejected spatial parses restore the previous state through a clearing load (fail-closed precedent) | `common/io/MyXMLio.java:425-447`, `:504-516` |
| `addMacroXML` parses macro XML in `isGGTOrDefaults` mode | `common/main/App.java:3664-3670` |

No scratch probe was executed; every statement is `PROVEN FROM SOURCE` except
the cross-window clipboard behavior, which stays `INFERRED` as in `P0`.

## 4. Outputs and where they live

| Required output | Location |
|---|---|
| 1. unit-system ADR | [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md) |
| 2. normative unit-system specification | [unit-system.md](../../geocedg/specs/units/unit-system.md) |
| 3. proposed/future geometry-export amendment contract | specification §15; pointer note in the [foundation](../../geocedg/specs/export/geometry-export-foundation.md) |
| 4. persistence and XML grammar contract for `D1` | specification §8, §9 |
| 5. exact `usm` grammar and lifecycle | specification §4, §8.2, §8.4 |
| 6. invariant matrix | specification §17.1 |
| 7. persistence, undo and compatibility matrix | specification §17.2, §17.3 |
| 8. `AQ-U1`…`AQ-U6` traceability | specification §19.1 |
| 9. `DQ-D0-1`…`DQ-D0-13` dispositions | [closeout and authorization record](pre_g9b_r6_plus_d0_prompt_closeout_record.md) (authority); specification §19.2 (clauses) |
| 10. contracts for `D1`, `C`, `E2`, `E3` | specification §18 |
| 11. candidate report | this report |
| 12. machine-readable mirror | [candidate evidence](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d0-candidate-evidence.json) |

## 5. Inherited contracts and supersessions

- **Inherited unchanged:** plan §4.1–§4.3 and `AQ-U1`; `AQ-U2`–`AQ-U6`; the
  undo semantics; the unit-independence invariant; the persistence target;
  `AQ-X2` (`ExportArea` `SESSION`), which `drawingScale` parallels; the `E2`
  and `E3` creation-time capture design (`AQ-E4`, `AQ-E3c` stay with those
  phases).
- **Superseded:** the plan §4.4 wording `physical model-to-output unit =
  presentationUnit` (supersession note in the plan; rule in specification
  §13); the `P0` inputs §1 reading of `presentationUnit` as expressing "export
  quantities" in a way that affects size; the `P0` inputs §3 alternative of a
  fail-closed marker and §7 option of a "labelled legacy mapping" for
  engineering scale (decided by `DQ-D0-7` and `DQ-D0-11`); the `P0` inputs §6
  description of the foundation as "accepted" (it is `Experimental`). The
  frozen `P0` artifacts are not edited.
- **Not changed:** the foundation's G5 rules and status; the G9X1 fidelity
  contract and its verifier pin; Picture export's `printingScale` sizing. They
  stay in force until `C` (specification §15.1).

## 6. Design choices of `D0` for author review

These follow from the decisions but were not dictated by them; the author may
accept, change or reject each when reviewing the candidate:

| Choice | Clause |
|---|---|
| a *remove* operation for an unused `usm` definition, permitted but not required | §4.3 |
| name and symbol grammar: 1–64 Unicode scalar values, no control character, no leading or trailing white space | §4.4 |
| `presentation` requires `construction`; a presentation-unit preference default ∈ {none, `mm`, `cm`, `m`} (no `usm`, by the same reason as `DQ-D0-1`) | §5.1, §10 |
| canonical factor form = the Java SE 19+ `Double.toString` specification; the reader also accepts non-canonical valid decimals and re-saves them canonically | §8.4 |
| fail closed also on an unknown attribute, a duplicate element, child content and a name or symbol without a factor | §8.7 |
| element position after `<worksheetText>` and before `<geocedgSpatial>`; any position accepted on read | §8.1, §8.7 |
| element in macro or non-clearing parses ignored with a log diagnostic, not validated | §8.8 |
| built-in factors compared through their binary64 nearest values; `usm` with `k = fb(mm)` has the physical meaning of `mm` | §3.2, §5.4 |
| DXF codes `mm` → `4`, `cm` → `5`, `m` → `6`, from the AutoCAD DXF header table, to be verified by `C` | §15.2 |
| LaTeX physical `xunit = fb(c) · 100 · a / b` cm per model unit | §15.2 |

## 7. Deviations

- The authorization edit adds a closeout and authorization record and its
  mirror besides the prompt amendment, following the `B` precedent, so that
  the dispositions are versioned with a JSON mirror.
- The base is the unpublished approved package, by author instruction; the
  prompt's entry gate was amended accordingly.
- The prepared prompt's G9X1 cross-reference note is not created
  (`DQ-D0-10`); the amended prompt already excludes it.
- No other deviation from the amended prompt.

## 8. Unresolved findings and questions before `D1`

1. **Fail-closed restore.** The existing restore covers only identity-bearing
   spatial parses (`MyXMLio.java:425-447`). `D1` must extend it to unit
   rejections and characterize the state of a window whose File → Open fails,
   before implementing §8.7.
2. **Canonical factor runtime.** The verification environment requires JDKs
   17 and 25; `Double.toString` meets the Java SE 19+ specification only on
   19+. `D1` must confirm the product runtime and prove §8.4 with fixed
   reference strings on every supported runtime.
3. **Unit user interface.** No author decision fixes where the user sets the
   construction and presentation units, defines `usm`, or sets the
   new-document preferences, nor how the paste notice is shown. These are
   `D1` design items needing an author decision or a `D1` prompt contract.
4. **`drawingScale` holder.** The specification fixes its semantics and
   lifetime; the plan places engineering scale in `C`, and `E3` captures it.
   The phase that introduces the holder (recommended: `C`) needs fixing in the
   `C` prompt.
5. **Two windows.** Whether two windows share a clipboard stays `INFERRED`; a
   `D1` probe settles it.
6. **`C` verification class.** Discharging §15.3 changes the G9X1 verifier
   pin and evidence contract (`tools/agent/verify-g9x1-extended-dxf.ps1:449`),
   which is verification infrastructure; the class proposed for `C` in the
   plan (`INTEGRATED_PHASE`) must be re-examined when `C` is prepared.
7. **Guide impact.** The silent loss of units on re-save by older builds
   (§8.9) must reach the user guides once `D1` exists.

## 9. Impact declarations

```text
PRODUCT_PHASE_EFFECT              = NONE
SERIALIZATION_CHANGE              = NONE   (contract specified, not implemented)
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE  (no verifier, registry, schema,
                                            prompt-contract or tools/agent change)
BOOTSTRAP IMPACT                  = NO CHANGE — documentation and specification
                                    only; no workstation prerequisite or
                                    bootstrap assumption is touched
GUIDE_IMPACT                      = NONE in D0 — no user-visible behavior
                                    exists; guide obligations recorded for D1
                                    (finding 7)
```

## 10. Verification

```text
tools/agent/verify.ps1 -Profile STATIC -LogDirectory <fresh root outside the repository>
```

on this exact candidate, with the console redirected outside `artifacts/`.
Its run identity, exit code and verdicts are reported with the candidate; this
frozen report cannot contain them. Before freezing: prompt validated with
`Test-PromptContractDocument` (`task` profile, `execution_safe = true`); JSON
validity of the new mirrors; every repository-relative Markdown link of the
changed files resolved; `git diff --check` clean. No `PHASE`, `INTEGRATION` or
`FINAL` run is part of `D0`.

## 11. State

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
D0 preparation package     = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-D0         = DOCUMENTARY CANDIDATE PENDING AUTHOR REVIEW
PRE-G9B-R6-plus-D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release was performed.
