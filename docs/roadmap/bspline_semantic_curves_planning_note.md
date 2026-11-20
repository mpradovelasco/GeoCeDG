# B-splines semánticas — nota de planificación

| Campo | Valor |
|---|---|
| Estado | `NEAR-TERM PLANNING CANDIDATE — NOT AUTHORIZED FOR IMPLEMENTATION` |
| Clasificación | `B-SPLINES — PLANNING ONLY — IMPLEMENTATION NOT AUTHORIZED` |
| Fecha | 2026-11-20 |
| Fase | Ninguna asignada. No crea una puerta G, un ADR, una especificación normativa ni un prompt de implementación |
| Aprobación | `selfApproved=false`; pendiente de revisión autoral |

Esta nota registra una línea de trabajo posible. No describe una capacidad
disponible y no modifica `SplineV2`, Locus V2, intersecciones, métricas ni
serialización.

## 1. Punto de partida

`SplineV2` (G9S1 y refinamientos G9S1-R1, `PASS — AUTHOR APPROVED`) crea un
`GeoLocusV2` semántico con dominio, tramos y nudos explícitos. Su familia es
una **interpolación polinómica acotada** que pasa por los puntos dados, con
grado limitado, variantes abierta y cerrada/periódica, continuidad estructural
exacta en los nudos y admisión numérica explícita. No es una familia general de
B-splines definida por puntos de control y vector de nudos, ni NURBS. Classic
`Spline` permanece intacto y debe seguir así.

## 2. Objeto matemático a estudiar

Una curva B-spline de grado \(p\) con puntos de control \(P_0,\dots,P_n\) y
vector de nudos no decreciente \(U=(u_0,\dots,u_{n+p+1})\):

\[
C(u)=\sum_{i=0}^{n} N_{i,p}(u)\,P_i,\qquad u\in[u_p,u_{n+1}],
\]

con las funciones base de Cox–de Boor. Aspectos que el estudio debe fijar:

- **Dominio orientado** \([u_p,u_{n+1}]\) como parámetro canónico de la curva,
  ligado a \(U\) y nunca al índice de una muestra (AGENTS §11.3).
- **Soporte local**: \(N_{i,p}\) es no nula solo en \([u_i,u_{i+p+1})\); mover
  \(P_i\) afecta a lo sumo a \(p+1\) tramos.
- **Multiplicidad y continuidad**: en un nudo interior de multiplicidad \(m\le p\)
  la curva es, en condiciones ordinarias, \(C^{p-m}\); \(m=p\) interpola el punto
  de control y \(m=p+1\) separa componentes.
- **Abierta (clamped), cerrada y periódica**: nudos extremos de multiplicidad
  \(p+1\); cierre geométrico; periodicidad con puntos de control y nudos
  envueltos y costura tratada como en el contrato periódico existente.
- **Degeneraciones y configuraciones inválidas**: \(n<p\), \(U\) decreciente o de
  longitud incorrecta, multiplicidad \(>p+1\), puntos de control coincidentes o
  colineales, tramos de longitud nula, imágenes colapsadas; todas con estado
  tipado, sin geometría obsoleta.
- **Dependencias dinámicas**: los \(P_i\) (y, si se decide, los nudos) son
  entradas del DAG; la recomputación es determinista para una revisión dada.
- **Evaluación semántica independiente de la teselación**: de Boor o inserción
  de nudos como evaluador; la caché de render sigue siendo derivada.
- **Identidad y persistencia**: ID estable, versión semántica, \(p\), \(U\),
  roles de los puntos de control y contrato de parametrización serializados;
  Undo/Redo y reapertura con identidad exacta.
- **Reutilización de Locus V2**: métricas (longitud, posición), intersecciones,
  puntos semánticos, transformaciones de semejanza y resultados ricos se
  consumen desde las autoridades existentes; la representación polinómica a
  trozos por tramo permite reutilizar la ruta exacta ya usada por `SplineV2`.
- **Extensión racional/NURBS**: pesos \(w_i>0\), cónicas exactas, invariancia
  proyectiva. Queda como decisión separada, posterior a la B-spline no racional.

## 3. Alternativas arquitectónicas

| | A. Extensión compatible de `SplineV2` | B. Nueva familia semántica B-spline sobre Locus V2 |
|---|---|---|
| Idea | Añadir a `SplineV2` un modo "control points + knots" | Nuevo comando/familia (`BSplineV2` o similar) que produce un `GeoLocusV2` semántico propio |
| Ventajas | Reutiliza comando, herramienta y persistencia | Separa dos significados distintos: interpolar puntos frente a aproximar con un polígono de control; no altera documentos `SplineV2` existentes |
| Riesgos | Mezcla semánticas (los puntos dejan de estar sobre la curva); amplía un contrato aprobado y su serialización; ambigüedad en documentos antiguos | Una familia más en comandos, guía y exportación |
| Reutilización | Total, pero acoplada | Las mismas autoridades Locus V2 (evaluador, métricas, intersecciones, transformaciones, persistencia) a través de un evaluador nuevo |

**Dirección recomendada para el estudio (no decisión): B.** Es la menos
invasiva respecto a contratos ya aprobados: deja `SplineV2` y Classic `Spline`
intactos, mantiene el significado de cada objeto explícito y concentra lo nuevo
en un evaluador y un descriptor semántico. La decisión definitiva exige un
diseño propio y, en su caso, un ADR.

## 4. Dependencias

- **Científicas**: estabilidad de de Boor y de la conversión a forma polinómica
  por tramos; certificación de raíces en nudos de multiplicidad alta; tangencias
  en nudos \(C^0\); longitud con error controlado.
- **Operativas**: selección JUnit propia y, si procede, filas en la matriz CORE
  de `verification-levels.md`; verificación ordinaria `CORE + FOCUSED`; ninguna
  campaña `FINAL` sin autorización expresa.
- **De producto**: guía de usuario, herramienta y ayuda de comando bilingües;
  compatibilidad de lectura de documentos.

## 5. Interoperabilidad relacionada (trabajo independiente)

- **Importación DXF 2D** de entidades primitivas (`POINT`, `LINE`, `CIRCLE`,
  `ARC`, `LWPOLYLINE`, y `SPLINE` solo si hay familia equivalente).
- **Importación SVG** de la geometría vectorial admitida.
- **Servicio externo común de importación** con inserción transaccional: una
  operación atómica, un punto de Undo/Redo, procedencia y fidelidad declaradas,
  escala, unidades, capa y posición en el protocolo explícitas.
- **Exportación DXF `SPLINE`** cuando exista una representación matemáticamente
  equivalente (grado, nudos, puntos de control y pesos) y evidencia de
  compatibilidad con lectores CAD; hasta entonces sigue la polilínea adaptativa
  con tolerancia declarada de G9X1.

La importación construye objetos nativos mediante las autoridades existentes.
No recupera dependencias CAD ni asigna identidad CeDG por proximidad, etiquetas
u orden. La inserción de imágenes del portapapeles ya forma parte del producto
(1.2.x) y no es una funcionalidad nueva de esta línea.

## 6. Posible impacto editorial

- `CH30` (Spline V2: fundamentos, algoritmos e implementación): las B-splines
  serían una sección o capítulo hermano, nunca una reescritura de `SplineV2`.
- `CH07` (metodología e interoperabilidad) y `CH32` (perspectivas): importación
  DXF/SVG y exportación DXF `SPLINE`.

Si las B-splines se implementan durante la escritura del libro, su
incorporación requiere una revisión editorial explícita en `geocedg_book`; este
repositorio no modifica el manuscrito ni la baseline técnica editorial.
