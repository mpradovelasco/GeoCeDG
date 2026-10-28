# PRE-G9B-CU7-D — Conciliación normativa y candidato consolidado

| Campo | Valor |
|---|---|
| Encargo | «PRE-G9B-CU7-D — DECISIÓN AUTORAL SOBRE EL CANDIDATO», 2026-10-29 |
| Base revisada | `8e92b0d555385c47045571210f9421955625ff2f`, árbol `ea03514a24300b1dc706731f39e86100f66d203e`; limpio al entrar |
| Rama/worktree | `phase/pre-g9b-cu7-d-design`; `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG` |
| Estado del diseño | dirección D-CU7-01–11 aprobada con precisiones; D-CU7-12 aprueba diseño sujeto a conciliación |
| Estado de esta entrega | **CONSOLIDATED NORMATIVE CANDIDATE — PENDING AUTHOR REVIEW**; `selfApproved=false` |
| Implementación | D-CU7-13: **CU7-I NOT AUTHORIZED**; ningún incremento productivo iniciado |
| Clasificación/capa | documentación normativa candidata y planificación; semántica/ciclo de vida compartidos como objeto del diseño |
| Identidad de entrega | commit y árbol exactos informados en la entrega; sin hash autorreferente en este archivo |

## 1. Disposición autoral y conciliación explícita

Este registro aplica la decisión humana al estado documental, no concede al agente aprobación
del texto nuevo. Las recomendaciones originales quedan adoptadas como dirección con las cinco
precisiones siguientes; el texto normativo consolidado se presenta para revisión separada.

| Precisión autoral | Texto conciliado y consecuencia |
|---|---|
| 1. S/D distintas; S sin dependencia ficticia; D con prueba propia, diferible | ADR 0041, precisiones y decisiones 1–4; M08 §1–§5; I1/I4 de la propuesta. Fallar la clase afín difiere D, sin convertir estimación en correspondencia |
| 2. Actualización compatible atómica sin transportar raíz ni coincidencia; proteger roles/dependientes/grupos | ADR 0041 decisiones 5–9; M09 §1–§6; pruebas R01–R15/U01–U04. Rechazo/fallo conserva todo el estado de entrada |
| 3. Comenzar por singleton; I2G independiente, diferirlo no completa M09 | M09 §5; matriz R09 y orden I0→I2→I1; propuesta I2a/I2b e I2G. Salida singleton informa M09 parcial |
| 4. I3 solo dirección de diseño; contrato prospectivo/atomicidad/persistencia/barreras; rechazo tipado hasta implementación autorizada | ADR 0041 precisión I3; M09 §7; nueva prueba futura R16; contrato previo I3 §4 de la propuesta. Ningún PASS de I2 habilita I3 |
| 5. Autoridad documental/evidencia real; sin selección por proximidad, coordenadas o STRICT estimada | ADR 0041 y M08/M09; D07/R04–R07/K03/U03. Una consulta transitoria puede descubrir candidatos, pero no identifica ni reasocia objetos/raíces |

Las rutas hoy admitidas conservan su contrato. La exigencia de rechazo I3 se refiere a
peticiones que requieren la nueva operación coordinada aún no soportada; no introduce una
prohibición universal de todo cambio de conductor. La evidencia K5 y sus límites quedan
intactos y no se declaran resueltos mediante un cambio de documentación.

D-CU7-12 queda conciliada explícitamente en los cuatro destinos solicitados: ADR,
especificaciones, matriz y puertas. D-CU7-13 se conserva en todos ellos. No se marca este
descendiente como `PASS — AUTHOR APPROVED` ni se incorporan sus enmiendas a los contratos
productivos ADR 0019/0026 y especificaciones previas; el autor revisará la conciliación exacta.

## 2. Paquete revisable

- [ADR 0041 conciliado](../adr/0041-locus-v2-point-placement-and-point-redefinition.md).
- Especificaciones candidatas v2: [M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md)
  y [M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md).
- [Hoja de decisiones con disposición](pre_g9b_cu7_d_author_decision_sheet.md) y
  [matriz/puertas conciliadas](pre_g9b_cu7_d_validation_and_implementation_gates.md).
- [Propuesta concreta CU7-I](../architecture/pre_g9b_cu7_i_increment_proposal.md): entradas,
  dependencias, exclusiones, pruebas, gates y paradas por incremento. Primer encargo recomendado:
  **solo I2a+I2b (M09 singleton)**, pendiente de autorización sobre base exacta.
- [Arquitectura actualizada](../architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md);
  plan consolidado §2.32 y roadmap 5.102 reflejan el estado, sin reescribir sus antecedentes.

I2G sigue siendo necesario antes de completar el alcance multisalida de M09. I3 precisa su
contrato y autorización propios. I4 precisa demostrar su mapa afín y persistencia/lectores;
si no los supera, M08-D queda diferida explícitamente. Los nombres de APIs nuevas, codecs y
selecciones PHASE siguen siendo propuestas; no se presentan como interfaces existentes.

## 3. Evidencia y validación de esta conciliación

Solo documentación. Se usa `tools/agent/verify.ps1 -Profile STATIC` sobre candidato guardado,
con directorio nuevo `artifacts/pre-g9b-cu7-d/consolidation-20261029/`. El resultado/commit/árbol
canónicos se comprueban al entregar. No se reejecutan sondas, Gradle, PHASE, INTEGRATION,
FINAL ni smoke productivo: no hay cambios de código o comportamiento que lo requieran.
Se comprueban enlaces de archivos modificados/nuevos y `git diff --check`.

Comprobación focal guardada: 8 documentos de diseño/especificación y propuesta, 55 enlaces
relativos resueltos, 0 ausentes; exit 0. Los enlaces nuevos del plan y roadmap apuntan a los
dos entregables comprobados. `git diff --check`: exit 0. El perfil STATIC ejecutado después
del commit conserva su resultado canónico en el directorio indicado; su identidad exacta y
veredicto se informan en la entrega, sin atribuir evidencia de producto a esta verificación.

La sonda y el manifiesto anteriores, sus hashes, los resultados STATIC anteriores y el
[informe original](pre_g9b_cu7_d_candidate_report.md) se conservan sin editar ni reinterpretar.
La comprobación técnica documental no demuestra una operación nueva ni autoriza CU7-I.

## 4. Archivos e impacto

Inspeccionados: AGENTS.md; ADR 0041; candidatos M08/M09; arquitectura CU7-D; hoja de decisiones;
matriz/puertas; secciones actuales del plan y roadmap; contrato de verificación vigente y
cabecera/parametrización de verify.ps1. Se comprobó que solo existe AGENTS.md raíz.

Cambiados: esos ocho documentos de diseño/planificación existentes. Nuevos: esta conciliación
y la propuesta de incrementos. Fuentes de verdad: decisión autoral para el estado y contratos
vigentes para producto; la normativa nueva es candidata. Artefactos derivados: resultados
STATIC y comprobación de enlaces locales, generados por las herramientas y nunca autoridad
geométrica. Sin cambios de serialización, identidad, software, sonda o modelos.

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: solo texto de diseño y planificación; verify.ps1/registro, Gradle/JDK,
Conda/imports, fuentes generadas, packaging y premisas del bootstrap no cambian.
INFRASTRUCTURE_IMPACT = NONE
REQUIRED_LEVEL = STATIC
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = ningún comportamiento o contrato productivo nuevo;
la guía del alcance implementado sigue reservada a sus gates posteriores.
```

## 5. Compromisos y parada

Se mantienen `CU10-GUIDE-INTEGRATION-CHECK`, FINAL de CU10 y el expediente E1.
`DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` conserva su posición posterior a CU10 y al trabajo
editorial. No se adelantan esas fases ni se modifica el libro. La siguiente puerta es revisión
del candidato normativo consolidado y autorización separada de CU7-I, con commit/árbol,
incrementos y exclusiones expresos. No se publica, no se modifica main y no se crean etiquetas.
