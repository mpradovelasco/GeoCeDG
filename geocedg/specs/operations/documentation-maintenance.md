# GeoCeDG documentation maintenance contract

- Status: **NORMATIVE / AUTHOR APPROVED**
- Scope: living documentation, historical evidence, links and capability claims
- Accepted decision: `docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md`
- Product effect: none

## 1. Purpose

GeoCeDG documentation must let a reader distinguish current observable product
behavior, productive but internal capability, accepted technical authority,
proposed design, and immutable historical evidence. Documentation must not
promote an internal Java API into a user feature or describe a proposed G9
design as implemented.

This normative contract governs maintenance procedure only. Existing accepted
specifications and ADRs remain authoritative for geometric meaning.

## 2. Documentation classes

| Class | Location | Authority and maintenance rule |
|---|---|---|
| repository contract | `AGENTS.md` | root agent authority; change only by explicit task |
| normative specification | `geocedg/specs/` | geometric or operational contract after approval |
| ADR | `docs/adr/` | decision, alternatives and consequences; status is explicit |
| architecture | `docs/architecture/` | current implementation map or clearly marked proposal |
| developer/API guide | `docs/developer/` | current source-facing usage; never a second semantic specification |
| user guide | `docs/user/geocedg_user_guide_en.md` and `docs/user/geocedg_user_guide_es.md` | official bilingual current-state entry points with shared stable section identifiers; `docs/user/geocedg_user_guide.md` remains their index |
| operations manual | `docs/developer/geocedg_operations_manual.md` | workstation, build, packaging and installation procedure |
| mathematical reference | `docs/user/geocedg_mathematical_reference.md` | explanatory mathematics linked to normative specs |
| roadmap | `docs/roadmap/` | phase sequencing and gates, not proof of implementation |
| canonical prompt | `.github/prompts/` | executable task/review scope; references durable contracts |
| validation report | `docs/validation/` | historical execution evidence or current traceability |
| compact evidence | `geocedg/validation/` | versioned machine-readable evidence and policies |

## 3. Claim vocabulary

Every capability summary must use one of these meanings:

- **observable**: a supported user workflow exists and its saved-file behavior
  has been validated;
- **productive internal**: implementation exists, but no supported command,
  tool, persistence or ordinary user workflow is claimed;
- **accepted/normative**: author-approved contract; implementation status is a
  separate statement;
- **proposed / not normative**: design candidate awaiting author review;
- **historical**: true at a named commit or tag and not a claim about current
  `HEAD`.

Statements about G6-G8 internal Locus V2 facilities must say that public
commands, `Path`, copy/persistence and normal user workflows remain absent.
Normative design must state its implementation authorization separately.

Every future-phase roadmap row and implementation prompt must distinguish:

- hard semantic or contract dependencies;
- recommended execution predecessors; and
- global/release closeout gates.

A recommended operational order must not be restated as a kernel dependency,
and a frontend client must not become a prerequisite of the semantics it
consumes merely because it was scheduled first.

## 4. Living documents and frozen evidence

Living guides are corrected at current `HEAD`. Historical phase evidence is not
rewritten to match later phases. The fixed G8 closeout anchor is:

- annotated tag object: `fed1bfbeea77a48acce285429b397eda77054df1`;
- peeled commit: `e7810171179825a03b22d8c6eba28c672f468281`;
- tag name: `geocedg-g8-pass`.

Historical hash manifests and every file named by them are verified from those
tag blobs. Current living documents are checked separately for current status,
links and public/internal boundaries. A later checkout may reproduce G8
evidence only when `HEAD` descends from the peeled commit.

## 5. Documentation impact

Every implementation, behavior, command, packaging or operational task must
record exactly one of:

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = <repository-relative paths>
```

or:

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = <why no observable or developer-facing contract changed>
```

An internal semantic change normally affects an architecture/API guide even if
the user guide impact is `NONE`. A public command, tool, persistence behavior,
menu, installation path or exported format normally requires a user-guide
update.

## 6. Mathematics ownership

Normative definitions, validity domains, tolerances, degeneracies and failure
states remain in feature specifications. The mathematical reference may derive
  and illustrate them, but must link to the governing spec and label future or
  unimplemented spatial material. Copying a formula into a guide does not create a new
authority.

## 7. Maintenance workflow

1. Classify the change and identify its source-of-truth artifact.
2. Search living documents for the affected capability and prior phase-status
   language.
3. Update the smallest coherent set; retain historical wording only with an
   explicit commit/tag qualifier.
4. Update the capability traceability matrix.
5. When phase relationships change, update all three dependency classes in the
   roadmap, phase plan, affected prompts and machine evidence.
6. Resolve repository-relative Markdown links and validate JSON documents.
7. Run the focused verifier named by the governing task.
8. Record `GUIDE_IMPACT`, validation evidence and unresolved documentation debt.

Do not repair a historical evidence hash by editing its manifest. Correct the
living document and verify the historical blob through the frozen tag.

## 8. Sustainable automated checks

Future documentation validation should check:

- broken repository-relative links;
- missing status labels on proposed specifications and ADRs;
- references to nonexistent commands or tools;
- known contradictory phase phrases in living documents;
- schema-valid operational profiles;
- existence of every traceability target;
- fixed-tag type, target and `HEAD` ancestry;
- canonical UTF-8/LF hashes for historical evidence.
- dependency-graph acyclicity and the absence of forbidden kernel-to-frontend
  prerequisites.

Automated prose interpretation is advisory. It must not infer geometric truth
or rewrite documents.

## 9. Acceptance gate

The author accepted the claim vocabulary, historical-evidence boundary and
`GUIDE_IMPACT` protocol at G9P closeout. This document is normative; it does not
by itself authorize productive geometric work. The G9O1 operational
implementation is **PASS — AUTHOR APPROVED**. G9A1 is separately authorized and
not started; every later productive G9 phase remains unauthorized.

## 10. User-guide structure and derived navigation

Added by `PRE-G9B-R5-A`. This section is part of that phase's technical
candidate and becomes normative only through the author's decision on the exact
candidate commit; until then the sections above keep their approved status
unchanged.

The two user-guide editions carry their own navigation structure. The Markdown
source of each edition is the single authored source of both its content and its
structure; the packaged copies stay byte copies of those sources. Every outline,
anchor and navigation tree is derived from that source at presentation time.

1. **Stable section markers.** A section marker is a line
   `<!-- geocedg-guide-section: <id> -->` with `<id>` matching `[a-z0-9-]+`.
   Every marker is immediately followed by a level-1 or level-2 heading, and
   every level-1 or level-2 heading is immediately preceded by a marker. The
   first marker belongs to the level-1 title; no synthetic root identifier
   exists.
2. **Navigation identity.** The navigation identity of a level-1 or level-2
   heading is the identifier of its marker. The identity of a level-3 heading is
   the identifier of its enclosing marker, a `/`, and the heading's decimal
   numbering token, for example `spline-v2/7.2`. Every level-3 heading begins
   with such a token. Headings of level 4 or deeper are not navigation entries.
3. **Translated text is presentation only.** Heading text is the localized
   display label of a navigation entry. It is never an identity, is never
   compared across editions and is never slugified.
4. **No manual navigation authority.** No manual table of contents, anchor
   markup, navigation-only list, separate outline resource or catalog of
   translated headings or slugs is authored or maintained.
5. **Machine-checked alignment.** Navigation identities are unique within each
   edition, and the ordered vectors of `(heading level, numbering token,
   enclosing section id)` of the English and Spanish editions are equal element
   for element. The check extracts structure only and never interprets prose.
   A guide edit that breaks these rules is corrected in the guide sources of both
   editions, never by an index or a special case.

Navigation is presentation. It is never geometric, identity, dependency-graph or
serialization authority, and kernel code never consults it.
