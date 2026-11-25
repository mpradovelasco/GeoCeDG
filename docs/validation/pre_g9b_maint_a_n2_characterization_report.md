# PRE-G9B-MAINT-A-N2 — informe de caracterización y decisiones

> **Nota de R1 (2026-11-25).** Caracterización de A-N2, conservada. Las caracterizaciones
> nuevas de R1 (capacidad y escáneres, transiciones atómicas y Classic, piloto de conformidad)
> están en el [informe R1](pre_g9b_maint_a_n2_r1_report.md) §C y §F; el tope de 4 MiB de §2.2
> queda sustituido por la política de recursos del contrato `R-c` R1.

| Campo | Valor |
|---|---|
| Carácter | Informe técnico de caracterización de solo lectura. `selfApproved=false`. Evidencia, no autoridad |
| Base | `a9ee803bf4be92c58ab7e120111ebacc70055487`, árbol `92f8ab0674587f3bc249ae7964e0eddb18b6a35c` |
| Worktree / rama | `GeoCeDG-wt-maint-a-n2` / `phase/pre-g9b-maint-a-n2` |
| Método | lectura de código con citas `fichero:línea` sobre la base (tres análisis coordinados desde la sesión principal) y una sonda numérica desechable; ningún fichero rastreado se modificó |
| Evidencia conservada | `artifacts/agent/pre-g9b-maint-a-n2-characterization/` del worktree (ignorado por Git): los tres informes de análisis (`char-rc.md`, `char-export.md`, `char-numeric.md`), fuentes y salidas de la sonda, copia de los borradores de partida y `SHA256SUMS.txt` (SHA-256 del manifiesto `d6dd839a34ac9316a6d979ce63e781d587b5674bed19699342ed9d2341694a3f`) |

Las rutas abreviadas son: `JRE` = `source/shared/common-jre/src/main/java/org/geogebra/common/jre/io/MyXMLioJre.java`;
`XIO` = `source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java`;
`AGC` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`;
`GD` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`. Los números de línea son los de la
base.

## 1. Material de partida

Los cuatro borradores de la etapa 4 de la sesión anterior estaban disponibles en
`GeoCeDG/artifacts/agent/pre-g9b-maint-wave2-closeout/stage4-drafts/`:
`rc-persistence-contract-draft.md`, `reference-dxf-export-contract-draft.md`,
`a-dxf1-contract-draft.md` y `maint-interop-implementation-plan-draft.md`. Se escribieron sobre
`228580e96` más candidatos no publicados. Se trataron como material de partida: cada hipótesis
se contrastó con la base publicada (§2 a §4) y ninguna se trasladó sin verificar (§5).

## 2. Persistencia documental (`R-c`)

### 2.1 Contenedor y lector

| Hecho | Evidencia | Consecuencia para el contrato |
|---|---|---|
| `readZip` lee todas las entradas antes del XML; una entrada desconocida va a `loadBitmap`; `ImageIO.read` devuelve `null` y se registra una imagen vacía `<nombre>.png` en `ImageManagerD`; el rollback de apertura no la deshace | `JRE:154-321`, bucle `JRE:172-237`, rama final `JRE:227-229`; `MyXMLioD.loadBitmap` | lectores anteriores: abren, ignoran y dejan una imagen vacía inocua |
| `geocedg_defaults_v2.xml`: solo en documento GeoCeDG (fuera de ese contexto o en macros se omite en silencio), tope 16 KiB, analizador estricto por re-serialización y comparación; UTF-8 inválido da `IOException`; se analiza dentro del bucle ZIP, antes de vaciar la construcción | `V2StyleDefaults.java:26-28`; `JRE:165, 194-206` | precedente de entrada opcional, versionada, acotada y estricta |
| el escritor solo emite entradas conocidas | `JRE:412-494` | la versión con `R-c` debe escribir la entrada; las anteriores la pierden al guardar |
| `<geocedgHiddenLayers>` lo escribe `getFullXML` | `XIO:326-343` | precedente de bloque en `geogebra.xml`, solo con atributos planos |
| un hijo desconocido de `<geogebra>` se registra como «unknown tag», pero sus hijos se leen como elementos de primer nivel; un `geocedgSpatial` anidado lanzaría excepción | `MyXMLHandler` (`startGeoGebraElement`); igual en v1.2.2 (`:732`, `:805`) | un bloque en `geogebra.xml` es inseguro para lectores anteriores |
| `doParseXML` con rollback; A2 añadió `BSplineContractException` a la familia de fallo cerrado; el rollback restaura solo el XML de construcción | `XIO:478-601`, `XIO:538-540` | la restauración del documento completo es la de `AppD` (abajo) |
| v1.2.2 tiene el mismo lector (`MyXMLioJre`, `MyXMLioD`, `ImageManagerD`, `AppD` sin diferencias) | `git diff v1.2.2 HEAD` vacío para esos ficheros | la conducta de lectores anteriores está establecida para 1.2.2 |

### 2.2 Guardado, apertura, Undo, portapapeles, Insertar archivo

| Hecho | Evidencia | Consecuencia |
|---|---|---|
| guardado nativo: temporal en el mismo directorio, `ATOMIC_MOVE`, `DocumentArchivePreflight.validate` sobre los bytes exactos en un `AppDNoGui` desechable | `AppD:3614-3647`; `AtomicDocumentFileWriter.java:35-54` | el almacén se valida en el mismo preflight; la app desechable no instala nada |
| apertura nativa (Abrir, Recientes, arrastrar, API `openFile`, también `.ggb`): captura del archivo previo, restauración ante fallo, `nativeDocumentLoadCommitted` solo tras éxito | `AppD:3341-3404, 3443-3457, 3428-3430` | preparar al leer, instalar al confirmar |
| rutas no transaccionales: `.html`, `setBase64`, `setXML` con vaciado; Nuevo vacía la sesión tras la pregunta de guardar; `GuiManagerD.openURL` vacía **antes** de su diálogo (no conectado en las acciones GeoCeDG) | `AGC:1558-1564`; `GgbAPIJre:101-111`; `App.java:4341-4364`; `GuiManagerD:1839-1843` | instalar solo tras éxito; C-1 obligatoria |
| Undo nativo: una sola entrada `geogebra.xml`; la lectura toma solo la primera | `FileAppState.java:40-50`; `JRE:775-787, 362-378`; `GeoCeDGUndoManager` | el almacén queda fuera de Undo por construcción |
| copiar/pegar ordinario guarda XML en memoria (`CopyPasteD.copiedXML`), sin archivo ZIP. `clipboardResources()` sirve solo a la inserción de imagen del portapapeles: escribe el archivo del receptor, calcula SHA-256 de cada entrada distinta de `geogebra.xml` para probar que no cambian, y su recuperación relee ese archivo | `GD/GeoCeDGDocumentInsertion.java:438-487, 702-735` | el portapapeles nunca transporta el almacén; la relectura de recuperación debe reinstalar el mismo almacén |
| Insertar archivo carga la fuente en una app auxiliar; el receptor no se vacía | `GD/GeoCeDGDocumentInsertion.java:1019-1089` | las referencias de la fuente no se transfieren salvo decisión |
| `App.isSaved()` es verdadero cuando la construcción no tiene contenido relevante, sea cual sea la bandera; el almacén `R-a` nunca llama a `setUnsaved`; las capas ocultas sí, con `hasSaveRelevantDocumentPresentation` | `App.java:1768-1772`; `AGC:501, 520-523` | hoy Nuevo, Abrir, arrastrar y Cerrar descartan referencias sin preguntar; `R-c` debe seguir el patrón de capas ocultas |
| escáneres de inspección experta y de modo angular: entrada desconocida «fuera del piloto»; topes 8 MiB de archivo, 4 MiB por entrada, 32 entradas | `GD/GeoCeDGInspectionArchive:44-47, 328-344`; `GD/GeoCeDGInspectionXml:455-474`; `GD/GeoCeDGAngularSourceArchive:71-72, 213-241` | registrar la entrada; tope del almacén alineado a 4 MiB |
| GeoCeDG Classic diagnóstico (proceso aparte, `org.geogebra.desktop.GeoGebra3D`) ya pierde `geocedg_defaults_v2.xml` y `<geocedgHiddenLayers>` al guardar, contra la regla de preservación de `native-document-identity.md` | `GD/GeoCeDGClassicDiagnosticLaunch.java:43`; `ConstructionDefaults:707-710, 879-882` | desviación previa; decisión RC-D4 |
| sin topes de bytes en la apertura nativa, la URL ni Insertar archivo; el único tope por entrada es el de los valores V2 | `JRE` | el almacén define su propio tope |

### 2.3 Almacén `R-a` y registro

| Hecho | Evidencia |
|---|---|
| `GeoCeDGDxfReferenceSession`: lista de `Entry(record, visible)` direccionada por posición; sin identificador ni nombre (el nombre visible se deriva de capa · handle); `addAll`, `remove(int)`, `setVisible(int, boolean)`, `clear`; único oyente: repintado; R1 no cambió el almacén | `GD/GeoCeDGDxfReferenceSession.java:18-93`; `GD/GeoCeDGDxfReferenceText.java:33-48` |
| `DxfSplineReference` y `DxfFileProvenance`: constructores package-private; los dos resúmenes se recalculan en el constructor | `.../importer/dxf/DxfSplineReference.java:28-71`; `DxfFileProvenance.java:29-35` |
| límites de importación: 64 MiB, 10 000 entidades, `p ≤ 7`, `n + 1 ≤ 1 024`, razón de pesos `10^6`; sin constante de nudos (`n + p + 2 ≤ 1 032`); un registro máximo son unos 4 100 binary64 | `.../importer/dxf/DxfImportLimits.java:16-33`; `DxfSplineReader.java:541-542` |
| codificación canónica y `digestHex(contract, data)`: etiqueta ASCII, `0x00`, `p`, `n + 1`, nudos, puntos, `I`/`E` + pesos, binary64 big-endian, SHA-256 | `.../math/bspline/BSplineCanonicalEncoding.java:14-111` |
| tokens espaciales: 32 hex minúsculos de un generador no criptográfico; las colisiones las evita solo el registro de la construcción | `SpatialIdentityId.java:86-99`; `DefaultSpatialTokenSource:13-59` |

### 2.4 Respuesta a la caracterización C-1 (rutas que tocan el almacén antes de confirmar)

Ninguna ruta GeoCeDG de apertura pierde el almacén ante un fallo: las rutas nativas lo vacían
después de confirmar. Excepciones a tratar por `R-c`: `.html`, `setBase64` y `setXML` son no
transaccionales (un análisis fallido puede haber alterado la construcción mientras el almacén
sobrevive); Nuevo y el restablecimiento sin fichero vacían inmediatamente por su propia
semántica; `GuiManagerD.openURL` vacía antes de su diálogo, pero no está conectado en las
acciones de GeoCeDG. La línea de órdenes se ejecuta antes de que exista el almacén.

## 3. Exportación DXF

| Hecho | Evidencia | Consecuencia |
|---|---|---|
| cadena `GeoCeDGDxfExportController → GeometryExportService → GeoElementGeometryExportAdapter / G9X1GeometryExportAdapter / SemanticCurveExportAdapter2D → GeometryExportModel → DxfExporter` | `GeoCeDGDxfExportController.java:579-585` (población desde `Construction` o selección) | las referencias no entran por diseño; la nueva población se añade explícitamente |
| `AC1015`; handles desde `0x100` en orden del modelo; `$HANDSEED = 0x100 +` número de entidades; los handles estructurales bajo `0x100` | `DxfExporter.java:29-32, 138-140, 269-275` | referencias después de las entidades de la construcción |
| entidad común: `0, 5, 100, 8, 420, 999, 60`; reales con `Double.toString`, `-0.0` como `0.0`; `z` escrito como el entero `0` | `DxfExporter.java:480-507, 493-497` | el escritor de referencias reutiliza el formateador |
| sin escritor `SPLINE`; `AlgoBSpline extends AlgoLocusV2`: la familia sale como `LWPOLYLINE` aproximada con `ESTIMATED_ERROR` (DXF extendido), no soportada en G5 legado, polilínea en LaTeX | `AlgoBSpline.java:47`; `DxfExporter.dxfType` | `MAINT-A3` sigue separado |
| fidelidad: `Exactness {EXACT, APPROXIMATE}`, `SourceExportOutcome.Fidelity {EXACT, APPROXIMATE, UNSUPPORTED, INVALID, DECLARED}`, `Guarantee {CERTIFIED_ERROR_BOUND, ESTIMATED_ERROR, FLOATING_POINT_UNCERTIFIED}` | `GeometryExportModel.java:68-73`; `SourceExportOutcome.java:18-30`; `ApproximationEvidence.java:18-25` | clase `REFERENCE_DEFINITION` en eje propio |
| el preflight solo es escribible si la construcción aporta al menos una entidad | `GeometryExportPreflight.java:105-107` | enmienda de la regla de escritura |
| el DXF debe ser ASCII puro | `DxfPreparedOutput.java:88-100` | cadenas originales solo en el manifiesto |
| manifiesto `schema_version` 3; el validador exige número de entidades codificadas = modelo | `DxfFidelityManifestWriter.java:52, 499-508` | versión 4 y recuentos separados |
| salida pareada con temporales, sin sustitución y con rollback; «Two-file atomicity is not claimed» | `DxfPairedOutputWriter.java:17-22` | sin cambio |
| `$INSUNITS` de la unidad de construcción; sin escalado | `DxfExporter.java:141-142`; `GeometryExportModel.java:31-65` | las referencias se escriben en unidades del modelo sin transformación inversa |
| las salidas de imagen de Gráficos 1 dibujan referencias visibles (R1); Gráficos 2 y la miniatura no | `AGC:853-883`; `PictureExportService.java:298-301` | sin cambio; no son exportaciones semánticas |
| el lector A-DXF0 relee la salida del exportador (ignora `999`, `420`, `60`, `100`, `5`; reales al binary64 más cercano; códigos 41 por orden de aparición; bit 2 rechazado; 210/220/230 registrados) | `DxfSplineReader` | sirve para ida y vuelta, **no** como lector independiente |
| candidato A3 de planificación existente | `docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-a3-bspline-dxf-spline-export.prompt-candidate.md` | coordinar `schema_version`; su restricción racional afecta solo a construcciones |

Puntos de inserción mínimos: opción en `GeoCeDGDxfExportController.chooseRequest`
(`:374-402`) y `RequestChoice` (`:712-723`); instantánea entre el preflight (`:109`) y la
codificación (`:118`); sobrecarga de `GeometryExportService.encode` (`:113-118`); entidades
`SPLINE` añadidas tras el bucle de la construcción en `DxfExporter.encode` (`:100-106`), con
`$HANDSEED` y tabla de capas. Con el conjunto vacío los bytes no cambian; con la opción activa
se desplazan los handles estructurales de las tablas posteriores a LAYER.

## 4. Persistencia numérica de A-DXF1 (riesgo crítico §8 de la instrucción)

### 4.1 Rutas reales

| Elemento | Ruta | Exactitud |
|---|---|---|
| XML de comando | `AlgoElement.getXML` con `StringTemplate.xmlTemplate` (`AlgoElement.java:1335`); argumentos siempre etiquetas (`LocusV2PublicOperations.createBSpline:145-175`, `prepare:1472-1510`) | — |
| reales en XML | `xmlTemplate` fija 15 cifras (`StringTemplate.java:309-330`), pero `Kernel.formatRaw` devuelve antes `Double.toString` (enteros `Long.toString`) para XML (`Kernel.java:1347-1354`) | exacto salvo `-0.0` → `"0"` |
| literales tecleados | `MySpecialDouble` conserva el texto original (`Parser.jj:131, 279-291`; `MySpecialDouble.java:193-203`) | exacto, incluido `-0.0` tecleado |
| puntos y números libres | `<coords>`, `<value>` con `XMLStringBuilder.attr(double)` = `Double.toString` (`XMLStringBuilder.java:96-101`) | exacto salvo `-0.0` |
| listas con definición | escriben el texto de la definición | exacto si el texto lo es |
| **listas sin definición con números de valor decimal exacto** | `GeoNumeric.toValueString` → `Kernel.format(BigDecimal, xmlTemplate)` (`GeoNumeric.java:721-722`; `Kernel.java:1421-1448`), 15 cifras HALF_UP | **pérdida real**: `{0.1+0.2, 1/3, nextUp(1)}` → `{0.3,0.333333333333333,1}` |
| `<geocedgBSpline>` | solo contrato y regla de nudos; ningún número (`BSplineContractXml.java:117-132`); el bloque se conserva como atributos de texto (`:203-225`) | sin riesgo numérico |
| Undo, copiar, redefinir | mismo `getXML` y plantilla (`MyXMLio.getUndoXML:155-182`; `InternalClipboard:147`; `CopyPasteD:379, 418`) | igual que arriba |
| resumen | `getDefinitionDigestHex` sobre los patrones de bits exactos (`BSplineCanonicalEncoding.java:60-87, 136-142`) | un ulp cambia el resumen |

### 4.2 Sonda

Dos pruebas desechables ejecutadas en el checkout principal (mismo commit, árbol limpio) con
un `--init-script` de Gradle, cada una en una sola ejecución:
`:shared:common-jre:test --tests org.geocedg.common.kernel.bspline.ScratchNumericProbeTest` y
`…ScratchNumericProbe2Test`, ambas con salida 0. Rutas: R1 comando literal; R2 puntos libres
(`setCoords`) y lista literal de nudos; R3 puntos libres y números libres (`setValue`); R4
listas sin etiqueta construidas en código. Pasos: crear, guardar/reabrir dos veces,
guardar-reabrir-guardar, Deshacer/Rehacer con un cambio ajeno, copiar/pegar, reabrir tras la
copia, redefinición.

| Conjunto | Resultado |
|---|---|
| DS1 (valores de 17 cifras, par de nudos `1/3` y `nextUp(1/3)`, `nextUp(1e15)`) | idéntico bit a bit en todos los pasos de R1 a R4 |
| DS2 (`-0.0` en coordenadas y nudos, subnormales, `1e300`, `MIN_NORMAL`, `nextDown(MIN_NORMAL)`) | R1 idéntico (texto `-0.0` conservado); R2 difiere solo en coordenadas `-0.0`; R3 y R4 difieren en todo `-0.0`; en esos casos cambia el resumen |
| estabilidad de bytes | R4 no es idéntico en bytes en guardar-reabrir-guardar aunque los valores sí; causa no aislada |
| bloque de procedencia | se conserva al mover un punto; la redefinición ordinaria fue rechazada; la **redefinición compatible por sustitución** (`BSpline({A..E},2)`) tuvo éxito y **perdió el bloque** |

Limpieza: clase auxiliar eliminada de `build/tmp`, `build/test-results/test` restaurado desde
copia, `git status --porcelain` vacío en el checkout principal.

### 4.3 Conclusión

La advertencia de 15 cifras es falsa para las rutas que usa `BSpline`; existe una ruta real de
15 cifras que A-DXF1 debe evitar, y dos defectos reales: `-0.0` y la pérdida del bloque en la
redefinición por sustitución. Ninguno exige cambiar el formato numérico general del núcleo ni
una semántica de persistencia compartida: **no se emite condición de parada**. Solución mínima,
recogida en el contrato A-DXF1 §4.2, §12 y §13.1: normalizar `-0.0` antes de crear, usar solo
rutas exactas (R2 o R3), autocomprobación tras crear, conservar el bloque en la redefinición
por sustitución y añadir una fábrica GeoCeDG para adjuntarlo. Una codificación exacta propia
dentro de `<geocedgBSpline>` no es necesaria y duplicaría los datos de definición. No existe hoy
ninguna prueba que afirme ida y vuelta bit a bit de binary64 por XML (`MaintA2BSplinePersistenceTest`
compara bytes y resúmenes con decimales cortos): NUM-1 a NUM-10 la proponen.

## 5. Hipótesis de los borradores

| Borrador | Hipótesis | Estado | Efecto |
|---|---|---|---|
| R-c | entrada desconocida → `loadBitmap`, fallo solo registrado | confirmada con matiz (imagen vacía registrada) | §2 del contrato `R-c` (F1) |
| R-c | `geocedg_defaults_v2.xml` como precedente | confirmada con precisiones | F2 |
| R-c | escritor solo con entradas conocidas; capas ocultas; rollback; guardado atómico; captura y restauración; ganchos de confirmación; Undo de una entrada | confirmadas (rollback ampliado por A2) | F3 a F7 |
| R-c | «el portapapeles nativo escribe el archivo completo y transporta toda entrada distinta de `geogebra.xml`» | **refutada**: el copiar ordinario no usa ZIP; la inserción de imagen solo comprueba que no cambian | F9; desaparece el filtro de recursos que proponía el borrador |
| R-c | Classic descarta entradas GeoCeDG | confirmada; también descarta capas ocultas (desviación previa) | RC-D4 |
| R-c | `R-a` por índice, vaciado en los puntos citados | confirmada; R1 no lo cambió; nunca marca el documento como modificado | F10, F11, §8 del contrato |
| R-c | registro inmutable y codificación canónica | confirmadas; constructores package-private | F12, F13 |
| R-c | tope de entrada de 64 MiB | **revisada**: los escáneres fijan 4 MiB por entrada | RC-D9 |
| R-c | `STRING_NOT_SERIALIZABLE` al guardar | **sustituida**: codificación por bytes escapados; el guardado nunca falla por una cadena | §5.2 del contrato |
| exportación | exportaciones de imagen sin referencias | **refutada**: R1 las incluye en Gráficos 1 | §2 del contrato de exportación |
| exportación | cuatro clases de fidelidad | **corregida**: cinco (`DECLARED`) | §2 |
| exportación | estructura de pares, handles, capas, manifiesto v3, sin `SPLINE`, lector reutilizable | confirmadas | §2, §4 |
| exportación | nuevas: preflight exige una entidad de construcción; DXF solo ASCII; validador con recuento igual | **hechos nuevos** | §8 a §10 |
| exportación | seis estados de disposición | **revisada**: tabla de ocho estados de la instrucción §6.1 más la elección del usuario; el estado 4 con alternativas | §6 |
| A-DXF1 | lectura del bloque `version="1"` como infoset; sin fábrica pública | confirmadas | §2 hechos 1 y 2 |
| A-DXF1 | `AlgoBSpline` conserva el bloque en la redefinición compatible | **refutada en parte**: solo en la misma instancia; la ruta de sustitución lo pierde | AX-D13 |
| A-DXF1 | resumen de definición, `polynomialEquivalent`, `BSplineExactRational` package-private, `CmdBSpline` con puerta de Locus V2 | confirmadas (`BSplineData.java:312`; `BSplineExactRational.java:15`; `CmdBSpline.java:36`) | §2 |
| A-DXF1 | «las listas literales podrían escribirse con 15 cifras» | **refutada** para las rutas de `BSpline`; existe otra ruta de 15 cifras | §12 |
| A-DXF1 | el conjunto de verificación incluye las muestras de ajuste para garantizar `residuo ≤ estimación` | **rechazada**: contradice el conjunto distinto de import §14.1; se adoptan tres conjuntos disjuntos (ajuste, refinamiento, verificación reservada) y la aceptación exige ambas medidas `≤ ε` | AX-D3 |
| A-DXF1 | cota certificada «alcanzable ahora» con racional exacta | **revisada**: matemáticamente válida; coste sin medir; separada en `MAINT-A-DXF1b` | AX-D2 |
| plan | EXP no necesita la persistencia de RC; DXF1 no necesita el exportador | confirmadas | plan §2 |
| plan | DXF1 independiente de RC | **revisada**: estricta por AX-D1 | plan §2 |

## 6. Compatibilidad arquitectónica

- AGENTS §4: el almacén es modelo documental fuera del núcleo; la exportación es adaptador;
  A-DXF1 es biblioteca pura más servicio externo, con código GeoCeDG aditivo solo donde
  `bspline-2d.md` §10.5 lo reserva.
- AGENTS §1.7 y §17: ninguna referencia entra en el DAG; no hay segundo DAG; la relación es
  procedencia pasiva.
- AGENTS §1.8: la exportación de referencias es transferencia de definición, declarada con su
  clase propia; la aproximación declara cada medida con su grado.
- AGENTS §1.10: ninguna asociación por etiquetas, orden o coincidencia visual.
- AGENTS §13: el exportador no resuelve geometría; formatea datos del registro.
- ADR 0016: el almacén es contenido nuevo del archivo; ADR 0044 es la decisión de
  compatibilidad que exige.
- ADR 0014: su decisión 8 se precisa para referencias en ADR 0045; para construcciones sigue
  vigente.
- ADR 0043: su decisión 11 se precisa en ADR 0045 (lectura informativa del bloque); no requiere
  un ADR nuevo para A-DXF1.

## 7. Límites de esta caracterización

- Sin ejecución de 1.2.2 aislada, Classic ni lectores externos: son C-2, C-3 y RX-16.
- La sonda numérica cubre los dos conjuntos y las cuatro rutas descritos; no cubre Insertar
  archivo ni la ruta de imagen del portapapeles (NUM-5).
- La causa de la inestabilidad de bytes de R4 no se aisló.
- No se midió el coste de la cota exacta ni el del almacén en sus límites (C-4, AP-12).
