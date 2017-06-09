package wbs.apn.chat.settings.console;

import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatSettingsMonitorsPart")
public
class ChatSettingsMonitorsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	private
	long gayMale;

	private
	long gayFemale;

	private
	long biMale;

	private
	long biFemale;

	private
	long straightMale;

	private
	long straightFemale;

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

			ChatRec chat =
				chatHelper.findFromContextRequired (
					transaction);

			List <Long> onlineMonitorIds =
				chatUserHelper.searchIds (
					transaction,
					new ChatUserSearch ()

				.chatId (
					chat.getId ())

				.type (
					ChatUserType.monitor)

				.online (
					true)

			);

			transaction.debugFormat (
				"Got %s",
				integerToDecimalString (
					onlineMonitorIds.size ()));

			for (
				Long monitorId
					: onlineMonitorIds
			) {

				ChatUserRec monitor =
					chatUserHelper.findRequired (
						transaction,
						monitorId);

				transaction.debugFormat (
					"Orient %s, gender %s",
					enumName (
						monitor.getOrient ()),
					enumName (
						monitor.getGender ()));

				switch (monitor.getOrient ()) {

				case gay:

					switch (monitor.getGender ()) {

					case male:
						gayMale ++;
						continue;

					case female:
						gayFemale++;
						continue;

					}

					throw new RuntimeException ();

				case bi:

					switch (monitor.getGender ()) {

					case male:
						biMale ++;
						continue;

					case female:
						biFemale ++;
						continue;

					}

					throw new RuntimeException ();

				case straight:

					switch (monitor.getGender ()) {

					case male:
						straightMale ++;
						continue;

					case female:
						straightFemale ++;
						continue;

					}

					throw new RuntimeException ();

				}

				throw new RuntimeException ();

			}

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

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chat.settings.monitors"));

			// table open

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Orient",
				"Male",
				"Female");

			// gay row

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellWrite (
				formatWriter,
				"Gay");

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"gayMale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"gayMale",
					() -> integerToDecimalString (
						gayMale)),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"gayFemale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"gayFemale",
					() -> integerToDecimalString (
						gayFemale)),
				">");

			htmlTableRowClose (
				formatWriter);

			// bi row

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellWrite (
				formatWriter,
				"Bi");

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"biMale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"biMale",
					() -> integerToDecimalString (
						biMale)),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"biFemale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"biFemale",
					() -> integerToDecimalString (
						biFemale)),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			// straight row

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellWrite (
				formatWriter,
				"Straight");

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"straightMale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"straightMale",
					() -> integerToDecimalString (
						straightMale)),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"straightFemale\"",
				" size=\"6\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"straightFemale",
					() -> integerToDecimalString (
						straightFemale)),
				">");

			htmlTableCellClose (
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			// table close

			htmlTableClose (
				formatWriter);

			// form controls

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}
