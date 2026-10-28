# PRE-G9B-CU3 — Registro de cierre de `CU3-B1`

| Campo | Valor |
|---|---|
| Carácter | Registro de cierre de `CU3-B1`, efectivo por la decisión autoral de 2026-10-28 (§0). Es la única autoridad de ese estado. No cierra `CU3` global ni autoriza `CU3-B2` ni publica nada |
| Autoridad | decisión autoral «Aprobación y cierre de PRE-G9B-CU3-B1» (2026-10-28) |
| Producto | `dd3a2676d231f5c8b2c2bf0a9016eb6d8a5f7d2b` (árbol `f4cbd64e8c147977a5e417fa70182f3b893e6a87`) — [registro del candidato](pre_g9b_cu3_b1_candidate_record.md) |
| Descendiente verificado | `aabfd0b952838f964d2f2c25b3ebcba643935345` (árbol `c2b061ec0e6345147e561b93dbe0e8ef62898e8d`): solo pruebas, inventario y registro, con el producto byte a byte igual — [evidencia focal](pre_g9b_cu3_b1_focal_evidence_record.md) |
| Estado | `CU3-B1` `PASS — AUTHOR APPROVED`; `AUTHOR SMOKE — PASS`; `CU3-B2` no autorizado; `CU3` global sin `PASS — AUTHOR APPROVED`; nada publicado |
| Espejo | [`pre-g9b-cu3-b1-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-b1-closeout.json) |

## 0. Decisión autoral

Transcripción de la decisión autoral de 2026-10-28:

- **Smoke.** El autor completó el smoke manual con la guía corregida (`841d33b8`). Resultado: `AUTHOR SMOKE — PASS`, sin saltos indebidos entre raíces, sin pérdidas de definición ni de identidad tras guardar y reabrir, y sin comportamientos anómalos.
- **Aprobación.** `CU3-B1` `PASS — AUTHOR APPROVED` sobre el producto `dd3a2676` y su descendiente de pruebas verificado `aabfd0b9`, con la campaña `INTEGRATED_PHASE` `ACCEPTED / COMPLETE` ya autenticada.
- **Cuestiones no bloqueantes** (§2). No reabren `CU3-B1` salvo evidencia de incumplimiento normativo.
- **Verificaciones.** No se repiten verificaciones integradas por cambios solo documentales.
- **Alcance.** `CU3-B2` sigue sin autorización y `CU3` global, pendiente.
- **Publicación.** No se publican ramas, no se modifica `main` y no se crean etiquetas.

## 1. Hechos del cierre

| Hecho | Estado |
|---|---|
| Alcance | contactos anclados en los pliegues certificados de `CU2`, solo bajo `constructive-authority/v1` ([registro del candidato](pre_g9b_cu3_b1_candidate_record.md) §1) |
| Matriz B1-1 a B1-18 | cubierta con las pruebas de `PreG9BCu3FoldContactTest` y `PreG9BCu3FoldAnchorTransitionTest`, y con las suites que ya existían ([evidencia focal](pre_g9b_cu3_b1_focal_evidence_record.md) §1 a §4); B1-16 con la reserva del §2, punto 2 |
| Verificación del producto | `INTEGRATED_PHASE` de `dd3a2676` `ACCEPTED / COMPLETE`, autenticada (`INTEGRATION` `verification-07c07cc0f5b0419e8b13b1300181e28c`) |
| Verificación del descendiente | `INTEGRATED_PHASE` de `aabfd0b9` `ACCEPTED / COMPLETE`, autenticada (§3) |
| Rendimiento y corpus | equivalentes a `CU3-A`; el corpus del autor solo cambia en los contactos de `e_1` en sus pliegues |
| Smoke autoral | `AUTHOR SMOKE — PASS` (2026-10-28) |
| Publicación | ninguna: la rama remota sigue en `b1813da1`; `main`, las etiquetas y `geocedg_book` no cambian |

## 2. Cuestiones no bloqueantes registradas

| Id | Cuestión | Disposición |
|---|---|---|
| `DEBT-CU3B1-LEDGER-V6-HIGHWATER-IDEMPOTENCE` | Con entradas ancladas, `nextIncarnation` sube dos unidades en cada reapertura. Lo causan dos evaluaciones transitorias de la carga, sin vinculación de token único. Las entradas, los tokens y las reclamaciones no cambian, y las asignaciones posteriores son nuevas | deuda técnica de idempotencia y reproducibilidad de la persistencia v6. No se presupone que haya que cambiar el producto ahora |
| `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` | No está demostrada una redefinición efectiva del soporte del conductor que actúe como barrera de identidad conforme a ADR 0017. `N = Point(c3)` no llegó a aplicarse en el núcleo, ni con anclas ni con raíces transversales, así que ese caso no cuenta como superado | cobertura pendiente. B1-16 se apoya en la paridad de las redefiniciones del objetivo |

## 3. Reautenticación de la campaña del descendiente `aabfd0b9`

Comprobación con `Read-VerificationJson` y `Get-VerificationResultHash`: diario `COMPLETED`, commit y árbol exactos. No se repitió ninguna ejecución.

| Comprobación | `run_id` | Hojas | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-204b70f61f254392bbdafc7d1c6e0b9e` | 3/3 | `9720bab728ae88cc90ba6d564e27a1e03a47a3281c13394e17331fd314c743e8` |
| `INFRA_UNIT` | `verification-71180eb1bbe44ff29a1489a5b69b13d3` | 22/22 | `7778fc6d6db595c0420c3ed49e58c6f8f0f921b186da76c880813b760cbad85d` |
| `PHASE` `PRE-G9B-CU3` | `verification-4f4f39f4c5e54aff9c1897533cf22503` | 4/4 | `976fe160dfbb3d887f1d402b19d38d84074e0526dd10880bc904a0c00ccc1315` |
| `PHASE` `PRE-G9B-CU2` | `verification-3cd48c0f159d4e869dca563552d306fb` | 4/4 | `3e2b5028443036c42aee80f3e4515313cf247c53d2ccdd6070c6342983d46147` |
| `PHASE` `PRE-G9B-CU1` | `verification-b693714b720043e5a39dd52672bc1f4d` | 4/4 | `5f9654a3a19e31a752283d11ed2b890fe40dfc6db0a5d254ebe3cc1214267794` |
| `PHASE` `PRE-G9B-CU0` | `verification-f708b23b3e484e84820eabe00d6d3ff4` | 2/2 | `e237870dadbd21818097833f04a8ae8f43dedcb767335ef7df86565eff1e719e` |
| `INTEGRATION` | `verification-bf0133909f704f3091c305b4b31c2ee5` | 19/19 | `4547902025b465efaa9a247dba0330f26bde990a7c40889c4097f94c4510cce9` |

**Resultado: `CU3-B1` `PASS — AUTHOR APPROVED`** sobre el producto `dd3a2676` y el descendiente verificado `aabfd0b9`, por la decisión autoral de 2026-10-28.

- No implica `CU3` global.
- No autoriza `CU3-B2`.
