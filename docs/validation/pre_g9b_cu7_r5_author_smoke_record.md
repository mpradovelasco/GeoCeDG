# CU7 R5 — registro de smoke comunicado por el autor

Estado: **SMOKE PASS — AUTHOR REPORTED**.
Fecha de comunicación registrada: 2026-11-05, Europe/Madrid.
Procedencia: mensaje directo del autor en el chat de auditoría independiente R5,
apartado 2 del encargo «Auditoría independiente final, registro autoral y
preparación condicionada del cierre reducido».

> El smoke es perfecto.

El autor confirma que finalmente utilizó el worktree R5 después de detectar y
corregir una ejecución inicial equivocada desde main. Vincula su comunicación a:

- Commit: `13ee3b8746d4d874824eb44ca1d05a3e57b494c2`.
- Árbol técnico comprobado por la auditoría: `a4fead2412bbb8859dd167e3c22b82c54e584633`.
- Rama: `codex/pre-g9b-cu7-general-point-interaction-r5`.
- Worktree: `artifacts/cu7-r5-worktree`.

No ha comunicado defectos funcionales durante el smoke sobre la versión correcta.
No se proporcionó una lista individual de pasos; este registro no la infiere ni
afirma que ejecutara todos los casos del protocolo del implementador.
La fecha anterior es la de la comunicación registrada; no se inventan hora,
duración ni fecha independiente de cada operación física.

Esta evidencia es distinta del Computer Use/Win32 del implementador, de las
pruebas AWT y de las verificaciones del kernel. La auditoría revisó por separado
la identidad del candidato y la evidencia técnica; no presenció el smoke autoral.

El registro independiente original se conserva en
`artifacts/cu7-r5-independent-audit/author-smoke.json`, fuera del candidato.
Se incorpora ahora a una propuesta documental descendiente de R5 sin alterar
su commit. No equivale a aprobación autoral del paquete CU7, adopción de D1,
decisión de interfaz o autorización de promoción. `selfApproved=false`.
