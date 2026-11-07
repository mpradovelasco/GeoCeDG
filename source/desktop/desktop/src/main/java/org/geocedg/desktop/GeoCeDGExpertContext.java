/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;

/** Inert Desktop state affecting native insertion but absent from the archive. */
record GeoCeDGExpertContext(int workingLayer, int maxLayer, int activeView,
		boolean secondViewShowing, boolean thirdViewInitialized, Map<String, Integer> residentLayers,
		Set<String> protectedLabels, String residentPresentation) {
	private static final int VERSION = 3;
	private static final int MAX_BYTES = 64 * 1024;
	private static final int MAX_RECORDS = 512;

	static GeoCeDGExpertContext capture(AppGeoCeDG app) {
		return new GeoCeDGExpertContext(app.getLayerWorkspace().getWorkingLayer(),
				app.getMaxLayerUsed(), app.getActiveEuclidianView().getViewID(),
				app.hasEuclidianView2(1), app.isEuclidianView3Dinited(), layers(app),
				Collections.unmodifiableSet(new TreeSet<>(app.getKernel().getConstruction()
						.snapshotProtectedLabels())), presentationDigest(app));
	}

	static Map<String, Integer> layers(AppGeoCeDG app) {
		Map<String, Integer> result = new TreeMap<>();
		for (GeoElement geo : app.getKernel().getConstruction()
				.getGeoSetWithCasCellsConstructionOrder()) {
			String label = geo.getLabelSimple();
			if (label != null) {
				result.put(label, geo.getLayer());
			}
		}
		return Collections.unmodifiableMap(result);
	}

	private static String presentationDigest(AppGeoCeDG app) {
		StringBuilder rows = new StringBuilder();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetLabelOrder()) {
			String label = geo.getLabelSimple();
			String presentation = GeoCeDGExpertInspection.presentation(geo);
			rows.append(label.length()).append(':').append(label)
					.append(presentation.length()).append(':').append(presentation);
		}
		return GeoCeDGInspectionArchive.digest(rows.toString().getBytes(StandardCharsets.UTF_8));
	}

	void apply(AppGeoCeDG app) throws IOException {
		var workspace = app.getLayerWorkspace();
		if (workingLayer < 0 || workingLayer > workspace.getMaxLayer()
				|| maxLayer < 0 || maxLayer > workspace.getMaxLayer()
				|| (activeView != App.VIEW_EUCLIDIAN && activeView != App.VIEW_EUCLIDIAN2)
				|| secondViewShowing != app.hasEuclidianView2(1)
				|| thirdViewInitialized != app.isEuclidianView3Dinited()
				|| !workspace.isLayerShown(workingLayer) || !residentLayers.equals(layers(app))
				|| !residentPresentation.equals(presentationDigest(app))) {
			throw new IOException("Effective receiver layers, view or presentation cannot be reproduced");
		}
		workspace.setWorkingLayer(workingLayer);
		app.restoreMaxLayerUsed(maxLayer);
		app.setActiveView(activeView);
		if (app.getActiveEuclidianView().getViewID() != activeView) {
			throw new IOException("Active receiver view is unavailable in the worker");
		}
		app.getKernel().getConstruction().restoreProtectedLabels(protectedLabels);
	}

	void write(Path target) throws IOException {
		if (residentLayers.size() > MAX_RECORDS || protectedLabels.size() > MAX_RECORDS) {
			throw new IOException("Receiver context exceeds the record budget");
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var out = new DataOutputStream(bytes)) {
			out.writeInt(VERSION);
			out.writeInt(workingLayer);
			out.writeInt(maxLayer);
			out.writeInt(activeView);
			out.writeBoolean(secondViewShowing);
			out.writeBoolean(thirdViewInitialized);
			out.writeInt(residentLayers.size());
			for (var entry : residentLayers.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeInt(entry.getValue());
			}
			out.writeInt(protectedLabels.size());
			for (String label : protectedLabels) {
				out.writeUTF(label);
			}
			out.writeUTF(residentPresentation);
		}
		if (bytes.size() > MAX_BYTES) {
			throw new IOException("Receiver context exceeds the byte budget");
		}
		Files.write(target, bytes.toByteArray(), StandardOpenOption.CREATE_NEW);
	}

	record Captured(GeoCeDGExpertContext context, String sha256) {
	}

	static Captured read(Path path) throws IOException {
		byte[] bytes;
		try (var stream = Files.newInputStream(path)) {
			bytes = stream.readNBytes(MAX_BYTES + 1);
		}
		if (bytes.length > MAX_BYTES) {
			throw new IOException("Receiver context exceeds the byte budget");
		}
		try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
			if (in.readInt() != VERSION) {
				throw new IOException("Unknown receiver context version");
			}
			int working = in.readInt();
			int max = in.readInt();
			int view = in.readInt();
			boolean secondView = in.readBoolean();
			boolean thirdView = in.readBoolean();
			Map<String, Integer> layers = new TreeMap<>();
			int count = count(in);
			for (int i = 0; i < count; i++) {
				if (layers.put(in.readUTF(), in.readInt()) != null) {
					throw new IOException("Duplicate resident address in receiver context");
				}
			}
			Set<String> protectedLabels = new TreeSet<>();
			count = count(in);
			for (int i = 0; i < count; i++) {
				if (!protectedLabels.add(in.readUTF())) {
					throw new IOException("Duplicate reservation in receiver context");
				}
			}
			String presentation = in.readUTF();
			if (!presentation.matches("[0-9a-f]{64}")) {
				throw new IOException("Malformed receiver presentation digest");
			}
			if (in.read() != -1) {
				throw new IOException("Unexpected receiver context data");
			}
			return new Captured(new GeoCeDGExpertContext(working, max, view, secondView, thirdView,
					layers, protectedLabels, presentation), GeoCeDGInspectionArchive.digest(bytes));
		}
	}

	private static int count(DataInputStream in) throws IOException {
		int count = in.readInt();
		if (count < 0 || count > MAX_RECORDS) {
			throw new IOException("Invalid receiver context record count");
		}
		return count;
	}
}
