# Cierre de mantenimiento de las fuentes de empaquetado Windows

**WINDOWS PACKAGING SOURCE RESTORATION — TECHNICALLY ACCEPTED — DOCUMENTED — PENDING PUBLICATION**

El 13 de noviembre de 2026 el autor aceptó la reparación y las evidencias existentes. La eliminación accidental de `packaging/windows/` en `9376307f2d3da6d14799608c4ecc48adc4ed8d41` retiró fuentes que seguían consumiendo el empaquetado Windows y la configuración de Gradle Desktop. Se restauró la configuración preexistente como mantenimiento de infraestructura. La actuación no incorpora funcionalidad ni abre una fase del roadmap.

## Alcance aceptado

Rama `codex/restore-windows-packaging-sources`, commit `2737b4d5e2d503d984b5dc74234a2169c8d015ea`, árbol `1e454aa2004fe1616ccb3fbcf98bd74bde111fb6`. Su padre directo es el baseline indicado arriba. Git confirmó que las únicas adiciones del commit son estos archivos, con contenido y modo idénticos a los de `0f4f861548fd11bfa0228dd41486c26e95275eee`.

- `packaging/windows/INTERNAL_EVALUATION_ONLY.txt`
- `packaging/windows/NC_DISTRIBUTION_NOTICE.txt`
- `packaging/windows/NuGet.Config`
- `packaging/windows/file-associations-nc.properties`
- `packaging/windows/file-associations.properties`
- `packaging/windows/package.yml`

El séptimo archivo eliminado, `packaging/windows/README.md`, era documentación del subdirectorio y quedó fuera del alcance de la restauración. Es la única diferencia entre el árbol de restauración y el árbol de procedencia. El cierre añade únicamente este registro y su archivo de evidencias en un commit documental separado.

## Evidencia aceptada

| Ejecución existente | Resultado y registros |
| --- | --- |
| `tools/agent/verify.ps1 -Profile PACKAGING` | Salida **0**, `ACCEPTED / COMPLETE`, **3/3** contratos de aceptación y cero hallazgos. Ejecución `verification-c3dff29f2ecd4358b55ab02cd4e44ea9` sobre el commit y árbol indicados. Registros `packaging-clean-checkout.log` y `checkout/artifacts/agent/packaging/verification-result.json`. |
| `gradlew.bat :desktop:desktop:compileJava`, mediante `tools/agent/checks/gradle-command-evidence-producer.ps1` | Salida de Gradle **0**, `BUILD SUCCESSFUL`, **23 tareas ejecutadas**. Registros `desktop/compile-desktop.json`, `desktop/stdout.log` y `desktop/stderr.log`. |

Ambas ejecuciones se hicieron sobre una copia limpia del commit aceptado. Los registros originales se guardaron bajo `C:/Users/usuario/AppData/Local/Temp/geocedg-restore-packaging-3cb96b2666e1/`, con la consola de PACKAGING fuera de esa copia. El [archivo de evidencias](evidence/windows-packaging-source-restoration-2737b4d5e.zip) conserva los archivos originales byte por byte, con sus rutas relativas y las salidas de cada comprobación de PACKAGING. SHA-256 del archivo `18f9a51a0b73ac2732b90dda64f0bbfad4ebb2b1390f5ebf942af468205b5dbf`.

`GUIDE_IMPACT = NONE`. Se recuperan las entradas ya descritas por las guías. **Bootstrap** — no requiere cambios. El perfil restaurado recupera la configuración que ya consumía y no cambia versiones ni prerrequisitos.

El cierre se basó en la inspección de Git y de los resultados guardados. No se ejecutaron nuevas verificaciones. La publicación sigue pendiente de autorización expresa del autor.
