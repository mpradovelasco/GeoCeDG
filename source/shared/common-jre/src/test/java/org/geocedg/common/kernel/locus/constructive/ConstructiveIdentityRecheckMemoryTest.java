/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Scope;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Status;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * The reusable exact memory of the rechecks of a constructive identity certificate (PRE-G9B-CU3
 * focal optimization of the recheck, T1 to T3), on the replica of the author's model: in every
 * revision of the lifecycle of {@code ConstructiveIdentityContactTest} the accelerated recheck
 * decides as the procedure of the candidate; the memory certifies every sign of the cell itself
 * at the base revision; the specialization is exact and canonical whatever the history; a
 * changed point value repeats only the steps from its indeterminate; the prepared margin is the
 * margin; a kept derivative encloses and counts as a fresh one; and a memory that fails leaves
 * every decision to the procedure of the candidate.
 */
final class ConstructiveIdentityRecheckMemoryTest extends ConstructiveModelReplicaTest {

	/** Locus, target and driver point of the five mandatory contacts. */
	private static final String[][] CONTACTS = {{"e_1", "n", "K"}, {"e_1", "c_1", "K"},
			{"o_1", "h_2", "K"}, {"e_1", "c_1", "J"}, {"e_1", "r", "J"}};

	@Test
	void theAcceleratedRecheckDecidesAsTheCandidateInEveryRevision() {
		buildAuthorReplica();
		Map<String, ConstructiveIdentityCertificate> certificates = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			for (Scope scope : Scope.values()) {
				ConstructiveIdentityCertificate certificate = attempt(contact, scope);
				assertEquals(Status.IDENTITY_CERTIFIED, certificate.getStatus(),
						certificate.toString());
				certificates.put(String.join(" ", contact) + " " + scope, certificate);
			}
		}
		int[] outcomes = new int[2];
		compare("base", certificates, outcomes);
		GeoNumeric radius = (GeoNumeric) geo("rbCone");
		for (double value : new double[] {3, 4, 6, 7.5}) {
			setNumber(radius, value);
			compare("rbCone=" + value, certificates, outcomes);
		}
		setNumber(radius, 5);
		GeoNumeric angle = (GeoNumeric) geo("alpha");
		double alpha = angle.getValue();
		for (double degrees : new double[] {35, 60}) {
			setNumber(angle, Math.toRadians(degrees));
			compare("alpha=" + degrees, certificates, outcomes);
		}
		setNumber(angle, alpha);
		GeoPoint d1 = (GeoPoint) geo("D_1");
		GeoPoint h = (GeoPoint) geo("H");
		double x = d1.getInhomX();
		double y = d1.getInhomY();
		for (double factor : new double[] {0.5, 1.3, 3}) {
			place("D_1", h.getInhomX() + factor * (x - h.getInhomX()),
					h.getInhomY() + factor * (y - h.getInhomY()));
			compare("D_1 x" + factor, certificates, outcomes);
		}
		place("D_1", x, y);
		compare("restored", certificates, outcomes);
		double parameter = parameter("H");
		for (double delta : new double[] {-20, -0.5, 0.5, 20, 40}) {
			moveOnPath("H", parameter + delta);
			compare("H " + delta, certificates, outcomes);
		}
		moveOnPath("H", parameter);
		double onG = parameter("B");
		for (double delta : new double[] {-3, -0.5, 0.5, 3}) {
			moveOnPath("B", onG + delta);
			compare("B " + delta, certificates, outcomes);
		}
		moveOnPath("B", onG);
		compare("restored again", certificates, outcomes);
		assertTrue(outcomes[0] > 0 && outcomes[1] > 0, "the revisions keep and leave cells: "
				+ outcomes[0] + " rechecks hold and " + outcomes[1] + " fail");
	}

	@Test
	void theMemoryCertifiesEverySignOfTheCellAtTheBaseRevision() {
		buildAuthorReplica();
		int decided = 0;
		for (String[] contact : CONTACTS) {
			for (Scope scope : Scope.values()) {
				ConstructiveIdentityCertificate certificate = attempt(contact, scope);
				Slice slice = slice(contact);
				OutwardInterval[] box = recheckBox(certificate, slice.cone);
				ConstructiveIdentityCounters counters = new ConstructiveIdentityCounters(
						ConstructiveIdentityBudget.V1);
				ConstructiveIdentityRecheckMemory memory = certificate.getRecheckMemory();
				List<ConstructiveIdentityArithmetic.Condition> cell = certificate.getCell();
				for (int index = 0; index < cell.size(); index++) {
					OutwardInterval enclosure = memory.condition(certificate, index, box,
							counters);
					assertNotNull(enclosure);
					assertEquals(cell.get(index).getSign(), enclosure.certifiedSign(),
							"condition " + index + " of " + String.join(" ", contact));
					decided++;
				}
				List<ConstructiveIdentityCertificate.Correspondence> correspondences =
						certificate.getCorrespondences();
				for (int index = 0; index < correspondences.size(); index++) {
					ExactRational2D[] live = ConstructiveIdentityCertifier.exactPoint(
							slice.cone.getNode(correspondences.get(index).getNode()));
					OutwardInterval enclosure = memory.margin(certificate, index, live, box,
							counters);
					assertNotNull(enclosure);
					assertEquals(1, enclosure.certifiedSign(), "margin " + index + " of "
							+ String.join(" ", contact));
					decided++;
				}
			}
		}
		assertTrue(decided > 0);
	}

	@Test
	void theSpecializationIsExactAndCanonicalWhateverTheHistory() {
		buildAuthorReplica();
		ConstructiveIdentityCertificate certificate = attempt(CONTACTS[2], Scope.AUTOMATIC);
		OutwardInterval[] box = box(certificate);
		ExactRational2D[] values = ConstructiveIdentityRecheckMemory.pointValues(box);
		ExactRational2D[] changed = values.clone();
		int last = lastPoint(values, richest(certificate, values));
		changed[last] = values[last].add(ExactRational2D.of(1, 8));
		ExactRational2D[] centre = new ExactRational2D[CertificatePolynomial.INDETERMINATES];
		for (int index = 0; index < box.length; index++) {
			centre[index] = values[index] != null ? values[index] : ExactRational2D.midpoint(
					ExactRational2D.ofBinary64(box[index].getLower()),
					ExactRational2D.ofBinary64(box[index].getUpper()));
		}
		ConstructiveIdentityCounters counters = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.V1);
		ConstructiveIdentityRecheckMemory fresh = new ConstructiveIdentityRecheckMemory();
		ConstructiveIdentityRecheckMemory history = new ConstructiveIdentityRecheckMemory();
		int checked = 0;
		for (CertificatePolynomial polynomial : polynomials(certificate)) {
			CertificatePolynomial specialized = fresh.specialized(polynomial, values, counters);
			assertEquals(0, polynomial.valueAt(centre).compareTo(specialized.valueAt(centre)),
					"the specialization is exact");
			history.specialized(polynomial, changed, counters);
			CertificatePolynomial again = history.specialized(polynomial, values, counters);
			assertTrue(again.sameAs(specialized), "the same canonical polynomial after another"
					+ " value of " + certificate.getIndeterminateNames().get(last));
			checked++;
		}
		assertTrue(checked > 0);
	}

	@Test
	void aChangedPointValueRepeatsOnlyTheStepsFromItsIndeterminate() {
		buildAuthorReplica();
		ConstructiveIdentityCertificate certificate = attempt(CONTACTS[2], Scope.AUTOMATIC);
		ExactRational2D[] values = ConstructiveIdentityRecheckMemory.pointValues(
				box(certificate));
		CertificatePolynomial polynomial = richest(certificate, values);
		int last = lastPoint(values, polynomial);
		ConstructiveIdentityCounters counters = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.V1);
		ConstructiveIdentityRecheckMemory memory = new ConstructiveIdentityRecheckMemory();
		memory.specialized(polynomial, values, counters);
		int first = memory.getSubstitutions();
		assertTrue(first > 1, "one step for each point indeterminate that occurs: " + first);
		memory.specialized(polynomial, values, counters);
		assertEquals(first, memory.getSubstitutions(), "the same values repeat no step");
		ExactRational2D[] moved = values.clone();
		moved[last] = values[last].add(ExactRational2D.of(1, 8));
		memory.specialized(polynomial, moved, counters);
		assertEquals(first + 1, memory.getSubstitutions(), "a change of the last indeterminate"
				+ " repeats its step only");
		int earliest = firstPoint(values, polynomial);
		ExactRational2D[] early = moved.clone();
		early[earliest] = values[earliest].add(ExactRational2D.of(1, 8));
		memory.specialized(polynomial, early, counters);
		assertEquals(first + 1 + first, memory.getSubstitutions(), "a change of the first one"
				+ " repeats every step");
	}

	@Test
	void thePreparedMarginIsTheMarginOfTheCandidate() {
		buildAuthorReplica();
		int checked = 0;
		for (String[] contact : CONTACTS) {
			ConstructiveIdentityCertificate certificate = attempt(contact, Scope.AUTOMATIC);
			Slice slice = slice(contact);
			ConstructiveIdentityArithmetic field = certificate.getArithmetic().forRecheck(
					new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1));
			List<ConstructiveIdentityCertificate.Correspondence> correspondences =
					certificate.getCorrespondences();
			for (int index = 0; index < correspondences.size(); index++) {
				ConstructiveIdentityCertificate.Correspondence correspondence =
						correspondences.get(index);
				ExactRational2D[] live = ConstructiveIdentityCertifier.exactPoint(
						slice.cone.getNode(correspondence.getNode()));
				assertNotNull(live);
				CertificateElement prepared = certificate.getRecheckMemory().preparedMargin(
						certificate, index, live, field);
				CertificateElement direct = ConstructiveIdentityCertifier.margin(field,
						correspondence, live);
				assertNotNull(prepared);
				assertTrue(field.subtract(prepared, direct).isZero(), "A + x U + y W is the"
						+ " margin of " + String.join(" ", contact));
				checked++;
			}
		}
		assertTrue(checked > 0, "the contacts check their line-circle outputs: " + checked);
	}

	@Test
	void aKeptDerivativeEnclosesAndCountsAsAFreshOne() {
		buildAuthorReplica();
		ConstructiveIdentityCertificate certificate = attempt(CONTACTS[2], Scope.AUTOMATIC);
		OutwardInterval[] box = new OutwardInterval[CertificatePolynomial.INDETERMINATES];
		OutwardInterval[] own = box(certificate);
		for (int index = 0; index < box.length; index++) {
			box[index] = index < own.length ? own[index].hull(OutwardInterval.of(
					own[index].getLower() - 1E-9, own[index].getUpper() + 1E-9))
					: OutwardInterval.point(0);
		}
		int checked = 0;
		for (CertificatePolynomial polynomial : polynomials(certificate)) {
			CertificatePolynomial kept = polynomial.memoizing();
			for (int run = 0; run < 3; run++) {
				ConstructiveIdentityCounters fresh = new ConstructiveIdentityCounters(
						ConstructiveIdentityBudget.V1);
				ConstructiveIdentityCounters memo = new ConstructiveIdentityCounters(
						ConstructiveIdentityBudget.V1);
				OutwardInterval reference = polynomial.enclose(box, fresh);
				OutwardInterval again = kept.enclose(box, memo);
				assertEquals(reference.getLower(), again.getLower(), 0);
				assertEquals(reference.getUpper(), again.getUpper(), 0);
				assertEquals(fresh.getLargestTerms(), memo.getLargestTerms());
				assertEquals(fresh.getLargestBits(), memo.getLargestBits());
				assertEquals(fresh.getTermProducts(), memo.getTermProducts());
			}
			checked++;
		}
		assertTrue(checked > 0);
	}

	@Test
	void aMemoryThatFailsLeavesEveryDecisionToTheCandidate() {
		buildAuthorReplica();
		ConstructiveIdentityCertificate certificate = attempt(CONTACTS[2], Scope.AUTOMATIC);
		certificate.getRecheckMemory().disable();
		Slice slice = slice(CONTACTS[2]);
		assertTrue(ConstructiveIdentityCertifier.recheckCell(certificate, slice.cone,
				ConstructiveIdentityBudget.V1, true), "the procedure of the candidate decides");
		assertEquals(0, certificate.getRecheckMemory().getSubstitutions());
		moveOnPath("H", parameter("H") + 40);
		Slice other = slice(CONTACTS[2]);
		assertFalse(ConstructiveIdentityCertifier.recheckCell(certificate, other.cone,
				ConstructiveIdentityBudget.V1, true), "and fails closed in another cell");
	}

	// ---------------------------------------------------------------- helpers

	private void compare(String revision,
			Map<String, ConstructiveIdentityCertificate> certificates, int[] outcomes) {
		for (Map.Entry<String, ConstructiveIdentityCertificate> entry : certificates.entrySet()) {
			String[] contact = entry.getKey().split(" ");
			Slice slice = slice(contact);
			boolean reference = ConstructiveIdentityCertifier.recheckCell(entry.getValue(),
					slice.cone, ConstructiveIdentityBudget.V1, false);
			boolean accelerated = ConstructiveIdentityCertifier.recheckCell(entry.getValue(),
					slice.cone, ConstructiveIdentityBudget.V1, true);
			assertEquals(reference, accelerated, "the same decision for " + entry.getKey()
					+ " at " + revision);
			outcomes[reference ? 0 : 1]++;
		}
	}

	private ConstructiveIdentityCertificate attempt(String[] contact, Scope scope) {
		Slice slice = slice(contact);
		return ConstructiveIdentityCertifier.incidence(String.join(" ", contact), slice.cone,
				slice.point, slice.target, slice.support, scope, ConstructiveIdentityBudget.V1);
	}

	private Slice slice(String[] contact) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		Slice slice = new Slice();
		slice.target = builder.add(geo(contact[1]));
		slice.support = builder.add(geo("p"));
		slice.point = builder.addWithDriverSubstitution(traced(contact[0]), geo("E_1"),
				geo(contact[2]));
		slice.cone = builder.build();
		return slice;
	}

	private GeoElement traced(String locus) {
		return geo("e_1".equals(locus) ? "J_1" : "K_1");
	}

	private double parameter(String label) {
		return ((GeoPoint) geo(label)).getPathParameter().getT();
	}

	/** Moves a point on its path to a parameter, as a drag along the path does. */
	private void moveOnPath(String label, double t) {
		GeoPoint point = (GeoPoint) geo(label);
		point.getPathParameter().setT(t);
		((Path) point.getPath()).pathChanged(point);
		point.updateCoords();
		point.updateCascade();
	}

	private static void setNumber(GeoNumeric number, double value) {
		number.setValue(value);
		number.updateCascade();
	}

	private static OutwardInterval[] box(ConstructiveIdentityCertificate certificate) {
		List<ConstructiveIdentityCertificate.Indeterminate> indeterminates =
				certificate.getIndeterminates();
		OutwardInterval[] box = new OutwardInterval[indeterminates.size()];
		for (int index = 0; index < box.length; index++) {
			box[index] = indeterminates.get(index).getInterval();
		}
		return box;
	}

	/** The box of a recheck of the certificate on the cone, built as the recheck builds it. */
	private static OutwardInterval[] recheckBox(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone) {
		ConstructiveConeEvaluator<OutwardInterval> ideal = ConstructiveConeEvaluator.intervals(
				cone);
		List<ConstructiveIdentityCertificate.Indeterminate> indeterminates =
				certificate.getIndeterminates();
		OutwardInterval[] box = new OutwardInterval[indeterminates.size()];
		for (int index = 0; index < box.length; index++) {
			box[index] = ConstructiveIdentityCertifier.interval(cone, ideal,
					indeterminates.get(index));
			assertNotNull(box[index]);
		}
		return box;
	}

	private static List<CertificatePolynomial> polynomials(
			ConstructiveIdentityCertificate certificate) {
		List<CertificatePolynomial> polynomials = new ArrayList<>();
		for (ConstructiveIdentityArithmetic.Condition condition : certificate.getCell()) {
			CertificateElement element = condition.getElement();
			polynomials.add(element.getRational().getNumerator());
			polynomials.add(element.getRational().getDenominator());
			polynomials.add(element.getIrrational().getNumerator());
		}
		if (certificate.getArithmetic().getRadicand() != null) {
			polynomials.add(certificate.getArithmetic().getRadicand());
		}
		return polynomials;
	}

	/** The polynomial of the cell in which the most point indeterminates occur. */
	private static CertificatePolynomial richest(ConstructiveIdentityCertificate certificate,
			ExactRational2D[] values) {
		CertificatePolynomial richest = null;
		int most = 0;
		for (CertificatePolynomial polynomial : polynomials(certificate)) {
			int occurring = 0;
			for (int index = 0; index < values.length; index++) {
				if (values[index] != null && polynomial.maximumExponents()[index] > 0) {
					occurring++;
				}
			}
			if (occurring > most) {
				richest = polynomial;
				most = occurring;
			}
		}
		assertNotNull(richest);
		return richest;
	}

	private static int lastPoint(ExactRational2D[] values, CertificatePolynomial polynomial) {
		for (int index = values.length - 1; index >= 0; index--) {
			if (values[index] != null && polynomial.maximumExponents()[index] > 0) {
				return index;
			}
		}
		throw new IllegalStateException("no point indeterminate");
	}

	private static int firstPoint(ExactRational2D[] values, CertificatePolynomial polynomial) {
		for (int index = 0; index < values.length; index++) {
			if (values[index] != null && polynomial.maximumExponents()[index] > 0) {
				return index;
			}
		}
		throw new IllegalStateException("no point indeterminate");
	}

	/** The cone and the nodes of one contact. */
	private static final class Slice {
		private ConstructiveCone cone;
		private int target;
		private int support;
		private int point;
	}
}
