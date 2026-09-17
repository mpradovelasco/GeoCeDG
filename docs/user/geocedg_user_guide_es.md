<!-- geocedg-guide-section: about-this-guide -->
# Guía de usuario de GeoCeDG

GeoCeDG 1.0.0 · Edición en español

Esta es la guía de usuario oficial de GeoCeDG. Describe qué hace la aplicación
hoy, cómo trabajar con ella y dónde están sus límites actuales. Existe una
edición inglesa con la misma estructura y los mismos ejemplos,
`geocedg_user_guide_en.md`; la aplicación abre la edición correspondiente al
idioma de producto activo.

La guía está dirigida a un usuario técnico o científico. No es un manual de
desarrollo: no describe la compilación, el sistema de verificación ni la
arquitectura interna en Java. Cuando una noción interna resulta imprescindible
para comprender el comportamiento observable, se explica conceptualmente.

Todo lo que aquí se afirma está respaldado por el producto actual, sus
especificaciones aprobadas y sus pruebas. Las capacidades diseñadas pero no
disponibles se identifican como tales, y nada meramente planificado se presenta
como presente.

---

<!-- geocedg-guide-section: what-is-geocedg -->
## 1. Qué es GeoCeDG

GeoCeDG es una aplicación de geometría dinámica orientada a la **Geometría
Descriptiva computacional extendida (CeDG)**. Está construida sobre el núcleo
geométrico compartido de GeoGebra y lo extiende allí donde el trabajo de
geometría descriptiva exige una semántica que la geometría dinámica ordinaria no
proporciona.

### 1.1 La idea central: la construcción es el objeto

En CeDG, un resultado geométrico nunca es separable de la secuencia explícita de
construcciones que lo produce. Esto tiene consecuencias prácticas que
encontrará en toda la aplicación:

- **Trazabilidad constructiva.** Todo objeto derivado permanece ligado a los
  datos que lo definen. Si cambia una entrada, el resultado se recalcula a
  través del grafo de dependencias.
- **Parametrización explícita.** Los parámetros, sus dominios y sus rangos de
  validez se declaran; no se deducen de lo que casualmente aparece dibujado.
- **Procedimiento y resultado son cosas distintas.** La curva que ve en
  pantalla es una *representación* de un objeto semántico. No es el objeto y
  nunca es la autoridad de una longitud, una incidencia o una intersección.
- **Aproximación explícita.** Cuando un valor es aproximado, la aplicación lo
  dice e informa del método y de la evidencia que lo sustenta. Una aproximación
  nunca se presenta como resultado exacto.
- **Tratamiento deliberado de la degeneración.** Tangencias, coincidencias,
  discontinuidades, pérdida de ramas y configuraciones casi singulares producen
  un estado explícito, no geometría obsoleta heredada de un estado anterior.

### 1.2 En qué se diferencia del CAD

GeoCeDG no es un modelador orientado a sólidos. No tiene árbol de
características, ni flujo de modelado sólido basado en historial, ni una capa de
abstracción CAD genérica superpuesta a la geometría. El modelo es la propia
construcción: sus objetos, sus dependencias y sus parámetros explícitos.

La diferencia práctica es que usted construye *enunciando relaciones*, y la
aplicación conserva esas relaciones. No esculpe un resultado para después
intentar recuperar la intención que lo originó.

### 1.3 Versión y estado

La aplicación se identifica como **GeoCeDG 1.0.0** en el título de la ventana y
en **Ayuda → Acerca de GeoCeDG**. Ese número de versión identifica el estado de
producto descrito en esta guía. No constituye por sí mismo una publicación: el
empaquetado y la distribución se gobiernan por separado.

Dos capacidades aquí descritas —las curvas semánticas (Locus V2 y Spline V2) y
la exportación DXF extendida— están activadas por defecto en GeoCeDG, pero
conservan la madurez `experimental`. Funcionan como comportamiento normal del
producto y no requieren ningún argumento de arranque; «experimental» significa
que su contrato público todavía puede refinarse.

---

<!-- geocedg-guide-section: getting-started -->
## 2. Primeros pasos

### 2.1 Iniciar GeoCeDG

Inicie GeoCeDG desde su acceso directo instalado. El título de la ventana indica
`GeoCeDG 1.0.0`, o `GeoCeDG 1.0.0 — <nombre de archivo>` cuando hay un documento
abierto.

Si trabaja desde un checkout de fuentes preparado en lugar de una copia
instalada, el perfil de producto se lanza mediante la tarea Gradle
`:desktop:desktop:runGeoCeDG`. No hace falta ningún argumento adicional para
obtener las capacidades descritas en esta guía.

### 2.2 La ventana principal

GeoCeDG se abre en el espacio de trabajo **Construcción CeDG**, que es el
espacio del producto. Su disposición por defecto ofrece:

- la vista **Gráficos**, con ejes visibles y sin cuadrícula;
- la vista **Álgebra**;
- la barra de **Entrada algebraica**, con ayuda de entrada.

Las vistas **Hoja de cálculo**, **CAS**, **Protocolo de construcción** y
**Propiedades** están disponibles e inicialmente cerradas. Un documento guardado
o una preferencia guardada pueden imponer otra disposición; **Vista → Restaurar
distribución de Construcción** devuelve la organización del producto sin tocar la
geometría.

### 2.3 Idioma del producto

GeoCeDG ofrece dos idiomas de producto: **inglés** y **español**. El inglés es
el idioma de reserva cuando no se aplica ninguna otra elección.

Elija el idioma en **Opciones → Opciones de GeoCeDG → Idioma del producto…**.
La elección afecta a menús, diálogos y ayuda integrada, incluida la edición de
esta guía que abre **Ayuda → Guía de usuario de GeoCeDG**.

### 2.4 Preferencias

**Opciones → Preferencias…** abre la configuración global. Abrirla no selecciona
ningún objeto.

La pestaña **Disposición y presentación** reúne los ajustes de presentación de
GeoCeDG: el tema de presentación y los tamaños de presentación descritos a
continuación.

### 2.5 Temas de presentación

Un tema de presentación selecciona los colores de superficies conocidas de la
interfaz. Hay exactamente tres, elegibles en **Preferencias → Disposición y
presentación → Tema**:

| Tema | Carácter |
|---|---|
| **Original** | La apariencia histórica de GeoCeDG. Es el valor por defecto y el de reserva. |
| **Papel científico** | Paleta cálida y de bajo contraste, adecuada para trabajo en papel y capturas destinadas a publicación. |
| **Geometría fría** | Paleta fría y neutra, con un lienzo algo más luminoso. |

Un tema cambia solo superficies de presentación: marco, superficie de menú y
barra, paneles, fondo del lienzo de Gráficos y separadores. Nunca cambia el
color, el estilo o la visibilidad de los objetos, el significado de ejes o
cuadrícula, la salida de exportación, ni nada almacenado en un documento. La
elección es una preferencia de usuario: sobrevive al reinicio y no se ve
afectada por abrir un documento.

### 2.6 Tamaños de presentación

**Preferencias → Disposición y presentación → Tamaños de presentación** ofrece
seis controles independientes:

| Control | Qué gobierna | Por defecto |
|---|---|---|
| Tamaño de fuente de la interfaz general | Barra de entrada, ayuda de comandos, diálogos ordinarios | 12 pt |
| Tamaño de fuente de menús | La barra de menús y sus elementos | 14 pt |
| Tamaño de iconos de la barra | Botones de la barra, incluidas las herramientas propias fijadas | 28 px |
| Tamaño de fuente de Álgebra | La vista Álgebra y su editor | 12 pt |
| Tamaño de fuente del Protocolo de Construcción | La tabla del protocolo y su navegación | 12 pt |
| Tamaño de fuente de Gráficos | Ejes, coordenadas y etiquetas de objeto en las vistas Gráficas | 12 pt |

Son independientes: ninguno se deduce de otro. La fuente de los objetos **Texto**
de construcción es un ajuste aparte (**Tamaño de fuente de Texto**) y no forma
parte de este grupo, porque un objeto Texto pertenece al documento.

Los seis son preferencias de usuario. Nunca se escriben en una construcción ni
aparecen en un archivo guardado.

---

<!-- geocedg-guide-section: interface-and-workflow -->
## 3. Interfaz y flujo de construcción

### 3.1 Vistas

- **Gráficos** es donde se construye y donde actúa la herramienta vigente.
- **Álgebra** lista los objetos con su valor, descripción o definición, según el
  modo elegido en **Opciones → Presentación de Álgebra**.
- **Protocolo de construcción** (**Vista → Protocolo de construcción**) muestra
  la construcción como una secuencia ordenada, que es la lectura directa de su
  estructura de dependencias. **Vista → Mostrar barra de navegación de la
  construcción** activa o desactiva el control por pasos asociado a la vista
  Gráfica; no crea objetos ni genera un paso de deshacer.
- **Entrada algebraica** acepta comandos y definiciones.

### 3.2 Menús

La barra de menús contiene **Archivo**, **Editar**, **Vista**, **Construcción**,
**Opciones**, **Automatización** y **Ayuda**. Son proyecciones compactas de un
único catálogo de acciones, de modo que una acción referenciada desde varios
grupos aparece una sola vez.

**Construcción** es el menú geométrico y se organiza así:

| Grupo | Contenido |
|---|---|
| Puntos | Punto, Punto en objeto, Punto medio |
| Rectas y vectores | Recta, Segmento, Semirrecta, Vector, Segmento de longitud dada, Vector desde un punto |
| Polígonos | Polígono, Poligonal, Polígono regular, Polígono rígido, Polígono vectorial |
| Construcciones derivadas | Recta paralela, Recta perpendicular, Mediatriz, Bisectriz |
| Parámetros y controladores | Deslizador, Ángulo de amplitud dada, Casilla, Botón, Campo de entrada, Animar objeto seleccionado, Texto |
| Relaciones e intersecciones | Intersección, Tangentes, Relación, Polar o diámetro, Inspeccionar resultado rico y las tres acciones de materialización |
| Círculos y cónicas | Circunferencia (dos puntos, tres puntos, centro y radio), Arco, Cónica por cinco puntos, Elipse, Parábola, Hipérbola, Compás, Semicircunferencia, Arco de circunferencia circunscrita, Sector, Sector circunscrito |
| Curvas semánticas | Locus V2, Punto sobre curva semántica, Spline V2, Inspeccionar definición de curva semántica… |
| Métricas y validación | Ángulo, Distancia o longitud, Longitud total de Locus V2, Longitud parcial de Locus V2, Área, Pendiente |
| Transformaciones de semejanza | Reflejar respecto a un punto, Reflejar respecto a una recta, Trasladar por vector, Rotar un ángulo, Homotecia desde un punto |
| Anotaciones y medios | Imagen |
| Procedimientos de proyección manual | Procedimientos diédricos CeDG (no autorizados) |

La última entrada está visible pero no disponible: el espacio de procedimientos
diédricos no es una capacidad autorizada y así lo indica al seleccionarlo.

### 3.3 Barra de herramientas y desplegables

La barra proyecta las acciones de uso frecuente de ese mismo catálogo, agrupadas
en Mover; Punto e Intersección; Rectas y vectores; Polígonos; Construcciones
derivadas; Círculos y cónicas; Curvas semánticas; Ángulos y longitudes;
Transformaciones; Parámetros y controladores; Navegación.

**Curvas semánticas** y **Navegación** son desplegables mixtos compactos:
muestran como botón principal la última acción elegida. Esa memoria es solo
estado de presentación; no cambia ninguna semántica.

Una acción no disponible explica por qué lo está y no crea nada.

### 3.4 Seleccionar, crear y editar

Seleccione con la herramienta **Mover**. Cree eligiendo una herramienta y
marcando los datos requeridos, o escribiendo una definición en Entrada
algebraica.

La Entrada algebraica muestra una previsualización mientras escribe. **Intro**
confirma una transacción; **Escape** cancela; perder el foco no crea nada. No se
crea nada hasta pulsar Intro.

Para editar un objeto numérico ordinario puede hacer doble clic, pulsar **F2**,
editar directamente su fila de Álgebra o escribir una asignación compatible como
`k=0.25` en la Entrada algebraica. Las cuatro rutas conservan el mismo objeto; la
edición es explícita y constituye un único paso de deshacer.

**Deshacer** es `Ctrl`+`Z` y **Rehacer** es `Ctrl`+`Y`, también disponibles en
**Editar → Historial**.

### 3.5 Inspeccionar una definición

**Inspeccionar definición…** (menú contextual, o **Vista → Inspección de la
construcción**) muestra cómo está definido un objeto, en solo lectura. Para las
curvas semánticas y sus objetos derivados, **Inspeccionar definición de curva
semántica…** muestra además la estructura semántica: rama, componente, dirección
semántica, estado de la dirección (actual y admisible, o retenida/inactiva),
fuente, proveedor, parámetro y, en fuentes periódicas, la elevación periódica y
el lado de costura.

La inspección nunca modifica un objeto, nunca cambia su identidad y nunca cambia
el modo global de presentación de Álgebra.

Algunos objetos muestran una **definición de solo lectura**. No es un defecto:
significa que la redefinición directa arbitraria de ese objeto no es una
operación aprobada. Edite en su lugar los datos que lo definen. Propiedades lo
explica cuando corresponde.

---

<!-- geocedg-guide-section: documents -->
## 4. Documentos

```text
.cedg = documento nativo de GeoCeDG
.ggb  = entrada de compatibilidad
```

### 4.1 Formato nativo y formato de compatibilidad

`.cedg` es la extensión nativa de documento de GeoCeDG. Guarde su trabajo como
`.cedg`.

`.ggb` se acepta como entrada de compatibilidad. GeoCeDG lo abre sin alterarlo
de forma destructiva. Abrir un `.ggb` no lo convierte, y guardar trabajo GeoCeDG
como `.ggb` no es una promesa de que una aplicación upstream externa vaya a
conservar los tipos de objeto propios de GeoCeDG.

La extensión es una identidad de producto y de entrada/salida. No cambia el
significado geométrico, la madurez de los objetos, la identidad durable ni el
comportamiento de los comandos, y renombrar un archivo no transforma su
contenido. Internamente ambos archivos conservan hoy el código de aplicación
heredado `classic` y la misma disposición de archivo; GeoCeDG no introduce un
formato nuevo.

### 4.2 Comandos de documento

| Acción | Dónde |
|---|---|
| Nuevo archivo | Archivo → grupo Abrir |
| Abrir… | Archivo → grupo Abrir |
| Abrir reciente… | Archivo → grupo Abrir |
| Guardar | Archivo → grupo Guardar |
| Guardar como… | Archivo → grupo Guardar |
| Vista previa de impresión… | Archivo → Imprimir |
| Cerrar | Archivo → Cerrar |

**Guardar** escribe en la ruta nativa actual cuando existe; en caso contrario se
comporta como **Guardar como…**. No hay degradación silenciosa de `.cedg` a
`.ggb` ni conversión destructiva en ninguna dirección.

Si un guardado falla su prevalidación, el archivo anterior en disco se conserva y
sus cambios quedan sin guardar. Nada se escribe a medias.

### 4.3 Qué conserva un documento

Un `.cedg` guardado conserva la construcción y su estructura de dependencias:
curvas semánticas, sus dominios y parámetros explícitos, puntos semánticos con su
rama y su parámetro canónico, puntos de intersección materializados con sus
selectores durables y la disposición visual del documento.

No conserva las preferencias de la aplicación. Idioma, tema, tamaños de
presentación y biblioteca de herramientas propias instaladas pertenecen al perfil
GeoCeDG, no al documento.

### 4.4 Relación con Classic

La sesión de diagnóstico Classic de GeoCeDG es un proceso separado con
preferencias aisladas. Lee documentos ordinarios, pero no adquiere la creación de
curvas semánticas de GeoCeDG. Véase la sección 14.

---

<!-- geocedg-guide-section: basic-geometry -->
## 5. Geometría básica

GeoCeDG hereda las herramientas ordinarias de construcción plana del núcleo
compartido. Esta sección cubre lo necesario para trabajar con las capacidades
CeDG; no es un manual completo de cada herramienta heredada.

### 5.1 Puntos

Cree un punto libre con la herramienta **Punto** o escribiendo un par de
coordenadas:

```text
A=(0,0)
```

**Punto en objeto** restringe un punto a un objeto existente. **Punto medio**
crea el punto medio de dos puntos o de un segmento.

Un punto colocado sobre una curva *semántica* es una construcción distinta y más
rica; se describe en la sección 8.

### 5.2 Rectas, segmentos, semirrectas y vectores

```text
g=Line(A,B)
s=Segment(A,B)
r=Ray(A,B)
v=Vector(A,B)
```

Las herramientas correspondientes están en **Construcción → Rectas y vectores**.
**Segmento de longitud dada** y **Vector desde un punto** admiten un dato
numérico.

### 5.3 Circunferencias y cónicas

```text
c=Circle((0,0),1)
k=Circle(A,B,C)
e=Ellipse(A,B,C)
```

**Construcción → Círculos y cónicas** ofrece además arcos, sectores,
semicircunferencias, el compás, la cónica por cinco puntos, la parábola y la
hipérbola.

### 5.4 Polígonos

**Construcción → Polígonos** proporciona las herramientas ordinarias de
polígono, poligonal, polígono regular, polígono rígido y polígono vectorial. Se
comportan como en el núcleo compartido.

### 5.5 Parámetros y controladores

Un parámetro es un valor explícito y editable del que dependen otras
construcciones. Cree uno escribiendo una asignación o con la herramienta
**Deslizador**:

```text
k=1
```

Deslizadores, casillas, botones, campos de entrada y el ángulo de amplitud dada
están en **Construcción → Parámetros y controladores**. Un parámetro es la vía
normal para gobernar un lugar geométrico, una transformación o cualquier relación
que quiera estudiar dinámicamente.

### 5.6 Texto

La herramienta **Texto** crea un objeto Texto de construcción. El Texto pertenece
al documento; su tamaño lógico de fuente es una propiedad del documento
(**Tamaño de fuente de Texto**) y, en GeoCeDG, un Texto anclado a coordenadas
reales crece o decrece visualmente con el zoom de cada vista Gráfica mientras su
contenido, su anclaje y su tamaño lógico permanecen invariables. Un Texto con
posición absoluta en pantalla conserva el tamaño fijo heredado.

### 5.7 Transformaciones heredadas

**Construcción → Transformaciones de semejanza** proporciona la reflexión
respecto a un punto o a una recta, la traslación por vector, la rotación por
ángulo y la homotecia desde un punto. Aplicadas a objetos ordinarios se comportan
como en el núcleo compartido; aplicadas a curvas semánticas tienen la semántica
adicional descrita en la sección 10.

---

<!-- geocedg-guide-section: locus-v2 -->
## 6. Locus V2

### 6.1 Qué es un Locus V2

Un **Locus V2** es una curva semántica: la imagen de una construcción dependiente
evaluada sobre un dominio unidimensional explícito y orientado.

De manera informal, usted dispone de un *generador* —un parámetro conductor y la
construcción que depende de él— y declara el intervalo en el que se permite
mover ese parámetro. El lugar geométrico es el conjunto de posiciones que toma el
punto dependiente a lo largo de ese intervalo.

### 6.2 Por qué no es una polilínea dibujada

El lugar geométrico clásico muestreado es una lista de puntos calculados unidos
por segmentos rectos. Su identidad *es* ese muestreo. Un Locus V2 es distinto: el
muestreo dibujado en pantalla es una representación derivada, producida para
mostrarse, y nunca es autoridad de nada.

En particular:

- las longitudes se calculan a partir de la definición semántica, no de las
  cuerdas dibujadas;
- las intersecciones se resuelven en los parámetros originales, no contra
  segmentos de pantalla;
- los resultados no cambian con el zoom, el viewport ni la resolución;
- un punto «sobre» la curva porta un parámetro semántico, no un índice de
  vértice.

Esta separación es la razón de ser de Locus V2. Es también el motivo por el que
el comando `Locus` clásico sigue disponible y sin cambios: son objetos distintos
con contratos distintos.

### 6.3 Estructura: generador, dominio, orientación, rama, componente

- **Generador (fuente).** El parámetro conductor y la construcción dependiente
  que produce la curva. Su identidad procede de la coordenada o dominio
  semántico explícito, del tipo y versión del proveedor y de sus datos durables;
  nunca del rango visible de un deslizador, de una etiqueta, de un vértice
  dibujado o del valor actual.
- **Parámetro.** El parámetro canónico de la curva es el del conductor y su
  dominio. No es el índice de un punto muestreado.
- **Dominio explícito.** Usted declara el intervalo y si cada extremo está
  incluido. El dominio no procede de los límites visuales de un deslizador ni de
  lo dibujado.
- **Orientación.** El dominio está orientado, y eso es lo que da sentido a una
  longitud parcial y a una dirección.
- **Rama.** Una dirección semántica dentro de la curva. La rama por defecto
  producida por un generador es `generator.main`.
- **Componente.** Un trozo conexo maximal de la curva dentro de una evaluación
  dada. Allí donde la construcción deja de estar definida, la curva se separa en
  componentes; el hueco nunca se puentea.

### 6.4 Vigencia (currentness)

Un resultado semántico se calcula para un estado concreto de la construcción.
Cuando una entrada cambia, el resultado se recalcula y la evidencia anterior deja
de estar vigente. Por eso el inspector de definición puede informar de una
dirección como **actual y admisible** o como **retenida/inactiva — no actual**.

La vigencia importa especialmente al materializar puntos de intersección: véase
la sección 8.

### 6.5 Crear un Locus V2

Desde la interfaz, **Construcción → Curvas semánticas → Locus V2** ofrece la
selección de generador y dominio.

Desde la Entrada algebraica, las formas del comando son:

```text
LocusV2( <Dependent Point>, <Constrained Point> )
LocusV2( <Dependent Point>, <Scalar State>, <Domain Descriptor> )
LocusV2( <Dependent Point>, <Scalar State>, <True Driver>, <Domain Descriptor> )
```

Un descriptor de dominio es una lista
`{periódico, {inicio, fin, incluyeInicio, incluyeFin}}`.

### 6.6 Ejemplo resuelto

Introduzca cada línea una sola vez, pulsando Intro tras cada una:

```text
u=0
G=(u,0)
dom={false,{0,4,true,true}}
L=LocusV2(G,u,dom)
U=Point(L,"generator.main",1)
V=Point(L,"generator.main",3)
LL=Length(L)
LP=Length(L,U,V)
```

Resultados esperados: **`LL` = 4** y **`LP` = 2**.

El lugar geométrico es aquí el segmento del eje *x* entre `(0,0)` y `(4,0)`,
porque usted declaró explícitamente el dominio conductor `[0,4]`. Obsérvese que
el dominio procede de `dom`, no del rango de ningún deslizador ni de lo dibujado.

### 6.7 Disponibilidad

La creación de Locus V2 es comportamiento normal de GeoCeDG y no necesita ningún
argumento de arranque. El argumento histórico `--enableLocusV2=false` se conserva
como override de diagnóstico y compatibilidad que arranca GeoCeDG *sin* la
superficie de creación V2; cambia únicamente la exposición, nunca la identidad
durable, la serialización ni el significado geométrico.

La sesión de diagnóstico Classic nunca adquiere la creación V2, se usen los
argumentos que se usen.

Locus V2 conserva la madurez `experimental` y es exclusivo de GeoCeDG. Admite los
generadores, dominios explícitos y objetivos documentados. **No** es un `Path`
genérico: no se implican operaciones que esperen un objeto camino arbitrario.

---

<!-- geocedg-guide-section: spline-v2 -->
## 7. Spline V2

### 7.1 Qué es una Spline V2

Una **Spline V2** es una curva semántica construida a partir de una lista
ordenada de puntos constructores y un grado. Es un objeto semántico de primera
clase de la misma familia que Locus V2: tiene parámetro explícito, dominio
orientado, ramas y componentes, y el trazo dibujado es una representación
derivada.

Es un objeto distinto de la `Spline` Classic heredada, que permanece disponible y
sin cambios.

### 7.2 Crear una Spline V2

**Construcción → Curvas semánticas → Spline V2**, o la Entrada algebraica:

```text
SplineV2( <List of Points> )
SplineV2( <List of Points>, <Degree> )
SplineV2( <List of Points>, <Degree>, <Weight Function> )
SplineV2( <Point>, <Point>, <Point>, ... )
```

La clave de rama por defecto de una Spline V2 es `spline-v2/main`.

### 7.3 Los puntos constructores no son puntos semánticos

Los puntos que pasa a `SplineV2` son **puntos constructores**. Definen la curva.
Son puntos ordinarios, libres o dependientes, y **no** portan por sí mismos una
posición semántica sobre la curva resultante, aunque sus coordenadas estén
exactamente sobre ella.

Es un contrato deliberado, y la fuente más habitual de sorpresa. Una consulta
métrica o de incidencia necesita un punto con proveniencia semántica admisible
sobre esa curva; la coincidencia cartesiana no equivale a posición semántica.
Véase la sección 9.

### 7.4 Puntos sobre una Spline V2

Hay dos vías:

- **Interactiva.** Elija la herramienta **Punto** ordinaria y haga clic sobre el
  *trazo* de la curva. Hacer clic dentro de una curva cerrada no la selecciona.
  Si la preimagen semántica es única, se crea un punto. Si hay varias
  preimágenes, elija explícitamente o cancele; nada se elige por proximidad. Ese
  punto puede arrastrarse con Mover y conserva su identidad mientras la
  continuación sea única.
- **Parámetro explícito.** Use la forma con dirección, que es exacta y
  automatizable:

```text
P=Point(S,"spline-v2/main",0.25)
```

Un punto creado con parámetro explícito se controla mediante ese parámetro, no
mediante un arrastre que lo sustituya en silencio. El parámetro puede ser un
número con nombre para poder editarlo.

### 7.5 Ejemplo resuelto

En una construcción nueva:

```text
h=0
A=(-2,h)
B=(-2/3,h)
C=(2/3,h)
D=(2,h)
S=SplineV2({A,B,C,D},3)
P=Point(S,"spline-v2/main",0.25)
Q=Point(S,"spline-v2/main",0.75)
M=Length(S)
MP=Length(S,P,Q)
```

Resultados esperados: **`M` = 4** y **`MP` = 2**.

Editar `h` traslada la spline sin cambiar ninguna de las dos longitudes. Es la
dependencia dinámica funcionando como corresponde: la medida sigue a la
definición semántica, no al dibujo.

### 7.6 Persistencia

Una Spline V2, sus puntos semánticos y sus parámetros se guardan en el documento
nativo `.cedg` y se reconstruyen al reabrirlo, conservando sus identidades
durables.

### 7.7 Disponibilidad

Igual que Locus V2, la creación de Spline V2 es comportamiento normal de GeoCeDG
sin argumento de arranque, conserva la madurez `experimental` y no está
disponible en la sesión de diagnóstico Classic.

---

<!-- geocedg-guide-section: semantic-points-intersections-materialization -->
## 8. Puntos semánticos, intersecciones y materialización

### 8.1 Puntos semánticos

Un **punto semántico** sobre una curva semántica porta una dirección explícita:
la curva fuente, una rama, un componente y un parámetro canónico, junto con la
evidencia de su vigencia. Esa dirección —y no sus coordenadas— es su identidad.

En consecuencia, ninguno de los siguientes elementos define la posición de un
punto sobre una curva:

- sus coordenadas;
- su proximidad al trazo dibujado;
- el orden gráfico de dibujo;
- un índice visual o de lista.

Dos puntos de coordenadas idénticas pueden tener proveniencias semánticas
completamente distintas, y uno de ellos puede ser utilizable para una consulta
dada mientras el otro no lo es.

### 8.2 Resultados ricos de intersección

`Intersect(...)` aplicado a una curva semántica no devuelve una lista de puntos.
Devuelve un **resultado rico de intersección**: un objeto que porta cada raíz
hallada por el solucionador junto con su evidencia —rama y componente de cada
lado, parámetros semánticos, residuo, veredicto de admisibilidad y un **token
exacto** durable que identifica esa solución.

```text
c=Circle((0,0),1)
R=Intersect(S,c)
```

Las formas de comando relevantes son:

```text
Intersect( <Locus V2>, <Supported Object> )
Intersect( <Locus V2>, <Locus V2> )
Intersect( <Rich Intersection Result>, <Solution Token> )
```

Un resultado rico es un objeto dependiente normal: se recalcula cuando cambian
sus entradas.

### 8.3 Admisibilidad local frente a completitud global

Se hacen dos afirmaciones independientes sobre una intersección:

- **Admisibilidad local** de una raíz concreta: esa solución está certificada y
  tiene una identidad única resuelta, de modo que puede crearse un punto a partir
  de ella.
- **Completitud global** del resultado: el solucionador estableció que halló
  *todas* las soluciones en la región solicitada.

Son afirmaciones separadas. Un resultado cuya completitud global sea
`NOT_ESTABLISHED` no invalida por ello una raíz localmente admisible. A la
inversa, una raíz tangente, ambigua, caduca o insuficientemente certificada no se
vuelve admisible por estar visible en pantalla.

El inspector rotula estos estados: **Certificado; admisible**, **Solo resultado
enriquecido**, **Ya materializado**, y un aviso de resultado caduco cuando el
resultado subyacente ha cambiado desde que lo consultó.

### 8.4 El inspector

Seleccione el resultado rico en Álgebra y elija **Inspeccionar resultado rico**
(**Construcción → Relaciones e intersecciones**, o el menú contextual). El
inspector lista las soluciones con su evidencia y permite:

- **Crear uno** — materializar la solución resaltada;
- **Crear uno y cerrar**;
- **Crear seleccionados** — materializar un grupo elegido explícitamente
  (`Ctrl` o `Mayús` para seleccionar varios);
- **Crear todos los admisibles** — materializar toda raíz actualmente admisible;
- **Cerrar**.

El inspector permanece disponible para continuar después de crear un punto. Las
soluciones ya materializadas se identifican como tales.

**Mostrar marcadores de soluciones** muestra marcadores transitorios de las
soluciones candidatas. Los marcadores son solo presentación: no son objetos, no
se guardan y no confieren admisibilidad.

### 8.5 El token exacto

Cada solución admisible tiene un **token exacto** opaco. Es la identidad durable
de esa solución a través del recálculo, el guardado y la reapertura. No es una
coordenada, ni un índice, ni una etiqueta.

No necesita manipular tokens a mano cuando usa el inspector: para eso está. La
forma escrita `Intersect(R,"<token>")` existe para trabajo reproducible o
automatizado; no transcriba tokens manualmente.

### 8.6 Materialización

Materializar una solución crea un `GeoPoint` ordinario cuyo padre es esa
intersección y cuya identidad está ligada al token exacto. A partir de ahí es un
miembro normal del grafo de dependencias.

Tres reglas gobiernan su ciclo de vida:

- **El recálculo nunca crea puntos.** Cambiar una entrada recalcula el resultado
  rico; no añade puntos a su construcción en silencio.
- **Perder la admisibilidad deja el punto existente inactivo (dormant)**, no
  eliminado. Queda indefinido mientras su solución no sea actualmente admisible, y
  el inspector de definición informa de su dirección como *retenida/inactiva — no
  actual*.
- **Solo su propio selector lo reactiva.** Cuando la misma solución vuelve a ser
  admisible bajo su selector exacto, se reactiva ese mismo punto. No se inventa
  ningún punto nuevo ni se reasigna ningún punto por proximidad.

**Crear automáticamente puntos admisibles de nuevas consultas explícitas**
(**Opciones → Opciones de GeoCeDG**) es una opción explícita y visible que se
aplica solo a una consulta recién enviada. La consulta y los puntos que crea
forman un único paso compuesto de deshacer. No se ejecuta al seleccionar, reabrir
o recalcular un resultado.

### 8.7 Spline V2 × Spline V2

Un par de splines semánticas consume la misma maquinaria de resultado rico. Varias
raíces distintas pueden ser individualmente admisibles. Una raíz cuya
multiplicidad o identidad no esté resuelta —una tangencia, por ejemplo— permanece
solo como resultado rico.

Esta sección describe el flujo de trabajo y los conceptos necesarios. El
tratamiento matemático completo de la identidad de las intersecciones queda fuera
del alcance de una guía de usuario.

---

<!-- geocedg-guide-section: lengths-and-measurements -->
## 9. Longitudes y medidas

### 9.1 Dos superficies

```text
Length(...)       número ordinario
LocusLength(...)  resultado rico
```

`Length` devuelve un objeto numérico ordinario, utilizable allí donde se espere
un número. `LocusLength` devuelve un **resultado métrico rico** que además porta
el estado del cálculo, el dominio cubierto, la estimación de error y los
diagnósticos. No es una fila numérica ordinaria y no cabe esperar que se comporte
como tal.

Un escalar producido por `Length` sobre una curva semántica es un adaptador sobre
esa misma evidencia rica: su definición visible indica `Length(S,P,Q)`, mientras
que el auxiliar rico sigue siendo su padre real y puede verse en inspección
avanzada.

### 9.2 Longitud total y longitud parcial

```text
Length( <Locus V2> )
Length( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )

LocusLength( <Locus V2> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point>,
             <Direction>, <Boundary Policy>, <Same-position Policy> )
```

La forma total mide la curva completa sobre su dominio declarado. La forma
parcial mide el arco orientado entre dos posiciones semánticas.

Ambas funcionan sobre un Locus V2 y sobre una Spline V2, ya que una Spline V2 es
una curva semántica de la misma familia.

### 9.3 Los extremos necesitan proveniencia semántica

La forma parcial exige que los dos extremos sean **puntos semánticos admisibles
sobre esa curva**. Es la cara práctica de la regla enunciada en la sección 8.1.

En el ejemplo de la sección 7.5, `A` y `C` son puntos constructores: puntos
ordinarios que definen la spline. `P` y `Q` son puntos semánticos creados con una
dirección explícita. Por tanto:

```text
Length(S,P,Q)   definido
Length(S,A,C)   indefinido
```

aunque `A` y `C` estén exactamente sobre la curva. No hay selección por
proximidad ni elección implícita de preimagen, lo que resulta decisivo en cuanto
una curva se autointersecta y una posición tiene más de una preimagen.

Del mismo modo, un punto semántico perteneciente a una curva *distinta* no es un
extremo válido para esta.

### 9.4 Limitación actual: extremos procedentes de una intersección entre splines

`Length(S,P,Q)` puede quedar **indefinido** cuando `S` es una Spline V2 y uno de
los extremos es un punto materializado desde una intersección **Spline V2 ×
Spline V2**.

Qué es esto:

- **no** es un fallo numérico de la spline ni de la intersección;
- el punto de intersección es un punto válido y certificado, y la evidencia de la
  intersección sí conserva, por cada lado, la identidad del lugar geométrico, la
  revisión semántica, la rama, el componente, el parámetro semántico y la
  evidencia de token y vigencia;
- la resolución métrica admite hoy puntos semánticos cuya fuente es `S` y
  ocurrencias únicas de puntos constructores, y todavía no consume la proveniencia
  de intersección que porta ese punto.

Qué hacer:

- utilice como extremo métrico un punto con dirección explícita sobre `S` (la
  forma `Point(S, rama, parámetro)`);
- **no** sustituya por un punto cercano de ningún tipo: la proximidad no es
  posición semántica, y hacerlo produciría una respuesta sin proveniencia.

No se promete ninguna fecha de implementación. Queda registrado como asunto
abierto que requiere caracterización y contrato semántico antes de cualquier
cambio.

---

<!-- geocedg-guide-section: transformations -->
## 10. Transformaciones

### 10.1 Formas admitidas

Las transformaciones de semejanza bidimensionales ordinarias aceptan una curva
semántica como primer argumento:

```text
Translate( L, v )
Rotate( L, angle )
Rotate( L, angle, center )
Reflect( L, point )      / Mirror( L, point )
Reflect( L, line )       / Mirror( L, line )
Dilate( L, factor )
Dilate( L, factor, center )
```

Las mismas herramientas están disponibles en **Construcción → Transformaciones
de semejanza**.

La inversión respecto a circunferencia o cónica, la cizalladura, el estiramiento
no uniforme, las aplicaciones afines o proyectivas arbitrarias y las
transformaciones 3D **no** están admitidas sobre curvas semánticas.

### 10.2 El resultado es una nueva entidad semántica

Transformar una curva semántica produce **otra curva semántica**, con identidad
durable propia y su propio lugar en el grafo de dependencias. No es una imagen
transformada del original ni una copia de sus muestras de dibujo.

La transformación actúa sobre la geometría, no sobre el parámetro semántico: para
una fuente `L` y una transformación `T`, el resultado se evalúa como `T(L(u))` en
el mismo parámetro `u`. La identidad de la fuente nunca se reutiliza, ni siquiera
para una aplicación identidad.

### 10.3 Comportamiento dinámico

El resultado depende de la curva fuente y de todos los datos ordinarios de la
transformación. Edite el vector, el ángulo, el centro o el factor y la curva
transformada los sigue.

```text
O=(0,0)
k=1
T=Dilate(S,k,O)
```

La longitud de `T` se escala por `abs(k)`. La traslación, la rotación y la
reflexión conservan las longitudes.

En `k=0` la imagen colapsa, pero conserva su dominio válido: es una imagen
colapsada, no un punto sin parametrizar, y un clic nuevo no inventa una preimagen
sobre ella. Al restaurar `k`, el núcleo recupera los puntos semánticos existentes.

Las intersecciones de curvas transformadas derivan sus propios selectores y
tokens; no se heredan de la intersección de origen.

### 10.4 Reflexión respecto a un eje

Los ejes coordenados incorporados no tienen identidad persistente de objeto
ordinario, de modo que no pueden seleccionarse como espejo de una reflexión
semántica. Construya la recta explícitamente —por ejemplo `a: y = 0`— y
selecciónela.

---

<!-- geocedg-guide-section: dxf-export -->
## 11. Exportación DXF

### 11.1 Qué es y qué no es la exportación DXF

**Archivo → Importar y exportar → Exportar geometría 2D como DXF
(experimental)…** escribe un dibujo DXF ASCII (AC1015) a partir de geometría 2D
del modelo ya resuelta.

Es un adaptador de interoperabilidad. No es una captura de pantalla, no recorta a
la ventana, no modifica la construcción y nunca es autoridad geométrica. Nada del
archivo exportado realimenta el modelo.

La exportación DXF extendida (la capacidad G9X1) está activada por defecto en
GeoCeDG, con independencia de las curvas semánticas: son dos decisiones
separadas y ninguna implica la otra. El argumento histórico
`--enableExtendedDxf=false` se conserva como override de desactivación.

### 11.2 Correspondencias nativas exactas

Para estas familias la entidad DXF nativa representa el objeto resuelto en
coordenadas del modelo sobre su dominio declarado, sin discretización
intencionada:

| Fuente | Entidad DXF |
|---|---|
| punto | `POINT` |
| segmento | `LINE` |
| semirrecta | `RAY` |
| recta | `XLINE` |
| circunferencia | `CIRCLE` |
| arco de circunferencia | `ARC` |
| elipse / arco elíptico | `ELLIPSE` |
| polígono / poligonal | `LWPOLYLINE` |

«Exacto» significa aquí que no se introdujo ninguna aproximación deliberada. No
es una afirmación de aritmética simbólica.

### 11.3 Geometría aproximada exclusiva de exportación

Las curvas sin entidad DXF exacta aprobada se exportan como geometría
`LWPOLYLINE` acotada, producida por refinamiento diádico determinista en
**coordenadas del modelo**:

| Fuente | Representación |
|---|---|
| parábola / hipérbola | `LWPOLYLINE` acotada sobre un intervalo finito explícito |
| función acotada / curva paramétrica | `LWPOLYLINE` acotada; se exige dominio finito explícito |
| Locus V2 | una `LWPOLYLINE` por rama y componente válido |
| Spline V2 | `LWPOLYLINE` acotada |

La aproximación se deriva de la **geometría semántica**. No se deriva de la caché
de dibujo, del viewport, del nivel de zoom ni de la resolución de pantalla. La
misma construcción y la misma petición producen los mismos bytes.

Los huecos de dominio inválido nunca se puentean, los componentes constructivos
coincidentes permanecen distintos, y la proximidad de extremos nunca establece por
sí sola un cierre periódico.

### 11.4 Spline V2 y la entidad `SPLINE`

```text
DXF SPLINE exact entity      = NOT IMPLEMENTED
SplineV2 DXF representation  = APPROXIMATE LWPOLYLINE under current G9X1
```

No existe entidad `SPLINE` exacta en la línea base de exportación aprobada, y no
se produce ninguna. Una Spline V2 se exporta como geometría poligonal aproximada
con su evidencia registrada. Una futura `SPLINE` racional exacta exigiría una
autorización separada con evidencia de exactitud matemática y de conformidad
independiente de los lectores.

### 11.5 Resultados de fidelidad

La fidelidad se asigna por componente fuente, no una vez por archivo:

| Resultado | Significado |
|---|---|
| `EXACT` | entidad nativa, sin discretización intencionada |
| `APPROXIMATE` | discretización intencionada con método, tolerancia, garantía y evidencia de trabajo |
| `UNSUPPORTED` | fuente válida, sin correspondencia aprobada para su familia o dominio |
| `INVALID` | indefinida, no finita, caduca, con dominio mal formado, fuera de presupuesto o incapaz de satisfacer la evidencia solicitada |

Los acompañan códigos de motivo, entre ellos `MISSING_DOMAIN`, `NON_FINITE`,
`DISCONTINUITY_UNRESOLVED`, `TOLERANCE_NOT_ESTABLISHED`, `WORK_LIMIT`,
`STALE_SOURCE_REVISION` y `UNSUPPORTED_FAMILY`. Ninguna omisión es silenciosa.

### 11.6 Garantía: `ESTIMATED_ERROR`

La evidencia de aproximación lleva un nivel de garantía. El nivel utilizado por
la exportación actual es:

```text
ESTIMATED_ERROR
```

Es evidencia determinista de muestreo y derivadas, no una demostración. **No** es
una cota de error global certificada, y esta guía no la describe como tal. Un
`CERTIFIED_ERROR_BOUND` exigiría un contrato de demostración por intervalos,
curvatura o envolvente que la línea base actual no proporciona.

### 11.7 Preflight y diálogo de exportación

Antes de escribir nada, la exportación ejecuta un **preflight** y le muestra la
petición y sus consecuencias. El diálogo presenta actualmente sus rótulos en
inglés con independencia del idioma de producto.

| Campo | Significado |
|---|---|
| Objects | `Complete 2D geometric construction` o `Current selection` |
| Model-coordinate tolerance | tolerancia solicitada, por defecto `0.001` |
| Approximation | si se permite la aproximación aprobada de curvas tipadas |
| Closed domains | dominios finitos explícitos, como `inicio:fin` o `fuente@rama:inicio:fin`, separados por `;` |
| Allowed evidence | `ESTIMATED_ERROR` |
| Maximum evaluations / dyadic depth / vertices per component / total vertices | límites deterministas de trabajo |
| Coordinates / units | `Cartesian 2D world / UNITLESS` |
| Partial output | `Disabled (strict complete request)` |
| Sidecar | solicitar manifiesto incluso en una exportación totalmente exacta |

El informe de preflight enumera después los recuentos de componentes exactos,
aproximados, no soportados, inválidos y omitidos antes de que elija destino.

En una petición de construcción completa, la entrada es la población geométrica
tipada. Las listas, los parámetros numéricos, los resultados ricos de
intersección y el Texto no tienen correspondencia DXF aprobada y se notifican como
ajenos a esa población antes del preflight. Eso no es salida parcial. Una
selección explícita vigente nunca se filtra por esta regla.

### 11.8 Comportamiento estricto por defecto

La salida parcial por componentes está **desactivada** por defecto: una petición
que no pueda satisfacerse íntegramente se rechaza en lugar de truncarse en
silencio. Los objetos ocultos se incluyen y se notifican de forma visible.

### 11.9 Manifiesto acompañante

Se escribe un `<dibujo>.dxf.manifest.json` determinista en UTF-8 siempre que la
exportación contenga geometría aproximada, geometría omitida o parcial, un
componente solicitado no soportado, o una terminación por límite de trabajo. Una
exportación completamente exacta puede omitirlo, y usted puede solicitarlo
explícitamente.

El manifiesto registra el esquema y la proveniencia de la compilación, el SHA-256
del DXF, la política completa de la petición y sus límites de trabajo, y por cada
componente: el identificador de fuente y su alcance, la clave de rama, el
componente, el intervalo semántico, el handle y el tipo de entidad DXF, la
fidelidad, el método de aproximación, la tolerancia solicitada, la estimación
alcanzada, la garantía, el número de evaluaciones, el número de vértices y la
profundidad, además de cada aviso estructurado y cada omisión.

DXF y manifiesto se escriben en archivos temporales del mismo directorio, se
validan y se promueven juntos bajo una política de reversión definida.

### 11.10 Independencia del viewport

Las coordenadas son coordenadas cartesianas del modelo, sin unidades. Ni la
exportación ni su fidelidad dependen del zoom, el desplazamiento, el tamaño de la
ventana, los DPI o la vista vigente. La misma revisión de construcción y la misma
petición producen siempre la misma salida.

### 11.11 Frontera actual de exportación

No hay importación DXF, exportación de viewport, contrato de unidades físicas,
exportación de texto, exportación del `Locus` muestreado heredado, contorneado de
curvas implícitas, `SPLINE` exacta ni exportación 3D. El grosor y estilo de
línea, el relleno, la opacidad, el tamaño de punto y las etiquetas no se
transportan; la capa, el color RGB y la visibilidad vigente sí.

---

<!-- geocedg-guide-section: presentation-and-visualization -->
## 12. Presentación y visualización

Todo lo de esta sección es presentación. Nada de ello cambia la geometría, la
identidad, las medidas, el contenido del documento ni la salida de exportación.

### 12.1 Temas y lienzo

Los tres temas de presentación se describen en la sección 2.5. Un tema fija los
colores del marco, de la superficie de menú y barra, de los paneles y de los
separadores, así como el fondo del lienzo de Gráficos. Un fondo elegido
explícitamente por usted para una vista conserva su significado.

### 12.2 Fuentes y tamaño de iconos

Los seis tamaños de presentación independientes se describen en la sección 2.6:
fuente de la interfaz general, fuente de menús, tamaño de iconos de la barra,
fuente de Álgebra, fuente del Protocolo de Construcción y fuente de Gráficos. El
**Tamaño de fuente de Texto** de construcción es aparte y pertenece al documento.

### 12.3 Estilo de los objetos

El color, el estilo de línea, el estilo de punto, la visibilidad y el etiquetado
de los objetos individuales son propiedades ordinarias del objeto, accesibles
desde **Propiedades** o **Vista → Visibilidad y estilo**. **Copiar estilo
visual** los transfiere entre objetos.

### 12.4 Zoom y navegación

**Vista → Navegación** ofrece desplazamiento, el acercar y alejar heredados, la
vista estándar, mostrar todos los objetos y las acciones GeoCeDG descritas a
continuación. Las mismas acciones están en el desplegable Navegación de la barra.

**Zoom por rectángulo** espera un pulsar-arrastrar-soltar ordinario en el lienzo
y amplía al rectángulo trazado. Conserva la relación de escala X/Y vigente y
`Escape` cancela el gesto.

**Vista → Configurar zoom de navegación…** fija un factor de zoom (10 por
defecto) y atajos independientes, inicialmente sin asignar, para **Acercar por
factor** y **Alejar por factor**. Multiplican o dividen ambas escalas de vista en
torno al cursor vigente, o al centro real de la vista cuando no hay cursor
vigente. Mientras edita un atajo, el diálogo informa de inmediato de
*Disponible*, *Sin asignar (válido)*, *No válido* o de la acción con la que
colisiona; **Aplicar** permanece desactivado hasta que todo el borrador es
válido, y **Cancelar** conserva la configuración anterior.

Desde el teclado, haga clic en una zona vacía de la vista Gráfica para darle el
foco y use `Ctrl`+`+` y `Ctrl`+`-`. No los escriba en la Entrada algebraica.

Ninguna operación de zoom o navegación cambia medidas, coordenadas, identidad ni
proveniencia.

---

<!-- geocedg-guide-section: user-tools-and-automation -->
## 13. Herramientas de usuario y automatización

### 13.1 Herramientas de usuario persistentes

**Automatización → Herramientas de usuario → Gestionar herramientas de
usuario…** administra una biblioteca de paquetes de herramientas `.ggt` que
pertenece al **perfil GeoCeDG**, no al documento abierto.

Para instalar una:

1. Abra el gestor.
2. Pulse **Instalar .ggt…**, seleccione el paquete y revise el nombre y cualquier
   rechazo.
3. Elija la herramienta en el menú para activarla. Activarla registra la
   definición en el documento; después seleccione sus datos para producir los
   resultados.
4. Opcionalmente fíjela en la barra, ordénela, asígnele un grupo de barra y
   dele un icono PNG. Sin icono, la barra muestra una inicial compacta y conserva
   el nombre completo en la ayuda emergente y en la información de accesibilidad.

La instalación sobrevive a cerrar el documento, abrir otro y reiniciar. Eliminar
la instalación no borra los resultados ya construidos.

### 13.2 Herramienta, documento y biblioteca

Son tres ámbitos distintos, y mantenerlos separados evita casi toda confusión:

- la **biblioteca** es una preferencia de aplicación del perfil GeoCeDG;
- una **herramienta del documento** (una macro embebida) pertenece al `.cedg` y
  reconstruye sus resultados desde el documento;
- **invocar** una herramienta instalada copia la definición necesaria al
  documento, de modo que una construcción sigue siendo portable después de
  eliminar la entrada de la biblioteca.

Cuando un documento reabierto contiene una macro embebida equivalente a una
instalada, GeoCeDG compara el conjunto completo de definiciones y sus digests. La
entrada instalada sigue siendo la única elección visible y habilitada, mientras
que la macro embebida continúa perteneciendo al documento y reconstruyendo sus
resultados. Nunca se elimina ni se sustituye. Si una herramienta instalada ofrece
el mismo nombre de comando con una definición *distinta*, su entrada instalada
queda deshabilitada solo en ese documento. Ninguna asociación se decide por
nombre, orden de carga o posición en la barra.

Sin paquete instalado, **Herramientas del documento (solo locales)…** permite de
todos modos gestionar la definición portable.

### 13.3 Límites

Una herramienta de usuario no puede sobrescribir un comando nativo como `Point`,
`Length`, `LocusV2`, `SplineV2` o `Intersect`. Las herramientas que necesitan
guiones, objetos semánticos u operaciones no planas no admitidos se rechazan con
una explicación; no eluden la política del producto. Los paquetes que exceden el
tamaño admitido y los archivos inválidos o parciales fallan de forma cerrada.

Con varias ventanas abiertas, el gestor vuelve a comprobar la biblioteca antes de
modificarla y le pide reintentar si otra ventana la está actualizando.

### 13.4 Guiones

**Automatización → Propiedades y guiones del objeto…** abre la pestaña Guiones
del diálogo de Propiedades existente. GeoGebraScript actúa mediante comandos, no
mediante gestos de ratón sintéticos.

---

<!-- geocedg-guide-section: classic-compatibility-and-diagnostics -->
## 14. Compatibilidad Classic y herramientas de diagnóstico

### 14.1 Para qué sirve la sesión de diagnóstico Classic

**Archivo → Abrir sesión de diagnóstico Classic** lanza una aplicación Classic
heredada en un **proceso separado con preferencias aisladas**. Existe para que
pueda comparar comportamientos con la línea base heredada, abrir un documento
heredado con herramientas heredadas y usar facilidades heredadas que el perfil de
producto GeoCeDG no expone deliberadamente —de forma señalada, **Crear
herramienta** para redactar un `.ggt` desde cero.

### 14.2 Qué conserva y qué no

La sesión Classic conserva sus propias herramientas, su barra y su configuración
upstream, incluida la selección de idioma upstream. La continuidad sigue siendo
allí configurable por separado, mientras que el perfil de producto GeoCeDG la
bloquea **desactivada** para que las selecciones semánticas sean deterministas y
no una búsqueda de la posición más próxima a la anterior.

Classic **no puede crear** objetos Locus V2 ni Spline V2. La activación por
defecto de la superficie semántica se aplica únicamente al perfil de producto
GeoCeDG y no puede filtrarse a Classic, se usen los argumentos de arranque que se
usen.

### 14.3 Recursos heredados de diagnóstico

**Automatización → Laboratorio heredado (sesión separada)** abre un archivo
elegido explícitamente por usted en un proceso Classic de diagnóstico separado.
Ese diálogo **no** verifica el archivo contra el catálogo heredado canónico y no
lo promueve como autoridad científica. Para recursos canónicos registrados y
verificados por hash, use el script del repositorio
`tools/legacy/open-laboratory.ps1`.

No se instala ninguna barra heredada en el espacio de trabajo Construcción.

Son rutas de diagnóstico para un usuario técnico, no funciones normales del
producto. Las macros heredadas pueden tener rangos de validez, degeneraciones,
limitaciones dinámicas y aproximaciones numéricas muestreadas no documentados.

---

<!-- geocedg-guide-section: known-limitations -->
## 15. Limitaciones conocidas

Estas son las limitaciones que afectan a lo que hoy puede hacer en la aplicación.

**Curvas semánticas y medidas**

- Locus V2 y Spline V2 conservan la madurez `experimental` aunque estén activadas
  por defecto. «Activado por defecto» es exposición, no estabilidad: su contrato
  público todavía puede refinarse.
- Locus V2 no es un `Path` genérico. Admite los generadores, dominios explícitos
  y objetivos documentados.
- Las curvas semánticas son exclusivas de GeoCeDG. Se guardan en el documento
  nativo, y una aplicación upstream externa queda fuera de la garantía de
  compatibilidad.
- `Length(S,P,Q)` puede quedar indefinido cuando un extremo es un punto
  materializado desde una intersección Spline V2 × Spline V2. Véase la sección
  9.4.
- Una raíz de intersección tangente, ambigua, caduca o insuficientemente
  certificada permanece solo como resultado rico y no puede materializarse.

**Exportación**

- La `SPLINE` DXF exacta no está implementada; Spline V2 se exporta como
  `LWPOLYLINE` aproximada.
- La evidencia de aproximación es `ESTIMATED_ERROR`, no una cota de error global
  certificada.
- No hay importación DXF, exportación de viewport, contrato de unidades físicas,
  exportación de texto, exportación del `Locus` heredado, contorneado de curvas
  implícitas ni exportación 3D.

**No disponible**

- La semántica espacial —identidad de objetos tridimensionales, marcos de
  proyección y certificados de proyección canónica— no está disponible en el
  producto. El espacio **Procedimientos diédricos CeDG** es visible pero no está
  autorizado.
- No existe DSL CeDG, workbench, servicio de estudios ni capa de optimización.

**Plataforma y estado del producto**

- Windows es la única plataforma de trabajo validada.
- La identidad de marca es textual y provisional: no hay logotipo, juego de
  iconos ni arte de instalador definitivos de GeoCeDG, y el entorno de ejecución
  sigue usando cadenas y recursos de interfaz heredados.
- Los archivos `.cedg` y los `.ggb` compatibles conservan internamente el código
  de aplicación heredado `classic`; no hay un formato de archivo nuevo.
- El diálogo de exportación DXF se presenta en inglés con independencia del
  idioma de producto.

---

<!-- geocedg-guide-section: command-and-workflow-reference -->
## 16. Referencia de comandos y flujos

### 16.1 Tabla de referencia

| Tarea | Ruta GUI | Comando | Tipo de resultado | Notas importantes |
|---|---|---|---|---|
| Crear un lugar geométrico semántico | Construcción → Curvas semánticas → Locus V2 | `LocusV2(G,u,dom)` | curva semántica | el dominio es explícito; no procede del rango de un deslizador |
| Crear una spline semántica | Construcción → Curvas semánticas → Spline V2 | `SplineV2({A,B,C,D},3)` | curva semántica | los puntos constructores no son puntos semánticos |
| Punto sobre curva semántica, interactivo | Herramienta Punto, clic en el trazo | — | punto semántico | preimagen única, o elección explícita; nunca por proximidad |
| Punto sobre curva semántica, por parámetro | Construcción → Curvas semánticas → Punto sobre curva semántica | `Point(S,"spline-v2/main",0.25)` | punto semántico | clave de rama y parámetro canónico; el parámetro puede ser un número con nombre |
| Punto sobre un Locus V2, por parámetro | ídem | `Point(L,"generator.main",1)` | punto semántico | clave de rama por defecto del generador |
| Longitud total | Construcción → Métricas y validación | `Length(S)` | número | adaptador escalar sobre el resultado métrico rico |
| Longitud parcial | Construcción → Métricas y validación | `Length(S,P,Q)` | número | ambos extremos necesitan proveniencia semántica admisible |
| Evidencia métrica rica | Construcción → Métricas y validación | `LocusLength(S)` / `LocusLength(S,P,Q)` | resultado rico | estado, cobertura, estimación de error, diagnósticos |
| Intersecar curva semántica y objeto | Construcción → Relaciones e intersecciones → Intersección | `Intersect(S,c)` | resultado rico de intersección | no es una lista de puntos |
| Intersecar dos curvas semánticas | ídem | `Intersect(S,T)` | resultado rico de intersección | varias raíces pueden ser admisibles cada una |
| Materializar soluciones | Inspeccionar resultado rico → Crear uno / Crear seleccionados / Crear todos los admisibles | `Intersect(R,"<token>")` | punto | recalcular nunca crea puntos; no transcriba tokens |
| Trasladar | Construcción → Transformaciones de semejanza | `Translate(S,v)` | nueva curva semántica | conserva las longitudes |
| Rotar | ídem | `Rotate(S,angle,center)` | nueva curva semántica | conserva las longitudes |
| Reflejar | ídem | `Reflect(S,line)` / `Mirror(S,line)` | nueva curva semántica | construya la recta espejo explícitamente; los ejes no son seleccionables |
| Homotecia | ídem | `Dilate(S,k,O)` | nueva curva semántica | la longitud se escala por `abs(k)`; en `k=0` colapsa pero conserva su dominio |
| Exportar DXF | Archivo → Importar y exportar → Exportar geometría 2D como DXF (experimental)… | — | archivo DXF (+ manifiesto) | preflight primero; petición completa estricta por defecto |
| Abrir un documento | Archivo → Abrir… | — | — | `.cedg` nativo, `.ggb` entrada de compatibilidad |
| Guardar un documento | Archivo → Guardar / Guardar como… | — | — | guarde el trabajo nativo como `.cedg` |
| Inspeccionar una definición | menú contextual → Inspeccionar definición… | — | — | solo lectura; no cambia el modo de presentación de Álgebra |
| Inspeccionar la estructura semántica | Construcción → Curvas semánticas → Inspeccionar definición de curva semántica… | — | — | rama, componente, dirección, estado, parámetro |

### 16.2 Resumen de sintaxis

```text
LocusV2( <Dependent Point>, <Constrained Point> )
LocusV2( <Dependent Point>, <Scalar State>, <Domain Descriptor> )
LocusV2( <Dependent Point>, <Scalar State>, <True Driver>, <Domain Descriptor> )

SplineV2( <List of Points> )
SplineV2( <List of Points>, <Degree> )
SplineV2( <List of Points>, <Degree>, <Weight Function> )
SplineV2( <Point>, <Point>, <Point>, ... )

Point( <Locus V2>, <Branch Key>, <Canonical Parameter> )

Length( <Locus V2> )
Length( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )

LocusLength( <Locus V2> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point>,
             <Direction>, <Boundary Policy>, <Same-position Policy> )

Intersect( <Locus V2>, <Supported Object> )
Intersect( <Locus V2>, <Locus V2> )
Intersect( <Rich Intersection Result>, <Solution Token> )
```

Una Spline V2 es una curva semántica de la familia Locus V2, por lo que las
formas escritas arriba para `<Locus V2>` la aceptan.

### 16.3 Nombres de comando e idioma

Los identificadores de comando no se traducen en esta guía, y las formas inglesas
mostradas arriba funcionan en ambos idiomas de producto. Con el español
seleccionado, la ayuda de entrada ofrece además nombres localizados:
`LugarGeométricoV2` para `LocusV2`, `LongitudLugarGeométrico` para `LocusLength`,
`Longitud` para `Length`, `Interseca` para `Intersect`, `Punto` para `Point`,
`Refleja` para `Reflect`/`Mirror`; `SplineV2` conserva su nombre. Las claves de
rama como `"generator.main"` y `"spline-v2/main"`, los tokens exactos, las
extensiones de archivo y los literales de código son identificadores y no se
traducen nunca.

### 16.4 Teclado

| Tecla | Acción |
|---|---|
| `Intro` | confirmar la transacción de Entrada algebraica |
| `Escape` | cancelar la entrada, salir de una herramienta, cancelar un gesto de rectángulo |
| `Ctrl`+`Z` / `Ctrl`+`Y` | deshacer / rehacer |
| `Ctrl`+`+` / `Ctrl`+`-` | acercar / alejar, con la vista Gráfica enfocada |
| `F2` | editar el objeto seleccionado |
| `F1` | ayuda de la herramienta |
| `Tab` | recorrer los controles |

---

*GeoCeDG 1.0.0 — Manuel Prado-Velasco, Universidad de Sevilla. Los créditos y
licencias upstream obligatorios se conservan en `LICENSE`, `NOTICE.md` y
`THIRD_PARTY.md`.*
