# PRE-G9B-CU7-I — Propuesta concreta de incrementos

## Autorización posterior de 2026-10-29

Author decision of 2026-10-29 approves the consolidated design and prospective normative text
identified by commit 7e7a90e4e39db04ea49ee9ab1acf69ea34c81080. CU7-D is PASS / AUTHOR APPROVED.
The same decision separately authorizes only I2a+I2b, M09 singleton. Earlier statements of
pending review or no authorization below record the preceding design stage and are superseded
by this decision. I1/I2G/I3/I4/I5-global remain excluded. No future operation is thereby
implemented. M09 remains partial; see the CU7-D closeout record for exact scope.

- Estado: **I2a+I2b AUTHORIZED — M09 SINGLETON ONLY; OTHER INCREMENTS NOT AUTHORIZED**, 2026-10-29; `selfApproved=false`.
- Dirección: D-CU7-01–11 adoptadas con precisiones; diseño CU7-D aprobado sujeto a
  conciliación D-CU7-12. D-CU7-13 mantiene CU7-I sin autorización.
- [Disposición y candidato consolidado](../validation/pre_g9b_cu7_d_consolidated_candidate.md),
  [ADR 0041](../adr/0041-locus-v2-point-placement-and-point-redefinition.md),
  candidatos [M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md) y
  [M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md).
- [Matriz focal/adversaria](../validation/pre_g9b_cu7_d_validation_and_implementation_gates.md):
  las referencias R/S/K/D/U son obligaciones futuras, no evidencias PASS.

## 1. Orden y reglas de entrada

Se conservan los IDs históricos de puertas para no romper la matriz:

```text
I0 (contrato y encargo exacto) -> I2a -> I2b [I2: M09 singleton] -> I1 [M08-S]
                                 |
                                 +-> I2G (multisalida: contrato y autorización propios)
                                 +-> I3  (soporte/conductor: contrato y autorización propios)
I0 + contrato/prueba de mapa + I1 -> I4 (M08-D, diferible si falla su clase afín)
alcance realmente autorizado/ejecutado -> I5 (revisión por incremento/perímetro)
CU10 conserva su FINAL y sus compromisos; no es una salida automática de I5.
```

Las flechas son dependencias técnicas, nunca autorizaciones. El primer encargo recomendado
es **solo I2a/I2b**, con rechazos de grupos, roles y operación coordinada no soportados.
I1 no es prerrequisito de M09: I2 consume resultados/token existentes. I3 no es prerrequisito
de I2 y no se habilita al aceptar singleton. I2G no es prerrequisito de ese primer candidato,
pero sí de declarar completo el alcance multisalida de M09. I4 no bloquea I2/I1.

Antes de cualquier código: autor aprueba el texto consolidado, fija commit/árbol base exactos,
perímetro y exclusiones; se revalida estado de código/registro; se prepara la selección PHASE
mediante el procedimiento canónico. No se inventa aquí un ID ejecutable registrado.
Cambiar selección/infraestructura exige su revisión de impacto; este plan no exime sus gates.

## 2. Primer incremento propuesto — M09 singleton

| Etapa | Alcance y precondiciones | Pruebas/gate de salida |
|---|---|---|
| I2a — evaluación y transacción | target explícito singleton neutral VALUE; tabla cerrada de transiciones, candidate closure completo, epochs vigentes, DAG/posición P3-R1. Snapshot antes del parseo/enrollment; soporte de target sin ID con ID nuevo al commit, nunca falsa retención | R01–R08, R10–R14; U01–U03. Conservar/reemplazar/retirar claims exactamente una vez; rollback exacto en cada punto de fallo; no publicación parcial |
| I2b — consumidores y rechazos | misma evaluación compartida para Desktop/algebra/inspector/headless; impacto ordinario+durable completo; acción explícita del modo; sin geometrias nuevas en frontend | R03–R05, R07, R09 (rechazo), R13, R15–R16; U01–U03/U05–U06. Error tipado con sujeto y recuperación, cancelación distinta del rechazo y un Undo por éxito |

Tabla inicial cerrada: ordinary singleton neutral → token VALUE; semantic-address VALUE ↔
token VALUE; token VALUE → otro R/token explícitos. P puede moverse arbitrariamente: la
coincidencia no se exige. Cada consumidor revalida su propia continuidad, aunque P conserve ID.
Structural CU3 → otra familia se mantiene fuera hasta caracterizar y aprobar su transición;
los contactos ya admisibles pueden ser candidatos de token conservando sus grados reales.

Fuera de alcance: conversión de roles interacción/principal/projection-defined, separación de
hermanos, remapeo selectivo, habilitación I3, nueva proyección, codecs dinámicos y adopción de
autoridad. Un grupo incompleto, rol incompatible, raíz no admisible, ciclo o assessment stale
no se vuelve admisible por confirmación. Reemplazo heredado solo si su evaluación independiente
lo ofrece con impacto completo, sin fallback automático ni reasociación.

R16 cubre peticiones que necesitan la operación nueva I3: rechazo explícito/tipado sin mutación.
Esto no prohíbe rutas de cambio de fuente ya admitidas por los contratos actuales. La evidencia
K5 previa se conserva y guía la distinción, no se usa para declarar una corrección inexistente.

Salida I2: contrato/pruebas de singleton cumplidos, PHASE registrada + INTEGRATION del candidato
guardado y smoke autoral del perímetro; informe de limitaciones dice **M09 PARCIAL — I2G pendiente**.
No cerrar M09, habilitar I3 o iniciar otro incremento por ese PASS. Si el cierre de impacto,
la atomicidad o el predicado no se pueden establecer, detener y devolver alternativa al autor.

## 3. Incrementos independientes siguientes

| Incremento | Entrada/alcance | Pruebas y salida bloqueante |
|---|---|---|
| I1 — M08-S | tras I2 como orden elegido; autorización propia. Consulta y elección de dirección establecida; normal-form forward; sin dependencia ficticia de Q | S01–S10/U01–U03/U05–U06, R6/G9U1/CU2/CU3. Mover Q no mueve S; L sí reevalúa la dirección; candidatos unresolved no crean punto; PHASE+INTEGRATION y smoke propio |
| I2G — multisalida | autorización propia del adaptador y contrato completo de roles/grupo. No asumir API detach upstream. Entrada con I2 y prueba de reconstrucción de todos los hermanos | R09 (admisión solo ahora)/R10–R15/U01–U04/U06. Preservar hermanos, dependientes ordinarios/durables, persistencia/Undo y fallos; sin mapping completo rechazar. PHASE+INTEGRATION; reclasificar impacto si exige cambios generales upstream |
| I3 — soporte/conductor | solo dirección de diseño aprobada. Antes del código, contrato prospectivo y autorización propios; tras I2, sin dependencia de I2G salvo que el productor lo requiera | K01–K05/R11/R13–R16/U01–U06. Registros y DAG reconstruido concordantes, padres nuevos staged, fingerprints/certificados invalidados; barreras efectivas F/T/transversal; PHASE+INTEGRATION y smoke |
| I4 — M08-D | autorización propia tras I0/I1 y diseño de mapa/codec. Provider debe demostrar fórmula ideal afín no colapsada, dominio y correspondencia; coeficientes float actuales no bastan | D01–D08/U01–U06, prueba matemática + persistencia/lector previo antes de UI. Historia/caché no elige mínimo; si falla la clase afín, diferir D con informe explícito. PHASE+INTEGRATION si se implementa |

Cada salida exige artefactos guardados: contrato implementado, diff mínimo upstream justificado,
pruebas y logs con exit/identidad, informe de compatibilidad y límites, guía del alcance que se
implementó y smoke correspondiente. La comprobación integrada no acredita por sí sola aprobación
autoral ni publicación. No hay ejecución ni registro PHASE de estas propuestas en esta entrega.

## 4. Contrato previo adicional de I3

Su futuro encargo debe fijar explícitamente:

1. Conjunto de objetos afectados, cierre de padres prospectivos y reglas de participación;
   distinguir N cambiado de soporte, fuente con nuevo conductor y cambio en sitio del soporte.
2. Validación del host DAG y del grafo de identidad sobre el mismo overlay sellado, incluyendo
   soporte/coordenada que no aparecen como argumentos del comando serializado.
3. Assessment e impacto completos, epochs/lease y snapshot de entrada; una sola publicación
   de padres, registros, revisiones y claims; fallo restaura el documento completo.
4. Correspondencia estructural y barreras por cada familia F/T/transversal; retener L/P no
   conserva por sí solo tokens ni certificados. Ninguna equivalencia por etiqueta/coordenadas.
5. Undo/Redo, guardado/reapertura definido/dormante, copia/remapeo de cierre completo, lector
   incompatible y high-water sin reasignaciones; migración solo explícita si es necesaria.
6. Vocabulario tipado de rechazo/indefinición/recuperación; preservar política documental y
   grados de integridad. Ni estimación ni residuo pequeño establecen STRICT.

Hasta superar esas entradas, solo se plantea hacer explícito el rechazo de la petición no
soportada; no se corrigen registros de forma oportunista ni se amplía el ordinary refresh §10.

## 5. I5 y autorización que se solicita después de esta entrega

I5 revisa el alcance efectivamente ejecutado con commit/árbol congelados y pruebas acumuladas;
no borra las etiquetas de incompletitud M09/I2G o M08-D. La autorización separada futura debe
nombrar incrementos, exclusiones, dependencias, perfiles registrados y gates de aceptación,
además del commit/árbol base exactos. Propuesta inmediata: **I2a+I2b solamente**.

FINAL de CU10 conserva `CU10-GUIDE-INTEGRATION-CHECK` y E1. La deuda
`DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` sigue después de CU10 y del trabajo editorial.
Ni la revisión de este plan ni diferir D autoriza generalizar exactitud o modificar el libro.
