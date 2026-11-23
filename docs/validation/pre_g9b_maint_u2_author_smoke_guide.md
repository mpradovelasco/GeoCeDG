# PRE-G9B-MAINT-U2 — Guía de smoke manual para el autor

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-U2`: simplificación de la herramienta Punto |
| Prompt | [`pre-g9b-maint-u2-point-tool-unification`](../../.github/prompts/tasks/pre-g9b-maint-u2-point-tool-unification.prompt.md) |
| Informe del candidato | [informe](pre_g9b_maint_u2_candidate_report.md) |
| Carácter | Procedimiento de comprobación manual. No registra resultados ni aprobación: el autor anota lo que observa en su propio registro |

La guía cubre lo que las pruebas automáticas no observan: ventanas reales, foco, ratón físico y
lectura de los textos en las dos lenguas. Duración estimada: 15 a 20 minutos.

## 0. Preparación

1. Arranque GeoCeDG desde el worktree del candidato (`GeoCeDG-wt-maint-u2`, rama
   `phase/pre-g9b-maint-u2`), por ejemplo con `.\gradlew.bat :desktop:desktop:runGeoCeDG`.
   No use el directorio principal del repositorio.
2. Introduzca en la barra de entrada, en un documento nuevo:

   ```text
   S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)
   Q=(0.3,0.02)
   source=Segment((0,3),(2,3))
   ReferenceQ=(1,3.03)
   ```

3. Repita las secciones 1 a 7 primero en inglés y después en español (cambie el idioma de la
   aplicación entre las dos pasadas). El cambio de idioma reconstruye el panel.
4. `runGeoCeDG` puede dejar registros `hs_err_pid*.log` sin seguimiento bajo
   `source/desktop/desktop` si la JVM falla; no los borre: avise para moverlos a `artifacts/`.

## 1. Panel único y selector de origen (U2-1, U2-3)

1. Elija **Point**. Sobre la barra de entrada aparece el panel con **Origin:** y dos segmentos,
   **Click** (seleccionado) y **Reference Q**, y una línea que empieza por «Radius from the
   view».
   - ES: **Origen:**, **Clic** (seleccionado), **Referencia Q**; «Radio de la vista…».
2. Pase el puntero sobre esa línea: la ayuda emergente explica que la búsqueda puede crear el
   punto al instante y que las demás ofertas esperan a **Confirm**.
3. Pulse **Reference Q**: queda seleccionado, **Click** se deselecciona, la instrucción pasa a
   «Select object» y la línea a «Radius r world around Q · every offer needs Confirm».
4. Pulse otra vez **Reference Q**: vuelve **Click** (los dos segmentos son complementarios).
5. Elija **Point on object**: el panel es el mismo y conserva la elección de **Point**.

Esperado: ningún punto creado en estos pasos; el panel cabe en la anchura de la ventana.

## 2. Clic con Point (sin cambios de conducta)

1. Con **Point** y **Click**, pulse sobre la spline `S` cerca de `x = -1`.
2. Esperado: se crea un punto sobre `S` (la búsqueda halla la posición) o el panel ofrece una
   posición con **Confirm**; en ese caso, pulse **Confirm**. Un **Undo** retira el punto.
3. Pulse sobre el segmento `source`: se crea un punto nativo sobre el segmento, como antes.

## 3. Referencia Q con Point

1. Con **Point**, seleccione **Reference Q**. Pulse sobre `S` y después sobre `Q`.
2. Esperado: el panel ofrece la posición; nada se crea antes de **Confirm**. Tras **Confirm**
   hay un punto nuevo en `x ≈ 0.3`. Mueva `Q`: el punto nuevo no se mueve.

## 4. Herramienta 145 como alias (U2-2)

1. Elija **Construction → Semantic curves → Point on Locus V2 from reference**.
2. Esperado: el mismo panel, con **Reference Q** preseleccionada, instrucción «Select object».
   El icono y la posición de la herramienta en el desplegable no cambian.
3. Pulse **Click**: la línea pasa a «Radius r world around the click · every offer needs
   Confirm». Pulse sobre `S`: el panel ofrece la posición; pulse **Confirm**.
4. Cambie a **Move** y vuelva a la herramienta: conserva **Click**. Vuelva a **Point**: conserva
   su propia elección.

## 5. Menú contextual

1. Con **Move**, pulse con el botón derecho sobre el segmento `source` y elija **Create point
   from reference Q**.
2. Esperado: se activa **Point** con **Reference Q** seleccionada y el segmento elegido; la
   instrucción pide seleccionar `Q`. Pulse sobre `ReferenceQ` y **Confirm**: aparece un punto
   sobre el segmento en `(1,3)`.
3. Repita sobre la spline `S` con `Q`: el procedimiento es el mismo.

## 6. Permitir arrastre y Undo

1. En **Point**, desactive **Allow dragging**, cambie de origen y compruebe que sigue
   desactivada.
2. Cree un punto con **Reference Q** y **Confirm**: el punto queda fijo (no se arrastra con
   **Move**) y sigue a la curva si cambia la curva.
3. Un solo **Undo** retira el punto creado.

## 7. Asistencia de extremos métricos (U2-4)

1. En un documento nuevo introduzca, línea a línea, el lugar de prueba de CU7 y dos puntos libres:

   ```text
   I=(0.4965866445356883,1.7854818601338847)
   J=(-0.5082406249102692,2.5763181370126467)
   f=Circle(I,J)
   D=Point(f)
   g=Line(D,xAxis)
   K=Intersect(g,yAxis)
   M=Midpoint(K,D)
   h=LocusV2(M,D)
   A=(0.8976484384297626,1.7854818601338847)
   B=(3,-2)
   ```

2. Elija **Partial semantic locus length (experimental)** (ES: **Longitud semántica parcial del
   lugar geométrico (experimental)**) y pulse `h` (por ejemplo cerca de `(0.8, 2.4)`), `A` y `B`.
3. Esperado: el diálogo **Construct a metric endpoint** ofrece **Choose a position on the
   locus**. Al elegirlo:
   - la lista muestra las posiciones como el panel, por ejemplo «Branch 1 · Section 1 · u =
     …» (ES: «Rama 1 · Tramo 1 · u = …»), con el identificador técnico como ayuda
     emergente;
   - una línea muestra la garantía de la ruta (por ejemplo «Position verified locally»),
     con el detalle como ayuda emergente;
   - la casilla **Allow dragging** aparece activada.
4. Desactive **Allow dragging**, elija la posición y confirme el diálogo: se crea un punto fijo
   sobre `h`. Un solo **Undo** lo retira.
5. Repita y pulse **Cancel**: no se crea nada.

## 8. Rechazo «componentes no establecidas» (U2-5)

No hay un modelo de 1.2.2 que reproduzca este rechazo de forma fiable desde el panel (su
caracterización corresponde a `MAINT-F8-S0`). Si aparece, el texto debe decir:

- EN: «No point created: the components of this curve are not established here, so the position
  cannot be confirmed on a component. Use Point(curve, parameter) or the tool Point on semantic
  curve (explicit address)».
- ES: «No se creó ningún punto: las componentes de esta curva no están establecidas aquí, así
  que la posición no puede confirmarse sobre una componente. Use Point(curva, parámetro) o la
  herramienta Punto sobre curva semántica (dirección explícita)».

El código tipado `COMPONENT_NOT_ESTABLISHED` sigue disponible en la ayuda emergente del estado.

## 9. Guías

Lea en el worktree `docs/user/geocedg_user_guide_en.md` y `_es.md` (o la guía integrada del producto) §8.1 «Create a point on a curve» / «Crear un
punto sobre una curva» y el párrafo de §9.3 sobre **Choose a position on the locus** / **Elegir
una posición en el lugar**. Compruebe que los nombres de los controles coinciden con el panel.
