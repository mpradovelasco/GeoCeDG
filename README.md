# GeoCeDG

GeoCeDG es un fork independiente, basado en fuentes, de GeoGebra Classic 5
para Computer-Extended Descriptive Geometry (CeDG). Busca preservar la
trazabilidad constructiva, las dependencias dinámicas y la coherencia entre
objetos espaciales y sus proyecciones; no es un producto oficial de GeoGebra.

El proyecto está en desarrollo experimental. G0 fijó y validó el baseline,
G1/G1R establecieron la capa operativa, G2 incorporó el primer perfil Desktop
propio y G3 preserva y cataloga de forma controlada los recursos CeDG legacy.
G4 añadió packaging Windows interno y G5 una primera exportación geométrica 2D
DXF experimental. G6 añadió una entidad Locus V2 semántica paralela y G6R la
endureció con un laboratorio developer-only. G7 cerró métricas semánticas
internas y G8 cerró intersecciones internas tipadas, ambas autor-aprobadas.
G9P-R1, G9P y G9O1 son `PASS — AUTHOR APPROVED`; sus seis especificaciones son
normativas y ADR 0010–0015 están Accepted. El estado vigente de G9 y sus
autorizaciones se consulta en la [hoja de ruta](docs/roadmap/geocedg_roadmap.md);
el alcance y la activación de Locus V2 se describen en el
[manual operativo](docs/user/geocedg_user_guide.md#can-i-use-locus-v2-now).
Ningún recurso legacy se carga por defecto.

## Baseline

- GeoGebra: `5.4.928.0`
- commit upstream: `9b93256b7df401ff056c37b502d82df4d72b1522`
- tag: `geogebra-baseline-5.4.928.0`

La procedencia, el build y el toolchain validados se documentan en
[UPSTREAM.md](UPSTREAM.md). El README original de ese tag se conserva sin
alteraciones en [docs/upstream/GEOGEBRA_README.md](docs/upstream/GEOGEBRA_README.md).
Su selector raíz `:desktop:run` está desactualizado para el composite build
fijado; la discrepancia y las rutas correctas están documentadas en
[el mapa de módulos](docs/architecture/upstream_module_map.md).

## Primer arranque en Windows

Se requiere Git, PowerShell 7.2 o posterior, un JDK 22 para ejecutar Gradle y JDK completos
17 (compilación y tests) y 25 (Desktop), disponibles como toolchains. El wrapper
selecciona Java desde `JAVA_HOME` cuando está definido; sólo usa `PATH` si está
vacío o ausente. Un `JAVA_HOME` inválido no se sustituye silenciosamente por
`PATH`; `java` debe estar disponible en `PATH` también para el diagnóstico de
baseline. La verificación numérica requiere Conda y el entorno nombrado
`om_env`, con CPython **3.12.13** y mpmath **1.4.1**; un Python global no lo
sustituye. No es necesario instalar Gradle: se usa exclusivamente el wrapper
del repositorio. Windows es la única plataforma validada actualmente.

```powershell
git clone https://github.com/mpradovelasco/GeoCeDG.git
cd GeoCeDG
.\tools\bootstrap\bootstrap-windows.ps1
```

El bootstrap configura `upstream` sólo si falta, verifica el tag fijado y,
antes de la verificación costosa del producto, comprueba el Java efectivo, los
JDK 17/25 y las versiones y el origen de Python/mpmath dentro de `om_env`.
Delega después en `tools/agent/verify.ps1`, cuyo nivel predeterminado es
COMPOSED, no FULL; véase el [contrato de niveles](geocedg/specs/operations/verification-levels.md).
Por defecto es idempotente y no instala software. Consulte su ayuda con
`Get-Help .\tools\bootstrap\bootstrap-windows.ps1 -Detailed`.

Cada ejecución guarda `bootstrap-transcript.log`, `bootstrap-result.json` y
los logs de `preflight/` y `verification/` en una carpeta única bajo
`%TEMP%\geocedg-bootstrap`. `-LogDirectory <directorio>` cambia sólo ese
directorio padre común, no reutiliza una ejecución anterior. Antes de crear
logs se rechazan rutas bajo `build`, `.gradle`, `.kotlin`, copias temporales de
estado generado y ancestros enlazados. Los diagnósticos
describen el proceso y perfil actuales: la ausencia de una herramienta en un
sandbox no demuestra su ausencia en el host. Ante un fallo, consulte la etapa,
clasificación y logs antes de atribuirlo al producto o cambiar el entorno.

La instalación de requisitos de packaging es una acción separada y explícita
con `-InstallPackagingPrerequisites`: ejecuta únicamente el instalador focalizado
y termina, sin `fetch`, builds ni verificaciones G3/G5. La aceptación del
repositorio se ejecuta después, de forma independiente, con
`tools/agent/verify.ps1`; use `-Level FULL` cuando lo exija el contrato de niveles.

## Verificación, compilación y ejecución

Autoridad compuesta local (COMPOSED por defecto):

```powershell
.\tools\agent\verify.ps1
```

La autoridad informa rama (o `detached HEAD`), commit y última fase incluida
según el roadmap versionado. Valida el checkout actual con los mismos gates en
`main`, ramas de trabajo y `detached HEAD`; el nombre de rama es diagnóstico.
Las precondiciones históricas de G7 sólo se aplican al solicitar explícitamente
`verify-g7a-metrics.ps1 -ReproduceCharacterization` o
`verify-g7b-metrics.ps1 -ReproduceImplementation`.

Para la cobertura exhaustiva de tests compartidos y Desktop, además de las
puertas compuestas, use `tools/agent/verify.ps1 -Level FULL` (`-FullTests` es un
alias). DEV/PHASE tienen alcance acotado; `-SkipBuild` sólo aporta evidencia
estática y de toolchains, nunca aceptación COMPOSED/FULL. Las condiciones que
requieren FULL figuran en el [contrato de niveles](geocedg/specs/operations/verification-levels.md).

Compilación y arranque GeoCeDG desde la raíz:

```powershell
.\gradlew.bat :desktop:desktop:compileJava
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

El Desktop GeoGebra Classic 5 del baseline permanece como ruta explícita de
diagnóstico y regresión:

```powershell
.\gradlew.bat :desktop:desktop:run
```

Ambos arranques son gráficos. El bootstrap conserva `-LaunchDesktop` para el
baseline Classic; la verificación normal no abre ventanas. El contrato del
perfil, su perspectiva y su toolbar se encuentran en
[la especificación de aplicación](geocedg/specs/ui/application-profile.md).

El CeDG Laboratory abre de forma explícita un recurso no estable registrado:

```powershell
.\tools\legacy\open-laboratory.ps1
.\tools\legacy\open-laboratory.ps1 -Classic
```

La botonera contenida en `Templatev7.ggb` se conserva como organización legacy
de referencia; no sustituye ni redefine la toolbar estable de G2. El flujo de
ingest y promoción se documenta en
[la especificación de integración legacy](geocedg/specs/legacy/controlled-integration.md).

La exportación experimental de geometría 2D se invoca desde
`GeoCeDG > Export 2D geometry as DXF (experimental)...`. Su alcance exacto,
unidades, entidades soportadas y warnings se documentan en
[el manual operativo](docs/user/geocedg_user_guide.md#8-export-2d-geometry-to-dxf).

El alcance vigente de comandos, persistencia, interacción y limitaciones de
Locus V2 se documenta en el
[manual operativo](docs/user/geocedg_user_guide.md#can-i-use-locus-v2-now).
El laboratorio G6R conserva una ruta de desarrollo aislada:

```powershell
.\tools\locus-v2\open-locus-v2-laboratory.ps1 -ValidateOnly
.\tools\locus-v2\open-locus-v2-laboratory.ps1
```

El laboratorio usa preferencias temporales, no aparece en el arranque normal
ni en Classic y no puede guardar su construcción como `.ggb`. Consulte el
[manual operativo](docs/user/geocedg_user_guide.md#can-i-use-locus-v2-now) y la
[arquitectura G6R](docs/architecture/locus_v2_implementation.md).

## Requisitos de packaging Windows

El `app-image` y ZIP requieren el JDK 25 Desktop completo y su `jpackage`.
MSI/EXE requieren además un SDK .NET 6 o posterior y WiX Toolset 5.0.2 con sus
extensiones Util/UI 5.0.2. G4 se validó con Temurin 25.0.4, .NET SDK 8.0.303 y
WiX `5.0.2+aa65968c`.

```powershell
.\gradlew.bat -q javaToolchains
& "<JDK25>\bin\jpackage.exe" --version
dotnet --info
wix --version
wix extension list -g
```

El bootstrap normal detecta estos componentes sin instalarlos. La instalación
opt-in, idempotente y recomendada de .NET/WiX es:

```powershell
.\tools\bootstrap\bootstrap-windows.ps1 -InstallPackagingPrerequisites
.\tools\agent\verify-packaging.ps1 -CheckToolchain
```

La primera orden no ejecuta la verificación del repositorio y nunca instala el
JDK. Como alternativa manual equivalente para .NET/WiX:

```powershell
winget install --id Microsoft.DotNet.SDK.8 --exact
dotnet tool install --global wix --version 5.0.2 `
  --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources
Push-Location .\packaging\windows
wix extension add -g WixToolset.Util.wixext/5.0.2
wix extension add -g WixToolset.UI.wixext/5.0.2
Pop-Location
```

Generación técnica:

```powershell
.\tools\release\build-windows-package.ps1 -Target AppImage
.\tools\release\build-windows-package.ps1 -Target All
.\tools\agent\verify-packaging.ps1 -CheckToolchain -RequireArtifacts
```

Todos los binarios G4 son `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`.
La capacidad técnica está validada, pero la redistribución pública continúa
bloqueada. Véanse [ADR 0004](docs/adr/0004-standalone-windows-packaging.md) y
[el contrato de packaging](geocedg/specs/packaging/windows-packaging.md).

## Repositorio y flujo de trabajo

- `origin` es el repositorio GeoCeDG; el bootstrap lo inspecciona y nunca lo
  modifica.
- `upstream` debe apuntar exactamente a
  `https://github.com/geogebra/geogebra.git` y se usa para procedencia y
  sincronizaciones controladas.
- `main` contiene estados GeoCeDG aprobados. El trabajo se hace en ramas
  `feature/<nombre>`; una actualización upstream se aísla en
  `sync/geogebra-YYYYMMDD` y pasa por revisión y verificación.
- No se reescribe historia compartida ni se mezclan cambios upstream con una
  feature.

Puntos de entrada del repositorio:

- [AGENTS.md](AGENTS.md): contrato obligatorio para personas y agentes.
- [GeoCeDG — Living Technical Roadmap](docs/roadmap/geocedg_roadmap.md): fases,
  puertas y estado consolidado del proyecto.
- [docs/](docs/): arquitectura, ADR, roadmap, validación, upstream y licencias.
- [tools/](tools/): bootstrap, verificación, benchmark y futuras herramientas
  reproducibles.
- [.github/prompts/](.github/prompts/) y
  [ai-shell/prompts/](ai-shell/prompts/): prompts canónicos y perfiles breves.
- [geocedg/features/](geocedg/features/),
  [geocedg/specs/](geocedg/specs/) y [models/manifests/](models/manifests/):
  manifests, schemas y especificaciones; no son código geométrico generado.

Las decisiones operativas están en
[ADR 0002](docs/adr/0002-g1-operational-authority.md), las propuestas de
arquitectura en [docs/architecture/](docs/architecture/) y la evidencia de
validación en [docs/validation/](docs/validation/).

## Licencias y marcas

No se debe asumir una licencia única para código, traducciones, documentación,
branding, iconos, estilos, instaladores o servicios heredados. GeoCeDG no debe
redistribuir marcas ni el instalador upstream sin revisión. Véanse
[la matriz de componentes y sus asuntos pendientes](docs/licensing/component-matrix.md)
antes de preparar cualquier distribución.
