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
