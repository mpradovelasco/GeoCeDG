# CU7 integrado — M09, I2G y envolvente transaccional

Fecha: 2026-11-02 (Europe/Madrid). Candidato de investigación; `selfApproved=false`.
Base contrastada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`.
No adopta normativa, no reanuda A0/I2 y no acredita implementación.

## 1. Dictamen y fuerza de la evidencia

No se recomienda autorizar ahora M09 productivo por presentarlo como un singleton de bajo
impacto. La costura más pequeña identificada —cambiar el selector literal de un punto-token
del mismo R— tiene valor funcional real, pero no tiene una envolvente transaccional demostrada.
La alternativa completa exige además construcción prospectiva, correspondencia del host y
cierre de efectos, que aumentan su invasividad. I2G no se resuelve añadiendo una fila al
proveedor singleton: necesita contrato propio del productor y de todos sus outputs.

Esta es una conclusión de viabilidad sobre el estado actual, no una prueba de imposibilidad.
Se distinguen:

- **C (código):** comportamiento leído en la base actual, sin ejecución nueva.
- **H (histórico):** observación runtime de R1/R2/R3-E en su base y fixture archivadas.
- **D (derivación):** consecuencia arquitectónica de C/H; no prueba runtime adicional.
- **P (propuesta):** opción futura que requiere autorización y demostración independientes.

El estado de autorización vigente se obtiene de
`docs/architecture/pre_g9b_cu7_i_increment_proposal.md:3`: la disposición de 2026-10-30
supersede operativamente la antigua autorización singleton. La aprobación M08-S-R2 no reabre
M09. Este estudio no reinterpreta el encabezado histórico de M09 como permiso actual.

## 2. Contrato que tendría que cumplir M09

`geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md:32` separa identidad durable P,
definición comprometida y token de raíz de R. Conservar P cambiando su definición puede ser
compatible aun sin coincidencia geométrica; jamás transporta identidad entre raíces.
El predicado de retención (§2) incluye familia/proveedor/schema/autoridad/rol, transición
reconocida, padres prospectivos, ambos DAG, P3-R1, impacto completo y elección explícita.
La geometría o el tipo POINT por sí solos no satisfacen el predicado.

| Entrada/operación | Estado actual y condición necesaria |
|---|---|
| VALUE token → otro token admisible del mismo R | C/H: costura mecánica; P: cerrar exclusividad del selector, claims, revisiones, efectos, Undo y publicación. No es M09 aceptado. |
| VALUE token → token de otro R | C: `richInput` es final en `AlgoLocusIntersectionPointV2.java:29`; requiere cambio de productor/inputs y retirada/adopción de claims, no solo setter. |
| Ordinario o semantic-address VALUE → token, y vuelta | P: transición tipada completa, conservación/reconstrucción de dependientes y retiro de helpers/estado anterior. `GeoClass.POINT` no la autoriza. |
| Target sin ID durable | C: `SpatialIdentityRegistry.captureRedefineContext` devuelve null en :3545; §3 M09 exige envolvente previa a cualquier promoción y participación nueva, no RETAIN de un ID inexistente. |
| CU3 structural VALUE | Pendiente caracterización de procedencia y consumidores de certificados; no asimilar al VALUE ordinario. |
| Interacción/principal selector/proyección espacial → VALUE | Barrera de rol/autoridad; una confirmación no la elimina. |
| `Intersect(L,target)` rico → punto | Tipo incorrecto: consumir una solución admisible por token exacto; resultado rico no es punto. |
| Multisalida → un punto nuevo | I2G pendiente; preservar el rechazo anterior a remoción y el cierre de hermanos. |

Todo candidato debe revalidar admisibilidad local con el grado real de evidencia. Incompletitud
global no invalida automáticamente una solución local admisible; proximidad no certifica
contacto CU3. Un token perdido produce indefinición, sin elegir otra raíz. La recuperación
del mismo selector es distinta de una redefinición y de una identidad nueva.

## 3. Lo que ya aporta el kernel y su límite

| Pieza actual | Evidencia C | Reutilización y límite |
|---|---|---|
| Transacción registry/runtime prospectiva | `SpatialIdentityRegistry.java:650`, `:927`, `:1015` | Prepara registros/attachments, revisiones y resoluciones con DAG; útil para validación conjunta, pero presupone geos/inputs host y no coordina por sí sola su reconstrucción, Undo o escritores. |
| Contexto explícito previo | `SpatialIdentityRegistry.java:3543`, `Construction.java:1858` | Captura grupo, XML y epochs; exige target participante. Una API con nombre transaction no prueba la envolvente M09 futura. |
| Assessment/modos | `SpatialIdentityRegistry.java:5028`, `:5220`, `:5268` | RETAIN/FRESH/REJECT separados; modo explícito y fallo sin fallback. Token host de vigencia es XML en :5369; no registra todas las mutaciones ABA. |
| Frontera durable versionada | `DurableDependencyProjection.java:29`, `:117` | v1 directo y v2 hasta ancestros durables a través de helpers. Sirve para dependencias del registro, no para afirmar enumeración de incidencias, listeners, caches y todos los efectos host. |
| Publicación de padres | `LocusV2PublicOperations.java:722`, `:1066` | `ParticipationBatch` promueve argumentos, da etiqueta, reserva IDs y publica. Construir candidato en el host puede mutar antes del assessment; no es evaluación no mutante. |
| Claims | `AlgoLocusIntersectionPointV2.java:155` | Adquiere nuevo claim, libera anterior; `compute` actualiza después el punto. Operación ordinaria real, no commit atómico conjunto con registro/revisiones/Undo. |
| Rollback host | `Construction.java:2700`, `:2720` | XML snapshot y `processXMLString`; no guarda Undo. Cambia instancias en reconstrucción. No equivale al memento completo de R3-D/A0. |
| Undo | `UndoManager.java:68`, `:174`, `:211` | Lista, cursor y listeners son estado propio. `AlgebraProcessor.java:682` almacena Undo después de callbacks de actualización/REDEFINE. Incluir solo XML en rollback no lo restaura. |
| Callbacks | `Kernel.java:4050` | `notifyUpdate` invoca Views síncronamente. Los scopes de redefine protegen servicios participantes, no convierten cualquier setter de cualquier callback en escritor cercado. |
| Desactivar Views | `Kernel.java:3678` | Limpia/reinicializa vistas; no es aislamiento transaccional ni exclusión de setters, scripts y servicios compartidos. |

P3-R1 conserva posición por testigos de entrada y mínimo intervalo legal. Reutilizar su
snapshot es conveniente para M09/I2G/I3; sustituirlo por cualquier orden topológico pierde
el contrato de mínimo movimiento. Un productor/grupo reconstruido necesita sus testigos
completos; una referencia Java puede servir para enumeración dentro de la operación, nunca
para continuidad durable a través de una carga.

## 4. La evidencia histórica que decide los atajos

Fuentes H, leídas con `git show` sin checkout:

- Rama `phase/pre-g9b-cu7-i2-singleton`, informe
  `docs/validation/pre_g9b_cu7_i2_r1_guarantee_demonstration_report.md`, §§3–6.
- Rama `phase/pre-g9b-cu7-i2-r2-diagnostic`, informe
  `docs/validation/pre_g9b_cu7_i2_r2_diagnostic_experiment_report.md`, §§4–9.
- Rama `phase/pre-g9b-cu7-i2-r3-design`, diseño
  `docs/architecture/pre_g9b_cu7_i2_r3_bounded_transaction_design_candidate.md`, §§4–10.
- Rama `codex/pre-g9b-cu7-i2-r3-e-a0`, informe
  `docs/validation/pre_g9b_cu7_i2_r3_e_a0_experiment_report.md`, §§1, 4–7.

| Resultado H | Implicación D y límite |
|---|---|
| R2-A: dos productores LINE con soportes anónimos indistinguibles por observables permitidos; emisión/carga XML sin ticket de origen | Contar eventos o emparejar por orden no certifica biyección. No refuta un canal futuro explícito; sí impide llamar general a la reconstrucción existente. |
| R2-D: punto fijo host↔durable de fixture, 8 geos y 4 registros incluyendo binding sin GeoElement | Enumerar ambos grafos es útil y necesario; la prueba no cierra efectos implícitos, callbacks, ownership de helpers ni publicación. No promover IMPACT_COMPLETE. |
| R1-V1, R2-B1/B2 y R3-E-P06: assessment sigue current después de ABA silencioso con estado final igual | XML/epoch actuales son insuficientes para la nueva promesa de vigencia. Un observer numérico no cubre GeoPoint, y uno de GeoPoint tampoco demuestra universalidad. |
| R3-E-P01: fábrica pública promueve el literal durable a una etiqueta | Fixture estricta A0 «durable sin etiqueta» inadmisible. Corregir esa premisa exige sucesor explícito; no quitar etiqueta retrospectivamente ni declarar el mismo experimento superado. |
| R3-E-P02/P03 | Selector del mismo R actualiza mismo P/claims/dependientes, sin allocator/loader/emisión observada. Un selector compartido altera ambos consumidores. Independencia del GeoText no prueba exclusividad. |
| R3-E-P07: View ve T nuevo con registered aún viejo y hace setters ajenos | Setter+cascada deja observar prefijo y permite reentrada; no satisface publicación ni aislamiento M09. |
| R3-E-P08: fallos reales tras retención/liberación/escritura; XML recupera datos comparados pero loader cambia instancia y epochs +2 | Reversión de payload no es restauración exacta S0 de A0. Los adaptadores/mementos propuestos todavía no existen. |
| R3-E-P09: fallo tras almacenar Undo conserva cambio de contenido/cursor; fallo del loader de restore llega a fatal | Undo debe entrar en la frontera; recuperación XML puede fallar y no permite seguir bajo éxito ficticio. |
| R3-E-P10/P11: Undo/Redo, ZIP y copia normales funcionan para fixtures | Reutilización valiosa del codec/grafo normal. No demuestra una nueva transacción M09 ni su publicación tardía. |

R3-E instrumento `instrumented-run4`: 18 casos, 17 ejecutados y uno omitido; exit hijo 0.
Control `baseline-run1`: dos métodos, exit hijo 0. Son observaciones históricas del informe,
no ejecuciones nuevas; el éxito del arnés valida los contraejemplos, no acepta A0.
No se repiten aquí: sin hipótesis arquitectónica nueva volver a ejecutar no resuelve sus
fronteras ausentes.

Comparación nueva de blobs Git R3-E/base actual (sin ejecutar Java):

| Archivo | Blob igual en ambas bases |
|---|---|
| Construction | `868d615f698d5b63cc2d0019197102cd610c2267` |
| AlgebraProcessor | `62b7bdf08cf89a913efa953315dd1d066e2c7303` |
| UndoManager | `433c11354803dc6094838d3aac88074f16f538be` |
| AlgoElement | `15dc47be78d58299e0b311022c2c43cb7713951e` |
| AlgoIntersectLineConic | `4feb65d9c752066ab29a51fab99f12d3d0372846` |
| SpatialIdentityRegistry | `8092c03c03130ef12edde405976e0de0e26a7d58` |
| AlgoLocusIntersectionPointV2 | `d8ba2da6426f7975b2aaa389a20da43d034ff17f` |

`LocusV2PublicOperations` cambia de `92473f758b6e0a102d02c5514a5ba71dfb047b34`
a `a437f584a18493d137fd61c0fd99aee1a0dfa0c8`; se releyeron las rutas actuales de selección
y promoción arriba. La igualdad de los mecanismos nucleares sostiene pertinencia de las
insuficiencias; no convierte toda la campaña histórica en rerun de main.

## 5. I2G: por qué no hay todavía adaptador pequeño completo

C: `AlgoElement.java:817` elimina todos los outputs; `remove(out)` en :868 llama a
`remove()` completo. `Construction.java:2089` prepara remoción de hermanos salvo el rebuild
espacial de grupo aprobado. El proveedor neutral exige una salida en
`ConstructionGeoRedefineProvider.java:104`, `:154` y `:246`.

G9A3 sí tiene maquinaria de grupos completos por roles: diseño
`docs/architecture/g9a3_spatial_lifecycle_migration_design.md:155`, registry :5375 y
`SpatialRedefineOutputGroup.java:26`. Exige roles únicos, cobertura completa, misma
cardinalidad y contrato compatible por rol. Su rebuild preserva dependientes del grupo
homólogo; no autoriza separar la salida target y dejar el otro productor intacto.

`AlgoIntersectLineConic.java:63` conserva P/D/Q, edad, permutación, alive y casos especiales;
:205 crea uno o dos outputs según configuración; :236 registra el grupo; :287, :519 y :542
escriben los puntos y mantienen selección near-to conjunta. Sus accesores read-only CU2/CU3
no son API de detach/reemplazo/restauración. Mantener un hermano por coordenadas o índice
sería precisamente inventar la correspondencia que el contrato prohíbe.

| Opción I2G | Viabilidad y coste relativo D |
|---|---|
| Reasignar un slot P[i] o su parent | Inadmisible: compute sigue escribiendo el grupo y persiste receta conjunta; ownership/incidencias/selección no demostrados. Descartar este atajo. |
| Usar rebuild G9A3 existente como si fuera detach | Inadmisible sin proveedor y contrato de grupos homólogos. Existe sustrato, no adaptación I2G. |
| Productor GeoCeDG mediado con outputs internos y proxy público por rol | P: técnicamente plausible, pero convierte definiciones/grupo, necesita normal form, roles semánticos, propiedad/lifecycle de output interno y migration/reader propios. Coste alto, no «pequeño adaptador» ya demostrado. |
| Reconstrucción general con mapping completo de productor, outputs y consumidores | P: podría cubrir familias adicionales; coste muy alto y mayor superficie upstream. No compensa introducirla solo para declarar CU7 cerrado. |
| Rechazo tipado pre-mutation y construcción nueva independiente | Conserva garantía actual; pérdida funcional explícita: no retiene P ni reata automáticamente su cierre. No cuenta como I2G implementado. |

No se puede demostrar que ningún adaptador futuro sea pequeño. Sí se puede determinar que
ninguno de los mecanismos actuales inspeccionados cumple los cuatro puntos del §5 M09.
Un adaptador admisible futuro deberá demostrar productor/familia y roles completos; target
y hermano con dependientes ordinarios y durables; outputs sin ID; split/tangencia y selección;
incidencias y soporte; P3-R1; fallos antes/después de cada cambio; Undo/ZIP/lectores/copia.
Hasta entonces I2G se mantiene bloqueado por precondición de productor/grupo y se difiere
su implementación fuera del perímetro conservador propuesto.

## 6. ¿Hay subconjunto transaccional mínimo, cerrado y útil para varias partes?

**Sí hay sustrato útil compartido; no hay un subconjunto productivo ya demostrado suficiente
para los incrementos pendientes.** La distinción es el resultado central, no una evasión.

El núcleo reutilizable actual es validación de registros/attachments prospectivos,
proveedores tipados, separación RETAIN/FRESH/REJECT, DAG durable, snapshots P3-R1 y codecs.
Sirve a M09/I2G/I3 como parte de un protocolo. El mapper de copia completa tampoco se
convierte en correspondencia de redefinición. M08-S demuestra publicación acotada de puntos
nuevos con su contrato; no demuestra preservación de un target/producer anterior.

Un **mínimo lógico candidato** para redefinición, sin framework general, puede describirse:

1. Intención y política explícitas; entrada anterior a parseo/creación; propiedad síncrona y
   escritor/adaptadores enumerados. Preview sin autoridad ejecutable persistente.
2. Lectura no mutante de descriptor, cierre host↔durable a punto fijo y efectos de cada
   adaptador. Rechazar cualquier efecto/servicio no cerrado; dos DAG coherentes y testigos.
3. Delta privado de definición/claims/registros/valores y Undo preparado; semántica distinta
   para símbolos privados, IDs reservados y publicados. Cero asignación en el primer caso.
4. Instalación y recomputación con mementos completos, sin callbacks observando prefijos;
   postcondiciones; restauración antes de publicar; fatal explícito si restore falla.
5. Una frontera de publicación de todo el estado y un Undo; contexto consumido, nunca fallback
   implícito. La política de fallo de entrega posterior necesita decisión expresa.

Estos cinco puntos son P, no servicio existente. Se reutilizarían en M09 e I3, y parte de
ellos en I2G. I3 añade lineage/familias/barreras; I2G añade ownership/grupos; ninguno queda
resuelto por factorizar el núcleo. M08-D añade contrato matemático y lector propio y no
necesita que M09 exista para crear un punto dinámico nuevo; hacerlo depender de reconstrucción
general sería acoplamiento innecesario.

La única primera hipótesis razonablemente acotable sería un sucesor A0 explícito: token→token
del mismo R, P/selector preparticipantes, literal **etiquetado** admitido expresamente,
selector exclusivo, cero IDs/raíces nuevos, sin cambio de DAG y tabla finita de consumidores.
Su utilidad es cambiar la solución que alimenta un cierre conservando P. No soporta ordinary→token,
otro R, target sin ID, grupo, I3 o M08-D. Necesita todavía mementos, Undo y exclusión reales.
Por tanto no se recomienda venderlo como infraestructura amortizada para todo CU7 ni reabrir
otra investigación aislada por defecto. Si cerrar su exclusión exige interceptar todos los
setters o rediseñar Undo general, parar el sucesor y evaluar una fase propia de reconstrucción.

## 7. IDs, vigencia y restauración: decisiones que no se pueden ocultar

`SpatialIdentityRegistry.java:329` devuelve reserva durable real, no símbolo privado;
`RegistryState` en :3183 guarda mapas y epochs, pero no todo el estado de fuente, host o Undo.
R1/R2 muestran que issued contiene también reservas abandonadas; no es historial exclusivo
de publicaciones. El high-water de raíz pertenece al ledger y es otra cosa: abort privado no
lo avanza, commit fresco sí. No reabrir la deuda K6 de persistencia ya resuelta en CU3-B2.

Propuesta mínima para un futuro A0: medir cero allocator/reserva/retirement y cero emisión de
raíces; claims solo entre raíces existentes. No demuestra el problema general de participantes
nuevos. M09 general necesitaría simbolización privada y política explícita de escape/observación
antes de prometer rollback completo o no reutilización entre documentos/procesos.

El contexto de un solo uso y una sola ejecución síncrona reduce ABA entre diálogos, pero no
prueba aislamiento dentro de la ejecución. Un lock que los setters ajenos no consultan no
excluye esos escritores. La política R3-D de `COMMITTED_WITH_DELIVERY_FAILURE` posterior a L
es candidata: el estudio no la adopta ni relaja «fallo restaura entrada» de ADR0041.

## 8. Recomendación integrada y contingencias

| Capacidad | Destino recomendado a la decisión integrada |
|---|---|
| M09 I2a/I2b general | MANTENER BLOQUEADA HASTA RESOLVER UNA PRECONDICIÓN: protocolo host/registro/claims/Undo cerrado y vigente. Implementación diferida fuera del perímetro conservador. |
| A0 setter+cascada o rollback XML presentado como transacción completa | DESCARTAR LA PROPUESTA CONCRETA: los contraejemplos ya deciden esa hipótesis. |
| A0 sucesor acotado | Posibilidad futura condicionada, no implementación recomendada ahora ni puerta automáticamente abierta. No sustituye M09 completo. |
| I2G | DIFERIR A FASE POSTERIOR, categoría de planificación integrada; su estado técnico conserva el bloqueo por precondición de productor/grupo y correspondencia aprobados y demostrados. |
| Framework general de reconstrucción como tarea incidental CU7 | DESCARTAR la ampliación automática; evaluar en fase propia solo si demanda funcional adicional justifica el coste. |
| Diagnóstico/deudas/exclusiones y expediente de cierre reducido | IMPLEMENTAR EN CU7 SOLO CON PERÍMETRO RESTRINGIDO documental, si el autor acepta explícitamente el nuevo perímetro. No convertir diferimientos en completitud. |

Costes relativos: A0 sucesor medio-alto (fronteras upstream de callbacks/Undo incluso sin
rebuild); M09 general alto (prospectivos/correspondencia/efectos); I2G alto adicional
(productor/roles/receta/lifecycle); reconstrucción general muy alto. Son estimaciones por
subsistemas afectados e incertidumbre de garantías, no tiempos ni benchmarks.
Si el autor exige M09/I2G como indispensables para CU7, el cierre global debe permanecer
abierto y abrirse un encargo arquitectónico propio. Si admite cierre reducido, registrar
pérdida temporal exacta, operaciones rechazadas, condiciones de reconsideración y revisión
independiente; ningún éxito documental modifica los estados productivos pendientes.

## 9. Inventario y validación de este anexo

Inspeccionados: AGENTS; propuesta de incrementos vigente; contrato prospectivo M09; diseño
G9A3 y compatible-redefine; informes R1/R2/R3-D/R3-E citados; Construction, AlgebraProcessor,
Kernel, UndoManager, AlgoElement, AlgoIntersectLineConic; SpatialIdentityRegistry,
SpatialRedefineTransaction, SpatialRedefineHostState, SpatialRedefineOutputGroup,
ConstructionGeoRedefineProvider, DurableDependencyProjection, AlgoLocusIntersectionPointV2,
LocusV2PublicOperations. Búsqueda de pruebas G9A1/R3 localizada como mapa, sin ejecutarlas.

Cambios: solo este anexo candidato en staging ignorado para incorporación por el coordinador.
Capa: investigación/arquitectura documental. Semántica/compatibilidad: sin efecto.
Evidencia nueva: lectura estática y comparación de blobs; ninguna sonda Java nueva ni
aceptación de producto ejecutada. La validación documental del conjunto corresponde al
coordinador; este anexo no inventa log/receipt.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no cambia prerrequisito, toolchain, runtime o instalación.
VERIFICATION_INFRASTRUCTURE_IMPACT=NONE. GUIDE_IMPACT=NONE para el estudio; obligaciones
de guía/CU10 y deudas previas permanecen.
