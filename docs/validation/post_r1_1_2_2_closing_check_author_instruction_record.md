# GeoCeDG 1.2.2 — registro de la instrucción autoral de comprobación de cierre

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de esta instrucción. No aprueba ninguna implementación, no declara `PASS — AUTHOR APPROVED` y no autoriza publicar. `selfApproved=false` |
| Fecha | 22 de noviembre de 2026 |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| Candidato recibido | `bb341874b5ada13aebce89c3aec4f7f998b26e16`, con el [informe final](post_r1_1_2_2_final_intervention_report.md) |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`; etiquetas de versión `v1.2.0` y `v1.2.1` |

Este registro transcribe y ordena la instrucción del autor; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Qué decide

- **Acepta provisionalmente** la recomendación de aceptación técnica del candidato
  `bb341874b`, con sus limitaciones documentadas. La corrección C2 y el rendimiento
  alcanzado se consideran suficientes para el objetivo GeoCeDG 1.2.2.
- **No autoriza** nuevas optimizaciones geométricas, modificaciones de la política de
  aislamiento ni ampliaciones funcionales. No hacer admisible la longitud total condicionada
  ni optimizar su cálculo en esta versión. No abrir más objetivos.

## 2. Comprobación focal pedida antes del cierre

Una única comprobación del ciclo de vida de `LocusLength(w_2)`:

1. Crear el resultado numérico de longitud total y comprobar su coste.
2. Eliminarlo mediante la interfaz normal de GeoCeDG.
3. Comprobar si permanece algún resultado rico auxiliar y si existen consumidores legítimos.
4. Verificar que, sin consumidores, se libera el cálculo y se recupera el rendimiento
   anterior.
5. Comprobar guardado, reapertura y ausencia de regresiones.

«Si existe un resultado auxiliar huérfano, autoriza únicamente su corrección mínima y las
pruebas correspondientes. Si su permanencia responde al contrato vigente, documenta el
comportamiento sin alterar su semántica.»

## 3. Cierre

Resuelta la comprobación, preparar el cierre de GeoCeDG 1.2.2 y presentar la evidencia final
para la aprobación del autor. Sin ejecutar `FINAL`, sin promoción a `main`, sin etiqueta y
sin publicación hasta nueva autorización expresa.

Criterio de cierre: preservar toda la funcionalidad de 1.2.1, incorporar las capacidades
condicionadas de Locus V2 sin asociaciones semánticas incorrectas y mantener una respuesta
interactiva utilizable.
