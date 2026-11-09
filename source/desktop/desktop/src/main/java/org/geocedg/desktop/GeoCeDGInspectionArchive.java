/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geogebra.common.io.MyXMLio.JAVASCRIPT_FILE;
import static org.geogebra.common.io.MyXMLio.XML_FILE;
import static org.geogebra.common.io.MyXMLio.XML_FILE_DEFAULTS_2D;
import static org.geogebra.common.io.MyXMLio.XML_FILE_DEFAULTS_3D;
import static org.geogebra.common.io.MyXMLio.XML_FILE_MACRO;
import static org.geogebra.common.io.MyXMLio.XML_FILE_THUMBNAIL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.geocedg.common.kernel.geos.V2StyleDefaults;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Kernel;

/** Immutable input capture and non-evaluating ZIP gate for expert inspection. */
final class GeoCeDGInspectionArchive {

	static final int MAX_ARCHIVE_BYTES = 8 * 1024 * 1024;
	private static final int MAX_INFLATED_BYTES = 8 * 1024 * 1024;
	private static final int MAX_ENTRY_BYTES = 4 * 1024 * 1024;
	private static final int MAX_ENTRIES = 32;
	private static final int EOCD_BYTES = 22;
	private static final int CENTRAL_HEADER_BYTES = 46;
	private static final int LOCAL_HEADER_BYTES = 30;
	private static final int MAX_FINDINGS = 64;

	enum Code {
		INVALID_ARCHIVE, INVALID_XML, LIMIT_EXCEEDED, SCRIPT, MACRO, ANIMATION,
		RESOURCE, UNSUPPORTED_XML, UNSUPPORTED_EXPRESSION, UNSUPPORTED_COMMAND, CONTROL_LAYOUT
	}

	enum Disposition {
		NATIVE_XML, OMITTED_TRANSPORT, STATIC_ONLY
	}

	record Finding(Code code, String location, String detail) {
	}

	record Entry(String name, int expandedBytes, String sha256, Disposition disposition) {
	}

	/** An XML address, not a correspondence or proof of an identity. */
	record Declaration(String label, String nativeType, String durableId) {
	}

	static final class Inspection {
		private final byte[] original;
		private final byte[] nativeZip;
		private final String sha256;
		private final List<Entry> entries;
		private final List<Finding> findings;
		private final GeoCeDGInspectionXml.DocumentMetadata metadata;

		private Inspection(byte[] original, byte[] nativeZip, List<Entry> entries,
				List<Finding> findings, GeoCeDGInspectionXml.DocumentMetadata metadata) {
			this.original = original;
			this.nativeZip = nativeZip;
			sha256 = original == null ? null : digest(original);
			this.entries = List.copyOf(entries);
			this.findings = List.copyOf(findings);
			this.metadata = metadata;
		}

		String sha256() {
			return sha256;
		}

		List<Entry> entries() {
			return entries;
		}

		List<Finding> findings() {
			return findings;
		}

		Optional<String> documentId() {
			return Optional.ofNullable(metadata.documentId());
		}

		List<Declaration> declarations() {
			return metadata.declarations();
		}

		boolean loadable() {
			return nativeZip != null;
		}

		InputStream openOriginal() {
			if (original == null) {
				throw new IllegalStateException("Oversized archive was not captured");
			}
			return new ByteArrayInputStream(original);
		}

		InputStream openForNativeLoad() {
			if (!loadable()) {
				throw new IllegalStateException("Archive is restricted to static inspection");
			}
			return new ByteArrayInputStream(nativeZip);
		}
	}

	private record ZipMetadata(long crc, int expanded, int compressed, int method) {
	}

	private GeoCeDGInspectionArchive() {
	}

	/** Reads once, to prevent replacement of the file between inspection and loading. */
	static Inspection capture(Path path) throws IOException {
		Path fileName = path.getFileName();
		String name = fileName == null ? "" : fileName.toString().toLowerCase(Locale.ROOT);
		if ((!name.endsWith(".cedg") && !name.endsWith(".ggb")) || !Files.isRegularFile(path)) {
			throw new IOException("Expected a regular .cedg or .ggb file");
		}
		try (InputStream input = Files.newInputStream(path)) {
			byte[] bytes = input.readNBytes(MAX_ARCHIVE_BYTES + 1);
			if (bytes.length > MAX_ARCHIVE_BYTES) {
				throw new IOException("Native archive exceeds the compressed byte limit");
			}
			return inspectCaptured(bytes);
		}
	}

	static Inspection inspect(byte[] bytes) {
		if (bytes.length > MAX_ARCHIVE_BYTES) {
			return new Inspection(null, null, List.of(), List.of(new Finding(
					Code.LIMIT_EXCEEDED, "ZIP", "Compressed byte limit")),
					GeoCeDGInspectionXml.DocumentMetadata.EMPTY);
		}
		return inspectCaptured(bytes.clone());
	}

	private static Inspection inspectCaptured(byte[] bytes) {
		List<Entry> entries = new ArrayList<>();
		List<Finding> findings = new ArrayList<>();
		Map<String, byte[]> nativeEntries = new LinkedHashMap<>();
		GeoCeDGInspectionXml.DocumentMetadata metadata = GeoCeDGInspectionXml.DocumentMetadata.EMPTY;
		byte[] nativeZip = null;
		try {
			Map<String, ZipMetadata> directory = readDirectory(bytes);
			int remaining = MAX_INFLATED_BYTES;
			try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
				ZipEntry item;
				while ((item = zip.getNextEntry()) != null) {
					String name = item.getName();
					ZipMetadata expected = directory.remove(name);
					if (expected == null || item.isDirectory()) {
						throw new IOException("ZIP local headers do not match the directory");
					}
					int limit = Math.min(remaining, MAX_ENTRY_BYTES);
					byte[] raw = zip.readNBytes(limit + 1);
					if (raw.length > limit) {
						addFinding(findings, Code.LIMIT_EXCEEDED, name, "Expanded byte limit");
						return new Inspection(bytes, null, entries, findings, metadata);
					}
					zip.closeEntry();
					if (raw.length != expected.expanded() || item.getCrc() != expected.crc()
							|| item.getCompressedSize() != expected.compressed()
							|| item.getMethod() != expected.method()) {
						throw new IOException("ZIP local entry metadata does not match the directory");
					}
					remaining -= raw.length;
					Disposition disposition;
					if (XML_FILE.equals(name)) {
						metadata = GeoCeDGInspectionXml.scanDocument(name, raw, findings);
						disposition = metadata.safe() ? Disposition.NATIVE_XML
								: Disposition.STATIC_ONLY;
					} else {
						disposition = checkEntry(name, raw, findings);
					}
					entries.add(new Entry(name, raw.length, digest(raw), disposition));
					if (disposition == Disposition.NATIVE_XML) {
						nativeEntries.put(name, raw);
					}
				}
			}
			if (!directory.isEmpty() || entries.stream().noneMatch(entry -> XML_FILE.equals(entry.name()))) {
				throw new IOException("Incomplete ZIP directory or missing construction XML");
			}
			if (findings.isEmpty()) {
				nativeZip = repack(nativeEntries);
			}
		} catch (IOException | IllegalArgumentException malformed) {
			addFinding(findings, Code.INVALID_ARCHIVE, "ZIP", "Invalid native ZIP structure or CRC");
		}
		return new Inspection(bytes, nativeZip, entries, findings, metadata);
	}

	/** Rejects split, ZIP64, concatenated and ambiguous archives before inflating them. */
	private static Map<String, ZipMetadata> readDirectory(byte[] bytes) throws IOException {
		ByteBuffer zip = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		int end = bytes.length - EOCD_BYTES;
		if (end < 0 || zip.getInt(end) != 0x06054b50 || zip.getInt(end + 4) != 0
				|| zip.getShort(end + 20) != 0) {
			throw new IOException("Invalid ZIP end record");
		}
		int count = unsignedShort(zip, end + 10);
		int start = zip.getInt(end + 16);
		if (count == 0 || count > MAX_ENTRIES || unsignedShort(zip, end + 8) != count
				|| start <= 0 || start >= end || zip.getInt(end + 12) != end - start) {
			throw new IOException("Invalid ZIP directory bounds");
		}
		Map<String, ZipMetadata> entries = new LinkedHashMap<>();
		int position = start;
		int nextLocal = 0;
		for (int index = 0; index < count; index++) {
			if (position + CENTRAL_HEADER_BYTES > end || zip.getInt(position) != 0x02014b50
					|| zip.getShort(position + 34) != 0 || zip.getInt(position + 42) != nextLocal) {
				throw new IOException("Invalid ZIP directory entry");
			}
			int flags = unsignedShort(zip, position + 8);
			int method = unsignedShort(zip, position + 10);
			int compressed = zip.getInt(position + 20);
			int expanded = zip.getInt(position + 24);
			int nameLength = unsignedShort(zip, position + 28);
			int nameOffset = position + CENTRAL_HEADER_BYTES;
			int next = nameOffset + nameLength + unsignedShort(zip, position + 30)
					+ unsignedShort(zip, position + 32);
			if (next > end || nameLength == 0 || nameLength > 256 || compressed < 0
					|| expanded < 0 || (flags & ~0x080e) != 0
					|| method != ZipEntry.STORED && method != ZipEntry.DEFLATED) {
				throw new IOException("Unsupported ZIP entry encoding or size");
			}
			String name = StandardCharsets.UTF_8.newDecoder().decode(
					ByteBuffer.wrap(bytes, nameOffset, nameLength)).toString();
			if (!safeEntryName(name)) {
				throw new IOException("Unsafe ZIP entry name");
			}
			ZipMetadata meta = new ZipMetadata(Integer.toUnsignedLong(zip.getInt(position + 16)),
					expanded, compressed, method);
			if (entries.putIfAbsent(name, meta) != null) {
				throw new IOException("Duplicate ZIP entry name");
			}
			nextLocal = checkLocalEntry(zip, start, nextLocal, nameOffset, nameLength, flags, meta);
			position = next;
		}
		if (position != end || nextLocal != start) {
			throw new IOException("Unclaimed bytes in ZIP");
		}
		return entries;
	}

	private static int checkLocalEntry(ByteBuffer zip, int bound, int start,
			int nameOffset, int nameLength, int flags, ZipMetadata meta) throws IOException {
		if (start + LOCAL_HEADER_BYTES > bound || zip.getInt(start) != 0x04034b50
				|| unsignedShort(zip, start + 6) != flags
				|| unsignedShort(zip, start + 8) != meta.method()
				|| unsignedShort(zip, start + 26) != nameLength) {
			throw new IOException("ZIP local header mismatch");
		}
		int nameStart = start + LOCAL_HEADER_BYTES;
		long payloadEnd = (long) nameStart + nameLength + unsignedShort(zip, start + 28)
				+ meta.compressed();
		if (payloadEnd > bound) {
			throw new IOException("ZIP payload exceeds its bounds");
		}
		for (int i = 0; i < nameLength; i++) {
			if (zip.get(nameStart + i) != zip.get(nameOffset + i)) {
				throw new IOException("ZIP local filename mismatch");
			}
		}
		int next = (int) payloadEnd;
		if ((flags & 8) != 0) {
			if (next + 16 <= bound && zip.getInt(next) == 0x08074b50) {
				next += 4;
			}
			if (next + 12 > bound || !matchesMetadata(zip, next, meta)) {
				throw new IOException("ZIP data descriptor mismatch");
			}
			return next + 12;
		}
		if (!matchesMetadata(zip, start + 14, meta)) {
			throw new IOException("ZIP local metadata mismatch");
		}
		return next;
	}

	private static boolean matchesMetadata(ByteBuffer zip, int position, ZipMetadata meta) {
		return Integer.toUnsignedLong(zip.getInt(position)) == meta.crc()
				&& zip.getInt(position + 4) == meta.compressed()
				&& zip.getInt(position + 8) == meta.expanded();
	}

	private static int unsignedShort(ByteBuffer zip, int position) {
		return Short.toUnsignedInt(zip.getShort(position));
	}

	private static boolean safeEntryName(String name) {
		if (name.contains("\\") || name.contains(":")) {
			return false;
		}
		for (String segment : name.split("/", -1)) {
			if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)
					|| segment.chars().anyMatch(Character::isISOControl)) {
				return false;
			}
		}
		return true;
	}

	private static Disposition checkEntry(String name, byte[] raw, List<Finding> findings) {
		return switch (name) {
		case XML_FILE_DEFAULTS_2D, XML_FILE_DEFAULTS_3D ->
				GeoCeDGInspectionXml.scanDefaults(name, raw, findings);
		case V2StyleDefaults.ZIP_ENTRY -> checkV2Defaults(name, raw, findings);
		case JAVASCRIPT_FILE -> checkJavaScript(name, raw, findings);
		case XML_FILE_THUMBNAIL -> Disposition.OMITTED_TRANSPORT;
		case XML_FILE_MACRO -> {
			GeoCeDGInspectionXml.scanMacros(name, raw, findings);
			yield Disposition.STATIC_ONLY;
		}
		default -> {
			addFinding(findings, Code.RESOURCE, name, "Resource decoder is outside the pilot");
			yield Disposition.STATIC_ONLY;
		}
		};
	}

	private static Disposition checkJavaScript(String name, byte[] raw, List<Finding> findings) {
		String script = new String(raw, StandardCharsets.UTF_8);
		if (Kernel.defaultLibraryJavaScript.equals(script)
				|| (Kernel.defaultLibraryJavaScript + "\n").equals(script)) {
			return Disposition.OMITTED_TRANSPORT;
		}
		addFinding(findings, Code.SCRIPT, name, "Custom JavaScript library");
		return Disposition.STATIC_ONLY;
	}

	private static Disposition checkV2Defaults(String name, byte[] raw, List<Finding> findings) {
		try {
			if (raw.length > V2StyleDefaults.MAX_BYTES) {
				addFinding(findings, Code.LIMIT_EXCEEDED, name, "V2 defaults byte limit");
				return Disposition.STATIC_ONLY;
			}
			V2StyleDefaults.read(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(raw))
					.toString());
			return Disposition.NATIVE_XML;
		} catch (IOException | XMLParseException malformed) {
			addFinding(findings, Code.INVALID_XML, name, "Invalid V2 style defaults");
			return Disposition.STATIC_ONLY;
		}
	}

	static void addFinding(List<Finding> findings, Code code, String location, String detail) {
		if (findings.size() < MAX_FINDINGS) {
			findings.add(new Finding(code, location, detail));
		}
	}

	private static byte[] repack(Map<String, byte[]> entries) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
				ZipEntry part = new ZipEntry(entry.getKey());
				part.setTime(0);
				zip.putNextEntry(part);
				zip.write(entry.getValue());
				zip.closeEntry();
			}
		}
		return output.toByteArray();
	}

	static String digest(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}
}
