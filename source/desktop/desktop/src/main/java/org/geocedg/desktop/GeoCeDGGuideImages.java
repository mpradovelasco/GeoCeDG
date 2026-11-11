/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

/** Decodes bounded PNG figures and renders only the images supplied by the guide parser. */
final class GeoCeDGGuideImages {
	static final String RESOURCE_ROOT = "/org/geocedg/desktop/guide/";
	static final int MAX_BYTES = 2 * 1024 * 1024;
	static final int MAX_EDGE = 2048;
	static final int MAX_PIXELS = 3_000_000;
	static final int MAX_DISPLAY_WIDTH = 620;
	static final int MAX_DISPLAY_HEIGHT = 620;
	private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4e, 0x47,
			0x0d, 0x0a, 0x1a, 0x0a};
	private static final Pattern PATH = Pattern.compile("images/[a-z0-9][a-z0-9-]{0,79}\\.png");

	private GeoCeDGGuideImages() {
	}

	static ImageIcon load(String source) throws IOException {
		// Accept only the canonical relative path. No URL, path resolution, or decoding.
		if (!PATH.matcher(source).matches()) {
			throw new IOException("expected a canonical images/name.png path");
		}
		try (InputStream stream = GeoCeDGGuideImages.class.getResourceAsStream(
				RESOURCE_ROOT + source)) {
			if (stream == null) {
				throw new IOException("packaged image is missing");
			}
			return decode(stream);
		}
	}

	static ImageIcon decode(InputStream stream) throws IOException {
		byte[] bytes = stream.readNBytes(MAX_BYTES + 1);
		if (bytes.length > MAX_BYTES) {
			throw new IOException("PNG exceeds the 2 MiB byte limit");
		}
		if (bytes.length < PNG_SIGNATURE.length
				|| !Arrays.equals(bytes, 0, PNG_SIGNATURE.length,
						PNG_SIGNATURE, 0, PNG_SIGNATURE.length)) {
			throw new IOException("image is not a PNG");
		}
		var readers = ImageIO.getImageReadersByFormatName("png");
		if (!readers.hasNext()) {
			throw new IOException("no PNG decoder is available");
		}
		ImageReader reader = readers.next();
		try (var imageInput = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
			reader.setInput(imageInput, true, true);
			int width = reader.getWidth(0);
			int height = reader.getHeight(0);
			if (width <= 0 || height <= 0 || width > MAX_EDGE || height > MAX_EDGE
					|| (long) width * height > MAX_PIXELS) {
				throw new IOException("PNG dimensions exceed the guide limit");
			}
			return new ImageIcon(scale(reader.read(0)));
		} catch (RuntimeException exception) {
			throw new IOException("PNG could not be decoded", exception);
		} finally {
			reader.dispose();
		}
	}

	private static BufferedImage scale(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		double ratio = Math.min((double) MAX_DISPLAY_WIDTH / width,
				(double) MAX_DISPLAY_HEIGHT / height);
		if (ratio >= 1) {
			return image;
		}
		int scaledWidth = Math.max(1, (int) (width * ratio));
		int scaledHeight = Math.max(1, (int) (height * ratio));
		BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = scaled.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
		} finally {
			graphics.dispose();
		}
		return scaled;
	}

	/** Gives Swing a view backed by the parsed figure with URL loading disabled. */
	static final class EditorKit extends HTMLEditorKit {
		private static final long serialVersionUID = 1L;
		private transient Map<String, ImageIcon> images = Map.of();
		private final transient ViewFactory factory = new HTMLFactory() {
			@Override
			public View create(Element element) {
				var attributes = element.getAttributes();
				if (attributes.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMG) {
					String source = (String) attributes.getAttribute(HTML.Attribute.SRC);
					return new FigureView(element, source == null ? null : images.get(source));
				}
				return super.create(element);
			}
		};

		void setImages(Map<String, ImageIcon> images) {
			this.images = images;
		}

		@Override
		public ViewFactory getViewFactory() {
			return factory;
		}
	}

	private static final class FigureView extends ImageView {
		private static final int PAGE_GUTTER = 32;
		private final ImageIcon icon;
		private JViewport viewport;
		private final ComponentAdapter viewportResize = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				preferenceChanged(null, true, true);
			}
		};

		FigureView(Element element, ImageIcon icon) {
			super(element);
			this.icon = icon;
		}

		@Override
		public URL getImageURL() {
			return null;
		}

		@Override
		public void paint(Graphics graphics, Shape allocation) {
			if (icon == null) {
				super.paint(graphics, allocation);
				return;
			}
			Rectangle bounds = allocation.getBounds();
			Dimension size = figureSize();
			Graphics2D copy = (Graphics2D) graphics.create();
			try {
				copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				copy.drawImage(icon.getImage(), bounds.x, bounds.y, size.width, size.height, null);
			} finally {
				copy.dispose();
			}
		}

		@Override
		public void setParent(View parent) {
			if (viewport != null) {
				viewport.removeComponentListener(viewportResize);
			}
			super.setParent(parent);
			viewport = parent == null ? null : (JViewport) SwingUtilities.getAncestorOfClass(
					JViewport.class, getContainer());
			if (viewport != null && icon != null) {
				viewport.addComponentListener(viewportResize);
			}
		}

		@Override
		public float getPreferredSpan(int axis) {
			if (icon == null) {
				return super.getPreferredSpan(axis);
			}
			Dimension size = figureSize();
			return axis == X_AXIS ? size.width : size.height;
		}

		@Override
		public float getMinimumSpan(int axis) {
			return getPreferredSpan(axis);
		}

		@Override
		public float getMaximumSpan(int axis) {
			return getPreferredSpan(axis);
		}

		private Dimension figureSize() {
			int width = icon.getIconWidth();
			if (viewport != null && viewport.getExtentSize().width > PAGE_GUTTER) {
				width = Math.min(width, viewport.getExtentSize().width - PAGE_GUTTER);
			}
			int height = (int) Math.ceil((double) icon.getIconHeight() * width / icon.getIconWidth());
			return new Dimension(width, height);
		}
	}
}
