# PRE-G9B-CU3 — Registro de las puertas del incremento E3

| Campo | Valor |
|---|---|
| Carácter | Registro técnico del incremento E3 bajo la autoridad constructiva: la forma «una raíz doble en un punto exacto» de la regla de conflicto en `Σ_C`, con el certificado de paridad por simetría exacta, la reclasificación de la costura, la inversión de `pins.B08_B09` y la puerta G-A; `CU3-B` se presenta como pendiente (puerta G-B no alcanzada). Es evidencia técnica: no aprueba nada, no sustituye la decisión autoral y no declara `CU3` `PASS — AUTHOR APPROVED` |
| Autoridad | [registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md) §5 a §10, §16 y D-AUT-11; [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §6.2 a §6.4; [diseño de E3](../architecture/pre_g9b_cu3_e3_implementation_design.md); [diseño de la Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §9; [matriz](pre_g9b_cu3_validation_matrix.md) §3.4, §3.5b y §3.9; [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md), enmienda 1.4 |
| Rama | `phase/pre-g9b-cu3-implementation`, local, sin publicar |
| Base | E2 en `91222e312fed9756225407be6f0c72a1eafeac6e`, con sus puertas registradas en `5a1ac888ebcd88006ccfb94ff51bf97b34b8c92d` ([registro de E2](pre_g9b_cu3_e2_gate_record.md)) |
| Commit del código de E3 | `06f2bb409713b82a27b7da10094fc54822f80002` (árbol `aa7200d30ae73c87ed67d72c4aa501154cb8ab08`) |
| Verificación | `INTEGRATED_PHASE` sobre el candidato congelado de E4, que contiene E3 ([informe del candidato](pre_g9b_cu3_implementation_candidate_report.md) y [registro de verificación](pre_g9b_cu3_verification_record.md)) |
| `selfApproved` | `false` |

## 1. Alcance y método

E3 añade a la regla de conflicto de CU3.4, solo bajo `constructive-authority/v1`, la cuarta forma
del certificado de conteo (Etapa B §9.1): una raíz doble en un parámetro exacto `t₀ = k π/2` de un
conductor sobre una circunferencia del marco de los ejes. `h(t₀) = 0` lo establece el certificado
de identidad constructiva de E2 con el conductor en el punto exacto de su circunferencia; `h′(t₀) =
0` lo dan sus jets exactos; el pliegue de `CU2` en `t₀`, transferido por estructura, es el
certificado de paridad por simetría exacta y da el orden `FOLD_CONTACT`; y `h″` de signo fijo en la
caja hace de `t₀` la única raíz, doble ([diseño de E3](../architecture/pre_g9b_cu3_e3_implementation_design.md)
§2). Bajo la política heredada nada de esto se aplica.

Cada fila de la matriz que la autorización asigna a E3 tiene su prueba, citada por su identificador
(§3). El coste se mide con un banco fuera del producto (§5).

## 2. Commits de E3

| Commit | Contenido |
|---|---|
| `06f2bb409` | forma `ONE_DOUBLE` del certificado de conteo; disparador en los pliegues certificados bajo `Σ_C`; contradicciones de existencia, conteo y multiplicidad de la raíz doble; certificado y recomprobación en un parámetro exacto; pliegue de `CU2` en un parámetro exacto; decisiones exactas conservadas y recomprobadas entre revisiones; orden de contacto en la evidencia; informe de adopción con la regla de conflicto; pruebas de E3 y la de E1 que cambia de expectativa; banco de coste |

## 3. Puertas y filas de la matriz

| Puerta | Criterio (estrategia §2) | Evidencia | Resultado |
|---|---|---|---|
| G-A | todas las filas obligatorias de `CU3-A` de la matriz, más las regresiones de §3.9 | las filas de E1 ([registro de E1](pre_g9b_cu3_e1_gate_record.md) §3 y §4) y de E2 ([registro de E2](pre_g9b_cu3_e2_gate_record.md) §3 y §4), más las de E3 de esta tabla; §3.9: `pins.B08_B09` invertido en el modelo adoptado, `pins.H02` y `pins.H06` invertidos en E1, `preserve.contacts` igual, y las selecciones `PHASE` de `CU0`, `CU1` y `CU2` e `INTEGRATION` sobre el candidato ([registro de verificación](pre_g9b_cu3_verification_record.md)) | superada con la verificación del candidato |
| G-B | V-B-7 a V-B-10 y V-B-12, y V-B-1 en su forma de clase 2; sin regresión de G-A | `CU3-B` no se implementa (§7) | no alcanzada; `CU3-B` pendiente |

| Filas | Prueba |
|---|---|
| V-B-2, V-CONF-5 | `ConstructiveExactContactTest` (réplica): la raíz transversal de la costura de `e_1·c_1` se reclasifica como el contacto en el pliegue de `π`, con `EXISTENCE_EXACT`, `CONTACT_EXISTENCE_EXACT` y orden `FOLD_CONTACT`, sin token admisible; el contacto de `t = 0` es clase 2; solo quedan esos dos; bajo la política heredada CU3.4 no cambia; Deshacer restaura la raíz transversal y su token. `PreG9BCu3SeamReclassificationAuthorModelTest`: lo mismo en el modelo del autor. `PreG9BCu3ExactDoubleRootTest`: la forma y sus contradicciones, con una fuente que declara una decisión exacta |
| V-B-NOISE-1 | `PreG9BCu3SeamReclassificationAuthorModelTest`: con `α_cone` 35° la política heredada publica siete raíces de `e_1·c_1`, dos admitidas; tras la adopción quedan los dos contactos en pliegue. Con `D_1` a la mitad de su distancia a `H`, `u_1 = Intersect(o_1, h_2)` pasa de cuatro raíces a la tangencia de orden dos |
| V-AUT-ADOPT-8 (parte de E3) | `ConstructiveExactContactTest` y `PreG9BCu3SeamReclassificationAuthorModelTest`: el informe nombra antes de aplicarla cada raíz admitida que la regla retira, fusiona o reclasifica, con la causa `CONTRADICTED_BY_CERTIFIED_EVIDENCE` |
| V-B-1 (clase) | `ConstructiveIntersectionAdmissionTest` y `ConstructiveExactContactTest`: tras la adopción los contactos de `t = 0` son clase 2 con su orden (`FOLD_CONTACT` para `e_1`, `TWO` para `o_1·h_2`); su materialización sin conductor es `CU3-B` |
| §3.9, `pins.B08_B09` | `PreG9BCu3SeamReclassificationAuthorModelTest.theSeamOfTheAdoptedModelIsTheFoldContactAtPi`: el pin histórico se conserva y el estado adoptado se comprueba con `PreG9BCu0Baseline.inversionMessage("B08_B09", "CU3", …)`; `preserve.contacts` con la tolerancia de `CU0`; `PreG9BCu0LocusReferenceTest` mide el modelo bajo la política heredada sin cambios |
| controles adversarios | `ConstructiveExactContactTest`: un presupuesto agotado no decide nada; una circunferencia que no contiene `F(π)` por su radio binary64 no es identidad, y nada recibe existencia exacta; una revisión posterior en la misma celda recomprueba el mismo certificado. `PreG9BCu3ExactDoubleRootTest`: un signo exacto de `h″` que contradice su encierro y un parámetro exacto fuera del corchete no certifican nada; sin decisión exacta la clase 3 no cambia nada (V-CONF-4) |

V-B-11 quedó en E1 sin la decisión exacta: el cuarto de arco está fuera de `constructive-class/v1`
(registro de E1 §4).

## 4. Los contactos sin conductor tras E3

Resultado rico de las cuatro intersecciones del modelo del autor (sin `e_2`) tras la adopción:

| Intersección | Antes (política heredada) | Después (`constructive-authority/v1`) |
|---|---|---|
| `u_1 = Intersect(o_1, h_2)` | tangencia en `0`, clase 3 | tangencia en `0`, clase 2, `EXISTENCE_EXACT`, orden `TWO`, sin token admisible |
| `Intersect(e_1, n)` | contacto indeterminado en `0`, clase 3 | contacto en `0`, clase 2, orden `FOLD_CONTACT` |
| `Intersect(e_1, c_1)` | raíz transversal admisible en `−π` (costura) y contacto indeterminado en `0`, clase 3 | contacto en el pliegue de `π` (costura) y contacto en `0`, los dos clase 2, `FOLD_CONTACT`, sin token admisible |
| `Intersect(e_1, r)` | raíz transversal admisible en `−π` (costura) | contacto en el pliegue de `π`, clase 2, `FOLD_CONTACT` |

Los puntos publicados son los mismos (`preserve.contacts`). Los signos de `h″` son los del estudio
(§1.7): negativos para `e_1·n` y `e_1·c_1`, positivos para `o_1·h_2` y `e_1·r`. Los puntos de los
contactos se crean con `Intersect(L, r, K)` e `Intersect(L, r, J)` (E1 y E2), con `P ∈ r`
`STRICT`.

## 5. Coste

Banco `tools/benchmark/pre-g9b-cu/run-cu3-e3-cost.ps1` (prueba `PreG9BCu3E3CostProbe`), fuera
del producto. Para cada contacto del modelo adoptado mide el certificado de identidad constructiva
con el conductor en su parámetro exacto (estado, orden, signos y contadores frente a
`constructive-identity-certificate-budget/v1`), el pliegue de `CU2` en ese parámetro, los contactos
exactos que da la fuente residual de la consulta, la inyección de cada límite, la repetición de
cada intento, y el tiempo de la adopción y de un arrastre de `H` con la política constructiva y con
la heredada. Se ejecuta dos veces sobre el candidato congelado; los resultados y la evidencia
versionada están en el [registro de verificación](pre_g9b_cu3_verification_record.md). El
presupuesto lo hace cumplir el núcleo; el banco solo lo mide, y los tiempos son informativos.

## 6. Compatibilidad y persistencia

- La regla nueva solo actúa bajo `constructive-authority/v1`. La configuración capturada de la
  política heredada no da decisiones exactas y el disparador nuevo solo existe en `Σ_C`: las suites
  G8B, G8C1, G8C2 y G9U0-R4 y las referencias de `CU0` no cambian.
- Ninguna identidad, token ni selector cambia de formato. La raíz de la costura reclasificada pierde
  la admisibilidad de su token transversal y nunca se reasocia; Deshacer la adopción restaura la
  raíz y su token.
- El orden de contacto, el grado y las decisiones exactas son evidencia de la revisión: no se
  guardan en el documento ni entran en un token.
- La adopción sigue siendo explícita, atómica y reversible, y su informe recoge ahora el efecto de
  la regla de conflicto.

## 7. `CU3-B`: pendiente

`CU3-B` (germen tangente, ciclo de vida y materialización sin conductor de las clases 1 y 2) no se
implementa en este candidato. La estrategia §2 lo prevé: el candidato se congela con `CU3-A` y
`CU3-B` se presenta como pendiente sin rebajar ninguna evidencia. Los motivos, que piden decisiones
autorales y no solo ingeniería, están en el [diseño de E3](../architecture/pre_g9b_cu3_e3_implementation_design.md)
§4:

1. V-B-9 (D-CU3-11) exige que un pliegue desdoblado conserve una sola identidad, lo que cambia el
   comportamiento de dos tokens transversales que hoy existen: riesgo de reasociación.
2. Los dos contactos de `e_1·c_1` tendrían el mismo germen, y el desempate por fase rechaza la
   costura: fallarían cerrados sin una regla de fase nueva para el punto fijo de un pliegue.
3. El germen aprobado incorpora la clase de evidencia y el orden al token (frente a V-EVID-3).

## 8. Limitaciones y cuestiones abiertas

1. **Coste de un arrastre.** Bajo la política constructiva, cada revisión del modelo vuelve a
   comprobar las decisiones exactas de los cinco contactos y a transferir sus pliegues, y a analizar
   en `Σ_C` las cuatro intersecciones (§5). El banco mide el arrastre de `H` frente a la política
   heredada. Es tiempo de respuesta, no un presupuesto, y no cambia ninguna decisión.
2. **Parámetros exactos.** Solo `k π/2` en conductores sobre circunferencias del marco de los ejes.
   Un conductor sobre un segmento no da ninguna decisión exacta en E3.
3. **Paridad sin identidad.** Sin la identidad en `t₀`, la paridad no certifica ningún conteo ni
   reclasifica una raíz cuyo intervalo aislante no es simétrico respecto del pliegue; queda la
   contradicción solo del conjunto (diseño de E3 §2).
4. **Fallo cerrado.** Si ningún binary64 de `t₀` pasa la comprobación de residuo, los candidatos que
   contienen `t₀` quedan como estaban.

## 9. Pruebas manuales del autor

- En el modelo adoptado, `Intersect(e_1, c_1)` y `Intersect(e_1, r)`: la costura aparece como
  contacto en el pliegue, no como raíz transversal; el inspector muestra la evidencia.
- `α_cone` 35°: no queda ningún punto de ruido junto a los contactos.
- Deshacer la adopción: la raíz de la costura vuelve con su token.
- Un arrastre de `H` en el modelo adoptado: tiempo de respuesta frente al documento sin adoptar.
