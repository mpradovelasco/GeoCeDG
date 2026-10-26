# PRE-G9B-CU3 — Guía de prueba manual del candidato

| Campo | Valor |
|---|---|
| Carácter | Guía concisa de la prueba manual del autor que pide la disposición autoral de 2026-10-26, punto 6. Este documento no sustituye ni simula la prueba |
| Producto | el del candidato técnico `3c2ca7e8`. Los commits posteriores de la rama `phase/pre-g9b-cu3-implementation` (registro de verificación, caracterizaciones y documentos) no cambian el producto |
| Modelo | una **copia** de `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg`. El original es evidencia autoral fijada por SHA-256: no se abre para guardar ni se modifica |
| Resultado | el autor anota `PASS` o `FAIL` por paso, con la observación y la posición del modelo si falla. Resultado del 2026-10-26: smoke funcional `PASS — AUTHOR REPORTED`; rendimiento interactivo `NOT ACCEPTED` ([registro del smoke](pre_g9b_cu3_author_smoke_record.md)) |
| `selfApproved` | `false` |

**Preparación.** Copiar el modelo a una carpeta de trabajo y abrir la copia con el producto del
candidato. La copia no está adoptada: su política es `legacy-representation-authority/v1`.

- **Abreviaturas:** «Inspeccionar» es la acción del menú contextual **Inspeccionar definición…** y
  «resultado rico» es **Inspeccionar resultado rico**.
- **Etiquetas:** las órdenes usan las del modelo. `K` y `J` son las intersecciones de `p` e `i`, y
  `K` es la salida 1.
- **Tiempo de respuesta:** con `e_2` cada paso puede tardar más que en las pruebas automáticas,
  que lo retiran.

## A. Adopción explícita

1. **Oferta.** En la entrada algebraica, escribir `X1 = Intersect(e_1, n, K)`.
   - *Esperado:* aparece el diálogo **Autoridad constructiva**, con el mensaje, las relaciones
     examinadas, las relaciones cuya integridad o admisión cambia y los botones **Adoptar** y
     **Cancelar**, con Cancelar por defecto.
   - *Si falla:* el documento ya estaba adoptado; la etiqueta `X1` ya existía; o la orden falló por
     otra causa (leer el mensaje).
2. **Cancelar.** Pulsar **Cancelar**.
   - *Esperado:* no se crea `X1`, la política no cambia y Deshacer no tiene ningún paso nuevo.
3. **Adoptar.** Repetir el paso 1 y pulsar **Adoptar**.
   - *Esperado:* `X1` queda definido sobre el contacto. Un único `Ctrl+Z` retira a la vez `X1` y la
     adopción, y `Ctrl+Y` restaura las dos.
   - *Si falla:* dos pasos de Deshacer en lugar de uno, o la adopción sin `X1`, es un fallo del
     punto único de Deshacer.
4. **Inglés.** Cambiar el idioma a inglés y repetir los pasos 1 y 2 con `X0 = Intersect(e_1, n, K)`.
   - *Esperado:* **Constructive authority**, **Adopt** y **Cancel**, con el mismo contenido.

## B. Los cinco contactos tras la adopción

5. **Crear.** Con el documento adoptado, crear `X2 = Intersect(e_1, c_1, K)`,
   `X3 = Intersect(o_1, h_2, K)`, `X4 = Intersect(e_1, c_1, J)` y `X5 = Intersect(e_1, r, J)`.
   - *Esperado:* los cinco puntos, `X1` a `X5`, quedan definidos en las posiciones de los contactos
     del modelo.
6. **Inspeccionar** cada uno.
   - *Esperado:* `Integridad` es `STRICT`; `Pertenencia de P a L` es `STRICT`, por transferencia;
     `Incidencia de P en r` es `STRICT` con `CONSTRUCTIVE_IDENTITY_CERTIFICATE`; el error declarado
     de la posición aparece aparte de la integridad.
   - *Si falla:* `RELAXED` o `NOT_ESTABLISHED` con una causa (presupuesto, celda, transferencia):
     anotar la causa exacta. Es un fallo, porque en el estado guardado los cinco son `STRICT`.
7. **Resultados ricos.** Crear `R1 = Intersect(e_1, c_1)` y `R2 = Intersect(e_1, r)` y abrir su
   resultado rico; abrir también el de `u_1`.
   - *Esperado:*
     - en `R1` hay dos contactos, en `0` y en la costura (`−π`); en `R2`, uno en la costura;
     - todos son `TANGENT_ESTABLISHED`, `EXISTENCE_EXACT` y `CONTACT_EXISTENCE_EXACT`, con orden
       `FOLD_CONTACT`, y sin token admisible;
     - `u_1` muestra la tangencia en `0` con orden `TWO`.
   - *Si falla:* una raíz transversal admisible en la costura indica que la regla de conflicto no
     actuó (comprobar que el documento está adoptado).
   - *Nota:* no se crea ningún punto sin conductor sobre estos contactos; eso es `CU3-B`, pendiente.

## C. Dinámica

8. **Arrastre de `H`.** Arrastrar `H` sobre su camino unos pasos y volver.
   - *Esperado:* `X1` a `X5` siguen definidos y `STRICT`. Cada paso tarda unos 0,75 s, frente a unos
     0,08 s sin adoptar: es la limitación aceptada provisionalmente.
   - *Si falla:* anotar la posición si un contacto queda indefinido o no `STRICT` dentro de la misma
     celda. Si `H` sale de su celda, una prueba nueva es lo esperado y puede tardar más.
9. **Arrastre de `B`.** Igual que el paso 8.
10. **`D_1` ×3.** Ejecutar
    `SetCoords(D_1, x(H) + 3 (x(D_1) - x(H)), y(H) + 3 (y(D_1) - y(H)))`.
    - *Esperado:*
      - `X1` y `X5` quedan indefinidos con la causa `NOT_ON_LIMITED_TARGET` y la incidencia
        `CONTRADICTED`: el punto queda fuera de la parte limitada de `n` y de `r`;
      - `X3` queda indefinido porque `M_1` no existe;
      - `X2` y `X4` siguen `STRICT`.
    - *Después:* deshacer con `Ctrl+Z`.
11. **`α_cone` a 35°.** Ejecutar `SetValue(α_{cone}, 35°)` y abrir el resultado rico de `R1`.
    - *Esperado:* solo quedan los dos contactos en pliegue, sin puntos de ruido junto a ellos. Sin
      adoptar, el mismo caso publica siete raíces.
    - *Después:* deshacer.
12. **`D_1` a la mitad de su distancia a `H`.** Ejecutar
    `SetCoords(D_1, x(H) + 0.5 (x(D_1) - x(H)), y(H) + 0.5 (y(D_1) - y(H)))` y abrir el resultado
    rico de `u_1`.
    - *Esperado:* una sola tangencia de orden `TWO`. Sin adoptar, aparecen cuatro raíces.
    - *Después:* deshacer.
13. **Deshacer la adopción.** Deshacer hasta antes de la adopción y abrir el resultado rico de `R1`
    y `R2` si siguen existiendo; si no, crearlos de nuevo sin adoptar.
    - *Esperado:* la raíz transversal de la costura vuelve a ser admisible, con su token.

## D. Persistencia y lectores anteriores

14. **Reapertura.** Guardar la copia adoptada con `X1` a `X5`, cerrarla y reabrirla.
    - *Esperado:* la política adoptada se conserva; los cinco contactos son `STRICT` y Deshacer y
      Rehacer funcionan tras la reapertura.
15. **Producto anterior.** Abrir la copia adoptada con un producto anterior, por ejemplo uno
    construido desde la base `3cd0f710`.
    - *Esperado:* la carga falla con `MALFORMED_RECORD`, el mensaje nombra el registro
      `authorityPolicy`, y el documento que estuviera abierto queda intacto.
    - *Control:* una copia **sin adoptar** se abre con normalidad.

## Causas de fallo frecuentes

| Síntoma | Causa probable |
|---|---|
| ninguna oferta de adopción | el documento ya estaba adoptado o la orden falló antes por otra causa |
| `NOT_ESTABLISHED` con `BUDGET_EXHAUSTED` | un caso fuera del alcance medido; anotar los contadores del mensaje |
| `NOT_ESTABLISHED` tras mover un punto libre | se salió de la celda del certificado; el resultado de la prueba nueva está en el inspector |
| punto sobre la salida «equivocada» de `p ∩ i` | la permutación heredada del modelo: `K` es la salida 1 |
| diferencias en un documento sin adoptar | no son de `CU3`: la política heredada no cambia (CU3.4) |
