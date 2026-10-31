# M08-S-R2 Desktop — plan de implementación

Fecha 2026-11-01. Candidato, selfApproved=false. Base H1 6c6c863ba28413af4e14f350f88832f72b4caa1d,
con entrada documental 632962595e23b9b7e860d3c3d8dc717248484028.
La decisión autoral posterior resuelve el alcance: fijo, visible, sin filtros Desktop.

Capa: Desktop y registro de herramienta; kernel H1 intacto. Modo separado, eventos
L→Q consumidos antes del host, estado por controlador, cancelación y confirmación
contextual. Política de sesión con los parámetros existentes, sin datos persistentes.
Construcción exclusivamente mediante Selection.commit; un Undo explícito porque la
ruta interceptada no alcanza el callback creador del host. Dirección avanzada conservada.

VERIFICATION_CLASS=INTEGRATED_PHASE, frozenAtPhaseStart=true. Registrar PHASE desde
JUnit ejecutado, STATIC/INFRA_UNIT/PHASE/INTEGRATION sobre candidato congelado.
Pruebas de estado, cancelación, oferta única/múltiple, presupuesto/caducidad, persistencia,
native h/ReferenceR1 y regresión R1/H1. Smoke autoral posterior, no autoaprobación.
BOOTSTRAP IMPACT=NO CHANGE; GUIDE_IMPACT=UPDATE. Registro acotado de catálogo, sin runner nuevo.
No ADR aceptados, main, libro, M08-D, M09 ni arrastre general. Detenerse al entregar.
