# PRE-G9B-CU7-D — Cierre de diseño y autorización acotada

| Campo | Valor |
|---|---|
| Estado | **CU7-D — PASS / AUTHOR APPROVED** (diseño, no implementación) |
| Autoridad | decisión humana «CU7-D: aprobación autoral y autorización de CU7-I/I2a+I2b», 2026-10-29 |
| Candidato aprobado | `7e7a90e4e39db04ea49ee9ab1acf69ea34c81080` |
| Árbol aprobado y comprobado | `25e0e2f0a78670c31be39766021d3f7561bc3bf6` |
| Rama de diseño | `phase/pre-g9b-cu7-d-design`; entrada limpia y HEAD exacto |
| Alcance de aprobación | D-CU7-01–12, cinco precisiones, ADR 0041 y especificaciones M08/M09 como normativa prospectiva |
| Implementación autorizada | exclusivamente I2a+I2b, M09 singleton; candidato futuro pendiente de revisión y smoke autorales |
| Autopromoción | `selfApproved=false`; la aprobación procede de la instrucción humana |

## Coherencia normativa comprobada antes de registrar el cierre

Se contrastaron ADR 0041 y M09 con ADR 0026 y durable-dependency-projection §§8–10,
public-surface §20, y los contratos de identidad/evidencia de ADR 0017/0039/0040.
No se detectó contradicción sustantiva pendiente de decisión. La extensión de RETAIN a
transiciones singleton neutrales es una enmienda prospectiva explícita, no una lectura
retroactiva del código existente. Se conserva la distinción entre identidad del objeto P,
definición y token de raíz; no hay rebind, relajación de roles o upgrade de integridad.

La normativa aprobada se encuentra en [ADR 0041](../adr/0041-locus-v2-point-placement-and-point-redefinition.md)
y en [M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md) /
[M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md). Sus rutas conservan el
nombre candidates por trazabilidad; su nuevo estado es normativo prospectivo aprobado.
Las restricciones previas de ADR 0026, durable-frontier y public-surface se amplían solo en
los casos expresos de M09 §2/§10. Los demás predicados, ordinary refresh y fuentes V2 no se amplían.
La evidencia de CU3 y la caracterización CU7-D no se reescriben ni prueban capacidades futuras.

## Autorización de implementación y exclusiones

La base de implementación será descendiente de este cierre documental verificado, nunca
main sin esta normativa. Rama nueva prevista: `phase/pre-g9b-cu7-i2-singleton`.
[I2a/I2b](../architecture/pre_g9b_cu7_i_increment_proposal.md) implementará solo tabla cerrada
singleton VALUE; I2b comienza después de demostrar transacción, rollback y persistencia I2a.
Preparar selección PHASE por el procedimiento canónico; aceptación INTEGRATED_PHASE con
PHASE e INTEGRATION, más STATIC/INFRA_UNIT pertinentes. No se declara smoke manual ejecutado.

Excluidos I1, I2G, I3, I4, I5 global, M09-B, conversiones silenciosas de roles, generalizaciones
estructurales y cambios de autoridad/certificación/STRICT. Peticiones nuevas no soportadas
se rechazan explícitamente sin mutación; rutas antes válidas conservan sus contratos.
**Un candidato I2 satisfactorio es M09 PARCIAL, nunca M09 completo.**

## Verificación documental y conservación

Evidencia del candidato aprobado: STATIC `verification-41534803a6b045869657ee6862420fc7`,
`ACCEPTED / COMPLETE`, exit 0, en
`artifacts/pre-g9b-cu7-d/consolidation-20261029/static/verification-result.json`.
Este cierre se verifica mediante STATIC en un directorio nuevo antes de crear la rama I2;
su identidad/resultados se guardan separadamente, sin atribuirlos al candidato anterior.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: solo estado/documentación, sin cambios de prerequisitos,
runtime, verificador o packaging. GUIDE_IMPACT = NONE: no cambia producto disponible.
Se conservan CU10-GUIDE-INTEGRATION-CHECK, FINAL de CU10, E1 y
DEBT-CU3-EXACT-TANGENCY-GENERALIZATION. Sin main, push, tags, releases o cambios del libro.
