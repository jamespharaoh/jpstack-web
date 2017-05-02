package wbs.console.part;

import java.util.Map;
import java.util.Set;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.database.Transaction;

public
interface PagePart {

	void setup (
			Transaction parentTransaction,
			Map <String, Object> parameters);

	void prepare (
			Transaction parentTransaction);

	Set <ScriptRef> scriptRefs ();

	Set <HtmlLink> links ();

	void renderHtmlHeadContent (
			Transaction parentTransaction);

	void renderHtmlBodyContent (
			Transaction parentTransaction);

	void setWithMarkup (
			boolean markup);

	void cleanup ();

}
