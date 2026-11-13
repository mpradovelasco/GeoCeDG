# CU8-I · Incorporación y colocación en Gráficos 1

Esta revisión amplía [CU8-I v1](control-layout-v1.md) y conserva la
composición y el contexto de [R1/R2](control-layout-r1.md).

## Controles y respaldo

Se admiten deslizadores independientes de tipo numérico y angular con
intervalo nativo y posición libre, incluidos los situados en coordenadas
del lienzo. También se admiten botones y campos con ancla nativa estática
en el lienzo. Se mantienen la pertenencia exclusiva a Gráficos 1, el
rechazo de grupos, puntos de ancla externos y bloqueos de posición, y el
predicado v1 de las casillas. Los límites de un deslizador pueden
depender de otros objetos de la construcción.

La ranura aporta coordenadas de presentación absolutas al drawable de
Gráficos 1. Para un deslizador situado en el lienzo, captura su anchura
en píxeles lógicos usando la escala del eje de su orientación al iniciar
la colocación. Una anchura de presentación no positiva o no finita se
rechaza. El dibujo y el recorrido del puntero usan esa misma anchura.
La instancia, el tipo, los valores, las unidades, los límites, los
incrementos y el respaldo nativo permanecen en la construcción. Classic
de la misma distribución usa ese respaldo. Separar un control con
posición nativa de lienzo restaura su ubicación y tamaño de lienzo.
Separar un control absoluto conserva la política de v1. Quitar un
panel libera a todos sus miembros en su respaldo.

## Colocación

El selector de miembros se llena tras confirmar un alta. Con el panel
seleccionado, **Añadir control** previsualiza el único control seleccionado
si es elegible. Si la selección está vacía, activa una captura de un clic
en Gráficos 1 y previsualiza el control capturado. La propuesta busca sitio bajo
el encabezado, con margen interior, considerando las dimensiones del
control y los miembros existentes. El puntero ajusta la propuesta en
el panel y un clic válido o **Confirmar colocación** la aplica.
**Confirmar colocación** también acepta la propuesta inicial sin otro clic
en el panel. Escape cancela la captura, la previsualización o el gesto de edición
y deja abierto el gestor. Con el foco en el gestor y sin una acción pendiente,
Escape lo cierra y se puede volver a abrir desde el menú Vista o el panel.
Un solape impide confirmar el alta en ese punto. Si falta espacio, el panel crece por la derecha o
por abajo hasta contener el control y el margen, conservando el origen
y la posición de sus miembros. Los gestos de edición siguen disponibles.

La previsualización es estado local de Desktop. Confirmar revalida
el contexto, el panel, la instancia y la transformación, y publica
ranura y tamaño en una única operación de historial. La separación
es otra operación. El registro se serializa por instancia.

Una selección múltiple, un control incompatible o un contexto que deja de
ser válido se indica en el gestor. El rechazo termina el intento y deja
disponibles las acciones contextuales. Cambiar de panel, cerrar el gestor
o abrir un menú contextual cancela la captura y la colocación pendientes.
La propiedad nativa de orientación del deslizador rige la colocación,
el dibujo y los gestos. Control Layout conserva esa propiedad y la
pertenencia a vistas en todos sus recorridos. Si un control suelto pertenece a
Gráficos 3D, el gestor indica dónde desmarcar esa vista. Si la posición de un
deslizador está fijada, indica cómo quitar la fijación.

**Propiedades del panel**, en el gestor y en el menú contextual, abre el
editor R1 de nombre, color y transparencia del panel. **Propiedades del
objeto** abre el editor nativo del miembro. Escape descarta la edición de
apariencia y devuelve el control al gestor si está abierto.

## Formato y compatibilidad

El encabezado conserva la estructura v1 y usa el prefijo `2|` cuando
alguna ranura exige esta revisión. `A` identifica al deslizador
angular. Un deslizador de lienzo exige un sexto campo de ranura
con la anchura de presentación, decimal finita estrictamente positiva
con la gramática de coordenadas. Los controles absolutos conservan
cinco campos. Los demás tipos de lienzo tampoco necesitan un sexto
campo. El lector valida familia, modo de posición y anchura contra
la instancia nativa. Un encabezado v1 conserva el predicado v1.
La apariencia de R1 conserva su formato. Un documento que solo
requiere v1 sigue escribiéndose en v1. El límite total v2 añade hasta
25 bytes por ranura al límite v1 para alojar la anchura. Se conservan
los límites de 32 paneles y 256 ranuras.

Los lectores anteriores a CU8 que ignoran las marcas abren la
construcción nativa y pueden perder la disposición al guardar.
Los lectores CU8 que solo admiten v1 rechazan un encabezado v2.
El aviso de Desktop identifica ambas situaciones.

## Verificación

La fase CU8-I cubre la presentación y el ciclo de archivo e historial.
INTEGRATION cubre las regresiones compartidas, Desktop y el transporte.
El ejercicio interactivo usa una copia del modelo registrado
`InterCilConoOblique.cedg`. Sus ángulos tienen bloqueo de posición y
asignación 3D. La copia de prueba los desbloquea y los deja solo en
Gráficos 1 antes del alta.

```text
VERIFICATION_CLASS = INTEGRATED_PHASE
VERIFICATION_PROFILES = PHASE PRE-G9B-CU8-I, INTEGRATION
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = Se conservan launcher, JDK, Gradle y requisitos de estación.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_es.md, docs/user/geocedg_user_guide_en.md
selfApproved = false
```
