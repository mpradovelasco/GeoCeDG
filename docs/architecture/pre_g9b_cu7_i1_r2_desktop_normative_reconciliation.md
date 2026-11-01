# M08-S-R2 Desktop — conciliación normativa propuesta

**CANDIDATA, NO APLICADA a ADR aceptados ni especificaciones.** selfApproved=false.
Complementa la propuesta de kernel R2 y su contrato H1; no reinterpreta sus evidencias.

## Diferencias propuestas exactas

| Fuente/destino | Precisión a añadir, manteniendo el resto |
|---|---|
| ADR 0019 decisión 10, remisión M08-S | Separar resultado R6 histórico de permiso M08-S. Una dirección individual puede seleccionarse sólo con evidencia forward, componente, proximidad world acotada, regularidad y ausencia de barreras, todo vigente. No exigir mínimo ni completitud global. UNRESOLVED por sí mismo nunca da permiso. No cambiar arrastre general. |
| ADR 0041, M08-S estático | La referencia puede ser Q existente, consultado una vez. Herramienta L→Q conserva primero las admisiones R1/R6 por LocusSnapshotPlacement2D y consume M08SnapshotPlacement2D sólo en el fallback tipado; confirmación contextual explícita incluso para oferta única sin unicidad global. Sólo dirección forward/parents legítimos se persisten. Q no es nuevo padre. |
| ADR 0041, política de selección | Alcance fijo visible: todas las ramas/componentes admitidas por proveedor y política, distinto de cobertura global NOT_ESTABLISHED. Configuración de sesión sólo radio/tolerancias/presupuestos existentes; no filtro Desktop ni alcance restringible. |
| locus-v2-point-interaction.md | Documentar modo experimental 145 L→Q, selección de objetos separada de selección de direcciones, cierre de capability por cancelación/modo/documento/política, revalidación al commit, Undo único y diagnóstico tipado. R6 y sus 52 regresiones permanecen. |
| locus-v2-public-surface.md | Registrar consumidor Desktop del puente R2/H1 y dirección avanzada compacta. No comando dinámico Operación(L,Q), codec nuevo ni generalización de proveedores. |
| Especificaciones de parametrización/persistencia | Sin cambio de formato: definición forward canónica y ownership/ID existentes. No persistir Q de referencia, política, nonce, selector visual/capability efímera de elección ni índice de JList. El selector semántico estructural de componente sí pertenece a la dirección durable y se persiste con la construcción forward existente. |

Texto candidato de adición a ADR 0041:

> En M08-S la consulta desde un punto Q existente consume su posición actual como
> valor transitorio. La interacción Desktop es L→Q; la oferta y publicación pertenecen
> al kernel compartido. El permiso individual con cobertura global no establecida
> exige confirmación explícita de una Choice vigente, aunque sólo se ofrezca una.
> No implica proyección exacta, mínimo, unicidad global ni completitud. El alcance
> fijo abarca todas las ramas/componentes admitidas; es visible y distinto de cobertura.
> Los ajustes de sesión reutilizan la política world y contabilidad H1. La definición
> persistente es exclusivamente la construcción forward con sus dependencias legítimas.

No proponer cambios de STRICT, certificación tangencial, autoridad constructiva,
identidad, formatos, M09 o arrastre general. El autor debe decidir la aplicación final
de esta conciliación después de revisión independiente, evidencia y smoke. La restricción
configurable por componentes queda como ampliación futura sin autorización en este incremento.

## Bloques insertables para decisión autoral (no aplicados)

**ADR 0019, decisión 10 — añadir al final**, conservando literalmente la frontera R6:

> M08-S, como incremento independiente de R6, puede aportar un consumidor Desktop
> experimental desde referencia L→Q. Su admisión pertenece a un resultado tipado
> independiente, no a una reinterpretación del resolvedor R6. No incorpora un
> comando inverso público ni amplía la política general de arrastre.

**ADR 0019, Evidence model — sustituir sólo la frase de creación**
«For creation, a candidate must be uniquely established under the declared bounded
interaction contract.» por:

> Historical R6 creation retains UNIQUE_ADMISSIBLE_PREIMAGE and explicit choice
> from MULTIPLE_SEMANTIC_PREIMAGES under its existing bounded interaction contract.
> M08-S separately permits explicit selection
> of an individually admissible semantic address, with current component, canonical
> reconstructible direction, finite forward evaluation, proved world-distance bound,
> provider-supported regularity and no applicable unsafe barrier. The independent
> permission is not implied by UNRESOLVED, a residual, a rendered sample or candidate
> count. Neither a distance minimum nor global search completeness is required for
> that individual selection. Absence of established global uniqueness always requires
> explicit confirmation, even for a single offered address. R6 statuses and evidence
> retain their historical meanings.

**ADR 0041, decisión 3 — añadir precisión específica M08-S**:

> An independent M08-S individual-address proof may grant explicit local selection
> while the historical inverse result remains UNRESOLVED. This is not promotion of
> that historical result. Individual evidence, global coverage and selection permission
> are distinct. The initial perimeter requires the verified component, finite forward
> evaluation, bounded world proximity, regularity and current safe source/policy context;
> it requires neither a local/global minimum nor a global enumeration of nearby addresses.
> Confirmation consumes the exact query-owned choice after revalidation. A failed or
> exhausted proof offers no partial permission. No estimate establishes STRICT.

**cu7-m08-point-placement.md §2 — reemplazar pasos 3 a 5, sin cambiar los otros**:

> 3. Preserve admitted historical R1/R6 selections and their forward commit. Only
> UNRESOLVED with GLOBAL_UNIQUENESS_NOT_ESTABLISHED permits consulting the separate
> R2/H1 proof. For that individual-address route, establish the current admissible
> component, canonical reconstructible address,
> finite forward evaluation, a numerical guarantee for the world-distance bound,
> provider-supported regularity and absence of unsafe barriers. Report their actual
> evidence grade. A distance minimum is not required.
> 4. Preserve R6 status/coverage. In the separate M08-S result, offer only addresses
> whose individual proof completed. When global uniqueness is not established, require
> explicit selection/confirmation, including a single offered address. UNRESOLVED alone
> gives no permission. Search scope is fixed and visible: all provider/policy-admitted
> branches/components. This is distinct from global coverage, which remains not
> established for the individual-address operation. No Desktop postfilter restricts it.
> 5. Revalidate the exact source, definition/revision, component, transient request,
> policy and query-owned choice at the owning route's commit. For R2/H1, account for
> the remaining cumulative H1 budget through revalidation and prepared publication.
> R1/R6 retains its historical budget/forward contract; no H1 guarantee is imputed to it. A stale, foreign, consumed or
> exhausted selection rejects without publication. No ordinal, coordinate or caption
> is an executable semantic selector.

**locus-v2-point-interaction.md — añadir anexo M08-S, sin cambiar R6**:

> The experimental M08-S consumer preserves existing R1/R6 admissions through
> LocusSnapshotPlacement2D. Only historical UNRESOLVED with the typed reason
> GLOBAL_UNIQUENESS_NOT_ESTABLISHED may query M08SnapshotPlacement2D; all other
> non-admitted outcomes are terminal. Each route retains its own candidates, permission,
> revision, coverage and commit. No publication failure triggers a retry. For the R2/H1
> route, its result separates
> historical R6 evidence, GlobalCoverage.NOT_ESTABLISHED, local evidence and Permission;
> only EXPLICIT_LOCAL_SELECTION_REQUIRED permits offering a Choice. Choice is an
> ephemeral single-use query capability, not durable identity. WorkSnapshot distinguishes
> cumulative semantic misses (including nested/composed evaluators), interval passes
> and publication attempts under H1. BUDGET_EXHAUSTED is fail-closed. Desktop only
> selects construction objects, presents evidence and confirms; kernel revalidates and
> publishes the normal forward point. L→Q does not persist Q as a new parent. Policy
> uses the existing world parameters, fixed visible scope and session-only configuration.
> Escape/mode changes invalidate pending UI capabilities. A successful creation stores
> one Undo operation. Existing Point/Point-on-object, SplineV2 and dragging contracts
> remain unchanged.

**locus-v2-public-surface.md — añadir consumidor experimental**:

> Desktop reference placement is a separate experimental mode (145), registered by
> the application profile under cedg.locus.v2. It consumes the shared M08-S-R2/H1
> result and commit after the R1/R6 route described above, with explicit contextual confirmation. Advanced explicit-address
> authoring remains available in a compact dialog. There is no persistent L,Q inverse
> expression, new codec, general point dragging or M09 operation in this increment.

**Nota de sucesión propuesta para ADR 0041/spec M08**: conservar el cierre CU7-D y
su autorización histórica del 29 de octubre; añadir la posterior autorización focal
M08-S-R2/H1/Desktop como sucesión fechada. No transformar autorización de implementación
en aprobación final de producto. La aplicación de todos estos bloques exige decisión
autoral expresa sobre el candidato y sus resultados.

## D1/D2 — precisión focal posterior, candidata

D1 conserva los contratos históricos sin aplicarles el requisito de interior regular
propio de R2/H1. UNIQUE_ADMISSIBLE_PREIMAGE y MULTIPLE_SEMANTIC_PREIMAGES se
consumen exclusivamente por la selección/commit R1/R6. Sólo UNRESOLVED con
GLOBAL_UNIQUENESS_NOT_ESTABLISHED habilita consultar H1 bajo su propio presupuesto
acumulado y pruebas de admisibilidad. No se mezclan ofertas o permisos. No se afirma
un límite acumulado entre ambas consultas independientes ni se modifica la contabilidad
del kernel. La herramienta conserva confirmación compacta para la oferta única R1;
R1 múltiple exige elección y toda oferta R2 exige confirmación incluso siendo única.

**D2 — texto exacto propuesto para la cláusula de persistencia:**

> The visual choice selector and its query-owned capability are ephemeral and are
> not persisted. The structural semantic component selector belongs to the durable
> semantic address and is persisted by the existing forward construction contract.
> A UI caption, list index or transient capability is never that structural selector.

La distinción también se aplica a «selector» en el cuadro anterior: no suprime rama,
componente, parámetro ni su representación canónica persistente. No añade codec ni
cambia documentos existentes. Se modifica únicamente esta conciliación candidata;
los ADR aceptados y especificaciones normativas permanecen intactos.
