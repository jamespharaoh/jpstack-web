package wbs.clients.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.OptionalUtils.optionalCast;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.utils.TimeFormatter;

@PrototypeComponent ("chatBroadcastVerifyPart")
public
class ChatBroadcastVerifyPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ConsoleModule chatBroadcastConsoleModule;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	ChatUserRec fromUser;

	FormFieldSet searchFields;
	FormFieldSet numbersFields;
	FormFieldSet commonFields;
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
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

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

		verifyUserFields =
			chatBroadcastConsoleModule.formFieldSets ().get (
				"verify-user");

		form =
			(ChatBroadcastSendForm)
			requestContext.requestRequired (
				"chatBroadcastForm");

		formHints =
			ImmutableMap.<String,Object>builder ()

			.put (
				"chat",
				chatHelper.findRequired (
					requestContext.stuffInteger (
						"chatId")))

			.build ();

		updateResults =
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"chatBroadcastUpdates"));

		fromUser =
			chatUserHelper.findByCodeRequired (
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
			commonFields,
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
		List <Long> chatUserIds =
			(List <Long>)
			requestContext.requestRequired (
				"chatBroadcastChatUserIds");

		printFormat (
			"<p>%d recipients in total.</p>\n",
			chatUserIds.size ());

		if (form.search ()) {

			printFormat (
				"<p>The actual number of recipients may change slightly on ",
				"send as the search will be performed again.</p>\n");

		}

		List <ChatUserRec> chatUsers =
			chatUserHelper.findManyRequired (
				chatUserIds);

		formFieldLogic.outputListTable (
			formatWriter,
			verifyUserFields,
			chatUsers,
			false);

	}

}
