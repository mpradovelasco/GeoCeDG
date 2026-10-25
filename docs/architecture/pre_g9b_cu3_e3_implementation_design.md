# PRE-G9B-CU3 — Diseño de implementación del incremento E3 bajo la autoridad constructiva

| Campo | Valor |
|---|---|
| Carácter | Diseño de implementación del incremento E3 (`AGENTS.md` §9). No es contrato: la autoridad normativa es la enmienda 1.4 del [contrato de intersecciones](../../geocedg/specs/locus/locus-v2-intersections.md) (CU3.2 a CU3.4 bajo la política constructiva), [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) 1.0 y el [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md). Ante cualquier diferencia prevalecen ellos |
| Autorización | [registro de aprobación](../validation/pre_g9b_cu3_constructive_authority_approval_record.md) §5 a §10 y D-AUT-11 (D-CU3-10: reclasificación de la costura por paridad bajo la autoridad constructiva; D-CU3-13: certificado de paridad y configuración `Σ_C`); [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §6.2 (E3); [estrategia](pre_g9b_cu3_implementation_strategy.md) §2 (puertas G-A y G-B) |
| Base | E2 en `91222e312fed9756225407be6f0c72a1eafeac6e`, con sus puertas registradas en `5a1ac888ebcd88006ccfb94ff51bf97b34b8c92d` ([registro de E2](../validation/pre_g9b_cu3_e2_gate_record.md)) |
| `selfApproved` | `false` |

## 1. Alcance de E3

| Pieza | Contenido | Filas | Puerta |
|---|---|---|---|
| E3.1 | forma «una raíz doble en un punto exacto» del certificado de conteo en `Σ_C`, con el certificado de identidad constructiva en el parámetro exacto y el pliegue de `CU2` por simetría exacta (certificado de paridad); reclasificación de la costura; informe de impacto de la adopción con la regla de conflicto | V-B-2, V-CONF-5, V-B-NOISE-1, V-AUT-ADOPT-8 | G-A |
| E3.2 | inversión de `pins.B08_B09` en el modelo adoptado; `preserve.contacts` igual | §3.9 de la matriz | G-A |
| E3.3 | `CU3-B`: germen de contacto tangente, ciclo de vida y materialización sin conductor de las clases 1 y 2 | V-B-7 a V-B-10, V-B-12, V-B-1 (clase 2) | G-B |

La regla de conflicto solo cambia bajo `constructive-authority/v1`. Bajo la política heredada,
CU3.4 rige sin cambios y nada de E3 se aplica: la configuración capturada no da ninguna decisión
exacta y el disparador nuevo solo actúa en `Σ_C`.

## 2. E3.1 — Raíz doble en un punto exacto

CU3.4 admite cuatro formas de certificado de conteo. E1 implementó las tres de intervalos; la
cuarta, «una raíz doble en un punto exacto» (Etapa B §9.1), exige una decisión exacta o
estructural, y la enmienda 1.4 la admite en `Σ_C` junto con «un certificado de paridad por
simetría exacta».

**Parámetros exactos.** Un conductor sobre una circunferencia del marco de los ejes, con la
parametrización capturada `m + r (cos t e₀ + sin t e₁)`, tiene los parámetros exactos
`k π/2`. En ellos el punto del conductor es exacto en el cuerpo del certificado
(`m + r (c e₀ + s e₁)` con `(c, s) ∈ {(1, 0), (0, 1), (−1, 0), (0, −1)}` y `r = √(r²)`, la raíz
no negativa del radio al cuadrado exacto), y en ellos decide `CU2` los pliegues de
`fold-symmetry/v1`. E3 no inventa ningún otro parámetro. Cada copia `k π/2` tiene un encierro
binary64 hacia fuera exacto (producto racional exacto de `k` por los dos binary64 que encierran
`π/2`), nunca un desplazamiento binary64 de otra copia.

**Evidencia exacta en `Σ_C`** (`ResidualJetSource2D.exactContacts`, implementada por
`ConstructiveResidualJetSource`):

1. el certificado de identidad constructiva de la incidencia del punto trazado con el objetivo
   (`ConstructiveIdentityCertifier.incidenceAtParameter`), con el conductor en el punto exacto del
   parámetro y sus jets de orden dos: el residuo es el elemento cero (`h(t₀) = 0`) y, con los
   jets exactos, `h′(t₀)` también (`h′(t₀) = 0`), por `F′(t₀) = 0` en un punto estacionario o por
   tangencia en una rama regular; es clase 2, `CONTACT_EXISTENCE_EXACT`, contra el objetivo
   constructivo. La regla de frontera, la caja, la celda, la correspondencia de ramas con el
   algoritmo vivo, el presupuesto y la clave estructural son los de E2; el nodo de reemplazo del
   cono (el conductor del locus) ni se evalúa ni se lee, así que su posición persistida no entra en
   la clave;
2. en un punto estacionario, el pliegue de `CU2` en ese parámetro, transferido por estructura
   (`Cu2CertificateTransfer.foldAtParameter`, la ruta de E1.10 con el parámetro exacto dado en
   lugar del de un punto del documento): la simetría exacta `F(t₀ + s) = F(t₀ − s)` es el
   certificado de paridad, y el orden es `FOLD_CONTACT`; sin transferencia, el orden no se
   establece y el conteo sigue valiendo, porque `h′(t₀) = 0` ya es exacto;
3. un certificado que no da la identidad, una tangencia de orden superior (`h″(t₀) = 0`), un
   contacto transversal o un conductor sobre un segmento no dan evidencia: nada cambia.

El certificado se recomprueba sin trabajo simbólico en las revisiones siguientes mientras se
mantienen su clave y su celda (`ExactContactMemory`, en la configuración de decisión de la
intersección); la decisión de un mismo estado del cono se reutiliza tal cual.

**Certificado de conteo** (`RootCountCertificate2D.Form.ONE_DOUBLE`). En una caja `T` de la
componente, con `h″` de signo fijo en `T` y un punto crítico certificado cuyo valor contiene el
cero (la clase 3 de E1), la evidencia exacta en un parámetro cuyo encierro cae en el corchete del
punto crítico da `N = 1` doble en `t₀`: por el teorema de Taylor, `h` tiene el signo de `h″` en el
resto de `T`. Un signo exacto de `h″(t₀)` que contradiga el del encierro no certifica nada. Las
cajas son las de E1.

**Disparador.** Determinista e independiente de la escala: bajo `constructive-authority/v1`,
además de los disparadores de E1, un candidato cuyo intervalo aislante encuentra un pliegue
certificado de `CU2`. La raíz transversal de la costura de `e_1·c_1` dispara así por el pliegue de
`π`.

**Efecto** (Etapa B §9.2, contradicciones de existencia, conteo y multiplicidad):

- los candidatos de `T` cuyo intervalo no contiene `t₀` se retiran
  (`CONTRADICTED_BY_CERTIFIED_EVIDENCE`);
- de los que lo contienen, el más cercano a `t₀` se queda y los demás se fusionan en él;
- el que queda se sustituye por un candidato nuevo en `t₀` (su propio parámetro si cae en el
  encierro), tangente, de multiplicidad dos establecida, grado `EXISTENCE_EXACT`, evidencia
  `CONTACT_EXISTENCE_EXACT` y su orden de contacto; su aislamiento sigue `NOT_ESTABLISHED`, como
  en todo contacto de G8B-R1, así que nunca recibe germen transversal: el token transversal de una
  raíz reclasificada deja de ser admisible y no se reasocia;
- si ningún binary64 de `t₀` pasa la comprobación de residuo de la verificación, los candidatos que
  contienen `t₀` quedan como estaban (fallo cerrado), como en el caso simple de E1.

**Orden de contacto** (`ContactOrder`: `ONE`, `TWO`, `FOLD_CONTACT`, `NOT_ESTABLISHED`, Etapa B
§4.4). Es un atributo de la evidencia de existencia de la revisión, como el grado: nunca entra en
un token. La regla de conflicto lo registra en las raíces que reclasifica (`ONE` en la
reclasificación al alza de E1).

**Informe de impacto de la adopción.** La regla de conflicto de `Σ_C` puede ahora retirar,
fusionar o reclasificar una raíz que G8B-R1 admitía. El informe aplica la regla a todas las
soluciones de cada resultado (`ConstructiveRequalification2D.ofResult`) y da esas raíces como no
admitidas, con la causa `CONTRADICTED_BY_CERTIFIED_EVIDENCE` (V-AUT-ADOPT-8).

**Sin paridad aislada.** Sin la identidad, el certificado de paridad solo dice que en una caja
simétrica respecto del pliegue el número de raíces es par: no certifica ningún conteo ni contradice
la existencia o la multiplicidad de una raíz publicada cuyo intervalo aislante no es simétrico (la
raíz de la costura está en el extremo del dominio). Queda la contradicción solo del conjunto (la
completitud pasa a `NOT_ESTABLISHED`), que la clase 3 ya produce. La reclasificación de la costura
exige, pues, la identidad en `π` y la paridad juntas.

## 3. E3.2 — `pins.B08_B09`

La referencia de `CU0` (`PreG9BCu0LocusReferenceTest`) mide el modelo bajo la política heredada,
donde nada cambia, y conserva el pin como evidencia histórica. La inversión es una prueba de
aceptación propia sobre el modelo adoptado, con el patrón de H02 y H06
(`PreG9BCu0Baseline.inversionMessage(incidence, "CU3", ...)`; la línea base JSON no cambia):

- la raíz de la costura de `Intersect(e_1, c_1)` deja de ser `TRANSVERSE_ESTABLISHED` admisible:
  es el contacto en el pliegue de `π`, tangente, clase 2, `FOLD_CONTACT`, sin token admisible;
- los contactos de `t = 0` de `Intersect(e_1, n)`, `Intersect(e_1, c_1)` y `u_1 =
  Intersect(o_1, h_2)` son clase 2 (`FOLD_CONTACT`, `FOLD_CONTACT` y `TWO`), sin materializar;
- `preserve.contacts`: los mismos puntos con la tolerancia de `CU0`.

## 4. E3.3 — `CU3-B`

**No se implementa en este candidato.** La estrategia (§2) prevé que, si G-B no se supera, el
candidato se congele con `CU3-A` solo y `CU3-B` se presente como pendiente sin rebajar ninguna
evidencia. El estudio del código de identidad, hecho tras E3.1 y sin cambiar nada, encontró tres
puntos que deciden más que la ingeniería:

1. **Pliegue que se desdobla (V-B-9, D-CU3-11).** Un contacto en un pliegue que pasa a dos
   preimágenes `t₀ ± s` de un único punto debe conservar una sola identidad. Hoy esas dos
   preimágenes son dos raíces transversales, cada una con su germen y su token. Darles una sola
   identidad cambia el comportamiento de tokens transversales que ya existen cerca de los
   pliegues: es la condición de parada «riesgo de reasociación o pérdida de identidad».
2. **Dos contactos con el mismo germen.** En `e_1·c_1` el contacto de `t = 0` y el de la costura
   tienen el mismo linaje, el mismo objetivo, el mismo orden (`FOLD_CONTACT`), la misma clase y el
   mismo lado (`h″ < 0`): el germen aprobado (Etapa B §6.1) es igual. El desempate por rango de
   fase (ADR 0017) rechaza la costura, así que los dos fallarían cerrados. Materializarlos exige
   una regla de fase para el punto fijo de un pliegue que el contrato de G9U0-R4 no tiene.
3. **Evidencia dentro del token.** El germen aprobado contiene la clase de evidencia y el orden.
   El token deriva del germen, así que esa evidencia entraría en la identidad, y un paso de clase 1
   a clase 2 daría un token nuevo. La regla de que la evidencia de la revisión nunca entra en un
   token (V-EVID-3) pide una decisión autoral sobre esa composición.

Puntos técnicos que cualquier implementación debe respetar, también registrados: el libro de
tokens vuelve a leer el selector en cada instantánea, así que un selector de contacto debe
reconstruirse exactamente; un contacto aislado nunca debe heredar por la regla de enlace del único
token de su componente (`allowLegacySingletonBinding`), que lo reasociaría con el token de la raíz
transversal; ningún texto, prefijo ni marco de los gérmenes, selectores y tokens transversales
cambia; y la política del documento se vuelve a leer en cada cálculo (la adopción y Deshacer la
cambian).

En este candidato los contactos de clase 2 quedan clasificados y visibles en el resultado rico
(E3.1), sin token admisible: se crean con `Intersect(L, r, K)` e `Intersect(L, r, J)` (E1 y E2),
nunca sin conductor.

## 5. Validación y condiciones de parada

- **Pruebas nuevas.** `PreG9BCu3ExactDoubleRootTest` (la forma y la regla, con una fuente que
  declara una decisión exacta); `ConstructiveExactContactTest` (réplica: costura, contactos de
  `t = 0`, informe, Deshacer, presupuesto agotado, objetivo que no contiene `F(π)`,
  recomprobación); la prueba de E1 de los contactos en pliegue, ajustada a la clase 2; la
  inversión de `pins.B08_B09` y V-B-NOISE-1 en el modelo del autor (escritorio).
- **Regresiones.** `PHASE` de `CU0`, `CU1` y `CU2`; suites G8B, G8C1, G8C2 y G9U0-R4 sin cambios
  (política heredada); `INTEGRATION`.
- **Paradas.** Las de la estrategia §2 y las del prompt: en particular, ninguna tolerancia nueva,
  ninguna decisión por proximidad o por residuo, ningún cambio de token ni de selector bajo la
  política heredada, y ninguna reasociación.
