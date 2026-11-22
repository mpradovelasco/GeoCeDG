# GeoCeDG 1.2.2 — evidencia final de cierre para la aprobación del autor

| Campo | Valor |
|---|---|
| Carácter | **Evidencia técnica para la aprobación del autor.** No aprueba nada. `selfApproved=false`. Un resultado automatizado aceptado no es aprobación autoral |
| Instrucción | [comprobación de cierre](post_r1_1_2_2_closing_check_author_instruction_record.md), 2026-11-22 |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| **Candidato de cierre** | `a6562fa30dc7a468b8e62e16843f1ae2f440285b`, árbol `aac4b541b28660233cccd295791bb1a82871d108` |
| Candidato anterior | `bb341874b`, aceptado provisionalmente por el autor; el de cierre añade solo la corrección de §2 y su inventario |
| Versión y fecha | GeoCeDG 1.2.2, `2026-11-22` |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`, sin tocar; etiquetas de versión `v1.2.0` y `v1.2.1` |
| No ejecutado | `FINAL`, promoción a `main`, etiqueta `v1.2.2`, release, instalación de MSI o EXE |

## 1. Comprobación del ciclo de vida de `LocusLength(w_2)`

| Paso pedido | Resultado en el candidato anterior `bb341874b` | Resultado en el candidato de cierre |
|---|---|---|
| 1. Crear la longitud total y medir su coste | Número indefinido (subtotal de §28); en el ejecutable real, de ≈ 1,65 s a ≈ 6,3 s por cambio real (6 237–6 390 ms) | Igual |
| 2. Eliminarla por la interfaz normal (`deleteSelectedObjects`, la tecla Supr y el menú contextual) | El número desaparece | Igual |
| 3. ¿Queda un resultado rico auxiliar? ¿Tiene consumidores legítimos? | **Sí: queda su autoridad rica privada, sin ningún lector**, en el espacio de nombres reservado: el usuario no puede verla ni borrarla | No queda ninguno |
| 4. Sin consumidores, ¿se libera el cálculo? | **No:** el trabajo por cambio real sigue igual que con el número (22 824 evaluaciones de `w_2` en cuatro cambios) | **Sí:** vuelve al valor anterior a crearlo |
| 5. Guardado, reapertura | El huérfano se guarda, se reabre y sigue costando ≈ 6,2–6,5 s por cambio real | Documento sin huérfano; la reapertura cuesta lo mismo que el documento original |

**Diagnóstico.** La permanencia **no respondía al contrato**: §27 de
[`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) define esa autoridad
como la dependencia de un solo número, y §13 exige que la eliminación de un consumidor
libere su propiedad. Es un defecto presente desde 1.2.0, que en 1.2.2 se vuelve costoso
porque la longitud total de un lugar condicionado cuesta varios segundos.

## 2. Corrección mínima

Commit `781629cb1`, norma [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §29.

- Al eliminar el número, el adaptador escalar elimina también su autoridad rica privada si
  ningún otro algoritmo la lee. Si otro la lee, se queda, y se va con el último.
- El criterio es el nombre reservado, que se persiste: un documento reabierto se comporta
  igual. Un resultado rico con nombre propio del documento (los `LocusLength` anteriores a
  1.2.0, como `w_1` o `n_2` del modelo del autor) nunca se elimina con su número.
- Deshacer restaura el número con su autoridad; Rehacer elimina los dos.
- Ninguna otra semántica cambia. No se ha tocado la admisibilidad ni el cálculo de la
  longitud total.

**Límite declarado, sin cambios.** Si el número se **redefine** (por ejemplo `cuT = 5`), la
autoridad anterior sigue quedando sin lector, como en 1.2.0 y 1.2.1. Lo comprobé: eliminarla
dentro de la redefinición choca con la transacción de identidad espacial («Spatial identity
graph publication cannot overlap an active switch or uncompleted redefine»), y resolverlo
exige tocar esa transacción, que la instrucción no autoriza. Queda documentado en §29 y fijado
por una prueba para que cualquier cambio se vea. El remedio para el usuario es eliminar el
número en lugar de redefinirlo.

## 3. Verificaciones del candidato de cierre

| Verificación | Resultado |
|---|---|
| Pruebas focales | `PostR1PrivateMetricAuthorityTest`, 6 de 6 (eliminación, un segundo lector, longitud entre dos puntos, documento reabierto, eliminación del lugar, Deshacer y Rehacer, límite de la redefinición). `PostR1LengthDeletionDesktopTest`, 3 de 3 sobre el modelo del autor con la eliminación normal de la aplicación: trabajo de `w_2` de vuelta a su valor, Deshacer, Rehacer, guardado y reapertura, y `n_2`, resultado rico con nombre del documento, conservado. Las dos de Desktop fallan sobre `bb341874b` y pasan con la corrección |
| Regresión compartida completa | 2 536 pruebas, 0 fallos |
| Regresión Desktop completa | 1 573 pruebas, 1 fallo: el histórico de la base `9ccefdcf`, `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`. Ninguna regresión nueva |
| `STATIC` | `verification-443faf5422ea48d5a13ef0d366037c10`, `ACCEPTED / COMPLETE`, código 0 |
| `PHASE -Phase POST-R1-LOCUS` | `verification-6bb2140c1e714c28add05f4ee8d83416`, `ACCEPTED / COMPLETE`, código 0 |
| `INTEGRATION` | `verification-b81359f645654b699a81d85f7473a19d`, `ACCEPTED / COMPLETE`, código 0 |
| `PACKAGING` | `verification-675aa2ec12b248e7b0a9f3a31e105427`, `ACCEPTED / COMPLETE`, código 0 |

Las regresiones completas se ejecutaron sobre `781629cb1`; el candidato añade solo el inventario de pruebas. `FINAL` no se ejecutó.

**Paquetes** en `artifacts/packaging/windows-1.2.2-nc-candidate-4` (carpeta ignorada por Git), construidos desde un árbol limpio del candidato, `source_revision` = `a6562fa30dc7a468b8e62e16843f1ae2f440285b`, perfil NC:

| Paquete | SHA-256 |
|---|---|
| `GeoCeDG-1.2.2-windows-x64-nc.zip` | `d19ba613857d15baa0d5be639df404a488dc324ba4fd1a61a795c13cd1d5086d` |
| `GeoCeDG-1.2.2-windows-x64-nc.msi` | `ac2568077698b5b1f5621793095ab63a6c72a717a823e7004b6a2078966319f6` |
| `GeoCeDG-1.2.2-windows-x64-nc.exe` | `accb437afb6a7503b88cacf04c473e06994a35fea4a122337e2768e84d567ea1` |

Los paquetes de los candidatos anteriores se conservan. Ningún instalador se instaló.

**Humo del ejecutable real** (`GeoCeDG.exe` sin modificar, Java 25.0.4 propio, ventana real, preferencias y almacenes fuera del sistema del autor), modelo del autor, dos arranques. Tiempos por cambio real de `excCyl`, en ms:

| Estado | Tiempos |
|---|---|
| Documento tal como se guardó | 835–990 |
| Con `LocusLength(w_2, P, Q)` y una fórmula | 1 072–1 106 |
| Con la longitud, `Intersect(w_2, segmento)` y un punto sobre su raíz | 1 626–1 725 |
| **Con además `LocusLength(w_2)`** | **6 237–6 390** |
| **Tras eliminarla con `deleteSelectedObjects`** | **1 629–1 683**; resultados ricos de 6 a 5, ninguno sin lector |
| Segundo arranque, documento guardado tras la eliminación | 1 638–1 852; 5 resultados ricos, ninguno sin lector |

Además: `LocusLength(w_2, P, Q)` = 1,3140010177874024, clase `ESTIMATED`, con ruta; tras un cambio real, 1,3103543 y de vuelta; en `excCyl = 4,2` el lugar, la longitud y el punto quedan indefinidos y al volver recuperan valores y token; el segundo arranque lee el token, la longitud y el punto idénticos bit a bit. El app-image no cambió (433 ficheros, mismo resumen).

## 4. Criterio de cierre

| Criterio del autor | Evidencia |
|---|---|
| Preservar toda la funcionalidad de 1.2.1 | Regresiones completas compartida y Desktop sin fallos nuevos; el único fallo es el histórico de la base, visible y sin excluir. Los documentos escritos por `9ccefdcf` (1.2.1) se leen con sus tokens ([informe final](post_r1_1_2_2_final_intervention_report.md)); un documento sin piezas declaradas se escribe como antes; los resultados ricos con nombre del modelo del autor se conservan |
| Capacidades condicionadas de Locus V2 sin asociaciones semánticas incorrectas | C2 resuelto con la alternativa α: un token nombra su raíz o queda sin resolver; nunca pasa a otra por un cambio de lugar ([informe final](post_r1_1_2_2_final_intervention_report.md), §2.1). `LocusLength` condicionado entre dos puntos es un número; la intersección condicionada con punto dependiente funciona y se recupera tras una invalidez temporal |
| Respuesta interactiva utilizable | En el ejecutable real: ≈ 0,9 s por cambio real con el documento tal cual, ≈ 1,1 s con una longitud condicionada, ≈ 1,65 s con longitud, intersección y punto. La longitud total condicionada cuesta ≈ 6,3 s mientras existe y, desde esta corrección, nada después de eliminarla, también tras guardar y reabrir |

## 5. Limitaciones residuales, todas documentadas

- La longitud total de un lugar condicionado es un subtotal indefinido y cuesta ≈ 4,6–6,5 s
  por cambio real en el modelo del autor mientras exista. Ya no deja coste tras eliminarla.
- La redefinición de un número de longitud deja su autoridad anterior sin lector (§2).
- `Intersect` sobre un lugar condicionado añade ≈ 0,55–0,75 s por cambio real.
- Una pieza de dominio gobernada por deslizadores sigue siendo una ranura posicional.
- El objetivo histórico de 1 s de D-V6 no se alcanza: ≈ 0,9–1,1 s por cambio real con el
  documento tal como se guardó.
- Un salto en la dirección de la cuerda no lo detecta el recorrido condicionado.
- El fallo histórico de Desktop `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`
  sigue visible.

## 6. Decisiones pendientes del autor

Aprobación del candidato `a6562fa30` como GeoCeDG 1.2.2; la ejecución de `FINAL`; la
promoción a `main`, la etiqueta `v1.2.2` y la publicación; la prueba de los instaladores en
un entorno aislado. Ninguna se ha realizado.
