# POST-E3-LEGACY-INVENTORY-CASE-FIX — technical candidate report

```text
TASK                      = POST-E3-LEGACY-INVENTORY-CASE-FIX
OBSERVATION               = OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES
AUTHORIZATION             = BOUNDED CORRECTIVE IMPLEMENTATION AUTHORIZED — SUBJECT TO
                            READINESS GATE (author instructions of 2026-10-10)
BASELINE                  = 31286a2355cabebc69c3937442e7f15636da12ba
                            tree d223d7400b2eabacddafba874b49eae581c5c6c4 (published P_R6PLUS_E3)
VERIFICATION_CLASS        = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
                            (frozen at the readiness gate, before the first productive edit)
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this file
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved = false
```

Machine-readable mirror:
[`post-e3-legacy-inventory-case-fix-candidate-evidence.json`](../../geocedg/validation/post-e3/post-e3-legacy-inventory-case-fix-candidate-evidence.json).
Execution contract:
[canonical prompt](../../.github/prompts/tasks/post-e3-legacy-inventory-case-fix.prompt.md).

## 1. Result in brief

| Item | Result |
|---|---|
| Cause | `tools/legacy/ingest.ps1` keyed the per-macro label-to-type map with `@{}`, a case-insensitive hashtable. Labels differing only by case merged, and the last element in construction order won |
| Affected records | 24 of the 169 macro input/output records, in 8 of the 24 tools of `models/legacy/template-v7/derived/tool-inventory.yml`. The two scientific models have no macros |
| Correction | the map is `[hashtable]::new([StringComparer]::Ordinal)`: one line plus a comment, the least invasive supported mechanism (same type, case-sensitive keys) |
| Regenerated | the three authorized derived inventories, through the script's own `-Import` route. template-v7 gets 24 `type` values and `generator.sha256`. The scientific models get only `generator.sha256` |
| Historical integrity | every original byte-identical (SHA-256 before = after); manifests, curation, schema, curated GGT library, product code, command lookup and localization unchanged |
| Verification | the canonical legacy route passes before and after; `-Check` passes for all three; regeneration is byte-deterministic and idempotent; corrected types equal the source for 169/169 records. `STATIC` and `INFRA_UNIT` on the commit are reported outside this file |

## 2. Entry gate

```text
local main = origin/main = live remote main = 31286a2355cabebc69c3937442e7f15636da12ba
tree d223d7400b2eabacddafba874b49eae581c5c6c4; worktree clean
branch phase/post-e3-legacy-inventory-case-fix created from that commit
(independent of the other POST-E3 branches)
```

## 3. Readiness gate (completed before the first productive edit)

### 3.1 Collisions (gate items 1, 3)

A read-only scratch characterization (`post-e3-legacy-collisions.ps1`, on
copies of the originals) parsed `geogebra_macro.xml` of every registered legacy
original. For each macro input and output it compared the type obtained by the
case-insensitive map, the shipped behaviour, with the exact case-sensitive
type of the historical definition:

| Tool (legacy index) | Label collisions (examples) | Wrong input/output types |
|---|---|---|
| `sheetISOAnLand` (2) | `D:point/d:conic`, `E:point/e:conic`, `F:point/f:segment`, … | 11: input `D` conic→point; outputs `i`, `p`, `r`, `f_1`, `j_1`, `l_1`, `m_1` point→segment; `E` conic→point; `G`, `F` segment→point |
| `sheetISOAnVert` (3) | `D:point/d:conic`, `f':segment/F':point`, … | 6: input `D` conic→point; outputs `f'`, `n''`, `p''`, `r''`, `f''` point→segment |
| `directDimension` (4) | `g:line/G:point` | 1: output `g` point→line |
| `EllipseAxis` (7) | `C:point/c:conic` | 1: input `C` conic→point |
| `PoliLineVisibility` (9) | `F:point/f:segment` | 1: input `F` segment→point |
| `axisDimension` (11) | `f:line/F:point` | 1: input `f` point→line |
| `DuctSymbol` (13) | `f:segment/F:point` | 1: output `f` point→segment |
| `translationCoor` (19) | `f:line/F:point`, `i:line/I:point` | 2: inputs `f`, `i` point→line |
| `SquarebyDiagonal` (5), `conj2mainAxesEllipse` (22) | collisions only between non-I/O labels or equal types | 0 |

Total: 24 wrong types. The shipped inventory differed from the exact source
types in exactly these 24 of 169 records (`post-e3-legacy-compare.ps1`).

Other case-insensitive constructs on the same path were checked and have no
effect on the registered originals; they are recorded, not changed. They are
the zip-entry map (no entry names differ only by case), the
`Sort-Object -Unique` of command names (no command names differ only by case),
`$macroNames -contains` for tool dependencies (no false case-insensitive
match), and the tool-id map (slugs are lower case).

### 3.2 Affected derived records (gate item 2)

`generator.sha256` of every inventory is the normalized SHA-256 of `ingest.ps1`.
Any change to the script therefore changes that field in all three inventories,
the expected provenance update. The only other changes are the 24 `type` values
of template-v7. No other hash of these files or of the script is pinned
anywhere in the repository.

### 3.3 Boundaries (gate items 4, 5)

- Originals: `Templatev7.ggb` `f62e5b7a…4113`,
  `InterCilConoOblique.ggb` `b1cb614f…fa27`,
  `InterCilConoObliqueTwoLevels.ggb` `587328a8…b6b6`. Byte-identical before
  and after; never written (`-Import` refuses to overwrite a different original
  and found them identical).
- Manifests list the derived inventories by path and role only. Curation has no
  types. The inventory schema does not constrain `type`. No Java, verifier or
  registry consumer reads the inventories, apart from the legacy verifier
  (`tools/agent/verify-legacy.ps1`), which asserts counts, toolbar and locus
  maturity, not I/O types.
- The change is confined to the legacy ingest tool and the authorized derived
  metadata.

### 3.4 Frozen class (gate item 6)

`OPERATIONAL_VERIFICATION_INFRASTRUCTURE`: an operational legacy-ingest tool and
its derived data, with no product, test, registry or verifier change. The
acceptance is the focused operational evidence: the canonical legacy route,
`-Check` of every resource, idempotent regeneration and the record diff. The
contract also requires `STATIC` and `INFRA_UNIT`. No `FULL`, because the global
verification infrastructure is unchanged. `BOUNDED_PHASE` would have required a
new registered phase selection, which would have taken the change into the
registry, outside the bounded scope.

## 4. Correction

```diff
-                    $elementTypes = @{}
+                    # labels are case-sensitive in GeoGebra (D and d are different objects)
+                    $elementTypes = [hashtable]::new([StringComparer]::Ordinal)
```

`ContainsKey`, the indexer and assignment keep their semantics with ordinal
keys; nothing else in the script changed.

## 5. Verification evidence

| Check | Result |
|---|---|
| `ingest.ps1 -Import` then `-Check` then `-Import` again, per resource | exit 0 / 0 / 0 for all three; normalized inventory after the second import equals the first (idempotent, byte-deterministic) |
| corrected types vs historical source | 0 mismatches in 169 records (before: 24) |
| record diff | template-v7: 24 `type` lines and `generator.sha256`; scientific models: `generator.sha256` only. No label, tool, order or other metadata changed; 24 tools kept |
| `D` versus `d` (`sheetISOAnLand`) | input `D` = `point` (was `conic`); `d` stays a distinct `conic` element |
| `pwsh -File tools/agent/verify-legacy.ps1` (canonical legacy route) | exit 0 before and after ("G3 legacy integration verification passed"; identical log) |
| originals | SHA-256 unchanged |

Normalized inventory SHA-256, before → after:

```text
template-v7                         e6d9b751…c060 → 5eaa164c…7bdb
inter-cil-cono-oblique              dc038c86…f31b → bfb766ac…baec
inter-cil-cono-oblique-two-levels   7ec9659b…9c05 → 7f54ed00…8696
generator (normalized ingest.ps1)   77987a23…de80 → 941ee177…a21b
```

## 6. Changed paths

```text
M  tools/legacy/ingest.ps1
M  models/legacy/template-v7/derived/tool-inventory.yml
M  models/legacy/inter-cil-cono-oblique/derived/tool-inventory.yml
M  models/legacy/inter-cil-cono-oblique-two-levels/derived/tool-inventory.yml
A  docs/validation/post_e3_legacy_inventory_case_fix_candidate_report.md
A  geocedg/validation/post-e3/post-e3-legacy-inventory-case-fix-candidate-evidence.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md (this activity's status lines only)
```

`tools/legacy/` is GeoCeDG-owned, so no upstream provenance entry applies; the
inventories carry the generator hash as provenance. Historical documents that
cite the old wrong types, such as the `E3` preparation §K18 and §K20, record
past state and are not amended.

## 7. Author smoke (suggested)

1. Open `models/legacy/template-v7/derived/tool-inventory.yml`. In
   `sheetISOAnLand` and `sheetISOAnVert` the first input `D` is `point`;
   `EllipseAxis` input `C` is `point`.
2. Run `pwsh -File tools/legacy/ingest.ps1 -ResourceId cedg.legacy.template-v7
   -Source models/legacy/template-v7/original/Templatev7.ggb -TargetDirectory
   models/legacy/template-v7 -Check`. It reports "Legacy ingest check passed".
3. `git diff` of the three inventories shows only `type` and `generator.sha256`
   lines.

## 8. Final state

```text
POST-E3-LEGACY-INVENTORY-CASE-FIX = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES = CORRECTED IN THE LOCAL CANDIDATE
                                                   (author review pending)
PUBLICATION = NOT AUTHORIZED
selfApproved = false
```
