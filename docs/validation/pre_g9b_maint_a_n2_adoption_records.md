# PRE-G9B-MAINT-A-N2 — registros de adopción preparados y cierre normativo

**PREPARADOS — SIN DECISIÓN AUTORAL REGISTRADA.** `selfApproved=false`, `authorApproved=false`.

Este documento contiene los tres registros de adopción **completos**, a falta solo de la
decisión, la fecha y las palabras del autor. Contiene también el cierre normativo conjunto
preparado. No aprueba nada. Ningún contrato, ADR, enmienda ni el roadmap cambia de estado hasta
que el autor decida sobre el candidato exacto de §1.

## 1. Candidato que se adopta

| Elemento | Valor |
|---|---|
| Commit de contenido normativo | `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb` |
| Árbol | `7faccc1fc5d41f5d55d9fbb1ca5fd3473aa3e4fe` |
| Rama | `phase/pre-g9b-maint-a-n2-r4` |
| Base publicada | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` |
| `STATIC` | `verification-24030c218682494f8d989f6424062e5d`, salida `0`, `ACCEPTED` / `COMPLETE` (3 satisfechas, 0 violadas); diagnósticos permanentes `diagnostic.governance` `DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`; informe `artifacts/agent/pre-g9b-maint-a-n2-r4-static-9c7e6122/verification-result.json` (ignorado), SHA-256 `d7fd496cf7b07f5fb76b86b97ecf30b4a57b1f99edd776c7f220e4376e5beb52`; resumen recalculado igual al almacenado |
| Revisiones independientes | R3: dos, M1 a M3 resueltos. R4 final: I-1, I-2, presupuesto y m-1 a m-5 (§7 del informe R4). Verificación de la corrección G5, sin contradicción (§13.3) |
| Decisiones del autor | D-R1-1 a D-R1-10; RX-D18 y RX-D19 (R2); instrucción R4 (normal, contador, presupuesto, RX-D19 no ampliada); **D-R4-1** (2026-11-26) |
| Informe | [informe R4](pre_g9b_maint_a_n2_r4_report.md), con §13 |

Este documento llega en un descendiente documental de ese commit. No forma parte del contenido
normativo que se adopta.

## 2. Registros de adopción

### 2.1 `cedg-reference-store/v1` (`R-c`) + ADR 0044

| Elemento | Contenido |
|---|---|
| Decisión del autor | `<pendiente: palabras y fecha del autor>` |
| Contrato y revisión | [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md), revisión R4; [ADR 0044](../adr/0044-document-store-of-imported-references.md), revisión R4 |
| Candidato | `9c7e61229`, árbol `7faccc1fc` (§1) |
| Enmiendas | grupo R: A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-IMP-12, A-NDI-1 y A-ADR-3 ([enmiendas candidatas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md)) |
| Decisiones adoptadas | D-R1-1 a D-R1-5, D-R1-8, D-R1-10; D-R4-1; RC-D1 a RC-D17. En particular: presupuesto inicial configurable de 10 000 referencias y 1 500 000 valores (RC-D9); normal de origen con presencia por grupo (RC-D16); contador `values` exhaustivo (RC-D17) |
| Limitaciones | los lectores ≤ 1.2.2 pierden el almacén al guardar (notas de versión); un almacén por encima del presupuesto se rechaza con causa tipada y nunca se trunca; el presupuesto procede de una estación y de una sonda, no del producto; el caso al 100 % de `maxEntryBytes` no se midió; un grupo 210, 220 o 230 repetido conserva su última aparición |
| Dependencias | ninguna para adoptar; requisito de los otros dos contratos |
| Verificación | `STATIC` de §1; revisiones independientes R3 y R4; mediciones del presupuesto de R3 ([informe R3](pre_g9b_maint_a_n2_r3_report.md) §3) |
| Obligaciones de implementación | `MAINT-A-DXF0-RC-S0`: C-1 a C-3 y el comportamiento actual de C-5. `MAINT-A-DXF0-RC`: hito RC-M primero; códec y validación con el contador `values`; modificación aditiva mínima del registro de importación para la presencia de 210, 220 y 230 con las seis condiciones de D-R4-1; preparación y confirmación en todas las rutas; estado guardado; Classic con conservación opaca o barrera; escáneres; RC-1 a RC-26 (RC-25 (f) incluido); no regresión DX-1 a DX-13; **puertas de cierre** C-4 y C-5 en el presupuesto |

### 2.2 `cedg-reference-export/v1` + ADR 0045

| Elemento | Contenido |
|---|---|
| Decisión del autor | `<pendiente: palabras y fecha del autor>` |
| Contrato y revisión | [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md), revisión R4; [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md), revisión R4 |
| Candidato | `9c7e61229`, árbol `7faccc1fc` (§1) |
| Enmiendas | grupo X: A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1, A-ADR-1 y A-ADR-2 |
| Decisiones adoptadas | D-R1-5 a D-R1-7, D-R1-10; RX-D1 a RX-D19, con RX-D18 y RX-D19 fijadas por el autor y RX-D19 no ampliada (R4). Además: situaciones A, B y C del manifiesto; exportación solo de referencias en el flujo extendido; dos lecturas informativas de la procedencia |
| Limitaciones | conformidad AutoCAD declarada solo para AutoCAD 2026 (`ACADVER` 25.1), con ≤ 4 ulp por valor más estructura y geometría, y para ezdxf 1.4.2 exacto, sobre los fixtures; exponentes de tres cifras `NOT_WRITABLE`; la opción existe solo en el flujo extendido emparejado y con `cedg.export.dxf.extended` desactivada no puede exportarse ninguna referencia; deudas separadas DT-1 y DT-2 |
| Dependencias | `R-c` adoptado; EXP tras el hito RC-M; nunca en paralelo con `MAINT-A3` |
| Verificación | `STATIC` de §1; revisiones independientes R3 y R4; verificación G5 (informe R4 §13.3) |
| Obligaciones de implementación | `MAINT-A-DXF0-EXP`: casilla en el diálogo del flujo extendido; resolutor de §6; adaptador de referencias; escritor `SPLINE` con comprobación de ida y vuelta; preflight y manifiesto v4 con A, B y C; ruta del controlador de §9.1 solo en el flujo extendido, con el flujo G5 intacto; normal de salida `0/0/1` nunca atribuida al origen; regla `EXPONENT_NOT_PORTABLE`; RX-1 a RX-6, RX-8 a RX-17 y RX-19, con RX-15 (e) por el controlador real; identidad de bytes de la exportación ordinaria; evidencia externa RX-16, RX-17 y RX-19 |

### 2.3 `dxf-spline-approximation/v1` (A-DXF1)

| Elemento | Contenido |
|---|---|
| Decisión del autor | `<pendiente: palabras y fecha del autor>` |
| Contrato y revisión | [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md), revisión R4; sin ADR nuevo (ADR 0043 decisión 11; ADR 0045 registra las dos lecturas informativas) |
| Candidato | `9c7e61229`, árbol `7faccc1fc` (§1) |
| Enmiendas | grupo A: A-IMP-7, A-IMP-8, A-IMP-11 y A-BS-2 |
| Decisiones adoptadas | D-R1-5, D-R1-9, D-R1-10; AX-D1 a AX-D14 |
| Limitaciones | sin cota certificada en `MAINT-A-DXF1` (en `MAINT-A-DXF1b`); medidas `SAMPLE_RESIDUAL` y `ESTIMATED` con sus grados; constantes pendientes de medición |
| Dependencias | `R-c` adoptado; el servicio tras RC y DXF1-S0; DXF1b tras DXF1 |
| Verificación | `STATIC` de §1; revisiones independientes R3 y R4 |
| Obligaciones de implementación | `MAINT-A-DXF1-S0`: NUM-1 a NUM-10. `MAINT-A-DXF1`: fijar `bspline-param-lsq-constants/v1` como puerta de cierre; autocomprobación que relee también el bloque; normalización de `-0.0`; sin historial de fallos; interfaz por lectura informativa; RX-7 y RX-18 |

## 3. Cierre normativo conjunto preparado

Con la decisión del autor sobre §1, en una rama de cierre `phase/pre-g9b-maint-a-n2-closeout`
desde el commit que la decisión nombre, y en un único commit documental:

1. Completar los tres registros de §2 con las palabras y la fecha del autor, en este documento
   o en el registro de cierre que el autor indique.
2. Pasar los estados de los tres contratos a aprobados y los de ADR 0044 y 0045 a aceptados,
   con enlace a estos registros.
3. Aplicar literalmente las enmiendas de los grupos R, X y A, como secciones o reemplazos
   fechados que conservan la historia de aprobación, a:
   - `dxf-spline-reference.md` (A-IMP-1 a A-IMP-12);
   - `bspline-2d.md` (A-BS-1, A-BS-2);
   - `dxf-curve-fidelity-and-approximation.md` (A-EXP-1 a A-EXP-6);
   - `geometry-export-foundation.md` (A-GEF-1);
   - `native-document-identity.md` (A-NDI-1);
   - las líneas de estado de ADR 0014 y ADR 0043 (A-ADR-1 a A-ADR-3).
4. Escribir el registro aditivo del roadmap con el único borrador canónico A-RM-1
   ([enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md) §5), rellenando la
   fecha y el SHA. Registra C-4 y C-5 como puertas de cierre de RC.
5. Marcar el plan operativo como planificación aprobada.
6. Marcar en los prompts candidatos sus dependencias satisfechas, sin autorizar ningún
   incremento.
7. `STATIC` sobre el commit de cierre exacto y clasificación de cualquier descendiente con
   `tools/agent/documentary-descendant.ps1`.

**Publicación propuesta.** Solo con una instrucción autoral que nombre el SHA del cierre: avance
rápido sin forzar de `main` desde `a9ee803bf` (AGENTS §8.1) y push de `main`, sin etiqueta,
release ni empaquetado. El delta respecto de `main` es solo documentación, especificaciones,
ADR y prompts de tarea. No incluye código, recursos, pruebas, verificador ni empaquetado.

**Después del cierre.** Las cinco implementaciones (informe R4 §12) necesitan una instrucción
autoral de ejecución con base exacta. No requieren otra fase de diseño.
