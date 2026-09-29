/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.util.CopyPaste;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-C1-U1: the ordinary host clipboard keeps a dependent geo dependent
 * when a required predecessor command has a construction-constant input such as
 * an axis. No geo here is a durable CeDG identity participant.
 */
abstract class PreG9bR3C1U1OrdinaryClosureTestBase extends BaseUnitTest {
	private static final Pattern LABEL = Pattern.compile(
			"(?:<element type=\"[^\"]*\"|<expression) label=\"([^\"]+)\"");

	/** One clipboard payload: its renamed labels and its construction fragment. */
	private static final class Clip {
		private final List<String> labels;
		private final String fragment;

		Clip(List<String> labels, String fragment) {
			this.labels = labels;
			this.fragment = fragment;
		}
	}

	@Test
	void aParentAlgorithmWithAnAxisInputIsCopiedAndThePastedGeoStaysDependent() {
		axisConstruction();
		Clip clip = copy("M");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels),
				"every renamed element must be serialized");
		assertTrue(clip.fragment.contains("a1=\"xAxis\""), clip.fragment);
		assertTrue(clip.fragment.contains("a1=\"yAxis\""), clip.fragment);
		assertEquals(clip.fragment, copy("M").fragment, "the payload is deterministic");

		paste(clip);

		Construction cons = getConstruction();
		AlgoElement line = requireLookup("f_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, line);
		assertSame(requireLookup("P_{1}"), line.getInput(0));
		assertSame(cons.getXAxis(), line.getInput(1));
		AlgoElement intersection = requireLookup("D_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoIntersectLines.class, intersection);
		assertSame(requireLookup("f_{1}"), intersection.getInput(0));
		assertSame(cons.getYAxis(), intersection.getInput(1));
		AlgoElement midpoint = requireLookup("M_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoMidpoint.class, midpoint,
				"the pasted geo must not become free geometry");
		assertSame(requireLookup("D_{1}"), midpoint.getInput(0));
		assertSame(requireLookup("P_{1}"), midpoint.getInput(1));
		assertInstanceOf(AlgoMidpoint.class, requireLookup("M").getParentAlgorithm());
	}

	@Test
	void aParentAlgorithmWithTwoOutputsAndAnAxisInputIsCompleted() {
		add("c=Circle((0,0),2)");
		add("X=Intersect(c,xAxis)");
		add("M=Midpoint(X_1,X_2)");
		Clip clip = copy("M");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels));

		paste(clip);

		AlgoElement midpoint = requireLookup("M_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoMidpoint.class, midpoint);
		AlgoElement first = midpoint.getInput(0).getParentAlgorithm();
		assertInstanceOf(AlgoIntersectLineConic.class, first);
		assertSame(first, midpoint.getInput(1).getParentAlgorithm());
		assertSame(requireLookup("c_{1}"), first.getInput(0));
		assertSame(getConstruction().getXAxis(), first.getInput(1));
		assertEquals(0.0, ((GeoPoint) requireLookup("M_{1}")).getInhomX(), 1e-12);
	}

	@Test
	void noGeoInvolvedIsAnIdentityParticipant() {
		axisConstruction();
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		assertTrue(registry.isEmpty());
		Clip clip = copy("M");
		assertFalse(clip.fragment.contains("geocedgId"), clip.fragment);
		assertFalse(clip.fragment.contains("geocedgSpatial"), clip.fragment);

		paste(clip);

		assertTrue(registry.isEmpty(), "no durable identity may be synthesized");
		assertTrue(registry.getRecords().isEmpty());
		for (String label : List.of("P", "f", "D", "M", "P_{1}", "f_{1}", "D_{1}", "M_{1}")) {
			assertFalse(registry.isParticipating(requireLookup(label)), label);
		}
		assertFalse(getApp().getXML().contains("geocedgSpatial"));
	}

	@Test
	void aControlWithoutHostConstantsCopiesTheSameConstruction() {
		add("P=(1,1)");
		add("xl:y=0");
		add("yl:x=0");
		add("f=Line(P,xl)");
		add("D=Intersect(f,yl)");
		add("M=Midpoint(D,P)");
		Clip clip = copy("M");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels));
		assertEquals(Set.of("P", "xl", "yl", "f", "D", "M"), unprefixed(clip.labels));

		paste(clip);

		AlgoElement line = requireLookup("f_{1}").getParentAlgorithm();
		assertSame(requireLookup("P_{1}"), line.getInput(0));
		assertSame(requireLookup("xl_{1}"), line.getInput(1));
		assertInstanceOf(AlgoMidpoint.class, requireLookup("M_{1}").getParentAlgorithm());
	}

	@Test
	void theClosureAddsParentsOfCopiedGeosOnlyAndNeverTheirDependents() {
		axisConstruction();

		Clip clip = copy("P");

		assertEquals(Set.of("P"), unprefixed(clip.labels),
				"copying a point must not pull in its hidden axis-based dependents");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels));
	}

	@Test
	void aPastedDependentFollowsItsPastedPredecessorAndNotTheSource() {
		axisConstruction();
		paste(copy("M"));
		GeoPoint pastedMidpoint = (GeoPoint) requireLookup("M_{1}");
		GeoPoint sourceMidpoint = (GeoPoint) requireLookup("M");
		assertEquals(0.5, pastedMidpoint.getInhomX(), 1e-12);
		assertEquals(1.0, pastedMidpoint.getInhomY(), 1e-12);

		add("SetCoords(P_{1}, 3, 4)");

		assertEquals(1.5, pastedMidpoint.getInhomX(), 1e-12);
		assertEquals(4.0, pastedMidpoint.getInhomY(), 1e-12);
		assertEquals(0.5, sourceMidpoint.getInhomX(), 1e-12);
		assertEquals(1.0, sourceMidpoint.getInhomY(), 1e-12);
	}

	@Test
	void aSuccessfulPasteIsOneUndoStepAndUndoAndRedoKeepTheDependency() {
		axisConstruction();
		activateUndo();
		getApp().storeUndoInfo();
		UndoManager undo = getConstruction().getUndoManager();
		int history = undo.getHistorySize();

		paste(copy("M"));

		assertEquals(history + 1, undo.getHistorySize());
		getKernel().undo();
		assertNull(lookup("M_{1}"));
		assertNull(lookup("f_{1}"));
		getKernel().redo();
		assertInstanceOf(AlgoMidpoint.class, requireLookup("M_{1}").getParentAlgorithm());
		assertInstanceOf(AlgoLinePointLine.class, requireLookup("f_{1}").getParentAlgorithm());
		assertSame(getConstruction().getXAxis(),
				requireLookup("f_{1}").getParentAlgorithm().getInput(1));
	}

	@Test
	void savingAndReopeningKeepsTheOrdinaryDependencyWithoutIdentity() throws Exception {
		axisConstruction();
		paste(copy("M"));
		String xml = getApp().getXML();
		assertFalse(xml.contains("geocedgSpatial"), "no identity section may appear");

		AppCommon reopened = createAppCommon();
		reopened.getXMLio().processXMLString(xml, true, false);

		Construction cons = reopened.getKernel().getConstruction();
		AlgoElement line = reopened.getKernel().lookupLabel("f_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, line);
		assertSame(cons.getXAxis(), line.getInput(1));
		assertInstanceOf(AlgoMidpoint.class,
				reopened.getKernel().lookupLabel("M_{1}").getParentAlgorithm());
		assertTrue(cons.getSpatialIdentityRegistry().isEmpty());
	}

	@Test
	void theCompletionNeverReferencesAnObjectThatIsNotCopied() {
		axisConstruction();
		GeoElement point = requireLookup("P");
		GeoElement line = requireLookup("f");
		GeoElement intersection = requireLookup("D");
		GeoElement midpoint = requireLookup("M");

		ArrayList<ConstructionElement> lonely = new ArrayList<>(List.of(midpoint));
		assertTrue(InternalClipboard.addParentAlgorithmsOfCopiedGeos(lonely, null).isEmpty());
		assertEquals(List.of(midpoint), lonely,
				"a parent whose non-constant inputs are not copied must not be added");

		ArrayList<ConstructionElement> closure = new ArrayList<>(
				List.of(point, line, intersection, midpoint));
		InternalClipboard.addParentAlgorithmsOfCopiedGeos(closure, null);
		assertTrue(closure.contains(line.getParentAlgorithm()));
		assertTrue(closure.contains(intersection.getParentAlgorithm()));
		assertTrue(closure.contains(midpoint.getParentAlgorithm()));
		int size = closure.size();
		InternalClipboard.addParentAlgorithmsOfCopiedGeos(closure, null);
		assertEquals(size, closure.size(), "completion is idempotent");
	}

	private void axisConstruction() {
		add("P=(1,1)");
		add("f=Line(P,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("M=Midpoint(D,P)");
	}

	private GeoElement requireLookup(String label) {
		GeoElement geo = lookup(label);
		assertNotNull(geo, label);
		return geo;
	}

	private Clip copy(String... labels) {
		List<GeoElement> selection = new ArrayList<>();
		for (String label : labels) {
			selection.add(requireLookup(label));
		}
		String clipboard = InternalClipboard.getTextToSave(getApp(), selection,
				text -> text);
		int separator = clipboard.indexOf('\n');
		return new Clip(new ArrayList<>(List.of(clipboard.substring(0, separator).trim()
				.split(" "))), clipboard.substring(separator + 1));
	}

	private void paste(Clip clip) {
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), clip.labels, clip.fragment);
	}

	private static Set<String> serializedLabels(String fragment) {
		Set<String> labels = new HashSet<>();
		Matcher matcher = LABEL.matcher(fragment);
		while (matcher.find()) {
			labels.add(matcher.group(1));
		}
		return labels;
	}

	private static Set<String> unprefixed(List<String> labels) {
		Set<String> result = new HashSet<>();
		for (String label : labels) {
			result.add(label.substring(CopyPaste.labelPrefix.length()));
		}
		return result;
	}
}
