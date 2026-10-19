# PRE-G9B-CU1 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Decisiones necesarias para aprobar la política métrica revisada y autorizar la implementación de `CU1`. No registra ninguna decisión tomada por el agente |
| Estado | `PENDING AUTHOR DECISIONS`; `selfApproved=false` |
| Contexto | [informe del candidato de preparación](pre_g9b_cu1_preparation_candidate_report.md); [propuesta normativa revisada](../architecture/pre_g9b_cu1_normative_proposal.md) |

## 1. Ya decidido (no requiere nueva decisión)

Decidido en el [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md):

- C1-0 a C1-4;
- la precisión sobre H10;
- el alcance de `CU1` y su regla de parada;
- la clase `INTEGRATED_PHASE`;
- las observaciones registradas para `CU3` y `CU6`.

## 2. Para aprobar la política métrica revisada

| Id | Decisión | Opciones | Recomendación |
|---|---|---|---|
| D-CU1-1 | Política métrica v2 (A1.2-R): cuatro clases de garantía, estimador v2 y coherencia entre error y tolerancia | aprobar / aprobar con cambios / rechazar | aprobar. Corrige H10–H12 sin garantías falsas y aprovecha capacidades exactas que hoy no se usan |
| D-CU1-2 | Uso de las estimaciones (clase 3) como valores escalares y entradas del DAG (25.4–25.5) | (a) contrato de estimación: admisibles si su error estimado no supera `τ`, propagadas como estimación y nunca usadas como entrada rigurosa de un certificado; (b) solo resultados certificados | (a). Con (b), `o_1`, `d_2` y `e_2` quedarían indefinidos hasta la clase v3 de `CU2` |
| D-CU1-3 | Indicación visible de que un valor es estimado | (a) en `CU1`, la clase y el error en el resultado rico y en el inspector (que ya muestra la garantía), sin cambio de interfaz, y documentación en `CU9`; (b) además, una marca de clase en el texto del resultado rico de `LocusLength`, lo que cambia textos visibles y quizá huellas de pruebas; (c) además, una marca en el escalar `Length` | (a), porque la decisión de 2026-10-20 deja fuera de `CU1` las mejoras de interfaz; revisar (b) en `CU6` |
| D-CU1-4 | Constantes candidatas: `d_min`, fracción de sondeo, factor de seguridad y presupuestos (propuesta §4) | aprobar los intervalos ahora y los valores finales con el candidato de implementación / fijar valores ahora | aprobar los intervalos; los valores los fija el banco y se presentan con el candidato |
| D-CU1-5 | Capacidad estructural (clase 1): imágenes afines, por semejanza y semejanzas del conductor | incluir en `CU1` / diferir | incluir: invierte H11 de forma exacta y sin presupuesto |
| D-CU1-6 | Envolvente certificada (clase 2) con los modelos de intervalos existentes y la cota del cono, mediante una fachada pública de solo lectura | incluir en `CU1` / diferir a `CU2` con la clase v3 | incluir: no cambia ningún contrato de `CU2` y da la primera certificación real de longitudes (SplineV2 y programas v1/v2) |
| D-CU1-7 | Memorizar los fallos deterministas y tipar la dimensión de trabajo (ajuste de §13) | aprobar / mantener §13 | aprobar: evita repetir el presupuesto completo en cada recálculo |

## 3. Para autorizar la implementación

| Id | Decisión | Recomendación |
|---|---|---|
| D-CU1-8 | Cambiar la aserción de `LocusV2PointDrivenCreationRegressionTest.java:58-66`, que exige una revisión nueva al mover solo el conductor, como conformidad con `locus-v2-semantics.md:169-172` | aprobar |
| D-CU1-9 | Procedimiento de inversión de fijaciones y nueva línea base de `CU0` ([estrategia](../architecture/pre_g9b_cu1_correction_strategy.md) §4): prueba de aceptación de `CU1`, comprobación de inversión en la prueba de `CU0`, regeneración con el arnés y registro de cada campo cambiado | aprobar |
| D-CU1-10 | ADR 0037 (número provisional) y versiones de destino: `locus-v2-semantics.md` 1.2 y `locus-v2-metrics.md` 1.5, incorporadas como paso S0 de la implementación | aprobar |
| D-CU1-11 | Commit base exacto de la implementación | este candidato de preparación o su descendiente de cierre aprobado, con el commit y el árbol exactos nombrados por el autor |
| D-CU1-12 | Valores objetivo de las puertas G4 (≤ 256 evaluaciones anidadas por consulta puntual de `e_2`) y G6 (≤ 25 % del presupuesto en el índice de `o_1`) | aprobar como objetivos; el candidato de implementación presenta los valores medidos |

## 4. Fuera de decisión en este momento

- publicación, etiqueta y cambios en `main`;
- el libro;
- `CU2`–`CU10`;
- la ampliación del modelo certificado a la clase v3 y a expresiones generales (DEP-1),
  que pertenece a `CU2` o a un paquete futuro.
