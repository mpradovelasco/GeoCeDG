# PRE-G9B-CU3 — Matriz de validación científica y dinámica (candidata)

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de una futura implementación de `CU3`. No es evidencia de ejecución ni contrato aprobado |
| Estado | Matriz de la preparación, revisada por la revisión científica focal y por la comprobación final de coherencia científica de 2026-10-24. **Aprobada por el autor el 2026-10-24** con la precisión de la autoridad del objetivo, incorporada en el paso T0 (G-M3-4 y las filas que dependen de ella) ([registro de aprobación](pre_g9b_cu3_author_design_approval_record.md), única autoridad de ese estado); `selfApproved=false` |
| Normas candidatas | [propuesta normativa](../architecture/pre_g9b_cu3_normative_proposal.md) (A3.0 a A3.5); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Diseños | [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md); [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md); [Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md); [normalización, admisibilidad y ejes](../architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md); [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del cierre de `CU0`; las de las filas dependen de la [hoja de decisiones de `CU3`](pre_g9b_cu3_author_decision_sheet.md) |
| Ampliación V-AUT | §6: filas V-AUT de la autoridad constructiva, de la conciliación de 2026-10-25 ([propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md)). **Aprobada por el autor el 2026-10-25** como filas obligatorias ([registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md), única autoridad de ese estado); no modifica §1 a §5 |

Reglas de las filas:

- Cada fila obligatoria tiene una prueba que **falla sin la corrección o la capacidad** que valida
  (prueba adversaria), en la base `e6c908f2` o con la capacidad desactivada.
- Una fila dependiente de una decisión solo es obligatoria si se adopta la opción correspondiente.
- Las tolerancias de comparación de puntos y longitudes son las de la línea base de `CU0` (`1e-9` y
  `1e-6`). Una comparación de posiciones dentro de una tolerancia nunca valida una incidencia: las
  filas de incidencia exigen una decisión exacta o estructural.
- Cada fila que afirma algo exacto declara su configuración: `Σ_L`, la configuración capturada de
  `L` y autoridad; `Σ_L⁺`, que la contiene sin cambios y es la configuración de decisión de M3 y de
  los certificados de conteo; o `Σ_B`, la recalculación profunda, solo diagnóstico (Etapa A §2.3,
  §6.2 y §6.6).
- Precisión autoral de 2026-10-24 (G-M3-4): cada incidencia se decide contra la autoridad del
  objetivo (`locus-v2-intersections.md` §2.1; `locus-v2-certified-construction-model.md` §11.3):
  los coeficientes binary64 de la recta, con la parte limitada de un segmento o un rayo, y el
  centro y el semieje del adaptador de una circunferencia. Una reconstrucción del objetivo, o la
  matriz capturada de una constante que es también objetivo, solo da diagnósticos, salvo que se
  demuestre su igualdad exacta con la autoridad.

## 1. Referencias analíticas y medidas

| Dato | Valor | Fuente |
|---|---|---|
| `t*(K)` | `0` exacto en `Σ_L` y en `Σ_L⁺` (`K* − c` es un múltiplo positivo de `e₀`, con la semántica de conductor de `p`) | análisis §5.1; Etapa A §2.3 |
| relaciones exactas en `Σ_L` | `k` vertical por `H`; `h` e `i` horizontales; `I = i ∩ k = (H.x, B.y)`; centro de `c_1` igual a `H`; centro de `p` en `i`; `g ∥ k` (productos por 0 y ±1, normalización por potencias de dos; hipótesis `z = 1`) | análisis §5.8 |
| radio de `c_1` | `Σ_L`: el de la matriz capturada, `√(A4² + A5² − A2)`; autoridad como objetivo: el semieje del adaptador; `Σ_B`: el exacto `|HD_1|`; en general distintos entre sí, así que `P_L ≠ P_B` | Etapa A §6.2 y §6.6; G-M3-4 |
| datos del caso adversario de configuración | `fl(fl(√2)²) = 2,0000000000000004 ≠ 2` | comprobación aritmética de la comprobación final |
| `t*(J)` | `π`, no representable; garantía encerrada | análisis §5.1 |
| `K.x` binary64 | `fl(B.x + 5)`, que difiere de `B.x + 5` en `4,44·10⁻¹⁶` | análisis §5.7 |
| `M_1` | `(4,827722208159745; 7,673085542813242)` | `preserve.contacts` de `CU0` |
| `N_1` | `(4,827722208159745; −5,367469712127069)` | `preserve.contacts` de `CU0` |
| punto de la costura de `e_1` | `(1,6084395540124312; 10,130963488454622)` | `preserve.contacts` de `CU0` |
| identidad del modelo | `J_1(K) = M_1` en las dos ramas de `H_1`, con `|IK| > 0`, `M_y ≠ H_y` y `|HD_1| ≤ |HM|` | análisis §5.3; P3 (`0E-79`, 80 cifras, frontera `{H, B, altura de h, rbCone, D_1}`): una recalculación profunda del tipo `Σ_B`, que solo es diagnóstica; en `Σ_L⁺` la sostiene el análisis §5.8 y la decide G-M3 |
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
| G-M3-1 | viabilidad matemática en `Σ_L⁺` de los cinco contactos obligatorios (`e_1·n`, `e_1·c_1` y `o_1·h_2` en `K`; `e_1·c_1` y `e_1·r` en `J`), con las hipótesis aritméticas del análisis §5.8 comprobadas sobre los estados capturados | M3 decide exactamente (igual o distinto, nunca fuera de clase) en los cinco contactos y en las dos ramas de `H_1`, con las hipótesis de §5.8 comprobadas; V-M3-1 y V-M3-2 deciden igual con los objetivos reconstruidos, como diagnóstico del teorema; los controles V-M3-3 a V-M3-6 dan su resultado (distinto en el control desplazado un ulp; igual en el control accidental exacto; distinto en el accidental no representable); y V-CFG-1 a V-CFG-6 pasan. Que la incidencia se cumpla con la autoridad del objetivo lo decide G-M3-4 |
| G-M3-2 | presupuesto | los contadores de GC3-1, GC3-3 y GC3-4 de los cinco contactos, bajo el techo versionado propuesto por el banco y aprobado por el autor |
| G-M3-3 | correspondencia con el algoritmo vivo | V-M3-7 pasa en todos los algoritmos de varias salidas de los cinco subgrafos |
| G-M3-4 | autoridad del objetivo geométrico (precisión autoral de 2026-10-24): para cada incidencia, el punto exacto de `L` en `Σ_L` y el objetivo autoritativo; semántica exacta del objetivo frente a su autoridad (rectas, coeficientes proporcionales con un factor no nulo demostrado; segmentos y rayos, además su parte limitada; circunferencias, la representación del adaptador, distinta de la matriz capturada, de los semiejes y de las evaluaciones numéricas; objetivos reconstruidos, correspondencia exacta demostrada) en los casos obligatorios, en los controles adversarios y donde el redondeo rompe una relación constructiva exacta | V-M3-9 y V-CFG-1 a V-CFG-6 pasan: cada incidencia se decide contra la autoridad, y un desacuerdo sin correspondencia exacta demostrada falla cerrado |

La aprobación del diseño no certifica por anticipado la viabilidad de M3 ni su presupuesto. El
resultado de la puerta y el techo propuesto se presentan juntos al autor para una única decisión
cuando el presupuesto necesita su aprobación. Si la puerta no se supera, incluida G-M3-4, la
implementación de la segunda incidencia se detiene y se presentan las alternativas R-1 a R-3 y C3
de la Etapa A §6.7; ninguna queda autorizada automáticamente. Nunca se sustituye por un residuo ni
por una demostración en `Σ_B`.

### 2.2 Contadores

Contadores públicos y deterministas, sin reflexión, como en `CU1` y `CU2`; los tiempos son
informativos.

| Id | Puerta |
|---|---|
| GC3-1 | coste de M3 en `Σ_L⁺`: operaciones exactas, grado de la torre y longitud máxima en bits, por contacto; el diagnóstico de `Σ_B`, si se calcula, por separado |
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
| V-MAND-1 | `Intersect(e_1, n, K)` | el punto materializado es el de `Point(e_1, K)` (`P_L`), dentro de la tolerancia de punto de `M_1`; incidencia decidida por M3 en `Σ_L⁺` contra la autoridad de `n` (G-M3-4); orden `FOLD_CONTACT`; si la autoridad no contiene `P_L`, fallo cerrado y condición de parada |
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
| V-M3-1 | `Σ_L⁺` del modelo en el punto exacto `K*`: constantes de `Σ_L` sin cambios (`H`, `h`, `k`, `i`, `I`, `c_1` en su forma de matriz, `p` en su semántica de conductor), `g` en su estado binary64, y `K`, `m`, `M`, `n`, `M_1` y `h_2` recalculados | diagnóstico del teorema con los objetivos reconstruidos: decide igual para `n`, `c_1` y `h_2` en las dos ramas de `H_1`; con la precisión G-M3-4 nunca establece la incidencia, que decide V-M3-9 |
| V-M3-2 | ídem en `J*` | decide igual para `c_1` y `r` |
| V-M3-3 | control de configuración: la misma decisión con los objetos intermedios (`K`, `M`, `n`, `M_1`) tomados en su estado binary64 | refuta las incidencias con `n` y con `h_2`, que se construyen a partir de `K` y `M` redondeados (`INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`), y puede confirmar la de `c_1`, que solo depende de constantes comunes al corte; documenta por qué la regla 6 de `Σ_L⁺` recalcula exactamente los objetos intermedios (Etapa A §6.2). Con la precisión G-M3-4, `n` y `h_2` en su estado binary64 son su autoridad como objetivos: para ellos este control coincide con la decisión autoritativa de V-M3-9, y la regla 6 solo sirve para reconstruir `D*` y para el diagnóstico de la reconstrucción |
| V-M3-4 | control negativo: objetivo desplazado un ulp en la frontera | decide distinto |
| V-M3-5 | igualdad accidental exacta en `Σ_L⁺`: circunferencia trazada de radio 1,5 y recta `y = 1,5` | decide igual (clase 2) |
| V-M3-6 | igualdad accidental solo con valores no representables (`0.1 + 0.2` frente a `0.3`) | decide distinto, de forma exacta |
| V-M3-7 | correspondencia con el algoritmo vivo | para cada algoritmo de varias salidas, la rama exacta, elegida por la regla exacta de §10.2 con el estado capturado, tiene un encierro que contiene el valor vivo y es disjunto del de la otra rama; si no, `SELECTION_NOT_RECONSTRUCTED` |
| V-M3-8 | fuera de clase y presupuesto | operación trascendente del conductor: `EXACT_EVALUATION_OUT_OF_CLASS`; presupuesto agotado (inyección en el arnés): `EXACT_EVALUATION_BUDGET_EXHAUSTED`. Nunca una decisión |
| V-M3-9 | autoridad del objetivo en los cinco contactos (G-M3-4) | para cada contacto, M3 decide contra la autoridad del objetivo: `n` y `r` por los coeficientes binary64 de su recta soporte y su parte limitada, `h_2` por sus coeficientes binary64 y `c_1` por el centro y el semieje de su adaptador. Se registran la diferencia exacta entre la autoridad y la reconstrucción (proporcionalidad de las rectas; matriz frente a semieje de `c_1`), el signo exacto del residuo autoritativo y su magnitud aproximada. La incidencia solo se establece si se cumple exactamente con la autoridad |

### 3.3b Coherencia entre `Σ_L`, `Σ_L⁺` y `Σ_B` (comprobación final)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-CFG-1 | **incidencia demostrada en `Σ_B` y no demostrada en `Σ_L`**. `O = (0, 0)`; `c = Circle(O, (1, 1))`, que el corte captura como constante con `r² = fl(fl(√2)²) = 2,0000000000000004`; el conductor `P` sobre `d = Circle(O, 3)`; `Q = Intersect(c, Ray(O, P), 1)`; `L = LocusV2(Q, P)`; el objetivo `r = Circle(O, (1, −1))`, cuya autoridad es su adaptador (centro `O` y semieje `fl(√2)`, con `r² = fl(√2)²` exacto); `D` sobre `d` con coordenadas exactas, por ejemplo una salida de intersección | `Intersect(L, r, D)` indefinido con `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION` y el diagnóstico `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION` (en `Σ_B` los dos radios son `√2`); `Point(L, D)` definido; el residuo binary64 de `P̃` respecto de `r` es del orden de `10⁻¹⁶`, y **no** se acepta | una implementación que transfiriera la demostración desde `Σ_B`, o que aceptara la coincidencia numérica, definiría el punto |
| V-CFG-2 | el objetivo es la misma constante del corte (`r := c`) | con la precisión G-M3-4, la autoridad del objetivo es la representación de su adaptador (centro `O` y semieje `fl(√2)`), distinta de la matriz capturada (`r² = 2,0000000000000004` frente a `fl(√2)² = 2,00000000000000027…`): la incidencia se decide contra la autoridad y se refuta, con el diagnóstico `TARGET_AUTHORITY_MISMATCH` (matriz frente a semieje); con una circunferencia cuyas dos representaciones coinciden exactamente, se demuestra | una implementación que tomara la matriz como autoridad del objetivo la demostraría |
| V-CFG-3 | el objetivo es otro objeto de término idéntico, `Circle(O, (1, 1))` | su autoridad es su propio adaptador (centro `O` y semieje `fl(√2)`); M1 solo comparte una semántica si las representaciones coinciden exactamente; aquí se refuta como V-CFG-2 | una implementación que diera al objetivo la matriz de la constante por M1 la demostraría |
| V-CFG-4 | coincidencia de redondeo: un objetivo de término distinto con el mismo radio binary64, `Circle(O, (−1, 1))` | se recalcula (regla 6): refutada, como V-CFG-1; nunca se demuestra porque los dos radios binary64 coincidan | tomar el objetivo en su estado binary64 lo «demostraría» por coincidencia |
| V-CFG-5 | modelo del autor | con los objetivos reconstruidos, `Σ_L⁺` demostraría los cinco contactos (diagnóstico, V-M3-1); la decisión se toma contra la autoridad de cada objetivo (V-M3-9); el punto materializado sería `P_L`, nunca `P_B`; el radio de `c_1` de la decisión es el semieje de su adaptador, y su matriz capturada solo da un diagnóstico | una implementación que decidiera con los objetivos reconstruidos demostraría contactos que la autoridad no contiene |
| V-CFG-6 | conflicto de representación: el soporte del conductor, una circunferencia con `A4² + A5² − A2` distinto del cuadrado del semieje, usado también como operando de circunferencia en el corte | `CONFIGURATION_REPRESENTATION_CONFLICT`, con fallo cerrado | — |

### 3.4 Etapa B — evidencia de contacto (opción A)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-B-1 | los contactos de `t = 0` con `Intersect(L, r)` | clase 3 en el incremento mínimo; clase 2 con `CU3-B`; nunca una intersección de clase 3 | la F-2 original los materializaría |
| V-B-2 | contactos de la costura con `Intersect(L, r)` | con M3: `N = 1` doble en `π` (pliegue de `CU2` y M3 en `J*`), así que la raíz transversal publicada se reclasifica (V-CONF-5); sin M3: conserva su estado de G8B-R1 con la clase 3 visible, porque la clase 3 no es una contradicción | con M3, falla en la base |
| V-B-3 | recta `y = 1,5 + δ`, δ = +10⁻¹³ | `N = 0` certificado; ninguna solución publicada (V-CONF-1, H-CU3-3) | falla en la base |
| V-B-4 | δ = −10⁻¹³ y −10⁻¹⁰ | `N = 2` localizado; exactamente dos soluciones transversales, una por subcorchete (V-CONF-3, H-CU3-1, H-CU3-4) | falla en la base |
| V-B-5 | δ = 0 | clase 3 sin M3; clase 2 con M3 (V-M3-5); el resultado rico conserva la evidencia; sin objeto de punto crítico | — |
| V-B-6 | circunferencia frente a circunferencia, mismos δ | como V-B-3 a V-B-5 | falla en la base |
| V-B-7 (`CU3-B`) | `CONTACT_LOST` desde un contacto de clase 1 o 2 | punto indefinido; ningún token transferido | — |
| V-B-8 (`CU3-B`) | `CONTACT_SPLIT` en una tangencia regular | dos raíces con identidades nuevas | — |
| V-B-9 (`CU3-B`) | `CONTACT_SPLIT` en un pliegue | una intersección geométrica con dos preimágenes y una identidad (D-CU3-11) | — |
| V-B-10 (`CU3-B`) | `CONTACT_NOT_CERTIFIED` | punto indefinido; evidencia visible | — |
| V-B-11 | tangencia en un extremo incluido (cuarto de arco con `x = 1`) | con M3 (`h(0) = 0` y `h'(0) = 0` exactos, `h''` de signo fijo unilateral): reclasificada, nunca `TRANSVERSE_ESTABLISHED`; sin M3: conserva su estado de G8B-R1 con la evidencia visible | con M3, falla en la base |
| V-B-12 (`CU3-B`) | germen tangente | cambiar la escala de la ecuación del objetivo o invertir un segmento conserva el selector; invertir un rayo lo cambia | — |
| V-B-NOISE-1 | modelo con `α` 35° y `D_1` ×0,5 | con M3 (`N = 1` doble en `t = 0`: pliegue de `CU2`, M3 en `K*` y `h''` de signo fijo): ninguna raíz espuria publicada cerca de `t = 0` (H-CU3-1); sin M3: sin cambios, porque la clase 3 no es una contradicción | con M3, falla en la base |
| V-B-DUP-1 | traslación 10⁶ con radios 2 y 20 | exactamente dos soluciones (H-CU3-4) | falla en la base |

### 3.5 Grados de evidencia, admisibilidad y política de consumo

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-EVID-1 | vía G8B-R1 intacta | las suites G8B (kernel, ciclo de vida, topología), G8C1, G8C2 y G9U0-R4 pasan sin cambios, salvo las filas de conflicto identificadas; ninguna raíz transversal genérica cambia de admisibilidad ni de token | — |
| V-EVID-2 | grado visible | cada solución publica su grado; `LOCALLY_VERIFIED` en las capacidades sin envolventes; `EXISTENCE_CERTIFIED` cuando se calcula | — |
| V-EVID-3 | sin promoción | ningún grado sube por una tolerancia, por coincidencia de coordenadas, por una etiqueta o tras guardar y reabrir; un consumidor que exige un grado mínimo queda indefinido con su causa si deja de cumplirse | una promoción por persistencia o por tolerancia hace fallar la prueba |
| V-EVID-4 | segunda incidencia de la Etapa A | rechaza `LOCALLY_VERIFIED` y `EXISTENCE_CERTIFIED` como prueba de una igualdad en un punto prescrito | — |
| V-EVID-5 | regla de conflicto | V-CONF-1 a V-CONF-8 | — |
| V-EVID-6 | disparador | GC3-5 | — |

### 3.5b Regla de conflicto: certificados de conteo (comprobación final)

Todas en `Σ_L⁺`, con la continuidad exigida (una componente continua certificada, una caja de la
clase v3 válida). Ninguna fila retira nada por la mera existencia de un punto crítico.

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-CONF-1 | **ausencia**: δ = +10⁻¹³ (recta y circunferencia) | `N = 0` certificado: la solución `TANGENT_ESTABLISHED` publicada se retira (`CONTRADICTED_BY_CERTIFIED_EVIDENCE`); con una δ para la que la envolvente de `h` en la caja contiene el cero, no hay certificado `N = 0` y nada se retira | sin la regla, la tangencia sigue publicada; con una regla laxa, se retiraría también en el caso indecidible |
| V-CONF-2 | **una raíz transversal**: circunferencia de radio 1,5 frente a la secante `y = 0,75`, con un punto crítico de `h` certificado en una caja vecina | `N = 1` simple en la caja de la raíz: la solución se conserva con el grado `EXISTENCE_CERTIFIED`; los duplicados de su corchete se fusionan; el punto crítico vecino no la retira ni la reclasifica | una regla que tratara el punto crítico como contradicción la retiraría |
| V-CONF-3 | **dos raíces alrededor de un punto crítico**: δ = −10⁻¹³ y −10⁻¹⁰ | `N = 2` localizado en `[a, t*_l]` y `[t*_u, b]`: una solución por subcorchete, con las demás fusionadas; la solución tangente publicada en `t*` se retira (contradicción de existencia, porque `h(t*) ≠ 0`) | falla en la base (cuatro y tres soluciones) |
| V-CONF-4 | **clase 3 sola**: δ = 0, y una δ negativa con la envolvente de `h(t*)` que contiene el cero | ninguna solución verificada localmente se retira ni se reclasifica; la evidencia de clase 3 se muestra a su lado | una regla que retirara en clase 3 haría fallar la prueba |
| V-CONF-5 | **raíz doble exacta**: la costura y el contacto en `t = 0` del modelo, con M3 | `N = 1` doble: la raíz transversal de la costura se reclasifica y las raíces espurias de H-CU3-1 se retiran; con M3 desactivada o la puerta fallida, no cambia nada | sin M3, una regla basada en la clase 3 las retiraría indebidamente |
| V-CONF-6 | **continuidad y extremos** | una caja que corta un extremo de componente, la costura de una componente no cerrada, una banda `UNRESOLVED`, un hueco o una componente puntual no da certificado ni cambio; un extremo incluido con su decisión exacta (el cuarto de arco con M3) da un certificado unilateral | — |
| V-CONF-7 | **reclasificación al alza** | una solución tangente o indeterminada en una caja con `N = 1` simple certificado pasa a ser transversal `EXISTENCE_CERTIFIED` | — |
| V-CONF-8 | **contradicción solo del conjunto** | un resultado declarado completo con una sola raíz simple en una caja donde `C` deja `N ∈ {0, 1 doble, 2}`: la completitud pasa a `NOT_ESTABLISHED` y la solución se conserva | una regla que retirara la solución haría fallar la prueba |

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

- **Alcance de M3.** Prueba propiedades del objeto autoritativo en `Σ_L⁺`, que contiene la
  configuración capturada de `CU2`, no el valor escrito por el usuario. Una relación que el
  redondeo de las constantes capturadas rompe se refuta en `Σ_L⁺`, aunque la recalculación
  profunda `Σ_B` la demuestre (V-CFG-1). Es la limitación honesta del convenio de captura de
  `CU2`, y su remedio (alternativa C3) cambiaría un contrato aprobado.
- **Igualdades accidentales.** Una igualdad accidental se demuestra exactamente cuando es exacta en
  `Σ_L⁺` (V-M3-5) y se refuta cuando no lo es (V-M3-6). Ninguna fila acepta una coincidencia dentro
  de una tolerancia ni una coincidencia de redondeo (V-CFG-4).
- **Exactitud aritmética del modelo.** Las hipótesis del análisis §5.8, en particular la
  coordenada homogénea `z = 1`, se comprueban en la puerta, no aquí.
- **Barridos.** Son muestras finitas de las familias de parámetros: M3 decide cada revisión
  ejecutada, no todos los valores.
- **Ángulos de los pliegues.** Se calculan a partir de envolventes puntuales y son datos, no
  certificados.
- **Disparador de la regla de conflicto.** Se valida en los casos de la matriz; no demuestra que no
  exista otra raíz transversal contradicha fuera de sus disparadores.
- **Lectores anteriores.** La compatibilidad se comprueba con el producto actual, no con todas las
  versiones publicadas.

## 6. Ampliación candidata: autoridad constructiva e integridad de las relaciones (V-AUT)

Esta sección es una **ampliación candidata** de la conciliación de 2026-10-25
([propuesta](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md), §6.3, §13
y §14). No forma parte de la matriz aprobada el 2026-10-24, no la modifica y no tiene
aprobación autoral (`selfApproved=false`). Sus filas solo serán obligatorias si el autor aprueba
las decisiones D-AUT de las que dependen (§13 de la propuesta).

**Estado (2026-10-25).** El autor aprobó las decisiones D-AUT-1 a D-AUT-12 y la ampliación V-AUT
([registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md)). Desde entonces
las filas de esta sección son obligatorias para `CU3`, con las puertas de la propuesta §6.3.

Reglas propias:

- Las reglas del encabezamiento valen también aquí. La prueba adversaria falla con la capacidad
  desactivada o en la base de la implementación.
- Salvo que la fila diga otra cosa, las decisiones exactas se toman en `Σ_C`: la configuración
  constructiva, con los sustitutos de frontera declarados. `Σ_L` solo entra como representación
  certificada, por transferencia (§6.2).
- Bajo `legacy-representation-authority/v1`, las filas de §1 a §5 conservan su sentido aprobado.
  Bajo `constructive-authority/v1`, las filas que deciden en `Σ_L⁺` o contra la autoridad
  binary64 del objetivo se leen con AUT-7. Entre ellas están G-M3-4, V-M3-9, V-CFG-1 a V-CFG-6 y
  V-MAND-1 a V-MAND-3. Con AUT-7, la autoridad del objetivo es el objeto constructivo y la
  configuración de decisión es `Σ_C`. Esa lectura no reescribe ninguna fila aprobada: es la
  revisión que D-AUT-11 somete al autor.
- `RELAXED` nunca es prueba: ninguna fila lo acepta como premisa de `STRICT`.
- Un sondeo transitorio puede identificar objetos por etiqueta o por referencia Java. Una prueba
  de producto nunca lo hace (§6.6).

### 6.1 Adopción y política de documento (D-AUT-2)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-ADOPT-1 | documento nuevo | nace sin registro de política, con `legacy-representation-authority/v1`; al crear una forma de `CU3`, el comando ofrece el evento de adopción y, si se rechaza, falla cerrado con causa tipada | un producto que diera por defecto la política constructiva a los documentos nuevos haría fallar la prueba |
| V-AUT-ADOPT-2 | documento nuevo o vacío que adopta la política | `AUTHORITY_POLICY_ADOPTION` explícito y registrado, con informe de impacto vacío | una adopción implícita, sin evento, haría fallar la prueba |
| V-AUT-ADOPT-3 | documento existente: modelo del autor y corpus de `CU0`, `CU1` y `CU2` | abrir, editar, guardar, Deshacer y Rehacer nunca cambian la política; la adopción calcula su informe de impacto antes de aplicarse, se aplica en una sola transacción y no cambia ninguna identidad, token ni selector | — |
| V-AUT-ADOPT-4 | Deshacer y Rehacer de la adopción | Deshacer restaura la política anterior y sus resultados; Rehacer aplica la misma adopción con el mismo informe; no existe la vuelta automática | — |
| V-AUT-ADOPT-5 | sin documentos mixtos | ninguna operación deja dos autoridades en un documento | elegir la autoridad por relación o por comando haría fallar la prueba |
| V-AUT-ADOPT-6 | copiar, pegar y macros entre políticas | rige la política del destino; pegar formas de `CU3` en un documento sin la política constructiva sigue V-AUT-ADOPT-1 | — |
| V-AUT-ADOPT-7 | lector anterior | un documento con el registro de política, abierto con el producto de la base de la implementación, falla cerrado con causa tipada (patrón `MALFORMED_RECORD` del ADR 0029); nunca se lee con otra autoridad ni pierde el registro al guardarse | un lector que ignorara el registro cargaría el documento con la autoridad antigua |
| V-AUT-ADOPT-8 | recalificación de G8B-R1 en la adopción (D-AUT-6) | cada raíz que G8B-R1 admitió se recalifica una sola vez: `STRICT` si su corchete se certifica de nuevo contra el objetivo constructivo, `RELAXED` por (c2) o `NOT_ESTABLISHED`; un resultado contradicho pierde su admisibilidad con causa tipada, sin reasociación; el informe de impacto enumera las raíces de cada clase | heredar automáticamente la admisión haría fallar la prueba |

### 6.2 Transferencia de certificados de `CU2` (D-AUT-4)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-TRF-1 | margen: la componente de `e_1` que contiene `t*` | se transfiere con encierros de transferencia que contienen el valor capturado y el constructivo, con todos los signos estrictos; `P ∈ L` `STRICT`, con la vía `TRANSFERRED_BY_MARGIN` registrada | con un ulp de perturbación junto a una frontera de decisión, `OPEN_MARGIN_INSUFFICIENT` |
| V-AUT-TRF-2 | estructura: los pliegues de `e_1` (`fold-symmetry/v1`) y la periodicidad estructural de la costura | se transfieren si todas sus premisas estructurales son `STRICT` bajo la autoridad constructiva | con una premisa estructural no estricta, `STRUCTURAL_PREMISE_NOT_STRICT` |
| V-AUT-TRF-3 | nueva decisión: un conteo de Sturm, una multiplicidad o el valor crítico de la mordedura, pedidos por un consumidor estricto | se vuelven a decidir en `Σ_C` por un método admitido; si no hay vía, `CLOSED_DECISION_NOT_REDECIDED`, y si la nueva decisión refuta, `CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION` | consumir una componente puntual o la decisión cerrada de `Σ_L` sin decidirla de nuevo haría fallar la prueba |
| V-AUT-TRF-4 | correspondencia y revisión | una huella distinta o un estado de selección cambiado da `CORRESPONDENCE_NOT_DEMONSTRATED`; fallan cerrados tanto el consumidor estricto como el que acepta `RELAXED` | — |
| V-AUT-TRF-5 | presupuesto | con un presupuesto inyectado, `BUDGET_EXHAUSTED` y fallo cerrado estricto; nunca una decisión | — |
| V-AUT-TRF-6 | certificado de origen intacto | en todos los casos anteriores, los bytes, la huella, los componentes y las claves del certificado de `CU2` son iguales antes y después, y siguen valiendo para `Σ_L` | — |

### 6.3 Procedencia, intrinsecidad e integridad (D-AUT-1, D-AUT-3 y D-AUT-9)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-PROV-1 | cierre de procedencia (G-AUT-1) | con la semántica de los puntos sobre caminos de D-AUT-3, el evaluador constructivo hace exactas las relaciones de procedencia del modelo y de un corpus sintético; ninguna es `STRICT` sin su hecho de existencia establecido rigurosamente; el censo 160/160 del estudio solo entra como regresión | un punto sobre camino sin la semántica de camino refuta su camino; una recta tomada en binary64 refuta sus puntos |
| V-AUT-PROV-2 | intrinsecidad (G-AUT-3) | una sola semántica por objeto con cualquier conductor, captura y consumidor | las cuatro semánticas de `h_2` del estudio deben dar una sola |
| V-AUT-INT-1 | propagación por premisas (G-AUT-5) | en un grafo sintético con premisas `STRICT`, `RELAXED` y `CONTRADICTED`, la integridad de cada relación es la de su método, acotada por la peor premisa que usa su prueba; lo que depende de una premisa `CONTRADICTED` pasa a `NOT_ESTABLISHED`, y solo es `CONTRADICTED` lo que se refuta por sí mismo | una premisa lejana `RELAXED` que contaminara una incidencia de procedencia haría fallar la prueba |
| V-AUT-INT-2 | consumidores e interfaz | un consumidor que exige `STRICT` y no la recibe falla de forma explícita, con causa tipada; la consulta de integridad enumera las premisas `RELAXED`, separada de `NumericGuarantee`; `RelationIntegrity` no se persiste ni entra en los tokens | — |
| V-AUT-INT-3 | sin blanqueo (G-AUT-6) | ninguna relación `CONTRADICTED` pasa a `RELAXED`; un residuo binary64 dentro de la tolerancia no cambia una contradicción constructiva | admitir como relajado un contacto refutado haría fallar la prueba |

### 6.4 Política `RELAXED`: `SUBSTITUTE_GENERIC` versión 1 (D-AUT-5)

Las filas siguen la definición operativa de la propuesta §4.2b. En el modelo del autor, `m = 2` y
`N = 8`.

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-RLX-1 | igualdad accidental de grado 1: el control C3, con `I` sobre la vertical constante que pasa por su sustituto | se cumple en el sustituto y no en las perturbaciones: no hay evidencia (c1) y la relación queda en `NOT_ESTABLISHED` | una política que bastara con la igualdad en el sustituto la admitiría |
| V-AUT-RLX-2 | **un acuerdo finito no es una identidad**: relación sintética del evaluador, en una prueba directa del motor, cuyo residuo es la cúbica no nula que se anula en los `N + 1 = 9` puntos de la versión 1, calculada por álgebra lineal exacta a partir de ellos | en E1, `SUBSTITUTE_GENERIC` la admite: es `RELAXED` por (c1), nunca `STRICT` ni premisa de `STRICT`, y se presenta como evidencia computacional, no como prueba; en E2, el certificado da `NOT_AN_IDENTITY` para la misma relación y celda, retira la evidencia y la relación queda en `NOT_ESTABLISHED` | tomar el acuerdo en los nueve puntos como prueba daría `STRICT`; no retirar la evidencia tras `NOT_AN_IDENTITY` la dejaría en `RELAXED` |
| V-AUT-RLX-3 | igualdad accidental de grado 2: residuo cuadrático no nulo que se anula en la base `z`, por ejemplo `(w₁ − z₁)² − (w₂ − z₂)²` en las coordenadas de frontera `w` | la condición de rango lo rechaza: al menos un punto perturbado da un residuo no nulo, y la relación queda en `NOT_ESTABLISHED` | sin la condición de rango, un generador cuyas direcciones cumplieran `|ρ₁d₁| = |ρ₂d₂|` la aceptaría |
| V-AUT-RLX-4 | producto de dos sustitutos que se anula en la base, `(w₁ − z₁)(w₂ − z₂)` | se rechaza: cada dirección mueve todas las coordenadas, porque se descartan las salidas nulas del generador | perturbar un sustituto cada vez lo aceptaría |
| V-AUT-RLX-5 | celda operativa y ramas | una base tan cerca de una frontera entre celdas que una perturbación cambia un signo da `PERTURBATION_LEFT_CELL` y `NOT_ESTABLISHED`; una rama de la base fuera del corte del conductor que no corresponde a la salida viva da `NOT_ESTABLISHED` con su causa; en cada perturbación, la rama es la misma rama formal (índice de raíz bruta y pertenencia) | volver a elegir en cada punto la rama que cumple la relación aceptaría la intersección 1 de S2 |
| V-AUT-RLX-6 | determinismo, posición general y precondiciones | dos ejecuciones en la misma revisión dan los mismos puntos y resultados; los `N + 1` puntos no están sobre ninguna cuádrica, comprobado con aritmética exacta; con un generador degenerado inyectado, el tope de `64·N` salidas da `NOT_ESTABLISHED`; un objeto fuera de la clase sin declarar como frontera y un presupuesto inyectado dan `NOT_ESTABLISHED` con causa tipada | una semilla distinta por ejecución o por sesión daría resultados distintos |
| V-AUT-RLX-7 | los cinco contactos obligatorios en E1 | `P ∈ r` `RELAXED` por (c1), con el sustituto de `H` como premisa no constructible registrada, y `P ∈ L` `STRICT` por transferencia (V-AUT-TRF-1); se recalcula en cada revisión, y un arrastre sin cambio de revisión no repite nada (GC3-3) | con (c1) desactivado, ningún contacto pasa a `RELAXED` por (c2), aunque su residuo constructivo quede dentro de `τ_v1`: no tienen cambio de signo |

### 6.5 Decisiones numéricas heredadas (D-AUT-3, §2.6b de la propuesta)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-NUM-1 | definición condicionada con condición sin establecer (D3): `P = If(b, P₁, P₂)` | ninguna relación cuya prueba use la decisión es `STRICT` (P1); una identidad que vale para todo valor del corte solo es `STRICT` si la caja certificada contiene las dos ramas y cabe en la celda, y si no, queda en `NOT_ESTABLISHED` (P2); con (c1), `RELAXED`, con la decisión nombrada como premisa no establecida (P5) | encerrar solo la rama elegida daría `STRICT` a través de una decisión sin establecer |
| V-AUT-NUM-2 | objeto condicionado por un booleano de D2 (`AreConcyclic`, `Relation`, una comparación) frente a una condición polinómica de la clase (`x(A) > 0`) | con el booleano, la existencia o la selección del objeto queda en `NOT_ESTABLISHED` (P4); con la condición de la clase, el certificado decide su signo, lo registra en la celda y lo recomprueba, y el objeto deja de ser frontera (P3); al cruzar `x(A) = 0`, la prueba queda inaplicable y se intenta otra en la celda nueva | tomar el booleano heredado como premisa daría `STRICT`; sin la condición en la celda, la prueba de una rama sobreviviría al cambio de rama |
| V-AUT-NUM-3 | tangencia decidida por tolerancia (D1): recta casi tangente a una circunferencia, con `isEqual(dis, 0, delta)` de `AlgoIntersectLineConic` | la existencia y la multiplicidad de la salida ideal se deciden por un signo certificado o una decisión exacta, nunca por la tolerancia; si no se deciden, `NOT_ESTABLISHED`; la salida heredada no cambia | aceptar la tolerancia daría `STRICT` a un punto que no existe idealmente, o suprimiría uno que sí existe |
| V-AUT-NUM-4 | extremo de segmento (D1): punto aceptado por tolerancia junto al extremo de un segmento o un rayo | la pertenencia a la parte limitada se decide con el parámetro constructivo, con signo certificado o exacto; fuera, `NOT_ON_LIMITED_TARGET`; sin decidir, `NOT_ESTABLISHED` | la pertenencia por tolerancia daría `STRICT` fuera de la parte limitada |
| V-AUT-NUM-5 | selección condicionada (D4): `Element(lista, n)` con un índice calculado, e `Intersect` con punto de referencia | fuera de la clase: el objeto elegido es frontera declarada; una relación que dependa de él es como mucho `RELAXED`, con la selección como premisa no establecida, salvo P2 sobre todos los candidatos | tomar la selección heredada como premisa rigurosa daría `STRICT` |
| V-AUT-NUM-6 | comportamiento heredado sin cambios (P6) | en cada categoría D1 a D5, las salidas heredadas (valores, booleanos, índices, textos de `Relation`) son iguales byte a byte con las dos políticas; las de D2 no tienen integridad; GeoCeDG no añade ninguna reinterpretación por tolerancia | un producto que cambiara `AreEqual` o `Relation` bajo la política constructiva haría fallar la prueba |

### 6.6 Identidad durable y clave estructural del certificado (D-AUT-8)

La clave es `cic-structural-key/v1` (propuesta §5.4b). Cada fila se comprueba sobre el modelo del
autor y sobre una construcción sintética, con el certificado de la relación antes y después de la
operación.

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-ID-1 | renombrar un objeto del cono: el objetivo, un intermedio y el conductor | las identidades durables y la clave no cambian; la prueba se recomprueba y sigue siendo aplicable, con la misma integridad | la firma por etiquetas del prototipo haría inaplicable una prueba válida |
| V-AUT-ID-2 | redefinir un objeto del cono, aunque conserve su identidad por retención compatible (ADR 0029) | cambia su definición estructural y con ella la clave: la prueba queda inaplicable y se intenta otra | una recomprobación solo de signos aceptaría el certificado viejo |
| V-AUT-ID-3 | eliminar y volver a crear con la misma etiqueta (S4 en el producto) | otra identidad durable y otra clave: la prueba queda inaplicable, sin reasociación; eliminar un ancestro elimina el sujeto de la relación, y ningún certificado le sobrevive | una correspondencia por etiqueta conservaría el certificado viejo, que en S4 es falso |
| V-AUT-ID-4 | Deshacer tras un cambio del cono | se restauran las identidades, los roles, las dependencias y las revisiones; el certificado se vuelve a evaluar y coincide con el de antes del cambio; ninguno viejo sobrevive | — |
| V-AUT-ID-5 | Rehacer | el certificado coincide con el de después del cambio | — |
| V-AUT-ID-6 | guardar y reabrir | el archivo no contiene certificados ni integridades; tras la carga se recalculan con la misma clave, porque las identidades durables sí se persisten, y dan los mismos resultados de forma determinista | — |
| V-AUT-ID-7 | copiar o duplicar | identidades nuevas, claves nuevas y certificados nuevos, sin reutilizar ninguno | reutilizar el certificado del original haría fallar la prueba |

### 6.7 Certificado de identidad constructiva (D-AUT-8; G-AUT-2 y G-AUT-9)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-CIC-1 | las cinco incidencias obligatorias, en los alcances de frontera y automático | `IDENTITY_CERTIFIED` en su celda, con `t*` estructural, el orden de contacto por jets exactos y los valores ideales de los puntos sobre caminos y de los cortes encerrados rigurosamente (E1(d)) | una caja que no contiene el valor ideal da `NOT_ESTABLISHED`, nunca `IDENTITY_CERTIFIED` |
| V-AUT-CIC-2 | controles C1 a C5 de la propuesta §5.5 | C1, C2, C3 y C5 dan `NOT_AN_IDENTITY`, y C4 da `NOT_ESTABLISHED`; `NOT_AN_IDENTITY` nunca es una contradicción | un objetivo desplazado un ulp no se certifica |
| V-AUT-CIC-3 | construcciones sintéticas S1 a S3 | los teoremas se demuestran; las conjeturas falsas no; la prueba depende de la rama (S2); un corte olvida relaciones y nunca las inventa (limitación de S3) | — |
| V-AUT-CIC-4 | correspondencia de ramas | fuera del corte del conductor, la salida viva se certifica estrictamente más cerca de la rama elegida que de la otra; si las ramas casi coinciden y no se distinguen, `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`; dentro del corte rige el estado de selección capturado, cuya fidelidad aporta `CU2` por transferencia | una inversión de signo inyectada no da ningún certificado |
| V-AUT-CIC-5 | caso especial heredado de un punto ya incidente | la rama que devuelve la regla heredada (la intersección 1 de S2, que es `TA`) se reproduce y se contrasta con la salida viva, sin suponerla | — |
| V-AUT-CIC-6 | resultados tipados | segunda raíz independiente: `OUT_OF_CLASS`; presupuesto inyectado: `BUDGET_EXHAUSTED`; objeto indefinido: `INVALID` (`OBJECT_UNDEFINED`); precondición sin decidir: `PRECONDITION_UNDECIDED`; nunca una decisión por defecto | — |

### 6.8 Dinámica (G-AUT-10)

| Id | Caso | Criterio | Prueba adversaria |
|---|---|---|---|
| V-AUT-DYN-1 | las doce posiciones del estudio y movimientos de `B` y `H` | con la misma clave, recomprobación sin trabajo simbólico nuevo; un cambio de constante deja inaplicable la prueba de frontera y no la del alcance automático; con `H` +40 (otra celda), la recomprobación de los contactos en `K` falla cerrada y las pruebas nuevas dan los resultados de la propuesta §5.5, entre ellos la otra rama de `H_1`; con `D_1` ×3, la salida de la parte limitada certificada o `INVALID` | ninguna prueba sobrevive a un cambio de clave, de constante, de selección o de celda |
| V-AUT-DYN-2 | transición real de un estado de selección | la clave cambia, la prueba anterior queda inaplicable y se intenta otra | una recomprobación solo de signos aceptaría la prueba anterior |
| V-AUT-DYN-3 | arrastre sin cambio de revisión | ninguna evaluación exacta ni simbólica nueva (GC3-3) | — |
| V-AUT-DYN-4 | determinismo | dos ejecuciones en la misma revisión dan las mismas integridades, causas, premisas y contadores (como V-DET-CU3-1) | — |

### 6.9 Lo que estas filas no pueden demostrar

- **`SUBSTITUTE_GENERIC`** es evidencia computacional positiva. V-AUT-RLX-2 deja constancia de que
  un acuerdo en un conjunto finito de puntos no es una identidad, y ninguna fila de §6.4 demuestra
  una incidencia.
- **El certificado** vale en su celda y con los valores ideales encerrados. Las doce posiciones son
  recomprobaciones, no la demostración.
- **La recalificación de G8B-R1** se prueba en el corpus y en casos construidos, no en todos los
  documentos existentes.
- **Los lectores anteriores** se prueban con el producto de la base de la implementación, no con
  todas las versiones publicadas.
