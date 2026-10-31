# CU7-I1-M08-S-R2 — herramienta L → Q y dirección avanzada compacta

Estado: **DISEÑO CANDIDATO, SIN CÓDIGO PRODUCTIVO**. La secuencia gráfica L→Q es requisito
autoral de 2026-10-31. Este documento propone su realización, no pide volver a aprobar ese
requisito ni declara aprobados el permiso local, la política concreta o una implementación.
Base/contrato: [selección local individual](pre_g9b_cu7_i1_r2_refinement_local_selection.md).

## 1. Costuras existentes y diferencia funcional

`GeoCeDGEuclidianController.switchModeForProcessMode` deriva MODE_LOCUS_V2_POINT a
`createSemanticPoint`, que selecciona un solo GeoLocusV2 y abre `GeoCeDGLocusV2Dialogs`.
No existe en esa ruta un estado que espere el clic gráfico sobre Q. La pestaña R1 enumera
puntos existentes mediante objetos ResultChoice, no es una operación gráfica L→Q.
`GeoCeDGPointInteraction.createFromReference` ya valida pertenencia real al documento, copia
coordenadas de un GeoPoint definido y consulta el kernel; sólo consume admisión histórica R6.
Debe reutilizarse la separación de responsabilidades, sin tratar este helper como permiso nuevo.

La herramienta Punto ordinaria tiene otra ruta de clic/arrastre, con radio derivado del hit
gráfico en `interactionRadius`. Se preserva. La herramienta desde referencia **no utilizará**
ese radio: el píxel decide qué objeto se selecciona, nunca cuánto debe distar P de Q.
No modificar globalmente el modo Punto ni su semántica SplineV2 para añadir esta autoría.

El perfil vivo contiene `semantic.locus-v2.point-explicit`, modo auditado 134, experimental.
Recomendación: conservarlo para la dirección avanzada, y añadir una herramienta diferenciada
«Punto sobre Locus V2 desde referencia» / «Point on Locus V2 from reference», en el perfil y
su grupo de herramientas. Identificador candidato `semantic.locus-v2.point-from-reference`;
no existe hoy. Reservar/auditar el destino de modo al implementar, sin inventar un número
ejecutable en este diseño. Misma feature gate cedg.locus.v2 y madurez experimental; no promover
a estable ni añadir comandos algebraicos. Icono propio/reutilización autorizada y textos del
perfil, sin dispersar la definición del toolbar en código upstream.

## 2. Máquina de interacción, propiedad y cancelación

Estado efímero del controlador por instancia de aplicación/vista, no de la construcción.
Son descripciones, no clases nuevas implementadas:

| Estado | Acción | Transición y efecto |
|---|---|---|
| Espera L | clic en un GeoLocusV2 vigente, incluido SplineV2 | retener handle real del objeto; resaltar sólo selección; Espera Q |
| Espera L | vacío, punto u objeto no admitido | consumir evento, sin crear nada; indicación «Seleccione un Locus V2» |
| Espera Q | clic en GeoPoint 2D existente y definido | validar mismo documento; copiar q actual y política; consultar kernel |
| Espera Q | clic explícito en otro Locus V2 | sustituir selección de L, seguir esperando Q; ninguna consulta heredada |
| Espera Q | vacío u objeto distinto | no crear Q, no llamar a createNewPointForModeOther; indicación breve |
| Consulta | UNIQUE completo R6 vigente y permitido | publicar por la ruta vigente, sin diálogo; el clic Q es la acción creadora |
| Consulta | MULTIPLE vigente R6 o permiso local nuevo | selector/confirmación contextual, sin ventana de introducción de datos |
| Consulta | ausencia, no establecido, inválido, barrera o agotamiento | diagnóstico breve tipado; sin mutación; conservar L para elegir otro Q |
| Confirmación | aceptar una oferta vigente | publicar exactamente su dirección, un Undo; limpiar estado y volver a Espera L |
| Confirmación | cancelar | destruir consulta/preview; conservar L sólo como selección visible, volver a Espera Q |
| Cualquier estado pendiente | Escape, cambio de herramienta/documento/vista o reiniciar | consumir/inutilizar nonce y selección; limpiar preview/referencias; cero objetos/Undo |

Tras Escape o reinicio no se reutiliza L/Q ocultamente; si el modo permanece activo empieza
en Espera L. El cambio de herramienta sigue el ciclo del host. Tras éxito se inicia otra
secuencia L→Q completa. No memoria tácita de última rama, último candidato o último Q.

En intersecciones gráficas de varios L o varios Q, usar selección contextual de **objetos**
reales antes de la consulta, sin escoger por etiqueta, primer elemento del hit o cercanía.
No confundirla con el selector posterior de direcciones matemáticas. El nombre es presentación;
la selección transporta handles reales de construcción verificados. Una referencia Java es
un handle efímero, no una identidad durable ni prueba de correspondencia semántica.

Interceptar el modo nuevo antes de las rutas upstream que crean puntos libres o mueven Q.
Hover/selectionPreview no consulta ni publica. Un arrastre durante la selección no mueve Q
ni crea un punto libre. No añadir GeoElement auxiliares como preview: usar overlays de vista
con datos del kernel, sin autoridad geométrica. Si el cálculo se difiere, su devolución debe
comprobar nonce, modo y documento antes de presentar o publicar; Escape no deja un callback
capaz de crear más tarde. Preferir flujo acotado en el hilo normal de construcción, sin
trabajo paralelo sobre estado mutable no autorizado.

Revalidar L, q/política, contexto exacto, componente, revisión y oferta al confirmar. Si Q
cambia durante el selector, cancelar y consultar de nuevo su posición actual; nunca seguirlo
automáticamente como definición de P. Un Q libre sin ID no necesita adquirir identidad para
ser referencia transitoria. Un Q dependiente o antecesor de L conserva sus dependencias reales.
P sólo depende de L y de los inputs forward explícitos; si Q influye ya en L, esa ruta permanece.

La publicación reutiliza creación forward y ownership interactivo existente, no redefine un
punto. Debe pasar una sola vez por la frontera de Undo del controlador: no combinar
storeUndoInfo explícito con el callback del host que ya almacena Undo. La confirmación
contextual diferida deberá finalizar esa misma operación, renovar el estado de modo del host
y evitar que initNewMode la trate como creación inconclusa. Son obligaciones de prueba;
los hooks existentes no constituyen evidencia de que estén ya satisfechas para el flujo nuevo.

## 3. Política declarada sin diálogo obligatorio

Propuesta para decisión: política de sesión versionada del modo desde referencia, inicializada
con **los valores ya existentes**, no con una distancia obtenida de la pantalla:

| Dato | Propuesta inicial y justificación |
|---|---|
| Radio world | .1 unidades coordenadas del modelo; conserva el valor inicial visible de la pestaña Q de R1 y la consulta caracterizada del autor; es conveniencia de autoría, no escala universal ni certificado |
| Tolerancias de búsqueda | 1e-10 distancia comparable / 1e-12 parámetro de `LocusPointInteractionPolicy2D.initial`; sólo control de búsqueda, no prueba exacta ni identidad |
| Límites existentes | 32768 evaluaciones / 512 subdivisiones / 80 iteraciones de refinamiento / 1024 candidatos, `LocusPointInteractionWorkBudget2D.initial` |
| Alcance | ramas/componentes declaradas y certificadas del proveedor soportado; sin preferencia por historial; cobertura de búsqueda separada de la de componentes |
| Nuevo permiso | VALID_NEAR_ADDRESS bajo sus testigos; política de admisión candidata versionada aparte, sin cambiar la versión/resultado histórico R6 |

El valor .1 no se presenta como adecuado para toda unidad o tamaño de documento. Se propone
porque evita introducir un default numérico nuevo y deja el caso nativo a .01 dentro del radio;
si la unidad/escala exige otro radio, el usuario lo configura explícitamente. Ninguna curva se
acepta por estar a .1 según un render o un cálculo sin evidencia. Un error de búsqueda no
ensancha el radio, sube el presupuesto ni selecciona otra rama silenciosamente.

Mostrar siempre una indicación compacta al activar: «Referencia • radio 0,1 u • política v1»
/ «Reference • radius 0.1 u • policy v1», con enlace/botón «Política…» / «Policy…» y ayuda
accesible por teclado. Cambiar el radio en un control no modal opcional; detalles plegados
exponen todos los límites, tolerancias, alcance y versiones, con textos EN/ES. Aceptar sólo
valores finitos positivos y presupuesto válido. Edición invalida consulta pendiente.
No abrir este control durante la secuencia normal ni exigir rellenar campos por creación.

Guardar la configuración sólo como estado de sesión del cliente en el primer incremento,
no como campo .cedg, parent de P ni preferencia global oculta heredada de otro documento.
Al activar en un documento distinto, usar el perfil inicial declarado; exponer que el valor
es en sus coordenadas. Capturar la política efectiva completa en cada consulta; fuente/q/
política iguales y misma revisión producen el mismo procedimiento acotado. Una futura
preferencia persistente necesita su decisión propia; no es necesaria para dos clics.

La prueba intervalar también tiene que estar acotada. Propuesta de contabilización nueva:
cada llamada de valor/jet intervalar y forward de control cuenta contra el remanente del
techo de evaluaciones de la consulta compuesta, además del trabajo R6 ya contabilizado.
La vía puntual comienza con valor, primer jet y forward por dirección, sin refinamiento
adicional de raíces. Los límites de subdivisión/refinamiento R6 mantienen su significado
en cada ruta; no interpretar el contador acumulado de iteraciones como si demostrara un
techo global diferente al que el código aplica. No reiniciar contadores para ocultar coste.
Captura de programas mantiene sus límites estructurales propios. Si faltan remanente,
enclosures suficientes o cota de trabajo para captura/transferencia, no ofrecer; no inventar
un presupuesto adicional ilimitado. Esta composición aún no está implementada y su coste
debe medirse en la puerta de kernel antes de fijar valores productivos definitivos.

## 4. Selector contextual pequeño y mensajes

Un UNIQUE R6 completo puede crear directamente como antes. Un resultado local sin unicidad
global exige una aceptación adicional explícita, aunque sólo haya una dirección descubierta.
No ocultarla dentro del clic Q ni seleccionar por defecto el primer candidato de una lista.

La confirmación muestra «Dirección local verificada; unicidad global no establecida» /
«Verified local address; global uniqueness not established», resumen de rama/componente y
parámetro con acceso al contenido íntegro, «Crear» / «Create» y «Cancelar» / «Cancel».
Para varios candidatos: lista de ofertas verificadas con selección inicial vacía y botón
Crear deshabilitado hasta la elección. El orden sólo organiza presentación; el botón consume
el handle de oferta y dirección exactos de la consulta, nunca un índice persistido.
Marcadores opcionales de vista no sustituyen la dirección ni sus testigos.

Usar panel contextual ligero anclado a la vista con foco/lectura accesibles, límite al área
útil y scroll para listas/identificadores largos. No el diálogo avanzado ni un JOptionPane
que adopte el ancho máximo de todos los identificadores. La adaptación a DPI y resolución
es presentación; no altera radio, tolerancias, candidatos o permiso.

Diagnóstico breve, sin ventana modal habitual: fuente/Q inválidos; ninguna dirección probada;
ausencia certificada sólo con cobertura; evidencia local/autoridad insuficiente; candidatos
sin pertenencia inequívoca; singularidad; presupuesto agotado; selección caducada/ajena.
Cancelar es silencioso. Creación fallida dice «No se creó ningún punto» / «No point was
created», nunca «no point was changed». Detalles opcionales conservan causa tipada y grados.

## 5. Diálogo avanzado más compacto, secundario

Causa estática del tamaño: `createSemanticPoint` contiene dos pestañas. La pestaña Q usa
`GridLayout(0,2,8,6)` y un JLabel sin wrapping para `point.snapshot.policyValue`, que enumera
todos los presupuestos y tolerancias en una sola línea. GridLayout iguala el ancho de sus
columnas al componente más ancho; el tamaño preferido del tabbed pane incluye esa pestaña
aun cuando se vea la de dirección explícita. Los dos JTextField(24) no son la única causa.
No se ha medido el tamaño real en píxeles en esta revisión ni ejecutado un smoke visual.

Recomendación: conservar la acción avanzada existente, con rama/parámetro y validación,
sin pestaña «Desde Q existente». El flujo Q pasa a la herramienta nueva. Diseño GridBag o
equivalente: etiquetas de ancho natural, campos que crecen con el espacio disponible y dos
botones. Espaciado derivado del LookAndFeel/font del host, no tamaño gigante fijo.

Ancho preferido de rama basado en una dirección habitual como `generator.main` más padding
del control; parámetro basado en una representación binary64 con signo como
`-3.141592653589793`. No ampliar la ventana a un ID completo arbitrario: campo desplazable,
Home/End, selección/copiar íntegros y tooltip/detalle accesible; nunca truncar el valor real.
Expresiones avanzadas conservan el parser y la validación actuales. Ajustar al área útil del
monitor y usar scroll si la fuente accesible o la traducción EN/ES exceden ese espacio.
No imponer alturas/píxeles físicos ni modificar contenido para hacerlo caber. Preservar
labelFor, nombres accesibles, orden Tab, Enter/Escape y validación localizada.

## 6. Inventario mínimo prospectivo, no archivos modificados

| Capa / fuente existente | Cambio posterior acotado |
|---|---|
| kernel `locus/interaction` | testigos de dirección/distancia/guarda, resumen de barreras y presupuesto, contexto separado; R6 sin cambio de significado |
| kernel `locus/intersection` | puente interno a modelos de valor/primer jet existentes; ninguna geometría duplicada en UI |
| kernel `LocusV2PublicOperations` | reutilización interna de inputs dedicados, rol, dirección requerida y limpieza forward; no fabricar candidate R6 «mínimo» |
| Desktop `GeoCeDGPointInteraction` | consultar valor transitorio de Q y consumir nuevo permiso; sin resolver geometría |
| Desktop `GeoCeDGEuclidianController` | modo L→Q, rutas de hit/cancelación/reinicio, ciclo de callback/Undo, sin tocar el Punto legacy |
| Desktop `GeoCeDGLocusV2Dialogs` | extraer selector contextual, conservar dirección avanzada compacta y quitar su pestaña Q sólo al existir la sustitución gráfica |
| perfil `apps/geocedg/application-profile.yml`, resolución de modos/acciones e icono/localización | entrada diferenciada experimental, textos EN/ES y política accesible; auditar ID, sin command/codec nuevo |
| tests shared/common-jre y Desktop, guías vivas | pruebas de kernel/UI/persistencia y documentación del flujo; integración editorial posterior CU10 conservada |

No ampliar `GeoCeDGToolActivation` como sistema de memoria genérico: su contrato actual
es de Parallel/Perpendicular. Reutilizar sólo las costuras de cancelación que realmente
apliquen, no sus referencias recordadas. Si se necesita cambiar el host upstream, justificar
cada hook mínimo en la puerta correspondiente. No implementación de M08-D, M09 ni drag general.
