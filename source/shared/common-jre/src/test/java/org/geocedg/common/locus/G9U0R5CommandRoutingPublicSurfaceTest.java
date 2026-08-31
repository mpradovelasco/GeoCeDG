/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.algos.AlgoLocusSimilarityTransform2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.Dilateable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.geos.Mirrorable;
import org.geogebra.common.kernel.geos.Rotatable;
import org.geogebra.common.kernel.geos.Translateable;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.test.TestErrorHandler;
import org.junit.jupiter.api.Test;

/** Focused R5 ordinary-command routing and public-surface compatibility tests. */
class G9U0R5CommandRoutingPublicSurfaceTest
		extends G9U0PublicSurfaceTestBase {

	private static final Pattern ELEMENT_ID = Pattern.compile(
			"<element\\s+[^>]*geocedgId=\"([^\"]+)\"");
	private static final Pattern ELEMENT_LABEL = Pattern.compile(
			"<element\\s+[^>]*label=\"([^\"]+)\"");

	@Test
	void allSevenOrdinaryFormsRouteToOneSemanticParent() {
		GeoLocusV2 source = createLine();
		add("v=Vector((0,0),(1,2))");
		add("A=(2,3)");
		add("axis:y=x+1");
		String[] definitions = {
				"Lt=Translate(L,v)",
				"Lr0=Rotate(L,pi/3)",
				"Lr=Rotate(L,pi/3,A)",
				"Lmp=Reflect(L,A)",
				"Lml=Reflect(L,axis)",
				"Ld0=Dilate(L,2)",
				"Ld=Dilate(L,-2,A)"};
		Commands[] commands = {Commands.Translate, Commands.Rotate,
				Commands.Rotate, Commands.Mirror, Commands.Mirror,
				Commands.Dilate, Commands.Dilate};

		for (int index = 0; index < definitions.length; index++) {
			GeoLocusV2 image = add(definitions[index]);
			AlgoLocusSimilarityTransform2D parent = assertInstanceOf(
					AlgoLocusSimilarityTransform2D.class,
					image.getParentAlgorithm());
			assertEquals(commands[index], parent.getClassName());
			assertSame(source, parent.getInput(0));
			assertNotEquals(source.getLocusIdentity(), image.getLocusIdentity());
		}
	}

	@Test
	void reflectAndMirrorAliasesShareOrdinaryMirrorAuthority() {
		createLine();
		add("axis:y=x+1");
		GeoLocusV2 reflected = add("Fr=Reflect(L,axis)");
		GeoLocusV2 mirrored = add("Fm=Mirror(L,axis)");
		AlgoLocusSimilarityTransform2D reflectedParent = assertInstanceOf(
				AlgoLocusSimilarityTransform2D.class,
				reflected.getParentAlgorithm());
		AlgoLocusSimilarityTransform2D mirroredParent = assertInstanceOf(
				AlgoLocusSimilarityTransform2D.class,
				mirrored.getParentAlgorithm());
		assertEquals(Commands.Mirror, reflectedParent.getClassName());
		assertEquals(Commands.Mirror, mirroredParent.getClassName());
		assertEquals(reflectedParent.getTransformSnapshot().getSemanticSignature(),
				mirroredParent.getTransformSnapshot().getSemanticSignature());
	}

	@Test
	void featureOffPreservesButCannotCreateTransformedLocus() throws Exception {
		createLine();
		GeoLocusV2 image = add("T=Rotate(L,pi/3,(1,2))");
		String imageId = image.getPersistentLocusId().toString();
		AppCommon classic = AppCommonFactory.create(new AppConfigGeoCeDG(false));

		classic.getXMLio().processXMLString(getApp().getXML(), true, false, false);
		GeoLocusV2 preserved = assertInstanceOf(GeoLocusV2.class,
				classic.getKernel().lookupLabel("T"));
		assertEquals(imageId, preserved.getPersistentLocusId().toString());
		assertEquals(Commands.Rotate, preserved.getParentAlgorithm().getClassName());
		assertFalse(((AppConfigGeoCeDG) classic.getConfig())
				.getRuntimeFeatureService().isLocusV2CreationEnabled());
		assertThrows(AssertionError.class,
				() -> process(classic, "Blocked=Translate(T,(1,0))"));
		assertNull(classic.getKernel().lookupLabel("Blocked"));
	}

	@Test
	void ordinaryTextAndVectorOverloadsRemainUpstreamOwned() {
		GeoText rotatedText = add("Rt=Rotate(\"GeoCeDG\",pi/2)");
		add("v=Vector((0,0),(1,2))");
		add("A=(3,4)");
		GeoVector translatedVector = add("w=Translate(v,A)");

		assertNotNull(rotatedText);
		assertFalse(GeoLocusV2.class.isInstance(rotatedText));
		assertFalse(GeoLocusV2.class.isInstance(translatedVector));
		assertEquals("AlgoRotateText",
				rotatedText.getParentAlgorithm().getClass().getSimpleName());
		assertEquals("AlgoTranslateVector",
				translatedVector.getParentAlgorithm().getClass().getSimpleName());
	}

	@Test
	void circleInversionAndThreeDimensionalFormsFailClosed()
			throws Exception {
		createLine();
		add("c=Circle((0,0),1)");
		assertThrows(AssertionError.class, () -> add("Bad=Reflect(L,c)"));
		assertNull(lookup("Bad"));

		AppCommon app3d = AppCommonFactory.create3D(new AppConfigGeoCeDG(true));
		createLine(app3d);
		GeoElement center3d = process(app3d, "C3=(1,2,3)");
		GeoElement axis3d = process(app3d,
				"axis3=Line((0,0,0),(0,0,1))");
		GeoElement plane3d = process(app3d,
				"plane3=Plane((0,0,0),(1,0,0),(0,1,0))");
		assertTrue(center3d.isGeoElement3D());
		assertTrue(axis3d.isGeoElement3D());
		assertTrue(plane3d.isGeoElement3D());
		final String beforeXml = app3d.getXML();
		final int beforeGeoCount = app3d.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final int beforeIdentityCount = app3d.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();
		assertThrows(AssertionError.class,
				() -> process(app3d, "BadR=Rotate(L,pi/2,C3)"));
		assertThrows(AssertionError.class,
				() -> process(app3d, "BadAxis=Rotate(L,pi/2,axis3)"));
		assertThrows(AssertionError.class,
				() -> process(app3d, "BadPlane=Reflect(L,plane3)"));
		assertThrows(AssertionError.class,
				() -> process(app3d, "BadD=Dilate(L,2,C3)"));
		assertNull(app3d.getKernel().lookupLabel("BadR"));
		assertNull(app3d.getKernel().lookupLabel("BadAxis"));
		assertNull(app3d.getKernel().lookupLabel("BadPlane"));
		assertNull(app3d.getKernel().lookupLabel("BadD"));
		assertEquals(beforeXml, app3d.getXML());
		assertEquals(beforeGeoCount, app3d.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(beforeIdentityCount, app3d.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
	}

	@Test
	void locusDoesNotAcquireLegacyMutableTransformContractsOrPath() {
		GeoLocusV2 source = createLine();
		assertFalse(Path.class.isAssignableFrom(source.getClass()));
		assertFalse(Translateable.class.isAssignableFrom(source.getClass()));
		assertFalse(Rotatable.class.isAssignableFrom(source.getClass()));
		assertFalse(Mirrorable.class.isAssignableFrom(source.getClass()));
		assertFalse(Dilateable.class.isAssignableFrom(source.getClass()));
	}

	@Test
	void nestedCommandsHaveOneSerializableAttachmentPerGeo() {
		createLine();
		GeoLocusV2 image = add(
				"T=Translate(Rotate(Reflect(Dilate(L,2,(1,0)),y=x),pi/2),"
						+ "(3,4))");
		assertNotNull(image);
		String xml = getApp().getXML();
		assertEquals(1, count(xml, "<command name=\"Dilate\">"));
		assertEquals(1, count(xml, "<command name=\"Mirror\">"));
		assertEquals(1, count(xml, "<command name=\"Rotate\">"));
		assertEquals(1, count(xml, "<command name=\"Translate\">"));
		assertUniqueMatches(xml, ELEMENT_ID);
		assertUniqueMatches(xml, ELEMENT_LABEL);
	}

	@Test
	void rejectedCircularRedefineRollsBackSimilarityParticipation() {
		GeoLocusV2 source = createLine();
		final String beforeXml = getApp().getXML();
		final int beforeGeoCount = getConstruction().getGeoSetConstructionOrder()
				.size();
		final int beforeIdentityCount = getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();

		assertThrows(AssertionError.class,
				() -> add("L=Translate(L,(1,2))"));
		assertSame(source, lookup("L"));
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(beforeGeoCount,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(beforeIdentityCount, getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
	}

	private static void createLine(AppCommon app) {
		process(app, "s=0");
		process(app, "Q=(s,0)");
		process(app, "D={false,{-2,2,true,true}}");
		assertInstanceOf(GeoLocusV2.class, process(app, "L=LocusV2(Q,s,D)"));
	}

	private static GeoElement process(AppCommon app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(command, false,
						TestErrorHandler.INSTANCE, false, null);
		assertNotNull(result);
		assertTrue(result.length > 0);
		return result[0].toGeoElement();
	}

	private static int count(String text, String fragment) {
		int count = 0;
		int from = 0;
		while ((from = text.indexOf(fragment, from)) >= 0) {
			count++;
			from += fragment.length();
		}
		return count;
	}

	private static void assertUniqueMatches(String xml, Pattern pattern) {
		Matcher matcher = pattern.matcher(xml);
		Set<String> values = new HashSet<>();
		int matches = 0;
		while (matcher.find()) {
			matches++;
			assertTrue(values.add(matcher.group(1)), matcher.group(1));
		}
		assertTrue(matches > 0);
	}
}
