/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;

/**
 * G9U0 public-command adapter for the approved one-parameter target families.
 *
 * <p>The adapter deliberately reuses the author-approved G8C1 adaptive local
 * proof. The ordinary line, segment, ray and circle adapters expose the same
 * normalized residual, membership and differential-contact contract as the
 * G8C1 extended targets, so they may use that proof without changing the
 * historical G8 solver fallback. The delegated candidate set still reports
 * global completeness as not established and never promotes a tangency or
 * residual-only hit to an admissible token. For a locally isolated transverse
 * candidate it records an oriented, typed root germ under exact component
	 * lineage. That germ is revision evidence and may participate in a
	 * current-snapshot deterministic selector only when unique in the constructive
	 * query; it is neither a continuation certificate nor durable identity by
	 * itself. The public token ledger separately requires compatible provider and
	 * target contracts. Isolating intervals and candidate order never enter durable
	 * identity.</p>
 *
 * <p>This productive adapter intentionally contains no expression-tree
 * algebra, analytic fixture or sample-derived completeness claim. Closed
 * analytic enumerators remain test-private until a typed productive generator
 * certificate or interval-safe proof authority is approved.</p>
 */
public final class PublicTargetIntersectionCapability2D
		implements LocusIntersectionCapability2D {
	private static final String CURRENT_TRANSVERSE_GERM_PREFIX =
			"g9u0-r4/current-transverse-root-germ/v1/";
	private final ExtendedTargetIntersectionCapability2D adaptiveProof =
			new ExtendedTargetIntersectionCapability2D();

	@Override
	public String getCapabilityId() {
		return "g9u0-public-query-local-one-parameter/v1";
	}

	@Override
	public boolean supports(IntersectionCapabilityContext2D context) {
		TargetFamily family = context.getTarget().getFamily();
		return family == TargetFamily.LINE
				|| family == TargetFamily.SEGMENT
				|| family == TargetFamily.RAY
				|| family == TargetFamily.CIRCLE
				|| family == TargetFamily.ELLIPSE
				|| family == TargetFamily.PARABOLA
				|| family == TargetFamily.HYPERBOLA
				|| family == TargetFamily.BOUNDED_FUNCTION_GRAPH
				|| family == TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT;
	}

	@Override
	public IntersectionCandidateSet2D isolate(
			IntersectionCapabilityContext2D context) {
		if (!supports(context)) {
			throw new IllegalArgumentException(
					"Target family is outside the G9U0 public capability");
		}
		IntersectionCandidateSet2D delegated = adaptiveProof.isolate(context);
		boolean finiteAuthority = delegated.getGeometryKind() == GeometryKind.FINITE
				&& delegated.getOverlapEvidence().isEmpty();
		ArrayList<IntersectionCandidate2D> publicCandidates = new ArrayList<>();
		for (IntersectionCandidate2D candidate : delegated.getCandidates()) {
			Optional<String> germ = finiteAuthority
					? transverseGerm(context, candidate) : Optional.empty();
			publicCandidates.add(withCurrentRootGerm(context, candidate, germ));
		}
		return new IntersectionCandidateSet2D(delegated.getCompleteness(),
				delegated.getCompletenessMethod(), delegated.getGeometryKind(),
				delegated.getSupportLevel(), delegated.getNumericGuarantee(),
				delegated.getCoveredComponentKeys(), publicCandidates,
				delegated.getOverlapEvidence(), delegated.getDiagnostics());
	}

	private static IntersectionCandidate2D withCurrentRootGerm(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate,
			Optional<String> transverseGerm) {
		LocusInterval2D component = exactComponent(context, candidate);
		String lineage = IntersectionTokenLineage2D.stableComponentLineage(
				candidate.getBranchKey(), component);
		Optional<String> currentGerm = transverseGerm.map(germ ->
				CURRENT_TRANSVERSE_GERM_PREFIX + framed(lineage) + framed(germ));
		if (!currentGerm.isPresent()) {
			return candidate;
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : candidate.getDiagnostics()) {
			if (diagnostic.getCode() != DiagnosticCode.CONTINUATION_AMBIGUOUS) {
				diagnostics.add(diagnostic);
			}
		}
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED,
				"Captured an oriented transverse current root germ as "
						+ "revision evidence; only a unique current semantic "
						+ "selector may bind it to durable identity"));
		return new IntersectionCandidate2D(candidate.getBranchKey(),
				candidate.getComponentKey(), candidate.getSemanticParameter(),
				candidate.getLiftedPeriodicParameter(),
				candidate.getIsolatingInterval(),
				candidate.getLocalIsolationStatus(), currentGerm,
				candidate.getContactClass(), candidate.getMultiplicityStatus(),
				candidate.getEstablishedMultiplicity(), candidate.getSolverMethod(),
				candidate.getNumericGuarantee(), candidate.getLineageEventKind(),
				candidate.getCandidateParentContinuationKeys(),
				diagnostics);
	}

	private static Optional<String> transverseGerm(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate) {
		if (candidate.getLocalIsolationStatus()
				!= LocalIsolationStatus.ESTABLISHED
				|| candidate.getContactClass()
						!= ContactClass.TRANSVERSE_ESTABLISHED) {
			return Optional.empty();
		}
		LocusEvaluation2D evaluation = context.evaluate(candidate.getBranchKey(),
				candidate.getSemanticParameter());
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return Optional.empty();
		}
		LocusInterval2D component = exactComponent(context, candidate);
		TargetContactEvidence2D contact = context.evaluateContact(
				evaluation.getPoint(), context.evaluateDifferential(
						candidate.getBranchKey(), candidate.getSemanticParameter(),
						component));
		if (!contact.isEstablished()
				|| Math.abs(contact.getNormalizedIndicator()) <= context.getQuery()
						.getPolicy().getTangencyTolerance().getThreshold()) {
			return Optional.empty();
		}
		return Optional.of(framed(contact.getIndicatorId())
				+ (contact.getNormalizedIndicator() > 0 ? "positive" : "negative"));
	}

	static boolean isCurrentPublicRootGerm(String key) {
		return parseCurrentPublicRootGerm(key).isPresent();
	}

	static boolean isCurrentPublicRootGermForComponent(String key,
			String componentLineage) {
		return parseCurrentPublicRootGerm(key)
				.filter(germ -> germ.componentLineage.equals(componentLineage))
				.isPresent();
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static Optional<CurrentRootGerm> parseCurrentPublicRootGerm(
			String key) {
		if (key == null || !key.startsWith(CURRENT_TRANSVERSE_GERM_PREFIX)) {
			return Optional.empty();
		}
		try {
			String encoded = key.substring(CURRENT_TRANSVERSE_GERM_PREFIX.length());
			Frame component = readFrame(encoded, 0);
			Frame germ = readFrame(encoded, component.nextOffset);
			if (germ.nextOffset != encoded.length()) {
				return Optional.empty();
			}
			Frame indicator = readFrame(germ.value, 0);
			String orientation = germ.value.substring(indicator.nextOffset);
			if (!("positive".equals(orientation)
					|| "negative".equals(orientation))) {
				return Optional.empty();
			}
			String canonical = CURRENT_TRANSVERSE_GERM_PREFIX
					+ framed(component.value)
					+ framed(framed(indicator.value) + orientation);
			return canonical.equals(key)
					? Optional.of(new CurrentRootGerm(component.value))
					: Optional.empty();
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private static Frame readFrame(String value, int offset) {
		int separator = value.indexOf(':', offset);
		if (separator <= offset) {
			throw new IllegalArgumentException("Malformed current root germ");
		}
		String lengthText = value.substring(offset, separator);
		int length = Integer.parseInt(lengthText);
		if (length <= 0 || !Integer.toString(length).equals(lengthText)) {
			throw new IllegalArgumentException("Noncanonical current root germ");
		}
		int start = separator + 1;
		int end = Math.addExact(start, length);
		if (end > value.length()) {
			throw new IllegalArgumentException("Truncated current root germ");
		}
		return new Frame(value.substring(start, end), end);
	}

	private static final class Frame {
		private final String value;
		private final int nextOffset;

		private Frame(String value, int nextOffset) {
			this.value = value;
			this.nextOffset = nextOffset;
		}
	}

	private static final class CurrentRootGerm {
		private final String componentLineage;

		private CurrentRootGerm(String componentLineage) {
			this.componentLineage = componentLineage;
		}
	}

	private static LocusInterval2D exactComponent(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate) {
		LocusBranch2D branch = context.getDefinition().getBranch(
				candidate.getBranchKey());
		if (branch != null) {
			List<LocusInterval2D> components = branch.getValidDomainComponents();
			for (int index = 0; index < components.size(); index++) {
				if (candidate.getComponentKey().equals(
						IntersectionCapabilityContext2D.componentKey(
								candidate.getBranchKey(), index))) {
					return components.get(index);
				}
			}
		}
		throw new IllegalStateException(
				"Adaptive candidate lost its exact semantic component");
	}
}
