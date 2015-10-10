package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.isEmpty;

import java.util.Set;

import javax.inject.Inject;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.JqueryScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.record.GlobalId;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("coreLogonResponder")
public
class CoreLogonResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

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
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/login.js"))

			.build ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>%h</h1>\n",
			wbsConfig.consoleTitle ());

		goTestUsers ();

		goLoginForm ();

	}

	void goTestUsers () {

		if (isEmpty (wbsConfig.testUsers ()))
			return;

		printFormat (
			"<h2>Quick login</h2>\n");

		printFormat (
			"<p>Login shortcuts for development mode only</p>\n");

		printFormat (
			"<p class=\"login-buttons\">\n");

		for (
			String username
				: wbsConfig.testUsers ()
		) {

			String[] usernameParts =
				username.split ("\\.");

			if (usernameParts.length != 2)
				continue;

			String sliceCode =
				usernameParts [0];

			String userCode =
				usernameParts [1];

			UserRec user =
				userHelper.findByCode (
					GlobalId.root,
					sliceCode,
					userCode);

			if (user == null)
				continue;

			printFormat (
				"<button",
				" class=\"login-button\"",
				" data-slice-code=\"%h\"",
				sliceCode,
				" data-user-code=\"%h\"",
				userCode,
				" disabled>%h</button>\n",
				username);

		}

		printFormat (
			"</p>\n");

	}

	void goLoginForm () {

		printFormat (
			"<h2>Please log in</h2>\n");

		requestContext.flushNotices ();

		printFormat (
			"<form",
			" id=\"login-form\"",
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
			" class=\"slice-input\"",
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
			" class=\"username-input\"",
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
			" class=\"password-input\"",
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
