package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
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
import wbs.utils.time.TimeFormatter;

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

		htmlFormOpenPost ();

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

		htmlHeadingThreeWrite (
			"Message");

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"From user code",
			fromUser.getCode ());

		htmlTableDetailsRowWrite (
			"From user name",
			fromUser.getName ());

		htmlTableDetailsRowWrite (
			"From user info",
			ifNotNullThenElseEmDash (
				fromUser.getInfoText (),
				() -> fromUser.getInfoText ().getText ()));

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			messageMessageFields,
			Optional.absent (),
			form,
			formHints,
			FormType.search,
			"send");

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"back\"",
			" value=\"back\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

		// recipients info

		htmlHeadingThreeWrite (
			"Recipients");

		@SuppressWarnings ("unchecked")
		List <Long> chatUserIds =
			(List <Long>)
			requestContext.requestRequired (
				"chatBroadcastChatUserIds");

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"%d recipients in total.",
			chatUserIds.size ());

		if (form.search ()) {

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"The actual number of recipients may change slightly on send ",
				"as the search will be performed again.");

			htmlParagraphClose ();

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
