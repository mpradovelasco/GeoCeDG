# M08-S-R2 Desktop D1/D2 — plan focal

Clasificación: corrección de implementación Desktop y precisión documental candidata.
Base verificada limpia: 5e9f341d7271c3ffb87765d73440f40cc665b73e,
árbol 882324ec71fac890501a7620294f9ba34a5c01d3. Rama Desktop existente;
se preserva el candidato por su objeto Git inmutable. main/origin main caf67ffc intactos.

D1: componer Selection R1/R6 y Selection R2/H1 existentes mediante un cliente
privado Desktop. R1 UNIQUE/MULTIPLE se ofrecen y publican exclusivamente por
LocusSnapshotPlacement2D.Selection.commit. Se conserva la confirmación compacta de
la herramienta integrada también para R1 único; no se añaden pasos respecto del
candidato. R1 MULTIPLE exige elección sin predeterminado. Sólo UNRESOLVED con
Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED permite una nueva consulta R2/H1.
La clasificación real del resolvedor asigna esa causa después de comprobar separación
y ausencia de evidencia insegura, con un candidato. LOCAL_EVIDENCE_INSUFFICIENT,
BUDGET_EXHAUSTED, ausencia certificada, inválido, degenerado y unsupported rechazan.
Una excepción o fallo de publicación nunca dispara otra ruta.

Cada selección retiene su resultado/candidatos/cobertura/operación forward propios.
El cliente verifica contexto de documento/fuente y Q transitorio antes de publicar;
no certifica geometría. Sin nuevo API kernel, codec, identidad o criterio de interior.
No hay API pública para inyectar discovery R6 en H1: en el único fallback permitido
H1 repite su propio discovery con su sesión dura, independiente, necesaria para su
contabilidad. R1 conserva su presupuesto histórico; H1 conserva su presupuesto
acumulado consulta/confirmación. No se afirma límite conjunto nuevo ni se transfiere
remanente ficticio. Ningún agotamiento autoriza fallback. No se repite consulta al
confirmar; se consume la selección vigente de la ruta elegida. Un solo Undo host.

D2: cambiar sólo conciliación candidata: capability/selector visual efímero no se
persiste; selector estructural de componente sí forma parte de la dirección durable.
ADR aceptados y especificaciones normativas no se modifican.

VERIFICATION_CLASS=INTEGRATED_PHASE; frozenAtPhaseStart=true.
Foco antes/después extremo, R1 múltiples, R2 local único y nativo h/ReferenceR1,
rechazos/budget/foreign/stale/cancel, Q independiente/antecedente, propagación,
Undo/Redo/ID/copia/reapertura y regresiones existentes. Catálogo PHASE por procedimiento
canónico y JUnit real. STATIC, INFRA_UNIT si cambia metadata, PHASE e INTEGRATION
sobre SHA congelado; no FINAL. Sin cambios de runner ni reglas de aceptación.
BOOTSTRAP IMPACT — NO CHANGE REQUIRED: mismos runtimes/configuración/prerequisitos.
GUIDE_IMPACT=UPDATE: protocolo candidato del smoke D1; CU10 y libro sin adelantar.
No M08-D/M09/general drag/main/publicación/tags. selfApproved=false; detener tras entrega.
