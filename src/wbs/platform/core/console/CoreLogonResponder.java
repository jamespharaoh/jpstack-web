package wbs.platform.core.console;

import static wbs.framework.utils.etc.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.framework.utils.etc.CollectionUtils.listItemAtIndexRequired;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.stringSplitFullStop;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.entity.record.GlobalId;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("coreLogonResponder")
public
class CoreLogonResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	SliceConsoleHelper sliceHelper;

	@Inject
	UserConsoleHelper userHelper;

	@Inject
	WbsConfig wbsConfig;

	// state

	Optional <SliceRec> slice;

	// details

	@Override
	public
	String getTitle () {

		return wbsConfig.consoleTitle ();

	}

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/login.js"))

			.build ();

	}

	// implementation

	@Override
	protected
	void prepare () {

		Optional <String> sliceCode =
			requestContext.header (
				"x-wbs-slice");

		if (
			optionalIsPresent (
				sliceCode)
		) {

			slice =
				Optional.of (
					sliceHelper.findByCodeRequired (
						GlobalId.root,
						sliceCode.get ()));

		} else {

			slice =
				Optional.absent ();

		}

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

		if (
			isEmpty (
				wbsConfig.testUsers ())
		) {
			return;
		}

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

			List <String> usernameParts =
				stringSplitFullStop (
					username);

			if (
				collectionDoesNotHaveTwoElements (
					usernameParts)
			) {
				continue;
			}

			String sliceCode =
				listItemAtIndexRequired (
					usernameParts,
					0l);

			String userCode =
				listItemAtIndexRequired (
					usernameParts,
					1l);

			Optional <UserRec> userOptional =
				userHelper.findByCode (
					GlobalId.root,
					sliceCode,
					userCode);

			if (
				optionalIsNotPresent (
					userOptional)
			) {
				continue;
			}

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

		if (
			optionalIsPresent (
				slice)
		) {

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"slice\"",
				" value=\"%h\"",
				slice.get ().getCode (),
				">\n");

		}

		printFormat (
			"<table class=\"details\">\n");

		if (
			optionalIsNotPresent (
				slice)
		) {

			printFormat (
				"<tr>\n",

				"<th>Slice</th>\n",

				"<td><input",
				" class=\"slice-input\"",
				" type=\"text\"",
				" name=\"slice\"",
				" value=\"%h\"",
				requestContext.parameterOrDefault (
					"slice",
					wbsConfig.defaultSlice ()),
				" size=\"32\"",
				"></td>\n",

				"</tr>\n");

		}

		printFormat (
			"<tr>\n",

			"<th>Username</th>\n",

			"<td><input",
			" class=\"username-input\"",
			" type=\"text\"",
			" name=\"username\"",
			" value=\"%h\"",
			requestContext.parameterOrDefault (
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
