# PRE-G9B-CU1 — Informe del candidato técnico de implementación

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato técnico `CU1`: conformidad de evaluación, estabilidad de revisiones, composición anidada y política métrica v2 |
| Estado | `CU1 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; `CU2`–`CU10` no autorizados |
| Autorización | instrucción autoral «GeoCeDG — PRE-G9B-CU1 Author Approval of Metric Architecture and Implementation Authorization» de 2026-10-20: arquitectura métrica aprobada con las precisiones obligatorias N1–N5, decisiones D-CU1-1 a D-CU1-12 adoptadas e implementación acotada de `CU1` autorizada |
| Base autorizada | `e66ae4c06007f9aba47436a0c03f36608f0f4fd5`, árbol `c90ab8fb61648f231cb383b900af6b4b1e622f8f` (candidato de preparación revisado) |
| Rama | `phase/pre-g9b-cu1-implementation` (local, sin publicar) |
| Clase de verificación | `INTEGRATED_PHASE`: `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` sobre el candidato congelado |
| Identidad del commit y resultados de verificación | [registro de verificación](pre_g9b_cu1_verification_record.md), añadido por un commit documental descendiente (un commit no puede citar su propia identidad) |

## 1. Comprobación de la base

| Comprobación | Resultado |
|---|---|
| Identidad | `e66ae4c0…4fd5` existe y su árbol es `c90ab8fb…22f8f`, el del candidato de preparación revisado |
| Ascendencia | desciende del cierre de `CU0` `df675e9e`, de `d684e740`, de `636a0070` y de `main` `48a0775e` (= `origin/main`) |
| Contenido | especificaciones, ADR 0037, matriz de validación, hoja de decisiones y prompt canónico de la preparación, sin cambios posteriores |
| Espacio de trabajo | limpio al empezar; la rama se creó en la base exacta, no desde una referencia móvil |

## 2. Alcance ejecutado

La implementación siguió la estrategia S0–S10 de la preparación, con S0 primero.

| Etapa | Contenido | Commit |
|---|---|---|
| S0 | A1.1-R, A1.2-R y ADR 0037 integrados con N1–N5 en `locus-v2-semantics.md` (v1.2), `locus-v2-metrics.md` (v1.5, §25), `locus-v2-public-surface.md`, ADR 0037 y la matriz de validación | `435ed78c` |
| S1 | contadores públicos de trabajo anidado | candidato |
| S2 | estabilidad de revisiones por huella exacta del corte | candidato |
| S3–S4 | sesiones hijas con ámbito de corte y composición anidada | candidato |
| S5 | reutilización del índice métrico y memorización de fallos deterministas | candidato |
| S6 | política métrica v2, estimador v2 y admisibilidad condicionada por la tolerancia | candidato |
| S7–S8 | capacidades estructural (clase 1) y de envolvente certificada (clase 2) | candidato |
| S9 | pruebas de `CU1`, inversión controlada de los pines de `CU0` y conformidad de pruebas históricas | candidato |
| S10 | registro de la fase, verificación e informe | candidato y registro documental |

No se implementa nada de `CU2`–`CU10`. No cambian los certificados de existencia, la topología
de `e_2`, la materialización de tangencias, las políticas de intersección ni las herramientas. El
libro `geocedg_book` no se modifica. No hay publicación, etiqueta, release ni binario.

## 3. Cambios por etapa

### 3.1 S1 — Instrumentación (G3, G4, V-NEST-2)

`LocusInstrumentation2D` añade los recomputes equivalentes y cuatro contadores anidados: cortes
construidos, revisiones publicadas, estados métricos construidos y llamadas al evaluador. Una
copia anidada de un locus informa a un único propietario de corte, que los agrega de forma
transitiva. `LocusMetricInstrumentation2D` reenvía al mismo propietario las construcciones de
estados métricos. Los contadores son públicos: el pin B10.1 de `CU0` necesitaba reflexión.

### 3.2 S2 — Revisiones (A1.1-R, H03, D-CU1-8)

`AlgoDependentPointLocusV2` sustituye el testigo `|dependency-update=` por la huella exacta del
corte (`ReconstructionSliceFingerprint2D`, `reconstruction-slice-fingerprint/v1`): SHA-256 de los
XML de algoritmos y objetos del corte en orden de construcción, con los atributos numéricos de los
objetos que dependen del conductor neutralizados. El evaluador y la revisión se reconstruyen solo
si cambian el descriptor semántico o la huella. Un movimiento del conductor es un recómputo
equivalente: no publica revisión ni descarta estados métricos.

### 3.3 S3–S4 — Composición anidada (B10.1)

`ReconstructibleLocusEvaluator2D` deja de restablecer y recalcular el corte en cada punto
(`resetExternalState` + `updateConstruction` por evaluación). Cada evaluación se ejecuta con su
sesión como padre de los servicios anidados del corte (`runWithNestedServiceParent`). Los
servicios de métrica, vinculación de posiciones e intersección abren sesiones hijas con ámbito de
corte (`openServiceSession`). Una hija comparte con su raíz la pila de claves activas (ciclos y
profundidad) y el presupuesto; la raíz aplica su límite en sus propias fronteras, de modo que un
servicio anidado nunca queda interrumpido por el límite de su llamador.

### 3.4 S5 — Índice métrico

`LocusMetricIndex2D` memoriza los fallos deterministas de construcción bajo su clave completa y
los reproduce sin reconstruir. Un fallo causado por el límite de sesión del llamador
(`EvaluationWorkLimitException`) se propaga y nunca se memoriza. La clave del índice incluye la
firma de la política del estimador.

### 3.5 S6–S8 — Política métrica v2 (§25)

`LocusLength` y `LocusLength(L, A, B)` usan `LocusMetricPolicy2D.cu1PublicV2()`
(`cu1-metric-policy/v2`) y la capacidad compuesta `PolicyV2LocusMetricCapability2D`
(`cu1-public-metric-v2/v1`), que prueba en orden:

1. **Clase 1, estructural** (`StructuralLocusMetric2D`): imagen afín certificada `|a|·Δu` y
   semejanzas, con parámetros independientes del conductor, de conductores circunferencia, arco y
   segmento, leídas del programa de construcción capturado. La velocidad se encierra con cotas
   rigurosas de los valores singulares de la parte lineal (`MetricLinearPart2D`).
2. **Clase 2, envolvente certificada** (`CertifiedConeEnclosure2D`) sobre la fachada pública de solo
   lectura `CertifiedCurveEnclosure2D` de los modelos de intervalos existentes, con la cota del cono.
3. **Clase 3, estimador v2** (`LocusMetricEstimator2D`), diferencial si la fuente tiene derivada
   semántica y solo evaluador en otro caso.

Las cotas certificadas usan aritmética exterior (`MetricOutwardArithmetic2D`,
`MetricOutwardInterval2D`). La evidencia rica registra la tolerancia pedida, la envolvente
rigurosa disponible y las hipótesis de la estimación. Las imágenes por semejanza se miden a
través de su fuente (N1). Las consultas entre posiciones reutilizan el estado en caché (§25.9).
Las definiciones de implementación que §25 deja abiertas figuran en §25.12, como texto del
candidato.

## 4. Precisiones N1–N5

| Precisión | Implementación | Validación |
|---|---|---|
| N1 | la imagen se mide a través de su fuente con tolerancia `min(τ_fuente, τ_imagen/λ_sup)`, toma su clase y escala valor, error y envolvente por el factor encerrado rigurosamente; `λ = 0` da cero exacto; un factor no finito falla cerrado | V-TRANS-1, V-MET-STRUCT-3 |
| N2 | evidencia rigurosa, anchura, tolerancia pedida y admisibilidad se registran por separado; una envolvente ancha se conserva como evidencia; una estimación que cumple `τ` sigue siendo estimación; una estimación fuera de la envolvente se rechaza (`CONSISTENCY_NOT_ESTABLISHED`) | V-ADM-1, V-MET-ENCL-1 |
| N3 | las hipótesis de Richardson se registran con su escala (`d_min`, `h_probe`, factor de seguridad) sin afirmar orden; una cuerda que se anula retira la corrección y deja una estimación sin orden demostrado | V-MET-EST-1, V-ADV-4 |
| N4 | `τ = max(eps_abs, eps_rel·S)`; covarianza en el régimen relativo; tolerancia absoluta bajo el suelo; resultados independientes de la vista | V-INV-1, V-INV-1b, V-INV-2 |
| N5 | toda cota `CERTIFIED_ERROR_BOUND` resulta de cotas rigurosas de evaluaciones, normas, raíces y redondeos; una operación sin envolvente rigurosa falla cerrada; los casos adversarios nunca reciben clase 1 o 2 | V-CERT-RIGOR, V-ADV-1 a V-ADV-5 |

## 5. Decisiones D-CU1-1 a D-CU1-12

| Decisión | Aplicación |
|---|---|
| D-CU1-1 | cuatro clases (`MetricGuaranteeClass2D`); política `cu1-metric-policy/v2` |
| D-CU1-2 | las estimaciones son entradas ordinarias del DAG bajo el contrato de estimación; ningún certificador las usa como entrada rigurosa (V-DAG-1) |
| D-CU1-3 | la clase y el error se indican en el resultado rico existente; sin cambios generales de interfaz |
| D-CU1-4 | constantes fijadas dentro de los intervalos aprobados con evidencia de banco (§9) |
| D-CU1-5 | capacidad estructural incluida |
| D-CU1-6 | envolvente certificada incluida, mediante una fachada pública de solo lectura |
| D-CU1-7 | fallos deterministas memorizados con causa tipada (V-BUD-1) |
| D-CU1-8 | pruebas históricas que contradecían el contrato de revisión corregidas explícitamente (§7) |
| D-CU1-9 | pines de `CU0` invertidos de forma controlada con registro de referencias (§8) |
| D-CU1-10 | ADR 0037 y versiones normativas sujetos a las precisiones |
| D-CU1-11 | objetivos de rendimiento por contadores; los tiempos son informativos |
| D-CU1-12 | objetivos G4 y G6 medidos (§6) |

## 6. Puertas G1–G8

Las cifras proceden del banco `composition` de `CU0` ejecutado sobre el árbol de trabajo con el
código de producto del candidato (`tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario
composition`, salida ignorada en `artifacts/benchmarks/pre-g9b-cu/cu1-composition-run1`) frente a
su ejecución de `CU0` (`cu0-composition-run1`), y de las pruebas de §7. El registro de
verificación repite el banco sobre el commit del candidato. Los contadores son la medida
reproducible; los tiempos son informativos (D-CU1-11).

| Puerta | Evidencia | Resultado |
|---|---|---|
| G1 — sin reconstrucciones innecesarias | mover el conductor `E_1`: `o_1` sin revisión ni reconstrucción métrica (6 llamadas locales de la ruta de `w_1`), frente a 1 reconstrucción y ≈ 20 800 llamadas en `CU0`; un cambio de `excCyl`, fuera de la cadena, no toca `o_1` | cumple |
| G2 — sin resultados obsoletos | un cambio de `rbCone` o de `D_1` publica revisión y reconstruye; resultados idénticos bit a bit a los de un documento recién abierto con el mismo estado (V-CACHE-1) y a los de una construcción recién creada para cada clase de entrada (V-REV-2) | cumple |
| G3 — un corte por revisión | `e_2`: 1 construcción de corte tras la carga y 0 tras 29 consultas puntuales (V-SLICE-1) | cumple |
| G4 — trabajo anidado local | consulta puntual de `e_2`: 9 llamadas anidadas y 0 reconstrucciones, frente a 31 000–55 000 y 2 en `CU0`; objetivo ≤ 256 | cumple |
| G5 — reutilización y memorización | la segunda consulta con la misma clave no reconstruye; un fallo determinista se reproduce sin reconstruir (V-BUD-1) | cumple |
| G6 — margen del presupuesto (H04) | reconstrucción completa de `o_1`: ≈ 2 470 llamadas, el 7,5 % del presupuesto de 32 768, frente a ≈ 20 800 (64 %) en `CU0`; objetivo ≤ 25 % | cumple |
| G7 — cadena `o_1 → LocusLength → d_2 → e_2` | `preserve.e_2`, el perfil de longitud orientada y las longitudes de `o_1` se reproducen dentro de las tolerancias de `CU0`; `d_2` y `e_2` definidos (V-DAG-1) | cumple |
| G8 — determinismo, persistencia, compatibilidad | resultados idénticos bit a bit en ejecuciones repetidas y con otra vista (V-DET-1, V-INV-2); los documentos históricos se reabren y guardan idénticos, salvo la revisión de §7 | cumple |

| Medida (banco `composition`) | `CU0` | `CU1` |
|---|---:|---:|
| Copia anidada de `o_1` tras la carga: llamadas / reconstrucciones / revisión | 5 909 546 / 259 / 266 | 3 019 / 1 / 2 |
| Consulta puntual de `e_2`: llamadas anidadas / reconstrucciones | 31 310–54 928 / 2 | 9 / 0 |
| Carga del modelo con `e_2` | 15,3 s | 3,2 s |
| Arrastre de `E_1` con `e_2` (actualización + pintado) | 219 ms + 5 699 ms | 22 ms + 11 ms |
| Cambio de `rbCone` con `e_2` (actualización + pintado) | 164 ms + 5 657 ms | 86 ms + 25 ms |
| Recreación de `e_2`: tiempo / llamadas anidadas / reconstrucciones | 5 777 ms / 1 983 342 / 87 | 45 ms / 2 659 / 1 |

## 7. Conformidad de pruebas históricas (D-CU1-8, D-CU1-9)

Ninguna prueba se debilita; cada cambio expresa el contrato aprobado.

| Prueba | Cambio | Contrato |
|---|---|---|
| `G9U0GeneratorSuiteTest.g21` | una actualización sin cambio no evalúa; un cambio de `k` y su retorno publican el mismo trabajo | A1.1-R |
| `LocusV2PointDrivenCreationRegressionTest` | mover solo el conductor conserva la revisión | A1.1-R, D-CU1-8 |
| `G9U0MetricPositionTest.m04` | una estimación registra hipótesis; un valor certificado registra su envolvente rigurosa; ambos registran la tolerancia y son admisibles; se fijan las clases | §25.1, §25.4 |
| `PreG9BCu0LocusReferenceTest` | pin H03 invertido: el movimiento del conductor conserva revisión y estados | A1.1-R |
| `PreG9BCu0CompositionCharacterizationTest` | pin B10.1 invertido: la consulta puntual de `e_2` reutiliza la copia anidada y sus estados; el corte exterior se construye una vez | A1.1-R |
| `PreG9BCu0SyntheticCharacterizationTest` | pines H10/H11 invertidos: longitudes de arco y de circunferencia definidas y cercanas a la referencia que la línea base ya registraba | §25 |
| `pre-g9b-r5-b-base-fingerprints.json` | `historicalDocument.reopenedValues` de `L` y `M`: revisión 1 en lugar de 2, porque el recálculo tras reabrir es equivalente | D-CU1-8 |

Los métodos de prueba de `CU0` conservan su nombre: las identidades de la selección
`pre-g9b-cu0.desktop` no cambian. La línea base de `CU0` no se modifica y conserva las firmas
fijadas como evidencia histórica.

## 8. Referencias numéricas

| Referencia | `CU0` | `CU1` | Justificación |
|---|---|---|---|
| `H10`, longitud de `y = sin(2πkx)` en `[0, 1]` | 1,0 (la cuerda) para `k = 1, 2, 4` | 4,188275203684875; 8,111828618823123; 16,06471513503404 | estimador v2; diferencia con la referencia independiente de la línea base ≤ 1,7·10⁻¹¹ |
| `H11`, circunferencia de radio 8 y 20 | indefinida (presupuesto agotado) | `2πr` estructural | clase 1 |
| Circunferencias de radio 1 y 7 | 6,283185268677126; 43,98229688073988 | 6,283185307179586; 43,982297150257104 | clase 1, exacta salvo el redondeo del dominio |
| `n = Length(L)` del documento histórico R5-B | 4 | 4 | aritmética exterior con detección de exactitud |

Los valores `preserve` de `CU0` se reproducen dentro de sus tolerancias; ninguno exige una nueva
línea base.

## 9. Constantes calibradas (D-CU1-4)

| Constante | Valor | Intervalo aprobado | Evidencia |
|---|---|---|---|
| `d_min` | 6 (64 celdas) | 4–8 | resuelve H10 con `k = 1, 2, 4`; las sinusoides usan 3 153, 7 733 y 17 209 llamadas |
| Fracción de sondeo | `(√5 − 1)/2` | fija | V-ADV-1 a V-ADV-4 |
| Factor de seguridad | 2 | 1,5–4 | error real ≤ error declarado ≤ `τ` en V-MET-EST-1/2 (circunferencias de radio 1 a 1000, elipse, sinusoides, astroide) |
| Presupuesto por construcción | 32 768 evaluaciones | sin cambio | `o_1`: 7,5 % (G6) |
| Presupuesto de envolvente | 16 384 cajas | 4 096–65 536 | banco de calibración: en las curvas del corpus la envolvente de clase 2 necesitaría entre 10⁵ y 10⁷ cajas para `τ`; la parada anticipada por predicción la limita a ≈ 1–2 ms y la envolvente se adjunta como evidencia; con `τ` cien veces mayor las circunferencias se certifican con 16 384 cajas |

## 10. Definiciones de implementación y desviaciones de la matriz

Las definiciones que §25 deja abiertas figuran como texto del candidato en
`locus-v2-metrics.md` §25.12: extensión `S`, aceptación de hojas por el error declarado,
celdas por debajo de la resolución del parámetro, imágenes por semejanza, coherencia de los
certificados, redondeo exterior con detección de exactitud, trabajo de la clase 2 e
inversiones de dirección. Las precisiones respecto de la matriz figuran en su §5:
V-CACHE-1 (`D_1` en lugar de `excCyl`), V-INV-1 (traslación de `10⁶` en escala 1),
V-MET-STRUCT-1 (referencia semántica exacta), V-MET-STRUCT-2 (formas afines reconocidas) e
inversiones de dirección.

## 11. Limitaciones y deudas

| Identificador | Descripción | Propietario propuesto |
|---|---|---|
| `L-CU1-TRANSLATION-RESOLUTION` | una curva de tamaño unidad trasladada `10⁶` falla cerrada en clase 3 porque el redondeo de las coordenadas supera las cuotas de las hojas; la clase 1 no se ve afectada | decisión autoral (§12) |
| `L-CU1-CLASS2-REACH` | con `τ = 10⁻⁹·S` la clase 2 no se alcanza en curvas con giro total apreciable (≈ `ΘS³/τ` cajas); queda como evidencia rigurosa | `CU2` (clase v3) |
| `L-CU1-AFFINE-FORMS` | el certificado afín del proveedor reconoce solo formas directas | fuera de `CU1` |
| `L-CU1-REVERSAL-DENSITY` | dos inversiones de dirección entre muestras consecutivas de la partición mínima no se localizan; las hipótesis lo registran | `CU1-R` si se observa |
| `L-CU1-E2-LENGTH` | la longitud de `e_2` no se establece en clase 1 o 2; su salto de longitud orientada es de `CU2` (B10.2) | `CU2` |
| `L-CU1-IMPROPER-EVALUATOR` | los componentes impropios sin derivada conservan el fallo `LIMIT_NOT_ESTABLISHED` anterior | sin cambio |

## 12. Decisiones autorales pendientes

| Decisión | Pregunta | Opciones |
|---|---|---|
| D-CU1-R1 | Revisión técnica del candidato | aprobar / pedir correcciones |
| D-CU1-R2 | §25.12 como texto normativo de la versión 1.5 | adoptar / ajustar |
| D-CU1-R3 | `L-CU1-TRANSLATION-RESOLUTION` | aceptar como límite de binario64 documentado / añadir un término de resolución de coordenadas a `τ` en un paquete posterior |
| D-CU1-R4 | Presupuesto de envolvente 16 384 | adoptar / fijar otro valor del intervalo |
| D-CU1-R5 | Publicación de la rama de revisión | autorizar el empuje de `phase/pre-g9b-cu1-implementation` sin `main` |

## 13. Infraestructura de verificación modificada

- Registro: fase `PRE-G9B-CU1` (`compile.shared.semantic`, `compile.desktop.semantic`,
  `junit.shared.pre-g9b-cu1.semantic`, `junit.desktop.pre-g9b-cu1.semantic`) y sus cuatro nodos;
  huella del inventario actualizada.
- Inventario: selecciones `pre-g9b-cu1.shared` y `pre-g9b-cu1.desktop`, y recuentos de
  `discovery.*` y `final.*` refrescados con el actualizador oficial.
- `verification-final-coverage.Tests.ps1`: 66 selecciones, 70 fases y los recuentos de las dos
  selecciones nuevas.
- Manifiesto del límite upstream: 12 fuentes y 4 pruebas nuevas; notas de inversión en tres
  entradas de `CU0`.

## 14. Estado de autorización

`CU1 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`. No se declara
`PASS — AUTHOR APPROVED`. `CU2`–`CU10`, la publicación en `main`, etiquetas, releases,
binarios y cambios del libro siguen sin autorizar.
