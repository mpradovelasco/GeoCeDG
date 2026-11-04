# PRE-G9B-CU7 R5 — creación forward y referencia nativa: diseño previo

Fecha: 2026-11-04. Estado: implementación correctiva autorizada; candidato técnico.

## Clasificación y fuentes

Implementación/debugging en kernel de interacción y Desktop. Base R4 585c536a2986c929b1d64b31ae75967baa9afc9e. Se inspeccionaron AGENTS.md, contrato R6, LocusPointCreation2D, IndividualForwardPointCreation2D, LocusPointInteractionResolver2D, NativePathState2D, AlgoPointOnPath, GeoCeDGReferencePointTool, GuiManagerGeoCeDG y regresiones M08/R3/R4. No existe copilot-instructions.md ni AGENTS anidado pertinente. Auditoría R4: ACCEPTABLE WITH CONDITIONS, sin smoke físico; sus resultados se conservan.

## Causas estáticas y puerta de reproducción

La fachada sólo abre R2/H1 y forward cuando R6 devuelve exactamente GLOBAL_UNIQUENESS_NOT_ESTABLISHED. Una consulta general regular puede devolver LOCAL_EVIDENCE_INSUFFICIENT por su prueba numérica de mínimo estricto; ese resultado aborta antes de considerar el permiso forward independiente. Además, el descubrimiento forward consume discoverForSnapshot, que también exige evidencia de mínimo local. La creación individual autorizada no exige unicidad de proyección métrica. La reproducción gráfica del integrador debe identificar el mensaje y consulta concretos antes de declarar demostrado el fallo.

La modalidad Q nativa ya tiene un toggle en Punto/Punto en objeto y reutiliza AlgoDispatcher.point(Path,x,y). Su panel sólo vive dentro de AlgebraInputD; una barra de entrada oculta o no redistribuida puede hacerlo invisible. La selección canvas está integrada, pero la selección de Q desde Álgebra no tiene aún una ruta específica al panel. La disponibilidad funcional se comprobará en ventana real.

## Cambio kernel mínimo propuesto

Conservar sin cambios los resultados y permisos R6/H1. Reutilizar el resolvedor y su partición/refinamiento semánticos para extraer, en una consulta distinta y tipada, semillas de dirección forward antes de exigir el mínimo estricto. Una semilla no es un LocusPointInteractionCandidate2D ni autoriza publicación. No se alterará esa clase ni se marcará su evidencia local como establecida.

El descubrimiento forward separará los fallos de búsqueda/regularidad (indefinición, singularidad observada, agotamiento y dominio inválido, que cierran la ruta) de la ausencia de una prueba numérica de mínimo estricto (que no es requisito de esta admisión). La selección individual mantendrá captura reconstructible v3 fiel, existencia COMPLETE, componente certificada, selector estructural inequívoco, dirección canónica completa, reconstrucción forward finita, revalidación y publicación atómica. Proximidad sigue FLOATING_POINT_UNCERTIFIED; no afirma mínimo ni unicidad. Sólo se ampliará el despacho desde LOCAL_EVIDENCE_INSUFFICIENT cuando esa nueva consulta independiente demuestre sus propias precondiciones. Nunca se habilita un permiso por el Boolean del resultado R6 ni se activa F8.

La fachada mantiene prioridad R1/R6 y R2/H1. Los intentos completados descuentan trabajo del presupuesto acumulado; invalidación y agotamiento no abren una ruta alternativa. No cambia la dirección persistida, identidad ni contratos aprobados.

## Desktop nativo

Mantener clic heredado sin diálogo obligatorio. Hacer accesible la modalidad Desde referencia Q con panel contextual existente y montar/refrescar su contenedor real. Permitir seleccionar Q también desde Álgebra sin redefinirlo ni cambiar herramienta. La tabla efectiva se basa en NativePathState2D y restauradores tipados vigentes: recta, rayo, segmento y cónicas/partes 2D; no se atribuye soporte a Path arbitrario por su interfaz. Todos usan exclusivamente AlgoPointOnPath y conservan su propio parámetro/dominio/orientación. Q se copia transitoriamente y no entra en los inputs del algoritmo. Permitir arrastre usa fixed nativo, activo por defecto; fijo conserva dependencia del conductor.

## Validación y riesgos

Nuevas regresiones separan la creación individual sobre parábola, mensaje R6 no promovido, clic/Q y currentness/presupuesto; flujo nativo Q en círculo, segmento, parábola y familias lineales/arcos, Q independiente, fixed dinámico, Undo/Redo y reapertura. El integrador ejecuta primero tests focales y GUI-1 a GUI-4 reales; después congela el SHA técnico y ejecuta las cinco canónicas sobre ese SHA final. AWT/headless no se atribuye a smoke físico. Fallos originales no se reescriben.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED; mismos runtime y dependencias. GUIDE_IMPACT=UPDATE; ubicación y pasos de la modalidad Q. VERIFICATION_INFRASTRUCTURE_IMPACT=TEST_SELECTION; nivel FINAL. No main, libro, D1 aprobado, F8, nuevos comandos ni arquitectura universal.

## Evidencia gráfica causal incorporada antes del ajuste de política

El integrador reprodujo R4 en la ventana real: el fixture02 oculta Álgebra y Entrada. Un clic en L cerca de (1,1) produjo una oferta R2/H1 válida, pero su panel de confirmación estaba dentro de Entrada oculta. La selección no había fallado geométricamente. Tras mostrar Entrada, el mismo panel y Confirmar resultaron visibles. Otra petición mediante ReferenceQ=(.43,.12), después de aquel clic, usaba indebidamente el radio de píxeles transportado (.028284271247461905) como política Q persistente y mostró LOCAL_EVIDENCE_INSUFFICIENT. La distancia de Q a L excede ese radio: no procede fabricar admisión con él.

Se monta por tanto el mismo panel contextual en la fila inferior del producto cuando Entrada está oculta, sin cambiar preferencias del documento. La oferta de clic sigue mostrando y consumiendo su radio capturado exacto; una copia temporal conserva la política de referencia explícita y la restaura al abandonar esa oferta. Q nuevo vuelve a .1 por defecto o al radio que el usuario haya elegido expresamente. El agotamiento/ausencia con un radio menor sigue rechazado. El menú contextual del conductor ofrece Crear punto desde referencia Q / Create point from reference Q, reutiliza el mismo panel y admite Q desde Gráfica o Álgebra. En rutas nativas Q sólo inicializa la proyección AlgoPointOnPath y no se somete al radio de descubrimiento V2; clic nativo permanece heredado.

## Ajuste focal posterior a las primeras pruebas (sin alterar los casos históricos)

La primera ejecución focal conservada encontró una regresión real de la extracción de semillas: una imagen v3 colapsada carecía de la bandera COLLAPSED_IMAGE y la comprobación de mínimo estricto terminaba antes de observar su velocidad numérica cero. La consulta forward observa ahora, sólo en su auditoría privada, las guardas de regularidad existentes cuando falta aquella prueba de mínimo; una singularidad observada o evaluación inválida/no finita cierra el descubrimiento. No exige que exista un certificado C1/C2 ni que una capacidad diferencial opcional establezca regularidad universal. El resultado público R6, su diagnóstico y H1 permanecen iguales.

El test nuevo que suponía que alguna de siete peticiones parabólicas ordinarias producía LOCAL_EVIDENCE_INSUFFICIENT refutó esa hipótesis: todas eran ya admisibles bajo rutas existentes. Se mantiene el barrido como regresión de conservación. El caso focal independiente es un círculo v3 escalado con c=Circle((0,0),1E-6), N=Point(c), v=Vector((0,0),N), Q=N+2v, L=LocusV2(Q,N), petición (3E-6,0) y radio 1E-7. En ese caso la aritmética del mínimo estricto usa una escala mínima de1, distinta de la evidencia de dirección forward. El test exige registrar la razón R6 original, ruta seleccionada y residual FLOATING_POINT_UNCERTIFIED, sin cambiar aquella tolerancia ni afirmar una garantía métrica.

Se propaga además la auditoría privada a las evaluaciones de guardia de derivadas finitas: una evaluación fallida dentro de la guarda fina no puede confundirse con la ausencia de una prueba de mínimo. Una sonda inyecta ese fallo en el evaluador reconstructible real, conserva sus demás contratos y exige rechazo UNSAFE_DISCOVERY sin publicación. Es evidencia de fallo controlado, no una curva científica nueva ni smoke físico.

## Compatibilidad focal con las figuras de acotación E2/P4 — diseño previo

La calibración Desktop completa del checkpoint5d67 detectó una regresión nueva mediante un test histórico: el menú gráfico de la línea de una cota incorporaba indebidamente la nueva acción Q. Se revisan `native-dimensions.md` §4, §8.4 y §8.6 y ADR0034 aprobados. Sus salidas1–3 son segmentos con rol figura de un algoritmo de acotación, no conductores nativos ordinarios por el mero tipo GeoSegment; la interacción específica de la cota y su menú P4 tienen autoridad propia.

Se excluyen únicamente de la nueva modalidad Q los objetos cuyo propietario tipado `AlgoNativeDimension.ownerOf(geo)` sea no nulo. La exclusión se añade en el adaptador compartido de capacidades Desktop, de modo que contexto, selección canvas/Álgebra y constructor de colocación concuerden. No usa etiquetas, coordenadas ni coincidencia visual; no altera Path, los clics ordinarios, el algoritmo de cota, su desplazamiento ni los menús P4. Un segmento nativo ordinario sigue admitido. No se cambia ninguna norma ni test histórico.

Regresión nueva: un segmento ordinario ofrece Q; las líneas de dimensión y extensiones de cotas alineadas y lineales no adquieren Q, no abren ofertas y los intentos de publicación se rechazan antes de mutar XML, selección o historial. El integrador ejecutará de nuevo el test P4 original, el focal Desktop y la calibración completa. El fallo de calibración01 y su fuente se conservan, sin atribuirle aceptación.

## Corrección focal de pertenencia del contexto entre ventanas — diseño previo

En el smoke físico adicional del candidato ca895, tras trabajar en una segunda ventana Desktop y volver a la primera, la acción contextual Q dejó un panel sin los conmutadores específicos de modo. El clic posterior mostró Selection changed; make a new request antes de invocar geometría. Se conserva el registro original de eventos282–286 y la revisión externa multi-window-reference-context-review-01.md; no se reinterpreta como fallo de admisibilidad de la parábola.

La causa está en el límite Desktop heredado: cada DockManagerD registra un listener AWT global; eventDispatched entrega paneles de cualquier aplicación a setFocusedPanel sin comprobar su pertenencia. El componente de Graphics es un JPanel contenedor, por lo que la prueba de tipo del listener no lo excluye. El gestor A puede recordar la vista de B y devolver su controlador al menú de A. App.getMode también consulta esa vista extranjera. El rechazo posterior de currentness es correcto y debe conservarse.

Se añade una sola guarda al principio de DockManagerD.setFocusedPanel(DockPanelD,boolean), antes de generación y callbacks: sólo un panel registrado en el propio dockPanels, o null, puede alterar ese gestor. Es una comprobación de pertenencia de infraestructura transitoria, no identidad geométrica durable. Se mantienen la limpieza por null, el foco de paneles propios, el docking, el contrato ABA, la saturación de generación y todas las guardas de publicación. No cambia kernel, XML, Path, permisos R6/H1/forward ni la semántica de la selección. El punto de convergencia heredado es necesario para cubrir también llamadas directas y el listener global; una excepción sólo en el menú dejaría la contaminación activa.

Impacto upstream mínimo: cinco líneas en el método de convergencia y la procedencia en docs/upstream/modified-files.yml. No hay nueva API ni requisito de runtime. La mejora evita la selección de paneles de otra instancia también en Classic, sin imponer la política geométrica de GeoCeDG a Classic. Se conserva EUPL y se identifica el cambio en comentario local.

Validación previa al nuevo candidato: tres regresiones en R5ReferenceCreationDesktopTest, con dos aplicaciones embebidas sobre EDT. La primera envía MOUSE_CLICKED a Component.dispatchEvent y verifica su entrega global AWT, aislamiento de ambas aplicaciones y creación Q por sus controladores propios. La segunda rechaza paneles extranjeros por ambas entradas programáticas conservando construcción, selección, modo, historial, foco, generación y vigencia. La tercera conserva EV1→EV2→EV1 y null como invalidaciones reales, y permite una nueva colocación Q en el contexto propio. Estos tests no son input OS ni smoke físico. El integrador debe repetir las ventanas reales y los casos afectados sobre el nuevo SHA, y ejecutar después la verificación canónica vigente. Las evidencias de ca895 siguen atribuidas a aquel SHA.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED; ninguna dependencia ni herramienta nueva. GUIDE_IMPACT: NO ADDITIONAL CHANGE; la interfaz visible y sus pasos ya documentados permanecen iguales. VERIFICATION_INFRASTRUCTURE_IMPACT: TEST_SELECTION EXISTING; las regresiones se incorporan a una clase PHASE R5 ya seleccionada. Nivel requerido FINAL; sin aprobación autoral, D1 ni F8.

### Sincronización del nuevo test entre aplicaciones después del focal05

El focal05 conservado ejecutó88 casos:87 correctos y un fallo en la nueva comparación posterior a crear el punto nativo de la segunda aplicación. Sólo cambió el historial de la primera (0→1); construcción, vista, generación, modo y selección coincidieron. La causa de test se acredita por la ruta ordinaria: confirmar el punto semántico llama app.storeUndoInfo y UndoManagerD programa un hilo propio que añade su checkpoint después; el test tomó la instantánea de la primera aplicación antes de que terminara ese mismo trabajo legítimo. No se encontró una llamada de Undo de la segunda aplicación hacia la primera.

Se conserva íntegra la aserción de historial. El test separa sus tramos EDT, espera fuera de EDT la quiescencia tipada ya disponible en GeoCeDGUndoManager, inicializa la línea base ordinaria y espera su finalización antes de medir. Después de cada creación espera el store de su aplicación y comprueba exactamente un checkpoint propio antes de comparar el aislamiento frente a la otra. No se añade una espera al producto, se cancela trabajo ni se modifica la guarda, Undo o el scheduler. La corrección del test y el fallo original quedan registrados con cohortes distintas.
