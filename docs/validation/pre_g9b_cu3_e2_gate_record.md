# PRE-G9B-CU3 — Registro de las puertas del incremento E2

| Campo | Valor |
|---|---|
| Carácter | Registro técnico de las puertas del incremento E2 bajo la autoridad constructiva: G-AUT-2, G-AUT-9, G-AUT-10 y la parte de G-AUT-8 que mide el presupuesto del certificado de identidad constructiva. Reúne la evidencia de cada puerta y de cada fila de la matriz y lo que pasa a E3 y E4. Es evidencia técnica: no aprueba nada, no sustituye la decisión autoral y no declara `CU3` `PASS — AUTHOR APPROVED` |
| Autoridad | [registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md) §5 a §10 y §16; [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §5.5 a §5.9 y §6.2 a §6.4; [diseño de E2](../architecture/pre_g9b_cu3_e2_implementation_design.md); [matriz](pre_g9b_cu3_validation_matrix.md) §6; [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) 1.0, §3.2, §8 y §10 |
| Rama | `phase/pre-g9b-cu3-implementation`, local, sin publicar |
| Base | E1 en `50ac91c29fb2835874d10c7e7afe56c3c349f79f`, con sus puertas registradas en `73c20c5f85a0bceb4f2f5d1651a373688e5b57f8` ([registro de E1](pre_g9b_cu3_e1_gate_record.md)) |
| Commit de las puertas | `91222e312fed9756225407be6f0c72a1eafeac6e` (árbol `5289878073d33f28d69bee92d08de9a96bbb54bf`) |
| Evidencia de coste | [`pre-g9b-cu3-e2-cost-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e2-cost-evidence.json), SHA-256 `b86b0ce23740f68013cc80ca19a8732e4b02c38deb34c0c900d8c4070efde317` (ejecución A) |
| Resultado técnico | Las puertas G-AUT-2, G-AUT-9 y G-AUT-10 y la parte de G-AUT-8 que mide el presupuesto del certificado se cumplen con la evidencia de §3 a §8. Las selecciones `PHASE` de `CU0`, `CU1` y `CU2` y la ejecución `INTEGRATION` se aceptan sobre el commit de las puertas. `INTEGRATION` cubre también E1, cuya ejecución se había rechazado por los pins sin renovar del inventario (registro de E1 §10.8). No es aprobación autoral |
| Estado | E2 es un **candidato**: `P ∈ L` es `STRICT` por transferencia y `P ∈ r` es `STRICT` por el certificado de identidad constructiva en los cinco contactos, dentro de su celda, con su orden de contacto. `CU3` sigue incompleto hasta E3 y E4 |
| `selfApproved` | `false` |

## 1. Alcance y método

E2 lleva al producto el certificado de identidad constructiva (`constructive-identity-certificate/v1`,
especificación §8) y con él hace `STRICT` la segunda incidencia `P ∈ r` donde la evaluación exacta
de E1 solo la decidía para el sustituto de la rotación (diseño de E2 §1). El certificado es un
sistema numérico más del evaluador del cono de E1, de modo que decide los mismos predicados con la
misma selección heredada. Sus límites semánticos, el presupuesto y los contadores, están en el
núcleo compartido; el banco de coste está en `tools/benchmark/pre-g9b-cu/`, fuera del producto.

Cada puerta se evalúa con su criterio y sus pruebas adversarias (propuesta §6.3). Cada fila de la
matriz tiene su prueba, citada por su identificador en el código de la prueba (§4).

## 2. Commits de E2

| Commit | Contenido |
|---|---|
| `86be18d11` | certificado de identidad constructiva: álgebra exacta, cuerpo con una raíz, encierros, presupuesto y contadores; certificador, caja, celda, clave, recomprobación y alcances; orden de contacto; integración en `Intersect(L, r, D)` con el pliegue de `CU2`; caso especial de un punto ya incidente; pruebas de E2 y pruebas de E1 que cambian de expectativa |
| `b79558c1f` | banco de coste del certificado (G-AUT-8) |
| `2b6a17091` | inventario JUnit de E2 con las selecciones `final.*` refrescadas con evidencia ejecutada (la causa del rechazo de `INTEGRATION` en E1, registro de E1 §10.8) y su pin en el registro de verificación |
| `91222e312` | citas de V-M3-9 y V-CFG-5 en las pruebas de los cinco contactos; ninguna aserción cambia |

## 3. Puertas de E2

| Puerta | Criterio (propuesta §6.3) | Evidencia | Resultado |
|---|---|---|---|
| G-AUT-2, contactos | los cinco contactos `STRICT` en su celda, con orden de contacto | `ConstructiveIdentityContactTest`: los cinco contactos de la réplica son `IDENTITY_CERTIFIED` en los alcances automático y de frontera, con `H` cortado, el orden de contacto y el signo de `h″` del estudio; controles C1 a C5 y objetivo desplazado un ulp; V-M3-5, V-M3-6 y V-CFG-6 leídas con AUT-7. `ConstructiveIdentityIntegrationTest`: en `Intersect(L, r, D)` los cinco son `STRICT` por el certificado, con el pliegue de `CU2` transferido por estructura en los estacionarios. `PreG9BCu3StructuralPointAuthorModelTest`: lo mismo en el modelo del autor; filas V-AUT-CIC-1 y V-AUT-CIC-2 | superada |
| G-AUT-9, corrección del CIC | teoremas sintéticos demostrados; conjeturas falsas no demostradas; dependencia de la rama; correspondencia de ramas con el algoritmo vivo; un corte nunca inventa | `ConstructiveIdentityCertifierTest`: S1 a S4, la rama de S2, el caso especial heredado de un punto ya incidente reproducido y contrastado, el corte que olvida, la pertenencia a la parte limitada (el defecto del prototipo), resultados tipados, una caja sin encierro riguroso que no decide nada, la inversión inyectada de la rama viva y ramas que no se distinguen; `ConstructiveIdentityAlgebraTest` (cuerpo y presupuesto); filas V-AUT-CIC-3 a V-AUT-CIC-6 | superada |
| G-AUT-10, dinámica e identidad | ninguna prueba sobrevive a un cambio de su clave estructural, de constante, de selección o de celda; prueba nueva en la celda nueva; la clave usa identidad durable y sobrevive a un renombrado | `ConstructiveIdentityContactTest`: las posiciones del estudio en la réplica, movimientos de `B` y `H`, `H` +40, `D_1` ×3 y una transición real del estado de etiquetas; `ConstructiveIdentityIntegrationTest`: renombrado, redefinición, eliminación y nueva creación, Deshacer, Rehacer, reapertura, duplicado, arrastre y determinismo; filas V-AUT-ID-1 a V-AUT-ID-7 y V-AUT-DYN-1 a V-AUT-DYN-4 | superada |
| G-AUT-8, coste del certificado | presupuesto versionado y contadores deterministas | banco de coste (§6) y `ConstructiveIdentityAlgebraTest`: monomios de 16 indeterminadas, exponente fuera de rango, cada límite inyectado | superada; los techos provisionales bastan con margen (§6) |

## 4. Filas de la matriz

Todas las filas que la matriz asigna a E2 están citadas por su identificador en una prueba de E2:

| Filas | Prueba |
|---|---|
| V-AUT-CIC-1, V-AUT-CIC-2 | `ConstructiveIdentityContactTest`, `ConstructiveIdentityIntegrationTest`, `StructuralPointTest` y `PreG9BCu3StructuralPointAuthorModelTest` |
| V-AUT-CIC-3 a V-AUT-CIC-6 | `ConstructiveIdentityCertifierTest` y `ConstructiveIdentityAlgebraTest` |
| V-AUT-ID-1 a V-AUT-ID-7 | `ConstructiveIdentityIntegrationTest` y, V-AUT-ID-3 sobre construcciones sintéticas (S4), `ConstructiveIdentityCertifierTest` |
| V-AUT-DYN-1 a V-AUT-DYN-4 | `ConstructiveIdentityContactTest`, `ConstructiveIdentityIntegrationTest` y `StructuralPointMatrixTest` |
| V-AUT-RLX-2 (parte de E2) | `ConstructiveIdentityAlgebraTest` (la cúbica de los nueve puntos no es el elemento nulo) y `ConstructiveIdentityIntegrationTest` (la evidencia relajada de una igualdad accidental de las entradas libres se retira) |
| V-AUT-TRF-2 (modelo) | `Cu2CertificateTransferTest` y `PreG9BCu3StructuralPointAuthorModelTest`: los pliegues de `e_1` en `K` y en `J` se transfieren por estructura con la premisa de `k` certificada para todo valor del corte; una premisa que no es identidad del corte da `STRUCTURAL_PREMISE_NOT_STRICT` |
| V-M3-5, V-M3-6, V-CFG-6 (con AUT-7) | `ConstructiveIdentityContactTest` |
| V-M3-9, V-CFG-5 (con AUT-7) | `ConstructiveIdentityContactTest`, `ConstructiveIdentityIntegrationTest` y `PreG9BCu3StructuralPointAuthorModelTest`: cada contacto se decide contra su objetivo constructivo |

## 5. Las dos incidencias en los cinco contactos

| Contacto | `P ∈ L` | `P ∈ r` | Orden de contacto | Signo de `h″` (estudio §1.7) | Pliegue de `CU2` |
|---|---|---|---|---|---|
| `Intersect(e_1, n, K)` | `STRICT` por margen | `STRICT` por el certificado | estacionario | negativo (`−28,525`) | transferido por estructura en `0` |
| `Intersect(e_1, c_1, K)` | `STRICT` | `STRICT` | estacionario | negativo (`−24,672`) | transferido en `0` |
| `Intersect(o_1, h_2, K)` | `STRICT` | `STRICT` | tangente | positivo (`+2,569`) | — (punto regular) |
| `Intersect(e_1, c_1, J)` | `STRICT` | `STRICT` | estacionario | negativo (`−7,992`) | transferido en `π` |
| `Intersect(e_1, r, J)` | `STRICT` | `STRICT` | estacionario | positivo (`+16,236`) | transferido en `π` |

Lo mismo vale en la réplica y en el modelo del autor. En el alcance automático, la prueba usa ocho
indeterminadas (`A.x`, `A.y`, `B.y`, `rbCone`, `H.x`, `H.y`, `D_1.x` y `D_1.y`) y corta `H`, cuyo
camino lee la rotación `C′`; vale en su celda para todo valor de ellas. En el alcance de frontera,
con `A`, `D_1` y `rbCone` como constantes, usa tres. En los dos, `h′` es el elemento nulo.

**Posiciones dinámicas** (G-AUT-10, `ConstructiveIdentityContactTest`). En las posiciones del
estudio (`rbCone` 3, 4, 6 y 7,5; `α` 35° y 60°; `D_1` ×0,5 y ×1,3), en los movimientos de `B`
(±3) y de `H` (±20) y tras restaurar, el certificado del alcance automático se recomprueba en su
celda sin trabajo simbólico, y los cinco contactos siguen `STRICT`. En el alcance de frontera, una
constante que cambia (`rbCone` o `D_1`) cambia la clave y exige una prueba nueva; un movimiento de
`α`, de `B` o de `H` se recomprueba. Con `D_1` ×3 solo se recomprueban los dos `e_1·c_1`; las
pruebas nuevas dan `e_1·n` y `e_1·r` `CONTRADICTED` (fuera de la parte limitada), `o_1·h_2`
`INVALID` (`M_1` no existe, así que `h_2` no está definido) y los dos `e_1·c_1`
`IDENTITY_CERTIFIED`. Con `H` +40, en otra celda, solo se recomprueban los dos contactos en `J`;
las pruebas nuevas dan `e_1·n` `CONTRADICTED`, `o_1·h_2` `INVALID` y los otros tres
`IDENTITY_CERTIFIED`. Un objetivo desplazado un ulp da `NOT_AN_IDENTITY`.

## 6. Coste y presupuesto del certificado (G-AUT-8)

Banco `tools/benchmark/pre-g9b-cu/run-cu3-e2-cost.ps1` sobre `91222e31`, dos ejecuciones (A y B)
con el árbol limpio antes y después y cinco repeticiones medidas de cada intento. La evidencia
versionada es la ejecución A
([`pre-g9b-cu3-e2-cost-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e2-cost-evidence.json)).

| Contacto | Alcance | Productos de términos | Indeterminadas | Términos | Bits | Mediana (ms) |
|---|---|---|---|---|---|---|
| `e_1·n` en `K` | automático | 279 900 | 8 | 5 915 | 104 | 157 |
| | frontera | 6 501 | 3 | 98 | 698 | 21 |
| `e_1·c_1` en `K` | automático | 311 243 | 8 | 5 915 | 104 | 188 |
| | frontera | 8 387 | 3 | 98 | 698 | 25 |
| `o_1·h_2` en `K` | automático | 1 571 831 | 8 | 21 685 | 114 | 883 |
| | frontera | 11 467 | 3 | 98 | 698 | 34 |
| `e_1·c_1` en `J` | automático | 311 243 | 8 | 5 915 | 105 | 186 |
| | frontera | 8 387 | 3 | 98 | 700 | 25 |
| `e_1·r` en `J` | automático | 279 900 | 8 | 5 915 | 105 | 130 |
| | frontera | 6 501 | 3 | 98 | 700 | 19 |

- **Margen.** El máximo observado es de 1 571 831 productos (9,8 % del techo de 16 000 000),
  21 685 términos (10,8 % de 200 000) y 700 bits (17,1 % de 4 096). Las 16 indeterminadas se
  alcanzan solo en el caso sintético de una segunda raíz independiente, que es `OUT_OF_CLASS`
  por su segunda raíz cuadrada, no por el presupuesto. Los casos sintéticos S1 a S3 usan como
  mucho 2 370 productos.
- **Inyecciones** sobre `e_1·n` en `K`: 1 000 productos, 4 indeterminadas, 50 términos o 60
  bits dan `NOT_ESTABLISHED` `BUDGET_EXHAUSTED` en los cuatro casos, sin aproximación.
- **Arrastres.** Mover `E_1` deja la misma resolución; mover `H` recomprueba el mismo
  certificado, y la integridad sigue `STRICT`.
- **Determinismo.** Las cinco repeticiones de cada intento coinciden. Entre las ejecuciones A y B
  coinciden el estado, la causa, los cortes, las indeterminadas, la celda, el orden de contacto,
  los signos y los contadores. Difieren los tiempos y la clave estructural. La clave liga las
  identidades durables, y el modelo no persiste las de `K`, `J` y `r`: cada ejecución se las
  asigna al publicar los puntos que crea, con la fuente de tokens opaca de la sesión.
- **Conclusión.** Los techos provisionales de `constructive-identity-certificate-budget/v1`
  bastan con margen para los cinco contactos y los casos sintéticos. No se propone ningún cambio.
  Su aceptación productiva la decide el autor (registro de aprobación §16).

## 7. Compatibilidad y persistencia

- E2 no cambia ningún formato. El certificado es evidencia de revisión: el documento no guarda
  ningún certificado, clave ni signo, y la versión del certificador solo aparece en el registro de
  política del documento (V-AUT-ID-6).
- Ninguna identidad, token ni selector cambia: la integridad no participa en la identidad
  (`ConstructiveIdentityIntegrationTest`, el modelo del autor).
- Los documentos heredados no cambian: el certificado solo actúa bajo
  `constructive-authority/v1`, después de una adopción explícita.
- El caso especial de un punto ya incidente lo reconstruyen ahora la tabla de clase y el evaluador
  (diseño de E2 §2b). Antes fallaba cerrado con `SELECTION_NOT_RECONSTRUCTED`. `CU2` no lo captura,
  así que ninguna transferencia lo consume, y bajo la política heredada nada cambia.
- Al crear un punto estructural, la evidencia se calcula otra vez tras publicar las identidades de
  sus entradas (diseño de E2 §7), y el resumen de estado incluye las identidades.

## 8. Regresión

Todas las ejecuciones se hicieron sobre `91222e312fed9756225407be6f0c72a1eafeac6e`, con el espacio
de trabajo limpio antes y después, en este orden y sin cambios del árbol entre ellas
(`e2-final-gate.ps1` del directorio temporal de la sesión). Los resultados de `verify.ps1` se
re-autentican recalculando su `result_hash`.

| Ejecución | Orden | Resultado |
|---|---|---|
| banco A y B | `run-cu3-e2-cost.ps1 -OutputDirectory artifacts\benchmarks\pre-g9b-cu\cu3-e2-cost-91222e31-run{A,B}` | código 0 las dos; campos deterministas iguales (§6) |
| `PRE-G9B-CU0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu3-e2-final-phase-pre-g9b-cu0-91222e31` | `verification-c4b5f2f42c754c1fb52b43ab915f8932`, resultado `4231bd53c4cf2580b18b75018f9e8765e0ef521d49e7fb6c89169982b70da280`, `ACCEPTED / COMPLETE` 2/2, re-autenticado |
| `PRE-G9B-CU1` | ídem, `-Phase PRE-G9B-CU1`, `…-pre-g9b-cu1-91222e31` | `verification-54a0ce4985ee45bb81dcc8e30e6b9d94`, resultado `3f64df78773bc57bc17a18057b0ed7110992c3b136e8a95724be21c3c21fbfb1`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| `PRE-G9B-CU2` | ídem, `-Phase PRE-G9B-CU2`, `…-pre-g9b-cu2-91222e31` | `verification-124b09b476e3474fb91807e5ecc00789`, resultado `e96da045acb9c07747f23c3233c7991984ee8b2f7f8e9484762fd2834aaae81c`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| suites compartidas | `:shared:common-jre:test --rerun` con `G8B*`, `G8C1*`, `G8C2*` y `G9U0R4*` de `org.geocedg.common.locus`, y los paquetes `kernel.locus.intersection`, `kernel.locus.constructive` y `kernel.spatial` | 608 pruebas en 51 clases, correctas |
| suites de escritorio | `:desktop:desktop:test --rerun` con `PreG9BCu3*`, `G9U0R4*`, `G9U0LocalizationHelpTest` y `G9U1AlgebraSubmissionTest` | 25 pruebas en 7 clases, correctas |
| `INTEGRATION` | `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu3-e2-integration-91222e31` | `verification-320e6312a35f46b7a74df1b00d02ba91`, resultado `a442c3df8b1c22870773587af21e92f162a65e62698ccc2d4adb1ebc7cd101e5`, `ACCEPTED / COMPLETE`: 19 comprobaciones de aceptación satisfechas, ninguna violada ni no fiable; diagnósticos permanentes: `diagnostic.governance` `FINDING` y `diagnostic.historical-consistency` `UNAVAILABLE`, ninguno de aceptación; re-autenticado |

Con el inventario de `2b6a1709`, las selecciones ejecutadas `final.shared` y `final.desktop`
coinciden con sus pins, que era la causa del rechazo de E1.

## 9. Limitaciones y cuestiones abiertas

1. **Presupuesto del certificado.** Los techos provisionales bastan con margen para los cinco
   contactos y los casos sintéticos (§6). No se propone ningún cambio. Su aceptación productiva la
   decide el autor.
2. **Intervalos y caso especial.** La evaluación por intervalos no decide el cero exacto del caso
   especial de un punto ya incidente. Por eso no da encierro de esas salidas, y una posición o un
   corte que dependan de ellas fallan cerrados, como en E1.
3. **Un corte olvida relaciones.** Una premisa que vale idealmente pero pasa por un corte no se
   demuestra; por ejemplo, la rotación por cero de `Cu2CertificateTransferTest`. Es la limitación
   de S3 de la propuesta §5.5 y falla cerrada.
4. **Orden de contacto en la interfaz.** El inspector muestra la evidencia del certificado
   (`STRICT CONSTRUCTIVE_IDENTITY_CERTIFICATE`), pero no el orden de contacto ni el signo de `h″`.
   Quedan en la resolución y en el banco.
5. **Coste de la primera prueba.** Crear un contacto calcula la prueba dos veces (§7). La más cara,
   `o_1·h_2`, tarda alrededor de un segundo; las recomprobaciones de un arrastre no hacen trabajo
   simbólico.

## 10. Pendiente para E3 y E4 y pruebas manuales

- **E3.** Certificado de paridad por simetría exacta en la regla de conflicto (forma «una raíz
  doble en un punto exacto», con el certificado de E2 y el pliegue de `CU2`), reclasificación de la
  costura con la inversión de `pins.B08_B09`, y `CU3-B`. Filas V-B-2, V-B-NOISE-1, V-CONF-5,
  V-B-7 a V-B-10 y V-B-12 (V-B-11 quedó en E1 sin la decisión exacta: un cuarto de arco está
  fuera de `constructive-class/v1`).
- **E4.** Selecciones `pre-g9b-cu3.*`, inventario `final.*`, verificación `INTEGRATED_PHASE`,
  registro de cierre, nota de migración de D-CU3-12 e informe final.
- **Pruebas manuales del autor en el escritorio.**
  - Los cinco contactos del modelo después de la adopción. En el inspector, `P ∈ r` debe aparecer
    como `STRICT CONSTRUCTIVE_IDENTITY_CERTIFICATE`.
  - Un arrastre de `H` y de `B`: los contactos siguen siendo `STRICT` sin pausa perceptible.
  - `D_1` ×3: las salidas certificadas de `n` y de `r`.
  - Deshacer, Rehacer y reapertura.
