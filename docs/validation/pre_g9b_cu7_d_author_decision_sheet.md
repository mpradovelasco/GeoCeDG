# PRE-G9B-CU7-D — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Estado | **D-CU7-01–11: ARCHITECTURAL DIRECTION AUTHOR APPROVED WITH PRECISIONS** (2026-10-29); D-CU7-12: diseño aprobado sujeto a conciliación; D-CU7-13: CU7-I no autorizada; `selfApproved=false` |
| Diseño | [arquitectura](../architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md), [ADR 0041 propuesto](../adr/0041-locus-v2-point-placement-and-point-redefinition.md) |
| Enmiendas candidatas | [M08 §8](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md), [M09 §10](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md) |
| Autoridad existente | DA-5 ya retira la coincidencia; CU3 aprobado no aprueba estas nuevas decisiones |

La decisión autoral «PRE-G9B-CU7-D — DECISIÓN AUTORAL SOBRE EL CANDIDATO» de 2026-10-29
adopta las recomendaciones 01–11 como dirección con sus cinco precisiones, aprueba el diseño
sujeto a conciliación (12) y mantiene CU7-I sin autorización (13). La tabla conserva las
alternativas presentadas; la disposición siguiente prevalece sobre sus recomendaciones iniciales.

| Decisiones | Disposición autoral conciliada |
|---|---|
| 01–03 | S/D distintas; S normaliza dirección establecida sin dependencia ficticia de Q |
| 04 | D exige prueba propia; puede diferirse si la clase afín no supera puertas |
| 05–06 | actualización compatible y atómica de P, sin transportar raíz ni exigir coincidencia; protección de roles y dependientes |
| 07 | empezar por singleton; I2G independiente; diferirlo no completa M09 |
| 08 | reemplazo explícito; sin remapeo selectivo ni fallback automático |
| 09 | I3 aprobada solo como dirección de diseño; contrato propio y autorización separada; rechazo explícito y tipado mientras la nueva operación no esté implementada bajo autorización |
| 10–11 | autoridad documental, evidencia real y persistencia versionada; ninguna identidad por proximidad/coordenadas ni STRICT por estimación |
| 12 | diseño aprobado con precisiones, conciliadas en el candidato normativo; texto consolidado pendiente de revisión |
| 13 | CU7-I permanece sin autorización; ninguna ejecución productiva |

La [conciliación](pre_g9b_cu7_d_consolidated_candidate.md) y la
[propuesta de incrementos](../architecture/pre_g9b_cu7_i_increment_proposal.md) concretan
esta disposición. No se presume aprobación del nuevo texto ni autorización de implementación.

| Id | Alternativas que corresponden al autor | Recomendación y consecuencia |
|---|---|---|
| D-CU7-01 | dos operaciones explícitas S/D; una con modo obligatorio; solo S | **S y D separadas**. S fija dirección, D conserva dependencia de Q. Evita confundir una instantánea con proyección dinámica |
| D-CU7-02 | aceptar una sola dirección globalmente única; permitir elección de un candidato aislado de resultado múltiple; aceptar candidatos unresolved | **elección explícita de un candidato admisible en resultado múltiple**, sin elevar cobertura; unresolved no crea punto. Precisión candidata de ADR 0019 |
| D-CU7-03 | S como nueva receta persistente; S normalizada a Point existente; solo API sin superficie de autoría | **normalizar S al punto forward existente**. Nombre recomendado PointOnLocusV2 para la operación; gramática/API final en I0. No conservar Q como dependencia ficticia |
| D-CU7-04 | D general por mínimos numéricos; D acotada a mapa afín con prueba; diferir D | **diseñar D afín con puerta propia**. CU7-I no puede prometerla hasta demostrar la capacidad/codec; si se difiere, M08 dinámica queda pendiente expresamente |
| D-CU7-05 | RETAIN por coincidencia; ampliar todas las familias; actualización cerrada del punto compatible | **actualización cerrada y explícita de objeto P**. Mismo contrato neutral/rol/grupo, DAG y resultado admisible; nuevas coordenadas permitidas. No se transporta identidad de raíz |
| D-CU7-06 | convertir roles especializados a VALUE; conservar sus requisitos; migrar roles mediante contrato nuevo | **conservar requisitos**. Conversiones incompatibles ofrecen solo reemplazo independiente si es válido, o rechazo; no rebautizar roles para conseguir RETAIN |
| D-CU7-07 | primer CU7-I singleton con rechazo tipado; exigir adaptador de separación de salida múltiple antes de completar M09 | **dirección singleton-first adoptada**; I2G independiente. Su diferimiento no permite declarar M09 completo |
| D-CU7-08 | M09-C explícito; M09-B con remapeo selectivo; fallback automático | **conservar M09-C explícito e impacto completo**; diferir B. No enmendar ADR 0011 para un remapeo parcial sin necesidad demostrada |
| D-CU7-09 | permitir cambiar solo el soporte visible; operación coordinada de cierre; mantener rechazo con mejor diagnóstico | **operación coordinada gated por I3**, precedida de diagnóstico tipado. N, soporte, fuente, registros y claims se evalúan juntos; no auto-refresh indiscriminado de salidas públicas |
| D-CU7-10 | estimación numérica implica STRICT; preservar política y grados reales; adoptar política constructiva automáticamente | **preservar autoridad documental y grados reales**. S no afirma proyección exacta; D exige evidencia propia; CU3 clases/garantías no cambian |
| D-CU7-11 | reutilizar formatos S/M09 singleton; forzar versión nueva universal; diseñar codec D separado | **reutilizar los formatos existentes donde son suficientes y versionar D/adaptadores nuevos**. Rechazo de lectores desconocidos y matriz real antes de implementarlos |
| D-CU7-12 | aprobar íntegramente; aprobar con cambios/diferimientos explícitos; pedir otra iteración | **revisión del candidato exacto con esta hoja**. La elección no se infiere de STATIC ni de una aprobación de CU3 |
| D-CU7-13 | autorizar CU7-I después con base exacta; mantenerlo sin autorizar | **autorización posterior separada**, fijando incrementos, pruebas, presupuestos y perímetro. Esta entrega se detiene en CU7-D |

## Decisiones ya tomadas que no se vuelven a abrir

- DA-5: no coincidencia geométrica general para redefinir; Undo/Redo exactos.
- ADR 0017/0039: ningún token se reasigna por etiqueta, posición, proximidad o índice de solución.
- ADR 0040: autoridad constructiva solo por evento explícito; su adopción no concede STRICT
  universal. CU7-D no propone hacerla predeterminada.
- CU10: se conserva `CU10-GUIDE-INTEGRATION-CHECK` y su FINAL futuro.
- `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`: posterior a CU10 y al trabajo editorial del libro.
- E1: expediente de transferencia científica en CU10; el libro sigue sin autorización de cambios.

## Disposiciones necesarias para un futuro encargo CU7-I

La instrucción deberá indicar el commit/árbol aprobado, filas adoptadas/enmendadas, si I2G y
D se incluyen o se difieren, el alcance de I3 y el tratamiento explícito de M08/M09 incompletos.
La [matriz y puertas](pre_g9b_cu7_d_validation_and_implementation_gates.md) propone
`INTEGRATED_PHASE` por incremento productivo y reserva FINAL al cierre CU10, salvo que una
clasificación de impacto global obligue a otra decisión conforme al contrato de verificación.
No se autoriza aquí registrar selecciones PHASE nuevas ni modificar el verificador.
