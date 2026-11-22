# GeoCeDG — Coste de un cambio geométrico real tras R1 (D-V6)

**REGISTRO TÉCNICO — PENDIENTE DE REVISIÓN AUTORAL.** `selfApproved=false`.
Decisión que lo origina: D-V6 del autor (2026-11-21). Línea separada de la revisión
normativa del uso condicionado: no introduce semántica nueva, no cambia ningún contrato y
no depende del ADR 0042.

## 0. Identidad

| Papel | Commit |
|---|---|
| Antes: candidato de reparaciones | `ab37fb5fff0fadca13f4488eaff85d7917276c42` |
| Después: optimización | `4cbe89a3e` |

Modelo: `models/regression/post-r1-locus-on-intersection/` — original del autor y variante
transportada versionada. Arnés: `tools/benchmark/post-r1-locus/run-harness.ps1`, escenarios
`profile` y `benchmark`. El «antes» se midió con el mismo arnés sobre un checkout limpio
del candidato de reparaciones (`-CheckoutRoot`). Entorno de referencia: OpenJDK 17.0.10,
Windows 11, 24 procesadores lógicos, una sola JVM por ejecución, sin ventana.

## 1. Perfil

Escenario `profile`: 4 cambios de calentamiento y 20 medidos de `excCyl` (+0,05 y vuelta),
con muestreo JFR en proceso y atribución inclusiva por marcadores de pila. Los porcentajes
son de muestras del hilo de eventos, inclusivos, y por tanto se solapan.

| Marcador | Original, antes | Original, después | Transporte, antes | Transporte, después |
|---|---|---|---|---|
| Mediana del grafo, con muestreo (ms) | 1 753 | 681 | 2 203 | 789 |
| p95 del grafo, con muestreo (ms) | 2 006 | 947 | 2 229 | 800 |
| `ReconstructibleLocusEvaluator2D.evaluate` | 91,2 % | 75,7 % | 93,1 % | 79,1 % |
| `ReconstructionSliceFingerprint2D.snapshot` | 66,2 % | 17,5 % | 70,8 % | 18,0 % |
| `ReconstructibleLocusEvaluator2D.<init>` | 42,7 % | 40,9 % | 48,5 % | 47,1 % |
| `getHybridExistenceCertificate` | 54,8 % | 55,9 % | 47,2 % | 48,5 % |
| `AlgoLocusBetweenMetricV2` | 68,5 % | 52,4 % | 73,5 % | 60,7 % |
| `AlgoLocusIntersectionV2.compute` | 10,2 % | 23,6 % | 7,1 % | 20,0 % |
| `GeoElement.getXML` | 11,6 % | 0,9 % | 10,8 % | 1,0 % |
| `DrawLocusV2` | 3,1 % | 3,2 % | 5,5 % | 4,8 % |

Atribución. Un cambio de `excCyl` publica una revisión de `o`, `w_2` y `a_3` (y de
`trHole` en la variante). Reconstruir `w_2` y `a_3` exige evaluar, dentro de su copia
aislada, loci anidados cuyo conductor es el mismo del consumidor: 488 856 evaluaciones
anidadas en seis cambios. En cada una, la copia del locus anidado volvía a calcular su
huella completa de slice, cuya parte de estructura serializa los elementos a XML y los
normaliza con expresiones regulares. El tiempo propio lo confirmaba: los marcos de
`java.util.regex` y de construcción de cadenas sumaban en torno a la mitad de las muestras.
La certificación, la métrica y la intersección aparecen altas en inclusivo porque son las
que provocan esas evaluaciones, no por su propio cálculo. La pintura no es relevante.

## 2. Optimización

**Reutilizar la parte de estructura de la huella dentro de las copias aisladas.**

- Una copia aislada de evaluación nunca se edita: la crea el evaluador, la rellena una vez y
  solo cambia de valores. Su estructura es, por tanto, constante mientras lo sean los
  objetos que la forman.
- `ReconstructionSliceFingerprint2D.StructureMemo` guarda el resumen de estructura junto a
  una clave barata y completa: por elemento, la identidad del objeto, su etiqueta, si está
  etiquetado y definido, su algoritmo padre, las etiquetas de salida del primer padre visto
  y el texto del ledger de tokens de los resultados de intersección. Con la misma clave se
  devuelve el resumen guardado; con otra, se recalcula.
- Los resúmenes de valores, selección y etiquetas se calculan siempre. Ningún resultado se
  guarda: solo un resumen de estructura. Por eso un cambio de valores nunca se pierde.
- El memo pertenece al productor del locus anidado **dentro de la copia** y solo existe en
  un macro-kernel. Un documento vivo no lo tiene y su huella no cambia en nada.
- Modo de verificación (`-Dgeocedg.fingerprint.verifyMemo=true` o
  `GEOCEDG_FINGERPRINT_VERIFY_MEMO=1`): recalcula siempre y lanza una excepción si el
  resumen reutilizado difiere. Con él activo pasan las 1 935 pruebas compartidas de locus.

Alternativas descartadas:

| Alternativa | Motivo |
|---|---|
| Caché compartida de resultados entre productores | excluida por D-V6 sin diseño aprobado; además acopla revisiones de objetos distintos |
| Compartir la copia aislada entre `w_2` y `a_3` | copia mutable compartida; excluida por D-V6 |
| Omitir la huella del locus anidado en la copia | deja de detectar un cambio real de la función anidada: geometría obsoleta |
| Reducir muestras del certificado o de la métrica | reduce exactitud; fuera de objetivo |

Pruebas discriminantes (`PostR1FingerprintStructureMemoTest`):

1. Un locus anidado del mismo conductor reutiliza su estructura en la copia (al menos seis
   reutilizaciones en siete muestras, tres cálculos como máximo) y el productor vivo no
   tiene memo.
2. Una definición anidada que sigue al conductor externo nunca queda obsoleta: los siete
   puntos evaluados coinciden a 10⁻⁹ con un oráculo analítico independiente del kernel y
   son funciones anidadas distintas entre sí.

## 3. Antes y después

Escenario `benchmark`, sin muestreo. Tiempo de actualización del grafo por cambio, en ms.

| Serie | Original, antes | Original, después | Transporte, antes | Transporte, después |
|---|---|---|---|---|
| `excCyl` regular, mediana (6 cambios) | 1 695,5 | 605,1 | 2 132,3 | 719,0 |
| `excCyl` regular, p95 | 1 705,3 | 626,6 | 2 162,2 | 723,4 |
| `excCyl` a través del mordisco (3,60 / 3,70 / 3,80), mediana | 1 704,6 | 592,9 | 2 145,4 | 699,9 |
| ídem, máximo | 1 729,6 | 644,4 | 2 193,6 | 720,3 |
| `excCyl` pérdida y recuperación (4,2 / guardado), mediana | 1 683,5 | 602,7 | 2 119,8 | 700,3 |
| ídem, máximo | 1 716,1 | 616,2 | 2 132,7 | 703,5 |
| `E_1` interior, mediana (21 pasos) | 59,0 | 51,7 | 63,6 | 53,4 |
| `E_1` salida y regreso | 56,6 | 50,7 | 62,2 | 51,7 |
| `E_1` junto al pliegue | 55,4 | 48,3 | 61,6 | 51,9 |
| `N`, mediana (21 pasos) | 23,9 | 23,7 | 32,4 | 29,3 |
| Apertura, tres cargas | 13 041 / 8 215 / 7 954 | 7 718 / 3 493 / 3 248 | 8 838 / 8 785 / 8 892 | 3 357 / 3 246 / 3 341 |
| Pintura tras un paso, mediana | ≈ 5 | ≈ 5 | ≈ 5 | ≈ 5 |

Objetivo D-V6 — cambios válidos representativos de `excCyl` por debajo de 1 s, también en
p95, tras calentamiento controlado: **cumplido** en las tres series y en los dos
documentos. El escenario `profile`, con la sobrecarga del muestreo, da también p95 por
debajo de 1 s (947 y 800 ms).

Igualdad de trabajo semántico. Los contadores de las dos ejecuciones son idénticos serie a
serie: mismas revisiones, reconstrucciones, evaluaciones y evaluaciones anidadas (por
ejemplo 488 856 para `w_2` y `a_3` en la serie regular), mismas resoluciones de
intersección. La optimización abarata cada evaluación anidada; no evita ninguna. Tras la
pérdida y recuperación todos los loci quedan publicados en ambos candidatos.

Qué no cambia: los pasos de `E_1` y `N`, que ya no publican revisiones desde las
reparaciones A, B y A′, mejoran solo marginalmente.

Salidas (ignoradas por Git) en `artifacts/benchmarks/post-r1-locus/`: `profile-01`,
`profile-02-memo`, `benchmark-dv6-before-ab37fb5f-01`, `benchmark-dv6-after-01`, cada una
con `run-metadata.json` (commit, árbol, limpieza del checkout y SHA-256 de las salidas).

## 4. Hallazgo F8

Defecto previo, encontrado al escribir la prueba 2. No lo causa este trabajo: se reproduce
igual en la base `9ccefdcfe`.

Construcción mínima:

```text
c  = Circle((0,0), 2)      E2 = Point(c)
k  = Circle((0,0), 0.6)    T  = Point(k)
QO = (2,0)                 QT = Midpoint(T, QO)
Mt = Midpoint(E2, QT)      Lt = LocusV2(Mt, E2)
HA = (4,0.1)  HB = (-3,0.2)  l = Line(HA, HB)
v  = Intersect(Lt, l)      RA = Intersect(v, "<token de una raíz>")
ZP = Midpoint(RA, T)       Zt = LocusV2(ZP, T)
```

Síntoma: al mover `T` por la secuencia de ángulos 0,4 → 1,1 → 2,0 el locus vivo `Lt` pierde
su definición; su reconstrucción falla con `IllegalArgumentException: Reconstructed
evaluator omitted durable dependency` sobre el identificador de `E2`, su propio conductor.
Depende de la historia: 1,1 → 2,0 sin el paso previo no falla. Sin el consumidor `Zt`
(solo `Lt`, `v` y `RA`) no ocurre. Sobre la secuencia 0,4 / 1,1 / 2,0 / −2,5 / −1,2 / 0,0 /
2,9 `Lt` queda definido en 0,4, 1,1 y −1,2 e indefinido en las demás, en la base y en la
rama.

Alcance conocido: composiciones en las que el conductor de un locus consumidor es entrada
no conductora de un locus anidado. El modelo del autor no la contiene.

**Triado y reparado** el 2026-11-21, con la autorización del autor de la misma fecha. La
causa no estaba en la reconstrucción: la carga de una copia aislada ordenaba la lista de
dibujables de la vista viva. Causa, reparación, alternativas, pruebas y ablación:
[documento de reparaciones, §1.6](post_r1_locus_repairs.md#16-f8--la-carga-de-una-copia-aislada-no-toca-la-vista-viva).

## 5. Límites

1. Medido sin ventana y con una JVM; la latencia interactiva en una ventana real no se ha
   medido.
2. El coste restante de un cambio real es trabajo semántico: unas 81 000 evaluaciones
   anidadas por cambio en cada uno de los dos loci del agujero. Reducirlo más exige compartir resultados entre productores o entre el
   certificado y la métrica del mismo locus, que D-V6 excluye sin diseño aprobado. Si el
   autor lo quisiera, la propuesta concreta sería un resultado de recorrido por revisión,
   propiedad del productor, leído por su certificado y su métrica; no se ha diseñado.
3. La primera carga de un documento sigue siendo la más lenta (≈ 7,7 s con la JVM fría).

## 6. Coste de la evidencia condicionada por revisión (incremento I0)

Medición del incremento I0 del uso condicionado (ADR 0042), con el protocolo real: el
testimonio del evaluador del slice y la política `conditioned-traversal/v1`. Escenario
`conditioned` del arnés (`tools/benchmark/post-r1-locus/run-harness.ps1 -Scenario
conditioned`), sobre el documento original y sobre el transportado; misma estación y mismas
reservas que el §5 (sin ventana, una JVM, una serie corta: telemetría, nunca umbral).

| Locus | Evaluaciones | Celdas | Regiones | Primera petición | Petición repetida |
|---|---|---|---|---|---|
| `w_2` | 353 a 373 | 176 a 186: 132 a 136 tramos, 40 a 46 ausencias, 4 bandas de transición | 2: `[-2,76; -1,82]` y `[1,82; 2,76]` | 0,41 a 0,50 s | 0 ms |
| `a_3` | 377 a 385 | 188 a 192: 142 a 144 tramos, 40 a 46 ausencias, 4 bandas de transición | las mismas 2 | 0,43 a 0,48 s | 0 ms |
| `trHole` (transportado) | 257 | 128 tramos | 1: el periodo completo | 0,16 s | 0 ms |
| `o`, `o_1`, `e_2`, `s_2` | — | — | — | no publican evidencia: su certificado cubre el dominio | — |

Lectura:

1. **Se calcula una vez por revisión y solo si alguien la pide.** Mover `E_1` o `N` no
   publica revisiones (reparaciones A y A′) y conserva la evidencia: coste nulo durante el
   arrastre. En I0 ningún consumidor la pide, de modo que el coste de un cambio real de
   `excCyl` no varía (670 a 780 ms en esta serie, sin pintura).
2. **Cuando se consuma**, un cambio real añadirá el coste de la evidencia de cada locus
   condicionado que se use o se dibuje: en el original ≈ 0,9 s para `w_2` y `a_3` juntos,
   sobre los ≈ 0,7 s de la actualización del grafo. Ese sobrecoste es trabajo semántico
   nuevo (unas 360 evaluaciones del slice por locus, ≈ 1,3 ms cada una). El certificado de
   la misma revisión ya gasta una exploración de 32 muestras y 24 bisecciones por transición
   que, para las bandas con evidencia condicionada, el dibujo deja de necesitar; hacerla
   perezosa es la compensación natural y se decide en el incremento I2, que es donde el
   dibujo empieza a consumir la evidencia. No se comparte ningún resultado entre productores
   (D-V6).
3. **Límite de trabajo.** Queda fijado en 1 024 evaluaciones por recorrido (≈ 1,3 s en el
   peor caso con el coste por evaluación de este modelo). Los tres loci gastan entre 257 y
   385. Lo que el límite no alcanza es `UNEXPLORED`, nunca un tramo.
4. **Clase de los tramos.** Los de `w_2` y `a_3` son `RESOLUTION_ONLY`: su slice lee,
   además del token de la raíz, una selección recta-cónica heredada, que no testifica
   separación; un valor de separación ausente rebaja la clase (contrato §3.2) y no cambia
   ningún permiso.

Hallazgo del banco con el protocolo real, corregido antes de cerrar I0: el primer criterio
de testimonio de una selección recta-cónica heredada exigía la permutación asentada
(`ReconstructionFidelity2D.isSettled`). En el modelo esas selecciones están, correctamente,
en el estado de compactación de su primera inicialización, que el evaluador reproduce. Con
aquel criterio `w_2` y `a_3` daban solo bandas de fallo y celdas sin explorar, y `trHole`
agotaba el límite de trabajo sin clasificar ninguna celda (2,6 s y 1,1 s por locus). El
testimonio usa ahora el estado de selección que la norma ya define como reproducible
(`locus-v2-certified-construction-model.md` §10.2: compactación, permutación fija o punto
ya incidente); una definición tangente o una permutación pendiente, que el cálculo siguiente
fijaría por proximidad, siguen sin estar establecidas.

## 7. Medición con el bloque 3B completo (candidato 1.2.2)

Medición de 2026-11-22 sobre `0f02c8f21` (I0 a I4T), con la máquina sin otra carga y el
escenario `conditioned` del arnés (`tools/benchmark/post-r1-locus/run-harness.ps1`), seis
cambios reales de `excCyl` por modelo.

| Modelo | Grafo por cambio | Evidencia de `w_2` por revisión |
|---|---|---|
| original | 589 a 708 ms | 393 a 472 ms |
| derivado con transporte | 723 a 778 ms | 391 a 414 ms |

- El coste del grafo no cambia con I1 a I4T respecto a la medida de la sección 3: buscar
  raíces en las regiones condicionadas no añade trabajo a un cambio en el que ninguna
  intersección lee un lugar condicionado, y el contenido de la evidencia se identifica por
  su contenido, no se recalcula.
- La evidencia sigue costando de 0,4 a 0,5 s por lugar con hueco y revisión, como en la
  sección 6, y se calcula cuando un consumidor la pide. Con los dos lugares con hueco del
  modelo dibujados, un cambio real de `excCyl` cuesta en total ≈ 0,65 s de grafo más
  ≈ 0,85 s de evidencia.
- **El objetivo de D-V6 (cambio real por debajo de 1 s) se cumple para el grafo y no para
  el total con la evidencia consumida por el dibujo.** Reducirlo exige no recorrer de nuevo
  lo que el cambio no altera (exploración perezosa o por celdas), que toca la identidad de
  contenido de la política aprobada y no se ha hecho sin un diseño aprobado. Queda como
  hallazgo para el autor, sin decisión tomada.
- Arrastrar el conductor no publica revisión y no recalcula la evidencia: su coste es nulo.

## 8. Memo de evaluaciones anidadas por instancia de evaluador (paso C5, decisión DN-3)

Decisión DN-3 del autor (2026-11-22,
[registro de la autorización](../validation/post_r1_1_2_2_corrections_author_authorization_record.md)).
Norma: [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), sección
«Corrections after block 3B». **REGISTRO TÉCNICO — PENDIENTE DE REVISIÓN AUTORAL.**

### 8.1 Qué se repetía

Cada evaluación del slice de un lugar con hueco (`w_2`, `a_3`) recalcula, dentro de su
copia aislada, una intersección con el lugar anidado `o` y dos longitudes sobre `o_1` y
`e_2`. Esos lugares anidados no dependen del conductor del consumidor: entre dos
evaluaciones de la misma revisión su copia no cambia, y sin embargo se volvían a muestrear
enteros. Medido con una sesión instrumentada, fuera del repositorio, sobre el candidato
`b2c5aad35`: 186 001 evaluaciones de `o` por recorrido de `w_2` (≈ 512 por evaluación del
slice) y 23 273 de `o_1` y `e_2`; con un memo por instancia de evaluador, las realmente
calculadas bajan de 209 274 a 41 844 (−80 %) en el recorrido y de 82 046 a 28 018 (−66 %)
en el grafo.

### 8.2 Qué se ha hecho

`LocusNestedEvaluationMemo2D`: un memo de resultados de evaluación anidada, propiedad de
una instancia de `ReconstructibleLocusEvaluator2D` (una copia aislada, de una revisión, de
un productor). Lo leen y lo llenan solo las sesiones de servicio de las actualizaciones de
slice de esa instancia.

| Condición del autor | Cómo se cumple |
|---|---|
| Sin caché global ni objetos geométricos mutables compartidos | Un memo por instancia de evaluador, inalcanzable cuando ella lo es; guarda resultados de evaluación inmutables, ninguna copia ni objeto geométrico |
| Claves con todas las dependencias relevantes | Identidad del lugar anidado, revisión semántica, rama y parámetro canónico; el propio memo añade la instancia de evaluador. Un lugar anidado cuya definición sigue al conductor publica otra revisión |
| Vigencia de dependencias y de contexto | El memo muere con su evaluador, que es de una revisión. No se guarda ningún resultado cuyo cálculo encontrase una reentrada, una revisión incoherente, una sesión cerrada o un límite agotado |
| Presupuestos, diagnósticos, DAG, invalidez y determinismo | Una petición que el memo responde se carga al presupuesto y se cuenta como fallo de sesión igual que una calculada; ninguna clasificación depende de lo que el memo contenga. No toca revisiones, aristas ni publicación |
| Reproducción bit a bit | Resúmenes de resultados idénticos con y sin memo en todas las series (§8.3); pruebas `PostR1NestedEvaluationMemoTest` |
| Ejecución sin reutilización como oráculo | `geocedg.nestedEvaluationMemo=off` (o `GEOCEDG_NESTED_EVALUATION_MEMO=off`): no se crea memo. `verify` recalcula cada petición respondida y falla ante una diferencia |

No cambia la política `conditioned-traversal/v1`, la identidad de contenido de la evidencia
ni ningún contrato de resultado. Límite: 65 536 resultados por instancia; al llenarse no se
guarda más y las peticiones se calculan.

Regresión compartida completa (`gradlew :shared:common-jre:test --tests org.geocedg.*`,
2 517 pruebas) en los tres modos, **0 fallos en cada uno**: memo activado, `verify` y
`off`. Desktop (`PostR1*` y `PreG9BCu2AuthorModel*`, 78 pruebas) con el memo activado: el
único fallo es el ya conocido en la base `9ccefdcfe`.

### 8.3 Medición

Escenario `total` del arnés (`tools/benchmark/post-r1-locus/run-harness.ps1 -Scenario total
-NestedEvaluationMemo off|on`), añadido en este paso: en cada cambio real de `excCyl` mide
por separado la actualización del grafo, la evidencia condicionada de todos los lugares de
la nueva revisión y una pintura fuera de pantalla, y resume lo que publica cada lugar. JVM
OpenJDK 17.0.10 del proceso de pruebas, Windows 11, 24 procesadores, máquina sin otra carga,
4 cambios de calentamiento. Milisegundos, mediana / p95.

| Documento, serie (cambios) | Parte | Sin memo | Con memo |
|---|---|---|---|
| original, regular (20) | grafo | 596 / 606 | 541 / 614 |
| | evidencia | 849 / 865 | 530 / 602 |
| | pintura | 7 / 7 | 7 / 9 |
| | **total** | **1 449 / 1 479** | **1 096 / 1 224** |
| original, a través del mordisco (9) | total | 1 472 / 1 644 | 1 094 / 1 211 |
| original, pérdida y recuperación (6) | total | 1 415 / 1 443 | 1 038 / 1 136 |
| transporte, regular (20) | grafo | 729 / 879 | 640 / 667 |
| | evidencia | 994 / 1 084 | 601 / 619 |
| | pintura | 6 / 8 | 6 / 8 |
| | **total** | **1 723 / 1 932** | **1 253 / 1 279** |
| transporte, a través del mordisco (9) | total | 1 713 / 1 746 | 1 248 / 1 259 |
| transporte, pérdida y recuperación (6) | total | 1 710 / 1 747 | 1 227 / 1 298 |

- **Resultados idénticos.** El resumen SHA-256 de lo que publican los siete lugares en cada
  paso (firma de la estructura de existencia, celdas de la evidencia con su clase y sus
  extremos, 16 puntos evaluados por lugar, valores de `N` y `E_1`) coincide entre las dos
  ejecuciones en las ocho series, incluidas la transición topológica y la pérdida con
  recuperación, y coincide también con una ejecución anterior de la misma medición.
- **Mejora.** El total baja un 24 % en el original y un 27 % en el transportado; la
  evidencia, un 38 % y un 40 %.
- **El objetivo de D-V6 no se alcanza.** Con la evidencia consumida, el total sigue por
  encima de 1 s en los dos documentos: mediana 1,10 s y p95 1,22 s en el original, 1,25 s y
  1,28 s en el transportado. El grafo solo (0,54 y 0,64 s) sí lo cumple, como antes. No se
  declara alcanzado ningún objetivo de latencia interactiva; tampoco se ha medido en una
  ventana real.

Las cifras son de una estación y de series cortas: sirven para comparar las dos columnas,
no como umbral.

## 9. Estudio de la segunda optimización (paso C6). No implementada

La autorización pide estudiarla si el memo no basta, y no implementarla sin justificar su
alcance y su compatibilidad. Este apartado es el estudio; no hay código.

### 9.1 Dónde queda el tiempo

Perfil JFR del hilo de eventos con un prototipo equivalente al memo (fuera del repositorio,
sobre los JAR del candidato; porcentajes del total de un cambio con evidencia): recorrido
condicionado 49 %, certificado 28 %, métrica viva 14 %, construcción de copias y XML 7 %.
Por tipo de trabajo: trabajo propio de la intersección anidada 31 %, evaluación de las
copias 28 %, **resumen de huella del slice 18 %**, motor métrico 8 %, construcción de copias
y XML 10 %.

### 9.2 Las candidatas

| # | Optimización | Alcance | Ganancia estimada | Compatibilidad y riesgo |
|---|---|---|---|---|
| O2-a | **Comparar el estado exacto en vez de resumirlo.** Dentro de una copia aislada, un lugar anidado cuyo conductor es el del consumidor recalcula en cada evaluación las partes de valores, selección y etiquetas de la huella de su slice (cadenas y cuatro SHA-256) para concluir que nada cambió. Se guardaría, junto al resumen, el vector exacto del que sale el texto, y solo se recalcularía el texto si el vector difiere | `ReconstructionSliceFingerprint2D` y `ReconstructionExactState2D`; solo en copias aisladas, como el memo de estructura de D-V6 | −0,10 a −0,17 s por cambio | No cambia ninguna norma ni el valor de ninguna huella. El riesgo es de implementación: el vector debe cubrir exactamente lo que lee el texto, o un cambio real dejaría geometría obsoleta. Se valida con un modo de verificación que calcula siempre el texto y compara, como `geocedg.fingerprint.verifyMemo` |
| O2-b | **No repetir la resolución de identidad en la intersección anidada.** Cada evaluación del slice resuelve, en la copia, la identidad pública y los tokens de la intersección anidada, aunque solo necesita el punto de la raíz ya seleccionada | Resolvedores de identidad de intersección y libro de tokens, dentro de las copias | hasta −0,2 a −0,3 s | Alto: es el código de identidad, el de mayor riesgo del producto. Necesita diseño propio |
| O2-c | **Sincronizar valores en la copia existente** en vez de reconstruirla en cada revisión | `AlgoDependentPointLocusV2`, `ReconstructibleLocusEvaluator2D` | −0,1 a −0,15 s | El evaluador de una revisión es hoy inmutable (`locus-v2-semantics.md` v1.2). Probable enmienda normativa |
| O2-d | **Que el recorrido reutilice las evaluaciones del certificado** de la misma instancia | Certificador y recorrido condicionado | ≤ 19 % del recorrido (medido: 70 de 363 evaluaciones repiten parámetro) | El certificado tendría que guardar el testimonio de cada evaluación |

Todas las ganancias de esta tabla son estimaciones a partir del perfil; ninguna está medida.

### 9.3 Conclusión

O2-a es la segunda optimización que el informe de revisión identificó, y es compatible sin
cambio de norma. Pero su ganancia estimada no basta por sí sola: dejaría el total en torno a
0,93–1,0 s en el original y 1,1 s en el transportado. Para bajar de 1 s con margen en los
dos documentos haría falta además O2-b u O2-c, que tocan identidad o la inmutabilidad del
evaluador y piden una decisión propia. Por eso no se ha implementado O2-a en este paso: se
presenta al autor el conjunto, para que decida si el objetivo de 1 s con la evidencia
consumida se persigue en 1.2.2 y con qué palancas.

## 10. Medición en la aplicación empaquetada (paso C7)

Agente de humo fuera del repositorio, cargado por el `GeoCeDG.exe` **sin modificar** del
app-image del candidato `a4b03d049` (`JAVA_TOOL_OPTIONS`), con su runtime propio (Java
25.0.4), ventana real, preferencias y almacenes redirigidos fuera del sistema del autor. El
documento es el original del modelo. Cada cifra es el tiempo, en el hilo de eventos, de un
cambio real de `excCyl` (+0,05 y vuelta) más la evidencia condicionada de `w_2` y `a_3`;
la pintura en pantalla es asíncrona y no se incluye. Una sola ejecución de cada serie.

| Estado del documento | Milisegundos por cambio, en orden |
|---|---|
| Tal como se guardó | 1 288, 1 282, 1 309, 1 094, 1 116, 1 033, 1 050, 995 |
| Con una `LocusLength(w_2, P, Q)` | 1 345, 1 401, 1 283, 1 290, 1 315, 1 314 |
| Con esa longitud y una `Intersect(w_2, segmento)` | 7 866, 7 799, 7 944, 7 954, 8 039, 8 120 |
| Con la longitud, una fórmula, la intersección y un punto sobre su raíz | 7 874, 7 538, 7 910, 7 790, 7 849, 7 546, 8 451, 7 937 |

- En la aplicación real, el documento tal como se guardó cuesta lo mismo que en el arnés:
  en torno a 1,0–1,3 s por cambio real, por encima del objetivo.
- Una longitud sobre una región de `w_2` añade unos 0,3 s por cambio real.
- **Una intersección de `w_2` con un segmento añade unos 6,5 s por cambio real.** Es código
  del incremento I4 (búsqueda de raíces sobre las regiones condicionadas), ya presente en el
  candidato `b2c5aad35`, que no se había medido: la búsqueda evalúa el slice del lugar con
  hueco varios miles de veces, y cada evaluación cuesta del orden de un milisegundo. El
  memo del paso C5 abarata cada una de esas evaluaciones, no su número. Es una estimación
  de la causa; el tiempo es medido.
- El modelo del autor no contiene ninguna intersección sobre un lugar con hueco, de modo
  que su coste no cambia. Pero la función que I4 entrega, intersecar un lugar condicionado,
  no es interactiva hoy sobre un lugar de este coste por evaluación.

Reducirlo pide que los consumidores de un mismo lugar y revisión no repitan sus
evaluaciones (certificado, recorrido, intersección y métrica piden muchas veces los mismos
parámetros) o que la búsqueda de raíces parta de los nodos de la evidencia condicionada en
vez de volver a muestrear. Ninguna de las dos cosas está autorizada ni diseñada: la decisión
DN-3 cubre el memo por instancia de evaluador, que no alcanza a los consumidores del lugar
vivo. Queda como hallazgo para el autor.
