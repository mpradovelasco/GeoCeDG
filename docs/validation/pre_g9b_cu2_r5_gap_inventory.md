# PRE-G9B-CU2 — Inventario de los 34 huecos confirmados (D-CU2-T13-R5)

| Campo | Valor |
|---|---|
| Carácter | Análisis del agente para la decisión autoral D-CU2-T13-R5. No es evidencia de ejecución ni aprobación; `selfApproved=false` |
| Origen | Decisión D-CU2-T13-R6, punto 5: R5 no queda autorizada globalmente; presentar el inventario clasificado de los 34 huecos con su identificador normativo, obligatoriedad, evidencia existente, impacto y corrección mínima, y marcar los que resuelven R1, R2, R4 o R6 |
| Filas | las 34 de §3 de la [revisión de trazabilidad](pre_g9b_cu2_traceability_review.md) |
| Método | cuatro revisores de solo lectura, uno por grupo de filas, releyeron el requisito de la [matriz](pre_g9b_cu2_validation_matrix.md), las pruebas y el código; una verificación dirigida, también de solo lectura, contrastó cada afirmación de defecto de producto o de inconsistencia normativa (§3). No se ha ejecutado ninguna prueba nueva |
| Base | rama local `phase/pre-g9b-cu2-corrective` en `e9541c42e` (R1, R2, R4, refuerzo y corrección de H-T13-A) |
| Relacionado | [registro de R6](pre_g9b_cu2_r6_correction_record.md): H-T13-A corregido; [registro de R7](pre_g9b_cu2_r7_correction_record.md): H-T13-B corregido y H-T13-C clasificado; §7, contenido por categorías pedido por D-CU2-T13-R7 |

## 1. Clasificación y obligatoriedad

Clases de la decisión D-CU2-T13-R6:

1. ausencia de prueba de una propiedad implementada;
2. cobertura parcial o insuficiente;
3. defecto real de producto;
4. inconsistencia normativa;
5. problema exclusivamente de trazabilidad;
6. requisito fuera del alcance autorizado de `CU2`.

Una fila puede tener una clase principal y otras secundarias, entre paréntesis. «(3)» indica un
defecto confirmado por la verificación de §3; «(3?)», un indicio que necesita una ejecución para
confirmarse.

Las 34 filas son obligatorias. Son filas F, A o GC de una matriz de aceptación (cabecera de la
matriz), que solo declara informativos GC-7, el tiempo de GC-8 y la comprobación externa V-DXF-4,
y §8 del
[registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md) hace obligatorios los
parámetros críticos y los casos sintéticos exactos. Ninguna es solo de trazabilidad (5) ni queda
fuera del alcance de `CU2` (6).

Resumen por clase principal: 26 filas de cobertura parcial (2), 7 de ausencia de prueba (1) y 1
inconsistencia normativa (4, V-AVAIL-6). Hay dos defectos de producto confirmados y abiertos: la
causa tipada de un `Point(L, u)` sin resolver (V-LIN-4 y V-POINT-5) y la posición de las
componentes puntuales de una imagen por semejanza (V-POINT-6). R6 corrigió un tercero, H-T13-A, que
afectaba a V-COINC-3 y V-COINC-8. Tienen además una inconsistencia normativa secundaria GC-2, V-UND-1,
V-REV-EXACT-7, V-COINC-2, V-COINC-5, V-COINC-7 y V-TEX-1. La verificación de §3 refutó un posible
defecto (la propagación del fallo de V-E2-5).

## 2. Inventario

Abreviaturas de las clases de prueba (`PreG9BCu2*`): HC `HybridCertificationTest`, SST
`SelectionSettlementTest`, C3 `ClassV3ModelTest`, RET `RevisionExactnessTest`, SFC
`SliceFidelityCharacterizationTest`, REP `RepresentationTest`, CM `ConsumerMigrationTest`, LIN
`LineageTest`, AMC `AuthorModelCertificationTest`, AMR `AuthorModelRepresentationTest`, AMSF
`AuthorModelSliceFidelityTest`, AMSS `AuthorModelSelectionSettlementTest`, AME
`AuthorModelExportTest`. Tamaños: S, una prueba o una aserción; M, varias pruebas o una
construcción del modelo nueva; L, cambio de producto.

| Fila | Tipo | Clase | Evidencia existente | Impacto | Corrección mínima propuesta | R1, R2, R4, R6 |
|---|---|---|---|---|---|---|
| GC-1 | GC | 2 (1) | SST, sintético: revisión, cortes y contadores, incluido `CERTIFICATE_COMPUTATION`, sin cambio al entrar y salir del hueco. En el modelo, solo revisiones, sin `e_2` y sin hueco (C3 del modelo), e identidad del certificado de `e_2` | una revisión o una recertificación al cruzar un hueco del modelo pasaría inadvertida | prueba del modelo a excCyl 2,9 y 3,7: `N` dentro del hueco, en `t_c` y fuera, y `E_1` en tres posiciones; en los siete loci, revisiones, certificados, cajas y cortes propios y anidados sin cambio (M) | ninguna |
| GC-2 | GC | 2 (4) | instrumentación: revisión + 1 y contador mayor que antes, una cota inferior; HC cambia el radio sin contar después; V-CRIT-9 solo comprueba que no se reutiliza | certificaciones duplicadas por revisión pasarían inadvertidas; un cambio que reconstruye el corte de `e_2` cuesta una certificación propia y una anidada, y la puerta no dice cómo cuentan las anidadas (§3) | igualdad `certificados + 1`; en el modelo, por cambio y locus, cálculos de certificado ≤ revisiones ≤ 1, y 0 en los loci no afectados; fijar la regla de los anidados (S–M) | ninguna |
| GC-3 | GC | 2 (1) | V-INV-1 de R4: dibujo, zoom, imagen y consultas sin certificados, cajas ni cortes; SST y HC sintéticos. Sin contador: `LocusLength`, `Intersect`, `Point(L, u)`, DXF y LaTeX | un consumidor que recertificara pasaría inadvertido | prueba del modelo con contadores propios y anidados alrededor de esos cinco consumidores y de las tres exportaciones LaTeX: ninguna recertificación (M) | R4 en parte |
| GC-8 | GC | 2 (1) | AMC asevera los presupuestos con los parámetros del autor, las nueve mordeduras y los trece binary64; no en V-PER-3 (rbCone 8 y 10) ni en V-CRIT-9 | un agotamiento en esos valores no haría fallar ninguna prueba | aseverar «dentro del presupuesto» en V-PER-3 y en la clasificación de V-CRIT-9 (S) | ninguna |
| V-LVL-1 | F | 2 (1) | HC asevera todo salvo la autoridad sobre el DAG y la identidad; REP, que no se exporta; la exploración va aparte y fuera de la firma | bajo | recertificar `lw` con una política exploratoria distinta: firma, claves, linaje, identidad persistente, dependencias y huella idénticas (S) | ninguna |
| V-LVL-2 | F | 1 | solo objetos de valor; la evidencia no se persiste y la política está fuera de la huella, sin prueba | serializar la evidencia avanzaría revisiones sin detectarse | XML de `lw` sin evidencia, recarga con la misma huella y firma; otra política con la misma firma (S–M) | ninguna |
| V-UND-1 | F | 2 (4) | la causa `COMPUTATIONAL_DEPENDENCY_FAILURE` solo se inyecta en una prueba; los fallos reales salen `UNRESOLVED` con otras causas | la vía del productor no está probada y la causa declarada no tiene emisor; la norma no la exige por su nombre (§3) | locus sobre un `LocusLength` de una fuente no establecida: `UNRESOLVED` tipado y ninguna inexistencia (S); decisión: emitir la causa (M) o declararla reservada (S) | ninguna |
| V-PER-3 | A | 2 (1) | AMC: al menos dos bandas de `o_1` de anchura ≤ `10⁻⁹` con evidencia del parámetro del rayo y sin componente en su punto medio; no localiza los conductores (sondeo: `t* ≈ ±0,3280` con rbCone 8, `±0,7115` con 10) | una banda mal situada pasaría | `t*` con 50 dígitos: dentro de una banda, sin componente, y solo esas bandas (S) | ninguna |
| V-E2-5 | F | 2 | AMC asevera `EVALUATION_FAILED` en `t = 1` con un presupuesto reducido para todo el bloque; la prueba de V-AVAIL-2 admite más de un hueco y su Javadoc dice que rutas posteriores también fallan | la verificación de §3 descarta la propagación (el presupuesto forma parte de la clave de la métrica), pero ninguna prueba demuestra la localidad | fallo solo en `t = 1`, exactamente un hueco y al menos 32 conductores `VALID` en la misma revisión (S) | ninguna |
| V-REV-EXACT-3 | F | 2 | SFC: bits y evaluación del locus sintético; RET: `lr2` sintético fiel; AMSF compara `lr2` del modelo con el modelo desde cero sin aseverar la fidelidad; ninguna prueba compara `lr2` con `R2` viva bit a bit tras el ulp | residuo pequeño | fidelidad y bits de excCyl en la copia de `lr2` del modelo; evaluación de `lr2` igual a `R2` viva fuera de la banda (S) | R2 en parte |
| V-REV-EXACT-7 | A | 2 (4) | el producto registra la causa de cada reconstrucción; RET asevera `VALUES` + 1 y `STRUCTURE` al renombrar; ninguna prueba lee `SELECTION` ni `LABELS` | la causa tipada que exige `locus-v2-semantics.md` no se prueba; un cambio de etiquetado se tipa `STRUCTURE`; ver §3 | `SELECTION` + 1 sin `VALUES` ni `STRUCTURE` al asentar; causa de un cambio de etiquetado, tras decidir si debe ser `LABELS` (S) | R1 y R6 en parte (revisión, no causa) |
| V-REV-EXACT-8 | F | 2 | RET: un solo cambio sintético, sin certificados; V-DET-1 (R4) cubre la mordedura, la primera inicialización, deshacer, rehacer y reabrir | faltan la ida y vuelta de V-REV-EXACT-2, `lr2` con un ulp, los literales de V-REV-EXACT-4, el conductor en `t_c`, el renombrado y los contadores de cortes | ampliar la doble ejecución de RET a esos estados con certificado y contadores; en el modelo, la alternancia en `t = −1` y `t_c` con `lr2` (M) | R4 en parte |
| V-REV-EXACT-9 | F | 2 | RET caracteriza la pérdida del literal angular sin certificado; deshacer y rehacer solo con huellas; reabrir con certificados idénticos en V-REV-EXACT-1 | el caso con pérdida no verifica el certificado | antes de recargar, ningún certificado; después, el de un documento nuevo con el valor recargado; deshacer y rehacer ese literal (S) | R2 y R4 en parte |
| V-REV-EXACT-10 | A | 2 | RET: revisión de `LocusLength` y seis huellas distintas y fieles; sin `−1e-300` ni locus anidado | no está demostrado que el cambio exacto llegue a un locus anidado | añadir `−1e-300`, `−0.0` y la cadena anidada del caso de la vuelta de origen: revisión, huella, certificado y métrica nuevos (S) | ninguna |
| V-SEL-7 | F | 2 | C3: firmas idénticas en la configuración crítica; SST: estado `[0, 1]`; ninguna prueba deshace ni reabre un `[1, 0]` | la rama «si cambia» y el estado `[1, 0]` sin verificar; el «diagnóstico» esperado no está precisado | con `[1, 0]`: deshacer, rehacer y recargar; firma y certificado idénticos o revisión nueva con linaje de aparición; lo mismo en el modelo (M) | ninguna |
| V-LIN-4 | A | 1 (3) | el resolvedor emite `AMBIGUOUS` (solo posible dentro de la tolerancia del dominio, entre hijas de una división); ninguna prueba lo asevera | `AMBIGUOUS` sin prueba; el punto queda indefinido sin causa tipada (§3) | prueba de `AMBIGUOUS` (S); precisar «interseca» en la fila; decisión sobre la causa del punto (M) | ninguna |
| V-COINC-2 | A | 2 (4) | producto en el modelo de cajas y el certificador; sintético en HC y C3; en el modelo, `lr2` solo se compara por igualdad | sin evidencia en bb, bc y 3,7; los recuentos 49/55 de §1 son caracterización, no elemento esencial | prueba de escritorio con `lr2` en bb, bc y 3,7: causa, componentes locales con bordes de banda y cada muestra indefinida en la banda o en inexistencia certificada; registrar 49/55 sin fijarlos (M) | R2 en parte; depende de R7 |
| V-COINC-3 | F | 2 (3, corregido) | SST: compactación, orden de consultas, revisión y componentes de `lq`; `R2` creada en `t_c` y reabierta; la causa `SELECTION` existe pero ninguna prueba la lee | el par etiquetado no podía crearse en `t_c` (H-T13-A) | crear `{Q, R}` y por orden en `t_c`, con `lr` y `LocusLength`, también en el modelo; `SELECTION` + 1 y mismas componentes para `lq` y `lr2` (S y M) | R2 en parte; R6 corrige el defecto |
| V-COINC-5 | F | 2 (4) | sintético con `k = 30`; bordes de banda de `lr2`; modelo etiquetado por V-TR-1; a 3,7 solo el centro del corchete | falta `lr2` a 3,7; «salidas indefinidas en el hueco» choca con F-CU2-1 (la salida está definida hasta `1,6·10⁻⁷` dentro del hueco) | a 3,7, `lr2` `NOT_ESTABLISHED`, bordes de banda y pertenencia a la banda; reformular la fila (S) | R2 en parte; depende de R7 |
| V-COINC-7 | A | 2 (4) | `lr2` sintético; el corte etiquetado está definido donde se certifica existencia, pero la definición del vivo no se comprueba; el modelo omite las muestras `UNRESOLVED` | a 3,7, `t_c` cae en el hueco y la aserción es inaplicable; R2 recentró el muestreo sin que la fila lo diga | `Q` y `R` vivas definidas; en el modelo, toda discrepancia de `lr2` dentro de la banda; precisar el muestreo a 3,7 (S–M) | R2 en parte; depende de R7 |
| V-COINC-8 | F | 2 (3, corregido) | la huella incluye selección y etiquetado; firmas idénticas tras recalcular, deshacer, rehacer y reabrir (C3); certificados idénticos al reabrir | H-T13-A incumplía la fila; deshacer, rehacer y recalcular solo comparan designaciones | comparar el certificado antes y después de cada operación en las dos variantes; un cambio real que asevere `SELECTION` (M) | R2 en parte; R6 corrige el defecto y compara el certificado de `lq` en esos estados |
| V-POINT-4 | F | 1 (2) | posición y existencia en HC; el producto rechaza puntos en la inexistencia e implementa `UNAVAILABLE`, solo probado con una estructura fija; la política no ofrece gancho de fallo | el rechazo en los vecinos de `t*` y la vía `UNAVAILABLE` sin evidencia | evaluar en `t* ± k` ulp (S); fallo forzado con un gancho de prueba o una entrada métrica (M) | ninguna |
| V-POINT-5 | F | 2 (3) | solo estructuras fijas para longitud, ruta y declaraciones; el producto declara la componente en DXF, LaTeX y el inspector sin prueba | `Point(L, u)` queda indefinido sin causa (§3); la ruta de longitud 0 no se puede construir con posiciones binary64 | pruebas sobre V-POINT-1 y V-POINT-2 con el patrón de la prueba de declaraciones de exportación; exponer la causa en `Point(L, u)` (M y S) | ninguna |
| V-POINT-6 | F | 1 (3) | `COMPLETE` en HC; intersección solo con estructura fija; la limitación del locus dependiente está implementada; las semejanzas reutilizan la estructura de la fuente | la imagen hereda y exporta la posición de la componente puntual sin transformar, contra la norma de las semejanzas (§3) | pruebas de intersección, del locus dependiente y de las cuatro semejanzas; transformar la posición o declararla no disponible (M y S) | ninguna |
| V-CON-1 | F | 2 (1) | CM usa estructuras inyectadas; en el modelo, dibujo y DXF/LaTeX a 2,9 y 3,7; faltan rutas, intersecciones y exportaciones en bb y bc, e interacción sobre un arco | riesgo bajo por diseño, sin demostrar cerca del valor crítico | prueba de escritorio, siete loci × {2,9; 3,7; bb; bc}: cierre, código 70, `cycle`, ciclo completo, ubicación de una raíz en `±π` y arrastre por `±π` (M) | ninguna |
| V-CON-3 | F | 2 (1) | CM da `2π − 4` y `2π` con estructuras inyectadas, sin clase; en el modelo solo longitud 0 con recubrimiento vacío | valor y clase desconocidos en los arcos del modelo y en el ciclo con esquina; puede revelar un defecto | `LocusLength` de `o`, `v`, `w` y `a_1` a 3,7 y de `lq` y `lr` críticos frente a una cuadratura de precisión ampliada, con su clase de §25.1 (M) | ninguna |
| V-CON-4 | F | 2 (1) | CM: raíces interiores en un arco inyectado; tokens construidos a mano; la fase no se asevera | la identidad de las raíces de `Intersect` a través de una revisión real sin demostrar | `Intersect(o, recta)` con 3,7 → 3,85: orden, raíces interiores, selectores iguales tras la revisión, `EMPTY` solo con `COMPLETE` (M) | ninguna |
| V-CON-7 | F | 1 (2) | la prueba construye la imagen con las ramas de la fuente: igualdad tautológica; fuente abierta, solo traslación, sin simetría | bajo: la herencia es por construcción | las cuatro órdenes públicas sobre `lq` en mordedura: firma, maximalidad, cierre y claves de componente (S) | R4 solo para el determinismo |
| V-B06-1 | F | 1 | AMR y REP miden el giro con bucles no cíclicos; el vértice de cierre nunca se mide; el teselador sí refina de forma cíclica | bajo | medir de forma cíclica en los dos auxiliares (S) | ninguna |
| V-B06-3 | F | 2 (1) | AMR: seis loci en el estado del autor y un «DPI» simulado; la prueba de R4 asevera certificados iguales y presentación distinta, no vértices | invariancia del trazo sin demostrar en los arcos y en el vértice singular | extender a la primera familia en 3,7, 3,85 y bc y a `lq` y `lr` críticos (S) | R4, relación indirecta |
| V-B07-2 | F | 2 (1) | REP: solo el caso sintético y el extremo inferior; en el modelo, solo vértices extremos a ≤ 0,75 px | el extremo superior y el modelo sin verificar | la misma comprobación en ambos extremos, en el caso sintético y en 3,7, 3,85 y 3,9 (S–M) | ninguna |
| V-B07-4 | F | 1 | REP inspecciona los datos de dibujo; `DrawLocusV2` dibuja punteado con alfa 128 sin prueba | bajo: un cambio de estilo pasaría inadvertido | dibujar `lx` y `lr2` con un gráfico registrador y aseverar trazo punteado y alfa 128 (S) | ninguna |
| V-AVAIL-6 | F | 4 (2) | REP asevera `COMPLETE`/`FAILED`, la causa y vértices evaluados; el dibujo no lleva clase de garantía (§3) | la aserción no se puede verificar tal como está escrita | precisar la fila («coordenadas iguales bit a bit a las del evaluador, sin mejorar su clase») y probarla (S); o propagar la garantía numérica del evaluador (M, decisión autoral) | ninguna |
| V-TEX-1 | F | 2 (4) | AME cuenta cierres de `o` y `e_2`; compila PGF y PSTricks; Asymptote nunca se compila y, sin herramientas, la prueba pasa; la integración continua no instala TeX (§3) | el documento Asymptote nunca se valida; sin TeX la fila pasa sin compilar | topología de los siete loci y de `lq` y `lr` críticos; compilar Asymptote; hacer visible la falta de herramientas; precisar la orden en la fila (M) | ninguna |

## 3. Defectos e inconsistencias contrastados

Una verificación de solo lectura contrastó cada afirmación de defecto o de inconsistencia de los
cuatro lotes con el código, las normas y las pruebas de `e9541c42e`. No se ha ejecutado nada; donde
hace falta una ejecución para demostrarla, se indica cuál.

| Afirmación | Veredicto | Evidencia | Clase |
|---|---|---|---|
| V-LIN-4 y V-POINT-5: un `Point(L, u)` cuyo selector o posición no se resuelve queda indefinido sin causa tipada | confirmada | `AlgoSemanticLocusPoint2D.java:326` devuelve `null` y descarta el estado tipado, y `:159` deja el punto indefinido; si falla el enlazador (`:182-185`), el enlace no se guarda, contra el Javadoc de `:271`; `LocusMetricPositionBinder2D.java:63-64` convierte `AMBIGUOUS`, `NO_CORRESPONDENCE` y `NOT_CONTAINED` en `null` y `:73-82` informa `POSITION_OUTSIDE_DOMAIN`; ningún otro canal expone la causa. La norma (`locus-v2-existence-components.md:492` y `:528-530`) exige «its point undefined with a typed cause» | 3 |
| V-POINT-6: la imagen por semejanza de un locus con componente puntual conserva la posición de la fuente | confirmada por lectura | `AlgoLocusSimilarityTransform2D.java:101` reutiliza la estructura de la fuente, cuya posición es `F(t*)` en coordenadas de la fuente (`HybridExistenceCertifier2D.java:1663-1665` y `:2266-2270`); la exportación la escribe (`SemanticCurveExportAdapter2D.java:792`), y llega al manifiesto DXF y a los comentarios LaTeX. La norma (`locus-v2-similarity-transformations.md:357-358`) dice «whose position enclosure is transformed». Ejecución que lo demuestra: tangencia exterior y `Translate(lq, (1, 0))`, comparando las dos posiciones y el mensaje exportado | 3 |
| V-E2-5: el fallo de la métrica de `w_1` en un conductor se propaga a otros | refutada | el presupuesto forma parte de la clave del índice métrico (`LocusMetricIndexKey2D.java:56` y `:140`; `LocusMetricEngine2D.java:199-200`) y los fallos se recuerdan por clave completa (`LocusMetricIndex2D.java:67-75`); el envoltorio de la prueba solo agota el presupuesto a ±4 ulp de `π/2`; el teselador marca un hueco por racha de fallos. La prueba es débil (admite más de un hueco) y su Javadoc lo exagera | 2 |
| V-REV-EXACT-7: un cambio de etiquetado se tipa `STRUCTURE` | confirmada | la parte estructural de la huella incluye el XML con las etiquetas (`ReconstructionSliceFingerprint2D.java:184-187` y `:204-208`; `AlgoElement.java:1594`) y se compara antes que la de etiquetado, que solo guarda qué salidas están etiquetadas (`:276-280`, `:138-139`); `LABELS` solo puede salir de algoritmos ocultos al XML (`AlgoElement.java:1326`). La norma (`locus-v2-semantics.md:299-301` y `:307-308`) pide distinguir con una causa tipada una transición real de un recálculo equivalente, sin nombrar la causa | 4 |
| V-UND-1: `COMPUTATIONAL_DEPENDENCY_FAILURE` no tiene emisor | confirmada | solo aparece en `LocusCoverageCause2D.java:45` y en una prueba que la inyecta; el fallo real más próximo se tipa `NOT_CERTIFIED` (`LocusMetricFunctionStructure2D.java:297-299`). La norma (§15.5, `locus-v2-existence-components.md:452-457`) y la fila piden `UNRESOLVED` con causa tipada, sin nombrar esa causa | 2 (4) |
| V-AVAIL-6: el dibujo no lleva la clase de garantía de las coordenadas | confirmada | los vértices dibujados no la guardan (`LocusRenderData2D.java:93-98`) y el teselador no lee la calidad de la evaluación (`LocusComponentTessellator2D.java:716-733`); la norma de presentación (`locus-v2-presentation.md:248-250`) solo pide la fidelidad por componente con sus causas; las exportaciones sí declaran una garantía de aproximación | 4 |
| V-TEX-1: Asymptote no se compila y, sin herramientas, la prueba pasa | confirmada | solo se compilan PGF y PSTricks (`PreG9BCu2AuthorModelExportTest.java:416-441`); de Asymptote solo se cuentan los cierres (`:384-387`); sin herramientas se imprime «UNAVAILABLE» y la prueba pasa, sin omitirse; `.github/workflows/verify.yml` no instala TeX | 2 (4) |
| GC-2: las certificaciones anidadas | confirmada; la puerta no dice cómo cuentan | certificar `e_2` lee las estructuras métricas de su corte (`AlgoDependentPointLocusV2.java:383`), que certifican la copia de `o_1` del corte (`AlgoLocusBetweenMetricV2.java:283`), y cada reconstrucción del corte crea una copia nueva (`ReconstructibleLocusEvaluator2D.java:176-185`). Un cambio no conductor que reconstruye el corte de `e_2` cuesta una certificación propia y una anidada, más una de `o_1` vivo si le afecta; un cambio de `v_1` recertifica la copia aunque `o_1` no cambie. El registro de verificación de `39741a0b` midió «7 certificados y 1 anidado por paso». Ejecución que lo cuantifica: contadores propios y anidados de `o_1` y `e_2` al cambiar rbCone de 5 a 5,1 y al cambiar `v_1` | 2 (4) |

De aquí salen siete decisiones autorales, previas a cualquier corrección:

1. la causa tipada de un `Point(L, u)` sin resolver: cambio de producto acotado (M);
2. la posición de las componentes puntuales de las imágenes por semejanza: cambio de producto
   (S–M);
3. `STRUCTURE` frente a `LABELS` para un cambio de etiquetado: precisión normativa, o cambio de la
   huella, que alteraría las huellas existentes;
4. emitir `COMPUTATIONAL_DEPENDENCY_FAILURE` o declararla reservada (V-UND-1);
5. la redacción de V-AVAIL-6, o propagar la garantía numérica al dibujo;
6. la regla de las certificaciones anidadas en GC-2;
7. el tratamiento de Asymptote y de la falta de herramientas TeX en V-TEX-1.

## 4. Relación con R1, R2, R4 y R6

- **R1** (V-POINT-3) cubre en parte V-REV-EXACT-7: revisión y linaje al renombrar, sin causa tipada.
- **R2** cubre en parte V-REV-EXACT-3, V-REV-EXACT-9, V-COINC-2, V-COINC-3, V-COINC-5, V-COINC-7 y
  V-COINC-8, pero no resuelve ninguna fila entera.
- **R4** cubre en parte GC-3 (V-INV-1), V-REV-EXACT-8 y V-REV-EXACT-9 (V-DET-1) y roza V-B06-3; no
  resuelve ninguna fila.
- **R6** corrige el defecto que impedía V-COINC-3 y V-COINC-8 (H-T13-A). Su prueba asevera, para
  `lq`, el certificado tras reabrir, recalcular, deshacer y rehacer, y la revisión que publica el
  cambio de etiquetado, sin su causa tipada. Las dos filas siguen abiertas por los elementos que
  indica §2. V-COINC-2, V-COINC-5 y V-COINC-7 dependen además de la banda (D-CU2-T13-R7).

## 5. Erratas de la revisión de trazabilidad

- V-LVL-1 es de tipo F, no A (matriz §3), y V-PER-3 es de tipo A, no F.
- V-B07-4 es de tipo F, no A.
- V-CON-7: la cita no es parcial sino nominal; la prueba construye la imagen con las ramas de la
  fuente, así que la igualdad de firmas es tautológica.
- V-REV-EXACT-3 y V-REV-EXACT-8: R2 y R4 ya cubren parte de lo que §3 de la revisión da por
  ausente.

## 6. Propuesta para la decisión

La decisión es del autor. Si elige completar las filas, el agente propone agruparlas así, de menor
a mayor coste y con la regla de defectos de R2 y R4 (caracterizar, corregir dentro del perímetro o
escalar):

| Grupo | Filas | Naturaleza | Tamaño |
|---|---|---|---|
| G1. Aserciones que faltan sobre construcciones ya probadas | GC-8, V-LVL-1, V-PER-3, V-E2-5, V-REV-EXACT-3, V-REV-EXACT-9, V-REV-EXACT-10, V-POINT-4 (vecinos de `t*`), V-CON-7, V-B06-1, V-B06-3, V-B07-2, V-B07-4 | pruebas; sin riesgo de producto conocido | S cada una |
| G2. Contadores, consumidores y estados del modelo | GC-1, GC-2, GC-3, V-LVL-2, V-REV-EXACT-8, V-SEL-7, V-CON-1, V-CON-3, V-CON-4, V-TEX-1 (topología) | pruebas de escritorio sobre el modelo; V-CON-3 puede revelar un defecto de la métrica | M cada una |
| G3. Salidas en raíces coincidentes | V-COINC-2, V-COINC-3, V-COINC-5, V-COINC-7, V-COINC-8 | pruebas; dependen de la banda que fije D-CU2-T13-R7 | M |
| G4. Precisión normativa o cambio de producto | V-UND-1, V-LIN-4 y V-POINT-5 (causa del punto), V-POINT-6 (posición de las imágenes), V-AVAIL-6 (clase de garantía), V-REV-EXACT-7 (`STRUCTURE` frente a `LABELS`), GC-2 (certificaciones anidadas), V-TEX-1 (Asymptote y herramientas ausentes), V-COINC-5 y V-COINC-7 (redacción) | cada una necesita primero una decisión autoral: precisar la fila o autorizar el cambio de producto de §3 | S a L |

G1 y G2 no cambian el producto. G3 conviene hacerlo después de D-CU2-T13-R7, porque la banda
cambia sus resultados esperados. G4 no debería empezar sin decidir cada punto de §3.

## 7. Contenido y propuesta de resolución por categorías (D-CU2-T13-R7, punto 7)

La decisión D-CU2-T13-R7 mantiene R5 pendiente y pide, antes de autorizar G1 a G4, su contenido
completo y la propuesta de resolución separados en cuatro categorías. No autoriza aplicar los 34
cambios, y V-DET-1 sigue abierta hasta demostrar su cobertura conforme al contrato. Nada de esta
sección se ha implementado. Cada fila aparece en la categoría de cada una de sus partes; las
correcciones mínimas son las de §2.

### 7.1 Refuerzo de pruebas

Sin cambio de producto ni de norma. Si una prueba revela un defecto, se caracteriza y se escala, como
en R2 y R4.

| Grupo | Filas | Contenido |
|---|---|---|
| G1 | GC-8, V-LVL-1, V-PER-3, V-E2-5, V-REV-EXACT-3, V-REV-EXACT-9, V-REV-EXACT-10, V-POINT-4 (vecinos de `t*`), V-CON-7, V-B06-1, V-B06-3, V-B07-2, V-B07-4 | aserciones sobre construcciones ya probadas (§2) |
| G2 | GC-1, GC-3, V-LVL-2, V-REV-EXACT-8, V-SEL-7, V-CON-1, V-CON-3, V-CON-4, V-TEX-1 (topología) | pruebas de escritorio del modelo con contadores, consumidores y estados; V-CON-3 puede revelar un defecto de la métrica |
| G3 | V-COINC-2, V-COINC-3, V-COINC-5 y V-COINC-7 (sus aserciones), V-COINC-8 | salidas en raíces coincidentes; los resultados esperados de `lr2` son ya los de la banda de R7: banda estrecha en torno a las raíces coincidentes, componentes locales con bordes de banda y fallo cerrado tipado donde el cálculo heredado no es decisivo |
| V-DET-1 | V-DET-1 | extender la doble ejecución a las filas que aún no cubre (las de G1 a G3 cuando existan), para cerrarla conforme al contrato |

Propuesta: G1 primero; G3 cuando el autor confirme la banda de R7; G2 después, por coste.

### 7.2 Corrección de producto

Cada punto necesita una autorización expresa y una prueba que falle sobre el candidato anterior.

| Filas | Defecto o cambio | Alternativa normativa |
|---|---|---|
| V-LIN-4 y V-POINT-5 | un `Point(L, u)` sin resolver queda indefinido sin causa tipada (confirmado, §3) | ninguna: la norma ya la exige |
| V-POINT-6 | la imagen por semejanza conserva la posición de la componente puntual de la fuente (confirmado por lectura, §3) | declarar la posición no disponible en la imagen |
| V-UND-1 | emitir `COMPUTATIONAL_DEPENDENCY_FAILURE` | declararla reservada (§7.3) |
| V-AVAIL-6 | propagar la clase de garantía numérica al dibujo | precisar la fila (§7.3) |
| V-REV-EXACT-7 | tipar `LABELS` un cambio de etiquetado; altera las huellas existentes | precisar que es `STRUCTURE` (§7.3) |
| V-POINT-4 (fallo forzado) | un gancho de prueba para la vía `UNAVAILABLE` | usar una entrada métrica que falle, sin gancho |
| G3 (observación de R7) | la compactación de la primera inicialización cuenta con la tolerancia heredada de `10⁻⁵` y el modelo con el predicado exacto ([registro de R7](pre_g9b_cu2_r7_correction_record.md) §6.6); por confirmar con una sonda | si se confirma, banda tipada en esa franja |

Propuesta: V-LIN-4 y V-POINT-5, y V-POINT-6, son defectos confirmados de bajo riesgo; los demás,
según la decisión normativa de §7.3.

### 7.3 Modificación normativa

Precisiones de filas o de normas, para decidir antes de cualquier prueba que dependa de ellas.

| Filas | Precisión propuesta |
|---|---|
| V-REV-EXACT-7 | la causa tipada de un cambio de etiquetado (`STRUCTURE` frente a `LABELS`) |
| V-UND-1 | `COMPUTATIONAL_DEPENDENCY_FAILURE` emitida o reservada |
| V-AVAIL-6 | «coordenadas iguales bit a bit a las del evaluador, sin mejorar su clase» |
| GC-2 | la regla de las certificaciones anidadas por revisión |
| V-TEX-1 | compilar Asymptote, hacer visible la falta de herramientas TeX y precisar la orden |
| V-COINC-5 y V-COINC-7 | redactar las filas con la precisión F-CU2-1 de R7: una salida etiquetada está definida más allá del extremo exacto (exceso de evaluación); la segunda salida sin etiqueta tiene su banda tipada; el muestreo de V-COINC-7 a 3,7 |
| V-LIN-4 | precisar «interseca» |
| §10.3 del modelo de construcción certificada | opcional y editorial: la banda está acotada por predicados conservadores del cálculo heredado sobre entradas de primer nivel (registro de R7, §4) |

### 7.4 Deficiencias de trazabilidad

Sin efecto sobre el producto ni sobre las normas.

- Las erratas de §5: tipos de V-LVL-1, V-PER-3 y V-B07-4; la cita nominal de V-CON-7; la cobertura
  parcial de V-REV-EXACT-3 y V-REV-EXACT-8 por R2 y R4.
- V-COINC-7: el recentrado del muestreo de R2 no consta en la fila.
- La tabla de §6 de la matriz debe citar cada prueba nueva cuando se añada.

Propuesta: corregirlas juntas en la revisión de trazabilidad y en la matriz, antes que el resto, por
no tener riesgo.
