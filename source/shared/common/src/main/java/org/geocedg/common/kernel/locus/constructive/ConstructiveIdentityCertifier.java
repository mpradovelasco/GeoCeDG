/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Failure;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Line;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Cause;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.ContactOrder;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Status;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * Attempts and rechecks constructive identity certificates
 * ({@code constructive-identity-certificate/v1}, {@code locus-v2-constructive-authority.md}
 * section 8) of an incidence of a point with a line, segment, ray, circle or point of a
 * constructive cone. The cone is evaluated by the same evaluator as the exact evaluation M3, over
 * exact jets of order two in the field of the certificate, so both decide the same predicates with
 * the same inherited selection, and the order of contact comes from the same evaluation:
 *
 * <ul>
 * <li>the boundary rule is automatic: free points and numbers are indeterminates; the axes are
 * constants; a point on a constructible line path is one indeterminate, the coordinate along the
 * larger coefficient of the live line, with the other solved from the exact line; a point on a
 * constructible circle path keeps the inherited path map; a rotation, any other boundary point
 * and a point on a path that depends on objects outside the class are cut as free points of the
 * plane, which forgets relations and never creates them; a boundary number is an
 * indeterminate;</li>
 * <li>the decision box contains the ideal value of every indeterminate: the exact binary64 value
 * of a free input, which is its definition, and for a point on a path or a cut the hull of its
 * live value and of the rigorous enclosure of its ideal value; without such an enclosure the
 * interval of a cut is the whole real line, so that every condition that reads it is undecided
 * (rule P2);</li>
 * <li>every sign is certified on that box and recorded: the cell; the live output of every
 * line-circle intersection outside the slice of the substituted driver is certified strictly
 * closer, on the whole box, to the selected branch than to the other;</li>
 * <li>the residual of the incidence is decided as an element of the field: the zero element is an
 * identity; any other is {@code NOT_AN_IDENTITY}, never a contradiction; the membership of the
 * limited part of a segment or ray is a certified sign, whose certified failure is a
 * contradiction with that limited target;</li>
 * <li>with a substituted driver on a circle or a segment, the driver is the exact jet of its path
 * at the structural point, and the order of contact follows from the jets of the traced point:
 * stationary when its first derivative is the zero element, otherwise transverse when the first
 * derivative of the residual has a certified sign, tangent when it is the zero element and the
 * second has a certified sign; the signs enter the cell.</li>
 * </ul>
 *
 * <p>A certificate applies at another revision only when its structural key is equal, every sign
 * of its cell is certified with the same value on the new decision box and every checked branch
 * still corresponds to the live algorithm (section 8.2); the recheck does no symbolic work.</p>
 */
public final class ConstructiveIdentityCertifier {

	/** Versioned name of the structural key. */
	public static final String KEY_VERSION = "cic-structural-key/v1";

	private static final OutwardInterval UNBOUNDED = OutwardInterval.of(Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY);

	private ConstructiveIdentityCertifier() {
	}

	/**
	 * Attempts the certificate of {@code point} on {@code target} with the automatic boundary
	 * rule of section 8.
	 *
	 * @param relation the name of the relation, in a report only
	 * @param cone the cone of the relation
	 * @param point node of the point
	 * @param target node of a line, segment, ray, circle or point
	 * @param support node of the path of the substituted driver, for the order of contact, or
	 *        {@code -1}
	 * @param budget the budget of the attempt
	 * @return the certificate
	 */
	public static ConstructiveIdentityCertificate incidence(String relation, ConstructiveCone cone,
			int point, int target, int support, ConstructiveIdentityBudget budget) {
		return incidence(relation, cone, point, target, support,
				ConstructiveIdentityCertificate.Scope.AUTOMATIC, budget);
	}

	/**
	 * Attempts the certificate of {@code point} on {@code target} in a scope.
	 *
	 * @param relation the name of the relation, in a report only
	 * @param cone the cone of the relation
	 * @param point node of the point
	 * @param target node of a line, segment, ray, circle or point
	 * @param support node of the path of the substituted driver, for the order of contact, or
	 *        {@code -1}
	 * @param scope which inputs are indeterminates
	 * @param budget the budget of the attempt
	 * @return the certificate
	 */
	public static ConstructiveIdentityCertificate incidence(String relation, ConstructiveCone cone,
			int point, int target, int support, ConstructiveIdentityCertificate.Scope scope,
			ConstructiveIdentityBudget budget) {
		return incidence(relation, cone, point, target, support, scope, budget, 0);
	}

	/**
	 * The attempt with a decision box widened by a half-width on every indeterminate, an
	 * adversarial injection of the validation (control C4): a wider box still contains the
	 * ideal values, so it can only lose certificates, never create one.
	 */
	static ConstructiveIdentityCertificate incidence(String relation, ConstructiveCone cone,
			int point, int target, int support, ConstructiveIdentityCertificate.Scope scope,
			ConstructiveIdentityBudget budget, double widening) {
		return attempt(relation, incidenceText(point, target), scope, cone,
				new int[] {point, target}, support, null, budget, widening,
				(certificate, jets, values, driven) -> decide(certificate, jets, values[0],
						values[1], driven));
	}

	/**
	 * Attempts the certificate of {@code point} on {@code target} with the driver of the slice
	 * at an exact parameter of its path, a circle read in the frame of the axes, instead of at
	 * the point that replaces it (PRE-G9B-CU3 increment E3: the form one double root at an exact
	 * point of the conflict rule). The driver is the exact jet of order two of the circle at
	 * the parameter, {@code m + r (cos t0 e0 + sin t0 e1)} with {@code r} the nonnegative root of
	 * the exact squared radius, so the order of contact follows as for a structural point; the
	 * replacement node of the cone is neither evaluated nor read, so its persisted position never
	 * enters the key.
	 *
	 * @param relation the name of the relation, in a report only
	 * @param cone a cone with the slice of the locus, its driver as the replacement node
	 * @param point node of the traced point in the slice
	 * @param target node of a line, segment, ray or circle
	 * @param support node of the circle of the driver
	 * @param quarter the parameter {@code t0 = quarter pi / 2}, from {@code 0} to {@code 3}
	 * @param frame the frame {@code {e0x, e0y, e1x, e1y}} of the captured driver, of the axes
	 * @param budget the budget of the attempt
	 * @return the certificate
	 */
	public static ConstructiveIdentityCertificate incidenceAtParameter(String relation,
			ConstructiveCone cone, int point, int target, int support, int quarter,
			double[] frame, ConstructiveIdentityBudget budget) {
		DriverAt at = new DriverAt(quarter, frame);
		return attempt(relation, incidenceText(point, target) + at.describe(),
				ConstructiveIdentityCertificate.Scope.AUTOMATIC, cone, new int[] {point, target},
				support, at, budget, 0, (certificate, jets, values, driven) -> decide(certificate,
						jets, values[0], values[1], driven));
	}

	/**
	 * A premise of the structure of a fold ({@code fold-symmetry/v1}) for every value of the
	 * substitutes: the line of a node is exactly horizontal, its coefficient of {@code x} the
	 * zero element, or vertical, its coefficient of {@code y} the zero element, with the other
	 * coefficient certified nonzero on the box.
	 *
	 * @param relation the name of the premise, in a report only
	 * @param cone the cone of the line
	 * @param line node of the line
	 * @param horizontal whether the line must be horizontal rather than vertical
	 * @param budget the budget of the attempt
	 * @return the certificate
	 */
	public static ConstructiveIdentityCertificate axisParallel(String relation,
			ConstructiveCone cone, int line, boolean horizontal,
			ConstructiveIdentityBudget budget) {
		return attempt(relation, (horizontal ? "horizontal n" : "vertical n") + line,
				ConstructiveIdentityCertificate.Scope.AUTOMATIC, cone, new int[] {line}, -1, null,
				budget, 0, (certificate, jets, values, driven) -> {
					if (!(values[0] instanceof Line)) {
						return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS,
								"not a line");
					}
					@SuppressWarnings("unchecked")
					Line<CertificateJet> value = (Line<CertificateJet>) values[0];
					CertificateElement zero = (horizontal ? value.getA() : value.getB())
							.getValue();
					CertificateElement other = (horizontal ? value.getB() : value.getA())
							.getValue();
					return vanishes(certificate, jets, zero, other);
				});
	}

	/**
	 * A premise of the structure of a fold ({@code fold-symmetry/v1}) for every value of the
	 * substitutes: one coordinate of the point of a node is that of the centre of the circle of
	 * another node.
	 *
	 * @param relation the name of the premise, in a report only
	 * @param cone the cone of both objects
	 * @param point node of the point
	 * @param circle node of the circle
	 * @param ordinate whether the ordinates must be equal rather than the abscissas
	 * @param budget the budget of the attempt
	 * @return the certificate
	 */
	public static ConstructiveIdentityCertificate sameCoordinate(String relation,
			ConstructiveCone cone, int point, int circle, boolean ordinate,
			ConstructiveIdentityBudget budget) {
		return attempt(relation, (ordinate ? "same ordinate n" : "same abscissa n") + point
				+ " n" + circle, ConstructiveIdentityCertificate.Scope.AUTOMATIC, cone,
				new int[] {point, circle}, -1, null, budget, 0,
				(certificate, jets, values, driven) -> {
					if (!(values[0] instanceof Point) || !(values[1] instanceof Circle)) {
						return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS,
								"not a point and a circle");
					}
					@SuppressWarnings("unchecked")
					Point<CertificateJet> centre = (Point<CertificateJet>) values[0];
					@SuppressWarnings("unchecked")
					Circle<CertificateJet> driver = (Circle<CertificateJet>) values[1];
					ConstructiveIdentityArithmetic field = jets.getField();
					CertificateElement offset = ordinate
							? field.subtract(centre.getY().getValue(),
									driver.getCenterY().getValue())
							: field.subtract(centre.getX().getValue(),
									driver.getCenterX().getValue());
					return vanishes(certificate, jets, offset, null);
				});
	}

	/** The decision of one relation on the values of its nodes. */
	private interface Decider {
		ConstructiveIdentityCertificate decide(ConstructiveIdentityCertificate.Builder certificate,
				ConstructiveIdentityJetArithmetic jets, Object[] values, boolean driven);
	}

	private static ConstructiveIdentityCertificate vanishes(
			ConstructiveIdentityCertificate.Builder certificate,
			ConstructiveIdentityJetArithmetic jets, CertificateElement zero,
			CertificateElement nonzero) {
		if (!zero.isZero()) {
			return certificate.outcome(Status.NOT_AN_IDENTITY, Cause.RESIDUAL_NOT_ZERO,
					"the residual is not the zero element of the field");
		}
		if (nonzero != null) {
			int sign = jets.getField().sign(nonzero);
			if (sign != 1 && sign != -1) {
				return certificate.outcome(Status.NOT_ESTABLISHED, Cause.PRECONDITION_UNDECIDED,
						"the other coefficient is not certified nonzero");
			}
		}
		return certificate.certified();
	}

	private static ConstructiveIdentityCertificate attempt(String relation, String text,
			ConstructiveIdentityCertificate.Scope scope, ConstructiveCone cone, int[] nodes,
			int support, DriverAt at, ConstructiveIdentityBudget budget, double widening,
			Decider decider) {
		int[] roots = new int[nodes.length + 1];
		System.arraycopy(nodes, 0, roots, 0, nodes.length);
		roots[nodes.length] = support;
		boolean readsReplacement = at == null;
		ConstructiveIdentityCertificate.Builder certificate = ConstructiveIdentityCertificate
				.builder(relation, scope).key(key(cone, text, roots, scope, budget,
						readsReplacement));
		ConstructiveIdentityArithmetic field = new ConstructiveIdentityArithmetic(
				new ConstructiveIdentityCounters(budget));
		ConstructiveIdentityJetArithmetic jets = new ConstructiveIdentityJetArithmetic(field);
		certificate.evaluation(field);
		try {
			Boundary boundary = Boundary.of(cone, jets, certificate, scope, widening,
					readsReplacement, roots);
			ConstructiveConeEvaluator<CertificateJet> evaluator = new ConstructiveConeEvaluator<>(
					cone, jets, true, Collections.emptyMap(), boundary.fixed, boundary);
			boolean driven = false;
			if (at != null) {
				// The driver at the exact parameter: the slice reads the jet of the circle there
				// in place of the replacement, which is never evaluated.
				Result path = evaluator.evaluate(support);
				if (!path.hasValue()) {
					return failed(certificate, evaluator, path);
				}
				Point<CertificateJet> driver = at.jet(jets, path.getValue());
				if (driver == null) {
					return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS,
							"the path of the driver is not a circle");
				}
				correspondences(certificate, evaluator);
				Map<Integer, Object> fixed = new HashMap<>(boundary.fixed);
				fixed.put(cone.getReplacementNode(), driver);
				evaluator = new ConstructiveConeEvaluator<>(cone, jets, true,
						Collections.emptyMap(), fixed, boundary);
				driven = true;
			} else if (cone.hasDriverSubstitution() && support >= 0) {
				Result path = evaluator.evaluate(support);
				Result replacement = evaluator.evaluate(cone.getReplacementNode());
				for (Result result : new Result[] {path, replacement}) {
					if (!result.hasValue()) {
						return failed(certificate, evaluator, result);
					}
				}
				Point<CertificateJet> driver = driverJet(jets, path.getValue(),
						replacement.getValue());
				if (driver != null) {
					evaluator.setSliceDriver(driver);
					driven = true;
				}
			}
			Object[] values = new Object[nodes.length];
			for (int index = 0; index < nodes.length; index++) {
				Result result = evaluator.evaluate(nodes[index]);
				if (!result.hasValue()) {
					return failed(certificate, evaluator, result);
				}
				values[index] = result.getValue();
			}
			correspondences(certificate, evaluator);
			return decider.decide(certificate, jets, values, driven);
		} catch (ConstructiveIdentityException stopped) {
			return certificate.outcome(Status.NOT_ESTABLISHED, stopped.getTypedCause(),
					stopped.getMessage());
		} catch (ConstructiveIdentityBudgetExhaustedException exhausted) {
			return certificate.outcome(Status.NOT_ESTABLISHED,
					exhausted.getLimit() == ConstructiveIdentityBudgetExhaustedException.Limit
							.EXPONENT_RANGE ? Cause.EXPONENT_RANGE_EXCEEDED
									: Cause.BUDGET_EXHAUSTED, exhausted.getMessage());
		} catch (ArithmeticException undecided) {
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.PRECONDITION_UNDECIDED,
					undecided.getMessage());
		}
	}

	/**
	 * Rechecks a certified identity at another revision (section 8.2), without symbolic work: it
	 * still applies only when the structural key is equal, every sign of its cell is certified
	 * with the same value on the decision box of the revision, built as in an attempt, and every
	 * checked line-circle output is still certified closer to its selected branch than to the
	 * other.
	 *
	 * @param certificate a certificate of an earlier revision
	 * @param cone the cone of the same relation at this revision
	 * @param point node of the point
	 * @param target node of the target
	 * @param support node of the path of the substituted driver, or {@code -1}
	 * @param budget the budget of an attempt
	 * @return whether the certificate applies at this revision
	 */
	public static boolean recheck(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone, int point, int target, int support,
			ConstructiveIdentityBudget budget) {
		return certificate.isIdentityCertified() && key(cone, incidenceText(point, target),
				new int[] {point, target, support}, certificate.getScope(), budget, true)
						.equals(certificate.getKey()) && recheckCell(certificate, cone, budget);
	}

	/**
	 * Rechecks at another revision a certified identity with the driver at an exact parameter
	 * of its circle ({@link #incidenceAtParameter}), as {@link #recheck} does.
	 *
	 * @param certificate a certificate of an earlier revision
	 * @param cone the cone of the same relation at this revision
	 * @param point node of the traced point in the slice
	 * @param target node of the target
	 * @param support node of the circle of the driver
	 * @param quarter the parameter {@code t0 = quarter pi / 2}
	 * @param frame the frame of the captured driver
	 * @param budget the budget of an attempt
	 * @return whether the certificate applies at this revision
	 */
	public static boolean recheckAtParameter(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone, int point, int target, int support, int quarter,
			double[] frame, ConstructiveIdentityBudget budget) {
		return certificate.isIdentityCertified() && key(cone, incidenceText(point, target)
				+ new DriverAt(quarter, frame).describe(), new int[] {point, target, support},
				certificate.getScope(), budget, false).equals(certificate.getKey())
				&& recheckCell(certificate, cone, budget);
	}

	private static boolean recheckCell(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone, ConstructiveIdentityBudget budget) {
		return recheckCell(certificate, cone, budget, true);
	}

	/**
	 * Whether every sign of the cell is certified with the same value on the decision box of
	 * the cone, built as in an attempt, and every checked line-circle output is still certified
	 * closer to its selected branch than to the other. Accelerated, every sign is first enclosed
	 * through the recheck memory of the certificate, on the same box; when that enclosure does
	 * not certify the sign, or cannot be built, the enclosure of the procedure without the memory
	 * decides it, so that every sign the procedure without the memory certifies stays certified.
	 * Without acceleration only that procedure runs: for the tests that compare both. The memory,
	 * the margin preparations it triggers and that procedure share the one budget of the recheck
	 * ({@code locus-v2-constructive-authority.md} section 10, author decision D1-A).
	 */
	static boolean recheckCell(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone, ConstructiveIdentityBudget budget, boolean accelerated) {
		return recheckCell(certificate, cone, new ConstructiveIdentityCounters(budget),
				accelerated);
	}

	/**
	 * @param counters the counters of the recheck, on its one budget: for the tests that read
	 *        them
	 * @return whether the certificate applies, as {@link #recheckCell(
	 *         ConstructiveIdentityCertificate, ConstructiveCone, ConstructiveIdentityBudget,
	 *         boolean)}
	 */
	static boolean recheckCell(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone, ConstructiveIdentityCounters counters, boolean accelerated) {
		ConstructiveIdentityArithmetic field = certificate.getArithmetic().forRecheck(counters);
		ConstructiveIdentityRecheckMemory memory = accelerated ? certificate.getRecheckMemory()
				: null;
		ConstructiveIdentityCounters memoryCounters = counters;
		try {
			ConstructiveConeEvaluator<OutwardInterval> ideal =
					ConstructiveConeEvaluator.intervals(cone);
			List<ConstructiveIdentityCertificate.Indeterminate> indeterminates =
					certificate.getIndeterminates();
			OutwardInterval[] box = new OutwardInterval[indeterminates.size()];
			for (int index = 0; index < box.length; index++) {
				box[index] = interval(cone, ideal, indeterminates.get(index));
				if (box[index] == null) {
					return false;
				}
			}
			List<ConstructiveIdentityArithmetic.Condition> cell = certificate.getCell();
			for (int index = 0; index < cell.size(); index++) {
				ConstructiveIdentityArithmetic.Condition condition = cell.get(index);
				if (memory != null && certifies(memory.condition(certificate, index, box,
						memoryCounters), condition.getSign())) {
					continue;
				}
				if (!certifies(field.enclose(condition.getElement(), box), condition.getSign())) {
					return false;
				}
			}
			List<ConstructiveIdentityCertificate.Correspondence> correspondences =
					certificate.getCorrespondences();
			for (int index = 0; index < correspondences.size(); index++) {
				ConstructiveIdentityCertificate.Correspondence checked = correspondences.get(index);
				ExactRational2D[] live = exactPoint(cone.getNode(checked.getNode()));
				if (live == null) {
					return false;
				}
				if (memory != null && certifies(memory.margin(certificate, index, live, box,
						memoryCounters), 1)) {
					continue;
				}
				if (!certifies(field.enclose(margin(field, checked, live), box), 1)) {
					return false;
				}
			}
			return true;
		} catch (ConstructiveIdentityException | ConstructiveIdentityBudgetExhaustedException
				| ArithmeticException failed) {
			return false;
		}
	}

	/**
	 * The structural key {@code cic-structural-key/v1} of the relation (section 8.1): the
	 * structural description of the cone, with durable identities, structural definitions,
	 * classification and inherited selection and label states; the relation and the scope; the
	 * exact values that the proof takes as constants among the nodes it reads, the axes, the
	 * persisted coordinates of the points that keep the inherited path map and, in the boundary
	 * scope, the free points and numbers; and the versions. The values of the indeterminates
	 * never enter, and neither do the values above a cut, which the proof never reads.
	 *
	 * @return {@code cic-structural-key/v1:} followed by the SHA-256 of the key text
	 */
	static String key(ConstructiveCone cone, String relation, int[] roots,
			ConstructiveIdentityCertificate.Scope scope, ConstructiveIdentityBudget budget) {
		return key(cone, relation, roots, scope, budget, true);
	}

	/**
	 * The structural key, with or without the replacement of a substituted driver among the
	 * nodes the proof reads: without it when the driver stands at an exact parameter.
	 */
	private static String key(ConstructiveCone cone, String relation, int[] roots,
			ConstructiveIdentityCertificate.Scope scope, ConstructiveIdentityBudget budget,
			boolean replacement) {
		StringBuilder text = new StringBuilder(KEY_VERSION).append('\n')
				.append(AuthorityPolicy.CONSTRUCTIVE_AUTHORITY).append('\n')
				.append(ConstructiveClassTable.VERSION).append('\n')
				.append(AuthorityPolicy.RELAXED_ADMISSION).append('\n')
				.append(ConstructiveIdentityCertificate.VERSION).append('\n')
				.append(budget).append('\n')
				.append(scope).append('\n')
				.append(relation).append('\n')
				.append(cone.describe());
		boolean[] read = reads(cone, replacement, roots);
		for (ConstructiveConeNode node : cone.getNodes()) {
			ConstructiveOperation operation = node.getOperation();
			if (read[node.getIndex()] && (operation == ConstructiveOperation.AXIS
					|| operation == ConstructiveOperation.PATH_POINT
							&& onConstructibleCircle(cone, node)
					|| scope == ConstructiveIdentityCertificate.Scope.BOUNDARY
							&& (operation == ConstructiveOperation.FREE_POINT
									|| operation == ConstructiveOperation.FREE_NUMBER))) {
				text.append("constant n").append(node.getIndex());
				for (double value : node.getLiveState()) {
					text.append(' ').append(Long.toHexString(Double.doubleToLongBits(value)));
				}
				text.append('\n');
			}
		}
		return KEY_VERSION + ":" + sha256(text.toString());
	}

	@SuppressWarnings("unchecked")
	private static ConstructiveIdentityCertificate decide(
			ConstructiveIdentityCertificate.Builder certificate,
			ConstructiveIdentityJetArithmetic jets, Object subject, Object object,
			boolean driven) {
		ConstructiveIdentityArithmetic field = jets.getField();
		if (!(subject instanceof Point)) {
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS,
					"the subject is not a point");
		}
		Point<CertificateJet> p = (Point<CertificateJet>) subject;
		CertificateElement x = p.getX().getValue();
		CertificateElement y = p.getY().getValue();
		Line<CertificateJet> limited = null;
		CertificateElement residual;
		CertificateElement other = null;
		if (object instanceof Line) {
			Line<CertificateJet> line = (Line<CertificateJet>) object;
			residual = field.add(field.add(field.multiply(line.getA().getValue(), x),
					field.multiply(line.getB().getValue(), y)), line.getC().getValue());
			if (line.isLimited() && !line.hasOutlyingIntersections()) {
				limited = line;
			}
		} else if (object instanceof Circle) {
			Circle<CertificateJet> circle = (Circle<CertificateJet>) object;
			CertificateElement dx = field.subtract(x, circle.getCenterX().getValue());
			CertificateElement dy = field.subtract(y, circle.getCenterY().getValue());
			residual = field.subtract(field.add(field.multiply(dx, dx), field.multiply(dy, dy)),
					circle.getRadiusSquared().getValue());
		} else if (object instanceof Point) {
			Point<CertificateJet> q = (Point<CertificateJet>) object;
			residual = field.subtract(x, q.getX().getValue());
			other = field.subtract(y, q.getY().getValue());
		} else {
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS,
					"the target is not a line, segment, ray, circle or point");
		}
		if (!residual.isZero() || other != null && !other.isZero()) {
			return certificate.outcome(Status.NOT_AN_IDENTITY, Cause.RESIDUAL_NOT_ZERO,
					"the residual is not the zero element of the field");
		}
		if (limited != null) {
			CertificateElement parameter = field.subtract(
					field.multiply(field.subtract(x, limited.getStartX().getValue()),
							limited.getB().getValue()),
					field.multiply(field.subtract(y, limited.getStartY().getValue()),
							limited.getA().getValue()));
			int start = field.sign(parameter);
			int end = limited.isRay() ? 1 : field.sign(field.subtract(field.add(
					field.multiply(limited.getA().getValue(), limited.getA().getValue()),
					field.multiply(limited.getB().getValue(), limited.getB().getValue())),
					parameter));
			if (start == ConstructiveArithmetic.UNDECIDED
					|| end == ConstructiveArithmetic.UNDECIDED) {
				return certificate.outcome(Status.NOT_ESTABLISHED, Cause.PRECONDITION_UNDECIDED,
						"the membership of the limited part is not certified");
			}
			if (start < 0 || end < 0) {
				return certificate.outcome(Status.CONTRADICTED, Cause.NOT_ON_LIMITED_TARGET,
						"the point is certified outside the limited part of the target");
			}
		}
		if (driven && !(object instanceof Point)) {
			order(certificate, jets, p, object);
		}
		return certificate.certified();
	}

	/**
	 * The order of contact at the structural point, from the exact jets of the traced point and
	 * the residual of the target, which does not move with the driver, with the signs of the
	 * first two derivatives of the residual along the trace; every certified sign enters the
	 * cell.
	 */
	@SuppressWarnings("unchecked")
	private static void order(ConstructiveIdentityCertificate.Builder certificate,
			ConstructiveIdentityJetArithmetic jets, Point<CertificateJet> p, Object target) {
		ConstructiveIdentityArithmetic field = jets.getField();
		CertificateElement zero = field.ofBinary64(0);
		CertificateElement dx = orZero(p.getX().getFirst(), zero);
		CertificateElement dy = orZero(p.getY().getFirst(), zero);
		CertificateElement ddx = orZero(p.getX().getSecond(), zero);
		CertificateElement ddy = orZero(p.getY().getSecond(), zero);
		CertificateElement speed = field.add(field.multiply(dx, dx), field.multiply(dy, dy));
		CertificateElement first;
		CertificateElement second;
		if (target instanceof Line) {
			Line<CertificateJet> line = (Line<CertificateJet>) target;
			CertificateElement a = line.getA().getValue();
			CertificateElement b = line.getB().getValue();
			first = field.add(field.multiply(a, dx), field.multiply(b, dy));
			second = field.add(field.multiply(a, ddx), field.multiply(b, ddy));
		} else {
			Circle<CertificateJet> circle = (Circle<CertificateJet>) target;
			CertificateElement ox = field.subtract(p.getX().getValue(),
					circle.getCenterX().getValue());
			CertificateElement oy = field.subtract(p.getY().getValue(),
					circle.getCenterY().getValue());
			CertificateElement two = field.ofBinary64(2);
			first = field.multiply(two, field.add(field.multiply(ox, dx),
					field.multiply(oy, dy)));
			second = field.multiply(two, field.add(field.add(field.multiply(ox, ddx),
					field.multiply(oy, ddy)), speed));
		}
		int firstSign = field.sign(first);
		if (speed.isZero()) {
			// A stationary point: the first derivative is the zero element, and the second says
			// on which side of the target the trace stays.
			certificate.contactOrder(ContactOrder.STATIONARY, firstSign, field.sign(second));
			return;
		}
		if (field.sign(speed) != 1) {
			certificate.contactOrder(ContactOrder.NOT_ESTABLISHED, firstSign,
					ConstructiveArithmetic.UNDECIDED);
			return;
		}
		if (firstSign != 0) {
			certificate.contactOrder(firstSign == ConstructiveArithmetic.UNDECIDED
					? ContactOrder.NOT_ESTABLISHED : ContactOrder.TRANSVERSE, firstSign,
					ConstructiveArithmetic.UNDECIDED);
			return;
		}
		int secondSign = field.sign(second);
		certificate.contactOrder(secondSign == 0 ? ContactOrder.HIGHER_ORDER
				: secondSign == ConstructiveArithmetic.UNDECIDED ? ContactOrder.NOT_ESTABLISHED
						: ContactOrder.TANGENT, firstSign, secondSign);
	}

	/**
	 * The exact jet of order two of the driver on its path at the structural point: on a circle,
	 * the derivatives in the angle, {@code (-(y - cy), x - cx)} and {@code (cx - x, cy - y)}; on a
	 * segment, its direction and zero. The order of contact does not depend on the orientation
	 * or the speed of the parameter.
	 *
	 * @return the jet, or {@code null} for another path
	 */
	@SuppressWarnings("unchecked")
	private static Point<CertificateJet> driverJet(ConstructiveIdentityJetArithmetic jets,
			Object path, Object replacement) {
		if (!(replacement instanceof Point)) {
			return null;
		}
		ConstructiveIdentityArithmetic field = jets.getField();
		Point<CertificateJet> driver = (Point<CertificateJet>) replacement;
		CertificateElement x = driver.getX().getValue();
		CertificateElement y = driver.getY().getValue();
		if (path instanceof Circle) {
			Circle<CertificateJet> circle = (Circle<CertificateJet>) path;
			CertificateElement ox = field.subtract(x, circle.getCenterX().getValue());
			CertificateElement oy = field.subtract(y, circle.getCenterY().getValue());
			return new Point<>(new CertificateJet(x, field.negate(oy), field.negate(ox)),
					new CertificateJet(y, ox, field.negate(oy)));
		}
		if (path instanceof Line && ((Line<CertificateJet>) path).isLimited()
				&& !((Line<CertificateJet>) path).isRay()) {
			Line<CertificateJet> segment = (Line<CertificateJet>) path;
			return new Point<>(new CertificateJet(x, segment.getB().getValue(), null),
					new CertificateJet(y, field.negate(segment.getA().getValue()), null));
		}
		return null;
	}

	private static String incidenceText(int point, int target) {
		return "incidence n" + point + " on n" + target;
	}

	private static CertificateElement orZero(CertificateElement value, CertificateElement zero) {
		return value == null ? zero : value;
	}

	private static void correspondences(ConstructiveIdentityCertificate.Builder certificate,
			ConstructiveConeEvaluator<CertificateJet> evaluator) {
		for (ConstructiveConeEvaluator.LiveCorrespondence<CertificateJet> checked
				: evaluator.getCorrespondences()) {
			certificate.correspondence(new ConstructiveIdentityCertificate.Correspondence(
					checked.getNode(), values(checked.getSelected()),
					values(checked.getOther())));
		}
	}

	private static Point<CertificateElement> values(Point<CertificateJet> point) {
		return new Point<>(point.getX().getValue(), point.getY().getValue());
	}

	private static ConstructiveIdentityCertificate failed(
			ConstructiveIdentityCertificate.Builder certificate,
			ConstructiveConeEvaluator<CertificateJet> evaluator, Result result) {
		correspondences(certificate, evaluator);
		String message = String.valueOf(result);
		Failure failure = result.getFailure();
		switch (failure) {
		case INVALID:
			return certificate.outcome(Status.INVALID, Cause.OBJECT_UNDEFINED, message);
		case UNDECIDED:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.PRECONDITION_UNDECIDED,
					message);
		case SELECTION_NOT_RECONSTRUCTED:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.SELECTION_NOT_RECONSTRUCTED,
					message);
		case BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED:
			return certificate.outcome(Status.NOT_ESTABLISHED,
					Cause.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED, message);
		case LIVE_DECISION_DISAGREES:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.LIVE_DECISION_DISAGREES,
					message);
		case SUBSTITUTE_UNDEFINED:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.SUBSTITUTE_UNDEFINED,
					message);
		case PATH_MAP_UNDEFINED:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.PATH_MAP_UNDEFINED, message);
		case BUDGET_EXHAUSTED:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.BUDGET_EXHAUSTED, message);
		default:
			return certificate.outcome(Status.NOT_ESTABLISHED, Cause.OUT_OF_CLASS, message);
		}
	}

	private static boolean certifies(OutwardInterval enclosure, int sign) {
		return enclosure != null && enclosure.certifiedSign() == sign;
	}

	/**
	 * @return the margin {@code |O - P|^2 - |S - P|^2} of a checked line-circle output at its
	 *         live point {@code P}, with {@code O} the other branch and {@code S} the selected one
	 */
	static CertificateElement margin(ConstructiveIdentityArithmetic field,
			ConstructiveIdentityCertificate.Correspondence checked, ExactRational2D[] live) {
		CertificateElement x = field.ofRational(live[0]);
		CertificateElement y = field.ofRational(live[1]);
		return field.subtract(squaredDistance(field, checked.getOther(), x, y),
				squaredDistance(field, checked.getSelected(), x, y));
	}

	private static CertificateElement squaredDistance(ConstructiveIdentityArithmetic field,
			Point<CertificateElement> point, CertificateElement x, CertificateElement y) {
		CertificateElement dx = field.subtract(point.getX(), x);
		CertificateElement dy = field.subtract(point.getY(), y);
		return field.add(field.multiply(dx, dx), field.multiply(dy, dy));
	}

	private static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte value : digest) {
				hex.append(Character.forDigit((value >> 4) & 0xF, 16))
						.append(Character.forDigit(value & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException missing) {
			throw new IllegalStateException(missing);
		}
	}

	/**
	 * The interval of one indeterminate on the decision box of the cone: the exact value of a
	 * free input; the hull of the live value and of the rigorous enclosure of the ideal value of a
	 * point on a line path, or {@code null} without that enclosure; the hull for a cut, or the
	 * whole real line without the enclosure; the whole real line for a boundary number.
	 */
	static OutwardInterval interval(ConstructiveCone cone,
			ConstructiveConeEvaluator<OutwardInterval> ideal,
			ConstructiveIdentityCertificate.Indeterminate indeterminate) {
		ConstructiveConeNode node = cone.getNode(indeterminate.getNode());
		int coordinate = indeterminate.getCoordinate();
		switch (node.getOperation()) {
		case FREE_POINT: {
			ExactRational2D[] exact = exactPoint(node);
			return exact == null ? null : OutwardInterval.enclosing(exact[coordinate]);
		}
		case FREE_NUMBER: {
			double value = live(node, 0);
			return Double.isFinite(value) ? OutwardInterval.point(value) : null;
		}
		case BOUNDARY_NUMBER:
			return UNBOUNDED;
		default: {
			OutwardInterval[] enclosure = idealEnclosure(ideal, node.getIndex());
			ExactRational2D[] exact = exactPoint(node);
			boolean onLine = node.getOperation() == ConstructiveOperation.PATH_POINT
					&& !isCut(cone, node);
			if (enclosure == null || exact == null) {
				return onLine ? null : UNBOUNDED;
			}
			return enclosure[coordinate].hull(OutwardInterval.enclosing(exact[coordinate]));
		}
		}
	}

	/** Whether the node is cut: a rotation, another boundary point, or a point on a path that
	 * depends on objects outside the class. */
	static boolean isCut(ConstructiveCone cone, ConstructiveConeNode node) {
		switch (node.getOperation()) {
		case ROTATE_POINT:
		case BOUNDARY_POINT:
			return true;
		case PATH_POINT:
			return !constructible(cone, node.getInput(0))
					|| cone.getNode(node.getInput(0)).getOperation().getShape()
							!= ConstructiveOperation.Shape.LINE
							&& !onConstructibleCircle(cone, node);
		default:
			return false;
		}
	}

	/** Whether a point on a path lies on a constructible circle, keeping the path map. */
	static boolean onConstructibleCircle(ConstructiveCone cone, ConstructiveConeNode node) {
		ConstructiveConeNode path = cone.getNode(node.getInput(0));
		return path.getOperation().getShape() == ConstructiveOperation.Shape.CIRCLE
				&& constructible(cone, path.getIndex());
	}

	/** Whether the object is defined from free inputs by operations of the class only. */
	static boolean constructible(ConstructiveCone cone, int index) {
		ConstructiveConeNode node = cone.getNode(index);
		ConstructiveOperation.Membership membership = node.getOperation().getMembership();
		if (membership == ConstructiveOperation.Membership.BOUNDARY
				|| membership == ConstructiveOperation.Membership.OUTSIDE
				|| node.getOperation() == ConstructiveOperation.ROTATE_POINT) {
			return false;
		}
		for (int position = 0; position < node.getInputCount(); position++) {
			if (!constructible(cone, node.getInput(position))) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	static OutwardInterval[] idealEnclosure(ConstructiveConeEvaluator<OutwardInterval> ideal,
			int index) {
		Result result = ideal.evaluate(index);
		if (!result.hasValue() || !(result.getValue() instanceof Point)) {
			return null;
		}
		Point<OutwardInterval> point = (Point<OutwardInterval>) result.getValue();
		if (!point.getX().isFinite() || !point.getY().isFinite()) {
			return null;
		}
		return new OutwardInterval[] {point.getX(), point.getY()};
	}

	static ExactRational2D[] exactPoint(ConstructiveConeNode node) {
		double x = live(node, 0);
		double y = live(node, 1);
		double z = live(node, 2);
		if (!node.isLiveDefined() || !Double.isFinite(x) || !Double.isFinite(y)
				|| !Double.isFinite(z) || z == 0) {
			return null;
		}
		ExactRational2D denominator = ExactRational2D.ofBinary64(z);
		return new ExactRational2D[] {ExactRational2D.ofBinary64(x).divide(denominator),
				ExactRational2D.ofBinary64(y).divide(denominator)};
	}

	private static double live(ConstructiveConeNode node, int position) {
		double[] state = node.getLiveState();
		return state.length > position ? state[position] : Double.NaN;
	}

	/**
	 * The nodes that an attempt from the roots reads, with the replacement of a substituted
	 * driver: the walk stops at the cuts, whose inputs the proof never reads.
	 */
	static boolean[] reads(ConstructiveCone cone, int... roots) {
		return reads(cone, true, roots);
	}

	/**
	 * The nodes that an attempt from the roots reads, with or without the replacement of a
	 * substituted driver: without it, when the driver stands at an exact parameter of its path,
	 * the walk never enters the replacement, whose value the slice does not read.
	 */
	private static boolean[] reads(ConstructiveCone cone, boolean replacement, int... roots) {
		boolean[] seen = new boolean[cone.size()];
		List<Integer> stack = new ArrayList<>();
		for (int root : roots) {
			if (root >= 0) {
				stack.add(root);
			}
		}
		if (cone.hasDriverSubstitution() && replacement) {
			stack.add(cone.getReplacementNode());
		}
		while (!stack.isEmpty()) {
			int index = stack.remove(stack.size() - 1);
			if (!replacement && index == cone.getReplacementNode()) {
				continue;
			}
			if (seen[index]) {
				continue;
			}
			seen[index] = true;
			ConstructiveConeNode node = cone.getNode(index);
			if (isCut(cone, node)) {
				continue;
			}
			for (int position = 0; position < node.getInputCount(); position++) {
				stack.add(node.getInput(position));
			}
		}
		return seen;
	}

	/**
	 * The boundary of one attempt: the indeterminates of the free inputs, in the automatic scope,
	 * and of the cuts, and the rule of the points on constructible line paths.
	 */
	private static final class Boundary
			implements ConstructiveConeEvaluator.PathPointRule<CertificateJet> {
		private final ConstructiveCone cone;
		private final ConstructiveIdentityJetArithmetic jets;
		private final ConstructiveIdentityCertificate.Builder certificate;
		private final ConstructiveIdentityCertificate.Scope scope;
		private final double widening;
		private final ConstructiveConeEvaluator<OutwardInterval> ideal;
		private final Map<Integer, Object> fixed = new HashMap<>();
		private final Map<Integer, Point<CertificateJet>> pathPoints = new HashMap<>();

		private Boundary(ConstructiveCone cone, ConstructiveIdentityJetArithmetic jets,
				ConstructiveIdentityCertificate.Builder certificate,
				ConstructiveIdentityCertificate.Scope scope, double widening) {
			this.cone = cone;
			this.jets = jets;
			this.certificate = certificate;
			this.scope = scope;
			this.widening = widening;
			ideal = ConstructiveConeEvaluator.intervals(cone);
		}

		static Boundary of(ConstructiveCone cone, ConstructiveIdentityJetArithmetic jets,
				ConstructiveIdentityCertificate.Builder certificate,
				ConstructiveIdentityCertificate.Scope scope, double widening, boolean replacement,
				int... roots) {
			Boundary boundary = new Boundary(cone, jets, certificate, scope, widening);
			boolean[] read = reads(cone, replacement, roots);
			for (ConstructiveConeNode node : cone.getNodes()) {
				if (read[node.getIndex()]) {
					boundary.assign(node);
				}
			}
			return boundary;
		}

		private void assign(ConstructiveConeNode node) {
			int index = node.getIndex();
			boolean automatic = scope == ConstructiveIdentityCertificate.Scope.AUTOMATIC;
			switch (node.getOperation()) {
			case FREE_POINT: {
				// In the boundary scope a free point is the exact constant of the evaluator.
				if (automatic && exactPoint(node) != null) {
					String name = name(node);
					fixed.put(index, new Point<>(variable(node, 0, name + ".x"),
							variable(node, 1, name + ".y")));
				}
				break;
			}
			case FREE_NUMBER: {
				if (automatic && Double.isFinite(live(node, 0))) {
					fixed.put(index, variable(node, 0, name(node)));
				}
				break;
			}
			case BOUNDARY_NUMBER: {
				if (node.isDriverSlice()) {
					throw new ConstructiveIdentityException(Cause.OUT_OF_CLASS,
							"a boundary number that moves with the driver");
				}
				fixed.put(index, variable(node, 0, name(node)));
				certificate.cut(name(node));
				break;
			}
			default:
				if (isCut(cone, node)) {
					if (node.isDriverSlice()) {
						throw new ConstructiveIdentityException(Cause.OUT_OF_CLASS,
								"a cut that moves with the driver");
					}
					String name = name(node);
					fixed.put(index, new Point<>(variable(node, 0, name + ".x"),
							variable(node, 1, name + ".y")));
					certificate.cut(name);
				}
				break;
			}
		}

		/**
		 * One indeterminate for a point on a constructible line path: the coordinate along the
		 * larger coefficient of the live line, the other solved from the exact line, whose
		 * coefficient is certified nonzero on the box. The point is one indeterminate of the
		 * attempt, however many evaluations of the attempt read it.
		 */
		@Override
		public Point<CertificateJet> pointOnLine(ConstructiveConeNode node,
				Line<CertificateJet> line) {
			Point<CertificateJet> known = pathPoints.get(node.getIndex());
			if (known != null) {
				return known;
			}
			Point<CertificateJet> point = newPointOnLine(node, line);
			pathPoints.put(node.getIndex(), point);
			return point;
		}

		private Point<CertificateJet> newPointOnLine(ConstructiveConeNode node,
				Line<CertificateJet> line) {
			ConstructiveIdentityArithmetic field = jets.getField();
			ConstructiveConeNode path = cone.getNode(node.getInput(0));
			boolean horizontal = Math.abs(live(path, 1)) >= Math.abs(live(path, 0));
			int free = horizontal ? 0 : 1;
			CertificateElement divisor = (horizontal ? line.getB() : line.getA()).getValue();
			int sign = field.sign(divisor);
			if (sign != 1 && sign != -1) {
				throw new ConstructiveIdentityException(Cause.PRECONDITION_UNDECIDED,
						"the coefficient of the line path is not certified nonzero");
			}
			CertificateJet value = variable(node, free, name(node) + (horizontal ? ".x" : ".y"));
			CertificateElement other = (horizontal ? line.getA() : line.getB()).getValue();
			CertificateElement solved = field.negate(field.divide(field.add(
					field.multiply(other, value.getValue()), line.getC().getValue()), divisor));
			CertificateJet solvedJet = jets.constant(solved);
			return horizontal ? new Point<>(value, solvedJet) : new Point<>(solvedJet, value);
		}

		private CertificateJet variable(ConstructiveConeNode node, int coordinate, String name) {
			ConstructiveIdentityCertificate.Indeterminate indeterminate =
					new ConstructiveIdentityCertificate.Indeterminate(node.getIndex(), coordinate,
							name, null);
			OutwardInterval interval = interval(cone, ideal, indeterminate);
			if (interval == null) {
				throw new ConstructiveIdentityException(Cause.PATH_MAP_UNDEFINED,
						"no rigorous enclosure of the ideal value of " + name);
			}
			if (widening > 0) {
				interval = interval.add(OutwardInterval.of(-widening, widening));
			}
			CertificateElement element = jets.getField().indeterminate(name, interval);
			certificate.indeterminate(new ConstructiveIdentityCertificate.Indeterminate(
					node.getIndex(), coordinate, name, interval));
			return jets.constant(element);
		}

		private static String name(ConstructiveConeNode node) {
			return node.getPresentationName() == null ? "n" + node.getIndex()
					: node.getPresentationName();
		}
	}

	/**
	 * An exact parameter {@code t0 = quarter pi / 2} of a circle driver read in the frame of the
	 * axes, where the point of the circle is exact in the field of the certificate.
	 */
	private static final class DriverAt {
		private static final String[] NAMES = {"0", "pi/2", "pi", "-pi/2"};
		private static final int[] COS = {1, 0, -1, 0};
		private static final int[] SIN = {0, 1, 0, -1};

		private final int quarter;
		private final double[] frame;

		DriverAt(int quarter, double[] frame) {
			if (quarter < 0 || quarter > 3 || frame == null || frame.length != 4) {
				throw new IllegalArgumentException("Not an exact parameter of a circle driver");
			}
			for (double component : frame) {
				if (component != 0 && component != 1 && component != -1) {
					throw new IllegalArgumentException("Not a frame of the axes");
				}
			}
			this.quarter = quarter;
			this.frame = frame.clone();
		}

		String describe() {
			return " driver at " + NAMES[quarter] + " frame " + frame[0] + "," + frame[1] + ","
					+ frame[2] + "," + frame[3];
		}

		/**
		 * The exact jet of order two of the circle at the parameter: the point
		 * {@code m + r (c e0 + s e1)}, its derivative {@code r (-s e0 + c e1)} and its second
		 * derivative {@code -r (c e0 + s e1)}, with {@code (c, s) = (cos t0, sin t0)}.
		 *
		 * @return the jet, or {@code null} for another path
		 */
		@SuppressWarnings("unchecked")
		Point<CertificateJet> jet(ConstructiveIdentityJetArithmetic jets, Object path) {
			if (!(path instanceof Circle)) {
				return null;
			}
			Circle<CertificateJet> circle = (Circle<CertificateJet>) path;
			ConstructiveIdentityArithmetic field = jets.getField();
			CertificateElement radius = field.sqrt(circle.getRadiusSquared().getValue());
			int c = COS[quarter];
			int s = SIN[quarter];
			CertificateElement alongX = field.multiply(radius,
					field.ofBinary64(c * frame[0] + s * frame[2]));
			CertificateElement alongY = field.multiply(radius,
					field.ofBinary64(c * frame[1] + s * frame[3]));
			CertificateElement turnX = field.multiply(radius,
					field.ofBinary64(c * frame[2] - s * frame[0]));
			CertificateElement turnY = field.multiply(radius,
					field.ofBinary64(c * frame[3] - s * frame[1]));
			return new Point<>(new CertificateJet(field.add(circle.getCenterX().getValue(),
					alongX), turnX, field.negate(alongX)), new CertificateJet(field.add(
							circle.getCenterY().getValue(), alongY), turnY, field.negate(alongY)));
		}
	}
}
