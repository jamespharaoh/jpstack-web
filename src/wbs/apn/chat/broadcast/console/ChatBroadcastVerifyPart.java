package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

			sendForm =
				chatBroadcastSendFormType.build (
					transaction,
					formHints);

			fromUser =
				chatUserHelper.findByCodeRequired (
					transaction,
					chat,
					sendForm.value ().fromUser ());

			List <Long> chatUserIds =
				genericCastUnchecked (
					requestContext.requestRequired (
						"chatBroadcastChatUserIds"));

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// form

			htmlFormOpenPost ();

			// always hidden

			sendForm.outputFormAlwaysHidden (
				transaction,
				"search",
				"numbers",
				"common",
				"mesage-user",
				"message-message");

			// temporarily hidden

			sendForm.outputFormTemporarilyHidden (
				transaction,
				"search",
				"numbers",
				"common",
				"message-user");

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

			sendForm.outputFormRows (
				transaction,
				"message-message");

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

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"%s recipients in total.",
				integerToDecimalString (
					recipients.size ()));

			if (sendForm.value ().search ()) {

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"The actual number of recipients may change slightly on ",
					"send as the search will be performed again.");

				htmlParagraphClose ();

			}

			verifyForm.outputListTable (
				transaction,
				false);

		}

	}

}
