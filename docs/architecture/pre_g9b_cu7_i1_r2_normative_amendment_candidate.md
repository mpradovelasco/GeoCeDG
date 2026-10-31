# CU7-I1-M08-S-R2 — contrato tipado y enmienda candidata mínima

Estado: **CANDIDATE — NOT EFFECTIVE — REQUIRES AUTHOR APPROVAL**.
No altera ADR 0019, ADR 0041 ni especificaciones aceptadas. No concede permiso productivo.
Complementa el [diseño matemático](pre_g9b_cu7_i1_r2_local_direction_design.md) y sucede a la
[propuesta R1](pre_g9b_cu7_i1_r1_local_admission_candidate.md), que se conserva íntegra.

## 1. Contrato del resultado, anterior a toda API

Se propone un resultado efímero M08-S separado del resultado histórico R6. Estos nombres
denotan estados semánticos candidatos, **no enums, clases o firmas Java existentes**.

| Dimensión independiente | Estados propuestos y significado |
|---|---|
| Cobertura inversa global | `COMPLETE_REQUESTED_SCOPE`, `INCOMPLETE`, `NOT_ESTABLISHED`; sólo la primera puede sustentar ausencia/unicidad dentro de su alcance |
| Estructura de dominio | componente seleccionado `CERTIFIED_CURRENT` o `NOT_ESTABLISHED`; inventario global de componentes se registra aparte con su grado real |
| Vínculo modelo/autoridad | `ESTABLISHED_IN_ACTIVE_CONFIGURATION` o `NOT_ESTABLISHED`; identifica versión, configuración y, si procede, transferencia admitida |
| Evidencia local | `REGULAR_INJECTIVE_CHART_ESTABLISHED`, `NOT_ESTABLISHED`, `SINGULAR`, `COMPONENT_AMBIGUOUS`; el primer estado exige el teorema y datos de diseño §3 |
| Distancia | `OUTWARD_BOUND_ESTABLISHED` sobre F(U) y q capturado, `NUMERICAL_ESTIMATE_ONLY`, `NOT_ESTABLISHED` o `OUTSIDE_POLICY`; sólo el primero sirve para la modalidad nueva |
| Mínimo | `NOT_CLAIMED`, `ISOLATED_LOCAL_MINIMUM_ESTABLISHED`, `NOT_ESTABLISHED`; la modalidad propuesta siempre emite NOT_CLAIMED, incluso si su semilla parece un mínimo |
| Barreras y trabajo | `SAFE_FOR_DECLARED_LOCAL_SCOPE`, `UNSAFE`, `NOT_ESTABLISHED`, `BUDGET_EXHAUSTED`; anotar qué se examinó y las regiones no exploradas |
| Vigencia | `CURRENT`, `STALE`, `CONSUMED`, `CANCELLED` |
| Permiso | `NOT_OFFERABLE`, `EXPLICIT_LOCAL_ADDRESS_CONFIRMATION_REQUIRED`; el nuevo resultado no tiene permiso «único» ni autoselección |

El resultado conserva el status/cobertura R6 como diagnóstico separado, sin modificarlo.
`UNRESOLVED` no proporciona permiso; una prueba local nueva e independiente puede producir
el nuevo permiso aunque el resultado R6 permanezca UNRESOLVED por falta de cobertura global.
Si falta cualquier dimensión necesaria, el resultado nuevo también es no ofertable y
expone el motivo no resuelto. No usar reason, número de candidatos o mensajes para inferirlo.

### Evidencia de una oferta

Cada dirección ofertable contiene fuente durable, revisión exacta, contrato de proveedor,
rama, selector estructural de componente, dirección canónica/periodic lift y bits de u0;
U y sus reglas de frontera; configuración/procedencia del modelo; cajas exteriores de F/F';
eje y margen mu; cota de distancia y radio; grados numéricos; premisas y versiones; evaluación
forward de control; alcance de seguridad y resumen tipado de barreras/trabajo. La prueba no
es identidad del punto ni se persiste. No basta una etiqueta `ESTABLISHED` sin esos testigos.

El intervalo U aísla una carta regular y su pertenencia semántica, **no una raíz estacionaria**.
Las ofertas deben distinguirse por sus direcciones canónicas y evidencia de separación, nunca
por distancia entre imágenes. Para el primer perímetro se exige que las celdas de ofertas
distintas en la misma rama/componente sean separadas; solapamiento sin demostración de
equivalencia canónica o separación produce rechazo, no un desempate por orden.
Direcciones coincidentes en coordenadas pueden ser diferentes preimágenes semánticas.

### Predicado conjuntivo de permiso

Fuente/definición válidas y actuales; componente certificado y sin ambigüedad; modelo fiel
bajo autoridad activa; carta regular e inyectiva demostrada; cota exterior a q; forward
válido; aislamiento de la carta y separación de ofertas establecidos; ausencia demostrada
de barreras inseguras en el alcance exigido; trabajo no agotado; contexto vigente.

La falta de exploración global fuera de las cartas se declara como tal y no equivale a
una barrera local demostrada. Tampoco se convierte en prueba de ausencia de barreras.
En el primer incremento cualquier barrera insegura **observada** por la búsqueda dentro
del alcance solicitado veta toda la oferta nueva; no se oculta para rescatar un candidato.
Si no puede transportarse un resumen íntegro de esas observaciones se emite NOT_ESTABLISHED.
No se exige explorar un dominio global imposible para afirmar una propiedad sólo local.
Presupuesto agotado siempre veta el nuevo permiso, aunque exista una prueba parcial.

### Confirmación, vigencia y consumo

Una oferta, incluso única en la lista descubierta, requiere aceptación explícita de la
dirección y del alcance local: «seleccionar esta dirección; no se ha demostrado unicidad
global ni que sea el punto más próximo». Presentación EN/ES en el consumidor; no geometría
de frontend, botón predeterminado de creación ni selección silenciosa por historial.

El contexto privado pertenece a la operación de consulta y liga documento, nonce efímero,
identidad fuente, definición/revisión, selector de componente y evidencia, configuración y
versiones, q capturado, alcance y política completos, presupuesto y testigos. La elección
es un miembro autenticado de ese contexto con su dirección exacta, no una coordenada,
etiqueta, ordinal ni referencia Java convertida en identidad semántica.

Antes de publicar, comprobar de nuevo todas las ligaduras y la evaluación forward. Un cambio
de fuente/DAG relevante, autoridad, configuración, componente, política o q en el editor
exige consulta nueva. Undo/Redo, carga, sustitución/reconstrucción de fuente y cierre de la
operación invalidan la selección, incluso si reaparecen valores iguales. No se presupone que
igualdad de coordenadas, fingerprint estático o referencia Java detecten ABA. Si las guardas
actuales de revisión no prueban esta vigencia, mantener la operación sin permiso hasta una
solución acotada aprobada; no introducir un contador global de mutaciones por conveniencia.

Reutilizar primero la revisión ya monotónica de `GeoLocusV2`: su publicación exige incremento
estricto y conserva el máximo al descartar la definición. Eso cubre el ABA que publique
versiones A/B/A en el mismo objeto. No demuestra por sí solo invalidación al cargar otra
instancia, cambiar política del host o mutar una copia del evaluador sin publicar; esas
fronteras se deben comprobar por ciclo de vida/contexto y forman parte de G2.

q es un valor capturado y presentado como tal. Si Q cambia mientras se muestra la oferta,
se cancela o se vuelve a consultar para confirmar su posición actual; no se incorpora una
suscripción geométrica persistente. Si Q es antecesor de L, sus cambios ya invalidan la
fuente por esa ruta normal. Tras crear P, mover Q no cambia P salvo esa dependencia de L.

Confirmación, cancelación, fallo o caducidad consumen/destruyen el contexto. Sólo una
publicación forward exitosa crea P y un Undo. Todo rechazo previo deja XML, DAG, registros,
identidades y pila Undo sin cambios. No se reclama la transacción general M09; si la creación
forward existente no garantiza limpieza ante un fallo concreto, se bloquea esa ruta.

### Causas tipadas mínimas, sin colapsar diagnósticos

Fuente inválida; dominio/componente no certificado; modelo no disponible; fidelidad/autoridad
no establecida; carta/regularidad/separación no demostrada; singularidad; componente ambiguo;
distancia fuera de política; evidencia sólo estimada; barrera insegura; presupuesto agotado;
selección caducada, ajena, consumida; cancelación. Ausencia certificada sólo con cobertura
completa; «ninguna oferta local demostrada» no significa ausencia de direcciones.
No inventar «mínimo local» ni «proyección» para describir una oferta de carta regular.

## 2. Texto candidato para ADR 0019

**Añadir al final de decisión 4, sin reemplazar sus reglas R6:**

> El resultado histórico R6 conserva íntegros status, cobertura y getUniqueCandidate. Una
> confirmación de usuario no promueve UNRESOLVED. Exclusivamente para M08-S con Q explícito,
> una frontera distinta puede ofrecer una dirección próxima de una carta C1 regular e
> inyectiva demostrada mediante evidencia exterior fiel a la autoridad activa, bajo el
> contrato CU7-I1-M08-S-R2. Debe separar cobertura global, evidencia local, distancia,
> mínimo, barreras y vigencia. Su permiso exige confirmación explícita aun con una sola
> dirección descubierta. No afirma mínimo de distancia, proyección exacta ni unicidad global.
> Toda evidencia necesaria no establecida mantiene esa oferta sin permiso.

**Añadir a Evidence model y al alcance de creación:**

> La nueva modalidad NEAR_REGULAR_ADDRESS selecciona una dirección forward válida, sin
> exigir un mínimo aislado del objetivo. Requiere rama/componente inequívocos, carta local
> separada y prueba uniforme de regularidad e inyectividad, cota de distancia a la instantánea
> numérica explícita y ausencia de barreras inseguras. Una estimación de R6 no constituye esa
> prueba. No modifica las garantías de exactitud, autoridad constructiva, STRICT, comandos
> forward ni la política general de edición/arrastre.

No se enmiendan otras decisiones de ADR 0019 ni su identidad durable.

## 3. Texto candidato para ADR 0041

**Añadir a decisión 3, conservando la regla histórica:**

> M08-S distingue dos vías: admisión ya existente bajo R6, y selección local explícita bajo
> el resultado independiente CU7-I1-M08-S-R2. Esta segunda vía no selecciona directamente
> miembros de un UNRESOLVED histórico: valida de nuevo evidencia local tipada. La falta de
> completitud global no anula una carta local demostrada, pero tampoco permite declarar
> ausencia, unicidad ni mínimo global. Para NEAR_REGULAR_ADDRESS no se reclama aislamiento
> de una raíz estacionaria; sí regularidad, inyectividad, separación de carta y pertenencia
> semántica demostradas. Una sola oferta local exige confirmación explícita.

**Añadir a decisión 2:**

> La selección usa q sólo como valor transitorio. La única definición guardada es la forma
> forward aprobada con sus padres semánticos. No se guarda Q, la consulta, sus testigos o un
> algoritmo que repita la búsqueda al cargar. La creación no amplía el arrastre ni autoriza
> M08-D o M09.

No cambia la tabla de identidad/redefinición, los compromisos CU10 ni la deuda científica.

## 4. Texto candidato para especificaciones pertinentes

| Artefacto y punto de inserción | Cambio mínimo propuesto tras aprobación |
|---|---|
| `geocedg/specs/locus/locus-v2-point-interaction.md`, después de §5 | incorporar el contrato independiente de §1 de este candidato; declarar explícitamente que §§3/5 y sus getters R6 siguen iguales, sin nueva política de movimiento |
| `geocedg/specs/locus/candidates/cu7-m08-point-placement.md`, §2 paso 4 | conservar admisión UNIQUE/MULTIPLE vigente; añadir segunda vía exclusivamente por permiso local tipado de §1; jamás leer directamente candidatos de UNRESOLVED como permiso |
| mismo contrato, §1 y §2 | distinguir elección de carta próxima de selección de mínimo; copiar q sin padre nuevo; persistencia forward y revalidación exacta |
| `geocedg/specs/locus/locus-v2-public-surface.md`, §11 Creation tool | opción Q ya existente con política world explícita, aviso y confirmación obligatoria para la nueva vía; sin comando dinámico ni geometría Desktop |
| mismo contrato, §13 | no cambio de codec/versiones/normal form; contexto y pruebas no entran en .cedg/.ggb, Undo, clipboard o macros |

El contrato de modelos certificados, existencia y autoridad constructiva se consume sin
ampliarlo: sólo capacidades ya cubiertas, transferencias ya admitidas y componentes cuya
certificación local exista. Si una integración requiere cambiar ese alcance o persistencia,
se detiene para decisión específica. Ninguna enmienda se aplica en R2.

## 5. Ubicación mínima propuesta, posterior al contrato

1. Kernel compartido: reutilizar semillas del resolvedor R6 sin alterar su resultado. Evaluar
   una carta interior con el modelo intervalar real y emitir prueba más resumen de barreras.
   Hoy no hay un permiso de esta clase, ni se puede fabricarlo desde Desktop.
2. Los modelos intervalares viven en un paquete de intersecciones con visibilidad privada.
   Proponer un adaptador interno de alcance mínimo o extracción interna del contrato y sus
   capturas; decidir con pruebas de equivalencia que no cambien los consumidores actuales.
   No crear una API pública ni volver a implementar trigonometría, captura o coeficientes.
3. Kernel: un contexto de selección local separado, validación/consumo y delegación de
   publicación en la construcción forward ya utilizada por `LocusSnapshotPlacement2D`.
   No abrir a esta vía el commit histórico sólo por permitir UNRESOLVED.
   `LocusPointInteractionCandidate2D` contiene evidencia R6 y su diagnóstico dice «minimum»;
   no fabricar ese candidato para representar una carta sin mínimo. Reutilizar internamente
   la creación forward con dirección requerida, inputs dedicados, rol interactivo existente
   y limpieza, separando el permiso nuevo del envoltorio R6. Mantener forma persistente y
   ownership actuales, sin conversión silenciosa de roles ni API nueva de producto.
4. Desktop: consumir estado/causas y dirección del kernel; presentar alcance local y exigir
   selección/confirmación, cancelación y único Undo. Headless, si se expone, consume el mismo
   contrato sin diálogo; no añadir ahora un comando ni afirmar interfaz inexistente.

Nombres definitivos de clases/métodos y selección PHASE se reservan al trabajo autorizado
posterior, después de superar las puertas; esta lista no afirma APIs implementadas.
