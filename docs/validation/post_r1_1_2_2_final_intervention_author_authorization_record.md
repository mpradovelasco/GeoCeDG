# GeoCeDG 1.2.2 — registro de la autorización autoral de la intervención correctiva final

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. Es la única autoridad del estado de la autorización de la intervención correctiva final. No aprueba ninguna implementación, no declara `PASS — AUTHOR APPROVED` y no autoriza publicar. `selfApproved=false` |
| Instrucción | «GeoCeDG 1.2.2 — Autorización de intervención correctiva final y preparación del cierre», decisión del autor de 22 de noviembre de 2026: **ÚLTIMA INTERVENCIÓN CORRECTIVA ACOTADA, AUTORIZADA** |
| Repositorio | `mpradovelasco/GeoCeDG` |
| Rama | `maintenance/geocedg-1.2.2-post-r1`, local y sin publicar |
| Candidato de partida | `a4b03d049eeccbc7ad8c48d7e359220d2545d23b` |
| Último registro comunicado | `27e5b803b` |
| `main` | local y `origin/main` en `9ccefdcfe922d8c0e329bdeec30c12fc058de463`; etiquetas de versión `v1.2.0` y `v1.2.1` |
| Versión objetivo y fecha | GeoCeDG 1.2.2, `2026-11-22` |
| Registro que la origina | [paso C2 detenido](post_r1_1_2_2_c2_pair_identity_contradiction_record.md) y el cierre parcial del [registro de progreso](post_r1_locus_repairs_progress.md), checkpoint 10 |

Este registro transcribe y ordena la instrucción del autor; ante cualquier diferencia
prevalece la instrucción autoral.

## 1. Qué autoriza

Una última intervención correctiva acotada, limitada a:

1. Resolver C2, evitando asociaciones semánticas incorrectas de tokens en intersecciones de
   loci multicomponente.
2. Diagnosticar y mejorar el rendimiento de `Intersect` sobre loci condicionados,
   especialmente el sobrecoste observado en `Intersect(w_2, segmento)`.

Completada y validada, el objetivo es cerrar GeoCeDG 1.2.2 sin nuevas funcionalidades ni
nuevas líneas de desarrollo. No se autoriza un rediseño general de Locus V2, del kernel, de
la persistencia ni del sistema de dependencias. Se preservan las implementaciones y
contratos aprobados de I0–I4T, C0, C1, C3, C4 y C5.

## 2. C2 — identidad de componentes y piezas declaradas

Se autoriza la **solución α**, identidad declarativa, exclusivamente en el alcance mínimo
necesario para corregir los defectos demostrados.

- **Distinciones.** Pieza declarada (porción de la definición paramétrica), componente
  geométrica (entidad conexa cuya existencia y continuidad establece su contrato), ranura
  posicional (posición con significado constructivo declarado dentro de una estructura
  dinámica) y raíz de intersección (su identidad no depende de su posición numérica o
  visual). «La identidad durable de una pieza constructiva no debe depender de su índice de
  enumeración. Sin embargo, debe preservarse la semántica posicional de las ranuras aprobada
  en D2. Un cambio de contenido en una ranura posicional no constituye automáticamente un
  defecto de identidad.»
- **Requisitos.** Reutilizar las identidades constructivas durables existentes siempre que
  distingan inequívocamente las piezas; introducir nuevas solo cuando sean imprescindibles y
  con ciclo de vida y persistencia bien definidos; no usar índices, coordenadas, proximidad,
  orden de enumeración, referencias Java ni historia accidental; preservar los tokens y
  bindings heredados por correspondencia semántica demostrable; ante correspondencia
  ambigua, conservar la identidad y dejar el punto temporalmente indefinido, sin
  reasignarlo; respetar las reglas de redefine, copy, Undo/Redo y reapertura; no modificar
  la semántica general de las listas ni a los demás consumidores de ranuras posicionales.
- **Invariante obligatorio.** «Un token puede seguir identificando su raíz semántica o
  quedar temporalmente sin resolver, pero no puede reasignarse silenciosamente a otra raíz
  por un cambio accidental de ordinal.»
- **Estrategia.** Revisar la contradicción documentada, contrastar con D2, E-3, ADR
  0021/0028, la persistencia y el código; separar el cambio legítimo de una ranura, la
  reordenación de piezas con identidad constructiva persistente, la sustitución real de una
  pieza y el cambio topológico que impide resolver un token; implementar solo lo necesario
  y sus regresiones; no promover el trabajo de `wip/post-3b-c2-structural-pair-lineage` sin
  revisarlo; si exigiera una reforma arquitectónica considerable, detener C2 y presentar una
  alternativa de fallo cerrado.

## 3. Rendimiento de `Intersect` sobre loci condicionados

- **Objetivo.** Eliminar o reducir sustancialmente el sobrecoste, sin comprometer las
  garantías de Locus V2, actuando sobre el mecanismo responsable del coste.
- **Procedimiento.** Reproducir con perfil focal; separar evaluación geométrica, aislamiento
  de raíces, evidencia condicionada, búsquedas repetidas, publicación y consumidores
  dependientes; identificar la causa dominante por instrumentación, no solo por estimación;
  revisar cálculos repetidos en la misma revisión y resultados inmutables reutilizables;
  aplicar la corrección menos invasiva que reduzca el coste demostrado; comparar con el
  procedimiento anterior como oráculo, incluidos casos adversos.
- **Límites.** Se permite reutilizar la memoización ya desarrollada cuando sea
  semánticamente compatible. Sin cachés globales, sin compartir objetos geométricos
  mutables, sin relajar aislamiento, pertenencia, admisibilidad o certificación. Los
  presupuestos, la identidad de los resultados y la propagación por el DAG permanecen
  correctos.
- **Criterio.** No se exige un tiempo universal inferior a un segundo. El rendimiento D-V6
  alcanzado con C5 se acepta como referencia provisional y **no se autoriza implementar C6**
  solo para el objetivo histórico de un segundo. Para `Intersect` se requiere una mejora
  material sobre los unos 6,5 s comunicados, medida en mediana y p95, con y sin el
  consumidor, en la misma configuración. Si no se consigue sin modificar contratos
  fundamentales, se presenta la limitación cuantificada y la causa, sin investigación
  indefinida.

## 4. Preservación, validación y cierre

- La intervención no degrada las capacidades de 1.2.1 ni las de 1.2.2. No se modifican
  pruebas para acomodar un resultado incorrecto; los cambios de expectativa se justifican
  contra los contratos.
- Primero las pruebas focales de C2 y de `Intersect`; después las regresiones compartida y
  Desktop, y `STATIC`, `PHASE POST-R1-LOCUS`, `INTEGRATION` y `PACKAGING` sobre el candidato
  final. Humo del ejecutable real con el modelo del autor: apertura, cambios de parámetros,
  `LocusLength` condicionado definido, intersección condicionada con punto dependiente,
  guardado, reapertura y recuperación de identidades, y recuperación tras invalidez
  temporal.
- No instalar MSI ni EXE sobre la instalación de trabajo.
- Documentar los fallos históricos diferenciándolos de cualquier regresión nueva; conservar
  la trazabilidad; preparar **un único candidato final** y un informe final conciso con
  recomendación explícita de aceptación o rechazo técnico.

## 5. Qué no autoriza

Abrir nuevos objetivos del roadmap; nuevas capacidades geométricas ajenas a C2 o
`Intersect`; reabrir el diseño de Locus V2; rediseñar listas, persistencia o DAG fuera de lo
indispensable; implementar C6 por rendimiento; modificaciones normativas adicionales;
ejecutar `FINAL`; promover a `main`; crear `v1.2.2`; publicar una release; declarar
`PASS — AUTHOR APPROVED`. Un defecto ajeno al alcance se documenta y se evalúa como
posible regresión bloqueante, sin iniciar otra línea de desarrollo.

## 6. Criterio de cierre de 1.2.2

Conservar la funcionalidad de 1.2.1, añadir las capacidades condicionadas sin asociaciones
semánticas incorrectas y con respuesta interactiva utilizable: sin regresiones nuevas
conocidas, identidad correcta o fallo cerrado explícito, consumidores principales de loci
condicionados en funcionamiento, mejora reproducible de `Intersect` sin degradar garantías,
validación reproducible y limitaciones residuales documentadas. La aprobación del autor,
`FINAL` y la publicación se deciden por separado a partir del informe final.
