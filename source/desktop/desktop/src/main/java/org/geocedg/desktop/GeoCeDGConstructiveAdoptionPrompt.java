/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geocedg.common.kernel.locus.constructive.AuthorityAdoptionImpact;

/**
 * PRE-G9B-CU3 explicit adoption event of {@code constructive-authority/v1}
 * ({@code locus-v2-constructive-authority.md} section 4; validation matrix rows V-AUT-ADOPT-1
 * and V-AUT-ADOPT-2), offered when a form of {@code CU3} is entered in a document governed by
 * the legacy policy. It shows the impact report computed before anything is applied and only
 * returns the decision; the adoption, one undoable operation, belongs to the caller. Tests
 * inject a lambda.
 */
interface GeoCeDGConstructiveAdoptionPrompt {

	/**
	 * @param app application
	 * @param impact impact report of the adoption, computed without applying anything
	 * @return whether the document adopts the constructive authority
	 */
	boolean ask(AppGeoCeDG app, AuthorityAdoptionImpact impact);

	/**
	 * @return the Swing dialog
	 */
	static GeoCeDGConstructiveAdoptionPrompt dialog() {
		return (app, impact) -> {
			String language = app.getLocale().getLanguage();
			JTextArea report = new JTextArea(text(impact, language), 10, 60);
			report.setEditable(false);
			report.setLineWrap(true);
			report.setWrapStyleWord(true);
			report.setCaretPosition(0);
			String adopt = GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.Adopt",
					language);
			String cancel = GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.Cancel",
					language);
			Object[] options = {adopt, cancel};
			int choice = JOptionPane.showOptionDialog(app.getMainComponent(),
					new Object[] {GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.Message",
							language), new JScrollPane(report)},
					GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.Title", language),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
					options[1]);
			return choice == 0;
		};
	}

	/**
	 * @param impact impact report
	 * @param language language of the texts
	 * @return the report as text: the relations examined and every change of integrity or
	 *         admission, in construction order
	 */
	static String text(AuthorityAdoptionImpact impact, String language) {
		StringBuilder text = new StringBuilder(GeoCeDGProfile.getText(
				"ConstructiveAuthority.Adoption.Examined", language)).append(": ")
						.append(impact.getExaminedRelations()).append('\n');
		if (impact.isEmpty()) {
			return text.append(GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.NoChange",
					language)).toString();
		}
		text.append(GeoCeDGProfile.getText("ConstructiveAuthority.Adoption.Changes", language))
				.append(": ").append(impact.getEntries().size()).append('\n');
		for (AuthorityAdoptionImpact.Entry entry : impact.getEntries()) {
			text.append("  ").append(entry).append('\n');
		}
		return text.toString().stripTrailing();
	}
}
