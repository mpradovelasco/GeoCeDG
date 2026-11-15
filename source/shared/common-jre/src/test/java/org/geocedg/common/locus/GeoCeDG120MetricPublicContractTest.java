/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class GeoCeDG120MetricPublicContractTest extends G9U0PublicSurfaceTestBase {

	@Test
	void numericPublicQueriesHaveTheirOwnIdentityAndSurviveReopening() {
		var locus = createPeriodicCircle();
		GeoNumeric first = add("m=LocusLength(L)");
		GeoNumeric second = add("n=LocusLength(L)");
		assertEquals("c", add("c=Circle((0,0),2)").getLabelSimple());
		GeoNumeric formula = add("value=m+n");
		assertEquals(2 * Math.PI, first.getDouble(), 1e-9);
		assertEquals(first.getDouble(), second.getDouble());
		assertEquals(4 * Math.PI, formula.getDouble(), 1e-9);
		var firstRich = AlgoLocusMetricScalarAdapter.richResultOf(first);
		assertTrue(firstRich.isAuxiliaryObject());
		assertFalse(first.isAuxiliaryObject());
		var registry = locus.getConstruction().getSpatialIdentityRegistry();
		var firstId = registry.getPersistentGeoId(first);
		var richId = registry.getPersistentGeoId(firstRich);
		assertNotEquals(firstId, registry.getPersistentGeoId(second));
		assertNotEquals(richId,
				registry.getPersistentGeoId(AlgoLocusMetricScalarAdapter.richResultOf(second)));
		getApp().setXML(getApp().getXML(), true);
		var reloaded = assertInstanceOf(GeoNumeric.class, lookup("m"));
		var reloadedRich = AlgoLocusMetricScalarAdapter.richResultOf(reloaded);
		var reloadedRegistry = reloaded.getConstruction().getSpatialIdentityRegistry();
		assertEquals(firstId, reloadedRegistry.getPersistentGeoId(reloaded));
		assertEquals(richId, reloadedRegistry.getPersistentGeoId(reloadedRich));
		assertTrue(reloadedRich.isAuxiliaryObject());
		assertFalse(reloaded.isAuxiliaryObject());
		assertEquals(4 * Math.PI, ((GeoNumeric) lookup("value")).getDouble(), 1e-9);
	}

	@Test
	void historicalRichQueryAndItsLengthChildKeepTheirTypesAndStyle() {
		var locus = createPeriodicCircle();
		var legacyChild = LocusV2PublicOperations.scalarLength(locus.getConstruction(), "oldChild", locus);
		var legacyRich = AlgoLocusMetricScalarAdapter.richResultOf(legacyChild);
		legacyRich.setAuxiliaryObject(false);
		GeoLocusMetricResult legacyAuxiliary = LocusV2PublicOperations.totalMetric(
				locus.getConstruction(), "oldAux", locus);
		legacyAuxiliary.setAuxiliaryObject(true);
		String richLabel = legacyRich.getLabelSimple();
		var registry = locus.getConstruction().getSpatialIdentityRegistry();
		var richId = registry.getPersistentGeoId(legacyRich);
		getApp().setXML(getApp().getXML(), true);
		var reloadedRich = assertInstanceOf(GeoLocusMetricResult.class, lookup(richLabel));
		var reloadedChild = assertInstanceOf(GeoNumeric.class, lookup("oldChild"));
		assertEquals(richId, reloadedChild.getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(reloadedRich));
		assertEquals(reloadedRich, AlgoLocusMetricScalarAdapter.richResultOf(reloadedChild));
		assertFalse(reloadedRich.isAuxiliaryObject());
		assertTrue(assertInstanceOf(GeoLocusMetricResult.class, lookup("oldAux")).isAuxiliaryObject());
		assertEquals(2 * Math.PI, reloadedChild.getDouble(), 1e-9);
	}

	@Test
	void inadmissibleEndpointLeavesAnUndefinedScalarWithDiagnostics() {
		createPeriodicCircle();
		add("A=(1,0)");
		add("B=Point(L,1)");
		GeoNumeric result = add("m=LocusLength(L,A,B,\"forward\",\"strict\",\"zero-length\")");
		assertFalse(result.isDefined());
		assertFalse(result.isAuxiliaryObject());
		var authority = AlgoLocusMetricScalarAdapter.richResultOf(result);
		assertTrue(authority.isDefined());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				authority.getMetricResult().getComputationStatus());
	}

	@Test
	void explicitTraversalKeepsItsNumericValueAndUserAuxiliarySetting() {
		createPeriodicCircle();
		add("t=0.5");
		add("A=Point(L,0)");
		add("B=Point(L,t)");
		GeoNumeric result = add("a1=LocusLength(L,A,B,\"forward\",\"strict\",\"zero-length\")");
		assertEquals("b", add("b=Circle(B,1)").getLabelSimple());
		assertFalse(result.isAuxiliaryObject());
		assertEquals(0.5, result.getDouble(), 1e-9);
		GeoNumeric parameter = (GeoNumeric) lookup("t");
		parameter.setValue(0.75);
		parameter.updateCascade();
		assertEquals(0.75, result.getDouble(), 1e-9);
		result.setAuxiliaryObject(true);
		getApp().setXML(getApp().getXML(), true);
		GeoNumeric reloaded = assertInstanceOf(GeoNumeric.class, lookup("a1"));
		assertTrue(reloaded.isAuxiliaryObject());
		assertEquals(0.75, reloaded.getDouble(), 1e-9);
	}
}
