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
| `org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java` | evaluador `LocusEvaluator2D`, `LocusDifferentialEvaluator2D`, `LocalPiecewisePolynomialLocus2D` |
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
encaminamiento, inventario, identidad SplineV2) y `MaintA2BSplineNativeArchiveTest` en Desktop;
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
| 8 | longitud finita; vértice de grado 1 y longitud 0 de la curva colapsada (la referencia de cuadratura de alta precisión queda en el punto abierto 7.2) | `MaintA2BSplineFamilyTest` |
| 9 | raíz en nudo y raíz en la costura cuentan una vez | `MaintA2BSplineFamilyTest` |
| 10 | imagen trasladada igual a la B-spline de los puntos trasladados (1e-13); reenvío de coeficientes locales | `MaintA2BSplineFamilyTest`, `MaintA2LocalPolynomialRoutingTest` |
| 11 | mover `P_6` cambia solo los tramos de su soporte (comparación de bits); grado y nudos sin localidad | `MaintA2BSplineFamilyTest` |
| 12 | XML guardado y recargado idéntico; identidad y dependencias; `.cedg` guardar-reabrir-guardar con la misma construcción | `MaintA2BSplineFamilyTest`, `MaintA2BSplineNativeArchiveTest` |
| 13 | coeficientes, intersecciones, pares, interacción de puntos, longitudes y XML de SplineV2 y de sus imágenes por semejanza idénticos al dorado capturado en la base antes de cualquier edición | `MaintA2SplineV2BitIdentityTest` |
| 20 | coeficientes locales dentro de `K_loc` en nudos cerca de 1e6, rangos amplios, `p = 7` y 125 tramos; globales correctamente redondeados; ningún consumidor lee globales; por encima de 64 tramos responde la ruta del evaluador | `MaintA2BSplineAccuracyTest`, `MaintA2LocalPolynomialRoutingTest` |
| 21 | todas las causas; reversión completa en documento, GGT y portapapeles; mensaje tipado en la apertura nativa | `MaintA2BSplineContractCodecTest`, `MaintA2BSplineNativeArchiveTest` |
| 22 | ver §6 | sondas fuera del árbol |
| 23 | la evaluación no consulta datos de renderizado | `MaintA2BSplineFamilyTest` |
| 24 | lectura, validación de versión y conservación en el orden del infoset; aplicabilidad condicionada a `MAINT-A-DXF1` | `MaintA2BSplineContractCodecTest`, `MaintA2BSplineFamilyTest` |
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

## 5. Notas de versión (para la primera versión que contenga `BSpline`)

- Los documentos con `BSpline` no se abren en GeoCeDG 1.2.2 ni anteriores (la validación de
  identidad rechaza el documento completo, sin causa que nombre la familia).
- Clase de colisión K15 (informe de preparación E2): una herramienta de usuario o macro de
  documento llamada `BSpline` (en cualquier combinación de mayúsculas) oculta el comando nativo, y
  una biblioteca de herramientas guardada que la contenga deja de poder leerse (los bytes del
  almacén no cambian). Los documentos existentes con una macro así siguen cargando como antes.

## 6. BS-22: lectores anteriores

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

## 7. Riesgos y puntos abiertos

1. **Decisión del autor (BS-24):** la aplicabilidad de la procedencia de aproximación depende del
   atributo de resumen que fije `MAINT-A-DXF1`; `MAINT-A2` expone el resumen de la definición
   (`BSplineDefinition2D.getDefinitionDigestHex`) y no calcula aplicabilidad.
2. BS-8 usa comprobaciones estructurales de longitud; la referencia de cuadratura de alta precisión
   independiente queda pendiente.
3. `κ_max = 2^16` es candidato (§4.1).
4. Las selecciones históricas que contienen clases con métodos nuevos (`GeoCeDGCommandsTest`,
   `PreG9BR6CapabilityMatrixTest`) conservan su recuento anterior hasta una actualización con
   evidencia ejecutada; `INTEGRATION` usa `core.*` y las selecciones de esta fase, que no los
   contienen o ya los recogen.
5. Observación previa a `A2` (no corregida): los resultados de pares de SplineV2 por el comando
   público ordenan el par por sus identidades durables aleatorias; el orden de las soluciones, el
   lado evaluado y los contadores de trabajo cambian entre ejecuciones. La prueba BS-13 usa por eso
   identidades fijas.
