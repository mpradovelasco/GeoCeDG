# GeoCeDG — Uso condicionado de Locus V2: diseño (bloque 3A, revisión 4)

**DISEÑO PROPUESTO, REVISIÓN 4 — TEXTO NORMATIVO PENDIENTE DE APROBACIÓN AUTORAL. NADA DE V
ESTÁ IMPLEMENTADO.** `selfApproved=false`.
Decisión propuesta: [ADR 0042](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md).
Texto normativo candidato: [contrato candidato](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md).
Decisiones del autor y puntos abiertos: [registro de decisiones](../validation/post_r1_conditioned_use_author_decision_sheet.md).

La revisión 1 es el texto del commit `ab37fb5ff`. Esta revisión incorpora las decisiones
D-V1 a D-V7 del autor (2026-11-21) y sustituye las alternativas anteriores donde las
contradicen. La revisión 3 añade la enmienda focal de seis puntos que el autor pidió tras
revisar la revisión 2 (commit `83cd56c4c`); no cambia el objetivo funcional ni el resto del
diseño. La revisión 4 añade las dos precisiones focales que el autor pidió tras revisar la
revisión 3 (commit `57c495cce`): identidad de las raíces entre configuraciones y resolución
de la evidencia bajo semejanzas. Las diferencias constan en el ADR y en los §13 a §15 del
contrato candidato; los §14 y §15 llevan los criterios de validación. Este documento no repite el contrato: explica cómo se llega a él desde el código actual, cómo se
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
| ¿Cómo se identifica hoy una raíz de intersección? | `IntersectionRootDeterministicSelector2D`: el selector base es el linaje de la componente certificada que contiene la raíz más el germen transversal tipado; solo un grupo de dos o más raíces con el mismo selector se enriquece con fase y rango, que exigen componente orientada y cardinalidad del grupo. `PublicIntersectionRootIdentityResolver2D.component()` devuelve nulo cuando ninguna componente certificada contiene el parámetro, y esa raíz queda sin selector y sin asignación en el ledger. No interviene ninguna comprobación de completitud |
| ¿Qué exige hoy la integridad bajo autoridad constructiva? | `RELAXED` solo se produce por `SUBSTITUTE_GENERIC` (`SubstituteGenericEvidence`) y, para raíces, por la recalificación de §7.4 (`CertifiedContactAnalysis2D.admission`: raíz transversal aislada cuyo recinto riguroso del residuo constructivo contiene el cero dentro del umbral). El punto de token exige admisión `STRICT` o `RELAXED` (`LocusIntersectionResult2D.constructivelyAdmitted`); el punto estructural por correspondencia exige autoridad constructiva e incidencia exacta o `RELAXED` (`StructuralPointResolver`). El punto paramétrico no consulta integridad |
| ¿Cómo se trata hoy la razón cero? | `LocusSimilarityTransform2D.isCollapsed()`; `AlgoLocusSimilarityTransform2D` publica la propiedad `COLLAPSED_IMAGE`, conserva el dominio declarado y reutiliza la estructura de existencia de la fuente con sus componentes y huecos; la evaluación devuelve el centro; las métricas dan cero exacto (`collapsedState`, `zeroState`); una consulta inversa nueva devuelve `DEGENERATE_SOURCE_IMAGE`; el paquete de intersecciones no tiene rama propia para la imagen colapsada |
| ¿Cuánto cuesta recorrer el dominio? | R1, recorrido externo sin el protocolo de este diseño ni pintura: `w_2` 209 evaluaciones / 663 ms; `a_3` 223 / 720 ms; `trHole` 138 / 284 ms |
| ¿Cada cuánto habría que repetirlo? | Una vez por revisión. Tras A y A′ mover `E_1` o `N` no publica revisiones; tras D-V6 una revisión por cambio real cuesta ≈ 0,3 s por locus del agujero |

## 3. Cómo resuelve el diseño cada obligación

### 3.1 Hipótesis, clases y admisión

Contrato §3 y §4. Se **comprueba** en los nodos: validez, selección establecida de cada
dependencia selectora, criterios geométricos entre nodos y separación de la raíz
seleccionada. Se **supone**, con ese nombre: H1 validez, H2 selección, H3 continuidad
(incluidos los cortes de operaciones con rama) y H4 resolución.

`MARGIN_BOUNDED` queda corregida, y en la revisión 3 precisada. Una cota rigurosa de la
separación entre raíces prueba una sola cosa: que las raíces que la cota encierra siguen
separadas dos a dos en la celda. Es un hecho sobre posiciones de raíces. No prueba que las
demás operaciones del slice sean válidas (H1) ni la continuidad de la función compuesta (H3),
y tampoco la estabilidad de ninguna selección (H2): qué raíz selecciona una dependencia
depende además del germen y su orientación, de la elegibilidad y el aislamiento de cada raíz,
de la pertenencia a un objetivo limitado, de la cardinalidad del grupo y de raíces que la
cota no encierra. H2 solo se reduce con una prueba del propio predicado de selección, que es
trabajo del certificador o de un promotor. La tabla del
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
antiguos de la revisión 1 se retira. En su lugar el contrato §6.6 fija las obligaciones de
1.2.2, cada una con su prueba en I1: identidad durable, guardar y reabrir, copiar y
duplicar con remapeo, deshacer y rehacer, redefinición compatible e incompatible, selección
y continuación, degeneraciones, recuperación tras invalidez temporal y lectura sin cambios
de las construcciones de regresión.

El punto paramétrico no necesita evidencia intervalar para estar definido: su contrato es
validez puntual, dirección inequívoca y procedencia (§15.8). La evidencia intervalar decide
qué continuación se ofrece y con qué grado se presenta. La revisión 3 separa los dos hechos
(contrato §6.3 y §6.4): un punto cuya evaluación aislada y cuya selección son válidas está
definido también dentro de una banda; lo que la banda impide es la continuación, es decir,
el seguimiento, la ruta y la longitud. Una evaluación válida dentro de una banda de
ausencia o de fallo es además una contradicción de esa celda, que se comunica al productor.

**Identidad de las raíces de intersección** (contrato §6.5). El selector vigente liga el
linaje de la componente certificada que contiene la raíz, y el código deja sin selector la
raíz que no está en ninguna. Una región condicionada no tiene linaje y no se le da: sus
límites son las bandas que la travesía detectó, de modo que una identidad construida con
ellos, o con celdas, sería una identidad por muestreo. La única referencia de la fuente que
no depende de la travesía es la rama. Por eso la raíz sobre un tramo toma un selector de
tipo propio, `conditioned-use/branch-frame-root/v1`: linaje de rama más germen transversal
tipado, sin rango. Se le aplica sin cambios la regla vigente de asignación: solo un selector
único entre las raíces publicadas del resultado asigna o resuelve un token, y la asignación
es función del estado actual, nunca de la historia. Consecuencias que el contrato declara:

- dos o más raíces del mismo germen sobre una rama condicionada se publican como raíces sin
  token durable, porque el rango exige componente certificada y cardinalidad verificada;
- una raíz que pasa de una componente certificada a la parte no certificada de su rama, o al
  revés, cambia de selector: el token anterior deja de ser actual, no se transfiere y vuelve
  a serlo cuando el estado regresa;
- las entradas de este tipo necesitan una versión nueva del formato del ledger; las
  existentes no cambian de texto ni de significado.

En el modelo del autor una recta que corta el contorno transportado en dos puntos da dos
gérmenes opuestos y, por tanto, dos tokens.

**Identidad entre configuraciones** (revisión 4, contrato §6.5, puntos 4 a 6). Que el
selector de marco de rama sea único en el estado actual no prueba que la raíz actual sea la
que recibió el token: el marco es la rama entera, sus regiones están separadas por bandas y
las raíces encontradas no son todas las raíces. En el mundo certificado esa separación la da
el linaje de componente, y la norma vigente ya dice que no poder establecer la admisibilidad
actual no autoriza a otra raíz a adquirir el token (`locus-v2-intersections.md` §6.1) y que
una raíz que cruza un hueco termina (§6.3). Aquí hace falta una regla propia:

- La asignación guarda, con el selector y el token, un **origen**: la dirección por
  parámetro de la raíz en la publicación que asignó el token. Es un dato de creación, fijo,
  de la misma naturaleza que el parámetro de una dirección. No forma parte del selector y
  nunca se actualiza.
- **Guarda de reutilización.** El token solo se resuelve si el selector es único y, además,
  el origen y el intervalo aislante de la raíz actual están en una misma región condicionada
  del contenido publicado: todas las celdas entre ambos son tramos.
- Si la guarda falla, el token se **retiene**: no se entrega a esa raíz, no se transfiere,
  no se retira y no se asigna otro para ese selector. La raíz se publica con asa local y
  `NOT_ESTABLISHED`; el objeto que depende del token queda indefinido con su causa vigente y
  vuelve a estar definido cuando un estado cumple la guarda.

Propiedad que se exige y se prueba: **la evidencia puede retener un token; nunca lo elige**.
De ella salen las dos decisiones de detalle. Hay una sola asignación por selector, porque con
varias, cada una con su origen, a cuál queda conectada una raíz dependería de qué bandas haya
encontrado la travesía. Y el origen no sigue a la raíz, porque actualizarlo sería historia
de movimiento, que el contrato de intersecciones excluye de la identidad. La región no entra
en el selector ni en el ledger: se lee como premisa, igual que en el seguimiento (contrato
§8.2), y las celdas entre el origen y la raíz quedan registradas como celdas usadas por el
resultado. Dentro de una región el token significa lo mismo que la selección determinista
vigente en una componente: la única raíz de su germen en su marco; no afirma que se haya
seguido una raíz individual. Consecuencias que el contrato declara:

- una raíz que la configuración saca de la región de su origen queda sin token hasta que el
  estado regresa, y otra raíz del mismo selector en otra región no recibe token durable en
  ese resultado; otro resultado de intersección tiene su propio ledger y su propio origen;
- dos documentos con el mismo ledger resuelven igual en el mismo estado. Dos historias que
  asignan en configuraciones distintas guardan orígenes distintos y pueden diferir en cuándo
  se retiene un token, nunca en qué raíz tiene qué token.

### 3.5 Ciclo de vida determinista

Contrato §7. Tipo propuesto `LocusConditionedEvidence2D`, inmutable, propiedad del productor
del locus. La revisión 3 separa dos atributos que la revisión 2 confundía en «generación»:

- **Identidad de contenido**: `(revisión, versión de política, conjunto de contradicciones)`
  en forma canónica. Es lo que un resultado registra y de lo que se decide la vigencia.
- **Contador de publicación**: entero que crece en cada publicación de la revisión. Ordena
  las publicaciones de una sesión y despierta a los dependientes. No es identidad, no se
  compara entre historias y no se persiste.

Reglas:

- **Determinismo.** El conjunto de contradicciones es el conjunto de *parámetros* en los que
  el productor confirmó una contradicción; no son celdas, porque la celda que contiene un
  parámetro depende de lo ya refinado y, por tanto, del orden. Una celda se divide cuando lo
  exigen los criterios ordinarios o cuando es mayor que el paso mínimo y contiene un
  parámetro del conjunto. Esa regla es un predicado por celda, de modo que la partición es
  la misma con cualquier orden de descubrimiento. Dos historias que confirman el mismo
  conjunto tienen la misma identidad de contenido, aunque sus contadores difieran. La sonda
  (caso C9) da celdas idénticas con tres órdenes de consulta; I0 lo prueba con permutaciones.
- **Contradicción.** Quien la observa solo la *comunica*. El productor reevalúa el parámetro
  con su propio evaluador y, si la confirma, lo añade al conjunto y publica el contenido del
  conjunto nuevo. Un aviso no confirmado o repetido no cambia nada. El renderer no decide
  ninguna verdad geométrica.
- **Retirada e invalidación.** Una publicación retira exactamente las celdas que no están,
  con los mismos extremos, tipo y clase, en el contenido nuevo. Un resultado registra las
  celdas que usó y es vigente mientras todas sigan sin cambio; si no, se recalcula antes de
  servirse.
- **Identidad de los puntos afectados.** Conservan dirección e identidad: siguen definidos
  si su propia evaluación sigue siendo válida, y quedan indefinidos con causa tipada si no.
- **Sin evidencia obsoleta ni promoción por densidad.** Una revisión nueva descarta la
  evidencia y el conjunto de contradicciones anteriores. La vigencia nunca se decide
  comparando contadores.

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
| `Intersect(L, objetivo)` | `allComponents()` del solver; `component()` del resolvedor de identidad | añadir las regiones condicionadas al dominio de búsqueda; admisión por raíz; selector de marco de rama para la raíz sobre un tramo; completitud `NOT_ESTABLISHED`; nunca `EMPTY` |
| `Intersect(L1, L2)` | pares de componentes | pares de regiones o componentes; marco de rama por lado |
| `Translate`, `Rotate`, `Mirror`, `Dilate` | ninguna puerta; hereda la estructura | heredar también la evidencia condicionada, celda a celda, frente a la política en el mundo de la imagen: sin cambio si la razón absoluta no supera 1; con razón mayor, recomprobar cada tramo y reclasificar el que falle; razón cero: ningún cambio, rige el contrato vigente de `COLLAPSED_IMAGE` |
| Locus dependiente de puntos, raíces o métricas condicionadas | su propio certificado | su propia evidencia condicionada, con las hipótesis heredadas |
| Locus conducido por un punto sobre un locus condicionado | `resolveDomain` exige soporte `COMPLETE` | piezas del dominio desde las regiones condicionadas del soporte |
| Selección y manejo | ninguna | ninguna; el clic crea el punto por la consulta inversa existente y lo direcciona por parámetro |
| Exportación semántica | `existenceNotEstablished` | sin cambio de comportamiento en 1.2.2: omisión diagnosticada con la lista de regiones |

Las transformaciones no necesitan nueva semántica: la imagen ya es `T ∘ L` sobre el
evaluador de la fuente. Lo único que falta es que la evidencia viaje con la estructura.
Transformar la polilínea de dibujo no forma parte de ninguna vía, ni actual ni propuesta.

**Resolución bajo semejanzas** (revisión 4, contrato §8.7). La evidencia no puede viajar sin
más, porque los límites de la política son magnitudes del mundo del locus que la publica.
Una semejanza de razón absoluta `ρ` multiplica por `ρ` la flecha entre nodos y toda longitud
que mida un criterio, y deja iguales los extremos de celda, el paso mínimo, el giro, H1 a
H3, el testimonio, el margen y la clase, que son hechos del slice de la fuente. La cota de
H4 que declara la fuente, `δ`, vale `ρ·δ` en el mundo de la imagen.

- Con `ρ ≤ 1` (isometrías, reducciones y razón cero) todo tramo cumple la política de la
  imagen y las celdas se heredan iguales. Nada se fusiona.
- Con `ρ > 1` la cota de la fuente deja de cumplirla y no se conserva. Cada tramo se
  recomprueba en el mundo de la imagen, lo que es aritmética sobre los nodos que la fuente
  ya tiene; el que pasa es tramo de la imagen y el que falla se reclasifica con la regla
  ordinaria sobre la función imagen, subdividiendo hasta cumplir o hasta el paso mínimo,
  donde es `DISCONTINUITY_BAND`. El trabajo se carga al límite de la imagen; lo que no
  alcanza es `UNEXPLORED`.
- Una banda de la fuente nunca pasa a tramo porque la imagen sea pequeña.

La imagen declara H4 con el límite de la política en su mundo y nunca publica un tramo cuya
flecha en los nodos lo supere. Su evidencia tiene identidad de contenido propia, que
registra la de la fuente; sus regiones están contenidas en las de la fuente, de modo que la
imagen no admite nada que la fuente rechace. Una contradicción de H1 a H3 vista en la imagen
es de la celda de la fuente y se comunica a su productor.

**Razón cero.** La revisión 2 decía que la imagen era un punto con tramos «colapsados» sin
longitud ni intersección. Era incompatible con el contrato aprobado. La revisión 3 lo
conserva íntegro (contrato §8.7): la imagen es un Locus V2 válido con la propiedad
`COLLAPSED_IMAGE`, que mantiene dominios, ramas, direcciones y huecos; la evidencia se hereda
igual que con cualquier otra razón; un punto existente conserva su dirección y se recupera
cuando la razón deja de ser cero; las longitudes son los ceros del contrato métrico; un
objetivo que pasa por el centro es el caso vigente de solape no aislado. El código actual ya
se comporta así sobre la estructura heredada.

**Integridad bajo autoridad constructiva.** La revisión 2 decía que una incidencia sobre un
tramo condicionado era «como máximo `RELAXED`», lo que podía leerse como una concesión. La
revisión 3 (contrato §8, regla común) no amplía nada: `locus-v2-constructive-authority.md`
§6.1 ya dice que una verificación numérica bajo hipótesis no basta para `RELAXED`. Sobre una
región condicionada la integridad de una relación es `NOT_ESTABLISHED` salvo que esa
relación tenga su propia evidencia del tipo que el contrato de autoridad exige. Efecto
práctico en un documento constructivo: el punto paramétrico existe y muestra integridad no
establecida; una raíz se publica, y su punto de token solo se materializa si la
recalificación vigente la admite; el punto estructural por correspondencia sigue exigiendo
componente certificada. Si I4 mostrara que esa evidencia positiva es inalcanzable en fuentes
condicionadas de interés, ampliarla sería una propuesta normativa propia, con versión nueva
de la política de admisión; este contrato no la contiene. Los documentos bajo la autoridad
heredada, que es la de por defecto, no cambian.

Ejemplos sobre el modelo (variante con `trHole`):

| Operación | Hoy | Con V |
|---|---|---|
| `Point(trHole, 0.5)` | rechazado, `OUTSIDE_COMPONENTS` | definido; `POINT_VALID`; dirección por parámetro |
| Arrastre de ese punto | no disponible | a lo largo de la región; se detiene en una banda |
| `LocusLength(trHole, A, B)` | no disponible | `ESTIMATED`, con H1–H4 |
| `Intersect(trHole, recta)` | cero soluciones | raíces locales `VERIFIED_UNCERTIFIED`, con token cuando su germen es único en la rama; completitud `NOT_ESTABLISHED` |
| `Rotate(trHole, 30°, S_1)` | imagen sin componentes ni consumidores | imagen con la evidencia de la fuente y los mismos consumidores |
| `LocusV2(f(Point(trHole, u)), u)` | sin evaluaciones útiles | su propia evidencia condicionada |
| Consumidor que exige componente certificada o `STRICT` | rechaza | rechaza igual |

V no corrige el procedimiento original del modelo: dibujaría completos los dos arcos de
`w_2` y `a_3`, con su imagen conjugada. La corrección es la variante transportada.

### 3.7 Presentación (D-V3)

- El estilo del locus no se toca. Se retira la raya larga obligatoria de la revisión 1.
- **Estado en las propiedades**, obligatorio: texto localizado, por ejemplo «Condicionado —
  existencia/continuidad no certificadas», con las hipótesis H1–H4, la versión de política y
  el número de regiones y bandas por tipo, en el inspector de definición y en la
  descripción del objeto. Hoy ese estado es un enumerado crudo sin localizar; este
  punto lo sustituye.
- **Marca del sistema**, viable con intervención acotada. La infraestructura de la pista de
  orientación ya resuelve lo difícil: colocación en píxeles, recorte a la vista, cuerda
  mínima visible, trazo de 1 px con el color del objeto, ausencia de área de selección y
  punto único de pintura en `GeoCeDGEuclidianView.paint`. La marca condicionada reutiliza ese
  patrón con tres diferencias: se ancla en el vértice medio del trazo visible de cada región
  (dato que `LocusRenderData2D` ya tiene), su glifo no es un triángulo ni tiene dirección, y
  no depende de la preferencia de la pista de orientación. Coste: un glifo por región
  visible. No toca `DrawLocusV2`, la huella, la geometría ni la evidencia.
- Glifo: un rombo hueco de 7 px, aprobado por el autor el 2026-11-21. No es una flecha y no
  sugiere sentido.
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
fase de importación DXF. Por decisión del autor de 2026-11-21 la dependencia queda
registrada en el roadmap como requisito de la línea futura de B-splines y DXF, en un
registro aditivo fechado, sin abrir ninguna subfase.

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

Solo tras aprobar el texto normativo revisado, con una excepción que el autor concedió el
2026-11-21 al revisar la revisión 3: pueden adelantarse los trabajos preparatorios de I0 a
I3 que no dependan de las dos precisiones de la revisión 4, conservados como candidatos
pendientes de la aprobación normativa final. I4 e I4T no se inician antes de ella. Cada
incremento cierra sus pruebas y demuestra ausencia de regresiones. Todos, de I0 a I4T, son necesarios para declarar
concluida la capacidad en 1.2.2.

| Inc. | Contenido | Depende de | Criterio de aceptación |
|---|---|---|---|
| I0 | Testimonio de evaluación y `LocusConditionedEvidence2D` en el kernel, sin cambiar operaciones públicas; política v1; conjunto de contradicciones; contadores | — | banco §7 con el protocolo real; determinismo frente al orden; presupuesto; coste por revisión con pintura; ningún consumidor cambia |
| I1 | Dirección por parámetro y su códec; puntos sobre regiones condicionadas; validez puntual separada de la continuación | I0 | las obligaciones del contrato §6.6; criterios 1a y 4a |
| I2 | Estado en las propiedades y marca del sistema | I0 | estilo intacto; marca independiente de zoom, grosor, color y tipo de línea; ninguna unión a través de una banda |
| I3 | Longitud entre posiciones y total, con subtotales y bandas | I1 | H10 y P2 intactos; ninguna banda omitida |
| I4 | Intersecciones locales, de objetivo y de pares, con el selector de marco de rama y la versión nueva del ledger; locus dependientes y anidados; reparación de F8 antes de cerrar el incremento | I1 | nunca `EMPTY` ni completitud; candidato en banda no publicado; una raíz local admisible alimenta construcciones; criterios 1a, 1b, 2a a 2d y 7a a 7e |
| I4T | Herencia de la evidencia por las transformaciones; manejo geométrico; adaptadores de interfaz separados del kernel | I0, I4 | la imagen admite los mismos consumidores sobre sus propias celdas; contrato `COLLAPSED_IMAGE` intacto (criterio 5); resolución bajo semejanzas (criterios 8a a 8f); ninguna polilínea como fuente |

Los criterios numerados son los de los §14 y §15 del contrato candidato. I0 lleva además los
criterios 3a, 3b, 4b y 6, e I3 la parte de rutas del 4a.

Fuera de 1.2.2: exportación semántica de regiones condicionadas; K2 y K1.

**F8, defecto previo.** El autor autorizó el 2026-11-21 su triaje temprano y su reparación
antes de cerrar I4, adelantándola si afecta a incrementos anteriores. Con un locus consumidor cuyo conductor es entrada no
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
de producto ni prototipo del kernel. Los casos C17 y C18 se ejecutan en una segunda sonda,
`tools/benchmark/post-r1-locus/conditioned-use-precisions-probe.js`, que aplica en paralelo
la regla de la revisión 3 y la de la revisión 4 y comprueba que cada caso discrimina: la
primera falla y la segunda pasa.

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
| C12. Cota de separación sin validez ni selección | una cota de raíces no prueba H1, H3 ni la estabilidad de la selección: las raíces siguen separadas y la dependencia cambia de raíz dentro de la celda | H1, H2, H3 | `MARGIN_BOUNDED` con los mismos permisos que `RESOLUTION_ONLY` y H2 registrada | tratarla como certificado o como selección estable | por contrato §3.3; caso a añadir a la sonda en I0 |
| C13. Imagen por semejanza de razón cero | rige el contrato `COLLAPSED_IMAGE` | la existencia de posiciones y rutas, H1 | imagen válida con dominios, huecos, direcciones y celdas de la fuente; longitudes nulas del contrato métrico; solape no aislado en el centro | fabricar una raíz aislada, reclasificar celdas o invalidar puntos existentes | por contrato §8.7 |
| C14. Punto válido dentro de una banda | validez puntual y continuación son hechos distintos | — | punto definido, con la banda declarada; seguimiento y ruta rechazados con causa | invalidar el punto o moverlo a un vecino | por contrato §6.3, §6.4 |
| C15. Dos raíces del mismo germen en una rama condicionada | sin rango no hay selector único | — | raíces publicadas sin token durable | asignar tokens por orden, índice o celda | por contrato §6.5 |
| C16. Mismo conjunto de contradicciones en distinto orden | el contenido depende del conjunto | — | misma identidad de contenido y mismas celdas; contadores libres | identidad dependiente del orden | por contrato §7 |
| C17. Desaparición y aparición en regiones separadas: una rama con dos regiones separadas por una ausencia; la recta `y = c` tiene una raíz de germen positivo en la primera (`c = 0,2`) o en la segunda (`c = 1,8`) | las dos raíces tienen el mismo selector de marco de rama y no son la misma raíz | H1 a H3 de los tramos entre el origen y la raíz | el token se retiene: raíz con asa local, punto dependiente indefinido, una sola entrada en el ledger; al volver a la primera región el token es actual | entregar el token a la raíz de la otra región; asignar un segundo token; que la evidencia elija | regla de la revisión 3: el punto salta a la segunda región. Regla de la revisión 4: retenido en un paso y tras un estado sin raíces, recuperado al volver. Con profundidades 32, 64 y 128 y con una banda confirmada entre origen y raíz, la raíz tiene el token o ninguno |
| C18. Semejanza de razón mayor que 1: circunferencia unidad y dilatación | la cota de H4 de la fuente vale `ρ·δ` en la imagen y deja de cumplir la política | H4 en el mundo de la imagen | con `ρ·d ≤ δ`, celdas heredadas; con `ρ·d > δ`, celdas reclasificadas; banda en el paso mínimo; una banda de la fuente sigue siendo banda | publicar un tramo con flecha superior al límite; convertir una banda en tramo en una reducción | 128 tramos de flecha 3,01·10⁻⁴; razón 3: heredados (9,04·10⁻⁴); razón 4: copiar publica 128 tramos de 1,20·10⁻³, la regla publica 256 de 3,01·10⁻⁴, iguales a los de una travesía propia; razón 100 con paso mínimo 2⁻⁸: 256 bandas; salto de 0,5 a razón 10⁻⁴: la banda se conserva |
