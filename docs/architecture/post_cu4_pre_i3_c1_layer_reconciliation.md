# POST-CU4 PRE-I3 · Reconciliación de capas C1

Investigación de interoperabilidad Desktop y corrección de fixtures y arnés.
El candidato I2 congelado para el diagnóstico es
`6ec68e788a81ff10da19ad64d7e721ed1d00ebb5`, árbol
`4787dac73a328655a57870cc435e3d86fcf9b0bb`. El [checkpoint I1](post_cu4_i1_p4_preparation.md)
conserva las reproducciones anteriores y la propuesta `ARC-01`. La autoridad
es el [contrato F3/P4 §2](../../geocedg/specs/ui/document-insertion.md).

## Causa y clasificación

Los controles históricos eran dependientes del catálogo de herramientas del
perfil Desktop. En Gradle, con el `APPDATA` heredado, los helpers reconstruían
`a` y `s` ya en capa 5 y el ensayo volvía a obtener capa 5. Con `APPDATA` vacío
los helpers leen los mismos escalares en capa 0. El receptor tiene un punto
residente `B` en capa 3 oculta y puntos de historial en capa 4. El ensayo lee
esos puntos y tiene máximo 4 antes de copiar los escalares. El resultado
independiente del cambio de capa de trabajo vivo a 0 o 4 demuestra que el
máximo del host y la capa de trabajo de sesión son dos estados distintos.

`a=2` y `s=0` son instancias exactas `GeoNumeric`, independientes y dibujables,
con una definición literal y sin algoritmo productor. El escritor nativo
omite `<layer>` para esa combinación. El lector solo aplica una capa explícita
si encuentra el tag. Durante la construcción de los objetos que llegan por
el clipboard, `ConstructionDefaults` aplica el máximo usado del host y la
sección de carga suspende la capa de trabajo interactiva. `insertFrom` usa la
ruta XML interna con `putdown=true` y el buffer propio de F3. El clipboard de
Windows no participa. El verificador F3 encuentra `layer a` o `layer s` y
rechaza antes de tocar el receptor. El `B` de origen conserva su capa explícita
2 en el elemental y 0 en Locus/Spline, y el residente conserva su capa 3.
[Escritor](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/XMLBuilder.java)
· [Defaults](../../source/shared/common/src/main/java/org/geogebra/common/kernel/ConstructionDefaults.java)
· [Lector](../../source/shared/common/src/main/java/org/geogebra/common/io/ConsElementXMLHandler.java)
· [F3](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java)

La barra de herramientas Desktop inicializa `GeoCeDGUserTools`. Con el
backend instalado, el catálogo de usuario se resuelve a partir de `APPDATA`
cuando no hay ruta portátil. Validar una definición GGT usa un `MacroKernel`
aislado para los objetos y el registro de macros. El `App` anfitrión sigue
siendo compartido. El `setLayer` de un geo que se reconstruye en ese kernel
alcanza `Kernel.notifyChangeLayer` y eleva el contador `App.maxLayerUsed` del
documento anfitrión. Una aplicación vacía lo muestra incluso cuando la macro
no se activa y sus objetos no existen en la construcción principal. El mismo
catálogo se lee al crear tanto el helper de origen como el de ensayo.
[Catálogo Desktop](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserTools.java)
· [Validación GGT](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserToolLibrary.java)
· [Contador del host](../../source/shared/common/src/main/java/org/geogebra/common/main/App.java)

La diferencia de entorno expuso una deficiencia de fixtures y una dependencia
real del estado de aplicación en el fallback heredado. La protección F3 opera
según contrato. Un paso que copiara la capa a mano tras evaluar el XML seguiría
expuesto al mismo fallback al guardar, reabrir o reconstruir el historial.
La corrección se limita a controles reproducibles. La fidelidad persistente
de escalares sin tag y el aislamiento del contador al validar catálogos quedan
como decisiones separadas. Una futura enmienda deberá fijar la semántica del
escalar sin tag en archivos antiguos, el alcance del tag existente al guardar,
y la propiedad del contador entre kernel de documento y kernel de macros.
Exigir regresiones con estilos predeterminados, macros y Undo/Redo en ambos
lectores GeoCeDG y Classic antes de cambiar escritura o lectura.

## Comparación controlada

La ejecución Gradle recompiló el I2 congelado mediante
`gradle-command-evidence-producer.ps1 -GradleTask :desktop:desktop:test`, con
filtros exactos `f3i01`, `f3i02`, `f3i07`, y registró su classpath. Las variantes
aisladas reutilizaron esas clases y dependencias, el mismo Corretto 17.0.10,
el directorio de trabajo Desktop y `-ea`. Los tres backends bajo `APPDATA`
vacío reprodujeron el rechazo. El cruce con preferencias Java en memoria
identificó `APPDATA` como factor y separó el directorio temporal del JVM.
El arnés de observación usó el mismo código de fixtures, inicialización
Desktop real embebida y copy/paste nativo. El catálogo sintético aislado
reprodujo la propagación sin cargar un catálogo personal.

| Evidencia local | Factor | Resultado |
|---|---|---|
| `c1-gradle-baseline/evidence.json` y XML JUnit | I2, worker Gradle, perfil instalado | Gradle 0, 3/3 controles originales |
| `c1-f3-factor` | Mismo build. `APPDATA` vacío con backend instalado, memoria o portátil. Perfil nativo completo como contraste | 0/3 en las tres raíces vacías, 3/3 con entorno nativo |
| `c1-env-cross` | `APPDATA` heredado con temporal aislado. `APPDATA` vacío con temporal del sistema | 3/3 y 0/3 respectivamente, también con preferencias Java en memoria |
| `c1-appdata-witness` | Trazas de origen, copia y máximo con el mismo backend en memoria | `5→5` y `0→4`. Crear el subdirectorio de preferencias vacío conserva `0→4` |
| `c1-corrected-focused/catalog` | Raíz nueva con GGT sintético nativo de capa 5 | 1/1. Aplicación vacía con máximo 5, macro sin registrar. El histórico pasa `5→5` |
| `c1-corrected-focused/f3` | Raíz nueva, controles de referencia corregidos | 3/3. Código 0 |

Las rutas de la tabla parten de `artifacts/agent/post-cu4-pre-i3/`. Las
ejecuciones anteriores al cambio prueban su causa. El ensayo focal posterior
recompiló únicamente el arnés y los dos fixtures sobre las clases I2
congeladas, por lo que se distingue de la verificación del SHA candidato.

## Corrección de referencias y puerta

Los tres métodos de referencia conservan sus verificaciones de historia,
identidad, L1 y persistencia. Sus fuentes positivas crean en capa 4 y tienen
un punto ordinario con tag explícito antes del primer escalar sin tag. En
`elementaryReference` se crea `A` antes de `a`. En `locusReference` los puntos
ya precedían a `s`. Las variantes históricas quedan como negativos en el
perfil vacío. La sonda exige las cuatro parejas de casos por ambas capas de
trabajo, compara capa, clase y valores, y verifica receptor intacto y bytes
de origen intactos en el rechazo. El caso sintético se ejecuta en su propia
JVM y tiene su propio código de salida.

Ejecutar `tools/agent/probes/post-cu4-p4/run-p4-probe.ps1` con `-Jdk`,
`-ClasspathArgs` y `-BuildEvidence` del build focal y un `-Run` nuevo. Cada
suite tiene un `APPDATA` propio. `-PortablePreferences` cambia el backend de
`f3` y `study`. `catalog` verifica el descubrimiento del catálogo del backend
instalado usando el almacén Java en memoria y una raíz de aplicación propia.
El arnés exige exactamente 3, 8 y 1 pruebas exitosas respectivamente y registra
la procedencia de clases, archivos de entrada, versión de Java y resultados.

**G1.** Explicación reproducida y controles fiables en perfil vacío. La fila
`ARC-01` queda preparada como candidata con los límites del [diseño I1](post_cu4_i1_p4_preparation.md#propuesta-arc-01--arco-circular-nativo-directo).
Sus pruebas nativas y el rechazo ordinario P4 se conservan. La admisión del
arco y el tratamiento persistente de escalares sin tag requieren decisión
autoral. Un resultado `5→5` en perfil con catálogo sirve para el diagnóstico
del entorno. La referencia positiva que se usa para la puerta es `4→4` y el
rechazo de seguridad se comprueba con `0→4`.

`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`. Se modifican fixtures y un arnés
de investigación. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` porque usa el JDK
y el productor Gradle existentes. `GUIDE_IMPACT=NONE` porque el cambio se
limita a controles de laboratorio y F3/P4 conserva el contrato público.
