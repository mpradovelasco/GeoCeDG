# CU7-I1-M08-S-R2 — matriz del puente compartido y límites de cobertura

Estado: candidato técnico para revisión, sin smoke ni aprobación final. Implementación
Desktop no iniciada. Los resultados efectivos están en los informes/índice de entrega;
esta tabla describe qué prueba constituye evidencia de cada requisito, no una ejecución UI.

Clase focal compartida: `org.geocedg.common.locus.M08SnapshotPlacementKernelTest`.
Regresión histórica: `G9U0R6SemanticLocusPointInteractionTest`, sin modificar sus 52 casos.

| Requisito | Prueba focal nueva | Alcance y oráculo |
|---|---|---|
| dirección individual, no promoción UNRESOLVED | generalConstructionGetsIndividualProofWithoutPromotingR6 | grafo real no afín; R6 sigue UNRESOLVED, Choice tiene enclosure y parámetro exacto de su semilla |
| Q no participa duramente ni es padre nuevo | referenceIsNotAssignedIdentityOrAddedAsParentAndItsMotionIsIgnored | ID Q sigue null, DAG sin Q; mover Q no mueve P; entrada real de L sí propaga a P y sus dependientes |
| fuente A→B→A | sourceABAInvalidatesOldChoiceWithoutMutationAtRejection | revisión semántica no se reutiliza; rechazo STALE, XML idéntico al estado de entrada del rechazo |
| Q cambia antes de confirmar | changingReferenceBeforeConfirmationRejectsWithoutMutation | valor copiado y punto existente se revalidan, sin arista dinámica |
| candidato ajeno y bypass de publicación | foreignChoiceAndPublicationBypassAreRejected | Choice privado a contexto, handshake no disponible fuera de commit, XML igual |
| cancelar/repetir | cancellationAndReplayCannotPublish | commit null/close y replay no crean ni publican |
| reconstrucción/carga | reloadInvalidatesSourceHandleAndPointRoundTripRetainsForwardAddress | guardas de registro antes de consultar fuente desligada; ID/dirección del P reabierto |
| Undo/Redo | nativeStyleCreationHasOneNormalUndoRedoBoundary | host almacena una acción normal; restaura ID, no transacción general M09 |
| agotamiento | exhaustedDiscoveryBudgetCannotOfferPartialEvidence | no oferta parcial, no documento modificado |
| autoridad activa | constructiveAuthorityIsNotSilentlyCertifiedInCapturedConfiguration | rechazo explícito; no transferencia por proximidad ni cambio automático de política |
| extremos/seam periódico | endpointAndPeriodicSeamAreRejectedByInteriorEvidenceContract | vecindad interior obligatoria; periodo v1/v2 no se amplía |
| proximidad con margen | distanceMustBeProvenRatherThanMerelyFinite | fuera de radio e igualdad incierta rechazados por intervalos, no sólo residual |
| SplineV2 | ordinarySplinePathAndDragKeepTheirHistoricalContract | creación forward y movimiento R6 admitido siguen sus contratos |
| mínimo innecesario | certificateCanEstablishNearAddressThatIsNotADistanceMinimum | d'(0) no nulo por cota de F'_y y diferencia q_y; evidencia nueva no afirma mínimo |
| singularidad | zeroDerivativeAtFoldCannotUseFloatingRegularityAsProof | proyección de círculo con velocidad cero; primer jet no prueba regularidad |
| gaps | disconnectedDomainDoesNotBridgeTheGap | no se inventa una componente entre intervalos válidos |
| proveedor insuficiente | unknownNonAffineProgramCannotGainCertificateFromNumericalSeed | expresión fuera de captura; una semilla/float no gana prueba intervalar |
| Q dependiente | dependentReferenceUsesOnlyItsCopiedValue | ni Q ni su antecedente independiente se añaden como padres de P |
| antecedencia legítima | referenceAncestorKeepsOnlyItsExistingRouteThroughTheLocus | spline y semejanza con Q como centro; influencia sólo a través de L |
| límite real de antecedencia en h | nativeAncestorQueryWithSeamCandidateKeepsTheConservativeRejection | consulta desde I/radio 1 incluye semilla de frontera no acreditada; la nueva vía conserva rechazo íntegro |
| referencia inválida | undefinedAndDetachedReferenceAreRejectedBeforeQuery | Q indefinido/null, rechazo INVALID_REFERENCE, XML igual |
| varias direcciones | multipleProvenAddressesRequireAnExplicitMember | dos ofertas verificadas; publicación del miembro elegido, sin transporte de raíz ni selección por índice durable |
| propietario de ejecución | publicationFromAnotherThreadIsRejectedBeforeAnyMutation | no publicación fuera del hilo propietario de la consulta |
| vigencia separada | validityIsSeparateFromImmutableQueryEvidence | CURRENT/STALE/CONSUMED independientes del Result histórico/inmutable |

## Caso nativo obligatorio, separado de las fixtures reconstruidas

Sonda `Cu7M08SRealCaptureProbeTest` fuera de discovery ordinario, ejecutada sobre Java real.
Archivo C:/Temp/TestM08-S.cedg, SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d. ReferenceR1 no existe
en ese archivo: la sonda añade el punto (.8976484384297626,1.7854818601338847) expresamente.

- `captureNativeHBeforeAnyNewAdmission`: captura el programa constructivo real y sus
  constantes/pasos, enclosures de valor/jet en el argumento R1 y vecindades interiores;
  comprueba ausencia de mutación XML. No sustituye el programa por una elipse ideal.
- `authorizedBridgeOnExactNativeHasForwardOnlyPersistenceAndCopy`: consulta por la frontera
  nueva, permiso explícito, R6 UNRESOLVED conservado, creación de P, ID/dirección, Q sin padre
  ni ID incorporado, movimiento Q ignorado, guardado de copia .cedg y reapertura real, copia
  con ID/linaje nuevos y original sin cambios. No interacción Desktop simulada ni smoke autoral.

La captura nativa informa programa v1 CIRCLE/PARALLEL/MEET/MIDPOINT, radio capturado
1.2787102323238368, base (1,0),(0,1), centro de I. En u=-6.407985253531479e-11:
F_x∈[.8876484384297622,.8876484384297629],
F_y∈[1.7854818600519442,1.785481860051946],
F'_y∈[1.278710232323836,1.2787102323238377] en la caracterización inicial.
El puente obtiene cota superior de distancia cuadrada 1.0000000000000913e-4, dentro de
radio .1. CERTIFIED_ERROR_BOUND del modelo representado no certifica coordenadas Java
exactas, mínimo ni STRICT. El grado del forward numérico se conserva.

## Límites que no deben convertirse en PASS genérico

La puerta es para el perímetro capturable, regular e interior bajo autoridad heredada, y
para h/ReferenceR1 concretos. No certifica todo Locus V2 ni todos los Q. La búsqueda sigue
siendo acotada; conservar barreras R6 puede dejar sin oferta direcciones matemáticamente
válidas. La implementación inicial veta toda oferta nueva si cualquiera de las semillas
retenidas no supera las pruebas locales: no afirma ausencia y no cambia las rutas antiguas.
En particular, I como Q/radio 1 sobre h no es la fixture positiva de antecedencia del puente.

Falta transferencia para documentos constructivos; no se admite por estimación. Extremos,
seams, singularidades, modelo no disponible o coincidencias no resueltas siguen rechazados.
No hay algoritmo general de proyección ni arrastre nuevo. Los casos oscilantes/estrechos fuera
de modelo no reciben una garantía universal porque pasen controles suaves o affine-spline.

Los casos UI, pantallas/DPI, selector/confirmación contextual y diálogo compacto de la
matriz de precisión R2 siguen pendientes. Tampoco se declara aquí un rollback universal
ante escritores/callbacks arbitrarios: se reutiliza el constructor forward aprobado, sin
introducir la transacción M09. Los rechazos de admisión ocurren antes de reservar/publicar.

## Propuesta de siguiente autorización y smoke

Después de revisión autoral del puente y la enmienda: autorizar por separado Desktop L→Q,
con confirmación contextual obligatoria para este h sin unicidad global; mantener autoría
avanzada y flujos previos de SplineV2. No arrancar automáticamente esa implementación.

El [smoke futuro conservado](pre_g9b_cu7_i1_r2_refinement_matrix_smoke.md) deberá usarse sobre
el candidato Desktop posterior. Precisión: su ejemplo de Q antecedente I puede rechazar por
semilla de seam en este primer perímetro; usar además la fixture positiva de spline/semejanza
para verificar la influencia legítima sin padre nuevo. No declarar ejecutado ese smoke, ni
presentar el diálogo actual como sustituido. M08-S sigue pendiente de smoke y aprobación.
DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG, M08-D, M09/A0, CU10 y deuda exacta conservados.
