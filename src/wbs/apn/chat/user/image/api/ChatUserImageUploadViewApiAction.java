package wbs.apn.chat.user.image.api;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserImageUploadViewApiAction")
public
class ChatUserImageUploadViewApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatUserImageUploadExpiredPage> expiredPageProvider;

	@PrototypeDependency
	Provider <ChatUserImageUploadFormPage> formPageProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			ChatUserImageUploadTokenRec imageUploadToken =
				chatUserImageUploadTokenHelper.findByToken (
					transaction,
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

				return optionalOf (
					expiredPageProvider.get ());

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

				return optionalOf (
					formPageProvider.get ());

			}

		}

	}

}
