package wbs.apn.chat.user.image.api;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.api.mvc.ApiAction;

@PrototypeComponent ("chatUserImageUploadViewAction")
public
class ChatUserImageUploadViewAction
	extends ApiAction {

	// dependencies

	@Inject
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@Inject
	Database database;

	@Inject
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserImageUploadTokenRec imageUploadToken =
			chatUserImageUploadTokenHelper.findByToken (
				(String)
				requestContext.request (
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

			Provider<Responder> responderProvider =
				responder (
					"chatUserImageUploadExpiredPage");

			return responderProvider.get ();

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

			Provider<Responder> responderProvider =
				responder (
					"chatUserImageUploadFormPage");

			return responderProvider.get ();

		}

	}

}
