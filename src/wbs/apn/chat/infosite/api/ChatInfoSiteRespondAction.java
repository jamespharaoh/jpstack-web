package wbs.apn.chat.infosite.api;

import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.api.ApiAction;

@PrototypeComponent ("chatInfoSiteRespondAction")
public
class ChatInfoSiteRespondAction
	extends ApiAction {

	@Inject
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	RequestContext requestContext;

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatInfoSiteRec infoSite =
			chatInfoSiteHelper.find (
				requestContext.requestInt ("chatInfoSiteId"));

		if (! equal (
				infoSite.getToken (),
				requestContext.request ("chatInfoSiteToken"))) {

			throw new RuntimeException ("Token mismatch");

		}

		ChatUserRec otherUser =
			chatUserHelper.find (
				requestContext.parameterInt ("otherUserId"));

		chatMessageLogic.chatMessageSendFromUser (
			infoSite.getChatUser (),
			otherUser,
			requestContext.parameter ("text"),
			null,
			ChatMessageMethod.infoSite,
			null);

		transaction.commit ();

		return responder ("chatInfoSiteMessageSentResponder")
			.get ();

	}

}
