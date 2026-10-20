# PRE-G9B-CU2 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Decisiones necesarias para aprobar la arquitectura de `CU2` y autorizar su implementación. No registra ninguna decisión tomada por el agente |
| Estado | `PENDING AUTHOR DECISIONS`; `selfApproved=false` |
| Contexto | [informe del candidato de preparación](pre_g9b_cu2_preparation_candidate_report.md); [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md); [ADR 0038 (PROPOSED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) |

## 1. Ya decidido (no requiere nueva decisión)

- Dirección híbrida DA-1 (2026-10-19) y ADR 0027, con la admisibilidad local independiente de
  la completitud global.
- El contrato de estimación de `CU1` (D-CU1-2) y la regla de cierre de `CU1`: una longitud
  `ESTIMATED` no se convierte en evidencia rigurosa de una componente de `CU2`
  ([registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) §7).
- El alcance de `CU2`: B07, B10.2, H01 y B06, sin puntos estructurales, materialización de
  tangencias, cambios del motor de intersecciones ni herramientas públicas nuevas (`CU3`).
- La regla de referencias de `CU0` (C1-4) y las puertas por contadores (C1-1).

## 2. Para aprobar la arquitectura y las normas

| Id | Decisión | Opciones | Recomendación |
|---|---|---|---|
| D-CU2-1 | Certificación híbrida (A2.1-R) y ADR 0038: cuatro niveles de evidencia, continuidad de `F` como requisito, arcos, autoridad única de cierre, indefinición computacional, regla de publicación y linaje estructural | aprobar / aprobar con cambios / rechazar | aprobar. Sustituye a E-0 de `CU0` y extiende el ADR 0027 sin cambiarlo |
| D-CU2-2 | Clase v3 del modelo de construcciones certificadas (A2.2-R): solo las capacidades que necesita el modelo del autor, resultado de caja tipado, transiciones certificadas por raíz simple de un predicado y totalidad y periodicidad estructurales con la premisa de función de configuración | aprobar / limitar a una parte | aprobar. Sin cualquiera de esas piezas, al menos un locus del modelo queda `NOT_ESTABLISHED` ([análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §6) |
| D-CU2-3 | Representación y certificación de `e_2` (sustituye a D-1 de `CU0`) | (a) una componente abierta cortada en el origen de la longitud orientada, certificada con el certificado estructural de la función métrica y el evento `ORIGIN_WRAP` (A2.3-R), sin usar el valor de la longitud y sin cambiar `LocusLength`; (b) `e_2` `NOT_ESTABLISHED` hasta que la longitud de `o_1` se certifique en clase 1 o 2 | (a). (b) deja `e_2` sin componentes certificadas ni dibujo hasta un paquete futuro |
| D-CU2-4 | Topología del dibujo y política de teselación v2 (A2.4-R), alineación de los consumidores (A2.5) y topología de las exportaciones (A2.6) | aprobar / aprobar con cambios | aprobar |
| D-CU2-5 | Constantes de certificación: anchura `δ` de los corchetes, presupuesto de cajas por rama y revisión, profundidad mínima de existencia y límite de pasos de la clase v3 ([propuesta](../architecture/pre_g9b_cu2_normative_proposal.md) §8) | aprobar los intervalos ahora y los valores con el candidato de implementación / fijar los valores ahora | aprobar los intervalos; los valores los fija el banco (precedente D-CU1-4) |
| D-CU2-6 | Chorros de segundo orden | (a) en `CU2`, solo para las operaciones de la clase v3 y solo para certificar pliegues; (b) diferirlos a `CU3` | (a). Con (b), los pliegues de `e_1` no se pueden eximir del criterio de giro: se refinan hasta la resolución del parámetro con diagnóstico, y B06 queda sin corregir en `e_1` |
| D-CU2-7 | Alcance de la retirada del certificado de compatibilidad | (a) el productor dependiente pasa al certificador híbrido; los productores estáticos y analíticos (SplineV2, recorridos de segmento y fixtures del laboratorio) pasan a un certificado explícito «probado por el proveedor», con su contrato citado, como prevé el ADR 0027; (b) recertificar también esos productores con cajas | (a). Su contrato ya prueba la validez en todo el dominio; (b) añade coste sin corregir ninguna incidencia |
| D-CU2-8 | Locus con definición definida y recubrimiento certificado vacío | (a) publicado, `VALID`, `COMPLETE`, cero componentes; (b) estado de definición `EMPTY_DOMAIN`; (c) `UNDEFINED` | (a). Conserva la separación entre dominio declarado y cobertura (ADR 0027, decisión 1). (b) mezcla los dos ejes; (c) es el defecto H01 |
| D-CU2-9 | Certificador de 512 celdas de la raíz seleccionada de SplineV2 (`SelectedRootContinuousDomainCertifier2D`), que usa el testigo `m` | (a) conservarlo como caso particular del contrato híbrido, corrigiendo su dependencia de la raíz actual (que contradice A1.1-R) y la última celda del dominio semiabierto; (b) sustituirlo por el certificador de cajas sobre `SplineIntervalModel2D` | (a): cambio menor que conserva el testigo aprobado; (b) en un paquete posterior |
| D-CU2-10 | Constantes de dibujo DC-6 (`d_min`, `θ_max`, presupuesto de vértices) y criterio de giro en las exportaciones | intervalos ahora y valores con el banco; criterio de giro solo en el dibujo / también en las exportaciones | intervalos ahora; giro solo en el dibujo. La exportación conserva su tolerancia en unidades del modelo, ahora con la desviación sin acotar, que ya impide puentear saltos |
| D-CU2-11 | Especificación durable de la exportación LaTeX | (a) crear una especificación en `geocedg/specs/export/` con las reglas de DQ-C5, hoy solo en el [registro de decisiones de `R6-plus-C`](pre_g9b_r6_plus_c_author_decisions_record.md), y la topología de A2.6; (b) mantenerlas en el registro | (a) |

## 3. Para autorizar la implementación

| Id | Decisión | Recomendación |
|---|---|---|
| D-CU2-12 | Clase de verificación: `INTEGRATED_PHASE` (`PHASE` propio, `PHASE` de `CU0` y `CU1`, e `INTEGRATION`) o `GLOBAL_IMPACT` (`FINAL`) | `INTEGRATED_PHASE`. El impacto queda en los consumidores de Locus V2, que cubren `PHASE` e `INTEGRATION`; `FINAL` solo añade hojas de la infraestructura de verificación, que `CU2` no toca |
| D-CU2-13 | Commit base exacto de la implementación | este candidato de preparación o su descendiente de cierre aprobado, con el commit y el árbol exactos nombrados por el autor |
| D-CU2-14 | Procedimiento de inversión de las fijaciones `B07`, `H01`, `B06` y `B10.2` y nueva línea base ([estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) §5) | aprobar |
| D-CU2-15 | Corrección documental del manifiesto del corpus: describe el cilindro como vertical y es oblicuo (hallazgo F-CU2-6) | hacerla en el paso T12 de `CU2`, con el registro de nueva línea base. No cambia ningún hash del modelo ni ninguna referencia, y la prueba del corpus no fija la descripción |

## 4. Decisiones de `CU0` que siguen abiertas

Formuladas en la [hoja de `CU0`](pre_g9b_cu0_author_decision_sheet.md) §3 para decidir antes de
autorizar `CU2`. E-0 y D-1 quedan sustituidas por D-CU2-1 a D-CU2-3.

| Id | Decisión | Recomendación |
|---|---|---|
| E-1 | Evidencia `EXPLORATORY` en el dibujo: no dibujarla, o dibujarla con marca visual distinta | dibujarla con marca visual solo en bandas `UNRESOLVED`, nunca unida a una componente ni exportada. Es el único cambio visible fuera de la geometría |
| E-2 | Puntos y extremos métricos: exigir una componente certificada que contenga la dirección, o aceptar validez puntual | exigir componente certificada, aunque sea local, para rutas y continuidad; validez puntual solo para puntos exactos aislados |
| E-3 | Linaje de componentes por la fuente estructural de sus fronteras y migración de selectores guardados | adoptar el linaje estructural; migración por contención única con reescritura perezosa y fallo cerrado ante ambigüedad |

## 5. Observaciones registradas para otros paquetes

- **`CU3` (F-CU2-5).** La línea base de `CU0` registra la raíz de la costura de
  `Intersect(e_1, c_1)` como `TRANSVERSE_ESTABLISHED` (`pins.B08_B09`). Geométricamente es una
  tangencia: conservarla como transversal conservaría un artefacto. No requiere decisión ahora.
- **`CU3`.** Las fronteras certificadas y los chorros de segundo orden de `CU2` son entradas
  para los contactos tangentes. La precisión `OBS-CU0-CU3-TANGENT-EXISTENCE` no cambia.
- **Limitación de `CU1`.** `L-CU1-TRANSLATION-RESOLUTION` sigue abierta. La matriz admite
  el fallo cerrado de la certificación con traslaciones de `10⁶` (V-INV-2).

## 6. Fuera de decisión en este momento

- publicación, etiqueta y cambios en `main`;
- el libro;
- la implementación de `CU3`–`CU10`;
- la certificación en clase 1 o 2 de la longitud de `o_1` y de `e_2`.
