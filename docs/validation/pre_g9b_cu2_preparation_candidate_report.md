# PRE-G9B-CU2 — Informe del candidato de preparación

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato documental de preparación de `CU2`. Evidencia, no autoridad normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-CU — CU1 Final Author Approval, Documentary Closeout and CU2 Preparation Authorization» (2026-10-20), transcrita en el [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) §7; revisión: instrucción autoral «GeoCeDG — PRE-G9B-CU2. Revisión documental focalizada, certificación de degeneraciones y refinamiento normativo» (2026-10-20) |
| Base de la preparación | cierre documental de `CU1` `27bf652f176e57ced3807b82afe14fc00b7586a9`, árbol `94b50ab6b2b429a913a31e29c3d73f16175c64e6`, hijo directo de `2fe8ed198e1861a5b0e4558e8088fe20901249e4` |
| Base de la revisión | candidato de preparación publicado `60d546441dcca5355edfe296ee8a0a521f8a3565`, árbol `f3a3b616d6a3f64e5ad024089f1dbf0824aa2d8f`, hijo directo del cierre de `CU1` |
| Rama | `phase/pre-g9b-cu2-preparation`: publicada en `60d54644` por instrucción autoral; la revisión es un commit local descendiente, sin publicar |
| Clase | candidato documental: `STATIC` y comprobaciones pertinentes, como autorizan las dos instrucciones |

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
- Cuatro sondeos de solo lectura, desde el directorio temporal de la sesión, con los ayudantes
  de la línea base de `CU0`: dos en la preparación y dos en la revisión
  ([análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §1 y §8). La
  caracterización de `CU0` no se ha repetido.

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

Ninguna corrección de `CU2` exige cambiar un contrato de `CU1` o de `CU3`.

## 4. Revisión focalizada P1–P5

La revisión conserva la arquitectura (niveles de evidencia, certificación híbrida, clase v3,
arcos, autoridad única de cierre, certificado estructural de `LocusLength`, `ORIGIN_WRAP`,
linaje, migración de consumidores y estrategia T0–T13 con certificados en sombra) y resuelve
las cinco precisiones del autor.

| Precisión | Resultado | Documentos | Decisiones |
|---|---|---|---|
| P1, valor crítico de la mordedura | El discriminante se factoriza exactamente en las distancias del conductor a las dos tangentes desde `P`. En el valor crítico tiene un cero doble sin cambio de signo: `Q` y `R` existen en todo el círculo y coinciden en `t_c`, y cada locus es una componente periódica cerrada y continua con una esquina en `t_c` (giro de 133,7° y 147,3°). Un certificado algebraico exacto (polinomio en `tan(t/2)` con coeficientes racionales exactos, máximo común divisor y Sturm) prueba la multiplicidad 2. Ningún binary64 del deslizador es exactamente crítico: la clasificación exacta cambia entre `0x1.d416bb67a38bbp1` y `0x1.d416bb67a38bcp1`, la fórmula en `double` se equivoca en uno y el evaluador da puntos dentro del hueco exacto. La configuración crítica exacta se reproduce con datos enteros | análisis §3.2, §3.3, §3.6 y §3.8; diseño §4.3 y §4.6; A2.2-R; ADR 0038 decisión 7; V-CRIT-1 a V-CRIT-10 | D-CU2-16 (nueva); D-CU2-5 |
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

## 5. Qué no se ha hecho

- No se ha modificado código productivo, pruebas, registro, inventario, serialización,
  especificaciones vigentes ni ADR aceptados. El ADR 0038 es un candidato (**PROPOSED**), y las
  enmiendas A2.1-R a A2.6 siguen siendo propuestas.
- No se ha iniciado la implementación de `CU2` ni de `CU3`–`CU10`.
- No se ha alterado el modelo original: los sondeos trabajan sobre copias en el directorio
  temporal.
- No se ha publicado la revisión, ni etiquetado ni cambiado `main`.
- No se ha tocado el libro.

## 6. Comprobaciones previas al commit

| Comprobación | Resultado |
|---|---|
| Contrato del prompt (`Test-PromptContractDocument`, perfil `task`) | `execution_safe = true`; los siete campos presentes y en orden; ninguno ausente, duplicado ni desconocido |
| Enlaces relativos de los documentos nuevos y modificados | todos resuelven |
| Nombres retirados de cierre | ninguno |
| `git diff --check` | limpio |
| Identificadores cruzados (`D-CU2-n`, `V-…`, `GC-n`) | todos los citados están definidos |

La verificación del commit de la revisión (`STATIC`) se comunica con la entrega. Un commit no
puede citar su propia identidad.

## 7. Limitaciones

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

## 8. Cuestiones todavía abiertas

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
6. **Roadmap y plan consolidado.** No registran todavía la publicación de `60d54644` ni esta
   revisión; quedan para el siguiente paso documental autorizado.

## 9. Estado de autorización

`CU1` está `PASS — AUTHOR APPROVED` según su registro de cierre. La preparación de `CU2` y su
revisión focalizada están ejecutadas como candidato documental pendiente de revisión autoral.

La siguiente decisión autoral es la aprobación definitiva de la arquitectura de `CU2`
(D-CU2-1 a D-CU2-11, D-CU2-16, D-CU2-17 y E-1 a E-3) y la autorización expresa de su
implementación sobre un commit base exacto (D-CU2-12 a D-CU2-15).

La implementación de `CU2` y `CU3`–`CU10` no están autorizadas.
