# POST-CU4 · Censo estático de tipos para P4

**Estado. Anexo candidato de caracterización.** Base `d5332e400b0ab68afd14273495b9ae5f03e6c911`. La tabla cubre el enum completo. Su unidad es el discriminante de `GeoClass`. Los productores y dependencias prioritarios están en la matriz principal. [Inventario por productor](post_cu4_p4_inventory.md) · [Enum de tipos](../../source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java)

## Interpretación

`P`, `E` y `U` transcriben `POTENTIAL`, `EXCLUDED` y `UNDETERMINED` del diagnóstico sobre un nombre de tipo **igual a `xmlName`**. `E` incluye todo valor del enum marcado 3D y las exclusiones explícitas del switch. `U` puede terminar admitido o rechazado tras reconstrucción. El modo angular, los guiones, M1, los recursos y la transacción tienen sus propias puertas. [Regla estática y admisión cargada](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGInsertionAdmission.java)

La columna XML reproduce el nombre del enum. Algunos tipos se reconstruyen por `<command>`, `<expression>` o una ruta especial. Los alias de fábrica, el uso de mayúsculas/minúsculas, las celdas CAS y el nodo `geocedgSpatial` se deben comprobar en la declaración real. Las fábricas de geo son evidencia del constructor a partir de un tipo. El productor posterior y la reconstrucción efectiva requieren cotejo adicional. [Fábrica común](../../source/shared/common/src/main/java/org/geogebra/common/kernel/GeoFactory.java) · [Fábrica 3D](../../source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/GeoFactory3D.java) · [Cargador XML](../../source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java)

| Enum | Nombre XML | Diagnóstico bruto | Clase y frontera de admisión |
|---|---|---|---|
| `ANGLE` | `angle` | P | `GeoAngle`. `AlgoAnglePoints` o libre deslizable finito. Otros productores carecen de fila específica. |
| `AXIS` | `axis` | U | `GeoAxis`. Constante del kernel como entrada. La excepción de F3 se limita al mismo eje incorporado. |
| `BOOLEAN` | `boolean` | P | `GeoBoolean`. Libre, condición o resultado upstream de clase exacta. |
| `BUTTON` | `button` | E | `GeoButton`. `CmdButton`, contenido de interacción y posibles scripts. |
| `TEXTFIELD` | `textfield` | E | `GeoInputBox`. `AlgoInputBox` con geo enlazado o sin argumento. |
| `CONIC` | `conic` | U | `GeoConic`. Círculo y elipse admitidos por subtipo. Parábola, hipérbola y degeneradas excluidas. |
| `CONICPART` | `conicpart` | E | `GeoConicPart` y variante `GeoConicPart3D`. Arco/sector por centro, circunscrito o sobre soporte cónico. |
| `FUNCTION` | `function` | P | `GeoFunction`. Productor libre o dependiente upstream, con límites y ramas según su definición. |
| `IMAGE` | `image` | E | `GeoImage`. Recurso de ZIP, nombre y anclas. La copia XML carece del recurso binario. |
| `LINE` | `line` | P | `GeoLine`. Libre o dependiente, por ejemplo `AlgoJoinPoints` con dos puntos. |
| `LIST` | `list` | U | `GeoList`. Clasificación recursiva por clase de miembros 2D y productor del geo exterior. |
| `LOCUS` | `locus` | E | `GeoLocus`. `AlgoLocus` y `AlgoLocusSlider`. Representación de camino legado. |
| `NUMERIC` | `numeric` | P | `GeoNumeric`. Literal, deslizador o productor upstream. También salida de una familia GeoCeDG reconocida o excluida por su productor. |
| `POINT` | `point` | P | `GeoPoint`. Libre o dependiente, por ejemplo `AlgoDependentPoint`, `AlgoPointOnPath` o `AlgoPointInRegion`. |
| `POLYGON` | `polygon` | P | `GeoPolygon`. `AlgoPolygon` o `AlgoPolygonRegular` con aristas y salidas hermanas. |
| `RAY` | `ray` | P | `GeoRay`. `AlgoJoinPointsRay` o `AlgoRayPointVector`. Origen, dirección y orientación. |
| `SEGMENT` | `segment` | P | `GeoSegment`. `AlgoJoinPointsSegment` o salida de polígono. Extremos ordenados. |
| `TEXT` | `text` | P | `GeoText`. Libre definido o dependiente. Comparar productor y anclas por variante. |
| `FORMULA` | `formula` | U | `GeoFormula`. Representación inline propia, excluida por clase. |
| `VECTOR` | `vector` | P | `GeoVector`. `AlgoVector` o `AlgoVectorPoint`, entre otros. Componentes y posible origen. |
| `CURVE_CARTESIAN` | `curvecartesian` | U | `GeoCurveCartesian`. `AlgoCurveCartesian`, spline nativa 2D y variantes. Excluida. |
| `IMPLICIT_POLY` | `implicitpoly` | U | `GeoImplicitCurve` en `kernel.implicit`. Productores implícitos o ajuste. Excluida. |
| `FUNCTION_NVAR` | `functionnvar` | U | `GeoFunctionNVar`. Variables ordenadas, funciones y regiones. Excluida. |
| `POLYLINE` | `polyline` | U | `GeoPolyLine`. `AlgoIsoABorder` admitido. `AlgoPolyLine` excluido. |
| `PENSTROKE` | `penstroke` | E | `GeoLocusStroke`. `AlgoLocusStroke`, trazo muestreado. |
| `TURTLE` | `turtle` | E | `GeoTurtle`. `AlgoTurtle`, estado de comandos. |
| `CAS_CELL` | `cascell` | E | `GeoCasCell`. Celda del CAS con su propia ruta XML y gemelos cuando aplican. |
| `ANGLE3D` | `angle3d` | E | `GeoAngle3D`. `AlgoAnglePoints3D` y variantes orientadas. Excluida. |
| `POINT3D` | `point3d` | E | `GeoPoint3D`. Productores 3D nativos y piloto espacial GeoCeDG. Excluido. |
| `VECTOR3D` | `vector3d` | E | `GeoVector3D`. `AlgoVector3D`. Excluido. |
| `SEGMENT3D` | `segment3d` | E | `GeoSegment3D`. Extremos 3D y aristas de objetos compuestos. Excluido. |
| `LINE3D` | `line3d` | E | `GeoLine3D`. `AlgoJoinPoints3D` y otras incidencias. Excluida. |
| `RAY3D` | `ray3d` | E | `GeoRay3D`. Origen y dirección 3D. Excluido. |
| `CONIC3D` | `conic3d` | E | `GeoConic3D`. `AlgoConicFivePoints3D` y cortes no limitados. Excluida. |
| `CONICSECTION` | `conic3dpart` | E | `GeoConicSection`. `AlgoIntersectPlaneQuadricLimited` y salidas recortadas. Excluida. |
| `AXIS3D` | `axis3D` | E | `GeoAxis3D`. Constante 3D y alias de fábrica `axis3d`, según contexto XML. |
| `CURVE_CARTESIAN3D` | `curvecartesian3d` | E | `GeoCurveCartesian3D`. Coordenadas paramétricas, dominio y ruta de cálculo 3D. Excluida. |
| `POLYGON3D` | `polygon3d` | E | `GeoPolygon3D`. `AlgoPolygon3D`, productor regular y caras de sólidos. Excluido. |
| `PLANE3D` | `plane3d` | E | `GeoPlane3D`. Plano libre/dependiente o `AlgoPlaneThreePoints`, entre otros. Excluido. |
| `QUADRIC` | `quadric` | E | `GeoQuadric3D`. Esferas y cuádricas. Fábrica también reconoce `quadric3d`. Excluida. |
| `QUADRIC_PART` | `quadricpart` | E | `GeoQuadric3DPart`. Lateral o región limitada. La fábrica usa `quadric3dpart`. Excluida. |
| `QUADRIC_LIMITED` | `quadriclimited` | E | `GeoQuadric3DLimited`. Conos/cilindros limitados con salidas hermanas. Fábrica `quadric3dlimited`. Excluida. |
| `POLYLINE3D` | `polyline3d` | E | `GeoPolyLine3D`. Vértices 3D en orden. Excluida. |
| `POLYHEDRON` | `polyhedron` | E | `GeoPolyhedron`. Prismas, pirámides y sólidos con caras. Excluido. |
| `NET` | `net` | E | `GeoPolyhedronNet`. Productor de desarrollo, incluidas variantes de pirámide/prisma. Excluido. |
| `SURFACECARTESIAN3D` | `surfacecartesian3d` | E | `GeoSurfaceCartesian3D`. Superficie paramétrica con dos parámetros. Excluida. |
| `SURFACECARTESIAN` | `surfacecartesian` | U | `GeoSurfaceCartesian2D` en `kernel.kernelND`. Ausente de clases elementales. |
| `IMPLICIT_SURFACE_3D` | `implicitsurface3d` | E | `GeoImplicitSurface`. Ecuación o `AlgoDependentImplicitSurface`. Excluida. |
| `CLIPPINGCUBE3D` | `clippingcube3d` | E | `GeoClippingCube3D`. Auxiliar del entorno de vista 3D, sin fila P4. |
| `SPACE` | `space` | E | `GeoSpace`. Auxiliar/constante 3D, sin fila P4. |
| `AUDIO` | `audio` | U | `GeoAudio`. Referencias multimedia, fuera de las clases elementales. |
| `VIDEO` | `video` | U | `GeoVideo`. Fuente multimedia, fuera de las clases elementales. |
| `EMBED` | `embed` | U | `GeoEmbed`. Contenido integrado y recursos potenciales, fuera de P4. |
| `DEFAULT` | `default` | U | Discriminante genérico, por ejemplo `GeoScriptAction`. El productor y contrato real requieren identificación. |
| `SYMBOLIC` | `symbolic` | U | `GeoSymbolic`. Resultados simbólicos y dependencias del CAS. Excluido. |
| `INLINE_TEXT` | `inlinetext` | U | `GeoInlineText`. Estado inline propio, excluido por clase. |
| `TABLE` | `table` | U | `GeoInlineTable`. Estado de tabla editable, excluido por clase. |
| `MIND_MAP` | `mindmap` | U | `GeoMindMapNode`. Nodos y enlaces de editor, excluido por clase. |
| `PIECHART` | `piechart` | U | `GeoPieChart` en `kernel.statistics`, `AlgoPieChart`. Excluido por clase. |
| `SHAPE_STADIUM` | `stadium` | U | `GeoStadium`, `AlgoStadium`. Excluido por clase. |
| `LOCUS_V2` | `locusv2` | P | `GeoLocusV2`. Productor GeoCeDG del paquete reconocido. Spline V2 tiene precedencia específica. |
| `LOCUS_METRIC_RESULT` | `locusmetricresult` | U | `GeoLocusMetricResult`. Productor Locus V2 reconocido. Las variantes públicas tienen persistencia. |
| `LOCUS_INTERSECTION_RESULT` | `locusintersectionresult` | U | `GeoLocusIntersectionResult`. Productor Locus V2 reconocido. Las variantes públicas persisten evidencia de tokens cuando procede. |

## Casos que exigen cotejo aparte

- **Alias y pérdida de tipo.** `GeoFactory` reconoce `circle`, `doubleLine`, `ellipse`, `emtpyset`, `hyperbola`, `intersectinglines`, `parabola` y `parallellines` como alias históricos de `GeoConic`, y `functionconditional` y `interval` como alias de `GeoFunction`. Su caso por defecto registra error y devuelve `GeoNumeric`. Si un tipo bruto inesperado llega a esa ruta, el clasificador cargado puede ver una clase elemental. La prueba de un candidato experto debe detectar la sustitución al cotejar el tipo declarado con el reconstruido y distinguir alias compatibles de degradaciones. [Fábrica común](../../source/shared/common/src/main/java/org/geogebra/common/kernel/GeoFactory.java)
- **Salida 3D con discriminante compartido.** `GeoConicPart3D` devuelve `GeoClass.CONICPART`. El guard de objetos excluye también `isGeoElement3D()` y el productor de paquete 3D. Un análisis que clasifique solo por `GeoClass.is3D` de ese discriminante pierde esta diferencia. [Parte cónica 3D](../../source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/geos/GeoConicPart3D.java)
- **Celdas, macros y otros nodos.** `getGeoSetWithCasCellsConstructionOrder` agrega las celdas al universo recorrido por admisión. El grafo de `AlgoMacro` y `geogebra_macro.xml` tienen productor y cierre propios. `geocedgSpatial` vive bajo la construcción y contiene registros tipados, de modo que el censo de tipos geométricos es una parte de la inspección. [Recorrido de construcción](../../source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java) · [Contrato de dependencias durables](../../geocedg/specs/spatial/durable-dependency-projection.md)
- **Versiones Locus V2.** `GeoClass` conserva comentarios de etapa para resultados ricos internos. Las clases concretas distinguen resultados internos transitorios de variantes públicas persistentes mediante `publicPersistence`. La política por productor de P4 sigue la clase que se reconstruye y las condiciones de identidad. Una salida interna sin persistencia queda fuera del perímetro archivístico por diseño. [Resultado métrico](../../source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusMetricResult.java) · [Resultado de intersección](../../source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java)

## Comprobación reproducible del perímetro

Desde la raíz de la base del estudio, comparar el conjunto de identificadores de la tabla con el enum y examinar ambas fábricas. `rg` con código 1 expresa una búsqueda válida sin coincidencias. La primera búsqueda debe devolver 63 entradas.

```powershell
$geoClass = 'source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java'
$geoDiscriminants = @(rg -n '^\s*[A-Z][A-Z_0-9]+\(' $geoClass)
$geoSearchExit = $LASTEXITCODE
if ($geoSearchExit -ge 2) { exit $geoSearchExit }
$geoDiscriminants
$geoDiscriminants.Count
$geoCommon = 'source/shared/common/src/main/java/org/geogebra/common'
rg -n 'createGeoElement|case |return new' "$geoCommon/kernel/GeoFactory.java" "$geoCommon/geogebra3D/kernel3D/GeoFactory3D.java"
$geoFactoryExit = $LASTEXITCODE
if ($geoFactoryExit -ge 2) { exit $geoFactoryExit }
```

Para una sonda por archivo, añadir el tipo XML declarado, su clase cargada, el productor y firma de argumentos, el cierre DAG y de registros, recursos y el resultado independiente de copia y reapertura. Varias instancias del mismo discriminante pueden tener resultados distintos. La matriz incluye filas iniciales para arcos, cónicas focales y polilíneas. Cada ampliación se decide con las pruebas de su productor y su perímetro.

Para ampliar el inventario por productor, empezar por el comando persistido y
su despacho, seguir el procesador y la creación del algoritmo, y confirmar
en `setInputOutput` las salidas, puertos y auxiliares. Buscar además usos
del tipo en algoritmos y procesadores para detectar productores secundarios.
Por ejemplo, este rastreo distingue las construcciones de partes de cónica
que comparten el mismo discriminante.

```powershell
$geoKernel = 'source/shared/common/src/main/java/org/geogebra/common/kernel'
rg -n -g 'Algo*.java' -g 'Cmd*.java' -e 'new GeoConicPart\(' -e 'new AlgoConicPart\w*\(' $geoKernel
$geoProducerExit = $LASTEXITCODE
if ($geoProducerExit -ge 2) { exit $geoProducerExit }
```

El resultado de texto es un índice de entrada. Revisar constructores
indirectos, transformaciones, expresiones dependientes y salidas hermanas en
el código. Para una ampliación real, confirmar la clase y el algoritmo en la
construcción reabierta, y aplicar el clasificador a todo el cierre. La tabla
cubre el vocabulario de tipos del baseline y la matriz documenta los
productores de interés. Variantes adicionales se deciden con esa regla
estática y una sonda propia.
