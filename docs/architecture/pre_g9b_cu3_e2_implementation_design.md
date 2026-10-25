# PRE-G9B-CU3 — Diseño de implementación del incremento E2 bajo la autoridad constructiva

| Campo | Valor |
|---|---|
| Carácter | Diseño de implementación del incremento E2 (`AGENTS.md` §9). No es contrato: la autoridad normativa es [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) 1.0, en particular §8 a §10, y el [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md). Ante cualquier diferencia prevalecen ellos |
| Autorización | [registro de aprobación](../validation/pre_g9b_cu3_constructive_authority_approval_record.md) §7, §9, §10 y §16 |
| Base | incremento E1 en `50ac91c29fb2835874d10c7e7afe56c3c349f79f`, con sus puertas registradas en `73c20c5f85a0bceb4f2f5d1651a373688e5b57f8` ([registro de las puertas de E1](../validation/pre_g9b_cu3_e1_gate_record.md)) |
| Prototipo de referencia | `PreG9BCu3ConstructiveIdentityProbe` (banco, nunca producto) y su [evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json) |
| `selfApproved` | `false` |

## 1. Alcance de E2

El certificado de identidad constructiva (CIC, `constructive-identity-certificate/v1`) pasa a ser
producto: decide `P ∈ r` en los cinco contactos con integridad `STRICT` cuando la construcción y
sus condiciones lo justifican, y retira la evidencia `RELAXED` cuando la relación no es una
identidad. Sus piezas:

| Pieza | Contenido | Filas de la matriz |
|---|---|---|
| E2.1 | álgebra exacta: polinomios de hasta 16 indeterminadas, funciones racionales, el cuerpo `Q(V)[√Δ]` con una raíz cuadrada, encierros por la forma de valor medio, presupuesto `constructive-identity-certificate-budget/v1` y contadores | V-AUT-CIC-6, G-AUT-8 |
| E2.2 | el certificador sobre el evaluador del cono de E1, la regla de frontera automática, la caja de decisión con los valores ideales, la clave `cic-structural-key/v1` y la recomprobación | V-AUT-CIC-1 a 5, V-AUT-ID-1 a 7 |
| E2.3 | orden de contacto por jets exactos de orden dos | V-MAND-1 a 4 (orden), G-AUT-2 |
| E2.4 | integración en `Intersect(L, r, D)`: `RELAXED` a `STRICT`, retirada por `NOT_AN_IDENTITY`, recomprobación entre revisiones | V-AUT-RLX-2, V-AUT-DYN-1 a 4, G-AUT-10 |
| E2.5 | premisas de los pliegues por el certificado, para la transferencia por estructura | V-AUT-TRF-2 en el modelo |
| E2.6 | caso especial heredado de un punto ya incidente (D1) en la tabla de clase y el evaluador | V-AUT-CIC-5 |

## 2. Un solo evaluador

El certificado no tiene su propio evaluador de la construcción. Es un cuarto sistema numérico del
evaluador del cono de E1 (`ConstructiveConeEvaluator`), junto a la evaluación exacta M3, los
intervalos y los jets de intervalos. Así M3, `SUBSTITUTE_GENERIC` y el certificado deciden los
mismos predicados (D1), con la misma selección heredada y la misma correspondencia con el algoritmo
vivo. No hay dos semánticas de un objeto. El evaluador gana tres puntos de extensión, sin cambiar su
comportamiento con los demás sistemas, y la regla del caso especial de §2b:

- una regla para los puntos sobre rectas, que el certificado usa para su indeterminada;
- una entrada de signo propia para las comprobaciones contra el algoritmo vivo, que el certificado
  guarda aparte de su celda porque leen el estado vivo de la revisión;
- el conductor del corte sustituido como un valor propio, el jet del conductor.

Este diseño también evita el defecto del prototipo en la pertenencia a segmentos: la intersección de
rectas del evaluador de E1 ya comprueba la pertenencia a la parte limitada y la rechaza con
`INVALID`. Las pruebas adversarias lo fijan.

## 2b. El caso especial de un punto ya incidente (D1)

`AlgoIntersectLineConic` tiene un caso especial: si un punto etiquetado `S` está registrado como
incidente con la recta y con la cónica (por sus listas de incidencia, nunca por una tolerancia), la
salida de su índice es `S` y la otra salida es la raíz que no es `S`, decidida con `isEqual`. La
especificación §3.2 lo deja en la clase (D1), con la decisión sustituida por un predicado riguroso.
E1 lo rechazaba con `SELECTION_NOT_RECONSTRUCTED`. E2 lo reproduce:

- el estado de selección lo registra con el índice de la salida de `S` (que entra en la clave), y
  `S` es la tercera entrada del nodo;
- la salida de ese índice es `S`; la otra es la raíz cuya distancia a `S` tiene signo positivo
  decidido, siempre que la de la otra raíz sea exactamente cero (cero exacto en M3, elemento nulo
  en el certificado). Cualquier otro resultado, o un signo sin decidir, da
  `SELECTION_NOT_RECONSTRUCTED`. Los intervalos nunca deciden ese cero, así que no dan encierro de
  esas salidas: es fallo cerrado, como en E1;
- fuera del corte del conductor, la salida viva se contrasta como en las demás salidas.

`CU2` no captura este caso (`CertifiedConstructionCapture2D`), así que ninguna transferencia lo
consume. El accesor de solo lectura del caso especial se añade a `AlgoIntersectLineConic`, como los
del estado de selección de `CU2`.

## 3. Álgebra y presupuesto

- **Monomios.** Dos palabras de 64 bits con ocho exponentes de ocho bits cada una, la primera
  indeterminada en el byte alto: el orden sin signo de las palabras es el lexicográfico de los
  exponentes. La suma de exponentes es bytewise y detecta el desbordamiento de un byte; un exponente
  mayor que 255 da `EXPONENT_RANGE_EXCEEDED` y nunca se trunca. Antes de multiplicar dos polinomios
  se comparan sus exponentes máximos.
- **Coeficientes.** Racionales exactos (`ExactRational2D`). Cada polinomio resultante se mide contra
  los términos y los bits del presupuesto.
- **Funciones racionales.** Sin máximo común divisor, con división exacta acotada solo para reducir
  el tamaño; la nulidad de una función es la de su numerador, de modo que la representación no
  canónica nunca da un certificado falso.
- **Cuerpo.** Una sola extensión cuadrática en la versión 1. Un cuadrado perfecto se extrae; una
  segunda raíz independiente o una raíz anidada dan `OUT_OF_CLASS`. La rama de cada raíz es la de
  signo positivo certificado en la caja.
- **Encierros.** Forma de valor medio: valor exacto en el centro de la caja, más los encierros de las
  derivadas parciales por los semianchos. Primero se usa el primer orden, con las derivadas
  encerradas de forma ingenua. Si no certifica el signo, se usa el segundo orden, con las derivadas
  en primer orden y las segundas derivadas ingenuas, y se intersecan los dos encierros. Las
  derivadas se calculan para cada encierro y no se guardan: un certificado conserva sus
  condiciones y nada más, y su memoria queda acotada por el presupuesto.
- **Presupuesto.** `constructive-identity-certificate-budget/v1` con los techos fijados por el autor
  (16 000 000 productos de términos, 16 indeterminadas, 200 000 términos y 4 096 bits), contadores
  deterministas y restricción solo hacia abajo para las inyecciones adversarias. G-AUT-8 los mide en
  E2; si alguno resulta insuficiente se presenta la evidencia al autor y no se eleva.

## 4. Frontera, caja y celda

- **Regla de frontera automática** (especificación §8): las entradas libres son indeterminadas; un
  punto sobre una recta constructible es una indeterminada, la coordenada del coeficiente mayor de la
  recta viva, con la otra despejada; un punto sobre una circunferencia constructible conserva la
  aplicación de camino heredada, y sus coordenadas persistidas son constantes de la clave; una
  rotación, cualquier otro punto de frontera y un punto sobre un camino que depende de objetos fuera
  de la clase se cortan como puntos libres del plano. El corte coincide con los sustitutos de
  `SUBSTITUTE_GENERIC`, de modo que `NOT_AN_IDENTITY` se refiere a la misma frontera cuyas
  perturbaciones dieron la evidencia relajada.
- **Caja de decisión.** No tiene semianchura ni ninguna constante: es el valor exacto de cada entrada
  libre, que es su definición, y, para un punto sobre camino o un corte, la envolvente de su valor
  vivo y del encierro riguroso de su valor ideal que da la evaluación por intervalos de E1. Sin ese
  encierro, el intervalo de un corte es toda la recta real (regla P2), y toda condición que lo lea
  queda sin decidir.
- **Celda.** Cada signo certificado de la evaluación se registra con su elemento. La comprobación
  de la correspondencia de ramas guarda las dos raíces formales para recalcular su margen con la
  salida viva de otra revisión.

## 5. Alcances, clave y recomprobación

- **Alcances.** El alcance automático es la regla de frontera de la especificación §8 y el único de
  `Intersect(L, r, D)`. El alcance de frontera, que la matriz exige comparar (V-AUT-CIC-1 y
  V-AUT-DYN-1), toma los puntos y números libres como las constantes binary64 exactas de la revisión:
  solo los puntos sobre rectas, los cortes y los números de frontera son indeterminadas. Es una
  prueba más débil, que solo vale para esas constantes, y nunca la usa el producto.
- **Clave.** `cic-structural-key/v1` es el SHA-256 de:
  - las versiones (autoridad, tabla de clase, política relajada, certificador y presupuesto);
  - el alcance y la relación;
  - la descripción estructural del cono de E1, con identidades durables, definiciones estructurales,
    clasificación y estados de selección y de etiqueta;
  - las constantes que la prueba lee: los ejes, los puntos que conservan la aplicación de camino y,
    en el alcance de frontera, los puntos y números libres.

  Ni las etiquetas ni los valores de las indeterminadas entran, y tampoco los valores por encima de
  un corte, que la prueba no lee (especificación §8.1, apartado 4). Un renombrado conserva la
  clave; una redefinición, una eliminación seguida de recreación o una copia la cambian, porque
  cambian la estructura o las identidades durables de la frontera de la relación.
- **Recomprobación.** En otra revisión exige la clave igual; recalcula la caja con la misma regla,
  vuelve a certificar cada signo de la celda y el margen de cada rama contrastada, sin trabajo
  simbólico nuevo y con contadores propios, de modo que nunca cambia el certificado que comprueba.
  Si algo falla, se intenta una prueba nueva.
- **Inyección adversaria.** Una ampliación de la caja de decisión en una semianchura (control C4)
  solo puede perder certificados, porque la caja sigue conteniendo los valores ideales; es de
  paquete y solo la usan las pruebas.

## 6. Orden de contacto

El certificado evalúa el cono una sola vez sobre jets exactos de orden dos en el cuerpo: los objetos
que no se mueven con el conductor no cuestan más que su valor. El conductor sustituido es el jet de
su camino en el punto estructural: en una circunferencia, las derivadas en el ángulo; en un
segmento, su dirección. Con `F` el punto trazado y `h` el residuo del objetivo:

- `F′` es el elemento nulo: punto estacionario de la traza. Su pliegue lo decide el certificado de
  `CU2` (ADR 0038, decisión 8), nunca este; el signo de `h″`, cuando se certifica, dice de qué lado
  del objetivo queda la traza;
- si `|F′|² > 0` está certificado: transversal si `h′` tiene signo certificado; tangente si `h′` es el
  elemento nulo y `h″` tiene signo certificado.

Los signos de `|F′|²`, `h′` y `h″` entran en la celda, y el certificado da los de `h′` y `h″`. Con
el jet del conductor en el ángulo, son comparables con los valores exactos del estudio (§1.7:
`−28,525`, `−24,672`, `+2,569`, `−7,992` y `+16,236`).

En un contacto estacionario, `Intersect(L, r, D)` pide además a `CU2` el pliegue por la vía de
estructura (`Cu2CertificateTransfer`, decisión cerrada `FOLD`). Cuando una premisa de simetría lee un
corte, la decide un certificado de este tipo para todo valor del corte (E2.5).

## 7. Integración en `Intersect(L, r, D)`

Cuando la evaluación exacta de `P ∈ r` usa un sustituto de frontera:

1. si la resolución anterior tiene un certificado y se recomprueba, se conserva;
2. si no, se intenta un certificado;
3. `IDENTITY_CERTIFIED`: `P ∈ r` es `STRICT`, con el certificado y su orden de contacto;
4. `NOT_AN_IDENTITY`, o `INVALID` porque un objeto de la relación está certificado indefinido para
   los valores ideales: se retira la evidencia de `SUBSTITUTE_GENERIC` y `P ∈ r` queda
   `NOT_ESTABLISHED` (especificación §8: `INVALID` deja la relación en `NOT_ESTABLISHED` con
   `OBJECT_UNDEFINED`);
5. `CONTRADICTED`: contradicción con el objetivo limitado;
6. cualquier otro resultado (`NOT_ESTABLISHED` con su causa) conserva la evidencia relajada, si la
   hay.

En un contacto estacionario, la resolución lleva también el registro del pliegue de `CU2` (§6).
Ninguna identidad, token ni selector cambia: la integridad es evidencia de revisión.

**Identidades durables y caché.** La clave lee las identidades durables del cono. Al crear un punto
estructural, su lote de participación publica las identidades de sus entradas después del primer
cálculo, así que el resumen de estado de la resolución incluye ahora las identidades y la creación
vuelve a calcular la evidencia tras publicarlas. Sin esto, el certificado de la creación tendría una
clave sin la identidad de `K` y no coincidiría con el de Deshacer, Rehacer o la reapertura
(V-AUT-ID-4 a V-AUT-ID-6). Un arrastre sigue sin repetir nada, porque no cambia ninguna identidad.

## 8. Validación de E2

- Puertas G-AUT-2, G-AUT-9 y G-AUT-10, y G-AUT-8 para el presupuesto del certificado
  (propuesta §6.3), con las filas V-AUT-CIC, V-AUT-ID y V-AUT-DYN de la matriz §6.
- Construcciones sintéticas S1 a S4 y controles C1 a C5 de la propuesta §5.5, con la rotación por
  un número libre para que su valor ideal se encierre con rigor.
- El banco del certificado mide sus contadores en el modelo y en los casos sintéticos.

## 9. Condiciones de parada específicas

Además de las del registro de aprobación §10:

- si un valor ideal necesario no se puede encerrar con rigor en la caja;
- si un límite del presupuesto del certificado resulta insuficiente para los cinco contactos: se
  presenta la evidencia al autor;
- si la integración exigiera cambiar una identidad, un token o un selector.
