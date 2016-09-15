package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.booleanToYesNo;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
	void prepare () {

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
	void renderHtmlBodyContent () {

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
