# Propuesta de adopción — `cedg-reference-export/v1` y ADR 0045

**PROPUESTA DE APROBACIÓN — NO APROBADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1`.
`selfApproved=false`. Este documento no registra `AUTHOR APPROVED`.

| Campo | Valor |
|---|---|
| Contrato | [`geocedg/specs/export/dxf-reference-export.md`](../../geocedg/specs/export/dxf-reference-export.md), `cedg-reference-export/v1`, revisión R1 |
| ADR | [ADR 0045](../adr/0045-separate-dxf-export-of-imported-references.md), propuesto, revisión R1 |
| Candidato | commit de contenido R1 y su árbol: [informe R1](pre_g9b_maint_a_n2_r1_report.md) §J |
| `STATIC` | informe R1 §J |

## 1. Decisiones autorales ya fijadas que aplica

D-R1-1, D-R1-5 (ningún estado de aproximación fallida), D-R1-6 (estado 4: alternativa A, la
referencia se incluye por defecto y la construcción modificada se exporta desde
`Construction`), D-R1-7 (AutoCAD Core Console y ezdxf), D-R1-10 (sin bandera por novedad).

## 2. Decisiones que se proponen con la adopción

| Id | Propuesta |
|---|---|
| RX-D1 | casilla en el diálogo DXF existente, desmarcada por defecto |
| RX-D3 | pesos en forma canónica: solo si algún peso efectivo ≠ 1 |
| RX-D4 a RX-D6 | posición de los códigos 41, bandera 1 y códigos 210/220/230 y 42/43 según la evidencia del piloto de conformidad (informe R1 §F) y RX-16/RX-17 |
| RX-D7 | capa fija `GEOCEDG_REF` |
| RX-D8 | referencias ocultas incluidas con `60 = 1` |
| RX-D9 | área de exportación por la envolvente del polígono de control |
| RX-D10 | conmutadores por operación; preferencia persistente en el gestor |
| RX-D11 | sistema de coordenadas del modelo (`data`) |
| RX-D12 | manifiesto obligatorio con referencias (versión 4) |
| RX-D13 | dependencia estricta del contrato y de los tipos RC-M; integración tras RC |
| RX-D15 | referencias con datos idénticos: ambas incluidas y señaladas |

**Decisiones pendientes del autor** (afectan a la garantía de interoperabilidad, no a la
semántica de la referencia; informe R1 §K):

- **RX-D18** — regla de lectura de AutoCAD: el piloto mostró que AutoCAD 2026 lee mal un 10 %
  de los decimales de 15–17 cifras por 1–3 ulp. Recomendación A: exactitud para ezdxf; AutoCAD
  hasta 4 ulp como limitación documentada del lector. Alternativa B: condición de parada.
- **RX-D19** — valores con exponente de tres cifras, que AutoCAD trunca en silencio.
  Recomendación: rechazarlos como `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE`. Alternativa: decimales
  sin exponente para los pequeños.

## 3. Enmiendas incluidas (grupo X)

A-IMP-4, A-IMP-5, A-IMP-6, A-BS-1, A-EXP-1 a A-EXP-6, A-GEF-1, A-ADR-1 y A-ADR-2 del
[conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md).

## 4. Garantías y límites

- Exportación ordinaria idéntica byte a byte con la opción desactivada.
- Fidelidad `REFERENCE_DEFINITION`: relectura al mismo binary64, comprobada antes de promover;
  no afirma nada sobre el archivo original ni sobre otros lectores.
- Racionales como `SPLINE` racional, nunca como polilínea.
- Inclusión por asociación explícita vigente; nunca por coordenadas.
- Límite: la conformidad externa cubre los fixtures y versiones de lector registrados; no es
  interoperabilidad universal.

## 5. Dependencias

Requiere la adopción de `cedg-reference-store/v1`. Sus estados representados dependen de
`dxf-spline-approximation/v1` §10 y §11; antes de A-DXF1 solo existen referencias sin
representación y elecciones del usuario. Habilita `MAINT-A-DXF0-EXP`, nunca en paralelo con
`MAINT-A3`.

## 6. Recomendación

Adoptar `cedg-reference-export/v1` revisión R1 y aceptar ADR 0045, con las enmiendas del grupo X.
