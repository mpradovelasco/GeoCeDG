# GeoCeDG 1.2.0 — Propuesta de autoridad para la rotación de ángulo variable

## Estado y decisión autoral

**AUTORIDAD APROBADA POR EL AUTOR EL 2026-11-16.** El autor aprobó en la
conversación de este encargo la alternativa recomendada
`composition-rotation-geometry/v1` para nuevas certificaciones de documentos
nuevos y antiguos, con guardas nativas separadas. La implementación, revisión y
validación siguen pendientes; esta decisión no acepta un candidato ni autoriza
su publicación. La fecha identifica esta decisión científica y no introduce
metadatos de versión del producto.

Se autoriza, únicamente para la nueva operación derivada
`ROTATE_BY_ANGLE`, la rotación geométrica exacta `R(theta)` como autoridad de su
certificado, con guardas separadas y rigurosas de admisibilidad de la ejecución
nativa. El ajuste a cero de `MyMath.cos` se trataría como un error numérico
acotado de esa realización. No se cambiarían los comandos, objetos, valores
numéricos nativos, antiguos operadores constantes ni XML.

La alternativa es conservar la interpretación literal del ajuste a cero. En
ese caso sus entradas y salidas son eventos microscópicos de la función
certificada; no puede afirmarse continuidad regular a través de ellos.

## Causa y relación con la autoridad vigente

El [contrato de construcción certificada](../../geocedg/specs/locus/locus-v2-certified-construction-model.md)
§2 define la evaluación exact-real de la secuencia de fórmulas, incluyendo sus
predicados de rama. Las funciones trascendentes del conductor son exactas y el
evaluador flotante las aproxima. El conductor circular emplea `Math.cos` y
`Math.sin` sin ajuste a cero; ese precedente permite aproximación numérica, pero
no elimina por sí mismo una rama explícita de otra operación.

`GeoPoint.rotate(phi, center)` emplea `MyMath.cos(phi)`: devuelve cero si el
coseno calculado tiene magnitud menor que `1e-16`. Su seno usa `Math.sin` sin esa
rama. La primera implementación R9 reproduce el predicado: una caja que cruza
el umbral queda sin prueba de continuidad. En la construcción original, el
desplazamiento horizontal entre los extremos de `e_2` es aproximadamente
`14.8966`: corresponde a la longitud de su fuente `o_1`, no a la longitud total
de `e_2`. Su cociente por el denominador angular es aproximadamente `2.246` y
constituye solo una cota geométrica de diagnóstico del giro total. La longitud
de `e_2` requiere su propio recinto integral. El recorrido cruza `pi/2`; estas
cifras **no son premisas de un certificado** ni sustituyen los recintos del
programa real.

Interpretar el operador nuevo como rotación geométrica exacta requirió por ello
la elección explícita de autoridad registrada arriba. No basta modificar su derivada, declarar
el operador total o ampliar un recinto de valores y llamarlo continuo.

## Alternativa recomendada: operador geométrico con realización controlada

Para puntos finitos `P` y `C`, el nuevo operador representa

`F = C + R(theta) (P - C)`.

El certificado usa seno y coseno exactos, sus derivadas y sus identidades
geométricas. Sus entradas siguen siendo las del DAG actual, con procedencia
estable, y las cotas de una entrada métrica proceden de la estructura y de la
velocidad certificadas de su fuente. Una estimación pública de longitud no
demuestra ninguna premisa.

Se deben mantener dos conceptos explícitos:

1. Un recinto semántico de valores y derivadas de la rotación geométrica. Su
   regularidad y continuidad pertenecen a ese mapa matemático.
2. Un recinto de la realización nativa suficiente para decidir sus predicados
   de finitud, denominadores, incidencia, degeneración y selección. Este recinto
   contiene el ajuste a cero y los errores flotantes pertinentes. No aporta
   derivadas ni constituye una prueba de continuidad de los números calculados.

Una implementación acotada puede reutilizar un recorrido de intervalos de
valores sin forma centrada para la segunda función. En él, una banda que puede
seleccionar el ajuste a cero contiene tanto el coseno calculable como cero;
cada operación propaga esos recintos outward. Las guardas de la evaluación
semántica exigen también que los predicados nativos pertinentes estén
establecidos. No se permite reutilizar una intersección con una forma de valor
medio para estrechar ese error: la derivada de la geometría ideal no acota los
saltos del ajuste nativo.

Esta descripción es una interfaz de prueba, no una autorización para suponer
que un segundo recorrido ya cubre todos los productores. Cada operación y
cada función trascendente que utilice debe tener un recinto demostrado de su
realización. Una entrada sin esa justificación permanece sin prueba.

### Cota local del ajuste y fórmula homogénea

Con el mismo argumento real, sea `tau = 1e-16`. Si se omite únicamente el
redondeo de la función trigonométrica, el ajuste del coseno produce

`delta F = delta c (P - C)`, con `|delta c| < tau`.

Por tanto `norm(delta F) <= tau * norm(P - C)`. **No es una tolerancia absoluta
universal de coordenadas**. Un centro distante o un radio grande amplifican el
error, y las operaciones posteriores pueden amplificarlo de nuevo.

Si `epsilon_c` y `epsilon_s` acotan los errores demostrados de `Math.cos` y
`Math.sin`, respectivamente, una cota para la parte trigonométrica es

`norm(delta F) <= norm(P - C) * sqrt((tau + epsilon_c)^2 + epsilon_s^2)`.

Si el argumento nativo difiere del argumento semántico, se añade la cota de esa
diferencia, usando que la rotación es Lipschitz respecto al ángulo con constante
`norm(P - C)`. No puede inventarse esa cota a partir del redondeo mostrado, de
una estimación métrica o de una muestra. El redondeo de los productos, sumas,
normalización y entradas se propaga además por el recinto nativo.

Para `P=(x,y,z)` y centro inhomogéneo `C=(cx,cy)`, la realización conserva la
secuencia nativa:

`qx=z*cx; qy=z*cy; u=x-qx; v=y-qy;`

`X=u*cos(theta)-v*sin(theta)+qx;`

`Y=u*sin(theta)+v*cos(theta)+qy; Z=z`.

La geometría ideal puede normalizar esta identidad cuando `z` está separado de
cero. La guarda nativa conserva los productos originales y la normalización:
no reemplaza silenciosamente la secuencia por una fórmula inhomogénea que
oculte overflow, underflow o pérdida completa de una dirección por redondeo.

### Incidencia, rayos y selección de raíces

Una regla reutilizable `Intersect(Circle(C,r), Ray(C,Q))` puede normalizarse a
`C + r normalize(Q-C)` solamente tras demostrar centro compartido por
operandos, radio positivo, dirección no nula y selección estable de la raíz
correspondiente. Son condiciones de construcción, no coincidencias de nombres
o coordenadas actuales.

Además, el recinto nativo debe mantener la dirección por encima del umbral de
degeneración, la incidencia y los predicados de camino limitado. Una norma
ideal positiva no basta si la realización puede redondear `Q` al propio `C`.
Tangencias, supresión por tolerancia, cambios de raíz y selección no uniforme
permanecen tipados y sin certificado. Esta regla no amplía las clases de
intersección ni materializa nuevas raíces por proximidad.

## Distinción respecto al coseno escalar

`SCALAR_COS` representa la operación numérica pública de `MyDouble.cos`, que
ajusta a cero por debajo de `Kernel.STANDARD_PRECISION` (`1e-8`). Su resultado
puede utilizarse directamente como divisor. `SCALAR_SIN` tiene el mismo ajuste
para argumentos de magnitud mayor que `0.1`.

La propuesta **no** convierte esos resultados en cosenos o senos ideales. Una
expresión como `1/cos(pi/2+1e-10)` no adquiere validez porque el coseno exacto
sea distinto de cero: el divisor nativo es cero. También debe rechazarse
`1/x(Rotate((1,0), theta))` cuando el recinto de la realización permita que esa
coordenada sea cero. La distinción de autoridades no puede perderse al
componer coordenadas, aritmética, círculos o intersecciones.

## Guarda nativa independiente del valor de una métrica

`NativeVariableRotationGuard2D.enclose(x,y,z,cx,cy)` es un helper puro para
operandos constantes capturados. Su referencia geométrica usa
`(x/z-cx,y/z-cy)`, no un campo inhomogéneo almacenado de forma independiente.
Exige entradas finitas y `abs(z)>Kernel.STANDARD_PRECISION`; conserva la
secuencia homogénea nativa y comprueba finitud de todos sus recintos
intermedios y de la normalización. La inversión de signo cuando `z<0` es
exacta y no cambia el cociente.

El helper conserva, para cada operación aritmética, el recinto de la fórmula
exacta con los coeficientes trigonométricos nativos representados y el recinto
de su realización redondeada. Propaga un error absoluto entre ambos. Sean
`u=2^-53`, `eta=Double.MIN_VALUE`, `M` una cota superior del valor absoluto del
resultado nativo encerrado y `r=u*M+eta`. `eta` es una cota representable
conservadora de la contribución de underflow. Se aplican outward:

- suma/resta: `e = ea + eb + r`;
- producto: `e = abs(a_ref)*eb + abs(b_ref)*ea + ea*eb + r`;
- división por el `z` constante: `e = ea/abs(z) + r`.

La aritmética intervalar contiene tanto el resultado real previo al redondeo
como cada resultado binario de la secuencia. Un overflow, un extremo no finito
o una división no admisible rechaza la prueba. El recinto de seno/coseno
nativo utiliza `[-nextUp(1),nextUp(1)]`; se conserva una ulp adicional en lugar
de necesitar una hipótesis más fuerte sobre el rango de `Math`.

A ese error aritmético se añade el error trigonométrico del mismo ángulo
nativo: `ec <= tau+ulp(1)`, `es <= ulp(1)`. Para el desplazamiento inhomogéneo
exacto `(dx,dy)`, las cotas de coordenadas son
`ex <= roundoffX+abs(dx)*ec+abs(dy)*es` y
`ey <= roundoffY+abs(dx)*es+abs(dy)*ec`. La norma invariante proporciona
recintos globales de la salida alrededor del centro. Se añade el error de las
restas nativas que `GeoVec3D.lineThroughPoints` usa para el rayo.

Con `dmin/dmax` encerrando la norma exacta del desplazamiento y `E` la cota de
norma del error tras esas restas, la dirección nativa cumple
`d_native in [max(0,dmin-E),dmax+E]`. Se exige
`directionLower > sqrt(2)*Kernel.STANDARD_PRECISION`, lo que garantiza que al
menos uno de los dos coeficientes de dirección supera su umbral. El error
crece con la magnitud de las coordenadas; un centro grande que puede absorber
el desplazamiento no adquiere una dirección falsa.

La prueba vale para **todo ángulo nativo finito**. No asigna a una longitud
numérica estimada el recinto de su integral verdadera. Una entrada métrica sin
recinto numérico de realización puede conservarse como opaca y
`finite-if-admitted`: cada evaluación nativa exige el resultado rico admisible
y el escalar finito. Una consulta inválida, agotamiento o fallo numérico retira
el resultado con causa; esta prueba no garantiza que el estimador siempre
termine. Negación y división por constante finita de magnitud al menos uno
pueden preservar esa finitud sin valor. Otros consumidores necesitan su
propia prueba o permanecen sin establecer.

El helper **no** prueba por sí solo el coeficiente libre de una recta,
discriminante, pertenencia al camino, permutación o supresión de raíces. El
consumidor radial debe demostrar además esos predicados nativos. Una
coordenada de la salida utilizada como divisor no puede heredar la guarda de
norma: si su recinto incluye cero, la división sigue sin ser admisible.

`getErrorX/Y` se refieren al mismo ángulo nativo; no acotan una diferencia
angular desconocida entre la estimación y la integral verdadera. Para ángulos
arbitrarios distintos solo se puede afirmar inicialmente una cota amplia de
`2R` más errores, donde `R` es el radio de la salida considerada. No se
presenta como precisión del renderer ni como garantía de la estimación.

### Addendum R9: banda nativa para radio variable y raíz radial positiva

La extensión de InheritedLineCircleBand2D conserva sus entradas y fórmulas
históricas cuando no se suministran cotas adicionales. Los nuevos campos
opcionales de Box comienzan con NaN: matrixA2MagnitudeUpper,
halfAxisLower y radiusRepresentationError. requireStrictPositiveRay comienza
en false. Una cota explícita inválida no permite emitir una prueba.

Para un círculo de centro constante y radio variable, la variación del radio
es dominio geométrico, no error de representación. El consumidor demuestra
por separado una cota EC no negativa del error puntual entre el cuadrado del
semieje nativo y el radio al cuadrado representado por su matriz. La banda usa
esa cota solo cuando se declara radiusRepresentationError; en otro caso
conserva la comparación histórica con el semieje constante. El denominador
de la prueba de incidencia usa una cota inferior outward de halfAxisLower²
cuando existe, y conserva el semieje anterior en otro caso. Los términos
absolutos de la fórmula nativa w usan como magnitud de A2 el máximo entre
abs(a2) y matrixA2MagnitudeUpper. Las cotas deben cubrir todos los valores
puntuales de la caja, no solo sus extremos o una muestra.

La prueba de camino histórica permite la tolerancia nativa de 1e-5 fuera del
rayo. La modalidad explícita requireStrictPositiveRay exige en cambio
pathLow>1e-5 y error < lower(pathLow-1e-5). Se mantiene además cualquier
restricción del extremo finito. Para la construcción radial cuyo origen de
rayo es el centro del círculo, el consumidor establece la simetría de las
raíces y el signo de la raíz seleccionada. La misma cota de error de ambas
raíces demuestra entonces que la positiva sigue admitida y la negativa queda
fuera incluso considerando la tolerancia; no se elige una raíz por cercanía.

La cota coordinateError ya toma el máximo de los errores de q/u y w/q,
seguido del error de reconstrucción de coordenadas. Cuando todos los
predicados de incidencia y camino se cumplen, cada normalización admisible
aporta una cota outward de coordinates+coordinateError. Su máximo se devuelve
en computedCoordinateMagnitudeUpper. Este campo de salida se reinicia en cada
llamada y solo constituye evidencia cuando keepsOutput devuelve true. El
recinto [-M,M] resultante contiene las coordenadas de ambas raíces calculadas;
no aporta derivadas ni precisión respecto a un ángulo semántico distinto.

NativeRadialRayGuard2D puede reunir esas pruebas en una cubierta fija de 64
cajas de fase, conservando el resto de guardas heredadas, sin muestrear
ángulos de una estimación métrica. Las pruebas focales deben conservar los
controles históricos, aceptar cotas explícitas válidas y rechazar márgenes
insuficientes, representación no acotada o degeneración. Este addendum
describe la extensión antes de su implementación; no afirma resultados de
ejecución.

## Compatibilidad, fallos y validación

Los operadores `ROTATE` y `ROTATE_ABOUT` existentes conservan sus coeficientes
constantes capturados y sus firmas. La nueva regla es derivada, versionada y
reconstruible; no introduce una asociación persistente ni un nuevo comando.
La decisión aprobada incluye su aplicación a las nuevas certificaciones de
documentos nuevos y antiguos: una rotación variable que antes estaba fuera de
la clase puede adquirir evidencia de la geometría ideal bajo esta regla. No se
cambia el XML, la identidad de los objetos ni el cálculo de los valores
nativos. Sí cambia expresamente el mapa al que se atribuye la nueva evidencia,
conforme a la enmienda de autoridad aprobada. No se presume que la política
global del documento haya pasado a `constructive-authority/v1`.

El programa, su firma y la evidencia deben identificar
`composition-rotation-geometry/v1`; una reapertura reconstruye y revalida esa
regla. Una prueba emitida con esta autoridad no se presenta como una prueba de
continuidad de la realización nativa literal. El diseño debe comparar la
reapertura de documentos antiguos y sus consumidores, conservando los valores
numéricos, las identidades y las antiguas operaciones constantes. Si esos
invariantes no se cumplen, no se considera una migración compatible.

Se rechaza o conserva el estado no establecido si faltan origen durable,
entradas actuales, recintos de argumento o realización, finitud, margen de
degeneración, selección uniforme o presupuesto. Un cambio paramétrico vuelve a
validar las mismas premisas; no selecciona otra interpretación silenciosamente.
Los controles flotantes existentes de publicación y de raíces siguen vigentes.

La aceptación técnica requiere, al menos:

- cajas que atraviesan ambos umbrales del ajuste del coseno de rotación, con
  valores y derivadas ideales encerrados y realización encerrada por separado;
- reciprocales del coseno escalar y de una coordenada rotada que puede ser cero;
- puntos homogéneos con `z!=1`, overflow de `z*cx` y un centro grande que puede
  absorber por redondeo el desplazamiento de un extremo de rayo;
- radio positivo, radio degenerado, raíz regular asentada, tangencia, camino
  limitado y selección no uniforme;
- preservación de operaciones y firmas constantes, captura/reapertura y
  revalidación tras pérdida y recuperación de premisas;
- composiciones sintéticas ajenas al cono y, por separado, el cono original
  inmutable con continuidad y velocidad positiva en cada componente, extremos
  laterales distintos, cambios de `H` y `v_1`, cortes y rendering coherente.

## Alternativa literal y texto breve para el autor

Conservar la autoridad literal mantiene las guardas actuales: las cajas
uniformes usan su rama y una caja que cruza el ajuste queda sin prueba. Para
obtener cobertura completa sería necesario aislar y representar sus
microeventos con límites laterales, incluso cuando su magnitud esté por debajo
de la resolución visual. Esa alternativa no permite afirmar que el mapa
literal sea una curva regular continua a través de tales eventos. Tampoco
autoriza a borrar las bandas no resueltas.

**Decisión aprobada por el autor:** autorizar que la nueva rotación de ángulo
variable se certifique como la rotación geométrica matemática, considerando el
ajuste numérico del coseno como error acotado de su realización y exigiendo
guardas nativas separadas para todos sus consumidores. Se mantienen intactas
las operaciones constantes históricas y el coseno escalar público. La regla
versionada se aplicaría a nuevas certificaciones de documentos nuevos y
antiguos, sin cambiar sus valores calculados, identidades o XML. La opción
alternativa conserva la interpretación literal y sus eventos microscópicos.
