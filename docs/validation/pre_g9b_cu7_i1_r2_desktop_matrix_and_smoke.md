# M08-S-R2 Desktop — matriz y smoke autoral

Candidato pendiente; selfApproved=false. Resultados automáticos exactos en
artifacts/pre-g9b-cu7-i1-r2-desktop/delivery.json. Este protocolo NO declara smoke ejecutado.

## Matriz de obligaciones y oráculos

| Obligación | Evidencia prevista sobre Java real | Límite |
|---|---|---|
| L→Q, vacío/Q antes de L, cancelación | Cu7M08SReferenceToolTest; XML idéntico, capability consumida | estado y Swing, no inspección visual |
| Evento gráfico L→Q y Undo único | Cu7M08SCorrectionTest.referenceToolCreatesOneUndoAndCancellationCreatesNone | press/release reales en controlador; confirmación por método |
| Oferta única sin autoaceptar | oneChoiceIsOnlyPublishedAfterContextualConfirmation | botón contextual; segundo clic antiguo no publica |
| Varias ofertas sin elección por defecto | multipleLocalAddressesHaveNoDefaultChoice | objeto Choice real y botón; lista efímera |
| Q libre/dependiente | pruebas ReferenceToolTest y regresiones R1 | sin padre directo, sin ID añadido a Q |
| Q antecesor de L | sourceAncestorAndItsDependentsPropagateNormally | conserva sólo influencia legítima por L |
| L cambia, dependientes de P | prueba anterior y R1 sourceChangesPropagateButReferenceChangesDoNot | no drag general |
| Q cambia después | uniqueLocalRequiresConfirmationAndQDoesNotFollow; nativo | coordenadas P invariantes si Q no influye en L |
| Selección caducada/consumida | staleChoiceRejectsAndCanStartAgain; doble botón; H1 | XML y capability, no mutación |
| Presupuesto | budgetExhaustionAndEscapeCancelWithoutMutation; H1 15 y sonda optativa | límites semánticos/intervalares H1 intactos |
| .cedg y copia/linaje | Cu7M08SDesktopNativeProbeTest | original nativo hash fijado; copias temporales |
| SplineV2, modo Punto y arrastre existente | G9U1SemanticPointInteractionTest (20), R1 (9), R6 (52) | rutas históricas conservadas |
| Diálogo avanzado compacto | advancedDialogIsCompactWithoutReferenceTab | tamaño preferido/campos completos; DPI requiere smoke |
| Perfil, toolbar/menu, localización | GeoCeDGProfileTest, G9U1ProfileCompilerTest y catálogo integrado | conteo nuevo 134/75; no fallback silencioso |
| Caso nativo autoral | sonda optativa Cu7M08SDesktopNativeProbeTest | separada del fixture sintético y de acceptance PHASE |

Pruebas nuevas de componente buscan el botón Confirmar por texto, no el primer JButton
recursivo: JScrollPane contiene flechas JButton. Se conserva el fallo de ese arnés en
focal-complete; no se atribuye a producto. Los estados tipados y códigos no se ocultan.

## Smoke autoral — no ejecutado automáticamente

1. Desde la raíz del candidato ejecutar `.\gradlew.bat :desktop:desktop:runGeoCeDG`
   (ruta existente de la guía Developer), con perfil GeoCeDG y cedg.locus.v2 habilitado.
   Trabajar sobre copia de C:/Temp/TestM08-S.cedg; no sobrescribir el original.
2. Crear temporalmente ReferenceR1=(0.8976484384297626,1.7854818601338847).
   Es el Q de este ensayo; el original no contiene Q. Mostrarlo y mostrar h si es necesario.
3. Activar «Punto sobre Locus V2 desde referencia» en herramientas de curvas semánticas.
   Leer radio .1 world y alcance fijo todas las ramas/componentes admitidas; comprobar
   indicación separada de cobertura global no establecida. Abrir Política sólo para
   inspeccionar tolerancias/presupuestos existentes; cancelar sin cambiar documento.
4. Clic gráfico en h; después clic en ReferenceR1. No debe aparecer diálogo general de
   parámetros ni crearse/moverse un punto libre al seleccionar Q. El selector compacto
   muestra rama/componente/u; incluso una sola dirección requiere Confirmar. No afirma
   proyección, mínimo ni unicidad global. Confirmar y nombrar P para inspección.
5. Mover ReferenceR1: P permanece en su dirección si Q no participa en h. Crear un
   dependiente Z=P+(1,0). Cambiar un antecedente válido de h y comprobar recomputación
   forward de P y Z; conservar dirección semántica conforme al contrato. No exigir
   arrastrar P sobre Locus V2 general: sigue fuera del alcance.
6. Una sola operación Deshacer debe retirar P y Rehacer restaurarlo con dirección/ID.
   Guardar con nombre distinto .cedg, cerrar y reabrir. Comprobar definición forward,
   mismo ID y ausencia de ReferenceR1 como nuevo padre directo. Copiar P con dependencias
   mediante ruta nativa y comprobar ID distinto/linaje, sin padre Q artificial.
7. Repetir varias creaciones L→Q. Escape antes de Q, durante confirmación, cambio de
   herramienta y clic vacío deben cancelar sin objetos/Undo. Tras cambiar Q o L mientras
   el selector está abierto, confirmar debe rechazar selección caducada sin publicar.
8. En modelo controlado con dos direcciones admisibles, comprobar lista sin selección
   predeterminada y Confirmar deshabilitado hasta elección. Confirmar la dirección elegida
   por su información semántica. Cancelar no modifica documento.
9. Con presupuesto mínimo válido, comprobar BUDGET_EXHAUSTED y cero publicación. Restaurar
   valores iniciales. Con radio insuficiente no interpretar falta de oferta como ausencia
   certificada; observar causa tipada y volver a seleccionar.
10. Sobre SplineV2, verificar modo Punto, Punto en objeto y arrastre semántico previos.
    Abrir modalidad avanzada de dirección explícita; verificar campos compactos, claves
    largas completas y desplazables, entrada inválida localizada, Cancelar sin mutación.
11. Repetir controles, teclado/Escape y lectura EN/ES en pantalla pequeña y DPI alto.
    Registrar dimensiones y problemas de foco/solapamiento. No convertir resultados
    automáticos en PASS autoral. Comunicar commit exacto usado y cada observación.

Las rutas avanzadas y legacy permanecen; esta herramienta añade selección gráfica explícita
de Q y admite ofertas locales H1 sin reinterpretar UNRESOLVED. M09 sigue pendiente.
