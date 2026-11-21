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
