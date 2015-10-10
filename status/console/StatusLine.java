package wbs.platform.status.console;

import wbs.console.part.PagePart;

public
interface StatusLine {

	/**
	 * Returns a unique name for this status line.
	 */
	String getName ();

	/**
	 * Generates a PagePart to include in the status page.
	 */
	PagePart get ();

	/**
	 * Generates any javascript code to update the status page.
	 */
	String getUpdateScript ();
}
