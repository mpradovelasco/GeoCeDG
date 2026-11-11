# PRE-G9B-CU10 — Expediente integrado

11 de noviembre de 2026. Candidato local para la revisión del autor.
`selfApproved=false`. `AUTHOR SMOKE = PENDING`.

## Identidad y alcance

La base publicada es el cierre CU9 `b6a86bf00dfb691c82e6009dfed1a9a0c20a865a`,
árbol `8ebaeb3a545a7bdb435f72ea966fcd7f89594888`. Antes de intervenir se
cotejaron `HEAD`, `main`, `origin/main` y la referencia viva de GitHub con
`git ls-remote --exit-code origin refs/heads/main`, código 0.
Rama local `codex/pre-g9b-cu10-integrated-closeout`.

CU10 es una actuación de integración, verificación y documentación con
impacto global. El [plan correctivo](../architecture/pre_g9b_cu_corrective_usability_plan.md),
el [roadmap](../roadmap/geocedg_roadmap.md#track-pre-g9b-cu--corrección-y-usabilidad)
y el [cierre CU9](pre_g9b_cu9_closeout_record.md) gobiernan el perímetro.
La [matriz de compromisos y deudas](pre_g9b_cu10_commitments_and_debt.md)
recoge las decisiones CU0–CU9. El [dossier de modelos](pre_g9b_cu10_model_validation.md)
identifica los barridos, las entradas y los límites de su evidencia. El
[expediente E1](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md) se entrega para
revisión científica y editorial en el repositorio de producto.

La identidad exacta de la ejecución es la del informe tipado de `FINAL`.
El informe posterior a la campaña consignará commit y árbol, identidad del
recibo, dictamen y evidencias operativas. Los resultados de un commit
pertenecen a su árbol. Un eventual descendiente documental conserva esa
atribución.

## Puertas técnicas

- `WORKSTATION` comprueba las capacidades portables y el origen de las
  importaciones. El preflight en la base terminó con código 0,
  `ACCEPTED / COMPLETE`, con Gradle 9.4.1, JVM lanzadora Oracle 22.0.2,
  Corretto 17.0.10, Temurin 25.0.4+7, CPython 3.12.13 y mpmath 1.4.1
  dentro de `cedg_env`. Informe en
  `artifacts/pre-g9b-cu10/workstation/verification-result.json`.
- `FINAL -PlanOnly` devolvió código 0 y un plan completo para Windows,
  con 41 hojas de aceptación o proyecciones de aceptación, 10 diagnósticos
  y 17 productores. Huella
  `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517`.
  `artifacts/pre-g9b-cu10/final-plan.json` conserva el plan.
- La campaña de aceptación es `tools/agent/verify.ps1 -Profile FINAL`
  sobre el commit limpio y congelado. La validación explícita de artefactos
  es `PACKAGING` según el [contrato activo](../../geocedg/specs/operations/verification-levels.md#current-contract).
  La ejecución usa directorios de perfil de aplicación nuevos, limitados
  al proceso y sus hijos, para reproducir el entorno aislado de CU9 y
  evitar la sensibilidad observada en la [campaña CU4](pre_g9b_cu10_model_validation.md).
  El adaptador de cierre comprueba la identidad del recibo cuando se emite.
- `CU10-GUIDE-INTEGRATION-CHECK` combina inventario fuente ES/EN, casos
  Desktop, comparación de recursos del JAR con el árbol y smoke de la
  guía empaquetada. Los recuentos de CU9 son 294 entradas, 118 anclas,
  48 enlaces internos y 15 figuras por edición.
- Para cada paquete, conservar el manifiesto, SBOM, sumas de comprobación,
  composición, perfil, runtime y commit. La identidad del árbol se
  obtiene del commit mediante Git. El [manual operativo](../developer/geocedg_operations_manual.md#4-package-and-install-for-internal-evaluation)
  y el [contrato Windows](../../geocedg/specs/packaging/windows-packaging.md)
  rigen la construcción y el smoke de instalación.

## Protocolo breve de smoke autoral

Registrar fecha, perfil, SHA, paquete y resultado. Usar copias de trabajo
de los modelos y guardar los resultados en una ubicación de prueba.

1. Abrir el ejecutable empaquetado y comprobar `GeoCeDG 1.1.0` y la
   identificación de la distribución en la información del producto.
2. Abrir cada modelo del dossier de barridos. Repetir un barrido regular,
   una frontera y la configuración degenerada registrada. Verificar
   continuidad, estados de certificación, y dependencia al mover el
   conductor. Confirmar el comportamiento de la intersección y la medida
   previstas por cada fixture.
3. Crear o editar un objeto con la barra, comprobar la memoria de la
   herramienta, insertar en los argumentos admitidos y añadir un control
   al contenedor CU8. Guardar como `.cedg`, cerrar y volver a abrir.
   Comprobar posiciones, controles, argumentos y dependencias.
4. Abrir la guía ES y EN, usar la búsqueda, seguir un enlace interno y
   mostrar una figura de Locus V2 o Spline V2. Abrir o exportar un caso
   2D admitido según la guía.
5. Registrar el resultado de los pasos anteriores y anotar
   desviaciones con pasos de reproducción, configuración y, si procede,
   capturas. El smoke del agente se registra por separado.

## Versión, etiqueta y decisiones

Se propone mantener `1.1.0` con fecha `2026-10-15`, el valor vivo de
`packaging/windows/package.yml`, para preservar la identidad del producto
durante la revisión. Una versión nueva cambiaría entradas de compilación y
empaquetado y necesitaría su propia identidad técnica.

Se propone una etiqueta anotada nueva e inmutable
`geocedg-pre-g9b-cu-pass` para la revisión exacta autorizada, con versión,
commit, árbol y referencia al recibo en la anotación. Su destino y creación
requieren autorización expresa. Si se autoriza promover a `main`, aplicar
[AGENTS.md §8.1](../../AGENTS.md#81-geocedg-publication-policy), con lecturas
de la referencia viva, comprobación de ascendencia, limpieza y
fast-forward, más cotejo de las tres referencias tras la publicación.

Quedan reservadas al autor diez decisiones independientes.

1. Aceptación del resultado integrado sobre el SHA indicado.
2. Aceptación de las limitaciones pendientes de disposición autoral.
3. Aprobación del expediente científico-editorial E1.
4. Confirmación del smoke autoral.
5. Versión final.
6. Cierre del minitrack.
7. Publicación del candidato exacto en `main`.
8. Creación de la etiqueta anotada.
9. Eventual release de binarios con perfil, composición y evidencia legal aprobados.
10. Eventual reactivación editorial de `geocedg_book`.

```text
VERIFICATION_CLASS = GLOBAL_IMPACT
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = El expediente no cambia requisitos, perfiles ni herramientas.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = La comprobación de integración conserva los recursos aprobados en CU9.
selfApproved = false
```
