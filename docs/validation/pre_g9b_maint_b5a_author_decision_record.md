# PRE-G9B-MAINT-B5a — registro de las decisiones autorales

**PRE-G9B-MAINT-B5A — POLÍTICA NUMÉRICA APROBADA POR EL AUTOR — L-6 CORREGIDA POR B5a-R1 — CIERRE PENDIENTE DE LA AUTORIZACIÓN DE LA SEGUNDA OLA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe; el agente no aprueba nada |
| Fecha de la decisión | 25 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre de segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §4.1 |
| Base común | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` |
| Candidato | `65ca487da35d34d500fb12e5d5c815e03eca5754`, árbol `31ee1a88242ac3790030053e9cfe367281e64bfc`, rama `phase/pre-g9b-maint-b5a` |
| Norma aplicada | `geocedg/specs/locus/locus-v2-metrics.md` §28.3.1 (adoptada en N1, `67663ceee`); B5a no modifica su texto |
| Verificación del candidato aislado | `STATIC` `verification-63909aac9da14f2181c0adf302a955b8`, `PHASE` `verification-f4b3e898806a494bbc1a8fabc1f779be`, `INTEGRATION` `verification-9bad09f9b51e494a9ae1cfd7d27e8395`, todas `ACCEPTED / COMPLETE` (`CORE + FOCUSED`) |

## 1. Decisión

El autor aprueba como **política numérica de implementación** de `conditioned-route-jump-guard/v1`:

| Parámetro | Valor |
|---|---|
| `ρ_J` | `0,90` |
| `m` | `3` |
| `L` | `6` |
| Factor de resolución | `2^8` |
| `E_cap` | `16 384` |

Acepta las limitaciones declaradas, las posibles negativas conservadoras, el coste adicional
(+60–80 % por ruta condicionada) y la sensibilidad numérica bajo traslaciones grandes (rango
mostrado `≤ 10^5`).

Precisión autoral: **la firma de escalado no constituye prueba de discontinuidad ni
certificación de continuidad.** Un `CHORD_SCALING_JUMP_SIGNATURE` solo motiva la negativa
conservadora del escalar.

## 2. Lecturas L-1 a L-9 del informe focal

Auditadas contra §28.3.1 (auditoría del coordinador, 2026-11-25). Ninguna se eleva a norma.

| Lectura | Clase | Registro |
|---|---|---|
| L-2, L-3, L-7, L-9 | lectura fiel del texto | sin efecto normativo |
| L-1, L-4, L-8 | elección de implementación dentro del margen de la norma | se registran como política de implementación, no como norma |
| L-5 | la negativa es la que exige la condición 8; la causa `CHORD_SCALING_INCONCLUSIVE` añade una tercera causa a las dos que enumera la norma | la causa no se eleva a norma; hoy no se activa en producción |
| L-6 | **alteraba el contrato**: un fallo de evaluación de la comprobación conservaba la estimación del resultado rico con estado `SUCCESS`, mientras §28.1 califica ese fallo como «fallo del resultado, nunca un valor» | **no se eleva a norma**; corregida por la corrección focal autorizada B5a-R1 (§2.1) |

## 2.1 Corrección focal B5a-R1 (L-6)

Autorizada por el autor el 2026-11-25 («GeoCeDG — PRE-G9B-MAINT-B5a-R1. Corrección focal L-6 y
reconstrucción del candidato integrado»). Candidato `faebbe3aac7cc79556547acef84f1263a7bea106`,
árbol `00d8e3a6140620944c59037cd7153c9ff8d15c74`, rama `phase/pre-g9b-maint-b5a-r1` (gobernanza
`203cc4ebe`, técnico `5605357b9`, inventario `faebbe3aa`), descendiente de `65ca487da`, que se
conserva. Un fallo de evaluación de la comprobación dentro del tramo es ahora un
`NUMERICAL_FAILURE` con valor ausente y causa `EVALUATION_FAILED` trazable (hoja, parámetro,
estado y diagnóstico de la evaluación), con el mecanismo tipado existente; la firma, la hoja
inconclusa y el presupuesto conservan sus reglas. Sin cambios en parámetros, política, H1 a H4,
rutas admitidas, recorrido, identidades, serialización, SplineV2, A2 ni A-DXF0-R1.

| Perfil sobre `faebbe3aa` | Ejecución |
|---|---|
| `STATIC` | `verification-a37764a3da014943b6983d8b556f812c` |
| `PHASE PRE-G9B-MAINT-B5A` | `verification-351b03cb637b4cada65e2bbd2d68af34` |
| `INTEGRATION` | `verification-f865f2b10e4e4b51b1a29bb880463a8f` |

Todas `ACCEPTED / COMPLETE` (`CORE + FOCUSED`). Informe:
[pre_g9b_maint_b5a_r1_candidate_report.md](pre_g9b_maint_b5a_r1_candidate_report.md).

## 3. Límites

- Esta aprobación de parámetros no aprueba por sí sola el cierre ni la publicación de la segunda
  ola, que requieren la instrucción autoral sobre el SHA integrado exacto.
- La aceptación técnica es un hecho independiente de esta decisión.
- El informe focal y sus ejecuciones son artefactos congelados y no se reescriben; las
  imprecisiones de redacción señaladas por la auditoría (§2, D-B5a-6) se registran aquí.
