package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWriteSelected;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

@PrototypeComponent ("chatUserAdminPrefsPart")
public
class ChatUserAdminPrefsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	// state

	ChatUserRec chatUser;

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		if (

			isNull (
				chatUser)

			|| isNull (
				chatUser.getGender ())

			|| isNull (
				chatUser.getOrient ())

		) {

			requestContext.addError (
				"Cannot change prefs for this user");

			requestContext.flushNotices ();

			return;

		}

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.prefs"));

		// table open

		htmlTableOpenDetails ();

		// table contents

		htmlTableDetailsRowWrite (
			"Code",
			chatUser.getCode ());

		htmlTableDetailsRowWriteHtml (
			"Gender",
			() -> {

			htmlSelectOpen (
				"gender");

			if (chatUser.getGender () == Gender.male) {

				htmlOptionWrite (
					"male",
					true,
					"male");

				htmlOptionWrite (
					"female",
					false,
					"female");

			} else if (chatUser.getGender () == Gender.female) {

				htmlOptionWrite (
					"male",
					false,
					"male");

				htmlOptionWrite (
					"female",
					true,
					"female");

			} else if (chatUser.getGender () == null) {

				htmlOptionWrite (
					"male",
					false,
					"male");

				htmlOptionWrite (
					"female",
					false,
					"female");

			} else {

				shouldNeverHappen ();

			}

			htmlSelectClose ();

		});

		htmlTableDetailsRowWriteHtml (
			"Orient",
			() -> {

			htmlSelectOpen (
				"orient");

			if (chatUser.getOrient () == Orient.gay) {

				htmlOptionWriteSelected (
					"gay");

				htmlOptionWrite (
					"bi");

				htmlOptionWrite (
					"straight");

			} else if (chatUser.getOrient () == Orient.bi) {

				htmlOptionWrite (
					"gay");

				htmlOptionWriteSelected (
					"bi");

				htmlOptionWrite (
					"straight");

			} else if (chatUser.getOrient () == Orient.straight) {

				htmlOptionWrite (
					"gay");

				htmlOptionWrite (
					"bi");

				htmlOptionWriteSelected (
					"straight");

			} else {

				htmlOptionWrite (
					"—");

				htmlOptionWrite (
					"gay");

				htmlOptionWrite (
					"bi");

				htmlOptionWrite (
					"straight");

			}

			htmlSelectClose ();

		});

		// table close

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" value=\"update prefs\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
