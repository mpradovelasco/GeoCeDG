// Independent mathematical reconstruction (NOT GeoCeDG, not authoritative): oblique cone x oblique
// cylinder with a common base plane, elevation/plan projections, and the development of the cone
// through the spherical support curve. Inputs are the saved free parameters of the model copy.
// Usage: node geometry.js <dag-final2.json> <outDir>
const fs = require('fs');
const [,, dagPath, outDir] = process.argv;
const dag = JSON.parse(fs.readFileSync(dagPath, 'utf8'));
const P2 = l => { const c = dag.elems[l].coords; return [+c.x / +c.z, +c.y / +c.z]; };
const num = l => +dag.elems[l].value;

// ---- saved data
const A = P2('A'), B = P2('B'), H = P2('H'), I = P2('I'), Cc = P2('C_c'), Ppt = P2('P');
const D1 = P2('D_1'), S1 = P2('S_1'), E1s = P2('E_1'), Ns = P2('N');
const rCone = num('rbCone'), rCyl = num('rbCylinder');
const z0 = A[1];                       // elevation ordinate of the base plane (line h)
const yAxis = B[1];                    // plan ordinate of line i (frontal symmetry plane)
const V = [H[0], I[1], H[1]];          // cone vertex (x, depth, height)
const aCyl = (() => { const Lp = P2("L&apos;"); const d = [Lp[0] - A[0], Lp[1] - A[1]]; const n = Math.hypot(...d); return [d[0] / n, 0, d[1] / n]; })();
const rho = Math.hypot(H[0] - D1[0], H[1] - D1[1]);

const sub = (a, b) => a.map((x, i) => x - b[i]);
const add = (a, b) => a.map((x, i) => x + b[i]);
const mul = (a, s) => a.map(x => x * s);
const norm = a => Math.hypot(...a);

// ---- cone base point by driver parameter t (GeoGebra circle path parameter = polar angle)
const E = t => [B[0] + rCone * Math.cos(t), B[1] + rCone * Math.sin(t)];
const tOf = p => Math.atan2(p[1] - B[1], p[0] - B[0]);
const foot3 = e => [e[0], e[1], z0];

// ---- cylinder driver N(theta) and the far root R of Ray(P,N) with circle p
const N = th => [Cc[0] + rCyl * Math.cos(th), Cc[1] + rCyl * Math.sin(th)];
function rayCircle(P, Q) { // intersections of the ray P->Q with circle p; returns [near, far] or null
  const d = sub(Q, P), f = sub(P, B);
  const a = d[0] * d[0] + d[1] * d[1], b = 2 * (f[0] * d[0] + f[1] * d[1]), c = f[0] * f[0] + f[1] * f[1] - rCone * rCone;
  const disc = b * b - 4 * a * c;
  if (disc < 0) return null;
  const s = Math.sqrt(disc);
  const u1 = (-b - s) / (2 * a), u2 = (-b + s) / (2 * a);
  return [add(P, mul(d, u1)), add(P, mul(d, u2))];
}
// space point: cylinder generatrix through N meets cone generatrix through foot F
function spacePoint(n2, f2) {
  // n + lam*a = V + mu*(F - V); solve in (x, z) then check depth
  const n = [n2[0], n2[1], z0], F = foot3(f2), g = sub(F, V);
  // x: n0 + lam*a0 = V0 + mu*g0 ; z: n2 + lam*a2 = V2 + mu*g2
  const det = aCyl[0] * (-g[2]) - (-g[0]) * aCyl[2];
  const rx = V[0] - n[0], rz = V[2] - n[2];
  const lam = (rx * (-g[2]) - (-g[0]) * rz) / det;
  const mu = (aCyl[0] * rz - aCyl[2] * rx) / det;
  const X = add(V, mul(g, mu));
  const depthErr = (n[1] + lam * aCyl[1]) - X[1];
  return { X, mu, lam, depthErr };
}
function W(th, which /* 'R' far | 'Q' near */) {
  const n = N(th), rc = rayCircle(Ppt, n);
  const f = which === 'Q' ? rc[0] : rc[1];
  const sp = spacePoint(n, f);
  return { th, n, foot: f, X: sp.X, mu: sp.mu, depthErr: sp.depthErr, elev: [sp.X[0], sp.X[2]], plan: [sp.X[0], sp.X[1]] };
}

// ---- spherical support curve and developed angle Theta(t), t in [0, 2pi) measured from t=0 increasing
function sph(t) { const g = sub(foot3(E(t)), V); return add(V, mul(g, rho / norm(g))); }
function gauss(f, a, b, n = 2000) { // composite Simpson
  let s = f(a) + f(b); const h = (b - a) / n;
  for (let i = 1; i < n; i++) s += f(a + i * h) * (i % 2 ? 4 : 2);
  return s * h / 3;
}
const speed = t => { const e = 1e-6; return norm(sub(sph(t + e), sph(t - e))) / (2 * e); };
const thetaTab = (() => { const M = 7200, tab = [0]; for (let i = 1; i <= M; i++) tab.push(tab[i - 1] + gauss(speed, (i - 1) * 2 * Math.PI / M, i * 2 * Math.PI / M, 8) / rho); return tab; })();
function Theta(t) { // developed angle of the generatrix with driver parameter t, origin t=0, increasing
  let u = ((t % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
  const M = thetaTab.length - 1, x = u / (2 * Math.PI) * M, i = Math.min(M - 1, Math.floor(x));
  return thetaTab[i] + (thetaTab[i + 1] - thetaTab[i]) * (x - i);
}
const ThetaTotal = thetaTab[thetaTab.length - 1];
function develop(r, t) { const th = Theta(t); return [S1[0] - r * Math.sin(th), S1[1] - r * Math.cos(th)]; } // (0,-1) rotated clockwise by th

// ---- roots of the elevation curve o (R family) with the elevation ray q_1(t) = Ray(H, F_1)
function rootsOnRay(t, which = 'R', M = 20000) {
  const e = E(t), F1 = [e[0], z0], d = sub(F1, H);
  const h = th => { const w = W(th, which).elev; return (w[0] - H[0]) * d[1] - (w[1] - H[1]) * d[0]; };
  const out = [];
  let prev = h(0);
  for (let i = 1; i <= M; i++) {
    const th = i * 2 * Math.PI / M, cur = h(th);
    if (prev === 0 || prev * cur < 0) {
      let a = (i - 1) * 2 * Math.PI / M, b = th, fa = prev;
      for (let k = 0; k < 80; k++) { const m = 0.5 * (a + b), fm = h(m); if (fa * fm <= 0) b = m; else { a = m; fa = fm; } }
      const th0 = 0.5 * (a + b), w = W(th0, which);
      const along = (w.elev[0] - H[0]) * d[0] + (w.elev[1] - H[1]) * d[1];
      if (along >= 0) out.push(w);
    }
    prev = cur;
  }
  return out;
}

const res = {};
res.setup = { A, B, H, I, Cc, P: Ppt, D1, S1, rCone, rCyl, z0, yAxis, V, aCyl, rho, ThetaTotalRad: ThetaTotal, ThetaTotalDeg: ThetaTotal * 180 / Math.PI };

// ---- checks against saved objects
const tS = tOf(E1s);
res.savedCheck = {
  tSaved: tS, tSavedDeg: tS * 180 / Math.PI,
  ThetaAtSaved: Theta(tS), angSCbisSaved: num('angSCbis'),
  o2_over_rho: num('o_2') / rho,
  W_saved_model: P2('W'), W_saved_math: W(Math.atan2(Ns[1] - Cc[1], Ns[0] - Cc[0]), 'R').elev,
  R_saved_model: P2('R'), R_saved_math: W(Math.atan2(Ns[1] - Cc[1], Ns[0] - Cc[0]), 'R').foot,
};

// ---- R family foot range (tangent rays from P to g_1) and driver domain of incidence
const feet = []; for (let i = 0; i < 20000; i++) { const w = W(i * 2 * Math.PI / 20000, 'R'); feet.push(w); }
const fx = feet.map(w => w.foot[0]), fy = feet.map(w => w.foot[1]);
const ft = feet.map(w => tOf(w.foot));
res.RfamilyFoot = { xMin: Math.min(...fx), xMax: Math.max(...fx), yMin: Math.min(...fy), yMax: Math.max(...fy), tMinDeg: Math.min(...ft) * 180 / Math.PI, tMaxDeg: Math.max(...ft) * 180 / Math.PI, maxDepthErr: Math.max(...feet.map(w => Math.abs(w.depthErr))) };
const qfeet = []; for (let i = 0; i < 20000; i++) qfeet.push(W(i * 2 * Math.PI / 20000, 'Q'));
res.QfamilyFoot = { xMin: Math.min(...qfeet.map(w => w.foot[0])), xMax: Math.max(...qfeet.map(w => w.foot[0])), tMinDeg: Math.min(...qfeet.map(w => tOf(w.foot))) * 180 / Math.PI, tMaxDeg: Math.max(...qfeet.map(w => tOf(w.foot))) * 180 / Math.PI };
res.E1xRange = [B[0] - rCone, B[0] + rCone];
const cLo = (res.RfamilyFoot.xMin - B[0]) / rCone, cHi = (res.RfamilyFoot.xMax - B[0]) / rCone;
res.driverDomainByProjection = { absTdegFrom: Math.acos(cHi) * 180 / Math.PI, absTdegTo: Math.acos(cLo) * 180 / Math.PI, note: 'q_1 meets o iff E_1x in [xMin,xMax]; true for +t (same generatrix) and -t (mirror generatrix)' };

// ---- differential pair: saved E_1 and its mirror in line i
function pair(t, label) {
  const roots = rootsOnRay(t, 'R');
  return {
    label, t, tDeg: t * 180 / Math.PI, E1: E(t), Theta: Theta(t), nRoots: roots.length,
    roots: roots.map(w => {
      const r = norm(sub(w.X, V)), tFoot = tOf(w.foot);
      return {
        thetaN: w.th, elev: w.elev, plan: w.plan, foot: w.foot, footTdeg: tFoot * 180 / Math.PI,
        footEqualsE1: Math.hypot(w.foot[0] - E(t)[0], w.foot[1] - E(t)[1]) < 1e-6,
        trueRadius: r, developedByModel: develop(r, t), developedTrue: develop(r, tFoot),
      };
    }),
  };
}
res.pairSaved = pair(tS, 'saved E_1');
res.pairMirror = pair(-tS, 'mirror of E_1 in line i');
res.modelSaved = { C_2: P2('C_2'), D_2: P2('D_2'), G_2: P2('G_2'), H_2: P2('H_2'), F_2: P2('F_2'), E_2: P2('E_2') };

// ---- curves: (a) true developed hole contour by direct transport over N; (b) what the saved procedure traces
const rows = ['family\tthetaN\tfootTdeg\televX\televY\tplanX\tplanY\ttrueRadius\tdevX\tdevY'];
for (const fam of ['R', 'Q']) {
  for (let i = 0; i <= 720; i++) {
    const w = W(i * 2 * Math.PI / 720, fam), r = norm(sub(w.X, V)), tf = tOf(w.foot), d = develop(r, tf);
    rows.push([fam, w.th, tf * 180 / Math.PI, ...w.elev, ...w.plan, r, ...d].join('\t'));
  }
}
fs.writeFileSync(outDir + '/math-direct-transport.tsv', rows.join('\n') + '\n');
const rows2 = ['tDeg\tnRoots\trootIdx\televX\televY\tfootTdeg\tsameGeneratrix\ttrueRadius\tdevModelX\tdevModelY'];
for (let i = 0; i < 720; i++) {
  const t = -Math.PI + (i + 0.5) * 2 * Math.PI / 720, roots = rootsOnRay(t, 'R', 4000);
  if (!roots.length) { rows2.push([t * 180 / Math.PI, 0, '', '', '', '', '', '', '', ''].join('\t')); continue; }
  roots.sort((a, b) => b.elev[1] - a.elev[1]);
  roots.forEach((w, k) => { const r = norm(sub(w.X, V)), tf = tOf(w.foot), d = develop(r, t); rows2.push([t * 180 / Math.PI, roots.length, k, ...w.elev, tf * 180 / Math.PI, Math.abs(tf - t) < 1e-6, r, ...d].join('\t')); });
}
fs.writeFileSync(outDir + '/math-saved-procedure.tsv', rows2.join('\n') + '\n');
fs.writeFileSync(outDir + '/math-summary.json', JSON.stringify(res, null, 1));
console.log(JSON.stringify(res, null, 1));

// ---- optional: compare a kernel sweep (SWEEPROW lines of the external probe) with this reconstruction
if (process.argv[4]) {
  const lines = fs.readFileSync(process.argv[4], 'utf8').split(/\r?\n/).filter(l => l.startsWith('SWEEPROW'));
  let maxG = 0, maxH = 0, maxC = 0, maxD = 0, nDef = 0, nUndef = 0, mismatchDomain = 0, cHigher = 0, sameGen = 0, mirrorGen = 0;
  const outRows = ['i\ttDeg\tkernelDefined\tmathRoots\tsameGeneratrix\tdC2\tdD2\tdG2\tdH2\tC2thetaN\tD2thetaN'];
  for (const l of lines) {
    const f = l.replace('SWEEPROW ', '').split('\t');
    const i = +f[0], t = +f[1];
    const C = [+f[7], +f[8]], Dk = [+f[9], +f[10]], G = [+f[11], +f[12]], Hk = [+f[13], +f[14]];
    const roots = rootsOnRay(t, 'R', 20000);
    const def = Number.isFinite(C[0]) && Number.isFinite(Dk[0]);
    if (def !== (roots.length === 2)) mismatchDomain++;
    if (!def) { nUndef++; outRows.push([i, t * 180 / Math.PI, def, roots.length].join('\t')); continue; }
    nDef++;
    if (roots.length !== 2) { outRows.push([i, t * 180 / Math.PI, def, roots.length].join('\t')); continue; }
    roots.sort((a, b) => b.elev[1] - a.elev[1]); // higher elevation first
    const d = (p, q) => Math.hypot(p[0] - q[0], p[1] - q[1]);
    const cIsHigher = d(C, roots[0].elev) < d(C, roots[1].elev);
    if (cIsHigher) cHigher++;
    const rc = cIsHigher ? roots[0] : roots[1], rd = cIsHigher ? roots[1] : roots[0];
    const same = Math.abs(tOf(rc.foot) - t) < 1e-6; if (same) sameGen++; else mirrorGen++;
    const gM = develop(norm(sub(rc.X, V)), t), hM = develop(norm(sub(rd.X, V)), t);
    const e = [d(C, rc.elev), d(Dk, rd.elev), d(G, gM), d(Hk, hM)];
    maxC = Math.max(maxC, e[0]); maxD = Math.max(maxD, e[1]); maxG = Math.max(maxG, e[2]); maxH = Math.max(maxH, e[3]);
    outRows.push([i, t * 180 / Math.PI, def, roots.length, same, ...e, rc.th, rd.th].join('\t'));
  }
  const name = require('path').basename(process.argv[4]).replace(/\.txt$/, '');
  fs.writeFileSync(outDir + '/compare-' + name + '.tsv', outRows.join('\n') + '\n');
  console.log('SWEEP-COMPARE ' + name + ' ' + JSON.stringify({ rows: lines.length, kernelDefined: nDef, kernelUndefined: nUndef, domainMismatch: mismatchDomain, definedOnSameGeneratrix: sameGen, definedOnMirrorGeneratrix: mirrorGen, C2isHigherRoot: cHigher, maxAbsDiff: { C_2: maxC, D_2: maxD, G_2: maxG, H_2: maxH } }));
}

// ---- developed base contour (transformada de la base) and exploratory sample positions, for the figure
{
  const rowsB = ['tDeg\tdevX\tdevY'];
  for (let i = 0; i <= 720; i++) { const t = i * 2 * Math.PI / 720 * 0.999999; const d = develop(norm(sub(foot3(E(t)), V)), t); rowsB.push([t * 180 / Math.PI, ...d].join('\t')); }
  fs.writeFileSync(outDir + '/math-base-development.tsv', rowsB.join('\n') + '\n');
  // the 32 uniform exploration parameters of hybrid-existence-policy/v1 on [-pi, pi)
  const rowsE = ['param\ttDeg\tvalid\tleaf\tdevX\tdevY'];
  for (let k = 0; k < 32; k++) {
    const t = -Math.PI + 2 * Math.PI * (k + 0.5) / 32, roots = rootsOnRay(t, 'R', 20000);
    if (roots.length !== 2) { rowsE.push([t, t * 180 / Math.PI, false, '', '', ''].join('\t')); continue; }
    roots.sort((a, b) => b.elev[1] - a.elev[1]);
    roots.forEach((w, j) => rowsE.push([t, t * 180 / Math.PI, true, j === 0 ? 'w_2(C_2)' : 'a_3(D_2)', ...develop(norm(sub(w.X, V)), t)].join('\t')));
  }
  fs.writeFileSync(outDir + '/math-exploration-samples.tsv', rowsE.join('\n') + '\n');
}

// ---- optional: compare the live direct-transport point TrG (TLIVEROW lines) with this reconstruction
if (process.argv[5]) {
  const lines = fs.readFileSync(process.argv[5], 'utf8').split(/\r?\n/).filter(l => l.startsWith('TLIVEROW'));
  let maxG = 0, maxTau = 0, maxAng = 0, n = 0, ms = [];
  for (const l of lines) {
    const f = l.replace('TLIVEROW ', '').split('\t');
    const th = +f[0], w = W(th, 'R'), tau = tOf(w.foot), g = develop(norm(sub(w.X, V)), tau);
    maxTau = Math.max(maxTau, Math.abs(tau - +f[4])); maxAng = Math.max(maxAng, Math.abs(Theta(tau) - +f[5]));
    maxG = Math.max(maxG, Math.hypot(g[0] - +f[6], g[1] - +f[7])); n++; ms.push(+f[1]);
  }
  ms.sort((a, b) => a - b);
  console.log('TLIVE-COMPARE ' + JSON.stringify({ rows: n, maxAbsDiff: { trTau: maxTau, trAng: maxAng, TrG: maxG }, moveMs: { min: ms[0], median: ms[Math.floor(n / 2)], max: ms[n - 1] } }));
}

// ---- R1 additions (this copy only; the original script of the first delivery is unchanged)
{
  const r1 = {};
  // (a) position of each family along the cone generatrix: mu = 0 at the vertex, mu = 1 at the base circle
  for (const fam of ['R', 'Q']) {
    const mus = [], zs = [];
    for (let i = 0; i < 7200; i++) { const w = W(i * 2 * Math.PI / 7200, fam); mus.push(w.mu); zs.push(w.X[2]); }
    r1['family' + fam] = { muMin: Math.min(...mus), muMax: Math.max(...mus), zMin: Math.min(...zs), zMax: Math.max(...zs), basePlaneZ: z0, vertexZ: V[2] };
  }
  // check of lambda_F = |PN| / |PF| at the saved N
  { const th = Math.atan2(Ns[1] - Cc[1], Ns[0] - Cc[0]); const n = N(th), rc = rayCircle(Ppt, n);
    const d = (a, b) => Math.hypot(a[0] - b[0], a[1] - b[1]);
    r1.lambdaCheck = { PN: d(Ppt, n), PQ: d(Ppt, rc[0]), PR: d(Ppt, rc[1]), lambdaQ: d(Ppt, n) / d(Ppt, rc[0]), lambdaR: d(Ppt, n) / d(Ppt, rc[1]), muQ: W(th, 'Q').mu, muR: W(th, 'R').mu }; }
  // (b) chart orientation versus selector germ at the saved driver position
  const tS2 = tOf(E1s), roots = rootsOnRay(tS2, 'R', 20000);
  const phi = th => { const e = W(th, 'R').elev; return Math.atan2(e[1] - H[1], e[0] - H[0]); };
  const q1 = dag.elems['q_1'].coords; // homogeneous line of the ray as stored by the kernel
  const lineDist = th => { const e = W(th, 'R').elev; return (+q1.x * e[0] + +q1.y * e[1] + +q1.z) / Math.hypot(+q1.x, +q1.y); };
  const hDet = th => { const e = W(th, 'R').elev, F1 = [E(tS2)[0], z0], d = sub(F1, H); return ((e[0] - H[0]) * d[1] - (e[1] - H[1]) * d[0]) / Math.hypot(d[0], d[1]); };
  const D1n = (f, x) => { const e = 1e-6; let a = f(x + e) - f(x - e); if (f === phi) { if (a > Math.PI) a -= 2 * Math.PI; if (a < -Math.PI) a += 2 * Math.PI; } return a / (2 * e); };
  r1.chartVersusGerm = roots.map(w => ({ thetaN: w.th, elev: w.elev, r: Math.hypot(w.elev[0] - H[0], w.elev[1] - H[1]), dphi_dtheta: D1n(phi, w.th), dh_dtheta_det: D1n(hDet, w.th), minus_r_dphi: -Math.hypot(w.elev[0] - H[0], w.elev[1] - H[1]) * D1n(phi, w.th), d_canonicalLineDistance_dtheta: D1n(lineDist, w.th) }));
  r1.note = 'C_2 (token text8, germ POSITIVE) is the root of higher elevation; D_2 (text9, NEGATIVE) the lower one';
  // (c) fold locations: zeros of dphi/dtheta and the driver values where psi(t) = phi(fold)
  const folds = [];
  let prev = D1n(phi, 0);
  for (let i = 1; i <= 7200; i++) { const th = i * 2 * Math.PI / 7200, cur = D1n(phi, th);
    if (prev * cur < 0) { let a = (i - 1) * 2 * Math.PI / 7200, b = th, fa = prev; for (let k = 0; k < 60; k++) { const m = 0.5 * (a + b), fm = D1n(phi, m); if (fa * fm <= 0) b = m; else { a = m; fa = fm; } }
      const th0 = 0.5 * (a + b), w = W(th0, 'R'); folds.push({ thetaN: th0, phi: phi(th0), footTdeg: tOf(w.foot) * 180 / Math.PI, footT: tOf(w.foot) }); }
    prev = cur; }
  r1.foldsOfPhi = folds;
  // (d) data for the R1 figure: generatrix length at each foot, to frame the Q family beyond the base
  const rowsQ = ['family\tthetaN\tmu\ttrueRadius\tgeneratrixLength\tdevX\tdevY\tdevConjX\tdevConjY'];
  for (const fam of ['R', 'Q']) for (let i = 0; i <= 720; i++) { const w = W(i * 2 * Math.PI / 720, fam), tf = tOf(w.foot), r = norm(sub(w.X, V)); rowsQ.push([fam, w.th, w.mu, r, norm(sub(foot3(w.foot), V)), ...develop(r, tf), ...develop(r, -tf)].join('\t')); }
  fs.writeFileSync(outDir + '/r1-families.tsv', rowsQ.join('\n') + '\n');
  fs.writeFileSync(outDir + '/r1-geometry.json', JSON.stringify(r1, null, 1));
  console.log('R1-GEOMETRY ' + JSON.stringify(r1, null, 1));
}
