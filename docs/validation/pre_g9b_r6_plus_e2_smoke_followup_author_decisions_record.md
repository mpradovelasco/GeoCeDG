# PRE-G9B-R6-plus-E2 — author smoke follow-up: author decisions record

```text
RECORD_KIND            = AUTHOR DECISIONS (verbatim intent, transcribed by the agent)
DATE                   = 2026-10-08
PHASE                  = PRE-G9B-R6-plus-E2 (author smoke follow-up A/B)
ORIGINAL CANDIDATE     = T_R6PLUS_E2 878ff66b8839aeee44dfe1e557ac37b7c349f74b
                         tree e096b857e1ce7e1e959c0c637f3e8b3b87cd82fe (unchanged,
                         historical evidence with its PHASE and INTEGRATION runs)
AUTHOR_SMOKE           = PASS WITH OBSERVATIONS — ORIGINAL E2
selfApproved           = false
```

This record transcribes the author's instructions of 2026-10-08 (the authorized
follow-up prompt, and the author's answer after the part A characterization). It
creates no approval of the original or of a revised candidate; a later author
decision naming an exact commit is the sole authority for approval.

## 1. Author smoke of the original E2 candidate

`AUTHOR_SMOKE = PASS WITH OBSERVATIONS`. No defect in the principal dimension
construction behavior: aligned and linear dimensions, placement and interaction,
LaTeX exports with dimension lines and numeric texts are satisfactory; forward
incompatibility with older readers is an accepted limitation. Observations: the
initial thickness of the three dimension segments, the automatic unit suffix, and
the apparent absence of the macros embedded in `Templatev7.ggb`.

## 2. Part A — legacy document macros

Accepted characterization: the 24 original `Templatev7.ggb` macros are loaded,
registered, executable and survive document round trips; the behavior is identical
on the published base and the E2 candidate; the GeoCeDG toolbar intentionally does
not reconstruct the legacy document toolbar; not an E2 regression.

```text
OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS = A-INTENTIONAL-PROFILE-BEHAVIOR
OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX  = OPEN (separate UX/documentation observation:
                                             discovery and interactive selection of
                                             embedded macros)
```

Authorized: correcting any demonstrable user-guide discrepancy within documentary
scope. Not authorized: changing macro registration, command resolution, the toolbar
policy or the kernel. Exposing the original macros as interactive tools requires a
separate authorization.

## 3. Part B1 — dimension line thickness (authorized)

- Default GeoGebra `lineThickness = 2` for the dimension line and both extension
  lines.
- Configurable initial thickness, persisted in the established application
  preferences infrastructure; existing dimensions are unaffected by preference
  changes; each created segment keeps its own serialized style.
- No new geometric semantics or command arguments.

## 4. Part B2 — unit suffix presentation (bounded serialization extension authorized)

The smallest correct versioned extension of the existing `geocedgUnits`
document-state contract to persist a Boolean unit-suffix presentation policy:

- extend `geocedgUnits` (version 2 if the version-1 contract requires it), not the
  dimension geometry or `GeoText`; preserve all unit semantics and conversions;
- default "show unit suffix = false" for new documents; the user can enable or
  disable it for the current document; the selection persists across machines; an
  application preference establishes the initial policy of new documents only;
- a policy change updates existing native dimension texts without recomputing
  geometry; the numeric output stays in model units;
- documents without an explicit policy (existing E2 documents) keep the historical
  suffix-visible behavior; legacy documents and version-1 metadata are preserved
  without silent migration or destructive change;
- a new document keeps its policy after save/reopen, also with unspecified units;
  undo/redo, rollback, redefine and macro-loading invariants are preserved; picture
  and supported LaTeX exports show the same text;
- amend `unit-system.md` §14.2, its persistence clauses and the associated
  normative decision records explicitly, preserving the original approved decision
  as historical evidence.

Not authorized: a new `GeoElement`, a new XML element family, a change to native
dimension command signatures, a global `GeoText` modification, a geometric-unit
dependency. A broader architectural or serialization change stops for author review.

## 5. Verification and boundaries

B1 and B2 together in one revised E2 technical candidate; the original candidate
and its verification evidence are preserved; focused, serialization and
compatibility tests, `final.shared`, `final.desktop` (512 MB test heap) and the
official inventory producers; the revised candidate is frozen and `PHASE` and
`INTEGRATION` run on exactly the same commit and tree; it is reported for a focused
author re-smoke of B1/B2. No publication, merge, tag, release or promotion to
`PASS — AUTHOR APPROVED`.

## 6. Agent interpretation recorded for author review

The author's answer authorized B1 "as designed" without answering the sub-question
on typed commands. The agent applies the recommended design: the dimension
algorithm initializes its three segments to thickness 2 (as it already sets the arrow
endings), and the two tools apply the user's creation preference on top.
