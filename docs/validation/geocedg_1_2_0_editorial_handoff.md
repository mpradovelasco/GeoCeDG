# GeoCeDG 1.2.0 — Expediente científico-editorial

Expediente de transferencia preparado en GeoCeDG para `geocedg_book`. La incorporación editorial y la aceptación de una referencia de producto son decisiones del libro. Su ledger vigente registra como referencia aceptada `372da89f67c5a3c07100512b6b414a0028319ec5` (`geocedg-pre-g9b-r7-pass`). El baseline técnico del encargo 1.2.0 es `4fc0343a1f0dc59a645ee4fe8ec6c5c40fe2051e`, árbol `7cbabc4bd967d775aa730264614d2a1f5092c996`.

La referencia local publicada de `geocedg_book` es `78fbc88e9e12346b89d6ded411eaae94de2284b9`. La consulta de solo lectura confirmó que las copias inspeccionadas de `AGENTS.md`, `editorial/roadmap/BOOK_ROADMAP.md`, `editorial/style/EDITORIAL_CONTRACT.md`, `editorial/source-mapping/TECHNICAL_BASELINES.json` y `editorial/master-structure.md` corresponden a los blobs de esa referencia.

Estado del expediente: caracterización de partida y material para revisión. Una observación del documento guardado identifica un caso reproducible. Una capacidad de 1.2.0 requiere evidencia de ejecución sobre su candidato. [Expediente científico anterior](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md) · [Flujo operativo del libro](../developer/book_repository_workflow.md).

## Caso de estudio y procedencia

`InterCilConoOblique_2.cedg`, modelo de referencia del autor, mide 70 241 bytes. La copia inspeccionada tiene SHA-256 `4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01`. La inspección estructural se hizo sobre `geogebra.xml` dentro de la copia. Los identificadores siguientes son rótulos del caso de estudio. Las relaciones se leyeron en los argumentos de los comandos guardados.

| Construcción guardada | Uso en el estudio |
|---|---|
| `E_1 = Point(p)` y `K` es una de las salidas de `Intersect(p,i)` | `E_1` recorre la circunferencia `p`. `K` aporta un candidato a testigo en ese mismo conductor. Para usarlo hay que comprobar dirección, rama y validez de la configuración. |
| `o_1 = LocusV2(K_1,E_1)` y `N_1 = Point(o_1,text2,v_1)` | `N_1` fija el origen móvil de la longitud sobre `o_1`. La copia guarda `v_1 = 0` y un selector de rama explícito. |
| `w_1 = LocusLength(o_1,N_1,K_1)`, `d_2 = Length(w_1)` y `e_2 = LocusV2(R_1,E_1)` | La traslación de la generatriz de `e_2` consume `d_2` a través de `Q_1` y `j_2`. `w_1` está guardado como `locusmetricresult`. |
| `P_1 = translationCoor(M_1,O_1,h_2,L_1,f_2)` y `POrigen = Point(e_2,text3,v_1)` | Sus coordenadas guardadas coinciden salvo el redondeo en la última cifra de la ordenada. `POrigen` incorpora una dirección del recorrido por su definición. La construcción de `P_1` usa `translationCoor`. |
| `angSC = LocusLength(e_2,P_1,R_1)` y dos consultas `LocusLength(e_2,POrigen,R_1)` | `m_2` y `n_2` son dos comandos guardados con los mismos argumentos e identidades de salida distintas. Ambos elementos tienen `<auxiliary val="true"/>`. `angSC` carece de esa marca. El XML registra estado y dependencias. El origen de la marca y la duplicación de trabajo requieren inspección de código y medición. |
| `o_2 = Length(n_2)`, `angSCbis = o_2 / Distance(H,D_1)`, `U_1 = Rotate(T_1,-angSCbis,S_1)` y `s_2 = LocusV2(V_1,E_1)` | `V_1` procede de la intersección de un rayo `S_1 U_1` con una circunferencia centrada en `S_1`. El cociente es un ángulo en radianes cuando el radio es positivo y las entradas son admisibles. |

El archivo guarda `o_2 ≈ 6.1281716666463515` y `angSCbis ≈ 0.9239923366746318`. La sonda de reapertura con Desktop del baseline (`cone-both-gui.log`, salida 0, una prueba de caracterización ejecutada) observó `INVALID_QUERY/NO_ADDRESS` para `angSC` y `SUCCESS/ESTIMATED` con valor `6.1281716666463515` para `m_2` y `n_2`. Con `v_1 = 0.25`, la ordenada de `POrigen` pasó de `6.427635487116075` a `6.5178394636742025`, mientras que `P_1` conservó su posición. `e_2` conservó una componente abierta con evento estructural en el origen. Para `s_2`, la sonda obtuvo `NOT_ESTABLISHED/OPERATION_OUTSIDE_CLASS` y solo vértices exploratorios. Esta observación cuantifica el estado de certificación del caso. [Contratos de métrica](../../geocedg/specs/locus/locus-v2-metrics.md) · [Análisis anterior del cono](../architecture/pre_g9b_cu2_author_model_certification_analysis.md).

## Relaciones matemáticas para la transferencia

### Dirección y equivalencia constructiva

Sea $F(t;u)$ el punto trazado con dirección $t$ y configuración de entradas libres $u$. Para usar un punto $P(u)$ como extremo semántico se necesita una elección reproducible de $t_*(u)$ en una componente admisible y una demostración de $P(u)=F(t_*(u);u)$ en el dominio declarado de la receta. Una igualdad en el estado guardado es una sola evaluación de esa relación. Un testigo sobre `p` aporta una dirección cuando su existencia, pertenencia y selección se sostienen. La coincidencia del testigo con el origen fijado por `N_1` se comprueba por separado al variar `v_1`. Ante pérdida de una premisa, la receta conserva sus entradas y la identidad de su resultado queda disponible para una recuperación con las mismas premisas. La colocación admisible M08-S-R2 define una posición en una componente y puede partir de una indicación próxima a `P_1`. Su dependencia constructiva se refiere a la dirección seleccionada.

### Longitud, corte y composición

En una rama rectificable con la regularidad exigida para integrar su velocidad, la longitud orientada acumulada desde $t_N$ es $w(t)=\int_{t_N}^{t}\|F'(\tau)\|\,d\tau$, tomando $t$ en el recorrido elegido. En una rama periódica completa de longitud positiva $L$, el cambio de representante de una vuelta a la siguiente produce límites laterales de $w$ separados por $L$. La extensión afín descrita para `e_2` traslada ese salto a una coordenada. La separación tipada de límites depende de la positividad de $L$, establecible mediante una cuerda certificada no nula. El valor numérico de $L$ puede seguir siendo estimado.

Para la rotación que participa en `s_2`, sea $r=|H-D_1|>0$ y sean $\ell_-,\ell_+$ los límites laterales de la medida consumida. Si las otras entradas de la rotación tienen el mismo límite y el vector rotado tiene radio positivo, los dos límites de la rotación coinciden cuando $(\ell_+-\ell_-)/r\in 2\pi\mathbb Z$. La continuidad del resultado final requiere además las premisas de la construcción posterior con el rayo y la circunferencia. La pertenencia a $2\pi\mathbb Z$ requiere evidencia pertinente para la configuración y una estimación aislada deja pendiente la igualdad. Un corte constructivo elegido por el usuario puede separar las dos direcciones laterales incluso con una imagen continua. La existencia de un corte, la discontinuidad geométrica y la segmentación de dibujo requieren sus propias premisas.

En la celda regular del cono, el punto sobre la esfera auxiliar se expresa como $T(t)=V+r(z_x(t),z_y(t),-a)/\sqrt{a^2+|z(t)|^2}$, donde $a$ es la separación vertical al vértice y $z(t)$ la diferencia en planta desde el vértice al punto conductor. Para radio de conductor $R>0$ y $|a|>0$, la derivada de la normalización da la cota de longitud $\Lambda\le 2\pi Rr/|a|$. Cuando las selecciones del modelo mantienen esa proyección, $\Lambda$ es la longitud de la vuelta abierta de `e_2`. Con $0<R<|a|$ y una trayectoria no constante, la diferencia angular de los límites queda estrictamente entre cero y una vuelta. Para aplicar esa conclusión al archivo hay que verificar las premisas del modelo, incluida la selección de raíces y semirrectas. [Diseño de eventos y cortes de F4](../architecture/geocedg-1.2.0-locus-cuts-design.md).

### Evidencia y límites

| Clase | Material transferible |
|---|---|
| Derivación | Condiciones de la equivalencia, salto de una vuelta con longitud positiva y condición de coincidencia de las rotaciones laterales. |
| Caracterización previa | `e_2` tiene en el caso de referencia anterior una componente abierta certificada por estructura y coordenadas estimadas. Cambios de rama, radio o dominio requieren comprobar las hipótesis de nuevo. |
| Observación del baseline | Cadena y valores guardados en el modelo de referencia. La sonda Desktop confirma la separación de los orígenes y el carácter exploratorio de `s_2`. La frecuencia de intermitencia al mover libremente `H` sigue pendiente de comprobación visual. |
| Comparación de producto | 1.1.0 guarda `LocusLength` como resultado rico y permite consumir un resultado numérico a través de `Length(...)`. La aceptación de la superficie 1.2.0 requiere pruebas de reapertura, acceso al resultado rico, precisión declarada y conservación de las dependencias. |
| Instrumentación inicial | Al desplazar `H` un 2 % de la longitud del camino en el ensayo, se reconstruyó una vez el índice de `e_2`. La evaluación dependiente `s_2` hizo cuatro construcciones métricas anidadas y 18 041 evaluaciones anidadas, tanto con dos consultas públicas como con una. Las consultas reutilizaron el índice en ese estado. |
| Observación de desarrollo 1.2.0 | `cone-cuts-r2.log`, salida 0. Con `v_1 = 0`, el testigo `K` añade procedencia a ambos límites del reinicio existente en la variante cortada de `e_2`. Con `v_1 = ±0.25`, el corte permanece en la dirección cero y los dos arcos certificados se separan. En `s_2`, el evento compuesto queda sin resolver y los 32 vértices exploratorios se organizan en dos subtrazos. |
| Contraste de composiciones | `cone-cancellation.log`, salida 0. En una circunferencia trasladada, la longitud de una vuelta dividida por su propio radio produce una vuelta angular. El prototipo certifica el cierre de la rotación en esa clase. Con un segundo objeto radio de igual valor, la evidencia estructural queda sin establecer. El cono sigue sujeto a las premisas adicionales de su construcción. |
| Campaña pendiente | Certificación de los límites geométricos de `s_2`, costes reproducibles al mover `H` en el candidato, revisión final de persistencia e instalación y ejecución de ambos perfiles. |

El expediente operativo `artifacts/agent/geocedg-1.2.0/cone-baseline-characterization.md` conserva las instrucciones `javac @cone-javac.args` y `java @cone-both-gui.args` / `java @cone-single-gui.args`, los códigos de salida, el escenario de modificación de `H`, `v_1` y `E_1` y los registros de cada ejecución. Los tiempos son muestras del entorno indicado. Las comparaciones de coste necesitan repetir el escenario sobre el candidato conservando garantías y diagnósticos.

## Propuesta de correspondencias editoriales

| Unidad vigente | Correspondencia propuesta | Disposición de evidencia |
|---|---|---|
| `CH05` | Punto libre sobre un conductor, punto trazado y selección de una posición | Fundamento CeDG. Los pasos de interfaz dependen de la referencia de producto de la edición. |
| `CH18`, `CH21`, `CH22` | Desarrollo del cono, arco medido sobre el auxiliar y magnitud angular aplicada a un radio | Derivaciones y procedimiento con precondiciones. Verificar la extensión de cada ejemplo a la construcción elegida. |
| `CH20` | Intersección de las superficies y papel de las generatrices en el caso cono–cilindro | Caso procedente del modelo del autor. Cualquier reproducción editorial de archivo o figura requiere su trazabilidad y derechos. |
| `CH29` | Dirección, componente, longitud, garantías, extremos de costura y cortes constructivos | Fundamento y caracterización histórica disponibles. Las nuevas prestaciones requieren el candidato validado y una referencia editorial aceptada. |
| `CH31` | Evidencia de regresión, reproducibilidad y coste de recomputación del DAG | Comparaciones de rendimiento pendientes de medidas antes y después en el mismo escenario, con etiquetas de entorno e identidad. |

Para una futura figura, exportar la misma construcción en tres estados reproducibles: origen inicial, origen desplazado y retorno al estado inicial. Mostrar la elección del recorrido y los dos extremos laterales. La figura deberá salir de GeoCeDG, registrar el SHA-256 de la copia utilizada y remitir al contrato y a los parámetros del caso. [Registro de figuras de las guías](../user/images/figures.json) · [Referencias y ejemplos del expediente anterior](pre_g9b_cu10_scientific_examples_and_references.md).
