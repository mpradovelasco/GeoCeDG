# PRE-G9B-MAINT-A-N2 — registro de aprobación autoral de los tres contratos

| Campo | Valor |
|---|---|
| Decisión | **APROBADOS — AUTHOR APPROVED**, individualmente, los tres contratos normativos de `PRE-G9B-MAINT-A-N2-R4`, con sus ADR, enmiendas y decisiones registradas |
| Autor | autor del proyecto GeoCeDG |
| Fecha | 2026-11-26 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-A-N2. Aprobación autoral de los tres contratos y ejecución del cierre normativo», entregada en la conversación |
| Contenido normativo aprobado | commit `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb`, árbol `7faccc1fc5d41f5d55d9fbb1ca5fd3473aa3e4fe` |
| Registros de adopción | commit `cadad2b5630e9928a737a9c4b51c2832b1b1395b`, árbol `47edc050828bd94044e9dcb30bc3829be7b567fb`; [registros](pre_g9b_maint_a_n2_adoption_records.md) |
| Verificación del contenido | `STATIC` `verification-24030c218682494f8d989f6424062e5d`, salida `0`, `ACCEPTED` / `COMPLETE`; resumen recalculado igual al almacenado (comprobado de nuevo al iniciar el cierre) |
| Carácter | aprobación normativa; **no** autoriza implementación, push a `main`, etiquetas, releases ni empaquetado |

## 1. Contratos aprobados

| Contrato | Revisión | ADR | Enmiendas |
|---|---|---|---|
| [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md) — persistencia `R-c` | R4 | [ADR 0044](../adr/0044-document-store-of-imported-references.md), aceptado | grupo R: A-IMP-1, A-IMP-2, A-IMP-3, A-IMP-9, A-IMP-10, A-IMP-12, A-NDI-1, A-ADR-3 |
| [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md) — exportación DXF de referencias | R4 | [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md), aceptado | grupo X: A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1, A-ADR-1, A-ADR-2 |
| [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md) — conversión y aproximación A-DXF1 | R4 | ninguno nuevo (ADR 0043 decisión 11, refinada por ADR 0045) | grupo A: A-IMP-7, A-IMP-8, A-IMP-11, A-BS-2 |

Las enmiendas son las del
[conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md),
aplicadas por el cierre a los textos aprobados (§4).

## 2. Alcance ratificado expresamente por el autor

**`R-c`:**

- almacén documental fuera de `Construction` y del DAG;
- identidades propias de las referencias;
- persistencia de los datos originales y transformados;
- presupuesto inicial configurable de 1,5 millones de valores;
- protección obligatoria contra consumo excesivo de recursos y entradas malformadas;
- atomicidad documental;
- conservación opaca en GeoCeDG Classic;
- ausencia de historial persistente de aproximaciones fallidas;
- decisión D-R4-1 sobre la presencia individual de los grupos 210, 220 y 230, con su
  modificación aditiva mínima en el futuro incremento RC;
- C-4 y C-5 como puertas de cierre de RC.

**Exportación:**

- referencias como población diferenciada de `Construction`;
- exportación `SPLINE` de definiciones externas;
- fidelidad `REFERENCE_DEFINITION`;
- inclusión explícita de referencias, sin duplicación automática con construcciones que las
  representen válidamente;
- inclusión predeterminada de la referencia cuando su construcción derivada ha sido
  modificada;
- comprobaciones independientes mediante AutoCAD y ezdxf;
- política RX-D18 de conformidad experimental;
- política RX-D19 de rechazo de exponentes no portables;
- exportación exclusivamente de referencias en el flujo DXF extendido;
- flujo G5 heredado sin modificaciones.

**A-DXF1:**

- conversión exacta de definiciones compatibles;
- aproximación polinómica mediante el método acordado;
- tres conjuntos diferenciados de muestras;
- QR y determinismo;
- diagnósticos tipados y ausencia de construcción parcial ante fallo;
- procedencia pasiva y comprobación de aplicabilidad;
- persistencia numérica exacta según el contrato;
- cota global certificada diferida a A-DXF1b.

**Carácter provisional conservado.** Las constantes pendientes de validación y las puertas de
implementación conservan su carácter provisional. Entre ellas están:

- las constantes de A-DXF1 (AX-D6);
- el presupuesto, que C-4 vuelve a medir;
- la evidencia de conformidad RX-16, RX-17 y RX-19.

La aprobación normativa no afirma que estén validadas experimentalmente.

## 3. Decisiones autorales incluidas

| Decisión | Fecha | Contenido |
|---|---|---|
| D-R1-1 a D-R1-10 | 2026-11-25 | instrucción R1 |
| RX-D18, RX-D19 | 2026-11-25 | instrucción R2 |
| instrucción R4 | 2026-11-26 | gramática de `normal`, contador `values`, presupuesto de 1 500 000 valores, RX-D19 no ampliada |
| D-R4-1 | 2026-11-26 | presencia individual de 210, 220 y 230 con seis condiciones; diseño y alcance futuro de RC, no el código |
| esta aprobación | 2026-11-26 | los tres contratos, sus ADR y enmiendas; cierre documental autorizado |

## 4. Cierre normativo autorizado

La misma instrucción autoriza el cierre documental en `phase/pre-g9b-maint-a-n2-closeout`,
creada desde `cadad2b56`. El cierre:

- registra esta aprobación;
- pasa los contratos a `AUTHOR APPROVED` y las ADR 0044 y 0045 a aceptadas;
- aplica las enmiendas a los textos aprobados como enmiendas fechadas que conservan su
  historia de aprobación;
- incorpora A-RM-1 al roadmap;
- registra las dependencias y puertas de los cinco incrementos;
- actualiza las referencias cruzadas;
- prepara el cierre consolidado
  ([registro de cierre](pre_g9b_maint_a_n2_closeout_record.md)).

No altera el contenido técnico aprobado.

**Publicación: no autorizada todavía.** El push a `main` exige una autorización autoral
posterior que nombre el candidato de cierre exacto, conforme a AGENTS §8.1. No se crean
etiquetas, releases ni paquetes.

**Implementación: no autorizada.** Una autorización futura podrá comprender conjuntamente:

- `MAINT-A-DXF0-RC-S0`;
- `MAINT-A-DXF0-RC`;
- `MAINT-A-DXF0-EXP`;
- `MAINT-A-DXF1-S0`;
- `MAINT-A-DXF1`.

Quedan expresamente fuera `MAINT-A-DXF1b`, `MAINT-A3`, la NURBS nativa y los mantenimientos
independientes del importador y del exportador DXF existentes.
