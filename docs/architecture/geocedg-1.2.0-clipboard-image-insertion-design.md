# GeoCeDG 1.2.0 — Imagen desde el portapapeles

## Estado y alcance

**USER AUTHORIZED FUNCTIONAL / R11 FOCAL FAILED — TECHNICAL CLOSEOUT BLOCKED.**
Diseño inicial de aplicación y Desktop, escrito antes del código. La autorización funcional corresponde a
Archivo → Insertar → Imagen desde el portapapeles, diálogo compacto para elegir
capa y posición en la construcción, una operación de Deshacer, cancelación sin
cambios y persistencia nativa. Root autorizó el lote inicial tras confirmar el
smoke focal e interactivo de R10. La revisión inicial y el primer foco se
completaron después con resultado fallido e incompleto. La
[corrección R11 autorizada](geocedg-1.2.0-clipboard-image-correction-proposal.md)
recoge sus causas y concreta la verificación de recursos y las precondiciones
de callbacks. Su foco terminó con 216 casos, 118 aprobados y 98 fallidos, sin errores u omisiones. Las once rondas autorizadas están consumidas; el [informe de bloqueo](../validation/geocedg_1_2_0_r11_blocker_report.md) registra las causas y el ACK estático retirado.

Este documento no declara validación ni cierre de la función. La revisión de la
implementación y sus pruebas será independiente. No cambia el presupuesto
de correcciones ni autoriza una campaña global. R10, relativa a restauración de
vista en MEMORY, es una corrección distinta.

La clasificación es diseño/documentación; el cambio productivo posterior será
ORDINARY, limitado a Desktop y al perfil declarativo. No añade autoridad
geométrica, comandos del kernel, tipos, formato de archivo o nueva política de
identidad. Se mantienen D1–D4 y las garantías de Locus V2.

## Autoridades y mecanismo existente

- [AGENTS.md](../../AGENTS.md), en particular separación de capas, perfil
  declarativo, diseño previo, compatibilidad y validación.
- [Inserción de documentos](../../geocedg/specs/ui/document-insertion.md),
  §§2.2, 5–7: IMAGE sigue rechazada en Insertar archivo; se reutilizan las
  garantías de barrera, historial, snapshot y recuperación, no su admisión.
- [Identidad del documento nativo](../../geocedg/specs/ui/native-document-identity.md):
  imagen y esquinas usan el ZIP/XML nativo, sin otro formato.
- [Mantenimiento documental](../../geocedg/specs/operations/documentation-maintenance.md):
  el estado propuesto/implementado/observable se distingue expresamente.
- Perfil canónico `apps/geocedg/application-profile.yml`; clases actuales
  `GuiManagerD`, `GeoImage`, `Construction`, `GeoCeDGLayerWorkspace`,
  `GeoCeDGDocumentInsertion`, `GeoCeDGUndoManager` y `GeoCeDGRecoverySnapshot`.

El menú clásico existente llama a
`((GuiManagerD) app.getGuiManager()).loadImage(null, true)`. Se conservará
**esa llamada exacta** como única autoridad de adquisición y creación. La
propuesta inicial externa de congelar un Transferable antes del diálogo queda
descartada: cambiaría cuándo se lee el portapapeles y añadiría otro mecanismo
innecesario. No se crea un lector, filtro de sabores, decodificador o caché.

El método nativo de dos argumentos es
`loadImage(Transferable, boolean)`; `null` no es ambiguo entre los overloads
actuales. El cast del receptor a `GuiManagerD` distingue esta API Desktop de la
interfaz común, que ofrece una firma de cuatro argumentos. No se utiliza el
overload con una esquina: puede reutilizar puntos seleccionados.

Hechos actuales relevantes:

| Fuente | Comportamiento que debe respetar la orquestación |
|---|---|
| `GuiManagerD.loadImage(Transferable, boolean)` | Lee el portapapeles, registra recursos y crea dos puntos nuevos etiquetados y una imagen por recurso. Cambia selección/objetos recién creados; no almacena Deshacer. Puede crear varias imágenes. |
| `getImageFromTransferable` | Mantiene sus sabores y errores nativos. Puede devolver null en un error de acceso, mientras el llamador usa `fileName.length`; una excepción también es un fallo de la operación protegida. |
| `Construction.addToConstructionList` | Cada entrada se añade después de `getStep()` y avanza el paso. El índice es de ConstructionElement, no una fila de Álgebra ni del protocolo filtrado. |
| `GeoCeDGLayerWorkspace.setWorkingLayer` | Puede mostrar una capa oculta y modificar el conjunto persistido. No se usará como estado temporal para esta operación. |
| `GeoImage` | Es auxiliar por defecto. Sus puntos de esquina se registran como dependencias locateable; eliminar la imagen no elimina sus puntos etiquetados. |
| `ImageManagerD` y escritor nativo | La tabla de recursos es global. El archivo guarda recursos referenciados por los objetos, no toda esa tabla. Una entrada no referenciada puede permanecer en caché tras un fallo; no se elimina globalmente. |

## Interacción y estado previo

Se añadirá una acción declarativa `document.insert-clipboard-image` al grupo
`file-insert`. ES: «Imagen desde el portapapeles…»; EN: «Image from clipboard…».
La etiqueta, ayuda, icono y proyección del menú salen del perfil. No se añade
un modo de toolbar ni un atajo. Classic conserva sus acciones heredadas.

El diálogo compacto ofrece:

1. Capa entera en `0..LayerDomain.maxLayer(app)`, con la capa de trabajo actual
   preseleccionada. Una capa oculta se identifica como tal. Elegirla no cambia
   la capa de trabajo ni el conjunto de capas ocultas; la imagen quedará oculta
   conforme a la regla de visibilidad ya existente.
2. «Después del paso actual» / «Al final», con la primera opción predeterminada.
   El texto identifica el paso actual real; -1 representa el comienzo vacío.
3. Aceptar y Cancelar; Escape y cerrar equivalen a Cancelar.

**No se lee el portapapeles antes de Aceptar.** No hay vista previa ni promesa
de insertar un contenido capturado antes del diálogo. La llamada nativa leerá
el contenido existente cuando se ejecute después de la confirmación.

Antes de mostrar el diálogo se conserva un request efímero: dueño de la
construcción, paso/anchor reales y estado de capa presentado. El anchor puede
ser una referencia al ConstructionElement vivo durante esta operación; no es
una identidad geométrica serializada. Al aceptar se valida que el request siga
correspondiendo al documento, construcción y paso presentados. Una modificación
durante el bucle modal no retargetea silenciosamente la inserción: se rechaza o
se vuelve a presentar el diálogo actualizado antes de leer el portapapeles.
La observación de cambios usa un view privado del Kernel, independiente de que
el dispatcher principal ya tenga listeners desactivados; se retira al terminar
el request. No registra scripts ni crea otra autoridad del documento.

Cancelar no cambia paso, selección, modo, capa, visibilidad, flag guardado o
historial; no crea baseline, recurso ni GeoElement. Tampoco detiene animación ni
edita flags para facilitar el diálogo. El archivo asociado y el portapapeles
del sistema/construcciones permanecen ajenos a esta elección.

## Operación confirmada

La entrada productiva requiere EDT, documento no aislado, ninguna operación de
inserción incompatible activa, Undo activo y `GeoCeDGUndoManager` disponible y
quiescente/no degradado. La espera de stores usa la barrera acotada existente.
Si el snapshot nativo o las condiciones necesarias de recuperación no pueden
certificarse, se rechaza antes de crear objetos. No se eluden las condiciones
del host MEMORY para permitir una inserción de imagen.

Orden obligatorio:

1. Validar el request y capturar el estado anterior, incluida su identidad,
   archivo/flag guardado, runtime de paso, selección/recién creados, modo,
   working/hidden layers, maxLayerUsed y flags de scripts/listeners. Obtener
   snapshot nativo verificado y conservar el historial original por sus objetos
   y cursor, con el baseline de la barrera.
2. Activar exclusivamente el scope de publicación necesario para que callbacks
   o scripts no editen la construcción durante una creación parcial. Sus valores
   anteriores vuelven en `finally`; no se apoya la atomicidad solo en modalidad.
   El scope léxico impide dispatch también si un lector nativo invoca
   `enableListeners()` durante la recuperación, y al cerrarse restaura el valor
   anterior real. Durante el scope mantiene además `listenersEnabled=false`:
   `ScriptManager.reset` usa ese valor para distinguir Undo de un documento
   nuevo y conservar los listeners JS globales. Se conserva `clearView/reset`
   nativo para la limpieza de referencias y estado por objeto; no se bloquea
   indiscriminadamente ese ciclo de vida. No modifica los filtros de ScriptType.
3. Para la opción predeterminada dejar el paso confirmado. Para «Al final»
   ejecutar `cons.setStep(cons.steps()-1)` dentro de la transacción.
4. Invocar una sola vez:

   ```java
   ((GuiManagerD) app.getGuiManager()).loadImage(null, true);
   ```

5. Observar la diferencia por instancias de la construcción completa, no por
   prefijos de etiqueta o por lo visible en el paso. Para `N >= 1` imágenes,
   verificar `N` GeoImage y `2N` puntos nuevos etiquetados, sin objetos extra,
   con cada par de esquinas anterior a su imagen en un bloque consecutivo.
   Esquinas y recursos deben pertenecer a esta creación y estar definidos.
   Cada recurso referenciado debe existir, tener dimensiones válidas y ser
   serializable por la representación nativa. El resultado booleano por sí
   solo no demuestra éxito. `false`, excepción o delta inválido son fallo.
6. Asignar la capa elegida a todas las imágenes y a sus nuevas esquinas,
   reteniendo sus handles reales. Preservar los defaults nativos: imagen
   auxiliar, puntos con su clasificación nativa y etiquetas de esquina ocultas.
   No se cambia la condición auxiliar de puntos ajenos ni se introduce aquí
   otra política general de esquinas.
7. Verificar el bloque después del anchor o al final y preservar la secuencia
   relativa, instancias, dependencias, capas y propiedades de todos los objetos
   anteriores, incluidos los que formaban el sufijo oculto. Las etiquetas se
   eligen por el kernel contra toda la construcción. El working/hidden layer
   sigue igual; maxLayerUsed puede aumentar por las nuevas capas usadas.
8. Almacenar e identificar el único estado propio de Deshacer terminado.
   Conservar el prefijo original por sus command objects y comprobar si su
   último checkpoint coincide con el baseline del preestado vivo. Si no coincide,
   añadir ese baseline antes del postestado, como hace condicionalmente
   la rama experta de `Transaction.succeed` mediante `topMatchesLiveBaseline`:
   no perder ediciones vivas
   que todavía no tenían checkpoint. La comparación debe usar la representación
   pública autenticada cuando el AppState lleve bindings privados MEMORY; dos
   claves efímeras distintas no implican una edición del documento. Reanexar
   ese prefijo, el baseline solo cuando sea necesario y el postestado propio;
   descartar Redo solo al publicar el éxito. Un único Deshacer de la imagen debe
   volver al preestado vivo completo. El baseline condicional conserva el estado
   previo de otras ediciones, no añade otra operación parcial de imagen. Marcar
   el documento como no guardado. Conservar la selección y los recién creados
   del flujo nativo; no añadir un segundo creador o una segunda carga para
   modificar su apariencia. Cancelar o fallar conserva la historia y rama Redo
   originales sin anexar ese baseline temporal.

La geometría de posición inicial reutiliza el viewport nativo; es presentación,
no una nueva autoridad métrica. Las identidades y dependencias no se infieren de
esa posición. Un Deshacer retira todo el bloque de `3N` objetos; Rehacer conserva
imagen, recursos, enlaces, capas y orden. No se agrega un notice de unidades o
procedencia D1: no se insertó un documento con estado de unidades propio.

## Seam transaccional y recuperación

La extensión más pequeña es una entrada image-specific en
`GeoCeDGDocumentInsertion`, que reutilice sus barreras privadas, `HistoryRecord`,
`Certificate`, `GeoCeDGUndoManager.detach/attach`, `PreparedUndoBaseline`,
`GeoCeDGRecoverySnapshot` e aislamiento. Se mantiene separada de la admisión de
Insertar archivo y de los campos/helper/oferta del flujo experto. No se expone
una API pública genérica de «ejecutar cualquier mutación atómicamente» ni se
refactoriza toda la infraestructura de inserción.

El transaction actual no puede llamarse sin adaptar sus supuestos: exige una
admisión/helper o una oferta experta, verifica otra familia y comunica
procedencia de unidades. La nueva rama debe tener request, estado anterior y
postcondiciones explícitos; no simular esos supuestos con campos null.

La recuperación conserva las categorías existentes, sin declarar éxito parcial:

- Antes de mutación: rechazo, sin cambios. Aun cuando una adquisición falsa/no
  disponible no cree geos, se verifica y se restaura la posición/flags de la
  transacción, que ya pudo haber elegido «Al final».
- Limpieza sustractiva: retirar las imágenes antes de sus esquinas y luego
  los puntos en orden inverso. Restaurar cursor/paso, working/hidden layers,
  maxLayerUsed, selección/recién creados, modo, flags, archivo/UUID/guardado e
  historial, y certificar contenido y estado. Comprobar también `app.images`
  y registros locateable; eliminar solo la imagen dejaría puntos huérfanos.
  Esa limpieza por sí sola no certifica los parámetros privados MEMORY. Root
  concretó la elección técnica del diseño existente: `NativeAtomic` conserva su
  autoridad y puede reconstruir el preestado al fallar la creación/verificación.
  Si reconstruye, el resultado debe declarar reset de instancias/selección y
  referencias; no se describe como K1-S ni como mismas instancias aunque se
  hubieran retirado correctamente todos los objetos nuevos. No se añade una
  prueba de igualdad privada en Desktop ni se amplía el kernel. Cancelar sigue
  fuera de toda mutación. Un no-op solo puede conservar instancias si su ruta
  existente permite certificarlo; igualdad de XML no sustituye MEMORY.
- Si la limpieza o el certificado no son suficientes: usar la recarga protegida existente
  del snapshot/baseline apropiado, con sus scopes nativos de carga y su
  restauración de historial. Si se reconstruyen instancias/referencias, se
  informa el reset de sesión previsto por el contrato. El journal limita a dos
  los intentos explícitos de recarga protegida de toda la operación, además de
  las restauraciones automáticas acotadas de `NativeAtomic`. No se confunde ese
  límite con dos entradas totales al lector: la vía con companion nativo puede
  ejecutar hasta cinco entradas, y la de archivo hasta tres, contando los
  backstops automáticos. No hay un bucle ilimitado de reintentos. Una
  reconstrucción se declara como reset incluso cuando no había objetos
  etiquetados y la comparación de instancias sería vacuamente verdadera.
- Si no puede certificarse la recuperación: snapshot verificado e historial
  pasan al aislamiento existente; nunca se continúa editando/guardando una
  inserción parcial no certificada. No liberar el snapshot en un `finally`
  incondicional.

El store de Undo es asíncrono: programarlo no demuestra que acabó. La
identificación por contenido/generación, la barrera de cierre y la instalación
del historial antes del refresh siguen siendo las reglas existentes. Un error
de refresh no sustituye el resultado de una historia ya instalada.

Se permite que un recurso nuevo no referenciado permanezca en la caché global
tras fallo; no forma parte del documento ni se escribe como imagen adicional.
No se elimina porque otras ventanas pueden compartirlo. El certificado exige
conservar referencias y bytes de los recursos anteriores y no persistir la
imagen fallida. Cancelar, por ocurrir antes de leer, no registra ningún recurso.

Se conserva el XML nativo de GeoImage, layer y corners y sus entradas de imagen.
Persistencia significa conservar esos objetos, datos y orden al guardar/reabrir
en una aplicación nueva; no depende de que sobreviva la caché de esta sesión.
El cursor del protocolo es estado runtime que se restaura exactamente en Cancel
y recuperación. No se promete una política nueva de cursor en Undo ordinario o
reapertura: el formato nativo ya condiciona `consStep` a sus vistas/navegación.

## Conjunto productivo inicial: cinco archivos

| Archivo | Modificación acotada |
|---|---|
| `apps/geocedg/application-profile.yml` | Acción, grupo File/Insert y textos/ayuda/icono ES/EN dentro de los contratos del catálogo. |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java` | Un target productivo; conserva disponibilidad/aislamiento comunes. |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java` | Fachada de acción y dueño lazy, como las acciones de documento existentes. |
| Nuevo `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGClipboardImageInsertion.java` | Diálogo compacto, request/freshness y llamada a la entrada transaccional. |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java` | Entrada y rama transaccional específicas para imágenes, reutilizando barrera/historia/snapshot/recuperación existentes. |

R11 añade el ajuste mínimo del guard en `GeoCeDGProfile.java` a las fuentes de
este lote: el perfil pasa de 136 a 137 acciones. Su perímetro máximo es de seis
fuentes productivas; la corrección no añade otra autoridad de catálogo.

No se prevén cambios productivos en GuiManagerD, Construction, GeoImage,
ImageManagerD, codecs, tablas de admisión, clipboard ordinario, Classic o kernel.
Si la evidencia demuestra insuficiente este seam, se identifica la causa antes
de ampliar el perímetro. Tests, documentación, inventario/perfil derivados y
registro de validación afectados acompañan la implementación; no son otra
fuente de significado geométrico.

## Pruebas focales e inyección

Clases propuestas: `GeoCeDG120ClipboardImageInsertionTest` para operación,
persistencia y orden; `GeoCeDG120ClipboardImageRecoveryTest` para fallos e
historial. La división final puede ser una sola clase si evita fixtures
duplicados. Reutilizar helpers Desktop existentes de EDT, creación de documento,
observación, baseline y espera de quiescencia; no copiar un harness general.

Un seam package-private de invocación permite contar la llamada y simular
fallos/deltas parciales en tests. Su implementación productiva es únicamente la
llamada nativa exacta. No adquiere sabores ni interpreta datos. La prueba real
del cargador usa el portapapeles de Windows con una imagen pequeña controlada:
conserva el Transferable anterior y lo repone en `finally`, aun ante fallo. La
indisponibilidad del portapapeles es evidencia de entorno, no permiso para
sustituir la ruta nativa por un lector de producto alternativo. Distinguir
resultados simulados de ejecución real.

| Caso | Oráculo requerido |
|---|---|
| Diálogo Cancel/Escape/cierre | Cero invocaciones y lecturas de clipboard; ninguna diferencia en geos/recursos referenciados, historial/cursor, flag guardado, paso, modo, selección, capas o flags. |
| Aceptar y ruta exacta | Una llamada de dos argumentos con transfer null/fromClipboard true. El clipboard se lee después de confirmar; cambiarlo durante el diálogo no usa una captura anterior. |
| Imagen real y varias imágenes nativas | Delta `3N`, nuevos corners propios, recursos existentes y sin referencia a un punto seleccionado previo. Toda la carga equivale a un Undo. |
| Capa 0, alta, distinta de working y oculta | Imagen/esquinas en capa elegida; working/hidden set invariables; efecto oculto coherente y defaults auxiliares nativos preservados. |
| Paso -1, intermedio y final; ambas opciones | Bloque correctamente colocado y sufijo anterior idéntico por instancias/orden/dependencias. Incluir etiquetas en conflicto en ese sufijo. |
| Request obsoleto o precondición fallida | Rechazo antes de llamar al loader, sin nueva historia o deriva de estado. Incluir aislamiento, Undo no activo y store pendiente/degradado. |
| `false`, clipboard vacío/texto/busy o excepción | Recuperación certificada, incluso después de haber cambiado el paso para append; cursores y flags anteriores restaurados. |
| Tres Undo/Redo y rama Redo previa | Retirada completa y reconstrucción de referencias/recursos/capas/orden; prefijo original conservado por sus command objects; Redo descartado solo tras éxito. |
| Edición viva sin checkpoint después de un checkpoint, con Redo previo | Éxito seguido de un Undo conserva exactamente la edición viva anterior y retira solo el bloque de imágenes; el baseline previo se añade condicionalmente si no coincide con el último checkpoint. Control sin edición no añade baseline redundante. Cancel/Escape y fallo preservan el preestado vivo y los command objects/cursor/rama Redo originales, sin publicar el baseline temporal. |
| Guardar y reabrir en aplicación nueva | ZIP con recurso decodificable y mismos pixels/dimensiones que el creado, imagen/corners/layers/order correctos. Comparar recursos previos; no depender de caché estática o clipboard. |
| Fallo tras primera/segunda esquina, imagen sin label, verificación, store propio y attach | Retorno certificado al documento, paso, imágenes registradas, locateables e historial originales. Un objeto construido antes de etiquetarse también necesita limpieza; no basta la diferencia del conjunto etiquetado para descubrir fallos parciales. |
| Fallo sustractivo y recarga | Ejercer recarga protegida y sus resets declarados; fallo serio conserva snapshot/historial y entra en aislamiento. No sobrescribir el archivo actual. |
| Documento con imagen previa, Control Layout y Point(path) nativo | Preservación de recursos y estado nativo, con historial real. Mantener rechazos tipados y scopes R9/R10, sin relajar el host para admitir imágenes. |
| CUA | Menú y diálogo ES/EN, paso intermedio/append, Cancel, una imagen visible, Undo/Redo, guardar/reabrir. Es evidencia de la ruta real, complementaria a fault injection. |

La posibilidad de un GeoImage creado pero aún sin etiqueta obliga a observar
tanto el delta de construcción como el registro `app.images` anterior. Es un
detalle verificable del creator nativo, no motivo para cambiarlo. La limpieza
debe incluir la instancia incompleta antes de certificar.

El primer foco emitió 75 casos: 21 aprobados, 53 fallidos y uno interrumpido en
un modal real del fixture; otros doce filtros no tuvieron cobertura completada.
El resultado y su diagnóstico se conservan, sin aceptación de la función. El
foco correctivo R11 se ejecutó tras revisión independiente y también falló. El
cierre técnico se ha detenido según el informe de bloqueo. El registro PHASE,
STATIC y las puertas globales/packaging/CUA exigibles al árbol final siguen
pendientes de resolver ese bloqueo. No se reduce una puerta por haber pasado
una prueba focal ni se presume otra ejecución correctiva autorizada.

Bootstrap: sin cambio; no se incorpora dependencia o runtime nuevo. GUIDE_IMPACT:
actualizar después de implementar y verificar la ruta, sin promover ahora una
propuesta a capacidad observable. La documentación F5, dossier y smoke generales
siguen siendo responsabilidad de root y no cambian por este documento.
