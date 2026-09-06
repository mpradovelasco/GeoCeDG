# GeoCeDG Construction — guía rápida del candidato en revisión

Esta guía acompaña el sucesor técnico de presentación final posterior a Round 3.
**No es una declaración de PASS.** Use una construcción nueva o una copia de
trabajo; conserve intacto `TestBasic1.cedg` como evidencia del fallo histórico.
`Revision2.cedg` y `Revision3.cedg` también son entradas autorales de diagnóstico:
ábralas sólo para revisión y guarde cualquier cambio con otro nombre.
El [checklist](../validation/g9u1_author_resmoke_checklist.md) permite anotar el
resultado de una sesión completa.

## 1. Inicio y diferencia respecto a Classic

Desde la raíz del repositorio preparado, en PowerShell:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG --args="--enableLocusV2=true"
```

El argumento habilita la creación experimental de Locus V2/Spline V2 en este
proceso. Omitirlo no habilita esos comandos. Construction es el perfil CeDG;
Classic diagnóstico conserva sus herramientas y configuración upstream.
GeoCeDG mantiene **Continuidad desactivada**: las selecciones semánticas son
deterministas, no una búsqueda del punto más cercano a su posición anterior.
El título y **Ayuda → Acerca de GeoCeDG** identifican la autoridad central
`GeoCeDG 0.9`, el baseline GeoGebra y la autoría; el icono de ventana y la
pantalla de inicio usan los recursos GeoCeDG versionados aprobados. El splash
actual es más compacto y el arranque GeoCeDG solicita traerlo al primer plano;
esta política no se aplica al lanzador Classic.

## 2. Menú completo, barra de uso frecuente

El menú superior normal contiene **Archivo, Editar, Ver, Construcción, Opciones,
Automatización y Ayuda**. Los submenús agrupan las herramientas del catálogo.
Opciones ofrece sólo ajustes host declarados por el perfil: Continuidad permanece
desactivada en Construction y el idioma de producto se limita a inglés/español.
Una misma acción referenciada desde varios grupos aparece una sola vez.
Archivo y Editar muestran directamente sus acciones, separadas por función. En
Construcción encontrará **Rectas y vectores**, **Polígonos**, **Construcciones
derivadas**, **Círculos y cónicas**, **Curvas semánticas** y **Anotaciones y
medios**; Texto e Imagen no son vistas. La barra proyecta 52 acciones habituales
en 11 grupos, sin duplicarlas: Move incluye Move/Rotate; Punto/Intersección reúne
Punto, Punto sobre objeto, Limitar/Liberar, Intersección y Tangente; Rectas y
vectores, Polígonos y Construcciones derivadas conservan tres flyouts separados;
Parámetros incluye Deslizador, Ángulo fijo, Casilla, Botón y Campo de entrada.
Los flyouts mixtos **Curvas semánticas** y **Navegación** consumen las mismas
acciones del catálogo. El primero muestra `Locus V2`, `Spline V2` y Punto sobre
curva semántica; el segundo reúne Pan, Zoom por ventana, Acercar, Alejar y Copiar
estilo visual. Las herramientas menos frecuentes siguen en el menú y **Ayuda de
entrada** permanece en el extremo derecho. Una opción deshabilitada muestra una
razón, no crea objetos.
Un archivo puede conservar su distribución visual; **Ver → Reaplicar espacio de
trabajo → Construction** recupera la organización del producto sin reconstruir
la geometría.

| Quiero… | Ruta Classic | Ruta GeoCeDG / menú | Barra | Comando o nota |
| --- | --- | --- | --- | --- |
| Crear un punto libre o una recta | Punto / Recta | Construcción → Puntos / Rectas y vectores | Grupos Punto y Recta | `A=(0,0)`, `Line(A,B)` |
| Crear un parámetro | Deslizador / Entrada | Construcción → parámetros | Deslizador | `k=1`; editar su fila o deslizador |
| Crear un locus semántico | Locus clásico muestreado, distinto | Construcción → Curvas semánticas | Curvas semánticas | Dominio explícito; ejemplo abajo |
| Crear una spline semántica | Spline clásico, distinto | Construcción → Curvas semánticas | Curvas semánticas | `S=SplineV2({A,B,C,D},3)` |
| Colocar y arrastrar un punto sobre la curva | Punto sobre objeto | Punto y clic sobre el **trazo** semántico | Punto | Resolver semánticamente; escoger si hay ambigüedad |
| Crear un punto con parámetro conocido | Entrada | Construcción → Curvas semánticas → Punto sobre curva semántica / Entrada | Menú o Entrada | `P=Point(S,"spline-v2/main",0.25)` |
| Intersectar curvas | Intersección | Construcción → intersecciones | Intersección | `R=Intersect(S,g)` produce resultado rico |
| Materializar soluciones | Puntos de intersección | Inspector del resultado; Construcción → materialización | Menú/inspector | Una, varias seleccionadas o todas las admisibles |
| Medir longitud | Longitud | Construcción → medición | Grupo medición | `Length(S)` o `Length(S,P,Q)` |
| Transformar | Traslada / Rota / Refleja / Homotecia | Construcción → transformaciones | Grupos de transformación | `Translate`, `Rotate`, `Reflect`, `Dilate` |
| Inspeccionar definición | Descripción / Propiedades | Contexto → definición; Propiedades | Contexto | Inspección no implica permiso de redefinición |
| Ver dependencias en secuencia | Protocolo de construcción | Ver → Protocolo / Mostrar barra de navegación de la construcción | Menú | La barra se asocia a la Vista Gráfica y no crea undo |
| Ampliar un rectángulo | Zoom / navegación | Ver → navegación → Zoom por ventana | Zoom por ventana | Arrastrar rectángulo; Escape cancela |
| Zoom desde teclado | Atajos de vista | Foco en Vista Gráfica | No necesario | `Ctrl`+`+` y `Ctrl`+`-`; no escribirlos en Entrada |
| Guardar / abrir | `.ggb` | Archivo → Guardar / Abrir | Menú | Trabajo nativo `.cedg`; compatibilidad `.ggb` |
| Instalar una herramienta propia | Administrar herramientas / `.ggt` | Automatización → Herramientas de usuario | Grupo propio opcional | Instalación explícita, separada del documento |
| Ver ayuda / sintaxis | Ayuda / autocompletar | Ayuda; Entrada algebraica | Menú | No se crea nada hasta Intro |
| Usar scripts | Propiedades → guiones | Automatización / Propiedades | Menú | GGBScript usa comandos, no ratones sintéticos |

Los rótulos completos EN/ES y la ayuda contextual del candidato son la referencia
de navegación; la tabla no añade comandos ni permisos.

## 3. Spline, puntos semánticos y longitudes

Introduzca cada línea y pulse Intro una sola vez. En una construcción nueva:

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

Se espera **M=4 y MP=2**. Editar h traslada la spline sin cambiar esas longitudes.
Los parámetros explícitos de P/Q pueden ser números con nombre para editarlos.
También puede crear otros puntos usando **Punto → clic en el trazo** y arrastrarlos
con Mover. Esos puntos son interactivos; los puntos creados con `Point(...,u)`
se controlan mediante su parámetro explícito, no por un drag que lo sustituya.

**A/C no son P/Q.** A y C son nodos de interpolación, puntos libres o dependientes
ordinarios que definen la spline. No contienen automáticamente una dirección
semántica sobre S. `Length(S,A,C)` debe permanecer indefinido bajo el contrato
actual, aunque las coordenadas coincidan con la curva. No hay selección por
proximidad ni elección implícita de preimagen en una autointersección.

`Length(...)` devuelve un número ordinario. `LocusLength(...)` conserva estado,
cobertura, error y diagnósticos en un resultado rico: no espere una fila numérica
ordinaria. La definición visible de un escalar explica `Length(S,P,Q)`; el
auxiliar rico sigue siendo su padre real y puede verse en inspección avanzada.

## 4. Locus V2 ordinario

En otro documento o usando nombres aún libres:

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

Se espera **LL=4 y LP=2**. El dominio no procede de los límites visuales del
deslizador ni de la polilínea dibujada. La herramienta Locus V2 ofrece los
diálogos/selección de generador y dominio; no sustituye al Locus clásico.

## 5. Intersecciones, inspector y materialización

Con la spline recta S anterior:

```text
c=Circle((0,0),1)
R=Intersect(S,c)
```

Seleccione R en Álgebra y abra el inspector de resultado. Inspeccione las dos
soluciones, cree una o seleccione varias y materialícelas; el inspector permanece
abierto para continuar. Las ya materializadas se distinguen. No copie tokens.
Los marcadores son presentación transitoria, no objetos guardados independientes.

Spline V2 × Spline V2 también consume el resultado rico ya calculado: sólo las
raíces con certificado local e identidad admisible permiten crear puntos. Varias
raíces distintas pueden ser individualmente admisibles; una raíz tangente o con
multiplicidad/identidad sin resolver no se vuelve admisible por estar dibujada.
Completitud global `NOT_ESTABLISHED` no invalida por sí sola una raíz local válida.

En movimiento regular se conserva el punto/token. Si deja de ser admisible puede
quedar indefinido; sólo la recurrencia del mismo selector permite reactivarlo.
La opción de creación automática es explícita y visible para nuevas consultas;
recomputar nunca crea nuevos puntos por sí mismo.

## 6. Transformaciones y casos especiales

Ejemplos con S, un origen O y un factor k:

```text
O=(0,0)
k=1
T=Dilate(S,k,O)
```

Trasladar, rotar, reflejar y dilatar producen **otra curva semántica** con sus
dependencias. Edite k mediante su deslizador o la fila numérica ordinaria:
doble clic, F2 o edición directa de fila, escriba el nuevo valor y confirme con
Intro. En la Entrada libre puede confirmar `k=0.25`. Edición de fila, doble
clic, F2 y Entrada libre conservan el mismo `GeoNumeric` mediante la transacción
compatible G9A; la etiqueta localiza la edición explícita, pero no se convierte
en identidad durable.
La longitud de T se escala por `abs(k)`. En k=0 la imagen colapsa pero conserva
su dominio válido; no es un punto sin parametrización. Un clic nuevo no debe
inventar una preimagen. Restaurar k recupera los puntos semánticos existentes
según el kernel. Rotación/reflexión/traslación conservan las longitudes.

En curvas cerradas, arrastrar un punto interactivo a través de la costura conserva
el mismo punto cuando la continuación es única. En una autointersección elija
explícitamente la preimagen ofrecida; cancelar no crea nada.

## 7. Álgebra, Propiedades y navegación

Los objetos ordinarios mantienen su edición habitual. Locus/Spline, curvas
transformadas y resultados ricos muestran definición **sólo lectura** cuando
la edición arbitraria no es admisible; Propiedades lo explica. Modifique sus
inputs ordinarios. Inspeccionar no cambia identidad ni el modo global de
descripción de Álgebra. El menú de descripción indica el modo actual.

La Entrada muestra previsualización sin crear objetos: Intro confirma una
transacción, Escape cancela. Una redefinición incompatible no promete conservar
identidad. El Protocolo de construcción ayuda a revisar el procedimiento.

**Ver → Vistas** reutiliza las vistas host compatibles (Álgebra, Gráficos 2,
Hoja de cálculo, CAS y Vista de propiedades); 3D no se habilita en este
candidato. **Mostrar barra de navegación de la construcción** muestra u oculta
el control de la Vista Gráfica y refleja su estado, sin crear objetos ni undo.

En **Opciones**, Presentación de Álgebra contiene Valor/Descripción/Definición
como radio; Ordenar por, Redondeo, Etiquetado, Tamaño de fuente y Guardar
configuración usan los ajustes host existentes. **Preferencias…** abre la
configuración global sin seleccionar por sorpresa el primer objeto. Propiedades
de un objeto sigue siendo una acción contextual sobre selección explícita.

Para zoom con teclado, haga clic en un espacio vacío de la Vista Gráfica y use
`Ctrl`+`+` / `Ctrl`+`-`; el teclado numérico también sirve. En configuración
española el foco de la vista importa. Escape sale de herramientas; Zoom por
ventana está tanto en Ver/navegación como en la barra. El zoom no cambia métricas.

## 8. Herramientas propias persistentes y Laboratory

1. Para crear una herramienta nueva desde cero, use **Crear herramienta** del
   Classic diagnóstico y exporte la herramienta planar propia a `.ggt`. Para
   exportar una ya presente en el documento, abra el gestor GeoCeDG del paso 2,
   pulse **Herramientas del documento (solo locales)…**, seleccione la herramienta
   en el administrador heredado y guárdela como `.ggt`.
2. Abra **Automatización → Herramientas de usuario → Gestionar herramientas de
   usuario…**.
3. Pulse **Instalar .ggt…** y elija el archivo; revise el nombre y cualquier rechazo.
4. Elija la herramienta en el menú para activarla: registra la definición en el
   documento; después seleccione sus inputs para crear los resultados.
5. Marque/desmarque su fijación a la barra desde el administrador. Puede ordenar
   las herramientas fijadas y asignarles un grupo; varias herramientas del mismo
   grupo aparecen en un desplegable de barra, sin convertirse en acciones de
   producto permanentes. Un grupo se mantiene como una unidad visual contigua:
   sus miembros se ordenan dentro del desplegable y al cruzar otro elemento se
   desplaza el grupo completo. Al fijar o editar la presentación puede elegir
    opcionalmente un PNG propio. El gestor limita tamaño/dimensiones, muestra el
    recurso normalizado sin recortar y lo guarda sólo como preferencia de la
    aplicación; quitar el icono, desfijar o desinstalar limpia esa copia. Sin
    icono, la barra muestra una inicial compacta y conserva el nombre completo
    en la ayuda emergente y la información de accesibilidad.
6. Cierre el documento, abra uno nuevo o reinicie: la instalación explícita sigue
   disponible. Eliminar la instalación no borra resultados ya construidos.

Si un documento reabierto contiene la macro embebida equivalente a la instalada,
GeoCeDG comprueba el conjunto completo de definiciones y sus digests: la entrada
instalada sigue siendo la única elección visible y habilitada, pero la macro
embebida continúa perteneciendo al documento y reconstruyendo sus resultados.
No se elimina ni se sustituye. Si el paquete es parcial o su definición difiere,
se muestra una colisión/desajuste y no se elige por nombre. Sin paquete instalado,
**Herramientas del documento (solo locales)…** permite gestionar la definición
portable. El gestor persistente importa paquetes; no es un nuevo editor de macros.
Con varias ventanas, el gestor vuelve a comprobar la biblioteca antes de cambiarla;
si otra ventana está actualizándola, reintente después del aviso. Abrir el menú
actualiza la lista de herramientas instaladas.

La biblioteca pertenece al perfil GeoCeDG, no al `.cedg` abierto ni al catálogo
de 110 acciones. Las macros de un documento no se instalan automáticamente.
No pueden sobrescribir `Point`, `Length`, `LocusV2`, `SplineV2`, `Intersect` u otro
comando nativo. Scripts/cuerpos semánticos o procedimientos no admitidos se rechazan
con una explicación; no se eluden flags de producto.

Al invocar una herramienta, su definición necesaria pasa a ser autoridad local
del documento. Guarde, elimine después la instalación de la biblioteca y reabra:
el resultado debe reconstruirse desde el `.cedg`. La biblioteca instalada no es
un requisito externo de portabilidad para objetos ya creados.

Templatev7 es evidencia histórica, no un paquete instalado por defecto. Sus 24
herramientas están [clasificadas](../validation/g9u1_user_tools_review.md).
Los antiguos `postLocus`, `listLength` y `listLength12` no sustituyen Locus V2 y
Length. No se habilitan G9U2 ni reconstrucción espacial mediante macros.

## 9. Guardado y revisión pendiente

Guarde trabajo nativo como `.cedg`; `.ggb` es compatibilidad, no promesa de que
upstream entienda objetos semánticos GeoCeDG. Compruebe Deshacer/Rehacer, guardar
tras Deshacer, cerrar y reabrir dos veces. Un fallo de prevalidación de guardado
debe conservar el archivo anterior y dejar los cambios sin guardar.

El `TestBasic1.cedg` original contiene una ficha de dependencias incoherente y
permanece rechazado; no se ha reparado silenciosamente. La estabilización actúa
en la operación productora y en la barrera de guardado. Una recuperación del
archivo histórico requeriría una actuación separada y explícita.

Persisten las limitaciones de certificación/monodromía, la edición semántica no
autorizada y los bloqueos de G9U2/G9B/G9C/G10. El riesgo
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` conserva su disposición canónica;
este re-smoke no lo cierra por inferencia. Las fuentes autorales
`helixTopBar.png` y `helixSnapshot.png` se han promovido byte-exactas a recursos
versionados; los iconos/splash derivados son deterministas y no dependen del
directorio ignorado de ingestión. Su autorización interna no resuelve por sí
sola el gate independiente de redistribución pública.
