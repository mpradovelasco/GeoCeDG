# PRE-G9B-CU3 — Revisión arquitectónica focal K6: política del documento durante la carga

| Campo | Valor |
|---|---|
| Carácter | Revisión previa al cambio del mecanismo de carga, exigida por la decisión autoral de 2026-10-28 («Aprobación de G-B2-1 y autorización condicionada de implementación», §3). No aprueba nada |
| Origen | [expediente G-B2-1](../validation/pre_g9b_cu3_b2_g1_characterization_record.md), K6 |
| Estado | revisión documentada antes del cambio; `selfApproved=false` |

## 1. Mecanismo vigente (leído en el código)

- **Escritura.** `Construction` escribe la sección `geocedgSpatial`, que contiene el registro de
  autoridad del documento (`authorityPolicy`), **antes** de los elementos de la construcción.
- **Lectura.** `MyXMLHandler` abre una `SpatialIdentityRegistry.LoadSession` al leer la sección. El
  registro de autoridad se lee y se valida en `stageAuthorityRecord` (un único registro; versión no
  admitida o atributos mal formados fallan cerrados), pero solo se **publica** en
  `LoadSession.commit()` → `restoreDocumentAuthority()`, que el manejador llama al cerrar
  `</construction>` (`finishSpatialIdentityLoad`).
- **Política vigente durante el análisis.** Un análisis que restaura el documento entero limpia el
  registro (`clearLiveState`: `documentAuthority = null`, política heredada). Entre la sección y
  `</construction>`, por tanto, todos los elementos se crean y se evalúan con la política heredada,
  aunque el documento persista la autoridad constructiva.
- **Efecto (K6).** Esas evaluaciones confirman en el libro de tokens selectores transversales y
  reservan encarnaciones; si el ancla no está reclamada, además publican un estado sin ella. La
  evaluación posterior, ya con la política restaurada, reutiliza las anclas reclamadas.

## 2. Corrección propuesta (vía 1: restaurar y validar la política antes de evaluar)

En `LoadSession`, y solo para una carga que gobierna la política de un documento (no una importación
del portapapeles, no una construcción de macro, con propietario):

1. tras validar el registro de autoridad en `stageAuthorityRecord`, publicarlo como política vigente
   del registro, de modo que los elementos que siguen se evalúen con la política persistida;
2. guardar la política que tenía el registro al abrir la sesión y restaurarla en `abort()` si la
   carga falla (atomicidad);
3. `commit()` no cambia: `restoreDocumentAuthority()` vuelve a publicar el mismo registro.

| Propiedad | Por qué se cumple |
|---|---|
| Formato | no cambia: el registro y su posición son los vigentes |
| Contrato de identidad durable | no cambia: ningún germen, token ni libro cambia; solo desaparece la evaluación con una política distinta de la persistida |
| Documentos heredados | sin registro, nada se publica antes: siguen con la política heredada |
| Rechazo cerrado | la publicación anticipada ocurre solo tras una lectura válida; un registro mal formado, duplicado, de versión no admitida o en un lugar no permitido sigue fallando cerrado, y `abort()` restaura la política anterior |
| Importación del portapapeles y macros | excluidas: la política del destino gobierna, como hoy |
| Reconstrucciones por redefinición | se rigen por el registro del propio documento, que es el mismo |

## 3. Alcance y límites

- **Orden canónico.** La garantía «ninguna evaluación autoritativa con una política distinta de la
  persistida» vale para el orden que escribe el producto (sección antes de los elementos). Un archivo
  editado a mano con la sección después de algún elemento evaluaría esos elementos con la política
  heredada, como hoy; la evaluación posterior a la carga los recalcula. No se amplía el cargador para
  ese caso.
- **No se toca** el ciclo de vida general de carga, el formato del documento ni otros subsistemas.
- **Vía 2 (confirmación diferida del libro)** no es necesaria si la vía 1 elimina las reservas
  transitorias; se descarta si las pruebas lo confirman.

## 4. Verificación exigida (decisión autoral, §3, puntos 1 a 10)

Pruebas focales nuevas, con la sonda G-B2-1 como control (ninguna asignación transitoria en la carga):
documentos heredados y constructivos; carga, reapertura y guardado sin cambios con estado del libro
idéntico; rechazo cerrado de registros desconocidos, duplicados o incompatibles con restauración de la
política; atomicidad ante un error de carga; encarnaciones y reclamaciones; anclas `F` reclamadas y no
reclamadas; entradas `T`; Deshacer/Rehacer, copia y duplicación; formatos anteriores del libro; y
ausencia de evaluaciones con la política heredada en un documento constructivo.
