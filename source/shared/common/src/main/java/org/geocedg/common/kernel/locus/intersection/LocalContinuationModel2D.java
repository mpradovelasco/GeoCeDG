/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D.ContinuationJet;
import org.geocedg.common.kernel.locus.intersection.LocalContinuationEvidence2D.Work;

/** One captured root program and its typed, invertible similarity lineage. */
final class LocalContinuationModel2D {

	private final ConstructionBoxModel2D root;
	private final List<Map> maps;
	private final Map image;
	private final String lineage;

	private LocalContinuationModel2D(ConstructionBoxModel2D root, List<Map> maps,
			String lineage, Work work) {
		this.root = root;
		this.maps = List.copyOf(maps);
		this.lineage = lineage;
		Map composed = null;
		for (Map map : maps) {
			if (composed == null) { composed = map; }
			else { work.charge(); composed = composed.after(map); }
		}
		image = composed;
	}

	static LocalContinuationModel2D capture(LocusDefinition2D definition, String branch,
			int maximumDepth, Work work) {
		try {
			LocusDefinition2D current = definition;
			List<Map> maps = new ArrayList<>();
			StringBuilder signature = new StringBuilder("local-captured-similarity/v1");
			while (current.getEvaluatorCapability() instanceof LocusSimilarityEvaluator2D image) {
				if (maps.size() >= maximumDepth) { return null; }
				work.charge();
				work.correspondence();
				LocusDefinition2D source = image.getCapturedSourceDefinition();
				var imageBranch = current.getBranch(branch);
				var sourceBranch = source.getBranch(branch);
				if (imageBranch == null || sourceBranch == null
						|| current.getProvider() != source.getProvider()
						|| imageBranch.getOrientation() != sourceBranch.getOrientation()
						|| !imageBranch.getLineage().equals(sourceBranch.getLineage())
						|| !imageBranch.getExistenceStructure().getContinuousValidComponents().equals(
								sourceBranch.getExistenceStructure().getContinuousValidComponents())) {
					return null;
				}
				maps.add(new Map(image.getCapturedTransform()));
				signature.append('|').append(current.getLocusIdentity()).append('@')
						.append(current.getSemanticRevision()).append(':')
						.append(image.getCapturedTransform().getSemanticSignature());
				current = source;
			}
			work.charge();
			work.correspondence();
			if (!(current.getEvaluatorCapability() instanceof ReconstructibleLocusEvaluator2D evaluator)
					|| !evaluator.isReconstructionFaithful()) { return null; }
			CertifiedConstructionProgram2D program = evaluator.captureCertifiedProgramV3().orElse(null);
			if (program == null) { return null; }
			signature.append("|root=").append(current.getLocusIdentity()).append('@')
					.append(current.getSemanticRevision()).append('|').append(branch);
			return new LocalContinuationModel2D(new ConstructionBoxModel2D(program), maps,
					signature.toString(), work);
		} catch (ArithmeticException | IllegalArgumentException failure) {
			return null;
		}
	}

	CertifiedConstructionProgram2D program() { return root.getProgram(); }
	boolean isDirect() { return maps.isEmpty(); }

	ContinuationJet jet(SplineOutwardInterval2D box, boolean periodic, boolean second, Work work) {
		work.charge();
		work.intervalEvaluation();
		ContinuationJet captured = root.continuationJet(box, periodic, second);
		if (captured.outcome.kind != ConstructionBoxOutcome2D.Kind.VALID) { return captured; }
		SplineOutwardInterval2D[] value = captured.value;
		SplineOutwardInterval2D[] first = captured.first;
		SplineOutwardInterval2D[] secondJet = captured.second;
		if (image != null) {
			work.charge();
			work.similarityApplication();
			value = image.apply(value, false);
			first = first == null ? null : image.apply(first, true);
			secondJet = secondJet == null ? null : image.apply(secondJet, true);
		}
		ConstructionBoxOutcome2D outcome = ConstructionBoxOutcome2D.valid(value, first,
				captured.outcome.selection + '|' + lineage, captured.outcome.evaluations);
		return new ContinuationJet(outcome, value, first, secondJet);
	}

	/** Outward coefficients of the existing typed geometric similarity, not a fitted map. */
	private static final class Map {
		private final SplineOutwardInterval2D a, b, c, d, tx, ty;

		private Map(SplineOutwardInterval2D a, SplineOutwardInterval2D b,
				SplineOutwardInterval2D c, SplineOutwardInterval2D d,
				SplineOutwardInterval2D tx, SplineOutwardInterval2D ty) {
			if (a.multiply(d).subtract(b.multiply(c)).containsZero()) {
				throw new ArithmeticException("Composite similarity invertibility is not established");
			}
			this.a = a; this.b = b; this.c = c; this.d = d; this.tx = tx; this.ty = ty;
		}

		/** Exact affine composition enclosed outward, retaining the original map list. */
		private Map after(Map inner) {
			return new Map(a.multiply(inner.a).add(b.multiply(inner.c)),
					a.multiply(inner.b).add(b.multiply(inner.d)),
					c.multiply(inner.a).add(d.multiply(inner.c)),
					c.multiply(inner.b).add(d.multiply(inner.d)),
					a.multiply(inner.tx).add(b.multiply(inner.ty)).add(tx),
					c.multiply(inner.tx).add(d.multiply(inner.ty)).add(ty));
		}

		private Map(LocusSimilarityTransform2D transform) {
			var v = transform.getCapturedValues();
			SplineOutwardInterval2D one = p(1), zero = p(0), two = p(2);
			SplineOutwardInterval2D aa, bb, cc, dd, xx, yy;
			switch (transform.getKind()) {
			case TRANSLATION:
				aa = dd = one; bb = cc = zero; xx = p(v.get(0)); yy = p(v.get(1)); break;
			case ROTATION:
				aa = dd = ConstructionIntervalModel2D.cosine(p(v.get(0)));
				cc = ConstructionIntervalModel2D.sine(p(v.get(0))); bb = cc.negate();
				xx = p(v.get(1)).subtract(aa.multiply(p(v.get(1)))).subtract(bb.multiply(p(v.get(2))));
				yy = p(v.get(2)).subtract(cc.multiply(p(v.get(1)))).subtract(dd.multiply(p(v.get(2))));
				break;
			case POINT_REFLECTION:
				aa = dd = p(-1); bb = cc = zero;
				xx = two.multiply(p(v.get(0))); yy = two.multiply(p(v.get(1))); break;
			case LINE_REFLECTION:
				// Same normalization enclosure as the captured transform's Range.normalized.
				// Kind supplies the exact unit-normal correspondence; rounded n.n is not 1.
				SplineOutwardInterval2D nx = normalized(v.get(0)), ny = normalized(v.get(1));
				SplineOutwardInterval2D offset = normalized(v.get(2));
				aa = one.subtract(two.multiply(nx).multiply(nx));
				dd = one.subtract(two.multiply(ny).multiply(ny));
				bb = cc = two.multiply(nx).multiply(ny).negate();
				xx = two.multiply(nx).multiply(offset).negate();
				yy = two.multiply(ny).multiply(offset).negate(); break;
			case DILATION:
				if (transform.isCollapsed()) { throw new ArithmeticException("Collapsed similarity"); }
				aa = dd = p(v.get(0)); bb = cc = zero;
				xx = one.subtract(aa).multiply(p(v.get(1)));
				yy = one.subtract(dd).multiply(p(v.get(2))); break;
			default: throw new ArithmeticException("No typed similarity correspondence");
			}
			if (aa.multiply(dd).subtract(bb.multiply(cc)).containsZero()) {
				throw new ArithmeticException("Similarity invertibility is not enclosed away from zero");
			}
			a = aa; b = bb; c = cc; d = dd; tx = xx; ty = yy;
		}

		private SplineOutwardInterval2D[] apply(SplineOutwardInterval2D[] value, boolean derivative) {
			SplineOutwardInterval2D x = a.multiply(value[0]).add(b.multiply(value[1]));
			SplineOutwardInterval2D y = c.multiply(value[0]).add(d.multiply(value[1]));
			return new SplineOutwardInterval2D[] {derivative ? x : x.add(tx), derivative ? y : y.add(ty)};
		}
	}

	private static SplineOutwardInterval2D normalized(double value) {
		double radius = Math.nextUp(Math.abs(value) * 0x1.0p-50) + Double.MIN_VALUE;
		return new SplineOutwardInterval2D(Math.nextDown(value - radius), Math.nextUp(value + radius));
	}
	private static SplineOutwardInterval2D p(double value) { return SplineOutwardInterval2D.point(value); }
}
