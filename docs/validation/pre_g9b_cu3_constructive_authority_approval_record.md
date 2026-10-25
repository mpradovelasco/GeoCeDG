# PRE-G9B-CU3 — Registro de la aprobación autoral de la autoridad constructiva y de la implementación por incrementos

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de aprobación de la arquitectura y de la normativa de la autoridad constructiva y de la autorización de su implementación. No aprueba ninguna implementación todavía no ejecutada y no declara `CU3` `PASS — AUTHOR APPROVED` |
| Instrucción | «PRE-G9B-CU3 — Aprobación autoral de la autoridad constructiva e implementación por incrementos», decisión del autor de 25 de octubre de 2026: **ARQUITECTURA Y NORMATIVA APROBADAS — IMPLEMENTACIÓN AUTORIZADA CON PUERTAS BLOQUEANTES** |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Base autorizada | `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, árbol `9e4984db1dec327e47afeec2c1072f78c6e9cc49` (conciliación publicada en `phase/pre-g9b-cu3-authority-study`) |
| `main` | `e6c908f2203cd8feb5d4938013e16b212e0fe016`, cierre de `CU2`; `CU0`, `CU1` y `CU2` siguen `PASS — AUTHOR APPROVED` y sus contratos no se reabren |
| Rama de implementación | `phase/pre-g9b-cu3-implementation`, local; avanzó por avance rápido de `1a0903aa` a la base, y conserva en su historia la evidencia de la puerta G-M3 |
| Expediente aprobado | [propuesta normativa candidata](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md), consolidada y conciliada, con su §14; [estudio de la autoridad constructiva](../architecture/pre_g9b_cu3_constructive_authority_study.md); [evidencia del certificado de identidad constructiva](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json) |
| Registro anterior | [aprobación del diseño de `CU3`](pre_g9b_cu3_author_design_approval_record.md) (2026-10-24), que sigue vigente salvo en lo que revisa D-AUT-11 bajo la política constructiva |
| Prompt canónico | [`pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md`](../../.github/prompts/tasks/pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md), actualizado en E0 |
| `selfApproved` | `false` |

El autor revisó favorablemente la conciliación publicada en
`3cd0f710c370c3985ea536f1cc5edcadeca930ee` y aprobó como arquitectura normativa de GeoCeDG la
combinación A2+B2+C+D. Este registro transcribe y ordena su instrucción; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Identidad y puerta de entrada

| Comprobación | Resultado |
|---|---|
| SHA, árbol y ascendencia | `3cd0f710` existe; su árbol es `9e4984db…`; desciende de `1a0903aa` (T2 y evidencia de G-M3), de `80d72a50` (preparación) y de `main` `e6c908f2` |
| Rama publicada | `phase/pre-g9b-cu3-authority-study` apunta en `origin` a `3cd0f710` |
| Rama de implementación | `phase/pre-g9b-cu3-implementation` avanzó por avance rápido de `1a0903aa` a `3cd0f710`; en `origin` sigue en `1a0903aa` |
| Espacio de trabajo | limpio |
| `main` | local, `origin/main` y el remoto vivo en `e6c908f2`, sin divergencia; 73 referencias de etiqueta remotas, sin cambios |
| Producto en la base | entre `e6c908f2` y `3cd0f710`, la única diferencia bajo `source/` es la prueba de caracterización de T1 (`PreG9BCu3BaseCharacterizationTest`) |
| Autoridad leída | `AGENTS.md`, prompts canónicos de gobierno y verificación, hoja de ruta, prompt y registros de `CU3` |
| Selección de entrada `PRE-G9B-CU0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` sobre la base: `verification-18934b2d646347d7b2b16444a509e8b0`, resultado `b814963c9265327b57d18811e5a5a75cf84c69eedbd58334fbb958995ced6293`, `ACCEPTED / COMPLETE` 2/2, re-autenticado |
| Selección de entrada `PRE-G9B-CU1` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1` sobre la base: `verification-04f7e2b43c854d1ea761bca245db28a4`, resultado `2e4744111a91b6f501214e5ac77f039b3d6f32c3d310b3ad2589d4e7fe1873e7`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| Selección de entrada `PRE-G9B-CU2` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2` sobre la base: `verification-5436c43f4c494a07bb098abb3a79535b`, resultado `d678bda2268a6bbdcffba40271fb1e1a3759ccbd728bdf891190a7c3251c96e7`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |

Las tres selecciones se ejecutaron sobre la base exacta, en la rama de implementación todavía sin
cambios, para disponer de una referencia de regresión atribuible.

## 2. Aprobación de la arquitectura y de la normativa

1. **Arquitectura normativa.** A2 (autoridad geométrica constructiva ideal), B2 (certificación
   estructural de relaciones), C (revisión coordinada, versionada y mínima de los contratos de
   autoridad) y D (integridad geométrica `STRICT`, `RELAXED`, `NOT_ESTABLISHED` y `CONTRADICTED`,
   independiente de la precisión numérica y propagada por premisas).
2. **Decisiones.** D-AUT-1 a D-AUT-12, conforme a la propuesta normativa consolidada y conciliada,
   incluida su §14 (§3).
3. **ADR 0040**, aprobado como decisión arquitectónica para su incorporación normativa en E0.
4. **Textos AUT-1 a AUT-7**, la especificación nueva `locus-v2-constructive-authority.md` y las
   enmiendas versionadas a los contratos de Locus V2 y a las decisiones de `CU3` identificadas en
   la propuesta §7.
5. **Validación.** La ampliación V-AUT de la matriz de validación.
6. **Estrategia.** Los incrementos E0 a E4 y sus puertas.

La aprobación no convierte los resultados del prototipo en implementación productiva verificada.

| Destino | Versión | Sección | Textos |
|---|---|---|---|
| `locus-v2-constructive-authority.md` (nueva) | 1.0 | §1 a §11 | AUT-1, AUT-2, AUT-3, AUT-5 y AUT-6, con las reglas operativas de D-AUT-3, D-AUT-5 y D-AUT-8 |
| `locus-v2-certified-construction-model.md` | 1.4 | §12 | AUT-4 (§12.2) y AUT-7 (§12.1), con la referencia a AUT-6 (§12.3) |
| `locus-v2-intersections.md` | 1.4 | «Constructive authority amendment (version 1.4)» | AUT-7, y las revisiones de CU3.1 (c), CU3.2 y CU3.4 bajo la política constructiva |
| `locus-v2-existence-components.md` | 1.1 | §17 | nota de consumidor de AUT-4 |
| ADR 0040 | — | — | `ACCEPTED — ARCHITECTURE AUTHOR APPROVED (2026-10-25)` |
| ADR 0008, 0013, 0017, 0028, 0037 y 0038 | — | cabecera | línea «Amended by» que remite al ADR 0040; su texto de decisión no cambia |
| ADR 0039 | — | cabecera | línea que declara la sustitución de sus cláusulas de autoridad por el ADR 0040, solo bajo la política constructiva |

Las secciones aprobadas de fases anteriores no se reescriben: cada contrato recibe una sección
nueva, que solo rige en los documentos con `constructive-authority/v1`.

## 3. Decisiones D-AUT-1 a D-AUT-12

| Decisión | Disposición autoral |
|---|---|
| D-AUT-1 | APPROVED, (a): A2 como autoridad normativa por política de documento, ADR 0040 |
| D-AUT-2 | APPROVED, (a), conciliada: adopción solo explícita, en documentos nuevos y existentes, sin documentos mixtos, con fallo cerrado de los lectores anteriores; la promoción a predeterminada queda planificada y no autorizada |
| D-AUT-3 | APPROVED, (a) y (a): puntos sobre caminos por la aplicación de camino heredada sobre el camino ideal; decisiones numéricas heredadas por las categorías D1 a D5 y las reglas P1 a P6 |
| D-AUT-4 | APPROVED, (a): transferencia de `CU2` por las condiciones N1 a N3, con `EVIDENCE_INSUFFICIENT` y fallo cerrado |
| D-AUT-5 | APPROVED, (a), conciliada: `SUBSTITUTE_GENERIC` versión 1 y `ENCLOSURE_COMPATIBLE` solo para raíces con cambio de signo |
| D-AUT-6 | APPROVED, (a): recalificación de G8B-R1 una sola vez, en la adopción |
| D-AUT-7 | APPROVED, (a): `Intersect(L, r, D)` con `P ∈ r` `RELAXED` e integridad visible desde E1 |
| D-AUT-8 | APPROVED, (a), conciliada: certificado de identidad constructiva v1 con la clave `cic-structural-key/v1` por identidad durable |
| D-AUT-9 | APPROVED: tipo propio `RelationIntegrity`, sin persistir y fuera de los tokens |
| D-AUT-10 | APPROVED: E0 a E4 con sus puertas; `INTEGRATED_PHASE`; la rama detenida avanza al commit aprobado |
| D-AUT-11 | APPROVED: revisiones de D-CU3-3, -4, -5, -6, -7, -10 y -13, de G-M3-4, de CU3.1 (c), de CU3.2, de CU3.4 y de §11.4, según la propuesta §7 |
| D-AUT-12 | EJECUTADA: la publicación de la rama del estudio para su auditoría se hizo hasta `3cd0f710`; no autoriza nuevas publicaciones |

## 4. Base y ejecución

- Se reanuda `CU3` desde T2 sobre el commit exacto `3cd0f710c370c3985ea536f1cc5edcadeca930ee`.
- La rama de implementación avanza de forma controlada desde su estado detenido, sin perder la
  evidencia histórica de G-M3.
- E0 incorpora los contratos aprobados, registra esta decisión y actualiza expresamente el prompt
  canónico de implementación antes de modificar código productivo.
- Después se autoriza la ejecución autónoma de E1, E2, E3 y E4, respetando sus dependencias,
  presupuestos y condiciones de parada.

## 5. Integridad geométrica

- Cada objeto tiene una única autoridad geométrica conforme a la política versionada del
  documento.
- La identidad constructiva, la integridad de las relaciones y la precisión de sus representaciones
  son dimensiones independientes.
- `RELAXED` es una admisión computacional controlada, no una prueba de incidencia exacta.
- Ninguna relación contradicha rigurosamente respecto de la autoridad vigente puede admitirse como
  `RELAXED`.
- La integridad se propaga exclusivamente por las premisas que utiliza cada demostración.

## 6. Condiciones obligatorias de E1

- E1 debe demostrar rigurosamente la transferencia de los certificados de `CU2` que utilizan las
  construcciones nuevas.
- Los encierros de transferencia deben contener los valores geométricos ideales, incluidos los que
  proceden de puntos sobre caminos, de construcciones trigonométricas y de objetos externos a la
  clase exacta. Una estimación de error o una caja numéricamente amplia no sustituye esa
  demostración.
- Si la transferencia no queda establecida, `P ∈ L` no puede publicarse como `STRICT`.
- `SUBSTITUTE_GENERIC` se implementa conforme a la versión 1 conciliada, con sus pruebas
  adversarias y sin inferencias de identidad a partir de muestras finitas.

## 7. Condiciones obligatorias de E2

El certificado de identidad constructiva debe demostrar las cinco incidencias obligatorias bajo la
autoridad constructiva, con:

- identidad algebraica establecida;
- precondiciones rigurosas;
- correspondencia certificada de las ramas;
- valores ideales contenidos en las cajas;
- identidad durable y clave estructural coherentes;
- validez dinámica por revisión;
- fallo cerrado en las degeneraciones y en los casos no decididos.

El objetivo obligatorio es que las dos incidencias de cada intersección alcancen `STRICT` cuando la
construcción y sus condiciones lo justifican. Si E2 no supera sus puertas, E1 puede presentarse
como candidato parcial verificado, pero `CU3` permanece incompleto.

## 8. Compatibilidad

- Los documentos antiguos conservan su autoridad y su admisibilidad hasta una adopción explícita.
- Los documentos nuevos también empiezan bajo la política heredada durante `CU3`.
- La adopción constructiva es explícita, atómica, reversible mediante Deshacer y acompañada de un
  informe de impacto.
- No se modifican ni se reasocian identidades, tokens, selectores ni claves de linaje.
- Los certificados de `CU2` conservan íntegramente su autoridad original sobre la configuración
  capturada. La transferencia constructiva es evidencia nueva y separada.
- La futura promoción de la autoridad constructiva a comportamiento predeterminado queda
  planificada, pero no autorizada por esta decisión.

## 9. Validación

- Se ejecutan todas las puertas G-AUT aplicables, G-A, G-B y G-final.
- Las pruebas ejercitan las garantías matemáticas, la propagación de la integridad, la
  persistencia, la identidad durable, las degeneraciones y los controles adversarios.
- El candidato técnico se congela antes de la verificación `INTEGRATED_PHASE`.
- Un resultado automatizado `ACCEPTED / COMPLETE` no equivale a la aprobación autoral, y la
  comprobación manual del autor sigue siendo obligatoria.

## 10. Autonomía y condiciones de parada

El autor autoriza la ejecución de los incrementos aprobados sin confirmación para las decisiones
menores de implementación. El agente se detiene ante:

- una puerta bloqueante no superada;
- una contradicción científica;
- la necesidad de modificar contratos aprobados fuera de AUT-1 a AUT-7;
- un riesgo de reasociación o de pérdida de identidad;
- una incompatibilidad con documentos anteriores;
- la necesidad de aprobar un presupuesto normativo nuevo;
- la imposibilidad de demostrar una garantía obligatoria.

Las pruebas fallidas no se sustituyen por tolerancias, y la autoridad geométrica no se reinterpreta
en silencio.

## 11. Fuera de alcance

No se autoriza:

- la promoción de la autoridad constructiva a predeterminada;
- la modificación de la semántica de los comandos heredados;
- la ampliación general del certificado de identidad constructiva fuera de su clase aprobada;
- el inicio de `CU4`–`CU10` o de `G9B`;
- modificaciones de `geocedg_book`;
- la integración en `main`, la creación de etiquetas, releases o packaging.

## 12. Continuidad editorial

El registro E1 mantiene la planificación de una exposición científica amplia y comprensiva de estos
resultados para `CU10`, con el fundamento matemático, los ejemplos constructivos y la distinción
entre integridad geométrica y precisión numérica. `CU10` sigue sin iniciar ni autorizar.

## 13. Entrega final

Un candidato técnico congelado con:

- la identidad Git exacta;
- el estado de E0 a E4 y de todas las puertas;
- los resultados y las evidencias rigurosas;
- el estado de las dos incidencias en los cinco contactos;
- la compatibilidad y la persistencia;
- las degeneraciones y las limitaciones;
- el coste computacional y los presupuestos aprobados;
- las pruebas manuales pendientes.

`CU3` no se declara `PASS — AUTHOR APPROVED` sin una decisión autoral posterior.

## 14. Presupuestos versionados

La aprobación no fija los valores de los presupuestos versionados que la implementación necesita.
El contrato los deja al autor (`locus-v2-certified-construction-model.md` §11.4, condición 2;
`locus-v2-constructive-authority.md` §10), y su necesidad es una condición de parada (§10). Se
presentan al autor, con sus contadores deterministas, para una única decisión antes del código que
los usa:

| Presupuesto | Uso | Evidencia |
|---|---|---|
| `cu3-exact-evaluation-budget/v1` | cada evaluación exacta de producto y cada punto de `SUBSTITUTE_GENERIC` | propuesta de la puerta G-M3: 100 000 operaciones, profundidad 8 y 4 096 bits ([informe](pre_g9b_cu3_m3_gate_report.md) §6), sin aprobar |
| presupuesto del certificado de identidad constructiva | cada intento de prueba y cada recomprobación | contadores del prototipo ([evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json)) |

## 15. Registro del incremento E0

E0 registra esta decisión, incorpora los textos aprobados (§2), actualiza el prompt canónico y el
estado de la propuesta, de la estrategia, de la matriz, del plan consolidado, de la hoja de ruta y
del registro E1, y registra la publicación de `3cd0f710`, sin código productivo. E0 quedó en
`1f81666ea90f4ca53a9d782b4f905cc839c61cf1` (árbol `3e8b6f039ef2b4ac72d71b51773a3ef309f6fb2c`), con
`verify.ps1 -Profile STATIC` `verification-ac6df0aa06ce46689df9b4634c64e807`, resultado
`51877647cde299f8d9c229b6b3200d508552b8919a239cc2c72f03d4f0c69b42`, `ACCEPTED / COMPLETE`,
re-autenticado. Presentados los presupuestos (§14), el autor eligió primero detener E1 tras E0.

## 16. Decisión autoral sobre los presupuestos y continuación de E1

Instrucción «PRE-G9B-CU3 — Decisión autoral sobre presupuestos de cálculo y continuación de E1»,
de 2026-10-25. Se transcribe y se ordena; ante cualquier diferencia prevalece la instrucción.

**Evaluación exacta M3: `cu3-exact-evaluation-budget/v1`, aprobado.**

| Límite | Valor |
|---|---|
| Operaciones exactas | 100 000 |
| Profundidad de la torre | 8 |
| Bits | 4 096 |

- Se aplica por evaluación exacta de un punto estructural frente a su objetivo, conforme al
  contrato aprobado.
- Su agotamiento produce `EXACT_EVALUATION_BUDGET_EXHAUSTED`, con fallo cerrado, diagnóstico y
  contadores deterministas.
- No se amplían automáticamente los límites, y un agotamiento nunca se convierte en una
  aproximación aceptada.

**Certificado de identidad constructiva: `constructive-identity-certificate-budget/v1`, límites
iniciales de desarrollo y caracterización.**

| Límite | Valor |
|---|---|
| Productos de términos por intento | 16 000 000 |
| Indeterminadas | 16 |
| Términos por polinomio | 200 000 |
| Bits por coeficiente (provisional) | 4 096 |

- Son techos iniciales de ingeniería, no una declaración anticipada de suficiencia ni de
  rendimiento aceptado.
- Antes de su aceptación productiva en E2, G-AUT-8 debe medirlos, instrumentarlos completamente y
  validarlos con pruebas adversarias.
- La extensión del prototipo de nueve a dieciséis indeterminadas requiere una representación de
  monomios que soporte ese rango sin colisiones ni desbordamientos.
- Si el banco demuestra que algún límite es insuficiente, se presentan la evidencia y la
  modificación justificada para una decisión autoral; ningún límite se eleva en silencio.

**Continuación de E1.** Se autoriza continuar E1 sobre la base documental aprobada, conservando la
evidencia de E0, con todas sus condiciones, en especial:

1. encierros rigurosos de los valores ideales;
2. transferencia demostrada de los certificados de `CU2`;
3. autoridad geométrica única por documento;
4. integridad consultable y propagación por premisas;
5. admisión `RELAXED` exclusivamente conforme a su política versionada;
6. ninguna modificación silenciosa de la identidad, de la persistencia ni de los algoritmos
   heredados.

**Condiciones adicionales.**

- El defecto del prototipo del certificado sobre la pertenencia a segmentos se corrige, con pruebas
  adversarias, antes de usar el certificador correspondiente como autoridad.
- Los presupuestos se implementan en la capa correcta: los límites semánticos del cálculo en el
  núcleo compartido; la instrumentación, los bancos y los informes en sus capas externas.
- La autorización no supone que E1 ni E2 estén verificados. Se mantienen las puertas y las
  condiciones de parada aprobadas, y `CU3` no se declara `PASS — AUTHOR APPROVED` sin una decisión
  autoral.
- No se publica en `main` ni se inician fases posteriores fuera del alcance autorizado.
