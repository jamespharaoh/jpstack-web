package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
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
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;

@PrototypeComponent ("chatBroadcastSendPart")
public
class ChatBroadcastSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	ConsoleMultiFormType <ChatBroadcastSendForm> chatBroadcastSendFormType;

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ConsoleMultiForm <ChatBroadcastSendForm> form;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			Map <String, Object> formHints =
				ImmutableMap.<String, Object> builder ()

				.put (
					"chat",
					chatHelper.findFromContextRequired (
						transaction))

				.build ()

			;

			ChatBroadcastSendForm formValue =
				dynamicCastRequired (
					ChatBroadcastSendForm.class,
					optionalOrElseRequired (
						requestContext.request (
							"chat-broadcast-send-form"),
						() -> new ChatBroadcastSendForm ()));

			form =
				chatBroadcastSendFormType.buildResponse (
					transaction,
					formHints,
					formValue);

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

			htmlFormOpenPost (
				formatWriter);

			htmlHeadingThreeWrite (
				formatWriter,
				"Recipients");

			form.outputFormAlwaysHidden (
				transaction,
				formatWriter,
				"search",
				"numbers",
				"common",
				"message-user",
				"message-message");

			if (! form.value ().search ()) {

				htmlTableOpenDetails (
					formatWriter);

				form.outputFormRows (
					transaction,
					formatWriter,
					"numbers",
					"common");

				htmlTableClose (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"searchOn\"",
					" value=\"enable search\"",
					">");

				htmlParagraphClose (
					formatWriter);

				form.outputFormTemporarilyHidden (
					transaction,
					formatWriter,
					"search");

			}

			if (form.value ().search ()) {

				htmlTableOpenDetails (
					formatWriter);

				form.outputFormRows (
					transaction,
					formatWriter,
					"search",
					"common");

				htmlTableClose (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"searchOff\"",
					" value=\"disable search\"",
					">");

				form.outputFormTemporarilyHidden (
					transaction,
					formatWriter,
					"numbers");

			}

			htmlHeadingThreeWrite (
				formatWriter,
				"Message");

			htmlTableOpenDetails (
				formatWriter);

			form.outputFormRows (
				transaction,
				formatWriter,
				"message-user");

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"verify\"",
				" value=\"verify\"",
				">");

			htmlParagraphClose (
				formatWriter);

			form.outputFormTemporarilyHidden (
				transaction,
				formatWriter,
				"message-message");

			htmlFormClose (
				formatWriter);

		}

	}

}
