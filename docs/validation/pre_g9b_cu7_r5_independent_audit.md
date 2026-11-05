# CU7 R5 — auditoría independiente final

2026-11-05, Europe/Madrid. Dictamen técnico: **ACCEPTABLE WITH CONDITIONS**.
No se han encontrado defectos P0/P1/P2 bloqueantes en el delta R4→R5. R5-A,
R5-B y R5-C son aceptables en el perímetro demostrado. Procede preparar el
cierre reducido para decisión del autor, conservando el candidato intacto.
`selfApproved=false`; no aprobación del paquete, adopción normativa ni promoción.

## Autoridad e identidad

Revisión/auditoría de kernel semántico, interacción Desktop, persistencia y
verificación. Fuentes: GitHub vivo consultado primero, AGENTS.md y autoridades
locales del SHA congelado. GitHub main observado: eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17.
El contrato vigente de verificación es ACTIVE RECOVERY CONTRACT, al principio
de verification-levels.md. Las secciones históricas posteriores están archivadas;
no se exige su READINESS/FULL ni se ejecuta su FINALIZE retirado.
ADR0019/0041, M08 e interacción mantienen la enmienda autoral de 2026-11-02.
Roadmap: CU7 global abierto; M08-D y M09/I2, I2G/I3 no se incorporan.
No existe .github/copilot-instructions.md ni AGENTS anidado en el candidato.

R5: `13ee3b8746d4d874824eb44ca1d05a3e57b494c2`.
Árbol: `a4fead2412bbb8859dd167e3c22b82c54e584633`.
Rama: `codex/pre-g9b-cu7-general-point-interaction-r5`.
Base R4 ancestro: `585c536a2986c929b1d64b31ae75967baa9afc9e`.
Worktree: artifacts/cu7-r5-worktree, limpio antes y después de las sondas.
Delta: 24 archivos, 2006 inserciones y 99 eliminaciones; diez clases de producto.
main conserva un archivo no seguido previo, source/desktop/desktop/hs_err_pid25204.log;
no se ha borrado ni atribuido a R5. No se declara main limpio.

La entrega está en artifacts/pre-g9b-cu7-general-point-interaction-r5.
Se verificaron 9286 entradas SHA256 del índice: cero ausentes o discrepantes.
Los cinco informes cumplen esquema e identidad determinista recalculada; sólo FINAL
emite el recibo canónico en este protocolo. Su enlace a informe y candidato es válido.
El adaptador canónico phase-closeout.ps1 -Action INSPECT confirmó identidad sin
ejecutar verificación ni modificar Git. Evidencia nueva: artifacts/cu7-r5-independent-audit.

## R5-A: movimiento

R4 recibía el gesto, pero reutilizaba r_hit como distancia máxima al resultado.
La evidencia original incluye un pequeño movimiento exitoso y un posterior rechazo;
no sustenta que MouseDragged estuviera universalmente ausente.
R5 captura p evaluado y q world y usa r_move=nextUp(hypot(p-q)+r_hit).
El radio de clic permanece separado y el presupuesto no aumenta.

Esta operación floating-point no prueba una cota exacta: nextUp de la suma no
convierte la resta ni hypot en aritmética intervalar. Es admisible porque el radio
es política numérica de búsqueda, declarada sin certificación métrica. Overflow,
NaN e infinito son rechazados por la política finita antes de publicar. La sonda
independiente cubre cero, subnormales, 1e-300, 1, 1e100, 1e307, MAX_VALUE, Inf y NaN.

El kernel de continuación R4 permanece sin cambios: existencia y selector,
componente, rama, orientación, preimagen, costura/lift, aislamiento, recintos y
error paramétrico siguen siendo requisitos del proveedor. Las cartas prueban
g=(F-q)·F' y g_u=||F'||²+(F-q)·F'' con signo y cota positiva sobre cada tramo,
cuando esa ruta requiere C2; otras rutas no adquieren ese requisito. Un radio
grande no elimina un fallo de carta, singularidad o agotamiento. La mutación final
y el DAG conservan el protocolo atómico; el gesto conserva fuente, dirección y
generaciones hasta release/Undo. Los catch cancelan referencias obsoletas tras
reconstrucción de objetos, sin buscar sustitutos por coordenadas.

GUI02 demuestra P existente y reabierto, dependiente, Undo/Redo y puntos nuevos.
La suite R5 prueba radio anterior rechazado frente a carta admisible, movimientos
inversos, punto fijo dinámico y rechazo sin mutación por presupuesto/certificación.
Conclusión: corregido, con los límites científicos y computacionales heredados.

## R5-B: clic, Q y permiso forward

La oferta de parábola podía quedar oculta con Entrada desactivada; el mensaje de
unicidad global no establecida describía una oferta local admisible. Además el
radio de clic contaminaba la política Q y el despacho omitía el permiso individual
cuando R6 terminaba por LOCAL_EVIDENCE_INSUFFICIENT.

R5 monta el panel aunque Entrada esté oculta, restaura la política Q y separa
discoverForForwardAddress de los candidatos R6. La semilla no recibe STRICT ni
evidencia de mínimo. Su consumidor exige reconstrucción v3 fiel, autoridad
compatible, COMPLETE, componente/selector resueltos, canonicidad, evaluación
forward válida y proximidad FLOATING_POINT_UNCERTIFIED. Observaciones inválidas,
singulares, no finitas y agotamiento son barreras. R6/H1 no se reetiquetan.
La selección es explícita y de un handle efímero; el ordinal no se persiste.
Los intentos históricos y H1 descuentan trabajo antes de la alternativa forward.

Sonda independiente de círculo de escala pequeña: límites 1/8/32/128 rechazan
consumiendo exactamente su techo; 512/2048/8192 admiten la ruta individual con
315 evaluaciones acumuladas. Un fallo inyectado en el segundo guard, después
de construir, restaura XML completo y elimina el punto parcial. Una elección
ajena y Q cambiado no publican. Q no pasa a ser padre de la dirección creada.
Persisten las comprobaciones de revisión, autoridad, feature policy y fuente.
F8 no se activa: sigue faltando el permiso sobre inventario no COMPLETE.
Conclusión: corregido sin relajar R6/H1 ni convertir proximidad en certificado.

## R5-C y clases heredadas

La acción contextual llama al mismo flujo Q de Punto. Gráfica y Álgebra ofrecen
objetos reales; el gesto completo de Álgebra se consume para impedir redefinición
o doble publicación. El panel no fuerza la preferencia de Entrada del documento.
La colocación nativa usa AlgoDispatcher.point y AlgoPointOnPath con un único
padre conductor; no usa el solver V2 ni impone sus mínimos/completitud. Q puede
estar lejos: el límite world de descubrimiento V2 no restringe esa proyección
nativa. Las coordenadas y membresía finales se verifican; fixed impide edición,
manteniendo el algoritmo y la dependencia del conductor.

Tabla positiva de la modalidad Q: GeoLine, GeoRay, GeoSegment,
GeoConic y GeoConicPart 2D. NativePathState2D captura además GeoAxis para usos
internos; esto no concede acceso Q sobre ejes. La interfaz excluye ejes, 3D, figuras de cotas y tipos
fuera de tabla, incluidas funciones/implícitas, polilíneas y loci heredados.
Una clase Path arbitraria no adquiere permiso. Nuestras sondas verifican Q lejano
en recta, rayo, elipse, hipérbola y arco; funciones y polilíneas no son admitidas.
Otra sonda hace caducar el contexto durante la creación nativa y verifica rollback
del XML completo. La afirmación de independencia de Q excluye dependencias
previas legítimas si Q ya era ancestro del conductor.

Único cambio heredado de producto en R5: cinco líneas en DockManagerD. La guarda
rechaza paneles ajenos antes de callbacks/generación y no sustituye identidad
geométrica por pertenencia UI. Dos aplicaciones conservan sus controladores;
paneles propios siguen el docking existente, null y EV1→EV2→EV1 invalidan.
Sonda independiente: panel extranjero y panel retirado no alteran ticket;
reincorporación de Álgebra conserva la vista; null/retorno invalida la oferta.
La generación saturada se rechaza; pan/zoom no cambia la generación.
No se afirma una nueva campaña física de todos los modos de docking.

Construction, GeoPoint, AlgoPointOnPath, XML, Undo y restauradores no cambian
frente a R4. Se revisaron sus puntos de integración, tabla FILE/MEMORY, gramática
y fingerprint; las regresiones heredadas están efectivamente en los productores
canónicos. No se reabre su investigación general sin evidencia contradictoria.

## Evidencia gráfica y persistencia

Se revisaron los registros originales del implementador, no se ejecutó otro
smoke físico del auditor. GUI02 tiene 328 eventos, 439 imágenes verificadas y
21 documentos guardados. Las imágenes JPEG conservan sus nombres .png históricos.
El lanzamiento before-java-launch.json identifica 13ee/a4fead limpio; la campaña
canónica posterior pertenece al mismo SHA. Los eventos rápidos fallidos y los
arrastres Win32 sostenidos se conservan; tiempo del emisor no equivale a EDT.

Inspección visual directa: 008/009 muestran P de (0.4,0.16) a aproximadamente
(0.59,0.35) y Dependent de (2.4,3.16) a (2.59,3.35); 026 muestra oferta con
Entrada oculta; 029 muestra A arrastrado; 086 oferta nativa desde Álgebra;
123 movimiento tras reapertura; 156 retorno a la ventana nativa con H editable.
El análisis independiente de los 21 ZIP confirma XML legible y relaciones
Dependent=P+(2,3) binary64 en los documentos correspondientes. La correspondencia
entre archivos usa geocedgId; etiquetas sólo siguen las aristas serializadas
dentro de un archivo. Los selectores, rama, proveedor y fuente se conservan.
El parámetro raw coincide con canónico+lift*2π; se verificó la distinción en MEET.
ZIP no prueba por sí solo toda la historia Undo ni un certificado geométrico.

GUI-1, GUI-2, GUI-3 y GUI-4 están sustentadas por la evidencia del implementador,
con sus límites: no hay Undo individual por cada similitud ni smoke de cada
familia nativa. El autor añade evidencia independiente: SMOKE PASS — AUTHOR
REPORTED sobre R5, comunicación de 2026-11-05, tras corregir una ejecución inicial
desde main. No comunicó defectos en el smoke correcto ni una lista de pasos.

## Usabilidad y hallazgos

P3-U: solapamiento organizativo entre el flujo Q de Punto, la acción contextual
y la entrada especializada de Semantic Curve. Reproducción por código y smoke:
una circunferencia puede recibir Q por cualquiera de esos accesos. Comparten el
backend, pero el modo especializado inicia L→Q y ofrece Posición de clic con
política explícita; Punto conserva el clic ordinario y su radio de detección.
Punto en objeto conserva la intención nativa de restringir la creación a objeto.
La dirección semántica manual (MODE_LOCUS_V2_POINT) es otra capacidad y no debe
eliminarse como si fuera un duplicado de Q. La agrupación especializada tiene
sentido de descubrimiento en el workspace CeDG; no cambia autoridad geométrica.

Recomendación **U2**: mantener R5 intacto. Futuro diseño: Punto principal con
clic y Q opcional, misma selección contextual y despacho tipado, conservando
acceso avanzado explícito y compatibilidad de acciones. No hay contradicción
funcional que justifique U1 ni otra validación general del candidato actual.

P3 heredado: un presupuesto finito puede rechazar regiones regulares cerca de
pliegues o composiciones; no es imposibilidad matemática ni movilidad universal.
Se conserva el detalle de la auditoría R4, sin afirmar una nueva reproducción.
No hay nuevos hallazgos bloqueantes. Fuera de alcance: portabilidad bit a bit
no ensayada, lectores antiguos, toda la variedad de Path y F8.

## Verificación

| Perfil congelado | Aceptación/cobertura | Obligaciones | Diagnósticos clear/finding/unavailable |
| --- | --- | ---: | --- |
| STATIC | ACCEPTED / COMPLETE | 3 | 4/1/1 |
| INFRA_UNIT | ACCEPTED / COMPLETE | 22 | 0/0/0 |
| PHASE R5 | ACCEPTED / COMPLETE | 10 | 0/0/0 |
| INTEGRATION | ACCEPTED / COMPLETE | 19 | 7/2/1 |
| FINAL | ACCEPTED / COMPLETE | 41 | 7/2/1 |

PHASE: 728 casos, 0 fallos/errores/omisiones; incluye los 25 casos R5 y 9 P4.
INTEGRATION y FINAL: cada uno 10140 casos de aceptación, 11 skips históricos,
0 fallos/errores de aceptación. Separadamente cada campaña conserva un caso
diagnóstico Revision3 fallido por fixture ausente; total físico 10141.
Gobernanza recovery y unavailable históricos permanecen visibles, no PASS.
Se verificaron filtros efectivos, la adición de R5 a R4 sin exclusiones nuevas,
conteos JUnit y hashes. El manifiesto upstream añade procedencia histórica;
no equivale a 51 cambios productivos nuevos. No se alteran verificadores o tests.

Recibo FINAL: aa8d0c33de7dd092b77f1d2ac5a9a8477ef910cdaa1e0d362fd4b230ce310222.
Hash determinista del informe: 0be86bbd60695d577a72f54e29dfce54ed659f0ada434039c8b4dd321c03fb4e.

Sondas nuevas: run-probes.ps1 -Module desktop -RunName desktop-02, productor
canónico gradle-command-evidence-producer.ps1, inner exit 0, 7/7 JUnit sin skips.
desktop-01 falló antes de compilar por permisos de caché Gradle; se conserva,
clasificado como entorno. Reejecución con escalación administrada, sin cambio
de producto. El primer lector independiente asumía recibos en todos los perfiles;
se conserva ese intento y se corrige el lector: el protocolo sólo los emite en FINAL.
No se atribuyen fallos de preparación al candidato. No se repitió FINAL.

## D1, F8 y decisión

D1: **ADOPTAR CON CONDICIONES**. Se mantiene validación estricta del campo,
emisión sólo con fidelidad transitiva FILE, fallback heredado cuando no se prueba,
proyección de fingerprint limitada a líneas reconocidas y validadas, resto del XML
sin normalizar, Classic estricto y macros fuera de la nueva emisión. La igualdad
de bytes del corpus no promete portabilidad universal ni conservación por lectores
antiguos. MEMORY y Undo tienen correspondencia privada tipada y garantías propias.
La propuesta normativa se prepara separadamente, sin cambiar el contrato vigente.

F8 / LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION permanece diferido y desactivado.
La evidencia nueva no obliga a activarlo. Puede estudiar en el futuro admisión
local con identidad suficiente aunque completitud global siga no establecida;
no se convierte cobertura numérica en completitud.

Condiciones de cierre: alcance reducido explícito; conservación de las exclusiones
y límites; decisión autoral sobre D1, U2 y cierre; autorización separada de promoción
en el mismo paquete decisorio. No se requiere otro smoke o FINAL sobre R5 intacto.
La fase documental posterior no representa aceptación de una implementación nueva.
