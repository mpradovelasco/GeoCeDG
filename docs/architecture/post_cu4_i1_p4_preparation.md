# POST-CU4 I1 · Preparación de la primera fila P4

**`I1 PREPARED — PENDING AUTHOR IMPLEMENTATION AUTHORIZATION`** ·
**`PARTIALLY READY`**. Los tres controles pasan en el build canónico. La sonda
ya localiza la divergencia del runtime aislado y valida el arco con oráculos
analíticos. La divergencia impide usar el lanzador aislado para acreditar una
fila de admisión hasta resolver la condición de capas que se describe abajo.

**Investigación de interoperabilidad Desktop.** El [estudio original](post_cu4_p4_probe_evidence.md)
queda identificado por `e68e19d6a9e0bfbcaa1e207a042401727885e0a9`. Se conservan sus
sondas y fixtures en Git y las salidas históricas `p4/run-02` y `p4/run-06` en
`artifacts/agent/post-cu4-insertion-review/`. Esta preparación conserva la
[matriz aprobada](../../geocedg/specs/ui/document-insertion.md#2-insert-file-restricted-native-insertion-r),
incluidos sus rechazos actuales de arcos.

## Controles positivos de referencia

| Control F3 | Fuente y contrato que debe reproducir | Fallo de la sonda histórica |
|---|---|---|
| `f3i01ElementaryInsertionIsOneUndoStepWithIdentityKept` | Fixture `elementary`, que combina primitivos 2D, escalares, construcciones dependientes, visibilidad condicional, listas y secuencia. Inserción atómica, UUID conservado y un Undo. | Ensayo aislado, primera diferencia de capa en `a`. |
| `f3i02LabelRenamingIsDeterministicAndClosed` | Mismo `elementary` sobre dos receptores con las mismas colisiones. Orden y renombrado L1 reproducibles. | Misma diferencia en `a`, antes de comprobar L1. |
| `f3i07LocusV2AndSplineV2ReceiveFreshIdentitiesAndPersist` | Fixture `locus`, con `s` como driver, `L=LocusV2(Q,s,D)` y `S=SplineV2({A,B,C,E},3)`. IDs de los participantes nuevos y persistentes, IDs residentes conservados. | Ensayo aislado, primera diferencia de capa en `s`, antes de comprobar persistencia. |

Los tres controles son los métodos canónicos de
[`PreG9BR6PlusF3InsertionTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusF3InsertionTest.java).
El [cierre CU4](../validation/pre_g9b_cu4_closeout_record.md) acredita una
campaña FINAL del producto `ba4d16018cf683750a20619d9d59c84428bcbef7`. Su XML JUnit
de Desktop contiene los tres positivos. El diff de `source`, `gradle` y
`apps/geocedg` entre ese producto y `d5332e400b0ab68afd14273495b9ae5f03e6c911`
está vacío. Una ejecución focal posterior compiló las 47 tareas de Desktop y
ejecutó exactamente esos tres métodos. Código Gradle `0`, JUnit `3/3`, sin saltos.
La evidencia se conserva bajo `artifacts/agent/post-cu4-increments/i1-baseline/`.

### Política de capas y comparación focal

F3 compara `GeoElement.getLayer()` antes de publicar. La capa de trabajo es de
sesión. El conjunto de capas ocultas forma parte del documento y enmascara la
presentación sin reescribir la visibilidad intrínseca de un objeto. El receptor
canónico tiene `B` en capa 3 oculta y los controles se repitieron con capa de
trabajo 4 y 0. La sonda distingue la capa del objeto, la visibilidad propia, el
conjunto oculto y el máximo de capas que usa la ruta heredada de inicialización.
[Contrato de inserción §2.6](../../geocedg/specs/ui/document-insertion.md#2-insert-file-restricted-native-insertion-r)
· [Política Desktop](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLayerWorkspace.java)

El lanzador corregido toma el runtime de una ejecución reciente de Gradle,
compila solo las dos clases de la sonda y separa los controles F3 de los ensayos
directos en procesos distintos. Registra hashes del manifiesto de build, de las
clases críticas y de las fuentes compiladas. Una negativa controlada en el
diagnóstico de capas no cuenta como un control F3 positivo. Se conservan los dos
códigos por separado.

| Ejecución | Java y preferencias | Controles F3 | Sonda de casos y capas |
|---|---|---|---|
| `i1-baseline` | Corretto 17.0.10, worker Gradle, backend instalado | 3/3, código 0 | Fuera de la selección |
| `i1-p4/run-02` | Corretto 17.0.10, backend normal con almacén Java en memoria y `APPDATA` propio | 0/3, código 1 | 8/8, código 0 |
| `i1-p4/run-03` | Mismo JDK, clases y classpath, backend portátil con archivo propio | 0/3, código 1 | 8/8, código 0 |

Los dos lanzamientos aislados dieron el mismo testigo para `a` y `s`. El
escalar tiene capa 0 en la fuente cargada y el XML del objeto carece de
`<layer>`. En la prueba nativa obtiene capa 4, igual al máximo usado en el
receptor de ensayo. El receptor de ensayo reconstituye capa de trabajo 4 y
conjunto oculto `[3]` en ambas variantes de capa de trabajo del receptor vivo.
El `B` residente conserva capa 3 y el `B` importado conserva su propia capa
explícita, 2 para `elementary` y 0 para `locus`. Se comprobó la igualdad de
los certificados serializables y las instancias del receptor al rechazar, y
la igualdad de los bytes de origen.

La causa inmediata de los tres rechazos queda localizada en el fallback de
capa de escalares durante la copia XML. El escritor omite el tag para un
`GeoNumeric` con definición. El lector repone capas explícitas y, si falta el
tag, puede quedar el máximo usado de las propiedades por defecto al construir
el objeto. F3 compara las capas y rechaza esa divergencia en el ensayo. El
replay reproduce exactamente la pareja que lee el verificador. Su posición
permite diagnosticar el comparador F3, y solo el testigo nativo de `s` aporta
un mapa de copia en ese replay. En `elementary` el mapa no se completó.
[Escritor](../../source/shared/common/src/main/java/org/geogebra/common/kernel/geos/XMLBuilder.java)
· [Defaults](../../source/shared/common/src/main/java/org/geogebra/common/kernel/ConstructionDefaults.java)
· [Lector](../../source/shared/common/src/main/java/org/geogebra/common/io/ConsElementXMLHandler.java)
· [Verificador F3](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java)

**Clasificación.** Los fixtures canónicos se conservaron sin ajustes. El
diagnóstico excluye que el JDK 25 o la recompilación parcial fueran condiciones
necesarias para el rechazo. También excluye que seleccionar el backend portátil
sea por sí solo el disparador. Sigue sin establecerse qué diferencia de contexto
entre el worker Gradle y ambos arranques aislados evita el fallback en el worker.
Hay una diferencia real de fidelidad bajo las condiciones aisladas probadas, con
el rechazo preventivo correcto de F3. Una actuación separada debe aislar la
diferencia del worker y asegurar la política de escalares sin capa serializada
antes de usar esta sonda aislada como puerta de una fila nueva.

### Reproducción

Ejecutar los tres métodos exactos mediante el
[productor Gradle](../../tools/agent/checks/gradle-command-evidence-producer.ps1)
con `-GradleTask :desktop:desktop:test` y filtros `--tests` por nombre
cualificado. Preservar el informe del productor y el XML JUnit antes de otro
build. Un descriptor de classpath puede obtenerse en esa misma ejecución con
`--init-script tools/agent/probes/post-cu4-p4/record-desktop-classpath.init.gradle`
y `-Dgeocedg.p4.classpathFile=<ruta absoluta de salida>`.

```powershell
./tools/agent/probes/post-cu4-p4/run-p4-probe.ps1 `
  -Jdk 'C:/Users/usuario/.jdks/corretto-17.0.10' `
  -ClasspathArgs 'artifacts/agent/pre-g9b-cu4-r2-text/fixture-candidate-01/javac.args' `
  -BuildEvidence 'artifacts/agent/post-cu4-increments/i1-baseline/evidence.json' `
  -Run run-10
```

El descriptor histórico se utiliza únicamente como lista de rutas. El lanzador
descarta cualquier prefijo de clases de la sonda previa. Los archivos de cada
invocación quedan en `artifacts/agent/post-cu4-increments/i1-p4/<run>/`. Con
`-PortablePreferences` se selecciona la otra variante aislada. Ambos perfiles
utilizan un identificador nuevo, una JVM por suite y datos propios de ensayo.
El manifiesto `artifacts/agent/post-cu4-increments/i1-evidence.sha256` identifica
el JUnit focal, los logs, los argumentos y los archivos de ensayo conservados.

## Propuesta ARC-01 · Arco circular nativo directo

**Candidata a autorización.** Salida de clase exacta `GeoConicPart`, productor
exacto `AlgoConicPartCircle`, variante `CONIC_PART_ARC`, soporte
`CONIC_CIRCLE`. La forma de usuario `CircularArc(A,B,C)` se serializa como
`CircleArc`. Los puertos ordenados son centro `A`, inicio sobre la circunferencia
`B` y dirección del extremo `C`. El productor interno construye un círculo por
centro e inicio, proyecta ambas direcciones sobre ese soporte y usa el recorrido
positivo. `C` puede quedar fuera del círculo. Preservar que `B` incide en el
soporte y que el extremo evaluado está sobre el rayo `AC`.
[Dispatcher](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/BasicCommandProcessorFactory.java)
· [Productor](../../source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoConicPartCircle.java)
· [Parametrización](../../source/shared/common/src/main/java/org/geogebra/common/kernel/kernelND/GeoConicPartParameters.java)

- **Dominio inicial propuesto.** Mismo modo angular. Tres puntos ordinarios 2D,
  etiquetados, finitos y definidos, con clausura admitida por P4. `A≠B`, `A≠C`,
  radio finito positivo, amplitud nativa positiva y soporte circular definido.
  Para el piloto, limitar a un arco terminal sin macros, recursos o identidad
  espacial. Un `Point(arc,0.5)` actúa como testigo de la sonda. La admisión de
  consumidores requerirá el cotejo de su parámetro y dependencia, incluido el
  puerto local cuando su parámetro se serializa como literal.
- **Valor y rama.** El productor fija la orientación positiva. En coordenadas
  del círculo, la amplitud `Δ` se obtiene de sus parámetros angulares en radianes
  módulo `2π`, y la longitud nativa es `r·Δ` en unidades de construcción. El
  parámetro de camino está normalizado en `[0,1]`, con el inicio y el fin en sus
  extremos. El ensayo comprueba los valores analíticos y la instancia del camino.
  Los cambios de orientación, productor o puertos son discretos. El umbral de
  test `1e-9` se aplica a los testigos numéricos de radio 2 y a su ángulo en
  radianes. Para la futura fila queda por fijar y probar la tolerancia escalada
  y el tratamiento de la costura en magnitudes extremas.
- **Degeneraciones y transiciones.** Someter a prueba `A=B`, `A=C`, `B=C`,
  puntos distintos sobre el mismo rayo, entradas indefinidas o infinitas,
  radios próximos a la precisión efectiva, casi coincidencia angular por cada
  lado de la costura y cambio de orden. Al editar un resultado admitido,
  preservar la actualización y los estados del productor. El estudio anterior
  observó soporte circular definido cuando `C=A`. Esa configuración queda fuera
  del dominio inicial por carecer de rayo terminal geométrico. El candidato no
  asigna una nueva interpretación al resultado nativo.
- **Identidad y persistencia.** Comparar los tres puertos en orden mediante el
  mapa nativo durante el ensayo. Conservar sus aristas, comprobar que una colisión
  L1 conserva las instancias residentes y cotejar clase, productor, geometría y
  parámetros tras serialización. Registrar por separado el éxito de la copia
  directa y el de F3 completo. La primera puede cambiar UUID y requiere un
  checkpoint explícito en el receptor desechable. El segundo debe conservar el
  UUID y satisfacer la transacción y la recuperación aprobadas, incluido un único
  Undo/Redo. Las etiquetas L1 describen el resultado de una operación conocida,
  sin servir de identidad durable.
- **Presentación.** Comparar estilo, capa y visibilidad intrínseca con el testigo
  del objeto. La visibilidad efectiva se evalúa bajo el conjunto oculto del
  receptor y la política vigente de pertenencia a vistas. Incluir arco visible
  de capa 2 oculto por el documento fuente, receptor con su propia capa oculta,
  y la reapertura de ambos estados.
- **Rechazos.** Sector circular, arcos circunscritos o derivados de cónicas,
  productores 3D, fuentes con contenido activo, cierre espacial, macro o
  parámetro inverificable. Validar estos rechazos antes de cualquier cambio y
  preservar B2 en una prueba separada.

La enmienda futura añadiría una fila a §2.2 y su comparador a §2.6. Las pruebas
del arco cubren por separado alcance inicial, reconstrucción y paso dinámico por
posiciones excluidas al admitir. En `run-02` y `run-03` se comprobaron arco de
radio 2 con extremo fuera del círculo, longitudes `π`, `2π` y `3π`, recorrido
positivo, extremos y parámetro `0.5` de `W`, amplitud cero, entrada indefinida y
retorno. Se verificaron capa 2, color, visibilidad intrínseca, rechazo ordinario,
renombrado en copia, Undo/Redo explícito y reapertura. Quedan por ensayar los
radios de frontera, infinitos y variantes de productor, y la transacción de la
futura fila autorizada.

## Orden de las siguientes filas candidatas

1. **Polilínea ordinaria** `GeoPolyLine/AlgoPolyLine` con vértices 2D admitidos,
   orden y aristas exactos, vértices repetidos y longitud por segmento. Separar
   variante de lista, hoja ISO y trazos de pluma.
2. **Parábola focal** `GeoConic/AlgoParabolaPointLine`. Para foco `(0,1)` y
   directriz `y=-1`, cotejar `x²=4y`, foco/directriz y su degeneración a recta doble.
3. **Hipérbola focal** `GeoConic/AlgoHyperbolaFociLength`. Para focos `(-2,0)` y
   `(2,0)` y semieje 1, cotejar `x²-y²/3=1`, vértices y ambas ramas. Controlar
   edición del semieje hasta degeneración y retorno.

El sector puede reutilizar el productor del arco con un comparador de área,
frontera y región propio. Curva cartesiana y secuencia requieren primero resolver
la procedencia de puertos locales y la ausencia del mapa de copia en las sondas
previas. [Inventario P4](post_cu4_p4_inventory.md)

`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`. El cambio se limita al lanzador y
a los ensayos de investigación. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED` porque
se reutilizan el JDK Desktop y el productor Gradle existentes.
`GUIDE_IMPACT=NONE` porque el comportamiento público y la admisión P4 conservan
su contrato vigente.
