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

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatAffiliateUsersPart")
public
class ChatAffiliateUsersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	// state

	List <ChatUserRec> chatUsers;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.findRequired (
				requestContext.stuffInteger (
					"chatAffiliateId"));

		chatUsers =
			chatUserHelper.find (
				chatAffiliate);

		Collections.sort (
			chatUsers);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
