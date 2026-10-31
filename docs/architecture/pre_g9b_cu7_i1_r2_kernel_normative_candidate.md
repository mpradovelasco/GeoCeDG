# CU7-I1-M08-S-R2 — enmienda final candidata del puente compartido

Estado: **CANDIDATA PARA APROBACIÓN FINAL; ADR ACEPTADOS NO MODIFICADOS**.
La decisión autoral de 2026-10-31 aprueba la dirección de admisión individual y autoriza
el puente focal, no su aceptación como producto ni Desktop. Este texto concilia la
[precisión R2](pre_g9b_cu7_i1_r2_refinement_local_selection.md) con el perímetro implementado.
No promueve M08-S a PASS — AUTHOR APPROVED. selfApproved=false.

## Evidencia y contrato efectivo del candidato

`M08SnapshotPlacement2D` es una frontera experimental exclusiva de creación estática:
consulta un valor transitorio o un GeoPoint existente, devuelve un Result y un contexto
Selection, y requiere un Choice concreto en commit. Conserva la consulta histórica R6.

| Dimensión | Implementación focal y límite |
|---|---|
| Descubrimiento | semillas de la búsqueda R6 existente; sus diagnósticos/estados no cambian |
| Barreras | auditoría package-private del camino de búsqueda; no inferida de isEstablished, reason, cantidad ni residual; barrera observada veta toda oferta nueva |
| Dirección | fuente durable, definición/revisión actual, proveedor, parámetro canónico y selector de componente reconstruible; vecinos inmediatos contenidos en esa componente |
| Proximidad | caja exterior del valor en el argumento seleccionado; cota exterior de distancia cuadrada estrictamente bajo la cota inferior del radio cuadrado |
| Regularidad | modelo intervalar existente C1 en una vecindad interior de doubles adyacentes; primer jet en toda ella excluye cero en alguna coordenada; sin mínimo de distancia |
| Configuración | únicamente autoridad representada heredada; documento constructivo sin transferencia disponible se rechaza explícitamente, sin cambiar su política |
| Forward | evaluación normal finita dentro de la caja del modelo y dentro del radio numérico; el punto publicado debe reconstruir exactamente la dirección requerida |
| Grado | CERTIFIED_ERROR_BOUND para enclosure de la configuración representada; las coordenadas Java mantienen su grado anterior, no EXACT/STRICT por esa evidencia |
| Cobertura | resultado/cobertura histórica separados; GlobalCoverage.NOT_ESTABLISHED en la vía nueva, sin convertir unicidad R6 de su antiguo problema en unicidad de todas las direcciones próximas |
| Permiso | EXPLICIT_LOCAL_SELECTION_REQUIRED, aun con una sola oferta; ningún getUniqueCandidate nuevo ni autoselección |
| Vigencia | Selection.getValidity: CURRENT/STALE/CONSUMED, independiente del Result inmutable; revalida vínculo a registro, ID, definición, autoridad y Q actual si fue proporcionado |
| Trabajo | presupuesto de descubrimiento conservado y remanente reservado para valor, jet y forward de cada oferta y su revalidación; no reiniciar el contador para ocultar coste |
| Publicación | Choice privado a su consulta, handshake consumible sólo dentro de commit, misma construcción forward, inputs dedicados y rol R6 existentes; un Undo normal a cargo del host |

La vecindad se usa para probar regularidad, no para exigir que toda su imagen sea próxima
ni para aislar una raíz de distancia. La prueba puede ser más conservadora que el núcleo
matemático mínimo. No se amplía a fronteras/seams, modelo no capturable, predicados dudosos,
gaps sin pertenencia certificada, fuente colapsada/singular o presupuesto agotado.

La disponibilidad de semillas R6 es una limitación de descubrimiento, no un certificado
de mínimo en el permiso nuevo. Algunas direcciones matemáticamente válidas pueden quedar
sin ofrecer por ese descubrimiento conservador o por sus barreras; no se afirma completitud.
El tipo de evidencia local nuevo no es `LocusPointInteractionLocalEvidence2D` y no se fabrica
un candidate histórico que afirme «mínimo» para publicar una dirección individual.

## Texto propuesto para ADR 0019

Añadir a decisión 4 y Evidence model, sin sustituir las reglas de R6:

> M08-S puede seleccionar explícitamente una dirección individual canónica, actual y
> reconstruible mediante una frontera independiente. Debe acreditar pertenencia a componente,
> forward válido, proximidad world con grado declarado, regularidad del primer perímetro,
> fidelidad de configuración, ausencia de barreras aplicables, presupuesto y vigencia.
> No exige un mínimo local/global ni completitud global de búsqueda. Sin unicidad demostrada,
> incluso una sola oferta requiere elección/confirmación explícita. El estado UNRESOLVED
> histórico permanece intacto y no da permiso por sí mismo; la oferta necesita evidencia
> individual adicional y tipada. No amplía edición, arrastre, EXACT ni STRICT.

## Texto propuesto para ADR 0041

Añadir a decisiones 2/3:

> La normalización M08-S usa el valor actual de Q sólo durante la selección. La nueva vía
> de dirección individual distingue evidencia local, cobertura global y permiso, sin afirmar
> inversión única ni proyección métrica. Sólo persiste la construcción forward aprobada y
> sus padres semánticos; no se añade Q, consulta, prueba o política como padre/codec. La
> validez actual de la selección es independiente de la evidencia inmutable y se revalida
> antes de publicar. La vía compartida no autoriza automáticamente una herramienta Desktop.

## Especificaciones a conciliar después de aprobación

- `locus-v2-point-interaction.md`, sección nueva: contrato del Result/Choice/Selection y
  evidencia anterior; status R6 y garantías históricas conservados, sin reinterpretación.
- `candidates/cu7-m08-point-placement.md`, §2: añadir la nueva vía al lado de UNIQUE/MULTIPLE
  históricos. Una dirección ofrecida desde el UNRESOLVED necesita prueba independiente y
  elección explícita; «ninguna oferta probada» no es ausencia certificada.
- `locus-v2-public-surface.md`: registrar el puente compartido experimental y su límite
  de autoridad/proveedores. No anunciar una interfaz L→Q implementada; su futuro contrato
  conserva dirección avanzada y selector/confirmación contextual con autorización separada.

No cambios de modelos certificados, política constructiva, existencia, formatos o identidad.
No se exime una transferencia constructiva por haber demostrado cotas en el modelo capturado.
Los consumidores R6 previamente válidos permanecen en sus rutas anteriores.

## Siguiente paso exclusivamente propuesto

Tras dictamen favorable de la puerta científica y decisión autoral separada: implementar
el flujo L→Q, consumir los permisos compartidos, conservar creación directa/arrastre ya
admitidos y reducir el diálogo avanzado. La propuesta de interacción y política de sesión
está en [el diseño conservado](pre_g9b_cu7_i1_r2_refinement_interaction.md). Este incremento
se detiene antes de tocar Desktop. M09, M08-D y drag general permanecen fuera de alcance.
