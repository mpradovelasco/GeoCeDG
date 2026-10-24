# PRE-G9B-CU3 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Decisiones que requieren intervención autoral para aprobar el diseño de `CU3` y, en su caso, autorizar su implementación. No registra ninguna decisión tomada por el agente |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisada por la revisión científica focal de 2026-10-24; `selfApproved=false` |
| Expediente | [informe del candidato de preparación](pre_g9b_cu3_preparation_candidate_report.md), §11 (revisión focal) |
| Sustituye | las decisiones F-1, F-2, H-2 y H-3 de la [hoja de `CU0`](pre_g9b_cu0_author_decision_sheet.md) §4, que se conservan como antecedente; H-1 sigue en esa hoja |

Cada decisión indica su recomendación (**R**). Las decisiones marcadas con † cambiaron en la
revisión focal; §7 reúne la recomendación consolidada. Las decisiones menores de organización y
documentación no se elevan.

## 1. Etapa A — puntos estructurales

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-1** | Formas públicas (F-1-R) | **A**: `Point(L, D)` e `Intersect(L, r, D)`, con `D` documentado como correspondencia del conductor y nunca como punto inicial. B: `Point(L, D)` y un comando propio para la incidencia. C: solo `Point(L, D)` | **A** ([Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md) §3) |
| **D-CU3-2** † | Clases de procedencia de `D` (hecho 1) | tabla cerrada y versionada: `PATH_POINT`, `DEFINING_POINT` e `INTERSECTION_OUTPUT` (lista blanca de algoritmos heredados con el soporte entre sus entradas); el punto exacto de una intersección se selecciona por la forma exacta de la regla de selección heredada | adoptar la tabla (Etapa A §2.1) |
| **D-CU3-3** † | Correspondencia `D ↦ t(D)` (hecho 3) | correspondencia **exacta** `t*(D)` en una configuración declarada (`Σ_L` para `Point`, `Σ_B` para la incidencia): exacta por definición para `PATH_POINT` y `DEFINING_POINT`, y **encerrada** para `INTERSECTION_OUTPUT` desde un encierro certificado del punto exacto, nunca desde sus coordenadas binary64. Valor **numérico** `t̃(D)` (vía a) solo para la posición mostrada. Fallo cerrado en extremos, costuras, bordes de componente, transiciones, bandas sin resolver, huecos y componentes puntuales. Vía (b), mover el conductor, rechazada | adoptar (Etapa A §2.3) |
| **D-CU3-4** † | Autoridad de la segunda incidencia y M3 | **M1 ∪ M3**. M3 es la **dirección arquitectónica candidata del núcleo**: evaluación exacta en la configuración conjunta `Σ_B`, recalculada desde una frontera de captura común (evita la ruptura por redondeo de los objetos intermedios: `K.x = fl(B.x + 5)`), con `D*` reconstruido desde su procedencia y su selección heredada. Lleva una **puerta temprana y bloqueante** G-M3 (viabilidad matemática, presupuesto, correspondencia con el algoritmo vivo). Si la puerta falla: alternativas R-1 (solo `Point`), R-2 (otro método exacto) o R-3 (aplazar con una limitación documentada); **nunca** un residuo tolerado | **M1 ∪ M3** con la puerta G-M3 (Etapa A §6) |

## 2. Etapa B — evidencia de contacto

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-5** † | Materialización (F-2-R, sustituye a F-2) | **A**: solo se materializan como intersecciones las clases con existencia establecida (1 y 2 para los contactos sin cambio de signo); las clases 3 a 5 se conservan en el resultado rico y como diagnóstico. B: además, un objeto público de punto crítico. C: la clase 3 como intersección (**rechazada**). Con A, «established» en la fila de raíces pares de G8B-R1 significa clase 1 o 2: es una precisión normativa identificada, sin impacto en el producto actual | **A** para `CU3`; B como opción futura, no incorporada ([Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §5) |
| **D-CU3-10** | Raíz de la costura de `e_1·c_1` | el plan exige «la raíz transversal de la costura se conserva», y la línea base la fija como `TRANSVERSE_ESTABLISHED` y admisible; la caracterización prueba que es un contacto con cero doble en el pliegue de `t = π` (H-CU3-2). Alternativas: **reformular** el criterio («el punto de contacto de la costura se conserva y se materializa como contacto estructural, con su clase corregida») o conservarlo como transversal (**contrario a §5.1**) | **reformular**; el punto `preserve` no cambia y el pin de clasificación se invierte |
| **D-CU3-11** † | Germen tangente, ciclo de vida y pliegues | solo para la materialización sin conductor (`CU3-B`): germen `…/tangent-contact-germ/v1`, un formato nuevo del libro de tokens, el ciclo de vida, y en los pliegues una identidad para la pareja de preimágenes; alcance de recta, segmento, rayo y circunferencia | como se propone, dentro de `CU3-B` |
| **D-CU3-18** | Precisión científica sobre B08 y B09 (H-CU3-7) | los contactos de `e_1` con `n` y con `c_1`, y los de la costura, son contactos en un pliegue del trazado, no tangencias; solo `o_1·h_2` es una tangencia | adoptar la precisión en el plan vivo y en las normas candidatas |

## 3. Admisibilidad, H-2 y H-3

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-6** † | Admisibilidad local frente a existencia certificada (H-2-R, sustituye a H-2) | **conservar** la admisibilidad G8B-R1 y sus umbrales; añadir **grados de evidencia** (`LOCALLY_VERIFIED`, `EXISTENCE_CERTIFIED`, `EXISTENCE_STRUCTURAL`, `EXISTENCE_EXACT`) y una **política de consumo** sin promoción silenciosa; **ampliar** con una vía certificada con escala S1 para H02, que es aditiva. La versión anterior (aceptación solo certificada, sin admisibilidad para las capacidades sin envolventes) **se retira**, porque restringía una capacidad aprobada sin identificarlo. Los pares no cambian en `CU3`: su escala por la distancia al origen se registra como no conformidad con §8.1 | adoptar la propuesta revisada ([normalización, admisibilidad y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md) §2–§4) |
| **D-CU3-13** † | Regla de conflicto de evidencia (H-CU3-1 a H-CU3-4) | **adoptar**: cuando una evidencia certificada de la misma revisión contradice una verificación local, prevalece la certificada y la solución contradicha se retira o se reclasifica. Es la única restricción, un cambio normativo identificado (A3.1-R) con un disparador determinista e independiente de la escala. Alternativa: solo un diagnóstico, sin restricción (mantiene la compatibilidad completa, pero deja publicadas raíces contradichas por una prueba; contrario a §5.1) | **adoptar** la regla |
| **D-CU3-7** † | Compatibilidad de los documentos anteriores (`AGENTS.md` §11.6) | lo que cambia en un documento guardado: las soluciones contradichas por la evidencia certificada (la costura de `e_1·c_1` del modelo, las raíces espurias, las tangencias en extremos clasificadas como transversales, los duplicados). Las admisiones nuevas son aditivas. **(a)** la regla se aplica a todos los documentos, con un diagnóstico que explica la contradicción y ofrece la forma estructural; (b) los documentos anteriores conservan su admisibilidad hasta una migración explícita, con la política persistida (ADR 0013) | **(a)**, con nota de migración y V-COMPAT-1 y V-COMPAT-2 |
| **D-CU3-8** | Ejes (H-3-R, sustituye a H-3) | **A**: `xAxis` e `yAxis` como referencias reservadas con identidad estable en el registro de identidad (enmienda acotada del ADR 0010). B: solo un mensaje tipado. C: sustituir el eje por una recta ordinaria (**rechazada**) | **A** (la causa es `GEO_NOT_SERIALIZABLE`) |

## 4. Alcance y relación con `CU2`

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-9** † | Componentes puntuales; C22, C23 y C40 | el contrato de `CU2` (§15.4) dice que una componente puntual existe aunque ningún parámetro binary64 alcance su parámetro exacto, que se selecciona por su dirección estructural y que no crea ningún objeto de punto público. **(a)** fuera de la materialización de `CU3`: un punto estructural cuya correspondencia corta una componente puntual falla cerrado con `POINT_COMPONENT_NOT_MATERIALIZED`, sin introducir C22 ni C23 y sin comparar `t̃(D)` con su encierro. (b) Diseñar en `CU3` el direccionamiento exacto (por ejemplo, M3 evaluando el predicado de validez en `D*`) y materializar ahí, lo que enmendaría §15.4 | **(a)**; (b), C22, C23 y C40 con su propia autorización |
| **D-CU3-12** | Sintaxis nueva frente a lectores anteriores | aceptar el fallo cerrado de los lectores anteriores (P10), con nota de migración y la caracterización V-A-COMPAT-2 de la carga | aceptar |
| **D-CU3-19** † | Alcance y puertas de la implementación | **una autorización con dos incrementos**: `CU3-A` (mínimo: I-1 a I-6, que cierra los tres casos obligatorios y la costura con `Intersect(L, r, K/J)`, H02, H06 y H-CU3-1 a H-CU3-4) y `CU3-B` (I-7: materialización sin conductor de las clases 1 y 2, germen y ciclo de vida), que empieza solo tras la puerta G-A. Puertas G-M3 (temprana), G-A, G-B y G-final. Alternativa: autorizar solo `CU3-A` y dejar `CU3-B` para otra decisión | dos incrementos en una autorización ([estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §1 y §2) |

## 5. Verificación, base, prompt y publicación

| Id | Decisión | Alternativas | R |
|---|---|---|---|
| **D-CU3-14** | Clase de verificación y base | `INTEGRATED_PHASE` (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU3`, `CU2`, `CU1` y `CU0`, `INTEGRATION`); base exacta: el commit de esta revisión focal (descendiente de `213ea5aa`), o su descendiente si el autor pide cambios; `FINAL` en `CU10` | adoptar |
| **D-CU3-15** | Prompt canónico de tarea | crear `.github/prompts/tasks/pre-g9b-cu3-…prompt.md` como primer paso documental de la implementación autorizada (CLAUDE.md impide crearlo como efecto secundario de otra tarea); o no crearlo | crearlo **con la autorización de implementación** |
| **D-CU3-16** | Perfil de esfuerzo (H-1) | sigue pospuesto; no condiciona `CU3` | confirmar |
| **D-CU3-17** † | Publicación | la rama `phase/pre-g9b-cu3-preparation` está publicada en `213ea5aa` (push normal autorizado el 2026-10-24). La revisión focal es un commit local descendiente. Alternativas: un push normal por avance rápido de la rama de revisión, sin `main`, sin etiquetas y sin bypass; o mantenerla local | push normal de la rama **si el autor lo autoriza**; ninguna integración en `main` con la preparación |

## 6. Decisiones que no cambiaron en la revisión focal

D-CU3-1, D-CU3-8, D-CU3-10, D-CU3-12, D-CU3-14, D-CU3-15, D-CU3-16 y D-CU3-18 mantienen su
recomendación. D-CU3-2 añade la selección exacta de la salida heredada; D-CU3-14 y D-CU3-17 solo
actualizan la base y el estado de la publicación.

## 7. Recomendación consolidada de aprobación del diseño y autorización de implementación

Se recomienda una sola decisión autoral con este contenido:

1. **Aprobar el diseño** con las recomendaciones de D-CU3-1 a D-CU3-19:
   - aceptar el ADR 0039 y las enmiendas A3.0 a A3.5 para su incorporación en T0;
   - reformular el criterio de la costura (D-CU3-10);
   - adoptar la precisión de los pliegues en el plan vivo (D-CU3-18).
2. **Autorizar la implementación** sobre la base exacta de D-CU3-14, con dos incrementos:
   - **`CU3-A`** (alcance mínimo) en los pasos T0 a T10:
     - grados de evidencia, vía certificada y regla de conflicto;
     - taxonomía de contacto en el resultado rico;
     - M3 en `Σ_B`;
     - `Point(L, D)` e `Intersect(L, r, D)`;
     - ejes;
     - compatibilidad (a);
   - **`CU3-B`** (materialización sin conductor de las clases 1 y 2, germen y ciclo de vida) en
     T11, solo tras la puerta G-A.
3. **Puertas obligatorias:**
   - **G-M3** en T2, temprana y bloqueante. Si falla, parada y decisión autoral entre R-1, R-2 y
     R-3; nunca un residuo.
   - **G-A** antes de `CU3-B`.
   - **G-B** antes de congelar con `CU3-B`; si falla, se congela con `CU3-A` solo.
   - **G-final** (`INTEGRATED_PHASE`) sobre el candidato congelado.
   - **Comprobación manual del autor.**
4. **Exclusiones expresas:**
   - el objeto público de punto crítico;
   - la materialización en componentes puntuales, C22, C23 y C40;
   - la normalización y la tangencia de los pares;
   - H-1;
   - `CU4`–`CU10` y `G9B`;
   - `geocedg_book`;
   - cualquier publicación en `main` sin una decisión aparte.
5. **Prompt de tarea:** creado como primer paso documental de la implementación (D-CU3-15).

Con esta decisión, la siguiente intervención autoral ordinaria es la revisión del candidato
técnico congelado. Antes solo intervendría si falla G-M3 o G-B, o si se cumple una condición de
parada de la estrategia.
