# CU7 R5 — propuesta de adopción condicionada D1

2026-11-05. **PROPUESTA PARA DECISIÓN AUTORAL; NO VIGENTE**.
Recomendación independiente: **ADOPTAR CON CONDICIONES**.
`selfApproved=false`; no se modifica ningún contrato aprobado.

## Objeto y autoridad

La excepción candidata afecta exclusivamente a la cláusula histórica de identidad
de bytes XML al añadir `conicPointState` versión 1. No cambia la definición
geométrica de un punto, R6/H1, permisos forward, fuente o identidad durable.
No debe confundirse esta D1 con anteriores incrementos Desktop del mismo nombre.

Candidato auditado: `13ee3b8746d4d874824eb44ca1d05a3e57b494c2`,
árbol `a4fead2412bbb8859dd167e3c22b82c54e584633`.
La sucesión del diseño está en
[propuesta original y correcciones](pre_g9b_cu7_native_conic_state_candidate.md),
[restauración R3](pre_g9b_cu7_r3_native_undo_companion_design.md) y
[presupuesto R4](pre_g9b_cu7_r4_native_budget_design.md).
Las restricciones iniciales supersedidas de esos documentos no sustituyen las
tablas positivas del código congelado.

Se propone que la decisión autoral adopte el siguiente texto, y que sólo después
se incorpore una enmienda fechada a las cláusulas aprobadas pertinentes, preservando
sus antecedentes y los cierres históricos.

## Texto candidato de enmienda

1. La carga de archivos antiguos sin el campo conserva su interpretación
   histórica. La serialización ordinaria GeoCeDG puede añadir un único
   `conicPointState` versión 1 por punto elegible. Ninguna otra modificación de
   XML heredado queda autorizada por esta excepción.
2. El campo corresponde al punto nativo 2D sobre una cónica completa, creado por
   el algoritmo libre `AlgoPointOnPath` de un input. No sustituye el parámetro
   explícito de `Point(c,numeric)`, ni extiende el formato a partes de cónicas,
   3D, macros o cualquier implementación arbitraria de Path.
3. El lector exige los nueve atributos exactos de v1: version, t, pathType, x, y,
   z, tempT, tempPathType y animation. Debe rechazar versiones desconocidas,
   atributos sobrantes/incompletos, duplicados, contenido/anidamiento, ubicación
   u orden incompatible con coords/curveParam y propiedad algorítmica incorrecta.
   Se validan valores, coherencia de coordenadas, parámetro, fallback y animación
   con el estado nativo. La representación all-NaN sólo pertenece a las
   condiciones undefined expresamente reconocidas; no es un permiso para emitir
   un estado arbitrario ni entra en la excepción finita del fingerprint.
4. La emisión FILE necesita prueba positiva fresca y transitiva del cierre de
   fuentes mediante `NativeSourceFidelity2D`, además de coherencia local. Se
   revisan ancestros y las posiciones realmente serializadas. Una fuente no
   soportada, un ciclo o un presupuesto agotado impiden emitir el campo; se mantiene
   el payload heredado legible y no se promete fidelidad paramétrica exacta de ese
   fallback. No se admite un checkpoint aislado cuyo ancestro no sea reproducible.
5. La excepción de comparación de fingerprints del corpus es finita y estricta.
   Sólo se retira la línea completa reconocida y validada por
   `PreG9BR6PlusD1LegacyFingerprints`: propietario real único en la construcción
   principal, algoritmo/input elegibles, gramática exacta, igualdad de cada campo
   con el punto vivo, reconstrucción forward nativa independiente y coincidencia
   con el escritor actual. Los hashes de referencia y archivos inmutables no
   cambian. Todo byte restante, espacios y estructura incluidos, sigue sujeto al
   hash original. No se permite normalización DOM, borrado por nombre de etiqueta
   sin validación ni una excepción para otros atributos.
6. El fingerprint STRUCTURE del evaluador y el fingerprint de compatibilidad de
   bytes son contratos distintos. Clasificar el campo como estado derivado del
   conductor en STRUCTURE no elimina las entradas independientes de VALUES ni
   permite ocultar cambios de la construcción.
7. Classic conserva su escritor histórico y comparación de XML íntegro. El
   escritor nuevo excluye MacroKernel; el lector compartido estricto no implica
   garantía de conservación al guardar después con Classic o un lector antiguo.
   No se autoriza migración in situ de un kernel Classic con historia de
   orientación a un kernel GeoCeDG.
8. MEMORY, rollback y Undo/Redo mantienen sus restauradores y correspondencias
   privadas tipadas, con sus propias tablas positivas. Un estado admitido allí
   no queda automáticamente certificado para FILE. La asociación a nodos
   reconstruidos no se deduce por etiquetas, índices, coordenadas o proximidad.
9. La igualdad demostrada se refiere al corpus, estados y entorno ejecutados.
   No se promete identidad de bits entre plataformas no ensayadas, todo estado
   degenerado, todos los documentos ni todos los caminos nativos.

## Condiciones auditadas y evidencia

- El delta R5 no cambia el escritor, lectores, Construction, AlgoPointOnPath ni
  Undo. Sus contratos R3/R4 fueron revisados de forma selectiva; la aceptación
  R5 conserva sus regresiones efectivas.
- FILE usa una pasada fresca por serialización; presupuesto de cierre nativo
  4096 nodos nuevos, 65536 aristas y profundidad 256. Memoización no consume
  exploración nueva. SUPPORTED, UNSUPPORTED y RESOURCE_EXHAUSTED son distintos.
  No se convierte ese presupuesto en un límite global de objetos del documento.
- La tabla de algoritmos, tipos y operaciones se define exhaustivamente en
  NativeSourceFidelity2D del SHA auditado; una familia nominal por sí sola no
  basta. Los restauradores MEMORY y Undo no amplían esa tabla FILE.
- Los hashes de la entrega y cinco informes fueron reconciliados, el recibo
  FINAL es válido, y los 21 documentos GUI revisados tienen XML legible.
  La prueba de ausencia de XML inválido para la nueva emisión se apoya en las
  guardas, gramática y regresiones, no en generalizar sólo esos 21 ejemplos.
- Los intentos históricos fallidos, corpus y fingerprints quedan preservados.
  El fallback FILE no se anuncia como restauración exacta.

Véanse [auditoría](../validation/pre_g9b_cu7_r5_independent_audit.md) y
[expediente](../validation/pre_g9b_cu7_r5_reduced_closeout_proposal.md).
La adopción requiere decisión explícita del autor; el smoke favorable no la
sustituye. Si se rechaza esta excepción, no debe aprobarse el cierre reducido
con el contrato de bytes actual y este candidato sin resolver esa incompatibilidad.
