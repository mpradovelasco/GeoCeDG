# PRE-G9B-CU3 — T7-a, Puerta N: contrato normativo y validez

| Campo | Valor |
|---|---|
| Carácter | Registro de la puerta normativa y de validez de T7-a, previa a todo cambio del producto. No es una aprobación |
| Autoridad | instrucción autoral «PRE-G9B-CU3 — Decisión autoral sobre T7-a», de 2026-10-26 (§1) |
| Referencia | candidato T6 `3f0968385920e36b2b48191a4f6e736c4fc11c09`; [caracterización de T7-a](pre_g9b_cu3_t7a_characterization_record.md) |
| Enmienda | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md), versión 1.5, «Constructive admission policy v2 amendment» |
| Resultado | **Puerta N superada**, con las premisas A1 y A2 de §6, que son las mismas de la v1 (§10) |
| `selfApproved` | `false` |

## 1. Autorización

> DECISIÓN AUTORAL: T7-a APROBADA COMO DIRECCIÓN ARQUITECTÓNICA. IMPLEMENTACIÓN AUTORIZADA CON
> PUERTA NORMATIVA Y DE VALIDEZ BLOQUEANTE.

- **Alcance:** solo T7-a. No incluye T7-b, T5 ni otros cambios del algoritmo de certificación.
- **Orden:**
  1. la Puerta N: enmienda `cu3-certified-admission/v2`, condiciones de la consulta anticipada,
     correspondencia, conservación de la prueba, validez y continuidad, y auditoría de la cadena
     de encierros;
  2. después, la Puerta I.
- **Exigencias:**
  - la corrección no puede depender de una semimonotonía de `StrictMath` que no esté justificada;
  - hay que distinguir cuatro clases de fallo;
  - un encierro no nulo solo certifica la validez de la caja si la implementación cumple ese
    contrato;
  - si la puerta no se supera, el trabajo se detiene.
- **Decisiones normativas aprobadas:**
  - consultar la decisión exacta antes de buscar el signo del valor crítico;
  - aceptar las certificaciones que la v1 dejaba en el límite de trabajo;
  - mantener `MAXIMUM_EVALUATIONS = 4096`;
  - ningún umbral, tolerancia ni aproximación nuevos;
  - admisibilidad local independiente de la completitud global.

## 2. La regla de la v2

La v2 es la v1 con una sola regla añadida, y solo en el certificado de conteo de raíces de la
configuración constructiva `Σ_C`. Sea `T = [a, b]` la caja del certificado. La v1 ya ha
establecido estas cuatro condiciones antes de llegar al punto crítico:

1. **V1:** un encierro no nulo del chorro del residuo en todo `T` (la primera caja de
   `fixedSign(a, b, 2)`).
2. **V2:** un signo certificado `s` de `h''` en todo `T`.
3. **V3:** signos certificados y opuestos de `h'` en `a` y en `b`.
4. **V4:** el corchete `[l, u]` del cero de `h'`, obtenido con signos certificados de `h'`.

La regla es esta:

- **Encierro único de `h` en `[l, u]`.** Es el mismo que la v1 calcula como primera caja de
  `fixedSign(l, u, 0)`.
  - Si no se obtiene, el resultado es el de la v1: la fuente no está certificada válida.
  - Si su encierro de `h` excluye el cero, sigue el procedimiento de la v1.
  - Si lo contiene, se consulta la decisión exacta de la configuración antes de subdividir.
- **Cuándo la consulta establece la forma.** Establece «una raíz doble en un punto exacto», sin
  más subdivisión, solo si devuelve un contacto exacto con estas tres condiciones:
  - el encierro exterior de `t0` está contenido en `[l, u]`;
  - `h(t0) = 0` y `h'(t0) = 0` están establecidos exactamente;
  - el signo exacto de `h''(t0)` es `s` o no está decidido.
- **En cualquier otro caso** sigue el procedimiento de la v1 desde ese mismo encierro, con los
  mismos límites. Esos casos son:
  - no hay contacto;
  - el contacto está fuera del corchete;
  - el signo exacto de `h''` es opuesto;
  - el contacto es de orden superior (la configuración no lo devuelve como contacto);
  - la decisión exacta es insuficiente.

  El procedimiento de la v1 incluye su propia consulta exacta tras la subdivisión, que repite la
  misma decisión.

Fuera de la configuración constructiva no hay consulta anticipada. Un documento bajo
`legacy-representation-authority/v1` sigue en la v1 sin ningún cambio: mismas decisiones, mismos
recuentos y mismos diagnósticos.

La regla reutiliza el encierro del corchete, sin evaluarlo dos veces. Por eso, cuando no hay
anticipación, la v2 hace exactamente las mismas evaluaciones que la v1, en el mismo orden.

## 3. Condiciones de la consulta anticipada (N2)

La anticipación solo puede tener éxito si se cumplen todas estas condiciones:

| Condición | Cómo se establece |
|---|---|
| Fuente constructiva | `ResidualJetSource2D.isConstructive()`, es decir, un documento bajo `constructive-authority/v1` |
| Validez y continuidad `C²` en `T` | V1 y §6 |
| Signo fijo `s` de `h''` en `T` | V2: unión de subcajas con el mismo signo certificado |
| Punto crítico único en `T` | V2 y V3: `h'` es estrictamente monótona y sus extremos tienen signos opuestos |
| `t* ∈ [l, u]` | V4: signos certificados de `h'`. Un signo indeciso detiene la bisección sin estrechar el corchete |
| Validez en el corchete | el encierro no nulo de `[l, u]`, obtenido por la propia regla |
| Encierro de `h` en `[l, u]` que contiene el cero | sin ello no hay consulta |
| Contacto exacto en el corchete | `exactDouble`: `l ≤ inf(t0)` y `sup(t0) ≤ u` |
| `h(t0) = 0` y `h'(t0) = 0` exactos | certificado de identidad constructiva y sus chorros exactos (`ConstructiveResidualJetSource.decision`) |
| `h''(t0)` coherente | signo exacto igual a `s` o no decidido; un signo opuesto no anticipa nada |

## 4. Correspondencia (N3)

- **Parámetro.**
  - `t0 = kπ/2` se representa por su encierro `quarterTurns(k)`, calculado en aritmética
    racional exacta a partir de `Math.PI/2 < π/2 < nextUp(Math.PI/2)`; por tanto
    `t0 ∈ [inf, sup]` rigurosamente.
  - La regla exige `[inf, sup] ⊆ [l, u]`, con lo que `t0 ∈ [l, u] ⊆ T`.
- **Fuente.**
  - La decisión exacta y los encierros pertenecen al mismo `ConstructiveResidualJetSource`: el
    mismo cono de la consulta, los mismos nodos del punto trazado, del objetivo y del soporte, y
    el mismo marco.
  - El conductor del certificado exacto en `t0` es el punto exacto de su circunferencia con la
    misma parametrización `m + r (cos t e0 + sin t e1)` que usan los encierros.
  - El residuo exacto es el mismo funcional de incidencia: `a x + b y + c` para una recta y
    `|p − c|² − r²` para una circunferencia.
- **Rama.**
  - La selección de cada salida de intersección es estructural (la selección capturada) o se
    decide con signos certificados, constantes en toda la caja (§6).
  - El certificado exacto evalúa la misma selección.
  - Sus correspondencias vivas se registran y se comprueban en cada revisión.
- **Revisión.**
  - La memoria de decisiones exactas se indexa por la huella del cono, su estado, la revisión
    semántica y los presupuestos.
  - El certificado de otra revisión solo se reutiliza si `recheckAtParameter` comprueba su celda.
  - La fuente se captura por consulta, en la revisión vigente.
- **Configuración.** La política se lee de la autoridad del documento en cada cálculo
  (`DocumentDecisionConfiguration`).

## 5. Conservación de la prueba (N4)

- **Unicidad del punto crítico.** Por V2, `h''` tiene el signo `s` en todo `T`, así que `h'` es
  estrictamente monótona en `T`. Por V3 tiene exactamente un cero `t* ∈ (a, b)`, y por V4
  `t* ∈ [l, u]`.
- **Identificación de `t0`.** El contacto exacto da `h'(t0) = 0` con `t0 ∈ T`. Por la unicidad,
  `t0 = t*`.
- **Raíz única y doble.** Con `h(t0) = h'(t0) = 0` exactos y `h ∈ C²(T)`, el teorema de Taylor con
  resto de Lagrange da, para cada `t ∈ T`, un `ξ` entre `t0` y `t` con
  `h(t) = h''(ξ)(t − t0)²/2`. Por tanto `h` tiene el signo `s` en `T \ {t0}`: `t0` es la única
  raíz en `T`, aislada y de multiplicidad exactamente dos (`h''(t0) ≠ 0`, porque `s ≠ 0`).
- **El papel de la bisección omitida.** No interviene en este razonamiento. En la v1 solo decide
  qué rama sigue el algoritmo, y un encierro riguroso nunca puede certificar un signo de `h` en un
  intervalo que contiene un cero de `h`.
- **Coherencia sin subdividir.** La v2 solo consulta cuando el encierro del corchete contiene el
  cero. Una decisión exacta con `h(t0) = 0` en el corchete y un encierro de `h` que lo excluyera
  sería una contradicción entre dos pruebas. En esa situación la regla no consulta y no anticipa
  nada.

## 6. Validez y continuidad de la fuente (N5) y auditoría de los encierros (N6)

### 6.1 Contrato

Según `ResidualJetSource2D.enclose`, un resultado no nulo certifica que la fuente es válida y
continua en toda la caja.

Una auditoría independiente del código revisó si `ConstructiveResidualJetSource` cumple ese
contrato en sentido puntual. Abarcó:

- `ConstructiveResidualJetSource`, `ConstructiveConeEvaluator`, `IntervalJet`, `OutwardInterval`,
  `ConstructiveArithmetic` y `DriverParameterEnclosure.cosSin`;
- la memoria del corte y los valores independientes de T6.

Para todo `t` real de la caja, la pregunta fue si se cumplen:

- **(V)** la evaluación ideal está definida;
- **(B)** toda elección discreta es la misma en toda la caja, de modo que el residuo es una sola
  función `C²`;
- **(E)** los intervalos contienen `h(t)`, `h'(t)` y `h''(t)`.

### 6.2 Resultado

(V), (B) y (E) se cumplen bajo las premisas A1 y A2 (más A3, solo para un conductor sobre un
segmento):

- **(V).** Cada operación que puede no estar definida exige una prueba sobre todo el intervalo:
  - el divisor excluye el cero (`OutwardInterval.divide`);
  - el argumento de la raíz de un chorro tiene cota inferior positiva (`IntervalJet.sqrt`);
  - la raíz de valores exige una cota inferior no negativa;
  - radios positivos y puntos distintos (`requirePositive`);
  - rectas no paralelas;
  - discriminante positivo;
  - pertenencia a un camino limitado.

  Todas son signos certificados: un signo solo se certifica si el intervalo entero queda a un lado
  del cero. Como el intervalo contiene el valor ideal en cada `t`, la condición se cumple en cada
  `t`.
- **(B).** Toda bifurcación es de uno de estos dos tipos:
  - estructural: selección capturada, índice de salida, operación, marcas de limitado o
    semirrecta, marca del corte;
  - un signo certificado.

  Las raíces de recta y circunferencia son `pie ∓ sqrt(disc)·(b, −a)/norma`, con `disc > 0`
  certificado: cada raíz es una función suave de `t` y su índice no se intercambia dentro de la
  caja. Las desigualdades estrictas en la caja cerrada se mantienen en un entorno abierto, así que
  `h` es `C^∞` en un entorno de la caja.
- **(E).** Las reglas de la cadena de `IntervalJet` son correctas: producto; cociente
  `w' = (u' − w v')/v` y `w'' = (u'' − 2w'v' − w v'')/v`; raíz `w'' = (u'' − 2w'²)/(2w)`; coseno y
  seno; parámetro `(caja, 1, 0)`. Las operaciones mantienen la contención componente a componente.
- **Reutilización de T6.** Los valores independientes y la memoria del corte devuelven los mismos
  objetos que una evaluación completa: la clave contiene todo lo que lee la evaluación del punto
  trazado, y los fallos solo rechazan.

### 6.3 Ninguna decisión depende de la monotonía

La auditoría no encontró decisiones tomadas sobre un punto medio, una sola cota o un valor
binary64, ni envolventes de ramas, ni `abs`, `min`, `max` o `atan2` sobre valores, ni dependencias
de la monotonía entre cajas.

### 6.4 Premisas

- **A1. Precisión de `StrictMath.cos` y `StrictMath.sin`.**
  - Los extremos se ensanchan dos binary64 hacia fuera, frente al error inferior a un ulp de fdlibm
    (`ConstructiveArithmetic`).
  - `StrictMath` se define como los resultados de fdlibm. fdlibm documenta ese error para seno y
    coseno, pero `StrictMath` no repite la cota método a método; `Math` sí declara un ulp.
  - Es la premisa de todo encierro trigonométrico del núcleo y de todos los certificados de la v1.
  - Es una premisa de **precisión**, no de monotonía.
- **A2. Aritmética IEEE 754** con redondeo al más próximo, y `Math.sqrt` correctamente redondeada
  (lo especifica la API de Java). Redondear al más próximo y desplazar un binary64 hacia fuera da
  contención.
- **A3. Conductor sobre un segmento.** La caja debe estar dentro de `[0, 1]`.
  `ConstructiveResidualJetSource` no lo comprueba, porque depende de quien la llama. **No afecta a
  T7-a:** solo un conductor sobre una circunferencia tiene decisiones exactas.

### 6.5 Por qué T7-a no depende de la semimonotonía

- La validez en `T` procede de V1: un encierro no nulo en `T` entero, con (V) y (B) por contención.
- La validez en el corchete procede del encierro no nulo de `[l, u]` que la regla obtiene.
- Ninguna de las dos se deduce de otra caja, así que ninguna usa la monotonía respecto de la
  inclusión.
- La v1 sí podía devolver `INVALID` cuando el encierro de una subcaja fallaba tras el de su caja
  madre. Eso solo es posible si la evaluación no es monótona a nivel de ulp, y con
  `cosSin` (extremos de `StrictMath` en dos cajas distintas) no está excluido. La v2 no subdivide
  el corchete cuando la anticipación tiene éxito, así que tampoco depende de ello.

### 6.6 Observaciones preexistentes, ajenas a T7-a

No se corrigen aquí:

- **F1.** La especificación del modelo de construcción certificada (§6) habla de un ulp y de
  `|a|, |b| ≤ 1000` para la configuración capturada. `cosSin` ensancha dos binary64 y no comprueba
  esa cota de argumento. Es más conservadora en el ensanchamiento y no limita el argumento, cuya
  reducción en fdlibm es exacta. Se registra como discrepancia documental.
- **F2.** Es la premisa A3.
- **F3.** Un objetivo que depende del conductor se evalúa fuera del corte, con sus puertas vivas.
  Solo produce rechazos.
- **F4.** El residuo normalizado de una circunferencia o la norma de una recta pueden rechazar una
  caja cuyo `h` sí estaba encerrado. Solo produce rechazos.
- **F5.** La rotación se trata de forma distinta en el sistema exacto (sustitución) y en el de
  intervalos.
  - No afecta a (V), (B) ni (E).
  - En el corte, el sistema exacto la rechaza, así que no da contactos exactos que pudieran
    discrepar del encierro.
- **F6.** Una excepción aritmética dentro del evaluador no se trata como `Stop`, pero ningún
  llamante reutiliza el evaluador. Solo produce rechazos.

## 7. Las cuatro clases de fallo

| Clase | Qué es | Dónde aparece | v1 | v2 |
|---|---|---|---|---|
| Invalidez geométrica real | la fuente no tiene valor en algún `t` de `T` | por (V), el encierro de `T` es nulo | `NOT_CERTIFIED`, «the source is not certified valid on the box» | igual (no se llega al punto crítico) |
| Falta de demostración de validez | la fuente puede ser válida, pero el encierro de `T` o de `[l, u]` no se obtiene | V1, o la primera caja del corchete | igual | igual |
| Fallo numérico de un encierro en una subcaja | el encierro de una subcaja de `[l, u]` falla tras obtenerse el de `[l, u]`. Por (V), la fuente es válida en `[l, u]`: es un fallo numérico | subdivisión del valor crítico | `NOT_CERTIFIED` con el mismo motivo | con contacto exacto en el corchete: `ONE_DOUBLE`, sin subdividir. Sin él: v1 |
| Agotamiento del presupuesto | `MAXIMUM_EVALUATIONS = 4096` superado | cualquier punto | `NOT_CERTIFIED`, «the work limit of the certificate is reached» | igual, salvo que la anticipación establezca la forma antes |

El motivo textual de las dos primeras clases es el de la v1; la v2 no cambia los motivos de
rechazo. El registro de la implementación muestra cada clase en una prueba adversaria.

## 8. Diferencias aceptadas respecto de la v1

Todas son las que el autor aprobó (§1):

1. Un certificado que la v1 deja `NOT_CERTIFIED` por el límite de trabajo, o por el fallo
   numérico de una subcaja del corchete, es `ONE_DOUBLE` en la v2 cuando la decisión exacta lo
   establece. La caracterización observó dos casos, ambos en `rbCone = 7,5` (`o_1·h_2`), y ninguno
   del segundo tipo.
2. Cuando la v1 también llega a `ONE_DOUBLE`, la v2 lo establece con menos evaluaciones: cambian
   el contador del certificado y el número de encierros de su descripción, que son telemetría.
3. Si el encierro del corchete contiene el cero pero la subdivisión de la v1 habría certificado un
   signo, la v2 consulta la decisión exacta donde la v1 no la consultaba.
   - Por §5 no puede devolver un contacto en el corchete.
   - El coste es el de la consulta exacta, memorizada por revisión y dentro del presupuesto
     aprobado del certificado de identidad constructiva.
4. El nombre de la política en la evidencia y en los diagnósticos de los documentos constructivos
   pasa a ser `cu3-certified-admission/v2`.

No cambian:

- `MAXIMUM_EVALUATIONS`, `MAXIMUM_DEPTH`, `MAXIMUM_BISECTIONS` y el ancho de corchete;
- los presupuestos de los certificados exactos;
- las reglas de fallo cerrado;
- la regla de conflicto, los grados y la admisibilidad del §8 de la especificación;
- la independencia entre admisibilidad local y completitud global.

## 9. Transición, identidad y compatibilidad (§4 de la autorización)

- **Alcance.** La política sigue a la autoridad del documento:
  - `constructive-authority/v1` → v2;
  - `legacy-representation-authority/v1` → v1, sin cambios.

  El cambio de autoridad, y con él el de política, solo ocurre con el evento explícito de
  adopción, con Deshacer o al recargar el documento.
- **Documentos ya adoptados.**
  - La evidencia es de revisión: no se persiste y no entra en tokens, selectores ni el grafo.
  - En su siguiente cálculo, un documento adoptado se decide en la v2.
  - Un contacto que la v1 dejaba sin certificar puede pasar a `EXISTENCE_EXACT`, con multiplicidad
    y orden establecidos. La regla de conflicto actúa entonces como en CU3.4: la reclasificación se
    publica con el diagnóstico tipado `CONTRADICTED_BY_CERTIFIED_EVIDENCE`.
- **Admisibilidad.**
  - Solo cambia por una contradicción rigurosa, como en la enmienda 1.4.
  - Un contacto tangente no se admite para el consumo puntual antes de `CU3-B`
    (`ConstructiveAdmission.NOT_ESTABLISHED`).
  - En los dos casos de `rbCone = 7,5`, la admisibilidad era `false` en la v1 y lo sigue siendo.
- **Tokens.**
  - Los tokens durables y sus reglas no cambian.
  - Un token tangente nunca se transfiere.
  - Los identificadores `locus-root-local/v1` son de revisión, no se persisten y no son
    identidad.
  - No hay reasociación: una solución contradicha pierde su admisibilidad con causa tipada.
- **Resultados ricos.** Pueden cambiar en un documento adoptado:
  - la evidencia (grado, garantía numérica, método, multiplicidad y orden);
  - los diagnósticos;
  - el nombre de la política.
- **Pruebas.** Las pruebas de la regla de conflicto, las inversiones de pines y los casos
  obligatorios no exigen recuentos de evaluaciones, así que se mantienen. Las pruebas nuevas
  comparan la v2 con el procedimiento de la v1 sobre la misma fuente.
- **Formatos durables.** Ningún cambio.
- **Coherencia con los ADR.**
  - ADR 0017: la identidad determinista de fase y rango no cambia.
  - ADR 0039: la forma «raíz doble en un punto exacto» ya era una de las cuatro formas del
    certificado; solo cambia el orden de las comprobaciones.
  - ADR 0040: la autoridad constructiva decide en `Σ_C` y la v2 solo actúa ahí.

## 10. Conclusión

- **La Puerta N se supera.** La regla de §2 establece `ONE_DOUBLE` solo con premisas
  certificadas: validez y `C²` por contención en `T` y en el corchete, signo fijo de `h''`,
  unicidad del punto crítico y decisión exacta en el corchete.
- **Lo que no usa.** Ninguna tolerancia, ninguna aproximación y ninguna hipótesis de monotonía.
- **Premisas.** Sus premisas numéricas (A1 y A2) son las mismas de todos los certificados de la
  v1. T7-a no añade ninguna premisa y elimina la dependencia de la v1 respecto del fallo de
  subcajas.
- **Lo que queda abierto.** La premisa A1 y la discrepancia F1 se presentan al autor como
  propiedades preexistentes del núcleo, no como condiciones de T7-a.
- **Siguiente paso.** La Puerta I implementa la regla y la versión de la política en el núcleo
  compartido, con el procedimiento de la v1 como respaldo.
