# PRE-G9B-CU3 — Propuesta normativa candidata: autoridad geométrica constructiva e integridad de las relaciones

| Campo | Valor |
|---|---|
| Carácter | **Propuesta normativa candidata única** de la consolidación focal. No modifica ninguna especificación, ningún ADR, ningún formato persistido ni ningún código productivo. No es contrato hasta su aprobación autoral; `selfApproved=false` |
| Instrucción | «PRE-G9B-CU3 — Decisión autoral tras la auditoría independiente del estudio de autoridad constructiva» (2026-10-24) |
| Dirección adoptada por el autor | Para desarrollo normativo, no como contrato aprobado: **A2** (autoridad geométrica constructiva ideal), **B2** (certificación de relaciones estructurales), **C** (revisión coordinada, versionada y mínima de los contratos de autoridad) y **D** (estados de integridad estricta y relajada, independientes de las garantías numéricas, con propagación por premisas) |
| Antecedentes | [estudio de la autoridad constructiva](pre_g9b_cu3_constructive_authority_study.md), publicado en `da7ca96ae68732ad61db48923f5d8521c9ba8def` y revisado favorablemente por el autor; [informe de la puerta G-M3](../validation/pre_g9b_cu3_m3_gate_report.md); [registro de aprobación del diseño de `CU3`](../validation/pre_g9b_cu3_author_design_approval_record.md) |
| Rama | `phase/pre-g9b-cu3-authority-study`, local, sobre `da7ca96a` |
| Prototipo | `PreG9BCu3ConstructiveIdentityProbe` (`tools/benchmark/pre-g9b-cu/cu3-java/`) con su lanzador [`run-cu3-constructive-identity.ps1`](../../tools/benchmark/pre-g9b-cu/run-cu3-constructive-identity.ps1). Herramienta de investigación, no producto, en el commit `6ce61d818187ea390024fdf9c54beeaf3cbd743f` (árbol `4f8a08b32f2f195063c082ecad1b1660adc0ad08`), con las correcciones de una revisión independiente de la consolidación |
| Evidencia | [`pre-g9b-cu3-constructive-identity-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json), SHA-256 `b5668d21228e2f41315a96a9de159faa64d4396105fece1ae17e488e12fd449d`: copia byte a byte de la ejecución A, sobre un árbol limpio; la ejecución B dio el mismo SHA-256 |
| Estado | `CU3` sigue detenido en T2. Ninguna alternativa, norma, cota ni puerta queda aprobada por este documento |

Esta propuesta no repite la caracterización del estudio: la cita. Los textos normativos
candidatos van en inglés, como las especificaciones y los ADR a los que se destinan. Cada uno
indica su destino, su naturaleza (**nuevo**, **amplía**, **precisa**, **restringe** o
**sustituye**), las decisiones de las que depende y la evidencia que exige. Donde una decisión
tiene alternativas, el texto corresponde a la recomendada.

## Resumen

1. **Autoridad única y convivencia (§2).** Bajo la política versionada de documento
   `constructive-authority/v1`, cada objeto tiene un solo significado geométrico: el objeto
   constructivo ideal, función pura del grafo, de las entradas libres y de una tabla de clase
   versionada. El significado no depende del comando que lo consume, del conductor, de la captura
   ni de una reconstrucción. Los algoritmos heredados no cambian: calculan representaciones, y las
   relaciones que dan por procedencia son estrictas para los objetos ideales cuando esos objetos
   existen, lo que se decide aparte y de forma rigurosa. Los documentos existentes conservan la
   autoridad vigente (`legacy-representation-authority/v1`) hasta un evento explícito de
   adopción, con informe de impacto y sin reasociaciones; los lectores anteriores fallan cerrados
   ante el registro nuevo, y nunca hay dos autoridades en un documento.
2. **Transferencia de `CU2` (§3).** Un certificado de `CU2` sigue siendo, sin cambio de bytes, de
   huella ni de claves, un certificado de su representación `Σ_L`. La política admite su
   transferencia al objeto constructivo si y solo si hay correspondencia estructural, cita la
   misma revisión y, para cada una de sus decisiones, una de tres vías establece rigurosamente el
   mismo enunciado en la configuración constructiva: **margen** para las decisiones abiertas,
   **estructura** o **nueva decisión** para las cerradas. En otro caso, el resultado es
   `EVIDENCE_INSUFFICIENT` y todo consumidor estricto falla cerrado.
3. **Integridad `RELAXED` (§4).** La política `constructive-relaxed-admission/v1` admite una
   relación solo con evidencia positiva relativa a los objetos constructivos y sin contradicción
   rigurosa respecto de la misma autoridad. Esa evidencia es la igualdad exacta genérica con los
   sustitutos de frontera declarados (en el sustituto y en perturbaciones racionales suyas) o,
   solo en raíces con cambio de signo, un encierro riguroso del residuo constructivo compatible
   con la tolerancia versionada.
   Distingue evidencia relajada, contradicción rigurosa y ausencia de evidencia. G8B-R1 sigue
   siendo el precedente de verificación local, pero en un documento constructivo su admisión se
   recalifica, nunca se hereda.
4. **Certificación estructural genérica (§5).** El prototipo de **certificado de identidad
   constructiva** (CIC) establece, como evidencia de investigación, las cinco incidencias
   obligatorias como identidades en un cuerpo de funciones racionales, con una regla de frontera
   automática y sin código específico del modelo. `H` entra como punto libre, así que, dentro de
   su celda y con su valor ideal en la caja, la prueba no depende de su posición exacta no
   constructible. Las precondiciones son condiciones de signo certificadas por intervalos; las
   ramas fuera del corte del conductor se contrastan con el algoritmo vivo, y todo se recomprueba
   en otra configuración sin trabajo simbólico nuevo si la estructura no cambia. Los controles
   adversarios fallan como deben, y en construcciones sintéticas la capacidad demuestra los
   teoremas y no demuestra las conjeturas falsas. Las doce posiciones del estudio son
   recomprobaciones, no la demostración.
5. **Plan mínimo (§6).** Primero, en E1, construcciones útiles con integridad consultable:
   `P ∈ L` estricta por transferencia y `P ∈ r` relajada o estricta. Después, en E2, la
   certificación estricta por CIC; en E3, la regla de conflicto con paridad y la Etapa B; y en E4,
   el cierre `INTEGRATED_PHASE`. Hay puertas científicas y de compatibilidad con pruebas
   adversarias y condiciones de parada que permiten ejecutar sin consultas intermedias.
6. **Decisiones (§10).** D-AUT-1 a D-AUT-12, con recomendación. Ninguna se presupone.

## 1. Términos

| Término | Significado en esta propuesta |
|---|---|
| Objeto constructivo | El objeto matemático que define la construcción desde sus entradas libres (A2). El valor binary64 de una entrada libre es su definición |
| Representación | Todo estado numérico de un objeto: su estado binary64, la configuración capturada `Σ_L` de `CU2`, un encierro, un sustituto de frontera. Lleva su garantía numérica (`NumericGuarantee`) |
| Relación | Incidencia, pertenencia a una parte limitada o igualdad entre objetos constructivos |
| Integridad | Lo establecida que está una relación respecto de la autoridad del documento: `STRICT`, `RELAXED`, `NOT_ESTABLISHED` o `CONTRADICTED` ([estudio](pre_g9b_cu3_constructive_authority_study.md) §2.4) |
| Política de autoridad | Registro versionado de documento que fija la autoridad, la tabla de clase, la política `RELAXED` y el certificador vigentes (§2.5) |
| Tabla de clase | Lista versionada de los algoritmos cuya semántica exacta conoce el evaluador constructivo (`constructive-class/v1`) |
| Sustituto de frontera | El estado binary64 de un objeto que queda fuera de la clase, usado como dato exacto por el motor de cálculo (la lectura A1). Nunca es la autoridad |
| `Σ_L`, `Σ_C` | Configuración capturada de `CU2` y configuración constructiva: los mismos objetos con su significado constructivo |
| Corte | En un CIC, un objeto sustituido por indeterminadas. Olvida relaciones y nunca las inventa (§5.3) |
| Celda | Conjunto de configuraciones en que se cumplen todas las condiciones de signo y los estados de selección que registró una prueba (§5.4) |

## 2. Punto 1 — Autoridad única y convivencia con el producto existente

### 2.1 Regla de autoridad única

En un documento con la política `constructive-authority/v1`:

- **Un significado por objeto.** Cada objeto tiene exactamente un significado geométrico, el de
  su construcción ideal. Es una función pura del grafo, de las entradas libres, de la tabla de clase
  y de la semántica de los puntos sobre caminos (§2.6).
- **Independencia del consumidor.** El significado no depende del comando que consume el objeto
  (heredado o nuevo), del conductor, de la configuración capturada ni de la frontera de una
  reconstrucción. Desaparecen así las cuatro semánticas exactas de `h_2` que mostró el estudio
  (§1.5).
- **Todo lo demás es representación.** El estado binary64, la captura `Σ_L` y los encierros son
  representaciones con su garantía numérica. Una representación nunca decide una relación del
  objeto.

La regla es uniforme por documento y por versión, nunca por relación. Por eso no viola G-M3-4: el
significado de un objetivo no se redefine para que una incidencia se cumpla. Lo que cambia es la
autoridad de todos los objetos a la vez, por una decisión autoral versionada. Los cinco contactos
del modelo revelaron el problema, pero la decisión no depende de ellos: la autoridad binary64
contradice el 41 % de la procedencia del modelo (estudio §1.2), y eso basta para justificar el
cambio sin ningún caso particular.

### 2.2 Comandos heredados

- **Cálculo sin cambios.** Los algoritmos heredados (`AlgoIntersectLineConic`,
  `AlgoJoinPoints`, `AlgoCirclePointRadius` y los demás) siguen calculando en binary64, igual que
  hoy, con el mismo dibujo y la misma exportación.
- **Sus salidas son representaciones.** El `M_1` binary64 representa la intersección ideal de
  `c_1` y `n`, con error. Las relaciones que el algoritmo establece por procedencia (`M_1 ∈ c_1`,
  `M_1 ∈ n`) son `STRICT` para los objetos ideales, aunque sus representaciones no las cumplan
  exactamente.
- **La existencia del objeto ideal es un hecho aparte.** Una relación de procedencia vale para el
  objeto ideal cuando ese objeto existe. Su existencia se decide rigurosamente: la validez de su
  construcción, la pertenencia a una parte limitada y la rama, por margen, por estructura o por un
  certificado de identidad constructiva (§5). Mientras no se decide, es `NOT_ESTABLISHED`. La
  tolerancia con que un algoritmo heredado acepta, por ejemplo, un punto junto al extremo de un
  segmento es un artefacto de la representación y nunca decide la existencia del objeto ideal.
- **Comandos heredados que deciden relaciones.** `Relation`, `AreEqual`, `AreParallel`,
  `IsInRegion` o el demostrador heredado (`Prove`) siguen como están. Sus veredictos son
  resultados de la representación binary64, o verdad genérica sobre los complejos en el caso del
  demostrador: nunca son evidencia de integridad, y ningún consumidor de GeoCeDG los usa como tal.
  Ofrecer en ellos la integridad constructiva queda fuera de `CU3`.
- **Lo único que cambia** es el significado de lo que afirman las capas de GeoCeDG sobre esas
  salidas: certificados, integridad y admisiones. Ninguna línea de un algoritmo heredado cambia de
  comportamiento.

### 2.3 Comandos nuevos

Las formas de `CU3` (`Point(L, D)` e `Intersect(L, r, D)`) deciden contra los objetos
constructivos, tanto el punto del locus como el objetivo. Materializan una posición, que es una
representación con su error declarado, y publican la integridad de cada incidencia con sus
premisas. Su materialización se rige por D-CU3-5, revisada en D-AUT-7.

### 2.4 Resultados existentes de Locus V2

| Capa | Bajo `constructive-authority/v1` |
|---|---|
| Identidad: tokens, selectores, claves de linaje, direcciones de `Point(L, …)`, texto del adaptador dentro de los tokens | sin cambios; nunca dependen de la autoridad (ADR 0008, ADR 0013 y ADR 0039) |
| Certificados de `CU2` | se conservan como certificados de `Σ_L` y se transfieren por §3 |
| Raíces admitidas por G8B-R1 | se recalifican al adoptar la política (§4.4); las contradichas pierden la admisibilidad con causa tipada y nunca se reasocian (D-CU3-7) |
| Métricas (`CU1`) | su autoridad es el locus ideal; sus clases de garantía se corresponden con los estados de integridad (§4.6) |

### 2.5 Documentos, políticas versionadas y migraciones explícitas

- **Registro de política.** Es un registro aditivo y versionado del documento nativo:
  `authority`, `class`, `relaxedAdmission` y `certifier`, cada uno con su versión. Persiste la
  política, nunca la evidencia, que se recalcula (ADR 0013: «Reconstructible inputs and policy are
  persisted; current numeric evidence must be recomputed and verified»). Su sintaxis y su lugar en
  el archivo los fija E1 con el contrato de persistencia.
- **Ausencia de registro.** Equivale a `legacy-representation-authority/v1`: los contratos hoy
  aprobados, es decir, el objetivo autoritativo de `locus-v2-intersections.md` §2.1, la
  configuración de decisión de `locus-v2-certified-construction-model.md` §11.2–§11.3 y la admisión
  G8B-R1. Abrir, editar, guardar, deshacer y rehacer un documento antiguo nunca cambia su política
  (como la migración perezosa del ADR 0029, decisión 3).
- **Documentos nuevos.** Nacen con `constructive-authority/v1` (D-AUT-2).
- **Adopción explícita.** `AUTHORITY_POLICY_ADOPTION` es un evento semántico del documento, que
  pide el usuario o un comando:
  - calcula antes de aplicar un informe de impacto con las relaciones cuya integridad o admisión
    cambia;
  - se aplica en una sola transacción, con Deshacer;
  - no cambia ninguna identidad, token ni selector;
  - los resultados contradichos pierden su admisibilidad con causa tipada, sin reasociación
    (D-CU3-7, barrera de cardinalidad del ADR 0017);
  - no existe la vuelta automática; volver atrás es Deshacer.
- **Sin documentos mixtos.** A diferencia de los registros del ADR 0029, la autoridad es del
  documento entero. Dos autoridades en un documento darían dos significados a un mismo objeto.
- **Formas de `CU3` en un documento antiguo.** Crear una exige adoptar la política: el comando
  ofrece el evento de adopción y, si se rechaza, falla cerrado con causa tipada.
- **Copiar, pegar y macros.** Rige la política del destino. Pegar formas de `CU3` en un documento
  antiguo sigue la regla anterior.
- **Lectores anteriores.** Un documento con el registro de política falla cerrado en ellos, con
  causa tipada, igual que un documento con formas de `CU3` (D-CU3-12). Es el patrón del ADR 0029:
  una versión anterior rechaza un registro de versión nueva como `MALFORMED_RECORD` y no carga
  nada, nunca lo interpreta con su propia regla. E1 sitúa el registro en el formato de modo que
  así ocurra y lo demuestra con una prueba de compatibilidad (G-AUT-7). Un documento constructivo
  nunca se lee en silencio con otra autoridad, ni pierde su registro al guardarlo un lector
  anterior; tampoco cambia en silencio el resultado de un archivo antiguo (`AGENTS.md` §11.6).

### 2.6 Tabla de clase y puntos sobre caminos

- **`constructive-class/v1`.** Enumera los algoritmos con semántica exacta: rectas, segmentos y
  rayos por dos puntos; paralelas y perpendiculares; intersecciones recta–recta y
  recta–circunferencia con la selección heredada; puntos medios; circunferencias por centro y
  punto o por centro y radio; puntos sobre caminos; y los programas de clase v3 de los loci. La
  pertenencia a la clase la decide solo la clase del algoritmo, nunca un valor. Un objeto fuera de
  la clase entra en el cálculo exacto por su sustituto de frontera, declarado como premisa no
  constructible.
- **Puntos sobre caminos (D-AUT-3).** El dato persistido de un punto sobre camino son sus
  coordenadas. Su significado ideal es el punto del camino ideal que le asigna la aplicación de
  camino heredada (`pointChanged` y después `pathChanged`), evaluada exactamente sobre el camino
  ideal. Es la semántica heredada generalizada: intrínseca, determinista y reproducible.
- **Lo que no se usa como prueba.** El censo 160/160 del estudio usó la lectura anclada. No es
  prueba de la geometría ideal y queda solo como prueba de regresión de la puerta G-AUT-1. La
  certificación genérica de §5 no depende de la lectura elegida: basta con que su caja contenga el
  valor ideal (§5.3).

### 2.7 Textos candidatos AUT-1 y AUT-2 — autoridad constructiva y política de documento

- **Destino:** especificación nueva `geocedg/specs/locus/locus-v2-constructive-authority.md`, que
  creará la implementación tras la aprobación: AUT-1 es su §1 a §3 y AUT-2 su §4 y §5; ADR 0040
  (§9).
- **Naturaleza:** nuevo; sustituye, solo en los documentos que adoptan la política, la autoridad
  de los objetivos (`locus-v2-intersections.md` §2.1) y de la configuración de decisión
  (`locus-v2-certified-construction-model.md` §11.2–§11.3), como detalla §7.
- **Depende de:** D-AUT-1 y D-AUT-3 (AUT-1); D-AUT-2 (AUT-2).
- **Texto candidato:**

  > **§1 Single geometric authority.** Under the document policy `constructive-authority/v1`,
  > every object has exactly one geometric meaning: the ideal object that its construction defines
  > from the free inputs. The binary64 value of a free input is its definition. The meaning is a
  > pure function of the construction graph, the free inputs, the class table
  > `constructive-class/v1` and the semantics of points on paths. It does not depend on the command
  > that consumes the object, on a driver, on a captured configuration or on the boundary of a
  > reconstruction. Every numerical state of an object — its binary64 state, a captured
  > configuration, an enclosure, a boundary substitute — is a representation with a declared
  > numeric guarantee and never decides a relation of the object.
  >
  > **§2 Inherited algorithms.** Inherited algorithms are unchanged computation engines. Their
  > outputs represent the ideal outputs. A relation that an algorithm establishes by provenance
  > holds strictly for the ideal objects whenever they exist, whatever the error of the
  > representations. The existence of an ideal output — the validity of its construction, its
  > membership of a limited part and its branch — is a separate fact, decided rigorously by
  > margin, by structure or by a constructive identity certificate, and `NOT_ESTABLISHED`
  > otherwise; an inherited tolerance never decides it. Inherited commands that decide relations
  > (such as `Relation`, `AreEqual` or the generic prover) keep their behaviour; their verdicts are
  > results of the representation or generic truths over the complex numbers, and they are never
  > integrity evidence.
  >
  > **§3 Class table and boundary.** Membership of an object in the exact class is decided by the
  > class of its algorithm only. An object outside the class enters an exact evaluation through its
  > binary64 state as a declared boundary substitute, which is a premise of every relation whose
  > evidence uses it. The ideal point of a point on a path is the point of the ideal path assigned
  > to its persisted coordinates by the inherited path map evaluated exactly on the ideal path.

  AUT-2:

  > **§4 Document policy.** The policy is a versioned, additive document record. A document
  > without it is governed by `legacy-representation-authority/v1`, the contracts approved before
  > this amendment, and no ordinary operation changes it. A new document is created under
  > `constructive-authority/v1`. An existing document adopts it only through the explicit semantic
  > event `AUTHORITY_POLICY_ADOPTION`, which reports its impact before it is applied, is atomic and
  > undoable, changes no identity, token or selector, withdraws contradicted results with a typed
  > cause and never reassociates them. One document has one authority. The authority is never
  > chosen per relation, per object or per command. Creating a construct that requires the
  > constructive policy in a legacy document requires the adoption event or fails closed. A
  > reader that does not support the record fails closed with a typed cause and loads nothing; it
  > never reads the document under another authority.
  >
  > **§5 What is persisted.** The policy record is persisted. Integrity, transfer records,
  > certificates and their evidence are revision evidence, recomputed and never persisted.

## 3. Punto 2 — Transferencia de certificados de `CU2`

### 3.1 Lo que se conserva sin reinterpretación

- **Certificados.** Su texto, su naturaleza de evidencia (ADR 0038, decisión 18), su garantía y
  su alcance: deciden la configuración de las constantes binary64 capturadas, nunca una relación
  ideal (ADR 0038, decisión 7, sin cambios).
- **Huella de revisión** (ADR 0038, decisión 17) y fidelidad del corte, comparadas por bits.
- **Identidades.** Claves de linaje, incluidas las que llevan extremos hexadecimales cuando un
  extremo no es estructural; direcciones de componentes; tokens y selectores.
- **Componentes y estructuras de existencia** tal como `CU2` las publica.

La transferencia no edita nada de lo anterior. Produce un **registro de transferencia**, un hecho
nuevo de la revisión con estos campos:

- el certificado de origen, por su clase, su huella y su clave estructural;
- la versión de la autoridad;
- la vía de la transferencia;
- las premisas;
- el resultado.

### 3.2 Condiciones necesarias y suficientes

Sea `C` un certificado de `CU2` con enunciado `S` sobre la configuración capturada: `S(Σ_L)` está
certificado. Transferir `C` es establecer `S(Σ_C)`, el mismo enunciado sobre los mismos objetos
con su significado constructivo. Un certificado consta de varias decisiones (cajas, transiciones,
conteos, simetrías), y unas dependen de otras: un pliegue, por ejemplo, exige la continuidad de
su componente. **La política admite la transferencia de `C` si y solo si:**

- **(N1) Correspondencia estructural.** El programa capturado (clase v3, estados de selección
  heredados, estados de etiqueta, salidas efectivas y definiciones estructurales: todo lo que la
  huella cubre salvo los valores numéricos) es el programa del objeto constructivo, con el mismo
  corte del grafo y las mismas ramas.
- **(N2) Misma revisión.** El registro cita la huella de la revisión en que se consume y las mismas
  versiones de contrato.
- **(N3) Vía admitida, decisión por decisión.** Cada decisión de `C`, en el orden de sus
  dependencias, queda establecida rigurosamente en `Σ_C` por una de estas vías, que el registro
  nombra; `C` se transfiere solo si se transfieren todas:
  - **Margen** (`TRANSFERRED_BY_MARGIN`), para las decisiones abiertas: desigualdades estrictas
    certificadas con margen. Son las cajas válidas, las uniformemente inválidas en desigualdades
    estrictas, las transiciones por cambio de signo con derivada que excluye el cero y las celdas.
    Su programa de intervalos se vuelve a evaluar con cada constante capturada sustituida por un
    **encierro de transferencia**, que contiene a la vez su valor capturado y un encierro riguroso
    de su valor constructivo. Todas las decisiones deben conservar su signo estricto.
  - **Estructura** (`TRANSFERRED_BY_STRUCTURE`), para las decisiones cerradas que son consecuencia
    de la estructura: la simetría de pliegue (`fold-symmetry/v1`), la totalidad y la periodicidad
    estructurales, y los coeficientes exactamente nulos que corresponden a relaciones de
    procedencia. Se transfieren cuando todas sus premisas estructurales son relaciones `STRICT`
    bajo la autoridad constructiva.
  - **Nueva decisión** (`TRANSFERRED_BY_REDECISION`), para las decisiones cerradas sobre bits que
    una perturbación puede romper: conteos de Sturm y multiplicidades, componentes puntuales,
    parámetros singulares y valores críticos como el de la mordedura. Se decide otra vez la
    decisión en `Σ_C` por un método riguroso de los contratos de `CU2` y `CU3`: evaluación exacta
    con premisa constructible; una nueva certificación por intervalos cuando el hecho es de
    multiplicidad uno (un conteo de raíces simples, con cambio de signo y derivada que excluye el
    cero); o un certificado de identidad constructiva (§5) con sus condiciones certificadas.

**Por qué son necesarias.**

- Sin (N1), el certificado habla de otro programa.
- Sin (N2), de otra revisión: un certificado solo vale para la revisión cuya huella cita
  (ADR 0038, decisión 17).
- Sin (N3), `S(Σ_C)` no está establecido.
- La lista de vías de (N3) es cerrada **por decisión normativa**, no por un teorema: se admiten
  solo los métodos rigurosos ya contratados. Ni una tolerancia, ni la proximidad de coordenadas,
  ni la robustez observada en un muestreo transfieren nada. La estabilidad de las estructuras de
  `e_1` y `o_1` ante perturbaciones de un ulp (estudio §1.4) es evidencia, no transferencia. Un
  método riguroso nuevo entraría en la lista con una versión nueva de la política.

**Por qué son suficientes.** Cada vía es una prueba de su decisión en `Σ_C`:

- el margen, por la monotonía de la aritmética de intervalos;
- la estructura, porque la decisión se deduce de premisas estrictas;
- la nueva decisión, por construcción.

Las decisiones se transfieren en el orden de sus dependencias, así que cada una usa solo
premisas ya transferidas.

### 3.3 Sin transferencia demostrada

| Situación | Resultado | Consumidor que exige `STRICT` | Consumidor que acepta `RELAXED` |
|---|---|---|---|
| (N1) o (N2) fallan | `EVIDENCE_INSUFFICIENT: CORRESPONDENCE_NOT_DEMONSTRATED` | falla cerrado con esa causa | falla cerrado: sin correspondencia, el certificado no es evidencia del objeto |
| Decisión abierta sin margen suficiente | `EVIDENCE_INSUFFICIENT: OPEN_MARGIN_INSUFFICIENT` | falla cerrado | el certificado no basta: la relación solo es `RELAXED` con evidencia propia de §4.2 |
| Decisión cerrada sin estructura ni nueva decisión | `EVIDENCE_INSUFFICIENT: CLOSED_DECISION_NOT_REDECIDED` | falla cerrado | igual que la anterior |
| Premisa estructural no estricta | `EVIDENCE_INSUFFICIENT: STRUCTURAL_PREMISE_NOT_STRICT` | falla cerrado | igual que la anterior |
| Presupuesto agotado | `EVIDENCE_INSUFFICIENT: BUDGET_EXHAUSTED` | falla cerrado | igual que la anterior |
| La nueva decisión refuta `S` en `Σ_C` | `CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION` | falla cerrado | falla cerrado: una contradicción rigurosa nunca es relajadamente válida |

En todos los casos el certificado de origen, sus componentes y sus claves quedan intactos y siguen
valiendo para `Σ_L`. En una degeneración, como el valor crítico de la mordedura, hay dos casos:

- si una nueva decisión rigurosa en `Σ_C` refuta la decisión cerrada de `Σ_L`, el resultado es
  `CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION`, como en la tabla;
- si ninguna vía decide en `Σ_C` (por ejemplo, una multiplicidad con premisa no constructible y
  sin certificado de identidad), la integridad queda en `NOT_ESTABLISHED`.

En ninguno de los dos se toca la estructura de `CU2`.

### 3.4 Lo que `CU3` necesita de `CU2`, y por qué vía

| Resultado de `CU2` | Uso en `CU3` | Vía | En el modelo |
|---|---|---|---|
| Componente certificada que contiene `t*` (validez del corte) | `P = F(t*) ∈ L` | margen | decisiones abiertas con margen; la diferencia entre la constante capturada y la constructiva es del orden de `10⁻¹⁵` (radio² de `c_1`: `−3,66·10⁻¹⁵`, estudio §5). El encierro de transferencia exige encerrar rigurosamente el valor constructivo de las constantes capturadas que dependen de `H`, que no es constructible: por eso esos encierros están en E1 (§6.2) |
| Pliegues de `e_1` en `0` y en `π` (`fold-symmetry/v1:HORIZONTAL`) | contacto en pliegue; certificado de paridad | estructura | simetría estructural (estudio §1.7); el CIC confirma `F′(t*) ≡ 0` (§5.5) |
| Periodicidad estructural y cierre en la costura | costura de `e_1·c_1` y `e_1·r` | estructura | `t* = π` estructural en el CIC |
| Transiciones cercanas a `t*` | vigencia de la componente | margen | — |
| Componentes puntuales, parámetros singulares, multiplicidades | no los consume `CU3-A` (D-CU3-9) | nueva decisión, si un consumidor futuro los necesita | — |
| Huella y fidelidad del corte | condición (N2) | no se transfieren: son el ancla | sin cambios |
| Claves de linaje, tokens y selectores | identidad | no se transfieren: no son evidencia | sin cambios |

### 3.5 Texto candidato AUT-4 — transferencia de certificados de `CU2`

- **Destino:** `locus-v2-certified-construction-model.md`, nueva sección «Constructive authority
  amendment (version 1.4)», §12.2; `locus-v2-existence-components.md`, nota de consumidor.
- **Naturaleza:** nuevo; precisa el ADR 0038 sin cambiar su texto, que sigue diciendo lo que
  decide un certificado.
- **Depende de:** D-AUT-4.
- **Texto candidato:**

  > **§12.2 Transfer of CU2 certificates (`cu2-certificate-transfer/v1`).** A CU2 certificate
  > remains, unchanged in text, fingerprint, structure and lineage keys, a certificate of its
  > captured configuration `Σ_L`. Transferring it to the constructive authority establishes the
  > same statement in the constructive configuration `Σ_C` of the same objects. The policy admits
  > the transfer of a certificate if and only if (N1) the captured program is structurally the
  > program of the constructive object, with the same slice and branches (class-v3 program,
  > inherited selection states, label states, effective outputs and structural definitions), (N2)
  > the transfer cites the fingerprint of the consuming revision and the same contract versions,
  > and (N3) every decision of the certificate, in the order of its dependencies, is established in
  > `Σ_C` by an admitted route named by the transfer record:
  >
  > - *margin*, for open decisions: their interval program, re-evaluated with every captured
  >   constant replaced by an enclosure of both its captured value and a rigorous enclosure of its
  >   constructive value, keeps every strict sign;
  > - *structure*, for closed decisions implied by structure (fold symmetry, structural totality
  >   and periodicity, exact zeros that correspond to provenance relations), when every
  >   structural premise is `STRICT` under the constructive authority;
  > - *re-decision*, for closed decisions on captured bits (Sturm counts and multiplicities, point
  >   components, singular parameters, critical values): the decision is taken again in `Σ_C` by a
  >   rigorous method of the CU2 and CU3 contracts — exact evaluation with a constructible premise,
  >   a new interval certification of a fact of multiplicity one, or a constructive identity
  >   certificate.
  >
  > The list of routes is closed by this policy: no tolerance, coordinate proximity or sampled
  > robustness transfers a certificate, and a new rigorous route needs a new policy version.
  > Without a demonstrated transfer the result is `EVIDENCE_INSUFFICIENT` with a typed cause, and
  > every consumer that requires `STRICT` fails closed. A re-decision that refutes the statement
  > gives `CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION`. A transfer record is revision
  > evidence: it cites the source certificate by kind, fingerprint and structure key, the
  > authority version, the route and the premises, and it is never persisted. No source
  > certificate, component, fingerprint or lineage key is modified or reinterpreted.

## 4. Punto 3 — Integridad `RELAXED`

### 4.1 Tres hechos distintos

| Hecho | Estado | Ejemplo en la evidencia |
|---|---|---|
| **Evidencia relajada**: evidencia positiva relativa a los objetos constructivos, admitida por una política versionada, sin contradicción rigurosa | `RELAXED` | igualdad exacta genérica de los cinco contactos: con el sustituto binary64 de `H` y en doce posiciones racionales de `H` (estudio §1.6 y §1.7, 60 de 60) |
| **Contradicción rigurosa**: una prueba de que la relación es falsa para los objetos constructivos | `CONTRADICTED` | `J_1` fuera del segmento `n` con `D_1` ×3, certificado (§5.5, nuevas pruebas) |
| **Ausencia de evidencia**: ni prueba, ni admisión, ni contradicción | `NOT_ESTABLISHED` | una condición de signo indecidible, un objeto fuera de la clase, el presupuesto agotado o una identidad que no se cumple formalmente sin refutación |

Las tres son exclusivas. `STRICT` es la cuarta, la de la prueba. Que el residuo de una relación no
sea idénticamente nulo **no** es una contradicción: puede cumplirse por casualidad en la
configuración (control C3 de §5.5). Contradecir exige una refutación rigurosa.

### 4.2 Política positiva `constructive-relaxed-admission/v1`

Una relación `R` de una revisión es `RELAXED` si y solo si se cumplen las cuatro condiciones:

- **(a) Alcance.** `R` es una incidencia o una pertenencia entre objetos constructivos, para un
  consumidor que declara aceptar `RELAXED`.
- **(b) Sin contradicción.** Ninguna evidencia rigurosa de la misma revisión, respecto de la
  autoridad constructiva, refuta `R`. Cuentan como tal:
  - un encierro del residuo constructivo o del parámetro de pertenencia que excluye la relación;
  - una decisión exacta con premisa constructible;
  - una no pertenencia certificada a la parte limitada;
  - un certificado de conteo de raíces de CU3.4 relativo al objetivo constructivo;
  - un certificado de paridad por simetría exacta.

  En A1, una refutación exacta cuya premisa contiene un sustituto no constructible **no** es
  rigurosa respecto del objeto ideal (estudio §2.4, precisión 4). Deja la relación en
  `NOT_ESTABLISHED`: no sirve ni para admitir ni para retirar.
- **(c) Evidencia positiva** de una de estas clases, relativas a los objetos constructivos:
  - **(c1) `SUBSTITUTE_GENERIC`.** `R` se cumple exactamente en la evaluación exacta en que cada
    objeto no constructible de la frontera se sustituye por su sustituto binary64 declarado, y
    también en un número versionado (8 en v1) de perturbaciones racionales de cada sustituto
    dentro de la misma celda. En todas esas evaluaciones, cada predicado de validez, rama y
    pertenencia se cumple con margen estricto certificado y con el mismo signo.
  - **(c2) `ENCLOSURE_COMPATIBLE`.** Un encierro riguroso del residuo constructivo de `R`,
    normalizado como en `locus-v2-intersections.md` §8.1, contiene el cero y queda dentro de la
    tolerancia versionada `τ_v1 = 2·10⁻¹² + 2·10⁻¹²·max(1, S)`. Es el umbral que G8B-R1 aplica
    hoy, con su escala `S` por familia: 1 para rectas, segmentos y rayos, y el radio para
    circunferencias (estudio de precisión y tolerancias de `CU0`, §1). Aquí se aplica al residuo
    del objeto constructivo y no al de su representación. **Solo vale para una raíz cuyo corchete
    de cambio de signo está verificado contra la representación**, es decir, transversal. Si el
    corchete se vuelve a certificar contra el objetivo constructivo, ya no es (c2): es existencia
    certificada (CU3.1 (a)) y la raíz es `STRICT` (§4.4). Un contacto sin cambio de signo, sea
    tangencia o pliegue, nunca es `RELAXED` por (c2): un residuo pequeño no prueba una raíz
    (`OBS-CU0-CU3-TANGENT-EXISTENCE`), y la clase 3 de `CU3` sigue sin materializarse por
    tolerancia.

  **Por qué (c1) es evidencia del objeto y no del sustituto.** La igualdad exacta en un solo
  sustituto no lo es: el control C3 de §5.5 (una igualdad que vale exactamente en el sustituto de
  `I` y no como identidad) la cumpliría, y para un contacto tangente equivaldría a admitir una
  existencia a `10⁻¹⁶` del objeto. La versión genérica exige que la relación no dependa del valor
  del sustituto dentro de su celda, y el valor ideal es un punto más de esa familia. No es una
  prueba: un número finito de puntos no demuestra una identidad, y la demostración es el
  certificado de §5, que la hace `STRICT`. La asimetría con (b) es deliberada. Una igualdad que se
  mantiene al mover el sustituto apunta a una relación estructural. Una refutación en el sustituto
  solo dice que la relación no es una identidad en la familia de sustitutos, y puede deberse a una
  propiedad exacta del valor ideal que la familia no conserva, como la isometría de un giro.
- **(d) Registro.** Se registran la clase de evidencia, los sustitutos usados (las premisas no
  constructibles), las premisas y la versión de la política.

### 4.3 Lo que `RELAXED` nunca es

- **No es una prueba.** Nunca se presenta como demostración, y la interfaz la distingue de
  `STRICT`.
- **No es premisa de `STRICT`.** Por propagación por premisas, toda derivación que la usa es como
  mucho `RELAXED` (§4.6).
- **No es identidad.** No entra en tokens, selectores ni en el grafo, y no se persiste.
- **Cambia sin reasociar:**
  - baja a `NOT_ESTABLISHED` o a `CONTRADICTED` cuando aparece una contradicción, sin reasociar
    (D-CU3-7);
  - sube a `STRICT` cuando un certificador lo demuestra, sin cambiar ninguna identidad.
- **No blanquea una contradicción.** Lo que está `CONTRADICTED` respecto de la autoridad del
  documento no pasa nunca a `RELAXED`, sea cual sea su residuo binary64.

### 4.4 G8B-R1: precedente sin transferencia automática

**Qué es G8B-R1.** Es la verificación local de raíces transversales de G8B: residuo
`≤ 2·10⁻¹² + 2·10⁻¹²·max(1, S)` contra el **adaptador binary64** del objetivo, con evidencia de
aislamiento (`LOCALLY_VERIFIED`, CU3.4). Su admisión es relativa a la autoridad de representación.

**Alcance.** La recalificación solo afecta a las raíces que G8B-R1 admitió en documentos
existentes, que son transversales: el motor vigente no materializa raíces tangentes. Las formas de
`CU3` nacen bajo la autoridad constructiva (§2.3) y sus contactos se deciden directamente por
§4.2 y §5, sin nada que recalificar.

**Lo que se conserva:**

- en los documentos con `legacy-representation-authority/v1`, G8B-R1 y sus umbrales siguen
  exactamente como están aprobados;
- en todos, G8B-R1 sigue siendo el buscador de candidatos y el precedente de verificación local.

**Por qué no se transfiere sin justificación.** La admisión es relativa a una autoridad. La
distancia entre la representación y el objeto constructivo es del mismo orden que el redondeo para
el que se calibró la tolerancia: en el modelo, `10⁻¹⁶` entre `P_C` y `P_L` (estudio §1.7). Por
tanto, una admisión respecto de la representación no implica nada respecto del objeto. Hay que
volver a decidirla, y hacerlo es barato.

**Vías justificadas** en un documento con `constructive-authority/v1`, por orden:

1. **`STRICT`**, si el corchete de cambio de signo de la raíz se vuelve a certificar contra el
   objetivo constructivo: encierros del residuo constructivo en los extremos con signos opuestos
   estrictos, y encierro de la derivada sin el cero. Es mejor que heredar: es existencia
   certificada.
2. **`RELAXED`**, por (c2) `ENCLOSURE_COMPATIBLE` y sin contradicción.
3. **`NOT_ESTABLISHED`** en otro caso. La raíz no se publica como admitida por la política
   constructiva.

Si una contradicción rigurosa la refuta, la raíz se retira con causa tipada, sin reasociación. Al
adoptar la política, el informe de impacto (§2.5) enumera las raíces de cada clase.

**Conflicto con contratos aprobados, que se declara.** CU3.4 dice que, sin contradicción, la
admisibilidad no cambia, y D-CU3-6, D-CU3-7 y D-CU3-13 conservan las capacidades de G8B-R1 fuera
de las contradicciones rigurosas. Esta propuesta lo mantiene sin cambios bajo
`legacy-representation-authority/v1`. Bajo `constructive-authority/v1`, en cambio, una raíz sin
justificación pasaría a `NOT_ESTABLISHED` sin haber sido contradicha, porque la instrucción
autoral pide no transferir la admisibilidad entre autoridades sin justificación. Para acotar ese
cambio:

- solo ocurre una vez, en el evento explícito de adopción, versionado, informado antes de aplicarse
  y reversible con Deshacer;
- después, dentro del documento constructivo, la admisibilidad vuelve a cambiar solo por una
  contradicción rigurosa o por evidencia nueva de la revisión;
- la recalificación por corchete constructivo es barata y, salvo degeneraciones, da `STRICT`.

Es una revisión de CU3.4 y de esas decisiones para la política nueva, que D-AUT-6 y D-AUT-11
someten al autor.

### 4.5 Consumo y presentación

- Cada consumidor declara la integridad mínima que exige. Uno que exige `STRICT` falla de forma
  explícita, con causa tipada, si no la recibe.
- La integridad de un objeto es la peor de las relaciones en que se apoya su definición. Se
  muestra con la lista de sus premisas `RELAXED`, separada siempre de la precisión de su
  representación.

### 4.6 Propagación por premisas

La regla es la del estudio (§2.4, precisión 3), en forma normativa:

- cada relación lleva su método y sus premisas (relaciones de procedencia, relaciones certificadas
  y hechos de existencia);
- su integridad es la de su método, acotada por la peor de sus premisas;
- una premisa `CONTRADICTED` anula la derivación: lo que dependía de ella pasa a `NOT_ESTABLISHED`,
  y a `CONTRADICTED` solo si se refuta por sí mismo;
- una incidencia de procedencia es `STRICT` aunque una relación lejana sea `RELAXED`: solo hereda lo
  que su prueba usa.

**Relación con los vocabularios existentes**, que no cambian:

- **Hacia `STRICT`** solo cuenta la evidencia rigurosa respecto del objeto constructivo:
  procedencia con existencia establecida (§2.2), certificados de `CU2` transferidos (§3),
  evidencia exacta o de intervalos rigurosos de la naturaleza que fija el ADR 0038 (decisión 18)
  decidida en `Σ_C`, los grados `EXISTENCE_CERTIFIED`, `EXISTENCE_STRUCTURAL` y `EXISTENCE_EXACT`
  de CU3.4 y las clases de contacto 1 y 2 de CU3.2 decididos contra el objetivo constructivo, y
  los certificados de §5. Una clase 2 obtenida con sustitutos no constructibles es (c1), es decir,
  `RELAXED`.
- **Hacia `RELAXED`** solo cuentan (c1) y (c2) de §4.2. Una verificación numérica bajo hipótesis,
  un grado `LOCALLY_VERIFIED` o una clase métrica 3 (`ESTIMATED`, ADR 0037) no bastan por sí
  solos: son ejes de evidencia o de precisión, y `LOCALLY_VERIFIED` se recalifica por §4.4.
- **Las métricas** conservan su propia admisibilidad (ADR 0037); la admisión `RELAXED` solo se
  aplica a incidencias y pertenencias.
- **`NumericGuarantee`** sigue siendo el eje de la precisión de una representación y no se
  corresponde con la integridad.

### 4.7 Textos candidatos AUT-3 y AUT-5 — integridad y política relajada

- **Destino:** `locus-v2-constructive-authority.md` §6 (integridad) y §7 (política `RELAXED`);
  `locus-v2-intersections.md`, nota en CU3.4.
- **Naturaleza:** nuevo; amplía CU3.4 sin cambiar G8B-R1 en los documentos antiguos.
- **Depende de:** D-AUT-5, D-AUT-6 y D-AUT-9.
- **Texto candidato:**

  > **§6 Integrity of relations (`relation-integrity/v1`).** Every relation decided under the
  > constructive authority carries an integrity of its own type, `RelationIntegrity`, never a value
  > added to an existing enumeration: `STRICT` (proved or rigorously certified for the
  > constructive objects), `RELAXED` (admitted by a versioned positive policy without rigorous
  > contradiction), `NOT_ESTABLISHED` (neither proved, admitted nor contradicted) or `CONTRADICTED`
  > (rigorously refuted for the constructive objects). Integrity is independent of the numeric
  > guarantee of any representation. A relation carries its method and premises; its integrity is
  > that of its method bounded by the worst of its premises; a `CONTRADICTED` premise voids the
  > derivation, whose result is `NOT_ESTABLISHED` unless it is itself refuted. A residual that is
  > not identically zero is not a contradiction. A consumer declares the minimum integrity it
  > requires and fails explicitly with a typed cause when it is not met. Integrity is revision
  > evidence; it never enters a token, a selector or the construction graph and is never
  > persisted.
  >
  > **§7 Relaxed admission (`constructive-relaxed-admission/v1`).** A relation is `RELAXED` if
  > and only if (a) it is an incidence or a membership between constructive objects requested by a
  > consumer that accepts `RELAXED`; (b) no rigorous evidence of the same revision refutes it for
  > the constructive objects — an exact refutation whose premise contains a non-constructible
  > boundary substitute is not rigorous for the ideal object and leaves the relation
  > `NOT_ESTABLISHED`; (c) it has positive evidence of a declared kind: `SUBSTITUTE_GENERIC` (the
  > relation holds exactly in the exact evaluation with the declared binary64 substitutes of the
  > non-constructible boundary and in a versioned number of rational perturbations of every
  > substitute within the same cell — eight in version 1 — and every validity, branch and
  > membership predicate of those evaluations holds with a certified strict margin and the same
  > sign) or `ENCLOSURE_COMPATIBLE` (only for a root whose sign-change bracket is verified against
  > the representation: a rigorous enclosure of the normalized constructive residual contains zero
  > and lies within `2e-12 + 2e-12 max(1, S)`, the G8B-R1 threshold with its scale `S` per target
  > family; a contact without a sign change is never relaxed by a residual bound); and (d) the
  > record names the evidence kind, the substitutes, the premises and the policy version.
  > `RELAXED` is never presented as a proof, never becomes `STRICT` through a tolerance, a
  > coordinate coincidence, persistence or another revision, and a `CONTRADICTED` relation never
  > becomes `RELAXED`.
  >
  > **§7.4 G8B-R1 under the constructive authority.** The G8B-R1 local verification, relative to
  > the binary64 target adapter, is unchanged in documents governed by
  > `legacy-representation-authority/v1` and remains the candidate search everywhere. Its
  > admission is not inherited by the constructive authority. In a document governed by
  > `constructive-authority/v1`, a root it verifies is `STRICT` when its sign-change bracket is
  > re-certified against the constructive target, `RELAXED` by `ENCLOSURE_COMPATIBLE` without
  > contradiction, and otherwise `NOT_ESTABLISHED`; a root that rigorous evidence contradicts is
  > withdrawn with a typed cause and never reassociated. This re-qualification happens once, in
  > the explicit adoption event, which reports it before it is applied; afterwards, the
  > admissibility of a root changes only by rigorous contradiction or new evidence of the
  > revision.

## 5. Punto 4 — Certificación estructural genérica

### 5.1 La capacidad

Un **certificado de identidad constructiva** (CIC, `constructive-identity-certificate/v1`) recibe
una relación `R` entre objetos de la clase (incidencia de un punto con una recta, un segmento, un
rayo o una circunferencia, y la pertenencia a su parte limitada), opcionalmente con un conductor
fijado en un punto exacto de su camino. Devuelve uno de estos resultados:

| Resultado | Significado | Integridad |
|---|---|---|
| `IDENTITY_CERTIFIED` | el residuo es el elemento nulo del cuerpo y todas las condiciones de signo están certificadas en la caja de decisión | `STRICT` en la celda (§5.4) |
| `LIMITED_PART_EXCLUDED` | identidad con el soporte y no pertenencia certificada a la parte limitada | `CONTRADICTED` para el objetivo limitado |
| `NOT_AN_IDENTITY` | el residuo no es formalmente nulo; no es una refutación | `NOT_ESTABLISHED` (puede pedirse una refutación por encierro) |
| `PRECONDITION_UNDECIDED` | una condición de signo no se certifica en la caja | `NOT_ESTABLISHED` |
| `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED` | una rama elegida fuera del corte del conductor no se certifica correspondiente a la salida viva del algoritmo heredado | `NOT_ESTABLISHED` |
| `INVALID` | una condición de validez se certifica falsa: un objeto de la relación no existe | la relación queda `NOT_ESTABLISHED` con causa `OBJECT_UNDEFINED`, y la existencia del objeto ideal es un hecho `CONTRADICTED`; falla cerrado |
| `OUT_OF_CLASS`, `BUDGET_EXHAUSTED` | fuera de la clase o del presupuesto versionado | `NOT_ESTABLISHED` |

### 5.2 Fundamento y corrección

**Evaluación.** Cada objeto se evalúa como elemento de `K = Q(V)[√Δ]`, el cuerpo de funciones
racionales de las indeterminadas `V` con coeficientes racionales, ampliado por una raíz cuadrada
`√Δ` que no es un cuadrado. Las constantes son las entradas libres binary64 tomadas como racionales
exactos. Las operaciones son las de regla y compás de la tabla de clase.

**Decisiones.** Cada decisión de rama, validez, pertenencia y no degeneración es una condición de
signo sobre un elemento de `K`:

- puntos distintos y rectas no paralelas;
- discriminantes y radios positivos;
- divisores no nulos;
- la rama de cada raíz cuadrada;
- los parámetros de segmentos y rayos;
- la selección heredada de cada intersección.

Cada signo se certifica por evaluación de intervalos con redondeo hacia fuera en una caja `X` de
las indeterminadas, y se registra. Una circunferencia pertenece a la clase por el algoritmo que la
define, nunca por su valor actual.

**Ramas.** Cada salida de una intersección recta–circunferencia es la que da la regla heredada con
el estado de selección capturado. Hay dos casos:

- **Fuera del corte de un conductor sustituido** (el punto `D`, los objetivos y sus ancestros, o
  cualquier relación sin conductor), la salida viva del algoritmo heredado se calcula en la misma
  configuración. Se exige que esa salida binary64 esté certificada, en toda la caja, como
  estrictamente más cercana a la rama formal elegida que a la otra. Si no, el resultado es
  `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`, sin ninguna tolerancia. Así se cubre también el caso
  especial heredado de un punto ya incidente, que el algoritmo coloca en un índice propio.
- **Dentro del corte** (`H_1` y `G_1` en el modelo), la salida viva corresponde a otra posición
  del conductor y no es comparable. Rige el estado de selección capturado, como en el evaluador de
  clase v3 de `CU2`, cuya fidelidad al algoritmo vivo certifica `CU2`
  (`locus-v2-certified-construction-model.md` §10.2).

Las intersecciones en modo continuo y las selecciones por punto de referencia quedan fuera de la
clase, como en §11.2 de ese modelo.

**Proposición.** Sea `X` una caja (convexa y conexa) en la que todas las condiciones registradas
tienen su signo certificado, incluida `Δ > 0`. Para todo `v ∈ X`:

- cada operación de la construcción es válida en `v`;
- las ramas y las pertenencias elegidas son las del estado de selección capturado;
- el valor real de cada objeto es una función continua en `X`.

Si además el residuo de `R` es el elemento nulo de `K`, `R` se cumple en todo `v ∈ X`.

**Demostración (esbozo).** Se razona por inducción sobre la construcción:

- Las condiciones de validez en todo `X` hacen continua cada operación real en `X`.
- Su expresión formal coincide con ella en el abierto denso `U ⊂ X` donde ningún denominador formal
  se anula. Es denso porque los denominadores son polinomios no nulos.
- `√Δ` denota la raíz positiva, como la evalúan los intervalos. La condición de rama registrada
  elige, para cada raíz cuadrada de la construcción, el signo que la hace positiva.
- El residuo real es continuo en `X` y nulo en `U`, luego nulo en `X`. ∎

**Corolario para el producto.** La relación vale para los objetos que muestra el documento si,
además:

- la estructura del cono de dependencias es la que la prueba registró (§5.4);
- las ramas corresponden a las del algoritmo heredado, en el sentido de «Ramas»;
- `X` contiene el valor ideal de las indeterminadas.

**El valor ideal tiene que estar en la caja.** Para una entrada libre, el valor binary64 es el
ideal. Para un punto sobre camino y para un corte no lo es, y en la producción ese valor se encierra
rigurosamente. Por ejemplo, el corte de `H` se encierra evaluando por intervalos su subgrafo no
constructible, con un coseno y un seno de redondeo hacia fuera del ángulo binary64.

El prototipo no calcula esos encierros. Se apoya en que la caja certificada (semianchura hasta
`10⁻¹`) excede en muchos órdenes la distancia entre la representación y el valor ideal. El
redondeo de unas pocas operaciones binary64 deja esa distancia en torno a `10⁻¹⁴` como mucho; es
una estimación, no una medida. Los encierros son un requisito de E1 (§6).

**Completitud.** Faltan un máximo común divisor y una extracción exhaustiva de cuadrados
perfectos, así que la representación formal no es canónica. Eso puede ocultar una identidad
(`NOT_AN_IDENTITY`) o dejar un signo sin decidir. Nunca produce un certificado falso:

- un elemento formalmente nulo es nulo;
- `INVALID` exige un signo certificado o una nulidad formal.

**Encierros.** Los signos se certifican con una forma de valor medio de segundo orden de cada
polinomio: valor y gradiente racionales exactos en el centro de la caja, y encierro ingenuo de las
segundas derivadas. La evaluación ingenua de polinomios expandidos pierde el signo por cancelación,
incluso en cajas de `10⁻⁹`: el prototipo lo midió y lo corrigió así.

### 5.3 Regla de frontera automática y entradas no constructibles

La regla no contiene código del modelo:

- **Entradas libres.** Un punto o un número libre es una o dos indeterminadas. Los ejes son
  constantes.
- **Puntos sobre caminos.** Un punto sobre una recta constructible es una indeterminada: una
  coordenada libre y la otra despejada de la recta exacta.
- **Cortes.** Se corta, es decir, se convierte en punto libre del plano, todo punto calculado
  fuera de la clase, y todo punto sobre un camino que depende de objetos fuera de la clase. Una
  recta que pasa por un punto cortado es una recta arbitraria, así que mantener el punto sobre ella
  añade indeterminadas sin información.

**Un corte olvida y nunca inventa.** Si una identidad se cumple con el punto libre, se cumple con
cualquier valor suyo, incluido el ideal no constructible. Si la identidad necesita la relación
olvidada, el resultado es `NOT_AN_IDENTITY` o `NOT_ESTABLISHED`, nunca una afirmación falsa: es
incompletitud honesta, que muestra la fila «limitation» de §5.5.

**El caso de `H`.** `H = Point[j]` con `j = Line[A, C']` y `C' = Rotate[C, 47°, A]`. La regla
corta `H` como punto libre, y las cinco identidades se cumplen. Por eso, dentro de su celda y con el
valor ideal de `H` en la caja, la prueba no depende de esa posición exacta no constructible.

### 5.4 Celdas, recomprobación y dinámica

- **La celda de una prueba.** Una prueba fija el conjunto de configuraciones en que se cumplen sus
  condiciones de signo registradas y los estados de selección heredados que usó. Es un conjunto
  semialgebraico abierto, y la identidad vale en toda ella. Que una configuración pertenece a la
  celda se certifica, revisión a revisión, recomprobando los signos registrados en una caja pequeña
  alrededor de sus valores vivos, sin trabajo simbólico.
- **Fallo cerrado de la recomprobación.** La recomprobación falla cerrada si:
  - cambia la estructura del cono de dependencias: un objeto redefinido, eliminado o sustituido
    por otro con la misma etiqueta, o un cambio de lo que se corta (`NOT_APPLICABLE`);
  - cambia una constante de la prueba (`NOT_APPLICABLE`: hace falta una prueba nueva);
  - cambia un estado de selección;
  - un signo no se certifica igual;
  - una rama fuera del corte del conductor deja de corresponder a la salida viva.

  Los signos solos no bastan. En la construcción S4 de §5.5, una circunferencia se sustituye por
  otra con la misma etiqueta: los signos registrados seguirían aceptando la prueba antigua, y la
  comprobación estructural la rechaza.
- **Otra celda.** La configuración puede estar en otra celda, con otro vector de signos. Se
  intenta entonces una prueba nueva, que cuesta milisegundos en el modelo. Las celdas son finitas
  y se demuestran bajo demanda.
- **Dinámica.** Arrastrar una entrada solo exige recomprobar. Cambiar de celda exige probar de
  nuevo, y si no se puede, la integridad queda en `NOT_ESTABLISHED`.

### 5.5 Evidencia del prototipo

Los resultados están en el archivo JSON citado arriba. Las cifras de trabajo son contadores
deterministas de productos de términos polinómicos. Los tiempos, la medición de la evaluación
ingenua y algunas magnitudes geométricas citadas aparte (como `|HM| − |HD_1|`) no están en él:
son cálculos o ejecuciones de desarrollo, a título informativo.

**Incidencias obligatorias.** La tabla da el alcance, las indeterminadas y el resultado:

| Alcance | Indeterminadas | Constantes | Resultado |
|---|---|---|---|
| frontera | `B.y`, `H.x`, `H.y` | `A`, `D_1`, `rbCone` | las cinco incidencias, `IDENTITY` certificada; `t* = 0` (`K`) o `π` (`J`) para todo valor de las indeterminadas; extensión `√Δ` con `Δ` de grado 4 y 13 términos; entre 42 y 60 condiciones de signo; las ramas fuera del corte del conductor (`K` o `J`, y `M_1` en `o_1·h_2`) corresponden a la salida viva, y las del corte (`H_1` y `G_1`) siguen el estado de selección capturado; certificadas en cajas de semianchura `10⁻⁹`, `10⁻⁶`, `10⁻³` y `10⁻¹`; entre 948 y 2 854 productos de términos |
| automático | `A.x`, `A.y`, `B.y`, `rbCone`, `H.x`, `H.y`, `D_1.x`, `D_1.y` | ninguna (solo el eje) | las cinco, `IDENTITY` certificada, con las mismas ramas contrastadas; corte registrado: `H`, por depender su camino de la rotación; certificadas hasta `10⁻¹`, salvo `o_1·h_2` en `10⁻¹`, donde no se certifica que `M_1` quede antes del extremo del segmento (su margen geométrico, `|HM| − |HD_1| ≈ 2,5`, apunta a sobreestimación de los intervalos); entre 1 511 y 687 481 productos |

**Orden de contacto, por jets exactos en los dos alcances.**

- En los cinco contactos, `h′ ≡ 0` y `h″` tiene signo certificado. Los signos coinciden con los
  valores exactos del estudio (§1.7: `−28,5`, `−24,7`, `+2,57`, `−7,99` y `+16,2`).
- `F′(t*) ≡ 0` en `e_1` en `K` y en `J`: el punto trazado es estacionario. Es coherente con los
  pliegues que `CU2` certifica por simetría exacta, pero el CIC no certifica un pliegue por sí
  solo: eso exige las condiciones del ADR 0038, decisión 8.
- `|F′(t*)| > 0` certificado en `o_1·h_2`: punto regular, tangencia.
- Los signos de `h″` y de `|F′|` se registran como condiciones de la evaluación.
- El coste llega como mucho a 1 622 383 productos.

**Recomprobación en las doce posiciones del estudio y en movimientos de `B` y `H`.**

| Posición | Prueba de frontera | Prueba automática |
|---|---|---|
| base, `α_cone` 35° y 60° (mueven `H`), `excCyl` 3,7 (fuera del cono), restauraciones, parámetro de `H` ±20 y de `B` ±3 | certificada | certificada |
| `rbCone` 3, 4, 6 y 7,5; `D_1` ×0,5 y ×1,3 | `NOT_APPLICABLE` (cambió una constante) | certificada, sin trabajo simbólico nuevo |
| `D_1` ×3 | `NOT_APPLICABLE` | `e_1·n`, `o_1·h_2` y `e_1·r`: fallo cerrado (pertenencia al segmento); `e_1·c_1` en `K` y en `J`: certificadas |
| parámetro de `H` +40 (`H.x < K.x`: otra celda) | contactos en `K`: fallo cerrado (rama de `H_1`); en `J`: certificados | igual |

**Pruebas nuevas donde la recomprobación falla.**

| Posición | `e_1·n` en `K` | `e_1·c_1` en `K` | `o_1·h_2` en `K` | `e_1·c_1` en `J` | `e_1·r` en `J` |
|---|---|---|---|---|---|
| `D_1` ×3 | identidad con el soporte; **fuera de la parte limitada (certificado)** | certificada | `INVALID`: `M_1` no existe | certificada | identidad; fuera de la parte limitada (certificado) |
| `H` +40 | identidad; fuera de la parte limitada (certificado) | **certificada en la celda nueva** (la otra rama de `H_1`) | `INVALID`: `M_1` no existe | certificada | certificada |

Esto confirma lo que el estudio vio en el modelo: con `D_1` ×3, `e_1·n` y `e_1·r` quedan fuera de
la parte limitada, `M_1` no existe y `e_1·c_1` sigue siendo exacto. Ahora esos hechos están
certificados y no solo observados, con la salvedad de §11 sobre los valores ideales. La celda
nueva de `H` +40 da la prueba de la otra rama de `H_1`, que es el teorema de reflexión del
[análisis de `CU3`](pre_g9b_cu3_initial_state_and_capability_analysis.md) §5.3.

**Controles adversarios.**

| Control | Resultado |
|---|---|
| C1, objetivos binary64 `n` y `h_2` (la representación, no la construcción) | `NOT_AN_IDENTITY` |
| C2, la otra intersección de `c_1` con el soporte de `n` como objetivo de `h_2` | `NOT_AN_IDENTITY` (la prueba distingue las ramas reales) |
| C3, igualdad accidental: `I` sobre la vertical constante que pasa por su sustituto, con residuo nulo en él | `NOT_AN_IDENTITY`; el residuo en la caja es `[−1,0·10⁻⁹; 1,0·10⁻⁹]`: la igualdad en un punto no se certifica |
| C4, caja de semianchura 5 | `NOT_ESTABLISHED` (radio de `c_1`: en esa caja `H` puede alcanzar `D_1`) |
| C5, emparejamiento erróneo: `J_1` en `J` contra `n` | `NOT_AN_IDENTITY` |

**Construcciones sintéticas.** Se construyen en un anfitrión nuevo, con órdenes en inglés y en el
alcance automático:

| Enunciado | Resultado |
|---|---|
| S1: el circuncentro está en la tercera mediatriz; el segundo vértice y el vértice girado 47°, no constructible, están en la circunferencia circunscrita; el baricentro está en la tercera mediana | cuatro `IDENTITY` certificadas; corte del vértice girado |
| S1: conjetura falsa: el baricentro está en la circunferencia circunscrita | `NOT_AN_IDENTITY` |
| S2: Tales con las dos intersecciones de una recta con la circunferencia de diámetro `TA TB` | la intersección 2, `IDENTITY`; la 1, `NOT_AN_IDENTITY`: la prueba depende de la rama, como debe. Las dos ramas corresponden a la salida viva |
| S2: la intersección 1 es el propio `TA` (igualdad de puntos) | `IDENTITY`: la intersección 1 que da la regla heredada es `TA`, y su rama corresponde a la salida viva; no se supone |
| S3: el pie de la perpendicular a una recta no constructible está en la circunferencia de diámetro `UA UB` | `IDENTITY` |
| S3: un punto sobre una recta constructible está en ella; conjetura falsa: está en la circunferencia | `IDENTITY` (una indeterminada); `NOT_AN_IDENTITY` |
| S3: limitación: un punto sobre una recta no constructible se corta, y su propia pertenencia, que es de procedencia, no se vuelve a demostrar | `NOT_AN_IDENTITY`, como se esperaba; esa relación es `STRICT` por procedencia, no por el CIC |
| S4: cambio estructural: tras una prueba de `SC` sobre `cc`, se elimina `cc` y se vuelve a crear con la misma etiqueta como `Circle(SO, mAB)` | prueba anterior certificada; los signos registrados solos la seguirían aceptando (`CERTIFIED`, un certificado viejo y falso); la recomprobación la rechaza (`NOT_APPLICABLE`: cambió la estructura en `cc`); la prueba nueva da `NOT_AN_IDENTITY` |

**Coste.** En la ejecución A, cada prueba de incidencia tardó como mucho 0,66 s y cada prueba con
jets 1,71 s, a título informativo. La ejecución completa tarda entre 24 y 28 s.

En una ejecución de desarrollo no versionada, anterior a la regla de corte de §5.3, mantener `H`
sobre `j` con `C'` libre llevaba la prueba de `o_1·h_2` al límite de términos, y la ejecución
tardaba 340 s. La regla es necesaria, no cosmética.

### 5.6 Por qué las doce posiciones no son la demostración

- **La demostración es la identidad formal** junto con su celda. Las doce posiciones del estudio
  no prueban nada por sí mismas: son configuraciones en las que se comprueba, con certificado, que
  pertenecen a la celda de una prueba ya hecha.
- **La universalidad es por celdas.** La identidad vale en toda su celda. Fuera de ella, otra celda
  exige otra prueba, como la de `H` +40.
- **La genericidad 12 de 12 del estudio** (posiciones racionales de `H`) queda superada como
  evidencia. La prueba del alcance automático vale para todo `H`, `A`, `D_1`, `B` y `rbCone` de la
  celda, con la estructura y las ramas que registró y con los valores ideales en la caja.

### 5.7 Límites del prototipo y requisitos de producción

| Límite del prototipo | Requisito de producción (E1 o E2) |
|---|---|
| una sola extensión cuadrática; una segunda raíz independiente da `OUT_OF_CLASS` | torre de extensiones con normalización, o fallo cerrado tipado igual que hoy |
| funciones racionales sin máximo común divisor (solo divisiones exactas acotadas); un cuadrado perfecto no detectado oculta una identidad, sin falsearla | aritmética con simplificación y presupuesto versionado; el coste depende de la construcción y necesita su banco |
| circunferencias como única cónica; sin expresiones ni transformaciones | la tabla de clase versionada de §2.6; lo demás se corta o queda fuera de la clase |
| el valor ideal de un punto sobre camino o de un corte no se encierra (se confía en la holgura de la caja) | encierro riguroso de esos valores ideales (§5.2), en E1 |
| dentro del corte del conductor, la correspondencia de ramas descansa en el estado de selección capturado | la fidelidad de selección que `CU2` ya certifica, consumida por transferencia (§3) |
| la huella estructural del prototipo es su propia firma de cada objeto | la huella de revisión del ADR 0038, decisión 17, que ya cubre las definiciones estructurales |
| encierros sin subdivisión; las cajas grandes pierden certificados por sobreestimación | subdivisión o formas de Taylor de orden mayor cuando haga falta |
| `NOT_AN_IDENTITY` no intenta refutar | refutación opcional por encierro del residuo que excluye el cero, que da `CONTRADICTED` |
| conductores solo en circunferencias, con jets de orden dos | familias de camino de la clase v3, con los jets de A3.5 |

### 5.8 Relación con otras pruebas

- **Procedencia.** Da `STRICT` sin el CIC cuando el objeto ideal existe (§2.2), y el CIC no la
  vuelve a demostrar (fila «limitation»).
- **Evaluación exacta (M3).** Decide exactamente con una premisa constructible. Con una premisa no
  constructible decide para los sustitutos; repetida en perturbaciones racionales de ellos, da
  (c1) `SUBSTITUTE_GENERIC`, que es `RELAXED`. El CIC hace `STRICT` lo que M3 deja en `RELAXED`.
- **Simetría y paridad.** Son pruebas estructurales independientes. El CIC confirma la estructura,
  por ejemplo `t*` estructural y `F′ ≡ 0`, sin sustituirlas.
- **Prover heredado.** Prueba verdad genérica sobre los complejos, sin ramas reales. El CIC
  distingue ramas (C2, S2) porque sus decisiones de rama son signos certificados y porque contrasta
  las ramas con el algoritmo vivo. Por eso puede ser autoridad de prueba y el prover heredado no
  (estudio §2.5).

### 5.9 Texto candidato AUT-6 — certificados de identidad constructiva

- **Destino:** `locus-v2-constructive-authority.md` §8; referencia en
  `locus-v2-certified-construction-model.md` §12.3.
- **Naturaleza:** nuevo.
- **Depende de:** D-AUT-8.
- **Texto candidato:**

  > **§8 Constructive identity certificates (`constructive-identity-certificate/v1`).** A
  > constructive identity certificate decides a relation between objects of the class by exact
  > evaluation in the field `Q(V)[sqrt(Delta)]` of rational functions of a set `V` of
  > indeterminates, extended by square roots under a versioned rule. The boundary rule is
  > automatic: free points and numbers are indeterminates, the axes are constants, a point on a
  > constructible line path is one indeterminate with its other coordinate solved from the exact
  > line, and every point computed outside the class or lying on a path that depends on objects
  > outside the class is cut as a free point of the plane. A cut forgets relations and never
  > creates them. A circle belongs to the class by the algorithm that defines it, never by its
  > current value. Every branch, validity, membership and non-degeneracy decision is a sign
  > condition certified by outward-rounded enclosures on a decision box and recorded with the
  > inherited selection states it used. Every line-circle output is the one the inherited
  > selection rule gives with the captured selection state; outside the slice of a driver replaced
  > by an exact point, its live binary64 output must be certified strictly closer, on the whole
  > box, to the selected branch than to the other, or the result is
  > `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`; inside the slice, the captured selection state
  > governs, as in the class-v3 evaluator. Continuous-mode intersections and reference-point
  > selections are outside the class. When the residual of the relation is the zero element and
  > every recorded condition is certified on a box that contains the ideal value of the
  > indeterminates, the relation is `STRICT` on that box (`IDENTITY_CERTIFIED`). A certified
  > non-membership of a limited part is a rigorous contradiction of the relation with that limited
  > target. A residual that is not the zero element is `NOT_AN_IDENTITY`, never a contradiction.
  > An undecided condition, a construct outside the class or an exhausted versioned budget gives
  > `NOT_ESTABLISHED` with a typed cause; a validity condition certified false gives `INVALID`, and
  > the relation is `NOT_ESTABLISHED` with the cause `OBJECT_UNDEFINED`. At another revision a
  > certificate applies only when the structure of its dependency cone (the definitions of the
  > objects it used and what it cut), its constants and its inherited selection states are
  > unchanged, every recorded sign is certified with the same value on a box around the live
  > values of its indeterminates and every branch outside the driver slice still corresponds to
  > the live algorithm; otherwise a new certificate is attempted, and its absence leaves the
  > relation `NOT_ESTABLISHED`. Certificates are revision evidence and are never persisted. The
  > ideal value of every indeterminate that is not a free input — a point on a path or a cut — is
  > enclosed rigorously.

## 6. Punto 5 — Plan mínimo de ejecución

### 6.1 Principios

- **Antes, los contratos.** Ningún cambio productivo antes de E0. Después, autonomía por puertas.
- **Utilidad antes que rigor máximo.** E1 da construcciones útiles con integridad consultable. E2
  hace estrictas las relaciones justificadas, sin cambiar ninguna identidad.
- **Cada incremento** tiene su puerta, sus pruebas adversarias y sus condiciones de parada.
- **Las referencias se conservan.** Las referencias de `CU0` y las regresiones de `CU0`, `CU1`,
  `CU2`, G8 y G9U0 se mantienen según sus contratos. Un cambio de `pins` solo se acepta con una
  prueba de la corrección.

### 6.2 Incrementos

| Incremento | Contenido | Resultado en el modelo |
|---|---|---|
| **E0. Contratos** | aprobación autoral del ADR 0040 y de los textos AUT-1 a AUT-7, de las decisiones D-AUT y del prompt de tarea; T0 los incorpora sin reescribir las secciones aprobadas | ninguno |
| **E1. Integridad consultable** | (a) tipo `RelationIntegrity`, registros de evidencia, propagación por premisas, requisitos de consumidor y consulta de integridad con premisas; (b) evaluador constructivo con la tabla de clase, la semántica de puntos sobre caminos, los sustitutos declarados y la evaluación exacta M3 llevada a producto con presupuesto; (c) registro de política con fallo cerrado de los lectores anteriores, nacimiento constructivo de los documentos nuevos y evento de adopción con informe de impacto; (d) encierros rigurosos de los valores constructivos de las constantes capturadas, incluidas las que dependen de objetos no constructibles (evaluación por intervalos de su subgrafo, con funciones trigonométricas de redondeo hacia fuera), y con ellos la transferencia de `CU2` por margen y por estructura para lo que necesita `CU3-A`; (e) política `RELAXED` v1 con (c1) genérica y recalificación de G8B-R1 en la adopción; (f) `Point(L, D)` e `Intersect(L, r, D)` bajo la autoridad constructiva | los tres casos obligatorios y la costura materializados: `P ∈ L` `STRICT` por transferencia, `P ∈ r` `RELAXED` por (c1), consultables; los documentos antiguos, sin cambios |
| **E2. Certificación estricta** | (a) CIC v1: clase, regla de frontera automática, correspondencia de ramas con el algoritmo vivo, celdas con huella estructural, recomprobación por revisión, prueba nueva por celda, presupuestos, con los encierros de E1(d) para los valores ideales; (b) subida de `RELAXED` a `STRICT` y bajada por contradicción, sin identidad nueva; (c) orden de contacto por jets exactos para las clases de contacto 1 y 2, con sus signos registrados en la celda; el pliegue lo sigue certificando `CU2` | `P ∈ r` `STRICT` en los cinco contactos, dentro de su celda |
| **E3. Conflicto y Etapa B** | certificado de paridad por simetría exacta (SM-B-14) en la regla de conflicto; reclasificación de la costura con prueba e inversión de `pins.B08_B09`; `CU3-B` (materialización sin conductor) bajo la autoridad constructiva | costura reclasificada como contacto en pliegue; `preserve.contacts` igual |
| **E4. Cierre** | verificación `INTEGRATED_PHASE`; registro de cierre; revisión manual del autor | candidato congelado |

### 6.3 Puertas y pruebas adversarias

Precisan las del estudio (§8) y añaden G-AUT-9 y G-AUT-10.

| Puerta | Incremento | Qué establece | Pruebas adversarias |
|---|---|---|---|
| G-AUT-0 | E0 | contratos y decisiones aprobados por el autor | — |
| G-AUT-1, cierre de procedencia | E1 | el evaluador constructivo hace exactas las relaciones de procedencia del modelo y de un corpus sintético, con la semántica de D-AUT-3; ninguna relación de procedencia es `STRICT` sin su hecho de existencia | un punto sobre camino sin la semántica de camino refuta su camino; una recta binary64 refuta sus puntos; casi-tangencia en que la tolerancia heredada crea o suprime una intersección que la ideal no tiene; punto aceptado por tolerancia junto al extremo de un segmento; el censo 160/160 entra solo como regresión |
| G-AUT-3, intrinsecidad | E1 | una sola semántica por objeto con cualquier conductor, captura y consumidor | las cuatro semánticas de `h_2` deben dar una |
| G-AUT-4, transferencia | E1 | margen, estructura y nueva decisión; `EVIDENCE_INSUFFICIENT` y fallo cerrado estricto; bytes del certificado de origen intactos | perturbaciones de un ulp; valor crítico de la mordedura (cerrado); componente puntual consumida sin nueva decisión; huella distinta; correspondencia rota por un estado de selección cambiado |
| G-AUT-5, integridad | E1 | propagación por premisas; consumidores estrictos que fallan de forma explícita | grafo sintético con premisas `STRICT`, `RELAXED` y `CONTRADICTED`; premisa lejana relajada que no contamina una incidencia de procedencia |
| G-AUT-6, sin blanqueo | E1 | ninguna relación `CONTRADICTED` pasa a `RELAXED`; autoridad por documento; (c1) exige genericidad | admitir como relajado un contacto refutado; elegir autoridad por relación; documento mixto; residuo binary64 dentro de la tolerancia con contradicción constructiva; igualdad exacta solo en el sustituto, como C3 de §5.5, que (c1) no debe admitir |
| G-AUT-7, compatibilidad | E1 a E4 | documentos antiguos sin cambios; tokens que se resuelven; `preserve` igual; `pins` invertidos solo con prueba; los lectores anteriores fallan cerrados ante el registro | corpus de `CU0`, `CU1` y `CU2`; modelo del autor sin adoptar y adoptado; Deshacer de la adopción; copiar y pegar entre políticas; documento constructivo abierto con un lector anterior |
| G-AUT-8, coste | E1 y E2 | presupuestos versionados y contadores deterministas | inyección de presupuesto; construcción con una segunda raíz independiente |
| G-AUT-2, contactos | E2 | los cinco contactos `STRICT` en su celda, con orden de contacto | C1 a C5 de §5.5; objetivo desplazado un ulp; V-M3-5, V-M3-6 y V-CFG-6 |
| G-AUT-9, corrección del CIC | E2 | teoremas sintéticos demostrados; conjeturas falsas no demostradas; dependencia de la rama; correspondencia de ramas con el algoritmo vivo; un corte nunca inventa | las filas de S1 a S4 y otras nuevas del mismo tipo; caso especial heredado de un punto ya incidente; ramas casi coincidentes que no se pueden distinguir; inversión de signo inyectada; caja que no contiene el valor ideal |
| G-AUT-10, dinámica | E2 | ninguna prueba sobrevive a un cambio de estructura, de constante, de selección o de celda; prueba nueva en la celda nueva | doce posiciones, movimientos de `B` y `H`, `H` +40, `D_1` ×3, transición real de un estado de selección; redefinición de un objeto del cono y sustitución con la misma etiqueta, como S4 |
| G-A y G-B | E1 y E3 | las de `CU3`, ajustadas a esta decisión | las de la matriz de `CU3`, revisadas |
| G-final | E4 | `INTEGRATED_PHASE` | — |

### 6.4 Autonomía y condiciones de parada

Aprobados E0 y el prompt de tarea, el agente ejecuta E1 a E4 sin consultas intermedias para
decisiones menores de implementación, pruebas u organización. Puede corregir defectos locales
dentro de los contratos aprobados, con evidencia adversaria. **Solo se detiene ante una condición
de parada real:**

- una puerta que no se supera dentro de los contratos aprobados;
- la necesidad de cambiar un contrato aprobado más allá de AUT-1 a AUT-7;
- un presupuesto que exija una aprobación autoral nueva;
- una incompatibilidad de identidad o de documentos antiguos;
- una contradicción que retiraría un resultado de un documento antiguo;
- la imposibilidad de satisfacer una garantía obligatoria.

Un fallo de certificación nunca se convierte en una aproximación silenciosa. Si E2 no supera
G-AUT-9 o G-AUT-10, E1 se presenta como candidato parcial verificado, con `P ∈ r` `RELAXED`.

### 6.5 Verificación

- **Clase:** `INTEGRATED_PHASE` (D-CU3-14), con selecciones `pre-g9b-cu3.*` ampliadas con las
  puertas G-AUT.
- **Base (D-AUT-10):** la rama de implementación detenida avanza por avance rápido al commit que
  apruebe el autor. Es posible porque esta rama desciende de `1a0903aa`.
- **Candidato:** se congela antes de la verificación final; las selecciones oficiales se ejecutan
  sobre el mismo commit y el mismo árbol.

## 7. Revisión coordinada de los contratos (C)

| Contrato | Cláusula | Revisión | Naturaleza |
|---|---|---|---|
| ADR nuevo 0040 | — | autoridad constructiva, política de documento, integridad, transferencia, política `RELAXED` y CIC (§9) | nuevo |
| especificación nueva `locus-v2-constructive-authority.md` | §1 a §8 | AUT-1, AUT-2, AUT-3, AUT-5 y AUT-6 | nueva |
| `locus-v2-intersections.md` | §2.1 (adaptador autoritativo); CU3.1; CU3.4 | AUT-7: bajo la política constructiva, el objetivo autoritativo es el objeto constructivo y el adaptador binary64 es la representación de la búsqueda de raíces; CU3.4 decide en `Σ_C`; G8B-R1 se recalifica (AUT-5 §7.4) | nueva versión semántica (1.4), solo bajo la política nueva |
| `locus-v2-intersections.md` | CU3.1 (c) y CU3.2 (clases de contacto) | bajo la política constructiva, la clase 2 (aritmética exacta) exige una premisa constructible; con sustitutos no constructibles, la evidencia es (c1) y la integridad `RELAXED`; las clases 1 y 2 decididas contra el objeto constructivo son `STRICT` | nueva versión semántica, solo bajo la política nueva |
| `locus-v2-intersections.md` | CU3.4, «Without contradiction, admissibility is unchanged» | se mantiene bajo la política antigua; bajo la nueva, la admisibilidad cambia sin contradicción solo en el evento de adopción, por la recalificación de §4.4 | nueva versión semántica, solo bajo la política nueva |
| `locus-v2-certified-construction-model.md` | §11.2 y §11.3 | AUT-7: configuración de decisión constructiva; `Σ_L⁺` queda como configuración de la política antigua; nueva §12 con AUT-4 y la referencia a AUT-6 | nueva versión semántica (1.4), solo bajo la política nueva |
| `locus-v2-certified-construction-model.md` | §11.4, condiciones 3 y 4 | la 3 (correspondencia con el algoritmo vivo) se conserva y se extiende al CIC; la 4 («no decision is transferred from another configuration») la sustituyen, bajo la política nueva, las vías admitidas de AUT-4 y la recomprobación de una misma prueba de AUT-6 | nueva versión semántica, solo bajo la política nueva |
| `locus-v2-existence-components.md` | nota de consumidor | los consumidores estrictos de `CU2` consumen certificados transferidos (AUT-4) | aditiva |
| Registro de aprobación de `CU3` | G-M3-4 | bajo la política nueva, la autoridad del objetivo que comprueba es la constructiva (AUT-7); se mantiene que el significado de un objetivo nunca se redefine para que una incidencia se cumpla | decisión nueva (D-AUT-11) |
| ADR 0028 | decisión 3 | su «authority» es la de la representación | recalificación aditiva |
| ADR 0038 | decisiones 7 y 17 | sin cambio de texto; la transferencia es una capa nueva | aditiva |
| ADR 0039 | autoridad de `L` en `Σ_L` y del objetivo | sustituido por el ADR 0040 solo bajo la política constructiva | nueva versión semántica |
| ADR 0008 | G8B-R1 | admisión de la política antigua y buscador de candidatos; sin transferencia automática | recalificación aditiva |
| ADR 0013 | política persistida | el registro de política entra entre las entradas persistidas | aditiva |
| ADR 0017 | barrera de cardinalidad | nota de migración para la adopción | aditiva |
| ADR 0029 | migración perezosa | patrón del evento de adopción, sin documentos mixtos | sin cambio |
| ADR 0037 | clases 1 a 4 | correspondencia con la integridad (§4.6) | aditiva |
| Decisiones de `CU3` | D-CU3-3, -4, -5, -6, -7, -10 y -13 | D-AUT-11 | decisiones nuevas |
| `AGENTS.md` | §1 y §11 | ninguna: la dirección es coherente con ellos, y el archivo es de solo lectura para los agentes | — |

### 7.1 Texto candidato AUT-7 — objetivo y configuración de decisión bajo la autoridad constructiva

- **Destino:** `locus-v2-intersections.md`, nueva sección «Constructive authority amendment
  (version 1.4)»; `locus-v2-certified-construction-model.md` §12.1.
- **Naturaleza:** sustituye §2.1 (tabla de autoridades del objetivo) y §11.2–§11.3 **solo** en los
  documentos con `constructive-authority/v1`. En los demás, esas secciones rigen sin cambios.
- **Depende de:** D-AUT-1 y D-AUT-11.
- **Texto candidato:**

  > **Constructive authority amendment (version 1.4).** In a document governed by
  > `constructive-authority/v1`, the authoritative target of an intersection is the constructive
  > object of `locus-v2-constructive-authority.md` §1: a line, segment, ray or circle as its
  > construction defines it, with the limited part of a segment or ray as its construction defines
  > it. The approved target adapters of §2.1, including `g8b-circle-radial-distance-adapter/v1`,
  > remain the representations used by the numerical root search and by G8B-R1; their contract
  > text inside tokens is unchanged. The decision configuration of a structural point is the
  > constructive configuration `Σ_C` of the locus and of the target, with the declared boundary
  > substitutes; the captured configuration `Σ_L` of CU2 is a certified representation whose
  > certificates enter by `cu2-certificate-transfer/v1`. The conflict rule of CU3.4 decides in
  > `Σ_C`, against the constructive target, and also admits a parity certificate by exact
  > symmetry. An incidence is never decided against a reconstruction chosen for that incidence. In
  > a document governed by `legacy-representation-authority/v1`, §2.1 and §11.2–§11.3 of the
  > certified construction model apply unchanged.

## 8. Riesgos y compatibilidad

Los riesgos y la tabla de compatibilidad del estudio (§7) se mantienen. Esta consolidación añade:

| Riesgo | Mitigación |
|---|---|
| **El coste del CIC en construcciones generales.** Crece con las raíces independientes y con los cortes mal elegidos: 340 s en una ejecución de desarrollo anterior a la regla de §5.3 | regla de corte; presupuesto versionado; banco por familia; `NOT_ESTABLISHED` al agotarse, nunca una aproximación |
| **Sobreestimación de intervalos** en cajas grandes o con muchas indeterminadas | forma de valor medio de segundo orden (ya en el prototipo); subdivisión; la caja de decisión es pequeña y la recomprobación usa la configuración viva |
| **Incompletitud de los cortes**: un teorema que necesita la relación olvidada queda `NOT_ESTABLISHED` | es honesta; la procedencia sigue siendo `STRICT`; el informe nombra el corte |
| **Adopción en documentos con muchas raíces G8B** | informe de impacto previo; transacción con Deshacer; la recalificación por corchete constructivo es barata |
| **Dos políticas vivas en el producto** | el registro las distingue por documento; las pruebas G-AUT-7 cubren las dos; sin documentos mixtos |
| **Comprensión del usuario** | consulta de integridad con premisas, separada de la precisión; `RELAXED` nunca se presenta como prueba |

**Compatibilidad.** Ningún formato existente cambia: el registro de política es aditivo. Los
documentos antiguos conservan su política y sus resultados. Los tokens, los selectores, las claves
de linaje y los certificados de `CU2` no cambian. `preserve.contacts` se conserva, y `pins.B08_B09`
se invierte solo en E3 y con prueba.

## 9. ADR candidato 0040

- **Destino:** `docs/adr/0040-constructive-geometric-authority-and-relation-integrity.md`, que solo
  se crea tras la aprobación.
- **Naturaleza:** nuevo; `PROPOSED` hasta la decisión autoral.
- **Texto candidato:**

  > # ADR 0040 — Constructive geometric authority and relation integrity
  >
  > - Status: **PROPOSED** (candidate of the PRE-G9B-CU3 focal consolidation; no author approval).
  > - Amends: ADR 0008, 0013, 0017, 0028, 0037 and 0038 additively; supersedes the authority clauses
  >   of ADR 0039 in documents governed by `constructive-authority/v1`.
  >
  > ## Context
  >
  > Gate G-M3 of PRE-G9B-CU3 showed that two approved contracts take a representation as geometric
  > authority: the target by its binary64 state and the locus by its CU2 captured configuration.
  > Under that authority the five mandatory contacts of the author model are exactly refuted,
  > while a constructive semantics makes them exact. The binary64 representation contradicts 66 of
  > the 160 provenance relations of the model; a circle carries three distinct exact radii; a
  > reconstructed target has four distinct exact meanings. A focal consolidation prototyped a
  > constructive identity certificate that establishes, as research evidence, the five incidences
  > as identities with the non-constructible input as a free indeterminate.
  >
  > ## Decision
  >
  > 1. **Single authority.** Under the versioned document policy `constructive-authority/v1`,
  >    every object has one geometric meaning, the ideal object its construction defines from the
  >    free inputs; every numerical state is a representation.
  > 2. **Document policy.** Documents without the record keep
  >    `legacy-representation-authority/v1`; new documents are created under the constructive
  >    policy; existing documents adopt it only by the explicit event `AUTHORITY_POLICY_ADOPTION`;
  >    one document has one authority, and a reader that does not support the record fails closed.
  > 3. **Inherited algorithms** are unchanged representation engines; their provenance relations
  >    are strict for the ideal objects whenever those exist, and the existence of an ideal output
  >    is decided rigorously, never by an inherited tolerance.
  > 4. **Class table and boundary.** A versioned class table decides exactness by algorithm class;
  >    objects outside it enter exact evaluation as declared boundary substitutes; a point on a path
  >    means the point of the ideal path assigned by the inherited path map.
  > 5. **Integrity.** Relations carry `STRICT`, `RELAXED`, `NOT_ESTABLISHED` or `CONTRADICTED` in
  >    a type of their own, independent of numeric guarantees, propagated by premises, never
  >    persisted and never part of identity.
  > 6. **Relaxed admission.** `constructive-relaxed-admission/v1` admits a relation only with
  >    positive evidence relative to the constructive objects — exact equality with the declared
  >    substitutes that persists under rational perturbations of them, or, for sign-change roots
  >    only, a compatible enclosure of the constructive residual — and without rigorous
  >    contradiction; G8B-R1 is not inherited and is re-qualified once, at adoption.
  > 7. **CU2 transfer.** CU2 certificates remain certificates of their captured configuration and
  >    transfer only by structural correspondence, the same revision and one of margin, structure or
  >    re-decision; otherwise `EVIDENCE_INSUFFICIENT` and strict consumers fail closed.
  > 8. **Constructive identity certificates** prove relations as identities in a field of rational
  >    functions with an automatic boundary rule, certified sign conditions, branches that
  >    correspond to the live algorithm outside the driver slice, cells bound to the structure of
  >    their dependency cone, rechecks and new proofs per cell.
  >
  > ## Consequences
  >
  > Legacy documents and every identity are unchanged. The mandatory contacts become
  > materializable with explicit integrity and, with constructive identity certificates, strict.
  > The product carries two policies, distinguished per document. Certification of constructions
  > outside the class remains `NOT_ESTABLISHED` or `RELAXED`.
  >
  > ## Alternatives considered
  >
  > Representation authority with structural certificates (B1), invalid: it declares incident
  > two incompatible exact sets. Capture recomputation alone (C3), insufficient: the binary64
  > target still refutes. Declared-boundary authority (A1), rejected as authority: its
  > contradictions at the boundary are unreliable; kept as computation engine. Tolerance
  > incidence, contrary to `AGENTS.md` §1. Inherited prover as authority, rejected: no real
  > branches. Per-relation authority, rejected: two meanings for one object.

## 10. Decisiones autorales necesarias

| Decisión | Alternativas | Recomendación |
|---|---|---|
| **D-AUT-1**, autoridad | (a) A2 como autoridad normativa por política de documento, ADR 0040; (b) mantener la autoridad de representación | (a) |
| **D-AUT-2**, alcance y adopción | (a) documentos nuevos constructivos, antiguos con la autoridad vigente hasta la adopción explícita, sin documentos mixtos; (b) adopción solo explícita, también en los nuevos; (c) autoridad por comando | (a); (c) se rechaza porque da dos significados a un objeto |
| **D-AUT-3**, puntos sobre caminos | (a) la aplicación de camino heredada sobre el camino ideal; (b) proyección ortogonal anclada | (a): intrínseca y heredada. La certificación genérica no depende de la elección |
| **D-AUT-4**, transferencia de `CU2` | (a) las condiciones de §3.2, con `EVIDENCE_INSUFFICIENT` y fallo cerrado; (b) recalcular `Σ_L` (C3) | (a): conserva certificados, huellas y claves |
| **D-AUT-5**, política `RELAXED` v1 | (a) (c1) genérica (sustituto y ocho perturbaciones racionales) para toda relación, y (c2) solo para raíces con cambio de signo, con el umbral de G8B-R1 aplicado al residuo constructivo; (b) solo (c1); (c) sin `RELAXED` | (a): ningún contacto sin cambio de signo se admite por una cota de residuo, y ninguna igualdad aislada en el sustituto basta |
| **D-AUT-6**, G8B-R1 en documentos adoptados | (a) recalificación por §4.4; (b) herencia automática | (a): la herencia automática transfiere una admisión entre autoridades sin justificación |
| **D-AUT-7**, revisión de D-CU3-5 | (a) materializar `Intersect(L, r, D)` con `P ∈ r` `RELAXED` e integridad visible desde E1; (b) materializar solo con `STRICT`, después de E2 | (a): da construcciones útiles antes y no presenta lo relajado como prueba |
| **D-AUT-8**, CIC | (a) CIC v1 como certificador estricto con la regla de frontera automática, las celdas y los encierros rigurosos de los valores ideales; (b) pruebas específicas por teorema | (a): capacidad reutilizable |
| **D-AUT-9**, nombres y API | tipo propio `RelationIntegrity`, sin persistir y fuera de los tokens | la indicada; evita la colisión con `STRICT` de `OpenBoundaryPolicy` |
| **D-AUT-10**, plan, puertas, autonomía y base | E0 a E4 con las puertas de §6.3; `INTEGRATED_PHASE`; la rama detenida avanza al commit aprobado | lo indicado |
| **D-AUT-11**, decisiones de `CU3` | D-CU3-3: autoridad constructiva, `Σ_L` como representación transferida. D-CU3-4: M3 en `Σ_C` con sustitutos declarados, y el CIC para las premisas no constructibles. D-CU3-5: D-AUT-7. D-CU3-6 y D-CU3-7: G8B-R1 conservada bajo la política antigua y recalificada en la adopción bajo la nueva (§4.4), sin reasociaciones. D-CU3-10: reclasificación de la costura por paridad bajo la autoridad constructiva. D-CU3-13: certificado de paridad y configuración `Σ_C`. G-M3-4: autoridad del objetivo constructiva bajo la política nueva. CU3.1 (c), CU3.2 y §11.4: como en §7 | aprobar las revisiones |
| **D-AUT-12**, publicación | publicar esta rama para la auditoría, con push normal y sin `main` | decisión del autor; el agente no publica |

## 11. Lo que esta propuesta no establece

- **Ninguna aprobación.** Ningún contrato, cota, presupuesto ni puerta queda aprobado. `CU3`
  sigue detenido en T2.
- **El prototipo no es producto.** Su evidencia es de investigación: no certifica el producto ni
  sustituye las puertas de §6.
- **Universalidad.** Ninguna prueba vale fuera de su celda. Las doce posiciones son
  recomprobaciones.
- **Valores ideales.** El prototipo no encierra rigurosamente los de los puntos sobre caminos ni
  los de los cortes (§5.2); por eso sus «certificados», incluidas las contradicciones de las
  pruebas nuevas, valen con esa salvedad.
- **Ramas dentro del corte del conductor.** El prototipo las toma del estado de selección
  capturado sin volver a certificar su fidelidad, que en el producto aporta `CU2`.
- **Coste general.** Las cifras son las del modelo y de cuatro construcciones sintéticas.
- **Comandos heredados de relaciones.** No se integran con la integridad (§2.2).
- **Sintaxis del registro de política.** Queda para E1.

## 12. Reproducción

```text
pwsh -NoProfile -File tools/benchmark/pre-g9b-cu/run-cu3-constructive-identity.ps1 -OutputDirectory <directorio nuevo>
```

Hubo dos ejecuciones sobre `6ce61d81`, con el espacio de trabajo limpio, en
`artifacts/benchmarks/pre-g9b-cu/cu3-constructive-identity-6ce61d81-runA` y `…-runB`, que el
repositorio ignora:

- códigos de salida 0 en el sondeo y en la limpieza;
- fuentes del producto sin cambios;
- el mismo SHA-256 de `cu3-constructive-identity.json`;
- 24,3 a 27,7 s por ejecución, a título informativo.

Las ejecuciones anteriores, sobre `51cc3637`, dieron una evidencia distinta y ya sustituida, sin
la correspondencia de ramas ni la huella estructural.

El sondeo lee copias del modelo del autor, construye las construcciones sintéticas en un anfitrión
embebido nuevo y no asigna ningún veredicto.
