# Planificación desarrollo GeoCeDG

**User:** Anonymous
**Created:** 8/9/2026 11:05:20
**Updated:** 8/9/2026 17:38:51
**Exported:** 8/9/2026 17:44:46
**Link:** [https://chatgpt.com/g/g-p-68374facac5481919ce3a2f357ce954a/c/6a7842cf-714c-83eb-a792-fb0a909e6335](https://chatgpt.com/g/g-p-68374facac5481919ce3a2f357ce954a/c/6a7842cf-714c-83eb-a792-fb0a909e6335)

## Prompt:
8/9/2026, 11:05:19 AM

Quiero preparar un repositorio y carpeta local asociada en el que desarrollaré una versión de Geogebra orientada a CeDg, GeoCeDG. Para ello partiré de Geogebra, aprovechando que es software libre y tendré como primeros objetivos
1 Cambio de front end, inicialmente mostrando un gui diferente y una reorganización de los botones , además de incluir los adiciones que tengo desarrollados, pero que quiero que no sean permanentes.
2. Instalador diferente y propio, para evitar el de Geogebra que no es libre
3. Preparación con un sistema de prompt files , capa operativa , etc. Para facilitarme.el desarrollo de nuevos comandos y funciones, como exportación a DXF, etc.
4. Planificación del diseño de un nuevo comando Locus que supere al actual de Geogebra, permitiendo medición de longitud entre dos puntos en este, e intersección con otros objetos 2D.

Realizarme una planificación, estudiando el diseño de Geogebra que tienes en la red. Como resultado de la planificación dispondrás de un fichero.md para el agente , junto con unas instrucciones para mi, porque partiré creando el repo y copia local , con carpetas que organicen fuentes, prompt files , tools, modelos y aplicaciones. Los fuentes de partida serán una copia de Geogebra actual, posiblemente. Quiero que aproveches el conocimiento integrado que procede de la experiencia en mis otros proyectos.
Para finalizar , puedes revisar la metodología de CeDG y los avances actuales en tu conocimiento. Verás que no dispongo de plataforma propia, pero si de resultados prometedores de la metodología

## Response:
8/9/2026, 11:05:20 AM

# Resultado de la planificación

La recomendación central es crear **GeoCeDG como fork trazable del código fuente de GeoGebra**, pero evitar que el proyecto se convierta desde el inicio en una copia difícil de sincronizar. La arquitectura debe apoyarse en cuatro separaciones:

1. **Código upstream frente a extensiones GeoCeDG.**
2. **Semántica geométrica del kernel frente a interfaz, automatización y exportadores.**
3. **Herramientas CeDG heredadas frente a funciones experimentales y estables.**
4. **Definición geométrica de `Locus` frente a sus aproximaciones para visualización.**

He preparado tres documentos directamente utilizables:

- [AGENTS.md - instrucciones permanentes para los agentes](sandbox:/mnt/data/AGENTS.md)
- [GeoCeDG_PLAN_INICIAL.md - planificación completa e instrucciones para crear el repositorio](sandbox:/mnt/data/GeoCeDG_PLAN_INICIAL.md)
- [FIRST_AGENT_TASK.md - primera solicitud ejecutable para el agente](sandbox:/mnt/data/FIRST_AGENT_TASK.md)
- [Paquete ZIP con los tres documentos](sandbox:/mnt/data/GeoCeDG_initial_planning_bundle.zip)

`AGENTS.md` está redactado en inglés técnico para que actúe como autoridad operativa estable. La planificación está en español.

---

## 1. Forma correcta de iniciar GeoCeDG

No recomiendo copiar el directorio de una instalación de GeoGebra ni partir de su instalador. Debes:

- crear un fork de `geogebra/geogebra`;
- renombrarlo a `GeoCeDG`;
- clonarlo conservando todo su historial;
- añadir el repositorio oficial como remoto `upstream`;
- congelar el SHA inicial mediante un tag;
- comprobar que la aplicación desktop arranca antes de introducir cambios.

El repositorio actual de GeoGebra utiliza una construcción Gradle compuesta y separa el kernel compartido, desktop y web principalmente en `source/shared`, `source/desktop` y `source/web`. El arranque desktop documentado se hace mediante `:desktop:run`; además, el `build.gradle.kts` actual de desktop solicita Java 25 para esa tarea, por lo que no conviene basarse en documentación histórica que todavía mencione Java 8. ([GitHub](https://github.com/geogebra/geogebra))

Una carpeta local coherente con tus demás proyectos sería:

```text
C:\DESARROLLOYDATOS\Areas\ProyectosNoFinanciados\GeoCeDG
```

El arranque puede hacerse así:

```powershell
Set-Location C:\DESARROLLOYDATOS\Areas\ProyectosNoFinanciados

git clone https://github.com/<CUENTA>/GeoCeDG.git
Set-Location .\GeoCeDG

git remote add upstream https://github.com/geogebra/geogebra.git
git fetch --all --tags --prune

New-Item -ItemType Directory -Force .\docs\upstream | Out-Null
git rev-parse HEAD |
    Set-Content -Encoding utf8 .\docs\upstream\BASELINE_COMMIT.txt

git tag -a geogebra-baseline-2026-08-09 `
    -m "Initial GeoCeDG baseline from GeoGebra source"

java --version
.\gradlew.bat --version
.\gradlew.bat projects
.\gradlew.bat :desktop:run
```

No afirmo que ese build esté validado en tu futura copia local: precisamente la primera tarea del agente consiste en reproducirlo, documentar el toolchain real del commit y crear una puerta de verificación determinista.

---

## 2. Decisión de licencias

Hay una diferencia importante entre la situación actual y la recogida en algunos de tus trabajos anteriores. Los artículos describen correctamente la licencia que figuraba entonces como GPL, pero la página oficial actual, actualizada en noviembre de 2025, establece:

- código fuente: **EUPL 1.2**;
- instaladores y servicios de GeoGebra: licencia propia limitada a usos no comerciales;
- traducciones, documentación, imágenes de interfaz, estilos, iconos y logotipos: condiciones diferentes, principalmente CC BY-NC-SA;
- marca GeoGebra: no se concede mediante la EUPL.

La propia página oficial aclara que una obra derivada exclusivamente del código puede redistribuirse conforme a EUPL sin restricción no comercial, pero que al combinarla con los materiales e idiomas del producto vuelven a intervenir las restantes restricciones. ([GeoGebra](https://www.geogebra.org/license))

La EUPL permite usar, modificar y redistribuir el código, incluso comercialmente, pero exige preservar los avisos, identificar las modificaciones y redistribuir conforme a la EUPL o a una licencia compatible cuando corresponda. Además, la EUPL no concede la marca del licenciante y recomienda que un fork independiente use otro nombre; **GeoCeDG es, por tanto, una denominación adecuada**. ([Interoperable Europe Portal](https://interoperable-europe.ec.europa.eu/collection/eupl/faqs))

De aquí se deduce que tu punto 2 requiere más que un instalador propio:

- instalador GeoCeDG;
- iconos y logotipo propios;
- estilos propios;
- inventario de traducciones;
- matriz componente/licencia;
- `LICENSES/`, `NOTICE.md` y `THIRD_PARTY.md`;
- registro del SHA upstream y de los archivos modificados.

Durante la investigación interna pueden conservarse temporalmente recursos heredados, debidamente identificados. Para una distribución pública que pretenda ser plenamente libre habrá que sustituir o revisar específicamente esos recursos. Esto debe someterse a revisión jurídica antes de una liberación comercial.

---

## 3. Frontend propio sin romper GeoGebra

GeoGebra ya proporciona una interfaz `AppConfig` y configuraciones especializadas como `AppConfigGeometry`. Estas controlan el código y nombre de la aplicación, perspectiva, paneles, vistas, preferencias, filtros de comandos, comportamiento algebraico y tipo de toolbar. La configuración de Geometry, por ejemplo, define su propio nombre, perspectiva, panel de herramientas, preferencias y filtros. ([GitHub](https://github.com/geogebra/geogebra/blob/main/source/shared/common/src/main/java/org/geogebra/common/main/AppConfig.java))

Por ello, no recomiendo modificar globalmente la interfaz Classic. La primera extensión debe ser algo conceptualmente equivalente a:

```text
AppConfigGeoCeDG
GeoCeDG launcher
GeoCeDG perspective
GeoCeDG feature profile
GeoCeDG toolbar manifest
GeoCeDG resource bundle
```

La barra clásica está codificada mediante una cadena de identificadores de modos. GeoGebra ya dispone de un parser que interpreta menús, submenús y separadores, y las macros de usuario pueden añadirse al final de la definición. Esto permite que un manifiesto legible de GeoCeDG sea la fuente de verdad y se traduzca después al formato heredado. ([GitHub](https://github.com/geogebra/geogebra/blob/main/source/shared/common/src/main/java/org/geogebra/common/gui/toolbar/ToolBar.java))

La barra propuesta queda organizada por semántica CeDG:

1. Construcción y selección.
2. Proyecciones diédricas.
3. Cambios de plano, giros y abatimientos.
4. Primitivas, incidencia y posiciones relativas.
5. Curvas y superficies.
6. Intersecciones y locus.
7. Desarrollos y verdaderas magnitudes.
8. Medición y validación.
9. Herramientas CeDG.
10. Importación y exportación.

Debe conservarse una perspectiva **Upstream Classic** de diagnóstico. Ocultar comandos en GeoCeDG es preferible a eliminarlos del código, tanto para validar como para facilitar futuras fusiones con upstream.

---

## 4. Tratamiento de tus herramientas actuales

Tus trabajos ya muestran que las construcciones y GeoGebraScript pueden encapsular procedimientos CeDG complejos, incluida lógica condicional, herramientas de geometría plana, formatos ISO y operaciones de transporte. Sin embargo, la experiencia con la longitud del locus demuestra también que esta técnica no basta para modificar la semántica de un comando interno. `978-3-031-72829-7_81.pdf`

Por ello, las adiciones actuales deben entrar inicialmente en:

```text
models/legacy/
```

Cada herramienta tendrá:

```text
models/legacy/<tool-id>/
├─ model.ggb o tool.ggt
├─ manifest.yml
├─ README.md
├─ expected/
└─ screenshots/
```

Los estados serán:

```text
legacy -> research -> experimental -> stable
                         |
                         -> deprecated
```

Un **CeDG Laboratory** cargará herramientas `legacy`, `research` o `experimental` sin introducirlas en la toolbar estable. Así, su existencia no las convierte accidentalmente en API permanente.

Esta decisión incorpora la experiencia de tus otros proyectos: fuentes canónicas separadas de artefactos, manifiestos de procedencia, generación determinista, promoción explícita y validación antes de integrar una función.

---

## 5. Arquitectura del repositorio

La parte añadida alrededor de GeoGebra queda, en forma resumida:

```text
GeoCeDG/
├─ source/                  # fuentes GeoGebra conservando estructura
├─ geocedg/
│  ├─ specs/
│  ├─ features/
│  ├─ resources/
│  └─ validation/
├─ models/
│  ├─ canonical/
│  ├─ research/
│  ├─ regression/
│  └─ legacy/
├─ tools/
│  ├─ agent/
│  ├─ build/
│  ├─ release/
│  ├─ dxf/
│  └─ upstream/
├─ packaging/
├─ python/
├─ apps/
├─ .github/prompts/
├─ ai-shell/prompts/
├─ docs/
│  ├─ architecture/
│  ├─ adr/
│  ├─ licensing/
│  ├─ roadmap/
│  └─ validation/
└─ artifacts/               # generado, ignorado por Git
```

La fuente canónica de prompts estará en `.github/prompts/`; `ai-shell/prompts/` contendrá únicamente perfiles breves como `ask`, `plan`, `verify`, `refactor` y `architect`. La autoridad ejecutable será `tools/agent/`, no los informes redactados por el agente.

---

## 6. Decisión arquitectónica para DXF

La exportación DXF no debe introducirse en el kernel como lógica geométrica. El kernel debe proporcionar un modelo de solo lectura:

```text
GeometryExportModel
├─ identificador estable
├─ tipo de objeto
├─ unidades
├─ coordenadas o parámetros
├─ capa, nombre y estilo
├─ procedencia constructiva
├─ representación exacta/paramétrica
└─ tolerancia cuando exista aproximación
```

El exportador transformará objetos ya resueltos:

```text
Point      -> POINT
Segment    -> LINE
Circle     -> CIRCLE
Arc        -> ARC
Polyline   -> LWPOLYLINE
Curve      -> SPLINE, cuando proceda
Locus      -> curva paramétrica o polilínea adaptativa
```

Python podrá validar DXF, efectuar conversiones por lotes y producir informes. La exportación interactiva debería consumir el DTO Java, evitando interpretar el XML de `.ggb` como si fuera la autoridad geométrica.

---

## 7. Por qué `Locus` exige un rediseño de kernel

Tus resultados publicados establecen que un modelo CeDG conserva una secuencia de objetos gráfico-matemáticos, dependencias constructivas y parametrización dinámica, sin sustituir necesariamente las curvas por B-splines.

El locus es especialmente importante porque encapsula la construcción de un punto genérico y produce las proyecciones paramétricas de intersecciones y desarrollos. Las extensiones mediante `postLocus`, `locusLength` y `locusLength12` permitieron aproximar longitudes, pero introdujeron errores, ondulaciones, sensibilidad al filtrado y pérdida de fluidez dinámica. El propio estudio concluye que hace falta reformular `Locus` en los fuentes Java.

La inspección del código actual confirma la causa:

- `GeoLocusND` conserva una lista de muestras;
- el parámetro del camino va de $0$ a $N-1$;
- la posición se interpola entre muestras consecutivas;
- la pertenencia se calcula contra segmentos de la polilínea;
- `AlgoLocusND` emplea tolerancias dependientes de píxeles y las convierte usando la escala de la vista;
- `CmdIntersect` no presenta un tratamiento explícito de `GeoLocus`.

([GitHub](https://github.com/geogebra/geogebra/blob/main/source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLocusND.java))

Por tanto, aumentar el número de puntos no sería una solución suficiente.

---

## 8. Diseño de `Locus V2`

La definición propuesta es:

$$
\Omega=\bigcup_{j=1}^{m}I_j,
\qquad
F_j:I_j\setminus D_j\rightarrow\mathbb{R}^2,
\qquad
L=\bigcup_{j=1}^{m}F_j(I_j\setminus D_j).
$$

Aquí:

- $I_j$ es una rama orientada del dominio;
- $D_j$ contiene valores indefinidos o inválidos;
- $F_j$ es la construcción dinámica;
- la polilínea dibujada es solo una aproximación de $L$.

Internamente se separarán:

```text
LocusDefinition2D
LocusEvaluator2D
LocusMetricIndex
LocusRenderCache
LocusIntersectionSolver
```

### Longitud

Para una rama absolutamente continua:

$$
s_j(a,b)=\int_a^b\left\|F'_j(\omega)\right\|\,d\omega.
$$

La implementación distinguirá:

- resultado analítico o simbólico;
- cuadratura adaptativa;
- aproximación por evaluación puntual con estimación de error;
- estado indefinido por discontinuidad o dominio no acotado.

La primera API será experimental:

```text
LocusLength[L]
LocusLength[L, A, B]
```

Antes de incorporarla a `Length`, deben resolverse expresamente:

- las dos trayectorias posibles en una curva cerrada;
- orientación frente a arco mínimo;
- varias ramas;
- auto-intersecciones con múltiples preimágenes;
- ramas infinitas;
- puntos arbitrarios frente a puntos construidos sobre el locus.

### Intersección

Para un objeto implícito $G(x,y)=0$:

$$
h_j(\omega)=G(F_j(\omega))=0.
$$

Se aplicará:

1. selección de candidatos mediante cajas/BVH;
2. aislamiento adaptativo;
3. refinamiento en el parámetro original;
4. comprobación del residuo;
5. clasificación de transversalidad, tangencia, multiplicidad o solape;
6. identidad dinámica estable basada en rama y parámetro.

No bastará detectar cambios de signo, porque una tangencia puede tener una raíz de multiplicidad par.

La incorporación será progresiva:

```text
L1  recta, segmento, semirrecta, circunferencia y cónica
L2  función y curva implícita
L3  locus-locus y Path general
L4  tramos coincidentes
```

Todo ello comenzará detrás de `cedg.locus.v2`, conservando comportamiento legacy para los ficheros anteriores.

---

## 9. Modelos canónicos para validar GeoCeDG

Tus avances proporcionan ya una batería científica adecuada:

- encuentro cono-cilindro;
- iluminación focal de esfera;
- desarrollo de cono oblicuo mediante curva soporte;
- codos cilíndricos y cónicos con número discreto de virolas;
- helicoide desarrollable;
- convoluta con zonas propias y no propias de la arista de retroceso.

Los modelos discretos de codos han demostrado parametrización dinámica tanto continua como discreta y ventajas de automatización frente a CAD.

La investigación sobre desarrollables generales obtuvo una solución exacta para el helicoide y una solución discretizada para la convoluta; además, identificó la dependencia paramétrica de la región de convergencia $R_p$. Esto proporciona casos de prueba especialmente valiosos para ramas infinitas, cambios cualitativos y pérdida de validez.

Las pruebas de `Locus V2` deberán exigir:

- independencia de zoom, DPI y ventana;
- determinismo;
- longitud acumulada monótona;
- residuo de intersección dentro de tolerancia;
- identidad estable mientras no cambie la topología;
- invalidación correcta de cachés;
- degeneraciones explícitas, nunca geometría residual obsoleta.

---

## 10. Instalador GeoCeDG

`jpackage` es la opción inicial más sostenible. El JDK 25 permite crear aplicaciones Java autocontenidas y paquetes:

- Windows: `exe`, `msi`;
- macOS: `dmg`, `pkg`;
- Linux: `deb`, `rpm`.

Incluye runtime generado con `jlink`, iconos propios, asociaciones de fichero, accesos directos, licencia y recursos personalizados. Los paquetes deben construirse en su plataforma de destino; Windows requiere WiX. ([Oracle Docs](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-tool-user-guide.pdf?utm_source=chatgpt.com))

La secuencia será:

```text
build Java
    -> jpackage app-image
    -> prueba de arranque
    -> EXE/MSI
    -> instalación/desinstalación
    -> asociación de ficheros
    -> manifiesto y SBOM
```

`jpackage` también permite incluir una licencia, elegir directorio de instalación, crear accesos directos y registrar la aplicación en el menú de Windows. ([Oracle Docs](https://docs.oracle.com/en/java/javase/25/jpackage/manage-installation.html?utm_source=chatgpt.com))

---

## 11. Orden de desarrollo acordado

La planificación completa define estas puertas:

```text
G0  gobierno, licencia y línea base
G1  prompt files y capa operativa
G2  perfil GeoCeDG y frontend
G3  importación no permanente de herramientas existentes
G4  instalador propio
G5  exportación DXF 2D
G6  definición, evaluator y render de Locus V2
G7  medición de longitud
G8  intersecciones
G9  DSL Python y workbench
```

La secuencia es deliberada. No conviene iniciar `Locus V2` antes de disponer de:

- build congelado;
- modelos de regresión;
- verificadores ejecutables;
- serialización caracterizada;
- forma de comparar comportamiento legacy y V2.

---

## 12. Siguiente acción concreta

Después de crear y clonar el fork:

1. Copia [AGENTS.md](sandbox:/mnt/data/AGENTS.md) en la raíz.
2. Copia [FIRST_AGENT_TASK.md](sandbox:/mnt/data/FIRST_AGENT_TASK.md) en la raíz.
3. Coloca [GeoCeDG_PLAN_INICIAL.md](sandbox:/mnt/data/GeoCeDG_PLAN_INICIAL.md) como:

```text
docs/roadmap/geocedg_initial_plan.md
```

4. Crea una rama:

```powershell
git switch -c bootstrap/geocedg-baseline
```

5. Entrega `FIRST_AGENT_TASK.md` al agente.

Esa primera solicitud prohíbe expresamente modificar `Locus`, la interfaz o el instalador. Su función es inspeccionar el commit real, reproducir el build, localizar los puntos exactos de extensión y crear el scaffold mínimo. A partir de su informe se podrá preparar con precisión la primera solicitud de implementación: **G1, capa operativa y sistema de prompts**.

---

**Sources:**

- [GitHub - geogebra/geogebra: GeoGebra apps (mirror) · GitHub](https://github.com/geogebra/geogebra)
- [License - GeoGebra](https://www.geogebra.org/license)
- [FAQs | Interoperable Europe Portal](https://interoperable-europe.ec.europa.eu/collection/eupl/faqs)
- [Packaging Tool User's Guide](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-tool-user-guide.pdf?utm_source=chatgpt.com)

## Prompt:
8/9/2026, 12:33:26 PM

Plan perfecto. De acuerdo con todo, empezamos como indicas, pero en el plan, antes de G9, debería se capaz de definir un objeto 3D por sus proyecciones, de modo que el DSL, pueda convertir esa definición en proyecciones que pueda manejar el software, con semántica completa.
De esta forma el G8 trata las intersecciones de objetos 2D, incluyendo ya los locus V2, y en la siguiente fase trabajamos:

A. Asociación objeto 3D y proyecciones asociadas
B. Proyecciones canónicas como aquellas, capaces de definir de forma completa el objeto. En el caso de objetos primitivas, estás son bastante directas y basta con que no estén degeneradas. Por ejemplo, un recta de punta a un plano de proyección debe utilizar otro plano para definir sus proyecciones canónicas.
En objetos no primitivas, podría tratarse mediante las proyecciones canónicas de ciertas primitivas suyas más de definiciones de caras, etc. Intuyo una extensión de las metodologías de definición de objetos sólidos 3D mediante superficies, pero adaptada a su soporte en proyecciones , usando CeDG como abordaje.

El DSL podría ya ser definida sobre objetos 3D de mejor manera.

Y como fases posteriores añadir
- Tratamiento mejorado de capas, más similar a las herramientas de CAD
- tratamiento mejorado del zoom, con zoom de de ventana centrado en el cursor al picar una tecla, facilitar el trabajo con sistemas de escalas extendidas.
- Añadir visibilidad, mediante métodos propios en proyecciones
- Añadir conversión del sistema a la vista 3D.
- Mejora de la salida a pdf, que respete un marco de dimensiones dadas y su tamaño en pdf . Añadir tb otros formatos.
- Mejorar el rendimiento computacional. Actualmente es bastante bajo.

## Response:
8/9/2026, 12:33:26 PM

## Revisión incorporada

La modificación es estructural: **el DSL deja de ser la fase inmediatamente posterior a las intersecciones 2D**. Antes se introduce una capa semántica Java capaz de representar un objeto espacial, asociarlo con sus proyecciones y determinar si esas proyecciones lo definen de forma completa.

Esta decisión está plenamente alineada con la evolución ya prevista para CeDG. La documentación existente señala expresamente que CeDG base todavía no asocia las proyecciones con los objetos espaciales ni incorpora el concepto de sólido; además, define como evolución futura una asociación jerárquica vista-objeto y, posteriormente, la incorporación de sólidos. `Modelado-parametrico-computacional-v2.pdf` `Modelado-parametrico-computacional-v2.pdf` El trabajo de 2023 confirma también que CeDG carece todavía de un framework que asocie proyecciones con objetos 3D y lo identifica como una línea activa de investigación. `symmetry-15-00984-v2.pdf`

La prueba de concepto existente con `ListAssociations3DViews`, `ObjectViewCouple` y listeners JavaScript demuestra que la asociación puede organizarse jerárquicamente, pero se conserva ahora como evidencia de investigación y caso de regresión, no como arquitectura definitiva. `Modelado-parametrico-computacional-v2.pdf` `Modelado-parametrico-computacional-v2.pdf`

---

# 1. Nueva fase G9: semántica espacial y proyecciones canónicas

La identidad principal pasa a ser el objeto espacial:

```text
SpatialObject3D
├─ identidad estable
├─ tipo geométrico
├─ parámetros y dominio de validez
├─ dependencias constructivas
├─ estado de exactitud
├─ proyecciones asociadas
└─ definición primitiva o composición espacial
```

Cada proyección queda asociada mediante un objeto tipado:

```text
ProjectionBinding
├─ SpatialObject3D
├─ ProjectionFrame
├─ GeoElement 2D asociado
├─ rol de la proyección
├─ correspondencia paramétrica o topológica
├─ procedencia constructiva
└─ estado de validez
```

Los roles previstos son:

- `defining`: participa en la definición o reconstrucción del objeto;
- `derived`: se obtiene del objeto espacial;
- `auxiliary`: pertenece a un procedimiento CeDG;
- `analysis`: se genera para medir o validar;
- `presentation`: existe únicamente para representación o publicación.

Esto evita utilizar como identidad semántica los nombres, etiquetas, capas, orden de creación o coincidencias visuales de las entidades 2D.

## Criterio formal de proyección canónica

Para un tipo geométrico $T$, sea $X_T$ su espacio de configuraciones válidas y sea

$$
\Pi=\{\pi_1,\ldots,\pi_k\}
$$

un conjunto de operadores de proyección conocidos. Definimos:

$$
\Phi_{T,\Pi}(x)
=
\bigl(\pi_1(x),\ldots,\pi_k(x)\bigr).
$$

Un conjunto de proyecciones se considera **canónico y definitorio** en un dominio determinado cuando:

1. $\Phi_{T,\Pi}$ es inyectiva, salvo equivalencias geométricas expresamente admitidas;
2. existe un procedimiento CeDG explícito de reconstrucción;
3. la reproyección del objeto reconstruido coincide con las proyecciones definitorias;
4. se cumplen las condiciones de correspondencia y no degeneración propias del tipo;
5. la reconstrucción y sus restricciones pertenecen al grafo de dependencias.

Por tanto, **no se fija un número universal de vistas canónicas**. La suficiencia depende del tipo de objeto, su posición y el dominio de validez.

### Ejemplo de la recta de punta

Una recta perpendicular a un plano de proyección produce en él una proyección puntual. Esa proyección sigue siendo válida como representación, pero ha perdido la dirección de la recta y no puede actuar como única proyección definitoria.

El certificado canónico exigirá:

- otra proyección no colapsada; o
- una construcción equivalente que proporcione la dirección perdida.

El estado no será simplemente verdadero o falso. El kernel podrá devolver:

```text
VALID
UNDERDETERMINED
AMBIGUOUS
INCONSISTENT_PROJECTIONS
DEGENERATE
UNDEFINED
```

### Curvas espaciales

Dos curvas planas proyectadas no determinan por sí solas una curva espacial: es necesaria una correspondencia entre sus puntos.

Para las curvas CeDG, el mecanismo preferente será un parámetro común. En particular, dos proyecciones obtenidas con el mismo parámetro de `Locus V2` proporcionarán una correspondencia natural:

$$
C(t)
\longleftrightarrow
\bigl(c_1(t),c_2(t)\bigr).
$$

Esta es una razón adicional para situar G9 después de G6-G8: la nueva semántica espacial podrá apoyarse en la parametrización robusta de `Locus V2`.

---

# 2. Objetos no primitivos y sólidos

He separado esta parte como **G9C**, dentro de la misma fase global G9. Las primitivas y los objetos compuestos requieren contratos distintos.

La propuesta es un **modelo de frontera proyectiva CeDG**:

```text
ProjectiveBoundaryObject3D
├─ primitivas constituyentes
├─ vértices
├─ curvas o aristas espaciales
├─ caras
│  ├─ superficie soporte
│  ├─ bucles de contorno orientados
│  ├─ proyecciones asociadas
│  └─ dominio de validez
├─ incidencias y adyacencias
├─ orientación
└─ diagnóstico de cierre y validez
```

Se reutilizan conceptos geométricos útiles de B-Rep -vértices, aristas, caras, orientación y cierre-, pero **la B-Rep no se convierte en la autoridad del modelo**. La autoridad continúa siendo:

- la secuencia constructiva;
- las primitivas definitorias;
- las dependencias;
- las superficies soporte;
- las relaciones de incidencia;
- las proyecciones enlazadas.

Un sólido existirá cuando la frontera sea cerrada, orientada y topológicamente válida. Las operaciones booleanas o CSG podrán incorporarse posteriormente como procedimientos constructivos, pero no sustituirán la trazabilidad CeDG.

---

# 3. Roadmap revisado

La secuencia queda así:

| Puerta | Contenido |
|---|---|
| G0 | Gobierno, licencia, baseline, mapa del código y primera línea base de rendimiento |
| G1 | Prompt files, verificadores, CI, manifiestos y benchmarks |
| G2 | Perfil GeoCeDG y frontend |
| G3 | Herramientas heredadas no permanentes |
| G4 | Instalador propio |
| G5 | Exportación DXF 2D |
| G6 | Base semántica de `Locus V2` |
| G7 | Longitud sobre locus |
| G8 | Intersecciones 2D, incluidos los locus |
| **G9A** | Asociación nativa objeto 3D-proyecciones |
| **G9B** | Proyecciones canónicas y certificados de suficiencia para primitivas |
| **G9C** | Objetos compuestos, superficies, caras y sólidos proyectivos |
| **G10** | DSL 3D CeDG y workbench |
| **G11** | Capas jerárquicas y estados de vista |
| **G12** | Zoom, navegación y escalas extendidas |
| **G13** | Visibilidad geométrica propia en proyecciones |
| **G14** | Conversión a la vista 3D |
| **G15** | Hojas, PDF, SVG y formatos gráficos |
| **G16** | Optimización final y escalabilidad |

## G10: DSL sobre objetos 3D

El DSL podrá declarar directamente:

```text
spatial object
projection frames
canonical defining projections
primitive definitions
surfaces and faces
incidence relations
construction procedures
validity expectations
```

El compilador no reconstruirá por su cuenta el objeto espacial. Solicitará al kernel:

- creación;
- asociación;
- reconstrucción;
- certificación;
- generación de proyecciones;
- diagnóstico de degeneraciones.

Así se evita tener dos implementaciones diferentes de la semántica CeDG, una en Java y otra en Python.

---

# 4. Fases posteriores

## Capas

El modelo será jerárquico y añadirá:

- grupos y subcapas;
- bloqueo de selección y edición;
- visibilidad por vista;
- indicadores de impresión y exportación;
- filtros por función;
- estados de capa guardables;
- roles como `construction`, `auxiliary`, `result`, `dimension`, `annotation` o `layout`.

Se distinguen expresamente:

$$
\text{existencia geométrica}
\neq
\text{visibilidad geométrica}
\neq
\text{visibilidad de interfaz}.
$$

Ocultar una capa no elimina ni invalida el objeto.

## Zoom y escalas extendidas

Se separan cuatro espacios:

```text
coordenadas del modelo
transformación de la vista
escala de dibujo
escala física de impresión
```

G12 incluye:

- zoom centrado en el cursor;
- activación por tecla del zoom de ventana;
- `ZoomPrevious`;
- `FitSelection`;
- `FitLayer`;
- vistas nombradas;
- navegación por teclado;
- soporte de escalas extremas.

El zoom no podrá afectar a longitudes, intersecciones, visibilidad geométrica o certificados canónicos.

## Visibilidad

La visibilidad se calculará a partir del objeto espacial, la orientación de sus caras y la dirección de proyección. Una curva o arista podrá dividirse en subdominios:

```text
VISIBLE
HIDDEN
SILHOUETTE
COINCIDENT
AMBIGUOUS
```

La GUI solo aplicará estilos al resultado. El cálculo pertenecerá a un servicio geométrico Java conectado con G9.

## Vista 3D

La primera conversión será unidireccional:

```text
SpatialObject3D CeDG
        ↓
adaptador
        ↓
vista 3D de GeoGebra
```

La vista 3D será derivada y diagnóstica. No se mantendrán dos geometrías independientes. Como validación, el objeto 3D se reproyectará y sus proyecciones se compararán con los bindings canónicos.

La edición directa desde la vista 3D se pospone hasta disponer de una política inequívoca para traducir los cambios de vuelta a las construcciones CeDG.

## PDF y otros formatos

Se introduce un espacio de hoja independiente:

```text
DrawingSheet
├─ tamaño físico
├─ orientación
├─ marco y cajetín
├─ escala
├─ viewports
├─ capas incluidas
└─ estilos de impresión
```

El PDF deberá respetar:

- tamaño físico de página;
- dimensiones del marco;
- escala;
- márgenes;
- espesores;
- capas seleccionadas;
- geometría vectorial cuando sea posible.

Las pruebas comprobarán el `MediaBox` del PDF y dimensiones físicas conocidas. SVG y PNG de alta resolución se incorporan en la misma fase; DXF permanece como exportación geométrica en G5.

Los trabajos previos ya incluyeron controles para formatos ISO, escala y exportación, pero mediante herramientas añadidas; G15 convierte esas capacidades en un subsistema nativo, verificable y desacoplado del lienzo. `978-3-031-72829-7_81.pdf`

## Rendimiento

El rendimiento no queda aplazado por completo hasta G16. Desde G0 se medirán:

- arranque;
- latencia del grafo de dependencias;
- evaluación y teselación de locus;
- intersecciones;
- reconstrucción y reproyección;
- visibilidad;
- render 2D y 3D;
- serialización;
- memoria y cachés.

G16 será la fase de optimización intensiva, pero únicamente sobre perfiles reproducibles. Esto es especialmente importante porque los experimentos actuales ya muestran ondulaciones por acumulación numérica y pérdida de suavidad dinámica debido a la carga de `postLocus` y `locusLength`. `978-3-031-72829-7_81.pdf` `978-3-031-72829-7_81.pdf`

No se introduce C++ ni paralelismo complejo por anticipado. Primero se medirán los cuellos de botella reales en Java.

---

# 5. Efecto sobre la primera tarea del agente

La primera tarea sigue siendo exclusivamente de **caracterización y scaffold**. No implementará G9.

Se ha ampliado para que localice también:

- clases de objetos 3D;
- sistemas de coordenadas;
- operadores de proyección;
- transformaciones entre modelo y vistas 2D/3D;
- serialización de elementos 3D;
- capas, estilos y visibilidad;
- impresión y exportación vectorial;
- puntos de instrumentación y benchmarking.

De esta forma, la futura especificación G9 se redactará sobre el código real del commit seleccionado, no sobre una jerarquía de clases supuesta.

---

# Documentos revisados

- [Plan completo GeoCeDG v2](sandbox:/mnt/data/GeoCeDG_PLAN_INICIAL_v2.md)
- [AGENTS.md v2](sandbox:/mnt/data/AGENTS_v2.md)
- [Primera tarea del agente v2](sandbox:/mnt/data/FIRST_AGENT_TASK_v2.md)
- [Propuesta específica de semántica espacial y proyecciones](sandbox:/mnt/data/SPATIAL_PROJECTION_SEMANTICS_PROPOSAL.md)
- [Paquete completo con nombres canónicos](sandbox:/mnt/data/GeoCeDG_initial_planning_bundle_v2.zip)

SHA-256 del paquete:

```text
403ca9cc4e58117985c7bc2c1b1550f0db0c6e3645cc5a01b9423fd2c1d82e10
```

El paquete v2 sustituye al anterior. Tras crear y clonar el fork, deben copiarse sus cuatro archivos con los nombres y ubicaciones indicados en la sección 16.7 del plan y ejecutarse después las instrucciones de `FIRST_AGENT_TASK.md`.



---
Powered by [ChatGPT Exporter](https://www.chatgptexporter.com)
