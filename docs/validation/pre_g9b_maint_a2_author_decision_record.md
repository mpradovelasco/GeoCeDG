# PRE-G9B-MAINT-A2 — registro de las decisiones autorales

**PRE-G9B-MAINT-A2 — SMOKE AUTORAL PASS, CD-1 OPCIÓN A ADOPTADA, CIERRE `CORE + FOCUSED` ACEPTADO — CIERRE PENDIENTE DE LA INTEGRACIÓN DE LA SEGUNDA OLA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe; el agente no aprueba nada |
| Fecha de la decisión | 25 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre de segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §4.2 |
| Base común | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` |
| Candidato | `c63b10aade4f2792c855c1bc4ad6278f9a54fcfc`, árbol `602e70b7a17e9e50c7d56504acab6799ac8ca35c`, rama `phase/pre-g9b-maint-a2` |
| Contrato | `bspline-2d/v1` (`geocedg/specs/curves/bspline-2d.md`), ADR 0043 |
| Verificación del candidato aislado | `STATIC` `verification-3ed8d4fa0f11481cb878d49eee90ecaf`, `PHASE` `verification-41dfd9fa847f46a09d83baaff0efe37a`, `INTEGRATION` `verification-496a70d0ef6841a9bfb2793ffd692525`, todas `ACCEPTED / COMPLETE` (`CORE + FOCUSED`) |

## 1. Decisiones

```text
AUTHOR_SMOKE = PASS — AUTHOR REPORTED
```

El smoke lo ejecutó el autor y comunicó su resultado; el agente no lo ha ejecutado ni verificado.

- **CD-1, opción A (adoptada por el autor):** interfaz opcional de derivada unilateral
  (`OneSidedLocusDifferentialEvaluator2D`); en el extremo superior de integración de cada pieza
  se usa la derivada del lado izquierdo; se preservan los resultados de las demás familias, en
  especial SplineV2 (dorado BS-13 bit a bit).
- **Nivel de cierre:** el autor acepta expresamente el cierre con `CORE + FOCUSED`, con las
  suites completas compartida y Desktop como diagnóstico, **sin exigir `FINAL`** en esta segunda
  ola. Resuelve el `VERIFICATION_ESCALATION_REQUEST` de A2 como dispensa de `FINAL` para la ola.
- **`κ_max = 2^16`:** sigue siendo umbral candidato conforme al contrato y a las pruebas, con la
  regla inactiva en producción. No es un valor universalmente certificado.
- **BS-24:** conserva la limitación de aplicabilidad de la procedencia hasta el contrato de
  A-DXF1. El bloque de procedencia no expone garantías vigentes después de una modificación
  incompatible de la definición.

## 2. Límites

- Estas decisiones no aprueban por sí solas el cierre ni la publicación de la segunda ola, que
  requieren la instrucción autoral sobre el SHA integrado exacto.
- Notas de versión que acompañan a la ola: los lectores ≤ 1.2.2 rechazan un documento con
  `BSpline`; colisión K15 (una macro de usuario llamada `BSpline` oculta el comando nativo).
- Deudas declaradas en el informe consolidado (§H): por encima de 64 tramos responde la ruta del
  evaluador; sin certificados (A4); aplicabilidad BS-24 pendiente de A-DXF1.
- La aceptación técnica es un hecho independiente de esta decisión.
