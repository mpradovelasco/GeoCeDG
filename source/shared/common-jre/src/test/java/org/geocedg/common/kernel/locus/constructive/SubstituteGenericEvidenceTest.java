/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.SubstituteGenericEvidence.Cause;
import org.geocedg.common.kernel.locus.constructive.SubstituteGenericEvidence.Result;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * {@code SUBSTITUTE_GENERIC} version 1 ({@code locus-v2-constructive-authority.md} section 7.1;
 * validation matrix rows V-AUT-RLX-1 to V-AUT-RLX-7): the five mandatory contacts are
 * {@code RELAXED} with the rotation named as an unestablished premise, while an incidence that
 * holds only at the binary64 substitute, a perturbation that leaves the cell and a refutation for
 * the substitutes are never relaxed; nothing infers an identity from the finite set.
 */
final class SubstituteGenericEvidenceTest extends ConstructiveModelReplicaTest {

	@Test
	void theFiveMandatoryContactsAreRelaxedWithTheRotationAsAnUnestablishedPremise() {
		buildAuthorReplica();
		String[][] contacts = {{"n", "J_1", "K"}, {"c_1", "J_1", "K"}, {"h_2", "K_1", "K"},
				{"c_1", "J_1", "J"}, {"r", "J_1", "J"}};
		for (String[] contact : contacts) {
			ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
			int target = builder.add(geo(contact[0]));
			int point = builder.addWithDriverSubstitution(geo(contact[1]), geo("E_1"),
					geo(contact[2]));
			ConstructiveCone cone = builder.build();

			Result result = SubstituteGenericEvidence.incidence("P on " + contact[0], cone,
					point, target, ExactEvaluationBudget.V1);

			String name = String.join("/", contact);
			assertEquals(RelationIntegrity.RELAXED, result.getRecord().getIntegrity(),
					name + ": " + result);
			assertEquals(RelationIntegrityRecord.Evidence.SUBSTITUTE_GENERIC,
					result.getRecord().getEvidence());
			assertEquals(1, result.getRecord().getUnestablishedPremises().size());
			assertTrue(result.getRecord().getUnestablishedPremises().get(0).contains("Cr"));
			assertEquals(2, result.getCoordinates(), "C' has two boundary coordinates");
			assertEquals(8, result.getPerturbations(), "N = max(8, (m + 1)(m + 2)/2)");
			assertEquals(9, result.getCounters().size(), "N + 1 exact evaluations");
			for (ExactEvaluationCounters counters : result.getCounters()) {
				assertTrue(counters.getOperations() <= ExactEvaluationBudget.MAXIMUM_OPERATIONS);
			}
		}
	}

	@Test
	void aRefutationForTheSubstitutesIsNeverAContradiction() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("h"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));

		Result result = SubstituteGenericEvidence.incidence("P on h", builder.build(), point,
				target, ExactEvaluationBudget.V1);

		assertEquals(Cause.REFUTED_FOR_THE_SUBSTITUTES, result.getCause());
		assertEquals(RelationIntegrity.NOT_ESTABLISHED, result.getRecord().getIntegrity(),
				"V-AUT-INT-3: an exact refutation with a substitute is not rigorous");
	}

	@Test
	void anIncidenceThatHoldsOnlyAtTheBinary64SubstituteIsNotRelaxed() {
		add("O=(0,0)");
		add("X=(1.3,0.4)");
		add("w=31°");
		GeoPoint rotated = add("S=Rotate(X,w,O)");
		add("F=(" + rotated.getInhomX() + "," + rotated.getInhomY() + ")");
		add("c=Circle(O,F)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(rotated);
		int target = builder.add(lookup("c"));
		ConstructiveCone cone = builder.build();

		assertEquals(Incidence.HOLDS, ConstructiveConeEvaluator.exact(cone,
				ExactEvaluationBudget.V1).incidence(point, target).getIncidence(),
				"the binary64 substitute lies exactly on the circle through its own copy");
		Result result = SubstituteGenericEvidence.incidence("S on c", cone, point, target,
				ExactEvaluationBudget.V1);

		assertEquals(Cause.PERTURBATION_REFUTED, result.getCause(),
				"V-AUT-RLX-2: a coincidence of the substitute is not an incidence: " + result);
		assertEquals(RelationIntegrity.NOT_ESTABLISHED, result.getRecord().getIntegrity());
	}

	@Test
	void aPerturbationThatLeavesTheCellIsNeverMixed() {
		add("O=(0,0)");
		add("P=(1,0)");
		add("w=0.000000000001");
		add("S=Rotate(P,w,O)");
		add("f=Line(O,S)");
		add("t=Ray((2,0),(2,1))");
		add("X=Intersect(f,t)");
		add("g=Line(O,(1,1))");
		add("Y=Intersect(f,g)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("Y"));
		int target = builder.add(lookup("f"));
		int meet = builder.add(lookup("X"));
		ConstructiveCone cone = builder.build();

		Result holds = SubstituteGenericEvidence.incidence("Y on f", cone, point, target,
				ExactEvaluationBudget.V1);
		Result left = SubstituteGenericEvidence.incidence("X on f", cone, meet, target,
				ExactEvaluationBudget.V1);

		assertEquals(RelationIntegrity.RELAXED, holds.getRecord().getIntegrity(),
				"a relation by provenance holds at every perturbation: " + holds);
		assertEquals(Cause.PERTURBATION_LEFT_CELL, left.getCause(),
				"V-AUT-RLX-3: the membership of the ray changes sign: " + left);
	}

	@Test
	void anAccidentalEqualityOfDegreeOneIsNotRelaxed() {
		add("O=(0,0)");
		add("X=(1.3,0.4)");
		add("w=31°");
		GeoPoint rotated = add("S=Rotate(X,w,O)");
		add("F=(" + rotated.getInhomX() + ",0)");
		add("v=Line(F,yAxis)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(rotated);
		int target = builder.add(lookup("v"));

		Result result = SubstituteGenericEvidence.incidence("S on v", builder.build(), point,
				target, ExactEvaluationBudget.V1);

		assertEquals(Cause.PERTURBATION_REFUTED, result.getCause(),
				"V-AUT-RLX-1: the vertical through the substitute holds only there");
	}

	@Test
	void aFiniteAgreementIsNotAnIdentity() {
		List<ExactRational2D> base = List.of(ExactRational2D.ofBinary64(0.7),
				ExactRational2D.ofBinary64(-1.3));
		List<List<ExactRational2D>> points = new ArrayList<>();
		points.add(base);
		points.addAll(SubstituteGenericPerturbationSet.of(base).getPoints());
		ExactRational2D[] cubic = cubicThrough(points);
		Predicate<List<ExactRational2D>> vanishes = w -> cubic(cubic, w).isZero();

		assertEquals(9, points.size());
		assertTrue(SubstituteGenericEvidence.holdsOnTheSet(base, vanishes),
				"V-AUT-RLX-2: a nonzero cubic vanishes at the N + 1 = 9 points");
		assertFalse(cubic(cubic, List.of(ExactRational2D.of(3), ExactRational2D.of(5)))
				.isZero(), "the cubic is not the zero polynomial");
		RelationIntegrityRecord admitted = RelationIntegrityRecord.derive("cubic",
				RelationIntegrity.STRICT, RelationIntegrityRecord.Evidence.SUBSTITUTE_GENERIC,
				Collections.emptyList(), List.of("boundary substitute w"), null);
		assertEquals(RelationIntegrity.RELAXED, admitted.getIntegrity(),
				"the kind admits it as RELAXED, never as STRICT");
		RelationIntegrityRecord consumer = RelationIntegrityRecord.derive("consumer",
				RelationIntegrity.STRICT, RelationIntegrityRecord.Evidence.PROVENANCE,
				List.of(admitted), Collections.emptyList(), null);
		assertEquals(RelationIntegrity.RELAXED, consumer.getIntegrity(),
				"and never as a premise of STRICT");
	}

	@Test
	void theRankConditionRejectsCoincidencesOfDegreeTwo() {
		ExactRational2D z1 = ExactRational2D.ofBinary64(0.7);
		ExactRational2D z2 = ExactRational2D.ofBinary64(-1.3);
		List<ExactRational2D> base = List.of(z1, z2);

		assertFalse(SubstituteGenericEvidence.holdsOnTheSet(base, w -> square(w.get(0)
				.subtract(z1)).subtract(square(w.get(1).subtract(z2))).isZero()),
				"V-AUT-RLX-3: a nonzero quadric through the base is rejected");
		assertFalse(SubstituteGenericEvidence.holdsOnTheSet(base, w -> w.get(0).subtract(z1)
				.multiply(w.get(1).subtract(z2)).isZero()),
				"V-AUT-RLX-4: every direction moves every coordinate");
	}

	@Test
	void theEvidenceIsDeterministic() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("h_2"));
		int point = builder.addWithDriverSubstitution(geo("K_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		Result first = SubstituteGenericEvidence.incidence("P on h_2", cone, point, target,
				ExactEvaluationBudget.V1);
		Result second = SubstituteGenericEvidence.incidence("P on h_2", cone, point, target,
				ExactEvaluationBudget.V1);

		assertEquals(first.toString(), second.toString());
		assertEquals(first.getCell(), second.getCell());
		assertEquals(first.getCounters().toString(), second.getCounters().toString());
	}

	@Test
	void anExactEvaluationWithoutSubstitutesNeedsNoRelaxedKind() {
		add("A=(0.1,0.3)");
		add("B=(1.7,2.9)");
		add("f=Line(A,B)");
		add("M=Midpoint(A,B)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int line = builder.add(lookup("f"));
		int midpoint = builder.add(lookup("M"));

		Result result = SubstituteGenericEvidence.incidence("M on f", builder.build(),
				midpoint, line, ExactEvaluationBudget.V1);

		assertEquals(Cause.NO_SUBSTITUTE, result.getCause());
		assertEquals(Incidence.HOLDS, result.getBase().getIncidence(),
				"the exact evaluation is itself rigorous");
	}

	private static ExactRational2D square(ExactRational2D value) {
		return value.multiply(value);
	}

	/** The ten monomials of degree at most 3 in two variables. */
	private static ExactRational2D[] monomials(List<ExactRational2D> w) {
		ExactRational2D x = w.get(0);
		ExactRational2D y = w.get(1);
		return new ExactRational2D[] {ExactRational2D.ONE, x, y, x.multiply(x), x.multiply(y),
				y.multiply(y), x.multiply(x).multiply(x), x.multiply(x).multiply(y),
				x.multiply(y).multiply(y), y.multiply(y).multiply(y)};
	}

	private static ExactRational2D cubic(ExactRational2D[] coefficients, List<ExactRational2D> w) {
		ExactRational2D[] terms = monomials(w);
		ExactRational2D value = ExactRational2D.ZERO;
		for (int index = 0; index < terms.length; index++) {
			value = value.add(coefficients[index].multiply(terms[index]));
		}
		return value;
	}

	/** A nonzero cubic through nine points, by exact Gaussian elimination. */
	private static ExactRational2D[] cubicThrough(List<List<ExactRational2D>> points) {
		int columns = 10;
		List<ExactRational2D[]> rows = new ArrayList<>();
		for (List<ExactRational2D> point : points) {
			rows.add(monomials(point));
		}
		int[] pivotColumn = new int[rows.size()];
		int rank = 0;
		for (int column = 0; column < columns && rank < rows.size(); column++) {
			int pivot = -1;
			for (int row = rank; row < rows.size() && pivot < 0; row++) {
				if (!rows.get(row)[column].isZero()) {
					pivot = row;
				}
			}
			if (pivot < 0) {
				continue;
			}
			ExactRational2D[] swap = rows.get(pivot);
			rows.set(pivot, rows.get(rank));
			rows.set(rank, swap);
			ExactRational2D[] lead = rows.get(rank);
			ExactRational2D inverse = ExactRational2D.ONE.divide(lead[column]);
			for (int index = 0; index < columns; index++) {
				lead[index] = lead[index].multiply(inverse);
			}
			for (int row = 0; row < rows.size(); row++) {
				ExactRational2D[] other = rows.get(row);
				if (row != rank && !other[column].isZero()) {
					ExactRational2D factor = other[column];
					for (int index = 0; index < columns; index++) {
						other[index] = other[index].subtract(factor.multiply(lead[index]));
					}
				}
			}
			pivotColumn[rank++] = column;
		}
		boolean[] isPivot = new boolean[columns];
		for (int row = 0; row < rank; row++) {
			isPivot[pivotColumn[row]] = true;
		}
		int free = 0;
		while (isPivot[free]) {
			free++;
		}
		ExactRational2D[] solution = new ExactRational2D[columns];
		java.util.Arrays.fill(solution, ExactRational2D.ZERO);
		solution[free] = ExactRational2D.ONE;
		for (int row = 0; row < rank; row++) {
			solution[pivotColumn[row]] = rows.get(row)[free].negate();
		}
		return solution;
	}
}
