# POST-CU4 · Selector angular de Preferencias

`OBS-CU4-ANGLE-PREFERENCES-REFRESH` sigue abierta y no bloquea el cierre de CU4.
Esta caracterización y su corrección candidata se refieren a `d5332e400b0ab68afd14273495b9ae5f03e6c911`,
árbol `e8ee4b4b371d34f7f22cc081dee1f429963150dd`. La sonda se ejecutó con un
proceso Swing de ensayo. La intervención propuesta pertenece a Desktop y su
implementación queda sujeta a autorización. [Cierre CU4](../validation/pre_g9b_cu4_closeout_record.md#obs-cu4-angle-preferences-refresh)

## Estado efectivo y causa

`Kernel.getAngleUnit()` distingue `ANGLE_DEGREE`, `ANGLE_RADIANT` y
`ANGLE_DEGREES_MINUTES_SECONDS`. `Kernel.getKernelXML()` serializa el valor en
`<kernel><angleUnit val="…"/></kernel>`. Con el ajuste habilitado para la
aplicación, `MyXMLHandler.handleAngleUnit()` restaura `degree`, `radiant` o
`degreesMinutesSeconds` en ese kernel. `getAngleUnitUsesDegrees()` agrupa
grados y DMS y por ello resulta insuficiente para proyectar los tres estados de
Preferencias. La admisión F3 y la oferta B2 consultan el kernel del receptor. El
registro CU4 ya recoge un caso con grados efectivos y radianes visibles en el
panel, seguido de una inserción ordinaria coherente con los grados efectivos.
[Kernel](../../source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java)
· [Lector XML](../../source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java)
· [Inserción](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java)

`OptionsAdvancedD` crea tres radios mutuamente excluyentes. Su constructor llama
a `updateGUI()`, que consulta el modo efectivo y usa `setSelected`. Ante una
acción del usuario, `actionPerformed` escribe el modo, actualiza la
construcción y marca el documento como modificado. La selección programática
observada en el ensayo no disparó ese manejador. `PropertiesViewD` crea
`OptionsAdvancedD` al seleccionar `OptionType.GLOBAL` por primera vez y guarda
la instancia. Cuando se solicita de nuevo el mismo tipo de panel, el retorno
temprano de `setOptionPanelWithoutCheck()` actualiza el título y omite
`updateGUI()`. Mostrar la vista ya creada también puede reutilizar el panel.
`PropertiesDockPanel.setVisible()` llama a `setSelectedOptionPanelVisible()` y
`OptionsAdvancedD.setSelected()` tiene el cuerpo vacío.
[Panel avanzado](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/dialog/options/OptionsAdvancedD.java)
· [Vista de Preferencias](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/view/properties/PropertiesViewD.java)
· [Panel acoplable](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/layout/panels/PropertiesDockPanel.java)

En GeoCeDG, `.cedg` y `.ggb` recorren la carga transaccional nativa. Una carga
correcta actualiza el archivo actual y la línea base de Undo antes de
`nativeDocumentLoadCommitted()`. Este hook atiende el estado de documento de
GeoCeDG. `GuiManagerD.updateGUIafterLoadFile()` atiende la perspectiva, las
vistas y otros efectos del host. Ninguno sincroniza ese selector. Una instancia
de Preferencias que sobreviva a la carga puede conservar el radio anterior
aunque el kernel ya represente el nuevo documento. La creación tardía lee el
estado actualizado durante el constructor.
[Carga Desktop](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java)
· [Ciclo GeoCeDG](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java)
· [Gestor Swing](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/GuiManagerD.java)

## Comportamiento observado en la sonda

Se crearon archivos con `a = 1` y un modo angular explícito. El escritor de
GeoCeDG guardó los `.cedg` y `MyXMLioD` produjo los ZIP de ensayo con extensión
`.ggb`, para probar esa ruta de apertura con bytes controlados. Esta segunda
serie cubre la ruta de compatibilidad por extensión con archivos sintéticos.
El `.ggb` preparado para el smoke histórico aporta un segundo ejemplo de esa
ruta. El inventario local `artifacts/post-cu4-angle/documents.json`
registra el modo leído del XML y el SHA-256 de cada archivo generado.

| Caso ejecutado en el EDT | Resultado |
|---|---|
| Global existente en RAD, aperturas `.cedg` DEG → RAD → DMS → DEG | El kernel siguió los cuatro archivos. Volver a seleccionar Global conservó el radio anterior en los cuatro cambios de modo. |
| Mismo panel, aperturas `.ggb` DEG → RAD → DMS → DEG | La apertura DEG con DEG seleccionado mantuvo la coincidencia. Los otros tres cambios dejaron el radio anterior. |
| Primera selección de Global tras abrir un `.cedg` | Coincidió con el kernel para DEG, RAD y DMS. |
| Clic explícito DEG → RAD | Cambió el modo efectivo, dejó el documento modificado y conservó los comandos y el cursor de Undo en ese instante. |
| Undo, Undo, Redo, Redo con instantáneas de DEG, RAD y DMS | Restauró RAD → DEG → RAD → DMS. El radio conservó DMS → RAD → DEG → RAD respectivamente. |
| `PropertiesViewD.updateGUI()` explícito en los casos de apertura y replay | Hizo coincidir el radio con el modo. Cero `ActionEvent` de los radios y mismos modo, indicador guardado, comandos y cursor antes y después del refresco. |

El caso de Undo/Redo creó dos instantáneas explícitas después de las acciones de
radio. `MyXMLio.getUndoXML()` incluye los ajustes del kernel y
`UndoManagerD.readUndoState()` lee el ZIP de la instantánea. El clic de unidad
carece de un punto propio inmediato en `OptionsAdvancedD.actionPerformed`.
Un guardado ordinario de historial puede capturarlo posteriormente. Por
ejemplo, `PropertiesViewD.closeDialog()` llama a `app.storeUndoInfo()` si el
editor de texto no está activo. La regresión distingue ese comportamiento de
un replay que efectivamente restaura otra unidad. Abrir otro documento
establece su propia línea base de Undo. Undo de la construcción actúa dentro
de ese documento.
[Serialización de Undo](../../source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java)
· [Restauración Desktop](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/main/undo/UndoManagerD.java)
· [Adaptador GeoCeDG](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUndoManager.java)

Sonda local `artifacts/post-cu4-angle/AnglePreferencesProbe.java`. Desde la raíz
del repositorio, con Temurin 25.0.4 y el classpath local fijado en los archivos
de argumentos, se ejecutó la compilación de la sonda con `javac` y su ejecución
Swing con `java`.

```powershell
$geoJdk = 'C:/Users/usuario/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin'
& "$geoJdk/javac.exe" '@artifacts/post-cu4-angle/javac.args'
& "$geoJdk/java.exe" '@artifacts/post-cu4-angle/java.args'
```

`javac` terminó con código 0 y avisos de anotaciones JUnit ausentes en el
classpath histórico. `java` terminó con código 0, tres pruebas ejecutadas y tres
superadas. Los logs `artifacts/post-cu4-angle/javac.log` y
`artifacts/post-cu4-angle/java.log` conservan los diagnósticos. El segundo
identifica el origen de las clases cargadas. Los hashes de las cuatro clases
críticas comprobadas coincidieron antes y después. La sonda compiló su propio
arnés y consumió salidas Desktop y shared previamente disponibles. Esta
evidencia caracteriza el runtime local con Swing real y componentes embebidos.
La certificación de un candidato requiere reconstrucción y validación con el
verificador. Quedan para la regresión y el smoke focal la ventana visible, el
acoplamiento y desacoplamiento, la apertura fallida, Nuevo y los reemplazos XML
y Base64 fuera de las aperturas por archivo.

## Corrección candidata

1. Extraer de `OptionsAdvancedD.updateGUI()` la sincronización puntual de los
   tres radios. La operación lee una vez `getAngleUnit()` en el EDT y aplica
   `setSelected` a los radios existentes. La operación conserva la construcción,
   el estado guardado, las preferencias persistidas y el historial. La acción
   explícita del usuario conserva su recorrido y su política de historial actuales.
2. En `PropertiesViewD`, usar esa operación en la reactivación de Global ya
   seleccionado y al volver a mostrar el panel Global. Dar al gestor Swing un
   acceso estrecho que compruebe si `advancedPanel` existe. El gestor de
   GeoCeDG comprueba `hasPropertiesView()` antes de obtener la vista. Esto
   permite refrescar un panel existente sin montar uno nuevo ni activar
   validaciones o edición de otras pestañas.
3. Invocar ese acceso desde los recorridos de documento ya completados de
   `AppGeoCeDG`. `commitLoadedDocument()` es el punto común candidato de los
   reemplazos confirmados tras la inicialización del espacio de capas. Cubrir
   también Nuevo completado tras las preferencias y los valores por defecto si
   el smoke muestra un cambio de modo en esa ruta. El helper de presentación
   opera después de la transición. Si se programa desde otro hilo, vuelve a
   consultar el kernel vigente cuando llega al EDT. Una notificación anterior
   en cola carece de autoridad para aplicar el modo de otro documento.
4. Para Undo/Redo públicos, probar un observador por aplicación de
   `EventType.UNDO` y `EventType.REDO` con el mismo acceso de presentación.
   `Kernel` emite esos eventos al terminar el replay y el bloque de
   actualización. Menú, teclado y `GgbAPI.undo/redo` llegan al kernel. El
   observador se limita a esos eventos, tiene el ciclo de vida de la ventana y
   lee en el EDT tras el evento. Las operaciones internas que usan directamente
   el gestor de historial y forman parte de una transacción quedan fuera de
   ese observador. Antes de extender la cobertura a un nuevo camino de usuario,
   comprobar dónde termina su transacción y dónde se vuelven a habilitar los
   listeners. La ruta ordinaria de cambio de documento y la reactivación
   proporcionan sincronización adicional ante una vista reutilizada.
   [Eventos del kernel](../../source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java)
   · [API pública](../../source/shared/common/src/main/java/org/geogebra/common/plugin/GgbAPI.java)

El perímetro candidato abarca los dos adaptadores Swing anteriores, los puntos
de enlace de `AppGeoCeDG` y `GuiManagerGeoCeDG` y una prueba de regresión
Desktop. La sincronización conserva la precedencia de la carga y del historial
y la semántica de formateo de DEG, RAD y DMS. Su único efecto observable es
corregir el radio. Un listener en `Kernel.setAngleUnit()` ampliaría el contrato
a consumidores ajenos a Swing y recibiría estados intermedios de cargas y
rollback. Un `updateGUI()` general al publicar una carga modificaría widgets
ajenos al incidente. El hook de aplicación y la operación estrecha acotan esos
dos efectos.

## Regresión y ubicación propuestas

| Regresión Swing en Desktop | Oráculo |
|---|---|
| Global existente y visible, Global existente oculto, primera creación | Un único radio seleccionado y equivalente al entero de `getAngleUnit()` para DEG, RAD y DMS, sin crear panel en la ruta de refresco. |
| Abrir DEG, RAD, DMS y reabrir DEG en `.cedg` y `.ggb` | Coincidencia después de procesar el EDT en cada apertura, con preferencias iniciales distintas al modo leído. Incluir `.ggb` legado representativo. |
| Cambiar de pestaña y mostrar de nuevo Global, acoplado y desacoplado | El mismo modo al reactivar Global. Conservar panel y foco según la política vigente. |
| Abrir inválido, cancelar, Nuevo y dos cargas seguidas | El panel refleja el kernel efectivo final. Una carga rechazada recuperada conserva modo, archivo y selección previos. La cola EDT jamás aplica un modo viejo. |
| Undo/Redo con y sin cambio de modo y sin punto disponible | El radio sigue el modo que restauró el kernel. Se conserva la política de historial del clic de unidad y de la apertura de otro archivo. Comprobar la acción de interfaz y `GgbAPI`. |
| Refresco programático y clic real | Cero `ActionEvent` del selector durante el refresco. Igualdad de modo, XML, flag guardado, comandos y cursor antes y después del refresco. El clic escribe la unidad elegida y marca la modificación. |
| Dos ventanas, una sin Preferencias creada, y Classic de diagnóstico | Aislamiento por instancia. Preferencias sigue perezosa. Classic conserva el comportamiento aprobado de carga y selección. |
| F3/B2 después de abrir y después de cambiar voluntariamente la unidad | Misma decisión ordinaria o B2 que dicta el kernel. Reutilizar el caso histórico de texto en grados y adaptación en radianes. |

Ubicarlo como `I0`, una corrección de presentación independiente sobre el
`main` publicado después de CU4. Puede cerrarse antes de ampliar P4 y su
corrección carece de dependencias geométricas con P4 o con el diseño experto.
La decisión autoral delimita `I0` y su puerta de fase. Para el ciclo de
desarrollo corresponde `tools/agent/verify.ps1 -Profile DEV -Module desktop`
con filtros explícitos de la nueva regresión y los casos CU4/F3 colindantes.
La aceptación posterior requiere la puerta focal registrada para `I0` y un
smoke visible que abra, cambie y reabra documentos en los tres modos. Los
resultados de la sonda presente se conservan como caracterización, con sus
comandos y entradas separados de los resultados de esa futura puerta.
