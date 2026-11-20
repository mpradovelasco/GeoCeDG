# GeoCeDG 1.2.x — Disposición sucesora de estado y publicación

Registro documental del 2026-11-20, preparado por instrucción autoral
(«Autorización autoral de reconciliación, adopción de baseline 1.2.1 y
preparación editorial científica», decisión B). Reúne en un solo lugar la
relación entre la campaña R13, la continuación correctiva posterior, la
publicación de GeoCeDG 1.2.0, el mantenimiento de interacción con puntos sobre
Locus V2 y la publicación de GeoCeDG 1.2.1.

**Este registro no reclasifica ningún resultado de campaña.** No convierte un
resultado fallido en satisfactorio, no atribuye verificaciones que no constan y
no origina ninguna aprobación. Los informes contemporáneos de cada campaña
conservan su contenido y prevalecen sobre este resumen para lo que cada uno
registra. Principio aplicado: la aceptación posterior de un producto no modifica
el resultado histórico de sus campañas anteriores.

## 1. Cadena de hechos

| # | Fecha | Hecho | Identidad | Fuente |
|---|---|---|---|---|
| 1 | 2026-11-11 | `PRE-G9B-CU10` cierra con excepción autoral expresa a la puerta `FINAL`. La campaña histórica terminó `REJECTED_VERIFICATION_CORE / COMPLETE`, 39/41 controles (semántica 15/15, seguridad 4/4, infraestructura de verificación 20/22), sin recibo. | candidato `20ae57aa484fc0d60c65de76a23151d4d2a758c1` | [cierre CU10](pre_g9b_cu10_closeout_record.md) |
| 2 | 2026-11-11 | El cierre CU10 queda integrado en `main`. | `39f40e7b860bd3758a32a949996d36b10b7fae57`, árbol `c663d482641ad3e5bfc5f85a4ee7f389d77f1d2f` | historial de `main` (§3, punto 1) |
| 3 | 2026-11-15/16 | Implementación de 1.2.0 y rondas R1–R13 desde `main` `4fc0343a1f0dc59a645ee4fe8ec6c5c40fe2051e`. | rama `codex/geocedg-1.2.0` | [expediente de verificación](geocedg_1_2_0_validation_dossier.md) |
| 4 | 2026-11-16 | **R13 termina y falla.** 265 casos: 262 aprobados y tres fallidos, sin errores ni omisiones; trece de trece rondas consumidas; cierre técnico bloqueado en aquel checkpoint. Causa registrada: objetos huérfanos en el registro global de imágenes durante recuperaciones excepcionales. | foco sobre `6f1d357ca2224dedd0e84fda0faed2d600154bab` con 73 rutas modificadas; checkpoint rechazado `0ae033ff324d3d3102667718e6894dafbe33c89a` | [informe terminal R13](geocedg_1_2_0_r13_terminal_report.md) · [ejecución R13](geocedg_1_2_0_r13_execution.md) |
| 5 | 2026-11-17 | El autor aprueba el plan G0: una continuación nueva, «not an extension or reinterpretation of the thirteen closed historical rounds», con un máximo de seis unidades correctivas. | S protegido `0ae033ff3` | [continuación correctiva](../architecture/geocedg-1.2.0-corrective-continuation.md) |
| 6 | 2026-11-17 | Unidad A (ciclo de vida de la generación de imágenes y su certificado): foco final 84/84. Sexta unidad: foco 98/98. Son resultados **focales** de commits intermedios; su evidencia se conserva fuera del repositorio. | `4ca1db1c3`, `b0fcee9db`, `0623085a3` | ídem, «A focal evidence» y «Sixth-unit focal result» |
| 7 | 2026-11-17/18 | Ejecución Shared completa: 7 821 pruebas, 7 802 aprobadas, nueve fallos de aserción y diez omisiones autorizadas; ocho de los nueve ya fallaban en S. Después: P1, P2, contexto escalar nativo y consolidación P2, con focos 151/151, 67/67 y 182/182; reconciliación de los inventarios JUnit. | `0420d8ee7` … `4f6a0a9e3` | ídem, «Blocking broad Shared findings»; [consolidación P2](../architecture/geocedg-1.2.0-p2-final-consolidation.md) |
| 8 | 2026-11-19 | **Publicación de GeoCeDG 1.2.0**, decisión autoral posterior y distinta de R13. | tag anotado `v1.2.0`, objeto `e7a16a3e23504fb3d7a00989e0b9b3129a989cd4`, commit `4f6a0a9e3ae25702c8c43fef287bf5881e0cb24e`, árbol `ab96f99ad2cfe423e40a0229b0b2f415e02fdff0` | [roadmap, 2026-11-19](../roadmap/geocedg_roadmap.md#2026-11-19--geocedg-120-publicada-y-mantenimiento-correctivo-posterior) · [registro de release](../releases/v1.2.0.release.json) |
| 9 | 2026-11-19 | Mantenimiento correctivo posterior a 1.2.0, interacción con puntos sobre Locus V2: `PASS — AUTHOR APPROVED` por decisión autoral; `STATIC` y dos `PHASE` aceptados; smoke manual `PASS — AUTHOR REPORTED`; «No se ejecutaron `INTEGRATION`, `FINAL` ni packaging». | `335755b2090871b6c36b100de17a1535f96c6016`; cierre `bbeb963e63153a1463d08fcc80b542b750d5a12c` | [registro de cierre](geocedg_1_2_0_post_release_locus_v2_point_interaction_closeout_record.md) |
| 10 | 2026-11-20 | **Publicación de GeoCeDG 1.2.1**, versión correctiva sin nuevas funcionalidades. `STATIC` y `PACKAGING` `ACCEPTED / COMPLETE` sobre el commit de la release; no se ejecutó `FINAL`. | tag anotado `v1.2.1`, objeto `085d71439784512e1ff6011cfeaf51e66be79519`, commit `4267cff35129ea743d911a61e97b3d189d66e4d2`, árbol `bb7adf0653d7d89a77259d41b8b149c7db4cb407` | [roadmap, 2026-11-20](../roadmap/geocedg_roadmap.md#2026-11-20--geocedg-121-publicada) · [registro de release](../releases/v1.2.1.release.json) |

## 2. Cuatro cosas que no deben confundirse

1. **Resultado técnico histórico de R13.** Fallido. Sigue siéndolo. Ningún
   documento posterior lo convierte en aprobado.
2. **Continuación correctiva y su evidencia.** Trabajo posterior a R13 con
   resultados focales propios, incluidos los casos excepcionales que R13 dejó
   fallidos. Esos focos pertenecen a commits intermedios y su evidencia está
   fuera del repositorio: aquí se citan como referenciados, no reautenticados.
   Formulación que sostienen: los tres casos de R13 tienen una corrección
   posterior con evidencia focal registrada. No sostienen que R13 pasara ni que
   1.2.0 superase un cierre técnico global.
3. **Decisiones autorales de publicación.** La de 1.2.0 y la de 1.2.1 son
   decisiones propias del autor, con sus fechas, y conservan su validez con
   independencia del resultado de campañas anteriores.
4. **Evidencia propia de cada publicación.** Es la que enumera su registro de
   release, y solo esa. Una publicación autorizada no convierte pruebas
   fallidas, no ejecutadas o no establecidas en pruebas satisfactorias.

GeoCeDG 1.2.1 no aporta evidencia sobre el subsistema de imágenes: su
mantenimiento «no cambia la evaluación, la longitud, la estructura de
existencia, la persistencia ni la serialización de Locus V2» y ninguno de los
43 archivos que cambian entre `v1.2.0` y `v1.2.1` pertenece a la inserción o
recuperación de imágenes.

## 3. Datos que siguen sin establecerse

Se buscaron en el repositorio, en su historial y en los artefactos locales de
esta estación. Lo que sigue no pudo establecerse y se conserva como pendiente.

| # | Dato | Estado | Causa | Impacto |
|---|---|---|---|---|
| 1 | Fecha y condiciones del avance de `main` al cierre CU10 | `REPORTED — NOT RE-AUTHENTICATED` | Git establece que `39f40e7b` es ancestro de `4fc0343a1` y de `main`. La fecha del push, la lectura del remoto y la excepción administrativa constan solo en una auditoría local no versionada (`artifacts/post-cu10/publication/`). El registro de cierre CU10 conserva, como texto de su fecha, «PUBLICATION — PENDING EXACT-COMMIT AUTHORIZATION». | Ninguno sobre el producto. La integración es un hecho de historial; sus circunstancias no son citables como autoridad. |
| 2 | Verificación ejecutada sobre el commit de la release 1.2.0 (`STATIC`, `PACKAGING`, `PHASE`, `INTEGRATION`, `FINAL`) | `NOT ESTABLISHED` | Ningún documento versionado registra resultados sobre `4f6a0a9e3` y ningún resultado tipado local está ligado a ese commit. La continuación correctiva declara su evidencia «outside tracked source». | No puede afirmarse ninguna campaña global ni de empaquetado sobre la release 1.2.0. Su registro de release lo declara así. |
| 3 | Contenido y resultado del smoke selectivo autoral de 1.2.0 | `NOT ESTABLISHED` | El mensaje del tag `v1.2.0` dice «Author-approved selective smoke and controlled publication». No hay registro de qué casos se ejecutaron ni de sus resultados. El [guion de smoke](geocedg_1_2_0_author_smoke.md) es anterior y no lo registra. | No se atribuye al autor ninguna comprobación individual. Consta la decisión, no su detalle. |
| 4 | Registro propio de la decisión de publicación de 1.2.0 | `NOT ESTABLISHED` como documento | La decisión consta en el mensaje del tag y en la sección del roadmap del 2026-11-19. No existe un registro de cierre o de publicación dedicado. | La procedencia de la decisión se cita por esas dos fuentes. |
| 5 | Disposición final, en el commit de la release, de los ocho fallos Shared que ya existían en S | `NOT ESTABLISHED` | La continuación correctiva deja sus causas raíz como `NOT_ESTABLISHED` y no registra un cierre posterior. | Se conserva como límite abierto de 1.2.x. |

## 4. Límites vigentes de 1.2.x

Se remiten a su fuente; no se copian.

- Limitaciones residuales del mantenimiento, sección F de su
  [registro de cierre](geocedg_1_2_0_post_release_locus_v2_point_interaction_closeout_record.md):
  tiempo inicial de búsqueda sobre `s_2`; evaluación inválida aislada de `s_2`
  en `u = -π/6`, sin investigar; rutas de carta certificada no revisadas;
  garantía `FLOATING_POINT_UNCERTIFIED` de la continuación paramétrica;
  inventario JUnit sin regenerar para las tres clases de test añadidas.
- Limitaciones y deudas del track `PRE-G9B-CU` conservadas por el
  [cierre CU10](pre_g9b_cu10_closeout_record.md), entre ellas
  `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`.
- No hay campaña `FINAL` aceptada sobre el commit de 1.2.0 ni sobre el de
  1.2.1. El contrato vivo de
  [niveles de verificación](../../geocedg/specs/operations/verification-levels.md#current-contract)
  no fija una clase de verificación para una release; la regla que exigía la
  campaña completa pertenece a su parte archivada.

## 5. Registros de release

Los [registros de release](../releases/README.md) describen cada publicación de
forma verificable: identidad del tag, commit y árbol, distribución, contraste
con GitHub, verificación aceptada, no ejecutada y no establecida, y límites.
No contienen un indicador único de aprobado y no son declaraciones autónomas de
éxito técnico. El registro de 1.2.0 declara vacía la verificación aceptada
sobre su commit; el de 1.2.1 declara `STATIC` y `PACKAGING`.

## 6. Alcance

Este registro no autoriza ni inicia ninguna fase, no modifica contratos de
verificación, especificaciones ni ADR, no diagnostica ni corrige el subsistema
de imágenes y no decide el baseline técnico del libro, que se resuelve en su
propio repositorio. `selfApproved=false`.
