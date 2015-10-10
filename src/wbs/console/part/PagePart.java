package wbs.console.part;

import java.util.Map;
import java.util.Set;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

public
interface PagePart {

	void setup (
			Map<String,Object> parameters);

	void prepare ();

	Set<ScriptRef> scriptRefs ();

	Set<HtmlLink> links ();

	void renderHtmlHeadContent ();

	void renderHtmlBodyContent ();

	void setWithMarkup (
			boolean markup);

}
