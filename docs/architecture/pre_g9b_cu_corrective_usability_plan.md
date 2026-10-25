# PRE-G9B-CU — Plan consolidado del track de corrección y usabilidad

| Campo | Valor |
|---|---|
| Carácter | Plan vivo del track `PRE-G9B-CU`. No es especificación normativa: las decisiones arquitectónicas se incorporan a sus especificaciones mediante el procedimiento de revisión y aprobación |
| Estado del marco | `PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS` (instrucción autoral «PRE-G9B-CU — Author Plan Approval, Architectural Refinements and CU0 Authorization», 2026-10-19) |
| Estado de esta consolidación | aceptada con `CU0`, que está `PASS — AUTHOR APPROVED` (decisión autoral de 2026-10-20; [registro de cierre](../validation/pre_g9b_cu0_closeout_record.md), única autoridad de ese estado); las propuestas D–H y las enmiendas candidatas siguen siendo candidatas, no normativas; `selfApproved=false` |
| Paquetes | `CU0` y `CU1` `PASS — AUTHOR APPROVED` (2026-10-20; [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md), única autoridad de ese estado); `CU2` `PASS — AUTHOR APPROVED` (2026-10-24, con la comprobación manual del autor; [registro de cierre de `CU2`](../validation/pre_g9b_cu2_closeout_record.md), única autoridad de ese estado) sobre el candidato `6ff1ddf7` y su descendiente documental `098ec04f`, integrado en `main` por avance rápido con su cierre (§2.6). Historia de `CU2`: arquitectura `AUTHOR APPROVED` con la precisión D-CU2-20-R e implementación autorizada, condicionada, sobre `070af6f9` (2026-10-21; [registro de aprobación](../validation/pre_g9b_cu2_author_architecture_approval_record.md), única autoridad de ese estado), implementada como candidato técnico local (`CU2 IMPLEMENTATION CANDIDATE`) en la rama `phase/pre-g9b-cu2-implementation` ([informe del candidato](../validation/pre_g9b_cu2_implementation_candidate_report.md), [registro de verificación](../validation/pre_g9b_cu2_verification_record.md)) y corregida por su descendiente correctivo en la rama local `phase/pre-g9b-cu2-corrective` (decisiones D-CU2-T13-R1, D-CU2-T13-R2, D-CU2-T13-R4, D-CU2-T13-R6 y D-CU2-T13-R7 y mandato autoral de autonomía correctiva controlada de 2026-10-23), con el candidato correctivo `2856f246` verificado ([registro de la corrección autónoma](../validation/pre_g9b_cu2_corrective_autonomy_record.md)); la resolución autoral consolidada de 2026-10-23 aprobó sus doce decisiones de nivel C (§2.5), y el candidato final `6ff1ddf7` ([paquete de revisión final](../validation/pre_g9b_cu2_final_review_package.md)) se aprobó con el cierre de 2026-10-24. `CU3`: preparación autorizada el 2026-10-24 sobre `main` = `e6c908f2` (§2.7) y publicada en su rama de revisión en `80d72a50` ([expediente](../validation/pre_g9b_cu3_preparation_candidate_report.md)); diseño `AUTHOR APPROVED` e implementación **autorizada** en dos incrementos con puertas bloqueantes sobre `80d72a50` (2026-10-24, §2.8; [registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md), única autoridad de ese estado), detenida en el paso T2 porque la puerta G-M3 no se supera en la autoridad del objetivo ([informe de la puerta](../validation/pre_g9b_cu3_m3_gate_report.md); rama publicada en `1a0903aa` para su auditoría); bloqueo confirmado y estudio de la autoridad geométrica constructiva autorizado (§2.9; [estudio](pre_g9b_cu3_constructive_authority_study.md)), publicado para su auditoría en `da7ca96a`; dirección A2, B2, C y D adoptada para desarrollo normativo y consolidación focal entregada como propuesta normativa candidata (§2.10; [propuesta](pre_g9b_cu3_constructive_authority_normative_proposal.md)), publicada para su auditoría en `18740956`; dirección A2+B2+C+D aceptada como referencia arquitectónica y conciliación final de la propuesta publicada en `3cd0f710` (§2.11); arquitectura de la autoridad constructiva y su normativa **aprobadas** por el autor el 2026-10-25 e implementación reanudada desde T2 sobre `3cd0f710` en los incrementos E0 a E4 con puertas bloqueantes (§2.12; [registro de aprobación](../validation/pre_g9b_cu3_constructive_authority_approval_record.md), única autoridad de ese estado); `CU3` no aprobado. `CU4`–`CU10` **no autorizados** |
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

## 2.5 Decisiones adoptadas por el mandato correctivo y la resolución consolidada de `CU2` (2026-10-23)

Fuentes: el [registro de la corrección autónoma](../validation/pre_g9b_cu2_corrective_autonomy_record.md)
y el [paquete de revisión final](../validation/pre_g9b_cu2_final_review_package.md), que las
aplican; las instrucciones autorales son su autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Mandato correctivo | «PRE-G9B-CU2 — Mandato autoral de autonomía correctiva controlada»: nivel A sin parada (pruebas, trazabilidad, documentación); nivel B con condiciones (defectos de producto bajo contratos aprobados y violados, con pruebas adversarias); nivel C reservado al autor; trata las 34 entradas de R5 | `CU2`; candidato correctivo `2856f246` |
| C-1 a C-12 | «PRE-G9B-CU2 — Resolución autoral consolidada y preparación del cierre»: alternativas recomendadas para C-1 a C-8, C-11 y C-12; C-8, solo precisión editorial de §10.3; C-9 (a), prueba del fallo forzado de la forma límite, con inyección en el arnés o interna, sin cambiar API pública, persistencia, serialización, identidad ni comportamiento de producción; C-10 (a), limitación computacional tipada, sin cambiar el contrato métrico de `CU1` ni introducir un estimador, y la estimación con cambio de variable en el backlog futuro, sin asignación a `CU3`; C-11 y C-12, comportamiento vigente con sus limitaciones documentadas, sin correspondencias por tolerancia o proximidad ni cambios de direcciones persistidas | `CU2`; candidato final `6ff1ddf7` |
| Cierre | `CU2` no se declara todavía `PASS — AUTHOR APPROVED`; revisión científica focal, verificación `INTEGRATED_PHASE` y paquete de revisión final antes de la decisión; `FINAL` solo si el contrato de cierre o la autoridad ejecutable exigen un recibo; la comprobación manual del autor sigue pendiente y no se simula | `CU2`; `CU3`–`CU10` sin autorizar |

## 2.6 Decisiones adoptadas por la instrucción de cierre de `CU2` (2026-10-24)

Fuente: [registro de cierre de `CU2`](../validation/pre_g9b_cu2_closeout_record.md), que las
transcribe y es su autoridad. Antes, el autor autorizó publicar la rama correctiva solo para una
auditoría independiente (2026-10-24; publicada en `098ec04f`, sin etiquetas).

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Cierre | `CU2` `PASS — AUTHOR APPROVED` sobre el candidato `6ff1ddf7` y su descendiente documental `098ec04f`, con la comprobación manual del autor confirmada expresamente | `CU2` cerrado |
| Limitaciones | aceptadas las de §8 del paquete de revisión final, `L-CU2-INDEPENDENT-EXPRESSION-STATE` solo fuera de la clase v3; las mejoras propuestas quedan como trabajo futuro sin autorizar y no son tareas de `CU3`; las cotas demostradas se distinguen de las pérdidas de cobertura observadas (R7-E) | `CU10` (informe de limitaciones) |
| Integración | avance rápido de `main` hasta el commit de cierre, sin reescribir historia ni modificar el producto verificado; incorpora también los cierres aprobados de `CU0` y `CU1`; sin releases ni paquetes de redistribución | publicación del producto por fases, en lugar de reservarla entera a `CU10` |
| Transferencia científica | registro E1, versión 5: principio transversal de integridad geométrica frente a precisión numérica, relacionado con SM-A-5, SM-E-6 y V-POINT-4; el expediente de `CU10` lo recoge; `geocedg_book` no cambia | `CU10`, §8 |
| Frontera de fase | el cierre de `CU2` no autoriza `CU3`; la siguiente fase necesita una decisión autoral nueva | `CU3`–`CU10` sin autorizar |

## 2.7 Autorización de la preparación de `CU3` (2026-10-24)

Fuentes: las instrucciones autorales «PRE-G9B-CU3 — Autorización de preparación arquitectónica,
científica y normativa» y «Decisión sobre la planificación de CU3», ambas de 2026-10-24. El
[expediente de la preparación](../validation/pre_g9b_cu3_preparation_candidate_report.md) las
aplica; las instrucciones autorales son su autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Preparación | **preparación autorizada, implementación no autorizada**, sobre la base exacta `main` = `e6c908f2203cd8feb5d4938013e16b212e0fe016`; `CU0`–`CU2` no se reabren | `CU3` (preparación) |
| Principio | la materialización autoritativa de un punto exige evidencia de existencia e identidad; un residuo bajo una tolerancia no demuestra una intersección; se distinguen el contacto estructural, la tangencia de existencia certificada, el contacto aproximado, el punto crítico sin intersección y los casos no establecidos; F-2 (`TANGENT_CERTIFIED_ISOLATED`) no aprobada | `CU3` |
| Correspondencia | distinguir la pertenencia de `K` a sus objetos de origen, la pertenencia al soporte del conductor, la existencia, unicidad y orientación de `K ↦ t(K)`, y la evaluación de `F(t(K))` con la segunda incidencia; no presuponer que `K` actúe como conductor; fallo cerrado sin una correspondencia demostrable | `CU3`, Etapa A |
| No autorizado | código productivo de `CU3`, comandos o herramientas públicas, cambios de persistencia o identidad, modificación de algoritmos heredados, promoción de especificaciones, cambios de contratos de `CU0`–`CU2`, `CU4`, `CU5`, `CU8-D` u otras fases, `geocedg_book`; ninguna publicación ni integración en `main` sin comprobar la protección, y sin el bypass administrativo como procedimiento ordinario | `CU3`–`CU10` |

El expediente presenta las decisiones D-CU3-1 a D-CU3-18 en una
[hoja única](../validation/pre_g9b_cu3_author_decision_sheet.md). Entre ellas está la
reformulación del criterio «la raíz transversal de la costura se conserva» (D-CU3-10), porque la
caracterización prueba que ese punto es un contacto con cero doble en un pliegue. El texto de §4
conserva el criterio tal como se aprobó hasta que el autor decida.

## 2.8 Aprobación del diseño e implementación de `CU3` (2026-10-24)

Fuente: la instrucción autoral «PRE-G9B-CU3 — Aprobación autoral del diseño e implementación por
incrementos» de 2026-10-24, transcrita en el
[registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md), que es su
autoridad.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Diseño | aprobado conforme a las recomendaciones de D-CU3-1 a D-CU3-19, con condiciones expresas; ADR 0039 aceptado y enmiendas A3.0 a A3.5 incorporadas en el paso T0; la aprobación no certifica por anticipado la viabilidad de M3 ni sus presupuestos | `CU3` |
| Implementación | autorizada sobre la base exacta `80d72a50557a82a7a9d501a26252d1758ac6d94d` en dos incrementos: `CU3-A` (I-1 a I-6) y `CU3-B` (I-7), este solo tras la puerta G-A; si G-B falla, `CU3-A` se presenta como candidato parcial verificado | `CU3` |
| G-M3 | obligatoria, temprana y bloqueante, con la comprobación nueva de la autoridad del objetivo geométrico (G-M3-4): cada incidencia se decide contra el objetivo autoritativo del contrato de intersecciones, y su semántica nunca se redefine para que los contactos resulten incidentes; el presupuesto de M3 se presenta con el resultado de la puerta para una única decisión; si falla, se detienen las partes dependientes y se presentan R-1, R-2, R-3 y C3 | `CU3`, paso T2 |
| Regla de conflicto | D-CU3-13 en su versión final: solo certificados rigurosos de conteo, con continuidad, conteo y localización, extremos y costuras, e identidad y configuración coherentes | `CU3-A` |
| Compatibilidad | D-CU3-7 (a): las soluciones contradichas pierden su admisibilidad sin reasociarse; identidades, tokens válidos, DAG, Deshacer, Rehacer, guardar, reabrir y copia preservados | `CU3-A` |
| Autonomía | ejecución autónoma de los pasos previstos; parada solo ante un fallo de G-M3, una incompatibilidad normativa o de identidad, un cambio de contrato aprobado, un presupuesto que requiera aprobación o una garantía imposible | `CU3` |
| Exclusiones | puntos críticos públicos sin incidencia, componentes puntuales (C22, C23 y C40), pares fuera de lo aprobado, C3, tolerancias de presentación, `CU4`–`CU10`, `G9B`, `geocedg_book` y cualquier publicación en `main`, etiquetas, releases o paquetes | `CU3`–`CU10` |

## 2.9 Auditoría de G-M3 y estudio de la autoridad geométrica constructiva (2026-10-24)

Fuentes: las instrucciones autorales «G-M3 detenida — auditoría independiente antes de elegir
alternativa», «PRE-G9B-CU3 — Decisión autoral tras la auditoría independiente de G-M3» e
«Hipótesis arquitectónica adicional para el estudio de autoridad geométrica constructiva», todas de
2026-10-24.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Auditoría | la implementación sigue detenida en T2; R-1 no se adopta; publicación autorizada de la rama `phase/pre-g9b-cu3-implementation` en `1a0903aa53170c16889a2ecd6b373f1f8ec42364`, por push normal, sin etiquetas ni cambios en `main`, para una auditoría independiente del prototipo de M3 | `CU3` |
| G-M3-4 | confirmada como bloqueo de la implementación que depende de M3; R-1, R-2, R-3 y C3 no se adoptan | `CU3` |
| Objetivo | se mantiene: intersecciones dinámicas con sus dos incidencias establecidas, con su grado real de evidencia; no se sustituye por una construcción que solo establezca la pertenencia al locus | `CU3` |
| Estudio | autorizado un estudio arquitectónico focal de la autoridad geométrica constructiva (alternativas A, B y C, y la hipótesis de integridad estricta y relajada), solo con investigación, análisis de código, sondeos de solo lectura y documentación candidata; sin código productivo, sin cambios en especificaciones aprobadas ni en formatos persistidos, sin reanudar T3–T13 y sin promover ninguna alternativa | `CU3`; [estudio](pre_g9b_cu3_constructive_authority_study.md) |

## 2.10 Dirección normativa y consolidación focal tras la auditoría del estudio (2026-10-24)

Fuentes: las instrucciones autorales «PRE-G9B-CU3 — Publicación del estudio de autoridad
geométrica para auditoría independiente» y «PRE-G9B-CU3 — Decisión autoral tras la auditoría
independiente del estudio de autoridad constructiva», ambas de 2026-10-24.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Publicación | publicada la rama `phase/pre-g9b-cu3-authority-study` en `da7ca96ae68732ad61db48923f5d8521c9ba8def`, por push normal, sin etiquetas, sin PR y sin cambios en `main` ni en la rama de implementación detenida, para una auditoría independiente del estudio y su evidencia | `CU3` |
| Dirección | tras revisar favorablemente el estudio, el autor adopta como dirección de desarrollo normativo, no como contrato aprobado, A2 (autoridad geométrica constructiva ideal), B2 (certificación de relaciones estructurales), C (revisión coordinada, versionada y mínima de los contratos de autoridad) y D (estados de integridad estricta y relajada, independientes de las garantías numéricas, con propagación por premisas) | `CU3` |
| Consolidación | autorizada una única consolidación focal: autoridad única y convivencia con el producto existente, transferencia de los certificados de `CU2`, política `RELAXED` positiva y versionada, certificación estructural genérica y plan mínimo de ejecución, entregados como una sola propuesta normativa candidata con sus decisiones autorales y su estrategia de implementación; se conservan la caracterización y las evidencias; el censo 160/160 no se usa como prueba universal; `CU3` sigue detenido en T2, sin código productivo y sin cambios en contratos aprobados, `main` ni `geocedg_book` | `CU3`; [propuesta](pre_g9b_cu3_constructive_authority_normative_proposal.md) |

## 2.11 Conciliación final previa a la decisión normativa (2026-10-25)

Fuentes: tres instrucciones autorales de 2026-10-25. La primera autoriza publicar la rama del
estudio hasta `18740956`. La segunda es «PRE-G9B-CU3 — Conciliación final previa a la decisión
normativa». La tercera, «PRE-G9B-CU3 — Precisión autoral sobre evolución de la autoridad
constructiva y continuidad editorial», no modifica la autorización vigente.

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Publicación | publicada la rama `phase/pre-g9b-cu3-authority-study` en `187409564b683e17ec62848c4b3b1f1d7a04836f`, por push normal, sin forzar, sin etiquetas, sin PR y sin cambios en `main` ni en la rama de implementación detenida | `CU3` |
| Dirección de referencia | tras revisar favorablemente la consolidación publicada, el autor acepta A2+B2+C+D como dirección arquitectónica de referencia, y el certificado de identidad constructiva y sus evidencias como base científica favorable; antes de adoptar formalmente D-AUT-1 a D-AUT-12 pide cuatro precisiones: adopción inicial controlada, política `RELAXED`, decisiones numéricas heredadas, e identidad y huella estructural | `CU3`; [propuesta](pre_g9b_cu3_constructive_authority_normative_proposal.md) §14 |
| Conciliación | se actualizan solo la propuesta candidata, la matriz (ampliación candidata V-AUT, §6 de la [matriz](../validation/pre_g9b_cu3_validation_matrix.md)) y las decisiones D-AUT correspondientes, sin repetir sondeos y con los incrementos E0 a E4 y sus puertas; la propuesta da una recomendación consolidada única (§13); `CU3` sigue detenido en T2, sin código productivo y sin cambios en contratos aprobados, `main` ni `geocedg_book` | `CU3` |
| Evolución de la autoridad | planificación futura, no decisión. La adopción explícita de `constructive-authority/v1` es transitoria: `CU3` se implementa y valida bajo adopción explícita; después de `CU3` se verifica la compatibilidad general; luego, una decisión autoral independiente puede promover la autoridad constructiva a predeterminada en los documentos nuevos, y los existentes conservan su política hasta una migración explícita. La autoridad predeterminada no implica integridad `STRICT` universal | `CU3` y posteriores |
| Continuidad editorial | planificación futura: el expediente de `CU10` incluirá una exposición científica amplia y comprensiva de la autoridad constructiva y la integridad de las relaciones, que distinga la aportación CeDG de las capacidades de representación geométrica exacta de CAD ([registro E1](pre_g9b_cu_scientific_methods_editorial_handoff_plan.md), versión 10, §7); no se modifica `geocedg_book` ni se adelanta `CU10` | `CU10`; E1 |

## 2.12 Aprobación de la autoridad constructiva e implementación por incrementos (2026-10-25)

Fuente: la instrucción autoral «PRE-G9B-CU3 — Aprobación autoral de la autoridad constructiva e
implementación por incrementos», de 2026-10-25
([registro de aprobación](../validation/pre_g9b_cu3_constructive_authority_approval_record.md),
única autoridad de ese estado).

| Id | Decisión | Efecto en el plan |
|---|---|---|
| Publicación | publicada la rama `phase/pre-g9b-cu3-authority-study` en `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, por push normal, sin forzar, sin etiquetas, sin PR y sin cambios en `main` ni en la rama de implementación | `CU3` |
| Arquitectura y normativa | aprobadas A2+B2+C+D, D-AUT-1 a D-AUT-12 con la §14 de la propuesta, el ADR 0040, los textos AUT-1 a AUT-7, la especificación nueva [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md), las enmiendas versionadas de la propuesta §7, la ampliación V-AUT de la matriz y los incrementos E0 a E4 con sus puertas; la aprobación no convierte los prototipos en implementación verificada | `CU3` |
| Ejecución | `CU3` se reanuda desde T2 sobre la base exacta `3cd0f710`; la rama de implementación avanza por avance rápido y conserva la evidencia de G-M3; E0 incorpora los contratos, registra la decisión y actualiza el prompt canónico antes de cualquier código productivo; después, ejecución autónoma de E1 a E4 con sus condiciones de parada, entre ellas la aprobación de un presupuesto normativo nuevo | `CU3` |
| Fuera de alcance | la promoción de la autoridad constructiva a predeterminada, la semántica de los comandos heredados, la ampliación general del certificado fuera de su clase, `CU4`–`CU10`, `G9B`, `geocedg_book`, `main`, etiquetas, releases y packaging | `CU3` y posteriores |
| Continuidad editorial | se mantiene en E1 la exposición científica amplia y comprensiva prevista para `CU10`; `CU10` sigue sin iniciar ni autorizar | `CU10`; E1 |

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

### CU2 — Existencia, continuidad, fronteras y representación (`PASS — AUTHOR APPROVED`)

- **Cierre:** `PASS — AUTHOR APPROVED` el 2026-10-24, con la comprobación manual del autor, sobre
  el candidato `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb` y su descendiente documental
  `098ec04f7ac72b691a989796637b3ff681e6e3de`
  ([registro de cierre](../validation/pre_g9b_cu2_closeout_record.md), única autoridad de ese
  estado; §2.6). Las limitaciones aceptadas y el trabajo futuro sin autorizar constan en ese
  registro.
- **Estado anterior al cierre:** arquitectura `AUTHOR APPROVED` con la precisión D-CU2-20-R e implementación
  autorizada, condicionada, el 2026-10-21 (§2.4;
  [registro de aprobación](../validation/pre_g9b_cu2_author_architecture_approval_record.md)),
  sobre la base exacta `070af6f902cd0be291247e73efb5629ef2a39eba`; implementada en la rama local
  `phase/pre-g9b-cu2-implementation` (candidato de T13 `39741a0b`) y corregida en la rama local
  `phase/pre-g9b-cu2-corrective` (candidato correctivo `2856f246`;
  [registro de la corrección autónoma](../validation/pre_g9b_cu2_corrective_autonomy_record.md));
  la resolución autoral consolidada de 2026-10-23 (§2.5) aprobó las decisiones C-1 a C-12, y el
  candidato final congelado `6ff1ddf7`, su verificación y la propuesta de cierre constan en el
  [paquete de revisión final](../validation/pre_g9b_cu2_final_review_package.md).
  Hasta el cierre, `CU2` no estaba `PASS`: lo decidió la decisión autoral de cierre sobre ese
  candidato, con la comprobación manual del autor. La preparación, su revisión focalizada y su cierre de
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

- **Estado:** preparación autorizada el 2026-10-24 (§2.7) y publicada en la rama
  `phase/pre-g9b-cu3-preparation` en `80d72a50`
  ([expediente](../validation/pre_g9b_cu3_preparation_candidate_report.md)); diseño aprobado e
  implementación autorizada en dos incrementos con puertas bloqueantes el mismo día (§2.8;
  [registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md)), en la
  rama `phase/pre-g9b-cu3-implementation`, publicada en `1a0903aa` para su auditoría. La
  implementación está detenida en el paso T2: la puerta G-M3 no se supera en la autoridad del
  objetivo (G-M3-4), porque los cinco contactos obligatorios se refutan exactamente contra ella
  ([informe de la puerta](../validation/pre_g9b_cu3_m3_gate_report.md)). Tras la auditoría, el
  autor confirmó el bloqueo y autorizó un estudio arquitectónico de la autoridad geométrica
  constructiva (§2.9), publicado para su auditoría en la rama `phase/pre-g9b-cu3-authority-study`
  en `da7ca96a` ([estudio](pre_g9b_cu3_constructive_authority_study.md)). Tras revisarlo
  favorablemente, adoptó A2, B2, C y D como dirección de desarrollo normativo y autorizó una
  consolidación focal (§2.10), entregada en esa rama como propuesta normativa candidata única
  ([propuesta](pre_g9b_cu3_constructive_authority_normative_proposal.md)), con un prototipo de
  certificado de identidad constructiva y su evidencia, y publicada para su auditoría en
  `18740956`. Tras revisarla favorablemente, el autor aceptó A2+B2+C+D como dirección
  arquitectónica de referencia y pidió una conciliación final con cuatro precisiones (§2.11),
  entregada en la misma rama y publicada en `3cd0f710`. El 2026-10-25 el autor aprobó la
  autoridad constructiva y su normativa (§2.12) y reanudó la implementación desde T2 sobre esa
  base, en la rama `phase/pre-g9b-cu3-implementation`, en los incrementos E0 a E4 con puertas
  bloqueantes. Documentos del diseño:
  - [análisis](pre_g9b_cu3_initial_state_and_capability_analysis.md);
  - [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md);
  - [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md);
  - [normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md);
  - [propuesta normativa](pre_g9b_cu3_normative_proposal.md);
  - [ADR 0039 (ACCEPTED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md);
  - [estrategia](pre_g9b_cu3_implementation_strategy.md);
  - [matriz](../validation/pre_g9b_cu3_validation_matrix.md);
  - [hoja de decisiones](../validation/pre_g9b_cu3_author_decision_sheet.md);
  - [prompt canónico](../../.github/prompts/tasks/pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md).

  `CU3` no aprobado: su aprobación exige una decisión autoral sobre el candidato técnico congelado.
- **Prerrequisitos:** `CU2`; decisiones F-1, F-2, H-2 y H-3, sustituidas en la preparación por
  D-CU3-1 a D-CU3-19.
- **Alcance:** Etapa A (`Point(L, D)`, `Intersect(L, r, D)` con verificación de la
  segunda incidencia); Etapa B (clase de contacto tangente certificada, germen tangente,
  vigencia, desaparición y desdoblamiento); normalización de residuos (H02); ejes
  (H06); clasificación de contacto por punto crítico.
- **Aceptación:** los tres casos obligatorios materializados con identidad y dinámica;
  el punto de contacto de la costura se conserva y se materializa como contacto estructural, con
  su clase corregida (D-CU3-10, aprobada el 2026-10-24); barrido de escala; casi-tangencias; todo
  ello con la incidencia decidida contra la autoridad del objetivo (G-M3-4, §2.8).
- **Precisión autoral de 2026-10-20** (`OBS-CU0-CU3-TANGENT-EXISTENCE`): un punto crítico
  aislado certificado con residuo dentro de ε no prueba la existencia de una raíz. El
  diseño distingue contacto estructural, tangencia de existencia certificada,
  aproximación numérica y punto crítico sin intersección, y no convierte aproximaciones
  toleradas en incidencias exactas. La materialización autoritativa de
  `TANGENT_CERTIFIED_ISOLATED` tal como la formula F-2 no está aprobada.
- **Evolución prevista de la autoridad** (precisión autoral de 2026-10-25; planificación futura,
  §2.11): `CU3` se implementa y valida bajo adopción explícita de la autoridad constructiva, que
  es transitoria. Después de `CU3` se verifica la compatibilidad general, y una decisión autoral
  independiente puede promover la autoridad constructiva a predeterminada en los documentos
  nuevos. Los documentos existentes conservan su política hasta una migración explícita, y cada
  relación conserva su grado real de evidencia: la autoridad predeterminada no implica integridad
  `STRICT` universal.

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
Desde la precisión autoral de 2026-10-25 (§2.11), incluye también una exposición científica amplia
y comprensiva de la autoridad constructiva y de la integridad de las relaciones, que distingue la
aportación CeDG de las capacidades de representación geométrica exacta de CAD (registro E1, §7,
punto 11).

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
tal como se presentaron. Las decisiones de nivel C del descendiente correctivo (C-1 a C-12)
quedaron resueltas el 2026-10-23 (§2.5), y el cierre de `CU2` se decidió el 2026-10-24 (§2.6).
La preparación de `CU3` se autorizó el 2026-10-24 (§2.7), y el mismo día el autor aprobó su
diseño (D-CU3-1 a D-CU3-19 de la
[hoja de decisiones de `CU3`](../validation/pre_g9b_cu3_author_decision_sheet.md)) y autorizó la
implementación en dos incrementos (§2.8). La puerta G-M3 se ejecutó en el paso T2 y no se supera
en la autoridad del objetivo; tras una auditoría independiente, el autor confirmó el bloqueo y
autorizó el estudio de la autoridad geométrica constructiva (§2.9). Tras revisarlo, adoptó A2, B2,
C y D como dirección de desarrollo normativo y autorizó una consolidación focal (§2.10). Tras
revisar la consolidación publicada, aceptó esa dirección como referencia arquitectónica y pidió
una conciliación final (§2.11). El 2026-10-25 aprobó la
[propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md), con D-AUT-1 a
D-AUT-12, y reanudó la implementación (§2.12). La siguiente intervención autoral es la decisión
sobre los presupuestos versionados que el contrato le reserva, que se presentan tras E0; después
vendrá la revisión del candidato técnico congelado. Las de `CU0` que afectan a `CU3`, sustituidas
por las de esa hoja, siguen en la
[hoja de decisiones de `CU0`](../validation/pre_g9b_cu0_author_decision_sheet.md), con la
precisión de `OBS-CU0-CU3-TANGENT-EXISTENCE` sobre F-2; el texto de las enmiendas
candidatas de `CU0` está en las
[enmiendas normativas candidatas](pre_g9b_cu0_candidate_normative_amendments.md).
