# GeoCeDG - Planificación inicial del repositorio, arquitectura y desarrollo

**Fecha de referencia:** 9 de agosto de 2026
**Versión del plan:** 2.0
**Revisión principal:** semántica espacial, proyecciones canónicas y fases posteriores de producción
**Producto propuesto:** GeoCeDG
**Base tecnológica:** fork del código fuente actual de GeoGebra
**Primer cliente:** aplicación de escritorio de la familia Classic 5
**Núcleo:** Java compartido de GeoGebra, extendido de forma mínima y estructural

---

## 1. Conclusión ejecutiva

La estrategia recomendable no es convertir inmediatamente toda la distribución de GeoGebra en un producto distinto, ni introducir todas las herramientas CeDG existentes en el núcleo. Conviene construir GeoCeDG en cinco movimientos controlados:

1. **Congelar y caracterizar una línea base reproducible de GeoGebra.**
2. **Crear un perfil de aplicación GeoCeDG**, con frontend, perspectivas, barras de herramientas, recursos y funciones experimentales independientes del perfil Classic.
3. **Separar las extensiones por madurez**: modelos/herramientas heredadas, investigación, funciones experimentales y funciones estables.
4. **Modificar el kernel únicamente cuando cambie la semántica geométrica**, empezando por un rediseño de `Locus` que separe el objeto geométrico de su muestreo gráfico.
5. **Introducir, antes del DSL, una semántica espacial CeDG nativa** que asocie cada objeto 3D con sus proyecciones, verifique qué conjunto de proyecciones lo define completamente y permita componer objetos complejos mediante primitivas, superficies, caras y relaciones constructivas.

La primera versión útil debe ser una aplicación de escritorio basada en el kernel común de GeoGebra, con:

- interfaz GeoCeDG propia;
- carga optativa de las herramientas CeDG ya desarrolladas;
- sistema de prompt files y verificación ejecutable;
- instalador propio generado desde los fuentes;
- modelos de regresión CeDG;
- exportación DXF 2D como servicio externo al kernel;
- `Locus V2` inicialmente detrás de una bandera experimental;
- un modelo semántico `SpatialObject3D`–proyecciones, también inicialmente experimental;
- criterios verificables de suficiencia y degeneración de proyecciones canónicas.

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

Los comandos oficiales documentados para arrancar las aplicaciones incluyen:

```powershell
.\gradlew.bat :desktop:run
.\gradlew.bat :web:run
```

El `run` actual del módulo desktop solicita un toolchain Java 25. No conviene seguir ciegamente documentación antigua que mencione Java 8; la versión efectiva debe obtenerse siempre del commit congelado.

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


## 6. Estructura de carpetas recomendada

Se debe conservar la estructura upstream y añadir una envolvente GeoCeDG clara:

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

El código Java real del producto debe integrarse en módulos coherentes con el build de GeoGebra. `apps/geocedg-desktop/` contendrá el contrato y la documentación del producto; el agente determinará, tras mapear Gradle, si el launcher se añade al módulo desktop existente o a un submódulo nuevo.

No se debe decidir ese detalle antes de reproducir la línea base.

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

Crear un perfil `GeoCeDG` basado en la infraestructura `AppConfig`, con:

- código de aplicación propio;
- namespace de preferencias propio;
- perspectiva inicial;
- distribución de paneles;
- filtros de comandos;
- categorías CeDG;
- recursos propios;
- acceso opcional a una perspectiva Classic para diagnóstico.

No conviene borrar funciones de GeoGebra. Es preferible ocultarlas en el perfil GeoCeDG y conservarlas en el modo diagnóstico, facilitando validación y sincronización upstream.

### 8.2 Barra recomendada

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

### 8.3 Fuente de verdad

La fuente será un manifiesto legible, por ejemplo `geocedg/specs/ui/toolbar.yml`. Un adaptador lo traducirá a la cadena de modos de GeoGebra. No se mantendrán varias cadenas manuales divergentes.

---

## 9. Instalador propio

Se recomienda `jpackage` del JDK actual:

- genera una imagen autocontenida con runtime Java;
- produce `exe`/`msi` en Windows, `dmg`/`pkg` en macOS y `deb`/`rpm` en Linux;
- permite iconos, licencia, asociaciones de fichero, accesos directos y recursos propios;
- cada paquete debe construirse en su plataforma objetivo;
- en Windows requiere WiX.

### Secuencia

1. generar y probar `app-image`;
2. crear paquete Windows inicial;
3. añadir asociación `.ggb` y, si se define, una extensión propia GeoCeDG;
4. incorporar metadatos, licencia y recursos propios;
5. generar SBOM/manifiesto de dependencias;
6. automatizar por plataforma;
7. firmar paquetes solo después de estabilizar el flujo.

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

### 10.3 Autoridad ejecutable

Crear progresivamente:

```text
tools/agent/
├─ bootstrap.ps1
├─ verify.ps1
├─ verify-baseline.ps1
├─ verify-geometry.ps1
├─ verify-locus.ps1
├─ verify-packaging.ps1
├─ license-audit.ps1
└─ upstream-sync.ps1
```

Los informes solo resumen. El resultado válido procede de estos comandos.

---

## 11. Diseño de `Locus V2`

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

Los intervalos \(I_j\) representan componentes o ramas orientadas. \(D_j\) contiene valores indefinidos o geométricamente inválidos.

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

### Decisión

Implementar primero fuera del kernel. El kernel solo proporcionará una vista de solo lectura:

```text
GeometryExportModel
├─ id y procedencia constructiva
├─ tipo geométrico
├─ unidades
├─ parámetros/coordenadas
├─ capa, nombre y estilo
├─ representación exacta/paramétrica
└─ tolerancia si hay discretización
```

### Alcance inicial

- proyecciones 2D;
- desarrollos planos;
- puntos, líneas, arcos, circunferencias, polilíneas;
- splines solo cuando exista representación adecuada;
- Locus general como curva paramétrica o polilínea adaptativa documentada.

### Regla

El exportador no resolverá geometría. Convertirá objetos ya resueltos.

Python puede:

- comprobar DXF;
- comparar entidades;
- efectuar conversiones por lotes;
- generar informes.

La exportación interactiva debe consumir el DTO Java para no depender del XML interno de `.ggb`.

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

## G0 - Gobierno, licencia y línea base

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

## G1 - Esqueleto operativo

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

**Trabajo**

- inventario de `.ggb`, `.ggt`, GGBScript;
- manifiestos;
- carga en laboratorio;
- métricas y casos degenerados.

**Salida**

- herramientas reproducibles y optativas;
- ninguna promoción automática a estable.

## G4 - Instalador propio

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

## G5 - DXF 2D

**Trabajo**

- DTO;
- servicio exportador;
- capas/unidades;
- validación de round-trip.

**Salida**

- exportación reproducible de modelos canónicos;
- tolerancias explícitas.

## G6 - Caracterización y base de Locus V2

**Trabajo**

- especificación matemática;
- evaluator;
- ramas;
- separación render/métrica;
- caché y feature flag;
- ejecución dual.

**Salida**

- Locus V2 se dibuja y conserva dependencias;
- independencia de zoom demostrada;
- todavía sin API pública estable.

## G7 - Longitud

**Trabajo**

- índice métrico;
- error;
- `LocusLength`;
- casos cerrados, multirrama y auto-intersección;
- modelo del cono oblicuo.

**Salida**

- medición dinámica tolerada y reproducible;
- regresión de los modelos CeDG.

## G8 - Intersecciones 2D

**Trabajo**

- objetos básicos;
- tangencia;
- identidad dinámica;
- ampliación a implícitas y locus-locus.

**Salida**

- Locus como entidad 2D de primer nivel para incidencia soportada;
- pruebas de topología y degeneraciones.

## G9 - Semántica espacial y proyecciones canónicas

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

**Trabajo**

- jerarquía, roles, bloqueo y filtros;
- visibilidad por vista;
- estados guardables;
- integración con DXF, PDF y modelos heredados.

**Salida**

- organización técnica comparable a herramientas de dibujo profesional;
- compatibilidad con capas numéricas de GeoGebra.

## G12 - Navegación, zoom y escalas extendidas

**Trabajo**

- zoom centrado en cursor y zoom de ventana por tecla;
- vistas nombradas y zoom previo;
- separación modelo/vista/dibujo/impresión;
- pruebas con rangos de escala extremos.

**Salida**

- navegación precisa y reproducible;
- ninguna métrica depende de pantalla, zoom o DPI.

## G13 - Visibilidad geométrica en proyecciones

**Trabajo**

- partición visible/oculta/silueta;
- poliedros y primitivas primero;
- transición dinámica y estilos;
- ampliación posterior a curvas y superficies generales.

**Salida**

- representación de visibilidad derivada de la geometría espacial y de la dirección de proyección;
- no se confunde con ocultación por capa.

## G14 - Puente a la vista 3D

**Trabajo**

- adaptador desde `SpatialObject3D`;
- cobertura incremental de tipos;
- reproyección diagnóstica;
- política de solo lectura inicial.

**Salida**

- el sistema CeDG se visualiza en la vista 3D existente sin crear una segunda verdad geométrica.

## G15 - Hojas, PDF y formatos gráficos

**Trabajo**

- `DrawingSheet`, marcos y cajetines;
- escalas físicas y viewports;
- PDF vectorial con tamaño exacto;
- SVG y PNG;
- pruebas dimensionales del fichero generado.

**Salida**

- publicación reproducible que respeta marco, tamaño físico, escala y capas.

## G16 - Rendimiento y escalabilidad

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

## 16. Instrucciones para crear el repositorio y copia local

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
- esta planificación a `docs/roadmap/geocedg_initial_plan.md`;
- `SPATIAL_PROJECTION_SEMANTICS_PROPOSAL.md` a `docs/architecture/proposed_spatial_projection_semantics.md`.

El último documento es una propuesta no normativa: G9 deberá convertirla, tras inspeccionar el código real, en especificaciones y ADR aprobadas.

Después:

```powershell
git switch -c bootstrap/geocedg-baseline
git add AGENTS.md FIRST_AGENT_TASK.md `
  docs/roadmap/geocedg_initial_plan.md `
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

## 17. Estrategia de ramas

```text
main                         estados liberables
integration                  integración validada
bootstrap/*                  preparación inicial
feature/ui-profile
feature/packaging
feature/dxf
feature/locus-v2
feature/spatial-semantics
feature/canonical-projections
feature/projective-boundary
feature/layers
feature/navigation-scale
feature/visibility
feature/3d-bridge
feature/publication
feature/performance
research/*
sync/geogebra-YYYYMMDD
```

La sincronización upstream se hará en rama específica:

```powershell
git fetch upstream --tags --prune
git switch -c sync/geogebra-2026MMDD integration
git merge --no-ff upstream/main
.\tools\agent\verify.ps1
```

Resolver, validar y abrir PR. No mezclar una sincronización upstream con una función nueva.

---

## 18. Primer paquete de decisiones que debe producir el agente

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

Ese informe permitirá ajustar esta planificación al commit real antes de empezar la implementación.

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

### Al cerrar G2

GeoCeDG deberá:

- arrancar como producto separado;
- mostrar su interfaz CeDG;
- conservar acceso de diagnóstico a Classic;
- usar recursos identificados y controlados;
- cargar funciones experimentales sin hacerlas permanentes;
- compilar y probarse de forma reproducible;
- mantener el kernel geométrico upstream sin cambios semánticos;
- estar preparado para instalarse y para abordar DXF y Locus V2.

Ese estado constituye la primera plataforma propia CeDG, todavía sin semántica espacial nativa.

### Al cerrar G9

GeoCeDG deberá además:

- representar un objeto 3D mediante identidad semántica estable;
- mantener vinculadas sus proyecciones definitorias, derivadas y auxiliares;
- certificar si las proyecciones canónicas son suficientes y no degeneradas;
- reconstruir y reproyectar primitivas;
- representar objetos compuestos mediante primitivas, superficies, caras y relaciones orientadas;
- serializar esas relaciones y mantenerlas en el grafo de dependencias;
- diagnosticar ambigüedad, contradicción y degeneración.

Este es el hito que habilita un DSL verdaderamente 3D.

### Al cerrar G16

La plataforma deberá añadir:

- DSL 3D y workbench clientes del kernel;
- capas técnicas y estados de vista;
- navegación y escalas extendidas;
- visibilidad geométrica en proyecciones;
- representación 3D derivada;
- publicación PDF/SVG con tamaño físico y escala controlados;
- rendimiento medido y mejorado sobre benchmarks canónicos.
