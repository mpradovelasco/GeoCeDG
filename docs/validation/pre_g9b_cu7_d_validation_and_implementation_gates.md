# PRE-G9B-CU7-D — Matriz focal/adversaria y puertas de implementación

- Estado: **PLAN CANDIDATO — NOT AUTHOR APPROVED**; `selfApproved=false`.
- [Diseño](../architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md),
  [decisiones](pre_g9b_cu7_d_author_decision_sheet.md),
  [evidencia CU7-D ejecutada](pre_g9b_cu7_d_candidate_report.md).
- Las filas S/D/R/K/U son **pruebas futuras**, no PASS ni capacidades implementadas.
  La ejecución CU7-D se limita a la sonda C y STATIC; no hay smoke del producto nuevo.

## 1. Matriz de pruebas

| Id / puerta | Caso | Oráculo y condición exigida |
|---|---|---|
| C01 / CU7-D | N cambia de soporte; opciones mínimas y normales; modo default/contract-update | errores y assessment observables; distinguir rechazo por contrato de reconstrucción prospectiva; XML idéntico ante rechazo |
| C02 / CU7-D | L cambia conductor; sin/con resultado materializado; nuevo cierre sin/con participación previa | observar definición, ID y estado del claim por separado; no generalizar K5 sin condiciones |
| C03 / CU7-D | punto ordinario sin ID y punto semántico durable → token no coincidente, tres modos | observar fresco/retención/rechazo y exactitud del rollback; no inferir éxito por ausencia de excepción |
| S01 / I1 | segmento/afín con una dirección en radio; fuera de radio | dirección forward elegida y none/unique solo con cobertura completa del ámbito |
| S02 / I1 | evaluador general con 0 o 1 candidato localizado | unresolved; no crea punto ni etiqueta auxiliaria |
| S03 / I1 | cruce, lazo y dos ramas con la misma imagen | candidatos semánticos distintos; elección explícita; cancelación sin modificación |
| S04 / I1 | permutar enumeración/orden de resultados, renombrar, variar zoom/DPI/viewport | mismo conjunto/selección semántica; ninguna identidad por índice o rótulo |
| S05 / I1 | Q se mueve/elimina tras crear S; luego L se mueve | S no sigue Q; sí reevalúa su dirección sobre L |
| S06 / I1 | seam periódico en ambos sentidos; nudo de spline; arco abierto | una canonicalización válida; lift exacto; no wrap sobre arco abierto ni duplicado de nudo |
| S07 / I1 | gap, punto-componente, cúspide, componente ausente o split/merge | rechazo/indefinido tipado; sin cruce de hueco o cambio de rama por cercanía |
| S08 / I1 | R5 negativo, transformación no finita, colapso y recuperación | vínculo al locus transformado; nueva consulta colapsada no elige arbitrariamente; S existente conserva dirección según R6 |
| S09 / I1 | revisión cambia entre consulta y commit; candidato forjado/otra fuente | rechazo stale/invalid; rollback sin huérfanos |
| S10 / I1 | agotamiento de evaluaciones/composición/refinamiento | unresolved explícito; contadores deterministas, sin éxito parcial |
| D01 / I4 | F(u)=A*u+B, segmento cerrado; Q interior y exterior | fórmula analítica del mínimo y clamp solo en extremos incluidos; garantía matemática/arimética separadas |
| D02 / I4 | extremo abierto; intervalo sin mínimo; A=0; dominio no finito | causa tipada de inexistencia/degeneración/capacidad, no proyección inventada |
| D03 / I4 | Q/L recorren estados directo, incremental, inverso y tras reapertura | misma receta y resultado para el mismo snapshot, sin autoridad del último u |
| D04 / I4 | dos mínimos, mínimo/máximo estacionarios, mínima distancia constante | no asignar por distancia previa; ambiguous/unsupported/map-not-established |
| D05 / I4 | mapa no monótono, pliegue, singularidad, seam con monodromía | no continuación automática; receta retenida, binding inválido y coordenadas indefinidas |
| D06 / I4 | perder/restaurar componente o prueba; borrar caché | misma receta recupera solo con correspondencia demostrada; nunca otro mínimo más cercano |
| D07 / I4 | constructiva frente a heredada; Q fuera de clase exacta | política preservada; no STRICT por residuo pequeño o por coeficientes flotantes |
| D08 / I4 | desbordamiento, escala/traslación, presupuesto insuficiente | error/garantía veraz y causa no finita/trabajo; no suelo dependiente de viewport |
| R01 / I2 | P=(100,100) → raíz admisible distante con dependientes ordinarios/durables | DA-5: no rechazar por distancia; conservar ID solo por predicado autorizado; recomputar cierre |
| R02 / I2 | point VALUE ↔ token VALUE; token de R1 → token de R2 | mantener P si compatible; liberar/adquirir claims una vez; jamás token compartido entre propietarios |
| R03 / I2 | `Intersect(L,obj)` rico propuesto como punto | elegir solución o rechazo de familia; no convertir dependientes punto en consumidores ricos |
| R04 / I2 | raíz latente/cuarentena, token ajeno/forjado; contacto clase 3 | no materializar; no reasociar; conservar libros y XML ante fallo |
| R05 / I2 | raíz localmente admisible con completitud global NOT_ESTABLISHED | admitir bajo contrato actual sin declarar completitud nueva |
| R06 / I2 | contacto F/T válido y contacto sin prueba exacta/estructural | preservar evidencia CU3 y grados; no usar certificación de punto crítico como prueba de intersección |
| R07 / I2 | rol interacción/principal/proyección distinto al candidato | no RETAIN por clase Java; reemplazo solo si evaluación independiente lo ofrece |
| R08 / I2 | punto viejo sin ID; candidato crea auxiliares con participación | contexto previo al parseo; ID nuevo al commit; ningún objeto/ID publicado si falla |
| R09 / I2G | productor de 2 salidas con hermanos sin/con ID y dependientes | rechazo antes de borrar, o adaptador completo demostrado; nunca pérdida silenciosa de hermanos |
| R10 / I2 | nueva dependencia posterior a P | P3-R1: recolocación mínima legal; mismo orden relativo de elementos no afectados |
| R11 / I2 | R/L/target/helper depende directa o indirectamente de P | INVALID_DAG antes de publicar; XML/identidades/ledger sin cambios |
| R12 / I2 | punto redefine a posición que hace una recta dependiente degenerada | operación válida puede confirmar; recta indefinida con diagnóstico, sin identidad nueva por proximidad |
| R13 / I2 | cambiar estado entre assessment/confirmación; cancelar | stale/cancel; sin commit, Undo extra ni reuso de selección |
| R14 / I2 | inyectar fallo en reserva, instalación, reconstrucción y postcondición | restauración exacta de definiciones, IDs, revisiones, claims, cuarentena y high-water; fallo fatal si rollback imposible |
| R15 / I2 | reemplazo C con cascada ordinaria y durable; impacto incompleto | informe distingue recalculado/indefinido/eliminado; incompleto no ofrece reemplazo |
| K01 / I3 | N=Point(c3), c3 nuevo y ya participante | cierre coordinado; ningún registro público con DAG prospectivo diferente |
| K02 / I3 | L cambia conductor con misma familia; familia/rol incompatible | retención de objeto y continuidad de soluciones se juzgan separadamente |
| K03 / I3 | geometría idéntica con soporte/driver IDs distintos | demostrar barrera efectiva para F, T y transversales; no reutilizar por igualdad visual |
| K04 / I3 | soporte en sitio cambia radio/definición y vuelve | latencia/recuperación solo bajo mismo selector completo, no declarar barrera por valor distinto |
| K05 / I3 | split/merge, orientación, seam, contacto regular → pareja | conservar ADR 0017/0039; pruebas de antiguo claim, retiro/cuarentena y sin transferencia entre familias |
| U01 / I1–I4 | guardar/reabrir definido y dormante, Undo/Redo repetidos | mismos IDs/receta/selector; certificados recalculados, no coordenadas de reparación |
| U02 / I1–I4 | copiar/duplicar cierre completo y copiar cierre parcial | IDs frescos y remapeo exacto completo; política externa/rechazo tipado para parcial |
| U03 / I1–I4 | borrar/recrear y renombrar con reutilización de etiqueta | recreado no impersona ID anterior; selección vieja no se recupera por etiqueta |
| U04 / I4/I2G | lector actual y lector previo frente a receta/version nueva o corrupta | leer el contrato soportado o rechazar seguro; no degradar a punto libre/snapshot |
| U05 / I1–I4 | crear muchos puntos consumidores de misma revisión | no resolver globalmente una vez por hijo; contadores y cache invalidation deterministas |
| U06 / integración | corpus CU0/CU1/CU2/CU3, Classic, spline, transformaciones, DXF/LaTeX | invariantes aprobados preservados; nuevos formatos no cambian documentos no editados |

## 2. Estrategia por incrementos candidatos

| Puerta | Entregable futuro y parada | Verificación propuesta |
|---|---|---|
| I0 — contrato | decisiones autorales aplicadas expresamente, texto normativo aprobado, tabla de transiciones, codec/lectores definidos y perímetro registrado | STATIC; no código hasta esa autorización |
| I1 — M08 S | query/selección/commit por dirección, normal-form Point y consumidores sin nueva verdad geométrica | PHASE propia registrada + regresiones R6/G9U1/CU2/CU3; INTEGRATION del incremento |
| I2 — M09 singleton | predicado cerrado, target sin ID, impacto completo, claims y snapshot anterior al parseo | PHASE propia + A3/R3/CU3 + pruebas focales de rollback/Undo; INTEGRATION |
| I2G — grupo opcional | solo si lo exige el autor: adaptador completo, semántica de hermanos y persistencia | puerta bloqueante antes de completar M09 de varias salidas; INTEGRATION; reclasificar impacto si supera perímetro acotado |
| I3 — soporte/conductor | operación de cierre prospectivo, mensaje tipado y barrera efectiva F/T/transversal | PHASE propia + K01–K05 + CU3-A/B1/B2 + guardado/copia; INTEGRATION |
| I4 — M08 D | proveedor afín riguroso, receta/versionado/lectores y resultado sin historia | puerta matemática y de persistencia antes de UI; PHASE + INTEGRATION; si no hay prueba, detener D sin aproximación silenciosa |
| I5 — revisión | congelar commit/árbol, guía mínima del alcance implementado, smoke autoral y limitaciones | INTEGRATED_PHASE completo; aprobación propia, sin publicación implícita |

No hay identificador PHASE `PRE-G9B-CU7-I` registrado por este diseño. Antes de invocarlo se
necesita un encargo de implementación y el procedimiento canónico de registro/selección; no se
usará una fase de otro paquete como sustituto. DEV está incompleto en la base y debe fallar
seguro; no se altera infraestructura para hacer viable un encargo de diseño.

Comandos de planificación (no evidencia ejecutada CU7-I):

```powershell
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase <id-registrado-aprobado> -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar receipt/identidad y estado de aceptación conforme al contrato vigente. Los hallazgos
documentales/gobernanza son diagnósticos, no se ocultan ni se convierten en aceptación de producto.
La sonda CU7-D usa un productor existente como evidencia observacional: no es un perfil DEV
completo ni amplía una selección PHASE.

## 3. Smoke de CU7-I propuesto, no ejecutado

1. Abrir una copia del modelo del autor; verificar política documental y registrar checksum
   del original sin modificarlo. Crear S en una componente certificada, mostrar la dirección,
   mover Q y L por separado y comprobar la diferencia de dependencias.
2. En cruce o preimágenes coincidentes, elegir/cancelar explícitamente; variar zoom/DPI y
   confirmar que la elección no cambia por la presentación.
3. Redefinir un P con dependientes hacia una raíz distante. Presentar efecto e identidad
   retenida/nueva; verificar degenerate downstream, Undo/Redo y ausencia de objetos huérfanos.
4. Probar ciclos y un productor con hermanos. Rechazar antes de mutar si I2G no está autorizado;
   si sí lo está, verificar ambos hermanos y todos sus consumidores después del commit/Undo.
5. Cambiar soporte/conductor, observar causas tipadas y que los puntos F/T/transversales no
   heredan otras raíces. Verificar también cambio en sitio y recuperación legítima.
6. Guardar/reabrir definido/dormante, duplicar y borrar/recrear etiquetas. Repetir selección
   tras Undo; ninguna referencia antigua se reasocia por etiqueta.
7. Solo si I4 fue incluido: mover Q por el dominio afín, extremos y fallo de prueba; comparar
   con la referencia analítica y repetir sin cachés, por camino directo e inverso.

El smoke del agente sirve como evidencia técnica. La aceptación funcional/alcance corresponde
al autor. No se atribuye a CU7-D ninguna de estas operaciones futuras.

## 4. Puertas que permanecen posteriores

FINAL de CU10, con `CU10-GUIDE-INTEGRATION-CHECK` y el expediente E1; ninguna ejecución
anticipada por este documento. La deuda exacta generalizada de tangencias conserva su posición
posterior a CU10 y al trabajo editorial. Cualquier impacto global detectado en CU7-I se somete
a la clasificación del contrato operativo: el plan no rebaja una comprobación obligatoria.
