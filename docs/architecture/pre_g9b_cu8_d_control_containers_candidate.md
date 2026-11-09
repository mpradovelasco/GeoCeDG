# PRE-G9B-CU8-D — Contenedores de controles

**Candidato de diseño M05 para decisión autoral.** Caracterización estática sobre
`b842165733261126e0f742586834e2bfc612b6f1`, árbol
`607af3a916025d8a1bbd5cc8f4a21c9a95d7fd66`. La especificación que habilitaría CU8-I
requiere adopción expresa. M05.1 ya pertenece al cierre de CU4.
La [disposición autoral de 2026-11-09](#5-adopción-y-puertas-de-cu8-i-2026-11-09)
adopta la dirección y acota las decisiones de formato pendientes.

Fuentes de alcance y compatibilidad.
[Plan consolidado](pre_g9b_cu_corrective_usability_plan.md) ·
[Estudio CU0, M05 y DA-6](pre_g9b_cu_corrective_usability_plan_candidate.md) ·
[Decisión CU0](../validation/pre_g9b_cu0_author_decision_sheet.md) ·
[Cierre CU4](../validation/pre_g9b_cu4_closeout_record.md) ·
[Identidad documental](../../geocedg/specs/ui/native-document-identity.md) ·
[Espacios de trabajo](../../geocedg/specs/ui/cedg-workspaces.md) ·
[Inserción F3/P4/B2](../../geocedg/specs/ui/document-insertion.md) ·
[Inserción experta](../../geocedg/specs/ui/expert-document-insertion-candidate.md)

## 1. Qué permite el código actual

Las rutas de código son relativas al baseline. Los símbolos identifican los
puntos que deberá cubrir la implementación y sus pruebas.

| Fuente | Hecho observado | Implicación para M05 |
|---|---|---|
| [`Group`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/groups/Group.java), [`Construction`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java) | El grupo conserva instancias, asigna `parentGroup`, escoge un líder por índice constructivo para la navegación y afecta al orden de dibujo mediante `createGroupFromSelected`. `getGroupsXML` escribe `<group l0="…" l1="…"/>`. | Agrupación heredada sin identidad de panel, ancla, tamaño ni contorno. |
| [`MyXMLHandler`](../../source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java), [`ConsElementXMLHandler`](../../source/shared/common/src/main/java/org/geogebra/common/io/ConsElementXMLHandler.java), [`SelectionManager`](../../source/shared/common/src/main/java/org/geogebra/common/main/SelectionManager.java) | `handleGroup` resuelve los valores de los atributos con `lookupLabel`. Seleccionar un miembro puede seleccionar el grupo y el controlador usa el grupo para la selección enfocada. CU0 caracteriza la ausencia de una interfaz Desktop de Group para esta función. | Reutilizar su pertenencia cambiaría el comportamiento de selección. Añadir atributos al grupo sería inseguro para el lector anterior, que intenta resolver todos sus valores. |
| [`GeoNumeric`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoNumeric.java), [`DrawSlider`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/draw/DrawSlider.java) | El deslizador admite posición absoluta o de mundo. `getSliderLocation` suministra el origen de dibujo y también lo consulta el controlador al iniciar el arrastre. El ancho absoluto está en píxeles lógicos. | Conservar el mecanismo nativo de valor y limitar la primera versión a deslizadores ya absolutos. El ancla de mundo se aplica a la presentación del panel. |
| [`GeoButton`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoButton.java), [`GeoInputBox`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoInputBox.java), [`DrawButton`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/draw/DrawButton.java), [`DrawInputBox`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/draw/DrawInputBox.java) | El botón puede proyectar un punto inicial en `getScreenLocX/Y(view)`. El campo hereda su posición y además mantiene el widget activo en `updateBoxPosition` y `setWidgetVisible`. | La caja dibujada, el hit y el editor Swing o simbólico deben usar el mismo origen efectivo. Una mera traslación de gráficos deja el editor en otra posición. |
| [`GeoBoolean`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoBoolean.java), [`DrawBoolean`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/draw/DrawBoolean.java) | `isAbsoluteScreenLocActive` es siempre verdadero. `setRealWorldLoc` y el cambio de modo carecen de efecto. El dibujo lee el origen absoluto. Un `startPoint` presente tiene precedencia sobre los offsets al leerlo. | Un cambio de modo individual del host no puede realizar el anclaje común. Se excluyen casillas con `startPoint` propio en v1. |
| [`EuclidianController`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianController.java), [`GeoCeDGEuclidianView`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianView.java), [`GeoCeDGEuclidianController`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java) | El movimiento de controles distingue el cambio de valor, el arrastre de posición y las rutas de clic. Existen especializaciones GeoCeDG de vista y controlador. | Reservar el arrastre del encabezado y de los tiradores al modo de disposición. Reutilizar activación de los hijos. El contorno debe entrar antes de dibujar los controles, pues el `paint` actual de GeoCeDG añade sus overlays después de `super.paint`. |
| [`EuclidianView`](../../source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianView.java), [`GeoCeDGLayerWorkspace`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLayerWorkspace.java) | Se filtran capas al pintar y detectar objetos. `drawObjects` y `drawActionObjectsOnShownLayers` se usan en más de una ruta de dibujo. | Aplicar el filtro de panel además del nativo, en un contexto de vista en vivo. Verificar por separado hit, pintura y foco, y la ruta de exportación. |
| [`GeoElement.getElementOpenTagXML`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java), [`MyXMLio.getUndoXML`](../../source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java), [`GeoCeDGUndoManager`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUndoManager.java) | El escritor de elementos ya admite atributos GeoCeDG para identidad espacial. La apertura de `construction` lee atributos por clave. Archivo e historial comparten `Construction.getConstructionXML`. Undo nativo puede usar un compañero autenticado del XML. | Un formato acotado puede compartir el ciclo de vida documental existente. Atribuir una relación a una instancia durante la carga exige validar la pertenencia y sus rutas de reconstrucción. |
| [`CopyPaste`](../../source/shared/common/src/main/java/org/geogebra/common/util/CopyPaste.java), [`GeoCeDGCopyPaste`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGCopyPaste.java), [`GeoCeDGDocumentInsertion`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java) | La copia nativa usa XML de subconjuntos y puede expandir grupos nativos. La inserción certificada y experta ensaya y verifica adiciones y tiene barrera de recuperación. Sus contratos no certifican remapeo de paneles. | Un fragmento que admita miembros nunca debe copiar una relación del receptor o transferirla por etiqueta. La v1 excluye transferir paneles de documento a documento. |

## 2. Contrato propuesto para CU8-I

### 2.1 Entidad, membresía y validez

Un panel es una entidad de presentación perteneciente al documento. Lleva un
UUID local al documento, una vista dueña, un modo de anclaje, dos coordenadas
de ancla, tamaño preferido, visibilidad y un conjunto explícito de ranuras.
Cada ranura tiene un identificador opaco local al panel, una referencia a un
control vivo del mismo documento y su desplazamiento en píxeles lógicos.
Una ranura no se reutiliza tras una baja durante la misma sesión. Un panel
vacío es válido y puede servir como plantilla de disposición.

En v1 la vista dueña es Gráficos 1 de Desktop. Cada miembro debe estar asignado
únicamente a esa vista, ser un deslizador `GeoNumeric` absoluto sin ancla
externa, un `GeoButton` o `GeoInputBox` absoluto sin punto dependiente, o una
casilla independiente absoluta sin punto inicial propio. Un objeto puede
ocupar a lo sumo una ranura. Los grupos nativos, paneles anidados, controles
multivista y conversiones automáticas de controles vinculados quedan fuera.
Los controles existentes mantienen sus productores, valores, scripts,
dependencias, condiciones de visibilidad y las posiciones nativas de respaldo.
El panel no es un `GeoElement`, no puede ser argumento de comandos y no añade
aristas al DAG. La vinculación al lienzo corresponde a una coordenada fija
del sistema de Gráficos 1.

Crear desde el espacio de trabajo, añadir o quitar miembros, convertir modo,
mover, redimensionar, ocultar y borrar son operaciones documentales explícitas.
El alta verifica la instancia seleccionada y la elegibilidad al confirmar.
Eliminar un control retira su ranura en la misma transacción. Eliminar un panel
libera los controles vivos sin eliminarlos. Una orden de separación escribe
la posición efectiva en el mecanismo nativo absoluto, incluida la conversión
según los insets del host. Si queda fuera de pantalla, la orden permite elegir
una posición visible o conservar la posición nativa de respaldo.
Un cambio incompatible de posición, capa, agrupación o vista que requiera
dejar el panel pide separación explícita primero. Si una edición heredada lo
deja temporalmente fuera de ámbito, el vínculo puede quedar suspendido solo
para la misma instancia vigente. Una sustitución no certificada retira la
ranura o se rechaza antes de mutar si la operación no puede hacerlo
atómicamente. El reemplazo nunca hereda la ranura por etiqueta.

### 2.2 Posición, tamaño y degeneraciones

| Modo | Origen efectivo | Efecto de navegación |
|---|---|---|
| Fijo | Esquina superior izquierda del área útil de Gráficos 1 más desplazamiento lógico persistido. | Persiste frente a pan y zoom. Se adapta al área útil al cambiar la ventana o los insets, sin reescribir el documento. |
| Vinculado | Proyección `toScreenCoordXd/Yd` de un par finito `(x,y)` en el sistema de Gráficos 1. Cada control usa ese origen más su offset lógico. | El origen sigue el pan y el zoom, incluidos ejes con escalas distintas. El tamaño de controles y márgenes conserva legibilidad de interfaz. |

Mover conserva offsets. El arrastre comienza con un punto de captura del
encabezado y obtiene la nueva ancla usando el transformador inverso de la
vista en modo vinculado. Cambiar de modo proyecta o invierte el origen
efectivo una sola vez para mantener la posición visible al confirmar. Solo
se redondea al producir geometría de pantalla. Coordenadas no finitas,
transformación no disponible o vista sustituida impiden confirmar el gesto.
Un ancla válida fuera del encuadre no se reposiciona automáticamente.
El gestor de paneles puede seleccionar el panel desde fuera del lienzo y
ofrecer centrar la vista o mover explícitamente el panel al área visible.

Redimensionar cambia el rectángulo preferido, no los tamaños intrínsecos ni
los offsets. El tamaño efectivo incluye los límites medidos de los controles
activos, encabezado y márgenes. Un cambio de texto, idioma, fuente o DPI puede
ampliar el mínimo efectivo sin tocar la posición persistida. La vista recorta
al viewport, y el panel no recorta un campo de entrada o su widget. La
previsualización detecta solapes de controles al mover o añadir ranuras y
permite elegir otra posición. Si un rótulo dinámico introduce un solape
posterior, se conserva el orden nativo de hit y el gestor permite corregirlo.

### 2.3 Dibujo, selección, capas, foco y herramientas

- El panel se dibuja en vivo entre las capas geométricas y sus controles. Un
  único cálculo de origen efectivo alimenta los límites de dibujo y de hit,
  el origen del deslizador y la posición de los editores. Se actualiza por
  cambio de vista, de documento, de fuente o de disposición, sin mutaciones de
  controles durante el render ni una copia de sus valores en el panel.
- La visibilidad de un miembro es la conjunción de su disponibilidad en el
  paso constructivo, visibilidad nativa, vista, capa y visibilidad del panel.
  Ocultar un panel no cambia capas, condiciones ni el valor de una casilla.
  La pertenencia no mueve capas. El contorno de un panel visible se mantiene
  accesible en el modo de disposición aunque sus miembros estén ocultos.
  El panel no permite eludir un bloqueo heredado de posición. Los miembros
  con bloqueo de posición no se admiten para mover en conjunto en v1.
- En modo habitual, el encabezado y el fondo no son hits geométricos. Un clic
  en un hijo conserva la acción nativa del control, incluido clic de botón,
  conmutación de casilla, arrastre de valor y edición de texto. El panel no
  se agrega a la selección del kernel ni a la navegación por Tab de los
  GeoElements. Un control con geometría cubierta sigue las prioridades de
  hit vigentes.
- La acción manifestada «Disposición de controles» abre un gestor y habilita
  manejadores de panel en Gráficos 1. En ese modo, los tiradores mueven
  posiciones sin disparar scripts, conmutar casillas ni modificar valores.
  No se entra durante una herramienta activa que capture el lienzo, un
  selector de diálogo o una edición de campo pendiente. La edición de un
  campo activo usa su propio flujo de aplicar o cancelar antes de entrar.
  Escape cancela la previsualización y restituye la selección del gestor.
  Cambiar de documento, modo o vista invalida el gesto y su captura de foco.
  Si un editor ya activo queda oculto se usa su política nativa de salida
  sin simular un clic ni introducir un segundo commit.
- Los controles conservan su participación nativa en otras acciones,
  Algebra, atajos y documentos. El gestor es una acción experimental de
  perfil, localizable en ES/EN, con acceso desde menú y sin apropiarse de
  Ctrl+Mayús+D, que GeoCeDG asigna a DXF. Gráficos 2, otras vistas y
  exportaciones mantienen su tratamiento nativo de controles y omiten el
  contorno. La disposición no es contenido de una hoja PDF, SVG o DXF.

### 2.4 Formato y reconstrucción

Propuesta v1. Incorporar un registro opcional y versionado
`geocedgControlLayout` en el encabezado `construction`, más
`geocedgControlSlot` en la apertura del `element` del miembro. Usar una
codificación ASCII simple de campos tipados con orden canónico y límites
explícitos, por ejemplo 32 paneles y 256 ranuras en total y 64 KiB de datos
de disposición. El registro de panel contiene el UUID, la vista, el modo,
el ancla, el tamaño, la visibilidad y los IDs esperados de sus ranuras.
La ranura declara panel, ID, familia y offsets. El escritor escapa atributos
con el constructor XML existente. Los límites concretos se verifican antes
de adoptar el contrato. El parser debe detectar duplicados y rechazar campos
inesperados, versiones desconocidas, valores no finitos, pertenencias cruzadas,
familias incoherentes, referencias ausentes y ranuras huérfanas.

Cada marca la escribe el objeto exacto que posee la ranura. El lector recoge
la marca junto con la instancia creada o reclamada por el lector nativo para
ese bloque `element` y verifica la relación completa al acabar de cargar.
El formato nativo conserva sus propios nombres para transportar definiciones,
pero ningún índice de identidad de panel busca una etiqueta, ni resuelve
mediante coordenadas, tipo, orden o posición. Si la ruta de carga no demuestra
qué instancia corresponde al bloque o detecta dos bloques para la misma
ranura, falla de forma atómica. En particular, hay que caracterizar los
elementos de salida de `Button`, `InputBox` y `Checkbox` y las rutas que
reutilizan un elemento previo en `ConsElementXMLHandler`.

Archivo completo, historial y reconstrucción autorizada del mismo documento
usan la misma descripción. Un contexto de escritura explícito delimita
esa autoridad. XML de macros, portapapeles, estilos, defaults y transportes
de inserción no emiten una marca huérfana. La lectura incremental en una
construcción existente tampoco puede adoptar marcas del origen. Validar en
un registro temporal y publicar al final de carga sin efectos parciales.
El cierre de una transacción o un fallo de recuperación nunca deja una
mezcla de un registro antiguo con objetos nuevos.

No se cambia la versión raíz, `app="classic"`, el ZIP, `<group>`, los IDs
espaciales ni la extensión. Un documento antiguo sin atributos tiene cero
paneles. Los lectores anteriores que ignoren ambos atributos conservan los
controles y sus posiciones nativas de respaldo, con pérdida explícita de
la disposición si guardan. Esa conducta es un objetivo de prueba con el
lector anterior, no una garantía de que GeoGebra externo admita cualquier
`.cedg`. El Classic de la misma distribución debe leer, preservar y
reescribir el registro, con sus controles disponibles por la ruta de
respaldo y sin acción de creación. Un archivo con versión nueva desconocida
se rechaza con diagnóstico, antes de sobrescribir la sesión o el archivo.

### 2.5 Historial, copias e inserción

| Ruta | Política inicial |
|---|---|
| Undo/Redo, guardado y reapertura | Una edición confirmada produce una entrada. Cancelar, mover el ratón durante el gesto, pan o cambiar de vista no produce ninguna. Un estado anterior vuelve a materializar miembros solo por el registro exacto de esa instantánea. Se verifica el compañero privado nativo y la recuperación F3, incluida una secuencia con Redo pendiente. |
| Protocolo y redefinición | Retroceder suspende el hit y la presentación de los miembros de pasos posteriores. Avanzar recupera exclusivamente el mismo objeto probado. Una redefinición ajena conserva el registro y lo restaura de un documento completo. La sustitución de un miembro se comprueba antes de publicar y retira o rechaza la pertenencia sin reusar su ID. |
| Copiar, pegar o duplicar controles | La copia nativa ordinaria de controles soportados genera controles sueltos con su propia identidad. Copiar una selección parcial o total desde el lienzo nunca arrastra un panel implícitamente. Cortar, borrar o reemplazar respeta las operaciones de retirada de ranuras. |
| Duplicar disposición | El gestor ofrece duplicar el rectángulo vacío con modo, tamaño y visibilidad, y con UUID nuevo y posición elegida por el usuario. Para copiar un panel poblado en el futuro se necesita una correspondencia explícita de la copia para todos los miembros y sus dependencias, nuevos IDs y una sola transacción. |
| Abrir, importar, insertar | Abrir un documento nativo reemplaza la construcción bajo las reglas de carga normal. La inserción F3/P4/B2 o experta de un documento con disposición CU8 se rechaza explícitamente antes de copiar en v1. Si solo el receptor contiene paneles, su registro y referencias forman parte del cotejo de recuperación y de no interferencia. La admisión de fuentes sin panel conserva las tablas actuales. |

Un receptor con paneles no amplía la clase pasiva de inserción experta. Si
el ensayo o el certificado vigente no puede cubrir el estado del receptor
con paneles, rechaza esa combinación antes de la mutación con diagnóstico.
La eventual transferencia de paneles requiere ampliar el contrato de
inserción, su envoltura pasiva, la traza de miembros y la comprobación bajo
barrera. Debe evitarse una degradación silenciosa a miembros sueltos al
insertar un archivo entero.

## 3. Complejidad y decisión solicitada

| Alternativa | Complejidad | Evidencia y consecuencia |
|---|---|---|
| Overlay temporal o `Group` como ayuda de selección | Baja para una ayuda de sesión | `Group` ya reúne geos y `GeoCeDGEuclidianView` ya pinta overlays. Las etiquetas del XML y la selección en bloque no acreditan identidad o disposición persistente. Esta opción no satisface M05. |
| Panel documental acotado y dos modos de ancla | Media, condicionada a las sondas de carga y foco | `Construction.getConstructionXML` sirve a archivo e historial. Los cuatro dibujos tienen puntos identificables de resolución de posición, y el deslizador consulta el origen al iniciar el arrastre. Se añaden un registro, un codec y un adaptador de disposición con cambios pequeños en esos puntos, más pruebas de las rutas de ciclo de vida. |
| Paneles entre vistas o con anclas `GeoPoint` vivas, duplicación poblada e inserción conservando paneles | Alta | Las vistas actuales y `AbsoluteScreenLocateable` no garantizan un ancla dinámica común para los cuatro tipos. `CopyPaste` mueve subconjuntos y resuelve identidad participante en rutas delimitadas. F3/P4/B2 excluye botones y campos y la revisión experta exige equivalencia, cobertura pasiva y recuperación. Habría que cambiar sus contratos de transporte, remapeo y certificación. |

**Recomendación al autor.** Adoptar el perímetro medio y autorizar CU8-I en
Gráficos 1 de Desktop, con ambos modos de ancla, los cuatro tipos de control
positivos y edición de la disposición. Aprobar de manera expresa el formato
v1, la política Classic y de degradación, y los límites de copia e inserción.
Si una sonda demuestra que la asociación por bloque o la recuperación necesita
un cambio de identidad global, detener ese incremento y reevaluar la
persistencia. Si solo falla el editor activo, corregir la ruta de foco o
presentar la exclusión del tipo como decisión de alcance. Ni la posición
dinámica de un punto ni el remapeo entre documentos son requisitos para los
dos modos de esta propuesta.

### Secuencia implementable tras autorización

1. Registrar fase y pruebas. Congelar sondas de lector anterior, apertura
   Classic, XML de archivo/Undo/reconstrucción y editor de campo en Swing.
2. Introducir el registro y codec acotados en el modelo de documento. Acoplar
   escritura, lectura, retiro y restauración a las rutas nativas. Probar
   invariantes y recuperación antes de habilitar la interfaz.
3. Implementar el resolvedor por vista y las operaciones explícitas del gestor.
   Integrar pintura, hit, origen del deslizador y widget activo de campo. Añadir
   los comandos de disposición al manifiesto bajo activación experimental.
4. Verificar supresión de marcadores al copiar, diagnóstico de fuente con
   paneles y protección del receptor en F3/P4/B2 y revisión experta. Ejecutar
   las regresiones del historial, CU4 y CU7 sin alterar sus contratos.
5. Congelar el candidato con su matriz de rutas y proponer el smoke del autor.

## 4. Matriz de validación

| Sonda | Resultado requerido |
|---|---|
| Identidad | Renombrar un miembro conserva su ranura. Borrar y crear uno con la misma etiqueta, posición y tipo no la recupera. Colisión de UUID al duplicar, o de ranura al cargar, falla antes de publicar. |
| Compatibilidad | Archivo anterior sin campos, reapertura nativa con cuatro tipos, guardado Classic de la misma distribución y apertura con lector anterior. En el último se contrastan objetos, DAG y posiciones nativas de respaldo. |
| XML hostil o incompleto | Inyectar versión desconocida, contenido fuera de límites, offsets no finitos, duplicados, familia incorrecta y marca sin cabecera. Estado y archivo de destino intactos ante error. |
| Protocolo e historial | Ocultar pasos, volver, borrar, deshacer y rehacer, redefinir otro objeto, intentar sustituir un miembro, cancelar un arrastre. Certificar ranuras, DAG, estado guardado y un Undo por edición. |
| Transformación | Pan, zoom con escalas distintas, cambio de tamaño, insets y DPI. El modo fijo conserva offsets respecto al área útil. El vinculado sigue la transformación de la vista y conserva dimensiones lógicas. Conversión de modo con diferencia de hasta un píxel por cuantización final. |
| Interacción real Swing | Botón con contador, casilla, deslizador horizontal y vertical y campo con vínculo. Comparar hit, clic y valor antes y después del movimiento. Probar editor simbólico y normal, Escape, foco, cambio de documento y conflicto con selector de diálogo. |
| M07 en captura activa | Reproducir `OBS-CU6-M07-GV1-SLIDER-PICK` con un deslizador suelto y comparar uno agrupado. El rechazo gráfico conocido puede mantenerse. El clic capturado conserva el texto y la apertura del diálogo, y queda aislado del arrastre, cambios de valor, scripts y edición de la disposición. Verificar inserción en el cursor y confirmación desde Álgebra, ambos pasos de ESC y reanudación de las acciones nativas al cerrar. La reparación del clic de M07 no es una puerta de CU8-I. |
| Visibilidad | Matriz panel, capa, condición, paso y vista. Ningún control invisible recibe hit o mantiene un editor fantasma. Ocultar y mostrar conserva valores y condiciones. Probar Gráficos 2 y el visor de exportación. |
| Transporte | Copia parcial y total, duplicación vacía, apertura o intento de inserción de fuente con paneles y receptor con paneles en ambas rutas. Verificar respuesta en diagnóstico y recuperación de Undo/Redo con fallos inducidos. |
| Carga | Sonda de 1.000 objetos ajenos y 256 ranuras usando contadores de visita. En un gesto se actualizan solo las ranuras del panel afectado y la búsqueda de pertenencia usa un índice por instancia. Ningún escaneo del DAG por evento de ratón ni escrituras de XML por fotograma. |

Smoke autoral propuesto para CU8-I. Abrir en Desktop un documento con un
panel fijo y otro vinculado, que en conjunto contengan los cuatro controles.
Ejecutar sus acciones, desplazar y ampliar ambos paneles, recorrer el
protocolo y deshacer/rehacer. Guardar, reabrir y contrastar en Classic la
preservación del documento. Probar un documento anterior y comprobar el
diagnóstico al intentar insertar el documento con paneles. Registrar
resolución, escala de interfaz, localización, documento y candidato exacto.

Para CU8-D corresponde `STATIC` y una comprobación de enlaces y delta
documental. Para una futura CU8-I corresponde registrar `PHASE` con el
contrato de fase integrado y seleccionar `INTEGRATION` por el impacto
concreto en archivo, historial, perfil y recuperación de inserción. El smoke
autoral queda como evidencia distinta de las sondas automatizadas.
[Autoridad de verificación](../../geocedg/specs/operations/verification-levels.md)

```text
VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = El candidato documental conserva el launcher, los prerrequisitos de la estación y los ejecutables de verificación.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = M05 está en diseño candidato. El comportamiento observable y las guías de usuario no cambian.
```

## 5. Adopción y puertas de CU8-I (2026-11-09)

El autor acepta la dirección de complejidad media de CU8-D y autoriza
CU8-I en una sesión nueva después de la publicación. La caracterización
original corresponde a `fef825860693cfb3f6412946b07816c8895e30ec`,
árbol `226dfa681949ec39e98c8ff2292c37501a960e66`, blob del diseño
`2e1d8832b1a015cc35e57235e0a2f4ba07358859`. El producto técnico de
CU5/CU6 contiene ese mismo blob. `selfApproved=false`.
[Decisión de cierre y transición](../validation/pre_g9b_cu5_cu6_closeout_record.md)

### Dirección adoptada

- Panel documental de presentación en GeoCeDG Desktop, Gráficos 1.
  Se aplica la tabla positiva de §2.1 para deslizadores numéricos, botones,
  casillas y campos de entrada nativos. Sus acciones, valores, dependencias,
  capas y posiciones de respaldo conservan su autoridad.
- Dos modalidades de anclaje. El origen fijo se expresa en píxeles lógicos
  respecto al área útil. El origen vinculado proyecta un par finito de
  coordenadas de Gráficos 1. Los desplazamientos y tamaños intrínsecos de
  los controles se expresan en píxeles lógicos en ambas modalidades.
- La pertenencia se prueba por registro del documento, ranura e instancia
  viva de la misma vista y construcción. Cada control ocupa a lo sumo una
  ranura. El panel aporta identidad de presentación y conserva el DAG
  existente. La selección, reconstrucción y restauración requieren
  correspondencia explícita. Etiquetas, posición, orden y proximidad
  carecen de autoridad de reasociación.
- La presentación combina vista, capa, condición, paso constructivo y
  visibilidad del panel. Dibujo, hit, arrastre y posición del editor
  comparten origen. Un gesto de disposición respeta el editor activo y la
  captura M07. Los modos normales de los cuatro controles y las
  interacciones aprobadas de CU5/CU6 son regresiones obligatorias.
- La persistencia de archivo, Undo/Redo y reconstrucción debe restaurar
  el estado de pertenencia de su propio documento de forma transaccional.
  Una edición confirmada tiene una entrada Undo. Navegación y cancelación
  conservan el historial. Un documento anterior sin disposición abre
  con cero paneles y mantiene sus garantías de escritura histórica.
- Copia ordinaria de controles y duplicación de una disposición vacía
  delimitan el diseño inicial. La transferencia entre documentos, copia
  de panel poblado, anclas a GeoElements vivos, paneles anidados y alcance
  multivista quedan fuera. El transporte de fragmentos conserva sus
  fronteras de identidad y las garantías vigentes de inserción y rollback.

### Contrato de compatibilidad y decisiones previas al código

La lectura de una disposición necesita contexto autorizado, prueba de
instancia por bloque propietario, validación de relaciones y publicación
atómica del registro. La escritura histórica de documentos sin disposición
y el tratamiento de capas explícitas de CU6, incluido `0`, conservan su
perímetro. La conservación validada en Classic incluye geometría, DAG y
propiedades nativas de los cuatro controles, más sus posiciones de respaldo.
Un guardado que admita la disposición debe preservar su registro. La UI
de creación de paneles pertenece a GeoCeDG Desktop Gráficos 1.
[Identidad nativa y enmienda CU6](../../geocedg/specs/ui/native-document-identity.md) ·
[Cotejo histórico D1](../../geocedg/specs/units/unit-system.md)

| Decisión pendiente | Sonda y regla para resolverla |
|---|---|
| CU8-FORMAT-V1 | La pareja XML `geocedgControlLayout`/`geocedgControlSlot`, su soporte exacto, campos, orden y límites 32/256/64 KiB son propuestas. Cotejar la correspondencia por bloque de los cuatro productores, las rutas FILE/MEMORY, la restauración y el comportamiento al encontrar datos malformados o versión desconocida. Ratificar la gramática y los límites antes del codec. |
| CU8-CLASSIC-LEGACY | Probar Classic de la misma distribución y un lector anterior con entrada nativa y archivo heredado. Determinar si el formato conserva la disposición y las posiciones de respaldo, si la ruta se debe rechazar o si puede ofrecer una degradación explícita. La eventual pérdida de disposición al guardar con un lector anterior requiere decisión autoral. |
| CU8-INSERT | La propuesta rechaza una fuente con paneles antes de copiar y admite un receptor con paneles solo bajo certificado y rollback completos. Verificar las barreras de F3/P4/B2 y de inserción experta. Toda nueva exclusión en un contrato vigente y toda ampliación de la clase certificada requieren conciliación expresa. |

La primera puerta de CU8-I congela estas decisiones y los negativos
correspondientes. Si la prueba por instancia exige cambiar la autoridad de
identidad durable o la recuperación transaccional existente, elevar ese
punto al autor antes del código afectado. La matriz anterior usa la
[limitación M07 aceptada](../validation/pre_g9b_cu5_cu6_closeout_record.md#obs-cu6-m07-gv1-slider-pick)
como línea base de no interferencia. CU9 y CU10 conservan su reserva.
