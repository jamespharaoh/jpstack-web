package wbs.console.part;

import java.util.Map;
import java.util.Set;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.logging.TaskLogger;

public
interface PagePart {

	void setup (
			TaskLogger parentTaskLogger,
			Map <String, Object> parameters);

	void prepare (
			TaskLogger taskLogger);

	Set <ScriptRef> scriptRefs ();

	Set <HtmlLink> links ();

	void renderHtmlHeadContent (
			TaskLogger taskLogger);

	void renderHtmlBodyContent (
			TaskLogger taskLogger);

	void setWithMarkup (
			boolean markup);

	void cleanup ();

}
