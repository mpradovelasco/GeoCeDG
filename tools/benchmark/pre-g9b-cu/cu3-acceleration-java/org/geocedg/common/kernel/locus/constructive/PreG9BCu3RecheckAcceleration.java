/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * PRE-G9B-CU3 focal optimization of the recheck, for {@code PreG9BCu3RecheckAccelerationProbe}
 * only: one recheck of a certificate at a new revision with and without its recheck memory
 * (times, which signs the memory certifies and which fall back to the procedure of the candidate,
 * and the size of the memory), read through package access and reflection. It changes nothing but
 * the memory of the certificate, as a recheck does, and is not part of any source set.
 */
public final class PreG9BCu3RecheckAcceleration {

	private PreG9BCu3RecheckAcceleration() {
	}

	public static Map<String, Object> analyze(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone) throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		ConstructiveIdentityBudget budget = ConstructiveIdentityBudget.V1;
		List<Double> reference = new ArrayList<>();
		boolean referenceOutcome = false;
		for (int run = 0; run < 5; run++) {
			long start = System.nanoTime();
			referenceOutcome = ConstructiveIdentityCertifier.recheckCell(certificate, cone, budget,
					false);
			reference.add((System.nanoTime() - start) / 1E6);
		}
		long start = System.nanoTime();
		boolean first = ConstructiveIdentityCertifier.recheckCell(certificate, cone, budget, true);
		double firstMs = (System.nanoTime() - start) / 1E6;
		List<Double> warm = new ArrayList<>();
		boolean warmOutcome = false;
		for (int run = 0; run < 5; run++) {
			start = System.nanoTime();
			warmOutcome = ConstructiveIdentityCertifier.recheckCell(certificate, cone, budget,
					true);
			warm.add((System.nanoTime() - start) / 1E6);
		}
		rows.put("candidate_outcome", referenceOutcome);
		rows.put("accelerated_outcome", first);
		rows.put("accelerated_outcome_repeated", warmOutcome);
		rows.put("candidate_median_ms", median(reference));
		rows.put("accelerated_first_ms", firstMs);
		rows.put("accelerated_repeated_median_ms", median(warm));
		rows.put("signs", signs(certificate, cone));
		rows.put("memory", footprint(certificate.getRecheckMemory()));
		return rows;
	}

	private static Map<String, Object> signs(ConstructiveIdentityCertificate certificate,
			ConstructiveCone cone) {
		Map<String, Object> rows = new LinkedHashMap<>();
		ConstructiveConeEvaluator<OutwardInterval> ideal = ConstructiveConeEvaluator.intervals(cone);
		List<ConstructiveIdentityCertificate.Indeterminate> indeterminates =
				certificate.getIndeterminates();
		OutwardInterval[] box = new OutwardInterval[indeterminates.size()];
		int points = 0;
		for (int index = 0; index < box.length; index++) {
			box[index] = ConstructiveIdentityCertifier.interval(cone, ideal,
					indeterminates.get(index));
			if (box[index] == null) {
				rows.put("box", "undefined");
				return rows;
			}
			if (box[index].getLower() == box[index].getUpper()) {
				points++;
			}
		}
		rows.put("indeterminates", box.length);
		rows.put("point_indeterminates", points);
		ConstructiveIdentityRecheckMemory memory = certificate.getRecheckMemory();
		ConstructiveIdentityCounters counters = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.V1);
		ConstructiveIdentityArithmetic field = certificate.getArithmetic().forRecheck(
				new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1));
		int memoryConditions = 0;
		int fallbackConditions = 0;
		int candidateConditions = 0;
		List<ConstructiveIdentityArithmetic.Condition> cell = certificate.getCell();
		for (int index = 0; index < cell.size(); index++) {
			int sign = cell.get(index).getSign();
			OutwardInterval fast = memory.condition(certificate, index, box, counters);
			OutwardInterval slow = field.enclose(cell.get(index).getElement(), box);
			boolean fastOk = fast != null && fast.certifiedSign() == sign;
			boolean slowOk = slow != null && slow.certifiedSign() == sign;
			memoryConditions += fastOk ? 1 : 0;
			fallbackConditions += fastOk ? 0 : 1;
			candidateConditions += slowOk ? 1 : 0;
			if (fastOk != slowOk) {
				rows.put("condition " + index + " differs", "memory " + fastOk + ", candidate "
						+ slowOk);
			}
		}
		rows.put("conditions", cell.size());
		rows.put("conditions_certified_by_the_memory", memoryConditions);
		rows.put("conditions_left_to_the_candidate", fallbackConditions);
		rows.put("conditions_certified_by_the_candidate", candidateConditions);
		int memoryMargins = 0;
		int candidateMargins = 0;
		List<ConstructiveIdentityCertificate.Correspondence> correspondences =
				certificate.getCorrespondences();
		for (int index = 0; index < correspondences.size(); index++) {
			ExactRational2D[] live = ConstructiveIdentityCertifier.exactPoint(
					cone.getNode(correspondences.get(index).getNode()));
			if (live == null) {
				continue;
			}
			OutwardInterval fast = memory.margin(certificate, index, live, box, counters);
			OutwardInterval slow = field.enclose(ConstructiveIdentityCertifier.margin(field,
					correspondences.get(index), live), box);
			memoryMargins += fast != null && fast.certifiedSign() == 1 ? 1 : 0;
			candidateMargins += slow != null && slow.certifiedSign() == 1 ? 1 : 0;
		}
		rows.put("margins", correspondences.size());
		rows.put("margins_certified_by_the_memory", memoryMargins);
		rows.put("margins_certified_by_the_candidate", candidateMargins);
		rows.put("accelerated_counters", counters.toString());
		return rows;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> footprint(ConstructiveIdentityRecheckMemory memory)
			throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		Field chainsField = ConstructiveIdentityRecheckMemory.class.getDeclaredField("chains");
		chainsField.setAccessible(true);
		Map<CertificatePolynomial, Object> chains =
				(Map<CertificatePolynomial, Object>) chainsField.get(memory);
		Field marginsField = ConstructiveIdentityRecheckMemory.class.getDeclaredField("margins");
		marginsField.setAccessible(true);
		Map<Integer, Object> margins = (Map<Integer, Object>) marginsField.get(memory);
		Map<CertificatePolynomial, Boolean> seen = new IdentityHashMap<>();
		long[] totals = new long[3];
		int steps = 0;
		for (Object chain : chains.values()) {
			Field base = chain.getClass().getDeclaredField("base");
			base.setAccessible(true);
			count((CertificatePolynomial) base.get(chain), seen, totals);
			Field results = chain.getClass().getDeclaredField("results");
			results.setAccessible(true);
			for (CertificatePolynomial result : (List<CertificatePolynomial>) results.get(chain)) {
				count(result, seen, totals);
				steps++;
			}
		}
		rows.put("chains", chains.size());
		rows.put("kept_steps", steps);
		rows.put("prepared_margins", margins.size());
		rows.put("kept_polynomials_with_derivatives", totals[0]);
		rows.put("kept_terms", totals[1]);
		rows.put("kept_coefficient_bits", totals[2]);
		rows.put("substitutions_computed", memory.getSubstitutions());
		return rows;
	}

	private static void count(CertificatePolynomial polynomial,
			Map<CertificatePolynomial, Boolean> seen, long[] totals) throws Exception {
		if (polynomial == null || seen.put(polynomial, Boolean.TRUE) != null) {
			return;
		}
		totals[0]++;
		for (ExactRational2D value : polynomial.getTerms().values()) {
			totals[1]++;
			totals[2] += value.bitLength();
		}
		Field partials = CertificatePolynomial.class.getDeclaredField("partials");
		partials.setAccessible(true);
		CertificatePolynomial[] kept = (CertificatePolynomial[]) partials.get(polynomial);
		if (kept != null) {
			for (CertificatePolynomial partial : kept) {
				count(partial, seen, totals);
			}
		}
	}

	private static double median(List<Double> values) {
		List<Double> sorted = new ArrayList<>(values);
		Collections.sort(sorted);
		return sorted.get(sorted.size() / 2);
	}
}
