# CU7 integrado — Evidencia I3 / M08-D y alternativas

Fecha: 2026-11-02, Europe/Madrid. Base inspeccionada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`. Documento de estudio candidato; `selfApproved=false`. No adopta contratos, no implementa capacidades ni reabre M08-S-R2/CU3. Clasificación: planificación y auditoría de código, con entrega documental autorizada. Capas estudiadas: semántica/dependencias/identidad/persistencia del kernel compartido; consumidores Desktop/headless.

## 1. Autoridad y método

Se contrastaron `AGENTS.md`, ADR0041, el contrato candidato M08 aprobado prospectivamente, la propuesta de incrementos, la matriz CU7-D y fuentes Java actuales. La enmienda de ADR0041 de 2026-11-02, líneas 158–177, y la sucesión final de la propuesta de incrementos prevalecen sobre los encabezados históricos: solo M08-S-R2 está cerrado; M09 productivo continúa bloqueado y I2G/I3/M08-D no reciben autorización de implementación.

Los datos históricos CU7-D se atribuyen a su informe y sonda guardada. No se repitió Java, GUI ni aceptación de producto en este anexo. «Estático» indica lectura actual de código; «histórico» indica ejecución anterior identificada; «derivación» indica razonamiento matemático; «propuesta» no significa API existente. Ninguna fila K/D/U se declara PASS por inspección.

Para abreviar anclas de código, `K/` significa `source/shared/common/src/main/java/org/geocedg/common/kernel/`; `T/` significa `source/shared/common-jre/src/test/java/org/geocedg/common/`. Los nombres y líneas corresponden a la base indicada.

## 2. I3: cuatro operaciones diferentes

| Operación | Qué cambia | Contrato que debe conservarse |
|---|---|---|
| Cambio geométrico en sitio | Valor/definición del mismo soporte durable, sin sustituir su identidad | Reconstrucción por revisión; la vuelta al mismo selector completo puede recuperar un token latente conforme a su familia. Un valor distinto no exige por sí solo retiro permanente. |
| N cambia a otro soporte | Padre Path real de N y, por consecuencia, las entradas capturadas del generador de L | El nuevo soporte, su coordenada y participantes deben estar en el mismo overlay prospectivo que el DAG host y los registros. Igualdad geométrica entre soportes no establece correspondencia. |
| L cambia fuente/conductor dentro de familia | Dependiente trazado, estado/coordenada, soporte y cierre reconstructor de L | Retener el ID de L es cuestión del contrato del objeto; retener una raíz exige selector/owner/familia compatibles por separado. |
| Cambio de lineage/familia/rol | El contrato constructor o la correspondencia semántica | Operación explícita con barrera aprobada; no ordinary refresh oportunista, rebind por coordenadas ni simple copia de campos antiguos. |

La distinción no es nominal. `K/locus/SemanticGeneratorDescriptor1D.java:25`–`:42` enumera familia, dependentPointId, stateId, coordinateId, supportId, dominio, branch input, orientación, dependencias y firma de revisiones. `:189`–`:211` forma su firma semántica. `K/algos/AlgoDependentPointLocusV2.java:599`–`:631` exige registros durables actuales y construye ese descriptor; `:572`–`:595` reconstruye el evaluador al cambiar descriptor/fingerprint. `:738`–`:789` distingue argumentos públicos del comando, que omiten parte de la entrada del evaluador, de las entradas directas/reconstructoras completas. Por tanto reconstruir solo el texto `LocusV2(X,N)` no demuestra que el nuevo soporte/coordenada hayan reemplazado correctamente los campos capturados.

### 2.1 Evidencia histórica y su límite

`docs/validation/pre_g9b_cu7_d_candidate_report.md:30`–`:40` y `docs/architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md:114`–`:145` registran:

- N con ADVANCED_RETAIN rechaza `DURABLE_CONTRACT_CHANGE`; las opciones normales del host no lo arreglan.
- Contract-update sobre N alcanza `MALFORMED_RECORD` por participación prospectiva ausente o desacuerdo registro/DAG; que el soporte ya participe no certifica el cierre.
- Cambiar L puede admitirse en el caso mínimo. Con R/claim materializados, la participación nueva ausente produce `GEO_ATTACHMENT_MISSING`.
- Preregistrar `K=LocusV2(X2,N2)` permite el cambio observado de L: conserva L y el objeto claim, que queda indefinido; el token anterior no es admisible. Es una fixture transversal concreta, en ambas políticas, sin prueba general F/T ni persistencia.

La sonda fuente es `tools/benchmark/pre-g9b-cu/cu7-d/probe-java/org/geocedg/common/locus/PreG9bCu7DRedefineCharacterizationTest.java:34`–`:57`; el fixture `:103`–`:119` usa dos círculos distintos con geometría igual y coordenadas/directores distintos. Su fuente `:129`–`:201` observa XML/ID/claim/token. No instrumenta todos los estados de ledger, no contiene matriz F/T ni prueba de codec. El informe CU7-D §2 identifica el proceso histórico, sus 24 observaciones y hashes. No se reinterpretan las observaciones como aceptación I3.

### 2.2 Barreras F/T/transversal: qué se ve en la implementación

`K/locus/SemanticGeneratorDomainProvider1D.java:32`–`:34` define el parámetro como `true-coordinate/<coordinateId>`. `K/locus/intersection/PublicIntersectionRootIdentityResolver2D.java:1124`–`:1129` construye el continuation contract a partir de provider ID, parameter descriptor y contrato residual del target. `:1116`–`:1121` retiene la firma semántica completa del provider como address proof de revisión.

Esto **no permite concluir** que cualquier cambio de supportId sea ya una barrera efectiva: `K/locus/intersection/LocusIntersectionTokenLedger2D.java:1347`–`:1363` busca la asignación inicial por contract + selector + lineage y compara el contrato del target; no exige igualdad de toda la firma de revisión del provider. Esa separación permite el movimiento ordinario legítimo. Cambiar el valor de una firma de evidencia no equivale a cambiar el selector o retirar su asignación.

La envolvente exterior también importa: `K/algos/AlgoLocusIntersectionV2.java:298`–`:313` construye la pareja durable L/target; `:335`–`:337` abre el ledger con owner, constructiveIntersectionLineage y topologyContext. El ledger `:62`–`:77` rechaza cambio de material sin autorización de copia. Un nuevo coordinateId cambia el continuation contract; un nuevo L cambia la pareja; un cambio de soporte bajo los mismos IDs necesita su política explícita, no una afirmación automática de continuidad ni de retiro. El análisis no ha demostrado un camino actual autorizado que viole esas barreras; identifica la obligación de I3.

Las familias no se intercambian:

- F: `K/locus/intersection/FoldContactAnchor2D.java:13`–`:19` y `:145`–`:148`: germen de pliegue por involución y punto fijo exacto, junto a lineage de componente. Conserva su propia identidad entre sus dos preimágenes y contacto, según ADR0039.
- T: `K/locus/intersection/RegularTangentAnchor2D.java:11`–`:16` y `:66`–`:70`: lineage + familia/orientación canónica del target + lado certificado. El token de contacto regular no pasa a una pareja transversal.
- Transversal: selector intrínseco, fase/rango solo con el contexto exacto permitido. ADR0017 decisiones 6–9 y nota CU3-B2 final mantienen split/merge, orientación y seam/monodromía. Falta de prueba periódica produce cuarentena; rotación demostrada produce retiro; no son el mismo estado.

ADR0039:134–147 distingue estructura de germen y evidencia por revisión, y prohíbe fusionar/reasignar los tokens transversales a un contacto F. ADR0040:51–84 separa autoridad documental, ideal constructivo, integridad y transferencia CU2. Cambiar L/N no convierte certificados anteriores en prueba actual.

### 2.3 Disposición K01–K05

| Gate | Resultado de este estudio | Prueba futura precisa |
|---|---|---|
| K01 | Viable en principio; cierre prospectivo coordinado no implementado/demostrado | N nuevo soporte sin/con ID/participación; sellar overlay de todos los padres ocultos y explícitos; mismo DAG y registros; fallo en cada stage deja S0. |
| K02 | La retención de L ya puede ocurrir en una fixture; no es continuidad general de raíces | Misma familia/rol y cambio de coordinate/source; familia incompatible rechaza; revisar por separado IDs N/L/P, contrato, tokens y claims. |
| K03 | Nuevo coordinateId da barrera de contrato visible estáticamente; soporte diferente bajo mismos IDs requiere política propia | Tres fixtures F/T/transversal, geometría igual, IDs soporte/driver diferentes; comparar selector completo/material/owner y ledger antes/después. No aceptar igualdad de coordenadas como oráculo. |
| K04 | Movimiento ordinario y retorno no equivalen a cambio de soporte | Barrer radio/posición/definición del mismo soporte; pérdida y recuperación del mismo selector, con estados latente/cuarentena/retiro diferenciados. |
| K05 | Reutilizar contratos CU2/CU3/ADR0017; I3 debe probar su integración | Split/merge, cambio de orientación, seam amplio/directo y contacto T→pareja; claim antiguo, Undo, reopen/copy; ninguna transferencia entre familias. |

Recomendación I3: **MANTENER BLOQUEADA HASTA RESOLVER UNA PRECONDICIÓN** para implementación coordinada. Comparte staging/snapshot/lease/commit/rollback con M09, pero añade reconstrucción real del generador y barreras científicas propias; una transacción M09 positiva no satisface K01–K05. Coste relativo alto por la combinación, no por una supuesta prohibición universal del kernel. No exige I2G si todos los productores del cierre son singleton; sí lo exige si el cierre seleccionado incluye grupos que deben reconstruirse. No convertir I3 en dependencia de M08-S ya cerrado.

## 3. M08-D: clase cerrada viable e independencia de M09

Para una componente declarada `[a,b]`, finita, cerrada y no puntual, con fórmula ideal demostrada `F(u)=B+uA`, `A≠0`, y Q en la misma autoridad geométrica, sea `g(u)=||B+uA-Q||²`. Entonces `g''(u)=2 A·A>0`: el mínimo único es `u*=clamp(A·(Q-B)/(A·A),a,b)`. La continuidad en un compacto asegura existencia; la convexidad estricta asegura unicidad. Es una derivación exacta, no validación de un algoritmo binary64.

Si un extremo es abierto y el mínimo cae allí o más allá, el ínfimo no se alcanza. A=0 tiene imagen puntual pero parámetro no único y queda fuera del contrato no colapsado propuesto; no se decide arbitrariamente una dirección. Una componente cerrada elegida no establece mínimo global entre componentes. Dominios no finitos, pliegues, periodicidad, seams y mapa no afín quedan fuera de la primera clase. Son exclusiones explícitas, no atajos numéricos.

### 3.1 Qué falta y qué se reutiliza realmente

`K/locus/CertifiedAffineLocus2D.java:8`–`:23` ofrece coeficientes `double[]` provider-owned sin inferencia por muestras; no ofrece por sí solo semántica exacta del ideal, relaciones con Q o nueva receta D. `K/locus/ReconstructibleLocusEvaluator2D.java:723`–`:765` solo reconoce en su clase directa el escalar mismo o un `MyDouble` finito por coordenada, no cualquier expresión `Point(B+uA)`. `:440`–`:467` expone firma y coeficientes de esa clase. `K/locus/LocusSimilarityEvaluator2D.java:275`–`:309` los propaga a través de similitudes capturadas, con posible pérdida por no finitud. Nada de ello autoriza elevarlos a STRICT o a la prueba ideal más fuerte de M08 §4.1.

Sí hay infraestructura útil: programas de construcción capturados y acotados (`K/locus/CertifiedConstructionProgram2D.java:14`–`:20`, `:40`–`:51`, `:74`–`:111`), intervalos del modelo representado (`K/locus/intersection/ConstructionIntervalModel2D.java:130`–`:153`), revisiones/componentes de L, entradas ordinarias y participación pública. No hace falta reconstruir un target existente como en M09: D se puede crear como un nuevo algoritmo con padres verdaderos L/Q/receta y publicación ordinaria. Se necesitarían aún comprobaciones atómicas de creación y Undo propias, sin adoptar la transacción M09 general.

La receta propuesta debe contener versión de map/provider, ID L/Q, selector estructural de branch/componente, dominio/orientación, regla de selección/clamp, versiones de tolerancia/trabajo e inputs A/B/dominio cuando sean declarados. El último u y los certificados actuales son evidencia derivada; no se serializan como identidad ni eligen un mínimo. Un snapshot L/Q/inputs idéntico debe dar el mismo resultado después de borrar cachés, recorrido inverso y reapertura.

### 3.2 Opción de implementación restringida real

Un paquete futuro defendible, **no código existente**, sería D-AFFINE-CLOSED:

1. Tabla cerrada de fuentes cuya definición demuestra `B+uA` y de dónde proceden A/B. Primera alternativa: reconocer exactamente un constructor/AST expresamente limitado y sus padres durables; otra: ampliar solo un subconjunto afín de programas capturados. No aceptar identidad por que dos evaluaciones ajusten una recta, por nombre de clase o por coeficientes redondeados. No empezar con un recognizer general de toda expresión GeoGebra.
2. Una componente finita cerrada seleccionada; prueba de A·A>0 bajo la autoridad del documento y coherencia del dominio. Rechazar fuera de clase o prueba perdida. En constructive-authority, interpretar ideal y sustitutos conforme ADR0040; la tabla `ConstructiveClassTable` no convierte dependientes aritméticos en operaciones exactas automáticamente. Cualquier nueva semántica/proveedor exige aprobación propia; no reescribir silenciosamente la tabla global.
3. Evaluación de dot/división/clamp por aritmética exacta de los datos admitidos o intervalos outward con denominador separado de cero, error en u y propagación a P. Cerca de un extremo debe probarse la decisión o usarse un resultado con garantía compatible y causa explícita; nunca elegir por tolerancia de pantalla. Escalado estable evita overflow evitable; si la política/presupuesto no permite probar, resultado tipado sin coordenadas stale.
4. Algoritmo dependiente nuevo en kernel; un único commit de creación; sus padres reales incluyen L/Q y datos de receta. Cambios del mapa, rama o soporte necesitan redefine explícito, que puede permanecer rechazado mientras M09 no esté disponible. Movimiento ordinario de Q/inputs reevalúa la receta sin redefinirla.
5. Formato/lector antes UI: nueva receta versionada y capability gate que el lector previo real rechace sin cargar P como libre o S. No basta añadir un XML desconocido que el loader pueda ignorar. Copia de cierre completa remapea todos los IDs; parcial rechaza o usa política explícita; Undo restaura receta e IDs y recalcula evidencia.
6. Solo después, operación pública/headless y presentación Desktop mínima. Feature flag experimental. No reutilizar el gesto R6 en cada update ni introducir una segunda DAG en un listener.

Gates de abandono de este paquete: no demostrar vínculo fuente↔fórmula ideal dentro de la tabla pequeña; necesitar ampliar la clase general/arreglar la reconstrucción host; no cumplir lector previo cerrado; o no demostrar numérica y policy del clamp. En esos casos se difiere D sin ampliar el alcance automáticamente y sin fallback a scan, CU3 o S. Aceptar una tabla más estrecha requiere decisión explícita si pierde casos comprometidos.

Este diseño es **viable en principio, coste medio-alto, invasividad kernel/formatos acotable**, no imposible por la ausencia de provider actual. El coste principal es la garantía y el lifecycle del nuevo contrato; el cálculo analítico es pequeño. Se puede desarrollar en paralelo con investigación M09 porque no requiere la reconstrucción de un objeto existente ni I3 para sus movimientos ordinarios.

### 3.3 Comparación con capacidades existentes

| Opción | Valor real | Límite / recomendación |
|---|---|---|
| CU3 `Point(L,D)` | Correspondencia dinámica por procedencia y soporte real, con evidencia de pertenencia y formato ya implementados | Reutilizar sin código cuando el problema ya ofrece ese D. No sirve para Q arbitrario. `K/algos/AlgoLocusDriverCorrespondencePoint2D.java:56`–`:75`; resolver `:136`–`:192` y `:359`–`:399`. |
| Composición Q→u→`Point(L,branch,u)` | Usuario que ya construyó A/B/dominio obtiene DAG dinámico y persistencia normal sin inversor nuevo | `K/algos/AlgoSemanticLocusPoint2D.java:96`–`:101` acepta parámetro dependiente. No demuestra por sí sola que L mantenga la receta al redefinirlo, no tiene contrato D de estado/error/reader. Min/Max/ClosestPoint son CONDITIONED_SELECTION (`K/locus/constructive/ConstructiveClassTable.java:92`–`:95`), sin STRICT automático. |
| Perpendicular/intersección + CU3 | Construcción CeDG explícita para el pie interior cuando la tabla/procedencia y componente lo admiten | Fuera del segmento no inventar clamp; igualdad de coordenadas no hace D procedente. Aplicar sus rechazos actuales. |
| Nuevo D-AFFINE-CLOSED | Clamp certificado, mapa persistente L/Q, estados tipados, garantía numérica y verificación del vínculo a la fuente | Beneficio adicional real frente fórmula manual; nuevo formato/proof/lifecycle. No resuelve proyecciones no afines. |
| Wrapper automático de la fórmula, sin proof/receta | Reduce escritura manual | **DESCARTAR LA PROPUESTA CONCRETA POR EXISTIR UNA ALTERNATIVA MEJOR**: la composición ya expresa ese cálculo y el wrapper no cumple M08-D. |

Los tests inspeccionados `T/kernel/locus/constructive/StructuralPointMatrixTest.java:69`–`:106`, `:146`–`:177` y `:423`–`:436` cubren ruta de soporte, soporte coincidente/copiado distinto, familia escalar fuera de CU3, dominio exacto y copia de la forma estructural. Son evidencia de diseño del test, no PASS nuevo de este estudio.

**Recomendación para la decisión integrada:** D-AFFINE-CLOSED puede elegirse como el único desarrollo productivo nuevo de CU7 si el autor considera valiosos la garantía/clamp/persistencia de ese caso restringido. No debe empaquetarse detrás de M09 ni usarse para justificar un framework común. En ausencia de ese caso de valor prioritario, preferir **DIFERIR A FASE POSTERIOR** M08-D con este paquete cerrado conservado: el simple caso afín ya tiene alternativas explícitas, mientras el nuevo coste de prueba/formato no compra el caso curvo general que motiva la inversión de loci. El diferimiento es una decisión de valor/coste, no una prueba de inviabilidad. Implementar solo el wrapper no es una alternativa aceptable ni un cumplimiento parcial de D.

### 3.4 D01–D08 y U04: campaña futura mínima

| Gates | Oráculo y cobertura |
|---|---|
| D01–D02 | Referencia racional/exacta independiente para interior, ambos exteriores e igualdad exacta de extremos; a<b finitos cerrados; abiertos, A=0, dominio no finito y clase ausente rechazan con causa. Garantía aritmética separada de unicidad matemática. |
| D03 | Mismo snapshot por asignación directa, pasos, recorrido inverso, caché eliminada y reapertura: misma receta/selector, mismo resultado/garantía deterministas. |
| D04–D05 | Caso no afín con dos mínimos o mínimo/máximo; pliegue/singularidad/seam: UNSUPPORTED o MAP_NOT_ESTABLISHED, ninguna elección por último u. |
| D06 | Pérdida de componente/afinidad/no colapso y recuperación: coordenadas actuales indefinidas, receta retenida; recuperar solo el mismo mapa/selector demostrado. |
| D07 | Políticas legacy/constructive; Q libre, construido dentro/fuera de clase y sustitutos; nunca elevar a STRICT por residuo pequeño, finite output o coefficients double. |
| D08 | A muy pequeño/grande, traslaciones grandes, cancelación en Q−B, overflow/underflow, límites del presupuesto; residual/error con enclosure válido o causa tipada; zoom/DPI irrelevantes. |
| U01–U03/U05–U06 | Reopen definido/dormante, Undo/Redo repetido, copia completa/parcial, renombrado/delete-recreate, múltiples consumidores una revisión, regresiones CU3/R6/M08-S sin alterar sus garantías. |
| U04 | Writer actual→lector actual, anterior real y recipe/version corruptos/desconocidos; gate previo a UI; desconocido rechaza documento/operación de forma segura, nunca carga snapshot/libre. |

## 4. Dependencias para el grafo integrado

- Obligatorias I3: cierre prospectivo + identidad/contratos + transacción host/durable + reconstrucción del generador + barreras F/T/transversal + persistencia/Undo. I2G solo cuando el cierre real lo exige. Autorización I3 es otra dimensión, no consecuencia de pasar I2.
- Obligatorias D: clase matemática/proof + snapshot fuente coherente + identidad de mapa/componente + DAG ordinario de creación + receta/version/lectores + garantías/policy + publicación/Undo. No M09 ni I3 para crear y mover ordinariamente el nuevo objeto; redefinir su mapa existente puede seguir no soportado.
- Convenientes: captura/intervalos/componentes/revisiones existentes de CU2, evidencia exacta/relajada de CU3 cuando aplique, participación/identity registry, UI contextual ya probada por M08-S. Reutilización de un panel no otorga garantías científicas ni permisos de commit.
- Históricas: I2→I1 era orden elegido, ya superado por el cierre independiente M08-S. I1→I4 es conveniencia de superficie/normalización más estado ya satisfecho, no razón para bloquear la matemática D.

## 5. Inventario, efecto y límites

Inspeccionados: AGENTS; ADR0017/0039/0040/0041; contrato M08; propuesta incrementos; matriz, candidato y arquitectura CU7-D; sonda CU7-D; SemanticGeneratorDescriptor/DomainProvider; AlgoDependentPointLocusV2; PublicIntersectionRootIdentityResolver; ledger y address proof; AlgoLocusIntersectionV2; component lineage; FoldContactAnchor/RegularTangentAnchor; CertifiedAffineLocus; ReconstructibleLocusEvaluator; LocusSimilarityEvaluator; CertifiedConstructionProgram; ConstructionIntervalModel; SplinePolynomialModel/SplineIntervalModel (descartada la suposición de que exista una SplineV2 de dos puntos: su create exige al menos tres y grado≥3); AlgoSemanticLocusPoint; public operations; AlgoLocusDriverCorrespondencePoint; StructuralPointResolver/ClassTable/Operation y StructuralPointMatrixTest; se consultaron puntos de entrada de MyXMLHandler para no presuponer rechazo de tags desconocidos.

Creado únicamente este anexo de investigación en staging ignorado; el integrador decide su incorporación al worktree del estudio. No cambia semántica, contrato, runtime, lector, ID, solver ni guía. No se ejecutaron tests/experimentos nuevos; no hay logs/receipt de aceptación propios de este anexo. Los límites principales son la falta de prueba runtime I3 completa y de provider/codec D implementados; el estudio propone puertas exactas y no los transforma en capacidades existentes.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no se alteran requisitos de estación, JDK/Gradle ni tooling. VERIFICATION_INFRASTRUCTURE_IMPACT=NONE. GUIDE_IMPACT=NONE para este anexo; cualquier implementación futura debe documentar su efecto y conservar CU10/E1, deuda general de tangencia y arrastre Locus V2 como obligaciones independientes.
