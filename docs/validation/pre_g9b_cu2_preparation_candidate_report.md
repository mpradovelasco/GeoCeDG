# PRE-G9B-CU2 — Informe del candidato de preparación

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato documental de preparación de `CU2`. Evidencia, no autoridad normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-CU — CU1 Final Author Approval, Documentary Closeout and CU2 Preparation Authorization» (2026-10-20), transcrita en el [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) §7 |
| Base | cierre documental de `CU1` `27bf652f176e57ced3807b82afe14fc00b7586a9`, árbol `94b50ab6b2b429a913a31e29c3d73f16175c64e6`, hijo directo de `2fe8ed198e1861a5b0e4558e8088fe20901249e4` |
| Rama | `phase/pre-g9b-cu2-preparation` (local, sin publicar) |
| Clase | candidato documental: `STATIC` y comprobaciones pertinentes, como autoriza la instrucción |

## 1. Cierre de `CU1` y su publicación

| Elemento | Valor |
|---|---|
| Commit de cierre | `27bf652f176e57ced3807b82afe14fc00b7586a9`, árbol `94b50ab6…` |
| Contenido | registro de cierre y su espejo `geocedg/validation/pre-g9b-cu/pre-g9b-cu1-closeout.json`; estado del ADR 0037, de `locus-v2-metrics.md` (§25.12 adoptado) y de `locus-v2-semantics.md`; roadmap (5.63) y plan consolidado |
| Autenticación previa | las seis ejecuciones de aceptación y las dos históricas del registro de cierre (§2), re-autenticadas antes del cierre |
| `STATIC` del cierre | `verification-22a777547bd943d8b0e66af7027fc90f`, resultado `ff5b1e83512aa7cb57770ac1b4e1abe01422944a6e21db2d89fd4fc92502f9a3`, `ACCEPTED / COMPLETE` 3/3; solo diagnósticos permanentes. Re-autenticado para este informe: hash recalculado igual al registrado; commit y árbol exactos |
| Registro en `artifacts/agent/` | `pre-g9b-cu1-closeout-static-27bf652f/` |
| Publicación | push normal, sin `--force`, de la rama de revisión `phase/pre-g9b-cu1-implementation`. Consulta en vivo (`git ls-remote origin`, 2026-10-20): `refs/heads/phase/pre-g9b-cu1-implementation` = `27bf652f…`; `refs/heads/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`, sin cambio, igual a `main` local y a `origin/main`. Ninguna etiqueta `CU` local ni remota; ningún release ni binario |
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

- Lectura del código de los productores, del modelo de intervalos y de los consumidores, con
  referencias `archivo:línea` comprobadas contra `27bf652f`
  ([consumo](../architecture/pre_g9b_cu2_consumption_and_representation.md) §2;
  [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) §1).
- Deducción analítica de la existencia de las dos familias del modelo.
- Un sondeo de solo lectura, desde el directorio temporal de la sesión, con los ayudantes de
  la línea base de `CU0`, en dos ejecuciones
  ([análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §1 y §8). La
  caracterización de `CU0` no se ha repetido.

## 3. Conclusiones principales

1. **Certificado falso.** El productor dependiente declara `COMPLETE` sin prueba, contra el
   ADR 0027, que exige fallar cerrado. Otros productores reciben el mismo certificado por el
   constructor de rama por defecto (D-CU2-7).
2. **Certificación alcanzable.** Los siete loci del modelo del autor se certifican con la
   clase v3, las transiciones certificadas, la periodicidad estructural y el certificado
   estructural de la métrica. El valor crítico de la mordedura falla cerrado, como debe.
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
7. **B06.** Los pliegues de `e_1` son reales (velocidad nula); el giro de 156,8° de `w` es una
   falta de teselación. Corregir los dos exige el criterio de giro y chorros de segundo
   orden.
8. **Uniones falsas.** Cerca del valor crítico el dibujo une un hueco real (F-CU2-2).
9. **Observaciones.** La raíz de la costura de `e_1 ∩ c_1` es una tangencia (F-CU2-5, para
   `CU3`), y el manifiesto del corpus describe mal el cilindro (F-CU2-6).

Ninguna corrección de `CU2` exige cambiar un contrato de `CU1` o de `CU3`.

## 4. Qué no se ha hecho

- No se ha modificado código productivo, pruebas, registro, inventario, especificaciones
  vigentes ni ADR aceptados. El ADR 0038 es un candidato (**PROPOSED**), y las enmiendas
  A2.1-R a A2.6 siguen siendo propuestas.
- No se ha iniciado la implementación de `CU2` ni de `CU3`–`CU10`.
- No se ha publicado este candidato, ni etiquetado ni cambiado `main`.
- No se ha tocado el libro.

## 5. Comprobaciones previas al commit

| Comprobación | Resultado |
|---|---|
| Contrato del prompt (`Test-PromptContractDocument`, perfil `task`) | `execution_safe = true`; los siete campos presentes y en orden; ninguno ausente, duplicado ni desconocido |
| Enlaces relativos de los documentos nuevos y modificados | todos resuelven |
| Nombres retirados de cierre | ninguno |
| `git diff --check` | limpio |
| Terminaciones de línea del roadmap y del plan | conservadas byte a byte; solo las líneas editadas cambian |

La verificación de este commit (`STATIC`) se comunica con la entrega. Un commit no puede
citar su propia identidad.

## 6. Limitaciones

1. El sondeo cubre los parámetros del autor y el barrido de excCyl de 3,6 a 5,0 con
   `rbCone` = 5. Los puntos aislados con `rbCone` > 7,5735 se deducen analíticamente y no se
   han medido.
2. El coste de la certificación por cajas y las constantes son candidatos; los fijará el
   banco de la implementación (D-CU2-5, D-CU2-10).
3. La captura de clase v3 de los algoritmos upstream (recta–cónica, intersección indexada,
   circunferencia por dos puntos y macros) se ha diseñado sobre su lectura, no prototipado.
4. El inventario de pruebas existentes afectadas se da por categorías (estrategia §5); se
   completará en la implementación.

## 7. Estado de autorización

`CU1` está `PASS — AUTHOR APPROVED` según su registro de cierre. La preparación de `CU2`
está ejecutada como candidato documental pendiente de revisión autoral.

La siguiente decisión autoral es aprobar la arquitectura de `CU2` (D-CU2-1 a D-CU2-11 y
E-1 a E-3) y autorizar expresamente su implementación sobre un commit base exacto
(D-CU2-12 a D-CU2-15).

La implementación de `CU2` y `CU3`–`CU10` no están autorizadas.
