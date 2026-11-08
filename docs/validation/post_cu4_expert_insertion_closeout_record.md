# POST-CU4 · Cierre autoral de la inserción experta

**POST-CU4-EXPERT-INSERTION — PASS — AUTHOR APPROVED**

El 2026-11-08 el autor comunicó un smoke satisfactorio del producto `066b53d0…` y
autorizó la adopción y publicación del sucesor al verificar la activación
predeterminada, la limpieza de consola, las regresiones y ambos arranques.
El smoke manual se atribuye al producto ensayado por el autor. El sucesor
se comprobó mediante ensayos automatizados. `selfApproved=false`.

| Identidad | Commit | Árbol |
|---|---|---|
| Base publicada `main` al iniciar | `d5332e400b0ab68afd14273495b9ae5f03e6c911` | `e8ee4b4b371d34f7f22cc081dee1f429963150dd` |
| Smoke manual comunicado | `066b53d0cc2dd8e78cf1c542e6adea820ec77d5c` | `b60b95dec5c6c7de1e52b28c8de878d72f552448` |
| Producto final verificado | `bf25d95355e2cd27555e316cf8024e7fa927052e` | `eabab698973f25b36791780f3809916a180b5208` |

Rama técnica `codex/post-cu4-i3-i5-expert-insertion`. El commit que
contiene este registro es su descendiente documental directo en
`codex/post-cu4-expert-insertion-closeout`. El resultado de la publicación
se conserva por separado en la evidencia local con ambos commits y árboles.

## Delta final y evidencia funcional

- El catálogo habilita la acción estable del perímetro pasivo 2D en
  **Archivo → Insertar**. El override diagnóstico
  `--enableExpertInsertion=false` sigue disponible. Las comprobaciones
  exactas de perfil mantienen el gate y la separación de acciones. Las
  guías incorporadas EN/ES describen el acceso ordinario.
- GeoCeDG usa `INFO` en la consola ordinaria. Se corrigen la severidad de
  la narración de accesibilidad, el mensaje emitido al registrar una imagen y
  la advertencia por una opción ausente. Los niveles y destinos explícitos
  permanecen disponibles. Los fallos de arranque y de recuperación grave emiten
  diagnósticos de error. El informe estructurado del trabajador y las
  comparaciones para consentimiento siguen disponibles.
- El launcher Gradle y la app-image interna Windows x64 arrancaron sin
  argumentos de aplicación. Cada ensayo recorrió los diálogos reales en
  español e inglés con un receptor que contenía una edición sin guardar.
  Se mostró el arco que P4 rechaza, el paso de `u` de capa 0 a 5 y la
  incorporación del arco a la vista gráfica 1, con su pertenencia a la
  vista 2 conservada. El informe y la vista previa precedieron a aceptar.
- Ambos recorridos comprobaron cancelación, rechazo de una dependencia
  rota, recuperación certificada ante un fallo después de mutar,
  aceptación, Deshacer, Rehacer, Guardar como y reapertura de una copia.
  Verificaron el UUID receptor, los puertos del arco, su actualización
  al mover `C` y la persistencia de la capa 5 y de la pertenencia a vistas.
  Los cuatro trabajadores de cada ensayo terminaron y sus temporales
  se limpiaron. La app-image se ejecutó fuera del checkout con la JVM
  incluida y sus propios recursos.
- Las consolas ordinarias se verificaron sin trazas DEBUG. El control
  explícito comprobó las severidades DEBUG y ERROR. El control Classic
  conservó su aplicación, menú y configuración de nivel predeterminada.

## Verificación del producto

| Comprobación | Resultado y trazabilidad |
|---|---|
| Focales previas a la congelación | 59/59 Desktop y 41/41 del validador del perfil. La comprobación de producto se repitió con el conjunto integrado del SHA congelado. |
| `STATIC` | Código 0, `ACCEPTED / COMPLETE`, 3 obligaciones. Ejecución `verification-03ec1182cc5d4e039991ae2c6ffdaaf6`, SHA-256 del informe tipado `b836b805f2a5a0411f477529fbdaccd03038318c6cea36fe58f221686880867f`. |
| `PHASE POST-CU4-I3-I5-EXPERT-INSERTION` | Código 0, `ACCEPTED / COMPLETE`, 6 obligaciones. 228 casos Desktop y 35 compartidos, incluidas las regresiones I2–I5, F3/P4/B2, CU4, vigencia, recuperación, geometría válida y persistencia. Ejecución `verification-f4175924e794445397bea4dfde6d48b2`, SHA-256 `9dcd93dad58f38d8ed6aa581c5f6af478fcd95e384b71fb5ac0f5dccac56d9f5`. |
| Smoke ordinario | Código 0 para el arranque ordinario, diagnóstico y Classic. SHA-256 de los eventos del recorrido ordinario `e92deea34dd8fd10a17c05a4c6c700cc0dbf59d8d8c95422a888abfa2c700896`. |
| App-image `INTERNAL` | Build y smoke con código 0. 433 archivos cotejados. SHA-256 del manifiesto de la imagen `c18b118aabfca15e664eaef04361dcd928e99000151d2394a26bb5d0547ef926`. SHA-256 de los eventos del recorrido `46bf9aad328f099f8c28454253f0fec132a696523b5bb1f0a967bb0e7f7b86c1`. |

`CHANGE_ROUTE = ORDINARY` · `VERIFICATION_CLASS = INTEGRATED_PHASE`.
El plan `PHASE` aporta las regresiones integradas exigibles. Se resolvieron
los planes globales `INTEGRATION` y `PACKAGING` para delimitar cobertura.
El ensayo de app-image cubrió el paquete interno solicitado. El perfil global
`INTEGRATION` y la campaña `FINAL` quedan fuera de esta clase. La aceptación
se apoya en los informes tipados del producto.

`GUIDE_IMPACT = UPDATED` en el producto por el acceso ordinario.
`BOOTSTRAP IMPACT = NO CHANGE REQUIRED` porque usa el launcher y los
prerrequisitos de escritorio existentes. El ajuste de las huellas de perfil y
JUnit conserva los criterios y filtros y refleja una aserción renombrada y
la nueva política de activación.

## Adopción y límites

La [normativa experta v1](../../geocedg/specs/ui/expert-document-insertion-candidate.md)
adopta EXP-01–EXP-04 dentro del perímetro demostrado. La revisión pasiva
del estado vivo, el informe, el consentimiento vigente, el ensayo aislado,
la incorporación transaccional, la recuperación, la persistencia y el
guardado explícito son parte de ese perímetro. Se pueden admitir cambios
de capa, vistas, presentación y geometría válida identificados por el
informe, y equivalencias sin establecer cuando las garantías de integridad
están verificadas. El caso `Point(s,t)` cambia de `(0,0)` a `(3,0)`
en el segmento de `(0,0)` a `(4,0)` con `t=0.75`, por el modo nativo
de parámetros sobre trayectoria del receptor. Se conservan el productor
`AlgoPointOnPath`, sus dependencias y la actualización dinámica.

La vía [certificada F3/P4/B2](../../geocedg/specs/ui/document-insertion.md)
mantiene su admisión y garantías. ARC-01/P4 y la ampliación de oráculos y
familias quedan como trabajo futuro. La primera versión excluye macros fuera
del perímetro demostrado, contenido activo, 3D y semántica espacial, el
remapeo durable participante y la conversión angular entre documentos.
Geometría malformada, pérdida de dependencias y falta de garantías de
recuperación bloquean la incorporación. La cobertura empaquetada de este
cierre corresponde a Windows x64 y distribución interna.

## Publicación y evidencia

La autorización permite fast-forward no forzado a `main` desde la base
esperada y bajo las reglas remotas. Este cierre es
`DOCUMENTATION_STATUS_ONLY` y su comprobación documental tiene identidad
propia. Los registros CU4 conservan su atribución histórica.

En la consulta previa al cierre, GitHub devolvió `main` bloqueada y exigía
un pull request con una aprobación. Esa configuración impide el avance
directo autorizado. El cierre queda preparado en la rama de revisión. Las
referencias remotas verificadas se conservan con la evidencia local.

Evidencia local en `artifacts/agent/post-cu4-expert-closeout/`, con
planes, resultados, intentos fallidos, manifiestos y comprobaciones de
identidad. El [protocolo de smoke](post_cu4_expert_insertion_smoke.md)
y el [espejo estructurado](../../geocedg/validation/post-cu4-expert-insertion/post-cu4-expert-insertion-closeout.json)
conservan las referencias.
