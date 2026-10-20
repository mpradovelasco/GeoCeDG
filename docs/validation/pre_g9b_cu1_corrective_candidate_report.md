# PRE-G9B-CU1 — Informe del candidato correctivo (decisión D-CU1-R6)

| Campo | Valor |
|---|---|
| Carácter | Candidato técnico correctivo de `CU1`: sincronización determinista de la prueba ISO A que rechazó `INTEGRATION`. Sustituye al candidato `8ee19667` como candidato de revisión de `CU1` |
| Estado | `CU1 CORRECTIVE TECHNICAL CANDIDATE — PENDING VERIFICATION AND AUTHOR REVIEW`; `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; `CU2`–`CU10` no autorizados |
| Autorización | decisión autoral D-CU1-R6 de 2026-10-20, «FIX TEST, RE-VERIFY»: corrección focalizada de la prueba ISO A que conserve íntegramente su objetivo, por sincronización determinista con el estado real del deshacer, sin esperas arbitrarias, reintentos, exclusiones ni relajación de aserciones; diagnóstico previo entre defecto de la prueba y fallo del producto; detención si hiciera falta cambiar código de producto o upstream; nuevo candidato verificado sobre su identidad exacta, sin reutilizar aceptaciones anteriores |
| Padre | `508412e2f38d744bd2b333acd5754a77b7861e52`, árbol `696c5eb17ddfe8b625434e504e3cdd13934c36b8`: descendiente documental del candidato `8ee196679e0a6ab101eee0a4e60682efe3f11be0` con su [registro de verificación](pre_g9b_cu1_verification_record.md) |
| Informes de `CU1` | [informe del candidato](pre_g9b_cu1_implementation_candidate_report.md), que sigue describiendo la implementación; este informe solo describe la corrección |

## 1. Diagnóstico: defecto de la prueba, no del producto

La prueba `PreG9BR6PlusE3IsoABorderDesktopTest.toolCreatesOneSheetWithOneUndoPoint` ejecuta el
escenario `tool` en una JVM propia: crea un marco ISO A con la herramienta, deshace y rehace.
Su objetivo es verificar que la creación produce un único paso de deshacer y que Deshacer y
Rehacer restauran la construcción.

El gestor de deshacer de GeoCeDG (`GeoCeDGUndoManager`, sobre `UndoManagerD`) guarda cada punto
de deshacer en un hilo propio: el hilo escribe el estado comprimido en un archivo temporal con el
monitor del gestor tomado y después lo añade al historial. `UndoManager.undo()` está
sincronizado sobre el mismo monitor y no hace nada si el historial consolidado no tiene un estado
anterior. El escenario deshacía inmediatamente después del clic, sin esperar al guardado que el
propio clic había lanzado.

Un sondeo de diagnóstico reprodujo el escenario fuera de Gradle, con las mismas llamadas y el
mismo classpath de pruebas. Registró, justo antes de `undo()`, los guardados pendientes de
`GeoCeDGUndoManager`, el cursor y el historial consolidado, cuyos estados clasificó
descomprimiendo cada archivo. Repitió la lectura cuando ya no quedaba ningún guardado pendiente.

| Ejecución (24 JVM en paralelo) | Resultado |
|---|---|
| Escenario original, candidato `8ee19667` | 34 fallos de 96 |
| Escenario original, base autorizada `e66ae4c0` (código de producto de `main`) | 42 fallos de 96 |
| Escenario con línea base y espera del guardado, candidato `8ee19667` | 0 fallos de 144 |

Todos los fallos, en el candidato y en la base, tienen la misma firma:

- antes de `undo()`: un guardado pendiente, cursor 1 e historial `[vacío]`; el guardado del
  clic aún no había llegado, así que `undo()` no tenía nada que deshacer y el marco seguía;
- cuando terminan los guardados: ninguno pendiente, contabilidad no degradada, cursor 2 e
  historial `[vacío, marco]`, en orden.

El historial del producto es siempre coherente y completo; no se observó ningún estado perdido,
duplicado ni desordenado. Con la sincronización, el guardado del clic llegó entre 82 ms y
1 684 ms después del clic (mediana 360 ms, percentil 95 de 781 ms) con 24 JVM en paralelo;
sin carga, llegó a los 82–87 ms y el escenario original llamaba a `undo()` 1–2 ms después.

Conclusión: es un defecto de sincronización de la prueba respecto del contrato asíncrono del
gestor de deshacer, el mismo que el escenario `link` de la misma clase ya respeta. La corrección
no requiere cambiar código de producto ni upstream.

Observación, fuera del alcance de `CU1` y sin cambio propuesto en este candidato
(`OBS-CU1-UNDO-ASYNC-STORE-WINDOW`): por el mismo diseño heredado, un Deshacer pedido antes de que
llegue el guardado de la última acción actúa sobre el historial ya consolidado. En uso
interactivo, la ventana es la latencia medida (unos 80 ms sin carga). Si el autor la considera
relevante, su tratamiento sería una propuesta correctiva separada sobre código upstream.

## 2. Corrección

Archivo modificado: `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusE3IsoABorderDesktopTest.java`.
Solo cambia el escenario `tool`:

1. Después de crear `P` y antes de activar la herramienta, el escenario fija una línea base de
   deshacer síncrona (`prepareUndoBaseline` y `commitUndoBaseline`). La línea base descarta los
   guardados asíncronos anteriores.
2. Registra un `addUndoInfoStoredListener` y, después del clic, espera con
   `CountDownLatch.await` al aviso del guardado del clic. La espera termina en cuanto llega el
   aviso. La cota de 60 s, constante `UNDO_STORE_TIMEOUT_SECONDS`, solo convierte un guardado que
   no llega en un fallo explícito, antes del límite de 300 s del escenario.
3. Aserciones añadidas: el guardado llega («the creation stored its undo point»); el historial
   tiene exactamente un paso sobre la línea base (`getHistorySize() == 1`); después de deshacer
   sigue existiendo `P`, la construcción anterior a la creación.

Se conservan todas las aserciones anteriores con sus valores: un único punto de deshacer en el
retorno del clic, el modo de un solo uso, el diálogo y la geometría del marco, el XML, «undo
removes the whole sheet» y el marco restaurado por Rehacer. No hay esperas fijas, reintentos ni
exclusiones. La variable local `prompt` pasa a ser `final` porque la línea base alarga su
distancia de uso (regla de Checkstyle).

`docs/upstream/modified-files.yml` añade una nota al propósito de la entrada de la prueba, que ya
estaba registrada. El inventario de JUnit, el registro y las fijaciones de cobertura no cambian:
las identidades de las pruebas son las mismas.

## 3. Evidencia de desarrollo

| Comprobación | Resultado |
|---|---|
| Clase `PreG9BR6PlusE3IsoABorderDesktopTest` (13 escenarios en JVM propias) | 13 de 13 |
| Escenario `tool` corregido, 24 JVM en paralelo | 288 de 288 (antes: 121 fallos de 288 en el candidato y 58 de 144 en la base) |
| Escenario `tool` corregido, secuencial | 40 de 40 |
| Checkstyle de las pruebas Desktop | sin avisos en la clase; quedan los 4 avisos preexistentes de otras clases |
| `git diff --check` | limpio |

Los sondeos son evidencia de diagnóstico ejecutada desde el directorio temporal de la sesión,
no pruebas registradas. Repetirlos requiere el classpath de pruebas Desktop y lanzar el `main`
del escenario, o del sondeo, en varias JVM a la vez.

## 4. El fallo precede a `CU1`

El [registro de verificación](pre_g9b_cu1_verification_record.md) del candidato `8ee19667`
conserva las dos ejecuciones rechazadas de `INTEGRATION`:
`verification-eb1ff68a9d5d410ca74b887d2cfebe00` y
`verification-c126f174db774b5a9c7ddca1e4382689`, ambas `REJECTED_SEMANTIC`, 18/19. Sus
registros siguen en `artifacts/agent/pre-g9b-cu1-integration-8ee19667/` y
`artifacts/agent/pre-g9b-cu1-run2-integration-8ee19667/`. Ese registro también documenta el
escenario original sobre la base: 58 fallos de 144, frente a 121 de 288 en el candidato. El
sondeo de §1 añade que el mecanismo es idéntico en la base.

## 5. Verificación de este candidato

La clase sigue siendo `INTEGRATED_PHASE`. Sobre la identidad exacta de este commit se ejecutarán
`STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION`,
sin reutilizar ningún resultado de `8ee19667` ni de ejecuciones de desarrollo. Un commit no
puede citar su propia identidad: los resultados se registran en un commit documental
descendiente.

## 6. Alcance y autorización

El candidato no cambia código de producto, especificaciones, ADR, inventario ni registro, y no
amplía el alcance funcional de `CU1`. `CU2`–`CU10`, la publicación en `main`, las etiquetas, los
releases, los binarios y los cambios del libro siguen sin autorizar.

Otras observaciones de la corrección:

- `OBS-CU1-ISOA-UNDO-STORE-RACE`: corregida en este candidato, pendiente de su verificación.
- `OBS-CU1-UNDO-TEMP-FILES`: los escenarios en JVM propias terminan con `Runtime.halt`, que no
  ejecuta `deleteOnExit`. Sus archivos `GeoGebraUndoInfo*.ggb` quedan en el directorio temporal
  del usuario: 14 796 archivos, 13,8 MB, desde 2026-10-12. No se borran ni se corrigen aquí.
