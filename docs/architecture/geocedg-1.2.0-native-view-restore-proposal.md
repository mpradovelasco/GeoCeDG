# GeoCeDG 1.2.0 — Propuesta de restauración exacta de vistas en MEMORY

## Estado y puerta autoral

**AUTHOR AUTHORIZED — 2026-11-16 — R10 FOCAL + COMPUTER USE COMPLETE; GLOBAL VALIDATION PENDING.**

Clasificación: diseño de una corrección de estado del host. Capa: MEMORY y
contexto del único DAG ordinario de carga. El autor autorizó expresamente
«Autorizar R10 focalizada» el 2026-11-16. La ronda adicional R10 se consume
al incorporar el reproducer causal; quedan consumidas 10/10 rondas autorizadas.
La autorización se limita a la causa, perímetro y oráculos de este documento;
no implica aceptación del candidato, publicación o una campaña global.

No se solicita una autoridad geométrica nueva. La vista no es autoridad
métrica. Se mantienen D1–D4, la autoridad de rotación aprobada en R9 y la
separación MEMORY/FILE. El añadido de imagen desde el portapapeles tiene
autorización funcional y diseño independientes.

## Causa demostrada y límite de evidencia

Computer Use sobre Desktop R9 publicó una colocación S real en una copia
del cono. Ctrl+Z y el botón Undo no retiraron el punto. El registro
[stderr de la sesión](../../artifacts/agent/geocedg-1.2.0/r9-native-cu/stderr.log)
identifica dos fallos de XML:

- El destino Undo llega a finishRestore con registro 40/40, reservas 0/0 y
  entradas/consumo nativos comprobados antes del digest ordinario. Ese destino
  conserva solo el digest; no están aislados todos sus posibles deltas.
- El rollback del propio Undo identifica el primer delta en coordSystem,
  posición 404, con igual longitud de XML 77514/77514, registro 43/43 y
  reservas 0/0.

Con ancho 1218, los valores esperados son xZero=-775.0909090909093 y
scale=55.36363636363638; los obtenidos son -775.0909090909092 y
55.36363636363637. yZero e yscale coinciden en ese fragmento.

La secuencia binary64 nativa reproduce exactamente la diferencia:

    inv = 1 / scale
    xmin = -xZero * inv                 = 14.000000000000002
    xmax = (width - xZero) * inv        = 36
    scale2 = width / (xmax - xmin)      = 55.36363636363637
    xZero2 = -scale2 * xmin             = -775.0909090909092

EuclidianView.setCoordSystem de cinco argumentos escribe los cuatro números,
pero después llama a setXYMinMaxForSetCoordSystem y setRealWorldBounds.
Esta última actualiza bound objects y llama a updateBounds, que reconstruye
escala y origen a partir de los límites. Repetir el setter sin guarda puede
repetir el error. La comprobación aritmética anterior no ejecutó JVM ni midió
rendimiento; sigue siendo necesario un reproducer Desktop.

Se propone restaurar los datos capturados, sin tolerar la diferencia:
no redondear XML, omitir sus vistas ni cambiar igualdad exacta por tolerancia.
Para el target Undo se archivarán sus bytes antes de cargar: no se presume
que su único delta sea el de la vista.

## Clase mínima y guardas

Solo se resolverán vistas 2D ya existentes por su número nativo 1 o 2. No se
creará, mostrará ni materializará otra vista. El payload no conservará
referencias a vistas, geos, controladores o settings anteriores.

La clase de reparación exige:

1. Vista cartesiana lineal, sin ejes logarítmicos, rejilla polar o ratio de
   ejes bloqueado. Orígenes finitos y escalas positivas dentro de los límites
   ya admitidos por el setter nativo.
2. Cuatro límites propios de la vista: GeoNumeric nativos, anónimos,
   independientes y modificables, de la construcción propietaria.
   No escribir límites nombrados, dependientes o de otra clase.
3. Dimensiones físicas vivas iguales a las capturadas. Los offsets son
   visibleFromX y el inset vertical settings.height−settings.visibleUntilY,
   exactamente los que usa el writer nativo para el marco. Los hints de tamaño
   de settings no son una redimensión física: el lector puede normalizarlos al
   tamaño vivo serializado. No escribir hints/sentinel ni inventar una
   compensación de ventana para conseguir el digest.
4. Ninguna actualización de límites en curso al capturar; guardar y restaurar
   siempre el valor previo de isUpdatingBounds, también ante excepciones.
5. Sesión MEMORY propietaria activa, propósito ROLLBACK_RESTORE o
   NATIVE_OR_UNDO_RESTORE y una sola aplicación por lectura autenticada.

hasDynamicBounds no basta como criterio: también puede haber cuatro objetos
anónimos instalados por el companion. Se comprobarán las condiciones reales.
Una vista fuera de clase no adquiere permiso ni fallback histórico nuevo por
el mero rechazo de esta reparación. Si necesita un restablecimiento no
demostrado, se rechazará con causa tipada y permanecerán los checks exactos.

## Estado inmutable

NativeViewState/v1 acompañará al snapshot y a su UndoTemplate con datos
primitivos:

- número de vista, dimensiones, offsets y condiciones de clase;
- los cuatro valores vivos de origen/escalas y los cuatro de settings,
  distinguiendo ambos estados como ya se hace con visibilidad de ejes;
- límites cacheados de la vista y valores de los cuatro bound objects
  anónimos cuando sean necesarios para preservar su estado;
- seis valores cacheados del kernel para esa vista si el oráculo demuestra
  que requieren restitución independiente.

No se conservarán GeoElement ni referencias al host anterior. El número de
vista identifica un recurso nativo de presentación, no una identidad espacial.
Cada Undo/Redo obtiene una sesión nueva. Un payload ajeno, usado o sin sesión
no puede aplicarse.

## Seam y orden de restauración

El seam mínimo es el comienzo del try de Construction.updateConstruction,
después de poner updateConstructionRunning=true y antes de actualizar
independientes, aleatorios, near-to o algoritmos. El método ya toma el
nativeRollbackRestore activo y mide el único DAG ordinario.

restoreViewsBeforeDag deberá:

1. Validar sesión, propósito, etapa de uso, vistas, dimensiones físicas, offsets y
   bound owners actuales de todas las entradas antes de cambiar la primera.
2. Guardar isUpdatingBounds, ponerla a verdadero, restaurar settings sin
   notificación y usar setCoordSystem de cinco argumentos con repaint=false.
   La guarda evita el recálculo recíproco de escala por updateBounds.
   Restaurar siempre la guarda previa en finally.
3. Preservar los datos de bounds y cachés necesarios, sin dejar una deriva
   privada oculta detrás de cuatro atributos XML correctos.
4. Devolver el control al mismo DAG ordinario. Corner y sus dependientes deben
   leer ya el marco restituido: no computar con el marco alterado y corregir
   después mediante otra cascada.
5. Consumir la aplicación. finishRestore mantiene entradas, consumo, registro,
   reservas y XML exactos; la visibilidad de ejes conserva su tratamiento.

No basta que el marco sea correcto al comienzo del DAG. El lector vuelve a
activar las notificaciones después de kernel.updateConstruction y puede
notificar CE de zoom. El oráculo deberá comprobar persistencia del estado
hasta terminar el lector y antes de finishRestore. No se admite un parche
cosmético tardío que cambie XML después de que Corner o un consumidor hayan
calculado con otros valores. Si fuera necesario mantener una guarda de
sesión durante ese tramo, su restauración debe ser acotada y estar en finally.

No se añade un hook al formato FILE ni otro updateConstruction. El fallo de
una carga usa el companion del estado anterior correspondiente; no reutiliza
el payload del destino fallido.

### Kernel: almacenamiento silencioso solo si es necesario

El guard de updateBounds evita también setEuclidianViewBounds y sus cachés
del kernel. No se supondrá que esas cachés coinciden con la vista.

El setter ordinario del kernel notifica EVProperty.ZOOM; Corner ejecuta
compute y updateCascade en ese camino. No se utilizará para ocultar un
problema de orden. Hay consumidores reales de cachés del kernel además de
Corner, que lee directamente la vista.

Si el oráculo muestra que deben restituirse los seis valores del kernel,
se permite como tercer archivo Kernel.java: un helper estrecho, tipado y
autenticado por la sesión MEMORY almacena esos valores sin notificar CE.
El setter ordinario conserva comportamiento y notificaciones. No se añade
un booleano público genérico para silenciar propagación. Los consumidores
se actualizan mediante el único DAG existente.

## Conjunto editable máximo propuesto

| Fuente | Cambio acotado |
|---|---|
| NativeTransactionalState2D | Captura primitiva/versionada, guards, aplicación antes del DAG, traslado al template y checks. |
| Construction | Invocación en el seam existente y autorización estrecha cuando la necesite el helper tipado. |
| Kernel, solo si lo demuestra el oráculo | Restitución autenticada y silenciosa de las seis cachés, sin recálculo o notificación. |
| Tests Shared/Desktop nuevos o focales | Reproducer y oráculos positivos/negativos. |

No se cambia EuclidianView productivo: se reutilizan sus APIs. No se amplían
tablas FILE, codecs, comandos o tolerancias, ni se normaliza XML ordinario.
Si estas APIs no permiten conservar el estado y una sola propagación dentro
de la clase acotada, se documentará el obstáculo antes de ampliar el perímetro.

## Pruebas requeridas

1. **Reproducer causal.** Obtener por zoom/redimensión nativos un marco cuyo
   round-trip no sea fijo. Archivar bits y XML antes de Undo. Incluir aparte
   la aritmética exacta del tuple registrado. Debe fallar sin la corrección
   por el marco y no por mera apariencia.
2. **Atomicidad e historia.** XML ordinario exacto, datos de
   vista/settings/bounds/kernel, registro y reservas, cursor y objetos.
   Tres ciclos Undo/Redo con marco no uniforme: retirada real de la salida y
   recuperación de la misma identidad; un solo DAG.
3. **Orden y consumidores.** Corner(1..4) y dependientes ordinarios, getters
   de caché del kernel y un consumidor que realmente use esas cachés.
   Verificar datos correctos antes de los consumidores y al fin del lector;
   no cascada tardía para hacer coincidir XML.
4. **Dos vistas.** Dos vistas ya existentes con marcos distintos. No crear
   EV2 al capturar/restaurar una construcción que solo tiene EV1.
   Ausencia de vista, dimensiones/offsets incompatibles y payload ajeno se
   rechazan antes de una aplicación parcial.
5. **Negativos y excepciones.** Bounds nombrados/dependientes, log, ratio
   bloqueado/polar, escala no admisible, payload extranjero/reutilizado y
   fallo inyectado durante la aplicación. Guards, cursor, listeners y flags
   regresan a la situación exigida por su contrato.
6. **Independencia geométrica.** Zoom/DPI/vista no cambian garantías,
   componentes, eventos ni valores del locus y longitud del mundo.
   Solo cambian consumidores que declaran dependencia del marco, como Corner.
7. **Cono y Computer Use.** Otra copia SHA-verificada: cambios de H/v_1,
   rechazo de extremo, commit S por inspector, Ctrl+Z, botón Undo, Redo y
   Zoom In nativo. Archivar AppState destino y companion antes del intento.
   El punto debe desaparecer/reaparecer; una ventana sin error no basta.

Primero se ejecutarán los oráculos focales con el productor canónico, Memory16
y Desktop06 como regresiones. Después corresponden los checks afectados del
nuevo árbol. STATIC/PHASE/INTEGRATION/FINAL, benchmark, Computer Use y paquetes
nuevos siguen siendo obligaciones independientes.

## Disposición autoral y secuencia

El autor autorizó **una ronda adicional R10** para esta causa de restauración
exacta de vista y el conjunto máximo descrito, sin nueva autoridad geométrica.
Se incorpora primero GeoCeDG120NativeViewRestoreTest y se conserva el target
antes de Undo. Root ejecutó y archivó su resultado diagnóstico sobre el
producto aún sin corregir, antes de autorizar el parche productivo. La revisión
estática independiente del parche y de los tests está cerrada. La verificación
focal quedó completada según los recibos registrados al final del documento;
Computer Use real quedó completado en el alcance descrito al final. Este cierre
de R10 conserva pendientes las puertas globales del futuro árbol final.

Bootstrap: sin cambio. Inventarios: registrar tests e identidades tras su
ejecución autorizada. Guías y expediente: documentar solo la clase demostrada;
no prometer restauración universal de cualquier disposición de ventanas.

## Implementación R10 y evidencia causal previa

El productor canónico ejecutado por root archivó dos diagnósticos antes de
modificar producto. El primero no llegó a JUnit por una sobrecarga ambigua
del fixture; se corrigió únicamente esa lambda. El segundo ejecutó dos casos:
aritmética correcta y Undo real fallido. La selección nueva es
GeoCeDG120NativeViewRestoreTest y sus archivos están en
[r10-view-reproducer-02](../../artifacts/agent/geocedg-1.2.0/r10-view-reproducer-02/).

El zoom nativo obtuvo xZero=-852.6000000000001, yZero=160.4307692307692,
xscale=60.900000000000006, yscale=26.738461538461532. Su round-trip produjo
-852.6, 160.43076923076922, 60.9, 26.738461538461536. El fallo conserva
registro 0/0 y reservas 0/0; la diferencia de marco es comprobable, aunque
no haya IDs espaciales en este fixture reducido.

Se conservaron antes del intento los siguientes bytes en target-state:

| Evidencia | SHA-256 |
|---|---|
| target-before-undo.ggb | f38ff44eedba0e8050014c1e65c50003dbff61903ca392acef802101a28fd2c5 |
| target-before-undo.xml | 1f66a6b2394bbf662dc8d56d45a32ffd224cecc454c7d25640f76ac7c60fd4f5 |
| target-frame.txt | c2864989e2446b38586eb3e2d5ccb7ae64c04b1000d5d8d9b5670b64fbc15730 |

El parche usa los tres archivos productivos máximos. NativeViewState/v1
almacena únicamente primitivas, records y listas inmutables. La sesión valida
todas las vistas existentes antes de aplicar la primera; restaura el marco,
los límites cacheados y sus objetos anónimos antes del DAG. El setter nativo
de cinco argumentos sigue siendo el encargado del transform y del controller.
El guard de updatingBounds evita la vuelta por cocientes. Se conserva también
loadingMode en try/finally alrededor de esa restitución para que el setter
no la registre como una edición de coordenadas hecha por el usuario.

El setter bajo guard no actualiza las seis cachés del kernel. Por ello se usa
el tercer archivo: un payload inmutable, de constructor privado, solo puede
escribirlas mientras es exactamente el payload que está aplicando la sesión
MEMORY propietaria activa. La entrada nueva no notifica CE y no sustituye el
setter ordinario; los consumidores reciben el marco y las cachés antes del
único DAG. finishRestore comprueba nuevamente el estado al terminar el lector,
además de todas las comprobaciones previas de inputs, registro y XML.

Una vista que estaba fuera de la clase positiva no recibe reparación: solo
puede continuar si el lector ordinario ya dejó iguales sus ocho primitivas de
marco/settings, los cuatro límites cacheados de la vista y las seis cachés del
kernel. Estas comprobaciones se hacen antes de la primera aplicación y al fin
del lector, sin leer ni escribir los objetos límite excluidos. Un marco o una
caché distintos, o una clase incompatible que necesite reparación,
produce NativeViewRestoreException con NativeViewFailure; no añade una ruta
histórica ni una tolerancia. Esta conservación deja intacto el caso headless
0×0 que ya era exacto. El payload no materializa EV2 y cada Undo/Redo crea una
sesión nueva de aplicación única.

La presencia de todos los slots cacheados se valida antes de cambiar vistas.
El helper no amplía arrays ni llama a prolongGraphicsBoundArrays: solo escribe
slots que ya existían al capturar. La entrada rechaza también una reentrada
mientras restaura vistas; el permiso del payload de caché solo existe dentro
de ese tramo, y ambos guards se cierran en finally ante una excepción.

La revisión independiente estática de producto y los oráculos complementarios
de consumidores están registrados en los informes externos de R10. Los resultados
focales históricos y el estado de las comprobaciones pendientes se distinguen
a continuación. Esta sección no declara cierre ni convierte el resultado rojo
en validación positiva.

### Diagnóstico del guard de dimensiones y ajuste causal

La ejecución r10-focal-desktop-01 confirmó cuatro casos del cono y rechazó
siete casos de vistas antes de entrar en el setter. El diagnóstico separado
r10-dimensions-diagnostic-01 identifica los pares exactos: live 1218×632 en
ambos estados, settings 0×0→1218×632 y visible 0,0→0,632, versión 1.

El fixture conserva una situación nativa reproducible, sin editar XML:
Desktop.updateSize con keepCenter=false no escribe settings.size; startXML
serializa las dimensiones físicas vivas; handleEvSize llama a setSizeFromFile,
que hace que los getters usen el hint leído. getVisibleUntilY usa por defecto
ese mismo alto mediante sentinel. En ambos estados el inset vertical es cero.
Comparar los getters absolutos como si fueran dimensiones físicas rechazaba
la normalización ordinaria del lector antes de aplicar la reparación.

Root aprobó el ajuste técnico dentro de R10: comprobar dimensiones físicas,
visibleFromX e inset vertical exactos, calculando la resta como long, sin
restaurar ni normalizar manualmente hints o sentinel. Se conservan esos hints
antiguos y nuevos en el diagnóstico. No cambian los ocho valores del marco y
settings capturados, bounds, cachés, registro, entradas nativas o XML exactos.
El positivo causal permanece intacto; un negativo de cambio real de inset
comprueba que el guard físico no se haya debilitado. El ajuste no constituye
por sí mismo el cierre de runtime; requiere el conjunto focal y Computer Use.

### Dos vistas: reloj nativo y autoridad de cada destino

El siguiente [foco histórico](../../artifacts/agent/geocedg-1.2.0/r10-focal-desktop-02/evidence.json)
ejecutó 14 casos: 13 pasaron y falló la aserción de bounds de EV2 después de
Redo en twoExistingViewsRetainDistinctFramesAndBothKernelCachePrefixes.
Los cuatro casos del cono y el reproducer causal ya corregido pasaron en ese
foco, pero el conjunto completo no se declaró conforme.

El [diagnóstico ordenado](../../artifacts/agent/geocedg-1.2.0/r10-two-views-diagnostic-01/junit/TEST-org.geocedg.desktop.GeoCeDG120NativeViewConsumersTest.xml)
conserva SHA-256
5a67803c9d703a31955e0008a298687179f411d45d930b4511b06ab182be26ea.
Identifica dos estados nativos legítimos de bounds/cache, con los ocho valores
del marco/settings idénticos:

| Etapa | Bits de xmax en EV2 | Valor |
|---|---|---|
| Antes de baseline | 4620693217682128895, estado A | 7.999999999999999 |
| Antes de store, después de await y antes de Undo | 4617503167946074795, estado B | 5.166666666666667 |
| Después de Undo y antes de Redo | 4620693217682128895, estado A | 7.999999999999999 |
| Después de Redo | 4617503167946074795, estado B | 5.166666666666667 |

El montaje crea y dimensiona EV2 dentro de la misma acción EDT que establece
su marco y baseline. El componente Swing deja pendiente su evento de tamaño.
zoomAroundCenter conserva expresamente los bounds anteriores mientras cambia
escala/origen. Al despachar componentResized, EuclidianControllerD llama a
updateSize y el companion recalcula los bounds desde ese mismo marco. Para
ancho 960, intervalo inicial de longitud 17 y factor 1.2, la operación nativa
da exactamente el valor y bits de B. Esto ocurre antes del siguiente store.
Undo restaura A y Redo restaura B; esperar A para ambos era una suposición
obsoleta del oráculo, no un defecto productivo demostrado.

El oráculo corregido conserva este reloj nativo. Captura PRE inmediatamente
antes de baseline en su acción EDT y POST tras crear el marcador, inmediatamente
antes de store en su propia acción EDT. Exige Undo=PRE y Redo=POST para ambas
vistas, con igualdad exacta de marco, settings, bounds, objetos anónimos y seis
cachés. Mantiene Corner y su dependiente reales en EV1, retirada/recuperación
del marcador y un único DAG en ambas direcciones. EV2 ya existe pero no se
presenta como showing en el host embebido; no se inventa un Corner(2,1) válido.
No se modifica producto, no se drena artificialmente la cola ni se normalizan
los valores esperados para corregir esta aserción.

### Cobertura revisada y cierre focal

GeoCeDG120NativeViewConsumersTest contiene ocho casos revisados estáticamente:
consumidores reales de vista y caché del kernel mediante una imagen centrada,
dos vistas y sus prefijos, ownership ajeno/inactivo, límite nombrado excluido,
rollback ordinario, cambio de caché fuera de clase con XML idéntico, excepción
one-shot dentro del setter guardado y cambio real de inset. El caso de fallo
dentro del setter comprueba también flags, cursor y objetos de historia, bits
y parámetro del punto nativo y recuperación posterior de Undo/Redo.

La identidad revisada del test PRE/POST es SHA-256
0ad9cf534db4608c1901792b261326b3a4ac5a68c82a3ea8cbfe1d7cd5a3e8c1.
Se conservan por separado los dos casos de GeoCeDG120NativeViewRestoreTest,
incluido su target archivado antes del intento, y los cuatro casos focales del
cono. Las revisiones independientes son estáticas y no sustituyen la ejecución.

El productor GRADLE_TEST ejecutado por root completó el nuevo foco de 14 casos
y la regresión MEMORY de 16 casos. Los recibos conservan la orden, sus filtros,
entorno, logs y hashes de JUnit. Ambos indican COMPLETED, inner_exit_code=0 y
test_dry_run=false; la lectura posterior comprobó todos los hashes JUnit
declarados, cuatro archivos Desktop y un archivo MEMORY.

| Selección y orden base | Casos | Fallos / errores / omitidos | Recibo y log |
|---|---|---|---|
| r10.desktop — :desktop:desktop:test | 14: Restore 2, Consumers 8, ConeCertification 2, ConePlacementLifecycle 2 | 0 / 0 / 0 | [evidence.json](../../artifacts/agent/geocedg-1.2.0/r10-focal-desktop-03/evidence.json), [stdout](../../artifacts/agent/geocedg-1.2.0/r10-focal-desktop-03/process/stdout.log) |
| r10.memory — :shared:common-jre:test | 16 | 0 / 0 / 0 | [evidence.json](../../artifacts/agent/geocedg-1.2.0/r10-memory-01/evidence.json), [stdout](../../artifacts/agent/geocedg-1.2.0/r10-memory-01/process/stdout.log) |

Los SHA-256 de esos recibos son, respectivamente,
1b06cdb2d7b23e024fa976bb52c19d859faec0e05bdfea4bc57db0d39d8b5830 y
336a0705f849624ae19b6982217d6ee266ad20b3d590cf3d86560cf978696e2d.
La ejecución Desktop03 utiliza las fuentes productivas revisadas
NativeTransactionalState2D 9c7dcfa6…, Construction 1cc96d35… y Kernel 3d26a1f5…,
y el test PRE/POST 0ad9cf53… identificado arriba. La regresión MEMORY usa el
mismo producto. Los diagnósticos rojos anteriores se conservan como trazabilidad
de causas y correcciones, sin sustituirlos por el resultado posterior.

Estos resultados cierran el foco automatizado R10. Su comprobación interactiva
real figura a continuación. Se mantienen 10/10 rondas consumidas y las puertas
globales del futuro árbol final como obligaciones independientes.

### Computer Use real y cierre acotado de R10

Root observó la secuencia completa en el Desktop de desarrollo installDist,
sobre una copia SHA-verificada del cono. La sesión y sus evidencias están en
el expediente externo geocedg-120-r10-cu, bajo la raíz autorizada de visualizations;
los [logs de lanzamiento](../../artifacts/agent/geocedg-1.2.0/r10-native-cu/launch.json)
y el [modelo guardado](../../artifacts/agent/geocedg-1.2.0/r10-native-cu/cone-working.cedg)
se conservan por separado. observations.json contiene 12 observaciones y 15 PNG,
incluidos recortes de diálogos. session-status.json declara expresamente runtime
de desarrollo, globalAcceptance=false y finalCandidate=false.

La secuencia comprobó zoom nativo, rechazo métrico del P_1 original y acceso
al diagnóstico rico. El asistente no ofreció equivalencia E para ese punto;
se seleccionó explícitamente la colocación S independiente generator.main,
t=0. La creación produjo W_1=Point(e_2,text4,t_2) y protocolo 132/132.
El botón Undo retiró W_1 y dejó 129/129; el botón Redo lo recuperó con la misma
receta y 132/132. Ctrl+Z volvió a retirarlo y una segunda recuperación dejó
132/132. Se guardó, cerró por la barra de título y reabrió en un proceso Desktop
nuevo: W_1 y su receta seguían presentes, con 132/132 y s_2 visualmente sólido
y abierto. La imagen de la curva complementa las pruebas semánticas focales;
no constituye por sí sola un certificado geométrico.

La revisión posterior de evidencias contrastó las capturas de creación, Undo
y reapertura, verificó los hashes del original y del archivo guardado y leyó
el XML nativo sin modificarlo. El guardado contiene una sola orden Point para
W_1 con entradas e_2, text4 y t_2; no contiene nativeRollbackBinding.

| Evidencia | SHA-256 |
|---|---|
| Original inmutable InterCilConoOblique_2.cedg | 4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01 |
| Copia guardada y reabierta | b73766f1a74773382febcf524d026b27130af94c1cfe0ed4e75e08db3daaee3d |
| observations.json | c969b58fc076e7a552dba80178ab8df82feee030a6b5761ef58532084cd813e1 |
| session-status.json | 6b6e3dac1322ceb06179f34e5d730aefbd3c3f986f876eb774469a6f20ef3a9b |

El alcance acredita Undo y Redo por toolbar y Undo por teclado. Ctrl+Y fue
seguido de una comprobación con el botón Redo; no se atribuye una prueba
independiente a Redo por teclado. Alt+F4 no cerró aquella sesión y se usó el
cierre observado por la barra de título. No se generaliza este smoke a layouts
excluidos, cambios físicos de ventana durante restore ni al producto empaquetado.

R10 queda cerrada en su foco automatizado y Computer Use de desarrollo, con
10/10 rondas consumidas. No se declara validación global, aceptación del
candidato exacto, paquete nuevo ni autorización de publicación. La incorporación
del añadido de imagen desde portapapeles y las puertas globales posteriores
siguen su secuencia autorizada independiente.
