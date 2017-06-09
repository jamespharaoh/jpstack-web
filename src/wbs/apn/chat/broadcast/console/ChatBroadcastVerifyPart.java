package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.forms.core.ConsoleMultiForm;
import wbs.console.forms.core.ConsoleMultiFormType;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatBroadcastVerifyPart")
public
class ChatBroadcastVerifyPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	ConsoleMultiFormType <ChatBroadcastSendForm> chatBroadcastSendFormType;

	@SingletonDependency
	@NamedDependency
	ConsoleFormType <ChatUserRec> chatBroadcastVerifyFormType;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	ChatUserRec fromUser;

	ConsoleMultiForm <ChatBroadcastSendForm> sendForm;
	ConsoleForm <ChatUserRec> verifyForm;

	List <ChatUserRec> recipients;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chat =
				chatHelper.findFromContextRequired (
					transaction);

			Map <String, Object> formHints =
				ImmutableMap.<String, Object> builder ()

				.put (
					"chat",
					chatHelper.findFromContextRequired (
						transaction))

				.build ();

			ChatBroadcastSendForm sendFormValue =
				dynamicCastRequired (
					ChatBroadcastSendForm.class,
					requestContext.requestRequired (
						"chat-broadcast-send-form"));

			sendForm =
				chatBroadcastSendFormType.buildResponse (
					transaction,
					formHints,
					sendFormValue);

			fromUser =
				chatUserHelper.findByCodeRequired (
					transaction,
					chat,
					sendForm.value ().fromUser ());

			List <Long> chatUserIds =
				genericCastUnchecked (
					requestContext.requestRequired (
						"chat-broadcast-send-results"));

			recipients =
				chatUserHelper.findManyRequired (
					transaction,
					chatUserIds);

			verifyForm =
				chatBroadcastVerifyFormType.buildResponse (
					transaction,
					formHints,
					recipients);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// form

			htmlFormOpenPost (
				formatWriter);

			// always hidden

			sendForm.outputFormAlwaysHidden (
				transaction,
				formatWriter,
				"search",
				"numbers",
				"common",
				"message-user",
				"message-message");

			// temporarily hidden

			sendForm.outputFormTemporarilyHidden (
				transaction,
				formatWriter,
				"search",
				"numbers",
				"common",
				"message-user");

			// message info

			htmlHeadingThreeWrite (
				formatWriter,
				"Message");

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"From user code",
				fromUser.getCode ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"From user name",
				fromUser.getName ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"From user info",
				ifNotNullThenElseEmDash (
					fromUser.getInfoText (),
					() -> fromUser.getInfoText ().getText ()));

			sendForm.outputFormRows (
				transaction,
				formatWriter,
				"message-message");

			htmlTableClose (
				formatWriter);

			// form controls

			htmlParagraphOpen (
				formatWriter);

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

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

			// recipients info

			htmlHeadingThreeWrite (
				formatWriter,
				"Recipients");

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"%s recipients in total.",
				integerToDecimalString (
					recipients.size ()));

			if (sendForm.value ().search ()) {

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"The actual number of recipients may change slightly on ",
					"send as the search will be performed again.");

				htmlParagraphClose (
					formatWriter);

			}

			verifyForm.outputListTable (
				transaction,
				formatWriter,
				false);

		}

	}

}
