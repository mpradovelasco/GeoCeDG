# PRE-G9B-CU3 — Registro de cierre de `CU3-A`

| Campo | Valor |
|---|---|
| Carácter | Registro de cierre de `CU3-A`. Hace efectiva la aprobación autoral condicionada, tras comprobar sus condiciones (§4). No cierra `CU3-B` ni `CU3` global ni publica nada |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Resolución de OBS-01, cierre de CU3-A, mantenimiento del verificador y autorización de CU3-B1», de 2026-10-27, §1 a §3 |
| Candidato | `f61f295447d75755e4c932a0f8f41533563550dd` (árbol `875e1cbf5dca2b6d0bbea8b72d0c24ec3aa306c0`), sin cambios de producto en este cierre |
| Estado | `CU3-A` `PASS — AUTHOR APPROVED`, por la aprobación autoral condicionada de 2026-10-27 y el cumplimiento de sus condiciones; sin publicar |

## 1. OBS-CU3A-SMOKE-01: causa establecida

**Archivo del autor.**

- `C:\Temp\InterCilConoOblique.cedg`, el único modelo de esa carpeta.
- Es idéntico byte a byte al modelo versionado `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg`: SHA-256 `1AE6F51293FB111B54510948CBCC6E8BA38AB63B658BA3F8A668858AE890456C`, 58 896 bytes.
- Política `legacy-representation-authority/v1`, `rbCone = 5` (deslizador de 0,5 a 10).
- Trabajé siempre sobre copias, con una sonda externa de escritorio sobre el producto de `9f6f27f2`, cuyo Locus V2 es idéntico al de `f61f2954`.

**Cadena de dependencias.**

- `e_2 = LocusV2(R_1, E_1)`.
- El generador es `R_1 = translationCoor(J_1, O_1, k, Q_1, j_2)`, una macro.
- Entre sus ancestros está `N_1 = Point(o_1, text2, v_1)` (`AlgoSemanticLocusPoint2D`): el punto de `o_1` en el parámetro `v_1 = 0`, sobre la componente que designa el selector manual `text2`.
- `text2` es el selector principal `geocedg-locus-principal-selection/v1` de la componente `generator.main/component/[-π, π)/closed/open`, es decir, la curva cerrada completa de `o_1` sobre todo el dominio del conductor.

**Transición.** Es la misma cambiando `rbCone` con una orden algebraica, de forma progresiva (como con el deslizador) o con una actualización completa de la construcción:

| `rbCone` | `N_1` | causa de `N_1` | `R_1` | existencia certificada de `o_1` (`CU2`) |
|---|---|---|---|---|
| 5 a 7,57 | definido | `RESOLVED` (`RESOLVED_BY_DECOMPOSITION_EQUALITY`) | definido | una componente periódica cerrada en `[-π, π)`, `COMPLETE` |
| 7,58 a 7,60 | indefinido | `PRINCIPAL_BRANCH_UNRESOLVED` | indefinido | bandas no resueltas de 10⁻⁶ a 10⁻⁹ de ancho cerca de `t ≈ −0,04…−0,08`, `BUDGET_EXHAUSTED`; completitud `NOT_ESTABLISHED` |
| 8 a 10 | indefinido | `PRINCIPAL_BRANCH_UNRESOLVED` | indefinido | banda degenerada en `t ≈ ±0,328`: «Bounded-line membership is not decided» (paso 16, `RAY_PARAMETER`) |
| vuelta a 7,57 o menos | definido | `RESOLVED` | definido | componente única |

- **El primer ancestro indefinido es `N_1`.** Todo el resto de la cadena hasta `R_1` es propagación.
- **Sobre `e_2`.** Su definición semántica sigue `VALID` en el núcleo en todo el intervalo. Lo que el autor vio indefinido es su generador.

**Justificación matemática.**

- **La curva `o_1` sigue existiendo.** Las 720 muestras de su parámetro son válidas en todos los valores probados, y `t = 0` está en un intervalo certificado (por ejemplo `[−0,328, 0,328]` con `rbCone = 8`).
- **Lo que deja de establecerse** es que `o_1` sea **una sola componente cerrada**:
  - hacia 7,58, el certificador híbrido de `CU2` agota su presupuesto en una banda estrecha;
  - a partir de unos 7,6, el punto trazado `K_1` atraviesa el origen `I` de la semirrecta `n_1 = Ray(I, E_1)` en `t ≈ ±0,328`. Allí el parámetro de la semirrecta es exactamente 0, y la pertenencia a la semirrecta cerrada no puede decidirse con intervalos: el contrato de `CU2` la convierte en una banda que ninguna componente cruza.
- **Consecuencia para `N_1`.** Su selector designa precisamente la componente completa. El contrato aprobado solo resuelve un selector por una correspondencia estructural demostrada (`locus-v2-existence-components.md` §15.6), así que `N_1` falla cerrado con causa tipada.
- **Contraprueba.** El mismo punto definido sin selector de componente, `N_2 = Point(o_1, K)` (punto estructural de `CU3-A`, tras adoptar la autoridad constructiva), coincide con `N_1` en `rbCone = 5`. Además sigue definido y `STRICT` de 5 a 10 y de vuelta, mientras `N_1` falla desde 7,58.

**Clasificación.**

- **B: procedimiento constructivo del modelo.** El punto buscado sigue existiendo, pero el procedimiento de `N_1` (un selector manual de la componente cerrada completa) deja de ser válido cuando la existencia certificada no sostiene esa componente.
- **Base del núcleo, preexistente y conforme al contrato.**
  - El camino de certificación de `o_1` y de resolución del selector no ha cambiado desde `main` `e6c908f2` (`CU2` aprobado); `CU3` solo añadió accesores de lectura.
  - El documento del autor está bajo la política heredada, en la que nada de `CU3` decide.
- **No es una regresión** de `CU3-A`, T1 a T3, T6, T7-a ni D1-A.

**Corrección.**

- **Producto:** ninguna; no hay defecto respecto de los contratos aprobados.
- **Modelo:** el procedimiento robusto es `Point(o_1, K)`, que exige adoptar la autoridad constructiva. Hay que crearlo como punto nuevo, porque el núcleo rechaza redefinir `N_1` en su sitio, y rehacer sobre él la cadena que hoy cuelga de `N_1`. El original no se ha modificado.
- **Mejora posible de `CU2`** (decisión autoral futura, no autorizada aquí):
  - decidir de forma exacta la pertenencia de un punto al origen de una semirrecta, para que esa tangencia no corte la componente;
  - revisar el presupuesto del certificador híbrido.

  Las dos son reglas normativas nuevas.

## 2. OBS-CU3A-SMOKE-02

`OBS-CU3A-SMOKE-02 — CLOSED — AUTHOR ACCEPTED EXPLANATION`.

La adopción explícita se comprobó en una copia limpia (registro de D1-A y del smoke, §4). No hay cambios de producto.

## 3. Hechos del cierre

| Hecho | Estado |
|---|---|
| Candidato técnicamente aceptado | `f61f2954`: `INTEGRATED_PHASE` `ACCEPTED / COMPLETE` (`INTEGRATION` `verification-ef34ade20c5d44a79a93926dce2c2ee5`), autenticada ([registro de D1-A y del smoke](pre_g9b_cu3_a_d1a_and_smoke_record.md) §6) |
| Smoke autoral comunicado | `AUTHOR SMOKE — FUNCTIONALLY SATISFACTORY WITH TWO OPEN OBSERVATIONS`, sobre `9f6f27f2`, que difiere solo en la contabilidad D1-A, con resultados publicados idénticos |
| Observaciones | OBS-01 con causa establecida (§1); OBS-02 cerrada (§2) |
| Aprobación autoral | condicionada de 2026-10-27, efectiva por §4 |
| Publicación | ninguna: `main`, las etiquetas y `geocedg_book` no cambian |

## 4. Condiciones de la aprobación

| Condición | Cumplimiento |
|---|---|
| 1. Causa de OBS-01 rigurosamente establecida | sí, §1: primer ancestro indefinido, mecanismo, transición, contraprueba y preexistencia |
| 2. Defecto corregido dentro del alcance | no hay defecto del producto respecto de los contratos aprobados; la mejora de `CU2` exige una regla normativa nueva y queda para una decisión futura |
| 3. Garantías geométricas y de identidad | sin cambios de producto desde la verificación; fallo cerrado tipado |
| 4. Verificación del candidato definitivo | `INTEGRATED_PHASE` de `f61f2954` `ACCEPTED / COMPLETE` |
| 5. Sin regresiones bloqueantes conocidas | ninguna |
| 6. Comprobación manual por cambios funcionales | no procede: el producto no cambia por este cierre |

**Resultado: `CU3-A` `PASS — AUTHOR APPROVED`** sobre `f61f2954`, por la aprobación autoral
condicionada de 2026-10-27, cuyas condiciones se cumplen.

- Si el autor considera que la limitación preexistente de `CU2` (condición 2) exige corregirse antes
  del cierre, puede revocar este registro.
- Esta aprobación no implica la de `CU3-B` ni la de `CU3` global.
