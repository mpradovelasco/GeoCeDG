# PRE-G9B-D1 — human decision docket

Estado: **CANDIDATE — PENDING AUTHOR REVIEW**

~~~text
selfApproved=false
authorApproved=false
passClaimed=false
publicRedistributionAllowed=false
~~~

La investigación de continuación reduce las ocho decisiones iniciales a cuatro
elecciones genuinamente humanas. La evidencia está en el
[informe de disposición](pre_g9b_d1_licensing_disposition_and_bounded_remediation_candidate.md)
y el [overlay machine-readable](../../geocedg/validation/pre-g9b-d1/component-disposition.json).
Responder este docket no declara D1 PASS ni autoriza publicación.

## D1-HD-01 — grants del material GeoCeDG

**Cuestión.** Elegir licencias separadas para código/scripts, documentación y
artwork propio.

**Evidencia factual.** El autor confirmó titularidad de branding. El root
LICENSE actual sigue siendo no-grant. EUPL-1.2 es la licencia de la base de
código upstream; docs y artwork no necesitan heredar una licencia de software.

**Alternativas.**

1. Recomendación: código/scripts **EUPL-1.2**, docs **CC BY 4.0**, artwork no
   marcario **CC BY 4.0**.
2. Código **EUPL-1.2-or-later** y docs/artwork CC BY 4.0.
3. Usar CC BY-SA 4.0 para docs y/o artwork si se desea reciprocidad.
4. Aplicar NC o dual licensing a artwork, sabiendo que añade permiso separado
   a una distribución comercial futura.

**Consecuencia técnica.** Determina headers/SPDX, índices de LICENSES, notices,
source distribution y qué assets pueden viajar en perfiles NC/comerciales.

**Recomendación técnica.** Alternativa 1: minimiza divergencia de la base,
permite uso comercial futuro y conserva attribution. “or-later”, share-alike o
NC son preferencias normativas del autor, no necesidades del package.

**Decisión humana.** Licencia, titulares, fecha efectiva y alcance por paths.

## D1-HD-02 — nombre y logo GeoCeDG

**Cuestión.** Definir el uso del nombre/logo separado del copyright de los
ficheros de artwork.

**Evidencia factual.** Titularidad/provenance/hashes ya están resueltos. Una
licencia copyright permisiva no debe interpretarse como endorsement ni grant
de marca.

**Alternativas.**

1. Recomendación: permitir uso nominativo y redistribución inalterada del logo
   con GeoCeDG; reservar uso en forks, logos modificados y endorsement salvo
   permiso.
2. Política de marca más permisiva para forks con reglas de diferenciación.
3. No adoptar política de marca por ahora y no conceder derechos marcarios
   implícitos.

**Consecuencia técnica.** Afecta nombres de instalador, iconos, about/NOTICE,
fork branding y documentación de redistribuidores; no cambia hashes actuales.

**Decisión humana.** Nivel de control de marca/nombre y permiso para terceros.

## D1-HD-03 — perfil objetivo GeoGebra

**Cuestión.** Elegir la ruta de distribución que debe guiar la siguiente
remediación.

**Evidencia factual.** Retener Language Files/UI images/styles mantiene el
producto completo bajo restricciones NC de GeoGebra. El código EUPL es
independiente. COMMERCIAL-A requiere acuerdo; COMMERCIAL-B exige sustituir o
excluir cientos de recursos y verificar su ausencia.

**Alternativas.**

1. Preparar primero PROFILE NC, con atribución y términos GeoGebra aplicables.
2. Preparar dossier y negociar COMMERCIAL-A, sin contacto automático.
3. Autorizar diseño/proyecto mayor COMMERCIAL-B de recursos independientes.
4. Mantener solo evaluación interna.

**Consecuencia técnica.** NC/A preserva UI/localization con baja ingeniería;
B cambia recursos, fallback, accesibilidad y mantenimiento. Ninguna ruta evita
obligaciones de terceros.

**Recomendación técnica.** Completar NC primero si la prioridad es una entrega
pública docente; mantener el inventario COMMERCIAL-B como workstream separado.
Si la prioridad es comercial cercana, valorar A antes de financiar el reemplazo
masivo B.

**Decisión humana/negocio.** Perfil prioritario, tolerancia a NC, presupuesto de
reemplazo o decisión de negociación.

## D1-HD-04 — ocho payloads sin evidencia suficiente

**Cuestión.** Autorizar cómo resolver cuatro JAR y cuatro fonts que no tienen
grant/provenance exactos suficientes.

**Evidencia factual.**

- JAR: Giac `70501`, math-cross-platform `3.6.3`, OpenGeoProver `20120725`,
  jsobject `1`.
- Fonts: `jlm_cmmib10.ttf`, `jlm_cmssi10.ttf`, `jlm_cmti10.ttf`,
  `jlm_special.ttf` (hashes en la matriz).
- Cambiar Giac/math/prover puede alterar CAS, resultados numéricos o proving.
- `jsobject-1.jar` duplica una API presente en el módulo `jdk.jsobject`, pero su
  exclusión requiere pruebas de linkage/plugins.

**Alternativas técnicamente razonables.**

1. Obtener del titular/upstream exacto source, licencia y correspondencia a
   cada hash; retener con bundle completo.
2. Autorizar rebuild/upgrade pinneado de Giac/math con regresión funcional.
3. Autorizar exclusión/reemplazo de OpenGeoProver con pruebas de fallback.
4. Autorizar exclusión focal de jsobject tras tests Desktop/plugin.
5. Autorizar sustitución de cuatro fonts por alternativas métricamente
   verificadas; mantenerlas internas hasta entonces.

**Consecuencia técnica.** 2–5 son cambios de composición/runtime y no están
autorizados por este checkpoint. Una sustitución de library no puede
presentarse como simple metadata legal.

**Recomendación técnica.** Intentar 1 para Giac/math/OGP; usar 4 para jsobject
si las pruebas confirman redundancia; sustituir solo los cuatro fonts, no el set
completo, si no aparece evidencia exacta.

**Decisión humana.** Autorizar por componente retain/rebuild/exclude/replace y
el nivel de validación productiva correspondiente.

## Disposiciones retiradas del docket

- Branding provenance/ownership: resuelto por disposición expresa del autor.
- `.cedg`: el MSI ya era correcto; se corrigió el falso negativo del checker.
- JNA: propuesta Apache-2.0 registrada; no exige cambiar el artefacto.
- JOGL/GlueGen: retener con LICENSE/notices 2.6.0.
- Cyrillic/fonts restantes: 42 de 46 dispositionados con textos.
- Temurin: retener 25.0.4+7 con pin, legal tree y source access.
- WiX: retener build 5.0.2 y cubrir payload MSI bajo MS-RL/source access.

## Forma de respuesta

~~~text
D1-HD-01 = alternativa <n> [con ajustes exactos]
D1-HD-02 = alternativa <n>
D1-HD-03 = alternativa <n>
D1-HD-04 = <disposición por cada componente/grupo>
public redistribution = todavía no autorizada
D1 PASS = no reclamado
~~~
