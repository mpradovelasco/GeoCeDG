# PRE-G9B-CU9 — Cierre autoral

**PRE-G9B-CU9 — PASS — AUTHOR APPROVED**

El 2026-11-11, en «GeoCeDG — PRE-G9B-CU9 / Cierre autoral», el autor
confirmó su revisión y aprobación de CU9, R1 y R2. `selfApproved=false`.
La decisión permite preparar la promoción directa por avance rápido.
La publicación requiere autorización expresa posterior sobre el candidato
de promoción.

| Identidad | Valor |
|---|---|
| Baseline vivo de `main` | `6c032394d70a004a4b91a88338e647f13d365cfa`, árbol `96f155ee138b5ec3d15707791d73c46cc3c134df` |
| CU9 inicial | `7035b01c501c9303cd75331c070d7d1061751e6b`, árbol `0afd5d9289686f8c3772d28c2e3d4195f0bbcb16` |
| CU9-R1 | `e2be710c43e33a22648d9729cb71b091791f538b`, árbol `51284a796191e6e9fe962a785d407a3a17e62e44` |
| Producto aprobado CU9-R2 | `f5356cb1e7bb60ed152ff236b7dceb3fd64bad86`, árbol `84289da22ca60159d17c8077f0a38bdb6d0db759`, rama `codex/pre-g9b-cu9-r2-guide-marks-and-figures` |
| Cierre documental | Commit que contiene este registro, hijo directo de CU9-R2, rama `codex/pre-g9b-cu9-closeout` |

[Espejo estructurado](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu9-closeout.json).

## Alcance adoptado

- **B11.** Referencia de `LocusLength`, cobertura, resultados y políticas de
  recorrido implementadas, con ejemplos reproducibles.
- **B12.** Guía de uso bilingüe, tareas y ejemplos, comparación con GeoGebra
  Classic fijado y referencia exhaustiva. La matriz aprobada tiene 294 entradas,
  282 cubiertas y 12 exclusiones justificadas. Las 282 tienen marca o indicación
  explícita de disponibilidad. Hay cero parciales y cero ausentes.
- **H07.** Rutas, nombres, comandos, formatos, disponibilidad y límites
  actualizados para el producto aprobado.
- **Ayuda Desktop.** Navegación derivada de Markdown con 118 anclas, búsqueda
  local, 48 enlaces internos y 15 figuras por edición. Las marcas distinguen
  novedades, ampliaciones, capacidades heredadas y límites. Los 17 PNG
  conservan el inventario de procedencia, los pies y los textos alternativos.

[Guía ES](../user/geocedg_user_guide_es.md) ·
[Guía EN](../user/geocedg_user_guide_en.md) ·
[Matriz](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json) ·
[Inventario de figuras](../user/images/figures.json).

## Evidencia y puerta documental

| Árbol ejecutado | Evidencia atribuida |
|---|---|
| CU9 inicial `7035b01c5` | STATIC 3/3, PHASE CU9 5/5 e INTEGRATION 19/19, todos `ACCEPTED / COMPLETE`. |
| CU9-R1 `e2be710c4` | STATIC 3/3, PHASE CU9 5/5 e INTEGRATION 19/19, todos `ACCEPTED / COMPLETE`. PHASE ejecutó 127 casos Desktop y 39 compartidos. La app-image INTERNAL Windows x64 se probó fuera del checkout con el runtime incluido, en ES y EN. |
| CU9-R2 `f5356cb1` | STATIC 3/3 `ACCEPTED / COMPLETE`. La selección focal Desktop pasó sus 127 casos, con auditoría bilingüe, empaquetado y smoke gráfico ES/EN. Los dos Markdown y los 17 PNG del JAR coinciden byte a byte con los blobs aprobados. Los 1.690 archivos de clase de Desktop coinciden con R1. |
| Descendiente documental | Puerta STATIC y comprobaciones de coherencia sobre su identidad congelada. El espejo identifica la ruta prevista del informe de cierre. |

El espejo conserva los SHA, árboles, identificadores de ejecución y huellas.
PHASE e INTEGRATION pertenecen a los árboles en los que se ejecutaron. R2 tiene
evidencia focal propia según la decisión autoral de verificación reducida.
El [contrato vigente](../../geocedg/specs/operations/verification-levels.md#current-contract)
y el registro ejecutable no prescriben una campaña adicional para este cierre
de estado. La inspección de un recibo de aceptación FINAL corresponde al
candidato integrado de CU10. El espejo distingue los informes existentes
de ese futuro recibo.

## Preparación de la promoción

El destino es el commit documental de la tabla de identidades. Preparar el
avance rápido conforme a [AGENTS.md §8.1](../../AGENTS.md#81-geocedg-publication-policy)
comprende comprobar que el `main` vivo coincide con `origin/main` y el `main`
local, verificar la ascendencia y cotejar el delta documental y la limpieza.
Tras la autorización expresa sobre el SHA de promoción, repetir la lectura
remota y los controles de limpieza, avanzar `main` por fast-forward y
comprobar las tres referencias tras el push.

## Traspaso a CU10

CU10 requiere su propia autorización. Su punto de partida será la revisión
publicada del cierre de CU9, una vez autorizada la promoción.

1. Congelar el candidato integrado. Ejecutar una vez el perfil canónico
   `FINAL` sobre ese commit exacto y conservar su informe y recibo cuando
   corresponda. Incluir `CU10-GUIDE-INTEGRATION-CHECK` sobre la guía vigente,
   con empaquetado y coherencia ES/EN. [Compromiso CU3](pre_g9b_cu3_global_closeout_record.md).
2. Validar dinámicamente con los modelos del autor. Registrar modelo y versión,
   intervalos, expectativas y observaciones de los barridos B07, B10 con y sin
   `e_2`, continuidad y materialización B08/B09 e inserción B01. Recoger el
   smoke autoral y los datos de rendimiento según el plan.
3. Consolidar las limitaciones y la disposición de deudas. Mantener trazables
   la resolución numérica de CU1, las clases de certificación CU2/CU3 y la
   generalización de tangencias, M07, los elementos diferidos de CU7 y las
   fronteras de inserción y compatibilidad de CU8. Registrar los límites
   editoriales de CU9, incluidos los destinos internos propios del visor y
   la reducción del texto de las capturas en columnas estrechas.
4. Entregar E1, `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF`, conforme al
   [plan científico §7](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md#7-expediente-de-cu10-scientific_methods_editorial_handoff).
   Debe inventariar métodos y estado, alcance y generalidad, fuentes y pruebas,
   resultados rigurosos y estimaciones, degeneraciones, modelos reproducibles,
   bibliografía, lagunas y propuesta de apéndices. Incluir la exposición de
   autoridad constructiva e integridad de las relaciones, con su distinción
   respecto de la representación exacta en CAD. La transferencia al libro
   pertenece a una actuación editorial separada.
5. Someter al autor el candidato, las limitaciones, la versión y las
   decisiones de cierre y eventual publicación del track.
   [Plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md#cu10--cierre-integrado).

```text
VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = El cierre solo registra estado, evidencia y traspaso.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = El cierre conserva los blobs aprobados de las guías, matriz y figuras.
selfApproved = false
```
