package wbs.apn.chat.broadcast.console;

import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.etc.OptionalUtils;

@PrototypeComponent ("chatBroadcastSendPart")
public
class ChatBroadcastSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ConsoleModule chatBroadcastConsoleModule;

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// state

	FormFieldSet <ChatBroadcastSendForm> searchFields;
	FormFieldSet <ChatBroadcastSendForm> numbersFields;
	FormFieldSet <ChatBroadcastSendForm> commonFields;
	FormFieldSet <ChatBroadcastSendForm> messageUserFields;
	FormFieldSet <ChatBroadcastSendForm> messageMessageFields;

	ChatBroadcastSendForm form;
	Optional <UpdateResultSet> updateResults;
	Map <String, Object> formHints;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		searchFields =
			chatBroadcastConsoleModule.formFieldSet (
				"send-search",
				ChatBroadcastSendForm.class);

		numbersFields =
			chatBroadcastConsoleModule.formFieldSet (
				"send-numbers",
				ChatBroadcastSendForm.class);

		commonFields =
			chatBroadcastConsoleModule.formFieldSet (
				"send-common",
				ChatBroadcastSendForm.class);

		messageUserFields =
			chatBroadcastConsoleModule.formFieldSet (
				"send-message-user",
				ChatBroadcastSendForm.class);

		messageMessageFields =
			chatBroadcastConsoleModule.formFieldSet (
				"send-message-message",
				ChatBroadcastSendForm.class);

		form =
			(ChatBroadcastSendForm)
			OptionalUtils.ifNotPresent (
				requestContext.request (
					"chatBroadcastForm"),
				Optional.<ChatBroadcastSendForm>of (
					new ChatBroadcastSendForm ()));

		updateResults =
			OptionalUtils.optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"chatBroadcastUpdates"));

		formHints =
			ImmutableMap.<String,Object>builder ()

			.put (
				"chat",
				chatHelper.findRequired (
					requestContext.stuffInteger (
						"chatId")))

			.build ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPost ();

		htmlHeadingThreeWrite (
			"Recipients");

		formFieldLogic.outputFormAlwaysHidden (
			requestContext,
			formatWriter,
			searchFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormAlwaysHidden (
			requestContext,
			formatWriter,
			numbersFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormAlwaysHidden (
			requestContext,
			formatWriter,
			commonFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormAlwaysHidden (
			requestContext,
			formatWriter,
			messageUserFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormAlwaysHidden (
			requestContext,
			formatWriter,
			messageMessageFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		if (! form.search ()) {

			htmlTableOpenDetails ();

			formFieldLogic.outputFormRows (
				requestContext,
				formatWriter,
				numbersFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormRows (
				requestContext,
				formatWriter,
				commonFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			htmlTableClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"searchOn\"",
				" value=\"enable search\"",
				">");

			htmlParagraphClose ();

			formFieldLogic.outputFormTemporarilyHidden (
				requestContext,
				formatWriter,
				searchFields,
				form,
				formHints,
				FormType.search,
				"send");

		}

		if (form.search ()) {

			htmlTableOpenDetails ();

			formFieldLogic.outputFormRows (
				requestContext,
				formatWriter,
				searchFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormRows (
				requestContext,
				formatWriter,
				commonFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			htmlTableClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"searchOff\"",
				" value=\"disable search\"",
				">");

			formFieldLogic.outputFormTemporarilyHidden (
				requestContext,
				formatWriter,
				numbersFields,
				form,
				formHints,
				FormType.search,
				"send");

		}

		htmlHeadingThreeWrite (
			"Message");

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			messageUserFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"verify\"",
			" value=\"verify\"",
			">");

		htmlParagraphClose ();

		formFieldLogic.outputFormTemporarilyHidden (
			requestContext,
			formatWriter,
			messageMessageFields,
			form,
			formHints,
			FormType.search,
			"send");

		htmlFormClose ();

	}

}
