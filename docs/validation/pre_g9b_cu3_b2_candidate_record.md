# PRE-G9B-CU3 — Registro del candidato `CU3-B2`: tangencias regulares certificadas

| Campo | Valor |
|---|---|
| Carácter | Registro del candidato técnico de G-B2-2 y de su verificación G-B2-3, pedido por la decisión autoral de 2026-10-28 («Aprobación de G-B2-1 y autorización condicionada de implementación»). No aprueba nada; se detiene antes del smoke autoral (G-B2-4) |
| Candidato | `2d13ad4c9b61d6f443b691b8214a8b2361ad0451` (árbol `64dca8e5138050c0e1c5fc6b659dd33ffa430728`) en `phase/pre-g9b-cu3-implementation`, sin publicar |
| Sustituido | `a5bd18b8` (§5): su campaña se aceptó, pero el corpus reveló un efecto en certificados de `CU3-A` y `CU3-B1` |
| Estado | `CU3-B2 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `CU3` global sin `PASS — AUTHOR APPROVED`; `DOC-CU3-CU9-STRUCTURAL-POINTS` sigue bloqueante para ese cierre; deudas de `CU3-B1` con su trazabilidad; nada publicado |

## 1. Decisiones implementadas

| Decisión | Implementación |
|---|---|
| D-B2-1(a), `CONTACT_SPLIT` sin transferencia | el germen regular es disjunto de los transversales y de los de pliegue: ninguna raíz transversal puede resolver su selector ni heredar su token; las transversales siguen ADR 0017 sin cambios |
| D-B2-2(a), lado `σ` | signo de `h''(t₀)` decidido exactamente por el certificador de identidad constructiva, multiplicado por el factor de la orientación canónica del objetivo, también exacto: en una recta, el signo del coeficiente normal de mayor módulo (gana `x` en un empate exacto); en una circunferencia, `+1` (nivel del centro `−r² < 0`). Solo se decide tras certificar la tangencia |
| D-B2-3(a), libro v6 con `T` | entrada `T,` con la aridad de `S` y `F`; la versión 6 se escribe si hay una entrada `F` o `T`; los lectores v5 y anteriores rechazan cerrado. La condición se mantiene: ningún lector v6 se ha distribuido |
| D-B2-4(a), idempotencia | corrección K6 en la causa (§2), sin tocar el contador |
| D-B2-5(a), redefinición del soporte | sin camino de redefinición en el núcleo (K5); sin cambio |
| D-B2-6(a), fase y rango | el selector regular admite fase y rango de ADR 0017; la etiqueta de pliegue sigue sin rango |
| D-B2-7(a) | G-B2-1 antes del código |
| P1 | ancla solo con orden `TWO` del certificado exacto (velocidad de la traza no nula, `h'' ≠ 0` certificado), certificado `ONE_DOUBLE`, `t₀` interior a su componente, sin pliegue certificado y con un objetivo recta o circunferencia. Un objetivo limitado no se ancla, porque su parte limitada aún no se decide sin tolerancias |

**Enmiendas aplicadas:**

- `locus-v2-intersections.md`, versión 1.6, aprobada como contrato y no como producto;
- ADR 0039, enmienda de 2026-10-28;
- ADR 0017, nota de aplicación sin cambio de decisiones.

**Diagnóstico tipado nuevo:** `REGULAR_CONTACT_ANCHORED`, con sus etiquetas en inglés y en español.

## 2. Puerta K6

[Revisión focal](../architecture/pre_g9b_cu3_b2_k6_load_policy_review.md), documentada antes del cambio.

- **Cambio.** La sesión de carga publica el registro de autoridad validado en cuanto lo lee (la sección
  precede a los elementos) y `abort()` restaura la política anterior.
- **Sin efectos colaterales.** No cambia el formato, ni el contrato de identidad, ni el ciclo de vida
  general de carga.

| Punto exigido | Evidencia |
|---|---|
| 1, documentos heredados y constructivos | `PreG9BCu3B2LoadPolicyTest` k6x1 y k6x2 |
| 2, carga, reapertura y guardado sin cambios | k6x2, k6x3: estado del libro idéntico byte a byte |
| 3, rechazo cerrado | k6x4: registro duplicado o de versión desconocida |
| 4, atomicidad | k6x4 y k6x6: `abort()` restaura la política anterior |
| 5, encarnaciones y reclamaciones | k6x2 y la sonda G-B2-1 sobre el candidato: contador estable y ninguna asignación en la carga |
| 6, anclas `F` reclamadas y no reclamadas | k6x2 y k6x3: el token de un ancla no reclamada ya no cambia al reabrir |
| 7, entradas `T` | `PreG9BCu3RegularTangentTest` b2x12And17 |
| 8, Deshacer/Rehacer, copia y duplicación | k6x7 y b2x12And17 |
| 9, formatos anteriores | k6x1 (versión 4) y b2x12 (lector v5) |
| 10, ninguna evaluación con otra política | sonda G-B2-1 (`cu3-b2-g1-candidate-2d13ad4c`): 0 asignaciones en la carga en las cuatro variantes, determinista salvo identificadores (SHA-256 normalizado `a8c5d7d4…`) |

**Límite.** La garantía vale para el orden que escribe el producto. Un archivo editado a mano con la
sección después de un elemento conserva el comportamiento anterior en ese elemento.

## 3. Pruebas por fila

| Filas | Naturaleza | Prueba |
|---|---|---|
| B2-1, B2-4, B2-12, B2-17 a B2-19, B2-21 | producto, configuraciones reales (réplica del modelo del autor, objetivos libres, fuente singular) | `PreG9BCu3RegularTangentTest` (7) |
| B2-5 a B2-7, B2-10, B2-11 | propiedades del germen y del selector, en claves canónicas | `PreG9BCu3RegularTangentGermTest` (6) |
| B2-13, B2-22 | producto: reapertura, Deshacer/Rehacer, rechazo y atomicidad | `PreG9BCu3B2LoadPolicyTest` (7) |
| B2-2, B2-3 | invariancia demostrada en el diseño (§3.2); en el germen, la familia y el lado | `PreG9BCu3RegularTangentGermTest` |
| B2-8, B2-9 | **no alcanzables** moviendo un objetivo (K1, K2). Sin ancla actual, el token reclamado queda latente por las reglas generales de ADR 0017 | — |
| B2-14 a B2-16 | K5: no hay camino de redefinición del soporte. Las redefiniciones en sitio están cubiertas por `PreG9BCu3FoldContactTest` b1x16 | — |
| B2-20 | rendimiento y corpus | §4 |
| B2-23 | objetivo limitado: sin ancla, por la familia del objetivo | código (`CertifiedContactAnalysis2D.regularAnchor`) |

**Alcance de lo demostrado.**

- **Observado con cambios dinámicos reales:** reapertura, Deshacer/Rehacer, duplicación, vista y
  redefiniciones en sitio.
- **Demostrado en el selector y el germen:** separación de familias, lados, rangos y marcador local.
- **No alcanzable hoy en el producto:** la transición dinámica tangencia ↔ pareja y la ausencia
  certificada.

No se afirma que una transición dinámica general esté validada.

**Inversiones explícitas de pines.** En estas pruebas, `u_1` pasa a ser admisible solo por su germen
regular:

- `ConstructiveEarlyExactDecisionTest`;
- `ConstructiveExactContactTest`;
- `PreG9BCu3FoldContactTest` b1x18;
- `PreG9BCu3SeamReclassificationAuthorModelTest` (`B08_B09`).

## 4. Corpus, persistencia y rendimiento frente a `CU3-B1` (`dd3a2676`)

**Corpus del modelo del autor** (14 configuraciones × 10 objetos).

- Idénticos salvo identificadores: todos menos `u_1` y `cu3X4`, que es el mismo par
  `Intersect(o_1, h_2)`.
- En esos dos, las soluciones llevan ahora el germen regular y `REGULAR_CONTACT_ANCHORED`.
- Los certificados de su tangencia ganan dos condiciones: el pivote exacto de la orientación canónica.
- `X1`–`X5`, `cu3X1`–`cu3X3` y los pliegues de `CU3-B1` no cambian.

**Rendimiento** (banco E3, mediana del arrastre en ms, tres ejecuciones; `CU3-B1` → `CU3-B2`):

| Ejecución | adopción | `H` heredada | `B` heredada | `H` constructiva | `B` constructiva |
|---|---|---|---|---|---|
| A | 2617 → 2274 | 83,8 → 70,7 | 55,9 → 52,2 | 51,5 → 46,4 | 46,4 → 47,1 |
| B | 2212 → 2279 | 72,2 → 71,7 | 50,9 → 54,5 | 47,3 → 46,1 | 44,7 → 45,2 |
| C | 2296 → 2289 | 78,1 → 66,5 | 51,6 → 50,7 | 45,9 → 44,9 | 45,9 → 45,0 |

Equivalente dentro de la variación entre ejecuciones. Salidas en
`artifacts/benchmarks/pre-g9b-cu/cu3-b2-candidate-2d13ad4c` (ignorado).

## 5. Candidato sustituido `a5bd18b8`

- **Campaña.** La campaña `INTEGRATED_PHASE` de `a5bd18b8` fue `ACCEPTED / COMPLETE` y está
  autenticada, con INTEGRATION `verification-527cd78e…` 19/19.
- **Defecto revelado por el corpus.** El pivote de la orientación canónica se decidía para **todo**
  objetivo recta, y cada signo certificado entra en la celda del certificado de identidad constructiva.
  Las celdas de los certificados de pliegue y estructurales de `CU3-A` y `CU3-B1` pasaban de 26 a 28
  condiciones (`cu3X1`, `cu3X3`), y los arrastres eran unas 1,7 veces más lentos.
- **Corrección.** `2d13ad4c` decide el pivote solo tras certificar la tangencia.
- **Evidencia.** No se reutiliza ninguna evidencia de `a5bd18b8`.

## 6. Verificación G-B2-3 del candidato `2d13ad4c`

Comprobación con `Read-VerificationJson` y `Get-VerificationResultHash`: diario `COMPLETED`, commit y
árbol exactos.

| Comprobación | `run_id` | Resultado | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-37fbc33a6da54ba0a5f3d623008e3333` | `ACCEPTED / COMPLETE` 3/3 | `48a742d63b6ccbf22b71a128480b41f32b66707515163ecc49868ef0492036c4` |
| `INFRA_UNIT` | `verification-743207ebe4854dc19693d383469c710c` | `ACCEPTED / COMPLETE` 22/22 | `fff8a916c6c65461c8c7e44d5b6c7a5200903fb4ce55321cbe571bb804127b27` |
| `PHASE` `PRE-G9B-CU3` | `verification-b26af47733144bf69cfbfb19b4220c4c` | `ACCEPTED / COMPLETE` 4/4 | `5709e80fac9a8681fec7e5286455ee20c63e4536068eccf51756a9a2bef67093` |
| `PHASE` `PRE-G9B-CU2` | `verification-d3b790179f664c65a7c51563c5730f17` | `ACCEPTED / COMPLETE` 4/4 | `10a96551ef80d6ee56229cf04026a37b10970527aa335ea94affcad32d08aabf` |
| `PHASE` `PRE-G9B-CU1` | `verification-433453be2d524b3291ac4cac590d0725` | `ACCEPTED / COMPLETE` 4/4 | `902433b526d9d9617f6e955353bad7351e778fcde09b76ad6dde1f59ce3eadc2` |
| `PHASE` `PRE-G9B-CU0` | `verification-f5a4321be18d4fbca0198652f608ee48` | `ACCEPTED / COMPLETE` 2/2 | `5ebad2b17a235f150dc7a86a110d8cb15880ecb419076c37730503c5f484c8a9` |
| `INTEGRATION` | `verification-8a6d1632f8c04a6aaa72d0442b46d63b` | **`REJECTED_SEMANTIC`** 18/19 | `7113af93d766931c12062e151d435ef83c0946d8ce63322ce1130c8f67ad9e9d` |
| `INTEGRATION` (reejecución) | `verification-b5021e247450476584409abe807cfcd7` | `ACCEPTED / COMPLETE` 19/19 | `2b144564fcf9b4abea015e48104db83b8e4020598928e40acb6d01b8e8170759` |

**Sobre el rechazo.**

- **Causa.** El rechazo está en `junit.desktop.final`: `PreG9BR6PlusE3IsoABorderDesktopTest`
  `coherenceUseSheetScaleAndActivationWarning`, con `NullPointerException` en
  `Component.getMaximumSize`.
- **Diagnóstico.** Es la carrera previa y conocida del tamaño de la barra de herramientas fuera del
  EDT, ajena a `CU3-B2`. El mismo código de escritorio pasó 19/19 en `a5bd18b8`.
- **Tratamiento.** El rechazo no se reetiqueta. INTEGRATION se repitió sobre el mismo commit en un
  directorio nuevo.

**Clase de verificación.** El cambio de carga (K6) está acotado a la sesión de carga de documentos con
registro de autoridad. INTEGRATION cubre las suites completas de persistencia compartida y de
escritorio. No se ha ejecutado `FINAL`, ni se han cambiado las reglas de verificación.

## 7. Limitaciones de la certificación exacta

- La clase 2 de una tangencia regular solo se alcanza en `u_1` y en su par `cu3X4` (K1).
- La desaparición de una tangencia anclada da `CONTACT_NOT_CERTIFIED` en la práctica (K2). No hay
  diagnósticos propios de pérdida o división: el punto queda indefinido y su token latente.
- Los objetivos limitados y las cónicas no circulares no se anclan en este incremento.
- La barrera por redefinición del soporte del conductor no es alcanzable (K5).

## 8. Guía focal del smoke G-B2-4

Usa una copia de `C:\Temp\InterCilConoOblique-K1-contragiro.cedg` (no el original), en GeoCeDG del
candidato.

1. **Adopción.** `X1 = Intersect(e_1, n, K)` y acepta el diálogo «Autoridad constructiva».
2. **Tangencia regular.** `U = Intersect(o_1, h_2)`. Debe mostrar una solución admisible con
   «Contacto tangente regular anclado».
3. **Materialización.** `Intersect(o_1, h_2, <token>)`, copiando el token de esa solución. El punto
   queda sobre el contacto.
4. **Arrastre.** Mueve `rbCone` y `α_cone` por todo su recorrido. El punto sigue al contacto o queda
   indefinido, nunca salta a otra raíz, y vuelve al regresar.
5. **Guardado.** Guarda, cierra y reabre dos veces. El punto conserva su definición y su posición.
6. **Deshacer.** Deshacer de la adopción devuelve el resultado heredado, sin token admisible.
7. **Pliegues.** `S = Intersect(e_1, c_1)` sigue con sus dos soluciones ancladas en pliegues.

Anota cualquier salto de raíz, pérdida tras reabrir o diálogo inesperado.
