# PRE-G9B-CU3 — Registro del smoke autoral de `CU3-A` y de la prioridad de rendimiento

| Campo | Valor |
|---|---|
| Carácter | Transcripción de la instrucción autoral «PRE-G9B-CU3 — Resultado del smoke autoral y prioridad de rendimiento» (2026-10-26) y registro de su ejecución. No es un registro de cierre: `CU3-A` no está `PASS — AUTHOR APPROVED` |
| Candidato | técnico `3c2ca7e800001a19b96b686f1adb8fcc1866c796`; el producto probado es el suyo |
| Guía | [guía de prueba manual](pre_g9b_cu3_manual_test_guide.md) |
| `selfApproved` | `false` |

## 1. Resultado del smoke

> He realizado el smoke manual de CU3-A. Resultado autoral: funcionalmente correcto, rendimiento
> interactivo no aceptado. Las funcionalidades comprobadas se comportan correctamente, pero el
> arrastre de `H` y `B` es demasiado lento para el uso normal de GeoCeDG. No declaro todavía CU3-A
> `PASS — AUTHOR APPROVED`.

| Aspecto | Estado |
|---|---|
| Smoke funcional de `CU3-A` | `PASS — AUTHOR REPORTED` (2026-10-26) |
| Rendimiento interactivo (arrastres de `H` y `B`) | `NOT ACCEPTED` |
| Cierre de `CU3-A` | pendiente de decisión autoral |

El smoke ya no está pendiente en su conjunto, y no hay `PASS` final mientras el rendimiento no se
resuelva. La aceptación provisional de los tiempos como limitación del candidato (disposición
autoral §5) queda sustituida por este resultado: el rendimiento interactivo no se acepta.

El autor no detalla los pasos de la guía uno por uno; este registro no infiere más evidencia que
la declarada.

## 2. Publicación de la caracterización

> Autorizo publicar los commits locales `451c1b50` […] y `7f1a81ae` […]. Utilizar push normal y
> avance rápido, sin forzar, sin etiquetas, sin PR y sin modificar `main`. Verificar las identidades
> Git y el estado de las referencias remotas antes y después.

| Comprobación | Resultado |
|---|---|
| Antes | rama remota en `735c55c6`, antecesor de `7f1a81ae`; por publicar exactamente `451c1b501f7b010f279b0a1c9af34202c9913af4` y `7f1a81ae2906a09ae41394a56bbe7ba4032df255`; árbol local limpio; ninguna etiqueta en `7f1a81ae`; ninguna PR de la rama |
| Push | `git push --no-follow-tags origin 7f1a81ae…:refs/heads/phase/pre-g9b-cu3-implementation`: `735c55c6b..7f1a81ae2`, avance rápido, sin avisos de reglas omitidas |
| Después | solo cambió la referencia de la rama, a `7f1a81ae`. `main` sigue en `e6c908f2`, las 73 referencias de etiqueta son idénticas, siguen siendo 13 ramas y no hay PR |
| Auditoría | `artifacts/agent/pre-g9b-cu3-characterization-publication-audit/publication-audit.json`, ignorado por Git (SHA-256 `036d50781278aa40421caf46a6cb34590af69bbcae987f174e7bda1ee00a215d`) |

Los commits posteriores a `7f1a81ae`, empezando por el que añade este registro, son locales y no se
han publicado.

## 3. Prioridad de trabajo y verificación del rendimiento

El autor fija como siguiente actividad productiva que desea considerar una optimización focal del
rendimiento de la autoridad constructiva:

- **Objetivo:** reducir sustancialmente los tiempos de refresco de `H` y `B`, preservando
  íntegramente las garantías científicas de E1 a E3.
- **Prioridad:** el coste de `ConstructiveIdentityCertifier.recheckCell()` y de la evaluación
  exacta de las condiciones de signo.
- **Técnicas que evaluar:** la especialización exacta de las indeterminadas invariantes; la
  reutilización rigurosa de potencias y subexpresiones; la memoria por revisión con claves
  completas; y la reutilización de vecindades certificadas rigurosamente.
- **Verificación:**
  - comparar el candidato congelado con la versión optimizada en la misma estación y
    configuración;
  - medir por separado los arrastres de `H` y de `B`, las comprobaciones del certificado, las
    envolventes y las demás operaciones relevantes;
  - conservar los contadores deterministas, los límites de presupuesto, los resultados de
    integridad y el comportamiento en las degeneraciones.
- **Objetivo orientativo:** acercarse a los tiempos de la política heredada, idealmente por debajo
  de 100 ms en las actualizaciones habituales. La aceptación definitiva es autoral.
- **Prohibiciones:** ninguna optimización puede admitir evidencia obsoleta, modificar identidades ni
  sustituir pruebas matemáticas por tolerancias.

La publicación de la caracterización no autoriza por sí sola cambios productivos: requieren una
disposición específica tras revisar las propuestas. El
[plan de optimización focal](../architecture/pre_g9b_cu3_performance_optimization_plan.md) las
concreta para esa disposición. No se ha cambiado código productivo.

## 4. `CU3-B` y alcance

- `CU3-B` sigue sin implementar y sin aprobación de sus reglas de identidad. La
  [revisión arquitectónica](../architecture/pre_g9b_cu3_b_identity_architecture_review.md),
  publicada en `7f1a81ae`, queda pendiente de una auditoría independiente.
- No se modifica `main`, no se inicia ninguna otra fase, la autoridad constructiva no se promueve
  a predeterminada y `CU3` no se cierra.
- La continuidad editorial de `CU10` se mantiene (registro E1, versión 13); `geocedg_book` no
  cambia.
