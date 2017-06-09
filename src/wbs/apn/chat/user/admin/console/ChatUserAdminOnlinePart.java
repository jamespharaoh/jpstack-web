package wbs.apn.chat.user.admin.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;

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
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatUserAdminOnlinePart")
public
class ChatUserAdminOnlinePart
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

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.admin.online"));

			if (chatUser.getOnline ()) {

				htmlParagraphWrite (
					formatWriter,
					"This user is online");

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"offline\"",
					" value=\"take offline\"",
					">");

				htmlParagraphClose (
					formatWriter);

			} else {

				htmlParagraphWrite (
					formatWriter,
					"This user is offline");

				if (
					chatUser.getType () == ChatUserType.user
					&& chatUser.getFirstJoin () == null
				) {

					htmlParagraphWriteFormat (
						formatWriter,
						"This user has never been online before, please don't ",
						"bring them online unless you are sure!");

				}

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"online\"",
					" value=\"bring online\"",
					">");

				htmlParagraphClose (
					formatWriter);

			}

			htmlFormClose (
				formatWriter);

		}

	}

}
