package wbs.platform.core.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("coreLogonResponder")
public
class CoreLogonResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	public
	String getTitle () {
		return wbsConfig.consoleTitle ();
	}

	// implementation

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script type=\"text/javascript\">\n",
			"  if (window.parent != window)\n",
			"    window.parent.location = window.location;\n",
			"</script>\n");

	}


	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h1>%h</h1>\n",
			wbsConfig.consoleTitle ());

		printFormat (
			"<h1>Please log on</h1>\n");

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/"),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Slice</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"slice\"",
			" value=\"%h\"",
			requestContext.parameter (
				"slice",
				wbsConfig.defaultSlice ()),
			" size=\"32\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Username</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"username\"",
			" value=\"%h\"",
			requestContext.parameter (
				"username",
				""),
			" size=\"32\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Password</th>\n",

			"<td><input",
			" type=\"password\"",
			" name=\"password\"",
			" value=\"\"",
			" size=\"32\"",
			"></td>\n",

			"<td><input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
