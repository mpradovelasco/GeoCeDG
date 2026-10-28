# PRE-G9B-CU — Resumen de transferencia tras el cierre de `CU3` (2026-10-28)

| Campo | Valor |
|---|---|
| Carácter | Resumen autocontenido para retomar el track sin reconstruir la conversación de trabajo. No es autoridad: las autoridades son los registros enlazados, `AGENTS.md`, el plan consolidado y el roadmap |
| Estado | `CU0` a `CU3` `PASS — AUTHOR APPROVED`; `CU4` a `CU10` no autorizados; nada de `CU3` publicado |

## 1. Dónde está todo

- **Rama local de trabajo.** `phase/pre-g9b-cu3-implementation`, con todos los commits de `CU3` desde
  `e6c908f2`. La rama remota de revisión sigue en `b1813da1` y `main` en `e6c908f2`. No se ha creado
  ninguna etiqueta.
- **Autoridades del estado.**
  - [cierre global de `CU3`](pre_g9b_cu3_global_closeout_record.md) y su
    [expediente](pre_g9b_cu3_global_closeout_dossier.md);
  - cierres de [`CU3-A`](pre_g9b_cu3_a_closeout_record.md), [`CU3-B1`](pre_g9b_cu3_b1_closeout_record.md)
    y [`CU3-B2`](pre_g9b_cu3_b2_closeout_record.md);
  - plan consolidado `docs/architecture/pre_g9b_cu_corrective_usability_plan.md`, §2.7 a §2.30 y §4 a §6;
  - roadmap `docs/roadmap/geocedg_roadmap.md`.
- **Producto aprobado.** `2d13ad4c` (árbol `64dca8e5`). Lo posterior es solo documentación, incluida la
  guía de usuario (`8f5c4f45`).

## 2. Qué hace `CU3`

- **Etapa A.** `Point(L, D)` e `Intersect(L, r, D)`: puntos estructurales por correspondencia con el
  conductor y segunda incidencia decidida exactamente, solo con la autoridad constructiva adoptada
  (`constructive-authority/v1`, ADR 0040).
- **Etapa B.** Soluciones ancladas sin conductor en `Intersect(L, r)`:
  - en pliegues certificados de `CU2` (`CU3-B1`, germen `fold-contact-germ/v1`);
  - en tangencias regulares de clase 1 o 2 (`CU3-B2`, germen `regular-tangent-germ/v1`, lado
    certificado del signo de `h''`).
- **Persistencia.** Libro de tokens v6 con discriminador `P`/`S`/`F`/`T`. La carga aplica la política
  persistida en cuanto valida su registro (corrección K6).
- **Normativa vigente.**
  - `locus-v2-intersections.md` 1.6;
  - `locus-v2-public-surface.md` §21;
  - ADR 0017 (con nota), ADR 0039 (con enmiendas) y ADR 0040.

## 3. Compromisos y deudas abiertos

- **`CU10-GUIDE-INTEGRATION-CHECK`.** En `FINAL` de `CU10`, verificar la integración y el empaquetado de
  la guía cambiada en `8f5c4f45`. Sobre los árboles documentales no se ha ejecutado ninguna campaña
  integrada.
- **`DEBT-CU3-EXACT-TANGENCY-GENERALIZATION`.** Hoy solo `o_1·h_2` produce una tangencia regular de
  clase 2. La deuda queda para después de `CU10` y del libro.
- **Observaciones.**
  - `OBS-CU3B2-SILENT-DRIVER-REDEFINITION-REFUSAL` y `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER`,
    para `CU7`;
  - `OBS-DESKTOP-OFF-EDT-TOOLBAR-MAXSIZE-RACE`, para `CU5` o `CU6`;
  - `OBS-CU3A-XMLEDIT-01`, abierta y no bloqueante;
  - la ayuda de sintaxis de la aplicación sin las formas con conductor, para `CU9`.
- **Publicación.** La de la rama y el avance de `main` requieren una decisión autoral aparte, con
  identidad exacta y avance rápido sin forzar.

## 4. Próxima puerta

Según la secuencia del plan (§5), la línea principal sigue con `CU7-D` (diseño de M08 y M09) y después
`CU7-I`. `CU4`, `CU5` y `CU8-D` son elegibles en paralelo; `CU9` y `CU10` cierran el track. **Ninguna
está autorizada.** El siguiente agente debe esperar una instrucción autoral expresa y leer antes
`AGENTS.md`, los prompts canónicos de gobierno y verificación y el roadmap desde el disco.

## 5. Lecciones operativas útiles

- **Verificador.** Solo se ejecuta desde PowerShell: `tools/agent/verify.ps1`.
  - `INTEGRATED_PHASE` = `STATIC`, `INFRA_UNIT`, `PHASE` de `CU3` a `CU0` e `INTEGRATION`.
  - No se reutiliza evidencia entre commits.
  - Una `INTEGRATION` rechazada no se reetiqueta: se repite.
- **Certificador de identidad constructiva.** Cada signo que decide entra en la celda del certificado.
  Decidir signos adicionales para todos los objetivos cambió certificados ajenos y ralentizó los
  arrastres; hay que compararlo siempre con el corpus del candidato anterior.
- **Ficheros con finales de línea mixtos.** Los del núcleo y los `menu*.properties` se editan byte a
  byte; el editor convencional los aplana.
- **Guía de usuario.** El índice está fijado en 104 anclas: un complemento sin anclas nuevas usa
  encabezados de nivel 4.
