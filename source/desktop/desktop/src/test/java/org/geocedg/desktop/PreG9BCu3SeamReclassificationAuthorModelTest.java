/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.constructive.AuthorityAdoptionImpact;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.intersection.IntersectionExistenceEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 increment E3 on the author model ({@code InterCilConoOblique.cedg}, without
 * {@code e_2}) after the explicit adoption of {@code constructive-authority/v1}: the inversion of
 * {@code pins.B08_B09} (D-CU3-10 under D-AUT-11: the seam reclassified by parity, with the exact
 * decision at {@code pi}), the contacts of {@code preserve.contacts} unchanged, and V-B-NOISE-1:
 * with {@code alpha_cone} 35 degrees, and with {@code D_1} at half its distance from {@code H},
 * no root of the rounding noise stays published around the contacts at {@code t = 0}. The
 * baseline of {@code CU0} keeps the pin as historical evidence, and under the legacy policy
 * nothing changes ({@code PreG9BCu0LocusReferenceTest}). Validation matrix rows V-B-2, V-CONF-5,
 * V-B-NOISE-1 and section 3.9 (inversion of {@code pins.B08_B09}, {@code preserve.contacts}).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3SeamReclassificationAuthorModelTest {

	private static final String ALPHA = "\u03b1_{cone}";
	private static final List<String> PAIRS = List.of("Intersect(o_1,h_2)", "Intersect(e_1,n)",
			"Intersect(e_1,c_1)");

	@TempDir
	Path work;

	@Test
	void theSeamOfTheAdoptedModelIsTheFoldContactAtPi() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> values = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
			Map<String, Object> contacts = PreG9BCu0References.contacts(app);
			Map<String, Object> rows = new LinkedHashMap<>();
			rows.put("contacts", contacts);
			rows.put("evidence", evidence(app));
			return rows;
		});
		JSONObject pinned = baseline.getJSONObject("pins").getJSONObject("B08_B09");
		JSONArray seamPins = pinned.getJSONArray("Intersect(e_1,c_1)");
		assertEquals("TRANSVERSE_ESTABLISHED", seamPins.getJSONObject(0)
				.getString("contact_class"), "historical pin");
		assertTrue(seamPins.getJSONObject(0).getBoolean("admissible"), "historical pin");
		Map<String, Object> contacts = castMap(values.get("contacts"));
		Map<?, ?> seam = (Map<?, ?>) ((List<?>) contacts.get("Intersect(e_1,c_1)")).get(0);
		assertEquals("PERIODIC_SEAM", seam.get("domain_location"));
		assertEquals("TANGENT_ESTABLISHED", seam.get("contact_class"), PreG9BCu0Baseline
				.inversionMessage("B08_B09", "CU3", "the seam root of Intersect(e_1, c_1) is"
						+ " the fold contact at pi"));
		assertTrue((Boolean) seam.get("admissible"), PreG9BCu0Baseline.inversionMessage(
				"B08_B09", "CU3-B1", "the seam root is admitted by its fold-anchored token"));
		for (String pair : PAIRS) {
			List<?> rows = (List<?>) contacts.get(pair);
			assertEquals(((JSONArray) pinned.get(pair)).length(), rows.size(), pair);
			// CU3-B1 anchors the contacts of e_1 at its folds; the regular tangency of o_1 and
			// h_2 belongs to CU3-B2, which is not authorized.
			boolean fold = pair.startsWith("Intersect(e_1,");
			for (Object row : rows) {
				assertEquals("TANGENT_ESTABLISHED", ((Map<?, ?>) row).get("contact_class"),
						pair);
				assertEquals(fold, ((Map<?, ?>) row).get("admissible"),
						pair + ": a contact is materialized only when anchored at a fold");
			}
			PreG9BCu0Baseline.assertMatches("preserve.contacts." + pair,
					baseline.getJSONObject("preserve").getJSONObject("contacts").get(pair),
					PreG9BCu0References.contactPoints(contacts).get(pair),
					PreG9BCu0References.POINT_TOLERANCE);
		}
		Map<String, Object> evidence = castMap(values.get("evidence"));
		assertEquals(List.of("EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT TWO"),
				evidence.get("u_1"), "o_1 has no fold: a tangency of order two");
		assertEquals(List.of("EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT FOLD_CONTACT"),
				evidence.get("cu0X1"));
		assertEquals(List.of("EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT FOLD_CONTACT",
				"EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT FOLD_CONTACT"), evidence.get("cu0X2"),
				"the seam at pi and the contact at 0");
	}

	@Test
	void withAlphaThirtyFiveOnlyTheTwoFoldContactsStayPublished() throws Exception {
		Map<String, Object> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> values = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.setNumber(app, ALPHA, Math.toRadians(35));
			GeoLocusIntersectionResult seam = (GeoLocusIntersectionResult)
					PreG9BR6PlusF3Fixtures.must(app, "cu3N1=Intersect(e_1,c_1)");
			values.put("legacy near zero", nearZero(seam));
			values.put("legacy admitted", admitted(seam));
			List<String> report = new ArrayList<>();
			for (AuthorityAdoptionImpact.Entry entry : AuthorityPolicyAdoption.report(
					app.getKernel().getConstruction()).getEntries()) {
				if ("cu3N1".equals(entry.getSubject())) {
					report.add(entry.getAfter() + " " + entry.getCause());
				}
			}
			values.put("report", report);
			AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
			GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) app.getKernel()
					.lookupLabel("cu3N1");
			values.put("adopted near zero", nearZero(adopted));
			values.put("adopted admitted", admitted(adopted));
			values.put("evidence", evidence(app).get("cu3N1"));
			return values;
		});
		assertTrue((Integer) rows.get("legacy near zero") > 1,
				"H-CU3-1 under the legacy policy: noise around t = 0 " + rows);
		assertEquals(2, rows.get("legacy admitted"), "two noise roots are admitted: " + rows);
		assertEquals(List.of("NOT_ESTABLISHED CONTRADICTED_BY_CERTIFIED_EVIDENCE",
				"NOT_ESTABLISHED CONTRADICTED_BY_CERTIFIED_EVIDENCE"), rows.get("report"),
				"V-AUT-ADOPT-8: the report names the two withdrawals before they are applied");
		assertEquals(1, rows.get("adopted near zero"), "V-B-NOISE-1: " + rows);
		assertEquals(2, rows.get("adopted admitted"),
				"CU3-B1: the two fold contacts are admitted through their anchored tokens " + rows);
		assertEquals(List.of("EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT FOLD_CONTACT",
				"EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT FOLD_CONTACT"), rows.get("evidence"),
				"only the seam at pi and the contact at 0: " + rows);
	}

	@Test
	void withHalfTheDistanceOfDOneOnlyTheTangencyStaysPublished() throws Exception {
		Map<String, Object> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> values = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			GeoPoint d1 = (GeoPoint) app.getKernel().lookupLabel("D_1");
			GeoPoint h = (GeoPoint) app.getKernel().lookupLabel("H");
			d1.setCoords(h.getInhomX() + 0.5 * (d1.getInhomX() - h.getInhomX()),
					h.getInhomY() + 0.5 * (d1.getInhomY() - h.getInhomY()), 1);
			d1.updateRepaint();
			values.put("legacy near zero", nearZero((GeoLocusIntersectionResult) app.getKernel()
					.lookupLabel("u_1")));
			AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
			values.put("adopted near zero", nearZero((GeoLocusIntersectionResult) app
					.getKernel().lookupLabel("u_1")));
			values.put("evidence", evidence(app).get("u_1"));
			return values;
		});
		assertTrue((Integer) rows.get("legacy near zero") > 1,
				"H-CU3-1 under the legacy policy: noise around t = 0 " + rows);
		assertEquals(1, rows.get("adopted near zero"), "V-B-NOISE-1: " + rows);
		assertEquals(List.of("EXISTENCE_EXACT CONTACT_EXISTENCE_EXACT TWO"),
				rows.get("evidence"), rows.toString());
	}

	/** @return the number of solutions whose token is admissible */
	private static int admitted(GeoLocusIntersectionResult result) {
		int count = 0;
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			count += result.isPointAdmissible(solution.getIdentity().getRootToken()) ? 1 : 0;
		}
		return count;
	}

	/** @return the number of published solutions within {@code 1e-6} of {@code t = 0} */
	private static int nearZero(GeoLocusIntersectionResult result) {
		int count = 0;
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			count += Math.abs(solution.getRevisionEvidence().getSemanticParameter()) < 1E-6 ? 1
					: 0;
		}
		return count;
	}

	/**
	 * @return per intersection result of the document, the grade, contact evidence and order of
	 *         contact of each solution, in the order of the abscissas
	 */
	private static Map<String, Object> evidence(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String label : List.of("u_1", "cu0X1", "cu0X2", "cu3N1")) {
			if (!(app.getKernel().lookupLabel(label) instanceof GeoLocusIntersectionResult)) {
				continue;
			}
			List<LocusIntersectionSolution2D> solutions = new ArrayList<>(
					((GeoLocusIntersectionResult) app.getKernel().lookupLabel(label))
							.getIntersectionResult().getFiniteSolutions());
			solutions.sort((a, b) -> Double.compare(a.getEvaluatedPoint().getX(),
					b.getEvaluatedPoint().getX()));
			List<String> list = new ArrayList<>();
			for (LocusIntersectionSolution2D solution : solutions) {
				IntersectionExistenceEvidence2D evidence = solution.getRevisionEvidence()
						.getExistenceEvidence();
				list.add(evidence.getGrade() + " " + evidence.getContactEvidence() + " "
						+ evidence.getContactOrder());
			}
			rows.put(label, list);
		}
		return rows;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}
}
