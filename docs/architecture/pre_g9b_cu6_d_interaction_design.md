# PRE-G9B-CU6-D — Interacción y presentación

**Candidato de diseño.** La puerta técnica CU5 se comprobó sobre el commit `85ae467063c1d6ad382fa8809a6c5a3a348ca04e`, árbol `59f63ec87780e7517b0f56c15710973d9c7bb96b`, con PHASE y el smoke Desktop del modelo autoral, ambos con código 0. Este expediente
registra las decisiones locales DF-1 a DF-6 y el perímetro de implementación.
La caracterización de código corresponde al baseline publicado
`b842165733261126e0f742586834e2bfc612b6f1`, árbol
`607af3a916025d8a1bbd5cc8f4a21c9a95d7fd66`. Antes de implementar se cotejarán
los puntos de extensión que haya cambiado el checkpoint CU5.

## Autoridad y colocación

El [plan consolidado](pre_g9b_cu_corrective_usability_plan.md#cu6--interacción-y-presentación)
sitúa M07 primero y obliga a demostrar identidad y vigencia. El [estudio
CU0 de diálogos](pre_g9b_cu0_redefinition_and_canvas_picking_design.md#parte-2--m07-nombres-de-objetos-del-lienzo-en-los-diálogos)
propone selección explícita y restauración del foco. Su propuesta de
resolver un objeto anterior por etiqueta queda desplazada por la precisión
autoral M07 del plan consolidado. El [plan candidato de CU0](pre_g9b_cu_corrective_usability_plan_candidate.md)
define los seis asuntos DF y conserva el análisis histórico.

El diseño pertenece a Swing Desktop, salvo los ajustes de presentación del
desplazamiento M03 en el código compartido de etiquetas. Se conservan los
productores, tipos, órdenes internos, unidades, DAG, rutas de persistencia,
identidad durable y semántica de Locus V2. La selección gráfica localiza un
objeto ya existente y ofrece su nombre al campo. Los comandos y manejadores
heredados siguen determinando qué expresiones y tipos admite cada campo.

## M07 y DF-5 — Selección acotada por campo

### Matriz positiva y negativa

| Diálogo y entrada normal | Campo habilitado y ejemplo | Exclusiones y semántica preservada |
|---|---|---|
| Punto por dirección semántica, `GeoCeDGLocusV2Dialogs.createSemanticPoint` | Parámetro numérico. Con `t=0.25` ya existente, seleccionar su deslizador o su fila algebraica e insertar `t` en el campo. | Rama es una clave textual. La fuente Locus V2 preseleccionada y el contrato de `createSemanticPoint` conservan sus guardas. El selector Q de CU7 es independiente. |
| Rotar Locus V2, `GeoCeDGSimilarityTools.click` | Ángulo. Seleccionar el ángulo numérico `α` existente. | Centro y fuente son operandos previos. Se conserva `evaluateToNumeric`, su tratamiento angular y la operación transaccional. |
| Dilatar Locus V2, mismo punto de entrada | Factor. Seleccionar el número `k` existente. | Centro, fuente, admisibilidad del factor y similitud conservan su contrato. |
| SplineV2 con grado, `GeoCeDGSplineV2Authoring.hostDegreeDialog` | Grado. Seleccionar el número `d` existente. | Los puntos conservan el orden de selección y se pasan por instancia. Conservar `NumberInputHandler`, `DegreeInput` y la validación del comando. |
| Radio, ángulo dado, rotación ordinaria, dilatación ordinaria, segmento dado y polígono regular | Sin botón M07 en esta entrega. | `DialogManagerD` abre seis subclases con manejadores y operandos propios. Sus modos de herramienta y compromisos quedan como hoy. La extensión posterior requiere probar cada entrada, incluidos Enter, Aplicar, confirmación asíncrona y operaciones con varios operandos. |
| Redefinir, Renombrar y Texto | Sin cambio de ruta en esta entrega. | Se conserva el mecanismo heredado. Redefinir implica vigencia del objeto editado. Renombrar declara un nombre. Texto tiene un editor estructurado y una vista previa propia. Ampliar M07 aquí sería una decisión de alcance separada. |
| Casillas, Crear herramienta, inspector, trazas, formularios de archivo, unidades y exportación, confirmaciones y reportes de inserción | Sin botón M07. | Tienen contratos de selección o texto distintos. Los resultados ricos conservan sus tokens y selector dedicado. |

Estos cuatro campos cubren las tres familias de diálogos numéricos de GeoCeDG
identificadas por CU0, con Rotar y Dilatar como dos entradas. Las rutas
heredadas que CU0 sugirió migrar permanecen como opciones de ampliación.
La reducción de superficie evita migrar simultáneamente manejadores de
Redefinir, editores estructurados y acciones heredadas que ya tienen otro
mecanismo. Classic conserva el diálogo original.

### Sesión Swing

Cada `AppGeoCeDG` admite un único diálogo M07 abierto, en el EDT.
Un botón bilingüe «Seleccionar objeto» / «Pick object» arma el campo
habilitado y ofrece el mismo gesto desde teclado mediante su mnemónico.
El botón y un estado breve indican si se espera una selección o se ha
invalidado una referencia. El diálogo conserva su campo, validación y
botones de aceptación y cancelación.

Las llamadas numéricas actuales esperan el resultado del diálogo modal.
Se conserva ese punto de retorno con un `JDialog` modeless propiedad del
marco y un `SecondaryLoop` AWT acotado a su vida. Esto permite al propietario
recibir el gesto de selección. Una puerta Swing temporal cubre las raíces
del documento propietario durante toda la apertura, incluidas las vistas
separadas de ese documento cuando existan. Las demás ventanas de GeoCeDG
mantienen su funcionamiento independiente. La puerta impide interacción
con el anfitrión salvo el clic explícitamente admitido al armar el campo.
Un panel de vidrio temporal puede resolver el origen mediante coordenadas
convertidas y acceder a los hits directamente. Se conserva y restaura el
panel previo y su visibilidad, sin reemplazar un aislamiento ya activo.
Se prepara el loop antes de mostrar y se libera en la misma limpieza al
cancelar, cerrar o confirmar. El fallo de apertura libera toda adquisición
parcial. No abrir un segundo selector encima del existente.

| Estado | Gesto y efecto |
|---|---|
| Editando | El diálogo conserva el foco. El lienzo propietario permanece protegido como con el diálogo modal de origen. |
| Armado | Botón o mnemónico conserva la selección del campo. Un clic izquierdo ordinario en Gráficos 1 o en una fila de Álgebra puede aportar un objeto. Derecha, rueda, arrastre, doble clic, iconos de visibilidad, grupos, el buscador y otras vistas no actúan sobre el anfitrión. |
| Elección superpuesta | Si el rótulo pulsado identifica un candidato admisible, usar su objeto. Ante varios cuerpos elegibles mostrar sus nombres en una elección explícita del mismo diálogo. Si cambia la vigencia entre la elección y su confirmación, descartar esa oferta. |
| Selección aceptada | Insertar un único token, sustituir la selección textual o insertar en el cursor y conservar el resto del contenido. Utilizar el nombre para el analizador con separadores según el campo. Devolver foco y cursor tras la inserción. Armar de nuevo admite selecciones sucesivas. |
| Selección rechazada | Campo y construcción conservados. Informar de selección vacía, tipo incompatible o referencia caducada. Permitir otra elección. |
| ESC | Primer ESC armado cancela la captura y devuelve el foco. ESC en edición, botón Cancelar o cierre por X terminan el diálogo. Consumir las fases del mismo gesto de teclado y los remanentes de ratón. |
| Cambio de documento propietario | Cancelar el diálogo y su captura. Una ventana de otro documento no aporta objetos. Un cambio autorizado de historial/reconstrucción invalida las elecciones y vuelve a exigir una selección. |
| Confirmación | Revalidar objetos y operandos capturados antes de cada consumo. Invocar el manejador original una sola vez con la entrada vigente. Una selección caducada mantiene el texto y presenta un diagnóstico en el diálogo. Un operando previo caducado obliga a reiniciar la herramienta. |

En Gráficos 1 se pueden usar `EuclidianView.getLabelHit`,
`setHits(GPoint, PointerEventType)` y `getHits`, con coordenadas relativas
al panel y sin despachar al controlador de herramienta. En Álgebra se
resuelve `TreePath` y `GeoMutableTreeNode.getGeo` sobre el árbol actual.
Una fila de un objeto oculto en gráficos puede ser válida. La captura
respeta la elegibilidad del objeto, sus rótulos reales, el documento y el
paso del protocolo. La previsualización de herramientas queda fuera de la
ruta. La puerta no cambia `App.setMode` ni las memorias F2 y CU5. Una
intercepción a nivel de controlador tendría que preceder incluso a
`EuclidianControllerD.mousePressed`, que llama a `prepareModeForFreehand`
después de `wrapMousePressed`. Esta es la razón para resolver el gesto en
la puerta Swing y consumir su secuencia completa.

`AppD.dispatchKeyEvent` cierra un diálogo ante ESC antes de su binding
Swing ordinario. El punto de extensión es `AppGeoCeDG.dispatchKeyEvent`,
limitado a la raíz del documento dueño y al diálogo de la sesión.
La precedencia es aislamiento de inserción, diálogo de argumentos, menú
compacto, comportamiento heredado. El teclado del diálogo mantiene la
edición ordinaria. El teclado del anfitrión no activa herramienta, Undo,
Redo ni nuevas ventanas mediante el mismo gesto capturado. La elección se
rechaza si un panel CU7 o un diálogo de inserción posee ya la interacción.
Se debe terminar la sesión ante cierre del documento, pérdida del
propietario o transición incompatible. Al cambiar de foco hacia una
ventana ajena se desarma la captura sin perder la edición vigente.

### Identidad y consumo

La sesión conserva una referencia fuerte a cada instancia capturada,
la construcción y el registro de identidad de origen y, cuando ya existe,
la evidencia del ID asignado. No asigna IDs. Al recibir una elección,
verifica que la instancia pertenece al documento actual y al paso
disponible, que su tipo es numérico y que su etiqueta es una referencia
analizable. Antes de confirmar, repite esas comprobaciones, incluida una
consulta lexical sin autocreación
`Kernel.lookupLabel(label, false, SymbolicMode.NONE) == captured`. La
consulta confirma el nombre de una instancia ya verificada. Una instancia
nueva con la misma etiqueta, aunque tenga las mismas coordenadas o tipo,
jamás sustituye la anterior. Se prefiere rechazo conservador tras
reconstrucción, incluso si hay un ID durable coincidente.

Cada token insertado mantiene una marca transitoria en su documento Swing.
Las ediciones externas al token desplazan sus límites. Si el usuario
modifica o retira el token, el campo deja de tratarlo como una referencia
tomada del lienzo. La revalidación comprueba también que el tramo sigue
siendo la misma secuencia lexical, con límites de token compatibles.
No se pasa al analizador un token marcado e inválido. Una coincidencia
textual que resulte de deshacer un cambio de texto no restaura por sí sola
una prueba de identidad. Los tokens escritos manualmente conservan las
reglas normales del analizador. Una nueva selección expresa reemplaza el
tramo inválido, sin reescribir el resto.

La reconstrucción puede reciclar el objeto `Construction`, por lo que esa
identidad sola es insuficiente. Mientras vive el diálogo, el
`EventDispatcher` provee `REMOVE`, `RENAME`, `UNDO`, `REDO` y
`EventListener.reset()` al limpiar vistas. Una baja o cambio de nombre de
una instancia elegida invalida su prueba de manera irreversible hasta una
elección nueva. Undo/Redo invalida todas. Un cambio de documento cancela la
captura y el diálogo completo. Las comprobaciones de cierre cubren también
casos con notificaciones suspendidas. Un objeto de un paso posterior es
inutilizable. En este diálogo se exige nueva selección tras una baja del
protocolo. H05 conserva su propia política de suspensión para memorias
Paralela/Perpendicular.

Además del texto se revalidan los operandos del gesto inicial. Para Punto
se verifica la fuente Locus V2 por instancia y vínculo vigente. Para
similitudes se conservan fuente, operando y modo originales, con los
controles de identidad existentes antes de publicar. Para Spline se valida
cada punto ordenado tras salir del diálogo y antes de formar el comando.
Cuando el manejador entrega su resultado mediante callback, la segunda
comprobación antes de publicar cubre cualquier reentrada durante su
evaluación. El mecanismo no cambia la interpretación de los argumentos ni
la interacción CU7 o de inserción experta.

## DF-1 — B04, índice de salida ordinario

Insignia discreta al seleccionar un punto en Gráficos 1,
solo en la vista interactiva. La ruta existente de pintado en
`GeoCeDGEuclidianView.paint` separa el lienzo vivo de `exportPaint`. Ambas
variantes ordinarias de `CmdIntersect` usan el mismo productor
`AlgoIntersectLineConic` obtenido del dispatcher. Se lee
`getIntersectionPoints()` del padre actual y se usa el índice base uno
del elemento que sea la misma instancia del punto visible y definido.
Esta lectura evita crear una intersección o reordenar soluciones por sus
coordenadas. Dejan de verse insignias al perder la selección,
desaparecer la solución, cambiar el productor o retirarse de la vista.
La insignia no cubre salidas de `AlgoIntersectSingle`, otras familias,
tokens Locus V2, 3D, impresión o exportación. El índice actual es texto de
presentación y no se persiste.

## DF-2 — M02, ESC en el menú compacto

ESC cancela el texto pendiente del grosor y cierra el menú en una sola
acción, con el modo y las ediciones ya aceptadas conservados. El
`FieldPopupListener` de `GeoCeDGEuclidianStyleBar` confirma al ocultarse,
y el campo también escucha pérdida de foco. Marcar la cancelación y
restaurar el valor mostrado antes de ocultar impide una confirmación por
esas dos vías. Las ediciones explícitas y el cierre ordinario conservan
la política F4-A. Consumir el gesto en el despacho de teclas de la
aplicación propietaria, antes de la ruta heredada que podría elegir Mover.
Cubrir popups ligeros y separados, editor enfocado y foco en el lienzo,
escala de interfaz, texto válido e inválido, un solo Undo para cambios
confirmados y cero Undo por ESC.

## DF-3 — M03, desplazamiento de etiquetas

En GeoCeDG, el gesto de arrastre usa el recorrido útil de la vista en
píxeles lógicos sin el radio heredado de 80. La etiqueta puede llegar a
cualquier posición visible que permita el arrastre. Cuando el ancla y el
desplazamiento queden fuera de la vista tras zoom, pan o cambio de tamaño,
se limita la posición dibujada al borde visible en vez de volver al ancla.
El valor guardado conserva el desplazamiento elegido y se usa de nuevo
cuando hay espacio. La vista aplica las correcciones de margen y fórmulas
indexadas de la familia correspondiente.

`EuclidianController.moveLabel` delega al setter compartido
`GeoElement.setLabelOffset`. `Drawable.addLabelOffset` descarta
desplazamientos fuera de su banda, lo que produce el salto. Acotar el
cambio a la entrada de arrastre y a la colocación de etiquetas para
GeoCeDG, conservando la política heredada de Classic, texto absoluto,
cotas y otros usos del setter. Calcular la suma y el recorte con
intermedios anchos para admitir enteros XML históricos. El `labelOffset`
existente y su lector siguen siendo autoridad, con la misma semántica de
píxeles lógicos. No crear etiquetas, relaciones geométricas ni nuevos
campos de persistencia. Cotejar arrastre largo, zoom, DPI, reapertura,
Undo/Redo, caso ángulo con punto de apoyo y salida/exportación pertinente.

## DF-4 — M06, navegación por nombre

Buscador en la vista de Álgebra de GeoCeDG, accesible desde su barra y
teclado. Coincidencia literal parcial con coincidencias exactas primero.
Comparación Unicode NFC y plegado de mayúsculas independiente del idioma.
Aceptar notación de subíndice con o sin llaves y sus dígitos Unicode como
equivalencias de búsqueda. Mantener los caracteres especiales literales.
La consulta nunca interpreta una expresión ni filtra el modelo. En una
consulta se recorre el árbol en su orden visible de categorías, incluyendo
nodos de categorías colapsadas que estén presentes en el modelo.
Los auxiliares omitidos por la configuración quedan fuera. Anterior y
Siguiente navegan las coincidencias, seleccionan el nodo actual, expanden
su categoría y desplazan la fila a la vista. La selección usa el objeto
del nodo vivo. Renombrado, cambios de categoría, altas y bajas y cambios de
modelo invalidan la lista. Coalescer actualizaciones en EDT y revalidar el
nodo antes de seleccionar. El buscador conserva la configuración de
orden, visibilidad y auxiliares. Queda inactivo durante una selección de
argumentos M07. Fixture de 1500 o más objetos con un recorrido lineal por
consulta y un desplazamiento por navegación. Los tiempos quedan como
diagnóstico.

## DF-6 — M10, información complementaria

Calcular el tooltip de la fila actual de `GeoCeDGAlgebraView` al solicitarlo.
El renderer usa Valor para un objeto independiente sin definición aun
cuando se selecciona otro modo. A partir del modo realmente mostrado,
Valor ofrece Definición y Definición o Descripción ofrecen Valor. Usar
`GeoElement.getDefinition(StringTemplate)` y
`toValueString(StringTemplate)` para la información que ya produce el
kernel. Si `isDefined()` es falso, indicar valor indefinido. Si el objeto
carece de definición, indicar que no está disponible. Localizar los textos
en español e inglés. Limitar la salida larga a 240 puntos de código más
elipsis antes del escape HTML y conservar la notación de subíndice
mediante el formateador existente donde proceda. Es un límite local para
que el popup mantenga un tamaño manejable. El álgebra, su renderizado
normal y los datos subyacentes conservan sus cadenas completas. No
mostrar información de filas retiradas ni de cabeceras.

## Ejecución y prueba

Primero entregar el diálogo M07 y sus guardas. Completar en incrementos
acotados B04, M02, M03, M06 y M10. Los cambios de fuentes heredadas se
limitan a los puntos necesarios y quedan registrados en el inventario
upstream. Los candidatos se validan mediante
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU6` con selección
registrada y medida, y `-Profile INTEGRATION` sobre el SHA congelado. La
clase es `INTEGRATED_PHASE` por rutas compartidas de presentación, edición
y persistencia y por los cruces con selección y protocolo. `STATIC` cubre
el expediente. No se necesita un nuevo prerrequisito ni modificar el
bootstrap. La adición de entradas y ayudas visibles tiene impacto de
guía y debe registrarse conforme al contrato vigente, preservando las
obligaciones reservadas a CU9 y CU10.

| Bloque | Verificación focal y adversaria |
|---|---|
| M07 | En cada positivo, inserción entre texto, sustitución de selección, subíndice, dos selecciones sucesivas, objeto de etiqueta visible y fila algebraica. Probar un objeto no numérico, pasos del protocolo, otra ventana, superpuestos, casilla/guion debajo del clic y doble clic. Comprobar documento/Undo/DAG y estado del modo antes y después de una captura sin confirmar. Forzar eliminación y alta homónima, renombrado, Undo/Redo, reconstrucción, sustitución de un operando y cierre de ventana. Preservar validez de una selección al editar texto fuera de su tramo. Verificar tecla ESC, X, Cancelar, OK y foco sin pérdida de texto. |
| B04 | Intersección secante, tangente y disjunta, segmento recortado, cambios de entradas, continuidad y orden de argumentos. Comparar el índice mostrado con la variante indexada bajo el mismo estado. Confirmar ausencia de marcas en resultados ricos. |
| M02 | Popup ligero y pesado, foco dentro y fuera del editor, ESC con grosor editado válido e inválido, Enter, clic fuera y sincronización del Undo. Comprobar el modo y la memoria de CU5. |
| M03 | Etiqueta indexada más allá de 80 px, bordes y retorno tras zoom, DPI, guardado, reapertura y Undo/Redo. Probar segmentos, rayos, polígonos, cónicas, vectores, ángulos y Classic. |
| M06 y M10 | Coincidencia literal y subíndice, lista grande, cambio de modo y de modelo, valores indefinidos, cadenas largas, ES/EN, descripción de un independiente y etiqueta con HTML especial. |
| Transversal | Regresiones CU5, aislamiento CU4 y experto, Punto CU7 y Classic. Smoke Desktop sobre un documento guardado que recorra los cuatro positivos, separe las capturas de las herramientas, navegue protocolo y compruebe la vista algebraica y el menú de estilos. Registrar smoke del agente y autoral por separado. |

La ruta M07 representa una decisión de presentación. Cambiar los contratos
de referencia de un campo, habilitar selección semántica mediante tokens
en diálogos ordinarios o conservar una captura a través de reconstrucción
mediante una equivalencia nueva requieren decisión autoral específica.
