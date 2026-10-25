/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityAdoptionImpact;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.RelationIntegrity;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-CU3 increment E1 on the Desktop: the explicit adoption event that a form of
 * {@code CU3} offers in a document governed by the legacy policy (validation matrix rows
 * V-AUT-ADOPT-1 and V-AUT-ADOPT-2), with its impact report computed before anything is applied
 * and one undo point together with the input that offered it; the typed refusal when the offer is
 * declined; and the integrity of a structural point in the
 * definition inspector, with the premises that are not established and the declared error of
 * its position apart from it (V-AUT-INT-2), and, since increment E2, the evidence of a
 * constructive identity certificate.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3ConstructiveAdoptionDesktopTest {

	private static final String[] LOCUS = {"O=(0,0)", "c=Circle(O,3)", "P=Point(c)",
			"Q=Midpoint(O,P)", "L=LocusV2(Q,P)", "D=Intersect(c,yAxis,2)",
			"g=Line((0,1.5),(1,1.5))"};

	@Test
	void anAcceptedOfferAdoptsThePolicyAndCreatesTheCommand() throws Exception {
		AppGeoCeDG app = locus();
		final UndoManagerD undo = baseline(app);
		List<AuthorityAdoptionImpact> offers = new ArrayList<>();
		app.setConstructiveAdoptionPrompt((owner, impact) -> {
			offers.add(impact);
			assertFalse(AuthorityPolicyAdoption.isConstructive(
					owner.getKernel().getConstruction()), "the report is computed first");
			return true;
		});
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND[]> result = new AtomicReference<>();

		GeoCeDGAlgebraInputSubmission.submit(app, "X=Intersect(L,g,D)", info(), errors,
				result::set);

		assertEquals(1, offers.size(), "V-AUT-ADOPT-1: one explicit event");
		assertTrue(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
		assertEquals("", errors.getErrors());
		assertNotNull(result.get());
		StructuralPointResolution resolution = resolution(lookup(app, "X"));
		assertTrue(resolution.isDefined(), String.valueOf(resolution));
		assertEquals(RelationIntegrity.STRICT, resolution.getIntegrity());
		// The adoption and the input are one transaction: one Undo returns to the earlier
		// policy without the point, and Redo brings both back.
		await(undo::undoPossible);
		app.getKernel().undo();
		await(() -> app.getKernel().lookupLabel("X") == null);
		assertFalse(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
		assertFalse(undo.undoPossible(), "one undo point");
		app.getKernel().redo();
		await(() -> app.getKernel().lookupLabel("X") != null);
		assertTrue(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
	}

	@Test
	void anAdoptionWhoseInputEndsWithoutAResultKeepsItsUndoPoint() throws Exception {
		AppGeoCeDG app = locus();
		UndoManagerD undo = baseline(app);
		// The input submitted again meets an undefined variable whose question is declined.
		app.setConstructiveAdoptionPrompt((owner, impact) -> {
			lookup(owner, "g").remove();
			return true;
		});

		GeoCeDGAlgebraInputSubmission.submit(app, "X=Intersect(L,g,D)", info(),
				new ErrorAccumulator(), result -> fail("the input reports no result"));

		assertNull(app.getKernel().lookupLabel("X"));
		assertTrue(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
		await(undo::undoPossible);
		app.getKernel().undo();
		await(() -> app.getKernel().lookupLabel("g") != null);
		assertFalse(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
		assertFalse(undo.undoPossible(), "one undo point");
	}

	@Test
	void aDeclinedOfferFailsClosedWithTheTypedCause() throws Exception {
		AppGeoCeDG app = locus();
		String xml = app.getXML();
		app.setConstructiveAdoptionPrompt((owner, impact) -> false);
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND[]> result = new AtomicReference<>(new GeoElementND[0]);

		GeoCeDGAlgebraInputSubmission.submit(app, "S=Point(L,D)", info(), errors, result::set);

		assertNull(result.get(), "the command reports no result");
		assertTrue(errors.getErrors().contains(AuthorityPolicyAdoption.REQUIRED_CAUSE),
				errors.getErrors());
		assertFalse(AuthorityPolicyAdoption.isConstructive(app.getKernel().getConstruction()));
		assertNull(app.getKernel().lookupLabel("S"));
		assertEquals(xml, app.getXML(), "nothing changes");
	}

	@Test
	void aConstructiveDocumentAndOtherErrorsAreNeverOffered() throws Exception {
		AppGeoCeDG app = locus();
		app.setConstructiveAdoptionPrompt((owner, impact) -> fail("no offer"));
		ErrorAccumulator unknown = new ErrorAccumulator();

		GeoCeDGAlgebraInputSubmission.submit(app, "S=Point(L,c)", info(), unknown,
				result -> { });

		assertFalse(unknown.getErrors().isEmpty(), "an ordinary error goes through unchanged");
		assertFalse(unknown.getErrors().contains(AuthorityPolicyAdoption.REQUIRED_CAUSE));
		AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND[]> result = new AtomicReference<>();
		GeoCeDGAlgebraInputSubmission.submit(app, "S=Point(L,D)", info(), errors, result::set);
		assertEquals("", errors.getErrors());
		assertNotNull(result.get());
	}

	@Test
	void theReportOfTheOfferListsEveryChange() {
		AuthorityAdoptionImpact none = new AuthorityAdoptionImpact(List.of(), 3);
		AuthorityAdoptionImpact one = new AuthorityAdoptionImpact(List.of(
				new AuthorityAdoptionImpact.Entry("R", "admission of root 1", "ADMITTED",
						"RELAXED", "ENCLOSURE_COMPATIBLE")), 4);

		String empty = GeoCeDGConstructiveAdoptionPrompt.text(none, "es");
		String changed = GeoCeDGConstructiveAdoptionPrompt.text(one, "en");

		assertTrue(empty.contains(GeoCeDGProfile.getText(
				"ConstructiveAuthority.Adoption.NoChange", "es")), empty);
		assertTrue(empty.contains(": 3"), empty);
		assertTrue(changed.contains("R: admission of root 1 ADMITTED -> RELAXED"), changed);
		assertTrue(changed.contains(GeoCeDGProfile.getText(
				"ConstructiveAuthority.Adoption.Changes", "en") + ": 1"), changed);
	}

	@Test
	void theInspectorShowsTheIntegrityApartFromThePrecision() throws Exception {
		AppGeoCeDG app = locus();
		// A line through a conditioned point and the point of L at D: the incidence holds for
		// every value of the inherited decision, which has no rigorous enclosure, so the
		// constructive identity certificate decides nothing and the incidence is RELAXED.
		eval(app, "w=0.3");
		eval(app, "Sc=If(w>0,(1,2),(3,-1))");
		eval(app, "M=Midpoint(O,D)");
		eval(app, "h=Line(Sc,M)");
		// The same line through a rotation: the rotation is cut and the certificate proves the
		// incidence for every value of the cut.
		eval(app, "Sr=Rotate((1,2),w,O)");
		eval(app, "hr=Line(Sr,M)");
		AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
		eval(app, "X=Intersect(L,g,D)");
		eval(app, "Y=Intersect(L,h,D)");
		eval(app, "Z=Intersect(L,hr,D)");
		for (String language : new String[] {"en", "es"}) {
			app.setLocale(new Locale(language));
			String strict = GeoCeDGDefinitionInspector.semanticDetails(app, lookup(app, "X"));
			String relaxed = GeoCeDGDefinitionInspector.semanticDetails(app, lookup(app, "Y"));
			String certified = GeoCeDGDefinitionInspector.semanticDetails(app, lookup(app, "Z"));

			assertTrue(strict.contains(GeoCeDGProfile.getText("Definition.Integrity", language)
					+ ": STRICT"), strict);
			assertTrue(strict.contains(GeoCeDGProfile.getText("Definition.Membership", language)
					+ ": STRICT TRANSFERRED_BY_MARGIN"), strict);
			assertTrue(strict.contains(GeoCeDGProfile.getText("Definition.DeclaredError",
					language)), "the precision is separate: " + strict);
			assertTrue(relaxed.contains(GeoCeDGProfile.getText("Definition.Integrity",
					language) + ": RELAXED"), relaxed);
			assertTrue(relaxed.contains(GeoCeDGProfile.getText(
					"Definition.UnestablishedPremise", language)), "V-AUT-INT-2: " + relaxed);
			assertTrue(certified.contains(GeoCeDGProfile.getText("Definition.Incidence",
					language) + ": STRICT CONSTRUCTIVE_IDENTITY_CERTIFICATE"), certified);
		}
	}

	private static AppGeoCeDG locus() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : LOCUS) {
			eval(app, command);
		}
		return app;
	}

	private static EvalInfo info() {
		return new EvalInfo(true, true).withSliders(true).withSymbolic(true);
	}

	/** The current document as the undo baseline, established synchronously. */
	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var prepared = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(prepared);
		}
		return undo;
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(), "Desktop undo operation did not complete");
	}

	private static StructuralPointResolution resolution(GeoElement point) {
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}
}
