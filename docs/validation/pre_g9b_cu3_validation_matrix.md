# PRE-G9B-CU3 — Matriz de validación científica y dinámica (candidata)

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de una futura implementación de `CU3`. No es evidencia de ejecución ni contrato aprobado |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisada por la revisión científica focal de 2026-10-24; `selfApproved=false` |
| Normas candidatas | [propuesta normativa](../architecture/pre_g9b_cu3_normative_proposal.md) (A3.0 a A3.5); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Diseños | [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md); [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización, admisibilidad y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md); [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del cierre de `CU0`; las de las filas dependen de la [hoja de decisiones de `CU3`](pre_g9b_cu3_author_decision_sheet.md) |

Reglas de las filas:

- Cada fila obligatoria tiene una prueba que **falla sin la corrección o la capacidad** que valida
  (prueba adversaria), en la base `e6c908f2` o con la capacidad desactivada.
- Una fila dependiente de una decisión solo es obligatoria si se adopta la opción correspondiente.
- Las tolerancias de comparación de puntos y longitudes son las de la línea base de `CU0` (`1e-9` y
  `1e-6`). Una comparación de posiciones dentro de una tolerancia nunca valida una incidencia: las
  filas de incidencia exigen una decisión exacta o estructural.
- Cada fila que afirma algo exacto declara su configuración: `Σ_L`, la configuración capturada de
  `L`, o `Σ_B`, la configuración exacta conjunta (Etapa A §2.3 y §6.2).

## 1. Referencias analíticas y medidas

| Dato | Valor | Fuente |
|---|---|---|
| `t*(K)` | `0` exacto en `Σ_L` y en `Σ_B` (`K* − c` es un múltiplo positivo de `e₀`) | análisis §5.1; Etapa A §2.3 |
| `t*(J)` | `π`, no representable; garantía encerrada | análisis §5.1 |
| `K.x` binary64 | `fl(B.x + 5)`, que difiere de `B.x + 5` en `4,44·10⁻¹⁶` | análisis §5.7 |
| `M_1` | `(4,827722208159745; 7,673085542813242)` | `preserve.contacts` de `CU0` |
| `N_1` | `(4,827722208159745; −5,367469712127069)` | `preserve.contacts` de `CU0` |
| punto de la costura de `e_1` | `(1,6084395540124312; 10,130963488454622)` | `preserve.contacts` de `CU0` |
| identidad del modelo | `J_1(K) = M_1` en las dos ramas de `H_1`, con `|IK| > 0`, `M_y ≠ H_y` y `|HD_1| ≤ |HM|` | análisis §5.3; P3 (`0E-79`, 80 cifras, frontera `{H, B, altura de h, rbCone, D_1}`) |
| rama del documento | `H_1 = 2I − K` | análisis §5.2 |
| `h″` en `t = 0` | `e_1·n` ≈ −3,348; `e_1·c_1` ≈ −2,061; `o_1·h_2` ≈ +2,569 | análisis §5.4 |
| `h″` en `t = π` | `e_1·c_1` ≈ −0,668; `e_1·r` ≈ +1,085 | análisis §5.4 |
| naturaleza del contacto | `e_1` en `0` y en `π`: pliegue (`F′ = 0`, certificado por simetría exacta en `CU2`), con ángulos de 58,4° (`n`, `r`) y 31,6° (`c_1`); `o_1`: tangencia regular | análisis §5.4b |
| desaparición | `D_1` ×3: `M_1` indefinido; sin contacto `e_1·n` ni `o_1·h_2`; `e_1·c_1` persiste | análisis §5.5 |
| casi-tangencias sintéticas | circunferencia de radio 1,5 frente a `y = 1,5 + δ` y frente a la circunferencia de centro `(3 + δ, 0)` y radio 1,5, con δ entre `±10⁻²` y `±10⁻¹³` y 0 | análisis §4, P6 |
| tangencia en un extremo incluido | cuarto de arco de la circunferencia unidad desde `(1, 0)` con la recta `x = 1` | análisis §4, P6 |
| barrido H02 | radios 2 a 20 000, traslaciones 0, 10³ y 10⁶, recta `y = T + 0,5 r` | análisis §4, P7 |

## 2. Puertas

### 2.1 Puerta temprana y bloqueante de M3 (paso T2 de la estrategia)

| Id | Criterio | Se supera si |
|---|---|---|
| G-M3-1 | viabilidad matemática en `Σ_B` de los cinco contactos obligatorios (`e_1·n`, `e_1·c_1` y `o_1·h_2` en `K`; `e_1·c_1` y `e_1·r` en `J`) | V-M3-1, V-M3-2 y V-M3-3 a V-M3-6 pasan: igual en los cinco y en las dos ramas; distinto en el control desplazado un ulp; igual en el control accidental exacto; distinto en el control accidental no representable |
| G-M3-2 | presupuesto | los contadores de GC3-1, GC3-3 y GC3-4 de los cinco contactos, bajo el techo versionado propuesto por el banco y aprobado por el autor |
| G-M3-3 | correspondencia con el algoritmo vivo | V-M3-7 pasa en todos los algoritmos de varias salidas de los cinco subgrafos |

Si la puerta no se supera, la implementación de la segunda incidencia se detiene y se presentan
las alternativas R-1 a R-3 de la Etapa A §6.5. Nunca se sustituye por un residuo.

### 2.2 Contadores

Contadores públicos y deterministas, sin reflexión, como en `CU1` y `CU2`; los tiempos son
informativos.

| Id | Puerta |
|---|---|
| GC3-1 | coste de M3 en `Σ_B`: operaciones exactas, grado de la torre y longitud máxima en bits, por contacto |
| GC3-2 | envolventes de `h`, `h′` y `h″`: número de cajas por contacto en los casos de la matriz |
| GC3-3 | arrastre del conductor sin cambio de revisión: ninguna evaluación exacta nueva (el certificado de incidencia es por revisión) |
| GC3-4 | cambio de `rbCone`: una evaluación exacta por revisión y por punto estructural |
| GC3-5 | regla de conflicto: el análisis certificado se dispara solo en los candidatos de su disparador; ninguna evaluación adicional en un barrido de raíces transversales genéricas |

## 3. Pruebas

### 3.1 Etapa A — los cuatro hechos y la correspondencia exacta

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-A-1 | hecho 1: `K = Intersect(p, i, 2)` | procedencia `INTERSECTION_OUTPUT` con entrada `p`; `K*` reconstruido por la regla exacta de selección | un punto libre en las coordenadas de `K` da `PROVENANCE_NOT_ESTABLISHED` |
| V-A-2 | hecho 1: algoritmo fuera de la tabla | `PROVENANCE_NOT_ESTABLISHED` | `Dilate(K, 1, B)`, geométricamente igual a `K`, no se admite |
| V-A-3 | hecho 1: `Point(p)` y punto definitorio de un segmento | `PATH_POINT` con `t*` igual a su parámetro de camino; `DEFINING_POINT` con 0 o 1 exactos | — |
| V-A-4 | hecho 2: soporte coincidente distinto | `DRIVER_SUPPORT_MISMATCH` con `p' = Circle(B, rbCone)` y `K' = Intersect(p', i, 2)` | sin el control de identidad, `K'` se aceptaría por coincidencia |
| V-A-5 | hecho 2: copia del soporte | `DRIVER_SUPPORT_MISMATCH` si el identificador persistente no coincide | — |
| V-A-6 | hecho 2: conductor escalar o de rama | `UNSUPPORTED_DRIVER_FAMILY` | — |
| V-A-7 | hecho 3: `t*(K) = 0` exacto; `t*(J)` encerrado alrededor de `π` | valores y encierros; el conductor vivo `E_1` no se mueve (su `PathParameter` no cambia) | una implementación por la vía (b) movería `E_1` |
| V-A-8 | hecho 3: la correspondencia no usa las coordenadas binary64 de `D` como autoridad | con `K` en una posición perturbada un ulp sin cambiar sus operandos, `[t]` no cambia | una implementación que proyectara `K` binary64 cambiaría `[t]` |
| V-A-9 | hecho 3: extremo de segmento | `D` en el extremo por procedencia: parámetro exacto; `D` en la recta soporte fuera del segmento: `OUT_OF_DRIVER_DOMAIN`; `[t]` que corta el extremo sin procedencia sobre el segmento: `DRIVER_DOMAIN_UNDECIDED` | ningún recorte por tolerancia |
| V-A-10 | hecho 3: arco con `D` en la circunferencia fuera del arco | `OUT_OF_DRIVER_DOMAIN` | — |
| V-A-11 | hecho 3: costura | `[t]` alrededor de `±π` en una componente periódica cerrada: definido; en una componente cuyo extremo es la costura: `SEAM_SIDE_UNDECIDED` | — |
| V-A-12 | hecho 3: orientación | conductor sobre una circunferencia de orientación invertida: `F(t*(D))` igual al punto del locus con el conductor allí | — |
| V-A-13 | hecho 4: `[t]` fuera de las componentes certificadas | indefinido con la causa de `CU2` | — |
| V-A-14 | hecho 4: `[t]` que corta un extremo de componente, una transición, una banda `UNRESOLVED` o un hueco | `COMPONENT_BOUNDARY_UNDECIDED` | — |
| V-A-15 | hecho 4: componente puntual (configuración sintética de V-POINT-1 de `CU2`, con `D` sobre el soporte en el parámetro exacto de la componente) | `POINT_COMPONENT_NOT_MATERIALIZED`; ningún objeto de punto público | una implementación que comparara `t̃(D)` con el encierro de la componente crearía un punto |
| V-A-16 | hecho 4: `Completeness = NOT_ESTABLISHED` con `[t]` en una componente continua certificada | definido (ADR 0027) | — |
| V-A-17 | hecho 4: incidencia no establecida | `Intersect(L, r, D)` indefinido con `INCIDENCE_NOT_ESTABLISHED` o `INCIDENCE_REFUTED`; `Point(L, D)` definido | un objetivo `r'` a `10⁻¹³` de `M_1` no se acepta: M3 lo refuta |
| V-A-18 | incidencia inferida de una proyección | `Point(L, D)` con `P̃` a distancia binary64 nula de `r` no implica `Intersect` | — |
| V-A-19 | objetivo limitado | `D_1` ×3: `Intersect(e_1, n, K)` con `NOT_ON_LIMITED_TARGET`; al restaurar, el mismo punto y la misma identidad | — |
| V-A-20 | selección no reconstruible | raíces coincidentes del algoritmo de `D` (recta tangente a `p`): `SELECTION_NOT_RECONSTRUCTED` | — |
| V-A-21 | permutación de las salidas del origen | si el estado heredado intercambia `J` y `K`, `D*` cambia con él y el punto sigue al objeto `K`, sin coordenadas antiguas | — |
| V-A-22 | persistencia | guardar, reabrir, Deshacer, Rehacer y copiar reconstruyen el punto desde el DAG; ningún parámetro ni certificado se persiste | — |

### 3.2 Casos obligatorios y dinámica

| Id | Caso | Criterio |
|---|---|---|
| V-MAND-1 | `Intersect(e_1, n, K)` | `M_1` dentro de la tolerancia de punto; incidencia decidida por M3 en `Σ_B`; orden `FOLD_CONTACT` |
| V-MAND-2 | `Intersect(e_1, c_1, K)` | igual |
| V-MAND-3 | `Intersect(o_1, h_2, K)` | `N_1`; orden dos (tangencia regular) |
| V-MAND-4 | costura: `Intersect(e_1, c_1, J)` e `Intersect(e_1, r, J)` | el punto de la costura, como contacto estructural en el pliegue (D-CU3-10) |
| V-DYN-1 | barridos de `rbCone` (3 a 7,5), `α_cone` (35°, 47° y 60°), `D_1` (×0,5, ×1 y ×1,3) y `excCyl` (2,9 y 3,7) | los cuatro puntos siguen la construcción con la misma identidad; M3 decide igual en cada revisión |
| V-DYN-2 | desaparición y reaparición (`D_1` ×3 y vuelta) | V-A-19 |
| V-DYN-3 | `Point(o_1, K)` frente al rodeo del autor `Point(o_1, text2, v_1)` | el mismo punto, sin el selector manual |
| V-DET-CU3-1 | determinismo | dos ejecuciones en la misma revisión dan los mismos resultados, causas y contadores |
| V-VIEW-1 | vista | ningún resultado ni ninguna clase cambian con el zoom, el DPI, la cámara ni el tamaño de la vista |

### 3.3 Evaluación exacta (M3)

| Id | Caso | Criterio |
|---|---|---|
| V-M3-1 | `Σ_B` del modelo en el punto exacto `K*`, con la frontera `{A, B, rbCone, H, D_1}` | decide igual para `n`, `c_1` y `h_2` en las dos ramas de `H_1` |
| V-M3-2 | ídem en `J*` | decide igual para `c_1` y `r` |
| V-M3-3 | control de configuración: la misma decisión con los objetos intermedios (`K`, `M`, `n`, `M_1`) tomados en su estado binary64 | refuta las incidencias con `n` y con `h_2`, que se construyen a partir de `K` y `M` redondeados (`INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`), y puede confirmar la de `c_1`, que solo depende de constantes comunes al corte; documenta por qué se exige la configuración conjunta |
| V-M3-4 | control negativo: objetivo desplazado un ulp en la frontera | decide distinto |
| V-M3-5 | igualdad accidental exacta en `Σ_B`: circunferencia trazada de radio 1,5 y recta `y = 1,5` | decide igual (clase 2) |
| V-M3-6 | igualdad accidental solo con valores no representables (`0.1 + 0.2` frente a `0.3`) | decide distinto, de forma exacta |
| V-M3-7 | correspondencia con el algoritmo vivo | para cada algoritmo de varias salidas, la rama exacta, elegida por la regla exacta de §10.2 con el estado capturado, tiene un encierro que contiene el valor vivo y es disjunto del de la otra rama; si no, `SELECTION_NOT_RECONSTRUCTED` |
| V-M3-8 | fuera de clase y presupuesto | operación trascendente del conductor: `EXACT_EVALUATION_OUT_OF_CLASS`; presupuesto agotado (inyección en el arnés): `EXACT_EVALUATION_BUDGET_EXHAUSTED`. Nunca una decisión |

### 3.4 Etapa B — evidencia de contacto (opción A)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-B-1 | los contactos de `t = 0` con `Intersect(L, r)` | clase 3 en el incremento mínimo; clase 2 con `CU3-B`; nunca una intersección de clase 3 | la F-2 original los materializaría |
| V-B-2 | contactos de la costura con `Intersect(L, r)` | **no** `TRANSVERSE_ESTABLISHED` (regla de conflicto); clase 3 o 2 | falla en la base |
| V-B-3 | recta `y = 1,5 + δ`, δ = +10⁻¹³ | clase 4 sin intersección; ninguna solución publicada (regla de conflicto, H-CU3-3) | falla en la base |
| V-B-4 | δ = −10⁻¹³ y −10⁻¹⁰ | clase 4 con dos raíces; exactamente dos soluciones transversales `EXISTENCE_CERTIFIED` (H-CU3-1, H-CU3-4) | falla en la base |
| V-B-5 | δ = 0 | clase 3 sin M3; clase 2 con M3 (V-M3-5); el resultado rico conserva la evidencia; sin objeto de punto crítico | — |
| V-B-6 | circunferencia frente a circunferencia, mismos δ | como V-B-3 a V-B-5 | falla en la base |
| V-B-7 (`CU3-B`) | `CONTACT_LOST` desde un contacto de clase 1 o 2 | punto indefinido; ningún token transferido | — |
| V-B-8 (`CU3-B`) | `CONTACT_SPLIT` en una tangencia regular | dos raíces con identidades nuevas | — |
| V-B-9 (`CU3-B`) | `CONTACT_SPLIT` en un pliegue | una intersección geométrica con dos preimágenes y una identidad (D-CU3-11) | — |
| V-B-10 (`CU3-B`) | `CONTACT_NOT_CERTIFIED` | punto indefinido; evidencia visible | — |
| V-B-11 | tangencia en un extremo incluido (cuarto de arco con `x = 1`) | no `TRANSVERSE_ESTABLISHED` (regla de conflicto); clase 1 si el extremo es estructural, 3 o 5 si no | falla en la base |
| V-B-12 (`CU3-B`) | germen tangente | cambiar la escala de la ecuación del objetivo o invertir un segmento conserva el selector; invertir un rayo lo cambia | — |
| V-B-NOISE-1 | modelo con `α` 35° y `D_1` ×0,5 | ninguna raíz espuria publicada como transversal ni admisible cerca de `t = 0` (H-CU3-1) | falla en la base |
| V-B-DUP-1 | traslación 10⁶ con radios 2 y 20 | exactamente dos soluciones (H-CU3-4) | falla en la base |

### 3.5 Grados de evidencia, admisibilidad y política de consumo

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-EVID-1 | vía G8B-R1 intacta | las suites G8B (kernel, ciclo de vida, topología), G8C1, G8C2 y G9U0-R4 pasan sin cambios, salvo las filas de conflicto identificadas; ninguna raíz transversal genérica cambia de admisibilidad ni de token | — |
| V-EVID-2 | grado visible | cada solución publica su grado; `LOCALLY_VERIFIED` en las capacidades sin envolventes; `EXISTENCE_CERTIFIED` cuando se calcula | — |
| V-EVID-3 | sin promoción | ningún grado sube por una tolerancia, por coincidencia de coordenadas, por una etiqueta o tras guardar y reabrir; un consumidor que exige un grado mínimo queda indefinido con su causa si deja de cumplirse | una promoción por persistencia o por tolerancia hace fallar la prueba |
| V-EVID-4 | segunda incidencia de la Etapa A | rechaza `LOCALLY_VERIFIED` y `EXISTENCE_CERTIFIED` como prueba de una igualdad en un punto prescrito | — |
| V-EVID-5 | regla de conflicto | cada contradicción tipificada retira o reclasifica la solución contradicha, con la causa `CONTRADICTED_BY_CERTIFIED_EVIDENCE`; sin contradicción, nada cambia | — |
| V-EVID-6 | disparador | GC3-5 | — |

### 3.6 Normalización (H-2)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-H2-1 | inversión de `pins.H02` | dos raíces admisibles por la vía certificada para la recta y para el segmento en todo el barrido | falla en la base |
| V-H2-2 | traslaciones 10³ y 10⁶ | el mismo resultado que en el origen | falla en la base |
| V-H2-3 | dibujos pequeños (radio `10⁻³`) | ningún cambio de admisibilidad: la vía G8B-R1 se mantiene | — |
| V-H2-4 | diagnósticos de rechazo | `RESIDUAL_REJECTED` y `MEMBERSHIP_REJECTED` emitidos en casos construidos | falla en la base |
| V-H2-5 | escala de la ecuación | multiplicar la ecuación del objetivo por una constante no cambia nada (§8.1) | — |
| V-H2-PAIR-1 | pares | sin cambios en `CU3`; la no conformidad con §8.1 está registrada como limitación | — |

### 3.7 Ejes (H-3)

| Id | Caso | Criterio |
|---|---|---|
| V-H3-1 | inversión de `pins.H06` | `Intersect(L, xAxis)` e `Intersect(L, yAxis)` creados (opción A de D-CU3-8) |
| V-H3-2 | persistencia de la identidad reservada | guardar, reabrir, Deshacer, Rehacer y copiar |
| V-H3-3 | objetivo no admitido | mensaje tipado, nunca `CAS.GeneralErrorMessage` |
| V-H3-4 | ningún objeto oculto | no se crea ninguna recta ordinaria `y = 0` |

### 3.8 Compatibilidad

| Id | Caso | Criterio |
|---|---|---|
| V-COMPAT-1 | documentos anteriores a `CU3` con tokens de raíces transversales genuinas | se siguen resolviendo: el texto de los adaptadores no cambia |
| V-COMPAT-2 | documentos con tokens de soluciones contradichas (la costura de `e_1·c_1`, una raíz espuria, un duplicado) | el comportamiento que decida D-CU3-7: fallo cerrado con diagnóstico y oferta de la forma estructural (a), o admisibilidad conservada hasta la migración (b) |
| V-COMPAT-3 | modelo del autor completo, con `e_2` | carga, `e_2` y `LocusLength` sin cambios; las referencias `preserve` de `CU0` y las selecciones de `CU1` y `CU2` se mantienen |
| V-A-COMPAT-1 | documento de `CU3` abierto con el producto actual | rechazo cerrado de las formas nuevas, sin reinterpretación como punto inicial (P10) |
| V-A-COMPAT-2 | ídem | se caracteriza si la carga se aborta o solo se pierden esos objetos; la nota de migración lo dice (D-CU3-12) |

### 3.9 Regresión

- **Inversión de los pins de `CU3` de la línea base de `CU0`:** `pins.B08_B09` (con la
  reformulación de la raíz de la costura, D-CU3-10), `pins.H02` y `pins.H06`, mediante pruebas de
  aceptación propias (decisión C1-4).
- **Referencias `preserve`:** `preserve.contacts` y el resto de las de `CU0`.
- **Fases anteriores:** las selecciones `PHASE` de `CU0`, `CU1` y `CU2`, e `INTEGRATION`.

## 4. Verificación

Clase recomendada: `INTEGRATED_PHASE`, con `STATIC`, `INFRA_UNIT`, `PHASE` de `CU3`, `CU2`,
`CU1` y `CU0`, e `INTEGRATION`. Las selecciones nuevas son `pre-g9b-cu3.shared` y
`pre-g9b-cu3.desktop`, registradas con el procedimiento oficial del inventario. `FINAL` queda para
`CU10` (decisión D-CU3-14).

## 5. Lo que estas pruebas no pueden demostrar

- **Alcance de M3.** Prueba la configuración exacta conjunta `Σ_B` con su frontera declarada, no
  el valor escrito por el usuario. Una relación que solo valga por cancelaciones entre valores
  trascendentes de la frontera se refutará en `Σ_B` (V-M3-3 documenta el caso análogo de los
  objetos intermedios).
- **Igualdades accidentales.** Una igualdad accidental se demuestra exactamente cuando es exacta en
  `Σ_B` (V-M3-5) y se refuta cuando no lo es (V-M3-6). Ninguna fila acepta una coincidencia dentro
  de una tolerancia.
- **Barridos.** Son muestras finitas de las familias de parámetros: M3 decide cada revisión
  ejecutada, no todos los valores.
- **Ángulos de los pliegues.** Se calculan a partir de envolventes puntuales y son datos, no
  certificados.
- **Disparador de la regla de conflicto.** Se valida en los casos de la matriz; no demuestra que no
  exista otra raíz transversal contradicha fuera de sus disparadores.
- **Lectores anteriores.** La compatibilidad se comprueba con el producto actual, no con todas las
  versiones publicadas.
