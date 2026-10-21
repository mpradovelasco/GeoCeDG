# PRE-G9B-CU2 — Informe del candidato de preparación

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato documental de preparación de `CU2`. Evidencia, no autoridad normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-CU — CU1 Final Author Approval, Documentary Closeout and CU2 Preparation Authorization» (2026-10-20), transcrita en el [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) §7; revisión: instrucción autoral «GeoCeDG — PRE-G9B-CU2. Revisión documental focalizada, certificación de degeneraciones y refinamiento normativo» (2026-10-20); cierre de coherencia: instrucción autoral «GeoCeDG — PRE-G9B-CU2. Cierre de coherencia normativa y registro del programa de apéndices científicos» (2026-10-20) |
| Base de la preparación | cierre documental de `CU1` `27bf652f176e57ced3807b82afe14fc00b7586a9`, árbol `94b50ab6b2b429a913a31e29c3d73f16175c64e6`, hijo directo de `2fe8ed198e1861a5b0e4558e8088fe20901249e4` |
| Base de la revisión | candidato de preparación publicado `60d546441dcca5355edfe296ee8a0a521f8a3565`, árbol `f3a3b616d6a3f64e5ad024089f1dbf0824aa2d8f`, hijo directo del cierre de `CU1` |
| Base del cierre de coherencia | revisión focalizada publicada `cbb7ea8ce379d8d74072f4d12dcd4443265eb7ac`, árbol `55a5a171c552acb1ecaa83afa51b15f55cc0bfd2`, hijo directo de `60d54644` |
| Rama | `phase/pre-g9b-cu2-preparation`: publicada en `60d54644` y después, por avance rápido normal, en `cbb7ea8c`, las dos veces por instrucción autoral; el cierre de coherencia es un commit local descendiente, sin publicar |
| Clase | candidato documental: `STATIC` y comprobaciones pertinentes, como autorizan las tres instrucciones |
| Nota posterior (2026-10-21) | Este informe se conserva como se congeló. El cierre de coherencia `070af6f902cd0be291247e73efb5629ef2a39eba` se publicó por instrucción autoral (avance rápido normal de `phase/pre-g9b-cu2-preparation` desde `cbb7ea8c`). El autor aprobó después la arquitectura y autorizó la implementación condicionada: [registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md). Ese registro sustituye D-CU2-20 por D-CU2-20-R (§3, §9 punto 9 y §10 de este informe) y corrige la atribución de F-CU2-12 que hace la fila del hallazgo en §5: la observación del ulp de `excCyl` se debía a H01 (definición obsoleta tras `setUndefined`), no a la huella; la huella sí distingue un ulp. Las carencias reales son el estado de selección fuera de la huella, los literales angulares sin texto original y la falta de validación de fidelidad (registro §12, con la precisión de T1) |

## 1. Cierre de `CU1` y publicaciones

| Elemento | Valor |
|---|---|
| Commit de cierre | `27bf652f176e57ced3807b82afe14fc00b7586a9`, árbol `94b50ab6…` |
| Contenido | registro de cierre y su espejo `geocedg/validation/pre-g9b-cu/pre-g9b-cu1-closeout.json`; estado del ADR 0037, de `locus-v2-metrics.md` (§25.12 adoptado) y de `locus-v2-semantics.md`; roadmap (5.63) y plan consolidado |
| Autenticación previa | las seis ejecuciones de aceptación y las dos históricas del registro de cierre (§2), re-autenticadas antes del cierre |
| `STATIC` del cierre | `verification-22a777547bd943d8b0e66af7027fc90f`, resultado `ff5b1e83512aa7cb57770ac1b4e1abe01422944a6e21db2d89fd4fc92502f9a3`, `ACCEPTED / COMPLETE` 3/3; solo diagnósticos permanentes. Re-autenticado: hash recalculado igual al registrado; commit y árbol exactos |
| Registro en `artifacts/agent/` | `pre-g9b-cu1-closeout-static-27bf652f/` |
| Publicación del cierre de `CU1` | push normal, sin `--force`, de la rama de revisión `phase/pre-g9b-cu1-implementation` en `27bf652f…`. `main` sin cambio en `48a0775e4f2f122f5e2614c62b5773777752ccd1` |
| `STATIC` del candidato de preparación | `verification-1b971a6e9d644f278d073e95a2f02e96`, resultado `b3bacd4c08b9877b59b7b3c5b563f66386f7ccc46e7ce6f28d1972250c2e41ae`, `ACCEPTED / COMPLETE` 3/3 sobre `60d54644`, árbol `f3a3b616…`; autenticado; diagnóstico de gobierno idéntico byte a byte al de la base |
| Publicación del candidato de preparación | por instrucción autoral (2026-10-20): push normal, sin `--force`, de `phase/pre-g9b-cu2-preparation` en `60d546441dcca5355edfe296ee8a0a521f8a3565` (rama nueva). Consulta en vivo antes de esta revisión: esa rama en `60d54644`, `phase/pre-g9b-cu1-implementation` en `27bf652f`, `main` en `48a0775e`; 73 etiquetas remotas, sin cambio; ningún release ni binario |
| `STATIC` de la revisión focalizada | `verification-ab36ab6b4a784caeaea71495accc07cf`, resultado `48fb4284142b8bfb0b01cb38260702f7f8d8c8278cc8bdbda8412e7f4c759bb7`, `ACCEPTED / COMPLETE` 3/3 sobre `cbb7ea8c`, árbol `55a5a171…`; re-autenticado antes del cierre de coherencia; solo diagnósticos permanentes |
| Publicación de la revisión focalizada | por instrucción autoral (2026-10-20): push normal de `phase/pre-g9b-cu2-preparation`, avance rápido `60d54644` → `cbb7ea8ce379d8d74072f4d12dcd4443265eb7ac`. Consulta en vivo antes del cierre de coherencia: esa rama en `cbb7ea8c`, `phase/pre-g9b-cu1-implementation` en `27bf652f`, `main` en `48a0775e`; 73 etiquetas remotas, sin cambio; ningún release ni binario |
| Libro | `geocedg_book` sin cambios: `78fbc88e9e12346b89d6ded411eaae94de2284b9`, árbol de trabajo limpio |

## 2. Entregables de la preparación

| Entregable pedido (§8 de la instrucción) | Documento |
|---|---|
| 1. Complemento de caracterización | [análisis del modelo del autor](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) |
| 2. Diseño consolidado de la certificación híbrida | [diseño de certificación](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §1–§3 y §5–§11 |
| 3. Propuesta normativa de existencia, continuidad y componentes | [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md) y [ADR 0038 (PROPOSED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) |
| 4. Ampliación del modelo de construcciones certificadas | [diseño de certificación](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §4; enmienda A2.2-R |
| 5. Diseño del evento estructural de la longitud orientada | [diseño del evento](../architecture/pre_g9b_cu2_oriented_length_event_design.md); enmienda A2.3-R |
| 6. Reglas de consumo de los certificados | [consumo y representación](../architecture/pre_g9b_cu2_consumption_and_representation.md) §1–§4 y §7 |
| 7. Estrategia de representación y teselación | [consumo y representación](../architecture/pre_g9b_cu2_consumption_and_representation.md) §5–§6 |
| 8. Matriz de pruebas y criterios de aceptación | [matriz de validación](pre_g9b_cu2_validation_matrix.md) |
| 9. Estrategia mínima de implementación, con dependencias y condiciones de parada | [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) |
| 10. Prompt canónico candidato | [`pre-g9b-cu2-existence-continuity-and-representation.prompt.md`](../../.github/prompts/tasks/pre-g9b-cu2-existence-continuity-and-representation.prompt.md) |
| 11. Hoja de decisiones pendientes | [hoja de decisiones](pre_g9b_cu2_author_decision_sheet.md) |

**Inspección realizada.**

- Lectura del código de los productores, del modelo de intervalos, de los consumidores y, en la
  revisión, de los algoritmos de intersección heredados (orden de raíces, tolerancia de
  tangencia y estado de selección), con referencias `archivo:línea` comprobadas.
- Deducción analítica de la existencia de las dos familias del modelo y, en la revisión, la
  factorización exacta del discriminante y el estudio del valor crítico.
- Seis sondeos de solo lectura, desde el directorio temporal de la sesión, con los ayudantes
  de la línea base de `CU0`: dos en la preparación, dos en la revisión y dos en el cierre de
  coherencia ([análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §1 y
  §8). La caracterización de `CU0` no se ha repetido.

## 3. Conclusiones principales

1. **Certificado falso.** El productor dependiente declara `COMPLETE` sin prueba, contra el
   ADR 0027, que exige fallar cerrado. Otros productores reciben el mismo certificado por el
   constructor de rama por defecto (D-CU2-7).
2. **Certificación alcanzable.** Los siete loci del modelo del autor se certifican con la
   clase v3, las transiciones certificadas, el certificado algebraico exacto, la periodicidad
   estructural y el certificado estructural de la métrica, también en el valor crítico.
3. **`e_2` sin el valor de la longitud.** La topología de `e_2` se certifica con hechos
   estructurales de la función métrica (dominio, continuidad, origen exacto y salto positivo
   por una cuerda), sin usar la longitud de clase 3. Es la diferencia principal con la
   propuesta E de `CU0` (F-CU2-7).
4. **Una autoridad de cierre.** Hoy cinco predicados distintos deciden el cierre de una
   curva.
5. **Fronteras.** La validez puntual del evaluador excede la raíz exacta hasta 4,3·10⁻⁶
   (F-CU2-1). Las fronteras se certifican sobre los predicados de validez.
6. **H01.** La regla de publicación depende del punto trazado, y un locus oculto sigue
   evaluando su última definición (F-CU2-3).
7. **B06.** Los pliegues de `e_1` son reales y exactos por simetría; el giro de 156,8° de `w`
   es una falta de teselación.
8. **Uniones falsas.** Cerca del valor crítico el dibujo une un hueco real (F-CU2-2).
9. **Observaciones.** La raíz de la costura de `e_1 ∩ c_1` es una tangencia (F-CU2-5, para
   `CU3`), y el manifiesto del corpus describe mal el cilindro (F-CU2-6).

Ninguna corrección de `CU2` exige cambiar un contrato de `CU1` o de `CU3`. La huella exacta
(D-CU2-20) es conformidad con el contrato de `CU1`, aunque toca uno de sus componentes.

## 4. Revisión focalizada P1–P5

La revisión conserva la arquitectura (niveles de evidencia, certificación híbrida, clase v3,
arcos, autoridad única de cierre, certificado estructural de `LocusLength`, `ORIGIN_WRAP`,
linaje, migración de consumidores y estrategia T0–T13 con certificados en sombra) y resuelve
las cinco precisiones del autor.

| Precisión | Resultado | Documentos | Decisiones |
|---|---|---|---|
| P1, valor crítico de la mordedura | El discriminante se factoriza exactamente en las distancias del conductor a las dos tangentes desde `P`. En el valor crítico tiene un cero doble sin cambio de signo: `Q` y `R` existen en todo el círculo y coinciden en `t_c`, y cada locus es una componente periódica cerrada y continua con una esquina en `t_c` (giro de 133,7° y 147,3°), porque `{Q, R}` están etiquetadas (C1, §5). Un certificado algebraico exacto (polinomio en `tan(t/2)` con coeficientes racionales exactos, máximo común divisor y Sturm) prueba la multiplicidad 2. Ningún binary64 del deslizador es exactamente crítico: la clasificación exacta cambia entre `0x1.d416bb67a38bbp1` y `0x1.d416bb67a38bcp1`, la fórmula en `double` se equivoca en uno y el evaluador da puntos dentro del hueco exacto. La configuración crítica exacta se reproduce con datos enteros | análisis §3.2, §3.3, §3.6 y §3.8; diseño §4.3 y §4.6; A2.2-R; ADR 0038 decisión 7; V-CRIT-1 a V-CRIT-10 | D-CU2-16 (nueva); D-CU2-5 |
| P2, extremos laterales de `e_2` | Los dos extremos comparten la dirección canónica `t_N` y no el valor de la longitud; cada uno tiene lado, inclusión, límite lateral y fuente propios. La pertenencia usa la coordenada de arco y el extremo excluido solo se alcanza como límite lateral: la canonicalización nunca los identifica | evento §4.2 y §4.4; diseño §3.2; A2.1-R y A2.3-R; ADR 0038 decisiones 3 y 10; V-E2-8 y V-E2-9 | D-CU2-1; D-CU2-3 |
| P3, pliegues de `e_1` | La existencia de un punto estacionario no se prueba con una envolvente de `F′` que contiene el cero. Los pliegues de `e_1` existen por una simetría exacta, demostrada sobre la construcción (6,4·10⁻¹⁵ medido) y verificable sobre las constantes capturadas; aislamiento y no degeneración (`|F″(0)|` ≈ 3,93, `|F″(π)|` ≈ 1,27) por chorros de segundo orden; tangentes laterales opuestas | análisis §4.3; diseño §5; A2.2-R; ADR 0038 decisión 8; V-FOLD-1 a V-FOLD-4 | D-CU2-6 (reformulada) |
| P4, selección e identidad | La salida de una intersección es una raíz bruta, ordenada por el parámetro a lo largo de la dirección algorítmica (`N − P` para `Ray(P, N)`), elegida por una permutación que es estado del algoritmo y que se reinicializa por cercanía al crear, cargar y en `updateConstruction`, también en la copia del corte. La regla se lee de ese estado, entra en la huella de la revisión y la copia debe coincidir con el algoritmo vivo. Tres identidades separadas; la contención única valida una correspondencia demostrada y nunca la crea; ningún selector persistido se reescribe | análisis §3.4; diseño §4.1 y §7; A2.1-R y A2.2-R; ADR 0038 decisiones 5 y 13; V-SEL-1 a V-SEL-7 y V-LIN-3, V-LIN-4 y V-LIN-6 | D-CU2-2; D-CU2-17 (nueva); E-3 (revisada) |
| P5, existencia y disponibilidad | Existencia, topología, disponibilidad numérica y disponibilidad de representación son hechos distintos. Un fallo de evaluación es computacional; un punto del evaluador en una no existencia certificada no se publica como válido; el dibujo y las exportaciones no interpolan, no extrapolan, declaran su fidelidad y fallan explícitamente | diseño §3.4; consumo principio 5, §5.6 y §6; evento §5 y §6; A2.1-R, A2.4-R y A2.6; ADR 0038 decisión 11; V-AVAIL-1 a V-AVAIL-6 | D-CU2-1; D-CU2-4 |
| Coherencia (§8 de la instrucción) | `UNIFORMLY_INVALID` por intervalos solo para desigualdades estrictas; completitud solo como recubrimiento certificado; ninguna reconstrucción de asociaciones por etiqueta, índice, coordenadas o proximidad; evidencia exploratoria sin autoridad | diseño §2, §4.1 y §7; A2.1-R y A2.2-R; V-UNIF-1 y V-COMPL-1 | D-CU2-1; D-CU2-2 |

**Delta documental de la revisión.** Once documentos candidatos revisados: los cuatro diseños,
la propuesta normativa, el ADR 0038, la estrategia (ajustes en T3, T4, T5, T7, T8, T10 y T11, y
tres condiciones de parada nuevas, sin cambiar la numeración), la matriz, la hoja de
decisiones, este informe y el prompt canónico. El roadmap y el plan consolidado no se tocan: la
instrucción limita la revisión a los documentos candidatos.

**Sondeos de la revisión.** Además de la factorización y de la clasificación exacta, leen el
estado de selección de los algoritmos de intersección y comprueban las relaciones exactas de las
constantes capturadas. Hallazgos nuevos: F-CU2-8 (estado de selección), F-CU2-9 (clasificación
binary64 y tolerancia del evaluador), F-CU2-10 (`P.y` difiere de `B.y` en una unidad de la última
cifra) y F-CU2-11 (esquinas en el valor crítico).

## 5. Cierre de coherencia C1–C3 y registro de E1

Sobre la base `cbb7ea8c`, la instrucción de cierre de coherencia pide resolver tres puntos
normativos y registrar un mandato editorial. La arquitectura y el orden T0–T13 no cambian.

| Punto | Resultado | Documentos | Decisiones |
|---|---|---|---|
| C1, raíz doble certificada frente a salidas efectivas | GeoGebra anula la segunda salida de una intersección cuando no tiene etiqueta y coincide con la primera dentro de 10⁻⁸ por coordenada. Se separan siete hechos: existencia, multiplicidad, identidad semántica de la solución, definición efectiva de la salida, disponibilidad en el corte, continuidad de los límites laterales y continuidad de la construcción. En el modelo del autor `{Q, R}` están etiquetadas, en el vivo y en los cortes: en la raíz doble las dos salidas están definidas y coinciden, y la componente periódica cerrada con su parámetro singular se mantiene, con la precondición nueva de que el corte reproduzca etiquetado y permutación. Con una segunda salida sin etiqueta la definición efectiva es dispersa y difiere entre el vivo y el corte (49 frente a 55 de 201 muestras): banda `UNRESOLVED` y `NOT_ESTABLISHED`. El valor límite está demostrado, pero es evidencia y no una salida operativa | análisis §3.8.4 y §3.9; diseño §4.1, §4.6 y §4.7; consumo §3.2 y §5.2; A2.2-R; ADR 0038 decisiones 5 y 16; V-SEL-3 y V-COINC-1 a V-COINC-9 | D-CU2-18 (nueva) |
| C2, componentes puntuales | Una existencia reducida a un parámetro aislado `{t*}` es una componente puntual, entidad propia con contrato de existencia, parámetro, inclusión, continuidad, maximalidad, completitud, identidad y linaje, métrica nula, pertenencia, representación y posición. No se confunde con una raíz doble interior. En `CU2` se declara sin dibujarla; ningún objeto punto público; los consumidores que no la admiten fallan explícitamente | diseño §3.5; consumo §3.1 y §6; A2.1-R, A2.4-R, A2.5 y A2.6; ADR 0038 decisión 15; V-CRIT-5 y V-POINT-1 a V-POINT-6 | D-CU2-19 (nueva) |
| C3, precisión matemática | El aislamiento ordinario de raíces simples no certifica una raíz múltiple; los procedimientos verificados para raíces múltiples añaden estructura o certifican un problema perturbado. El certificado exacto con máximo común divisor y Sturm sigue siendo el método de la clase admitida. Cinco naturalezas de evidencia, y distinción entre constantes binary64 capturadas y relaciones geométricas ideales | análisis §3.8.2; diseño §2 y §4.3; A2.2-R; ADR 0038 contexto, decisiones 7 y 18 y alternativas | D-CU2-16 (precisada) |
| Hallazgo F-CU2-12, huella de la revisión | La huella de `CU1` toma la serialización XML de 15 cifras significativas: un cambio de un ulp de `excCyl` no publicó revisión y el corte conservó el valor anterior, contra la «exact fingerprint» de `locus-v2-semantics.md` v1.2 | análisis §3.9; diseño §8; A2.1-R; ADR 0038 decisión 17; V-REV-EXACT-1 | D-CU2-20 (nueva) |
| E1, mandato editorial del autor | Obligación de cierre del track, con tres estados: mandato aprobado; diseño detallado de los apéndices pendiente de preparación y revisión; redacción e incorporación a `geocedg_book` no autorizadas. Plan de transferencia científica nuevo, entrada en `CU10` y en §8 del plan consolidado, y registro en la hoja de ruta (5.65) | [plan de transferencia](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md); [plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md) §4 y §8; hoja de ruta | no es una decisión de `CU2` (hoja de decisiones §1) |

**Delta documental.** Diez documentos candidatos revisados: el análisis, el diseño de
certificación, el de consumo, la propuesta normativa, el ADR 0038, la estrategia (T2, T3, T4, T9 y
T10, y condiciones de parada 15 a 17, sin cambiar la numeración), la matriz, la hoja de
decisiones, este informe y el prompt canónico. El diseño del evento de la longitud orientada no
está afectado. Un documento nuevo, el plan de transferencia científica. En el plan consolidado y en
la hoja de ruta solo se registra E1.

**Sondeos del cierre de coherencia.** Dos sondeos más de solo lectura (análisis §8, sondeos 3 y
4): salidas efectivas en raíces coincidentes y corte frente a vivo. Hallazgos nuevos: F-CU2-12
(huella), F-CU2-13 (supresión de la segunda salida sin etiqueta) y F-CU2-14 (permutación `[1, 0]`
de `J/K`). Las configuraciones sintéticas de V-POINT-1 y V-POINT-2 se han comprobado con el signo
del discriminante en 200 000 conductores, fuera de GeoCeDG.

## 6. Qué no se ha hecho

- No se ha modificado código productivo, pruebas, registro, inventario, serialización,
  especificaciones vigentes ni ADR aceptados. El ADR 0038 es un candidato (**PROPOSED**), y las
  enmiendas A2.1-R a A2.6 siguen siendo propuestas.
- No se ha iniciado la implementación de `CU2` ni de `CU3`–`CU10`.
- No se ha alterado el modelo original: los sondeos trabajan sobre copias en el directorio
  temporal.
- No se ha publicado el cierre de coherencia, ni etiquetado ni cambiado `main`.
- No se ha tocado el libro: E1 solo registra la obligación en este repositorio.
- No se ha diseñado ninguna capacidad nueva del núcleo para las raíces coincidentes más allá de
  su contrato candidato (D-CU2-18), ni cambiado la regla heredada.

## 7. Comprobaciones previas al commit

| Comprobación | Resultado |
|---|---|
| Contrato del prompt (`Test-PromptContractDocument`, perfil `task`) | `execution_safe = true`; los siete campos presentes y en orden; ninguno ausente, duplicado ni desconocido |
| Enlaces relativos de los documentos nuevos y modificados | todos resuelven |
| Nombres retirados de cierre | ninguno |
| `git diff --check` y finales de línea | limpio; el documento nuevo y las líneas nuevas, en LF; la hoja de ruta conserva sus líneas CRLF |
| Identificadores cruzados (`D-CU2-n`, `V-…`, `GC-n`, `F-CU2-n`) | todos los citados están definidos |
| Afirmaciones superadas por C3 | ninguna aparición de la afirmación anterior sobre los métodos de intervalos |

La verificación del commit del cierre de coherencia (`STATIC`) se comunica con la entrega. Un
commit no puede citar su propia identidad.

## 8. Limitaciones

1. El sondeo cubre los parámetros del autor y el barrido de excCyl de 3,6 a 5,0 con
   `rbCone` = 5. Los puntos aislados con `rbCone` > 7,5735 se deducen analíticamente y no se
   han medido.
2. El certificado algebraico exacto se ha probado como sondeo, con aritmética racional sin
   optimizar; su API, sus presupuestos y su coste final los fija la implementación (D-CU2-5,
   D-CU2-16).
3. La captura de clase v3 de los algoritmos upstream (recta–cónica, intersección indexada,
   circunferencia por dos puntos y macros) se ha diseñado sobre su lectura, no prototipado. La
   igualdad del estado de selección entre la copia del corte y el algoritmo vivo se ha
   observado en el modelo, no en construcciones con permutaciones invertidas.
4. El inventario de pruebas existentes afectadas se da por categorías (estrategia §5); se
   completará en la implementación.
5. La supresión de la segunda salida sin etiqueta se ha medido en el modelo, con construcciones
   añadidas a su copia de trabajo, en la penetración y en la mordedura más próximas y en la
   frontera de excCyl 3,7; las cotas conservadoras de su banda se han deducido de la tolerancia
   heredada y no se han medido con un certificador.
6. El inventario científico inicial del plan de transferencia se basa en el código, las pruebas y
   los documentos citados; no reabre ni verifica de nuevo los métodos de fases cerradas.

## 9. Cuestiones todavía abiertas

1. **Tangencias calculadas con redondeo.** Se certifican como su configuración redondeada.
   Conservar la relación exacta exigiría capturar simbólicamente el subgrafo independiente del
   conductor; no se propone.
2. **Raíces múltiples de predicados que no son de primer nivel** (después de una raíz
   cuadrada). Solo el método del punto estacionario y `UNRESOLVED`; su eliminación algebraica
   queda fuera de `CU2`.
3. **Estado de selección.** Si en alguna construcción la copia del corte no reproduce el estado
   vivo, la selección falla cerrada. Escribir el estado en la copia (opción b de D-CU2-17)
   exigiría una decisión aparte. La persistencia exacta de ese estado entre sesiones exigiría un
   cambio de formato.
4. **Tolerancia heredada del evaluador.** `CU2` no la cambia: el certificado manda y el
   evaluador no es autoridad de existencia.
5. **Pliegues sin simetría.** Quedan sin certificar; se refinan con diagnóstico.
6. **Roadmap y plan consolidado.** Registran ahora la obligación E1, como pide la instrucción,
   pero todavía no las publicaciones de `60d54644` y `cbb7ea8c` ni las revisiones de la
   preparación; quedan para el siguiente paso documental autorizado.
7. **Continuidad con segundas salidas sin etiqueta.** Fuera del modelo del autor, esos loci
   quedan `NOT_ESTABLISHED` a través de la raíz coincidente. Devolverles la continuidad exige
   la capacidad del núcleo de D-CU2-18 (b), que cambia la semántica del Locus V2 respecto del
   objeto vivo, o un cambio upstream (c).
8. **Primera inicialización en una raíz doble.** El cambio del estado heredado que provoca el
   primer movimiento del conductor publica una revisión con las mismas componentes (V-COINC-3):
   la regla de A1.1-R se cumple, pero no su consecuencia habitual. El autor puede preferir otra
   regla dentro de D-CU2-18.
9. **Huella exacta.** Toca un componente aprobado de `CU1` (D-CU2-20).
10. **Dibujo de las componentes puntuales.** En `CU2` se declaran sin dibujarlas (D-CU2-19); su
    representación gráfica y su materialización quedan para `CU3` o para una decisión posterior.
11. **Programa de apéndices (E1).** El diseño detallado de los apéndices necesita su propia
    preparación y revisión; la redacción y la incorporación a `geocedg_book`, autorizaciones
    aparte después de `CU10`.
12. **Discrepancias de estado documentales.** Al reunir el inventario científico aparecen
    cabeceras de especificaciones y ADR anteriores al código o a sus cierres (plan de
    transferencia §6.7). Reconciliarlas exige modificar documentos aprobados, con autorización
    propia.

## 10. Estado de autorización

`CU1` está `PASS — AUTHOR APPROVED` según su registro de cierre. La preparación de `CU2`, su
revisión focalizada y su cierre de coherencia están ejecutados como candidato documental
pendiente de revisión autoral. El mandato editorial E1 está aprobado por el autor y registrado
como obligación del track; nada de su programa está diseñado en detalle ni redactado.

La siguiente decisión autoral es la aprobación definitiva de la arquitectura de `CU2`
(D-CU2-1 a D-CU2-11, D-CU2-16 a D-CU2-20 y E-1 a E-3) y la autorización expresa de su
implementación sobre un commit base exacto (D-CU2-12 a D-CU2-15).

La implementación de `CU2` y `CU3`–`CU10` no están autorizadas.
