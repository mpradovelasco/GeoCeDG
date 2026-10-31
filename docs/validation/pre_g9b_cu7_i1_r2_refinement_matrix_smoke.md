# CU7-I1-M08-S-R2 — pruebas, smoke futuro y puertas de precisión

Estado: **PLAN CANDIDATO; SMOKE Y PRUEBAS PRODUCTIVAS NO EJECUTADOS**.
Se añade a la [matriz R2](pre_g9b_cu7_i1_r2_matrix_and_gates.md), sin reescribir sus resultados.
Contrato nuevo: [selección individual](../architecture/pre_g9b_cu7_i1_r2_refinement_local_selection.md).
UI: [dos clics](../architecture/pre_g9b_cu7_i1_r2_refinement_interaction.md).

## 1. Matriz focal adicional

N = nueva inspección nativa/sonda racional; H = evidencia Java histórica R1; S = inspección
estática de código; F = prueba futura no ejecutada. Una condición esperada no es un PASS.

| Caso | Capacidad/oráculo y resultado esperado | Evidencia actual / pendiente |
|---|---|---|
| RF-01 h exacto del archivo y ReferenceR1 añadido | legacy authority del archivo, driver/graph correctos; R6 sigue UNRESOLVED; nueva oferta sólo con prueba del punto/captura real | N sintaxis/hash y modelo condicional; H rechazo/forward; F puente runtime |
| RF-02 dirección válida dentro del radio, celda amplia fuera | no exigir cercanía de todo F(U); primer jet/validez puntual sí demostrados | N familia E_r; F proveedor real, ningún mínimo reclamado |
| RF-03 punto con residual flotante pequeño pero sin enclosure fiel | NOT_ESTABLISHED; confirmación humana no sustituye prueba | S contrato; F barrera exactitud/autoridad |
| RF-04 curva no afín admitida / evaluador opaco | primera: oferta si satisface todas las pruebas; segundo: no oferta aunque render idéntico | S capturas v1/v2; F fixtures de ambas |
| RF-05 punto regular probado pero sin mínimo de distancia | modo nuevo puede ofrecer; no cambiar el status R6 ni etiquetar «mínimo» | N/R2 ejemplo afín; F evidencia tipada distinta de candidate R6 |
| RF-06 dos direcciones con celdas solapadas, imágenes iguales o indistinguibles | elegir direcciones demostradas distintas; si pertenencia/equivalencia no se resuelve, rechazo; no ordinal, etiqueta ni posición como identidad | S regla nueva; F self-intersection y canonicalización +0/-0/seam |
| RF-07 singularidad/meseta/componente colapsada o C1 no probado | guarda inicial rechaza; no usar condición mínima de definición para saltar perímetro | S; F cusp, constante, diferencial puntual falso y punto sin entorno suave |
| RF-08 gaps, seams, knot y extremo abierto/cerrado | vía nueva interior únicamente; conservar rutas R6 anteriores válidas | S periodo 0 v1/v2; F ambos lados de fronteras y spline continuo |
| RF-09 barrera observada, agotamiento o prueba que consume remanente | veto nuevo de consulta completa; ningún éxito parcial ni radio ampliado | H agotamiento R1; F transporte íntegro y límites de prueba/captura |
| RF-10 clic L y clic Q libre/dependiente | referencias reales, q leído al seleccionar, Q sin padre nuevo de P | H helper R1; F nuevo flujo sin diálogo e inspección de DAG/XML |
| RF-11 Q antecesor de L | mover Q puede mover P por L, sin arista nueva Q→P ni ciclo | S forward; F caso native I con radio configurado explícitamente |
| RF-12 Q cambia durante confirmación; L cambia A→B→A; Undo/carga | caducidad, recaptura explícita; nunca publicar desde callback antiguo | S revisiones monotónicas publicadas y contexto; F ciclo de vida/reentrada |
| RF-13 clic vacío, Q inválido/ajeno o arrastre mientras se selecciona | no punto libre, no movimiento Q, no Undo; mensaje breve apropiado | S identifica rutas que deben interceptarse; F eventos reales |
| RF-14 Escape, otra herramienta, reiniciar, cambio de documento/vista | destruir oferta/referencias/overlay y callbacks; cero modificación documental | S host initNewMode; F cancelación en cada frontera |
| RF-15 un candidato local sin unicidad global | confirmación contextual obligatoria; selección de Q no basta para publicar | S contrato; F Enter/Escape/foco sin autoselección |
| RF-16 varios candidatos y varios hits de L o Q | selector pequeño de objetos y luego de direcciones, cada uno con handles; nunca diálogo completo | S; F objetos superpuestos, lista larga y navegación accesible |
| RF-17 misma fuente/q/política, cambios de zoom/DPI/monitor | misma consulta semántica y oferta; sólo hit/posición del panel cambian | S política world; F instrumentación de lecturas y rerun determinista |
| RF-18 cambio opcional de radio/política y cambio de documento | visible, invalida consulta, sin autoadaptación ni persistencia ficticia en P | S propuesta .1 heredado; F límites/valores no finitos y sesión |
| RF-19 éxito con selector diferido y cambio inmediato de herramienta | un Undo; P no desaparece como creación inconclusa; fallo no deja inputs/ID/claims | S costura endOfSwitchModeForProcessMode; F fallos antes/después de forward y finalización |
| RF-20 dependencia de P y .cedg/Undo/Redo/copia/remapeo | mismo forward/ID/dirección según codec vigente; Q/política/pruebas ausentes de nueva receta | H persistencia R1; F nueva vía y documentos históricos sin asociaciones |
| RF-21 Punto ordinario por clic sobre SplineV2 y arrastre admitido | comportamiento anterior conservado; creación local nueva no expande drag general | H regresión R1; F equivalencia del flujo anterior |
| RF-22 diálogo avanzado EN/ES, ID extenso, escala de fuente/DPI/resolución | contenido íntegro desplazable, validación anterior, sin pestaña Q obligatoria ni ancho por política | S causa GridLayout/JLabel; F medición real y accesibilidad |
| RF-23 nativo bajo autoridad constructiva adoptada explícitamente | misma política por documento; prueba transferida por ruta aprobada o rechazo; no hacer STRICT por proximidad | N archivo original heredado; F adopción separada y transferencias |

Todos estos casos deben trazarse en el incremento futuro a pruebas shared/Desktop y a los
casos base R2-01..32 pertinentes. No reducir suites existentes para fabricar una PHASE verde.

## 2. Protocolo futuro de smoke autoral

No ejecutado ni aprobado en esta revisión. Usar una copia de trabajo del nativo, conservando
el original y su hash. La versión probada deberá ser el candidato productivo exacto autorizado,
no este commit documental, que conserva el rechazo actual.

1. Abrir TestM08-S.cedg, comprobar h y SplineV2 b; crear con la autoría normal un punto libre
   ReferenceR1=(.8976484384297626,1.7854818601338847). Confirmar que el original no tenía Q.
   No crear ese punto como efecto secundario de la herramienta nueva.
2. Activar «Punto sobre Locus V2 desde referencia». Ver radio .1 u y política accesible, sin
   ventana. Clic h, clic ReferenceR1. Esperar consulta local verificada y confirmación pequeña
   si falta unicidad global; no esperar publicación automática en ese caso. Crear P.
   Si falta prueba del proveedor, esperar rechazo tipado; no declarar smoke satisfactorio
   de la capacidad principal por haber rechazado correctamente el caso objetivo.
3. Inspeccionar definición y padres de P: forma forward, fuente h y dirección elegida, sin
   ReferenceR1 como nuevo padre. Mover ReferenceR1: P no lo sigue. Modificar una entrada de h:
   P se reevalúa conservando la dirección o declara invalidez explícita, sin elegir otra.
4. Repetir con ReferenceDependent=ReferenceR1+(0,0). Mover su antecedente tras crear: mismo
   desacoplamiento. Como caso de Q antecesor, seleccionar I tras configurar explícitamente
   radio 1 u; comprobar que P depende de I por h. Restaurar .1 explícitamente al terminar.
5. Probar fuente con varias direcciones verificadas: ninguna seleccionada inicialmente;
   escoger una y cancelar otra vez. Debe salir un panel pequeño, sin pedir Q/radio/branch.
   Con un único candidato local, comprobar que Enter no crea sin confirmación pendiente.
6. Cancelar después de L, durante consulta y con selector abierto; usar Escape, otra
   herramienta y reinicio. Clic en vacío y arrastre sobre Q no crean ni mueven objetos.
   Reanudar empieza por L. Comprobar contadores de objetos e historial sin cambios.
7. Deshacer una creación exitosa una vez: desaparecen P y sus inputs dedicados nuevos.
   Rehacer una vez restaura dirección/identidad conforme al contrato. Guardar como copia
   nativa .cedg, cerrar y reabrir: P no consulta de nuevo Q ni exige un diálogo de búsqueda.
   Copiar/remapear según el flujo existente; no reasociar por nombres o posiciones.
8. Sobre SplineV2 b, comprobar Punto ordinario por clic y arrastre previos. Probar también
   la herramienta desde referencia; no se exige que P sobre h sea arrastrable en casos que
   el contrato vigente rechaza. Mantener explícita esa limitación independiente.
9. Abrir dirección semántica avanzada para h: rama generator.main, parámetro 0. Comprobar
   ventana compacta, entrada íntegra y validación. Repetir EN/ES, fuente grande, monitores de
   distinta resolución y DPI, IDs largos, teclado/lector accesible. Zoom/DPI no cambian la
   política ni el resultado al repetir la misma consulta; sólo afectan selección/presentación.

Registrar versión/SHA, archivo original/copia, fuente/Q/política exactos, direcciones,
estado tipado, screenshots de presentación y resultado por paso. La comprobación automática
no sustituye este smoke ni su decisión autoral.

## 3. Incremento posterior recomendado y puertas

Prioridad: demostrar la capacidad de creación general antes de mejorar tamaño de ventana.

| Puerta | Trabajo futuro propuesto | Condición objetiva |
|---|---|---|
| RF-G0 | revisión autoral del contrato individual y política propuesta | aprobar evidencia puntual/guarda inicial y textos prospectivos; flujo L→Q ya requerido, no volver a pedir autorización de diseño |
| RF-G1 | puente compartido a captura real y prueba puntual | h/ReferenceR1 en Java real: captura fiel, predicados, componente, autoridad, enclosure, diferencial/guarda, forward y barreras; rechazo adversario sin modelo; no UI nueva aún |
| RF-G2 | resultado/contexto y publicación forward | guardas exactas, vigencia ABA/ciclo de vida, presupuesto y cancelación; ningún residuo/Undo al fallar; no cambios de codec/roles |
| RF-G3 | modo Desktop L→Q y panel contextual | h y SplineV2, elección/confirmación correcta, sin creación Q ni diálogo obligatorio; eventos/reentrada/único Undo demostrados |
| RF-G4 | diálogo avanzado compacto y compatibilidad | conservar autoría explícita, validación, accesibilidad EN/ES y flujos R6 previos; medición real en escalas de UI |
| RF-G5 | candidato productivo integrado y smoke | PHASE por procedimiento canónico y clase INTEGRATED_PHASE aplicable, pruebas integradas concretas, STATIC y comprobaciones requeridas; congelar candidato y smoke autoral separado |

RF-G1 debe preceder RF-G3: una interfaz pequeña no resuelve la admisibilidad de h. Si el
modelo no permite la prueba puntual fiel o aparece una barrera de publicación, detener
esa capacidad y presentar la causa; no habilitar UNRESOLVED ni ampliar arquitectura en secreto.
No fijar aquí una selección PHASE ejecutable ni reescribir infraestructura para acomodarla.

## 4. Deuda y exclusiones preservadas

Se mantiene [DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG](pre_g9b_cu7_i1_r2_matrix_and_gates.md):
investigación posterior independiente, sin implementación/fase/fecha autorizadas y no
bloqueante de M08-S. Un P creado con rol interactivo sólo se arrastra cuando lo admite R6.
No ampliar ahora esa política ni confundir edición de una dirección con seguimiento de Q.

M08-D, M09/A0, I2G/I3 y reconstrucción general excluidos. M09 permanece pendiente; CU10,
integración editorial posterior y DEBT-CU3-EXACT-TANGENCY-GENERALIZATION intactos.
