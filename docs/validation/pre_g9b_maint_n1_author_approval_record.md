# PRE-G9B-MAINT-N1 — registro de la aprobación autoral

**PRE-G9B-MAINT-N1 — NORMA ADOPTADA POR EL AUTOR — NO IMPLEMENTADA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe; el agente no aprueba nada |
| Fecha de la decisión | 24 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre autoral coordinado de N1, A-N1 y U2-R1, integración y promoción a `main`», entregada por el autor en la conversación |
| Naturaleza | Adopción de una enmienda normativa documental. Implementación: **no autorizada** |
| Base común | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` |
| Candidato aprobado | `67663ceeefe9613dc229ba25da43af8e71557b1b`, árbol `6920d582594a5620918b55f5ed37f131fccc7983` (revisión 2), rama `phase/pre-g9b-maint-n1-metrics-amendment` |
| Clase de verificación | `DOCUMENTATION_STATUS_ONLY`; `STATIC` |
| Resultado técnico | `STATIC` `verification-f2254dd896d541c683c22cf7b2f85a52`, `ACCEPTED / COMPLETE` sobre el candidato exacto (registro `artifacts/agent/pre-g9b-maint-n1-r2-static-67663cee/`) |
| Integración | Primera ola coordinada; véase el [registro de integración](pre_g9b_maint_first_wave_integration_record.md) |

## 1. Decisión

El autor:

- adopta la enmienda normativa §28.3.1 de
  [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) (comprobación de escala
  de cuerdas, B5a), incluida la revisión 2 sobre la interpretación conservadora de las firmas
  numéricas, la resolución y las traslaciones;
- adopta §29.1 (retirada al final de una redefinición, B2-F1), incluida la atomicidad de B2-F1
  ante un fallo posterior a la sustitución del número;
- aprueba N1 como incremento normativo documental.

El texto adoptado es el del candidato. El
[registro de la enmienda](pre_g9b_maint_n1_metrics_amendment_record.md) conserva su redacción
de candidato; este registro es la única fuente de la aprobación.

## 2. Límites

- Los parámetros numéricos de B5a (`ρ_J`, `m`, `L`, `ε_res`, `E_cap` y sus intervalos
  admisibles) siguen siendo candidatos hasta su validación focal en `MAINT-B5a`.
- La adopción de la norma no equivale a implementación. El producto conserva su conducta actual
  hasta que `MAINT-B5a` y `MAINT-B2` la implementen y validen. Ninguno de los dos está
  autorizado.
- Siguen fuera de alcance §28.2, B2-F2 (no se implementa, decisión de 2026-11-23), B5b, T-A e
  `Intersect`.

## 3. Observaciones aceptadas y obligaciones futuras

Las condiciones de entrada del registro de la enmienda pasan a ser obligaciones de los
incrementos posteriores: para `MAINT-B5a`, la medición de referencia, las pruebas de traslación
y la justificación del factor de resolución (§6 de ese registro); para `MAINT-B2`, la prueba de
reversión atómica o la detención para una decisión de arquitectura (§7).

## 4. Estado

Las cabeceras de `locus-v2-metrics.md` registran la adopción. No cambia código, pruebas,
selecciones ni inventarios. La aceptación técnica de `STATIC` es un hecho independiente de esta
aprobación autoral.
