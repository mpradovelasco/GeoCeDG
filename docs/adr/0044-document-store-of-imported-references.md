# ADR 0044 — Document store of imported references outside `Construction`

- Status: **PROPOSED — READY FOR AUTHOR ADOPTION (revision R3)** (written by
  `PRE-G9B-MAINT-A-N2` on 2026-11-25, refined by `PRE-G9B-MAINT-A-N2-R1` and
  `PRE-G9B-MAINT-A-N2-R3`).
  `selfApproved=false`. Acceptance of this ADR would not authorize `MAINT-A-DXF0-RC-S0` or
  `MAINT-A-DXF0-RC`; each needs its own authorization on an exact base.
- R3 changes: decision 8 records the directly measured budget and the streaming reader with
  per-attribute bounds (pre-adoption audit M1).
- R1 changes: decision 8 (resource policy instead of a fixed 4 MiB cap, D-R1-4); decision 3
  makes source and transformed data mandatory (D-R1-3); decision 5 covers every replacement
  route; decision 9 (Classic, D-R1-8); no derivation history (D-R1-5).
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
   (bit patterns); both source and transformed data are mandatory and the transformed data are
   never replaced by a recomputation (D-R1-3); nature, effective weights, domain, closure,
   continuity and tessellation are recomputed; transformation and digests are verified bit for
   bit on reading. No history of conversions, approximations or failed attempts is stored
   (D-R1-5).
4. **Own document identity.** Each reference has a random 128-bit identifier minted at
   admission, unique in its store, independent of `ceID`, labels, coordinates, handles,
   digests and XML positions; it is not CeDG geometric identity.
5. **Prepare and commit in every route.** A store read from a file is validated while reading
   and installed by a non-throwing swap at the commit of the document transition, together
   with the construction, never in reloads that are not document changes. Routes that today
   replace the document without capture (embedded base64, `setBase64`, archive bytes by URL,
   API clearing `setXML`) are sent through the native transaction. A failure leaves the live
   document and its store unchanged or fully restored.
6. **Explicit incompatibility with older readers.** Documents with a store are declared
   unsupported by GeoCeDG ≤ 1.2.2, which ignore the store and lose it on re-save; release
   notes say so. No forced-rejection marker is added to `geogebra.xml`.
7. **Store state is document state outside Undo.** Store operations mark the document
   modified and are covered by the save prompt; Undo/Redo never touch them.
8. **Resource policy, not a fixed capacity.** Geometric admission, per-operation resources,
   always-on reader safety (streaming reading with a byte counter, single entry, strict parser,
   declared counts checked before allocation, every attribute bounded while it is read) and a
   configurable storage budget are distinct. The budget's initial values come from a direct
   measurement of the full Open → Save → Reopen cycle on a 1 GiB heap (10 000 references,
   1 500 000 values, entry bytes `17 × maxValues + 1 KiB × maxReferences`; the R1 value of
   3 000 000 was measured and not confirmed; the measured corpora reach 77 % and 91 % of
   `maxEntryBytes`) and are measured again by C-4, a closeout gate of `MAINT-A-DXF0-RC` that
   also covers the unmeasured case at 100 % of the string allowance; they are raised only by an
   explicit administrative setting with finite values; an over-budget document is rejected with
   a typed cause and never truncated (D-R1-4). The passive scanners recognize the entry under the
   same policy instead of imposing their 4 MiB constant.
9. **GeoCeDG Classic never loses the store silently.** It preserves the entry opaquely on every
   save route; if that cannot be guaranteed, it refuses every save route of such a document
   with a message (D-R1-8).

## Consequences

- `MyXMLioJre` (upstream, already modified by CU4) gains a reader branch with bounded
  inflation, a writer step and Classic's opaque preservation; `AppD` gains one GeoCeDG-owned
  entry for the transactional archive-bytes routes; `MyXMLHandler`, `getFullXML`,
  `doParseXML` and Undo are unchanged.
- `native-document-identity.md` needs an amendment of the CU4 kind (candidate A-NDI-1).
- `dxf-spline-reference.md` §2, §4.1, §10, §11 and §12 need amendments (candidates A-IMP-1
  to A-IMP-3, A-IMP-9, A-IMP-10); ADR 0043 decision 10 gets a status pointer (A-ADR-3).
- The initial budget never bounds the number of references below the importer's; it bounds
  memory: on the measured workstation a full Open → Save → Reopen cycle needs at most about
  256 MiB, one quarter of a 1 GiB heap, for the measured corpora at 100 % of `maxValues` and
  77 % and 91 % of `maxEntryBytes`; the worst admissible case (the whole string allowance used)
  was not measured and is a closeout gate of `MAINT-A-DXF0-RC` (C-4); the importer's extreme case (10 000 maximal
  references) needs an explicitly raised budget and more memory.
- The expert-inspection and angle-mode scanners, which today refuse any store, recognize it.

## Alternatives

| Alternative | Reason for rejection |
|---|---|
| Child element of `<geogebra>` in `geogebra.xml` | older readers interpret its children as top-level elements; it inflates the construction parse; travels through XML routes that Undo and clipboard use |
| References inside `Construction` (`R-d`) | not authorized; needs a kernel type, identity and dependency contract |
| Versioned entry name | a newer version would be silently ignored by a `v1` reader |
| Forced rejection marker for older readers | invades the construction XML for a compatibility the author waived |
| Keep `R-a` only | references lost at every reopen; contrary to the approved priority target |
| Fixed 4 MiB cap (A-N2 draft) | derived from two passive scanners, not from the product; rejected by D-R1-4 |
| Store only `source` and recompute `data` | loses the arithmetic-drift check; rejected by D-R1-3 |
| Base64 or a binary entry | measured gain after deflation of 7 to 17 % only; hex kept for simplicity |

## Status boundary

This ADR stays **proposed** until the author accepts it explicitly.
