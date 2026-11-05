# Candidato de implementación PRE-G9B-CU4

`selfApproved=false`. La implementación local parte de `0e56d0cb81c73a69eb10abf4e5208dd48efb5f64`. El manifiesto de entrega identifica el commit técnico y el árbol que se deben usar para la auditoría y el smoke. [Autorización](pre_g9b_cu4_author_design_approval_record.md) · [Puertas A1–A6](pre_g9b_cu4_preparation_candidate_report.md)

## Alcance técnico

- B01 incorpora las filas P4 de ángulo libre con deslizador y texto estático. La admisión compara la unidad de presentación angular del documento. La simulación y la comprobación posterior a la copia verifican referencias y estado del objeto. La recuperación sigue el protocolo F3.
- B05 y M01 incorporan dos prototipos de apariencia independientes, con precedencia de documento y preferencias personales según el contexto. Una entrada opcional de ZIP conserva los predeterminados V2 del documento. El estilo de los objetos existentes se restaura por su XML. Si un archivo antiguo omite la política de etiquetado, se restaura su construcción con el comportamiento de compatibilidad y se resuelve el fallback personal para las creaciones posteriores.
- M05.1 conserva las nuevas elecciones de «Nombre + valor» con el alias 6 del campo de etiqueta existente cuando el escritor histórico lo habría omitido. Los lectores resuelven el alias al modo 1. Los archivos antiguos con valor 1 conservan la lectura y la regla de emisión histórica para respetar D1. H09 usa los tipos nuevos para transferir color, grosor y tipo de línea a ambas curvas, conservando la presentación de sus etiquetas.
- H08 queda diferido. L1 mantiene su renombrado indexado. CU7, el libro y las demás fases quedan fuera de alcance.

## Smoke autoral

Usar una copia de trabajo de cada documento, una configuración Portable separada y el candidato exacto del manifiesto. Registrar el resultado de cada paso junto al SHA y árbol.

1. **Inserción compatible.** Crear un receptor en grados con un ángulo llamado `α_{cyl}` y guardarlo como `.cedg`. Insertar `InterCilConoObliqueTest.ggb`. Comprobar texto estático en su posición, los ángulos de 47° y 40° con límites de 0° a 90° y paso de 1°, y que una colisión recibe nombre indexado. Mover el ángulo insertado y comprobar que responden sus dependientes. Deshacer, rehacer, guardar como copia y reabrir. [Corpus de trabajo](../../models/regression/pre-g9b-cu/manifest.yml)
2. **Rechazo atómico.** Abrir otra copia del receptor y seleccionar radianes. Guardar el estado de partida, luego intentar la misma inserción. Verificar el mensaje de unidad incompatible, el número de objetos, la identidad del dibujo y el estado de Deshacer/Rehacer. El rechazo conserva la unidad del receptor.
3. **Predeterminados y compatibilidad.** Asignar colores y trazos distintos a Locus V2 y SplineV2 en Predeterminados y pulsar Guardar configuración. Reiniciar con la misma configuración y crear ambas curvas. Abrir un documento anterior con objetos de estilo explícito y confirmar que conserva su presentación. Abrir un documento guardado con defaults propios distintos y crear otra curva. En Nuevo vuelven los predeterminados personales.
4. **Etiquetas y T0.** Bajo «Ningún objeto nuevo», mostrar como deslizador un número escrito en Álgebra y elegir «Nombre + valor». Copiar y pegar, deshacer y rehacer, y comprobar tras reabrir. Aplicar estilos desde un documento con predeterminados de curva distintos a una selección de Locus V2, SplineV2 y un segmento. Verificar el cambio de trazo y la conservación de etiquetas y geometría. Repetir y comprobar que la segunda aplicación no añade un paso de Undo.
5. **Guarda CU7.** Sobre una copia de un caso R5 admitido, crear por clic y por Q con permisos de arrastre opuestos. Comprobar bloqueo, dependencia al mover el conductor y persistencia tras reabrir. Usar un SplineV2 ya guardado como control. [Matriz de casos R5](pre_g9b_cu7_r5_validation_matrix.md)

## Verificación y límites

La selección PHASE propia agrupa pruebas de los tipos y rutas modificados con regresiones F3, D1 y CU7. INTEGRATION ejecuta la cobertura compuesta del registro activo. STATIC, INFRA_UNIT, informes de proceso, JUnit, delta y hashes se referencian en el manifiesto externo al commit técnico. Los ensayos de compatibilidad distinguen la lectura de documentos antiguos y la posible pérdida de los nuevos defaults si se vuelven a guardar con un lector anterior.

| Puerta | Evidencia focal |
|---|---|
| A1 | `PreG9BCu4InsertionTest` usa el origen autoral y sintéticos de ángulo y texto. Verifica los nueve pares de unidades, las colisiones, los límites y los incrementos referenciados. `PreG9BR6PlusF3InsertionTest` conserva rechazos y cierre de dependencias. |
| A2 | `PreG9BCu4InsertionTest` inyecta un vínculo incorrecto con el mismo valor escalar y comprueba la recuperación. Las pruebas F3 de inserción y recuperación cubren instantáneas, historial y rutas K1. |
| A3 | `Cu4V2StyleDefaultsTest` comprueba los tipos y políticas del núcleo. `Cu4V2DefaultsArchiveTest` recorre los formatos de archivo y `PreG9BCu4StylesTest` verifica dos procesos Portable y la política automática con Álgebra visible y oculta. |
| A4 | `Cu4SliderLabelPersistenceTest` recorre el setter de Propiedades, la barra, persistencia, historial, inserción y portapapeles en la misma ventana y entre ventanas. `PreG9BR6PlusD1LegacyByteIdentityTest` comprueba la identidad de bytes del corpus anterior. |
| A5 | `PreG9BCu4StylesTest` comprueba los dos tipos V2 y que el color dinámico sigue activo. `PreG9BR6PlusF3R2StylesTest` comprueba las propiedades por tipo, selección, repintado y repetición sin paso adicional de Undo. |
| A6 | `PreG9BCu4StylesTest` comprueba las direcciones semánticas de clic y Q tras T0. Las guardas CU7 y del ciclo nativo prueban creación, arrastre, dependencia, persistencia e historial. |

El manifiesto conserva los intentos intermedios con su resultado original. La resolución de los perfiles se guarda junto a la ejecución. La compuesta se ejecuta una vez como INTEGRATION.

La prueba del lector anterior usó las clases empaquetadas de GeoCeDG
`b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513` y dos copias del `.ggb` de CU0.
La entrada opcional sintética y el alias de etiqueta produjeron el mismo XML de
construcción y las mismas entradas al volver a guardar que la copia de control.
El lector descartó la entrada opcional. El manifiesto conserva la procedencia,
el sondeo y las salidas de ambas variantes.

El receptor autoral completo `InterCilConoOblique.cedg` y su variante CU0 sin
`e_2` no superan el preflight de la instantánea de recuperación de F3. La
reproducción con las clases de producto del baseline y una carga ya admitida
de un solo punto devuelve `REJECTED_BEFORE_MUTATION` con un desacuerdo previo
entre el registro de identidad y el DAG. El caso positivo de B01 usa el
`.ggb` autoral y un receptor nativo F3 con Locus V2, SplineV2 e identidades.
El test del receptor completo exige el rechazo atómico. La compatibilidad de
la instantánea de esos dos receptores queda fuera de CU4.

El expediente registra autorización de implementación. El smoke y la decisión sobre el candidato técnico corresponden al autor.
