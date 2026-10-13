# PRE-G9B-R6-plus-F4-D implementation authorization record

```text
RECORD_KIND              = AUTHOR AUTHORIZATION, ENTRY GATE AND FROZEN VERIFICATION CONTRACT
TASK                     = PRE-G9B-R6-plus-F4-D IMPLEMENTATION AUTHORIZATION
AUTHORIZATION_DATE       = 2026-10-13
APPROVED_BASE            = P_F4_DESIGN_CLOSEOUT 0c7885260761f442c25b380fcbbae11d1215fb51
                           tree e75fb9c6b55a1677380802766300f514323762d6
                           parent 83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9
                           STATIC verification-13c304f54c664e8ca42e6103c4c66a9c
                           ACCEPTED / COMPLETE
PUBLISHED_MAIN_ANCHOR    = 847bb20a593e8092eabf80bd9d7c62ed7c21de08
BRANCH                   = phase/pre-g9b-r6-plus-f4-d-implementation (local, from 0c788526)

F4_DESIGN                = AUTHOR APPROVED (F4 author design closeout record)
F4-D IMPLEMENTATION      = AUTHORIZED (entry gate PASS, §2)
F4-D VERIFICATION_CLASS  = INTEGRATED_PHASE — FROZEN (§3)
F4-D TECHNICAL ACCEPTANCE= PENDING
F4-D AUTHOR SMOKE        = PENDING
F4-A IMPLEMENTATION      = NOT AUTHORIZED
PHASE G                  = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
```

This record transcribes the author instruction "PRE-G9B-R6-plus-F4-D IMPLEMENTATION
AUTHORIZATION" of 2026-10-13 and records the entry gate and the verification contract
it requires before product code. It is the first tracked change of the track. It
changes no decision of the
[F4 author design closeout record](pre_g9b_r6_plus_f4_author_design_closeout_record.md);
the canonical prompt
[`pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md)
is not amended and binds as approved. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-d-implementation-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-d-implementation-authorization.json).

## 1. Author authorization (essentials)

The author authorizes the F4-D implementation track only, on the previously approved F4
design, starting from the exact documentary closeout commit above without its prior
publication. Authorized: the minimal FreeHEP correction of
`AbstractCharTable.toName(char)` to `toName(Character.valueOf(c))`, with the approved
inherited-change boundary; test, verification-registry and evidence changes limited to
F4-D and explicitly justified. Not authorized: F4-A, phase G, publication, final author
approval of the technical candidate, a closeout, any FreeHEP refactoring, unrelated PDF
functionality, geometry-kernel changes or additional export semantics. The canonical
prompt's scope, test obligations, stop conditions and architectural restrictions remain
binding; any violation of the authorized scope or verification contract is a stop.

## 2. Entry gate (2026-10-13)

| # | Check | Result |
|---|---|---|
| 1 | HEAD, tree | `0c7885260761f442c25b380fcbbae11d1215fb51`, `e75fb9c6b55a1677380802766300f514323762d6` (exact) |
| 2 | linear ancestry from the published anchor | `847bb20a` → `089fc220` → `3251f085` → `83f07931` → `0c788526`; no merge commit |
| 3 | worktree | clean (no tracked or untracked change) |
| 4 | `main`, `origin/main`, live remote `main` | `847bb20a593e8092eabf80bd9d7c62ed7c21de08` (base intentionally local and ahead) |
| 5 | product delta in the documentary chain | none: the 16 paths changed by `847bb20a..0c788526` are documentation, prompts and validation mirrors only |
| 6 | `AbstractCharTable.java` | blob `22cb250d764ef32f5bad87420f3358c38b9d9d3d` identical at baseline `9b93256b` and at the base; `toName(char)` still reads `return toName(c);` (line 35) |
| 7 | reachability (reconciliation §3) | re-confirmed: `toName(char)` is called by `AbstractCharTable.toEncoding(char)`, called only by `FontUtilities.showString` (`:130`, `:138`), called only by `PDFGraphics2D.showCharacterCodes` (`:1113-1115`) from `writeString` (`:850`); production `PDFGraphics2D` instances only in `PictureExportService.writePDF` (`:177`) and `GraphicExportDialog.exportPDF` (`:1099`); no FreeHEP reference under `source/shared`, `source/web`, `tools` or `python`. Refinement: `Lookup.toName(char)` also delegates to a table's `toName(char)`, but nothing in the tree calls it |
| 8 | approved decisions for this track | `DQ-F4-5`, `-7`, `-9`, `-10` approved (design closeout record §1) |
| 9 | canonical prompt | present at the base, read in full; consistent with this authorization |
| 10 | governing files | `AGENTS.md`, canonical governance and verification prompts, the current contract of `verification-levels.md`, `verification-registry.json`, the JUnit inventory and the final-coverage pins read from disk |
| — | result | **PASS** |

## 3. Frozen verification contract

```text
VERIFICATION_CLASS   = INTEGRATED_PHASE — FROZEN (author instruction; executable registry,
                       not the archived verification-levels §12.8)
ACCEPTANCE           = on the same frozen clean candidate commit and tree, once each:
                       tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4-D
                         -LogDirectory artifacts\agent\<fresh>
                       tools\agent\verify.ps1 -Profile INTEGRATION
                         -LogDirectory artifacts\agent\<fresh>
                       tools\agent\verify.ps1 -Profile INFRA_UNIT
                       tools\agent\verify.ps1 -Profile STATIC
PHASE_ID             = PRE-G9B-R6-PLUS-F4-D
REQUIRED_CHECK_IDS   = compile.desktop.semantic
                       junit.desktop.pre-g9b-r6-plus-f4-d.semantic
                         (producer junit.desktop.pre-g9b-r6-plus-f4-d.producer,
                          selection pre-g9b-r6-plus-f4-d.desktop)
SELECTION            = the new focused class PreG9BR6PlusF4DEmbeddedFontPdfTest plus the
                       existing PostE2P1R2PhysicalPdfStyleTest,
                       PostE2P1R2BaseFingerprintTest, PreG9BR6PlusBPictureFidelityTest,
                       PreG9BR6PlusCBaseIdentityTest, PreG9BR6PlusCPhysicalExportTest
                       (canonical prompt obligation 9)
REGISTRATION         = phase entry, producer and projection nodes on the F2 shape; the
                       selection, the inventory (discovery and executed final.desktop
                       evidence) and the registry catalog pin through the official
                       updater; the final-coverage pins (selection count, F4-D identity
                       count, phase count) — the reason INFRA_UNIT is required
DEVELOPMENT CHECKS   = focused Gradle runs, Checkstyle reports, the upstream boundary
                       (Assert-GeoCeDGUpstreamBoundary), development INFRA_UNIT and
                       STATIC on the staged tree, git diff --check
NOT RUN              = FINAL (above the frozen class)
```

## 4. Obligations, evidence and normalization

- In-JVM, fail-never-skip tests (canonical prompt obligations 1–3, the text-extraction
  part of 4, 6–9), each obligation in its own test method; the delegation itself is
  pinned by a direct test of `toName(char)` against `toName(Character)` for every
  `char` of the PDFLatin, Symbol and ZapfDingbats tables.
- Base reproduction before the product change: the new tests run on the base product
  source (only test files added) must fail on the embedded-font routes with
  `StackOverflowError`; the base fingerprint fixtures are produced in the same run.
- External evidence (raster part of 4, and 5): Ghostscript and the Poppler/Xpdf tools
  installed on the workstation, each reported with tool, version, command, result and
  artifact, or `UNAVAILABLE` with its cause; never counted as a pass when missing.
- Normalization: only the exclusions the existing fingerprint tests already apply —
  PDF date strings `\(D:[0-9+\-Z']*\)` → `(D:)` and SVG clip identifiers
  `clip[0-9a-f-]+` → `clip`. Any further exclusion is a stop for author review.

## 5. Files of this record

This record, its JSON mirror, the roadmap (5.40) and the mini-track status. No product,
test, registry, specification or prompt change.
