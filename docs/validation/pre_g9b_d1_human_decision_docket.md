# PRE-G9B-D1 — human decision docket

Estado: **CANDIDATE — PENDING AUTHOR REVIEW**

~~~text
selfApproved=false
authorApproved=false
passClaimed=false
~~~

Este docket no solicita aprobación genérica de D1. Cada decisión futura debe
nombrar el identificador, la alternativa elegida y el scope autorizado. La
evidencia factual completa está en el
[informe de research/audit](pre_g9b_d1_licensing_assets_deployability_research_audit.md)
y la [matriz](../../geocedg/validation/pre-g9b-d1/component-audit.json).

## D1-HD-01 — licencia del material GeoCeDG

**Cuestión.** Qué licencia o permisos públicos, si alguno, tendrán por separado
el código/modificaciones, documentación, tools/scripts y ejemplos/modelos
GeoCeDG.

**Evidencia.** Root LICENSE es no-grant. Hay 621 paths añadidos y 96 modificados
en el registro upstream; doce JAR de proyecto y dos guías propias se
distribuyen.

**Alternativas técnicamente razonables.**

1. Elegir términos explícitos por clase y definir el source-distribution.
2. Mantener una o más clases solo para evaluación interna y excluirlas del
   release público.
3. Posponer toda distribución pública.

**Consecuencia.** Sin decisión explícita, B01 sigue bloqueando todo package
público y el bundle legal no puede completarse.

**Recomendación técnica.** Registrar una tabla autoritativa por clase y path,
además de política de notices/source offer. Esta recomendación no selecciona
licencia.

**Decisión humana.** Selección de licencia/permisos, titulares, alcance,
compatibilidad y fecha efectiva.

## D1-HD-02 — traducciones y UI assets upstream

**Cuestión.** Conservar, obtener permiso, sustituir o excluir las 390
traducciones y los assets upstream realmente distribuidos, incluidos logos
GeoGebra.

**Evidencia.** common.jar contiene 1.495 imágenes y logos explícitos;
desktop.jar contiene otras 389; common-jre.jar contiene 390 translations.
GeoGebra separa estas clases del código EUPL y les aplica términos
no-comerciales/atribución/share-alike; las marcas son separadas.

**Alternativas.**

1. Conservar para un scope expresamente autorizado y documentado.
2. Obtener permiso/acuerdo para el scope deseado.
3. Autorizar L4 para sustituir assets/translations y excluir marcas.
4. Reducir el producto a un conjunto mínimo auditado.

**Consecuencia.** Retenerlos mantiene un package sujeto a esas clases de
términos; sustituir/excluir cambia composición del runtime y requiere nueva
autorización y pruebas.

**Recomendación técnica.** Minimizar la closure y generar allowlist
byte-exacta antes de tocar recursos. No asumir que todos los PNG/SVG tienen la
misma procedencia.

**Decisión humana.** Scope de redistribución, permiso o estrategia
replace/exclude, y tratamiento de marcas.

## D1-HD-03 — branding GeoCeDG

**Cuestión.** Si los dos inputs del autor y cuatro derivados pueden
redistribuirse públicamente, con qué alcance y atribución.

**Evidencia.** Provenance, hashes y derivación son reproducibles. La autorización
registrada cubre el candidato interno, no un grant público.

**Alternativas.**

1. Autorizar expresamente originales y derivados para el scope seleccionado.
2. Autorizar solo derivados finales y excluir sources del runtime.
3. Proveer branding alternativo con provenance y permiso explícitos.
4. Mantener evaluación interna.

**Consecuencia.** Afecta desktop.jar, launcher, app-image, MSI y EXE.

**Recomendación técnica.** Sea cual sea la decisión, registrar titulares,
inputs, derivados, roles y hashes; no distribuir sources del branding si no son
necesarias.

**Decisión humana.** Derechos/autorización pública, alcance, atribución y uso
del nombre/marca GeoCeDG.

## D1-HD-04 — ReTeX y 46 fonts

**Cuestión.** Aprobar una disposición por cada font y el renderer, o autorizar
reemplazo/exclusión.

**Evidencia.** Trece TTF no tienen mapping, faltan textos GPL/public-domain y
existe evidencia cirílica que debe reconciliarse. El texto GPL con linking
exception de ReTeX no llega al package.

**Alternativas.**

1. Obtener fuentes/versiones/textos exactos y mantener el set completo.
2. Autorizar un set reducido de fonts completamente documentado.
3. Autorizar reemplazo técnico conservando métricas/rendering verificadas.
4. Excluir el renderer si el producto puede funcionar sin él, tras análisis
   funcional separado.

**Consecuencia.** Cambiar fonts o renderer puede afectar layout, rendering,
métricas visuales y regresión; no debe hacerse como simple edición legal.

**Recomendación técnica.** Intentar primero cerrar provenance/textos del payload
actual. Solo proponer replacement con casos de regresión y aprobación L4.

**Decisión humana.** Interpretación de evidencia, aceptación de términos y
autorización de conservar/reemplazar/excluir.

## D1-HD-05 — Giac, JOGL/GlueGen y binarios opacos

**Cuestión.** Qué hacer con los DLL/JAR cuyo source exacto y bundle legal no
están ligados al hash distribuido.

**Evidencia.** javagiac 70501, JOGL/GlueGen 2.6.0, math-cross-platform 3.6.3,
OpenGeoProver 20120725 y jsobject 1 carecen de metadata legal embebida
suficiente; Giac/JOGL/GlueGen incluyen nativos.

**Alternativas.**

1. Obtener artefacto fuente, tag, checksum, texto y notices exactos.
2. Obtener confirmación/permiso del upstream/distribuidor.
3. Autorizar replacement por componentes verificables.
4. Excluir capacidades dependientes y validar degradación explícita.
5. Mantenerlos solo en evaluación interna.

**Consecuencia.** Replacement/exclusion puede alterar CAS, 3D o proving y es un
cambio productivo; no se autoriza con este docket.

**Recomendación técnica.** Tratar hash del binario y hash/source de upstream
como pareja obligatoria; no aceptar solo coordenadas Maven.

**Decisión humana.** Disposición final y cualquier cambio de capacidad.

## D1-HD-06 — JNA y bundle de notices de dependencias

**Cuestión.** Seleccionar y registrar el camino de licencia de JNA y aprobar la
composición de notices/textos de todas las dependencias claras.

**Evidencia.** JNA indica Apache-2.0 OR LGPL-2.1-or-later y embebe 26 nativos.
EchoSVG/CSS4J/W3C/JCLF/FlatLaf tienen textos locales; J2ObjC, ANTLR, Rhino y
SpotBugs no los llevan en el package.

**Alternativas.**

1. Seleccionar una alternativa JNA con revisión humana y añadir textos/notices.
2. Cambiar dependencia, solo con autorización productiva.
3. Mantener evaluación interna.

**Consecuencia.** Determina notices, posible source handling y composición de
LICENSES/THIRD_PARTY.

**Recomendación técnica.** Generar el bundle desde una allowlist versionada, sin
deducir licencias por nombre.

**Decisión humana.** Elección JNA y aprobación del contenido legal final.

## D1-HD-07 — Temurin y frontera WiX/jpackage

**Cuestión.** Aprobar el runtime Temurin exacto y resolver si el instalador
redistribuye código/resources WiX sujeto a notices adicionales.

**Evidencia.** Temurin 25.0.4+7 incluye 195 archivos legales; falta pin de
download/checksum/source. WiX 5.0.2 es build tool, pero el payload de bootstrap
EXE/MSI no se caracterizó byte-exacto.

**Alternativas.**

1. Mantener Temurin, fijar origen/checksum/source y aprobar notices.
2. Autorizar otro runtime solo tras análisis/rebuild.
3. Limitar distribución a ZIP/app-image mientras MSI/EXE sigan sin closure.
4. Investigar y documentar el payload WiX antes de decidir.

**Consecuencia.** Afecta todos los formatos en el caso de Java y solo
instaladores en el caso WiX.

**Recomendación técnica.** Pin reproducible de Temurin y análisis binario del
installer; no copiar la licencia de una tool como supuesto de payload.

**Decisión humana.** Aceptación del runtime/uso de marcas y alcance de
MSI/EXE.

## D1-HD-08 — remediación técnica de la asociación .cedg

**Cuestión.** Autorizar una tarea L4 limitada para corregir la asociación o
cambiar explícitamente el contrato.

**Evidencia.** El perfil la declara, pero el MSI descompilado contiene cero
Extension, MIME, ProgId, open verb y target; la verificación canónica rechaza.

**Alternativas.**

1. Autorizar corrección para MSI/EXE y mantener el contrato.
2. Autorizar cambio de especificación que elimine o posponga la asociación.
3. Posponer installers y considerar solo salida portable, sin declarar D1
   técnicamente listo.

**Consecuencia.** D1 L5 no puede ser técnico-ready mientras perfil y output
divergan.

**Recomendación técnica.** Si la asociación sigue siendo requisito, corregir el
pipeline y añadir prueba contra MSI/EXE antes de cualquier closeout.

**Decisión humana.** Autorizar el scope productivo exacto; esta auditoría no lo
ejecuta.

## Forma de respuesta solicitada

Una decisión futura debería citar, por ejemplo:

~~~text
D1-HD-03 = alternativa 2
scope = <paths/outputs exactos>
public redistribution = todavía no autorizada
L4 implementation = autorizada/no autorizada
~~~

Resolver el docket tampoco declara D1 PASS. Después harán falta remediación
autorizada, rebuild, L5 y aprobación humana separada.
