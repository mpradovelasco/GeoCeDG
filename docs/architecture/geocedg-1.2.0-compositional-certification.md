# GeoCeDG 1.2.0 — Certificación por composición y reparación R9

## Autoridad y condición de cierre

Este diseño registra la novena y última ronda correctora autorizada. El autor exige que `s_2` quede definido y certificado y que el soporte se generalice a otras construcciones sin crear una clase especial por modelo. La propuesta autoral «jazziana» consiste en buscar de forma determinista una construcción justificable durante la autoría y conservar una receta que el kernel revalida en el DAG, sin repetir la búsqueda en cada actualización. Se mantienen D1–D4, el contrato métrico §26, la autoridad constructiva y la separación entre evidencia estructural, estimación numérica y representación.

El candidato `6f1d357ca2224dedd0e84fda0faed2d600154bab`, árbol `b6708de39577009752d45e0b73e9d44d94d695f9`, queda retirado de la aceptación final. Sus resultados PHASE, PACKAGING, Sandbox, benchmark y Computer Use se conservan como evidencia histórica. INTEGRATION se interrumpió expresamente al cambiar la condición de cierre y FINAL no se inició. No constituyen evidencia final del candidato corregido.

## Causas y alcance

La certificación de `s_2` falla por capacidades de composición ausentes: aritmética escalar dependiente, rotación con ángulo dependiente, radio dependiente, hechos rigurosos de una métrica sobre un arco abierto de una vuelta y propagación de esos hechos a los consumidores. El renderer dibuja deliberadamente la evidencia exploratoria con trazo punteado. La corrección debe obtener evidencia del recorrido y sus lados; cambiar el estilo no establece continuidad.

El rechazo de colocación `UNSUPPORTED_HOST_STATE` tiene otra causa: los cierres de los puntos nativos sobre caminos del documento contienen operaciones que la tabla positiva de restauración aún no admite. Su corrección separa explícitamente MEMORY de FILE y conserva la guarda de restauración exacta. El [diseño de restauración nativa](geocedg-1.2.0-native-memory-repair.md) define su perímetro y pruebas.

## Interfaces e invariantes comunes

1. El programa de prueba se compone de operaciones tipadas reutilizables. Se incorporan aritmética escalar, seno, coseno, raíz cuadrada, coordenadas y distancia de puntos, rotación con ángulo escalar y círculo con radio escalar. Las formas constantes previamente admitidas conservan sus operaciones y firmas. Las nuevas formas son datos derivados de la captura actual; no reemplazan la construcción ni su XML.
2. Cada paso conserva un origen constructivo. Las subexpresiones tienen rutas estructurales distintas dentro de su productor. Una etiqueta, coincidencia numérica o índice de muestreo no identifica una premisa.
3. La entrada métrica es terminal en el flujo de valores del programa consumidor. Sus extremos siguen siendo dependencias de prueba y del DAG real. La geometría que establece el extremo de una métrica se demuestra en la fuente; no introduce una segunda variable métrica independiente ficticia en el consumidor.
4. Un recinto métrico de caja aporta cotas outward del valor, de su primera derivada y la procedencia de recorrido, revisión y lado. Se deriva de la estructura certificada y de recintos de la velocidad de la fuente. Nunca utiliza el valor estimado de una integral para certificar existencia, continuidad, regularidad o separación de límites. Los presupuestos se conservan y su agotamiento queda tipado.
5. Se admite el arco abierto de una vuelta únicamente con origen en su extremo incluido, orientación correspondiente, recorrido estricto y política de longitud cero. La coordenada levantada del arco distingue el inicio y su límite final aunque compartan dirección canónica. Los demás casos requieren su propia prueba y conservan un estado explícito si no se obtiene.
6. Las reglas de normalización son identidades con premisas comprobadas sobre operandos actuales. Una intersección admisible de `Ray(C,Q)` y `Circle(C,r)` puede analizarse como `C + r normalize(Q-C)` si se prueban centro compartido, radio positivo, dirección no nula y selección nativa de la raíz correspondiente. Esta regla no usa nombres del cono ni igualdad visual. Las guardas de selección, incidencia y degeneración siguen siendo necesarias.
7. La composición produce un veredicto lateral distinto, igual o no establecido. La continuidad local se demuestra dentro de cada componente. Un evento escalar se conserva como tal hasta probar su efecto geométrico. La periodicidad del conductor no implica clausura de la imagen. El resultado numérico público conserva su propia garantía.
8. Las recetas y pruebas de normalización son versionadas (`composition-proof/v1`), reconstruibles desde la construcción y válidas para una revisión. La búsqueda de una receta pertenece a la autoría; la comprobación de sus premisas pertenece a la evaluación semántica. Las reglas por operación sirven para todas sus composiciones admisibles, no para una lista de documentos reconocidos.

## Referencia matemática y validación

Para la cadena regular del cono, sea `n(t)` la longitud sobre `e_2` desde el extremo incluido, `rho = Distance(H,D_1)` y `r(t) = Distance(H,I_1)`. La rotación y la intersección radial admitida dan `F(t)=S_1+r(t) R(-n(t)/rho) b`, donde `b` es la dirección unitaria de `T_1-S_1`. Con `rho>0`, `r(t)>0` y fuente rectificable certificada, esta composición es continua en cada arco regular. La longitud cumple `n'=speed(e_2)` y tiene límites laterales `0` y `Lambda` en el reinicio.

La separación en el reinicio se puede demostrar mediante recintos rigurosos que establezcan `0<Lambda/rho<2*pi` y las entradas laterales comunes; entonces los dos valores de la rotación son distintos. Si las premisas no se mantienen, el motor debe publicar la degeneración o la falta de prueba correspondiente. La derivación geométrica ideal del cono sirve como oráculo independiente, no sustituye las premisas comprobadas de las operaciones capturadas.

Las pruebas deben incluir composiciones sintéticas ajenas al cono, fórmulas escalares anidadas, constantes y dependencias dinámicas, división y raíz degeneradas, selección de raíces, origen abierto incluido e interior, orientación, límites de ambos lados, cancelación periódica, salto probado y caso indecidido. El cono se valida con cambios en `H` y `v_1`, costura canónica, cortes declarados, recuperación, copia, Undo/Redo y reapertura. Se exige cobertura certificada regular y rendering derivado sin unir extremos distintos. Ningún test puede promover una muestra o estimación a certificado.

La restauración MEMORY se prueba con commit real de colocación en el cono y fallo transaccional adversarial, incluyendo parámetros nativos y estado de selección de intersección. FILE conserva su tabla y sus rechazos. Tras la revisión independiente se actualizan los inventarios derivados y se ejecutan las verificaciones requeridas, benchmark y distribución sobre el nuevo commit y árbol exactos. El estado final y el smoke autoral permanecen pendientes hasta completar esas puertas.

## R9 — Refinamiento de los recintos de fuente

El diagnóstico Desktop04 obtuvo existencia local certificada de `s_2`, pero
no separó sus extremos de `ORIGIN_WRAP`. El índice de la fuente ya tenía una
cobertura finita válida, pero sus cotas eran demasiado amplias: el upper de la
longitud de `e_2`, dividido por el denominador angular, superaba una vuelta.
La cobertura inicial de 64 cajas aceptaba cada caja al obtener un recinto
finito; no intentaba estrechar una cota ya válida. No se modifica el valor
numérico público ni se utiliza su estimación para corregir ese problema.

`route-source-facts/v3` conserva el mismo índice compartido por revisión y
componente, las 64 celdas iniciales, profundidad máxima 24 y presupuesto total
2048 evaluaciones. Primero se obtiene la cobertura finita obligatoria y la
cota superior del hueco del círculo real, cuyo lower continúa siendo cero.
Si esa cobertura falla, se mantiene el rechazo tipado existente, sin ofrecer
integrales parciales como si fuesen una cobertura completa.

Después se refina de forma determinista la celda con mayor ancho outward de
su contribución integral, con desempate por parámetro canónico. Un intento
reserva dos evaluaciones, una por hijo. Los recintos de velocidad de ambos
hijos se intersectan con el recinto válido del padre; solo se sustituye el
padre si ambos hijos tienen prueba y la contribución conjunta mejora sin
ensanchar las cotas existentes. Si no se obtiene esa mejora, se conserva el
padre y no se repite el mismo intento. Los límites de profundidad y de
representabilidad se respetan.

El objetivo de esfuerzo es que `upper-lower <= 0.25*upper`, comprobado con
aritmética outward. **No es una tolerancia numérica de la longitud, una nueva
clase de garantía ni un criterio de aceptación geométrica.** Los consumidores
siguen comprobando sus desigualdades o separación con las cotas obtenidas.
Una cota que ya demuestra un hecho no necesita alcanzar ese objetivo, y una
que lo alcance no demuestra por sí sola un salto o regularidad.

`Index.getPrecisionStatus()` separa `TARGET_REACHED`, `EFFORT_BUDGET`,
`REFINEMENT_LIMIT`, `NO_IMPROVEMENT` y `NOT_AVAILABLE`. El agotamiento del
refinamiento opcional no invalida la cobertura finita que ya se demostró:
`isCertified` sigue verdadero, `getFailureCause` sigue nulo y se conservan las
cotas válidas. `NOT_AVAILABLE` identifica la ausencia de cobertura finita.
No se introduce estado de reintento por consumidor ni se repite una campaña
para obtener un resultado favorable.

La API pública conserva su presupuesto fijo. Una sobrecarga package-private
permite únicamente reducirlo para oráculos; rechaza valores fuera de
`[1,2048]`. La prueba principal ajena al cono usa
`F(t)=(1/(t+1/64),0)`, `t in [0,1]`, cuya longitud exacta es `4096/65`.
Compara una cobertura válida con presupuesto 64, aún amplia, con el índice
refinado: ambos deben encerrar la misma longitud, el segundo debe alcanzar
el objetivo con mejora efectiva y dentro del presupuesto. Un presupuesto 63
debe conservar el rechazo por cobertura incompleta. Se mantienen además los
oráculos de círculo real, subintervalos, componentes y degeneración.

## Estado de evidencia focal y documentación

La selección [Shared05](../../artifacts/agent/geocedg-1.2.0/r9-focal-shared-05/evidence.json)
terminó con salida 0: 94 casos, cero fallos, errores u omisiones. Incluye
oráculos de captura, cajas, índices, ramas nativas, compatibilidad, estado
nativo y cancelación. El caso de derivadas exactamente nulas conserva una
velocidad y longitud exactamente cero; no introduce un upper subnormal
positivo ni cambia el contrato histórico de una fuente colapsada.

[Desktop05](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-05/junit/TEST-org.geocedg.desktop.GeoCeDG120ConeCertificationTest.xml)
obtuvo `s_2` completo en el modelo base, con límites `ORIGIN_WRAP` distintos y
velocidad rigurosamente positiva. [Desktop06, cono](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-06/junit/TEST-org.geocedg.desktop.GeoCeDG120ConeCertificationTest.xml)
y [Desktop06, colocación](../../artifacts/agent/geocedg-1.2.0/r9-focal-desktop-06/junit/TEST-org.geocedg.desktop.GeoCeDG120ConePlacementLifecycleTest.xml)
aprobaron cuatro casos focales. La dinámica incluye el parámetro nativo de `H`
desplazado en 0.25 y restaurado, origen `v_1=0.5`, proximidad a ambos lados
de la costura canónica, origen indefinido y recuperación, corte declarado `K`
y reapertura con IDs y linaje conservados. En los estados regulares ensayados,
el lower de velocidad de `s_2` permaneció positivo (mínimo registrado
`0.5522824670667028`); el render bajo la política adaptativa fija del ensayo fue completo con un subtrazo,
27 vértices certificados y cero exploratorios. La colocación real y el rollback
de un fallo de publicación inyectado también pasaron.

`GUIDE_IMPACT`: ambas guías incorporan la sección 6.8 sobre composiciones y
aclaran en 9.1 que un número **Estimada/Estimated** no se convierte en
certificado por la geometría del locus. La matriz de cobertura enlaza esos
apartados. El expediente editorial conserva las observaciones del baseline
como históricas y transfiere los nuevos fundamentos y evidencia focal sin
modificar el manuscrito o el baseline aceptado del libro. No se añaden imágenes
sin Computer Use de R9.

La evidencia focal no sustituye la revisión independiente, las selecciones
amplias, PHASE, INTEGRATION, FINAL, Computer Use, benchmark ni empaquetado del
próximo commit y árbol exactos. El estado de candidato, aceptación y
publicación continúa pendiente.
