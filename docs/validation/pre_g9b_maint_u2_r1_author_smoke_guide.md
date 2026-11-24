# PRE-G9B-MAINT-U2-R1 — Guía de smoke reducida para el autor

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-U2-R1`: corrección funcional y de presentación tras el smoke de U2 |
| Prompt | [`pre-g9b-maint-u2-r1-smoke-corrections`](../../.github/prompts/tasks/pre-g9b-maint-u2-r1-smoke-corrections.prompt.md) |
| Informe del candidato | [informe](pre_g9b_maint_u2_r1_candidate_report.md) |
| Carácter | Procedimiento de comprobación manual. No registra resultados ni aprobación: el autor anota lo que observa en su propio registro |

Cubre solo las cinco observaciones del smoke de U2. El resto de la conducta de U2 (selector
**Clic | Referencia Q**, herramienta 145, menú contextual, Punto en objeto) no cambia; la
[guía de U2](pre_g9b_maint_u2_author_smoke_guide.md) sigue valiendo para ella. Duración
estimada: 10 minutos.

## 0. Preparación

1. Arranque GeoCeDG desde el worktree `GeoCeDG-wt-maint-u2`, rama `phase/pre-g9b-maint-u2-r1`,
   con `.\gradlew.bat :desktop:desktop:runGeoCeDG`. No use el directorio principal.
2. Haga las secciones 1 a 5 en inglés y repítalas en español (cambie el idioma de la
   aplicación entre las dos pasadas).

## 1. Ayudas emergentes del panel Punto (U2-R1-1)

1. Elija **Point**. Pase el puntero sobre la línea que empieza por «Radius from the view».
2. Esperado: la ayuda ocupa varias líneas cortas, en este orden: qué hacer («Click a Locus V2
   or Spline V2…»), **Radius:** …, **Confirm:** … y la nota sobre objetos nativos. No hay una
   línea que cruce la pantalla.
3. Pulse **Reference Q** y repita: propósito, **Radius:** r world alrededor de Q,
   **Confirm:** toda oferta espera a Confirm, y «The new point does not depend on Q».
4. Pase el puntero sobre **Allow dragging**: dos líneas.
5. Con **Construction → Semantic curves → Point on Locus V2 from reference** (145), pulse
   **Click** y repita el paso 2: tres líneas.

## 2. Ayuda de la herramienta de dirección explícita (U2-R1-2)

1. Abra **Construction → Semantic curves** y pase el puntero sobre **Point on semantic curve
   (explicit address)**.
2. Esperado: el nombre no cambia y la ayuda ocupa tres líneas: «Select a supported semantic
   curve.», «Then enter its branch key and canonical parameter.» y «The Point tool finds the
   position from a click instead.».
   - ES: «Seleccione una curva semántica compatible.», «Después introduzca su clave de rama y
     su parámetro canónico.» y «La herramienta Punto halla la posición con un clic.».
3. Elija la herramienta y pase el puntero sobre su botón en la barra: la misma ayuda, en
   varias líneas, bajo el nombre en negrita.

## 3. Asistencia de extremos métricos (U2-R1-3)

1. En un documento nuevo introduzca, línea a línea, en la barra de entrada:

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

   `A` es un punto libre a 0,01 del lugar `h`, sin dirección semántica sobre él.
2. Elija **Partial semantic locus length (experimental)** y pulse, por este orden, `h` (por
   ejemplo cerca de `(0.8, 2.4)`), `A` y `B`.
3. Esperado, primer diálogo **Construct a metric endpoint**: «A has no semantic address on h:
   it cannot be an endpoint of the length.» y «You can create a new point on h at a position
   next to A; A does not change.», con **Choose a position on the locus** y **Cancel**.
4. Pulse **Choose a position on the locus**. Esperado, segundo diálogo:
   - «Positions of h next to A (search radius 0.1):» y la lista con una posición, por ejemplo
     «Branch 1 · Section 1 · u = 0.0000» (sin signo menos), **sin ninguna fila seleccionada**;
   - «Select a position, then Create point.», la garantía («Position verified locally»),
     **Allow dragging** activada y la nota «The new point does not replace A in the length:
     measure again with the new point.»;
   - **Create point** desactivado y **Cancel**.
5. Pulse **Create point** sin seleccionar nada: no ocurre nada.
6. Pulse la fila: queda seleccionada y **Create point** se activa. Desactive **Allow
   dragging** y pulse **Create point**.
7. Esperado: aparece un único punto nuevo sobre `h`, muy cerca de `A` (a 0,01) pero distinto
   de `A`; `A` no se mueve. Con **Move** el punto nuevo no se arrastra. La longitud aún no se
   calcula: vuelva a usar **Partial semantic locus length** con `h`, el punto nuevo y `B`.
8. **Undo** una vez: desaparece solo el punto nuevo. **Redo**: vuelve, también fijo.
9. Mueva `I`: el punto nuevo sigue a `h`. Devuelva `I` a su sitio.
10. Repita los pasos 2 a 4 y pulse **Cancel** en cada diálogo (también con una fila
    seleccionada, y cerrando la ventana con su botón o con Escape): no se crea nada.
11. Defina `F=(6,6)` y repita los pasos 2 y 4 con `F` en lugar de `A`: un aviso dice «No
    position of h was found next to F (search radius 0.1). No point was created.» y no se
    crea nada.

## 4. Iconos de Locus V2 y de las longitudes (U2-R1-4)

1. Abra en la barra el desplegable de **Semantic Locus V2** (curvas semánticas) y el de
   medidas, que contiene **Total semantic locus length** y **Partial semantic locus length**;
   en los menús están en **Construction → Semantic curves** y **Construction → Metrics and
   validation** (ES: **Construcción → Curvas semánticas** y **Construcción → Medidas y
   validación**).
2. Esperado a tamaño de barra:
   - Locus V2: el icono de siempre (barra superior con dos círculos, trazos rojos
     discontinuos y punto azul);
   - longitud total: la curva entera en azul con sus dos extremos negros y una cota de lado a
     lado debajo;
   - longitud parcial: la curva negra con un tramo azul entre dos extremos blancos y una cota
     solo bajo ese tramo.
3. Compruebe que se distinguen también con el fondo gris de la barra y, si dispone de ello,
   con escalado de pantalla del 150 % o 200 %.

## 5. Spline V2 y Spline V2 cerrada (U2-R1-5)

1. Abra el desplegable de **Semantic Spline V2**.
2. Esperado: **Semantic Spline V2** y **Spline V2 with degree** conservan su icono; **Closed
   Semantic Spline V2** muestra el mismo dibujo con una flecha azul fina que vuelve del último
   punto de control al primero por el borde superior e izquierdo.
3. Cree una spline cerrada como de costumbre: la herramienta funciona igual que antes.
