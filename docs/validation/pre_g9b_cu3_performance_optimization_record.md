# PRE-G9B-CU3 — Registro de la optimización focal del rendimiento de `CU3-A` (T1 a T4)

| Campo | Valor |
|---|---|
| Carácter | Candidato técnico optimizado, entregado para la revisión del autor. No es una aprobación ni un cierre; `CU3` no está `PASS — AUTHOR APPROVED` |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Autorización de optimización focal del rendimiento de `CU3-A`», de 2026-10-26 (§1); [plan de optimización](../architecture/pre_g9b_cu3_performance_optimization_plan.md); [registro del smoke](pre_g9b_cu3_author_smoke_record.md) |
| Referencia congelada | producto del candidato `3c2ca7e8`, medido en `e6254926`, que solo cambia fuera de `source/` (la sonda del paso 0) |
| Candidato optimizado | `68bfd7640b12c25de0122a666d24f355488d0e22` (árbol `4a17664cf4f8dbb5f16e73088ddc44d15fb99169`), rama local `phase/pre-g9b-cu3-implementation`, no publicada |
| Evidencia | [`pre-g9b-cu3-performance-optimization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-performance-optimization-evidence.json) |
| `selfApproved` | `false` |

## 1. Autorización

Transcripción de las partes que fijan el alcance:

> DECISIÓN AUTORAL: PASO 0 Y FASE 1 (T1–T4) AUTORIZADOS CON CONDICIONES. T5–T7 NO AUTORIZADOS
> TODAVÍA.
>
> El objetivo es reducir significativamente el tiempo de refresco de `H` y `B` sin modificar la
> integridad geométrica, la identidad durable ni la semántica de los certificados.

- **Autorizado.** Paso 0; T1, reutilización de derivadas polinómicas exactas, «preferentemente
  inmutable y diferida»; T2, evaluación exacta con potencias y subexpresiones reutilizadas; T3,
  especialización exacta de las indeterminadas con cajas puntuales, «condicionada a que el paso 0
  justifique su beneficio»; T4, memoria por revisión con claves semánticamente completas. El orden
  de T1 a T3 puede ajustarse a lo medido.
- **Precisiones matemáticas.**
  > La especialización exacta conserva la función matemática, pero no presupone que dos
  > algoritmos distintos de intervalos produzcan encierros anidados.
  > Toda decisión seguirá requiriendo un encierro riguroso. Si un método optimizado no establece
  > un signo que el anterior certificaba, debe poder utilizarse el procedimiento anterior.
  > La memoria de certificados matemáticos reutilizables se distinguirá de los resultados de
  > integridad propios de cada revisión.
  > Ninguna decisión se considerará vigente sin comprobar su aplicabilidad en la revisión actual.
- **No se modifica:** la autoridad geométrica, las identidades durables, los tokens, selectores y
  gérmenes, los contratos de persistencia, los presupuestos productivos, los algoritmos heredados
  ni la política `RELAXED`. Toda memoria se invalida explícitamente ante cambios de estructura,
  identidad, selección, rama, política, revisión y constantes pertinentes; Deshacer, Rehacer,
  reapertura y copia conservan el comportamiento aprobado.
- **Medición y validación.** Comparar `3c2ca7e8` con cada etapa en la misma estación; `H` y `B`
  por separado, con mediana, percentil 95, coste de certificación, envolventes, memoria y adopción;
  demostrar la equivalencia de decisiones, estados, causas, signos, integridad y degeneraciones;
  ejecutar las pruebas adversarias de E1 a E3 y las de las memorias nuevas; clase
  `INTEGRATED_PHASE` sobre un candidato congelado.
- **No autorizado:** T5, T6, T7, `CU3-B`, la autoridad constructiva predeterminada y cualquier
  cambio de `main`.
- **Parada:** «Detenerse si una optimización exige cambiar un contrato normativo, un presupuesto,
  una identidad o una garantía geométrica.» El objetivo de menos de 100 ms es orientativo y la
  aceptación del rendimiento es del autor.

No se ha dado ninguna condición de parada.

## 2. Qué cambia

Todo el cambio productivo está en `org.geocedg.common.kernel.locus.constructive`, en el camino de
la nueva comprobación (`recheckCell`). Los intentos, las claves estructurales, los presupuestos,
los contadores de un intento, las identidades, la persistencia y la política no cambian.

| Etapa | Commit | Técnica | Cambio |
|---|---|---|---|
| 1 | `e9eb113a` | T2, potencias | `CertificatePolynomial.valueAt` calcula una vez las potencias de cada valor. La aritmética exacta da el mismo valor racional que los productos término a término |
| 2 | `ddbdb0c9` | T2, subexpresiones | margen preparado de cada salida recta–circunferencia comprobada: con las ramas formales `O` y `S` y el punto vivo `P = (x, y)`, `|O − P|² − |S − P|²` es la identidad exacta `A + x U + y W`, con `A = |O|² − |S|²`, `U = −2 (Oₓ − Sₓ)` y `W = −2 (O_y − S_y)`, construidas una sola vez |
| 2 | `ddbdb0c9` | T3 | especialización exacta, en los valores binary64 exactos de la revisión, de las indeterminadas cuya caja es un punto: en los cinco contactos, `A.x`, `A.y`, `rbCone`, `D_1.x`, `D_1.y` y `B.y`, seis de ocho; `H.x` y `H.y` son intervalos. Se sustituye una indeterminada cada vez, en orden de índice, y cada paso se guarda con su valor: un valor cambiado solo repite los pasos desde su indeterminada. El resultado es el polinomio canónico de la misma función, sea cual sea la historia |
| 2 | `ddbdb0c9` | T1 | los polinomios especializados guardan sus derivadas parciales una vez calculadas (diferidas e inmutables desde entonces) y las vuelven a registrar en los contadores en cada uso, como un cálculo nuevo |

Clases: `ConstructiveIdentityRecheckMemory` (nueva, una por certificado);
`CertificatePolynomial` (`specialize`, `memoizing`, derivadas guardadas solo en polinomios de la
memoria, potencias compartidas); `CertificateRationalFunction.specialized`;
`ConstructiveIdentityArithmetic.enclose(elemento, radicando, caja, contadores)`;
`ConstructiveIdentityCertificate.getRecheckMemory()`; `ConstructiveIdentityCertifier.recheckCell`.

**Procedimiento acelerado.** Para cada condición de la celda y cada margen,
`recheckCell` encierra primero el elemento especializado sobre la caja de la revisión, con
contadores propios. Si ese encierro certifica el signo registrado, el signo vale; si no lo
certifica, o si la memoria falla (presupuesto, excepción aritmética, denominador especializado
nulo), decide el encierro del procedimiento del candidato, que no ha cambiado. Así, todo signo que
el procedimiento anterior certificaba sigue certificado. Los dos encierros son rigurosos y no se
supone que uno contenga al otro.

## 3. Garantías

| Exigencia | Cómo se cumple |
|---|---|
| Ninguna decisión vigente sin comprobar su aplicabilidad | la memoria no guarda ninguna decisión. Cada comprobación sigue exigiendo la clave estructural igual y encierra de nuevo cada signo de la celda y cada margen sobre la caja de su propia revisión |
| Memoria matemática separada de la integridad por revisión | la memoria solo contiene objetos exactos derivados del certificado inmutable (márgenes preparados, polinomios especializados y sus derivadas). Las decisiones por revisión siguen en `ExactContactMemory` y en la resolución de los puntos estructurales, con sus huellas de revisión, sin cambios |
| Invalidación: estructura, identidad, selección, rama, política, versiones | la memoria pertenece a un certificado, y un certificado solo se comprueba si la clave `cic-structural-key/v1` es igual; la clave cubre el cono por identidad durable, la clasificación, la selección de ramas y el estado de etiquetas, las constantes y las versiones. Con otra clave no se llega a la memoria (prueba `aCertificateWhoseKeyChangedNeverReachesItsMemory`) |
| Invalidación: revisión y constantes pertinentes | el contenido especializado está indexado por los valores exactos de las indeterminadas puntuales. Cada comprobación compara esos valores con los guardados y repite los pasos que cambian. La caja de cada revisión se construye de nuevo |
| Deshacer, Rehacer, reapertura y copia | los certificados no se persisten; al reconstruir la construcción se crean certificados nuevos con memorias vacías. Las pruebas de `PHASE` de `CU3` sobre Deshacer, Rehacer, reapertura y duplicado pasan sin cambios (§6) |
| Presupuestos productivos | sin cambios. El trabajo de la memoria se cuenta con contadores propios del mismo presupuesto; el agotamiento devuelve el control al procedimiento del candidato |
| Persistencia, identidades, tokens, selectores, gérmenes, algoritmos heredados, `RELAXED` | sin cambios: ningún archivo de esas áreas se modifica |

**«Sin nuevo trabajo simbólico» (`locus-v2-constructive-authority.md` §8.2).** La especificación
dice que un arrastre sin cambio estructural «only rechecks, without new symbolic work». La
memoria no hace ninguna prueba nueva: sustituye valores exactos en los polinomios del certificado
ya probado, como ya hacía el procedimiento del candidato al evaluar el valor exacto en el centro, y
construye el margen preparado una sola vez, en lugar de cada vez. No cambia el texto normativo. Si
el autor lee §8.2 de otra forma, es una decisión suya (§8).

## 4. T4 no se ha implementado

T4 está autorizado, pero lo medido no justifica añadir otra memoria:

- en el arrastre, cada consulta se calcula **una sola vez por revisión**. La suma de los tiempos de
  las consultas instrumentadas coincide con el tiempo del arrastre;
- las decisiones exactas ya tienen una memoria por revisión (`ExactContactMemory`, con la huella del
  cono, el estado, la revisión semántica y los presupuestos): un recálculo en la misma revisión
  tarda 0,04–0,05 ms;
- una memoria por revisión de los encierros solo ahorraría los recálculos en la misma revisión, que
  el arrastre no hace.

Si el perfil del smoke real mostrara recálculos en la misma revisión, T4 podría retomarse con las
claves completas del plan.

## 5. Resultados comparativos

Misma estación, dos ejecuciones por etapa (A y B), 21 arrastres por medida, con
`run-cu3-performance-characterization.ps1` y sin cambios en el producto respecto de cada commit.
Valores en ms, mediana / percentil 95, de las ejecuciones A y B.

**Configuración del banco E3 (cuatro intersecciones públicas):**

| Etapa | Arrastre de `H` | Arrastre de `B` | Adopción |
|---|---|---|---|
| Referencia `3c2ca7e8` | 733,8 / 781,0 · 742,3 / 773,0 | 750,4 / 772,3 · 757,6 / 796,6 | 2 649 · 2 647 |
| Etapa 1 `e9eb113a` | 721,0 / 756,8 · 712,9 / 725,1 | 725,8 / 735,9 · 722,8 / 738,1 | 2 761 · 2 583 |
| Etapa 2 `ddbdb0c9` | 199,5 / 208,7 · 199,8 / 222,3 | 215,6 / 225,2 · 214,8 / 226,4 | 2 592 · 2 680 |
| Política heredada (referencia) | 67,7 · 67,6 | 50,6 · 51,1 | — |

**Configuración del smoke del autor (con los cinco puntos estructurales):**

| Etapa | Arrastre de `H` | Arrastre de `B` | Adopción |
|---|---|---|---|
| Referencia `3c2ca7e8` | 1 326,7 / 1 577,8 · 1 324,6 / 1 427,9 | 1 347,1 / 1 474,4 · 1 331,5 / 1 345,9 | 2 223 · 2 277 |
| Etapa 1 `e9eb113a` | 1 262,8 / 1 288,0 · 1 263,0 / 1 290,1 | 1 275,3 / 1 282,3 · 1 278,3 / 1 305,5 | 2 176 · 2 219 |
| Etapa 2 `ddbdb0c9` | 232,6 / 244,0 · 223,5 / 250,0 | 244,6 / 255,4 · 236,2 / 246,7 | 2 331 · 2 256 |
| Política heredada (referencia) | 43,5 · 45,3 | 43,2 · 44,2 | — |

Factor de la etapa 2 en el arrastre de `H`: unos 3,7 en E3 y unos 5,8 en la configuración del
smoke. La etapa 1 sola
mejora un 2–5 %, como anticipaba el paso 0. La adopción no cambia de forma apreciable.

**Coste de certificación por consulta durante el arrastre de `H`** (mediana de la parte exacta de
cada consulta, ejecución A; el arrastre de `B` es análogo):

| Consulta | Referencia | Etapa 2 | Envolventes (sin cambios) |
|---|---:|---:|---:|
| `o_1·h_2` | 457,7 | 4,6 | 33,0 |
| `e_1·n` | 6,9 | 1,2 | 13,7 |
| `e_1·c_1` (dos contactos) | 84,3 | 3,6 | 63,3 |
| `e_1·r` | 6,9 | 1,1 | 45,3 |
| Total | 555,8 | 10,5 | 155,3 |

**Anatomía de la comprobación** (`run-cu3-recheck-acceleration.ps1` sobre el candidato, dos
ejecuciones con los mismos recuentos). Para el certificado de cada contacto, tras dos movimientos de
`H`, dos de `B`, un cambio de `rbCone` y un movimiento de `D_1`:

| Contacto | Comprobación del candidato | Primera acelerada (memoria vacía) | Acelerada siguiente | Tras cambiar `rbCone` / `D_1` |
|---|---:|---:|---:|---:|
| `e_1·n` en 0 | 6,0 | 2,3–2,7 | 0,8–0,9 | 1,6 / 1,6–2,0 |
| `e_1·c_1` en 0 | 35,4 | 10,7–11,0 | 1,5–1,6 | 2,5 / 6,6–7,1 |
| `o_1·h_2` en 0 | 445 | 320–333 | 4,2–4,8 | 12,8–13,0 / 12,4–12,5 |
| `e_1·c_1` en π | 35,7 | 10,7–11,0 | 1,5–1,6 | 2,5 / 7,0 |
| `e_1·r` en π | 5,8 | 2,1–2,3 | 0,7–0,8 | 1,5 / 1,5 |

En las 30 revisiones medidas, la memoria certifica por sí misma **todos** los signos de la celda
(26, 19, 33, 19 y 26 condiciones) y todos los márgenes (1, 0, 2, 0 y 1). Ninguno pasa al
procedimiento del candidato, que certifica los mismos. Los movimientos de `H` y de `B` no añaden
ninguna sustitución: el recuento se mantiene en 43, 32, 131, 32 y 43. Sí las añaden el cambio de
`rbCone` y el movimiento de `D_1`, con un coste de 1,5 a 13 ms en la comprobación siguiente.

## 6. Equivalencia y pruebas

**Decisiones en los arrastres.** Los recuentos deterministas de la sonda de rendimiento son
iguales en las tres etapas para `H` y para `B`: cajas encerradas (504/506, 283, 1 760/2 173 y
932/908), consultas y contactos exactos (1, 1, 2 y 1) y residuos normalizados (0).

**Prueba nueva** `ConstructiveIdentityRecheckMemoryTest` (8 pruebas, en la selección
`pre-g9b-cu3.shared`):

- `theAcceleratedRecheckDecidesAsTheCandidateInEveryRevision`: los cinco contactos en los dos
  alcances, en todas las revisiones del ciclo de vida de `ConstructiveIdentityContactTest`, sin y con
  la memoria, con la misma decisión. Las revisiones son `rbCone` = 3, 4, 6 y 7,5; `α` = 35° y 60°;
  `D_1` ×0,5, ×1,3 y ×3; restauración; `H` −20, −0,5, +0,5, +20 y +40 (otra celda); y `B` −3, −0,5,
  +0,5 y +3. Hay comprobaciones que se mantienen y otras que fallan;
- `theMemoryCertifiesEverySignOfTheCellAtTheBaseRevision`;
- `theSpecializationIsExactAndCanonicalWhateverTheHistory`;
- `aChangedPointValueRepeatsOnlyTheStepsFromItsIndeterminate`;
- `thePreparedMarginIsTheMarginOfTheCandidate`: la diferencia es el elemento cero del cuerpo;
- `aKeptDerivativeEnclosesAndCountsAsAFreshOne`: mismos encierros y mismos contadores;
- `aMemoryThatFailsLeavesEveryDecisionToTheCandidate`;
- `aCertificateWhoseKeyChangedNeverReachesItsMemory`.

**Pruebas de E1 a E3.** Antes de congelar el candidato pasaron 462 pruebas de los paquetes
`constructive` e `intersection`, sin fallos. Entre ellas están las adversarias de E1 a E3: celdas,
signos, órdenes, `D_1` ×3, `H` +40, `α` = 35°, inyecciones de presupuesto, un objetivo a un ulp,
transición de selección, Deshacer, Rehacer, reapertura y duplicado. El resultado canónico es la
campaña de §7.

**Inventario.** La selección `pre-g9b-cu3.shared` incluye la clase nueva. El productor y el
actualizador oficiales refrescaron `discovery.shared` (6 542), `discovery.desktop` (2 271, solo la
evidencia), `pre-g9b-cu3.shared` (202, ejecutada) y `final.shared` (7 459, ejecutada). Con ellos
se actualizaron el pin `junit_inventory` del registro y el recuento de `CU3` en
`verification-final-coverage.Tests.ps1` (`68bfd764`).

## 7. Verificación `INTEGRATED_PHASE` del candidato `68bfd764`

Comandos, desde la raíz del repositorio y con PowerShell 7.6.9:

```powershell
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu3-opt-static-68bfd764
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu3-opt-infra-unit-68bfd764
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU3 -LogDirectory artifacts\agent\pre-g9b-cu3-opt-phase-pre-g9b-cu3-68bfd764
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu3-opt-phase-pre-g9b-cu2-68bfd764
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu3-opt-phase-pre-g9b-cu1-68bfd764
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu3-opt-phase-pre-g9b-cu0-68bfd764
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu3-opt-integration-68bfd764
```

Cada resultado se reautenticó con `Read-VerificationJson` y `Get-VerificationResultHash`. En todos,
el hash coincide, el diario está `COMPLETED` y el candidato es exactamente
`68bfd7640b12c25de0122a666d24f355488d0e22`, árbol `4a17664cf4f8dbb5f16e73088ddc44d15fb99169`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---|---|
| `STATIC` | `verification-dfdcc76ccf864540b3323c54a3a16e0d` | `7fcf533bdfea6065d50970234bd6952e0478739faf4c0a1593e9c95584c11aa0` | `25dfacc8` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo los diagnósticos permanentes |
| `INFRA_UNIT` | `verification-022dd871283448be8acdb6b8057aecf4` | `60c0119abdd04f7c3c2d7dacd2f5bb24216f11b52c0095a8d58f3c2aff5d936b` | `830887b1` | 2,2 min | `REJECTED_VERIFICATION_CORE / COMPLETE`, 20/22: `infra.registry` e `infra.report` violadas por el entorno (véase abajo) |
| `PHASE -Phase PRE-G9B-CU3` | `verification-ebdff6c9c48b425b80b8aaf1df972a21` | `6f0777f11a4d0d7b3efa02778b78c8eff37f09d927b80846db960372cbad51d2` | `1d298a3a` | 6,0 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu3.shared`: 22 clases y 202 pruebas; `pre-g9b-cu3.desktop`: 5 clases y 14 pruebas; 0 fallos y 0 omitidas |
| `PHASE -Phase PRE-G9B-CU2` | `verification-6d61a1e724ba4e99bba3a2ade1f579f6` | `d4e9311eea021995a2410b8e1501d429d943d7a963d07ca36064263dd6d7d4be` | `b9dc6d9e` | 9,4 min | `ACCEPTED / COMPLETE`, 4/4; 242 y 72 pruebas, 0 fallos |
| `PHASE -Phase PRE-G9B-CU1` | `verification-23fab295b97a4ca1a770d6039b422381` | `38aa646e3c268114ca9bc741f51c1e7071d9db9cac8140c8b417e5487e0faafa` | `b0e4f130` | 5,3 min | `ACCEPTED / COMPLETE`, 4/4; 57 y 20 pruebas, 0 fallos |
| `PHASE -Phase PRE-G9B-CU0` | `verification-5f6a29f0129e4513b8c8bddcb600c1e6` | `7ddcdcdc8534c85f7629004b766accd872c6025364d8adbfed3b56b6af17f50f` | `43e3e97e` | 2,5 min | `ACCEPTED / COMPLETE`, 2/2; 21 pruebas, 0 fallos |
| `INTEGRATION` | `verification-c1108292ce724ec183c3cd5837e3038a` | `0267ca4102950cf07fdb2cdfd33ed9f5796cc2a0c4f8d2999e30d3c6e0d83aea` | `4502c4bd` | 56 min | `ACCEPTED / COMPLETE`, 19/19 |

Los planes de `PHASE` e `INTEGRATION` son los mismos que en la verificación de `3c2ca7e8`.

**`INFRA_UNIT` rechazado por el entorno, no por el candidato.** Las suites
`verification-registry.Tests.ps1` y `verification-report.Tests.ps1` exigen que un rechazo de
esquema de `Test-Json` conserve el `FullyQualifiedErrorId`
`InvalidJsonAgainstSchemaDetailed,Microsoft.PowerShell.Commands.TestJsonCommand` («Schema
rejection did not retain the nominal Test-Json error identity»). Hechos:

- la observación de entorno de los resultados registra PowerShell 7.6.6 en la `INFRA_UNIT`
  aceptada de `3c2ca7e8` (`verification-aca78b2377c34d0eb126e1b2588d669c`, 2026-10-25) y 7.6.9 en
  esta. PowerShell se actualizó entre las dos ejecuciones;
- las dos suites fallan igual, con PowerShell 7.6.9, sobre un `git archive` de `3c2ca7e8`;
- el candidato solo cambia en `tools/agent` el recuento de `CU3` de
  `verification-final-coverage.Tests.ps1`, cuya comprobación (`infra.final-coverage`) se cumple.

Se cumplen las demás 20 comprobaciones de `INFRA_UNIT`. La clase `INTEGRATED_PHASE` no queda
completa mientras `INFRA_UNIT` no se acepte. Hay dos salidas, ambas del autor:

- volver a PowerShell 7.6.6 en la estación y repetir `INFRA_UNIT` sobre el mismo candidato;
- autorizar un mantenimiento aparte del verificador para la nueva identidad del error.

No se ha tocado la infraestructura de verificación ni el entorno.

## 8. Limitaciones y decisiones para el autor

- **Objetivo orientativo de 100 ms no alcanzado.** Los arrastres quedan en unos 200–215 ms (E3) y
  223–245 ms (smoke). Lo que queda son sobre todo los encierros de las consultas (unos 155 ms),
  territorio de T6 y T7, y unos 40–60 ms de trabajo heredado.
- **Coste de memoria.** En la sonda, el heap retenido tras la recolección sube de 34,3 a 40,0 MB
  (+5,7 MB) en el documento E3 y unos +11,4 MB en el del smoke, con los cinco puntos
  estructurales. La memoria de `o_1·h_2` guarda 215 polinomios con 21 037 términos y unos 1,45
  Mbit de coeficientes. La mayor parte son las derivadas guardadas y los pasos intermedios; se
  podría reducir si el autor lo pide, a costa de tiempo.
- **Primera comprobación.** La primera comprobación de un certificado nuevo prepara su memoria:
  unos 320 ms para `o_1·h_2`, una vez por certificado (tras la adopción, una reapertura o un
  intento nuevo).
- **Decisiones posibles distintas.** En principio, el encierro especializado puede certificar un
  signo que el del candidato no certifica, porque los dos algoritmos no producen encierros
  anidados. En ese caso la comprobación acelerada mantendría un certificado que el candidato
  sustituiría por un intento nuevo. Las dos decisiones serían rigurosas. No ha ocurrido en ninguna
  revisión medida ni probada.
- **Medida.** Una sola estación y el arnés de pruebas; el smoke interactivo real es del autor.
- **Verificación incompleta.** `INFRA_UNIT` está rechazado por la actualización de PowerShell
  (§7); el resto de la clase está aceptado.
- **Decisiones del autor:**
  - la salida para `INFRA_UNIT` (§7);
  - aceptación o no del candidato y de su rendimiento;
  - si autoriza T5 y T6, y si T7 necesita investigación;
  - el coste de memoria;
  - la lectura de §8.2;
  - T4, autorizado pero sin usar.

## 9. Estado

- Commits locales sobre `3853f323`: `e6254926` (sonda del paso 0), `e9eb113a`, `ddbdb0c9`,
  `70b3ada8`, `68bfd764` (candidato) y el de este registro. Nada publicado; `main`, las etiquetas y `geocedg_book` no cambian.
- `CU3-B`, T5, T6, T7 y la autoridad constructiva predeterminada siguen sin autorizar.
- `CU3` no está `PASS — AUTHOR APPROVED`; el cierre de `CU3-A` sigue pendiente de decisión autoral.
