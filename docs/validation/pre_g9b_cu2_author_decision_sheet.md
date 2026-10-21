# PRE-G9B-CU2 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Decisiones necesarias para aprobar la arquitectura de `CU2` y autorizar su implementación. No registra ninguna decisión tomada por el agente |
| Estado | `AUTHOR DECISIONS RECORDED` el 2026-10-21: todas las decisiones de esta hoja tienen disposición autoral en el [registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md), que es su única autoridad; D-CU2-20 se adoptó ampliada como D-CU2-20-R. Esta hoja conserva las preguntas y recomendaciones tal como se presentaron; `selfApproved=false` |
| Contexto | [informe del candidato de preparación](pre_g9b_cu2_preparation_candidate_report.md); [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md); [ADR 0038 (ACCEPTED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) |
| Revisión | 2026-10-20, revisión focalizada P1–P5: D-CU2-1 a D-CU2-6 y E-3 reformuladas; D-CU2-16 y D-CU2-17 nuevas. Cierre de coherencia C1–C3 (2026-10-20): D-CU2-18 a D-CU2-20 nuevas, D-CU2-16 precisada (C3), mandato editorial E1 registrado como obligación del track (§1) |

## 1. Ya decidido (no requiere nueva decisión)

- Dirección híbrida DA-1 (2026-10-19) y ADR 0027, con la admisibilidad local independiente de
  la completitud global.
- El contrato de estimación de `CU1` (D-CU1-2) y la regla de cierre de `CU1`: una longitud
  `ESTIMATED` no se convierte en evidencia rigurosa de una componente de `CU2`
  ([registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) §7).
- El alcance de `CU2`: B07, B10.2, H01 y B06, sin puntos estructurales, materialización de
  tangencias, cambios del motor de intersecciones ni herramientas públicas nuevas (`CU3`).
- La regla de referencias de `CU0` (C1-4) y las puertas por contadores (C1-1).
- La valoración favorable de la dirección general de la preparación y las precisiones P1–P5
  (instrucción autoral de 2026-10-20); `NOT_ESTABLISHED` sigue siendo obligatorio cuando no
  hay prueba, pero no es el resultado objetivo del valor crítico.
- La regla de C1 (instrucción autoral de cierre de coherencia, 2026-10-20): ninguna componente
  se declara `COMPLETE` y continua solo porque su ecuación geométrica admita una raíz doble si la
  salida indexada que define el punto trazado queda indefinida en ese parámetro; ninguna
  identidad de salida histórica se sustituye por otra por coincidencia espacial.
- El mandato editorial E1 (misma instrucción): es una obligación de cierre del track, registrada
  en el [plan](../architecture/pre_g9b_cu_corrective_usability_plan.md) (`CU10` y §8), en la hoja
  de ruta y en el
  [plan de transferencia científica](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md).
  No es una decisión de `CU2`. El diseño detallado de los apéndices está pendiente de preparación
  y revisión, y su redacción e incorporación a `geocedg_book` no están autorizadas.

## 2. Para aprobar la arquitectura y las normas

| Id | Decisión | Opciones | Recomendación |
|---|---|---|---|
| D-CU2-1 | Certificación híbrida (A2.1-R) y ADR 0038: cuatro niveles de evidencia, completitud como recubrimiento, continuidad de `F` como requisito, arcos con extremos laterales (P2), autoridad única de cierre, existencia, evaluación y representación como hechos distintos (P5), regla de publicación, e identidades, linaje y selectores (P4) | aprobar / aprobar con cambios / rechazar | aprobar. Sustituye a E-0 de `CU0` y extiende el ADR 0027 sin cambiarlo |
| D-CU2-2 | Clase v3 del modelo de construcciones certificadas (A2.2-R): solo las capacidades que necesita el modelo del autor; selección leída del estado del algoritmo (P4); resultado de caja tipado, con `UNIFORMLY_INVALID` por intervalos solo para desigualdades estrictas; transiciones certificadas; totalidad y periodicidad estructurales | aprobar / limitar a una parte | aprobar. Sin cualquiera de esas piezas, al menos un locus del modelo queda `NOT_ESTABLISHED` ([análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §6) |
| D-CU2-3 | Representación y certificación de `e_2` (sustituye a D-1 de `CU0`) | (a) una componente abierta cortada en el origen de la longitud orientada, con dos extremos laterales distintos (P2), certificada con el certificado estructural de la función métrica y el evento `ORIGIN_WRAP` (A2.3-R), sin usar el valor de la longitud y sin cambiar `LocusLength`; (b) `e_2` `NOT_ESTABLISHED` hasta que la longitud de `o_1` se certifique en clase 1 o 2 | (a). (b) deja `e_2` sin componentes certificadas ni dibujo hasta un paquete futuro |
| D-CU2-4 | Topología del dibujo y política de teselación v2 (A2.4-R), con fidelidad declarada y extremos no evaluables (P5); alineación de los consumidores (A2.5); topología de las exportaciones (A2.6) | aprobar / aprobar con cambios | aprobar |
| D-CU2-5 | Constantes de certificación: anchura `δ`, presupuesto de cajas, profundidad mínima de existencia, límite de pasos de la clase v3, y bits y bisecciones del certificado exacto ([propuesta](../architecture/pre_g9b_cu2_normative_proposal.md) §8) | aprobar los intervalos ahora y los valores con el candidato de implementación / fijar los valores ahora | aprobar los intervalos; los valores los fija el banco (precedente D-CU1-4) |
| D-CU2-6 | Pliegues (P3) | (a) certificarlos solo con cinco pruebas separadas: existencia por una simetría estructural verificada sobre el programa capturado, aislamiento y no degeneración por chorros de segundo orden, continuidad y tangentes laterales opuestas; los chorros, en `CU2` solo para esas pruebas; (b) diferir pliegues y chorros a `CU3` | (a). Con (b), los pliegues de `e_1` se refinan hasta la resolución del parámetro con diagnóstico, y B06 queda sin corregir en `e_1` |
| D-CU2-7 | Alcance de la retirada del certificado de compatibilidad | (a) el productor dependiente pasa al certificador híbrido; los productores estáticos y analíticos (SplineV2, recorridos de segmento y fixtures del laboratorio) pasan a un certificado explícito «probado por el proveedor», con su contrato citado, como prevé el ADR 0027; (b) recertificar también esos productores con cajas | (a). Su contrato ya prueba la validez en todo el dominio; (b) añade coste sin corregir ninguna incidencia |
| D-CU2-8 | Locus con definición definida y recubrimiento certificado vacío | (a) publicado, `VALID`, `COMPLETE`, cero componentes; (b) estado de definición `EMPTY_DOMAIN`; (c) `UNDEFINED` | (a). Conserva la separación entre dominio declarado y cobertura (ADR 0027, decisión 1). (b) mezcla los dos ejes; (c) es el defecto H01 |
| D-CU2-9 | Certificador de 512 celdas de la raíz seleccionada de SplineV2 (`SelectedRootContinuousDomainCertifier2D`), que usa el testigo `m` | (a) conservarlo como caso particular del contrato híbrido, corrigiendo su dependencia de la raíz actual (que contradice A1.1-R) y la última celda del dominio semiabierto; (b) sustituirlo por el certificador de cajas sobre `SplineIntervalModel2D` | (a): cambio menor que conserva el testigo aprobado; (b) en un paquete posterior |
| D-CU2-10 | Constantes de dibujo DC-6 (`d_min`, `θ_max`, presupuesto de vértices) y criterio de giro en las exportaciones | intervalos ahora y valores con el banco; criterio de giro solo en el dibujo / también en las exportaciones | intervalos ahora; giro solo en el dibujo. La exportación conserva su tolerancia en unidades del modelo, ahora con la desviación sin acotar, que ya impide puentear saltos |
| D-CU2-11 | Especificación durable de la exportación LaTeX | (a) crear una especificación en `geocedg/specs/export/` con las reglas de DQ-C5, hoy solo en el [registro de decisiones de `R6-plus-C`](pre_g9b_r6_plus_c_author_decisions_record.md), y la topología de A2.6; (b) mantenerlas en el registro | (a) |
| D-CU2-16 | Certificado algebraico exacto para el valor crítico y las degeneraciones equivalentes (P1): nueva capacidad del núcleo (racionales y polinomios exactos, máximo común divisor y sucesión de Sturm) para predicados de primer nivel, con la API, la evidencia, el presupuesto y las pruebas del [diseño](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §4.6, y con los binary64 de las constantes capturadas como referencia semántica | (a) incluirla en `CU2`; (b) solo intervalos (método del punto estacionario) y `NOT_ESTABLISHED` en el valor crítico | (a). Entre las técnicas estudiadas, es la que prueba la igualdad del caso crítico con las constantes capturadas y decide los binary64 contiguos; cuesta milisegundos por predicado. Hay procedimientos verificados de intervalos para raíces múltiples, pero necesitan estructura añadida o certifican un problema perturbado (C3). Con (b), el valor crítico y sus vecinos quedan sin decidir |
| D-CU2-17 | Lectura del estado de selección (P4) | (a) accesores de solo lectura en `AlgoIntersectLineConic` y `AlgoIntersectSingle`, registrados como modificaciones; el estado entra en la huella de la revisión y la copia del corte debe coincidir con el algoritmo vivo, o la selección falla cerrada; (b) escribir el estado del algoritmo vivo en la copia (cambio de escritura en clases heredadas) | (a). No cambia el comportamiento heredado y no infiere nada por cercanía; (b) solo si (a) fallara en el modelo del autor, con una decisión aparte |
| D-CU2-18 | Salidas efectivas en raíces coincidentes (C1; [diseño](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §4.7; [análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §3.9) | (a) contrato de salidas efectivas: el etiquetado de las salidas entra en el estado de selección y en la huella; una segunda salida sin etiqueta recibe una banda `UNRESOLVED` alrededor de la raíz coincidente y su locus queda `NOT_ESTABLISHED`; el valor límite certificado es solo evidencia; la regla heredada no cambia; en una primera inicialización en una raíz doble, el cambio de estado que provoca el primer movimiento publica una revisión con diagnóstico y las mismas componentes; (b) además, preparar en un paquete con autorización propia una capacidad mínima del núcleo, la selección continua de raíces coincidentes en el corte, con el contrato, el ciclo de vida, la compatibilidad y las pruebas del diseño §4.7; (c) cambiar la regla heredada (cambio de comportamiento upstream) | (a). El modelo del autor no necesita más: `{Q, R}` están etiquetadas y son continuas en la raíz doble. (b) solo si el autor quiere continuidad en esas construcciones; define el Locus V2 donde el objeto vivo no lo está y hay que declararlo. (c) no se recomienda |
| D-CU2-19 | Componentes puntuales (C2; diseño §3.5; [consumo](../architecture/pre_g9b_cu2_consumption_and_representation.md) §3.1) | (a) entidad propia con el contrato del diseño §3.5; en `CU2`, declarada en el inspector, en el anexo DXF y en los comentarios LaTeX, sin dibujarla; los consumidores que no la admiten lo declaran y fallan explícitamente; ningún objeto punto público; (b) como (a), y además dibujarla con una marca en `CU2`; (c) no admitirlas y dejar `UNRESOLVED` la existencia puntual | (a). (b) añade una representación nueva que el modelo del autor no necesita; (c) pierde una existencia certificada y la completitud de esos dominios |
| D-CU2-20 | Huella exacta de la revisión (hallazgo F-CU2-12; A2.1-R) | (a) corregir en `CU2`, en el paso T3, la huella de `CU1` (`ReconstructionSliceFingerprint2D`) para que use los valores binary64 exactos, como exige la «exact fingerprint» de `locus-v2-semantics.md` v1.2, con la prueba V-REV-EXACT-1; (b) corregirla en un paquete correctivo de `CU1` previo a `CU2` | (a). Es conformidad con una norma aprobada, pero toca un componente aprobado de `CU1` y por eso se somete a decisión. Sin la corrección, los certificados de los binary64 contiguos al valor crítico podrían ser los de la configuración anterior (condición de parada 16 de la estrategia) |

## 3. Para autorizar la implementación

| Id | Decisión | Recomendación |
|---|---|---|
| D-CU2-12 | Clase de verificación: `INTEGRATED_PHASE` (`PHASE` propio, `PHASE` de `CU0` y `CU1`, e `INTEGRATION`) o `GLOBAL_IMPACT` (`FINAL`) | `INTEGRATED_PHASE`. El impacto queda en los consumidores de Locus V2, que cubren `PHASE` e `INTEGRATION`; `FINAL` solo añade hojas de la infraestructura de verificación, que `CU2` no toca |
| D-CU2-13 | Commit base exacto de la implementación | el candidato de esta revisión o su descendiente de cierre aprobado, con el commit y el árbol exactos nombrados por el autor |
| D-CU2-14 | Procedimiento de inversión de las fijaciones `B07`, `H01`, `B06` y `B10.2` y nueva línea base ([estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) §5) | aprobar |
| D-CU2-15 | Corrección documental del manifiesto del corpus: describe el cilindro como vertical y es oblicuo (hallazgo F-CU2-6) | hacerla en el paso T12 de `CU2`, con el registro de nueva línea base. No cambia ningún hash del modelo ni ninguna referencia, y la prueba del corpus no fija la descripción |

## 4. Decisiones de `CU0` que siguen abiertas

Formuladas en la [hoja de `CU0`](pre_g9b_cu0_author_decision_sheet.md) §3 para decidir antes de
autorizar `CU2`. E-0 y D-1 quedan sustituidas por D-CU2-1 a D-CU2-3.

| Id | Decisión | Recomendación |
|---|---|---|
| E-1 | Evidencia `EXPLORATORY` en el dibujo: no dibujarla, o dibujarla con marca visual distinta | dibujarla con marca visual solo en bandas `UNRESOLVED`, nunca unida a una componente ni exportada. Es el único cambio visible fuera de la geometría |
| E-2 | Puntos y extremos métricos: exigir una componente certificada que contenga la dirección, o aceptar validez puntual | exigir componente certificada, aunque sea local, para rutas y continuidad; validez puntual solo para puntos exactos aislados |
| E-3 (revisada) | Linaje de componentes y resolución de selectores guardados (P4) | linaje por las fuentes estructurales de las fronteras; un selector se resuelve solo por una correspondencia estructural demostrada (igualdad de la descomposición o transición de linaje registrada), que la contención única valida pero no crea; sin reescribir el contenido persistido; fallo cerrado con causa tipada ante ambigüedad |

## 5. Observaciones registradas para otros paquetes

- **`CU3` (F-CU2-5).** La línea base de `CU0` registra la raíz de la costura de
  `Intersect(e_1, c_1)` como `TRANSVERSE_ESTABLISHED` (`pins.B08_B09`). Geométricamente es una
  tangencia: conservarla como transversal conservaría un artefacto. No requiere decisión ahora.
- **`CU3`.** Las fronteras certificadas, los parámetros singulares y los chorros de segundo
  orden de `CU2` son entradas para los contactos tangentes. La precisión
  `OBS-CU0-CU3-TANGENT-EXISTENCE` no cambia.
- **Comportamiento heredado (F-CU2-8, F-CU2-9, F-CU2-13).** La permutación de salida de las
  intersecciones se reinicializa por cercanía, el evaluador declara tangente lo que está a
  menos de su tolerancia y la segunda salida sin etiqueta se anula cuando coincide con la
  primera. `CU2` no los cambia: los lee o los contradice con el certificado. Una persistencia
  exacta del estado de selección exigiría un cambio de formato, fuera de `CU2`.
- **Limitación de `CU1`.** `L-CU1-TRANSLATION-RESOLUTION` sigue abierta. La matriz admite
  el fallo cerrado de la certificación con traslaciones de `10⁶` (V-INV-2).

## 6. Fuera de decisión en este momento

- publicación, etiqueta y cambios en `main`;
- el libro: el mandato editorial E1 ya está aprobado (§1), pero el diseño detallado de los
  apéndices, su redacción y su incorporación a `geocedg_book` necesitan autorizaciones propias;
- la implementación de `CU3`–`CU10`;
- la certificación en clase 1 o 2 de la longitud de `o_1` y de `e_2`;
- la reescritura de selectores persistidos y la captura simbólica de tangencias calculadas con
  redondeo, que necesitarían su propia autorización.
