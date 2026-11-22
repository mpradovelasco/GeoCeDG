# GeoCeDG 1.2.2 — informe final de la intervención correctiva final

| Campo | Valor |
|---|---|
| Carácter | **Informe técnico para revisión del autor.** No aprueba nada. `selfApproved=false`. Un resultado automatizado aceptado no es aprobación autoral |
| Autorización | [registro de la autorización](post_r1_1_2_2_final_intervention_author_authorization_record.md), instrucción del autor de 2026-11-22 |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| Candidato de partida | `a4b03d049eeccbc7ad8c48d7e359220d2545d23b` |
| **Candidato final** | `bb341874b5ada13aebce89c3aec4f7f998b26e16`, árbol `c6649825f28c4eabce97427b7f2477bde15e517e` |
| Versión y fecha | GeoCeDG 1.2.2, `2026-11-22` |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`, sin tocar; etiquetas de versión `v1.2.0` y `v1.2.1` |
| No ejecutado | `FINAL`, promoción a `main`, etiqueta `v1.2.2`, release, instalación de MSI o EXE: nada de ello está autorizado |

## 1. Resumen

1. **C2 está resuelto con la alternativa α en su alcance mínimo.** Las raíces de `Intersect`
   sobre una pieza declarada se identifican por lo que la pieza declara y no por su lugar
   en la lista. El defecto demostrado (escenario B del
   [registro de la contradicción](post_r1_1_2_2_c2_pair_identity_contradiction_record.md))
   ya no se produce, ni en pares ni en una fuente contra un objetivo. Un token nombra su
   raíz o queda sin resolver; no pasa a otra raíz por un cambio de lugar.
2. **El sobrecoste de 6,5 s que comuniqué no era de `Intersect`.** Medido por consumidor,
   con instrumentación, `Intersect(w_2, segmento)` añade en torno a 0,9 s por
   cambio real. Los ≈ 7 s los añade `LocusLength(w_2)`, la longitud total de un lugar
   condicionado, que estaba en la misma serie de mi medición anterior. **Mi atribución era
   errónea** y la corrijo aquí y en el registro de rendimiento.
3. **`Intersect` mejora poco, y lo declaro como limitación cuantificada.** La mejora
   aplicada (el token de una entrada del libro se calcula una vez) reduce cada evaluación
   anidada sin cambiar ningún resultado. El coste restante de la intersección es el número
   de evaluaciones que fija la política de aislamiento, que no he tocado.
4. **Recomendación:** **aceptación técnica del candidato `bb341874b`**, con dos reservas que son decisión del autor y no defectos de esta intervención: el coste de la longitud total de un lugar condicionado (§7) y que las piezas gobernadas por deslizadores sigan siendo ranuras posicionales (§6, punto 2). Ninguna regresión nueva conocida.

## 2. Correcciones realizadas

### 2.1 C2 — identidad declarativa de las piezas declaradas

Commit `ee2baa2b5`. Norma:
[`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md)
§20, [`spline-v2-pair-materialization.md`](../../geocedg/specs/curves/spline-v2-pair-materialization.md)
§11 y la nota final de
[`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md).

| Caso que la instrucción pide separar | Qué ocurre ahora |
|---|---|
| Cambio legítimo de una ranura posicional (la pieza se mueve con sus deslizadores; el dominio declarado entero cambia sus extremos) | Sin cambio respecto de 1.2.1: la ranura conserva su significado posicional |
| Reordenación de piezas con identidad constructiva persistente (la lista se redefine y una pieza literal, o declarada por objetos con identidad duradera, cambia de lugar) | **Corregido**: el punto sigue sobre la raíz de su pieza, con su token |
| Sustitución real de una pieza | El punto queda indefinido; el token no pasa a la raíz de la pieza que ocupa su lugar; vuelve si la declaración vuelve |
| Cambio topológico que impide resolver temporalmente un token | Sin cambio: el token queda dormido y se reactiva |

Cómo:

- **Nombre de la pieza.** Lo que declara su elemento de la lista: identidades duraderas
  existentes, literales y operaciones anónimas sobre ellos. Nunca un índice, un lugar, una
  etiqueta, un valor de un objeto dependiente, una coordenada ni una referencia Java. No se
  introduce ninguna identidad nueva.
- **Dónde se lee.** Solo en el marco de las raíces de `Intersect`. Las claves de linaje de
  §15.6, los selectores de componente, los puntos sobre un lugar y todas las firmas quedan
  como estaban.
- **Documentos anteriores.** El marco bajo el que un resultado escribió sus bindings se
  conserva para la pieza que nombraba, en la primera resolución tras leer el documento y
  solo para los bindings activos. Los tokens no cambian. Probado con tres documentos
  escritos por `9ccefdcf`.
- **Persistencia.** Los marcos conservados son un quinto campo opcional del estado del
  libro. Un libro que no conserva ninguno se escribe exactamente como antes: un documento
  sin piezas declaradas, o cuyas piezas son ranuras, no cambia de texto.

El trabajo de `wip/post-3b-c2-structural-pair-lineage` se revisó y **no se ha promovido**:
usaba el linaje E-3, que es posicional para piezas declaradas, y adoptaba los bindings
heredados sin distinguir la lectura de un documento de la reconstrucción por una
redefinición. La rama queda como estaba.

### 2.2 Rendimiento

Commit `c64d3d8bc`. Detalle y cifras en
[rendimiento, §11](../architecture/post_r1_locus_real_change_performance.md).

- **Diagnóstico por instrumentación.** Escenario `intersect` del arnés: cada consumidor de
  `w_2` sobre una copia fresca del modelo, con mediana y p95 por parte, contadores de
  trabajo, árbol de llamadas muestreado y resumen de resultados.
- **Corrección aplicada.** El token de una entrada del libro es función de su material, sus
  textos de linaje y su encarnación. Se recalculaba, con un SHA-256, por cada entrada en
  cada confirmación y en cada búsqueda, en cada evaluación anidada de un lugar cuyo slice
  contiene una intersección pública. Ahora se calcula una vez por entrada. Ningún token,
  binding ni texto de estado cambia.
- **Lo que no he tocado.** La política de aislamiento de raíces (256 muestras por región y
  los refinamientos), ni la estimación de longitudes. No se implementó C6.

## 3. Rendimiento medido

Arnés, sin ventana, misma máquina y misma sesión, en reposo. Tiempo total por cambio real de
`excCyl` (grafo, evidencia condicionada y pintura), mediana / p95 en ms, y evaluaciones de
`w_2` por cambio.

| Consumidores de `w_2` | Antes (`a4b03d049`) | Después (candidato final) | Evaluaciones |
|---|---|---|---|
| Ninguno | 1 141 / 3 433 | 1 117 / 3 374 | 16 |
| `LocusLength(w_2, P, Q)` y una fórmula | 1 505 / 1 600 | 1 432 / 1 525 | 337 |
| `LocusLength(w_2)`, longitud total | 8 037 / 8 251 | 7 622 / 7 830 | 5 722 |
| **`Intersect(w_2, segmento)`** | **2 011 / 2 068** | **1 863 / 1 962** | 792 |
| La intersección y un punto sobre su raíz | 2 064 / 2 396 | 1 886 / 2 010 | 792 |
| Todos juntos | 10 479 / 10 714 | 9 327 / 9 448 | 6 819 |

- **Sobrecoste de `Intersect`:** de ≈ 0,87 s a ≈ 0,75 s por cambio real (−14 %). No es la
  mejora material que la instrucción pedía sobre 6,5 s, porque los 6,5 s no eran suyos.
- **Sobrecoste de la longitud total:** de ≈ 6,9 s a ≈ 6,5 s. No se ha actuado sobre ella.
- El p95 de la fila sin consumidores es el máximo de la serie, que tiene un cambio lento
  al principio en las dos mediciones.
- En el 73 % del tiempo de la intersección está el aislamiento de raíces: una malla de 256
  subdivisiones por región (514 evaluaciones) y unas 260 de refinamiento, a ≈ 1,2 ms por
  evaluación de este lugar.

Los resúmenes de resultados de cada serie son idénticos antes y después: en las configuraciones sin intersección, los del arnés completo; en la que tiene la intersección y el punto, los de una comparación aparte sin el token, que nombra el objeto resultado de cada ejecución (series regular, a través del mordisco y pérdida con recuperación), con un único token por serie.

## 4. Verificaciones

Todas sobre el candidato final `bb341874b`, salvo donde se indica.

| Verificación | Resultado |
|---|---|
| Pruebas focales de C2 | `PostR1DeclaredPieceIdentityTest`, 13 de 13: cambio de lugar, sustitución y regreso, ranuras con deslizadores, dominio entero, Undo y Redo, reapertura, copia de cierre, tres documentos escritos por `9ccefdcf`, lista mixta, una fuente contra un objetivo y lectura estricta de los marcos conservados. `PostR1DeclaredPieceDesktopTest`, 1 de 1: documento nativo `.cedg` |
| Oráculo de rendimiento | Resúmenes de resultados idénticos antes y después en las seis configuraciones del arnés (§3) |
| Regresión compartida completa | 2 530 pruebas, 0 fallos (2 517 anteriores y las 13 nuevas). Ejecutada sobre el árbol de trabajo inmediatamente anterior al commit; el candidato difiere solo en comentarios y Gradle da la tarea por actualizada, con las mismas clases |
| Regresión Desktop completa | 1 570 pruebas, 1 fallo, sobre `d96214af2` (el candidato añade solo el inventario): el fallo histórico de §5 |
| `STATIC` | `verification-25453226beec415188f867c42786955f`, `ACCEPTED / COMPLETE`, código 0 |
| `PHASE -Phase POST-R1-LOCUS` | `verification-131dd169fee94307b094fa9a27ec1d6b`, `ACCEPTED / COMPLETE`, código 0 |
| `INTEGRATION` | `verification-334f2df480254c4694087264e38c0561`, `ACCEPTED / COMPLETE`, código 0 |
| `PACKAGING` | `verification-e775785b7a0f484f9f28307a1bafaf2e`, `ACCEPTED / COMPLETE`, código 0. Un primer intento, `verification-d15c71464b164d94a40d5a1937a814f5`, fue rechazado (`REJECTED_VERIFICATION_CORE`) por lanzarlo desde una unidad sustituida; se repitió desde la ruta real del mismo árbol, sin cambiar nada |

**Paquetes** en `artifacts/packaging/windows-1.2.2-nc-candidate-3` (carpeta ignorada por Git), `source_revision` = `bb341874b5ada13aebce89c3aec4f7f998b26e16`, perfil NC:

| Paquete | SHA-256 |
|---|---|
| `GeoCeDG-1.2.2-windows-x64-nc.zip` | `7b0f5075db016a3418a319825404add4dc49d751cb32febfef50ca6d2ba58338` |
| `GeoCeDG-1.2.2-windows-x64-nc.msi` | `851b2f239c2cdce015ebeaf6dca2fe84e03a977791ac247842ee59d41495260d` |
| `GeoCeDG-1.2.2-windows-x64-nc.exe` | `71ba0bae517b6023ce70b87562ec2221b1d758d82a9633a13c98e1d38ff45dce` |

Se construyeron desde un árbol limpio del candidato y se copiaron a esa carpeta, con las sumas comprobadas tras la copia. Los paquetes de los candidatos anteriores se conservan. **Ningún instalador se instaló.**

**Humo del ejecutable real** (`GeoCeDG.exe` del app-image, sin modificar, Java 25.0.4 propio, ventana real, preferencias y almacenes redirigidos fuera del sistema del autor), con el modelo del autor, en dos arranques:

| Comprobación pedida | Resultado |
|---|---|
| Apertura del documento | `GeoCeDG 1.2.2`, fecha `2026-11-22`; `w_2` definido, 2 regiones, 176 celdas |
| Actualización tras cambios de parámetros | La longitud cambia de 1,31400 a 1,31035 y vuelve; el punto sigue definido con su token |
| `LocusLength` condicionado definido | `LocusLength(w_2, P, Q)` = 1,3140010177874024, clase `ESTIMATED`, con ruta; una fórmula sobre él, definida |
| Intersección condicionada con punto dependiente | 1 raíz; punto definido en (18,2773; −3,9505) |
| Guardado, reapertura y recuperación de identidades | Guardado; **segundo arranque** sobre el documento guardado: longitud, fórmula y punto definidos, con el token, la longitud y las coordenadas idénticos bit a bit |
| Recuperación tras invalidez temporal | En `excCyl = 4,2` el lugar, la longitud y el punto quedan indefinidos; al volver, definidos con los mismos valores y el mismo token |

El app-image no cambió (433 ficheros, mismo resumen antes y después). Tiempos por cambio real en la aplicación empaquetada, en ms: documento tal como se guardó, 829–1 035; con una longitud, 1 061–1 134; con la longitud, la intersección y su punto, 1 619–1 699; añadiendo la longitud total, 6 198–6 355. La intersección añade, pues, ≈ 0,55 s en el ejecutable real, y la longitud total ≈ 4,6 s.

Observación del humo, sin verificar en la interfaz: al eliminar por el kernel el número `LocusLength(w_2)`, su resultado rico auxiliar siguió en el documento, se guardó y siguió costando en el segundo arranque (6 239–6 514 ms por cambio). Es la estructura de resultado rico y adaptador escalar anterior a esta intervención; no he comprobado qué hace el borrado desde la interfaz.

## 5. Fallos históricos y regresiones nuevas

| Fallo | Origen | Estado |
|---|---|---|
| `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` (Desktop) | Histórico: falla en la base `9ccefdcf`. Solo lo ejecuta `FINAL` y la regresión completa | Sigue visible, sin excluir ni adaptar |
| Diagnósticos fijos del verificador (un hallazgo de gobernanza en `STATIC`) | Históricos | Sin cambio |

**Regresiones nuevas: ninguna conocida.** Las dos pruebas de estructura de las guías que fallaban en el candidato `b2c5aad35` se corrigieron en `a4b03d049` y siguen pasando.

Defecto ajeno encontrado y solo documentado: tras redefinir una lista literal, que no reconstruye la construcción, el libro conserva en la firma de proveedor la revisión de definición anterior de la lista hasta la siguiente lectura del documento, de modo que el XML guardado y el releído difieren en ese texto. Es evidencia de revisión, no identidad ni token; los puntos y los tokens se leen igual. No lo considero bloqueante.

## 6. Desviaciones

1. **Atribución errónea del sobrecoste**, mía, en el cierre anterior (§1, punto 2). El
   requisito de «mejora material sobre los 6,5 s» partía de esa cifra. Sobre el coste real
   de `Intersect` la mejora es pequeña.
2. **Alcance de C2 más estrecho que «toda pieza».** Una pieza gobernada por objetos con
   nombre y sin identidad duradera (un deslizador) no tiene identidad declarativa y sigue
   siendo una ranura posicional, como en 1.2.1. Darle identidad exigiría publicar
   identidades duraderas para esos objetos o usar etiquetas, y ni lo uno ni lo otro cabía:
   lo primero cambia el ciclo de vida de redefinición de esos objetos y lo segundo
   contradice `spline-v2-pair-materialization.md` §2.
3. **La longitud total de un lugar condicionado** no está en el alcance nominal y no la he
   modificado: la documento como defecto de rendimiento ajeno (§7).

## 7. Riesgos residuales y limitaciones

- **Piezas que son ranuras.** Redefinir una lista cuyas piezas dependen de deslizadores
  puede seguir cambiando qué pieza ocupa un lugar, como antes. No hay regresión, pero el
  invariante solo queda garantizado para piezas con identidad declarativa.
- **El dominio entero que gana piezas** conserva sus raíces mientras la construcción no se
  reconstruya entre medias; si la redefinición la reconstruye, los puntos quedan sin
  resolver (nunca reasignados) hasta que la declaración vuelva.
- **Bindings heredados inactivos** al escribir el documento no se adoptan: quedan sin
  resolver.
- **`LocusLength(lugar condicionado)` total cuesta ≈ 7 s por cambio real en este modelo** y
  devuelve un número indefinido. Es coste de 1.2.2 (bloque 3B), no una regresión de esta
  intervención, pero sí un riesgo de uso: un documento de 1.2.1 con una longitud total de
  un lugar que 1.2.1 no certificaba la recalcula ahora a ese coste.
- **`Intersect` sobre un lugar condicionado** añade ≈ 0,75 s por cambio
  real en este modelo. Reducirlo más exige cambiar la política de aislamiento, que es una
  decisión normativa.
- Las limitaciones ya declaradas en el cierre anterior siguen vigentes: el objetivo
  histórico de 1 s de D-V6 no se alcanza, y un salto en la dirección de la cuerda no es
  detectado por el recorrido.
