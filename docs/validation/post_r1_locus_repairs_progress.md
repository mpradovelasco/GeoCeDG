# Ejecución posterior a R1 — registro de progreso y checkpoints

**CANDIDATO TÉCNICO — PENDIENTE DE REVISIÓN AUTORAL.** `selfApproved=false`. Registro único
de progreso del encargo «Ejecución secuencial tras R1: reparaciones, modelo y uso
condicionado de Locus V2» y de su continuación «Revisión normativa post-R1, continuación de
implementación y objetivo 1.2.2» (instrucciones autorales del 2026-11-21). No es un cierre
de fase, no registra aprobación y no autoriza publicar en `main`.

## Plan y perímetro

1. Bloque 1 — reparaciones A, F7, F6, B y A′, con pruebas focales, aclaraciones normativas
   estrechas y benchmark.
2. Bloque 2 — modelo corregido (transporte del contorno del agujero) y evidencia portable.
3. Bloque 3A — diseño completo del uso condicionado y paquete normativo para revisión.
4. Revisión normativa del paquete 3A con las decisiones D-V1 a D-V7 del autor, y
   optimización D-V6 en línea separada.
5. Bloque 3B — implementación de V: **no iniciada**; requiere aprobación del texto
   normativo revisado.
6. Candidato GeoCeDG 1.2.2: **no iniciado**; ninguna fuente de versión se ha tocado.

Perímetro: kernel Java compartido (huella del slice, ledger, evaluador aislado, registro
de identidad, productor de intersecciones, admisión de expresiones), mensajes, pruebas,
modelos de regresión, validación, arnés de benchmark, especificaciones de locus y
documentación. Fuera de perímetro: `CLAUDE.md`, `AGENTS.md`, prompts, guías de usuario,
empaquetado, libro.

## Estado inicial

| Dato | Valor |
|---|---|
| `main` local y `origin/main` | `9ccefdcfe922d8c0e329bdeec30c12fc058de463`, árbol `18dbb9bc4216a536b5b2fc3d17321737001cdd36`, limpio |
| Rama de trabajo | `maintenance/post-r1-locus-repairs-and-conditioned-use`, creada desde esa base |
| Modelo original | `C:\temp\InterCilConoObliqueFinal_2.cedg`, 78 781 bytes, SHA-256 `949c2c931d22ec33820cd3ae5b35e8a75b94f5f3d45474fb8e818d74ff98159b`: verificado |
| Evidencia R1 | presente (dos emisiones del 2026-11-21); ningún artefacto auxiliar faltante |
| Política de verificación | `verification-levels.md`, «Operational simplification»: ordinaria = CORE + FOCUSED; `FINAL` solo con autorización expresa. Vigente |
| Estación | PowerShell 7.6.10; JVM de pruebas OpenJDK 17.0.10; Windows 11 |

## Checkpoint 1 — bloque 1: reparaciones

Estado: **terminado**. Diseño, alternativas y pruebas discriminantes:
[documento de reparaciones](../architecture/post_r1_locus_repairs.md).

| Pieza | Commit | Prueba focal |
|---|---|---|
| A — huella del slice y ledger | `3881d7340` | `PostR1SliceLedgerFingerprintTest` (6) |
| F7 — propiedad de auxiliares | `bc878779d` | `PostR1DedicatedAuxiliaryOwnershipTest` (5) |
| F6 — autoridad de la evaluación aislada | `d111a2e55` | `PostR1IsolatedEvaluationAuthorityTest` (5) |
| B — actualización equivalente | `efed59bc8`, `246f0bb2d` | `PostR1EquivalentIntersectionUpdateTest` (4) |
| A′ — conductores anidados | `417ab2c0f` | `PostR1NestedLocusDriverFingerprintTest` (4) |

Evidencia durante el desarrollo (Gradle directo, no perfil de aceptación): todas las
pruebas `org.geocedg.*` del módulo compartido sobre `417ab2c0f`, 245 suites, 2 410 casos, 0
fallos. Ablaciones: A 4/6, F7 4/5 y A′ 3/4 casos fallan sin la reparación.

Benchmark antes/después con el mismo arnés: paso de `E_1` en el modelo original 2 369 ms →
58 ms; paso de `N` 1 582 ms → 24 ms; revisiones de `w_2`/`a_3` por paso 1 → 0.

## Checkpoint 2 — bloque 2: modelo y evidencia portable

Estado: **terminado**. Commit `49704eb7a` (paquete, oráculo, pruebas Desktop y arnés) y
`2dd34f248` (escenario de inspección de evidencia).

- Paquete `models/regression/post-r1-locus-on-intersection/`: original byte-exacto, receta
  única del transporte, variante generada, manifiesto y resultados esperados.
- Referencia independiente `geocedg/validation/post-r1-locus/`: reproduce byte a byte las
  tablas de R1 con entradas reducidas.
- `PostR1AuthorModelRegressionTest` (3 casos): desviación máxima frente a la referencia
  1,5·10⁻⁸; imagen conjugada ausente; independencia de `E_1`; guardado y reapertura con las
  mismas identidades.
- Limitación vigente declarada: el contorno transportado es fiel y `NOT_ESTABLISHED`; su
  dibujo y su consumo son los de la evidencia exploratoria hasta que exista V.

## Checkpoint 3 — bloque 3A: diseño del uso condicionado

Estado: **paquete entregado para revisión; implementación de V en pausa**.

| Documento | Ruta |
|---|---|
| ADR propuesto | `docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md` |
| Contrato candidato | `geocedg/specs/locus/candidates/post-r1-conditioned-use.md` |
| Diseño | `docs/architecture/post_r1_locus_conditioned_use_design.md` |
| Hoja de decisiones | `docs/validation/post_r1_conditioned_use_author_decision_sheet.md` |

Experimentos disponibles: testimonio de raíz en la copia aislada (`evidence`), sonda
externa de la travesía (`conditioned-use-probe.js`). Identidad del paquete: la del commit
que contiene este registro; véase «Identidad del candidato».

## Checkpoint 4 — revisión normativa, D-V6 y objetivo 1.2.2

Estado: **paquete normativo revisado entregado para aprobación; D-V6 terminado; 3B y el
candidato 1.2.2 sin iniciar**. Rama `maintenance/geocedg-1.2.2-post-r1`, creada desde
`96a4137e0`, local y sin publicar. La rama anterior no se ha movido.

Decisiones del autor incorporadas (D-V1 a D-V7): constan, con el lugar donde queda cada
una, en el [registro de decisiones](post_r1_conditioned_use_author_decision_sheet.md). Son
decisiones de producto; el ADR 0042 sigue `PROPOSED` y el contrato sigue siendo candidato.

| Línea | Commit | Contenido |
|---|---|---|
| D-V6, código | `4cbe89a3e` | `StructureMemo` en `ReconstructionSliceFingerprint2D`, solo dentro de copias aisladas; `PostR1FingerprintStructureMemoTest` (2); escenario `profile` y series nuevas del arnés |
| D-V6, registro | `266d0a85e` | [documento de rendimiento](../architecture/post_r1_locus_real_change_performance.md): perfil, atribución, alternativas excluidas, antes y después, hallazgo F8 |
| Revisión normativa | `83cd56c4c` | ADR 0042 revisión 2, contrato `conditioned-use/v2-candidate`, diseño revisión 2 y registro de decisiones, con las diferencias respecto a `ab37fb5ff` |
| Inventario | `1838c7211` | la prueba nueva entra en `post-r1-locus.shared` (211 identidades); descubrimiento compartido 6 942; pin del catálogo |

D-V6, medido con el mismo arnés sobre `ab37fb5ff` y `4cbe89a3e`, actualización del grafo
por cambio de `excCyl`: modelo original 1 696 → 605 ms de mediana (p95 1 705 → 627);
variante transportada 2 132 → 719 ms (p95 2 162 → 723); a través del cambio de topología y
tras pérdida y recuperación, máximos de 644 y 720 ms. Objetivo de menos de 1 s, también en
p95: cumplido. Los contadores de revisiones, reconstrucciones y evaluaciones son idénticos
antes y después. Sin caché compartida de resultados ni copias mutables compartidas.

Hallazgo **F8**, previo y reproducible en la base `9ccefdcfe`: un locus anidado vivo pierde
su definición cuando el conductor de un locus consumidor es entrada no conductora suya.
Reproductor en el documento de rendimiento, §4. No triado ni tocado; propuesto dentro de
I4 (punto P4 del registro de decisiones).

Puntos que requieren decisión del autor: P1 (aprobación del texto normativo revisado), P2
(marca del sistema), P3 (ubicación en el roadmap de la exportación semántica) y P4 (F8).
El roadmap no se ha modificado.

## Verificación del candidato

### Candidato de la continuación

Candidato técnico verificado: `1838c721149bb6a4ad9088dff767cc64320cfb00`, árbol
`52329d83632de0c6ffc609400aa1dad31d19da7a`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. Todas las ejecuciones desde la raíz, con PowerShell 7.6.10.

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-1838c721` | 0 | `verification-c364304a1a9647eb95843bd9bf3c2a12` | `ACCEPTED / COMPLETE` | 3 contratos; 6 diagnósticos |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-1838c721` | 0 | `verification-b4a84680006a4916b63234788d0fc0a0` | `ACCEPTED / COMPLETE` | 6 contratos, 5 productores; 211 identidades compartidas y 24 Desktop; 0 diagnósticos |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-1838c721` | 0 | `verification-09243cff866f47d5a029c5bcca545ac5` | `ACCEPTED / COMPLETE` | 18 contratos, 15 productores; 9 diagnósticos |

Diagnósticos distintos de `DIAGNOSTIC_CLEAR`: los mismos dos permanentes que abajo.

Otras comprobaciones de la continuación, con Gradle directo y no como perfil de aceptación:
las pruebas compartidas de locus (1 935 casos) pasan con el modo de verificación del memo
activado, que recalcula siempre y falla si un resumen reutilizado difiere. Los cuatro
puntos de cobertura reducida de abajo siguen vigentes sin cambio: `FINAL` no ejecutado ni
autorizado, `final.shared` y `final.desktop` sin refrescar, `INFRA_UNIT` completo no
ejecutado, y el fallo previo de `PreG9BCu2AuthorModelConsumerTest`. No se formula
`VERIFICATION_ESCALATION_REQUEST`.

### Candidato de reparaciones

Candidato técnico verificado: `ab37fb5fff0fadca13f4488eaff85d7917276c42`, árbol
`bec152ae807e3d0cbc8bf9e0abbe685ccdd09c17`, árbol de trabajo limpio. Política aplicada:
ordinaria = CORE + FOCUSED. Todas las ejecuciones desde la raíz, con PowerShell 7.6.10.

| Nivel | Comando | Código | Identidad de ejecución | Veredicto | Notas |
|---|---|---|---|---|---|
| `STATIC` | `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/post-r1-locus-static-ab37fb5f` | 0 | `verification-4266398083c34bb1bcb870c5f8f73d77` | `ACCEPTED / COMPLETE` | 3 contratos; 6 diagnósticos |
| FOCUSED: `PHASE` | `tools/agent/verify.ps1 -Profile PHASE -Phase POST-R1-LOCUS -LogDirectory artifacts/agent/post-r1-locus-phase-ab37fb5f` | 0 | `verification-8d64bf2053a24a559ac0f51c201a445d` | `ACCEPTED / COMPLETE` | 6 contratos, 5 productores; 209 identidades compartidas y 24 Desktop; 0 diagnósticos |
| CORE: `INTEGRATION` | `tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/post-r1-locus-integration-ab37fb5f` | 0 | `verification-9627e2466f7f481eac71f9653990ca6b` | `ACCEPTED / COMPLETE` | 18 contratos, 15 productores; 9 diagnósticos |

Cada recibo está en `verification-result.json` de su directorio de log (ignorado por Git).
Diagnósticos distintos de `DIAGNOSTIC_CLEAR`, que no afectan a la aceptación:
`diagnostic.governance` = `DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` =
`DIAGNOSTIC_UNAVAILABLE`, las dos notas permanentes del verificador.

Otras comprobaciones:

- `git diff --check`: limpio en cada commit.
- `tools/agent/tests/verification-final-coverage.Tests.ps1`, ejecutada sola: 13 casos, 822
  aserciones, 0 fallos, con los dos recuentos actualizados (98 selecciones, 89 fases).
- Regresión Desktop adyacente ejecutada con Gradle directo, no perfil de aceptación: 17
  suites, 93 casos, 1 fallo, que es previo (véase abajo).

Cobertura reducida, declarada:

1. No se ejecutó `FINAL` ni las suites sin filtro: no está autorizado y la política vigente
   no lo exige. Las selecciones ejecutadas `final.shared` (7 867) y `final.desktop` (2 634)
   del inventario no se han refrescado y no incluyen las pruebas nuevas; refrescarlas exige
   una campaña exhaustiva.
2. `INFRA_UNIT` completo no se ejecutó: en esta estación (PowerShell 7.6.10) dos de sus
   suites fallan en cualquier commit por la identidad de error de `Test-Json`, condición
   previa y ajena a este trabajo.
3. **Fallo previo.** `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`
   falla en la base `9ccefdcfe` con el mismo mensaje que en la rama (para `e_2` espera una
   raíz `INTERIOR` en la costura y no se publica ninguna); comprobado en un checkout limpio
   de la base. No pertenece a CORE ni a la selección de la fase. No se ha triado ni tocado.
4. La latencia interactiva en una ventana real y el frontend web no se han medido ni
   construido.

Riesgo no cubierto por CORE + FOCUSED: ninguno que este trabajo haya identificado como
propio; no se formula `VERIFICATION_ESCALATION_REQUEST`. El punto 1 es una obligación de
mantenimiento del inventario para la próxima campaña exhaustiva.

## Identidad del candidato

| Papel | Commit | Árbol |
|---|---|---|
| Base | `9ccefdcfe922d8c0e329bdeec30c12fc058de463` | `18dbb9bc4216a536b5b2fc3d17321737001cdd36` |
| Candidato técnico `T` (reparaciones, modelo, paquete 3A, selección de fase) | `ab37fb5fff0fadca13f4488eaff85d7917276c42` | `bec152ae807e3d0cbc8bf9e0abbe685ccdd09c17` |
| Registro `D` de `T` | `96a4137e0`, descendiente exclusivamente documental de `T` | — |
| Candidato técnico de la continuación `T2` (D-V6, paquete normativo revisado, inventario) | `1838c721149bb6a4ad9088dff767cc64320cfb00` | `52329d83632de0c6ffc609400aa1dad31d19da7a` |
| Registro `D2` | el commit que contiene esta versión del fichero, descendiente exclusivamente documental de `T2` | — |

Commits de `T` sobre la base, en orden: `3881d7340` (A), `bc878779d` (F7), `d111a2e55` (F6),
`efed59bc8` (B), `417ab2c0f` (A′), `246f0bb2d` (B, copias aisladas), `49704eb7a` (modelo,
oráculo, pruebas Desktop y arnés), `2dd34f248` (escenario de evidencia), `79d56efd0`
(aclaraciones normativas, documento de reparaciones y paquete 3A) y `ab37fb5ff` (selección
de fase e inventario).

Commits de `T2` sobre `D`: `4cbe89a3e`, `266d0a85e`, `83cd56c4c` y `1838c7211`.

El paquete normativo sometido a decisión es el contenido de `T2`: ADR 0042 revisión 2,
contrato `conditioned-use/v2-candidate`, diseño revisión 2 y registro de decisiones. Una
aprobación debe citar `1838c7211` (el texto es el de `83cd56c4c`, sin cambio posterior). La
revisión 1, de `ab37fb5ff`, queda sustituida.

Ramas locales `maintenance/post-r1-locus-repairs-and-conditioned-use` (en `96a4137e0`) y
`maintenance/geocedg-1.2.2-post-r1`, sin publicar. `main` local y `origin/main` siguen en
la base. Ninguna etiqueta creada ni movida; versión de producto sin cambiar (1.2.1).

## Pendiente, siguiente acción y reanudación

- **Pendiente del autor:** P1 a P4 del registro de decisiones; revisión de las reparaciones
  y del modelo (bloques 1 y 2) y de D-V6; revisión del texto de las aclaraciones normativas
  estrechas; autorización de publicación si procede.
- **No hecho:** bloque 3B (I0 a I4T). Candidato 1.2.2 (versión, empaquetado, humo de
  app-image, notas). Publicación en `main`. `FINAL` (no autorizado, no necesario bajo la
  política vigente).
- **Reanudación exacta:** con la aprobación de P1, partir de la punta de
  `maintenance/geocedg-1.2.2-post-r1`, comprobar la aprobación, el commit que cubre y si la
  base ha cambiado, y empezar por el incremento I0 del
  [plan](../architecture/post_r1_locus_conditioned_use_design.md#5-plan-de-incrementos-3b):
  testimonio de evaluación y `LocusConditionedEvidence2D` en el kernel, sin cambiar
  operaciones públicas. Orden: I0, I1, I2, I3, I4, I4T; después, el candidato 1.2.2.
- **Conservado:** el original del modelo, la evidencia R1 y las salidas de benchmark en
  `artifacts/benchmarks/post-r1-locus/` (ignoradas por Git). Dos worktrees separados, de la
  base y de `ab37fb5ff`, usados solo para medir el «antes», quedan en el scratchpad de la
  sesión.
