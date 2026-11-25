# PRE-G9B-MAINT-A2 — Guía de smoke manual del autor

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-A2`: familia B-spline polinómica `bspline-2d/v1` (comando `BSpline`) |
| Informe del candidato | [informe](pre_g9b_maint_a2_candidate_report.md) |
| Estado | guía para el smoke manual del autor; no registra ningún resultado |
| `selfApproved` | `false` |

Cada paso indica lo que se espera ver. Cualquier diferencia se anota con el paso, la acción y lo
observado. El smoke no sustituye a las pruebas automáticas ni las amplía.

## 1. Preparación

1. Abra GeoCeDG construido desde el candidato, con un documento nuevo, en inglés.
2. Escriba en la entrada algebraica:
   `A=(0,0)`, `B=(1,2)`, `C=(3,2)`, `D=(4,0)`, `E=(5,1)`.

## 2. Creación y formas

| Paso | Acción | Se espera |
|---|---|---|
| 2.1 | `b=BSpline({A,B,C,D,E})` | Curva cúbica que empieza en `A` y termina en `E` sin pasar en general por `B`, `C`, `D`. Tipo «B-spline». Aparecen una lista `l1` y un número con valor 3 (argumentos promovidos, como en Spline V2) |
| 2.2 | `b2=BSpline({A,B,C,D,E},3,{0,0,0,0,1,4,4,4,4})` | Curva con los mismos puntos y nudos desiguales |
| 2.3 | `b1=BSpline({A,B,C,D,E},1)` | La poligonal de control |
| 2.4 | `bz=BSpline({A,B,C,D},3)` | La Bézier cúbica de `A,B,C,D`, que empieza en `A` |
| 2.5 | Abra la Ayuda de entrada y busque `BSpline` | Las tres sintaxis, también en español con marcadores en español y cabecera `BSpline` |

## 3. Indefinición tipada y recuperación

| Paso | Acción | Se espera |
|---|---|---|
| 3.1 | `p=3`, `bp=BSpline({A,B,C,D,E},p)`, luego `p=8` | `bp` queda indefinida, sin dibujo ni geometría antigua |
| 3.2 | `p=3` | `bp` reaparece |
| 3.3 | `bk=BSpline({A,B,C,D},3,{0,0,0,0,1,1,1,0.5})` | Indefinida (nudos decrecientes); no hay error de comando |
| 3.4 | `bf=BSpline({A,B,C})` | Indefinida (pocos puntos); el grado no se rebaja |

## 4. Edición dinámica

| Paso | Acción | Se espera |
|---|---|---|
| 4.1 | Arrastre `E` | `b` se recalcula; solo cambia la parte final |
| 4.2 | Arrastre `A` hasta que coincida exactamente con `E` (o use `bc=BSpline({A,B,C,D,A})`) | Curva cerrada; puede tener esquina en la costura |

## 5. Medida, puntos, intersecciones y semejanzas

| Paso | Acción | Se espera |
|---|---|---|
| 5.1 | `LocusLength(b)` | Longitud finita con evidencia de estimación |
| 5.2 | `P=Point(b,"bspline-2d/main",1.5)` y arrastre `P` | `P` se mueve sobre la curva |
| 5.3 | `g:y=1.5` y `Intersect(b,g)` | Resultado rico con las intersecciones visibles |
| 5.4 | `Translate(b,Vector((1,1)))`, `Rotate(b,pi/6,A)` | Imágenes que siguen a `b` al mover sus puntos |
| 5.5 | Zoom y desplazamiento de la vista | Longitud e intersecciones no cambian |

## 6. Persistencia, Deshacer, copia

| Paso | Acción | Se espera |
|---|---|---|
| 6.1 | Guarde como `.cedg`, cierre y vuelva a abrir | Mismas curvas, medidas e intersecciones |
| 6.2 | Cambie `p` y use Deshacer y Rehacer | Se restaura cada estado |
| 6.3 | Copie `b` con sus puntos y pegue | Nueva curva con identidad nueva |
| 6.4 | Redefina `b` como `BSpline({A,B,C,D,E},2)` | La curva cambia de grado |
| 6.5 | Inserte el `.cedg` guardado en otro documento (Insertar archivo) | Se admite |

## 7. Rechazo de un documento con contrato desconocido

Solo si el autor desea comprobarlo a mano: en una copia del `.cedg`, cambie dentro de
`geogebra.xml` `contract="bspline-2d/v1"` por `contract="bspline-2d/v2"` y abra la copia. Se
espera un mensaje que indica una versión de contrato de B-spline más reciente y no admitida; el
documento abierto antes no cambia.

## 8. Fuera de alcance

No hay herramienta gráfica para `BSpline`. Los documentos con `BSpline` no se abren en GeoCeDG
1.2.2. La exportación DXF `SPLINE` nativa (`MAINT-A3`), los pesos (`MAINT-A-R`) y los resultados
certificados (`MAINT-A4`) no forman parte de este incremento.
