# PRE-G9B-MAINT-A-DXF0 y A-DXF0-R1 — registro de las decisiones autorales

**PRE-G9B-MAINT-A-DXF0 + R1 — SMOKES AUTORALES PASS — CIERRE PENDIENTE DE LA AUTORIZACIÓN DE LA SEGUNDA OLA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: las decisiones son del autor y este registro solo las transcribe; el agente no aprueba nada |
| Fecha de las decisiones | 25 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre de segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §4.3 y §5; confirmación del smoke focal de R1 escrita por el autor en la conversación («Smoke PASS») |
| Contrato | `dxf-spline-reference/v1` revisión 2 (`geocedg/specs/import/dxf-spline-reference.md`), persistencia `R-a` |
| Candidato A-DXF0 | `177da00f6ca886a40b538713fafb49f01514992d`, árbol `f2f078287623f2566002e2f9e35a89f2e0872727` (conservado con sus verificaciones) |
| Candidato A-DXF0-R1 | `47c1c2e41d6d6bea6be13f3537168e59ed280d6c`, árbol `92e53d38bd8964e3e2dac9b0a2db4d8026894085`, rama `phase/pre-g9b-maint-a-dxf0-r1` |
| Ascendencia de R1 | gobernanza `751857c25` (prompt de tarea), técnico `902f8d4f9`, inventario e informe `47c1c2e41` |

## 1. Verificación de los candidatos aislados

Todas `ACCEPTED / COMPLETE` (`CORE + FOCUSED`, no exhaustiva):

| Candidato | `STATIC` | `PHASE PRE-G9B-MAINT-A-DXF0` | `INTEGRATION` |
|---|---|---|---|
| A-DXF0 `177da00f6` | `verification-e089f558b4de4b749d7736776ed86b50` | `verification-2e88ca1ce62e4a9b9c07714b2734deb2` | `verification-afb1353d2c7c47b38bcaef5bd5b1d9a1` |
| A-DXF0-R1 `47c1c2e41` | `verification-3eb6bfef7255452cb2587760750727d6` | `verification-e989b912e2cc49deab79e67068a4386f` | `verification-eb20b66e2b8e49c8adf10a97a12b0416` |

Esos resultados pertenecen a los candidatos aislados y no se atribuyen al árbol integrado.

## 2. Decisiones

```text
A-DXF0     AUTHOR_SMOKE = PASS — AUTHOR REPORTED
A-DXF0-R1  AUTHOR_SMOKE = PASS — AUTHOR REPORTED
```

Los smokes los ejecutó el autor y comunicó su resultado; el agente no los ha ejecutado ni
verificado.

- El smoke de A-DXF0 aprueba el funcionamiento comprobado del importador `R-a`, con la corrección
  focal R1 como condición previa.
- R1, autorizado por el autor: la importación es la última entrada de File → Insert («Import DXF
  Reference…» / «Importar referencia DXF…»); disponible sin bandera y sin calificativo
  experimental, con madurez formal `stable` acotada al contrato implementado (`R-a`; no declara
  `R-c` ni A-DXF1); las denominaciones visibles de la exportación DXF existente pierden
  «experimental» sin cambiar sus algoritmos ni garantías; las referencias visibles se presentan en
  las salidas gráficas de Gráficos 1; información ordinaria compacta con detalles aparte.
- Q-DXF-3 queda resuelta por el autor en ese sentido (registrada en el contrato, §10 y §17).

## 3. Límites

- A-DXF0 **no** se registra como `PASS — AUTHOR APPROVED` en este registro: la aprobación de
  cierre y la publicación de la segunda ola requieren la instrucción autoral sobre el SHA
  integrado exacto.
- Diferido: Gráficos 2; persistencia `R-c`; exportación DXF de referencias; A-DXF1; madurez formal
  de `export.dxf-2d`, que sigue `experimental` con su bandera.
- La aceptación técnica es un hecho independiente de estas decisiones.
