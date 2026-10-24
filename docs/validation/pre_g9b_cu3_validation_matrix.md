# PRE-G9B-CU3 — Matriz de validación científica y dinámica (candidata)

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de una futura implementación de `CU3`. No es evidencia de ejecución ni contrato aprobado |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Normas candidatas | [propuesta normativa](../architecture/pre_g9b_cu3_normative_proposal.md) (A3.0 a A3.5); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Diseños | [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md); [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md); [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del cierre de `CU0`; las de las filas dependen de la [hoja de decisiones de `CU3`](pre_g9b_cu3_author_decision_sheet.md) |

Regla de las filas:

- Cada fila obligatoria tiene una prueba que **falla sin la corrección o la capacidad** que valida
  (prueba adversaria), en la base `e6c908f2` o con la capacidad desactivada.
- Una fila marcada como dependiente de una decisión solo es obligatoria si la decisión adopta la
  opción correspondiente.
- Las tolerancias de comparación de puntos y longitudes son las de la línea base de `CU0` (`1e-9`
  y `1e-6`). Ninguna fila acepta una incidencia por un residuo.

## 1. Referencias analíticas y medidas

| Dato | Valor | Fuente |
|---|---|---|
| `t(K)` | `0` exacto (proyección canónica y sistema propio; `K.y − B.y = 0`) | análisis §5.1 |
| `t(J)` | `π`, canónicamente `−π` | análisis §5.1 |
| `M_1` | `(4,827722208159745; 7,673085542813242)` | `preserve.contacts` de `CU0` |
| `N_1` | `(4,827722208159745; −5,367469712127069)` | `preserve.contacts` de `CU0` |
| punto de la costura de `e_1` | `(1,6084395540124312; 10,130963488454622)` | `preserve.contacts` de `CU0` |
| identidad del modelo | `J_1(K) = M_1` en las dos ramas de `H_1`, con las condiciones `|IK| > 0`, `M_y ≠ H_y` y `|HD_1| ≤ |HM|` | análisis §5.3 (deducción); P3 (`0E-79` con 80 cifras) |
| rama del documento | `H_1 = 2I − K` | análisis §5.2 |
| `h″` en `t = 0` | `e_1·n` ≈ −3,348; `e_1·c_1` ≈ −2,061; `o_1·h_2` ≈ +2,569 | análisis §5.4 |
| `h″` en `t = π` | `e_1·c_1` ≈ −0,668; `e_1·r` ≈ +1,085 | análisis §5.4 |
| naturaleza del contacto | `e_1` en `0` y en `π`: pliegue (`F′ = 0`, certificado por simetría exacta en `CU2`), ángulos 58,4° (`n`, `r`) y 31,6° (`c_1`); `o_1`: tangencia regular (0° con `h_2`) | análisis §5.4b |
| desaparición | `D_1` ×3 (`|HD_1| > |HM|`): `M_1` indefinido; sin contacto `e_1·n` ni `o_1·h_2`; `e_1·c_1` persiste | análisis §5.5 |
| casi-tangencias sintéticas | circunferencia de radio 1,5 frente a `y = 1,5 + δ` y frente a la circunferencia de centro `(3 + δ, 0)` y radio 1,5, con δ entre `±10⁻²` y `±10⁻¹³` y 0 | análisis §4, P6 |
| tangencia en un extremo incluido | cuarto de arco de la circunferencia unidad desde `(1, 0)` con la recta `x = 1` | análisis §4, P6 |
| barrido H02 | radios 2 a 20 000, traslaciones 0, 10³ y 10⁶, recta `y = T + 0,5 r` | análisis §4, P7 |

## 2. Puertas por contadores

Contadores públicos y deterministas, sin reflexión, como en `CU1` y `CU2`; los tiempos son
informativos.

| Id | Puerta |
|---|---|
| GC3-1 | coste de la evaluación exacta (M3) del corte de `e_1` y `o_1` en `K` y en `J`: operaciones exactas, profundidad de la torre y tamaño de los enteros, por debajo de un presupuesto versionado fijado por el banco del candidato |
| GC3-2 | envolventes de `h`, `h′` y `h″`: número de cajas por contacto en los casos de la matriz, por debajo del presupuesto versionado |
| GC3-3 | arrastre del conductor sin cambio de revisión: ninguna evaluación exacta nueva (el certificado de incidencia es por revisión) |
| GC3-4 | arrastre de `rbCone`: una evaluación exacta por revisión y por punto estructural |

## 3. Pruebas

### 3.1 Etapa A — los cuatro hechos

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-A-1 | hecho 1: `K = Intersect(p, i, 2)` | procedencia `INTERSECTION_OUTPUT` con entrada `p` | un punto libre en las coordenadas de `K` da `PROVENANCE_NOT_ESTABLISHED` |
| V-A-2 | hecho 1: algoritmo fuera de la tabla | `PROVENANCE_NOT_ESTABLISHED` | `Dilate(K, 1, B)`, geométricamente igual a `K`, no se admite |
| V-A-3 | hecho 1: `Point(p)` y punto definitorio de un segmento | `PATH_POINT`; `DEFINING_POINT` con parámetro exacto 0 o 1 | — |
| V-A-4 | hecho 2: soporte coincidente distinto | `DRIVER_SUPPORT_MISMATCH` con `p' = Circle(B, rbCone)` y `K' = Intersect(p', i, 2)` | sin el control de identidad, `K'` se aceptaría por coincidencia |
| V-A-5 | hecho 2: copia del soporte (copia del documento o macro) | `DRIVER_SUPPORT_MISMATCH` cuando el identificador persistente no coincide | — |
| V-A-6 | hecho 2: conductor escalar o de rama | `UNSUPPORTED_DRIVER_FAMILY` | — |
| V-A-7 | hecho 3: `t(K) = 0` en el modelo; `t(J) = −π` canónico | valores exactos; el conductor vivo `E_1` no se mueve (su `PathParameter` no cambia) | una implementación por la vía (b) movería `E_1` |
| V-A-8 | hecho 3: segmento con `D` en un extremo y `D` en la recta soporte fuera del segmento | parámetro exacto en el extremo; `OUT_OF_DRIVER_DOMAIN` fuera, sin recortar por tolerancia | — |
| V-A-9 | hecho 3: arco con `D` en la circunferencia fuera del arco | `OUT_OF_DRIVER_DOMAIN` | — |
| V-A-10 | hecho 3: orientación | conductor en una circunferencia de orientación invertida: `t(D)` con el signo de su sistema propio, y `F(t(D))` igual al punto del locus con el conductor allí | — |
| V-A-11 | hecho 4: `t(D)` fuera de las componentes certificadas | indefinido con la causa de `CU2` | — |
| V-A-12 | hecho 4: `Completeness = NOT_ESTABLISHED` con `t(D)` en una componente certificada | definido (ADR 0027) | — |
| V-A-13 | hecho 4: incidencia no establecida | `Intersect(L, r, D)` indefinido con `INCIDENCE_NOT_ESTABLISHED`; `Point(L, D)` definido | un objetivo `r'` a `10⁻¹³` de `M_1` (residuo menor que la tolerancia) **no** se acepta |
| V-A-14 | objetivo limitado | `D_1` ×3: `Intersect(e_1, n, K)` con `NOT_ON_LIMITED_TARGET`; al restaurar, el mismo punto y la misma identidad | — |
| V-A-15 | permutación de las salidas del origen | si el estado heredado intercambia `J` y `K`, el punto sigue al objeto `K` y no conserva coordenadas antiguas | — |
| V-A-16 | persistencia | guardar, reabrir, Deshacer, Rehacer y copiar reconstruyen el punto desde el DAG; no se persiste ningún parámetro | — |

### 3.2 Casos obligatorios y dinámica

| Id | Caso | Criterio |
|---|---|---|
| V-MAND-1 | `Intersect(e_1, n, K)` | `M_1` dentro de la tolerancia de punto; incidencia de clase 1 o 2 (M3); orden `FOLD_CONTACT` |
| V-MAND-2 | `Intersect(e_1, c_1, K)` | igual |
| V-MAND-3 | `Intersect(o_1, h_2, K)` | `N_1`; orden dos (tangencia regular) |
| V-MAND-4 | contacto de la costura: `Intersect(e_1, c_1, J)` e `Intersect(e_1, r, J)` | el punto de la costura, como contacto estructural en el pliegue (D-CU3-10) |
| V-DYN-1 | barridos de `rbCone` (3 a 7,5), `α_cone` (35°, 47° y 60°), `D_1` (×0,5, ×1 y ×1,3) y `excCyl` (2,9 y 3,7) | los cuatro puntos siguen la construcción con la misma identidad; ninguno se vuelve indefinido |
| V-DYN-2 | desaparición y reaparición (`D_1` ×3 y vuelta) | V-A-14 |
| V-DYN-3 | `Point(o_1, K)` frente al rodeo del autor `Point(o_1, text2, v_1)` | el mismo punto, sin el selector manual |
| V-DET-CU3-1 | determinismo | dos ejecuciones en la misma revisión dan los mismos resultados, causas y contadores |
| V-VIEW-1 | vista | ningún resultado ni ninguna clase cambian con el zoom, el DPI, la cámara ni el tamaño de la vista (P9 como referencia) |

### 3.3 Etapa B — evidencia de contacto

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-B-1 | los tres contactos de `t = 0` con `Intersect(L, r)` | clase 3 sin una evaluación exacta; clase 2 con simetría exacta más M3; nunca una intersección materializada de clase 3 | la F-2 original los materializaría |
| V-B-2 | contactos de la costura con `Intersect(L, r)` | igual; **no** `TRANSVERSE_ESTABLISHED` (H-CU3-2) | falla en la base |
| V-B-3 | recta `y = 1,5 + δ`, δ = +10⁻¹³ | clase 4 sin intersección; ninguna solución `TANGENT_*` (H-CU3-3) | falla en la base |
| V-B-4 | δ = −10⁻¹³ y −10⁻¹⁰ | clase 4 con dos raíces; exactamente dos soluciones transversales con identidades nuevas (H-CU3-1, H-CU3-4) | falla en la base (cuatro y tres soluciones) |
| V-B-5 | δ = 0 | clase 3; sin punto de intersección (opción A) o con el punto crítico como objeto propio (opción B) | — |
| V-B-6 | circunferencia frente a circunferencia, mismos δ | como V-B-3 a V-B-5 | falla en la base |
| V-B-7 | `CONTACT_LOST`: de δ = 0 a δ > 0 desde un contacto de clase 1 o 2 | punto indefinido; ningún token transferido | — |
| V-B-8 | `CONTACT_SPLIT` en una tangencia regular | dos raíces con identidades nuevas; ninguna hereda el token tangente | — |
| V-B-9 | `CONTACT_SPLIT` en un pliegue | una sola intersección geométrica con dos preimágenes emparejadas por la simetría certificada; nunca dos puntos en la misma posición (D-CU3-11) | — |
| V-B-10 | `CONTACT_NOT_CERTIFIED` (clase 3 tras un cambio) | punto indefinido; evidencia visible; no es `LOST` ni `SPLIT` | — |
| V-B-11 | tangencia en un extremo incluido (cuarto de arco con `x = 1`) | no `TRANSVERSE_ESTABLISHED` (H-CU3-2); clase 1 o 2 si el extremo es estructural (inicio del arco sobre la recta por el mismo punto), clase 3 o 5 si no | falla en la base |
| V-B-12 | germen tangente | cambiar la escala de la ecuación del objetivo o invertir el sentido de un segmento conserva el selector; invertir un rayo lo cambia (§5.1 y §8.3 de la especificación de intersecciones) | — |
| V-B-NOISE-1 | modelo con `α` 35° y `D_1` ×0,5 | ninguna raíz espuria publicada como transversal ni admisible cerca de `t = 0` (H-CU3-1) | falla en la base |
| V-B-DUP-1 | traslación 10⁶ con radios 2 y 20 | exactamente dos soluciones (H-CU3-4) | falla en la base |
| V-B-CRIT-1 (opción B de D-CU3-5) | objeto de punto crítico | punto de la fuente con la envolvente de `h(t*)` como evidencia; no afirma la incidencia; no se convierte en intersección cuando su evidencia mejora | — |

### 3.4 Evaluación exacta (M3)

| Id | Caso | Criterio |
|---|---|---|
| V-M3-1 | el corte de `e_1` y de `o_1` en el punto exacto `K` | decide `h = 0` para `n`, `c_1` y `h_2` en las dos ramas de `H_1` |
| V-M3-2 | en el punto exacto `J` | decide `h = 0` para `c_1` y `r` |
| V-M3-3 | coincidencia accidental (un objetivo cuyo valor escrito coincide con el contacto solo por redondeo) | **no** decide la igualdad; da la clase 3 o la 4 |
| V-M3-4 | operación fuera de clase (función trascendente del conductor) | `EXACT_EVALUATION_OUT_OF_CLASS` |
| V-M3-5 | presupuesto agotado (inyección en el arnés) | `EXACT_EVALUATION_BUDGET_EXHAUSTED`; nunca una decisión |

### 3.5 Normalización (H-2)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-H2-1 | inversión de `pins.H02` | dos raíces admisibles para la recta y para el segmento en todo el barrido de radios | falla en la base |
| V-H2-2 | traslaciones 10³ y 10⁶ | el mismo resultado que en el origen | falla en la base |
| V-H2-3 | pares de loci trasladados 10⁶ | la normalización no usa la distancia al origen; mismas raíces que en el origen | falla en la base |
| V-H2-4 | diagnósticos de rechazo | `RESIDUAL_REJECTED` y `MEMBERSHIP_REJECTED` emitidos en casos construidos | falla en la base (no se emiten) |
| V-H2-5 | escala de la ecuación | multiplicar la ecuación del objetivo por una constante no cambia nada (§8.1) | — |
| V-H2-6 | capacidades sin envolventes (opción (i) de D-CU3-6) | sus raíces no son admisibles; su evidencia es visible | — |

### 3.6 Ejes (H-3)

| Id | Caso | Criterio |
|---|---|---|
| V-H3-1 | inversión de `pins.H06` | `Intersect(L, xAxis)` e `Intersect(L, yAxis)` creados (opción A de D-CU3-8) |
| V-H3-2 | persistencia de la identidad reservada | guardar, reabrir, Deshacer, Rehacer y copiar; el token de la raíz conserva la identidad reservada |
| V-H3-3 | objetivo no admitido | mensaje tipado, nunca `CAS.GeneralErrorMessage` |
| V-H3-4 | ningún objeto oculto | no se crea ninguna recta ordinaria `y = 0` |

### 3.7 Compatibilidad

| Id | Caso | Criterio |
|---|---|---|
| V-COMPAT-1 | documentos anteriores a `CU3` con tokens transversales persistidos (incluido un documento con una raíz admisible ordinaria) | los tokens de las raíces transversales genuinas se siguen resolviendo: el texto de los adaptadores no cambia |
| V-COMPAT-2 | documento con un token de una raíz mal clasificada (la costura de `e_1·c_1`) | el comportamiento que decida D-CU3-7: fallo cerrado con diagnóstico y oferta de la forma estructural (a), o política anterior conservada (b) |
| V-COMPAT-3 | modelo del autor completo, con `e_2` | carga, `e_2` y `LocusLength` sin cambios; las referencias `preserve` de `CU0` y las selecciones de `CU1` y `CU2` se mantienen |
| V-A-COMPAT-1 | documento de `CU3` abierto con el producto actual | rechazo cerrado de las formas nuevas, sin reinterpretación como punto inicial (P10) |
| V-A-COMPAT-2 | ídem | se caracteriza si la carga del documento se aborta o solo se pierden esos objetos, y la nota de migración lo dice (D-CU3-12) |

### 3.8 Regresión

- **Inversión de los pins de `CU3` de la línea base de `CU0`:** `pins.B08_B09` (con la
  reformulación de la raíz de la costura, D-CU3-10), `pins.H02` y `pins.H06`, mediante pruebas de
  aceptación propias (decisión C1-4).
- **Referencias `preserve`:** `preserve.contacts` (puntos y residuos) y el resto de las
  referencias de `CU0`.
- **Fases anteriores:** las selecciones `PHASE` de `CU0`, `CU1` y `CU2`, más `INTEGRATION`.

## 4. Verificación

Clase recomendada: `INTEGRATED_PHASE`, con `STATIC`, `INFRA_UNIT`, `PHASE` de `CU3`, `CU2`,
`CU1` y `CU0`, e `INTEGRATION`. Las selecciones nuevas son `pre-g9b-cu3.shared` y
`pre-g9b-cu3.desktop`, registradas con el procedimiento oficial del inventario. `FINAL` queda para
`CU10` (decisión D-CU3-14).

## 5. Lo que estas pruebas no pueden demostrar

- La evaluación exacta prueba la configuración de las constantes capturadas, no el valor escrito
  por el usuario.
- Una tangencia accidental no se certifica nunca: la matriz lo exige (V-M3-3).
- Los barridos son muestras finitas de las familias de parámetros; la persistencia del teorema
  del modelo se deduce (análisis §5.3), no se mide para todo valor.
- Los ángulos de los pliegues se calculan a partir de envolventes puntuales y son datos, no
  certificados.
- La compatibilidad con lectores anteriores se comprueba con el producto actual, no con todas las
  versiones publicadas.
