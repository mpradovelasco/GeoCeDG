# GeoCeDG user documentation — index

This path is the stable entry point to the GeoCeDG user documentation. It is an
index, not a manual: it holds no product statement of its own, so that there is
exactly one authority per document class and no third, divergent user manual.

## Official user guides

The user-facing behaviour of GeoCeDG 1.0.0 is documented in two official
editions with identical structure, identical examples and the same stable
section identifiers:

| Edition | File |
|---|---|
| English | [GeoCeDG user guide](geocedg_user_guide_en.md) |
| Español | [Guía de usuario de GeoCeDG](geocedg_user_guide_es.md) |

The application opens the edition matching the active product language through
**Help → GeoCeDG user guide** / **Ayuda → Guía de usuario de GeoCeDG**, with
English as the fallback.

## Other user documentation

- [Mathematical reference](geocedg_mathematical_reference.md) — explanatory
  mathematics linked to the governing normative specifications.

## Operational and developer documentation

- [Operations manual](../developer/geocedg_operations_manual.md) — workstation
  requirements, clone preparation, build, packaging, installation, application
  identification and technical references. This material was relocated from this
  path when the bilingual user guides were introduced.
- [Developer guide](../developer/geocedg_developer_guide.md) — implementation
  internals, kernel extension process and verification usage.
- [Agent prompt guide](../developer/geocedg_agent_prompt_guide.md).

## Authority

Phase status, gates and authorization live in the
[living roadmap](../roadmap/geocedg_roadmap.md). Normative geometric and
operational contracts live in `geocedg/specs/` and the accepted ADRs under
`docs/adr/`. Historical execution evidence lives in `docs/validation/` and
`geocedg/validation/` and is not rewritten by later phases.

## Superseded

- [Construction quick guide](geocedg_construction_quick_guide.md) — legacy;
  superseded by the two official user guides.
