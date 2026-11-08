/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Category;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/** B02 through the attached Swing protocol and its inherited cell renderers. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu5ConstructionProtocolTest {
	private static final int TOOLBAR_ICON = 2;
	private static final int CAPTION = 6;

	@Test
	void rowHeightsFollowVisibleRenderedContent() throws Exception {
		AtomicReference<ConstructionProtocolViewD> view = new AtomicReference<>();
		AtomicInteger originalIconSize = new AtomicInteger();
		AtomicInteger wrappedHeight = new AtomicInteger();
		try {
			SwingUtilities.invokeAndWait(() -> {
				AppGeoCeDG app = G9U1TestApp.create();
				view.set(protocol(app));
				JTable table = view.get().getTable();
				originalIconSize.set(app.getPresentationSize(Category.TOOLBAR_ICON));
				app.setPresentationSize(Category.TOOLBAR_ICON, 48);
				GeoElement indexed = G9U1TestApp.eval(app, "A_1=(1,2)");
				indexed.setCaption(
						"A_1 ample readable spacing for every indexed caption in this row");
				indexed.update();
				G9U1TestApp.eval(app, "B=(3,4)");
				G9U1TestApp.eval(app, "s=Segment(A_1,B)");
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				view.get().getTableColumns()[CAPTION].setWidth(60);
				assertTrue(table.getModel().getValueAt(0, 1).toString().contains("<sub>"));
			});
			SwingUtilities.invokeAndWait(() -> {
				ConstructionProtocolViewD protocol = view.get();
				JTable table = protocol.getTable();
				wrappedHeight.set(table.getRowHeight(0));
				assertTrue(wrappedHeight.get() > table.getRowHeight(1), "wrapped caption fits");
				toggleColumn(protocol, CAPTION);
				assertTrue(table.getRowHeight(0) < wrappedHeight.get());
				toggleColumn(protocol, TOOLBAR_ICON);
				Icon icon = assertInstanceOf(Icon.class,
						table.getModel().getValueAt(2, TOOLBAR_ICON));
				assertTrue(table.getRowHeight(2) >= icon.getIconHeight() + table.getRowMargin());
				toggleColumn(protocol, TOOLBAR_ICON);
				assertEquals(-1, table.convertColumnIndexToView(TOOLBAR_ICON));
			});
		} finally {
			if (view.get() != null && originalIconSize.get() > 0) {
				SwingUtilities.invokeAndWait(() -> ((AppGeoCeDG) view.get().getApp())
						.setPresentationSize(Category.TOOLBAR_ICON, originalIconSize.get()));
			}
		}
	}

	@Test
	void rowHeightsSurviveDocumentAndPresentationTransitions() throws Exception {
		onEdt(() -> {
			LookAndFeel originalLaf = UIManager.getLookAndFeel();
			install(new FlatLightLaf());
			try {
				AppGeoCeDG app = G9U1TestApp.create();
				ConstructionProtocolViewD protocol = protocol(app);
				JTable table = protocol.getTable();
				G9U1TestApp.eval(app, "A_1=(1,2)");
				G9U1TestApp.eval(app, "B=(3,4)");
				String xml = app.getXML();
				app.setXML(xml, true);
				assertEquals(2, table.getRowCount());
				int[] originalHeights = heights(table);
				assertReadable(table);

				protocol.firstStep();
				assertArrayEquals(originalHeights, heights(table));
				protocol.nextStep();
				assertArrayEquals(originalHeights, heights(table));
				protocol.lastStep();

				baseline(app);
				G9U1TestApp.eval(app, "C=(5,6)");
				store(app);
				assertEquals(3, table.getRowCount());
				app.getKernel().undo();
				assertArrayEquals(originalHeights, heights(table));
				app.getKernel().redo();
				assertEquals(3, table.getRowCount());
				int[] editedHeights = heights(table);

				SwingUtilities.updateComponentTreeUI(protocol.getCpPanel());
				assertArrayEquals(editedHeights, heights(table));
				install(new FlatDarkLaf());
				SwingUtilities.updateComponentTreeUI(protocol.getCpPanel());
				assertReadable(table);
				install(new FlatLightLaf());
				SwingUtilities.updateComponentTreeUI(protocol.getCpPanel());
				assertArrayEquals(editedHeights, heights(table));

				Locale originalLocale = app.getLocale();
				int originalFont = app.getPresentationSize(Category.CONSTRUCTION_PROTOCOL_FONT);
				try {
					int changedFont = originalFont < 24 ? 24 : 12;
					app.setPresentationSize(Category.CONSTRUCTION_PROTOCOL_FONT, changedFont);
					assertEquals(changedFont, table.getFont().getSize());
					assertReadable(table);
					app.setLanguage(Locale.ENGLISH.equals(originalLocale)
							? Locale.forLanguageTag("es") : Locale.ENGLISH);
					assertReadable(table);
				} finally {
					app.setLanguage(originalLocale);
					app.setPresentationSize(Category.CONSTRUCTION_PROTOCOL_FONT, originalFont);
				}
				assertArrayEquals(editedHeights, heights(table));
			} finally {
				install(originalLaf);
			}
			return null;
		});
	}

	private static ConstructionProtocolViewD protocol(AppGeoCeDG app) {
		ConstructionProtocolViewD protocol =
				(ConstructionProtocolViewD) app.getGuiManager().getConstructionProtocolView();
		protocol.getData().attachView();
		return protocol;
	}

	private static void toggleColumn(ConstructionProtocolViewD protocol, int modelIndex) {
		protocol.new ColumnKeeper(protocol.getTableColumns()[modelIndex],
				protocol.getData().columns[modelIndex]).actionPerformed(null);
	}

	private static void assertReadable(JTable table) {
		int minimum = 2 * table.getFontMetrics(table.getFont()).getHeight() + table.getRowMargin();
		assertTrue(IntStream.of(heights(table)).allMatch(height -> height >= minimum));
	}

	private static int[] heights(JTable table) {
		return IntStream.range(0, table.getRowCount()).map(table::getRowHeight).toArray();
	}

	private static void install(LookAndFeel lookAndFeel) {
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (UnsupportedLookAndFeelException e) {
			throw new AssertionError(e);
		}
	}
}
