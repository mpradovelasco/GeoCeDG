# GeoCeDG

GeoCeDG es un fork independiente, basado en fuentes, de GeoGebra Classic 5
para Computer-Extended Descriptive Geometry (CeDG). Busca preservar la
trazabilidad constructiva, las dependencias dinámicas y la coherencia entre
objetos espaciales y sus proyecciones; no es un producto oficial de GeoGebra.

El proyecto está en desarrollo experimental. G0 fijó y validó el baseline,
G1/G1R establecieron la capa operativa y G2 incorpora el primer perfil Desktop
propio. Todavía no se han importado herramientas CeDG ni se ha modificado la
semántica geométrica.

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

Se requiere Git, PowerShell 7, un JDK 22 para ejecutar Gradle y un JDK 25
disponible como toolchain para arrancar Desktop. No es necesario instalar
Gradle: se usa exclusivamente el wrapper del repositorio. Windows es la única
plataforma validada actualmente.

```powershell
git clone https://github.com/mpradovelasco/GeoCeDG.git
cd GeoCeDG
.\tools\bootstrap\bootstrap-windows.ps1
```

El bootstrap comprueba el workstation, configura `upstream` sólo si falta,
verifica el tag fijado y delega las puertas del repositorio en
`tools/agent/verify.ps1`. Es idempotente y no instala software. Consulte su
ayuda con `Get-Help .\tools\bootstrap\bootstrap-windows.ps1 -Detailed`.

## Verificación, compilación y ejecución

Autoridad compuesta local:

```powershell
.\tools\agent\verify.ps1
```

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
