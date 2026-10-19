# PRE-G9B-CU — Plan consolidado del track de corrección y usabilidad

| Campo | Valor |
|---|---|
| Carácter | Plan vivo del track `PRE-G9B-CU`. No es especificación normativa: las decisiones arquitectónicas se incorporan a sus especificaciones mediante el procedimiento de revisión y aprobación |
| Estado del marco | `PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS` (instrucción autoral «PRE-G9B-CU — Author Plan Approval, Architectural Refinements and CU0 Authorization», 2026-10-19) |
| Estado de esta consolidación | `CU0 CONSOLIDATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Paquetes | `CU0` autorizado y ejecutado como candidato pendiente de revisión; `CU1`–`CU10` **no autorizados** |
| Base | candidato de planificación `444ad46b1c18a6101dae68a75a8d7a5f92ed83fe` (árbol `1aff549ccf3c0deff5581ee7942d566bd5cc4e19`), hijo de `main` `48a0775e4f2f122f5e2614c62b5773777752ccd1`; local, no publicado |
| Antecedente | el [plan candidato](pre_g9b_cu_corrective_usability_plan_candidate.md) revisado por el autor conserva las fichas detalladas de B01–B12 y M01–M10 (su sección B.2); este documento lo sustituye en estado, decisiones, paquetes y secuencia |
| Evidencia | [informe de caracterización inicial](../validation/pre_g9b_cu_characterization_evidence_report.md); [informe del candidato CU0](../validation/pre_g9b_cu0_baseline_candidate_report.md); [línea base geométrica](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json) |

## 1. Restricciones permanentes

- La arquitectura CeDG, la integridad del DAG, la identidad semántica y las garantías de
  persistencia son obligatorias (`AGENTS.md` §1, §11 y §16).
- La aprobación del marco no hace normativas las propuestas técnicas: cada decisión se
  incorpora a su especificación o ADR con aprobación propia.
- Cada paquete exige autorización autoral explícita con commit base exacto; un PASS
  técnico no es aprobación autoral.
- No se reabren fases históricas ni se alteran sus registros.
- El libro `geocedg_book` permanece en pausa durante el track (§8).

## 2. Decisiones adoptadas por la instrucción de 2026-10-19

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Marco | `PRE-G9B-CU` aprobado como marco de planificación con enmiendas | este documento |
| B10.2 | La discontinuidad de `e_2` no se acepta automáticamente; analizar la longitud orientada sin cambiar en silencio `LocusLength` | [análisis D](pre_g9b_cu0_oriented_length_analysis.md); `CU2` |
| DA-1 | Dirección híbrida aprobada (evaluación puntual, evidencia exploratoria, componente localmente certificada, completitud global); buscar activamente certificados locales en extremos, tangencias y transiciones; especificación concreta aprobada **antes de `CU2`** | [propuesta E](pre_g9b_cu0_hybrid_certification_proposal.md) |
| DA-3 | Opción C por etapas: A, puntos estructurales con las dos incidencias; B, materialización de tangencias certificadas localmente. Casos obligatorios `Intersect(e_1, n)`, `Intersect(e_1, c_1)`, `Intersect(o_1, h_2)`. B08/B09 no se cierran solo con una construcción alternativa | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md); `CU3` |
| Precisión | Estudiar el control de criterios numéricos sin implementar controles; un umbral no convierte una raíz no certificada en certificada | [estudio H](pre_g9b_cu0_precision_and_tolerance_study.md) |
| DA-5 | Se retira la exigencia de coincidencia geométrica en M09; `Deshacer`/`Rehacer` exactos; reemplazo con identidad nueva cuando no se pueda conservar | [diseño G](pre_g9b_cu0_redefinition_and_canvas_picking_design.md) parte 1; `CU7-D` |
| M07 | Elevada a mejora importante de `CU6`; la modalidad Swing no es impedimento; diseño previo dentro de la preparación de `CU6` sin bloquear las correcciones científicas | diseño G parte 2 |
| B03 | Activación persistente por herramienta durante la sesión, sin interferencia; indicación de estado clara; referencias no disponibles en el paso actual suspendidas; ninguna reasociación por etiqueta, posición, índice o semejanza; recuperación tras reconstrucción solo con correspondencia demostrable | `CU5` |
| H02 | La tolerancia nunca depende del tamaño de presentación; residuos normalizados con escalas características; pruebas con raíces existentes, ausentes, tangencias y casi-tangencias | `CU3` |
| DC-1 | Dirección aceptada: ampliar la matriz `P4` a ángulos libres y textos estáticos con las garantías transaccionales existentes | `CU4` |
| DC-3 | Dirección aceptada: persistencia de preferencias de estilo y soporte coherente de Locus V2 y SplineV2, sin cambiar retroactivamente objetos existentes | `CU4` |
| DC-4 | Dirección aceptada: ejes como referencias portadoras de orientación, sin dibujar indicaciones sobre los propios ejes | `CU5` |
| DC-6 | Criterios de teselación adicionales con límites justificados por pruebas reproducibles; 10° y profundidad 4 son candidatos, no constantes aprobadas | `CU2` |
| CU8 | Solo diseño inicialmente; implementación con autorización separada | `CU8-D` |
| Libro | En pausa; actualización mínima de la base técnica solo tras el cierre, la aprobación y una etiqueta nueva, con autorización separada | §8 |

## 3. Incidencias y paquetes

Prioridad tras `CU0`. Las incidencias H10–H12 son hallazgos nuevos de `CU0`.

| Id | Incidencia | Prioridad | Paquete |
|---|---|---|---|
| B10.1 / H03 / H04 | `e_2` bloquea la aplicación; rotación de revisiones; margen del presupuesto métrico | P0 / P1 / P2 | `CU1` |
| **H10** | `LocusLength` devuelve la cuerda (1,0) en vez de la longitud de arco para `y = sin(2πkx)` en `[0,1]`, sin aviso | **P0** | `CU1` |
| **H11** | longitud indefinida (presupuesto agotado) para una circunferencia Locus V2 de radio ≥ 8 | **P1** | `CU1` |
| **H12** | precisión métrica ≈ 6–8·10⁻⁹ relativa frente a 10⁻⁹ declarada; error no comparado con la tolerancia | P2 | `CU1` |
| B10.2 | `e_2` dibujado cerrado | P0 | `CU2` |
| B07 / H01 | huecos y desaparición de loci | P1 | `CU2` |
| B06 | rizado de presentación | P2 | `CU2` |
| B08 / B09 | contactos tangentes no materializables | P1 | `CU3` |
| H02 / H06 | escala de tolerancias; ejes como objetivos | P1 / P3 | `CU3` |
| B01, B05, M01, M05.1, H08, H09 | inserción, valores por defecto, etiquetas, estilos | P1–P3 | `CU4` |
| B02, B03, M04, H05 | protocolo, memoria de autoría, orientación | P1–P3 | `CU5` |
| M07 | nombres desde el lienzo en diálogos | **P1** (elevada) | `CU6` |
| B04, M02, M03, M06, M10 | interacción y presentación | P2–P3 | `CU6` |
| M08, M09 | punto sobre Locus V2; redefinición sin coincidencia | P2 | `CU7` |
| M05 | contenedores de controles | P3 | `CU8` (diseño) |
| B11, B12, H07 | documentación y guía | obligatoria | `CU9` |

## 4. Paquetes

### CU0 — Línea base reproducible, caracterización y decisiones (autorizado; candidato)

Entregado en el candidato `CU0` (ver el [informe](../validation/pre_g9b_cu0_baseline_candidate_report.md)):
corpus con manifiesto; pruebas de caracterización versionadas y selección
`PRE-G9B-CU0`; banco reproducible y escenarios ventaneados en `tools/benchmark/pre-g9b-cu/`;
línea base geométrica; reproducción ventaneada de B02/B03 con el `main` del producto y
el JDK 25 del producto; matriz de cobertura de la guía; análisis D; propuestas E, F, G y H;
enmiendas normativas candidatas; hoja de decisiones; conciliación del estado vivo.

### CU1 — Conformidad de evaluación, revisión, composición anidada y métrica

- **Objetivo:** que `e_2` y cualquier Locus V2 que dependa de métricas u otros loci se
  evalúe sin bloquear la aplicación y con longitudes correctas, conforme a
  `locus-v2-semantics.md` y `locus-v2-metrics.md`.
- **Alcance:** revisión semántica por contenido (mover solo el conductor no republica);
  corte reconstruido una vez por revisión; copias anidadas estables y reutilización del
  índice métrico; sesión propagada a la evaluación anidada; contadores anidados públicos;
  presupuesto métrico con fallo explícito (H04, H11); corrección de la métrica de
  evaluador: profundidad mínima, control de consistencia, tolerancia relativa repartida y
  admisibilidad condicionada al error (H10, H12).
- **Fuera de alcance:** existencia y dibujo (`CU2`), materialización (`CU3`).
- **Aceptación:** puertas por contadores (decisión C1-1), referencias `preserve` de la
  línea base reproducidas, fijaciones B10.1, H03, H10 y H11 invertidas por pruebas de
  aceptación propias, banco antes/después.
- **Verificación:** `INTEGRATED_PHASE` (`PHASE` propio + `INTEGRATION`); el inventario
  de selecciones se actualiza con el procedimiento oficial.

### CU2 — Existencia, continuidad, fronteras y representación

- **Prerrequisitos:** `CU1`; aprobación de la especificación de certificación híbrida
  (DA-1) y de las decisiones E-1 a E-3 y D-1.
- **Alcance:** cuatro niveles de evidencia; certificación estructural (totalidad,
  periodicidad, eventos de dependencia como el `wrapEvent` métrico); programa de
  construcción clase v3 con resultado `UNIFORMLY_INVALID`; transiciones certificadas;
  regla de publicación (H01); consumidores alineados con §11; linaje de componentes;
  `e_2` como componente abierta cortada en el origen de su longitud orientada; dibujo
  por componente hasta la frontera, sin uniones ni cierres no probados; teselación
  (B06) con límites justificados por el banco.
- **Aceptación:** las fijaciones B07, H01, B10.2 y B06 se invierten; referencias
  `preserve` (puntos, extremos localizados, longitud orientada) se mantienen; cadena
  `o_1 → LocusLength → d_2 → e_2` válida sin certificado de compatibilidad.
- **Verificación:** `INTEGRATED_PHASE`, con las regresiones DXF/LaTeX de exportación.

### CU3 — Materialización: puntos estructurales y contactos tangentes

- **Prerrequisitos:** `CU2`; decisiones F-1, F-2, H-2 y H-3.
- **Alcance:** Etapa A (`Point(L, D)`, `Intersect(L, r, D)` con verificación de la
  segunda incidencia); Etapa B (clase de contacto tangente certificada, germen tangente,
  vigencia, desaparición y desdoblamiento); normalización de residuos (H02); ejes
  (H06); clasificación de contacto por punto crítico.
- **Aceptación:** los tres casos obligatorios materializados con identidad y dinámica;
  la raíz transversal de la costura se conserva; barrido de escala; casi-tangencias.

### CU4 — Valores por defecto, etiquetado, estilos e inserción

Alcance del plan candidato (B05, M01, M05.1, B01, H08, H09) con las direcciones DC-1 y
DC-3 aceptadas.

### CU5 — Flujo de construcción en la interfaz

B02 (política estable de alto de filas: la reproducción ventaneada muestra 20 px tras
abrir o refrescar la interfaz y 36/40 px tras una edición, un deshacer o un cambio de
idioma), B03 (activación persistente por herramienta, indicación de estado, suspensión
por paso, sin reasociación), M04 (DC-4) y H05.

### CU6 — Interacción y presentación

Preparación de diseño de M07 al comienzo del paquete (`CU6-D`), seguida de M07 como
prioridad y de B04, M02, M03, M06 y M10.

### CU7 — Puntos sobre Locus V2 y redefinición

`CU7-D` (ADR y especificaciones de M08 y de M09 reformulada) con aprobación propia;
`CU7-I` después.

### CU8 — Contenedores de controles

`CU8-D` solamente; `CU8-I` requiere autorización separada.

### CU9 — Guía de usuario

Revisión exhaustiva, completa, bilingüe y comprensible, partiendo de la
[matriz de cobertura](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json)
(267 entradas: 116 cubiertas, 119 parciales, 20 ausentes, 12 excluidas con razón).

### CU10 — Cierre integrado

Cierre del alcance efectivamente autorizado y ejecutado: candidato integrado, `FINAL`
una vez, validación dinámica con los modelos del autor, smoke autoral, informe de
limitaciones, versión y, si se autoriza, publicación con etiqueta anotada nueva.

## 5. Secuencia

```text
CU0 ──► CU1 ──► CU2 ──► CU3 ──► CU7-D ──► CU7-I ─┐
  ├──► CU4 ────────────────────────────────────────┤
  ├──► CU5 ──► CU6-D ──► CU6 ─────────────────────┼──► CU9 ──► CU10
  └──► CU8-D ─────────────────────────────────────┘
```

`CU1`–`CU3` son la línea principal de estabilización científica. `CU4`, `CU5` y `CU8-D`
pueden avanzar en paralelo tras `CU0` porque tocan zonas disjuntas.

## 6. Compromisos de cierre y elementos diferibles

| Compromiso del cierre del track | Diferible con decisión autoral expresa |
|---|---|
| `CU1` completo (B10.1, H03, H04, H10, H11, H12) | perfil de esfuerzo de documento (H-d) |
| `CU2` completo (B07, B10.2, H01, B06) | ampliaciones de la clase v3 que no necesite el corpus |
| `CU3`: Etapa A y los tres casos obligatorios; H02; H06 | Etapa B para contactos fuera de los casos obligatorios, si F-2 lo limita |
| `CU4` (B01, B05, M01, M05.1, H09) | H08 (renombrado con subíndices) |
| `CU5` (B02, B03, M04, H05) | — |
| `CU6`: M07 | B04, M02, M03, M06, M10 |
| `CU7-D` (diseño aprobado de M08 y M09) | `CU7-I` |
| `CU8-D` | `CU8-I` |
| `CU9` completo | — |

Una funcionalidad nueva pendiente no impide cerrar las correcciones científicas ya
verificadas.

## 7. Verificación

Clases de planificación según el registro vivo: `BOUNDED_PHASE` → `PHASE` registrado;
`INTEGRATED_PHASE` → `PHASE` + `INTEGRATION`; `GLOBAL_IMPACT` → `FINAL`. Cada paquete
registra su selección `PHASE`; la selección `PRE-G9B-CU0` reúne las pruebas de
caracterización y sirve a todos los paquetes para comparar antes y después. Las puertas
de rendimiento usan contadores deterministas; los tiempos son informativos. `FINAL` se
ejecuta una vez en `CU10`.

## 8. Libro

El libro permanece en pausa tras BOOK-FIGPOL-PILOT. Durante el track no se modifican el
libro, las figuras del piloto, la base técnica editorial ni los capítulos, y no se
inician fases de redacción. Tras el cierre del producto, la aprobación autoral y una
etiqueta nueva de GeoCeDG, una fase editorial autorizada por separado actualizará la base
técnica mínima. Las etiquetas históricas y los registros de cierre anteriores no se
modifican.

## 9. Decisiones pendientes

Las decisiones necesarias para autorizar `CU1` y preparar `CU2`/`CU3` están en la
[hoja de decisiones](../validation/pre_g9b_cu0_author_decision_sheet.md); el texto de las
enmiendas candidatas, en las
[enmiendas normativas candidatas](pre_g9b_cu0_candidate_normative_amendments.md).
