# PRE-G9B-MAINT-P0 — D. Diseño preliminar revisado de las mejoras pendientes de CU7

| Campo | Valor |
|---|---|
| Carácter | **Diseño preliminar candidato.** No modifica ADR 0026, 0029, 0041 ni el contrato M09, no aprueba un contrato sustitutorio y no altera retroactivamente las decisiones de CU7. `implementationAuthorized=false`; `selfApproved=false` |
| Estado | `DESIGN CANDIDATE — PENDING AUTHOR REVIEW` (revisión 2) |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0», bloque C, y enmiendas autorales finales del expediente, punto 3 (ambas del 2026-11-23) |
| Base | `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2) |
| Evidencia | [informe de caracterización](../validation/pre_g9b_maint_p0_characterization_report.md), §C y §F |

## Registro de revisiones

| Revisión | Fecha | Contenido |
|---|---|---|
| 1 | 2026-11-23 | Versión inicial |
| 2 | 2026-11-23 | Enmiendas autorales, punto 3: S1 como incremento de transparencia y seguridad y S2 como objetivo funcional principal; sin reducir la coherencia del DAG ni aceptar publicación parcial confiando en Undo; caracterización de referencias por nombre, fronteras de transacción y condiciones de restauración antes de aprobar la implementación (§3). I2G0 pasa a ser una barrera basada en el riesgo demostrado para el productor y su grupo, que no rechaza operaciones Classic legítimas, con caracterización previa de ambos comportamientos (§4). F8 queda condicionado a una prueba discriminante; la traza de código y la sonda P-2 no son confirmación empírica (§2). U2 se mantiene como mejora prioritaria. M08-D e I3 siguen diferidos |

## 0. Resumen

1. **U2 es una mejora de interfaz prioritaria, pequeña y sin riesgo para el núcleo.** Todas
   las entradas interactivas de creación sobre Locus V2 ya pasan por la misma fachada,
   `LocusPointCreation2D`; la duplicación está solo en la interfaz. Se propone un único panel
   de Punto con selector de origen (clic o referencia Q). La herramienta 145 se conserva como
   alias del mismo panel. El diálogo de extremos métricos se alinea con el panel y se le
   añade su primera prueba. Las guías EN/ES se reescriben.
2. **F8 puede tener sentido, pero no como capacidad constructiva nueva, y su justificación
   depende de una prueba discriminante.** El objeto que publicaría (`Point(L, u)` sobre una
   componente certificada) ya es admisible y ya se crea por comando. Lo que faltaría es la
   ruta interactiva. **Según la traza del código, no según una observación**, en un mismo
   locus el clic funcionaría sobre la parte condicionada (evidencia débil) y se rechazaría
   sobre una componente certificada local (evidencia fuerte). La sonda P-2 de esta fase no
   fue discriminante. Ni la traza ni P-2 son confirmación empírica. La exigencia `COMPLETE`
   es una restricción de alcance de producto, no una condición de admisibilidad.
   Recomendación:
   - mitigación inmediata dentro de U2 (el rechazo remite a `Point(L, u)` y a la herramienta 134);
   - una prueba discriminante sobre un locus mixto real (`MAINT-F8-S0`);
   - solo si esa prueba confirma la conducta trazada, un incremento mínimo `MAINT-F8` que
     reutiliza el permiso individual existente; si no la confirma, cerrar F8.

   No se recomienda cancelar F8 antes de esa prueba.
3. **La sustitución con identidad nueva simplifica el objetivo, no los dependientes.**
   - Elimina la tabla cerrada de transiciones y el predicado de retención de `P`.
   - Las puertas transaccionales que bloquearon M09 pertenecen al camino de sustitución del
     anfitrión. Solo desaparecen si el autor acepta el nivel de garantía del ADR 0026 (reversión
     al XML de entrada, objetos Java reconstruidos, un paso de Undo) en lugar de la
     restauración exacta exigida a M09.
   - Una identidad nueva para `P` cambia el contrato de cada dependiente duradero directo.
     Por tanto, o se pierden (lo que ya hace la sustitución legada vigente), o hace falta una
     enmienda que permita actualizar su contrato (M09-B, hoy excluido).
4. **Ya existe una sustitución con identidad nueva**: el modo `LEGACY_REPLACEMENT` del ADR
   0026, con impacto tipado, confirmación y un paso de Undo. Su defecto es que elimina todo el
   cierre duradero dependiente, incluidos los Locus V2, y que su informe lo anuncia
   inexactamente.
5. **Secuencia recomendada:**
   - `MAINT-S0`: caracterización previa (referencias por nombre, fronteras de transacción,
     condiciones de restauración, fusión, hermanos, veracidad del impacto, frecuencia en los
     modelos); la aprobación de la implementación de S1 y S2 depende de sus resultados;
   - `MAINT-S1`: **incremento de transparencia y seguridad**: sustitución explícita dentro de
     las normas vigentes, con impacto veraz en cuatro clases, objetivos sin identidad y
     barrera de grupo;
   - `MAINT-S2`: **objetivo funcional principal**: sustituir un punto por otro con identidad
     nueva preservando las construcciones dependientes compatibles, mediante actualización
     explícita de sus dependencias y revalidación de su evidencia, tras enmienda (N3).

   Ninguno de los dos reduce las garantías de coherencia del DAG ni acepta una publicación
   parcial confiando en que Undo la repare: la operación es atómica y su propia reversión
   no es un Undo (§3.3).
6. **I2G:** no existe una operación segura para separar una salida de su productor.
   `MAINT-I2G0` es una barrera **basada en el riesgo demostrado** para el productor y su grupo:
   impide la destrucción silenciosa de identidades y dependencias CeDG, empezando por la de un
   hermano con identidad fijada hoy por una prueba, y no rechaza las redefiniciones Classic
   legítimas que no ponen nada CeDG en riesgo. El número de salidas por sí solo no decide. Ambos
   comportamientos se caracterizan antes de modificar el anfitrión (§4.3). Retirar todo el
   grupo con impacto explícito es opcional; conservar los hermanos con el productor original no
   es viable en este programa y la interfaz debe decirlo.
7. **M08-D e I3 siguen expresamente diferidos** (§5).

## 1. U2 — Simplificación de la herramienta Punto

### 1.1 Qué no cambia

- La fachada del núcleo `LocusPointCreation2D`: orden y predicados de rutas, presupuesto
  acumulado y reproducción A3.
- `IndividualForwardPointCreation2D`, `M08SnapshotPlacement2D`, `LocusSnapshotPlacement2D`,
  `ConditionedPointPlacement2D`, el adaptador `GeoCeDGReferencePlacement` y la vigencia de
  `GeoCeDGPointRequest`.
- Persistencia: códecs `geocedg-locus-address/v1`, `geocedg-locus-principal-selection/v1` y
  `geocedg-locus-parameter-address/v1`; rol de salida de interacción; Q nunca es padre; un paso
  de Undo por creación.
- Identificadores de modo 1, 501, 134 y 145 e identificadores de acción (el perfil exige 137
  acciones coherentes).
- «Punto en objeto» (decisión E del mantenimiento de 1.2.0), la herramienta 134, la
  colocación nativa `AlgoPointOnPath`, las exclusiones de ejes, 3D y figuras de cota, y los
  valores por defecto (radio Q `0,1`, radio de clic tomado de la vista, «Permitir arrastre»
  activo).

### 1.2 Cambios propuestos (solo interfaz)

| Id | Cambio | Archivos previstos |
|---|---|---|
| U2-1 | El panel de Punto/Punto en objeto ofrece un selector de origen único, «Clic» o «Referencia Q», que sustituye los dos conmutadores excluyentes por modo | `GeoCeDGReferencePointTool.java` |
| U2-2 | La herramienta 145 se conserva (modo, acción, icono, posición en el desplegable de curvas semánticas) como alias que abre el mismo panel con «Referencia Q» preseleccionado | `GeoCeDGEuclidianController.java` (activación), `GeoCeDGReferencePointTool.java` |
| U2-3 | El panel muestra el origen del radio («de la vista» o «r mundo») y la regla de confirmación, sin cambiar ninguna de las dos conductas | `GeoCeDGReferencePointTool.java` |
| U2-4 | La asistencia «Elegir una posición» de los extremos métricos usa la presentación del panel (`GeoCeDGAddressPresentation`), la garantía de la ruta y «Permitir arrastre», con su primera prueba | `GeoCeDGMetricEndpointAssistance.java` |
| U2-5 | El rechazo «Componente no establecida» remite a `Point(L, parámetro)` y a la herramienta de dirección explícita, como ya hace el rechazo por presupuesto (mitigación de F8) | `GeoCeDGReferencePointTool.java`, recursos de traducción |
| U2-6 | Guías EN/ES §8.1 reescritas como «Crear un punto sobre una curva», con los dos orígenes y la alternativa de dirección explícita | `docs/user/geocedg_user_guide_en.md`, `_es.md` (`GUIDE_IMPACT = UPDATE`) |

Retirar la herramienta 145 de la barra es una opción de simplificación mayor que el autor
puede elegir (D-U2); se recomienda no hacerlo en este incremento para conservar la
compatibilidad de acciones.

### 1.3 Pruebas de no regresión y nuevas

- Existentes, intactas:
  - E1/E2: `Cu7M08SGeneralCreationTest`, `GeneralPointInteractionDesktopTest`, `R3PointInteractionDesktopTest`, `G9U1SemanticPointInteractionTest`, `R4ViewActivationDesktopTest`, `PostR1ConditionedPointDesktopTest`, `Maintenance120PointToolsDesktopTest`;
  - E3/E6: `R5ReferenceCreationDesktopTest`;
  - E4/E5: `Cu7M08SReferenceToolTest`, `Cu7M08SDesktopEventsTest`, `Cu7M08SCorrectionTest`;
  - E7: herramienta 134 y pruebas de perfil e iconos;
  - E8: pruebas de comandos `Point`;
  - E10/E11: arrastre y Undo nativo;
  - núcleo: `M08PointCreationKernelTest`, `IndividualForwardPointCreationTest`, `R5ForwardCreationRoutingTest`, `Maintenance120PointInteractionTest`.
- Nuevas:
  - el selector de origen da direcciones idénticas por clic y por Q en el mismo locus;
  - el alias 145 abre el mismo estado del panel;
  - la colocación en el perfil;
  - la asistencia de extremos: presentación y un solo Undo;
  - el texto del rechazo `COMPONENT_NOT_ESTABLISHED`.
- Pregunta abierta que no bloquea U2: Ctrl+clic con Punto sobre un Locus V2 toma la ruta heredada; conviene fijar con una prueba qué hace.

## 2. F8 — Dictamen

### 2.1 Distinción previa

| Concepto | Naturaleza | Dirección | Dónde existe |
|---|---|---|---|
| Componente certificada local | `ContinuousComponent` con evidencia `EXACT` o `RIGOROUS_INTERVAL`, maximalidad `ESTABLISHED` o `LOCAL_CERTIFIED_SUBINTERVAL` | selector de componente | `HybridExistenceCertifier2D`, `SelectedRootContinuousDomainCertifier2D` |
| Región condicionada | celdas de evidencia `NUMERICAL_UNDER_HYPOTHESES` (no certifica) | dirección por parámetro, sin selector | solo sobre los parámetros que **ningún** certificado cubre (`AlgoDependentPointLocusV2.java:554-568`) |

1.2.2 no amplía la creación sobre componentes certificadas locales: por construcción, el uso
condicionado vive en el complemento de los certificados.

### 2.2 Casos

| Caso | Hoy | Cubierto por | Naturaleza del rechazo |
|---|---|---|---|
| C1 clic/Q dentro de una componente `LOCAL_CERTIFIED_SUBINTERVAL` de un inventario `NOT_ESTABLISHED` | rechazado (traza; sin prueba) | comandos `Point(L, u)`, `Point(L, "rama", u)`, herramienta 134; `Point(L, D)` bajo autoridad constructiva | **limitación funcional** (existencia §11 y §15.8 admiten la dirección exacta; la exigencia `COMPLETE` es la «tabla inicial» del candidato de creación) |
| C2 lo mismo con componente máxima | igual | igual | igual |
| C3 clic sobre una rama `COMPLETE` de un locus con otra rama `NOT_ESTABLISHED` | R1/R6 y H1 buscan en la rama completa; el respaldo individual se rechaza porque «alguna rama no es `COMPLETE`» | R1/R6, H1, comandos | limitación funcional del respaldo |
| C4 clic sobre la parte condicionada | crea (1.2.2) | `CONDITIONED_PARAMETER_ADDRESS` | no es F8 |
| C5 clic en una banda | rechazado | comandos por parámetro | regla aprobada de creación por petición; no es F8 |
| C6 sin componente certificada y captura infiel | rechazado | — | **necesaria** |
| C7 extremo `UNRESOLVED_BAND_EDGE` | rechazado | — | **necesaria** |
| C8 componente puntual | rechazado | — | **necesaria** (existencia §15.8) |
| C9 documento bajo autoridad constructiva | rechazado (`AUTHORITY_NOT_ESTABLISHED`) | `Point(L, D)` | **necesaria** (ADR 0040) |
| C10 presupuesto agotado, petición obsoleta, barrera insegura | rechazado | — | **necesaria** (ADR 0019) |

### 2.3 Valor y reutilización

F8 no crea ningún objeto nuevo. Añade una ruta interactiva (clic o Q) hacia una construcción
ya admisible, con la propiedad de interacción (arrastrable, entradas ocultas) en lugar de un
número `u` visible. Puede reutilizar:

- la selección, la elección, la confirmación y la publicación atómica de `IndividualForwardPointCreation2D`;
- `LocusComponentSelectorResolver2D` y `LocusMetricPositionBinder2D`;
- la reproducción acumulada A3;
- el mismo códec y el mismo rol de salida.

Cambios mínimos en el núcleo:

1. Predicado del permiso: en lugar de «toda rama `COMPLETE`», «la rama de la dirección
   elegida tiene una componente certificada vigente que resuelve el selector retenido, con la
   dirección estrictamente dentro de su intervalo certificado». Se registra como permiso
   distinto, de modo que la completitud global y el alcance del certificado local sigan siendo
   hechos separados.
2. Descubrimiento de semillas: inspeccionar las componentes certificadas de ramas
   `NOT_ESTABLISHED` sin cambiar la clasificación de R6.
3. Un valor de ruta nuevo que se prueba antes de la ruta condicionada (primero la evidencia
   certificada), replicado en `GeoCeDGReferencePlacement.Route` de Desktop.
4. Texto del panel que declara «inventario global no establecido» y el alcance del certificado
   local.

### 2.4 Recomendación (decisión D-F8)

1. Mitigación inmediata en U2 (U2-5).
2. `MAINT-F8` en dos pasos:
   - **F8-S0**: una prueba discriminante sobre un locus mixto real, que **determine** sin
     presuponerla la conducta en el caso C1 y, por contraste, en C4 sobre el mismo locus. Es
     discriminante solo si el mismo montaje puede dar los dos resultados (creación o rechazo)
     y si un control confirma que la ruta de clic acepta la captura del productor. La sonda
     P-2 de esta fase no cumplía esa condición, porque un locus sintético no tiene captura de
     productor. Un candidato es un locus que dependa de una raíz de intersección
     seleccionada, que produce componentes `LOCAL_CERTIFIED_SUBINTERVAL` y regiones
     condicionadas.
   - **F8-min**, solo si S0 confirma la traza: los cambios de §2.3 y las regresiones de la
     propuesta F8 (líneas 94-101), más el par de monotonía sobre un mismo locus mixto (clic en
     la región condicionada: dirección por parámetro; clic en la componente local: dirección
     con selector). La autoridad constructiva debe seguir rechazada, y R6, H1 y los resultados
     condicionados deben quedar idénticos.
3. Si S0 no confirma la traza, cerrar F8 como redundante con la evidencia de la sonda.
4. Fuera de alcance: el respaldo multirrama (C3) y cualquier arrastre nuevo.

## 3. M09 reformulado como sustitución explícita

### 3.1 Comparación

| Aspecto | A. M09 original (conserva la identidad de `P`) | B. Sustitución explícita con identidad nueva | C. Reutilización directa de lo existente |
|---|---|---|---|
| Identidad de `P` | se conserva; cambia la definición | `P` se retira; `P'` tiene identidad propia (nueva si participa) | duradero: retirada y nueva; sin identidad: solo el anfitrión |
| Dependientes duraderos directos | intactos; solo se recalcula su evidencia | su frontera cambia de `id(P)` a la de `P'`: se pierden (S1) o se actualiza su contrato tras enmienda (S2) | todos retirados **y eliminados**, también los Locus V2 |
| Dependientes ordinarios | se recalculan | reasociados por la sustitución en el DAG y recalculados | reasociados, sin informar |
| Necesidades del núcleo | tabla cerrada de transiciones, sobre de entrada sin identidad, intercambio de reclamaciones, adaptador de grupo | sobre de entrada sin identidad, impacto completo y veraz, barrera de grupo, retirada explícita previa a la reconstrucción; enmienda para S2 | ninguna; corrección de veracidad del informe |
| Puertas transaccionales | bloqueadas (exactitud de restauración, vistas, Undo, ABA) | **las mismas si se exige restauración exacta**; superables al nivel del ADR 0026 | nivel ADR 0026 ya aceptado |
| Complejidad | alta; muy alta con I2G | media (S1); media-alta (S2) | baja |
| Utilidad para Locus V2 | máxima | S1: transparencia y seguridad; S2: la de M09 | negativa: destruye los loci dependientes |

### 3.2 Respuesta a la pregunta del autor

¿Permite este planteamiento prescindir de las garantías complejas de M09?

- **Sí** para el objetivo: desaparecen la tabla cerrada de transiciones, el predicado de
  retención y toda continuidad entre `P` y `P'`. Los tokens nunca se transfieren.
- **Solo bajo una condición** para la transacción: que el autor acepte un nivel de
  restauración menos estricto que la restauración exacta de M09, como el del ADR 0026
  (D-M09-1). Si se exige la restauración exacta, la reformulación hereda los mismos bloqueos,
  porque usa el mismo camino del anfitrión. Aceptar otro nivel de restauración **no** rebaja
  la coherencia exigida: la operación sigue siendo atómica, sin publicación parcial, con
  reversión propia y no por Undo, y con el DAG y el registro de identidad coherentes tras
  cualquier fallo. Qué nivel puede cumplir esas condiciones lo decide la caracterización de
  `MAINT-S0` (§3.5), antes de aprobar la implementación.
- **No** para los dependientes: una identidad nueva obliga a decidir el destino de cada
  dependiente duradero directo, mientras que la conservación de identidad los dejaba intactos.
  Es más sencillo para el objetivo y no lo es para los dependientes.

### 3.3 Operación mínima propuesta (`MAINT-S1`)

**Perímetro:**

- `P` es un punto libre o producido por un algoritmo de salida única;
- `P` no está en la cadena de conductor ni de soporte de un Locus V2 (eso es I3; se rechaza con causa tipada);
- `P` no tiene rol especializado ni vínculo de proyección;
- `P'` es una definición nueva de familia punto.

Un `P'` existente ya etiquetado (fusión nativa) queda fuera hasta caracterizarlo (S0, D-M09-3).

**Flujo:**

1. El usuario elige «Sustituir punto…» sobre `P`. La entrada reutiliza el diálogo de
   redefinición en modo de sustitución explícita.
2. Escribe la definición de `P'`.
3. El núcleo evalúa sin mutar:
   - captura el sobre de entrada antes de analizar (también para un objetivo sin identidad);
   - comprueba el ciclo (`INVALID_DAG`) y la barrera de grupo (§4);
   - calcula el impacto completo: cierre del DAG del anfitrión más cierre duradero.
4. Se presenta el impacto en cuatro clases:

| Clase | Contenido en S1 | Tratamiento |
|---|---|---|
| Conservados | dependientes ordinarios (sin identidad duradera) de `P` | pasan a depender de `P'` por la sustitución en el DAG y se recalculan. Si la geometría degenera, quedan indefinidos con diagnóstico; no es motivo de rechazo |
| Revalidados | objetos con identidad que no dependen de ningún perdido y cuyo contrato no cambia (su frontera duradera no contiene `id(P)` y `P'` no la altera). En la práctica, los dependientes duraderos de un `P` sin identidad, como hace hoy el refresco de frontera certificada del ADR 0029 | conservan identidad; la revisión avanza; certificados, grados de integridad, evidencia condicionada y tokens se recalculan por sus reglas. Un token puede quedar inactivo; nunca se reasigna |
| Perdidos | dependientes duraderos directos cuya frontera cambia (incluidos Locus V2 públicos, resultados métricos y puntos token) y su cascada ordinaria | se retiran explícitamente antes de la reconstrucción, con lista completa |
| No determinables | impacto incompleto, referencias por nombre fuera del DAG, productor de varias salidas, I3 | rechazo tipado, sin mutación. Las referencias por nombre (guiones, textos dinámicos) pueden tratarse por política declarada (D-M09-4) |

5. El usuario confirma o cancela. Cancelar no consume Undo ni reservas.
6. Ejecución: se revalida la vigencia, se retiran los perdidos, se hace una única
   reconstrucción, una publicación y un paso de Undo. Si algo falla, se restaura el XML de
   entrada (nivel ADR 0026) o se produce un fallo tipado fatal; nunca hay éxito parcial.

Requisitos de coherencia que S1 y S2 no rebajan:

- **atomicidad**: ningún estado intermedio se publica; las vistas no observan ni escriben un
  estado a mitad de operación (el hallazgo P07 de M09 debe resolverse o demostrarse
  inaplicable a este camino en `MAINT-S0`);
- **reversión propia**: la operación se deshace a sí misma ante un fallo; nunca se confía en
  que el usuario o el Undo reparen una publicación parcial, y ningún punto de Undo se almacena
  antes del éxito (hallazgo P09);
- **coherencia tras la reversión**: DAG, registro de identidad, reservas, reclamaciones de
  token y selección iguales a los de antes de la operación, comprobados por pruebas con
  inyección de fallos en cada etapa;
- **vigencia**: la evaluación y la ejecución se refieren al mismo estado; un cambio entre
  ambas invalida la confirmación (incluido el caso ABA que la comparación de XML no ve).

**Justificación de la asociación:** los conservados pasan a depender de `P'` porque el usuario
sustituye explícitamente `P` en el DAG. La etiqueta de `P`, que `P'` recibe como asa del
analizador, es el mecanismo de reconstrucción del anfitrión y nunca la prueba de la
asociación. No se usan coordenadas, proximidad ni coincidencia de nombres (ADR 0010).

**Correcciones incluidas:**

- informe legado veraz: los dependientes que se eliminan se anuncian como retirados y eliminados, y se incluye la cascada ordinaria (OD-3);
- barrera de grupo antes de eliminar nada (§4).

### 3.4 `MAINT-S2` — objetivo funcional principal: dependientes compatibles preservados

**Objetivo:** sustituir un punto `P` por otro `P'` con identidad nueva, preservando las
construcciones dependientes compatibles mediante la actualización explícita de sus
dependencias y la revalidación de su evidencia. Es lo que hace útil la sustitución en el caso
de uso que motivó M09: convertir un punto provisional en una intersección Locus V2 sin perder
los loci que dependen de él.

**Condición previa:** enmienda autoral del ADR 0041 (decisión 8), del ADR 0026 (decisión 6),
del ADR 0029 (decisión 4) y del contrato M09 §3, con un mapa trivial y total
`{P ↦ P'}`; cualquier otra identidad se aplica en sí misma.

**Dependiente compatible:** dependiente duradero directo de `P` cuyo paso de construcción
propio sigue siendo válido con `P'`: mismo comando, mismas entradas salvo `P ↦ P'`, y `P'`
de un tipo que ese comando admite en esa posición. La compatibilidad es estructural y se
decide antes de mutar; nunca por coordenadas, etiquetas o proximidad.

**Regla propuesta para un dependiente compatible:**

- conserva su identidad, con frontera actualizada explícitamente (`id(P)` sustituido por
  `id(P')`) y revisión nueva;
- revalida toda su evidencia por sus reglas (certificados, grados de integridad, evidencia
  condicionada, existencia); si la revalidación no la establece, el objeto queda indefinido o
  con el grado que corresponda, con diagnóstico, como tras cualquier cambio ordinario, y no se
  pierde;
- no hereda ni transfiere tokens ni reclamaciones; sus tokens se revalidan por sus guardas y
  pueden quedar inactivos.

Aplicable también a Locus V2 públicos. Los dependientes no compatibles siguen las clases de
S1 y se anuncian como perdidos antes de confirmar.

### 3.5 `MAINT-S0` — caracterización previa

Solo pruebas e informe, sin cambio de producto. La aprobación de la implementación de S1 y de
S2 depende de sus resultados.

1. **Referencias por nombre fuera del DAG:** inventario de los lugares donde una etiqueta
   actúa como referencia (guiones GGBScript y JavaScript, textos dinámicos, condiciones de
   visibilidad, colores dinámicos, cadenas de `Execute`, herramientas de usuario y macros,
   subtítulos) y de qué ocurre con cada una cuando `P'` recibe la etiqueta de `P`. Decide si la
   política D-M09-4 basta.
2. **Fronteras de transacción:** dónde empieza y termina hoy la redefinición del anfitrión;
   cuándo se notifica a las vistas (P07); cuándo se almacena el punto de Undo, que es
   asíncrono en Desktop (P09); el arrendamiento de publicación y la guarda del registro de
   identidad; el borde que comparte con B2.
3. **Condiciones de restauración:** qué restaura exactamente la reversión del ADR 0026 (XML,
   objetos Java, registros de identidad, reservas, reclamaciones de token, selección, estado
   de vistas, historial de Undo) y qué no, con inyección de fallos en cada etapa.
4. Ruta nativa de fusión con un `P'` existente, con `P` y `P'` con y sin identidad.
5. Estado de los hermanos tras una redefinición nativa fallida de una salida de
   intersección.
6. Impacto anunciado frente a conjunto realmente eliminado en la sustitución legada.
7. Destrucción fijada de un hermano con identidad.
8. Frecuencia en los modelos del autor de puntos aguas arriba de objetos duraderos: dimensiona
   la utilidad real de S1 frente a S2.

### 3.6 Pruebas discriminantes de S1/S2

Detalladas en el prompt candidato; resumen:

- ciclo rechazado sin mutación;
- reasociación ordinaria con un solo Undo y Redo exacto;
- degeneración que deja indefinido sin rechazar;
- dependiente duradero perdido (S1) o actualizado (S2), nunca en silencio;
- Locus V2 público aguas abajo;
- barrera I3;
- reclamaciones de token liberadas y adquiridas una vez;
- `P'` numéricamente igual a `P` y un objeto coincidente ajeno: identidad nueva y el objeto ajeno fuera del impacto;
- impacto anunciado igual a impacto ejecutado;
- productor de varias salidas en sus tres variantes;
- inyección de fallos en cada etapa;
- vigencia y cancelación;
- persistencia;
- referencias por nombre;
- certificados recalculados sin `STRICT` obsoleto.

## 4. I2G — Productores con varias salidas

### 4.1 Hechos

- No hay operación segura para separar una salida (§C.4 del informe; ADR 0041, decisión 7).
- Hoy, al redefinir una salida:
  - sin identidades: hermanos y dependientes eliminados en silencio;
  - objetivo con identidad: rechazo con causa engañosa;
  - objetivo sin identidad con un hermano con identidad: el hermano y su registro se destruyen, comportamiento fijado por prueba (OD-4).
- Solo afecta a productores heredados: los productores de puntos de GeoCeDG son de salida
  única, y las intersecciones de Locus V2 evitan el problema con resultado rico y un punto
  token por raíz.

### 4.2 Opciones y viabilidad

| Opción | Viable | Condición | Recomendación |
|---|---|---|---|
| I2G-0 barrera basada en el riesgo (§4.3): rechazo tipado antes de eliminar nada cuando la redefinición destruiría identidades o dependencias CeDG; conducta Classic sin cambios cuando no hay nada CeDG en riesgo | sí, con piezas existentes; la barrera se comprueba antes de `prepareReplace` | cambia el comportamiento fijado de OD-4: decisión de tratarlo como defecto (D-I2G); caracterización previa de ambos comportamientos | **incremento `MAINT-I2G0`**, independiente y pequeño |
| I2G-1 sustituir y retirar explícitamente todo el grupo (hermanos y sus dependientes), con impacto completo y confirmación | sí, con la retirada nativa dentro del sobre de entrada | nunca por defecto silencioso | opcional dentro de S1 |
| I2G-2 sustituir el grupo por otro productor con el mismo conjunto de roles | no para intersecciones heredadas: sus salidas no tienen roles semánticos | — | fuera |
| I2G-3 conservar el productor para los hermanos y dejar la ranura de `P` como salida oculta sin etiqueta | **no en este programa**: no existe la operación; exigiría una costura nueva en `Construction`; la recarga no está probada; imposible para cardinalidad variable y para el caso tangente | — | investigación futura, con su propia puerta |
| I2G-4 reexpresar los hermanos como `Intersect(c, g, k)` | no admisible como identidad sin prueba por productor (índice y punto de referencia son selectores ordinales o de proximidad) | — | fuera |

La interfaz debe decir expresamente que **conservar los puntos hermanos no es posible**,
porque el productor solo existe como grupo, y ofrecer solo cancelar o, si se autoriza I2G-1,
retirar el grupo con su impacto.

### 4.3 Barrera basada en el riesgo (`MAINT-I2G0`)

**Predicado de riesgo.** Antes de `prepareReplace`, se calcula el conjunto que el anfitrión
eliminaría (hermanos del objetivo y su cierre dependiente). La redefinición está en riesgo si
ese conjunto contiene:

- un objeto con identidad duradera CeDG o un registro en `SpatialIdentityRegistry`;
- un dependiente duradero CeDG (Locus V2, resultados métricos, puntos token, referencias de
  autoridad constructiva);
- una reserva o reclamación de token.

Con riesgo: rechazo tipado antes de eliminar nada, con una causa que nombre el grupo y lo que
se destruiría (y, si S1 autoriza I2G-1, la alternativa de retirar el grupo con impacto
explícito). Sin riesgo: la conducta Classic no cambia, aunque el productor tenga varias
salidas.

**El número de salidas no decide por sí solo.** Un productor de varias salidas cuyos hermanos
son objetos Classic sin identidad ni dependientes CeDG se redefine como en Classic; el riesgo
depende de lo que se perdería, no de la cardinalidad.

**Caracterización previa (primer paso de `MAINT-I2G0`, solo pruebas):**

- conducta Classic legítima: para cada productor de varias salidas heredado (recta-cónica,
  cónica-cónica, polinómicos, vértices de polígono regular, tangentes), qué se elimina al
  redefinir una salida sin nada CeDG en el grupo, fijada como referencia que no debe cambiar;
- conducta en riesgo: los tres casos actuales (todos sin identidad pero con dependientes CeDG
  en los hermanos, objetivo con identidad, hermano con identidad), qué se destruye y qué causa
  se informa;
- estado de los hermanos tras un fallo de reconstrucción nativa (instantánea tomada después de
  eliminarlos).

Solo después se modifica el anfitrión, y únicamente en los casos con riesgo.

## 5. Capacidades diferidas (fuera del programa)

M08-D (correspondencia dinámica entre puntos y loci), I3 (transición coordinada de soporte,
conductor y fuente), la reconstrucción general de construcciones, el arrastre universal sobre
cualquier Locus V2 (`DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG`) y la redefinición general con
conservación de identidad más allá de lo aprobado e implementado. Ningún incremento de este
programa depende de ellas. S1 y S2 las rechazan con causa tipada cuando su perímetro las
exigiría (I3).
