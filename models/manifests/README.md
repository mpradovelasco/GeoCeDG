# Model manifest registry

- `model-manifest.template.yml` is a non-importable template.
- `catalog.yml` lists imported model manifest paths.
- The governing contract is
  `geocedg/specs/operations/model-manifest.schema.json` plus
  `geocedg/specs/operations/manifest-contracts.md`.

Copying the template is not sufficient to approve a model. Replace every
placeholder, set `template` to `false`, provide the model asset and license
evidence, and pass the relevant geometric specification and regression gates.

Legacy packages additionally separate `original/`, curation, and derived
inventory as defined by `geocedg/specs/legacy/controlled-integration.md`.
