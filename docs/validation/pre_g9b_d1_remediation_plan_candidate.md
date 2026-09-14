# PRE-G9B-D1 — remediation plan candidate

Estado: **CANDIDATE — BOUNDED VERIFIER REMEDIATION EXECUTED; PRODUCT REMEDIATION PENDING**

Este plan parte del
[informe original](pre_g9b_d1_licensing_assets_deployability_research_audit.md),
el [informe de disposición](pre_g9b_d1_licensing_disposition_and_bounded_remediation_candidate.md)
y el [docket reducido](pre_g9b_d1_human_decision_docket.md). La única
remediación ejecutada fue corregir dos defectos del verificador MSI: lectura de
la proyección `RegistryValue` de WiX 5 y confinamiento de su evidencia temporal.
No cambió producto, licencia, assets, runtime, asociación instalada ni fase.

## Checkpoint de continuación

- `.cedg`: **RESUELTO TÉCNICAMENTE**. El MSI anterior ya registraba la
  asociación; el audit conservado contenía un falso negativo. PACKAGING final
  es `ACCEPTED / COMPLETE`.
- Dependencies: 36/40 JAR externos pueden retenerse con los notices/textos
  registrados. Giac, math-cross-platform, OpenGeoProver y jsobject siguen
  bloqueados.
- Fonts: 42/46 dispositionadas; cuatro requieren grant exacto o replacement.
- Runtime/tooling: Temurin y el payload WiX quedan retenibles con sus textos y
  source-access obligations.
- Decisiones humanas: D1-HD-01 a D1-HD-04. El resto se retiró del docket.

## Principios de ejecución futura

- No comenzar L4 hasta que cada decisión aplicable D1-HD tenga respuesta
  explícita.
- Congelar commit, package profile, runtime y hashes de la composición elegida.
- Cambiar la declaración/generador, no artefactos generados.
- Separar remediation legal-documental, composición productiva y defecto de
  deployability.
- Mantener internal-evaluation, version 0.9.0, defaults y feature flags hasta
  una autorización distinta.
- Conservar informes históricos sin reescritura.

## Orden candidato

### R1. Fijar decisiones y scope

Entradas: D1-HD-01 a D1-HD-04.

Cambios mínimos futuros: un registro autoritativo de decisión por clase, scope
binario/source y outputs permitidos. No incorporar todavía textos o assets.

Automatizable: validación de identificadores, paths y flags de aprobación.

Humano: licencias, permisos, branding, marcas, disposición y autorización L4.

Stop: cualquier clase distribuida sin decisión.

### R2. Cerrar inventario técnico

Enriquecer el inventario existente para que derive del runtimeClasspath y del
payload real:

- coordenada, versión, purl y hash de cada JAR;
- hashes de entries nativos;
- assets, translations y fonts embebidos;
- módulos y árbol legal Java;
- payload/directories de app-image/ZIP/MSI/EXE;
- correspondencia source/tag/checksum para binarios opacos.

Cambios mínimos: ampliar el generador/SBOM existente solo si se autoriza; no
crear un segundo pipeline paralelo.

Automatizable: extracción, hashing, diff de closure y tests negativos.

Humano: resolver falsos agrupamientos, autoría y términos ambiguos.

Stop: dependencia declarada/resuelta/staged sin correspondencia explícita.

### R3. Ejecutar disposición upstream/assets

Según D1-HD-02/D1-HD-03:

- allowlist exacta de recursos conservados;
- replacements GeoCeDG-owned autorizados;
- exclusions en la fuente del packaging/build;
- eliminación de originals de branding del runtime si el autor lo decide;
- tests que detecten logos, marks y assets prohibidos dentro de JAR.

Cambios mínimos: solo recursos y wiring necesarios; sin refactor de kernel.

Automatizable: hashes, allowlist/denylist, entry scans.

Humano: permiso y aceptación visual/branding.

Validación: launch smoke, UI/resource tests, hashes y absence proofs.

### R4. Cerrar fonts/renderers y dependencias

Añadir al bundle solo textos/notices aprobados y vincular cada componente al
source exacto. Para UNKNOWN / BLOCKED, obtener evidencia, reemplazar o excluir
según autorización.

Si cambian fonts, renderer, CAS, 3D o proving, abrir validación funcional
específica; no tratarlo como edición documental.

Automatizable: generación de LICENSES/THIRD_PARTY desde allowlist, detección de
textos ausentes y version drift.

Humano: alternativa JNA, términos no estándar, compatibility y permissions.

### R5. Runtime Java e instaladores

- fijar URL/tag/checksum/source de Temurin;
- verificar que runtime/legal permanece íntegro;
- analizar payload WiX byte-exacto y aplicar la decisión D1-HD-07;
- reducir nativos cross-platform anidados solo si se autoriza y es seguro;
- conservar el checker de asociación `.cedg` contra el modelo MSI compilado;
- añadir MS-RL/source access para el payload WiX incorporado.

Validación mínima: app-image/ZIP launch, MSI/EXE install/uninstall, asociación,
upgrade/cleanup si aplica, hash correspondence.

### R6. Rebuild y L5

Desde candidate limpio e inmutable:

1. construir app-image, ZIP, MSI y EXE;
2. producir closure/SBOM enriquecida y hashes;
3. comparar declaration, resolved graph y staged payload;
4. validar todos los notices/textos y absence lists;
5. ejecutar verificación canónica PACKAGING/WORKSTATION requerida;
6. ejecutar el perfil D1 que la autoridad registre, sin redefinir FINAL;
7. generar candidate report y detenerse para revisión humana.

Resultado técnico máximo antes de aprobación jurídica/branding:

~~~text
DEPLOYABILITY TECHNICALLY READY — PENDING HUMAN LEGAL/BRANDING APPROVAL
selfApproved=false
authorApproved=false
passClaimed=false
~~~

## Archivos potenciales — no autorizados aquí

Una futura autorización podría afectar de forma mínima:

- LICENSE, LICENSES/**, NOTICE.md, THIRD_PARTY.md;
- geocedg/resources/assets-manifest.yml;
- packaging/windows/package.yml y file-associations.properties;
- tools/release/build-windows-package.ps1 y verificadores existentes;
- build resource inclusion/exclusion del módulo Desktop;
- resources reemplazados explícitamente;
- specs/report/roadmap D1.

Kernel, semántica CeDG, defaults, feature flags, P1 y version 1.0 siguen fuera de
alcance salvo autorización posterior distinta.

## Matriz de verificación futura

| Riesgo | Evidencia requerida |
|---|---|
| closure drift | dos extracciones deterministas con mismo hash y candidate |
| dependencia omitida | runtimeClasspath = staged allowlist |
| nested native/asset | entry-level inventory y policy |
| texto/notice ausente | cada componente apunta a texto packaged o excepción aprobada |
| marca upstream | scan de paths/hashes y revisión humana |
| excluded material | cero PDF/GGB/GGT/inbox/knowledge bundle |
| Java runtime | versión/checksum/módulos/runtime legal |
| MSI/EXE | decompile + install/uninstall + .cedg |
| cambio accidental de producto | diff limitado y pruebas de comportamiento |
| publicación prematura | markers/approval flags fail-closed |

No ejecutar P1, PHASE científico, COMPOSED, FULL, tag, merge a main ni
publicación como consecuencia automática de este plan.
