# PRE-G9B-CU0 — Longitud orientada de `e_2`: análisis matemático y arquitectónico (entregable D)

| Campo | Valor |
|---|---|
| Carácter | Diseño matemático preliminar del paquete `CU0`. No es especificación normativa, no modifica la semántica vigente de `LocusLength` y no autoriza implementación |
| Estado | `CU0 DESIGN CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU — Author Plan Approval, Architectural Refinements and CU0 Authorization» (2026-10-19), §3.1 |
| Evidencia | [línea base geométrica CU0](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json) (generada dos veces de forma independiente con resultado idéntico); [informe de caracterización](../validation/pre_g9b_cu_characterization_evidence_report.md); [informe del candidato CU0](../validation/pre_g9b_cu0_baseline_candidate_report.md) |
| Autoridad aplicable | `geocedg/specs/locus/locus-v2-metrics.md` §2, §6, §7, §16, §22 y §24.5; `locus-v2-existence-components.md`; ADR 0027; `AGENTS.md` §11 |

## 1. Construcción analizada

La cadena del modelo del autor es:

```text
E_1  conductor en la circunferencia p (dominio canónico [-π, π), periódico)
K_1  = Intersect(b_1, n_1)            punto trazado de o_1
o_1  = LocusV2(K_1, E_1)              proyección horizontal de la curva esférica
N_1  = Point(o_1, text2, v_1), v_1=0  origen semántico sobre o_1
w_1  = LocusLength(o_1, N_1, K_1)     forma por defecto: forward / strict / zero-length
d_2  = Length(w_1)
Q_1  = pointJump(L_1, g_2, d_2)       Q_1 = L_1 + d_2·u(g_2)
R_1  = translationCoor(J_1, O_1, k, Q_1, j_2)
                                      R_1 = Q_1 + z(J_1)·u(j_2), con z la altura
                                      con signo de J_1 respecto de O_1 medida según k
e_2  = LocusV2(R_1, E_1)
```

Las dos herramientas de usuario son afines y totales (el modelo las define como
`B = A + Avance·UnitVector(f)` y `I = F + ((E - C)·UnitVector(f))·UnitVector(i)`, con
`E` pie de la perpendicular). Por tanto, para el parámetro `t` del conductor,

```text
e_2(t) = L_1 + w_1(t)·u(g_2) + z(t)·u(j_2).
```

`e_2` es el desarrollo del cilindro proyectante vertical que pasa por la curva `o_1`:
abscisa = longitud semántica medida sobre `o_1` desde `N_1`; ordenada = altura del
punto espacial correspondiente. Es la construcción clásica de verdadera forma de una
curva sobre su cilindro proyectante.

## 2. Qué discontinuidad presenta realmente `w_1`

Sea `F(t)` la evaluación de `o_1` y `σ(t) = ‖F'(t)‖`. La línea base registra (valores
del producto sin cambios):

| Magnitud | Valor |
|---|---|
| Longitud total `Length(o_1)` | `L = 13,2364580874242` |
| `w_1(-10⁻³)`; `w_1(10⁻³)` | `13,23294585898`; `0,00351222845` |
| `‖e_2(-10⁻⁶) − e_2(10⁻⁶)‖` | `13,23645106296` (= `L` salvo `2·10⁻⁶·σ`) |
| `w_1(π − 10⁻³)`; `w_1(−π + 10⁻³)` | `6,61622998255`; `6,62022810491` |
| `‖e_2(−π) − e_2(π − 10⁻⁹)‖` | `2,0·10⁻⁹` (continuidad en la costura) |

Por la definición vigente de la ruta hacia delante sobre una rama periódica
(`locus-v2-metrics.md` §22: el recorrido opera sobre el dominio fundamental orientado
declarado y la dirección de costura canónica), con `t_N = 0` la dirección de `N_1`:

```text
w_1(t) = ∫_{t_N}^{t} σ(u) du                  si t ≥ t_N
w_1(t) = L − ∫_{t}^{t_N} σ(u) du              si t < t_N
```

Conclusiones:

1. `w_1` tiene **una sola discontinuidad**: un salto de amplitud exactamente `L` en
   `t = t_N`, donde el extremo móvil `K_1` pasa por el origen `N_1`.
2. `w_1` es **continua en la costura** `±π` del conductor.
3. Su derivada `dw_1/dt = σ(t)` es continua en todo el dominio, incluido `t_N`: la
   función es un diente de sierra, `w_1(t) = S(t) mod L`, con
   `S(t) = ∫_{t_N}^{t} σ(u) du` la longitud acumulada sin envolver.
4. En `t = t_N` la política `zero-length` da `w_1 = 0`; la política `full-cycle` daría
   `L`. El extremo incluido lo decide la política de misma posición, no un épsilon.

## 3. Clasificación según las cinco categorías del encargo

| Categoría | ¿Se da en `w_1` / `e_2`? | Justificación |
|---|---|---|
| 1. Discontinuidad geométrica real | **No** | La curva espacial es cerrada y continua; su desarrollo es un arco continuo |
| 2. Cambio de rama o de selección de recorrido | **Sí** | En `t_N` la ruta hacia delante pasa de «casi un ciclo completo» a «vacía»: cambia la ruta elegida, no la geometría |
| 3. Representación numérica de la longitud | **No** | El salto coincide con `L` dentro de `2·10⁻⁶·σ`; la evaluación es suave (defecto del punto medio ∝ h²) |
| 4. Exclusivamente gráfica | **En parte** | La «cuerda de cierre» visible (13,19 unidades) es un segmento de dibujo que une los dos extremos porque el certificado declara una componente periódica cerrada |
| 5. Corte necesario de un recorrido periódico | **Sí** | Una coordenada real que recorre una vez una curva cerrada debe cortarse en algún punto; aquí el corte está en el origen semántico elegido por el autor (`N_1`) |

La periodicidad del conductor (`E_1` da una vuelta) y la de `o_1` (curva cerrada) no
implican la de la magnitud derivada: `w_1` no es continua como función periódica y
`e_2` no es una curva cerrada. El productor dependiente actual hereda la periodicidad
del conductor sin prueba (certificado de compatibilidad, B07/B10.2): ese es el error de
representación.

## 4. Cómo se transmite a `e_2`

`e_2(t) = L_1 + w_1(t)·u(g_2) + z(t)·u(j_2)` hereda exactamente la discontinuidad de
`w_1`: en `t_N` el punto salta `L` a lo largo de `g_2` con la misma altura
(`e_2(0⁺) = (27,0434; 6,7250)`, `e_2(0⁻) = (13,8069; 6,7250)`), y es continuo en el
resto, incluida la costura `±π`. Como conjunto, la imagen de `e_2` sobre un ciclo del
conductor es **un único arco abierto** cuyos extremos están a la misma altura,
separados por el perímetro `L`, y representan el mismo punto espacial (la generatriz
de `N_1`). Es exactamente el resultado esperado de un desarrollo.

En el dominio canónico `[−π, π)` ese arco aparece partido en dos trozos,
`[−π, 0)` y `[0, π)`, que se unen de forma continua a través de la costura. En el
círculo del conductor `S¹` es una sola componente: `S¹ ∖ {t_N}`, o, en coordenada
elevada, el intervalo `[t_N, t_N + 2π)` con la política `zero-length`.

## 5. ¿Se obtiene la construcción prevista con un intervalo continuo de longitud?

**Sí, sin cambiar `LocusLength`.** La coordenada de longitud orientada sobre un
intervalo seleccionado que propone el encargo,

```text
s(u) = ∫_{u_0}^{u} ‖F'(t)‖ dt,     u ∈ [u_0, u_0 + 2π),
```

coincide con la función que ya calcula `LocusLength(o_1, N_1, K_1)` hacia delante
cuando `u_0 = t_N` y el extremo móvil es la ocurrencia de generador (`metrics` §24.5).
Sobre ese intervalo `s` es continua, estrictamente creciente (σ > 0) y va de `0` a
`L`. Lo que falta no es una nueva magnitud sino:

1. que el **dominio de trabajo de `e_2`** se corte en `t_N` y no en la costura del
   conductor;
2. que el productor de `e_2` **no declare** una componente periódica cerrada; y
3. que el dibujo **no una** los extremos.

## 6. Representaciones consideradas

| Representación | Valoración |
|---|---|
| Longitud orientada respecto a un origen semántico explícito | Ya existe: `N_1` es una posición semántica (`text2`, `v_1`). Mover el origen es cambiar `v_1` |
| Recorrido continuo entre dos posiciones identificadas semánticamente | Ya existe (ruta `LocusMetricRoute2D`). Válido mientras el destino no cruce el origen |
| Coordenada de longitud acumulada sobre un intervalo orientado | Es `s(u)` del §5. Es la representación recomendada para el dominio de `e_2` |
| Parámetro desenrollado (*lifted*) | Una longitud acumulada de más de una vuelta exige un número de vueltas como estado. Para un conductor periódico no es función de la configuración actual (dependería del historial de arrastre), lo que viola el determinismo exigido. Solo tendría sentido con un conductor no periódico explícito (p. ej. un deslizador `t ∈ [0, 4π]`), y entonces sería otra construcción. No se propone |
| Elección explícita del corte del dominio periódico | Sería una opción del usuario en `LocusV2`. Con el hecho estructural del §7 el corte se obtiene automáticamente y de forma exacta. Queda como alternativa si el autor quiere fijar cortes a mano |
| Continuidad respecto a cambios de parámetros del modelo | El corte depende solo de la dirección semántica de `N_1`; al cambiar `α_cone`, `rbCone` o `D_1` cambia `L` y la forma del desarrollo de modo continuo, y el corte permanece en `t_N` |
| Separación entre periodicidad del conductor y de la magnitud derivada | Imprescindible: la periodicidad de un locus dependiente debe establecerse, no heredarse del conductor (ADR 0027) |

**Recomendación:** representar `e_2` como **una componente abierta**
`[t_N, t_N + 2π)` (o `(t_N, t_N + 2π]` con `full-cycle`) que atraviesa la costura del
conductor, con frontera publicada por un **hecho estructural de la métrica**, sin
cambiar los valores de `w_1` ni de `e_2`.

## 7. Ampliación acotada necesaria

Las capacidades métricas existentes calculan el valor correcto. Hacen falta dos piezas
acotadas, ambas sin persistencia nueva:

1. **Hecho estructural de envoltura en el resultado métrico** (enmienda de
   `locus-v2-metrics.md`). Cuando el extremo destino es la ocurrencia de generador de
   la misma fuente (`metrics` §24.5, conductor vivo) y el origen es una posición
   semántica fija sobre una componente periódica, el resultado publica, como dato
   derivado de la revisión:

   ```text
   wrapEvent = { driverParameter: t_N (dirección canónica del origen),
                 jump: L (longitud del ciclo), sameSide: zero-length | full-cycle }
   ```

   El valor es exacto (es la dirección del origen, no una raíz numérica), depende solo
   de entradas del DAG y no se serializa.
2. **Consumo por el productor dependiente** (enmienda de
   `locus-v2-existence-components.md` §7, autoridad de frontera 1: evento estructural
   del proveedor o de una dependencia). Si el corte aislado de `e_2` contiene un
   resultado métrico con `wrapEvent` sobre el mismo conductor, la frontera de
   componente de `e_2` se sitúa en `t_N` con la política de extremo del §2.4, y la
   componente no es periódica cerrada.

Para otros orígenes (un destino que no sea ocurrencia de generador, por ejemplo una
intersección que se mueve con el conductor) la posición del salto no es estructural:
se localizaría como transición numérica (evidencia de nivel 2 o certificado de nivel 3
según la propuesta E) o quedaría `NOT_ESTABLISHED`. No se infiere de coordenadas.

## 8. Consecuencias para la topología de `e_2`

| Aspecto | Hoy | Tras la ampliación |
|---|---|---|
| Completitud | `COMPLETE` por compatibilidad, sin prueba | La que establezca la certificación de `o_1` y de la cadena (propuesta E). Con `o_1` certificada y el evento estructural, `COMPLETE` con una componente abierta |
| Componentes | una periódica `[−π, π)` | una abierta `[t_N, t_N + 2π)` que cruza la costura |
| Dibujo | polígono cerrado con cuerda de 13,19 | polilínea abierta desde `e_2(t_N⁺)` hasta `e_2(t_N⁻)` |
| Valores puntuales | correctos | idénticos (referencias `preserve.e_2` de la línea base) |
| Métricas de `e_2`, intersecciones, DXF | sobre una componente cerrada falsa | sobre la componente abierta; el DXF exporta un `LWPOLYLINE` abierto |

## 9. Identidad, revisiones y dependencias dinámicas

- El evento se deriva de entradas durables (`N_1` y su dirección; procedencia de
  ocurrencia de generador de `K_1`) en cada revisión. No usa coordenadas, proximidad ni
  orden.
- Mover `N_1` (cambiar `v_1`) cambia la revisión semántica de `w_1` y, por tanto, la
  de `e_2`: el corte se desplaza y el desarrollo se recalcula por el DAG normal.
- Arrastrar solo el conductor no cambia el evento ni la topología de `e_2`; tras `CU1`
  tampoco debe cambiar la revisión de `o_1` (H03).
- La identidad de componente de `e_2` cambia si cambia `t_N`. Hoy la identidad de
  componente incluye los extremos exactos (`LocusComponentLineage2D`); los objetos que
  dependan de direcciones sobre `e_2` necesitarían la relación de linaje explícita que
  ya prevé `locus-v2-existence-components.md` §8. En el modelo del autor ningún objeto
  depende de `e_2`.

## 10. Cómo sigue funcionando la cadena al retirar los certificados incorrectos

El encargo exige que `o_1 → LocusLength → d_2 → e_2` siga siendo semánticamente válida.
El estudio de certificación (propuesta E) establece que hoy la ocurrencia de generador
del resolvedor métrico exige `COMPLETE` sobre `o_1`. Si el productor dependiente pasa
a `NOT_ESTABLISHED` sin certificación nueva, `w_1` y `d_2` quedan indefinidos y `e_2`
desaparece. La secuencia segura es, por tanto:

1. certificar `o_1` con evidencia real (la cadena de `K_1` es total en todo el
   dominio del conductor: rayos desde puntos exteriores, intersecciones recta–recta no
   paralelas, una circunferencia centrada en el origen de su rayo; ver propuesta E §5);
2. publicar el evento estructural de `w_1`;
3. certificar `e_2` por totalidad estructural de las herramientas afines sobre
   entradas certificadas, con frontera en `t_N`;
4. solo entonces retirar el certificado de compatibilidad.

Si en algún paso la evidencia no basta, la longitud que alimenta `e_2` no se sustituye
por una aproximación gráfica: el resultado queda indefinido con diagnóstico explícito.

## 11. Riesgo métrico independiente detectado en CU0

El inventario de umbrales señala que la métrica de un `LocusV2` general se calcula por
refinamiento de polígono de cuerdas sin profundidad mínima, de modo que tres muestras
colineales podrían aceptarse con error cero, y que el presupuesto de 32 768
evaluaciones podría agotarse en circunferencias de radio moderado. El
[informe del candidato CU0](../validation/pre_g9b_cu0_baseline_candidate_report.md)
recoge la comprobación ejecutada (sonda `metric-aliasing`) y su resultado. Para el
modelo del autor, la longitud de `o_1` se ha contrastado de forma independiente con la
suma de cuerdas refinada (mismo informe).
