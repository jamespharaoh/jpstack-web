package wbs.platform.console.part;

import java.util.Map;
import java.util.Set;

import wbs.platform.console.html.HtmlLink;
import wbs.platform.console.html.ScriptRef;

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
