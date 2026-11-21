/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicy;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair F6: the isolated copy of a reconstruction slice decides its relations under
 * the authority of the document that owns the slice ({@code
 * locus-v2-constructive-authority.md} section 4: one document has one authority). A locus that
 * consumes {@code Point(L, D)}, which exists only under the constructive authority, evaluates
 * in its slice as its traced point does in the live document.
 */
final class PostR1IsolatedEvaluationAuthorityTest extends G9U0PublicSurfaceTestBase {
	private static final double[] PARAMETERS = {-2.5, -1.0, 0.3, 1.7, 3.0};

	@Test
	void aLocusOverAStructuralPointEvaluatesAsTheLiveDocument() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		buildStructuralPoint();
		GeoLocusV2 consumer = add("locA=LocusV2(Sa,N)");
		assertNotNull(consumer);
		ReconstructibleLocusEvaluator2D evaluator = evaluator(consumer);
		assertTrue(evaluator.getEffectiveAuthority().isConstructive());
		assertTrue(evaluator.isReconstructionFaithful());
		assertTrue(evaluator.getEvaluatorSignature().contains(
				"|authority=" + AuthorityPolicy.CONSTRUCTIVE_AUTHORITY));

		String before = getApp().getXML();
		List<double[]> evaluated = new ArrayList<>();
		for (double parameter : PARAMETERS) {
			LocusEvaluation2D evaluation = consumer.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference());
			assertTrue(evaluation.isValid(), parameter + ": " + evaluation.getStatus() + " "
					+ evaluation.getDiagnostic());
			evaluated.add(new double[] {evaluation.getPoint().getX(),
					evaluation.getPoint().getY()});
		}
		assertEquals(before, getApp().getXML(), "evaluation never changes the live document");

		long revision = consumer.getSemanticRevision();
		GeoPoint traced = point("Sa");
		for (int index = 0; index < PARAMETERS.length; index++) {
			moveDriver(getKernel(), PARAMETERS[index]);
			assertTrue(traced.isDefined(), "live at " + PARAMETERS[index]);
			assertNull(resolutionCause(traced), "resolved, no typed cause");
			assertEquals(traced.getInhomX(), evaluated.get(index)[0], 1E-12);
			assertEquals(traced.getInhomY(), evaluated.get(index)[1], 1E-12);
		}
		assertEquals(revision, consumer.getSemanticRevision(), "driver moves keep the revision");
		assertSame(evaluator, evaluator(consumer));
	}

	@Test
	void theLegacyAuthorityAndItsSignatureAreUnchanged() {
		buildSource();
		GeoLocusV2 source = locus("loc");
		ReconstructibleLocusEvaluator2D evaluator = evaluator(source);
		assertFalse(evaluator.getEffectiveAuthority().isConstructive());
		assertEquals(AuthorityPolicy.LEGACY_REPRESENTATION, evaluator.getEffectiveAuthority());
		assertFalse(evaluator.getEvaluatorSignature().contains("authority="));
		assertNull(getConstruction().getSpatialIdentityRegistry().getDocumentAuthorityRecord());
		// A form that requires the constructive authority still fails closed, typed.
		add("G=(1,0.5)");
		add("g=Circle(G,1)");
		add("N=Point(g)");
		add("Pz=(8,0)");
		add("lT=Line(Pz,N)");
		add("Da=Intersect(c,lT,2)");
		AssertionError refusal = assertThrows(AssertionError.class,
				() -> add("Sa=Point(loc,Da)"));
		assertTrue(String.valueOf(refusal.getMessage()).contains(
				AuthorityPolicyAdoption.REQUIRED_CAUSE), refusal.getMessage());
		assertNull(getKernel().lookupLabel("Sa"));
	}

	@Test
	void adoptionAndItsUndoRebuildTheSliceUnderTheAuthorityOfTheDocument() {
		getApp().setUndoActive(true);
		buildSource();
		getApp().storeUndoInfo();
		GeoLocusV2 source = locus("loc");
		ReconstructibleLocusEvaluator2D legacy = evaluator(source);
		long revision = source.getSemanticRevision();
		List<String> reference = evaluations(source);

		AuthorityPolicyAdoption.adopt(getConstruction());
		source = locus("loc");
		ReconstructibleLocusEvaluator2D constructive = evaluator(source);
		assertNotSame(legacy, constructive, "another evaluator after the adoption");
		assertTrue(constructive.getEffectiveAuthority().isConstructive());
		assertTrue(source.getSemanticRevision() > revision, "the adoption publishes a revision");
		assertEquals(reference, evaluations(source), "the traced function is the same");

		getKernel().undo();
		source = locus("loc");
		assertFalse(AuthorityPolicy.of(getConstruction()).isConstructive());
		assertFalse(evaluator(source).getEffectiveAuthority().isConstructive());
		assertEquals(reference, evaluations(source));
	}

	@Test
	void reopeningRebuildsTheContextFromTheDocument() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		buildStructuralPoint();
		GeoLocusV2 consumer = add("locA=LocusV2(Sa,N)");
		List<String> reference = evaluations(consumer);
		assertTrue(reference.get(0).contains("VALID"), reference.toString());

		AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reopened.setXML(getApp().getXML(), true);
		Kernel kernel = reopened.getKernel();
		assertTrue(AuthorityPolicy.of(kernel.getConstruction()).isConstructive());
		GeoLocusV2 loaded = (GeoLocusV2) kernel.lookupLabel("locA");
		assertNotNull(loaded);
		assertTrue(evaluator(loaded).getEffectiveAuthority().isConstructive());
		assertEquals(reference, evaluations(loaded));
	}

	@Test
	void aRelationOutsideTheClassStaysRefusedInTheLiveDocumentAndInTheSlice() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		buildSource();
		// A rotation by a free angle is a declared boundary of the constructive class.
		add("Az=(0.2,0.1)");
		add("Cz=(4,0.3)");
		add("al=50*pi/180");
		add("Cr=Rotate(Cz,al,Az)");
		add("lR=Line(Az,Cr)");
		add("Db=Intersect(c,lR,1)");
		GeoPoint refused = add("Sb=Point(loc,Db)");
		assertNotNull(refused);
		assertFalse(refused.isDefined());
		assertEquals(StructuralPointResolver.Cause.CORRESPONDENCE_NOT_CERTIFIED,
				resolutionCause(refused));
	}

	private void buildSource() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("E=Point(c)");
		add("Q=(5,1)");
		add("F=Midpoint(E,Q)");
		assertNotNull(add("loc=LocusV2(F,E)"));
	}

	private void buildStructuralPoint() {
		buildSource();
		add("G=(1,0.5)");
		add("g=Circle(G,1)");
		add("N=Point(g)");
		add("Pz=(8,0)");
		add("lT=Line(Pz,N)");
		add("Da=Intersect(c,lT,2)");
		GeoPoint structural = add("Sa=Point(loc,Da)");
		assertNotNull(structural);
		assertTrue(structural.isDefined());
	}

	private static void moveDriver(Kernel kernel, double angle) {
		GeoPoint driver = (GeoPoint) kernel.lookupLabel("N");
		driver.setCoords(1 + Math.cos(angle), 0.5 + Math.sin(angle), 1.0);
		driver.updateCascade();
	}

	private GeoLocusV2 locus(String label) {
		return (GeoLocusV2) getKernel().lookupLabel(label);
	}

	private GeoPoint point(String label) {
		return (GeoPoint) getKernel().lookupLabel(label);
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus) {
		return (ReconstructibleLocusEvaluator2D) locus.getSemanticDefinition()
				.getEvaluatorCapability();
	}

	private static StructuralPointResolver.Cause resolutionCause(GeoPoint point) {
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution().getCause();
	}

	private static List<String> evaluations(GeoLocusV2 locus) {
		List<String> rows = new ArrayList<>();
		for (double parameter : PARAMETERS) {
			LocusEvaluation2D evaluation = locus.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference());
			rows.add(parameter + ":" + evaluation.getStatus() + (evaluation.isValid()
					? ":" + Double.toHexString(evaluation.getPoint().getX()) + ","
							+ Double.toHexString(evaluation.getPoint().getY())
					: ""));
		}
		return rows;
	}
}
