package wbs.apn.chat.infosite.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goApi");

		) {

			ChatInfoSiteRec infoSite =
				chatInfoSiteHelper.findRequired (
					transaction,
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
					transaction,
					requestContext.parameterIntegerRequired (
						"otherUserId"));

			chatMessageLogic.chatMessageSendFromUser (
				transaction,
				infoSite.getChatUser (),
				otherUser,
				requestContext.parameterRequired (
					"text"),
				optionalAbsent (),
				ChatMessageMethod.infoSite,
				emptyList ());

			transaction.commit ();

			return responder (
				transaction,
				"chatInfoSiteMessageSentResponder");

		}

	}

}
