# Uso condicionado de Locus V2 — registro de decisiones autorales y puntos abiertos

**REVISIÓN 2 — TEXTO NORMATIVO PENDIENTE DE APROBACIÓN AUTORAL.** `selfApproved=false`.
Este documento no aprueba nada. Registra las decisiones de producto que el autor ya tomó
el 2026-11-21, dice dónde quedan recogidas y enumera lo único que sigue necesitando su
decisión. La revisión 1 de esta hoja es la del commit `ab37fb5ff`.

## 1. Decisiones del autor incorporadas

Fuente: instrucción del autor de 2026-11-21, «Revisión normativa post-R1, continuación de
implementación y objetivo 1.2.2». Son decisiones de producto. No convierten el ADR 0042 en
norma vigente ni autorizan publicar.

| Id | Decisión | Dónde queda recogida | Qué sustituye de la revisión 1 |
|---|---|---|---|
| D-V1 | Uso condicionado activo por defecto para todo Locus V2 elegible en 1.2.2; sin activación manual, modo experimental, política por documento ni opción de desactivarlo | contrato §1; ADR, decisión 1 | la pregunta «adoptar o no»; no había regla de activación |
| D-V2 | Códec de dirección por parámetro aceptado; sin requisito de lectura por 1.2.1; persistencia completa y reproducible dentro de 1.2.2 | contrato §6, obligaciones en §6.4 | el límite declarado con lectores anteriores y su prueba, que se retira |
| D-V3 | No se impone estilo de trazo; marca discreta del sistema si es viable; estado y garantías consultables en las propiedades | contrato §9; diseño §3.7 | la raya larga obligatoria |
| D-V4 | Exportación geométrica semántica de regiones condicionadas excluida inicialmente, con diagnóstico; exportación de imagen de lo mostrado; vinculación a la línea futura de B-splines, representaciones derivadas y DXF | contrato §10; diseño §3.8 | el incremento I5 |
| D-V5 | Uso funcional completo: puntos, seguimiento, intersecciones, transformaciones, construcciones dependientes, loci anidados, métricas, propagación, presentación y manejo | contrato §8, un contrato por operación; diseño §3.6 | intersecciones y locus dependiente como incrementos posteriores opcionales; las transformaciones no figuraban |
| D-V6 | Reducir el coste de un cambio geométrico real sin caché compartida ni copias mutables compartidas | [documento de rendimiento](../architecture/post_r1_locus_real_change_performance.md) | — |
| D-V7 | GeoCeDG 1.2.2 es la versión objetivo; el número no se cambia hasta implementar y validar | registro de progreso; ninguna fuente de versión se ha tocado | — |

Corrección normativa pedida por el autor, además de las decisiones:

| Punto | Dónde queda |
|---|---|
| `MARGIN_BOUNDED`: una cota rigurosa de separación de raíces no prueba la validez de las demás operaciones ni la existencia o continuidad | contrato §3.3: por clase, lo demostrado, lo comprobado solo en nodos, lo que sigue siendo hipótesis y los consumidores; las tres clases dan los mismos permisos; H1 y H2 no se retiran sin prueba |
| Ciclo de vida determinista por revisión y generación, independiente del orden de consulta; el renderer no decide verdad | contrato §7 |
| Matriz de consumidores con intersecciones y transformaciones | contrato §8.1 a §8.10 |
| Las transformaciones conservan función semántica, parametrización y grado de evidencia | contrato §8.7 |
| Casos adversos conservados | diseño §7, con dos casos nuevos (C12, C13) |

## 2. Qué se somete a aprobación

El **texto** del paquete revisado, en la rama `maintenance/geocedg-1.2.2-post-r1`:

| Documento | Papel |
|---|---|
| [ADR 0042, revisión 2](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md) | decisión de arquitectura propuesta, con las diferencias respecto a `ab37fb5ff` |
| [Contrato candidato, `conditioned-use/v2-candidate`](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md) | texto normativo candidato, matriz de operaciones y enmiendas que su adopción requeriría |
| [Diseño, revisión 2](../architecture/post_r1_locus_conditioned_use_design.md) | inspección de capacidades, extensión mínima por operación, viabilidad de la marca, plan 3B, banco adversarial |

La identidad exacta del paquete (commit y árbol) consta en el
[registro de progreso](post_r1_locus_repairs_progress.md), checkpoint 4. Una aprobación
debe citar ese commit.

Mientras el texto no esté aprobado no se inicia la semántica V (bloque 3B). El motivo es
AGENTS §3.1 y §9: V cambia contratos de admisibilidad, identidad y evidencia de
`locus-v2-existence-components.md` §15.1, §15.6 y §15.8.

## 3. Puntos que requieren decisión del autor

Las decisiones D-V1 a D-V7 no se vuelven a preguntar. Quedan cuatro puntos.

### P1 — Aprobación del texto normativo revisado

Aprobar el ADR 0042 revisión 2 y el contrato candidato como base normativa para iniciar 3B
por I0, con las enmiendas del contrato §12 a redactar sobre las especificaciones al
implementar cada incremento. Si el autor corrige algún punto, se entrega primero el texto
corregido.

### P2 — La marca del sistema (concreción de D-V3)

La inspección concluye que la marca es viable con intervención acotada, reutilizando el
patrón de la pista de orientación (diseño §3.7). Tres elecciones de producto que el
contrato §9.4 deja propuestas:

| Elección | Propuesta | Alternativa |
|---|---|---|
| Glifo | rombo hueco de 7 px con el color del objeto, uno por región visible | otro glifo no direccional que el autor prefiera |
| Visibilidad | siempre visible mientras el locus lo sea; no depende de la preferencia de la pista de orientación | gobernada por una preferencia de vista |
| Exportación de imagen | la marca se exporta con lo mostrado allí donde el exportador de imagen puede dibujarla (contrato §10.1), para que una imagen no presente una región condicionada como indistinguible de una certificada | no exportarla nunca, como la pista de orientación, que solo existe en la pintura viva |

La tercera es la única que va más allá de lo que hoy hace la pista de orientación: exige
pintar la marca también en la vía de exportación de imagen, no solo en la pintura viva.

### P3 — Ubicación en el roadmap de la exportación semántica (D-V4)

No existe hoy una fase que cubra B-splines y representaciones derivadas, importación DXF y
exportación DXF extendida con calidad, procedencia y tolerancias. `G9X1` está cerrada y
difiere la entidad `SPLINE`; la nota de `PRE-G9B-S1` indica que un contrato nuevo de
fidelidad o una entidad DXF `SPLINE` «requeriría una subfase separada»; `G15` deja DXF en
`G5`; no hay fase de importación DXF.

Propuesta: registrar la exportación semántica de regiones condicionadas como requisito de
entrada de esa subfase separada de la línea `G9X`, cuando el autor la abra, con estas
condiciones ya fijadas por D-V4: la importación DXF es de solo lectura, y una spline o
polilínea de intercambio nunca sustituye en silencio al objeto CeDG autoritativo. Este
trabajo no ha modificado el roadmap. Decide el autor si la anotación se incorpora al
roadmap ahora, y con qué identificador, o al abrir la subfase.

### P4 — Defecto previo F8 dentro de I4

Al optimizar D-V6 apareció un defecto previo, reproducible en la base `9ccefdcfe`: un locus
anidado vivo pierde su definición cuando el conductor de un locus consumidor es entrada no
conductora suya ([reproductor](../architecture/post_r1_locus_real_change_performance.md#4-hallazgo-f8)).
El modelo del autor no contiene esa composición. Afecta a las composiciones anidadas que
D-V5 incluye en el objetivo.

Propuesta: triarlo y corregirlo dentro de I4, con su prueba, como parte del objetivo 1.2.2.
Alternativa: tratarlo como reparación separada antes de 3B.

## 4. Punto de reanudación

Con la aprobación de P1 se reanuda en 3B por I0 sin repetir lo cerrado: verificar la
aprobación y el commit que cubre, comprobar si la base ha cambiado, y seguir el
[plan](../architecture/post_r1_locus_conditioned_use_design.md#5-plan-de-incrementos-3b):
I0, I1, I2, I3, I4, I4T. P2, P3 y P4 no bloquean I0 ni I1: P2 se necesita al empezar I2, P4
al empezar I4, y P3 solo afecta al roadmap.
