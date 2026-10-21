# PRE-G9B-CU — Plan consolidado del track de corrección y usabilidad

| Campo | Valor |
|---|---|
| Carácter | Plan vivo del track `PRE-G9B-CU`. No es especificación normativa: las decisiones arquitectónicas se incorporan a sus especificaciones mediante el procedimiento de revisión y aprobación |
| Estado del marco | `PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS` (instrucción autoral «PRE-G9B-CU — Author Plan Approval, Architectural Refinements and CU0 Authorization», 2026-10-19) |
| Estado de esta consolidación | aceptada con `CU0`, que está `PASS — AUTHOR APPROVED` (decisión autoral de 2026-10-20; [registro de cierre](../validation/pre_g9b_cu0_closeout_record.md), única autoridad de ese estado); las propuestas D–H y las enmiendas candidatas siguen siendo candidatas, no normativas; `selfApproved=false` |
| Paquetes | `CU0` y `CU1` `PASS — AUTHOR APPROVED` (2026-10-20; [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md), única autoridad de ese estado); arquitectura de `CU2` `AUTHOR APPROVED` con la precisión D-CU2-20-R e implementación autorizada, condicionada, sobre `070af6f9` (2026-10-21; [registro de aprobación](../validation/pre_g9b_cu2_author_architecture_approval_record.md), única autoridad de ese estado), en curso en la rama local `phase/pre-g9b-cu2-implementation`; `CU2` no está `PASS`; `CU3`–`CU10` **no autorizados** |
| Mandato editorial | E1, aprobado por el autor el 2026-10-20 como obligación de cierre del track (§2.3 y §8): diseño detallado de los apéndices pendiente de preparación y revisión; redacción e incorporación a `geocedg_book` no autorizadas |
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
- El libro `geocedg_book` permanece en pausa durante el track (§8); el mandato editorial E1
  (§2.3) no lo modifica.

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

## 2.1 Decisiones adoptadas por la instrucción de 2026-10-20

Fuente: [registro de cierre de `CU0`](../validation/pre_g9b_cu0_closeout_record.md), que las
transcribe y es su autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| C1-0 | `CU0` `PASS — AUTHOR APPROVED`; la preparación de `CU1` parte de `d684e740` o de su descendiente de cierre verificado | `CU0`; `CU1` |
| C1-1 | Puertas de rendimiento por contadores deterministas (reconstrucciones de índices, construcción de cortes, revisiones, evaluaciones anidadas); los tiempos de arrastre y carga son indicadores complementarios | `CU1` |
| C1-2 | `CU1` incluye B10.1, H03, H04, H10, H11 y H12; la política métrica A1.2 se revisa antes de implementar | `CU1` |
| C1-3 | `CU1` es `INTEGRATED_PHASE` (`PHASE` propio + `INTEGRATION`) | `CU1` |
| C1-4 | Las referencias `preserve` son regresión obligatoria; las fijaciones `pins` solo se invierten o sustituyen mediante pruebas de aceptación; ninguna referencia numérica cambia en silencio | todos |
| H10 | La profundidad mínima y la concordancia de dos niveles no garantizan la longitud de un evaluador general (aliasing). La política revisada distingue cuatro clases de resultado (estructural o analítico con garantías, numérico con cota justificada, estimación sin garantía suficiente, no establecido), no introduce garantías falsas y declara cuándo un resultado puede alimentar otra construcción del DAG; H11 y H12 se investigan conjuntamente con límites honestos | `CU1` |
| Alcance de `CU1` | Se mantiene; si una corrección exige cambiar un contrato de `CU2` o `CU3`, se detiene y se presenta la dependencia | `CU1` |
| `OBS-CU0-CU3-TANGENT-EXISTENCE` | Un punto crítico aislado certificado con residuo dentro de ε no prueba una raíz; distinguir contacto estructural, tangencia de existencia certificada, aproximación numérica y punto crítico sin intersección; F-2 (`TANGENT_CERTIFIED_ISOLATED`) no aprobada tal como está formulada | `CU3` |
| `OBS-CU0-CU6-M07-SELECTION-IDENTITY` | La etiqueta puede introducir una referencia textual, pero no prueba la identidad de un objeto seleccionado antes; comprobar la vigencia de la selección tras Deshacer, Rehacer o reconstrucción y pedir una selección nueva si no se puede establecer; nunca reasociar en silencio | `CU6` |

## 2.2 Decisiones adoptadas por la instrucción de cierre de `CU1` (2026-10-20)

Fuente: [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md), que las
transcribe y es su autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| D-CU1-R1 | `CU1` `PASS — AUTHOR APPROVED` sobre el candidato técnico exacto `143f5d37`, con smoke autoral `PASS` | `CU1` |
| D-CU1-R2 | §25.12 de `locus-v2-metrics.md` v1.5 forma parte del contrato de `CU1`; se conserva la distinción entre longitud estructural, envolvente certificada, estimación numérica y resultado no establecido; las hipótesis de un estimador no son por sí mismas una certificación | `CU1`; `CU2` |
| D-CU1-R3 | `L-CU1-TRANSLATION-RESOLUTION` aceptada provisionalmente como limitación numérica de binary64, no geométrica; se mantiene el fallo explícito; deuda registrada sin tolerancias arbitrarias ni dependientes de la posición absoluta | investigación métrica por planificar; disposición a más tardar en `CU10` |
| D-CU1-R4 | Presupuesto versionado de 16 384 cajas de la envolvente certificada: límite computacional, no garantía de certificación | `CU1` |
| D-CU1-R5 | Rama de revisión publicada; no autoriza la promoción a `main` | `CU10` |
| D-CU1-R6 | Corrección de la prueba ISO A y nueva verificación completa aceptadas, sin cambios de producto | `CU1` |
| Limitaciones | `L-CU1-CLASS2-REACH` y `L-CU1-AFFINE-FORMS` se evalúan en `CU2` solo si su alcance las necesita; `L-CU1-E2-LENGTH` es de `CU2`; `L-CU1-REVERSAL-DENSITY` y `L-CU1-IMPROPER-EVALUATOR` quedan para paquetes posteriores; `OBS-CU1-UNDO-ASYNC-STORE-WINDOW` exigiría una propuesta correctiva separada sobre código upstream; `OBS-CU1-UNDO-TEMP-FILES` es deuda de higiene de pruebas con disposición a más tardar en `CU10` | `CU2`; posteriores; `CU10` |
| Preparación de `CU2` | Autorizada la preparación arquitectónica, normativa y técnica, sin código productivo, desde el cierre exacto de `CU1`: certificación híbrida DA-1 con separación entre evaluación puntual, evidencia exploratoria, componentes localmente certificadas y completitud global (ADR 0027); certificación constructiva del modelo del autor (`o`, `v`, `w`, `a_1`, `o_1`, `e_1`, `e_2`); una longitud `ESTIMATED` de `CU1` nunca es por sí misma evidencia rigurosa de una componente; evento estructural de la longitud orientada de `e_2`; reglas de consumo; representación y teselación; sin adelantar `CU3` | `CU2` |

## 2.3 Mandato editorial adoptado por la instrucción de cierre de coherencia de `CU2` (2026-10-20)

Fuente: instrucción autoral «GeoCeDG — PRE-G9B-CU2. Cierre de coherencia normativa y registro
del programa de apéndices científicos» de 2026-10-20.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| E1 | Mandato editorial del autor: los métodos científicos que GeoCeDG implementa, diseña o estudia se transferirán al libro como apéndices técnicos, con rigor, generalidad declarada y evidencia. Es una obligación de cierre del track, no una decisión de implementación de `CU2` | registro vivo en el [plan de transferencia científica](pre_g9b_cu_scientific_methods_editorial_handoff_plan.md); expediente `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` en `CU10`; §6 y §8 |

Estados: **mandato editorial del autor: aprobado ahora. Diseño detallado de los apéndices:
pendiente de preparación y revisión. Redacción e incorporación a `geocedg_book`: no autorizadas
todavía.** Cada paquete actualiza en el registro el estado y la evidencia de los métodos que
implementa o diseña, sin texto editorial.

## 2.4 Decisiones adoptadas por la instrucción de aprobación de la arquitectura de `CU2` (2026-10-21)

Fuente: [registro de aprobación de la arquitectura de `CU2`](../validation/pre_g9b_cu2_author_architecture_approval_record.md),
que las transcribe y es su autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Arquitectura | Enmiendas A2.1-R a A2.6 aprobadas e incorporadas en T0 a sus especificaciones; ADR 0038 `ACCEPTED — AUTHOR APPROVED` como decisión arquitectónica, sin que ello signifique que `CU2` esté implementado | `CU2` |
| D-CU2-1 a D-CU2-11 | 1, 2 y 4 aprobadas; 3, 6, 7, 8, 9 y 11, opción A; 5 y 10, rangos aprobados, con constantes fijadas por bancos reproducibles en el candidato técnico | `CU2` |
| D-CU2-12 a D-CU2-15 | `INTEGRATED_PHASE` (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU2`, `CU1` y `CU0`, `INTEGRATION`); base exacta `070af6f9`; inversión de B07, H01, B06 y B10.2 con re-baseline trazable; corrección del manifiesto del cilindro en T12 sin alterar el modelo ni sus hashes | `CU2` |
| D-CU2-16 a D-CU2-19 y E-1 a E-3 | opción A en las cuatro decisiones (certificado algebraico exacto, accesores de solo lectura de la selección, tratamiento conservador de salidas coincidentes, componentes puntuales declaradas y no dibujadas); E-1 a E-3 aprobadas | `CU2` |
| D-CU2-20-R | Huella de revisión exacta y versionada y fidelidad de reconstrucción del corte, como conformidad con el contrato de `CU1` y puerta temprana y bloqueante: caracterización en T1 y corrección en T3, antes de que un certificado de T4 sea válido | `CU2`; contrato de `CU1` sin reabrir su cierre |
| Implementación | Autorizada, condicionada, para B07, B10.2, H01 y B06 y las capacidades normativas y de núcleo que exigen, en el orden T0–T13 y con las condiciones de parada de la estrategia y del registro (§9). El candidato queda en la rama local; su `PASS` exige una revisión autoral nueva con su verificación y su smoke | `CU2`; `CU3`–`CU10` sin autorizar |

## 3. Incidencias y paquetes

Prioridad tras `CU0`. Las incidencias H10–H12 son hallazgos nuevos de `CU0`. Con el cierre de
`CU1`, B10.1, H03, H04, H10, H11 y H12 están corregidas dentro de su contrato.

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

### CU0 — Línea base reproducible, caracterización y decisiones (`PASS — AUTHOR APPROVED`)

Aprobado por el autor el 2026-10-20 sobre el candidato técnico `636a0070` y su
descendiente documental `d684e740`
([registro de cierre](../validation/pre_g9b_cu0_closeout_record.md)). Entregado en el
candidato `CU0` (ver el [informe](../validation/pre_g9b_cu0_baseline_candidate_report.md)):
corpus con manifiesto; pruebas de caracterización versionadas y selección
`PRE-G9B-CU0`; banco reproducible y escenarios ventaneados en `tools/benchmark/pre-g9b-cu/`;
línea base geométrica; reproducción ventaneada de B02/B03 con el `main` del producto y
el JDK 25 del producto; matriz de cobertura de la guía; análisis D; propuestas E, F, G y H;
enmiendas normativas candidatas; hoja de decisiones; conciliación del estado vivo.

### CU1 — Conformidad de evaluación, revisión, composición anidada y métrica (`PASS — AUTHOR APPROVED`)

- **Estado:** aprobado por el autor el 2026-10-20 con smoke autoral `PASS` sobre el
  candidato técnico correctivo `143f5d37` y su descendiente documental de verificación
  `2fe8ed19`, publicados para revisión en la rama `phase/pre-g9b-cu1-implementation`
  ([registro de cierre](../validation/pre_g9b_cu1_closeout_record.md)). `STATIC`,
  `INFRA_UNIT`, `PHASE` de `CU1` y de `CU0` e `INTEGRATION` `ACCEPTED / COMPLETE` sobre ese
  candidato; las dos ejecuciones rechazadas del candidato anterior `8ee19667` son evidencia
  histórica. Informes: [implementación](../validation/pre_g9b_cu1_implementation_candidate_report.md),
  [verificación del candidato anterior](../validation/pre_g9b_cu1_verification_record.md),
  [corrección D-CU1-R6](../validation/pre_g9b_cu1_corrective_candidate_report.md) y
  [verificación del candidato correctivo](../validation/pre_g9b_cu1_corrective_verification_record.md).
  Limitaciones y propietarios en el registro de cierre (§6).
- **Antecedente:** preparación normativa y técnica ejecutada el 2026-10-20
  ([informe](../validation/pre_g9b_cu1_preparation_candidate_report.md)).
- **Preparación:** [análisis del aliasing métrico](pre_g9b_cu1_metric_aliasing_analysis.md);
  [propuesta normativa revisada](pre_g9b_cu1_normative_proposal.md) (A1.1-R, A1.2-R,
  ADR 0037 candidato); [estrategia de corrección](pre_g9b_cu1_correction_strategy.md);
  [matriz de validación](../validation/pre_g9b_cu1_validation_matrix.md);
  [prompt canónico](../../.github/prompts/tasks/pre-g9b-cu1-evaluation-composition-and-metric-conformance.prompt.md);
  [hoja de decisiones](../validation/pre_g9b_cu1_author_decision_sheet.md).
- **Objetivo:** que `e_2` y cualquier Locus V2 que dependa de métricas u otros loci se
  evalúe sin bloquear la aplicación y con longitudes correctas, conforme a
  `locus-v2-semantics.md` y `locus-v2-metrics.md`.
- **Alcance:** revisión semántica por contenido (mover solo el conductor no republica);
  corte reconstruido una vez por revisión; copias anidadas estables y reutilización del
  índice métrico; sesión propagada a la evaluación anidada; contadores anidados públicos;
  presupuesto métrico con fallo explícito (H04, H11); política métrica revisada con
  clases de garantía explícitas y límites honestos para el evaluador general (H10, H12),
  según la precisión H10 de §2.1: la profundidad mínima y la concordancia de niveles no
  bastan por sí solas.
- **Fuera de alcance:** existencia y dibujo (`CU2`), materialización (`CU3`).
- **Aceptación:** puertas por contadores (decisión C1-1), referencias `preserve` de la
  línea base reproducidas, fijaciones B10.1, H03, H10 y H11 invertidas por pruebas de
  aceptación propias, banco antes/después.
- **Verificación:** `INTEGRATED_PHASE` (`PHASE` propio + `INTEGRATION`); el inventario
  de selecciones se actualiza con el procedimiento oficial.

### CU2 — Existencia, continuidad, fronteras y representación

- **Estado:** arquitectura `AUTHOR APPROVED` con la precisión D-CU2-20-R e implementación
  autorizada, condicionada, el 2026-10-21 (§2.4;
  [registro de aprobación](../validation/pre_g9b_cu2_author_architecture_approval_record.md)),
  sobre la base exacta `070af6f902cd0be291247e73efb5629ef2a39eba`; en curso en la rama local
  `phase/pre-g9b-cu2-implementation`. `CU2` no está `PASS`: lo decide la revisión autoral del
  candidato técnico congelado. La preparación, su revisión focalizada y su cierre de
  coherencia se publicaron por instrucción autoral en `phase/pre-g9b-cu2-preparation`
  (`60d54644`, `cbb7ea8c` y `070af6f9`, por avance rápido normal)
  ([informe de preparación](../validation/pre_g9b_cu2_preparation_candidate_report.md),
  [propuesta normativa](pre_g9b_cu2_normative_proposal.md),
  [estrategia](pre_g9b_cu2_implementation_strategy.md),
  [matriz](../validation/pre_g9b_cu2_validation_matrix.md),
  [hoja de decisiones](../validation/pre_g9b_cu2_author_decision_sheet.md)).
- **Prerrequisitos:** cumplidos: `CU1`; aprobación de la propuesta normativa, del ADR 0038 y
  de las decisiones D-CU2-1 a D-CU2-20-R y E-1 a E-3 (2026-10-21). E-0 y D-1 de `CU0` quedan
  sustituidas por D-CU2-1 a D-CU2-3.
- **Condiciones de la autorización de 2026-10-21:** contratos antes del código (T0); la huella
  exacta y la fidelidad de reconstrucción (D-CU2-20-R) se validan antes de depender de un
  certificado nuevo; una raíz múltiple no implica por sí sola inexistencia ni discontinuidad, y
  un resultado numérico estimado no se convierte en evidencia rigurosa de existencia; la
  conmutación (T11) no hace desaparecer `o_1 → LocusLength → d_2 → e_2`, y un locus no
  certificable queda `NOT_ESTABLISHED`; las únicas modificaciones upstream son los accesores
  de solo lectura de D-CU2-17; las doce garantías científicas y las doce condiciones de parada
  adicionales del registro (§8 y §9).
- **Condiciones de la autorización de 2026-10-20:** la certificación no se deriva solo del
  muestreo y la evidencia exploratoria no adquiere autoridad semántica; ADR 0027 y la
  independencia entre admisibilidad local y completitud global se preservan; una longitud
  `ESTIMATED` de `CU1` no certifica por sí misma una componente; `LocusLength` no cambia
  en silencio y `e_2` no se cierra por tener un conductor periódico; el kernel compartido
  conserva la autoridad geométrica frente a la presentación y la exportación; no se
  adelanta `CU3`. Cuestiones de `CU1` que `CU2` evalúa: `L-CU1-E2-LENGTH` y, solo si su
  alcance lo necesita, `L-CU1-CLASS2-REACH` y `L-CU1-AFFINE-FORMS`.
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
- **Precisión autoral de 2026-10-20** (`OBS-CU0-CU3-TANGENT-EXISTENCE`): un punto crítico
  aislado certificado con residuo dentro de ε no prueba la existencia de una raíz. El
  diseño distingue contacto estructural, tangencia de existencia certificada,
  aproximación numérica y punto crítico sin intersección, y no convierte aproximaciones
  toleradas en incidencias exactas. La materialización autoritativa de
  `TANGENT_CERTIFIED_ISOLATED` tal como la formula F-2 no está aprobada.

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
prioridad y de B04, M02, M03, M06 y M10. Precisión autoral de 2026-10-20
(`OBS-CU0-CU6-M07-SELECTION-IDENTITY`): la etiqueta puede introducir una referencia
textual en el comando, pero no prueba la identidad de un objeto seleccionado antes; el
diseño comprueba la vigencia de la selección tras Deshacer, Rehacer o reconstrucción,
pide una selección nueva cuando no puede establecerla y nunca reasocia en silencio un
objeto distinto que haya adquirido la misma etiqueta.

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

Obligación de cierre E1 (§2.3): el expediente `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF`, preparado
a partir del [plan de transferencia científica](pre_g9b_cu_scientific_methods_editorial_handoff_plan.md)
§7, con el inventario consolidado de métodos, su alcance y generalidad, los enlaces a
especificaciones, ADR, código, pruebas y modelos, la separación entre resultados rigurosos y
estimaciones, las degeneraciones y limitaciones, los ejemplos reproducibles, la propuesta de
apéndices, la bibliografía y las lagunas. El expediente no inicia ninguna fase editorial.

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
| E1: expediente `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` en `CU10` (§2.3) | — |

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

**Mandato editorial E1 (§2.3).** El autor aprobó el 2026-10-20 que los métodos científicos de
GeoCeDG se transfieran al libro como apéndices técnicos. Durante el track solo se mantiene el
[plan de transferencia científica](pre_g9b_cu_scientific_methods_editorial_handoff_plan.md),
registro vivo de este repositorio; `CU10` entrega su expediente. Después de `CU10` no hay fase
editorial automática: la actualización de la base técnica del libro, el diseño detallado y la
redacción de los apéndices y cualquier cambio de capítulos necesitan autorizaciones autorales
propias.

## 9. Decisiones pendientes

Las decisiones C1-0 a C1-4 quedaron resueltas el 2026-10-20 (§2.1), y las de `CU1`
(D-CU1-1 a D-CU1-12 y D-CU1-R1 a D-CU1-R6) con su cierre (§2.2). Las de la preparación de
`CU2`, con E-1 a E-3 de `CU0`, quedaron resueltas el 2026-10-21 (§2.4); su
[hoja de decisiones](../validation/pre_g9b_cu2_author_decision_sheet.md) conserva las preguntas
tal como se presentaron. La siguiente decisión autoral de `CU2` es la revisión de su candidato
técnico. Las de `CU0` que afectan a `CU3` siguen en la
[hoja de decisiones de `CU0`](../validation/pre_g9b_cu0_author_decision_sheet.md), con la
precisión de `OBS-CU0-CU3-TANGENT-EXISTENCE` sobre F-2; el texto de las enmiendas
candidatas de `CU0` está en las
[enmiendas normativas candidatas](pre_g9b_cu0_candidate_normative_amendments.md).
