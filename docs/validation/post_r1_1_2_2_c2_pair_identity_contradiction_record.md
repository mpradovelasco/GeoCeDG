# GeoCeDG 1.2.2 — paso C2 detenido: la decisión DN-1 no resuelve la asociación que debía resolver

| Campo | Valor |
|---|---|
| Carácter | **Registro de una contradicción encontrada al ejecutar C2, con alternativas para decisión del autor.** No modifica ningún contrato y no aprueba nada. `selfApproved=false` |
| Regla aplicada | Autorización de 2026-11-22: «Si aparece una contradicción que requiera modificar otras decisiones normativas, detén únicamente el incremento afectado y presenta el problema con sus alternativas para decisión del autor» |
| Paso afectado | **C2** (DN-1, linaje estructural de componente en el selector de pares). Los demás pasos no dependen de él y continúan |
| Estado de la rama | `maintenance/geocedg-1.2.2-post-r1` contiene C0 y C1 y **no** contiene C2 |
| Trabajo conservado | Rama local `wip/post-3b-c2-structural-pair-lineage`, commit `3bcdf3241`: C2 implementado según DN-1, sin verificar y sin integrar |
| Corrige | El [informe de revisión](post_r1_1_2_2_candidate_review_report.md), §0.1 fila B1 y §1: su caracterización del defecto era incorrecta en un punto esencial (§2 de este registro) |

## 1. Qué se ha medido

Sondas fuera del repositorio, sobre la base `9ccefdcfe` y sobre C2 implementado según DN-1.
Fuente `LA`: lugar afín con un dominio declarado de dos piezas. Fuente `SP`: una curva
`SplineV2` que corta la recta `y = 1` tres veces (subiendo en 0,174, bajando, subiendo en
2,826). `X` es un punto creado sobre la raíz de 0,174.

| Escenario | Base (1.2.1) | C2 según DN-1 (linaje E-3) |
|---|---|---|
| **A. Deslizadores.** La pieza declarada en segundo lugar, que contiene la raíz, salta a `[2,3; 2,95]`; la primera ocupa su sitio | `X` pasa a 2,826 | `X` pasa a 2,826 |
| **B. Redefinición de la lista.** De `{[-2; -1,6], [0,1; 0,9]}` a `{[0,1; 0,9], [2,3; 2,95]}`: la pieza de la raíz sigue en la lista, sin cambio | `X` pasa a 2,826 | `X` pasa a 2,826 |
| Dos fuentes de dos piezas con la misma clave de rama | falla entera | resuelto por C1 |
| Documento de la base con puntos sobre raíces de par, abierto con C2 | — | token conservado, punto en su raíz, libro en versión 8 |

En el escenario B la raíz de 0,174 no se mueve, su pieza `[0,1; 0,9]` no cambia y sigue
declarada, y aun así `X` acaba sobre otra raíz. Es la asociación incorrecta que el autor
pidió resolver, y C2 según DN-1 **no la resuelve**.

## 2. Por qué, y qué estaba mal en mi informe

1. **El linaje E-3 de una pieza de dominio declarado es posicional por el tipo de sus
   extremos.** `LocusComponentLineage2D` nombra una componente por las fuentes de sus dos
   extremos. En un dominio declarado, la primera pieza es «extremo inferior del dominio →
   extremo de componente declarada», la última es «extremo de componente declarada → extremo
   superior del dominio», y las intermedias comparten las dos fuentes y caen en la clave por
   extremos numéricos. Con dos piezas, eso coincide exactamente con el ordinal. E-3 se diseñó
   para componentes delimitadas por transiciones certificadas, donde las fuentes de los
   extremos sí distinguen; para una lista declarada no identifica el elemento declarado.
2. **Las fuentes de varias componentes que tienen modelo D2 son, en lo que he podido
   comprobar, solo las de dominio declarado.** Un lugar con arcos derivados del certificado
   (intersección recta-cónica) no tiene «certified interval curve model» y sus pares quedan
   solo como resultado rico, sin token. Por tanto, en el espacio alcanzable, ordinal y linaje
   E-3 nunca difieren para dos piezas.
3. **El escenario A no es una asociación incorrecta bajo la semántica aprobada de D2.** El
   token nombra «la única raíz de esa clase de germen en esa componente», y la reactivación
   de la ranura no afirma continuidad física (ADR 0028). Si la pieza se desplaza, su raíz es
   otra. En el informe lo presenté como defecto reproducido; era la semántica de ranura.
   **Esa caracterización era incorrecta.**
4. **El escenario B sí es un defecto**, y lo es tanto con ordinales como con E-3: al
   redefinir la lista, el elemento declarado que contiene la raíz cambia de posición y el
   token queda ligado a la posición.
5. Lo que sí era cierto: el selector D2 cita `component-<ordinal>` contra
   `spline-v2-pair-materialization.md` §2, y la colisión de claves (corregida en C1).

## 3. Qué haría falta, y por qué excede DN-1

Para que `X` no pase a otra raíz en el escenario B, la identidad de una pieza declarada tiene
que ser la del **elemento declarado**, no la de su posición. Eso no es el linaje E-3: exige
una regla nueva, y la misma limitación existe hoy en los tokens de una sola fuente sobre
piezas declaradas, que usan E-3 desde CU2. Es una decisión normativa distinta de DN-1.

## 4. Alternativas

| # | Alternativa | Escenario A | Escenario B | Coste y riesgo | Norma |
|---|---|---|---|---|---|
| α | **Identidad declarativa de pieza**: el linaje de una pieza declarada es el texto canónico de la declaración de sus extremos (sus expresiones con identificadores duraderos, o sus literales) | Sin cambio: la pieza se mueve y su raíz es la de su ranura | **Resuelto**: la pieza de la raíz conserva su identidad | Medio. Regla nueva; hay que decidir si alcanza también a los tokens de una sola fuente; adopción de entradas heredadas como en C2 | Enmienda de `locus-v2-existence-components.md` §15.6 y de `spline-v2-pair-materialization.md` |
| β | **Clave por extremos numéricos** de la pieza declarada en los selectores de pares | `X` queda sin definir al moverse la pieza y vuelve si la pieza vuelve | **Resuelto** mientras la pieza conserve sus extremos | Bajo. Pero un punto sobre una pieza gobernada por deslizadores queda sin definir en cuanto la pieza se mueve: se pierde persistencia dinámica | Enmienda de `spline-v2-pair-materialization.md`; la clave usa valores de parámetro declarados |
| γ | **C2 tal como está en la rama `wip`** (E-3) | Sin cambio | **Sin cambio** | Ya implementado. Solo aporta conformidad textual del selector; introduce libro versión 8, que 1.2.1 no lee | DN-1, ya aprobada |
| δ | **No hacer C2 en 1.2.2**: C1 resuelve la colisión; el escenario B se documenta como limitación conocida | Sin cambio | Sin cambio | Nulo | Ninguna |

Mi recomendación es **α**, decidida junto con su alcance (solo pares, o también una sola
fuente), porque es la única que da al elemento declarado una identidad semántica sin recurrir
a posición ni a valores. Si el autor prefiere no abrir esa regla en 1.2.2, recomiendo **δ**
antes que γ: γ cambia el formato persistido sin corregir el comportamiento.

No recomiendo cambiar la semántica de ranura de D2 (escenario A): es una decisión aprobada
del ADR 0028 y no es objeto de este encargo.

## 5. Qué no cambia

- C0 y C1 están confirmados y verificados por separado; C1 corrige la pérdida del resultado
  con varias componentes por fuente.
- C3, C4, C5 y C6 no dependen de C2 y continúan.
- C7, el candidato sucesor, necesita la decisión del autor sobre C2 para poder afirmar que
  no contiene el defecto de identidad del escenario B.
