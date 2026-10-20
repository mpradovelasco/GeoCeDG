# PRE-G9B-CU1 — Registro de verificación del candidato correctivo

| Campo | Valor |
|---|---|
| Carácter | Registro documental de la verificación del candidato correctivo de `CU1` (decisión D-CU1-R6). Lo añade un commit documental descendiente, que no modifica el candidato ni es un candidato nuevo |
| Estado | `CU1 CORRECTIVE TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`: `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` son `ACCEPTED / COMPLETE` sobre el candidato; la clase `INTEGRATED_PHASE` está completa; `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; `CU2`–`CU10` no autorizados |
| Candidato | `143f5d3793fc645b7c79503d7717997716637be2`, árbol `4de3cd7150fe0ec12607c1ab2a9697134f9de453`, en la rama local `phase/pre-g9b-cu1-implementation` (sin publicar) |
| Padre | `508412e2f38d744bd2b333acd5754a77b7861e52`, árbol `696c5eb17ddfe8b625434e504e3cdd13934c36b8`: descendiente documental del candidato anterior `8ee196679e0a6ab101eee0a4e60682efe3f11be0` |
| Informes | [informe del candidato](pre_g9b_cu1_implementation_candidate_report.md), [informe correctivo](pre_g9b_cu1_corrective_candidate_report.md) y [registro de verificación del candidato anterior](pre_g9b_cu1_verification_record.md) |

## 1. Identidad y delta

| Comprobación | Resultado |
|---|---|
| Delta padre → candidato | 4 archivos: 1 nuevo y 3 modificados; 154 inserciones y 7 borrados |
| Delta candidato anterior `8ee19667` → candidato | 6 archivos, 415 inserciones y 8 borrados: el registro de verificación anterior, el enlace a él desde el informe del candidato, el informe correctivo, la prueba ISO A, el manifiesto upstream y el roadmap |
| Código de producto | sin cambios respecto de `8ee19667`: ningún archivo bajo `source/` fuera de `src/test` |
| Pruebas | solo `PreG9BR6PlusE3IsoABorderDesktopTest`, escenario `tool` ([informe correctivo](pre_g9b_cu1_corrective_candidate_report.md) §2) |
| Inventario, registro, fijaciones de cobertura | sin cambios: las identidades de las pruebas son las mismas |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` correcto antes del commit: 1 094 archivos registrados |
| Finales de línea | `git diff --cached --check` limpio. El roadmap y el manifiesto conservan sus 2 873 y 139 terminadores CRLF; sus líneas nuevas o editadas usan LF |
| Árbol de trabajo e índice | limpios antes y después de cada ejecución de aceptación |
| Publicación | `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`; ninguna rama de `CU1` existe en el remoto |

## 2. Verificación de aceptación sobre el candidato

La clase autorizada es `INTEGRATED_PHASE`. Las ejecuciones fueron secuenciales, desde la raíz
del repositorio y con el candidato como `HEAD`, en directorios de registro nuevos:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu1-r6-static-143f5d37
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu1-r6-infra-unit-143f5d37
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu1-r6-phase-pre-g9b-cu1-143f5d37
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu1-r6-phase-pre-g9b-cu0-143f5d37
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu1-r6-integration-143f5d37
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Las cinco terminaron con código de salida 0. Los `result_hash` se autenticaron con
`Read-VerificationJson` y `Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-842a984fd13745b2a401396b55e42a7d` | `446832167e558f549091b6b4fa9e79a4b23f84c985aec6cfb487fcc9ad9b9d0f` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-1bc73db300fe4fd0ba85054d79561554` | `f9eda832c7dae76742747cbfd651813c9c40224123b1f474f8b9aa7ee7feca0c` | `830887b1…ce1d` | 2,1 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU1` | `verification-9acf4bcb65254f0389e197dff95cc178` | `f04f0a5cd9470009c75ac4af803350ce1cdfc893475e040958a5f3d7dd6251f8` | `b0e4f130…4519` | 5,2 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU0` | `verification-53c51a5f709b4d6393706b9721c8b022` | `e9d0ec1031b8ef52abae175c0ad0a894cd2b122be1718b69ee45ccdc7022e210` | `43e3e97e…9173` | 2,4 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas; 0 fallos, 0 omitidas |
| `INTEGRATION` | `verification-22c2ccd338434539baa10311565a0d47` | `aa6ebf5af42c0e87c34bb0c0558adf6634a351acf9967ceb23370fdb18abc32d` | `4502c4bd…7aea` | 32,6 min | `ACCEPTED / COMPLETE`, 19/19; solo diagnósticos permanentes |

Los planes son los mismos que en las ejecuciones del candidato anterior `8ee19667`.

Detalle de `INTEGRATION` (2026-10-20, 12:27–12:59 UTC):

- `junit.desktop.final`: 180 clases, 2 179 pruebas, 0 fallos y 1 omitida, la upstream
  `org.geogebra.CasValueExtractor.extractMockValues`. `PreG9BR6PlusE3IsoABorderDesktopTest`
  pasa sus 13 pruebas, incluida `toolCreatesOneSheetWithOneUndoPoint`.
- `junit.desktop.g9u1-isolated`: 2 clases y 5 pruebas, sin fallos.
- `junit.shared.final`: 703 clases, 7 015 pruebas, 0 fallos y las mismas 10 omitidas upstream.
- `compile.shared` y `compile.desktop`, las siete hojas Python, `static.scientific-inputs`,
  `workspace-profile.product`, `packaging.product`, `packaging.repository-safety`,
  `packaging.toolchain-safety`, `repository.boundary-safety` y `workstation.live-safety`:
  `CONTRACT_SATISFIED`.

Diagnósticos de `STATIC` e `INTEGRATION`: `diagnostic.governance` = `DIAGNOSTIC_FINDING` y
`diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`, las notas permanentes del
protocolo temporal de recuperación de la infraestructura de verificación. Los demás diagnósticos
de `INTEGRATION`, incluidos los cuatro de Checkstyle, `diagnostic.style`,
`diagnostic.documentation`, `junit.desktop.revision3-diagnostic` y
`repository.whitespace-diagnostic`, son `DIAGNOSTIC_CLEAR`.

Los registros están en `artifacts/agent/pre-g9b-cu1-r6-*-143f5d37/`, que Git ignora. Los de las
dos ejecuciones rechazadas del candidato anterior se conservan en
`artifacts/agent/pre-g9b-cu1-integration-8ee19667/` y
`artifacts/agent/pre-g9b-cu1-run2-integration-8ee19667/`.

## 3. Lo que no se ha ejecutado

- `FINAL`: la clase autorizada no lo exige y el plan lo reserva para `CU10`. No hay recibo de
  verificación.
- Prueba de humo autoral: corresponde a la revisión autoral (D-CU1-R1).
- Las ejecuciones de §2 no reutilizan ningún resultado del candidato `8ee19667` ni de las
  ejecuciones de desarrollo.

## 4. Contenido de este commit documental

Este commit:

- añade este registro;
- enlaza el registro desde el informe correctivo;
- registra la identidad del candidato y el resultado de su verificación en el roadmap (filas
  «Última fase ejecutada» y «Siguiente puerta» y sección «Track PRE-G9B-CU»).

No modifica código, pruebas, registro, inventario ni evidencias, y no cambia ningún registro de
cierre.

## 5. Estado de autorización

La decisión autoral D-CU1-R6 («FIX TEST, RE-VERIFY») está ejecutada: la prueba se corrigió sin
cambios de producto y el candidato correctivo `143f5d37` completa la verificación
`INTEGRATED_PHASE`. Se presenta para revisión autoral junto con este descendiente documental.
Siguen pendientes las decisiones D-CU1-R1 a D-CU1-R5 de §12 del informe del candidato. No se
declara `PASS — AUTHOR APPROVED`. `CU2`–`CU10`, la publicación en `main`, las etiquetas, los
releases, los binarios y los cambios del libro siguen sin autorizar.
