/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Structural certificate of the function of the driver that a between-position length
 * defines ({@code locus-v2-metrics.md} section 26; oriented length design sections 4 and 5).
 * For {@code LocusLength(L, A, B)} whose target {@code B} is the generator occurrence of the
 * driver of {@code L} and whose origin {@code A} is a fixed semantic position, it publishes
 * where the route exists, as pieces of the canonical driver domain, and the events of the
 * function: the origin wrap of a certified closed periodic source. Every fact derives from
 * the certified structure of the source, from semantic addresses, from the route policy and
 * from rigorous enclosures of the source program, never from a computed length.
 *
 * <p>It is revision-scoped derived data, never persisted, and changes the value of no
 * query. A certificate that cannot be established leaves the dependent existence
 * unresolved, never nonexistent.</p>
 */
public final class LocusMetricFunctionStructure2D {
	/** Version of the structural certificate. */
	public static final String VERSION = "metric-function-structure/v1";

	/** Whether the route exists on a piece of the driver domain. */
	public enum State {
		/**
		 * The route exists, its length is finite and it is continuous on the piece away
		 * from the events.
		 */
		DEFINED,
		/** The certified structure of the source and the route policy exclude the route. */
		UNDEFINED,
		/** The source does not decide the route. */
		UNRESOLVED,
		/**
		 * The enclosure of a certified boundary of the source component: the route exists
		 * strictly on its defined side; the exact root itself is not decided here.
		 */
		BOUNDARY
	}

	/** One piece of the canonical driver domain. */
	public static final class Piece {
		private final LocusInterval2D interval;
		private final State state;
		private final LocusBoundary2D boundary;
		private final boolean definedBelow;

		/**
		 * @param interval canonical interval of the piece
		 * @param state state of the route on it
		 * @param boundary certified boundary of the source that a {@link State#BOUNDARY}
		 *        piece encloses; {@code null} for the other states
		 * @param definedBelow for a boundary piece, whether the route exists below the root
		 */
		public Piece(LocusInterval2D interval, State state, LocusBoundary2D boundary,
				boolean definedBelow) {
			this.interval = Objects.requireNonNull(interval);
			this.state = Objects.requireNonNull(state);
			this.boundary = boundary;
			this.definedBelow = definedBelow;
			if ((state == State.BOUNDARY) != (boundary != null)
					|| boundary != null && !boundary.hasEnclosure()) {
				throw new IllegalArgumentException(
						"Exactly a boundary piece carries an enclosed certified boundary");
			}
		}

		/** @return canonical interval of the piece */
		public LocusInterval2D getInterval() {
			return interval;
		}

		/** @return state of the route on the piece */
		public State getState() {
			return state;
		}

		/** @return enclosed boundary of the source of a boundary piece, or {@code null} */
		public LocusBoundary2D getBoundary() {
			return boundary;
		}

		/** @return for a boundary piece, whether the route exists below its root */
		public boolean isDefinedBelow() {
			return definedBelow;
		}

		/** @return canonical text of the piece */
		public String getSignature() {
			return interval + ":" + state + (boundary == null ? ""
					: ":" + boundary.getSignature() + (definedBelow ? ":below" : ":above"));
		}
	}

	/**
	 * Origin wrap event (section 26.2): at the canonical address of the origin the length
	 * restarts. Its two one-sided ends share that address and are distinct; the same-position
	 * policy decides which of them is included. The one-sided limits of the length are
	 * {@code 0} and the total length {@code L} of the source, which the event bounds
	 * rigorously without reading it.
	 */
	public static final class OriginWrap {
		private final double driverParameter;
		private final TraversalDirection direction;
		private final SamePositionPolicy samePosition;
		private final double jumpLowerBound;
		private final double jumpUpperBound;
		private final String jumpValueReference;

		/**
		 * @param driverParameter canonical address of the origin
		 * @param direction traversal direction of the route
		 * @param samePosition same-position policy of the route
		 * @param jumpLowerBound outward-rounded positive chord of the source, a lower bound
		 *        of {@code L}; {@code NaN} when no positive chord is certified
		 * @param jumpUpperBound rigorous upper bound of {@code L} from the enclosures of the
		 *        speed of the source
		 * @param jumpValueReference reference to the total length of the source, which this
		 *        certificate never reads
		 */
		public OriginWrap(double driverParameter, TraversalDirection direction,
				SamePositionPolicy samePosition, double jumpLowerBound, double jumpUpperBound,
				String jumpValueReference) {
			if (!Double.isFinite(driverParameter) || !Double.isFinite(jumpUpperBound)
					|| !(jumpUpperBound > 0) || jumpLowerBound > jumpUpperBound
					|| !Double.isNaN(jumpLowerBound) && !(jumpLowerBound > 0)) {
				throw new IllegalArgumentException("Invalid origin wrap");
			}
			this.driverParameter = driverParameter;
			this.direction = Objects.requireNonNull(direction);
			this.samePosition = Objects.requireNonNull(samePosition);
			this.jumpLowerBound = jumpLowerBound;
			this.jumpUpperBound = jumpUpperBound;
			this.jumpValueReference = Objects.requireNonNull(jumpValueReference);
		}

		/** @return canonical address of the origin, exact */
		public double getDriverParameter() {
			return driverParameter;
		}

		/** @return traversal direction */
		public TraversalDirection getDirection() {
			return direction;
		}

		/** @return same-position policy */
		public SamePositionPolicy getSamePosition() {
			return samePosition;
		}

		/** @return whether the jump is certified positive (a positive chord of the source) */
		public boolean isJumpPositive() {
			return jumpLowerBound > 0;
		}

		/** @return certified lower bound of the jump, or {@code NaN} */
		public double getJumpLowerBound() {
			return jumpLowerBound;
		}

		/** @return certified upper bound of the jump */
		public double getJumpUpperBound() {
			return jumpUpperBound;
		}

		/** @return reference to the total length of the source, never read here */
		public String getJumpValueReference() {
			return jumpValueReference;
		}

		/**
		 * @return whether the origin belongs to the arc that starts there: the length is zero
		 *         at the origin with {@code zero-length} forward, and so on by symmetry
		 */
		public boolean isStartIncluded() {
			return (samePosition == SamePositionPolicy.ZERO_LENGTH)
					== (direction == TraversalDirection.FORWARD);
		}

		/**
		 * @param originToken canonical token of the consumer of the length
		 * @param start whether the end starts the arc ({@code s = 0}) or ends it
		 * @return end of an arc of a dependent locus at this event
		 */
		public LocusBoundary2D end(String originToken, boolean start) {
			return new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
					new LocusBoundarySource2D(originToken, "ORIGIN_WRAP", start ? "start" : "end"),
					start == isStartIncluded(), LocusEvidenceNature2D.EXACT);
		}

		/** @return canonical text of the event */
		public String getSignature() {
			return "ORIGIN_WRAP[" + Double.toHexString(driverParameter) + "]:" + direction
					+ ":" + samePosition + ":jump=" + (isJumpPositive() ? "CERTIFIED["
							+ Double.toHexString(jumpLowerBound) + "," : "NOT_ESTABLISHED[,")
					+ Double.toHexString(jumpUpperBound) + "]:" + jumpValueReference;
		}
	}

	private final boolean certified;
	private final LocusCoverageCause2D cause;
	private final String evidence;
	private final List<Piece> pieces;
	private final List<OriginWrap> events;

	private LocusMetricFunctionStructure2D(boolean certified, LocusCoverageCause2D cause,
			String evidence, List<Piece> pieces, List<OriginWrap> events) {
		this.certified = certified;
		this.cause = cause;
		this.evidence = Objects.requireNonNull(evidence);
		this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
		this.events = Collections.unmodifiableList(new ArrayList<>(events));
	}

	/**
	 * @param pieces pieces tiling the canonical driver domain, in order
	 * @param events events of the function
	 * @param evidence evidence of the structure
	 * @return certified structure
	 */
	public static LocusMetricFunctionStructure2D certified(List<Piece> pieces,
			List<OriginWrap> events, String evidence) {
		if (pieces.isEmpty()) {
			throw new IllegalArgumentException("A certified structure covers the domain");
		}
		return new LocusMetricFunctionStructure2D(true, null, evidence, pieces, events);
	}

	/**
	 * @param cause typed cause; never a nonexistence cause
	 * @param evidence evidence of the refusal
	 * @return structure that is not established
	 */
	public static LocusMetricFunctionStructure2D notEstablished(LocusCoverageCause2D cause,
			String evidence) {
		if (!cause.admits(CoverageState.UNRESOLVED)) {
			throw new IllegalArgumentException("An unestablished structure is unresolved");
		}
		return new LocusMetricFunctionStructure2D(false, cause, evidence, List.of(),
				List.of());
	}

	/**
	 * Derives the structure from the certified structure of the source (oriented length
	 * design section 4.3). A closed periodic source defines the length everywhere, with one
	 * origin wrap at the origin, once a rigorous upper bound of its length establishes that
	 * the route is finite. An open source component that contains the origin defines it,
	 * under a strict {@code zero-length} route, from the origin to the end of the component in
	 * the traversal direction, closed at the origin; the enclosure of that end keeps the
	 * certified boundary of the source, the unresolved coverage of the source stays
	 * unresolved and the rest of the domain is unreachable. Any other case is not
	 * established.
	 *
	 * @param source certified existence structure of the source branch
	 * @param origin canonical address of the fixed origin
	 * @param direction traversal direction
	 * @param boundaryPolicy open boundary policy
	 * @param samePosition same-position policy
	 * @param chord outward-rounded lower bound of a chord of the source, or {@code NaN}
	 * @param lengthBound rigorous upper bound of the length of a closed periodic source, or
	 *        {@code NaN}
	 * @param jumpReference reference to the total length of the source, never read here
	 * @return structure of the length as a function of the driver
	 */
	public static LocusMetricFunctionStructure2D derive(LocusExistenceStructure2D source,
			double origin, TraversalDirection direction, OpenBoundaryPolicy boundaryPolicy,
			SamePositionPolicy samePosition, double chord, double lengthBound,
			String jumpReference) {
		LocusInterval2D domain = source.getCanonicalDomain();
		String evidence = "source=" + source.getCertifierContract() + ":"
				+ source.getCompleteness() + "|origin=" + Double.toHexString(origin) + "|"
				+ direction + "|" + boundaryPolicy + "|" + samePosition;
		if (!domain.contains(origin, 0)) {
			return notEstablished(LocusCoverageCause2D.NOT_CERTIFIED,
					"origin-outside-the-canonical-domain|" + evidence);
		}
		if (source.isClosedPeriodic()) {
			if (!(lengthBound > 0) || !Double.isFinite(lengthBound)) {
				return notEstablished(LocusCoverageCause2D.NOT_CERTIFIED,
						"rectifiability-not-established|" + evidence);
			}
			// Every route on a certified closed component exists: one piece, one wrap.
			boolean positive = chord > 0 && chord <= lengthBound;
			return certified(List.of(new Piece(domain, State.DEFINED, null, false)),
					List.of(new OriginWrap(origin, direction, samePosition,
							positive ? chord : Double.NaN, lengthBound, jumpReference)),
					evidence + "|length<=" + Double.toHexString(lengthBound));
		}
		if (boundaryPolicy != OpenBoundaryPolicy.STRICT
				|| samePosition != SamePositionPolicy.ZERO_LENGTH) {
			return notEstablished(LocusCoverageCause2D.NOT_CERTIFIED,
					"open-source-policy-without-structure|" + evidence);
		}
		ContinuousComponent component = source.getComponentContaining(origin);
		if (component == null || component.isArc() && component.isFullLength()) {
			return notEstablished(LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED,
					"origin-outside-an-open-certified-component|" + evidence);
		}
		boolean forward = direction == TraversalDirection.FORWARD;
		LocusBoundary2D far = forward ? component.getUpperBoundary()
				: component.getLowerBoundary();
		List<Piece> defined = definedPieces(domain, component, origin, forward);
		List<Piece> pieces = new ArrayList<>(defined);
		Reach reach = new Reach(far, origin, forward,
				source.getTopology() == LocusExistenceStructure2D.DomainTopology.PERIODIC);
		for (Coverage coverage : source.getCoverage()) {
			addOutside(pieces, coverage, defined, reach);
		}
		pieces.sort((a, b) -> Double.compare(a.interval.getLower(), b.interval.getLower()));
		return certified(pieces, List.of(), evidence);
	}

	/**
	 * Where a strict route from the origin cannot reach. It stops at the far end of the
	 * origin component only when that end is certified: a transition, a domain end or a
	 * structural event. Past the edge of an unresolved band the source may continue, so
	 * reachability there is unresolved; on a bounded domain the part behind the origin is
	 * unreachable in any case, because the route never wraps.
	 */
	private static final class Reach {
		private final LocusBoundary2D far;
		private final double origin;
		private final boolean forward;
		private final boolean stops;
		private final boolean periodic;

		Reach(LocusBoundary2D far, double origin, boolean forward, boolean periodic) {
			this.far = far;
			this.origin = origin;
			this.forward = forward;
			this.periodic = periodic;
			LocusBoundary2D.Kind kind = far.getKind();
			stops = kind == LocusBoundary2D.Kind.CERTIFIED_TRANSITION
					|| kind == LocusBoundary2D.Kind.DOMAIN_END
					|| kind == LocusBoundary2D.Kind.STRUCTURAL_EVENT;
		}

		/** @return whether the route certainly cannot reach the part */
		boolean unreachable(double lower, double upper) {
			if (stops) {
				return true;
			}
			return !periodic && (forward ? upper <= origin : lower >= origin);
		}
	}

	/**
	 * @return the canonical pieces of the route from the origin to the far end of its
	 *         component, closed at the origin; across the seam for an arc that crosses it
	 */
	private static List<Piece> definedPieces(LocusInterval2D domain,
			ContinuousComponent component, double origin, boolean forward) {
		double start = component.getStart();
		double end = component.getEnd();
		boolean startIncluded = component.getLowerBoundary().isBinaryEndIncluded();
		boolean endIncluded = component.getUpperBoundary().isBinaryEndIncluded();
		List<Piece> result = new ArrayList<>();
		boolean crossing = component.isArc() && component.crossesSeam();
		boolean originAfterSeam = crossing && origin <= end;
		if (forward) {
			if (crossing && !originAfterSeam) {
				result.add(defined(origin, domain.getUpper(), true, false));
				result.add(defined(domain.getLower(), end, true, endIncluded));
			} else {
				result.add(defined(origin, end, true, endIncluded));
			}
		} else if (crossing && originAfterSeam) {
			result.add(defined(start, domain.getUpper(), startIncluded, false));
			result.add(defined(domain.getLower(), origin, true, true));
		} else {
			result.add(defined(start, origin, startIncluded, true));
		}
		return result;
	}

	private static Piece defined(double lower, double upper, boolean lowerClosed,
			boolean upperClosed) {
		return new Piece(new LocusInterval2D(lower, upper, lowerClosed, upperClosed),
				State.DEFINED, null, false);
	}

	/**
	 * Adds the parts of one coverage item of the source outside the defined part: its
	 * unresolved coverage stays unresolved, the enclosure of the far boundary of the origin
	 * component carries that boundary, and the rest is unreachable by a strict route where
	 * the route certainly stops, unresolved elsewhere.
	 */
	private static void addOutside(List<Piece> pieces, Coverage coverage, List<Piece> defined,
			Reach reach) {
		LocusBoundary2D far = reach.far;
		boolean forward = reach.forward;
		LocusInterval2D interval = coverage.getInterval();
		List<double[]> remaining = new ArrayList<>();
		remaining.add(new double[] {interval.getLower(), interval.getUpper(),
			interval.isLowerClosed() ? 1 : 0, interval.isUpperClosed() ? 1 : 0});
		for (Piece piece : defined) {
			List<double[]> next = new ArrayList<>();
			for (double[] part : remaining) {
				next.addAll(subtract(part, piece.interval));
			}
			remaining = next;
		}
		boolean farEnclosure = coverage.getState() == CoverageState.CERTIFIED_BOUNDARY
				&& reach.stops && far.hasEnclosure()
				&& interval.getLower() <= far.getEnclosureUpper()
				&& far.getEnclosureLower() <= interval.getUpper();
		for (double[] part : remaining) {
			boolean lowerClosed = part[2] == 1;
			boolean upperClosed = part[3] == 1;
			if (!(part[0] < part[1] || part[0] == part[1] && lowerClosed && upperClosed)) {
				continue;
			}
			State state;
			if (coverage.getState() == CoverageState.UNRESOLVED) {
				state = State.UNRESOLVED;
			} else if (farEnclosure) {
				state = State.BOUNDARY;
			} else {
				state = reach.unreachable(part[0], part[1]) ? State.UNDEFINED
						: State.UNRESOLVED;
			}
			pieces.add(new Piece(new LocusInterval2D(part[0], part[1], lowerClosed,
					upperClosed), state, state == State.BOUNDARY ? far : null, forward));
		}
	}

	/** @return the parts of {@code {lower, upper, lowerClosed, upperClosed}} outside a cut */
	private static List<double[]> subtract(double[] part, LocusInterval2D cut) {
		List<double[]> result = new ArrayList<>();
		boolean disjoint = cut.getUpper() < part[0] || cut.getLower() > part[1]
				|| cut.getUpper() == part[0] && !(cut.isUpperClosed() && part[2] == 1)
				|| cut.getLower() == part[1] && !(cut.isLowerClosed() && part[3] == 1);
		if (disjoint) {
			result.add(part);
			return result;
		}
		if (part[0] < cut.getLower()
				|| part[0] == cut.getLower() && part[2] == 1 && !cut.isLowerClosed()) {
			result.add(new double[] {part[0], cut.getLower(), part[2],
				cut.isLowerClosed() ? 0 : 1});
		}
		if (cut.getUpper() < part[1]
				|| cut.getUpper() == part[1] && part[3] == 1 && !cut.isUpperClosed()) {
			result.add(new double[] {cut.getUpper(), part[1], cut.isUpperClosed() ? 0 : 1,
				part[3]});
		}
		return result;
	}

	/** @return whether the structure is certified */
	public boolean isCertified() {
		return certified;
	}

	/** @return typed cause of a structure that is not established, or {@code null} */
	public LocusCoverageCause2D getCause() {
		return cause;
	}

	/** @return evidence of the structure */
	public String getEvidence() {
		return evidence;
	}

	/** @return pieces of the canonical driver domain, in order */
	public List<Piece> getPieces() {
		return pieces;
	}

	/** @return events of the function */
	public List<OriginWrap> getEvents() {
		return events;
	}

	/**
	 * @return the piece that contains the canonical parameter, or {@code null}
	 */
	public Piece pieceContaining(double canonicalParameter) {
		for (Piece piece : pieces) {
			if (piece.interval.contains(canonicalParameter, 0)) {
				return piece;
			}
		}
		return null;
	}

	/** @return canonical text of the structure */
	public String getSignature() {
		StringBuilder signature = new StringBuilder(VERSION).append('|')
				.append(certified ? "CERTIFIED" : "NOT_ESTABLISHED:" + cause).append('|')
				.append(evidence);
		for (Piece piece : pieces) {
			signature.append("|piece=").append(piece.getSignature());
		}
		for (OriginWrap event : events) {
			signature.append("|event=").append(event.getSignature());
		}
		return signature.toString();
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
