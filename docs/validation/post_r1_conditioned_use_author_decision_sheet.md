# Uso condicionado de Locus V2 — registro de decisiones autorales y puntos abiertos

**REVISIÓN 4 — TEXTO NORMATIVO APROBADO POR EL AUTOR EL 2026-11-22.** `selfApproved=false`.
La aprobación consta en el
[registro de aprobación](post_r1_conditioned_use_author_approval_record.md), que es su única
autoridad; los §4 a §6 de esta hoja describen la etapa anterior y quedan sustituidos por el
§7. Este documento no aprueba nada. Registra las decisiones que el autor ya tomó el
2026-11-21, dice dónde quedan recogidas y enumera lo único que sigue necesitando su
decisión. La revisión 1 de esta hoja es la del commit `ab37fb5ff`, la revisión 2 la de
`83cd56c4c` y la revisión 3 la de `57c495cce`.

## 1. Decisiones de producto del autor

Fuente: instrucción del autor de 2026-11-21, «Revisión normativa post-R1, continuación de
implementación y objetivo 1.2.2». Son decisiones de producto. No convierten el ADR 0042 en
norma vigente ni autorizan publicar. El autor las mantuvo al revisar la revisión 2.

| Id | Decisión | Dónde queda recogida |
|---|---|---|
| D-V1 | Uso condicionado activo por defecto para todo Locus V2 elegible en 1.2.2; sin activación manual, modo experimental, política por documento ni opción de desactivarlo | contrato §1; ADR, decisión 1 |
| D-V2 | Códec de dirección por parámetro aceptado; sin requisito de lectura por 1.2.1; persistencia completa y reproducible dentro de 1.2.2 | contrato §6, obligaciones en §6.6 |
| D-V3 | No se impone estilo de trazo; marca discreta del sistema; estado y garantías consultables en las propiedades | contrato §9; diseño §3.7 |
| D-V4 | Exportación geométrica semántica de regiones condicionadas excluida inicialmente, con diagnóstico; exportación de imagen de lo mostrado; vinculación a la línea futura de B-splines, representaciones derivadas y DXF | contrato §10; diseño §3.8 |
| D-V5 | Uso funcional completo: puntos, seguimiento, intersecciones, transformaciones, construcciones dependientes, loci anidados, métricas, propagación, presentación y manejo | contrato §8, un contrato por operación; diseño §3.6 |
| D-V6 | Reducir el coste de un cambio geométrico real sin caché compartida ni copias mutables compartidas | [documento de rendimiento](../architecture/post_r1_locus_real_change_performance.md) |
| D-V7 | GeoCeDG 1.2.2 es la versión objetivo; el número no se cambia hasta implementar y validar | registro de progreso; ninguna fuente de versión se ha tocado |

## 2. Respuesta del autor a la revisión 2

Fuente: respuesta del autor de 2026-11-21 a la entrega de la revisión 2, sobre el candidato
`1838c721149bb6a4ad9088dff767cc64320cfb00`.

| Asunto | Decisión del autor | Dónde queda recogida |
|---|---|---|
| Diseño | valoración favorable; D-V1 a D-V7 se mantienen | — |
| D-V6 | resultado aceptado técnicamente, con la cobertura de verificación documentada | registro de progreso |
| Aprobación normativa | no concedida todavía: pendiente de una enmienda focal de seis puntos, sin cambiar el objetivo funcional ni revisar el diseño en general | §3 de esta hoja |
| P2, marca del sistema | aprobado el rombo hueco de 7 px, independiente del estilo del usuario; el estado sigue siempre disponible en propiedades | contrato §9.3 y §9.4 |
| P3, exportación semántica | registrarla como requisito de la línea B-splines/DXF, sin abrir todavía una subfase | roadmap, registro aditivo de 2026-11-21; contrato §10.3 |
| P4, defecto F8 | autorizado el triaje temprano y la reparación antes de cerrar I4, adelantándola si afecta a incrementos anteriores | diseño §5 |
| Tras la aprobación | I0 a I4T en secuencia y preparación del candidato funcional 1.2.2, sin nuevos checkpoints autorales rutinarios | registro de progreso |
| Límites | no publicar en `main`, no crear `v1.2.2`, no registrar aprobación autoral por iniciativa del agente | — |

## 3. Enmienda focal de la revisión 3

Los seis puntos pedidos por el autor. El texto exacto es el del contrato candidato; su §14
lleva el diferencial y los criterios de validación.

| # | Punto pedido | Cómo se resuelve | Texto |
|---|---|---|---|
| 1 | Integridad `RELAXED`: `CONDITIONED` no la concede automáticamente bajo autoridad constructiva | se **preservan** los requisitos positivos vigentes y no se propone ampliarlos. La evidencia condicionada no es un tipo de admisión relajada ni una premisa que eleve la integridad; sin evidencia propia de la relación, la integridad es `NOT_ESTABLISHED` | contrato §2, §8 (regla común), §8.1, §8.5 |
| 2 | Identidad de intersecciones: regiones sin linaje frente a selectores que lo exigen; no usar la partición | la región no recibe linaje. La raíz sobre un tramo toma un selector de tipo propio cuyo marco es la rama (linaje de rama más germen transversal), sin rango. Los selectores de raíces certificadas no cambian | contrato §6.5, §8.5, §8.6, §11 |
| 3 | Generaciones: contador de publicación frente a identidad canónica del contenido | dos atributos separados. El contenido es función del conjunto de parámetros con contradicción confirmada; la vigencia se decide por celdas, nunca por contador | contrato §7 |
| 4 | Puntos en bandas: validez puntual frente a continuación | un punto con evaluación aislada y selección válidas está definido también en una banda; la banda solo impide seguimiento, rutas y longitudes | contrato §6.3, §6.4, §8.1 a §8.3 |
| 5 | Dilatación de razón cero | el contrato vigente de `COLLAPSED_IMAGE` se conserva sin cambio: dominios, huecos, direcciones, identidades y métricas nulas | contrato §8.7 |
| 6 | `MARGIN_BOUNDED`: no inferir estabilidad de selección de la separación | la cota solo prueba la separación de las raíces que encierra; H2 queda entera | contrato §3.3 |

Dos consecuencias de la enmienda que el contrato declara y que conviene tener presentes al
aprobar:

- **Punto 1.** En un documento bajo autoridad constructiva, el punto de token de una raíz
  sobre una región condicionada solo se materializa si la recalificación vigente la admite
  (`STRICT`, o `RELAXED` por su propia evidencia positiva). Si no, la raíz se publica y el
  punto queda indefinido con la causa existente. Los documentos bajo la autoridad heredada,
  que es la de por defecto, no cambian. Ampliar la admisión relajada sería una propuesta
  normativa distinta, que este paquete no contiene.
- **Punto 2.** Sobre una fuente condicionada, dos o más raíces del mismo germen en una rama
  se publican sin token durable, porque el rango exige componente certificada y cardinalidad
  verificada. Una raíz que pasa entre una componente certificada y la parte no certificada
  de su rama cambia de selector: su token deja de ser actual, no se transfiere y vuelve a
  serlo cuando el estado regresa.

Corrección añadida por coherencia con el punto 1, fuera de los seis: la revisión 2 resolvía
`Point(L, D)` e `Intersect(L, r, D)` por correspondencia de conductor como punto paramétrico
sobre una región. Contradecía `locus-v2-existence-components.md` §16, que exige componente
certificada. La revisión 3 deja ese contrato sin cambio (contrato §8.1).

## 3 bis. Respuesta del autor a la revisión 3 y precisiones focales de la revisión 4

Fuente: respuesta del autor de 2026-11-21 a la entrega de la revisión 3, sobre el candidato
`afb916e10ce551df06c26000b7ab0ce671c64e3b`.

| Asunto | Decisión del autor | Dónde queda recogida |
|---|---|---|
| Seis enmiendas | confirmadas como correctamente incorporadas; conformidad con la arquitectura general | — |
| Aprobación normativa | no registrada todavía: antes, dos precisiones focales | tabla siguiente |
| Alcance | no ampliar la revisión a otros contratos ni repetir verificaciones que sigan siendo válidas | — |
| Trabajo permitido | trabajos preparatorios de I0 a I3 que no dependan de las precisiones, como candidatos pendientes de la aprobación normativa final; I4 e I4T no se inician | diseño §5; registro de progreso |
| Límites | sin publicación en `main`, sin etiqueta `v1.2.2` y sin release | — |

| # | Precisión pedida | Cómo se resuelve | Texto |
|---|---|---|---|
| 7 | Identidad entre configuraciones: la unicidad actual de un selector de rama y germen no garantiza que una raíz que reaparece sea la misma; regla explícita que impida reutilizar tokens entre raíces diferentes, con test adverso de desaparición y aparición en regiones separadas | una sola asignación por selector, con un **origen** fijo (la dirección por parámetro de la raíz al asignar). El token solo se resuelve si el origen y la raíz actual están en una misma región condicionada; si no, se retiene: no se entrega a otra raíz, no se transfiere, no se retira y no se asigna otro. La evidencia puede retener un token y nunca lo elige | contrato §6.5 (puntos 4 a 9 y consecuencias), §6.6, §8.5; criterios 7a a 7e |
| 8 | Resolución bajo semejanzas: cómo se transforma la cota de H4 y cuándo hay que reclasificar frente a la política de la imagen; ninguna tolerancia incumplida se conserva en silencio | la cota `δ` de la fuente vale `ρ·δ` en la imagen. Con razón absoluta `ρ ≤ 1` las celdas se heredan iguales. Con `ρ > 1` cada tramo se recomprueba en el mundo de la imagen y el que falla se reclasifica, hasta banda en el paso mínimo. Una banda nunca pasa a tramo. La evidencia de la imagen tiene identidad de contenido propia | contrato §8.7, «Resolution under a similarity»; criterios 8a a 8f |

Consecuencias de las precisiones que el contrato declara y que conviene tener presentes al
aprobar:

- **Precisión 7.** Una raíz que la configuración saca de la región de su origen queda sin
  token, y su punto indefinido, hasta que el estado regresa. Otra raíz del mismo selector en
  otra región no recibe token durable en ese resultado; un resultado de intersección nuevo
  tiene su propio ledger y su propio origen. El origen es estado persistido del documento:
  dos historias que asignan en configuraciones distintas pueden diferir en cuándo se retiene
  un token, nunca en qué raíz tiene qué token. Dentro de la región de su origen el token
  significa lo mismo que la selección determinista vigente en una componente certificada, la
  única raíz de su germen: la guarda elimina la reutilización entre regiones que añadía el
  marco de rama y no añade una prueba de continuación que el contrato de intersecciones no
  tiene.
- **Precisión 8.** La imagen de una ampliación puede tener más celdas que la fuente, y
  bandas donde la fuente tiene tramos; entonces rechaza un seguimiento o una longitud que la
  fuente admite. Nunca ocurre lo contrario. Una reducción hereda las celdas de la fuente sin
  fusionarlas.

Pruebas discriminantes: los criterios 7a a 7e y 8a a 8f del contrato §15, y su ejecución
sobre evaluadores de verdad conocida en la sonda
`tools/benchmark/post-r1-locus/conditioned-use-precisions-probe.js` (casos C17 y C18 del
banco del diseño), que aplica en paralelo la regla de la revisión 3 y la de la revisión 4.

## 4. Qué se somete a aprobación

El **texto** del paquete revisado, en la rama `maintenance/geocedg-1.2.2-post-r1`:

| Documento | Papel |
|---|---|
| [ADR 0042, revisión 4](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md) | decisión de arquitectura propuesta, con las diferencias respecto a las revisiones 1, 2 y 3 |
| [Contrato candidato, `conditioned-use/v4-candidate`](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md) | texto normativo candidato, matriz de operaciones, enmiendas que su adopción requeriría, diferenciales y criterios de validación (§14 y §15) |
| [Diseño, revisión 4](../architecture/post_r1_locus_conditioned_use_design.md) | inspección de capacidades, extensión mínima por operación, plan 3B, banco adversarial |

La identidad exacta del paquete (commit y árbol) consta en el
[registro de progreso](post_r1_locus_repairs_progress.md), checkpoint 6. Una aprobación
debe citar ese commit.

Mientras el texto no esté aprobado no se inicia la semántica V (bloque 3B), salvo los
trabajos preparatorios de I0 a I3 que el autor permitió el 2026-11-21, que quedan como
candidatos pendientes y no cambian ninguna operación pública. El motivo es
AGENTS §3.1 y §9: V cambia contratos de admisibilidad, identidad y evidencia de
`locus-v2-existence-components.md` §15.1, §15.6 y §15.8.

## 5. Punto que requiere decisión del autor

Queda uno: la **aprobación normativa definitiva** del ADR 0042 revisión 4 y del contrato
`conditioned-use/v4-candidate` como base para 3B, con las enmiendas del contrato §12 a
redactar sobre las especificaciones al implementar cada incremento. Si el autor corrige
algún punto, se entrega primero el texto corregido.

## 6. Punto de reanudación

Con esa aprobación se reanuda en 3B sin repetir lo cerrado, a partir de lo que los trabajos
preparatorios hayan dejado como candidato: verificar la aprobación
y el commit que cubre, comprobar si la base ha cambiado y seguir el
[plan](../architecture/post_r1_locus_conditioned_use_design.md#5-plan-de-incrementos-3b):
I0, I1, I2, I3, I4, I4T, y después el candidato funcional GeoCeDG 1.2.2, sin checkpoints
autorales rutinarios intermedios. Siguen exigiendo decisión expresa del autor: publicar en
`main`, crear `v1.2.2`, ejecutar `FINAL` y registrar cualquier aprobación autoral.

## 7. Aprobación normativa de 2026-11-22

Fuente: instrucción del autor «APROBACIÓN AUTORAL — ADR 0042 Y USO CONDICIONADO DE LOCUS
V2», de 2026-11-22, transcrita en el
[registro de aprobación](post_r1_conditioned_use_author_approval_record.md).

| Asunto | Decisión del autor |
|---|---|
| Texto | aprobados el ADR 0042, revisión 4, y el contrato `conditioned-use/v4-candidate`, del commit `9160d67d7`, como base autorizada para implementar el uso condicionado |
| Cuestiones anteriores | las ocho, resueltas; aprobadas D-V1 a D-V7 y las dos precisiones |
| Implementación | bloque 3B completo, I0 a I4T en secuencia, sin consultas autorales rutinarias |
| Validación | criterios 7a a 7e y 8a a 8f sobre el kernel real, no solo con la sonda externa |
| Separación | evidencia condicionada, integridad constructiva, certificación rigurosa y completitud global siguen separadas |
| F8 | la reparación y sus pruebas de regresión se conservan |
| Después de I4T | candidato funcional GeoCeDG 1.2.2, con comprobaciones de Desktop, persistencia, rendimiento y app-image |
| Límites | sin publicación en `main`, sin etiqueta `v1.2.2`, sin release y sin `FINAL` hasta una decisión expresa posterior; un resultado automatizado aceptado no es aprobación autoral del incremento |
