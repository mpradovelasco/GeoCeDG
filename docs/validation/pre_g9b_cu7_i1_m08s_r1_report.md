# CU7-I1-M08-S-R1 — diagnóstico y corrección acotada

Estado: candidato pendiente de revisión y smoke autoral. El smoke de 5bc7d7dac NO es PASS.
Base exacta: 5bc7d7dac1c2466bfaecbcacaa04d8e5386ced63, árbol
733387ac71f1418cadaca3bf352c36033426c701, worktree limpio al iniciar.
Rama correctiva codex/pre-g9b-cu7-i1-m08-s-r1; original y main conservados.
Clasificación: diagnóstico primero; corrección Desktop/diagnósticos después, sin cambiar
admisibilidad, geometría, identidad ni persistencia. .github/copilot-instructions.md no existe.

## Caracterización focal antes de cambios productivos

Entrada autoral C:/Temp/TestM08-S.cedg, 28228 bytes, SHA-256
4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d.
Original solo leído; copia preservada en artifacts/pre-g9b-cu7-i1-r1/author-input/.
No se incorpora el documento privado al producto ni al libro.

Definición de h: I=(0.4965866445356883,1.7854818601338847),
J=(-0.5082406249102692,2.5763181370126467); f=Circle(I,J); L=Point(f);
g=Line(L,xAxis); K=Intersect(g,yAxis); M=Midpoint(K,L); h=LocusV2(M,L).
La sonda carga el archivo NATIVO con AppGeoCeDG.loadFile y crea ReferenceR1 cerca de
F_h(0), no reasocia por coordenadas. Q=(0.8976484384297626,1.7854818601338847).

Resultados Java reales idénticos en 5bc7d7dac y main anterior caf67ffc:

| Dimensión | Resultado observado |
|---|---|
| Identidad de h | geo:aa7268331ad4d3a3d1cc946e989b109b (leída del registro) |
| Proveedor / evaluador | circle-point/v1 / ReconstructibleLocusEvaluator2D |
| Estado / dominio | VALID, definido; [-pi,pi) |
| Rama / componente | generator.main / generator.main/component/periodic-closure |
| Estructura de componentes | COMPLETE; componente periódico certificado |
| Política | initial(0.1): radio world 0.1, equalDistanceTolerance 1e-10, parameterTolerance 1e-12; 32768 evaluaciones, 512 subdivisiones, 80 refinamientos, 1024 candidatos |
| Hit gráfico | DrawLocusV2.hit positivo en F_h(0) |
| Consulta | UNRESOLVED_NUMERICAL_SEARCH; BOUNDED_EVALUATOR_SEARCH |
| Candidato | uno, u=-6.407985253531479e-11; distancia 0.010000000000000009; celda [-0.0654498469507665,0.06544984694876632] |
| Evidencia local | ESTABLISHED: mínimo regular estricto en celda acotada; FLOATING_POINT_UNCERTIFIED |
| Diagnóstico | One local candidate was established, but bounded semantic search did not establish global uniqueness |
| Creación por clic | null, XML sin mutación |
| Forward explícito | Point(h,"generator.main",0) definido: (0.8876484384297626,1.7854818601338847) |

Es limitación HEREDADA, no regresión introducida por M08-S. Se ejecutaron las mismas
sondas con HEAD separado en caf67ffc (sin mover main), y se retornó a la rama correctiva.
La clase del resolvedor es idéntica en ambos commits. La creación arrastrable de SplineV2
también precede a M08-S; el candidato previo no aportó la autoría Q explícita requerida.

No confundir la completitud de componentes con la completitud de la búsqueda inversa.
Ni hit ni residual prueban unicidad. UNRESOLVED aquí NO significa ausencia, invalidez,
degeneración o presupuesto agotado. Otra sonda con presupuesto 1 reproduce agotamiento:
UNRESOLVED, NOT_APPLICABLE, diagnóstico de límite; el mismo enum histórico cubre causas distintas.

Evidencias bajo artifacts/pre-g9b-cu7-i1-r1/: diagnostic-current (2 tests, exit hijo 0),
diagnostic-author-current-2 (1 test nativo, exit hijo 0), diagnostic-main (3 tests, exit hijo 0).
JUnit incluye trazas de proveedor, dominio, componentes, candidatos y contadores.
diagnostic-author-current conserva un intento con NO TESTS FOUND, exit hijo 1: configuración
del arnés errónea al confundir path del proyecto con ruta del build compuesto; no defecto del producto.
Las sondas se ejecutan mediante tools/agent/checks/gradle-command-evidence-producer.ps1,
con --tests e init script que habilita XML JUnit. El exit exterior 0 no sustituye al inner_exit_code.

## Puerta normativa y alcance de corrección

ADR 0019 decisión 4 y M08 aprobado §2 pasos 2/4 preservan UNRESOLVED para un único
candidato de búsqueda general y prohíben ofrecerlo. El resultado actual del autor requiere
una decisión normativa para ser ofertable a partir de Q. No se cambiará esa política.
Una enmienda candidata separada debe distinguir admisibilidad local, cobertura y permiso
de selección; ni confirmación ni un nuevo botón pueden convertir el rechazo actual en éxito.

Diseño mínimo independiente para las fuentes YA admitidas: en la herramienta existente
Punto Locus V2, seleccionar L, abrir una opción separada de referencia existente Q y política
world explícita; conservar la autoría de rama/parámetro existente como alternativa.
Q libre o dependiente se lee como valor al confirmar; no se añade al DAG ni a la expresión
guardada. Usar LocusSnapshotPlacement2D y construcción forward actual, con elección exacta
del candidato y guardas de vigencia. Cancelación antes de commit no muta; el host conserva
su único Undo. No nuevo comando dinámico P=Operacion(L,Q), codec, rol o formato.
Las revisiones/identidad, limpieza de inputs fallidos y publicación forward existentes se
reutilizan; no se afirma una transacción general de reconstrucción M09.

Antes de publicar un candidato productivo se deberán demostrar Q libre/dependiente,
desacoplamiento de Q, actualización de L, ambigüedad/cancelación/vigencia, Undo/Redo,
reapertura nativa y copia/compatibilidad. Mensajes distinguirán creación de movimiento,
preservando los estados y refinando solo diagnósticos, sin cambiar permisos de admisión.

## Verificación y compromisos

Si hay corrección productiva: INTEGRATED_PHASE con PHASE registrada más INTEGRATION,
STATIC e INFRA_UNIT cuando corresponda a catálogo. Los focos previos no son aceptación.
BOOTSTRAP IMPACT: NO CHANGE REQUIRED; ningún runtime, instalación o prerequisite cambia.
GUIDE_IMPACT: se deberá explicar Q explícito y el rechazo del caso general; CU10 conserva
la integración editorial posterior. No ejecutar CU10 ni cambiar geocedg_book.
M08-D, M09/A0, I2G/I3 y reconstrucción general excluidos. Deuda exacta de tangencias intacta.
No publicación, main, etiquetas, releases o paquetes. Detener la ampliación de admisibilidad
para revisión autoral; no declararla implementada ni aprobar unilateralmente el candidato.
