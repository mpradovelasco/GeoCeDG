/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.MM;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.desktop.util.CopyPasteD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Native insertion buffers retain an explicit metric layer within their own XML scope. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120MetricInsertionTest {

	@Test
	void ordinaryBufferKeepsTheInheritedNumericLayerFallback() throws Exception {
		assertCopiedLayers(new CopyPasteD(), 4);
	}

	@Test
	void insertionBufferRetainsTheMetricLayerAndResetsTheScopeForTheNextNumber()
			throws Exception {
		assertCopiedLayers(new GeoCeDGInsertionCopyPaste(Set.of()), 0);
	}

	private static void assertCopiedLayers(CopyPasteD buffer, int metricLayer) throws Exception {
		AppGeoCeDG target = onEdt(() -> {
			AppGeoCeDG source = app(MM);
			// Each disposable host starts with an explicit counter, independent of user macros.
			source.resetMaxLayerUsed();
			must(source, "seg=Segment((0,0),(4,0))");
			must(source, "E=Point(seg)");
			must(source, "shift=Vector((0,0),(0,1))");
			must(source, "Q=Translate(E,shift)");
			must(source, "L=LocusV2(Q,E)");
			List<String> commands = List.of("LocusLength", "Length");
			List<String> labels = List.of("len", "standardLen");
			for (int index = 0; index < commands.size(); index++) {
				String label = labels.get(index);
				GeoNumeric length = assertInstanceOf(GeoNumeric.class,
						must(source, label + "=" + commands.get(index) + "(L)"));
				GeoNumeric tail = assertInstanceOf(GeoNumeric.class,
						must(source, label + "Tail=" + label + "+1"));
				assertInstanceOf(AlgoLocusMetricScalarAdapter.class, length.getParentAlgorithm());
				assertEquals(0, length.getLayer());
				assertEquals(0, tail.getLayer());
				assertFalse(length.isDrawable());
				assertFalse(tail.isDrawable());
				assertEquals(length.getConstructionIndex() + 1, tail.getConstructionIndex(),
						"The ordinary numeric must be the next serialized construction element");
			}
			baseline(source);

			AppGeoCeDG receiver = app(MM);
			receiver.resetMaxLayerUsed();
			must(receiver, "anchor=(10,10)").setLayer(4);
			assertEquals(4, receiver.getMaxLayerUsed());
			baseline(receiver);
			buffer.insertFrom(source, receiver, Set.of(), false);

			for (String label : labels) {
				GeoNumeric copiedLength = assertInstanceOf(GeoNumeric.class,
						receiver.getKernel().lookupLabel(label));
				GeoNumeric copiedTail = assertInstanceOf(GeoNumeric.class,
						receiver.getKernel().lookupLabel(label + "Tail"));
				assertInstanceOf(AlgoLocusMetricScalarAdapter.class, copiedLength.getParentAlgorithm());
				assertEquals(metricLayer, copiedLength.getLayer());
				assertEquals(4, copiedTail.getLayer(),
						"The metric XML scope must end before the next ordinary numeric");
				assertSame(copiedLength, copiedTail.getParentAlgorithm().getInput(0));
				assertEquals(4, copiedLength.getDouble(), 1e-12);
				assertEquals(5, copiedTail.getDouble(), 1e-12);
				assertEquals(0, source.getKernel().lookupLabel(label).getLayer());
				assertEquals(0, source.getKernel().lookupLabel(label + "Tail").getLayer());
			}
			return receiver;
		});
		awaitQuiescent(target);
	}
}
