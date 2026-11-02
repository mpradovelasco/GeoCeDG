# M08-S-R2 Desktop Events — diagnóstico anterior a corrección

Estado: candidato técnico; smoke autoral NO superado. Base productiva intacta:
`7ef6a26c573c57a3959b611f0f4c2a8dbc3f5027`, árbol
`51abc83fbb911788335e21d354456b2262c092cb`.

## Evidencia y causa delimitada

La sonda optativa `Cu7M08SDesktopFocusProbeTest` despacha a la superficie gráfica
real los eventos AWT moved/pressed/released/clicked; no llama `tool.click/commit`.
Para R1 extremo y el archivo autoral h/ReferenceR1 verifica modo 145, conservación
de L, hit Q, ejecución efectiva de consulta y resultado positivo de su autoridad.
La confirmación es un JPopupMenu con JPanel/JList que puede convertirse en una
ventana heavyweight focusable al no caber en su propietario. La fábrica Swing
real del JDK produce `javax.swing.Popup$HeavyWeightWindow`, del frame correcto.

`GeoGebraFrame.windowLostFocus` oculta expresamente todas sus ventanas de esa
clase. El handler no cancela el JPopupMenu: su referencia privada `popup` sigue
instalada, `menu.isVisible()` sigue siendo true y la selección del kernel queda
pendiente. Un segundo gesto sobre Q no consulta ni publica: la herramienta retorna
porque conserva `selection != null`. Así se demuestra una confirmación invisible
capaz de bloquear ambas rutas, sin defecto del resolvedor.

La entrega al handler real se realiza con estado de visibilidad Java simulado
mediante reflexión en el arnés; no hay peers visibles, entrada nativa, Robot ni
control de la sesión Windows. **No demuestra que Windows haya entregado ese foco
en cada intento del autor**, ni sustituye su smoke. La reproducción física anterior
observó el síntoma; no tenía traza suficiente para identificar su causa. Las pruebas
previas con JFrame ordinario no incorporaban este handler upstream y no detectaban
la interacción. La colisión comprobada justifica retirar esa dependencia de ventanas
transitorias y utilizar el panel embebido ya autorizado, sin alterar el handler global.

Otras dos causas deterministas medidas con despacho AWT y XML antes/después:

- Reaplicar el mismo modo desde DOCK_PANEL descarta L sin cambiar la construcción.
- Aplicar política descarta L; el siguiente clic en Q no puede ejecutar la consulta.

El código del editor además llama reset al abrirse. El radio se repite en las dos
instrucciones y en el editor; no es una segunda solicitud del kernel. No se atribuye
al sistema operativo una apertura automática del editor que no fue registrada.

## Resultados reproducibles y límites

`artifacts/pre-g9b-cu7-i1-r2-desktop-events/controlled-before-attempt3/process.json`:
producer COMPLETED, inner_exit_code 0; JUnit 4/4, sin fallos ni skips. Trazas
FOCUS_REPLAY R1_R6/R2_H1, SAME_MODE_REPLAY y POLICY_REPLAY. Original nativo:
`C:/Temp/TestM08-S.cedg`, SHA256
`4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`;
ReferenceR1 sólo se añade en memoria con la posición autorizada, radio world 0.1.

Se conservan los intentos 1/2 con códigos 1: errores del arnés al no montar el canvas
en el frame y al simular el flag incorrecto de JPopupMenu, respectivamente. No son
regresiones de producto. La sonda física interrumpida sólo alcanzó READY/foco en su
log; no es evidencia de un recorrido terminado ni aceptación. Sus fuentes se conservan
fuera de los source sets normales y no se vuelven a ejecutar automáticamente.

## Corrección mínima diseñada

Panel M08-S específico dentro de AlgebraInputD, BorderLayout.NORTH, sin ventanas
flotantes, posiciones absolutas ni PopupFactory para elegir Q/dirección. Un único
radio visible; Policy desplegable con campos existentes y alcance/cobertura técnicos.
Conservar L al sincronizar el mismo modo y al editar/aplicar política. Toda edición
invalida ofertas pendientes explícitamente, sin reconsulta automática ni mutación.
Mantener el carrier GeoCeDGReferencePlacement y todo el kernel literalmente intactos.
El teclado de controles propios se resuelve en Swing, evitando los atajos geométricos
globales mientras el foco pertenece al panel. Escape/cambio real de modo cierran el
estado; selección por referencias reales y publicación por la operación ya aprobada.

La validación posterior debe ejercitar listeners del controlador, montaje real del
panel, botones Swing, las dos autoridades y Undo/Redo; distinguirla expresamente del
smoke físico pendiente. STATIC/INFRA_UNIT/PHASE/INTEGRATION sobre candidato congelado.
No ADR aceptado, main, libro ni fases excluidas. No cambio de autoridad científica.
