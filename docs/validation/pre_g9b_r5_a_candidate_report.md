# PRE-G9B-R5-A — user-guide outline, anchors and navigation: candidate report

```text
TECHNICAL_CANDIDATE_STATE = PREPARED FOR ONE CLEAN IMMUTABLE COMMIT
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R5-A
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = BOUNDED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R5-A
ADDITIONAL_REQUIRED       = tools/agent/verify.ps1 -Profile STATIC (structural diagnostic)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval, and this file never needs to change when that
decision is made. The candidate commit cannot name itself, so its exact identity
and the post-commit acceptance evidence are reported outside it.

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R5-A` for implementation and technical
verification on 2026-09-30 and resolved the canonical prompt's open
implementation base. The prompt
`.github/prompts/tasks/pre-g9b-r5-a-guide-navigation.prompt.md` (blob
`fd68945b6b95ddd5b9ce3201c29d1fbb06668854`, read unchanged) keeps its historical
`PROPOSED` header; this report is where the authorization and exact base are
recorded, as the author instructed.

| Identity | Value |
|---|---|
| implementation base | `cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e` |
| base tree | `f34d350aa28e5324f184a4d1fb65ac55e0334c4b` |
| local `main` = `origin/main` = live remote `main` at entry | `cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e` |
| worktree at entry | clean |
| local branch (not pushed; not identity) | `feature/pre-g9b-r5-a-guide-navigation` |

Entry states re-read from the roadmap: `PRE-G9B-R4 = PASS — AUTHOR APPROVED`,
`PRE-G9B-R5-A = DESIGNED — NOT AUTHORIZED` before this instruction, and
`PRE-G9B-R5-B = DESIGNED — NOT AUTHORIZED`.

The verification class was frozen before any productive change. Section 12.8 of
`geocedg/specs/operations/verification-levels.md` still maps `BOUNDED_PHASE` to
PHASE alone, and the canonical prompt adds `STATIC` for the structural
diagnostic. No escalation was requested or run.

## 2. Source characterization

What the current source contains, as found at the base:

| Concern | Current source |
|---|---|
| renderer | `GeoCeDGGuideRenderer.toHtml`: a bounded, single-pass Markdown subset renderer; HTML comment lines, including the section markers, are dropped; headings become bare `<hN>` tags; `[text](target)` becomes an `<a href>` that the viewer never follows |
| window | `GeoCeDGGuideWindow`: one modeless, resizable `JDialog` whose content was a `JScrollPane` around a read-only `JEditorPane` with `HTMLEditorKit`; size contract 820 × 720 clamped to the screen |
| resource loading | `GeoCeDGActionRegistry.userGuideResource` / `readUserGuide` read `/org/geocedg/desktop/geocedg_user_guide_{en,es}.md`, with English as the fallback |
| packaging | `source/desktop/desktop/build.gradle.kts` `processResources` copies the two tracked Markdown files unchanged |
| renderer tests | `PostP1GuideRenderingTest` (17): literal `<h1>`, `<h2>`, `<h3>` presence, EN/ES tag-count parity, invisible markers, no `addHyperlinkListener` in the window source |
| packaging tests | `PostP1BilingualUserGuideTest` (16): 17 identical ordered markers and the byte identity of both packaged copies |
| host Swing pattern | `InputBarHelpPanelD` (Input Help) already combines a `JSplitPane` and a `JTree` with a hidden root and a `TreeSelectionListener` |
| documentation checks | the typed `DIAGNOSTIC_LEAF` / `DOCUMENTATION_DIAGNOSTIC` / `STRUCTURED_CONTRACT_V1` seam; no existing check of guide structure |

The canonical structural facts were revalidated mechanically on the base: each
edition has 17 markers, 1 level-1, 16 level-2 and 82 level-3 headings, none of
them inside a fence; every marker is immediately followed by an h1 or h2, every
h1 or h2 is immediately preceded by a marker, the first marker is on the h1, and
every h3 has a decimal prefix. Both editions have zero Markdown links and zero
nested-list lines. The latent renderer behaviour recorded by the prompt is
unchanged: an indented line after an open list item is still absorbed as a
continuation (`GeoCeDGGuideRenderer`, the `line.startsWith("  ")` branch). R5-A
does not touch it.

## 3. Frozen plan

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = BOUNDED_PHASE     (frozenAtPhaseStart = true)
ACCEPTANCE          = PHASE PRE-G9B-R5-A
ADDITIONAL REQUIRED = STATIC, with the new diagnostic.guide-structure leaf
PHASE selection     = compile.shared.semantic, compile.desktop.semantic,
                      junit.desktop.pre-g9b-r5-a.semantic,
                      infra.guide-structure-diagnostic
```

The two new checks stay outside the INTEGRATION, FINAL and INFRA_UNIT plans:
`diagnostic.guide-structure` is required for `STATIC` only, and
`infra.guide-structure-diagnostic` is reachable only through the R5-A PHASE
selection. The FINAL plan is therefore byte-identical to the base plan (section
13). Adding either check to those profiles would change the FINAL plan, a global
verification-infrastructure change outside `BOUNDED_PHASE`; it is left for an
author decision (section 15).

## 4. Documentation-maintenance amendment

New section 10 of
[`documentation-maintenance.md`](../../geocedg/specs/operations/documentation-maintenance.md),
"User-guide structure and derived navigation". It states that it becomes
normative only through the author's decision on the exact candidate. Its rules:

1. The exact marker form, and that every marker is immediately followed by an
   h1 or h2 and every h1 or h2 is immediately preceded by a marker; the first
   marker belongs to the title; there is no synthetic root identifier.
2. Navigation identity: an h1 or h2 takes its marker identifier; an h3 takes the
   enclosing marker identifier, `/` and its decimal numbering token; every h3
   begins with such a token; level 4 and deeper are not navigation entries.
3. Translated heading text is presentation only: never identity, never compared
   across editions, never slugified.
4. No manual table of contents, anchor markup, navigation-only list, separate
   outline resource or translated heading or slug catalog.
5. Identities are unique per edition, and the EN and ES
   `(heading level, numbering token, enclosing section id)` vectors are equal
   and machine-checked by structural extraction only; a violation is corrected
   in both guide sources, never by an index.

It closes by stating that navigation is presentation and never geometric,
identity, dependency-graph or serialization authority. It does not describe the
renderer implementation.

## 5. Anchor derivation

The anchor is computed inside the renderer's single parse
(`GeoCeDGGuideOutline.heading`):

```text
h1 / h2 : anchor = identifier of the marker on the immediately preceding line
h3      : anchor = <enclosing marker identifier> + "/" + <numbering token>
          numbering token = leading \d+(\.\d+)* of the heading text
h4..h6  : no anchor, no entry
```

`/` is the separator because neither a marker identifier (`[a-z0-9-]+`) nor a
numbering token can contain it, so every anchor decomposes uniquely into its two
structural parts. This matches the existing `spline-v2/main` form of the Spline
V2 branch keys. Examples: `about-this-guide` (title), `spline-v2` (§7),
`spline-v2/7.2`, `command-and-workflow-reference/16.4`.

Why translated text cannot be identity: the only inputs are the marker line and
the digits and periods at the start of the heading. A focal test renames every
heading of a fixture and gets identical anchors. On the real editions, 99 equal
anchors carry different EN and ES labels, for example `what-is-geocedg` =
"1. What is GeoCeDG" / "1. Qué es GeoCeDG".

A heading that breaks the rule gets no anchor and no entry, and a derived
duplicate never shadows the first occurrence. The viewer therefore never invents
identity. The focal tests and the diagnostic require every h1 to h3 of both
editions to have one.

### Representation

`<h2><a name="spline-v2">7. Spline V2</a></h2>`. The named anchor wraps the
heading text inside the heading. This choice rests on a scratch probe run with
the Desktop toolchain JDK 25.0.4, outside the tree:

| Candidate | Swing result |
|---|---|
| `<a name>` before `<h2>` | an implied paragraph, one extra line (+19 px) before every heading |
| empty `<a name></a>` inside `<h2>` | an extra line-break character inside the heading text |
| `id` on `<h2>` | breaks the existing literal `<h2>` assertions |
| **named anchor wrapping the text** | **pixel-identical to the plain heading** (same image hash); no added character; `href` absent, so no link styling |

Heading tags and counts stay literal and unchanged. A focal test proves that the
Swing document text is identical with and without the anchors.

## 6. Outline model

`GeoCeDGGuideRenderer.render(markdown)` returns
`Rendering(html, outline)` from one pass; `toHtml` is now `render(...).html()`,
so every existing caller and test is unchanged. Each
`GeoCeDGGuideOutline.Entry` holds `level`, `numbering`, `sectionId`, `anchor`,
`label` and `parentAnchor`. `label` is the reader text of the rendered heading,
with markup removed; it is the display text only. The parent is the nearest
preceding entry of lower level, referenced by its anchor. The outline is built
on each render and held only by the open window. It is never persisted,
serialized or packaged, and no outline resource exists.

## 7. Navigation UI

`GeoCeDGGuideWindow` now shows a horizontal `JSplitPane`, following the host
Input Help pattern: the tree on the left, 240 px by default (minimum 120 px,
never sized from the labels), and the unchanged document scroll pane on the
right. Resizing widens the document. The dialog keeps its 820 × 720 size
contract, minimum size, modality and close behaviour. `GeoCeDGGuideNavigator`
builds the `JTree` only from the outline, with a hidden Swing root that carries no
identity, the title as the single top node, the h2 sections under it, their h3
subsections under them, and every row expanded. Selecting a node, by mouse or
keyboard, scrolls the viewport so that the named anchor is at the top, as far as
the document height allows. Clicking the node that is already selected returns
to its heading. The positioning is programmatic (`modelToView2D` plus
`JViewport.setViewPosition`): no hyperlink listener, no `scrollToReference` and
no in-body table of contents exist. Reopening Help after a language change
reloads the same window with that edition's tree.

## 8. Scroll synchronization

```text
reference line = viewport top + REFERENCE_OFFSET (8 px, fixed, not content-derived)
current entry  = last entry whose anchor y <= reference line (inclusive);
                 the first entry when no anchor is at or above it
```

Anchor positions are recomputed from the live layout on every event, so window
resizing and re-wrapping stay correct. Offsets are resolved once per loaded
document.

Two guards prevent feedback loops:

1. A selection made by the synchronization never navigates. A `synchronizing`
   flag suppresses the tree listener.
2. A tree-driven navigation records the viewport position it set. Change events
   at that position keep the chosen entry. The first differing position, a real
   user scroll, returns control to the rule. An entry near the end therefore
   stays selected even though the document cannot bring its heading to the top.

The edge cases are tested. At the top, y = 0 and y = 1 select the title. At the
bottom the same rule applies. Each boundary is tested in both editions for more
than 80 entries: exactly at the reference line the next entry is selected, and
one pixel earlier the previous one. Repeated navigation to one entry yields one
position. An unknown anchor never scrolls. Tree-driven navigation causes exactly
one selection event.

## 9. Structural bilingual diagnostic

`diagnostic.guide-structure` is a `STATIC` `DIAGNOSTIC_LEAF` of class
`DOCUMENTATION_DIAGNOSTIC` running
`tools/agent/diagnostics/guide-structure-diagnostic.ps1`, with the pure extractor
`tools/agent/diagnostics/guide-structure.psm1`. It is fence-aware and extracts
structure only, reading no prose.

| Check | Finding rule |
|---|---|
| every marker immediately followed by an h1 or h2 | `MARKER_NOT_FOLLOWED_BY_H1_OR_H2` |
| every h1 or h2 immediately preceded by a marker | `H1_OR_H2_WITHOUT_MARKER` |
| a marker-like comment in the wrong form | `MALFORMED_MARKER` |
| every h3 has a numeric prefix | `H3_WITHOUT_NUMERIC_PREFIX` (and `H3_WITHOUT_ENCLOSING_SECTION`) |
| derived anchors unique per edition | `DUPLICATE_DERIVED_ANCHOR` |
| EN and ES `(level, numbering prefix, enclosing section id)` vectors equal element for element | `EN_ES_STRUCTURE_MISMATCH`, `EN_ES_ENTRY_COUNT_MISMATCH` |

Every finding names its rule and 1-based line. A mismatch names its vector index,
both lines and both vectors. The outcome is `DIAGNOSTIC_CLEAR` or
`DIAGNOSTIC_FINDING` (`DIAGNOSTIC_UNAVAILABLE` if a guide cannot be read), and
the exit status is always 0, as for every diagnostic. On the candidate both
editions are clear: 17 markers, 1/16/82 headings, 99 entries, 0 mismatches.

The negative cases live in `tools/agent/tests/guide-structure-diagnostic.Tests.ps1`
(`infra.guide-structure-diagnostic`, `VERIFICATION_CORE`, in the R5-A PHASE
selection): a missing marker, a misplaced and a malformed marker, an h3 without
a prefix, a duplicate anchor and duplicate marker, and EN/ES element,
entry-count and section-id mismatches. The suite exercises only the audit on
fixtures, so a documentation finding never becomes a verification-core
failure. The live guides are asserted by the diagnostic and by the product
JUnit selection. The Java runtime derivation and the PowerShell audit are two
independent implementations of section 10; neither is a second authority.

## 10. Compatibility

| Invariant | Evidence |
|---|---|
| authored guides byte-identical to the base | `git diff cfed55fad -- docs/user/geocedg_user_guide_en.md docs/user/geocedg_user_guide_es.md` is empty |
| packaged copies byte-identical to the sources | the unchanged `bothGuidesArePackagedAsByteCopiesOfTheirTrackedSources`; `build.gradle.kts` unchanged |
| existing renderer tests unchanged and passing | `PostP1GuideRenderingTest` 17/17, `PostP1BilingualUserGuideTest` 16/16, sources not edited |
| no hyperlink listener | no guide class contains `HyperlinkListener` or `scrollToReference`; the view has 0 hyperlink listeners at runtime |
| no manual TOC and no visible navigation markup | guide sources unchanged; no packaged outline resource; reader text identical with and without anchors |
| no R5-B work | no command name, alias, command table, GGBScript, serialization or `AD-R0-4` change |
| no kernel change | no file under `source/shared` changed |

## 11. Changed paths

| Layer | Paths |
|---|---|
| normative documentation contract | `geocedg/specs/operations/documentation-maintenance.md` (section 10) |
| Desktop GeoCeDG | `GeoCeDGGuideRenderer.java`, `GeoCeDGGuideWindow.java`, `GeoCeDGActionRegistry.java`; new `GeoCeDGGuideOutline.java`, `GeoCeDGGuideNavigator.java` |
| focal tests (new) | `PreG9BR5AGuideOutlineTest.java`, `PreG9BR5AGuideNavigationTest.java` |
| structural diagnostic (new) | `tools/agent/diagnostics/guide-structure.psm1`, `tools/agent/diagnostics/guide-structure-diagnostic.ps1`, `tools/agent/tests/guide-structure-diagnostic.Tests.ps1` |
| verification registry and catalog | `geocedg/specs/operations/verification-registry.json`, `verification-junit-inventory.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1` |
| upstream record | `docs/upstream/modified-files.yml` |
| status | `docs/roadmap/geocedg_roadmap.md`, this report, `geocedg/validation/pre-g9b-r5-a/pre-g9b-r5-a-candidate-evidence.json` |

The Desktop classes live under `source/desktop/desktop/src/main/java/org/geocedg/desktop/`
and the tests under the matching `src/test/java` path. No existing test file was
edited. The user guides, the guide index, `build.gradle.kts`, the prompts,
`AGENTS.md` and `CLAUDE.md` are unchanged.

## 12. Verification registration

| Registry entry | Kind | Profiles |
|---|---|---|
| phase selection `PRE-G9B-R5-A` | `compile.shared.semantic`, `compile.desktop.semantic`, `junit.desktop.pre-g9b-r5-a.semantic`, `infra.guide-structure-diagnostic` | PHASE |
| `junit.desktop.pre-g9b-r5-a.producer` / `.semantic` | process producer / `SEMANTIC` `PRODUCT` JUnit projection | PHASE only |
| `infra.guide-structure-diagnostic` | `VERIFICATION_CORE` acceptance leaf | PHASE only |
| `diagnostic.guide-structure` | `DOCUMENTATION_DIAGNOSTIC` leaf | `STATIC` |

`pre-g9b-r5-a.desktop` selects `PreG9BR5AGuideOutlineTest`,
`PreG9BR5AGuideNavigationTest`, `PostP1GuideRenderingTest`,
`PostP1BilingualUserGuideTest` and `G9U1ActionRegistryTest`: 79 identities.

The inventory was regenerated with the official updater from real producer
evidence: `--test-dry-run` discovery for both modules plus completed, passing
executed runs of `pre-g9b-r5-a.desktop` and `final.desktop`. Nothing was edited
by hand except the new selection's contract fields, and the updater's own
serializer reproduced the tracked file byte for byte before that insertion.

| Selection | Before | After |
|---|---|---|
| `discovery.shared` | 5 925 | 5 925 (identical hash) |
| `discovery.desktop` | 1 566 | 1 598 |
| `final.shared` | 6 842 | unchanged; no shared test identity changed |
| `final.desktop` | 1 560 | 1 592 (1 591 passed, 1 allowlisted disabled case) |
| `pre-g9b-r5-a.desktop` | — | 79 |

The `junit_inventory` pin was repinned after reproducing the previous pin exactly
from the base blob with `Get-VerificationCanonicalTextSha256`:
`5980e985…` → `5da8c284…`. The `static_contracts` pin is unchanged.
`verification-final-coverage.Tests.ps1` now records 30 selections, 41 PHASE
selections and the R5-A selection count.

## 13. Pre-candidate evidence (development diagnostics, not acceptance)

All of the following ran on the uncommitted working tree. They are
`DEVELOPMENT_DIAGNOSTIC` evidence for correctness before freezing, never
acceptance or closeout evidence.

| Check | Result |
|---|---|
| scratch Swing probes (JDK 25.0.4, outside the tree) | selected the anchor representation of section 5 |
| focal classes `PreG9BR5AGuideOutlineTest` / `PreG9BR5AGuideNavigationTest` | 20/20 and 12/12 passed, the display-guarded window case included (not skipped) |
| `PostP1GuideRenderingTest` / `PostP1BilingualUserGuideTest` (unchanged) | 17/17 and 16/16 passed, including the packaged-copy byte identity |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 violations, after one renderer local was made `final` |
| `guide-structure-diagnostic.ps1` on the live guides | `DIAGNOSTIC_CLEAR`: 17 markers, 1/16/82 headings, 99 entries per edition, 0 mismatches |
| `guide-structure-diagnostic.Tests.ps1` | 7 cases, 19 assertions, 0 failures |
| `verification-final-coverage.Tests.ps1`, `verification-registry.Tests.ps1` | 8 cases / 326 assertions and 22 cases / 62 assertions, 0 failures |
| producer `pre-g9b-r5-a.desktop` (executed) | 79/79 passed |
| producer `final.desktop` (executed, full Desktop suite) | 1 592 identities: 1 591 passed, 1 allowlisted disabled case; inner exit 0 |
| producers `discovery.shared` / `discovery.desktop` (`--test-dry-run`) | 5 925 / 1 598 identities |
| plan identities against an archive of the base tree (`-PlanOnly`) | `FINAL` 68 nodes `3d8752b9…`, `INTEGRATION` 46 nodes `4502c4bd…` and `INFRA_UNIT` 22 nodes `830887b1…` byte-identical to the base; `STATIC` 9 → 10 nodes (`7d52b28a…` → `25dfacc8…`); `PHASE -Phase PRE-G9B-R5-A` 7 nodes, `COMPLETE`, `31a5d90f…` |
| `verify.ps1 -Profile INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-72300c2bf7334f2abb1e8d0c1b4789bf`) |
| `verify.ps1 -Profile STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3; `diagnostic.guide-structure` `DIAGNOSTIC_CLEAR`; only the standing governance finding and historical-consistency unavailability (`verification-5a114531b90f40a0ab70546399ea032c`) |
| `Assert-GeoCeDGUpstreamBoundary` against baseline `9b93256b…` | OK, 821 registered files |
| `git diff --cached --check`, default and `cr-at-eol` policy | clean |
| authored guides against the base | no difference |

The frozen acceptance, `tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R5-A`,
and the additional `STATIC` run execute once each on the exact clean committed
candidate. Their results are reported outside this artifact.

## 14. Impact statements

```text
PRODUCT_PHASE_EFFECT                = DESKTOP HELP PRESENTATION (derived outline, anchors,
                                      navigation tree, scroll synchronization)
KERNEL / COMMAND / GEOMETRY         = NONE
SERIALIZATION / DURABLE IDENTITY    = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT  = MODIFIED — one new PHASE selection with its JUnit node
                                      pair and inventory selection, one PHASE-only verification-core
                                      suite, one STATIC-only documentation diagnostic, regenerated
                                      inventory counts, one repinned catalog hash and updated counts
                                      in the final-coverage suite; no verifier, supervisor, projection,
                                      schema or policy logic changed; INTEGRATION, FINAL and INFRA_UNIT
                                      plans unchanged
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda, packaging,
download or build/verification entry point changed; the new checks reuse the existing
PWSH process, structured-contract and JUnit-projection machinery.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = R5-A changes the navigation presentation around the existing
guide; the authored guide content is itself the navigated authority and is explicitly
frozen by the phase contract, so both editions stay byte-identical. The anchor and
alignment rule for future guide edits is carried by the normative
documentation-maintenance contract (section 10); no developer guide describes the
guide renderer or window.
```

## 15. Retained debt and observations

The entry overlap check used the latest ledger, the `PRE-G9B-R4` closeout record.
Only `GUIDE-A1-SECTION-9.3-STALE` touches the user guide, and it is a
guide-content correction that R5-A explicitly forbids. No ledger item is
absorbed; all carry forward unchanged.

| Identifier | State |
|---|---|
| `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL` | new observation: `diagnostic.guide-structure` runs in `STATIC` only and its fixture suite in the R5-A PHASE only, so the frozen `BOUNDED_PHASE` class leaves the FINAL plan unchanged; adding them to INTEGRATION/FINAL/INFRA_UNIT is a global verification-infrastructure decision for the author |
| `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION` | unchanged pre-existing renderer behaviour recorded by the prompt: an indented list line after an open item is absorbed as continuation text; both guides contain zero nested lists, so it stays latent; not touched |

## 16. Author smoke checklist

Technical acceptance does not replace author smoke. Suggested steps:

1. Start GeoCeDG in English and open **Help → GeoCeDG user guide**.
2. Check that the left tree shows the guide title with the 16 numbered sections
   under it and their numbered subsections under each, in document order.
3. Click several top-level entries (for example 6, 11 and 16) and several nested
   ones (for example 7.2, 9.3 and 11.4), and check that the document jumps to
   exactly that heading at the top of the reading area.
4. Scroll manually with the wheel and the scroll bar through several sections,
   and check that the tree follows the current section without oscillation or
   unexpected jumps.
5. Scroll slowly across several section boundaries, and click an entry near the
   end of the guide (for example 16.4), which cannot reach the top; it must stay
   selected until you scroll.
6. Switch the product language to Spanish, reopen the guide and repeat steps 2–5.
7. Compare the two trees: the same shape and numbering, with Spanish labels.
8. Check that the guide body shows no table of contents, anchor text, marker
   comment or other visible navigation markup.
9. Check that headings, tables, code blocks and lists still look as before.
10. Check that other help, including Input Help, contextual help and the command
    list, behaves as before and that no link opens a browser.

## 17. Authorization state

`PRE-G9B-R5-A` stops with this technical candidate for author review and the
required author smoke. `R5-B`, `R6`, `R7`, `G9B`, `G9C`, `G9U2`, productive
`G10` and further `G12` remain unauthorized. Push, branch publication, merge,
promotion, tag, release and binary publication are outside this phase; each
requires a separate explicit author instruction naming the exact candidate SHA.
