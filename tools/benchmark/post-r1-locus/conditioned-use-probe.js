// External design probe for the conditioned-use proposal of Locus V2 (post-R1 block 3A).
// NOT GeoCeDG, not product code, not a prototype of the kernel implementation: it exercises
// the traversal protocol that the design describes on synthetic evaluators whose truth is
// known in closed form, to show which facts a finite traversal detects by contract and which
// it can only assume. Usage: node conditioned-use-probe.js [outFile]
//
// The synthetic evaluator returns, for a parameter t in [0, 1]:
//   { valid, x, y, selector, margin, failed }
// where `selector` stands for the identity of the selected root, `margin` for the distance to
// the nearest competing root (the evidence a kernel root isolation could testify), and
// `failed` for a computational failure that is not a geometric absence.
'use strict';
const fs = require('fs');

const POLICY = { name: 'probe-policy/v0', initial: 32, maxTurn: 0.35, maxSagitta: 1e-3,
  minStep: 2 ** -20, lipschitzFactor: 4 };

// Canonical dyadic traversal: every node is k / 2^d of the declared domain, so the evidence
// set depends on the policy and on the function only, never on the order of the requests.
function traverse(f, policy, requests) {
  const nodes = new Map();
  let evaluations = 0;
  const at = t => { if (!nodes.has(t)) { nodes.set(t, f(t)); evaluations++; } return nodes.get(t); };
  const cells = [];
  for (let i = 0; i < policy.initial; i++) cells.push([i / policy.initial, (i + 1) / policy.initial]);
  if (requests) cells.sort((a, b) => requests(a) - requests(b));
  const leaves = [];
  const refine = (a, b) => {
    const pa = at(a), pb = at(b), h = b - a;
    const mid = (a + b) / 2;
    const reason = why(pa, pb, at, a, b, policy);
    if (reason && h / 2 >= policy.minStep) { refine(a, mid); refine(mid, b); return; }
    leaves.push({ a, b, kind: classify(pa, pb, reason) });
  };
  for (const [a, b] of cells) refine(a, b);
  leaves.sort((p, q) => p.a - q.a);
  return { leaves, evaluations, nodes };
}

function why(pa, pb, at, a, b, policy) {
  // A failure is localized at its boundary only: the interior of a failed band is never
  // subdivided, because no amount of work there turns a failure into evidence.
  if (pa.failed && pb.failed) return null;
  if (pa.failed || pb.failed) return 'failure';
  if (pa.valid !== pb.valid) return 'validity';
  if (!pa.valid) return null;
  if (pa.selector !== pb.selector) return 'selector';
  const pm = at((a + b) / 2);
  if (pm.failed) return 'failure';
  if (!pm.valid) return 'validity';
  if (pm.selector !== pa.selector) return 'selector';
  const chord = Math.hypot(pb.x - pa.x, pb.y - pa.y);
  const off = Math.abs((pb.x - pa.x) * (pa.y - pm.y) - (pa.x - pm.x) * (pb.y - pa.y)) / (chord || 1);
  if (off > policy.maxSagitta) return 'sagitta';
  const turn = Math.abs(Math.atan2(pb.y - pm.y, pb.x - pm.x) - Math.atan2(pm.y - pa.y, pm.x - pa.x));
  if (Math.min(turn, 2 * Math.PI - turn) > policy.maxTurn && chord > policy.maxSagitta) return 'turn';
  return null;
}

function classify(pa, pb, reason) {
  if (pa.failed || pb.failed || reason === 'failure') return 'UNRESOLVED_FAILURE';
  if (!pa.valid && !pb.valid) return 'ABSENT_AT_NODES';
  if (pa.valid !== pb.valid) return 'TRANSITION_BAND';
  if (reason === 'selector') return 'SELECTOR_BAND';
  if (reason) return 'DISCONTINUITY_SUSPECTED';
  // Margin class: an estimated Lipschitz bound from the two observed margins is NOT a bound.
  const slope = Math.abs(pb.margin - pa.margin) / (pb.t - pa.t || 1);
  return Math.min(pa.margin, pb.margin) > POLICY.lipschitzFactor * slope * (pb.t - pa.t)
    ? 'SPAN_MARGIN_ESTIMATED' : 'SPAN_RESOLUTION_ONLY';
}

const withT = f => t => Object.assign({ t }, f(t));
const smooth = t => ({ valid: true, x: Math.cos(2 * Math.PI * t), y: Math.sin(2 * Math.PI * t),
  selector: 'a', margin: 1 });
const cases = [];
const add = (id, title, f, truth, expect) => cases.push({ id, title, f: withT(f), truth, expect });

add('C1', 'valid jump at an angular cut (atan2 branch)', t => {
  const phi = Math.atan2(Math.sin(2 * Math.PI * (t - 0.37)), Math.cos(2 * Math.PI * (t - 0.37)));
  return { valid: true, x: t, y: phi, selector: 'a', margin: 1 };
}, { events: [[0.87, 0.87]] }, 'the jump is isolated to the minimum step as a suspected discontinuity');

for (const width of [1e-2, 1e-4, 1e-7]) {
  add('C2/' + width, 'two validity events inside one initial cell (gap of width ' + width + ')', t =>
    (t > 0.4171 && t < 0.4171 + width) ? { valid: false } : smooth(t),
  { events: [[0.4171, 0.4171 + width]] },
  'detected only when a node falls inside the gap; otherwise the span is reported verified');
}

add('C3', 'two roots swap and swap back between nodes', t => {
  const swapped = t > 0.6113 && t < 0.6113 + 3e-5;
  const p = smooth(t);
  return swapped ? { valid: true, x: -p.x, y: -p.y, selector: 'b', margin: 1 }
    : Object.assign(p, { margin: 1 });
}, { events: [[0.6113, 0.6113 + 3e-5]] }, 'undetected unless a node falls inside; margins observed at nodes are large');

add('C4', 'margin vanishes between nodes while observed slopes are small', t => {
  const p = smooth(t);
  p.margin = 1 - Math.exp(-(((t - 0.2931) / 2e-4) ** 2));
  return p;
}, { events: [[0.2931, 0.2931]] }, 'the estimated-Lipschitz margin class accepts the span: an observed slope is not a bound');

add('C5', 'evaluation failure where the curve exists', t =>
  (t > 0.55 && t < 0.58) ? { failed: true } : smooth(t),
{ events: [[0.55, 0.58]] }, 'the band is UNRESOLVED_FAILURE, never an absence');

add('C6', 'narrow transition with a large metric contribution', t => {
  const s = (t - 0.5013) / 1e-5;
  return { valid: true, x: t, y: 5 * Math.exp(-s * s), selector: 'a', margin: 1 };
}, { events: [[0.5013, 0.5013]], length: 1 + 10 }, 'undetected: the spike of length 10 is missing from a chord total');

function overlaps(leaf, event) { return leaf.a <= event[1] && leaf.b >= event[0]; }

const lines = ['# conditioned-use design probe, policy ' + JSON.stringify(POLICY),
  'case\ttitle\tevaluations\tleaves\tflagged kinds\tevent detected\tchord length\texpected by design'];
for (const c of cases) {
  const run = traverse(c.f, POLICY);
  const flagged = run.leaves.filter(l => !l.kind.startsWith('SPAN'));
  const detected = c.truth.events.every(e => flagged.some(l => overlaps(l, e)));
  const kinds = [...new Set(run.leaves.map(l => l.kind))].join(',');
  let length = 0;
  const sorted = [...run.nodes.keys()].sort((a, b) => a - b).map(t => run.nodes.get(t));
  for (let i = 1; i < sorted.length; i++) {
    if (sorted[i].valid && sorted[i - 1].valid) length += Math.hypot(sorted[i].x - sorted[i - 1].x, sorted[i].y - sorted[i - 1].y);
  }
  lines.push([c.id, c.title, run.evaluations, run.leaves.length, kinds, detected,
    length.toFixed(6) + (c.truth.length ? ' (true ' + c.truth.length + ')' : ''), c.expect].join('\t'));
}

// C9: the evidence set is independent of the order of the requests.
const reference = traverse(cases[0].f, POLICY);
const reversed = traverse(cases[0].f, POLICY, cell => -cell[0]);
const windowFirst = traverse(cases[0].f, POLICY, cell => Math.abs(cell[0] - 0.8));
const key = run => run.leaves.map(l => l.a + ':' + l.b + ':' + l.kind).join('|');
lines.push(['C9', 'the same policy with three orders of requests', reference.evaluations,
  reference.leaves.length, '-', key(reference) === key(reversed) && key(reference) === key(windowFirst),
  '-', 'identical leaves and kinds: the dyadic mesh is canonical'].join('\t'));

const text = lines.join('\n') + '\n';
if (process.argv[2]) fs.writeFileSync(process.argv[2], text);
process.stdout.write(text);
