# PRE-G9B-D1 — licensing disposition and bounded remediation candidate

Estado: **CANDIDATE — PENDING AUTHOR REVIEW**

~~~text
selfApproved=false
authorApproved=false
passClaimed=false
publicRedistributionAllowed=false
~~~

Este informe continúa, sin reemplazar, el checkpoint factual inmutable
`3267436dcc7dcf0d7c7bc30b63cb2159f2992f85` (tree
`05fe03cbb5069c7a6c4b0b50b550ddc3d55e1905`). No es asesoramiento legal:
separa evidencia de licencia, compatibilidad técnica, decisión de negocio y
cuestiones que requieren revisión humana o profesional.

La evidencia machine-readable es el
[overlay de disposiciones](../../geocedg/validation/pre-g9b-d1/component-disposition.json),
que se une por `component_id` a la
[matriz original](../../geocedg/validation/pre-g9b-d1/component-audit.json).

## 1. Baseline y método

- Public main: `c2e15b3d09a921442108ecb6b5891b4d6211a3ae`.
- Audit aceptado: `3267436dcc7dcf0d7c7bc30b63cb2159f2992f85`.
- Rama de continuación: `fix/pre-g9b-d1-disposition-packaging`.
- Consulta externa: 15 de septiembre de 2026.
- Se conservaron la closure y hashes del audit. Solo se repitieron pruebas donde
  apareció evidencia nueva: asociación MSI, fonts, binarios opacos, Temurin y
  frontera WiX.
- Para cada componente se examinó el artefacto exacto distribuido, su manifest,
  textos embebidos, POM oficial, tag/release/source primario y, cuando fue
  posible, correspondencia de hash.
- Una licencia de proyecto no se proyectó sobre fonts, assets o binarios
  nativos sin una asignación explícita.

## 2. Resultado ejecutivo por perfil

### PROFILE NC

Es técnicamente viable como producto público **no comercial**, pero no está
listo ni autorizado. Si conserva las 390 traducciones y las imágenes/styles UI
de GeoGebra, el producto completo queda sujeto a los términos no comerciales
de GeoGebra y no debe describirse como “Open Source” o “Free Software”. El
código fuente EUPL sí puede describirse separadamente como open source.[^1][^2]

Antes de producir un candidato NC deben resolverse:

1. licencia elegida por el autor para código/docs/artwork GeoCeDG;
2. bundle completo de textos, notices, atribución y acceso a sources;
3. cuatro JAR externos sin grant/provenance exactos: Giac 70501,
   math-cross-platform 3.6.3, OpenGeoProver 20120725 y jsobject 1;
4. cuatro TTF sin términos exactos: `jlm_cmmib10.ttf`, `jlm_cmssi10.ttf`,
   `jlm_cmti10.ttf` y `jlm_special.ttf`;
5. una decisión humana expresa de aceptar el alcance NC de GeoGebra y la
   atribución “Made with GeoGebra®”.

### COMMERCIAL-A — acuerdo con GeoGebra

Ruta técnicamente viable y de menor esfuerzo de ingeniería. El código
GeoGebra/EUPL y las dependencias de terceros conservan sus licencias
independientes; el acuerdo debe cubrir el uso comercial de los Materials,
Language Files, UI images/styles y cualquier marca GeoGebra retenida. No se ha
contactado a GeoGebra ni se presupone que ofrecerá un acuerdo.[^1]

El dossier de contacto futuro debería incluir:

- producto GeoCeDG, titular, territorio, canales, precio/ingresos y usuarios;
- exacto upstream commit y lista de modificaciones;
- closure byte-exacta: 390 traducciones, conteos/hashes de imágenes/styles y
  logos/marcas retenidos;
- app-image/ZIP/MSI/EXE, actualización, soporte y plazo de distribución;
- atribución propuesta, tratamiento de GeoGebra® y ausencia de instalador
  upstream;
- lista de dependencias de terceros y obligaciones que GeoCeDG asumirá aparte;
- alcance de source release EUPL y licencia propuesta del material propio.

### COMMERCIAL-B — materiales independientes

Es jurídicamente separable como estrategia técnica: conservar código EUPL,
eliminar/reemplazar Language Files, UI images/icons/styles, documentación y
marcas GeoGebra, y usar recursos GeoCeDG o de terceros comercialmente
redistribuibles. No es una remediación documental acotada: afecta 390
traducciones y aproximadamente 1.884 imágenes detectadas en los JAR de
proyecto, además de styles, localization fallback, accesibilidad y pruebas
visuales. Debe tratarse como un proyecto mayor de recursos/localización con
allowlist por hash y regresión funcional.

Ventaja: independencia comercial y menor dependencia futura de cambios en los
términos de GeoGebra. Costes: alta inversión inicial, mantenimiento continuo,
posibles cambios visibles y riesgo de omitir recursos embebidos. Los cuatro JAR
y cuatro fonts bloqueados seguirían necesitando resolución independiente.

| Dimensión | COMMERCIAL-A | COMMERCIAL-B |
|---|---|---|
| Permiso GeoGebra | acuerdo comercial necesario | no para código EUPL; exclusión total verificable de Materials/Language Files |
| Ingeniería | baja-media | alta; proyecto de recursos/localización |
| Mantenimiento | acuerdo y reporting | inventario/allowlist y replacements permanentes |
| Branding | condiciones negociadas y atribución GeoGebra | solo GeoCeDG; política de marca propia |
| Cambio visible | bajo | medio-alto |
| Independencia a largo plazo | baja-media | alta |

## 3. Propuesta de licencias para material GeoCeDG

No se ejecuta ninguna elección en este candidato.

| Clase | Recomendación técnica | Alternativa material | Consecuencia |
|---|---|---|---|
| código/modificaciones/scripts | **EUPL-1.2** | EUPL-1.2-or-later | EUPL-1.2 coincide con la base y evita grants divergentes. “or-later” facilita adoptar revisiones futuras, pero delega esa evolución y exige política de headers/SPDX explícita. |
| documentación propia | **CC BY 4.0** | CC BY-SA 4.0 | BY maximiza reutilización, incluida comercial. BY-SA exige compartir adaptaciones bajo iguales términos. Evitar NC deja abiertas ambas rutas comerciales. |
| artwork no marcario | **CC BY 4.0** | CC BY-SA 4.0; dual license; CC BY-NC-SA | BY reduce fricción. SA preserva reciprocidad. NC bloquearía reutilización comercial salvo licencia separada. Dual/permiso comercial permite NC público pero añade gestión. |
| nombre/logo GeoCeDG | copyright anterior + política separada de marca | guía de uso nominativo y permiso explícito para logos | Una licencia copyright no debe conceder implícitamente derecho de marca, endorsement o identidad de producto. |
| binario completo | términos compuestos por componente; no un rótulo único | PROFILE NC o ruta comercial autorizada | El binario NC no es “open source/free software” por las restricciones de Materials/Language Files. |

Para el branding, la disposición del autor cierra titularidad/provenance: dos
inputs y cuatro derivados mantienen los hashes del manifest. Queda una decisión
de licencia copyright y otra, separada, de control de nombre/logo. La propuesta
es CC BY 4.0 para artwork operativo y una política de marca que permita uso
nominativo y redistribución inalterada con GeoCeDG, pero reserve forks,
endorsement y logos modificados salvo permiso. Esto evita bloquear una edición
comercial propia sin regalar identidad de marca.

## 4. Dependencias investigadas a cierre factual

“Compatible” significa que los términos primarios no imponen una prohibición
NC/comercial conocida **si se cumplen sus obligaciones**. No es una conclusión
legal sobre el producto completo.

| Componente | Artefacto/version y path | Términos primarios | Obligación principal | NC | Comercial | Disposición |
|---|---|---|---|---:|---:|---|
| ReTeX | `renderer-base-0.1.jar`, `renderer-desktop.jar` | GPL-2.0-or-later + linking exception | LICENSE, GPLv2, exception, copyright, source correspondiente | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |
| JOGL | 2.6.0, Java + Win native | composite permissive del LICENSE 2.6.0; manifest commit `f596460…` | reproducir LICENSE/notices completos | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |
| GlueGen | 2.6.0, Java + Win native | composite permissive; manifest commit `c06493e…` | reproducir LICENSE/notices completos | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |
| JNA | 5.18.1 incl. nested natives | Apache-2.0 OR LGPL-2.1-or-later | propuesta: seleccionar Apache-2.0 y conservar texto/notices | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |
| FlatLaf | 3.7 incl. 7 nested natives | Apache-2.0 | conservar texto/copyright | sí | sí | RESOLVED — RETAIN |
| Giac | `javagiac-70501-…`; DLL `3209b44…` | upstream GPL; JAR/DLL opacos sin tag/source/version resource[^10][^16] | source correspondiente + GPL + GMP/MPFR notices, hoy imposibles de ligar | no | no | UNRESOLVED — PRIMARY EVIDENCE INSUFFICIENT |
| math-cross-platform | 3.6.3 | POM solo dice fork GWT de Commons Math; sin grant/source[^15] | obtener source/license exacto o rebuild probado | no | no | UNRESOLVED — PRIMARY EVIDENCE INSUFFICIENT |
| OpenGeoProver | 20120725 | no grant global; source muestra “Not for commercial use”[^11][^17] | obtener grant y correspondencia exacta | no | no | UNRESOLVED — PRIMARY EVIDENCE INSUFFICIENT |
| jsobject | 1, tres clases | sin metadata legal[^18]; JDK incluido ya contiene `jdk.jsobject`[^12] | excluir tras pruebas o demostrar grant | no | no | EXCLUDE CANDIDATE |
| Temurin runtime | 25.0.4+7-LTS | GPL-2.0 + Classpath Exception + terceros por módulo | conservar `runtime/legal`, pin/source access | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |
| WiX payload | 5.0.2+aa65968c | MS-RL | texto/notices y source de ficheros WiX redistribuidos | sí | sí | RESOLVED — NOTICE/TEXT REQUIRED |

Las disposiciones de esta tabla se apoyan respectivamente en las licencias
primarias del renderer,[^3] JOGL,[^6] GlueGen,[^7] JNA,[^8] FlatLaf,[^9]
Temurin[^13] y WiX.[^14]

Las otras 31 dependencias externas del audit ya tenían una licencia/texto
primario identificado. Con JOGL/GlueGen/JNA resueltos aquí, el resultado es 36
de 40 JAR externos retenibles con notices; cuatro permanecen bloqueados. La
matriz machine-readable conserva coordenadas, paths y hashes exactos.

Soluciones mínimas para los cuatro JAR bloqueados:

- **Giac:** obtener el source/build exacto de `70501`; si no existe, autorizar
  rebuild/upgrade desde commit pinneado y ejecutar regresión CAS. No sustituir
  como edición legal.
- **math-cross-platform:** pedir/publicar el fork exacto o reconstruir desde
  una base Apache conocida con diff documentado; exige regresión numérica.
- **OpenGeoProver:** obtener permiso/grant del titular o excluir el engine tras
  pruebas de `Prove`/`ProveDetails` y fallback. Cambia capacidad soportada.
- **jsobject:** candidato preferido a exclusión porque el JDK 25 ya aporta
  `jdk.jsobject`; verificar linkage, API Desktop y plugins antes de removerlo.

## 5. Fonts: 46/46 dispositionadas

Los nombres y SHA-256 de las 46 están en la matriz base; el overlay registra la
licencia por `component_id`. Se verificaron 33 coincidencias byte-exactas con
JLaTeXMath upstream; los 13 históricos no coinciden con el HEAD actual.

| Grupo | Nº | Términos | Requisitos | NC/comercial |
|---|---:|---|---|---|
| Computer Modern explícitos | 10 | Knuth | texto; renombrar modificaciones | compatible |
| Cyrillic `wn*` | 8 | Knuth para bytes + GPL/exception para módulo | ambos textos + source del módulo | compatible |
| Greek `fc*` | 8 | GPL-2.0 para font module | GPLv2 + source/access | compatible |
| Euler/AMS | 4 | SIL-OFL-1.1, reserved names | OFL y nombres reservados | compatible |
| JLaTeXMath Latin `jlm*` | 9 | GPL-2.0-or-later + linking exception | GPL/COPYING/exception + source/access | compatible |
| `dsrom` | 1 | permiso libre específico | conservar texto/naming | compatible |
| `rsfs`, `stmary` | 2 | declaración PD upstream | añadir notice de provenance | compatible |
| `cmmib`, `cmssi`, `cmti`, `special` | 4 | no establecido para bytes exactos | grant exacto o reemplazo con regresión métrica | bloqueado |

La contradicción cirílica queda explicada, no borrada: el `LICENSE` raíz
asigna los ocho fonts `wn*` a Knuth, mientras `jlatexmath-font-cyrillic`
licencia el módulo bajo GPL+exception. Cumplir ambas capas es la disposición
conservadora.[^3][^4] Para los cuatro bloqueados, los name tables apuntan a
AMS/BaKoMa/Calixte, pero ni la coincidencia byte-exacta con el upstream actual
ni un carve-out explícito están disponibles. La distribución BaKoMa actual
publica términos, pero no coincide byte a byte con esos binarios y no permite
proyectarlos retrospectivamente sobre el payload.[^5]

## 6. Temurin y WiX

### Temurin

El build usó `C:\Users\usuario\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2`:
Temurin 25.0.4+7-LTS, HotSpot, x86-64. `release` fija source
`a2ce02c38bc9`, build source `e6ba7dec3d07654074559310376a3ae89da5f4ac`
y repositorios Adoptium. El asset oficial reproducible es
`OpenJDK25U-jdk_x64_windows_hotspot_25.0.4_7.zip`, SHA-256
`7caab7db43bf4b94a2e6252c699e70d90084f9aa7c943cd3414761fd540937ae`.[^13]

`jpackage/jlink` conserva 195 ficheros legales bajo 52 directorios de módulos:
GPLv2, Classpath/assembly exceptions y notices de terceros. El root `NOTICE` del
JDK no aparece como root del runtime reducido; el bundle futuro debe enlazar el
release/source y no eliminar `runtime/legal/**`. Es apto técnicamente para NC y
comercial con esas obligaciones.

### WiX

El compilador/CLI WiX es build-only, pero el MSI sí incorpora
`Wix4UtilCA_X64`, `WixUiCa_X64` y recursos UI; el EXE es wrapper nativo de
OpenJDK/jpackage que contiene el MSI, no un Burn bundle. Por tanto no debe
declararse WiX “no distribuido”. El MS-RL 5.0.2 exige conservar notices/licencia
y source para cada fichero distribuido que contiene código WiX. La ruta
técnica es incluir MS-RL y acceso al source exacto v5.0.2 de esos payloads.[^14]

## 7. `.cedg`: erratum, causa y corrección

El MSI pre-change del audit, SHA-256
`bbd69489b791ff307f904c6b625135db6a3e5753b30836b82159c3967a9cf948`,
**sí contenía** la asociación. WiX 5 descompila la authoring `Extension/ProgId`
como filas `RegistryValue`; el checker solo buscaba los nodos de authoring y
produjo un falso negativo.

La corrección mínima fue al verificador, no al generador:

- aceptar ambas proyecciones (`WIX_AUTHORING` y `MSI_REGISTRY_ROWS`);
- verificar exactamente `.cedg`, MIME `application/x-geocedg-cedg`, ProgId
  GeoCeDG-owned, descripción, open verb con `%1` y target `GeoCeDG.exe`;
- rechazar `.ggb` y MIME upstream;
- escribir la descompilación temporal dentro del write-root del ResultPath.

El MSI reconstruido desde `f674f54ab33df052a4b4dffc2deaf969db5b4081`
contiene una asociación y cero upstream, un MIME, un ProgId propio, un open
verb y un launcher target. El gate PACKAGING terminó `ACCEPTED / COMPLETE`.
El contrato vigente no define un icono específico de tipo de fichero y el MSI
no crea `DefaultIcon`; por ello no se inventó una nueva obligación de registro.
No se cambió instalación, launcher, runtime, asociación ni semántica de
producto; se corrigió la lectura de la evidencia real.

## 8. Gap exacto de LICENSE/NOTICE/THIRD_PARTY/SBOM

La futura remediación autorizada debería producir:

1. root `LICENSE` como índice de grants separados, sin aplicar una licencia
   propia a contenido upstream/tercero;
2. `LICENSES/` con EUPL-1.2, licencia elegida de docs/artwork, GeoGebra NC y
   CC BY-NC-SA, ReTeX GPL/exception/COPYING, cada familia de fonts,
   JOGL/GlueGen completos, Apache-2.0, MPL/LGPL/W3C/ANTLR/BSD/component-local,
   Temurin/OpenJDK y WiX MS-RL;
3. `NOTICE.md` con GeoCeDG copyright/modifications, “Made with GeoGebra®” para
   PROFILE NC, upstream URL/commit y ausencia de endorsement;
4. `THIRD_PARTY.md` generado desde una allowlist versionada con artefacto,
   versión, hash, terms, packaged text, source URL y obligación;
5. source-access/corresponding-source manifest para EUPL/GPL/Temurin/WiX;
6. SBOM que incluya purl/coordenada/version/licencias, runtime Java, fonts,
   assets y nested natives.

Los 52 `version: unknown` del SBOM no se cambiaron. El builder aplana
`installDist` a filenames y no conserva identidad autoritativa Gradle para JAR
de proyecto y algunos transitivos; extraer versiones por regex de filename
sería heurístico. La remediación correcta es capturar el resolved graph en el
builder existente y enlazarlo por hash, no inventar versiones.

## 9. Decisiones humanas que permanecen

El [docket actualizado](pre_g9b_d1_human_decision_docket.md) reduce ocho
decisiones a cuatro:

1. licencias de código, docs y artwork GeoCeDG;
2. política de nombre/logo GeoCeDG;
3. elegir NC, COMMERCIAL-A o autorizar el proyecto COMMERCIAL-B;
4. acción autorizada para cuatro JAR y cuatro fonts sin evidencia suficiente.

JNA, JOGL/GlueGen, Temurin, WiX, Cyrillic y `.cedg` ya no requieren una
decisión de interpretación técnica; solo incorporación de textos/source
access y, cuando proceda, revisión profesional de implementación de
cumplimiento.

## 10. Validación y límites

| Check | Resultado | Evidencia |
|---|---|---|
| pre-change PACKAGING | rechazo por falso negativo `.cedg` | `verification-7393bacda95747638b61dd8b13e9b51f` |
| decompile independiente del MSI pre-change | asociación presente | MSI hash `bbd69489…`; registry projection |
| focal checker tras corrección | `CONTRACT_SATISFIED` | `artifacts/pre-g9b-d1/continuation/post-verifier-fix/packaging-product-result.json` |
| rebuild All | exit 0; 52 JAR/6 excluded | `artifacts/packaging/windows/build-manifest.json` |
| primer PACKAGING post-fix | producto satisfecho; SAFETY rechazó write-root temporal | `verification-7dcfb4df3d12443493f9abee1534de5c` |
| PACKAGING final | `ACCEPTED / COMPLETE`, 0 diagnostics | `verification-21c95c098d2148208817014e96f7453c`, result hash `f20c353f…` |
| inventory correspondence | 52/52 nombres, 40/40 hashes externos, 46/46 fonts | 11/12 JAR propios byte-idénticos; solo `desktop.jar` cambia por build provenance del nuevo SHA |

Artefactos finales internos: ZIP `afc7d686…`, MSI `23626ad9…`, EXE
`f760ae31…`. Todos conservan `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`.
No se hizo installation smoke porque el repositorio no ofrece un smoke
instalado reversible dentro del gate; la inspección determinista del MSI sí es
canónica. No se ejecutaron PHASE/INTEGRATION/FINAL científicos.

**Bootstrap impact: NO_CHANGE.** No cambió el JDK/WiX requerido ni su modo de
adquisición; solo se documentó el identity pin que el bundle futuro debe
conservar.

## 11. Siguiente paso D1 recomendado — no ejecutado

Solicitar las cuatro decisiones del docket y autorización separada para:

1. añadir grants/textos/notices/source-access elegidos;
2. obtener o sustituir/excluir los cuatro JAR y cuatro fonts bloqueados;
3. enriquecer el SBOM desde metadata resuelta, sin parser de filenames;
4. reconstruir una closure nueva y ejecutar PACKAGING + STATIC documental.

Solo entonces podría presentarse un **D1 deployability candidate**. Este
checkpoint no declara D1 PASS ni permite redistribución.

## Fuentes primarias

[^1]: [GeoGebra License oficial, actualizada noviembre de 2025](https://github.com/geogebra/legal/blob/main/geogebra_license.md).
[^2]: [EUPL 1.2, Comisión Europea](https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
[^3]: [JLaTeXMath LICENSE](https://github.com/opencollab/jlatexmath/blob/master/LICENSE).
[^4]: [CTAN Computer Modern — Knuth License](https://ctan.org/pkg/cm).
[^5]: [CTAN BaKoMa fonts](https://ctan.org/pkg/bakoma-fonts).
[^6]: [JOGL 2.6.0 LICENSE](https://raw.githubusercontent.com/sgothel/jogl/v2.6.0/LICENSE.txt).
[^7]: [GlueGen 2.6.0 LICENSE](https://raw.githubusercontent.com/sgothel/gluegen/v2.6.0/LICENSE.txt).
[^8]: [JNA 5.18.1 upstream](https://github.com/java-native-access/jna/tree/5.18.1).
[^9]: [FlatLaf 3.7 upstream](https://github.com/JFormDesigner/FlatLaf/tree/3.7).
[^10]: [GeoGebra Giac mirror](https://github.com/GeoGebra/giac).
[^11]: [OpenGeoProver primary source mirror](https://github.com/ivan-z-petrovic/open-geo-prover).
[^12]: [JDK 25 `jdk.jsobject` API](https://docs.oracle.com/en/java/javase/25/docs/api/jdk.jsobject/netscape/javascript/package-summary.html).
[^13]: [Temurin 25.0.4+7 official release](https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.4%2B7).
[^14]: [WiX 5.0.2 MS-RL](https://raw.githubusercontent.com/wixtoolset/wix/v5.0.2/LICENSE.TXT).
[^15]: [POM exacto de `math-cross-platform` 3.6.3](https://repo.geogebra.net/releases/org/apache/math-cross-platform/3.6.3/math-cross-platform-3.6.3.pom).
[^16]: [POM exacto de `javagiac` 70501 para Windows x64](https://repo.geogebra.net/releases/org/geogebra/javagiac/70501/javagiac-70501-natives-windows-amd64.pom).
[^17]: [POM exacto de OpenGeoProver 20120725](https://repo.geogebra.net/releases/org/geogebra/OpenGeoProver/20120725/OpenGeoProver-20120725.pom).
[^18]: [POM exacto de `jsobject` 1](https://repo.geogebra.net/releases/org/geogebra/jsobject/1/jsobject-1.pom).
