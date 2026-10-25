# PRE-G9B-CU3 — Registro de verificación del candidato técnico

| Campo | Valor |
|---|---|
| Carácter | Registro documental de la verificación `INTEGRATED_PHASE` (puerta G-final) del candidato técnico `CU3` congelado en E4. Lo añade un commit documental descendiente, que no modifica el candidato ni es un candidato nuevo |
| Estado | `CU3 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`: `STATIC`, `INFRA_UNIT`, las cuatro `PHASE` e `INTEGRATION` son `ACCEPTED / COMPLETE` a la primera sobre el mismo candidato; `CU3-A` completo y `CU3-B` pendiente (puerta G-B no alcanzada); no se declara `PASS — AUTHOR APPROVED` y no se ejecuta el cierre autoral |
| Candidato | `3c2ca7e800001a19b96b686f1adb8fcc1866c796`, árbol `0f782b29e83716b9a7b57c0b4a82c02cf6288168`, en la rama local `phase/pre-g9b-cu3-implementation` (sin publicar) |
| Base autorizada | `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, árbol `9e4984db1dec327e47afeec2c1072f78c6e9cc49` |
| Informe | [informe del candidato](pre_g9b_cu3_implementation_candidate_report.md) y su espejo [`pre-g9b-cu3-candidate-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-candidate-evidence.json); [registro de E3](pre_g9b_cu3_e3_gate_record.md) |
| `selfApproved` | `false` |

## 1. Identidad y delta

| Comprobación | Resultado |
|---|---|
| Commits | 31 commits desde la base, de E0 a E4; la lista con su papel está en el espejo |
| Delta base → candidato | 156 archivos: 103 nuevos y 53 modificados, ninguno borrado; 28 644 inserciones y 359 borrados |
| Código de producto | 84 archivos bajo `source/` fuera de `src/test`: 57 nuevos y 27 modificados, entre ellos las tres tablas `menu*.properties`; 15 244 inserciones y 86 borrados |
| Pruebas | 29 archivos: 26 nuevos y 3 modificados; 7 529 inserciones y 40 borrados. No se elimina ninguna prueba |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` (base `9b93256b`) correcto sobre el candidato: 1 246 archivos registrados en `docs/upstream/modified-files.yml` |
| Referencias numéricas | ningún archivo de referencia de `CU0` cambia. Las tres fijaciones invertidas (`pins.H02` y `pins.H06` en E1, `pins.B08_B09` en E3) conservan su valor histórico en la línea base, y su prueba comprueba el estado invertido con `PreG9BCu0Baseline.inversionMessage`; las registran el [registro de E1](pre_g9b_cu3_e1_gate_record.md) §4 y el [registro de E3](pre_g9b_cu3_e3_gate_record.md) §3 |
| Libro y gobernanza | `geocedg_book`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/` y los prompts canónicos sin cambios. Solo cambió el prompt de tarea de `CU3`, en E0 y por instrucción autoral |
| Finales de línea | los archivos de finales mixtos o CRLF (roadmap, plan consolidado, `verification-registry.json`, `verification-final-coverage.Tests.ps1`, `IntersectionSemanticMetadata2D.java`, `modified-files.yml`) conservan sus terminadores, y sus líneas nuevas toman el de sus vecinas; `git diff --check` limpio en cada commit |
| Árbol de trabajo e índice | limpios antes y después de cada ejecución de aceptación, de cada banco y de cada sondeo |
| Publicación | `main` = `origin/main` = `e6c908f2203cd8feb5d4938013e16b212e0fe016`. El remoto tiene `phase/pre-g9b-cu3-implementation` en `1a0903aa` (la puerta G-M3), `phase/pre-g9b-cu3-authority-study` en `3cd0f710` y `phase/pre-g9b-cu3-preparation` en `80d72a50`; la rama local no se ha publicado |

## 2. Evidencia previa al commit

### 2.1 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol limpio de `06f2bb40` para el descubrimiento y las selecciones finales, y sobre ese árbol con
los cambios de registro e inventario de E4 para las dos selecciones nuevas.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0 | 1,4 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0 | 1,2 min |
| `final.shared` | ejecución | `COMPLETED`, código 0 | 4,8 min |
| `final.desktop` | ejecución | `COMPLETED`, código 0 | 20,2 min |
| `pre-g9b-cu3.shared` | ejecución | `COMPLETED`, código 0; 21 clases | 2,0 min |
| `pre-g9b-cu3.desktop` | ejecución | `COMPLETED`, código 0; 5 clases | 1,5 min |

El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` se ejecutó en la
sesión con las dos evidencias de descubrimiento y las cuatro de ejecución:

- `discovery.shared`: 6 523 → 6 534, y `final.shared`: 7 440 → 7 451 (+11: las pruebas nuevas de E3 en shared);
- `discovery.desktop`: 2 268 → 2 271, y `final.desktop`: 2 262 → 2 265 (+3: las de E3 en Desktop);
- `pre-g9b-cu3.shared`: 194, y `pre-g9b-cu3.desktop`: 14 (selecciones nuevas), con el commit E0
  `1f81666e` como `input_identities`;
- las demás selecciones no cambian; el pin `junit_inventory` pasa de `6531bd03…900d` a
  `165135e8…dd61`, y la prueba de cobertura fija 70 selecciones, 72 selecciones `PHASE` y los
  recuentos de las dos nuevas.

### 2.2 Ejecuciones de desarrollo

Sobre el árbol de trabajo con los cambios de E4, ligadas por el verificador a la identidad de
`HEAD` (`06f2bb40`, árbol `aa7200d3`). Son evidencia de contenido, no de aceptación.

| Perfil | `run_id` | Resultado |
|---|---|---|
| `INFRA_UNIT` | `verification-d268e334f98c4f60b4b71031e35e50d1` | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU3` | `verification-6d2c01abdec444a892225124103f023e` | `ACCEPTED / COMPLETE`, 4/4 |

## 3. Verificación de aceptación sobre el candidato

La clase de verificación autorizada es `INTEGRATED_PHASE` (D-CU3-14). Las ejecuciones fueron
secuenciales, el 2026-10-25, desde la raíz del repositorio, con el candidato como `HEAD`, el árbol
de trabajo y el índice limpios y sin cambios del árbol entre ellas; justo antes se ejecutó dos
veces el banco de E3 (§4):

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu3-final-static-3c2ca7e8
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu3-final-infra-unit-3c2ca7e8
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU3 -LogDirectory artifacts\agent\pre-g9b-cu3-final-phase-pre-g9b-cu3-3c2ca7e8
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu3-final-phase-pre-g9b-cu2-3c2ca7e8
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu3-final-phase-pre-g9b-cu1-3c2ca7e8
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu3-final-phase-pre-g9b-cu0-3c2ca7e8
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu3-final-integration-3c2ca7e8
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Todas terminaron con código de salida 0. Los `result_hash` se re-autenticaron con
`Read-VerificationJson` y `Get-VerificationResultHash`, y cada diario `run-journal.json` está
`COMPLETED` con la identidad del candidato.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-1dc93880abdb4bdd856185c3fc0aee76` | `64a59fcae3078ae66929d52e3dc1dca90b67e9a84c3d1eb22f989d2e1d846692` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-aca78b2377c34d0eb126e1b2588d669c` | `d6221fe6f00e411e8ae671146cc9f1da303d41f54a55fbc990f50abf34b225cd` | `830887b1…ce1d` | 2,1 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU3` | `verification-8510169cc054466cabb133cfffaed4f5` | `c28a6540c88446843a15786be042f2f5a3fc0a92a0fb03b56131f1460f57d6dd` | `1d298a3a…14f7` | 5,3 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu3.shared`: 21 clases y 194 pruebas; `pre-g9b-cu3.desktop`: 5 clases y 14 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU2` | `verification-6e0f2b9eb37840aaae30f9295b7f9547` | `8a31cbe3c2b24694ba281733ebd24375510c72c446cc78d286862194bc625a95` | `b9dc6d9e…a930` | 8,8 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu2.shared`: 21 clases y 242 pruebas; `pre-g9b-cu2.desktop`: 9 clases y 72 pruebas; 0 fallos |
| `PHASE -Phase PRE-G9B-CU1` | `verification-894460340c7949f3b730b3dc0f03ab14` | `c884af79794764d72730fabd1edde34ffd83fd194837c777e66add81d20da3d3` | `b0e4f130…4519` | 4,8 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos |
| `PHASE -Phase PRE-G9B-CU0` | `verification-a095cd55fee74652951da2f4d0c9a739` | `05ad18a5bf13bd90ec212d47ed962af6d2a4736ecd41be42f6ad6329a4111dd1` | `43e3e97e…9173` | 2,3 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas, con `pins.H02` y `pins.H06` invertidas; 0 fallos |
| `INTEGRATION` | `verification-24fe4249ab9d4835921623ea1962985c` | `5897fa3d54cf4632ba373e140b2dbd77c95aaf114e7ffc7a0db0e154209356fa` | `4502c4bd…7aea` | 49,1 min | `ACCEPTED / COMPLETE`, 19/19 |

Los planes de `STATIC`, `INFRA_UNIT`, las `PHASE` de `CU2`, `CU1` y `CU0` e `INTEGRATION` son los
mismos de las ejecuciones del candidato de `CU2` ([registro de verificación de `CU2`](pre_g9b_cu2_verification_record.md)
§4): las selecciones de `CU0`, `CU1` y `CU2` conservan sus identidades. El plan de
`PHASE -Phase PRE-G9B-CU3` es nuevo.

En `INTEGRATION` pasaron:

- `junit.shared.final`: 745 archivos y 7 451 pruebas, con 10 omitidas;
- `junit.desktop.final`: 194 archivos y 2 265 pruebas, con 1 omitida;
- `junit.desktop.g9u1-isolated`: 2 clases y 5 pruebas;
- `junit.desktop.revision3-diagnostic`: 1 prueba.

Los recuentos de las dos selecciones finales son los del inventario. Las omitidas son preexistentes
y upstream: en Desktop, `org.geogebra.CasValueExtractor.extractMockValues`; en shared, las mismas
10 pruebas `org.geogebra.*` de las ejecuciones de `CU0`, `CU1` y `CU2`.

Los diagnósticos de `STATIC` y de `INTEGRATION` son `diagnostic.governance` = `DIAGNOSTIC_FINDING`
y `diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`. Son las notas permanentes del
protocolo de recuperación de la infraestructura de verificación, iguales que en `CU0`, `CU1` y
`CU2`. Los demás son `DIAGNOSTIC_CLEAR`: en `STATIC`, `diagnostic.documentation`,
`diagnostic.guide-structure`, `diagnostic.style` y `repository.whitespace-diagnostic`; en
`INTEGRATION`, los cuatro de Checkstyle, `diagnostic.documentation`, `diagnostic.style`,
`junit.desktop.revision3-diagnostic` y `repository.whitespace-diagnostic`.

## 4. Banco de coste de E3

El banco `tools/benchmark/pre-g9b-cu/run-cu3-e3-cost.ps1` (prueba `PreG9BCu3E3CostProbe`) se
ejecutó dos veces sobre el candidato con el árbol de trabajo limpio
(`product_sources_modified = false`), antes de las ejecuciones de §3. Las salidas están en
`artifacts/benchmarks/pre-g9b-cu/cu3-e3-cost-3c2ca7e8-run{A,B}`, que Git ignora; código 0 las dos.
Los dos `cu3-e3-cost.json` solo difieren en los tiempos. La evidencia versionada es la ejecución A,
copiada byte a byte: [`pre-g9b-cu3-e3-cost-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e3-cost-evidence.json),
SHA-256 `f068b6ed4cb7028b3299af8c46501c8ae169e5711a8e319afc7803c483a3dc0f`. Los contadores son la
medida reproducible; los tiempos son informativos.

**Decisiones exactas** (certificado de identidad constructiva con el conductor en su parámetro
exacto, en el modelo adoptado):

| Contacto | Certificado | Orden y `h″` | Productos de términos | Términos | Bits | Pliegue de `CU2` | Mediana A / B |
|---|---|---|---:|---:|---:|---|---|
| `e_1·n` en `0` | `IDENTITY_CERTIFIED`, 26 condiciones de celda | estacionario, −1 | 279 910 | 5 915 | 104 | transferido por estructura | 124,7 / 129,4 ms |
| `e_1·c_1` en `0` | `IDENTITY_CERTIFIED`, 19 condiciones | estacionario, −1 | 311 202 | 5 915 | 14 | transferido por estructura | 173,0 / 179,6 ms |
| `o_1·h_2` en `0` | `IDENTITY_CERTIFIED`, 33 condiciones | tangente, +1 | 1 571 841 | 21 685 | 114 | `EVIDENCE_INSUFFICIENT`, `DECISION_NOT_IN_CERTIFICATE`: no hay pliegue en `0` | 862,1 / 850,1 ms |
| `e_1·c_1` en `π` | `IDENTITY_CERTIFIED`, 19 condiciones | estacionario, −1 | 311 202 | 5 915 | 14 | transferido por estructura | 173,2 / 176,1 ms |
| `e_1·r` en `π` | `IDENTITY_CERTIFIED`, 26 condiciones | estacionario, +1 | 279 910 | 5 915 | 105 | transferido por estructura | 120,3 / 124,0 ms |

- En las cinco decisiones `h′(t₀) = 0`. Las ocho indeterminadas son `A.x`, `A.y`, `B.y`, `rbCone`,
  `H.x`, `H.y`, `D_1.x` y `D_1.y`, con el corte en `H`. Las cinco repeticiones de cada intento dan
  resultados y contadores idénticos.
- La fuente residual de la consulta da en cada caso su contacto exacto, `CONTACT_EXISTENCE_EXACT`:
  orden `FOLD_CONTACT` en los pliegues de `e_1` y orden `TWO` en `o_1·h_2`, con la curvatura del
  signo de `h″`. El encierro del parámetro es `[0, 0]` en `0` y
  `[3,141592653589793, 3,1415926535897936]` en `π`.
- Frente al certificado en `K` y en `J` del banco de E2 ([registro de E2](pre_g9b_cu3_e2_gate_record.md)
  §6), los productos difieren entre −41 y +10, los términos son los mismos y los coeficientes de
  `e_1·c_1` son más cortos (14 bits frente a 104 y 105).

**Inyecciones** sobre `e_1·c_1` en `π`:

| Límite inyectado | Resultado | Contadores al detenerse |
|---|---|---|
| 1 000 productos de términos | `NOT_ESTABLISHED`, `BUDGET_EXHAUSTED` | 1 002 productos, 53 términos, 5 bits |
| 4 indeterminadas | `NOT_ESTABLISHED`, `BUDGET_EXHAUSTED` | 4 indeterminadas registradas y ningún producto |
| 50 términos | `NOT_ESTABLISHED`, `BUDGET_EXHAUSTED` | 363 productos, 53 términos, 3 bits |
| 8 bits | `NOT_ESTABLISHED`, `BUDGET_EXHAUSTED` | 42 585 productos, 3 806 términos, 12 bits |

Ninguna inyección decide nada ni se convierte en una aproximación.

**Documento** (tres repeticiones; mediana de A y de B):

- adopción del modelo con las cuatro intersecciones públicas de los contactos: 2 309,5 y 2 315,2 ms;
- arrastre de `H` dentro de su celda: 60,5 y 78,8 ms con la política heredada, y 766,6 y 749,4 ms
  con la constructiva;
- tras la adopción, las cuatro intersecciones publican exactamente los contactos del registro de E3
  §4: `u_1` en `0` con orden `TWO`; `Intersect(e_1, n)` en `0`; `Intersect(e_1, c_1)` en `−π` y en
  `0`; e `Intersect(e_1, r)` en `−π`; los pliegues con orden `FOLD_CONTACT`. Todos son
  `TANGENT_ESTABLISHED`, `EXISTENCE_EXACT` y `CONTACT_EXISTENCE_EXACT`, sin token admisible.

**Presupuesto.** Los mayores observados con el presupuesto fijo son 1 571 841 productos de términos
(9,8 % de 16 000 000), 8 indeterminadas (50 % de 16), 21 685 términos (10,8 % de 200 000) y 114 bits
(2,8 % de 4 096). Con el banco de E2, el máximo de bits es 700 (17,1 %), en el alcance de frontera.
Los techos provisionales de `constructive-identity-certificate-budget/v1` bastan con margen: ningún
límite se eleva ni se propone elevar, y su aceptación productiva la decide el autor (registro de
aprobación §16).

## 5. Contadores GC3

La estrategia (§4) pide los contadores GC3-1 a GC3-5 de la [matriz](pre_g9b_cu3_validation_matrix.md)
(§2.2) antes y después:

- **Antes.** La base no tiene ninguna de estas evaluaciones: las envolventes del residuo de
  contacto y la evaluación exacta llegan con E1. La política heredada del candidato es la
  referencia de GC3-2 y GC3-5.
- **Después.** Se midieron sobre el candidato el 2026-10-26 con un sondeo de diagnóstico del
  directorio temporal de la sesión (`PreG9BCu3Gc3DiagnosticProbe`). Se añadió al conjunto de
  pruebas de Desktop con el script de inicialización sin cambios del arnés de `CU0` y se retiró
  después del árbol de compilación, como los bancos.
- **Método.** El sondeo envuelve la configuración de decisión del documento para contar el trabajo
  que una consulta pública pide a su fuente residual; no cambia el producto.
- **Reproducibilidad.** Dos ejecuciones dan salidas idénticas byte a byte (SHA-256
  `c3229d62fcc0b32346c4d58df265d13f588fd92f0d479cd58c6549d2b577c760`), con el árbol limpio antes y
  después. Es evidencia de diagnóstico, no un banco versionado.

| Contador | Criterio (matriz §2.2) | Medida sobre el candidato | Lectura |
|---|---|---|---|
| GC3-1 | coste de la evaluación exacta por contacto | `Point(e_1, K)`, `Point(o_1, K)` y `Point(e_1, J)`: 83 operaciones, profundidad 0 y 109 bits; `Intersect(e_1, n, K)` e `Intersect(e_1, r, J)`: 553 operaciones, profundidad 1 y 385 y 389 bits; `Intersect(e_1, c_1, K)` e `Intersect(e_1, c_1, J)`: 489, 1 y 477; `Intersect(o_1, h_2, K)`: 1 531, 2 y 478 | dentro de `cu3-exact-evaluation-budget/v1` (100 000 operaciones, profundidad 8 y 4 096 bits), en `Σ_C`, la configuración de decisión de la autoridad constructiva (SM-B-11), no en `Σ_L⁺`. Frente al banco de E1 ([registro de E1](pre_g9b_cu3_e1_gate_record.md) §6), cuatro contactos usan 10 operaciones más (553 y 489 frente a 543 y 479); desde E1 solo el commit de E2 `86be18d1` cambió la evaluación exacta |
| GC3-2 | cajas de las envolventes de `h`, `h′` y `h″` por contacto | por consulta, con la política heredada y con la constructiva: `o_1·h_2`, 542 y 506; `e_1·n`, 301 y 283; `e_1·c_1`, 502 y 2 173; `e_1·r`, 1 y 908 | la política constructiva añade el análisis de los pliegues de `e_1` en `Σ_C` (E3): la consulta de `e_1·r` pasa de 1 caja a 908 y la de `e_1·c_1` se multiplica por 4,3. Una nueva computación en la misma revisión repite las mismas cajas: la evidencia certificada de una consulta se recalcula en cada computación. Solo se midieron los cuatro contactos del modelo |
| GC3-3 | arrastre del conductor sin cambio de revisión | `E_1` + 0,3: las revisiones de `e_1` y `o_1` no cambian y los ocho puntos estructurales conservan su resolución | ninguna evaluación exacta nueva; también V-AUT-DYN-3 y el banco de E1 |
| GC3-4 | cambio de `rbCone` | `rbCone` de 5 a 5,1: las revisiones de `e_1` y `o_1` pasan de 1 a 2; cada punto estructural tiene una resolución nueva, `STRICT`, y otra computación en la misma revisión devuelve la misma; los cinco certificados de identidad constructiva se conservan (misma clave y celda, recomprobados) | una evaluación exacta por revisión y por punto estructural |
| GC3-5 | regla de conflicto en un barrido de raíces transversales genéricas | cinco rectas que cortan dos veces una circunferencia trazada: 6 cajas por consulta con las dos políticas, ninguna consulta de contactos exactos y ningún diagnóstico `CONTRADICTED_BY_CERTIFIED_EVIDENCE` ni `CONTACT_EVIDENCE_CLASSIFIED`; las dos raíces son `TRANSVERSE_ESTABLISHED` con `EXISTENCE_CERTIFIED` | el análisis de la regla no se dispara y la política constructiva no añade trabajo; también V-EVID-6 (`PreG9BCu3CertifiedEvidenceTest`) |

## 6. Impacto en el arranque, en la infraestructura y en la guía

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: el candidato no cambia prerrequisitos de la estación, versiones de JDK o de
herramientas, el comportamiento de Gradle, los puntos de entrada de compilación o de verificación,
fuentes generadas, prerrequisitos de empaquetado ni archivos de referencia numérica. Sus cambios de
verificación son datos del registro y del inventario, y fijaciones de su prueba de cobertura,
hechos con los mecanismos oficiales.
```

- **Infraestructura de verificación.** El candidato cambia la selección de pruebas: la fase
  `PRE-G9B-CU3` y sus dos selecciones en `verification-registry.json`, el inventario JUnit, su pin y
  las fijaciones de `verification-final-coverage.Tests.ps1`. La clase autorizada `INTEGRATED_PHASE`
  (D-CU3-14) incluye `INFRA_UNIT`, con sus 22 hojas de infraestructura, e `INTEGRATION`, como en
  `CU2`. `FINAL` no lo exige la clase y queda para `CU10`.
- **Guía.**

  ```text
  GUIDE_IMPACT = NONE
  GUIDE_JUSTIFICATION = la guía de usuario corresponde a CU9 (plan consolidado y prompt de tarea de CU3), como en CU2.
  ```

  El candidato sí cambia contratos observables: las formas `Point(L, D)` e `Intersect(L, r, D)`, la
  oferta de adopción con su informe, el registro de política persistido y el rechazo cerrado de los
  lectores anteriores. Su documentación de usuario y de desarrollo queda pendiente (informe del
  candidato §8): `NONE` registra aquí ese aplazamiento, no la ausencia de cambios observables.

## 7. Lo que no se ha ejecutado

- `FINAL`: la clase autorizada no lo exige y el plan lo reserva para `CU10`. Por eso no hay recibo
  de verificación.
- Las pruebas manuales del autor (informe del candidato §9): corresponden a la revisión autoral y
  nunca se simulan.
- El escenario ventaneado del arnés de `CU0`: no se repitió.
- GC3-2 en los casos sintéticos de la matriz: solo se midieron los cuatro contactos del modelo.

## 8. Limitaciones

Son las de §6 del informe del candidato. Además:

1. La evidencia del inventario (§2.1) y las ejecuciones de desarrollo (§2.2) se produjeron antes
   del commit del candidato, sobre el árbol de trabajo. Las ejecuciones de aceptación (§3), el banco
   de E3 (§4) y el sondeo de GC3 (§5) son los únicos ligados a la identidad del candidato.
2. El sondeo de GC3 es evidencia de diagnóstico ejecutada desde el directorio temporal de la
   sesión, no un banco registrado.
3. Bajo la política constructiva, cada computación de una intersección del modelo recalcula sus
   envolventes (GC3-2), y el arrastre de `H` tarda entre 9 y 13 veces más que con la política
   heredada (§4). Es tiempo de respuesta, no un presupuesto, y no cambia ninguna decisión; amplía la
   evidencia de la quinta decisión del informe del candidato (§10).
4. Los tiempos son de una sola estación y no constituyen puertas.

## 9. Contenido de este commit documental

Este commit:

- añade este registro;
- añade la evidencia versionada del banco de E3 (§4);
- actualiza a la versión 12 el [registro de transferencia científica E1](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md):
  el código, las pruebas y la verificación de los métodos que el candidato implementa, sin cambiar
  ningún estado, como exige su §8;
- registra el resultado de la verificación en el roadmap.

No modifica código, pruebas, registro, inventario ni las evidencias existentes. El informe del
candidato y su espejo ya enlazan este registro.

## 10. Estado de autorización

`CU3` sigue siendo `CU3 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`, con `CU3-A` completo y
`CU3-B` pendiente. La verificación no añade decisiones a las seis de §10 del informe del candidato.
No se publica nada, no cambian `main`, las etiquetas, las releases ni `geocedg_book`, y no se
ejecuta el cierre autoral.
