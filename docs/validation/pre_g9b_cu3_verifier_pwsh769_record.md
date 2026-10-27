# PRE-G9B-CU3 — Incorporación de la corrección del verificador para PowerShell 7.6.9

| Campo | Valor |
|---|---|
| Carácter | Registro de mantenimiento de la infraestructura de verificación. No cambia ningún archivo de producto ni ningún criterio de aceptación |
| Autoridad | instrucción autoral de 2026-10-27, §4 («AUTORIZADA la incorporación de la corrección de compatibilidad del verificador con PowerShell») |
| Deuda | `DEBT-VERIFIER-PWSH-769-TESTJSON`: **CLOSED** |

**Corrección.**

- **Origen:** commit `08776f45` de la rama local `maintenance/pre-g9b-cu3-verifier-pwsh769`.
- **Incorporación:** commit propio `48f0d2ce` (`cherry-pick -x`) en la rama
  `phase/pre-g9b-cu3-implementation`, después del candidato y del expediente de cierre de `CU3-A`, sin mezclarse
  con cambios geométricos.
- **Qué cambia.** `Assert-VerificationJsonSchema` da a todo rechazo de esquema, lanzado o devuelto
  por `Test-Json`, la identidad estable `VerificationJsonSchemaRejected`, con categoría
  `InvalidData`. Los errores de sintaxis o de esquema inválido se propagan sin cambios.
- **Qué no cambia:** ningún esquema JSON ni criterio de aceptación.

El diagnóstico completo está en el [registro de estabilización](pre_g9b_cu3_a_stabilization_record.md) §4.

**Versiones verificadas.** Son las dos instaladas en la estación; no se afirma compatibilidad con
ninguna otra.

| PowerShell | `INFRA_UNIT` sobre `48f0d2ce` |
|---|---|
| 7.6.9 (instalación del sistema) | `verification-b238b72f4ede404fbf7a239597bae8b6` `ACCEPTED / COMPLETE` 22/22 |
| 7.6.6 (aislado, oficial firmado) | `verification-80fe5a2ceb294b64bed8f672990105d3` `ACCEPTED / COMPLETE` 22/22 |

**Pruebas.** Antes de incorporarla, la corrección ya pasaba en su rama `INFRA_UNIT` (22/22) con 7.6.9
(`verification-b3d0b149…`) y con 7.6.6 (`verification-893ea96d…`). Las suites
`verification-registry.Tests.ps1` (22 casos) y `verification-report.Tests.ps1` (9 casos, uno nuevo)
pasan en las dos versiones.

**Toolchain y packaging:** sin cambios.
