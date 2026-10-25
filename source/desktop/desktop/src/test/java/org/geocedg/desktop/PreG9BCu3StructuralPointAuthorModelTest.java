/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityAdoptionImpact;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.RelationIntegrity;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 increment E1 on the author model ({@code InterCilConoOblique.cedg}, without
 * {@code e_2}): the explicit adoption of {@code constructive-authority/v1} keeps every durable
 * identity; the five mandatory contacts are created with {@code P in L} {@code STRICT} by
 * transfer and, since increment E2, {@code P in r} {@code STRICT} by its constructive identity
 * certificate, with the order of contact and the fold of {@code CU2} at the stationary contacts
 * (E1 admitted {@code P in r} {@code RELAXED} by {@code SUBSTITUTE_GENERIC}); the exact
 * correspondence follows the inherited permutation {@code 1,0} of the outputs of
 * {@code Intersect(p, i)}, under which {@code K} is output 1, unlike the replica; and
 * {@code Point(o_1, K)} is the author's workaround {@code N_1 = Point(o_1, text2, v_1)} without
 * the manual selector. Matrix rows V-MAND-1 to V-MAND-4, V-A-21, V-DYN-1 ({@code excCyl}),
 * V-DYN-3, V-AUT-ADOPT-3, V-AUT-CIC-1 and V-AUT-TRF-2.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3StructuralPointAuthorModelTest {

	private static final double CLOSE = 1E-9;
	private static final List<String> CONTACTS = List.of("Q1=Intersect(e_1,n,K)",
			"Q2=Intersect(e_1,c_1,K)", "Q3=Intersect(o_1,h_2,K)", "Q4=Intersect(e_1,c_1,J)",
			"Q5=Intersect(e_1,r,J)");

	@TempDir
	Path work;

	@Test
	void theMandatoryContactsOfTheAuthorModel() throws Exception {
		Map<String, Object> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Construction construction = app.getKernel().getConstruction();
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("legacy", !AuthorityPolicyAdoption.isConstructive(construction));
			Map<String, String> before = identities(construction);
			AuthorityAdoptionImpact impact = AuthorityPolicyAdoption.adopt(construction);
			values.put("constructive", AuthorityPolicyAdoption.isConstructive(construction));
			values.put("identities kept", before.equals(identities(construction)));
			values.put("impact", impact.toString());
			GeoPoint k = (GeoPoint) construction.lookupLabel("K");
			AlgoElement source = k.getParentAlgorithm();
			values.put("source", source.getClass().getSimpleName());
			values.put("K output", outputIndex(source, k));
			values.put("permutation", source instanceof AlgoIntersectLineConic
					? List.of(((AlgoIntersectLineConic) source).getSelectionPermutation()[0],
							((AlgoIntersectLineConic) source).getSelectionPermutation()[1])
					: List.of());
			GeoPoint m1 = (GeoPoint) construction.lookupLabel("M_1");
			values.put("M_1", new double[] {m1.getInhomX(), m1.getInhomY()});
			GeoPoint n1 = (GeoPoint) construction.lookupLabel("N_1");
			values.put("N_1", new double[] {n1.getInhomX(), n1.getInhomY()});
			// The model has a point P: fresh labels, never a redefinition.
			values.put("P", describe(resolution(PreG9BR6PlusF3Fixtures.must(app,
					"SPe=Point(e_1,K)"))));
			values.put("P_o", describe(resolution(PreG9BR6PlusF3Fixtures.must(app,
					"SPo=Point(o_1,K)"))));
			for (String contact : CONTACTS) {
				values.put(contact, describe(resolution(PreG9BR6PlusF3Fixtures.must(app,
						contact))));
			}
			double exc = ((org.geogebra.common.kernel.geos.GeoNumeric) construction
					.lookupLabel("excCyl")).getValue();
			for (double value : new double[] {2.9, 3.7}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				for (String contact : CONTACTS) {
					String label = contact.substring(0, 2);
					values.put("excCyl=" + value + " " + label, describe(resolution(
							construction.lookupLabel(label))));
				}
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", exc);
			return values;
		});
		assertEquals(true, rows.get("legacy"), "V-AUT-ADOPT-3: the model opens legacy");
		assertEquals(true, rows.get("constructive"), rows.toString());
		assertEquals(true, rows.get("identities kept"),
				"V-AUT-ADOPT-3: the adoption changes no durable identity");
		assertEquals("AlgoIntersectLineConic", rows.get("source"));
		assertEquals(1, rows.get("K output"), "V-A-21: in the model K is output 1");
		assertEquals(List.of(1, 0), rows.get("permutation"), rows.toString());
		double[] m1 = (double[]) rows.get("M_1");
		Object[] point = (Object[]) rows.get("P");
		assertDefined(point, "STRICT", null, "P");
		assertClose(m1, point, "V-A-21: J_1(K) = M_1 under the permutation of the model");
		Object[] onO = (Object[]) rows.get("P_o");
		assertDefined(onO, "STRICT", null, "Point(o_1, K)");
		assertClose((double[]) rows.get("N_1"), onO,
				"V-DYN-3: the author's workaround N_1, without the manual selector");
		for (String contact : CONTACTS) {
			Object[] resolved = (Object[]) rows.get(contact);
			assertDefined(resolved, "STRICT", "STRICT", contact);
			assertEquals("CONSTRUCTIVE_IDENTITY_CERTIFICATE", resolved[6], contact);
			boolean regular = contact.startsWith("Q3");
			assertEquals(regular ? "TANGENT" : "STATIONARY", resolved[7], contact);
			if (!regular) {
				assertEquals(true, resolved[8], "V-AUT-TRF-2: the fold of CU2 by structure, "
						+ contact + ": " + resolved[5]);
			}
			String label = contact.substring(0, 2);
			for (double value : new double[] {2.9, 3.7}) {
				Object[] swept = (Object[]) rows.get("excCyl=" + value + " " + label);
				assertDefined(swept, "STRICT", "STRICT", "V-DYN-1 excCyl=" + value + " "
						+ contact);
				assertEquals((double) resolved[3], (double) swept[3], 0, "outside the cone");
				assertEquals((double) resolved[4], (double) swept[4], 0, "outside the cone");
			}
		}
		assertClose(m1, (Object[]) rows.get(CONTACTS.get(0)), "V-MAND-1");
		assertClose(m1, (Object[]) rows.get(CONTACTS.get(1)), "V-MAND-2");
		assertClose((double[]) rows.get("N_1"), (Object[]) rows.get(CONTACTS.get(2)),
				"V-MAND-3: N_1; its order of contact is decided by exact jets in E2");
		Object[] seam = (Object[]) rows.get(CONTACTS.get(3));
		Object[] seamOnR = (Object[]) rows.get(CONTACTS.get(4));
		assertEquals((double) seam[3], (double) seamOnR[3], CLOSE, "V-MAND-4: one point");
		assertEquals((double) seam[4], (double) seamOnR[4], CLOSE, "V-MAND-4: one point");
		assertFalse(((String) rows.get("impact")).isEmpty());
	}

	private static Map<String, String> identities(Construction construction) {
		Map<String, String> identities = new LinkedHashMap<>();
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			if (geo.isLabelSet()) {
				identities.put(geo.getLabelSimple(), String.valueOf(construction
						.getSpatialIdentityRegistry().getPersistentGeoId(geo)));
			}
		}
		return identities;
	}

	private static int outputIndex(AlgoElement algorithm, GeoElement geo) {
		for (int index = 0; index < algorithm.getOutputLength(); index++) {
			if (algorithm.getOutput(index) == geo) {
				return index;
			}
		}
		return -1;
	}

	private static StructuralPointResolution resolution(GeoElement point) {
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}

	/**
	 * @return definedness, membership and incidence integrity, position, description, evidence
	 *         of the incidence, order of contact and whether the fold of {@code CU2} transfers
	 */
	private static Object[] describe(StructuralPointResolution resolution) {
		return new Object[] {resolution.isDefined(),
				resolution.getMembership() == null ? null
						: resolution.getMembership().getIntegrity().name(),
				resolution.getIncidence() == null ? null
						: resolution.getIncidence().getIntegrity().name(),
				resolution.isDefined() ? resolution.getX() : Double.NaN,
				resolution.isDefined() ? resolution.getY() : Double.NaN,
				resolution.toString(),
				resolution.getIncidence() == null ? null
						: resolution.getIncidence().getEvidence().name(),
				resolution.getCertificate() == null
						|| resolution.getCertificate().getContactOrder() == null ? null
								: resolution.getCertificate().getContactOrder().name(),
				resolution.getFold() == null ? null : resolution.getFold().isTransferred()};
	}

	private static void assertDefined(Object[] row, String membership, String incidence,
			String message) {
		assertTrue((Boolean) row[0], message + ": " + row[5]);
		assertEquals(membership, row[1], message + ": " + row[5]);
		assertEquals(incidence, row[2], message + ": " + row[5]);
		if (incidence != null) {
			assertEquals(RelationIntegrity.STRICT.name(), incidence);
		}
	}

	private static void assertClose(double[] expected, Object[] row, String message) {
		assertEquals(expected[0], (double) row[3], CLOSE, message + ": " + row[5]);
		assertEquals(expected[1], (double) row[4], CLOSE, message + ": " + row[5]);
	}
}
