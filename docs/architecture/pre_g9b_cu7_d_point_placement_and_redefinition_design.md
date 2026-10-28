# PRE-G9B-CU7-D — Diseño de M08 y M09

| Campo | Valor |
|---|---|
| Clasificación | planificación/diseño normativo y arquitectónico, con documentación candidata y sonda no productiva |
| Estado | **ARCHITECTURAL DIRECTION AUTHOR APPROVED WITH PRECISIONS** (2026-10-29); conciliación normativa candidata pendiente de revisión; CU7-I sin autorización; `selfApproved=false` |
| Autorización | instrucción autoral «PRE-G9B-CU7-D — Autorización autoral de diseño de M08 y M09», 2026-10-28; solo CU7-D |
| Base comprobada | `main` = `origin/main` = `72f1c069586f3cf53f26428d31bfd1cbc7014b43`; árbol `7e2d8a45594bad9f08eff52c41b51c49929379d9`; limpio al entrar |
| Rama | `phase/pre-g9b-cu7-d-design`, local; worktree principal `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG` |
| Capa afectada por las propuestas | semántica y ciclo de vida del kernel Java compartido; adaptadores de interfaz como consumidores |
| Fuentes de verdad actuales | código/contratos vigentes y decisiones autorales aceptadas; los textos nuevos son candidatos |
| Producto, libro y publicación | ningún cambio productivo; sin CU7-I, otras fases, libro, push, etiqueta o promoción |

## 1. Recomendación

Separar M08 en dos operaciones explícitas: colocación que fija una dirección semántica y
correspondencia dinámica con una definición matemática persistente. Reutilizar el punto
semántico y el resolvedor R6 para la primera; exigir una capacidad nueva, acotada y certificable
para la segunda. `Point(L,D)` de CU3 ya cubre su propia correspondencia exacta y no se sustituye
por un inversor por distancia.

Para M09, ampliar únicamente una actualización explícita y transaccional del contrato de un
punto compatible. Conservar la identidad del objeto P no conserva ni transporta la identidad
de una raíz de otro resultado. No exigir coincidencia, conforme a DA-5. Mantener el reemplazo
real como elección independiente con identidad nueva e impacto veraz. No introducir remapeo
parcial automático. Tratar las salidas múltiples y el cambio de soporte como puertas propias.

El autor aprobó el diseño con cinco precisiones y sujeto a conciliación el 2026-10-29; el texto normativo consolidado sigue pendiente de revisión. Véase el [registro de conciliación](../validation/pre_g9b_cu7_d_consolidated_candidate.md). La [hoja de decisiones](../validation/pre_g9b_cu7_d_author_decision_sheet.md)
reserva al autor el perímetro de CU7-I, especialmente la proyección dinámica y la separación
de una salida de una intersección ordinaria de varias salidas.

## 2. Base, autoridades y dependencias normativas

La referencia remota se consultó mediante GitHub, `GET /repos/mpradovelasco/GeoCeDG/git/ref/heads/main`:
su SHA coincide con el local. `git ls-remote` no resolvió DNS dentro del sandbox; no se usó ese
fallo como indicio de divergencia. CU3 producto `2d13ad4c9b61d6f443b691b8214a8b2361ad0451`
es antecesor del HEAD. Desde el cierre `c75622e5c`, la base solo añade reconciliación del roadmap
(`7b6f0e847`) y la precisión de `AGENTS.md` (`72f1c0695`). No hay divergencia inesperada.

El registro global y su espejo confirman `CU3 PASS — AUTHOR APPROVED`; CU3-A, B1 y B2
están aprobados en sus registros respectivos. Las referencias del cierre a un main anterior
describen el instante del cierre; no se reescriben como evidencia de la promoción posterior.
`.github/copilot-instructions.md` no existe en esta base. Solo se encontró el `AGENTS.md` raíz;
no hay instrucciones locales adicionales. `CLAUDE.md` no se modifica.

| Dependencia | Autoridad y efecto en CU7-D |
|---|---|
| Orden de fases | roadmap 5.100 de la base y plan consolidado §4–§7: CU3 → CU7-D → CU7-I; aprobación propia del diseño |
| M08 y DA-4 | plan candidato §B.2, ficha M08 y DA-4: antecedente de alternativas, no especificación aprobada |
| M09 y DA-5 | plan consolidado §2 y diseño CU0 parte 1: retirado el requisito general de coincidencia |
| Dirección semántica | ADR 0019; point-interaction §§2–14, 17–18; public-surface §§6, 13, 20–21 |
| Existencia/componente | semántica Locus V2 y existencia §§15–17; ADR 0027/0038 por referencia de los contratos vigentes |
| Intersecciones | ADR 0017 y ADR 0039: selector completo, admisibilidad local, germen F/T y barreras; no identidad por lista |
| Autoridad geométrica | ADR 0040 y constructive-authority: política explícita por documento, integridad independiente de garantía numérica |
| Redefinición | ADR 0026 y public-surface §20: evaluación no mutante, roles completos, P3-R1, modos explícitos y rollback |
| Persistencia/remapeo | ADR 0011; durable-dependency-projection §§8–10, 14–15: contrato versionado, frontera durable, copia de cierre completo |
| CU3 aprobado | registros A/B1/B2/global y dossier global: evidencia y limitaciones; no certificación exacta generalizada |
| Verificación | verification-levels «Current contract», registro tipado y verify.ps1: STATIC para CU7-D; DEV incompleto en la base |

La cadena es normativa, no solo cronológica: M08 consume la dirección R6 sobre componentes
CU2; la correspondencia estructural y los contactos de CU3 fijan qué se puede afirmar sobre
incidencia; M09 consume esa admisibilidad y el sustrato G9A3, sin alterar sus tokens por la
conveniencia de una interfaz. Las propuestas de CU0 y del plan candidato no amplían esos
contratos automáticamente.

## 3. Hechos actuales del código

Los enlaces siguientes apuntan a archivos existentes inspeccionados. Los nombres de §5–§7
que se describen como propuestos no son APIs existentes.

| Hecho | Evidencia de fuente en la base |
|---|---|
| La consulta inversa acepta coordenadas transitorias y opcionalmente una dirección actual como restricción | `LocusPointInteractionQuery2D` y `LocusPointInteractionResolver2D.resolve` |
| El resultado impide declarar none/unique sin cobertura completa del ámbito; evaluador general con un candidato sigue unresolved | `LocusPointInteractionResult2D.SearchCoverage`, constructor y `Resolver.classify` |
| La búsqueda afín y la polinómica tienen evidencia distinta de la búsqueda general; no equivalen a prueba exacta | `Resolver.affineSearch`, `polynomialSearch`, point-interaction §§5–6 |
| Existe creación interactiva y arrastre Desktop con elección explícita de candidatos múltiples | `GeoCeDGPointInteraction.create/move`; `G9U1SemanticPointInteractionTest` |
| El punto ordinario tiene padres reales L, texto de dirección y valor numérico; conserva selector y binding actual por separado | `AlgoSemanticLocusPoint2D.setInputOutput/compute`, `LocusSemanticAddressState2D` |
| Crear/mover un punto interactivo no instala dependencia dinámica sobre otro Q | `LocusV2PublicOperations.createInteractiveSemanticPoint/moveInteractiveSemanticPoint` |
| CU3 tiene una familia propia de correspondencia, no interacción inversa | `AlgoLocusDriverCorrespondencePoint2D`, `createDriverCorrespondencePoint`; point-interaction §18 |
| El punto de token solo consume un resultado y un token exacto; no resuelve ni busca la raíz más próxima | `AlgoLocusIntersectionPointV2.compute`; adquisición/liberación de claims en esa clase |
| El proveedor neutral exige una salida y conserva la familia; su política distingue fuentes V2 de candidatos públicos | `ConstructionGeoRedefineProvider.describeCandidateGroup/describeEffect/inspect` |
| Los roles de interacción requieren estado dedicado exclusivo; VALUE y roles especializados no son intercambiables | `supportsStableOutputRole`, `hasDedicatedInteractionPointState` |
| Una fuente con el mismo constructor semántico puede conservar identidad con nuevas dependencias | `sameV2RedefineContract`; `AlgoDependentPointLocusV2.getV2RedefineContractId`; prueba `PostG9U1A3CompatibleV2RedefineTest` |
| Soporte/estado/coordenada son entradas explícitas adicionales al comando serializado | `AlgoDependentPointLocusV2` constructor, `getDurableDependencyGeos`, `PointDriver.resolve` de PublicOperations |
| El registro valida identidades prospectivas y DAG real; no basta editar una lista de IDs | `SpatialIdentityRegistry.validateConstructionIdentityDependencyDags` |
| El auto-refresh de una edición ordinaria excluye salidas públicas V2 y exige el mismo paso propio | `isRefreshableFrontierRecord`, `constructionStep`, `certifyOrdinaryEditFrontiers` |
| La operación de redefinición pasa por evaluación tipada, participación candidata, lease y reconstrucción | `AlgebraProcessor.prepareSpatialRedefine/processReplace`, `Construction.replaceInternal` |
| El manejador externo puede ocultar el diagnóstico; el nombre NoExceptionHandling no implica propagación | `AlgebraProcessor.changeGeoElementNoExceptionHandling` captura errores y llama ErrorHelper |

Consecuencia sobre antecedentes: la ficha M08 que dice «sin comando ni variante dinámica»
no significa que no haya herramienta ni punto dinámico sobre L. G9U1 ya consume R6; falta la
operación nueva dependiente de Q. La decisión 10 de ADR 0019 es una frontera de **R6**, no una
prohibición eterna de futuros consumidores autorizados.

## 4. Observaciones heredadas y sonda CU7-D

### 4.1 Alcance de la evidencia histórica

K5 de `pre_g9b_cu3_b2_g1_characterization_record.md` §4 registra rechazo de `N=Point(c3)`
y `lx=LocusV2(X2,N2)` con raíces materializadas; cambios en sitio del mismo soporte sí se
aplicaron. B1/B2 no demostraron una barrera de linaje después de una redefinición efectiva.
No se convierte esa ausencia de camino en una prueba de identidad de raíces.

La sonda histórica usó `ErrorHelper.silent()` y `new EvalInfo(true,true)`. La ruta normal
`changeGeoElement` añade modo simbólico y opciones de redefinición de etiqueta. Comparar
ambas es necesario; atribuir el fallo solo a esas opciones sería una hipótesis.

### 4.2 Investigación reproducible, sin arreglar producto

La sonda `PreG9bCu7DRedefineCharacterizationTest` ejecuta únicamente APIs ya existentes,
con construcciones nuevas por fila, errores observables y comprobación de XML idéntico ante
rechazo. Compara políticas heredada/constructiva, opciones de llamada, presencia de cierre
materializado y soporte ya participante. Es evidencia de caracterización, no aceptación de
una futura implementación. Su comando y resultados están en el
[informe del candidato](../validation/pre_g9b_cu7_d_candidate_report.md).

Resultados de la sonda final: el rechazo de N con `ADVANCED_RETAIN` es
`DURABLE_CONTRACT_CHANGE`; añadir las opciones normales no lo elimina. El cambio de conductor
de L sí se admite en un caso sin el cierre de intersección. El modo explícito sobre N puede
llegar a fallar con `MALFORMED_RECORD` al reconstruir dependencias públicas sin identidad
prospectiva. Por tanto «el kernel nunca permite cambiar el conductor» no es una generalización
válida del cierre histórico. Con el cierre K5 materializado, el cambio de L obtiene
`ADVANCED_RETAIN_AVAILABLE`, pero sin participación previa del nuevo cierre falla después
con `GEO_ATTACHMENT_MISSING`. Si primero se registra `K=LocusV2(X2,N2)`, se aplica el cambio
de L en ambas políticas: conserva su ID y el ID del claim, pero el claim queda indefinido y
su token anterior no es admisible. N con soporte ya participante sigue fallando porque las
dependencias persistidas discrepan del DAG prospectivo. Esto caracteriza un caso transversal;
no demuestra aún la barrera general F/T ni su persistencia. El informe registra los límites.

### 4.3 Qué debe cambiar para admitir el soporte rigurosamente

1. Ofrecer evaluación/mensaje tipados de la operación exacta; el usuario no debe recibir un
   silencio ni el agente deducir éxito de que la API no lance una excepción.
2. Preparar el cierre prospectivo completo del nuevo soporte, incluidas identidades requeridas,
   sin publicar auxiliares durante el análisis.
3. Reconstruir coherentemente el padre de L: su comando usa el conductor, pero el evaluador y
   su registro también tienen soporte/coordenada explícitos. No reutilizar el campo support
   antiguo después de cambiar N ni corregir solo la geometría visible.
4. Evaluar como operación coordinada los contratos de las fuentes públicas afectadas. El
   refresco ordinario §10 no autoriza esa modificación; necesita enmienda y puerta propias.
5. Recalcular descomposición, linaje, fingerprints, células constructivas y certificados.
   Retener el ID del objeto L no prueba que sus antiguos tokens sigan designando una raíz.
6. Probar la barrera efectiva de F, T y transversales, latencia/retirada/recuperación, Undo,
   reapertura y política documental. Hasta entonces ambas observaciones siguen con trabajo
   pendiente; CU7-D las investiga, no las declara resueltas productivamente.

## 5. Arquitectura M08

Los contratos completos están en el [candidato M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md).

| Alternativa | Ventaja | Límite y decisión recomendada |
|---|---|---|
| S: consulta y fijación de dirección | mínima integración, usa identidad/persistencia existentes | no sigue Q; recomendada como primera operación |
| D: mapa dinámico explícito | dependencia real Q → P, reconstruible | necesita capacidad y receta nuevas; puerta separada |
| Continuar la dirección del último evento R6 | barata como arrastre | no define un mapa dinámico independiente de historia; rechazada como autoridad |
| CU3 `Point(L,D)` | correspondencia exacta ya disponible | solo su tabla de procedencia/soporte; reutilizar cuando corresponde |
| Un modo implícito según el tipo de Q o tolerancia | menos nombres visibles | oculta cambios de DAG y persistencia; rechazada |

La dirección `a=(source,provider,branch,component,u,lift,seam)` es distinta del ID del punto.
Una aproximación inicial puede seleccionar u como entrada explícita del punto forward; no
certifica una proyección exacta de Q. La tolerancia de búsqueda no se convierte en identidad.
La pertenencia del punto nuevo se deriva de la construcción en L, no de la proximidad de q.

D necesita una regla de selección reconstruible en cada revisión. Se recomienda comenzar por
proyección sobre una componente afín finita con evidencia rigurosa de fórmula, no colapso y
dominio. El candidato contiene su derivación por convexidad. La clase afín actual es evidencia
estructural flotante: un proveedor nuevo debe justificar cualquier garantía más fuerte.
El diseño general de estacionarios queda descrito como futuro y no adelanta la deuda de
certificación exacta generalizada de tangencias.

## 6. Arquitectura M09

El [candidato M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md) define el
predicado, la tabla de transiciones y los estados del protocolo.

| Alternativa | Resultado |
|---|---|
| A: actualización explícita compatible | mismo ID de P, nuevos padres/revisiones y nuevo claim explícito, dependientes recalculados; recomendada |
| C: reemplazo heredado explícito | ID nuevo; retira/elimina el cierre según autoridad actual; debe describir también cascada ordinaria |
| B: reemplazo con remapeo selectivo | requiere autoridad nueva respecto a ADR 0011/0026; no recomendado para el primer perímetro |
| Rechazo tipado | correcto ante familia/rol/grupo/DAG/evidencia incompatibles; nunca implica aceptación de C |

La ampliación no convierte `Intersect(L,obj)` en un punto. Seleccionar una solución admisible
del resultado rico es un paso semántico explícito. Si ese resultado debe crearse en el gesto,
sus padres, libro y claim entran en la misma transacción; cancelar no deja construcciones nuevas.
No se intenta «mantener P» reescribiendo el token de otra raíz ni reasignando por etiqueta.

Un punto ordinario sin ID necesita un contexto transaccional antes de que el candidato lo
convierta en participante; no hay ID antiguo que conservar. Un punto con rol especializado
requiere conservar su contrato de propiedad o un reemplazo explícito; VALUE no sustituye
`LOCUS_INTERACTION_POINT` porque ambos sean puntos Java.

El caso ordinario de intersección con varias salidas merece decisión autoral explícita. Hoy
no existe un adaptador general para separar una salida manteniendo todas las identidades.
El primer perímetro recomendado puede rechazarlo con causa específica. Si el autor exige
resolverlo en CU7-I, se añade una puerta de adaptador completo; no se declara M09 completo
dejando esa parte fuera sin una decisión autoral de alcance.

## 7. Fronteras, impacto y ciclo de vida

| Responsabilidad | Kernel compartido | Interfaz/cliente |
|---|---|---|
| Consulta y elección | produce candidatos, ámbito, evidencia y vigencia | presenta marcadores y recoge elección explícita |
| Geometría y validez | fuente/componente/mapa, admisibilidad y grados reales | muestra estado; no resuelve un segundo problema geométrico |
| Identidad | ID, roles, claims, tokens, barreras y linaje | conserva handles vigentes; no recupera por etiqueta |
| Redefinir | evaluación, impacto completo, DAG, P3-R1 y transacción | presenta operaciones ofrecidas/impacto y solicita elección |
| Undo/Redo | restaura estado normal y comprueba integridad | agrupa una operación exitosa y actualiza selección |
| Persistencia/copia | formatos, cierre completo y rechazo de versiones desconocidas | abre/guarda mediante la autoridad documental |
| Workbench | API de solo lectura o invocación explícita del protocolo | ninguna verdad geométrica propia |

Desktop es el primer consumidor; algebra, diálogo y acción de inspector deben compartir la
misma autoridad. Headless necesita resultados tipados y selección explícita sin Swing. Web y
otros clientes deben soportar el contrato o rechazar la creación; la compatibilidad de lectura
se verifica por separado. No se presupone paridad de autoría de frontends que aún no tienen
esos adaptadores.

Impacto mínimo futuro: reutilizar los padres GeoCeDG, PublicOperations, proveedor/registro y
transacciones actuales. Las costuras upstream potenciales son `AlgebraProcessor`,
`Construction`, despacho de comandos y snapshot anterior a eliminación de hermanos. No hay
justificación para renombrar paquetes, convertir L en `Path`, reescribir las intersecciones
clásicas o introducir semántica en Desktop. Toda modificación futura de esas costuras necesita
su especificación aprobada y la puerta de integración.

Cada propuesta especifica precondiciones, dominio, API conceptual, fallos y persistencia en su
candidato normativo. No se ofrecen implementaciones imaginarias: el tipo de padre dinámico,
su codec y cualquier adaptador de grupo son trabajo futuro explícitamente gated.

## 8. Riesgos y compatibilidad

| Riesgo/dependencia | Control propuesto | Puerta |
|---|---|---|
| Un candidato numérico se anuncia como proyección exacta | separar dirección elegida de prueba inversa y grado de integridad | I1 / I4 |
| Continuidad guiada por historia o cercanía | mapa reconstruible, pruebas de caminos directo/incremental/inverso | I4 |
| Creación de un resultado/auxiliar durante assessment | cierre sellado y snapshot de entrada antes del parseo | I2 |
| Rol propietario perdido en redefinición | tabla cerrada, propiedad exclusiva y rechazo de conversión no aprobada | I2 |
| Hermano ordinario eliminado antes de fallar | prohibición previa o adaptador de grupo completo | I2G |
| Soporte visible nuevo y padre/registro antiguo | cierre coordinado y validación del DAG prospectivo real | I3 |
| Token F/T/transversal reutilizado tras cambio de linaje | barreras ADR 0017/0039 independientes de retener P/L | I3 |
| Reapertura dependiente del último u o de coordenadas | receta persistente, eliminar caché, pruebas dormantes | I4 |
| Lector anterior ignora una receta nueva | codec/version gate, ensayo real de rechazo seguro | I4 |
| Crecimiento de coste por cada punto o movimiento | contadores deterministas, cálculo por revisión compartido; O(N) snapshot declarado | I1–I4 |
| Perímetro insuficiente de M09 | aceptación autoral explícita de diferimientos; no cerrar por silencio | D7 / I2G |

No hay migración automática de documentos. La política constructiva sigue siendo explícita;
`STRICT` no se hereda de un default. S y las transiciones singleton admitidas pueden conservar
formatos normales actuales; D o un grupo nuevo precisan versión/rechazo y matriz de lectores.
Guardar no persiste una certificación vigente como prueba durable.

## 9. Enmiendas, validación y parada

El [ADR 0041 propuesto](../adr/0041-locus-v2-point-placement-and-point-redefinition.md) y los
candidatos M08 §8/M09 §10 contienen las enmiendas listas para revisión. No se modifica ADR 0019,
0026, 0017, 0039, 0040 ni ninguna especificación aprobada. La
[matriz y estrategia](../validation/pre_g9b_cu7_d_validation_and_implementation_gates.md) distingue
evidencia CU7-D de pruebas futuras y fija las puertas de CU7-I sin registrarlas ni ejecutarlas.

Se mantiene `CU10-GUIDE-INTEGRATION-CHECK`: FINAL de CU10 comprobará integración/empaquetado
de la guía de `8f5c4f45`. Se mantiene `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`, posterior a
CU10 y al trabajo editorial del libro. E1 mantiene su expediente de transferencia científica
en CU10; ninguna fase editorial, cambio de libro o implementación queda iniciado.

CU7-D termina con el candidato, su commit/árbol, caracterización y STATIC aplicable. Tras la decisión autoral, la próxima
acción es revisar la conciliación normativa y la [propuesta de incrementos](pre_g9b_cu7_i_increment_proposal.md), y autorizar CU7-I separadamente si procede. No se infiere de
un resultado técnico autorización de publicación o aprobación del diseño.
