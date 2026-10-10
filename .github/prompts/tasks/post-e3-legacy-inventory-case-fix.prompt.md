# POST-E3-LEGACY-INVENTORY-CASE-FIX — case-sensitive labels in the legacy tool inventory

**CANONICAL EXECUTION PROMPT — PREPARED. BOUNDED CORRECTIVE IMPLEMENTATION
AUTHORIZED, SUBJECT TO ITS CHARACTERIZATION AND READINESS GATE; NO AUTOMATIC
PUBLICATION.**

The author's instruction of 2026-10-10 (`PRE-G9B-R6-plus-E3-AUTHOR-CLOSEOUT`,
with its clarification of the same day) authorizes this bounded correction and
orders that it run later as its own task and branch, preferably after the
publication of the `E3`/`E3-R1` closeout. The authorization is recorded,
versioned, in the
[E3 and E3-R1 closeout record](../../../docs/validation/pre_g9b_r6_plus_e3_closeout_record.md)
§8. Where this prompt and that record differ, the record prevails.

```text
POST-E3-LEGACY-INVENTORY-CASE-FIX =
PREPARED — BOUNDED IMPLEMENTATION AUTHORIZED — SUBJECT TO READINESS GATE

observation              = OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES
selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (bounded; only after the readiness gate below)
passClaimed              = false
PHASE_KIND               = OPERATIONAL TOOL CORRECTION — LEGACY INGEST AND DERIVED INVENTORIES
CHANGE_ROUTE             = ORDINARY
VERIFICATION_CLASS       = to be characterized and frozen before the first productive edit
                           (template default BOUNDED_PHASE)
ORDER                    = 3 of the three POST-E3 activities
```

The observation as recorded by the `E3` preparation (§K20) and the `E3` author
decisions: `models/legacy/template-v7/derived/tool-inventory.yml` records wrong
macro input/output types (for example `D` as `conic`) because
`tools/legacy/ingest.ps1` keys the per-macro map `$elementTypes = @{}` by
element label; PowerShell's default hashtable compares string keys
case-insensitively, so labels that differ only by case (`D` and `d`) merge and
the last element wins.

<!-- geocedg-field: objective -->
## Objective

1. Readiness gate (before any productive edit): characterize the complete set
   of affected labels in every inventory that `ingest.ps1` derives
   (`models/legacy/template-v7/`, `models/legacy/inter-cil-cono-oblique/`,
   `models/legacy/inter-cil-cono-oblique-two-levels/`), the exact impact of
   regeneration on each derived file, and any other case-insensitive construct
   on the same path (for example `Sort-Object -Unique`), recording but not
   fixing those outside the label lookup; verify that no broader governance,
   schema, manifest or immutable-baseline contract is affected; choose and
   freeze `VERIFICATION_CLASS`.
2. Correction: make the label lookup case-sensitive with the least invasive
   supported PowerShell mechanism (for example an ordinal
   `[System.Collections.Generic.Dictionary[string,string]]` with
   `[StringComparer]::Ordinal`), keeping every other behaviour of the script.
3. Regenerate only the authorized derived inventories with the script's own
   import route and confirm its check route.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the author when starting the task: the
published `E3`/`E3-R1` closeout on `main` (preferred) or, failing that, the
local closeout commit; record it at entry and stop on a mismatch. The approved
`E3-R1` candidate `7cd501167c856eef356888ca87ed3a0c97cea55c` (tree
`3c51c9d3635c22939df6cab60259521f885eff92`) and the published pre-`E3` baseline
`ff56099184544e5988c63e0ea3339e3d0fe01fac` contain the same `ingest.ps1` and
derived inventories as the base unless the entry check shows otherwise.

## Authority and evidence hierarchy

`AGENTS.md` (controlled legacy integration); ADR 0003; the legacy manifests
and `geocedg/specs/operations/legacy-tool-inventory.schema.json`;
`tools/agent/verify-legacy.ps1`; the `E3` preparation §K20; then probe
evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- `tools/legacy/ingest.ps1`: the label-to-type lookup only.
- Regenerated derived inventories: `models/legacy/*/derived/tool-inventory.yml`,
  limited to the records the readiness gate shows the correction changes.
- Focused tests or verifier pins that the change makes necessary, named by the
  readiness gate before editing.
- New documentation: a characterization and candidate report under
  `docs/validation/`, its JSON mirror under `geocedg/validation/post-e3/`,
  provenance where the repository requires it, roadmap and mini-track status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- `models/legacy/template-v7/original/Templatev7.ggb`, every file under
  `models/legacy/*/original/`, and every historical geometry or macro
  definition: immutable.
- Manifests and curation files, unless the readiness gate proves a required,
  bounded update and the author confirms it.
- Product code, the GGT library, kernel, command lookup and localization.
- The other POST-E3 activities; F1, F2, F3, F4 and G.

## Architectural placement

The defect lives in the legacy ingest tool (`tools/legacy/ingest.ps1`), which
owns the derived inventories; the fix belongs there and nowhere else.

## Required design/specification

The legacy tool inventory schema and the controlled-legacy-integration ADR
govern the inventory contents; no schema change is authorized.

## Geometric invariants and degeneracies

Not applicable: the task changes type metadata of an inventory, not geometry.

## Compatibility and serialization

Inventories keep their schema and record order. Records not affected by the
case collision stay byte-identical. Regeneration is deterministic and
idempotent.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- Before/after inventories with an exact record-level diff, showing correct
  input and output types, exact label preservation, the `D` versus `d` case, no
  metadata loss and unchanged unaffected records.
- Idempotent regeneration: two consecutive regenerations give identical bytes;
  the script's check route passes.
- `tools/agent/verify-legacy.ps1` and the canonical `tools/agent/verify.ps1`
  profiles of the frozen class (at least `STATIC` and `INFRA_UNIT`), on the
  committed candidate, with exact commands, exit codes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: the readiness gate, the bounded correction and regeneration above,
their tests and the local technical candidate. Not authorized: anything beyond
the label lookup and its derived records, self-approval, or a correction that
requires changing a historical original.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local candidate commit is authorized. Push, merge, fast-forward of `main`,
tag and release are not; publication needs a separate author instruction after
review.

## Acceptance and closeout

The task stops with one frozen technical candidate pending author review; its
report and mirror record `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`.

## Required artifacts

The readiness-gate characterization (affected labels per inventory, regeneration
impact, frozen class), the candidate report with the record-level diff and the
idempotence evidence, provenance, exact verification evidence and the final
status block.

## Stop conditions

- Base identity differs from the author's instruction, or the tree is dirty at
  entry.
- The correction would require changing a historical original, a manifest
  checksum of an original, or the schema.
- The change cannot stay bounded to the label lookup and its derived records.
- A broader governance or immutable-baseline contract is affected.
- Regeneration is not deterministic or changes unaffected records.
