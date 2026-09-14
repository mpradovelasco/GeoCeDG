# PRE-G9B-D1 — research/audit de licensing, assets y deployability

Estado: **RESEARCH/AUDIT CANDIDATE — PENDING AUTHOR REVIEW**

~~~text
selfApproved=false
authorApproved=false
passClaimed=false
publicRedistributionAllowed=false
remediationAuthorized=false
~~~

Este informe es inventario factual y análisis técnico de composición para
decisión humana. No es asesoramiento legal, no concede licencia y no interpreta
una ambigüedad a favor de la redistribución.

Respuesta corta: GeoCeDG no está preparado todavía para abandonar
“INTERNAL EVALUATION — NOT FOR REDISTRIBUTION”. El paquete Windows es
reproducible por hash, pero combina material GeoCeDG sin decisión de licencia
pública, código upstream, 390 bundles de traducción, al menos 1.884 UI assets
embebidos —incluidos logos GeoGebra—, 46 fonts heterogéneas, binarios nativos y
dependencias sin bundle legal completo. Además, el MSI incumple el contrato de
asociación “.cedg”. Los blockers y decisiones están en el
[docket D1](pre_g9b_d1_human_decision_docket.md).

## 1. Baseline y autoridad

La auditoría se ancla al estado publicado comprobado el 14 de septiembre de
2026:

| Referencia | Valor observado |
|---|---|
| HEAD, main, origin/main | c2e15b3d09a921442108ecb6b5891b4d6211a3ae |
| árbol | f218606655122a0728c87eb076783c5daabb5e6f |
| live origin/main | mismo SHA, verificado con git ls-remote |
| divergencia main...origin/main | 0/0 |
| worktree de entrada | limpio |
| rama | audit/pre-g9b-d1-licensing-deployability |
| upstream pin | GeoGebra 5.4.928.0, 9b93256b7df401ff056c37b502d82df4d72b1522 |

El prompt nombró “docs/development/geocedg_roadmap.md”, que no existe. La
autoridad es [docs/roadmap/geocedg_roadmap.md](../roadmap/geocedg_roadmap.md).
Es una discrepancia nominal, no una contradicción de gobierno. El roadmap
mantiene D1 diseñado, P1 y G9B/G9C no autorizados, G9U2 bloqueado y
redistribución pública bloqueada. La autorización expresa de esta tarea abre
solo research/audit y documentación D1; no abre L4, P1 ni cambios productivos.

Se reutiliza la taxonomía L3 del
[diseño pre-G9B](../architecture/pre_g9b_stabilization_deployability_design.md):
ALLOW, ALLOW_WITH_NOTICE, REPLACE, EXCLUDE, REQUIRES_PERMISSION y
UNKNOWN / BLOCKED. Son propuestas técnicas, nunca autorizaciones legales.

## 2. Metodología reproducible

1. Se comprobó el grafo Git local, origin/main y el remote vivo antes de
   interpretar el alcance.
2. Se inspeccionaron roadmap, licensing, assets, packaging, ADR, specs y
   verificadores sin alterar evidencia histórica.
3. Se reconstruyó el paquete mediante
   “tools/release/build-windows-package.ps1 -Target All”. El intento sandboxed
   falló por red al descargar Gradle; la repetición gestionada fuera del
   sandbox terminó con exit 0. No se cambió producto para compensarlo.
4. Se obtuvo runtimeClasspath resuelto y se cruzó con los 52 JAR staged.
5. Se recorrieron ZIP/JAR entry-by-entry: extensiones, tamaños, SHA-256,
   metadata legal, resources, fonts y nativos. También runtime/legal.
6. Se compararon app-image, ZIP, MSI y EXE; el verificador canónico
   descompiló el MSI.
7. Se consultaron fuentes primarias externas vigentes a 2026-09-14. Cuando no
   pudo ligarse el binario exacto con evidencia suficiente, se mantuvo
   UNKNOWN / BLOCKED.

La [matriz estructurada](../../geocedg/validation/pre-g9b-d1/component-audit.json)
contiene los 52 JAR con hash, las 46 TTF por archivo, once clases adicionales,
fuentes primarias, texto disponible y disposición candidata.

## 3. Closure efectiva de distribución

~~~text
:desktop:desktop:installDist
  -> staging de 52 JAR
  -> marker + LICENSE/NOTICE/THIRD_PARTY/LICENSES/assets-manifest
  -> jpackage app-image + runtime Java
  -> ZIP normalizado del app-image
  -> jpackage/WiX MSI y EXE desde la misma entrada
~~~

En una aplicación classpath no modular, jpackage genera el runtime con el
conjunto estándar de módulos expuesto por el launcher; la closure no se limita
a los JAR.[^3]

### 3.1 Outputs

| Output | Bytes | SHA-256 |
|---|---:|---|
| GeoCeDG-0.9.0-windows-x64-internal.zip | 91.403.162 | be92fa4f8261fb8608bd908f4bcc253d298e7578484551cabbfa4d42c2d1f0a1 |
| packages/GeoCeDG-0.9.0-windows-x64-internal.msi | 84.816.364 | 74df47506499c24a771c22ea69cf7ac4a7297dded0e8225185d76b8b7a191fbd |
| packages/GeoCeDG-0.9.0-windows-x64-internal.exe | 85.389.824 | 1c042e75b6df190f3f1846edddc9dbca087b376c825eb85f3d9d9e76d9581cd1 |

Son GeoCeDG 0.9.0, internal-evaluation, Windows x64. El launcher app-image mide
547.840 bytes. MSI/EXE transportan la misma aplicación y añaden instalador.

### 3.2 Aplicación, dependencias y SBOM

Se distribuyen 52 JAR: doce outputs de proyecto y cuarenta dependencias
directas/transitivas. El grafo incluye J2ObjC annotations, ANTLR 2, SpotBugs
annotations, JSR-305, math-cross-platform, Rhino, FlatLaf, JNA,
EchoSVG/CSS4J/W3C APIs/JCLF, OpenGeoProver, ReTeX, JOGL/GlueGen y Giac.

La SBOM actual es exacta como “filename + SHA-256 + package path”, pero no es
suficiente para compliance: las **52 versiones figuran como unknown** y faltan
purl, coordenadas, licencias, copyright, notices, assets, fonts, nativos
anidados y runtime Java.

### 3.3 Recursos embebidos

| Contenedor | Contenido observado |
|---|---|
| common.jar | 5.403 clases, 846 PNG, 646 SVG, 3 GIF: 1.495 assets |
| common-jre.jar | 390 properties de traducción y 63 clases |
| desktop.jar | 1.393 clases, 267 PNG, 63 GIF, 59 SVG, 30 properties, 2 guías Markdown, perfiles/schemas y branding |
| renderer-desktop.jar | 46 TTF, 15 clases y solo 3 textos de fonts |

common.jar contiene geogebra-logo.png, geogebra_logo_transparent.png,
ggb_logo_back.png y ggb-logo-name.svg. Usar launcher GeoCeDG no excluye marcas
ni UI assets upstream. Los términos oficiales distinguen código fuente EUPL de
language files, documentación e imágenes/styles de UI —logos e iconos
incluidos—, sujetos a otros términos y restricción no comercial; producto y
marcas se tratan aparte.[^1][^2]

desktop.jar distribuye dos fuentes de branding del autor y cuatro derivados.
También distribuye geocedg_user_guide.md y
geocedg_construction_quick_guide.md: la exclusión de “repository documentation”
no es absoluta. No se observó CSS externo directo; resources y styles
distribuidos están embebidos y un scan superficial del app-image no los ve.

### 3.4 Fonts y ReTeX

Las 46 TTF se inventariaron individualmente. El registro fuente mapea 33:

- 4 SIL OFL 1.1;
- 1 dsrom con texto específico;
- 2 declaradas public domain;
- 10 Computer Modern bajo texto Knuth;
- 8 griegas descritas como GPL-2.0;
- 8 cirílicas descritas como Knuth.

Quedan 13 sin mapeo expreso: jlm_cmmib10.ttf, nueve jlm_jlm*,
jlm_cmssi10.ttf, jlm_cmti10.ttf y jlm_special.ttf. El JAR solo conserva
OFL.txt, Knuth_License.txt y License_for_dsrom.txt; faltan GPL-2.0 y evidencia
específica de las dos public-domain. renderer-base/LICENSE asigna Knuth a las
cirílicas mientras un COPYING GPL próximo en renderer-web exige reconciliación.
No se eligió la interpretación permisiva.

El código ReTeX/JLaTeXMath se describe en source como GPL-2.0-or-later con
excepción de linking, pero ese texto no llega al bundle legal.

### 3.5 Nativos

El builder excluye seis **JAR completos** Linux/macOS de JOGL/GlueGen/Giac y
conserva las variantes Windows: 1 DLL GlueGen, 1 Giac y 5 JOGL. Sin embargo:

- flatlaf-3.7.jar embebe 7 nativos: 3 Windows, 2 Linux y 2 macOS;
- jna-5.18.1.jar embebe 26 binarios para Windows, Linux, macOS, Solaris y BSD.

El filtro opera por nombre de JAR, no por payload anidado. Es discrepancia de
composición, no por sí sola una conclusión legal. JNA declara Apache-2.0 o
LGPL-2.1-or-later; la selección y textos completos deben registrarse.[^7]

Los JAR nativos JOGL/GlueGen/javagiac no incorporan LICENSE/NOTICE. Las fuentes
JogAmp contienen terms/notices de múltiples orígenes, aún no ligados a cada DLL
2.6.0.[^11] El sitio oficial de Giac describe términos GPL, pero el binario
javagiac 70501 del repo GeoGebra no aporta POM/licencia: source correspondiente
y texto aplicable siguen UNKNOWN / BLOCKED.[^12]

### 3.6 Runtime Java e instalador

jpackage 25.0.4 generó Temurin/OpenJDK 25.0.4+7 con 52 módulos, 315 archivos y
128.004.149 bytes. runtime/legal contiene 52 directorios y 195 textos, incluidos
GPLv2, Classpath Exception, ADDITIONAL_LICENSE_INFO y notices de FreeType,
HarfBuzz, ICU, libjpeg, libpng y zlib. Es la clase mejor documentada, pero falta
fijar URL/checksum exactos de Temurin y revisar source availability y marcas.[^4][^5]

WiX 5.0.2 y .NET son tools de build; no se presume que todo tool se
redistribuya. La MS-RL es fuente primaria de WiX, pero falta determinar qué
engine/resources quedan dentro de EXE/MSI. Esa frontera sigue bloqueada.[^6]

### 3.7 Bundle legal

Los cuatro formatos incluyen marker, LICENSE, NOTICE.md, THIRD_PARTY.md,
LICENSES/README.md y assets-manifest. Demuestran estado, no suficiencia:
LICENSE es no-grant; LICENSES solo tiene README; NOTICE y THIRD_PARTY declaran
estar incompletos. Los LICENSE/NOTICE internos de algunos JAR no se consolidan.

### 3.8 Material excluido y source distribution

No se encontraron PDF, GGB, GGT, Templatev7, author inbox ni artifacts de
desarrollo en app-image/JAR. Quedan fuera los 12 PDF de docs/references/cedg,
Templatev7, knowledge bundles y modelos legacy. Disposición: EXCLUDE con test
negativo permanente.

No existe un source-distribution producido por el pipeline. El repositorio Git
es público, pero el scope de un futuro source bundle no está aprobado.
Development docs no empaquetados quedan separados; las dos guías son la
excepción demostrada.

## 4. Findings por categoría

### Material GeoCeDG

El registro tiene 717 entradas: 621 added y 96 modified. Código, scripts,
tools, documentación y modelos propios no comparten una decisión pública. Root
LICENSE lo confirma. Publicar un repo no sustituye una licencia.[^13] Debe
decidirse por clase: código/modificaciones, docs, tools, assets/branding y
ejemplos/modelos. Esta auditoría no selecciona licencia.

### Upstream GeoGebra

El código dispone de ruta EUPL-1.2, sujeta a excepciones file-by-file. No se
extiende a traducciones ni 1.884 assets observados. Hay logos/marcas GeoGebra.
Candidatos: código ALLOW_WITH_NOTICE; translations/UI/marcas
REQUIRES_PERMISSION hasta que el autor elija scope, permission, replacement o
exclusion. Replacement/exclusion sería L4 productivo y no se ejecuta.

### Branding GeoCeDG

Inputs, cuatro derivados, herramienta, roles y hashes están registrados. El
ICO coincide con e5dac1dd3a556f4ce9747f00d272281e9a571ecc5e757180ba1c6750b664cd73.
La autorización existente es interna; no prueba derechos públicos.
Disposición: REQUIRES_PERMISSION.

### Dependencies

EchoSVG, CSS4J y varias APIs conservan textos internos. J2ObjC, ANTLR, Rhino y
SpotBugs tienen fuentes oficiales identificadas —Apache-2.0, ANTLR 2, MPL-2.0
y LGPL-2.1— pero el package no incluye sus textos.[^8][^9][^10] Giac,
JOGL/GlueGen, math-cross-platform, OpenGeoProver y jsobject son los casos más
débiles. JNA requiere elección explícita. Solo los términos claros son
candidatos ALLOW_WITH_NOTICE; el resto sigue UNKNOWN / BLOCKED.

### Deployability técnica

El build produce todos los formatos, pero verify-packaging rechazó el candidate:
REJECTED_VERIFICATION_CORE, coverage incompleta. El contrato
packaging.msi-native-association observó cero registros .cedg, MIME, ProgId,
open verb o target al launcher en el MSI descompilado. El perfil declara la
asociación; el output real no la contiene. Bloquea “technically ready” incluso
si licensing se resolviera.

## 5. Infraestructura existente

| Artefacto | Correcto | Desactualizado o incompleto |
|---|---|---|
| docs/licensing/component-matrix.md | separa code/translations/UI/fonts/terceros y bloquea | baseline 2026-08-09; source triage, no closure actual |
| assets-manifest.yml | hashes y derivación branding GeoCeDG | no enumera 1.884 assets upstream; excluir logos contradice common.jar; “owned EUPL asset” no es grant público |
| THIRD_PARTY.md | reconoce incompletitud | no lista componente/version/license/notice |
| NOTICE.md | identidad y baseline | no atribuye closure ni logos/translations |
| LICENSE / LICENSES | no-grant fail-closed correcto | sin licencia proyecto ni textos suficientes |
| SBOM | 52 paths/hashes exactos | 52 versiones unknown; sin coords/licencias/assets/fonts/runtime/nativos |
| builder/verificador | hashes, marker, whole-JAR filter, forbidden direct files | no ve nativos/resources internos; exclusiones demasiado amplias; MSI association falla |
| informe G4 | válido para su candidate histórico | no representa c2e15b3: branding y .cedg cambiaron; no reescribir |

Counts/hash/enrichment se pueden automatizar; autoría, permiso, elección de
licencia y disposición final exigen decisión humana.

## 6. Blockers

### Críticos

1. **D1-B01 — licencia GeoCeDG no decidida.** Código nuevo/modificado, guías
   distribuidas, tools y otros materiales propios no tienen grant público.
2. **D1-B02 — resources upstream distintos del código.** 390 translations y
   1.884 UI assets llegan al runtime; hay logos/marcas y términos no comerciales.
3. **D1-B03 — branding solo autorizado internamente.**
4. **D1-B04 — ReTeX/fonts incompletos.** 13 sin términos, textos ausentes y
   evidencia cirílica en conflicto.
5. **D1-B05 — dependencias opacas/nativas.** Giac, JOGL/GlueGen,
   math-cross-platform, OpenGeoProver, jsobject y alternativa JNA pendientes.
6. **D1-B06 — bundle legal/SBOM insuficiente.**
7. **D1-B07 — frontera Temurin/WiX por cerrar.**
8. **D1-B08 — MSI no registra .cedg.**

### Menores pero obligatorios para L5

- corregir semántica de exclusiones del manifest en una futura remediación;
- decidir/eliminar nativos no Windows anidados innecesarios;
- definir source-distribution;
- enriquecer coords/purl/licenses/nested components;
- mantener tests de exclusión;
- fijar URLs, tags/versions y checksums externos.

## 7. Hechos suficientemente caracterizados

- baseline, toolchain, package profile y cuatro outputs;
- hashes de ZIP/MSI/EXE, app-image y 52 JAR;
- branding GeoCeDG, inputs y derivación;
- marker interno y bloqueo fail-closed;
- exclusión de PDFs, Templatev7, GGB/GGT e inbox;
- runtime Temurin y árbol legal;
- textos internos EchoSVG/CSS4J/carte/W3C/JCLF/FlatLaf y parte de JNA;
- defecto técnico exacto de .cedg.

“Suficiente” significa hecho reproducible, no redistribución aprobada.

## 8. Incertidumbres y límites

- No se evaluó compatibilidad legal ni si un uso sería comercial.
- Cinco binarios del repo GeoGebra no ofrecen metadata legal suficiente.
- No se cerró el límite byte-exacto de código WiX en MSI/EXE.
- Repositorios antiguos de ReTeX/artefactos no ofrecieron ruta pública estable;
  prevalece evidencia local y bloqueo.
- No se auditó indiscriminadamente el monorepo: source-only, development-only,
  referencias y fixtures se separaron.

## 9. Siguiente paso D1 — no ejecutado

El autor debe resolver el
[docket](pre_g9b_d1_human_decision_docket.md) y autorizar un scope L4.
Después puede ejecutarse el
[plan candidato](pre_g9b_d1_remediation_plan_candidate.md): decisiones
propias/branding/composición upstream, textos y provenance de
dependencies/fonts, inventario enriquecido, corrección .cedg, rebuild y L5.
Este checkpoint no puede declarar D1 PASS.

## 10. Validación de investigación

| Comando/check | Exit | Evidencia |
|---|---:|---|
| git ls-remote --heads origin main | 0 | live SHA c2e15b3... |
| build-windows-package.ps1 -Target All | 0 tras repetición escalada | artifacts/packaging/windows/build-manifest.json |
| gradlew runtimeClasspath dependencies | 0 escalado | grafo resuelto vs 52 JAR |
| scan independiente JAR/runtime + SHA-256 | 0 | 52 JAR, 46 fonts, assets, nativos, 52 módulos/195 legales |
| verify-packaging.ps1 -CheckToolchain -RequireArtifacts | 1 | verification-2bd7f751c1084546896b739fb82f8170; rechazo .cedg; artifacts/pre-g9b-d1/verification/packaging/verification-result.json |
| correspondence audit JSON vs payload | 0 | 52/52 JAR, 46/46 fonts y 3/3 outputs; cero forbidden entries; artifacts/pre-g9b-d1/verification/audit-correspondence.json |
| relative Markdown link check | 0 | cero enlaces locales rotos; artifacts/pre-g9b-d1/verification/document-links.json |
| verify.ps1 -Profile STATIC | 0 | verification-ea5a1dd19c3b4ff1be0e373e2483dc1d; ACCEPTED / COMPLETE; artifacts/pre-g9b-d1/verification/static/verification-result.json |

STATIC conservó un finding diagnóstico de governance y una proyección
historical-consistency unavailable ya descritos por el protocolo de recovery;
no cambian aceptación. Las validaciones finales de git diff --check y ausencia
de product changes se ejecutan contra el commit candidato. No se ejecutó
PHASE/COMPOSED/FULL: el cambio autorizado es documentación/evidencia y
packaging ya demostró el blocker relevante.

**Capa afectada:** documentación de validación y evidencia de auditoría. No se
modifican kernel, semántica CeDG, application behavior, runtime composition,
packaging input, versión, defaults ni feature flags.

**Bootstrap impact:** NO_CHANGE. La investigación no cambia prerrequisitos,
toolchain, bootstrap scripts ni ninguna asunción consumida por workstation
setup; solo registra las versiones observadas. El nivel requerido fue STATIC
más el check canónico PACKAGING focalizado sobre outputs reales.

### 10.1 Erratum de continuación — asociación `.cedg`

La conclusión original de que el MSI no contenía la asociación queda
preservada como resultado histórico del checker, pero fue refutada por
evidencia posterior. El MSI auditado, SHA-256 `bbd69489b791ff307f904c6b625135db6a3e5753b30836b82159c3967a9cf948`,
sí materializa `.cedg`, MIME, ProgId, open verb y target `GeoCeDG.exe` como
filas de registro. WiX 5 proyecta esas tablas compiladas como `RegistryValue`
al descompilar, mientras el checker original solo buscaba nodos de authoring
`Extension/ProgId`.

La continuación D1 corrigió únicamente el verificador y su write-root temporal;
no cambió el generador ni el comportamiento del instalador. La disposición y
receipts actualizados están en el
[informe de continuación](pre_g9b_d1_licensing_disposition_and_bounded_remediation_candidate.md).

## 11. Autoridades y fuentes locales inspeccionadas

- AGENTS.md;
- docs/roadmap/geocedg_roadmap.md;
- docs/architecture/pre_g9b_stabilization_deployability_design.md;
- docs/licensing/component-matrix.md;
- LICENSE, LICENSES/README.md, NOTICE.md y THIRD_PARTY.md;
- geocedg/resources/assets-manifest.yml;
- docs/upstream/modified-files.yml;
- packaging/windows/package.yml, README.md,
  file-associations.properties e INTERNAL_EVALUATION_ONLY.txt;
- geocedg/specs/packaging/windows-packaging.md;
- geocedg/specs/ui/native-document-identity.md;
- geocedg/specs/operations/verification-levels.md y
  verification-registry.json;
- docs/adr/0004-standalone-windows-packaging.md y
  docs/adr/0016-native-geocedg-document-identity.md;
- docs/validation/g4_standalone_packaging_report.md;
- source/desktop/desktop/build.gradle.kts;
- source/shared/renderer-base/LICENSE y los textos locales de fonts en los
  renderers Desktop/Web;
- gradle/libs.versions.toml;
- tools/release/build-windows-package.ps1;
- tools/agent/verify-packaging.ps1,
  tools/agent/checks/packaging-product.ps1 y
  tools/bootstrap/packaging-prerequisites.psm1;
- build-manifest, SBOM, hash inventories, app-image, runtime/legal, ZIP y MSI
  descompilado generados para el baseline.

## Fuentes primarias externas

Consultadas el 14 de septiembre de 2026.

[^1]: [GeoGebra License oficial](https://www.geogebra.org/license).
[^2]: [Fuente oficial de términos en geogebra/legal](https://github.com/geogebra/legal/blob/main/geogebra_license.md).
[^3]: [Oracle JDK 25 — jpackage packaging overview](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html).
[^4]: [Eclipse Temurin 25 — releases](https://github.com/adoptium/temurin25-binaries/releases/).
[^5]: [OpenJDK/Adoptium — licencia](https://github.com/adoptium/jdk/blob/master/LICENSE).
[^6]: [WiX Toolset — LICENSE.TXT](https://github.com/wixtoolset/wix/blob/main/LICENSE.TXT).
[^7]: [JNA — repositorio y alternativas](https://github.com/java-native-access/jna).
[^8]: [Mozilla Rhino — licencia](https://github.com/mozilla/rhino/blob/master/LICENSE.txt).
[^9]: [ANTLR 2 — licencia](https://www.antlr2.org/license.html).
[^10]: [Google J2ObjC — licencia](https://github.com/google/j2objc/blob/master/LICENSE).
[^11]: [GlueGen](https://github.com/sgothel/gluegen) y [JOGL](https://github.com/sgothel/jogl).
[^12]: [Giac/Xcas — sitio oficial](https://www-fourier.ujf-grenoble.fr/~parisse/giac.html).
[^13]: [GitHub Docs — repositorio sin licencia](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/licensing-a-repository).
