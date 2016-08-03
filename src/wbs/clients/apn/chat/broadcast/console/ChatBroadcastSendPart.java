package wbs.clients.apn.chat.broadcast.console;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.core.console.ChatConsoleHelper;
import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.OptionalUtils;

@PrototypeComponent ("chatBroadcastSendPart")
public
class ChatBroadcastSendPart
	extends AbstractPagePart {

	// dependencies

	@Inject @Named
	ConsoleModule chatBroadcastConsoleModule;

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	FormFieldLogic formFieldLogic;

	// state

	FormFieldSet searchFields;
	FormFieldSet numbersFields;
	FormFieldSet commonFields;
	FormFieldSet messageUserFields;
	FormFieldSet messageMessageFields;

	ChatBroadcastSendForm form;
	Optional<UpdateResultSet> updateResults;
	Map<String,Object> formHints;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

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
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-search");

		numbersFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-numbers");

		commonFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-common");

		messageUserFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-message-user");

		messageMessageFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-message-message");

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
					requestContext.stuffInt (
						"chatId")))

			.build ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<h3>Recipients</h3>\n");

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

			printFormat (
				"<table class=\"details\">\n");

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

			printFormat (
				"</table>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"searchOn\"",
				" value=\"enable search\"",
				"></p>\n");

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

			printFormat (
				"<table class=\"details\">\n");

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

			printFormat (
				"</table>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"searchOff\"",
				" value=\"disable search\"",
				"></p>\n");

			formFieldLogic.outputFormTemporarilyHidden (
				requestContext,
				formatWriter,
				numbersFields,
				form,
				formHints,
				FormType.search,
				"send");

		}

		printFormat (
			"<h3>Message</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			messageUserFields,
			updateResults,
			form,
			formHints,
			FormType.search,
			"send");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"verify\"",
			" value=\"verify\"",
			"></p>\n");

		formFieldLogic.outputFormTemporarilyHidden (
			requestContext,
			formatWriter,
			messageMessageFields,
			form,
			formHints,
			FormType.search,
			"send");

		printFormat (
			"</form>\n");

	}

}
