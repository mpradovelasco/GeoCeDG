# CU7-I1-M08-S-R1 — diagnóstico y corrección acotada

Estado: candidato pendiente de revisión y smoke autoral. El smoke de 5bc7d7dac NO es PASS.
Base exacta: 5bc7d7dac1c2466bfaecbcacaa04d8e5386ced63, árbol
733387ac71f1418cadaca3bf352c36033426c701, worktree limpio al iniciar.
Rama correctiva codex/pre-g9b-cu7-i1-m08-s-r1; original y main conservados.
Clasificación: diagnóstico primero; corrección Desktop/diagnósticos después, sin cambiar
admisibilidad, geometría, identidad ni persistencia. .github/copilot-instructions.md no existe.

## Caracterización focal antes de cambios productivos

Entrada autoral C:/Temp/TestM08-S.cedg, 28228 bytes, SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d.
Original solo leído; copia preservada en artifacts/pre-g9b-cu7-i1-r1/author-input/.
No se incorpora el documento privado al producto ni al libro.

Definición de h: I=(0.4965866445356883,1.7854818601338847),
J=(-0.5082406249102692,2.5763181370126467); f=Circle(I,J); L=Point(f);
g=Line(L,xAxis); K=Intersect(g,yAxis); M=Midpoint(K,L); h=LocusV2(M,L).
La sonda carga el archivo NATIVO con AppGeoCeDG.loadFile y crea ReferenceR1 cerca de
F_h(0), no reasocia por coordenadas. Q=(0.8976484384297626,1.7854818601338847).

Resultados Java reales idénticos en 5bc7d7dac y main anterior caf67ffc:

| Dimensión | Resultado observado |
|---|---|
| Identidad de h | geo:aa7268331ad4d3a3d1cc946e989b109b (leída del registro) |
| Proveedor / evaluador | circle-point/v1 / ReconstructibleLocusEvaluator2D |
| Estado / dominio | VALID, definido; [-pi,pi) |
| Rama / componente | generator.main / generator.main/component/periodic-closure |
| Estructura de componentes | COMPLETE; componente periódico certificado |
| Política | initial(0.1): radio world 0.1, equalDistanceTolerance 1e-10, parameterTolerance 1e-12; 32768 evaluaciones, 512 subdivisiones, 80 refinamientos, 1024 candidatos |
| Hit gráfico | DrawLocusV2.hit positivo en F_h(0) |
| Consulta | UNRESOLVED_NUMERICAL_SEARCH; BOUNDED_EVALUATOR_SEARCH |
| Candidato | uno, u=-6.407985253531479e-11; distancia 0.010000000000000009; celda [-0.0654498469507665,0.06544984694876632] |
| Evidencia local | ESTABLISHED: mínimo regular estricto en celda acotada; FLOATING_POINT_UNCERTIFIED |
| Diagnóstico | One local candidate was established, but bounded semantic search did not establish global uniqueness |
| Creación por clic | null, XML sin mutación |
| Forward explícito | Point(h,"generator.main",0) definido: (0.8876484384297626,1.7854818601338847) |

Es limitación HEREDADA, no regresión introducida por M08-S. Se ejecutaron las mismas
sondas con HEAD separado en caf67ffc (sin mover main), y se retornó a la rama correctiva.
La clase del resolvedor es idéntica en ambos commits. La creación arrastrable de SplineV2
también precede a M08-S; el candidato previo no aportó la autoría Q explícita requerida.

No confundir la completitud de componentes con la completitud de la búsqueda inversa.
Ni hit ni residual prueban unicidad. UNRESOLVED aquí NO significa ausencia, invalidez,
degeneración o presupuesto agotado. Otra sonda con presupuesto 1 reproduce agotamiento:
UNRESOLVED, NOT_APPLICABLE, diagnóstico de límite; el mismo enum histórico cubre causas distintas.

Evidencias bajo artifacts/pre-g9b-cu7-i1-r1/: diagnostic-current (2 tests, exit hijo 0),
diagnostic-author-current-2 (1 test nativo, exit hijo 0), diagnostic-main (3 tests, exit hijo 0).
JUnit incluye trazas de proveedor, dominio, componentes, candidatos y contadores.
diagnostic-author-current conserva un intento con NO TESTS FOUND, exit hijo 1: configuración
del arnés errónea al confundir path del proyecto con ruta del build compuesto; no defecto del producto.
Las sondas se ejecutan mediante tools/agent/checks/gradle-command-evidence-producer.ps1,
con --tests e init script que habilita XML JUnit. El exit exterior 0 no sustituye al inner_exit_code.

## Puerta normativa y alcance de corrección

ADR 0019 decisión 4 y M08 aprobado §2 pasos 2/4 preservan UNRESOLVED para un único
candidato de búsqueda general y prohíben ofrecerlo. El resultado actual del autor requiere
una decisión normativa para ser ofertable a partir de Q. No se cambiará esa política.
Una enmienda candidata separada debe distinguir admisibilidad local, cobertura y permiso
de selección; ni confirmación ni un nuevo botón pueden convertir el rechazo actual en éxito.

Diseño mínimo independiente para las fuentes YA admitidas: en la herramienta existente
Punto Locus V2, seleccionar L, abrir una opción separada de referencia existente Q y política
world explícita; conservar la autoría de rama/parámetro existente como alternativa.
Q libre o dependiente se lee como valor al confirmar; no se añade al DAG ni a la expresión
guardada. Usar LocusSnapshotPlacement2D y construcción forward actual, con elección exacta
del candidato y guardas de vigencia. Cancelación antes de commit no muta; el host conserva
su único Undo. No nuevo comando dinámico P=Operacion(L,Q), codec, rol o formato.
Las revisiones/identidad, limpieza de inputs fallidos y publicación forward existentes se
reutilizan; no se afirma una transacción general de reconstrucción M09.

Antes de publicar un candidato productivo se deberán demostrar Q libre/dependiente,
desacoplamiento de Q, actualización de L, ambigüedad/cancelación/vigencia, Undo/Redo,
reapertura nativa y copia/compatibilidad. Mensajes distinguirán creación de movimiento,
preservando los estados y refinando solo diagnósticos, sin cambiar permisos de admisión.

## Verificación y compromisos

Si hay corrección productiva: INTEGRATED_PHASE con PHASE registrada más INTEGRATION,
STATIC e INFRA_UNIT cuando corresponda a catálogo. Los focos previos no son aceptación.
BOOTSTRAP IMPACT: NO CHANGE REQUIRED; ningún runtime, instalación o prerequisite cambia.
GUIDE_IMPACT: se deberá explicar Q explícito y el rechazo del caso general; CU10 conserva
la integración editorial posterior. No ejecutar CU10 ni cambiar geocedg_book.
M08-D, M09/A0, I2G/I3 y reconstrucción general excluidos. Deuda exacta de tangencias intacta.
No publicación, main, etiquetas, releases o paquetes. Detener la ampliación de admisibilidad
para revisión autoral; no declararla implementada ni aprobar unilateralmente el candidato.

## Cambios independientes congelados y evidencia de fallo

- Diagnóstico y propuesta normativa: 48f26a438eeceae35eff62e9945faf09ff56554d.
- Mensajes/causas: 8f24c0379929050caec70a445c3e7776ed8c0291. Reason es diagnóstico
  efímero del resultado; status, candidatos, cobertura, matemática y permisos no cambian.
  Se separan creación/edición/movimiento y ausencia certificada, falta de unicidad global,
  evidencia local insuficiente, presupuesto, fuente inválida, degeneración, selección
  caducada/ajena y cancelación. EN/ES en el perfil vivo. Cancelar no abre un error modal.
- Funcional: 9159f6d8fe67494fde2f6463a1ee2609d6c35a71, árbol
  cd9bcc8d197b002b73481feb388df1e74857f89a. Pestaña Q explícito dentro de la herramienta
  Punto Locus V2, separada de la dirección manual y de clic con Punto. L se selecciona
  gráficamente; Q por elección explícita de un objeto existente, no por asociación de etiqueta.
  Política R6 v1 visible y radio world confirmado; solo se pasan coordenadas copiadas al kernel.
  No comando dinámico, codec, rol o formato nuevo. La referencia tampoco se promueve a durable.

authoring-focus conservó un fallo real de vigencia: tras recargar en el chooser,
getSemanticDefinition sobre la instancia retirada lanzaba IllegalStateException antes de
la guarda de registro. Se adelantó la comprobación de identidad/registro en Selection.commit;
ahora rechaza CU7_STALE_SELECTION sin acceder a esa definición ni publicar. No se amplía
admisibilidad ni se implementa generación universal de mutaciones.
authoring-focus-2: 31 tests reales, 0 fallos/errores/omisiones, exit hijo 0.
authoring-focus-3 repite 31/31, sin omisiones, con guarda al entrar a query sobre fuente
retirada y comprobación de que Q libre/dependiente no adquiere ID durable por ser referencia.
El endurecimiento y los controles finales están en b7c72ca729e0788e9366f99706df7a560a5952ab.
No cambian las identidades JUnit ni las reglas de admisión.
El arnés nativo está preservado fuera del descubrimiento normal en
tools/agent/probes/cu7-m08s-r1/; el init script focal añade ese directorio solo al proyecto
source/desktop/desktop. No hay marcadores ni diagnósticos nuevos en .cedg/XML.

**Control admitido sobre el archivo NATIVO del autor.** native-authoring, un test real,
exit hijo 0: ReferenceMultipleR1=(0.24829332226784415,1.8854818601338847), radio 1.
MULTIPLE_SEMANTIC_PREIMAGES / BOUNDED_EVALUATOR_SEARCH; dos candidatos ESTABLISHED,
FLOATING_POINT_UNCERTIFIED, u=0.10446160881669275 y 3.037131031944786, distancia
0.6367429867922275. Elección explícita crea P, sin Q en sus antecedentes; mover Q no lo
mueve; guardar/reabrir .cedg conserva ID y dirección. Es el permiso múltiple YA existente,
no la excepción normativa del candidato único. No se excluye una familia general entera.
El contrato geométrico es preexistente; R1 añade la superficie de usuario con Q existente
y política explícita, que no estaba presente ni en main ni en el candidato 5bc7d7dac.
native-authoring-edit: otro test real, exit hijo 0, confirma que el intento posterior de
arrastre de ese P general devuelve UNRESOLVED/GLOBAL_UNIQUENESS_NOT_ESTABLISHED y conserva
la dirección. No se afirma arrastre general disponible: es otra limitación R6 heredada.
Su ampliación local necesita alcance/pruebas y aprobación normativa propios, sin M08-D.
El replay de comparación main usa solo Cu7M08SNativeAuthorDiagnosticTest.java de la copia
aislada native-probe-src (o del commit diagnóstico 48f26a438); el probe nuevo de autoría
usa APIs R1 y no debe añadirse al compilador de baseline. El init y argv efectivos quedan
guardados en cada directorio de ejecución; no copiar todo tools/probes al baseline.

## Matriz R1

| Requisito | Evidencia focal / puerta |
|---|---|
| h autoral, proveedor, componentes, hit, local/global y forward | sonda nativa 5bc7d7dac y caf67ffc, XML JUnit con trazas idénticas |
| Agotamiento distinto de ausencia | exhaustedBudgetIsNotCertifiedAbsence; causa BUDGET_EXHAUSTED |
| EN/ES, creación/edición/movimiento | creationAndMovementHaveDifferentLocalizedMessages |
| Q libre/dependiente, sin dependencia ni movimiento ulterior | freeAndDependentReferencesAreConsumedAsValues |
| Cambios L y colapso/recuperación | sourceChangesPropagateButReferenceChangesDoNot; regresión R6 |
| Q antecedente L / DAG sin arista ficticia | referenceAncestorOfSourceKeepsOnlyItsLegitimateInfluence |
| Múltiple, cancelación y caducidad tras recarga | multipleChoiceCancelAndStaleSelectionDoNotPublish, igualdad XML tras rechazo |
| h general por Q sigue rechazado | generalLocusFromQStillRejectsWithoutNewPolicy |
| Varios locales en h, sin completitud inversa global | mismo test con QC/radio 1 y sonda native-authoring sobre archivo original: selección y .cedg válidos |
| Identidad, .cedg y copia/remapeo sin Q | referenceSnapshotNativeReopenAndClipboardUseForwardOnly |
| Superficie Desktop real, un Undo/Redo, cancelación sin mutación | explicitReferenceDialogCreatesOneUndoAndCancellationCreatesNone |
| No regresión clic/arrastre/SplineV2 y lifecycle | G9U1SemanticPointInteractionTest: 20/20; shared R6 52 y regresión INTEGRATION |
| Documentos antiguos, política, registros y codecs | regresión integrada; no formato/migración nuevo |

## Verificación del candidato final

VERIFICATION_CLASS=INTEGRATED_PHASE, frozenAtPhaseStart=true. PHASE
PRE-G9B-CU7-I1-M08-S-R1: compilaciones, shared R6 52, Desktop R1 31 y aislados 5.
INTEGRATION añade la regresión global de consumidores, guías/perfil, constructores, DAG,
registros, autoridades y codecs que el foco no cubre. STATIC e INFRA_UNIT por el catálogo.
No FINAL, recibo FINAL ni closeout autoral. El verificador vigente emite informes para
estos perfiles; solo FINAL puede emitir verification-receipt.json.

Procedimiento: Read-VerificationJUnitFiles/función canónica derivan la nueva selección
desde XML focal ejecutado (31 identidades); productor oficial la ejecuta; discovery shared
y Desktop con --test-dry-run; ejecución global Desktop para derivar su catálogo con
update-verification-junit-inventory.ps1. Nunca identidades o hashes supuestos.
Registro: 72 selecciones de catálogo / 74 PHASE; fixture de cantidades actualizado,
sin cambio del runner, proyecciones, reglas de aceptación ni allowlists de omisiones.
La preparación inicial PlanOnly rechazó JSON por concatenación incorrecta de un grupo
de regex al reencuadernar el hash; se corrigió el campo y el plan quedó COMPLETE. No fue
fallo del kernel ni campaña de aceptación. Se preserva en la trazabilidad de esta tarea.
La derivación Desktop ejecutada termina con 2276 tests, 0 fallos/errores, una omisión
preexistente, exit hijo 0. Las pruebas puras de catálogo: 8 casos/582 aserciones, 0 fallos.
Una comprobación de salida nativa nula detuvo antes de tiempo el primer encuadernado de
hash; se detectó el hash pendiente, se encuadernó y se verificó de nuevo. Ningún artefacto
fallido se presenta como aceptación. Los probes privados quedan fuera del discovery normal.

Resultados reales del commit final y sus SHA/árbol constan en
artifacts/pre-g9b-cu7-i1-r1/final/{static,infra_unit,phase,integration}/verification-result.json.
Las ejecuciones de preparación son diagnósticas y se mantienen separadas de esos informes.
No incrustar un hash autorreferencial ni declarar aprobado el candidato por los focos.
VERIFICATION_INFRASTRUCTURE_IMPACT=BOUNDED_CATALOG_REGISTRATION; BOOTSTRAP IMPACT=NO CHANGE
REQUIRED; GUIDE_IMPACT=UPDATED (EN/ES dentro de §8.1, sin cambiar índice ni jerarquía).

## Smoke autoral revisado — NO EJECUTADO POR EL AGENTE

1. Abrir el original C:/Temp/TestM08-S.cedg sin sobrescribirlo. Activar Locus V2.
   Crear ReferenceR1=(0.8976484384297626,1.7854818601338847).
2. Activar Punto Locus V2, seleccionar h y abrir Desde Q existente (referencia inicial).
   Seleccionar ReferenceR1, radio 0.1; revisar la política R6 v1 y confirmar. Esperado:
   NO se crea P; mensaje de candidato local verificado/unicidad no establecida. No debe
   afirmar ausencia, presupuesto agotado ni que un punto previo no cambió. Esta es prueba
   de rechazo correcto y capacidad PENDIENTE; nunca éxito funcional M08-S general.
3. Como control preexistente, usar dirección manual generator.main, parámetro 0, o
   Point(h,"generator.main",0): punto definido. No presentarlo como creación desde Q.
   Después crear ReferenceMultipleR1=(0.24829332226784415,1.8854818601338847): seleccionar
   h con Punto Locus V2, pestaña Q, elegir esa referencia y radio 1. Deben ofrecerse las dos
   direcciones locales anteriores. Cancelar no crea P. Repetir y elegir explícitamente una:
   crea P bajo las reglas R6 existentes, sin afirmar unicidad ni mínimo global.
   Mover ReferenceMultipleR1 no mueve P; guardar con otro nombre .cedg y reabrir lo conserva.
   Intentar arrastrar ese P general: se espera rechazo R6 por unicidad no establecida,
   con mensaje de movimiento y sin cambiar su dirección. Es limitación pendiente,
   NO smoke PASS de arrastre general. El control SplineV2 siguiente sí exige arrastre.
4. En documento nuevo, crear deslizador z (inicial 0, [-1,1]), A=(-2,z), B=(-2/3,z),
   C=(2/3,z), D=(2,z), S=SplineV2({A,B,C,D},3), Q=(0.3,0.02).
   Punto Locus V2 → S → pestaña Q → elegir Q → radio 0.1 → confirmar. Debe crear P
   arrastrable. Se distingue del clic directo con Punto, que sigue funcionando como antes.
5. Inmediatamente Undo quita solo P y Redo lo restaura. Mover Q lejos deja P en su dirección;
   cambiar z mueve S y P. Inspeccionar padres: S y dirección explícita, sin Q directo.
   Crear R=Q+(0.4,0) cerca de S y repetir desde R: mover Q/R posteriormente no lo sigue.
6. Probar Cross=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3), QC=(0,1).
   Seleccionar Cross, pestaña Q, elegir QC. Elegir una dirección explícita entre las varias
   ofrecidas; cancelar no crea P ni añade Undo. Repetir, confirmar y comprobar arrastre.
7. Guardar con otro nombre .cedg, cerrar y reabrir: misma dirección/definición; mover Q
   sigue sin afectar P. Copiar P remapea su cierre/source e identidad; Q no se copia como
   padre ficticio. Abrir un documento antiguo sin asociaciones: ninguna migración automática.
8. Si Q ya define L, conservar su influencia a través de L: no exigir que desaparezca del
   cierre transitivo. Sin nueva arista directa Q→P. Si cambia L/documento durante el chooser,
   rechazar la selección caducada sin crear P. Probar cancelar también el diálogo inicial.

## Resultado funcional y decisión reservada

R1 añade autoría explícita con Q existente para consultas ya admitidas, incluidas consultas
generales múltiples, y corrige mensajes
y la guarda de recarga. El candidato anterior solo reforzaba el consumo de la selección;
clic/arrastre SplineV2 ya existían. El caso h/ReferenceR1 de candidato único permanece pendiente: requiere
aprobar específicamente la enmienda candidata de admisibilidad local. No se aplica ni se
inicia automáticamente. M09/A0 y demás exclusiones mantienen su estado anterior.
