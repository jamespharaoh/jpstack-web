package wbs.clients.apn.chat.affiliate.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatAffiliateUsersPart")
public
class ChatAffiliateUsersPart
	extends AbstractPagePart {

	@Inject
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	List<ChatUserRec> chatUsers;

	@Override
	public
	void prepare () {

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.findRequired (
				requestContext.stuffInt (
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

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Name</th>\n",
			"<th>Info</th>\n",
			"<th>Online</th>\n",
			"</tr>\n");

		for (
			ChatUserRec chatUser
				: chatUsers
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%h</td>\n",
				chatUser.getCode ());

			printFormat (
				"<td>%h</td>\n",
				chatUser.getName ());

			printFormat (
				"<td>%h</td>\n",
				chatUser.getInfoText ());

			printFormat (
				"<td>%h</td>\n",
				chatUser.getOnline ()
					? "yes"
					: "no");

			printFormat (
				"</tr>");

		}

		printFormat (
			"</table>\n");

	}

}
