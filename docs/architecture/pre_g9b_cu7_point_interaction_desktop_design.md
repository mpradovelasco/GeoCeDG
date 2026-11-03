# PRE-G9B-CU7 — Interacción Desktop de puntos: diseño focal candidato

Fecha: 2026-11-03, Europe/Madrid. Implementación autorizada por el encargo
`PRE-G9B-CU7-GENERAL-POINT-INTERACTION`; candidato técnico, sin aprobación
autoral de resultados ni cierre CU7.

## Clasificación, autoridad y capas

Implementación focal sobre el candidato f97c691e8557cd4c82e37517d1487f9c5d3012e5.
Se han leído AGENTS.md, la interacción R6, M08-S-R2, las rutas compartidas de
creación/publicación y los controladores Desktop. No existe
`.github/copilot-instructions.md` en este candidato ni AGENTS.md anidado en
las áreas inspeccionadas. Kernel conserva descubrimiento, dirección,
continuación, prueba, identidad y transacción. Desktop conserva selección,
captura de petición world, controles, mensajes y gesto Undo.

## Contrato de interfaz y enmienda candidata

Se añade **Permitir arrastre**, activado por defecto, al panel existente de
creación desde Q/clic. Desactivarlo aplica el estado nativo persistente
`fixed` al punto construido. No congela su posición world: el punto continúa
dependiendo de su objeto y de su dirección/parámetro; los cambios de fuente
siguen propagándose. El cerrojo nativo puede cambiar después esta preferencia.
No se redefine un punto explícito previo ni se le asigna un rol interactivo.
El permiso de arrastre es necesario, pero la suficiencia kernel de cada nuevo
movimiento sigue siendo obligatoria. El checkbox no certifica continuación.

Para homogeneidad se permite que el mismo panel seleccione como fuente los
objetos 2D nativos lineales y cónicos (`GeoLine`, incluidos segmento/rayo,
y `GeoConic`). La posición inicial procede del clic o del valor copiado de Q.
Se reutiliza exclusivamente `AlgoDispatcher.point(Path,x,y,...)` y su algoritmo
nativo `AlgoPointOnPath`: no se implementa proyección geométrica en Desktop.
Esta ruta declara la selección/proximidad numérica nativa; no afirma mínimo
certificado, unicidad ni evidencia Locus V2. No incorpora Q como padre.
Se excluyen objetos 3D, listas, regiones, loci legacy y Paths arbitrarios de
esta ampliación, al igual que los ejes auxiliares fuera del orden de construcción;
sus herramientas normales siguen el contrato heredado.

La publicación nativa se hace dentro de `runAtomicConstructionMutation`, con
fuente/Q actuales y finitos, y se valida el resultado definido antes de cerrar.
Un fallo restaura el documento por la transacción normal. No se fabrica un
estado semántico Locus V2 para un Path nativo, ni se convierte GeoLocusV2 en Path.

## P2 y gesto

Antes del selector modal de fuentes del modo Punto/Punto en objeto se capturan
vista, coordenadas world y radio world derivado de la transformación vigente.
Después se usa exactamente esa petición, incluso si cambia zoom/pan mientras
está abierto el selector; un cambio de instancia de vista invalida la acción.
El frontend nunca recalcula desde las coordenadas antiguas de pantalla.

El arrastre de un punto semántico conserva la misma instancia e identidad;
el servicio kernel decide la continuación. No hay modal por movimiento.
Un contexto kernel de continuación se abre una sola vez al pulsar y se pasa
a todos los movimientos del gesto; Desktop no elige ejes, hojas ni parámetros.
Se descarta al soltar, cambiar herramienta o perder una referencia restaurada.
Un rechazo muestra un diagnóstico breve y conserva el último estado válido.
El controlador mantiene un único registro Undo por gesto aceptado. Los puntos
bloqueados no se toman como destinos de arrastre semántico.

## Validación y compatibilidad

Pruebas Desktop nativas: checkbox por defecto/desactivado; persistencia fixed;
Q transitorio y fuente dependiente; creación/arrastre nativos sin nueva
definición; P2 con cambio efectivo de transformación durante elección; click/Q
del círculo v3; gesto y conservación de instancia/dependientes; regresiones
R6/Spline y M08-S-R2. La verificación canónica y smoke real los registra el
integrador sobre el candidato final; una prueba AWT sin ventana no sustituye
el smoke físico.

El conjunto mínimo editable de Desktop comprende controlador, interacción,
panel y adaptador de referencia, un adaptador pequeño de publicación Path
nativa y pruebas. No cambia formato `.cedg`, upstream global, comandos ni
geometría de los Paths. La tabla nueva de creación/continuación Locus V2 se
define en su enmienda kernel candidata independiente, sin modificar R1/R6/H1.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: mismas bibliotecas/runtime/Gradle y
prerrequisitos. VERIFICATION_INFRASTRUCTURE_IMPACT=NONE. GUIDE_IMPACT=UPDATE:
el protocolo de smoke del candidato debe explicar el checkbox y el alcance
de fuentes 2D nativas. Nivel requerido: PHASE + INTEGRATION según clasificación
del candidato integrado.

## Enmienda focal de diagnóstico visible — 2026-11-03

La revisión del checkpoint técnico `2a1b6794` demuestra una insuficiencia de
interfaz: `EuclidianViewD.setToolTipText` delega en Swing y no establece siquiera
texto cuando la vista tiene tooltips desactivados; además el controlador lo
borra al soltar. Por tanto esa ruta no garantiza el diagnóstico breve exigido.
El autor del encargo autoriza esta corrección necesaria de presentación.

Se reutiliza `GeoCeDGStatusBar`: segmento propio, etiqueta no enfocable, diseño
de una fila y el temporizador cancelable existentes. El aviso se muestra al
rechazar un movimiento, conserva unos segundos de lectura después de soltar y
se deduplica mientras conserva el mismo texto. Tiene prioridad visual al inicio
de la fila; no comparte identidad ni cancelación con el aviso de unidades al
pegar. Un movimiento válido, una nueva pulsación, el cambio de herramienta o
documento lo borran. Un vencimiento antiguo no puede borrar un aviso posterior.
No aparece ventana nueva ni se solicita foco o selección.

Conjunto acotado: barra de estado, controlador, limpieza de sesión en AppGeoCeDG
y una regresión Desktop sobre tooltips OFF, texto visible tras soltar,
vencimiento/deduplicación y conservación de XML, ID, Undo, foco y selección.
El kernel y sus pruebas/proveedores no cambian. Debe existir un nuevo checkpoint
antes del catálogo y la ejecución Desktop final; la evidencia del checkpoint
anterior se conserva con su identidad. GUIDE_IMPACT=UPDATE: el mensaje breve
se describe como aviso de la barra de estado, no como tooltip garantizado.

## Enmienda focal de publicación nativa — 2026-11-03

Registrada antes de su código. La ruta adicional de referencia Q nativa llama al
constructor normal, que publica la etiqueta y notifica sincrónicamente `View.add`
antes de retornar. Por tanto fuente y Q deben volver a comprobarse dentro de la
misma transacción, después de esa publicación y del cerrojo. Se reutiliza la guarda
de vigencia original: fuente/registro/documento y Q actuales, estado de fuente y
coordenadas copiadas de Q. El punto final debe conservar el padre nativo normal
de una entrada, la fuente esperada, pertenencia a la construcción y el estado
fixed solicitado, además de finitud y residual nativo ya comprobados.

Un cambio síncrono de fuente o Q rechaza y restaura la transacción normal. Una
regresión concreta debe activar esa ventana mediante `View.add`; no basta un
rechazo anterior al constructor. No se añade solucionador, proximidad certificada,
identidad por coordenadas ni una segunda instantánea. La garantía fuerte de los
permisos Locus V2 nuevos sigue delimitada por su enmienda de estado host; no se
atribuye a todos los Paths heredados una fidelidad de persistencia no demostrada.
