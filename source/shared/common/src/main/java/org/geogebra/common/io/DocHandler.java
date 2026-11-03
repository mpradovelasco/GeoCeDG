// vendored
/*
 * Quick and dirty XML parser. Java Tip 128
 * http://www.javaworld.com/javaworld/javatips/jw-javatip128.html
 */

// GeoCeDG modification, 2026-11-03: focal empty-leaf grammar opt-in.
package org.geogebra.common.io;

import java.util.Map;

/**
 * Event listener for MyXMLio
 */
public interface DocHandler {
	/**
	 * Opt-in grammar for a versioned empty leaf. Other XML consumers retain their
	 * historical parser policy. Checked before duplicate attributes can collapse.
	 * @param tag element name
	 * @return whether this handler requires a duplicate-free, self-closing leaf
	 */
	default boolean hasStrictEmptyElementGrammar(String tag) {
		return false;
	}

	/**
	 * @param tag
	 *            element name
	 * @param h
	 *            attributes
	 * @throws XMLParseException
	 *             if invalid
	 */
	void startElement(String tag, Map<String, String> h)
			throws XMLParseException;

	/**
	 * @param tag
	 *            element name
	 * @throws XMLParseException
	 *             if invalid
	 */
	void endElement(String tag) throws XMLParseException;

	/**
	 * @throws XMLParseException
	 *             if invalid
	 */
	void startDocument() throws XMLParseException;

	/**
	 * @throws XMLParseException
	 *             if invalid
	 */
	void endDocument() throws XMLParseException;

	/**
	 * @param str
	 *            string between start and end tags
	 * @throws XMLParseException
	 *             if invalid
	 */
	void text(String str) throws XMLParseException;

}
