/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.MacroKernel;
import org.geogebra.common.kernel.MacroManager;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.CommandsConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.io.MyXMLioD;
import org.geogebra.desktop.main.AppD;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Explicit application-owned GGT packages; document macros never install themselves. */
final class GeoCeDGUserToolLibrary {

	static final int MAX_BYTES = 8 * 1024 * 1024;
	static final int MAX_ICON_BYTES = 256 * 1024;
	static final int MAX_ICON_EDGE = 1024;
	static final int TOOLBAR_ICON_SIZE = 64;
	private static final int STORE_VERSION = 3;
	private static final int DEFINITION_DIGEST_VERSION = 1;
	private static final int ICON_NORMALIZATION_VERSION = 1;
	private static final int MAX_PIN_ORDER = 4095;
	private static final byte[] PNG_SIGNATURE = new byte[] {
			(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
	private static final Pattern TOOLBAR_ATTRIBUTE = Pattern.compile(
			" showInToolBar=\"(?:true|false)\"");
	private static final Pattern CALL = Pattern.compile("([\\p{L}_][\\p{L}\\p{N}_]*)\\s*[\\[(]");
	private static final Set<Integer> SAFE_TABLES = Set.of(CommandsConstants.TABLE_GEOMETRY,
			CommandsConstants.TABLE_ALGEBRA, CommandsConstants.TABLE_TEXT,
			CommandsConstants.TABLE_LOGICAL, CommandsConstants.TABLE_FUNCTION,
			CommandsConstants.TABLE_CONIC, CommandsConstants.TABLE_LIST,
			CommandsConstants.TABLE_VECTOR, CommandsConstants.TABLE_TRANSFORMATION);
	private static final Set<String> PLANAR_TYPES = Set.of("point", "numeric", "angle",
			"line", "segment", "ray", "vector", "conic", "conicpart", "polygon", "polyline",
			"list", "function", "functionnvar", "curvecartesian", "text", "boolean",
			"implicitpoly");
	private final AppD app;
	private final Path storage;
	private final Map<String, Package> packages = new LinkedHashMap<>();
	private final Map<Macro, String> activated = new IdentityHashMap<>();
	private final List<Runnable> listeners = new ArrayList<>();

	/** Original exchange bytes plus application-only pin preferences. */
	static final class Package {
		private final String id;
		private final String name;
		private final byte[] bytes;
		private final String xml;
		private final List<String> commands;
		private final Map<String, String> definitionDigests = new LinkedHashMap<>();
		private final Map<String, PinLayout> pinned = new LinkedHashMap<>();

		Package(String name, byte[] bytes, String xml, List<String> commands) {
			this.id = digest(bytes);
			this.name = name;
			this.bytes = bytes.clone();
			this.xml = xml;
			this.commands = List.copyOf(commands);
		}

		String id() {
			return id;
		}

		String name() {
			return name;
		}

		List<String> commands() {
			return commands;
		}

		String definitionDigest(String command) {
			return definitionDigests.get(command);
		}

		boolean isPinned(String command) {
			return pinned.containsKey(command);
		}

		String pinGroup(String command) {
			PinLayout layout = pinned.get(command);
			return layout == null ? "" : layout.group;
		}

		int pinOrder(String command) {
			PinLayout layout = pinned.get(command);
			return layout == null ? Integer.MAX_VALUE : layout.order;
		}

		@Override
		public String toString() {
			return name + " \u2014 " + String.join(", ", commands);
		}
	}

	/** Application-only placement of one installed command in the product toolbar. */
	static final class PinnedCommand {
		private final Package tool;
		private final String command;
		private final PinLayout layout;

		PinnedCommand(Package tool, String command, PinLayout layout) {
			this.tool = tool;
			this.command = command;
			this.layout = layout;
		}

		Package tool() {
			return tool;
		}

		String command() {
			return command;
		}

		String group() {
			return layout.group;
		}

		int order() {
			return layout.order;
		}

		PinIcon icon() {
			return layout.icon;
		}
	}

	/** Validated application-preference PNG and its deterministic toolbar derivative. */
	static final class PinIcon {
		private final String sourceName;
		private final String sourceDigest;
		private final int sourceWidth;
		private final int sourceHeight;
		private final byte[] sourceBytes;
		private final byte[] toolbarBytes;

		private PinIcon(String sourceName, String sourceDigest, int sourceWidth,
				int sourceHeight, byte[] sourceBytes, byte[] toolbarBytes) {
			this.sourceName = sourceName;
			this.sourceDigest = sourceDigest;
			this.sourceWidth = sourceWidth;
			this.sourceHeight = sourceHeight;
			this.sourceBytes = sourceBytes.clone();
			this.toolbarBytes = toolbarBytes.clone();
		}

		String sourceName() {
			return sourceName;
		}

		String sourceDigest() {
			return sourceDigest;
		}

		int sourceWidth() {
			return sourceWidth;
		}

		int sourceHeight() {
			return sourceHeight;
		}

		byte[] sourceBytes() {
			return sourceBytes.clone();
		}

		byte[] toolbarBytes() {
			return toolbarBytes.clone();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PinIcon)) {
				return false;
			}
			PinIcon icon = (PinIcon) other;
			return sourceWidth == icon.sourceWidth && sourceHeight == icon.sourceHeight
					&& sourceName.equals(icon.sourceName)
					&& sourceDigest.equals(icon.sourceDigest)
					&& java.util.Arrays.equals(sourceBytes, icon.sourceBytes)
					&& java.util.Arrays.equals(toolbarBytes, icon.toolbarBytes);
		}

		@Override
		public int hashCode() {
			return sourceDigest.hashCode();
		}
	}

	private static final class PinLayout {
		private final String group;
		private final int order;
		private final PinIcon icon;

		PinLayout(String group, int order) {
			this(group, order, null);
		}

		PinLayout(String group, int order, PinIcon icon) {
			this.group = group;
			this.order = order;
			this.icon = icon;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof PinLayout && order == ((PinLayout) other).order
					&& group.equals(((PinLayout) other).group)
					&& java.util.Objects.equals(icon, ((PinLayout) other).icon);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(group, order, icon);
		}
	}

	GeoCeDGUserToolLibrary(AppD app, Path storage) throws IOException {
		if (!(app.getConfig() instanceof AppConfigGeoCeDG)) {
			throw new IllegalArgumentException("GeoCeDG profile required");
		}
		this.app = app;
		this.storage = storage.toAbsolutePath();
		refresh();
	}

	List<Package> packages() {
		return List.copyOf(packages.values());
	}

	Package packageById(String id) {
		return packages.get(id);
	}

	List<PinnedCommand> pinnedCommands() {
		List<PinnedCommand> result = new ArrayList<>();
		for (Package tool : packages.values()) {
			for (String command : tool.commands) {
				PinLayout layout = tool.pinned.get(command);
				if (layout != null) {
					result.add(new PinnedCommand(tool, command, layout));
				}
			}
		}
		result.sort((first, second) -> {
			int byOrder = Integer.compare(first.order(), second.order());
			if (byOrder != 0) {
				return byOrder;
			}
			int byPackage = first.tool().id().compareTo(second.tool().id());
			return byPackage != 0 ? byPackage
					: first.command().compareToIgnoreCase(second.command());
		});
		return List.copyOf(result);
	}

	void addListener(Runnable listener) {
		listeners.add(listener);
	}

	Package install(String fileName, byte[] bytes) throws IOException {
		return editStore(() -> installCurrent(fileName, bytes));
	}

	private Package installCurrent(String fileName, byte[] bytes) throws IOException {
		if (packages.size() >= 64) {
			throw new IOException("UserTools.Limit");
		}
		Package proposed = inspect(fileName, bytes);
		verifyDefinitionDigests(proposed, null);
		Set<String> existing = installedCommands();
		for (String command : proposed.commands) {
			if (existing.contains(key(command))) {
				throw new IOException("UserTools.CommandConflict: " + command);
			}
		}
		// An explicit install may adopt a complete equivalent document definition, but
		// a partial or different same-name definition is never renamed or replaced.
		registeredCount(proposed, false);
		Map<String, Package> next = new LinkedHashMap<>(packages);
		next.put(proposed.id, proposed);
		persist(next);
		packages.put(proposed.id, proposed);
		return proposed;
	}

	void remove(String id) throws IOException {
		editStore(() -> {
			requirePackage(id);
			Map<String, Package> next = new LinkedHashMap<>(packages);
			next.remove(id);
			persist(next);
			packages.remove(id);
			activated.entrySet().removeIf(entry -> id.equals(entry.getValue()));
			// Existing document definitions/AlgoMacro outputs remain document-owned.
			return null;
		});
	}

	void pin(String id, String command, boolean pinned) throws IOException {
		editStore(() -> {
			pinCurrent(id, command, pinned, null, false);
			return null;
		});
	}

	void pin(String id, String command, String iconName, byte[] iconBytes)
			throws IOException {
		PinIcon icon = createPinIcon(iconName, iconBytes);
		editStore(() -> {
			pinCurrent(id, command, true, icon, true);
			return null;
		});
	}

	void setPinIcon(String id, String command, String iconName, byte[] iconBytes)
			throws IOException {
		PinIcon icon = iconBytes == null ? null : createPinIcon(iconName, iconBytes);
		editStore(() -> {
			Package tool = requirePinned(id, command);
			PinLayout previous = tool.pinned.get(command);
			tool.pinned.put(command, new PinLayout(previous.group, previous.order, icon));
			try {
				persist(packages);
			} catch (IOException exception) {
				tool.pinned.put(command, previous);
				throw exception;
			}
			return null;
		});
	}

	private void pinCurrent(String id, String command, boolean pinned, PinIcon icon,
			boolean replaceIcon) throws IOException {
		Package tool = requirePackage(id);
		if (!tool.commands.contains(command)) {
			throw new IOException("UserTools.Unknown");
		}
		PinLayout previous = tool.pinned.get(command);
		if (pinned) {
			if (previous == null) {
				tool.pinned.put(command, new PinLayout("", nextPinOrder(), icon));
			} else if (replaceIcon) {
				tool.pinned.put(command,
						new PinLayout(previous.group, previous.order, icon));
			}
		} else {
			tool.pinned.remove(command);
		}
		try {
			persist(packages);
		} catch (IOException exception) {
			if (previous != null) {
				tool.pinned.put(command, previous);
			} else {
				tool.pinned.remove(command);
			}
			throw exception;
		}
	}

	void setPinGroup(String id, String command, String group) throws IOException {
		editStore(() -> {
			Package tool = requirePinned(id, command);
			String validated = validateGroup(group);
			Map<Package, Map<String, PinLayout>> previous = snapshotPinLayouts();
			PinLayout layout = tool.pinned.get(command);
			tool.pinned.put(command, new PinLayout(validated, layout.order, layout.icon));
			normalizeGroupBlocks();
			try {
				persist(packages);
			} catch (IOException exception) {
				restorePinLayouts(previous);
				throw exception;
			}
			return null;
		});
	}

	void movePinned(String id, String command, int delta) throws IOException {
		if (delta != -1 && delta != 1) {
			throw new IllegalArgumentException("delta");
		}
		editStore(() -> {
			Package tool = requirePinned(id, command);
			List<PinnedCommand> ordered = new ArrayList<>(pinnedCommands());
			int current = -1;
			for (int i = 0; i < ordered.size(); i++) {
				PinnedCommand pin = ordered.get(i);
				if (pin.tool() == tool && pin.command().equals(command)) {
					current = i;
					break;
				}
			}
			int target = current + delta;
			if (current < 0 || target < 0 || target >= ordered.size()) {
				return null;
			}
			Map<Package, Map<String, PinLayout>> previous = snapshotPinLayouts();
			PinnedCommand own = ordered.get(current);
			PinnedCommand other = ordered.get(target);
			if (!own.group().isEmpty() && own.group().equals(other.group())) {
				Collections.swap(ordered, current, target);
			} else {
				List<List<PinnedCommand>> units = visualUnits(ordered);
				int unit = unitContaining(units, tool, command);
				int targetUnit = unit + delta;
				if (unit < 0 || targetUnit < 0 || targetUnit >= units.size()) {
					return null;
				}
				Collections.swap(units, unit, targetUnit);
				ordered = units.stream().flatMap(List::stream).toList();
			}
			applyPinOrder(ordered);
			try {
				persist(packages);
			} catch (IOException exception) {
				restorePinLayouts(previous);
				throw exception;
			}
			return null;
		});
	}

	private void normalizeGroupBlocks() {
		List<PinnedCommand> ordered = pinnedCommands();
		List<PinnedCommand> normalized = new ArrayList<>();
		Set<String> emitted = new HashSet<>();
		for (PinnedCommand pin : ordered) {
			if (pin.group().isEmpty()) {
				normalized.add(pin);
			} else if (emitted.add(pin.group())) {
				for (PinnedCommand candidate : ordered) {
					if (pin.group().equals(candidate.group())) {
						normalized.add(candidate);
					}
				}
			}
		}
		applyPinOrder(normalized);
	}

	private static List<List<PinnedCommand>> visualUnits(List<PinnedCommand> ordered) {
		List<List<PinnedCommand>> units = new ArrayList<>();
		for (PinnedCommand pin : ordered) {
			if (pin.group().isEmpty() || units.isEmpty()
					|| !pin.group().equals(units.get(units.size() - 1).get(0).group())) {
				units.add(new ArrayList<>());
			}
			units.get(units.size() - 1).add(pin);
		}
		return units;
	}

	private static int unitContaining(List<List<PinnedCommand>> units, Package tool,
			String command) {
		for (int i = 0; i < units.size(); i++) {
			for (PinnedCommand pin : units.get(i)) {
				if (pin.tool() == tool && pin.command().equals(command)) {
					return i;
				}
			}
		}
		return -1;
	}

	private static void applyPinOrder(List<PinnedCommand> ordered) {
		for (int i = 0; i < ordered.size(); i++) {
			PinnedCommand pin = ordered.get(i);
			pin.tool().pinned.put(pin.command(), new PinLayout(pin.group(), i, pin.icon()));
		}
	}

	private Map<Package, Map<String, PinLayout>> snapshotPinLayouts() {
		Map<Package, Map<String, PinLayout>> snapshot = new IdentityHashMap<>();
		for (Package tool : packages.values()) {
			snapshot.put(tool, new LinkedHashMap<>(tool.pinned));
		}
		return snapshot;
	}

	private static void restorePinLayouts(
			Map<Package, Map<String, PinLayout>> snapshot) {
		for (Map.Entry<Package, Map<String, PinLayout>> entry : snapshot.entrySet()) {
			entry.getKey().pinned.clear();
			entry.getKey().pinned.putAll(entry.getValue());
		}
	}

	private Package requirePinned(String id, String command) throws IOException {
		Package tool = requirePackage(id);
		if (!tool.commands.contains(command) || !tool.pinned.containsKey(command)) {
			throw new IOException("UserTools.Unknown");
		}
		return tool;
	}

	private int nextPinOrder() throws IOException {
		int maximum = -1;
		Set<Integer> occupied = new HashSet<>();
		for (Package tool : packages.values()) {
			for (PinLayout layout : tool.pinned.values()) {
				maximum = Math.max(maximum, layout.order);
				occupied.add(layout.order);
			}
		}
		if (maximum < MAX_PIN_ORDER) {
			return maximum + 1;
		}
		for (int candidate = MAX_PIN_ORDER - 1; candidate >= 0; candidate--) {
			if (!occupied.contains(candidate)) {
				return candidate;
			}
		}
		throw new IOException("UserTools.Limit");
	}

	private static String validateGroup(String group) throws IOException {
		String candidate = group == null ? "" : group.strip();
		if (candidate.length() > 48 || !candidate.matches("[\\p{L}\\p{N} ._-]*")) {
			throw new IOException("UserTools.InvalidLayout");
		}
		return candidate;
	}

	private <T> T editStore(StoreAction<T> action) throws IOException {
		Files.createDirectories(storage.getParent());
		Path lockPath = storage.resolveSibling(storage.getFileName() + ".lock");
		final T result;
		try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE); FileLock lock = channel.tryLock()) {
			if (lock == null) {
				throw new IOException("UserTools.LibraryBusy");
			}
			// Another window/process may have installed, removed or pinned a tool.
			// Revalidate the whole store before changing it, never merge stale snapshots.
			refresh();
			result = action.execute();
		} catch (OverlappingFileLockException exception) {
			throw new IOException("UserTools.LibraryBusy", exception);
		}
		notifyListeners();
		return result;
	}

	@FunctionalInterface
	private interface StoreAction<T> {
		T execute() throws IOException;
	}

	/** Register only after explicit activation; no source is inferred from screen geometry. */
	Macro activate(String id, String command) throws IOException {
		refresh();
		Package tool = requirePackage(id);
		if (!tool.commands.contains(command)) {
			throw new IOException("UserTools.Unknown");
		}
		// Reinspect current policy; a file-load preservation flag is never creation authority.
		inspect(tool.name, tool.bytes);
		verifyDefinitionDigests(tool, tool.definitionDigests);
		if (registeredCount(tool, true) == tool.commands.size()) {
			return app.getKernel().getMacro(command);
		}
		registerWithHost(tool);
		return app.getKernel().getMacro(command);
	}

	private int registeredCount(Package tool, boolean adoptEquivalent) throws IOException {
		pruneActivated();
		int count = 0;
		Map<Macro, String> equivalent = new IdentityHashMap<>();
		for (String name : tool.commands) {
			Macro current = app.getKernel().getMacro(name);
			if (current != null) {
				if (current.getKernel() != app.getKernel()
						|| !tool.definitionDigest(name).equals(definitionDigest(current))) {
					throw new IOException("UserTools.DefinitionMismatch: " + name);
				}
				String owner = activated.get(current);
				if (owner != null && !tool.id.equals(owner)) {
					throw new IOException("UserTools.DocumentConflict: " + name);
				}
				equivalent.put(current, tool.id);
				count++;
			}
		}
		// Never let the host parser rename collisions in a partially present package.
		if (count != 0 && count != tool.commands.size()) {
			throw new IOException("UserTools.DocumentConflict");
		}
		if (adoptEquivalent && count == tool.commands.size()) {
			// The live objects remain document-owned reconstruction authority. This map only
			// records which installed package may present and invoke those exact definitions.
			activated.putAll(equivalent);
		}
		return count;
	}

	private void pruneActivated() {
		activated.entrySet().removeIf(entry -> {
			Macro macro = entry.getKey();
			return macro.getKernel() != app.getKernel()
					|| app.getKernel().getMacro(macro.getCommandName()) != macro
					|| !packages.containsKey(entry.getValue());
		});
	}

	private void registerWithHost(Package tool) throws IOException {
		Set<Macro> previous = Collections.newSetFromMap(new IdentityHashMap<>());
		previous.addAll(registeredMacros());
		boolean complete = false;
		try {
			// The live host owns explicitly activated definitions; validation macros never do.
			// GGT-only processing neither clears nor loads the active document construction.
			app.getXMLio().processXMLString(tool.xml, false, true, false);
			List<Macro> current = registeredMacros();
			if (current.size() != previous.size() + tool.commands.size()
					|| !current.containsAll(previous)) {
				throw new IOException("UserTools.InvalidArchive");
			}
			for (String name : tool.commands) {
				Macro macro = app.getKernel().getMacro(name);
				if (macro == null || previous.contains(macro)
						|| macro.getKernel() != app.getKernel()
						|| !tool.definitionDigest(name).equals(definitionDigest(macro))) {
					throw new IOException("UserTools.InvalidArchive");
				}
			}
			for (String name : tool.commands) {
				Macro macro = app.getKernel().getMacro(name);
				macro.setShowInToolBar(false);
				activated.put(macro, tool.id);
			}
			app.updateCommandDictionary();
			complete = true;
		} catch (Exception exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		} finally {
			if (!complete) {
				for (Macro macro : registeredMacros()) {
					if (!previous.contains(macro)) {
						app.getKernel().removeMacro(macro);
						activated.remove(macro);
					}
				}
				app.updateCommandDictionary();
			}
		}
	}

	private List<Macro> registeredMacros() {
		List<Macro> current = app.getKernel().getAllMacros();
		return current == null ? List.of() : List.copyOf(current);
	}

	void select(String id, String command) throws IOException {
		Macro macro = activate(id, command);
		app.setMode(EuclidianConstants.MACRO_MODE_ID_OFFSET
				+ app.getKernel().getMacroID(macro));
	}

	String unavailableReason(Package tool) {
		try {
			inspect(tool.name, tool.bytes);
			registeredCount(tool, false);
			return null;
		} catch (IOException exception) {
			return exception.getMessage().split(":", 2)[0];
		}
	}

	private Package requirePackage(String id) throws IOException {
		Package tool = packages.get(id);
		if (tool == null) {
			throw new IOException("UserTools.Unknown");
		}
		return tool;
	}

	private Set<String> installedCommands() {
		Set<String> result = new HashSet<>();
		for (Package tool : packages.values()) {
			for (String command : tool.commands) {
				result.add(key(command));
			}
		}
		return result;
	}

	private Package inspect(String name, byte[] bytes) throws IOException {
		if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".ggt")
				|| bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
			throw new IOException("UserTools.InvalidArchive");
		}
		String xml = readMacroXml(bytes);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			Element root = factory.newDocumentBuilder().parse(new ByteArrayInputStream(
					xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
			if (!"geogebra".equals(root.getTagName())) {
				throw new IOException("UserTools.InvalidArchive");
			}
			List<String> names = new ArrayList<>();
			Set<String> unique = new HashSet<>();
			for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node instanceof Element) {
					Element macro = (Element) node;
					String command = macro.getAttribute("cmdName");
					if (!"macro".equals(macro.getTagName())
							|| !command.matches("[\\p{L}][\\p{L}\\p{N}_]{0,63}")
							|| !unique.add(key(command)) || nativeCommand(command) != null) {
						throw new IOException("UserTools.CommandConflict: " + command);
					}
					names.add(command);
				}
			}
			if (names.isEmpty() || names.size() > 64) {
				throw new IOException("UserTools.InvalidArchive");
			}
			NodeList all = root.getElementsByTagName("*");
			for (int i = 0; i < all.getLength(); i++) {
				Element element = (Element) all.item(i);
				validateElement(element, unique);
			}
			return new Package(name, bytes, xml, names);
		} catch (IOException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		}
	}

	private void validateElement(Element element, Set<String> macros) throws IOException {
		String tag = element.getTagName();
		if ("element".equals(tag) && !PLANAR_TYPES.contains(
				element.getAttribute("type").toLowerCase(Locale.ROOT))) {
			throw new IOException("UserTools.UnsupportedBody");
		}
		// Ordinary identity records may accompany native macro XML;
		// spatial/semantic models may not.
		if (element.getParentNode() instanceof Element
				&& "geocedgSpatial".equals(((Element) element.getParentNode()).getTagName())
				&& !"geo".equals(tag)) {
			throw new IOException("UserTools.UnsupportedBody");
		}
		if ("ggbscript".equals(tag) || "javascript".equals(tag)) {
			for (int i = 0; i < element.getAttributes().getLength(); i++) {
				if (!element.getAttributes().item(i).getNodeValue().isBlank()) {
					throw new IOException("UserTools.UnsupportedBody");
				}
			}
			if (!element.getTextContent().isBlank()) {
				throw new IOException("UserTools.UnsupportedBody");
			}
		}
		if (tag.startsWith("cedg") || tag.startsWith("spatial") || tag.startsWith("locusV2")) {
			throw new IOException("UserTools.UnsupportedBody");
		}
		if ("command".equals(tag)) {
			validateCommand(element.getAttribute("name"), macros, true);
		}
		// Nested commands in expression/input attributes must not bypass the command-node check.
		if ("expression".equals(tag) || "input".equals(tag)) {
			for (int i = 0; i < element.getAttributes().getLength(); i++) {
				Matcher calls = CALL.matcher(element.getAttributes().item(i).getNodeValue());
				while (calls.find()) {
					validateCommand(calls.group(1), macros, false);
				}
			}
		}
	}

	private void validateCommand(String name, Set<String> macros, boolean required)
			throws IOException {
		if (macros.contains(key(name))) {
			return;
		}
		String internal = nativeCommand(name);
		if (internal == null) {
			if (required) {
				throw new IOException("UserTools.UnsupportedBody: " + name);
			}
			return;
		}
		Commands command = Commands.valueOf(internal);
		if (RuntimeFeatureService.isDedicatedLocusV2Command(command)) {
			boolean enabled = ((AppConfigGeoCeDG) app.getConfig()).getRuntimeFeatureService()
					.isLocusV2CreationEnabled();
			throw new IOException((enabled ? "UserTools.UnsupportedBody: "
					: "LocusV2.FeatureDisabled: ") + name);
		}
		if (!SAFE_TABLES.contains(command.getTable())) {
			throw new IOException("UserTools.UnsupportedBody: " + name);
		}
	}

	private String nativeCommand(String name) {
		String internal = Commands.lookupInternal(name);
		if (internal == null) {
			internal = Commands.lookupInternal(app.getInternalCommand(name));
		}
		return internal;
	}

	private List<Macro> validateWithHost(Package tool) throws IOException {
		ValidationKernel kernel = new ValidationKernel(app.getKernel());
		try {
			new MyXMLioD(kernel, kernel.getConstruction()).processXMLString(tool.xml,
					false, true, false);
			List<Macro> result = kernel.getAllMacros();
			if (result.size() != tool.commands.size()) {
				throw new IOException("UserTools.InvalidArchive");
			}
			for (int i = 0; i < result.size(); i++) {
				Macro macro = result.get(i);
				if (!tool.commands.get(i).equals(macro.getCommandName())
						|| macro.getMacroInput() == null || macro.getMacroInput().length == 0
						|| macro.getMacroOutput() == null || macro.getMacroOutput().length == 0) {
					throw new IOException("UserTools.InvalidArchive");
				}
				for (GeoElement geo : macro.getMacroInput()[0].getConstruction()
						.getGeoSetConstructionOrder()) {
					if (geo.isGeoElement3D()) {
						throw new IOException("UserTools.UnsupportedBody");
					}
				}
			}
			return result;
		} catch (Exception exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		}
	}

	private void verifyDefinitionDigests(Package tool, Map<String, String> expected)
			throws IOException {
		Map<String, String> calculated = new LinkedHashMap<>();
		for (Macro macro : validateWithHost(tool)) {
			calculated.put(macro.getCommandName(), definitionDigest(macro));
		}
		Map<String, String> prior = expected == null ? Map.of()
				: new LinkedHashMap<>(expected);
		if (!prior.isEmpty() && !prior.equals(calculated)) {
			throw new IOException("UserTools.InvalidArchive");
		}
		tool.definitionDigests.clear();
		tool.definitionDigests.putAll(calculated);
	}

	private static String definitionDigest(Macro macro) throws IOException {
		XMLStringBuilder builder = new XMLStringBuilder();
		macro.getXML(builder);
		String xml = builder.toString();
		Matcher toolbar = TOOLBAR_ATTRIBUTE.matcher(xml);
		if (!toolbar.find()) {
			throw new IOException("UserTools.InvalidArchive");
		}
		// Toolbar exposure belongs to the application profile. Every other macro field,
		// including command, metadata, inputs, outputs and normalized construction XML,
		// remains definition-bearing and therefore participates in equivalence.
		String normalized = toolbar.replaceFirst(" showInToolBar=\"application\"");
		return digest(("GeoCeDG macro definition\n"
				+ DEFINITION_DIGEST_VERSION + "\n" + normalized)
				.getBytes(StandardCharsets.UTF_8));
	}

	private static PinIcon createPinIcon(String fileName, byte[] bytes) throws IOException {
		return createPinIconForSource(basePngName(fileName), bytes);
	}

	private static PinIcon createPinIconForSource(String sourceName, byte[] bytes)
			throws IOException {
		if (bytes == null || bytes.length == 0 || bytes.length < PNG_SIGNATURE.length) {
			throw new IOException("UserTools.InvalidIcon");
		}
		if (bytes.length > MAX_ICON_BYTES) {
			throw new IOException("UserTools.Limit");
		}
		for (int i = 0; i < PNG_SIGNATURE.length; i++) {
			if (bytes[i] != PNG_SIGNATURE[i]) {
				throw new IOException("UserTools.InvalidIcon");
			}
		}
		BufferedImage source;
		int width;
		int height;
		try (ImageInputStream input = ImageIO.createImageInputStream(
				new ByteArrayInputStream(bytes))) {
			if (input == null) {
				throw new IOException("UserTools.InvalidIcon");
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IOException("UserTools.InvalidIcon");
			}
			ImageReader reader = readers.next();
			try {
				if (!"png".equalsIgnoreCase(reader.getFormatName())) {
					throw new IOException("UserTools.InvalidIcon");
				}
				reader.setInput(input, true, true);
				width = reader.getWidth(0);
				height = reader.getHeight(0);
				if (width < 1 || height < 1 || width > MAX_ICON_EDGE
						|| height > MAX_ICON_EDGE
						|| (long) width * height > (long) MAX_ICON_EDGE * MAX_ICON_EDGE) {
					throw new IOException("UserTools.Limit");
				}
				source = reader.read(0);
			} finally {
				reader.dispose();
			}
		} catch (IOException exception) {
			if (exception.getMessage() != null
					&& exception.getMessage().startsWith("UserTools.")) {
				throw exception;
			}
			throw new IOException("UserTools.InvalidIcon", exception);
		}
		if (source == null || source.getWidth() != width || source.getHeight() != height) {
			throw new IOException("UserTools.InvalidIcon");
		}
		BufferedImage normalized = new BufferedImage(TOOLBAR_ICON_SIZE, TOOLBAR_ICON_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		double scale = Math.min((double) TOOLBAR_ICON_SIZE / width,
				(double) TOOLBAR_ICON_SIZE / height);
		int scaledWidth = Math.max(1, (int) Math.round(width * scale));
		int scaledHeight = Math.max(1, (int) Math.round(height * scale));
		Graphics2D graphics = normalized.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			graphics.drawImage(source, (TOOLBAR_ICON_SIZE - scaledWidth) / 2,
					(TOOLBAR_ICON_SIZE - scaledHeight) / 2, scaledWidth, scaledHeight, null);
		} finally {
			graphics.dispose();
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (!ImageIO.write(normalized, "png", output)) {
			throw new IOException("UserTools.InvalidIcon");
		}
		return new PinIcon(sourceName, digest(bytes), width, height, bytes,
				output.toByteArray());
	}

	private static String basePngName(String fileName) throws IOException {
		if (fileName == null) {
			throw new IOException("UserTools.InvalidIcon");
		}
		String portable = fileName.replace('\\', '/');
		String name = portable.substring(portable.lastIndexOf('/') + 1).strip();
		if (name.isEmpty() || name.length() > 128
				|| !name.toLowerCase(Locale.ROOT).endsWith(".png")
				|| !name.matches("[\\p{L}\\p{N} ._()-]+")) {
			throw new IOException("UserTools.InvalidIcon");
		}
		return name;
	}

	private static String readMacroXml(byte[] bytes) throws IOException {
		Set<String> entries = new HashSet<>();
		String xml = null;
		int total = 0;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName();
				if (!entries.add(name) || entries.size() > 128 || name.contains("..")
						|| name.startsWith("/") || name.contains("\\") || name.contains(":")) {
					throw new IOException("UserTools.InvalidArchive");
				}
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int read;
				while ((read = zip.read(buffer)) != -1) {
					total += read;
					if (total > MAX_BYTES) {
						throw new IOException("UserTools.Limit");
					}
					output.write(buffer, 0, read);
				}
				if ("geogebra_macro.xml".equals(name)) {
					xml = output.toString(StandardCharsets.UTF_8);
				} else if (!name.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpg|jpeg|gif|svg)")) {
					throw new IOException("UserTools.InvalidArchive");
				}
			}
		}
		if (xml == null) {
			throw new IOException("UserTools.InvalidArchive");
		}
		return xml;
	}

	void refresh() throws IOException {
		Map<String, Package> validated = readStored();
		boolean changed = !packages.keySet().equals(validated.keySet());
		for (Package fresh : validated.values()) {
			Package existing = packages.get(fresh.id);
			changed |= existing == null || !existing.name.equals(fresh.name)
					|| !existing.pinned.equals(fresh.pinned);
		}
		if (!changed) {
			return;
		}
		// Publish only after every package, hash and pin has validated successfully.
		// Keep existing presentation objects live where the original package is unchanged.
		Map<String, Package> next = new LinkedHashMap<>();
		for (Package fresh : validated.values()) {
			Package existing = packages.get(fresh.id);
			if (existing != null && existing.name.equals(fresh.name)) {
				existing.pinned.clear();
				existing.pinned.putAll(fresh.pinned);
				next.put(existing.id, existing);
			} else {
				next.put(fresh.id, fresh);
			}
		}
		packages.clear();
		packages.putAll(next);
	}

	private Map<String, Package> readStored() throws IOException {
		Map<String, Package> validated = new LinkedHashMap<>();
		if (!Files.exists(storage)) {
			return validated;
		}
		if (Files.size(storage) > 2L * MAX_BYTES) {
			throw new IOException("UserTools.Limit");
		}
		try {
			JSONObject root = new JSONObject(Files.readString(storage));
			int version = root.getInt("version");
			int expectedRootLength = version == STORE_VERSION ? 3 : 2;
			if (root.length() != expectedRootLength
					|| (version != 1 && version != 2 && version != STORE_VERSION)
					|| version == STORE_VERSION && root.getInt("definitionDigestVersion")
							!= DEFINITION_DIGEST_VERSION) {
				throw new IOException("UserTools.InvalidArchive");
			}
			JSONArray stored = root.getJSONArray("packages");
			if (stored.length() > 64) {
				throw new IOException("UserTools.Limit");
			}
			Set<String> names = new HashSet<>();
			Set<Integer> pinOrders = new HashSet<>();
			int migratedOrder = 0;
			for (int i = 0; i < stored.length(); i++) {
				JSONObject entry = stored.getJSONObject(i);
				if (entry.length() != (version == STORE_VERSION ? 5 : 4)) {
					throw new IOException("UserTools.InvalidArchive");
				}
				byte[] bytes = Base64.getDecoder().decode(entry.getString("ggt"));
				Package tool = inspect(entry.getString("name"), bytes);
				if (!tool.id.equals(entry.getString("sha256"))
						|| validated.containsKey(tool.id)) {
					throw new IOException("UserTools.InvalidArchive");
				}
				Map<String, String> storedDefinitions = version == STORE_VERSION
						? readDefinitionDigests(entry.getJSONArray("definitions"), tool)
						: null;
				verifyDefinitionDigests(tool, storedDefinitions);
				for (String command : tool.commands) {
					if (!names.add(key(command))) {
						throw new IOException("UserTools.CommandConflict: " + command);
					}
				}
				JSONArray pins = entry.getJSONArray("pinned");
				for (int p = 0; p < pins.length(); p++) {
					String pin;
					String group;
					int order;
					if (version == 1) {
						pin = pins.getString(p);
						group = "";
						order = migratedOrder++;
					} else {
						JSONObject layout = pins.getJSONObject(p);
						if (layout.length() != (version == STORE_VERSION ? 4 : 3)) {
							throw new IOException("UserTools.InvalidArchive");
						}
						pin = layout.getString("command");
						group = validateGroup(layout.getString("group"));
						order = layout.getInt("order");
					}
					if (!tool.commands.contains(pin) || tool.pinned.containsKey(pin)
							|| order < 0 || order > MAX_PIN_ORDER || !pinOrders.add(order)) {
						throw new IOException("UserTools.InvalidArchive");
					}
					PinIcon icon = version == STORE_VERSION && !pins.getJSONObject(p).isNull("icon")
							? readPinIcon(pins.getJSONObject(p).getJSONObject("icon")) : null;
					tool.pinned.put(pin, new PinLayout(group, order, icon));
				}
				validated.put(tool.id, tool);
			}
		} catch (JSONException | IllegalArgumentException exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		}
		return validated;
	}

	private static Map<String, String> readDefinitionDigests(JSONArray stored,
			Package tool) throws IOException, JSONException {
		if (stored.length() != tool.commands.size()) {
			throw new IOException("UserTools.InvalidArchive");
		}
		Map<String, String> definitions = new LinkedHashMap<>();
		for (int i = 0; i < stored.length(); i++) {
			JSONObject entry = stored.getJSONObject(i);
			String command = entry.getString("command");
			String sha256 = entry.getString("sha256");
			if (entry.length() != 2 || !tool.commands.contains(command)
					|| definitions.put(command, sha256) != null
					|| !sha256.matches("[0-9a-f]{64}")) {
				throw new IOException("UserTools.InvalidArchive");
			}
		}
		return definitions;
	}

	private static PinIcon readPinIcon(JSONObject stored) throws IOException, JSONException {
		if (stored.length() != 6
				|| stored.getInt("normalizationVersion") != ICON_NORMALIZATION_VERSION) {
			throw new IOException("UserTools.InvalidArchive");
		}
		byte[] source = Base64.getDecoder().decode(stored.getString("source"));
		PinIcon icon;
		try {
			icon = createPinIcon(stored.getString("sourceName"), source);
		} catch (IOException exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		}
		if (!icon.sourceDigest.equals(stored.getString("sourceSha256"))
				|| icon.sourceWidth != stored.getInt("sourceWidth")
				|| icon.sourceHeight != stored.getInt("sourceHeight")) {
			throw new IOException("UserTools.InvalidArchive");
		}
		return icon;
	}

	private void persist(Map<String, Package> next) throws IOException {
		String json;
		try {
			JSONArray entries = new JSONArray();
			for (Package tool : next.values()) {
				JSONArray pins = new JSONArray();
				for (Map.Entry<String, PinLayout> pin : tool.pinned.entrySet()) {
					PinIcon icon = pin.getValue().icon;
					pins.put(new JSONObject().put("command", pin.getKey())
							.put("group", pin.getValue().group)
							.put("order", pin.getValue().order)
							.put("icon", icon == null ? JSONObject.NULL : iconJson(icon)));
				}
				JSONArray definitions = new JSONArray();
				for (String command : tool.commands) {
					String definition = tool.definitionDigest(command);
					if (definition == null) {
						throw new IOException("UserTools.InvalidArchive");
					}
					definitions.put(new JSONObject().put("command", command)
							.put("sha256", definition));
				}
				entries.put(new JSONObject().put("name", tool.name).put("sha256", tool.id)
						.put("ggt", Base64.getEncoder().encodeToString(tool.bytes))
						.put("definitions", definitions).put("pinned", pins));
			}
			json = new JSONObject().put("version", STORE_VERSION)
					.put("definitionDigestVersion", DEFINITION_DIGEST_VERSION)
					.put("packages", entries).toString();
		} catch (JSONException exception) {
			throw new IOException("UserTools.InvalidArchive", exception);
		}
		if (json.getBytes(StandardCharsets.UTF_8).length > 2L * MAX_BYTES) {
			throw new IOException("UserTools.Limit");
		}
		Files.createDirectories(storage.getParent());
		Path temporary = Files.createTempFile(storage.getParent(), "user-tools-", ".tmp");
		try {
			Files.writeString(temporary, json, StandardCharsets.UTF_8);
			Files.move(temporary, storage, StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static JSONObject iconJson(PinIcon icon) throws JSONException {
		return new JSONObject().put("normalizationVersion", ICON_NORMALIZATION_VERSION)
				.put("sourceName", icon.sourceName)
				.put("sourceSha256", icon.sourceDigest)
				.put("sourceWidth", icon.sourceWidth)
				.put("sourceHeight", icon.sourceHeight)
				.put("source", Base64.getEncoder().encodeToString(icon.sourceBytes));
	}

	private void notifyListeners() {
		for (Runnable listener : List.copyOf(listeners)) {
			listener.run();
		}
	}

	private static String key(String command) {
		return command.toLowerCase(Locale.ROOT);
	}

	private static String digest(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/** Reuse the host parser/engine with an isolated registration table, never the live table. */
	private static final class ValidationKernel extends MacroKernel {
		private final MacroManager macros = new MacroManager();

		ValidationKernel(Kernel parent) {
			super(parent);
			setGlobalVariableLookup(false);
		}

		@Override
		public void addMacro(Macro macro) {
			macros.addMacro(macro);
		}

		@Override
		public Macro getMacro(String name) {
			return macros.getMacro(name);
		}

		@Override
		public ArrayList<Macro> getAllMacros() {
			return macros.getAllMacros();
		}
	}
}
