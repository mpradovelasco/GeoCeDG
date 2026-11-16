# GeoCeDG 1.2.0 — Propuesta correctora de imagen del portapapeles

## Estado y autorización

**FOCAL FAILED — TECHNICAL CLOSEOUT BLOCKED (11/11).** El autor autorizó
expresamente una ronda correctora adicional después de revisar esta propuesta,
cuyo SHA-256 previo a la autorización era
`b61e035986002867f0d5dd34e634f66e56e0346e10425b226bdd60dfa19d415a`.
El límite quedó ampliado a once rondas y R11 se dedicó exclusivamente a las
causas y al perímetro descritos aquí. R10 completó su foco y Computer Use.
La autorización funcional independiente de imágenes permitió su implementación
inicial, diseño, revisión y primera ejecución; el fallo de esa ejecución se
conserva como evidencia. El foco correctivo terminó naturalmente con código
interno 1: 216 casos, 118 aprobados y 98 fallidos, sin errores u omisiones.
La revisión estática volvió a omitir la colocación obligatoria de la acción en
el cluster y su ACK se ha retirado íntegramente. La corrección no está validada;
las once rondas están consumidas. El
[informe de bloqueo](../validation/geocedg_1_2_0_r11_blocker_report.md) separa las
causas y la evidencia. No se ha aplicado otra corrección ni se han iniciado
las puertas globales o paquetes del árbol actual. La publicación y la
aceptación del candidato siguen pendientes.

Clasificación: depuración y diseño correctivo. Capa: perfil y Desktop. No se
solicita cambiar autoridad geométrica, kernel, DAG, formatos, tolerancias o
garantías de Locus V2. Se conservan D1–D4 y las tres fuentes productivas R10.

## Evidencia retenida

El productor canónico ejecutó `clipboard.initial` con dieciséis filtros sobre
las fuentes recogidas en `clipboard-image-initial-01/source-before-runtime.json`.
La selección comprendía dos clases nuevas, diez de catálogo afectadas y cuatro
de regresión R10. Es desarrollo sobre HEAD retirado
`6f1d357ca2224dedd0e84fda0faed2d600154bab`, árbol
`b6708de39577009752d45e0b73e9d44d94d695f9`, con cambios sin commit.

Resultado interno **1**, campaña incompleta: los cuatro XML emitidos contienen
75 casos, 21 aprobados, 53 fallidos y uno interrumpido que Gradle marcó skipped.
Ese skipped no es una omisión autorizada. No se infiere cobertura de los otros
doce filtros. El ejecutor entró en un diálogo modal de aislamiento real desde
un fixture que esperaba éxito; quedó esperando en el EDT. Se guardaron el
volcado de hilos, identidad del proceso y log del daemon antes de interrumpir
únicamente ese proceso, verificado por PID, padre y comando. La interrupción
fue causal y deliberada; no se aplicó un timeout ni un veredicto por duración.

Evidencias en
[clipboard-image-initial-01](../../artifacts/agent/geocedg-1.2.0/clipboard-image-initial-01/).
Las 64 entradas de fuente conservaron sus hashes durante la ejecución. No se
considera verde la revisión inicial frente a los hallazgos posteriores.

## Causas y correcciones propuestas

### C1. El compilador aún exige 136 acciones

El perfil incorpora correctamente una acción adicional y los oráculos de
catálogo esperan 137, pero `GeoCeDGProfile.compileActions` conserva el guard
`actions.size() != 136`. Rechaza el perfil v2 y activa el fallback v1 válido.
Esto explica las entradas ausentes, excepciones JSON y menús heredados de la
ejecución; no es un problema de entorno o BOM.

Actualizar el guard a 137 y su explicación. Conservar las comprobaciones de
unicidad, referencias, colocación, localización y fallback. Este ajuste mínimo
requiere un sexto archivo productivo respecto a los cinco del diseño inicial;
no añade otra lista de acciones ni otra autoridad declarativa.

### C2/C3. La verificación experta I2 no sirve para esta imagen propia

La transacción usa `GeoCeDGExpertIntegrity.receiverResources`. Ese método pasa
por el envelope pasivo de inspección I2, que excluye imágenes y sus recursos.
Por ello rechaza tanto una imagen previa como el resultado recién creado.
Además, `GeoCeDGExpertInspection.checkDeclarations` resuelve mediante
`Kernel.lookupLabel`, que respeta el paso actual. Los objetos del sufijo oculto
aparecen ausentes y provocan `Native declaration mismatch at A/B` al insertar
antes del primer paso o en un paso intermedio. Ambas causas son independientes
del guard de catálogo.

Sustituir únicamente esas llamadas de la rama de imágenes por un helper local
en `GeoCeDGDocumentInsertion`: escribir el ZIP del documento vivo con el writer
nativo y `false` para thumbnail, verificarlo con
`GeoCeDGRecoverySnapshot.verify(bytes, app, uuid)`, que ya llama al preflight
nativo existente, sin duplicar esa validación, y obtener hashes de sus miembros.
Comparar los recursos previos por nombre y bytes; excluir el XML de construcción.
El writer con `false` ya omite el thumbnail derivado. Si un miembro emitido se
llama `geogebra_thumbnail.png`, puede ser un recurso de imagen real y se conserva
en la comparación: no se excluye solo por ese nombre. Esta precisión dentro de
C2 se comprobó en el writer antes del foco correctivo. Los nombres son
direcciones de miembros del ZIP, no identidad geométrica.

El writer recorre la construcción completa sin cambiar `Construction.getStep()`
ni ejecutar una cascada adicional en el documento vivo. El preflight conserva
la evaluación de su kernel descartable, prevista en su contrato. El digest usa
un buffer fijo y conteos long, consume completamente cada entrada y comprueba
CRC/tamaño cuando estén declarados, rechazando nombres duplicados. No importa
los límites de admisión I2 ni inventa otro envelope: recibe únicamente el ZIP
finito propio ya capturado y validado por la infraestructura nativa.
Se mantienen las comprobaciones de todo el
prefijo y sufijo por sus instancias, orden, declaraciones y dependencias. No
ampliar la admisión I2, Insert File o Expert; no introducir un lector o decoder
de imágenes, ni borrar recursos de la caché global. La adquisición continúa
siendo exactamente `GuiManagerD.loadImage(null, true)`, después del diálogo.

### C4. Callbacks JavaScript por objeto no restituibles por la ruta actual

El scope ya preserva los listeners globales y el reset nativo de referencias.
Sin embargo, `ScriptManager.reset` vacía los mapas de callbacks por objeto antes
de consultar el flag de listeners. La captura Atomic ordinaria no los escribe.
Cambiarla a otra operación no resuelve el problema: el writer histórico de
`GeoElement.getListenerTagsXML` usa el mapa de update también para escribir
objectClick. El riesgo afecta a recuperación y a Undo/Redo; no se ocultará como
un reset genérico de memoria de referencias.

Propuesta conservadora: rechazar este contexto antes de modificar el documento
o leer el portapapeles. Usar el factory nativo `AppD.newScriptManager` mediante
una subclase privada de `ScriptManagerD` en `AppGeoCeDG`, sin otros cambios de
comportamiento. El predicado lee los dos mapas protegidos sin crear mapas o
manager, y rechaza una clase ajena conservadoramente. Revalidarlo al confirmar
y antes de la barrera para detectar registro durante el diálogo. Informar del
motivo de indisponibilidad.

No rechazar indiscriminadamente los listeners globales ni los scripts ordinarios
de objetos. No reparar el writer upstream en esta ronda ni prometer soporte de
esos callbacks. Documentar la limitación y probar que el rechazo conserva ambos
callbacks operativos, instancias, documento, recursos e historial, con cero
invocaciones del loader.

### C5. El fixture no debe abrir una recuperación modal real inesperada

Instalar en los hosts de prueba el presenter guionizado existente y un cierre
limitado a ese host. Un resultado serio inesperado debe retornar y hacer fallar
la aserción de éxito, conservando la causa y sus excepciones suppressed. No
cambiar el aislamiento productivo, transformar el fallo en aprobación, omitir
casos o depender de intervención manual para que JUnit termine.

## Perímetro máximo propuesto

| Fuente productiva | Cambio dentro de R11 |
|---|---|
| `GeoCeDGProfile.java` | Guard y explicación 136 → 137. |
| `GeoCeDGDocumentInsertion.java` | Recursos del ZIP nativo propio en la rama image; predicado de admisibilidad antes de mutación. |
| `AppGeoCeDG.java` | Factory y predicado no mutante para callbacks por objeto. |
| `GeoCeDGClipboardImageInsertion.java` | Freshness y rechazo contextual antes del loader. |
| `apps/geocedg/application-profile.yml` | Mensaje y ayuda de la limitación observada. |
| `GeoCeDGActionRegistry.java` | Solo si es necesario para exponer el mismo motivo de indisponibilidad. |

Pruebas: las dos clases nuevas y los oráculos de catálogo afectados; no
renombrar identidades de tests para alterar inventarios. Documentación, cobertura
ES/EN y expediente acompañarán los resultados reales. Los demás incrementos,
R10, I2, los codecs y el kernel quedan fuera de esta corrección.

## Verificación de la ronda propuesta

1. Revisión independiente de cada causa, parche y oráculo antes de ejecutar.
2. Foco canónico completo y no modal: perfil v2 con 137 acciones; imagen real y
   previa, recursos, capas ocultas, posiciones -1/intermedia/final, prefijo y
   sufijo, Undo/Redo, guardado/reapertura y fallos de publicación/recuperación.
3. Negativos de callbacks update/click distintos, registro durante diálogo y
   flag inicialmente desactivado; conservar el positivo de listener global y
   la limpieza nativa de referencias.
4. Revisión independiente del resultado y Computer Use de la acción real.
5. Solo después, terminar guías/cobertura, fijar nuevo checkpoint e inventarios
   y ejecutar todas las puertas globales y de distribución del mandato sobre
   el árbol definitivo. Ninguna focal sustituye esas puertas.

La autorización R11 cubre **un ciclo correctivo focalizado** con estas causas
y límites. Una causa que exija ampliar este perímetro o una nueva decisión
normativa se documentará antes de continuar la parte afectada. No se autoriza
por inferencia otra ronda ni la publicación.
