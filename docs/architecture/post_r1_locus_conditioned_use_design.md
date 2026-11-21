# GeoCeDG — Uso condicionado de Locus V2: diseño (bloque 3A, revisión 2)

**DISEÑO PROPUESTO, REVISIÓN 2 — TEXTO NORMATIVO PENDIENTE DE REVISIÓN AUTORAL. NADA DE V
ESTÁ IMPLEMENTADO.** `selfApproved=false`.
Decisión propuesta: [ADR 0042](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md).
Texto normativo candidato: [contrato candidato](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md).
Decisiones del autor y puntos abiertos: [registro de decisiones](../validation/post_r1_conditioned_use_author_decision_sheet.md).

La revisión 1 es el texto del commit `ab37fb5ff`. Esta revisión incorpora las decisiones
D-V1 a D-V7 del autor (2026-11-21) y sustituye las alternativas anteriores donde las
contradicen. Las diferencias constan en el ADR y en el §13 del contrato candidato. Este
documento no repite el contrato: explica cómo se llega a él desde el código actual, cómo se
implementaría, qué se ha medido y qué queda fuera.

## 1. Objetivo y frontera de autoridad

Presentar y usar una construcción paramétrica fiel con garantías declaradas inferiores a
la certificación rigurosa, en las mismas familias de operaciones que un locus certificado
(D-V5) y sin activación por parte del usuario (D-V1). El procedimiento, que es el evaluador
del slice, sigue siendo la autoridad; la malla es derivada. La certificación rigurosa no
cambia y sigue siendo directa cuando está disponible.

Nombre. El contrato usa **evidencia condicionada**, **tramo condicionado** (`SPAN`) y
**región condicionada**. No usa «componente», que en `locus-v2-existence-components.md`
§15.2 es un objeto certificado, ni «verificada», que atribuiría como hecho lo que son
hipótesis.

## 2. Inspección de capacidades actuales

| Pregunta | Hecho, con su fuente |
|---|---|
| ¿Qué devuelve una evaluación? | `LocusEvaluation2D`: estado, punto, regularidad, calidad y diagnóstico. No transporta token, germen, intervalo aislante ni separación por raíz |
| ¿Existe ese testimonio? | Sí, en la copia aislada, tras cada evaluación. Medido sobre `w_2` y `a_3` (escenario `evidence` del arnés): dos raíces con `DETERMINISTIC_SELECTION_ESTABLISHED`, parámetro canónico, intervalo aislante de anchura ≈ 7·10⁻¹³, aislamiento `ESTABLISHED` y garantía `ESTIMATED_ERROR`; la separación entre ambas va de 3,2 rad en el interior a 0,54 rad junto a cada pliegue. 2–4,5 ms por evaluación, sin resolución adicional |
| ¿Hay ya una naturaleza para esto? | `LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES`, nombrada en §15.1, sin productor ni contrato de consumo |
| ¿Qué exige hoy un punto? | `AlgoSemanticLocusPoint2D.containingComponent` (405-436): una componente certificada que contenga el parámetro; sin ella, `OUTSIDE_COMPONENTS`. Los códecs existentes persisten la clave de linaje de componente |
| ¿Qué dice la norma del punto aislado? | §15.8: «pointwise validity is admitted only for operations that need an isolated point» |
| ¿Dónde se restringe una intersección? | `LocusIntersectionSolver2D.allComponents()` (548-558) recorre solo componentes certificadas. Sin ninguna y sin `COMPLETE`: `SUCCESS`, `NOT_ESTABLISHED`, `UNRESOLVED`, cero soluciones, diagnóstico `COVERAGE_NOT_ESTABLISHED` (97-117). El solver de pares usa pares de componentes |
| ¿Cómo se transforma hoy un locus? | `AlgoLocusSimilarityTransform2D`: envuelve la definición de la fuente con la semejanza (`LocusSimilarityEvaluator2D`), conserva parámetro, dominio, ramas, orientación y linaje, crea identidad propia y **hereda** la estructura de existencia sin recalcularla (nota A2.5 de la norma de transformaciones). No exige `COMPLETE` ni componentes. La evidencia exploratoria se delega a la fuente. No transforma ninguna polilínea |
| ¿Qué hacen las métricas? | `LocusMetricEngine2D`: total condicionado H10 solo si la estructura no establece ningún hecho; `AlgoLocusBetweenMetricV2` (235-239) exige `COMPLETE`, una componente y certificación |
| ¿Y un locus dependiente de otro locus? | `AlgoDependentPointLocusV2.resolveDomain` (799-804): las piezas del dominio se toman del soporte solo si es `COMPLETE`; `upstreamLimitation` devuelve `upstream-locus-coverage-not-established` y el certificado es `NOT_ESTABLISHED` |
| ¿Y la exportación semántica? | `SemanticCurveExportAdapter2D` (385-434): sin componentes emite `EXISTENCE_NOT_ESTABLISHED` con las bandas no resueltas y no exporta nada; no bloquea el resto |
| ¿Hay marcas del sistema en la vista? | Sí: la «pista de orientación» de rectas, segmentos y semirrectas (`GeoCeDGOrientationCue`, `GeoCeDGOrientationCueRenderer`, pintada en `GeoCeDGEuclidianView.paint`). Es una superposición Desktop en píxeles de dispositivo, de tamaño fijo (10 px), con el color del objeto, sin área de selección, no serializada, que solo aparece en la pintura viva y se gobierna con una preferencia global desactivada por defecto. Los locus están excluidos. `DrawLocusV2` no dibuja ninguna marca propia: las marcas de `LocusRenderData2D.MarkKind` son solo datos |
| ¿Dónde se lee hoy el estado de un locus? | Solo en el inspector de definición (`GeoCeDGDefinitionInspector.appendLocusDetails`, 165-205), como texto en inglés sin localizar con el nombre crudo del enumerado (`existence completeness: NOT_ESTABLISHED`). La vista algebraica muestra `LocusV2[experimental, revision=N]` |
| ¿Cuánto cuesta recorrer el dominio? | R1, recorrido externo sin el protocolo de este diseño ni pintura: `w_2` 209 evaluaciones / 663 ms; `a_3` 223 / 720 ms; `trHole` 138 / 284 ms |
| ¿Cada cuánto habría que repetirlo? | Una vez por revisión. Tras A y A′ mover `E_1` o `N` no publica revisiones; tras D-V6 una revisión por cambio real cuesta ≈ 0,3 s por locus del agujero |

## 3. Cómo resuelve el diseño cada obligación

### 3.1 Hipótesis, clases y admisión

Contrato §3 y §4. Se **comprueba** en los nodos: validez, selección establecida de cada
dependencia selectora, criterios geométricos entre nodos y separación de la raíz
seleccionada. Se **supone**, con ese nombre: H1 validez, H2 selección, H3 continuidad
(incluidos los cortes de operaciones con rama) y H4 resolución.

`MARGIN_BOUNDED` queda corregida. Una cota rigurosa de la separación entre raíces prueba una
sola cosa: que esas raíces no colisionan en la celda y, por tanto, que la seleccionada no se
intercambia con otra de *esas*. No prueba que las demás operaciones del slice sean válidas
(H1), ni que no aparezcan o desaparezcan raíces fuera del recinto, ni la selección de las
demás dependencias (H2), ni la continuidad de la función compuesta (H3). La tabla del
contrato §3.3 dice, por clase, qué queda demostrado, qué solo comprobado en nodos, qué sigue
siendo hipótesis y quién puede consumirla. Las tres clases dan los **mismos permisos**: la
clase informa del diagnóstico y orienta la promoción; nunca concede más uso.

### 3.2 Testimonio por dependencia

API interna propuesta, en el kernel:

```text
ReconstructibleLocusEvaluator2D
  evaluateWithTestimony(definition, branch, t, session) -> LocusConditionedSample2D
LocusConditionedSample2D            (inmutable; versión locus-evaluation-testimony/v1)
  evaluation : LocusEvaluation2D
  testimony  : List<LocusSelectionTestimony2D>
LocusSelectionTestimony2D
  operationIdentity   identidad durable de la operación selectora en el slice
  kind                LOCUS_ROOT_TOKEN | LINE_CONIC_SELECTION | STRUCTURAL_POINT
  established         selección determinista establecida o no
  selectionKey        token exacto o estado de selección asentado (nunca índice)
  parameter, isolatingInterval, localIsolation, numericGuarantee
  separation          distancia a la raíz más próxima del mismo resultado, o ausente
```

Propiedad y ámbito: el evaluador, sobre su copia aislada; lee la evidencia que el slice
calculó para esa evaluación (hoy se descarta) y no inicia ninguna resolución. La falta de
separación degrada la clase; la falta de selección establecida produce `SELECTION_BAND`.

### 3.3 Ausencia, fallo y totales

Contrato §5. Un fallo es `FAILURE_BAND`, nunca ausencia, y su interior no se subdivide: la
sonda muestra que sin esa regla el trabajo crece sin producir evidencia. La ausencia
condicionada exige causa geométrica elegible en todos los nodos. En los totales ninguna
banda se omite por estrecha: contribución, cota o `SUBTOTAL`.

### 3.4 Direcciones, identidad y persistencia (D-V2)

Dirección por parámetro sin selector de componente, en el códec propio
`geocedg-locus-parameter-address/v1`. Los códecs existentes no cambian de texto ni de
significado, ninguna dirección se reinterpreta y ninguna migración inventa identidades o
asociaciones. Una muestra, un índice de polilínea o una posición de pantalla nunca son
dirección.

La compatibilidad de lectura con GeoCeDG 1.2.1 no se exige y la prueba con lectores
antiguos de la revisión 1 se retira. En su lugar el contrato §6.4 fija las obligaciones de
1.2.2, cada una con su prueba en I1: identidad durable, guardar y reabrir, copiar y
duplicar con remapeo, deshacer y rehacer, redefinición compatible e incompatible, selección
y continuación, degeneraciones, recuperación tras invalidez temporal y lectura sin cambios
de las construcciones de regresión.

El punto paramétrico no necesita evidencia intervalar para estar definido: su contrato es
validez puntual, dirección inequívoca y procedencia (§15.8). La evidencia intervalar decide
qué continuación se ofrece y con qué grado se presenta.

### 3.5 Ciclo de vida determinista

Contrato §7. Tipo propuesto `LocusConditionedEvidence2D`, inmutable, propiedad del productor
del locus, identificado por `(revisión, versión de política, generación)`.

- **Determinismo.** El contenido es función de la función trazada, la política y el
  *conjunto de contradicciones* confirmadas de la revisión, en orden canónico de dirección
  diádica. Dos historias que confirman las mismas celdas llegan a la misma evidencia. La
  sonda (caso C9) da celdas idénticas con tres órdenes de consulta.
- **Contradicción.** Quien la observa solo la *comunica*. El productor reevalúa el parámetro
  con su propio evaluador y, si la confirma, añade la celda al conjunto, la refina y publica
  la generación siguiente. El renderer no decide ninguna verdad geométrica.
- **Retirada e invalidación.** Una generación retira exactamente los tramos que su
  refinamiento sustituye. La publicación es una actualización del productor en el DAG: se
  recalcula el dependiente que usó una celda retirada, y solo ese.
- **Identidad de los puntos afectados.** Conservan dirección e identidad: siguen definidos
  si su propia evaluación sigue siendo válida, y quedan indefinidos con causa tipada si no.
- **Sin evidencia obsoleta ni promoción por densidad.** Una revisión nueva descarta la
  evidencia y el conjunto de contradicciones anteriores.

### 3.6 Operaciones (D-V5)

El contrato §8 tiene un contrato por operación con entradas y dominios, precondiciones,
evidencia requerida, garantía, incidencia, identidad y continuación, propagación,
degeneraciones, persistencia y rechazos. Extensión mínima que necesita cada operación sobre
el código actual:

| Operación | Dónde está hoy la puerta | Extensión mínima |
|---|---|---|
| `Point(L, u)`, `Point(L, "rama", u)` | `containingComponent` | resolver por dirección por parámetro cuando ninguna componente contiene `u`; nuevo códec; causa tipada en bandas |
| Arrastre y seguimiento | continuaciones locales, que exigen `certifies()` | continuación a lo largo de una región condicionada; parada en banda |
| Longitud entre posiciones | `AlgoLocusBetweenMetricV2` exige `COMPLETE` y certificación | ruta dentro de una región; estimador v2 existente; resultado `ESTIMATED` con hipótesis |
| Longitud total | `LocusMetricEngine2D.declaredPieces` | recorrer tramos y bandas con la política de §5.3; H10 y P2 intactos |
| `Intersect(L, objetivo)` | `allComponents()` del solver | añadir las regiones condicionadas al dominio de búsqueda; admisión por raíz; completitud `NOT_ESTABLISHED`; nunca `EMPTY` |
| `Intersect(L1, L2)` | pares de componentes | pares de regiones o componentes |
| `Translate`, `Rotate`, `Mirror`, `Dilate` | ninguna puerta; hereda la estructura | heredar también la evidencia condicionada, celda a celda; H4 escalada por la razón; razón cero como imagen colapsada |
| Locus dependiente de puntos, raíces o métricas condicionadas | su propio certificado | su propia evidencia condicionada, con las hipótesis heredadas |
| Locus conducido por un punto sobre un locus condicionado | `resolveDomain` exige soporte `COMPLETE` | piezas del dominio desde las regiones condicionadas del soporte |
| Selección y manejo | ninguna | ninguna; el clic crea el punto por la consulta inversa existente y lo direcciona por parámetro |
| Exportación semántica | `existenceNotEstablished` | sin cambio de comportamiento en 1.2.2: omisión diagnosticada con la lista de regiones |

Las transformaciones no necesitan nueva semántica: la imagen ya es `T ∘ L` sobre el
evaluador de la fuente. Lo único que falta es que la evidencia viaje con la estructura.
Transformar la polilínea de dibujo no forma parte de ninguna vía, ni actual ni propuesta.

Ejemplos sobre el modelo (variante con `trHole`):

| Operación | Hoy | Con V |
|---|---|---|
| `Point(trHole, 0.5)` | rechazado, `OUTSIDE_COMPONENTS` | definido; `POINT_VALID`; dirección por parámetro |
| Arrastre de ese punto | no disponible | a lo largo de la región; se detiene en una banda |
| `LocusLength(trHole, A, B)` | no disponible | `ESTIMATED`, con H1–H4 |
| `Intersect(trHole, recta)` | cero soluciones | raíces locales `VERIFIED_UNCERTIFIED`; completitud `NOT_ESTABLISHED` |
| `Rotate(trHole, 30°, S_1)` | imagen sin componentes ni consumidores | imagen con la evidencia de la fuente y los mismos consumidores |
| `LocusV2(f(Point(trHole, u)), u)` | sin evaluaciones útiles | su propia evidencia condicionada |
| Consumidor que exige componente certificada o `STRICT` | rechaza | rechaza igual |

V no corrige el procedimiento original del modelo: dibujaría completos los dos arcos de
`w_2` y `a_3`, con su imagen conjugada. La corrección es la variante transportada.

### 3.7 Presentación (D-V3)

- El estilo del locus no se toca. Se retira la raya larga obligatoria de la revisión 1.
- **Estado en las propiedades**, obligatorio: texto localizado, por ejemplo «Condicionado —
  existencia/continuidad no certificadas», con las hipótesis H1–H4, la versión de política,
  el número de regiones y bandas por tipo y la generación, en el inspector de definición y
  en la descripción del objeto. Hoy ese estado es un enumerado crudo sin localizar; este
  punto lo sustituye.
- **Marca del sistema**, viable con intervención acotada. La infraestructura de la pista de
  orientación ya resuelve lo difícil: colocación en píxeles, recorte a la vista, cuerda
  mínima visible, trazo de 1 px con el color del objeto, ausencia de área de selección y
  punto único de pintura en `GeoCeDGEuclidianView.paint`. La marca condicionada reutiliza ese
  patrón con tres diferencias: se ancla en el vértice medio del trazo visible de cada región
  (dato que `LocusRenderData2D` ya tiene), su glifo no es un triángulo ni tiene dirección, y
  no depende de la preferencia de la pista de orientación. Coste: un glifo por región
  visible. No toca `DrawLocusV2`, la huella, la geometría ni la evidencia.
- Glifo propuesto: un rombo hueco de 7 px, relleno con el fondo de la vista. No es una
  flecha y no sugiere sentido.
- Alternativa de reserva del contrato §9.5: si la marca resultara frágil, basta el estado en
  las propiedades.

### 3.8 Exportación (D-V4)

Las exportaciones de imagen exportan lo dibujado, con la marca del sistema donde el
exportador pueda dibujarla (punto P2 del registro de decisiones). Las semánticas omiten las regiones
condicionadas en 1.2.2 con el diagnóstico que el adaptador ya produce. La exportación
semántica queda vinculada a la línea futura de B-splines y representaciones derivadas,
importación DXF y extensión de la exportación DXF.

Ubicación en el roadmap, sin inventar fase: no existe hoy ninguna que cubra ese conjunto.
`G9X1` (DXF extendido) está cerrada y difiere la entidad `SPLINE`; la nota de `PRE-G9B-S1`
(roadmap, líneas 3150-3159) dice que un contrato nuevo de fidelidad o una entidad DXF
`SPLINE` «requeriría una subfase separada»; `G15` deja expresamente DXF en `G5`; no hay
fase de importación DXF. La dependencia se propone, por tanto, como requisito de esa
subfase separada de la línea `G9X`, a abrir por el autor, y queda anotada en el registro de
decisiones. Este trabajo no la añade al roadmap.

### 3.9 Rendimiento

Clave `(revisión, versión de política)`; presupuesto de evaluaciones explícito; cancelación
al publicarse otra revisión; memoria del orden de 10⁴ bytes por locus. La optimización de
los cambios reales es el trabajo D-V6, separado de V:
[documento de rendimiento](post_r1_locus_real_change_performance.md).

## 4. Semántica generada y promoción rigurosa

Sin cambios respecto a la revisión 1. El análisis y la exploración proponen; el kernel
valida por revisión; no hay consultas a un asistente durante el arrastre. Una malla de
`Z(t)` no es un tubo certificado del parámetro de raíz `u(t)`.

**K2**, relevante para el transporte: `P(θ) = L(τ(θ))`, `ℓ(θ) = Λ_L(τ(θ))`. Necesita un
recinto intervalar de `τ` por celda (con levantamiento del `atan2`), la preimagen por `τ`
de componentes y eventos de `L`, el recinto de `Λ_L` como función y la costura. Longitud de
posición frente a longitud recorrida, que contiene `|τ′|`; retrocesos, `τ` constante,
salidas del dominio, varias vueltas y límites laterales. **K1**: familias inversas; el
pliegue exige `h = 0` y `h_u = 0` simultáneos. Ambos son capacidades posteriores
independientes, con su propia aprobación, salvo que una necesidad de corrección obligue a
elevar una propuesta puntual.

## 5. Plan de incrementos 3B

Solo tras aprobar el texto normativo revisado. Cada incremento cierra sus pruebas y
demuestra ausencia de regresiones. Todos, de I0 a I4T, son necesarios para declarar
concluida la capacidad en 1.2.2.

| Inc. | Contenido | Depende de | Criterio de aceptación |
|---|---|---|---|
| I0 | Testimonio de evaluación y `LocusConditionedEvidence2D` en el kernel, sin cambiar operaciones públicas; política v1; conjunto de contradicciones; contadores | — | banco §7 con el protocolo real; determinismo frente al orden; presupuesto; coste por revisión con pintura; ningún consumidor cambia |
| I1 | Dirección por parámetro y su códec; puntos sobre regiones condicionadas | I0 | las nueve obligaciones del contrato §6.4 |
| I2 | Estado en las propiedades y marca del sistema | I0 | estilo intacto; marca independiente de zoom, grosor, color y tipo de línea; ninguna unión a través de una banda |
| I3 | Longitud entre posiciones y total, con subtotales y bandas | I1 | H10 y P2 intactos; ninguna banda omitida |
| I4 | Intersecciones locales, de objetivo y de pares; locus dependientes y anidados; triaje y corrección de F8 | I1 | nunca `EMPTY` ni completitud; raíz en banda no publicada; una raíz local admisible alimenta construcciones |
| I4T | Herencia de la evidencia por las transformaciones; manejo geométrico; adaptadores de interfaz separados del kernel | I0, I4 | la imagen admite los mismos consumidores; razón cero tratada; ninguna polilínea como fuente |

Fuera de 1.2.2: exportación semántica de regiones condicionadas; K2 y K1.

**F8, defecto previo que entra en I4.** Con un locus consumidor cuyo conductor es entrada no
conductora de un locus anidado (composición `L_t`), el locus anidado vivo pierde su
definición en ciertas posiciones: su reconstrucción falla con «Reconstructed evaluator
omitted durable dependency» sobre su propio conductor. Es dependiente de la historia de
actualizaciones y se reproduce igual en la base `9ccefdcfe`. Reproductor en el
[documento de rendimiento](post_r1_locus_real_change_performance.md#4-hallazgo-f8). Sin
triar más allá de eso.

## 6. Riesgos

1. **Falso tramo condicionado.** Existe y se declara; solo la promoción lo elimina.
2. **Deriva hacia certificación por densidad.** Bloqueada por contrato y por tipo.
3. **Superficie de operaciones.** Mayor que en la revisión 1; cada operación tiene su
   contrato y su prueba, y las puertas de los consumidores estrictos no se tocan.
4. **Coste por revisión.** Medir en I0; presupuesto con `UNEXPLORED` si excede.
5. **F8.** Puede condicionar las composiciones anidadas de I4; se trata allí.

## 7. Banco adversarial

Se conserva íntegro de la revisión 1 y forma parte de la aceptación de I0. Para cada caso:
hecho que debe detectarse por contrato, hipótesis no demostrada, resultado admitido y fallo
esperado. Superar el banco no certifica nada. Resultados de la sonda externa
`tools/benchmark/post-r1-locus/conditioned-use-probe.js` (evaluadores sintéticos de verdad
conocida; 32 celdas iniciales, flecha 10⁻³, giro 0,35 rad, paso mínimo 2⁻²⁰); no es código
de producto ni prototipo del kernel.

| Caso | Hecho a detectar por contrato | Hipótesis no demostrada | Resultado admitido | Fallo esperado | Sonda |
|---|---|---|---|---|---|
| C1. Salto válido en un corte angular | el salto queda aislado al paso mínimo | H3 fuera de la banda | `DISCONTINUITY_BAND`; los tramos no se unen | — | detectado; 95 evaluaciones |
| C2. Dos eventos de validez en una celda | solo si un nodo cae dentro | H1 | banda si se detecta; tramo bajo H1 si no | falso tramo | hueco 10⁻²: detectado; 10⁻⁴ y 10⁻⁷: no |
| C3. Dos raíces intercambian y regresan | solo si un nodo cae dentro | H2 | `SELECTION_BAND` o tramo bajo H2 | falso tramo, con márgenes grandes en los nodos | no detectado |
| C4. El margen se anula entre nodos | ninguno con muestras finitas | H2; el margen observado no es cota | `MARGIN_ESTIMATED`, nunca `MARGIN_BOUNDED` | clase sobreestimada | aceptado como estimado |
| C5. Fallo de evaluación con existencia | el fallo no es ausencia | — | `FAILURE_BAND`, sin subdividir su interior | — | detectado; 288 evaluaciones |
| C6. Transición estrecha con contribución métrica | la banda no se omite de un total | H4 | contribución, cota o `SUBTOTAL` | total erróneo si se omite | no detectada por cuerdas: 1,0 frente a 11 |
| C7. Raíz local con cobertura no establecida | la raíz se publica local | cobertura | raíz `VERIFIED_UNCERTIFIED`; completitud `NOT_ESTABLISHED` | declarar `EMPTY` o completo | por contrato §8.5 |
| C8. Selección ambigua, tangencia, auto-intersección, varias preimágenes | la dirección por parámetro es inequívoca; la inversa no | — | punto por parámetro definido; asignación por coincidencia rechazada | asignar preimagen por proximidad | por contrato §6 |
| C9. Retirada, promoción y órdenes de consulta | misma evidencia con cualquier orden | — | celdas y clases idénticas | dependencia del orden | idénticas con tres órdenes |
| C10. Guardar y reabrir antes y después de refinar | la dirección no depende de la evidencia | — | misma dirección; evidencia recalculada | persistir la malla | por contrato §6, §7 |
| C11. Modelo del cono | arcos de `w_2`/`a_3` y contorno transportado | H1–H4 | regiones con bandas de transición en los pliegues | unir los dos arcos | testimonio medido (§2) |
| C12. Cota de separación sin validez | una cota de raíces no prueba H1 ni H3 | H1, H3 | `MARGIN_BOUNDED` con los mismos permisos que `RESOLUTION_ONLY` | tratarla como certificado | por contrato §3.3; caso a añadir a la sonda en I0 |
| C13. Imagen por semejanza de razón cero | la imagen es un punto | — | tramos colapsados; sin longitud ni intersección de arco | tratar el punto como arco | por contrato §8.7 |
