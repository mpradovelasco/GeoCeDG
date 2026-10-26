/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;

/**
 * Existence grades, contact evidence and the conflict rule of {@code cu3-certified-admission/v1},
 * and of its version 2 under the constructive authority, on the candidates of one query
 * ({@code locus-v2-intersections.md} CU3.2 and CU3.4; Stage B design section 9; PRE-G9B-CU3
 * pieces I-2 and I-3). The candidates the solver marks as
 * non-transverse, as not locally isolated (the local minima of its search) or as closer to each
 * other than their own brackets trigger a root-count certificate on a box of their certified
 * continuous component; the trigger reads only parameters and solver states, never the scale of
 * the drawing. A candidate is withdrawn, merged into one preimage or reclassified only when such
 * a certificate really contradicts its existence, its count or its classification; a certified
 * critical point alone, a class-3 contact or a certificate that does not hold leaves every
 * candidate unchanged. Every transverse isolated candidate also receives the grade
 * {@code EXISTENCE_CERTIFIED} when a simple root is certified around it. In the constructive
 * configuration a candidate at a certified fold of {@code CU2} triggers too, and the form one
 * double root at an exact point, from the exact decisions of that configuration, reclassifies a
 * root published as transverse at a fold and withdraws the rounding noise around a contact
 * (PRE-G9B-CU3 increment E3). The analysis creates no point and no token; the solver verifies
 * every surviving candidate as before.
 */
final class CertifiedContactAnalysis2D {

	/** The adjusted candidates of one query. */
	static final class Outcome {
		private final List<IntersectionCandidate2D> candidates;
		private final List<IntersectionDiagnostic2D> diagnostics;
		private final boolean completenessContradicted;

		private Outcome(List<IntersectionCandidate2D> candidates,
				List<IntersectionDiagnostic2D> diagnostics, boolean completenessContradicted) {
			this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
			this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
			this.completenessContradicted = completenessContradicted;
		}

		/** @return the candidates that remain, with their evidence, in parameter order */
		List<IntersectionCandidate2D> getCandidates() {
			return candidates;
		}

		/** @return the diagnostics of the withdrawals, merges and contact evidence */
		List<IntersectionDiagnostic2D> getDiagnostics() {
			return diagnostics;
		}

		/**
		 * @return whether a certificate leaves the count of a box undetermined where the
		 *         capability declared the set complete: a contradiction of the set only
		 */
		boolean isCompletenessContradicted() {
			return completenessContradicted;
		}
	}

	/** The entries of the candidates of one analysis, in their order, and its outcome. */
	private static final class Analysis {
		private final List<Entry> entries;
		private final Outcome outcome;

		Analysis(List<Entry> entries, Outcome outcome) {
			this.entries = entries;
			this.outcome = outcome;
		}
	}

	private CertifiedContactAnalysis2D() {
	}

	/**
	 * @param context the query
	 * @param source the residual of the decision configuration
	 * @param candidates the candidates of the capability
	 * @param complete whether the capability declared the candidate set complete
	 * @return the adjusted candidates
	 */
	static Outcome apply(IntersectionCapabilityContext2D context, ResidualJetSource2D source,
			List<IntersectionCandidate2D> candidates, boolean complete) {
		return analyse(context, source, candidates, complete).outcome;
	}

	/** @return the entries of the candidates, in their order, and the outcome */
	private static Analysis analyse(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, List<IntersectionCandidate2D> candidates,
			boolean complete) {
		List<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		Map<String, List<Entry>> byComponent = new LinkedHashMap<>();
		List<Entry> entries = new ArrayList<>();
		for (IntersectionCandidate2D candidate : candidates) {
			Entry entry = new Entry(candidate);
			entries.add(entry);
			byComponent.computeIfAbsent(candidate.getBranchKey().length() + ":"
					+ candidate.getBranchKey() + candidate.getComponentKey(),
					key -> new ArrayList<>()).add(entry);
		}
		boolean contradicted = false;
		for (List<Entry> group : byComponent.values()) {
			Window window = Window.of(context, group.get(0).candidate);
			if (window == null) {
				continue;
			}
			group.sort((first, second) -> Double.compare(first.candidate.getSemanticParameter(),
					second.candidate.getSemanticParameter()));
			for (Entry entry : new ArrayList<>(group)) {
				if (entry.state != State.OPEN || !triggered(context, source, entry, group)) {
					continue;
				}
				RootCountCertificate2D certificate = certificate(source, window, entry);
				if (certificate == null) {
					continue;
				}
				ContactEvidenceClass contact = certificate.getContact() != null
						? certificate.getContact() : certificate.isCertified() ? null
								: ContactEvidenceClass.CONTACT_NOT_ESTABLISHED;
				if (entry.candidate.getContactClass() != ContactClass.TRANSVERSE_ESTABLISHED) {
					entry.contact = contact;
				}
				entry.certificate = certificate;
				if (certificate.isCertified()) {
					resolve(context, source, window, certificate, group, entries, diagnostics);
				} else {
					entry.state = State.ANALYSED;
					if (certificate.getContact() != null) {
						diagnostics.add(new IntersectionDiagnostic2D(
								DiagnosticCode.CONTACT_EVIDENCE_CLASSIFIED,
								"Candidate at " + entry.candidate.getSemanticParameter() + ": "
										+ certificate.getContact() + "; "
										+ certificate.describe()));
					}
					contradicted |= complete && certificate.getContact()
							== ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE;
				}
			}
			for (Entry entry : group) {
				if (entry.state == State.OPEN || entry.state == State.ANALYSED) {
					grade(source, window, entry);
				}
			}
		}
		List<IntersectionCandidate2D> result = new ArrayList<>();
		for (Entry entry : entries) {
			if (entry.state != State.WITHDRAWN) {
				result.add(entry.publish(context, source));
			}
		}
		result.sort((first, second) -> {
			int byBranch = first.getBranchKey().compareTo(second.getBranchKey());
			if (byBranch != 0) {
				return byBranch;
			}
			int byComponentKey = first.getComponentKey().compareTo(second.getComponentKey());
			return byComponentKey != 0 ? byComponentKey : Double.compare(
					first.getSemanticParameter(), second.getSemanticParameter());
		});
		if (contradicted) {
			diagnostics.add(new IntersectionDiagnostic2D(DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
					"A certified critical point leaves the count of a box undetermined: the"
							+ " completeness of the set is not established"));
		}
		return new Analysis(entries, new Outcome(result, diagnostics, contradicted));
	}

	/**
	 * The re-qualification of every published solution of one result under the constructive
	 * authority, as the adoption event reports it before anything is applied ({@code
	 * locus-v2-constructive-authority.md} section 7.4), with the conflict rule of the
	 * constructive configuration applied to all of them together: the rule may withdraw, merge
	 * or reclassify a solution there (PRE-G9B-CU3 increment E3), and such a solution is no longer
	 * admitted.
	 *
	 * @param context a query of the published result
	 * @param source the residual in the constructive configuration
	 * @param solutions the published solutions
	 * @return per solution, in the same order, its admission under the constructive authority,
	 *         or {@code null} when the conflict rule contradicts it there
	 */
	static List<ConstructiveAdmission> requalify(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, List<LocusIntersectionSolution2D> solutions) {
		List<IntersectionCandidate2D> candidates = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : solutions) {
			candidates.add(candidate(solution));
		}
		Analysis analysis = analyse(context, source, candidates, false);
		List<ConstructiveAdmission> admissions = new ArrayList<>();
		for (int index = 0; index < candidates.size(); index++) {
			Entry entry = analysis.entries.get(index);
			admissions.add(entry.state == State.WITHDRAWN ? null
					: entry.admission(context, source));
		}
		return admissions;
	}

	/** @return the candidate that a published solution would be, without its identity */
	private static IntersectionCandidate2D candidate(LocusIntersectionSolution2D solution) {
		IntersectionRootRevisionEvidence2D evidence = solution.getRevisionEvidence();
		return new IntersectionCandidate2D(evidence.getBranchSnapshotKey(),
				evidence.getResolvedValidComponentKey(), evidence.getSemanticParameter(),
				evidence.getLiftedPeriodicParameter(), evidence.getIsolatingInterval(),
				evidence.getLocalIsolationStatus(), Optional.empty(),
				solution.getClassification().getContactClass(),
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				evidence.getSolverMethod(), evidence.getNumericGuarantee(),
				LineageEventKind.APPEARED, Collections.emptyList(), Collections.emptyList());
	}

	/**
	 * The trigger of Stage B section 9.3: a non-transverse or not locally isolated candidate,
	 * or one closer to another candidate of its component than the bracket of either; in the
	 * constructive configuration also a candidate whose isolating interval meets a certified
	 * fold of {@code CU2}, where a root is never simple (PRE-G9B-CU3 increment E3, the seam of the
	 * author's model). The trigger reads parameters, solver states and the certified folds only.
	 */
	private static boolean triggered(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, Entry entry, List<Entry> group) {
		IntersectionCandidate2D candidate = entry.candidate;
		if (candidate.getContactClass() != ContactClass.TRANSVERSE_ESTABLISHED
				|| candidate.getLocalIsolationStatus() != LocalIsolationStatus.ESTABLISHED
				|| source.isConstructive() && entry.atCertifiedFold(context)) {
			return true;
		}
		for (Entry other : group) {
			if (other == entry) {
				continue;
			}
			double distance = Math.abs(other.candidate.getSemanticParameter()
					- candidate.getSemanticParameter());
			if (distance <= Math.max(width(candidate), width(other.candidate))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the first certificate on the sampling cell of the candidate and its successive
	 *         quarters that certifies a count or a critical point, the last one otherwise, or
	 *         {@code null} when no box of the component contains the candidate
	 */
	private static RootCountCertificate2D certificate(ResidualJetSource2D source,
			Window window, Entry entry) {
		IntersectionParameterInterval2D isolating = entry.candidate.getIsolatingInterval();
		double center = entry.candidate.getSemanticParameter();
		double half = window.span / CertifiedAdmissionPolicy2D.CELL_DIVISOR;
		RootCountCertificate2D last = null;
		for (int reduction = 0; reduction <= CertifiedAdmissionPolicy2D.BOX_REDUCTIONS;
				reduction++, half /= 4) {
			double[] box = window.box(center, half);
			if (box == null || box[0] > isolating.getLower()
					|| box[1] < isolating.getUpper()) {
				continue;
			}
			last = RootCountCertificate2D.certify(source, box[0], box[1]);
			if (last.isCertified() || last.getCritical() != null) {
				return last;
			}
		}
		return last;
	}

	/** Applies the contradictions of one count certificate to the candidates of its box. */
	private static void resolve(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, Window window, RootCountCertificate2D certificate,
			List<Entry> group, List<Entry> entries, List<IntersectionDiagnostic2D> diagnostics) {
		double lower = certificate.getLower();
		double upper = certificate.getUpper();
		List<Entry> members = new ArrayList<>();
		for (Entry entry : group) {
			if (entry.state != State.WITHDRAWN && window.inside(entry, lower, upper)) {
				members.add(entry);
			}
		}
		String evidence = certificate.describe();
		if (certificate.getForm() == RootCountCertificate2D.Form.NONE) {
			for (Entry member : members) {
				member.withdraw(diagnostics, "no root in the box: " + evidence);
			}
			return;
		}
		if (certificate.getForm() == RootCountCertificate2D.Form.ONE_DOUBLE) {
			resolveDouble(context, source, window, certificate, members, group, entries,
					diagnostics);
			return;
		}
		List<double[]> roots = certificate.getRoots();
		List<List<Entry>> located = new ArrayList<>();
		for (int index = 0; index < roots.size(); index++) {
			located.add(new ArrayList<>());
		}
		for (Entry member : members) {
			List<Integer> hits = new ArrayList<>();
			for (int index = 0; index < roots.size(); index++) {
				if (window.overlaps(member, roots.get(index), lower, upper)) {
					hits.add(index);
				}
			}
			if (hits.isEmpty()) {
				member.withdraw(diagnostics,
						"the isolating interval is disjoint from every located root: " + evidence);
			} else if (hits.size() == 1) {
				located.get(hits.get(0)).add(member);
			} else {
				member.state = State.ANALYSED;
			}
		}
		for (int index = 0; index < roots.size(); index++) {
			List<Entry> preimage = located.get(index);
			double[] root = roots.get(index);
			Entry kept = representative(preimage, root, window, lower, upper);
			for (Entry member : preimage) {
				if (member != kept) {
					member.withdraw(diagnostics, "merged into the one simple root of its"
							+ " sub-bracket: " + evidence);
				}
			}
			if (kept != null && isTransverseIsolated(kept.candidate)) {
				kept.state = State.CERTIFIED;
				kept.grade = ExistenceGrade.EXISTENCE_CERTIFIED;
				if (kept.certificate == null) {
					kept.certificate = certificate;
				}
				continue;
			}
			IntersectionCandidate2D replacement = certifiedRoot(context, source, window, root,
					kept != null ? kept.candidate : members.get(0).candidate);
			if (replacement == null) {
				// No parameter of the bracket passes the residual check of the verification:
				// the candidate stays as it is rather than leaving its root unverified.
				if (kept != null) {
					kept.state = State.ANALYSED;
				}
				continue;
			}
			Entry created = new Entry(replacement);
			created.state = State.CERTIFIED;
			created.grade = ExistenceGrade.EXISTENCE_CERTIFIED;
			created.certificate = certificate;
			created.contact = kept != null ? kept.contact : null;
			created.order = ContactOrder.ONE;
			if (kept != null) {
				kept.withdraw(diagnostics, "reclassified as a transverse root with certified"
						+ " existence: " + evidence);
			}
			group.add(created);
			entries.add(created);
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CERTIFIED_EXISTENCE_ESTABLISHED,
					"Certified simple root at " + replacement.getSemanticParameter()
							+ (kept == null ? " without a published candidate" : "") + ": "
							+ evidence));
		}
	}

	/**
	 * The contradictions of the form one double root at an exact point {@code t0} (Stage B
	 * design section 9.2, PRE-G9B-CU3 increment E3): a candidate whose isolating interval misses
	 * {@code t0} is withdrawn (existence); of those that contain it, all but the closest are
	 * merged into it (count); and that one, published as transverse, tangent or undetermined, is
	 * replaced by a tangent candidate at {@code t0} with multiplicity two, the existence grade
	 * {@code EXISTENCE_EXACT}, contact class 1 or 2 and its order of contact (multiplicity). The
	 * replacement never inherits a transverse germ, so the token of a transverse candidate stops
	 * being admissible and is never reassociated. When no binary64 parameter of {@code t0} passes
	 * the residual check of the verification, the candidates that contain it stay as they are.
	 */
	private static void resolveDouble(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, Window window, RootCountCertificate2D certificate,
			List<Entry> members, List<Entry> group, List<Entry> entries,
			List<IntersectionDiagnostic2D> diagnostics) {
		double lower = certificate.getLower();
		double upper = certificate.getUpper();
		double[] root = certificate.getRoots().get(0);
		ExactContactEvidence2D exact = certificate.getExact();
		String evidence = certificate.describe();
		List<Entry> located = new ArrayList<>();
		for (Entry member : members) {
			if (window.overlaps(member, root, lower, upper)) {
				located.add(member);
			} else {
				member.withdraw(diagnostics, "the isolating interval misses the one double root"
						+ " at " + exact.getParameter() + ": " + evidence);
			}
		}
		Entry kept = null;
		double distance = Double.POSITIVE_INFINITY;
		double middle = root[0] + (root[1] - root[0]) / 2;
		for (Entry member : located) {
			double current = Math.abs(window.lift(member.candidate.getSemanticParameter(), lower,
					upper) - middle);
			if (current < distance) {
				distance = current;
				kept = member;
			}
		}
		IntersectionCandidate2D replacement = doubleRoot(context, source, window, root,
				kept != null ? kept.candidate : members.get(0).candidate, exact);
		if (replacement == null) {
			// No parameter of t0 passes the residual check of the verification: the candidates
			// that contain it stay as they are rather than leaving the root unverified.
			for (Entry member : located) {
				member.state = State.ANALYSED;
			}
			return;
		}
		for (Entry member : located) {
			if (member != kept) {
				member.withdraw(diagnostics, "merged into the one double root at "
						+ exact.getParameter() + ": " + evidence);
			}
		}
		Entry created = new Entry(replacement);
		created.state = State.CERTIFIED;
		created.grade = ExistenceGrade.EXISTENCE_EXACT;
		created.certificate = certificate;
		created.contact = exact.getEvidence();
		created.order = exact.getOrder();
		if (kept != null) {
			kept.withdraw(diagnostics, "reclassified as the one double root at the exact"
					+ " parameter " + exact.getParameter() + " (" + exact.getOrder() + "): "
					+ evidence);
		}
		group.add(created);
		entries.add(created);
		diagnostics.add(new IntersectionDiagnostic2D(DiagnosticCode.CERTIFIED_EXISTENCE_ESTABLISHED,
				"Certified double root at the exact parameter " + exact.getParameter() + ", "
						+ exact.getEvidence() + ", order " + exact.getOrder()
						+ (kept == null ? " without a published candidate" : "") + ": "
						+ evidence));
	}

	/**
	 * A tangent candidate of multiplicity two at an exact parameter, at the binary64 parameter of
	 * its enclosure whose residual passes the residual check of the verification: the parameter
	 * of the candidate it replaces when that one lies in the enclosure, otherwise the end with
	 * the smallest residual. Its isolation stays {@code NOT_ESTABLISHED}, as for every contact of
	 * G8B-R1, so it gets no transverse germ.
	 *
	 * @return the candidate, or {@code null} when no such parameter passes the check
	 */
	private static IntersectionCandidate2D doubleRoot(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, Window window, double[] root,
			IntersectionCandidate2D template, ExactContactEvidence2D exact) {
		double own = template.getSemanticParameter();
		double ownLifted = window.lift(own, root[0], root[1]);
		double chosen = Double.NaN;
		double smallest = Double.POSITIVE_INFINITY;
		if (root[0] <= ownLifted && ownLifted <= root[1] && passes(context, template, own) >= 0) {
			chosen = own;
		} else {
			for (double lifted : new double[] {root[0], root[1]}) {
				double parameter = context.getDefinition().getProvider().canonicalize(lifted
						+ window.canonicalShift(lifted));
				double magnitude = passes(context, template, parameter);
				if (magnitude >= 0 && magnitude < smallest) {
					smallest = magnitude;
					chosen = parameter;
				}
			}
		}
		if (Double.isNaN(chosen)) {
			return null;
		}
		return new IntersectionCandidate2D(template.getBranchKey(), template.getComponentKey(),
				chosen, OptionalDouble.empty(), new IntersectionParameterInterval2D(chosen, chosen),
				LocalIsolationStatus.NOT_ESTABLISHED, Optional.empty(),
				ContactClass.TANGENT_ESTABLISHED, MultiplicityStatus.ESTABLISHED, OptionalInt.of(2),
				SolverMethod.CERTIFIED_INTERVAL, NumericGuarantee.CERTIFIED_ERROR_BOUND,
				LineageEventKind.APPEARED, Collections.emptyList(), List.of(
						new IntersectionDiagnostic2D(DiagnosticCode.CONTACT_EVIDENCE_CLASSIFIED,
								"Root-count certificate of "
										+ CertifiedAdmissionPolicy2D.version(source)
										+ ": one double root at the exact parameter "
										+ exact.getParameter() + ", " + exact.getEvidence()
										+ ", order " + exact.getOrder())));
	}

	/**
	 * @return the magnitude of the normalized binary64 residual at the parameter when it passes
	 *         the residual check of the verification, otherwise {@code -1}
	 */
	private static double passes(IntersectionCapabilityContext2D context,
			IntersectionCandidate2D template, double parameter) {
		LocusEvaluation2D evaluation = context.evaluate(template.getBranchKey(), parameter);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return -1;
		}
		TargetResidualEvaluation2D residual = context.evaluateResidualEvidence(
				evaluation.getPoint());
		if (!residual.isEstablished()) {
			return -1;
		}
		TargetResidual2D value = residual.getResidual().get();
		double magnitude = Math.abs(value.getNormalizedResidual());
		return magnitude <= context.getQuery().getPolicy().getResidualTolerance()
				.threshold(value.getCharacteristicScale()) ? magnitude : -1;
	}

	/**
	 * The preimage that keeps its candidate: the transverse isolated one, so that its identity
	 * is kept, the one closest to the root when several are; otherwise the candidate closest to
	 * the root, which is reclassified.
	 */
	private static Entry representative(List<Entry> preimage, double[] root, Window window,
			double lower, double upper) {
		boolean anyIsolated = false;
		for (Entry member : preimage) {
			anyIsolated |= isTransverseIsolated(member.candidate);
		}
		Entry closest = null;
		double distance = Double.POSITIVE_INFINITY;
		double middle = root[0] + (root[1] - root[0]) / 2;
		for (Entry member : preimage) {
			if (anyIsolated && !isTransverseIsolated(member.candidate)) {
				continue;
			}
			double current = Math.abs(window.lift(member.candidate.getSemanticParameter(),
					lower, upper) - middle);
			if (current < distance) {
				distance = current;
				closest = member;
			}
		}
		return closest;
	}

	/**
	 * A transverse isolated candidate in a certified root bracket, at the point of the bracket
	 * whose binary64 residual passes the residual check of the verification with the smallest
	 * magnitude: the midpoint or an end.
	 *
	 * @return the candidate, or {@code null} when no such point passes the check
	 */
	private static IntersectionCandidate2D certifiedRoot(IntersectionCapabilityContext2D context,
			ResidualJetSource2D source, Window window, double[] root,
			IntersectionCandidate2D template) {
		double middle = root[0] + (root[1] - root[0]) / 2;
		double shift = window.canonicalShift(middle);
		double chosen = Double.NaN;
		double smallest = Double.POSITIVE_INFINITY;
		for (double lifted : new double[] {middle, root[0], root[1]}) {
			double parameter = context.getDefinition().getProvider().canonicalize(lifted + shift);
			double magnitude = passes(context, template, parameter);
			if (magnitude >= 0 && magnitude < smallest) {
				smallest = magnitude;
				chosen = parameter;
			}
		}
		if (Double.isNaN(chosen)) {
			return null;
		}
		double low = Math.min(chosen, root[0] + shift);
		double high = Math.max(chosen, root[1] + shift);
		return new IntersectionCandidate2D(template.getBranchKey(), template.getComponentKey(),
				chosen, OptionalDouble.empty(), new IntersectionParameterInterval2D(low, high),
				LocalIsolationStatus.ESTABLISHED, Optional.empty(),
				ContactClass.TRANSVERSE_ESTABLISHED, MultiplicityStatus.ESTABLISHED,
				OptionalInt.of(1), SolverMethod.CERTIFIED_INTERVAL,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, LineageEventKind.APPEARED,
				Collections.emptyList(), List.of(new IntersectionDiagnostic2D(
						DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED,
						"Root-count certificate of " + CertifiedAdmissionPolicy2D.version(source)
								+ ": one simple root in the bracket")));
	}

	/**
	 * The grade {@code EXISTENCE_CERTIFIED} of a transverse isolated candidate: a simple root
	 * certified on a box around it, widened from its own bracket while {@code h'} stays
	 * certified nonzero and the box stays in its sampling cell.
	 */
	private static void grade(ResidualJetSource2D source, Window window, Entry entry) {
		IntersectionCandidate2D candidate = entry.candidate;
		if (!isTransverseIsolated(candidate) || entry.grade == ExistenceGrade.EXISTENCE_CERTIFIED) {
			return;
		}
		double center = candidate.getSemanticParameter();
		double half = Math.max(width(candidate) / 2,
				CertifiedAdmissionPolicy2D.bracketWidth(center));
		double limit = window.span / CertifiedAdmissionPolicy2D.CELL_DIVISOR;
		for (int expansion = 0; expansion <= CertifiedAdmissionPolicy2D.GRADE_EXPANSIONS
				&& half <= limit; expansion++, half *= 2) {
			double[] box = window.box(center, half);
			if (box == null) {
				return;
			}
			RootCountCertificate2D certificate = RootCountCertificate2D.certifySimple(source,
					box[0], box[1]);
			if (certificate.isCertified()) {
				entry.grade = ExistenceGrade.EXISTENCE_CERTIFIED;
				entry.certificate = certificate;
				return;
			}
			// h' not certified nonzero or ends of one sign: a wider box cannot certify more; a
			// box clipped by the component is the widest one.
			if (certificate.getContact() != null || box[2] == 1) {
				return;
			}
		}
	}

	private static boolean isTransverseIsolated(IntersectionCandidate2D candidate) {
		return candidate.getContactClass() == ContactClass.TRANSVERSE_ESTABLISHED
				&& candidate.getLocalIsolationStatus() == LocalIsolationStatus.ESTABLISHED;
	}

	private static double width(IntersectionCandidate2D candidate) {
		return candidate.getIsolatingInterval().getUpper()
				- candidate.getIsolatingInterval().getLower();
	}

	/** Analysis state of one candidate. */
	private enum State {
		OPEN, ANALYSED, CERTIFIED, WITHDRAWN
	}

	/** One candidate with the evidence the analysis gives it. */
	private static final class Entry {
		private final IntersectionCandidate2D candidate;
		private State state = State.OPEN;
		private ExistenceGrade grade = ExistenceGrade.LOCALLY_VERIFIED;
		private ContactEvidenceClass contact;
		private ContactOrder order;
		private RootCountCertificate2D certificate;

		Entry(IntersectionCandidate2D candidate) {
			this.candidate = candidate;
			if (candidate.getExistenceEvidence().getGrade()
					== ExistenceGrade.EXISTENCE_CERTIFIED) {
				grade = ExistenceGrade.EXISTENCE_CERTIFIED;
			}
		}

		void withdraw(List<IntersectionDiagnostic2D> diagnostics, String why) {
			state = State.WITHDRAWN;
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE, "Candidate "
							+ candidate.getContactClass() + " at "
							+ candidate.getSemanticParameter() + (contact == null ? ""
									: " (" + contact + ")") + " withdrawn: " + why));
		}

		IntersectionCandidate2D publish(IntersectionCapabilityContext2D context,
				ResidualJetSource2D source) {
			IntersectionExistenceEvidence2D evidence = candidate.getExistenceEvidence();
			String described = certificate != null ? certificate.describe()
					: evidence.getCertificate();
			IntersectionExistenceEvidence2D computed = IntersectionExistenceEvidence2D.of(grade,
					source.getConfiguration(), contact, described);
			if (evidence.isCertifiedRoute()) {
				// The capability certified it; the route keeps its own certificate and scale.
				computed = computed.withCertifiedRoute(evidence.getCharacteristicScale());
			}
			if (order != null) {
				computed = computed.withContactOrder(order);
			}
			if (source.isConstructive()) {
				computed = computed.withConstructiveAdmission(admission(context, source));
			}
			return candidate.withExistenceEvidence(computed);
		}

		/**
		 * The re-qualification of {@code locus-v2-constructive-authority.md} section 7.4:
		 * {@code STRICT} with existence certified against the constructive target,
		 * {@code RELAXED} by {@code ENCLOSURE_COMPATIBLE} for a transverse root whose
		 * constructive residual enclosure contains zero within the G8B-R1 threshold, and
		 * {@code NOT_ESTABLISHED} otherwise; a contact without a sign change is never relaxed.
		 */
		private ConstructiveAdmission admission(IntersectionCapabilityContext2D context,
				ResidualJetSource2D source) {
			if (!isTransverseIsolated(candidate)) {
				return ConstructiveAdmission.NOT_ESTABLISHED;
			}
			if (grade == ExistenceGrade.EXISTENCE_CERTIFIED) {
				return ConstructiveAdmission.STRICT;
			}
			if (atCertifiedFold(context)) {
				// At a certified fold the residual is even: a contact without a sign change,
				// never relaxed by a residual bound (section 7.2).
				return ConstructiveAdmission.NOT_ESTABLISHED;
			}
			double[] residual = source.normalizedResidual(candidate.getSemanticParameter());
			if (residual == null || residual[0] > 0 || residual[1] < 0) {
				return ConstructiveAdmission.NOT_ESTABLISHED;
			}
			LocusEvaluation2D evaluation = context.evaluate(candidate.getBranchKey(),
					candidate.getSemanticParameter());
			if (!evaluation.isValid() || evaluation.getPoint() == null) {
				return ConstructiveAdmission.NOT_ESTABLISHED;
			}
			TargetResidualEvaluation2D adapter = context.evaluateResidualEvidence(
					evaluation.getPoint());
			if (!adapter.isEstablished()) {
				return ConstructiveAdmission.NOT_ESTABLISHED;
			}
			double threshold = context.getQuery().getPolicy().getResidualTolerance()
					.threshold(adapter.getResidual().get().getCharacteristicScale());
			return Math.max(-residual[0], residual[1]) <= threshold
					? ConstructiveAdmission.RELAXED : ConstructiveAdmission.NOT_ESTABLISHED;
		}

		/** @return whether the isolating interval meets a certified fold of the source */
		private boolean atCertifiedFold(IntersectionCapabilityContext2D context) {
			LocusBranch2D branch = context.getDefinition().getBranch(candidate.getBranchKey());
			if (branch == null) {
				return false;
			}
			LocusInterval2D domain = context.getDefinition().getProvider().getDeclaredDomain();
			double period = context.getDefinition().getProvider().isPeriodic()
					? domain.getUpper() - domain.getLower() : 0;
			IntersectionParameterInterval2D interval = candidate.getIsolatingInterval();
			for (LocusCertifiedFold2D fold : branch.getExistenceStructure().getCertifiedFolds()) {
				for (int shift = -1; shift <= 1; shift++) {
					double offset = shift * period;
					if (fold.getParameterLower() + offset <= interval.getUpper()
							&& fold.getParameterUpper() + offset >= interval.getLower()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/** The certified continuous component of a group of candidates, in parameter terms. */
	private static final class Window {
		private final double lower;
		private final double upper;
		private final double span;
		private final double period;
		private final double domainLower;
		private final double domainUpper;
		private final boolean periodic;

		private Window(double lower, double upper, double period, double domainLower,
				double domainUpper, boolean periodic) {
			this.lower = lower;
			this.upper = upper;
			this.span = upper - lower;
			this.period = period;
			this.domainLower = domainLower;
			this.domainUpper = domainUpper;
			this.periodic = periodic;
		}

		/**
		 * @return the window of the component of a candidate, or {@code null} when the
		 *         component is not certified or its parameters do not follow the canonical
		 *         order
		 */
		static Window of(IntersectionCapabilityContext2D context,
				IntersectionCandidate2D candidate) {
			LocusBranch2D branch = context.getDefinition().getBranch(candidate.getBranchKey());
			if (branch == null) {
				return null;
			}
			List<ContinuousComponent> components =
					branch.getExistenceStructure().getContinuousValidComponents();
			for (int index = 0; index < components.size(); index++) {
				if (!candidate.getComponentKey().equals(IntersectionCapabilityContext2D
						.componentKey(candidate.getBranchKey(), index))) {
					continue;
				}
				ContinuousComponent component = components.get(index);
				if (!component.getNature().certifies() || !component.followsCanonicalOrder()) {
					return null;
				}
				LocusInterval2D interval = component.getInterval();
				LocusInterval2D domain = context.getDefinition().getProvider()
						.getDeclaredDomain();
				double width = interval.getUpper() - interval.getLower();
				if (!(width > 0) || Double.isInfinite(width)) {
					return null;
				}
				return new Window(interval.getLower(), interval.getUpper(),
						domain.getUpper() - domain.getLower(), domain.getLower(),
						domain.getUpper(), component.isClosedPeriodic());
			}
			return null;
		}

		/**
		 * @return {@code {lower, upper, clipped}}: a box of half-width {@code half} around the
		 *         parameter, strictly inside the component unless it is closed periodic, with
		 *         {@code clipped = 1} when the component made it narrower; or {@code null}
		 */
		double[] box(double center, double half) {
			double low = center - half;
			double high = center + half;
			if (!(low < center && center < high)) {
				return null;
			}
			if (!periodic && (low <= lower || high >= upper)) {
				double room = Math.min(center - lower, upper - center) / 2;
				if (!(room > 0)) {
					return null;
				}
				return new double[] {center - room, center + room, 1};
			}
			return new double[] {low, high, 0};
		}

		/** @return the copy of the parameter that is closest to the box */
		double lift(double parameter, double low, double high) {
			if (!periodic) {
				return parameter;
			}
			double middle = low + (high - low) / 2;
			double best = parameter;
			for (int shift = -1; shift <= 1; shift += 2) {
				double copy = parameter + shift * period;
				if (Math.abs(copy - middle) < Math.abs(best - middle)) {
					best = copy;
				}
			}
			return best;
		}

		/** @return whether the isolating interval of the candidate lies in the box */
		boolean inside(Entry entry, double low, double high) {
			IntersectionParameterInterval2D interval = entry.candidate.getIsolatingInterval();
			double offset = lift(entry.candidate.getSemanticParameter(), low, high)
					- entry.candidate.getSemanticParameter();
			return interval.getLower() + offset >= low && interval.getUpper() + offset <= high;
		}

		/** @return whether the isolating interval of the candidate meets the root bracket */
		boolean overlaps(Entry entry, double[] root, double low, double high) {
			IntersectionParameterInterval2D interval = entry.candidate.getIsolatingInterval();
			double offset = lift(entry.candidate.getSemanticParameter(), low, high)
					- entry.candidate.getSemanticParameter();
			return interval.getLower() + offset <= root[1]
					&& interval.getUpper() + offset >= root[0];
		}

		/** @return the multiple of the period that brings a lifted parameter into the domain */
		double canonicalShift(double parameter) {
			if (!periodic) {
				return 0;
			}
			if (parameter > domainUpper) {
				return -period;
			}
			return parameter < domainLower ? period : 0;
		}
	}
}
