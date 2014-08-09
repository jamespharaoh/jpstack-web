package wbs.platform.script.system.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.script.system.model.SystemScriptRec;

@PrototypeComponent ("systemScriptSourcePart")
public
class SystemScriptSourcePart
	extends AbstractPagePart {

	@Inject
	SystemScriptConsoleHelper systemScriptHelper;

	SystemScriptRec script;

	@Override
	public
	void prepare () {

		script =
			systemScriptHelper.find (
				requestContext.stuffInt ("systemScriptId"));

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl ("/systemScript.source"),
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"<p><textarea",
			" name=\"text\"",
			" style=\"width: 100%%; height: 75%%;\"",
			" cols=\"80\"",
			" rows=\"40\"",
			">%h</textarea></p>",
			script.getText ());

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>");

		printFormat (
			"</form>\n");

	}

}
