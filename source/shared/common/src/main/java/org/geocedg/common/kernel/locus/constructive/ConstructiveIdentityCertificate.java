/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The outcome of one attempt of a constructive identity certificate
 * ({@code constructive-identity-certificate/v1}, {@code locus-v2-constructive-authority.md}
 * section 8) for one incidence between objects of the class. {@code IDENTITY_CERTIFIED}: the
 * residual of the incidence is the zero element of the field and every recorded condition is
 * certified on a box that contains the ideal value of every indeterminate, so the incidence is
 * {@code STRICT} on that box. {@code NOT_AN_IDENTITY}: the residual is not the zero element,
 * which is never a contradiction. {@code CONTRADICTED}: a certified non-membership of the limited
 * part of the target. {@code INVALID}: a validity condition certified false, the relation being
 * {@code NOT_ESTABLISHED} with {@code OBJECT_UNDEFINED}. {@code NOT_ESTABLISHED}: an undecided
 * condition, a construct outside the class, an ideal value without a rigorous enclosure or an
 * exhausted budget, with its typed cause.
 *
 * <p>A certificate identifies its objects by durable identity only, through its structural key
 * {@code cic-structural-key/v1}. It is revision evidence and is never persisted.</p>
 */
public final class ConstructiveIdentityCertificate {

	/** Versioned name of the certifier. */
	public static final String VERSION = "constructive-identity-certificate/v1";

	/** Outcome of the attempt. */
	public enum Status {
		/** The residual is the zero element and every recorded condition is certified. */
		IDENTITY_CERTIFIED,
		/** The residual is not the zero element; never a contradiction. */
		NOT_AN_IDENTITY,
		/** The point is certified outside the limited part of the target. */
		CONTRADICTED,
		/** A validity condition is certified false: an object of the relation is undefined. */
		INVALID,
		/** Nothing is decided; the cause says why. */
		NOT_ESTABLISHED
	}

	/** Typed cause of an attempt that does not certify an identity. */
	public enum Cause {
		/** No cause: the identity is certified. */
		NONE,
		/** The residual is not the zero element of the field. */
		RESIDUAL_NOT_ZERO,
		/** The point lies outside the limited part of the target. */
		NOT_ON_LIMITED_TARGET,
		/** A validity condition is certified false. */
		OBJECT_UNDEFINED,
		/** A sign condition is not certified on the box. */
		PRECONDITION_UNDECIDED,
		/** A construct outside the class of the certifier, a second independent square root. */
		OUT_OF_CLASS,
		/** The inherited selection of an intersection is not reproduced. */
		SELECTION_NOT_RECONSTRUCTED,
		/** A live output is not certified closer to the selected branch than to the other. */
		BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED,
		/** The live algorithm is undefined where the constructive object exists. */
		LIVE_DECISION_DISAGREES,
		/** A free input or a cut has no finite binary64 value. */
		SUBSTITUTE_UNDEFINED,
		/** The inherited path map does not define the ideal point of a point on a path. */
		PATH_MAP_UNDEFINED,
		/** The versioned budget of the attempt is exhausted. */
		BUDGET_EXHAUSTED,
		/** An exponent exceeds the range of a monomial; never wrapped. */
		EXPONENT_RANGE_EXCEEDED
	}

	/**
	 * The order of contact of a certified incidence at a structural point, from the exact jets of
	 * the traced point in the parameter of its driver; its signs are conditions of the cell.
	 */
	public enum ContactOrder {
		/** The first derivative of the residual has a certified sign: order one. */
		TRANSVERSE,
		/** The first derivative is the zero element, the second has a certified sign. */
		TANGENT,
		/**
		 * The first derivative of the traced point is the zero element: a stationary point of the
		 * trace, whose fold, if any, the certificate of {@code CU2} decides (ADR 0038, decision
		 * 8), never this certificate.
		 */
		STATIONARY,
		/** The first two derivatives of the residual are the zero element. */
		HIGHER_ORDER,
		/** A sign of the jets is not certified. */
		NOT_ESTABLISHED
	}

	/** Which inputs of the relation are indeterminates. */
	public enum Scope {
		/**
		 * The boundary rule of section 8: free points and numbers are indeterminates, so the proof
		 * holds for every value of them in the cell. The only scope of {@code Intersect(L, r, D)}.
		 */
		AUTOMATIC,
		/**
		 * Free points and numbers are the exact binary64 constants of the revision, which enter
		 * the structural key; only the points on line paths, the cuts and the boundary numbers are
		 * indeterminates. The proof holds for those constants only, and a change of one makes it
		 * inapplicable (validation matrix V-AUT-CIC-1 and V-AUT-DYN-1).
		 */
		BOUNDARY
	}

	/** An indeterminate of the attempt, with the object and coordinate it stands for. */
	static final class Indeterminate {
		private final int node;
		private final int coordinate;
		private final String name;
		private final OutwardInterval interval;

		Indeterminate(int node, int coordinate, String name, OutwardInterval interval) {
			this.node = node;
			this.coordinate = coordinate;
			this.name = name;
			this.interval = interval;
		}

		/** @return the cone node it belongs to */
		int getNode() {
			return node;
		}

		/** @return the coordinate: {@code 0} for x or a number, {@code 1} for y */
		int getCoordinate() {
			return coordinate;
		}

		/** @return its name in a report */
		String getName() {
			return name;
		}

		/** @return its interval on the decision box */
		OutwardInterval getInterval() {
			return interval;
		}
	}

	/** A line-circle output checked against the live algorithm, with both formal branches. */
	static final class Correspondence {
		private final int node;
		private final ConstructiveConeEvaluator.Point<CertificateElement> selected;
		private final ConstructiveConeEvaluator.Point<CertificateElement> other;

		Correspondence(int node, ConstructiveConeEvaluator.Point<CertificateElement> selected,
				ConstructiveConeEvaluator.Point<CertificateElement> other) {
			this.node = node;
			this.selected = selected;
			this.other = other;
		}

		int getNode() {
			return node;
		}

		ConstructiveConeEvaluator.Point<CertificateElement> getSelected() {
			return selected;
		}

		ConstructiveConeEvaluator.Point<CertificateElement> getOther() {
			return other;
		}
	}

	private final String relation;
	private final Status status;
	private final Cause cause;
	private final String message;
	private final String key;
	private final List<Indeterminate> indeterminates;
	private final List<ConstructiveIdentityArithmetic.Condition> cell;
	private final List<Correspondence> correspondences;
	private final List<String> cuts;
	private final Scope scope;
	private final ContactOrder contactOrder;
	private final int[] derivativeSigns;
	private final ConstructiveIdentityCounters counters;
	private final ConstructiveIdentityArithmetic arithmetic;

	private ConstructiveIdentityCertificate(Builder builder) {
		relation = builder.relation;
		status = Objects.requireNonNull(builder.status);
		cause = Objects.requireNonNull(builder.cause);
		message = builder.message;
		key = builder.key;
		indeterminates = Collections.unmodifiableList(new ArrayList<>(builder.indeterminates));
		cell = Collections.unmodifiableList(new ArrayList<>(builder.cell));
		correspondences = Collections.unmodifiableList(new ArrayList<>(builder.correspondences));
		cuts = Collections.unmodifiableList(new ArrayList<>(builder.cuts));
		scope = builder.scope;
		contactOrder = builder.contactOrder;
		derivativeSigns = builder.derivativeSigns.clone();
		counters = builder.counters;
		arithmetic = builder.arithmetic;
	}

	/** @return the scope of the attempt */
	public Scope getScope() {
		return scope;
	}

	/**
	 * @return the order of contact of a certified incidence at a structural point, or
	 *         {@code null} without a substituted driver
	 */
	public ContactOrder getContactOrder() {
		return contactOrder;
	}

	/**
	 * The sign of a derivative of the residual of the target along the trace at the structural
	 * point, in the parameter of the driver: {@code 0} for the zero element, {@code 1} or
	 * {@code -1} when certified, otherwise {@link ConstructiveArithmetic#UNDECIDED}, also when
	 * no order of contact was computed. At a stationary point the first is the zero element and
	 * the second, when certified, says on which side of the target the trace stays.
	 *
	 * @param order {@code 1} or {@code 2}
	 * @return the sign
	 */
	public int getResidualDerivativeSign(int order) {
		if (order != 1 && order != 2) {
			throw new IllegalArgumentException("order " + order);
		}
		return derivativeSigns[order - 1];
	}

	/** @return the relation, in a report only */
	public String getRelation() {
		return relation;
	}

	/** @return the outcome */
	public Status getStatus() {
		return status;
	}

	/** @return whether the identity is certified */
	public boolean isIdentityCertified() {
		return status == Status.IDENTITY_CERTIFIED;
	}

	/** @return the typed cause, {@code NONE} for a certified identity */
	public Cause getCause() {
		return cause;
	}

	/** @return a description of the cause, or {@code null} */
	public String getMessage() {
		return message;
	}

	/** @return the structural key {@code cic-structural-key/v1}, or {@code null} without a cone */
	public String getKey() {
		return key;
	}

	/** @return the names of the indeterminates, in index order */
	public List<String> getIndeterminateNames() {
		List<String> names = new ArrayList<>();
		for (Indeterminate indeterminate : indeterminates) {
			names.add(indeterminate.getName());
		}
		return names;
	}

	/** @return the number of recorded sign conditions of the cell */
	public int getCellSize() {
		return cell.size();
	}

	/** @return the signs of the cell, in evaluation order */
	public List<Integer> getCellSigns() {
		List<Integer> signs = new ArrayList<>();
		for (ConstructiveIdentityArithmetic.Condition condition : cell) {
			signs.add(condition.getSign());
		}
		return signs;
	}

	/** @return the cut objects, by their presentation names, in cone order */
	public List<String> getCuts() {
		return cuts;
	}

	/** @return the counters of the attempt, or {@code null} when it never started */
	public ConstructiveIdentityCounters getCounters() {
		return counters;
	}

	List<Indeterminate> getIndeterminates() {
		return indeterminates;
	}

	List<ConstructiveIdentityArithmetic.Condition> getCell() {
		return cell;
	}

	List<Correspondence> getCorrespondences() {
		return correspondences;
	}

	ConstructiveIdentityArithmetic getArithmetic() {
		return arithmetic;
	}

	@Override
	public String toString() {
		return VERSION + " " + relation + ": " + status + (cause == Cause.NONE ? "" : " " + cause)
				+ (message == null ? "" : " (" + message + ")") + " scope " + scope
				+ (contactOrder == null ? "" : " contact " + contactOrder + " h'=" + sign(0)
						+ " h''=" + sign(1)) + " indeterminates="
				+ getIndeterminateNames() + " cell=" + cell.size()
				+ (counters == null ? "" : " [" + counters + "]");
	}

	private String sign(int position) {
		int value = derivativeSigns[position];
		return value == ConstructiveArithmetic.UNDECIDED ? "?" : String.valueOf(value);
	}

	static Builder builder(String relation, Scope scope) {
		return new Builder(relation, scope);
	}

	/** Assembles one certificate. */
	static final class Builder {
		private final String relation;
		private Status status;
		private Cause cause = Cause.NONE;
		private String message;
		private String key;
		private final List<Indeterminate> indeterminates = new ArrayList<>();
		private final List<ConstructiveIdentityArithmetic.Condition> cell = new ArrayList<>();
		private final List<Correspondence> correspondences = new ArrayList<>();
		private final List<String> cuts = new ArrayList<>();
		private final Scope scope;
		private ContactOrder contactOrder;
		private final int[] derivativeSigns = {ConstructiveArithmetic.UNDECIDED,
				ConstructiveArithmetic.UNDECIDED};
		private ConstructiveIdentityCounters counters;
		private ConstructiveIdentityArithmetic arithmetic;

		private Builder(String relation, Scope scope) {
			this.relation = relation;
			this.scope = Objects.requireNonNull(scope);
		}

		Builder contactOrder(ContactOrder value, int first, int second) {
			contactOrder = value;
			derivativeSigns[0] = first;
			derivativeSigns[1] = second;
			return this;
		}

		Builder key(String value) {
			key = value;
			return this;
		}

		Builder indeterminate(Indeterminate indeterminate) {
			indeterminates.add(indeterminate);
			return this;
		}

		Builder cut(String name) {
			cuts.add(name);
			return this;
		}

		Builder evaluation(ConstructiveIdentityArithmetic value) {
			arithmetic = value;
			counters = value.getCounters();
			return this;
		}

		Builder correspondence(Correspondence value) {
			correspondences.add(value);
			return this;
		}

		ConstructiveIdentityCertificate certified() {
			status = Status.IDENTITY_CERTIFIED;
			cause = Cause.NONE;
			return build();
		}

		ConstructiveIdentityCertificate outcome(Status outcome, Cause typed, String text) {
			status = outcome;
			cause = typed;
			message = text;
			return build();
		}

		private ConstructiveIdentityCertificate build() {
			if (arithmetic != null) {
				cell.addAll(arithmetic.getCell());
			}
			return new ConstructiveIdentityCertificate(this);
		}
	}
}
