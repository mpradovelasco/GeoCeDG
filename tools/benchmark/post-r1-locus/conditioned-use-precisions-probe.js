// External design probe for the two focal precisions of revision 4 of the conditioned-use
// candidate contract (post-R1 block 3A): identity across configurations (contract 6.5) and
// resolution under a similarity (contract 8.7).
// NOT GeoCeDG, not product code, not a prototype of the kernel implementation. It runs the
// rules of revision 3 and of revision 4 side by side on synthetic evaluators of known truth,
// to show that each adverse case discriminates: the earlier rule fails it and the new rule
// passes it. Usage: node conditioned-use-precisions-probe.js [outFile]
'use strict';
const fs = require('fs');

const POLICY = { name: 'probe-policy/v0', initial: 32, maxTurn: 0.35, maxSagitta: 1e-3,
  minStep: 2 ** -20 };

// ---- traversal: the protocol of conditioned-use-probe.js, plus the contradiction set -------

function why(pa, pb, at, a, b, policy) {
  if (pa.valid !== pb.valid) return 'validity';
  if (!pa.valid) return null;
  const pm = at((a + b) / 2);
  if (!pm.valid) return 'validity';
  const chord = Math.hypot(pb.x - pa.x, pb.y - pa.y);
  const off = sagitta(pa, pm, pb);
  if (off > policy.maxSagitta) return 'sagitta';
  const turn = Math.abs(Math.atan2(pb.y - pm.y, pb.x - pm.x) - Math.atan2(pm.y - pa.y, pm.x - pa.x));
  if (Math.min(turn, 2 * Math.PI - turn) > policy.maxTurn && chord > policy.maxSagitta) return 'turn';
  return null;
}

function sagitta(pa, pm, pb) {
  const chord = Math.hypot(pb.x - pa.x, pb.y - pa.y);
  return Math.abs((pb.x - pa.x) * (pa.y - pm.y) - (pa.x - pm.x) * (pb.y - pa.y)) / (chord || 1);
}

function kindOf(pa, pb, reason) {
  if (!pa.valid && !pb.valid) return 'ABSENT_BAND';
  if (pa.valid !== pb.valid) return 'TRANSITION_BAND';
  if (reason === 'validity') return 'TRANSITION_BAND';
  if (reason) return 'DISCONTINUITY_BAND';
  return 'SPAN';
}

// A cell is split when the ordinary criteria require it, or when it is larger than the
// minimum step and contains a parameter of the contradiction set (contract 7.3). The rule is
// a predicate of the cell, so the partition does not depend on any order.
function refineInto(leaves, at, policy, contradictions, a, b) {
  const pa = at(a), pb = at(b), half = (b - a) / 2;
  let reason = why(pa, pb, at, a, b, policy);
  const hit = contradictions.filter(p => p >= a && p <= b);
  if ((reason || hit.length) && half >= policy.minStep) {
    refineInto(leaves, at, policy, contradictions, a, a + half);
    refineInto(leaves, at, policy, contradictions, a + half, b);
    return;
  }
  if (!reason && hit.some(p => at(p).valid !== pa.valid)) reason = 'validity';
  leaves.push({ a, b, kind: kindOf(pa, pb, reason) });
}

function traverse(f, policy, contradictions) {
  const nodes = new Map();
  const at = t => { if (!nodes.has(t)) nodes.set(t, f(t)); return nodes.get(t); };
  const leaves = [];
  for (let i = 0; i < policy.initial; i++) {
    refineInto(leaves, at, policy, contradictions || [], i / policy.initial, (i + 1) / policy.initial);
  }
  leaves.sort((p, q) => p.a - q.a);
  return { leaves, at };
}

function regions(leaves) {
  const out = [];
  for (const l of leaves) {
    if (l.kind !== 'SPAN') continue;
    const last = out[out.length - 1];
    if (last && last[1] === l.a) last[1] = l.b; else out.push([l.a, l.b]);
  }
  return out;
}

const key = leaves => leaves.map(l => l.a + ':' + l.b + ':' + l.kind).join('|');
const lines = [];
const failures = [];
const check = (name, ok) => { if (!ok) failures.push(name); return ok ? 'ok' : 'FAIL'; };

// =============================================================================================
// Precision 1. Identity across configurations (case C17).
// One branch with two regions A = [0, 0.4] and B = [0.6, 1] separated by a geometric absence.
// Target: the line y = c. A root of positive germ exists on A for c in (0, 0.4) and on B for
// c in (1.6, 2). Both roots have the same branch-frame selector text.
// =============================================================================================

const twoArcs = hole => t => {
  if (t > 0.4 && t < 0.6) return { valid: false };
  if (hole && t > hole[0] && t < hole[1]) return { valid: false };
  return { valid: true, x: t, y: t < 0.5 ? t : t + 1 };
};

function findRoots(run, c) {
  const out = [];
  for (const l of run.leaves) {
    if (l.kind !== 'SPAN') continue;
    let lo = l.a, hi = l.b;
    const h = t => run.at(t).y - c;
    const ha = h(lo), hb = h(hi);
    if (ha === 0 || hb === 0 || (ha < 0) === (hb < 0)) continue;
    for (let i = 0; i < 60; i++) {
      const mid = (lo + hi) / 2;
      if ((h(mid) < 0) === (ha < 0)) lo = mid; else hi = mid;
    }
    out.push({ t: (lo + hi) / 2, interval: [lo, hi], germ: hb > ha ? '+' : '-' });
  }
  return out;
}

const selectorOf = root => 'conditioned-use/branch-frame-root/v1|branch:main|germ:' + root.germ;

function unique(roots) {
  const count = new Map();
  for (const r of roots) count.set(selectorOf(r), (count.get(selectorOf(r)) || 0) + 1);
  return roots.filter(r => count.get(selectorOf(r)) === 1);
}

// Revision 3: a selector that is unique among the current roots resolves its ledger token.
function resolveByUniqueness(ledger, roots) {
  const held = [];
  for (const r of unique(roots)) {
    const s = selectorOf(r);
    if (!ledger.has(s)) {
      ledger.set(s, { token: 'T' + (ledger.size + 1), origin: r.t });
      held.push({ root: r, token: ledger.get(s).token, status: 'NEW_TOPOLOGICAL_SOLUTION' });
    } else {
      held.push({ root: r, token: ledger.get(s).token, status: 'DETERMINISTIC_SELECTION_ESTABLISHED' });
    }
  }
  return held;
}

// Revision 4: one binding per selector with a fixed origin; the token is resolved only when
// the origin and the isolating interval of the root lie in one conditioned region. Otherwise
// it is withheld: no transfer, no second allocation, a revision-local handle for the root.
function resolveWithGuard(ledger, roots, run) {
  const held = [];
  const regs = regions(run.leaves);
  for (const r of unique(roots)) {
    const s = selectorOf(r);
    if (!ledger.has(s)) {
      ledger.set(s, { token: 'T' + (ledger.size + 1), origin: r.t });
      held.push({ root: r, token: ledger.get(s).token, status: 'NEW_TOPOLOGICAL_SOLUTION' });
      continue;
    }
    const o = ledger.get(s).origin;
    const connected = regs.some(g => g[0] <= o && o <= g[1] && g[0] <= r.interval[0] && r.interval[1] <= g[1]);
    held.push(connected
      ? { root: r, token: ledger.get(s).token, status: 'DETERMINISTIC_SELECTION_ESTABLISHED' }
      : { root: r, token: null, status: 'NOT_ESTABLISHED (revision-local handle)' });
  }
  return held;
}

const regionName = t => (t < 0.5 ? 'A' : 'B');
const RULES = [['revision 3: selector uniqueness', resolveByUniqueness],
  ['revision 4: origin and reuse guard', resolveWithGuard]];

lines.push('# C17. Identity across configurations: disappearance and appearance in separate regions');
lines.push('rule\tstate\troot (region, parameter)\tselector\ttoken held by the root\tstatus\tpoint P bound to T1');
const run17 = traverse(twoArcs(null), POLICY, []);
lines.push('# regions of the source: ' + JSON.stringify(regions(run17.leaves)));
const STATES = [['S1 c=0.2', 0.2], ['S2 c=1.8 (one step)', 1.8], ['S1b c=1.0 (no root)', 1.0],
  ['S2b c=1.8 (after an empty state)', 1.8], ['S3 c=0.3 (back on A)', 0.3]];
const outcome = {};
for (const [ruleName, rule] of RULES) {
  const ledger = new Map();
  outcome[ruleName] = [];
  for (const [stateName, c] of STATES) {
    const roots = findRoots(run17, c);
    const held = rule(ledger, roots, run17);
    const owner = held.find(h => h.token === 'T1');
    const p = owner ? '(' + owner.root.t.toFixed(4) + ', ' + c + ') on ' + regionName(owner.root.t) : 'undefined';
    outcome[ruleName].push(owner ? regionName(owner.root.t) : '-');
    const r = roots[0];
    lines.push([ruleName, stateName, r ? regionName(r.t) + ', ' + r.t.toFixed(4) : 'none',
      r ? selectorOf(r) : '-', held[0] ? (held[0].token || 'none') : '-', held[0] ? held[0].status : '-', p].join('\t'));
  }
  lines.push('# ' + ruleName + ': ledger entries = ' + ledger.size + ', origin of T1 = ' + [...ledger.values()][0].origin.toFixed(4));
}
const sameSelector = selectorOf(findRoots(run17, 0.2)[0]) === selectorOf(findRoots(run17, 1.8)[0]);
lines.push('# the two roots have the same selector text: ' + check('C17 same selector', sameSelector));
lines.push('# revision 3 gives T1 to the root on B (the defect): '
  + check('C17 rev3 reuses', outcome[RULES[0][0]].join('') === 'AB-BA'));
lines.push('# revision 4 never gives T1 to the root on B and returns it on A: '
  + check('C17 rev4 withholds', outcome[RULES[1][0]].join('') === 'A---A'));

// Withhold, never choose: other policy depths, and a contradiction confirmed between the
// origin and the root, can only withhold T1; no root ever holds another token of the selector.
lines.push('# C17b. The evidence withholds a token and never chooses one');
lines.push('content\troot at c=0.3\ttoken\tledger entries');
const hole = [0.2617, 0.2617 + 1e-6];
const variants = [['initial depth 32', twoArcs(null), POLICY, []],
  ['initial depth 64', twoArcs(null), Object.assign({}, POLICY, { initial: 64 }), []],
  ['initial depth 128', twoArcs(null), Object.assign({}, POLICY, { initial: 128 }), []],
  ['hole of 1e-6 at 0.2617, not detected', twoArcs(hole), POLICY, []],
  ['the same hole, contradiction confirmed at 0.2617005', twoArcs(hole), POLICY, [0.2617005]]];
const tokensSeen = new Set();
const withheldByBand = [];
for (const [name, f, policy, contradictions] of variants) {
  const first = traverse(f, policy, []);
  const ledger = new Map();
  resolveWithGuard(ledger, findRoots(first, 0.2), first);
  const now = traverse(f, policy, contradictions);
  const held = resolveWithGuard(ledger, findRoots(now, 0.3), now);
  tokensSeen.add(held[0].token || 'none');
  withheldByBand.push(held[0].token);
  lines.push([name, held[0].root.t.toFixed(4), held[0].token || 'none', ledger.size].join('\t'));
}
lines.push('# tokens ever held by the root: ' + [...tokensSeen].join(', ') + ' -> '
  + check('C17b never another token', [...tokensSeen].every(t => t === 'T1' || t === 'none')));
lines.push('# a band confirmed between origin and root withholds: '
  + check('C17b band withholds', withheldByBand[3] === 'T1' && withheldByBand[4] === null));
const orderA = key(traverse(twoArcs(hole), POLICY, [0.2617005, 0.7311]).leaves);
const orderB = key(traverse(twoArcs(hole), POLICY, [0.7311, 0.2617005, 0.2617005]).leaves);
lines.push('# the content does not depend on the order or multiplicity of the contradictions: '
  + check('C17b order', orderA === orderB));

// Histories: a document built directly in S2 records another origin. It can differ from the
// incremental one in when its token is withheld, never in which root holds which token.
const direct = new Map();
const heldDirect = resolveWithGuard(direct, findRoots(run17, 1.8), run17);
const backOnA = resolveWithGuard(direct, findRoots(run17, 0.2), run17);
lines.push('# C17c. direct construction in S2: token ' + heldDirect[0].token + ' with origin '
  + [...direct.values()][0].origin.toFixed(4) + '; in S1 that token is '
  + (backOnA[0].token ? 'held' : 'withheld') + ' -> '
  + check('C17c direct history', heldDirect[0].token === 'T1' && backOnA[0].token === null));

// =============================================================================================
// Precision 2. Resolution under a similarity (case C18).
// Source: the unit circle. Image: the dilation of absolute ratio rho about the origin.
// =============================================================================================

const circle = t => ({ valid: true, x: Math.cos(2 * Math.PI * t), y: Math.sin(2 * Math.PI * t) });
const scaled = (f, rho) => t => { const p = f(t); return p.valid ? { valid: true, x: rho * p.x, y: rho * p.y } : p; };

// Revision 3: the cells of the source are copied, whatever the ratio.
function inheritByCopy(source) { return source.leaves.map(l => Object.assign({}, l)); }

// Revision 4: bands are inherited as bands; for rho <= 1 every span is inherited; for
// rho > 1 every span is checked again in the world of the image and, where a check fails, the
// cell is classified anew by the ordinary rule on the image function.
function inheritByRule(source, f, rho, policy) {
  if (rho <= 1) return inheritByCopy(source);
  const image = scaled(f, rho);
  const nodes = new Map();
  const at = t => { if (!nodes.has(t)) nodes.set(t, image(t)); return nodes.get(t); };
  const out = [];
  for (const l of source.leaves) {
    if (l.kind !== 'SPAN') { out.push(Object.assign({}, l)); continue; }
    if (!why(at(l.a), at(l.b), at, l.a, l.b, policy)) { out.push(Object.assign({}, l)); continue; }
    refineInto(out, at, policy, [], l.a, l.b);
  }
  return out.sort((p, q) => p.a - q.a);
}

function measure(leaves, f, rho, policy) {
  const image = scaled(f, rho);
  let worst = 0, above = 0, spans = 0;
  for (const l of leaves) {
    if (l.kind !== 'SPAN') continue;
    spans++;
    const s = sagitta(image(l.a), image((l.a + l.b) / 2), image(l.b));
    worst = Math.max(worst, s);
    if (s > policy.maxSagitta) above++;
  }
  return { cells: leaves.length, spans, bands: leaves.length - spans, worst, above };
}

lines.push('# C18. Resolution under a similarity: unit circle, chord-deviation limit ' + POLICY.maxSagitta);
lines.push('rule\tratio\tcells\tspans\tbands\tlargest chord deviation of a span in the image\tspans above the limit');
const source18 = traverse(circle, POLICY, []);
const result18 = {};
for (const rho of [0, 0.5, 1, 3, 4]) {
  for (const [name, leaves] of [['revision 3: copy', inheritByCopy(source18)],
    ['revision 4: rule', inheritByRule(source18, circle, rho, POLICY)]]) {
    const m = measure(leaves, circle, rho, POLICY);
    result18[name + rho] = m;
    lines.push([name, rho, m.cells, m.spans, m.bands, m.worst.toExponential(3), m.above].join('\t'));
  }
}
lines.push('# rho <= 1 and rho = 3 inherit the 128 cells unchanged: '
  + check('C18 inherit', [0, 0.5, 1, 3].every(r => key(inheritByRule(source18, circle, r, POLICY)) === key(source18.leaves))));
lines.push('# rho = 4, revision 3 keeps spans above the limit (the defect): '
  + check('C18 rev3 silent', result18['revision 3: copy4'].above === 128));
lines.push('# rho = 4, revision 4 publishes no span above the limit: '
  + check('C18 rev4 reclassifies', result18['revision 4: rule4'].above === 0 && result18['revision 4: rule4'].cells === 256));
lines.push('# rho = 4 inherited-and-refined equals a traversal of the image itself: '
  + check('C18 equals own traversal', key(inheritByRule(source18, circle, 4, POLICY)) === key(traverse(scaled(circle, 4), POLICY, []).leaves)));

// Minimum step: where the checks still fail there, the cell is a band of the image.
const coarse = Object.assign({}, POLICY, { minStep: 2 ** -8 });
const sourceCoarse = traverse(circle, coarse, []);
const atStep = measure(inheritByRule(sourceCoarse, circle, 100, coarse), circle, 100, coarse);
const atStepCopy = measure(inheritByCopy(sourceCoarse), circle, 100, coarse);
lines.push(['revision 3: copy, minimum step 2^-8', 100, atStepCopy.cells, atStepCopy.spans, atStepCopy.bands,
  atStepCopy.worst.toExponential(3), atStepCopy.above].join('\t'));
lines.push(['revision 4: rule, minimum step 2^-8', 100, atStep.cells, atStep.spans, atStep.bands,
  atStep.worst.toExponential(3), atStep.above].join('\t'));
lines.push('# rho = 100 at the minimum step: bands, and no span above the limit: '
  + check('C18 minimum step', atStep.above === 0 && atStep.spans === 0 && atStep.bands === 256));

// A band never becomes a span because the image is small: a jump of 0.5 in the source is
// 5e-5 at rho = 1e-4, below the limit; a traversal of the image alone would call it a span.
const jump = t => ({ valid: true, x: t, y: t < 0.37 ? 0 : 0.5 });
const sourceJump = traverse(jump, POLICY, []);
const inheritedJump = inheritByRule(sourceJump, jump, 1e-4, POLICY);
const ownJump = traverse(scaled(jump, 1e-4), POLICY, []).leaves;
const bandsOf = leaves => leaves.filter(l => l.kind !== 'SPAN').length;
lines.push('# C18b. jump of 0.5 at rho = 1e-4: bands in the source ' + bandsOf(sourceJump.leaves)
  + ', inherited by the rule ' + bandsOf(inheritedJump) + ', found by a traversal of the image alone ' + bandsOf(ownJump)
  + ' -> ' + check('C18b band kept', bandsOf(inheritedJump) === bandsOf(sourceJump.leaves) && bandsOf(sourceJump.leaves) > 0 && bandsOf(ownJump) === 0));

// Each image decides against its immediate source, and inheritance never coarsens.
const up = { leaves: inheritByRule(source18, circle, 4, POLICY) };
const upDown = inheritByRule(up, scaled(circle, 4), 0.25, POLICY);
const down = { leaves: inheritByRule(source18, circle, 0.25, POLICY) };
const downUp = inheritByRule(down, scaled(circle, 0.25), 4, POLICY);
lines.push('# C18c. Dilate(Dilate(L, 4), 1/4): ' + upDown.length + ' cells (never coarsened); Dilate(Dilate(L, 1/4), 4): '
  + downUp.length + ' cells -> ' + check('C18c composition', upDown.length === 256 && downUp.length === 128));

lines.push(failures.length ? '# RESULT: FAIL ' + failures.join('; ') : '# RESULT: every discriminating check holds');
const text = lines.join('\n') + '\n';
if (process.argv[2]) fs.writeFileSync(process.argv[2], text);
process.stdout.write(text);
process.exitCode = failures.length ? 1 : 0;
