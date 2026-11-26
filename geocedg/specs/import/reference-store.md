# Document store of imported references (`R-c`) — `cedg-reference-store/v1`

| Field | Value |
|---|---|
| Status | **CANDIDATE — READY FOR AUTHOR ADOPTION (revision R4).** Author decision D-R4-1 of 2026-11-26 authorizes, for the future scope of `MAINT-A-DXF0-RC`, the additive import-record change of A-IMP-12 that §5.2 item 11 needs (R4 report §13). Normative design written by `PRE-G9B-MAINT-A-N2` and refined by `PRE-G9B-MAINT-A-N2-R1` to `-R4`. `selfApproved=false`. It authorizes no code: `MAINT-A-DXF0-RC-S0` and `MAINT-A-DXF0-RC` are not authorized |
| Revision | R4 (2026-11-26, independent R3 audit I-1 and I-2): `normal` preserves the groups 210, 220 and 230 present and their presence, the effective normal is derived and never stored (§5.2 item 11, F22, A-IMP-12); the `values` counter is defined exhaustively over serialized binary64 values (§5.2 item 12); budget 1 500 000 values adopted by the author as the provisional product value. R3 (2026-11-25, pre-adoption audit M1): budget measured directly; `maxValues` 1 500 000 with an aggregate string allowance instead of the R1 value 3 000 000; streaming reader with per-attribute bounds; C-4 at the budget; Classic validation scope and string limit clarified. R1 (2026-11-25): the fixed 4 MiB cap is replaced by a measured, configurable resource policy (§5.3, D-R1-4); source and transformed data stay mandatory (D-R1-3); no derivation history (D-R1-5); atomic document transition per route (§7.1); GeoCeDG Classic preserves the entry opaquely (§7.2, D-R1-8); maturity without a novelty flag (D-R1-10). The A-N2 text is commit `874286e95` |
| Contract identifier | `cedg-reference-store/v1` (the store); its records stay `dxf-spline-reference/v1` |
| Container | one optional ZIP entry of the native document, fixed name `geocedg_references.xml` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| Level | `R-c` of [`dxf-spline-reference.md`](dxf-spline-reference.md) §12 (answered point by point in §17) |
| Architectural layer | Document/application model outside the geometric kernel (AGENTS §4); a pure codec in the importer package |
| Decision | [ADR 0044](../../../docs/adr/0044-document-store-of-imported-references.md) (proposed) |
| Companion candidates | [reference DXF export](../export/dxf-reference-export.md); [conversion and approximation](dxf-spline-approximation.md); [candidate amendments](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md); [validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md) |
| Author directions | D-A-6, D-A-7 (P0 sheet §0); D-A-N2-1 to D-A-N2-5 (A-N2 instruction); D-R1-1 to D-R1-5, D-R1-8, D-R1-10 (R1 instruction) |

## 0. Boundary

The store keeps inside the `.cedg` document an authoritative copy of every imported
reference (the admitted binary64 source data, the transformed data, the explicit
transformation, provenance and diagnostics), its presentation state and its export
preference, so that the document reopens without the original DXF file. It creates no
`GeoElement`, is not part of `Construction`, of its XML, of Undo/Redo or of the spatial
identity registry, has no dependencies and publishes no measure (D-R1-1). A reference stays
external data without CeDG authority (import §1, layer 2). The original DXF file is
provenance only: it is never read again to rebuild a stored reference, and a file found
again never corrects or replaces the stored copy. The store keeps no history of conversions,
approximations or failed attempts (D-R1-5): the provenance block of a created construction is
the only record of a derivation (`dxf-spline-approximation/v1` §13.2).

## 1. Authority

AGENTS §1 (1, 3, 7, 8, 10), §4, §5, §9, §12.6, §13, §16, §17; import
`dxf-spline-reference.md` §1 to §12, §14; `bspline-2d.md` §8, §10.5, §12.3; ADR 0016;
[`native-document-identity.md`](../ui/native-document-identity.md) (serialization, CU4
amendment, open, Classic boundary, invariants).

## 2. Code facts at the base

Verified on `a9ee803bf` (A-N2 characterization report §2 and R1 report §C). They justify
the mechanism; the code remains the authority on itself.

| # | Fact | Consequence |
|---|---|---|
| F1 | `MyXMLioJre.readZip` reads every entry before the XML; an unknown entry falls to the image branch: `ImageIO.read` reads only the header, returns null, and an empty image is registered as `<name>.png` (not undone by rollback); the rest of the entry is inflated and discarded; nothing fails | an older reader opens the document, ignores the store and keeps a harmless empty image |
| F2 | `geocedg_defaults_v2.xml` (CU4) is read only in a GeoCeDG document context, inside the ZIP loop, with a byte cap of 16 KiB and a strict parser; an invalid block fails the preflight; it is the only bounded entry and the only one checked for duplicates | precedent for an optional, versioned, bounded, strictly validated GeoCeDG entry |
| F3 | `writeGeoGebraFile` writes only known entries; an unknown entry read at open is not re-saved | older readers drop the store on re-save; the version with `R-c` writes it explicitly |
| F4 | An unknown child of `<geogebra>` is only logged, but its own children are read as top-level elements | a block inside `geogebra.xml` is unsafe for older readers |
| F5 | Native Undo/Redo stores a single `geogebra.xml` entry and reads only the first entry | an archive entry is outside Undo by construction |
| F6 | Native save writes to a same-directory temporary, re-reads it with `DocumentArchivePreflight.validate` in a disposable `AppDNoGui`, then promotes | the store is validated by the same preflight; the disposable app installs nothing |
| F7 | Native open (File → Open, Recent, file drop, API `openFile`, Reset with a file, `.html` with a file name, `.ggb`) captures the previous archive, preflights, loads, restores it on failure; the last hook that can still roll back is `beforeNativeUndoBaselineCommit`; `nativeDocumentLoadCommitted` runs **after** the protected block | validation must finish while reading; the installation at the commit must be a non-throwing swap |
| F8 | `.html` with embedded base64, `setBase64`, `loadURL` of archive bytes and `base64://` replace the document through `App.loadXML(ZipFile)` without capture or preflight; an API clearing `setXML` also lacks capture; a failed parse can leave a partly replaced construction | these routes must be sent through the native transaction (§7.1) |
| F9 | `commitLoadedDocument` also runs for reloads that are not document changes (tool-creation dialog, preference reload) | the store is never installed or cleared there |
| F10 | Hidden layers already use prepare-then-commit: the parse records the set in a field without initializer, and the transition commits it | the store follows the same pattern |
| F11 | Ordinary copy/paste keeps XML in memory; Insert File loads the source in a helper app and never clears the receiver; the clipboard-image insertion writes and preflights a receiver snapshot and its recovery re-reads it | no clipboard route transports the store; recovery must leave the live store untouched |
| F12 | `App.isSaved()` is true whenever the construction has no save-relevant content; the `R-a` store never calls `setUnsaved`; hidden layers use `setUnsaved` and `hasSaveRelevantDocumentPresentation` | the store follows the hidden-layer pattern |
| F13 | The `R-a` store is a list of `(record, visible)` addressed by position, without identifier or name | `R-c` addresses references by identifier |
| F14 | `DxfSplineReference` and `DxfFileProvenance` have package-private constructors and compute both digests in the constructor | the codec lives in `org.geocedg.common.importer.dxf`; digests are recomputed, never trusted |
| F15 | `BSplineCanonicalEncoding.digestHex(contract, data)` (SHA-256 of a canonical big-endian binary64 encoding) | integrity reuses the canonical encoding |
| F16 | Spatial tokens come from a non-cryptographic generator whose collisions only the construction registry prevents | store identifiers have their own source and uniqueness check |
| F17 | The 4 MiB entry cap, the 8 MiB archive and inflated totals and the 32-entry limit are private constants of two passive scanners (expert inspection, angle-mode source check of Insert File); both treat any unknown entry as a finding, so today **any** store, whatever its size, makes those operations refuse the document; the expert worker caps sources at 8 MiB | the scanners must recognize the entry and validate it under the store policy; the product capacity is not derived from them |
| F18 | No reader has inflation-ratio or ZIP-bomb protection; known entries are loaded unbounded | the store reader brings its own bounded read |
| F19 | Neither the packaged application nor the developer launcher sets `-Xmx`: the JVM takes a quarter of the RAM (1 GiB on a 4 GiB machine); the expert worker uses `-Xmx512m` | budgets are sized for a 1 GiB heap with margin |
| F20 | GeoCeDG Classic diagnostic (same package, separate process, `App3D` without product configuration) uses the same open and save code with `geoCeDGDocument = false`; it drops the CU4 entry and hidden layers on save | without an explicit rule it would drop the store too (§7.2) |
| F21 | GeoCeDG 1.2.2 has a byte-identical archive reader | the older-reader behavior of F1/F3 holds for 1.2.2 |
| F22 | (R4) `DxfSplineReader` stores groups 210, 220 and 230 into one three-element array initialized to `0.0`, the last occurrence of a repeated group winning, with one flag set when any of them appears; `DxfSplineReference.normal()` returns `null` or that array, so an absent component reads `0.0` and the presence of each group is not kept | the store records presence per group (§5.2 item 11) and `MAINT-A-DXF0-RC` adds it to the import record (A-IMP-12); the array alone is never serialized |

## 3. Scope

Inside: container, schema, identity, lifecycle and atomic transitions in every document route,
integrity, resource policy, failure model, compatibility, Classic, frontend obligations and
validation. Outside: the export of references (`cedg-reference-export/v1`), the conversion and
approximation method (`dxf-spline-approximation/v1`), `R-d`, `MAINT-A-R`, Graphics 2.

## 4. Container

**One optional ZIP entry with the fixed name `geocedg_references.xml`**, with the version
inside its content (decisions RC-D1, RC-D2):

| Criterion | ZIP entry (adopted) | child of `<geogebra>` in `geogebra.xml` (rejected) |
|---|---|---|
| outside `Construction` | physically | outside `<construction>`, inside the document XML |
| Undo/Redo, clipboard XML | never enters (F5, F11) | travels through XML routes |
| older readers | ignored, empty image registered, dropped on re-save (F1, F3) | its children read as top-level elements (F4) |
| size | own bounded read; the construction parse is unchanged | inflates the SAX parse of the construction and its preflight |
| upstream change | `MyXMLioJre` reader branch and writer (already modified by CU4) | `MyXMLHandler` and `MyXMLio.getFullXML` |

A versioned entry name would reach a `v1` reader as an unknown entry and be lost silently; with
a fixed name a `v2` content reaches the `v1` reader, which rejects it with a typed cause. The
reader that introduces `R-c` also rejects any unknown root entry whose name starts with
`geocedg_` and a duplicated `geocedg_references.xml` (decision RC-D8).

## 5. Schema `cedg-reference-store/v1`

### 5.1 Syntax

Strict UTF-8, secure XML processing, no DTD or DOCTYPE, no external entity, no processing
instruction other than the XML declaration, no comment, no text between elements, no
namespace. Attributes in the written order; the reader rejects unknown, duplicated or missing
attributes and elements.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<geocedgReferenceStore contract="cedg-reference-store/v1" count="1" values="29">
  <reference id="(32 hex)" record="dxf-spline-reference/v1" visible="true" export="AUTO">
    <name text="South profile"/>
    <file sha256="(64 hex)" baseName="plan.dxf" acadVersion="AC1027" insUnits="4" documentUnit="mm"/>
    <entity handle="2A" layer="CURVES" ordinal="12" planeZ="(16 hex)" normalGroups="230" normal="(16 hex)"/>
    <representation rationalFlag="SET" weights="EXPLICIT"/>
    <transform coefficients="(6 × 16 hex: a b c d e f)"/>
    <source degree="2" controlPoints="3" knots="(16 hex …)" x="(16 hex …)" y="(16 hex …)" w="(16 hex …)"/>
    <data x="(16 hex …)" y="(16 hex …)"/>
    <digests source="(64 hex)" data="(64 hex)"/>
    <diagnostics codes="CLOSED_FLAG_CONTRADICTED UNRECOGNIZED_GROUP_CODES NORMAL_RECORDED" unrecognized="90 280"/>
  </reference>
</geocedgReferenceStore>
```

In the example (R4) the source is a rational quadratic with three control points and explicit
weights, and the DXF entity carried only group 230: `values = 6 knots + 3 x + 3 y + 3 w + 3
data x + 3 data y + 6 coefficients + 1 planeZ + 1 normal = 29` (item 12). The same entity
without any of the groups 210, 220 and 230 is written `normalGroups="none"` without a `normal`
attribute and counts 28.

### 5.2 Values

1. `contract` is checked **before** any other attribute or element; any value other than
   `cedg-reference-store/v1` → `UNSUPPORTED_STORE_VERSION`. `count` is the number of
   `<reference>` elements and `values` the number of binary64 values serialized in the entry,
   counted exactly as item 12 defines; both are checked against the policy (§5.3) **before**
   any list is allocated, and against the actual content.
2. Reals are binary64 bit patterns: 16 lowercase hexadecimal digits of
   `Double.doubleToRawLongBits`, separated by one space; every list has exactly
   `17k − 1` characters for its declared `k` values. A non-finite pattern → `MALFORMED_STORE`.
   Every value, `-0.0` included, round-trips exactly (decision RC-D3; the R1 measurements show
   that after deflation hex costs only 7 to 17 % more than base64 or binary, and parsing is not
   the bottleneck). Integers are canonical decimal.
3. **Both `source` and `data` are mandatory** (D-R1-3): `data` is never replaced by a
   recomputation from `source`. On reading, `T(source)` is recomputed and must equal `data`
   bit for bit (§9); this check detects corruption and arithmetic drift but never substitutes
   the stored `data`.
4. `w` is present iff `weights="EXPLICIT"` (the absence marker of import §4.6). Knots and
   weights are not repeated under `<data>`: by contract they are those of `<source>`.
5. Never stored: nature, effective weights, domain, closure, continuity and tessellation, which
   are recomputed (import §5, §8; §12 point 3). The tessellation is never a persisted
   representation.
6. Strings (`text`, `baseName`, `handle`, `layer`, `acadVersion`, `documentUnit`) are
   XML-escaped and at most 1 024 bytes each **after escaping**; an import whose provenance
   string would exceed it is rejected as a whole with `STRING_TOO_LONG` (never truncated; the
   limit is part of the contract, not of the configurable budget), and a rename beyond it is
   refused; the base name never carries a path (import
   §9). A character that XML 1.0 cannot carry, and the backslash itself, are written as
   `\u{H…}` (one to six uppercase hexadecimal digits of the code point); the reader decodes
   exactly this form and rejects any other backslash sequence. Saving never fails because of a
   string.
7. `export` is the persistent export preference `AUTO`, `INCLUDE` or `EXCLUDE`
   (`cedg-reference-export/v1` §6.2); `AUTO` is the default.
8. `<name>` is present only when the user renamed the reference; otherwise the display name
   is derived from provenance (layer · handle). A name is never identity nor association.
9. Order is the order of the user's list; order is never identity.
10. `<diagnostics>` is optional. `codes` lists distinct import diagnostic codes from the closed
    enumeration of import §4.4 to §4.6 and §5, in the enumeration's order, separated by one space
    (at most 1 024 bytes); `unrecognized` lists the distinct unrecognized DXF group codes of the
    entity as canonical decimal integers in `0..1071`, in increasing order, separated by one
    space (at most 1 072 codes, 5 360 bytes). Duplicates, unknown codes or out-of-range values are
    `MALFORMED_STORE`. Their bytes count in the aggregate allowance of §5.3.
11. **Plane normal of the source entity (R4).** The store preserves the groups 210, 220 and
    230 that the DXF entity actually carried, and their presence, never a filled-in triple:
    - `normalGroups` lists the groups present, as canonical decimal integers in increasing
      order separated by one space, taken from `210`, `220` and `230`, or is `none` when the
      entity carries none of them. Every non-empty subset is admissible: one, two or the three
      groups.
    - `normal` is present **iff** `normalGroups` is not `none`, and then holds exactly one
      binary64 value per listed group, in the order of the list, in the form of item 2
      (`17g − 1` characters for `g` listed groups; numeric-list class of §5.3 kind 3). It holds
      the value the reader retained for that group: the current reader keeps the last
      occurrence of a group repeated within one entity (import A-IMP-12).
    - The four cases stay distinct: **(a)** no group: `normalGroups="none"`, no `normal`;
      **(b)** the three groups explicit: `normalGroups="210 220 230"` and three values, even
      when they equal `0 0 1`; **(c)** partial presence, one or two groups: only those groups
      and their values, so that a partial normal is never confused with a complete explicit
      triple; **(d)** the **effective normal**, the DXF interpretation in which an absent 210
      or 220 is `0` and an absent 230 is `1` (all absent: `(0, 0, 1)`), is **derived** on demand
      from (a) to (c), is never stored, never attributed to the source file and never used to
      transform coordinates (import §4.3).
    - **Current reader (fact at the base, F22).** `DxfSplineReader` sets each component of a
      three-element array to the value of its group when the group appears and leaves an absent
      component at `0.0`; `DxfSplineReference.normal()` returns `null` when no group appeared
      and otherwise that array, which records neither which groups were present nor the DXF
      default of an absent 230. That array is therefore neither the provenance of (a) to (c)
      nor the effective normal of (d), and a store is never written from it alone: the writer
      needs the presence of each group, which `MAINT-A-DXF0-RC` adds to the import record
      additively (A-IMP-12) without changing admission, values or any other behavior of the
      importer. Absent components are never written as `0.0` or `1.0`.
    - **Author decision D-R4-1 (2026-11-26).** The minimal additive change is in the scope of
      `MAINT-A-DXF0-RC`, under six conditions:
      1. for each of the three components the record keeps whether its group was present and,
         when it was, its original binary64 value;
      2. the geometric behavior, the admission rules, the diagnostics (`NORMAL_RECORDED`
         included) and the current interpretation of the importer are unchanged;
      3. presence is never inferred from a numeric value: a present `0.0` or `-0.0` is
         present, with its bit pattern kept (item 2);
      4. the recorded normal is never confused with the effective normal, and no provenance
         is invented;
      5. references stay outside `Construction` and the DAG;
      6. tests cover total absence, partial presence and complete presence, including
         explicit zeros and `-0.0`, together with persistence and reopening (RC-25).

      The decision authorizes the normative design and the future scope of RC, not the code
      change yet.
    - Validation: a group listed twice, out of order or outside `210 220 230`, a `normal`
      attribute present with `normalGroups="none"` or absent otherwise, a number of values
      different from the number of groups, or a non-finite value → `MALFORMED_STORE`. The
      diagnostic `NORMAL_RECORDED` is in `<diagnostics codes>` **iff** `normalGroups` is not
      `none` (`MALFORMED_STORE` otherwise).
12. **The `values` counter (R4).** `values` counts every binary64 value **serialized** in the
    entry, and nothing else. For a reference `r` with `c_r` = `controlPoints`, degree `p_r`,
    `K_r = c_r + p_r + 1` knots, `e_r = 1` when `weights="EXPLICIT"` (the `w` list is present)
    and `0` otherwise, and `g_r` the number of groups listed in `normalGroups` (0 to 3):

    | Field | binary64 values |
    |---|---|
    | `<source knots>` | `K_r` |
    | `<source x>`, `<source y>` | `c_r` each |
    | `<source w>` (only when `weights="EXPLICIT"`) | `e_r · c_r` |
    | `<data x>`, `<data y>` (transformed coordinates) | `c_r` each |
    | `<transform coefficients>` | `6` |
    | `<entity planeZ>` | `1` |
    | `<entity normal>` (only the groups present) | `g_r` |

    `v_r = K_r + (4 + e_r) · c_r + 7 + g_r` and `values = Σ_r v_r`. **Not counted**, because
    they are not binary64 values or are not serialized: integers (`count`, `values`, `degree`,
    `controlPoints`, `ordinal`, `insUnits`, the `unrecognized` codes), identifiers and hashes
    (`id`, `sha256`, both digests), flags and enumerations (`contract`, `record`, `visible`,
    `export`, `rationalFlag`, `weights`, `normalGroups`, `codes`), strings, and every implicit
    or derived value: unit weights when `weights` is not `EXPLICIT`, the knots and weights of
    `<data>` (those of `<source>` by item 4), absent normal groups, the effective normal,
    effective weights, nature, domain, closure and the tessellation. The writer computes
    `v_r` from the record by this formula only; the reader counts each decoded 16-digit token
    in one running counter, independent of the XML order of attributes and elements, and
    checks every list against its own declared size (`K_r`, `c_r`, `c_r` when explicit, `6`,
    `1`, `g_r`). A running count above the declared `values` stops the reader at once; a final
    count different from the declared `values`, or a list of another length, is
    `MALFORMED_STORE`; a declared `values` above `maxValues` is `STORE_BUDGET_EXCEEDED` before
    any allocation (§5.3). An absent optional field contributes nothing. The counter is
    arithmetic over lengths that kind 1 already admits: it adds **no geometric restriction**;
    one admissible reference counts at most `1 032 + 5 · 1 024 + 7 + 3 = 6 162` values.

### 5.3 Resource policy (R1, author decision D-R1-4; budget measured in R3)

There is **no arbitrary fixed capacity of the store**. Five kinds of limit are kept distinct:

| Kind | What it bounds | Rule | Configurable |
|---|---|---|---|
| 1. Geometric admission of an entity | one reference | the predicates of import §4.4: `1 ≤ p ≤ 7`, `p + 1 ≤ n + 1 ≤ 1 024`, `m + 1 = n + p + 2`, weight ratio `≤ 10^6`; re-checked on reading | no (contract) |
| 2. Per-operation resources | one import, one save, one open | import §4.1 (`FILE_BYTES_MAX`, `SPLINE_ENTITIES_MAX`), the tessellation work limit of import §8; the save and open costs measured in R1 report §C | as the import constants |
| 3. Reader safety | an untrusted archive | **incremental (streaming) reading**: the entry is parsed directly from the inflating stream through a byte counter that stops at `maxEntryBytes` plus one, without buffering the whole entry; one entry with this name; strict parser; the root's declared `count` and `values` checked against the budget before anything else is read; **every attribute token bounded while it is read** (a numeric list by `17 × 1 032 − 1` bytes; a string by 1 024 bytes after escaping; `diagnostics codes` by 1 024 bytes and `diagnostics unrecognized` by 5 360 bytes (§5.2 item 10); any other attribute by 64 bytes), so that the parser never accumulates an attribute beyond that bound; running count of decoded values; every list length checked against its declared size | no: always on |
| 4. Document storage budget | one store | three budgets of one configuration (below) | **yes** |
| 5. Platform | the JVM | the heap of the running process (F19); an `OutOfMemoryError` is never a control path: the budgets are chosen so that a full Open → Save → Reopen cycle stays within about one quarter of the smallest default heap (1 GiB); the R3 measurement supports this for the measured corpora and the full-allowance worst case is a C-4 gate (§14) | through the launcher |

**Storage budget (kind 4).** Configuration `cedg.references.store.budget`, read once per
process from the GeoCeDG preferences (with an explicit administrative override by system
property), with three values:

| Budget | Initial value (measured in R3) | Derivation |
|---|---|---|
| `maxReferences` | 10 000 | equals the importer's `SPLINE_ENTITIES_MAX`: the number of references is never bounded below the importer's |
| `maxValues` | 1 500 000 binary64 values | memory, not XML size (kind 5): measured minimum heap for a full Open → Save → Reopen cycle (previous document held, preflights without tessellation, live load with tessellation) of 192–256 MiB for a representative 1.5 M-value corpus and at most 256 MiB for 10 000 references of 1.5 M values with short strings, i.e. within one quarter of a 1 GiB heap for these corpora, which reach 100 % of `maxValues` and 77 % and 91 % of `maxEntryBytes` (R3 report §3); the worst admissible case, 10 000 references at `maxValues` with the whole string allowance used (100 % of `maxEntryBytes`), was **not** measured and is a closeout gate of C-4 (§14) |
| `maxEntryBytes` | derived: `17 × maxValues + 1 KiB × maxReferences` (about 34.1 MiB) | every value costs exactly 17 bytes (§5.2); 1 KiB per reference is an **aggregate** allowance for the fixed attributes and ordinary strings (about 690 bytes per reference measured together) and the strings of all references, so that long strings cannot multiply the memory of the store; each string keeps its own limit (§5.2 item 6) |

**R3 measurement (M1 of the pre-adoption audit).** The value of R1, 3 000 000 values with `7 KiB × maxReferences`, was measured directly in R3 and was **not** confirmed: a representative
3 M-value corpus needs 448–512 MiB, and the worst admissible case (10 000 references, 3 M values,
every string at 1 024 bytes, 119 MB entry) needs 704–768 MiB for the full cycle, held 863 MiB of
a 1 GiB heap after a collection, and leaves less than 1.45 times margin before the rest of the
application. The string allowance alone added about 256 MiB. The initial values are therefore
1 500 000 values with an aggregate string allowance; 2 000 000 values (full-cycle need 256–320
MiB, margin 3.2 to 4 times) is a documented alternative above the quarter-heap criterion. The
values are measured on one workstation and with a probe, not with the product (R3 report §3);
they remain a configuration, not a universal limit. The measured 1.5 M-value corpora reach
77 % and 91 % of `maxEntryBytes`; the case at 100 % of the allowance (about 3.4 MB more string
bytes than the measured short-string corpus) is not measured, so the quarter-heap claim is
established only for the measured corpora. C-4 (§14) measures the cycle again on the
implementation, including that full-allowance case, as a closeout gate of `MAINT-A-DXF0-RC`;
a budget not confirmed there is revised on that evidence before the closeout. The
importer's own worst case (10 000 references of 1 024 control points, about 61 M values) does
not fit any budget sized for a default heap: such an import is rejected explicitly with the
values it would require, and a station with more memory can raise the budget.

**Author decision and counter of R4.** The author adopted 1 500 000 values for the design as
the provisional, configurable product value backed by the available measurements; 2 000 000
is not chosen without further evidence. The R3 probe counted `6 + K + (4 + e) · c` per
reference, without `planeZ` (its corpora carry no normal groups); the counter of §5.2 item 12
adds `1 + g` per reference. The R4 count is therefore never below the R3 count of the same
store: the measured 1.5 M-value corpora count `1 502 697` (R3-T, 2 716 references) and
`1 510 000` (R3-NS, 10 000 references) under item 12, and a store admitted at `maxValues`
under item 12 holds no more serialized values than they did. The memory measurements remain
an upper bound for the same composition. `maxEntryBytes` is unchanged; the measured entries
already serialized `planeZ`, and the attribute `normalGroups="none"` adds 6 bytes per reference
over the probe's `normal="none"` (60 000 bytes at 10 000 references, about 0.17 % of
`maxEntryBytes`, inside the 1 KiB aggregate allowance), so R3-T stays at 76.6 % and R3-NS
moves from 90.6 % to 90.7 % of it. The correction of the counter introduces no geometric
restriction.

Rules:

- The budgets are **not** a product limit chosen from the size of the XML: they are the initial
  values of a documented configuration. Raising them is an explicit administrative action
  (preference or system property), never automatic, and reaches the reader as a finite value:
  no setting disables the safety limits of kind 3 or makes a read unbounded.
- Every mutation that grows the store (import, rename, preference change) is checked exactly
  against the budget before it is applied, because the serialized size is deterministic; an
  import that would exceed it is rejected as a whole before anything is added.
- A document whose store exceeds the budget of the station is rejected on opening with
  `STORE_BUDGET_EXCEEDED`, whose message states the required values and how to raise the
  budget; nothing is truncated, the file is never altered and the previous document stays.
- The disposable save and open preflights validate completely (kinds 1 and 3, §9) but do not
  tessellate; tessellation happens once, in the live load (it is 55 to 80 % of the read
  cost).
- The scanners of F17 recognize `geocedg_references.xml`, validate it under the same policy and
  raise their archive totals by the store budget; their 4 MiB entry constant no longer applies
  to it. The capacity of the product is never reduced to avoid updating them.

### 5.4 Empty store

No entry is written when the store is empty: a document without references keeps exactly the
current entry inventory and bytes.

## 6. Document identity of a reference

- **`id`:** 128 random bits from a cryptographically strong source, 32 lowercase hex, minted
  when the record is admitted into the store; not drawn from the spatial identity registry;
  unique in the store (redraw on collision at minting; `DUPLICATE_ID` on reading).
- **Never derived** from coordinates, data digests, DXF handle or layer, file name, labels,
  ordinals, XML position or order. Two imports of the same file yield different references.
  Equal data digests only produce the informative notice `SAME_DATA_AS_REFERENCE`.
- **Stability:** the `id` survives save, reopen, Save As and a file copy on disk; the scope is
  the document.
- **Semantic scope:** the `id` identifies external data of the document; it is not CeDG
  geometric identity, not `ceID`, not a graph node and never an input of a command.
- Every Desktop operation (show, hide, rename, remove, preference) addresses the reference by
  `id`.

## 7. Lifecycle

| Event | Behavior |
|---|---|
| Import | admitted records + new `id`s + `visible="true"` + `export="AUTO"`, within the budget; document marked modified; no Undo step; `Construction`, its XML and its Undo history unchanged |
| Show / hide / rename / preference | changes the store; document modified; no Undo step |
| Remove | explicit action with confirmation; when live constructions name the reference in their provenance, the confirmation says «N derived constructions will remain»; no Undo step (decision RC-D5) |
| Conversion or approximation (A-DXF1) | never changes the store, whether it succeeds or fails (D-R1-5) |
| Save / Save As | writes the entry when the store is not empty; the save preflight validates it |
| Reopen | store rebuilt from the entry without the DXF; tessellations recomputed; same `id`s, order, names, visibility and preferences |
| New, Reset without file, Close | cleared after the save prompt, which covers a modified store |
| Open another document, every replacement route | §7.1 |
| Undo / Redo | never adds, removes or changes references or preferences |
| Copy / paste of constructions | never transports references |
| Save As, file copy | copies the whole store with its ids |
| Duplicate a reference | not offered in `v1`; a later operation would mint a new `id` and start at `AUTO` |
| Insert File | the source store is validated by the source preflight (unknown version → insertion rejected, receiver unchanged); the source's references are **not** transferred (decision RC-D6), and an informative notice says how many were not inserted; the receiver store is unchanged |
| Clipboard-image insertion recovery | the live store never changed; whatever the reread prepared is discarded |
| Recovery snapshot, API `getGGBfile`/`getBase64` | include the entry: they are the complete document |
| GeoCeDG Classic | §7.2 |
| Change of construction unit | no rescale and no change of references (import §6) |

### 7.1 Atomic document transition

The incoming store is **prepared** without touching the current document, **verified**
completely while reading, and **installed** together with the new construction as one logical
document change; on any failure the previous document and its store remain or are fully
restored.

1. **Preparation.** The `readZip` branch decodes and validates the entry (§9) into a pending
   slot of the reading operation; an absent entry yields an explicit empty store. The pending
   slot is cleared at the start of every route and after every commit; XML-only routes
   (without archive) prepare an empty store or leave the store alone, as below.
2. **Commit.** The installation is a non-throwing reference swap, because the native commit
   hook runs after the protected block (F7). It happens in the document-transition hooks
   (`nativeDocumentLoadCommitted` through the session reset, and the startup workspace
   initialization for the first document), **never** in `commitLoadedDocument`, which also
   serves reloads that are not document changes (F9).
3. **Routes.**

| Route | Transaction | Rule |
|---|---|---|
| File → Open, Recent, file drop, API `openFile`, Reset with a file, `.html` with a file name, http(s) drop | native capture, preflight, restore (F7) | install the prepared store at the commit |
| Command line, shell, first document | blank start | install at the startup workspace initialization; the store field has no initializer (F10) |
| `.html` with embedded base64, `setBase64`, `loadURL` of archive bytes, `base64://` | none today (F8) | routed through the native transaction (one GeoCeDG-owned entry that preflights and loads like a native open); a partly replaced construction is impossible |
| API clearing `setXML` | none today | wrapped in the same capture and restore; installs an empty store on success |
| Non-API clearing `setXML` (tool creation, preference reload) | not a document change | leaves the store alone |
| New, Reset without file, Close | save prompt first | clear after the prompt |
| upstream `openURL` | clears before its dialog | stays unwired in GeoCeDG, or is overridden to clear only after success |
| Insert File, clipboard-image insertion, recovery snapshot | helper apps or snapshots | the live store is never changed |

C-1 (§14) is a mandatory entry condition of `MAINT-A-DXF0-RC`: every row above is proved with
a failure injected after parsing.

### 7.2 GeoCeDG Classic (author decision D-R1-8)

Classic preserves the store **opaquely**: in its non-GeoCeDG reading branch, the entry is read
under the safety limits and the budget of §5.3 (kinds 3 and 4; Classic, which has no GeoCeDG
preferences, uses the codec's default budget and the same administrative system property),
its bytes are kept without being interpreted as Classic geometry (Classic applies the byte-level subset of kind 3: the bounded read with its byte counter, the single entry, the root's declared `count`/`values` against the budget and the per-attribute bounds; it decodes no value and validates no record: digests, transformations and predicates are re-validated by GeoCeDG on reopening), and the writer writes them
back unchanged on every save route (Save, Save As, `.ggb`, base64 and HTML exports). An entry
that exceeds the budget or violates a safety limit makes Classic refuse to open the document
with a message, so that nothing can be lost afterwards. The bytes are dropped by New, `openURL`
and API document replacement, like the rest of the document. Classic's own native-open capture
carries them because it uses the same writer. Classic need not validate the content: GeoCeDG
re-validates fail-closed on reopening. The change stays in `MyXMLioJre`, already modified by CU4.

If `MAINT-A-DXF0-RC-S0` (C-3) shows that opaque preservation cannot be guaranteed without
altering the reader or writer beyond this rule, Classic instead refuses every save route of a
document whose archive contained the entry, with a message naming the reason; a save barrier
in `saveGeoGebraFile` alone is insufficient because base64 and HTML exports would still lose
the store. Silent loss is never accepted.

## 8. Saved state

Every store mutation marks the document modified (`setUnsaved`), and a non-empty store is
save-relevant document content (the hidden-layer pattern, F12), so New, Open, drop and Close
prompt before discarding references. A failed conversion or approximation never marks the
document modified.

## 9. Integrity

On reading, in this order, each failure with its typed cause (§10):

1. bounded inflation (§5.3, kind 3); the ZIP CRC is checked by the archive reader;
2. strict UTF-8 and grammar (§5.1); version first; `count` and `values` against the budget
   before allocation;
3. per reference: the `source` data satisfy the data predicates 4 to 12 of import §4.4
   (predicate 13 is represented by the stored finite `planeZ`) and `BSplineData` validation; effective weights and nature are rebuilt by import §4.6;
4. `T(source)` is recomputed with the arithmetic of import §6 (`(a·x + b·y) + e`, no FMA) and
   must equal the stored `data` **bit for bit** (`TRANSFORM_MISMATCH`); `T` finite and
   non-singular;
5. both digests are recomputed with `BSplineCanonicalEncoding.digestHex(
   "dxf-spline-reference/v1", …)` and must equal the stored ones (`DIGEST_MISMATCH`);
6. reference ids are unique (`DUPLICATE_ID`).

The check detects corruption and incoherence; it is not a signature. The stored copy is the
authority of the reference.

## 10. Failure model

| Cause | When |
|---|---|
| `UNSUPPORTED_STORE_VERSION` | `contract` other than `v1` |
| `UNKNOWN_GEOCEDG_ENTRY` | an unknown root entry `geocedg_*`, or a duplicated store entry (RC-D8) |
| `STORE_BUDGET_EXCEEDED` | `count`, `values` or bytes above the budget of the station (§5.3), including the byte counter of §5.3 kind 3 reaching `maxEntryBytes` plus one; the message states the required values when they are declared |
| `STRING_TOO_LONG` | a string above 1 024 bytes after escaping, on import or rename; on reading, a stored string above the limit is an attribute beyond its bound (§5.3 kind 3) and rejects the document with `MALFORMED_STORE` |
| `MALFORMED_STORE` | grammar, encoding, numbers, attributes, duplicates, list lengths, `w` incoherent with `weights`, an attribute beyond its bound (§5.3 kind 3), more decoded values or references than declared in `values` or `count` |
| `INADMISSIBLE_RECORD` | a record fails the data predicates of import §4.4 |
| `TRANSFORM_MISMATCH`, `DIGEST_MISMATCH`, `DUPLICATE_ID` | §9 |

Per route: **Open** (every route of §7.1): the preflight fails before the live document is
touched; if a failure came later, the captured archive is restored and the live store was
never changed; a localized message names the cause, and **Details…** the reference. **Save:**
the preflight rejects the bytes, the previous file stays unchanged. **Insert File:** insertion
rejected. **Undo, clipboard, redefinition:** they never read the entry. One invalid record
rejects the whole document; a subset is never loaded silently (an explicit «Open without
references» recovery action is a separate decision, RC-D7).

## 11. Compatibility

- **Older readers are not supported (D-R1-2).** GeoCeDG 1.2.2 and earlier open the
  construction, treat the entry as an unreadable image and **lose the references when they save
  again**. No forced-rejection marker is written in `geogebra.xml`. Release notes of the first
  version with `R-c` say: «Documents that contain saved DXF references require GeoCeDG ≥ X.Y;
  earlier versions ignore them and remove them when saving.»
- **Versions that declare `R-c`:** correct reading and writing; safe typed rejection of
  unknown versions, malformed data and over-budget stores; restoration of the previous
  document; no silent loss in any product route, Classic included (§7.2).
- **Old documents:** without the entry → empty store; no migration, no rewrite; their bytes and
  entry inventory do not change on reopen and save unless the user imports references.
- **Future versions:** a `v1` reader rejects `v2`.

## 12. Frontend obligations

- Reference manager, ordinary use (D-A-N2-5): recognizable name, nature, visibility,
  representation state from the export resolver, operations (show, hide, rename, remove,
  preference, convert/approximate when `MAINT-A-DXF1` exists), short notices. **Details…**:
  definition, provenance, transformation, identifiers, digests, diagnostics, budget use.
- The A-DXF0 «session only» notice at save is withdrawn; the save prompt covers references.
- Confirmation on removal with live derived constructions; open-failure message with the typed
  cause and, for `STORE_BUDGET_EXCEEDED`, the required values.
- Inspector text: «Imported reference, read-only; saved in the document».
- No change to tools, snapping, hit testing or layers (import §10).
- Availability and maturity (D-R1-10, reconciled with AGENTS §5; decision RC-D14):
  persistence is part of the existing import action (formally `stable`, bounded to `R-a` since
  A-DXF0-R1), with no activation flag of its own and no «experimental» wording. While
  `MAINT-A-DXF0-RC` is open the capability exists only on its branch and is not shipped, so
  no interim AGENTS §5 state is needed. It enters the ordinary profile only at the author's
  closeout, which extends the import's `stable` bound to `R-c`; before that nothing is called
  `stable`. The alternative AGENTS §5 route, `experimental` behind a flag, is the one D-R1-10
  excludes for mere novelty.

## 13. Kernel and upstream impact (AGENTS §9)

| Piece | Change | Ownership |
|---|---|---|
| `MyXMLioJre.readZip` | branch for `geocedg_references.xml`, the `geocedg_*` guard, bounded inflation, hand-over of a prepared store; Classic opaque bytes | upstream file already modified by CU4 |
| `MyXMLioJre.writeGeoGebraFile` | writes the entry the host provides (or Classic's opaque bytes), before `geogebra.xml` | upstream file already modified |
| store codec and policy | new, pure, in `org.geocedg.common.importer.dxf`; no `Construction`, `GeoElement` or kernel type | GeoCeDG |
| host interface | new, GeoCeDG; implemented by `AppGeoCeDG`; the preflight app only validates | GeoCeDG |
| `AppD` | one GeoCeDG-owned entry that sends archive-bytes routes through the native transaction; capture and restore around the API clearing `setXML` | upstream Desktop file, minimal |
| `AppGeoCeDG`, `GeoCeDGDxfReferenceSession` | ids, prepare and commit, saved state | GeoCeDG |
| inspection and angle-mode scanners, expert worker caps | recognize the entry; budgets | GeoCeDG |

**Unchanged:** `Construction`, `<construction>`, `MyXMLHandler`, `MyXMLio.getFullXML`,
`doParseXML`, the Undo managers, `SpatialIdentityRegistry`, command codecs, commands, Locus V2.

## 14. Required characterization (`MAINT-A-DXF0-RC-S0` before code; C-4 and the budget part of C-5 before the closeout of `MAINT-A-DXF0-RC`)

- **C-1** every route of §7.1 with a failure injected after parsing (mandatory entry
  condition);
- **C-2** isolated 1.2.2 with a store entry: opens, ignores, registers the empty image, drops
  on re-save;
- **C-3** Classic with the entry: opaque preservation on every save route, or the save barrier;
- **C-4** (a closeout gate of `MAINT-A-DXF0-RC`, run on its implementation, not in RC-S0) the
  full Open → Save → Reopen cycle with the previous document held, at 100, 1 000 and 10 000
  typical references and **at the budget**: a representative corpus, near-maximal references,
  and the worst admissible case, 10 000 references at `maxValues` with the whole aggregate
  string allowance used (an entry at 100 % of `maxEntryBytes`), which R3 did not measure; with
  time, retained and peak heap and a minimum-heap sweep up to 1 GiB, compared with the R3
  measurements; plus the reader-safety cases: an honest over-budget store, an under-declared
  store, an inflation bomb and a single attribute of maximal length within the entry budget.
  RC does not close until C-4 holds; a budget not confirmed is revised on that evidence before
  the closeout;
- **C-5** in two parts (R3): in `MAINT-A-DXF0-RC-S0`, the **current** behavior of the
  expert-inspection and angle-mode scanners, the expert worker and the recovery snapshot with
  a document carrying the entry (refusal or acceptance recorded per route, without product
  change); and, as a closeout gate of `MAINT-A-DXF0-RC` on its implementation, the same routes
  with stores at the budget: the entry recognized and validated under the policy, no 4 MiB
  refusal.

## 15. Validation

Cases RC-1 to RC-26 of the
[validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md).

## 16. Stop conditions

Stop if the implementation needs: references in `geogebra.xml` or in the construction XML; a
change to `MyXMLHandler`, Undo or spatial identity; ids minted by the construction registry; an
open route that cannot follow §7.1; Classic unable both to preserve and to block; a resource
policy that cannot reject an over-budget store without truncation.

## 17. Answers to `dxf-spline-reference.md` §12

| Point of §12 | Answer |
|---|---|
| 1. older and current readers with an unknown entry | F1, F3, F21; §11 |
| 2. entry name, versioned identifier, fail-closed rule | §4, §5.2, §10 |
| 3. stored data, authority of the copy | §5.2, §9; tessellation never stored |
| 4. save/reopen, mismatch with a file found again | §7; §0 |
| 5. Undo/Redo, copy/paste between documents, close without saving | §7, §7.1 |
| 6. size limits and integrity | §5.3, §9 |
| 7. exclusion from semantic exports and from the layer domain | semantic exports exclude references unless `cedg-reference-export/v1` is chosen; the store is never part of the layer domain |

## 18. Decisions

| Id | Question | Resolution | Status |
|---|---|---|---|
| RC-D1 | Container | ZIP entry `geocedg_references.xml` | proposed |
| RC-D2 | Identifier | `cedg-reference-store/v1`, fixed entry name | proposed |
| RC-D3 | Numbers | 16-hex bit patterns (measured: deflated size within 7 to 17 % of binary) | proposed |
| RC-D4 | GeoCeDG Classic | opaque preservation on every save route; save barrier on every route only if C-3 rules it out | fixed (D-R1-8) |
| RC-D5 | Removal | outside Undo, with confirmation | proposed |
| RC-D6 | Insert File | never transfer references | proposed |
| RC-D7 | Corrupt store | reject the whole document; «Open without references» as a separate increment | proposed |
| RC-D8 | `geocedg_*` guard, single entry | yes | proposed |
| RC-D9 | Capacity | no fixed product cap; configurable budget derived from measured memory (§5.3): 10 000 references, 1 500 000 values, entry bytes `17 × maxValues + 1 KiB × maxReferences`; streaming reader with per-attribute bounds | fixed in principle (D-R1-4); initial values measured in R3 (the R1 value 3 M not confirmed) and adopted by the author on 2026-11-26 as the provisional configurable product value (2 M not chosen without new evidence); re-measured by C-4 |
| RC-D10 | Reserved elements | `export` preference only; no derivation history | fixed (D-R1-5) |
| RC-D11 | Display name | stored only when the user renames | proposed |
| RC-D12 | `source` and `data` | both stored, verified bit for bit, `data` never recomputed in its place | fixed (D-R1-3) |
| RC-D13 | Normative form | this contract + amendments + ADR 0044 | proposed |
| RC-D14 | Maturity | no novelty flag; branch-only until the author's closeout, which extends the import's `stable` bound to `R-c` (§12) | no flag fixed (D-R1-10); the reconciliation with AGENTS §5 proposed |
| RC-D15 | Budget configuration surface | GeoCeDG preference with an administrative system-property override; finite values only | proposed |
| RC-D16 | Plane normal of the source | groups 210, 220 and 230 present and their presence (`normalGroups`, `normal`); effective normal derived, never stored; presence added to the import record by `MAINT-A-DXF0-RC` (§5.2 item 11, F22, A-IMP-12) | requirement fixed by the author (instruction R4 of 2026-11-26); the additive change of the import record that it needs is authorized by the author decision D-R4-1 of 2026-11-26, with six conditions (§5.2 item 11), for the scope of `MAINT-A-DXF0-RC` |
| RC-D17 | `values` counter | every serialized binary64 and nothing else, `v_r = K_r + (4 + e_r) · c_r + 7 + g_r` (§5.2 item 12) | fixed by the author (instruction R4 of 2026-11-26) |
