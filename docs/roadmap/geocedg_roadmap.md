# GeoCeDG — Living Technical Roadmap

| Campo | Valor |
|---|---|
| Carácter | Roadmap vivo y normativo de fases; no sustituye las especificaciones ni los ADR aceptados |
| Versión documental | 3.12 |
| Fecha de revisión | 13 de agosto de 2026 |
| Baseline GeoGebra | 5.4.928.0, commit `9b93256b7df401ff056c37b502d82df4d72b1522`, tag `geogebra-baseline-5.4.928.0` |
| Estado actual | G6 `PASS`; G6R `PASS`; G7A-R1, G7A y G7B `PASS — AUTHOR APPROVED`; G7 `PASS`; V2 sigue experimental, interno y desactivado por defecto |
| Última fase cerrada | G7 — Native Locus V2 metrics |
| Siguiente puerta | G8 permanece `NOT STARTED`; requiere autorización separada |
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
- paquete de planificación G8 propuesto y listo para revisión de autor, sin
  iniciar caracterización ni implementación;
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
│ Servicios de documento e interoperabilidad                  │
│ DXF, PDF/SVG, hojas, informes, empaquetado, CLI             │
├─────────────────────────────────────────────────────────────┤
│ Capa Python posterior a G9                                  │
│ DSL 3D CeDG, orquestación, estudios, validación, notebooks  │
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

La semántica espacial no debe residir en Python ni en el workbench. El DSL consumirá y generará objetos definidos por el kernel; no será la autoridad que reconstruye informalmente un objeto 3D a partir de nombres o coincidencias gráficas.

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
| DSL y estudios | Python, después de G9 |
| Medición e intersección de Locus | kernel Java |
| Visibilidad geométrica en proyecciones | servicio geométrico compartido Java |
| Capas jerárquicas, bloqueo y estados de vista | modelo de documento/aplicación compartido |
| Zoom, navegación y escalas de pantalla | capa de vista/desktop; nunca autoridad métrica |
| Conversión a vista 3D | adaptador compartido desde la semántica espacial al kernel/vista 3D existente |
| PDF/SVG y hojas de dibujo | servicio de documento/exportación |
| Benchmarks y validación | tests + modelos + herramientas de rendimiento |
| Workbench futuro | cliente, sin lógica geométrica propia |

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
- `ZoomWindow`, `ZoomPrevious`, `FitSelection`, `FitLayer` y vistas nombradas;
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
| G8 | `NOT STARTED — PLANNING READY FOR AUTHOR REVIEW` | [Plan G8 propuesto](g8_locus_v2_intersections_plan.md); spec y ADR siguen propuestos; no hay ejecución ni implementación |
| G9–G16 | `NOT STARTED` | No iniciadas |

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
G8 = NOT STARTED
G9 = NOT STARTED
```

## G8 - Intersecciones 2D

**Estado:** `NOT STARTED — PLANNING READY FOR AUTHOR REVIEW`

El [plan de ejecución propuesto](g8_locus_v2_intersections_plan.md) recomienda
dos puertas, sin iniciarlas:

1. **G8A — caracterización y decisiones de autor:** probes exclusivamente
   test-private, referencias independientes y medición de solver, tangencia,
   tolerancias, resultado rico, identidad/topología y trabajo acotado;
2. **G8B — kernel interno 2D mínimo:** solo después de que G8A sea
   `PASS — AUTHOR APPROVED`, la spec pase a normativa, el ADR se acepte o
   sustituya y exista autorización productiva separada.

No se reserva una G8C. Implícitas, funciones y locus–locus se caracterizarán
como Level C y solo justificarán una fase posterior si la evidencia muestra que
no caben de forma sostenible en el mínimo.

**Mínimo productivo propuesto, pendiente de decisión**

- recta, segmento, semirrecta y circunferencia;
- cónicas no degeneradas solo si G8A demuestra un contrato cerrado;
- aislamiento por componente semántico, refinamiento y verificación residual;
- tangencia de multiplicidad par sin depender solo de cambios de signo;
- resultado rico inmutable con ausencia completa, incertidumbre, overlap,
  garantía y lifecycle separados;
- identidad por parámetro/rama/componente y linaje topológico, nunca por
  proximidad de coordenadas; y
- estado inicialmente local a la consulta; ningún índice métrico G7 sirve como
  autoridad de intersección.

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
la [spec propuesta](../../geocedg/specs/locus/locus-v2-intersections.md), la
[matriz de validación](../validation/g8_locus_v2_intersection_validation_matrix.md),
el [plan de contadores](../validation/g8_locus_v2_intersection_benchmark_plan.md),
la [trazabilidad científica](../validation/g8_locus_v2_intersection_scientific_traceability.md),
el [ADR 0008 Proposed](../adr/0008-locus-v2-intersection-result-and-continuation.md)
y los prompts futuros
[G8A](../../.github/prompts/tasks/g8a-locus-v2-intersection-characterization.prompt.md)
y [G8B](../../.github/prompts/tasks/g8b-locus-v2-intersection-kernel.prompt.md).

Ninguno de estos documentos ejecuta G8. La spec permanece
`PROPOSED — NOT NORMATIVE`, ADR 0008 permanece `Proposed`, G8A/G8B no han
comenzado y toda implementación productiva exige revisión y autorización
explícita del autor.

## G9 - Semántica espacial y proyecciones canónicas

**Estado:** `NOT STARTED`

### G9A - Asociación objeto 3D–proyecciones

**Trabajo**

- `SpatialObject3D`, `ProjectionFrame`, `ProjectionBinding` y registros estables;
- roles definitorio, derivado, auxiliar, análisis y presentación;
- integración con dependencias y serialización;
- migración explícita de la prueba de concepto, sin inferencia por etiquetas.

**Salida**

- un objeto espacial mantiene identidad estable y conoce todas sus proyecciones asociadas;
- cambios dinámicos se propagan de manera trazable;
- ficheros heredados continúan siendo compatibles.

### G9B - Proyecciones canónicas de primitivas

**Trabajo**

- esquemas para punto, recta, plano, circunferencia/curva, esfera, cilindro y cono;
- predicados de suficiencia y no degeneración;
- reconstrucción y reproyección;
- estados `VALID`, `UNDERDETERMINED`, `AMBIGUOUS`, `INCONSISTENT` y `DEGENERATE`.

**Salida**

- GeoCeDG puede definir una primitiva 3D por un conjunto suficiente de proyecciones;
- una recta de punta requiere y utiliza una vista adicional no degenerada;
- el certificado canónico se actualiza dinámicamente.

### G9C - Objetos compuestos, superficies y sólidos proyectivos

**Trabajo**

- agregación jerárquica;
- superficies soporte;
- curvas/aristas espaciales;
- caras y bucles orientados;
- incidencia, adyacencia, cierre y orientación;
- bindings de proyección de componentes y caras.

**Salida**

- objetos no primitivos pueden definirse por primitivas, relaciones y fronteras proyectivas;
- un sólido cerrado dispone de diagnóstico topológico;
- la geometría sigue siendo constructiva y no una copia opaca de B-Rep CAD.

**Puerta global G9**

- round-trip proyección–objeto–proyección validado;
- serialización estable;
- casos canónicos y degenerados superados;
- API todavía experimental detrás de `cedg.spatial.semantics`.

## G10 - DSL 3D CeDG y workbench

**Estado:** `PENDING`

**Trabajo**

- DSL declarativo sobre `SpatialObject3D`;
- declaración de marcos, proyecciones canónicas, primitivas, caras, superficies y procedimientos;
- compilación/orquestación;
- inspección de dependencias y certificados;
- estudios y cliente de escritorio.

**Salida**

- el DSL expresa objetos 3D y genera las proyecciones que maneja GeoCeDG;
- no duplica reconstrucción, validez ni lógica geométrica del kernel.

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

### Optimización sin línea base

**Riesgo:** complejidad y errores sin mejora demostrable.
**Control:** benchmarks desde G0 y perfiles antes de cada cambio de rendimiento.

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
- capas técnicas y estados de vista;
- navegación y escalas extendidas;
- visibilidad geométrica en proyecciones;
- representación 3D derivada;
- publicación PDF/SVG con tamaño físico y escala controlados;
- rendimiento medido y mejorado sobre benchmarks canónicos.
