# PRE-G9B-CU3 — Registro del candidato `CU3-B1`: contactos anclados en pliegues certificados

| Campo | Valor |
|---|---|
| Carácter | Registro del candidato técnico `CU3-B1`, congelado y verificado. Queda pendiente de revisión y aceptación autoral. No aprueba nada |
| Autoridad | instrucción autoral de 2026-10-27 (R1–R6 y `CU3-B1`), ratificada por la decisión autoral «Cierre de CU3-A y continuación de CU3-B1» (2026-10-27); [diseño de la puerta](../architecture/pre_g9b_cu3_b1_fold_contact_design.md) (`83806038`) |
| Base | `CU3-A` `PASS — AUTHOR APPROVED` sobre `f61f2954` ([registro de cierre](pre_g9b_cu3_a_closeout_record.md), `4d48b641`) |
| Candidato | `dd3a2676d231f5c8b2c2bf0a9016eb6d8a5f7d2b` (árbol `f4cbd64e8c147977a5e417fa70182f3b893e6a87`) = `958aac7b` (implementación, pruebas e inventario, sobre `4d48b641`) + etiquetas de los tres diagnósticos |
| Estado | `CU3-B1 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `CU3-B2` no autorizado; `CU3` global sin `PASS — AUTHOR APPROVED`; `selfApproved=false`; nada publicado |

## 1. Alcance implementado

Todo el cambio actúa solo bajo `constructive-authority/v1`. Los documentos heredados no cambian.

**Ancla de pliegue** (`FoldContactAnchor2D`).

- La etiqueta exacta es (involución, punto fijo). Se decide con el encierro canónico de `t₀` de cada pliegue certificado de `CU2` (`fold-symmetry/v1:HORIZONTAL` → `t->-t`, puntos `0` y `pi`; `VERTICAL` → `t->pi-t`, `pi/2` y `-pi/2`).
- Se decide solo cuando el encierro toca exactamente un punto fijo; los dos distan `π`.

**Paso de pliegue** (`CertifiedContactAnalysis2D.anchorFolds`).

- Se activa en cada pliegue etiquetado con algún candidato cerca.
- Un certificado de conteo sobre una caja centrada en el punto fijo decide la forma.
- El extremo superior de la caja se desplaza `2⁻⁹` de su anchura. Así la bisección del punto crítico no cae en `t₀`, que es un punto crítico exacto de todo residuo en un pliegue.
- La imagen por la involución de cada raíz localizada debe caer en la caja (encierro exterior). Eso prueba que la raíz doble está en `t₀` y que las dos raíces simples están emparejadas.

| Forma en la caja | Resultado |
|---|---|
| `ONE_DOUBLE` en `t₀` | una solución anclada, `EXISTENCE_EXACT`, con la clase y el orden de la decisión exacta |
| `TWO_SIMPLE` emparejadas | una solución anclada en la preimagen canónica (la posterior al punto fijo), `EXISTENCE_CERTIFIED`; los dos candidatos transversales se retiran con `SUPERSEDED_BY_CERTIFIED_FOLD_PAIR`, sin fusión ni redirección |
| `NONE` | ninguna solución; diagnóstico `FOLD_CONTACT_LOST` |
| cualquier otra forma, imagen fuera de la caja o raíz que ningún binary64 verifica | ninguna ancla; diagnóstico `IDENTITY_DISCONTINUITY` con la razón; las raíces siguen las reglas transversales |

**Identidad.**

- **Germen:** `pre-g9b-cu3/fold-contact-germ/v1/` + `framed(linaje de la componente)` + `framed(framed(involución) + framed(punto fijo))`. No contiene evidencia, orden, intervalos ni parámetros.
- **Selector:** el selector R4 base, sin rango (`ofIntrinsicPhase` rechaza un germen de pliegue).
- **Elegibilidad:** el resolvedor admite el germen de pliegue sin vinculación heredada de token único. La guarda de topología observada de G9U0-R4 exceptúa las raíces ancladas, porque su selector es exacto.
- **Admisión:** una solución anclada es `STRICT` con evidencia exacta o certificada, y admisible para `Intersect(L, r, token)`. Las clases 3 a 5 no anclan.

**Persistencia.**

- El libro de tokens escribe la versión 6 solo cuando contiene una entrada anclada. Cada entrada lleva el discriminador `P` (par), `F` (ancla de pliegue) o `S` (raíz transversal).
- Las versiones 4 y 5 se escriben byte a byte como antes.
- Un estado 6 sin entrada anclada, una entrada `F` en la versión 5 o un tipo que no concuerda con su selector se rechazan cerrados. Una versión desconocida también, igual que los lectores anteriores rechazan la 6.

**Archivos.**

- Producto: `FoldContactAnchor2D` (nuevo), `CertifiedContactAnalysis2D`, `PublicTargetIntersectionCapability2D`, `IntersectionRootDeterministicSelector2D`, `PublicIntersectionRootIdentityResolver2D`, `LocusIntersectionTokenLedger2D` e `IntersectionSemanticMetadata2D` (tres códigos de diagnóstico).
- Los archivos con finales de línea mixtos se editaron línea a línea, conservando cada EOL.

## 2. Matriz de validación

| Fila | Caso | Prueba | Resultado |
|---|---|---|---|
| B1-1 | pareja de preimágenes con una sola identidad | `PreG9BCu3FoldContactTest.b1x1…` | una solución, germen `t->-t, 0`, `STRICT`, dos sustituciones |
| B1-2 | pareja → raíz doble | `b1x2to4…` | la raíz doble de clase 3 no ancla; el token reclamado queda latente, sin otro token |
| B1-3 | transición inversa | `b1x2to4…` | el mismo token vuelve al reaparecer la pareja |
| B1-4 | desaparición y reaparición | `b1x2to4…` | `NONE` sin solución; reactivación del mismo token |
| B1-5 | dos pliegues de una componente | `b1x5and6…` (réplica `e_1·c_1`) | dos anclas, `0` y `pi`, dos tokens, sin rango |
| B1-6 | costura | `b1x1…`, `b1x5and6…` | etiqueta `pi` en la costura |
| B1-7 | pérdida del certificado de simetría | `b1x7…` | sin pliegue no hay ancla; token latente, sin reasociación; vuelve con la simetría |
| B1-8 | ambigüedad | `b1x8…` | cuatro preimágenes cerca del pliegue: sin ancla ni sustitución |
| B1-9 | tokens transversales previos | `b1x9…` | la adopción sustituye los dos tokens; Deshacer los restaura |
| B1-10 | Deshacer y Rehacer | `b1x10…` | reclamación restaurada y retirada |
| B1-11 | guardado y reapertura | `b1x11to13…` | estado 6 con discriminador; las entradas sobreviven a la recarga |
| B1-12 | documentos heredados | `b1x12…` | sin anclas; libro en versión 4 |
| B1-13 | rechazo de la versión 6 | `b1x11to13…` | una entrada `F` en versión 5 y una versión desconocida se rechazan cerrados |
| B1-14 | copia y duplicación | — | sin prueba propia: el mecanismo de copia del libro (cierre fresco) no cambia y las entradas ancladas pasan por él; queda como cobertura indirecta |
| B1-15 | integridad y admisibilidad | `b1x2to4…`, `b1x5and6…` | admisible con clase 2 o con pareja certificada; clase 3 deja el token latente |
| B1-16 | redefinición en el DAG | — | sin prueba propia: las barreras de ADR 0017 no cambian y el contrato del libro las aplica igual a los selectores anclados |
| B1-17 | contactos de `CU3-A` | suites `StructuralPoint*` y `PreG9BCu3*` | sin cambios |
| B1-18 | intersecciones sin contacto | `G9U0R4TokenLedgerAllocationTest` (tokens dorados) | sin cambios |

**Pruebas adaptadas.** Afirmaban que un contacto no se admite antes de `CU3-B`, y pasan a que se admite solo cuando está anclado:

- `ConstructiveIntersectionAdmissionTest`, `ConstructiveExactContactTest` y `ConstructiveEarlyExactDecisionTest`;
- en escritorio, `PreG9BCu3SeamReclassificationAuthorModelTest`, con la inversión `B08_B09` → `CU3-B1`. La tangencia regular `o_1·h_2` (`CU3-B2`) sigue sin admitirse.

## 3. Verificación

**Primera campaña (`958aac7b`), detenida.**

- `PHASE` `PRE-G9B-CU1` (`verification-067c4beeb6b14a4eadd52a3d6120af14`) dio `REJECTED_SEMANTIC`: `G9U0LocalizationHelpTest.l02` exige una etiqueta `LocusV2.Results.Value` para cada código de diagnóstico.
- Las etiquetas se añadieron en inglés y en español, y la nota correspondiente en el manifiesto upstream (`dd3a2676`).
- Se detuvo `INTEGRATION` y no se reutiliza ninguna evidencia de `958aac7b`.

**Campaña `INTEGRATED_PHASE` de `dd3a2676`.**

- Todas las ejecuciones fueron `ACCEPTED / COMPLETE` en la primera ejecución.
- Se reautenticaron con `Read-VerificationJson` y `Get-VerificationResultHash`: diario `COMPLETED`, commit y árbol exactos.
- `INFRA_UNIT` se ejecutó con PowerShell 7.6.9 del sistema, ya con la corrección del verificador incorporada.

| Comprobación | `run_id` | Hojas | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-13e67033c5af44a1abb6824b46ffb8bc` | 3/3 | `018c6a2cde3bd33a4c44effb566e8ae60512d2efcc86ebb0634f56b5d2a99870` |
| `INFRA_UNIT` | `verification-19d79c5ece8c486688fde08b0d5ae22e` | 22/22 | `4f492c26539e2aa2e1e0f401d7b99d94bca0ef636d8a228850dd22f96d1f27ba` |
| `PHASE` `PRE-G9B-CU3` | `verification-79da138dbfc644c2ae5e77905c49fed5` | 4/4 | `85c40ec8e71b9fcb0cd59c9964fa62752fe50398b51955306bf66bb10df04f26` |
| `PHASE` `PRE-G9B-CU2` | `verification-e8a2dd11cf5749c39be18a6c7499c8d1` | 4/4 | `fcb6f686f1e565a8dd258f5800c15b3148ebdf8773aedc10bd5d310ef7e08be8` |
| `PHASE` `PRE-G9B-CU1` | `verification-1e7389e1b1fc4d65a92485f73d97d2e8` | 4/4 | `852f05c80cb8af547ddf808d1f87ac3ba26106da6bc75453b6eacfb43e835a1e` |
| `PHASE` `PRE-G9B-CU0` | `verification-aea219d736b94fb59e68e8b3190101ca` | 2/2 | `b4ccf6bacfcfacd95c90a71ce88470d2012dac18bfca896c0c87703547c53594` |
| `INTEGRATION` | `verification-07c07cc0f5b0419e8b13b1300181e28c` | 19/19 | `09d0dc2667cfd435dba95eba53b853244edd94264c3887a508f2f41292f61169` |

**Inventario.**

- `pre-g9b-cu3.shared` pasa de 223 a 232 identidades (nueve pruebas nuevas).
- `discovery.*` y `final.shared` se renovaron con evidencia ejecutada.
- Pin `junit_inventory` `8c8556d6…` y recuento de `verification-final-coverage.Tests.ps1`.
- `final.desktop` no cambia, porque no se añadió ni renombró ninguna prueba de escritorio.

## 4. Rendimiento

Producto del candidato, sin sombra, tres ejecuciones en la misma estación que `f61f2954`. Arrastres bajo la autoridad constructiva; valores en ms (mediana / percentil 95). Salidas en `artifacts/benchmarks/pre-g9b-cu/cu3-b1-candidate-dd3a2676/`, sin seguimiento.

| Ejecución | E3 `H` | E3 `B` | Smoke `H` | Smoke `B` | Primer arrastre del smoke |
|---|---|---|---|---|---|
| A | 51,5 / 61,9 | 46,4 / 54,6 | 74,3 / 118,9 | 74,1 / 84,0 | 1,78 s |
| B | 47,3 / 52,6 | 44,7 / 48,5 | 68,5 / 79,4 | 69,4 / 71,2 | 1,53 s |
| C | 45,9 / 53,4 | 45,9 / 49,2 | 70,3 / 79,2 | 70,0 / 78,5 | 1,50 s |
| `f61f2954` (`CU3-A`) | 43,7–49,2 | 42,6–45,5 | 65,7–76,0 | 67,4–71,0 | 1,51–1,68 s |

- **Comparación.** El rendimiento es equivalente al de `CU3-A`, dentro de la dispersión ya observada. El arrastre de `H` en E3 queda unos 2–3 ms por encima en la mediana: es el paso de pliegue, que solo actúa cerca de los pliegues.
- **Transición pareja ↔ contacto.** No tiene banco propio. Las pruebas `b1x2to4` y `b1x10` la recorren sin coste apreciable.

**Corpus del modelo del autor** (`cu3-t7a-corpus`, 14 estados del smoke adoptado), comparado con `f61f2954` módulo hashes, identificadores y tokens:

- los puntos estructurales `X1`–`X5` de `CU3-A` son idénticos (B1-17);
- `u_1 = Intersect(o_1, h_2)` es idéntico: la tangencia regular sigue fuera de `CU3-B1`;
- cambian solo los resultados ricos de los contactos de `e_1` en sus pliegues, que pasan a anclados y admisibles.

## 5. Limitaciones

- **El ancla es local a la caja del certificado** (una celda de muestreo alrededor del pliegue). Cuando la pareja sale de ella, las raíces siguen las reglas transversales con tokens propios, y el token anclado queda latente.
- **Raíces dobles de clase 2.** En la réplica, el contacto de clase 2 solo aparece en los contactos estructurales del autor (`e_1·c_1`, `e_1·n`). Un objetivo libre que pasa por el pliegue da clase 3 en la raíz doble. La transición pareja ↔ doble con raíz de clase 2 no se puede producir moviendo un objetivo libre.
- **Tokens no reclamados.** No se conservan latentes (ADR 0017). Las transiciones conservan el token cuando hay un punto que lo reclama.
- **B1-14 y B1-16** no tienen prueba propia (§2).

## 6. Guía del smoke (focal)

Usa una copia de `C:\Temp\InterCilConoOblique-K1-contragiro.cedg` o del modelo original.

1. Adopta la autoridad constructiva al primer `Intersect(e_1, c_1)` (Deshacer debe quitarla).
2. Comprueba que `Intersect(e_1, c_1)` publica la costura y el contacto en `t = 0`, ambos admisibles. Materializa uno con `Intersect(e_1, c_1, <token>)`.
3. Mueve `rbCone` y `α_cone` dentro del rango del deslizador: el punto materializado sigue al contacto o queda indefinido sin saltar a otra raíz.
4. Guarda, cierra y reabre: el punto conserva su definición.
5. Deshacer de la adopción: los resultados heredados vuelven sin cambios.

## 7. Decisiones autorales solicitadas

1. Aceptación del candidato `CU3-B1` `dd3a2676`.
2. Disposición de las limitaciones del §5 y de las filas B1-14 y B1-16 sin prueba propia.
3. Publicación de la rama, si se desea (no autorizada).
