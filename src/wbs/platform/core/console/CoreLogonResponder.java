package wbs.platform.core.console;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.TaskLogger;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("coreLogonResponder")
public
class CoreLogonResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingOneWrite (
			wbsConfig.consoleTitle ());

		goTestUsers ();

		goLoginForm ();

	}

	void goTestUsers () {

		if (
			collectionIsEmpty (
				wbsConfig.testUsers ())
		) {
			return;
		}

		htmlHeadingTwoWrite (
			"Quick login");

		htmlParagraphWrite (
			"Login shortcuts for development mode only");

		htmlParagraphOpen (
			htmlClassAttribute (
				"login-buttons"));

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

			formatWriter.writeLineFormat (
				"<button",
				" class=\"login-button\"",
				" data-slice-code=\"%h\"",
				sliceCode,
				" data-user-code=\"%h\"",
				userCode,
				" disabled>%h</button>",
				username);

		}

		htmlParagraphClose ();

	}

	void goLoginForm () {

		htmlHeadingTwoWrite (
			"Please log in");

		requestContext.flushNotices ();

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrl (
				"/"),
			htmlIdAttribute (
				"login-form"));

		// form hidden

		if (
			optionalIsPresent (
				slice)
		) {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"slice\"",
				" value=\"%h\"",
				slice.get ().getCode (),
				">");

		}

		// table open

		htmlTableOpenDetails ();

		// slice row

		if (
			optionalIsNotPresent (
				slice)
		) {

			htmlTableDetailsRowWriteHtml (
				"Slice",
				() -> formatWriter.writeLineFormat (
					"<input",
					" class=\"slice-input\"",
					" type=\"text\"",
					" name=\"slice\"",
					" value=\"%h\"",
					requestContext.parameterOrDefault (
						"slice",
						wbsConfig.defaultSlice ()),
					" size=\"32\"",
					">"));

		}

		// username row

		htmlTableDetailsRowWriteHtml (
			"Username",
			() -> formatWriter.writeLineFormat (
				"<input",
				" class=\"username-input\"",
				" type=\"text\"",
				" name=\"username\"",
				" value=\"%h\"",
				requestContext.parameterOrDefault (
					"username",
					""),
				" size=\"32\"",
				" disabled",
				">"));

		// password row

		htmlTableDetailsRowWriteHtml (
			"Password",
			() -> formatWriter.writeLineFormat (
				"<input",
				" class=\"password-input\"",
				" type=\"password\"",
				" name=\"password\"",
				" value=\"\"",
				" size=\"32\"",
				" disabled",
				">"));

		// table close

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"log in\"",
			" disabled",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
