# GeoCeDG — Living Technical Roadmap

| Campo | Valor |
|---|---|
| Carácter | Roadmap vivo y normativo de fases; no sustituye las especificaciones ni los ADR aceptados |
| Versión documental | 3.70 |
| Fecha de revisión | 4 de septiembre de 2026 |
| Baseline GeoGebra | 5.4.928.0, commit `9b93256b7df401ff056c37b502d82df4d72b1522`, tag `geogebra-baseline-5.4.928.0` |
| Estado actual | G7 y G8 `PASS`; G9P-R1, G9P, G9O1, G9A1, G9A2, G9A3, el track G9A, G9U0, G9U0-R1, G9X1, G9U0-R2, G9U0-R3, G9U0-R4, G9U0-R5, G9S1, G9U0-R6, G9S1-R1 y la planificación G10P `PASS — AUTHOR APPROVED`. R6 añade resolución inversa semántica y estado explícito de punto draggable en kernel, incluido cruce periódico bidireccional/path-independent, sin `Path`, render/pixel como autoridad ni frontend G9U1. Su aceptación GUI sigue diferida a G9U1 por diseño. R4 conserva determinismo actual > heurística de continuidad y el riesgo abierto `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`. G9U1 DESIGN post-R6 está aprobado en checkpoint independiente `00982e7e...`; la reconciliación post-R1 no identifica novedad semántica y satisface la autorización condicional del autor para producir un candidato G9U1, nunca un PASS de implementación. El autor aprueba R1-D2, acepta DISPOSITION C para el alcance monodrómico demostrado y autoriza B — PARTIAL IMPLEMENTATION CONTRACT, incluida recurrencia del slot semántico. G9S1-R1 está `PASS — AUTHOR APPROVED`; los bloqueos históricos y sus correcciones permanecen documentados. G9B/G9C no están autorizadas, G9U2 sigue bloqueada y ninguna implementación productiva G10 está autorizada; Locus V2 y la semántica espacial siguen experimentales y desactivadas por defecto |
| Última fase cerrada | G9S1-R1 — `PASS — AUTHOR APPROVED` |
| Última fase ejecutada | G9S1-R1 — `PASS — AUTHOR APPROVED`; `implementationComplete=true`, `selfApproved=false`, `authorApprovedPhase=true`, `passClaimed=true`; `manualAuthorSmoke=PASS` |
| Siguiente puerta | G9U1 en implementación autorizada tras planificación A/B PASS y auditoría post-R1 sin novedad material; detenerse en candidato pendiente de revisión/smoke autoral, sin promover G9U1 |
| Primer cliente | Aplicación de escritorio de la familia Classic 5 |
| Núcleo | Java compartido de GeoGebra, extendido solo cuando la semántica lo requiere |

Este roadmap gobierna el orden, estado y puertas de las fases. Las
especificaciones de `geocedg/specs/` son la autoridad técnica normativa de cada
capacidad; los ADR aceptados registran decisiones y alternativas; y los informes
de `docs/validation/` conservan evidencia histórica de ejecución. Cuando una
especificación o un ADR posterior aceptado contradice una propuesta anterior de
este documento, prevalece la autoridad posterior y el roadmap debe registrar la
supersesión. La [guía de usuario](../user/geocedg_user_guide.md) describe las
capacidades observables vigentes.

### G9S1-R1 — cronología técnica preservada

Estado publicado actual: `PASS — AUTHOR APPROVED`, smoke autoral `PASS`,
tag `geocedg-g9s1-r1-pass` -> `af459d856f1cdc384805f3035203acce8e6f6104`.
El sucesor operacional publicado es `f8a21a087234b18fc13741a0ac2baf80608e9022`.
Los párrafos siguientes conservan su estado/evidencia histórica, no sustituyen
esta decisión posterior ni afirman reejecución de FULL.

**Estado técnico final, 3 de septiembre:** las correcciones A/B superan PHASE
A/B (192 casos cada una, SHA-256 idéntico
`59793eef3641d8c93b012998c0a795fb949f5cefdb54d30ebcd337542ab155dd`),
COMPOSED (1281 casos, cero fallos/omisiones) y FULL clean (7781 casos: 7770
pasan, 11 omisiones upstream, cero fallos), todas con código de salida 0.
`implementationComplete=true`; `manualAuthorSmoke=PENDING`. Los 76 archivos
del conjunto de fuentes probado están archivados; esta reconciliación de estado
documental es posterior y no reemplaza sus hashes ni recibos. Los bloqueos de
la cronología siguiente no son fallos de estas ejecuciones finales. No hay
aprobación de fase ni autovalidación autoral.

La decisión autoral posterior acepta el contrato B y la reactivación del mismo
slot semántico, y autoriza la implementación productiva acotada. El
[ADR 0021](../adr/0021-spline-pair-singleton-germ-materialization.md) y la
[especificación R1](../../geocedg/specs/curves/spline-v2-pair-materialization.md)
registran esta decisión sin modificar los 13 documentos/tests/evidencias de D2,
salvo estas autoridades vivas. La palabra pendiente en los archivos D2 describe
su checkpoint histórico, no revoca la aprobación posterior.

`G9S1-R1-D2 DESIGN=AUTHOR APPROVED`;
`DISPOSITION C=AUTHOR ACCEPTED`;
`IMPLEMENTABILITY=B — PARTIAL IMPLEMENTATION CONTRACT — AUTHOR APPROVED`;
`implementationStarted=true`, `implementationAuthorized=true`,
`selfApproved=false`, `authorApprovedPhase=false`, `passClaimed=false`.
La [ejecución canónica](../../.github/prompts/tasks/g9s1-r1-spline-pair-intersection-materialization.prompt.md)
y [matriz productiva](../validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
exigen PHASE A/B, COMPOSED y FULL con limpieza de outputs; no se declara ningún
resultado por la mera existencia del código o del diseño aprobado.

La prueba positiva nativa de cruce de nudo expuso saltos C0/C1 no nulos en los
polinomios binarios independientes. La negativa del certificador fue correcta
bajo D2; se conserva ese fallo, la condición de parada aplicada y el
[informe del bloqueo](../validation/g9s1_r1_implementation_blocker_report.md)
y su [evidencia durable](../../geocedg/validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json)
sin reescribirlos como éxito. La decisión posterior autoriza la corrección
acotada del [ADR 0022](../adr/0022-structural-spline-continuity.md): base directa
equivalente con continuidad estructural, eliminación periódica exacta y puente
de intervalos sobre esa misma función. Conserva las ecuaciones originales,
la familia bounded Classic-compatible y validación numérica sin elevar tolerancias.
La [arquitectura](../architecture/g9s1_r1_structural_spline_continuity.md) y
[nota de investigación sucesora](../research/g9s1_r1_structural_spline_numerics.md)
separan derivación, igualdad estructural, aproximación y evidencia.

**Cronología histórica del conjunto bloqueado anterior a A/B (no estado actual):**

`G9S1-R1=BLOCKED — AUTHORIZED CORRECTIVE CONTINUATION`.
`implementationComplete=false`. COMPOSED y diagnóstico dirigido reproducen dos
regresiones científicas actuales: rechazo de la spline quíntica histórica de 25
puntos y raíces transversales materializables espurias en una tangencia implícita.
Se conservan como fallos obligatorios; FULL continúa requerido, no ejecutado e
incompleto. La corrección acotada del orden de filtros R6 no resuelve estos dos
bloqueos. La disposición autoral posterior autoriza A (construcción con precisión
superior acotada, mismas ecuaciones y admisión) y B (certificación univariada de
la composición estructural con la implícita). No declara resuelto ninguno.
Se conservan PHASE 153/153, COMPOSED 1137 con seis fallos y diagnóstico A/B 132
con cuatro fallos como conjuntos históricos diferentes; R6 49/49 y lifecycle
22/22 no sustituyen los fallos científicos. Se reanuda DEV antes de PHASE,
COMPOSED y FULL del nuevo conjunto de fuentes, sin reducir ningún requisito.
La finalización técnica requiere los recibos completados PHASE A/B, COMPOSED y FULL
del mismo conjunto de fuentes; este registro no declara PASS ni aprobación manual.
El caso nativo, 20 casos estructurales y la costura periódica independiente deben
validarse junto con todos los requisitos previos de R1.

**Disposición técnica actual:** esos requisitos se ejecutaron con los resultados
finales indicados arriba. Se conservan los recibos fallidos y el conjunto probado
final por separado; solo está pendiente la revisión/smoke del autor.

El alcance es SplineV2 x SplineV2 autenticado y transformaciones certificables,
no pares LocusV2 genéricos. Una clase de germen transversal única certificada por
producto completo de componentes puede materializarse; `+,+,-` puede admitir
solo la clase singleton. Rangos proyectados, nudos, cajas e historial no son
identidad. La recurrencia es del slot estructural, no de una trayectoria física
a través de singularidades. Grupos same-germ y el testigo monodrómico conservan
rich-only. No se implementa G9U1 ni se modifica su checkpoint protegido.

Orden vigente: `G9S1 PASS -> R6 PASS -> R1 structural correction + complete validation -> R1 author closeout ->
G9U1 design reconciliation -> separate G9U1 implementation authorization`.
El riesgo `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` permanece OPEN / TRACKED;
la persistencia de cuarentena de pares R1 no sustituye su evidencia nativa R4.

### G9S1-R1-D2 — registro conservado previo a aprobación productiva

El siguiente texto conserva la cronología del checkpoint D2 anterior a la
decisión autoral registrada arriba; no es el estado actual de autorización.

El autor acepta `G9S1-R1 DESIGN FINDING = DISPOSITION C — AUTHOR ACCEPTED`
para la monodromía periódica demostrada. No es PASS de R1 ni autorización
productiva. `generalRegularPairInvalidation=REJECTED`;
`productiveImplementationStarted=false`,
`productiveImplementationAuthorized=false`, `selfApproved=false`,
`authorApprovedPhase=false`, `passClaimed=false`.

El [contrato final D2](../architecture/g9s1_r1_d2_pair_sheet_contract.md) propone
**B — PARTIAL IMPLEMENTATION CONTRACT**: selector simétrico de clase de germen
transversal única certificada en el producto completo de componentes, sin
parámetros, cajas, rangos proyectados ni historial en la identidad. Como máximo
dos raíces elegibles por par de componentes; grupos con varias raíces del mismo
germen permanecen rich-only, incluido el testigo periódico aceptado. La pérdida
temporal de certificado conserva el selector; su recurrencia única reactiva el
mismo punto, sin afirmar una trayectoria histórica a través de singularidades.
Este alcance parcial y la recurrencia del slot estructural requieren revisión
autoral explícita antes de cualquier implementación. Las transiciones de carta,
nudo o rango certificadas no invalidan por sí mismas.

`G9S1-R1-D2 = FINAL DESIGN CANDIDATE — PENDING AUTHOR REVIEW`.
G9U1 DESIGN sigue aprobado en el checkpoint protegido, sin cambios; su
implementación permanece no autorizada y bloqueada hasta disposición autoral
R1 y reconciliación separada. No se abre otra fase de investigación. La
[evidencia D2](../../geocedg/validation/g9s1-r1/g9s1-r1-d2-design-evidence.json)
es DEV, no aceptación productiva. El riesgo R4 periódico nativo sigue abierto.

### Registro conservado de entrada G9S1-R1 (3 de septiembre de 2026)

El texto de las iteraciones anteriores queda conservado como cronología; la
aceptación posterior del hallazgo y el contrato actual son los de D2 arriba.

**Iteración de diseño 2 — decisión autoral posterior:** los contraejemplos de
rango proyectado se aceptan como evidencia, pero el fallback que invalidaba por
cambio de rango/región durante movimiento transversal regular **NO está
aprobado**. `G9S1-R1 DESIGN = NOT APPROVED — CONTINUE DESIGN`;
`implementationStarted=false`, `selfApproved=false`, `authorApproved=false`,
`passClaimed=false`. La [continuación de atlas y monodromía](../architecture/g9s1_r1_symmetric_atlas_design.md)
separa certificados numéricos, cartas actuales y selector durable. Las
transiciones certificadas únicas no invalidan por cruzar cajas, rangos o cartas.
Una familia exacta de splines cúbicas periódicas recorridas dos veces demuestra
una permutación tras un bucle regular; esto justifica estudiar cortes/alcances
con monodromía, no cuarentena indiscriminada ni una implementación aprobada.
Disposición de diseño **C — MONODROMY-AWARE ATLAS / QUARANTINE REQUIRED** en ese
alcance demostrado; el esquema general y su política de cortes siguen sujetos
a revisión. La evidencia diagnóstica anterior se conserva como primera iteración.

Esta actualización sustituye el estado prospectivo pre-reconciliación de G9U1
que conservan las secciones históricas de R6 más abajo. El diseño post-R6 está
`PASS — AUTHOR APPROVED` en el checkpoint independiente
`00982e7e148a634cd57ed928f322774df267d5e3`, rama
`feature/g9u1-construction-workspace-planning-after-r6`. No se integra esa rama
ni se ejecuta G9U1. El main de entrada es `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`;
los descendientes de verificación/workstation de R6 no son fases geométricas.

La secuencia autorizada queda `G9S1 PASS -> G9U0-R6 PASS -> G9S1-R1 -> G9U1`.
Esta continuación autoriza **solo diseño, sin implementación productiva**:
los rangos de ambas proyecciones paramétricas no garantizan por sí solos un
selector estable de raíces del dominio producto. La
[primera revisión matemática y de código](../architecture/g9s1_r1_pair_materialization_design_review.md)
se conserva como evidencia histórica. Su alternativa de invalidación por
cambio de carta/rango fue rechazada; no es la propuesta actual. La nueva
iteración demuestra una obstrucción monodrómica en un alcance explícito, no
una imposibilidad general ni materialización lograda.

Dependencia semántica: G9U1 no puede materializar pares mediante proximidad para
eludir R1. Orden recomendado: tras disposición autoral de R1, revisión acotada separada del diseño
G9U1 protegido y autorización explícita de implementación. Puerta global:
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` permanece `OPEN / TRACKED`, con su
disposición exigida antes del cierre global G9; esta revisión no aporta el
round-trip nativo pendiente. R1 no tiene aprobación ni PASS.

---

## 1. Conclusión ejecutiva

La estrategia recomendable no es convertir inmediatamente toda la distribución de GeoGebra en un producto distinto, ni introducir todas las herramientas CeDG existentes en el núcleo. Conviene construir GeoCeDG en cinco movimientos controlados:

1. **Congelar y caracterizar una línea base reproducible de GeoGebra.**
2. **Crear un perfil de aplicación GeoCeDG**, con frontend, perspectivas, barras de herramientas, recursos y funciones experimentales independientes del perfil Classic.
3. **Separar las extensiones por madurez**: modelos/herramientas heredadas, investigación, funciones experimentales y funciones estables.
4. **Modificar el kernel únicamente cuando cambie la semántica geométrica**, empezando por un rediseño de `Locus` que separe el objeto geométrico de su muestreo gráfico.
5. **Introducir, antes del DSL, una semántica espacial CeDG nativa** que asocie cada objeto 3D con sus proyecciones, verifique qué conjunto de proyecciones lo define completamente y permita componer objetos complejos mediante primitivas, superficies, caras y relaciones constructivas.

La plataforma útil se construye por incrementos sobre el kernel común de
GeoGebra. Tras G6A y la implementación candidata G6B ya dispone de interfaz
propia, laboratorio legacy, packaging técnico Windows, exportación DXF 2D
experimental y una entidad semántica Locus V2 interna validada; los siguientes
elementos combinan capacidades cerradas y objetivos explícitamente futuros:

- interfaz GeoCeDG propia (`PASS`, G2);
- carga optativa de herramientas CeDG legacy (`PASS`, G3);
- prompt files y verificación ejecutable (`PASS`, G1/G1R);
- packaging e instaladores Windows generados desde fuentes (`TECHNICAL PASS`,
  G4/G4R; redistribución pública bloqueada);
- corpus y modelos de regresión CeDG (`PASS` para la infraestructura G3/G5,
  ampliación continua);
- exportación DXF 2D como servicio externo al kernel (`PASS`, G5; feature
  experimental);
- contrato matemático y arquitectura de `Locus V2` (`PASS`, G6A), con entidad
  experimental interna, evaluator, composición anidada y render derivado
  implementados y validados en G6B (`PASS`);
- métricas internas Locus V2 implementadas y validadas en G7B, con G7A-R1,
  G7A y G7B aprobadas por el autor, spec normativa y ADR 0007 Accepted;
- arquitectura y fases de planificación G8 aprobadas por el autor; G8A,
  G8B-R1 y G8B son `PASS — AUTHOR APPROVED`, la spec G8 es normativa, ADR 0008
  está Accepted con la aclaración R1, el diseño G8C y G8C1 son `PASS — AUTHOR
  APPROVED`; el contrato G8C2 es normativo, ADR 0009 está Accepted y el
  kernel interno G8C2 y cierre global G8 son `PASS — AUTHOR APPROVED`;
- un modelo semántico `SpatialObject3D`–proyecciones (`PENDING`, G9);
- criterios verificables de suficiencia y degeneración de proyecciones
  canónicas (`PENDING`, G9).

Este orden permite obtener pronto un producto reconocible sin arriesgar la estabilidad del kernel y prepara una evolución científica mantenible. El primer hito de producto se alcanza en G2; el primer hito de plataforma CeDG con semántica espacial completa se alcanza en G9.

---

## 2. Qué representa CeDG y qué debe preservar GeoCeDG

CeDG organiza un modelo como una secuencia de entidades gráfico-matemáticas relacionadas por dependencias constructivas. Los parámetros se propagan dinámicamente y las proyecciones, procedimientos de geometría descriptiva y relaciones funcionales forman parte del modelo; no son una mera visualización posterior.

Por tanto, GeoCeDG debe preservar:

- trazabilidad del procedimiento;
- parámetros y dominios explícitos;
- dependencia dinámica;
- coherencia entre proyecciones;
- separación entre procedimiento y resultado;
- integridad geométrica de curvas y superficies;
- tratamiento explícito de degeneraciones;
- distinción entre resultado exacto, simbólico, numérico y discretizado.

Los resultados existentes muestran ya una base científica prometedora:

- intersecciones de superficies formuladas mediante funciones locus;
- desarrollos de superficies radiadas y comparación favorable con CAD;
- modelos con variables discretas, como el número de virolas;
- primer tratamiento de superficies desarrollables generales y aristas de retroceso;
- herramientas GeoGebraScript para medición y operaciones CeDG;
- identificación experimental de las limitaciones del `Locus` actual.

El objetivo de GeoCeDG no es imitar un CAD, sino convertir esa metodología en una plataforma propia, verificable y extensible.

La versión CeDG existente presenta, sin embargo, una carencia semántica explícita: las proyecciones representan objetos espaciales, pero el software no conserva todavía una identidad 3D nativa que las agrupe ni un certificado de que las vistas disponibles son suficientes para determinar el objeto. La prueba de concepto basada en listas y JavaScript demuestra la viabilidad de la asociación jerárquica vista–objeto, pero no debe convertirse sin más en la arquitectura de producción. GeoCeDG debe elevar esa relación al kernel, integrarla en el grafo de dependencias y serializarla de forma estable antes de definir el DSL de alto nivel.

---

## 3. Hallazgos relevantes sobre el diseño actual de GeoGebra

### 3.1 Organización del repositorio

El repositorio público actual es un espejo del repositorio principal de desarrollo. Usa una construcción Gradle compuesta con grandes familias:

- `source/shared/common`: kernel común, objetos, algoritmos, comandos y contratos compartidos;
- `source/shared/*`: módulos auxiliares compartidos;
- `source/desktop/*`: aplicación de escritorio;
- `source/web/*`: aplicaciones web;
- `source/build-logic`: lógica de construcción;
- `doc/dev`: documentación de extensión y pruebas.

La entrada canónica comprobada desde la raíz del composite build es:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
.\gradlew.bat :desktop:desktop:run
```

El primer comando arranca GeoCeDG y el segundo conserva GeoGebra Classic como
referencia diagnóstica. La indicación
histórica `:desktop:run` del README upstream no refleja la ruta del composite
actual; se conserva literalmente solo en la
[copia archivada del README upstream](../upstream/GEOGEBRA_README.md). La JVM
validada que ejecuta Gradle es JDK 22 y la tarea Desktop solicita y utiliza un
toolchain JDK 25. La autoridad operativa sigue siendo el wrapper y
`tools/agent/verify.ps1`, no documentación histórica sobre Java 8.

### 3.2 Perfil de aplicación

GeoGebra ya dispone de una abstracción `AppConfig` y de configuraciones especializadas, como `AppConfigGeometry`. Estas clases controlan aspectos como:

- código y nombre de aplicación;
- perspectiva forzada;
- distribución de paneles;
- tipos de herramientas;
- filtros de comandos;
- vistas disponibles;
- preferencias;
- etiquetado, sliders y comportamiento algebraico.

Esto favorece crear `AppConfigGeoCeDG` o una extensión equivalente, en lugar de alterar de forma global `AppConfigGeometry` o `AppConfigDefault`.

### 3.3 Barra de herramientas

La barra clásica se representa mediante una cadena de definición con identificadores de modo. La infraestructura:

- agrupa modos en menús;
- admite separadores;
- incorpora herramientas de usuario/macros;
- analiza la cadena para construir la barra.

Por ello, el frontend inicial puede generarse desde un manifiesto GeoCeDG que compile a la definición de toolbar existente. A medio plazo, el manifiesto debe ser la fuente estable y la cadena solo un formato de adaptación.

### 3.4 Incorporación de comandos

La documentación de desarrollo de GeoGebra confirma un flujo coherente:

1. registro/localización del comando;
2. algoritmo en el kernel común;
3. declaración explícita de entradas, salidas y dependencias;
4. método `compute`;
5. procesador y dispatcher;
6. pruebas.

GeoCeDG debe seguir este mecanismo. Un comando no debe añadirse únicamente como botón o script si su semántica debe pertenecer al kernel.

### 3.5 Estado interno de `Locus`

El diseño actual muestra el problema central:

- `GeoLocusND` almacena una lista `myPointList`;
- el rango del parámetro de camino es `0 ... N-1`;
- un punto del camino se interpola entre muestras consecutivas;
- la búsqueda de pertenencia usa segmentos de esa polilínea;
- el algoritmo de generación usa tolerancias de distancia expresadas inicialmente en píxeles;
- esas tolerancias se transforman a unidades del mundo mediante la escala de la vista;
- existe además un límite temporal por paso.

En consecuencia, el muestreo gráfico interviene hoy en la identidad práctica del camino. Esta arquitectura no puede ser la autoridad para medir longitudes científicas ni para encontrar intersecciones independientes del zoom.

La modificación requerida es estructural y pertenece al kernel.

---

## 4. Licencias: decisión crítica de arranque

La situación actual difiere de la descrita en algunos trabajos previos, que reflejaban la licencia vigente cuando fueron escritos.

A fecha de esta planificación:

- **el código fuente de GeoGebra está bajo EUPL 1.2**;
- la EUPL permite usar, modificar y redistribuir código, también en contextos comerciales, con obligaciones de atribución, conservación de avisos, identificación de cambios y reciprocidad;
- **los instaladores de GeoGebra no se distribuyen con esa misma libertad**;
- los ficheros de idioma, documentación, imágenes de interfaz, estilos, iconos y logotipos están sujetos a CC BY-NC-SA u otras condiciones indicadas por GeoGebra;
- la marca GeoGebra no se concede con la EUPL;
- un producto que combine el código con los materiales restringidos puede continuar sujeto a limitaciones no comerciales.

### Consecuencia

Crear un instalador propio es necesario, pero **no suficiente** para obtener una distribución completamente libre. GeoCeDG debe disponer de:

1. nombre y marca propios;
2. instalador propio;
3. iconos, estilos y recursos propios;
4. inventario de traducciones y decisión explícita sobre su sustitución;
5. matriz componente/licencia;
6. preservación de avisos EUPL;
7. registro de modificaciones.

Durante el desarrollo interno se pueden conservar provisionalmente recursos heredados, siempre identificados. Para una distribución pública sin restricción no comercial, esos recursos deben sustituirse o someterse a revisión específica.

Esta planificación no constituye asesoramiento jurídico. La liberación pública o comercial debe incluir una revisión legal final.

---

## 5. Arquitectura propuesta

```text
┌─────────────────────────────────────────────────────────────┐
│ Aplicación GeoCeDG de escritorio                            │
│ perfil, perspectivas, toolbar, capas, navegación            │
├─────────────────────────────────────────────────────────────┤
│ Capa externa de estudios posterior a la puerta global G9    │
│ DSL, análisis, optimización, solvers, informes y workbench   │
├─────────────────────────────────────────────────────────────┤
│ Servicios compartidos de documento, evaluación e intercambio│
│ evaluación aislada, aplicación atómica, DXF, PDF/SVG y CLI  │
├─────────────────────────────────────────────────────────────┤
│ Semántica espacial CeDG en Java                             │
│ objetos 3D, asociaciones, proyecciones canónicas,           │
│ composición por superficies/caras, visibilidad y puente 3D  │
├─────────────────────────────────────────────────────────────┤
│ Extensiones geométricas CeDG del kernel Java                │
│ Locus V2, métrica, incidencia e intersecciones 2D           │
├─────────────────────────────────────────────────────────────┤
│ Kernel común de GeoGebra                                    │
│ objetos, algoritmos, grafo de dependencias, evaluación      │
└─────────────────────────────────────────────────────────────┘
```

La semántica espacial no debe residir en Python ni en el workbench. El DSL y
el runtime de estudios consumirán objetos y evaluaciones definidos por el
kernel; no reconstruirán informalmente geometría a partir de nombres,
coincidencias gráficas ni un segundo grafo de dependencias. El servicio
compartido de evaluación/aplicación solo podrá reutilizar el `Construction` y
su DAG normal; no contendrá un optimizador genérico.

### Regla de colocación

| Funcionalidad | Capa recomendada |
|---|---|
| Perfil y GUI GeoCeDG | aplicación desktop |
| Reorganización de botones | manifiesto de UI + adaptador toolbar |
| Herramientas ya desarrolladas | paquetes no permanentes en `models/legacy` |
| Instalador | `packaging/` + `tools/release/` |
| Prompt files | `.github/prompts/` |
| Ejecución/verificación de agentes | `tools/agent/` |
| Exportación DXF | servicio externo sobre DTO geométrico |
| Asociación objeto 3D–proyecciones | kernel/semántica compartida Java |
| Criterios de proyección canónica | kernel/semántica compartida Java |
| Objetos compuestos, caras y sólidos proyectivos | kernel/semántica compartida Java |
| Caracterización G10P de estudios | documentación/validación; puede diseñarse durante G9 sin código productivo |
| Evaluación aislada y aplicación atómica de candidatos | servicio compartido Java de construcción/documento, después de la puerta global G9 |
| DSL, estudios, sweeps, optimización y adapters de solver | runtime externo solver-neutral, después de la puerta global G9 |
| Medición e intersección de Locus | kernel Java |
| Visibilidad geométrica en proyecciones | servicio geométrico compartido Java |
| Capas jerárquicas, bloqueo y estados de vista | modelo de documento/aplicación compartido |
| Zoom, navegación y escalas de pantalla | capa de vista/desktop; nunca autoridad métrica |
| Conversión a vista 3D | adaptador compartido desde la semántica espacial al kernel/vista 3D existente |
| PDF/SVG y hojas de dibujo | servicio de documento/exportación |
| Benchmarks y validación | tests + modelos + herramientas de rendimiento |
| Workbench de estudios futuro | cliente del runtime y del kernel, sin lógica geométrica propia |

---


## 6. Estructura de carpetas

El siguiente árbol fue la **topología objetivo inicial**. Se conserva como
registro de intención y no debe interpretarse como una lista de directorios que
haya que crear por adelantado:

```text
GeoCeDG/
├─ AGENTS.md
├─ README.md
├─ UPSTREAM.md
├─ LICENSE
├─ NOTICE.md
├─ THIRD_PARTY.md
├─ LICENSES/
│  ├─ EUPL-1.2.txt
│  └─ third-party/
├─ source/                         # estructura GeoGebra heredada
├─ gradle/
├─ doc/dev/                        # documentación upstream
├─ geocedg/
│  ├─ specs/
│  │  ├─ commands/
│  │  ├─ objects/
│  │  ├─ spatial/
│  │  ├─ projections/
│  │  ├─ visibility/
│  │  ├─ layers/
│  │  ├─ drawing/
│  │  ├─ performance/
│  │  ├─ ui/
│  │  ├─ exports/
│  │  └─ serialization/
│  ├─ features/
│  │  ├─ stable.yml
│  │  └─ experimental.yml
│  ├─ resources/
│  │  ├─ icons/
│  │  ├─ styles/
│  │  ├─ translations/
│  │  └─ assets-manifest.yml
│  └─ validation/
│     ├─ invariants/
│     ├─ tolerances/
│     ├─ analytic/
│     ├─ projection-certificates/
│     ├─ visibility/
│     └─ baselines/
├─ models/
│  ├─ canonical/
│  ├─ research/
│  ├─ regression/
│  ├─ legacy/
│  └─ manifests/
├─ apps/
│  ├─ geocedg-desktop/
│  ├─ workbench/
│  └─ python/
├─ python/
│  ├─ pyproject.toml
│  ├─ src/geocedg/
│  └─ tests/
├─ tools/
│  ├─ agent/
│  ├─ build/
│  ├─ release/
│  ├─ dxf/
│  ├─ benchmark/
│  └─ upstream/
├─ packaging/
│  ├─ common/
│  ├─ windows/
│  ├─ macos/
│  └─ linux/
├─ .github/
│  ├─ prompts/
│  │  ├─ canonical/
│  │  ├─ tasks/
│  │  └─ reviews/
│  └─ workflows/
├─ ai-shell/
│  └─ prompts/
│     ├─ ask.md
│     ├─ plan.md
│     ├─ verify.md
│     ├─ refactor.md
│     └─ architect.md
├─ docs/
│  ├─ architecture/
│  ├─ adr/
│  ├─ licensing/
│  ├─ research/
│  ├─ roadmap/
│  ├─ upstream/
│  └─ validation/
├─ tests/
│  ├─ geometry/
│  ├─ spatial/
│  ├─ projections/
│  ├─ visibility/
│  ├─ regression/
│  ├─ integration/
│  └─ golden/
├─ benchmarks/
│  ├─ kernel/
│  ├─ rendering/
│  └─ models/
└─ artifacts/                      # generado; ignorado por Git
```

G0–G5 adoptaron una estructura incremental y más pequeña: el contrato del
producto reside en `apps/geocedg/application-profile.yml`; los puntos Java de
perfil, launcher e integración permanecen localizados en los módulos shared y
Desktop existentes; las especificaciones están agrupadas por capacidad bajo
`geocedg/specs/`; los recursos legacy se preservan en `models/legacy/`; y la
automatización propia se distribuye entre `tools/agent/`, `tools/bootstrap/`,
`tools/benchmark/`, `tools/legacy/` y `tools/release/`. El packaging validado es
exclusivamente Windows y reside en `packaging/windows/`; no se han creado aún
clientes Python, macOS o Linux. Esta estructura adoptada prevalece sobre los
directorios hipotéticos del árbol anterior y solo debe ampliarse cuando una fase
aprobada lo necesite.

---

## 7. Gestión de las adiciones CeDG no permanentes

Las herramientas actuales no deben incorporarse directamente a la barra estable. Se propone:

### 7.1 Paquete de herramienta

Cada herramienta o grupo tendrá:

```text
models/legacy/<identificador>/
├─ model.ggb o tool.ggt
├─ manifest.yml
├─ README.md
├─ expected/
└─ screenshots/            # solo evidencia, no autoridad
```

### 7.2 Manifiesto mínimo

```yaml
id: cedg.example
maturity: legacy
source: original-location-or-publication
loaded_by_default: false
inputs: []
outputs: []
validity_domain: ""
known_degeneracies: []
reference_models: []
expected_metrics: {}
license: ""
```

### 7.3 Estados

- `legacy`: conservación y caracterización;
- `research`: experimento activo;
- `experimental`: integrado bajo feature flag;
- `stable`: API y comportamiento aprobados;
- `deprecated`: conservado por compatibilidad.

### 7.4 Laboratorio CeDG

El perfil GeoCeDG incluirá un modo de laboratorio que cargue paquetes experimentales. De este modo se conserva la flexibilidad de GeoGebraScript y las herramientas de usuario sin convertirlas en comandos permanentes.

---

## 8. Frontend inicial

### 8.1 Estrategia

G2 creó un perfil `GeoCeDG` basado en la infraestructura `AppConfig`, con:

- identidad de producto, launcher y namespace de preferencias propios, conservando
  el `app_code` de serialización Classic por compatibilidad;
- perspectiva inicial;
- distribución de paneles;
- base explícita para filtros de comandos futuros, sin filtro activo en G2;
- seis grupos conservadores de toolbar sin operaciones geométricas nuevas;
- branding textual provisional y recursos propios mínimos;
- acceso opcional a una perspectiva Classic para diagnóstico.

No conviene borrar funciones de GeoGebra. Es preferible ocultarlas en el perfil GeoCeDG y conservarlas en el modo diagnóstico, facilitando validación y sincronización upstream.

### 8.2 Arquitectura objetivo de barra

1. Construcción y selección
2. Proyecciones diédricas
3. Cambios de plano, giros y abatimientos
4. Primitivas e incidencia
5. Curvas y superficies
6. Intersecciones y Locus
7. Desarrollos y verdaderas magnitudes
8. Medición y validación
9. Herramientas CeDG
10. Importación y exportación

La lista anterior sigue siendo una arquitectura objetivo para fases posteriores;
no describe la toolbar reducida actualmente visible. G2 implementó únicamente
los grupos seguros que podían sostenerse sin cambiar semántica ni anticipar
herramientas CeDG.

### 8.3 Fuente de verdad adoptada

La fuente adoptada es `apps/geocedg/application-profile.yml`, validada contra el
contrato UI. Un adaptador la traduce a la cadena de modos de GeoGebra; no se
mantienen varias cadenas manuales divergentes. Véanse el
[ADR 0001](../adr/0001-geocedg-product-profile.md), la
[especificación del perfil](../../geocedg/specs/ui/application-profile.md) y el
[informe G2](../validation/g2_frontend_foundation_report.md).

---

## 9. Packaging e instalador propio

La propuesta inicial recomendó `jpackage` por su runtime autocontenido y por la
posibilidad de generar instaladores nativos. G4/G4R concretaron y validaron el
siguiente pipeline para Windows:

```text
installDist
-> filtro determinista de binarios nativos Windows
-> jpackage app-image
-> ZIP autocontenido / MSI / EXE
-> manifest, hashes y SBOM
```

La asociación `.ggb` se incluye solo en MSI/EXE. El toolchain validado utiliza
JDK 25 con `jpackage`, .NET SDK y WiX Toolset 5.0.2. Linux y macOS no están
validados ni deben presentarse como plataformas soportadas. Los outputs actuales
se etiquetan `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`.

`PACKAGING TECHNICAL STATUS = PASS`; la redistribución pública sigue bloqueada
pendiente de aprobación de licencias y assets. La capacidad técnica no constituye
autorización jurídica de distribución. Véanse el
[ADR 0004](../adr/0004-standalone-windows-packaging.md), la
[especificación Windows](../../geocedg/specs/packaging/windows-packaging.md) y el
[informe G4](../validation/g4_standalone_packaging_report.md).

El primer instalador no debe reutilizar el instalador oficial de GeoGebra ni sus recursos de marca.

---

## 10. Sistema de prompt files y capa operativa

### 10.1 Autoridades

- `AGENTS.md`: reglas duraderas del repositorio;
- `.github/prompts/canonical/`: prompts comunes;
- `.github/prompts/tasks/`: solicitudes ejecutables;
- `.github/prompts/reviews/`: auditorías;
- `ai-shell/prompts/`: perfiles breves;
- `tools/agent/`: autoridad ejecutable.

Los prompts no deben duplicar especificaciones. Deben referenciarlas.

### 10.2 Plantilla de tarea

```markdown
# Objective

# Authority and evidence hierarchy

# Scope

# Explicitly forbidden scope

# Architectural placement

# Required design/specification

# Geometric invariants and degeneracies

# Compatibility and serialization

# Required tests and commands

# Required artifacts

# Stop conditions
```

### 10.3 Autoridad ejecutable adoptada

`tools/agent/verify.ps1` es la única autoridad compuesta. Los verificadores de
capacidad permanecen subordinados y se incorporan solo al existir una fase que
los justifique. A cierre de G5, la estructura relevante es:

```text
tools/agent/
├─ verify.ps1
├─ verify-baseline.ps1
├─ verify-operational.ps1
├─ verify-frontend.ps1
├─ verify-legacy.ps1
├─ verify-packaging.ps1
└─ verify-dxf.ps1
```

El onboarding reside separadamente en `tools/bootstrap/bootstrap-windows.ps1` y
los benchmarks en `tools/benchmark/run.ps1`. Los informes resumen evidencia; no
sustituyen el resultado de los verificadores.

---

## 11. Diseño de `Locus V2`

> **Antecedente conceptual, no implementación actual.** La
> [caracterización G6A](g6_locus_v2_plan.md), su
> [modelo semántico](../architecture/locus_v2_semantic_model.md), el
> [contrato normativo](../../geocedg/specs/locus/locus-v2-semantics.md) y
> [ADR 0006 Accepted](../adr/0006-parallel-locus-v2-semantic-entity.md)
> superseden cualquier detalle incompatible de esta formulación histórica.

## 11.1 Definición

Sea el dominio del objeto conductor:

\[
\Omega = \bigcup_{j=1}^{m} I_j,
\]

y sea la construcción dependiente:

\[
F_j : I_j \setminus D_j \rightarrow \mathbb{R}^{2}.
\]

Entonces:

\[
L = \bigcup_{j=1}^{m} F_j(I_j \setminus D_j).
\]

Históricamente, los intervalos \(I_j\) se describieron aquí como componentes o
ramas orientadas. El contrato normativo G6 ya no identifica ambos conceptos:
una rama es una solución constructiva semántica y su subconjunto válido puede
tener varios componentes separados por \(D_j\).

La lista de puntos mostrada en pantalla es solo una aproximación de \(L\).

## 11.2 Componentes internas

```text
LocusDefinition2D
  ├─ driver/domain
  ├─ branches and orientation
  ├─ invalid intervals
  └─ dependency provenance

LocusEvaluator2D
  ├─ evaluate(branch, parameter)
  ├─ derivative/tangent if available
  └─ symbolic/analytic metadata

LocusMetricIndex
  ├─ adaptive world-space partition
  ├─ cumulative length
  ├─ error estimates
  ├─ branch bounding boxes
  └─ spatial index

LocusRenderCache
  └─ viewport-specific tessellation

LocusIntersectionSolver
  └─ candidate isolation and parameter refinement
```

La caché de render no puede ser usada como autoridad métrica.

## 11.3 Longitud

Para una rama absolutamente continua:

\[
s_j(a,b)=\int_a^b \|F'_j(\omega)\|\,d\omega.
\]

Casos:

- derivada e integral analíticas disponibles: solución analítica/simbólica;
- derivada evaluable: cuadratura adaptativa;
- solo evaluación puntual: subdivisión adaptativa con estimación de error;
- discontinuidad o rama no acotada: longitud parcial o estado indefinido explícito.

### API experimental

```text
LocusLength[L]
LocusLength[L, A, B]
LocusLength[L, A, B, branch, mode, tolerance]
```

Tras validar la semántica, se podrá integrar en la familia estándar `Length`.

### Ambigüedades obligatorias

- curva cerrada: recorrido orientado o mínimo;
- auto-intersección: un punto geométrico puede tener varios parámetros;
- varias ramas: A y B deben identificar rama;
- puntos arbitrarios: no se proyectan silenciosamente;
- punto creado sobre Locus: conserva rama y parámetro;
- dominio infinito: la longitud total puede no existir.

## 11.4 Intersecciones

Para un objeto implícito \(G(x,y)=0\):

\[
h_j(\omega)=G(F_j(\omega))=0.
\]

Para otro objeto paramétrico \(Q(v)\):

\[
F_j(\omega)-Q(v)=0.
\]

### Algoritmo

1. detección gruesa con cajas por rama/BVH;
2. aislamiento de candidatos;
3. refinamiento en los parámetros originales;
4. verificación del residuo;
5. clasificación: transversal, tangente, múltiple, solapada;
6. asignación de identidad dinámica estable.

No basta buscar cambios de signo: una tangencia puede tener raíz de multiplicidad par.

### Fases

- L1: recta, segmento, semirrecta, circunferencia y cónica;
- L2: función y curva implícita;
- L3: locus-locus y `Path` genérico;
- L4: tramos coincidentes/solapados.

## 11.5 Compatibilidad

- feature flag `cedg.locus.v2`;
- modo legacy para ficheros anteriores;
- serialización de versión semántica;
- ejecución dual para diagnóstico;
- migración documentada;
- ninguna variación silenciosa de resultados.

## 11.6 Pruebas

### Geometría analítica

- recta y segmento;
- circunferencia;
- elipse;
- parábola;
- curva trascendente;
- cúspide;
- lazo y auto-intersección;
- varias ramas;
- discontinuidad;
- tangencia casi doble;
- solape;
- rama no acotada.

### Modelos CeDG

- mordedura/penetración cono-cilindro;
- iluminación focal de esfera;
- desarrollo de cono oblicuo por curva soporte esférica;
- codos cilíndricos y cónicos con número discreto de virolas;
- helicoide desarrollable;
- convoluta con zonas propias y no propias.

### Invariantes

- independencia de zoom, DPI y tamaño de ventana;
- determinismo;
- longitud acumulada monótona;
- residuo de intersección dentro de tolerancia;
- identidad estable mientras no cambie la topología;
- invalidación correcta de cachés;
- fallo explícito en degeneraciones.

---

## 12. Exportación DXF

### Decisión adoptada en G5

La exportación permanece fuera del kernel y consume una representación neutral
de solo lectura. La frontera implementada es:

```text
GeoElement
-> GeoElementGeometryExportAdapter
-> GeometryExportModel
-> DxfExporter
-> ASCII DXF AC1015
```

El exportador no resuelve ni reinterpreta geometría. G5 exporta la construcción
2D completa mediante una API independiente de los diálogos Desktop, con unidad
DXF `UNITLESS`, coordenadas de modelo invariantes frente al zoom y normalización
determinista de layers y estilos soportados.

### Alcance G5 validado

- representación exacta: puntos, segmentos, rectas, semirrectas,
  circunferencias, arcos circulares, elipses, arcos elípticos, polígonos y
  polilíneas;
- no soportado en G5: parábolas, hipérbolas, curvas generales, texto y `Locus`
  legacy;
- sin recorte implícito por viewport y sin aproximaciones silenciosas.

La propuesta inicial de exportar un `Locus` general como curva paramétrica o
polilínea adaptativa queda **superseded by
[ADR 0005](../adr/0005-neutral-2d-geometry-export.md)**. El `Locus` legacy no
constituye una autoridad geométrica independiente de su muestreo y G5 no lo
exporta. La futura exportación de locus deberá consumir la semántica aprobada de
`Locus V2`; toda representación aproximada deberá declarar expresamente su
naturaleza, tolerancia y error.

La variante, los mappings, pérdidas de estilo, invariantes y tipos no soportados
están definidos en la
[especificación G5](../../geocedg/specs/export/geometry-export-foundation.md) y
registrados en el
[informe de validación G5](../validation/g5_native_2d_dxf_export_report.md).

---

## 13. Semántica espacial CeDG y proyecciones canónicas

Esta capacidad constituye una extensión estructural de CeDG y debe preceder al DSL. La prueba de concepto existente asocia vistas y objetos mediante listas y eventos JavaScript; GeoCeDG debe transformar esa idea en objetos y relaciones nativas, tipadas, serializables y dependientes del kernel.

### 13.1 Entidades mínimas

```text
SpatialObject3D
├─ stableId y semanticVersion
├─ tipo geométrico y dimensionalidad
├─ parámetros y dominio de validez
├─ dependencias constructivas
├─ estado exacto/numérico/discreto
├─ ProjectionSet
└─ definición primitiva o composición espacial

ProjectionFrame
├─ stableId
├─ plano y triedro de referencia
├─ operador de proyección
├─ tipo de proyección
└─ unidades y orientación geométrica

ProjectionBinding
├─ spatialObjectId
├─ projectionFrameId
├─ uno o varios GeoElement 2D
├─ rol: defining | derived | auxiliary | analysis | presentation
├─ correspondencia paramétrica/topológica
├─ procedencia constructiva
└─ validez y degeneraciones

CanonicalProjectionCertificate
├─ schemaId y versión
├─ conjunto de bindings usados
├─ predicados de suficiencia
├─ predicados de no degeneración
├─ ambigüedades y equivalencias permitidas
└─ revisión del grafo para la que es válido
```

La identidad espacial será `SpatialObject3D`. Las proyecciones serán representaciones geométricas de primer nivel vinculadas a esa identidad, no simples etiquetas ni objetos 2D visualmente coincidentes.

### 13.2 Definición formal de suficiencia

Sea \(X_T\) el espacio de configuraciones válidas de un tipo geométrico \(T\), y sea un conjunto de operadores de proyección conocidos

\[
\Pi = \{\pi_1,\ldots,\pi_k\}.
\]

La aplicación conjunta es

\[
\Phi_{T,\Pi}:X_T\rightarrow Y_1\times\cdots\times Y_k,
\qquad
\Phi_{T,\Pi}(x)=\bigl(\pi_1(x),\ldots,\pi_k(x)\bigr).
\]

Un conjunto de proyecciones será **canónico y definitorio** para \(T\) en un dominio \(U\subseteq X_T\) cuando:

1. sea completo: \(\Phi_{T,\Pi}(x)=\Phi_{T,\Pi}(y)\) implique \(x\sim_T y\), donde \(\sim_T\) representa únicamente las equivalencias geométricas expresamente admitidas;
2. exista un procedimiento constructivo de reconstrucción \(R_{T,\Pi}\) sobre los datos válidos;
3. la reproyección de \(R_{T,\Pi}\) sea coherente con las proyecciones de entrada;
4. se satisfagan los predicados de no degeneración y correspondencia del esquema;
5. la reconstrucción y sus restricciones formen parte del grafo de dependencias CeDG.

Para objetos con topología discreta, la suficiencia se evaluará dentro de una clase topológica declarada. No se aceptará una reconstrucción que cambie silenciosamente de rama, conectividad u orientación.

### 13.3 Proyecciones canónicas de primitivas

La fase empezará con esquemas tipados, no con una regla universal basada solo en el número de vistas.

- **Punto.** Dos proyecciones ortogonales en planos no paralelos, con triedros conocidos y correspondencia explícita, definen el punto.
- **Recta.** Deben existir vistas y correspondencias suficientes para reconstruir posición y dirección. Si una recta es de punta a un plano, su proyección sobre ese plano degenera en un punto; ese binding conserva valor representativo, pero no puede ser la única proyección direccional definitoria. El esquema exigirá otra vista no colapsada o una primitiva equivalente que aporte la dirección perdida.
- **Plano.** Se definirá mediante primitivas suficientes —por ejemplo, tres puntos no colineales o dos rectas incidentes— y sus bindings canónicos; no mediante una silueta sin correspondencia espacial.
- **Circunferencia, cónica y curvas espaciales.** Se preferirá su definición por centro, plano, radio, focos, directrices, generatrices u otras primitivas constructivas. Dos curvas proyectadas solo definen una curva 3D cuando existe correspondencia entre puntos; un parámetro común, especialmente el proporcionado por Locus V2, constituye una correspondencia válida.
- **Esfera, cilindro, cono y cuádricas.** Sus esquemas canónicos se basarán en los elementos geométricos que las determinan: centro, eje, vértice, directriz, radio, secciones o parámetros equivalentes. Las curvas de contorno serán proyecciones derivadas, no necesariamente la definición primaria.

Cada tipo tendrá un `CanonicalProjectionSchema<T>` versionado con precondiciones, degeneraciones y procedimiento inverso.

### 13.4 Objetos compuestos, superficies y sólidos

GeoCeDG no debe copiar un flujo CAD de extrusión/booleanas como fundamento. Se propone un **modelo de frontera proyectiva CeDG**, construido en tres niveles:

1. **Primitivas y curvas espaciales**, definidas por construcciones y bindings canónicos.
2. **Superficies y caras**, donde cada cara contiene una superficie soporte, bucles de contorno orientados, incidencias y sus proyecciones asociadas.
3. **Objeto compuesto o sólido**, definido por una agregación jerárquica de componentes y, cuando proceda, por una frontera cerrada, orientada y topológicamente válida.

```text
ProjectiveBoundaryObject3D
├─ components: SpatialObject3D[]
├─ vertices / spatial curves / edges
├─ faces
│  ├─ supportingSurface
│  ├─ orientedBoundaryLoops
│  ├─ projectionBindings
│  └─ validityDomain
├─ adjacency and incidence relations
├─ orientation / inside-outside policy
└─ closure and manifold diagnostics
```

Esta estructura toma de B-Rep la utilidad de vértices, aristas, caras y orientación, pero conserva como autoridades CeDG las construcciones, las dependencias y las proyecciones enlazadas. Las operaciones CSG podrán añadirse después como procedimientos constructivos, nunca como sustitución automática de esa trazabilidad.

Un objeto complejo será canónico cuando lo sean sus primitivas definitorias y, además, sean suficientes sus relaciones de incidencia, orientación, correspondencia de bordes y topología. La mera coincidencia visual de contornos no será una prueba de identidad.

### 13.5 Flujo dinámico y compatibilidad

El flujo deberá ser bidireccional en sentido semántico, aunque no necesariamente editable en ambos sentidos durante la primera implementación:

```text
proyecciones definitorias
    -> reconstrucción/actualización de SpatialObject3D
    -> validación y certificado
    -> generación de proyecciones derivadas
    -> comparación de coherencia
```

Reglas:

- cambios en cualquier proyección definitoria propagan al objeto y a las proyecciones derivadas;
- una vista degenerada cambia el estado del certificado, no elimina silenciosamente el objeto;
- una contradicción entre vistas produce `INCONSISTENT_PROJECTIONS` con diagnóstico;
- una definición insuficiente produce `UNDERDETERMINED`;
- varias soluciones admisibles producen `AMBIGUOUS`, salvo que el esquema incorpore una regla constructiva que seleccione una rama;
- los ficheros heredados permanecen sin asociaciones hasta que exista una migración o asociación explícita;
- los vínculos se serializan por identificadores estables, nunca por etiquetas visibles.

La primera versión puede ofrecer edición autoritativa desde las proyecciones canónicas y tratar la vista 3D como derivada. La edición 3D bidireccional se estudiará después, cuando exista una política no ambigua de propagación hacia las construcciones CeDG.

### 13.6 API interna propuesta

Nombres provisionales, sujetos a inspección del código real:

```text
SpatialObject3D
ProjectionFrame
ProjectionBinding
ProjectionAssociationService
CanonicalProjectionSchema<T>
CanonicalProjectionValidator
SpatialReconstructionService
ReprojectionService
SpatialCompositionBuilder
ProjectiveBoundaryObject3D
ProjectionConsistencyReport
```

La prueba de concepto previa con `ListAssociations3DViews`, `ObjectViewCouple` y listeners JavaScript se conservará como evidencia de investigación y caso de regresión, no como API final.

### 13.7 Validación de G9

Casos mínimos:

- punto general y punto sobre plano de proyección;
- recta general y recta de punta a cada plano;
- plano general y plano proyectante;
- circunferencia en plano general;
- curva espacial con parámetro común en dos proyecciones;
- tetraedro reconstruido desde vistas ortográficas;
- cilindro y cono definidos por primitivas;
- objeto compuesto con caras y orientación;
- cambios dinámicos que atraviesan una degeneración;
- contradicción deliberada entre vistas;
- serialización y reapertura sin pérdida de identidad.

Propiedades exigidas:

- reconstrucción–reproyección dentro de tolerancia;
- independencia de zoom y DPI;
- identidad estable mientras no cambie la topología;
- diagnóstico explícito de suficiencia, ambigüedad y degeneración;
- determinismo;
- compatibilidad con el grafo de dependencias;
- ausencia de inferencias basadas solo en nombres, etiquetas o proximidad visual.

---

## 14. Capacidades posteriores a la semántica espacial

### 14.1 Capas mejoradas

La capa actual de GeoGebra es insuficiente como sistema de organización técnica. GeoCeDG debe incorporar un modelo jerárquico y serializable con:

- grupos y subcapas;
- nombre, rol y orden explícitos;
- visibilidad por vista;
- bloqueo de selección y edición;
- indicadores `print`, `export` y `construction`;
- estilos y filtros por rol;
- estados de capa guardables;
- compatibilidad con la capa numérica heredada.

Roles iniciales: `reference`, `construction`, `auxiliary`, `result`, `hidden`, `dimension`, `annotation`, `layout` y `experimental`.

La pertenencia a una capa no cambia la existencia geométrica ni la validez del objeto. Se distinguirán tres conceptos: existencia, visibilidad geométrica y visibilidad de interfaz.

### 14.2 Zoom, navegación y sistemas de escala extendidos

Debe separarse rigurosamente:

```text
coordenadas del modelo
!= transformación de la vista
!= escala de dibujo
!= escala física de impresión
```

Funciones objetivo:

- zoom centrado en el cursor;
- tecla configurable para activar zoom de ventana con anclaje en la posición actual del cursor;
- `ZoomPrevious`, `FitSelection`, `FitLayer` y vistas nombradas; el
  `ZoomWindow` básico requerido por el workspace Construction se adelanta a
  G9U1 mediante el seam de rectángulo de vista existente;
- navegación precisa por teclado;
- perfiles de escala muy grandes o muy pequeños;
- conservación del centro y escala por vista;
- ausencia de influencia del zoom en métricas, intersecciones o certificados canónicos.

### 14.3 Visibilidad propia en proyecciones

No se reducirá a ocultar objetos completos. Para una dirección de proyección, el motor debe poder particionar una curva o arista en subdominios:

```text
VISIBLE | HIDDEN | SILHOUETTE | COINCIDENT | AMBIGUOUS
```

La visibilidad depende de la geometría espacial, las caras orientadas y la dirección de proyección. Por ello se implementará en un servicio compartido Java conectado al modelo espacial de G9. La salida será una `VisibilityPartition` con puntos de transición, residuos/tolerancias y procedencia. La GUI solo aplicará estilos a esa partición.

Se empezará por poliedros y superficies primitivas, y se ampliará después a superficies y loci espaciales generales.

### 14.4 Conversión a la vista 3D

La primera versión será unidireccional:

```text
SpatialObject3D CeDG -> adaptador -> objetos/vista 3D de GeoGebra
```

La vista 3D será una representación derivada y diagnóstica. La prueba de coherencia consistirá en reproyectar el objeto mostrado en 3D y comparar el resultado con sus bindings canónicos. La cobertura se ampliará desde puntos, rectas y planos a curvas, superficies y objetos compuestos.

No se mantendrán dos verdades geométricas independientes. La edición directa de la vista 3D quedará deshabilitada o limitada hasta que exista una política de actualización inequívoca hacia las construcciones CeDG.

### 14.5 Hojas, PDF y formatos gráficos

Se añadirá un espacio de presentación separado del espacio del modelo:

```text
DrawingSheet
├─ tamaño físico y orientación
├─ marco y cajetín
├─ unidades y escala
├─ Viewport[]
├─ estilos de impresión
├─ capas incluidas
└─ metadatos de publicación
```

La salida PDF deberá respetar exactamente el tamaño físico de la hoja y del marco, con geometría vectorial siempre que sea posible. Las pruebas comprobarán `MediaBox`, márgenes, escala y longitudes físicas conocidas. Se incorporarán además SVG y PNG de alta resolución; DXF permanece en G5 como exportación geométrica. Otros formatos se añadirán mediante adaptadores, no mediante lógica duplicada.

### 14.6 Rendimiento y escalabilidad

El rendimiento no se pospone hasta la última puerta. Desde G0 se registrarán benchmarks y cada fase tendrá presupuesto de regresión. La fase final de optimización solo aplicará cambios sustentados por perfiles.

Métricas mínimas:

- tiempo de arranque;
- latencia de actualización del grafo;
- evaluación y teselación de Locus V2;
- intersecciones;
- reconstrucción/reproyección 3D;
- visibilidad;
- render 2D/3D;
- serialización;
- memoria y tamaño de cachés.

La optimización priorizará, según evidencia: invalidación incremental, cachés por revisión del grafo, índices espaciales, reducción de objetos temporales, evaluación perezosa y partición segura de tareas. No se introducirá C++ ni paralelismo complejo sin demostrar primero un cuello de botella estable y un contrato claro de integración.

G10R medirá específicamente el pipeline de estudios: coste por candidato,
recomputaciones del DAG, ausencia de render y efectos laterales, memoria,
cancelación, determinismo y cachés acotadas por revisión coherente. G16 conserva
la responsabilidad distinta de optimizar el sistema completo sobre todos los
benchmarks acumulados. La optimización matemática de un diseño en G10 no es
optimización de rendimiento del software.

---

## 15. Roadmap por puertas

### Estado consolidado

| Puerta | Estado | Evidencia / condición |
|---|---|---|
| G0 | `PASS` | [Baseline](../validation/baseline_report.md) |
| G1/G1R | `PASS` | [G1](../validation/g1_operational_layer_report.md) y [G1R](../validation/g1r_repository_onboarding_report.md) |
| G2 | `PASS` | [Frontend foundation](../validation/g2_frontend_foundation_report.md) |
| G3 | `PASS` | [Controlled legacy integration](../validation/g3_controlled_legacy_integration_report.md) |
| G4/G4R | `TECHNICAL PASS` | [Packaging](../validation/g4_standalone_packaging_report.md); redistribución pública `BLOCKED` pendiente de licencia/assets |
| G5 | `PASS` | [Native 2D DXF export](../validation/g5_native_2d_dxf_export_report.md); feature integrada con estado experimental |
| G6A | `PASS — AUTHOR APPROVED` | [Informe G6A](../validation/g6a_locus_v2_characterization_report.md), [contrato normativo](../../geocedg/specs/locus/locus-v2-semantics.md) y [ADR 0006 Accepted](../adr/0006-parallel-locus-v2-semantic-entity.md) |
| G6B | `PASS` | [Informe G6B](../validation/g6b_locus_v2_kernel_report.md); entidad experimental interna, sin superficie pública |
| G6R | `PASS` | [Informe G6R](../validation/g6r_locus_v2_hardening_report.md); hardening, laboratorio developer-only y optimización de render medida |
| G7 | `PASS` | [G7A reejecutado](../validation/g7a_locus_v2_metric_characterization_report.md), [R1 acotado](../validation/g7a_r1_locus_v2_metric_refinement_report.md) y G7B `PASS — AUTHOR APPROVED`; spec normativa y ADR 0007 Accepted |
| G8 planning | `PASS — AUTHOR APPROVED` | [Plan G8](g8_locus_v2_intersections_plan.md); cerrado antes de G8A |
| G8A | `PASS — AUTHOR APPROVED` | [Informe y decisiones G8A](../validation/g8a_locus_v2_intersection_characterization_report.md); 65 probes test-private, referencias independientes, spec normativa y ADR 0008 Accepted, sin implementación productiva |
| G8B-R1 | `PASS — AUTHOR APPROVED` | [Refinamiento de admisibilidad local](../validation/g8b_r1_locus_v2_intersection_point_admissibility_report.md); Option B normativa, completitud global preservada, sin superficie pública ni Level C |
| G8B | `PASS — AUTHOR APPROVED` | [Núcleo interno y evidencia G8B](../validation/g8b_locus_v2_intersection_kernel_report.md); mínimo line/segment/ray/circle aprobado como implementación interna |
| G8C design | `PASS — AUTHOR APPROVED` | [Diseño G8C](g8c_locus_v2_extended_intersections_design.md); subdivisión G8C1/G8C2 aprobada; contratos de ambas fases normativos |
| G8C1 | `PASS — AUTHOR APPROVED` | Kernel interno tipado: cónicas no degeneradas, funciones reales explícitamente acotadas e implícitas polinómicas regulares; 38 pruebas focales |
| G8C2 | `PASS — AUTHOR APPROVED` | Kernel interno Locus V2 × Locus V2; 34 pruebas focales, evidencia y verificación canónica aprobadas |
| G8 | `PASS — AUTHOR APPROVED` | Arquitectura nativa 2D Locus V2 y cobertura tipada G8B/G8C1/G8C2 cerradas; capacidad todavía interna/experimental |
| G9P-R1 / G9P | `PASS — AUTHOR APPROVED` / `PASS — AUTHOR APPROVED` | [Plan integrado](../architecture/g9p_integrated_plan.md), seis especificaciones normativas, ADR 0010–0015 Accepted y [decisiones D1–D8](../validation/g9p_author_decisions.md); ninguna implementación productiva |
| G9O1 | `PASS — AUTHOR APPROVED` | Precedente operacional cerrado; no es dependencia semántica de G9A1 |
| G9A1 | `PASS — AUTHOR APPROVED` | Identidad/persistencia durable sin solving espacial cerrada; no autoriza A2 |
| G9A2 | `PASS — AUTHOR APPROVED` | Núcleo espacial y piloto projection-defined de punto cerrados; 64 pruebas focales y repetición determinista sin fallos; su cierre no autorizó A3 por sí solo |
| G9A3 | `PASS — AUTHOR APPROVED` | Hardening de ciclo de vida, migración explícita, copy closure, redefine y recuperación cerrados; completa el track G9A |
| G9A | `PASS — AUTHOR APPROVED` | Identidad/persistencia durable, piloto POINT y ciclo de vida/migración cerrados en G9A1–G9A3 |
| G9U0 | `PASS — AUTHOR APPROVED` | Superficie pública Locus V2 experimental y desactivada por defecto; 93 pruebas focales y repetición determinista sin fallos; candidato de 114 rutas preservado |
| G9U0-R1 | `PASS — AUTHOR APPROVED` | Hardening correctivo de preview, creación pública Desktop, handoff secuencial vacío y reconstrucción aislada de constantes canónicas; 6/6 pruebas R1, smoke manual autoral y autoridad compuesta completas |
| G9X1 | `PASS — AUTHOR APPROVED` | DXF extendido externo al kernel; 62/62 escenarios focales y 10/10 regresiones G5, repetición determinista, sidecar condicional obligatorio y escritura pareada validados; experimental y default-off |
| G9U0-R2 planning/design | `PASS — AUTHOR APPROVED` | Autoridad normativa para la puerta pre-G9U1 de presentación/continuidad Locus V2 e identidad documental nativa `.cedg`; no afirma comportamiento implementado |
| G9U0-R2 implementation | `PASS — AUTHOR APPROVED` | El smoke original R2-L11 se conserva; corrección, 31+31, regresiones, gates auxiliares, composed y re-smoke autoral pasan; `selfApproved=false`, `authorApproved=true`, `passClaimed=true`; MSI/registro real `NOT_REQUESTED` |
| G9U0-R3 | `PASS — AUTHOR APPROVED` | Hardening frontend acotado: ciclo de vida del menú, invisibilidad Euclidian del auxiliar exacto y etiquetas compactas que desacoplan layout e identidad; conserva el smoke que halló el ancho y registra la corrección, reemplazo automatizado y re-smoke PASS; no cambia kernel/XML/identidad ni implementa markers |
| G9U0-R4 | `PASS — AUTHOR APPROVED` | Correctivo kernel acotado: determinismo > continuidad; selector durable de componente/germen enriquecido solo ante colisión con orientación, tipo de dominio, cardinalidad y fase/rango intrínseco; certificado topológico periódico adaptativo separado del selector, sin `span / 256`. Ledger v4 conserva claims dormant/quarantined y reactiva el mismo `GeoPoint` únicamente bajo el mismo selector actual; no auto-crea puntos. Se preservan tres fallos intermedios; los re-smokes finales de cuatro raíces y recurrencia/reactivación pasan. Autoridad 27+28+3 = 58 y 54 rutas/31 bajo `source/`; A/B deterministas y composed PASS; checkpoint `4ef2c9d` es evidencia pre-correctiva; `selfApproved=false`, `authorApproved=true`, `passClaimed=true` |
| G9U0-R5 | `PASS — AUTHOR APPROVED` | Siete formas ordinarias `Translate`/`Rotate`/`Reflect`/`Mirror`/`Dilate` crean un nuevo Locus V2 semántico con ID/DAG propios; Option A `k=0` conserva `FINITE`/`UNBOUNDED` y añade `COLLAPSED_IMAGE`; smoke dinámico aceptado con limitación de entrada libre G9A caracterizada; `selfApproved=false`, `authorApproved=true`, `passClaimed=true`; no autoriza G9U1 |
| G9S1 | `PASS — AUTHOR APPROVED` | Option B: `SplineV2` crea un nuevo `GeoLocusV2` semántico con dominio, spans y knots explícitos; Classic `Spline` permanece intacto; total/parcial scalar `Length` y autoridad rica `LocusLength` validadas; `selfApproved=false`, `authorApproved=true`, `passClaimed=true` |
| G9U0-R6 | `PASS — AUTHOR APPROVED` | Puerta kernel acotada entre G9S1 y G9U1: request geométrica transitoria -> resultado tipado con cero/uno/varios preimages -> selección explícita -> punto ordinario con address semántica editable en DAG. Incluye Locus V2, SplineV2 y transformadas R5, además del cruce periódico bidireccional/path-independent del mismo punto y el negativo unresolved sin mutación; no implementa `Path`, Point-tool/frontend ni comando paralelo. ADR 0019 está Accepted y la spec es normativa; `manualGuiSmoke=DEFERRED TO G9U1 BY DESIGN`, `kernelDiagnosticAcceptance=PASS`, `selfApproved=false`, `authorApproved=true`, `passClaimed=true` |
| G9U1 | `DESIGN PASS — AUTHOR APPROVED / POST-R1 RECONCILED / IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW` | Checkpoint post-R6 `00982e7e148a634cd57ed928f322774df267d5e3` inmutable; R1 publicado satisface la capacidad de pares certificada. Auditoría sin novedad material; autorización condicional actual satisfecha. 11 familias, 18 clusters, 110 acciones y 163 escenarios; Point R6 distinto de materialización de tokens R1. La implementación round 2 está completa como candidato sucesor, pendiente de re-smoke y closeout explícitos del autor; nunca autoaprobada |
| G9U1 Round 3 | `TECHNICAL STABILIZATION CANDIDATE — VALIDATION/AUTHOR CLOSEOUT PENDING` | Sucesor acotado de `5f492d4ee77289d9def89aa6ed431226d2de3457`; conserva 11 familias, 18 clusters y 110 acciones. Corrige el gesto Álgebra/EDT de `kesc`, reconcilia macro embebida con paquete instalado equivalente, añade icono PNG app-only, promociona branding autoral versionado y proyecta menús/toolbar desde la única autoridad schema-v2. No modifica main, no crea tag PASS y no sustituye el re-smoke autoral |
| G9B / G9C | `DESIGNED — NOT AUTHORIZED` | Track kernel tras cierre de G9A; no depende de completar el cliente G9U1 |
| G9U2 | `BLOCKED ON THE APPROVED G9 GATE` | Workspace de procedimientos diédrico solo tras `G9 PASS — AUTHOR APPROVED` |
| G9 spatial solving | `POINT PILOT — AUTHOR APPROVED` | G9A2 se limita a frames/sistemas/mapas/relaciones y reconstrucción projection-defined de punto; no hay primitivas generales, objetos compuestos ni autoridad 3D |
| G10P | `PASS — AUTHOR APPROVED — PLANNING/CHARACTERIZATION ONLY` | Corpus y dirección de estudios/optimización aprobados como planificación; no autoriza producto ni altera G9 |
| G10A/G10B/G10C1/G10C2/G10U/G10R | `PROPOSED — NOT AUTHORIZED — NOT STARTED` | Toda implementación espera `G9 PASS — AUTHOR APPROVED`, G10P aprobado y prompts separados |
| G11–G16 | `NOT STARTED` | Conservan sus alcances y puertas posteriores; G16 sigue siendo rendimiento global |

Los estados `experimental` describen madurez de una capacidad, no una puerta
fallida. Un gate solo se marca `PASS` cuando satisface sus validaciones; un
blocker de alcance parcial debe quedar visible, como la redistribución pública
de G4.

### Reglas de mantenimiento y cierre documental

Este roadmap debe actualizarse cuando una fase cambie de estado, se subdivida o
modifique su alcance. Un ADR aceptado puede superseder una propuesta anterior y
el roadmap debe registrar esa supersesión. Las specs son la autoridad técnica
normativa de cada capacidad; los informes de validación son evidencia histórica
de ejecución; y la user guide describe las capacidades observables actuales.
Estas funciones documentales no deben convertirse en autoridades concurrentes.

Antes de que una fase pueda cerrarse como `PASS`, se debe revisar
`docs/user/geocedg_user_guide.md`. Si la fase cambia capacidades observables,
comandos de ejecución, instalación o workflow de usuario, la guía debe
actualizarse para reflejar los comandos, la interfaz, los manifiestos de
funciones, las capacidades disponibles, las limitaciones y el estado realmente
validados. El manual no debe anticipar como implementada ninguna capacidad que
siga pendiente en esta planificación.

## G0 - Gobierno, licencia y línea base

**Estado:** `PASS`

**Trabajo**

- crear fork y remotos;
- congelar SHA;
- reproducir build;
- mapear módulos;
- inventariar licencias;
- introducir `AGENTS.md`;
- crear `tools/agent/verify-baseline.ps1`;
- registrar una línea base inicial de rendimiento sin optimizar.

**Salida**

- build desktop reproducible;
- informe de línea base;
- matriz de licencias;
- mapa de extensiones 2D, 3D, vistas, capas y exportación;
- repositorio limpio;
- ninguna modificación funcional.

## G1/G1R - Esqueleto operativo y onboarding reproducible

**Estado:** `PASS`

**Trabajo**

- carpetas;
- prompt files;
- ADR;
- manifiestos de modelos;
- verificadores;
- CI básica;
- harness de benchmarks y modelos de estrés.

**Salida**

- flujo de agente reproducible;
- autoridad de fuentes definida;
- presupuestos de regresión inicialmente informativos;
- importación todavía no realizada.

## G2 - Perfil GeoCeDG y frontend

**Estado:** `PASS`

**Trabajo**

- launcher/config;
- perspectiva;
- toolbar desde manifiesto;
- recursos iniciales;
- modo Classic diagnóstico;
- feature flags.

**Salida**

- aplicación identificada como GeoCeDG;
- sin cambio semántico en el kernel;
- pruebas de arranque y layout.

## G3 - Herramientas heredadas no permanentes

**Estado:** `PASS`

**Trabajo**

- inventario de `.ggb`, `.ggt`, GGBScript;
- manifiestos;
- carga en laboratorio;
- métricas y casos degenerados.

**Salida**

- herramientas reproducibles y optativas;
- ninguna promoción automática a estable.

## G4/G4R - Packaging e instalador propio

**Estado:** `TECHNICAL PASS`; redistribución pública `BLOCKED` pendiente de
aprobación de licencia y assets

**Trabajo**

- `jpackage app-image`;
- EXE/MSI Windows;
- recursos propios;
- asociación de fichero;
- manifiesto/SBOM.

**Salida**

- instalación y desinstalación limpia;
- aplicación arranca sin instalación Java externa;
- no se usa instalador oficial.

## G5 - Infraestructura de exportación geométrica 2D y DXF

**Estado:** `PASS` (capacidad DXF `experimental`)

**Trabajo**

- representación neutral de solo lectura;
- adaptador GeoElement y servicio exportador independiente de la GUI;
- escritor ASCII DXF AC1015;
- layers, unidades y estilos con políticas explícitas;
- UI Desktop GeoCeDG y validación semántica del resultado.

**Salida**

- exportación reproducible de entidades 2D soportadas;
- tipos no soportados y pérdidas declarados sin aproximación silenciosa;
- invariancia frente al zoom y regresión estructural/geométrica.

## G6 - Locus V2

**Estado:** `G6 PASS`; G6A `PASS — AUTHOR APPROVED`; G6B `PASS`; G6R `PASS`

La [planificación ejecutable detallada](g6_locus_v2_plan.md) incorporó la
primera revisión del autor y G6A ejecutó la caracterización autorizada. La
segunda revisión aprobó el
[contrato normativo](../../geocedg/specs/locus/locus-v2-semantics.md), fixtures,
mediciones y mapa de impacto, y aceptó
[ADR 0006](../adr/0006-parallel-locus-v2-semantic-entity.md). El autor autorizó
después G6B mediante el prompt canónico endurecido y fijado por hash.

Los dos modelos de intersección cono-cilindro suministrados por el autor
reproducen el límite legacy con roles distintos. El modelo `TwoLevels` es el
control funcional (aproximadamente 125–127 ms). En el modelo patológico, el
estado previo a `Flatten` midió aproximadamente 31.9 ms; las tres creaciones de
tercer nivel tardaron aproximadamente 6.03, 5.95 y 5.67 s, terminaron
indefinidas al superar el guard legacy de 500 ms por paso y dejaron una
recomputación posterior de aproximadamente 21.0 s. La instrumentación muestra
que `AlgoLocusSliderND` actualiza por muestra un dependency slice con dos loci
interiores y dos `AlgoPerimeterLocus`, regenerando trabajo upstream. Esta causa
se limita a los artefactos medidos. Los originales están fijados por hash bajo
`models/legacy/`; no son autoridad semántica V2 ni están autorizados para
redistribución pública.

- **G6A — mathematical/semantic characterization and contract:** definición,
  parámetro semántico proporcionado por el provider, ramas/componentes válidos,
  degeneraciones, garantía numérica, composición anidada, clasificación,
  compatibilidad y diseño de validación; caracterización cerrada `PASS` sin
  implementación productiva del kernel.
- **G6B — minimal Locus V2 kernel implementation:** implementación mínima
  productiva pero interna/experimental. Añade clasificación V2 distinta,
  definiciones/ramas/evaluator, providers estrechos, algoritmos con dependencias
  normales, evaluación anidada recursiva con sesión compartida acotada y un
  drawable derivado. El fixture controlado demuestra tres niveles y mide
  también profundidad cinco sin regeneración upstream.
- **G6R — hardening and developer laboratory:** endurece value/lifecycle/session
  contracts, conserva los gates funcionales anidados, adopta teselación
  adaptativa exclusivamente visual tras medición, y añade un laboratorio
  opt-in con preferencias temporales. No registra comando, `Path`, XML,
  métrica, intersección ni UI pública.

El paquete incluye el
[modelo semántico](../architecture/locus_v2_semantic_model.md), el
[mapa de impacto upstream](../architecture/locus_v2_upstream_impact.md), la
[matriz de validación](../validation/g6_locus_v2_validation_matrix.md), el
[plan de benchmarks](../validation/g6_locus_v2_benchmark_plan.md) y el
[ADR 0006 Accepted](../adr/0006-parallel-locus-v2-semantic-entity.md). Las
secciones conceptuales previas de este roadmap siguen siendo material de
partida; la spec enlazada es la autoridad normativa. El resultado se documenta en el
[informe G6A](../validation/g6a_locus_v2_characterization_report.md) y el
[informe G6B](../validation/g6b_locus_v2_kernel_report.md). Existe una entidad
V2 productiva interna, pero no un comando, workflow de usuario, persistencia,
`Path`, métrica, intersección, exportación o comportamiento 3D. Classic y el
comando público `Locus[...]` de GeoCeDG continúan usando exclusivamente el
locus legacy.

G6R añade únicamente un workflow **developer-only** y explícito mediante
`tools/locus-v2/open-locus-v2-laboratory.ps1`; no cambia la disponibilidad
pública. Su arquitectura, API, trazabilidad y evidencia se registran en el
[mapa de implementación](../architecture/locus_v2_implementation.md), la
[referencia API](../developer/locus_v2_api.md), la
[matriz G6R](../validation/g6r_locus_v2_traceability_matrix.md) y el
[informe G6R](../validation/g6r_locus_v2_hardening_report.md).

## G7 - Métricas nativas Locus V2

**Estado:** `PASS — G7A AND G7B AUTHOR APPROVED`

El [paquete G7 restaurado](g7_locus_v2_metrics_plan.md) incorpora las decisiones
conceptuales revisadas por el autor. La
[spec métrica](../../geocedg/specs/locus/locus-v2-metrics.md) es normativa y el
[ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md) está Accepted.
G7A midió y el autor aceptó `LAZY_COMPONENT_REVISION` con
`DEDICATED_SHARED_OWNER`. Solo se comparte estado métrico inmutable de
componente; las contributions de ruta se derivan después. G7B implementa ahora
esa arquitectura como API productiva interna, sin superficie métrica pública.

### G7A - Caracterización métrica

**Estado:** `PASS — AUTHOR APPROVED`

**Trabajo reejecutado**

- medir tolerancias absolutas/relativas, integración, error y límites
  impropios con referencias independientes;
- auditar `GeoLocusMetricResult`, un `GeoClass` append-only y su lifecycle
  completo;
- comparar no-reuse, índice eager por revisión e índice lazy por componente;
- resolver participación numérica, admisibilidad escalar y efectos en Algebra
  View, algoritmos genéricos y CAS;
- ejecutar traces 1/10/100 y composición métrica anidada con contadores
  funcionales;
- ejecutar el refinement R1 con valores/errores sin sentinels, reutilización
  directa de `NumericGuarantee` G6, work ceilings independientes y ownership
  multi-consumer N=1/3/10/100;
- preparar spec, ADR y presupuestos G7B para revisión final de autor.

G7A usó únicamente probes/tests privados y evidencia versionada. El
[informe](../validation/g7a_locus_v2_metric_characterization_report.md), el
[informe R1](../validation/g7a_r1_locus_v2_metric_refinement_report.md), la
[API candidata](../developer/locus_v2_metric_api.md) y la
[trazabilidad](../validation/g7a_locus_v2_metric_traceability_matrix.md)
registran las 42 decisiones originales y 22 recomendaciones R1 aprobadas por el
autor, además de las tres normalizaciones API finales. Los 51 probes
test-private pasan; no se creó implementación productiva.

### G7B - Kernel métrico mínimo

**Estado:** `PASS — AUTHOR APPROVED`

Las puertas documentales se satisficieron antes de ejecutar G7B: G7A es
`PASS — AUTHOR APPROVED`, la spec es normativa y ADR 0007 está Accepted. El
kernel productivo interno aprobado implementa esta arquitectura:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

Las operaciones `BetweenPositionsMetricQuery` y `TotalLocusMetricQuery` son
distintas. La primera usa un resolver de ruta con `FORWARD`, `REVERSE`,
`ZERO_LENGTH`/`FULL_CYCLE` y `STOP_AT_END`/`WRAP_TO_START`/`STRICT`; la segunda
suma una vez cada componente válido y un ciclo fundamental de cada rama
periódica, sin fabricar conexiones. La longitud usa variación total y
multiplicidad constructiva.

Desde el primer candidato G7B incluye valores cerrados sin sentinels,
`MetricErrorAmount2D` cerrado, traversal estructuralmente ausente en resultados
total, evidencia de error tipada y alineada con G6, work ceilings deterministas,
resultado rico en el DAG, lifecycle completo, keys completas, índice acotado,
owner compartido que conserva solo `LocusMetricComponentState2D`, eviction determinista, publicación P1 atómica,
exception safety, igualdad index ON/OFF y gates funcionales repetidos,
multi-consumer y anidados. Las 62 pruebas productivas y las tres pruebas del
laboratorio constituyen la evidencia focalizada aprobada por el autor; G7 queda
`PASS` sin ampliar la superficie pública.

El [modelo semántico](../architecture/locus_v2_metric_semantic_model.md), la
[arquitectura](../architecture/locus_v2_metric_architecture.md), la
[matriz](../validation/g7_locus_v2_metric_validation_matrix.md), el
[plan de benchmarks](../validation/g7_locus_v2_metric_benchmark_plan.md) y los
prompts [G7A](../../.github/prompts/tasks/g7a-locus-v2-metric-characterization.prompt.md)
y [G7B](../../.github/prompts/tasks/g7b-locus-v2-metric-kernel.prompt.md)
definen los gates.

**Frontera pública efectiva G7B**

- API Java interna;
- `GeoLocusMetricResult`;
- laboratorio developer-only;
- sin comando, cambios en `Length`/`Perimeter`, `Path`, XML, persistencia, 3D,
  G8 o G9.

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7B = PASS — AUTHOR APPROVED
G7 = PASS
G7B CAPACITY 64 = PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT
G8 PLANNING = PASS — AUTHOR APPROVED
G8A = PASS — AUTHOR APPROVED
G8B-R1 = PASS — AUTHOR APPROVED
G8B = PASS — AUTHOR APPROVED
G8 PRODUCTIVE IMPLEMENTATION = INTERNAL MINIMUM KERNEL — AUTHOR APPROVED
G8C DESIGN = PASS — AUTHOR APPROVED
G8C1 = PASS — AUTHOR APPROVED
G8C2 CONTRACT = NORMATIVE — AUTHOR APPROVED
ADR 0009 = ACCEPTED
G8C2 = PASS — AUTHOR APPROVED
G8 = PASS — AUTHOR APPROVED
HISTORICAL G8 CLOSEOUT SNAPSHOT (NOT THE CURRENT G9 STATE):
G9 DESIGN = AUTHORIZED — NOT STARTED
G9 IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
CURRENT G9 STATE:
G9P DESIGN = PASS — AUTHOR APPROVED
G9A1 = PASS — AUTHOR APPROVED
G9A2 = PASS — AUTHOR APPROVED
G9A3 = PASS — AUTHOR APPROVED
G9A = PASS — AUTHOR APPROVED
G9U0 = PASS — AUTHOR APPROVED
G9U0-R1 = PASS — AUTHOR APPROVED
G9X1 = PASS — AUTHOR APPROVED
G9U0-R2 PLANNING / DESIGN = PASS — AUTHOR APPROVED
G9U0-R2 IMPLEMENTATION = PASS — AUTHOR APPROVED
G9U0-R3 = PASS — AUTHOR APPROVED
G9U0-R4 = PASS — AUTHOR APPROVED
authorDecision = INTRINSIC_SEMANTIC_PHASE_RANK_AUTHORIZED
historicalAuthorSmoke1 = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
historicalAuthorSmoke3 = TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE
manualAuthorFinalSmokeFourRoot = PASS
manualAuthorFinalSmokeReactivation = PASS
selfApproved = false
authorApproved = true
passClaimed = true
protectiveCheckpoint = 4ef2c9df433aec7c6385a488a02581358da83f60 (PRE_CURRENT_CORRECTION)
G9U0-R5 DESIGN = PASS — AUTHOR APPROVED
G9U0-R5 IMPLEMENTATION = PASS — AUTHOR APPROVED
G9S1 = PASS — AUTHOR APPROVED
G9U0-R6 = PASS — AUTHOR APPROVED
G9U1 = DESIGN CANDIDATE — PROTECTED / NOT AUTHORIZED
G9B / G9C = DESIGNED — NOT AUTHORIZED
G10 PRODUCTIVE IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

## G8 - Intersecciones 2D

**Estado:** G8A, G8B-R1, G8B, G8C design, G8C1, G8C2 y global G8 `PASS —
AUTHOR APPROVED`; contratos normativos y ADR 0008/0009 `Accepted`; capacidad
interna, experimental y sin superficie pública.

El autor aprueba la arquitectura de planificación, cierra el diseño G8C y
autoriza las ejecuciones separadas de G8C1 y, tras su cierre, G8C2:

1. **G8A — caracterización y decisiones de autor:** probes exclusivamente
   test-private, referencias independientes y medición de solver, tangencia,
   tolerancias, completitud, resultado rico, identidad/topología y trabajo
   acotado. La ejecución separada y la revisión de autor han terminado; la
   puerta está cerrada en `PASS — AUTHOR APPROVED`;
2. **G8B — kernel interno 2D mínimo:** prompt canónico ejecutado; implementación,
   R1, pruebas y evidencia cerrados en `PASS — AUTHOR APPROVED`;
3. **G8C — diseño de incidencia 2D extendida:** `PASS — AUTHOR APPROVED`. La
   auditoría y 32 probes test-private delimitan cónicas no
   degeneradas, funciones reales explícitamente acotadas, curvas implícitas
   polinómicas regulares y Locus V2 × Locus V2. La subdivisión queda aprobada:
   G8C1 (objetivos uniparamétricos) está cerrado en `PASS — AUTHOR APPROVED`;
   el contrato G8C2 (solver biparamétrico) es normativo y su ejecución separada
   está cerrada en `PASS — AUTHOR APPROVED`.

Locus V2 × Locus V2 requiere resolver `F(t) = Q(u)` y por ello introduce un
problema genuinamente bidimensional de parámetros, topología dual, overlap e
identidad. La revisión final contra la implementación G8C1 no encontró
contradicción: el contrato biparamétrico es normativo, ADR 0009 está Accepted y
la tarea canónica G8C2 está cerrada en `PASS — AUTHOR APPROVED`. G8 está cerrado
globalmente; G9 avanzó después mediante sus puertas y autorizaciones separadas.

**Capacidad CeDG fundamental**

Las proyecciones definidas mediante Locus V2 son resultados geométricos CeDG y
deben poder participar como entidades de primera clase en intersecciones con
las familias 2D ordinarias soportadas. Cada solución finita debe mantener
identidad semántica y alimentar construcciones posteriores mediante el DAG
normal cuando la continuación sea inequívoca:

```text
construcción CeDG -> proyección Locus V2 -> intersección 2D identificada
    -> construcción CeDG posterior -> propagación dinámica normal
```

Una coordenada anónima calculada en un instante no satisface esta capacidad.
Debe conservar trazabilidad constructiva, rama/componente/preimagen,
parametrización semántica, identidad dinámica y cambios topológicos explícitos.
Este carácter fundamental no amplía por sí mismo la familia inicial: cada
familia se promueve incrementalmente mediante evidencia.

**Mínimo productivo autorizado para G8B**

- recta, segmento, semirrecta y circunferencia;
- cónicas completas diferidas: G8A confirmó la representación de ecuación,
  pero no un contrato uniforme cerrado de completitud/degeneración;
- aislamiento por componente semántico, refinamiento y verificación residual;
- tangencia de multiplicidad par sin depender solo de cambios de signo;
- resultado rico inmutable con ejes separados de cómputo, completitud
  (`COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED`), tipo geométrico, garantía,
  identidad y lifecycle;
- `GeoElement` rico no numérico como autoridad y un consumidor interno
  obligatorio de un punto seleccionado por token semántico; queda indefinido
  sin retargeting si la solución falta, está stale o es ambigua y solo recupera
  el mismo token conforme al contrato;
- completitud global y admisibilidad de una solución son ortogonales: un root
  finito, verificado, localmente aislado y sin ambigüedad puede alimentar ese
  punto con padre `INCOMPLETE` o `NOT_ESTABLISHED`, conservando visible esa
  procedencia y sin afirmar exhaustividad;
- identidad duradera basada en el par de fuentes, linaje constructivo/de rama y
  contexto topológico; parámetro, intervalo aislante, residuo y certificado son
  evidencia numérica ligada a una revisión, nunca identidad fundamental;
- genealogía merge/split caracterizada: no existe herencia universal en los
  casos simétricos; se usan tokens de evento, relaciones candidatas y
  ambigüedad o discontinuidad explícita si la continuación no es única; y
- política `g8b-initial-normalized/v1`: residuo de significado geométrico común
  o contrato tipado por familia, parámetros en el espacio semántico del
  provider, tangencia normalizada y coordenadas solo como verificación;
- presupuestos funcionales iniciales aprobados provisionalmente desde G8A; y
- estado local a la consulta; no hay índice métrico G7, owner compartido ni
  índice de intersecciones en el mínimo.

**Frontera mantenida**

- `GeoLocus` legacy y Classic no cambian;
- no comando/dispatcher público, `Path`, punto arbitrario sobre V2, XML,
  persistencia o migración;
- no exportación G5, 3D, semántica espacial G9 ni DSL Python;
- `LocusRenderCache2D`, vértices, `myPointList`, viewport, zoom, DPI y
  tolerancias de píxel están prohibidos como autoridad.

El paquete incluye el
[modelo semántico](../architecture/locus_v2_intersection_semantic_model.md), la
[arquitectura](../architecture/locus_v2_intersection_architecture.md), el
[impacto upstream](../architecture/locus_v2_intersection_upstream_impact.md),
la [spec normativa](../../geocedg/specs/locus/locus-v2-intersections.md), la
[matriz de validación](../validation/g8_locus_v2_intersection_validation_matrix.md),
el [plan de contadores](../validation/g8_locus_v2_intersection_benchmark_plan.md),
la [trazabilidad científica](../validation/g8_locus_v2_intersection_scientific_traceability.md),
el [ADR 0008 Accepted](../adr/0008-locus-v2-intersection-result-and-continuation.md)
y los prompts canónicos
[G8A](../../.github/prompts/tasks/g8a-locus-v2-intersection-characterization.prompt.md)
y [G8B](../../.github/prompts/tasks/g8b-locus-v2-intersection-kernel.prompt.md),
además del [informe G8A](../validation/g8a_locus_v2_intersection_characterization_report.md),
su [matriz de trazabilidad](../validation/g8a_locus_v2_intersection_traceability_matrix.md),
el [informe G8B](../validation/g8b_locus_v2_intersection_kernel_report.md), la
[trazabilidad productiva G8B](../validation/g8b_locus_v2_intersection_traceability_matrix.md)
y el [informe enfocado G8B-R1](../validation/g8b_r1_locus_v2_intersection_point_admissibility_report.md).
La extensión dividida se documenta en el
[plan G8C](g8c_locus_v2_extended_intersections_design.md), la
[spec G8C1/G8C2 normativa](../../geocedg/specs/locus/locus-v2-extended-intersections.md),
el [informe de caracterización](../validation/g8c_locus_v2_extended_intersection_characterization_report.md),
el [ADR 0009 Accepted](../adr/0009-locus-v2-locus-intersection-pair-semantics.md),
la [revisión final del contrato G8C2](../validation/g8c2_locus_v2_locus_intersection_contract_review.md),
el prompt histórico ya ejecutado
[G8C1](../../.github/prompts/tasks/g8c1-locus-v2-extended-target-intersections.prompt.md)
y el prompt canónico aún no ejecutado
[G8C2](../../.github/prompts/tasks/g8c2-locus-v2-locus-intersections.prompt.md).

G8A se ejecutó después del cierre de planificación y añadió solo
caracterización test-private. El autor aprobó D1–D17, la spec es normativa y
ADR 0008 está Accepted. El prompt G8B produjo el kernel interno autorizado y
G8B-R1 aplicó Option B, separando la admisibilidad local de una solución de la
completitud global. La revisión final del autor aprueba tanto G8B-R1 como G8B;
ello no abre una superficie pública ni cierra G8 globalmente. La revisión del
15 de agosto de 2026 cierra el diseño G8C, aprueba su subdivisión y promueve el
contrato G8C1. Esa ejecución y su revisión final están cerradas en `PASS —
AUTHOR APPROVED`. La revisión posterior contra el kernel real hace normativo el
contrato G8C2, acepta ADR 0009 y autoriza G8C2. La ejecución posterior, sus 34
pruebas focales, evidencia y verificación completa han recibido aprobación
autoral. El conjunto G8A/G8B/G8C1/G8C2 satisface el gate fundamental de
incidencia 2D y cierra G8 globalmente sin ampliar la superficie pública.

La siguiente instantánea conserva literalmente el estado histórico del cierre
G9A1; no describe el estado actual de G9A2, que se registra en la sección G9.

```text
G8 PLANNING = PASS — AUTHOR APPROVED
G8A = PASS — AUTHOR APPROVED
G8B-R1 = PASS — AUTHOR APPROVED
G8B = PASS — AUTHOR APPROVED
G8 SPEC = NORMATIVE / AUTHOR-APPROVED R1 REFINEMENT APPLIED
ADR 0008 = ACCEPTED — R1 CLARIFICATION APPLIED
G8 PRODUCTIVE IMPLEMENTATION = INTERNAL MINIMUM KERNEL — AUTHOR APPROVED
G8C DESIGN = PASS — AUTHOR APPROVED
G8C1 = PASS — AUTHOR APPROVED
G8C2 CONTRACT = NORMATIVE — AUTHOR APPROVED
ADR 0009 = ACCEPTED
G8C2 = PASS — AUTHOR APPROVED
G8 = PASS — AUTHOR APPROVED
G9P-R1 = PASS — AUTHOR APPROVED
G9P = PASS — AUTHOR APPROVED
G9 SPECIFICATIONS = NORMATIVE / AUTHOR APPROVED
ADR 0010–0015 = ACCEPTED
G9O1 = PASS — AUTHOR APPROVED
G9A1 = PASS — AUTHOR APPROVED
G9A2 = PASS — AUTHOR APPROVED
G9A3 = PASS — AUTHOR APPROVED
G9A = PASS — AUTHOR APPROVED
G9U0 = PASS — AUTHOR APPROVED
G9U0-R1 = PASS — AUTHOR APPROVED
G9X1 = PASS — AUTHOR APPROVED
G9U0-R2 PLANNING / DESIGN = PASS — AUTHOR APPROVED
G9U0-R2 IMPLEMENTATION = PASS — AUTHOR APPROVED
G9U0-R3 = PASS — AUTHOR APPROVED
G9U0-R4 = PASS — AUTHOR APPROVED
authorDecision = INTRINSIC_SEMANTIC_PHASE_RANK_AUTHORIZED
historicalAuthorSmoke1 = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
historicalAuthorSmoke3 = TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE
manualAuthorFinalSmokeFourRoot = PASS
manualAuthorFinalSmokeReactivation = PASS
selfApproved = false
authorApproved = true
passClaimed = true
protectiveCheckpoint = 4ef2c9df433aec7c6385a488a02581358da83f60 (PRE_CURRENT_CORRECTION)
G9U0-R5 DESIGN = PASS — AUTHOR APPROVED
G9U0-R5 IMPLEMENTATION = PASS — AUTHOR APPROVED
G9S1 = PASS — AUTHOR APPROVED
G9U0-R6 = PASS — AUTHOR APPROVED
G9U1 DESIGN = PASS — AUTHOR APPROVED — POST-R6 RECONCILED
G9U1 IMPLEMENTATION = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
G9U1 implementationStarted = true
G9U1 implementationAuthorized = true
G9U1 selfApproved = false
G9U1 authorApprovedDesign = true
G9U1 passClaimedImplementation = false
G9U1 DETERMINISTIC_CONTINUITY_OFF_REQUIRED
G9U1 INTRINSIC_PHASE_RANK_TOKEN_AUTHORITY_REQUIRED
G9U1 MULTI_MATERIALIZATION_REQUIRED
G9U1 PERSISTENT_INSPECTOR_SESSION_REQUIRED
G9U1 AUTO_REACTIVATION_EXISTING_POINTS_KERNEL
G9U1 AUTO_MATERIALIZATION_FRONTEND_ONLY
G9B = DESIGNED — NOT AUTHORIZED
G9C = DESIGNED — NOT AUTHORIZED
G9U2 = BLOCKED ON THE APPROVED G9 GATE
G9 SPATIAL SOLVING = POINT PILOT — AUTHOR APPROVED; GENERAL PRIMITIVES NOT STARTED
```

## G9 - Semántica espacial y proyecciones canónicas

**Estado:** G9P-R1, G9P, G9O1, G9A1, G9A2, G9A3, el track G9A, G9U0,
G9U0-R1, G9X1, G9U0-R2, G9U0-R3, G9U0-R4, G9U0-R5 y G9S1 `PASS — AUTHOR
APPROVED`. Option B publica `SplineV2` como nuevo Locus V2 semántico y conserva
Classic `Spline`.
G9A cierra identidad/persistencia durable, el piloto projection-defined de punto
y su ciclo de vida/migración; G9U0 cierra la superficie pública Locus V2
experimental. G9X1 está cerrado en `PASS — AUTHOR APPROVED`, con exportación
externa, experimental y desactivada por defecto. La planificación/diseño
G9U0-R2 planning/design e implementación están en `PASS — AUTHOR APPROVED` y
sus contratos son normativos. El smoke autoral original falló R2-L11 y se
conserva como evidencia histórica; la corrección acotada, toda la automatización
incluida composed y el re-smoke autoral pasan. `.cedg` es comportamiento nativo
de GeoCeDG, `.ggb` queda como entrada de compatibilidad y el refinamiento visual
R2 está cerrado. R3 conserva esa autoridad y cierra la exposición pública
frontend del menú/inspector, del auxiliar visual de token y del selector acotado
tras smoke y re-smoke autorales. R4 conserva tres smokes históricos: el primero
materializó dos raíces pero invalidó un punto durante movimiento regular; el
segundo encontró cuatro raíces que todavía no podían materializarse y el tercero
materializó cuatro pero perdió definedness durante movimiento regular. El
correctivo adaptativo y ledger-v4 supera A/B 58/58 y la autoridad composed
completa; los re-smokes finales de cuatro raíces y
reactivación del mismo punto pasan y el autor cierra R4. G9U1, G9B y G9C siguen
sin autorización; G9U2
permanece bloqueada por la puerta G9 aprobada. El
[plan integrado G9P](../architecture/g9p_integrated_plan.md) y el
[paquete de decisiones](../validation/g9p_author_decisions.md) gobiernan el
contrato. A1 implementa identidad/persistencia durable y A2 activa únicamente
el piloto espacial projection-defined de punto. El
[diseño aprobado G9U0-R2](../architecture/g9u0_r2_product_refinement_design.md)
registra la inserción posterior sin reescribir el cierre histórico G9P.

### Dependencias y orden recomendado posteriores a G9P

```text
track kernel:   G9A1 --> G9A2 --> G9A3 --> G9B --> G9C

track producto: G9A3 --> G9U0 --> G9U0-R1 --+
                                               +--> G9U0-R2 --> G9U0-R3 --> G9U0-R4 --> G9U0-R5 --> G9S1 --> G9U0-R6 --> G9U1
                G5 + autoridad G6-G8 --> G9X1 -+

checkpoint editorial programado: G9U0-R4 --> BOOK-P1 --> G9U0-R5
                                (track independiente; no arista semántica software)

G9C + G9U1 + evidencia G9O1 --> cierre global G9 --> G9U2

G9O1: primero por recomendación operacional; sin arista semántica hacia G9A1.
```

El diagrama expresa dependencias semánticas/contractuales, no un calendario.
Se distinguen: (1) dependencias duras, (2) predecesores de ejecución
recomendados y (3) puertas de cierre global/release. El orden de bajo conflicto
recomendado pasa a ser `G9O1; A1; A2; A3; U0; U0-R1; X1; U0-R2; U0-R3; U0-R4; BOOK-P1; U0-R5; S1; U0-R6; U1; B; C;
cierre; U2`, pero
los puntos y coma no son flechas semánticas. Tras A3, el track kernel B/C puede
avanzar sin U1. U0 sí requiere A3 para publicar objetos persistentes. X1 puede
consumir snapshots internos G6-G8 y declarar su `id_scope`; ejecutar U0 antes
de X1 sigue recomendado para la integración pública. R2 requiere como puerta de
entrada el cierre ya aprobado de U0-R1 y X1, pero X1 no se convierte en
autoridad semántica de estilos o documentos. R3 es un correctivo público
frontend separado que ya cerró `PASS — AUTHOR APPROVED`. R4 ya cerró la
admisibilidad/continuación pública. BOOK-P1 es una revisión editorial
independiente intencionalmente programada antes de R5, no una dependencia del
software. R5 ya cerró las transformaciones semánticas 2D como `PASS — AUTHOR
APPROVED`; G9S1 también cerró `PASS — AUTHOR APPROVED`. U1 integra las acciones
aprobadas solo después de una autorización autoral U1 todavía separada.

### G9O1 - Bundles de conocimiento y guías operativas

**Estado:** `PASS — AUTHOR APPROVED`

Implementa el generador determinista y los perfiles aprobados, con
clasificación de propiedad, exclusión de material restringido, hashes raw/
canónicos, política de árbol sucio, budgets y guías reproducibles. No cambia
geometría. Su evidencia participa en el cierre operacional global. G9O1 no es
una dependencia semántica de G9A1 y su cierre no aprobó por sí mismo A1; la
aprobación posterior de G9A1 queda registrada en su propia puerta.

### G9A1 - Fundamento de identidad y persistencia

**Estado:** `PASS — AUTHOR APPROVED`

G9A1 añade IDs tipados durables y registro por construcción para geos,
objetos, frames, sistemas de proyección, mapas de diagrama, relaciones de frame
y bindings; XML versionado, copy/remap, undo/reopen, colisiones y transacción
explícita de redefine compatible. No resuelve geometría espacial y no reutiliza
`ceID`, etiquetas, orden o coordenadas.

El cierre de autor confirma 62 pruebas focales G9A1 y las 55 pruebas upstream
de redefine sin fallos, errores ni omisiones, Checkstyle limpio, verificación
estática/evidencial sellada y autoridad compuesta sin `-SkipBuild`. Se revisó
`docs/user/geocedg_user_guide.md`: no requiere cambio porque A1 no añade ningún
comando, UI, flujo de usuario ni capacidad activada por defecto. La evidencia
del candidato conserva su significado histórico y su hash canónico-LF. Este
cierre no autoriza ni ejecuta G9A2 ni ninguna fase posterior.

### G9A2 - Núcleo espacial y piloto de punto

**Estado:** `PASS — AUTHOR APPROVED`

La ejecución separada autorizada implementa frames, roles, bindings, evaluación
del sistema de proyecciones y el piloto projection-defined para punto. Separa
`q_i = pi_i(x)` de la colocación geométrica diédrica
`p_i = delta_i(q_i)`, con relaciones de charnela/cambio de plano,
reconstrucción intrínseca, reproyección intrínseca+diagrama, estados
independientes y publicación atómica en el DAG. La vista 3D sigue derivada y no
existe bucle bidireccional. La feature permanece experimental y desactivada por
defecto.

El cierre de autor confirma 64 pruebas focales sin fallos, errores ni omisiones,
una repetición determinista completa, persistencia real, referencias analíticas
vigentes, Checkstyle limpio, verificación estática/evidencial sellada y
autoridad compuesta sin `-SkipBuild`. Se aceptan los límites documentados:
piloto solo de punto, residuales binary64 como evidencia numérica, adaptador 3D
unidireccional sin smoke dedicado de renderer y restauración determinista del
adaptador transitorio por la siguiente publicación normal del DAG.

Se revisó `docs/user/geocedg_user_guide.md`: no requiere cambio porque G9A2 no
añade comandos, GUI, flujo de usuario ni capacidad activada por defecto. El
informe y la evidencia del candidato conservan su significado histórico y el
hash canónico-LF de la evidencia
`28ea3021c7b6bc191407a8c7a0138d8b5fc12c9c512cbc2c2b5ff4bc93f7e77c`.
Este cierre no autoriza ni ejecuta G9A3 ni ninguna fase posterior.

### G9A3 - Ciclo de vida y migración

**Estado:** `PASS — AUTHOR APPROVED`

La ejecución autorizada endurece mutaciones de bindings/sistemas/mapas/
relaciones, copy closure, undo/reopen y las rutas hostiles de redefine.
Recomputación conserva ID; redefine explícito y semánticamente compatible solo
puede transferirlo mediante la transacción tipada aprobada; reemplazo real o
incompatible, delete+recreate y copy crean IDs nuevos. También cubre referencias
rotas, ficheros heredados no asociados, migración explícita y recuperación
determinista sin inferencia por etiqueta, coordenadas, proximidad, orden, posición
XML ni referencia Java.

El camino diagnóstico GeoCeDG Classic preserva y recomputa tipos nativos e
identidades sin downgrade; la apertura en un upstream externo que desconozca
esos tipos permanece fuera de garantía. Dos ejecuciones enfocadas deterministas
cerraron 72 escenarios G9A3 más 181 regresiones heredadas, 253/253 sin fallos,
errores ni skips; Checkstyle main/test, la autoridad compuesta sin `-SkipBuild` y
la verificación estática/inventario terminaron limpias. La auditoría independiente
no encontró P0/P1 bloqueantes.

El [informe del candidato](../validation/g9a3_spatial_lifecycle_migration_report.md)
y su evidencia máquina conservan históricamente el inventario de 81 rutas y sus
afirmaciones originales `authorApproved=false` y `passClaimed=false`; el cierre
autoral posterior no reescribe esa evidencia. La guía de usuario fue revisada y
no cambia porque G9A3 no añade comandos, GUI, flujos observables ni capacidad
activada por defecto. Este cierre completa formalmente G9A. En el momento de
ese cierre, G9U0, G9X1, G9U1, G9B, G9C y cualquier implementación productiva
G10 seguían sin autorizar y sin ejecutar; G9U2 continuaba bloqueada por la
puerta G9 aprobada. El cierre posterior de G9U0 se registra a continuación.

### G9U0 - Superficie pública experimental Locus V2

**Estado:** `PASS — AUTHOR APPROVED`

Implementa un generador semántico 1D reconstructible con un único driver/
dominio explícito: estado escalar `u -> t(u)` o punto con preimagen sobre
segmento, circunferencia, arco circular o rama/componente Locus V2. Incluye
`L1 -> punto -> L2`, continuidad, seams periódicos, ciclos rechazados por el
DAG, creación V2 sin redirigir `Locus`, métrica rica con adaptador escalar
guardado obligatorio para `Length[GeoLocusV2]`, Intersect general, token-punto,
persistencia, ayuda y política Classic. G9U0 inspeccionó las convenciones reales
de overload y la revisión autoral aceptó la sintaxis mapped-scalar elegida.
GeoCeDG Classic conserva objetos V2/rich nativos y nunca los degrada
silenciosamente para un upstream externo.

El candidato aprobado queda preservado en
`f5904c6138e24889642ca5c9648096c4784adcf5`, con 114 rutas exactas, 93/93
pruebas focales en dos ejecuciones deterministas, Checkstyle y autoridad
compuesta completos, y todos los contadores hard-zero y de exclusión de alcance
a cero. La capacidad conserva madurez `experimental` y permanece desactivada
por defecto. Los commits operacionales concurrentes de `main` se integraron como
historia independiente y no forman parte del alcance G9U0. No se ejecutó ninguna
fase posterior G9 ni implementación G10.

#### G9U0-R1 — Hardening correctivo de creación pública y ciclo de vida

**Estado:** `PASS — AUTHOR APPROVED`

R1 conserva íntegros la matemática, la API pública experimental, la identidad
durable y la política default-off aprobadas en G9U0. Aísla el preview de la
publicación durable, admite únicamente el merge vacío de instrumentación en un
handoff secuencial bootstrap/EDT —sin relajar el confinamiento de evidencia no
vacía— y evita reconstruir constantes canónicas como `yAxis` dentro del
evaluador aislado. Las 6/6 regresiones R1, las 93/93 históricas G9U0, las
autoridades G9A/legacy, Checkstyle y la verificación compuesta pasaron. El autor
reprodujo manualmente el caso original círculo → `C` → `D` en `yAxis` →
`E = Midpoint(C,D)` → `LocusV2(E,C)`, incluida su manipulación dinámica, sin
falso error CAS, y aprobó el cierre. La incidencia separada de guardado quedó
fuera de alcance de R1; el diseño aprobado G9U0-R2 la trata solo después
del cierre G9X1 y sin reinterpretar el PASS histórico. Este cierre no autoriza G9X1, G9U1, G9B, G9C, G9U2 ni ninguna
implementación productiva G10.

### G9X1 - DXF extendido exacto/aproximado

**Estado:** `PASS — AUTHOR APPROVED`

El candidato preserva el corpus exacto G5 y añade outcomes por componente,
preflight, sidecar obligatorio para toda reducción de fidelidad y escritura
pareada cuando corresponde, antes de aproximaciones acotadas por dominio
semántico. Toda aproximación es export-only, determinista y ajena al
Construction/render. El modo parcial rechaza por defecto; una opción futura
exigiría intención explícita, aviso y sidecar. El muestreo solo establece
`ESTIMATED_ERROR`; `SPLINE` e implícitas siguen diferidas. Las coordenadas
continúan sin unidad física, los IDs ordinarios conservan alcance de revisión de
construcción y la escritura pareada no declara atomicidad universal de dos
ficheros. Su dependencia dura es G5 más los contratos internos G6-G8; U0 fue el
precedente de integración recomendado. La
[evidencia de cierre](../validation/g9x1_extended_dxf_implementation_candidate_report.md)
registra 62/62 pruebas G9X1, 10/10 regresiones G5, repetición determinista,
21 contadores de autoridad prohibida a cero y verificación compuesta completa.
El cierre retiene los riesgos documentados, mantiene el gate experimental
`cedg.export.dxf.extended` desactivado por defecto y no autoriza G9U1, G9B,
G9C, G9U2 ni implementación productiva G10.

### G9U0-R2 — PRE-G9U1 PRODUCT / DOCUMENT REFINEMENT

**Estado de planificación/diseño:** `PASS — AUTHOR APPROVED`

**Estado de implementación:** `PASS — AUTHOR APPROVED`; el smoke autoral
original falló R2-L11 y se conserva, mientras la corrección acotada, toda la
automatización incluida composed y el re-smoke interactivo pasan;
`implementationAuthorized=true`, `implementationStarted=true`,
`selfApproved=false`, `authorApproved=true` y `passClaimed=true`. El smoke real
de MSI/registro queda `NOT_REQUESTED`.

**Racional de nombre:** R2 es el siguiente refinamiento acotado de la familia
G9U0 después de G9U0-R1. Conserva la convención `-R<n>` ya usada por G7A,
G8B, G9P y G9U0, evita ocupar el `G9U1A` ya reservado para el futuro
schema/compiler de workspace y no crea una familia paralela. Pese al sufijo,
R2 es una puerta de ejecución independiente con prompt, verifier, evidencia y
aprobación propios. El
[diseño aprobado](../architecture/g9u0_r2_product_refinement_design.md)
documenta la comparación completa.

**Entrada ejecutada:** el autor invocó por separado el prompt canónico R2 sobre
el commit aprobado `ce022b756b51fe12497e1932ba3ae58093dd1405`; G9U0-R1 y
G9X1 conservan `PASS — AUTHOR APPROVED`, ADR 0016 y las dos specs normativas
permanecen como autoridad. Esta autorización inició únicamente R2 y no
constituyó por sí sola PASS ni autorización de otra fase; el PASS llegó mediante
la decisión autoral final registrada en este cierre.

**Alcance aprobado:** (1) integrar Locus V2 con color, grosor, tipo de línea,
show/hide, presentación de etiqueta aplicable, Properties, selección/highlight,
estilo persistente, copy y undo/redo ordinarios sin tocar semántica;
(2) demostrar continuidad de render equivalente a una curva ordinaria cuando
una línea, circunferencia o cónica cruza el locus, sin crear gap, componente o
subpath artificial; y (3) establecer `.cedg` como extensión documental nativa
de GeoCeDG, con `.ggb` como entrada de compatibilidad no destructiva, manteniendo
el ZIP/XML validado y `app_code: classic` mientras no exista evidencia separada
que justifique otra decisión.

**Exclusiones:** R2 no convierte Locus V2 en `Path`, no cambia identidad,
revisión, generador, dominio, ramas/componentes, métricas, intersecciones,
solution tokens ni autoridad del DAG semántico, no crea un
modelo paralelo de estilos, no serializa teselación y no implementa workspace
v2, G9B, G9C, G9U2 ni G10. Tampoco promete que un upstream externo pueda abrir
tipos GeoCeDG desconocidos ni autoriza exportación `.ggb` con downgrade.

**Política documental aprobada:** en GeoCeDG normal, Save/Save As nativos terminan en `.cedg`;
una extensión omitida añade `.cedg`; Save sobre un `.ggb` abierto deriva a Save
As nativo y nunca sobrescribe el origen; reopen, recientes, drag/drop y apertura
directa admiten `.cedg` y `.ggb`. GeoCeDG Classic abre y preserva `.cedg` sin
downgrade ni creación habilitada, pero esa capacidad no cambia por sí sola su
identidad predeterminada de documento nuevo. Un `.cedg` corrupto falla cerrado;
MSI/EXE Windows asocian `.cedg` mediante ProgID propio y los formatos portables
no crean asociación. No se congela MIME arbitrario ni se reclama validación de
asociación fuera de Windows. La extensión enruta I/O pero nunca infiere
semántica o migración desde el nombre.

**Validación/salida:** la corrección R2-L11 amplía el inventario a
51 rutas/26 `source/` y mantiene 31+31 tests. Focused A/B deterministas,
regresiones G9U0-R1/G9U0/G9X1/G5/G9A/legacy Locus, packaging, Checkstyle,
checks Git y `R2-R07` composed pasan. El smoke autoral fallido se conserva y el
re-smoke correctivo fue aceptado por el autor. El resultado es
`PASS — AUTHOR APPROVED`; no se reclama smoke instalado MSI/registro.

**Relación con G9U1 y fases retenidas:** G9U1 pasa a ser la siguiente puerta del
roadmap, pero no una ejecución autorizada. El PASS de R2 satisface su prerequisito
de orden; solo otra decisión explícita del autor podrá autorizar G9U1. G9B y G9C siguen
independientes de este track de producto y no están autorizadas; G9U2 conserva
su bloqueo global y G10 productivo sigue sin autorización.

```text
G9U0-R2 PLANNING / DESIGN = PASS — AUTHOR APPROVED
planningSelfApproved = false
planningAuthorApproved = true

G9U0-R2 IMPLEMENTATION = PASS — AUTHOR APPROVED
implementationAuthorized = true
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS
installedMsiRegistrySmoke = NOT_REQUESTED

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

### G9U0-R3 — PUBLIC LOCUS V2 UI EXPOSURE HARDENING

**Estado:** `PASS — AUTHOR APPROVED`

**Entrada y continuidad:** R3 parte de
`88801ba540cceeaeb1c2366be3c3a8d705f1b09d`. El tag anotado
`geocedg-g9u0-r2-pass` conserva como peel
`9694dd4c3c274f627839d0eb5d2827a7910bf0ca`, ancestro de esa entrada. El único
commit intermedio, `Consolidate BOOK-P0-post operations`, modifica el puente del
libro, guías y tooling operacional/editorial ya documentado; no reemplaza la
autoridad de producto R2 ni introduce una línea competidora.
El verificador R2 conserva ahora ese mismo encadenamiento mediante el patrón
operacional `TAGGED_DESCENDANT` ya usado por R1/G9X1: fija tag, peel, forma de
commit, ascendencia e inventario R2 51/26, y ejecuta las regresiones sobre la
fuente actual sin relajar la evidencia histórica.

**Alcance:** el `JMenu` propio se poblaba correctamente al inicio, pero el ciclo
heredado `updateFonts()` vaciaba todos los menús y solo los `BaseMenu` upstream
se reconstruían de forma lazy. R3 reutiliza un único método de población tanto
en init/rebuild como después del clear heredado. Conserva el gate DXF
independiente y hace accesibles las cinco acciones Locus V2 con el único opt-in
`--enableLocusV2=true`, incluido el inspector real de resultados ricos. Además,
el `GeoText` auxiliar que mantiene el token exacto del punto materializado sigue
siendo dependencia normal, persistente y remapeable, pero deja de ser visible en
Graphics.

**Exclusiones:** no cambia semántica Locus/intersección, solver, identidad,
contenido/ledger de token, XML, render, `Path`, workspace ni política de
materialización. `Intersect(L,T)` continúa creando cero puntos persistentes; un
punto requiere confirmación explícita. R3 no implementa candidate markers: el
overlay transitorio del resultado activo pertenece al diseño futuro G9U1.

**Corrección de re-smoke:** el primer smoke fue funcionalmente satisfactorio,
pero `TokenChoice.toString()` exponía el token opaco completo y Swing lo usaba
para dimensionar el selector y el diálogo. El candidato conserva ese token
íntegro como entrada exacta, muestra solo ordinal transitorio localizado y
clasificación, y mantiene el diagnóstico completo en un área acotada con wrap.
El ordinal no es identidad ni dato persistente.

**Cierre:** los 22 escenarios R3 más 17 regresiones frontend pasan en dos
ejecuciones deterministas idénticas, junto con las autoridades históricas y
composed. Se conserva `MANUAL SMOKE — FUNCTIONALLY PASSING, UI WIDTH DEFECT
FOUND`; la corrección acotada posterior y el re-smoke autoral pasan.
`implementationStarted=true`, `selfApproved=false`, `authorApproved=true`,
`passClaimed=true`, `manualAuthorSmoke=PASS` y `manualAuthorReSmoke=PASS`.

### G9U0-R4 — PUBLIC LOCUS V2 INTERSECTION INITIAL ADMISSIBILITY AND CONTINUATION CORRECTION

**Estado:** `PASS — AUTHOR APPROVED`

**Entrada y reproducción:** R4 parte del `main` publicado
`ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b`, que es a la vez el peel del tag
anotado `geocedg-g9u0-r3-pass`. La autoridad primaria de reproducción es el
fixture byte-exacto `locusFromMidpoint.cedg` de 13 301 bytes y SHA-256
`47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`.
`Intersect(a,c)` produce dos raíces finitas, actuales, transversales,
localmente aisladas y sin candidatos irresueltos, pero el baseline R3 las
publicaba como `NOT_ESTABLISHED` porque ambas pertenecen al mismo componente y
la capability histórica solo emitía clave cuando había una única raíz por
componente.

**Interpretación normativa:** admisibilidad local no equivale a completitud
global. Una raíz individual puede alimentar una construcción mientras el
resultado conserva `INCOMPLETE` o `NOT_ESTABLISHED`, siempre que su aislamiento,
revisión, contexto constructivo, componente y ausencia de ambigüedad sean
coherentes. La primera publicación correcta usa
`NEW_TOPOLOGICAL_SOLUTION`/`APPEARED`, no una continuación ficticia.
Las secciones 6.2 y 8.2 de la
[especificación normativa de intersecciones](../../geocedg/specs/locus/locus-v2-intersections.md)
registran la refinación R4 determinista sin debilitar las reglas G8.

**Corrección candidata:** el ledger asigna una nueva encarnación opaca a cada
raíz pública inicial actual, transversal, localmente aislada, no ambigua y
no-pair. La asignación se vincula con el contexto durable del resultado y una
dirección semántica exacta de la revisión; el token no codifica coordenadas,
parámetro, ordinal de solver, índice de muestreo ni viewport.

El smoke autoral de ese primer candidato confirmó la admisión y materialización
inicial, pero falló porque un punto quedó indefinido tras un movimiento pequeño
sin transición topológica aparente. La igualdad exacta de parámetro entre
revisiones era una condición incorrecta: una raíz transversal regular cambia
normalmente su parámetro.

La decisión autoral correctiva establece `deterministic semantic selection >
continuity heuristic`. Cada snapshot actual forma primero un selector base con
contexto durable, rama/componente y germen transversal orientado. Si el selector
base es único, se conserva sin rango. Si varias raíces aisladas lo comparten, la
decisión autoral posterior `INTRINSIC_SEMANTIC_PHASE_RANK_AUTHORIZED`, formalizada
en [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md),
permite enriquecerlo con orientación declarada, tipo de dominio,
cardinalidad verificada del grupo y fase/rango inducido por los intervalos
semánticos canónicos disjuntos. Esto no es identidad por orden de enumeración:
el solver puede devolver las raíces en cualquier permutación y la asociación
semántica permanece igual.

El ledger v4 resuelve el token exacto sin usar el parámetro anterior ni la
historia de movimiento, importa estado canónico phase-v3 y estados auténticos
pre-phase v2/v1, rechaza reetiquetados o estados claimed incompatibles y no
cambia el envelope público `locus-root/v3`. El selector durable queda separado
del certificado topológico de la revisión. Una asignación reclamada por un punto
materializado que ordinariamente no es actual se conserva `CLAIMED_DORMANT`: el
mismo `GeoPoint` queda indefinido y el kernel lo reactiva automáticamente, sin
reemplazarlo, solo cuando el mismo selector vuelve a resolver de forma única y
admisible. Liberar el último claim permite podar la asignación. Ninguna
reactivación crea un punto nuevo.

Para un grupo ranked periódico, el correctivo construye un certificado
intrínseco adaptativo de tubos/celdas de fase entre snapshots. Los tubos del
mismo rango deben conservar el selector, permanecer disjuntos y respetar la
separación cíclica; el certificado solo guarda reuse y nunca selecciona la raíz
actual. El umbral fijo `component span / 256` era una cota de actualización, no
evidencia topológica, y fue retirado de esta autoridad. La ausencia de una
relación diagnóstica acotada en un grupo no periódico tampoco se convierte en
identidad ni invalida por sí misma su selector actual único.

En el caso periódico, selector durable y certificado topológico de la revisión
son autoridades separadas. Evidencia cíclica insuficiente o no única conserva
el grupo previo no actual en cuarentena durable; un offset único cero lo libera
y permite reactivar los mismos puntos; un offset único no-cero probado lo
retira permanentemente. Copy exacto conserva la cuarentena pero no puede
certificar su liberación, y liberar el último claim permite podar el grupo.

En un componente no periódico, dos intervalos aislados no pueden intercambiar
su rango orientado sin colisión, pérdida de aislamiento o transición de
topología. En un componente periódico, el intervalo fundamental explícito
define el marco de fase; una raíz cuyo intervalo toca el seam no se etiqueta por
proximidad ni por winding mutable. Una costura/monodromía intrínsecamente
no-reactivable y probada publica `IDENTITY_DISCONTINUITY` y retira de forma
permanente solo los tokens del grupo afectado; los grupos independientemente
certificados permanecen actuales. Este límite preserva la precedencia
determinista aunque no garantice continuidad de token a través del seam. Cambio
de cardinalidad, orientación o componente,
tangencia, merge/split, overlap, candidato irresuelto, stale/revision mismatch
o pair sin aislamiento también falla de forma cerrada y nunca retargetea un
token por coordenada u orden.

La evidencia `unresolved` queda acotada al componente semántico en el que no se
pudo excluir o verificar un candidato; no invalida raíces justificadas de otros
componentes. La orientación canónica del contacto hace invariantes las
representaciones homogéneas/proyectivas equivalentes de rectas, cónicas e
implícitas polinómicas regulares. El rayo conserva deliberadamente su dirección
como semántica y no se reduce a su recta soporte no orientada.

**Exclusiones:** R4 no cambia solving geométrico, completitud, residual,
garantía numérica, estructura XML/ZIP, render, inspector, candidate markers,
`Path`, workspace ni G9U1. El ledger v4 añade únicamente estado durable de claim
active/dormant/quarantined dentro del XML ya autorizado. No introduce la política
`DETERMINISTIC_LOCAL`: las cuatro raíces ya satisfacen la admisibilidad local y
solo estaban bloqueadas por colisión de identidad. Locus V2 × Locus V2 conserva
la autoridad simétrica independiente de aislamiento en dos parámetros y no
recibe rango unilateral.

```text
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorFinalSmokeFourRoot = PASS
manualAuthorFinalSmokeReactivation = PASS
```

La fuente actual declara 27 pruebas públicas, 28 de ledger y 3 Desktop de
archivo nativo (58 en total). Además de la autoridad previa, cubre el update
directo del tamaño observado en UI, la recurrencia 2→4→2 por varias trayectorias
incluido reopen en dormancia, el mismo `GeoPoint` reactivado con el mismo token,
ausencia de auto-creación, claims active/dormant con conteo de referencias,
cuarentena periódica durable, liberación por offset cero, retiro por offset
no-cero probado y copy que no libera la cuarentena. La serialización
emite ledger v4 e importa v3/v2/v1 bajo sus contratos exactos.
La cuarentena `q`/`r` está cubierta en recompute/export-import/copy del ledger;
el tercer test Desktop cubre por separado dormancia `2→4→2`, reactivación del
mismo punto y reopen `.cedg` posterior a la reactivación, no un round-trip
nativo de cuarentena periódica.

El checkpoint protector actual es
`4ef2c9df433aec7c6385a488a02581358da83f60`, creado antes del correctivo
adaptativo/dormant. Sus corridas 50/50 y SHA-256
`f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`, así como
el SHA pre-final `c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`, son
evidencia histórica y no validan la fuente actual. La autoridad productiva de
cierre A/B pasó 58/58 con summary SHA-256 histórico pre-checkout
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`;
composed salió 0 y terminó con `All GeoCeDG verification gates passed.`. Esa
evidencia automatizada no constituyó por sí misma aprobación autoral; los dos
re-smokes finales aportaron la decisión separada del autor. La autoridad de
producto permanece en `63c291464111a5bcdbca488d6639662e46c389c4`, designada
por el tag anotado inmutable `geocedg-g9u0-r4-pass` (objeto
`0f9b303057b00d23722ad1f9d3594b4609d668a7`).

Un correctivo operativo posterior al cierre, sin cambio productivo, endurece
la evidencia de fuente: el hash R4 anterior usaba bytes físicos del worktree y
dependía de la conversión LF/CRLF de `core.autocrlf`. Siguiendo el precedente R3,
las fuentes tracked se leen ahora de los Git blobs sellados por el PASS, el
texto UTF-8 se canoniza a LF y los fixtures `.cedg` conservan hash binario
byte-exacto. La regresión controlada exige LF = CRLF y contenido real distinto
!= hash. La autoridad operacional A/B pasa 58/58 con summary canónico
`1bda6e3b2d3efa350f945ecb1e8e51b7007dba3ea5fce0d97654cade33ceefd9`;
el tag PASS no se mueve y no se modifica Java, geometría, identidad, ledger,
persistencia, frontend ni comandos públicos.

Los smokes históricos permanecen registrados como
`FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION` e
`IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE`; el tercer smoke pasó la parte
de dos raíces y la materialización inicial de cuatro, pero falló después durante
movimiento regular ordinario. Ese fallo demostró que el
guard fijo `span / 256` no era evidencia de topología y motivó el certificado
adaptativo actual. El autor ejecutó después Case 1 de movimiento regular de
cuatro raíces y Case 2 de recurrencia/reactivación, y aceptó ambos como `PASS`.

La ejecución composed de reemplazo expuso la ausencia de localización pública
para `DETERMINISTIC_SELECTION_ESTABLISHED`. La corrección acotada añadió la
misma clave a los bundles base, inglés y español, sin cambiar semántica. El
inventario fuente candidato comprende 54 rutas exactas, 31 bajo `source/`,
incluidos el consumidor de punto materializado y el rich result que gestionan el
claim durable, sin logs ni evidencia generada versionados. El mismo punto puede
reactivarse por recompute del kernel; crear un punto nuevo, marker o acción
create-all permanece exclusivamente frontend, explícito y futuro en G9U1, que
solo podrá consumir tokens actuales. R5 sí se ha ejecutado hasta candidato sin
PASS; G9U1 no se ha ejecutado.

El resolver incremental ordena `R` raíces una vez y valida `P` puntos
materializados por lookup directo: `O(R log R + P)` sin un solve adicional por
hijo ni replay de historia. Los diagnósticos legacy de transición completa
pueden conservar peor caso `O(R²)` porque no seleccionan ni reactivan tokens.

**Riesgo de validación transversal retenido:**
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` (`RETAINED NONBLOCKING`). Su origen
es R4 y su área es el ciclo de vida/persistencia de tokens periódicos. Falta un
round trip nativo `.cedg` dedicado del estado de cuarentena; sí existen pruebas
de recompute/export-import/copy del ledger, dormancia/reactivación no periódica
en `.cedg` y reopen posterior a reactivación. G9U1 debe revisitarlo porque su UX
consume esta autoridad persistida; deberá resolverse o recibir disposición
autoral explícita no más tarde del cierre global G9. No se convierte en requisito
geométrico implícito de R5.

### BOOK-P1 — revisión editorial independiente programada

**Estado:** checkpoint editorial posterior a R4 y anterior a la decisión R5.

BOOK-P1 pertenece al track independiente del libro. Su posición deliberada en
el calendario es `G9U0-R4 PASS -> BOOK-P1 -> G9U0-R5`, pero no es una arista de
dependencia del software, no altera semántica geométrica y no autoriza ni bloquea
por sí mismo una implementación GeoCeDG.

### G9U0-R5 — LOCUS V2 2D SIMILARITY TRANSFORMATIONS

**Estado de diseño:** `PASS — AUTHOR APPROVED`

**Estado productivo:** `PASS — AUTHOR APPROVED`

R5 hace que los comandos ordinarios `Translate`, `Rotate`,
`Reflect`/`Mirror` —solo simetría axial/central, no inversión— y `Dilate`
acepten `GeoLocusV2` y produzcan un nuevo Locus V2 semántico de DAG normal,
conceptualmente `L'(u)=T(L(u))`. El resultado obtiene identidad durable nueva,
mantiene el dominio, branches, componentes, parámetro, orientación y seam
periódico de la fuente, y puede alimentar Point-on-Locus, métricas,
intersecciones, transformaciones posteriores, persistencia, copy y undo/redo.
Las intersecciones transformadas consumirán el selector determinista del
snapshot actual cerrado por R4; covariancia geométrica no reutiliza tokens ni
convierte la historia de movimiento en identidad.

El [diseño aprobado](../architecture/g9u0_r5_locus_v2_similarity_transformations.md),
la [especificación normativa](../../geocedg/specs/locus/locus-v2-similarity-transformations.md),
la [matriz de implementación](../validation/g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md)
y el [prompt canónico](../../.github/prompts/tasks/g9u0-r5-locus-v2-similarity-transformations.prompt.md)
gobiernan la implementación autorizada. No autorizan G9U1.

La escala uniforme `k=0` usa la **Option A — AUTHOR APPROVED**: un locus
semántico degenerado válido que conserva dominio, `FINITE`/`UNBOUNDED`, branches,
componentes, orientación y direcciones inválidas de la fuente, y añade
`COLLAPSED_IMAGE`. Esa propiedad semántica prueba longitud rica cero sobre cada
componente válido sin sanar gaps ni fabricar una raíz de intersección aislada.
No se convierte en un `GeoPoint`; la Option B queda rechazada para R5.

Las siete formas públicas producen un objeto nuevo con ID durable y dependencias
DAG normales. La evaluación consulta primero la fuente. Los interceptores 3D
rechazan fail-closed ejes, planos y centros 3D; la reflexión axial normaliza
coeficientes finitos sin overflow; y un transform R5 rechazado antes de su propia
publicación/redefine revierte sus etiquetas, parent y reservas. Esto no cambia
la regla host que conserva un subcomando anidado ya exitoso si falla después un
comando externo no relacionado. R5 no
modifica `Path`, formato ZIP/XML, solver de intersección, renderer ni G9U1.

El smoke final autoral acepta el deslizador, la edición explícita del numérico
existente en Algebra, transiciones positivas/negativas/cero repetidas y
save/reopen. La expresión libre `k=0.25` se rechaza antes de R5 como G9A
`REDEFINE_CONTEXT_MISSING`; el rechazo es atómico y no corrompe la construcción.
La limitación queda diferida a la UX prospectiva de G9U1 y no es un fallo de
`Dilate`; R5 no amplía G9A. `selfApproved=false`, `authorApproved=true`,
`passClaimed=true`.

### G9S1 — SEMANTIC SPLINE 2D CAPABILITY

**Estado:** `PASS — AUTHOR APPROVED`

```text
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
```

La decisión aprobada es Option B: el nuevo comando experimental `SplineV2`
posee una definición semántica explícita —familia/grado, fuentes, dominio
orientado, spans, knots, continuidad y degeneraciones— y publica un nuevo
`GeoLocusV2` de DAG normal. `Spline` clásico conserva su comando, tipo, XML y
comportamiento upstream; no existe conversión ni migración implícita.

El resultado reutiliza Point-on-Locus, longitud rica total/parcial y el escalar
ordinario guardado `Length(L)` / `Length(L,P,Q)`,
intersecciones ricas, tokens y lifecycle R4, transformaciones R5,
copy/undo/redo y persistencia `.cedg`. La intersección separa descubrimiento,
aislamiento local, clasificación, selector semántico, admisibilidad y
completitud global. Los knots tienen ownership canónico; coordenadas, orden del
solver/lista, samples, viewport, proximidad e historia siguen prohibidos como
identidad. La ruta polinómica unilateral puede alimentar el selector/token R4
solo para una celda transversal localmente aislada; mantiene error estimado y
completitud global `NOT_ESTABLISHED`. La ruta polinómica spline×spline aporta
evidencia rica simétrica mediante cajas Bernstein y refinamiento dual, pero no
prueba todavía unicidad rectangular: no crea continuation key pública, entrada
ledger activa ni punto materializable.

La [ADR 0018 aceptada](../adr/0018-semantic-spline-2d-capability.md),
la [especificación normativa](../../geocedg/specs/curves/semantic-spline-2d.md),
la [arquitectura](../architecture/g9s1_semantic_spline_2d_capability.md),
la [revisión de métodos](../research/g9s1_semantic_spline_numerical_methods.md),
la [matriz](../validation/g9s1_semantic_spline_2d_capability_validation_matrix.md)
y el [prompt canónico](../../.github/prompts/tasks/g9s1-semantic-spline-2d-capability.prompt.md)
gobiernan la implementación cerrada. El prompt tiene canonical-LF SHA-256
`2e940ff75e20113975e02e5a46db0fbd6f57bffd21e22550dca07ee678d2a324`.

El candidato implementa el solver denso flotante compatible con la familia
Classic, sustitución polinómica por span y partición por raíces de la derivada
para targets unilaterales, subdivisión/exclusión Bernstein más Newton dual para
pares ricos y Simpson adaptativo sobre la derivada para longitud. No implementa
Sturm/Descartes ni aritmética intervalar certificada, y no se inventa una cita:
cualquier teorema o garantía superior requiere fuente académica verificada.

El primer smoke autoral pasó salvo la longitud parcial. La caracterización
probó que `LocusLength(L,P,Q)` ya contenía el valor rico correcto, pero el
subarco evaluator-only seguía publicando evidencia
`FLOATING_POINT_UNCERTIFIED` basada en interpolación de coordenada de arco y
`CmdLength` carecía de la ruta V2 de tres argumentos. El candidato reemplazado
integra adaptativamente el intervalo semántico exacto para producir evidencia
local estimada con supuestos explícitos y publica `Length(L,P,Q)` solo mediante
padre rico oculto más el adaptador escalar existente. El re-smoke reducido
pasó: el control Spline V2 publicó `M = 4` y `MP = 2`; el control Locus V2
publicó `LM = 2` usando la clave pública real `generator.main`. La indicación
previa `scalar-locus/main` era una instrucción de smoke errónea, no un defecto
del producto.

**Exclusiones:** no modifica `Spline` clásico, no hace `Path` a
`GeoLocusV2`, no usa render como autoridad, no implementa superficies/3D/CAD,
no ejecuta G9U1 y no cierra por sí sola
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`.

**Salida:** G9S1 y R6 están cerrados por decisión autoral. G9U1 sigue protegido
y sin ejecutar; su requisito kernel R6 está satisfecho, pero aún requiere
reconciliación post-R6, revisión y autorización propias.

### G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT

**Estado:** `PASS — AUTHOR APPROVED`

```text
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN
kernelDiagnosticAcceptance = PASS
```

R6 es la puerta kernel necesaria para que un futuro Point-tool pueda crear y
arrastrar puntos ordinarios sobre Locus V2, SplineV2 y sus imágenes de similitud
R5 sin convertir a `GeoLocusV2` en `Path`. La dirección forward ya aprobada
evalúa una address exacta `(source, branch/component, u)`. R6 añade la dirección
de interacción inversa y acotada:

```text
source semántica + request geométrica transitoria
  -> resultado tipado: ninguno / único / varios / unresolved / invalid /
     degenerate / unsupported
  -> selección explícita de una address
  -> GeoPoint ordinario con estado semántico editable en DAG normal
```

Distancia y proximidad pueden limitar, sembrar o ordenar candidatos para
presentación; nunca constituyen identidad durable. Branch, component,
canonical parameter, orientación y política periódica pertenecen al proveedor.
No se persisten píxeles, click, segmento render, índice de lista/solver ni
historia de movimiento. Un self-intersection devuelve preimages distintos y no
elige silenciosamente.

La ruta estructural afín certificada cubre por completo los componentes finitos
solicitados y puede justificar `none/unique` sin ajustar muestras. El fallback
evaluator-only recorre dominios/componentes mediante subdivisión/refinamiento
deterministas y acotados, pero cero o un candidato local sigue `unresolved`
porque no excluye un mínimo estrecho no muestreado. La especialización SplineV2
reutiliza spans polinómicos G9S1 y los puntos estacionarios de distancia al
request, con ownership canónico de knots. Sus coeficientes x/y se obtienen como
par coherente, la profundidad capturada es O(1), la propagación por similitudes
es lineal en profundidad y tanto la consulta como el techo común 128 limitan la
composición. Toda respuesta candidata se verifica forward; ni coeficientes ni
residuos floating declaran aritmética exacta.

La edición escribe address y parámetro, publica el cascade y comprueba la
postcondición dentro del snapshot/restore normal de `Construction`. Así falla
cerrada sin estado parcial. El snapshot cuesta O(N) por edición, no crea por sí
mismo una entrada undo y un rollback puede reconstruir instancias Java: el
cliente futuro deberá agrupar el gesto válido y abandonar/reobtener objetos tras
un fallo. El estado persistido conserva component lineage exacto; un endpoint
compartido sin match único falla cerrado. Los auxiliares quedan ocultos solo si
coinciden el rol estable `LOCUS_INTERACTION_POINT` y ownership estructural
exclusivo, y esa presentación se restaura tras adjuntar identidad al reabrir.

El cierre técnico añade el gate que faltaba para dominio periódico: un mismo
punto de interacción cruza el seam de un SplineV2 cerrado en ambos sentidos,
mantiene ID/source/branch/component, no duplica candidato y alcanza la misma
address final por ruta directa o incremental. La address versionada conserva
los bits canónicos exactos; el numérico oculto solo reconstruye exactamente
`canonical + lift * period`, evitando una falsa pérdida por round-trip modular
floating. El control periódico evaluator-only sigue `UNRESOLVED` y no muta el
punto, por lo que no se introduce una heurística de proximidad.

Una similitud R5 invertible puede mapear el request hacia la fuente como
aceleración, pero el punto final pertenece al locus transformado. En
`COLLAPSED_IMAGE`, un point query nuevo es normalmente degenerado/no único;
un punto existente conserva su address exacta, colapsa geométricamente y se
recupera al volver `k != 0`.

La [ADR 0019 Accepted](../adr/0019-semantic-locus-point-interaction-support.md),
la [spec normativa](../../geocedg/specs/locus/locus-v2-point-interaction.md), la
[arquitectura aprobada](../architecture/g9u0_r6_semantic_locus_point_interaction_support.md),
la [matriz aprobada de 72 escenarios](../validation/g9u0_r6_semantic_locus_point_interaction_validation_matrix.md)
y el [prompt canónico](../../.github/prompts/tasks/g9u0-r6-semantic-locus-point-interaction-support.prompt.md)
gobiernan el cierre. Los 72 escenarios y 55 métodos focales incluyen las
negativas de cobertura, atomicidad, ownership, lineage y composición. La
aprobación autoral es independiente de la evidencia automatizada y no es
self-approval.

El diseño G9U1 de 17 rutas se preserva en la rama independiente
`feature/g9u1-construction-workspace-planning`, checkpoint
`857de6628489bda0b65a5ba5145e62ca0795fc32`. Su prompt tiene hash canonical-LF
`2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322`.
R6 no lo mergea ni lo ejecuta. Con R6 cerrado, otra tarea debe reconciliar sus
hipótesis con el API publicado y solicitar revisión y autorización propias para
G9U1.

R6 no tiene consumidor Desktop productivo; por decisión autoral su aceptación
diagnóstica es el test-host/API. El smoke GUI queda reservado a G9U1 y deberá
recorrer: Point tool -> click LocusV2/SplineV2 -> create -> drag -> seam crossing
-> ambiguity chooser -> transformed source -> `k=0` -> save/reopen.

El riesgo `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` permanece
`OPEN / TRACKED`: persistencia ordinaria de puntos R6 no equivale al round trip
nativo pendiente del estado de cuarentena del ledger de intersección.

### G9U1 - Workspace CeDG Construction

**Estado del diseño:** `PASS — AUTHOR APPROVED`; checkpoint post-R6 inmutable;
reconciliación post-R1 sin novedad material bajo autorización explícita actual.

**Estado de implementación:** `IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`

Revisión autoral 1: **COMPLETED WITH FINDINGS — NOT PASS**. El checkpoint
`geocedg-g9u1-author-review-checkpoint-1` conserva `b492194082f1adc9f981d85d92a58ef57490196f`.
La estabilización autorizada trabaja en `codex/g9u1-author-review-stabilization-1`,
sin promoción a main ni tag PASS. Menús normales, barra curada, herramientas
propias persistentes y correcciones del productor documental/presentación métrica
se describen en [la revisión](../validation/g9u1_author_manual_review_round1.md).
La salida prevista es **FRONTEND REVIEW CANDIDATE — PENDING AUTHOR RE-SMOKE**;
ninguna ejecución técnica sustituye ese re-smoke.

Revisión autoral 2: el candidato sucesor parte del commit publicado e inmutable
`fa6339204b87385af79331e434778ca16cd8dcf0`. El checklist completo
`docs/validation/g9u1_author_resmoke_checklist.md` es input autoral y no se
reescribe como evidencia del agente; su contenido quedó fijado en el commit de
procedencia de un solo path `01c0bec77a30b43b7ebcf75acacdd098840fa2fe`.
La disposición acotada vive en
`docs/validation/g9u1_author_review_round2.md`: persistencia documental nativa
de invocaciones de user tools, helpers semánticos no visibles por defecto,
edición algebraica ordinaria, enmienda de siete menús, ayuda específica,
pin/group/order de user tools, versión/About central y discoverability de la
dirección semántica. `U1-RV06` conserva exactamente la afirmación histórica de
seis menús como `SUPERSEDED_BY_AUTHOR_ROUND2_AMENDMENT`; `U1-R2-01`–`U1-R2-10`
representan la autoridad vigente. Este sucesor sigue siendo
**IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**, sin promoción a `main`,
sin tag `geocedg-g9u1-pass` y con re-smoke autoral pendiente.

La ejecución `final-composed` de esta segunda estabilización completó 1.480
tests y dejó verdes los resúmenes canónicos de fase, pero el resultado compuesto
final se conserva como **FAILED**: el check heredado de whitespace del baseline
interpretó como whitespace inválido los 28 hard breaks CommonMark de dos espacios
del checklist. El input autoral byte-exacto no cambia en su commit de procedencia
`01c0bec77a30b43b7ebcf75acacdd098840fa2fe` (SHA-256 canonical-LF
`b87a74b6a1e421e6909c6949a442bd3e935920b57a60f9a571a7ec34f6b89f02`, blob
`b4a2cbb5cca0176be43e1d0c5dad4705683a31ea`). La representación viva sustituye
de forma mecánica y reversible solo esos 28 hard breaks por `<br>` (SHA-256
canonical-LF `ba036c052dfc8e03837c1bae2672623b3e0a813b529f1a87c4ecf6647f0ec26b`, blob
`b253da52983049938dfdb74571b89bc76e112ee4`), sin cambiar afirmaciones ni estados
autorales. Los hashes canónicos del cohort round 2 quedan congelados en la
autoridad versionada. Las ejecuciones técnicas aplicables se conservan como
receipts externos y se reportan en el handoff final; no sustituyen el re-smoke
autoral ni declaran PASS.

Revisión autoral 3: la rama sucesora
`codex/g9u1-author-review-stabilization-3` parte byte-exactamente del candidato
Round 2 publicado. Conserva la evidencia fallida y el checklist autoral. La
corrección acotada identifica el rechazo real de `Revision3.cedg` como una
inicialización de la Construction/metric owner de GeoCeDG en el launcher thread
antes de los gestos Álgebra del Swing EDT. El overload de arranque exclusivo de
GeoCeDG crea e inicializa frame/aplicación sincrónicamente en el EDT, después de
preparar el splash; Classic, Dilate, G9A, `GeoNumeric` y el confinamiento kernel
no cambian. El fixture autoral y el fallback limpio cubren ROW, DOUBLE_CLICK, F2
y FREE_INPUT. Los recursos `helixTopBar.png`/`helixSnapshot.png` se promueven
con provenance y derivados deterministas para frame/package y splash,
respectivamente; el gate de redistribución pública permanece bloqueado.

El catálogo mantiene 11 familias, 18 clusters y 110 acciones. Una nueva
proyección declarativa dentro del mismo perfil —28 grupos de presentación, 12
grupos primarios de toolbar y entries de menú tipadas— organiza
**File, Edit, View, Construction, Options, Automation, Help** sin duplicar
autoridad. File/Edit quedan planos con separadores; Construction contiene
Rectas y vectores, Polígonos, Construcciones derivadas, Círculos y cónicas,
Curvas semánticas y Anotaciones y medios; View/Options reutilizan estado host.
La navegación es view-specific y las preferencias globales no seleccionan un
objeto implícito.

La biblioteca de tools usa store v3: digest crudo del `.ggt`, digest normalizado
por definición (solo `showInToolBar` se neutraliza) y, opcionalmente, PNG acotado
con derivado 64×64 app-only. Una macro embebida equivalente sigue siendo
autoridad de reconstrucción documental mientras la entrada instalada conserva
la presentación; diferencia o conjunto parcial falla cerrado. No se implementa
otro motor Macro ni se hace depender `.cedg` de preferencias externas.

Esta revisión permanece **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
Sus escenarios, hashes y receipts exactos se congelarán únicamente sobre el
cohort técnico final. Hasta entonces no hay PASS, promoción a `main`, tag
`geocedg-g9u1-pass` ni autoaprobación.

```text
implementationStarted = true
implementationAuthorized = true
selfApproved = false
authorApprovedDesign = true
passClaimedImplementation = false
```

**Entradas obligatorias satisfechas:** G9U0-R2 implementation, G9U0-R3,
G9U0-R4, G9U0-R5, G9S1 y G9U0-R6 cerraron `PASS — AUTHOR APPROVED`. Ninguno de
esos cierres autoriza U1 automáticamente. El checkpoint pre-R6 permanece
inmutable y esta rama sucesora reconcilia su contenido contra las APIs R6
publicadas. El diseño sucesor está aprobado por el autor; la implementación
queda autorizada condicionalmente por la instrucción actual tras esta auditoría.

La revisión post-G9S1 descubrió una dependencia kernel que no cabía en un gate
frontend: la interacción gráfica ordinaria `Point` sobre un Locus V2 necesitaba
una resolución inversa determinista desde un objetivo geométrico hasta una
dirección semántica exacta. G9U0-R6 ya cerró esa dependencia como `PASS — AUTHOR
APPROVED`. G9U1 consumirá `LocusPointInteractionQuery2D`, el resultado tipado de
`LocusPointInteractionResolver2D.resolve(...)` y las operaciones públicas de
creación/movimiento interaction-owned; no reimplementará el resolver ni inferirá
identidad desde render, píxeles, proximidad u orden. La forma exacta
`Point(L, branch, u)` sigue siendo la autoridad de comando/GGBScript y no se
convierte en una API de ratón sintética.

Los prompts históricos G9P, post-R3 y post-R5 permanecen inmutables. El prompt
post-G9S1 de entrada tenía canonical-LF SHA-256
`6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4`.
El checkpoint protegido conserva esas 17 rutas en
`feature/g9u1-construction-workspace-planning` con prompt canonical-LF SHA-256
`2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322`.
La autoridad canónica sucesora post-R6 vive en
`.github/prompts/tasks/g9u1-construction-workspace-after-g9s1-r1.prompt.md`;
su hash canonical-LF se congela en la evidencia de esta reconciliación. El
diseño base está aprobado; el sucesor post-R1 está autorizado para producir
un candidato sin autoaprobación. El prompt post-R6 conserva su hash exacto
`561546019efc1e1d5e4367ddde73e9a2b0a0d767343eb9348b46d9e9c06f12df`.

El perfil vivo `apps/geocedg/application-profile.yml` usa schema-v2 en el
candidato implementado; `application-profile-v2.candidate.*` preserva el diseño
histórico. Un catálogo declarativo único alimenta menús, toolbar y help. Las once
familias profesionales aceptadas siguen siendo la taxonomía de alto nivel; el
manifiesto las desarrolla en dieciocho clusters operativos sin crear otra
autoridad. La matriz de completitud clasifica cada acción como `MUST HAVE`,
`SHOULD HAVE`, `DEFERRED` u `OUT OF SCOPE / REQUIRES NEW KERNEL PHASE`; conserva
110 acciones estables. La reconciliación post-R1 preservó 118 escenarios post-R6
y añadió 20 `U1-PAIR` (138); la primera estabilización añadió 15 `U1-RV` (153)
y la segunda añade diez `U1-R2` (163), sin eliminar esa historia. El perímetro
round 2 contiene 204 métodos focales en 21 clases (190 Desktop y 14 shared), con
inventario exacto en la evidencia G9U1 vigente. La enmienda de presentación
proyecta los mismos 110 IDs en siete menús normales; no crea acciones nuevas.
Se retienen `U1-PNT-01`–`U1-PNT-20` para el flujo Point real.

La reconciliación asigna cada defecto observado a su seam normal:

- hit testing de Locus V2 contra el stroke visible, nunca contra el interior
  cerrado/relleno del path y sin tocar la semántica del locus;
- preview Algebra de `SplineV2(...)` en el lifecycle normal de preview de
  comandos, sin construir durante tecleo incompleto;
- inspección de Definition como vista de solo lectura de la definición real;
- indicador radio/check correcto para Description Mode usando el estado del
  árbol Algebra existente, sin preferencia paralela;
- `ZoomWindow` obligatorio mediante el rectángulo de vista existente, accesible
  por menú, toolbar y teclado; pan, zoom in/out, vista estándar y mostrar todos
  son acompañantes acotados, mientras `ZoomPrevious`, `FitSelection`,
  `FitLayer`, vistas nombradas y escala/navegación avanzada siguen en G12;
- entrada libre compatible como `k=0.25` exclusivamente por la transacción
  redefine atómica G9A: el label localiza el objetivo explícito en el contexto
  de comando, pero nunca es identidad durable;
- `Continuity = OFF` mediante el ajuste host existente y un clamp del lifecycle
  GeoCeDG que prevalece sobre preferencias, restart, workspace y carga
  `.cedg`/`.ggb`; no existe segundo ajuste y Classic sigue configurable;
- help/autocomplete/tooltip/status y GGBScript usan los mismos comandos y
  action IDs, con inglés y español completos y fallback explícito a inglés;
- accesibilidad por teclado, nombres accesibles, escala/DPI, errores localizados
  y límites de rendimiento forman parte del mismo gate.

Las acciones Locus V2 y Spline V2 consumen las autoridades G9U0–R5/G9S1.
Transformadas, `COLLAPSED_IMAGE`, métricas total/parcial y rich-result se exponen
sin comandos paralelos. Spline V2 × Spline V2 consume ahora los tokens R1 de roots localmente
certificadas y slots singleton-germ. Varias raíces distintas/opposite-germ pueden
ser elegibles incluso en un span pair; colisiones same-germ, tangencias/contactos
múltiples, monodromía y evidencia insuficiente permanecen rich-only. La
simetría gobierna pair/slot/evidence, no igualdad de tokens opacos entre owners
independientes. No se amplía el caso Locus genérico.

El resultado rico determinista sigue siendo autoridad de intersección. Markers
transitorios solo representan tokens actuales del resultado activo; no son
GeoElement/XML/DAG/Protocol/undo. Crear uno, varios seleccionados o todos los
puntos elegibles es explícito y deshacible; la multi-creación es una transacción
compuesta. El inspector queda abierto para materializaciones repetidas e indica
cuáles ya existen sin usar tokens opacos como labels de layout. La reactivación
de un punto existente pertenece al recompute kernel; crear puntos nunca vistos
por política automática es una acción frontend explícita, visible y opt-in, y
no ocurre durante recompute.

El riesgo `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` sigue `OPEN / TRACKED`.
La futura validación U1 debe intentar cerrarlo con un round trip `.cedg` nativo
real de la cuarentena periódica; una referencia de planificación, XML fabricado
o export/import aislado del ledger no basta. Si no se cierra, requiere
disposición autoral explícita antes del cierre global G9. El autor permite
continuar el candidato G9U1 si el experimento es inconcluso sin corrupción ni
violación real del contrato kernel/persistencia; una violación real exige STOP.

La identidad visual es solo frontend y permanece revisable por el autor. Los
roles lógicos futuros `geocedg.brand.topbar` y `geocedg.brand.startup` conservan
una autoridad de procedencia cada uno; esta planificación no integra, copia ni
fabrica `helixTopBar.png` o `helixSnapshot.png`. GeoCeDG Classic sigue siendo
un proceso/ruta diagnóstica visualmente distinto, con preferencias separadas y
la misma preservación semántica compartida.

### G9B - Proyecciones canónicas de primitivas

**Estado:** `DESIGNED — NOT AUTHORIZED`

Implementará por etapas esquemas tipados para recta, segmento, rayo, vector,
plano, circunferencia, cónica y curva espacial soportada sobre la base del piloto
de punto. Cada familia define datos mínimos por configuración, frames admisibles,
correspondencia, ecuaciones, garantías, degeneraciones y certificado dinámico;
el número de vistas nunca basta por sí solo.
Su única dependencia semántica de fase es el cierre autor-aprobado de G9A3 y el
contrato de primitivas. G9U1 es un cliente y no constituye gate de entrada.

### G9C - Objetos compuestos y frontera proyectiva

**Estado:** `DESIGNED — NOT AUTHORIZED`

Cubrirá solo colecciones de puntos, curvas/arcos espaciales, aristas, bucles
orientados, caras, superficies soporte/regladas/desarrollables, objetos
poliédricos, incidencia, adyacencia, orientación, ownership de frontera y
componentes conexas necesarios para CeDG. No será un feature tree ni una copia
genérica de B-Rep CAD.

### Puerta global G9 y G9U2

El cierre global exige round-trip proyección–objeto–proyección, serialización y
migración estables, casos canónicos/dinámicos/degenerados, los tracks B/C y
U0/U0-R1/X1/U0-R2/U1 aprobados, evidencia operacional G9O1, counters y composed verify,
seguido de aprobación autoral explícita. Solo entonces G9U2 podrá
implementar el workspace `CeDG Dihedral Procedures` y sus procedimientos
constructivos consumiendo sistemas/mapas/charnelas tipados; hasta ese momento
está `BLOCKED ON THE APPROVED G9 GATE`.
El cierre es una acción de revisión autor/verificador sin semántica nueva, no una
fase productiva ni un prompt de implementación independiente.

## G10 - DSL, estudios geométrico-funcionales, optimización y workbench

**Estado de planificación:** G10P `PASS — AUTHOR APPROVED —
PLANNING/CHARACTERIZATION ONLY`. G10A, G10B, G10C1, G10C2, G10U y G10R
permanecen `PROPOSED — NOT AUTHORIZED — NOT STARTED`. No existe todavía una
capacidad de producto G10.

La [caracterización G10P](../validation/g10p-study-optimization/g10p_study_optimization_roadmap_analysis.md)
se apoya en el cierre publicado G9A1 y en modelos canónicos suministrados,
propiedad del autor y autorizados para su inclusión y publicación. Se cerró
durante G9 porque solo cambia planificación. Cualquier
implementación G10A o posterior requiere antes `G9 PASS — AUTHOR APPROVED`,
G10P aprobado, especificaciones/ADR propios y un prompt canónico invocado de
forma separada. G10P deberá revalidar sus seams contra el G9 finalmente cerrado.

El modelo conceptual distingue variables de diseño `d`, variables de operación
`u` y escenarios `e`. La construcción autoritativa evalúa `G(d,u,e)` mediante
el DAG normal; el modelo funcional `F` añade supuestos declarados; y el solver
consume objetivos/restricciones sin reconstruir geometría. Validez geométrica,
factibilidad funcional, terminación del solver y garantía de optimalidad son
ejes independientes. Un candidato válido o una factibilidad muestreada no es
un óptimo global establecido.

### G10P - Caracterización de arquitectura de estudios y optimización

Fija taxonomía, corpus canónico, variables/dominios/unidades, supuestos,
objetivos, restricciones puntuales y de trayectoria, estados de resultado,
separación kernel/exterior, alternativas de evaluación aislada, persistencia,
benchmarks y artefactos normativos futuros. Es documentación no productiva y no
autoriza ninguna fase.

El cierre autoral aprueba como dirección de planificación el corpus y la
descomposición P/A/B/C1/C2/U/R; los ejes independientes de currentness fuente,
validez geométrica, factibilidad funcional, cobertura de dominio, terminación,
garantía de optimalidad y evidencia numérica; y el requisito de un token fuente
coherente. Los identificadores y la implementación de ese token quedan diferidos
a especificaciones futuras y a G10A tras el cierre global G9.

### G10A - Fundamento de evaluación determinista de estudios

Después de la puerta global G9, podrá implementar el mínimo servicio compartido
para evaluar candidatos en un contexto aislado y sin render, ligado a una
revisión coherente, con política explícita de scripts/eventos, límites,
cancelación, descarte/restauración y lectura tipada de validez/resultados. Una
aplicación seleccionada será una transacción explícita, multi-parámetro,
atómica, undoable y revalidada contra la revisión fuente. No habrá solver ni
DSL dentro del kernel.

### G10B - Modelo de estudio y DSL solver-neutral

Declarará referencias durables, variables continuas/discretas/categóricas,
intervalos de operación, escenarios, unidades, supuestos, cantidades
funcionales, objetivos y restricciones. La persistencia inicial será un
artefacto externo versionado que referencia IDs durables; introducir objetos de
estudio en XML `.ggb` exigiría evidencia y otra decisión explícita.

### G10C1 - Orquestación y solver mínimo robusto

Cubrirá análisis directo, sweeps, diseño inverso escalar, optimización escalar
acotada, enumeración discreta finita, combinación discreta-finita con un
continuo escalar, búsqueda del peor caso sobre un dominio operativo y
clasificación conservadora de garantía. Usará adapters a bibliotecas maduras y
mantendrá el contrato compartido independiente de cualquier solver.

### G10C2 - Optimización avanzada

Quedan detrás de otra puerta: optimización no lineal multidimensional,
multiobjetivo/Pareto, mixed-integer general, incertidumbre, diseño robusto,
sensibilidad, muestreo adaptativo y métodos certificados/globales cuando su
evidencia lo justifique. G10C2 no es requisito para un primer G10 útil. No se
añadirán optimizadores genéricos ni diferenciación automática al kernel por
conveniencia.

### G10U - Workbench de estudios

Será un cliente para configurar, ejecutar/cancelar, inspeccionar candidatos
inválidos y garantías, representar respuestas, comparar alternativas y pedir
la aplicación explícita de un resultado. No mantendrá geometría ni un grafo de
dependencias propio y el runtime no dependerá de un workspace concreto.

### G10R - Cierre de validación y rendimiento del estudio

Validará corpus, determinismo, equivalencia de reruns, ausencia de mutación del
documento vivo, coste/recomputaciones por evaluación, cero render, memoria,
cancelación, caché acotada e invalidación por revisión. Es distinto de G16, que
optimiza transversalmente la plataforma completa.

```text
G9A1 PASS --> G10P (planificación no productiva)

tracks G9 restantes --> G9 GLOBAL PASS
G9 GLOBAL PASS + G10P aprobado --> G10A --> G10B --> G10C1 --> G10U --> G10R
                                                    +--> G10C2 (puerta posterior separada)
```

El cierre mínimo G10 v1 requiere P/A/B/C1/U/R aprobados; G10C2 puede permanecer
posterior. Los estudios espaciales consumen certificados G9 para los tipos que
usen; la aplicación atómica consume el ciclo de vida G9A3; y los estudios
públicos sobre Locus V2 consumen G9U0. Ningún cliente GUI constituye autoridad
semántica ni gate inverso para el runtime.

## G11 - Capas y estados de vista

**Estado:** `PENDING`

**Trabajo**

- jerarquía, roles, bloqueo y filtros;
- visibilidad por vista;
- estados guardables;
- integración con DXF, PDF y modelos heredados.

**Salida**

- organización técnica comparable a herramientas de dibujo profesional;
- compatibilidad con capas numéricas de GeoGebra.

## G12 - Navegación, zoom y escalas extendidas

**Estado:** `PENDING`

**Trabajo**

- zoom centrado en cursor y zoom de ventana por tecla;
- vistas nombradas y zoom previo;
- separación modelo/vista/dibujo/impresión;
- pruebas con rangos de escala extremos.

**Salida**

- navegación precisa y reproducible;
- ninguna métrica depende de pantalla, zoom o DPI.

## G13 - Visibilidad geométrica en proyecciones

**Estado:** `PENDING`

**Trabajo**

- partición visible/oculta/silueta;
- poliedros y primitivas primero;
- transición dinámica y estilos;
- ampliación posterior a curvas y superficies generales.

**Salida**

- representación de visibilidad derivada de la geometría espacial y de la dirección de proyección;
- no se confunde con ocultación por capa.

## G14 - Puente a la vista 3D

**Estado:** `PENDING`

**Trabajo**

- adaptador desde `SpatialObject3D`;
- cobertura incremental de tipos;
- reproyección diagnóstica;
- política de solo lectura inicial.

**Salida**

- el sistema CeDG se visualiza en la vista 3D existente sin crear una segunda verdad geométrica.

## G15 - Hojas, PDF y formatos gráficos

**Estado:** `PENDING`

**Trabajo**

- `DrawingSheet`, marcos y cajetines;
- escalas físicas y viewports;
- PDF vectorial con tamaño exacto;
- SVG y PNG;
- pruebas dimensionales del fichero generado.

**Salida**

- publicación reproducible que respeta marco, tamaño físico, escala y capas.

## G16 - Rendimiento y escalabilidad

**Estado:** `PENDING`

**Trabajo**

- perfiles reproducibles sobre benchmarks acumulados;
- selección de cuellos de botella;
- invalidación incremental, cachés, índices y reducción de asignaciones donde proceda;
- presupuestos de rendimiento convertidos en gates.

**Salida**

- mejoras cuantificadas frente a G0;
- ausencia de regresiones geométricas;
- decisiones Java/C++ o paralelismo sustentadas por evidencia, no por intuición.

G16 conserva el rendimiento global y no absorbe el contrato G10R de evaluación
de candidatos ni la optimización matemática de diseños.

---

## Apéndice A. Historical bootstrap record (G0)

> **Registro histórico, superseded y no operativo.** Esta sección conserva las
> instrucciones que originaron G0 para explicar la evolución del proyecto; no
> debe ejecutarse como onboarding actual. La línea base finalmente adoptada es
> GeoGebra 5.4.928.0, commit
> `9b93256b7df401ff056c37b502d82df4d72b1522`, y las rutas/comandos históricos
> que siguen en este apéndice pueden estar obsoletos. Para clonar, preparar y
> ejecutar el repositorio se deben usar [README.md](../../README.md), la
> [guía de usuario](../user/geocedg_user_guide.md) y
> `tools/bootstrap/bootstrap-windows.ps1`.

### 16.1 Repositorio remoto

1. Crear un **fork de `geogebra/geogebra`** en GitHub.
2. Renombrar el fork a `GeoCeDG`.
3. No crear inicialmente un repositorio vacío con una copia de archivos, porque se perdería trazabilidad de commits y facilitaría errores de licencia.
4. Mantener el fork privado durante la fase de inventario de recursos si se desea, pero conservar siempre los avisos de licencia.

### 16.2 Carpeta local recomendada

En coherencia con el resto de proyectos:

```text
C:\DESARROLLOYDATOS\Areas\ProyectosNoFinanciados\GeoCeDG
```

### 16.3 Clonado y remotos

```powershell
Set-Location C:\DESARROLLOYDATOS\Areas\ProyectosNoFinanciados

git clone https://github.com/<CUENTA>/GeoCeDG.git
Set-Location .\GeoCeDG

git remote -v
git remote add upstream https://github.com/geogebra/geogebra.git
git fetch --all --tags --prune
git remote -v
```

Si el fork ya incluye `upstream`, no repetir el alta.

### 16.4 Congelar línea base

```powershell
New-Item -ItemType Directory -Force .\docs\upstream | Out-Null

$baseline = git rev-parse HEAD
$baseline | Set-Content -Encoding utf8 .\docs\upstream\BASELINE_COMMIT.txt

git tag -a geogebra-baseline-2026-08-09 `
  -m "GeoCeDG baseline from current GeoGebra source: $baseline"

git push origin geogebra-baseline-2026-08-09
```

El agente completará `UPSTREAM.md` con fecha, rama, SHA, toolchain y estado del build.

### 16.5 Comprobar toolchain y Gradle

```powershell
java --version
.\gradlew.bat --version
.\gradlew.bat projects
.\gradlew.bat :desktop:tasks --all
```

El commit actual inspeccionado solicita Java 25 para el `run` desktop. Si Gradle no aprovisiona automáticamente el toolchain, instalar un JDK 25 compatible y repetir.

### 16.6 Primera compilación

```powershell
.\gradlew.bat :desktop:run
```

Después de comprobar visualmente el arranque, ejecutar las pruebas que el mapa de tareas confirme. Como primera hipótesis:

```powershell
.\gradlew.bat :desktop:test
```

No añadir GeoCeDG hasta que la línea base arranque y las pruebas seleccionadas pasen.

### 16.7 Añadir los documentos iniciales

Copiar:

- `AGENTS.md` a la raíz;
- `FIRST_AGENT_TASK.md` a la raíz;
- este roadmap a `docs/roadmap/geocedg_roadmap.md`;
- `SPATIAL_PROJECTION_SEMANTICS_PROPOSAL.md` a `docs/architecture/proposed_spatial_projection_semantics.md`.

El último documento es una propuesta no normativa: G9 deberá convertirla, tras inspeccionar el código real, en especificaciones y ADR aprobadas.

Después:

```powershell
git switch -c bootstrap/geocedg-baseline
git add AGENTS.md FIRST_AGENT_TASK.md `
  docs/roadmap/geocedg_roadmap.md `
  docs/architecture/proposed_spatial_projection_semantics.md
git commit -m "docs: define GeoCeDG bootstrap architecture and agent policy"
git push -u origin bootstrap/geocedg-baseline
```

Abrir una pull request hacia `integration` o, si aún no existe, hacia `main` exclusivamente para estos documentos.

### 16.8 No hacer todavía

- no renombrar paquetes Java;
- no borrar módulos web;
- no sustituir masivamente la marca;
- no importar herramientas CeDG;
- no modificar `Locus`;
- no implementar todavía la asociación 3D–proyecciones;
- no crear objetos espaciales a partir de etiquetas o coincidencias visuales;
- no crear el instalador;
- no añadir DXF;
- no aplicar formateo global;
- no sincronizar a mano archivos sueltos desde otra copia de GeoGebra.

La primera tarea del agente es solo caracterización y scaffold.

---

## 17. Estrategia de ramas vigente

```text
main                         fases cerradas y promovidas
feature/gN-<capacidad>       implementación aislada de una fase
planning/gN-<capacidad>      planificación previa explícita
research/<capacidad>         experimentos no promovidos
sync/geogebra-YYYYMMDD       sincronización upstream aislada
```

La sincronización upstream se hará en rama específica desde el estado de
integración aprobado que corresponda y terminará en revisión antes de alcanzar
`main`. No se prescribe aquí una rama `integration` permanente:

```powershell
git fetch upstream --tags --prune
git switch -c sync/geogebra-2026MMDD main
git merge --no-ff upstream/main
.\tools\agent\verify.ps1
```

Resolver, validar y abrir PR. No mezclar una sincronización upstream con una función nueva.

---

## Apéndice B. Historical G0 decision package

> Esta lista fue la entrada de G0 y está completada. Sus resultados se conservan
> en el [informe de baseline](../validation/baseline_report.md) y documentos
> enlazados; no constituye trabajo pendiente.

1. SHA y build reproducible.
2. Mapa Gradle real.
3. Punto exacto de creación del launcher GeoCeDG.
4. Mecanismo exacto de selección de `AppConfig`.
5. Origen de la perspectiva y toolbar.
6. Inventario de recursos no EUPL.
7. Clases exactas implicadas en Locus e Intersect.
8. Contrato de serialización actual de Locus.
9. Clases y módulos de objetos 3D, sistemas de coordenadas y vistas 2D/3D.
10. Mecanismos existentes de proyección, reproyección y enlace entre `GeoElement` 2D/3D.
11. Contratos actuales de capa, estilo, visibilidad, impresión y exportación.
12. Puntos de instrumentación y benchmark del kernel y render.
13. Estrategia de módulo para `jpackage`.
14. Primer `verify-baseline.ps1`.
15. Estructura de directorios mínima, sin duplicar upstream.
16. Riesgos y decisiones pendientes.

Ese informe permitió ajustar el roadmap al commit real antes de empezar la implementación.

---

## 19. Riesgos principales

### Licencias de activos

**Riesgo:** creer que todo el producto está cubierto por EUPL.
**Control:** matriz por componente y recursos propios.

### Divergencia upstream

**Riesgo:** cambios masivos dificultan fusionar GeoGebra.
**Control:** perfil separado, parches mínimos y sincronización aislada.

### `Locus` convertido en polilínea refinada

**Riesgo:** mejorar solo la densidad de muestras sin corregir semántica.
**Control:** separar definición, evaluator, métrica y render.

### Exactitud declarada indebidamente

**Riesgo:** llamar exacta a una longitud numérica.
**Control:** clasificación y estimación de error.

### Herramientas heredadas permanentes

**Riesgo:** convertir scripts de investigación en API.
**Control:** estados de madurez y laboratorio.

### Lógica geométrica en DXF o workbench

**Riesgo:** duplicar el kernel.
**Control:** DTO de solo lectura y servicios clientes.

### Asociación espacial inferida de forma frágil

**Riesgo:** vincular objetos por etiquetas, orden de creación o proximidad visual.
**Control:** identificadores estables, `ProjectionBinding` tipado y serialización versionada.

### Proyecciones canónicas declaradas sin suficiencia

**Riesgo:** aceptar dos vistas aunque una o ambas pierdan información esencial.
**Control:** esquemas por tipo, predicados de no degeneración y certificado dinámico.

### B-Rep CAD introducida como autoridad

**Riesgo:** perder la construcción CeDG al copiar una representación sólida opaca.
**Control:** frontera proyectiva ligada a primitivas, superficies, caras, proyecciones y dependencias.

### Confusión entre capa, visibilidad y existencia

**Riesgo:** ocultar un objeto y tratarlo como geométricamente inexistente, o usar capas para resolver ocultaciones.
**Control:** estados separados y servicios distintos.

### Optimización de rendimiento sin línea base

**Riesgo:** complejidad y errores sin mejora demostrable.
**Control:** benchmarks desde G0 y perfiles antes de cada cambio de rendimiento.

### Estudio externo que duplica autoridad geométrica

**Riesgo:** reconstruir geometría, validez o dependencias en Python, DSL o
workbench y divergir del `Construction`.
**Control:** evaluación compartida sobre el DAG normal, referencias durables y
clientes sin segundo grafo.

### Garantía de estudio exagerada

**Riesgo:** presentar un candidato factible, una muestra o convergencia local
como prueba de factibilidad continua u óptimo global.
**Control:** ejes separados de validez, factibilidad, terminación, cobertura y
garantía; `GLOBAL_OPTIMUM_ESTABLISHED` solo con evidencia independiente.

### Prompt files divergentes

**Riesgo:** varias copias de las mismas reglas.
**Control:** `.github/prompts` canónico y `tools/agent` como autoridad ejecutable.

---

## 20. Resultados esperados por hitos

### G2, cerrado como `PASS`

GeoCeDG alcanzó en G2:

- arrancar como producto separado;
- mostrar su interfaz CeDG;
- conservar acceso de diagnóstico a Classic;
- usar recursos identificados y controlados;
- cargar funciones experimentales sin hacerlas permanentes;
- compilar y probarse de forma reproducible;
- mantener el kernel geométrico upstream sin cambios semánticos;
- una base preparada para abordar packaging, DXF y Locus V2; packaging y DXF se
  materializaron posteriormente en G4 y G5.

Ese estado constituyó la primera plataforma propia CeDG, todavía sin semántica
espacial nativa. G3–G5 ampliaron la plataforma sin iniciar esa semántica.

### Objetivo pendiente al cerrar G9

GeoCeDG deberá además:

- representar un objeto 3D mediante identidad semántica estable;
- mantener vinculadas sus proyecciones definitorias, derivadas y auxiliares;
- certificar si las proyecciones canónicas son suficientes y no degeneradas;
- reconstruir y reproyectar primitivas;
- representar objetos compuestos mediante primitivas, superficies, caras y relaciones orientadas;
- serializar esas relaciones y mantenerlas en el grafo de dependencias;
- diagnosticar ambigüedad, contradicción y degeneración.

Este es el hito que habilita un DSL verdaderamente 3D.

### Objetivo pendiente al cerrar G16

La plataforma deberá añadir:

- DSL 3D y workbench clientes del kernel;
- estudios geométrico-funcionales con análisis directo, diseño inverso,
  optimización acotada y garantías conservadoras;
- capas técnicas y estados de vista;
- navegación y escalas extendidas;
- visibilidad geométrica en proyecciones;
- representación 3D derivada;
- publicación PDF/SVG con tamaño físico y escala controlados;
- rendimiento medido y mejorado sobre benchmarks canónicos.
