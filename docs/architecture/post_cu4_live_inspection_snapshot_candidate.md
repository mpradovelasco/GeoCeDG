# POST-CU4 · Captura viva I2 candidata

La captura pertenece a la capa de aplicación Desktop. El escritor nativo
define los datos persistibles y el kernel conserva la autoridad geométrica.
El adaptador I2 entrega bytes .cedg al trabajador pasivo que también
consume archivos guardados. La sesión activa conserva localmente la
asociación con el archivo y el testigo de estado. El hash del receptor
RECEIVER identifica los bytes entregados al trabajador.

## Contrato de captura

Se invoca en el EDT, en un turno sin diálogos ni carga nativa, con la
construcción activa y el historial quiescente. Bajo el monitor del
GeoCeDGUndoManager se registra el estado de sesión, se escribe en memoria
con MyXMLioJre.writeGeoGebraFile(..., false) y se comprueba la ausencia
de cambios observables antes de devolver la captura. Se reutiliza el
escritor que usa P7. Para envoltura, límites e identidad se usa el
analizador estático I2, porque la verificación P7 llama al lector
nativo en una aplicación desechable. La captura lee el receptor
aunque esté sin guardar. El trabajador aplica de nuevo su barrera
pasiva antes de evaluar los bytes.

Los miembros nativos preservan el UUID, los objetos y productores
serializables, sus dependencias, identidades durables, macros sin
imágenes, scripts nativos inertes durante la captura, defaults,
unidades, propiedades persistidas y presentación. El informe expone el
contexto nativo cargado (unidades, precisión y capas ocultas) como
observaciones tipadas. El testigo local conserva archivo asociado, flag
guardado, identificador Tube, modo, selección, capa de trabajo y máximo
de capa, dueño de la construcción e identidad de su historial.
Estos datos de sesión quedan en Desktop. El cierre libera bytes y
referencias. El transporte usa un archivo nuevo en el directorio
privado de la operación. Su propietario lo elimina tras el trabajador,
cancelación o error de transporte.

Se rechazan inicialización, carga, escritura nativa activa, animación
en curso, pausada o programada,
aislamiento F3 e historial pendiente o degradado. Se rechazan antes
de escribir las referencias a imágenes, iconos de macro e imágenes por
barra. El escritor puede materializar la caché de imágenes y cambiar
el nombre de imagen de un gráfico en writeConstructionImages. También
puede omitir recursos que no pudo escribir. Los listeners registrados
en ScriptManager quedan fuera de la captura porque el escritor omite
datos de listeners. El trabajador inventaría scripts serializados y
macros con su barrera pasiva. La captura no guarda, publica, restaura
ni modifica el historial, no carga auxiliares y no cambia la selección
ni la herramienta. El escritor conserva su protocolo interno de
kernel.isSaving() y puede actualizar métricas de serialización.

## Coherencia y comprobación posterior

La consistencia cubre las mutaciones documentales Desktop serializadas
en el EDT y Undo/Redo coordinado con el monitor. Se rechazan las
notificaciones del kernel que recibe un observador efímero durante la
escritura. El observador se retira incluso tras un fallo. Las escrituras directas al kernel
desde otros hilos quedan fuera del contrato de la sesión Desktop.

El testigo compara por recaptura el contenido exacto de los miembros ZIP,
ignorando metadatos de compresión y fecha. Incluye estado de sesión e
historial por referencia local. Un cambio en bytes persistidos, identidad,
dueño, archivo, guardado, modo, capas de sesión, selección o historial invalida la
comparación. Un receptor incapturable o una captura cerrada dan un
resultado no vigente. El hash de transporte identifica los bytes
declarados por el trabajador. La capa de trabajo de sesión no forma parte
del archivo nativo. El ensayo usa el contexto del trabajador y conserva
las diferencias observadas. Un cambio revertido puede devolver el
mismo estado. I4 requiere además invalidación monotónica durante la
vida de la oferta y revalidación dentro de la barrera de publicación,
incluidos estados relevantes ausentes en el archivo nativo. P7 y Undo
no proporcionan esa generación de edición. La captura I2 carece de
autoridad de recuperación.

## Validación

Probar estado guardado y sin guardar, reapertura de identidades,
redefinición con nuevas aristas, Undo/Redo, rechazo de recursos y de
captura durante cambios, fallo de escritor, limpieza y ejecución pasiva.
Comparar certificado F3, historial, herramienta, selección, asociación
y archivo antes y después. Un receptor válido para la captura puede
quedar restringido al inventario estático por la gramática del trabajador.
El límite comprimido se aplica al emitir el ZIP. La creación del XML
en memoria sigue el coste del escritor nativo.
