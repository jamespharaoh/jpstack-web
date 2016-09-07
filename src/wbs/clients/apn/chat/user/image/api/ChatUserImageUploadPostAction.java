package wbs.clients.apn.chat.user.image.api;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.io.FileOutputStream;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.api.mvc.ApiAction;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.utils.RandomLogic;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.sms.message.core.model.MessageRec;

@Log4j
@PrototypeComponent ("chatUserImageUploadPostAction")
public
class ChatUserImageUploadPostAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserImageUploadPostAction.goApi ()",
				this);

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

			Provider<Responder> responderProvider =
				responder (
					"chatUserImageUploadExpiredPage");

			return responderProvider.get ();

		}

		try {

			// update the image

			List<FileItem> fileItems =
				requestContext.fileItems ();

			if (fileItems.size () != 1) {

				throw new RuntimeException (
					stringFormat (
						"Wrong number of file items: %s",
						fileItems.size ()));

			}

			FileItem fileItem =
				fileItems.get (0);

			if (
				stringNotEqualSafe (
					fileItem.getFieldName (),
					"file")
			) {

				throw new RuntimeException (
					stringFormat (
						"File item has wrong name: %s",
						fileItem.getName ()));

			}

			if (log.isDebugEnabled ()) {

				try {

					String filename =
						stringFormat (
							"/tmp/%s",
							randomLogic.generateLowercase (10));

					IOUtils.write (
						fileItem.get (),
						new FileOutputStream (
							filename));

					log.debug (
						stringFormat (
							"Written %s bytes to temporary file %s",
							fileItem.get ().length,
							filename));

				} catch (Exception exception) {

					log.debug (
						"Error writing image data to debug file",
						exception);

				}

			}

			chatUserLogic.setImage (
				imageUploadToken.getChatUser (),
				ChatUserImageType.image,
				fileItem.get (),
				fileItem.getName (),
				/*fileItem.getContentType (),*/
				"image/jpeg",
				Optional.<MessageRec>absent (),
				false);

			// update token

			imageUploadToken

				.setFirstUploadTime (
					imageUploadToken.getFirstUploadTime () != null
						? imageUploadToken.getFirstUploadTime ()
						: transaction.now ())

				.setLastUploadTime (
					transaction.now ())

				.setNumUploads (
					imageUploadToken.getNumUploads () + 1);

			// commit and show confirmation page

			transaction.commit ();

			Provider<Responder> responderProvider =
				responder (
					"chatUserImageUploadSuccessPage");

			return responderProvider.get ();

		} catch (Exception exception) {

			// log exception

			exceptionLogger.logThrowable (
				"webapi",
				requestContext.requestPath (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.ignoreWithUserWarning);

			// start new transaction

			@Cleanup
			Transaction errorTransaction =
				database.beginReadWrite (
					"ChatUserImageUploadPostAction.goApi ()",
					this);

			// update token

			imageUploadToken

				.setFirstFailedTime (
					imageUploadToken.getFirstFailedTime () != null
						? imageUploadToken.getFirstFailedTime ()
						: transaction.now ())

				.setLastFailedTime (
					transaction.now ())

				.setNumFailures (
					imageUploadToken.getNumFailures () + 1);

			// commit and show error page

			errorTransaction.commit ();

			Provider<Responder> responderProvider =
				responder (
					"chatUserImageUploadErrorPage");

			return responderProvider.get ();

		}

	}

}
