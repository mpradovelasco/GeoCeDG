# GeoCeDG 1.2.2 — informe de revisión técnica complementaria del candidato

| Campo | Valor |
|---|---|
| Carácter | **Informe de revisión para decisión del autor. Análisis y plan; ningún cambio de producto.** No aprueba nada, no declara ningún incremento `PASS` y no sustituye a ninguna autoridad. `selfApproved=false` |
| Instrucción | Sexta instrucción del autor, 2026-11-22: análisis complementario con tres objetivos de corrección para 1.2.2 (identidad de pares multicomponente, `LocusLength` condicionado, rendimiento D-V6) y revisión de regresiones, validación, empaquetado y gobernanza |
| Candidato revisado | `b2c5aad355d955d684512f6e08f986c200e25bb3` (versión 1.2.2, fecha de versión `2026-11-22`, que el autor mantiene); punta de la rama `03580658449d346010a014ebf77ea03e91124d19` (solo registro documental) |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar. `main` = `origin/main` = `9ccefdcfe922d8c0e329bdeec30c12fc058de463`. Etiquetas de versión: `v1.2.0`, `v1.2.1` |
| No ejecutado | `FINAL`, promoción a `main`, etiqueta `v1.2.2`, release: ninguno está autorizado y ninguno se ha hecho |
| Estado del árbol | Limpio en `035806584` durante todo el análisis; este fichero es el único añadido y **no está confirmado en Git** |
| Método | Lectura de código, contratos y pruebas; sondas y mediciones **fuera del repositorio** (scratchpad de la sesión), sobre el checkout del candidato, sobre un worktree de la base `9ccefdcfe` y sobre los JAR de la app-image del candidato |

Convención: **medido** significa observado en esta revisión con el comando indicado en §8;
**estimado** significa deducido de un perfil o de la lectura del código, sin medición directa.

## 0. Resumen para la decisión

### 0.1 Clasificación

| # | Asunto | Clase | Origen |
|---|---|---|---|
| B1 | Un punto creado sobre una raíz de `Intersect(L1, L2)` puede pasar a **otra raíz** con el mismo token cuando cambia el ordinal de una componente (asociación incorrecta, reproducida) | **Defecto bloqueante** | Previo: se reproduce igual en la base `9ccefdcfe` (1.2.1) |
| B2 | `Intersect(L1, L2)` pierde **todas** sus raíces (`NUMERICAL_FAILURE`) cuando las dos fuentes comparten clave de rama y tienen dos o más componentes cada una | **Defecto bloqueante** (falla cerrado: no asocia mal, pero el resultado desaparece) | Previo: igual en la base |
| N1 | El escalar de `LocusLength(L, A, B)` sobre una región condicionada queda sin definir aunque el valor, su error estimado y su tolerancia ya son admisibles | **Corrección necesaria para 1.2.2** (requiere decisión normativa DN-2) | Límite que el incremento I3 dejó expresamente sin decidir |
| N2 | Un cambio real con la evidencia consumida cuesta 1,26 a 1,31 s (medido); el objetivo D-V6 es < 1 s | **Corrección necesaria para 1.2.2** (requiere aprobar el diseño DN-3) | Coste del bloque 3B |
| N3 | Dos pruebas renombradas en I2 siguen citadas con su nombre anterior en la matriz de validación CU2 (fila V-B07-4) | **Corrección necesaria** (solo pruebas) | Bloque 3B |
| N4 | Cinco adaptaciones de pruebas dejaron de fijar un hecho que antes fijaban (§4.2) | **Corrección necesaria** (solo pruebas) | Bloque 3B |
| N5 | La evidencia de aislamiento `SAFEGUARDED_REGULAR_JACOBIAN` se publica con el vocabulario `ESTABLISHED` / `EXHAUSTIVE_RECTANGLE` de un rectángulo certificado | **Corrección necesaria** menor (vocabulario y prueba) | I4 |
| N6 | El registro afirma «regresión compartida sin fallos» para I4T; la ejecución completa no se repitió tras el último ajuste | **Corregido en esta revisión** con una ejecución completa nueva (§5.2) | Registro del bloque 3B |
| D1 | Total con bandas (`LocusLength(L)` como subtotal) como escalar | **Diferible** sin comprometer la función prevista (§2.6) | — |
| D2 | Reutilizar la copia aislada entre revisiones cuando solo cambian valores; compartir las evaluaciones del certificado con el recorrido | **Diferible** si N2 alcanza el objetivo con las palancas 1 y 2 (§3.5) | — |
| D3 | Lanzamiento del ejecutable empaquetado, instalación de MSI y EXE, regresión completa de Desktop sobre el commit final | **Pendiente de la validación final**, no del análisis (§5.3) | — |

Mi recomendación es no aceptar técnicamente `b2c5aad35` como 1.2.2: B1 y B2 son defectos de
identidad y de resultado que ya existen en 1.2.1 y que el autor ha fijado como objetivos de
corrección; N1 y N2 son la función prevista del uso condicionado. El plan de §7 los resuelve
en un candidato sucesor.

### 0.2 Decisiones que se piden al autor

| Id | Decisión | Alternativa recomendada | Alternativa menos invasiva |
|---|---|---|---|
| DN-1 | Identidad de componente en el selector de pares (D2) | Linaje estructural de componente de la decisión E-3 (el mismo que ya usan los tokens de una sola fuente), contrato de entrada `…/v2`, adopción exacta de las entradas heredadas | Corregir solo B2 (clave local con lado, sin cambio normativo) y dejar B1: **no la recomiendo**, B1 es una asociación incorrecta |
| DN-2 | Admisión escalar de una longitud condicionada | Nuevo §28.3 de `locus-v2-metrics.md`: admite la ruta entre dos posiciones de una misma región bajo las condiciones de §2.4; los totales siguen regidos por §26.5 y §26.6 sin cambio | Limitarla a las causas que H10 y P2 ya admiten: no da número en el modelo del autor, **no cumple el objetivo** |
| DN-3 | Diseño de reutilización para D-V6 | Memo de evaluaciones anidadas propio de cada instancia de evaluador (no compartido entre productores), más resumen de valores más barato; política de evidencia intacta | Solo el memo: medido en 0,96 a 1,00 s, en el límite del objetivo |
| DN-4 | Adaptaciones de pruebas N3 a N5 | Restituir nombres citados y volver a fijar los hechos | — |

Ninguna de las tres decisiones normativas modifica el ADR 0042 ni el contrato
`conditioned-use/v4`. DN-1 modifica `spline-v2-pair-materialization.md` (ADR 0028) y la
cláusula de formato del libro de tokens; DN-2 modifica `locus-v2-metrics.md` §28.3, que es
una enmienda mía de I3 que aplaza la cuestión; DN-3 no modifica ninguna norma.

## 1. Identidad de pares multicomponente

### 1.1 Diagnóstico

**B2 — colisión de la clave de par.** `LocusPairIdentity2D.componentPair`
(`…/locus/intersection/LocusPairIdentity2D.java:21`) forma la clave de un par de componentes
ordenando los dos textos `rama + componente`, sin la fuente. Si las dos fuentes comparten
clave de rama (`generator.main`, que es lo habitual) y tienen componentes `component-0` y
`component-1`, los pares (A₀, B₁) y (A₁, B₀) producen el mismo texto.
`LocusPairIntersectionCandidateSet2D` rechaza claves cubiertas repetidas («Covered pair keys
must be unique and nonblank», línea 120) y el solucionador convierte la excepción en
`NUMERICAL_FAILURE`.

- **Medido** en el candidato y en la base: dos fuentes certificadas con dos piezas cada una
  dan `status=NUMERICAL_FAILURE`, cero raíces y un libro sin entradas; con 2 × 1 piezas el
  resultado es correcto y tiene su entrada `P`.
- Falla cerrado: no asocia nada mal. Pero desaparece el resultado entero, y con él todos los
  puntos que dependían de sus raíces, en cuanto la segunda fuente pasa de una a dos
  componentes (un cambio topológico ordinario).
- I4 esquivó el caso para las regiones condicionadas con claves que nombran su lado
  (`…/first-region-i`, `…/second-region-i`); las componentes certificadas quedaron como
  estaban.

**B1 — el selector D2 liga un ordinal.** `PublicSplinePairRootIdentityResolver2D`
(líneas 146 a 161) pasa como «linaje de componente» de cada lado
`group.getFirstComponentKey()`, que es la clave `rama/component-<ordinal>`. La entrada `P`
persistida lo confirma: su selector `pair-singleton-transverse-germ/v1` contiene
`generator.main/component-1`. `spline-v2-pair-materialization.md` §2 exige «component
lineage» y prohíbe expresamente «output indices» en la clave duradera (línea 57).

- **Medido, asociación incorrecta** (candidato y base, sonda `ReviewPairWrongProbe`): un
  lugar de dos piezas declaradas, una curva `SplineV2` que corta la recta tres veces, un
  punto `X` sobre la raíz en (0,174; 1). Al cambiar las piezas de modo que la raíz, sin
  moverse, pasa a estar en la primera componente y otra raíz de la misma clase de germen
  queda en la segunda: `X` pasa a (2,826; 1) con el mismo token y estado
  `DETERMINISTIC_SELECTION_ESTABLISHED`; la raíz original, que sigue existiendo, recibe un
  token nuevo. Al deshacer el cambio, `X` vuelve.
- **Medido, pérdida de identidad** (sonda `ReviewPairIndexProbe`): con una sola raíz, el
  mismo desplazamiento deja el punto sin definir y añade una entrada al libro.
- Si el documento se guarda en el estado desplazado, la asociación incorrecta queda
  persistida.
- Las raíces de `Intersect(L, objetivo)` de una sola fuente no tienen este defecto: desde
  CU2 (paso T11, decisión E-3) se identifican con
  `LocusComponentLineage2D.identityKey`, el linaje estructural de la componente
  (`locus-v2-existence-components.md` §15.6). El camino de pares quedó fuera de aquel
  cambio.

**Comprobación de no regresión de I4, medida:** un documento guardado por la base (libro
versión 5) con puntos sobre raíces de `Intersect(LA, SP)` y de `Intersect(SP, LA)` se abre en
el candidato con el libro idéntico byte a byte, los dos tokens conservados y los dos puntos
definidos. El orden canónico de lados que introdujo I4 no rompe documentos existentes.

### 1.2 Solución propuesta

1. **Clave local de revisión con lado (corrige B2; no es normativa).** Las claves de
   cobertura, de candidato y de token provisional de un par califican cada lado con la
   identidad duradera de su fuente, en el orden canónico por identidad de fuente que §2 ya
   manda para los descriptores. Con fuentes distintas dos pares distintos nunca coinciden;
   con la misma fuente el par no ordenado sigue siendo el correcto. Las claves de región de
   I4 con sufijo de lado se retiran en favor de este mecanismo único. Estas claves son
   locales de revisión: no son identidad y no se persisten.
2. **Linaje estructural de componente en el selector (corrige B1; DN-1).** El descriptor de
   cada lado liga `LocusComponentLineage2D.identityKey(estructura, componente)`: las fuentes
   estructurales de los dos extremos de la componente, independientes de su posición y de su
   ordinal. Es la misma autoridad que ya rige los tokens de una sola fuente; no se introduce
   una segunda verdad.
3. **Compatibilidad.**
   - Descomposición igual al dominio completo (una componente): el descriptor conserva el
     texto heredado `rama/component-0`. Todos los documentos existentes de una componente por
     lado quedan idénticos byte a byte. Es la misma regla de igualdad de descomposición de
     E-3.
   - Entradas heredadas de varias componentes: **adopción exacta** en la primera resolución
     tras la lectura, por igualdad del texto del selector heredado calculado en la
     configuración guardada (al reabrir, los ordinales son los del guardado), nunca por
     proximidad; la entrada se reescribe con el selector estructural y conserva su token. Es
     el precedente de `withCurrentRootBinding` (R3 → R4). La alternativa estricta es dejarlas
     dormidas; no la recomiendo porque deja sin definir puntos de documentos de 1.2.1 que hoy
     funcionan.
   - Las entradas nuevas llevan el contrato `d2-spline-pair-singleton-germ/v2`. Si la
     distinción por contrato basta, el formato del libro no cambia de versión; se confirma en
     el primer paso de la implementación y, si no, pasa a versión 8 con lectura de las
     anteriores.
4. **Linaje de par persistido.** Para pares N × M, que hasta ahora nunca pudieron persistirse
   (B2), el linaje de par se escribe calificado por fuente. Para los que hoy funcionan el
   texto no cambia.

**Límite que declaro.** Donde nada estructural distingue una componente (las piezas de un
dominio declarado), E-3 usa como clave los extremos declarados de la pieza. No es un ordinal
ni una coordenada de raíz, pero es un dato de parámetro: una pieza que cambia sus extremos
cambia de clave y el token queda dormido (nunca se traslada a otra raíz). Una identidad
declarativa por elemento de la lista sería un diseño nuevo que afectaría también a los tokens
de una sola fuente; no lo propongo para 1.2.2.

### 1.3 Comportamiento dinámico y persistencia

| Situación | Con la corrección |
|---|---|
| Reconstrucción dinámica sin cambio topológico | Selector y token idénticos; equivalencia de revisión como hoy |
| Aparece o desaparece una componente ajena | Los ordinales cambian; el linaje estructural de la componente de la raíz no: token y punto conservados |
| La componente de la raíz se divide o se funde | Sus fuentes de extremo cambian: selector distinto, token dormido, punto sin definir; al deshacer el cambio, vuelve. Ninguna raíz hereda el token por posición |
| 2 × 1 pasa a 2 × 2 | El resultado se mantiene (B2); la raíz conserva su selector |
| Guardar y reabrir | Mismo libro, mismo token; las entradas heredadas se adoptan una vez |
| Deshacer y rehacer | El libro viaja en el estado del documento como hoy; la adopción es determinista y se repite igual |
| Contrato Locus V2 | Sin cambio de revisión, de DAG ni de publicación; I4 (marco de rama, guarda de reutilización, libro versión 7) no se toca |

### 1.4 Alcance mínimo, riesgos y pruebas

- **Ficheros:** `LocusPairIdentity2D`, `LocusPairIntersectionCandidateSet2D`,
  `LocusPairIntersectionSolver2D`, `EvaluatorPairIntersectionCapability2D`,
  `PiecewisePolynomialPairIntersectionCapability2D`, `LocusPairIntersectionContext2D`,
  `PublicSplinePairRootIdentityResolver2D`, `PublicConditionedPairRootIdentityResolver2D`,
  `LocusIntersectionTokenLedger2D`; `spline-v2-pair-materialization.md`,
  `locus-v2-extended-intersections.md`, `locus-v2-public-surface.md`.
- **Riesgos:** huellas de base de las pruebas de pares (las claves locales aparecen en
  diagnósticos); la adopción de entradas heredadas es el punto delicado y se prueba con un
  documento generado por la base, no por el candidato.
- **Pruebas discriminantes** (fallan hoy, pasan después):
  1. 2 × 2 con clave de rama compartida: `SUCCESS`, raíces y entradas `P`.
  2. La configuración de `ReviewPairWrongProbe`: el punto queda sobre su raíz o sin definir,
     nunca en (2,826; 1); la otra raíz recibe token propio.
  3. La de `ReviewPairIndexProbe`: token y punto conservados al reordenarse una pieza ajena.
  4. División y fusión de la componente de la raíz: dormido y recuperado.
  5. Documento de la base con una componente por lado: libro idéntico byte a byte.
  6. Documento de la base con entradas de varias componentes: adoptadas, token conservado,
     punto en la misma raíz; segunda lectura sin cambios.
  7. Deshacer y rehacer a través del cambio topológico; guardar en el estado cambiado y
     reabrir.
  8. Simetría: `Intersect(A, B)` e `Intersect(B, A)` dan el mismo selector.
- **Criterio de aceptación:** las ocho pruebas; ninguna prueba de pares existente cambia de
  expectativa salvo las de diagnóstico de clave local; `STATIC`, `PHASE POST-R1-LOCUS` e
  `INTEGRATION` aceptados.

## 2. Admisibilidad de `LocusLength` condicionado

### 2.1 Diagnóstico

`LocusMetricResult2D.isScalarAdmissible()` exige cobertura geométrica `COMPLETE`, salvo el
recorrido numérico tipado de P2. `asConditioned` (I3) publica toda longitud medida sobre
regiones con cobertura `INCOMPLETE` y sin recorrido tipado, y `locus-v2-metrics.md` §28.3
dice que la admisión «is a separate decision of admissibility, which this amendment does not
take». Ese es el único bloqueo.

**Medido** (sonda `ReviewLengthProbe` y observador sobre los JAR del candidato):

| Caso | Estado | Valor | Error estimado / tolerancia | Cobertura | Escalar |
|---|---|---|---|---|---|
| Modelo del autor, `LocusLength(w_2, P, Q)` en una región | `SUCCESS`, `ESTIMATED`, `RECTIFIABLE`, `TARGET_REACHED` | 1,3140010178 | 2,5·10⁻¹¹ / 1,3·10⁻⁹ | `INCOMPLETE` | sin definir |
| Modelo del autor, `LocusLength(w_2)` | `SUCCESS`, subtotal de 2 regiones, 4 bandas de transición omitidas | 8,8068123 | 2,6·10⁻⁹ / 7,1·10⁻⁹ | `INCOMPLETE` | sin definir |
| `(x, eˣ)` en [0, 1], `LocusLength(L)` (H10) | `SUCCESS`, `ESTIMATED` | 2,0034971116 | 9,0·10⁻¹² / 2,0·10⁻⁹ | `COMPLETE` | **definido** |
| El mismo lugar, `LocusLength(L, A₀, B₁)` entre sus extremos | `SUCCESS`, `ESTIMATED` | 2,0034971116 | 9,0·10⁻¹² / 2,0·10⁻⁹ | `INCOMPLETE` | sin definir |
| Posiciones en regiones distintas, o una dentro de una banda | `INVALID_QUERY` | — | — | — | sin definir (correcto) |

Las dos filas de `(x, eˣ)` son el mismo número con la misma evidencia: hoy la norma lo admite
como total y lo rechaza como ruta. En el modelo del autor el estimador ya converge dos
órdenes por debajo de la tolerancia. Causa de cobertura de `w_2` y `a_3`: `NOT_CERTIFIED`
genérica; sus tramos son de clase `RESOLUTION_ONLY`.

Hallazgos laterales, medidos:

- Una posición colocada exactamente en un extremo de región se rechaza con
  `POSITION_IN_BAND` (`LocusConditionedEvidence2D.regionOf` excluye el nodo que comparte con
  la banda), de modo que hoy no se puede medir una región entera.
- Un lugar fuera de clase con una ausencia (`(x, √(x−0,3)+eˣ)`) da en `LocusLength(L)`
  `NUMERICAL_FAILURE / EVALUATION_FAILED`: toma el camino H10 del dominio declarado, que
  falla, y no ofrece el subtotal sobre su región. Es coherente con «H10 y P2 no cambian»,
  pero el resultado rico es peor que el de un lugar con causa `NOT_CERTIFIED`.

### 2.2 Qué es cada cosa

| Concepto | Qué afirma | Qué no afirma |
|---|---|---|
| **Valor numérico** | El resultado del estimador v2 sobre el tramo de la ruta | Nada sobre la existencia del lugar entre nodos |
| **Garantía de error** | Clase `ESTIMATED`: error **estimado** ≤ tolerancia, bajo las hipótesis del estimador (§25.5) | No es una cota rigurosa; nunca `CERTIFIED` |
| **Evidencia condicionada** | Los nodos evaluados son válidos, con su testimonio de selección, y los criterios de la política `conditioned-traversal/v1` se cumplen entre ellos; las bandas quedan fuera de la ruta | Entre nodos, existencia, selección, continuidad y resolución son las hipótesis H1 a H4, declaradas |
| **Certificación rigurosa** | No hay ninguna: la estructura de existencia sigue `NOT_ESTABLISHED` | El escalar no certifica el lugar, no completa su cobertura y no convierte la región en componente |

### 2.3 Qué dicen los contratos

- El contrato `conditioned-use/v4` §8.3 fija para la longitud entre dos posiciones la
  garantía «at most `ESTIMATED`» y no dice nada del escalar. No hay que tocarlo.
- `locus-v2-metrics.md` §25.4 ya admite como escalar una estimación con error estimado ≤
  tolerancia.
- §26.6 (P2, decisión científica del autor de 2026-11-17) excluye para **el total de un
  dominio declarado** la causa `NOT_CERTIFIED` genérica, las bandas de tolerancia nativa, la
  selección incierta y los presupuestos agotados, y afirma que un muestreo con éxito no
  establece continuidad. Esas exclusiones son del modo P2 y siguen vigentes.
- La extensión que propongo es un modo distinto y aditivo: no admite un total, admite una
  ruta dentro de **una** región, y descansa en una evidencia que P2 no tenía (nodos con
  testimonio y bandas aisladas al paso mínimo). Aun así toca una materia que el autor decidió
  expresamente, y por eso es DN-2 y no una enmienda de oficio.

### 2.4 Extensión propuesta (DN-2)

Nuevo §28.3, versión `conditioned-length-scalar/v1`. Una longitud **entre dos posiciones** es
admisible como escalar cuando se cumplen todas:

1. Las dos posiciones están en una misma región condicionada y la ruta no sale de ella.
2. Todas las celdas de la ruta son tramos; la clase de cada tramo se registra.
3. Ninguna cobertura de la rama que solape la ruta tiene una causa de la lista excluida, que
   son aquellas en las que el certificador identifica un posible evento:
   `INHERITED_TOLERANCE_BEHAVIOUR`, `SELECTION_NOT_REPRODUCED`, `DEGENERATE_CONFIGURATION`,
   `BUDGET_EXHAUSTED` y la falta de fidelidad de la reconstrucción. La lista exacta se cierra
   con una sonda sobre el corpus antes de escribir la norma.
4. Estado `SUCCESS`, rectificable, valor finito, resultado de ruta `TARGET_REACHED` o
   `WRAPPED_TO_START`.
5. Garantía `ESTIMATED_ERROR` con tolerancia registrada y error estimado ≤ tolerancia.
6. Evidencia tipada vigente: revisión semántica, identidad de contenido de la evidencia
   condicionada y versión de la política coinciden con las actuales.

El resultado rico conserva cobertura `INCOMPLETE`, clase `ESTIMATED`, el diagnóstico
`EXISTENCE_NOT_CERTIFIED` y la evidencia tipada (rama, región, ruta, identidad de contenido,
política). La admisión se implementa como una segunda evidencia tipada junto a la de P2 en
`isScalarAdmissible()`, con las mismas guardas; ninguna otra condición de admisión cambia.

Además, una posición situada en el nodo extremo de una región pertenece a la región (el nodo
es una muestra válida del tramo): así se mide una región entera entre sus dos extremos.

### 2.5 Comportamiento definido

| Situación | Resultado rico | Escalar |
|---|---|---|
| Ruta dentro de una región, condiciones 1 a 6 | `SUCCESS`, `ESTIMATED`, condicionado | **número** |
| Ruta que cruzaría una banda de cualquier clase | `POSITIONS_IN_DIFFERENT_REGIONS` o `ROUTE_CROSSES_BAND` con las clases de banda | sin definir |
| Discontinuidad detectada | Es una banda: el caso anterior | sin definir |
| Posición dentro de una banda o de una celda sin explorar | `POSITION_IN_BAND` | sin definir |
| Una posición en componente certificada y otra en región | No hay ruta común | sin definir |
| Dominio parcial: total con bandas omitidas | `SUCCESS`, subtotal con la lista de bandas | sin definir (D1) |
| Total sin bandas | Sigue rigiéndose por §26.5 y §26.6 | como hoy |
| Error estimado > tolerancia, fallo de evaluación, presupuesto | Estado y causa propios | sin definir |
| Pérdida de validez: contradicción confirmada, nueva revisión, otra política | La identidad de contenido cambia; la longitud se recalcula por el DAG | sin definir o número nuevo; nunca el anterior |
| Causa de la lista excluida | `SUCCESS` condicionado | sin definir, con diagnóstico de la causa |
| Documento reabierto | Se recalcula; el escalar no persiste valor | igual que antes de guardar |

Propagación: un escalar condicionado que alimenta otra construcción es, para el certificador,
una estimación y no una entrada rigurosa (§26.3, ya probado para H10): el lugar que dependa
de él queda fuera de clase y se usa a su vez de forma condicionada. No se propaga ninguna
afirmación de certificación.

### 2.6 Lo que no propongo para 1.2.2

`LocusLength(L)` de un lugar con huecos es siempre un subtotal: sus bandas de transición, de
paso mínimo, no tienen contribución ni cota. Admitirlo como escalar ordinario haría que una
fórmula lo leyera como longitud total. Con la corrección de extremos, la longitud de cada
región se obtiene con `LocusLength(L, A, B)` y su suma la escribe el usuario. Un escalar de
subtotal exigiría una forma pública explícita y un cambio de `locus-v2-public-surface.md`; es
diferible (D1). Tampoco propongo cambiar H10 para que ofrezca el subtotal cuando su
estimación falla.

### 2.7 Alcance, riesgos y pruebas

- **Ficheros:** `LocusMetricResult2D`, `LocusMetricEngine2D`, `LocusMetricRouteResolver2D`,
  una clase nueva de evidencia tipada, `LocusConditionedEvidence2D.regionOf`;
  `locus-v2-metrics.md` §28.3 y §28.1; guía del usuario §6.9 (dice que la longitud
  condicionada no alimenta fórmulas).
- **Riesgos:** las pruebas que hoy afirman `assertFalse(isScalarAdmissible())` sobre rutas en
  región pasan a afirmar lo contrario (I3, I4T): es el cambio de comportamiento aprobado, no
  una relajación. Las pruebas de P2 sobre totales no deben cambiar.
- **Pruebas discriminantes:**
  1. `LocusLength(L, A, B)` en una región: escalar definido, igual al valor rico, y
     `2*a+1` definido.
  2. Modelo del autor (Desktop): `LocusLength(w_2, P, Q)` definido; tras un cambio real de
     `excCyl`, recalculado.
  3. Región entera entre sus extremos; extremo compartido con banda.
  4. Cada fila «sin definir» de §2.5.
  5. Las pruebas de P2 `opaqueRangeMayNotHideNonTotalConsumers` y
     `knownNativeJumpsRemainInadmissible`, sin tocar, siguen pasando.
  6. Un salto de 9·10⁻⁴ dentro de una ruta: sin escalar (hoy ya lo rechaza el estimador).
  7. Contradicción confirmada dentro de la ruta: el escalar deja de estar definido; fuera de
     ella, se conserva.
  8. Un lugar que dependa del escalar queda `NOT_ESTABLISHED` y nunca certificado.
  9. Guardar y reabrir; deshacer y rehacer.
- **Criterio de aceptación:** las nueve; la estructura de existencia de la fuente es idéntica
  antes y después de medir.

## 3. Rendimiento D-V6

### 3.1 Medición

Observador externo sobre los JAR de la app-image del candidato, documento original del
autor, JDK 25, sin ventana, 6 cambios de calentamiento y 12 medidos de `excCyl` (+0,05 y
vuelta); en cada cambio: actualización del grafo, evidencia de `w_2`, evidencia de `a_3`,
pintura. Tres ejecuciones.

| Fase (ms, **medido**) | Mediana | Mínimo | Máximo |
|---|---|---|---|
| Grafo | 518 a 535 | 506 | 574 |
| Evidencia de `w_2` | 355 a 368 | 333 | 394 |
| Evidencia de `a_3` | 371 a 384 | 359 | 397 |
| Pintura | 7 | 6 | 9 |
| **Total** | **1 256 a 1 308** | 1 211 | 1 347 |

El objetivo D-V6 (< 1 s, también en p95) no se cumple con la evidencia consumida.

### 3.2 Dónde se va el tiempo

Contadores por cambio (**medido**):

| Trabajo | Cantidad |
|---|---|
| Revisiones publicadas | `o`, `v`, `w`, `a_1`, `w_2`, `a_3`: una cada uno; `o_1`, `e_2`, `s_2`, `e_1`: ninguna |
| Copias aisladas reconstruidas | una por lugar con revisión; en `w_2` y `a_3`, 7 copias anidadas cada una |
| Evaluaciones de nivel superior de `w_2` y `a_3` | certificado: 128 cada uno; recorrido: ≈ 363 y ≈ 381 |
| Evaluaciones anidadas | grafo: 82 046 por lugar; evidencia: 209 274 (`w_2`) y 221 096 (`a_3`) |

Cada evaluación de `w_2` en un parámetro recalcula, dentro de su copia, la intersección
`Intersect(o, q_1)` y dos longitudes sobre `o_1` y `e_2`. Atribución de las evaluaciones
anidadas del recorrido de `w_2` (**medido**, sesión instrumentada con una clase sombra):
186 001 evaluaciones de `o` pedidas por la intersección (≈ 512 por evaluación de `w_2`) y
23 273 de `o_1` y `e_2` pedidas por la métrica.

Perfil JFR del hilo de eventos, 20 cambios, porcentaje del total (**medido**; categorías
exclusivas):

| Quién lo pide | % | Qué hace dentro | % |
|---|---|---|---|
| Recorrido condicionado | 57 | Evaluación de las copias (propia y anidada) | 39 |
| Certificado | 25 | Trabajo propio del solucionador de intersección en la copia | 23 |
| Métrica viva | 11,5 | Resumen de huella (`ReconstructionSliceFingerprint2D`) | 18 |
| Resto (construcción de copias, XML) | 5,4 | Construcción de copias y XML | 7 |
| Pintura | 0,4 | Motor métrico | 7 |

### 3.3 Operaciones repetidas

1. **La misma evaluación anidada, pedida una y otra vez (medido).** `o` no depende del
   conductor de `w_2`: entre dos evaluaciones de `w_2` de la misma revisión, su copia de `o`
   no cambia, pero la intersección la vuelve a muestrear entera. Con un memo por instancia de
   evaluador, las evaluaciones anidadas realmente calculadas bajan de 209 274 a 41 844 en el
   recorrido de `w_2` (−80 %) y de 82 046 a 28 018 en el grafo (−66 %).
2. **El resumen de huella en cada evaluación (medido como 18 % del perfil; su reducción es
   estimada).** Los lugares anidados cuyo conductor es el del consumidor recalculan el
   resumen de valores de su slice cada vez que el conductor se mueve, para concluir que nada
   cambió. La parte de estructura ya se reutiliza (D-V6); la de valores construye cadenas y
   aplica SHA-256.
3. **Certificado y recorrido del mismo lugar (medido).** De las ≈ 363 evaluaciones del
   recorrido de `w_2`, 70,5 por cambio (19 %) repiten un parámetro que el certificado de la
   misma instancia ya evaluó. No son reutilizables hoy porque el certificado no guarda el
   testimonio.
4. **La copia aislada se reconstruye entera por revisión (medido: una por lugar).** Un cambio
   de `excCyl` es solo de valores, pero se crea una copia nueva y con ella se recalcula lo que no
   depende de `excCyl`, como los índices métricos de `o_1` y `e_2`: en la fase de grafo la
   métrica pide esas evaluaciones, de las que unas 16 800 ocurren al construir las copias (≈ 50 000 evaluaciones por
   cambio, medido).
5. **Trabajo de identidad en cada resolución anidada (estimado a partir del 23 %).** La
   intersección dentro de la copia resuelve identidad pública y tokens en cada evaluación.

### 3.4 Prototipo medido de la palanca 1

Fuera del repositorio, con una clase sombra de `LocusEvaluationSession2D` delante de los JAR
del candidato: memo de `(lugar, revisión, rama, parámetro)` propio de cada instancia de
evaluador (su `sliceScope`), solo para sesiones de servicio anidadas; el presupuesto de
evaluaciones se sigue cargando igual.

| (ms, **medido**) | Sin memo | Con memo |
|---|---|---|
| Grafo, mediana | 518 a 535 | 484 a 493 |
| Evidencia de `w_2`, mediana | 355 a 368 | 223 a 239 |
| Evidencia de `a_3`, mediana | 371 a 384 | 245 a 261 |
| **Total, mediana** | **1 256 a 1 308** | **964 a 1 005** |
| Total, máximo | 1 347 | 1 047 |

**Igualdad de resultados, medida:** en las tres ejecuciones (dos sin memo, una con memo) y en
los dos valores de `excCyl`, son idénticos los resúmenes de valores de todos los objetos, de
las estructuras de existencia de todos los lugares, de la evidencia condicionada (identidad
de contenido y partición en celdas) y de 24 puntos evaluados por lugar. El prototipo es una
medición, no código del producto; su memo no está acotado (≈ 19 700 entradas por instancia).

### 3.5 Alternativas

| # | Alternativa | Ganancia | Riesgo y coste | Norma |
|---|---|---|---|---|
| 1 | **Memo de evaluaciones anidadas por instancia de evaluador.** Propio del evaluador, muere con él, resultados inmutables, clave con revisión, presupuesto cargado igual | **Medida: −0,26 a −0,30 s** | Bajo. Acotarlo; modo de verificación que recalcula y compara; las pruebas que cuentan evaluaciones anidadas cambian de cifra | Ninguna. No es caché compartida entre productores ni copia mutable compartida; aun así es un diseño que el autor debe aprobar (DN-3) |
| 2 | **Resumen de valores más barato** para los lugares anidados de la copia (misma información, sin cadenas ni SHA-256 por evaluación) | Estimada: −0,10 a −0,17 s | Medio: es la guarda contra geometría obsoleta; se valida con el modo `verifyMemo` existente y con `PostR1NestedLocusDriverFingerprintTest` | Ninguna |
| 3 | Modelo de la fuente por revisión dentro de la intersección anidada (no repetir identidad y tokens en cada evaluación) | Estimada: hasta −0,2 s | Alto: toca el código de identidad | Ninguna, pero requiere diseño propio |
| 4 | Sincronizar valores en la copia existente en vez de reconstruirla | Estimada: −0,1 a −0,15 s | Alto: hoy el evaluador de una revisión es inmutable (`locus-v2-semantics.md` v1.2); haría falta retirar el anterior de forma que falle cerrado | Probable enmienda |
| 5 | Que el recorrido reutilice las evaluaciones del certificado | Estimada: ≤ 19 % del recorrido | Medio: el certificado tendría que guardar testimonio | Ninguna |
| 6 | Reducir la profundidad o el paso de la política `conditioned-traversal/v1` | — | Cambia la evidencia y su identidad | **Sí. No la propongo** |
| 7 | Caché compartida entre `w_2` y `a_3` (piden las mismas evaluaciones) | Grande | Excluida por D-V6 sin diseño aprobado | No la propongo |

Recomendación: 1 y 2 para 1.2.2. Objetivo de aceptación: con los dos lugares con hueco
dibujados, **mediana ≤ 0,85 s y p95 < 1 s** en el documento original y en el transportado. Si
1 y 2 no lo alcanzan, la 5; las 3 y 4 son diferibles (D2).

Lo que se preserva: la política de evidencia y la identidad de contenido de la evidencia; las
revisiones y su publicación; el DAG; la determinación de invalidez (una clave de memo incluye
la revisión del lugar anidado, y un lugar que cambia publica otra); los presupuestos de
trabajo, que se cargan igual haya acierto o no, de modo que ninguna clasificación depende de
lo que el memo contenga.

### 3.6 Pruebas

1. Igualdad: huellas de `PostR1NestedLocusDriverFingerprintTest` y resúmenes de §3.4,
   idénticos con y sin memo.
2. Modo de verificación (propiedad del sistema, como `geocedg.fingerprint.verifyMemo`): con
   él activo pasan todas las pruebas compartidas de locus.
3. Invalidez: una definición anidada que sigue al conductor externo nunca se sirve del memo
   (la composición de F8); un lugar anidado que publica revisión dentro de la misma copia
   deja de acertar.
4. Presupuesto: un caso que agota el límite de trabajo lo agota igual con y sin memo.
5. Aislamiento: dos productores no comparten ninguna entrada; el memo de un evaluador
   retirado no es alcanzable.
6. Banco: escenario `conditioned` del arnés, ampliado para dar el total por cambio.

## 4. Regresiones

### 4.1 Pruebas adaptadas: la lista exacta

En el cierre hablé de «cinco pruebas adaptadas». Es incorrecto. Entre `349c1856c` (antes del
bloque 3B) y la punta hay **14 ficheros de prueba modificados** y 11 nuevos:

| Fichero | Incremento | Qué cambió | Juicio |
|---|---|---|---|
| `G9U0GeneratorSuiteTest` (g04) | I1 | Los puntos de un lugar fuera de clase pasan de «sin definir» a definidos por parámetro, con coordenadas y clase de dirección afirmadas; la no existencia certificada sigue sin punto | Conforme; más estricta |
| `M08PointCreationKernelTest` | I1 | `REJECTED` → `CONDITIONED_PARAMETER_ADDRESS`; sin permiso M08, sin selección local, XML intacto | Conforme. Renombrada; el nombre anterior no se cita en ninguna matriz |
| `PreG9BCu2ConditionedEstimateTest` | I1 | Punto aislado definido por parámetro; añade que la estructura no cambia | Conforme; más estricta |
| `PreG9BCu2RepresentationTest` (2 casos) | I2 | Evidencia exploratoria punteada → regiones con el trazo del objeto | Conforme con el contrato §9, pero **N3** y **N4-b** |
| `G9U0PublicSurfaceTestBase.assertNoTotalWithoutExistence` | I3 | Admite un total condicionado no escalar | Conforme, pero **N4-a** |
| `G9U0R4IntersectionAdmissibilityContinuationTest` | I4 | «Ninguna raíz» → «ninguna raíz admisible» y completitud `NOT_ESTABLISHED` | Conforme, pero **N4-d** |
| `PreG9bR3X1ExpressionPointPairMaterializationTest` | I4 | Raíces en región con enlace de marco de rama; nunca una ranura D2 | Conforme, pero **N4-c** |
| `PreG9bR3X1RedefineWitnessTest` | I4 | La raíz en región nunca lleva el token de la reclamación retenida | Conforme, pero **N4-d** |
| `PreG9BS1AuthorDxfReproductionTest` (Desktop) | I4, corrección | El resultado declarado junto al emitido | Conforme, pero **N4-e** |
| `PostR1ConditionedEvidenceTest` | I0 a I4 | Solo casos nuevos; uno fija el límite declarado del salto a lo largo de la cuerda | Sin debilitamiento |
| `GeoCeDGProfileTest`, `PreG9BP0PresentationThemeTest`, `PreG9BP1PublicSurfaceTest`, `PreG9BR6PlusGProductMetadataTest` | Candidato | Versión, fecha y número de secciones de la guía (102 → 103) | Metadatos |

### 4.2 Invariantes que dejaron de fijarse

- **N3.** `pre_g9b_cu2_validation_matrix.md`, fila V-B07-4, y el registro de conformidad T11
  citan `exploratoryEvidenceIsDrawnApartAndOnlyInsideItsBands` y
  `exploratoryEvidenceIsDrawnDottedAndTranslucent`, que I2 renombró. La fila de una matriz
  aprobada cita hoy pruebas que no existen. Corrección: restituir los dos nombres, como hice
  con `anUncertifiedExpressionIssuesNoPairToken`, con el javadoc enmendado. La matriz no se
  toca.
- **N4-a.** El ayudante ya no fija `INVALID_QUERY` ni `INCOMPLETE_AGGREGATE` en la rama sin
  valor: acepta cualquier estado con algún diagnóstico. Debe afirmar una lista cerrada de
  estados y códigos.
- **N4-b.** Para `lr2` se eliminó la afirmación de que la banda junto a la raíz coincidente
  se dibuja, sin sustituirla.
- **N4-c.** Las afirmaciones son condicionales al resultado (`assertEquals(eligible.isEmpty(),
  ledger.startsWith("4|"))`): la prueba pasa haya o no raíces. El caso es determinista y debe
  fijar el resultado que se obtiene.
- **N4-d.** Los bucles sobre las raíces no afirman cuántas hay; con cero raíces no comprueban
  nada.
- **N4-e.** Se eliminó `assertEquals(1, getIncompleteSemanticCoverageCount())` en lugar de
  fijar el valor nuevo.

Ninguna de estas adaptaciones oculta un fallo: en todos los casos el comportamiento afirmado
es el aprobado. Lo que se perdió es poder discriminar.

### 4.3 Corrección DXF (`048ec4edf`)

El codificador estricto de `DxfExporter` rechazaba la exportación entera cuando un resultado
no emitido acompañaba a los emitidos. I4 añadió, por el contrato §8.10, un resultado
**declarado** (`Fidelity.DECLARED`, razón `EXISTENCE_NOT_ESTABLISHED`, con la lista de
regiones) junto a la componente certificada. El cambio es una condición: un resultado
declarado no tiene entidad y no bloquea la petición estricta. Las componentes certificadas se
exportan como antes; las regiones condicionadas no se exportan nunca; el sidecar lo declara.
Un resultado no emitido de cualquier otra fidelidad sigue bloqueando. La prueba de
reproducción del autor lo afirma. Juicio: correcta y mínima; pendiente N4-e.

### 4.4 Evidencia `SAFEGUARDED_REGULAR_JACOBIAN`

Qué es: para una raíz transversal con un lado en región, el resolvedor de pares condicionados
evalúa tres puntos por curva en un rectángulo de 1/1024 del lugar de la raíz en cada fuente,
estima el jacobiano por diferencias centrales y comprueba que su orientación es constante y
que la diferencia de las curvas cambia de signo entre lados opuestos. Nueve evaluaciones en
coma flotante.

Qué no es: no es un recinto certificado, no cubre el rectángulo de forma exhaustiva y no
prueba unicidad. La norma lo dice (`locus-v2-extended-intersections.md`: «numerical evidence
under the hypotheses of the spans, of guarantee `FLOATING_POINT_UNCERTIFIED`, and never a
certified enclosure») y el resultado lo declara: `CONDITIONED_REGION_ROOT` en cada raíz,
completitud `NOT_ESTABLISHED`, y bajo autoridad constructiva la admisión de la raíz queda no
establecida. Los valores de enumeración existían reservados antes de I4; I4 es su primer uso.

**N5.** `LocalPairIsolationEvidence2D` obliga a que un aislamiento `ESTABLISHED` lleve
cobertura `EXHAUSTIVE_RECTANGLE`, de modo que la evidencia numérica se publica con las mismas
palabras que el rectángulo de Krawczyk. Quien lea solo el estado
(`LocusIntersectionResult2D`, admisión de punto) no distingue los dos. No he encontrado ningún
consumidor que eleve una garantía por ello, pero la distinción depende de leer el método y la
garantía. Corrección: un valor de cobertura propio para el rectángulo muestreado bajo
hipótesis, y una prueba de que ningún consumidor (exportación, completitud, autoridad
constructiva) trata esa evidencia como certificada. No requiere cambio de norma.

## 5. Validación y empaquetado

### 5.1 Los dos fallos de Desktop frente a la base

Regresión completa de Desktop sobre el código de I4T, en un worktree: 1 568 pruebas, 2
fallos.

| Prueba | En la base `9ccefdcfe` | Causa | Atribución |
|---|---|---|---|
| `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` | **Falla igual** (comprobado el 2026-11-21 en un worktree limpio de la base) | Espera `seam=[INTERIOR]` para `e_2` y obtiene `seam=[]` | Previo. No está en ninguna selección CORE; solo lo vería `FINAL`. No se ha corregido ni reetiquetado, y sigue visible. Bloquea el refresco de la selección `final.desktop` |
| `G9U1Revision3AlgebraEditingDiagnostic` | Pasa en el checkout principal | Lee un archivo del autor ignorado por Git, que no existe en un worktree | Entorno del worktree; no es un fallo del producto |

### 5.2 Regresión compartida completa

El registro del bloque 3B dice «compartida sin fallos». Para I4T, la única ejecución completa
conservada (2 495 pruebas) tuvo un fallo en una prueba nueva mía, que ajusté después; la
ejecución completa no se repitió, y la corrección solo quedó cubierta por `PHASE` e
`INTEGRATION`. En esta revisión la he repetido sobre el árbol del candidato:
`gradlew :shared:common-jre:test --tests org.geocedg.*` sobre `035806584` (el candidato más
el registro documental), **2 495 pruebas en 256 clases, 0 fallos, 0 errores, 0 omitidas**,
código 0, 217 s. Un primer intento quedó anulado por un error mío de procedimiento (lancé una
sonda de Gradle sobre el mismo checkout mientras corría y Gradle perdió su fichero de
resultados); no fue un fallo de prueba y lo repetí a solas. No es un perfil del verificador y
no equivale a `FINAL`.

### 5.3 Diferencia entre I4T y el candidato

`0f02c8f21` → `b2c5aad35`: 12 ficheros, ninguno de código de producto.
`packaging/windows/package.yml` (versión y fecha), sus dos pines, cuatro pruebas de Desktop
que fijan la versión, las tres guías (sección 6.9), el documento de rendimiento §7 y el
registro de progreso. `b2c5aad35` → `035806584`: solo el registro de progreso.

Verificado sobre `b2c5aad35`: `STATIC`, `PHASE POST-R1-LOCUS`, `INTEGRATION` y `PACKAGING`
(identificadores en §8), y un humo sobre los JAR de la app-image.

**No verificado:**

- Lanzamiento del ejecutable empaquetado (`GeoCeDG.exe` de la app-image).
- Instalación, arranque y desinstalación del MSI y del EXE.
- Regresión completa de Desktop sobre el commit del candidato (se hizo sobre I4T; la
  diferencia son las cuatro pruebas de versión, que sí se ejecutaron).
- Latencia interactiva en una ventana real.
- `FINAL`.

Como el plan de §7 produce un candidato sucesor, propongo hacer estas comprobaciones sobre
él y no sobre `b2c5aad35`. Los paquetes actuales se conservan como evidencia.

## 6. Gobernanza

Comprobado en disco, en la rama:

| Hecho | Dónde |
|---|---|
| Aprobación normativa del ADR 0042, revisión 4, y del contrato `conditioned-use/v4-candidate` del commit `9160d67d7` | `docs/validation/post_r1_conditioned_use_author_approval_record.md` (commit `2ee7aae26`), §2 |
| Autorización de I0, I1, I2, I3, I4 e I4T y de preparar el candidato 1.2.2 | El mismo registro, §3 |
| Lo no autorizado: `main`, etiqueta `v1.2.2`, release, `FINAL` | El mismo registro, §4 |
| Estado del ADR | `ACCEPTED — ARCHITECTURE AUTHOR APPROVED (2026-11-22)`, con la salvedad de que la implementación no está aprobada como producto |
| Estado del contrato | `NORMATIVE CONTRACT — AUTHOR APPROVED (2026-11-22); IMPLEMENTATION AUTHORIZED … NOT YET APPROVED AS PRODUCT` |
| Hoja de ruta | Entrada aditiva «2026-11-22 — Aprobación autoral del ADR 0042 y autorización del bloque 3B» |

Observaciones:

1. El registro es efectivo **en la rama local**; nada de ello está en `main` ni en el remoto,
   porque no hay publicación autorizada.
2. El contrato aprobado sigue en `geocedg/specs/locus/candidates/` y se titula «candidate
   contract». Moverlo es una decisión de gobierno que no he tomado.
3. Ningún registro declara aprobado un incremento ni el candidato.
4. La decisión de hoy de mantener `2026-11-22` como fecha de versión aún no está anotada: el
   checkpoint 8 la llama «provisional». Es una anotación documental pendiente de su orden.

## 7. Plan correctivo

Cada paso se cierra como los incrementos del bloque 3B: commit de código, commit de
inventario en un worktree aparte, `STATIC`, `PHASE POST-R1-LOCUS` e `INTEGRATION`, y avance
de la rama solo con resultado aceptado.

| Paso | Contenido | Depende de | Decisión previa |
|---|---|---|---|
| C0 | Pruebas: N3, N4-a a N4-e. Sin código de producto | — | DN-4 |
| C1 | B2: clave local de revisión con lado; retirada de las claves de región con sufijo | — | Ninguna (no normativa) |
| C2 | B1: linaje estructural en el selector D2, contrato `…/v2`, adopción de entradas heredadas; enmienda de `spline-v2-pair-materialization.md` | C1 | **DN-1** |
| C3 | N5: vocabulario del aislamiento jacobiano | C1 | Ninguna |
| C4 | N1: admisión escalar de la ruta en región y extremos de región; enmienda de §28.3; guía §6.9 | — | **DN-2** |
| C5 | N2, palanca 1: memo por instancia de evaluador, con modo de verificación | — | **DN-3** |
| C6 | N2, palanca 2: resumen de valores | C5 | DN-3 |
| C7 | Candidato sucesor: banco D-V6, regresión completa compartida y de Desktop, paquetes, ejecutable e instaladores | C0 a C6 | Fecha de versión que el autor designe |

Orden recomendado: C0, C1, C2, C3, C4, C5, C6, C7. C1 a C3 y C4 son independientes entre sí
y de C5 y C6; los ordeno así para que los cambios de identidad, que son los de mayor riesgo,
tengan más ejecuciones de regresión por detrás. Si el autor aprueba solo parte de las
decisiones, los pasos sin decisión pendiente (C0, C1, C3) pueden hacerse igualmente.

Tras C7 siguen requiriendo una decisión expresa: la aceptación del candidato, `FINAL`,
`main`, la etiqueta y la release.

## 8. Trazabilidad

**Verificaciones conservadas** (todas `ACCEPTED / COMPLETE`, código 0):

| Commit | `STATIC` | `PHASE POST-R1-LOCUS` | `INTEGRATION` |
|---|---|---|---|
| I2 `45ab0157d` | `verification-358032b278104ce89254cfeb414145fc` | `verification-fa0c456e389c41baa81ebe9fc5cef444` | `verification-0966b2ebe7234c63b52a75939cb486d2` |
| I3 `c7e1f2a56` | `verification-37fb707973ed418e894865071ad1d2fe` | `verification-a002cd94974a4b8695f7c9ce6eb7bddc` | `verification-c494bf71f8ad46b8bd79d10296656a07` |
| I4 `223e1d6de` | `verification-53949bc6a1734ceda32cf2b88ec6d093` | `verification-50398e1da5264506b9f19ac5614680d7` | `verification-80480162e8b747f2b92ce583a4d03b53` |
| I4T `0f02c8f21` | `verification-47684adf4b90459091b0df73d8ec1b27` | `verification-ee22fb8aa8cd49c8adbf14a23315f02e` | `verification-e41bdd5c26a44ceea4c741664f1000c5` |
| Candidato `b2c5aad35` | `verification-130b611c356b462ca9fb89549ac209d7` | `verification-c9b4beb3cb1346b79c3ad735d9890fd4` | `verification-2b6e6b612f7e412195921b8e824430bd` |

`PACKAGING` del candidato: `verification-e85784e657744ce5a50eae7a69daadf1`. Paquetes en
`artifacts/packaging/windows-1.2.2-nc-candidate` (ignorado por Git): ZIP
`50147389…a38000`, MSI `3ee9af0a…14ca35f`, EXE `a45adbd0…84537d5`.

**Worktrees conservados** (`git worktree list`): `wt-i2`, `wt-i3`, `wt-i4`, `wt-i4t` y los
anteriores `base-9ccefdcf` y `cand-ab37fb5f`. No he eliminado ninguno.

**Sondas y mediciones de esta revisión**, todas fuera del repositorio, en el scratchpad de
la sesión (`…\scratchpad\review` y `…\scratchpad\smoke`); el scratchpad puede borrarse en una
limpieza del sistema, así que las conclusiones que importan están en este informe:

| Sonda | Qué midió | Sobre |
|---|---|---|
| `ReviewPairKeyProbe` | B2: 2 × 2, 2 × 1 | candidato y base |
| `ReviewPairIndexProbe` | B1: pérdida de identidad | base |
| `ReviewPairWrongProbe` | B1: asociación incorrecta | candidato y base |
| `ReviewCrossVersionSave` / `ReviewCrossVersionLoad` | Documento de la base abierto en el candidato | base → candidato |
| `ReviewLengthProbe`, `ReviewPerf` modo `length` | §2.1 | candidato; JAR del candidato |
| `ReviewPerf`, `ReviewCounts`, `ReviewStructure`, clases sombra | §3 | JAR del candidato `b2c5aad35` |

Las sondas se ejecutaron con un script de inicio de Gradle que añade sus fuentes al conjunto
de pruebas sin escribir en el árbol, y con `java` directamente sobre los JAR. Las cifras de
rendimiento son de una estación, sin ventana y en series cortas: sirven para comparar y para
fijar un objetivo, no como umbral certificado.
