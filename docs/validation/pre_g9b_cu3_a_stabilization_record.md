# PRE-G9B-CU3 — Registro de estabilización de `CU3-A`: auditoría de T7-a, P1 y verificador

| Campo | Valor |
|---|---|
| Carácter | Registro técnico de las puertas G0 a G3 y G5. No es una aprobación; `CU3-A` y `CU3` no están `PASS — AUTHOR APPROVED` |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Estabilización de CU3-A, resolución presupuestaria, mantenimiento del verificador y preparación arquitectónica de CU3-B», de 2026-10-27 |
| Candidato | `9f6f27f265725b27673f5c37880947ad6c4f2399` (árbol `abe10e3d4eda338be2b3c36373c4b09af21e7fdd`), sin cambios de producto en esta etapa |
| Resultado | G1 `SATISFIED WITH RECORDED PRE-EXISTING ASSUMPTIONS`; G2 `BLOCKED — AUTHOR NORMATIVE DECISION REQUIRED` (D1); G3 corregido en una rama aparte; G5 candidato congelado y verificado, con la declaración de listo para el smoke pendiente de D1 |
| `selfApproved` | `false` |

## 1. G0 — Inventario e identidad

| Elemento | Comprobado |
|---|---|
| Rama | `phase/pre-g9b-cu3-implementation`, árbol de trabajo limpio, sin archivos sin seguimiento |
| HEAD local | `4fc672244b53dad9c55dbbe0360f6455b3f45400` (árbol `a809a278`) |
| Remoto | `origin/phase/pre-g9b-cu3-implementation` = `b1813da1bf4e8a11abb579e833ab9b3cb45b3906`; `main` = `origin/main` = `e6c908f2` |
| Commits locales | 17 sobre `b1813da1`, de T6 (`9e11496d`) al registro de T7-a (`4fc67224`) |
| Ancestros | `3f096838` (T6), `22891c7b` (Puerta N), `247f335c` (finales de línea), `9f6f27f2` (T7-a) y `4fc67224`, todos en la historia de HEAD |
| Árbol de T7-a | `9f6f27f2^{tree}` = `abe10e3d4eda338be2b3c36373c4b09af21e7fdd` |
| Producto entre `9f6f27f2` y HEAD | sin diferencias en `source`, `tools` ni `geocedg/specs` |

No hay modificaciones inesperadas. Los commits locales no publicados son los registrados en los
registros de T6 y T7-a.

## 2. G1 — Auditoría focal de T7-a

**Contrato frente a implementación.** `RootCountCertificate2D.Search.critical` llega a la regla de
la v2 solo después de `run()`. En ese punto ya están establecidas estas cuatro condiciones:

| Condición | Cómo la establece el código |
|---|---|
| Encierro no nulo de `T` | primera caja de `fixedSign(a, b, 2)`; si es nulo, `INVALID` antes de llegar aquí |
| `0 ∉ h''(T)` | todas las subcajas de `fixedSign(a, b, 2)` con el mismo signo certificado `s` |
| Unicidad del punto crítico | signos certificados y opuestos de `h'` en `a` y `b` |
| `t* ∈ [l, u]` | bisección con signos certificados de `h'`; un signo indeciso detiene la bisección sin estrechar el corchete |

La regla, solo para `isConstructive()`, sigue estos pasos:

1. Calcula un encierro de `[l, u]`; si es nulo, devuelve `invalid`.
2. Si su signo de `h` es indeciso, consulta `exactDouble`.
3. Solo un resultado `ONE_DOUBLE` termina la búsqueda.

`exactDouble` exige:

- que el encierro exterior de `t0` esté dentro de `[l, u]`;
- `h(t0) = h'(t0) = 0` exactos, propios de la decisión exacta;
- un signo exacto de `h''` igual a `s` o no decidido.

**Demostración.** `h'` es estrictamente monótona en `T`, así que `t0` es su único cero en `T`. Por
Taylor con resto, `h` tiene el signo `s` en `T \ {t0}`: `t0` es la única raíz, aislada y doble. La
demostración usa solo las precondiciones, no la comparación con la v1. En cualquier otro caso, la
v1 continúa desde el mismo encierro (`fixedSign(l, u, 0, enclosed)`) con las mismas evaluaciones.

**Encierros e `INVALID`.**

- La auditoría independiente de la [Puerta N](pre_g9b_cu3_t7a_gate_n_record.md) §6 sigue
  correspondiendo al código de `9f6f27f2`: ninguno de los archivos auditados cambió después.
- Un encierro no nulo certifica validez, ramas constantes (`C²`) y contención en toda la caja, bajo
  A1 (precisión de `StrictMath`) y A2 (IEEE). No se usa ninguna monotonía.
- Las cuatro clases de fallo se distinguen como en la Puerta N §7.
- Las observaciones F1 a F6 son preexistentes, y ninguna invalida T7-a:
  - F2 (dominio de un segmento) no alcanza a T7-a, porque solo un conductor sobre una circunferencia
    tiene decisiones exactas;
  - F3, F4 y F6 solo producen rechazos;
  - F5 no afecta a los encierros, y en el corte el sistema exacto rechaza;
  - F1 es documental.
- El certificado no tiene garantías incondicionales: depende de A1 y A2, las mismas de la v1.

**Contador de evaluaciones (4 097 frente a 4 096).** `Search.enclose` incrementa el contador antes
de comparar con `MAXIMUM_EVALUATIONS`, y lanza `Exhausted` sin llamar a la fuente cuando lo supera.
Una sonda externa compilada contra el jar del producto contó las llamadas reales a la fuente en una
búsqueda que agota el límite:

| Fuente | Forma | Contador del certificado | Encierros calculados |
|---|---|---:|---:|
| no constructiva | `NOT_CERTIFIED`, límite de trabajo | 4 097 | 4 096 |
| constructiva sin contacto exacto | `NOT_CERTIFIED`, límite de trabajo | 4 097 | 4 096 |

Es una convención de telemetría: el contador incluye el intento rechazado, y ningún trabajo
geométrico excede el límite. No se corrige nada.

**Compatibilidad.**

| Propiedad | Evidencia |
|---|---|
| Documentos heredados en la v1 | gate `isConstructive()`; `aLegacyDocumentKeepsVersionOne`; `CapturedResidualJetSource2D` sin decisiones exactas |
| Documentos adoptados en la v2 | `getPolicyVersion` por la recalificación constructiva; `everyExactExistenceIsADoubleRootUnderVersionTwoInEveryConfiguration` |
| Tokens y persistencia | la evidencia es de revisión y no entra en tokens; `theDecisionFollowsTheConfigurationThroughUndoRedoAndReopen` comprueba que el XML no contiene ni la versión ni el certificado |
| Admisibilidad | solo cambia por la regla de conflicto; en los dos casos de `rbCone = 7,5` sigue siendo `false` |
| Deshacer, Rehacer, reapertura y configuración vigente | pruebas de la réplica y campaña `INTEGRATED_PHASE` |

**Puerta G1: `SATISFIED WITH RECORDED PRE-EXISTING ASSUMPTIONS`** (A1, A2; F1 a F6 sin cambios).

## 3. G2 — P1: presupuesto de la comprobación acelerada

### 3.1 Flujo

Una decisión exacta en una revisión (`ConstructiveResidualJetSource.decide`, y los puntos
estructurales) sigue este flujo:

1. Usa la decisión memorizada del mismo estado.
2. Si no la hay, comprueba con `recheckAtParameter` el certificado de una revisión anterior.
3. Si la comprobación falla, hace un intento nuevo (`incidenceAtParameter`), con su propio
   presupuesto, como exige `locus-v2-constructive-authority.md` §10.

T1 a T3 aceleran solo el paso 2, que es `ConstructiveIdentityCertifier.recheckCell`:

| Operación | Trabajo contabilizado | Presupuesto actual | Alcance semántico |
|---|---|---|---|
| Memoria acelerada (T1 a T3) | especialización en los valores puntuales, derivadas guardadas y encierros de las condiciones y márgenes | `memoryCounters`, uno nuevo de `budget` por comprobación | interno de la comprobación |
| Preparación del margen (T2) | identidad `A + x U + y W` de cada salida comprobada, una vez por certificado | contadores nuevos de `budget` por salida, en la primera comprobación que lo pide | interno, memorizado entre revisiones |
| Respaldo (procedimiento del candidato) | encierro de cada condición o margen que la memoria no certifica | `forRecheck(new ConstructiveIdentityCounters(budget))`, uno nuevo por comprobación | el procedimiento anterior a T1 |
| Total de una comprobación | memoria + preparaciones nuevas + respaldo | hasta `(2 + k)` veces el presupuesto, con `k` salidas preparadas en esa comprobación | sin una cota única |

Cada contabilidad es determinista y falla cerrada por separado. El agotamiento de la memoria o de
la preparación devuelve el control al respaldo, y el del respaldo da una comprobación fallida. Nada
de esto puede promover `RELAXED` a `STRICT`: la comprobación solo certifica los signos registrados
de la celda, y la integridad la deciden la clase y la transferencia, no este paso.

### 3.2 Caracterización con contadores deterministas

Una sonda externa sobre la réplica del modelo del autor midió los productos de términos de cada
contabilidad en los cinco contactos obligatorios, en los dos ámbitos, con el presupuesto aprobado:

| Contacto | Ámbito | Memoria | Preparación | Respaldo | Total (16 000 000) |
|---|---|---:|---:|---:|---:|
| `e_1·n`, `e_1·c_1`, `e_1·r` (`K` y `J`) | automático | 2 | 16 | 30 | 48 |
| ídem | frontera | 2 | 10 | 14 | 26 |
| `o_1·h_2` (`K`) | automático | 88 | 624 591 | 658 274 | 1 282 953 (8 %) |
| `o_1·h_2` (`K`) | frontera | 21 | 951 | 1 073 | 2 045 |

En todos los casos:

- la memoria más la preparación cuesta menos que el respaldo;
- el agregado de las tres contabilidades no llega al 8 % de un solo presupuesto.

Con presupuestos restringidos:

- por debajo del consumo de la memoria, fallan la comprobación acelerada y la sencilla;
- desde el consumo del respaldo, ambas tienen éxito.

En las revisiones medidas de T1 a T6, la memoria nunca agotó su presupuesto ni recurrió al
respaldo.

### 3.3 Interpretación normativa

- **Lo que fija la especificación.** `locus-v2-constructive-authority.md` §10 fija el presupuesto
  del certificado «por intento» (each attempt), y la disposición autoral de 2026-10-26 §4 dice
  «presupuestos por intento». Una comprobación no es un intento: §8.2 la describe como aplicar la
  prueba en otra revisión «sin trabajo simbólico nuevo».
- **El único texto sobre la comprobación.** Es la tabla de la propuesta de presupuestos (registro
  de aprobación de la autoridad constructiva §14): el presupuesto del certificado se usa en «cada
  intento de prueba y cada recomprobación». La decisión autoral de §16 fijó los límites por
  intento sin repetir ese uso.
- **Lo que no fija.** Ningún texto aprobado determina si las estrategias internas de una
  comprobación (memoria, preparación memorizada y respaldo) comparten un presupuesto o si cada una
  tiene el suyo.
- **Clasificación:** caso C. La normativa no permite determinarlo de forma inequívoca. Definirlo
  sería decidir por primera vez qué unidad cubre el presupuesto en una comprobación. Según la
  instrucción, no se implementa automáticamente.
- **Lo que sí se puede afirmar.**
  - Todo intento sigue con un único presupuesto (T1 a T3 no tocan los intentos).
  - Cada contabilidad respeta el límite.
  - Con la lectura «un presupuesto por comprobación», la implementación actual permite superarlo
    en el peor caso hasta `(2 + k)` veces. No ocurre en el corpus, donde el agregado es como mucho
    el 8 % de un presupuesto.

### 3.4 Decisión D1 que se pide al autor

| Opción | Regla | Cambio | Efecto observable |
|---|---|---|---|
| **D1-A (recomendada)** | un único presupuesto por comprobación, compartido por la memoria, las preparaciones que esa comprobación desencadena y el respaldo, sin reinicios; su agotamiento hace fallar la comprobación y el intento nuevo conserva su propio presupuesto (§10) | mínimo y local: un solo `ConstructiveIdentityCounters` creado en `recheckCell` y pasado a la memoria, a la preparación y al `forRecheck` del respaldo | ninguno en el corpus. Como la memoria más la preparación cuesta menos que el respaldo en todos los casos medidos, ninguna comprobación que hoy tiene éxito con el presupuesto aprobado fallaría. En teoría, una comprobación cuyo agregado supere el presupuesto fallaría y pasaría a un intento nuevo |
| D1-B | las tres contabilidades son presupuestos internos de la comprobación, cada uno acotado, con el peor caso documentado de `(2 + k)` presupuestos | ninguno en el producto; aclaración documental en §10 | ninguno |

- **Por qué D1-A.** Coincide con el criterio arquitectónico de la instrucción: un procedimiento
  interno no obtiene trabajo adicional reiniciando el presupuesto de una operación semántica.
- **Si se elige D1-A:**
  - se implementa con commits trazables;
  - se añaden las pruebas de §3.5 de la instrucción (sin respaldo, con respaldo, agotamiento en la
    memoria, en la preparación y en el respaldo, fallos parciales, recálculo por revisión, cambios
    de configuración, determinismo y ausencia de promoción a `STRICT`);
  - se congela un candidato nuevo y se repite `INTEGRATED_PHASE`.
- **Si se elige D1-B:** el candidato `9f6f27f2` no cambia.

**Puerta G2: `BLOCKED — AUTHOR NORMATIVE DECISION REQUIRED`** (D1).

## 4. G3 — Compatibilidad con PowerShell 7.6.9

- **Diagnóstico reproducible.** Con un esquema simple, `Test-Json -ErrorAction Stop` lanza la misma
  identidad en 7.6.6 y en 7.6.9 (`InvalidJsonAgainstSchemaDetailed`; sintaxis: `InvalidJson`).
  - Las diferencias aparecen en restricciones condicionales: el contrato puro de tipo de nodo y la
    terminología de canales mezclada. Ahí, 7.6.9 devuelve `$false` sin registro de error.
  - `Assert-VerificationJsonSchema` lanzaba entonces su error genérico, que también es un rechazo.
    Por eso el rechazo era el mismo en ambas versiones y solo cambiaba la identidad.
  - Dos pruebas exigían la identidad de `Test-Json`. Era una dependencia accidental.
- **Corrección, solo de infraestructura**, commit `08776f45` en la rama local
  `maintenance/pre-g9b-cu3-verifier-pwsh769` (worktree aparte), sin mezclar con el candidato:
  - `Assert-VerificationJsonSchema` da a todo rechazo de esquema, lanzado o devuelto, la identidad
    `VerificationJsonSchemaRejected` con categoría `InvalidData` y el detalle de `Test-Json` como
    excepción interna;
  - cualquier otro fallo (esquema malformado o inválido, E/S) se propaga sin cambios;
  - las dos pruebas exigen esa identidad exacta, y una prueba nueva comprueba que un esquema
    inválido no se informa como rechazo;
  - no cambia ningún esquema, criterio ni archivo de producto.

| Comprobación | 7.6.9 | 7.6.6 aislado |
|---|---|---|
| Esquema simple: válido, tipo, requerido, propiedad extra, no objeto | mismos resultados | mismos resultados |
| Rechazo vía el módulo corregido | `VerificationJsonSchemaRejected` / `InvalidData` | idéntico |
| `verification-registry.Tests.ps1` | 22 casos `PASS` | 22 casos `PASS` |
| `verification-report.Tests.ps1` (con el caso nuevo) | 9 casos `PASS` | 9 casos `PASS` |
| `INFRA_UNIT` completo en la rama | `verification-b3d0b1495651436ba151a5c517f1f439` `ACCEPTED / COMPLETE` 22/22 | `verification-893ea96d32564ab1b25d0c9f5dbcede7` `ACCEPTED / COMPLETE` 22/22 |

- **Incorporación futura.** El commit es independiente y puede incorporarse por selección en una
  rama de mantenimiento o en `main` cuando el autor lo autorice. Hasta entonces, la deuda
  `DEBT-VERIFIER-PWSH-769-TESTJSON` queda resuelta técnicamente en esa rama, pero no en la rama de
  `CU3`.
- **Verificación del candidato geométrico.** Sigue usando PowerShell 7.6.6 aislado para
  `INFRA_UNIT`.

## 5. G5 — Candidato

- **Identidad.** Sin cambios de producto (G1 y G2), el candidato sigue siendo `9f6f27f2`.
- **Campaña reutilizada.** La campaña `INTEGRATED_PHASE` registrada en el
  [registro de T7-a](pre_g9b_cu3_t7a_implementation_record.md) §7 corresponde a ese candidato y se
  volvió a autenticar el 2026-10-26: hash, diario, candidato, árbol y firma `de567676…`. Esta etapa
  no la repite, porque los cambios posteriores son solo documentales.

| Perfil | Ejecución | Estado |
|---|---|---|
| `STATIC` | `verification-44a0828328d849059f973c1d8941792f` | `ACCEPTED / COMPLETE` 3/3 |
| `INFRA_UNIT` (7.6.6 aislado) | `verification-6342897440a547debf5691c31a3b9b1c` | `ACCEPTED / COMPLETE` 22/22 |
| `PHASE` `CU3` | `verification-a731357ab7074d369d0d650a74dadcca` | `ACCEPTED / COMPLETE` |
| `PHASE` `CU2` | `verification-5a637acd2be145fc8c19665e5bb7cfbf` | `ACCEPTED / COMPLETE` |
| `PHASE` `CU1` | `verification-f4aa2e72a95942fb9fff98b97ae60ac4` | `ACCEPTED / COMPLETE` |
| `PHASE` `CU0` | `verification-4ae9645951454f4899e15ccc05d43831` | `ACCEPTED / COMPLETE` |
| `INTEGRATION` | `verification-9e5c794a18fd4f24b809b0439b86caee` | `ACCEPTED / COMPLETE` 19/19 |

**Puerta G5.** El candidato está congelado y verificado, y su guía de smoke existe. La declaración
`TECHNICAL CANDIDATE READY FOR AUTHOR SMOKE` exige además P1 resuelto, así que depende de D1:

- con D1-B, se declara sobre `9f6f27f2` sin trabajo adicional;
- con D1-A, sobre el candidato nuevo, tras su verificación.

## 6. Documentos de esta etapa

- [Hoja de decisiones de `CU3-B`](../architecture/pre_g9b_cu3_b_decision_sheet.md) (G4).
- [Guía de prueba manual](pre_g9b_cu3_manual_test_guide.md) actualizada y
  [hoja de resultados](pre_g9b_cu3_a_smoke_results_sheet.md) (G6).
- [Expediente de cierre de `CU3-A`](pre_g9b_cu3_a_closeout_dossier.md), preparado y no aplicado (G7).
