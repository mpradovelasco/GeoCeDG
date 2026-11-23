# PRE-G9B-MAINT-P0 — Expediente de planificación del mantenimiento previo a G9B

| Campo | Valor |
|---|---|
| Carácter | **Expediente de planificación para revisión autoral.** No aprueba nada, no abre G9B ni ninguna fase de producto y no autoriza implementación. `implementationAuthorized=false`; `selfApproved=false` |
| Estado | `PLANNING DOSSIER — PENDING AUTHOR REVIEW` (revisión 2, consolidada como candidato de planificación) |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0. Planificación del mantenimiento científico y funcional previo a G9B» (2026-11-23) y enmiendas autorales finales del expediente antes del commit (2026-11-23) |
| Base | `main` = `origin/main` = `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2), árbol `8c1d4ca163b214898ec392bd9d02f04c955661d1`; etiqueta `v1.2.2` → `1b54ff95dc1d72e3acef29035b2defd1ed8658e1`; comprobado en `origin` el 2026-11-23 |
| Rama de trabajo | `phase/pre-g9b-maint-p0-planning`, local, creada en la base |
| Sin tocar | código, pruebas, selecciones, especificaciones y ADR aprobados, roadmap, `main`, etiquetas, releases y el libro `geocedg_book` |

## Registro de revisiones

| Revisión | Fecha | Contenido |
|---|---|---|
| 1 | 2026-11-23 | Expediente inicial A–G presentado al autor |
| 2 | 2026-11-23 | Enmiendas autorales finales incorporadas a A–G: arquitectura B-spline preparada para NURBS, importación DXF `SPLINE` racional de referencia y aproximación B-spline como objetivos explícitos, secuencia A-N1, A-DXF0, A2, A-DXF1, A3 y A-R; precisiones sobre B5a y B1; S1 y S2, I2G0, U2 y F8. Las sondas, sus resultados y la historia de las recomendaciones de la revisión 1 se conservan en cada documento |

## 1. Entregables

| | Documento | Contenido |
|---|---|---|
| A | [Informe de caracterización](pre_g9b_maint_p0_characterization_report.md) | Estado real de cada capacidad con evidencia de código y pruebas, solapamientos, carencias, riesgos y dos sondas de esta fase |
| B | [Propuesta de B-splines, NURBS e interoperabilidad](../architecture/pre_g9b_maint_p0_bspline_proposal.md) | Definición matemática, comparación con SplineV2, alternativas, arquitectura preparada para NURBS, primera implementación polinómica, exportación `SPLINE`, importación DXF de referencia, aproximación B-spline, NURBS nativa diseñada, secuencia y validación |
| C | [Plan correctivo de las limitaciones de 1.2.2](../architecture/pre_g9b_maint_p0_limitations_plan.md) | B1–B5: causa, impacto, intervención mínima u opciones, riesgo, coste, pruebas, cierre; prioridad |
| D | [Diseño preliminar revisado de CU7](../architecture/pre_g9b_maint_p0_cu7_revised_design.md) | U2, dictamen de F8, sustitución explícita en lugar de M09, viabilidad de I2G, capacidades diferidas |
| E | [Roadmap candidato del programa](../architecture/pre_g9b_maint_program_plan_candidate.md) | Incrementos, clases de trabajo, dependencias, olas, puertas, verificación, esfuerzo relativo, deudas diferidas |
| F | [Hoja de decisiones autorales](pre_g9b_maint_p0_author_decision_sheet.md) | Solo lo que exige al autor, con recomendación por defecto y alternativas |
| G | [Prompts candidatos](../architecture/pre_g9b_maint_p0_candidate_prompts/README.md) | Catorce prompts por incremento, no autorizados y fuera de la capa canónica |

## 2. Hallazgos y orientaciones principales

1. **B-splines y NURBS.** SplineV2 es una familia interpolante y su contrato aprobado excluye
   las B-splines de control. Se propone una familia semántica nueva, independiente de
   SplineV2 y concebida desde el origen como familia racional en general, cuya primera
   implementación es el subconjunto polinómico con pesos unitarios implícitos, publicado como
   `GeoLocusV2`. Hereda casi todo el aparato de Locus V2 sin tocar SplineV2. La NURBS
   semántica nativa queda diseñada y no autorizada; el núcleo no recibe interfaces racionales
   generales antes de necesitarlas.
2. **Interoperabilidad DXF.**
   - Importación de solo lectura de entidades `SPLINE` 2D polinómicas y racionales como
     geometría de referencia, fuera del núcleo y sin semántica CeDG implícita.
   - Construcción explícita de una B-spline polinómica aproximante, con conversión exacta
     cuando la fuente es polinómica y tres medidas de error distinguidas; la cota global
     certificada tiene un procedimiento demostrable.
   - Exportación `SPLINE` de los datos autoritativos de la familia.

   Geometría autoritativa, referencia importada, aproximación y render se mantienen
   separadas.
3. **Un escalar erróneo admitido en 1.2.2.** La sonda P-1 confirma que una ruta condicionada
   que cruza un salto alineado con la cuerda se admite como número `ESTIMATED` que incluye la
   longitud del salto: un incumplimiento de la hipótesis H3 no detectado. Es la prioridad
   científica (`MAINT-B5a`), con una guarda que es comprobación numérica conservadora y no
   prueba universal.
4. **Las otras limitaciones de 1.2.2:**
   - B2 (huérfano tras redefinir) tiene una intervención focal con enmienda de §29;
   - B1 (total condicionado) queda con decisión normativa pendiente entre tres opciones; la
     recomendación técnica es calcular el subtotal solo a petición, sin perder el dato;
   - B3 no tiene trabajo repetido dentro de la política vigente: se recomienda aceptarlo como
     límite, con una mejora opcional sin cambio normativo;
   - B4 no tiene caso constructivo demostrado.
5. **U2** es solo de interfaz y prioritaria: la fachada del núcleo ya es única.
6. **F8** no aporta una construcción nueva; podría aportar una ruta interactiva que falta.
   La carencia es una inferencia por traza de código, no una observación: depende de una
   prueba discriminante sobre un locus mixto real antes de cualquier incremento.
7. **Sustitución explícita en lugar de M09.** La reformulación simplifica el objetivo, no los
   dependientes:
   - S1 es un incremento de transparencia y seguridad; S2, que preserva los dependientes
     compatibles con dependencias actualizadas y evidencia revalidada, es el objetivo
     funcional principal;
   - ninguno rebaja la coherencia del DAG ni acepta publicación parcial confiando en Undo;
   - antes de aprobar la implementación se caracterizan las referencias por nombre, las
     fronteras de transacción y las condiciones de restauración (`MAINT-S0`).
8. **I2G.** No existe una operación segura para separar una salida. `MAINT-I2G0` es una
   barrera basada en el riesgo demostrado para el productor y su grupo, no en el número de
   salidas: impide la destrucción silenciosa de identidades y dependencias CeDG, incluida la
   de un hermano fijada hoy por una prueba, sin rechazar operaciones Classic legítimas, y
   empieza por caracterizar ambos comportamientos. Conservar los hermanos con el productor
   original no es viable en este programa.
9. **M08-D e I3** siguen expresamente diferidos.

## 3. Condiciones de cierre de P0

| Condición del encargo | Dónde se responde |
|---|---|
| Decidir la arquitectura de B-splines sin comprometer SplineV2, preparada para NURBS | B §0, §4 y §5; F, D-A-1 a D-A-5 |
| Importación DXF racional de referencia y aproximación B-spline como objetivos explícitos | B §8, §9 y §11; E §2-§3; F, D-A-6 a D-A-10; prompts `maint-a-dxf0` y `maint-a-dxf1` |
| Priorizar las cinco limitaciones de 1.2.2 | C §0; F, D-B-1a a D-B-4 |
| Autorizar la mejora U2 | D §1; F, D-U2; prompt `maint-u2` |
| Resolver si F8 tiene sentido | D §2; F, D-F8; prompt `maint-f8` |
| Aprobar o rechazar una propuesta concreta y simplificada de sustitución M09 | D §3; F, D-M09-1 a D-M09-4; prompts `maint-s0-s1` y `maint-n3-s2` |
| Determinar el alcance técnicamente viable de I2G | D §4; F, D-I2G; prompt `maint-i2g0` |
| Disponer de una secuencia implementable sin abrir investigación innecesaria | E §2-§4; F, D-ORD |

## 4. Límites respetados y lo que no se hizo

- Ninguna implementación productiva. Las dos sondas se ejecutaron desde el directorio
  temporal de la sesión, sin versionarse; los resultados de pruebas previos de
  `build/test-results` se respaldaron y restauraron.
- No se modificaron especificaciones ni ADR aprobados, el roadmap, `AGENTS.md`, `CLAUDE.md`,
  `.github/prompts/**` ni el libro. Los prompts candidatos están fuera de la capa canónica a
  propósito.
- Verificación: el expediente se confirma en un commit documental en la rama de trabajo y
  `tools/agent/verify.ps1 -Profile STATIC` se ejecuta sobre ese commit exacto. Su
  identificador y su resultado se comunican al autor junto con el SHA; no pueden constar en
  el propio commit que verifican. No se ejecuta `FINAL`.
- No se registró P0 en el roadmap (decisión D-0).
