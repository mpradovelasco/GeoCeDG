# PRE-G9B-CU3 — Informe del candidato técnico congelado

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato técnico de `CU3` bajo la autoridad constructiva aprobada: incrementos E0 a E4, puertas G-AUT, G-A, G-B y G-final, evidencia, las dos incidencias de los cinco contactos, compatibilidad y persistencia, degeneraciones y límites, coste y presupuestos, y pruebas manuales pendientes |
| Estado | `CU3 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW` con `CU3-A` completo y `CU3-B` pendiente; `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; ningún cierre autoral se ejecuta; `CU4`–`CU10` y `G9B` no autorizados; la autoridad constructiva no es la predeterminada |
| Autorizaciones | «PRE-G9B-CU3 — Aprobación autoral de la autoridad constructiva e implementación por incrementos» (2026-10-25; [registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md) §1 a §15) y «PRE-G9B-CU3 — Decisión autoral sobre presupuestos de cálculo y continuación de E1» (2026-10-25; el mismo registro, §16) |
| Base autorizada | `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, árbol `9e4984db1dec327e47afeec2c1072f78c6e9cc49` (conciliación final publicada de la propuesta normativa) |
| Rama | `phase/pre-g9b-cu3-implementation`, local, sin publicar; `main`, etiquetas, releases, empaquetado y `geocedg_book` sin tocar |
| Clase de verificación | `INTEGRATED_PHASE` (D-CU3-14): `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU3`, `PHASE -Phase PRE-G9B-CU2`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` sobre el candidato congelado |
| Identidad del candidato y resultados | [registro de verificación](pre_g9b_cu3_verification_record.md), añadido por un commit documental descendiente (un commit no puede citar su propia identidad) |

## 1. Base y entrada

| Comprobación | Resultado |
|---|---|
| Base | `3cd0f710` con el árbol `9e4984db`; la rama de implementación detenida en `1a0903aa` avanzó a ella por avance rápido (D-AUT-10), conservando la evidencia de la puerta G-M3 |
| Puerta de entrada | `PHASE` de `CU0`, `CU1` y `CU2` `ACCEPTED / COMPLETE` sobre la base, re-autenticadas antes de E0 |
| Espacio de trabajo | limpio al empezar cada incremento y antes de cada ejecución de verificación |

## 2. Incrementos y puertas

| Incremento | Commits | Contenido | Puertas | Resultado |
|---|---|---|---|---|
| E0 | `1f81666e`, `acae78a9` | contratos aprobados incorporados antes del código (ADR 0040, especificación `locus-v2-constructive-authority.md` 1.0, enmiendas 1.4, registro de aprobación, prompt canónico), decisión de presupuestos y diseño de E1 | G-AUT-0 | superada (decisión autoral) |
| E1 | `f09cd51c` … `50ac91c2`; registro `73c20c5f` | integridad de las relaciones; política de documento y adopción explícita; tabla de clase, cono y evaluación exacta M3 con presupuesto; transferencia de certificados de `CU2`; `SUBSTITUTE_GENERIC` v1; `Point(L, D)` e `Intersect(L, r, D)`; piezas I-1 a I-3 e I-6 de `CU3-A`; oferta de adopción en el escritorio | G-AUT-1, G-AUT-3 a G-AUT-8, G-A (E1) | superadas ([registro de E1](pre_g9b_cu3_e1_gate_record.md)); E1 fue candidato parcial con `P ∈ r` `RELAXED` |
| E2 | `86be18d1`, `b79558c1`, `2b6a1709`, `91222e31`; registro `5a1ac888` | certificado de identidad constructiva v1 en el núcleo, con presupuesto y contadores; `P ∈ r` `STRICT` en los cinco contactos con su orden de contacto y el pliegue de `CU2` | G-AUT-2, G-AUT-9, G-AUT-10, G-AUT-8 (certificado) | superadas; `PHASE` de `CU0`, `CU1` y `CU2` e `INTEGRATION` aceptadas sobre `91222e31` ([registro de E2](pre_g9b_cu3_e2_gate_record.md)) |
| E3 | `06f2bb40` | regla de conflicto en `Σ_C` con la forma «una raíz doble en un punto exacto» y el certificado de paridad por simetría exacta; reclasificación de la costura; inversión de `pins.B08_B09`; banco de coste de las decisiones exactas | G-A; G-B | G-A superada con la verificación del candidato; G-B no alcanzada: `CU3-B` pendiente ([registro de E3](pre_g9b_cu3_e3_gate_record.md)) |
| E4 | este candidato y su registro de verificación | registro de la fase `PRE-G9B-CU3` con dos selecciones, inventario JUnit refrescado con evidencia ejecutada, fijaciones de infraestructura, este informe, su espejo y documentos vivos | G-final | `INTEGRATED_PHASE` sobre el candidato ([registro de verificación](pre_g9b_cu3_verification_record.md)) |

El registro de cierre de `CU3` no forma parte de este candidato: transcribe una decisión autoral
y solo se redacta después de ella.

## 3. Evidencia

**Pruebas.** Todas las filas de la matriz asignadas a `CU3-A` y a la ampliación V-AUT tienen su
prueba, citada por su identificador en el código: el registro de E1 (§3 y §4), el de E2 (§4) y el de
E3 (§3) dan la correspondencia fila a fila. Las pruebas de `CU3` forman dos selecciones nuevas del
inventario:

| Selección | Contenido | Identidades |
|---|---|---|
| `pre-g9b-cu3.shared` | las 18 clases del paquete `kernel.locus.constructive` y las 3 `PreG9BCu3*` de `kernel.locus.intersection` | 194 |
| `pre-g9b-cu3.desktop` | las 5 clases `PreG9BCu3*` del escritorio | 14 |

**Evidencia versionada** en `geocedg/validation/pre-g9b-cu/`: el coste de E1
(`pre-g9b-cu3-e1-cost-evidence.json`), el lector de la base frente a un documento adoptado
(`pre-g9b-cu3-e1-base-reader-evidence.json`), el coste del certificado en E2
(`pre-g9b-cu3-e2-cost-evidence.json`) y el coste de las decisiones exactas de E3
(`pre-g9b-cu3-e3-cost-evidence.json`, en el registro de verificación). Las evidencias del estudio y
de la puerta G-M3 siguen siendo las de la preparación.

## 4. Las dos incidencias en los cinco contactos

| Contacto | `P ∈ L` | `P ∈ r` | Orden de contacto | `h″` | Pliegue de `CU2` | Sin conductor, tras E3 |
|---|---|---|---|---|---|---|
| `Intersect(e_1, n, K)` | `STRICT` por transferencia (margen) | `STRICT` por el certificado | estacionario | negativo | transferido por estructura en `0` | contacto en `0`, clase 2, `FOLD_CONTACT` |
| `Intersect(e_1, c_1, K)` | `STRICT` | `STRICT` | estacionario | negativo | transferido en `0` | contacto en `0`, clase 2, `FOLD_CONTACT` |
| `Intersect(o_1, h_2, K)` | `STRICT` | `STRICT` | tangente | positivo | — (punto regular) | `u_1`: tangencia en `0`, clase 2, orden `TWO` |
| `Intersect(e_1, c_1, J)` | `STRICT` | `STRICT` | estacionario | negativo | transferido en `π` | la raíz de la costura es el contacto en el pliegue de `π`, clase 2 |
| `Intersect(e_1, r, J)` | `STRICT` | `STRICT` | estacionario | positivo | transferido en `π` | ídem |

Los mismos resultados en la réplica y en el modelo del autor. El certificado usa ocho
indeterminadas (`A.x`, `A.y`, `B.y`, `rbCone`, `H.x`, `H.y`, `D_1.x`, `D_1.y`) y corta `H`, cuyo
camino lee la rotación `C′`; vale en su celda para todo valor de ellas. Los signos de `h″` son los del
estudio (§1.7). La correspondencia exacta sigue la permutación heredada del modelo: allí `K` es la
salida 1 de `Intersect(p, i)`. `Point(o_1, K)` reproduce el recurso del autor `N_1`.

Fuera de su celda (registro de E2 §5): con `D_1` ×3, `e_1·n` y `e_1·r` quedan certificados fuera de
la parte limitada (`CONTRADICTED`), `o_1·h_2` es `INVALID` porque `M_1` no existe, y los dos
`e_1·c_1` siguen certificados; con `H` desplazado 40 unidades la prueba se rehace en la celda nueva.

## 5. Compatibilidad y persistencia

- **Documentos anteriores.** Conservan `legacy-representation-authority/v1` hasta una adopción
  explícita: CU3.4 sin cambios, las referencias y selecciones de `CU0`, `CU1` y `CU2` aceptadas, y
  las suites G8B, G8C1, G8C2 y G9U0-R4 sin cambios.
- **Adopción.** Explícita (la entrada de Álgebra la ofrece cuando una forma de `CU3` la necesita),
  atómica (mutación atómica de la construcción con restauración ante fallo), reversible (un único
  punto de Deshacer con la entrada) y con informe de impacto previo, que desde E3 aplica la regla de
  conflicto de `Σ_C` a todas las soluciones de cada resultado (V-AUT-ADOPT-8).
- **Identidad.** La adopción no cambia ninguna identidad durable, token, selector ni linaje
  (V-AUT-ADOPT-3). Las raíces que la regla de conflicto contradice tras la adopción pierden la
  admisibilidad de su token y nunca se reasocian; el informe las nombra y Deshacer las restaura.
- **Certificados de `CU2`.** Conservan su autoridad: entran en la configuración constructiva por
  `cu2-certificate-transfer/v1` (margen, estructura o nueva decisión), con sus bytes intactos.
- **Persistencia.** El registro de política va en la sección `geocedgSpatial`; ningún certificado,
  clave, signo, grado ni orden de contacto se guarda. Guardar, reabrir, Deshacer, Rehacer y copiar
  conservan la política y la identidad (registros de E1 §7 y de E2 §7).
- **Lectores anteriores (D-CU3-12, nota de migración).** Un producto anterior rechaza un documento
  adoptado con `MALFORMED_RECORD` («Unknown geocedgSpatial record: authorityPolicy»), aborta la carga
  y deja intacto el documento abierto; no reinterpreta ninguna forma de `CU3` como punto inicial
  ([evidencia del lector de la base](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e1-base-reader-evidence.json)).
  Un documento sin adoptar sigue abriéndose con él. Para compartir un documento con un producto
  anterior debe guardarse antes de la adopción, o deshacerse la adopción antes de guardar.
- **Predeterminada.** La autoridad constructiva no es la predeterminada: ningún documento nace con
  ella.

## 6. Degeneraciones y límites

1. **Censo de procedencia (cuestión abierta de E1).** Con el evaluador del producto y
   `cu3-exact-evaluation-budget/v1`, 140 de las 160 relaciones del censo se cumplen y ninguna se
   refuta; las veinte del cono de la rotación necesitan 4 126 bits frente a 4 096 y fallan cerradas
   (registro de E1 §10.1). No se propone ningún cambio.
2. **H02 en traslaciones grandes.** El segmento inclinado de `pins.H02` trasladado a `T = 10⁶` con
   `r ≤ 20` sigue perdiendo una raíz; corregirlo exigiría un suelo de posición, que las condiciones
   de parada prohíben (registro de E1 §10.2). No forma parte de la matriz.
3. **Caso especial heredado de un punto ya incidente.** La tabla de clase y el certificado lo
   reproducen (D1); los intervalos no deciden su cero exacto y una posición que dependa de él falla
   cerrada.
4. **Clase constructiva.** Arcos y conductores que no son circunferencias ni segmentos quedan fuera
   (`UNSUPPORTED_DRIVER_FAMILY`); un sustituto de frontera que se mueve con el conductor no tiene
   encierro en `Σ_C`. V-B-11 (cuarto de arco) se evalúa sin la decisión exacta.
5. **Decisiones exactas de E3.** Solo en los parámetros `k π/2` de conductores sobre
   circunferencias del marco de los ejes. Sin la identidad, la paridad no reclasifica una raíz.
6. **Un corte olvida relaciones.** Una premisa ideal que pasa por un corte no se demuestra y falla
   cerrada (registro de E2 §9.3).
7. **Casi tangencias.** Con `y = 1 + 2⁻⁵¹` la evaluación exacta da `INVALID` y con
   `y = 1 − 2⁻⁵²` `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`: nunca se crea ni se suprime una
   intersección que el objeto ideal no tenga (G-AUT-1, registro de E1).
8. **Tiempo de respuesta.** La primera prueba de `o_1·h_2` tarda alrededor de un segundo; bajo la
   política constructiva, el arrastre de `H` en el modelo vuelve a comprobar cinco decisiones exactas
   y analiza en `Σ_C` cuatro intersecciones (§7).
9. **Sin acción de menú** para la adopción, que se ofrece cuando una entrada la necesita (registro
   de E1 §10.5).

## 7. Coste y presupuestos

| Presupuesto | Estado | Máximo observado |
|---|---|---|
| `cu3-exact-evaluation-budget/v1` (100 000 operaciones, profundidad 8, 4 096 bits) | aprobado (registro de aprobación §16) | contactos del modelo: 1 531 operaciones, profundidad 2, 478 bits; `SUBSTITUTE_GENERIC`: 9 evaluaciones y 11 554 operaciones; el censo agota los bits en veinte relaciones (§6.1) |
| `constructive-identity-certificate-budget/v1` (16 000 000 productos de términos, 16 indeterminadas, 200 000 términos, 4 096 bits) | techos iniciales provisionales; G-AUT-8 los valida | E2: 1 571 831 productos (9,8 %), 21 685 términos (10,8 %) y 700 bits (17,1 %); E3, decisiones exactas: las del banco de E3 sobre el candidato, en el registro de verificación |

Toda inyección de un límite falla cerrada con `BUDGET_EXHAUSTED`, nunca como aproximación. Los
contadores son deterministas. El banco de E3 mide además el tiempo de la adopción y del arrastre de
`H` frente a la política heredada (registro de verificación). Ningún límite se ha elevado ni se
propone elevar; la aceptación productiva de los techos del certificado la decide el autor.

## 8. Pendiente

- **`CU3-B`** (germen tangente, ciclo de vida y materialización sin conductor de las clases 1 y 2):
  no implementado. Pide tres decisiones autorales (registro de E3 §7): la identidad única de un
  pliegue desdoblado frente a dos tokens transversales existentes (D-CU3-11), el desempate de dos
  contactos con el mismo germen en el punto fijo de un pliegue, y la evidencia dentro del token.
- **Documentación de usuario y de desarrollo** de la adopción, `Point(L, D)`, `Intersect(L, r, D)`
  y la integridad: no actualizada en este candidato, como en `CU0`–`CU2`; se propone para la
  publicación.

## 9. Pruebas manuales del autor

- La oferta de adopción en inglés y en español: el informe, Adoptar y Cancelar; Deshacer de la
  adopción con el punto creado.
- Los cinco contactos del modelo tras la adopción: en el inspector, `P ∈ r` como `STRICT
  CONSTRUCTIVE_IDENTITY_CERTIFICATE`; las filas del inspector y los campos del diálogo de
  resultados ricos.
- Un arrastre de `H` y de `B`: los contactos siguen `STRICT`; el tiempo de respuesta frente al
  documento sin adoptar.
- `D_1` ×3: las salidas certificadas de `n` y de `r`.
- `Intersect(e_1, c_1)` e `Intersect(e_1, r)` en el modelo adoptado: la costura como contacto en el
  pliegue; `α_cone` 35°: sin puntos de ruido junto a los contactos; Deshacer de la adopción.
- La apertura de un documento adoptado con un producto anterior; Deshacer, Rehacer y reapertura.

## 10. Decisiones que se piden al autor

1. La aprobación del candidato `CU3-A`, o los cambios que pida.
2. `CU3-B`: autorizarlo como incremento aparte, con las tres decisiones de §8, o diferirlo.
3. El censo de procedencia: reproducirlo con un presupuesto propio de caracterización, fuera del
   producto, o bastar la regresión sin refutaciones (§6.1).
4. La aceptación productiva de los techos provisionales del certificado (§7).
5. Si el tiempo de respuesta bajo la política constructiva (§6.8) es aceptable o pide trabajo de
   rendimiento.
6. La publicación de la rama, que no se ha hecho.
