# PRE-G9B-MAINT-P0 — B. Propuesta matemática y arquitectónica de B-splines, NURBS e interoperabilidad 2D

| Campo | Valor |
|---|---|
| Carácter | **Propuesta candidata de planificación.** No es especificación normativa, no aprueba ni modifica `semantic-spline-2d.md`, los ADR 0018/0021/0022/0028 ni los contratos DXF, y no autoriza implementación. `implementationAuthorized=false`; `selfApproved=false` |
| Estado | `PROPOSAL — ARCHITECTURAL DIRECTION APPROVED BY THE AUTHOR 2026-11-23` (revisión 3): D-A-1 a D-A-5 aprobadas como dirección arquitectónica, D-A-6 y D-A-8 aprobadas, D-A-7 aprobada con R-a para la primera implementación (R-c después, R-d no autorizada), D-A-9 como política de diseño y D-A-10 como dirección; `MAINT-A-R` diseñada y no autorizada; ninguna implementación autorizada ([hoja F](../validation/pre_g9b_maint_p0_author_decision_sheet.md) §0) |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0», bloque A, y enmiendas autorales finales del expediente, punto 1 (ambas del 2026-11-23): solo planificación, caracterización técnica y diseño preliminar |
| Base inspeccionada | `main` = `origin/main` = `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2), árbol `8c1d4ca163b214898ec392bd9d02f04c955661d1` |
| Expediente | [portada del expediente](../validation/pre_g9b_maint_p0_dossier.md) · [informe de caracterización](../validation/pre_g9b_maint_p0_characterization_report.md) · [hoja de decisiones](../validation/pre_g9b_maint_p0_author_decision_sheet.md) |

## Registro de revisiones

| Revisión | Fecha | Contenido |
|---|---|---|
| 1 | 2026-11-23 | Versión inicial: familia hermana `BSpline` como proveedor de `GeoLocusV2`; exportación `SPLINE` posterior; importación DXF diferida; recomendación de no incluir NURBS en el programa |
| 2 | 2026-11-23 | Enmiendas autorales finales, punto 1: arquitectura preparada para NURBS desde el origen (§5); importación DXF `SPLINE` de referencia, polinómica y racional (§8); construcción explícita de B-splines aproximantes (§9); NURBS semántica nativa diseñada y no autorizada (§10); secuencia A-N1, A-DXF0, A2, A-DXF1, A3, A-R (§11). La recomendación de la revisión 1 sobre NURBS queda sustituida y se conserva aquí como historia: la exclusión de NURBS deja de presentarse como decisión arquitectónica definitiva |
| 3 | 2026-11-23 | Resolución autoral definitiva: estado de D-A-1 a D-A-10 en la cabecera. Precisiones: la primera implementación de A-DXF0 usa R-a y R-c es el objetivo posterior prioritario, con caracterización previa del archivo `.cedg`; la cota certificada de §9.5 se justifica con su procedimiento de acotación, incluidos redondeo y precondiciones; la ubicación del registro de procedencia de §9.7 y su comportamiento en copia, redefinición y persistencia se resuelven en el contrato normativo (la preferencia de §9.7 queda como propuesta). El resto del texto no cambia |

## 0. Resumen y recomendación

**Decisión de orientación propuesta (revisión 2):** arquitectura preparada para NURBS;
implementación semántica racional diferida; importación DXF racional de referencia y
aproximación B-spline incorporadas como objetivos explícitos, con incrementos y puertas
propios. Se mantienen separadas cuatro capas: geometría autoritativa CeDG, definición
importada de referencia, aproximación matemática y render (§5.4).

1. **SplineV2 no es una B-spline de control y su contrato aprobado lo dice expresamente.**
   Es la familia interpolante de `Spline` Classic reconstruida como `GeoLocusV2`: los
   datos son puntos por los que pasa la curva, la parametrización es por cuerda
   normalizada en `[0, 1]`, los nudos son siempre simples y los coeficientes se obtienen
   resolviendo un sistema lineal denso. `semantic-spline-2d.md` §1 (aprobado) la declara
   «not a NURBS, control-point B-spline or arbitrary fitting contract».
2. **Una familia semántica nueva e independiente de SplineV2, concebida desde el origen como
   familia B-spline racional en general (alternativa β, §4 y §5).** Su primera
   implementación (`MAINT-A2`) es el subconjunto polinómico, con pesos unitarios
   implícitos, publicado como `GeoLocusV2` mediante su propio proveedor. Hereda sin cambios
   casi todo el aparato de Locus V2 (longitud, intersecciones unilaterales, pares sin
   certificar, puntos sobre la curva, semejanzas, dibujo, exportación aproximada y
   persistencia), que es genérico sobre tres interfaces que SplineV2 ya implementa. Añadir
   después pesos positivos explícitos es una versión nueva de la misma familia, no una
   familia nueva: un objeto polinómico es el caso `w_i ≡ 1` de la definición racional.
3. **Sin interfaces racionales generales en el núcleo antes de necesitarlas.** La
   definición, la persistencia y el evaluador de la familia se diseñan sobre coordenadas
   homogéneas; las interfaces genéricas de Locus V2 siguen siendo polinómicas hasta el
   incremento racional nativo `MAINT-A-R`.
4. **Ampliar SplineV2 (alternativa α) se desaconseja.** Mezclaría en un mismo comando y
   un mismo tipo dos semánticas incompatibles (puntos interpolados frente a puntos de
   control), exigiría enmendar una especificación aprobada y, como el XML de SplineV2 no
   lleva atributo de familia ni de versión, cualquier cambio de ecuaciones en una forma
   existente cambiaría en silencio la geometría de los documentos reabiertos.
5. **Importación DXF `SPLINE` de referencia (`MAINT-A-DXF0`, §8).** Lectura de solo lectura
   de entidades `SPLINE` 2D polinómicas y racionales definidas por puntos de control, con
   procedencia, evaluación para el render independiente de la vista y rechazo diagnosticado
   de lo que no se puede reproducir. La geometría importada no es un objeto CeDG: no entra
   en el DAG, no se convierte en `GeoLocusV2` y su render no certifica nada. No espera a la
   familia semántica.
6. **Aproximación explícita (`MAINT-A-DXF1`, §9).** El usuario puede generar, a partir de
   una entidad importada, una B-spline polinómica nueva (una construcción CeDG distinta de
   la curva racional original) con error y procedencia declarados. Se distinguen residuo en
   muestras, estimación numérica del error entre curvas y cota global certificada, que en
   este caso sí tiene un procedimiento demostrable (§9.5).
7. **DXF de salida (`MAINT-A3`):** los datos autoritativos de la familia (grado, nudos y
   puntos de control binary64) pueden escribirse **sin transformar** en una entidad
   `SPLINE` no racional, con evidencia de conformidad de lectores. SplineV2 sigue
   exportándose como `LWPOLYLINE` aproximada.
8. **NURBS semántica nativa (`MAINT-A-R`, §10):** diseñada como extensión racional del
   núcleo con garantías propias para evaluación, métricas, intersecciones y persistencia.
   Queda **diseñada y no autorizada** en este programa.

## 1. Estado de partida verificado

Detalle y citas completas en el [informe de caracterización](../validation/pre_g9b_maint_p0_characterization_report.md), §A.

### 1.1 SplineV2

| Aspecto | Hecho | Fuente |
|---|---|---|
| Familia | Ecuaciones de `Spline` Classic; equivalencia geométrica con Classic a `1e-8` | `SplinePolynomialModel2D.java:15-23`; `G9S1SemanticSpline2DTest` s28 |
| Datos | 3–32 puntos 2D finitos; grado `3 ≤ d ≤ min(N, 12)`; sistema denso `≤ 512` incógnitas; función de peso opcional | `SplinePolynomialModel2D.java:25-29, 71-80, 156-163` |
| Parametrización | Cuerda normalizada (`Math.hypot`) en `t ∈ [0, 1]`; la función de peso la sustituye | `SplinePolynomialModel2D.java:102-117, 281-296` |
| Nudos | Estrictamente crecientes, siempre simples; el nudo interior pertenece al tramo derecho | `SplinePolynomialModel2D.java:108-117, 191-204` |
| Condiciones de extremo | `d = 3`: spline cúbica natural; `d ≥ 4`: filas heredadas no estándar que ADR 0022 prohíbe sustituir | `SplinePolynomialModel2D.java:298-357`; ADR 0022, decisión 2 |
| Continuidad publicada | `C^{d-1}` paramétrica exacta de la función representada en cada nudo y en la costura periódica; ni `G^k`, ni regularidad, ni interpolación exacta | `SplineStructuralModel2D.java`; `SplinePolynomialModel2D.java:258-261` |
| Cierre | Periódica solo si el primer y el último punto son exactamente iguales en binary64; igualdad aproximada → indefinida | `SplinePolynomialModel2D.java:118-123` |
| Identidad | Un `GeoLocusV2` nuevo con identificador duradero; la lista y el grado se promueven a objetos etiquetados | `LocusV2PublicOperations.java:103-135, 1432-1477` |
| Persistencia | `<command name="SplineV2">` con sus entradas; sin atributo de familia ni de versión | `GeoLocusV2.java:507-519` |
| Calidad | `SEMANTICALLY_CONSTRUCTED`, `ANALYTIC_EVALUATION`, `FLOATING_POINT_UNCERTIFIED`; existencia probada por el proveedor (`spline-v2-interpolation/v1`) | `AlgoSplineV2.java:43, 93-112` |
| Representación interna | Base de potencias truncadas con numeradores exactos; ninguna base B-spline, red de control ni forma de Bézier como autoridad | §A.2 del informe |

### 1.2 Exportación e importación DXF

| Aspecto | Hecho | Fuente |
|---|---|---|
| Versión y entidades | ASCII `AC1015`; solo `POINT`, `LINE`, `RAY`, `XLINE`, `CIRCLE`, `ARC`, `ELLIPSE`, `LWPOLYLINE` | `DxfExporter.java:332-389` |
| Exactas | Punto, segmento, rayo, recta, circunferencia, arco, elipse y arco elíptico, polígono y polilínea | `GeoElementGeometryExportAdapter.java:179-241` |
| Aproximadas | Parábola, hipérbola, función, curva paramétrica, Locus V2 y SplineV2: `LWPOLYLINE`, `APPROXIMATE`, `ORIENTED_DYADIC_REFINEMENT`, `ESTIMATED_ERROR`; tolerancia por defecto `0.001` en unidades del modelo; manifiesto lateral obligatorio (esquema v3) | `G9X1GeometryExportAdapter.java:291-301, 476-534`; `DxfFidelityManifestWriter.java` |
| `SPLINE` y bulges | Nunca se escriben | `DxfExporter.java:468-478` |
| Importación DXF | No existe ningún lector DXF ni evaluador NURBS o de de Boor en el código principal | búsqueda en todo `src/main`; informe §A.4, §A.5, §A.8 |
| Reserva normativa | `SPLINE` queda fuera hasta disponer a la vez de exactitud matemática y de evidencia independiente de conformidad de lectores; la vía exige subfase propia | ADR 0014, decisión 8; `dxf-curve-fidelity-and-approximation.md` §4 y §9 (X1C); roadmap, nota de `PRE-G9B-S1` |
| Condiciones autorales D-V4 | Conservar calidad, procedencia y tolerancias de cada representación derivada; importación DXF de solo lectura; una representación de intercambio nunca sustituye en silencio al objeto CeDG autoritativo | roadmap, registro del 2026-11-21; `post-r1-conditioned-use.md` §10 |
| Colocación | Un adaptador de importación o exportación pertenece a la capa externa al núcleo; el núcleo solo cuando la capacidad participa en el DAG, en la identidad o en la serialización, o la consumen varios frontales | AGENTS §4 y §13 |

### 1.3 Superficie de reutilización

Una familia que publique un `GeoLocusV2` mediante una subclase de `AlgoLocusV2` cuyo
evaluador implemente `LocusEvaluator2D`, `LocusDifferentialEvaluator2D` y
`PiecewisePolynomialLocus2D` obtiene sin cambios:

| Capacidad | Consumidor genérico |
|---|---|
| Publicación, revisión, definición, existencia probada por el proveedor | `AlgoLocusV2`, `LocusExistenceStructure2D.providerProved` |
| Derivadas, longitud total y parcial, `LocusLength` | `PolicyV2LocusMetricCapability2D`, `DifferentialLocusMetricCapability2D` (los nudos son puntos de ruptura) |
| Intersecciones unilaterales con recta, segmento, rayo, circunferencia, cónica y polinómica implícita; tokens R4 | `PolynomialTargetIntersectionCapability2D` (grado compuesto `≤ 96`) |
| Pares locus × locus, solo ricos | `PiecewisePolynomialPairIntersectionCapability2D` |
| Punto sobre la curva, inversión y continuación | `LocusPointInteractionResolver2D`, `LocusPointContinuation2D` |
| Semejanzas R5 | `LocusSimilarityEvaluator2D` |
| Dibujo | `DrawLocusV2`, `LocusRenderCache2D` |
| Exportación DXF y LaTeX aproximadas | `SemanticCurveExportAdapter2D` |
| Persistencia, copia y Undo | XML de comando + identidad de `GeoLocusV2` |

No se obtienen sin trabajo adicional (están ligados a SplineV2): el modelo certificado de
intervalos (`CertifiedIntervalCurveModel2D.capture`, interfaz privada de paquete), los
tokens D2 de pares (`d2-spline-pair-singleton-germ/v1`, enumerados en
`spline-v2-pair-materialization.md` §10), la certificación estructural de objetivos
implícitos (ADR 0022, corrección B), el certificador D-CU2-9, los extremos de longitud
parcial en los puntos definidores (dependen de la interpolación) y el cableado de
producto (cadena de tipo, estilo por defecto, familia de inserción, modos de herramienta,
fábrica).

Qué consumidores dependen solo de `LocusEvaluator2D` y `LocusDifferentialEvaluator2D`, y
cuáles exigen además `PiecewisePolynomialLocus2D`, no está caracterizado consumidor por
consumidor [I]. Es la pregunta que decide cuánto hereda una curva racional sin extensión del
núcleo y la debe responder `MAINT-A-N1` (§10.2).

## 2. Definición matemática de la familia

### 2.1 Datos que definen el objeto

| Dato | Definición | Precondición |
|---|---|---|
| Grado `p` | entero | `1 ≤ p ≤ p_max` (propuesta `p_max = 7`, decisión D-A-3) |
| Puntos de control | lista ordenada `P_0, …, P_n` de puntos 2D finitos | `n + 1 ≥ p + 1` |
| Pesos | `w_0, …, w_n` | **implícitos e iguales a 1 en la primera implementación**; en la extensión racional, explícitos, finitos y estrictamente positivos (§10) |
| Vector de nudos | `U = (u_0, …, u_m)`, `m = n + p + 1`, no decreciente | multiplicidad interior `≤ p`; multiplicidad extrema `= p + 1` en el alcance inicial |
| Dominio | `[a, b] = [u_p, u_{n+1}]` | `a < b` |
| Orientación | la de `u` creciente | — |

La definición general de la familia es racional:

\[
C(u) = \frac{\sum_{i=0}^{n} N_{i,p}(u)\,w_i\,P_i}{\sum_{i=0}^{n} N_{i,p}(u)\,w_i},
\qquad u \in [a,b],
\]

y su subconjunto polinómico, que es el único que implementa `MAINT-A2`, es el caso
`w_i ≡ 1`, donde el denominador vale 1 por partición de la unidad:

\[
C(u) = \sum_{i=0}^{n} N_{i,p}(u)\,P_i .
\]

Las funciones base son las de Cox–de Boor

\[
N_{i,0}(u) = \begin{cases}1 & u_i \le u < u_{i+1}\\ 0 & \text{en otro caso}\end{cases},
\qquad
N_{i,p}(u) = \frac{u-u_i}{u_{i+p}-u_i}N_{i,p-1}(u) + \frac{u_{i+p+1}-u}{u_{i+p+1}-u_{i+1}}N_{i+1,p-1}(u),
\]

con el convenio `0/0 := 0`. La propiedad de un nudo interior se asigna al tramo de su
derecha y `u = b` pertenece al último tramo no degenerado; es la misma regla que fija
`semantic-spline-2d.md` §3 para SplineV2 y debe ser única.

Forma homogénea: con `P_i^w = (w_i x_i, w_i y_i, w_i)`, la curva homogénea
`C^w(u) = Σ N_{i,p}(u) P_i^w` es una B-spline polinómica en `R^3` y `C` es su proyección
central. Evaluar siempre en forma homogénea con `w ≡ 1` da exactamente la curva polinómica y
permite añadir pesos sin cambiar el algoritmo.

### 2.2 Propiedades garantizables por la definición (no por muestreo)

Para la familia polinómica:

1. **Soporte local.** `N_{i,p}` se anula fuera de `[u_i, u_{i+p+1})`.
2. **Partición de la unidad y no negatividad** en `[a, b]`. Cada tramo `[u_j, u_{j+1})`
   está en la envolvente convexa de `P_{j-p}, …, P_j`, lo que da cajas englobantes
   rigurosas por tramo sin evaluar la curva.
3. **Invariancia afín.** La imagen de `C` por una afinidad `A` es la B-spline con los mismos
   `p` y `U` y puntos de control `A(P_i)`. Las semejanzas y afinidades son exactas sobre los
   datos de control; las proyectividades no lo son en la familia polinómica.
4. **Derivada exacta.** `C'(u) = Σ_{i=0}^{n-1} N_{i+1,p-1}(u) Q_i`, con
   `Q_i = p (P_{i+1} − P_i) / (u_{i+p+1} − u_{i+1})`. La hodógrafa es otra B-spline de grado
   `p − 1`: su envolvente convexa por tramo acota `‖C'‖` y, si no contiene el origen,
   certifica regularidad en el tramo.
5. **Disminución de la variación.** Una recta corta la curva como mucho tantas veces como
   al polígono de control. Junto con la inserción de nudos (Boehm), que no cambia la curva,
   permite aislar raíces frente a rectas de forma certificable.
6. **Polinomialidad por tramo.** En cada tramo no degenerado `C` es un polinomio de grado
   `≤ p`. Para un objetivo algebraico `G(x, y) = 0` de grado `d`, `G(C(u))` es un polinomio
   de grado `≤ d·p` por tramo. Las intersecciones con recta, circunferencia y cónica se
   reducen al aislamiento de raíces polinómicas por tramo, que el aparato de SplineV2 ya
   realiza (`PolynomialTargetIntersectionCapability2D`).
7. **Longitud.** `∫ ‖C'(u)‖ du` no tiene forma cerrada en general. Exige cuadratura
   adaptativa con estimación de error (AGENTS §11.4), rompiendo en los nudos; el
   integrando es analítico por tramo salvo donde `C' = 0`.

Lo que cambia con pesos (relevante para §8-§10):

8. Con pesos positivos se conservan el soporte local, la envolvente convexa por tramo, la
   invariancia afín (los pesos no cambian) y la disminución de la variación. Se gana la
   **invariancia proyectiva**: una proyectividad actúa exactamente sobre los `P_i^w`.
9. La derivada es `C' = (A' − W' C) / W`, con `A = Σ N w P` y `W = Σ N w`; ya no es una
   B-spline de grado `p − 1` y la regularidad no se certifica con la hodógrafa polinómica.
10. `G(C(u)) = 0` equivale a `W(u)^d · G(A(u)/W(u)) = 0`, que es un polinomio de grado
    `≤ d·p` por tramo en el numerador homogéneo. Las intersecciones algebraicas siguen
    siendo aislamiento de raíces polinómicas, pero sobre una interfaz que publique `A` y
    `W` por tramo, que hoy no existe (§10.2).
11. `W(u) ≥ min_i w_i > 0` en todo el dominio, por partición de la unidad. Es la cota que
    hace certificable el error de una aproximación polinómica (§9.5).
12. Un cambio de pesos que conserva la curva (reparametrización de Möbius) cambia la
    parametrización: los puntos dirigidos por `u` se moverían. La identidad de una dirección
    por parámetro no sobrevive a esa reparametrización y el contrato debe declararlo.

### 2.3 Continuidad paramétrica, continuidad geométrica y regularidad

| Noción | Definición | Qué la determina |
|---|---|---|
| Continuidad paramétrica `C^k` en un nudo | derivadas de orden `≤ k` respecto de `u` iguales por ambos lados | **la estructura de nudos**: un nudo interior de multiplicidad `k_j` da al menos `C^{p - k_j}`, también con pesos positivos. Garantía estructural exacta, independiente de los puntos de control |
| Regularidad | `C'(u) ≠ 0` | **los puntos de control** (y los pesos): puntos de control consecutivos coincidentes o una hodógrafa que pasa por el origen producen puntos estacionarios y posibles cúspides |
| Continuidad geométrica `G^1` | tangente unitaria continua | regularidad a ambos lados más `C^1`, o `C^0` con `P_{j-1}, P_j, P_{j+1}` alineados y `P_j` entre ellos |
| `G^2` | curvatura continua | `C^2` más regularidad; no se propone garantizarla de otro modo |

Consecuencias que el contrato debe declarar:

- una curva `C^{p-1}` puede tener una cúspide en un tramo interior: la continuidad
  paramétrica no certifica suavidad geométrica (lo mismo ocurre hoy en SplineV2);
- un nudo de multiplicidad `p` hace pasar la curva por un punto de control y admite una
  esquina;
- un nudo interior de multiplicidad `p + 1` rompe la curva. Se propone **rechazarlo** con
  causa tipada: admitirlo exigiría varias componentes y no aporta nada que dos objetos no
  den.

Certificación propuesta: la continuidad paramétrica se publica desde la estructura de
nudos (exacta). La regularidad y `G^1` quedan, en el alcance inicial, como en SplineV2
(`FLOATING_POINT_UNCERTIFIED`, `SINGULAR` solo si la velocidad flotante es exactamente
cero); su certificación por la hodógrafa es parte del incremento de modelo certificado
(§6.4, `MAINT-A4`).

### 2.4 Nudos múltiples

| Multiplicidad de un nudo interior | Continuidad | Efecto geométrico |
|---|---|---|
| 1 | `C^{p-1}` | transición suave estándar |
| `1 < k < p` | `C^{p-k}` | menos suavidad; para `p = 3, k = 2`, `C^1` |
| `p` | `C^0` | la curva pasa por `P_j`; posible esquina |
| `p + 1` | discontinua | rechazado |
| `> p + 1` | — | inválido |

Multiplicidad `p + 1` en ambos extremos (curva **anclada**) da `C(a) = P_0`,
`C(b) = P_n` y tangentes de extremo dirigidas por `P_1 − P_0` y `P_n − P_{n-1}`, también con
pesos positivos.

### 2.5 Extremos, cierre y periodicidad

| Forma | Definición | Continuidad en la costura | Alcance |
|---|---|---|---|
| Abierta anclada | multiplicidad `p + 1` en `a` y `b` | — | inicial |
| Cerrada `C^0` | anclada con `P_n` exactamente igual a `P_0` (mismo objeto o mismas coordenadas binary64); con pesos, el cierre depende solo de los puntos extremos | `C^0`; la costura es una esquina salvo alineación | inicial; el cierre se reconoce por igualdad exacta, **nunca por proximidad**, como en SplineV2 y en `G9X1LocusV2ExportTest` l06 |
| Periódica | los últimos `p` puntos de control repiten los `p` primeros y los nudos son periódicos | `C^{p-1}` | incremento posterior; necesita una prueba exacta de los chorros en la costura análoga a la de ADR 0022 |
| Abierta no anclada | extremos de multiplicidad `≤ p` | — | incremento posterior; la curva no pasa por `P_0`, `P_n` |

Una curva periódica o no anclada es convertible en una anclada insertando nudos en `a` y
`b` hasta multiplicidad `p + 1`. La conversión no cambia la curva en aritmética exacta,
pero en binary64 los puntos de control insertados se redondean: es una equivalencia salvo
redondeo declarado, no una igualdad de datos.

### 2.6 Precondiciones, invariantes y degeneraciones

Precondiciones de admisión (fallo tipado, sin conservar geometría anterior): grado entero
dentro de la política; `n + 1 ≥ p + 1`; puntos de control definidos y finitos; nudos
finitos, no decrecientes, en número `m + 1 = n + p + 2`; multiplicidades según §2.4;
dominio de longitud positiva; límite de tamaño (propuesta: `n + 1 ≤ 128`, decisión D-A-3);
y, en la extensión racional, pesos finitos y estrictamente positivos con un cociente
`max w / min w` acotado por la política.

Invariantes: evaluación determinista e independiente de vista, escala, DPI y teselación;
parámetro canónico `u`, nunca el índice de muestra ni la longitud de arco; la estructura de
nudos determina la continuidad publicada; preimágenes distintas de un mismo punto (lazos)
se mantienen distintas.

| Degeneración | Consecuencia propuesta |
|---|---|
| Puntos de control consecutivos coincidentes | definida; posible punto estacionario; regularidad no establecida en el tramo |
| Puntos de control alineados | definida; imagen en una recta, con posible retroceso |
| Todos los puntos de control coincidentes | definida; curva reducida a un punto, longitud 0 (objeto degenerado declarado) |
| Tramos de longitud paramétrica nula (nudos repetidos) | no evaluados; propiedad de nudo única |
| Autointersección | admitida; preimágenes distintas |
| Punto de control, grado, nudo o peso indefinido | objeto indefinido con causa tipada |
| Peso nulo o negativo (extensión racional) | rechazo tipado; el denominador podría anularse y la envolvente convexa deja de valer |

### 2.7 Política numérica

- Evaluación: algoritmo de de Boor en binary64 sobre los puntos homogéneos (combinaciones
  convexas, estable); con `w ≡ 1` coincide con el de Boor polinómico. Los resultados son
  numéricos y así se declaran.
- Hechos estructurales (multiplicidades, orden de continuidad, anclaje, cierre exacto,
  igualdad exacta de todos los pesos): exactos sobre los datos capturados, sin tolerancias.
- Publicación polinómica: la interfaz `PiecewisePolynomialLocus2D` exige coeficientes en
  potencias del parámetro canónico global. Con nudos de magnitud grande o grado alto, la
  conversión a la base de potencias global está mal condicionada. Opciones técnicas (sin
  decisión autoral): una extensión aditiva de la interfaz con base local por tramo,
  invisible para SplineV2, o una comprobación de condicionamiento que falle cerrada. Se
  resuelve en la especificación (`MAINT-A-N1`).
- Pesos (extensión racional): no se normalizan en silencio; los datos persistidos son los
  del usuario. Una curva con todos los pesos exactamente iguales es exactamente polinómica
  y puede publicarse como tal; la igualdad se comprueba de forma exacta, nunca con
  tolerancia.

## 3. Comparación con SplineV2

| Dimensión | SplineV2 (vigente) | Familia B-spline propuesta |
|---|---|---|
| Definición matemática | Interpolante: pasa por sus datos; coeficientes por sistema lineal denso | De control: combinación convexa de sus datos (racional en general, polinómica en el primer subconjunto); sin sistema lineal |
| Datos del usuario | Puntos de paso, grado, peso opcional | Puntos de control, grado, nudos; pesos en la extensión racional |
| Parámetro semántico | `t ∈ [0, 1]` por cuerda normalizada, derivado de las posiciones de los puntos | `u` del vector de nudos, dato explícito del usuario e independiente de las posiciones |
| Efecto de mover un dato | Global (todo el sistema cambia) y desplaza los nudos | Local (`p + 1` tramos) y no cambia los nudos |
| Identidad semántica | `GeoLocusV2`, rama `spline-v2/main`, una componente | `GeoLocusV2`, rama propia, una componente |
| Dependencias dinámicas | Lista, puntos, grado y peso son padres del DAG | Lista de puntos de control, grado, nudos (y pesos) son padres del DAG |
| Persistencia | Comando `SplineV2` y sus entradas; sin versión explícita | Comando nuevo con contrato versionado desde el inicio (§6.3) |
| Continuidad | `C^{d-1}` exacta en todos los nudos; nunca menos | `C^{p-k}` por nudo según multiplicidad; esquinas declaradas posibles |
| Certificabilidad | Modelo de intervalos propio, tokens D2, objetivos implícitos certificados | Inicialmente flotante (como las fuentes polinómicas genéricas); el modelo certificado sería un incremento propio, más simple que el de SplineV2 por la envolvente convexa (§2.2) |
| Evaluación | Base de potencias global con caché redondeada; numeradores exactos | de Boor homogéneo; tramos polinómicos publicados para los consumidores |
| Longitudes | Genéricas de Locus V2; extremos en puntos definidores | Genéricas de Locus V2; los puntos de control no están en la curva salvo los extremos anclados |
| Intersecciones | Polinómicas por tramo; pares certificados | Polinómicas por tramo; pares solo ricos al principio |
| Transformaciones | Semejanzas por composición; las afinidades no conservan la familia (la imagen afín de una interpolante por cuerda no es la interpolante de las imágenes) | Semejanzas por composición desde el principio; afinidades exactas sobre la red de control como extensión posible; proyectividades exactas solo con pesos |
| Compatibilidad de documentos | Debe conservarse bit a bit; cualquier cambio de ecuaciones es incompatible | Sin documentos previos; lectores anteriores a la versión fallan cerrados (comando desconocido) |
| DXF | `LWPOLYLINE` aproximada; un `SPLINE` solo con puntos de control derivados y redondeados | `LWPOLYLINE` aproximada al principio; `SPLINE` con los datos autoritativos sin transformar en un incremento posterior |

Implementar las dos familias juntas no duplica semántica: comparten todo el aparato
genérico de §1.3 y solo difieren en el modelo y el evaluador. El coste propio de la familia
nueva es el comando, el algoritmo, el evaluador, la persistencia versionada, el cableado de
producto mínimo y sus pruebas.

## 4. Alternativas arquitectónicas

| Alternativa | Descripción | Ventajas | Inconvenientes | Valoración |
|---|---|---|---|---|
| α — ampliar SplineV2 | Nuevas formas de argumentos (por ejemplo `SplineV2(lista, grado, nudos)`) que interpretan la lista como red de control | Reutiliza tipo, herramientas y estilos | Dos semánticas incompatibles bajo un nombre y un tipo; enmienda de `semantic-spline-2d.md` §1 aprobado; el tercer argumento hoy es una función de peso; riesgo de regresión sobre pruebas CORE y Classic; documentos sin versión; ninguna vía limpia hacia pesos racionales | Desaconsejada |
| **β — familia nueva preparada para NURBS, publicada como `GeoLocusV2`** | Algoritmo, modelo y evaluador homogéneo propios; mismo tipo de resultado `GeoLocusV2`; primer subconjunto polinómico | Semántica limpia; reutilización casi completa (§1.3); SplineV2 intacta; persistencia versionada desde el origen; los pesos son una versión de la misma familia | Comando, traducciones, huellas R5-B y pruebas nuevas; certificación de pares no heredada | **Recomendada** |
| β' — familia polinómica cerrada y otra familia NURBS después | Igual que β, pero sin prever pesos | Ligeramente más simple ahora | Dos familias, dos comandos y dos contratos para una misma noción; migración de objetos o duplicación de consumidores al llegar los pesos; contradice la orientación autoral | Rechazada |
| γ — Locus V2 genérico | `LocusV2(punto, conductor)` con un punto construido por fórmulas de de Boor | Ningún tipo nuevo | Evaluador por copias aisladas de la construcción (coste alto); pierde la estructura polinómica, los nudos y la continuidad estructural; DXF nunca exacto | Rechazada |
| δ — `Curve` Classic | Expresión paramétrica por trozos | Ningún código nuevo | Sin identidad semántica, sin nudos, sin dominio de Locus V2; ADR 0018 ya rechazó esta vía para SplineV2 | Rechazada |
| ε — representación B-spline interna compartida | Modelo B-spline interno usado por la familia y como representación derivada de SplineV2 | Una sola exportación `SPLINE` para ambas | La conversión de SplineV2 no es exacta en binary64; obliga a una clase de fidelidad nueva; amplía el alcance | Diferida (posible `MAINT-A5`) |

Clasificación según AGENTS §4: la familia cambia el significado de un objeto geométrico,
participa en el DAG, afecta a la identidad y a la serialización y la consumen varios
frontales: pertenece al **núcleo Java compartido**. La exportación `SPLINE` y la importación
de referencia son adaptadores fuera del núcleo; la aproximación (§9) es un servicio externo
que produce una construcción del núcleo.

## 5. Arquitectura preparada para NURBS

### 5.1 Una familia conceptual, un primer subconjunto

La identidad conceptual de la familia es «curva B-spline 2D definida por grado, red de
control, vector de nudos y pesos positivos». El subconjunto polinómico es el caso de pesos
unitarios implícitos. Consecuencias:

- un objeto polinómico creado por `MAINT-A2` sigue siendo un objeto válido de la misma
  familia cuando exista la extensión racional, sin migración ni cambio de tipo;
- la extensión racional añade una forma de datos (pesos explícitos) y una versión del
  contrato, no un tipo ni un comando conceptualmente distinto. El nombre visible de la forma
  racional (una forma más de `BSpline` o un comando `NURBS` que produce la misma familia) se
  decide en `MAINT-A-R` (D-A-5);
- convertir un objeto polinómico en racional, o al revés, es una redefinición ordinaria de
  sus entradas y sigue las reglas de identidad de cualquier redefinición.

### 5.2 Qué fija la arquitectura desde el origen y qué se difiere

| Aspecto exigido | Primera implementación (`MAINT-A2`) | Preparación para NURBS | Extensión racional (`MAINT-A-R`) |
|---|---|---|---|
| Grado, red de control, vector de nudos, multiplicidades, dominio y orientación | §2.1, §2.4, §2.5 | iguales para la forma racional | sin cambios |
| Pesos | implícitos, `w_i ≡ 1`; no se serializan | modelo de datos y evaluador con un componente homogéneo; ranura de pesos reservada en el contrato | explícitos, finitos, positivos, persistidos |
| Continuidad paramétrica | estructural por multiplicidad (exacta) | la misma regla vale con pesos positivos | sin cambios |
| Regularidad y degeneraciones | flotante, como SplineV2 | declaradas en términos de `A` y `W` | certificación propia (la hodógrafa polinómica no basta) |
| Evaluación | de Boor homogéneo con `w ≡ 1` | mismo algoritmo | mismo algoritmo con pesos; derivadas por cociente |
| Definición y persistencia versionadas | contrato `bspline-2d/v1` serializado | `v1` es subconjunto exacto de la versión racional | `bspline-2d/v2`; un lector `v1` falla cerrado ante `v2`; un lector `v2` lee `v1` como `w ≡ 1` |
| Identidad semántica y dependencias dinámicas | `GeoLocusV2` con identificador duradero; puntos, grado y nudos como padres del DAG | los pesos serán padres del DAG como los demás datos | sin cambios de identidad |
| Compatibilidad con consumidores de Locus V2 | hereda §1.3 por `PiecewisePolynomialLocus2D` | ninguna interfaz racional genérica en el núcleo | interfaz por tramos de numeradores homogéneos y extensión de las capacidades de intersección (§10.2) |
| Separación de evaluación numérica, evidencia condicionada y resultados certificados | evaluación `FLOATING_POINT_UNCERTIFIED`; existencia probada por el proveedor, sin regiones condicionadas propias; resultados certificados solo con `MAINT-A4` | la misma separación | garantías propias por capacidad |

### 5.3 Lo que no se introduce ahora

- Ninguna interfaz racional general en el núcleo (`PiecewisePolynomialLocus2D` no cambia).
- Ninguna forma de comando con pesos ni ningún peso serializado en `bspline-2d/v1`.
- Ninguna conversión automática de una curva racional en polinómica ni de una importada en
  objeto CeDG.

Sí se fija ahora, porque cambiarlo después alteraría la identidad conceptual: el nombre y el
alcance de la familia, la forma homogénea del evaluador, el identificador de contrato
serializado y su regla de versiones, y la regla de que los pesos serán datos del usuario y
padres del DAG.

Biblioteca de evaluación: se recomienda un único núcleo matemático puro y sin estado
(datos → puntos y derivadas, de Boor homogéneo, inserción de nudos y conversión por tramos a
Bernstein), sin dependencias de `Construction`, `GeoElement` ni de la vista. Lo usan la
familia del núcleo (`MAINT-A2`), el lector de referencias (`MAINT-A-DXF0`) para el render y
el ajuste de aproximaciones (`MAINT-A-DXF1`). Una biblioteca matemática no es una autoridad
geométrica: la autoridad nace del contrato de la familia en el núcleo. La alternativa,
evaluadores separados en el núcleo y en el adaptador, duplica código numérico y sus pruebas.
La colocación exacta (módulo compartido fuera de los paquetes `kernel`) la fija
`MAINT-A-N1`.

### 5.4 Cuatro capas que no se confunden

| Capa | Qué es | Autoridad | Ejemplo |
|---|---|---|---|
| 1. Geometría autoritativa CeDG | Objeto del DAG con definición, identidad, dependencias y garantías declaradas | sí, la única | `GeoLocusV2` de la familia B-spline o de SplineV2 |
| 2. Definición importada de referencia | Datos leídos de un archivo externo con su procedencia; no participan en el DAG | ninguna sobre la construcción; es un dato externo inmutable | `SPLINE` racional leída de un DXF (§8) |
| 3. Aproximación matemática | Construcción CeDG nueva (capa 1) cuya relación con la referencia es un ajuste con error declarado | la de su propia definición; ninguna sobre la referencia | B-spline polinómica ajustada a una `SPLINE` importada (§9) |
| 4. Render | Lo que la vista dibuja de cualquiera de las anteriores | ninguna; no certifica | teselación de una referencia o de un `GeoLocusV2` |

Reglas: ninguna capa sustituye en silencio a otra (D-V4); ninguna asociación entre capas se
establece por coordenadas, proximidad o nombres; la relación entre una aproximación y su
referencia es solo de procedencia; el render de una referencia no es evidencia de nada.

## 6. Primera implementación polinómica: alcance, API y persistencia

### 6.1 Alcance inicial (`MAINT-A2`)

Incluido: abierta anclada y cerrada `C^0` por igualdad exacta de extremos; polinómica
(pesos implícitos unitarios); `1 ≤ p ≤ 7`; multiplicidad interior `≤ p`; `n + 1 ≤ 128`;
evaluación, derivada, longitud, intersecciones unilaterales con los objetivos polinómicos
vigentes, pares solo ricos, punto sobre la curva, semejanzas R5 por composición, dibujo,
exportación aproximada existente, persistencia versionada, Undo/Redo y reapertura.

Excluido del primer incremento: pesos, periódica, no anclada, multiplicidad `p + 1`,
afinidades exactas sobre la red de control, modelo certificado y tokens D2 de pares,
herramienta gráfica dedicada (el comando basta para validar la semántica), exportación
`SPLINE`, importación DXF.

### 6.2 API conceptual

| Forma | Significado |
|---|---|
| `BSpline(<Lista de puntos de control>, <Grado>, <Lista de nudos>)` | Forma completa y explícita |
| `BSpline(<Lista de puntos de control>, <Grado>)` | Nudos generados por la regla declarada `clamped-uniform/v1`: `p + 1` ceros, enteros consecutivos y `p + 1` repeticiones de `n − p + 1`. La regla forma parte del contrato persistido |
| `BSpline(<Lista de puntos de control>)` | Grado 3 y regla `clamped-uniform/v1` (decisión D-A-2) |

Resultado: un `GeoLocusV2` con cadena de tipo `BSpline`, identificador duradero nuevo y
rama propia (por ejemplo `bspline-2d/main`). Las entradas sin etiqueta se promueven como en
SplineV2 (observación `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION`), para que el DAG sea
idéntico en vivo y tras reabrir. El parámetro de `Point(L, u)` y de `LocusLength(L, A, B)`
es `u`.

Nombre: `BSpline` está libre (no existe en `Commands.java` ni en los paquetes de
traducción). Requiere cabecera canónica en inglés (ADR 0031), entradas `es`/`en` y
regenerar el corpus de huellas R5-B. La forma con pesos queda reservada a `MAINT-A-R`.

### 6.3 Persistencia y compatibilidad

- XML de comando ordinario más un identificador de contrato **serializado** (por ejemplo
  `bspline-2d/v1`), a diferencia de SplineV2. El mecanismo concreto (atributo del comando u
  otro) lo fija `MAINT-A-N1` tras comprobar qué admite el lector XML. Una versión futura que
  añada formas (pesos, periódica) es distinguible y un lector antiguo falla cerrado con causa
  tipada.
- Ningún documento existente cambia: la familia es nueva. Los documentos con `BSpline` no
  se abren en 1.2.2 (comando desconocido), consecuencia normal de una capacidad nueva que
  la nota de versión debe declarar.
- Exportación `.ggb` Classic: como SplineV2, no aplicable (GeoCeDG solo escribe documentos
  nativos).
- Procedencia de aproximación (§9.7): si se adopta, es un bloque pasivo y opcional del mismo
  contrato, sin efecto en la evaluación.

### 6.4 Incrementos de la familia fuera de la secuencia principal

| Id | Contenido | Condición |
|---|---|---|
| `MAINT-A4` | Modelo certificado de intervalos (envolvente convexa por tramo, regularidad por hodógrafa), pares con tokens D2, objetivos implícitos certificados | enmienda tipo ADR 0028 de `spline-v2-pair-materialization.md` §10; solo con caso de uso |
| `MAINT-A5` | Representación `SPLINE` derivada de SplineV2 con una clase de fidelidad nueva («equivalente salvo redondeo de coeficientes derivados») | decisión autoral; diferida |
| `MAINT-A7` | Periódica, no anclada y afinidades exactas sobre la red de control | ampliación versionada de `bspline-2d` |

La importación DXF, que la revisión 1 dejaba como `MAINT-A6` diferido, pasa a ser
`MAINT-A-DXF0` (§8).

## 7. Interoperabilidad 2D de salida

### 7.1 Cuatro niveles de fidelidad que no deben confundirse

| Nivel | Qué es | Ejemplo en GeoCeDG |
|---|---|---|
| 1. Geometría semántica autoritativa | El objeto CeDG con su definición, dependencias, identidad y garantías | `GeoLocusV2` de SplineV2 o de la familia B-spline en el DAG |
| 2. Representación matemática equivalente en el formato | Una entidad cuya definición en el formato reproduce el conjunto de puntos sin aproximación, salvo el redondeo declarado de los datos | `CIRCLE`, `ARC`, `ELLIPSE`, `LINE`; `SPLINE` de una B-spline con los mismos datos binary64 |
| 3. Aproximación geométrica controlada | Una entidad que se aparta de la curva con tolerancia y error declarados | `LWPOLYLINE` con `ESTIMATED_ERROR` y manifiesto |
| 4. Representación gráfica | Lo que muestra la vista | exportaciones de imagen |

Ninguna exportación modifica el nivel 1 (`G9X1LocusV2ExportTest` ya exige que exportar no
mute DAG, XML ni Undo). Los niveles de §5.4 son los de los datos dentro de GeoCeDG; estos son
los de una entidad escrita en un formato de intercambio.

### 7.2 Qué se transfiere y qué no

| Información | Nivel 2 posible | Requiere aproximación o metadatos |
|---|---|---|
| Grado, nudos y puntos de control de la familia polinómica | Sí, como `SPLINE` no racional (grupos 70, 71, 72, 73, 40, 10/20) | — |
| Pesos de la extensión racional | Sí, como `SPLINE` racional (bandera 4, grupo 41), solo tras `MAINT-A-R` | — |
| Conjunto de puntos de SplineV2 | Solo con puntos de control derivados y redondeados | Clase de fidelidad nueva o `LWPOLYLINE` (vigente) |
| Cónicas | Circunferencia y elipse: sí, con entidades propias (vigente). Un arco de parábola es exactamente una Bézier cuadrática polinómica, representable como `SPLINE` no racional; la hipérbola exige pesos | Parábola e hipérbola: hoy aproximadas; su exportación como `SPLINE` sería un incremento propio |
| Dependencias, identidad duradera, continuidad certificada, grados de garantía, direcciones semánticas de puntos derivados | No existen en DXF | Solo en el manifiesto lateral (esquema v3, ampliable) |
| Regiones condicionadas de Locus V2 | No: su evidencia es `NUMERICAL_UNDER_HYPOTHESES` | Omitidas con diagnóstico (vigente, D-V4); su exportación semántica es requisito declarado de esta línea, sin diseño todavía |

### 7.3 Exportación `SPLINE` (incremento `MAINT-A3`)

- Escribir siempre una spline **no racional, anclada y no periódica** (bandera 8, plana;
  bandera 1 solo para el cierre exacto), con los nudos y puntos de control autoritativos
  mediante `Double.toString` (ida y vuelta exacta en binary64, ya usado por el escritor).
- No escribir puntos de ajuste (grupos 11/21/31): su interpretación depende del lector y
  no admite afirmación de exactitud.
- Clase de fidelidad: exacta en datos («transferencia exacta de la definición»), distinta
  de una afirmación sobre la geometría que reconstruya cada lector. La afirmación solo vale
  con evidencia de conformidad: el lector reproduce la evaluación de referencia en una
  batería de grados `1..7`, nudos no uniformes y múltiples y cierre `C^0`. El repositorio
  ya dispone de un flujo de comprobación con AutoCAD Core Console (registro de PRE-G9B-CU2,
  V-DXF-4); la decisión D-A-4 fija el segundo lector independiente. El lector propio de
  `MAINT-A-DXF0` sirve para pruebas de ida y vuelta, pero no cuenta como lector
  independiente.
- El manifiesto lateral registra familia, contrato, clase de fidelidad y datos de
  procedencia de cada entidad.

## 8. Importación DXF `SPLINE` de referencia (`MAINT-A-DXF0`)

### 8.1 Objetivo y límites

Leer entidades DXF `SPLINE` 2D, polinómicas y racionales, y representarlas fielmente como
**geometría importada de referencia**: visible, inspeccionable, con procedencia, y sin
semántica CeDG implícita. No es una NURBS nativa: no participa en el DAG, no se convierte en
`GeoLocusV2`, no es entrada de herramientas de construcción ni de capacidades de Locus V2, y
su render no certifica nada. El archivo DXF nunca se modifica.

### 8.2 Colocación

Modelo de importación de solo lectura en una **capa externa al núcleo** (AGENTS §4:
adaptador de importación), con tres piezas:

1. lector DXF: análisis del archivo, validación y diagnóstico por entidad;
2. modelo de referencia: registros inmutables (datos binary64 tal como se leen, más
   procedencia) que pertenecen a la sesión o al documento, nunca a `Construction`;
3. render: teselación en unidades del modelo con tolerancia declarada, calculada una vez
   por entidad e independiente del zoom; la vista solo la transforma y dibuja.

La evaluación usa la biblioteca matemática pura de §5.3.

### 8.3 Alcance inicial propuesto (decisión D-A-6)

| Aspecto | Admitido | Excluido o diferido, con diagnóstico explícito |
|---|---|---|
| Archivo | DXF ASCII con sección `ENTITIES`; versiones con entidad `SPLINE` (desde R13) | DXF binario |
| Espacio | espacio modelo | espacio papel (grupo 67 = 1); entidades dentro de bloques o `INSERT` |
| Definición | puntos de control, grado y nudos completos (grupos 71, 72, 73, 40, 10/20/30); pesos (41) cuando la bandera 4 lo indica | splines solo con puntos de ajuste (73 = 0, 74 > 0): su curva depende del lector. Si hay a la vez datos de control y de ajuste, se usan los de control y se registra la presencia de los de ajuste |
| Naturaleza | polinómica (sin bandera 4, o pesos ausentes) y racional (bandera 4 con un peso por punto de control) | incoherencias entre bandera y pesos, que se rechazan con causa |
| Pesos | finitos y estrictamente positivos; cociente `max w / min w` dentro de un límite declarado | pesos nulos, negativos o no finitos |
| Grado y tamaño | `1 ≤ p ≤ p_max_import` y `n + 1 ≤ N_max_import`, declarados (propuesta inicial: `p ≤ 7`, alineado con la familia, y `n + 1 ≤ 1 024`) | por encima de los límites |
| Nudos | número `= n + p + 2`, no decrecientes y finitos; anclados (multiplicidad `p + 1` en los extremos); multiplicidad interior `≤ p` | no anclados, periódicos (bandera 2) y multiplicidad interior `p + 1` |
| Planaridad | todos los puntos de control con la misma `z` (la curva está entonces en el plano `z = c`, exactamente, por partición de la unidad y pesos positivos); `c` se registra | `z` no constante (curva 3D) |
| Cierre | se reconoce solo por igualdad exacta de `P_0` y `P_n`; la bandera 1 es un indicio que se contrasta con los datos | cierre por proximidad; una bandera de cierre contradicha por los datos se diagnostica |
| Orientación y dominio | la del archivo; dominio `[u_p, u_{n+1}]`; nunca se invierte en silencio | — |
| Unidades y transformación | se registran las unidades del encabezado (`$INSUNITS`); por defecto, transformación identidad; una transformación afín explícita y elegida por el usuario se aplica a los puntos de control (exacta en aritmética exacta, con redondeo binary64 declarado) y se registra | escalados implícitos |

Cada entidad se importa o se rechaza de forma atómica; un archivo con entidades admitidas y
rechazadas produce las primeras y la lista de las segundas con su causa.

### 8.4 Procedencia

Por entidad: resumen criptográfico del archivo, nombre del archivo, versión DXF, handle
(grupo 5), capa (grupo 8), posición en la sección, contrato de importación
(`dxf-spline-reference/v1`, propuesto), transformación aplicada y lista de datos ignorados
(puntos de ajuste, tolerancias 42-44, tangentes 12/13). Nada de la procedencia depende de la
hora de importación.

### 8.5 Distinción frente a las construcciones CeDG

- Presentación propia (capa y estilo de referencia) y propiedad «Referencia importada,
  solo lectura» visible en el inspector.
- No seleccionable como entrada de herramientas de construcción ni como destino de ajuste
  magnético: un clic de la herramienta Punto sobre una referencia no crea un punto «sobre»
  ella, porque sería una asociación por coordenadas.
- Ningún comando la acepta como camino, objetivo de intersección ni curva para longitudes.
- La exportación DXF de GeoCeDG no la reescribe (sería pasar por el programa una entidad
  ajena como si fuera propia); se declara.

### 8.6 Persistencia (decisión D-A-7)

| Opción | Qué es | Núcleo | Valoración |
|---|---|---|---|
| R-a | Solo en la sesión; al reabrir hay que volver a importar | ninguno | **recomendada para la primera implementación**: valida lectura y fidelidad sin cambiar el formato |
| R-b | El documento guarda una referencia no geométrica (archivo, resumen, handles, transformación) y vuelve a leer el DXF al abrir; si falta o cambió, diagnóstico y nada se muestra | ninguno; cambia el contenido del archivo `.cedg` fuera de `Construction` | depende del archivo externo |
| R-c | El documento guarda una copia de las definiciones importadas y su procedencia, fuera de `Construction` | ninguno; cambia el contenido del archivo `.cedg` fuera de `Construction` | **siguiente paso recomendado**, tras caracterizar cómo trata 1.2.2 una entrada desconocida del archivo `.cedg` (sin caracterizar) |
| R-d | Objeto de referencia dentro de `Construction`, con identidad persistente y dependencias | sí, todas las de §8.7 | solo si se aprueba la aproximación dinámica (§9.8) |

### 8.7 Responsabilidades del núcleo si la referencia entra en el documento con identidad y dependencias (R-d)

1. Un tipo de elemento no constructivo, con `PersistentGeoId`, registro en
   `SpatialIdentityRegistry` y reglas de copia (una copia es una identidad nueva con la misma
   procedencia), borrado y Undo/Redo.
2. Captura inmutable de los datos binary64, con regla de lectura decimal declarada.
3. Serialización versionada, con lectores anteriores que fallen cerrados.
4. Participación en el DAG como objeto libre, con rechazo tipado en todo consumidor que no
   esté declarado (punto en camino, `Intersect`, `LocusLength`, semejanzas…), para que no se
   convierta en una segunda autoridad geométrica.
5. Dibujo propio y relación con capas y visibilidad.
6. Regla de reapertura: la copia guardada es la autoridad de la referencia y el archivo
   externo solo procedencia.
7. Exclusión del contrato espacial y de proyección (objeto 2D sin identidad espacial).
8. Pruebas, escenarios y huellas R5-B de cualquier comando nuevo.

Ni R-a ni R-c crean un DAG paralelo: la referencia no tiene dependencias, es un dato.

## 9. B-splines aproximantes de NURBS importadas (`MAINT-A-DXF1`)

### 9.1 Objetivo

A petición explícita del usuario, construir a partir de una entidad importada una B-spline
polinómica **nueva**: una construcción CeDG de la familia (§6), distinta de la curva
racional original, con error y procedencia declarados. Nunca se presenta como exacta ni sus
resultados (intersecciones, longitudes, puntos) se atribuyen a la curva original.

### 9.2 Conversión exacta frente a aproximación

| Caso de la fuente | Operación | Garantía |
|---|---|---|
| Polinómica (sin pesos, o todos los pesos exactamente iguales), anclada y dentro de los límites de la familia | **conversión exacta**: los mismos grado, nudos y puntos de control | exacta en datos; error 0 por definición |
| Polinómica fuera de los límites (grado o tamaño) | aproximación | la de §9.5 |
| Racional con pesos distintos | aproximación | la de §9.5 |

La conversión exacta no es una aproximación y se declara de otra forma.

### 9.3 Método recomendado para la primera implementación

Ajuste por mínimos cuadrados lineales **en el parámetro de la fuente** (correspondencia
paramétrica `A(u) ≈ C(u)` en el mismo dominio y orientación):

1. Datos del usuario: grado `q` (por defecto 3, `≤ 7`), tolerancia `ε` en unidades del
   modelo, número máximo de puntos de control (`≤ 128`).
2. Nudos iniciales: los nudos distintos de la fuente con multiplicidad `min(k, q)`; después,
   inserción de nudos en el punto medio de los tramos cuyo error estimado supera `ε`, hasta
   alcanzar `ε` o los límites.
3. Restricciones: interpolación exacta de los extremos (`A(a) = C(a) = P_0`,
   `A(b) = C(b) = P_n`, tomados de los datos y no evaluados); opcionalmente, dirección de
   tangente en los extremos.
4. Resolución por QR (no por ecuaciones normales), determinista.
5. Fallo tipado si `ε` no se alcanza dentro de los límites: ningún objeto parcial; se
   informa del mejor error estimado para que el usuario cambie `ε` o los límites.

Extremos, continuidad, singularidades y cierre:

- la continuidad del resultado es la de sus propios nudos (`C^{q-k}`), publicada por la
  familia; no se hereda de la fuente;
- una cúspide o un punto estacionario de la fuente no se reproduce como tal: el error
  paramétrico sigue acotado, pero el rasgo geométrico no se garantiza;
- una fuente cerrada exactamente produce una aproximación cerrada `C^0` exactamente, porque
  comparte el valor de extremo; la tangencia en la costura no se garantiza salvo restricción
  explícita.

Alternativa considerada, construcción manual asistida: el usuario coloca los puntos de
control con la referencia visible y el sistema mide el error. Sin correspondencia
paramétrica, la medida es una distancia de Hausdorff estimada, más cara y difícil de
certificar. Se recomienda como edición posterior de un ajuste, no como primera vía.

### 9.4 Por qué la correspondencia paramétrica

`sup_u ‖C(u) − A(u)‖` acota la distancia de Hausdorff entre las dos curvas: todo punto de
una está a esa distancia como mucho de algún punto de la otra. Una cota paramétrica es, por
tanto, una cota geométrica de posición. No acota diferencias de longitud, tangente o
curvatura, ni garantiza que las intersecciones de `A` estén cerca de intersecciones de `C`
(una tangencia puede desaparecer o desdoblarse).

### 9.5 Medidas de error y sus garantías

| Medida | Definición | Garantía | Primera implementación |
|---|---|---|---|
| Residuo en muestras | `max` y media cuadrática de `‖C(u_k) − A(u_k)‖` en las muestras del ajuste | hecho sobre esas muestras; nada entre ellas | sí |
| Estimación numérica del error entre curvas | máximo sobre un conjunto de verificación distinto y más denso, refinado por maximización local | `ESTIMATED`; no es cota | sí |
| Cota global certificada | `sup_u ‖C(u) − A(u)‖ ≤ max_k ‖d_k‖ / min_i w_i`, donde `d_k` son los coeficientes de Bernstein por tramo de `D(u) = Σ N_i w_i P_i − W(u) A(u)` (polinomio vectorial de grado `≤ p + q` por tramo, tras refinar ambas curvas a nudos comunes), calculados con redondeo dirigido | rigurosa respecto de los datos leídos en binary64: `W(u) ≥ min w_i` por partición de la unidad y `D(u)` está en la envolvente convexa de sus coeficientes; se ajusta por subdivisión | sub-incremento `MAINT-A-DXF1b` o dentro de A-DXF1 si su coste lo permite (D-A-9) |

La cota certificada se refiere a la curva definida por los datos ya leídos: el redondeo de
la lectura decimal del archivo se declara aparte.

### 9.6 Participación en Locus V2

La aproximante es una curva ordinaria de la familia y participa en las operaciones de Locus V2
admitidas para ella (§1.3), con las garantías de la familia. Ninguna de esas operaciones se
presenta como resultado sobre la fuente: el inspector y los textos dicen «sobre la
aproximación (error declarado)». La precisión atribuible a la fuente nunca supera la medida
de error disponible y, para intersecciones, tangentes y longitudes, no se atribuye ninguna.

### 9.7 Procedencia y persistencia

Registro de procedencia de la aproximante: resumen de la entidad fuente (archivo, handle,
datos), contrato de importación, método de ajuste (`bspline-param-lsq/v1`, propuesto), `q`,
`ε`, estrategia de nudos, medidas de §9.5 con su grado y el resumen de la definición de la
aproximante en el momento del ajuste.

- Si la definición de la aproximante cambia después (el usuario mueve un punto de control),
  las medidas quedan declaradas «no aplicables a la definición actual»; el objeto sigue
  siendo una B-spline válida. Se degrada la garantía, no el objeto.
- Lugar del registro (D-A-10): bloque pasivo del contrato de la familia (viaja con el
  objeto, cambio de serialización del núcleo pequeño) o registro del documento fuera de
  `Construction` (sin cambio del núcleo, se pierde al copiar el objeto). Se recomienda el
  primero.

### 9.8 Relación con la fuente: congelada o dinámica (D-A-10)

| Modo | Comportamiento ante cambios de la fuente | Requisitos |
|---|---|---|
| **Congelada e independiente** (recomendada) | ninguno: la aproximante es una construcción propia; si la fuente se vuelve a importar con otros datos, la procedencia indica «fuente distinta de la registrada» cuando ambas están presentes; nunca se reajusta sola | A2, A-DXF0 (R-a basta) |
| Dinámica vinculada | la aproximante es dependiente de la referencia y se reajusta en cada revisión, con sus medidas | referencia en `Construction` (R-d, §8.7), ajuste determinista como función de la fuente, medidas por revisión y política de fallo cuando `ε` deja de alcanzarse; diferida |

## 10. NURBS semántica nativa (`MAINT-A-R`): diseñada, no autorizada

### 10.1 Qué aporta

Representación exacta de hipérbolas y arcos de circunferencia y elipse dentro de la familia,
invariancia proyectiva exacta sobre los datos, importación de `SPLINE` racionales como
objetos CeDG nativos (no solo como referencia) y exportación `SPLINE` racional.

### 10.2 Qué exige del núcleo

- Versión `bspline-2d/v2` con pesos persistidos y padres del DAG.
- Una interfaz por tramos de numeradores homogéneos (`A_x`, `A_y`, `W` polinómicos por
  tramo) y la extensión de `PolynomialTargetIntersectionCapability2D` para aislar raíces de
  `W^d · G(A/W)`; los pares racional × racional exigen un método propio.
- Métricas: cuadratura sobre `‖C'‖` con `C' = (A' − W' C) / W`, analítica por tramo porque
  `W > 0`; inventario previo de qué consumidores dependen solo de los evaluadores genéricos
  (§1.3).
- Certificación propia: la envolvente convexa sigue valiendo, la regularidad no se certifica
  con la hodógrafa polinómica.
- Política de pesos: positividad estricta, cociente acotado, ninguna normalización
  silenciosa, y declaración de que una reparametrización de Möbius mueve las direcciones
  por parámetro (§2.2, punto 12).
- Nombre visible de la forma racional (D-A-5).

### 10.3 Estado

Diseñada en esta propuesta para que `MAINT-A-N1` y `MAINT-A2` no la impidan; ni su
especificación ni su implementación están autorizadas en este programa. Se abrirá, si el
autor lo decide, con su propia especificación, su prompt y su puerta.

## 11. Secuencia del bloque A y dependencias técnicas

| Id | Contenido | Depende técnicamente de | Puede ir en paralelo con |
|---|---|---|---|
| `MAINT-A-N1` | Diseño normativo: especificación de la familia `bspline-2d` preparada para NURBS (v1 polinómica) y su ADR; contrato de importación de referencia `dxf-spline-reference/v1`; colocación de la biblioteca matemática; mecanismo de versión serializada | decisiones D-A-1..D-A-7 | bloques B y C |
| `MAINT-A-DXF0` | Importación DXF `SPLINE` de solo lectura (§8) | aprobación del contrato de importación de A-N1; D-A-6, D-A-7 | `MAINT-A2` |
| `MAINT-A2` | Familia polinómica semántica (§6) | aprobación de la especificación de familia de A-N1 | `MAINT-A-DXF0` |
| `MAINT-A-DXF1` | Aproximación explícita (§9), con su contrato breve aprobado antes del código | `MAINT-A2`, `MAINT-A-DXF0`; D-A-8..D-A-10 | `MAINT-A3` |
| `MAINT-A3` | Exportación DXF `SPLINE` polinómica (§7.3) | `MAINT-A2`; D-A-4 | `MAINT-A-DXF0`, `MAINT-A-DXF1` |
| `MAINT-A-R` | NURBS semántica nativa (§10) | `MAINT-A2` y decisión propia | — (no autorizada) |

No hay orden artificial entre la importación de referencia, la familia polinómica y la
exportación: A-DXF0 no espera a A2, y A3 no espera a la importación. La única coordinación
es la biblioteca matemática de §5.3, que comparten A-DXF0 y A2: la crea el primero que se
autorice con la API fijada por A-N1; si se autorizan a la vez, la crea A-DXF0 en un primer
commit que A2 toma como base, para que dos ramas no editen los mismos archivos.

## 12. Casos de validación propuestos

| Id | Caso | Propiedad |
|---|---|---|
| BS-1 | Grado 1 con nudos uniformes | coincide exactamente con la poligonal de control |
| BS-2 | Cúbica anclada uniforme de 4 puntos | coincide con la Bézier cúbica (orientación `t = 0 → P_0`, opuesta a `BezierCurve` Classic) |
| BS-3 | Cúbica con nudos no uniformes | evaluación de de Boor frente a una referencia racional exacta independiente |
| BS-4 | Nudo interior doble y triple en cúbica | `C^1` y `C^0` publicados; esquina con tres puntos de control no alineados |
| BS-5 | Multiplicidad `p + 1` interior; nudos decrecientes; `n < p` | rechazo tipado sin geometría obsoleta |
| BS-6 | Puntos de control consecutivos coincidentes | definida; regularidad no establecida; longitud correcta |
| BS-7 | Cierre exacto frente a cierre aproximado | cerrada `C^0` solo con igualdad exacta |
| BS-8 | Longitud total y parcial | frente a cuadratura de referencia; monotonía por rama; invariancia de zoom/DPI |
| BS-9 | Intersecciones con recta, circunferencia, cónica; tangencia; raíz en un nudo | residuos dentro de tolerancia; sin duplicados en nudos compartidos |
| BS-10 | Semejanzas R5 | la imagen evaluada coincide con la B-spline de los puntos transformados |
| BS-11 | Dinámica: mover un punto de control, cambiar grado y nudos | actualización local; estado indefinido y recuperación |
| BS-12 | Guardar, reabrir, Undo/Redo | identidad duradera y bytes estables; contrato serializado |
| BS-13 | No regresión de SplineV2 | selecciones CORE de SplineV2 sin cambios |
| BS-14 (`MAINT-A3`) | `SPLINE` leída por dos lectores independientes | puntos de referencia reproducidos por cada lector dentro de una tolerancia de lectura declarada |
| BS-15 | Evaluador homogéneo con `w ≡ 1` | idéntico bit a bit al de Boor polinómico en la batería BS-1..BS-4 |
| DX-1 (`MAINT-A-DXF0`) | `SPLINE` polinómica y racional (arco de circunferencia como cuadrática racional) | la evaluación reproduce puntos de referencia exactos (`(cos θ, sin θ)` para el arco) dentro de la tolerancia numérica declarada |
| DX-2 | Bandera 4 sin pesos, pesos no positivos, recuentos incoherentes, nudos decrecientes | rechazo por entidad con causa; el resto del archivo se importa |
| DX-3 | Spline solo con puntos de ajuste; periódica; no anclada; 3D; espacio papel; dentro de un bloque | rechazo o diferimiento diagnosticado |
| DX-4 | Bandera de cierre contradicha por los datos | diagnóstico; ningún cierre por proximidad |
| DX-5 | Archivo intacto | resumen del archivo idéntico antes y después; ningún DAG, XML ni Undo mutado |
| DX-6 | Distinción | la referencia no es seleccionable por herramientas de construcción ni aceptada por comandos de Locus V2 |
| DX-7 | Render | teselación idéntica a cualquier zoom; tolerancia declarada respetada frente a la evaluación |
| AP-1 (`MAINT-A-DXF1`) | Fuente polinómica dentro de límites | conversión exacta, datos idénticos |
| AP-2 | Cuarto de circunferencia racional, `q = 3` | medidas de §9.5 coherentes: residuo ≤ estimación ≤ cota certificada; las tres ≤ `ε` |
| AP-3 | `ε` inalcanzable | fallo tipado sin objeto |
| AP-4 | Edición de la aproximante | medidas declaradas no aplicables |
| AP-5 | Fuente cerrada | aproximación cerrada `C^0` exacta |
| AP-6 | Procedencia | guardar y reabrir conservan el registro; una copia conserva la procedencia con identidad nueva |

## 13. Decisiones autorales de este bloque

Están en la [hoja de decisiones](../validation/pre_g9b_maint_p0_author_decision_sheet.md):

- D-A-1, familia nueva preparada para NURBS frente a ampliación de SplineV2;
- D-A-2, parámetro y formas de comando;
- D-A-3, límites y alcance inicial;
- D-A-4, exportación `SPLINE` y lectores de conformidad;
- D-A-5, NURBS: arquitectura preparada y extensión nativa diferida;
- D-A-6, alcance inicial de la importación DXF;
- D-A-7, representación y persistencia de la referencia;
- D-A-8, tipo de aproximación ofrecida;
- D-A-9, política de tolerancias y garantías;
- D-A-10, relación entre referencia y aproximante.

## 14. Riesgos

| Riesgo | Mitigación |
|---|---|
| Condicionamiento de la publicación polinómica con nudos grandes o grado alto | base local por tramo o comprobación que falle cerrada (§2.7) |
| Confusión del usuario entre SplineV2 y la familia B-spline | nombres, ayudas y guía distintos; tipo de objeto visible en propiedades |
| Confusión entre referencia importada, aproximación y construcción | las cuatro capas de §5.4 con presentación, inspector y textos distintos; la referencia no es seleccionable por herramientas de construcción |
| Segunda autoridad geométrica o DAG paralelo por la importación | la referencia es un dato sin dependencias (R-a, R-c); R-d solo con las responsabilidades de §8.7 aprobadas |
| Lectores DXF que interpretan de forma distinta las banderas de cierre o periodicidad | escribir solo anclada no periódica; importar solo anclada no periódica; conformidad de lectores como puerta |
| Aproximaciones leídas como exactas | conversión exacta y aproximación declaradas de forma distinta; tres medidas de error con su grado; textos «sobre la aproximación» |
| Una familia polinómica que impida los pesos | §5.2 y §5.3 fijados en A-N1; caso BS-15 |
| Expectativa de certificación desde el inicio | declarar los pares solo ricos en A2 (como cualquier fuente polinómica genérica) y la cota certificada de aproximación como medida separada |
| Cabeceras de estado desactualizadas en ADR 0022, la nota de diseño estructural, la especificación de autoría de SplineV2 y la fila «Phase» de la especificación DXF | conciliación documental separada, autorizada por el autor (informe de caracterización, §A.6) |
