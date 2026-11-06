# PRE-G9B-CU4-R2-TEXT

La rama local `codex/pre-g9b-cu4-r2-text` parte de `0f27f933ad314ade674b6ca744c7fd045312fe9c`, árbol `72f97c4e7a6a0e6e4ecdfd5653ae1af7bca9314b`. Su padre es `28edf08993e14d6f3d9697e18703836637e1d1bf`, candidato R2 con recibo propio. La consulta GitHub confirmó `main` en `1c3893f539d926c16da837a42d439a1cf8bea34f`. El autor comunicó `SMOKE PASS — AUTHOR REPORTED — B2 POSITIVE INSERTION` sobre `0f27f933a`. Ese resultado se limita a la inserción positiva de la corrección anterior. La auditoría de este delta, su smoke y su aprobación autoral son posteriores.

## Frontera de partida y diseño

`formatted.ggb` conserva todos los elementos de `positive.ggb` y añade el comando nativo `Text`, entrada `theta`, salida `formatted`, con su elemento de presentación. El XML registra el comando y sus referencias y deja el resultado textual a `AlgoText`. El rechazo ocurre primero en el filtro de archivos B2, que excluía los comandos. El certificado de construcción también excluía cualquier texto con productor. P4 ya clasifica `GeoText` dependiente como elemental. El receptor preparado se guarda en grados. Los auxiliares `adapted.cedg` y `merged.cedg` del caso DEG-RAD muestran la adaptación previa del control sin texto dinámico. El segundo contiene también la construcción Locus V2 y SplineV2 del receptor y el remapeo nativo. El inventario y los hashes están en `artifacts/agent/pre-g9b-cu4-r2-text/fixture-inventory.json`.

La clase inicial de texto dinámico es la salida terminal de `AlgoText` nativo, con exactamente un argumento que referencia por identidad un ángulo libre ya certificado. El filtro del ZIP verifica la forma nativa de ese comando antes de cargarlo. En cada auxiliar y en cada copia se exige que el contenido coincida con el formato nativo del valor del ángulo en ese contexto. La comparación conserva el productor, sus referencias y las propiedades aplicables, incluido un anclaje a un punto libre nombrado cuando existe. Así, una diferencia en la cadena solo puede corresponder al formato del mismo valor. Se mantienen el texto literal, el certificado angular y el remapeo F3. Las opciones adicionales de `Text`, los ajustes de precisión locales al texto y la composición con literales quedan fuera de la primera clase. Requieren acreditar sus formas nativas y sus entradas. Un texto que alimente una operación posterior, como el cálculo de la longitud de su cadena o su conversión a número, queda fuera de B2.

Desktop conserva la validación previa del archivo y usa los formateadores existentes para mostrar la vista previa. La publicación sigue el preflight, la oferta vinculada a la fuente y al receptor, el dry run y la transacción F3. Un rechazo por alcance B2 ofrece igualar el modo en una copia, revisar la construcción y reintentar con todas las validaciones normales. Las causas identificadas de F3/P4 conservan su diagnóstico propio.

## Plan de evidencia

`VERIFICATION_CLASS=INTEGRATED_PHASE`. El mandato autoral requiere una campaña `FINAL` sobre el SHA definitivo. Los planes efectivos muestran 68 nodos de `FINAL`, incluidos los 22 de `INFRA_UNIT` y los 46 de `INTEGRATION` con sus contratos idénticos. `STATIC` contiene `prompt.execution-safety` y `diagnostic.guide-structure` fuera de `FINAL`, por lo que se ejecuta aparte. `PHASE PRE-G9B-CU4` comparte cinco nodos de compilación, proyección y perfil con `FINAL`. Los otros cuatro corresponden a los productores y las proyecciones JUnit acotados. Se comprueba su cobertura por identidad de prueba contra los XML autenticados de las tareas amplias de `FINAL` sobre el SHA final. El informe previo de PHASE identifica las 53 pruebas compartidas y las 77 de Desktop contra el inventario vigente, con las mismas tareas Gradle. El resultado previo sirve para delimitar esas selecciones. Las pruebas efectivas del nuevo SHA provienen de los productores del nuevo `FINAL`. El catálogo y los filtros mantienen sus identidades. `phase-closeout.ps1 -Action INSPECT` permite cotejar el recibo definitivo en modo de solo lectura conforme al contrato vigente.

`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`. El delta usa las APIs nativas y los prerrequisitos de Java, Gradle, Conda y Windows vigentes.

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md, docs/user/geocedg_user_guide_es.md
```

## Evidencia focal anterior a la congelación

El wrapper Gradle ejecutó `:desktop:desktop:test` con los filtros `Cu4B2InsertionTest`, `PreG9BCu4InsertionTest`, `PreG9BR6PlusF3InsertionTest` y `PreG9BR6PlusF3RecoveryTest`. Las 33 pruebas pasaron. La repetición de las cuatro pruebas de `Cu4B2InsertionTest` verificó además los motivos exactos de recuperación y el rechazo de un argumento adicional en el XML de `Text`. Ambos comandos devolvieron 0 y conservaron sus argumentos y XML en `artifacts/agent/pre-g9b-cu4-r2-text/gradle-focal-01` y `gradle-focal-02`.

| Caso | Comprobación |
|---|---|
| Seis direcciones entre grados, radianes y DMS | `Text(theta)` terminal, formato del receptor, vista previa idéntica al resultado, valor interno, deslizador, seno, coseno, punto, literal y anclaje por identidad. Los casos ejercitan `.ggb` y `.cedg`, colisiones y distinta precisión. |
| Archivos preparados | `formatted.ggb` y `positive.ggb` originales insertados en `native-receiver.cedg` con el receptor en radianes. La vista previa de `formatted` pasa de `47°` a `0.82 rad`. El productor y su argumento se conservan tras Undo/Redo y reapertura. Los originales conservan sus bytes. |
| Consumo o cambio de la definición | Se rechazan `Length(formatted)`, `ParseToNumber(formatted)`, una composición con literal y entradas de `Text` cambiadas a otro tipo o a una expresión. El receptor conserva instancias, aristas e historial. |
| Alternativa manual | La acción de Desktop entrega el aviso para `Length(formatted)`, identifica el comando y exige igualar el formato, revisar, guardar una copia y pasar la admisión normal. La copia de ese ejemplo vuelve a pasar por la ruta ordinaria. Una copia con guion sigue excluida. |
| Diagnóstico ajeno al formato | Biblioteca JavaScript, guion, botón excluido y XML corrupto producen su propio rechazo. |
| Cancelación, caducidad y recuperación | La cancelación y los cambios de construcción, modo, fichero o historial durante la oferta impiden llegar a la barrera. La inyección de cambios de estilo, contenido o dependencia durante la publicación activa la recuperación certificada. |

La sonda `R2TextFixtureProbe` contrasta los archivos preparados en `fixture-baseline-02` y `fixture-candidate-01`. La prueba de Desktop observa los datos de la confirmación y el aviso que la acción entrega. El comportamiento de la ventana Swing queda para el smoke autoral. Se conservan el primer intento fallido de la sonda y los registros de R1 y R2. La limitación histórica de preflight F3 de `InterCilConoOblique.cedg` sigue registrada en R1. H08 permanece diferido.

## Smoke autoral y auditoría focal

1. Arrancar el SHA congelado con `./gradlew.bat :desktop:desktop:runGeoCeDG`. Abrir `native-receiver.cedg` del directorio `b2-design/run-12/DEG-RAD` de la evidencia R2 y cambiar el formato del receptor a radianes.
2. Insertar `formatted.ggb`. Verificar que la ventana muestra el texto dinámico y el cambio `47° → 0.82 rad` con la precisión habitual. Cancelar y repetir cerrando la ventana. El dibujo y el historial deben conservarse. Repetir y aceptar. Mover el deslizador, comprobar que el texto, el seno, el coseno y el punto lo siguen, y comprobar Undo/Redo y reapertura en una copia.
3. Repetir con el receptor recién abierto y `positive.ggb` como control. Con el mismo formato en ambos documentos, la inserción ordinaria debe seguir sin la oferta B2.
4. Abrir el origen `formatted.ggb`, añadir `chars=Length(formatted)` y guardar una copia en grados. Insertarla en el receptor en radianes. Verificar el aviso y la ausencia de cambios. Igualar el formato en esa copia, revisar los objetos y textos, guardar otra copia e intentar insertarla de nuevo. La admisión ordinaria se aplica desde el inicio.

La auditoría puede revisar el diff respecto a `0f27f933a`, el inventario de los ZIP y la correspondencia de identidades de `AlgoText`. Los casos adversarios focales pueden variar la forma de `Text`, el consumidor, el anclaje o la revisión del receptor. El manifiesto de evidencia enlaza los diagnósticos y el recibo de `FINAL` para verificar el SHA sin repetir la campaña.
