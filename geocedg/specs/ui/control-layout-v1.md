# CU8-I · Disposición de controles v1

Estado experimental en GeoCeDG Desktop, Gráficos 1. El diseño adoptado
define la elegibilidad, las operaciones y sus invariantes.
[CU8-D, §2 y §5](../../../docs/architecture/pre_g9b_cu8_d_control_containers_candidate.md)

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
contrato. La autorización cubre únicamente la disposición. Los
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
La advertencia de la distribución nueva indica las dos limitaciones.
Los lectores nuevos conservan el desbloqueo de la casilla agrupada.

## Formato

Dos atributos ASCII sin namespace. El registro pertenece a la apertura
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
  65.536 bytes ASCII de valores de ambos atributos. Con cero paneles
  se omiten las dos marcas y se conserva la escritura histórica.

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

## Transporte

F3/P4/B2 y la vía experta rechazan antes de copiar una fuente con
paneles, incluso vacíos. Un receptor con paneles solo se admite si
la vía certifica pertenencia y recuperación, incluido Redo pendiente.
La envoltura pasiva experta vigente rechaza ese receptor. La copia
ordinaria de controles genera controles sueltos. La duplicación de
un panel vacío genera otro ID local.

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
