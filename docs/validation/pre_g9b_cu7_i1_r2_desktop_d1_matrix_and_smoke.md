# M08-S-R2 Desktop D1/D2 — matriz y smoke revisado

Candidato pendiente de revisión independiente y smoke autoral; selfApproved=false.
Este protocolo NO declara smoke ejecutado. Resultados y SHA exactos sólo en
artifacts/pre-g9b-cu7-i1-r2-desktop-d1/delivery.json.

## Matriz de regresiones y oráculos

| Obligación | Prueba/evidencia Java real | Oráculo y límite |
|---|---|---|
| Extremo SplineV2 restaurado | d1EndpointRestoresHistoricalAuthorityWithoutRelaxingH1; before/ | R6 UNIQUE, H1 sigue COMPONENT_NOT_ESTABLISHED, herramienta R1, P=(2,0); XML intacto hasta confirmar. |
| h/ReferenceR1 controlado | uniqueLocalRequiresConfirmationAndQDoesNotFollow | R6 unresolved/global uniqueness; ruta H1, confirmación requerida, Q sin nuevo ID/padre. |
| Archivo nativo exacto h | Cu7M08SDesktopD1NativeProbeTest optativa | Hash original, programa real, evidencia numérica, oferta H1; .cedg/reapertura/copia. No reemplazar por sintético. |
| Múltiples históricos | d1MultipleSplineCandidatesRequireExplicitChoice; multipleHistoricalAddressesHaveNoDefaultChoice | R1 MULTIPLE, sin selección por defecto; dirección confirmada coincide con forward. |
| Seam con autoridad R1 | d1HistoricalSeamAdmissionIsNotOverriddenByH1Rejection | H1 rechaza; R1 MULTIPLE se conserva, sin consultar H1 en herramienta. |
| Fallos terminales R1 | d1HistoricalBudgetAndAbsenceAreTerminalWithoutH1Query | Presupuesto/ausencia, sin segunda consulta ni mutación. |
| Rechazo H1 sin admisión R1 | d1H1ExhaustionWithNoHistoricalAdmissionStillRejects | R1 unresolved; presupuesto H1 insuficiente tras discovery; sin oferta/publicación; XML idéntico. |
| No mezclar capabilities | d1RoutesCannotExchangeChoicesOrReuseConsumedSelection | FOREIGN_CHOICE y CONSUMED_SELECTION, ninguna ruta publica con oferta ajena. |
| Caducidad/cancelación | staleChoiceRejectsAndCanStartAgain; d1LocalStaleReferenceAndCancellationLeaveNoMutation; switchingModeCancelsPendingCreation | Ambas rutas; XML sin cambios de commit, capability consumida, reinicio posible. |
| L→Q, Q libre/dependiente/antecesor | suite ReferenceTool y R1 Correction | Sin puntos libres accidentales, Q transitorio; sólo influencia legítima por L. |
| Q posterior y L dinámico/dependientes | uniqueLocalRequiresConfirmationAndQDoesNotFollow; sourceAncestorAndItsDependentsPropagateNormally; Correction | P no sigue referencia independiente; sigue forward desde L. |
| Undo/Redo único, ID | referenceToolCreatesOneUndoAndCancellationCreatesNone | Eventos del controlador; un Undo, Redo conserva ID. |
| Extremo nativo/copia/remapeo | d1EndpointNativeReopenAndCopyKeepForwardAddress | Reapertura conserva ID/dirección; copia nuevo ID y soporte remapeado con linaje. |
| Históricas Punto/Punto en objeto/arrastre/avance | G9U1SemanticPointInteractionTest; Cu7M08SCorrectionTest; R6 52; H1 15 | Sin ampliación de arrastre general; criterios kernel inalterados. |
| Compacto/accesibilidad/localización | advancedDialogIsCompactWithoutReferenceTab; perfiles Desktop | Sin pestaña Q ni diálogo habitual; inspección visual/DPI pendiente de autor. |

No se atribuye rollback transaccional nuevo: son operaciones forward existentes.
La comparación de XML prueba ausencia de mutación documental en los rechazos ejercidos,
no una nueva garantía universal sobre efectos externos o reentrada de GeoGebra.

## Smoke autoral D1 — no ejecutado automáticamente

Usar el commit exacto del índice de entrega. Lanzar por la ruta existente
`.\gradlew.bat :desktop:desktop:runGeoCeDG`. Cada ensayo usa copia del modelo;
no sobrescribir C:/Temp/TestM08-S.cedg. Probar Undo inmediatamente tras la creación,
antes de renombrar o cambiar otros objetos (que generan operaciones independientes).

1. Abrir copia del archivo autoral y crear temporalmente
   ReferenceR1=(0.8976484384297626,1.7854818601338847). Mostrar h y ReferenceR1.
2. Activar «Punto sobre Locus V2 desde referencia». Leer radio world .1 y alcance fijo
   todas las ramas/componentes admitidas. Política muestra los campos existentes;
   sin filtrar ramas y sin depender de zoom/DPI para la consulta geométrica.
3. Clic h → clic ReferenceR1. No debe aparecer el diálogo general ni crearse un punto
   libre. El selector compacto indica dirección local verificada y ausencia de unicidad
   global; exige Confirmar incluso con una oferta. Confirmar crea P forward, sin Q padre.
4. Deshacer una vez elimina sólo P; Rehacer lo restaura. En ensayos separados mover Q
   independiente (P no lo sigue), cambiar antecedente de h (P y Z=P+(1,0) se recalculan),
   guardar/reabrir una copia .cedg y copiar P con dependencias: ID/linaje/dirección correctos.
5. Modelo nuevo: S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3), Q=(2.02,0.01).
   Misma herramienta, S → Q, radio .1. Selector R1/R6 UNIQUE_ADMISSIBLE_PREIMAGE,
   cobertura histórica indicada; Confirmar produce extremo (2,0). No debe aparecer el
   rechazo COMPONENT_NOT_ESTABLISHED de H1. Repetir Undo/Redo y Q independiente.
6. Modelo nuevo: S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3), Q=(0,1).
   R1 MULTIPLE: ninguna elección predeterminada, Confirmar inactivo hasta elegir.
   Escoger por rama/componente/parámetro; comprobar dirección persistida. Cancelar no publica.
7. Repetir Escape antes de Q/durante confirmación, cambio de herramienta y reinicio L.
   Cambiar Q/L con selector abierto y confirmar: rechazo caducado sin publicación.
   Repetir varias construcciones; no deben quedar selección/identidades residuales.
8. En Política elegir presupuesto mínimo válido: rechazo BUDGET_EXHAUSTED, cero creación;
   no debe reintentar por otra ruta. Restaurar valores. Probar ausencia certificada de R1
   con Q lejano; no confundirla con unicidad/evidencia no establecida de otras fuentes.
9. Probar Punto, Punto en objeto y arrastre SplineV2; modalidad avanzada de dirección
   explícita compacta, sin pestaña desde Q, claves largas completas y desplazables.
   No exigir arrastre nuevo sobre h. Revisar mensajes EN/ES, foco/teclado y DPI alto.
10. Registrar SHA usado y resultados por caso. No declarar PASS autoral a partir de
    pruebas automáticas. M09 y arrastre general siguen pendientes.
