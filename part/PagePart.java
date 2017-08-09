package wbs.console.part;

import java.util.Set;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.database.Transaction;

import wbs.utils.string.FormatWriter;

public
interface PagePart {

	void prepare (
			Transaction parentTransaction);

	Set <ScriptRef> scriptRefs ();

	Set <HtmlLink> links ();

	void renderHtmlHeadContent (
			Transaction parentTransaction,
			FormatWriter formatWriter);

	void renderHtmlBodyContent (
			Transaction parentTransaction,
			FormatWriter formatWriter);

	void setWithMarkup (
			boolean markup);

	void cleanup ();

}
