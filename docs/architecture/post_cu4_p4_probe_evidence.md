# POST-CU4 · Sondas focales de admisión y copia nativa

**Evidencia de investigación.** Fuente `d5332e400b0ab68afd14273495b9ae5f03e6c911`,
árbol `e8ee4b4b371d34f7f22cc081dee1f429963150dd`. El diff del producto
desde el candidato aprobado `ba4d16018cf683750a20619d9d59c84428bcbef7`
está vacío. La aprobación CU4 pertenece a su [registro de cierre](../validation/pre_g9b_cu4_closeout_record.md).
Esta evidencia caracteriza los archivos y el runtime descritos abajo.

## Ejecución reproducible

El [arnés de investigación](../../tools/agent/probes/post-cu4-p4/PostCu4P4Probe.java)
se ejecuta con el [lanzador](../../tools/agent/probes/post-cu4-p4/run-p4-probe.ps1)
desde la raíz. Usa Temurin 25.0.4 y las salidas de test Desktop y
dependencias ya disponibles. El archivo de argumentos debe contener el
classpath de test Desktop con sus entradas resueltas. El lanzador elimina
del principio las clases de sondas anteriores. Recompila en un directorio
propio admisión, copia Desktop, orquestación y certificado B2, tres clases
de controles F3/CU4 y el arnés. Los JAR y las demás clases del runtime
proceden del classpath local. El manifiesto de la ejecución identifica el
origen del classpath, las fuentes recompiladas, el JAR common y sus hashes.

```powershell
./tools/agent/probes/post-cu4-p4/run-p4-probe.ps1 `
  -Jdk 'C:/Users/usuario/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2' `
  -ClasspathArgs 'artifacts/agent/pre-g9b-cu4-r2-text/fixture-candidate-01/javac.args' `
  -Run run-06
```

Usar un identificador de ejecución nuevo para repetir. El arnés genera sus
propios `.cedg` con el escritor nativo. El proceso, la configuración
portátil y el EDT son propios del ensayo. Se conservan `source.cedg`,
`merged.cedg`, los controles de F3, `inputs.json`, los argumentos,
`javac.log` y `java.log` bajo
`artifacts/agent/post-cu4-insertion-review/p4/run-06/`.
`javac` y `java` salieron con código 0. Se completaron los siete casos y
un diagnóstico de controles F3. Los avisos de anotaciones del classpath
local se conservan en el log del compilador.

## Resultados observados

El origen se genera, guarda y reabre. La ruta ordinaria se ejecuta sobre
un receptor con colisión de etiqueta. Para los cinco rechazos, se verifica
el motivo de admisión, la igualdad de las diferencias serializables
observadas, las instancias residentes y los bytes del origen. En otro
receptor se ejecuta directamente `GeoCeDGInsertionCopyPaste.insertFrom`.
Las etiquetas originales y las de los drivers colisionan con objetos
residentes. Se prueba L1, la identidad de las instancias residentes y las
aristas etiquetadas en los casos con mapa de copia. El par clase/productor
y el orden de sus entradas etiquetadas también se cotejan tras reabrir.

| Caso | Construcción de origen | F3 ordinario | Primitiva y reapertura |
|---|---|---|---|
| Arco | `A=(0,0)`, `B=(2,0)`, `C=(0,2)`, `obj=CircularArc(A,B,C)`, `W=Point(obj,0.5)` | Rechazo P4, `GeoConicPart/AlgoConicPartCircle`. | Mapa 5 instancias, incluidas las aristas etiquetadas de W. Conserva matriz, límites, orientación y longitud en las observaciones. Editar C a `(-2,0)` y luego al centro reproduce el mismo estado en ambos documentos. El caso del extremo en el centro sigue definido con soporte circular de tipo 4. |
| Sector | Mismos puntos, `obj=CircularSector(A,B,C)` | Rechazo P4, `GeoConicPart/AlgoConicPartCircle`. | Mapa 4 instancias. Conserva los testigos del soporte, límites y valor del sector en estas posiciones. Editar C y volver de la coincidencia reproduce el mismo estado de soporte. |
| Hipérbola | `F=(-2,0)`, `G=(2,0)`, `a=1`, `obj=Hyperbola(F,G,a)` | Rechazo P4 por subtipo, `GeoConic/AlgoHyperbolaFociLength`. | Mapa 4. Coinciden tipo y matriz. Con `a=1.25` el testigo cambia de forma coherente. En `a=2` ambos quedan definidos como recta doble de tipo 7, con retorno a hipérbola. |
| Parábola | `F=(0,1)`, `d: y=-1`, `obj=Parabola(F,d)` | Rechazo P4 por subtipo, `GeoConic/AlgoParabolaPointLine`. | Mapa 3. Coinciden tipo y matriz. Editar F a `(1,2)` cambia el testigo. Con F en `(0,-1)` ambos quedan definidos como recta doble de tipo 7, con retorno a parábola. |
| Curva | `a=2`, `obj=Curve(t,a*t^2,t,-1,1)` | Rechazo P4 por clase, `GeoCurveCartesian/AlgoCurveCartesian`. | Aparecen curva y escalar nuevos. Mapa nativo vacío. Coinciden límites y muestras cotejadas. Variar `a` a 3 reproduce el cambio en el temporal reabierto. |
| Secuencia | `a=2`, `obj=Sequence(k*a,k,1,4)` | Admitida, `GeoList/AlgoSequence`. | Aparecen lista y escalar nuevos. Mapa nativo vacío. Coinciden valores ordenados y su cambio para `a=3`. |
| Texto dependiente | `a=2`, `obj=Text(a)` | Admitido en modo igual, `GeoText/AlgoText`. | Mapa 2. Coinciden productor, dependencia etiquetada y texto, también tras variar `a` a 3. |

**Identidad y recuperación.** Ninguna muestra sintética lleva identidad
espacial durable. La primitiva directa cambió el UUID del receptor y no
creó un checkpoint en los siete casos. El arnés guardó un checkpoint
explícito en cada receptor desechable. Undo recuperó el XML normalizado y
UUID anteriores. Redo y reapertura recuperaron los testigos observados y
el UUID resultante de esa copia directa. F3 tiene su propia restauración
del UUID y escribe e identifica su propio store. La observación refuerza
la necesidad de ese perímetro al diseñar el modo experto. [Transacción F3](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java)

**Correspondencia y tolerancia.** En curva y secuencia, la medición del
resultado se apoya en que cada archivo sintético crea una única instancia
nueva de cada clase observada. Esa selección permite cotejar valores
del ejemplo. La procedencia del grafo completo queda sin demostrar
cuando falta el mapa de copia. En reapertura se cotejan los puertos
etiquetados del resultado ya localizado y se comprueba una edición
pareada del driver. El umbral absoluto del arnés es `1e-9` para sus
testigos numéricos locales. Tipo, clase, productor y estado se comprueban
por separado. Las muestras de curva y los coeficientes observados
carecen de alcance de equivalencia geométrica general. Para I1 se
requieren oráculos analíticos y pruebas de fronteras del dominio por fila.

## Controles existentes y límites de verificación

- `verify.ps1 -Profile DEV -Module desktop -TestFilter` con
  `PreG9BR6PlusF3InsertionTest`, `PreG9BCu4InsertionTest` y
  `Cu4B2InsertionTest` (nombres cualificados bajo `org.geocedg.desktop`),
  `-KeepBuildOutputs -LogDirectory artifacts/agent/post-cu4-insertion-review/dev-desktop`,
  salió con código 3, `REJECTED_VERIFICATION_CORE / INCOMPLETE`, sin
  productores. El [registro tipado](../../geocedg/specs/operations/verification-registry.json)
  declara pendiente `dev.focused-semantic-selection`. El informe es
  `dev-desktop/verification-result.json` dentro del directorio de evidencia.
- El arranque directo inicial `p4/run-02/java.log` (código 1) ejecutó
  33 pruebas. Pasaron 23 de 26 controles existentes. Fallaron
  `f3i01ElementaryInsertionIsOneUndoStepWithIdentityKept`,
  `f3i02LabelRenamingIsDeterministicAndClosed` y
  `f3i07LocusV2AndSplineV2ReceiveFreshIdentitiesAndPersist`, que esperaban
  inserción. Los siete casos nuevos de ese arranque también se detuvieron
  por la expectativa incorrecta de UUID del arnés. Los controles
  existentes de M1 y la clase B2 seleccionada se ejecutaron en ese
  arranque local. Su resultado conserva la procedencia de dicho classpath.
- En `run-06` los documentos de control del fixture F3 reprodujeron
  rechazo durante la prueba aislada por `layer a` y `layer s`, con
  receptor intacto. Ambos rechazos se observaron con capa de trabajo 4
  y con capa 0. El caso diagnóstico se considera completado al registrar
  y verificar el rechazo antes de mutación. La causa de la discrepancia
  respecto al comportamiento esperado queda abierta en ese runtime
  local. El mensaje identifica la primera comparación fallida de F3.
  Hace falta reproducirla con salidas compiladas y ligadas al verificador
  antes de atribuirla al producto o al entorno. I1 debe resolver esa
  discrepancia antes de certificar una fila.
- Quedan para incrementos futuros archivos de usuario y matrices de
  productores completos, `.ggb` legado por familia, recursos activos,
  macros combinadas con identidad, posiciones de costura y tangencia,
  cambios de rama, relaciones espaciales y recuperación por fallos
  inyectados. Los controles locales, las sondas sintéticas y la evidencia
  de aceptación histórica conservan alcances separados.

[Inventario y prioridades](post_cu4_p4_inventory.md) ·
[Diseño de inserción experta](post_cu4_expert_insertion_design.md)
