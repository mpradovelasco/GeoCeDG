# PRE-G9B-CU7 — Creación general: auditoría de contrato y enmienda candidata

Fecha: 2026-11-02, Europe/Madrid. Base inspeccionada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`, árbol `d62179e72dd2bb37226cba5956198809467cb7c1`. Estado: **CANDIDATO PARA DECISIÓN NORMATIVA; NO ADOPTADO NI IMPLEMENTADO**. `selfApproved=false`.

Este documento separa A, integrar clic como petición transitoria bajo las admisiones ya aprobadas, de B, admitir una dirección individual fuera de ellas sin C1. A está cubierta por la nueva autorización autoral condicionada, si no cambia sus criterios. B requiere una decisión normativa expresa; su implementación debe detenerse en esa frontera.

## 1. Pertenencia al requisito CU7 y origen del clic

**Sí, la creación desde una petición próxima pertenece al requisito M08/CU7.** El inventario original `docs/architecture/pre_g9b_cu_corrective_usability_plan_candidate.md:165` asigna M08 `PointOnLocusV2: punto sobre Locus V2 a partir de un punto próximo` al núcleo/CU7; su desarrollo en `:625`–`:644` distingue pertenencia, proximidad y proyección, y describe R6 como consulta transitoria con selección. El plan sucesor `docs/architecture/pre_g9b_cu_corrective_usability_plan.md:567` conserva M08/M09 en CU7. El contrato M08 aprobado prospectivamente, `geocedg/specs/locus/candidates/cu7-m08-point-placement.md:29`–`:44`, ya contempla una posición world q que *puede* proceder de Q, sin que Q identifique preimagen.

**El clic procede de la arquitectura R6, aunque R6 no implementó la herramienta gráfica final.** ADR0019 Context (`docs/adr/0019-semantic-locus-point-interaction-support.md:13`–`:26`) plantea la futura herramienta Point con una petición geométrica aproximada; decisiones 1–4 separan source, petición, proximidad e identidad. `docs/architecture/g9u0_r6_semantic_locus_point_interaction_support.md:50`–`:81` representa stroke hit-test → world request → kernel → selección → forward. El contrato de interacción `geocedg/specs/locus/locus-v2-point-interaction.md:10`–`:20` dice expresamente que R6 es capacidad kernel y difiere la herramienta. Por tanto el origen del flujo es R6; atribuir a su aceptación un smoke de un consumer gráfico aún ausente sería incorrecto.

| Operación | Autoridad / efecto | Relación con este encargo |
| --- | --- | --- |
| Forward por dirección explícita | `Point(L,branch,u)` y binder de dirección/componente; evaluación del mismo parámetro al cambiar L | Se reutiliza; no es una inversión ni una proyección |
| Creación desde clic o Q | Petición transitoria q descubre direcciones; elección explícita fija dirección forward; Q no es padre nuevo | Único objetivo funcional adicional candidato |
| Arrastre de P existente | Edición posterior de estado bajo el contrato de continuación dentro de rama/componente | Fuera del alcance general; la capacidad existente se conserva y la deuda general continúa |

La independencia de Q es semántica: tras crear, P depende de L y de sus entradas de dirección legítimas. Si Q ya influye en L, esa dependencia transitiva legítima no se elimina; lo que no se añade es Q como padre de una consulta inversa persistida.

## 2. Qué admite ya el contrato y dónde está la barrera

La enmienda M08-S-R2 del 2026-11-02 conserva R1/R6 y permite una ruta separada de selección individual. La concesión no es general: exige sus pruebas R2/H1 de componente/interior, modelo y C1, proximidad world certificada, fidelidad, ausencia de barreras, presupuesto y vigencia. Fuentes directas: ADR0019 enmienda final; ADR0041 `:158`–`:177`; M08 `:246`–`:299`; interacción enmienda final y superficie pública `:836`–`:853`.

En M08 `:263`–`:269` el criterio es literal: modelo intervalar C1 en la vecindad interior de doubles adyacentes y un primer jet que excluye cero en una coordenada en toda ella; enclosure puntual de distancia estrictamente bajo radio; forward finito dentro del enclosure. No basta que la dirección defina un forward válido.

| Situación | Criterio ya aprobado | ¿Cambio normativo necesario? |
| --- | --- | --- |
| Forward explícito actual/canónico, componente certificada y binder válido | Puede construirse sin certificado C1 de H1 | No; no atribuirle evidencia inversa nueva |
| Resultado R6 UNIQUE dentro de su evidencia/cobertura declarada | Admisión histórica R1/R6 | No |
| Resultado R6 MULTIPLE de direcciones admitidas localmente, incluso evaluator sin modelo intervalar C1 | Elección explícita de una candidata de ese resultado; grado numérico R6 real | No. No todo evaluator sin C1 está hoy bloqueado |
| Único candidato de búsqueda general con `GLOBAL_UNIQUENESS_NOT_ESTABLISHED` y prueba H1 completa | Ruta independiente R2/H1 y confirmación explícita | No; su cobertura sigue NOT_ESTABLISHED |
| Ese único candidato, pero sin modelo/prueba C1 H1 | No tiene permiso nuevo solo por ser forward válido | **Sí**, para admitirlo por otro criterio |
| Nuevo criterio que acepte solo residual numérico sin enclosure puntual | Sería menor garantía de proximidad que H1 | **Sí, además** de retirar la guarda C1; debe declararse expresamente |
| Evaluador finito con dirección que no puede asociarse a componente certificada/lineage reconstruible | No lo salva una evaluación finita o el clic | No admisible en este perímetro; requeriría otro trabajo de existencia/reconstrucción, excluido |

La distinción MULTIPLE es importante: la búsqueda general R6 puede descubrir y verificar varios candidatos sin excluir mínimos no muestreados. `LocusPointInteractionResolver2D.java:885`–`:950` mantiene ese comportamiento, mientras un solo candidato de cobertura no completa conserva UNRESOLVED. La regularidad histórica es evidencia numérica ESTIMATED_ERROR en sus rutas (`:697`–`:733`), no el certificado intervalar C1 H1. No se pueden convertir todos los fallos sin C1 en un mismo bloqueo ni cambiar esas admisiones para uniformarlas.

### 2.1 Implementación permitida de la parte A: clic con admisiones actuales

El nuevo encargo autoriza explícitamente clic y permite implementar cambios sin alterar las admisiones. M08 §1 ya define q como petición transitoria y §2 ya usa posición world. `M08SnapshotPlacement2D.java:202`–`:222` ofrece ambas entradas, `query(source,x,y,policy)` y `queryFromReference(source,Q,policy)`.

Por ello **sí es lícito integrar clic como otro origen del mismo protocolo existente**, manteniendo R1/R6 y, solo ante su causa autorizada, R2/H1. El clic debe copiar sus coordenadas world y fuente seleccionada; no crea un GeoPoint ficticio Q, no toma el parámetro del render y no adquiere autoridad de hit-testing. La query, Choice/Selection, política y commit pertenecen al kernel. Toda oferta nueva R2/H1 sigue requiriendo confirmación explícita y su presupuesto acumulado. Un fallo de commit no habilita fallback.

La enmienda Desktop que describe L→Q registra el consumer aprobado de aquel incremento. La instrucción autoral actual sí autoriza añadir el clic como consumer de sus mismas garantías; no exige reinterpretar C1, cambiar un codec o declarar que la nueva admisión sin C1 ya estaba aprobada. Clic y Q pueden producir conjuntos semánticamente equivalentes para el mismo q/política/source revision; sus capabilities son privadas a cada consulta y no son intercambiables. Q añade su comprobación de vigencia hasta commit; el clic conserva el valor world capturado, sin referencia Q.

Preservar el flujo R6 ya válido y el arrastre histórico. Si se añade confirmación al nuevo clic R2/H1, no utilizarla para promover un resultado inseguro ni para alterar retroactivamente los estados/garantías de R6.

## 3. Caracterización matemática mínima

Sea `a=(idL,provider,b,c,u0,lift,seam)` una dirección completa en una definición actual de L. Para definir `P=F[b,c](u0)` basta que la dirección sea inequívoca y reconstruible y que la evaluación forward sea válida bajo la autoridad del documento. No se necesita resolver `F(u)=q`, buscar un mínimo de distancia ni demostrar que F sea inyectiva local o globalmente.

La política de producto vigente añade una condición que aquí se conserva: el parámetro pertenece a una componente continua certificada y su selector se resuelve mediante correspondencia estructural. `locus-v2-point-interaction.md:393`–`:411` y existencia §15.5–§15.8 distinguen existencia, disponibilidad numérica y representación. Una evaluación válida nunca prueba existencia; unique containment valida una correspondencia y nunca la crea. Las componentes puntuales no se seleccionan mediante un parámetro binary64.

| Hecho | Prueba suficiente / significado | Lo que no demuestra |
| --- | --- | --- |
| Parámetro válido | Canonicalización provider y pertenencia actual en el dominio declarado | Existencia/continuidad de una componente sin su certificado |
| Rama/componente identificada | Branch key y selector persistible con resolución estructural única | Identidad por coordenadas, ordinal, etiqueta o containment aislado |
| Forward válido | Binder actual reconstruye el mismo selector/argumento y publica evaluación finita válida | Inyectividad, regularidad diferencial o cercanía ideal a q |
| Cercanía numérica | Residual world computado de la evaluación forward actual y radio declarado | Cota rigurosa del residual de la geometría ideal |
| Cercanía certificada | Enclosure puntual `B0` de la configuración pertinente y `sup(x∈B0)||x-q||²<rho²` | Mínimo local/global, búsqueda completa o inyectividad |
| Regularidad local | Evidencia diferencial con el grado que realmente tenga | Necesidad matemática universal de esa propiedad para crear P |
| Dirección individual determinada | El usuario elige un tuple canónico exacto de una consulta actual | Unicidad de todos los tuples que dan la misma imagen |
| Preimagen cartesiana única | Prueba de inyectividad/exclusión del problema inverso correspondiente | Se desprende de seleccionar una dirección concreta |
| Búsqueda completa | Cobertura demostrada del ámbito declarado | Se desprende de contar uno o varios candidatos encontrados |

Ejemplos matemáticos, no fixtures ejecutadas: en `F(u)=(u²,u³)`, la dirección u=0 define un punto aunque F'(0)=0; la singularidad diferencial no impide por sí sola la definición forward. En `F(u)=(sin u,sin 2u)`, dos parámetros canónicos no equivalentes pueden compartir posición y seguir siendo direcciones diferentes. Un mapa constante define un forward para cualquier argumento explícito, pero una petición geométrica por sí sola no establece qué dirección elegir; los resultados colapsados/degenerados actuales siguen rechazados en el primer perímetro. Ninguno de estos ejemplos autoriza al evaluador actual a certificar sus componentes o singularidades por analogía.

La identidad elegida vive en el tuple; la petición geométrica q no persiste. Una fuente que después pierde la componente/correspondencia deja P indefinido bajo las reglas actuales; no se elige otra dirección por proximidad ni se borra el selector retenido. Recuperar requiere la misma correspondencia aprobada.

## 4. Decisión cerrada propuesta para la parte B

**Propuesta preferente para revisión autoral: `INDIVIDUAL_FORWARD_ADDRESS_CREATION`, exclusiva de creación nueva.** Es el máximo perímetro útil que separa con honestidad la definición forward de la prueba inversa: no exige C1 ni mínimo/inyección/completitud inversa, pero nunca rebaja existencia, lineage, reconstructibilidad, autoridad, vigencia o presupuesto.

La decisión debe adoptar explícitamente **dos cambios respecto de la vía H1**: (i) regularidad C1 no es requisito universal de la nueva vía; (ii) la proximidad puede tener grado `FLOATING_POINT_UNCERTIFIED` cuando solo se dispone de evaluación/residual numérico. Un bound puntual exterior válido, si existe, permite grado `CERTIFIED_ERROR_BOUND` para esa proximidad; no se inventa ese bound cuando no existe. La evidencia numérica no se presenta como una cota de cercanía ideal. Las admisiones H1 conservan íntegra su garantía más fuerte y su nombre.

### 4.1 Predicado de admisión candidato

Antes de ofrecer una dirección y otra vez antes de publicar, la nueva vía deberá establecer:

1. Fuente actual con identidad durable, definición/provider y política de autoridad válidos. Evaluador reconstructible y determinista; no render, lambda no persistible, historia ni estado oculto.
2. Branch key y componente continua certificada actual que contiene el argumento; selector existente resuelto por correspondencia estructural única. No componente puntual, índice, matching por proximidad ni simple pertenencia sin lineage. Una cobertura de existencia global PARTIAL no basta por sí sola ni invalida automáticamente una componente local certificada: el consumidor local debe demostrar todo su ámbito actual, sin cambiar la estructura de existencia.
3. Dirección canónica completa, finita y exacta en sus bits persistidos, con lift/seam solo bajo el contrato periódico ya aprobado. Distintas preimágenes se conservan distintas; se deduplican solo equivalencias canónicas probadas por el proveedor.
4. Binder forward normal válido que reconstruye exactamente el selector elegido y produce coordenadas finitas bajo su grado habitual. Ni residual bajo ni un resultado UNRESOLVED histórico sustituyen esta prueba.
5. Petición q y radio world finitos; distancia de la representación forward actual computada finita dentro de la política numérica de búsqueda. Esta prueba recibe explícitamente grado no certificado cuando no hay bound puntual. No implica que Q esté sobre L, ni un mínimo, ni una proyección, ni una cota de error ideal. Si se ofrece grado certificado, acreditar además enclosure/fidelidad de configuración y desigualdad world rigurosa.
6. Elección explícita de un handle privado de esa consulta incluso si se encontró una sola dirección; mostrar el ámbito y grado de evidencia pertinentes. La elección resuelve cuál tuple se usará, no una falta de validez estructural.
7. Presupuesto acumulado contabilizado de descubrimiento, validación, revalidación y publicación; ningún reinicio oculto. Revalidación de fuente/revisión/autoridad/política y Q actual cuando fue la referencia. Selección single-use y rechazo de stale/foreign/consumed sin mutar.
8. Publicación exclusivamente por normal form forward y normal Undo host. No nuevo padre Q, fórmula dinámica inversa, consulta persistida, codec o autoridad de GUI. La creación no autoriza arrastre general.

La nueva vía necesita evidencia propia, no reinterpretar enums históricos: la falta de regularidad inversa por sí sola no invalida un forward, pero una singularidad **no resuelta en existencia/correspondencia/evaluación** sí veta la oferta. Un motivo histórico de unsafe no se convierte en seguro con confirmación; la nueva operación deberá demostrar independientemente su propio predicado. Una señal explícita de colapso/imagen degenerada, estado no finito, gap, componente no estable o trabajo agotado sigue siendo rechazo. No se promete cubrir arbitrarios evaluadores finitos.

La autoridad constructiva conserva ADR0040 y los contratos de transferencia vigentes: si la evidencia disponible pertenece a una configuración capturada y no existe transferencia aprobada hacia la autoridad activa que la operación necesita, la oferta falla tipadamente. Un residual numérico o incluso un enclosure de la configuración representada no adopta una política documental, no prueba incidencia ideal y no eleva ninguna coordenada/certificado a STRICT.

**Tabla inicial cerrada propuesta.** La primera implementación de esta vía se limita a
`ReconstructibleLocusEvaluator2D` con captura `CertifiedConstructionProgram2D` v3
actual establecida, estructura de existencia `COMPLETE` ya producida por CU2 y una
componente continua actual con selector inequívoco. Reutiliza el descubrimiento semántico
acotado existente y sus barreras; no inventa semillas desde el render ni elimina un rechazo
de fuente/capacidad. Puede reutilizar una dirección diagnóstica de búsqueda inversa no
completa únicamente después de demostrar de forma independiente los ocho predicados.
El grado no certificado se refiere a proximidad, no a una existencia sin pruebas. La
primera tabla no admite nuevas fuentes de existencia PARTIAL, evaluadores arbitrarios,
composición R5 sin captura v3 equivalente, componentes puntuales o colapsadas. Esas
extensiones quedan fuera hasta tener su evidencia y decisión propias. Exigir inventario
de existencia COMPLETE en esta tabla no exige completar la búsqueda inversa ni probar
unicidad de preimagen. Esta tabla hace ejecutable la decisión inicial sin ampliar CU2.

### 4.2 Estados candidatos y prueba negativa

Nombres conceptuales para diseño, no enums implementados: separar `DIRECTION_VALIDATED`, `COMPONENT_RESOLVED`, `FORWARD_VALID`, `PROXIMITY_NUMERICAL_UNCERTIFIED` / `PROXIMITY_CERTIFIED`, `EXPLICIT_ADDRESS_SELECTION_REQUIRED`, `GLOBAL_COVERAGE_NOT_ESTABLISHED`, `STALE` y `BUDGET_EXHAUSTED`. Mantener `MINIMUM_NOT_CLAIMED`, `INVERSE_UNIQUENESS_NOT_CLAIMED`, `STRICT_NOT_PROMOTED` como propiedades, no como promesas de exactitud.

Ninguna oferta probada se reporta como `NO_OFFER_ESTABLISHED` o causa más específica; no como ausencia certificada de posiciones. Que la búsqueda encuentre una, varias o ninguna dirección no cambia su cobertura. Si un predicado de identidad/reconstrucción falla, no se genera handle ejecutable. Si falla proximidad numérica/no finitud o presupuesto, no se publica punto. Los casos de renovación/copy/reopen consumen la misma dirección persistida y no vuelven a buscar q.

### 4.3 Alternativa más conservadora, explícitamente separada

Si el autor no acepta el grado de proximidad numérico no certificado, una decisión alternativa cerrada es `INDIVIDUAL_FORWARD_ADDRESS_CREATION_CERTIFIED_POINT_PROXIMITY`: mismo predicado anterior, pero requiere siempre enclosure puntual fiel de F(u0) y bound world riguroso; no exige C1 ni jet no nulo. Esta alternativa es matemáticamente suficiente, pero seguirá excluyendo evaluadores sin tal capacidad de valor, incluidos modelos v3 sin adaptador puntual.

No se presenta la alternativa como gratis: `M08LocalAddressEvidence2D.java:81`–`:101` combina hoy captura, suavidad, enclosure de valor y primer jet; `ConstructionIntervalModel2D.java:88` vuelve a exigir suavidad incluso para evaluar. Separar prueba puntual sin C1 puede exigir una costura limitada de evidencia nueva. No se quitan guards a H1 para simularla. Ninguna de las dos alternativas está implementada o adoptada en este documento.

## 5. Textos exactos candidatos de enmienda

Los siguientes bloques son para añadir como **enmienda sucesora de alcance independiente**, solo después de decisión autoral. No sustituyen ni reescriben la aceptación M08-S-R2 ni los estados R6. Si se elige la alternativa certificada de §4.3, sustituir en todos los bloques la admisión de proximidad numérica por su requisito de enclosure; no adoptar una mezcla ambigua.

### 5.1 ADR0019 — añadir a Decision / Evidence model

> Exclusivamente para la creación de un punto nuevo, la operación separada INDIVIDUAL_FORWARD_ADDRESS_CREATION puede ofrecer una dirección semántica individual actual, canónica y reconstruible, elegida explícitamente desde una petición geométrica transitoria. Debe demostrar fuente/provider vigentes, pertenencia a una componente continua certificada y selector resuelto por correspondencia estructural, y reconstrucción forward válida de esa misma dirección. No exige C1, inyectividad local/global, mínimo de distancia ni completitud de búsqueda inversa. La proximidad de descubrimiento se declara por su grado real: un residual numérico de la evaluación forward es FLOATING_POINT_UNCERTIFIED y no certifica cercanía a la geometría ideal; CERTIFIED_ERROR_BOUND solo se ofrece con cota puntual exterior y fidelidad de configuración demostradas. Ningún grado de proximidad establece identidad o STRICT. Toda oferta de esta vía necesita elección explícita incluso cuando solo se encontró una dirección. Una dirección inválida, no reconstruible, con componente ambigua o estado no vigente no se promueve por confirmación. Presupuesto agotado, gaps, colapso, estado no finito y singularidad no resuelta en existencia/correspondencia/evaluación fallan de forma tipada. R6, las admisiones R1/R2/H1 y el arrastre conservan sus estados y garantías; UNRESOLVED histórico no da permiso por sí solo.

### 5.2 ADR0041 — añadir precisión a snapshot placement, decisiones 2/3/11

> La creación de un punto desde un clic o una referencia Q son dos formas de autoría de una petición world transitoria. Ambas consumen la misma admisibilidad kernel y publican exclusivamente la construcción forward aprobada y su dirección semántica. Q y las coordenadas de clic no son identidades ni padres persistidos nuevos. La vía INDIVIDUAL_FORWARD_ADDRESS_CREATION tiene el predicado y el grado de proximidad descritos en su enmienda de ADR0019; no modifica las pruebas más fuertes de M08-S-R2/H1. Descubrimiento, evidencia individual, cobertura global, permiso y vigencia permanecen separados. La creación no afirma proyección métrica o seguimiento de Q ni autoriza una operación nueva de arrastre, redefinición, migración, codec o autoridad constructiva. Los parámetros/direcciones explícitos conservan la semántica forward y los selectores de componentes ya aprobados.

### 5.3 M08 — sección sucesora nueva para creación general

> Se añade INDIVIDUAL_FORWARD_ADDRESS_CREATION como operación exclusivamente de creación nueva, bajo el predicado de ocho puntos de esta decisión. Preserva sin reinterpretación las admisiones R1/R6 y R2/H1; una prueba o un commit fallidos de esas vías no equivalen a éxito de la nueva. La consulta nueva consume una fuente L y una posición world finita q, obtenida por clic o por lectura transitoria de Q, junto con radio y política determinista de trabajo. Sus ofertas son direcciones canónicas actuales con componente certificada, correspondencia estructural y binder forward válidos. No se exige regularidad C1 para definir la dirección ni mínimos o unicidad de preimagen. El residual puede ser numérico no certificado, declarado como tal, sin afirmar cota ideal; la prueba certificada puntual es una capacidad adicional cuyo grado solo se usa cuando está establecido. La elección privada, explícita y de un solo uso determina qué dirección se publica. La consulta no afirma ausencia, unicidad o completitud por el número de ofertas. Selecciones caducadas, ajenas, consumidas, sin reconstrucción o con presupuesto agotado no publican. Persisten solo la dirección forward y padres legítimos; Q, petición, evidencia, scope/choice visual e historial de descubrimiento son transitorios. El punto reevalúa su dirección al cambiar L y puede quedar indefinido según la correspondencia existente; no se reubica por proximidad ni sigue Q.

El texto anterior deberá incorporar por referencia una tabla aprobada de causas y capacidades/provider iniciales; no basta copiar el nombre de la operación para declarar implementado el predicado. El perímetro no incluye crear un nuevo certificado de existencia, nueva correspondencia ni capacidades de un evaluador que no las tiene.

### 5.4 Interacción — nueva sección sucesora, creación separada del arrastre

> La nueva admisión de creación individual selecciona una dirección, no una inversión única. El frontend puede obtener q por clic sobre una fuente seleccionada o desde Q; el kernel descubre, comprueba y entrega handles privados de ofertas actuales. La coordenada de pantalla sirve para hit-testing y se convierte en petición world; ni el segmento renderizado ni su índice suministran el parámetro o componente. Antes de publicar se revalidan fuente, definición, autoridad, componente/selector, dirección, política, petición/Q cuando corresponda y trabajo restante. Un self-intersection con distintas direcciones requiere elección explícita; no se deduplica por imagen. Un seam se canonicaliza solo por la autoridad periódica vigente; no se aplica wrap a arcos abiertos. Las componentes puntuales, selector ambiguo, gap o inexistencia no se materializan por confirmación. La pérdida posterior de correspondencia deja el selector retenido y el punto indefinido conforme al contrato existente. Esta sección no amplía el arrastre general.

### 5.5 Superficie pública — nueva frontera de consumer, sin comando/formato nuevo implícito

> La herramienta experimental de creación puede consumir una petición world originada por clic o referencia Q y la admisión kernel aprobada de creación individual. Publica el normal form forward ya soportado; no añade una expresión dinámica L,Q ni un comando inverso público por inferencia. Cuando un consumer solo implementa las vías R1/R6 o R2/H1 debe declarar ese perímetro y conservar sus garantías. Un consumer de la nueva vía debe presentar el grado de proximidad real y requerir selección explícita; no fabrica permiso a partir de un residual o un UNRESOLVED. Su interfaz y feature flag no crean autoridad geométrica. La compatibilidad/lectores, copia/Undo y persistencia siguen consumiendo exclusivamente el estado forward existente, sin nuevo codec.

## 6. Matriz de viabilidad y validación de la decisión

| Clase/caso | Bajo contratos actuales | Bajo la decisión nueva propuesta, si se implementa y prueba |
| --- | --- | --- |
| Afín/SplineV2/R5 ya admitido por R6 | Conservar admisión y grado actuales | Igual; no reducir su prueba |
| General con varios candidatos R6 válidos, sin certificado C1 intervalar | Selección explícita ya admisible | Igual; prueba de regresión independiente |
| General con un candidato diagnóstico y H1 completo | R2/H1 ya admisible con confirmación | Igual, conserva CERTIFIED_ERROR_BOUND local |
| Modelo general v3 con componente certificada y forward reconstruible pero sin modelo intervalar H1 | Fuera de la nueva vía H1 individual | Candidato a la vía numérica, con grado declarado y todos los predicados; no afirmar que cualquier programa v3 ya pasa |
| Evaluadores escalares como exp/sin fuera de clase certificada que no aportan componente actual | Forward numérico finito no basta | Continúan fuera; no se amplía CU2 ni se inventan componentes |
| Partial global de existencia con componente local certificada y selector establecido | Depende del consumer actual; no inferir global COMPLETE | La nueva vía local puede admitir solo su componente y declarar cobertura global no establecida; sin cambiar el certificado |
| Self-intersection, varias direcciones claras | Elección explícita según su vía válida | Diferentes tuples, mismo punto cartesiano permitido; no unicidad cartesiana implícita |
| Periodicidad/seam con contrato certificado | Canonicalización/lift existentes | Igual; no nueva política de seam ni wrap |
| Derivada nula o no disponible | Puede impedir descubrimiento/admisión R6/H1; no invalida todo forward | No requisito diferencial universal; solo admisión si existencia/correspondencia/forward/seguridad se demuestran independientemente. No se promete cobertura de singularidades |
| Collapsed image/intervalo de dirección no elegido | Rechazo/degenerate actual | Se conserva fuera del primer perímetro; no semilla arbitraria |
| Componente puntual, gap, selector sin correspondencia, archivo/evaluador no reconstructible | Rechazo/undefined tipado | Continúa rechazado |
| Revisión/Q/policy cambia, budget se agota, Choice stale/foreign/consumed | Rechazo sin publicación | Igual, con pruebas propias de la nueva vía |

La prueba decisiva de la futura norma nueva debe separar (1) una dirección que pasa existencia/binder y falla únicamente la guarda C1/modelo H1; (2) una que falla existencia/lineage; (3) una que solo pierde proximidad numérica; (4) una certificada más fuerte. El primer caso podría crear solo tras adoptar la norma; los otros dos negativos no se vuelven admisibles para lograr cobertura. La referencia `TestM08-S.cedg` permanece regresión de H1, no ejemplo que por sí solo demuestre la generalización nueva.

Además: clic/Q con el mismo q y policy deben concordar en direcciones/evidencia semántica (no compartir capabilities); Q se mueve/elimina después de commit sin mover P por dependencia nueva; L sí propaga cambios hacia P y sus dependientes; copy/Undo/Redo/.cedg definido y dormante conservan dirección/selectores, no q. Es necesaria interacción gráfica real del nuevo clic; una consulta programática no la sustituye. Las pruebas de la norma nueva permanecen diseñadas, no ejecutadas en este anexo.

## 7. Inventario, límites y decisión pendiente

Leídos desde disco: ADR0019/0041; M08; interacción y superficie pública; existencia §§15.5–15.8; plan CU candidato y sucesor en filas M08/CU7; R6 arquitectura; diseños R2 local/refinement/normative bridge/H1 y entrada Desktop; código `LocusPointInteractionResolver2D`, `LocusSnapshotPlacement2D`, `M08SnapshotPlacement2D`, `M08LocalAddressEvidence2D`, `ConstructionIntervalModel2D`, `CertifiedConstructionProgram2D`. No se ha repetido investigación M09, ejecutado Java, alterado ADR aceptado o introducido producto. Los archivos/pruebas efectivamente cambiados y ejecutados por el integrador se informan aparte.

Decisión pendiente exacta: adoptar o rechazar INDIVIDUAL_FORWARD_ADDRESS_CREATION con **proximidad numérica no certificada permitida y C1 no universal**, manteniendo los ocho predicados; o elegir explícitamente la alternativa certificada puntual de §4.3. Mientras esa decisión no exista, solo se implementa/valida la integración clic bajo las garantías actuales. No se atribuye autorización a la mera selección del usuario ni se declara implementada la generalización sin C1.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED para este anexo: no cambia prerrequisitos/tooling/runtime. VERIFICATION_INFRASTRUCTURE_IMPACT=NONE. GUIDE_IMPACT=NONE para la propuesta no adoptada; la implementación autorizada de clic debe registrar su propio impacto de guía. M08-S-R2 permanece PASS — AUTHOR APPROVED; CU7 global, M09 y M08-D no se cierran por este documento.
