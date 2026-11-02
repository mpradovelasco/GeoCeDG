# PRE-G9B-CU7-INTEGRATED-REASSESSMENT — Informe de traspaso

Fecha: 2026-11-02, Europe/Madrid.

**Estado: ESTUDIO INCOMPLETO Y EN PAUSA.** Este informe conserva el encargo y los resultados parciales para continuarlo en otro agente Codex del proyecto GeoCeDG. No es el entregable final del estudio, una propuesta normativa adoptada, una autorización de implementación ni una aprobación autoral. `selfApproved=false`.

El autor pidió «Haz una pausa». Después solicitó este informe completo de petición y resultados. La actividad posterior a la pausa se limita al traspaso, a recuperar los hallazgos ya obtenidos y a comprobar su estado y sus referencias. No se ha reanudado la investigación sustantiva ni ejecutado implementación.

## 1. Petición que se estaba resolviendo

Título autoral: **GeoCeDG — PRE-G9B-CU7-INTEGRATED-REASSESSMENT. Análisis integral de viabilidad, dependencias y estrategia de implementación de CU7. ENCARGO AUTORAL — ESTUDIO PROFUNDO, SIN IMPLEMENTACIÓN PRODUCTIVA.**

El objetivo es estudiar conjuntamente todos los incrementos pendientes de CU7 para permitir **una sola decisión informada de implementación**, seguida de revisión independiente. No se solicita otra secuencia de investigaciones aisladas por incremento. El resultado debe determinar qué implementar ahora, qué agrupar por infraestructura real, qué separar por garantías independientes, qué diferir, qué descartar si existe alternativa mejor y bajo qué alcance podría cerrarse CU7 sin presentar diferimientos como implementación.

La recomendación de modelo del encargo fue «GPT-6.1 Sol — Ultra». No se cambió ni se comprobó el modelo efectivo de esta sesión; no debe atribuirse una selección no acreditada.

### 1.1 Alcance científico y técnico obligatorio

| Parte | Pregunta que debe resolverse |
| --- | --- |
| A. M09 singleton, I2a/I2b | Redefinir un punto existente con identidad durable compatible; definición, dependencias, registros, claims y consumidores. Atomicidad/rollback, cierre ordinary+durable, target sin ID, IDs prospectivos, productores/roles, evaluación no mutante, ABA, Desktop/álgebra/headless. |
| B. I2G | Redefinir una salida de productor multisalida conservando hermanos y consumidores. Demostrar un adaptador pequeño y completo o identificar invasividad real. No suponer que un output heredado se puede separar arbitrariamente. |
| C. I3 | Cambio coordinado de soporte/conductor, fuente y participantes. Clausura prospectiva, registros espaciales, vínculos y barreras de identidad F/T/transversal de CU3. Nunca continuidad por coordenadas iguales. |
| D. I4, M08-D | Correspondencia dinámica P respecto a Q y Locus V2 L mediante relación explícita, reconstruible y persistible. Primera clase matemática cerrada, garantías numéricas, identidad del mapa, DAG, codec/lectores, validez dinámica y alternativas CeDG equivalentes. |
| E. I5 | Cierre completo, cierre con alcance reducido aprobado, cierre parcial con puertas pendientes o CU7 abierto por bloqueo indispensable. No equiparar aplazamiento y completitud. |

La investigación transversal es el centro: snapshot previo a construcción; transacción atómica; reconstrucción prospectiva; validación coherente del DAG host y grafo durable; contratos de definición; roles/grupos/correspondencia; impacto completo; IDs/high-water; claims/ledgers/cuarentena; vigencia; persistencia/Undo/copia/lectores; límites kernel/upstream/capas externas; reutilización de M08-S, CU3 y G9A3.

Debe contestarse expresamente si hay **un subconjunto mínimo y cerrado de capacidades transaccionales útil para varias partes de CU7**, demostrar utilidad y límites, y evitar diseñar un framework general por anticipación. Si la solución exige reconstrucción general de GeoGebra, evaluar si compensa introducirla ahora.

### 1.2 Comparación y entregables finales aún exigidos

Comparar al menos A: CU7 completo; B: CU7 acotado; C: separación por viabilidad; D: cierre conservador. Para cada estrategia: valor científico/funcional, cobertura, upstream, contratos/formatos, acoplamiento, identidad/DAG/persistencia, documentos existentes, reutilización, verificación/smoke, riesgos, diferimiento, reversibilidad y deuda. Usar estimaciones relativas justificadas, sin tiempos exactos inventados.

El entregable final debe incluir:

1. Dictamen ejecutivo y recomendación preferente.
2. Matriz de todos los incrementos: estado, viabilidad, dependencias, invasividad, riesgos y destino.
3. Grafo con dependencias obligatorias, convenientes e históricas, separando matemáticas, identidad/persistencia, transacción/DAG, API, UI y autorización.
4. Arquitectura mínima y archivos/subsistemas reutilizables, con comparación upstream.
5. Paquete de implementación de alcance cerrado, preparado para una autorización y revisión posterior; puertas internas y contingencias sin ampliación automática.
6. Plan de pruebas focales, adversarias, integración, compatibilidad, smoke y aceptación.
7. Riesgos y contingencias.
8. Plan preciso de diferimientos: pérdida temporal, contratos conservados, operaciones rechazadas, deuda y condición de reconsideración.
9. Propuesta de cierre CU7 y obligaciones fuera del perímetro.
10. Prompt canónico completo para el agente ejecutor, listo para revisión/autorización. **No ejecutarlo.**

Cada funcionalidad pendiente debe recibir una de las categorías autorales: IMPLEMENTAR EN CU7; IMPLEMENTAR EN CU7 SOLO CON PERÍMETRO RESTRINGIDO; DIFERIR A FASE POSTERIOR; MANTENER BLOQUEADA HASTA RESOLVER UNA PRECONDICIÓN; DESCARTAR LA PROPUESTA CONCRETA POR EXISTIR UNA ALTERNATIVA MEJOR.

### 1.3 Autorizaciones y prohibiciones

Autorizado: rama de estudio descendiente del main verificado, inspección profunda e histórica, matemáticas/arquitectura, sondas aisladas reproducibles si resuelven una incertidumbre decisiva, informes/matrices/decisiones candidatas/prompt y verificaciones documentales o focales pertinentes.

No autorizado: código productivo; aplicar ADR normativos; alterar contratos aprobados, identidades o serialización; publicar ramas/promover main/releases; ejecutar el plan propuesto; modificar `geocedg_book`; reabrir M08-S-R2 u otros incrementos aprobados. Arrastre general Locus V2 y otras deudas quedan aparte salvo análisis de dependencia, sin implementación.

## 2. Estado físico y Git exacto

La carpeta inicial del chat no contenía el repositorio. Se localizó y verificó el repositorio real:

```text
Repositorio principal:
C:\DesarrolloyDatos\Areas\ProyectosNoFinanciados\CeDG\GeoCeDG

Worktree independiente creado para el estudio:
C:\Users\usuario\Documents\ChatGPT\Geometría Descriptiva y CeDG\cu7-integrated-study

Rama del estudio:
research/pre-g9b-cu7-integrated-reassessment

Base main/origin/main/HEAD del estudio:
eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17

Árbol base:
d62179e72dd2bb37226cba5956198809467cb7c1

origin:
https://github.com/mpradovelasco/GeoCeDG

upstream:
https://github.com/geogebra/geogebra.git

Upstream fijado en docs/upstream/BASELINE_COMMIT.txt:
9b93256b7df401ff056c37b502d82df4d72b1522
```

Se verificó `git ls-remote origin refs/heads/main`: devolvió el mismo `eea7f57...`, exit 0. La comprobación es de esta sesión; revalidar al retomar, no asumir inmutabilidad remota.

Se creó exclusivamente el nuevo worktree/rama con:

```powershell
git worktree add -b research/pre-g9b-cu7-integrated-reassessment `
  'C:/Users/usuario/Documents/ChatGPT/Geometría Descriptiva y CeDG/cu7-integrated-study' `
  eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17
```

Exit 0. El checkout incidental de `CLAUDE.md` es el archivo versionado ordinario del worktree; no se editó. No se creó ningún commit del estudio ni se hizo push, merge, tag o release.

En la comprobación para este traspaso, antes de crear este archivo: `git status --short` vacío; rama y HEAD/árbol seguían exactamente en la base; dos worktrees registrados, principal/main y estudio/research. No existían los anexos de investigación propuestos. Este informe será el único archivo nuevo del traspaso y quedará sin commit.

Los artefactos históricos ignorados permanecen en el repositorio principal: **no se copiaron al worktree**. En el worktree de estudio, `artifacts/` solo contenía su `README.md` versionado al comprobarlo.

### 2.1 Entorno y síntomas observados

PowerShell devolvió mensajes de `InitializeDefaultDrives`/FileSystem y, con perfil cargado, un error al inicializar Conda (`StandardOutputEncoding`). Se continuó con `login=false` y, para localizar directorios cuando hizo falta, enumeración .NET. No son defectos del proyecto y no se parcheó código ni entorno para ocultarlos.

En el repositorio principal, Git bajo ejecución restringida llegó a dar «must be run in a work tree» y `git -C ...` dio permiso denegado. Una ejecución con escalación gestionada completó `git status --short` y `git ls-remote` correctamente. La creación autorizada del worktree también usó escalación. En el worktree dentro de la carpeta del chat, Git funciona sin esa escalación. No interpretar los síntomas de permisos como regresión GeoCeDG.

`.github/copilot-instructions.md` no existe en la base inspeccionada. Se comprobó y no se sustituyeron sus instrucciones por una aproximación.

## 3. Autoridad vigente y sucesión temporal

Se leyó `AGENTS.md`. Orden relevante: código/pruebas/configuración/serialización actuales; specs/ADR aprobados; modelos/invariantes; documentación/upstream fijado; investigación; artefactos; conversación. Los artefactos son evidencia, no autoridad para modificar semántica.

| Momento | Disposición y consecuencia |
| --- | --- |
| 2026-10-29 | CU7-D aprobado y contrato prospectivo M08/M09 aceptado sobre `7e7a90e4e39db04ea49ee9ab1acf69ea34c81080`, árbol `25e0e2f0a78670c31be39766021d3f7561bc3bf6`. Autorización histórica limitada a I2a/I2b. No acredita implementación. |
| 2026-10-30 | Disposición operativa en `docs/architecture/pre_g9b_cu7_i_increment_proposal.md:3`: R3-E/A0 aceptado solo como caracterización; no demuestra transacción segura. M09/A0 no se reanudan; se prioriza I1 M08-S desde main `caf67ffc...`, independientemente de M09. |
| 2026-11-02 | Enmienda sucesora ADR0041, desde línea 158, y cierre canónico M08-S-R2: **PASS — AUTHOR APPROVED** exclusivamente para M08-S-R2. M09 productivo continúa bloqueado; I2G/I3/M08-D y cierre global no aprobados. |
| Encargo actual | Autoriza análisis integrado y experimentos no productivos. No revoca los límites para implementar. |
| Pausa y traspaso | Investigación en pausa por instrucción expresa; se conserva el estado para otro agente. |

Fuentes decisivas:

- `docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md:158` y `:175`.
- `docs/architecture/pre_g9b_cu7_i_increment_proposal.md:3`, `:131`, `:140`, `:144`.
- `docs/validation/pre_g9b_cu7_i1_m08s_r2_closeout_record.md`.
- `docs/validation/pre_g9b_cu7_d_closeout_record.md`.

Los encabezados históricos que aún dicen I2a/I2b autorizado no permiten ignorar las disposiciones posteriores. La fila inicial «Disposición CU7 vigente» de `docs/roadmap/geocedg_roadmap.md:8` conserva un checkpoint anterior de M08-S; el sucesor fechado, también en `:4347`, prevalece. Se detectó esta incoherencia documental, pero **no se editó**.

M08-S-R2 fue aprobado sobre candidato técnico `86438edabaed06a6509c1be37cf06278941e0e46`, árbol `96e90172201440739bfe8afb503059379c70ff70`, con dictamen independiente y smoke autoral favorables. Su cierre registra STATIC/INFRA_UNIT/PHASE/INTEGRATION accepted/complete, exit 0; 196 ejecuciones PHASE, tres pruebas nativas y once omisiones históricas. Son resultados históricos del candidato indicado, **no pruebas repetidas en este estudio ni resultados del árbol de estudio**. No atribuir al smoke autoral pasos individuales no documentados.

## 4. Investigación realizada y evidencia histórica disponible

Se dividió la lectura en tres análisis paralelos: M09/I2G/transacciones e historia; I3/M08-D/CU3; gobernanza/verificación. La lectura principal cubrió autoridad, G9A3/P3-R1, dependencias durables y costuras host. Los tres análisis fueron interrumpidos al pausar. Para este traspaso se solicitó únicamente resumir lo ya obtenido, sin nueva investigación o escritura de anexos.

Ramas históricas locales encontradas:

| Rama | Commit |
| --- | --- |
| `phase/pre-g9b-cu7-i2-singleton` | `ed7813643d3450bb4482164e36e060788a29020c` |
| `phase/pre-g9b-cu7-i2-r2-diagnostic` | `c6e0aa98a2bb3308bf3ebce2cf6211a1198ec8d1` |
| `phase/pre-g9b-cu7-i2-r3-design` | `2981b13e0cb7eefe05ff218f58a5949f61bfc13b` |
| `codex/pre-g9b-cu7-i2-r3-e-a0` | `249934d09cb313e39f36b0dd79d288b7c0b800d7` |

Se pueden inspeccionar sin checkout, por ejemplo:

```powershell
git show codex/pre-g9b-cu7-i2-r3-e-a0:docs/validation/pre_g9b_cu7_i2_r3_e_a0_experiment_report.md
git show codex/pre-g9b-cu7-i2-r3-e-a0:docs/architecture/pre_g9b_cu7_i2_r3_bounded_transaction_design_candidate.md
```

Ubicación histórica local:

```text
C:\DesarrolloyDatos\Areas\ProyectosNoFinanciados\CeDG\GeoCeDG\artifacts\pre-g9b-cu7-i2\
  r1\
  r2\
  r3-d\
  r3-e\
```

`r3-e/instrumented-run4/` conserva `executed-inputs.json`, `executed-probe.java`, `process.json`, `changed.ggb`, `N0.xml`, ledgers antes/después P02, snapshots P08 en AFTER_RETAIN/AFTER_RELEASE/AFTER_POINT_WRITE y Undo antes/después P09. Se comprobó su existencia. En este traspaso se leyó también `process.json`: COMPLETED, `inner_exit_code=0`, ejecución histórica del 2026-10-30, logs en `instrumented-run4/process/stdout.log` y `stderr.log`. Esto acredita la procedencia del proceso; no convierte los oráculos negativos en puertas positivas.

Los registros de cierre y auditorías M08-S-R2 están en los directorios de `artifacts/` del repositorio principal, incluido `pre-g9b-cu7-i1-m08s-r2-closeout`. No se reabrió ese cierre.

### 4.1 Integridad histórica contrastada por el análisis M09

El análisis paralelo leyó la fuente archivada, JUnit y manifest de copias instrumentadas. Comprobó que el SHA-256 de `instrumented-run4/executed-probe.java` es `a4ef293aba5c38c7e4139cfa464860fc3b7d2a5ff00b62e39a6705df3d1d2f46`, coincidente con `executed-inputs.json`; las cuatro copias instrumentadas coinciden con sus `generated_sha256` en `r3-e/instrumented-sources/manifest.json`.

Sus blobs base coinciden con la base main actual:

| Clase | Blob Git base/actual |
| --- | --- |
| AlgoLocusIntersectionPointV2 | `d8ba2da6426f7975b2aaa389a20da43d034ff17f` |
| SpatialIdentityRegistry | `8092c03c03130ef12edde405976e0de0e26a7d58` |
| MyXMLHandler | `bd9e939c9755a43af7bd1ab6029feb18c1bb6284` |
| LocusIntersectionTokenLedger2D | `51b9a4ceab00471fcdf670b5b43bb05f2d9920e1` |

También contrastó igualdad R3-D/actual para Construction, UndoManager, Kernel, GeoPoint, GeoText, GeoLocusIntersectionResult, AlgoElement y AlgoIntersectLineConic. `LocusV2PublicOperations` sí cambia (+40/−6): el diff inspeccionado corresponde a creación M08-S; las rutas `selectIntersectionPoint`, `ParticipationBatch.prepare` y promoción del literal examinadas no cambiaron en ese delta. Esto refuerza la pertinencia de los mecanismos negativos históricos, sin acreditar una nueva ejecución completa sobre main.

Referencias históricas adicionales en la rama R3-E:

- `docs/validation/pre_g9b_cu7_i2_r1_guarantee_demonstration_report.md`: runtime/límites desde línea 49; G-T0–G-T3 desde 97; high-water desde 113; matriz C01–C27 desde 150.
- `docs/validation/pre_g9b_cu7_i2_r2_diagnostic_experiment_report.md`: reconstrucción desde 136; ABA desde 157; provisionales desde 176; cierre mixto desde 205; restauración desde 227; matriz desde 250.
- `docs/architecture/pre_g9b_cu7_i2_r3_bounded_transaction_design_candidate.md`: A0 desde 124; cierre desde 178; escritores desde 202; transacción propuesta desde 243; publicación/entrega desde 276; IDs desde 291; inventario desde 359.
- `docs/validation/pre_g9b_cu7_i2_r3_e_a0_experiment_report.md`: parada desde 13; resultados desde 131; puertas desde 171; comandos desde 187.

En `executed-probe.java`: P01 en 39; P02 en 52; ABA en 143; View/reentrada en 155; fallos de claim en 180; Undo/fatal en 221; Undo/ZIP en 244; copia en 276; setter+cascade en 378. Son referencias a la fuente archivada, no al árbol actual de tests.

## 5. Hallazgos parciales M09 / I2a-I2b / I2G

### 5.1 Dictamen provisional que ya emerge

**No existe todavía evidencia suficiente para recomendar como segura la reanudación productiva del paquete singleton, ni siquiera del A0 más reducido.** No es una prueba de imposibilidad de cualquier diseño futuro. Sí refuta atajos concretos y obliga a no llamar «bajo impacto» al singleton sin demostrar su envolvente.

Las tres identidades continúan separadas: ID durable del objeto P; contrato de definición/productor/roles/padres; token de raíz propiedad del ledger de R. Cambiar P hacia otra raíz puede conservar P solo bajo predicado compatible; no transporta identidad entre raíces. DA-5 no exige coincidencia geométrica.

### 5.2 Evidencia negativa decisiva de R3-E/A0

El informe histórico y la revisión paralela identificaron:

| Sonda | Hecho observado y límite |
| --- | --- |
| P01 | El literal GeoText con identidad durable resulta etiquetado por la fábrica pública. No satisface la fixture A0 estricta de literal durable sin etiqueta. No se puede quitar la etiqueta o cambiar la fixture retrospectivamente y seguir llamándolo la misma puerta. |
| P02 | Cambiar token dentro del mismo R actualiza mecánicamente P, claims y dependientes de la fixture sin allocator/loader/nueva evaluación del ledger observado. No actualiza contrato y revisiones semánticas como commit M09. Es costura útil, no transacción demostrada. |
| P03 | Compartir T entre consumidores cambia ambos. Independencia/etiqueta no prueba exclusividad del selector. |
| P04/P05 | Tokens inexistentes/ajenos se pueden detectar mediante admisibilidad; un setter sin preflight puede dejar P indefinido. Pérdida y recuperación dinámica del mismo selector no equivalen a rechazo transaccional. NaN de entrada y shell rica undefined son casos distintos. |
| P06 | El assessment existente sigue current tras ABA numérico y de GeoPoint cuando el snapshot final vuelve a coincidir. No demuestra vigencia de un nuevo contexto M09/A0. |
| P07 | Una View real observa T=t2 mientras el claim `registered` aún es t1; desde callback se realizan escrituras ajenas. La cascada ordinaria no demuestra aislamiento ni exclusión de reentrada. |
| P08 | Las inyecciones reales alcanzan fronteras de retención/liberación/escritura. El wrapper XML recupera datos comparados, pero entra en loader, cambia la instancia P y avanza epochs +2. No es restauración completa de S0 requerida por A0. |
| P09 | Fallo tras `storeUndoInfo`: XML vuelve pero contenido/cursor de Undo no se restituyen. Fallo en restauración LOAD_BEGIN alcanza ruta fatal. |
| P10/P11 | Undo/Redo, ZIP y copia ordinarios tienen éxitos comprobados para las fixtures. No prueban publicación transaccional conjunta ni el protocolo M09. |

Según el informe histórico, instrumented-run4: 18 casos, 17 ejecutados, 0 fallos/errores, 1 omitido intencional; baseline-run1: dos métodos, 0 fallos/errores/omitidos. El éxito JUnit es compatible con probar fallos de arquitectura. Las puertas A0-G1/G2/G4 quedaron parciales y G3/G5 no establecidas. No se promovió ninguna por ese exit 0.

También se conservan intentos históricos: permiso Gradle/lock; errores del arnés; y un run con oráculos fallidos luego corregidos. No se transformaron en cambios productivos.

### 5.3 Contraste con código actual

`Construction.runAtomicConstructionMutation`, `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java:2700`, captura XML y recarga en fallo (`:2719`). Su contrato dice expresamente que no almacena Undo; el llamante lo agrupa. No constituye por sí solo snapshot de todo el estado de seguridad, fuente de IDs, escritores o historial Undo.

`Kernel.notifyUpdate`, `.../org/geogebra/common/kernel/Kernel.java:4050`, llama síncronamente `view.update(geo)`. Un lock que los setters no consultan no demuestra cercado de todos los escritores. Suspender vistas no basta para probar exclusión semántica.

`SpatialIdentityRegistry` contiene mapas de issued/reserved/retired, snapshots locales, lifecycle y assessment. No inferir que un snapshot interno de un switch equivale a snapshot de toda la operación host. El allocator ya devuelve IDs reales; símbolos privados prospectivos no son automáticamente una API existente.

Para el traspaso se contrastaron por Git R3-D y la base actual en estas seis fuentes:

```text
source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java
source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java
source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java
source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java
source/shared/common/src/main/java/org/geogebra/common/main/undo/UndoManager.java
source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java
```

`git diff --quiet 2981b13... eea7f57... -- <seis rutas>` terminó con exit 0: no hay diferencias en ese conjunto. Se verificó además existencia del algoritmo de punto con `git cat-file -e`, exit 0. Esta equivalencia **es limitada a seis archivos**; no certifica entorno, cierre de llamadas, toda la instrumentación histórica ni ejecución sobre main actual.

### 5.4 Transiciones y multisalida

- Ordinary sin ID → token añade el problema de incorporar identidad nueva solo al commit y de reconstruir dependencias ordinarias. No hay identidad anterior que «retener».
- Ordinary/semantic-address con dependientes → token no se resuelve declarando igualdad de clase POINT. Requiere correspondencia real de productores/helpers y preservación de consumidores, incluidos anónimos.
- Token → token mismo R tiene costura mecánica real, pero falla como transacción completa A0 demostrada.
- Token → otro R añade rewire de richInput, padres y claims; no es el mismo A0.
- Structural CU3, interacción, principal y projection-defined requieren sus contratos/roles; no convertirlos a VALUE por clase Java.
- Un productor multisalida y sus descendientes deben enumerarse enteros. La participación estable por roles de G9A3 no es una API genérica para separar un output de `AlgoIntersectLineConic`.
- I2G necesita mapping completo de productor, hermanos, outputs internos y consumidores; persistencia, P3-R1 y fallos del conjunto. No se demostró un adaptador pequeño generalizable.

Límite concreto I2G inspeccionado: `AlgoIntersectLineConic.java` conserva arrays P/Q, `permutation`, D[], estado alive, incidencias y memoria near-to. Crea y registra el conjunto de outputs en líneas 209–243 y escribe todas las P mediante permutación en 519–521; continuidad/singularidad en 542–603. Mantener P dentro del array permite que el productor la siga escribiendo; retirarla requiere preservar la salida geométrica interna y mapear el conjunto. `AlgoElement.removeOutputExcept` elimina hermanos (`:877`); `Construction.prepareReplace` llama esa operación (`:3687`, referencia aproximada que debe comprobarse antes de enlazar). El gate multisalida tiene que precederla.

R2 también identificó ausencia de ticket causal del loader para reconstruir correspondencia total entre productores anónimos LINE sin usar etiquetas, orden o coordenadas como autoridad. Su fixture de ocho geos host y cuatro registros no demuestra el universo completo de lecturas/efectos. No confundir el high-water `nextIncarnation` del ledger con issued/retired/fuente de IDs del registry: son estados y obligaciones distintos.

Fuentes actuales para profundizar: `SpatialIdentityRegistry.assessRedefine` (`:5040`), currentness (`:5219`, XML de estado en `:5369`), `buildLegacyImpactReport` (`:5413`, cierre durable conocido, no universo ordinary completo), validación de DAG por versión (`:6615`) y `RegistryState` (`:3183`). `AlgoLocusIntersectionPointV2.synchronizeMaterializedClaim` (`:135`) es una secuencia que requiere coordinación exterior; richInput/tokenInput/point son finales. Las líneas corresponden a la lectura del equipo; comprobar anclas exactas al editar.

Tendencia provisional: M09 productivo **MANTENER BLOQUEADO HASTA RESOLVER PRECONDICIONES**; I2G **DIFERIR**, sin declarar M09 completo. La clasificación final y comparación integrada aún faltan.

## 6. Hallazgos parciales I3: soporte, conductor y CU3

`AlgoDependentPointLocusV2.java:73` en el paquete `org/geocedg/common/kernel/algos` conserva campos finales `dependentPoint`, `state`, `coordinate`, `support`, dominio y rama de soporte. La dependencia durable y la evaluación usan esos participantes. Cambiar solamente `N.path` no reconstruye el soporte capturado por L.

La caracterización CU7-D ya había observado `MALFORMED_RECORD` / `GEO_ATTACHMENT_MISSING` y un éxito condicionado a participación previa del cierre. Un caso transversal positivo condicionado no demuestra cambio coordinado general ni barreras completas F/T.

Precisamente, `pre_g9b_cu7_d_point_placement_and_redefinition_design.md` §4 refinó una conclusión histórica de rechazo universal: el nuevo conductor podía admitirse sin cierre materializado; con K5 y sin participación nueva aparecía `GEO_ATTACHMENT_MISSING`; tras preregistrar `K=LocusV2(X2,N2)`, retenía L y el objeto claim, pero el claim quedaba indefinido y el token previo no admisible. Cambiar soporte de N incluso ya participante podía seguir fallando por discrepancia registro/DAG. No generalizar esos casos a F/T, persistencia o cambio coordinado completo.

Referencias para continuar: `SemanticGeneratorDescriptor1D.java:25`–`:42` incluye participantes durables y firma; `AlgoDependentPointLocusV2.java:625`–`:631` crea descriptor con IDs y `:765`–`:789` declara inputs; `PublicIntersectionRootIdentityResolver2D.java:38`–`:47`, `:79`–`:82` y `:1124`–`:1129` separa selector intrínseco y contratos; `SemanticGeneratorDomainProvider1D.java:32`–`:34` incorpora coordenada verdadera. Son elementos a contrastar con K03–K05, no prueba positiva de barreras I3.

Deben distinguirse: cambio geométrico en sitio del mismo soporte; nuevo soporte de N; nueva fuente/conductor bajo el mismo constructor; cambio de lineage. Ordinary refresh de durable-frontier §10 no se puede ampliar a fuentes públicas para compensar el problema.

La barrera de raíces se evalúa separadamente de conservar N/L/P. Geometría igual con IDs de soporte distintos no autoriza reutilizar token. Para cada F, T y transversal se deben comparar selector completo, procedencia, familia y revisiones. Retorno geométrico legítimo del mismo selector no equivale a retiro permanente. Split/merge, seam, orientación y contacto→pareja mantienen ADR0017/0039/0040.

Tendencia: I3 comparte cierre prospectivo/atomicidad con M09, pero requiere además su propio contrato de correspondencia y barreras. **No está demostrado un subconjunto transaccional común suficiente**. La recomendación definitiva y análisis completo de K01–K05 están pendientes.

## 7. Hallazgos parciales M08-D y alternativas

### 7.1 Primera clase matemática

Para una componente finita cerrada `[a,b]` y una receta afín demostrada `F(u)=A*u+B`, `A≠0`:

```text
u0 = dot(A, Q-B) / dot(A,A)
u* = clamp(u0, a, b)
P  = F(u*)
```

`d²/du² ||F(u)-Q||² = 2 dot(A,A)>0` prueba unicidad del mínimo en ese intervalo cerrado. Es derivación matemática, no prueba de que cualquier objeto actual llamado CertifiedAffine suministre la receta ideal, su no colapso, dominio y correspondencia bajo la política documental.

Extremo abierto puede tener ínfimo no alcanzado. Colapso A=0, dominios no finitos, varias componentes, pliegues, singularidades y seams necesitan políticas/evidencia propias. Una semilla histórica no elige mapa ni raíz. El codec dinámico debe reconstruir identidad y padres L/Q sin depender del último u.

La revisión encontró que `CertifiedAffineLocus2D` actual ofrece evidencia afín flotante para casos concretos (direct scalar y similitudes), no el certificado más fuerte de proyección ideal exigido por M08-D. No hay codec/proveedor aprobado de M08-D que satisfaga ya D01–D08 y lectores.

Anclas: `CertifiedAffineLocus2D.java:8`–`:23` contiene coeficientes `double[]`; `ReconstructibleLocusEvaluator2D.java:723`–`:765` restringe la clase directa a escalar o `MyDouble` finito; `LocusSimilarityEvaluator2D.java:275`–`:286` propaga certificados por similitud. Coste provisional estimado del proveedor afín cerrado + codec/lectores/DAG/lifecycle: **medio-alto**, separado de la reconstrucción general M09. Es estimación, no medición ni contrato aprobado.

### 7.2 Alternativas reales, con límites

**CU3 `Point(L,D)`** ya da correspondencia dinámica por procedencia en su tabla cerrada y mismo soporte real. Reutilizarla cuando corresponde evita una inversión nueva. No cubre Q arbitrario ni se establece por coincidencia de coordenadas.

`AlgoLocusDriverCorrespondencePoint2D.java:56`–`:75` registra padres y reevalúa vía StructuralPointResolver; `StructuralPointResolver.java:136`–`:192` exige política/familia y procedencia, y `:359`–`:399` comprueba mismo objeto soporte y las construcciones admitidas. Las pruebas `StructuralPointMatrixTest` de soporte incompatible, extremos e interior se inspeccionaron por lectura; no se ejecutaron.

**Composición explícita `Q → u → Point(L,branch,u)`**: `AlgoSemanticLocusPoint2D` acepta `GeoNumberValue` dependiente y registra como inputs source, branch y parameter (`:96`–`:101`). Por tanto, para una construcción que ya conoce y comparte A/B/dominio con L, puede escribirse la fórmula de u con objetos algebraicos existentes y el punto forward. Hay dependencia auténtica de Q y normal form existente, sin que eso cree un proveedor M08-D universal.

Límite crucial: `ConstructiveClassTable.java:92`–`:94`, paquete `locus/constructive`, clasifica Min/Max/ClosestPoint como `CONDITIONED_SELECTION`. La composición con clamp no adquiere STRICT automáticamente bajo constructive-authority. Tampoco certifica por sí misma afinidad, identidad del mapa, validez al redefinir L o todo el contrato D01–D08/U04. Una construcción perpendicular/intersección de CU3 solo evita parte de ello si satisface su procedencia, soporte y componente.

Tendencia: descartar un nuevo comando/proveedor solo para duplicar un caso manual ya compuesto; diferir M08-D certificado general hasta proveedor/receta/lector demostrados. **No declarar M08-D implementado por estas alternativas.**

## 8. Arquitectura transversal: lo establecido y lo pendiente

| Necesidad | Reutilización comprobada | Límite pendiente |
| --- | --- | --- |
| Registry/runtime prospectivos | `SpatialLifecycleTransaction`, mutation/prospective graph, switches G9A3 | No coordinador general de estado host/Undo/escritores. |
| Assessment y modo explícito | `SpatialRedefineAssessment`, contexto, RETAIN/FRESH/REJECT y modos | ABA, target sin ID y cierre nuevo no resueltos por presencia de la API. |
| Dependencia durable | `DurableDependencyProjection`, reglas por schema 1/2, overlay candidato | Frontier no equivale al impacto ordinario completo ni a correspondencia de productores anónimos. |
| P3-R1 | `clamp(oldSlot, predecessorBound, dependentBound)` y orden relativo no afectado | Requiere mismos testigos de entrada y productor/grupo completo; no cualquier orden topológico. |
| Roles y grupos | Mapas stableOutputRole de G9A3 | No autorizan detach ni cardinalidad arbitraria de algoritmos heredados. |
| Claims/ledger | Algoritmo de punto-token y ledger actuales | Setter+cascade expone prefijos; hace falta publicación conjunta demostrada. |
| Rollback | XML y rollback lifecycle existentes | XML no restituye automáticamente Undo, identidad de instancia, fuente/issued y todo S0 de A0. |
| Vigencia M08-S | Selección de uso único y revalidación R2/H1 aprobadas | No transportar su éxito a redefiniciones host con nuevos escritores y callbacks. |
| Persistencia | Normal forms y codecs existentes, reglas de copia completa | M08-D nuevo necesita receta/versionado/lectores; grupo nuevo puede necesitar contrato propio. |

Conclusión transversal parcial: hay piezas compartidas útiles; todavía **no se ha demostrado un paquete mínimo cerrado suficiente para implementar conjuntamente M09/I2G/I3**. Diseñar mementos/cercado/Undo general sin resolver esto sería anticipar un framework. M08-D tiene un bloqueo científico y de formato independiente de la reconstrucción M09, y las alternativas forward no dependen de resolverla.

El antiguo orden I2→I1 es histórico: M08-S ya está aprobado e implementado. No volver a convertir M09 en prerrequisito de una capacidad completada.

Como orientación, empezaba a favorecerse una combinación C/D conservadora: preservar M08-S y capacidades ya aprobadas, rechazar la promesa de «singleton barato», registrar diferimientos precisos y evaluar cierre reducido mediante decisión autoral. **No es todavía dictamen final**: falta comparar A–D con todos los criterios y construir una actuación ejecutable coherente. Tampoco hay autorización para adoptar reducción de alcance.

## 9. Verificación: trabajo realmente ejecutado y plan pendiente

### 9.1 Autoridad y perfiles

`tools/agent/verify.ps1`, registro tipado y esquemas son autoridad ejecutable; se leyeron los prompts canónicos de gobernanza/verificación. La parte inicial de recuperación de `verification-levels.md` es activa; no reactivar reglas archivadas por leer un template histórico.

`DEV` está INCOMPLETE (`dev.focused-semantic-selection`); `OPERATIONAL` también (`operational.remaining-pure-coverage`). No usarlos como selección focal completa ni modificar infraestructura para hacer viable el estudio.

Hay fases registradas G9A3, POST-G9U1-A3/frontend, PRE-G9B-CU3, G9U0-R6, PRE-G9B-R6 y M08-S-R2-Desktop, entre otras. **No existe una fase M09/I2 o CU7 integrada registrada**. Antes de implementación futura habría que definir y registrar su selección por el procedimiento canónico. No inventar un ID ejecutable o sustituirlo por M08-S.

### 9.2 Únicas resoluciones nuevas realizadas

El análisis de verificación ejecutó, dos veces para lectura y luego comparación en memoria:

```powershell
& tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly
& tools/agent/verify.ps1 -Profile FINAL -PlanOnly
```

| Resolución en la base | Exit | Nodos | execution_plan_hash |
| --- | --- | --- | --- |
| INTEGRATION PlanOnly | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |
| FINAL PlanOnly | 0 | 68 | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` |

Todos los identificadores INTEGRATION están en FINAL: `missingIntegrationInFinal=[]`. La implementación del registro elimina duplicados de IDs dentro del mismo plan. Se puede estudiar una única FINAL compuesta que cubra integración tras el desarrollo, volviendo a comprobar la contención después de futuros registros. Esto evita duplicar campañas, **sin rebajar gates ni atribuir ejecución a un plan**.

No se guardaron logs ni JSON de esos PlanOnly en disco; quedaron solo en salidas de herramientas de la conversación. No hay receipt. No fueron ejecuciones de aceptación. No se ejecutaron pruebas de producto, experimentos Java nuevos, STATIC, INFRA_UNIT, PHASE, INTEGRATION ni FINAL en este estudio.

### 9.3 Matrices que debe integrar la continuación

Fuente: `docs/validation/pre_g9b_cu7_d_validation_and_implementation_gates.md`. Son obligaciones, no evidencia de PASS.

- R01–R16: M09; identidad sin coincidencia, claims, candidatos ricos/token, inadmisibles, integridad CU3, roles, target sin ID, grupos, P3-R1, ciclos, degeneración downstream, stale/cancel, fallos/rollback, impacto ordinary+durable y rechazo de operación I3 nueva.
- K01–K05: I3; cierre coordinado, conductor/familia, barreras F/T/transversal, cambio en sitio/recuperación, split/merge/orientación/seam/claims.
- D01–D08: M08-D; clase afín y extremos, independencia de historia, ambigüedad, singularidad, pérdida/recuperación, autoridad/integridad y estabilidad numérica.
- U01–U06: persistencia/Undo, copia de cierre, delete/recreate/ABA, lectores, reutilización por revisión y regresiones.
- Regresiones obligatorias a conservar: G9A3, CU3, R6 y M08-S-R2, además de lo exigido por registry y delta real.

Falta separar cobertura común/específica y diseñar campaña focal de desarrollo + campaña final compuesta sobre candidato congelado. No repetir una campaña completa por cada cambio pequeño ni omitir una prueba porque otra capacidad está bloqueada.

## 10. Cierre CU7: límites ya claros

- Un eventual singleton positivo seguiría siendo **M09 PARCIAL — I2G pendiente**. Eso está declarado expresamente en autoridades previas.
- I5 puede revisar un perímetro implementado; no borra funcionalidades diferidas.
- Cerrar globalmente CU7 con reducción de alcance exige decisión autoral expresa que sustituya el alcance anterior y conserve exclusiones/deudas identificadas. El agente no puede deducirla del coste ni del diferimiento.
- Sin reducción aprobada, puede mantenerse M08-S cerrado y CU7 global abierto, o cerrar otro perímetro puntual con nombre exacto; no PASS global ficticio.
- CU10, integración de guía, expediente E1, `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`, obligaciones editoriales y `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG` siguen separados. Libro intacto.

## 11. Archivos y subsistemas inspeccionados

Inventario principal; no significa lectura exhaustiva de todo el repositorio ni ejecución de todas las pruebas. Varias lecturas produjeron salida truncada; el agente siguiente debe leer las secciones exactas necesarias antes de razonar/editar.

Autoridad y planificación:

- `AGENTS.md`; ausencia comprobada de `.github/copilot-instructions.md`.
- `.github/prompts/canonical/governance.prompt.md`, `verification.prompt.md`, `tasks/task-template.prompt.md`.
- `docs/upstream/BASELINE_COMMIT.txt`, configuración Git y ramas/worktrees.
- ADR0026, ADR0041, ADR0017, ADR0039, ADR0040; ADR0010/0011/0029 como referencias relacionadas y lectura de apoyo del equipo.
- `docs/architecture/pre_g9b_cu7_i_increment_proposal.md`, `pre_g9b_cu7_d_point_placement_and_redefinition_design.md`.
- `docs/architecture/g9a3_spatial_lifecycle_migration_design.md`, `post_g9u1_a3_v2_compatible_redefine.md`.
- Cierre M08-S-R2, closeout CU7-D, matriz gates CU7-D y roadmap.
- Contratos M08/M09 en `geocedg/specs/locus/candidates/`.
- `geocedg/specs/spatial/durable-dependency-projection.md`, contratos Locus V2/public-surface/existence/intersections/constructive-authority y correspondencia CU3.
- `geocedg/specs/operations/verification-levels.md`, registry/esquemas y módulos de resolución.
- Informes/diseños/sondas históricas R1/R2/R3-D/R3-E/A0 desde ramas; artefactos locales indicados.

Código principal:

- `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java`.
- `.../org/geogebra/common/kernel/commands/AlgebraProcessor.java`.
- `.../org/geogebra/common/kernel/Kernel.java`, `algos/AlgoElement.java`, `algos/AlgoIntersectLineConic.java` y `main/undo/UndoManager.java` en sus rutas reales bajo `org/geogebra/common`.
- `.../org/geocedg/common/kernel/spatial/identity/`: Registry, LifecycleTransaction, RedefineTransaction/Context/Assessment/HostState/ImpactReport, ConstructionGeoRedefineProvider, DurableDependencyProjection y contratos asociados.
- `.../org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java`, `AlgoSemanticLocusPoint2D.java`, `AlgoDependentPointLocusV2.java`.
- `.../org/geocedg/common/kernel/locus/LocusV2PublicOperations.java`, `CertifiedAffineLocus2D.java`, constructive/ConstructiveClassTable.java y ledger de intersecciones.
- Pruebas G9A3/A3/R3, CU3, M08-S y redefinición upstream se localizaron/consultaron como mapa; no se ejecutaron.

Durante la exploración se intentaron inicialmente nombres de ADR/path que no existían; se corrigieron con `rg --files`. Los nombres válidos relevantes son `0017-deterministic-intersection-phase-rank-identity.md`, `0039-locus-v2-structural-points-and-certified-contact-evidence.md` y `0040-constructive-geometric-authority-and-relation-integrity.md`. `LocusV2PublicOperations` está en `kernel/locus`, no `kernel/commands`. Los tests shared pertinentes están en `source/shared/common-jre/src/test`, no `source/shared/common/src/test`.

## 12. Lo que no llegó a hacerse

No existen aún:

- Informe integrado final y matriz comparativa A–D completa.
- Grafo integrado de dependencias y caminos críticos suficientemente contrastado.
- Demostración positiva de subconjunto transaccional cerrado.
- Adaptador pequeño I2G demostrado.
- Contrato I3 prospectivo completo y comprobación exhaustiva F/T/transversal.
- Certificado/codec M08-D ni validación completa de las composiciones alternativas.
- Paquete recomendado definitivo listo para autorizar, con contingencias internas.
- Matriz integrada de pruebas y plan de diferimientos cerrado.
- Propuesta final de cierre CU7 y prompt canónico de implementación.
- Anexos que se habían encargado a agentes (`pre_g9b_cu7_integrated_m09_evidence.md`, `pre_g9b_cu7_integrated_i3_m08d_evidence.md`, `pre_g9b_cu7_integrated_governance_verification.md`): **no fueron escritos antes de la pausa**.
- Commit ni validación documental canónica del estudio.

No se debe convertir este traspaso en el estudio final ni tratar la orientación C/D como una decisión adoptada.

## 13. Secuencia recomendada para retomar sin perder trabajo

1. Leer este informe y el encargo autoral original; leer AGENTS y autoridad vigente desde disco. Revalidar main remoto, base, árbol, estado y rama. No cambiar el worktree principal.
2. Preservar este informe no comprometido; decidir su incorporación documental junto al estudio, sin tocar código o contratos aprobados.
3. Recuperar informes y fuentes históricas con `git show`; vincular las afirmaciones a casos/procesos/snapshots reales y hashes. No repetir sondas cuyo resultado negativo ya decide la precondición salvo que cambie una hipótesis concreta.
4. Completar la auditoría de impacto ordinary+durable y reconstrucción prospectiva. Distinguir mapear dependencias de certificar todos los efectos; no ampliar booleanos de completitud históricos.
5. Terminar I3 y M08-D como partes del mismo grafo, separando garantías comunes de las que solo se parecen. Resolver valor/coste de alternativas existentes sin rebautizarlas como M08-D certificado.
6. Comparar A–D y formular una recomendación única. Si ninguna extensión productiva tiene garantías suficientes, justificar una actuación conservadora concreta y ejecutable; no inventar una implementación «mínima» para cumplir un formato.
7. Redactar los entregables 1–10 de §1.2, incluida clasificación precisa por capacidad, deudas y prompt para autorización posterior. No ejecutar dicho prompt.
8. Validar el estudio documental con el wrapper canónico y clasificación aplicable; preservar comando, exit, logs e identidad. Si se requiere congelar candidato, hacerlo en la rama de estudio, sin push ni promoción y sin autoatribuir aprobación. No lanzar campañas de producto sin justificación/autorización aplicables.
9. Detenerse al entregar estudio y evidencias para revisión autoral/independiente.

## 14. Estado de este traspaso

Archivo nuevo: `docs/validation/pre_g9b_cu7_integrated_reassessment_handoff.md`. Capa: documentación de investigación/traspaso. No hay efecto semántico, cambio de runtime, codec, identidad, contrato, guía de usuario ni infraestructura.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no cambia ningún requisito de instalación, toolchain o runtime. VERIFICATION_INFRASTRUCTURE_IMPACT=NONE. GUIDE_IMPACT=NONE para este traspaso; las obligaciones futuras de CU10/guía permanecen.

Validación de producto: NO EJECUTADA. STATIC canónico: NO EJECUTADO. Este archivo no acredita aceptación de CU7. La comprobación de formato y el estado Git del traspaso se comunican por separado al entregarlo; no se inventa log o receipt.

Comprobación de formato: el primer intento `git diff --no-index --check -- NUL <informe>` devolvió 1 y una advertencia LF/CRLF, sin diagnóstico de espacios. Por ser comparación no-index de un archivo nuevo no se usa ese exit como aceptación. Se completa con lectura del archivo guardado para comprobar espacios al final de línea, marcadores de conflicto y salto final; `git diff --check` se informa aparte y solo cubre diferencias versionadas, no este archivo todavía sin añadir. Las salidas de estas comprobaciones quedan en la conversación, sin log/receipt canónico.

**Punto de reanudación:** completar el estudio integrado, sin implementación productiva. El bloqueo de una parte no cancela el análisis de las demás. La pausa original no debe confundirse con finalización ni aprobación.
