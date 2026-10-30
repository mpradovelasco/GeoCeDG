# CU7-I1-R1 — enmienda candidata de admisibilidad local

Estado: CANDIDATE — REQUIRES SPECIFIC AUTHOR APPROVAL. No normativa vigente,
no implementación ni permiso nuevo. Evidencia: informe CU7-I1-M08-S-R1 y archivo autoral h.
ADR 0019 aceptado, ADR 0041 y M08 aprobado se conservan sin modificaciones.

## Problema demostrado

ADR 0019 decisión 4 y M08 §2 pasos 2/4 exigen que un único candidato descubierto por
evaluador general permanezca UNRESOLVED y no se ofrezca. La existencia local certificada
de componente, la evidencia numérica local ESTABLISHED y la evaluación forward pueden
coexistir con cobertura inversa global no establecida. El h del autor lo reproduce en main
y candidato; no requiere solucionar M09 ni reconstrucción general.

## Propuesta para decisión autoral (no aplicada)

Preservar el resultado histórico R6 y su significado, incluidos UNRESOLVED y getUniqueCandidate.
Añadir en una futura frontera M08-S un resultado separado con dimensiones independientes:

| Dimensión | Valores propuestos |
|---|---|
| Cobertura inversa | COMPLETE_REQUESTED_SCOPE / INCOMPLETE / NOT_ESTABLISHED |
| Admisibilidad local de cada dirección | ESTABLISHED / NOT_ESTABLISHED / SINGULAR / NONISOLATED |
| Vigencia | CURRENT / STALE / CONSUMED |
| Oferta M08-S | EXPLICIT_LOCAL_SELECTION_REQUIRED / CERTIFIED_UNIQUE / NOT_OFFERABLE |

EXPLICIT_LOCAL_SELECTION_REQUIRED puede contener una o varias direcciones. Incluso una
sola exige confirmación explícita de su dirección, sin afirmar unicidad ni mínimo global.
No reinterpretar silenciosamente UNRESOLVED; un adaptador frontend no puede fabricar permiso.
La nueva oferta y su evidencia deben ser emitidas por el kernel bajo contrato aprobado.

Precondiciones conjuntivas: fuente vigente y válida; componente continuo certificado;
dirección canónica branch/component/u y reglas de seam; evidencia local ESTABLISHED con
grado numérico real; evaluación forward finita reproducida; aislamiento/separación demostrados
en la celda declarada; ausencia de barrera local insegura; no presupuesto agotado; elección
miembro de esa consulta, vigente al publicar. La ausencia de cobertura global por sí sola
no anula esa oferta local, pero una causa desconocida, singularidad, candidatos no separados,
inconsistencia de fuente o límite de trabajo la bloquea.

El código actual conserva candidatos aun en búsquedas inseguras: isEstablished() y residual
por sí solos NO bastan para emitir permiso. La futura frontera debe transportar la evidencia
de ausencia de barreras locales, no inferirla de una cadena diagnóstica o de la cantidad de
candidatos. Las garantías de exactitud/STRICT no cambian.

La confirmación elige la definición forward, nunca identidad por distancia, posición, índice
o enumeración. Q se descarta como dependencia. La edición/arrastre R6 existente no recibe
automáticamente esta ampliación; requerirá su propio alcance si fuera necesario.

## Textos a conciliar tras aprobación

- ADR 0019 decisión 4: conservar none/unique histórico; excepción explícita SOLO para la nueva
  frontera M08-S de oferta local, sin afirmar global exclusion.
- ADR 0019 Evidence model: distinguir creación por unicidad certificada y elección local
  autorizada en M08-S; nunca convertir estimación en STRICT.
- locus-v2-point-interaction: resultado de compatibilidad intacto; nuevo permiso/tipo separado.
- M08 aprobado §2: conservar paso 2; en paso 4 permitir SOLO el nuevo resultado y condiciones
  anteriores, sin ofrecer candidatos del UNRESOLVED histórico directamente.
- Superficie pública y ADR 0041: enlazar la operación explícita Q y su política nueva solo
  después de aprobación. Persistencia continúa como forward existente, sin codec nuevo.

## Puerta posterior propuesta

Autorizar específicamente diseño detallado del nuevo resultado y su implementación acotada.
Antes de publicar: h nativo del autor y locus general sintético; un candidato local sin
unicidad global; varios locales; singular/no aislado; presupuesto agotado; barrera insegura;
fuente obsoleta; cancelación; Q libre/dependiente; DAG/identidad, Undo/Redo, .cedg y copia;
regresión SplineV2 y arrastre existente; selección PHASE e INTEGRATION canónicas.

Hasta esa decisión el h autoral se rechaza explícitamente en creación desde Q. La autoría
Q para fuentes ya admitidas puede certificarse independientemente. No nueva autorización
de M08-D, M09/A0, I2G/I3, CU10, reconstrucción general ni deuda de tangencias.
