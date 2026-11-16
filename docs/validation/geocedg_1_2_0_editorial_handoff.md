# GeoCeDG 1.2.0 — Expediente científico-editorial

**Estado actual: transferencia técnica provisional; cierre R13 bloqueado.**
El [foco R13](geocedg_1_2_0_r13_terminal_report.md) terminó con 262/265 casos
aprobados y tres fallidos; trece rondas consumidas. La construcción científica
del cono tiene evidencia focal de certificación y dinámica; la recuperación
de imágenes conserva defectos. El flujo normal de imágenes fue observado con
Computer Use, incluido guardado y reapertura. No hay candidato aceptado ni se
promueve el baseline editorial. El manuscrito permanece fuera del alcance.

Expediente de transferencia preparado en GeoCeDG para `geocedg_book`. La incorporación editorial y la aceptación de una referencia de producto son decisiones del libro. Su ledger vigente registra como referencia aceptada `372da89f67c5a3c07100512b6b414a0028319ec5` (`geocedg-pre-g9b-r7-pass`). El baseline técnico del encargo 1.2.0 es `4fc0343a1f0dc59a645ee4fe8ec6c5c40fe2051e`, árbol `7cbabc4bd967d775aa730264614d2a1f5092c996`.

La referencia local publicada de `geocedg_book` es `78fbc88e9e12346b89d6ded411eaae94de2284b9`. La consulta de solo lectura confirmó que las copias inspeccionadas de `AGENTS.md`, `editorial/roadmap/BOOK_ROADMAP.md`, `editorial/style/EDITORIAL_CONTRACT.md`, `editorial/source-mapping/TECHNICAL_BASELINES.json` y `editorial/master-structure.md` corresponden a los blobs de esa referencia.

Estado del expediente en preparación, con caracterización histórica y evidencia focal R9/R10. Los resultados de las rondas anteriores se conservan con su identidad propia. La ronda 7 se ejecutó sobre `8940e4a3cade1ef7ff502c964d55ecd62d4c8a00`, árbol `d05960bbb6946854921546998c0bb6a8458ba5e2`. Sus selecciones focales aprobaron 30 pruebas Shared y 79 Desktop. La selección amplia Shared registró 7 741 aprobadas y las diez omisiones autorizadas. La selección amplia Desktop registró cuatro fallos, corregidos en la ronda 8 documentada a continuación. [Focal Shared](../../artifacts/agent/geocedg-1.2.0/inventory-r7/geocedg-1-2-0.shared/evidence.json) · [Focal Desktop](../../artifacts/agent/geocedg-1.2.0/inventory-r7/geocedg-1-2-0.desktop/evidence.json) · [Shared amplia](../../artifacts/agent/geocedg-1.2.0/inventory-r7/final.shared/evidence.json) · [Fallos Desktop](../../artifacts/agent/geocedg-1.2.0/inventory-r7-disposition/desktop-failure-comparison.json).

La ronda 8 corrigió las dos causas de los cuatro fallos Desktop. Su checkpoint `503a28fad4310bc47aca93213808b5110e47c29b`, árbol `0e1bec6165cb2556b02f24915f9f80be47105c27`, aprobó 81 pruebas de la fase 1.2.0, 2 527 de la selección amplia con una omisión autorizada y 472 de inserción y recuperación. El primer intento amplio se detuvo antes de JUnit por memoria nativa y se conserva con su diagnóstico y reintento justificado. [Focal R8](../../artifacts/agent/geocedg-1.2.0/inventory-r8/geocedg-1-2-0.desktop/evidence.json) · [Amplia R8](../../artifacts/agent/geocedg-1.2.0/inventory-r8-final-desktop-retry/final.desktop/evidence.json) · [Inserción R8](../../artifacts/agent/geocedg-1.2.0/inventory-r8-f3/pre-g9b-r6-plus-f3.desktop/evidence.json).

El candidato posterior `6f1d357ca2224dedd0e84fda0faed2d600154bab` quedó retirado al ampliar el autor la condición de cierre a la certificación de `s_2`. Sus paquetes y ensayos anteriores son evidencia histórica, no la distribución final de R9. El autor aprobó el 2026-11-16 la regla `composition-rotation-geometry/v1`, con geometría de rotación matemática y guardas nativas separadas. La [decisión de autoridad](../architecture/geocedg-1.2.0-variable-rotation-authority-proposal.md) y el [diseño composicional](../architecture/geocedg-1.2.0-compositional-certification.md) registran alcance y precondiciones.

La selección [Shared05 de R9](../../artifacts/agent/geocedg-1.2.0/r9-focal-shared-05/evidence.json) terminó con salida 0 y 94 pruebas aprobadas, sin errores ni omisiones. [Desktop05](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-05/junit/TEST-org.geocedg.desktop.GeoCeDG120ConeCertificationTest.xml) demostró la construcción base completa de `s_2`, sus extremos laterales distintos y un lower de velocidad de `0.988595401396134`. [Desktop06](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-06/junit/TEST-org.geocedg.desktop.GeoCeDG120ConeCertificationTest.xml) y su [ciclo de colocación](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-06/junit/TEST-org.geocedg.desktop.GeoCeDG120ConePlacementLifecycleTest.xml) aprobaron cuatro casos: dinámica, corte, reapertura, representación y colocación real con recuperación transaccional. Son ensayos focales sobre fuentes de desarrollo; no acreditan las puertas globales, el candidato exacto, su aceptación autoral ni su empaquetado.

R10 completa el flujo de historial tras zoom: [14 pruebas Desktop](../../artifacts/agent/geocedg-1.2.0/r10-focal-desktop-03/evidence.json) y [16 MEMORY](../../artifacts/agent/geocedg-1.2.0/r10-memory-01/evidence.json), junto a revisión independiente y Computer Use de colocación real, retirada y recuperación del punto y reapertura. Se conservan exactamente las tuplas de origen y escalas, límites y cachés de cada destino MEMORY, con dimensiones físicas y márgenes compatibles, antes del único DAG. Los hints de tamaño del lector conservan su normalización nativa. La vista sigue sin aportar autoridad geométrica o métrica. La [propuesta aprobada y su evidencia](../architecture/geocedg-1.2.0-native-view-restore-proposal.md) delimitan la clase demostrada; el formato FILE mantiene su lector ordinario.

La aceptación técnica del candidato exacto se acreditará mediante las puertas canónicas y sus recibos. El añadido autorizado de imagen desde el portapapeles falló su primer foco: 75 casos emitidos, 21 aprobados, 53 fallidos y uno interrumpido; los doce filtros restantes no tienen cobertura completada. La [propuesta R11](../architecture/geocedg-1.2.0-clipboard-image-correction-proposal.md) fue autorizada e implementada, pero su foco terminó con 216 casos, 118 aprobados y 98 fallidos, sin errores ni omisiones. La mejora no está verificada y el [cierre técnico quedó bloqueado tras R11](geocedg_1_2_0_r11_blocker_report.md). El autor ha autorizado posteriormente la [corrección R12](../architecture/geocedg-1.2.0-r12-image-recovery-design.md); todavía no hay candidato final validado. El smoke autoral y la aceptación de la referencia editorial mantienen sus decisiones propias. [Expediente científico anterior](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md) · [Flujo operativo del libro](../developer/book_repository_workflow.md).

## Caso de estudio y procedencia

`InterCilConoOblique_2.cedg`, modelo de referencia del autor, mide 70 241 bytes. La copia inspeccionada tiene SHA-256 `4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01`. La inspección estructural se hizo sobre `geogebra.xml` dentro de la copia. Los identificadores siguientes son rótulos del caso de estudio. Las relaciones se leyeron en los argumentos de los comandos guardados.

| Construcción guardada | Uso en el estudio |
|---|---|
| `E_1 = Point(p)` y `K` es una de las salidas de `Intersect(p,i)` | `E_1` recorre la circunferencia `p`. `K` aporta un candidato a testigo en ese mismo conductor. Para usarlo hay que comprobar dirección, rama y validez de la configuración. |
| `o_1 = LocusV2(K_1,E_1)` y `N_1 = Point(o_1,text2,v_1)` | `N_1` fija el origen móvil de la longitud sobre `o_1`. La copia guarda `v_1 = 0` y un selector de rama explícito. |
| `w_1 = LocusLength(o_1,N_1,K_1)`, `d_2 = Length(w_1)` y `e_2 = LocusV2(R_1,E_1)` | La traslación de la generatriz de `e_2` consume `d_2` a través de `Q_1` y `j_2`. `w_1` está guardado como `locusmetricresult`. |
| `P_1 = translationCoor(M_1,O_1,h_2,L_1,f_2)` y `POrigen = Point(e_2,text3,v_1)` | Sus coordenadas guardadas coinciden salvo el redondeo en la última cifra de la ordenada. `POrigen` incorpora una dirección del recorrido por su definición. La construcción de `P_1` usa `translationCoor`. |
| `angSC = LocusLength(e_2,P_1,R_1)` y dos consultas `LocusLength(e_2,POrigen,R_1)` | `m_2` y `n_2` son dos comandos guardados con los mismos argumentos e identidades de salida distintas. Ambos conservan la marca auxiliar histórica grabada en el XML. La creación nueva de `LocusLength` produce un número ordinario y conserva la autoridad rica como auxiliar interno. La instrumentación confirmó reutilización del índice de `e_2`; retirar una consulta ahorró seis evaluaciones por cambio de parámetros y conservó el trabajo anidado de `s_2`. |
| `o_2 = Length(n_2)`, `angSCbis = o_2 / Distance(H,D_1)`, `U_1 = Rotate(T_1,-angSCbis,S_1)` y `s_2 = LocusV2(V_1,E_1)` | `V_1` procede de la intersección de un rayo `S_1 U_1` con una circunferencia centrada en `S_1`. El cociente es un ángulo en radianes cuando el radio es positivo y las entradas son admisibles. |

El archivo guarda `o_2 ≈ 6.1281716666463515` y `angSCbis ≈ 0.9239923366746318`. La sonda de reapertura con Desktop del baseline (`cone-both-gui.log`, salida 0, una prueba de caracterización ejecutada) observó `INVALID_QUERY/NO_ADDRESS` para `angSC` y `SUCCESS/ESTIMATED` con valor `6.1281716666463515` para `m_2` y `n_2`. Con `v_1 = 0.25`, la ordenada de `POrigen` pasó de `6.427635487116075` a `6.5178394636742025`, mientras que `P_1` conservó su posición. `e_2` conservó una componente abierta con evento estructural en el origen. Para `s_2`, esa sonda histórica obtuvo `NOT_ESTABLISHED/OPERATION_OUTSIDE_CLASS` y solo vértices exploratorios. Describe el baseline, no el estado posterior R9. [Contratos de métrica](../../geocedg/specs/locus/locus-v2-metrics.md) · [Análisis anterior del cono](../architecture/pre_g9b_cu2_author_model_certification_analysis.md).

## Relaciones matemáticas para la transferencia

### Dirección y equivalencia constructiva

Sea $F(t;u)$ el punto trazado con dirección $t$ y configuración de entradas libres $u$. Para usar un punto $P(u)$ como extremo semántico se necesita una elección reproducible de $t_*(u)$ en una componente admisible y una demostración de $P(u)=F(t_*(u);u)$ en el dominio declarado de la receta. Una igualdad en el estado guardado es una sola evaluación de esa relación. Un testigo sobre `p` aporta una dirección cuando su existencia, pertenencia y selección se sostienen. La coincidencia del testigo con el origen fijado por `N_1` se comprueba por separado al variar `v_1`. Ante pérdida de una premisa, la receta conserva sus entradas y la identidad de su resultado queda disponible para una recuperación con las mismas premisas. La colocación admisible M08-S-R2 define una posición en una componente y puede partir de una indicación próxima a `P_1`. Su dependencia constructiva se refiere a la dirección seleccionada.

### Receta reproducible de equivalencia por traslación

Este ejemplo concreta el caso 3 del smoke. Procede de la familia ensayada en `GeoCeDG120EndpointAssistanceTest`, con un segmento común y el mismo vector en ambas traslaciones. Para el parámetro normalizado del segmento, $F(t)=(4t,1)$, el testigo tiene $t=1/4$ y el extremo final $t=3/4$. La longitud entre ambos es 2 unidades. Los ensayos R6 y R7 verificaron pérdida de la premisa nativa, recuperación, copia y reapertura. La secuencia modal completa de Desktop queda pendiente de comprobación interactiva. [Evidencia R6](../../artifacts/agent/geocedg-1.2.0/round6-regressions/evidence.json) · [Ensayos de extremos R7](../../artifacts/agent/geocedg-1.2.0/inventory-r7/geocedg-1-2-0.shared/junit/TEST-org.geocedg.common.locus.GeoCeDG120EndpointAssistanceTest.xml).

1. En un documento vacío, introducir cada línea por separado.

   ```text
   available=true
   seg=Segment((0,0),(4,0))
   E=Point(seg)
   witnessParameter=If(available,0.25)
   K=Point(seg,witnessParameter)
   u=(0,1)
   Q=Translate(E,u)
   L=LocusV2(Q,E)
   P=Translate(K,u)
   T=Point(L,"generator.main",0.75)
   rejected=LocusLength(L,P,T)
   ```

2. Abrir el inspector de `rejected` desde su fila de Álgebra. El resultado carece de un extremo admisible. Elegir **Construir un extremo métrico** y **Crear punto equivalente** con el testigo `K`. Confirmar la creación y renombrar el punto nuevo como `Pexact`.
3. Introducir `m=LocusLength(L,Pexact,T)` y `f=m+1`. Se esperan `m=2` y `f=3`, con la garantía que publique el motor. El inspector conserva el resultado rico. El resultado `rejected` sigue dependiendo de `P`.
4. Introducir `available=false`. La premisa de `K` pierde su parámetro válido. `Pexact`, `m` y `f` deben quedar indefinidos, incluso si el punto nativo retiene coordenadas previas.
5. Introducir `available=true`. Deben recuperarse el mismo `Pexact` y los valores 2 y 3. Guardar con otro nombre y reabrir conserva las dependencias y permite repetir ambos estados.

### Longitud, corte y composición

En una rama rectificable con la regularidad exigida para integrar su velocidad, la longitud orientada acumulada desde $t_N$ es $w(t)=\int_{t_N}^{t}\|F'(\tau)\|\,d\tau$, tomando $t$ en el recorrido elegido. En una rama periódica completa de longitud positiva $L$, el cambio de representante de una vuelta a la siguiente produce límites laterales de $w$ separados por $L$. La extensión afín descrita para `e_2` traslada ese salto a una coordenada. La separación tipada de límites depende de la positividad de $L$, establecible mediante una cuerda certificada no nula. El valor numérico de $L$ puede seguir siendo estimado.

Para la rotación que participa en `s_2`, sea $r=|H-D_1|>0$ y sean $\ell_-,\ell_+$ los límites laterales de la medida consumida. Si las otras entradas de la rotación tienen el mismo límite y el vector rotado tiene radio positivo, los dos límites de la rotación coinciden cuando $(\ell_+-\ell_-)/r\in 2\pi\mathbb Z$. La continuidad del resultado final requiere además las premisas de la construcción posterior con el rayo y la circunferencia. La pertenencia a $2\pi\mathbb Z$ requiere evidencia pertinente para la configuración y una estimación aislada deja pendiente la igualdad. Un corte constructivo elegido por el usuario puede separar las dos direcciones laterales incluso con una imagen continua. La existencia de un corte, la discontinuidad geométrica y la segmentación de dibujo requieren sus propias premisas.

En la celda regular del cono, el punto sobre la esfera auxiliar se expresa como $T(t)=V+r(z_x(t),z_y(t),-a)/\sqrt{a^2+|z(t)|^2}$, donde $a$ es la separación vertical al vértice y $z(t)$ la diferencia en planta desde el vértice al punto conductor. Para radio de conductor $R>0$ y $|a|>0$, la derivada de la normalización da la cota de longitud $\Lambda\le 2\pi Rr/|a|$. Cuando las selecciones del modelo mantienen esa proyección, $\Lambda$ es la longitud de la vuelta abierta de `e_2`. Con $0<R<|a|$ y una trayectoria no constante, la diferencia angular de los límites queda estrictamente entre cero y una vuelta. Para aplicar esa conclusión al archivo hay que verificar las premisas del modelo, incluida la selección de raíces y semirrectas. [Diseño de eventos y cortes de F4](../architecture/geocedg-1.2.0-locus-cuts-design.md).

### Prueba composicional implementada en R9

La implementación no identifica un patrón especial por los rótulos del cono.
Captura operaciones tipadas de aritmética escalar, coordenadas, distancia,
rotación y círculos de radio dependiente. Una entrada métrica termina el flujo
de valores del consumidor y conserva sus extremos en el DAG de prueba. Un
índice de recintos de velocidad proporciona cotas de su integral y derivada,
sin utilizar el valor numérico estimado. El refinamiento `route-source-facts/v3`
conserva un presupuesto de 2048 evaluaciones; su objetivo del 25 % solo regula
esfuerzo de estrechamiento y no modifica tolerancias ni garantías métricas.

La regla radial reutilizable demuestra
$F(t)=C+r(t)R(-n(t)/\rho)b$ para centro y punto rotado fijos, dirección no
degenerada, radio positivo, centro compartido por procedencia y raíz positiva
admisible. Aquí $b$ es una dirección unitaria y $n$ la longitud desde el
extremo incluido del arco fuente. Su velocidad satisface
$\|F'\|^2=(r')^2+(r n'/\rho)^2$. El certificado completo de límites y una cota
de velocidad estrictamente positiva son hechos distintos: R9 los comprueba
por separado. La derivación ideal por proyecciones anterior sirve como oráculo
matemático; la prueba ejecutada utiliza las operaciones y recintos del DAG.

La geometría matemática de la rotación es la autoridad aprobada. Su realización
nativa conserva el ajuste numérico del coseno y tiene guardas propias de
finitud, normalización homogénea, discriminante, incidencia y selección sobre
el rayo. Una longitud nativa estimada puede permanecer opaca para esa guarda
cuando la admisibilidad se prueba para cualquier ángulo finito. No se afirma
una precisión angular que no esté demostrada. El número público puede seguir
siendo **Estimada**, aunque la continuidad, regularidad y separación de límites
de su consumidor tengan prueba.

El arco abierto de una vuelta queda acotado a origen en el extremo incluido,
sentido adelante, política estricta y longitud cero. La consulta lateral
temporal conserva el límite final sin habilitar `full-cycle` público sobre un
arco abierto. Fuera de esa clase, la operación puede conservar un estado sin
establecer. La equivalencia de F2 tampoco es general: su receta de traslación
no admite automáticamente `P_1` del cono ni convierte coincidencias en
dependencias.

### Evidencia y límites

| Clase | Material transferible |
|---|---|
| Derivación | Condiciones de la equivalencia, salto de una vuelta con longitud positiva y condición de coincidencia de las rotaciones laterales. |
| Caracterización previa | `e_2` tiene en el caso de referencia anterior una componente abierta certificada por estructura y coordenadas estimadas. Cambios de rama, radio o dominio requieren comprobar las hipótesis de nuevo. |
| Observación histórica del baseline | Cadena y valores guardados en el modelo de referencia. La sonda inicial confirmó la separación de los orígenes y el carácter exploratorio de `s_2` en ese baseline. No describe el certificado R9. |
| Comparación de producto | 1.1.0 guarda `LocusLength` como resultado rico y permite consumir un resultado numérico a través de `Length(...)`. Los cuatro ensayos focales de contrato R7 aprobaron la salida numérica nueva, consultas con identidad distinta, compatibilidad histórica, reapertura y conservación del estado auxiliar elegido. Los fallos de inserción registrados entonces se corrigieron en R8. La aceptación final de persistencia depende de las puertas del próximo candidato exacto con R9. |
| Instrumentación inicial | En la perturbación inicial de `H` documentada por la sonda (coordenadas solicitadas multiplicadas por 1,02 y proyección nativa al camino), se reconstruyó una vez el índice de `e_2`. La evaluación dependiente `s_2` hizo cuatro construcciones métricas anidadas y 18 041 evaluaciones anidadas, tanto con dos consultas públicas como con una. Las consultas reutilizaron el índice en ese estado. |
| Observación histórica de desarrollo | `cone-cuts-r2.log`, salida 0, anterior a R9. Con `v_1 = 0`, el testigo `K` añadió procedencia a ambos límites del reinicio existente en la variante cortada de `e_2`. Con `v_1 = ±0.25`, el corte permaneció en la dirección cero y los dos arcos certificados se separaron. En aquella implementación `s_2` conservó el evento compuesto sin resolver y trazos exploratorios. |
| Contraste de composiciones | `cone-cancellation.log`, salida 0. En una circunferencia trasladada, la longitud de una vuelta dividida por su propio radio produce una vuelta angular. El caso de cierre y su control con un segundo radio de igual valor están cubiertos por `GeoCeDG120ConstructiveCutsTest`, cuyos ocho casos aprobaron en R7. El cono sigue sujeto a las premisas adicionales de su construcción. |
| Certificación focal R9 | Shared05: 94 casos aprobados. Desktop05: `s_2` completo, con extremos estructurales distintos y velocidad inferior positiva. El índice de `e_2` conserva lower cero; no demuestra por sí mismo regularidad de `e_2`. |
| Dinámica y persistencia focal R9 | Desktop06: desplazamiento nativo de `H` en `t_H+0.25` y retorno; `v_1=0.5`, `-pi+0.1`, `pi-0.1` y retorno; origen indefinido y recuperación sin certificado obsoleto; corte declarado `K` con dos componentes al separarse del reinicio; guardar otra copia y reabrir conservando IDs, linaje y seis coordenadas de extremos/interior. El mínimo lower de velocidad observado en estos estados fue `0.5522824670667028`. |
| Representación y colocación focal R9 | En esos estados, bajo la política adaptativa fija del ensayo, el render tuvo fidelidad COMPLETE, 27 vértices certificados, un subtrazo y ningún vértice exploratorio; sus extremos corresponden a las dos imágenes laterales, sin segmento de cierre. La colocación S real en el cono pasó commit, tres ciclos Undo/Redo y reapertura, y el fallo de publicación inyectado restituyó las entradas nativas y selección. No sustituye Computer Use del producto final. |
| Alcance científico limitado | No hay certificación universal de cualquier composición, origen, política o degeneración. Un divisor independiente de igual valor no adquiere por ello la prueba de cancelación del radio propio. Las premisas y presupuestos siguen siendo condiciones de cada revisión. |
| Campaña pendiente | Medición de respuesta y Computer Use sobre el próximo árbol exacto, puertas globales e instalación y uso de sus paquetes INTERNAL y NC. Ninguna evidencia del candidato retirado se promueve a evidencia final de R9. |

El expediente operativo conserva las instrucciones `javac @cone-javac.args` y `java @cone-both-gui.args` / `java @cone-single-gui.args`, los códigos de salida y el escenario de modificación de `H`, `v_1` y `E_1`. La comparación A/B posterior ejecutó referencia, variante, variante y referencia con perfiles aislados. Los valores, diagnósticos y contadores coincidieron. La caché estudiada mantuvo las 18 041 evaluaciones anidadas tras mover `H` y sus tiempos no establecieron una mejora. Un control con dependencia aleatoria mostró una guarda insuficiente y la variante se descartó. Los tiempos corresponden al entorno medido; la respuesta del candidato final queda pendiente. [Caracterización inicial](../../artifacts/agent/geocedg-1.2.0/cone-baseline-characterization.md) · [Comparación A/B y límites](../../artifacts/agent/geocedg-1.2.0/f5-comparison.md) · [Sonda de cortes](../../artifacts/agent/geocedg-1.2.0/cone-cuts-r2.log) · [Sonda de cancelación](../../artifacts/agent/geocedg-1.2.0/cone-cancellation.log).

## Propuesta de correspondencias editoriales

| Unidad vigente | Correspondencia propuesta | Disposición de evidencia |
|---|---|---|
| `CH05` | Punto libre sobre un conductor, punto trazado y selección de una posición | Fundamento CeDG. Los pasos de interfaz dependen de la referencia de producto de la edición. |
| `CH18`, `CH21`, `CH22` | Desarrollo del cono, arco medido sobre el auxiliar y magnitud angular aplicada a un radio | Derivaciones y procedimiento con precondiciones. Verificar la extensión de cada ejemplo a la construcción elegida. |
| `CH20` | Intersección de las superficies y papel de las generatrices en el caso cono–cilindro | Caso procedente del modelo del autor. Cualquier reproducción editorial de archivo o figura requiere su trazabilidad y derechos. |
| `CH29` | Dirección, componente, longitud, garantías, extremos de costura y cortes constructivos | Fundamento, caracterización histórica y prueba composicional focal R9 disponibles, distinguiendo número estimado y geometría certificada. Una afirmación de producto final requiere todavía el candidato validado y una referencia editorial aceptada. |
| `CH31` | Evidencia de regresión, reproducibilidad y coste de recomputación del DAG | Instrumentación y comparación A/B disponibles, con valores y diagnósticos conservados y sin mejora demostrada. Medición del candidato final pendiente. |

Para una futura figura, exportar la misma construcción en tres estados reproducibles: origen inicial, origen desplazado y retorno al estado inicial. Mostrar la elección del recorrido y los dos extremos laterales. La figura deberá salir de GeoCeDG, registrar el SHA-256 de la copia utilizada y remitir al contrato y a los parámetros del caso. [Registro de figuras de las guías](../user/images/figures.json) · [Referencias y ejemplos del expediente anterior](pre_g9b_cu10_scientific_examples_and_references.md).
