package wbs.apn.chat.broadcast.console;

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
import wbs.console.forms.context.MultiFormContextBuilder;
import wbs.console.forms.context.MultiFormContexts;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;

@PrototypeComponent ("chatBroadcastSendPart")
public
class ChatBroadcastSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	MultiFormContextBuilder <ChatBroadcastSendForm>
		chatBroadcastSendFormContextsBuilder;

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	MultiFormContexts <ChatBroadcastSendForm> formContext;

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

			formContext =
				chatBroadcastSendFormContextsBuilder.build (
					transaction,
					formHints);

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

			htmlFormOpenPost ();

			htmlHeadingThreeWrite (
				"Recipients");

			formContext.outputFormAlwaysHidden (
				transaction,
				"search",
				"numbers",
				"common",
				"message-user",
				"message-message");

			if (! formContext.object ().search ()) {

				htmlTableOpenDetails ();

				formContext.outputFormRows (
					transaction,
					"numbers",
					"common");

				htmlTableClose ();

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"searchOn\"",
					" value=\"enable search\"",
					">");

				htmlParagraphClose ();

				formContext.outputFormTemporarilyHidden (
					transaction,
					"search");

			}

			if (formContext.object ().search ()) {

				htmlTableOpenDetails ();

				formContext.outputFormRows (
					transaction,
					"search",
					"common");

				htmlTableClose ();

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"searchOff\"",
					" value=\"disable search\"",
					">");

				formContext.outputFormTemporarilyHidden (
					transaction,
					"numbers");

			}

			htmlHeadingThreeWrite (
				"Message");

			htmlTableOpenDetails ();

			formContext.outputFormRows (
				transaction,
				"message-user");

			htmlTableClose ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"verify\"",
				" value=\"verify\"",
				">");

			htmlParagraphClose ();

			formContext.outputFormTemporarilyHidden (
				transaction,
				"message-message");

			htmlFormClose ();

		}

	}

}
