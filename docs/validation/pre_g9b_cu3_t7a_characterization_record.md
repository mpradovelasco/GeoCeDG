# PRE-G9B-CU3 — Caracterización científica de T7-a: decisión exacta anticipada

| Campo | Valor |
|---|---|
| Carácter | Caracterización de solo lectura respecto del producto. No implementa T7-a ni cambia ningún contrato, presupuesto o certificado. No es una aprobación |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Caracterización científica focal de T7-a», de 2026-10-26 (§1) |
| Referencia | candidato T6 congelado `3f0968385920e36b2b48191a4f6e736c4fc11c09` ([registro de T6](pre_g9b_cu3_enclosure_reuse_record.md)) |
| Herramienta | `tools/benchmark/pre-g9b-cu/run-cu3-t7a-characterization.ps1` con `cu3-t7a/` (commits `3b6bd31f` y `db752438`); medida en `db752438`, cuyo producto es idéntico al de `3f096838` |
| Evidencia | [`pre-g9b-cu3-t7a-characterization-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-t7a-characterization-evidence.json) |
| Recomendación | **VIABLE CON REVISIÓN NORMATIVA** (§8) |
| `selfApproved` | `false` |

## 1. Autorización

> DECISIÓN AUTORAL: CARACTERIZACIÓN T7-a AUTORIZADA — IMPLEMENTACIÓN NO AUTORIZADA

El objetivo es estudiar si, cuando una decisión exacta de E3 establece `h(t0) = 0`, `h'(t0) = 0`,
`h''(t0) ≠ 0` y `t0` dentro del corchete crítico, puede omitirse rigurosamente la subdivisión de
`fixedSign(low, high, 0)` y pasar directamente a `exactDouble()`.

- **Pedido:** precondiciones completas, una sonda externa reproducible y versionada, el corpus
  (modelo del autor, sus configuraciones dinámicas y las matrices de `CU2` y `CU3`), los casos
  críticos A a E, el rendimiento medido y una recomendación única.
- **No autorizado:** cambios en el producto, contratos, políticas de presupuesto, implementar T7-a
  o T7-b, `main`, fases nuevas, `geocedg_book` y declarar `CU3` `PASS — AUTHOR APPROVED`.

## 2. Qué es T7-a

En el certificado de conteo (`RootCountCertificate2D`), cuando `h''` tiene signo fijo en la caja
`T = [a, b]` y `h'` cambia de signo, `critical()` hace lo siguiente:

1. Acota el punto crítico biseccionando el signo de `h'`; el resultado es el corchete
   `[low, high]`.
2. Busca el signo de `h` en ese corchete con `fixedSign(low, high, 0)`, biseccionando hasta la
   profundidad 24.
3. Si el valor es 0, consulta la decisión exacta (`exactDouble`).

T7-a cambia solo el orden: consulta `exactDouble` **antes** del paso 2. Si la decisión exacta
sitúa `t0` en el corchete, termina ahí. Si no, sigue exactamente el camino actual.

## 3. Método de la sonda

**Copia sombra.**

- **Cómo se genera.** El lanzador la genera en tiempo de ejecución a partir del fuente del
  producto, cuyo blob comprueba, con tres sustituciones exactas:
  - `certify` delega en el registro y `certifyWith` conserva el cuerpo original;
  - en `critical()` se inserta la consulta anticipada (solo en el modo T7-a) y el registro del
    resultado de la bisección;
  - se mide la consulta exacta posterior.
- **Dónde se usa.** Se compila contra el jar del producto y va delante en el classpath solo de las
  JVM de prueba. El producto y su árbol no cambian.

**Modos.**

- `compare`: devuelve el certificado del producto, calcula el de T7-a sobre la misma fuente y la
  misma caja y registra los dos.
- `t7a`: devuelve el certificado de T7-a, para observar los resultados publicados y los tiempos.
- `off`: el procedimiento del producto a través de la sombra, como control. Reproduce los tiempos
  del candidato T6 (§7), así que la sombra no añade coste.

**Corpus.**

- Las selecciones `PHASE` de `CU3` y `CU2`, compartidas y de escritorio: 451 y 86 pruebas.
- La sonda del corpus sobre el modelo del autor adoptado, con las cuatro intersecciones del banco
  E3 y los cinco puntos estructurales. Configuraciones: base, `H` +0,5/−20/+20/+40, `B` ±3, `D_1` ×3
  y ×0,5, `rbCone` = 3 y 7,5, `α_cone` = 35° y 60°, y restauración.
- En las dos configuraciones, una instantánea canónica de todos los resultados publicados y de los
  puntos estructurales.

## 4. Análisis matemático de las precondiciones

Sea `T = [a, b]` la caja del certificado y `[low, high] ⊆ T` el corchete del punto crítico.

1. **La raíz exacta está en el corchete.** Se demuestra. `exactDouble` solo acepta un contacto cuyo
   encierro `[l, u]` de `t0 = kπ/2` cumple `low ≤ l` y `u ≤ high`. El encierro procede de
   `quarterTurns`, que usa `Math.PI/2 < π/2 < nextUp(Math.PI/2)` en aritmética racional exacta, así
   que `t0 ∈ [l, u] ⊆ [low, high]` rigurosamente.
2. **La rama y la configuración de decisión son las vigentes.** Se demuestra.
   - La decisión exacta pertenece a la misma fuente: el mismo cono de la consulta, con su selección
     de ramas y su estado. Su memoria (`ExactContactMemory`) se indexa por la huella del cono, el
     estado, la revisión semántica y los presupuestos.
   - Un certificado de otra revisión solo se reutiliza tras la comprobación de su celda
     (T1 a T3).
   - La caja `T` está dentro de la ventana de la componente del candidato.
3. **La fuente está definida en todo el intervalo omitido.** Se demuestra por el contrato de la
   fuente, con una salvedad numérica.
   - `run()` empieza con `fixedSign(a, b, 2)`, cuya primera llamada encierra `T` entero. Si ese
     encierro fuera nulo, el resultado sería `INVALID` antes de llegar a `critical()`.
   - Según `ResidualJetSource2D`, un encierro no nulo certifica que la fuente es válida y continua
     en toda la caja. Por tanto, la validez en `[low, high] ⊆ T` ya está certificada.
   - La bisección omitida podría devolver hoy `INVALID` solo si el encierro de una subcaja fallara
     donde el de `T` no falló, es decir, si la evaluación no fuera monótona respecto de la
     inclusión.
   - **Monotonía de la cadena de encierros:**
     - `OutwardInterval` es monótona: suma y resta con error exacto; producto, cociente y raíz con
       redondeo IEEE monótono y `nextUp`/`nextDown`; la división falla solo si el divisor contiene
       el cero, y en una subcaja el divisor está contenido en el de la caja.
     - `IntervalJet` y las decisiones de signo del evaluador (`requirePositive`, discriminantes,
       comprobaciones de rama) componen operaciones monótonas.
     - `DriverParameterEnclosure.cosSin` encierra con rigor: los extremos se ensanchan dos ulp y se
       añaden los extremos ±1. Su monotonía a nivel de ulp depende de que `StrictMath.cos`/`sin`
       sean semimonótonas, algo que la especificación de Java exige a `Math` pero no declara
       expresamente para `StrictMath`.
   - **Conclusión.** Un `INVALID` en la bisección omitida no queda excluido por demostración. Sería
     una falsa negativa numérica, porque el contrato ya certifica la validez en `T`. No apareció en
     ningún caso del corpus.
4. **El signo de `h''` y la unicidad del punto crítico** se conservan: ambos se establecen antes,
   con `fixedSign(a, b, 2)` y el signo de `h'` en los extremos, y T7-a no los toca.
5. **Existencia, aislamiento y multiplicidad.** Se conservan.
   - `ONE_DOUBLE` se justifica así: `h''` tiene signo fijo en `T`, luego `h'` es estrictamente
     monótona y tiene un único cero, que la decisión exacta identifica con `t0`. Con `h(t0) = 0` y
     `h'(t0) = 0` exactos, el teorema de Taylor da a `h` el signo de `h''` en `T \ {t0}`. Por tanto,
     `t0` es la única raíz, de multiplicidad dos.
   - La bisección del valor crítico no forma parte de esa justificación: solo decide qué rama sigue
     el algoritmo. La forma normativa («one double root at an exact point»,
     `locus-v2-intersections.md`, regla de conflicto) se define por esas condiciones.
6. **No se introducen aproximaciones ni tolerancias.** Se demuestra: T7-a solo reordena una
   consulta exacta.

**Consecuencia.** Cuando la decisión exacta sitúa `t0` en el corchete, un encierro riguroso no puede
certificar un signo de `h` en un intervalo donde `h` se anula. Por eso la bisección omitida solo
puede acabar de tres maneras:

| Final de la bisección omitida | Resultado hoy | Resultado con T7-a | ¿Cambia la decisión? |
|---|---|---|---|
| Valor 0 | `exactDouble` | `exactDouble` | No |
| `INVALID` | `NOT_CERTIFIED` | `ONE_DOUBLE` | Sí |
| Agota `MAXIMUM_EVALUATIONS` = 4 096 | `NOT_CERTIFIED` por límite de trabajo | `ONE_DOUBLE` | Sí |

Un signo no nulo contradiría la decisión exacta.

T7-a añade además un efecto: consulta la decisión exacta en llamadas donde hoy el valor crítico
tiene signo certificado y el producto no la consulta. Esas consultas pueden lanzar intentos del
certificado de identidad constructiva, siempre dentro de su presupuesto aprobado.

## 5. Comparación de resultados

**Llamadas a `certify` en el modo `compare`** (859 en total):

| Corpus | Llamadas | Misma decisión | Distinta | Valor crítico hoy | Evaluaciones hoy → T7-a | Consultas exactas nuevas |
|---|---:|---:|---:|---|---:|---:|
| Selecciones compartidas de `CU3` y `CU2` | 69 | 69 | 0 | 0: 54; signo: 8; no alcanzado: 7 | 33 826 → 7 916 | 8 (0,39 ms en total) |
| Selecciones de escritorio de `CU3` y `CU2` | 668 | 668 | 0 | 0: 664; signo: 4 | 379 940 → 367 436 | 4 (0,002 ms) |
| Modelo del autor, 19 configuraciones | 122 | 120 | **2** | 0: 112; **agotado: 2**; no alcanzado: 8 | 88 800 → 5 134 | 2 (las agotadas, 29,5 ms) |

- **Qué se compara.** «Misma decisión» compara la forma, la caja, las piezas, los corchetes de raíz,
  el corchete crítico, la clase de contacto, la evidencia exacta (parámetro, encierro, orden,
  curvatura, clase) y el motivo. Solo excluye el número de evaluaciones.
- **`INVALID`** no apareció en ningún corpus.
- **Las 2 diferencias** son las únicas llamadas en que el producto agota el límite de trabajo: en
  `rbCone = 7,5`, el producto queda en `NOT_CERTIFIED` («the work limit of the certificate is
  reached», 4 097 evaluaciones) y T7-a da `ONE_DOUBLE` en `t0 = 0`, orden `TWO`, con 4
  evaluaciones.
- **Escritorio.** La mayor parte de sus llamadas son de la configuración heredada, sin decisiones
  exactas. Allí T7-a no actúa y casi no ahorra.

**Pruebas en el modo `t7a`.** Las 451 pruebas compartidas y las 86 de escritorio pasan, igual que
en el modo `compare`. Esas selecciones incluyen:

- los casos obligatorios y las cinco incidencias estrictas;
- las inversiones autorizadas de los pines;
- Deshacer, Rehacer, reapertura y duplicado;
- inyecciones de presupuesto;
- el objetivo desplazado un ulp;
- la regla de conflicto con signo exacto discrepante.

**Resultados publicados del modelo del autor** (140 objetos en 19 configuraciones):

- **Idénticos tal cual:** 77.
- **Idénticos salvo identidades, recuentos de encierros y contadores de trabajo:** 61.
  - Las identidades `geo:<hash>`, y las partes de las fichas derivadas de ellas, las genera cada
    carga de la copia del modelo, así que cambian entre las dos JVM con o sin T7-a.
  - Los textos de diagnóstico citan el número de encierros del certificado («1711 enclosures»
    frente a «4 enclosures»).
- **Distintos: 2.** Son las intersecciones `o_1·h_2` (`u_1` del modelo y `cu3X4`) en
  `rbCone = 7,5`:

  | Campo publicado | Producto | T7-a |
  |---|---|---|
  | Garantía | `ESTIMATED_ERROR` | `CERTIFIED_ERROR_BOUND` |
  | Método | `SAFEGUARDED_DERIVATIVE` | `CERTIFIED_INTERVAL` |
  | Multiplicidad | `NOT_ESTABLISHED` | `ESTABLISHED` |
  | Orden de contacto | ninguno | `TWO` |
  | Diagnóstico | — | «one double root at the exact parameter 0, CONTACT_EXISTENCE_EXACT, order TWO» |

  La admisibilidad (no admisible) y la clase `TANGENT_ESTABLISHED` no cambian. El punto estructural
  `X3` es `STRICT` en ambos modos.

## 6. Casos críticos

**A. Validez.**

- `INVALID` no apareció en ninguna de las 859 llamadas.
- Las matrices cubren bandas no resueltas, discontinuidades, costuras, cambios de selección y
  fallos de encierro, con decisiones idénticas.
- En teoría, T7-a cambiaría un `INVALID` de subcaja por `ONE_DOUBLE`. Solo podría ocurrir por una
  no monotonía a nivel de ulp de `cosSin` (§4, punto 3). Sería una decisión más fuerte, justificada
  por el contrato de la fuente.

**B. Degeneraciones.**

- Contacto de orden superior (`h''(t0) = 0`): la decisión exacta no da contacto, T7-a no encuentra
  nada en el corchete y sigue el camino actual.
- Puntos críticos coincidentes: sin signo fijo de `h''` en `T`, el algoritmo no llega a `critical()`.
- Extremos y costuras: la caja respeta la ventana de la componente, y la costura en π (pliegue de
  `e_1`) da decisiones idénticas.
- Discrepancia entre el signo exacto de `h''` y su encierro: hubo un caso (anticipación
  `NOT_CERTIFIED`) con el mismo resultado que el producto.

**C. Presupuestos.**

- El producto alcanza `MAXIMUM_EVALUATIONS` = 4 096 en 2 llamadas, ambas en `rbCone = 7,5`. T7-a
  establece allí un certificado `ONE_DOUBLE` riguroso.
- El límite de trabajo no es normativo, pero su efecto cambia una decisión publicada (§5).
- Los presupuestos del certificado de identidad constructiva no cambian. T7-a puede consultarlo en
  más llamadas: 14 en todo el corpus, con un coste medido despreciable salvo las 2 agotadas
  (29,5 ms).

**D. Correspondencia.**

- `exactDouble` descarta todo contacto cuyo encierro no quepa en el corchete.
- Los contactos pertenecen a la misma fuente, revisión y configuración.
- Ninguna anticipación tomó evidencia de otro punto, rama o componente: las 104 anticipaciones
  `ONE_DOUBLE` del modelo coinciden con el producto salvo las 2 agotadas.

**E. Dinámica.**

- Las 19 configuraciones del modelo cubren:
  - cambios de celda: `D_1` ×3, `H` +40, `rbCone` y `α_cone`;
  - revisiones sucesivas;
  - restauración.
- Las selecciones cubren Deshacer, Rehacer y reapertura.
- Solo cambia la decisión en `rbCone = 7,5`.

**Escenarios que cambiarían una decisión de existencia o admisibilidad:**

- **Existencia y certificación:** donde el producto agota el límite de trabajo (observado) o
  devuelve `INVALID` en la bisección omitida (no observado). En ambos, T7-a refuerza la evidencia.
- **Admisibilidad:** no cambió en ningún caso; los contactos tangentes no son admisibles en ninguno
  de los dos modos.

## 7. Rendimiento

Misma estación y mismas condiciones. Copia sombra en los modos `off` (control, igual al candidato
T6) y `t7a`: cinco ejecuciones de cada uno, con 21 arrastres por medida. Valores en ms, mediana /
percentil 95.

| Ejecución | E3 `H` | E3 `B` | Smoke `H` | Smoke `B` | Adopción E3 |
|---|---|---|---|---|---:|
| `off` A | 105,3 / 111,0 | 109,2 / 111,9 | 126,8 / 143,9 | 132,4 / 137,3 | 2 425 |
| `off` B | 102,0 / 133,1 | 106,6 / 111,3 | 126,0 / 157,1 | 132,4 / 136,5 | 2 450 |
| `off` C | 102,2 / 108,8 | 109,4 / 125,6 | 127,5 / 146,2 | 134,4 / 138,4 | 2 689 |
| `off` D | 111,6 / 138,0 | 114,8 / 137,1 | 130,9 / 152,8 | 140,4 / 158,1 | 2 840 |
| `off` E | 109,8 / 131,3 | 113,4 / 120,0 | 127,2 / 162,2 | 135,5 / 157,1 | 2 801 |
| `t7a` A | 46,1 / 54,1 | 45,8 / 60,2 | 131,5 / 337,3 | 115,9 / 364,7 | 2 332 |
| `t7a` B | 47,0 / 55,8 | 44,9 / 50,4 | 73,1 / 81,5 | 72,3 / 77,8 | 2 549 |
| `t7a` C | 50,6 / 79,9 | 48,7 / 63,3 | 91,9 / 122,6 | 74,9 / 95,5 | 2 543 |
| `t7a` D | 51,9 / 75,3 | 49,6 / 56,4 | 73,6 / 89,7 | 70,9 / 76,7 | 2 458 |
| `t7a` E | 55,1 / 66,9 | 53,8 / 64,8 | 69,7 / 83,3 | 70,9 / 78,1 | 3 159 |
| Política heredada (registro de T6) | 67–68 | 47–51 | 41–44 | 42–44 | — |

- **Banco E3.** T7-a deja los arrastres de `H` y `B` en 45–55 ms de mediana, frente a 102–115 ms
  del producto: por debajo de 100 ms y cerca de la política heredada.
- **Configuración del smoke.**
  - En cuatro de las cinco ejecuciones, `H` queda en 70–92 ms y `B` en 71–75 ms, frente a ~126–140
    ms del producto.
  - Una ejecución (A) dio 132 y 116 ms con p95 de 337 y 365 ms.
  - Sus registros muestran el mismo trabajo que la B (1 636 llamadas, 1 032 anticipaciones). La
    diferencia está en cuándo caen las consultas exactas lentas (intentos y comprobaciones del
    certificado de identidad constructiva, de 100 a 1 000 ms, que el producto también hace después
    de la bisección).
  - No es una mejora demostrada para el smoke en todas las ejecuciones.
- **Evaluaciones y subdivisiones evitadas.** En la consulta instrumentada, las cajas por arrastre
  bajan de 504/283/1 760/932 (`o_1·h_2`, `e_1·n`, `e_1·c_1`, `e_1·r`) a 4/4/27/4 en el de `H`, y de
  506/283/2 173/908 a 4/4/8/4 en el de `B`. En el corpus del modelo, las evaluaciones bajan de
  88 800 a 5 134.
- **Coste de consultar la evidencia exacta.** Es la misma consulta que el producto hace tras la
  bisección, ahora antes. En las consultas nuevas el coste medido es despreciable. Una consulta en
  frío puede costar un intento del certificado (cientos de ms), igual que hoy.
- **Coste restante por arrastre:**
  - comprobación CIC y decisiones exactas, ~10,5 ms como en T6;
  - encierros, ~1 ms;
  - el resto es propagación, recálculo del locus y trabajo heredado.
- **Memoria y preparación.** T7-a no añade ninguna memoria. El heap retenido baja ligeramente
  (39,9–40,0 MB frente a 40,9 MB en E3) porque la memoria del corte de T6 guarda menos cajas.

## 8. Recomendación: VIABLE CON REVISIÓN NORMATIVA

- **Es matemáticamente correcta.** Las precondiciones 1, 2 y 4 a 6 se demuestran; la 3 se apoya en
  el contrato de la fuente.
- **En el corpus coincide** con el producto en 857 de 859 certificados y en 138 de 140 resultados
  publicados, salvo identidades por ejecución y recuentos de encierros.
- **Cambia decisiones y garantías observables** que requieren autorización:
  1. donde el procedimiento actual agota su límite de trabajo, T7-a establece un certificado
     `ONE_DOUBLE` y la evidencia publicada sube de estimada a certificada (observado en
     `rbCone = 7,5`);
  2. en teoría, también donde la bisección omitida devolviera `INVALID` por la no monotonía de
     `cosSin` a nivel de ulp (no observado);
  3. los textos de diagnóstico y el contador de evaluaciones del certificado cambian;
  4. la decisión exacta se consulta en llamadas en que hoy no se consulta, con el presupuesto del
     certificado ya aprobado.
- **No es «VIABLE» sin más**, porque el punto 1 cambia evidencia publicada en un caso real.
- **No es «NO ESTABLECIDA»**, porque las diferencias están explicadas y acotadas.
- **La coincidencia en el corpus no es una demostración general.** La demostración es la de §4, con
  su salvedad del punto 3.

**Cambios normativos mínimos, si el autor decide implementar T7-a (no aplicados):**

1. **Nueva versión de la política.** Pasar de `cu3-certified-admission/v1` a una v2 que diga: «la
   forma de una raíz doble en un punto exacto se establece por sus condiciones; la decisión exacta
   de la configuración puede consultarse antes de buscar el signo del valor crítico, y un `t0` cuyo
   encierro esté dentro del corchete del punto crítico certificado decide la forma sin más
   subdivisión».
2. **Diferencias aceptadas.** Declarar que un certificado que la v1 dejaba sin certificar por su
   límite de trabajo puede quedar certificado en la v2, sin tolerancias nuevas y con el mismo
   límite.
3. **Diagnósticos.** Declarar informativos el número de encierros de los diagnósticos y el
   contador de evaluaciones.
4. **Rama `INVALID`.** Decidir si basta el contrato de la fuente, o si T7-a debe mantener una
   comprobación de validez del corchete para reproducir también esa rama.

## 9. Asuntos que siguen separados

Ninguno queda resuelto ni autorizado por esta caracterización:

- la deuda `DEBT-VERIFIER-PWSH-769-TESTJSON`;
- la cuestión de los contadores acumulados de T1 a T3 y su respaldo;
- las decisiones arquitectónicas de `CU3-B`;
- la aceptación autoral definitiva de `CU3-A`;
- la futura promoción de la autoridad constructiva a predeterminada.

T7-b sigue fuera de alcance.

## 10. Estado

- **Commits locales.** Sobre `685dd4bf`: `3b6bd31f` y `db752438` (herramienta), y el de este
  registro. El producto es idéntico al de `3f096838`. Nada publicado; `main`, las etiquetas y
  `geocedg_book` no cambian.
- **Salidas sin seguimiento.** Están en `artifacts/benchmarks/pre-g9b-cu/`:
  - `cu3-t7a-characterization-db752438`;
  - `cu3-t7a-performance-extra-db752438`, con las ejecuciones de rendimiento C a E.

  El JSON de evidencia guarda sus SHA-256.
- **Comparación del corpus.** La del resumen de la ejecución es la comparación literal. La
  normalizada de §5 es la misma función que ahora contiene el lanzador, aplicada a esas salidas.
- **Pendiente del autor.** T7-a no está implementada ni autorizada, y su implementación queda a
  decisión del autor. `CU3` no está `PASS — AUTHOR APPROVED`.
