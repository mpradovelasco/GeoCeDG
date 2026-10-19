# PRE-G9B-CU0 — Redefinición de puntos (M09) y selección de argumentos en el lienzo (M07) (entregable G)

| Campo | Valor |
|---|---|
| Carácter | Reformulación de requisitos y diseño candidato del paquete `CU0`. No es especificación normativa ni autoriza implementación; `CU7-D` y el diseño previo de `CU6` lo concretarán con su propia aprobación |
| Estado | `CU0 DESIGN CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Encargo | Instrucción autoral de 2026-10-19, §3.5 (DA-5 aprobada: se retira la exigencia de coincidencia) y §3.6 (M07 elevada) |
| Autoridad vigente | ADR 0026 (redefinición avanzada y reemplazo heredado explícito), ADR 0011 (clases de ciclo de vida), ADR 0013, 0008 y 0017 (tokens); `geocedg/specs/spatial/durable-dependency-projection.md`; `locus-v2-public-surface.md` §9, §10 y §20; `geocedg/specs/ui/authoring-memory.md` |
| Evidencia | estudio de código de `CU0` resumido en el [informe del candidato](../validation/pre_g9b_cu0_baseline_candidate_report.md) |

## Parte 1 — M09: redefinir un punto con otra construcción

### 1.1 Requisito reformulado

> El usuario puede redefinir un punto `P` mediante cualquier otra construcción válida,
> incluida una intersección Locus V2 materializable. La posición nueva **no** tiene que
> coincidir con la anterior ni estar próxima a ella. GeoCeDG garantiza un DAG válido,
> sin ciclos; coherencia entre definición y resultado; las reglas de identidad durable;
> un informe de las consecuencias conocidas en los dependientes; la representación
> explícita de la invalidez geométrica que se produzca; y una operación atómica y
> reversible. La coincidencia de coordenadas no se usa nunca como criterio de identidad.

Esto sustituye el enunciado de M09 del plan candidato («La igualdad numérica de
coordenadas puede ser una condición geométrica de admisión»), que queda retirado.

### 1.2 Situación actual

| Caso (`P` creado por una intersección ordinaria) | Hoy |
|---|---|
| `P` sin identidad durable; `P = Intersect(L, obj)` | `Intersect(L, obj)` produce un resultado rico, no un punto. Con dependientes que consumen puntos, la reconstrucción falla («Redefinition failed»); sin dependientes, `P` podría convertirse en resultado rico. Los hermanos de una intersección de varias salidas se eliminan |
| `P` sin identidad durable; `P = Intersect(R, "token")` | reemplazo heredado; sin decisión de identidad (no la había); no exige coincidencia (pendiente de sonda) |
| `P` con identidad durable de una salida; `P = Intersect(L, obj)` | `DURABLE_CONTRACT_CHANGE` sin operación: «Candidate changes the construction-defined geo family» |
| `P` con identidad durable; `P = Intersect(R, "token")` | el proveedor clasifica `FRESH`; solo se ofrece el reemplazo heredado, que retira **y elimina** el cierre durable dependiente y reconstruye los dependientes ordinarios; un paso de deshacer |
| `P` salida de un productor de varias salidas con hermanos sin identidad | falla cerrado antes del análisis, con error genérico |

Restricciones vigentes: el ADR 0026 (decisión 6) prohíbe al núcleo cualquier reasociación
(*rebind*); el ADR 0011 solo admite remapeos en importaciones o pegados de cierre
completo; `locus-v2-public-surface.md` §10 prohíbe reasignar tokens automáticamente; la
relajación de `durable-dependency-projection.md` §9.3 es una condición de parada
declarada que requiere decisión autoral. El informe de impacto del reemplazo heredado
no lista los dependientes ordinarios eliminados en cascada y describe como «no vigentes»
objetos que en realidad se eliminan.

### 1.3 Tres identidades que deben distinguirse

1. **Continuidad del objeto de construcción** que el usuario redefine (`P`: su
   identidad durable, su etiqueta, su papel para los dependientes).
2. **Identidad semántica de la solución concreta de intersección** (token del libro del
   resultado rico `R`). Nunca se transporta desde la definición anterior; si `P` era un
   punto de token de otro resultado `R'`, su reclamación se libera.
3. **Continuidad o sustitución de los dependientes**: siguen dependiendo de `P` y se
   recalculan (aunque queden indefinidos), o se sustituyen explícitamente.

### 1.4 Modos candidatos para `CU7-D`

| Modo | Efecto | Autoridad necesaria |
|---|---|---|
| **M09-A. Cambio de definición con identidad del objeto conservada** | `P` conserva identidad y etiqueta; su definición pasa a `Intersect(R, token)` (o a la construcción elegida); los dependientes siguen unidos a `P` y se recalculan; los que pierdan validez geométrica quedan indefinidos con diagnóstico; el token pertenece al libro de `R` | ampliación del camino `IDENTITY_PRESERVING_CONTRACT_UPDATE` para admitir candidatos públicos (puntos de token y semánticos); enmienda de `durable-dependency-projection.md` §9.3 y del ADR 0026 (distinción entre identidad del objeto de construcción e identidad de la solución) |
| **M09-B. Reemplazo explícito con identidad nueva y remapeo transaccional** | `P` recibe identidad nueva; la operación construye su propia tabla de remapeo de los dependientes hacia el nuevo `P`, con procedencia, dentro de una transacción atómica; el informe lo muestra antes de confirmar | ADR nuevo o enmienda de la decisión 6 del ADR 0026 y del rechazo de remapeos parciales del ADR 0011 |
| **M09-C. Reemplazo heredado** (existe) | identidad nueva; retira y elimina el cierre durable dependiente | ninguna; el informe de impacto debe completarse (§1.6) |

Recomendación técnica: **M09-A** como modo principal, porque es lo que el autor describe
(«cambiar completamente su definición matemática»), conserva el DAG y no transporta
tokens; **M09-C** como alternativa explícita; **M09-B** solo si el autor necesita
sustituciones en las que el objeto redefinido deba considerarse otro objeto.

### 1.5 Flujo de interacción candidato

1. El usuario redefine `P` (diálogo Redefinir, vista algebraica o una acción
   «Redefinir como intersección Locus V2»).
2. Si la nueva definición es `Intersect(L, obj)` (resultado rico), la interfaz pide
   elegir una solución (lista de tokens con marcadores, como el inspector de
   intersecciones) y forma `Intersect(R, token)` dentro de la misma transacción atómica,
   reutilizando `R` si ya existe (sin duplicar el resultado rico).
3. El núcleo evalúa sin mutar: ciclo (`INVALID_DAG` con explicación), orden de
   construcción, grupo de salidas, impacto completo.
4. La interfaz presenta las opciones autorizadas (M09-A / M09-C / Cancelar) y el impacto.
5. Se ejecuta como un único paso de deshacer.

### 1.6 Garantías y reglas

| Exigencia del autor | Regla propuesta |
|---|---|
| DAG válido, sin ciclos | rechazo previo `INVALID_DAG` si `L` u `obj` dependen de `P`; nunca reconstrucción parcial |
| Coherencia definición–resultado | una sola publicación tras la reconstrucción; nada parcialmente modificado si falla (instantánea de entrada y restauración exacta, también para objetos ordinarios: hoy la instantánea ordinaria se toma después de eliminar hermanos) |
| Identidad durable | reglas del §1.3; ninguna coordenada, proximidad, etiqueta ni orden transporta continuidad |
| Consecuencias en dependientes | informe completo antes de confirmar: dependientes que se recalculan, que quedarán indefinidos (si es predecible), que se eliminan (cierre durable y cascada ordinaria) |
| Invalidez geométrica | no es motivo de rechazo si el DAG y la operación son válidos; se representa como indefinido con diagnóstico, con identidad conservada |
| Atomicidad y reversibilidad | `Deshacer` restaura la construcción anterior completa (definiciones, identidades, libros de tokens); `Rehacer` restaura la redefinición; guardar y reabrir reproduce el estado |
| Grupos de varias salidas | decisión G-3: separar `P` de su productor (hoy prohibido), redefinir el grupo completo o mantener el rechazo con mensaje específico |
| Orden de construcción | decisión G-4: si `L` es posterior a `P`, reordenación mínima (P3-R1 ampliado) o rechazo explicado |

### 1.7 Alcance de `CU7-D`

ADR (enmienda del 0026 o nuevo), enmiendas de `durable-dependency-projection.md` y de
`locus-v2-public-surface.md` §20, diseño del diálogo de elección y del informe de impacto
completo, sondas previas (redefinir un punto ordinario sin identidad como
`Intersect(R, "token")`; volver de un punto de token a uno ordinario), y la matriz de
pruebas de `CU7-I`: modos A y C, ciclo, grupo de varias salidas, reordenación,
dependientes que quedan indefinidos, deshacer/rehacer exactos, guardar/reabrir, copia y
ausencia de cualquier criterio de coincidencia.

## Parte 2 — M07: nombres de objetos del lienzo en los diálogos

### 2.1 Requisito

Mientras un diálogo pide argumentos, el usuario identifica gráficamente un objeto y su
nombre (referencia válida para el analizador) se inserta en la posición del cursor del
campo activo, sin abandonar el diálogo, conservando el texto, admitiendo selecciones
sucesivas, sin ejecutar herramientas sobre el lienzo, sin modificar la construcción antes
de confirmar y con `Escape`, `Cancelar` y `Deshacer` correctos.

### 2.2 Situación actual

- El único mecanismo es el heredado de `InputDialogD`: cuando un diálogo **no modal**
  recibe el foco, la aplicación entra en el modo `MODE_SELECTION_LISTENER` y un clic en
  el lienzo (o en la vista algebraica) inserta `" etiqueta "` reemplazando la selección
  de texto. Lo usan Redefinir, Renombrar y Texto.
- Los diálogos de herramienta Radio, Ángulo dado, Rotar, Dilatar, Segmento dado y
  Polígono regular son no modales pero **desactivan** la selección: un clic en el lienzo
  ejecuta la herramienta activa.
- Todos los diálogos GeoCeDG que piden valores son `JOptionPane` **modales**: parámetro
  del punto semántico, ángulo o factor de semejanza de Locus V2, grado de SplineV2.
- Riesgos del mecanismo heredado: un clic puede alternar una casilla o ejecutar un guion;
  un doble clic abre otro diálogo Redefinir; la selección queda global; entrar y salir
  del modo cuenta como cambio de herramienta en la memoria de autoría F2-R1 y al cerrar
  siempre se vuelve a Mover; tras `Deshacer` el diálogo conserva el objeto antiguo; no
  hay pruebas de esta función.

### 2.3 Solución candidata recomendada: sesión de selección propiedad del diálogo

Se combinan las alternativas 2 y 3 del encargo:

1. **Botón «Seleccionar en el lienzo»** junto a cada campo que admite referencias, y un
   atajo de teclado equivalente.
2. Al pulsarlo se abre una **sesión de selección de argumento** en el controlador
   GeoCeDG del lienzo (mismo patrón que la puerta de aislamiento de la inserción):
   - el siguiente clic se resuelve con la prueba de impacto habitual; si hay varios
     candidatos se ofrece el selector existente;
   - el controlador consume todos los demás eventos: no se ejecuta la herramienta
     activa, no se arrastra, no se alternan casillas ni se ejecutan guiones;
   - no se pasa por `App.setMode`, de modo que la memoria de autoría y la herramienta
     activa no cambian;
   - la vista algebraica también puede aportar el objeto (objetos ocultos).
3. Se inserta la referencia en la posición del cursor del campo (regla de selección:
   decisión M07-a) con el mismo formato que `InputDialogD` (etiqueta tal cual, con
   espacios separadores; los tokens de intersección como texto entrecomillado).
4. La sesión admite **selecciones sucesivas** hasta pulsar `Escape` o volver al campo;
   el primer `Escape` termina la selección, el segundo cancela el diálogo.
5. Los diálogos GeoCeDG pasan de `JOptionPane` modal a `JDialog` **no modal** con un
   **bucle secundario** (`EventQueue.createSecondaryLoop`) para que el código que hoy
   espera una respuesta síncrona siga funcionando, mientras el marco principal recibe
   los eventos que filtra la sesión.
6. En los seis diálogos de herramienta heredados, la sesión sustituye a la selección
   desactivada: el clic aporta el nombre en vez de ejecutar la herramienta.
7. Antes de confirmar, el diálogo comprueba la vigencia de sus referencias (tras un
   `Deshacer` o una reconstrucción los objetos se resuelven de nuevo por etiqueta en el
   momento de confirmar; si ya no existen, el diálogo lo indica y no crea objetos
   nuevos por error).

Alternativas descartadas como solución principal: hacer no modales los diálogos con el
mecanismo heredado tal cual (hereda todos los riesgos del §2.2) y suspender y restaurar
el diálogo (parpadeo de ventanas, restauración de todo el estado no textual, y
`JOptionPane` no se puede suspender).

### 2.4 Decisiones previas al diseño de `CU6`

| Id | Cuestión | Recomendación |
|---|---|---|
| M07-a | Selección de texto existente al insertar: reemplazarla o insertar en el cursor | reemplazar la selección y, si no hay selección, insertar en el cursor (comportamiento estándar de edición) |
| M07-b | Diálogos cubiertos en la primera entrega | los tres diálogos GeoCeDG de valores, los seis de herramienta heredados y Redefinir, Renombrar y Texto (estos ya tienen el mecanismo heredado; se migran a la sesión nueva) |
| M07-c | `Deshacer` con un diálogo abierto | desactivado mientras haya una sesión de selección activa; con el diálogo abierto y sin sesión, permitido con revalidación al confirmar |
| M07-d | Gráficos 2 y 3D | Gráficos 1 y vista algebraica en la primera entrega |

### 2.5 Validación prevista

Con el arnés ventaneado de `CU0`: inserción en el cursor con texto previo y con selección;
varias selecciones seguidas; clic sobre objetos superpuestos; casillas y botones con guion
no se activan; la herramienta activa no se ejecuta; `Escape` por niveles; `Cancelar` no
cambia nada; `Deshacer` según M07-c; foco devuelto al campo; ninguna modificación de la
construcción hasta confirmar.
