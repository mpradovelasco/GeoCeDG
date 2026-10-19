# PRE-G9B-CU0 — Certificación híbrida de existencia y continuidad (entregable E)

| Campo | Valor |
|---|---|
| Carácter | Propuesta técnica del paquete `CU0` para la decisión DA-1. No es especificación normativa ni autoriza implementación; la especificación concreta se somete a aprobación antes de `CU2` |
| Estado | `CU0 DESIGN CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral de 2026-10-19, §3.2 (DA-1 aprobada como dirección híbrida) |
| Autoridad vigente | `geocedg/specs/locus/locus-v2-existence-components.md` 1.0 (`NORMATIVE — AUTHOR APPROVED`), ADR 0027, `locus-v2-certified-construction-model.md` 1.1, `locus-v2-semantics.md`, `AGENTS.md` §11 |
| Evidencia | [línea base CU0](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json) (fijaciones `B07`, `H01`, `B10.2`); [análisis de longitud orientada](pre_g9b_cu0_oriented_length_analysis.md); estudio de código de `CU0` resumido en el [informe del candidato](../validation/pre_g9b_cu0_baseline_candidate_report.md) |

## 1. Reglas que la propuesta no altera

Del encargo y de la autoridad vigente:

1. Una componente no se certifica porque todas sus muestras sean válidas.
2. La completitud global no se infiere de una exploración finita.
3. Una región no certificada no bloquea resultados locales cuya admisibilidad pueda
   establecerse independientemente.
4. No se inventan continuidades, componentes ni identidades.
5. Los productores dependientes fallan cerrados (`NOT_ESTABLISHED`) mientras no haya
   evidencia (ADR 0027).
6. Ninguna coordenada, muestra visual, vista o resolución establece identidad
   (`locus-v2-existence-components.md` §4).

## 2. Situación actual (hechos de `CU0`)

| Hecho | Referencia |
|---|---|
| El modelo de existencia ya tiene los ejes del encargo: cobertura `CERTIFIED_EXISTS` / `CERTIFIED_DOES_NOT_EXIST` / `UNRESOLVED`, maximalidad `ESTABLISHED` / `LOCAL_CERTIFIED_SUBINTERVAL` y completitud `COMPLETE` / `NOT_ESTABLISHED` | `LocusExistenceStructure2D.java:23-39` |
| La evaluación puntual ya es independiente de las componentes para ramas `NOT_ESTABLISHED` | `LocusDefinition2D.java:137-143` |
| No existe un tipo para evidencia exploratoria no certificada; las evidencias son cadenas libres | `LocusExistenceStructure2D.java:42-127` |
| El productor dependiente ordinario publica `completeCompatibility` (`COMPLETE`, una componente igual al dominio declarado, sin prueba). Cinco predicados independientes de «cierre periódico» lo aceptan (dibujo, exportación, rutas métricas, costura de intersecciones, resolvedor de identidad) | `AlgoDependentPointLocusV2.java:164-191`; `LocusRenderCache2D.java:137-147` y otros |
| La publicación exige que el trazador esté definido en la posición actual del conductor (H01) | `AlgoDependentPointLocusV2.java:112-128`; `AlgoLocusV2.java:86-92` |
| Único certificador dinámico: raíz seleccionada SplineV2 ∩ paralela con conductor circular; 512 celdas fijas; siempre `NOT_ESTABLISHED` | `SelectedRootContinuousDomainCertifier2D.java` |
| Existe un modelo de intervalos riguroso de construcciones ordinarias (clases v1/v2: valor y primera derivada con redondeo hacia fuera), pero solo se usa para pares y exportación; no distingue «indeciso» de «uniformemente inválido»; excluye rayos y segmentos acotados, intersecciones recta–cónica y cónica–cónica, expresiones generales y macros | `ConstructionIntervalModel2D.java`; especificación §9 |
| Varios consumidores son más estrictos que la tabla §11 de la especificación porque leen el accesor solo-completo `getValidDomainComponents()`: extremos métricos de generador y de intersección, `Point(L,u)`, creación de puntos, rutas periódicas y costuras | estudio de `CU0` §1.9–§1.10 |
| La identidad de componente incluye los bits exactos de sus extremos | `LocusComponentLineage2D.java:21-31` |

Consecuencia práctica: retirar el certificado de compatibilidad sin certificación nueva
dejaría sin dibujo, métrica, puntos ni exportación a todos los loci dependientes
ordinarios, y rompería la cadena `o_1 → LocusLength → d_2 → e_2`, porque la ocurrencia de
generador del resolvedor métrico exige `COMPLETE`.

## 3. Arquitectura propuesta: cuatro niveles de evidencia

| Nivel | Nombre | Significado | Productor | Consumidores admitidos | Nunca |
|---|---|---|---|---|---|
| 1 | `POINT_VALID` | `F(t)` se evalúa válidamente en un parámetro | evaluador (existe) | puntos exactos, verificación de residuos | implica nada sobre un intervalo |
| 2 | `EXPLORATORY` (nuevo, tipado) | tramos válidos muestreados, transiciones localizadas por bisección sobre la validez, sospechas de salto, con método y trabajo registrados | explorador determinista acotado | diagnóstico, inspector, sembrado de certificadores; dibujo solo con marca explícita si el autor lo decide (decisión E-1) | se promueve a componente, a completitud ni a identidad |
| 3 | `LOCAL_CERTIFIED` | intervalo con existencia, continuidad de la misma rama y evidencia de frontera; maximal o subintervalo local | certificadores de §4 | todos los consumidores de §11 de forma local: dibujo, métrica de componente, puntos exactos, intersecciones locales, DXF con anexo | se declara completo |
| 4 | `COMPLETE` | la evidencia certificada cubre todo el dominio canónico, componentes maximales y finitud probada | ensamblador común | métrica total, rutas periódicas, «todas las raíces», cierre periódico | se obtiene parando un muestreador |

El nivel 2 es una estructura hermana de `LocusExistenceStructure2D`, no contenido de
`Coverage` ni de `ContinuousComponent`; así se respeta el ADR 0027, que rechaza construir
componentes a partir de muestreo. El cambio requiere enmienda de la especificación y un
ADR (ver [enmiendas candidatas](pre_g9b_cu0_candidate_normative_amendments.md)).

## 4. Fuentes de certificación (ampliación mínima)

Se siguen las autoridades de frontera de `locus-v2-existence-components.md` §7, en su
orden.

### 4.1 Evidencia estructural (autoridad 1)

1. **Totalidad estructural de operaciones.** Una operación es *total* sobre su dominio
   de entrada si está definida y es continua para toda entrada válida de su tipo:
   traslación por vector, vector unitario de una recta definida, paralela y
   perpendicular por un punto, intersección de una recta con su perpendicular,
   punto medio, homotecia y giro con parámetros constantes, y las herramientas afines
   del autor (`pointJump`, `translationCoor`). Si todas las operaciones de un corte
   son totales y sus entradas dependientes del conductor están certificadas en un
   intervalo, la salida está certificada en ese intervalo sin cálculo numérico. Se
   publica con una tabla versionada de operaciones totales.
2. **Periodicidad estructural.** Si el corte depende del conductor solo a través del
   punto conductor (no de `t` directamente) y todas sus selecciones de rama son
   funciones de la configuración actual, la continuidad en la costura del conductor
   es estructural: `F(−π) = F(π)` porque el punto conductor es el mismo. Sustituye a la
   inferencia actual «el proveedor es periódico».
3. **Eventos estructurales de dependencias.** Por ejemplo el evento de envoltura del
   resultado métrico (`wrapEvent`, [análisis D](pre_g9b_cu0_oriented_length_analysis.md)
   §7): frontera exacta, sin raíz numérica.

### 4.2 Programa de construcción, clase v3 (autoridad 4)

El modelo de intervalos existente se amplía, con un identificador de versión nuevo y sin
reetiquetar programas antiguos (especificación §8):

| Ampliación | Contenido |
|---|---|
| Tercer resultado por caja | `UNIFORMLY_INVALID` con razón tipada (discriminante con certeza negativo, divisor con certeza nulo en todo el dominio de la caja, punto en el infinito), distinto de `UNRESOLVED`. Permite publicar `CERTIFIED_DOES_NOT_EXIST` y, por tanto, fronteras maximales |
| Rayos y segmentos | intersección acotada como recta más predicado de pertenencia uniforme (`λ ≥ 0`, `0 ≤ λ ≤ 1`) decidido por intervalos |
| Circunferencias | centro–radio constante, centro–punto (radio dependiente del conductor) |
| Intersección recta–circunferencia | fórmula cerrada con la rama de raíz cuadrada elegida por la permutación que el algoritmo fija en modo no continuo (`AlgoIntersectLineConic`: «non-continuous: use computeContinuous() to init a permutation and then always use this permutation», líneas 268-274). La permutación se captura con el programa y el modelo reproduce la misma raíz computacional. En modo continuo la selección depende del historial y queda fuera de la clase |
| Productos escalares y vectores escalados por magnitudes dependientes | necesarios para `translationCoor` |
| Herramientas de usuario | expansión de la macro en sus pasos internos cuando todos pertenecen a la clase |
| Segunda derivada | chorros de segundo orden para fronteras de pliegue y tangencias (propuesta F) |

El modelo de dibujo y el de exportación no cambian: consumen las componentes que
publique el certificador.

### 4.3 Transiciones entre regiones válidas e inválidas (autoridades 4 y 5)

En los loci de geometría descriptiva el dominio válido termina donde una construcción
auxiliar se vuelve tangente: el discriminante `D(t)` de una intersección recta–cónica
cambia de signo. La propuesta:

1. **Predicado de validez explícito** de cada operación condicional (`D(t) ≥ 0`,
   `λ(t) ≥ 0`, `‖v(t)‖ > 0`), evaluado por intervalos.
2. **Frontera certificada como raíz simple del predicado**: cambio de signo de `D` en un
   corchete más una envolvente de `D'` que excluye el cero (Newton por intervalos o
   Krawczyk escalar, ya disponibles para splines) ⇒ transición única en un corchete de
   anchura `δ`. `D` es suave aunque `F` tenga una singularidad de raíz cuadrada en la
   frontera, que es lo que impide hoy dibujar hasta el extremo.
3. **Inclusión del extremo por el predicado**: en una tangencia recta–circunferencia
   (`D = 0`) la raíz doble existe, el extremo es cerrado y las dos ramas (`Q` y `R`)
   coinciden; donde se anula un divisor, el extremo es abierto.
4. **Componente maximal** si los dos lados de cada frontera están certificados (existe /
   no existe) y la frontera es única; si no, `LOCAL_CERTIFIED_SUBINTERVAL` con la banda
   de transición `UNRESOLVED` explícita.

### 4.4 Extremos incluidos y certificación unilateral

En un extremo incluido del dominio (segmento, arco, dominio escalar cerrado) la
certificación es unilateral: basta la evidencia del lado interior. Para tangencias en un
extremo, ver la propuesta F §5.

### 4.5 Presupuestos, determinismo y coste

La certificación es determinista: particiones dependientes solo del dominio y de la
política versionada, presupuestos explícitos y agotamiento como `UNRESOLVED` tipado,
nunca como certificado. Los puntos de partición son material del certificado, no
identidad. El coste depende de `CU1`: hoy cada evaluación puntual reconstruye el corte
aislado completo; una certificación por intervalos captura el programa una vez por
revisión.

## 5. Aplicación al modelo del autor

| Locus | Operaciones del corte | Evidencia propuesta | Resultado esperado |
|---|---|---|---|
| `o_1`, `e_1` | conductor `E_1` en `p`; rayo `I E_1` desde un punto exterior; paralela vertical; intersecciones recta–recta; circunferencia de centro `I` por `E_1`; `Intersect(d_1, i, 1)` (circunferencia centrada en la recta: dos raíces siempre distintas); rayo `H I_1`; `Intersect(c_1, s_1, 1)` (rayo desde el centro: una raíz siempre); horizontal por `G_1`; intersecciones de rayos con rectas no paralelas | clase v3 + periodicidad estructural; todas las cajas válidas | `COMPLETE`, una componente periódica `[−π, π)` probada, no supuesta |
| `e_2` | herramientas afines totales sobre `J_1`, `O_1`, `Q_1`; `d_2` de `w_1` con `wrapEvent` | totalidad estructural + evento estructural | `COMPLETE`, una componente abierta `[t_N, t_N + 2π)` |
| `o`, `v`, `w`, `a_1` | rayo `P N` ∩ circunferencia de base `p` (dos salidas `Q`, `R`); rayos y rectas | clase v3 + transiciones del discriminante del rayo `P N` con `p` | para `excCyl ≤ 3,5`: una componente periódica; para `3,7 ≤ excCyl ≤ 3,9`: componentes cerradas cuyos extremos son las transiciones (en la línea base `t = 1,04182` y `t = 1,51171` con `excCyl = 3,7`); `o` y `v` comparten esos extremos, como `w` y `a_1`, porque `Q = R` en la tangencia |

La línea base ya registra los extremos localizados numéricamente (`preserve.branch_ends`)
y la coincidencia de los extremos de `o` y `v`: son referencias para validar el
certificador, no certificados.

Para `excCyl ≥ 4,0` (H01) la corrección es independiente de la certificación: la
publicación debe depender de las entradas de la definición, no de que el trazador exista
en la posición actual del conductor. El locus seguirá definido y su existencia la dirá
el certificado.

## 6. Consumidores

Con el nivel 3 publicado, cada consumidor se alinea con la tabla §11 vigente:

| Consumidor | Cambio necesario |
|---|---|
| Dibujo | dibujar cada componente certificada hasta su frontera; no cerrar salvo componente periódica completa probada; no unir a través de bandas `UNRESOLVED` |
| Métrica entre posiciones y extremos de generador | aceptar una dirección contenida en una componente certificada (hoy exigen `COMPLETE`) |
| Métrica total, rutas periódicas, `full-cycle`, `wrap-to-start` | siguen exigiendo `COMPLETE` |
| `Point(L,u)` principal y creación de puntos | operar sobre componentes certificadas; distinguir «dirección válida por evaluación» de «dirección en componente certificada» (decisión E-2) |
| Intersecciones | sin cambio de regla: raíces locales en componentes certificadas; sin afirmación de vacío |
| DXF y LaTeX | componentes locales con anexo obligatorio; cierre solo con componente periódica completa |

## 7. Identidad y linaje de componentes

Hoy cualquier movimiento de una frontera certificada cambia la identidad de las raíces y
puntos de esa componente. Para componentes cuya frontera procede de un predicado
estructural (transición del discriminante de una operación identificada), se propone un
linaje estable por **fuente estructural de la frontera** (operación, predicado, lado),
según prevé `locus-v2-existence-components.md` §8 («explicit provider/certifier
transition»), y no por los bits de los extremos. Los objetos guardados del autor no se
ven afectados: `text2` referencia la componente `[−π, π)` de `o_1`, que se conserva si
`o_1` queda certificada `COMPLETE` (decisión E-3 sobre la migración de selectores
existentes).

## 8. Riesgos

| Riesgo | Mitigación |
|---|---|
| Muchas pruebas actuales dependen del certificado de compatibilidad (estudio de `CU0`, escenario S1/S1′) | introducir la certificación antes de retirar la compatibilidad; actualizar las fijaciones con trazabilidad |
| Clase v3 insuficiente para algún modelo | fallo cerrado con `NOT_ESTABLISHED`, nivel 2 visible para el usuario y diagnóstico explícito |
| Selección recta–cónica en modo continuo | fuera de la clase; parada si exige rediseñar la identidad de raíces (especificación §14) |
| Coste | después de `CU1`; programa capturado una vez por revisión |
| Cambio de identidad de componentes | linaje estructural (§7) y migración explícita |

## 9. Qué debe aprobarse antes de `CU2`

1. Los cuatro niveles y el tipo `EXPLORATORY` (enmienda de existencia + ADR nuevo).
2. La clase de programa v3 y el resultado `UNIFORMLY_INVALID`.
3. La totalidad y la periodicidad estructurales como evidencia de autoridad 1.
4. El evento estructural métrico (`wrapEvent`).
5. La regla de publicación independiente de la posición actual del conductor (H01).
6. Las adaptaciones de consumidores del §6 y el linaje del §7.

El texto candidato está en las
[enmiendas normativas candidatas](pre_g9b_cu0_candidate_normative_amendments.md) y las
decisiones abiertas (E-1 a E-3) en la
[hoja de decisiones](../validation/pre_g9b_cu0_author_decision_sheet.md).
