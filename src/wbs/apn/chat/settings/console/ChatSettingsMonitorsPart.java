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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		ChatRec chat =
			chatHelper.findFromContextRequired ();

		List <Long> onlineMonitorIds =
			chatUserHelper.searchIds (
				taskLogger,
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.type (
				ChatUserType.monitor)

			.online (
				true)

		);

		taskLogger.debugFormat (
			"Got %s",
			integerToDecimalString (
				onlineMonitorIds.size ()));

		for (
			Long monitorId
				: onlineMonitorIds
		) {

			ChatUserRec monitor =
				chatUserHelper.findRequired (
					monitorId);

			taskLogger.debugFormat (
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

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chat.settings.monitors"));

		// table open

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Orient",
			"Male",
			"Female");

		// gay row

		htmlTableRowOpen ();

		htmlTableCellWrite (
			"Gay");

		htmlTableCellOpen ();

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

		htmlTableCellClose ();

		htmlTableCellOpen ();

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

		htmlTableRowClose ();

		// bi row

		htmlTableRowOpen ();

		htmlTableCellWrite (
			"Bi");

		htmlTableCellOpen ();

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

		htmlTableCellClose ();

		htmlTableCellOpen ();

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

		htmlTableCellClose ();

		htmlTableRowClose ();

		// straight row

		htmlTableRowOpen ();

		htmlTableCellWrite (
			"Straight");

		htmlTableCellOpen ();

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

		htmlTableCellClose ();

		htmlTableCellOpen ();

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

		htmlTableCellClose ();

		htmlTableRowClose ();

		// table close

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
