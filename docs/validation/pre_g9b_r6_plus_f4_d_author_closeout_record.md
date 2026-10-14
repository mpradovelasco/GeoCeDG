# PRE-G9B-R6-plus-F4-D author approval and closeout record

```text
PRE-G9B-R6-plus-F4-D      = PASS — AUTHOR APPROVED (with its scope amendment 01; one
                            approved implementation)
AUTHOR_SMOKE              = PASS (author confirmation; not performed by the agent)
APPROVED IMPLEMENTATION   = 6214d15a0bc986cf1d16f178c67fd76bf6e54bc2
                            tree 47b634b1885a9b5650af73369caa97a955bf22f2
                            parent 3968771327aca5aa3e7b47fc68a74451f44fc9d6
INTEGRATED_PHASE          = ACCEPTED / COMPLETE (PHASE PRE-G9B-R6-PLUS-F4-D,
                            INTEGRATION 19/19, INFRA_UNIT 22/22 and STATIC on the
                            approved implementation)
TD-PDF-2                  = CLOSED — RESOLVED BY F4-D (author approved, 6214d15a)
TD-PDF-3                  = CLOSED — RESOLVED BY F4-D SCOPE AMENDMENT 01 (author
                            approved, 6214d15a)
OBS-F4P-FREEHEP-XREF-SIZE = OPEN — UNRESOLVED — INDEPENDENT OBSERVATION (unchanged)
F4-D STOP                 = aae7b12c — HISTORICAL EVIDENCE (accepted as correct)
F4-D WIP                  = 6c2fc409 — UNVERIFIED — NOT IN THE APPROVED LINEAGE
F4-D PUBLICATION          = NOT AUTHORIZED
F4-A                      = NOT AUTHORIZED — STARTS ONLY AFTER F4-D PUBLICATION (DQ-F4-10)
G                         = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (F4-D with scope amendment 01 only, implementation 6214d15a)
passClaimed               = true    (F4-D only)
implementationAuthorized  = false   (F4-A, G, every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE    (closeout; INTEGRATED_PHASE accepted on 6214d15a)
closeoutClass             = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
```

## 1. Author instruction and scope

On 2026-10-14 the author authorized in writing "GeoCeDG — PRE-G9B-R6-plus-F4-D AUTHOR
APPROVAL AND CLOSEOUT". The author confirms that every interactive F4-D author-smoke test
passed (`AUTHOR_SMOKE = PASS`) and explicitly approves the technical implementation
candidate `6214d15a0bc986cf1d16f178c67fd76bf6e54bc2` (tree
`47b634b1885a9b5650af73369caa97a955bf22f2`), covering both minimal FreeHEP corrections:

1. the elimination of the `AbstractCharTable.toName(char)` recursion (`TD-PDF-2`);
2. the correction of the missing initial Type 1 moveto in `CharstringEncoder.move()`
   (`TD-PDF-3`).

The accepted evidence comprises the four official verification profiles, the structural
Type 1 tests, the external PDF validation and the author smoke. The authorization covers
the documentary closeout only: no product, test, specification, registry or accepted
evidence change, no heavy re-verification while the exact-candidate evidence stays valid,
no publication, no release tag, no F4-A and no G. This record is the sole authority for the
author approval of F4-D; the agent records the decision and does not approve its own work.
Machine-readable mirror:
[`pre-g9b-r6-plus-f4-d-author-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-d-author-closeout.json).

## 2. Entry verification

```text
PUBLISHED BASE        = 847bb20a593e8092eabf80bd9d7c62ed7c21de08
                        (local main = origin/main = live remote main, read with
                        git ls-remote on 2026-10-14)
APPROVED CANDIDATE    = HEAD of phase/pre-g9b-r6-plus-f4-d-amendment-01 at entry:
                        commit, tree and parent re-resolved and equal to the author's
                        identities
LINEAR CHAIN          = 847bb20a → 089fc220 → 3251f085 → 83f07931 → 0c788526 →
                        3d609397 → aae7b12c → 39687713 → 6214d15a (8 commits, one parent
                        each, no merge; every tree re-resolved)
WIP                   = 6c2fc409 (parent 3d609397) is not an ancestor of 6214d15a
WORKTREE              = clean; no stash
SCOPE                 = 847bb20a..6214d15a touches 7 files under source/: the two
                        inherited FreeHEP files, two Desktop test classes and three test
                        fixtures; nothing under source/shared
```

| Commit | Role | Tree |
|---|---|---|
| `089fc2205300559c572c12f0cd32326abd0c2b5e` | F4 preparation | `61fa86b01d66f129e3bc52e8e93d05959530bed6` |
| `3251f08595fee8cd5819cc4c607e1dd97c6d0ad2` | F4 author-decision reconciliation | `0c6fcb3367ab7c8cf40b7e13d9c7f0253aad9aee` |
| `83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9` | F4 final author reconciliation amendment (approved design base) | `d4e6989c585e6839d22eaffc84df783c09a0c065` |
| `0c7885260761f442c25b380fcbbae11d1215fb51` | F4 author design closeout | `e75fb9c6b55a1677380802766300f514323762d6` |
| `3d609397cf82c428872a0b6859426aba7c0efdeb` | F4-D implementation authorization | `0c04e38f79f28d5506c025f055ade92048204c8e` |
| `aae7b12ccc7e8c21eced1b10f7aa08562ae2bec2` | F4-D implementation stop (historical) | `f6732b1536af3c831fb9bfb05bd8d0784d6f331a` |
| `3968771327aca5aa3e7b47fc68a74451f44fc9d6` | F4-D scope amendment 01 | `c88143b02760e88ea18d1b4185a0693b55eb31bf` |
| `6214d15a0bc986cf1d16f178c67fd76bf6e54bc2` | **F4-D technical candidate — approved implementation** | `47b634b1885a9b5650af73369caa97a955bf22f2` |

Authorities present and unchanged: [author design closeout](pre_g9b_r6_plus_f4_author_design_closeout_record.md)
(`DQ-F4-1` to `DQ-F4-10`); [implementation authorization](pre_g9b_r6_plus_f4_d_implementation_authorization_record.md);
[implementation stop report](pre_g9b_r6_plus_f4_d_implementation_stop_report.md);
[scope amendment 01](pre_g9b_r6_plus_f4_d_scope_amendment_01_record.md);
[candidate report](pre_g9b_r6_plus_f4_d_candidate_report.md) and its mirror. They remain
historical evidence and are not amended.

## 3. Lineage

| Step | Outcome |
|---|---|
| implementation on `3d609397` | the one-line `TD-PDF-2` correction removed the `StackOverflowError` but 43 of 230 embedded Type 1 glyphs rendered with slanted left stems; **STOP** `aae7b12c`, accepted by the author as correct; unverified work kept on `wip/pre-g9b-r6-plus-f4-d-type1-outline-blocked` (`6c2fc409`), never a candidate |
| scope amendment 01 `39687713` | author option A: productive scope `AbstractCharTable.toName(char)` and `CharstringEncoder.move(double,double)`; `INTEGRATED_PHASE` maintained by the source impact analysis |
| technical candidate `6214d15a` | both corrections, focused tests, registry selection and pins, external evidence; **final approved implementation** |

## 4. Accepted verification evidence

Re-read from the receipts on 2026-10-14; each `result_hash` recomputed with the verifier's
own `Get-VerificationResultHash` over the report read by `Read-VerificationJson`, and equal
to the stored value:

| Profile | Run | Verdict | Result hash |
|---|---|---|---|
| `PHASE PRE-G9B-R6-PLUS-F4-D` | `verification-0d7d0418479645779c9c2690ab50b140` | `ACCEPTED / COMPLETE`; `compile.desktop.semantic` and `junit.desktop.pre-g9b-r6-plus-f4-d.semantic` (selection of 53 identities) `CONTRACT_SATISFIED`; no diagnostic | `6f0116f2cef11bb96a1b7b7636ea340225a715445f4de15ce4e1b3c80f89424e` |
| `INTEGRATION` | `verification-c1f5709d41444f6e88c801a9d4ba9189` | `ACCEPTED / COMPLETE` 19/19 `CONTRACT_SATISFIED`; standing `diagnostic.governance` finding and `diagnostic.historical-consistency` unavailable | `3ac11536cd8c57055f38203c6c4a52cc63861df800a7f63ea12a6be9e26aa4e7` |
| `INFRA_UNIT` | `verification-1abe42a5651e4dafb469838331413042` | `ACCEPTED / COMPLETE` 22/22 `CONTRACT_SATISFIED` | `6c28c956fe072f98b0c5c2c3cbe6f7abad7ab4a01d8f5c03b722a71c24dfd6ff` |
| `STATIC` | `verification-e9a03b359f8f43208ebec344a125ca7e` | `ACCEPTED / COMPLETE` 3/3 `CONTRACT_SATISFIED`; the same standing diagnostics | `1cec22e1d4a58c1ac4a9a976f8956ac25b86a33b346db5104d738ddfaf4d8d2b` |

All four name candidate `6214d15a0bc986cf1d16f178c67fd76bf6e54bc2`, tree
`47b634b1885a9b5650af73369caa97a955bf22f2` and `run_state` `COMPLETED`; they ran in that
order on 2026-10-13/14 (UTC 23:36 to 00:21) with the commit and a clean worktree checked
before each run. Receipts: `artifacts/agent/f4d-amend01-phase/` and
`artifacts/agent/f4d-amend01-integration/`; the `INFRA_UNIT` and `STATIC` runs wrote to
the verifier's temporary folders, copied unchanged to `artifacts/agent/f4d-amend01-infra-unit/`
and `artifacts/agent/f4d-amend01-static/` (SHA-256 of `verification-result.json`:
`ba216fa753e9cc3023b724ca92bdb60b7b688367154f636ff7191b92f77d00ea` and
`0b58fd5da323a016459bb27ff2482a77eb612f67fb01e558ca27f96f67a5e55c`). The executed
`final.desktop` producer behind INTEGRATION ran on the exact commit. The development
`INFRA_UNIT` and `STATIC` runs of the candidate report §11 are bound to `39687713` and are
not acceptance evidence. `FINAL` was not run (above the frozen class). No heavy
verification is repeated for this closeout.

**Structural and external evidence** (candidate report §4–§6, reproduced on the frozen
commit): every glyph FreeHEP encodes for the product sans and serif fonts in four styles
decodes to its rounded Java shape; 0 glyphs without an initial moveto in all 17 embedded
programs (43 per program in the one-line state); Ghostscript 10.02.1, Poppler 24.04.0 and
Xpdf 4.06 read every PDF and extract the exact text, Spanish accents included; the stated
tolerance (r = 5 px at 1200 dpi, 0.106 mm) holds in every scene and both renderers, and
embedded-only ink is matched within 2 px in all 26 comparisons. The 12 PDFs written by the
frozen candidate equal the pre-freeze PDFs after the date normalization and the external
evidence reproduced identically (`artifacts/pre-g9b-r6-plus-f4-d/candidate/`;
`external-evidence.json` SHA-256 `ae3d56a0c21eef5cee2b95c355630dcfad87668d6ed2bbe7280553cc3d2f3922`).
Outline correctness (font-unit exact) and rasterization (renderer scan conversion) are
reported separately there. No new normalization exclusion was introduced.

## 5. Author smoke disposition

```text
AUTHOR_SMOKE = PASS
```

The author confirms that all interactive F4-D author-smoke tests passed (checklist:
candidate report §10). The agent did not perform the smoke and attributes no individual
result, reader or document to the author beyond that confirmation.

## 6. Approved implementation scope

1. `AbstractCharTable.toName(char)` delegates to `toName(Character.valueOf(c))` (one line,
   dated GeoCeDG notice): embedded-font PDFs are written again through the GeoCeDG physical,
   legacy and picture-dialog routes and the Classic picture dialog.
2. `CharstringEncoder.move(double,double)` writes `0 hmoveto` when the displacement from the
   current point is zero (two lines and a two-line notice; Adobe Type 1 Font Format 1.1,
   §6.4): every subpath of every embedded glyph begins with a moveto; nonzero displacements
   and every other command are encoded as before.

Both are registered in `docs/upstream/modified-files.yml`. No FreeHEP update, replacement or
refactoring, no dialog, exporter, kernel, serialization or text-size change; the
text-as-shapes PDFs, SVG, EMF and PNG outputs equal the base after the existing
normalization.

## 7. Debt and risk dispositions

| Id | Disposition | Historical evidence | Resolution reference |
|---|---|---|---|
| `TD-PDF-2` (= `OBS-POST-E2-P1-R1-PDF-EMBEDDED-FONTS-FAILS`) | **CLOSED — RESOLVED BY F4-D** | registered at POST-E2-P1-R1; product defect confirmed by `PRE-G9B-R6-plus-F4-PREP` (`089fc220`, upstream `c4fd03ce`); stop report | `AbstractCharTable.java` blob `52c0ff23` in `6214d15a`; obligations 1–4 and 6–8; author approval of this record |
| `TD-PDF-3` | **CLOSED — RESOLVED BY F4-D SCOPE AMENDMENT 01**; its relation to `TD-PDF-2` (latent behind it, surfaced only once it was corrected) is kept | stop report `aae7b12c`; WIP `6c2fc409`; scope amendment 01 | `CharstringEncoder.java` blob `e6d6c6f7` in `6214d15a`; `PreG9BR6PlusF4DType1OutlineTest`; external evidence; author approval of this record |
| `F4R-EMBED-FIDELITY` | **CLOSED** — realized as `TD-PDF-3` and resolved with it | register §2, §5 | as `TD-PDF-3` |
| `F4R-SHAPES-UNCHANGED` | **CLOSED — HELD** | register §2 | obligations 6–8 |
| `F4R-FAILED-EXPORT-REPORTING` | **CLOSED FOR F4-D — NOT REALIZED**: no export failure on any route after the correction; the inherited generic failure reporting of the picture dialog is unchanged and was not part of the approved F4-D obligations; no debt opened | register §2 | obligations 1–2 |
| `OBS-F4P-FREEHEP-XREF-SIZE` | **OPEN — UNRESOLVED — INDEPENDENT OBSERVATION**: Ghostscript still repairs "incorrect xref size" on every FreeHEP PDF, embedded and text as shapes alike; neither caused nor changed by F4-D; proposed for the G structured register; not authorized | register §3; candidate report §6 | — |
| FreeHEP provenance families, inherited `FIXME` in `FontEmbedderType1.writeGlyph`, JVM-wide Classic FreeHEP defaults, external tools outside the workstation contract | RETAINED NOTES — NON-BLOCKING (candidate report §9); none is a defect against the approved F4-D scope | candidate report §9 | — |

The F4-A risks of the register §2 (`F4R-POPUP-FOCUS`, `F4R-LINETYPE-COUPLING`,
`F4R-DEFAULTS-UNDO`, `F4R-CLASSIC-LEAK`) are untouched. `TD-PDF-1` stays deferred
(`DQ-F4-8`). No existing evidence establishes an unresolved violation of the approved F4-D
scope.

## 8. Closeout verification

Closeout class `DOCUMENTATION_STATUS_ONLY`: this record and its mirror, the F4 risk and debt
register §6, roadmap 5.44 and the mini-track plan; no product, test, specification,
catalog, registry, prompt or accepted-evidence change. The canonical F4-D prompt is not
edited (no prompt amendment is authorized). Acceptance by the official `STATIC` on the
documentary successor of `6214d15a`, reported outside this record, which cannot name its
own commit. PHASE, INTEGRATION and INFRA_UNIT are not repeated.

## 9. Publication readiness and F4-A dependency

```text
F4-D PUBLICATION = NOT AUTHORIZED
```

Assessment only, no action: the chain `847bb20a` → … → `6214d15a` → this closeout is linear,
descends from the live remote `main` `847bb20a` and contains no WIP or unrelated commit, so
an ordinary non-force fast-forward would be possible once the closeout is accepted by
`STATIC`. It includes the STOP commit `aae7b12c` as documentary lineage and excludes the
WIP `6c2fc409`. Publication needs a separate author authorization naming the exact closeout
commit, a fresh read of the live remote `main` and the publication gate; no push, tag or
release is made by this closeout.

F4-A remains **NOT AUTHORIZED**. Under `DQ-F4-10` it starts only after F4-D is published and
with its own authorization and exact base; this closeout does not satisfy that
precondition. The documentary aggregate F4 closeout follows F4-A. G stays not authorized.
