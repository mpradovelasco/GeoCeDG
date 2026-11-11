# PRE-G9B-CU10 — Expediente integrado

11 de noviembre de 2026. Evidencia del candidato técnico original de CU10.
La puerta técnica de `FINAL` sigue incumplida. El autor aprobó el cierre por
excepción expresa y dispensó toda nueva campaña. `selfApproved=false`.
`AUTHOR SMOKE = PENDING`.
[Registro de cierre excepcional](pre_g9b_cu10_closeout_record.md).

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
El informe posterior a la campaña consigna commit y árbol, ausencia de
recibo, dictamen y evidencias operativas. Los resultados pertenecen al árbol
ejecutado. El descendiente documental `9f97e089e9c791aa938277282df72406e415b285`
conserva esa atribución.

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

## Resultado de la campaña integrada

**Puerta técnica incumplida.** El único `FINAL` terminó con código **3** y
resultado `REJECTED_VERIFICATION_CORE / COMPLETE`. El candidato integrado es
`20ae57aa484fc0d60c65de76a23151d4d2a758c1`, árbol
`0c0516bbb0f507125683a82433277b10f823346a`. El informe de esta disposición
es documental. La campaña pertenece al candidato integrado exacto.

| Comprobación | Resultado |
|---|---|
| Campaña canónica | `verification-79db5599d9304094b2020b308b836507`, 68/68 nodos ejecutados. Huella lógica `0d6e21c86d31b3cae29967f03235e70f538ac22d3ec76a9e5193a5c4ba01a915`. SHA-256 del archivo `3902e0f5538633f099e0a7c80a05626a39780f396fcaf2df017e8180f0f64ed8`. [Resultado tipado](../../artifacts/pre-g9b-cu10/final/verification-result.json) · [Diario](../../artifacts/pre-g9b-cu10/final/run-journal.json) |
| Controles obligatorios | 39/41 satisfechos. Semántica 15/15, seguridad 4/4 e infraestructura de verificación 20/22. Los 17 productores terminaron con código 0. |
| JUnit | 2.522 Desktop y 7.732 compartidos, más 5 Desktop aislados. Cero fallos y errores. Once omisiones admitidas por el inventario, una Desktop y diez compartidas. La ejecución diagnóstica Desktop adicional pasó su caso. |
| Diagnósticos | Ocho claros. El diagnóstico de gobernanza registró el protocolo de recuperación vigente. El diagnóstico de consistencia histórica quedó no disponible. |
| Recibo | El veredicto rechazado dejó el resultado y su evidencia. El verificador no emitió recibo de aceptación. |

### Disposición técnica del rechazo

- `infra.final-coverage` terminó con código 1. Su prueba conserva la
  expectativa de **85** selecciones JUnit, frente a **92** en el inventario
  vivo. El análisis de las aserciones posteriores encontró **82** registros
  PHASE frente a **87** y **159** casos esperados para
  `pre-g9b-r6-plus-f4-a.desktop` frente a **161**. Esas dos aserciones
  quedaron fuera de la ejecución de esa prueba por su primera excepción.
  [Salida conservada](../../artifacts/pre-g9b-cu10/final/checks/infra.final-coverage/stderr.bin) ·
  [Prueba](../../tools/agent/tests/verification-final-coverage.Tests.ps1).
- `infra.static-semantic` terminó con código 1. El contrato
  `workspace.profile-authority` conserva la huella canónica
  `5dfcf28c7caaebe56d55bc038c648ddd0c9cc6277912d86f7ee2456b6d6f944d`
  de `workspace-profile-validation.ps1`. CU8 cambió el script en
  `0d05b024c` y la huella canónica actual es
  `ff6773feee8c47e8c2c3a1945d2d73b38c1b19c1b65cc4cceb7e3f7062ce974e`.
  El catálogo está enlazado por el registro y este es su único pin de entrada
  divergente. El control semántico del perfil sí quedó satisfecho.
  [Salida conservada](../../artifacts/pre-g9b-cu10/final/checks/infra.static-semantic/stderr.bin) ·
  [Catálogo](../../geocedg/specs/operations/verification-static-contracts.json).

El preflight comprobó la cobertura declarada con `PlanOnly` y el toolchain con
`WORKSTATION`. Las pruebas de `INFRA_UNIT` se ejecutaron por primera vez
como hojas de esta campaña `FINAL`. Un `INFRA_UNIT` focal previo habría
expuesto las dos divergencias antes de congelar el candidato.

Las dos causas se reproducen leyendo las fuentes del baseline publicado.
El delta congelado solo incorpora documentación. La reparación de §11.2
requiere otro perímetro, y §11.3 exige cobertura `UNTRUSTED` debida a
exactamente una proyección JUnit obsoleta. Este resultado tiene cobertura
`COMPLETE` y dos hojas `CONTRACT_VIOLATED`.
[Contrato y rutas de reparación](../../geocedg/specs/operations/verification-levels.md#11-verification-input-identities-and-evidence-preserving-repair).

La decisión autoral posterior autorizó las tres expectativas citadas y las
actualizaciones de huellas de `workspace.profile-authority` y del catálogo
estático en el registro. Una autorización adicional cubrió la huella del
generador Windows en `packaging.source-authority`. Las correcciones quedan
aplicadas en el commit de cierre y sin reverificación por dispensa expresa.
El resultado histórico conserva su veredicto y sus dos fallos.
[Registro de cierre excepcional](pre_g9b_cu10_closeout_record.md).

## Distribución Windows

Se generaron ambos conjuntos completos con el builder versionado,
`-Target All` y directorios inicialmente ausentes. NC reutilizó el
`installDist` del mismo commit y volvió a exportar el inventario de
componentes. Cada conjunto conserva app-image, ZIP, MSI, EXE, manifiesto
de compilación, SBOM, avisos, composición y sumas SHA-256.

| Perfil | Generación | Verificación canónica y evidencia |
|---|---|---|
| INTERNAL | Código 0. [Log](../../artifacts/pre-g9b-cu10/build-internal.log) · [Manifiesto](../../artifacts/pre-g9b-cu10/packages-internal/build-manifest.json) · [SBOM](../../artifacts/pre-g9b-cu10/packages-internal/geocedg-windows.cdx.json) · [SHA-256](../../artifacts/pre-g9b-cu10/packages-internal/SHA256SUMS.txt) | `verify.ps1 -Profile PACKAGING -VerifyPackagingArtifacts`, código 0, `ACCEPTED / COMPLETE`, 3/3. [Informe](../../artifacts/pre-g9b-cu10/verify-packaging-internal/verification-result.json). Evaluación interna. |
| NC | Código 0. [Log](../../artifacts/pre-g9b-cu10/build-nc.log) · [Manifiesto](../../artifacts/pre-g9b-cu10/packages-nc/build-manifest.json) · [SBOM](../../artifacts/pre-g9b-cu10/packages-nc/geocedg-windows.cdx.json) · [SHA-256](../../artifacts/pre-g9b-cu10/packages-nc/SHA256SUMS.txt) | `verify-packaging.ps1 -RequireArtifacts`, código 0, `ACCEPTED / COMPLETE`, 3/3. [Informe](../../artifacts/pre-g9b-cu10/verify-packaging-nc/verification-result.json). Perfil de distribución no comercial sujeto a su composición y condiciones documentadas. |
| COMMERCIAL | El builder salió con código 1 antes de crear el directorio de salida. [Log](../../artifacts/pre-g9b-cu10/commercial-denial-confirmed.log) | Perfil no autorizado conforme a [su registro](../licensing/geocedg_profile_commercial.md). |

El toolchain observado es PowerShell 7.6.9, Gradle Wrapper 9.4.1, JDK
22.0.2 para el lanzador, Corretto 17.0.10 para la compilación compartida,
Temurin y `jpackage` 25.0.4 para Desktop y Windows, .NET SDK 8.0.303 y
WiX 5.0.2 con extensiones Util y UI 5.0.2. Los manifiestos identifican
las entradas y las huellas de esta ejecución. El procedimiento es
repetible con las entradas fijadas. La identidad binaria entre
reconstrucciones de `jpackage` queda fuera de esta comprobación.

El cotejo independiente verificó 433 archivos por app-image y su identidad
con las entradas de cada ZIP. Las dos ediciones de la guía y sus 17 PNG
coinciden byte a byte con los blobs del árbol exacto en ambos JAR. Se
identificaron en los ejecutables las siete imágenes del icono declarado.
Los ZIP se ejecutaron fuera del checkout y cargaron `jvm.dll` de sus
propias imágenes. Las cuatro pruebas de MSI y EXE en Windows Sandbox
dieron códigos 0/0 para instalación y desinstalación. Se observó el
título `GeoCeDG 1.1.0`, el runtime incluido, el registro de distribución
idéntico al del app-image y la asociación `.cedg` con su MIME interno.
La asociación `.ggb` permaneció ausente en las cuatro sesiones. También
se inspeccionó el MSI integrado en cada EXE.
[Evidencia de empaquetado y ejecución](../../artifacts/pre-g9b-cu10/operational-evidence/archive-sha256.json) ·
[Microsoft Windows Sandbox](https://learn.microsoft.com/en-us/windows/security/application-security/application-isolation/windows-sandbox/windows-sandbox-cli) ·
[OpenJDK MSI wrapper](https://github.com/openjdk/jdk25u/blob/master/src/jdk.jpackage/windows/native/msiwrapper/MsiWrapper.cpp).

**Observación P2 del manifiesto NC.** Su
`file_association.description` dice `GeoCeDG document (internal evaluation)`.
El perfil NC y los MSI independiente e integrado usan `GeoCeDG document`.
La discrepancia se encuentra también en un manifiesto NC histórico. El
verificador validó la asociación real y omitió la comparación de ese campo
informativo. El commit de cierre aplica `$SelectedAssociationDescription`
al campo del generador. Los artefactos previos conservan su evidencia y el
generador corregido queda sin ejecución posterior.
[Prueba del campo](../../artifacts/pre-g9b-cu10/operational-evidence/nc-association-discrepancy.json) ·
[Contrato NC](../licensing/geocedg_profile_nc.md).

## Smoke, revisión independiente y worktrees

El [protocolo del agente](../../artifacts/pre-g9b-cu10/operational-evidence/desktop-smoke/protocol.md)
enlaza con las 21 capturas y los cuatro archivos generados. Se reprodujo
`LocusLength=10`, la materialización explícita de un punto admisible,
la recomputación de Spline V2, la paralela, CU8 y la persistencia nativa.
El DXF conservó exactamente la coordenada evaluada de la entidad
seleccionada. El modelo `.ggb` del autor se insertó con Deshacer y
Rehacer y se conservó una copia nativa de esa prueba. El visor de ayuda
abrió ES/EN, buscó y navegó a figuras y siguió un enlace interno.
`CU10-GUIDE-INTEGRATION-CHECK` cuenta con auditoría fuente, recursos
empaquetados, pruebas Desktop y smoke del candidato.

La combinación Ctrl+clic de memoria de referencia quedó pendiente en
Computer Use. Las suites Desktop `PreG9BR6PlusF2AuthoringMemoryTest`
y `PreG9BR6PlusF2R1DualActivationTest` pasaron sus 35 casos.
`AUTHOR SMOKE = PENDING`. La interacción exhaustiva de los modelos del
autor se apoya en el [dossier CU10-B](pre_g9b_cu10_model_validation.md).

La revisión independiente contrastó alcance CU0–CU9, modelos, expediente
E1, ambas distribuciones y la evidencia gráfica. Corrigió la procedencia
del corpus y dos precisiones científicas de E1 antes de congelar. Detectó
la observación NC y confirmó por separado el veredicto y la ineligibilidad
de las rutas de conciliación. Ambos informes PACKAGING fueron cotejados
con el esquema y sus huellas canónicas.
[Dictamen de evidencias](../../artifacts/pre-g9b-cu10/operational-evidence/independent-review.md) ·
[Dictamen sobre FINAL](../../artifacts/pre-g9b-cu10/independent-final-disposition.md).

**POST-CU CLEANUP — PARTIAL.** Se inventariaron los tres worktrees.
El principal conserva el candidato y este descendiente documental.
`cu7-general-creation` se conserva con 18 entradas pendientes y
11.491 rutas ignoradas. `cu7-integrated-study` está limpio y carece de
archivos ignorados. Su commit exclusivo `7bc5d9f80` está preservado
en `research/pre-g9b-cu7-integrated-reassessment`. No hubo coincidencias
en los procesos accesibles ni en 100 sesiones recientes de Codex. La
inspección de diez campos de procesos, incluidos procesos de terminal,
quedó incompleta. Se conserva ese worktree por el criterio de uso
inequívoco. Los tres blobs de `CLAUDE.md` coinciden con Git.
[Inventario](../../artifacts/pre-g9b-cu10/cleanup/inventory.json) ·
[Sesiones](../../artifacts/pre-g9b-cu10/cleanup/sessions.json).

La consulta de GitHub de la campaña confirmó el baseline `b6a86bf0` en `main`,
igual a `main` y `origin/main` locales. Las decisiones de promoción,
etiqueta y release quedan reservadas al autor.
[Lectura remota](../../artifacts/pre-g9b-cu10/final-origin-main.tsv).

## Fuentes y archivos afectados

Se inspeccionaron `AGENTS.md`, el roadmap y los dos planes CU, los
registros de cierre CU0–CU9, el contrato de verificación, los inventarios
JUnit y estático, el registro ejecutable, el manual operativo y el
contrato Windows. La matriz, el dossier de modelos y E1 enlazan los ADR,
contratos, clases, pruebas, modelos, catálogos y bibliografía inspeccionados
para sus resultados.

El candidato congelado modifica el plan correctivo y el roadmap e
incorpora seis documentos de validación. Este registro posterior modifica
el informe principal y la matriz de deudas. El delta pertenece a la capa
de documentación y trazabilidad. Las pruebas ejercitaron el núcleo,
Desktop y la distribución a través de sus rutas vigentes. Los resultados
de JUnit, empaquetado, Computer Use, benchmark e instalación se conservan
como evidencia generada en `artifacts/pre-g9b-cu10`.

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

El autor conserva `1.1.0` con fecha `2026-10-15`, acepta la evidencia
dentro de sus límites demostrados, recibe E1 para una futura fase editorial
y cierra CU10 y el minitrack mediante la excepción expresa a `FINAL`.
El smoke autoral sigue pendiente de comunicación. La promoción exacta a
`main`, las etiquetas, el release y el trabajo editorial requieren
autorizaciones propias.
[Registro de cierre](pre_g9b_cu10_closeout_record.md) ·
[AGENTS.md §8.1](../../AGENTS.md#81-geocedg-publication-policy).

Clasificación del expediente en el candidato técnico original.

```text
VERIFICATION_CLASS = GLOBAL_IMPACT
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
BOOTSTRAP_JUSTIFICATION = El expediente no cambia requisitos, perfiles ni herramientas.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = La comprobación de integración conserva los recursos aprobados en CU9.
selfApproved = false
```
