# PRE-G9B-MAINT-A2 — Informe del candidato técnico

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
PRODUCT_PHASE_EFFECT      = PRE-G9B-MAINT-A2
GUIDE_IMPACT              = UPDATE (sección 7, subsecciones 7.8 y 7.9; referencia de comandos)
BOOTSTRAP_IMPACT          = NO CHANGE
FINAL                     = NOT AUTHORIZED
VERIFICATION_ESCALATION   = REQUESTED (FINAL o dispensa explícita del autor; §7.1)
```

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-A2`: familia B-spline polinómica `bspline-2d/v1`, comando `BSpline` |
| Autorización | instrucción del autor de 2026-11-24 («Segunda ola coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2»), coordinada |
| Prompt | [`pre-g9b-maint-a2-bspline-kernel-family`](../../.github/prompts/tasks/pre-g9b-maint-a2-bspline-kernel-family.prompt.md), commit de gobernanza `97d16d50e` |
| Base | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` |
| Biblioteca | merge `--no-ff` de `d22c4cc85aefc454ee8598711d717d21db57da7c` (árbol `2d722f50c…`, rama `phase/pre-g9b-maint-a-math`) en `0207b605b`; no se modifica ninguna fuente de la biblioteca |
| Contratos | [`bspline-2d.md`](../../geocedg/specs/curves/bspline-2d.md) `v1`; [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md); [matriz A-N1](pre_g9b_maint_a_n1_validation_matrix.md) §0 y BS-1 a BS-24; [registro de aprobación A-N1](pre_g9b_maint_a_n1_author_approval_record.md) |
| Guía de smoke | [guía para el autor](pre_g9b_maint_a2_author_smoke_guide.md) |
| `selfApproved` | `false` |

Este artefacto forma parte del commit técnico que describe. No contiene los resultados de la
verificación de ese commit (`STATIC`, `PHASE PRE-G9B-MAINT-A2`, `INTEGRATION`), que se obtienen
después sobre el commit congelado y se entregan aparte. La aprobación autoral, si llega, se
registra en otro documento.

## 1. Resultado

`BSpline(<Lista de puntos>)`, `BSpline(<Lista>, <Grado>)` y `BSpline(<Lista>, <Grado>, <Lista de
nudos>)` crean un `GeoLocusV2` con identidad durable propia, rama `bspline-2d/main`, existencia
probada por el proveedor con el contrato `bspline-2d/v1`, el parámetro de nudos del usuario,
continuidad estructural exacta publicada y cierre solo por igualdad exacta. La evaluación usa la
biblioteca compartida `org.geocedg.common.math.bspline`; el núcleo no añade operaciones de coma
flotante. SplineV2 queda bit-idéntica (BS-13).

## 2. Cambios

### 2.1 Núcleo (familia)

| Fichero | Contenido |
|---|---|
| `org/geocedg/common/kernel/algos/AlgoBSpline.java` | algoritmo `AlgoLocusV2`; causa tipada única por revisión; contrato de redefinición `bspline-2d/v1`; elemento de familia mediante el gancho del escritor |
| `org/geocedg/common/kernel/bspline/BSplineDefinition2D.java` | captura de entradas, regla `clamped-uniform/v1`, validación de la biblioteca, resumen de la definición (expuesto para `MAINT-A-DXF1`) |
| `org/geocedg/common/kernel/bspline/BSplineInvalidity.java` | causas del §7 en su orden (`INPUT_UNDEFINED` + las nueve de la biblioteca) |
| `org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java` | evaluador `LocusEvaluator2D`, `OneSidedLocusDifferentialEvaluator2D` (§2.2.1), `LocalPiecewisePolynomialLocus2D` |
| `org/geocedg/common/kernel/bspline/BSplineContractXml.java`, `BSplineContractException.java`, `BSplinePersistedContractTarget.java` | códec `<geocedgBSpline>` y bloque pasivo de procedencia |
| `org/geogebra/common/kernel/commands/CmdBSpline.java` | procesador: solo tipos de argumento; toda precondición numérica da curva indefinida tipada |
| `Commands`, `CommandDispatcher`, `BasicCommandProcessorFactory`, `RuntimeFeatureService`, `LocusV2PublicOperations.createBSpline`, `GeoLocusV2` (tipo «B-spline») | cableado del comando, puerta de función y fábrica con promoción de argumentos como SplineV2 |

### 2.2 Extensión aditiva de Locus V2 (§11.3)

- `LocalPiecewisePolynomialLocus2D` (nuevo): extiende `PiecewisePolynomialLocus2D` con los
  coeficientes locales por tramo en potencias descendentes de `s ∈ [0, 1]`. La interfaz existente
  no cambia.
- `PolynomialSpanCoefficients2D` (nuevo): accesor único; devuelve los locales cuando la fuente los
  publica y, si no, hace exactamente la llamada heredada una vez. Contiene también la regla
  residual (`admitsGlobalPublication`, `POLYNOMIAL_PUBLICATION_ILL_CONDITIONED`,
  `KAPPA_MAX_CANDIDATE = 2^16`).
- Consumidores encaminados: intersección unilateral (C1 `isolate`, C2 política de grado/64
  tramos), pares (C3 tramos, C4 límites), interacción de puntos (C5, búsqueda local en `s`) y la
  envoltura de semejanzas (C6, reenvía la extensión). Para toda fuente sin la extensión la
  aritmética es la de la base; BS-13 lo demuestra bit a bit.

Clasificación completa de los 13 usuarios de `PiecewisePolynomialLocus2D` en la base:

| Fichero (`org/geocedg/common/kernel/…`) | Clase de uso | Acción |
|---|---|---|
| `locus/PiecewisePolynomialLocus2D` | la interfaz | ninguna |
| `spline/SplineSemanticEvaluator2D` | implementación SplineV2, sin coeficientes locales | ninguna |
| `locus/LocusSimilarityEvaluator2D` | envoltura (C6) | reenvía la extensión |
| `locus/intersection/PolynomialTargetIntersectionCapability2D` | lee coeficientes (C1) y longitudes (C2) | encaminado |
| `locus/intersection/PiecewisePolynomialPairIntersectionCapability2D` | lee coeficientes (C3) y longitudes (C4) | encaminado |
| `locus/interaction/LocusPointInteractionResolver2D` | lee coeficientes (C5) | encaminado |
| `locus/interaction/LocusPointContinuation2D` | solo tipo y `supportsPiecewisePolynomial`, después C5 | ninguna |
| `locus/interaction/LocusPointInteractionWorkBudget2D` | solo la constante de profundidad | ninguna |
| `locus/intersection/IntersectionCapabilityContext2D` | tipo, soporte y `evaluatePolynomialDerivative` | ninguna |
| `locus/intersection/LocusPairIntersectionContext2D` | tipo, profundidad, soporte y `evaluatePolynomialDerivative` | ninguna |
| `locus/metric/PolicyV2LocusMetricCapability2D` | solo la constante de profundidad | ninguna |
| `locus/intersection/SplineImplicitIntervalCertification2D` | nivel certificado ligado a `SplineSemanticEvaluator2D` | ninguna |
| `locus/intersection/SplineIntervalModel2D` | nivel certificado ligado a `SplineSemanticEvaluator2D`; solo número de tramos | ninguna |

Envolturas u ocurrencias para una fuente BSpline: solo `LocusSimilarityEvaluator2D` implementa o
reenvía la interfaz y está encaminada. `ReconstructibleLocusEvaluator2D` no la implementa. El §24.7
de `locus-v2-metrics.md` trata de puntos extremo copiados (`AlgoDependentPoint`), no de
evaluadores. No existe ninguna envoltura sin encaminar: la regla residual queda **inactiva en
producción**; se prueba con una envoltura de prueba que no reenvía la extensión.
`MaintA2GlobalCoefficientInventoryTest` fija las lecturas de coeficientes globales que quedan
(predeterminado de la interfaz, rama heredada del accesor y conformidad de las implementaciones) y
la clasificación de cada fichero que nombra la interfaz.

#### 2.2.1 Derivada por la izquierda en el estimador métrico (opción A, decisión del autor)

Problema. En un nudo `C^0` (multiplicidad `p`, por ejemplo todo nudo interior de grado 1 o el
nudo triple de una cúbica) la derivada salta. El estimador de clase 3 integra por piezas que
terminan en los nudos, pero tomaba la velocidad del extremo superior de una pieza con
`evaluateDifferential`, que devuelve la derivada del tramo propietario del parámetro, es decir, el
tramo **de la derecha**. La regla de Simpson adaptativa no convergía (`MAXIMUM_DEPTH`) y la
longitud quedaba `LIMIT_NOT_ESTABLISHED`. Autorizado por el coordinador como extensión aditiva
estricta del consumidor (opción A):

- `locus/metric/OneSidedLocusDifferentialEvaluator2D` (nuevo, opcional): extiende
  `LocusDifferentialEvaluator2D` con `supportsOneSidedDifferential` y
  `evaluateDifferentialFromBelow`, la derivada del tramo que termina en el parámetro.
- `BSplineSemanticEvaluator2D` la implementa con `BSplineEvaluation.firstDerivativeInSpan`: en el
  inicio `u_j > a` de un tramo, el tramo no degenerado anterior; en `b`, el último tramo, también
  en una curva cerrada exacta (cuyo propietario de la costura es el primero); en cualquier otro
  punto, el tramo propietario. El parámetro no se identifica con `a`.
- `LocusSimilarityEvaluator2D` la reenvía solo si su fuente la declara; para cualquier otra
  fuente `supportsOneSidedDifferential` es `false`.
- `LocusMetricEstimator2D` (único consumidor): en `estimate` y `estimateRange`, la velocidad del
  extremo **superior** de una pieza es la derivada por la izquierda **solo** si el evaluador
  diferencial declara la interfaz; el extremo inferior, los puntos interiores, el muestreo y toda
  otra fuente conservan exactamente la aritmética anterior. Las ediciones están limitadas a la
  ruta de búsqueda de la velocidad (líneas de la base ~181–200 y un método nuevo tras `speed`,
  ~526), sin solaparse con los fragmentos de `MAINT-B5a`.
- Sin cambios en propiedad del parámetro, direcciones semánticas, interacción de puntos,
  intersecciones, tangentes ni continuidad publicada.

Justificación semántica. En la pieza cerrada `[u_j, u_{j+1}]` el integrando es la norma de la
derivada del polinomio de ese tramo, prolongado hasta su extremo: la longitud de la pieza es
`∫ |C_j'(u)| du` sobre el intervalo cerrado, y el valor en el extremo es el límite por la izquierda.
Cambiar el valor del integrando en un solo punto no cambia la integral (medida nula); lo que
cambia es que la cuadratura adaptativa ve la función continua de la pieza y converge.

Prueba de aditividad: `MaintA2OneSidedMetricAdditivityTest` fija el inventario (los cuatro
ficheros de producción que nombran la interfaz y la clasificación de cada implementación de
`LocusDifferentialEvaluator2D`: `SplineSemanticEvaluator2D` no la declara) y comprueba que
SplineV2 y su imagen por semejanza no la declaran (ruta heredada), que la familia y su imagen
devuelven la derivada del tramo izquierdo en el nudo triple y del último tramo en la costura, y
que la longitud de la imagen rotada coincide con la de la fuente. El dorado BS-13 (longitudes
`S1`–`S5`, `T1`–`T5` y longitud parcial de `S2`) sigue idéntico bit a bit.

### 2.3 Persistencia (§8)

- `AlgoElement.getCmdExtensionXML` (nuevo, protegido, vacío por defecto) se escribe antes de
  `<input>`; ningún otro algoritmo cambia sus bytes.
- `MyXMLHandler` lee `<geocedgBSpline>` solo para un `<command name="BSpline">` que resuelve al
  comando nativo (sin macro de ese nombre); todas las comprobaciones lanzan
  `BSplineContractException` desde el manejador del elemento, fuera de la recogida de errores de
  comandos. Para cualquier otro comando la conducta es la de la base (un hijo desconocido solo se
  registra en el log).
- `MyXMLio.doParseXML` relanza esa excepción en todas las rutas (incluida GGT/macros) y restaura la
  construcción de entrada (`requiresParseRollback`).
- Causas: `UNSUPPORTED_CONTRACT_VERSION` (prioritaria aunque haya atributos nuevos),
  `CONTRACT_MISSING` (incluido un elemento escrito después de `<input>`), `MALFORMED_CONTRACT`
  (atributo o hijo desconocido, duplicado, texto, regla de nudos desconocida o incoherente con el
  número de argumentos, procedencia sin `version` o con hijos o repetida) y
  `UNSUPPORTED_PROVENANCE_VERSION`. Una procedencia sin `version` es `MALFORMED_CONTRACT` porque es
  un defecto estructural del bloque, no una versión no admitida.
- Bloque de procedencia: un único hijo opcional `approximationProvenance` con atributos solamente;
  se conserva la lista ordenada (nombre, valor) del infoset y se vuelve a escribir con el escapado
  estándar de GeoCeDG. Guardar, reabrir y guardar es idéntico en bytes para documentos escritos
  por GeoCeDG; los espacios o comillas no canónicos de una entrada editada a mano se normalizan
  (se conserva el infoset, no los bytes). `MAINT-A2` no define atributo de resumen, no calcula la
  aplicabilidad y nunca escribe un bloque nuevo.
- `AppGeoCeDG` muestra el mensaje tipado `BSpline.LoadError.<CAUSA>` en lugar de un error
  genérico.

Rutas que leen `<command>` y su conducta de fallo cerrado: apertura nativa y `.ggb` (preflight en
una aplicación desechable; el documento abierto no se toca), `setXML`/`evalXML`, Deshacer/Rehacer
(`processConstructionSnapshot`), portapapeles (restauración y `false`), Insertar archivo (carga
auxiliar y transferencia estricta), redefinición (`buildConstruction` restaura y relanza) y
macros/GGT (relanzada). Ningún documento existente cambia de conducta.

### 2.4 Producto

Tipo «B-spline» (EN/ES); estilo por defecto: el prototipo genérico de Locus V2, sin cambiar el
formato versionado de valores por defecto; Insertar archivo admite la familia por la fila Locus
V2 sin código nuevo; ayuda y traducciones EN/ES (`BSpline` sin alias español, como SplineV2);
mensajes de carga en el perfil; guía EN/ES §7.8 y §7.9 y sintaxis en §16; sin herramienta
gráfica ni interfaz nueva de inspector.

### 2.5 Pruebas y verificación registradas

Pruebas compartidas en `org.geocedg.common.kernel.bspline` (familia, precisión, códec,
encaminamiento, inventario, identidad SplineV2, longitud BS-8, persistencia BS-11/BS-12/BS-24 y
aditividad de la opción A) y `MaintA2BSplineNativeArchiveTest` en Desktop;
`cmdBSpline` (exigido por `SelfTest`); listas de comandos GeoCeDG en `CommandDispatcherTest`,
`CommandFilterTest` y `CommandsValidationTest`; recuentos de la puerta R5-B (568, 517, 490);
filas `BSpline` de la matriz GGBScript (tres formas × tres superficies × dos idiomas y seis
sondas de variante de mayúsculas) y su método `bSplineRows`, con el hash canónico de la matriz y
el pin `static_contracts` actualizados; recuentos del esquema de la guía. El corpus R5-B
(`pre-g9b-r5-b-base-fingerprints.json`) se regeneró con una sonda fuera del árbol sobre el
candidato: cada resumen (universo de tokens, resoluciones de guion y XML, tablas inversas y
resoluciones de usuario EN/ES en sus dos estados, sintaxis inglesa completa) coincide con el valor
anterior al quitar las líneas que nombran `BSpline`, y la sintaxis Classic española sin la entrada
`BSpline` es el texto anterior; los demás cambios son las entradas `BSpline` de `commandNames.en`
y de los diccionarios que listan `SplineV2`.
Fase `PRE-G9B-MAINT-A2` con las selecciones `pre-g9b-maint-a2.shared` y
`pre-g9b-maint-a2.desktop`, nodos del registro, pin del inventario y recuentos de
`verification-final-coverage.Tests.ps1`. Todos los ficheros nuevos o modificados bajo `source/`
están registrados en `docs/upstream/modified-files.yml` junto al bloque de la biblioteca.

## 3. Decisiones aplicadas (revisión del coordinador)

| Tema | Decisión |
|---|---|
| Entrada indefinida o elemento de lista no válido en un recálculo | `INPUT_UNDEFINED`; tipo de argumento erróneo al crear: error de argumento ordinario, sin objeto |
| Estilo | prototipo genérico de Locus V2 |
| `eps_domain` | `1E-12 · (b − a)`, el precedente de SplineV2 escalado al dominio; lo consume solo la pertenencia tolerante al dominio del proveedor; propiedad de tramos, cierre, anclaje, multiplicidad y continuidad se deciden de forma exacta en la biblioteca |
| Contrato de redefinición | uno solo, `bspline-2d/v1`; la regla de nudos es un dato persistido aparte |
| Regla residual | implementada como mecanismo, inactiva en producción |
| Más de 64 tramos | la capacidad unilateral declina por su propio límite (sin motivo nuevo); responde la capacidad adaptativa |
| Procedencia | ver §2.3 |

## 4. Resultados BS

| BS | Resultado | Prueba |
|---|---|---|
| 1, 2, 17, 18 | poligonal de grado 1 y cierre exacto; Bézier con `u = 0 → P_0`; equivalencia Bézier `p = 1..6` y propiedad de nudos idénticas bit a bit a la biblioteca | `MaintA2BSplineFamilyTest` |
| 3, 15, 16 | barrido `p = 1..7`, `n + 1 ∈ {p+1, 2p+1, 32}`, nudos uniformes y aleatorios (semilla 20261124) y familias adversas (desplazamiento 1e6, separación 1e-6, puntos casi coincidentes): dentro de E1 y E3 con las constantes derivadas; el evaluador del núcleo es la evaluación de la biblioteca bit a bit (BS-15 lo prueba la biblioteca) | `MaintA2BSplineAccuracyTest` |
| 4, 6, 7 | continuidad `C^{p-k}` exacta; degeneraciones definidas (longitud 0, `SINGULAR`); cierre solo exacto | `MaintA2BSplineFamilyTest` |
| 5, 19 | causas tipadas sin geometría ni geometría antigua y recuperación; sin rebaja del grado; regeneración `clamped-uniform/v1` | `MaintA2BSplineFamilyTest` |
| 8 | frente a una referencia independiente (§4.2): total `C^2`, total y parcial a través de un nudo de multiplicidad 3, grado 1 (total, parcial a través de dos nudos, cerrada) y cúbica cerrada (costura), todas dentro del error declarado; longitud acumulada estrictamente monótona en la curva `C^2` y en la del nudo triple; mismo valor y mismo error declarado tras zoom ×7.5, desplazamiento y `pixelRatio = 2`; longitud 0 de la curva colapsada | `MaintA2BSplineLengthTest`, `MaintA2BSplineFamilyTest` |
| 9 | raíz en nudo y raíz en la costura cuentan una vez | `MaintA2BSplineFamilyTest` |
| 10 | imagen trasladada igual a la B-spline de los puntos trasladados (1e-13); reenvío de coeficientes locales | `MaintA2BSplineFamilyTest`, `MaintA2LocalPolynomialRoutingTest` |
| 11 | mover `P_6` cambia solo los tramos de su soporte (comparación de bits); grado y nudos sin localidad; un nudo libre `t` en `{0,0,0,0,t,4,4,4,4}`: `t = 3` recalcula la curva, `t = 5` da `KNOTS_DECREASING` sin geometría antigua, `t = 2` recupera | `MaintA2BSplineFamilyTest`, `MaintA2BSplinePersistenceTest` |
| 12 | XML guardado y recargado idéntico; guardar-reabrir-guardar idéntico en bytes con identidad y dependencias; Deshacer/Rehacer de un movimiento y de una edición de nudo; copiar y pegar con identidad nueva; redefinición con la misma conducta que SplineV2 en las dos rutas (§4.3); sustitución por otra familia que retira el elemento; `.cedg` guardar-reabrir-guardar | `MaintA2BSplineFamilyTest`, `MaintA2BSplinePersistenceTest`, `MaintA2BSplineNativeArchiveTest` |
| 13 | coeficientes, intersecciones, pares, interacción de puntos, longitudes y XML de SplineV2 y de sus imágenes por semejanza idénticos al dorado capturado en la base antes de cualquier edición | `MaintA2SplineV2BitIdentityTest` |
| 20 | coeficientes locales dentro de `K_loc` en nudos cerca de 1e6, rangos amplios, `p = 7` y 125 tramos; globales correctamente redondeados; ningún consumidor lee globales; por encima de 64 tramos responde la ruta del evaluador | `MaintA2BSplineAccuracyTest`, `MaintA2LocalPolynomialRoutingTest` |
| 21 | todas las causas; reversión completa en documento, GGT y portapapeles; mensaje tipado en la apertura nativa | `MaintA2BSplineContractCodecTest`, `MaintA2BSplineNativeArchiveTest` |
| 22 | ver §6 | sondas registradas en `geocedg/validation/pre-g9b-maint-a2/bs22-older-reader/` |
| 23 | la evaluación no consulta datos de renderizado | `MaintA2BSplineFamilyTest` |
| 24 | lectura, validación de versión y conservación en el orden del infoset; el bloque sobrevive a Deshacer y a copiar; aplicabilidad condicionada a `MAINT-A-DXF1` (§7.3) | `MaintA2BSplineContractCodecTest`, `MaintA2BSplineFamilyTest`, `MaintA2BSplinePersistenceTest` |
| 14 | `MAINT-A3` | — |

### 4.1 Constantes y medidas (§0.1, §0.2 de la matriz)

Constantes derivadas por `MAINT-A-DXF0` para la secuencia de operaciones de la biblioteca
(nota de derivación de ese incremento): `K_pos(p) = 8p + 1`, `K_der(p) = 8p − 3`,
`ρ_der = K_der · 2^-33`, `K_loc(p) = (17p − 14) · 3^p + 1`. El evaluador del núcleo no añade
operaciones (canonización `b → a` de una curva cerrada, que es una selección exacta), así que las
constantes valen para él; la prueba verifica además la igualdad bit a bit con la biblioteca.

Máximos observados (`error / (ε·S)`), sin uso para endurecer ninguna constante:

| Familia | E1 posición | E3 derivada | local | evaluación con globales | κ máximo |
|---|---|---|---|---|---|
| desplazamiento 1e6 | 2.6 | 2.1e-6 | 269 | 1.0e3 | 1e7 |
| separación 1e-6 | 1.44 | 2.48 | 152 | 3.9e7 | 1e7 |
| puntos casi coincidentes | 1.63 | 1.05 | 95.9 | 5.0e6 | 3.0e13 |
| barrido BS-16, máximo de todas las celdas | 3.34 | 1.54 | 254 | 2.5e12 | 1.3e18 |
| BS-20 nudos en `[1e6, 1e6+11]` | 1.63 | 0.77 | 252 | 4.6e43 | 1e42 |
| BS-20 rango `[-1e8, 1e8]` | 1.78 | 0.59 | 187 | 4.0e57 | 1e56 |
| BS-20 cúbica de 128 puntos (125 tramos) | 1.82 | 1.31 | 12.5 | 4.7e6 | 2.0e6 |

La columna «evaluación con globales» mide la representación por coeficientes globales
correctamente redondeados evaluados exactamente: crece con κ (por ejemplo `κ ≈ 1.3e5` da `1.6e5`,
`κ ≈ 2.6` da `6.1`), mientras que la columna local queda acotada por `3^p`. Con `κ_max = 2^16` la
ruta global se mantendría en unos `1e5 ε S ≈ 1e-11 S`. `κ_max` sigue siendo un valor candidato
para el autor; en producción la regla residual está inactiva porque todos los lectores están
encaminados.

### 4.2 BS-8: longitud frente a la referencia

Referencia independiente: cuadratura de Gauss-Legendre de 20 nodos (nodos y pesos por Newton con
80 dígitos) en `BigDecimal` con 60 dígitos significativos, de la norma de la derivada racional
exacta (`ExactRationalBSplineReference.derivative`) de los datos binary64, **tramo a tramo** y con
el polinomio de cada tramo sobre el tramo cerrado (los nodos son interiores, así que la referencia
nunca toma la derivada del tramo vecino en un nudo `C^0`). Su exactitud se muestra con `m = 4` y
`m = 8` subdivisiones por tramo (diferencia `< 1e-20` en todos los casos). Puntos de control
`A=(0,0)`, `B=(1,2)`, `C=(3,2.5)`, `D=(4,0.5)`, `E=(6,1)`, `F=(7,3)`, `G=(5,4)`.

| Caso | Estimación | Referencia (30 dígitos) | `|error|` | error declarado |
|---|---|---|---|---|
| total `C^2`, `BSpline({A..F},3)` | 9.297400655149506 | 9.29740065515248299003775379651 | 3.0e-12 | 2.4e-9 |
| total con nudo triple, `BSpline({A..G},3,{0,0,0,0,1,1,1,2,2,2,2})` | 10.343231861280563 | 10.3432318612761722884626100685 | 4.4e-12 | 1.8e-9 |
| parcial `u ∈ [0.5, 1.5]` a través del nudo triple | 5.175140592537678 | 5.17514059253186801628550216884 | 5.8e-12 | 8.5e-10 |
| cúbica cerrada `BSpline({A,B,C,D,A},3)` (costura) | 8.220506496587502 | 8.22050649657229452315254570675 | 1.5e-11 | 1.2e-9 |
| grado 1 total, `BSpline({A..E},1)` | 8.595241580617241 | 8.59524158061723994263975719344 | 1.1e-15 | 0 |
| grado 1 parcial `u ∈ [0.5, 2.5]` (dos nudos) | 4.2976207903086205 | 4.29762079030861997131987859672 | 5.7e-16 | 0 |
| grado 1 cerrada `BSpline({A,B,C,D,A},1)` | 10.564817641957685 | 10.5648176419576844939123588806 | 4.2e-16 | 0 |

El importe establecido acota la discretización de la estimación, no la suma en coma flotante de
sus hojas: en grado 1 la regla es exacta y declara 0; la prueba admite por eso, además, 16 ulp
del valor (el error observado es inferior a 1 ulp). Es la semántica existente del importe
establecido, común a toda fuente lineal a trozos; `A2` no la cambia. La referencia de grado 1
coincide además con la suma de las longitudes de los segmentos.

Antes de la opción A, el total con nudo triple, el parcial a través de él y los casos de grado 1
daban `LIMIT_NOT_ESTABLISHED` (`MAXIMUM_DEPTH`); el total `C^2` ya pasaba con el mismo valor.

### 4.3 BS-12: paridad de la redefinición con SplineV2

Misma redefinición compatible (cambio de grado sobre la misma lista de puntos), en objetos
nuevos para cada ruta:

| Ruta | SplineV2 `SplineV2(pts,3) → SplineV2(pts,5)` | BSpline `BSpline(pts,3) → BSpline(pts,2)` |
|---|---|---|
| ordinaria (`changeGeoElement` sin autoridad de sustitución) | rechazada (`REDEFINE_REJECTED`: el modo `ADVANCED_RETAIN` no lo autoriza la evaluación `LEGACY_REPLACEMENT_AVAILABLE`); nada cambia, identidad conservada | idéntico |
| sustitución explícita de ADR 0026 (`withSpatialReplacementOperation`) | aplicada; identidad durable nueva | idéntico |

La identidad nueva en la ruta de sustitución no es un defecto de `A2`: es el resultado de las
reglas existentes (§8.5 de `bspline-2d.md`: «whether the durable identity survives is decided by
those rules, not by this family»), igual para SplineV2. La prueba fija los cuatro resultados.

## 5. Notas de versión (para la primera versión que contenga `BSpline`)

- Los documentos con `BSpline` no se abren en GeoCeDG 1.2.2 ni anteriores (la validación de
  identidad rechaza el documento completo, sin causa que nombre la familia).
- Clase de colisión K15 (informe de preparación E2): una herramienta de usuario o macro de
  documento llamada `BSpline` (en cualquier combinación de mayúsculas) oculta el comando nativo, y
  una biblioteca de herramientas guardada que la contenga deja de poder leerse (los bytes del
  almacén no cambian). Los documentos existentes con una macro así siguen cargando como antes.

## 6. BS-22: lectores anteriores

Evidencia registrada en `geocedg/validation/pre-g9b-maint-a2/bs22-older-reader/`: fuentes de las
tres sondas (escritor del fixture, lector 1.2.2 y lector de la base), receta, salidas y el fixture
`bspline-fixture.cedg` con su SHA-256
`671ee7ad70ae8e930b84c3940fbc059fbc48811013f9c4830c6db3c708818426`.

- Lector de la base (`228580e96`, y cualquier compilación anterior a `MAINT-A2`): sonda fuera del
  árbol sobre el lector compartido con un documento `BSpline` v1 simulado →
  `SpatialIdentityException: MALFORMED_RECORD` («Construction identity base contract disagrees
  with the attached geo»), construcción de entrada restaurada; sin la sección de identidad,
  `GEO_ATTACHMENT_MISSING`. Confirma §8.4 y el informe A-N1 §2.1.
- GeoCeDG 1.2.2: se usa la imagen de aplicación `windows-1.2.2-nc-candidate-4` (`source_revision`
  `a6562fa30`); la etiqueta `v1.2.2` (`1b54ff95d`) solo añade documentación a ese commit, así que
  el código es el publicado. Sonda aislada (JDK 25, `APPDATA`, `LOCALAPPDATA`, `user.home` y
  `java.io.tmpdir` en un directorio temporal externo; los jars se leen sin modificarlos) que abre
  con `AppGeoCeDG.loadFile` un `.cedg` escrito por el candidato (`b=BSpline({A,B,C,D,E})`,
  `M=LocusLength(b)`) sobre un documento que ya contenía `Kept`: `loaded=false`, `Kept` sigue
  siendo el único objeto, sin mensaje tipado. Cadena registrada: `unknown tag in <command>:
  geocedgBSpline`, `CommandNotFoundError: Unknown command : BSpline`, errores de `LocusLength` y,
  por último, `SpatialIdentityException: MALFORMED_RECORD` («Construction identity base contract
  disagrees with the attached geo»), que rechaza el fichero completo. Ningún documento parcial se
  presenta como completo, como predice §8.4.

## 7. Decisiones del autor, riesgos y puntos abiertos

### 7.1 Opción A y `VERIFICATION_ESCALATION_REQUEST`

```text
VERIFICATION_ESCALATION_REQUEST
  motivo          = nueva interfaz genérica opcional de Locus V2
                    (OneSidedLocusDifferentialEvaluator2D) y una rama del estimador métrico
                    de clase 3 (LocusMetricEstimator2D), §2.2.1
  aditividad      = solo la familia la implementa; la envoltura de semejanzas la reenvía;
                    toda otra fuente toma la ruta anterior (inventario y pruebas de §2.2.1;
                    dorado BS-13 idéntico bit a bit)
  nivel pedido    = FINAL, o una dispensa explícita del autor
  ejecutado       = STATIC, PHASE PRE-G9B-MAINT-A2 e INTEGRATION; FINAL no se ejecuta
```

La opción A es una **decisión del autor**: la autorizó el coordinador como extensión aditiva
estricta; el autor decide si la acepta y con qué nivel de verificación.

### 7.2 Otros puntos para el autor

1. **BS-24:** la aplicabilidad de la procedencia de aproximación depende del atributo de resumen
   que fije `MAINT-A-DXF1`; `MAINT-A2` expone el resumen de la definición
   (`BSplineDefinition2D.getDefinitionDigestHex`) y no calcula aplicabilidad. Hueco declarado:
   BS-24 queda cubierto solo en lectura, validación de versión, conservación del infoset,
   Deshacer y copia.
2. **`κ_max = 2^16`** es un valor candidato (§4.1); la regla residual está inactiva en
   producción.
3. **Notas de versión** (§5): los lectores 1.2.2 y anteriores rechazan el fichero completo
   (BS-22); colisión K15 con una macro o herramienta llamada `BSpline`.

### 7.3 Comportamiento de la base que `A2` no cambia

1. **Más de 64 tramos:** la capacidad unilateral de intersección declina por su propio límite
   existente y responde la capacidad adaptativa del evaluador; no hay motivo nuevo. Es la conducta
   de la base para toda fuente polinómica a trozos.
2. **Orden de pares de SplineV2 (deuda previa):** los resultados de pares de SplineV2 por el
   comando público ordenan el par por sus identidades durables aleatorias; el orden de las
   soluciones, el lado evaluado y los contadores de trabajo cambian entre ejecuciones. La prueba
   BS-13 usa por eso identidades fijas.
3. **Importe de error establecido en grado 1** (§4.2): declara 0 porque no incluye el redondeo de
   la suma.
4. **Identidad tras la sustitución explícita** (§4.3): nueva, igual que SplineV2.

### 7.4 Selecciones del inventario JUnit

Se calculó el hash de identidades de descubrimiento (`--test-dry-run`) de cada selección en la
base `228580e96` (copia `git archive` fuera del árbol) y en el árbol del candidato, y se comparó
con el valor guardado en cada árbol.

Desactualizadas por `A2` (iguales en la base, distintas en el candidato) y actualizadas con el
actualizador canónico a partir de evidencia de productor ejecutada (`COMPLETED`, salida 0):
`discovery.shared` (descubrimiento), `pre-g9b-maint-a2.shared` (con las tres clases nuevas
`MaintA2BSplineLengthTest`, `MaintA2BSplinePersistenceTest` y
`MaintA2OneSidedMetricAdditivityTest`), `pre-g9b-r6-plus-e2.shared`,
`pre-g9b-r6-plus-e3.shared` (método nuevo `GeoCeDGCommandsTest.cmdBSpline`),
`pre-g9b-r6.desktop`, `pre-g9b-r6-plus-b.desktop`, `pre-g9b-r6-plus-c.desktop`,
`pre-g9b-r6-plus-e2.desktop` y `pre-g9b-r6-plus-e3.desktop` (método nuevo
`PreG9BR6CapabilityMatrixTest.bSplineRows`). El pin del inventario en el registro y los recuentos
fijados en `verification-final-coverage.Tests.ps1` se actualizan en consecuencia
(`pre-g9b-maint-a2.shared` pasa de 164 a 178 identidades).

Ya desactualizadas en la base (previas; no se tocan): `geocedg-1-2-0.shared`,
`pre-g9b-r4.shared`, `pre-g9b-r6.shared`, `pre-g9b-r6-plus-a1.shared`,
`pre-g9b-r6-plus-b.shared`, `pre-g9b-cu2.shared`,
`pre-g9b-cu7-general-point-interaction.shared`,
`pre-g9b-cu7-general-point-interaction-r3.shared`,
`pre-g9b-cu7-general-point-interaction-r4.shared`, `pre-g9b-cu7-r5.shared`,
`post-cu4-expert-insertion.shared`, `pre-g9b-cu8-i.shared`, `post-r1-locus.shared`,
`geocedg-1-2-0.desktop`, `final.desktop.g9u1-isolated`,
`final.desktop.revision3-diagnostic` y `pre-g9b-cu4.desktop`.

`final.shared` y `final.desktop`: no se actualizan. Son selecciones solo de `FINAL`, ya estaban
desactualizadas en la base y también las dejan así `MAINT-U2` y `MAINT-B5a`.
