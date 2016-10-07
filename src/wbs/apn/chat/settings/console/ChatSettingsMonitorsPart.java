package wbs.apn.chat.settings.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.extern.log4j.Log4j;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@PrototypeComponent ("chatSettingsMonitorsPart")
public
class ChatSettingsMonitorsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

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
	void prepare () {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		List<Long> onlineMonitorIds =
			chatUserHelper.searchIds (
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.type (
				ChatUserType.monitor)

			.online (
				true)

		);

		log.debug ("Got " + onlineMonitorIds.size ());

		for (
			Long monitorId
				: onlineMonitorIds
		) {

			ChatUserRec monitor =
				chatUserHelper.findRequired (
					monitorId);

			log.debug (
				stringFormat (
					"Orient %s, gender %s",
					monitor.getOrient (),
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
	void renderHtmlBodyContent () {

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
