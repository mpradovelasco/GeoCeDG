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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-C1: the identity-bearing clipboard copies the complete predecessor
 * closure, including commands with a construction-constant input, and a rejected
 * or failed paste restores the exact pre-paste host state.
 */
class PreG9bR3C1ClipboardCorrectnessTest extends PreG9bR3RedefineTestBase {
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
	void anAxisInputPredecessorIsCopiedAndTheLocusPastesDependent() throws Exception {
		axisWitness();
		final Map<String, PersistentGeoId> sourceIds = participantIds();
		Clip clip = copy("a");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels),
				"every renamed element must be serialized");
		assertTrue(clip.fragment.contains("a1=\"xAxis\""), clip.fragment);
		assertTrue(clip.fragment.contains("a1=\"yAxis\""), clip.fragment);
		assertEquals(clip.fragment, copy("a").fragment, "the payload is deterministic");

		paste(clip);

		Construction cons = getConstruction();
		AlgoElement line = requireLookup("f_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, line);
		assertSame(requireLookup("C_{1}"), line.getInput(0));
		assertSame(cons.getXAxis(), line.getInput(1));
		AlgoElement intersection = requireLookup("D_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoIntersectLines.class, intersection);
		assertSame(requireLookup("f_{1}"), intersection.getInput(0));
		assertSame(cons.getYAxis(), intersection.getInput(1));
		AlgoElement locus = requireLookup("a_{1}").getParentAlgorithm();
		assertSame(requireLookup("D_{1}"), locus.getInput(0));
		assertSame(requireLookup("C_{1}"), locus.getInput(1));
		assertNull(registry().getPersistentGeoId(cons.getXAxis()));
		assertNull(registry().getPersistentGeoId(cons.getYAxis()));
		assertFreshRemappedCopy(sourceIds);

		AppCommon reopened = openInFreshApp(getApp().getXML());
		Construction reopenedCons = reopened.getKernel().getConstruction();
		AlgoElement reopenedLine = reopened.getKernel().lookupLabel("f_{1}")
				.getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, reopenedLine);
		assertSame(reopenedCons.getXAxis(), reopenedLine.getInput(1));
		assertEquals(registry().writeSpatialSection(),
				reopenedCons.getSpatialIdentityRegistry().writeSpatialSection());
		for (String label : List.of("c_{1}", "C_{1}", "D_{1}", "a_{1}")) {
			assertEquals(id(label), reopenedCons.getSpatialIdentityRegistry()
					.getPersistentGeoId(reopened.getKernel().lookupLabel(label)), label);
		}
	}

	@Test
	void aFreeLineControlStillCopiesTheSameConstruction() {
		getKernel().setContinuous(false);
		add("A=(0,0)");
		add("c=Circle(A,1)");
		add("C=Point(c)");
		add("xl:y=0");
		add("yl:x=0");
		add("f=Line(C,xl)");
		add("D=Intersect(f,yl)");
		add("a=LocusV2(D,C)");
		final Map<String, PersistentGeoId> sourceIds = participantIds();
		Clip clip = copy("a");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels));
		assertEquals(Set.of("A", "c", "C", "xl", "yl", "f", "D", "a"),
				unprefixed(clip.labels));

		paste(clip);

		AlgoElement line = requireLookup("f_{1}").getParentAlgorithm();
		assertSame(requireLookup("C_{1}"), line.getInput(0));
		assertSame(requireLookup("xl_{1}"), line.getInput(1));
		AlgoElement intersection = requireLookup("D_{1}").getParentAlgorithm();
		assertSame(requireLookup("f_{1}"), intersection.getInput(0));
		assertSame(requireLookup("yl_{1}"), intersection.getInput(1));
		assertFreshRemappedCopy(sourceIds);
	}

	@Test
	void anAxisDependentHelperOfTheClosureStaysDependent() {
		helperWitness();
		Clip clip = copy("a");
		assertEquals(serializedLabels(clip.fragment), new HashSet<>(clip.labels));

		paste(clip);

		GeoElement helper = requireLookup("M_{1}");
		AlgoElement midpoint = helper.getParentAlgorithm();
		assertInstanceOf(AlgoMidpoint.class, midpoint,
				"a dependent copied helper must not become free");
		assertSame(requireLookup("D_{1}"), midpoint.getInput(0));
		assertSame(requireLookup("P_{1}"), midpoint.getInput(1));
		AlgoElement intersection = requireLookup("D_{1}").getParentAlgorithm();
		assertSame(getConstruction().getYAxis(), intersection.getInput(1));
		assertSame(helper, requireLookup("c_{1}").getParentAlgorithm().getInput(0));
	}

	@Test
	void anUnrebuildableIdentityPayloadIsRejectedAndRestoresEverything() throws Exception {
		axisWitness();
		prepareUndo();
		Clip legacy = withoutDefinition(copy("a"), "f");
		HostState before = new HostState();

		paste(legacy);

		before.assertRestored(new HostState());
		assertNull(lookup("a_{1}"));
		AppCommon reopened = openInFreshApp(getApp().getXML());
		assertFalse(reopened.isBlockUpdateScripts());
		assertTrue(reopened.getXML().contains("<scripting blocked=\"false\""));
		assertEquals(registry().writeSpatialSection(), reopened.getKernel()
				.getConstruction().getSpatialIdentityRegistry().writeSpatialSection());
	}

	@Test
	void aPayloadThatWouldDegradeAHelperIsRejectedInsteadOfPastedFree() {
		helperWitness();
		prepareUndo();
		Clip legacy = withoutDefinition(copy("a"), "f");
		HostState before = new HostState();

		paste(legacy);

		before.assertRestored(new HostState());
		assertNull(lookup("M_{1}"), "no helper may be pasted as free geometry");
	}

	@Test
	void scriptsAlreadyBlockedStayBlockedAfterARejectedPaste() {
		axisWitness();
		prepareUndo();
		Clip legacy = withoutDefinition(copy("a"), "f");
		getApp().setBlockUpdateScripts(true);
		HostState before = new HostState();

		paste(legacy);

		before.assertRestored(new HostState());
		assertTrue(getApp().isBlockUpdateScripts());
	}

	@Test
	void aHostMutationBeforeAFailedImportIsRolledBack() {
		axisWitness();
		prepareUndo();
		Clip clip = copy("a");
		HostState before = new HostState();

		boolean imported = InternalClipboard.evalClipboardXMLAtomically(getApp(),
				clip.fragment, null, () -> {
					requireLookup("A").remove();
					throw new IllegalStateException("injected after the mutation");
				});

		assertFalse(imported);
		before.assertRestored(new HostState());
		assertNotNull(lookup("a"));
	}

	@Test
	void aRollbackFailureKeepsTheImportFailureAsSuppressedCause() {
		helperWitness();
		Clip legacy = withoutDefinition(copy("a"), "f");

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> InternalClipboard.evalClipboardXMLAtomically(getApp(),
						legacy.fragment, "not a construction", null));

		assertNotNull(failure.getCause());
		assertEquals(1, failure.getSuppressed().length);
		SpatialIdentityException importFailure = assertInstanceOf(
				SpatialIdentityException.class, failure.getSuppressed()[0]);
		assertEquals(SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
				importFailure.getDiagnostic().getCode());
	}

	@Test
	void aSuccessfulPasteIsOneUndoStepThatUndoAndRedoRestoreExactly() {
		axisWitness();
		prepareUndo();
		UndoManager undo = getConstruction().getUndoManager();
		int history = undo.getHistorySize();
		String sectionBefore = registry().writeSpatialSection();
		Map<String, PersistentGeoId> idsBefore = labelledIds();

		paste(copy("a"));

		assertEquals(history + 1, undo.getHistorySize());
		String sectionAfter = registry().writeSpatialSection();
		final Map<String, PersistentGeoId> idsAfter = labelledIds();
		assertNotEquals(sectionBefore, sectionAfter);

		getKernel().undo();
		assertEquals(sectionBefore, registry().writeSpatialSection());
		assertEquals(idsBefore, labelledIds());
		assertNull(lookup("a_{1}"));

		getKernel().redo();
		assertEquals(sectionAfter, registry().writeSpatialSection());
		assertEquals(idsAfter, labelledIds());
		assertInstanceOf(AlgoLinePointLine.class,
				requireLookup("f_{1}").getParentAlgorithm());
	}

	private void axisWitness() {
		getKernel().setContinuous(false);
		add("A=(0,0)");
		add("c=Circle(A,1)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		assertNotNull(add("a=LocusV2(D,C)"));
	}

	/** The participants depend on the axis-derived helper M, which has no identity. */
	private void helperWitness() {
		getKernel().setContinuous(false);
		add("P=(1,1)");
		add("f=Line(P,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("M=Midpoint(D,P)");
		add("c=Circle(M,1)");
		add("C=Point(c)");
		add("E=Midpoint(C,P)");
		assertNotNull(add("a=LocusV2(E,C)"));
		assertNull(registry().getPersistentGeoId(requireLookup("M")));
	}

	private void prepareUndo() {
		activateUndo();
		getApp().storeUndoInfo();
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

	/** @return the payload as serialized before C1: the helper is renamed only */
	private static Clip withoutDefinition(Clip clip, String label) {
		String prefixed = "CLIPBOARDmagicSTRING" + label;
		Pattern command = Pattern.compile("<command name=\"[^\"]*\">\\s*<input[^>]*/>"
				+ "\\s*<output a0=\"" + Pattern.quote(prefixed) + "\"/>\\s*</command>\\s*");
		Pattern element = Pattern.compile("<element type=\"[^\"]*\" label=\""
				+ Pattern.quote(prefixed) + "\">.*?</element>\\s*", Pattern.DOTALL);
		String fragment = removeOnce(clip.fragment, command);
		fragment = removeOnce(fragment, element);
		assertTrue(clip.labels.contains(prefixed));
		return new Clip(clip.labels, fragment);
	}

	private static String removeOnce(String text, Pattern pattern) {
		Matcher matcher = pattern.matcher(text);
		assertTrue(matcher.find(), pattern.pattern());
		String result = text.substring(0, matcher.start()) + text.substring(matcher.end());
		assertFalse(pattern.matcher(result).find(), pattern.pattern());
		return result;
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
			result.add(label.substring("CLIPBOARDmagicSTRING".length()));
		}
		return result;
	}

	/** @return the durable ids of the construction participants, by label */
	private Map<String, PersistentGeoId> participantIds() {
		Map<String, PersistentGeoId> ids = labelledIds();
		assertFalse(ids.isEmpty());
		return ids;
	}

	private Map<String, PersistentGeoId> labelledIds() {
		TreeMap<String, PersistentGeoId> ids = new TreeMap<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			PersistentGeoId id = registry().getPersistentGeoId(geo);
			if (id != null) {
				ids.put(geo.getLabelSimple(), id);
			}
		}
		return ids;
	}

	/**
	 * Asserts that every source participant has one pasted copy with a fresh id,
	 * immediate copy lineage and dependencies remapped onto pasted ids only.
	 */
	private void assertFreshRemappedCopy(Map<String, PersistentGeoId> sourceIds) {
		Set<PersistentGeoId> pastedIds = new HashSet<>();
		for (String label : sourceIds.keySet()) {
			pastedIds.add(id(label + "_{1}"));
		}
		for (Map.Entry<String, PersistentGeoId> source : sourceIds.entrySet()) {
			PersistentGeoId pastedId = id(source.getKey() + "_{1}");
			assertFalse(sourceIds.containsValue(pastedId), source.getKey());
			GeoIdentityRecord pasted = record(pastedId);
			assertEquals(source.getValue(), pasted.getCopySourceId(), source.getKey());
			assertTrue(pastedIds.containsAll(pasted.getDependencies()),
					source.getKey() + " refers back to the source: "
							+ pasted.getDependencies());
			GeoIdentityRecord original = record(source.getValue());
			assertEquals(original.getDependencies().size(),
					pasted.getDependencies().size(), source.getKey());
		}
		assertEquals(2 * sourceIds.size(), labelledIds().size());
	}

	/** Every authoritative host fact that a rejected paste must leave unchanged. */
	private final class HostState {
		private final String xml;
		private final String section;
		private final Map<String, PersistentGeoId> ids;
		private final int undoHistory;
		private final boolean undoPossible;
		private final boolean redoPossible;
		private final boolean scriptsBlocked;
		private final boolean loadingMode;
		private final boolean notifyViews;
		private final boolean fileLoading;
		private final Object lookupStrategy;

		HostState() {
			final Kernel kernel = getKernel();
			Construction cons = getConstruction();
			UndoManager undo = cons.getUndoManager();
			xml = getApp().getXML();
			section = registry().writeSpatialSection();
			ids = labelledIds();
			undoHistory = undo.getHistorySize();
			undoPossible = undo.undoPossible();
			redoPossible = undo.redoPossible();
			scriptsBlocked = getApp().isBlockUpdateScripts();
			loadingMode = kernel.getLoadingMode();
			notifyViews = kernel.isNotifyViewsActive();
			fileLoading = cons.isFileLoading();
			lookupStrategy = kernel.getCommandLookupStrategy();
		}

		void assertRestored(HostState after) {
			assertEquals(xml, after.xml);
			assertEquals(section, after.section);
			assertEquals(ids, after.ids);
			assertEquals(undoHistory, after.undoHistory);
			assertEquals(undoPossible, after.undoPossible);
			assertEquals(redoPossible, after.redoPossible);
			assertEquals(scriptsBlocked, after.scriptsBlocked);
			assertEquals(loadingMode, after.loadingMode);
			assertEquals(notifyViews, after.notifyViews);
			assertEquals(fileLoading, after.fileLoading);
			assertEquals(lookupStrategy, after.lookupStrategy);
		}
	}
}
