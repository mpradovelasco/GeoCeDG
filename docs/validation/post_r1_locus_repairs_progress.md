# Ejecución posterior a R1 — registro de progreso y checkpoints

**CANDIDATO TÉCNICO — PENDIENTE DE REVISIÓN AUTORAL.** `selfApproved=false`. Registro único
de progreso del encargo «Ejecución secuencial tras R1: reparaciones, modelo y uso
condicionado de Locus V2», de su continuación «Revisión normativa post-R1, continuación de
implementación y objetivo 1.2.2» y de las respuestas del autor a las revisiones 2 y 3
(instrucciones autorales del 2026-11-21). No es un cierre
de fase, no registra aprobación y no autoriza publicar en `main`.

## Plan y perímetro

1. Bloque 1 — reparaciones A, F7, F6, B y A′, con pruebas focales, aclaraciones normativas
   estrechas y benchmark.
2. Bloque 2 — modelo corregido (transporte del contorno del agujero) y evidencia portable.
3. Bloque 3A — diseño completo del uso condicionado y paquete normativo para revisión.
4. Revisión normativa del paquete 3A con las decisiones D-V1 a D-V7 del autor, y
   optimización D-V6 en línea separada.
5. Bloque 3B — implementación de V: **no iniciada**; requiere aprobación del texto
   normativo revisado.
6. Candidato GeoCeDG 1.2.2: **no iniciado**; ninguna fuente de versión se ha tocado.

Perímetro: kernel Java compartido (huella del slice, ledger, evaluador aislado, registro
de identidad, productor de intersecciones, admisión de expresiones), mensajes, pruebas,
modelos de regresión, validación, arnés de benchmark, especificaciones de locus y
documentación. Fuera de perímetro: `CLAUDE.md`, `AGENTS.md`, prompts, guías de usuario,
empaquetado, libro.

## Estado inicial

| Dato | Valor |
|---|---|
| `main` local y `origin/main` | `9ccefdcfe922d8c0e329bdeec30c12fc058de463`, árbol `18dbb9bc4216a536b5b2fc3d17321737001cdd36`, limpio |
| Rama de trabajo | `maintenance/post-r1-locus-repairs-and-conditioned-use`, creada desde esa base |
| Modelo original | `C:\temp\InterCilConoObliqueFinal_2.cedg`, 78 781 bytes, SHA-256 `949c2c931d22ec33820cd3ae5b35e8a75b94f5f3d45474fb8e818d74ff98159b`: verificado |
| Evidencia R1 | presente (dos emisiones del 2026-11-21); ningún artefacto auxiliar faltante |
| Política de verificación | `verification-levels.md`, «Operational simplification»: ordinaria = CORE + FOCUSED; `FINAL` solo con autorización expresa. Vigente |
| Estación | PowerShell 7.6.10; JVM de pruebas OpenJDK 17.0.10; Windows 11 |

## Checkpoint 1 — bloque 1: reparaciones

Estado: **terminado**. Diseño, alternativas y pruebas discriminantes:
[documento de reparaciones](../architecture/post_r1_locus_repairs.md).

| Pieza | Commit | Prueba focal |
|---|---|---|
| A — huella del slice y ledger | `3881d7340` | `PostR1SliceLedgerFingerprintTest` (6) |
| F7 — propiedad de auxiliares | `bc878779d` | `PostR1DedicatedAuxiliaryOwnershipTest` (5) |
| F6 — autoridad de la evaluación aislada | `d111a2e55` | `PostR1IsolatedEvaluationAuthorityTest` (5) |
| B — actualización equivalente | `efed59bc8`, `246f0bb2d` | `PostR1EquivalentIntersectionUpdateTest` (4) |
| A′ — conductores anidados | `417ab2c0f` | `PostR1NestedLocusDriverFingerprintTest` (4) |

Evidencia durante el desarrollo (Gradle directo, no perfil de aceptación): todas las
pruebas `org.geocedg.*` del módulo compartido sobre `417ab2c0f`, 245 suites, 2 410 casos, 0
fallos. Ablaciones: A 4/6, F7 4/5 y A′ 3/4 casos fallan sin la reparación.

Benchmark antes/después con el mismo arnés: paso de `E_1` en el modelo original 2 369 ms →
58 ms; paso de `N` 1 582 ms → 24 ms; revisiones de `w_2`/`a_3` por paso 1 → 0.

## Checkpoint 2 — bloque 2: modelo y evidencia portable

Estado: **terminado**. Commit `49704eb7a` (paquete, oráculo, pruebas Desktop y arnés) y
`2dd34f248` (escenario de inspección de evidencia).

- Paquete `models/regression/post-r1-locus-on-intersection/`: original byte-exacto, receta
  única del transporte, variante generada, manifiesto y resultados esperados.
- Referencia independiente `geocedg/validation/post-r1-locus/`: reproduce byte a byte las
  tablas de R1 con entradas reducidas.
- `PostR1AuthorModelRegressionTest` (3 casos): desviación máxima frente a la referencia
  1,5·10⁻⁸; imagen conjugada ausente; independencia de `E_1`; guardado y reapertura con las
  mismas identidades.
- Limitación vigente declarada: el contorno transportado es fiel y `NOT_ESTABLISHED`; su
  dibujo y su consumo son los de la evidencia exploratoria hasta que exista V.

## Checkpoint 3 — bloque 3A: diseño del uso condicionado

Estado: **paquete entregado para revisión; implementación de V en pausa**.

| Documento | Ruta |
|---|---|
| ADR propuesto | `docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md` |
| Contrato candidato | `geocedg/specs/locus/candidates/post-r1-conditioned-use.md` |
| Diseño | `docs/architecture/post_r1_locus_conditioned_use_design.md` |
| Hoja de decisiones | `docs/validation/post_r1_conditioned_use_author_decision_sheet.md` |

Experimentos disponibles: testimonio de raíz en la copia aislada (`evidence`), sonda
externa de la travesía (`conditioned-use-probe.js`). Identidad del paquete: la del commit
que contiene este registro; véase «Identidad del candidato».

## Checkpoint 4 — revisión normativa, D-V6 y objetivo 1.2.2

Estado: **paquete normativo revisado entregado para aprobación; D-V6 terminado; 3B y el
candidato 1.2.2 sin iniciar**. Rama `maintenance/geocedg-1.2.2-post-r1`, creada desde
`96a4137e0`, local y sin publicar. La rama anterior no se ha movido.

Decisiones del autor incorporadas (D-V1 a D-V7): constan, con el lugar donde queda cada
una, en el [registro de decisiones](post_r1_conditioned_use_author_decision_sheet.md). Son
decisiones de producto; el ADR 0042 sigue `PROPOSED` y el contrato sigue siendo candidato.

| Línea | Commit | Contenido |
|---|---|---|
| D-V6, código | `4cbe89a3e` | `StructureMemo` en `ReconstructionSliceFingerprint2D`, solo dentro de copias aisladas; `PostR1FingerprintStructureMemoTest` (2); escenario `profile` y series nuevas del arnés |
| D-V6, registro | `266d0a85e` | [documento de rendimiento](../architecture/post_r1_locus_real_change_performance.md): perfil, atribución, alternativas excluidas, antes y después, hallazgo F8 |
| Revisión normativa | `83cd56c4c` | ADR 0042 revisión 2, contrato `conditioned-use/v2-candidate`, diseño revisión 2 y registro de decisiones, con las diferencias respecto a `ab37fb5ff` |
| Inventario | `1838c7211` | la prueba nueva entra en `post-r1-locus.shared` (211 identidades); descubrimiento compartido 6 942; pin del catálogo |

D-V6, medido con el mismo arnés sobre `ab37fb5ff` y `4cbe89a3e`, actualización del grafo
por cambio de `excCyl`: modelo original 1 696 → 605 ms de mediana (p95 1 705 → 627);
variante transportada 2 132 → 719 ms (p95 2 162 → 723); a través del cambio de topología y
tras pérdida y recuperación, máximos de 644 y 720 ms. Objetivo de menos de 1 s, también en
p95: cumplido. Los contadores de revisiones, reconstrucciones y evaluaciones son idénticos
antes y después. Sin caché compartida de resultados ni copias mutables compartidas.

Hallazgo **F8**, previo y reproducible en la base `9ccefdcfe`: un locus anidado vivo pierde
su definición cuando el conductor de un locus consumidor es entrada no conductora suya.
Reproductor en el documento de rendimiento, §4. No triado ni tocado; propuesto dentro de
I4 (punto P4 del registro de decisiones).

Puntos que requieren decisión del autor: P1 (aprobación del texto normativo revisado), P2
(marca del sistema), P3 (ubicación en el roadmap de la exportación semántica) y P4 (F8).
El roadmap no se ha modificado.

## Checkpoint 5 — enmienda focal, respuestas del autor y reparación F8

Estado: **texto normativo corregido entregado para aprobación definitiva; F8 reparado; 3B
y el candidato 1.2.2 sin iniciar**. Misma rama, local y sin publicar.

Respuesta del autor a la revisión 2 (2026-11-21), registrada en el
[registro de decisiones](post_r1_conditioned_use_author_decision_sheet.md), §2: valoración
favorable, D-V1 a D-V7 mantenidas, D-V6 aceptado técnicamente, aprobación normativa
pendiente de una enmienda focal de seis puntos, marca del sistema aprobada (rombo hueco de
7 px), exportación semántica a registrar como requisito de línea futura, y triaje y
reparación de F8 autorizados.

| Línea | Commit | Contenido |
|---|---|---|
| Enmienda focal | `57c495cce` | ADR 0042 revisión 3, contrato `conditioned-use/v3-candidate`, diseño revisión 3 y registro de decisiones revisión 3. Los seis puntos, su diferencial y sus criterios de validación están en el §14 del contrato |
| Roadmap (P3) | `58231d7d8` | registro aditivo fechado: la exportación semántica de regiones condicionadas es requisito de la línea futura de B-splines, representaciones derivadas y DXF. Ninguna fase ni subfase abierta |
| F8 (P4) | `350b9d115` | guarda de macro-kernel en `LayerManager.replace`; `PostR1SliceCopyViewIsolationTest` (1) y `PostR1SliceCopyViewIsolationDesktopTest` (1); nota de diseño en el documento de reparaciones, §1.6 |
| Inventario | `afb916e10` | las dos pruebas entran en las selecciones de fase (212 compartidas, 25 Desktop); descubrimientos 6 943 y 2 599; pin del catálogo |

Resolución de los seis puntos, en una línea cada uno: (1) `CONDITIONED` nunca concede
`RELAXED`; se preservan los requisitos positivos de la autoridad constructiva y no se
propone ampliarlos. (2) La raíz sobre un tramo toma un selector de tipo propio con la rama
como marco, sin rango; la región no recibe linaje y la partición nunca es identidad. (3) La
identidad de contenido de la evidencia se separa del contador de publicación; la vigencia
se decide por celdas. (4) La validez puntual se separa de la admisibilidad de continuación:
un punto válido dentro de una banda está definido. (5) La razón cero conserva el contrato
vigente de `COLLAPSED_IMAGE`. (6) `MARGIN_BOUNDED` solo prueba la separación de las raíces
que la cota encierra; H2 queda entera.

F8, causa demostrada: la carga de una copia aislada pasaba por la ruta de redefinición para
las entradas anónimas repetidas y, desde ella, ordenaba la lista de dibujables de la vista
viva; con la vista recorriendo esa lista, la excepción se descartaba en la ruta de carga de
macros y la copia quedaba vacía. En Desktop el efecto era un
`ConcurrentModificationException` al actualizar los dibujables. Ablación: sin la guarda
fallan las dos pruebas nuevas. Detalle en el
[documento de reparaciones, §1.6](../architecture/post_r1_locus_repairs.md#16-f8--la-carga-de-una-copia-aislada-no-toca-la-vista-viva).

## Checkpoint 6 — dos precisiones focales y trabajo preparatorio de I0

Estado: **texto normativo de la revisión 4 entregado para aprobación definitiva; primer
candidato preparatorio de I0 verificado; I4, I4T y el candidato 1.2.2 sin iniciar**. Misma
rama, local y sin publicar.

Respuesta del autor a la revisión 3 (2026-11-21), registrada en el
[registro de decisiones](post_r1_conditioned_use_author_decision_sheet.md), §3 bis: las seis
enmiendas están correctamente incorporadas y la arquitectura general tiene su conformidad;
antes de registrar la aprobación normativa definitiva pide dos precisiones focales, sin
ampliar la revisión a otros contratos ni repetir verificaciones válidas; permite adelantar
los trabajos preparatorios de I0 a I3 que no dependan de ellas, como candidatos pendientes,
y no iniciar I4 ni I4T; no autoriza publicación en `main`, etiqueta `v1.2.2` ni release.

| Línea | Commit | Contenido |
|---|---|---|
| Precisiones focales | `9160d67d7` | ADR 0042 revisión 4, contrato `conditioned-use/v4-candidate` (§6.5 puntos 4 a 9, §8.7 «Resolution under a similarity», §15 con el diferencial y los criterios 7a a 7e y 8a a 8f), diseño revisión 4 y registro de decisiones revisión 4; segunda sonda externa `tools/benchmark/post-r1-locus/conditioned-use-precisions-probe.js` |
| I0 preparatorio | `ee1518a1e` | paquete nuevo `org.geocedg.common.kernel.locus.conditioned` (12 clases) y `PostR1ConditionedEvidenceTest` (15 casos) |
| Inventario | `349c1856c` | la prueba entra en `post-r1-locus.shared` (227 identidades); descubrimiento compartido 6 958; pin del catálogo |

Resolución de las dos precisiones, en una línea cada una: (7) una sola asignación por
selector de marco de rama, con un origen fijo; el token solo se resuelve si el origen y la
raíz actual están en una misma región condicionada y, si no, se retiene sin entregarlo a
otra raíz: la evidencia puede retener un token y nunca lo elige. (8) La cota de H4 de la
fuente vale `ρ·δ` en la imagen; con razón absoluta no mayor que 1 las celdas se heredan
iguales y con razón mayor cada tramo se recomprueba en el mundo de la imagen y el que falla
se reclasifica; una banda nunca pasa a tramo.

Pruebas discriminantes. Los criterios 7a a 7e y 8a a 8f del contrato §15 son pruebas de I4 e
I4T, que no se han iniciado. Su contenido se ejecuta ya, sobre evaluadores de verdad
conocida y con la regla de la revisión 3 y la de la revisión 4 en paralelo, en la sonda
`conditioned-use-precisions-probe.js` (`node`, código de salida 0, «every discriminating
check holds»):

| Caso | Regla de la revisión 3 | Regla de la revisión 4 |
|---|---|---|
| C17, desaparición y aparición en regiones separadas (`c` = 0,2 → 1,8 → 1,0 → 1,8 → 0,3) | el token pasa a la raíz de la segunda región y el punto dependiente salta a ella | token retenido en un paso y tras un estado sin raíces; raíz con asa local; una sola entrada en el ledger; recuperado al volver a la primera región |
| C17b, la evidencia no elige | — | con profundidades iniciales 32, 64 y 128, y con una banda confirmada entre el origen y la raíz, la raíz tiene el token o ninguno |
| C17c, historia directa | — | el documento construido en el segundo estado guarda otro origen; su token se retiene en el primero |
| C18, circunferencia unidad, límite 10⁻³ | razón 4: 128 tramos con flecha 1,205·10⁻³, todos sobre el límite | razón 0, 0,5, 1 y 3: las 128 celdas heredadas; razón 4: 256 tramos de 3,012·10⁻⁴, iguales a los de una travesía propia |
| C18, paso mínimo 2⁻⁸ y razón 100 | 128 tramos de 3,0·10⁻² | 256 bandas, ningún tramo sobre el límite |
| C18b, salto de 0,5 a razón 10⁻⁴ | — | la banda se conserva; una travesía de la imagen sola la daría por tramo |
| C18c, composición | — | `Dilate(Dilate(L, 4), 1/4)`: 256 celdas; `Dilate(Dilate(L, 1/4), 4)`: 128 |

Trabajo preparatorio de I0 (candidato pendiente de la aprobación normativa; ninguna
operación pública lo lee y nada del producto llama al paquete nuevo). Contiene: tipos de
celda, clases de tramo e hipótesis; muestra con testimonio de selección; política
versionada `conditioned-traversal/v1`; evidencia inmutable con identidad de contenido,
regiones y retirada; travesía diádica en anchura con conjunto de contradicciones y límite de
trabajo; propietario del lado del productor con contador de publicación y regla de aviso; y
registro de las celdas usadas por un resultado. Las pruebas cubren los casos C1 a C6, C9,
C12 y C16 del banco y los criterios 3a, 3b, 4b y 6 del contrato. Ablaciones: sin la división
por contradicción fallan 5 pruebas; sin la regla de que el interior de un fallo no se
subdivide, 2; si los nodos ya evaluados no se cargan al límite de trabajo, 1.

Queda de I0, sin hacer: el testimonio leído del evaluador real del slice, la integración en
el productor del locus, el banco con el protocolo real y la medición del coste por
revisión. Tres valores o reglas del candidato se fijan al cerrar I0 y hoy son provisionales:
el umbral de margen (10⁻³) y el límite de trabajo (2 048 evaluaciones) de la política, y la
clasificación de una celda cuyos nodos están ausentes con causas distintas, que hoy es
`FAILURE_BAND` por prudencia (no es ausencia condicionada, que exige la misma causa).

## Checkpoint 7 — aprobación normativa y bloque 3B

Decisión autoral de 2026-11-22, «APROBACIÓN AUTORAL — ADR 0042 Y USO CONDICIONADO DE LOCUS
V2», transcrita en el
[registro de aprobación](post_r1_conditioned_use_author_approval_record.md): aprobados el ADR
0042 revisión 4 y el contrato `conditioned-use/v4-candidate` del commit `9160d67d7`, y
autorizado el bloque 3B completo (I0 a I4T) sin consultas rutinarias. Siguen sin autorizar la
publicación en `main`, la etiqueta `v1.2.2`, una release y `FINAL`. La aprobación del texto
no aprueba ninguna implementación: cada incremento es un candidato técnico.

Este checkpoint se amplía con cada incremento. Las enmiendas del contrato §12 se incorporan
a las especificaciones con el incremento que implementa cada una.

| Paso | Commits | Contenido | Verificación ordinaria |
|---|---|---|---|
| Registro de la aprobación | `2ee7aae26` | registro de aprobación; ADR 0042 `ACCEPTED`; contrato `conditioned-use/v4`; cabeceras del diseño y del registro de decisiones; registro aditivo en la hoja de ruta | `STATIC` `verification-d2744077338947a7b33ee2e2f6cf881b`, `ACCEPTED / COMPLETE` |
| **I0** | `02b1d688f` (código, especificación, arnés), `4490d58fb` (inventario) | testimonio del evaluador real; lista cerrada de causas de ausencia; propietario de la evidencia en el productor y accesos en `GeoLocusV2`; ninguna celda dentro de una cobertura certificada; política v1 con límite de trabajo 1 024; escenario `conditioned` del arnés; `locus-v2-existence-components.md` 1.2, §19 | sobre `4490d58fb` (árbol `0e5adf81`): `STATIC` `verification-63e5bd05085542348374b113342fe26f`, `PHASE POST-R1-LOCUS` `verification-e9081ba52f334db9a6ff5c1a56f829de` (235 identidades compartidas y 25 de Desktop), `INTEGRATION` `verification-0104d11323c04f2cb5510c84f8c81c3d`; las tres `ACCEPTED / COMPLETE`, código 0 |
| **I1** | `cbced7165` (código, especificaciones), `433091e99` (inventario) | dirección por parámetro y su códec `geocedg-locus-parameter-address/v1`; validez puntual separada de la admisibilidad de la continuación; seguimiento, arrastre y colocación por clic sobre regiones condicionadas; enmiendas de `locus-v2-public-surface.md`, `locus-v2-point-interaction.md` y §19.4 de existencia | sobre `433091e99` (árbol `486e8e89`): `STATIC` `verification-5fb01067b27e4dc49a9ac862a241686a`, `PHASE POST-R1-LOCUS` `verification-e9ee390c6cd64da6bf6f192751568495`, `INTEGRATION` `verification-4c5268a2c6bf4c19b13bc78982518c43`; las tres `ACCEPTED / COMPLETE`, código 0 |
| **I2** | `a704a406c` (código, especificación), `45ab0157d` (inventario) | una región condicionada se dibuja como un trazo continuo con el estilo del objeto; marca de sistema (rombo hueco de 7 px); grado y explicación en las propiedades; enmienda de `locus-v2-presentation.md` | sobre `45ab0157d` (árbol `303c00cb`): `STATIC` `verification-358032b278104ce89254cfeb414145fc`, `PHASE POST-R1-LOCUS` `verification-fa0c456e389c41baa81ebe9fc5cef444`, `INTEGRATION` `verification-0966b2ebe7234c63b52a75939cb486d2`; las tres `ACCEPTED / COMPLETE`, código 0 |
| **I3** | `3cf7c2994` (código, especificación), `c7e1f2a56` (inventario) | longitud entre dos posiciones de una región y longitud total sobre regiones condicionadas, con subtotales; `locus-v2-metrics.md` §28; la admisión escalar no se amplía | sobre `c7e1f2a56` (árbol `7abe3372`): `STATIC` `verification-37fb707973ed418e894865071ad1d2fe`, `PHASE POST-R1-LOCUS` `verification-a002cd94974a4b8695f7c9ce6eb7bddc`, `INTEGRATION` `verification-c494bf71f8ad46b8bd79d10296656a07`; las tres `ACCEPTED / COMPLETE`, código 0 |
| **I4** | `637b5ee69` (código, especificaciones), `223e1d6de` (inventario), `048ec4edf` (corrección) | `Intersect` sobre regiones condicionadas; selector de marco de rama; libro de tokens versión 7 con origen y guarda de reutilización; pares; lugares dependientes y anidados; declaración en la exportación semántica | sobre `223e1d6de` (árbol `c9a25378`): `STATIC` `verification-53949bc6a1734ceda32cf2b88ec6d093`, `PHASE POST-R1-LOCUS` `verification-50398e1da5264506b9f19ac5614680d7`, `INTEGRATION` `verification-80480162e8b747f2b92ce583a4d03b53`; las tres `ACCEPTED / COMPLETE`, código 0. La corrección `048ec4edf` queda cubierta por la verificación de I4T |
| **I4T** | `d5589c271` (código, especificación, pruebas), `0f02c8f21` (inventario) | la imagen por una semejanza hereda la evidencia condicionada celda a celda y la enuncia en su propio mundo; `locus-v2-similarity-transformations.md`, enmienda de uso condicionado | sobre `0f02c8f21` (árbol `165d6b0c`): `STATIC` `verification-47684adf4b90459091b0df73d8ec1b27`, `PHASE POST-R1-LOCUS` `verification-ee22fb8aa8cd49c8adbf14a23315f02e`, `INTEGRATION` `verification-e41bdd5c26a44ceea4c741664f1000c5`; las tres `ACCEPTED / COMPLETE`, código 0 |

### I0 — evidencia condicionada desde el productor real

- **Testimonio.** `ReconstructibleLocusEvaluator2D.evaluateWithTestimony` lee, de lo que la
  copia aislada calculó para esa evaluación, el testimonio de las dependencias selectoras
  que lee el punto trazado: puntos de token (token exacto, admisibilidad y distancia a la
  otra raíz), selecciones recta-cónica heredadas (estado de selección reproducible) y puntos
  estructurales. No inicia ninguna resolución.
- **Ausencia y fallo.** Lista cerrada de `conditioned-traversal/v1`:
  `DEPENDENCY_UNDEFINED` e `INTERSECTION_ROOT_ABSENT`. Todo lo demás es fallo. Una celda con
  ausencias de causas distintas es `FAILURE_BAND` (regla definitiva).
- **Productor.** Una evidencia por revisión, calculada a petición sobre la parte del dominio
  declarado que el certificado no cubre. Mover el conductor no publica revisión y la
  conserva. Un aviso solo cambia el contenido si el productor lo confirma con su evaluador.
- **Ningún consumidor cambia** en I0: estructura de existencia, definición y revisión son
  las mismas con y sin evidencia.
- **Pruebas.** `PostR1ConditionedProducerTest` (7 casos, kernel real): una región sobre un
  locus fuera de clase; ninguna evidencia en un locus certificado; ausencia condicionada y
  banda de transición localizada al paso mínimo; revisión nueva descarta la evidencia;
  aviso confirmado, no confirmado y repetido, con celdas retiradas y resultados vigentes
  (criterio 3b); testimonio del token y de la selección recta-cónica.
  `PostR1ConditionedEvidenceTest` gana el caso de cobertura certificada (16 casos).
- **Banco con el protocolo real y rendimiento.**
  [Documento de rendimiento, §6](../architecture/post_r1_locus_real_change_performance.md#6-coste-de-la-evidencia-condicionada-por-revisión-incremento-i0):
  `w_2` y `a_3`, dos regiones con cuatro bandas de transición en los pliegues, 353 a 385
  evaluaciones y 0,41 a 0,50 s por revisión; `trHole`, una región de periodo completo, 257
  evaluaciones y 0,16 s. Coste nulo al arrastrar. El banco real destapó y corrigió el
  criterio de testimonio de la selección recta-cónica (allí descrito).
- **Pendiente que I0 trasladó.** El sobrecoste por revisión cuando la evidencia se consume
  (≈ 0,9 s en el modelo original). La exploración perezosa del certificado prevista para I2
  no se hizo, por su riesgo sobre la versión de la política y las firmas; el coste se mide
  de nuevo y se informa con el candidato (hallazgo 7).

### I1 a I4T — resumen por incremento

Cada incremento se cerró del mismo modo: commit de código, commit de inventario creado en un
worktree aparte sobre ese código (selecciones `post-r1-locus.*` ampliadas, descubrimiento y
pin del catálogo), y `STATIC`, `PHASE POST-R1-LOCUS` e `INTEGRATION` sobre el commit de
inventario. Las selecciones `final.*` no se refrescan. Regresión completa
`org.geocedg.*`: compartida sin fallos; Desktop con el único fallo previo conocido,
`PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` (falla ya en
la base `9ccefdcfe`). En los worktrees falla además
`G9U1Revision3AlgebraEditingDiagnostic`, que lee un archivo del autor ignorado por Git y
ausente allí; en el checkout principal pasa.

- **I1.** `PostR1ParameterAddressTest` (14) y `PostR1ConditionedPointDesktopTest` (1).
  Criterios 1a (punto) y 4a. Tres pruebas anteriores pasan a afirmar el comportamiento
  aprobado.
- **I2.** `PostR1ConditionedPresentationTest` (6) y
  `PostR1ConditionedPresentationDesktopTest` (1). Pintado de 7 a 10 ms sobre el modelo.
- **I3.** `PostR1ConditionedMetricTest` (6). Una longitud sobre regiones condicionadas es
  un resultado rico de cobertura `INCOMPLETE`, nunca admisible como escalar.
- **I4.** `PostR1ConditionedIntersectionTest` (19), `PostR1ConditionedPairIntersectionTest`
  (3) y `PostR1ConditionedDependentLocusTest` (3), con órdenes reales sobre el kernel.
  Criterios 1a (raíz), 1b, 2a a 2d y 7a a 7e. La regresión de Desktop del incremento
  detectó que un lugar con una componente certificada y una región condicionada dejaba de
  codificarse en DXF, porque el codificador estricto rechazaba todo resultado sin entidad;
  `048ec4edf` lo corrige (un componente declarado nunca bloquea la petición estricta) y
  adapta `PreG9BS1AuthorDxfReproductionTest`.
- **I4T.** `PostR1ConditionedSimilarityTest` (8) y
  `PostR1ConditionedIntersectionDesktopTest` (1, sobre el modelo del autor: raíz con
  token en el lugar con hueco, token retenido en la otra región, imagen heredada y
  documento nativo releído). Criterios 5 y 8a a 8e; 8f es la suite de semejanza existente,
  que pasa sin cambios.

**Hallazgos que se trasladan al autor, sin decisión tomada:**

1. *Salto a lo largo de la cuerda.* Un salto en la dirección de la cuerda no deja rastro en
   la desviación ni en el giro y queda dentro de un tramo: es el límite declarado del
   recorrido (contrato §4). Un criterio adicional cambiaría el contenido de la política
   aprobada.
2. *Admisión escalar.* No se amplía a las longitudes condicionadas
   (`locus-v2-metrics.md` §28.3); es una decisión de admisibilidad aparte.
3. *Aislamiento local de pares.* La capacidad de solo evaluación nunca lo establece, de modo
   que ningún par con un lado condicionado podía recibir token. I4 añade la evidencia
   numérica que el modelo ya tipificaba (`SAFEGUARDED_REGULAR_JACOBIAN`), declarada no
   certificada.
4. *Clave de par.* La clave no ordenada de un par de lugares coincide para (i, j) y (j, i)
   cuando las dos fuentes comparten clave de rama y tienen varias componentes. Las regiones
   reciben claves propias de cada lado; el caso de dos fuentes certificadas de varias
   componentes no se ha reproducido ni tocado, porque cambiaría textos de identidad.
5. *Costura periódica.* La guarda de reutilización no continúa una región a través de la
   costura: una raíz que la cruza queda con el token retenido, que es el lado seguro.
6. *Pruebas de conformidad anteriores.* Cuatro pruebas que afirmaban «ninguna raíz» o
   «ningún token de par» sobre fuentes sin certificado afirman ahora el comportamiento
   aprobado: la raíz se declara condicionada, no hay ranura D2 y el punto existente no se
   reapunta. Se conserva el nombre `anUncertifiedExpressionIssuesNoPairToken`, citado por
   la matriz de validación de R3-X1.
7. *Coste.* La evidencia cuesta de 0,4 a 0,5 s por lugar con hueco y revisión; la medición
   hecha con I4 no es concluyente porque la máquina estaba cargada, y se repite con el
   candidato.

## Verificación del candidato

### Candidato de las precisiones focales y del trabajo preparatorio de I0

Candidato técnico verificado: `349c1856c6e42f7ddb14c7b24f064ca2d627dc01`, árbol
`7cc7637b31ad5260602569dc60461a16acbadb38`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. El commit del texto normativo, `9160d67d7`, no es un
descendiente solo documental de `afb916e10`: `tools/agent/documentary-descendant.ps1`
devuelve `REQUIRES_DECISION` (código 5) porque cambia textos clasificados como normativos y
añade un fichero bajo `tools/`. Por eso se verificó como candidato propio con `STATIC`
(`tools/agent/verify.ps1 -Profile STATIC -LogDirectory
artifacts/agent/post-r1-locus-static-9160d67d`, código 0,
`verification-f5c85675ce0f4fc6b0a87551d7eaf774`, `ACCEPTED / COMPLETE`); no cambia ningún
fichero bajo `source/`, así que `PHASE` e `INTEGRATION` de `afb916e10` no se repitieron
para él. El trabajo preparatorio de I0 sí añade código, y la verificación completa se
ejecutó sobre la punta técnica:

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-349c1856` | 0 | `verification-cbe19ad149904e79968389c70f704a77` | `ACCEPTED / COMPLETE` | los dos diagnósticos permanentes |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-349c1856` | 0 | `verification-36e4741d8f3941368d2077677b80938b` | `ACCEPTED / COMPLETE` | 227 identidades compartidas y 25 Desktop; 0 hallazgos de diagnóstico |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-349c1856` | 0 | `verification-521344db1c644d80b2fb120faea2a3d0` | `ACCEPTED / COMPLETE` | los dos diagnósticos permanentes |

Diagnósticos distintos de `DIAGNOSTIC_CLEAR`: los mismos dos permanentes que abajo
(`diagnostic.governance` y `diagnostic.historical-consistency`).

Bucle interno, que no es evidencia de aceptación: `gradlew :shared:common-jre:test --tests
org.geocedg.common.locus.PostR1ConditionedEvidenceTest` (15 casos, 0 fallos) y Checkstyle de
`:shared:common` y `:shared:common-jre` sin hallazgos en los ficheros nuevos. Los cuatro
puntos de cobertura reducida siguen vigentes sin cambio: `FINAL` no ejecutado ni
autorizado, `final.shared` y `final.desktop` sin refrescar, `INFRA_UNIT` completo no
ejecutado, y el fallo previo de `PreG9BCu2AuthorModelConsumerTest`. No se formula
`VERIFICATION_ESCALATION_REQUEST`.

### Candidato de la enmienda focal y de F8

Candidato técnico verificado: `afb916e10ce551df06c26000b7ab0ce671c64e3b`, árbol
`b3a192b0ec48d9fab0067e7a2508f7ee89a2ae3f`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. La verificación de `1838c7211` dejó de cubrir el candidato al
cambiar código de producto (F8), por lo que se repitió entera sobre el commit nuevo.

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-afb916e1` | 0 | `verification-f56427531ff94cdcadbd227984a714f5` | `ACCEPTED / COMPLETE` | 3 contratos; 6 diagnósticos |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-afb916e1` | 0 | `verification-0eeb268129534c03a7635fe111aca003` | `ACCEPTED / COMPLETE` | 6 contratos, 5 productores; 212 identidades compartidas y 25 Desktop; 0 diagnósticos |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-afb916e1` | 0 | `verification-8a4802967e8d465b8cb90177a27db277` | `ACCEPTED / COMPLETE` | 18 contratos, 15 productores; 9 diagnósticos |

Diagnósticos distintos de `DIAGNOSTIC_CLEAR`: los mismos dos permanentes que abajo. Los
cuatro puntos de cobertura reducida siguen vigentes sin cambio: `FINAL` no ejecutado ni
autorizado, `final.shared` y `final.desktop` sin refrescar, `INFRA_UNIT` completo no
ejecutado, y el fallo previo de `PreG9BCu2AuthorModelConsumerTest`. No se formula
`VERIFICATION_ESCALATION_REQUEST`.

### Candidato de la continuación

Candidato técnico verificado: `1838c721149bb6a4ad9088dff767cc64320cfb00`, árbol
`52329d83632de0c6ffc609400aa1dad31d19da7a`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. Todas las ejecuciones desde la raíz, con PowerShell 7.6.10.

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-1838c721` | 0 | `verification-c364304a1a9647eb95843bd9bf3c2a12` | `ACCEPTED / COMPLETE` | 3 contratos; 6 diagnósticos |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-1838c721` | 0 | `verification-b4a84680006a4916b63234788d0fc0a0` | `ACCEPTED / COMPLETE` | 6 contratos, 5 productores; 211 identidades compartidas y 24 Desktop; 0 diagnósticos |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-1838c721` | 0 | `verification-09243cff866f47d5a029c5bcca545ac5` | `ACCEPTED / COMPLETE` | 18 contratos, 15 productores; 9 diagnósticos |

Diagnósticos distintos de `DIAGNOSTIC_CLEAR`: los mismos dos permanentes que abajo.

Otras comprobaciones de la continuación, con Gradle directo y no como perfil de aceptación:
las pruebas compartidas de locus (1 935 casos) pasan con el modo de verificación del memo
activado, que recalcula siempre y falla si un resumen reutilizado difiere. Los cuatro
puntos de cobertura reducida de abajo siguen vigentes sin cambio: `FINAL` no ejecutado ni
autorizado, `final.shared` y `final.desktop` sin refrescar, `INFRA_UNIT` completo no
ejecutado, y el fallo previo de `PreG9BCu2AuthorModelConsumerTest`. No se formula
`VERIFICATION_ESCALATION_REQUEST`.

### Candidato de reparaciones

Candidato técnico verificado: `ab37fb5fff0fadca13f4488eaff85d7917276c42`, árbol
`bec152ae807e3d0cbc8bf9e0abbe685ccdd09c17`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. Todas las ejecuciones desde la raíz, con PowerShell 7.6.10.

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-ab37fb5f` | 0 | `verification-4266398083c34bb1bcb870c5f8f73d77` | `ACCEPTED / COMPLETE` | 3 contratos; 6 diagnósticos |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-ab37fb5f` | 0 | `verification-8d64bf2053a24a559ac0f51c201a445d` | `ACCEPTED / COMPLETE` | 6 contratos, 5 productores; 209 identidades compartidas y 24 Desktop; 0 diagnósticos |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-ab37fb5f` | 0 | `verification-9627e2466f7f481eac71f9653990ca6b` | `ACCEPTED / COMPLETE` | 18 contratos, 15 productores; 9 diagnósticos |

Cada recibo está en `verification-result.json` de su directorio de log (ignorado por Git).
Diagnósticos distintos de `DIAGNOSTIC_CLEAR`, que no afectan a la aceptación:
`diagnostic.governance` = `DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` =
`DIAGNOSTIC_UNAVAILABLE`, las dos notas permanentes del verificador.

Otras comprobaciones:

- `git diff --check`: limpio en cada commit.
- `tools/agent/tests/verification-final-coverage.Tests.ps1`, ejecutada sola: 13 casos, 822
  aserciones, 0 fallos, con los dos recuentos actualizados (98 selecciones, 89 fases).
- Regresión Desktop adyacente ejecutada con Gradle directo, no perfil de aceptación: 17
  suites, 93 casos, 1 fallo, que es previo (véase abajo).

Cobertura reducida, declarada:

1. No se ejecutó `FINAL` ni las suites sin filtro: no está autorizado y la política vigente
   no lo exige. Las selecciones ejecutadas `final.shared` (7 867) y `final.desktop` (2 634)
   del inventario no se han refrescado y no incluyen las pruebas nuevas; refrescarlas exige
   una campaña exhaustiva.
2. `INFRA_UNIT` completo no se ejecutó: en esta estación (PowerShell 7.6.10) dos de sus
   suites fallan en cualquier commit por la identidad de error de `Test-Json`, condición
   previa y ajena a este trabajo.
3. **Fallo previo.** `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`
   falla en la base `9ccefdcfe` con el mismo mensaje que en la rama (para `e_2` espera una
   raíz `INTERIOR` en la costura y no se publica ninguna); comprobado en un checkout limpio
   de la base. No pertenece a CORE ni a la selección de la fase. No se ha triado ni tocado.
4. La latencia interactiva en una ventana real y el frontend web no se han medido ni
   construido.

Riesgo no cubierto por CORE + FOCUSED: ninguno que este trabajo haya identificado como
propio; no se formula `VERIFICATION_ESCALATION_REQUEST`. El punto 1 es una obligación de
mantenimiento del inventario para la próxima campaña exhaustiva.

## Identidad del candidato

| Papel | Commit | Árbol |
|---|---|---|
| Base | `9ccefdcfe922d8c0e329bdeec30c12fc058de463` | `18dbb9bc4216a536b5b2fc3d17321737001cdd36` |
| Candidato técnico `T` (reparaciones, modelo, paquete 3A, selección de fase) | `ab37fb5fff0fadca13f4488eaff85d7917276c42` | `bec152ae807e3d0cbc8bf9e0abbe685ccdd09c17` |
| Registro `D` de `T` | `96a4137e0`, descendiente exclusivamente documental de `T` | — |
| Candidato técnico de la continuación `T2` (D-V6, paquete normativo revisado, inventario) | `1838c721149bb6a4ad9088dff767cc64320cfb00` | `52329d83632de0c6ffc609400aa1dad31d19da7a` |
| Registro `D2` de `T2` | `9e2454206`, descendiente exclusivamente documental de `T2` | — |
| Candidato técnico de la enmienda focal y de F8 `T3` | `afb916e10ce551df06c26000b7ab0ce671c64e3b` | `b3a192b0ec48d9fab0067e7a2508f7ee89a2ae3f` |
| Registro `D3` de `T3` | `96d0479ed`, descendiente exclusivamente documental de `T3` | — |
| Candidato técnico de las precisiones focales y del trabajo preparatorio de I0 `T4` | `349c1856c6e42f7ddb14c7b24f064ca2d627dc01` | `7cc7637b31ad5260602569dc60461a16acbadb38` |
| Registro `D4` | el commit que contiene esta versión del fichero, descendiente exclusivamente documental de `T4` | — |

Commits de `T` sobre la base, en orden: `3881d7340` (A), `bc878779d` (F7), `d111a2e55` (F6),
`efed59bc8` (B), `417ab2c0f` (A′), `246f0bb2d` (B, copias aisladas), `49704eb7a` (modelo,
oráculo, pruebas Desktop y arnés), `2dd34f248` (escenario de evidencia), `79d56efd0`
(aclaraciones normativas, documento de reparaciones y paquete 3A) y `ab37fb5ff` (selección
de fase e inventario).

Commits de `T2` sobre `D`: `4cbe89a3e`, `266d0a85e`, `83cd56c4c` y `1838c7211`.

Commits de `T3` sobre `D2`: `57c495cce`, `58231d7d8`, `350b9d115` y `afb916e10`.

Commits de `T4` sobre `D3`: `9160d67d7`, `ee1518a1e` y `349c1856c`.

El paquete normativo sometido a aprobación definitiva es el de la revisión 4: ADR 0042
revisión 4, contrato `conditioned-use/v4-candidate`, diseño revisión 4 y registro de
decisiones revisión 4. Una aprobación debe citar `9160d67d7`, que es el commit del texto
(sin cambio posterior); `ee1518a1e` y `349c1856c` solo añaden el trabajo preparatorio de I0
y su inventario, que quedan como candidatos pendientes de esa aprobación. Las revisiones 1
(`ab37fb5ff`), 2 (`83cd56c4c`) y 3 (`57c495cce`) quedan sustituidas.

Ramas locales `maintenance/post-r1-locus-repairs-and-conditioned-use` (en `96a4137e0`) y
`maintenance/geocedg-1.2.2-post-r1` (bloque 3B completo en `0f02c8f21`), sin publicar. `main` local y `origin/main` siguen en
la base. Ninguna etiqueta creada ni movida; versión de producto sin cambiar (1.2.1).

## Pendiente, siguiente acción y reanudación

- **Pendiente del autor:** revisión de los incrementos I0 a I4T como producto (ninguno está
  aprobado: un resultado automatizado aceptado no es aprobación autoral); revisión de las
  reparaciones y del modelo (bloques 1 y 2) y de F8; los hallazgos anteriores; designación
  de la fecha de versión; autorización de publicación si procede.
- **No hecho:** candidato funcional 1.2.2 (versión, guías, empaquetado, humo de app-image y
  medición de rendimiento), que es el paso siguiente. Publicación en `main`, etiqueta
  `v1.2.2`, release y `FINAL`: no autorizados.
- **Reanudación exacta:** partir de `0f02c8f21` en `maintenance/geocedg-1.2.2-post-r1`,
  comprobar que `main` sigue en `9ccefdcfe`, y preparar el candidato 1.2.2 como se preparó
  1.2.1 (`packaging/windows/package.yml`, pin del contrato estático, pruebas de Desktop
  que afirman la versión, guías), con `STATIC` y `PACKAGING`.
- **Conservado:** el original del modelo, la evidencia R1 y las salidas de benchmark en
  `artifacts/benchmarks/post-r1-locus/` (ignoradas por Git). Dos worktrees separados, de la
  base y de `ab37fb5ff`, usados solo para medir el «antes», quedan en el scratchpad de la
  sesión.
