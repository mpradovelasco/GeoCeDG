# CU8-I · Disposición de controles v1

Contrato adoptado por el autor para GeoCeDG Desktop, Gráficos 1, en
el cierre conjunto de CU8-I, R1 y R2 del 2026-11-10. La función conserva
su clasificación experimental en el perfil. El diseño adoptado define
la elegibilidad, las operaciones y sus invariantes.
[Cierre autoral](../../../docs/validation/pre_g9b_cu8_i_closeout_record.md) ·
[CU8-D, §2 y §5](../../../docs/architecture/pre_g9b_cu8_d_control_containers_candidate.md).

La [revisión focal de incorporación](control-layout-r3.md) amplía este
contrato. Los archivos que solo usan el formato v1 mantienen su lectura
y escritura históricas.

La composición, el desacoplamiento, la apariencia, la extensión de
archivo y el contexto del miembro accesible se rigen por
[CU8-I-R1 y R2](control-layout-r1.md). El alcance admite paneles fijos
o vinculados al lienzo y las cuatro familias de control indicadas
abajo. Gráficos 2, paneles anidados y transferencia de paneles entre
documentos quedan fuera del perímetro adoptado.

## Puerta A

Base publicada `876a67362939cb5208a9562e396bbd3add7dfc04`, árbol
`211a518644a51e6507a042c6320bdf13b9d8c349`. Las sondas de entrada
confirmaron el propietario de cada uno de los cuatro tipos en archivo
y memoria, y la conservación de los vínculos nativos en la reapertura.
El lector anterior real `066b53d0cc2dd8e78cf1c542e6adea820ec77d5c`
ignoró las marcas, conservó controles, valores, dependencias y respaldos,
y eliminó la disposición al guardar. Las sondas de inserción
observaron pérdida de las marcas en F3 y rechazo por la envoltura
pasiva experta.

El autor autorizó expresamente el 2026-11-09 la degradación documentada
con lectores anteriores y la política acotada de inserción de este
contrato. La disposición incluye la apariencia y el orden de R1.
Las únicas excepciones adicionales para lectores anteriores son las
dos propiedades nativas indicadas abajo. Los
lectores anteriores carecen de aviso propio. La distribución nueva
advierte que abrir y volver a guardar con una versión anterior puede
eliminar irreversiblemente la disposición y aconseja conservar una
copia original. GeoCeDG y Classic de la misma distribución conservan
el registro, los respaldos y las propiedades nativas al abrir,
guardar, deshacer y rehacer. Classic presenta los controles en sus
posiciones nativas de respaldo.

La segunda sonda usó un archivo v1 escrito por el candidato. El lector
anterior abrió la casilla desbloqueada, omitió el desbloqueo al guardar
y recuperó el bloqueo de posición al reabrir. En el archivo de control
sin paneles, su escritor ya omitía esa propiedad. El autor autorizó
por separado únicamente este cambio de bloqueo en lectores anteriores.
Los lectores nuevos conservan el desbloqueo de la casilla agrupada.

Con un deslizador que tiene una definición y el modo Nombre y valor,
los lectores anteriores pueden omitir `labelMode` al volver a guardar.
Al reabrir, el modo puede pasar a Valor según el etiquetado efectivo.
El mismo comportamiento se comprobó sin paneles. GeoCeDG anterior
conserva la selección explícita con el alias `6`. Classic anterior
puede omitir también ese alias. El aviso de la distribución nueva
informa de los efectos verificados. El autor autorizó por separado
solo este cambio de rótulo en lectores anteriores y lo ratificó en
el cierre. La distribución nueva conserva el modo efectivo en el
perímetro CU8. Ambas excepciones históricas preservan los valores,
las dependencias, la geometría y la identidad documental.

## Formato

El formato base consta de dos atributos ASCII sin namespace. El registro pertenece a la apertura
de la construcción principal. Cada marca de ranura pertenece a la
apertura del bloque `element` de la instancia que la ocupa.

```text
geocedgControlLayout = 1 "|" panel (";" panel)*
panel = panel-id "," view "," anchor "," x "," y "," width "," height "," visible "," slots
view = 1
anchor = F | W
visible = 0 | 1
slots = "" | slot-id (":" slot-id)*
geocedgControlSlot = panel-id "," slot-id "," family "," dx "," dy
family = S | B | C | I
```

- `F` expresa el origen respecto al área útil en píxeles lógicos. `W`
  expresa el par en coordenadas de Gráficos 1. Los desplazamientos de
  ranura se expresan en píxeles lógicos en ambos modos.
- Los identificadores se escriben como UUID en minúscula. Cada panel
  tiene ID único en el documento y cada ranura ID único en su panel.
  Los paneles y sus listas de ranuras se escriben ordenados por ID.
- Las coordenadas y offsets son números binarios64 finitos. Se
  escriben con `Double.toString` y se leen en decimal, con signo
  menos opcional, fracción opcional con al menos un dígito tras el
  punto y exponente decimal opcional `[Ee]-?[0-9]+`. Cada campo
  admite como máximo 24 caracteres. El ancho y alto preferidos son
  enteros positivos de 32 bits en píxeles lógicos.
- `S` es deslizador numérico absoluto, `B` botón absoluto, `C`
  casilla independiente absoluta e `I` campo de entrada absoluto.
  Se aplica el predicado de posición, vista y dependencia de CU8-D.
- Se admiten simultáneamente 32 paneles, 256 ranuras en total y
  65.536 bytes ASCII acumulados en el registro, las marcas de ranura
  y el atributo opcional de apariencia. Con cero paneles se omiten
  las marcas CU8 y se conserva la escritura histórica.

El atributo opcional `geocedgControlAppearance` de R1, en la misma
apertura que el registro, codifica nombre Unicode, RGB, opacidad y
orden por identificador de panel. El color predeterminado es
`#E1EDF7`, la opacidad predeterminada es 65 % y el rango admisible
es de 25 % a 80 %. El nombre vacío toma el rótulo localizado «Panel».
La ausencia del atributo conserva el orden canónico v1 y la
apariencia predeterminada. Su gramática y el rechazo seguro de
datos inválidos se especifican en [Apariencia y persistencia](control-layout-r1.md#apariencia-y-persistencia).

El lector valida la pertenencia completa antes de publicarla. Un
bloque solo puede reclamar su propia instancia, que el lector nativo
crea o reutiliza. Cada ranura declarada requiere un único bloque
propietario vivo. Etiqueta, posición, orden y proximidad carecen de
autoridad. Un error de contexto, versión, gramática, límites,
duplicación, orfandad o propietario rechaza la carga y conserva la
sesión previa, incluido su historial.

La escritura de marcas se limita al archivo nativo y a instantáneas
completas de historial y reconstrucción local. Una marca en una
entrada `.ggb` de compatibilidad o en XML genérico se rechaza. Se
conservan ZIP, versión XML, `app="classic"`, identidad durable,
`Group` y las excepciones de persistencia ya aprobadas. Las macros,
defaults, estilos, portapapeles y fragmentos de inserción carecen de
autoridad para publicar pertenencia.

Para un deslizador que ocupa una ranura, la escritura completa de
archivo e historial conserva el modo efectivo en la etiqueta nativa
`labelMode`, incluso si el deslizador tiene una definición. Se usa
el alias nativo `6` solo cuando consta una selección explícita de
Nombre y valor según CU4. Los deslizadores sueltos y los fragmentos
conservan las reglas históricas de ese campo.

## Transporte

F3/P4/B2 y la vía experta rechazan antes de copiar una fuente con
paneles, incluso vacíos. Un receptor con paneles solo se admite si
la vía certifica pertenencia y recuperación, incluido Redo pendiente.
La envoltura pasiva experta vigente rechaza ese receptor. La copia
ordinaria de controles genera controles sueltos. La duplicación de
un panel vacío genera otro ID local.

`OBS-CU6-M07-GV1-SLIDER-PICK` sigue abierta y no bloqueante. La
captura M07 conserva su política de foco, selección desde Álgebra
y cancelación. El acceso contextual de CU8 respeta la captura activa.
[Limitación CU6](../../../docs/validation/pre_g9b_cu5_cu6_closeout_record.md#obs-cu6-m07-gv1-slider-pick).

## Impacto de verificación

```text
VERIFICATION_CLASS = INTEGRATED_PHASE
frozenAtPhaseStart = true
VERIFICATION_PROFILES = PHASE PRE-G9B-CU8-I, INTEGRATION
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE REGISTRY AND JUNIT INVENTORY
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
GUIDE_IMPACT = UPDATED
selfApproved = false
```

PHASE cubre la presentación y el ciclo documental CU8 junto a sus
regresiones directas. INTEGRATION comprueba la interacción adicional
con persistencia e identidad compartidas, suites completas del kernel
y Desktop, y la superficie de perfil y exportación. No cambian los
prerrequisitos, el JDK, Gradle ni el arranque de la estación.
