# POST-CU4 I3–I5 · Smoke autoral Desktop

Usar el build del SHA y árbol indicados en el informe técnico. Crear un
directorio de prueba nuevo. Los archivos son sintéticos. El generador escribe
solo en el destino indicado y registra el SHA-256 de cada entrada.

```powershell
$fixtures = 'artifacts/agent/post-cu4-i3-i5/author-smoke'
./tools/agent/probes/post-cu4-i3-i5/new-smoke-fixtures.ps1 -OutputDirectory $fixtures
$preferences = Join-Path (Resolve-Path $fixtures).Path 'smoke-preferences.properties'
.\gradlew.bat :desktop:desktop:runGeoCeDG "--args=--enableExpertInsertion=true --settingsfile=`"$preferences`""
```

| Paso | Acción en español | Expected result in English |
|---:|---|---|
| 1 | Abre `receiver-layer-four.ggb`. Añade `SinGuardar=(4,1)` y deja el dibujo sin guardar. | The current receiver contains `A` and the unsaved point. The files on disk retain their original hashes. |
| 2 | En **Archivo → Insertar → Insertar archivo…**, selecciona `source-arc.ggb`. | **File → Insert → Insert File…** refuses the direct arc under certified admission. The drawing stays intact. |
| 3 | Abre **Insertar con revisión experta…** y elige el mismo origen. Inspecciona el resumen, el detalle y la vista previa. Comprueba que se puede aceptar el resultado. | **Insert with expert review…** shows the trial, including the resident `A`, its unsaved peer, the added points and arc, and the label remap. In an isolated profile, the source scalar `u` starts on layer 0 and the receiver's layer context yields layer 4. The detail identifies both layers and the confirmation is enabled. |
| 4 | Cancela y comprueba que siguen solo `A` y `SinGuardar`. Vuelve a abrir y acepta una oferta nueva. | Cancel leaves the receiver intact. A fresh acceptance adds the reviewed objects. No file is written by insertion. |
| 5 | Sin mover objetos aún, usa Deshacer y Rehacer. Después arrastra `C`. | One Undo removes the added construction. Redo restores it. Moving `C` changes the arc endpoint and sweep while preserving the center and radius. |
| 6 | Elige **Guardar como…** y acepta una ruta `.cedg` distinta. Reabre esa copia y mueve `C` otra vez. | The separate copy reopens with the live dependencies, the prior unsaved point and the remapped labels. The source and initial receiver hashes remain unchanged. |
| 7 | Desde la acción experta, selecciona `source-unusable-direction.ggb`, `source-broken-arc.ggb` y `source-invalid.ggb`. | The first lacks a usable terminal ray for a defined arc. The second has a dangling reference. The last is a malformed archive. Each review identifies the blocked condition and offers no operative confirmation. |

Anotar SHA, idioma, nombre de cada fixture, valor observado de capa, ruta de
la copia y cualquier diferencia respecto a la tabla. Si un catálogo local
altera el máximo inicial de capas, repetir el paso 3 en un perfil de prueba
vacío y registrar ambos contextos. Para el paso 6 puede usarse `Guardar`
cuando el aviso redirige a `Guardar como…`.
