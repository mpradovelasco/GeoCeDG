# Document store of imported references (`R-c`) — `cedg-reference-store/v1`

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR REVIEW.** Normative design written by `PRE-G9B-MAINT-A-N2`. `selfApproved=false`. It authorizes no code: `MAINT-A-DXF0-RC-S0` and `MAINT-A-DXF0-RC` are not authorized |
| Contract identifier | `cedg-reference-store/v1` (the store); its records stay `dxf-spline-reference/v1` |
| Container | one optional ZIP entry of the native document, fixed name `geocedg_references.xml` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| Level | `R-c` of [`dxf-spline-reference.md`](dxf-spline-reference.md) §12 (which this contract answers point by point, §17) |
| Architectural layer | Document/application model outside the geometric kernel (AGENTS §4: «import/export adapter», «document/application model»); a pure codec in the importer package |
| Decision | [ADR 0044](../../../docs/adr/0044-document-store-of-imported-references.md) (proposed) |
| Companion candidates | [reference DXF export](../export/dxf-reference-export.md); [conversion and approximation](dxf-spline-approximation.md); [candidate amendments](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md); [validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md) |
| Author directions | D-A-6, D-A-7 (P0 sheet §0); D-A-N2-1 to D-A-N2-5 (instruction of 2026-11-25) |

## 0. Boundary

The store keeps inside the `.cedg` document an authoritative copy of every imported
reference (the admitted binary64 data, the explicit transformation, provenance and
diagnostics), its presentation state, its export preference and its derivation history, so
that the document reopens without the original DXF file. It creates no `GeoElement`, is not
part of `Construction`, of its XML, of Undo/Redo or of the spatial identity registry, has no
dependencies and publishes no measure. A reference stays external data without CeDG
authority (import §1, layer 2). The original DXF file is provenance only: it is never read
again to rebuild a stored reference, and a file found again never corrects or replaces the
stored copy.

## 1. Authority

AGENTS §1 (1, 3, 7, 8, 10), §4, §5, §9, §12.6, §13, §16, §17; import
`dxf-spline-reference.md` §1 to §12, §14; `bspline-2d.md` §8, §10.5, §12.3; ADR 0016;
[`native-document-identity.md`](../ui/native-document-identity.md) (serialization, CU4
amendment, open, Classic boundary, invariants).

## 2. Code facts at the base

Verified on `a9ee803bf` (characterization report §2). They justify the mechanism; the code
remains the authority on itself.

| # | Fact | Consequence |
|---|---|---|
| F1 | `MyXMLioJre.readZip` reads every entry before the XML; an unknown entry falls to the image branch: `ImageIO.read` returns null and an empty image is registered as `<name>.png` in the image manager (not undone by rollback); nothing fails | an older reader opens the document, ignores the store and keeps a harmless empty image |
| F2 | `geocedg_defaults_v2.xml` (CU4) is read only in a GeoCeDG document context, inside the ZIP loop, with a byte cap of 16 KiB and a strict parser; an invalid block fails the preflight | precedent for an optional, versioned, capped, strictly validated GeoCeDG entry |
| F3 | `writeGeoGebraFile` writes only known entries; an unknown entry read at open is not re-saved | the current build and 1.2.2 drop the store on re-save; the version that introduces `R-c` must write it explicitly |
| F4 | An unknown child of `<geogebra>` is only logged, but its own children are then read as top-level elements (a nested `construction`, `kernel` or `geocedgHiddenLayers` would be interpreted; `geocedgSpatial` would throw) | a block inside `geogebra.xml` is unsafe for older readers |
| F5 | Native Undo/Redo stores a single `geogebra.xml` entry and reads only the first entry | an archive entry is outside Undo by construction |
| F6 | Native save writes to a same-directory temporary, runs `DocumentArchivePreflight.validate` on the exact bytes in a disposable `AppDNoGui`, then promotes | the store is validated by the same preflight; the disposable app installs nothing |
| F7 | Native open (File → Open, Recent, drag and drop, API `openFile`, `.ggb` included) captures the previous archive, loads, and on failure restores it; the host's `nativeDocumentLoadCommitted` runs only after success | a store prepared during reading and installed at the commit never loses the live store on failure |
| F8 | `.html`, `setBase64` and a clearing `setXML` replace the document without the transactional capture; New clears the session right after the save prompt; the upstream `openURL` clears **before** its dialog (not wired in GeoCeDG actions) | the store must be installed only on success in those routes, and the upstream `openURL` must not clear it early (characterization C-1) |
| F9 | Ordinary copy/paste keeps XML in memory; Insert File loads the source in a helper app; the clipboard-image insertion writes the receiver's archive, hashes every entry other than `geogebra.xml` to prove it unchanged, and its recovery re-reads that archive | no clipboard route transports the store; the recovery reread must reinstall the same store |
| F10 | `App.isSaved()` is true whenever the construction has no save-relevant content, whatever the flag; the `R-a` store never calls `setUnsaved`; hidden layers use `setUnsaved` plus an override of `hasSaveRelevantDocumentPresentation` | the store must follow the hidden-layer pattern or references can be discarded without a prompt |
| F11 | The `R-a` session store is a list of `(record, visible)` addressed by list position, with no identifier and no name; one store per window; cleared by `clearDxfReferences()` | `R-c` replaces positions by identifiers |
| F12 | `DxfSplineReference` and `DxfFileProvenance` have package-private constructors and compute both digests in the constructor; there is no separate knot limit (`n + p + 2 ≤ 1 032`) | the codec lives in `org.geocedg.common.importer.dxf`; digests are recomputed, never trusted |
| F13 | `BSplineCanonicalEncoding.digestHex(contract, data)`: ASCII tag, `0x00`, `p`, `n + 1`, knots, points, `I` or `E` + weights, big-endian binary64; SHA-256, 64 lowercase hex | integrity reuses the existing canonical encoding |
| F14 | Spatial tokens are 32 lowercase hex from a non-cryptographic generator whose collisions are prevented only by the construction registry | store identifiers need their own source and their own uniqueness check |
| F15 | The expert inspection and angle-mode scanners classify unknown entries as outside their pilot and cap archives at 8 MiB, entries at 4 MiB and 32 entries | the implementation must register the entry with those scanners; the store cap is aligned with their entry cap (§5.3) |
| F16 | GeoCeDG Classic diagnostic is a separate process on the upstream main class; it already drops `geocedg_defaults_v2.xml` and `<geocedgHiddenLayers>` on save | a pre-existing deviation from the Classic preservation rule; decision RC-D4 |
| F17 | GeoCeDG 1.2.2 has a byte-identical archive reader (`MyXMLioJre`, `MyXMLioD`, `ImageManagerD`, `AppD`) | the older-reader behavior of F1/F3 is established for 1.2.2 |

## 3. Scope

Inside: container, schema, identity, lifecycle in every document route, integrity, failure
model, compatibility, limits, frontend obligations and validation. Outside: the export of
references (`cedg-reference-export/v1`), the conversion and approximation method
(`dxf-spline-approximation/v1`), `R-d`, `MAINT-A-R`, Graphics 2.

## 4. Container

**One optional ZIP entry with the fixed name `geocedg_references.xml`**, with the version
inside its content (decision RC-D1, RC-D2):

| Criterion | ZIP entry (adopted) | child of `<geogebra>` in `geogebra.xml` (rejected) |
|---|---|---|
| outside `Construction` | physically | outside `<construction>`, inside the document XML |
| Undo/Redo, clipboard XML | never enters (F5, F9) | travels through `setXML`, API and clipboard XML routes |
| older readers | ignored, empty image registered, dropped on re-save (F1, F3) | its children read as top-level elements (F4) |
| size | own cap before parsing; the construction parse is unchanged | inflates the SAX parse of the construction and its preflight |
| upstream change | `MyXMLioJre` reader branch and writer (already modified by CU4) | `MyXMLHandler` and `MyXMLio.getFullXML` |
| precedent | `geocedg_defaults_v2.xml` | `geocedgHiddenLayers` (flat attributes only) |

The name carries no version: a versioned name (`…_v2.xml`) would reach a `v1` reader as an
unknown entry, treated as an image and lost silently; with a fixed name, a `v2` content
reaches the `v1` reader, which rejects it with a typed cause. Complement (decision RC-D8):
the reader that introduces `R-c` rejects any unknown root entry whose name starts with
`geocedg_`, as a guard for future GeoCeDG blocks.

## 5. Schema `cedg-reference-store/v1`

### 5.1 Syntax

Strict UTF-8, no DTD, no processing instruction other than the XML declaration, no comment,
no text between elements, no namespace. Attributes in the written order; the reader rejects
unknown, duplicated or missing attributes and elements.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<geocedgReferenceStore contract="cedg-reference-store/v1" count="1">
  <reference id="(32 hex)" record="dxf-spline-reference/v1" visible="true" export="AUTO">
    <name text="South profile"/>
    <file sha256="(64 hex)" baseName="plan.dxf" acadVersion="AC1027" insUnits="4" documentUnit="mm"/>
    <entity handle="2A" layer="CURVES" ordinal="12" planeZ="(16 hex)" normal="none"/>
    <representation rationalFlag="SET" weights="EXPLICIT"/>
    <transform coefficients="(6 × 16 hex: a b c d e f)"/>
    <source degree="2" controlPoints="3" knots="(16 hex …)" x="(16 hex …)" y="(16 hex …)" w="(16 hex …)"/>
    <data x="(16 hex …)" y="(16 hex …)"/>
    <digests source="(64 hex)" data="(64 hex)"/>
    <diagnostics codes="CLOSED_FLAG_CONTRADICTED" unrecognized="62 370"/>
    <derivations>
      <derivation id="(32 hex)" operation="APPROXIMATION" outcome="FAILED" method="bspline-param-lsq/v1" cause="TOLERANCE_NOT_REACHED_POINT_LIMIT" bestIndicator="(16 hex)" bestControlPoints="128"/>
    </derivations>
  </reference>
</geocedgReferenceStore>
```

### 5.2 Values

1. `contract` is checked **before** any other attribute or element; any value other than
   `cedg-reference-store/v1` → `UNSUPPORTED_STORE_VERSION`. `count` equals the number of
   `<reference>` elements.
2. Reals are binary64 bit patterns: 16 lowercase hexadecimal digits of
   `Double.doubleToRawLongBits`, lists separated by one space. A non-finite pattern →
   `MALFORMED_STORE`. Every value, `-0.0` included, round-trips exactly. Integers are
   canonical decimal (no sign, no leading zero). The inspector shows decimals; the file is
   not meant to be edited by hand (decision RC-D3).
3. `w` is present iff `weights="EXPLICIT"` (the absence marker of import §4.6). Knots and
   weights are not repeated under `<data>`: by contract they are those of `<source>`
   (import §6).
4. Only primary data and non-recomputable provenance are stored. **Recomputed on reading
   and never stored:** nature, effective weights, domain, closure, continuity, tessellation
   (import §5, §8; §12 point 3).
5. Strings (`text`, `baseName`, `handle`, `layer`, `acadVersion`, `documentUnit`) are
   XML-escaped; the base name never carries a path (import §9). A character that XML 1.0
   cannot carry, and the backslash itself, are written as `\u{H…}` (one to six uppercase
   hexadecimal digits of the code point); the reader decodes exactly this form and rejects
   any other backslash sequence (`MALFORMED_STORE`). Saving never fails because of a string.
6. `<derivations>` is optional; each `<derivation>` has `id`, `operation`
   (`EXACT_CONVERSION` or `APPROXIMATION`), `outcome` (`CREATED` or `FAILED`), `method`, and
   optional `cause`, `bestIndicator` (bit pattern of the best global indicator of a failed
   approximation), `bestControlPoints` (its number of control points) and `construction`
   (the `geocedgId` of the created output, information only, never a resolution key).
   Entries are append-only and are written by
   `dxf-spline-approximation/v1` §13.2. They are reserved and validated in `v1` even before
   `MAINT-A-DXF1` exists (decision RC-D10).
7. `export` is the persistent export preference `AUTO`, `INCLUDE` or `EXCLUDE`
   (`cedg-reference-export/v1` §6.2); `AUTO` is the default.
8. `<name>` is optional: present only when the user renamed the reference; otherwise the
   display name is derived from provenance (layer · handle). A name is never identity nor an
   association key (decision RC-D11).
9. Order is the order of the user's list (insertion order); order is never identity.

### 5.3 Limits of `v1`

Fixed by this contract, not by the import constants at run time (decision RC-D9):

| Limit | Value |
|---|---|
| entry size (uncompressed) | `≤ 4 MiB` (aligned with the scanners' entry cap, F15) |
| references | `≤ 10 000` |
| per reference | `1 ≤ p ≤ 7`; `p + 1 ≤ n + 1 ≤ 1 024`; `m + 1 = n + p + 2`; weight ratio `≤ 10^6` |
| derivation entries per reference | `≤ 256` |
| string length | `≤ 1 024` UTF-8 bytes each |

**Real capacity.** A reference stores `source` and `data`, so a maximal one (`n + 1 = 1 024`,
`p = 7`, explicit weights) holds about 6 150 binary64 values, about 105 KB in this encoding:
about 39 such references fit in 4 MiB. A typical reference (a cubic with 20 control points)
takes about 3 KB, so about 1 300 fit. The store is therefore stricter than the approved
import limits (`SPLINE_ENTITIES_MAX = 10 000`, `N_MAX_IMPORT = 1 024`), which keep governing
the reading of a DXF file; admission into the document is additionally bounded by this
capacity (candidate amendment A-IMP-9).

**Every mutation is checked.** The serialized size is deterministic, so each operation that
grows the store is checked exactly before it is applied: an import whose admitted records
would exceed a limit is rejected as a whole before anything is added; a rename or a
preference change that would exceed it is refused; an approximation attempt on a reference
with 256 derivation entries is refused before computing (`DERIVATION_LIMIT_REACHED`,
`dxf-spline-approximation/v1` §9). All refusals use `STORE_LIMIT_EXCEEDED` or that cause and
change nothing, so a valid store can always be saved. A larger capacity requires
`cedg-reference-store/v2` or decision RC-D9.

### 5.4 Empty store

No entry is written when the store is empty: a document without references keeps exactly
the current entry inventory and bytes.

## 6. Document identity of a reference

- **`id`:** 128 random bits from a cryptographically strong source, 32 lowercase hex, minted
  when the record is **admitted** into the store (import). It is not drawn from the spatial
  identity registry and does not widen the construction's identity space; uniqueness inside
  the store is checked at minting (redraw on collision) and on reading (`DUPLICATE_ID`).
- **Never derived** from coordinates, data digests, DXF handle or layer, file name, labels,
  ordinals, XML position or order. Two imports of the same file yield different
  references. Equal data digests only produce the informative notice
  `SAME_DATA_AS_REFERENCE`, never an association or a deduplication.
- **Stability:** the `id` survives save, reopen, Save As and a file copy on disk. Two copied
  files share their ids: the scope is the document, not the universe.
- **Semantic scope:** the `id` identifies external data of the document; it is not CeDG
  geometric identity, not `ceID`, not a graph node and never an input of a command.
- **Derivation `id`:** same format and source, minted by `dxf-spline-approximation/v1` for
  each attempt.
- The Desktop store addresses every operation (show, hide, rename, remove, preference) by
  `id`, never by list position.

## 7. Lifecycle

| Event | Behavior |
|---|---|
| Import | admitted records + new `id`s + `visible="true"` + `export="AUTO"`; document marked modified; no Undo step; `Construction`, its XML and its Undo history unchanged |
| Show / hide / rename / preference | changes the store; document modified; no Undo step |
| Remove | explicit action with confirmation; when live constructions name the reference in their provenance, the confirmation says «N derived constructions will remain»; no Undo step (decision RC-D5) |
| Save / Save As | writes the entry when the store is not empty; the save preflight validates it; the A-DXF0 «session only» notice disappears |
| Reopen | store rebuilt from the entry without the DXF; tessellations recomputed; same `id`s, order, names, visibility, preferences and derivations |
| New | empty store; the save prompt covers a non-empty store |
| Close | discarded after the save prompt; the prompt covers store changes |
| Close without saving | the file is unchanged |
| Open another document | the incoming store is **prepared** while reading and **installed** only at the transition commit; until then the live store is untouched |
| Failed open | the previous document, its store included, stays as it was; the prepared store is discarded |
| Non-transactional replacement (`.html`, `setBase64`, clearing `setXML`) | the incoming store is installed only after a successful parse; otherwise the live store stays |
| Undo / Redo | never adds, removes or changes references, preferences or derivation entries |
| Copy / paste of constructions | never transports references |
| Save As, file copy | copies the whole store with its ids |
| Duplicate a reference | not offered in `v1`; a later duplicate operation would mint a new `id`, copy no derivation entry and start at `AUTO` |
| Insert File | the helper validates the source entry (unknown version → insertion rejected, receiver unchanged); the source's references are **not** transferred (decision RC-D6); the receiver store is unchanged |
| Clipboard-image insertion recovery | the reread of the receiver's archive reinstalls the identical store |
| Recovery snapshot, expert inspection, API `getGGBfile`/`getBase64` | include the entry: they are the complete document |
| `.ggb` route | the extension only routes (ADR 0016); an entry inside a `.ggb` is read the same way |
| GeoCeDG Classic diagnostic | decision RC-D4 |
| Change of construction unit | no rescale and no change of references (import §6) |

The store creates no second DAG: there is no propagated evaluation between records, nor
from a record to the construction.

## 8. Saved state

Every store mutation marks the document modified (`setUnsaved`), and a non-empty store is
save-relevant document content (the hidden-layer pattern, F10), so New, Open, drop and
Close prompt before discarding references. Saving clears the mark as for any other content.

## 9. Integrity

On reading, in this order, each failure with its typed cause (§10):

1. size `≤` cap before parsing (read at most `cap + 1` bytes); the ZIP CRC is checked by the
   archive reader;
2. strict UTF-8; strict grammar (§5); version first;
3. per reference: the `source` data satisfy the data predicates 4 to 13 of import §4.4 and
   `BSplineData` validation; effective weights and nature are rebuilt by import §4.6;
4. `T(source)` is recomputed with the arithmetic of import §6 (`(a·x + b·y) + e`, no FMA)
   and must equal `data` **bit for bit** (`TRANSFORM_MISMATCH`); `T` finite and non-singular;
5. both digests are recomputed with `BSplineCanonicalEncoding.digestHex(
   "dxf-spline-reference/v1", …)` and must equal the stored ones (`DIGEST_MISMATCH`);
6. reference and derivation ids are unique (`DUPLICATE_ID`).

The check detects corruption and incoherence; it is not a signature and does not protect
against a deliberate coherent edit. The stored copy is the authority of the reference.

## 10. Failure model

| Cause | When |
|---|---|
| `UNSUPPORTED_STORE_VERSION` | `contract` other than `v1` (a future `v2` included) |
| `UNKNOWN_GEOCEDG_ENTRY` | an unknown root entry `geocedg_*` (RC-D8) |
| `STORE_LIMIT_EXCEEDED` | size, counts or limits of §5.3 (reading, import or any growing mutation) |
| `MALFORMED_STORE` | grammar, encoding, numbers, attributes, duplicates, `w` incoherent with `weights` |
| `INADMISSIBLE_RECORD` | a record fails the data predicates of import §4.4 |
| `TRANSFORM_MISMATCH`, `DIGEST_MISMATCH`, `DUPLICATE_ID` | §9 |

Per route: **Open** (every native route): the disposable preflight fails before the live
document is touched; if a failure came later, the captured archive is restored and the live
store was never changed; a localized message names the cause, and **Details…** the
reference (`id`, name). **Save:** the preflight rejects the bytes, the previous file stays
unchanged, the in-memory document is unchanged. **Insert File:** insertion rejected.
**Undo, clipboard, redefinition:** they never read the entry. One invalid record rejects the
whole document; a subset is never loaded silently (an explicit «Open without references»
recovery action is a separate decision, RC-D7).

## 11. Compatibility

- **Older readers are not supported (D-A-N2-2).** A document with a store opens in GeoCeDG
  1.2.2 and earlier readers with the same archive reader (F17): the construction loads, the
  entry is treated as an unreadable image, and **the references are lost when that reader
  saves the document again**. No forced-rejection marker is written in `geogebra.xml` (it
  would intrude into the construction XML for a requirement the author waived). Release notes
  of the first version with `R-c` must say: «Documents that contain saved DXF references
  require GeoCeDG ≥ X.Y; earlier versions ignore them and remove them when saving.»
- **The version that introduces `R-c`:** correct reading and writing; safe typed rejection of
  unknown versions and malformed data; restoration of the previous document; no silent loss
  in any product route (Classic per RC-D4).
- **Old documents:** without the entry → empty store; no migration, no rewrite; their
  construction bytes and entry inventory do not change on reopen and save unless the user
  imports references. Nothing is associated without explicit action (AGENTS §12.6).
- **Future versions:** `v2` must be readable by its own reader; a `v1` reader rejects it.

## 12. Frontend obligations

- Reference manager, ordinary use (D-A-N2-5): recognizable name, nature («polynomial»,
  «rational»), visibility, conversion/approximation state from the export resolver,
  operations (show, hide, rename, remove, preference, convert/approximate when
  `MAINT-A-DXF1` exists), short notices. **Details…**: definition, provenance,
  transformation, identifiers, digests, diagnostics, limits, derivation history.
- The «session only» notice at save is withdrawn; the save prompt covers references.
- Confirmation on removal with live derived constructions; open-failure message with the
  typed cause.
- Inspector text: «Imported reference, read-only; saved in the document».
- No change to tools, snapping, hit testing or layers: references stay non-selectable and are
  never snapping targets (import §10).

## 13. Kernel and upstream impact (AGENTS §9)

| Piece | Change | Ownership |
|---|---|---|
| `MyXMLioJre.readZip` | branch for `geocedg_references.xml` (and the `geocedg_*` guard): bounded read, GeoCeDG codec, hand-over of a prepared store to a host | upstream file already modified by CU4 |
| `MyXMLioJre.writeGeoGebraFile` | writes the entry the host provides, before `geogebra.xml` | upstream file already modified |
| store codec `cedg-reference-store/v1` | new, pure, in `org.geocedg.common.importer.dxf` (package-private constructors of the records, F12); no `Construction`, `GeoElement` or kernel type | GeoCeDG |
| host interface (provide bytes; receive a prepared store) | new, GeoCeDG; implemented by `AppGeoCeDG`; the preflight's `AppDNoGui` does not implement it and therefore only validates | GeoCeDG |
| `AppGeoCeDG`, `GeoCeDGDxfReferenceSession` | ids, prepare + commit, saved state, clearing moved to the commit | GeoCeDG |
| inspection and angle-mode scanners | register the entry and its cap | GeoCeDG |
| `GuiManagerD.openURL` | must not clear the store before its dialog, or stay unwired in GeoCeDG (C-1) | upstream, only if wired |
| Classic | only if RC-D4 = preserve | upstream |

**Unchanged:** `Construction`, `<construction>`, `MyXMLHandler`, `MyXMLio.getFullXML`,
`doParseXML`, the Undo managers, `SpatialIdentityRegistry`, command codecs, commands,
Locus V2.

## 14. Required characterization before code (`MAINT-A-DXF0-RC-S0`)

- **C-1** every open, New, replacement and URL route against the prepare/commit rule (F7, F8),
  with a failure injected after parsing;
- **C-2** an isolated probe of 1.2.2 with a store entry: opens, ignores, registers the empty
  image, drops on re-save;
- **C-3** GeoCeDG Classic diagnostic with the entry;
- **C-4** cost of reading, validating and tessellating a store at the limits, in open and in
  the save preflight;
- **C-5** the scanners (inspection, angle mode) and the recovery snapshot with the entry.

## 15. Validation

Cases RC-1 to RC-22 of the
[A-N2 validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md).

## 16. Stop conditions

Stop if the implementation needs: references or derivations in `geogebra.xml` or in the
construction XML; a change to `MyXMLHandler`, Undo or spatial identity; ids minted by the
construction registry; an open route that empties or replaces the live store before the
commit and cannot be corrected in Desktop; a clipboard or Insert File route that cannot
exclude the store without changing its resource contract. None was found in the base; C-1
is mandatory evidence.

## 17. Answers to `dxf-spline-reference.md` §12

| Point of §12 | Answer |
|---|---|
| 1. older and current readers with an unknown entry | F1, F3, F17; §11 |
| 2. entry name, versioned identifier, fail-closed rule | §4, §5.2 (1), §10 |
| 3. stored data, authority of the copy | §5.2, §9; tessellation never stored |
| 4. save/reopen, mismatch with a file found again | §7; §0 (the stored copy prevails; a reimport creates new references) |
| 5. Undo/Redo, copy/paste between documents, close without saving | §7 |
| 6. size limits and integrity | §5.3, §9 |
| 7. exclusion from semantic exports and from the layer domain | semantic exports exclude references unless the separate capability `cedg-reference-export/v1` is chosen; the store is never part of the layer domain |

## 18. Open decisions

| Id | Question | Recommendation | Consequence |
|---|---|---|---|
| RC-D1 | Container | ZIP entry `geocedg_references.xml` | only `MyXMLioJre` upstream; outside Undo by construction |
| RC-D2 | Identifier | `cedg-reference-store/v1`, fixed entry name | future versions rejected by `v1` |
| RC-D3 | Numbers | 16-hex bit patterns | exact and canonical; illegible by hand (the inspector shows decimals). Alternative: shortest decimal with a canonical-form check |
| RC-D4 | GeoCeDG Classic | validate and preserve the entry bytes opaquely | meets ADR 0016 d. 7 and «no silent loss»; touches the Classic writer. Alternatives: Classic refuses to save a document with a store (message), or accept the loss as the existing defaults/hidden-layer deviation |
| RC-D5 | Removal | outside Undo, with confirmation | Undo unchanged; recovery only by reopening the saved file or reimporting (new `id`) |
| RC-D6 | Insert File | never transfer references | inserted derived constructions become orphans (export state 8) |
| RC-D7 | Corrupt store | reject the whole document; a later explicit «Open without references» action that marks the document modified and never overwrites | no silent loss; the action is a separate increment |
| RC-D8 | `geocedg_*` guard | yes | rejects future unknown GeoCeDG blocks; the corpus check (RC-4) must pass |
| RC-D9 | Limits | §5.3, entry `≤ 4 MiB` (about 39 maximal or 1 300 typical references) | aligned with the scanners; stricter than the import limits (A-IMP-9); alternatives: a higher cap with updated scanner caps, or storing `source` only and recomputing `data` (halves the size, loses the arithmetic-drift check of RC-D12) |
| RC-D10 | `<derivations>` and `export` reserved in `v1` | yes | no `v2` when export and A-DXF1 arrive |
| RC-D11 | Display name | stored only when the user renames | never identity or association |
| RC-D12 | Store `data` besides `source` | yes, with bit-for-bit verification | detects corruption and arithmetic drift |
| RC-D13 | Normative form | this contract + amendments of the import contract and of `native-document-identity.md` + ADR 0044 | ADR 0016 requires a compatibility decision for a new archive layout |
| RC-D14 | Maturity | the persistence starts `experimental` (AGENTS §5) and becomes `stable`, like the import after R1, only by author decision after its smoke | the incompatibility with older readers is declared |
