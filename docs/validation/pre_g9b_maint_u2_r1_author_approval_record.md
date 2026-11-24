# PRE-G9B-MAINT-U2 y U2-R1 — registro de la aprobación autoral

**PRE-G9B-MAINT-U2-R1 — INCREMENTO DE PRODUCTO APROBADO POR EL AUTOR — CONDICIONADO A LA VERIFICACIÓN INTEGRADA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe; el agente no aprueba nada |
| Fecha de la decisión | 24 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre autoral coordinado de N1, A-N1 y U2-R1, integración y promoción a `main`», entregada por el autor en la conversación |
| Naturaleza | Aprobación de un incremento de producto Desktop (U2 con su corrección R1) |
| Base común | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` |
| Candidato aprobado | `f99a99785ffd5c733a98262888a48a8fad31ec0e`, árbol `dea0f32aebd2dd7915c1a8d044818c77eb39269b`, rama `phase/pre-g9b-maint-u2-r1` (worktree `GeoCeDG-wt-maint-u2`) |
| Ascendencia | U2: gobernanza `4d351b2bd`, técnico `98e21093d`; U2-R1: gobernanza `ae688c393`, técnico `f99a99785` |
| Clase de verificación | `STATIC` + `PHASE PRE-G9B-MAINT-U2` + `INTEGRATION` (`CORE + FOCUSED`, no exhaustiva); `FINAL` no autorizado |
| Integración | Primera ola coordinada; véase el [registro de integración](pre_g9b_maint_first_wave_integration_record.md) |

## 1. Resultados técnicos del candidato aislado

Todos sobre el candidato exacto `f99a99785`, `ACCEPTED / COMPLETE`:

| Perfil | Ejecución |
|---|---|
| `STATIC` | `verification-c7455ad1bdba4039967410d2adcde12c` |
| `PHASE PRE-G9B-MAINT-U2` | `verification-1ba8834cfcda4e8cabf404daed078feb` |
| `INTEGRATION` | `verification-910d92b6fa4e457d819ca0603df29ae0` |

Esos resultados pertenecen al candidato aislado y no se atribuyen al árbol integrado, que se
verifica por separado (registro de integración).

## 2. Decisión

El autor aprueba U2 junto con su corrección R1. Considera satisfactorios el panel Punto, las
ayudas, el flujo corregido de extremos métricos y la diferenciación de iconos.

```text
AUTHOR_SMOKE = PASS — AUTHOR REPORTED
```

El smoke de R1 lo ejecutó personalmente el autor y comunicó su resultado. El agente no lo ha
ejecutado ni verificado.

La aprobación como incremento de producto queda condicionada a la aceptación de la verificación
integrada (`STATIC`, `PHASE PRE-G9B-MAINT-U2` e `INTEGRATION`) sobre el candidato integrado.

## 3. Límites

- No se modifica el comportamiento del selector Clic/Referencia Q.
- Deudas fuera del cierre: Ctrl+clic con Punto sobre Locus V2; F8 (`MAINT-F8`, no autorizado);
  los mensajes genéricos anteriores al panel (rechazo de `GeoCeDGLocusV2Dialogs`); un eventual
  encadenamiento automático de la longitud; el manifiesto upstream que ya fallaba en la base,
  según los informes de [U2](pre_g9b_maint_u2_candidate_report.md) y
  [U2-R1](pre_g9b_maint_u2_r1_candidate_report.md).
- Los informes de candidato y las guías de smoke son artefactos congelados y no se reescriben.
- La aceptación técnica es un hecho independiente de esta aprobación autoral.
