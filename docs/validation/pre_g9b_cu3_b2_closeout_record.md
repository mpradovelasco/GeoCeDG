# PRE-G9B-CU3 — Registro de cierre de `CU3-B2`

| Campo | Valor |
|---|---|
| Carácter | Registro de cierre de `CU3-B2`, efectivo por la decisión autoral de 2026-10-28 (§0). Es la única autoridad de ese estado. No cierra `CU3` global ni publica nada |
| Autoridad | decisión autoral «Aprobación de CU3-B2 y preparación del cierre global» (2026-10-28) |
| Producto | `2d13ad4c9b61d6f443b691b8214a8b2361ad0451` (árbol `64dca8e5138050c0e1c5fc6b659dd33ffa430728`) — [registro del candidato](pre_g9b_cu3_b2_candidate_record.md) |
| Estado | `CU3-B2` `PASS — AUTHOR APPROVED`; `AUTHOR SMOKE — PASS`; `CU3` global sin `PASS — AUTHOR APPROVED`; nada publicado |
| Espejo | [`pre-g9b-cu3-b2-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-b2-closeout.json) |

## 0. Decisión autoral

Transcripción de la decisión de 2026-10-28:

- **Smoke.** El autor realizó satisfactoriamente el smoke manual sobre `2d13ad4c` con la guía del
  registro del candidato (§8): `AUTHOR SMOKE — PASS`.
- **Aprobación.** `CU3-B2` `PASS — AUTHOR APPROVED` sobre el candidato productivo `2d13ad4c` (árbol
  `64dca8e5`) y su campaña `INTEGRATED_PHASE` autenticada.
- **Expediente.** Se conservan la ejecución rechazada de `INTEGRATION` (18/19) y la aceptada (19/19),
  ambas sobre el mismo candidato; la incidencia de la barra de herramientas queda caracterizada (§2).
- **Verificaciones.** No se repiten campañas integradas por cambios solo documentales.
- **Publicación.** No se publican ramas, no se modifica `main` y no se crean etiquetas.

## 1. Hechos del cierre

| Hecho | Estado |
|---|---|
| Alcance | tangencias regulares de clase 1 o 2 con las precondiciones P1, solo bajo `constructive-authority/v1`; D-B2-1 a D-B2-7 implementadas |
| Normativa | `locus-v2-intersections.md` versión 1.6, enmienda de ADR 0039 de 2026-10-28 y nota de aplicación de ADR 0017 |
| Puerta K6 | corrección en la causa; [revisión focal](../architecture/pre_g9b_cu3_b2_k6_load_policy_review.md) |
| Corpus y rendimiento | idénticos a `CU3-B1` salvo el par `o_1·h_2`; rendimiento equivalente |
| Candidato sustituido | `a5bd18b8`, por la celda de los certificados (registro del candidato §5); su evidencia no se reutiliza |
| Smoke | `AUTHOR SMOKE — PASS` (2026-10-28) |
| Publicación | ninguna: la rama remota sigue en `b1813da1`; `main` (`e6c908f2`), etiquetas y libro sin cambios |

## 2. Incidencia de la barra de herramientas fuera del EDT

| Campo | Valor |
|---|---|
| Ejecución | `INTEGRATION` `verification-8a6d1632f8c04a6aaa72d0442b46d63b`, `REJECTED_SEMANTIC` 18/19 |
| Hoja | `junit.desktop.final`: `PreG9BR6PlusE3IsoABorderDesktopTest.coherenceUseSheetScaleAndActivationWarning` |
| Firma | `NullPointerException: Cannot read field "width" because "d" is null` en `Component.getMaximumSize`, desde la reconstrucción de la barra de herramientas (`GeoCeDGToolbarContainer.applyNativeToolPresentation`) |
| Causa caracterizada | los campos de tamaño de Swing no son seguros entre hilos: una invalidación en el EDT entre la lectura de `maxSizeSet` y la de `maxSize` deja el segundo nulo. Es previa a `CU3` y se había observado ya en dos campañas anteriores |
| Relación con `CU3-B2` | ninguna: el código de escritorio es idéntico al de `a5bd18b8`, cuya `INTEGRATION` fue 19/19; `CU3-B2` solo cambia el núcleo compartido y una prueba de escritorio de `CU3` |
| Tratamiento | el rechazo no se reetiqueta; `INTEGRATION` se repitió sobre el mismo commit: `verification-b5021e247450476584409abe807cfcd7`, `ACCEPTED / COMPLETE` 19/19 |
| Disposición | `OBS-DESKTOP-OFF-EDT-TOOLBAR-MAXSIZE-RACE`: defecto de prueba o de interfaz de escritorio, no geométrico, sin corrección dentro de `CU3`; queda para una fase de interfaz (`CU5` o `CU6`) |

## 3. Verificación autenticada del candidato

Comprobación con `Read-VerificationJson` y `Get-VerificationResultHash`: diario `COMPLETED`, commit y
árbol exactos.

| Comprobación | `run_id` | Resultado | `result_hash` |
|---|---|---|---|
| `STATIC` | `verification-37fbc33a6da54ba0a5f3d623008e3333` | `ACCEPTED / COMPLETE` 3/3 | `48a742d63b6ccbf22b71a128480b41f32b66707515163ecc49868ef0492036c4` |
| `INFRA_UNIT` | `verification-743207ebe4854dc19693d383469c710c` | `ACCEPTED / COMPLETE` 22/22 | `fff8a916c6c65461c8c7e44d5b6c7a5200903fb4ce55321cbe571bb804127b27` |
| `PHASE` `PRE-G9B-CU3` | `verification-b26af47733144bf69cfbfb19b4220c4c` | `ACCEPTED / COMPLETE` 4/4 | `5709e80fac9a8681fec7e5286455ee20c63e4536068eccf51756a9a2bef67093` |
| `PHASE` `PRE-G9B-CU2` | `verification-d3b790179f664c65a7c51563c5730f17` | `ACCEPTED / COMPLETE` 4/4 | `10a96551ef80d6ee56229cf04026a37b10970527aa335ea94affcad32d08aabf` |
| `PHASE` `PRE-G9B-CU1` | `verification-433453be2d524b3291ac4cac590d0725` | `ACCEPTED / COMPLETE` 4/4 | `902433b526d9d9617f6e955353bad7351e778fcde09b76ad6dde1f59ce3eadc2` |
| `PHASE` `PRE-G9B-CU0` | `verification-f5a4321be18d4fbca0198652f608ee48` | `ACCEPTED / COMPLETE` 2/2 | `5ebad2b17a235f150dc7a86a110d8cb15880ecb419076c37730503c5f484c8a9` |
| `INTEGRATION` | `verification-8a6d1632f8c04a6aaa72d0442b46d63b` | `REJECTED_SEMANTIC` 18/19 (§2) | `7113af93d766931c12062e151d435ef83c0946d8ce63322ce1130c8f67ad9e9d` |
| `INTEGRATION` (reejecución) | `verification-b5021e247450476584409abe807cfcd7` | `ACCEPTED / COMPLETE` 19/19 | `2b144564fcf9b4abea015e48104db83b8e4020598928e40acb6d01b8e8170759` |

**Resultado: `CU3-B2` `PASS — AUTHOR APPROVED`** sobre `2d13ad4c`, por la decisión autoral de
2026-10-28. No implica `CU3` global.

## 4. Alcance demostrado y deuda

- La materialización de `CU3-B2` es general respecto de los certificados aceptados (clase 1 o 2, orden
  dos, precondiciones P1). La producción actual de esos certificados está limitada a las clases
  demostradas, en especial `o_1·h_2`. No se promete la admisión de objetivos libres cuya tangencia
  sigue en clase 3.
- `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` (plan consolidado §2.29): ampliar la certificación exacta de
  tangencias regulares por procedencia constructiva, decisiones algebraicas exactas y familias
  matemáticas admitidas progresivamente. No bloquea `CU3`, no autoriza investigación ni
  implementación, y queda para planificación posterior, previsiblemente después de `CU10` y del
  trabajo editorial del libro.
