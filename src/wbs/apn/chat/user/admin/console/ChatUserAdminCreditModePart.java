package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatUserAdminCreditModePart")
public
class ChatUserAdminCreditModePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserCreditModeConsoleHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (
				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.monitor)
			) {

				htmlParagraphOpen ();

				formatWriter.writeFormat (
					"This is a monitor and has no credit mode.");

				htmlParagraphClose ();

				return;

			}

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chatUser.admin.creditMode"));

			htmlTableOpenDetails ();

			htmlTableDetailsRowWriteHtml (
				"Credit mode",
				() -> chatUserCreditModeConsoleHelper.writeSelect (
					"creditMode",
					requestContext.formOrElse (
						"creditMode",
						() -> chatUser.getCreditMode ().name ())));

			htmlTableDetailsRowWriteHtml (
				"Actions",
				stringFormat (
					"<input",
					" type=\"submit\"",
					" value=\"change mode\"",
					">"));

			htmlTableClose ();

			htmlFormClose ();

		}

	}

}
