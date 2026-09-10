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

## Flujo normal para un clon existente en Windows

Ejecute estos comandos desde la raíz del repositorio con PowerShell 7.2 o
posterior. Antes de actualizar, inspeccione cualquier trabajo local; no lo
descarte ni lo mezcle implícitamente con la actualización:

```powershell
git status --short
git switch main
git pull --ff-only
```

Se requiere Git, PowerShell 7.2 o posterior, un JDK 22 para ejecutar Gradle y JDK completos
17 (compilación y tests) y 25 (Desktop), disponibles como toolchains. El wrapper
selecciona Java desde `JAVA_HOME` cuando está definido; sólo usa `PATH` si está
vacío o ausente. Un `JAVA_HOME` inválido no se sustituye silenciosamente por
`PATH`; `java` debe estar disponible en `PATH` también para el diagnóstico de
baseline. La verificación numérica requiere Conda y el entorno GeoCeDG
`cedg_env`, con CPython **3.12.13** y mpmath **1.4.1**; un Python global no lo
sustituye. Conda forma parte exclusivamente de la infraestructura operativa de
verificación: la compilación Java y la ejecución del producto no dependen de
Conda cuando no se solicitan comprobaciones Python. No es necesario instalar
Gradle: se usa exclusivamente el wrapper del repositorio. Windows es la única
plataforma validada actualmente.

Desde la raíz del repositorio, cree el entorno por primera vez con:

```powershell
conda env create --file .\cedg_env.yml
```

Actualice una instalación existente, eliminando dependencias ajenas al contrato,
con:

```powershell
conda env update --name cedg_env --file .\cedg_env.yml --prune
```

Compruebe la implementación, las versiones y los orígenes de importación:

```powershell
conda run --no-capture-output -n cedg_env python -c "import json,platform,sys,mpmath; print(json.dumps({'implementation':platform.python_implementation(),'python':platform.python_version(),'executable':sys.executable,'prefix':sys.prefix,'mpmath':mpmath.__version__,'mpmath_file':mpmath.__file__}))"
```

El resultado debe identificar exactamente CPython `3.12.13` y mpmath `1.4.1`;
`executable`, `prefix` y `mpmath_file` deben pertenecer al mismo prefijo de
`cedg_env`. La comprobación canónica, real y obligatoria tras la instalación es:

```powershell
.\tools\agent\verify.ps1 -Profile WORKSTATION
```

El comando público de compatibilidad es:

```powershell
.\tools\agent\verify-workstation.ps1
```

Ambos resuelven el mismo plan y el mismo contrato live. Validan Gradle 9.4.1,
Java 22 para el launcher, JDK completos 17 y 25, `cedg_env`, CPython 3.12.13,
mpmath 1.4.1 y la procedencia de ejecutable, prefijo e importación. WORKSTATION
no compila el producto ni ejecuta fixtures o diagnósticos de gobernanza.

Las instrucciones anteriores usaban el entorno externo `om_env`. Cree y use
`cedg_env`; no renombre, actualice, pode ni elimine `om_env` como parte de esta
migración.

Si `cedg_env` no satisface el contrato, no lo elimine sólo porque su nombre
coincida. Resuelva y valide primero su nombre, prefijo absoluto, prefijo Python,
origen del ejecutable, pertenencia al inventario Conda y separación de `base`:

```powershell
$probeJson = conda run -n cedg_env python -c `
  "import json,os,sys; print(json.dumps({'environment_name':os.environ.get('CONDA_DEFAULT_ENV',''),'environment_prefix':os.environ.get('CONDA_PREFIX',''),'python_prefix':sys.prefix,'python_executable':sys.executable}))"
if ($LASTEXITCODE -ne 0) {
    throw "No se puede resolver cedg_env con seguridad; no se eliminará ningún entorno."
}

$facts = $probeJson | ConvertFrom-Json
$rawEnvironmentPrefix = [string]$facts.environment_prefix
$rawPythonPrefix = [string]$facts.python_prefix
$rawPythonExecutable = [string]$facts.python_executable
if ([string]::IsNullOrWhiteSpace($rawEnvironmentPrefix) -or
    [string]::IsNullOrWhiteSpace($rawPythonPrefix) -or
    [string]::IsNullOrWhiteSpace($rawPythonExecutable) -or
    -not [IO.Path]::IsPathFullyQualified($rawEnvironmentPrefix) -or
    -not [IO.Path]::IsPathFullyQualified($rawPythonPrefix) -or
    -not [IO.Path]::IsPathFullyQualified($rawPythonExecutable)) {
    throw "La sonda no devolvió rutas absolutas; no se eliminará ningún entorno."
}
$resolvedPrefix = [IO.Path]::GetFullPath(
    $rawEnvironmentPrefix).TrimEnd('\', '/')
$pythonPrefix = [IO.Path]::GetFullPath(
    $rawPythonPrefix).TrimEnd('\', '/')
$pythonExecutable = [IO.Path]::GetFullPath(
    $rawPythonExecutable)
$basePrefix = [IO.Path]::GetFullPath(
    ((conda info --base).Trim())).TrimEnd('\', '/')
$condaInventory = conda env list --json | ConvertFrom-Json -AsHashtable
$knownPrefixes = @(
    $condaInventory['envs'] |
        ForEach-Object {
            [IO.Path]::GetFullPath([string]$_).TrimEnd('\', '/')
        }
)
$prefixBoundary = $resolvedPrefix + [IO.Path]::DirectorySeparatorChar
$listedExactlyOnce = @($knownPrefixes | Where-Object {
    $_.Equals($resolvedPrefix, [StringComparison]::OrdinalIgnoreCase)
}).Count -eq 1
$validIdentity =
    $facts.environment_name -ceq 'cedg_env' -and
    $pythonPrefix.Equals($resolvedPrefix,
        [StringComparison]::OrdinalIgnoreCase) -and
    $pythonExecutable.StartsWith($prefixBoundary,
        [StringComparison]::OrdinalIgnoreCase) -and
    $listedExactlyOnce -and
    -not $resolvedPrefix.Equals($basePrefix,
        [StringComparison]::OrdinalIgnoreCase)
if (-not $validIdentity) {
    throw "La identidad o el origen de cedg_env es incoherente; no se eliminará ningún entorno."
}

Write-Host "Prefijo cedg_env verificado: $resolvedPrefix"
conda env remove --prefix $resolvedPrefix --yes
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo eliminar el prefijo cedg_env verificado."
}
conda env create --file .\cedg_env.yml
```

Si la sonda de identidad no puede ejecutarse, deténgase e inspeccione
manualmente el inventario Conda: no deduzca el prefijo ni elimine otro entorno.
La guía de usuario contiene el procedimiento ampliado de instalación,
recuperación y diagnóstico.

El bootstrap es una operación avanzada y separada de preparación/preflight.
Puede inspeccionar prerrequisitos, remotos y el tag fijado, pero no compila,
lanza el producto ni ejecuta aceptación o gobernanza implícitamente. No es el
verificador live de la instalación. Consulte
`Get-Help .\tools\bootstrap\bootstrap-windows.ps1 -Detailed` sólo para ese flujo.

Cada ejecución guarda `bootstrap-transcript.log`, `bootstrap-result.json` y
los logs de preflight en una carpeta única bajo
`%TEMP%\geocedg-bootstrap`. `-LogDirectory <directorio>` cambia sólo ese
directorio padre común, no reutiliza una ejecución anterior. Antes de crear
logs se rechazan rutas bajo `build`, `.gradle`, `.kotlin`, copias temporales de
estado generado y ancestros enlazados. Los diagnósticos
describen el proceso y perfil actuales: la ausencia de una herramienta en un
sandbox no demuestra su ausencia en el host. Ante un fallo, consulte la etapa,
clasificación y logs antes de atribuirlo al producto o cambiar el entorno.

La instalación de requisitos de packaging es una acción separada y explícita
con `-InstallPackagingPrerequisites`: ejecuta únicamente el instalador focalizado
y termina, sin `fetch`, builds ni verificaciones G3/G5. La verificación se
ejecuta después, de forma independiente, mediante un perfil explícito de
`tools/agent/verify.ps1`.

## Verificación, compilación y ejecución

Compruebe la instalación, compile y ejecute por separado:

```powershell
.\tools\agent\verify.ps1 -Profile WORKSTATION
.\gradlew.bat :desktop:desktop:compileJava
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

La compilación y la ejecución Java no necesitan Conda. La configuración actual
no instala toolchains automáticamente: Java 22 y los JDK completos 17 y 25
deben estar disponibles en la máquina.

La autoridad informa rama (o `detached HEAD`), commit y última fase incluida
según el roadmap versionado. Valida el checkout actual con los mismos gates en
`main`, ramas de trabajo y `detached HEAD`; el nombre de rama es diagnóstico.
Las precondiciones históricas de G7 sólo se aplican al solicitar explícitamente
`verify-g7a-metrics.ps1 -ReproduceCharacterization` o
`verify-g7b-metrics.ps1 -ReproduceImplementation`.

Los perfiles canónicos son `STATIC`, `INFRA_UNIT`, `WORKSTATION`, `OPERATIONAL`,
`DEV`, `PHASE`, `INTEGRATION` y `FINAL`. Los alias heredados se conservan como
adaptadores; `COMPOSED` selecciona `INTEGRATION` y `FULL`/`FullTests` seleccionan
`FINAL`. Un perfil con cobertura incompleta falla de forma explícita y nunca
delega en un wrapper mixto heredado.

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

Ambos arranques son gráficos. Bootstrap no lanza ninguno; la verificación
normal tampoco abre ventanas. El contrato del
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

El perfil de packaging inspecciona estos componentes sin instalarlos. La
preparación opt-in, idempotente y recomendada de .NET/WiX es:

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
