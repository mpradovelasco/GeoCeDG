# Propuesta de adopción — `dxf-spline-approximation/v1` (A-DXF1)

**PROPUESTA DE APROBACIÓN — NO APROBADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1`; confirmada sin cambios por `PRE-G9B-MAINT-A-N2-R2`.
`selfApproved=false`. Este documento no registra `AUTHOR APPROVED`.

| Campo | Valor |
|---|---|
| Contrato | [`geocedg/specs/import/dxf-spline-approximation.md`](../../geocedg/specs/import/dxf-spline-approximation.md), `dxf-spline-approximation/v1`, revisión R1 |
| ADR | ninguno nuevo: ADR 0043 decisión 11 ya sitúa el bloque de procedencia; ADR 0045 registra su lectura informativa por la exportación |
| Candidato | commit de contenido R2 y su árbol: [informe R1](pre_g9b_maint_a_n2_r1_report.md), anexo R2 (R2 no cambia este contrato; su texto es el de R1, commit `228d91d44`) |
| `STATIC` | informe R1, anexo R2 (y §J para el candidato R1) |

## 1. Decisiones autorales ya fijadas que aplica

Además, fijadas por la instrucción R1 (§9): tres conjuntos disjuntos sin orden afirmado (AX-D3),
normalización explícita de `-0.0` (AX-D12) y conservación del bloque en la redefinición por
sustitución como obligación de `MAINT-A-DXF1` (AX-D13).

D-R1-1, D-R1-5 (fallo sin construcción, sin Undo, sin marca de modificado, sin historial;
diagnóstico inmediato e informe de sesión), D-R1-9 (`MAINT-A-DXF1` sin cota certificada;
`MAINT-A-DXF1b` aparte), D-R1-10 (sin bandera por novedad; sin «experimental» visible).

## 2. Decisiones que se proponen con la adopción

| Id | Propuesta |
|---|---|
| AX-D1 | requiere `R-c`; la biblioteca puede desarrollarse en paralelo |
| AX-D4 | bloque `v1` malformado: objeto cargado, bloque conservado, `NOT_INTERPRETABLE` |
| AX-D5 | grado 3 por defecto |
| AX-D7 | Householder propio sin pivotado |
| AX-D8 | una fuente convertible no se aproxima en `v1` |
| AX-D9 | entradas por rutas exactas probadas (puntos libres por valor; nudos por decimales más cortos) |
| AX-D11 | nudos de la fuente por encima de `N_A`: fallo tipado; referencia exportable |

**Decisiones pendientes de validación:** las constantes de §8 (AX-D6), que `MAINT-A-DXF1-S0` y
`MAINT-A-DXF1` medirán antes de que el autor las fije; los puntos marcados «pending validation»
de §5.9.

## 3. Enmiendas incluidas (grupo A)

A-IMP-7, A-IMP-8 y A-BS-2 del
[conjunto de enmiendas](../architecture/pre_g9b_maint_a_n2_candidate_amendments.md).

## 4. Garantías y límites

- Conversión exacta: igualdad de datos binary64 (salvo el signo de cero), sin medidas.
- Aproximación: residuo muestral y estimación reservada, cada una con su grado; nunca presentadas
  como cotas rigurosas.
- Rango completo en aritmética exacta demostrado (§5.9, punto 1); el criterio numérico de rango
  es una regla de diseño.
- Persistencia exacta de las entradas por rutas caracterizadas; autocomprobación antes de
  confirmar.
- Límite: una polinómica de más de 128 puntos no es aproximable en `v1` cuando ningún grado
  admisible cabe en `N_A` con los nudos de la fuente (con `q ≥ p` nunca cabe; un `q < p` puede
  caber); sigue exportable.
- Propuesta de madurez: sin bandera por novedad; solo en su rama hasta el cierre autoral, que
  declara su estado formal (AGENTS §5).

## 5. Dependencias

Requiere la adopción de `cedg-reference-store/v1`. Habilita `MAINT-A-DXF1-S0` y `MAINT-A-DXF1`;
`MAINT-A-DXF1b` necesitará su propia autorización.

## 6. Recomendación

Adoptar `dxf-spline-approximation/v1` revisión R1 con las enmiendas del grupo A.
