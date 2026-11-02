# M08-S-R2 Desktop Events — matriz y smoke autoral pendiente

Este protocolo no afirma ejecución humana posterior a la corrección. Candidato
identificado por commit/árbol en el delivery del expediente Events. Nunca sustituir
el archivo original ni las evidencias D1/H1. No automatizar la sesión Windows.

## Matriz de evidencia

| Caso | Nivel de prueba y oráculo | Resultado exigido |
| --- | --- | --- |
| R1 extremo (2,0), Q=(2.02,0.01), r=.1 | Nuevo dispatch AWT del controlador y botones Swing | R1_R6 UNIQUE; confirmación embebida; P=(2,0) |
| h/ReferenceR1 nativo exacto | Sonda optativa con hash original y dispatch AWT | R2_H1; evidencia local H1; confirmación explícita; forward durable |
| Fixture h construido por comandos | Regresión normal de controlador, diferenciada del nativo | R2_H1; sin unicidad global inventada |
| Frame.windowLostFocus y popup real anterior | Caracterización optativa antes; flags UI simulados | Confirmación anterior oculta, selección pendiente; XML igual |
| Frame.windowLostFocus y panel corregido | Regresión normal, frame no mostrado | Selector embebido permanece; ningún heavyweight de elección |
| Sincronizar mismo modo 145 | Dispatch L, setMode DOCK_PANEL, dispatch Q | L conservado, consulta positiva |
| Foco de canvas/panel | FocusEvent controlado; no foco OS | Estado conservado; sin reconsulta |
| Editar/aplicar r y Policy | Editor/acciones Swing más eventos de canvas | L conservado; oferta previa consumida; nuevo Q necesario |
| Política inválida o presupuesto agotado | Constructores/autoridades vigentes y controller | Rechazo sin mutación ni publicación parcial |
| Varios hits Q | Hits reales solapados, lista de referencias | Ningún Q implícito; elección de objeto y de dirección separadas |
| R1 múltiples | Eventos de spline con varias preimágenes | Ningún índice elegido por defecto; elección explícita |
| Caducidad/consumo/cancelación | AWT, mutación posterior de Q, botones retirados | Rechazo/cierre; XML y registros sin publicación |
| Escape/cambio de modo/retorno a herramienta | Bindings Swing y ruta host existente | Limpieza; sin puntos libres; reinicio explícito |
| Teclado inline | Dispatcher real y bindings Swing controlados | No atajos globales geométricos desde la lista/botones |
| Una operación Undo y Redo | Publicación por botón tras eventos; UndoManager real | Un Undo retira P; Redo restaura su identidad |
| Q libre/dependiente/antecesor de L | Históricas y nativo con kernel real | Ningún padre Q nuevo; dependencia legítima a través de L |
| Cambio L y dependientes P | Regresiones históricas de forward | Propagación dinámica sin geometría en Desktop |
| Guardar/reabrir .cedg, copia y remapeo | Históricas más sonda nativa Events | ID/dirección persistidos; copia nueva con linaje; sin Q ficticio |
| Punto/Punto en objeto/arrastre SplineV2 | Regresiones históricas e INTEGRATION | Capacidades previas conservadas, no arrastre general nuevo |
| Layout EN/ES, reconstrucción input y anchos | Árbol Swing y layout no visible | Panel NORTH de AlgebraInputD, radio único, texto íntegro |
| Render, DPI, foco físico, mouse Windows | Smoke humano pendiente | Autor verifica recorrido y presentación reales |

Los intentos fallidos del arnés se conservan con causa y código. El índice final
relaciona cada JUnit con su fuente; no se interpreta un programa de click/commit
como prueba de los listeners gráficos. Tampoco se interpreta AWT sintético como
smoke físico. La campaña final exige los cuatro perfiles canónicos aplicables.

## Smoke humano: caso autoral principal

1. Abrir el candidato exacto y una copia de `C:/Temp/TestM08-S.cedg`. Confirmar la
   entrada algebraica visible y modo CeDG. No guardar sobre el original.
2. Crear temporalmente en esa copia
   `ReferenceR1=(0.8976484384297626,1.7854818601338847)`.
3. Activar «Punto sobre Locus V2 desde referencia». Debe aparecer un panel integrado
   inmediatamente encima de la entrada, no una ventana sobre la construcción.
   Instrucción «Seleccione L» y un único r world=0.1. No mensaje de cobertura previa.
4. Clic sobre h en un tramo no ocupado por Q (en la vecindad (.8,2.4) del modelo
   original). Observar h seleccionado e instrucción «Seleccione Q existente».
5. Sin cambiar r, clic sobre ReferenceR1. Debe aparecer la dirección semántica y el
   aviso de que no se establece unicidad global. Todavía no debe existir P.
   Si varios objetos coinciden en el hit, elegir explícitamente ReferenceR1 en el
   selector de objetos y después confirmar la dirección.
6. Pulsar Confirmar. Debe aparecer P por la construcción forward existente. Una
   sola acción Deshacer lo retira y Rehacer restaura su identidad/dirección.
7. Mover ReferenceR1: P permanece, salvo dependencia legítima de Q a través de L.
   Cambiar una entrada de h: P/dependientes se reevalúan según forward; no ampliar drag.
8. Guardar copia .cedg, reabrir, comprobar P y dirección. Copiar P: nueva identidad
   y remapeo/linaje existentes, sin añadir ReferenceR1 como padre ficticio.

## Smoke de estado, compatibilidad y presentación

1. Entre L y Q editar r a 0.2 y volver a 0.1. L debe seguir seleccionado; no se abre
   otra ventana, no se reconsulta ni se pierde L. Policy se despliega sin reinicio.
2. Tras una oferta editar política. La interfaz debe anunciar cancelación de la
   elección anterior. Confirmar viejo control no puede publicar; volver a clicar Q.
   Texto inválido/NaN bloquea consulta con diagnóstico; corregir sin cambiar geometría.
3. Clic vacío al esperar Q no crea punto libre. Escape, Reiniciar, Cancelar y cambio
   de herramienta limpian la operación. Repetir varias construcciones sin reinicios
   inesperados. Cambiar foco panel/canvas no oculta la confirmación ni reconsulta.
4. En documento temporal crear spline recto
   `S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)` y `Q=(2.02,0.01)`.
   Herramienta → clic en S → clic Q → Confirmar: P en (2,0) mediante R1/R6.
5. Comprobar caso con múltiples direcciones: ninguna elegida por defecto. Confirmar
   requiere elección semántica explícita. Fuentes/presupuestos no admitidos rechazan
   sin objetos nuevos. Herramientas Punto y Punto en objeto y drag SplineV2 siguen
   funcionando como antes; no se ofrece arrastre nuevo de Locus general.
6. Idiomas EN/ES; Tab, lista con flechas, Enter y Escape. Comprobar que flechas en
   selector no mueven la geometría seleccionada. Redimensionar, diferentes DPI y
   volver a la herramienta: panel alineado con entrada, sin overlay ni clipping que
   impida operar. Semántica larga desplazable. Modalidad avanzada compacta intacta.

Si falla: anotar commit, idioma, r/policy, modo/instrucción antes y después de cada
clic, objetos presentes bajo el cursor, selector de objetos/direcciones visible,
diagnóstico y si Undo muestra creación. Captura manual de panel+construcción si es
posible. No inferir causa kernel de ausencia visual de confirmación. No sobrescribir
archivo original. Informar el paso exacto; no ejecutar automáticamente sondas antiguas
que muestran ventanas o controlen entrada nativa.

M08-S pendiente de smoke y aprobación; M09 pendiente y excluido. Mantener CU10 y
deuda científica. Detenerse tras entrega técnica, sin promoción ni nueva fase.
