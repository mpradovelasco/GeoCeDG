# PRE-G9B-MAINT-P0 — F. Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | **Hoja de decisiones autorales.** Desde la revisión 3 transcribe la resolución autoral del 2026-11-23 (§0); las recomendaciones de la revisión 2 (§1-§4) se conservan como historia. `selfApproved=false`: las decisiones son del autor y esta hoja solo las transcribe |
| Estado | `DECISIONS RECORDED — AUTHOR RESOLUTION 2026-11-23` (revisión 3); D-B-1b sigue pendiente; ninguna implementación autorizada |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0», enmiendas autorales finales del expediente y resolución autoral definitiva del expediente (las tres del 2026-11-23) |
| Base | `f80f0e9b029e678007f399508163bf794951992c` (GeoCeDG 1.2.2); candidato resuelto `f0f41c4bf186944f227d5b38addc719fb701ce07` |
| Expediente | [portada](pre_g9b_maint_p0_dossier.md) |

## 0. Resolución autoral del 2026-11-23

Resolución «GeoCeDG — PRE-G9B-MAINT-P0. Resolución autoral definitiva del expediente de
planificación», sobre el candidato documental `f0f41c4bf186944f227d5b38addc719fb701ce07`
(árbol `91e0525f4eb409b953fed708e2d85f135361522b`). Naturaleza: aprobación de planificación y
autorización de cierre documental. **Implementación productiva: no autorizada.** El autor
aprueba el marco `PRE-G9B-MAINT-P0` con las decisiones siguientes; ninguna autoriza por sí
misma un incremento productivo, cada implementación requiere autorización propia sobre una
base exacta y el programa sigue separado de G9B (`DESIGNED — NOT AUTHORIZED`).

| Id | Resolución | Precisiones del autor |
|---|---|---|
| **D-0** | **Aprobada** | `PRE-G9B-MAINT` es un programa de mantenimiento independiente de G9B. El cierre de P0 y el marco se registran con una entrada aditiva en el roadmap. La conciliación de cabeceras antiguas es mantenimiento documental independiente y no amplía este cierre |
| **D-ORD** | **Aprobada como prioridad de planificación** | Primera ola: `MAINT-N1` (enmiendas de B5a y B2), `MAINT-B5a`, `MAINT-U2`, `MAINT-I2G0` (caracterización y, previa autorización, barrera) y `MAINT-A-N1`. Los incrementos pueden avanzar por separado cuando sus dependencias lo permitan |
| **D-V** | **Aprobada** | Documentales: `STATIC`. Caracterizaciones focales: `STATIC` + `PHASE` registrada. Productivos: `STATIC` + `PHASE` + `INTEGRATION` según la clase aprobada. `FINAL` no se exige por categoría; una campaña adicional requiere justificación y autorización expresa. La aceptación automática no es aprobación del autor |
| **D-A-1 a D-A-5** | **Aprobadas como dirección arquitectónica** | Familia B-spline semántica nueva, independiente de SplineV2, concebida para la extensión racional; primera implementación polinómica con pesos unitarios implícitos. `MAINT-A-R` diseñada y no autorizada. Sin interfaces racionales generales en el núcleo mientras no hagan falta. Sin cambios en la semántica ni en los formatos de SplineV2 |
| **D-A-6** | **Aprobada** | Importación DXF 2D de `SPLINE` polinómicas y racionales con datos de control completos (`MAINT-A-DXF0`) como objetivo prioritario; solo lectura; referencias, no construcciones autoritativas; fuera del DAG y sin conversión automática a `GeoLocusV2`; límites, diagnósticos, unidades y transformaciones de la propuesta B |
| **D-A-7** | **Aprobada con precisión** | R-a (solo sesión) en la primera implementación de A-DXF0. R-c (copia en el documento fuera de `Construction`, sin DAG paralelo) es el objetivo posterior prioritario, tras caracterizar la compatibilidad del archivo `.cedg`, los lectores anteriores y las reglas de guardado y reapertura. R-d no autorizada |
| **D-A-8** | **Aprobada** | Conversión exacta para fuentes polinómicas dentro de los límites; aproximación explícita para fuentes racionales o fuera de límites; ajuste en el parámetro de la fuente como primera dirección; la aproximante es una construcción CeDG nueva y distinta de la referencia |
| **D-A-9** | **Aprobada como política de diseño** | Tres garantías distintas: residuo en muestras, estimación numérica, cota global rigurosamente certificada. La cota se justifica por su procedimiento de acotación, con redondeo y precondiciones. Nada calculado sobre la aproximación se atribuye a la curva original |
| **D-A-10** | **Aprobada como dirección** | Primera aproximación congelada e independiente, con procedencia explícita; sin actualización dinámica ni dependencias nuevas entre la referencia y la construcción. La ubicación del registro de procedencia, y su comportamiento en copia, redefinición y persistencia, se resuelven en el contrato normativo |
| **D-B-1a** | **Aprobada como dirección normativa** | B5a es la primera prioridad científica. Distinguir ausencia de certificación, incumplimiento real de H3 e incumplimiento no detectado; la ausencia de alarma no demuestra continuidad; la guarda no certifica. Umbrales, refinamientos y presupuesto se fijan en la especificación del incremento. Criterio de aceptación en el [plan C](../architecture/pre_g9b_maint_p0_limitations_plan.md) §1.6. Sin cambios en inventario de componentes, tokens, identidad ni certificados de existencia |
| **D-B-1b** | **Pendiente** | O1, O2 y O3 se conservan; preferencia técnica O2. Antes de adoptarla, caracterizar el resultado rico vigente, sus consumidores y su comportamiento por revisión. §28.2 no se modifica todavía |
| **D-B-1c** | **Diferida hasta validar B5a** | La admisión T-A se evaluará con la política corregida de consistencia numérica |
| **D-B-1d** | **Diferida** | Sin `conditioned-traversal/v2` en el programa inicial |
| **D-B-2** | **Aprobada con alcance reducido** | Solo B2-F1: limpieza de autoridades ricas auxiliares privadas sin consumidores al completar correctamente una redefinición, en el borde transaccional adecuado, dentro del mismo ciclo de Undo/Redo, respetando consumidores legítimos y resultados con identidad y uso independiente, sin cambios parciales si la redefinición se revierte. **Decisión expresa: no se implementa B2-F2**: ningún barrido al cargar, ninguna migración ni reparación silenciosa de archivos históricos; los huérfanos heredados quedan como limitación documentada. Sustituye la recomendación de la revisión 2 (§3), que incluía ambos barridos |
| **D-B-3** | **Aprobada** | Coste de `Intersect` condicionado y D-V6 aceptados como limitaciones cuantificadas; sin cambios en la política de aislamiento ni en la selección de raíces; `MAINT-PERF-O2a` opcional y con autorización específica |
| **D-B-4** | **Aprobada** | Semántica posicional D2 mantenida; una prueba de regresión fija la reordenación de listas, sin identidades duraderas nuevas |
| **D-U2** | **Aprobada como alcance propuesto** | U2-1 a U2-6; se mantienen todas las rutas y capacidades existentes; implementación por su incremento propio |
| **D-F8** | **Aprobada para caracterización** | `MAINT-F8-S0` sobre un locus mixto real. La traza y P-2 no son confirmación. `MAINT-F8-min` solo con evidencia discriminante de una carencia real. Componentes certificadas locales y regiones condicionadas no se confunden |
| **D-M09-1 a D-M09-4** | **Dirección aprobada; implementación pendiente** | Sustituir `P` por `P'` con identidad nueva y dependencias propias, conservando los dependientes estructuralmente compatibles; sin conservar la identidad de `P` ni exigir coincidencia geométrica. `MAINT-S0` es caracterización previa obligatoria; S1 (seguridad y transparencia) y S2 (objetivo funcional principal) requieren autorización tras revisar S0. Siete garantías no negociables; el nivel transaccional definitivo se decide tras S0, sin debilitamiento anticipado de las garantías de M09. Alcance inicial y referencias por nombre según el [diseño D](../architecture/pre_g9b_maint_p0_cu7_revised_design.md) §3.0 |
| **D-I2G** | **Aprobada para caracterización y diseño** | Barrera basada en el riesgo real, no solo en el número de salidas; impedir la destrucción silenciosa de identidades, dependientes CeDG o reclamaciones de hermanos; conducta Classic preservada sin riesgo CeDG demostrado; caracterizar antes de modificar el anfitrión; conservación de hermanos diferida; una retirada de grupo, si se autoriza, se describe con exactitud y exige confirmación |

Capacidades que permanecen fuera del programa inmediato: M08-D; I3; reconstrucción general
del núcleo; arrastre universal sobre Locus V2; redefinición general con conservación de la
identidad de `P`; NURBS semántica nativa (`MAINT-A-R`); importación DXF fuera del subconjunto
racional 2D aprobado; actualización dinámica de aproximaciones DXF; migraciones automáticas de
documentos antiguos; barrido de auxiliares ricos durante la carga; cambios generales de la
política de aislamiento de intersecciones. Ninguna es dependencia de un incremento aprobado
salvo demostración y nueva autorización.

La promoción documental del expediente a `main` y la autorización de los primeros incrementos
son decisiones autorales posteriores.

## Recomendaciones presentadas al autor (revisión 2, conservadas como historia)

Las secciones 1 a 4 son las recomendaciones de la revisión 2, sin cambios. Donde difieren de §0,
prevalece §0.

Solo figuran las decisiones que exigen al autor. Cada una lleva una recomendación técnica por
defecto (**R**) y sus alternativas. Las decisiones técnicas internas de cada incremento se
toman en su especificación o en su prompt.

Las enmiendas autorales del 2026-11-23 fijan orientaciones, no aprobaciones: la arquitectura
preparada para NURBS, la importación DXF racional de referencia y la aproximación B-spline
como objetivos, B5a como prioridad científica, S2 como objetivo funcional principal de la
sustitución, U2 como mejora prioritaria y F8 condicionado. Esta hoja las recoge como
contexto; ninguna fila las convierte en decisiones aprobadas ni aprueba la implementación
nativa NURBS.

Registro de revisiones: la revisión 1 (2026-11-23) es la versión inicial. La revisión 2
(2026-11-23) actualiza D-ORD, D-V, D-A-1 a D-A-5, D-U2, D-F8, D-M09-1, D-M09-2, D-M09-4 y
D-I2G; divide la antigua D-B-1 en D-B-1a, D-B-1c y D-B-1d; y añade D-A-6 a D-A-10 y la
decisión pendiente D-B-1b, según las enmiendas autorales finales. La revisión 3 (2026-11-23)
añade §0 con la resolución autoral definitiva y deja §1-§4 sin cambios como historia.

## 1. Programa

| Id | Pregunta | Recomendación | Alternativas y consecuencias |
|---|---|---|---|
| **D-0** | ¿Se adopta el marco del programa? Identificador `PRE-G9B-MAINT`, separación de G9B, principios del [plan](../architecture/pre_g9b_maint_program_plan_candidate.md) §0, registro aditivo en el roadmap mediante un paso documental | **R:** adoptarlo. Registrar P0 y el programa en el roadmap como registro aditivo, sin abrir G9B. Encargar como tarea documental propia la conciliación de las cabeceras de estado desactualizadas (informe §A.6) | Sin registro: la autorización de P0 y las siguientes vivirían solo en la conversación, como ocurre ahora |
| **D-ORD** | ¿Qué primera ola se autoriza? | **R:** `MAINT-N1` (partes de B5a y B2) + `MAINT-B5a` (un escalar erróneo admitido; prioridad científica), `MAINT-U2` (interfaz prioritaria), `MAINT-I2G0` (empezando por su caracterización) y `MAINT-A-N1` (diseño normativo de la familia preparada para NURBS y del contrato de importación). En la ola siguiente, `MAINT-A-DXF0` y `MAINT-A2` pueden ir en paralelo | Empezar por la importación DXF sin diseño normativo previo: más rápido, pero el contrato de importación y la biblioteca común quedarían sin fijar. Empezar por la sustitución: requiere más caracterización y decisiones previas |
| **D-V** | Política de verificación del programa | **R:** `CORE + FOCUSED` por incremento (`STATIC` + `PHASE` propia + `INTEGRATION`); caracterizaciones con `STATIC` + `PHASE`; documentales con `STATIC`; sin `FINAL` por categoría. `MAINT-S1`/`S2` pueden pedir un escalado razonado | Exigir `FINAL` a los incrementos que tocan `Construction.replace`: más confianza en suites upstream, con coste de campaña completa |

## 2. Bloque A — B-splines, NURBS e interoperabilidad

| Id | Pregunta | Recomendación | Alternativas y consecuencias |
|---|---|---|---|
| **D-A-1** | ¿Familia nueva o ampliación de SplineV2? ¿Con qué identidad conceptual? | **R:** familia nueva, independiente de SplineV2, publicada como `GeoLocusV2` con su propio proveedor y concebida como familia B-spline racional en general, cuyo primer subconjunto es el polinómico con pesos unitarios implícitos (alternativa β de la [propuesta B](../architecture/pre_g9b_maint_p0_bspline_proposal.md) §4 y §5). SplineV2 queda intacta | α, ampliar SplineV2: enmienda de una especificación aprobada, dos semánticas bajo un nombre, sin vía limpia hacia pesos. β', familia polinómica cerrada y otra NURBS después: dos familias para una noción y migración futura |
| **D-A-2** | Parámetro semántico y formas del comando | **R:** el parámetro es `u` del vector de nudos del usuario. Formas `BSpline(puntos, grado, nudos)`, `BSpline(puntos, grado)` con la regla persistida `clamped-uniform/v1` y `BSpline(puntos)` con grado 3. Contrato versionado y serializado `bspline-2d/v1`; la forma con pesos queda reservada | Normalizar a `[0, 1]` como SplineV2: mejor condicionamiento sin trabajo adicional, a costa de un parámetro distinto del que escribe el usuario y del de una `SPLINE` importada |
| **D-A-3** | Alcance inicial y límites de la familia | **R:** abierta anclada y cerrada `C^0` por igualdad exacta de extremos; polinómica; `1 ≤ p ≤ 7`; `n + 1 ≤ 128`; multiplicidad interior `≤ p`; pares solo ricos; solo comando (sin herramienta) en `MAINT-A2` | `p ≤ 12` por paridad con SplineV2 (más condicionamiento y menos lectores DXF conformes); incluir periódica desde el principio (exige prueba exacta de costura); herramienta gráfica en A2 |
| **D-A-4** | Exportación DXF `SPLINE` | **R:** incremento propio `MAINT-A3` tras A2. Clase «transferencia exacta de la definición» solo para la familia polinómica, curva anclada no periódica, sin puntos de ajuste. Conformidad con AutoCAD Core Console y un segundo lector independiente que el autor designe. SplineV2 sigue en `LWPOLYLINE` | Diferir `SPLINE` (la familia solo exportaría polilíneas aproximadas); incluir SplineV2 con una clase nueva de equivalencia salvo redondeo (`MAINT-A5`) |
| **D-A-5** | NURBS | **R:** arquitectura preparada para NURBS desde `MAINT-A-N1` (propuesta B §5): forma homogénea, contrato con regla de versiones y pesos reservados como datos del usuario y padres del DAG; sin interfaces racionales generales en el núcleo mientras no hagan falta. La NURBS semántica nativa (`MAINT-A-R`, propuesta B §10) queda **diseñada y no autorizada**; su nombre visible (forma de `BSpline` o comando `NURBS` de la misma familia) se decide al abrirla | Incluir pesos ya en A2: exige ampliar la interfaz polinómica del núcleo para tramos racionales antes de tener la familia polinómica validada. No preparar la arquitectura: la extensión futura cambiaría la identidad conceptual |
| **D-A-6** | Alcance inicial de la importación DXF (`MAINT-A-DXF0`) | **R:** el de la propuesta B §8.3: DXF ASCII, espacio modelo, entidades `SPLINE` con datos de control completos, polinómicas y racionales con pesos finitos y positivos, ancladas, multiplicidad interior `≤ p`, planas con `z` constante; límites declarados (`p ≤ 7`, `n + 1 ≤ 1 024`, cociente de pesos acotado); transformación identidad por defecto y afín explícita registrada; rechazo diagnosticado por entidad de lo demás | Admitir también no ancladas (dominio estándar `[u_p, u_{n+1}]`, con riesgo de discrepancia entre lectores); admitir puntos de ajuste (curva dependiente del lector); límites más altos |
| **D-A-7** | Representación y persistencia de la geometría de referencia | **R:** modelo de solo lectura fuera del núcleo; en `MAINT-A-DXF0`, solo en la sesión (R-a). Como paso siguiente, copia de las definiciones y su procedencia en el documento fuera de `Construction` (R-c), tras caracterizar cómo trata 1.2.2 una entrada desconocida del archivo `.cedg`. Un objeto de referencia en `Construction` con identidad y dependencias (R-d) solo si se aprueba la aproximación dinámica, con las responsabilidades del núcleo de la propuesta B §8.7 | R-b, referencia al archivo externo: depende de que el archivo siga existiendo y no cambie. R-d desde el principio: obliga a un tipo nuevo del núcleo y a todos sus contratos |
| **D-A-8** | Tipo de aproximación ofrecida (`MAINT-A-DXF1`) | **R:** conversión exacta cuando la fuente es polinómica y está dentro de los límites; en otro caso, ajuste por mínimos cuadrados en el parámetro de la fuente, con grado declarado (3 por defecto), nudos iniciales de la fuente e inserción adaptativa, extremos interpolados y fallo tipado si no se alcanza la tolerancia (propuesta B §9.2-§9.3). La edición manual queda como edición posterior | Construcción manual asistida como primera vía: sin correspondencia paramétrica, su error solo se estima por distancia de Hausdorff y es difícil de certificar |
| **D-A-9** | Política de tolerancias y garantías | **R:** tolerancia explícita en unidades del modelo y límites de grado y tamaño de la familia; publicar siempre residuo en muestras y estimación numérica del error entre curvas, cada una con su grado; la cota global certificada (propuesta B §9.5) dentro de A-DXF1 si su coste lo permite o en `MAINT-A-DXF1b`. Ninguna afirmación sobre longitudes, tangentes o intersecciones de la fuente | Solo residuo y estimación (sin cota certificada): más simple, garantía más débil. Cota certificada obligatoria desde el principio: más trabajo antes de la primera utilidad |
| **D-A-10** | Relación entre la referencia importada y la aproximante | **R:** aproximación congelada e independiente, con registro de procedencia como bloque pasivo del contrato de la familia; si la aproximante se edita, sus medidas se declaran no aplicables; nunca se reajusta sola (propuesta B §9.7-§9.8) | Registro de procedencia fuera de `Construction` (sin cambio del núcleo; se pierde al copiar). Aproximación dinámica vinculada: exige R-d y un ajuste determinista por revisión; diferida |

## 3. Bloque B — Limitaciones de 1.2.2

| Id | Pregunta | Recomendación | Alternativas y consecuencias |
|---|---|---|---|
| **D-B-1a** | Enmienda de `locus-v2-metrics.md` §28.3 para B5a (en `MAINT-N1`) | **R:** una ruta condicionada no es número si la prueba de escalado de cuerdas observa la firma de un salto. Se declara comprobación numérica conservadora, no prueba universal: distingue discontinuidad no detectada, incumplimiento de H3 y ausencia de certificación ([plan C](../architecture/pre_g9b_maint_p0_limitations_plan.md) §1.2); presupuesto de trabajo acotado con negativa conservadora si se agota; batería adversarial y no regresión de admisibilidad (§1.5). B5a es la prioridad científica del programa | Sin la enmienda, aceptar y fijar con prueba que un salto alineado con la cuerda puede dar un número erróneo admitido (confirmado por la sonda P-1) |
| **D-B-1b** | Política del subtotal del total condicionado (B1) | **Pendiente, sin aprobación anticipada.** Opciones del plan C §3.2: O1, no calcularlo; O2, calcularlo solo a petición expresa para la revisión vigente; O3, mantener la estimación actual con optimizaciones justificadas. **R técnica:** O2, que elimina el coste automático y conserva el dato; antes, medir el valor actual del resultado rico y sus lectores | O1: coste base, se pierde el dato. O3: sin cambio normativo, conserva 4,4–6,4 s por cambio |
| **D-B-1c** | Admisión T-A (una región sin bandas) como número | **R:** opcional, de bajo coste, en `MAINT-N1` | No admitirla: sin cambio |
| **D-B-1d** | `conditioned-traversal/v2` (B5b) | **R:** diferido | Abrirlo ahora: cambia la identidad de contenido de toda la evidencia |
| **D-B-2** | Huérfano de redefinición (B2, enmienda de §29) | **R:** barrido al completar la redefinición, dentro del mismo paso de Undo, y barrido al cargar de las autoridades privadas sin lector, con diagnóstico y nota de versión | Solo el barrido de redefinición: los documentos con huérfanos anteriores los conservan para siempre |
| **D-B-3** | `Intersect` condicionado (B3) y D-V6 | **R:** aceptarlos como límites cuantificados; no cambiar la política de aislamiento; `MAINT-PERF-O2a` (coste por evaluación, sin cambio normativo) como incremento opcional al final | Fijar un objetivo y autorizar la sección áurea, con cambio declarado de los últimos bits de las raíces y un banco adverso |
| **D-B-4** | Piezas gobernadas por deslizadores (B4) | **R:** cerrar como límite declarado y añadir una prueba que fije la reordenación de piezas al redefinir la lista | Dar identidad duradera a los numéricos de las listas de dominio: cambia identidad y serialización sin caso constructivo demostrado |

## 4. Bloque C — CU7

| Id | Pregunta | Recomendación | Alternativas y consecuencias |
|---|---|---|---|
| **D-U2** | Alcance de U2 | **R:** U2-1 a U2-6 del [diseño D](../architecture/pre_g9b_maint_p0_cu7_revised_design.md) §1.2: panel único con selector de origen, alias 145 conservado en la barra, origen del radio visible, asistencia de extremos alineada y probada, rechazo que remite a la dirección explícita, guías EN/ES. Mejora de interfaz prioritaria | Retirar 145 del desplegable (más simple; pierde un acceso visible de R5); no tocar la asistencia de extremos (deja OD-5) |
| **D-F8** | ¿Qué se hace con F8? | **R:** mitigación en U2 (U2-5); `MAINT-F8-S0`, una prueba discriminante sobre un locus mixto real que determine la conducta sin presuponerla; `MAINT-F8-min` solo si S0 confirma la traza (el clic sobre una componente certificada local se rechaza mientras el clic sobre la región condicionada crea). La traza de código y la sonda P-2 no son confirmación empírica. Si S0 no la confirma, cerrar F8 como redundante con esa evidencia | Integrar solo el equivalente (mensaje y guía) y mantener F8 diferido. Cancelar ya: no lo respalda la caracterización |
| **D-M09-1** | Nivel de restauración y requisitos de coherencia de la sustitución | **R:** requisitos no negociables: atomicidad, ninguna publicación parcial, reversión propia de la operación y no por Undo, ningún punto de Undo antes del éxito, DAG y registro de identidad coherentes tras cualquier fallo (diseño D §3.3). El nivel de restauración (el del ADR 0026, con objetos Java reconstruidos, u otro) se decide tras `MAINT-S0`, que caracteriza referencias por nombre, fronteras de transacción y condiciones de restauración; la implementación de S1 y S2 no se aprueba antes | Restauración exacta de M09: la reformulación hereda todos los bloqueos transaccionales de M09 |
| **D-M09-2** | Dependientes duraderos directos | **R:** `MAINT-S1` como incremento de transparencia y seguridad dentro de las normas vigentes (los dependientes duraderos directos se pierden, con informe veraz y confirmación); `MAINT-S2` como **objetivo funcional principal**, tras la enmienda `MAINT-N3`: los dependientes compatibles (paso propio válido con `P'`) conservan identidad, con dependencias actualizadas explícitamente y evidencia revalidada, incluidos los Locus V2; tokens revalidados y nunca transferidos | Solo S1: seguridad y transparencia sin la utilidad de M09. S1 y S2 en un único incremento: más valor de una vez, más riesgo y una enmienda previa obligatoria |
| **D-M09-3** | Etiqueta y `P'` existente | **R:** en S1, `P'` es una definición nueva que recibe la etiqueta de `P` como asa del analizador (nunca como identidad); la fusión con un `P'` existente queda fuera hasta caracterizarla en `MAINT-S0` | `P'` con etiqueta nueva: obliga a reescribir los comandos dependientes y rompe las referencias por nombre |
| **D-M09-4** | Referencias por nombre fuera del DAG (guiones, textos dinámicos) | **R:** se deciden con el inventario de `MAINT-S0`; por defecto, se listan en el impacto como «siguen al nombre; no verificadas» y no bloquean | Rechazar toda sustitución con referencias por nombre: más estricto, menos útil |
| **D-I2G** | Productores con varias salidas | **R:** `MAINT-I2G0` como barrera basada en el riesgo demostrado para el productor y su grupo (diseño D §4.3): rechazo tipado antes de eliminar nada cuando se destruirían identidades, dependientes duraderos o reclamaciones CeDG, empezando por la destrucción silenciosa fijada por `G9A1SpatialIdentityRedefineHostTest.java:483-497`, tratada como defecto; conducta Classic sin cambios cuando no hay nada CeDG en riesgo, sea cual sea el número de salidas; caracterización previa de ambos comportamientos. Retirada explícita del grupo (I2G-1) como opción con impacto completo dentro de S1. Conservar los hermanos con el productor original (I2G-3) queda declarado no viable en este programa | Rechazar por número de salidas: rechaza operaciones Classic legítimas. Mantener el comportamiento actual (destrucción silenciosa). Investigar I2G-3 ahora (costura nueva en `Construction`, recarga no probada) |

## 5. Qué no se pide decidir

No se piden decisiones sobre G9B, el libro, versiones, etiquetas, `main`, M08-D, I3, la
reconstrucción general ni el arrastre universal: el programa no los toca. Tampoco se pide
aprobar la implementación de `MAINT-A-R`.
