# PRE-G9B-CU10 — Registro de cierre autoral excepcional

11 de noviembre de 2026. El autor, mediante «Decisión autoral extraordinaria:
corrección focal y cierre sin nueva campaña», acepta la evidencia científica,
funcional y de empaquetado dentro de sus límites demostrados. Autoriza las
correcciones focales y el cierre por excepción expresa a la puerta técnica
de `FINAL`. La dispensa se circunscribe a CU10. `selfApproved=false`.

**PRE-G9B-CU10 — PASS — AUTHOR APPROVED WITH EXPLICIT FINAL VERIFICATION EXCEPTION**

**PRE-G9B-CU — CLOSED — AUTHOR APPROVED WITH EXPLICIT FINAL VERIFICATION EXCEPTION**

**PUBLICATION — PENDING EXACT-COMMIT AUTHORIZATION**

## Identidades y resultado histórico

| Hito | Commit | Árbol |
|---|---|---|
| Baseline publicado, cierre CU9 | `b6a86bf00dfb691c82e6009dfed1a9a0c20a865a` | `8ebaeb3a545a7bdb435f72ea966fcd7f89594888` |
| Candidato técnico ejecutado | `20ae57aa484fc0d60c65de76a23151d4d2a758c1` | `0c0516bbb0f507125683a82433277b10f823346a` |
| Descendiente documental histórico | `9f97e089e9c791aa938277282df72406e415b285` | `eb5f8121ff5f0e673eed7ec9e95254e6e80ab9b4` |
| Corrección y cierre excepcional | Commit que incorpora este registro, hijo del descendiente documental | Árbol de ese commit |

La campaña histórica `tools/agent/verify.ps1 -Profile FINAL` terminó con
código 3. Ejecutó 68/68 nodos y 17/17 productores terminaron con código 0.
Pasaron 39/41 controles obligatorios, con semántica 15/15, seguridad 4/4 e
infraestructura de verificación 20/22. Las hojas `infra.final-coverage` e
`infra.static-semantic` terminaron con código 1. El verificador conserva
su resultado rechazado y carece de recibo de aceptación.
[Informe histórico](../../artifacts/pre-g9b-cu10/final/verification-result.json) ·
[Expediente de evidencia y limitaciones](pre_g9b_cu10_integrated_candidate.md).

```text
AUTHOR_DECISION = APPROVE_AND_CLOSE
EXPLICIT_FINAL_VERIFICATION_EXCEPTION = AUTHORIZED
FINAL_HISTORICAL_RESULT = REJECTED_VERIFICATION_CORE / COMPLETE
FINAL_HISTORICAL_CHECKS = 39/41
FINAL_ACCEPTANCE_RECEIPT = NONE
NEW_VERIFICATION_CAMPAIGN = NOT EXECUTED — AUTHOR DECISION
PRODUCT_SEMANTICS_CHANGED = false
SCIENTIFIC_ALGORITHMS_CHANGED = false
SOURCE_CORRECTIONS_POST_FINAL = NOT REVERIFIED
```

El cierre se acredita en este registro de gobernanza y en su
[espejo estructurado](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu10-closeout.json).
El consumidor automatizado mantiene su exigencia de recibo y esa exigencia
sigue incumplida. La reconciliación §11.3 es inaplicable porque la cobertura
histórica fue `COMPLETE` y fallaron dos controles de infraestructura.
[Rutas ordinarias de aceptación y reparación](../../geocedg/specs/operations/verification-levels.md#11-verification-input-identities-and-evidence-preserving-repair).

## Correcciones focales y preparación futura

1. En las aserciones de cobertura, las selecciones JUnit pasan de 85 a 92,
   los registros PHASE de 82 a 87 y los casos de
   `pre-g9b-r6-plus-f4-a.desktop` de 159 a 161. Las cifras proceden del
   inventario versionado. [Prueba de cobertura](../../tools/agent/tests/verification-final-coverage.Tests.ps1).
2. En `workspace.profile-authority` se actualiza la huella canónica de
   `tools/agent/workspace-profile-validation.ps1` a
   `ff6773feee8c47e8c2c3a1945d2d73b38c1b19c1b65cc4cceb7e3f7062ce974e`.
   El autor autorizó adicionalmente actualizar la huella de
   `tools/release/build-windows-package.ps1` en `packaging.source-authority`
   tras el cambio del generador. Se calculó la huella del catálogo
   resultante para registrarla. El espejo conserva las huellas completas.
   [Catálogo](../../geocedg/specs/operations/verification-static-contracts.json) ·
   [Registro](../../geocedg/specs/operations/verification-registry.json).
3. En el generador de `build-manifest.json`, el campo informativo
   `file_association.description` usa `$SelectedAssociationDescription`.
   Los valores efectivos son `GeoCeDG document (internal evaluation)`
   para INTERNAL y `GeoCeDG document` para NC.
   [Generador Windows](../../tools/release/build-windows-package.ps1).

Antes de congelar futuros candidatos destinados a `FINAL`, deben
completarse satisfactoriamente `INFRA_UNIT` y `STATIC`, además de
`WORKSTATION` y `FINAL -PlanOnly` cuando correspondan. La preparación
revisa la vigencia de catálogos e inventarios derivados. La regla es
prospectiva y conserva el perfil `FINAL` y sus criterios de aceptación.
[Manual operativo](../developer/geocedg_operations_manual.md) ·
[Norma de verificación](../../geocedg/specs/operations/verification-levels.md#current-contract) ·
[Plantilla de tareas](../../.github/prompts/tasks/task-template.prompt.md).

## Empaquetado, transferencia y limitaciones

Los artefactos INTERNAL y NC del candidato técnico incluyeron app-image,
ZIP, MSI y EXE. Ambas verificaciones históricas `PACKAGING` terminaron
con código 0, `ACCEPTED / COMPLETE` y 3/3 controles. Las cuatro pruebas
aisladas de instalación y desinstalación terminaron con código 0 en ambos
pasos. Los instaladores mantuvieron la asociación `.cedg` con su
descripción de perfil y excluyeron la asociación `.ggb`. COMMERCIAL
falló de forma cerrada antes de generar artefactos. El manifiesto NC
histórico conserva la descripción INTERNAL. La corrección del generador
queda aplicada en fuente sin ejecución posterior ni regeneración.
[Evidencia de distribución Windows](pre_g9b_cu10_integrated_candidate.md#distribución-windows).

Se mantienen las limitaciones aceptadas de CU1–CU9 y las deudas
registradas, incluidas la generalización de tangencias certificadas,
las ampliaciones de CU7 fuera de su cierre reducido y la caracterización
de `OBS-CU10-B01-LAYER-PROFILE`. La evidencia del perfil limpio conserva
su alcance. El smoke del agente está documentado. El smoke del autor
sigue pendiente y el gesto Ctrl+clic de memoria de referencia conserva
solo su evidencia automatizada. Se mantienen los dos worktrees de CU7
y los trabajos pendientes. [Matriz de compromisos y deudas](pre_g9b_cu10_commitments_and_debt.md) ·
[Dossier CU10-B](pre_g9b_cu10_model_validation.md) ·
[Smoke y worktrees](pre_g9b_cu10_integrated_candidate.md#smoke-revisión-independiente-y-worktrees).

El autor recibe E1, `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF`, como material
preparado para la siguiente fase editorial. Cualquier intervención en
`geocedg_book` requiere autorización propia. Se conserva la versión
`1.1.0` del `2026-10-15`. Publicación en `main`, etiquetas y releases
siguen sujetos a sus autorizaciones específicas.
[Expediente E1](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md) ·
[Política de publicación](../../AGENTS.md#81-geocedg-publication-policy).

## Capa y alcance de evidencia

El delta afecta infraestructura de verificación, metadatos de
empaquetado y documentación de gobernanza. La regla de preparación usa
perfiles y prerrequisitos existentes. La guía de usuario conserva su
evidencia histórica. El cierre toma como autoridades `AGENTS.md`, el
contrato de verificación, sus inventarios y registro, el manual operativo,
el contrato Windows, la matriz, el roadmap y el plan correctivo.

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = La preparación usa perfiles y prerrequisitos existentes.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_operations_manual.md
              .github/prompts/tasks/task-template.prompt.md
```
