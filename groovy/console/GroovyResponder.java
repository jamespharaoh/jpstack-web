package wbs.platform.groovy.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.exception.logic.ExceptionLogicImpl;

@PrototypeComponent ("groovyResponder")
public
class GroovyResponder
	extends HtmlResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h1>Groovy executor</h1>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"\"",
			">\n");

		printFormat (
			"<p>Script<br>\n",
			"<textarea",
			" name=\"groovy\"",
			" cols=\"64\"",
			" rows=\"16\"",
			">%h</textarea></p>\n",
			ifNull (
				requestContext.getForm ("groovy"),
				""));

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"execute\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		String result =
			(String)
			requestContext.request ("result");

		String output =
			(String)
			requestContext.request ("output");

		if (result != null || output != null) {

			printFormat (
				"<h2>Result</h2>\n");

			printFormat (
				"<p>%h</p>\n",
				ifNull (result, "null"));

			printFormat (
				"<h2>Output</h2>\n",
				output);

		}

		Throwable excep =
			(Throwable) requestContext.request ("excep");

		if (excep != null) {

			printFormat (
				"<h2>Exception</h2>\n");

			printFormat (
				"<p><pre>%h</pre></p>\n",
				ExceptionLogicImpl.throwableDump (excep));

		}

	}

}
