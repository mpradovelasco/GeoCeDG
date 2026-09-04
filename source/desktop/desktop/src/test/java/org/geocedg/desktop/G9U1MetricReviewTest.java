/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Locale;

import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Partial metric author finding: input points are not semantic endpoint addresses. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1MetricReviewTest {

	@Test
	void originalSplineInputsRemainTruthfullyInvalidMetricEndpoints() {
		AppGeoCeDG app = G9U1TestApp.create();
		// Exact b/A/C reduction from TestBasic1.cedg, SHA-256:
		// 0791895e1133d4a44ff26c88760cfc951db787c42056a8b5758c79a9b5687be0.
		eval(app, "A=(2.36,3.04)");
		eval(app, "B=(5.62,1.08)");
		eval(app, "C=(9.44,3.24)");
		eval(app, "D=(10.86,1.86)");
		GeoLocusV2 spline = (GeoLocusV2) eval(app, "b=SplineV2({A,B,C,D},3)");
		GeoNumeric partial = (GeoNumeric) eval(app, "q=Length(b,A,C)");
		GeoLocusMetricResult rich = adapter(partial).getRichInput();
		assertTrue(spline.isDefined());
		assertNull(lookup(app, "A").getParentAlgorithm());
		assertNull(lookup(app, "C").getParentAlgorithm());
		assertTrue(rich.isDefined());
		assertFalse(rich.isScalarAdmissible());
		assertFalse(partial.isDefined());
		LocusMetricResult2D payload = rich.getMetricResult();
		assertEquals(MetricComputationStatus.INVALID_QUERY, payload.getComputationStatus());
		assertEquals(MetricValueKind.ABSENT, payload.getMetricValue().getKind());
		assertEquals(MetricCoverage.INCOMPLETE, payload.getCoverage());
		assertEquals(TraversalOutcome.TARGET_NOT_REACHABLE,
				payload.getTraversalOutcome().orElseThrow());
		assertTrue(payload.getMetricValue().getFiniteValue().isEmpty());
		assertTrue(payload.getErrorEvidence().getNumericGuarantee().isEmpty());
		assertEquals(spline.getSemanticRevision(), rich.getSourceSemanticRevision());
		assertTrue(payload.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getMessage().contains("requires exact semantic addresses")));
		assertEquals("Length(b,A,C)", definition(partial));
		assertSame(spline, rich.getParentAlgorithm().getInput(0));
		assertSame(lookup(app, "A"), rich.getParentAlgorithm().getInput(1));
		assertSame(lookup(app, "C"), rich.getParentAlgorithm().getInput(2));
	}

	@Test
	void exactSemanticEndpointsPublishAndRecomputeThroughTheRichParent() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoNumeric partial = createSemanticPartial(app);
		GeoLocusMetricResult rich = adapter(partial).getRichInput();
		assertInstanceOf(AlgoLocusBetweenMetricV2.class, rich.getParentAlgorithm());
		assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				lookup(app, "P").getParentAlgorithm());
		assertEquals(2, partial.getDouble(), 1E-8);
		assertEquals(2, rich.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 1E-8);
		assertEquals("Length(S,P,Q)", definition(partial));
		assertEquals(MetricComputationStatus.SUCCESS,
				rich.getMetricResult().getComputationStatus());
		update(app, "s", 2);
		assertEquals(4, partial.getDouble(), 1E-8);
		update(app, "v", 0.5);
		assertEquals(2, partial.getDouble(), 1E-8);
		update(app, "v", -1);
		assertFalse(partial.isDefined());
		assertFalse(rich.isScalarAdmissible());
		update(app, "v", 0.75);
		assertEquals(4, partial.getDouble(), 1E-8);
		assertSame(rich, adapter(partial).getRichInput());
	}

	@Test
	void totalAndPartialPresentationUsesLocalizedPublicProvenanceWithoutChangingXml() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoNumeric partial = createSemanticPartial(app);
		GeoNumeric total = (GeoNumeric) eval(app, "M=Length(S)");
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			String command = app.getLocalization().getCommand("Length");
			String xml = app.getXML();
			assertEquals(command + "(S,P,Q)", definition(partial));
			assertEquals(command + "(S)", definition(total));
			assertTrue(partial.getLongDescriptionHTML(true, false).contains(command));
			assertTrue(partial.getLongDescriptionHTML(true, false).contains("P"));
			assertEquals(xml, app.getXML());
			for (GeoNumeric scalar : new GeoNumeric[] {partial, total}) {
				AlgoLocusMetricScalarAdapter adapter = adapter(scalar);
				String richLabel = adapter.getRichInput().getLabelSimple();
				assertEquals(1, adapter.getInputLength());
				assertSame(adapter.getRichInput(), adapter.getInput(0));
				assertEquals("Length(" + richLabel + ")",
						normalize(adapter.getDefinition(StringTemplate.xmlTemplate)));
				assertTrue(adapter.getXML().contains("<input a0=\"" + richLabel + "\""));
			}
		}
	}

	@Test
	void partialScalarPreservesHiddenParentAndIdentityAcrossTwoNativeReopens(
			@TempDir Path directory) {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoNumeric partial = createSemanticPartial(app);
		PersistentGeoId scalarId = id(app, partial);
		PersistentGeoId richId = id(app, adapter(partial).getRichInput());
		PersistentGeoId pointId = id(app, lookup(app, "P"));
		LocusSemanticAddress2D firstAddress = ((AlgoSemanticLocusPoint2D) lookup(app, "P")
				.getParentAlgorithm()).getCurrentSemanticAddress();
		for (int iteration = 0; iteration < 2; iteration++) {
			Path archive = directory.resolve("partial-" + iteration + ".cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(archive.toFile()));
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(archive.toFile(), false));
			partial = (GeoNumeric) lookup(reopened, "MP");
			assertEquals(scalarId, id(reopened, partial));
			assertEquals(richId, id(reopened, adapter(partial).getRichInput()));
			assertEquals(pointId, id(reopened, lookup(reopened, "P")));
			assertEquals(firstAddress, ((AlgoSemanticLocusPoint2D) lookup(reopened, "P")
					.getParentAlgorithm()).getCurrentSemanticAddress());
			assertEquals("Length(S,P,Q)", definition(partial));
			assertEquals(iteration == 0 ? 2 : 4, partial.getDouble(), 1E-8);
			update(reopened, "s", 2);
			assertEquals(4, partial.getDouble(), 1E-8);
			app = reopened;
		}
	}

	@Test
	void foreignOrCoincidentPointsNeverReceiveInferredMetricPreimages() {
		AppGeoCeDG app = G9U1TestApp.create();
		createSemanticPartial(app);
		eval(app, "T=SplineV2({(-2,0),(0,0),(2,0)},3)");
		eval(app, "R=Point(T,\"spline-v2/main\",0.75)");
		eval(app, "A=(-1,0)");
		for (String command : new String[] {"Foreign=Length(S,P,R)",
				"Coincident=Length(S,A,Q)"}) {
			GeoNumeric scalar = (GeoNumeric) eval(app, command);
			assertFalse(scalar.isDefined());
			assertEquals(MetricComputationStatus.INVALID_QUERY,
					adapter(scalar).getRichInput().getMetricResult().getComputationStatus());
		}
		assertNull(((GeoPoint) lookup(app, "A")).getParentAlgorithm());
	}

	@Test
	void ordinaryLocusQuickGuideUsesTheSameSemanticMetricAuthority() {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "u=0");
		eval(app, "G=(u,0)");
		eval(app, "dom={false,{0,4,true,true}}");
		eval(app, "L=LocusV2(G,u,dom)");
		eval(app, "U=Point(L,\"generator.main\",1)");
		eval(app, "V=Point(L,\"generator.main\",3)");
		GeoNumeric total = (GeoNumeric) eval(app, "LL=Length(L)");
		GeoNumeric partial = (GeoNumeric) eval(app, "LP=Length(L,U,V)");
		assertEquals(4, total.getDouble(), 1E-8);
		assertEquals(2, partial.getDouble(), 1E-8);
		assertEquals("Length(L,U,V)", definition(partial));
		assertSame(lookup(app, "L"), adapter(partial).getRichInput()
				.getParentAlgorithm().getInput(0));
	}

	private static GeoNumeric createSemanticPartial(AppGeoCeDG app) {
		eval(app, "s=1");
		eval(app, "S=SplineV2({(-2s,0),(-2s/3,0),(2s/3,0),(2s,0)},3)");
		eval(app, "u=0.25");
		eval(app, "v=0.75");
		eval(app, "P=Point(S,\"spline-v2/main\",u)");
		eval(app, "Q=Point(S,\"spline-v2/main\",v)");
		return (GeoNumeric) eval(app, "MP=Length(S,P,Q)");
	}

	private static AlgoLocusMetricScalarAdapter adapter(GeoNumeric scalar) {
		return assertInstanceOf(AlgoLocusMetricScalarAdapter.class, scalar.getParentAlgorithm());
	}

	private static String definition(GeoNumeric scalar) {
		return normalize(scalar.getDefinitionDescription(StringTemplate.defaultTemplate));
	}

	private static String normalize(String text) {
		return text.replace(" ", "").replace('[', '(').replace(']', ')');
	}

	private static void update(AppGeoCeDG app, String label, double value) {
		GeoNumeric numeric = (GeoNumeric) lookup(app, label);
		numeric.setValue(value);
		numeric.updateCascade();
	}

	private static PersistentGeoId id(AppGeoCeDG app, GeoElement geo) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
	}
}
