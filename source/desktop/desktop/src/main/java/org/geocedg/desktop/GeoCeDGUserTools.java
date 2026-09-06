/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geocedg.desktop.GeoCeDGUserToolLibrary.Package;
import org.geocedg.desktop.GeoCeDGUserToolLibrary.PinnedCommand;
import org.geogebra.desktop.gui.dialog.ToolManagerDialogD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Dynamic application tools are separate from the immutable product action catalog. */
public final class GeoCeDGUserTools {

	private static final Map<AppD, WeakReference<GeoCeDGUserTools>> INSTANCES =
			new WeakHashMap<>();
	private final AppD app;
	private final GeoCeDGActionRegistry registry;
	private final GeoCeDGUserToolLibrary library;
	private final String loadFailure;
	private JToggleButton nativeVisualReference;

	private GeoCeDGUserTools(AppD app) {
		this.app = app;
		this.registry = ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
		GeoCeDGUserToolLibrary loaded = null;
		String failure = null;
		try {
			loaded = new GeoCeDGUserToolLibrary(app, storagePath());
		} catch (IOException exception) {
			failure = exception.getMessage();
		}
		library = loaded;
		loadFailure = failure;
		nativeVisualReference = GeoCeDGToolbarContainer.createNativeToolReference(app);
	}

	GeoCeDGUserTools(AppD app, GeoCeDGUserToolLibrary library) {
		this.app = app;
		this.registry = ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
		this.library = library;
		this.loadFailure = null;
		this.nativeVisualReference = GeoCeDGToolbarContainer.createNativeToolReference(app);
	}

	private static Path storagePath() {
		Path preferences;
		try {
			preferences = GeoGebraPreferencesD.getFile().toPath().toAbsolutePath();
		} catch (NullPointerException exception) {
			preferences = GeoCeDG.getDefaultPreferencesFile();
		}
		return preferences.resolveSibling(preferences.getFileName() + ".user-tools-v1.json");
	}

	private static synchronized GeoCeDGUserTools get(AppD app) {
		WeakReference<GeoCeDGUserTools> reference = INSTANCES.get(app);
		GeoCeDGUserTools result = reference == null ? null : reference.get();
		if (result == null) {
			result = new GeoCeDGUserTools(app);
			INSTANCES.put(app, new WeakReference<>(result));
		}
		return result;
	}

	/**
	 * @param app product app
	 * @return current installed tools and the existing management action
	 */
	public static JMenu createMenu(AppD app) {
		GeoCeDGUserTools tools = get(app);
		JMenu menu = new JMenu(tools.text("UserTools.Title"));
		menu.getAccessibleContext().setAccessibleName(menu.getText());
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent event) {
				tools.populate(menu);
			}

			@Override
			public void menuDeselected(MenuEvent event) {
				// No geometric state is attached to menu presentation.
			}

			@Override
			public void menuCanceled(MenuEvent event) {
				// Cancelling creates nothing.
			}
		});
		tools.populate(menu);
		return menu;
	}

	/**
	 * @param app product app
	 * @param nativeVisualReference actual native button in the containing toolbar
	 * @return independent pinned user-tool group, empty when unpinned
	 */
	public static JComponent createPinnedToolbar(AppD app,
			JToggleButton nativeVisualReference) {
		GeoCeDGUserTools tools = get(app);
		tools.nativeVisualReference = nativeVisualReference;
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
		panel.getAccessibleContext().setAccessibleName(tools.text("UserTools.Title"));
		Runnable refresh = () -> tools.populatePins(panel);
		if (tools.library != null) {
			WeakReference<JPanel> reference = new WeakReference<>(panel);
			tools.library.addListener(() -> {
				JPanel live = reference.get();
				if (live != null) {
					tools.populatePins(live);
				}
			});
		}
		refresh.run();
		return panel;
	}

	/** @param app product app whose explicitly installed library is managed */
	public static void showManager(AppD app) {
		get(app).showDialog();
	}

	void populate(JMenu menu) {
		menu.removeAll();
		registry.refresh();
		JMenuItem management = GeoCeDGMenuBar.createItem(
				registry.get("automation.manage-user-tools"), new ButtonGroup());
		management.setFont(app.getPlainFont());
		menu.add(management);
		menu.addSeparator();
		if (!refreshLibrary()) {
			JMenuItem failure = new JMenuItem(text("UserTools.LibraryError"));
			failure.setFont(app.getPlainFont());
			failure.setEnabled(false);
			menu.add(failure);
			return;
		}
		if (library.packages().isEmpty()) {
			JMenuItem empty = new JMenuItem(text("UserTools.Empty"));
			empty.setFont(app.getPlainFont());
			empty.setEnabled(false);
			menu.add(empty);
		}
		for (Package tool : library.packages()) {
			String reason = library.unavailableReason(tool);
			for (String command : tool.commands()) {
				JMenuItem item = new JMenuItem(command);
				item.setFont(app.getPlainFont());
				item.setToolTipText(reason == null ? tool.name() : explain(reason));
				item.setEnabled(reason == null);
				item.getAccessibleContext().setAccessibleDescription(item.getToolTipText());
				item.addActionListener(event -> invoke(tool, command));
				menu.add(item);
			}
		}
	}

	void populatePins(JPanel panel) {
		panel.removeAll();
		if (refreshLibrary()) {
			List<PinnedCommand> ordered = library.pinnedCommands();
			Map<String, List<PinnedCommand>> grouped = new LinkedHashMap<>();
			for (PinnedCommand pin : ordered) {
				if (!pin.group().isEmpty()) {
					grouped.computeIfAbsent(pin.group(), ignored -> new ArrayList<>()).add(pin);
				}
			}
			Set<String> renderedGroups = new LinkedHashSet<>();
			for (PinnedCommand pin : ordered) {
				if (pin.group().isEmpty()) {
					panel.add(createPinnedButton(pin));
				} else if (renderedGroups.add(pin.group())) {
					List<PinnedCommand> group = grouped.get(pin.group());
					if (group.size() == 1) {
						panel.add(createPinnedButton(group.get(0)));
					} else {
						panel.add(createPinnedGroup(pin.group(), group));
					}
				} else {
					// The first command already rendered the shared dropdown.
				}
			}
		}
		panel.setVisible(panel.getComponentCount() > 0);
		panel.revalidate();
		panel.repaint();
	}

	private JToggleButton createPinnedButton(PinnedCommand pin) {
		String reason = library.unavailableReason(pin.tool());
		JToggleButton button = new JToggleButton();
		applyPinnedIcon(button, pin, false);
		configurePinnedButton(button, reason == null
				? pin.command() + " \u2014 " + pin.tool().name() : explain(reason));
		button.setEnabled(reason == null);
		button.getAccessibleContext().setAccessibleName(pin.command());
		button.putClientProperty("geocedg.userTool.command", pin.command());
		button.putClientProperty("geocedg.userTool.group", pin.group());
		button.addActionListener(event -> {
			button.setSelected(false);
			invoke(pin.tool(), pin.command());
		});
		return button;
	}

	private JToggleButton createPinnedGroup(String name, List<PinnedCommand> commands) {
		JToggleButton button = new JToggleButton();
		applyPinnedIcon(button, commands.get(0), true);
		button.putClientProperty("geocedg.userTool.activeCommand",
				commands.get(0).command());
		String description = name + " \u2014 " + String.join(", ", commands.stream()
				.map(PinnedCommand::command).toList());
		configurePinnedButton(button, description);
		button.getAccessibleContext().setAccessibleName(name);
		button.putClientProperty("geocedg.userTool.group", name);
		button.putClientProperty("geocedg.userTool.groupSize", commands.size());
		JPopupMenu popup = new JPopupMenu();
		for (PinnedCommand pin : commands) {
			String reason = library.unavailableReason(pin.tool());
			JMenuItem item = new JMenuItem(pin.command());
			item.setIcon(pinnedIcon(pin));
			item.setFont(app.getPlainFont());
			item.setEnabled(reason == null);
			item.setToolTipText(reason == null ? pin.tool().name() : explain(reason));
			item.getAccessibleContext().setAccessibleName(pin.command());
			item.getAccessibleContext().setAccessibleDescription(item.getToolTipText());
			item.addActionListener(event -> {
				applyPinnedIcon(button, pin, true);
				button.putClientProperty("geocedg.userTool.activeCommand", pin.command());
				invoke(pin.tool(), pin.command());
			});
			popup.add(item);
		}
		button.putClientProperty("geocedg.userTool.popup", popup);
		button.addActionListener(event -> {
			button.setSelected(false);
			popup.show(button, 0, button.getHeight());
		});
		return button;
	}

	private void configurePinnedButton(JToggleButton button, String description) {
		GeoCeDGToolbarContainer.applyNativeToolPresentation(button,
				nativeVisualReference);
		button.putClientProperty("geocedg.toolbar.nativeVisualReference",
				nativeVisualReference);
		button.setToolTipText(description);
		button.getAccessibleContext().setAccessibleDescription(description);
	}

	private void applyPinnedIcon(JToggleButton button, PinnedCommand pin,
			boolean dropdown) {
		Icon icon = pinnedIcon(pin);
		button.setIcon(dropdown ? new DropdownIcon(icon) : icon);
		button.setText(null);
		button.putClientProperty("geocedg.userTool.icon.source",
				pin.icon() == null ? "monogram" : "custom");
		button.putClientProperty("geocedg.userTool.monogram",
				pin.icon() == null ? monogram(pin.command()) : null);
	}

	private Icon pinnedIcon(PinnedCommand pin) {
		return pin.icon() == null
				? new MonogramIcon(monogram(pin.command()), app.getScaledIconSize())
				: toolbarIcon(pin.icon());
	}

	static String monogram(String fullName) {
		String value = fullName == null ? "" : fullName.strip();
		return value.codePoints().filter(Character::isLetter).findFirst()
				.stream().mapToObj(codePoint -> new String(Character.toChars(codePoint))
						.toUpperCase(Locale.ROOT)).findFirst().orElse("\u2022");
	}

	private ImageIcon toolbarIcon(GeoCeDGUserToolLibrary.PinIcon icon) {
		ImageIcon source = new ImageIcon(icon.toolbarBytes());
		int size = app.getScaledIconSize();
		return new ImageIcon(source.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	private static final class MonogramIcon implements Icon {
		private static final Color BACKGROUND = new Color(0xe7edf5);
		private static final Color BORDER = new Color(0x74849a);
		private static final Color FOREGROUND = new Color(0x26384f);
		private final String monogram;
		private final int size;

		MonogramIcon(String monogram, int size) {
			this.monogram = monogram;
			this.size = size;
		}

		@Override
		public int getIconWidth() {
			return size;
		}

		@Override
		public int getIconHeight() {
			return size;
		}

		@Override
		public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
			Graphics2D g2 = (Graphics2D) graphics.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(BACKGROUND);
				g2.fillRoundRect(x + 1, y + 1, size - 2, size - 2, size / 4, size / 4);
				g2.setColor(BORDER);
				g2.drawRoundRect(x + 1, y + 1, size - 3, size - 3, size / 4, size / 4);
				Font font = new Font(Font.SANS_SERIF, Font.BOLD,
						Math.max(12, Math.round(size * 0.55f)));
				g2.setFont(font);
				FontMetrics metrics = g2.getFontMetrics();
				int textX = x + (size - metrics.stringWidth(monogram)) / 2;
				int textY = y + (size - metrics.getHeight()) / 2 + metrics.getAscent();
				g2.setColor(MonogramIcon.FOREGROUND);
				g2.drawString(monogram, textX, textY);
			} finally {
				g2.dispose();
			}
		}
	}

	private static final class DropdownIcon implements Icon {
		private final Icon delegate;

		DropdownIcon(Icon delegate) {
			this.delegate = delegate;
		}

		@Override
		public int getIconWidth() {
			return delegate.getIconWidth();
		}

		@Override
		public int getIconHeight() {
			return delegate.getIconHeight();
		}

		@Override
		public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
			delegate.paintIcon(component, graphics, x, y);
			Graphics2D g2 = (Graphics2D) graphics.create();
			try {
				g2.setColor(MonogramIcon.FOREGROUND);
				int right = x + getIconWidth() - 3;
				int bottom = y + getIconHeight() - 3;
				g2.fillPolygon(new int[] {right - 6, right, right - 3},
						new int[] {bottom - 4, bottom - 4, bottom}, 3);
			} finally {
				g2.dispose();
			}
		}
	}

	private void invoke(Package tool, String command) {
		try {
			library.select(tool.id(), command);
		} catch (IOException exception) {
			failure(exception.getMessage());
		}
	}

	private void showDialog() {
		if (!refreshLibrary()) {
			failure("UserTools.LibraryError" + ": " + explain(loadFailure));
			return;
		}
		final JDialog dialog = new JDialog(app.getFrame(), text("UserTools.Title"), true);
		JPanel content = new JPanel(new BorderLayout(6, 6));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JTextArea scope = new JTextArea(text("UserTools.Scope"), 3, 50);
		scope.setEditable(false);
		scope.setLineWrap(true);
		scope.setWrapStyleWord(true);
		scope.setOpaque(false);
		scope.setFont(app.getPlainFont());
		content.add(scope, BorderLayout.NORTH);
		JList<Package> list = new JList<>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setListData(library.packages().toArray(new Package[0]));
		list.getAccessibleContext().setAccessibleName(text("UserTools.Installed"));
		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(620, 180));
		content.add(scroll, BorderLayout.CENTER);
		JPanel south = new JPanel(new BorderLayout());
		JPanel pins = new JPanel(new FlowLayout(FlowLayout.LEADING));
		south.add(pins, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JButton install = new JButton(text("UserTools.Install"));
		JButton remove = new JButton(text("UserTools.Remove"));
		JButton document = new JButton(text("UserTools.Document"));
		JButton close = new JButton(text("UserTools.Close"));
		buttons.add(install);
		buttons.add(remove);
		buttons.add(document);
		buttons.add(close);
		south.add(buttons, BorderLayout.SOUTH);
		content.add(south, BorderLayout.SOUTH);
		list.addListSelectionListener(event -> {
			Package tool = list.getSelectedValue();
			remove.setEnabled(tool != null);
			populateManagerPins(pins, tool);
		});
		remove.setEnabled(false);
		install.addActionListener(event -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("GeoGebra tools (.ggt)", "ggt"));
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
				try {
					Path file = chooser.getSelectedFile().toPath();
					if (Files.size(file) > GeoCeDGUserToolLibrary.MAX_BYTES) {
						throw new IOException("UserTools.Limit");
					}
					library.install(file.getFileName().toString(), Files.readAllBytes(file));
					list.setListData(library.packages().toArray(new Package[0]));
				} catch (IOException exception) {
					failure(exception.getMessage());
				}
			}
		});
		remove.addActionListener(event -> {
			Package selected = list.getSelectedValue();
			if (selected != null && JOptionPane.showConfirmDialog(dialog,
					text("UserTools.RemoveConfirm"), text("UserTools.Title"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				try {
					library.remove(selected.id());
					list.setListData(library.packages().toArray(new Package[0]));
				} catch (IOException exception) {
					failure(exception.getMessage());
				}
			}
		});
		document.addActionListener(event -> {
			dialog.dispose();
			new ToolManagerDialogD(app).setVisible(true);
		});
		close.addActionListener(event -> dialog.dispose());
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(close);
		app.setComponentOrientation(dialog);
		dialog.pack();
		dialog.setLocationRelativeTo(app.getMainComponent());
		dialog.setVisible(true);
	}

	void populateManagerPins(JPanel panel, Package selected) {
		panel.removeAll();
		Package tool = selected == null ? null : library.packageById(selected.id());
		if (tool != null) {
			List<PinnedCommand> ordered = library.pinnedCommands();
			List<String> commands = new ArrayList<>();
			for (PinnedCommand pin : ordered) {
				if (pin.tool().id().equals(tool.id())) {
					commands.add(pin.command());
				}
			}
			for (String command : tool.commands()) {
				if (!tool.isPinned(command)) {
					commands.add(command);
				}
			}
			for (String command : commands) {
				int position = pinnedPosition(ordered, tool, command);
				JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
				row.putClientProperty("geocedg.userTool.command", command);
				row.putClientProperty("geocedg.userTool.order", position);
				String label = position < 0 ? command : (position + 1) + ". " + command;
				JCheckBox pin = new JCheckBox(label, tool.isPinned(command));
				pin.setToolTipText(text("UserTools.Pin"));
				pin.getAccessibleContext().setAccessibleDescription(pin.getToolTipText());
				row.add(pin);
				String groupName = tool.pinGroup(command);
				JButton group = new JButton(groupName.isEmpty()
						? text("UserTools.Ungrouped") : groupName);
				group.putClientProperty("geocedg.userTool.group", groupName);
				group.setEnabled(position >= 0);
				group.setToolTipText(text("UserTools.Group"));
				group.addActionListener(action -> editGroup(tool, command, panel));
				JButton up = new JButton(text("UserTools.MoveUp"));
				JButton down = new JButton(text("UserTools.MoveDown"));
				JButton icon = new JButton(text("UserTools.Icon"));
				up.setEnabled(position > 0);
				down.setEnabled(position >= 0 && position + 1 < ordered.size());
				icon.setEnabled(position >= 0);
				icon.setToolTipText(text("UserTools.IconHelp"));
				icon.getAccessibleContext().setAccessibleName(text("UserTools.Icon"));
				icon.getAccessibleContext().setAccessibleDescription(icon.getToolTipText());
				up.addActionListener(action -> move(tool, command, -1, panel));
				down.addActionListener(action -> move(tool, command, 1, panel));
				icon.addActionListener(action -> choosePinIcon(tool, command, panel, false));
				pin.addActionListener(action -> {
					if (pin.isSelected()) {
						choosePinIcon(tool, command, panel, true);
					} else {
						try {
							library.pin(tool.id(), command, false);
						} catch (IOException exception) {
							failure(exception.getMessage());
						}
						populateManagerPins(panel, tool);
					}
				});
				row.add(group);
				row.add(up);
				row.add(down);
				row.add(icon);
				panel.add(row);
			}
		}
		panel.revalidate();
		panel.repaint();
	}

	private void choosePinIcon(Package tool, String command, JPanel panel,
			boolean pinWithoutIconOnCancel) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(text("UserTools.Icon"));
		chooser.setFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
		chooser.setAcceptAllFileFilterUsed(false);
		try {
			if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
				Path file = chooser.getSelectedFile().toPath();
				if (Files.size(file) > GeoCeDGUserToolLibrary.MAX_ICON_BYTES) {
					throw new IOException("UserTools.Limit");
				}
				if (pinWithoutIconOnCancel) {
					library.pin(tool.id(), command, file.getFileName().toString(),
							Files.readAllBytes(file));
				} else {
					library.setPinIcon(tool.id(), command, file.getFileName().toString(),
							Files.readAllBytes(file));
				}
			} else if (pinWithoutIconOnCancel) {
				library.pin(tool.id(), command, true);
			}
		} catch (IOException exception) {
			failure(exception.getMessage());
		}
		populateManagerPins(panel, tool);
	}

	private static int pinnedPosition(List<PinnedCommand> ordered, Package tool,
			String command) {
		for (int i = 0; i < ordered.size(); i++) {
			PinnedCommand pin = ordered.get(i);
			if (pin.tool().id().equals(tool.id()) && pin.command().equals(command)) {
				return i;
			}
		}
		return -1;
	}

	private void editGroup(Package tool, String command, JPanel panel) {
		Object value = JOptionPane.showInputDialog(app.getMainComponent(),
				text("UserTools.Group"), text("UserTools.Title"), JOptionPane.PLAIN_MESSAGE,
				null, null, tool.pinGroup(command));
		if (value != null) {
			try {
				library.setPinGroup(tool.id(), command, value.toString());
			} catch (IOException exception) {
				failure(exception.getMessage());
			}
		}
		populateManagerPins(panel, tool);
	}

	private void move(Package tool, String command, int delta, JPanel panel) {
		try {
			library.movePinned(tool.id(), command, delta);
		} catch (IOException exception) {
			failure(exception.getMessage());
		}
		populateManagerPins(panel, tool);
	}

	private String text(String key) {
		try {
			return registry.text(key);
		} catch (IllegalArgumentException missingProfileText) {
			return app.getLocalization().getMenu(key);
		}
	}

	private boolean refreshLibrary() {
		if (library == null) {
			return false;
		}
		try {
			library.refresh();
			return true;
		} catch (IOException exception) {
			return false;
		}
	}

	private String explain(String detail) {
		if (detail == null) {
			return text("UserTools.Unavailable");
		}
		int separator = detail.indexOf(':');
		return separator < 0 ? text(detail)
				: text(detail.substring(0, separator)) + detail.substring(separator);
	}

	private void failure(String reason) {
		JOptionPane.showMessageDialog(app.getMainComponent(),
				text("UserTools.Failure") + explain(reason), text("UserTools.Title"),
				JOptionPane.WARNING_MESSAGE);
	}
}
