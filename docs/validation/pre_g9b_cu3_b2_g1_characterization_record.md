# PRE-G9B-CU3 — Expediente G-B2-1 de `CU3-B2`: caracterización

| Campo | Valor |
|---|---|
| Carácter | Expediente de la puerta G-B2-1, autorizada por la decisión autoral de 2026-10-28 («Decisión autoral sobre G-B2-0 y autorización de G-B2-1»). Solo caracterización: ningún cambio de código productivo. Para revisión autoral; no solicita ni presupone G-B2-2 |
| Decisión de la que parte | G-B2-0 aprobado con precisiones: D-B2-1(a) a D-B2-7(a); precisiones P1 (regularidad de la tangencia) y P2 (persistencia) |
| Diseño | [diseño de `CU3-B2`](../architecture/pre_g9b_cu3_b2_regular_tangent_design.md) (§12, resultados de G-B2-1); [enmiendas candidatas](../architecture/pre_g9b_cu3_b2_normative_proposal.md) (no promovidas); [matriz](pre_g9b_cu3_b2_validation_matrix.md) |
| Base | `HEAD` de `phase/pre-g9b-cu3-implementation` sobre `fbd19387`; el producto es el de `CU3-B1` (`dd3a2676`) |
| Estado | G-B2-1 entregado; G-B2-2 no autorizado; `CU3` global no aprobado; `DOC-CU3-CU9-STRUCTURAL-POINTS` sigue bloqueante para ese cierre; nada publicado; `selfApproved=false` |

## 0. Herramienta y reproducibilidad

- **Sonda:** [`PreG9BCu3B2CharacterizationProbe`](../../tools/benchmark/pre-g9b-cu/cu3-b2-g1/probe-java/org/geocedg/common/kernel/locus/constructive/PreG9BCu3B2CharacterizationProbe.java),
  prueba no productiva fuera de todo inventario, sobre los modelos de prueba de `ConstructiveModelReplicaTest`
  (incluida la réplica del modelo del autor).
- **Lanzador:** [`run-cu3-b2-g1-characterization.ps1`](../../tools/benchmark/pre-g9b-cu/run-cu3-b2-g1-characterization.ps1).
  Genera fuera del árbol una copia instrumentada de `LocusIntersectionTokenLedger2D` (dos puntos de
  impresión tras un interruptor apagado por defecto), ejecuta la sonda dos veces y registra las
  líneas, su SHA-256 y la identidad de la ejecución. Al terminar recompila las clases de prueba sin la
  sonda.
- **Ejecución registrada:** §8.

## 1. Alcance real de la certificación exacta (sonda 1)

| Caso | Política | Clase de evidencia | Orden | Admisible |
|---|---|---|---|---|
| `o_1·h_2` de la réplica del autor (`u_1`) | heredada | 3 (`CONTACT_NUMERICALLY_COMPATIBLE`) | — | no |
| `o_1·h_2` de la réplica del autor (`u_1`) | constructiva | **2** (`CONTACT_EXISTENCE_EXACT`, raíz doble exacta en el parámetro exacto 0, por la regla de conflicto de T7-a) | `TWO` | no (`CU3-B2` no implementado) |
| fuente regular (circunferencia por `Midpoint`) y recta libre tangente | constructiva | 3 o `CONTACT_NOT_ESTABLISHED` con `CONTINUATION_AMBIGUOUS` | — | no |
| fuente regular por `Rotate`, tangencia en el parámetro exacto 0 del conductor, con recta, circunferencia exterior o interior | constructiva | 3 (envolvente del punto crítico `[0, 7·10⁻¹⁶]`; el residuo contiene el cero) | — | no |
| recta construida tangente (`Tangent`) o que pasa por el punto exacto de contacto | constructiva | 3 o 5 | — | no |

**Conclusión K1.** La decisión exacta de una tangencia regular solo se obtiene hoy en `u_1`, por la
vía estructural de T7-a. Ningún objetivo libre, ninguna recta construida tangente y ningún contacto en
un parámetro exacto de una fuente regular llega a la clase 2. `CU3-B2` cubriría, con el producto
actual, el caso obligatorio y los contactos con el mismo mecanismo estructural, no las tangencias
regulares en general.

**Conclusión K2 (ausencia).** Bajo la autoridad constructiva, cuando el objetivo se separa de la
fuente sin cortarla, el resultado es `UNRESOLVED` con `CONSTRUCTIVE_ADMISSION_NOT_ESTABLISHED` («la
fuente o el objetivo no tiene envolvente en la configuración constructiva»), no un `NONE`
certificado. `CONTACT_LOST` no es alcanzable en esos casos: el estado correcto es
`CONTACT_NOT_CERTIFIED` (diseño §12.2).

## 2. Fuentes singulares (sonda 5, precisión P1)

| Caso | Resultado | ¿Tangencia regular? |
|---|---|---|
| lugares de puntos definidos por expresión (`(u², u³)`, `(u², u²)`, con cúspide o parada) | sin componente certificada (`UNRESOLVED`): no llegan a la clasificación de contactos | no |
| punto de retorno sin simetría (`V = (0,001; 1)`, `F'(t₀) = 0`), objetivo justo dentro | dos raíces simples certificadas (`TWO_SIMPLE`), orden `ONE` cada una | no |
| el mismo punto de retorno, objetivo justo fuera | `UNRESOLVED` | no |
| pliegue simétrico con contacto exacto (`x = 2`) | clase 3 y `IDENTITY_DISCONTINUITY` del ancla de pliegue (vía `CU3-B1`) | no |

**Conclusión K7.** En ningún caso una fuente singular se clasifica con orden `TWO`. La regla vigente
de CU3.2 (orden dos solo si el encierro de la derivada de la fuente excluye el vector cero) se
respeta. La raíz doble exacta del residuo en una fuente singular no se ha podido producir con
objetivos libres, por K1; la fila adversaria B2-21 la cubre con una contribución analítica declarada,
como la prueba de transición de `CU3-B1`.

## 3. Tokens transversales alrededor de una tangencia (sonda 2)

Fuente regular, objetivo `y = b`, dos puntos materializados `X` e `Y` sobre las raíces transversales
(política heredada; bajo la autoridad constructiva, con la fuente por `Midpoint`, las raíces no son
admisibles porque la fuente no tiene envolvente constructiva):

| `b` | Tokens | Puntos |
|---|---|---|
| 0,9 → 0,99 | los mismos dos | definidos, siguen a sus raíces |
| 1 (tangencia) | una raíz con token nuevo, no reclamado; `CONTINUATION_AMBIGUOUS` | indefinidos |
| 0,99 (primera revisión tras la división) | dos tokens nuevos, no admisibles; `CONTINUATION_AMBIGUOUS` | indefinidos |
| 0,9 (siguiente revisión) | **los dos tokens originales**, admisibles | definidos en sus raíces |
| 1 → 0,9 → 0,8 | el mismo patrón: una revisión ambigua y después los tokens originales | indefinidos y después definidos |

**Conclusión K4.**

- Las raíces transversales **conservan** sus tokens reclamados a través de la tangencia, por ADR 0017
  decisión 13: el mismo selector completo vuelve a resolverse de forma única.
- La fusión y la división son barreras: durante la tangencia y en la primera revisión posterior
  (comparación con la revisión de la tangencia) la continuación es ambigua y falla cerrada, con tokens
  frescos no admisibles.
- Ningún token pasa de una raíz a otra ni de la tangencia a una raíz.
- Para `CU3-B2`: la revisión de `CONTACT_SPLIT` coincide con la revisión ambigua; el token regular
  queda latente y las transversales reaparecen con sus propios tokens en la siguiente revisión no
  ambigua. Es coherente con D-B2-1(a) sin cambiar ninguna regla transversal.

## 4. Redefinición del soporte del conductor (sonda 3, D-B2-5)

| Redefinición | Aplicada | Efecto en los tokens (`F` y transversales) |
|---|---|---|
| `N = Point(c3)`, `N = PointIn(c3)` (otro soporte del conductor) | **no**: la definición queda `Point(c)`, sin error | sin cambios |
| `lx = LocusV2(X2, N2)` (otro conductor) | **no**: la definición queda `LocusV2(X0, N)`, sin error | sin cambios |
| `c = Circle(O, 2)`, `c = Circle((0,0), 2)` (el mismo soporte, otra definición) | sí | sin cambios: mismo objeto, mismo linaje |
| `c = Circle((0,0), 2.5)` y vuelta a `Circle((0,0), 2)` | sí | el ancla deja de ser actual y vuelve con **el mismo token** |
| `c = Dilate(c3, 1, O)` | sí | heredada: sin cambios; constructiva: `CONSTRUCTIVE_ADMISSION_NOT_ESTABLISHED`, ancla latente, punto indefinido |

**Conclusión K5.**

- El núcleo no admite ninguna redefinición que cambie el soporte o el conductor de un Locus V2
  existente: la rechaza en silencio. Por eso la barrera de linaje por redefinición no es alcanzable.
- Las redefiniciones admitidas actúan sobre el mismo objeto soporte y conservan su identidad
  persistente: son cambios de geometría o de definición, no de linaje, y siguen las reglas ordinarias
  (latencia y reactivación).
- `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` pasa de «no demostrada» a **«camino inexistente»**:
  se propone cubrirla con pruebas de que los dos rechazos dejan intactos los tokens `F` y `T`, y de
  que las redefiniciones en sitio conservan el linaje (filas B2-14 a B2-16 revisadas).
- El rechazo silencioso, sin mensaje al usuario, es un comportamiento previo de usabilidad, ajeno a
  `CU3-B2`; se registra como `OBS-CU3B2-SILENT-DRIVER-REDEFINITION-REFUSAL` para `CU7`
  (redefinición de Locus V2).

## 5. Reservas transitorias del libro de tokens (sonda 4, D-B2-4, P2)

Subida de `nextIncarnation` por reapertura (`setXML` del propio documento), con la copia instrumentada:

| Variante | Por reapertura | Asignaciones observadas en la carga |
|---|---|---|
| un ancla de pliegue reclamada | +2 | dos selectores **transversales** (`current-transverse-root-germ`, signos positivo y negativo) |
| un ancla de pliegue sin reclamar | +3 | los dos transversales y, después, el ancla `F` otra vez |
| réplica: dos anclas reclamadas | +1 | un selector transversal (`radial-distance`) |
| réplica: dos anclas sin reclamar | +3 | un transversal y las dos anclas `F` otra vez |

**Causa (K6).**

- Durante la carga, el resultado se evalúa con la política heredada antes de que se restaure el
  registro de autoridad del documento. Esa evaluación produce selectores transversales, que no están
  en el estado v6 restaurado (sus tokens se podaron al quedar superados por la pareja certificada), y
  reserva encarnaciones nuevas. La evaluación final, ya con la autoridad constructiva, reutiliza las
  entradas `F` reclamadas.
- Si el ancla no está reclamada, la evaluación transitoria publica un estado sin ella y la evaluación
  final la vuelve a asignar: **el token de un ancla no reclamada cambia en cada reapertura**. Es
  admisible por ADR 0017 (solo se conservan los tokens reclamados), pero contradice la idempotencia.
- Ninguna de esas asignaciones transitorias llega a un punto: los puntos se restauran con tokens
  persistidos. La identidad durable no se ve afectada.

**Viabilidad de la corrección.** Corregir la causa, no el contador: que ninguna evaluación de un
documento en carga use una política distinta de la persistida. Dos vías, por orden de preferencia:

1. restaurar el registro de autoridad del documento antes de evaluar los elementos (orden de lectura
   de la sección del registro espacial o lectura anticipada del registro de autoridad);
2. si (1) no es posible sin tocar el formato, no confirmar en el libro las evaluaciones hechas
   durante la carga hasta que esta termine; la actualización posterior confirma.

Cualquiera de las dos debe preservar, con pruebas: unicidad (ninguna encarnación reutilizada),
determinismo (mismo estado tras abrir y guardar sin editar), reclamaciones, latencia, duplicación,
Deshacer/Rehacer y reapertura. No se redefine el contrato de identidad durable (P2). Si en G-B2-2 la
vía (1) cambiara el formato del documento o la vía (2) afectara a algún contrato, se prepararía una
decisión autoral antes de seguir.

## 6. Coste (sonda 6)

No se ha medido. Por K1, `CU3-B2` solo añadiría un ancla en los contactos de clase 2 (hoy, `u_1`),
cuyo certificado ya calcula T7-a: no hay cajas nuevas en los demás contactos. La medición queda para
G-B2-3 (fila B2-20).

## 7. Reglas definitivas propuestas

**Identidad.**

- Germen `pre-g9b-cu3/regular-tangent-germ/v1` (linaje, familia y orientación canónica del objetivo,
  `σ`), con fase y rango de ADR 0017 si el germen se repite (D-B2-6).
- Materialización solo con las precondiciones del diseño §2, ampliadas por P1 (§12.1 del diseño).
- Estados: `CURRENT`; `CONTACT_SPLIT` con dos raíces certificadas; `CONTACT_LOST` **solo** con `NONE`
  certificado; `CONTACT_NOT_CERTIFIED` en cualquier otra ausencia o indecisión (K2);
  `IDENTITY_DISCONTINUITY` en las barreras.
- Ningún token regular pasa a una raíz transversal; las transversales siguen ADR 0017 tal como lo
  hace hoy el motor (K4).

**Persistencia.**

- Libro v6 con la entrada `T` (D-B2-3), mientras ningún lector v6 se haya distribuido. Sigue siendo
  cierto: `CU3-B1` no está en `origin` ni en `main`.
- Sin reservas transitorias en la carga (K6, vía 1 o 2), con las garantías de P2.

## 8. Ejecución registrada

| Campo | Valor |
|---|---|
| Comando | `pwsh -NoProfile -File tools\benchmark\pre-g9b-cu\run-cu3-b2-g1-characterization.ps1 -OutputDirectory artifacts\benchmarks\pre-g9b-cu\cu3-b2-g1-run2` (salida ignorada por git) |
| Base de la ejecución | commit `fbd19387` (árbol `8190e4d7`), blob del libro `86fe1653`; sonda SHA-256 `2677f1fd4a63e9276e8b851c8fad3457e0c03013da48aa79569b0ece87179e7c`, igual a la versionada en este commit |
| Ejecuciones | A y B: código 0; JUnit 10 pruebas, 0 fallos; 193 líneas cada una |
| Reproducibilidad | las líneas brutas difieren solo en los identificadores aleatorios de cada ejecución (material de los tokens y objetos de linaje); normalizadas, A y B son idénticas: SHA-256 `9a0b582d03e7cb8209f01e8e7604c52b18162490839cbce854672c7f7347de43` |

## 9. Propuesta de cambios mínimos de código (para G-B2-2, no autorizada)

| Cambio | Ámbito |
|---|---|
| ancla de tangencia regular: germen, `σ` con el signo certificado de `h''` y la orientación canónica, precondiciones P1 | `CertifiedContactAnalysis2D` (junto al ancla de pliegue), `PublicTargetIntersectionCapability2D` |
| selector con rango para gérmenes repetidos | `IntersectionRootDeterministicSelector2D`, resolvedor |
| estados y diagnósticos `REGULAR_CONTACT_*` con etiquetas | `IntersectionSemanticMetadata2D`, `menu*.properties` |
| entrada `T` del libro v6 y regla «v6 exige una entrada de contacto» | `LocusIntersectionTokenLedger2D` |
| sin reservas transitorias en la carga (K6) | restauración del registro de autoridad o confirmación diferida del libro |
| pruebas B2-1 a B2-23 | `common-jre` (análisis y réplica) y escritorio (persistencia) |

## 10. Verificaciones necesarias en G-B2-2 y G-B2-3

- Pruebas focales de la matriz, incluidas la prueba de transición con contacto exacto declarado
  (como `PreG9BCu3FoldAnchorTransitionTest`) y la réplica del autor.
- Esta misma sonda sobre el candidato: ninguna asignación transitoria en la carga.
- Campaña `INTEGRATED_PHASE` completa del candidato (`STATIC`, `INFRA_UNIT`, `PHASE` de `CU3` a
  `CU0`, `INTEGRATION`) con el inventario renovado; rendimiento y corpus; smoke autoral.

## 11. Limitaciones y riesgos

- **Alcance (K1):** con el producto actual, `CU3-B2` solo da token admisible a `u_1` y a contactos con
  el mismo mecanismo estructural. Ampliarlo exigiría decisiones exactas nuevas, fuera de este
  incremento.
- **Transiciones dinámicas:** como en `CU3-B1`, la transición tangencia ↔ pareja con existencia exacta
  no se puede producir moviendo un objetivo libre; se prueba con contribuciones analíticas declaradas.
- **Ausencia no certificada (K2):** en la práctica, la desaparición de una tangencia anclada dará
  `CONTACT_NOT_CERTIFIED`, no `CONTACT_LOST`.
- **Corrección K6:** toca la carga de documentos; su riesgo es de regresión en la persistencia, no de
  identidad, y lo cubren las filas B2-13 y B2-22.
- **Redefinición (K5):** la barrera de linaje no se puede demostrar porque no hay camino; si `CU7`
  admitiera la redefinición del conductor, la barrera tendría que probarse entonces.

## 12. Recomendación

Autorizar G-B2-2 con este alcance:

- tangencias regulares de clase 1 o 2 con las precondiciones P1 (hoy, `u_1`);
- estados y reglas del §7;
- corrección K6 por la vía 1, o la vía 2 si la 1 cambia el formato;
- pruebas B2-1 a B2-23;
- verificación `INTEGRATED_PHASE` en G-B2-3 y smoke en G-B2-4.

Alternativa: autorizar solo la corrección K6 como incremento propio y dejar la materialización para
cuando haya decisiones exactas más generales. No se recomienda, porque deja `u_1` sin token admisible
y la corrección K6 es independiente del germen.
