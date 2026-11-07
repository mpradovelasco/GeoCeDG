# POST-CU4 · Contrato candidato de publicación experta

**Para decisión autoral.** C3 de `POST-CU4-INSERTION-PRE-I3`. La publicación
significa incorporar al dibujo activo en memoria. La aprobación del usuario
de una oferta es distinta de la aprobación autoral del contrato y de la
publicación de código o binarios. Este documento se apoya en la inspección
de I2 `6ec68e788a81ff10da19ad64d7e721ed1d00ebb5`, su consolidación PRE-I3 y
la caracterización estática de la implementación. [F3, P4, M1 y B2 vigentes](../../geocedg/specs/ui/document-insertion.md) · [Diseño de partida](post_cu4_expert_insertion_design.md) · [Informe de inspección](post_cu4_i2_i5_inspection_contract.md)

## 1. Perímetro de la primera entrega integrada

I3 presenta la revisión en una acción explícita **Insertar con revisión
experta…**, experimental y desactivada por defecto. I4 añade una publicación
aditiva, con `overwrite=false`, una sola mutación lógica y un paso Undo. I5
acredita productores y comparadores dentro de ese perímetro. El informe puede
mostrar casos más amplios aunque no exista un plan publicable.

El piloto publicable se limita a origen y receptor `.cedg`/`.ggb` pasivos,
2D, con contexto angular compatible, configuración reproducible y sin
macros, recursos de construcción, identidades durables participantes ni
autoridad espacial. Los documentos con ID de raíz válido conservan su UUID.
Un archivo legado sin UUID solo puede contribuir si se prueba la procedencia
de sus registros dentro del artefacto exacto. La falta de UUID persistido del
receptor en el archivo asociado no excluye por sí sola una sesión con UUID
vivo válido. Cualquier referencia externa al subgrafo se limita a constantes
incorporadas identificadas por tipo, con verificación del puerto. Se exige
cierre de todas las salidas copiadas, incluidas las auxiliares.

El piloto admite dos clases de diferencia. La primera contiene cambios
observados y enumerados que el plan ya reproduce, como renombre L1,
presentación 2D, y cambios de valor o formato de un resultado terminal cuando
el comparador del productor los mide y prueba que quedan dentro de su dominio
y carecen de consumidores constructivos o indirectos. La segunda contiene
incertidumbres acotadas, por ejemplo equivalencia dinámica global sin prueba
cuando el grafo, los parámetros, la definición, la validez, la persistencia y
las comprobaciones obligatorias de esa familia están acreditados. Una
degeneración distinta, un cambio de dominio, una pérdida de objetos o aristas,
una reconstrucción con otro productor o una pérdida en reapertura quedan en
inspección. En todos los casos se conserva `D1`. Los valores no se escalan,
las unidades del receptor se mantienen y el aviso usa el origen del búfer de
operación. [Unidades y procedencia](../../geocedg/specs/units/unit-system.md#11-copy-and-paste) · [Identidad y persistencia espacial](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md#8-persistence-and-lifecycle)

La primera fila experta fuera de P4 propuesta es `GeoConicPart` con productor
exacto `AlgoConicPartCircle`, variante arco circular directo de tres puntos
2D, en el dominio y con los testigos definidos por I1. La tabla de despacho
se aplica por productor, variante, dominio, puertos y consumidores auditados.
Un arco fuera de dominio queda en inspección. La fila experta y la fila P4
candidata requieren decisiones independientes. Los fixtures elementales y el
texto dinámico terminal pasivo sirven de controles. La curva cartesiana y la
secuencia sirven de ensayos de cobertura y correspondencia, y quedan en
inspección si carecen de trazas, oráculos o cláusula autorizada aplicable.
Macros, activos, 3D, espacial, CAS, mezclas angulares y omisiones deliberadas
requieren una decisión posterior de capacidades y su propia prueba de
recuperación. [Inventario P4](post_cu4_p4_inventory.md) · [Primera fila y controles](post_cu4_i1_p4_preparation.md)

## 2. Estados y predicado de publicabilidad

| Eje independiente | Valores y regla |
|---|---|
| Inspección | `INSPECTABLE` si existe inventario útil con procedencia y cobertura. `NOT_EVALUABLE` si una política, un límite o falta de observación impide el juicio solicitado. `TECHNICAL_FAILURE` si falla una etapa que debía completarse. Un inventario estático puede acompañar a cualquiera de los dos últimos. |
| Integridad | `VERIFIED`, `NOT_ESTABLISHED` o `TECHNICALLY_INVALID`, por etapa y para el plan completo. Un lector sin error solo acredita carga. DAG incoherente, pérdida, sustitución de identidad o referencia no autorizada invalidan el resultado para publicación. |
| Correspondencia | `PROVEN`, `NOT_ESTABLISHED`, `ADDED_WITH_PROVENANCE`, `KNOWN_OMISSION`, `RECONSTRUCTED_DIFFERENTLY`, `NOT_EVALUATED` o `TECHNICAL_FAILURE`. Las últimas cinco categorías se aplican al objeto o aspecto documentado. Una falta de mapa no acredita pérdida. |
| Geometría | `CERTIFIED_EQUIVALENT` solo para una equivalencia y dominio cubiertos por un certificado aprobado. `KNOWN_CHANGES`, `DECLARED_UNCERTAINTIES`, `NOT_EVALUABLE` o `TECHNICALLY_INVALID` preservan su alcance. Los cambios e incertidumbres pueden coexistir. |
| Presentación y persistencia | Estado por dimensión y hallazgo. Cambios de presentación conocidos pueden ser admisibles. Persistencia exige coherencia constructiva y de recursos en el ciclo probado, además de los campos de presentación prometidos. |
| Publicabilidad | `NOT_PUBLISHABLE` con barreras explícitas, u `OFFERABLE_EXPERT` si pasan las barreras y existe un plan. `PUBLISHABLE_WITH_CONSENT` es una autorización efectiva de un solo uso, únicamente al revalidar bajo barrera. Es independiente de los certificados geométricos. |

El evaluador de política exige todas estas precondiciones antes de construir
una oferta publicable.

1. Archivo, contexto y configuración dentro de la política pasiva versionada.
   Carga estricta de ambos documentos, ensayo y reapertura con diagnóstico
   íntegro. Un error acumulado del lector invalida la etapa aunque devuelva
   objetos o éxito nominal.
2. Receptor preexistente íntegro y preservado por el ensayo. Inventario
   completo de objetos y productores. Correspondencia verificable de puertos
   ordenados, salidas hermanas, referencias indirectas y constantes permitidas.
   Identidad de documento y cierre de construcción y de recursos comprobados.
   Toda autoridad espacial inferida está fuera del piloto.
3. Cada adición tiene procedencia y cae en una fila experta habilitada. Cada
   incertidumbre autorizable identifica propiedad, alcance, objeto afectado,
   límite del comparador y recomendación de comprobación. Desconocer un
   productor, entrada, salida, validez, capa efectiva, diferencia esperada,
   efecto ejecutable o pérdida en persistencia bloquea el plan. Conocer un
   cambio de capa permite proponerlo solo si la fila permite ese cambio y se
   verifican sus consecuencias visibles. La fidelidad de capa F3 conserva
   su propio contrato.
4. El archivo nativo del ensayo y la reapertura tienen la misma construcción
   en el perímetro prometido, con aristas, parámetros, dominios, unidades,
   validez, identidades y recursos esperados. Las diferencias voluntarias se
   aplican antes de escribir el ensayo y se ensaya el resultado real. Una
   diferencia posterior no incluida en el informe invalida el plan.
5. Existe certificado de captura viva vigente y recuperación F3 aplicable al
   receptor. La transacción verifica ambas condiciones otra vez. Consentir
   cambios geométricos medidos o incertidumbre opcional nunca habilita una
   barrera de seguridad, integridad, persistencia o recuperación fallida.

## 3. Interfaces candidatas de aplicación

Los nombres siguientes describen responsabilidades de I3–I5. La interfaz I2
actual no crea una oferta ni entrega un payload ejecutable. La futura API
puede adoptar nombres locales equivalentes y conservar estas fronteras.

| Interfaz | Entrada, resultado y límites |
|---|---|
| `InspectionInput` y `ReceiverCapture` | Bytes nativos inmutables del origen y de la captura I2 viva. Cada entrada aporta SHA-256 y tamaño. El host retiene separadamente el propietario de sesión/Construction/Undo, sello de vigencia y certificado del contexto. El worker recibe únicamente bytes y contexto inerte permitido. Identificar tanto los bytes originales como la envoltura pasiva que se entrega al lector si difieren. |
| `InspectionReport` → `ReviewModel` | I2 aporta etapas, diagnóstico, diferencias por dimensión, procedencia, productores, puertos, cobertura, riesgos y pruebas. El adaptador de revisión agrega condiciones de la política y recomendaciones ligadas a los hallazgos. I3 renderiza estados y referencias legibles. Desktop no reinterpreta geometría ni infiere identidades. |
| `TrialPlan` | Receta inmutable del host para volver a producir el ensayo. Contiene digest de entradas, versión de política/runtime, filas de familia, modo de copia, configuración, reglas L1/D1, remapeo exigido, diferencias y efectos exactos, manifestación canónica del resultado y perfil de recuperación. El plan identifica el certificado de reapertura. El worker aporta evidencia acotada que el host valida. No se ejecuta XML, comandos o código recibido del informe. |
| `ReviewOffer` y `ConsentReceipt` | El controlador I3 emite la oferta de un solo uso tras validar informe y plan. El recibo registra la aceptación explícita de IDs de riesgos/diferencias concretos. La UI separa certificación, integridad, límites, efectos de sesión y resultado experto. |
| `PublicationTransaction` | Consume oferta, recibo y plan con autoridad local. Revalida, toma barrera, ejecuta la misma receta nativa y devuelve rechazo previo, incorporación verificada, recuperación K1-S, recuperación con reinicio declarado o aislamiento. No guarda el archivo. |
| `FamilyPolicy` | Selecciona por clase, productor, variante y dominio. Aporta capacidades pasivas, observaciones obligatorias, tolerancia, pruebas de dinámica y degeneración, aristas indirectas, resultados terminales aceptables y cobertura de persistencia. Una fila nueva permanece fuera de la transacción hasta que sus pruebas sean obligatorias. |

I2 preinspecciona la captura sin cargar la construcción. Puede transportar
el resultado por un archivo privado de operación, también para inventario
estático cuando una capacidad impide la lectura nativa. La comparación
`matchesCurrentState` vuelve a examinar los miembros nativos y el contexto
local. Un resultado positivo compara dos estados observados. I3–I4 necesitan
registrar invalidaciones desde antes de capturar hasta que se consume la
oferta, aun si el documento vuelve al estado anterior. El informe I2 aporta
inventario y pruebas parciales por etapa. Los puertos anónimos o locales y
la reapertura de objetos ordinarios aún pueden quedar sin correspondencia.
[Captura I2](post_cu4_live_inspection_snapshot_candidate.md) · [Diferencial I2](post_cu4_pre_i3_differential_contract.md)

El plan necesita una traza total por registro para los objetos ordinarios del
piloto, incluidos los que carecen de ID durable. La propuesta es enlazar cada
instancia a su registro nativo en el punto que lo emite y a la instancia que
el lector reconstruye al consumir ese mismo registro. La clave efímera se
acota al digest de los bytes y al nodo/puerto del registro que el escritor y
el lector demuestran haber procesado. Para la copia se verifica la relación
operación → registro de transporte → instancia y cada puerto de productor.
Para reapertura se verifica la relación registro nativo → instancia. Los
IDs existentes, cuando una fase futura los admita, se comprobarán además por
la API del registro y su remapeo. La coincidencia de un ordinal global,
coordenada, nombre o representación textual no es testigo. Cada traza exige
completitud, unicidad, tipo, productor y aristas. Se usan observadores
efímeros de la ruta nativa, sin tags, identidades persistentes ni escritor
geométrico alternativo. Si un productor no entrega ese testigo, su fila
permanece en inspección. [Importador nativo](../../source/shared/common/src/main/java/org/geogebra/common/util/InternalClipboard.java) · [Copia Desktop](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java)

## 4. Consentimiento y vigencia

La oferta incluye versión de esquema, runtime, política y comparadores,
nonce de un solo uso, vigencia de diez minutos desde la emisión medida con
reloj monotónico, digest de origen
capturado, huella de contenido y sello vivo del receptor, digest del plan y
digest de la proyección efectivamente mostrada. El digest de contenido se
calcula sobre una representación canónica versionada de campos y entradas
nativas, con orden y tipos explícitos. Excluye el empaquetado ZIP variable
solo cuando se cotejan todos los nombres y bytes expandidos admitidos. La
hora de captura y la ruta son procedencia. El digest de archivo es identidad
de los bytes; la revisión de sesión detecta cambios y retorno al mismo
contenido. El historial, cursor, estado guardado, archivo, UUID, propietarios,
capas, preferencias efectivas, precisión, vistas, activación, recursos y
condiciones de pasividad son parte del estado exigido. La selección y la
herramienta activa se verifican cuando el plan prevé afectarlas.

Estados del controlador: `REVIEWING → OFFERED → ACCEPTED → CONSUMED`, con
salidas terminales `CANCELLED`, `STALE`, `EXPIRED` y `FAILED`. Cancelar, cerrar,
reabrir, sustituir, cambiar el plan o informe, editar, Undo/Redo, cambiar vista,
recurso, historial o política invalida el token. Un cambio seguido de su
reversión sigue siendo invalidante. Si el observador de vigencia carece de
cobertura demostrada, solo se permite inspección. Aceptar consume el nonce
una única vez aunque la transacción se rechace antes de mutar. El diálogo se
cierra antes de entrar en la sección crítica. La aplicación comprueba otra
vez los bytes de la ruta seleccionada con lectura acotada y exige igualdad
con la captura. El último cotejo corresponde al instante de revalidación de
esa lectura. Solo se ejecutan los bytes inmutables del plan y no se vuelve a
leer la ruta como entrada de la copia. Un reemplazo posterior del archivo
externo no puede sustituir esos bytes. Si la comprobación de la ruta o del
receptor no se puede completar en el presupuesto de la oferta, esta caduca.
Los callbacks tardíos o los clics repetidos carecen de efecto. Las filas de
cambio aceptado e incertidumbre aceptada constan en el recibo. Un cambio no
mostrado o un fallo de integridad conduce al rechazo o la recuperación.

`AngularOfferState` aporta un precedente limitado. Observa eventos, historial,
activación y geometría para B2. Su verificación y `sameSource` anteceden a
`transact`. El modo experto sitúa el cotejo final de sesión en la barrera del
EDT antes de la captura P7 y `detach`, sin bombeo de eventos entre la revisión
final y el inicio de la mutación. Los hooks de invalidación deben cubrir las
rutas Desktop que cambian estado relevante sin emitir un evento de kernel.
La vigencia cubre mutaciones soportadas y coordinadas por EDT/Undo. Las
referencias Java del host permanecen en su JVM.
[Vigencia B2 y barrera vigente](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java)

## 5. Frontera de seguridad

La barrera pasiva de I2 actúa antes del lector evaluador tanto para origen
como receptor, ensayo y reapertura. Acota ZIP, sintaxis XML, vocabulario,
paridad de parsers, tamaños y capacidades. Solo los miembros y referencias
permitidos se entregan al lector. El host valida esquema, límites, hashes y
contexto de las respuestas del worker. La JVM corta, con memoria, tiempo y
temporales acotados, separa sesión y referencias. El lanzador actual no
demuestra confinamiento de red, archivos, portapapeles o procesos del sistema
operativo. La seguridad del piloto depende de una gramática positiva y de
auditar todos los puntos de efecto de sus filas. Un comando que necesita un
aislamiento adicional queda bloqueado hasta tenerlo en todas las estaciones
admitidas. Nunca se pasa contenido hostil directamente a
`DocumentArchivePreflight` como si fuese un escáner sin evaluación.

| Capacidad | Política candidata |
|---|---|
| Scripts de objeto, biblioteca JS personalizada y listeners | Diagnóstico previo a lectura o publicación. Se excluyen del piloto. Desactivar scripts de actualización es insuficiente frente al resto de rutas. |
| Animación, aleatoriedad, tiempo o entorno externo | No publicar con animación activa, pausada o programada ni con estado mutable exterior al plan. Un estado no reproducible carece de oferta. |
| Macros | Inventario estático. Rechazo del piloto incluso con igual nombre. Una futura fila requiere M1/MU-1, digest de definición y recursos, clausura transitiva, verificación de enlaces a instancias y evidencia de recuperación. |
| Recursos | Referencias externas, imágenes y entradas no auditadas quedan inventariadas y bloqueadas. Los defaults y miembros pasivos que admite I2 solo se cargan si la política correspondiente los valida y su huella participa en la prueba. |
| Objetos no certificados | Diferenciar rechazo P4 de seguridad y de integridad. La ausencia de P4 no impide inventariar. Un productor sin fila experta o con evaluación incompleta queda sin oferta. |

[Barrera I2](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGInspectionArchive.java) · [Parser de capacidades](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGInspectionXml.java) · [Preflight Desktop evaluador](../../source/desktop/desktop/src/main/java/org/geogebra/desktop/io/DocumentArchivePreflight.java) · [Lector ZIP y JavaScript](../../source/shared/common-jre/src/main/java/org/geogebra/common/jre/io/MyXMLioJre.java)

## 6. Transacción, recuperación y guardado

1. **Aislar y ensayar.** Crear auxiliares de vida acotada a partir de las
   entradas exactas. Ensayar con el contexto efectivo, escribir nativamente,
   reabrir y comprobar. Liberar cada auxiliar o proceso en todos los estados.
2. **Preparar.** Revalidar las entradas, el informe, el plan y la pasividad.
   En EDT y con el monitor Undo, exigir contabilidad fiable y quiescente,
   cotejar vigencia, preparar snapshot P7 verificado y capturar certificado
   completo. Mover historia y cursor por referencia y preparar un baseline
   Undo con identidad y preflight. Un rechazo previo revierte cualquier
   preparación local de la barrera y conserva el receptor.
3. **Aplicar.** Usar la copia nativa aditiva en búfer de operación, con
   diagnósticos estrictos para todo payload, bajo la misma barrera F3. La
   comprobación posterior exige conservación de los participantes vivos,
   UUID, ruta, macro/recurso y configuración del receptor, adiciones y aristas
   esperadas, y ningún cambio inesperado. Comparar también los miembros
   nativos exactos del estado aplicado con los que se reabrieron en ensayo,
   o reabrir y verificar el candidato real antes de cerrar la transacción.
   Si no se puede establecer la equivalencia de esos archivos en el
   presupuesto transaccional, recuperar. El diagnóstico o aviso externo se
   emite al terminar y no modifica el documento.
4. **Cerrar.** Identificar stores por generación, contenido y cardinalidad.
   Añadir un único paso del resultado al prefijo original y descartar Redo
   solo tras certificar el resultado. Mantener ruta y UUID, marcar no guardado
   y mostrar D1. Si el portapapeles cambia selección, modo o pertenencia a
   vistas, el plan presenta el efecto concreto y verifica su perímetro.
5. **Recuperar.** K1-S solo con retirada de todas las adiciones en orden
   inverso, cierre de efectos del búfer y certificado completo. Si no es
   aplicable y es seguro recargar, K1-R-H usa el baseline Undo de barrera,
   sin fallback y con un reintento. Conservar la historia original y declarar
   cada reinicio previsto de instancias o sesión. P7 es el archivo de
   recuperación para aislamiento y no se intercambia con el baseline.
   Una recarga nominal sin certificado, un store no identificado, un fallo
   de rollback interno, fallo doble de recarga o attach inseguro exige
   aislamiento F3. Retener la historia cuando no sea seguro reanexarla.
   Permitir únicamente guardar la copia P7 verificada a una ruta autorizada,
   reabrir el original cuando exista o cerrar. Ningún camino de error muestra
   éxito parcial. La excepción de limpieza se agrega al fallo original.

La ruta F3 tiene `barrier`, `Transaction`, `attachBlock` y `guardedReload`
privados y acoplados a sus verificadores. La reutilización prevista es una
extracción pequeña de esa misma orquestación con verificadores explícitos
por política. El constructor de un plan experto no recibe un modo de omitir
la admisión o la recuperación F3. La prueba común debe cubrir también el
estado que queda fuera del XML. Por ejemplo, la copia nativa reserva
etiquetas del origen en el receptor. `Construction` expone `addProtectedLabel`
y el predicado de disponibilidad, sin una API de snapshot/restauración del
conjunto. F3 §9.1 documenta que K1-S conserva esas reservas. La ruta experta
requiere un alcance de reservas que restaure exactamente las previas después
de su uso y en recuperación. También necesita diagnósticos no condicionados
a identidad, porque `evalClipboardXMLAtomically` solo convierte
`hasErrors()` en fallo para payloads con identidad. Estas son obligaciones
de implementación y de autorización estrecha, no capacidades ya disponibles.
[Transacción F3](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java) · [Undo](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUndoManager.java) · [Snapshot P7](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGRecoverySnapshot.java) · [Aislamiento](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGInsertionIsolation.java) · [Reservas de etiquetas](../../source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java)

Tras éxito, la UI mantiene un aviso de sesión de inserción experta con el
resumen y acceso al informe. Ofrece **Guardar como…** a un `.cedg` distinto.
La primera operación de guardado que incluiría ese estado exige una decisión
explícita de ruta y no permite reemplazar silenciosamente el archivo previo.
La propuesta para el piloto fuerza un destino distinto de los archivos
receptor y origen, incluyendo alias detectables. Cancelar o fallar el guardado
mantiene el estado pendiente y deja los archivos intactos. Tras guardar una
copia se aplica el ciclo normal al nuevo archivo. Undo y Redo actualizan el
aviso según el paso experto activo y la asociación vigente. La marca es de
sesión. No se añade una etiqueta geométrica ni una entrada persistente al
archivo. La identificación posterior a reapertura exigiría un ADR de
compatibilidad y migración. [Política nativa de Guardar y Guardar como](../../geocedg/specs/ui/native-document-identity.md#save-and-save-as-state-machine)

## 7. Decisiones candidatas y puertas C3

| Decisión que debe constar en la autorización | Texto candidato y alcance |
|---|---|
| `EXP-01` Superficie y publicabilidad | Adoptar una especificación separada de inserción experta con los §§1–5 de este candidato. Añadir en §1 de F3 la referencia a la acción separada. La ruta F3 mantiene P4, B2, M1, L1, D1 y su alcance certificado. Aprobar explícitamente cambios terminales y las incertidumbres opcionales enumeradas en §1. |
| `EXP-02` Transacción y ganchos nativos | Autorizar la extracción de la barrera común y la instrumentación mínima y optativa de diagnósticos, procedencia y reservas de etiquetas. La entrada normal F3 y la copia general conservan sus políticas. Los ganchos se justifican en los puntos de efecto e incluyen regresiones de Classic y F3. Si falta un enganche para una fila, bloquear esa fila. |
| `EXP-03` Consentimiento y guardado | Adoptar la vigencia de §4 y la política §6 para una sesión experta con acción separada, aviso de estado, nuevo destino y sin cambios de formato. Confirmar terminología EN/ES y que la activación experimental quede fuera del perfil predeterminado. |
| `EXP-04` Familias y primer alcance | Aprobar la tabla experta del piloto descrita en §1, con su prueba del arco circular directo en I5. La ampliación P4 de I1 necesita otra decisión y otro delta normativo. Toda capacidad activa, espacial o destructiva permanece fuera de la autorización integrada inicial. |

| Puerta | Condición y evidencia PRE-I3 | Resultado contractual y dependencia de implementación |
|---|---|---|
| G4 Integridad | Predicado §2 y ensayo/cierre §6 contrastados con F3 §§2 y 5. | Definido. Exige un testigo total de puertos y persistencia, y diagnóstico estricto de clipboard. Un informe I2 parcial queda en inspección. |
| G5 Vigencia | Oferta de un uso y cotejo de §4 contrastados con `AngularOfferState`, `sameSource`, la barrera y la captura viva I2. | Definido. I2 coteja estados observados. I3–I4 necesitan invalidación monotónica durante la oferta, incluidas reversiones, y cotejo dentro de la barrera. |
| G6 Seguridad | Gramática positiva y clasificación de §5 contrastadas con I2 y el lector nativo. | Definido para la envolvente pasiva. Exige adversarios de todas las rutas y del canal worker. La JVM separada no acredita confinamiento de OS. |
| G7 Recuperación | Rutas y certificado de §6 contrastados con barrera, stores, K1-S, K1-R-H y P7 vigentes. | Definido. Exige reservas acotadas, cobertura de estado de sesión e inyección de fallos sobre la barrera extraída. La deuda histórica de Undo con macros queda fuera del piloto. |
| G8 Implementabilidad | Interfaces de §3, decisiones `EXP-01`–`EXP-04` y [prompt integrado](../../.github/prompts/tasks/post-cu4-i3-i5-expert-insertion.prompt.md). | Secuencia y puntos de extensión identificados. Necesita autorización estrecha para los ganchos de `EXP-02` y una campaña en el futuro SHA. Las decisiones pendientes no se convierten en capacidades actuales. |

La verificación de C3 en PRE-I3 es documental y de código. Las pruebas F3/B2
existentes sirven como referencia de comportamiento en sus propios candidatos.
Las pruebas de publicación, vigencia bajo transacción, integración del diálogo
y recuperación experta pertenecen al futuro SHA de I3–I5. [Cierre CU4 y límites](../validation/pre_g9b_cu4_closeout_record.md)
