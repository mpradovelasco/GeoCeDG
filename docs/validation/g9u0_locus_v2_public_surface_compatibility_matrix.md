# G9U0 Locus V2 compatibility matrix

- Status: **G9U0 = PASS — AUTHOR APPROVED / FROZEN CORPUS / HOST TESTS PASSED /
  EXTERNAL UPSTREAM RUNTIME NOT EXECUTED**
- Machine corpus:
  `../../geocedg/validation/locus-v2/g9u0/g9u0-compatibility-corpus.json`

| Class | Frozen entries | Required disposition |
|---|---:|---|
| Immutable G9P GGB controls | 4 | no legacy migration or result change |
| G6 nested legacy-Locus controls | 2 | legacy behavior retained |
| Public-provider/query coverage manifest | 1 | declarative coverage only; live commands create native XML in tests |
| Hostile/future native declarations | 5 | explicit unsupported/broken status; no inferred repair |
| External-upstream boundary fixture | 1 | unsupported-open warning and zero lossy conversion |
| **Total** | **13** | exact paths and hashes frozen with the candidate source |

## Fixture status

The provider/query entry is explicitly a declarative coverage manifest, not a
native GeoGebra construction. Executable tests create native constructions
through the selected public commands and exercise the live GeoGebra XML
writer/reader. The hostile inputs are injected into that live grammar. Thus
production XML remains the sole serialization authority, and the corpus does
not claim a native persisted model from metadata-only XML. The current text
bytes and their canonical-LF hashes are frozen. Focused P01–P13 and P15–P16 host
tests passed save/reopen, copy, undo/redo, old-file, fork-Classic and hostile
input behavior. P14 passed the GeoCeDG unsupported-boundary/warning and zero-
downgrade contract; it did not execute an external upstream binary.

## Execution evidence

The focused and deterministic verifier runs each passed all 16 compatibility
scenarios as part of the exact 93/93 G9U0 total. The composed authority also
passed without `-SkipBuild`. Evidence directories are:

- `artifacts/g9u0/candidate/focused-final-pass-escalated`;
- `artifacts/g9u0/candidate/focused-deterministic-pass-escalated`;
- `artifacts/g9u0/candidate/composed-final-pass`.

These paths retain the original implementation-candidate execution provenance.
The author reviewed that evidence and approved G9U0 as PASS; the surface remains
experimental and default-off.

## Required hostile cases

The corpus covers future provider version, missing generator ID,
missing support reference, missing token lineage, duplicate durable ID and the
external-upstream no-downgrade boundary. Corrupt fixtures preserve exact bad
input; tests must not rewrite them to make loading pass.

## Compatibility meanings

“Classic” means the GeoCeDG fork Classic diagnostic path using the same shared
kernel. It must preserve supported native types while interactive creation is
disabled. It does not mean an external upstream GeoGebra binary. No external
runtime result is claimed until such a runtime is explicitly executed and
recorded.

No G9X1, G9U1, G9B, G9C, G9U2, G10 or later implementation was executed.
