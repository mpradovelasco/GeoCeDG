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
