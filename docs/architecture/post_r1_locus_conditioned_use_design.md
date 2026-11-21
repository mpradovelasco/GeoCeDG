# GeoCeDG — Uso condicionado de Locus V2: diseño (bloque 3A posterior a R1)

**DISEÑO PROPUESTO — PENDIENTE DE REVISIÓN AUTORAL. NADA DE V ESTÁ IMPLEMENTADO.**
`selfApproved=false`. Decisión propuesta: [ADR 0042](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md).
Texto normativo candidato: [contrato candidato](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md).
Decisiones que corresponden al autor: [hoja de decisiones](../validation/post_r1_conditioned_use_author_decision_sheet.md).

Este documento no repite el contrato candidato: explica cómo se llega a él desde el código
actual, cómo se implementaría, qué se ha medido y qué queda fuera.

## 1. Objetivo y frontera de autoridad

Presentar y usar una construcción paramétrica fiel con garantías declaradas inferiores a
la certificación rigurosa. El procedimiento (el evaluador del slice) sigue siendo la
autoridad; la malla es derivada. La certificación rigurosa no cambia y sigue siendo
directa cuando está disponible.

Nombre. «Componente verificada» atribuye dos propiedades que son hipótesis: que la región
es una componente (con linaje y continuidad establecidos) y que está verificada. El
contrato usa **evidencia condicionada**, **tramo condicionado** (`SPAN`) y **región
condicionada**: dicen lo que hay, evidencia bajo hipótesis declaradas, y no reutilizan
«componente», que en `locus-v2-existence-components.md` §15.2 es un objeto certificado.

## 2. Inspección de capacidades actuales

| Pregunta | Hecho, con su fuente |
|---|---|
| ¿Qué devuelve una evaluación? | `LocusEvaluation2D`: estado, punto, regularidad, calidad y diagnóstico. No transporta token, germen, intervalo aislante ni separación por raíz |
| ¿Existe ese testimonio? | Sí, en la copia aislada, tras cada evaluación. Medido sobre `w_2` y `a_3` (escenario `evidence` del arnés): el resultado rico que sigue al conductor publica dos raíces con `DETERMINISTIC_SELECTION_ESTABLISHED`, parámetro canónico, intervalo aislante de anchura ≈ 7·10⁻¹³, aislamiento `ESTABLISHED` y garantía `ESTIMATED_ERROR`; la separación entre ambas va de 3,2 rad en el interior a 0,54 rad junto a cada pliegue. Coste observado: 2–4,5 ms por evaluación, sin resolución adicional |
| ¿Hay ya una naturaleza para esto? | `LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES`, nombrada en §15.1 («an estimate under its own contract»), sin productor ni contrato de consumo |
| ¿Qué exige hoy un punto? | `AlgoSemanticLocusPoint2D.containingComponent`: una componente certificada que contenga el parámetro; sin ella, `OUTSIDE_COMPONENTS`. Los códecs `geocedg-locus-address/v1` y `geocedg-locus-principal-selection/v1` persisten la clave de linaje de componente |
| ¿Qué dice la norma del punto aislado? | §15.8: «pointwise validity is admitted only for operations that need an isolated point». El contrato vigente ya distingue el punto aislado de las operaciones que necesitan continuidad o ruta |
| ¿Qué precedentes hay de grados inferiores? | Longitud total condicionada H10 y P2 (`locus-v2-metrics.md` §26.5, §26.6); intersecciones `VERIFIED_UNCERTIFIED`; integridad `RELAXED` (`locus-v2-constructive-authority.md` §7.3). Son precedentes de forma (hipótesis registrada, sin autoridad, consumo declarado), no equivalencias: `CONDITIONED` no se convierte en `RELAXED` ni en `STRICT`, y la autoridad del documento no cambia |
| ¿Cómo se dibuja hoy lo no certificado? | `locus-v2-presentation.md`: evidencia exploratoria con estilo distinto, solo dentro de bandas no resueltas, nunca unida ni exportada. En el modelo: 32 muestras uniformes en trazo punteado |
| ¿Cuánto cuesta recorrer el dominio? | R1, recorrido externo, sin el protocolo de este diseño ni pintura: `w_2` 209 evaluaciones / 663 ms; `a_3` 223 / 720 ms; `trHole` 138 / 284 ms |
| ¿Cada cuánto habría que repetirlo? | Una vez por revisión. Tras las reparaciones A y A′ mover `E_1` o `N` no publica revisiones de estos loci (0 de 21 pasos, antes 21 de 21) |

## 3. Obligaciones del encargo y cómo las resuelve el diseño

### 3.1 A — hipótesis y admisión

Contrato candidato §3 y §4. Se **comprueba**: validez y selección establecida en cada nodo,
incluido el punto medio de cada celda; criterios geométricos (flecha y giro) entre nodos;
separación de la raíz seleccionada en cada nodo. Se **supone**, y se registra con ese
nombre: H1 validez, H2 selección, H3 continuidad (incluidos los cortes de las operaciones
con rama, como `atan2`), H4 resolución.

Tres clases de evidencia de un tramo, que no se confunden: `MARGIN_BOUNDED` (cota rigurosa
de la separación en toda la celda), `MARGIN_ESTIMATED` (separación observada en los nodos,
sin cota entre ellos) y `RESOLUTION_ONLY`. La sonda externa confirma lo que el contrato
obliga a declarar (§7 de este documento): un margen que se anula entre nodos con
pendientes observadas pequeñas pasa por `MARGIN_ESTIMATED`; dos eventos dentro de una celda
y un intercambio de raíces que regresa no se detectan si ningún nodo cae dentro, con
independencia del paso mínimo, porque el paso mínimo solo se alcanza donde algo disparó el
refinamiento. No se promete detección universal.

### 3.2 B — evidencia por dependencia

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

Propiedad y ámbito: el evaluador, sobre su copia aislada; el testimonio se lee de la
evidencia que el slice calculó para esa evaluación (hoy se descarta) y no inicia ninguna
resolución. Coste: lectura. La falta de separación degrada la clase del tramo; la falta de
selección establecida produce `SELECTION_BAND`, nunca un tramo `RESOLUTION_ONLY`. Ninguna
raíz se materializa por índice, proximidad, orden de coordenadas o coincidencia visual.

### 3.3 C — no existencia, fallo y totalización

Contrato candidato §5. Un fallo computacional, de presupuesto, de autoridad o de selección
es `FAILURE_BAND`, nunca ausencia; la sonda muestra además que el interior de una banda de
fallo no debe subdividirse (solo su frontera): sin esa regla el trabajo crece sin producir
evidencia. La ausencia condicionada exige causa geométrica elegible en todos los nodos de
la celda y es de la misma naturaleza que un tramo; no es `CERTIFIED_DOES_NOT_EXIST`.

Totales: toda celda del dominio pedido se contabiliza. Una banda estrecha no desaparece:
contribución a través de la banda si hay límites laterales y la política la admite, cota
explícita si existe, y en otro caso `SUBTOTAL` con las bandas enumeradas o rechazo. La
sonda (caso C6) muestra una transición de anchura 10⁻⁵ con contribución métrica 10 sobre
un total de 11 que una suma de cuerdas omite por completo. La concordancia entre dos
aproximaciones no es cota de error.

### 3.4 D — direcciones, identidad y persistencia

Los puntos actuales conservan linaje de componente; permitir `Point(L, u)` sobre una región
sin componente exige otra dirección. Propuesta (contrato §6): una **dirección por
parámetro** sin selector de componente, en un códec nuevo
`geocedg-locus-parameter-address/v1`. Los dos códecs existentes no cambian de texto ni de
significado. Una dirección con selector nunca se reinterpreta como dirección por parámetro
ni al revés; convertirla es una redefinición explícita del punto.

Qué evidencia intervalar necesita el punto paramétrico: **ninguna para estar definido**.
Su contrato es validez puntual, dirección inequívoca y procedencia, que es lo que §15.8 ya
admite para una operación que necesita un punto aislado. La evidencia intervalar decide
otra cosa: qué continuación se ofrece (arrastre, seguimiento) y con qué grado se presenta.

| Evento | Efecto sobre la dirección | Efecto sobre el punto |
|---|---|---|
| Refinamiento, división o unión de tramos | ninguno | ninguno; cambia la evidencia informada |
| Retirada de evidencia (contradicción) | ninguno | indefinido solo si su propia evaluación deja de ser válida o de tener selección establecida |
| Promoción a componente certificada | ninguno | definido; se informa la componente; no adquiere selector de componente |
| Copia, redefinición, deshacer/rehacer | reglas de los códecs existentes | identidad según esas reglas |
| Reapertura | se lee la dirección; la evidencia se recalcula | definido si evalúa |
| Lector anterior (≤ 1.2.1) | no reconoce el códec | punto indefinido con la causa tipada de dirección inválida; el resto del documento se lee |

**No se presupone «sin cambio de formato».** Es un cambio de formato aditivo con un límite
de compatibilidad hacia atrás declarado (decisión D-V2). La prueba de lectores forma parte
del primer incremento: documento con dirección por parámetro leído por la app-image 1.2.1
publicada (receta de observación aislada ya usada en R1), y documentos 1.2.1 leídos por el
candidato sin cambio de resultado.

### 3.5 E — ciclo de vida de la evidencia

Contrato candidato §7.

- Propietario: el productor del locus (`AlgoDependentPointLocusV2`), junto a su certificado
  de existencia y con su mismo ámbito de revisión. Tipo propuesto
  `LocusConditionedEvidence2D`, inmutable, identificado por
  `(revisión, versión de política, generación)`.
- Publicación atómica; una revisión nueva descarta la evidencia anterior y cancela el
  trabajo en curso.
- Invalidación de consumidores: por el grafo normal, y solo si la generación nueva cambia
  las celdas que su resultado usó.
- Contradicción: quien la observa la comunica al productor; el productor publica una
  generación con el tramo dividido. El renderer no modifica la admisibilidad.
- Promoción: por el certificador; no cambia identidad ni función.
- Reproducibilidad: malla diádica canónica; mismas celdas y clases con cualquier orden de
  consultas (sonda, caso C9: idénticas con tres órdenes).
- Progresividad: lo que un consumidor puede leer es la generación publicada, igual haya
  dibujado lo que haya dibujado la vista. Durante un arrastre que sí cambia la función, la
  evidencia de la revisión nueva se calcula hasta el límite de trabajo; lo no alcanzado es
  `UNEXPLORED`, explícito.

### 3.6 F — contrato por consumidor

Matriz completa en el contrato candidato §8, por consumidor y autoridad documental, con
premisas, grado publicado, rechazos, identidad y persistencia. Regla de propagación: por
premisas usadas, no por el mínimo de una escala. Ejemplos de comportamiento sobre el modelo
(configuración guardada, variante con `trHole`):

| Operación | Hoy | Con V |
|---|---|---|
| `Point(trHole, 0.5)` | rechazado, `OUTSIDE_COMPONENTS` | definido; existencia `POINT_VALID`, tramo `RESOLUTION_ONLY` informado; dirección por parámetro |
| Arrastre de ese punto | no disponible | continuación a lo largo de la región; se detiene en una banda con su causa |
| `LocusLength(trHole, A, B)` con A y B en la región | no disponible | `ESTIMATED`, con H1–H4 y las hipótesis del estimador |
| `LocusLength(trHole)` | estimación condicionada H10 (5,1 s en R1) | igual o `ESTIMATED` condicionada si todo el dominio son tramos; H10 no cambia |
| `Intersect(trHole, recta)` | cero soluciones | raíces locales `VERIFIED_UNCERTIFIED` en tramos; completitud `NOT_ESTABLISHED`; nunca `EMPTY` |
| `LocusV2(f(Point(trHole, u)), u)` | sin evaluaciones útiles | su propia evidencia condicionada, con las hipótesis heredadas registradas |
| `Point(w_2, 2.3)` (procedimiento original) | rechazado | definido en el arco donde la raíz seleccionada está establecida; entre los dos arcos, `TRANSITION_BAND` |
| Consumidor que exige componente certificada o `STRICT` | rechaza | rechaza igual |

V no corrige el modelo original: dibujaría completos los dos arcos de `w_2` y `a_3`,
incluida la imagen conjugada, porque esa imagen es geometría del procedimiento guardado. La
corrección es la variante transportada del bloque 2.

### 3.7 G — presentación y exportación

Contrato candidato §9 y §10. Trazo condicionado continuo, distinto del certificado y de
las marcas exploratorias; bandas no unidas; el texto de estado dice «condicionado, no
certificado» y ofrece las hipótesis; «completo» se reserva a `COMPLETE`. Convención
visual concreta: decisión D-V3. Exportación: las exportaciones de imagen de la vista
exportan lo dibujado; DXF y LaTeX semántico solo si pueden transportar clase de fidelidad,
versión de política y advertencia, y en otro caso omisión diagnosticada (decisión D-V4).
La evidencia exploratoria vigente sigue sin exportarse.

### 3.8 H — rendimiento

- Clave: `(revisión, versión de política)`. Con A y A′ la revisión no cambia al mover
  conductores; no se recalcula una malla por un movimiento que no cambia la función.
- Presupuesto: evaluaciones por revisión, explícito en la política. Referencia: 138–223
  evaluaciones a 2–4,5 ms. Reconstruir un locus tras un cambio real cuesta hoy ≈ 0,8 s por
  locus del agujero (benchmark del bloque 1); la evidencia condicionada añadiría del orden
  de 0,3–0,7 s por revisión, que el incremento I0 debe medir con el protocolo real y con
  pintura.
- Cancelación: al publicarse una revisión nueva. Memoria: nodos × (parámetro, punto,
  testimonio), del orden de 10⁴ bytes por locus; se libera con la revisión.
- Reutilización: la teselación de pintura reutiliza los nodos como semillas.
- Las piezas C (detección de cambios sin XML) y D (compartir datos inmutables) de R1 no se
  proponen: tras A, B y A′ el coste por paso de conductor es ≈ 60 ms y no las justifica.
  Compartir copias mutables (E) no está autorizado y no se necesita.

## 4. Semántica generada y promoción rigurosa

El análisis y la exploración proponen (correspondencias, particiones, semillas, planes de
prueba); el kernel valida las premisas por revisión; no hay consultas a un asistente
durante el arrastre. Una malla de `Z(t)` no es un tubo certificado del parámetro de raíz
`u(t)`: propone celdas candidatas que el verificador puede rechazar o subdividir. Los
planes son reconstruibles desde la definición y no crean asociaciones durables.

**K2, relevante para el transporte.** `P(θ) = L(τ(θ))` y `ℓ(θ) = Λ_L(τ(θ))`.

- Para este modelo: `L = e_2` (certificable hoy por su propia vía), `τ(θ)` es el ángulo
  polar del pie `R(θ)`, intersección de una semirrecta con la circunferencia base, y el
  ángulo desarrollado es `Λ_L(τ(θ)) / ρ`.
- Hace falta: un recinto intervalar de `τ` sobre cada celda (el programa de `R` es de clase
  constructiva; el `atan2` necesita levantamiento con control del corte); la preimagen por
  `τ` de las componentes y eventos de `L`; el recinto de `Λ_L` como función (existe la
  estructura de función métrica con evidencia de caja); y la costura `ORIGIN_WRAP`.
- Distinciones obligadas: longitud de posición `Λ_L∘τ` frente a longitud recorrida
  `∫|τ′|·‖L′‖`; retrocesos y tramos con `τ` constante; salidas del dominio de `L`; varias
  vueltas; límites laterales. El recinto de la integral ideal no es el recinto del valor
  numérico estimado nativo.
- Patrón general: composición de un locus certificado con una reparametrización escalar
  encerrada. Fuera de clase: `τ` no encerrable, `L` no certificado.

**K1, familias inversas.** No bloquea el transporte. Si se diseña: familia `u(t)` con
`h(t, u) = 0`; pliegue con `h = 0` y `h_u = 0` simultáneos y no degeneración; el recuento
en una caja exige controlar sus fronteras; un levantamiento angular sobre una componente
cerrada con giro no nulo necesita corte o recubrimiento. Empezar donde la estructura
separable lo justifique.

Ninguno se implementa por extensión. Cada uno es un incremento propio con su aprobación.

## 5. Plan de incrementos 3B

Solo tras aprobar el paquete. Cada incremento lleva pruebas, documentación y regresión.

| Inc. | Contenido | Depende de | Criterio de aceptación | Riesgo principal |
|---|---|---|---|---|
| I0 | Testimonio de evaluación y `LocusConditionedEvidence2D` en el kernel, sin consumidores; política v1; contadores; medida de coste con pintura | — | banco §7 con el protocolo real; determinismo frente al orden; ningún consumidor cambia | coste por revisión |
| I1 | Dirección por parámetro y su códec; `Point(L, u)` y forma de tres argumentos sobre evidencia condicionada; prueba de lectores | I0, D-V2 | guardar, reabrir, copiar, redefinir, deshacer; lector 1.2.1 falla cerrado; documentos 1.2.1 sin cambio | compatibilidad |
| I2 | Presentación: trazo condicionado, texto de estado, bandas | I0, D-V3 | invariancia con zoom, DPI y viewport de la evidencia; ninguna unión a través de una banda | confusión con «completo» |
| I3 | Longitud entre posiciones y total, con política de bandas | I1 | subtotal y rechazos tipados; H10 y P2 intactos | bandas estrechas |
| I4 | Continuación e intersecciones locales; locus dependiente | I1, I3 | nunca `EMPTY`; raíces en banda no publicadas | identidad de raíces |
| I5 | Exportación con clase de fidelidad | I2, D-V4 | omisión diagnosticada donde el formato no la transporta | formatos |
| I6 | K2 acotada | I0 | promoción por certificado; sin cambio de identidad | recintos de `τ` |
| K1 | Solo con incremento aprobado y delimitado | I0 | — | alcance |

Orden mínimo útil para el criterio del autor: I0 → I1 → I2 → I3.

## 6. Riesgos

1. **Falso tramo condicionado.** Existe y se declara; solo la promoción lo elimina.
2. **Deriva hacia certificación por densidad.** Bloqueada por contrato (§15.1 intacto) y por
   tipo: la naturaleza no certifica.
3. **Coste tras un cambio real.** Medir en I0; si excede, presupuesto con `UNEXPLORED`.
4. **Compatibilidad del códec nuevo.** Límite declarado; prueba de lectores en I1.
5. **Superficie de consumidores.** Matriz única en la norma; cada consumidor con su prueba.

## 7. Banco adversarial

Para cada caso: hecho que debe detectarse por contrato, hipótesis no demostrada, resultado
admitido y fallo esperado. No se exige al método una prueba universal que no ofrece, y
superar el banco no certifica nada. Resultados de la sonda externa
`tools/benchmark/post-r1-locus/conditioned-use-probe.js` (evaluadores sintéticos de verdad
conocida; política de sonda: 32 celdas iniciales, flecha 10⁻³, giro 0,35 rad, paso mínimo
2⁻²⁰); no es código de producto ni prototipo del kernel.

| Caso | Hecho a detectar por contrato | Hipótesis no demostrada | Resultado admitido | Fallo esperado | Sonda |
|---|---|---|---|---|---|
| C1. Salto válido en un corte angular | el salto queda aislado al paso mínimo | H3 fuera de la banda | `DISCONTINUITY_BAND`; los tramos a ambos lados no se unen | — | detectado; 95 evaluaciones |
| C2. Dos eventos de validez en una celda | solo si un nodo cae dentro | H1 | banda si se detecta; tramo declarado bajo H1 si no | falso tramo | hueco 10⁻²: detectado; 10⁻⁴ y 10⁻⁷: no detectado |
| C3. Dos raíces intercambian y regresan | solo si un nodo cae dentro | H2 | `SELECTION_BAND` o tramo bajo H2 | falso tramo, con márgenes grandes en los nodos | no detectado |
| C4. El margen se anula entre nodos | ninguno con muestras finitas | H2; el margen observado no es cota | `MARGIN_ESTIMATED`, nunca `MARGIN_BOUNDED` | clase sobreestimada si se llamara cota | aceptado como estimado: la sonda confirma que la pendiente observada no acota |
| C5. Fallo de evaluación con existencia | el fallo no es ausencia | — | `FAILURE_BAND`; sin subdividir su interior | — | detectado; 288 evaluaciones tras la regla de frontera |
| C6. Transición estrecha con contribución métrica | la banda no se omite de un total | H4 | contribución, cota o `SUBTOTAL` | total erróneo si se omite | no detectada por cuerdas: 1,0 frente a 11 |
| C7. Raíz local utilizable con cobertura no establecida | la raíz se publica local | cobertura | raíz `VERIFIED_UNCERTIFIED`; completitud `NOT_ESTABLISHED` | declarar `EMPTY` o completo | por contrato (§8) |
| C8. Selección ambigua, tangencia, auto-intersección, varias preimágenes | la dirección por parámetro es inequívoca; la inversa no | — | punto por parámetro definido; asignación de un punto coincidente rechazada | asignar preimagen por proximidad | por contrato (§6) |
| C9. Retirada, promoción y órdenes de consulta | misma evidencia con cualquier orden | — | celdas y clases idénticas | dependencia del orden | idénticas con tres órdenes |
| C10. Guardar y reabrir antes y después de refinar | la dirección no depende de la evidencia | — | misma dirección; evidencia recalculada | persistir la malla | por contrato (§6, §7); prueba en I1 |
| C11. Modelo del cono | arcos de `w_2`/`a_3` y contorno transportado | H1–H4 | regiones con bandas de transición en los pliegues | unir los dos arcos | testimonio medido (§2); recorrido externo de R1 |

Sintéticos independientes del modelo ya versionados como pruebas de las reparaciones
(`PostR1*` del módulo compartido) sirven de base a los de I0.
