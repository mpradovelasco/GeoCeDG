/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.Commands;
import org.junit.jupiter.api.Test;

/** Executable public command coverage included by the inherited command SelfTest. */
public class GeoCeDGCommandsTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void cmdLocusV2() {
		GeoLocusV2 locus = createLine();
		assertNotNull(locus);
		assertTrue(locus.isDefined());
		assertFalse(locus.isPath());
		assertEquals(Commands.LocusV2, locus.getParentAlgorithm().getClassName());
		assertEquals(-2, locus.getSemanticDefinition().getProvider()
				.getDeclaredDomain().getLower(), 0);
		assertEquals(2, locus.getSemanticDefinition().getProvider()
				.getDeclaredDomain().getUpper(), 0);
	}

	@Test
	void cmdLocusLength() {
		GeoLocusV2 locus = createLine();
		GeoLocusMetricResult metric = add("M=LocusLength(L)");
		assertNotNull(metric);
		assertTrue(metric.isDefined());
		assertEquals(Commands.LocusLength, metric.getParentAlgorithm().getClassName());
		assertEquals(locus.getLocusIdentity(), metric.getSourceLocusIdentity());
		assertEquals(4, metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow(), 1E-10);
	}

	@Test
	void cmdSplineV2() {
		add("A=(-2,0)");
		add("B=(-2/3,0)");
		add("C=(2/3,0)");
		add("D=(2,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D},3)");
		assertNotNull(spline);
		assertTrue(spline.isDefined());
		assertFalse(spline.isPath());
		assertEquals(Commands.SplineV2, spline.getParentAlgorithm().getClassName());
		GeoLocusMetricResult metric = add("M=LocusLength(S)");
		assertEquals(4, metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow(), 1E-10);
	}

	private GeoLocusV2 createLine() {
		add("s=0");
		add("Q=(s,0)");
		add("D={false,{-2,2,true,true}}");
		return add("L=LocusV2(Q,s,D)");
	}
}
