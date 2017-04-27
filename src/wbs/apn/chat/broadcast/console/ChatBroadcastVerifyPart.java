package wbs.apn.chat.broadcast.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalCast;
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

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	ChatUserRec fromUser;

	FormFieldSet <ChatBroadcastSendForm> searchFields;
	FormFieldSet <ChatBroadcastSendForm> numbersFields;
	FormFieldSet <ChatBroadcastSendForm> commonFields;
	FormFieldSet <ChatBroadcastSendForm> messageUserFields;
	FormFieldSet <ChatBroadcastSendForm> messageMessageFields;

	FormFieldSet <ChatUserRec> verifyUserFields;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chat =
			chatHelper.findFromContextRequired ();

		searchFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"send-search",
				ChatBroadcastSendForm.class);

		numbersFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"send-numbers",
				ChatBroadcastSendForm.class);

		commonFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"send-common",
				ChatBroadcastSendForm.class);

		messageUserFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"send-message-user",
				ChatBroadcastSendForm.class);

		messageMessageFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"send-message-message",
				ChatBroadcastSendForm.class);

		verifyUserFields =
			chatBroadcastConsoleModule.formFieldSetRequired (
				"verify-user",
				ChatUserRec.class);

		form =
			(ChatBroadcastSendForm)
			requestContext.requestRequired (
				"chatBroadcastForm");

		formHints =
			ImmutableMap.<String, Object> builder ()

			.put (
				"chat",
				chatHelper.findFromContextRequired ())

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			// form

			htmlFormOpenPost ();

			// always hidden

			formFieldLogic.outputFormAlwaysHidden (
				taskLogger,
				requestContext,
				formatWriter,
				searchFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormAlwaysHidden (
				taskLogger,
				requestContext,
				formatWriter,
				numbersFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormAlwaysHidden (
				taskLogger,
				requestContext,
				formatWriter,
				commonFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormAlwaysHidden (
				taskLogger,
				requestContext,
				formatWriter,
				messageUserFields,
				updateResults,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormAlwaysHidden (
				taskLogger,
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
				taskLogger,
				requestContext,
				formatWriter,
				searchFields,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormTemporarilyHidden (
				taskLogger,
				requestContext,
				formatWriter,
				numbersFields,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormTemporarilyHidden (
				taskLogger,
				requestContext,
				formatWriter,
				commonFields,
				form,
				formHints,
				FormType.search,
				"send");

			formFieldLogic.outputFormTemporarilyHidden (
				taskLogger,
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
				taskLogger,
				requestContext,
				formatWriter,
				messageMessageFields,
				optionalAbsent (),
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

			List <Long> chatUserIds =
				genericCastUnchecked (
					requestContext.requestRequired (
						"chatBroadcastChatUserIds"));

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"%s recipients in total.",
				integerToDecimalString (
					chatUserIds.size ()));

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
				taskLogger,
				formatWriter,
				verifyUserFields,
				chatUsers,
				emptyMap (),
				false);

		}

	}

}
