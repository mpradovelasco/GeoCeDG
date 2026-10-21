/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T8 on the author model: structural lineage of the shadow certificates
 * and resolution of the persisted selector of {@code N_1} ({@code
 * locus-v2-existence-components.md} section 15.6; certification design section 7, decision
 * E-3 revised). Matrix rows V-LIN-1, V-LIN-2, V-LIN-3, V-LIN-5, V-LIN-6 and V-E2-7. The
 * consumers read the certificates from step T9 and publish them at step T11.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelLineageTest {
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	private static final List<String> LOCI = List.of("o", "v", "w", "a_1", "e_1", "o_1", "e_2");
	private static final String CLOSURE = PreG9BCu0Corpus.MAIN + "/component/periodic-closure";

	@TempDir
	Path work;

	/**
	 * V-LIN-5: the model, saved before PRE-G9B-CU2, is certified anew when it is opened, and
	 * its first certificates relate to nothing earlier: no parent is guessed.
	 */
	@Test
	void theFirstCertificatesAfterOpeningGuessNoLineage() throws Exception {
		Map<String, String> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, String> values = new LinkedHashMap<>();
			for (String label : LOCI) {
				values.put(label, lineage(algorithm(app, label)));
			}
			return values;
		});
		for (String label : LOCI) {
			assertTrue(row.get(label).startsWith("APPEARED[]->["), label + " " + row);
		}
		assertEquals("APPEARED[]->[" + CLOSURE + "]", row.get("o"), row.toString());
	}

	/**
	 * V-LIN-1 and V-LIN-2: in the first family the closed component splits into the bite
	 * arc, whose lineage key stays the same while its ends move from excCyl 3.7 to 3.85 and
	 * 3.9, and merges back at 2.9; every transition is typed, with its parent and child
	 * keys, and no key carries a coordinate.
	 */
	@Test
	void movingEndsKeepTheLineageAndTheBiteIsATypedTransition() throws Exception {
		Map<String, List<String>> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			Map<String, List<String>> values = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				values.put(label, new ArrayList<>());
				algorithm(app, label).getHybridExistenceCertificate();
			}
			for (double excCyl : new double[] {3.7, 3.85, 3.9, 2.9}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				for (String label : FIRST_FAMILY) {
					AlgoDependentPointLocusV2 algorithm = algorithm(app, label);
					LocusExistenceStructure2D structure =
							algorithm.getHybridExistenceCertificate().getStructure();
					values.get(label).add(excCyl + "|" + lineage(algorithm) + "|"
							+ LocusComponentLineage2D.keys(structure) + "|"
							+ structure.getContinuousValidComponents().get(0).getStart());
				}
			}
			return values;
		});
		for (String label : FIRST_FAMILY) {
			List<String> steps = row.get(label);
			String[] bite = steps.get(0).split("\\|");
			assertTrue(bite[1].startsWith("SPLIT[" + CLOSURE + "]->["), steps.toString());
			assertTrue(!bite[2].contains("0x") && bite[2].contains("DISCRIMINANT"),
					steps.toString());
			for (int index = 1; index <= 2; index++) {
				String[] moved = steps.get(index).split("\\|");
				assertEquals("UNCHANGED[]->[]", moved[1], steps.toString());
				assertEquals(bite[2], moved[2], "V-LIN-1 " + steps);
				assertTrue(!moved[3].equals(bite[3]), "the ends move: " + steps);
			}
			String[] back = steps.get(3).split("\\|");
			assertTrue(back[1].startsWith("MERGED[") && back[1].endsWith("]->[" + CLOSURE + "]"),
					steps.toString());
			assertEquals("[" + CLOSURE + "]", back[2], steps.toString());
		}
	}

	/**
	 * V-LIN-3: the persisted selector {@code text2} of {@code N_1} cites the component
	 * {@code [-pi, pi)} of {@code o_1}; it resolves by equality of the decomposition with the
	 * certified closed component, with containment as validation, the address of {@code N_1}
	 * does not change, and saving keeps {@code text2} byte for byte.
	 */
	@Test
	void theSelectorOfN1ResolvesByEqualityOfTheDecomposition() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			GeoText text2 = (GeoText) app.getKernel().lookupLabel("text2");
			String retained = LocusPrincipalBranchState2D.decode(text2.getTextString())
					.getComponentLineageKey();
			AlgoSemanticLocusPoint2D n1 = (AlgoSemanticLocusPoint2D) app.getKernel()
					.lookupLabel("N_1").getParentAlgorithm();
			double address = n1.getCurrentSemanticAddress().getCanonicalParameter();
			AlgoDependentPointLocusV2 o1 = algorithm(app, "o_1");
			LocusComponentSelectorResolver2D.Resolution resolution =
					LocusComponentSelectorResolver2D.resolve(retained,
							o1.getHybridExistenceCertificate().getStructure(),
							o1.getHybridLineage(), address);
			values.put("retained", retained);
			values.put("status", resolution.getStatus().name());
			values.put("key", resolution.getLineageKey());
			values.put("address", n1.getCurrentSemanticAddress().getCanonicalParameter());
			values.put("text2.saved", text2Expression(app.getXML()));
			values.put("text2.original", text2Expression(PreG9BCu0Corpus.constructionXml(
					PreG9BCu0Corpus.CEDG)));
			return values;
		});
		assertEquals(PreG9BCu0Corpus.MAIN + "/component/-0x1.921fb54442d18p1/"
				+ "0x1.921fb54442d18p1/closed/open", row.get("retained"), row.toString());
		assertEquals("RESOLVED_BY_DECOMPOSITION_EQUALITY", row.get("status"), row.toString());
		assertEquals(CLOSURE, row.get("key"), row.toString());
		assertEquals(0.0, row.get("address"), row.toString());
		assertEquals(row.get("text2.original"), row.get("text2.saved"), row.toString());
	}

	/**
	 * V-LIN-6: a document whose first-family locus bites when it is certified, with a
	 * selector citing the complete component and its parameter inside the new arc: the
	 * selector stays unresolved with a typed cause, because the decomposition changed with
	 * no recorded transition from the cited component; containment creates nothing.
	 */
	@Test
	void containmentCreatesNoCorrespondence() throws Exception {
		Map<String, String> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			Map<String, String> values = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				AlgoDependentPointLocusV2 algorithm = algorithm(app, label);
				Result result = algorithm.getHybridExistenceCertificate();
				LocusExistenceStructure2D structure = result.getStructure();
				String legacy = LocusComponentLineage2D.create(PreG9BCu0Corpus.MAIN,
						structure.getCanonicalDomain());
				values.put(label, lineage(algorithm).substring(0, 8) + "|"
						+ structure.getContinuousValidComponents().get(0).contains(2.5) + "|"
						+ LocusComponentSelectorResolver2D.resolve(legacy, structure,
								algorithm.getHybridLineage(), 2.5).getStatus());
			}
			return values;
		});
		for (String label : FIRST_FAMILY) {
			assertEquals("APPEARED|true|NO_CORRESPONDENCE", row.get(label), row.toString());
		}
	}

	/**
	 * V-E2-7: dragging the driver changes neither the revision nor the event nor the
	 * component of {@code e_2}; moving the origin through {@code v_1} publishes a new revision
	 * whose component keeps its lineage, with its event at the new address.
	 */
	@Test
	void theEventOfE2KeepsItsLineageWhenTheOriginMoves() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			AlgoDependentPointLocusV2 algorithm = algorithm(app, "e_2");
			Result before = algorithm.getHybridExistenceCertificate();
			long revision = e2.getSemanticRevision();
			PreG9BCu0Corpus.movePathPoint(app, "E_1", 1.0);
			values.put("drag", (before == algorithm.getHybridExistenceCertificate()) + "|"
					+ (revision == e2.getSemanticRevision()));
			PreG9BCu0Corpus.setNumber(app, "v_1", 0.5);
			Result after = algorithm.getHybridExistenceCertificate();
			values.put("revision", e2.getSemanticRevision() > revision);
			values.put("keys", LocusComponentLineage2D.keys(before.getStructure()).equals(
					LocusComponentLineage2D.keys(after.getStructure())));
			values.put("key", LocusComponentLineage2D.keys(after.getStructure()).get(0));
			values.put("lineage", lineage(algorithm));
			values.put("ends", before.getStructure().getContinuousValidComponents().get(0)
					.getStart() + "->" + after.getStructure().getContinuousValidComponents()
							.get(0).getStart());
			return values;
		});
		assertEquals("true|true", row.get("drag"), row.toString());
		assertEquals(true, row.get("revision"), row.toString());
		assertEquals(true, row.get("keys"), row.toString());
		assertTrue(((String) row.get("key")).endsWith("-METRIC_INPUT/ORIGIN_WRAP/end"),
				row.toString());
		assertEquals("UNCHANGED[]->[]", row.get("lineage"), row.toString());
		assertEquals("0.0->0.5", row.get("ends"), row.toString());
	}

	private static String text2Expression(String xml) {
		Matcher matcher = Pattern.compile("<expression label=\"text2\" exp=\"[^\"]*\"")
				.matcher(xml);
		return matcher.find() ? matcher.group() : "missing";
	}

	private static String lineage(AlgoDependentPointLocusV2 algorithm) {
		StringBuilder text = new StringBuilder();
		for (LocusLineage2D lineage : algorithm.getHybridLineage()) {
			text.append(text.length() == 0 ? "" : " ").append(lineage.getTransition())
					.append(lineage.getParentKeys()).append("->").append(lineage.getChildKeys());
		}
		return text.toString();
	}

	private static AlgoDependentPointLocusV2 algorithm(AppGeoCeDG app, String label) {
		return (AlgoDependentPointLocusV2) PreG9BCu0Corpus.locus(app, label)
				.getParentAlgorithm();
	}
}
