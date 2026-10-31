# CU7-I1-M08-S-R2 — precisión de selección local individual

Estado: **CANDIDATO, NO NORMATIVA VIGENTE, SIN IMPLEMENTACIÓN PRODUCTIVA**.
Autorización: precisión autoral de 2026-10-31; preparación en esa fecha. `selfApproved=false`.
Base exacta: `3da1218e7576083b387cc0ec6b019a82dd0377b6`, árbol
`238d157270b0b34f8ba0435aefb71e6e9ddfcbc4`, limpio al iniciar. Rama nueva:
`codex/pre-g9b-cu7-i1-m08-s-r2-refinement`. R1 y R2 se conservan íntegros.

## 1. Diagnóstico del archivo concreto

Inspección nueva, sólo lectura, de `C:/Temp/TestM08-S.cedg`: 28228 bytes, SHA-256
`4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`, igual al modelo de R1.
Contenedor ZIP con `geogebra.xml`; no hay un objeto Q ni ReferenceR1. La referencia de las
pruebas anteriores fue creada por la sonda: (.8976484384297626,1.7854818601338847). No se
atribuye ese punto al archivo ni se conoce la posición de cualquier Q añadido en una sesión
no guardada del autor. Para el smoke se crea ese fixture explícitamente antes de la herramienta.

La cadena almacenada es Circle(I,J)→f; Point(f)→L; Line(L,xAxis)→g;
Intersect(g,yAxis)→K; Midpoint(K,L)→M; LocusV2(M,L)→h. I y J son entradas libres:
I=(.4965866445356883,1.7854818601338847), J=(-.5082406249102692,2.5763181370126467).
El ID de h se lee del atributo geocedgId: `geo:aa7268331ad4d3a3d1cc946e989b109b`.
Los nombres se usan para inspeccionar la sintaxis guardada, nunca para inferir asociación
durable ni para seleccionar objetos en la futura operación.

El registro nativo no contiene `authorityPolicy`. Según `DocumentAuthorityRecord`, su lector
actual aplica `legacy-representation-authority/v1`. Ésta es una conclusión sobre el archivo
guardado, no sobre posibles cambios de política de una sesión abierta. El registro contiene
identidades de h y su construcción; no serializa un certificado inverso ni la definición
runtime de sus componentes. Las coordenadas dibujadas de M no definen el locus.

Evidencia **Java real heredada de R1**, no reejecutada aquí: proveedor circle-point/v1,
evaluador reconstruible, VALID, dominio [-pi,pi), rama generator.main, componente
generator.main/component/periodic-closure con existencia COMPLETE; hit gráfico positivo.
Para ReferenceR1 y radio .1: un candidato u=-6.407985253531479e-11, distancia
.010000000000000009, celda [-.0654498469507665,.06544984694876632], evidencia numérica
ESTABLISHED/FLOATING_POINT_UNCERTIFIED. 162 evaluaciones y 55 refinamientos.
Cobertura BOUNDED_EVALUATOR_SEARCH; GLOBAL_UNIQUENESS_NOT_ESTABLISHED, no presupuesto agotado.
Forward explícito Point(h,"generator.main",0) definido en
(.8876484384297626,1.7854818601338847). La misma caracterización en main anterior y R1
demostró limitación heredada, no regresión M08-S. R2 sólo añadió diseño, de modo que no la corrigió.

Causa exacta vigente: `LocusPointInteractionResolver2D.classify` deja UNRESOLVED si hay un
solo candidato y no hay cobertura completa; `getUniqueCandidate()` devuelve null;
`LocusSnapshotPlacement2D.commit` acepta sólo UNIQUE/MULTIPLE. La interfaz no puede anularlo.
COMPLETE de componentes no es completitud de la búsqueda inversa. Ausencia de unicidad no
significa ausencia de posiciones válidas, y cambiar el texto del diagnóstico no permite crear.

La captura intervalar v1/v2 usa las constantes del driver real (semiejes, base y centro),
no una elipse ajustada a M. Su adaptador devuelve periodo 0: no se deduce cierre exacto de
cos/sin en ±Math.PI. El componente periódico certificado y la canonicalización del proveedor
se conservan, pero no habilitan por sí solos una caja que cruce el seam en otro modelo.
Las ofertas nuevas se limitarían inicialmente a direcciones interiores lejos de esa frontera.

## 2. Contrato matemático mínimo: elegir un argumento, no invertir una función

Sea a=(L,b,c,u0) una dirección canónica explícita. Una vez elegida, P=F_L(b,c,u0) es una
construcción forward. Para esa definición no es necesario resolver F(u)=Q, ni minimizar
d(u)=||F(u)-q||², ni demostrar que F sea global o localmente inyectiva. La dirección, y no
su imagen cartesiana, es el dato escogido. Dos argumentos distintos pueden dar la misma imagen.

El **núcleo matemático suficiente** para seleccionar desde Q consta de:

1. Dirección canónica persistible, fuente/revisión/política identificadas y actuales,
   pertenencia inequívoca de u0 a la rama/componente certificada y evaluación semántica válida.
2. Vínculo fiel entre esa definición y el modelo usado como evidencia, en la autoridad activa.
3. q binary64 finito copiado de Q durante la selección, radio rho world explícito, y una caja
   exterior B0 que contenga F(u0), con sup(x en B0)||x-q||² < rho². No se necesita que toda
   una celda vecina esté dentro del radio. Igualdad incierta o simple residual flotante no sirven.
4. Evaluación forward normal actual finita y coherente; comprobación adicional de que su
   representación satisface la política numérica declarada. No convertir ese control en prueba exacta.
5. Contexto seguro/vigente, elección explícita cuando falta unicidad, y publicación forward
   sin Q como padre nuevo. No se infiere permiso del resultado UNRESOLVED existente.

Si F(u0)∈B0 y B0 está en la bola de radio rho alrededor de q, entonces la dirección elegida
produce un punto semántico suficientemente próximo. Ésa es toda la conclusión métrica.
No hay una afirmación sobre otras direcciones ni una prueba de mínimo o de proyección exacta.
q es la posición numérica consultada de Q, no un certificado del valor ideal de Q dependiente.

Regularidad no es necesaria para **definir** un punto forward en una dirección cuya existencia
ya esté probada. Sí puede ser una precondición de una modalidad de selección/edición particular.
No se ofrece una singularidad no resuelta, componente incierta o dirección no certificada
invocando este argumento matemático. El primer perímetro de admisión siguiente es más conservador.

## 3. Primer perímetro recomendado y relación con R2

Recomendación prospectiva: modalidad **VALID_NEAR_ADDRESS**, con núcleo anterior y guarda
inicial **REGULAR_AT_ADDRESS_ESTABLISHED**. Mantener carta regular R2 como prueba suficiente
opcional; no exigir su ancho ni la inclusión de F(U) en la bola para todos los casos.

La guarda inicial exige C1 en un entorno abierto de u0 demostrado por el modelo capturado y
sus predicados estrictos, más una cota exterior de F'(u0) que excluya cero en al menos una
coordenada. No basta marcar REGULAR en una muestra. Por continuidad, esa derivada conserva
su signo en algún entorno; el teorema de la función inversa para esa coordenada prueba
existencia de una carta local inyectiva, **sin calcular su tamaño ni aislar una raíz de d'**.
Si el proveedor no prueba C1 cerca del punto, ni siquiera un diferencial puntual no nulo
demuestra esa propiedad. Una prueba cuantitativa de carta R2 también cumple la guarda.

La primera familia seguiría siendo programas reconstruibles v1/v2 fielmente capturados,
interiores suaves con predicados decididos con margen; spline estructural C1 y afín son
controles/adaptadores sólo con fidelidad probada. Rechazar seams/knot/extremos no cubiertos,
singularidades sin resolver, mesetas clasificadas DEGENERATE por la ruta de descubrimiento,
componentes colapsadas, ambigüedad de componente, barreras observadas, presupuesto agotado y
vínculos de autoridad no establecidos. No extender el perímetro a todo evaluador general.

| Cuestión | R2 conservado | Precisión candidata |
|---|---|---|
| Mínimo local/global | no se reclama | igual |
| Distancia | caja de toda F(U) dentro del radio | basta caja del punto F(u0) |
| Regularidad/inyección | signo uniforme en U explícito | C1 cercano + derivada puntual rigurosamente no nula; carta R2 alternativa suficiente |
| Ofertas próximas entre sí | celdas separadas obligatorias | no exigir separación de celdas si direcciones exactas están demostradas distintas y sus componentes son inequívocas |
| Evidencia ambigua | rechazo | rechazo si no se puede establecer igualdad/diferencia canónica, pertenencia o fidelidad; no desempatar por tolerancia |
| Permiso | frontera nueva, confirmación local | igual; R6 y getUniqueCandidate intactos |
| Arrastre | no ampliado | igual; guarda de creación no es autorización de continuación |

El ancho de una celda de descubrimiento es un artefacto de búsqueda, no identidad. Dos ofertas
con celdas solapadas pueden representar direcciones diferentes ya establecidas: se presentan
ambas con sus handles de consulta y datos semánticos, nunca se deduplican por distancia.
Si lo único disponible son cajas de raíces solapadas sin dirección individual demostrada,
siguen rechazadas. Las semillas R6 son material no autoritativo; no se relaja su propia regla
de separación ni se recicla un rechazo inseguro como permiso.

Estado tipado propuesto, antes de APIs: conservar las dimensiones R2 de cobertura global,
estructura, vínculo de autoridad, barreras/trabajo, vigencia y permiso. Sustituir sólo la
dimensión local de la vía nueva por `DIRECTION_VALIDATED` + evidencia de guarda
`REGULAR_AT_ADDRESS_ESTABLISHED` o `REGULAR_CHART_ESTABLISHED`; distancia
`OUTWARD_POINT_BOUND_ESTABLISHED`; mínimo siempre `NOT_CLAIMED`. Permiso únicamente
`EXPLICIT_LOCAL_ADDRESS_CONFIRMATION_REQUIRED`, incluso una sola oferta local. Falta de
alguna prueba necesaria: NOT_ESTABLISHED/NOT_OFFERABLE con causa específica. No son enums
implementados. Los puntos aún sólo numéricamente verificados no adquieren ese permiso.

## 4. Caracterización focal nueva y lo aún no establecido

La [sonda no productiva](../../geocedg/validation/pre-g9b-cu/m08s-r2-refinement-native-probe.py)
lee el XML exacto y verifica su hash antes/después. Confirma cadena, ausencia de Q y de
authorityPolicy, ID explícito y base del círculo almacenada. No carga Java ni asocia objetos.

Para la familia matemática E_r(u)=(I_x/2+(r/2)cos u,I_y+r sin u), r∈[1.27,1.29], demuestra
con racionales exactos que E_r(0) está dentro del radio .1 de ReferenceR1 y E'_y(0)=r>0.
No necesita encerrar una celda completa. En cambio, la celda [-1/2,1/2] no cabe en esa bola:
sin(1/2)≥1/2-(1/2)^3/6 y 1.27 veces esa cota excede .1. Esto ilustra por qué exigir cercanía
de toda una celda puede rechazar un argumento perfectamente próximo. R2 podría subdividir
y acabar certificando una celda menor; no se afirma que R2 sea matemáticamente inviable.

La sonda también conserva **los bits del parámetro descubierto en R1**, sin redondearlo a
cero: encierra sin/cos en ese argumento mediante |sin u0|≤|u0| y cos u0≥1-u0²/2, y demuestra
la misma cota de distancia y signo de derivada para la familia. No sustituir la dirección
descubierta por 0, por un índice o por la dirección que produzca coordenadas más parecidas.
Una futura oferta debe certificar y publicar exactamente su argumento seleccionado.

Ésta es evidencia sobre la familia derivada y la sintaxis nativa. Falta todavía ejecutar y
verificar la captura real v1/v2 de h, sus constantes/normalizaciones, C1, cotas exteriores
puntuales y vinculación de revisión. No sustituir esa captura por las coordenadas guardadas,
la fórmula ideal o el forward flotante observado en R1. Para este archivo la autoridad
guardada es heredada; si el usuario adopta autoridad constructiva, aplicar las transferencias
ya aprobadas o rechazar. No ampliar ADR 0040 ni STRICT para resolver M08-S.

La prueba analítica y la inspección permiten recomendar la nueva modalidad como perímetro
seguro **condicional a sus testigos reales**. No justifican declarar h ya admitido ni activar
selección desde el UNRESOLVED actual. La siguiente puerta prioritaria es ese puente de kernel,
antes de implementar la interfaz.

## 5. Textos candidatos, sin editar autoridad aceptada

Tras aprobación, conciliar con la [enmienda R2](pre_g9b_cu7_i1_r2_normative_amendment_candidate.md):

Texto mínimo candidato común a ADR 0019 decisión 4 y ADR 0041 decisión 3:

> Exclusivamente para M08-S desde una referencia transitoria, una frontera separada puede
> autorizar la elección explícita de una dirección individual actual, canónica y válida en
> una componente certificada, con cota exterior de distancia a la posición consultada y
> con las guardas de proveedor declaradas. No se exige que constituya un mínimo ni que toda
> su celda de descubrimiento esté dentro del radio. El primer perímetro exige regularidad
> en la dirección y entorno C1 establecidos rigurosamente, o una carta regular demostrada;
> no admite singularidades, pertenencias, autoridad o seguridad no establecidas. La falta
> de unicidad global exige confirmación contextual incluso con una única oferta descubierta.
> El status, cobertura y permiso históricos R6 no cambian; un UNRESOLVED sin la nueva
> evidencia independiente sigue sin permiso. Sólo persiste la construcción forward y sus
> padres semánticos, nunca Q ni la consulta. Esta admisión no amplía arrastre, exactitud o STRICT.

- **ADR 0019 decisión 4/Evidence model:** añadir una frontera separada exclusivamente M08-S
  desde Q para selección explícita de dirección individual actual y válida, con cota exterior
  puntual y guarda de proveedor declarada. Regularidad/inyección no se confunden con mínimo;
  una carta cuantitativa R2 basta pero no es necesaria. R6/UNRESOLVED permanecen intactos.
- **ADR 0041 decisiones 2/3:** la elección de argumento forward no afirma inversión única ni
  mínimo. Sin unicidad global, confirmar contextualmente incluso una única oferta; Q y prueba
  se descartan como autoridad después de publicar. El flujo ordinario es herramienta L→Q.
- **M08 §2 y locus-v2-point-interaction:** incorporar las dimensiones y predicado de §2/3;
  no presentar un candidate R6 de «mínimo» como nuevo testigo ni ampliar sus getters/arrastre.
- **Public surface §§11/13 y contrato UI:** distinguir herramienta desde referencia, dirección
  avanzada y clic directo previo; consumir el mismo permiso compartido, guardar sólo forward.

No hay cambio de formato, ID, codec ni política documental. Si el puente o la publicación
exigen una ampliación arquitectónica distinta, detener ese trabajo para decisión específica.
Ver [interacción propuesta](pre_g9b_cu7_i1_r2_refinement_interaction.md),
[matriz/smoke](../validation/pre_g9b_cu7_i1_r2_refinement_matrix_smoke.md) e
[informe](../validation/pre_g9b_cu7_i1_r2_refinement_report.md).
