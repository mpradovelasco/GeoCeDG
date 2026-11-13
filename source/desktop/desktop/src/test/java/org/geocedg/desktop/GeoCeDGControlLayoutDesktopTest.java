/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.euclidian.draw.DrawInputBox;
import org.geogebra.common.euclidian.draw.DrawSlider;
import org.geogebra.common.gui.dialog.options.model.ViewLocationModel;
import org.geogebra.common.gui.dialog.options.model.ViewLocationModel.IGraphicsViewLocationListener;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoButton;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.color.GeoGebraColorChooser;
import org.geogebra.desktop.gui.dialog.PropertiesPanelD;
import org.geogebra.desktop.gui.properties.SliderPropertiesPanelD;
import org.geogebra.desktop.gui.view.properties.PropertiesViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Checks live Graphics 1 presentation against the native drawable and hit paths. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class GeoCeDGControlLayoutDesktopTest {
	@TempDir
	Path temporary;

	private void assertRejectingFixedAngleKeepsItsExplicit3dExclusion() throws Exception {
		Path original = PreG9BR6PlusD1LegacyFingerprints.findRepositoryRoot()
				.resolve("models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg");
		Path copy = Files.copy(original, temporary.resolve("fixed-angle.cedg"));
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			assertTrue(app.loadFile(copy.toFile(), false));
			GeoCeDGEuclidianView view = (GeoCeDGEuclidianView) app.getEuclidianView1();
			var workspace = view.controlLayoutWorkspace();
			var angle = (GeoNumeric) app.getKernel().lookupLabel("α_{cone}");
			var otherAngle = app.getKernel().lookupLabel("α_{cyl}");
			assertFalse(app.isEuclidianView3Dinited());
			assertFalse(angle.isAbsoluteScreenLocActive());
			assertTrue(angle.isLockedPosition());
			var properties = new PropertiesPanelD(app, new GeoGebraColorChooser(app), false);
			properties.setLabels();
			properties.updateSelection(new Object[] {angle});
			var graphics3d = findComponent(properties, JCheckBox.class, box ->
					app.getLocalization().getMenu("GraphicsView3D").equals(box.getText()));
			assertNotNull(graphics3d);
			assertTrue(graphics3d.isSelected());

			var existingWindows = List.of(Window.getWindows());
			try {
				app.openControlLayout();
				var manager = List.of(Window.getWindows()).stream()
						.filter(window -> !existingWindows.contains(window))
						.filter(JDialog.class::isInstance).map(JDialog.class::cast)
						.filter(dialog -> app.layerText("ControlLayout.Title").equals(dialog.getTitle()))
						.findFirst().orElseThrow();
				var content = manager.getContentPane();
				var create = findComponent(content, JButton.class,
						button -> app.layerText("ControlLayout.New").equals(button.getText()));
				var attach = findComponent(content, JButton.class,
						button -> app.layerText("ControlLayout.Attach").equals(button.getText()));
				var instructions = findComponent(content, JLabel.class,
						label -> app.layerText("ControlLayout.Pick").equals(label.getText()));
				assertNotNull(create);
				assertNotNull(attach);
				assertNotNull(instructions);
				create.doClick(0);
				app.getSelectionManager().setSelectedGeos(List.of(angle), true);
				attach.doClick(0);
				assertEquals(app.layerText("ControlLayout.RemoveFrom3D"), instructions.getText());
				assertTrue(angle.isVisibleInView3D());
				assertNull(layout(app).slot(angle));
				assertCapturedRejection(app, workspace, view, angle, attach, instructions,
						"ControlLayout.RemoveFrom3D");
				assertTrue(angle.isVisibleInView3D());
				graphics3d.doClick(0);
				assertFalse(angle.isVisibleInView3D());
				app.getSelectionManager().setSelectedGeos(List.of(angle), true);
				attach.doClick(0);
				assertNull(layout(app).slot(angle));
				assertTrue(angle.isLockedPosition());
				properties.updateSelection(new Object[] {otherAngle});
				assertTrue(graphics3d.isSelected());
				properties.updateSelection(new Object[] {angle});
				assertFalse(graphics3d.isSelected());
				assertFalse(angle.isVisibleInView3D());
				assertEquals(app.layerText("ControlLayout.UnlockSlider"), instructions.getText());
				assertCapturedRejection(app, workspace, view, angle, attach, instructions,
						"ControlLayout.UnlockSlider");
				assertNull(layout(app).slot(angle));
				assertFalse(angle.isVisibleInView3D());
				var fixed = findComponent(properties, JCheckBox.class,
						box -> app.getLocalization().getMenu("fixed").equals(box.getText()));
				assertNotNull(fixed);
				assertTrue(fixed.isSelected());
				fixed.doClick(0);
				assertFalse(angle.isLockedPosition());
				app.getSelectionManager().setSelectedGeos(List.of(angle), true);
				attach.doClick(0);
				assertEquals(app.layerText("ControlLayout.ConfirmPlacement"), attach.getText());
				attach.doClick(0);
				assertSame(angle, layout(app).slot(angle).control());
				assertFalse(angle.isAbsoluteScreenLocActive());
				assertFalse(angle.isVisibleInView3D());
			} finally {
				workspace.close();
				app.getFrame().dispose();
			}
			return null;
		});
	}

	private static void assertCapturedRejection(AppGeoCeDG app,
			GeoCeDGControlLayoutWorkspace workspace, GeoCeDGEuclidianView view, GeoNumeric angle,
			JButton attach, JLabel instructions, String messageKey) {
		app.getSelectionManager().clearSelectedGeos();
		attach.doClick(0);
		assertEquals(app.layerText("ControlLayout.Capture"), instructions.getText());
		var press = MouseEventD.wrapEvent(new MouseEvent(view.getJPanel(),
				MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1_DOWN_MASK,
				50, 50, 1, false, MouseEvent.BUTTON1));
		assertTrue(workspace.press(press, List.of(angle)));
		assertTrue(workspace.release());
		assertEquals(app.layerText(messageKey), instructions.getText());
		assertEquals(app.layerText("ControlLayout.Attach"), attach.getText());
	}

	private void assertInteractiveAttachment() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var workspace = view.controlLayoutWorkspace();
			GeoNumeric slider = (GeoNumeric) absolute(app, "s=Slider(0,10,1)");
			slider.setAbsoluteScreenLocActive(true);
			slider.setSliderFixed(false);
			slider.setAbsoluteScreenLoc(300, 180);
			slider.setSliderWidth(240, true);
			slider.updateRepaint();
			GeoNumeric angle = (GeoNumeric) must(app, "t=Slider(0,6,0.1,1,100,true)");
			angle.addViews3D();
			assertFalse(app.isEuclidianView3Dinited());
			assertTrue(angle.isVisibleInView3D());
			var views = new ViewLocationModel(app, mock(IGraphicsViewLocationListener.class));
			views.setGeos(new Object[] {angle});
			angle.setVisibleInViewForPlane(false);
			angle.setSliderFixed(false);
			angle.setAbsoluteScreenLocActive(false);
			angle.setRealWorldLoc(5, -1);
			angle.setSliderWidth(3, true);
			angle.setSliderHorizontal(false);
			angle.updateRepaint();
			assertReadOnlySliderProperties(app, slider, angle);
			sample(view, new GPoint(1, 1));
			var existingWindows = List.of(Window.getWindows());
			try {
				app.openControlLayout();
				JDialog manager = null;
				for (Window window : Window.getWindows()) {
					if (!existingWindows.contains(window) && window instanceof JDialog dialog
							&& app.layerText("ControlLayout.Title").equals(dialog.getTitle())) {
						manager = dialog;
						break;
					}
				}
				assertNotNull(manager);
				var content = manager.getContentPane();
				var instructions = findComponent(content, JLabel.class,
						label -> app.layerText("ControlLayout.Pick").equals(label.getText()));
				var create = findComponent(content, JButton.class,
						button -> app.layerText("ControlLayout.New").equals(button.getText()));
				var attach = findComponent(content, JButton.class,
						button -> app.layerText("ControlLayout.Attach").equals(button.getText()));
				assertNotNull(instructions);
				assertNotNull(create);
				assertNotNull(attach);

				create.doClick(0);
				Panel original = layout(app).panels().get(0);
				manager.validate();
				assertTrue(instructions.getWidth() >= instructions.getPreferredSize().width);
				assertTrue(instructions.getVisibleRect().contains(
						0, 0, instructions.getWidth(), instructions.getHeight()));
				assertNull(layout(app).slot(slider));

				var selection = app.getSelectionManager();
				selection.setSelectedGeos(List.of(slider, angle), true);
				attach.doClick(0);
				assertEquals(app.layerText("ControlLayout.SingleSelection"), instructions.getText());
				selection.setSelectedGeos(List.of(angle), true);
				attach.doClick(0);
				assertEquals(app.layerText("ControlLayout.RemoveFrom3D"), instructions.getText());
				assertTrue(angle.isVisibleInView3D());
				assertTrue(workspace.eligibleContext());
				assertFalse(workspace.cancelGesture());
				views.applyToEuclidianView3D(false);
				assertFalse(app.isEuclidianView3Dinited());
				assertFalse(angle.isVisibleInView3D());

				var location = ((DrawSlider) view.getDrawableFor(slider)).getSliderLocation();
				GPoint sliderHit = new GPoint((int) location.x + 15, (int) location.y);
				view.setHits(sliderHit, PointerEventType.MOUSE);
				assertTrue(view.getHits().contains(slider));
				selection.clearSelectedGeos();
				attach.doClick(0);
				assertEquals(app.layerText("ControlLayout.Capture"), instructions.getText());
				escape(app, attach);
				assertTrue(manager.isShowing());
				assertEquals(app.layerText("ControlLayout.Attach"), attach.getText());
				assertFalse(workspace.cancelGesture());
				attach.doClick(0);
				click(view, sliderHit);
				assertEquals(app.layerText("ControlLayout.ConfirmPlacement"), attach.getText());
				assertSame(original, layout(app).panel(original.id()));
				assertNull(layout(app).slot(slider));
				escape(app, attach);
				assertTrue(manager.isShowing());
				assertEquals(app.layerText("ControlLayout.Attach"), attach.getText());
				assertNull(layout(app).slot(slider));
				selection.setSelectedGeos(List.of(slider), true);
				attach.doClick(0);
				attach.doClick(0);
				var sliderSlot = layout(app).slot(slider);
				assertSame(slider, sliderSlot.control());
				Panel enlarged = layout(app).panel(original.id());
				assertTrue(enlarged.width() > original.width());
				assertEquals(original.x(), enlarged.x());
				assertEquals(app.layerText("ControlLayout.Pick"), instructions.getText());
				assertTrue(slider.isSliderHorizontal());
				assertFalse(slider.isVisibleInView3D());

				selection.setSelectedGeos(List.of(angle), true);
				awaitQuiescent(app);
				attach.doClick(0);
				var sliderAt = view.getControlLayoutPosition(slider);
				click(view, new GPoint((int) sliderAt.x, (int) sliderAt.y));
				assertNull(layout(app).slot(angle));
				assertEquals(app.layerText("ControlLayout.PlacementOccupied"), instructions.getText());
				var insets = view.getSafeAreaInsets();
				click(view, new GPoint((int) enlarged.x() + insets.getLeft() + 8,
						(int) enlarged.y() + insets.getTop() + enlarged.height() - 4));
				var angleSlot = layout(app).slot(angle);
				assertNotNull(angleSlot);
				assertTrue(angleSlot.dy() > sliderSlot.dy());
				assertTrue(layout(app).panel(original.id()).height() > enlarged.height());
				assertEquals(3 * view.getYscale(), angleSlot.sliderWidth());
				assertEquals(5, angle.getRealWorldLocX());
				assertFalse(angle.isAbsoluteScreenLocActive());
				assertSame(sliderSlot, layout(app).slot(slider));
				awaitQuiescent(app);
				undo(app).undo();
				assertNull(layout(app).slot(app.getKernel().lookupLabel("t")));
				assertEquals(enlarged, layout(app).panel(original.id()));
				undo(app).redo();
				var restored = (GeoNumeric) app.getKernel().lookupLabel("t");
				assertFalse(restored.isSliderHorizontal());
				assertFalse(restored.isVisibleInView3D());
				assertEquals(angleSlot.id(), layout(app).slot(restored).id());
				assertEquals(angleSlot.sliderWidth(), view.getControlLayoutSliderWidth(restored));

				var at = view.getControlLayoutPosition(restored);
				GPoint start = new GPoint((int) at.x, (int) at.y - 10);
				GPoint end = new GPoint(start.x + 16, start.y + 14);
				drag(view, start, end);
				var moved = layout(app).slot(restored);
				assertEquals(angleSlot.dx() + 16, moved.dx());
				assertEquals(angleSlot.dy() + 14, moved.dy());
				assertEquals(angleSlot.sliderWidth(), moved.sliderWidth());

				var detach = findComponent(content, JButton.class,
						button -> app.layerText("ControlLayout.Detach").equals(button.getText()));
				assertNotNull(detach);
				detach.doClick(0);
				assertNull(layout(app).slot(restored));
				assertEquals(5, restored.getRealWorldLocX());
				assertEquals(-1, restored.getRealWorldLocY());
				assertEquals(3, restored.getSliderWidth());
				assertFalse(restored.isAbsoluteScreenLocActive());
				assertFalse(restored.isSliderHorizontal());
				assertFalse(restored.isVisibleInView3D());
				assertPanelProperties(app, content, original.id());
				var archive = temporary.resolve("styled-control-panel.cedg").toFile();
				assertTrue(app.saveGeoGebraFile(archive));
				AppGeoCeDG reopened = G9U1TestApp.create();
				assertTrue(reopened.loadFile(archive, false));
				assertEquals(layout(app).panel(original.id()), layout(reopened).panel(original.id()));
				var reopenedAngle = (GeoNumeric) reopened.getKernel().lookupLabel("t");
				assertFalse(reopenedAngle.isSliderHorizontal());
				assertFalse(reopenedAngle.isVisibleInView3D());
				escape(app, attach);
				assertFalse(manager.isDisplayable());
				assertFalse(workspace.isOpen());
				workspace.openPanel(original.id());
				assertTrue(workspace.isOpen());
				JDialog reopenedManager = Arrays.stream(Window.getWindows())
						.filter(window -> window instanceof JDialog dialog && dialog.isShowing()
								&& app.layerText("ControlLayout.Title").equals(dialog.getTitle()))
						.map(JDialog.class::cast).findFirst().orElseThrow();
				reopenedManager.setVisible(false);
				assertFalse(workspace.isOpen());
				workspace.openPanel(original.id());
				assertTrue(workspace.isOpen());
				assertFalse(reopenedManager.isDisplayable());
			} finally {
				workspace.close();
			}
			return null;
		});
	}

	private static void escape(AppGeoCeDG app, Component source) {
		for (int id : new int[] {KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED}) {
			assertTrue(app.dispatchKeyEvent(new KeyEvent(source, id, 0, 0,
					KeyEvent.VK_ESCAPE, (char) 27)));
		}
	}

	private static void assertReadOnlySliderProperties(AppGeoCeDG app, GeoNumeric slider,
			GeoNumeric angle) {
		var editor = new SliderPropertiesPanelD(app, null, false, true);
		slider.setLineOpacity(127);
		angle.setLineOpacity(127);
		editor.updatePanel(new Object[] {angle});
		editor.updatePanel(new Object[] {slider});
		editor.updatePanel(new Object[] {angle, slider});
		assertEquals(127, slider.getLineOpacity());
		assertEquals(127, angle.getLineOpacity());
		assertTrue(slider.isSliderHorizontal());
		assertFalse(angle.isSliderHorizontal());
	}

	private static void assertPanelProperties(AppGeoCeDG app, Container content, String panelId)
			throws Exception {
		String title = app.layerText("ControlLayout.PanelProperties");
		var action = findComponent(content, JButton.class, button -> title.equals(button.getText()));
		assertNotNull(action);
		var edit = new FutureTask<Void>(() -> {
			var editor = List.of(Window.getWindows()).stream()
					.filter(window -> window instanceof JDialog dialog && dialog.isShowing()
							&& title.equals(dialog.getTitle()))
					.map(JDialog.class::cast).findFirst().orElseThrow();
			try {
				var name = findComponent(editor, JTextField.class, field -> field.getColumns() == 28);
				var color = findComponent(editor, GeoGebraColorChooser.class, chooser -> true);
				var transparency = findComponent(editor, JSlider.class, slider ->
						app.layerText("ControlLayout.Transparency").equals(
								slider.getAccessibleContext().getAccessibleName()));
				assertNotNull(name);
				assertNotNull(color);
				assertNotNull(transparency);
				name.setText("Panel de prueba");
				color.setColor(new Color(0x4090a0));
				transparency.setValue(40);
				editor.getRootPane().getDefaultButton().doClick(0);
			} finally {
				editor.dispose();
			}
			return null;
		});
		SwingUtilities.invokeLater(edit);
		action.doClick(0);
		assertTrue(edit.isDone());
		edit.get();
		assertEquals("Panel de prueba", layout(app).panel(panelId).name());
		assertEquals(60, layout(app).panel(panelId).opacity());
		assertEquals(0x4090a0, layout(app).panel(panelId).rgb());
	}

	@Test
	void overlappingPanelsCompositeBackToFront() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			Panel rear = layout.addPanel(Anchor.FIXED, 50, 50, 180, 155, true,
					"Rear", 0x5599dd, 65);
			GeoBoolean check = checkbox(app);
			layout.addSlot(rear.id(), check, 30, 70);
			view.controlLayoutWorkspace().refreshPanel(rear.id());
			GPoint sample = controlHit(view, check);
			Color original = sample(view, sample);
			view.setHits(sample, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(check));

			Panel middle = layout.addPanel(Anchor.FIXED, 65, 72, 150, 110, true,
					"Middle", 0xffaa22, ControlLayout.MIN_OPACITY);
			assertComposite(original, middle, sample(view, sample));
			Color twice = sample(view, sample);
			Panel front = layout.addPanel(Anchor.FIXED, 78, 87, 145, 115, true,
					"Front", 0x129943, ControlLayout.MAX_OPACITY);
			assertComposite(twice, front, sample(view, sample));
			assertSame(front, view.controlLayoutWorkspace().panelAt(sample.x, sample.y));
			view.setHits(sample, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(check));
			click(view, sample);
			assertTrue(check.getBoolean());
			layout.removePanel(front.id());
			layout.removePanel(middle.id());
			click(view, sample);
			assertFalse(check.getBoolean());
		});
	}

	@Test
	void detachMovesTheControlBehindItsOldPanel() throws Exception {
		assertRejectingFixedAngleKeepsItsExplicit3dExclusion();
		assertInteractiveAttachment();
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			Panel panel = layout.addPanel(Anchor.FIXED, 55, 50, 200, 160, true,
					"Panel", 0xcc5500, 70);
			GeoBoolean check = checkbox(app);
			layout.addSlot(panel.id(), check, 32, 64);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			GPoint point = controlHit(view, check);
			Color grouped = sample(view, point);
			layout.removeSlot(check);
			check.updateRepaint();
			assertNull(view.getControlLayoutPosition(check));
			assertComposite(grouped, panel, sample(view, point));
			view.setHits(point, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(check));
			click(view, point);
			assertTrue(check.getBoolean());
			layout.removePanel(panel.id());
			view.setHits(point, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(check));
			click(view, point);
			assertFalse(check.getBoolean());
		});
	}

	@Test
	void contextResolvesTheFrontAccessiblePanelWithoutChangingTheLayout() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			try {
				assertContext(app);
			} finally {
				if (app.getGuiManager().showView(App.VIEW_PROPERTIES)) {
					app.getGuiManager().setShowView(false, App.VIEW_PROPERTIES);
				}
				GeoGebraFrame.getInstances().stream()
						.filter(frame -> frame.getApplication() == app).toList()
						.forEach(frame -> {
							if (frame.isDisplayable()) {
								frame.removeNotify();
							}
							frame.dispose();
						});
			}
			return null;
		});
	}

	private static void assertContext(AppGeoCeDG app) throws Exception {
		GeoCeDGEuclidianView view = view(app);
		var layout = layout(app);
		var workspace = view.controlLayoutWorkspace();
		var slider = (GeoNumeric) absolute(app, "s=Slider(0,10,1)");
		var button = (GeoButton) absolute(app, "b=Button()");
		var check = checkbox(app);
		var field = absolute(app, "f=InputBox(s)");
		var loose = (GeoButton) absolute(app, "loose=Button()");
		loose.setAbsoluteScreenLoc(450, 120);
		loose.updateRepaint();
		button.setClickScript(app.createScript(ScriptType.GGBSCRIPT, "SetValue(s,9)", true));
		Panel rear = layout.addPanel(Anchor.FIXED, 40, 40, 340, 220, true);
		Panel front = layout.addPanel(Anchor.FIXED, 270, 105, 105, 70, true);
		layout.addSlot(rear.id(), slider, 30, 50);
		layout.addSlot(rear.id(), button, 30, 85);
		layout.addSlot(rear.id(), check, 30, 125);
		layout.addSlot(rear.id(), field, 30, 163);
		workspace.refreshPanel(rear.id());
		baseline(app);
		slider.setValue(4);
		store(app);
		undo(app).undo();
		assertTrue(undo(app).redoPossible());
		int history = undo(app).getHistorySize();
		rear = layout.panel(rear.id());
		front = layout.panel(front.id());
		String before = constructionXML(app);

		var restoredSlider = (GeoNumeric) app.getKernel().lookupLabel("s");
		var restoredButton = app.getKernel().lookupLabel("b");
		var restoredCheck = app.getKernel().lookupLabel("q");
		var restoredField = app.getKernel().lookupLabel("f");
		var fieldDrawable = (DrawInputBox) view.getDrawableFor(restoredField);
		sample(view, new GPoint(1, 1));
		var fieldBounds = fieldDrawable.getInputFieldBounds();
		var sliderAt = ((DrawSlider) view.getDrawableFor(restoredSlider)).getSliderLocation();
		var clickPoints = List.of(new GPoint((int) sliderAt.x + 15, (int) sliderAt.y),
				controlHit(view, restoredButton), controlHit(view, restoredCheck),
				new GPoint((int) fieldBounds.getX() + 10, (int) fieldBounds.getY() + 10));
		var controls = List.of(restoredSlider, restoredButton, restoredCheck, restoredField);
		for (int i = 0; i < controls.size(); i++) {
			GPoint at = clickPoints.get(i);
			var context = workspace.contextAt(at, PointerEventType.MOUSE);
			assertSame(rear, context.panel());
			assertSame(controls.get(i), context.control());
			var menu = workspace.panelContextMenu(context, at, PointerEventType.MOUSE);
			assertEquals(3, menu.getComponentCount());
			((JMenuItem) menu.getComponent(0)).doClick(0);
			assertTrue(app.getGuiManager().showView(App.VIEW_PROPERTIES));
			assertEquals(List.of(controls.get(i)), app.getSelectionManager().getSelectedGeos());
			((PropertiesViewD) app.getGuiManager().getPropertiesView()).closeDialog();
			awaitQuiescent(app);
			assertTrue(undo(app).redoPossible(), "Closing unchanged properties preserves Redo");
			assertEquals(history, undo(app).getHistorySize());
		}
		assertPanelMenu(workspace, front, new GPoint(300, 115));
		assertPanelMenu(workspace, front, new GPoint(300, 160));
		assertPanelMenu(workspace, rear, new GPoint(50, 52));
		assertNull(workspace.contextAt(new GPoint(460, 130), PointerEventType.MOUSE));
		view.setHits(new GPoint(460, 130), PointerEventType.MOUSE);
		assertTrue(view.getHits().contains(app.getKernel().lookupLabel("loose")));

		GPoint buttonAt = clickPoints.get(1);
		var buttonContext = workspace.contextAt(buttonAt, PointerEventType.MOUSE);
		var stale = workspace.panelContextMenu(buttonContext, buttonAt, PointerEventType.MOUSE);
		var controller = view.getEuclidianController();
		controller.textfieldHasFocus(true);
		try {
			assertFalse(workspace.eligibleContext());
			((JMenuItem) stale.getComponent(0)).doClick(0);
			assertFalse(app.getGuiManager().showView(App.VIEW_PROPERTIES));
		} finally {
			controller.textfieldHasFocus(false);
		}

		var manager = spy(workspace);
		doNothing().when(manager).openPanel(rear.id());
		var memberMenu = manager.panelContextMenu(buttonContext, buttonAt, PointerEventType.MOUSE);
		((JMenuItem) memberMenu.getComponent(1)).doClick(0);
		verify(manager).openPanel(rear.id());

		var cover = layout.addPanel(Anchor.FIXED, 55, 110, 120, 60, true);
		assertPanelMenu(workspace, cover, buttonAt);
		((JMenuItem) stale.getComponent(0)).doClick(0);
		assertFalse(app.getGuiManager().showView(App.VIEW_PROPERTIES));
		layout.removePanel(cover.id());
		assertEquals(before, constructionXML(app));
		assertTrue(undo(app).redoPossible());

		GPoint sliderAtPoint = clickPoints.get(0);
		var sliderMenu = workspace.panelContextMenu(
				workspace.contextAt(sliderAtPoint, PointerEventType.MOUSE),
				sliderAtPoint, PointerEventType.MOUSE);
		((JMenuItem) sliderMenu.getComponent(0)).doClick(0);
		restoredSlider.setValue(7);
		((PropertiesViewD) app.getGuiManager().getPropertiesView()).closeDialog();
		awaitQuiescent(app);
		assertEquals(history + 1, undo(app).getHistorySize());
		assertFalse(undo(app).redoPossible());
	}

	private static void assertPanelMenu(GeoCeDGControlLayoutWorkspace workspace, Panel expected,
			GPoint at) {
		var context = workspace.contextAt(at, PointerEventType.MOUSE);
		assertSame(expected, context.panel());
		assertNull(context.control());
		assertEquals(2, workspace.panelContextMenu(context, at, PointerEventType.MOUSE)
				.getComponentCount());
	}

	private static String constructionXML(AppGeoCeDG app) {
		var xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, true);
		return xml.toString();
	}

	@Test
	void longUnicodeHeaderFitsWithoutSplittingCharacters() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			String name = "Δ𐐷e\u0301".repeat(12);
			Panel panel = layout(app).addPanel(Anchor.FIXED, 50, 50, 85, 90, true,
					name, 0x51aa29, 65);
			var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
			var graphics = image.createGraphics();
			try {
				var surface = new GGraphics2DD(graphics);
				var workspace = view.controlLayoutWorkspace();
				String fitted = workspace.fittedName(surface, panel.name(), 60);
				assertTrue(fitted.endsWith("…"));
				assertTrue(fitted.length() < name.length());
				assertTrue(fitted.codePoints().noneMatch(cp -> cp >= 0xd800 && cp <= 0xdfff));
				workspace.paint(surface);
				var insets = view.getSafeAreaInsets();
				assertEquals(0x365b7b,
						image.getRGB(insets.getLeft() + 52, insets.getTop() + 52) & 0xffffff);
			} finally {
				graphics.dispose();
			}
		});
	}

	@Test
	void panAndAnisotropicZoomMoveOnlyTheWorldOrigin() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			GeoNumeric slider = (GeoNumeric) absolute(app, "s=Slider(0,10,1)");
			GeoElement field = absolute(app, "f=InputBox(s)");
			GeoElement button = absolute(app, "b=Button()");
			GeoBoolean checkbox = (GeoBoolean) absolute(app, "q=Checkbox()");
			checkbox.setCheckboxFixed(false);
			Panel world = layout.addPanel(Anchor.WORLD, 2, 1, 100, 80, true);
			Panel fixed = layout.addPanel(Anchor.FIXED, 40, 60, 100, 80, true);
			layout.addSlot(world.id(), slider, 4, 15);
			layout.addSlot(world.id(), field, 5, 32);
			layout.addSlot(fixed.id(), button, 12, 31);
			layout.addSlot(fixed.id(), checkbox, 30, 58);

			view.setCoordSystem(200, 210, 50, 40);
			var sliderDrawable = (DrawSlider) view.getDrawableFor(slider);
			assertNotNull(sliderDrawable);
			assertPosition(304, 185, sliderDrawable.getSliderLocation());
			assertPosition(305, 202, view.getControlLayoutPosition(field));
			var buttonPosition = view.getControlLayoutPosition(button);
			var checkboxPosition = view.getControlLayoutPosition(checkbox);
			view.setCoordSystem(280, 250, 22, 60);
			assertPosition(328, 205, sliderDrawable.getSliderLocation());
			assertPosition(329, 222, view.getControlLayoutPosition(field));
			assertPosition(buttonPosition.x, buttonPosition.y, view.getControlLayoutPosition(button));
			assertPosition(checkboxPosition.x, checkboxPosition.y,
					view.getControlLayoutPosition(checkbox));
			assertEquals(87, slider.getAbsoluteScreenLocX());
			assertEquals(114, slider.getAbsoluteScreenLocY());
			app.setExporting(ExportType.PNG, 1);
			try {
				assertNull(view.getControlLayoutPosition(slider));
				assertPosition(87, 114, sliderDrawable.getSliderLocation());
				assertNull(view.getControlLayoutPosition(field));
			} finally {
				app.setExporting(ExportType.NONE, 1);
			}
		});
	}

	@Test
	void panelLayerAndStepFilterHitsWithoutChangingTheControl() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			GeoBoolean q = (GeoBoolean) absolute(app, "q=Checkbox()");
			q.setCheckboxFixed(false);
			q.setLayer(3);
			must(app, "n=7");
			GeoElement field = absolute(app, "f=InputBox(n)");
			Panel panel = layout.addPanel(Anchor.FIXED, 40, 60, 200, 90, true);
			layout.addSlot(panel.id(), q, 20, 30);
			layout.addSlot(panel.id(), field, 120, 45);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			assertTrue(view.isControlLayoutVisible(q));
			GPoint2D at = view.getControlLayoutPosition(q);
			var hit = new GPoint((int) at.x + 10, (int) at.y + 10);
			view.setHits(hit, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(q));
			click(view, hit);
			assertFalse(q.getBoolean());
			click(view, hit);
			assertTrue(q.getBoolean());
			app.getLayerWorkspace().setLayerHidden(3, true);
			view.setHits(hit, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(q));
			app.getLayerWorkspace().setLayerHidden(3, false);

			layout.updatePanel(new Panel(panel.id(), panel.anchor(), panel.x(), panel.y(),
					panel.width(), panel.height(), false));
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			view.setHits(hit, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(q));
			layout.updatePanel(panel);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			double fieldX = drawnFieldX(view, field);
			int step = q.getConstruction().getStep();
			q.getConstruction().setStep(-1);
			assertFalse(view.isControlLayoutVisible(q));
			q.getConstruction().setStep(step);
			assertTrue(view.isControlLayoutVisible(q));
			assertTrue(view.isControlLayoutVisible(field));
			assertEquals(fieldX, drawnFieldX(view, field), 1);
			assertTrue(q.getBoolean());
			assertEquals(3, q.getLayer());
			assertEquals(87, q.getAbsoluteScreenLocX());
			assertFieldEnclosureAfterUndoAndThumbnail();
			return null;
		});
	}

	private static void assertFieldEnclosureAfterUndoAndThumbnail() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGEuclidianView view = view(app);
		must(app, "n=7");
		GeoInputBox field = (GeoInputBox) absolute(app, "f=InputBox(n)");
		field.setLabelVisible(true);
		var layout = layout(app);
		Panel panel = layout.addPanel(Anchor.FIXED, 330, 145, 60, 80, true);
		var slot = layout.addSlot(panel.id(), field, 18, 43);
		view.controlLayoutWorkspace().refreshPanel(panel.id());
		drawnFieldX(view, field);
		var drawable = (DrawInputBox) view.getDrawableFor(field);
		var widget = drawable.getInputFieldBounds();
		int right = (int) Math.ceil(Math.max(widget.getMaxX(), drawable.getBounds().getMaxX()));
		var fill = new GPoint(right + 3, (int) (widget.getY() + widget.getHeight() / 2));
		GPoint2D fieldOrigin = view.getControlLayoutPosition(field);
		assertTrue(fill.x > fieldOrigin.x - slot.dx() + panel.width());
		Color expectedFill = sample(view, fill);
		assertComposite(Color.WHITE, panel, expectedFill);
		baseline(app);
		layout.updatePanel(panel.withStyle("Field", 0x265a90, 70));
		store(app);
		undo(app).undo();

		GeoInputBox restored = (GeoInputBox) app.getKernel().lookupLabel("f");
		assertNotNull(restored);
		assertNotSame(field, restored);
		Panel restoredPanel = layout.panel(panel.id());
		assertEquals(panel, restoredPanel);
		Color firstPaint = sample(view, fill);
		assertSame(restoredPanel, view.controlLayoutWorkspace().panelAt(fill.x, fill.y));

		var archive = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(archive, true);
		try (var entries = new ZipInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
			boolean thumbnail = false;
			for (var entry = entries.getNextEntry(); entry != null; entry = entries.getNextEntry()) {
				thumbnail |= MyXMLio.XML_FILE_THUMBNAIL.equals(entry.getName());
			}
			assertTrue(thumbnail, "the native writer rendered a thumbnail");
		}
		assertFalse(app.isExporting());
		Color paintAfterThumbnail = sample(view, fill);
		assertAll(() -> assertEquals(expectedFill, firstPaint, "first paint after Undo"),
				() -> assertEquals(expectedFill, paintAfterThumbnail, "first paint after thumbnail"));
		assertEquals(87, restored.getAbsoluteScreenLocX());
		assertTrue(undo(app).redoPossible());
	}

	@Test
	void panelRefreshVisitsOnlyItsSlotsAtTheDocumentLimit() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var kernel = app.getKernel();
			var cons = kernel.getConstruction();
			var layout = layout(app);
			kernel.setNotifyViewsActive(false);
			try {
				for (int i = 0; i < 1000; i++) {
					GeoPoint unrelated = new GeoPoint(cons, i, i + 1, 1);
					unrelated.setEuclidianVisible(false);
					unrelated.setAuxiliaryObject(true);
					unrelated.setLoadedLabel("P_{" + i + "}");
				}
				Panel first = layout.addPanel(Anchor.FIXED, 20, 20, 200, 200, true);
				Panel second = layout.addPanel(Anchor.FIXED, 240, 20, 200, 200, true);
				for (int i = 0; i < 256; i++) {
					GeoBoolean q = new GeoBoolean(cons, true);
					q.setCheckboxFixed(false);
					q.setEuclidianVisible(true);
					q.setLoadedLabel("q_{" + i + "}");
					layout.addSlot(i < 12 ? first.id() : second.id(), q, 0, 30 + i * 20);
					view.add(q);
				}
				assertEquals(1256, cons.getGeoSetConstructionOrder().size());
				assertEquals(12, view.controlLayoutWorkspace().refreshPanel(first.id()));
				assertEquals(244, view.controlLayoutWorkspace().refreshPanel(second.id()));
			} finally {
				cons.clearConstruction();
				kernel.setNotifyViewsActive(true);
			}
		});
	}

	private static GeoCeDGEuclidianView view(AppGeoCeDG app) {
		var view = (GeoCeDGEuclidianView) app.getEuclidianView1();
		view.getJPanel().setSize(800, 600);
		return view;
	}

	private static <T extends Component> T findComponent(Container root, Class<T> type,
			Predicate<T> matches) {
		for (var child : root.getComponents()) {
			if (type.isInstance(child) && matches.test(type.cast(child))) {
				return type.cast(child);
			}
			if (child instanceof Container nested) {
				T match = findComponent(nested, type, matches);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}

	private static ControlLayout layout(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getControlLayout();
	}

	private static GeoElement absolute(AppGeoCeDG app, String command) {
		GeoElement geo = must(app, command);
		((AbsoluteScreenLocateable) geo).setAbsoluteScreenLoc(87, 114);
		geo.setEuclidianVisible(true);
		geo.updateRepaint();
		return geo;
	}

	private static GeoBoolean checkbox(AppGeoCeDG app) {
		GeoBoolean check = (GeoBoolean) absolute(app, "q=Checkbox()");
		check.setCheckboxFixed(false);
		return check;
	}

	private static GPoint controlHit(GeoCeDGEuclidianView view, GeoElement control) {
		GPoint2D at = view.getControlLayoutPosition(control);
		return new GPoint((int) at.x + 10, (int) at.y + 10);
	}

	private static Color sample(GeoCeDGEuclidianView view, GPoint point) {
		var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			view.drawObjects(new GGraphics2DD(graphics));
			return new Color(image.getRGB(point.x, point.y));
		} finally {
			graphics.dispose();
		}
	}

	private static void assertComposite(Color behind, Panel front, Color actual) {
		var tint = new Color(front.rgb());
		double alpha = Math.round(front.opacity() * 255 / 100.0) / 255.0;
		assertEquals(behind.getRed() * (1 - alpha) + tint.getRed() * alpha, actual.getRed(), 1);
		assertEquals(behind.getGreen() * (1 - alpha) + tint.getGreen() * alpha,
				actual.getGreen(), 1);
		assertEquals(behind.getBlue() * (1 - alpha) + tint.getBlue() * alpha, actual.getBlue(), 1);
	}

	private static void assertPosition(double x, double y, GPoint2D actual) {
		assertNotNull(actual);
		assertEquals(x, actual.x, 0.01);
		assertEquals(y, actual.y, 0.01);
	}

	private static void click(GeoCeDGEuclidianView view, GPoint point) {
		var controller = (GeoCeDGEuclidianController) view.getEuclidianController();
		controller.mousePressed(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_PRESSED,
				0, MouseEvent.BUTTON1_DOWN_MASK, point.x, point.y, 1, false, MouseEvent.BUTTON1));
		controller.mouseReleased(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_RELEASED,
				1, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
	}

	private static void drag(GeoCeDGEuclidianView view, GPoint start, GPoint end) {
		var controller = (GeoCeDGEuclidianController) view.getEuclidianController();
		controller.mousePressed(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_PRESSED,
				0, MouseEvent.BUTTON1_DOWN_MASK, start.x, start.y, 1, false, MouseEvent.BUTTON1));
		controller.mouseDragged(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_DRAGGED,
				1, MouseEvent.BUTTON1_DOWN_MASK, end.x, end.y, 0, false, MouseEvent.NOBUTTON));
		controller.mouseReleased(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_RELEASED,
				2, 0, end.x, end.y, 1, false, MouseEvent.BUTTON1));
	}

	private static double drawnFieldX(GeoCeDGEuclidianView view, GeoElement field) {
		var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		try {
			view.drawObjects(new GGraphics2DD(graphics));
			var drawable = (DrawInputBox) view.getDrawableFor(field);
			assertNotNull(drawable);
			return drawable.getInputFieldBounds().getX();
		} finally {
			graphics.dispose();
		}
	}
}
