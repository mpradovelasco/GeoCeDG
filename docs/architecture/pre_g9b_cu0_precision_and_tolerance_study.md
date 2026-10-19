# PRE-G9B-CU0 — Precisión, umbrales y control por el usuario (entregable H)

| Campo | Valor |
|---|---|
| Carácter | Estudio breve del paquete `CU0`. No implementa controles nuevos ni cambia tolerancias normativas |
| Estado | `CU0 DESIGN CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral de 2026-10-19, §3.4 y «H02 — Tolerancias de intersección» (§4) |
| Autoridad vigente | `locus-v2-intersections.md` §8, `locus-v2-metrics.md` §10 y §15, `locus-v2-existence-components.md`, ADR 0013 (persistencia de políticas), `AGENTS.md` §1.8 (sin aproximación silenciosa) y §11.4–§11.5 |
| Evidencia | inventario de constantes de `CU0` (resumido en el [informe del candidato](../validation/pre_g9b_cu0_baseline_candidate_report.md)); [línea base CU0](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json) (`pins.H02`, `pins.H10_H11`, `preserve.o_1_total_length_independent`) |

## 1. Umbrales y presupuestos actuales

| Subsistema | Valor vigente | Escala | Normativo |
|---|---|---|---|
| Residuo de intersección | `2·10⁻¹² + 2·10⁻¹²·max(1, S)` | `S = 1` para recta, segmento, rayo, gráfica de función e implícita polinómica (≈ `4·10⁻¹²` absoluto); `max(1, r)` circunferencia; `max(1, semieje)` cónica; `max(1, ‖F‖, ‖Q‖)` pares de loci (distancia al origen) | §8.4 da los valores; no la regla de combinación |
| Corchete de raíz | `10⁻¹²` absoluto en unidades del parámetro | no acoplado al residuo | §8 |
| Tangencia | `|n̂·t̂| ≤ 10⁻¹⁰` sobre una tangente por diferencias finitas (paso `max(16·10⁻¹², 32·ulp(t), 10⁻⁶·span)`) | — | §8.3 (indicador documentado) |
| Deduplicación / continuación / coordenada | `4·10⁻¹²` / `10⁻⁸` / `4·10⁻¹²` | absolutos | §8.4 |
| Presupuesto de intersección | 32 768 evaluaciones, 8 192 subdivisiones, profundidad 40, 80 iteraciones, 512 candidatos | — | implementación |
| Métrica | `max(10⁻¹⁰, 10⁻⁹·S)` por nodo; presupuesto 32 768 evaluaciones, 16 384 subdivisiones, profundidad 22 **por llamada de integración** | `S` longitud local | §15 |
| Existencia puntual | umbrales absolutos heredados de GeoGebra (`10⁻⁸`, `10⁻⁵`) | absolutos | no |
| Certificación de existencia dinámica | 512 celdas fijas (única familia) | — | no |
| Épsilon de dominio | `10⁻¹²` absoluto, parte de la firma del proveedor | — | G6 |
| Dibujo | 0,75 px, profundidad 2–12, retroceso 32–512 muestras | píxeles (correcto: solo presentación) | documentación de arquitectura |
| Interacción de punto | radio de captura 3 px (ratón) × escala inversa | vista (única decisión semántica que lee la vista) | G9U1 |

No existe **ningún control** de tolerancia o precisión por comando, documento o
preferencia para operaciones semánticas Locus V2: cada recálculo reconstruye las
políticas desde constantes. Solo los diálogos de exportación (LaTeX, DXF) aceptan una
tolerancia, por invocación. La persistencia de la política que exige el ADR 0013 no está
implementada.

## 2. Influencia en los fallos encontrados

| Fallo | Mecanismo numérico | Evidencia |
|---|---|---|
| **H02** raíces transversales perdidas a escala | residuo absoluto `4·10⁻¹²` para objetivos de escala unidad y corchete absoluto `10⁻¹²`: tras la bisección el residuo vale ≈ `|dh/dt|·Δt`, de modo que una raíz regular solo se acepta si `|dh/dt| ≲ 4–8` | `pins.H02`: 2/2 admisibles con `r = 2`; 0/2 con `r = 200`; ninguna publicada con `r = 2000` |
| **B08/B09** clasificación incoherente | umbral `10⁻¹⁰` sobre tangentes por diferencias finitas, cuyo ruido es de ese orden: contactos estructuralmente equivalentes salen `TANGENT_ESTABLISHED` o `CONTACT_UNDETERMINED`. La no materialización se debe al contrato (solo transversales), no al umbral | `pins.B08_B09` |
| **H10** (nuevo) longitud aliasada | la métrica de un `LocusV2` general refina un polígono de cuerdas sin profundidad mínima y acepta tres muestras alineadas con error cero | `pins.H10_H11`: `y = sin(2πkx)` en `[0,1]` da longitud **1,0** para `k = 1, 2, 4`; las longitudes reales son 4,1883, 8,1118 y 16,0647. El valor es escalar admisible y no avisa |
| **H11** (nuevo) presupuesto agotado a escala moderada | el umbral absoluto `10⁻¹⁰` por nodo domina y el presupuesto de 32 768 evaluaciones se agota | `pins.H10_H11`: circunferencia de radio 7 medida; **radio 8 y mayores: longitud indefinida** (`LIMIT_NOT_ESTABLISHED`) |
| **H12** (nuevo) precisión menor que la declarada | sesgo de la suma de cuerdas ≈ `6–8·10⁻⁹` relativo frente a `10⁻⁹` declarado; la estimación de error no se compara con la tolerancia para la admisibilidad | `o_1`: `13,2364580874` frente a la referencia independiente `13,2364581958` (diferencia `1,1·10⁻⁷`); circunferencias `r = 1, 5, 7`: error relativo ≈ `6·10⁻⁹` |
| **B07/H01** | no es un umbral: certificado de compatibilidad y puerta de publicación (propuesta E) | `pins.B07`, `pins.H01` |
| **B06** | criterio de dibujo (profundidad mínima 2, un punto medio, sin criterio angular) | `pins.B06` |

H10 es especialmente grave para CeDG: los desarrollos de secciones de cilindros producen
precisamente curvas sinusoidales, y un `LocusLength` que alimente otro locus (como `w_1`
alimenta `e_2`) propagaría un valor erróneo sin diagnóstico. En el modelo del autor la
longitud de `o_1` es correcta; la cadena de `e_2` estaría expuesta a H11 si el autor
aumentara la escala (un perímetro mayor que ≈ 44 unidades agota el presupuesto).

## 3. ¿Conviene hacerlos configurables?

Primero hay que separar dos cosas que la instrucción distingue:

| Esfuerzo o precisión del cálculo (configurable con criterio) | Evidencia necesaria para un resultado (no configurable) |
|---|---|
| presupuestos de evaluación, subdivisión y profundidad | qué constituye un certificado de existencia, aislamiento o contacto |
| precisión solicitada de una métrica | la normalización del residuo y su escala característica |
| profundidad máxima de certificación por intervalos | la identidad de raíces, componentes y puntos |
| nivel de diagnóstico | la tolerancia que separa «incidente» de «no incidente» una vez declarada en el contrato |

Conclusiones:

1. **La mayoría de los fallos no se arreglan con un control de usuario, sino corrigiendo
   las políticas por defecto**: normalización relativa a la escala (H02), corchete
   acoplado al residuo, tolerancia métrica relativa repartida por nodos y profundidad
   mínima (H10–H12), presupuesto proporcional a la escala relativa (H11). Ofrecer un
   umbral configurable sin estas correcciones solo trasladaría el problema al usuario.
2. **Muchos parámetros independientes** dificultarían la reproducibilidad y la
   explicación, y aumentarían las combinaciones que deben validarse.
3. **Un control razonable es un perfil de esfuerzo** a nivel de documento, con pocos
   valores, que nunca cambie la definición de certificado: un perfil más exigente solo
   puede conseguir *más* evidencia (más raíces aisladas, envolventes más estrechas,
   métricas completadas), nunca rebajar el requisito.

## 4. Consecuencias para determinismo, identidad, persistencia y cachés

| Aspecto | Regla propuesta |
|---|---|
| Determinismo | el perfil es estado del documento y entra en la identidad de política de cada resultado; mismo documento y perfil ⇒ mismos resultados |
| Identidad | el perfil no altera la normalización del residuo, el texto de contrato del objetivo ni el germen: los tokens ya emitidos siguen válidos; un perfil puede hacer resolubles raíces antes no resueltas (tokens nuevos) |
| Persistencia | elemento versionado en el `.cedg` (por ejemplo `<geocedgPrecision profile="standard" version="1"/>`); los documentos antiguos se abren con el perfil estándar; esto cumple además el requisito pendiente del ADR 0013 |
| Cachés | el perfil forma parte de la clave del índice métrico y de los cachés de intersección |
| Revisiones | cambiar de perfil es una acción explícita, con deshacer, que avanza la revisión de los resultados afectados |
| Vista | ninguna tolerancia semántica depende del zoom, de los píxeles, del DPI ni de la vista; solo el dibujo |

Normalización geométrica propuesta para H02 (enmienda de `locus-v2-intersections.md`
§8): residuo dividido por una **escala característica invariante por traslación**, la
mayor de la extensión de la imagen de la componente del locus y la del objetivo acotado
(o la del locus si el objetivo no es acotado), con suelo absoluto para escalas
degeneradas; parada de la bisección por residuo (`Δt ≤ τ_res / |h'|`) y no por un
corchete absoluto; deduplicación relativa. Para pares de loci, sustituir la distancia al
origen por la misma escala característica (hoy contradice `locus-v2-semantics.md`).

## 5. Propuesta mínima

| Paso | Contenido | Paquete |
|---|---|---|
| H-a | Corregir la métrica: profundidad mínima y control de consistencia (Richardson o comparación de niveles) antes de aceptar; tolerancia relativa repartida por nodos; presupuesto coherente con la tolerancia relativa; admisibilidad escalar condicionada a que el error estimado cumpla la tolerancia declarada | `CU1` (conformidad con `locus-v2-metrics.md` §10 y §15 y con `AGENTS.md` §1.8) |
| H-b | Normalización de residuo invariante por traslación y escala (H02), corchete acoplado y diagnósticos de rechazo efectivamente emitidos (`RESIDUAL_REJECTED` y afines, hoy declarados pero nunca emitidos) | `CU3` |
| H-c | Clasificación de contacto por punto crítico (propuesta F) en lugar del umbral `10⁻¹⁰` sobre tangentes aproximadas | `CU3` |
| H-d (opcional) | Perfil de esfuerzo de documento `STANDARD` / `HIGH` / `EXHAUSTIVE`, persistido y versionado, que solo cambia presupuestos, profundidad de certificación, precisión métrica solicitada y nivel de diagnóstico; con acción explícita «reintentar con más esfuerzo» desde el inspector cuando un resultado quede en `WORK_LIMIT_REACHED` o `LIMIT_NOT_ESTABLISHED` | decisión H-1; después de `CU3` |

No se implementa ningún control nuevo en `CU0`.
