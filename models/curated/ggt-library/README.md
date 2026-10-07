# Curated GGT library

GeoCeDG-owned **derived** user-tool definitions (`PRE-G9B-R6-plus-E1-L`). The
contract is [`geocedg/specs/legacy/curated-ggt-library.md`](../../../geocedg/specs/legacy/curated-ggt-library.md).

| Path | Kind |
|---|---|
| `curation.yml` | authored curation record (selection, provenance evidence, style policy, recorded toolchain) |
| `library-manifest.json` | **generated**; never hand-edit |
| `tools/<command>.ggt` | **generated**; never hand-edit; binary in `.gitattributes` |

The provenance source is the immutable
`models/legacy/template-v7/original/Templatev7.ggb`, which this library only
reads. The owned icon sources are under `geocedg/resources/icons/ggt-library/`.
Rights and licensing are recorded in
[`docs/licensing/curated-ggt-library-rights-record.md`](../../../docs/licensing/curated-ggt-library-rights-record.md);
geometric equivalence with the template is not redistribution evidence.

Regenerate or verify with `tools/legacy/curate-ggt-library.ps1 [-VerifyOnly]`.
This directory is not a packaging input: only `PRE-G9B-R6-plus-E1-P`, when
authorized, may stage it into an installation.
