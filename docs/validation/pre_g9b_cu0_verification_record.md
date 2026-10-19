# PRE-G9B-CU0 — Registro de verificación del candidato técnico

| Campo | Valor |
|---|---|
| Carácter | Registro documental de la verificación del candidato técnico `CU0`. Lo añade un commit documental descendiente, que no modifica el candidato ni es un candidato nuevo |
| Estado | `CU0 TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `CU1`–`CU10` no autorizados |
| Candidato | `636a0070b6f4e32a8f270c25cfa70d9bd3b284b7`, árbol `8fac2cae5c799fe7da215641c2a94ae95044a84d`, en la rama local `phase/pre-g9b-cu0-candidate` (sin publicar) |
| Base autorizada | `444ad46b1c18a6101dae68a75a8d7a5f92ed83fe`, árbol `1aff549ccf3c0deff5581ee7942d566bd5cc4e19`: padre directo del candidato. La rama `phase/pre-g9b-cu-plan-candidate` sigue apuntando a ella |
| Informe | [informe del candidato](pre_g9b_cu0_baseline_candidate_report.md) |

## 1. Identidad y delta

| Comprobación | Resultado |
|---|---|
| Delta base → candidato | 36 archivos: 29 nuevos y 7 modificados; 10 866 inserciones y 21 borrados |
| Código de producto | ningún cambio fuera de `src/test`. Hay 9 archivos de prueba nuevos en `source/desktop/desktop/src/test/java/org/geocedg/desktop/`, registrados en `docs/upstream/modified-files.yml` |
| Libro, especificaciones, ADR, prompts | sin cambios |
| Corpus | los blobs de `InterCilConoOblique.cedg` y `InterCilConoObliqueTest.ggb` coinciden con los SHA-256 del manifiesto (`1ae6f512…456c`, `88779d58…0398`); la línea base `4cf872df…ba1d` es idéntica byte a byte a la salida del arnés |
| Árbol de trabajo e índice | limpios antes y después de cada ejecución de aceptación |
| Publicación | `main` = `origin/main` = remoto `main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`; ninguna de las dos ramas de fase existe en el remoto |

## 2. Evidencia previa al commit

### 2.1 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol de trabajo. Las fuentes de prueba eran ya las del candidato.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0; recuento sin cambio | 1,8 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0; 2180 identidades | 1,4 min |
| `pre-g9b-cu0.desktop` | ejecución | 6 clases, 21 pruebas, 0 fallos, 0 omitidas | 2,2 min |
| `final.desktop` | ejecución | 179 clases, 2174 pruebas, 0 fallos, 1 omitida | 17,0 min |

La omitida es la prueba upstream `org.geogebra.CasValueExtractor.extractMockValues`, que es
preexistente y también aparece en el `FINAL` de `PRE-G9B-R7` (Desktop: 2153 pruebas, 1
omitida).

El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` se ejecutó en la
sesión con las dos evidencias de descubrimiento y las dos de ejecución. Recuentos:

- `discovery.desktop`: 2159 → 2180;
- `final.desktop`: 2153 → 2174;
- `pre-g9b-cu0.desktop`: 21 (selección nueva);
- `input_identities` de la selección nueva: la base autorizada, como en las selecciones de fase anteriores.

La huella `junit_inventory` del registro cambia de `d1cfacfbfd53df712312878d62af4586783629e61ba7720eb7aee484b86533ac`
a `963f5c9cce4a9e9c4e6501d97372e8b47d3e68ab7a0bb0276bf3dabc900bd507`. La huella anterior se
reprodujo antes desde el blob de la base con `Get-VerificationCanonicalTextSha256`.

### 2.2 Ejecución de desarrollo

Se ejecutó `INFRA_UNIT` sobre el índice preparado: `verification-b0ed03f6707742c797d548841069334b`,
`ACCEPTED / COMPLETE`, 22/22 y plan `830887b1…ce1d`, igual al de la ejecución de aceptación. El
verificador la liga a la identidad de `HEAD` (la base). Es evidencia de contenido, no de aceptación
del candidato.

## 3. Verificación de aceptación sobre el candidato

La clase de verificación propuesta es `INTEGRATED_PHASE`: `PHASE` propio más `INTEGRATION`. Se
añadió `INFRA_UNIT` porque el candidato modifica el registro, el inventario y una suite de
infraestructura, y `STATIC` como validación estática del árbol. Las ejecuciones fueron
secuenciales, desde la raíz del repositorio y con el candidato como `HEAD`:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu0-static-636a0070
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu0-infra-unit-636a0070
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu0-phase-636a0070
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu0-integration-636a0070
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Las cuatro terminaron con código de salida 0. Los `result_hash` se autenticaron con
`Read-VerificationJson` y `Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-3ebfbcdd2be443da830df7036e1f1d84` | `c58fe1613e755818ea5e700963cc10d263338059b9bed1ae569d36f35f6f03bf` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-0ee52198f76e4633b2405ee4dfb9bb50` | `0b678cd3ec7fd8b3bc84e3a95c435e73bbd9b047f6a1b5d42945fff35808906d` | `830887b1…ce1d` | 2,1 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU0` | `verification-2567deb5439140978837d7be1b9d6f46` | `61204b2fcfac3b1a27efa87a2eb1efa762f7b7c19eb789cf31feb4d4cd2d9334` | `43e3e97e…9173` | 3,3 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases, 21 pruebas, 0 fallos, 0 omitidas |
| `INTEGRATION` | `verification-439437f91e4a4d358e0825c5ad1880c9` | `1a3816eede3fecbd02895183e082b29fa37d72bb0811dcf76259504aec482dfc` | `4502c4bd…7aea` | 42,8 min | `ACCEPTED / COMPLETE`, 19/19; solo diagnósticos permanentes |

Detalle de `INTEGRATION` (2026-10-19, 18:59–19:41 UTC):

- `junit.desktop.final`: 179 clases, 2174 pruebas, 0 fallos y 1 omitida (la upstream de §2.1).
- `junit.desktop.g9u1-isolated`: 2 clases y 5 pruebas, sin fallos.
- `junit.shared.final`: 700 clases, 6992 pruebas, 0 fallos y 10 omitidas, iguales que en el `FINAL` de `PRE-G9B-R7`.
- `compile.shared` y `compile.desktop`, las siete hojas Python, `static.scientific-inputs`,
  `workspace-profile.product`, `packaging.product`, `packaging.repository-safety`,
  `packaging.toolchain-safety`, `repository.boundary-safety` y `workstation.live-safety`:
  `CONTRACT_SATISFIED`.

Diagnósticos de `STATIC` e `INTEGRATION`: `diagnostic.governance` = `DIAGNOSTIC_FINDING` y
`diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`. Son las notas permanentes del
protocolo temporal de recuperación de la infraestructura de verificación y aparecen igual en
todas las ejecuciones anteriores (por ejemplo, en `STATIC` `verification-32aafdd48c0e46fbb9b286d75b128baf`
de `PRE-G9B-R7`). Los demás diagnósticos, incluidos `repository.whitespace-diagnostic`,
`diagnostic.documentation` y `diagnostic.guide-structure`, son `DIAGNOSTIC_CLEAR`.

Los registros están en `artifacts/agent/pre-g9b-cu0-{static,infra-unit,phase,integration}-636a0070/`,
que Git ignora.

## 4. Lo que no se ha ejecutado

- `FINAL`: la clase propuesta no lo exige y el plan lo reserva para `CU10`. Por eso no hay
  recibo de verificación.
- Ejecutable empaquetado y prueba de humo autoral: el candidato no cambia el producto. El
  escenario ventaneado del informe usa el `main` del producto con su JDK 25, no el ejecutable
  empaquetado.
- Ningún perfil cubre el arnés `tools/benchmark/pre-g9b-cu/`, que está fuera de `source/` y del
  verificador. Su reproducibilidad está documentada en el informe del candidato (§3.3, §3.4 y §4.2).

## 5. Limitaciones

Son las de §9 del informe del candidato. Además:

1. La evidencia del inventario (§2.1) se produjo antes del commit, sobre el árbol de trabajo
   con las mismas fuentes de prueba. Las ejecuciones de aceptación (§3) son las únicas ligadas
   a la identidad del candidato.
2. Los tiempos son de una sola estación y no constituyen puertas.

## 6. Contenido de este commit documental

Este commit:

- añade este registro;
- enlaza el registro desde el informe del candidato;
- registra la identidad del candidato y el resultado de su verificación en el roadmap (fila «Última fase ejecutada» y sección «Track PRE-G9B-CU»);
- concreta la opción de base de la decisión C1-0 en la [hoja de decisiones](pre_g9b_cu0_author_decision_sheet.md).

No modifica código, pruebas, registro, inventario ni evidencias, y no cambia ningún registro de cierre.

## 7. Estado de autorización

`CU0` se ha ejecutado según la autorización autoral de 2026-10-19 y está pendiente de revisión
autoral. No se declara `PASS — AUTHOR APPROVED`. `CU1`–`CU10` no están autorizados. No hay
publicación, etiqueta ni cambio de `main`, y el libro no ha cambiado.
