package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.isNull;
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
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatUserRec chatUser;

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

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

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

				requestContext.flushNotices (
					formatWriter);

				return;

			}

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.admin.prefs"));

			// table open

			htmlTableOpenDetails (
				formatWriter);

			// table contents

			htmlTableDetailsRowWrite (
				formatWriter,
				"Code",
				chatUser.getCode ());

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Gender",
				() -> {

				htmlSelectOpen (
					formatWriter,
					"gender");

				if (chatUser.getGender () == Gender.male) {

					htmlOptionWrite (
						formatWriter,
						"male",
						true,
						"male");

					htmlOptionWrite (
						formatWriter,
						"female",
						false,
						"female");

				} else if (chatUser.getGender () == Gender.female) {

					htmlOptionWrite (
						formatWriter,
						"male",
						false,
						"male");

					htmlOptionWrite (
						formatWriter,
						"female",
						true,
						"female");

				} else if (chatUser.getGender () == null) {

					htmlOptionWrite (
						formatWriter,
						"male",
						false,
						"male");

					htmlOptionWrite (
						formatWriter,
						"female",
						false,
						"female");

				} else {

					shouldNeverHappen ();

				}

				htmlSelectClose (
					formatWriter);

			});

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Orient",
				() -> {

				htmlSelectOpen (
					formatWriter,
					"orient");

				if (chatUser.getOrient () == Orient.gay) {

					htmlOptionWriteSelected (
						formatWriter,
						"gay");

					htmlOptionWrite (
						formatWriter,
						"bi");

					htmlOptionWrite (
						formatWriter,
						"straight");

				} else if (chatUser.getOrient () == Orient.bi) {

					htmlOptionWrite (
						formatWriter,
						"gay");

					htmlOptionWriteSelected (
						formatWriter,
						"bi");

					htmlOptionWrite (
						formatWriter,
						"straight");

				} else if (chatUser.getOrient () == Orient.straight) {

					htmlOptionWrite (
						formatWriter,
						"gay");

					htmlOptionWrite (
						formatWriter,
						"bi");

					htmlOptionWriteSelected (
						formatWriter,
						"straight");

				} else {

					htmlOptionWrite (
						formatWriter,
						"—");

					htmlOptionWrite (
						formatWriter,
						"gay");

					htmlOptionWrite (
						formatWriter,
						"bi");

					htmlOptionWrite (
						formatWriter,
						"straight");

				}

				htmlSelectClose (
					formatWriter);

			});

			// table close

			htmlTableClose (
				formatWriter);

			// form controls

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" value=\"update prefs\"",
				">");

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}
