package wbs.apn.chat.user.image.api;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserImageUploadViewAction")
public
class ChatUserImageUploadViewAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goApi");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatUserImageUploadViewAction.goApi ()",
					this);

		) {

			ChatUserImageUploadTokenRec imageUploadToken =
				chatUserImageUploadTokenHelper.findByToken (
					requestContext.requestStringRequired (
						"chatUserImageUploadToken"));

			// check the expiry time

			boolean expired =
				transaction.now ().isAfter (
					imageUploadToken.getExpiryTime ());

			if (expired) {

				// update token

				imageUploadToken

					.setFirstExpiredTime (
						imageUploadToken.getFirstExpiredTime () != null
							? imageUploadToken.getFirstExpiredTime ()
							: transaction.now ())

					.setLastExpiredTime (
						transaction.now ())

					.setNumExpired (
						imageUploadToken.getNumExpired () + 1);

				// commit and show expiry page

				transaction.commit ();

				return responder (
					taskLogger,
					"chatUserImageUploadExpiredPage");

			} else {

				imageUploadToken

					.setFirstViewTime (
						imageUploadToken.getFirstViewTime () != null
							? imageUploadToken.getFirstViewTime ()
							: transaction.now ())

					.setLastViewTime (
						transaction.now ())

					.setNumViews (
						imageUploadToken.getNumViews () + 1);

				// commit and show form page

				transaction.commit ();

				return responder (
					taskLogger,
					"chatUserImageUploadFormPage");

			}

		}

	}

}
