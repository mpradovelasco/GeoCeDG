# M08-S-R2-KERNEL-H1 — presupuesto de admisibilidad

Estado: **CANDIDATO TÉCNICO PENDIENTE DE REVISIÓN AUTORAL**. selfApproved=false.
No Desktop, smoke autoral ni aprobación final de M08-S. Fecha: 2026-10-31.
Base: a43635a49d958f4993bfd30cc083c05df66c527c,
árbol 8e2074fa9021f928eeda64444120d59ad7f1f963, verificados limpios.
Rama independiente: codex/pre-g9b-cu7-i1-m08-s-r2-kernel-h1.
[Plan previo y alcance congelado](../architecture/pre_g9b_cu7_i1_r2_kernel_h1_plan.md).

## Diagnóstico causal y evidencia antes/después

El descuento fijo de tres unidades confundía pases intervalares y una llamada
forward Java con misses semánticos. La sesión reference del forward no tenía límite
ni compartía el presupuesto del descubrimiento. La fábrica volvía a evaluar al
construir el punto, después de reservar IDs. Además, la modalidad CU1 permite cerrar
un servicio anidado antes de verificar el límite en el root: no equivale a un límite
duro de cada miss. Excepciones de evaluación pueden convertirse en resultados
inválidos por los evaluadores heredados; era necesario conservar la señal de límite.

La sonda optativa M08BudgetH1ProbeTest usa Java real, SplineV2 recta y una cadena de
traslaciones identidad. Suma los contadores de evaluator de TODAS sus definiciones,
independientemente del contador del puente. Se ejecuta el mismo archivo antes y
después; no se recibió el código de la auditoría externa y no se le atribuye esta
sonda. Se reproduce exactamente su síntoma 149/154, y se caracteriza la confirmación.

| Profundidad | Antes: consulta / total con commit | Después: consulta / total | Resultado posterior con límite 149 |
|---|---|---|---|
| 8 transformaciones, 9 evaluadores | 126 / 144 | 126 / 135 | creación completa dentro del límite |
| 9 transformaciones, 10 evaluadores | 140 / 160 | 140 / 149 | BUDGET_EXHAUSTED al confirmar; sin creación |
| 10 transformaciones, 11 evaluadores | 154 / 176 | 149 / 149 | BUDGET_EXHAUSTED en consulta; sin oferta |

Artefactos separados: artifacts/pre-g9b-cu7-i1-r2-kernel-h1/before,
after-working y focal. Todos conservan proceso, exit hijo y JUnit. El antes tiene
identity.json con SHA/árbol original y hash de la sonda. El productor neutral devuelve
0 aunque un hijo falle: sólo inner_exit_code y JUnit establecen el resultado real.
El foco preparatorio pasó 91 casos: 11 H1, 24 puente, 52 R6 y 4 sesiones CU1.

## Contrato de contabilidad utilizado

| Unidad | Definición y límite acumulado consulta + confirmación |
|---|---|
| Miss semántico | una entrada efectiva a un evaluator de una definición; cada nivel de semejanza y cada hijo de reconstrucción consume su propio miss. Límite: maximumSemanticEvaluations de la política |
| Hit de caché exacta | no ejecuta evaluator; cero misses. La revalidación limpia valores cacheados, conservando contadores/límites, para ejecutar de nuevo el forward |
| Pase intervalar | CAPTURE, SMOOTHNESS, VALUE_ENCLOSURE o FIRST_JET_ENCLOSURE del modelo capturado completo. Límite propio acumulado: maximumSubdivisions de la misma política |
| Intento de instalación/publicación | cero antes de completar todas las pruebas y el binding; uno como máximo por Selection. No tiene evaluación semántica pendiente |

Un pase intervalar no pretende medir FLOPs, nodos Java o tiempo. Incluye el recorrido
completo del programa/modelo y sus semejanzas; el pase de enclosure incluye sus
predicados internos de dominio, y la verificación del valor incluye la aritmética
exterior de proximidad. El chequeo explícito de suavidad cuenta su propio pase. Los
modelos conservan sus límites estructurales aprobados. No se añade subdivisión ni
refinamiento a la evidencia local: el descubrimiento conserva sus propios límites
R6 de componentes, subdivisiones, refinamientos y candidatos. Los dos contadores
que toman una cota numérica de maximumSubdivisions son unidades distintas, no una
mezcla ni un aumento oculto del límite semántico. Ninguno cambia la geometría.

WorkSnapshot expone límites y remanente reales, total semántico del root, trabajo de
descubrimiento, pases por clase y publicaciones. La instrumentación R6 histórica
conserva su significado y no se convierte en contador acumulado M08. Query y commit
usan la misma sesión/contadores: no hay reinicio del presupuesto ni reserva ficticia.
La consulta sólo ofrece pruebas terminadas. Una oferta válida puede agotar el
remanente y requerir un rechazo al confirmar; nunca autoriza omitir esa revalidación.

La modalidad hardBoundedForSnapshot comprueba antes de cada miss, incluso en hijos,
y conserva agotamiento sticky ante excepciones absorbidas. La modalidad CU1 normal
se mantiene; sus cuatro pruebas, incluida la frontera histórica 12/10, siguen activas.
El depth limit existente también impide oferta por BUDGET_EXHAUSTED; no es degeneración.

## Reconstrucción y publicación sin deuda de trabajo oculta

prepareForward usa el mismo LocusMetricPositionBinder2D, con sesión explícita y
presupuesto duro. Obtiene el binding de dirección, componente, revisión y punto
antes de crear inputs o reservar IDs. La evidencia intervalar y proximidad siguen
comprobándose; no se admite un valor por ser cacheado ni por tener residuo pequeño.

En el commit válido, Choice habilita dos consumos sellados: dirección de publicación
y binding inicial. Fuera del commit, en otro hilo, tras consumir o con fuente caducada
se rechazan. La fábrica usa el mismo AlgoSemanticLocusPoint2D; sólo su primera
evaluación consume el binding ya producido por el binder normal, verificando posición
y revisión. No se reevalúa después de la reserva de IDs. El binding temporal se destruye
al consumir/fallar/cancelar y no se serializa. Las recomputaciones futuras, carga,
Undo/Redo y copia usan la ruta forward habitual, sin Q como nuevo padre ni nuevo codec.

El caso de coste exactamente suficiente tiene consulta 154 + confirmación 11 = 165;
no aparece la reevaluación adicional de 11 durante instalación. Con 164 se rechaza
antes de cualquier reserva/publicación y se conserva XML completo, incluidos registros
e identidad. No se usa rollback XML ni una transacción M09 para esconder un gasto tardío.
El perímetro es el trabajo de admisión/forward de esta operación; no un presupuesto
universal de renderizado ni de escrituras arbitrarias ajenas al procedimiento.

## Inventario mínimo de cambios y conservación de contratos

| Fuente productiva compartida | Razón |
|---|---|
| LocusEvaluationSession2D | límite duro optativo y señal sticky; defaults históricos intactos |
| LocusPointInteractionResolver2D | recibir una sesión M08 suministrada; público R6 conserva sus sesiones y estados |
| M08SnapshotPlacement2D | sesión propietaria, remanente, costes reales, preparación y capabilities de un uso |
| M08LocalAddressEvidence2D | cobro explícito previo de pases intervalares; mismas pruebas matemáticas |
| LocusMetricPositionBinder2D | overload con sesión explícita; mismo binder y causas |
| AlgoSemanticLocusPoint2D | consumo inicial sellado del binding; actualizaciones ordinarias iguales |
| LocusV2PublicOperations | transportar la preparación al algoritmo forward, sin cambiar participación/codec |

Todos están en namespace GeoCeDG del kernel compartido. Se inspeccionaron también
los programas intervalares reales, evaluación compuesta/reconstruible, registros de
participación, Construction y la infraestructura de verificación; no se modifica
código GeoGebra ajeno ni Desktop. No nuevo comando, política de admisión, identidad,
formato o migración. La enmienda matemática R2 sigue candidata; no cambia ningún ADR
aceptado. Su párrafo experimental de coste fijo queda sustituido por este contrato
H1 para el candidato corregido, sin reescribir retrospectivamente el expediente R2.

## Matriz focal y evidencias

| Prueba H1 | Garantía |
|---|---|
| h1Limit149CannotOfferAfter154ActualEvaluations | reproduce 149/154 y rechazo tipado; contador independiente y XML intacto |
| exactlySufficientAccumulatedBudgetPublishesWithoutAnUnchargedReevaluation | 165 exactos, publicación única y recomputación futura normal |
| oneSemanticUnitShortAtCommitLeavesDocumentAndIdentitiesUntouched | 164, sin P/reservas/publicación; XML exacto y selección consumida |
| oneSemanticUnitShortAtQueryCannotPublishPartialProof | 153, prueba incompleta nunca seleccionable |
| exactlySufficientQueryHasNoFictitiousRemainderForConfirmation | 154, consulta completa pero confirmación rechazada con remanente cero |
| intervalPassesHaveTheirOwnExplicitUnitsAndExactAccumulatedLimit | 8 pases, dos por clase; no se confunden con 15 misses semánticos |
| oneIntervalPassShortAtConfirmationRejectsBeforeAnyReservation | 7 pases, rechazo antes de publicación sin cambio XML |
| staleCancelledAndConsumedSelectionsCannotSpendMoreWork | no gasto nuevo ante selección caducada/consumida |
| preparedForwardCapabilityCannotBeConsumedOutsideItsCommit | bypass rechazado antes de construir algoritmo/GeoElement |
| hardSessionCountsNestedServicesEvenWhenTheirExceptionIsAbsorbed | root duro 3 = 1 propio + 2 hijos; señal sticky |
| exactKeyCacheHitsDoNotPretendToExecuteSemanticEvaluators | hit cero, contador de evaluator independiente y miss adicional rechazado |

Se conservan 24 pruebas del puente y las 52 regresiones R6. La sonda real original
Cu7M08SRealCaptureProbeTest sigue siendo obligatoria y distinta de la cadena sintética.
Carga C:/Temp/TestM08-S.cedg, SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d; crea ReferenceR1
expresamente y comprueba el programa real h, evidencia de valor/jet, creación, ausencia
de padre Q, guardado/reapertura .cedg y copia/remapeo. El init H1 escribe en una carpeta
nueva, sin sobrescribir pruebas R2 anteriores. Mantiene CERTIFIED_ERROR_BOUND del
modelo representado, sin mínimo, unicidad global, STRICT ni nueva autoridad constructiva.

## Verificación exacta del candidato

VERIFICATION_CLASS=INTEGRATED_PHASE. Nueva PHASE explícita
PRE-G9B-CU7-I1-M08-S-R2-KERNEL-H1: conserva el foco R2 y añade H1/sesiones CU1.
Las nuevas identidades se derivan de JUnit ejecutado mediante Read-VerificationJUnitFiles
y Get-VerificationDeterministicHash. Discovery canónico e inventario ejecutado completo
se actualizan con update-verification-junit-inventory.ps1; no identidades supuestas,
allowlists nuevas o cambios de runner. El registro acotado conserva selecciones antiguas.

Comandos sobre el commit limpio congelado de la entrega:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel-h1/final/static
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel-h1/final/infra_unit
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU7-I1-M08-S-R2-KERNEL-H1 -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel-h1/final/phase
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/pre-g9b-cu7-i1-r2-kernel-h1/final/integration
git diff --check a43635a49d958f4993bfd30cc083c05df66c527c HEAD
```

Sonda H1 optativa: productor gradle-command-evidence-producer.ps1, tarea
:shared:common-jre:test, filtro org.geocedg.common.locus.M08BudgetH1ProbeTest, init
geocedg/validation/pre-g9b-cu/m08s-r2-h1-probe.init.gradle y propiedad
-Dgeocedg.h1.junit=artifacts/pre-g9b-cu7-i1-r2-kernel-h1/final/h1-probe/junit.
Sonda nativa: mismo productor, tarea :desktop:desktop:test, filtro
org.geocedg.desktop.Cu7M08SRealCaptureProbeTest e init
geocedg/validation/pre-g9b-cu/m08s-r2-h1-native.init.gradle.

El índice artifacts/pre-g9b-cu7-i1-r2-kernel-h1/delivery.json identifica SHA/árbol,
exits y resultados finales reales; este texto congelado no presupone su terminación.
INTEGRATION añade cobertura real de consumidores de sesiones/bindings, DAG, identidad,
persistencia, modelos intervalares y rutas forward fuera del foco. No FINAL ni pruebas
de otros incrementos. Se conservan los diagnósticos históricos no bloqueantes.
BOOTSTRAP IMPACT: NO CHANGE REQUIRED; mismos runtimes/configuración/cachés.
GUIDE_IMPACT=UPDATED; VERIFICATION_INFRASTRUCTURE_IMPACT=BOUNDED_CATALOG_REGISTRATION.

## Dictamen y parada

Recomendar revisión del puente para la autorización Desktop sólo si PHASE,
INTEGRATION y sondas finales satisfacen sus puertas. No se implementa L→Q ni se declara
PASS — AUTHOR APPROVED. Continúan los límites R2: autoridad heredada, modelos
capturables/regulares/interiores, rechazo conservador de seams/barreras, sin arrastre
general. No cambia el dictamen de M09 ni se abre M08-D/A0/I2G/I3. CU10, integración
editorial y deuda de certificación exacta generalizada de tangencias se conservan.
Main, candidato auditado, históricos y libro intactos; sin publicación ni etiquetas.
Entregar identidad/evidencias y detenerse para revisión y smoke autoral posterior.
