package wbs.apn.chat.user.image.api;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.FileOutputStream;
import java.util.List;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

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
					"ChatUserImageUploadPostAction.goApi ()",
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

			}

			try {

				// update the image

				List <FileItem> fileItems =
					requestContext.fileItems ();

				if (
					collectionDoesNotHaveOneElement (
						fileItems)
				) {

					throw new RuntimeException (
						stringFormat (
							"Wrong number of file items: %s",
							integerToDecimalString (
								fileItems.size ())));

				}

				FileItem fileItem =
					listFirstElementRequired (
						fileItems);

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

				if (taskLogger.debugEnabled ()) {

					try {

						String filename =
							stringFormat (
								"/tmp/%s",
								randomLogic.generateLowercase (10));

						IOUtils.write (
							fileItem.get (),
							new FileOutputStream (
								filename));

						taskLogger.debugFormat (
							"Written %s bytes to temporary file %s",
							integerToDecimalString (
								fileItem.get ().length),
							filename);

					} catch (Exception exception) {

						taskLogger.debugFormatException (
							exception,
							"Error writing image data to debug file");

					}

				}

				chatUserLogic.setImage (
					taskLogger,
					imageUploadToken.getChatUser (),
					ChatUserImageType.image,
					fileItem.get (),
					fileItem.getName (),
					/*fileItem.getContentType (),*/
					"image/jpeg",
					optionalAbsent (),
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

				return responder (
					taskLogger,
					"chatUserImageUploadSuccessPage");

			} catch (Exception exception) {

				// log exception

				exceptionLogger.logThrowable (
					taskLogger,
					"webapi",
					requestContext.requestPath (),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.ignoreWithUserWarning);

				// start new transaction

				try (

					Transaction errorTransaction =
						database.beginReadWrite (
							taskLogger,
							"ChatUserImageUploadPostAction.goApi ()",
							this);

				) {

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

					return responder (
						taskLogger,
						"chatUserImageUploadErrorPage");

				}

			}

		}

	}

}
