# M08-S-R2 Desktop D1/D2 — candidato correctivo

Estado: candidato pendiente de revisión independiente y smoke autoral.
selfApproved=false. No es PASS — AUTHOR APPROVED.

## Base y perímetro

Base limpia comprobada: 5e9f341d7271c3ffb87765d73440f40cc665b73e,
árbol 882324ec71fac890501a7620294f9ba34a5c01d3, rama
codex/pre-g9b-cu7-i1-m08-s-r2-desktop. El objeto base queda preservado por ascendencia.
main y origin/main locales permanecen en caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4;
H1 permanece en 6c6c863ba28413af4e14f350f88832f72b4caa1d.
No fetch, publicación, etiquetas, release, cambio de libro o ADR aceptado.

D1 es composición de consumidores Desktop existentes, no nueva admisibilidad kernel.
D2 modifica únicamente la conciliación normativa candidata. Los expedientes R1/R2/H1
anteriores y sus resultados permanecen históricos, sin sustitución retrospectiva.

## Causa y solución D1

La herramienta integrada consultaba exclusivamente M08SnapshotPlacement2D (R2/H1).
Su prueba de interior regular rechaza legítimamente el extremo de SplineV2 que R1/R6
ya admite. La sonda anterior usa exactamente SplineV2 recto terminado en (2,0),
Q=(2.02,0.01), radio .1: R6 UNIQUE_ADMISSIBLE_PREIMAGE, H1
COMPONENT_NOT_ESTABLISHED, herramienta sin oferta y XML idéntico.

GeoCeDGReferencePlacement es un cliente privado Desktop, sin API pública nueva.
Conserva selecciones kernel originales; sus Choice visuales sólo encapsulan la
capability de la ruta propietaria. No calculan proximidad, pertenencia, regularidad,
parámetros ni certificados. La tabla define toda la decisión de encaminamiento:

| Resultado R1/R6 | Acción Desktop |
|---|---|
| UNIQUE_ADMISSIBLE_PREIMAGE | Ofrecer su candidato y publicar sólo con commit R1/R6. Confirmación compacta conservada. |
| MULTIPLE_SEMANTIC_PREIMAGES | Ofrecer sus candidatos exactos; elección explícita, sin predeterminado; commit R1/R6. |
| UNRESOLVED_NUMERICAL_SEARCH + GLOBAL_UNIQUENESS_NOT_ESTABLISHED | Cerrar selección R1; consultar H1 con su propia sesión. Sólo EXPLICIT_LOCAL_SELECTION_REQUIRED habilita sus Choice. |
| Cualquier otro resultado no admitido | Rechazo terminal. Incluye presupuesto, ausencia certificada, degeneración, inválido, unsupported y evidencia local insuficiente/insegura. |
| Excepción, cancelación o fallo de confirmación/publicación | Cierre y rechazo/cancelación, sin segunda ruta. |

El resolvedor asigna GLOBAL_UNIQUENESS_NOT_ESTABLISHED después de las comprobaciones
de separación y barreras inseguras, para un candidato sin completitud global. No se
infiere permiso de UNRESOLVED ni del número/residuo/posición de candidatos. H1 debe
volver a demostrar su propia evidencia antes de ofrecer. Incluso una oferta única H1
requiere Confirmar. MULTIPLE R1 sigue siendo MULTIPLE aunque su cobertura no sea global.
No se importa la cobertura, revisión, oferta ni permiso de una ruta a la otra.

## Vigencia, publicación y presupuesto

Cada selección conserva su commit forward y garantías originales. El consumidor
comprueba contexto actual de documento/registro, binding real de fuente, definición,
revisión y Q existente/finito antes de confirmar; el kernel vuelve a validar su propia
selección. La comparación de la posición transitoria de Q sirve sólo para caducidad,
nunca para identidad o reasociación. La publicación consume una sola selección y
el host almacena una sola operación Undo tras éxito. No se almacena Undo en rechazo,
cancelación o elección ajena. El permiso de feature se comprueba también para R1.

No cambia ningún presupuesto kernel. La consulta R1 usa la política/contabilidad
histórica R6; su confirmación usa el forward R1 existente. No se le atribuye la garantía
dura H1 ni un nuevo límite de confirmación. H1 tiene su sesión dura acumulada de consulta,
prueba local y publicación, incluidas evaluaciones anidadas y costes intervalares.

No existe API pública H1 que reciba discovery R6 con el audit y sesión necesarios.
Únicamente en el fallback tipado es necesaria una segunda exploración interna H1.
Ambas consultas usan los campos world configurados, con límites independientes bajo
sus respectivos contratos. No se afirma un límite conjunto, no se reutiliza un remanente
inventado y un agotamiento R1 jamás dispara H1. No hay consulta redundante en admisiones
R1, rechazo terminal o confirmación; no se intenta otra ruta tras fallo de commit.

Q es valor transitorio; sólo dirección forward y padres legítimos se persisten.
Rama/componente/parámetro y su selector estructural mantienen el contrato durable.
La política permanece de sesión, alcance fijo visible de todas las ramas/componentes
admitidas, sin filtrar en Desktop. No cambia arrastre general, codec, DAG ni identidad.

## D2 y normativa

La conciliación candidata distingue explícitamente selector visual/capability efímera
(no persistente) de selector semántico estructural de componente (parte persistente
de la dirección durable). Los bloques candidatos se concilian con las dos rutas;
no se aplican a ADR 0019, ADR 0041 ni especificaciones aceptadas.

## Archivos y capas

- Desktop: nuevo GeoCeDGReferencePlacement y consumo en GeoCeDGReferencePointTool;
  presentación de ruta/cobertura y causas tipadas. No geometría nueva.
- Tests Desktop: Cu7M08SReferenceToolTest; adaptación de Cu7M08SCorrectionTest al
  cliente compuesto. Los casos originales se mantienen, algunos ahora usan R1.
- Sonda optativa nativa: m08s-r2-desktop-d1-native-probe y su init Gradle separado;
  no entra en el producto o discovery normal; el arnés nativo previo no se modifica.
- Documentación: plan, este informe, matriz/smoke D1 y conciliación candidata D2.
- Metadatos derivados de verificación: inventario JUnit y hash en registro, por
  el actualizador canónico y JUnit real; no nuevos comandos/runner/allowlists.

## Evidencias y límites

Raíz nueva exclusiva: artifacts/pre-g9b-cu7-i1-r2-desktop-d1/.
El índice delivery.json identifica el SHA/árbol final, hashes, comandos, códigos hijos,
JUnit y perfiles STATIC, INFRA_UNIT, PHASE e INTEGRATION. Sólo se declara completada
una verificación cuando su evidencia real figura allí. No se ejecuta FINAL.

before/ conserva la sonda anterior y JUnit (child 0). focused-attempt1/ conserva un
error del lanzador de prueba (array de argumentos transmitido como una sola cadena),
no un defecto del producto. focused-attempt2/ conserva dos expectativas erróneas del
arnés: igualdad del ID de soporte tras copia (debe remapearse) y rechazo de un seam
que tenía admisión histórica MULTIPLE válida. Se corrigen los oráculos y se conservan
los intentos; no se cambia el producto para hacer pasar esas expectativas.

focused-attempt3/ completó las 47 pruebas focales (18 herramienta, 9 Correction,
20 SemanticPointInteraction), child 0, sin fallos/skips. Los perfiles canónicos y
la sonda nativa posterior se registran separadamente en delivery.json.

La prueba nativa usa el original C:/Temp/TestM08-S.cedg con SHA256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d.
ReferenceR1 se crea sólo en memoria con las coordenadas autorizadas. El modelo
controlado de la suite no sustituye la evidencia del archivo autoral. Garantía H1:
CERTIFIED_ERROR_BOUND sobre la configuración numérica representada, no exactitud
simbólica, mínimo, completitud global o ampliación STRICT.

VERIFICATION_CLASS=INTEGRATED_PHASE; frozenAtPhaseStart=true.
VERIFICATION_INFRASTRUCTURE_IMPACT=UPDATE_REQUIRED únicamente por catálogo derivado
tras añadir/cambiar identidades JUnit; sin modificación de semántica del verificador.
BOOTSTRAP IMPACT — NO CHANGE REQUIRED: mismos runtimes y herramientas, sin nuevas
variables persistentes, prerrequisitos, servicios, configuración o instalación.
GUIDE_IMPACT=UPDATE: protocolo candidato de smoke D1; integración editorial futura
sigue comprometida en CU10. No se edita geocedg_book ni se atribuye al smoke ejecución.

M08-S sigue pendiente de smoke/aprobación final. M09 permanece pendiente, sin
implementación nueva. M08-D, A0, I2G, I3, reconstrucción general y
DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG quedan fuera de alcance.
Se preserva DEBT-CU3-EXACT-TANGENCY-GENERALIZATION. Detener tras entrega.
