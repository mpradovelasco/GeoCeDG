# M08-S-R2 Desktop — conciliación normativa propuesta

**CANDIDATA, NO APLICADA a ADR aceptados ni especificaciones.** selfApproved=false.
Complementa la propuesta de kernel R2 y su contrato H1; no reinterpreta sus evidencias.

## Diferencias propuestas exactas

| Fuente/destino | Precisión a añadir, manteniendo el resto |
|---|---|
| ADR 0019 decisión 10, remisión M08-S | Separar resultado R6 histórico de permiso M08-S. Una dirección individual puede seleccionarse sólo con evidencia forward, componente, proximidad world acotada, regularidad y ausencia de barreras, todo vigente. No exigir mínimo ni completitud global. UNRESOLVED por sí mismo nunca da permiso. No cambiar arrastre general. |
| ADR 0041, M08-S estático | La referencia puede ser Q existente, consultado una vez. Herramienta L→Q consume M08SnapshotPlacement2D; confirmación contextual explícita incluso para oferta única sin unicidad global. Sólo dirección forward/parents legítimos se persisten. Q no es nuevo padre. |
| ADR 0041, política de selección | Alcance fijo visible: todas las ramas/componentes admitidas por proveedor y política, distinto de cobertura global NOT_ESTABLISHED. Configuración de sesión sólo radio/tolerancias/presupuestos existentes; no filtro Desktop ni alcance restringible. |
| locus-v2-point-interaction.md | Documentar modo experimental 145 L→Q, selección de objetos separada de selección de direcciones, cierre de capability por cancelación/modo/documento/política, revalidación al commit, Undo único y diagnóstico tipado. R6 y sus 52 regresiones permanecen. |
| locus-v2-public-surface.md | Registrar consumidor Desktop del puente R2/H1 y dirección avanzada compacta. No comando dinámico Operación(L,Q), codec nuevo ni generalización de proveedores. |
| Especificaciones de parametrización/persistencia | Sin cambio de formato: definición forward canónica y ownership/ID existentes. No persistir Q de referencia, política, nonce, selector ni índice de JList. |

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

> For historical R6 creation, a candidate must be uniquely established under the
> declared bounded interaction contract. M08-S separately permits explicit selection
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

> 3. Establish the current admissible component, canonical reconstructible address,
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
> policy and query-owned choice at commit. Account for the remaining cumulative H1
> budget through revalidation and prepared publication. A stale, foreign, consumed or
> exhausted selection rejects without publication. No ordinal, coordinate or caption
> is an executable semantic selector.

**locus-v2-point-interaction.md — añadir anexo M08-S, sin cambiar R6**:

> The experimental M08-S consumer uses M08SnapshotPlacement2D. Result separates
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
> result and commit with explicit contextual confirmation. Advanced explicit-address
> authoring remains available in a compact dialog. There is no persistent L,Q inverse
> expression, new codec, general point dragging or M09 operation in this increment.

**Nota de sucesión propuesta para ADR 0041/spec M08**: conservar el cierre CU7-D y
su autorización histórica del 29 de octubre; añadir la posterior autorización focal
M08-S-R2/H1/Desktop como sucesión fechada. No transformar autorización de implementación
en aprobación final de producto. La aplicación de todos estos bloques exige decisión
autoral expresa sobre el candidato y sus resultados.
