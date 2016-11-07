package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWrite;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWriteSelected;
import static wbs.utils.web.HtmlInputUtils.htmlSelectClose;
import static wbs.utils.web.HtmlInputUtils.htmlSelectOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));
	}

	@Override
	public
	void renderHtmlBodyContent () {

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
