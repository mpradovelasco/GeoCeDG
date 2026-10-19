# PRE-G9B-CU1 — Informe del candidato de preparación normativa y técnica

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato documental de preparación de `CU1`. Evidencia, no autoridad normativa |
| Estado | `CU1 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-CU0. Aprobación autoral, cierre y autorización de preparación de CU1» (2026-10-20), transcrita en el [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md) |
| Base | cierre documental de `CU0` `df675e9ee42e1a4caac0e3bf2d74ab2d30450581`, árbol `76f2ddbe155b4a1b068ef475630e536ad5fc3d87`, hijo directo de `d684e74050988a3051a1692bfcf0651440b1567a` |
| Rama | `phase/pre-g9b-cu1-preparation` (local, sin publicar). La rama publicada `phase/pre-g9b-cu0-candidate` sigue en `d684e740` |
| Clase | candidato documental: solo validación estática (`STATIC`), autorizada por la instrucción |

## 1. Cierre de `CU0`

| Elemento | Valor |
|---|---|
| Commit de cierre | `df675e9ee42e1a4caac0e3bf2d74ab2d30450581`, árbol `76f2ddbe…` |
| Contenido | registro de cierre y su espejo `geocedg/validation/pre-g9b-cu/pre-g9b-cu0-closeout.json`; estado del roadmap (5.60) y del plan consolidado |
| Autenticación previa | `STATIC`, `INFRA_UNIT`, `PHASE PRE-G9B-CU0` e `INTEGRATION` sobre `636a0070` y `STATIC` sobre `d684e740`: cada `result_hash` recalculado coincide con el registrado, el estado de ejecución y el diario son `COMPLETED`, y el commit y el árbol coinciden. La clase `INTEGRATED_PHASE` no exige `FINAL` ni recibo, y no falta evidencia |
| `STATIC` del cierre | `verification-9c0f576c7e31411fb50f548a165cb595`, resultado `1caf3911e03b3f9287e82cb6d21c55600399647d40057125a67b673e2ae28397`, `ACCEPTED / COMPLETE` 3/3, autenticado; solo diagnósticos permanentes |
| Registro en `artifacts/agent/` | `pre-g9b-cu0-closeout-static-df675e9e/` |

## 2. Entregables de la preparación

| Entregable pedido (§7 del cierre) | Documento |
|---|---|
| Registro de cierre de `CU0` | [registro de cierre](pre_g9b_cu0_closeout_record.md) y su espejo |
| Análisis del aliasing métrico | [análisis](../architecture/pre_g9b_cu1_metric_aliasing_analysis.md) |
| Propuesta normativa revisada (A1.1-R, A1.2-R) | [propuesta](../architecture/pre_g9b_cu1_normative_proposal.md) y [ADR 0037 (PROPOSED)](../adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md) |
| Estrategia de corrección B10.1/H03/H04 y H10/H11/H12 | [estrategia](../architecture/pre_g9b_cu1_correction_strategy.md) |
| Matriz de validación y criterios de aceptación | [matriz](pre_g9b_cu1_validation_matrix.md) |
| Prompt canónico de implementación | [`pre-g9b-cu1-evaluation-composition-and-metric-conformance.prompt.md`](../../.github/prompts/tasks/pre-g9b-cu1-evaluation-composition-and-metric-conformance.prompt.md) |
| Decisiones pendientes | [hoja de decisiones](pre_g9b_cu1_author_decision_sheet.md) |

**Inspección realizada.** Tres estudios de solo lectura del código y de los contratos, todos
con referencias `archivo:línea` comprobadas contra el código:

- el subsistema métrico;
- la evaluación, las revisiones y la composición anidada;
- el modelo de intervalos y los proveedores analíticos.

Sus conclusiones esenciales están integradas en el análisis y en la estrategia.

## 3. Conclusiones principales

1. **Garantías.** Los tipos de garantía ya existen en el código, pero ninguna ruta de
   producción certifica una longitud. Todo `LocusV2(Q, P)` usa la métrica de solo
   evaluador.
2. **Imposibilidad.** Ningún algoritmo de muestreo finito certifica una longitud
   (análisis §3). La profundidad mínima y la concordancia de niveles solo mejoran una
   estimación.
3. **Capacidades exactas sin usar.** GeoCeDG tiene capacidades exactas que la métrica no
   usa: coeficientes afines certificados, escalas exactas de semejanza y envolventes con
   redondeo exterior del valor y de la primera derivada (SplineV2 y programas v1/v2). Con
   la cota del cono, esas envolventes dan una envolvente certificada de la longitud con
   convergencia de segundo orden. Su coste crece con el giro total y con `1/√τ`.
4. **Modelo del autor.** `o_1` y `e_2` quedan en clase 3 durante `CU1`, porque sus
   programas usan rayos e intersecciones recta–circunferencia, ajenos a la clase v1/v2.
   La cadena del autor sigue definida bajo el contrato de estimación, y su
   certificación depende de `CU2` (DEP-1).
5. **H03.** El token `dependency-update` es hoy el único mecanismo de invalidación
   cuando cambia un valor no conductor. Retirarlo exige antes una huella exacta de las
   entradas del corte; si no, habría geometría obsoleta.
6. **Prueba en conflicto con la especificación.** Una prueba existente exige lo contrario
   de la especificación vigente (`LocusV2PointDrivenCreationRegressionTest.java:58-66`):
   que la revisión avance al mover solo el conductor.
7. **Correcciones documentales detectadas.** La cabecera de `locus-v2-metrics.md` declara
   aprobada la enmienda v1.2, pero §22 la da por pendiente; además, `public-surface.md`
   cita líneas desfasadas de `LocusMetricResult2D`.
8. **Instrumentación.** Tres contadores de `LocusInstrumentation2D` y cinco de la
   instrumentación métrica nunca se registran.

Ninguna corrección de `CU1` exige cambiar un contrato de `CU2` o `CU3`. Las dependencias
DEP-1 a DEP-4 quedan registradas en la propuesta (§5).

## 4. Qué no se ha hecho

- No se ha modificado código productivo, pruebas, registro, inventario, especificaciones
  vigentes ni ADR aceptados. El ADR 0037 es un candidato (**PROPOSED**).
- No se han ejecutado cambios funcionales en las métricas.
- No se ha iniciado la implementación de `CU1` ni `CU2`–`CU10`.
- No se ha publicado, etiquetado ni cambiado `main`.
- No se ha tocado el libro.

## 5. Comprobaciones previas al commit

| Comprobación | Resultado |
|---|---|
| Contrato del prompt (`Test-PromptContractDocument`, perfil `task`) | `execution_safe = true`; los siete campos presentes y en orden; ninguno ausente, duplicado ni desconocido |
| Enlaces relativos de los documentos nuevos y modificados | todos resuelven |
| Nombres retirados de cierre | ninguno |
| `git diff --check` | limpio |

La verificación de este commit (`STATIC`) se comunica con la entrega. Un commit no puede
citar su propia identidad.

## 6. Limitaciones

1. Las cifras de coste de la envolvente (análisis §4.2) son estimaciones analíticas para
   curvas de curvatura regular. Las medirá el banco durante la implementación.
2. Las constantes del estimador v2 son candidatas. Su calibración y las puertas G4 y G6
   requieren la implementación.
3. El inventario de pruebas existentes afectadas por el nuevo estimador está empezado
   (estrategia §4) y se completará en la implementación.
4. La envolvente certificada se calcula sobre la curva semántica exacta, no sobre la
   evaluación en coma flotante. Así lo exige la definición de la longitud
   (`locus-v2-metrics.md` §2), pero comparar el valor estimado con la envolvente admite
   una holgura de redondeo, que la implementación debe acotar.

## 7. Estado de autorización

`CU0` está `PASS — AUTHOR APPROVED` según su registro de cierre. La preparación de `CU1`
está ejecutada como candidato documental pendiente de revisión autoral.

La siguiente decisión autoral es aprobar la política métrica revisada (D-CU1-1 a
D-CU1-7) y autorizar expresamente la implementación de `CU1` sobre un commit base exacto
(D-CU1-8 a D-CU1-12).

`CU1` (implementación) y `CU2`–`CU10` no están autorizados.
