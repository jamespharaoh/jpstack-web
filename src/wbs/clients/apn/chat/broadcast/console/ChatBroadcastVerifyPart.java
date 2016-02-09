package wbs.clients.apn.chat.broadcast.console;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.core.console.ChatConsoleHelper;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;

@PrototypeComponent ("chatBroadcastVerifyPart")
public
class ChatBroadcastVerifyPart
	extends AbstractPagePart {

	// dependencies

	@Inject @Named
	ConsoleModule chatBroadcastConsoleModule;

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	ChatUserRec fromUser;

	FormFieldSet searchFields;
	FormFieldSet numbersFields;
	FormFieldSet messageUserFields;
	FormFieldSet messageMessageFields;
	FormFieldSet verifyUserFields;

	ChatBroadcastSendForm form;
	Map<String,Object> formHints;
	Optional<UpdateResultSet> updateResults;

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

		chat =
			chatHelper.find (
				requestContext.stuffInt (
					"chatId"));

		searchFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-search");

		numbersFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-numbers");

		messageUserFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-message-user");

		messageMessageFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"send-message-message");

		verifyUserFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"verify-user");

		form =
			(ChatBroadcastSendForm)
			requestContext.request (
				"chatBroadcastForm");

		formHints =
			ImmutableMap.<String,Object>builder ()

			.put (
				"chat",
				chatHelper.find (
					requestContext.stuffInt (
						"chatId")))

			.build ();

		updateResults =
			Optional.fromNullable (
				(UpdateResultSet)
				requestContext.request (
					"chatBroadcastUpdates"));

		fromUser =
			chatUserHelper.findByCode (
				chat,
				form.fromUser ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// form

		printFormat (
			"<form method=\"post\">\n");

		// always hidden

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

		// temporarily hidden

		formFieldLogic.outputFormTemporarilyHidden (
			requestContext,
			formatWriter,
			searchFields,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormTemporarilyHidden (
			requestContext,
			formatWriter,
			numbersFields,
			form,
			formHints,
			FormType.search,
			"send");

		formFieldLogic.outputFormTemporarilyHidden (
			requestContext,
			formatWriter,
			messageUserFields,
			form,
			formHints,
			FormType.search,
			"send");

		// message info

		printFormat (
			"<h3>Message</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>From user code</th>\n",
			"<td>%h</td>\n",
			fromUser.getCode (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>From user name</th>\n",
			"<td>%h</td>\n",
			fromUser.getName (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>From user info</th>\n",
			"<td>%h</td>\n",
			fromUser.getInfoText () != null
				? fromUser.getInfoText ().getText ()
				: "-",
			"</tr>\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			messageMessageFields,
			Optional.absent (),
			form,
			formHints,
			FormType.search,
			"send");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send\"",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"back\"",
			" value=\"back\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		// recipients info

		printFormat (
			"<h3>Recipients</h3>\n");

		@SuppressWarnings ("unchecked")
		List<Integer> chatUserIds =
			(List<Integer>)
			requestContext.request (
				"chatBroadcastChatUserIds");

		printFormat (
			"<p>%d recipients in total.</p>\n",
			chatUserIds.size ());

		if (form.search ()) {

			printFormat (
				"<p>The actual number of recipients may change slightly on ",
				"send as the search will be performed again.</p>\n");

		}

		List<ChatUserRec> chatUsers =
			chatUserHelper.find (
				chatUserIds.stream ()
					.map (value -> (long) (int) value)
					.collect (Collectors.toList ()));

		formFieldLogic.outputListTable (
			formatWriter,
			verifyUserFields,
			chatUsers,
			false);

	}

}
