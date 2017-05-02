package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatAffiliateUsersPart")
public
class ChatAffiliateUsersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	List <ChatUserRec> chatUsers;

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

			ChatAffiliateRec chatAffiliate =
				chatAffiliateHelper.findFromContextRequired (
					transaction);

			chatUsers =
				chatUserHelper.find (
					transaction,
					chatAffiliate);

			Collections.sort (
				chatUsers);

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

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"User",
				"Name",
				"Info",
				"Online");

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					chatUser.getCode ());

				htmlTableCellWrite (
					chatUser.getName ());

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						chatUser.getInfoText (),
						() -> chatUser.getInfoText ().getText ()));

				htmlTableCellWrite (
					booleanToYesNo (
						chatUser.getOnline ()));

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

}
