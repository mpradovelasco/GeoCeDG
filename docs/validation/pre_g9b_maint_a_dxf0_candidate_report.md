# PRE-G9B-MAINT-A-DXF0 — informe del candidato técnico

| Campo | Valor |
|---|---|
| Estado | **PRE-G9B-MAINT-A-DXF0 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW**; smoke manual del autor pendiente. `selfApproved=false` |
| Autorización | instrucción del autor de 2026-11-24 («GeoCeDG — PRE-G9B-MAINT. Segunda ola coordinada», §0, §6, §8); prompt de tarea [`pre-g9b-maint-a-dxf0-dxf-spline-reference-import.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-dxf0-dxf-spline-reference-import.prompt.md) (commit de gobernanza `433d7fa93`) |
| Contratos | [`dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md) revisión 2; [`bspline-2d`](../../geocedg/specs/curves/bspline-2d.md) §3, §11, §12; [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md); [matriz A-N1](pre_g9b_maint_a_n1_validation_matrix.md) §0, DX-1 a DX-13 |
| Base | `228580e9684d2c54650f8d461c2a4d1142a7ef55` (árbol `a4c892aa9…`) |
| Hito de biblioteca | `d22c4cc85aefc454ee8598711d717d21db57da7c` (árbol `2d722f50c…`), rama `phase/pre-g9b-maint-a-math`, declarado hito técnico de dependencia por el coordinador (no es PASS autoral); fusionado en `8cf6cf8f9` |
| Rama y worktree | `phase/pre-g9b-maint-a-dxf0`, `GeoCeDG-wt-maint-a-dxf0` |
| Commits técnicos | `919fd5664` (implementación, pruebas, guías, registro de la fase); `0120cf4ca` (cuatro literales de recuento de acciones restantes); `a29309aff` (pin de encabezados de la guía y redacción «primera vista gráfica»); el commit siguiente añade el inventario de identidades, el pin del registro y este informe (§11) |
| Clase de verificación | `INTEGRATED_PHASE`: `STATIC` + `PHASE PRE-G9B-MAINT-A-DXF0` + `INTEGRATION` (`CORE + FOCUSED`). `FINAL = NOT AUTHORIZED` |
| `GUIDE_IMPACT` | `UPDATE`: sección 11.12 nueva (EN/ES), fila **Archivo** de 3.2, 11.11 y límites conocidos |
| Impacto de bootstrap | ninguno: no cambian requisitos de la estación ni supuestos del bootstrap de Windows |
| `AUTHOR_DECISION` | `NOT_RECORDED_IN_THIS_ARTIFACT` |

## 1. Qué se entrega

**Adaptador compartido fuera del núcleo** (`source/shared/common/src/main/java/org/geocedg/common/importer/dxf/`):

| Clase | Contrato |
|---|---|
| `DxfSplineReader` | §4.1 a §4.6: archivo (tamaño, binario, estructura, `$ACADVER`, `$INSUNITS`, cadenas UTF-8/ISO-8859-1 con escapes), selección (espacio papel, bloques, `INSERT` nunca expandido), códigos de grupo, 13 predicados de admisión en orden, lectura decimal y reconstrucción de pesos; cada entidad se admite o se rechaza de forma atómica |
| `DxfSplineReference` | registro inmutable §5: contrato, naturaleza, bandera y procedencia de pesos, `sourceData`, pesos efectivos, plano, transformación, `data`, dominio, cierre, continuidad estructural, procedencia, diagnósticos, teselación |
| `DxfAffineTransform` | §6: identidad por defecto; `(a·x + b·y) + e` sin FMA; solo puntos de control; `SINGULAR_TRANSFORM` |
| `DxfFileProvenance`, `DxfImportReport`, `DxfRejectedEntity` | §9 y §15: resumen SHA-256 del archivo, nombre base, versión, unidades, recuentos por tipo, rechazos con causa y orden |
| `DxfTessellation` | §8: calculada una vez en unidades del modelo, bisección determinista que parte en cada nudo, `τ = RENDER_REL_TOL · diag(caja del polígono de control)`, límite de trabajo declarado y marca de render incompleto |
| `DxfCause`, `DxfDiagnostic`, `DxfNature`, `DxfImportLimits` | causas tipadas, diagnósticos informativos, naturaleza y límites |

La evaluación usa solo la biblioteca pura `org.geocedg.common.math.bspline`. La prueba
`PreG9BMaintADxf0ImporterBoundaryTest` inventaría el código de producción del adaptador: sin
`Construction`, `GeoElement`, `org.geogebra`, `org.geocedg.common.kernel`, exportadores ni
escritura de archivos, y sin estado estático mutable.

**Desktop** (`source/desktop/desktop/src/main/java/org/geocedg/desktop/`): `GeoCeDGDxfReferenceSession`
(almacén `R-a`), `GeoCeDGDxfReferenceImport` (acción, puertos inyectables), `GeoCeDGDxfReferenceText`
(informe e inspector EN/ES), `GeoCeDGDxfReferenceDialogs` (selector, diálogo de unidades y
transformación, ventana de texto, gestor de la sesión). Cambios en `AppGeoCeDG` (almacén, vaciado
en `clearConstruction`, `resetDocumentSession`, `exitFrame` y `closeWithoutSavePrompt`; aviso no
bloqueante al guardar), `GeoCeDGEuclidianView.paint` (superposición solo en el pintado en vivo) y
`GeoCeDGActionRegistry` (objetivo `geocedg.import.dxf-reference`).

**Acción de menú.** El menú se genera desde `apps/geocedg/application-profile.yml`: la acción
`import.dxf-reference` («Import DXF reference…» / «Importar referencia DXF…»), sus textos y los del
inspector y diálogos están en `localized_text` del perfil, que es donde los diálogos de GeoCeDG
guardan sus textos. El prompt citaba `menu*.properties`; el mecanismo real es el perfil (decisión
del coordinador). La acción va al final del grupo **Archivo → Importar/Exportar** y al clúster
`import-export-authorized`; ninguna acción, modo, valor por defecto ni barra existente cambia.
Consecuencias necesarias: recuento de acciones 137 → 138 en `GeoCeDGProfile` y en siete pruebas;
`tools/agent/workspace-profile-validation.ps1` admite la acción (con una comprobación de que sigue
siendo experimental y de solo lectura) y se re-fija su entrada en
`verification-static-contracts.json` y el pin `static_contracts` del registro (precedente R4, A-1,
B, D1; se declara como desviación de perímetro, §10).

## 2. Constantes

| Constante | Valor aplicado | Origen | Caracterización |
|---|---|---|---|
| `FILE_BYTES_MAX` | 64 MiB | candidato Q-DXF-1 | un archivo de 64 MiB + 1 byte se rechaza tras calcular su resumen (Desktop lo rechaza por tamaño antes de leerlo) |
| `SPLINE_ENTITIES_MAX` | 10 000 | candidato | 10 000 cúbicas se importan y teselan en menos de 1 s en la estación (DX-11, 0,9 s incluido el resumen de 64 MiB); 10 001 se rechazan enteras |
| `P_MAX_IMPORT` | 7 | candidato, alineado con la familia | — |
| `N_MAX_IMPORT` | 1 024 | candidato | trabajo mínimo de render ≈ 2 evaluaciones por tramo + 21 por tramo de profundidad mínima, unas 23 000 evaluaciones para 1 017 tramos, por debajo del límite |
| `W_RATIO_MAX` | 10⁶ | candidato | razón exactamente 10⁶ admitida, 1 000 000,5 rechazada; el barrido racional con razón 10⁶ de la biblioteca queda por debajo de sus constantes |
| `RENDER_REL_TOL` | 10⁻⁴ | candidato | desviación máxima muestreada: Q 8,4·10⁻⁵ con τ = 1,41·10⁻⁴ (65 puntos); H 8,4·10⁻⁵ con τ = 2,24·10⁻⁴ (129 puntos); cúbica 4,66·10⁻⁴ con τ = 5,0·10⁻⁴ (107 puntos) |
| límite de render | 200 000 evaluaciones por registro más los dos extremos de cada tramo; profundidad mínima 3, máxima 24 | declarado aquí (§8) | agotarlo dibuja cuerdas hasta el final del dominio y marca el registro `RENDER_INCOMPLETE` |

Los valores siguen siendo candidatos revisables tras la caracterización (registro de aprobación A-N1).

## 3. Resultados DX-1 a DX-13

Pruebas compartidas: `PreG9BMaintADxf0ReaderTest` (7), `PreG9BMaintADxf0FidelityTest` (7),
`PreG9BMaintADxf0ImporterBoundaryTest` (1). Desktop: `PreG9BMaintADxf0DesktopTest` (7).

| Id | Prueba | Resultado |
|---|---|---|
| DX-1 | `dx1QuarterCircleThreeSeparateChecks`, `dx1SemicircleThreeSeparateChecks`, `dx1PolynomialSpline` | (a) evaluador frente a la evaluación racional exacta de los datos binary64 con los criterios E1/E3 racionales: razón máxima 0,617 (Q y H; constantes 19/20) y 1,57 en la cúbica polinómica (constantes 25/21); (b) curva de peso exacto frente al oráculo: 5·10⁻⁸⁰ (cero a 80 dígitos); (c) discrepancia por redondeo del peso: 1,31·10⁻¹⁷, informada aparte; redondeo de lectura del peso escrito: 2,73·10⁻¹⁷. En H, `u = 1` pertenece al tramo derecho y da `(0, 1)`; derivadas laterales dentro de E3; continuidad publicada `C⁰` |
| DX-2 | `dx2RejectionsFollowThePredicateOrderAndTheRestIsImported` | pesos no unitarios sin bandera, listas parciales (menos y más de `n + 1`), peso cero, negativo, no finito, `NaN`, razón > 10⁶, recuentos incoherentes y nudos decrecientes, cada uno con su causa; orden 10 → 11 → 12 comprobado; el resto del archivo se importa |
| DX-3 | `dx3ExcludedVariantsAreNotImported` | solo puntos de ajuste, periódica, no anclada, 3D, espacio papel, grado 8, contenido de bloque; `INSERT` contado y nunca expandido |
| DX-4 | `dx4ClosureIsDecidedByTheDataAlone` | cierre exacto sin bandera y bandera contradicha diagnosticados (`CLOSED_FLAG_CONTRADICTED`); sin cierre por proximidad ni por los códigos 42 a 44 |
| DX-5 | `dx5FileConstructionXmlAndUndoHistoryAreUntouched` | resumen y fecha de modificación del DXF idénticos; XML de la construcción, tamaño del historial y estados de Deshacer/Rehacer idénticos tras importar, ocultar y quitar; Deshacer y Rehacer no tocan las referencias |
| DX-6 | `dx6ReferencesAreNeitherHitNorSnappedNorIncident`, `exportsExcludeReferencesAndOnlyTheLivePaintShowsThem` | sin impactos en la vista; un clic de la herramienta Punto crea, como mucho, un punto libre independiente sin incidencia; DXF y LaTeX (PGF, PSTricks, Asymptote) idénticos con y sin referencias |
| DX-7 | `dx7RenderIsDeterministic…` (compartida), `dx7TessellationIsViewIndependent…` (Desktop) | teselación idéntica bit a bit entre importaciones; dentro de τ en 16 muestras por segmento; parte en cada nudo; el mismo objeto de teselación a dos zooms, dos tamaños de letra y escala de dispositivo 1 y 2; referencias ocultas no se dibujan; marca de render incompleto |
| DX-8 | `dx8UnitsAndExplicitTransformation`, `transformAndUnitsAreExplicitAndRecorded` | `$INSUNITS` y unidad del documento registrados; aviso no bloqueante con unidades distintas; sin escalado implícito; afín explícita aplicada solo a puntos de control en el orden declarado, nudos y pesos intactos; singular rechazada; cancelar no importa nada |
| DX-9 | `dx9ProvenanceIsCompleteAndIdenticalAcrossImports` | resumen del archivo, nombre base (nunca la ruta), versión, identificador, capa, ordinal, unidades, plano, transformación, contrato y resúmenes de datos idénticos en dos importaciones; ningún campo dependiente del tiempo |
| DX-10 | `dx10DecimalReading` | coma decimal, `NaN`, `Infinity`, hexadecimal y otros textos → `MALFORMED_REAL`; desbordamiento → `NONFINITE_VALUE`; enteros mal formados en 70 a 74 → `MALFORMED_REAL`; redondeo correcto de tokens representativos |
| DX-11 | `dx11FileLimitsAndBinaryDxf` | límites de tamaño y de entidades (rechazo completo, sin prefijo), DXF binario, estructura mal formada y ausencia de `ENTITIES` |
| DX-12 | `dx12SessionLifecycle` | Guardar no escribe referencias (archivo revisado entrada por entrada) y muestra el aviso de sesión; Nuevo, Abrir y Cerrar (ruta sin ventana) vacían el almacén; Deshacer/Rehacer (DX-5) no lo tocan |
| DX-13 | `dx13AdmittedWeightRepresentations` | las seis representaciones con las naturalezas `POLYNOMIAL`, `POLYNOMIAL`, `RATIONAL`, `RATIONAL_EQUAL_WEIGHTS`, `POLYNOMIAL`, `POLYNOMIAL`; bandera y procedencia según el archivo; diagnósticos informativos donde corresponde; pesos implícitos nunca registrados como valores del código 41; resumen con marca de ausencia |

## 4. Fidelidad y evaluación racional

Los registros conservan los binary64 leídos (§4.5). La evaluación usa el de Boor homogéneo de la
biblioteca; los registros de naturaleza `POLYNOMIAL` o `RATIONAL_EQUAL_WEIGHTS` se teselan con el
equivalente polinómico (`BSplineData.polynomialEquivalent`), los `RATIONAL` con la secuencia
racional. Las constantes E1 a E5, también las racionales con escalas homogéneas
(`K_pos^rat(p) = 8p + 3`, `K_der^rat(p) = 8p + 4`, `ρ_der = K_der · 2⁻³³`), se derivaron por el
procedimiento de la matriz §0.2 en la nota de la biblioteca y se validaron en su barrido (razones
observadas máximas 3,68 y 3,67). Redondeo de lectura, redondeo del peso y redondeo de la
transformación se declaran por separado y nunca se cargan al evaluador.

## 5. Procedencia

Por registro: resumen SHA-256 del archivo, nombre base, `$ACADVER` o `UNKNOWN`, identificador,
capa, ordinal en `ENTITIES`, `$INSUNITS` con nombre, unidad de construcción del documento, plano
`z = c`, los seis coeficientes de la transformación, bandera racional y procedencia de pesos,
contrato, resúmenes de la codificación canónica de `sourceData` y `data` (etiqueta
`dxf-spline-reference/v1`, marca `I` para pesos implícitos), datos ignorados y diagnósticos. Nada
depende de la hora de importación.

## 6. Render

Calculado una vez por registro en unidades del modelo; la vista solo aplica su transformación.
Estilo propio (trazo discontinuo morado de 1,5 px) y marca roja en el último punto si el render es
incompleto. El error del render es una estimación, nunca una cota ni prueba sobre la curva. Se
dibuja en la vista gráfica 1 (la vista `GeoCeDGEuclidianView`); la vista gráfica 2 no muestra
referencias.

## 7. Separación del DAG

Las referencias no son `GeoElement`, no están en `Construction` ni en su XML, no tienen
identidad, dependencias ni dibujable; no son impactos ni objetivos de ajuste; ninguna herramienta
ni comando las acepta y no publican medidas. Importar, ocultar y quitar no crean pasos de
Deshacer. Pruebas: DX-5, DX-6, DX-12 y el inventario de frontera del adaptador.

## 8. Q-DXF-3: exportaciones de imagen

Caracterización (prueba `exportsExcludeReferencesAndOnlyTheLivePaintShowsThem`): PNG del servicio
de imagen (también la imagen del portapapeles), impresión del servicio, PDF, SVG, EMF, imagen de
exportación del anfitrión y pintado de impresión del anfitrión son idénticos con y sin referencias:
todas esas rutas pintan mediante `exportPaint`, que no llama al pintado en vivo donde se dibujan
las superposiciones de GeoCeDG. Solo la vista en pantalla muestra las referencias. No se ha
modificado ningún servicio de exportación ni la generación de hojas.

**Limitación declarada y decisión del autor pendiente.** La propuesta aprobada de Q-DXF-3
(«reproducir la vista como presentación, sin afirmación semántica») no se cumple hoy en las
exportaciones de imagen: las referencias visibles no aparecen en ellas. Opciones para el autor:
(a) aceptar la omisión y declararla; (b) autorizar un incremento posterior que añada la
superposición a las rutas de imagen como presentación. Las exportaciones semánticas (DXF, LaTeX)
excluyen las referencias en cualquier caso (DX-6, SEP-4 parcial).

## 9. Verificación

Bucle interno (no evidencia de aceptación): Gradle directo con los filtros de la fase; todas las
pruebas en verde. Evidencia de inventario y ejecuciones de aceptación: ver §11, que se completa
tras el registro del inventario.

## 10. Desviaciones y observaciones

- **Textos de menú en el perfil**, no en `menu*.properties` (§1).
- **Validador del perfil de espacio de trabajo** y pines estáticos tocados por necesidad de la
  acción nueva (fuera de la lista literal del prompt; precedente R4/A-1/B/D1).
- **`.gitattributes`**: los DXF de prueba se fijan con `text eol=lf` para que la comparación byte a
  byte con su constructor no dependa de `core.autocrlf`.
- **Cerrar**: la ruta con ventana (`exitFrame`) vacía el almacén antes de cerrar; la prueba cubre la
  ruta sin ventana (`closeWithoutSavePrompt`), porque el anfitrión de prueba no tiene marco.
- **DPI**: Desktop no tiene un ajuste de DPI propio; DX-7 usa dos tamaños de letra y escala de
  dispositivo 1 y 2 sobre el mismo objeto de teselación.
- **Registro de rutas upstream**: los 26 archivos nuevos bajo `source/` se registran en
  `docs/upstream/modified-files.yml`; la comprobación heredada de frontera ya falla en la base por
  archivos posteriores a 1.2.0 no registrados (observación preexistente, sin cambio).

## 11. Ejecuciones de verificación

**Evidencia de inventario** (productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1`,
actualizador `tools/agent/update-verification-junit-inventory.ps1`; ejecuciones de Desktop con el
cerrojo de la estación):

| Selección | Commit de la evidencia | Resultado | Identidades |
|---|---|---|---|
| `discovery.shared` (`--test-dry-run`) | `919fd5664` | completada, salida 0 | 7 057 → 7 117 |
| `discovery.desktop` (`--test-dry-run`) | `0120cf4ca` | completada, salida 0 | 2 627 → 2 634 |
| `pre-g9b-maint-a-dxf0.shared` (`org.geocedg.common.math.bspline.*`, `org.geocedg.common.importer.dxf.*`) | `919fd5664` | completada, salida 0, 13 clases | 60 |
| `pre-g9b-maint-a-dxf0.desktop` (19 clases) | `a29309aff` | completada, salida 0, 0 fallos | 274 |
| `final.shared` (ejecutada) | `919fd5664` | completada, salida 0 | 7 867 → 8 094 |
| `final.desktop` (ejecutada) | `0120cf4ca` | completada, salida 1: dos fallos | no actualizada |

**`final.desktop`.** La ejecución completa de Desktop tuvo dos fallos:
`PreG9BR6PlusGProductMetadataTest.bothGuidesCoverTheR6PlusCapabilitiesAtStructureLevel`, causado
por la sección 11.12 (recuento de encabezados de nivel 3), corregido en `a29309aff` y comprobado en
la selección de la fase; y `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`,
el fallo histórico registrado en la base desde `9ccefdcf` y en la evidencia de cierre de 1.2.2
([geocedg_1_2_2_closing_evidence.md](geocedg_1_2_2_closing_evidence.md)), ajeno a este incremento.
El actualizador oficial rechaza evidencia ejecutada con fallos, así que la selección `final.desktop`
conserva su huella anterior. Solo `FINAL` (no autorizado) consume `junit.desktop.final.semantic`;
`INTEGRATION` no lo requiere en el registro vigente.

Pin del inventario en el registro y literales de `verification-final-coverage.Tests.ps1`
actualizados (selecciones 100 → 102, fases `PHASE` 90 → 91); la prueba
`verification-final-coverage.Tests.ps1` pasa en local (13 casos, 0 fallos). Las ejecuciones
`STATIC`, `PHASE PRE-G9B-MAINT-A-DXF0` e `INTEGRATION` sobre el commit congelado se registran en el
informe de punto de control al coordinador; `PHASE` e `INTEGRATION` esperan su «GO VERIFY».
