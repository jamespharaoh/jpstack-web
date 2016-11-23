package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.objectToStringNullSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteFormat;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import javax.inject.Named;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserAdminDatePart")
public
class ChatUserAdminDatePart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserDateModeConsoleHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		renderForm ();

		renderHistory ();

	}

	private
	void renderForm () {

		if (chatUser.getBarred ()) {

			formatWriter.writeLineFormat (
				"<p>This user is barred</p>");

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.date"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Date mode",
			() -> chatUserDateModeConsoleHelper.writeSelect (
				"dateMode",
				requestContext.formOrElse (
					"dateMode",
					() -> objectToStringNullSafe (
						chatUser.getDateMode ()))));

		htmlTableDetailsRowWriteHtml (
			"Radius (miles)",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"radius\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"radius",
					() -> integerToDecimalString (
						chatUser.getDateRadius ())),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Start hour",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"startHour\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"startHour",
					() -> integerToDecimalString (
						chatUser.getDateStartHour ())),
				">"));

		htmlTableDetailsRowWriteHtml (
			"End hour",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"endHour\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"endHour",
					() -> integerToDecimalString (
						chatUser.getDateEndHour ())),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Max profiles per day",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"dailyMax\"",
				" value=\"%h\"",
				requestContext.formOrElse (
					"dailyMax",
					() -> integerToDecimalString (
						chatUser.getDateDailyMax ())),
				">"));

		htmlTableClose ();

		formatWriter.writeLineFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>");

		htmlFormClose ();

	}

	private
	void renderHistory () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			"Source",
			"Mode",
			"Radius",
			"Hours",
			"Number");

		for (
			ChatUserDateLogRec chatUserDateLogRec
				: chatUser.getChatUserDateLogs ()
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatUserDateLogRec.getTimestamp (),
					() -> timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUserDateLogRec.getTimestamp ())));

			if (
				isNotNull (
					chatUserDateLogRec.getUser ())
			) {

				objectManager.writeTdForObjectMiniLink (
					chatUserDateLogRec.getUser ());

			} else if (
				isNotNull (
					chatUserDateLogRec.getMessage ())
			) {

				objectManager.writeTdForObjectMiniLink (
					chatUserDateLogRec.getMessage ());

			} else {

				htmlTableCellWrite (
					"API");

			}

			htmlTableCellWrite (
				chatUserDateLogRec.getDateMode ().name ());

			htmlTableCellWrite (
				integerToDecimalString (
					chatUserDateLogRec.getRadius ()));

			htmlTableCellWriteFormat (
				"%s–%s",
				integerToDecimalString (
					chatUserDateLogRec.getStartHour ()),
				integerToDecimalString (
					chatUserDateLogRec.getEndHour ()));

			htmlTableCellWrite (
				integerToDecimalString (
					chatUserDateLogRec.getDailyMax ()));

		}

		htmlTableClose ();

	}

}
