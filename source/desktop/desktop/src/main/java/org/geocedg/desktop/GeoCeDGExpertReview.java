/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.geocedg.desktop.GeoCeDGDocumentInsertion.ExpertOffer;

/** Swing lifecycle of one expert operation. Publication authority remains in F3. */
final class GeoCeDGExpertReview {
	static final String FEATURE_ID = "cedg.document.expert-insertion";
	static final String ARGUMENT = "enableExpertInsertion";
	private final AppGeoCeDG app;

	GeoCeDGExpertReview(AppGeoCeDG app) {
		this.app = app;
	}

	void start() {
		File selected = app.getInsertionSourceChooser().choose(app, "Insert.Expert.Title");
		if (selected == null) {
			return;
		}
		File file = GeoCeDGDocumentPolicy.resolveInsertionName(selected);
		String refusal = GeoCeDGDocumentPolicy.insertionRefusal(file);
		if (refusal != null) {
			message(app.layerText(refusal, GeoCeDGExpertReviewModel.plain(file.getName())));
			return;
		}
		var insertion = app.getDocumentInsertion();
		try (var review = insertion.beginExpertReview();
				var result = inspect(file, new GeoCeDGExpertWorker(review.snapshot()))) {
			if (result == null) {
				message(text("Cancelled"));
				return;
			}
			GeoCeDGExpertReviewModel model = GeoCeDGExpertReviewModel.project(app, result);
			BooleanSupplier current = () -> review.isCurrent() && !result.source().changed();
			ExpertOffer offer = null;
			if (result.available() && current.getAsBoolean()) {
				try {
					offer = review.offer(file, result.sourceBytes(), result.fingerprint(),
							result.reportSha256(), () -> !result.source().changed());
				} catch (IOException unavailable) {
					message(app.layerText("Insert.Expert.Stale",
							GeoCeDGExpertReviewModel.plain(file.getName())));
				}
			}
			if (!showReview(file, model, result.preview(), offer != null, current)) {
				return;
			}
			var publication = insertion.publishExpert(offer, result.reportSha256());
			switch (publication.getOutcome()) {
			case INSERTED -> {
				app.recordExpertInsertion(file, result.source().resolvedFile(), publication.getExpertStep());
				showSuccess(model.summary());
			}
			case SERIOUS_RECOVERY_FAILURE -> {
				// F3 already presents its isolation exit dialog.
			}
			default -> message(publication.getMessage());
			}
		} catch (IOException failure) {
			String detail = failure instanceof GeoCeDGExpertWorker.Timeout ? text("Timeout")
					: GeoCeDGExpertReviewModel.plain(failure.getMessage());
			message(app.layerText("Insert.Expert.Failed", detail));
		}
	}

	private GeoCeDGExpertWorker.Result inspect(File source, GeoCeDGExpertWorker worker)
			throws IOException {
		AtomicBoolean cancelled = new AtomicBoolean();
		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.getAccessibleContext().setAccessibleName(text("Inspecting"));
		JPanel content = new JPanel(new BorderLayout(0, 8));
		content.add(new JLabel(text("Inspecting")), BorderLayout.NORTH);
		content.add(progress, BorderLayout.CENTER);
		JButton cancel = new JButton(app.getLocalization().getMenu("Cancel"));
		JOptionPane pane = pane(content, cancel);
		JDialog dialog = dialog(pane);
		Runnable cancelInspection = () -> {
			cancelled.set(true);
			dialog.dispose();
		};
		cancel.addActionListener(event -> cancelInspection.run());
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				cancelInspection.run();
			}
		});
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		SwingWorker<GeoCeDGExpertWorker.Result, Void> operation = new SwingWorker<>() {
			@Override
			protected GeoCeDGExpertWorker.Result doInBackground() throws Exception {
				return worker.inspect(source, cancelled::get);
			}

			@Override
			protected void done() {
				dialog.dispose();
				if (cancelled.get()) {
					try {
						get().close();
					} catch (InterruptedException failure) {
						Thread.currentThread().interrupt();
					} catch (ExecutionException failure) {
						// Worker owns cleanup of interrupted, failed and cancelled runs.
					}
				}
			}
		};
		operation.execute();
		dialog.setVisible(true);
		if (cancelled.get() || !operation.isDone()) {
			cancelled.set(true);
			return null;
		}
		try {
			return operation.get();
		} catch (InterruptedException failure) {
			Thread.currentThread().interrupt();
			throw new IOException("Inspection interrupted", failure);
		} catch (ExecutionException failure) {
			if (failure.getCause() instanceof IOException io) {
				throw io;
			}
			throw new IOException("Inspection worker failed", failure.getCause());
		}
	}

	private boolean showReview(File file, GeoCeDGExpertReviewModel model, BufferedImage preview,
			boolean offerAvailable, BooleanSupplier current) {
		JTabbedPane tabs = new JTabbedPane();
		String source = app.layerText("Insert.Expert.SourceFile",
				GeoCeDGExpertReviewModel.plain(file.getAbsolutePath()));
		tabs.addTab(text("Summary"), scrollText(source + "\n\n" + model.summary(),
				text("Summary"), true));
		tabs.addTab(text("Details"), scrollText(model.details(), text("Details"), false));
		tabs.addTab(text("Preview"), preview == null
				? scrollText(text("PreviewMissing"), text("Preview"), true)
				: new JScrollPane(new JLabel(new ImageIcon(preview, text("Preview")))));
		tabs.setPreferredSize(new Dimension(650, 380));
		JPanel content = new JPanel(new BorderLayout(0, 8));
		String stale = app.layerText("Insert.Expert.Stale",
				GeoCeDGExpertReviewModel.plain(file.getName()));
		JLabel status = new JLabel(offerAvailable ? text("Review")
				: current.getAsBoolean() ? text("Unavailable") : stale);
		content.add(status, BorderLayout.NORTH);
		content.add(tabs, BorderLayout.CENTER);
		JButton close = new JButton(offerAvailable ? app.getLocalization().getMenu("Cancel")
				: text("Close"));
		JButton accept = new JButton(text("Accept"));
		JOptionPane pane = offerAvailable ? pane(content, close, accept) : pane(content, close);
		JDialog dialog = dialog(pane);
		close.addActionListener(event -> dialog.dispose());
		boolean[] accepted = {false};
		Runnable check = () -> {
			if (offerAvailable && !current.getAsBoolean()) {
				accept.setEnabled(false);
				status.setText(stale);
			}
		};
		accept.addActionListener(event -> {
			check.run();
			if (accept.isEnabled()) {
				accepted[0] = true;
				dialog.dispose();
			}
		});
		Timer expiry = new Timer(250, event -> check.run());
		expiry.start();
		try {
			dialog.setVisible(true);
		} finally {
			expiry.stop();
			dialog.dispose();
		}
		return offerAvailable && accepted[0];
	}

	private void showSuccess(String summary) {
		Object save = text("SaveAs");
		Object close = text("Close");
		int choice = JOptionPane.showOptionDialog(app.getMainComponent(),
				scrollText(text("Done") + "\n\n" + summary, text("Done"), true),
				text("Title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
				new Object[]{close, save}, close);
		if (choice == 1) {
			((GuiManagerGeoCeDG) app.getGuiManager()).saveAs();
		}
	}

	private static JScrollPane scrollText(String text, String name, boolean wrap) {
		JTextArea area = new JTextArea(text, 9, 58);
		area.setEditable(false);
		area.setLineWrap(wrap);
		area.setWrapStyleWord(wrap);
		area.setCaretPosition(0);
		area.getAccessibleContext().setAccessibleName(name);
		return new JScrollPane(area);
	}

	private static JOptionPane pane(Object content, JButton... options) {
		return new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, options, options[0]);
	}

	private JDialog dialog(JOptionPane pane) {
		JDialog dialog = pane.createDialog(app.getMainComponent(), text("Title"));
		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return dialog;
	}

	private String text(String key) {
		return app.layerText("Insert.Expert." + key);
	}

	private void message(String message) {
		JOptionPane.showMessageDialog(app.getMainComponent(), message, text("Title"),
				JOptionPane.INFORMATION_MESSAGE);
	}
}
