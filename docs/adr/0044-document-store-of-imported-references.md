# ADR 0044 — Document store of imported references outside `Construction`

- Status: **PROPOSED — CANDIDATE PENDING AUTHOR REVIEW** (written by `PRE-G9B-MAINT-A-N2`
  on 2026-11-25). `selfApproved=false`. Acceptance of this ADR would not authorize
  `MAINT-A-DXF0-RC-S0` or `MAINT-A-DXF0-RC`; each needs its own authorization on an exact
  base.
- Date: 2026-11-25.
- Authorization of the design work: author instruction «GeoCeDG — PRE-G9B-MAINT-A-N2.
  Diseño normativo coordinado de persistencia R-c, exportación DXF de referencias y
  aproximación A-DXF1» (2026-11-25); execution contract
  [`pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md).
- Base: `a9ee803bf4be92c58ab7e120111ebacc70055487`.
- Candidate contract: [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md).
- Related: ADR 0016 (native document), ADR 0043 (decision 10), AGENTS §4, §12.6.

## Context

`dxf-spline-reference/v1` (approved 2026-11-24) keeps imported `SPLINE` references outside
`Construction` and the DAG, session-only (`R-a`), and names `R-c` — a copy of the imported
definitions in the document, outside `Construction`, without a parallel DAG — as the later
priority target, subject to a characterization (§12). ADR 0016 and
`native-document-identity.md` keep the existing archive layout and require a separate
compatibility decision for any new archive content; the CU4 amendment admitted one optional
entry (`geocedg_defaults_v2.xml`) under that rule.

The author decided on 2026-11-25 (D-A-N2-1, D-A-N2-2) that references, constructions and
renders stay distinct; that new documents with `R-c` need not be readable by older GeoCeDG
versions, including 1.2.2; and that the implementing version must open, save and reopen them
without silent loss, read old documents, reject unknown versions and malformed data safely,
and restore the previous document when opening fails.

The characterization of `PRE-G9B-MAINT-A-N2` (report §2) established: an unknown archive
entry is ignored by the current reader and by 1.2.2 and dropped on re-save; an unknown child
of `<geogebra>` makes older readers interpret its children as top-level elements; Undo
stores only `geogebra.xml`; the native open captures and restores the previous archive and
commits the host state only after success; the `R-a` store has no identifier and does not
mark the document modified.

## Decision

1. **A document-owned store, not construction data.** Imported references are persisted in
   one optional ZIP entry `geocedg_references.xml` of the native document, with contract
   `cedg-reference-store/v1` inside its content. They are never `GeoElement`, never in
   `Construction` or its XML, never in Undo/Redo, never DAG participants, never spatial
   identities.
2. **Fixed entry name, version inside.** A future version reaches a `v1` reader under the
   same name and is rejected with a typed cause; unknown root entries `geocedg_*` are
   rejected by the reader that introduces `R-c`.
3. **The stored copy is the authority of a reference.** The admitted binary64 source data,
   the transformed data, the explicit transformation and the provenance are stored exactly
   (bit patterns); nature, effective weights, domain, closure, continuity and tessellation are
   recomputed; transformation and digests are verified bit for bit on reading.
4. **Own document identity.** Each reference has a random 128-bit identifier minted at
   admission, unique in its store, independent of `ceID`, labels, coordinates, handles,
   digests and XML positions; it is not CeDG geometric identity.
5. **Prepare and commit.** A store read from a file is installed only at the commit of the
   document transition; a failed open leaves the live document and its store unchanged.
6. **Explicit incompatibility with older readers.** Documents with a store are declared
   unsupported by GeoCeDG ≤ 1.2.2, which ignore the store and lose it on re-save; release
   notes say so. No forced-rejection marker is added to `geogebra.xml`.
7. **Store state is document state outside Undo.** Store operations mark the document
   modified and are covered by the save prompt; Undo/Redo never touch them.

## Consequences

- `MyXMLioJre` (upstream, already modified by CU4) gains a reader branch and a writer step;
  `MyXMLHandler`, `getFullXML`, `doParseXML` and Undo are unchanged.
- `native-document-identity.md` needs an amendment of the CU4 kind (candidate A-NDI-1).
- `dxf-spline-reference.md` §2, §4.1, §10, §11 and §12 need amendments (candidates A-IMP-1
  to A-IMP-3, A-IMP-9, A-IMP-10); ADR 0043 decision 10 gets a status pointer (A-ADR-3).
- The store capacity (4 MiB: about 39 maximal or 1 300 typical references) is stricter than
  the approved import limits, which keep governing the reading of a DXF file.
- GeoCeDG Classic, which already drops the CU4 entry and hidden layers, needs decision RC-D4.
- The expert and angle-mode scanners must register the entry; the store is capped at 4 MiB.

## Alternatives

| Alternative | Reason for rejection |
|---|---|
| Child element of `<geogebra>` in `geogebra.xml` | older readers interpret its children as top-level elements; it inflates the construction parse; travels through XML routes that Undo and clipboard use |
| References inside `Construction` (`R-d`) | not authorized; needs a kernel type, identity and dependency contract |
| Versioned entry name | a newer version would be silently ignored by a `v1` reader |
| Forced rejection marker for older readers | invades the construction XML for a compatibility the author waived |
| Keep `R-a` only | references lost at every reopen; contrary to the approved priority target |

## Status boundary

This ADR stays **proposed** until the author accepts it explicitly.
