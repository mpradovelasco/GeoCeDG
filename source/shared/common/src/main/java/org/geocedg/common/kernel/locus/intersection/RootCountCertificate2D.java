/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;

/**
 * A root-count certificate on one parameter box {@code T = [a, b]} of a source that the
 * decision configuration certifies valid on {@code T}, so that the residual is continuous there
 * ({@code locus-v2-intersections.md} CU3.4; Stage B design section 9.1). It establishes the
 * exact number of roots of the residual in {@code T}, counted with multiplicity, and their
 * location, in one of three interval forms:
 *
 * <ul>
 * <li>none: the residual has a certified fixed sign on {@code T}, directly, by monotonicity
 * with ends of one sign, or by a certified critical point whose value has the sign of
 * {@code h''};</li>
 * <li>one simple root: the residual is strictly monotone on a piece of {@code T} whose ends have
 * opposite certified signs;</li>
 * <li>two simple roots on either side of a certified critical point: fixed sign of
 * {@code h''}, a critical value of the opposite sign and ends of the sign of {@code h''}.</li>
 * </ul>
 *
 * <p>The form of one double root at an exact point needs an exact or structural decision and is
 * not computed by intervals. A certified critical point whose value enclosure contains zero is
 * contact class 3 and certifies no count. Every sign is that of an outward enclosure that
 * excludes zero; no tolerance and no binary64 value decides anything.</p>
 */
final class RootCountCertificate2D {

	/** Form of the certificate. */
	enum Form {
		NONE, ONE_SIMPLE, TWO_SIMPLE, NOT_CERTIFIED
	}

	private static final int INVALID = Integer.MIN_VALUE;

	private final Form form;
	private final double lower;
	private final double upper;
	private final List<double[]> pieces;
	private final List<double[]> roots;
	private final double[] critical;
	private final ContactEvidenceClass contact;
	private final int evaluations;
	private final String reason;

	private RootCountCertificate2D(Form form, double lower, double upper, List<double[]> pieces,
			List<double[]> roots, double[] critical, ContactEvidenceClass contact,
			int evaluations, String reason) {
		this.form = form;
		this.lower = lower;
		this.upper = upper;
		this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
		this.roots = Collections.unmodifiableList(new ArrayList<>(roots));
		this.critical = critical;
		this.contact = contact;
		this.evaluations = evaluations;
		this.reason = reason;
	}

	/**
	 * @param source the residual of the decision configuration
	 * @param lower lower end of the box
	 * @param upper upper end of the box
	 * @return the certificate, or why there is none
	 */
	static RootCountCertificate2D certify(ResidualJetSource2D source, double lower,
			double upper) {
		Search search = new Search(source);
		try {
			return search.run(lower, upper);
		} catch (Exhausted exhausted) {
			return search.result(Form.NOT_CERTIFIED, lower, upper, null, null,
					"the work limit of the certificate is reached");
		}
	}

	/**
	 * Certifies one simple root on a box by a single enclosure of {@code h'} that excludes zero
	 * and end values of opposite certified signs (CU3.1 (a)): the certificate of the grade
	 * {@code EXISTENCE_CERTIFIED} of a transverse root.
	 *
	 * @return {@link Form#ONE_SIMPLE} on the box, or {@link Form#NOT_CERTIFIED} with the reason;
	 *         its contact is {@link ContactEvidenceClass#CONTACT_NOT_ESTABLISHED} when
	 *         {@code h'} is not certified nonzero on the box, so that no wider box can help
	 */
	static RootCountCertificate2D certifySimple(ResidualJetSource2D source, double lower,
			double upper) {
		Search search = new Search(source);
		ResidualJetEnclosure2D box = search.enclose(lower, upper);
		if (box == null) {
			return search.invalid(lower, upper);
		}
		if (box.sign(1) == ResidualJetEnclosure2D.UNDECIDED) {
			return search.result(Form.NOT_CERTIFIED, lower, upper, null,
					ContactEvidenceClass.CONTACT_NOT_ESTABLISHED, "h' is not certified nonzero");
		}
		int start = search.pointSign(lower, 0);
		int end = search.pointSign(upper, 0);
		if (start == INVALID || end == INVALID) {
			return search.invalid(lower, upper);
		}
		if (start != 0 && start == end) {
			return search.result(Form.NOT_CERTIFIED, lower, upper, null,
					ContactEvidenceClass.CONTACT_NOT_ESTABLISHED, "the ends have one sign");
		}
		if (start == 0 || end == 0) {
			return search.result(Form.NOT_CERTIFIED, lower, upper, null, null,
					"an end sign is undecided");
		}
		double[] bracket = {lower, upper};
		return search.located(lower, upper, List.of(bracket), List.of(bracket), null, null,
				"h' excludes zero and the ends have opposite signs");
	}

	/** @return the form */
	Form getForm() {
		return form;
	}

	/** @return whether a number of roots is certified */
	boolean isCertified() {
		return form != Form.NOT_CERTIFIED;
	}

	/** @return lower end of the box */
	double getLower() {
		return lower;
	}

	/** @return upper end of the box */
	double getUpper() {
		return upper;
	}

	/** @return the pieces of the box that contain exactly one simple root each */
	List<double[]> getPieces() {
		return pieces;
	}

	/** @return the narrowed bracket of the root of each piece, in the same order */
	List<double[]> getRoots() {
		return roots;
	}

	/** @return the bracket of the certified critical point, or {@code null} */
	double[] getCritical() {
		return critical;
	}

	/**
	 * @return the contact class of the certified critical point, or {@code null} when the box
	 *         has none
	 */
	ContactEvidenceClass getContact() {
		return contact;
	}

	/** @return box enclosures used */
	int getEvaluations() {
		return evaluations;
	}

	/** @return why the certificate has this form */
	String getReason() {
		return reason;
	}

	/** @return a stable description */
	String describe() {
		StringBuilder text = new StringBuilder(form.name()).append(" on [").append(lower)
				.append(',').append(upper).append(']');
		if (critical != null) {
			text.append(" critical [").append(critical[0]).append(',').append(critical[1])
					.append(']');
		}
		for (double[] root : roots) {
			text.append(" root [").append(root[0]).append(',').append(root[1]).append(']');
		}
		return text.append(" (").append(reason).append(", ").append(evaluations)
				.append(" enclosures)").toString();
	}

	@Override
	public String toString() {
		return describe();
	}

	/** One certificate search, with its work count. */
	private static final class Search {
		private final ResidualJetSource2D source;
		private int evaluations;

		Search(ResidualJetSource2D source) {
			this.source = source;
		}

		RootCountCertificate2D run(double a, double b) {
			int second = fixedSign(a, b, 2);
			if (second == INVALID) {
				return invalid(a, b);
			}
			if (second != 0) {
				int startSlope = pointSign(a, 1);
				int endSlope = pointSign(b, 1);
				if (startSlope == INVALID || endSlope == INVALID) {
					return invalid(a, b);
				}
				if (startSlope != 0 && endSlope != 0) {
					return startSlope == endSlope
							? monotone(a, b, "h' keeps the sign of its ends since h'' has a"
									+ " fixed sign")
							: critical(a, b, second, startSlope);
				}
			}
			int first = fixedSign(a, b, 1);
			if (first == INVALID) {
				return invalid(a, b);
			}
			if (first != 0) {
				return monotone(a, b, "h' has a fixed sign");
			}
			int value = fixedSign(a, b, 0);
			if (value == INVALID) {
				return invalid(a, b);
			}
			if (value != 0) {
				return result(Form.NONE, a, b, null, null, "h has a fixed sign");
			}
			return result(Form.NOT_CERTIFIED, a, b, null,
					ContactEvidenceClass.CONTACT_NOT_ESTABLISHED,
					"no fixed sign of h, h' or h''");
		}

		/** The residual is strictly monotone on {@code [a, b]}. */
		private RootCountCertificate2D monotone(double a, double b, String why) {
			int start = pointSign(a, 0);
			int end = pointSign(b, 0);
			if (start == INVALID || end == INVALID) {
				return invalid(a, b);
			}
			if (start == 0 || end == 0) {
				return result(Form.NOT_CERTIFIED, a, b, null, null,
						why + "; an end sign is undecided");
			}
			if (start == end) {
				return result(Form.NONE, a, b, null, null, why + "; the ends have one sign");
			}
			double[] root = narrow(a, start, b);
			if (root == null) {
				return invalid(a, b);
			}
			return located(a, b, List.of(new double[] {a, b}), List.of(root), null, null,
					why + "; the ends have opposite signs");
		}

		/**
		 * {@code h''} has the sign {@code curvature} on {@code [a, b]} and {@code h'} changes
		 * sign once, from {@code slope} at {@code a}.
		 */
		private RootCountCertificate2D critical(double a, double b, int curvature, int slope) {
			double low = a;
			double high = b;
			for (int step = 0; step < CertifiedAdmissionPolicy2D.MAXIMUM_BISECTIONS
					&& high - low > CertifiedAdmissionPolicy2D.bracketWidth(low); step++) {
				double middle = low + (high - low) / 2;
				if (!(low < middle && middle < high)) {
					break;
				}
				int sign = pointSign(middle, 1);
				if (sign == INVALID) {
					return invalid(a, b);
				}
				if (sign == 0) {
					break;
				}
				if (sign == slope) {
					low = middle;
				} else {
					high = middle;
				}
			}
			double[] bracket = {low, high};
			int value = fixedSign(low, high, 0);
			if (value == INVALID) {
				return invalid(a, b);
			}
			if (value == 0) {
				return result(Form.NOT_CERTIFIED, a, b, bracket,
						ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE,
						"the residual at the certified critical point contains zero");
			}
			if (value == curvature) {
				return result(Form.NONE, a, b, bracket,
						ContactEvidenceClass.CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION,
						"the critical value has the sign of h''");
			}
			int start = pointSign(a, 0);
			int end = pointSign(b, 0);
			if (start == INVALID || end == INVALID) {
				return invalid(a, b);
			}
			if (start == 0 || end == 0) {
				return result(Form.NOT_CERTIFIED, a, b, bracket, null,
						"the critical value has the sign opposite to h''; an end sign is"
								+ " undecided");
			}
			List<double[]> pieces = new ArrayList<>();
			List<double[]> roots = new ArrayList<>();
			if (start == curvature) {
				double[] root = narrow(a, start, low);
				if (root == null) {
					return invalid(a, b);
				}
				pieces.add(new double[] {a, low});
				roots.add(root);
			}
			if (end == curvature) {
				double[] root = narrow(high, -curvature, b);
				if (root == null) {
					return invalid(a, b);
				}
				pieces.add(new double[] {high, b});
				roots.add(root);
			}
			ContactEvidenceClass contact = roots.size() == 2
					? ContactEvidenceClass.CRITICAL_POINT_CERTIFIED_TWO_ROOTS : null;
			String why = "the critical value has the sign opposite to h''";
			return roots.isEmpty() ? result(Form.NONE, a, b, bracket, contact,
					why + "; the ends have that sign too")
					: located(a, b, pieces, roots, bracket, contact, why);
		}

		/**
		 * Bisection of a strictly monotone residual between ends of opposite certified signs.
		 *
		 * @return the narrowed root bracket, or {@code null} when the source is not valid
		 */
		private double[] narrow(double a, int startSign, double b) {
			double low = a;
			double high = b;
			for (int step = 0; step < CertifiedAdmissionPolicy2D.MAXIMUM_BISECTIONS
					&& high - low > CertifiedAdmissionPolicy2D.bracketWidth(low); step++) {
				double middle = low + (high - low) / 2;
				if (!(low < middle && middle < high)) {
					break;
				}
				int sign = pointSign(middle, 0);
				if (sign == INVALID) {
					return null;
				}
				if (sign == 0) {
					break;
				}
				if (sign == startSign) {
					low = middle;
				} else {
					high = middle;
				}
			}
			return new double[] {low, high};
		}

		/**
		 * @return the certified sign of the jet of the order on the whole box, by bisection;
		 *         {@code 0} when it is undecided or not fixed; {@link #INVALID} when the
		 *         source is not valid on the box
		 */
		private int fixedSign(double a, double b, int order) {
			Deque<double[]> boxes = new ArrayDeque<>();
			boxes.push(new double[] {a, b, 0});
			int sign = 0;
			while (!boxes.isEmpty()) {
				double[] box = boxes.pop();
				ResidualJetEnclosure2D jet = enclose(box[0], box[1]);
				if (jet == null) {
					return INVALID;
				}
				int current = jet.sign(order);
				if (current != ResidualJetEnclosure2D.UNDECIDED) {
					if (sign == 0) {
						sign = current;
					} else if (current != sign) {
						return 0;
					}
					continue;
				}
				double middle = box[0] + (box[1] - box[0]) / 2;
				if (box[2] >= CertifiedAdmissionPolicy2D.MAXIMUM_DEPTH
						|| !(box[0] < middle && middle < box[1])) {
					return 0;
				}
				boxes.push(new double[] {middle, box[1], box[2] + 1});
				boxes.push(new double[] {box[0], middle, box[2] + 1});
			}
			return sign;
		}

		private int pointSign(double parameter, int order) {
			ResidualJetEnclosure2D jet = enclose(parameter, parameter);
			return jet == null ? INVALID : jet.sign(order);
		}

		private ResidualJetEnclosure2D enclose(double a, double b) {
			if (++evaluations > CertifiedAdmissionPolicy2D.MAXIMUM_EVALUATIONS) {
				throw new Exhausted();
			}
			return source.enclose(a, b);
		}

		private RootCountCertificate2D invalid(double a, double b) {
			return result(Form.NOT_CERTIFIED, a, b, null, null,
					"the source is not certified valid on the box");
		}

		private RootCountCertificate2D located(double a, double b, List<double[]> pieces,
				List<double[]> roots, double[] critical, ContactEvidenceClass contact,
				String reason) {
			return new RootCountCertificate2D(roots.size() == 1 ? Form.ONE_SIMPLE
					: Form.TWO_SIMPLE, a, b, pieces, roots, critical, contact, evaluations,
					reason);
		}

		RootCountCertificate2D result(Form form, double a, double b, double[] critical,
				ContactEvidenceClass contact, String reason) {
			return new RootCountCertificate2D(form, a, b, Collections.emptyList(),
					Collections.emptyList(), critical, contact, evaluations, reason);
		}
	}

	/** The work limit of one certificate is reached. */
	private static final class Exhausted extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Exhausted() {
			super(null, null, false, false);
		}
	}
}
