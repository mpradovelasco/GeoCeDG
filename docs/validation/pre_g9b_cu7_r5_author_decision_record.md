# CU7 R5 — resolución autoral y perímetro del cierre reducido

El autor emitió esta resolución en el chat de trabajo el 2026-11-05
(Europe/Madrid), sobre el candidato técnico `13ee3b8746d4d874824eb44ca1d05a3e57b494c2`
(árbol `a4fead2412bbb8859dd167e3c22b82c54e584633`) y su propuesta descendiente
`91566d0daff274ed85961ac41c593481c02cf21e` (árbol
`8529953c6887b0e1ecd1607f04e83ed1140b2ecc`). `selfApproved=false`.

> 1. **D1:** adoptar la excepción controlada de compatibilidad XML `conicPointState` v1, exclusivamente con las condiciones documentadas y auditadas.
> 2. **U2:** diferir la simplificación de las entradas de la herramienta Punto. Conservar la implementación funcional R5 y registrar la mejora de interfaz como deuda no bloqueante.
> 3. **F8:** mantener diferido y desactivado `LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION`.
> 4. **CU7:** aprobar el cierre reducido, con el alcance, las evidencias, exclusiones y limitaciones especificados en el expediente independiente. El smoke R5 se reconoce como `PASS — AUTHOR REPORTED`.

La resolución autoriza preparar el cierre e integrar en `main`, previa
comprobación de ascendencia, árboles, limpieza operativa, protecciones Git y
verificaciones de cierre. La anotación operativa del cierre requiere confirmar
el SHA y árbol de la promoción remota. Se preservan ramas, evidencias y
diagnósticos. Quedan excluidos nuevos cambios de producto, cambios históricos,
alteraciones contractuales fuera del perímetro, force-push, etiquetas y releases.

## Alcance y evidencia

- El alcance aprobado es la matriz del [expediente independiente](pre_g9b_cu7_r5_reduced_closeout_proposal.md#capacidades-propuestas-para-el-cierre-reducido), con sus guardas, límites y exclusiones. Incluye interacción por clic y Q, continuación certificada y colocación Q nativa dentro de las tablas positivas demostradas.
- D1 incorpora las nueve cláusulas y condiciones auditadas por referencia normativa en la [enmienda de compatibilidad](../../geocedg/specs/units/unit-system.md#83-omission-rule-and-legacy-byte-identity).
- U2 queda como deuda de organización de la herramienta Punto. F8 permanece diferido y desactivado. M08-D, M09/I2, I2G, I3, movilidad universal y familias fuera de las tablas siguen fuera del cierre. El cierre previo de M08-S-R2 conserva su expediente. CU10 y la deuda histórica de tangencias conservan su disposición.
- El [registro de smoke](pre_g9b_cu7_r5_author_smoke_record.md) delimita la evidencia autoral. La [auditoría independiente](pre_g9b_cu7_r5_independent_audit.md) conserva sus condiciones y procedencia. El recibo FINAL `aa8d0c33de7dd092b77f1d2ac5a9a8477ef910cdaa1e0d362fd4b230ce310222` pertenece al SHA técnico exacto. Los diagnósticos, incluido Revision3 por ausencia del fixture, mantienen su clasificación.

## Clasificación de este registro

`VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`.
`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`.
`GUIDE_IMPACT=NONE` porque este registro documenta decisiones sobre el
comportamiento ya verificado de R5. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`
porque no cambian los requisitos ni los procedimientos de instalación.
La validación del delta usa STATIC y comprobación Git. INSPECT autentica el
recibo existente del candidato técnico. La confirmación operativa de publicación
se registra por separado con el resultado remoto.
