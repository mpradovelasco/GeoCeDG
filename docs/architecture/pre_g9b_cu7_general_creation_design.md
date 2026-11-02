# PRE-G9B-CU7 — Creación general: diseño y frontera de autorización

Fecha: 2026-11-02, Europe/Madrid. Candidato, `selfApproved=false`. Base productiva `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`, árbol `d62179e72dd2bb37226cba5956198809467cb7c1`. Nueva dirección autoral: creación por clic o Q; sin M08-D, M09 o arrastre general.

## Clasificación y fuentes

Caracterización matemática y diseño con implementación condicionada. Capa: servicio compartido de autoría de puntos y consumidor Desktop. Fuentes de verdad: ADR0019/0041 y enmiendas 2026-11-02, M08 aceptado, interacción/superficie pública, R6/R1/R2/H1 y código de la base. Estudio integrado recuperado de `7bc5d9f80b6a7217d658b056ff6142fc668f72f3`; sus recomendaciones quedan sucedidas por la nueva decisión de alcance, sin reabrir M09.

## Dos partes separadas

**A, implementable bajo garantías vigentes:** clic y Q suministran la misma petición world transitoria. El núcleo compone exactamente R1/R6 y, solo ante `UNRESOLVED_NUMERICAL_SEARCH / GLOBAL_UNIQUENESS_NOT_ESTABLISHED`, R2/H1. Se conservan candidatos y commits de cada ruta, presupuesto, proximidad, componente, freshness, autoridad, confirmación y persistencia forward. `M08SnapshotPlacement2D.query(source,x,y,policy)` ya existe; no se necesita un Q auxiliar ni un formato nuevo.

**B, barrera normativa:** admitir una dirección individual fuera de los resultados seguros R6 sin el C1/jet no nulo H1 requiere otro criterio. La suficiencia de dirección+componente+forward no deroga la guarda explícita de la enmienda aprobada. Esta parte se detiene en texto normativo candidato; no se modifica H1, resolver, existencia/componentes o fábrica forward para saltar su autorización. R6 puede admitir ya varias direcciones de evaluadores sin H1 bajo su propio contrato: no se le aplica H1 retrospectivamente.

## Definición e invariantes

`P=F[b,c](u0)` queda definido por fuente durable, rama, selector estructural de componente y parámetro canónico reconstruible. q es petición transitoria; Q, si existe, se revalida hasta el commit y deja de ser padre después. L sigue siendo padre y propaga cambios. No se afirma mínimo, proyección exacta, unicidad global/cartesiana ni búsqueda inversa completa por el mero éxito forward.

La ruta implementable no cambia esos predicados ni la semántica de invalididad, seams, singularidades, gaps, colapso, branches, copias o dormancia. No usa render como dirección, `GeoLocusV2` no es `Path`. Los hits gráficos solo eligen fuente/petición. No se transporta la autorización de crear a la de arrastrar: un punto H1 nuevo no adquiere arrastre general.

## Diseño mínimo

1. Añadir `LocusPointCreation2D` en `kernel/locus/interaction`: fachada pequeña para las dos rutas ya existentes. `query(source,x,y,policy)` y `queryFromReference(source,Q,policy)` crean una selección efímera de uso único; cada Choice conserva su propietario y candidato kernel. Route `R1_R6`, `R2_H1`, `REJECTED`. Ningún cambio a resultados R6 o proof H1.
2. Trasladar la decisión de ruta de `GeoCeDGReferencePlacement` al servicio compartido, dejando el adaptador Desktop mínimo. Conservar sus observables y tests existentes. Validar source/Q/revisión/thread/feature antes del commit; un rechazo o fallo de publicación no intenta otra ruta.
3. Habilitar el clic bajo la misma confirmación contextual que Q cuando usa R2/H1; en modo145 una opción efímera Click position / Posición de clic consulta directamente el punto world con la política del panel, sin depender del radio inicial de la herramienta Punto ordinaria. El modo L→Q permanece por defecto; reutilizar panel/tool existentes, sin diálogo de creación rutinario ni confirmación ficticia. Mantener la creación directa R6/SplineV2 y el arrastre admitido actuales. Compartir admisibilidad en kernel; Desktop presenta opciones y registra un Undo por éxito.
4. Pruebas focales de equivalencia clic/Q, prioridad y negativa, source/Q stale, presupuesto, cancelación, Choice ajeno/consumido, persistencia y propagación. Añadir diagnóstico de evaluadores sin C1 que conserve sus rechazos actuales. Prueba gráfica real sobre copia de TestM08-S.cedg y fuentes identificadas.

Sin cambios a ADR/specs aceptados, normal forms, codecs, fuente evaluadora, render, resolución inversa, geometría o producción multisalida. Cambios upstream solo si el controlador GeoCeDG no proporciona la costura: cualquier necesidad mayor detiene esa ampliación.

## Validación y parada

VERIFICATION_CLASS=BOUNDED_PHASE; PRODUCT_PHASE_EFFECT=CU7_CREATION_CANDIDATE. Usar wrappers `tools/agent`: probes nuevos optativos bajo `geocedg/validation/pre-g9b-cu7-general-creation/` y su init Gradle, con evidencia de proceso/JUnit individualizada; no alterar los inventarios históricos ni inventar una fase nueva. Ejecutar PHASE PRE-G9B-CU7-I1-M08-S-R2-DESKTOP como regresión del candidato, sin presentarla como registro de las pruebas nuevas. Focus de desarrollo antes de la cohorte congelada y STATIC final. No cambia registry/runner/inventarios: INFRA_UNIT no aplicable. Registrar comando, exits, reportes, identidad y omisiones. La prueba gráfica complementa tests de eventos, no se infiere de ellos.

Si el clic requiere un criterio de admisión nuevo, detener también esa parte. Si únicamente falta modelo/componente para un evaluador general, informar causa tipada y preparar la enmienda candidata; no declarar cobertura por dominio finito, por dibujarse o por que su fórmula parezca regular. No implementar B sin decisión autoral posterior. No promover main, publicar, aprobar CU7 o modificar libro/CLAUDE.md.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED previsto: no hay requisito/toolchain/runtime nuevo. VERIFICATION_INFRASTRUCTURE_IMPACT: depende solo de la selección/inventario focal necesario; se documentará el delta real. GUIDE_IMPACT: actualización de autoría clic/Q si se implementa A; CU10/E1 se conservan.
