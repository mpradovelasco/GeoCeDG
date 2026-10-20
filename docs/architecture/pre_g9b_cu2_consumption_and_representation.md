# PRE-G9B-CU2 — Reglas de consumo de los certificados y estrategia de representación y teselación

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la preparación de `CU2` (entregables 6 y 7): cómo consumen la nueva estructura de existencia el dibujo, las métricas, las intersecciones, los puntos semánticos, la composición y las exportaciones DXF y LaTeX, y cómo se representa y tesela un Locus V2 (B06). No es especificación normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Antecedentes | [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md) §6; enmienda candidata A2.4; decisión DC-6 (2026-10-19: criterios de teselación con límites justificados por pruebas; 10° y profundidad 4 son candidatos) |
| Documentos hermanos | [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) |
| Revisión | 2026-10-20, revisión documental focalizada: principio 5 y §3 (disponibilidad, P5), §5.1 a §5.3 (parámetros singulares, pliegues y extremos no evaluables, P1–P3), §5.6 nuevo y §6 (fidelidad declarada). Cierre de coherencia (2026-10-20): §3.1 nuevo (componentes puntuales, C2), §3.2 nuevo (salidas efectivas en raíces coincidentes, C1), §5.2 y §6 |

## 1. Principios

1. **El núcleo compartido tiene la autoridad geométrica.** El dibujo y las aproximaciones de
   exportación derivan de la estructura de existencia certificada y nunca cambian la topología
   semántica: no unen, no cierran, no parten y no omiten componentes por su cuenta.
2. **Una sola autoridad de cierre.** Una curva se cierra solo si su componente está certificada
   como periódica cerrada (diseño de certificación §4.4).
3. **Cada consumidor declara su nivel.** Unos aceptan componentes localmente certificadas
   (nivel 3); otros exigen `COMPLETE` (nivel 4), y nunca aceptan menos.
4. **Ningún consumidor lee las vistas heredadas** para decidir la topología: ni
   `getValidDomainComponents()`, que solo tiene contenido con `COMPLETE`, ni el indicador
   `PERIODIC` del proveedor. Hoy la elección de vista explica la mayoría de las divergencias
   (§2).
5. **Fidelidad declarada (P5).** El dibujo y las exportaciones declaran su fidelidad efectiva y
   fallan explícitamente cuando no pueden representar un resultado. Nunca inventan un punto, una
   unión ni un extremo; la existencia certificada no es una garantía de sus coordenadas
   ([diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §3.4).

## 2. Estado actual por consumidor

Referencias al código en `27bf652f`. Prefijos: `L` = `kernel/locus`, `A` = `kernel/algos`,
`R` = `euclidian/draw`, `X` = `export`.

| Consumidor | Vista que lee | Consecuencia |
|---|---|---|
| Dibujo (`R/LocusRenderCache2D`) | lista certificada (94-100); cierre por indicadores (137-147) | cierre sin prueba de la costura; abandono del modo adaptativo en la primera evaluación inválida y retorno a 32 muestras uniformes (178-180, 205-207, 149-168); profundidad mínima 2 y una sola prueba de cuerda de 0,75 px (210-216); la proyección acotada de `screenChordError` (238-239) puentea un salto con un segmento |
| Exportación (`X/SemanticCurveExportAdapter2D`, `X/AdaptiveCurveApproximationBuilder2D`) | lista certificada; cierre por indicadores (471-480) | una componente con un extremo abierto se descarta como `MISSING_DOMAIN` (constructor 257-259); el residuo de cierre compara `P(−π)` con `P(π)` canónico, que es idénticamente 0 (281-295); la misma prueba de cuerda acotada (323-341) |
| Métrica total (`L/metric/LocusMetricEngine2D`) | exige `COMPLETE` en cada rama (93-102) | correcto para el total |
| Ruta entre posiciones (`L/metric/LocusMetricRouteResolver2D`) | cruza la costura solo con una única componente completa (102-111, 144-167) | origen de la envoltura de `w_1`; `resolveOpenBoundary` compara un índice de la lista certificada con el tamaño de la lista completa (180-184), una inconsistencia latente |
| Prueba de ciclo periódico (`PolicyV2LocusMetricCapability2D` 770-781) | lista completa | distinta de las variantes de solo evaluador (134-141) y diferencial (307-315) |
| Vinculación de posiciones (`LocusMetricPositionBinder2D` 26-86) | primera componente certificada que contiene el parámetro con el linaje coincidente | correcto si el linaje es estable |
| Dirección de la ocurrencia generadora (`IntersectionEndpointProvenanceResolver2D` 262-286) | lista completa (274) | si `o_1` quedara `NOT_ESTABLISHED`, `w_1`, `d_2` y `e_2` quedarían indefinidos |
| Intersecciones (`LocusIntersectionSolver2D`) | componentes certificadas; `EMPTY` solo con `COMPLETE` (81-102, 146-163) | correcto; tokens por linaje de componente |
| Punto semántico (`A/AlgoSemanticLocusPoint2D` 303-328) | lista certificada, exactamente una coincidencia | sin cruce de huecos; correcto |
| Interacción (`LocusPointInteractionResolver2D`) | creación con la lista completa; arrastre con la certificada (1093-1099, 1253-1258) | una componente solo es periódica si es todo el dominio (1158-1162) |
| Selector de rama principal (`L/LocusPrincipalBranchSelector2D` 54-62) | lista completa | imposible sobre un locus `NOT_ESTABLISHED` |
| Loci anidados con soporte locus (`A/AlgoDependentPointLocusV2` 310-328) | componentes completas del locus superior | hereda la topología, incluida la falsa |

## 3. Reglas de consumo

| Consumidor | Nivel | Regla |
|---|---|---|
| Dibujo | 3 | Cada componente certificada se dibuja hasta sus fronteras certificadas (§5). Ningún segmento cruza una banda `UNRESOLVED`, un intervalo de no existencia certificada ni una discontinuidad estructural. Se cierra solo una componente periódica cerrada. Un punto que no se puede evaluar no se interpola: el trazo se corta allí con una marca. La evidencia exploratoria, si la aprueba la decisión E-1, se dibuja con estilo propio solo en bandas `UNRESOLVED` y nunca se une a una componente |
| Métrica total | 4 | Suma sobre componentes. Una componente periódica cerrada aporta un ciclo; una componente abierta, su longitud. Con `NOT_ESTABLISHED`, `NOT_ESTABLISHED` |
| Ruta entre posiciones | 3 | Las dos direcciones deben estar en la misma componente certificada. Dentro de un arco que cruza la costura, la ruta sigue el arco (no exige una componente completa). `full-cycle` y `wrap-to-start` exigen una componente periódica cerrada |
| Extremos semánticos y ocurrencia generadora | 3 | Dirección contenida en una componente certificada, aunque sea local (decisión E-2). La ocurrencia generadora publica los eventos estructurales ([evento](pre_g9b_cu2_oriented_length_event_design.md) §4) |
| Intersecciones | 3 para raíces locales; 4 para `EMPTY` y para afirmar el conjunto completo | Sin cambios en la regla. El marco de fase es el del arco de la componente; la cuarentena de costura se aplica solo a componentes periódicas cerradas. Tokens y selectores por linaje estructural |
| Punto semántico `Point(L, u)` y pertenencia | 3 | `u` en exactamente una componente certificada (decisión E-2). Creación y arrastre leen la misma estructura. La semántica periódica (elevación, lado de costura) solo vale en componentes periódicas cerradas; en un arco, la dirección se resuelve sobre el arco |
| Selector de rama principal | 3 | Opera sobre componentes certificadas, no sobre la lista completa |
| Composición de loci dependientes | — | La existencia del dependiente compone la del origen: dominio de un soporte locus igual a las componentes del origen (arcos incluidos) y completitud no mayor que la del origen (ADR 0027). Un nodo métrico aporta solo su certificado estructural; un valor de clase 3 nunca es entrada rigurosa |
| Transformaciones por semejanza | — | La imagen hereda explícitamente la estructura de existencia: cobertura, maximalidad, completitud, linaje transportado y autoridad de cierre (`AlgoLocusSimilarityTransform2D` ya la copia, 87-95; se declara en `similarity-transformations`) |
| DXF | 3, con fidelidad incompleta y anexo cuando no hay 4 | Un `LWPOLYLINE` por componente certificada, en el orden del arco aunque cruce la costura. Marca cerrada (código 70) solo para una componente periódica cerrada. Extremos abiertos de tipo estructural, con el vértice del límite lateral (§5.3); nunca se descarta la componente. Un extremo que no se puede evaluar termina en el último vértice evaluado y el anexo declara su estado. Con `NOT_ESTABLISHED`: fidelidad incompleta con anexo obligatorio y estricta en cada componente declarada (`existence-components` §12). Las bandas `UNRESOLVED` no se interpolan |
| LaTeX (PGF, PSTricks, Asymptote) | 3 | Las mismas reglas de topología. `-- cycle`, `\pspolygon` o `--cycle` solo para una componente periódica cerrada. Los extremos no evaluables se declaran en comentarios y hacen la exportación incompleta. Las reglas de la decisión DQ-C5, que hoy viven solo en un registro de validación, pasan a una especificación durable (decisión D-CU2-11) |

### 3.1 Componentes puntuales (C2)

Una componente puntual `{t*}` ([diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md)
§3.5) no es un arco de longitud cero: ningún consumidor la trata como tal. En `CU2` no se
materializa ningún objeto punto (eso es de `CU3`), y la representación se declara sin dibujarla
(decisión D-CU2-19):

| Consumidor | Regla |
|---|---|
| Inspector | la enumera con su envolvente de `t*`, su fuente estructural y el estado de su posición (envolvente certificada o no disponible) |
| Dibujo | no la dibuja; nunca un segmento ni una curva. Si la representación del locus la omite, su fidelidad es parcial con causa «componente puntual declarada, no dibujada» |
| Métrica total | aporta cero; un locus `COMPLETE` formado solo por componentes puntuales mide 0 |
| Ruta entre posiciones | solo existe dentro de la misma componente puntual y mide 0 |
| Punto semántico, interacción, posiciones métricas, selector de rama principal | no la pueden seleccionar por un parámetro binary64, que en general no alcanza `t*`: fallo explícito con causa tipada |
| Intersecciones | no declaran `EMPTY` ni un conjunto completo mientras no excluyan la componente puntual con una distancia certificada positiva; si no, el resultado es incompleto con causa tipada |
| Loci dependientes con soporte locus | declaran la limitación y no son `COMPLETE` mientras el soporte tenga una componente puntual |
| Transformaciones por semejanza | heredan la componente con su parámetro y transforman la envolvente de su posición |
| DXF | ninguna entidad de trazo; el anexo la declara y la exportación es incompleta |
| LaTeX | ningún camino; un comentario la declara y la clasificación es incompleta |

### 3.2 Salidas efectivas en raíces coincidentes (C1)

Donde una segunda salida sin etiqueta queda dentro de la banda `UNRESOLVED` de la supresión
heredada (diseño §4.7), los consumidores ven una banda `UNRESOLVED` más: el dibujo y las
exportaciones terminan cada lado en el borde de la banda, con marca, y nunca la cruzan; el locus es
`NOT_ESTABLISHED`, de modo que los consumidores globales lo rechazan y la exportación es
incompleta. El valor límite certificado de la raíz coincidente es evidencia del inspector, nunca
un vértice dibujado ni exportado. En el modelo del autor no hay tal banda: `{Q, R}` están
etiquetadas y la raíz doble es un parámetro singular (§5.2).

## 4. Vistas heredadas y migración

- `getValidDomainComponents()` deja de usarse para decidir la topología. Se conserva como
  adaptador documentado, que con `COMPLETE` devuelve las componentes. Un arco que cruza la
  costura se devuelve como **una** componente con su forma de arco, nunca como dos intervalos.
- Se corrige la inconsistencia índice/tamaño de `resolveOpenBoundary`, que compara la lista
  certificada con la completa.
- Las tres pruebas de ciclo periódico de las capacidades métricas pasan a la autoridad única de
  cierre.

## 5. Representación y teselación (B06)

### 5.1 Política de dibujo v2

Política versionada, determinista para cada vista:

1. **Partición inicial:** profundidad mínima `d_min` sobre cada componente (candidato 4,
   decisión D-CU2-10).
2. **Criterios de aceptación de un tramo**, todos a la vez:
   - desviación de la cuerda ≤ 0,75 px (se conserva);
   - ángulo de giro entre cuerdas consecutivas ≤ `θ_max` (candidato 10°);
   - la desviación se mide sin acotar la proyección: distancia a la recta de la cuerda más
     distancia a sus extremos, para que un tramo casi degenerado no cuente como plano.
3. **Exención:** el criterio de ángulo se exime solo en dos casos **certificados**: en una banda
   paramétrica mínima alrededor de un pliegue (las cinco pruebas del diseño de certificación
   §5; `e_1` tiene dos, en `t = 0` y `t = ±π`) y en el vértice de un parámetro singular (la
   esquina de una raíz doble, diseño §4.6), que se dibuja como vértice exacto. Un giro cerrado sin
   certificar, como el de un valor binary64 contiguo al crítico, se refina como cualquier otro.
4. **Giro alto no certificado:** se refina hasta la resolución del parámetro y se registra un
   diagnóstico. No se exime ni se inventa continuidad.
5. **Presupuesto:** los vértices por vista tienen un tope versionado. Al agotarlo, el dibujo se
   hace más grueso de forma determinista, con diagnóstico, sin cruzar fronteras y sin pasar
   nunca a muestras uniformes.
6. **Sin retorno a muestras uniformes.** Dentro de una componente certificada la existencia está
   probada. Un fallo de evaluación de un punto es computacional (diseño §3.3 y §3.4): se marca
   como tal, no se convierte en hueco de existencia y no se interpola a través de él.

### 5.2 Aproximación a fronteras de raíz cuadrada

En una transición `D = 0` (recta–circunferencia), `F` tiene una singularidad de raíz cuadrada.
Cerca de la frontera `t*` la teselación usa la reparametrización local `t = t* ± s²`, en la que
`F` es regular. El vértice extremo es `F(t*)`, que en la raíz doble coincide para las dos salidas
(`Q = R`): el dibujo llega exactamente a la frontera y el hueco de B07 en el extremo desaparece.
Esto vale para salidas definidas en la raíz, como `{Q, R}`, etiquetadas; el extremo de una
segunda salida sin etiqueta cae dentro de la banda de la supresión heredada (§3.2) y no se dibuja.
Las fronteras `λ = 0` de un rayo no son singulares. Las fronteras abiertas singulares (divisor
nulo) se acercan por dentro hasta la resolución, con marca tipada, porque el límite puede no
existir.

En un parámetro singular de multiplicidad 2 (el valor crítico de la mordedura), el vértice es
`F(t_c)`, calculado con la raíz doble `λ = −b′/a`, sin raíz cuadrada. Los tramos de los dos lados
llegan a ese vértice con sus tangentes laterales y no se suavizan: la esquina es geométrica
([análisis](pre_g9b_cu2_author_model_certification_analysis.md) §3.8.1).

### 5.3 Extremos de discontinuidad estructural

En una frontera de evento (por ejemplo `e_2` en `t_N`), el límite lateral existe. Se evalúa con
la política de misma posición opuesta: en `e_2`, `w_1(t_N⁻) = L`. El dibujo y la exportación
terminan exactamente en ese límite. Si su cálculo falla, terminan en el último vértice evaluado y
lo declaran: nunca extrapolan (diseño de certificación §3.4).

### 5.4 Constantes (DC-6)

`d_min` y `θ_max` son constantes de política versionadas y se fijan con un banco reproducible.
Se añade un escenario `tessellation` al arnés de `CU0`. Referencia de B06, del modelo del autor
con la política actual:

| Locus | Vértices | Giro máximo | Defecto del punto medio (h = 10⁻³) |
|---|---:|---:|---:|
| `o` | 17 | 49,38° | 9,88·10⁻⁷ |
| `v` | 17 | 54,05° | 1,002·10⁻⁶ |
| `w` | 12 | 156,81° | 1,010·10⁻⁶ |
| `a_1` | 19 | 38,40° | 1,104·10⁻⁶ |
| `e_1` | 9 | 180° (pliegue real) | 1,961·10⁻⁶ |
| `o_1` | 27 | 18,65° | 1,867·10⁻⁶ |

Aceptación (filas V-B06-1 a V-B06-3 de la
[matriz de validación](../validation/pre_g9b_cu2_validation_matrix.md)):

- todo giro fuera de un pliegue certificado es ≤ `θ_max`;
- los pliegues de `e_1` están certificados;
- el número de vértices y el coste quedan dentro del presupuesto;
- la semántica es idéntica con cualquier zoom o DPI.

### 5.5 Determinismo

La teselación puede depender de la vista (tolerancia en píxeles), pero nunca cambia el estado de
definición, los dominios, las claves, la revisión ni las evaluaciones semánticas
(`locus-v2-semantics.md`). Con las mismas entradas y la misma vista, los vértices son idénticos.

### 5.6 Disponibilidad numérica y fidelidad declarada (P5)

Cada aproximación de dibujo o de exportación lleva su fidelidad efectiva, por componente:

| Fidelidad | Cuándo |
|---|---|
| completa | todos los tramos cumplen la política y los extremos se evaluaron |
| parcial | hay cortes por puntos no evaluables, extremos no evaluados o engrosamiento por presupuesto; se enumeran con su causa |
| fallida | ni siquiera la aproximación más gruesa admisible cabe en el presupuesto, o no se pudo evaluar ningún tramo; la componente no se representa y se informa |

La fidelidad del dibujo se expone a la instrumentación y al inspector; la de las exportaciones,
en el anexo DXF y en los comentarios y la clasificación LaTeX. Las situaciones y sus reglas están
en el [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §3.4.

## 6. Aproximaciones de exportación

- Mismas reglas de componentes, fronteras y cierre que el dibujo (§3).
- El constructor adaptativo acepta extremos abiertos con límite (§5.3) en vez de descartar la
  componente. Un extremo abierto o no evaluable se exporta hasta el último vértice evaluado; su
  estado se declara en el anexo DXF o en los comentarios LaTeX, y la exportación es incompleta
  (`INCOMPLETE_WITH_CERTIFIED_COMPONENTS` en LaTeX, fidelidad incompleta en DXF).
- Los parámetros singulares certificados se exportan como vértices exactos (§5.2).
- Las componentes puntuales no se exportan como trazo: se declaran y la exportación es
  incompleta (§3.1).
- La profundidad mínima vale también para componentes abiertas.
- El residuo de cierre se mide contra el certificado de cierre, no contra una identidad trivial.
- La tolerancia de exportación sigue en unidades del modelo (0,001 por defecto). Si el criterio
  de giro de §5.1 se aplica también a la exportación lo decide D-CU2-10.

## 7. Identidad en los consumidores

Los tokens de intersección, los selectores de puntos, las posiciones métricas y el estado de
interacción usan el linaje estructural de la componente (diseño §7), no los bits de sus extremos
ni su índice en una lista. Los selectores persistidos se resuelven solo por una correspondencia
estructural demostrada, que la contención única valida pero no crea; no se reescriben, y ante
ambigüedad fallan cerrados (diseño §7).
