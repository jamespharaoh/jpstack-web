package wbs.clients.apn.chat.infosite.api;

import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;

import wbs.api.mvc.ApiAction;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatInfoSiteRespondAction")
public
class ChatInfoSiteRespondAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatInfoSiteRespondAction.goApi ()",
				this);

		ChatInfoSiteRec infoSite =
			chatInfoSiteHelper.findRequired (
				requestContext.requestIntegerRequired (
					"chatInfoSiteId"));

		if (
			stringNotEqualSafe (
				infoSite.getToken (),
				requestContext.requestStringRequired (
					"chatInfoSiteToken"))
		) {

			throw new RuntimeException (
				"Token mismatch");

		}

		ChatUserRec otherUser =
			chatUserHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"otherUserId"));

		chatMessageLogic.chatMessageSendFromUser (
			infoSite.getChatUser (),
			otherUser,
			requestContext.parameterOrNull (
				"text"),
			Optional.absent (),
			ChatMessageMethod.infoSite,
			ImmutableList.of ());

		transaction.commit ();

		return responder ("chatInfoSiteMessageSentResponder")
			.get ();

	}

}
