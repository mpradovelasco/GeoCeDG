# Uso condicionado de Locus V2 — hoja de decisiones autorales (bloque 3A)

**PENDIENTE DE DECISIÓN AUTORAL.** `selfApproved=false`. Esta hoja no aprueba nada: recoge
la recomendación y las únicas decisiones que corresponden al autor. Las cuestiones técnicas
están resueltas en el diseño con código, medidas o experimentos y no se preguntan aquí.

## 1. Qué se somete a aprobación

Paquete del bloque 3A, en la rama `maintenance/post-r1-locus-repairs-and-conditioned-use`:

| Documento | Papel |
|---|---|
| [ADR 0042](../adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md) | decisión de arquitectura propuesta |
| [Contrato candidato](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md) | texto normativo candidato y enmiendas que su adopción requeriría |
| [Diseño](../architecture/post_r1_locus_conditioned_use_design.md) | inspección de capacidades, APIs, ciclo de vida, matriz de ejemplos, rendimiento, K1/K2, banco adversarial y plan 3B |

La identidad exacta del paquete (commit y árbol) consta en el
[registro de progreso](post_r1_locus_repairs_progress.md), checkpoint del bloque 3A. Una
aprobación debe citar ese commit.

Motivo normativo de la pausa: AGENTS §3.1 exige una especificación o ADR aprobados para
modificar las áreas de código afectadas y §9 exige el diseño antes de la implementación.
V modifica contratos de admisibilidad, identidad y evidencia que hoy no están fijados
(`locus-v2-existence-components.md` §15.1, §15.6 y §15.8). Las reparaciones y el modelo de
los bloques 1 y 2 no dependen de esta decisión y no se revierten ni se congelan.

## 2. Recomendación

Aprobar el contrato candidato como base normativa y autorizar los incrementos **I0 a I3**
del plan 3B: evidencia condicionada y testimonio de evaluación en el kernel, dirección por
parámetro con `Point(L, u)`, presentación y longitudes. Con ellos, un locus fiel sin
componente certificada se dibuja como curva y admite puntos y longitudes con grado
declarado, que es el criterio expresado por el autor el 2026-11-21. Intersecciones locales
y locus dependiente (I4), exportación (I5) y la promoción K2 (I6) quedan como incrementos
siguientes, cada uno con su autorización. K1 no es condición de cierre de V.

Qué permite y qué no:

- Permite usar una región donde la evaluación fiel es válida y la selección de raíces está
  establecida en cada nodo, bajo cuatro hipótesis declaradas entre nodos (validez,
  selección, continuidad y resolución).
- No certifica, no promueve por densidad y no detecta todo evento: dos eventos entre nodos,
  un intercambio de raíces que regresa o una transición muy estrecha pueden pasar
  inadvertidos. El grado lo dice, y solo una promoción por prueba (K2, K1) lo elimina.
- Los consumidores que exigen componente certificada, `COMPLETE` o `STRICT` siguen fallando
  cerrado. La autoridad del documento no cambia.

## 3. Decisiones irreductibles

### D-V1 — Adoptar el contrato de uso condicionado

Aprobar o no el ADR 0042 y el contrato candidato como base normativa, con las enmiendas de
su §12 a redactar sobre las especificaciones al implementar cada incremento.

- **Sí** (recomendado): abre I0–I3.
- **No**: los loci fieles sin componente certificada siguen como hoy (evidencia
  exploratoria punteada, sin consumidores) hasta disponer de certificación K1/K2.

### D-V2 — Compatibilidad de la dirección por parámetro

Un punto sobre una región condicionada necesita persistir una dirección sin selector de
componente, en un códec nuevo. Un documento que la contenga, abierto con GeoCeDG 1.2.1 o
anterior, mostrará ese punto indefinido con una causa tipada y leerá el resto. Ningún
documento existente cambia de resultado.

- **Aceptar ese límite** (recomendado): es la única forma de no asignar identidad a la malla
  ni reinterpretar selectores antiguos.
- **No aceptarlo**: `Point(L, u)` sobre regiones condicionadas no puede persistirse y V se
  reduce a dibujo y a medidas no persistidas.

### D-V3 — Convención de presentación

Cómo se distingue a la vista y en el texto un trazo condicionado de uno certificado y de las
marcas exploratorias. Propuesta: trazo continuo del color del locus con patrón de raya
larga, bandas sin unir, y texto de estado «trazado condicionado (no certificado)» con las
hipótesis a petición; «completo» reservado al valor certificado. Es una decisión de
producto: el autor puede fijar otra convención sin alterar el contrato.

### D-V4 — Alcance de la exportación

- **Primera versión sin exportación semántica** (recomendado): las exportaciones de imagen
  de la vista exportan lo dibujado; DXF y LaTeX semántico omiten las regiones condicionadas
  con diagnóstico, hasta el incremento I5.
- **Exportación semántica desde el principio**: exige definir en DXF y LaTeX el transporte
  de la clase de fidelidad antes de I2.

## 4. Punto de reanudación

Tras la decisión, este mismo encargo se reanuda en 3B sin repetir las etapas cerradas:
verificar la aprobación y el commit que cubre, comprobar si la base ha cambiado y empezar
por I0. Si la decisión modifica el contrato, se entrega primero el texto corregido.
