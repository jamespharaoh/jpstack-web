package wbs.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.rethrow;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.IOUtils;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserImageUploadAction")
public
class ChatUserImageUploadAction
	extends ConsoleAction {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserImageObjectHelper chatUserImageHelper;

	@Inject
	Database database;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserImageUploadResponder");
	}

	@Override
	public
	Responder goReal () {

		try {

			// check this is a multipart upload

			RequestContext fileUploadRequestContext =
				new ServletRequestContext (
					requestContext.request ());

			if (! FileUploadBase.isMultipartContent (
					fileUploadRequestContext)) {

				requestContext.addError (
					"Internal error");

				return null;

			}

			// process the request

			ServletFileUpload fileUpload =
				new ServletFileUpload (new DiskFileItemFactory ());

			List<FileItem> items =
				fileUpload.parseRequest (
					requestContext.request ());

			if (items.size () > 1) {

				requestContext.addError (
					"Multiple files received.");

				return null;

			}

			if (items.size () == 0) {

				requestContext.addError (
					"No file received.");

				return null;

			}

			FileItem item =
				items.get (0);

			String contentType =
				item.getContentType ();

			byte[] data =
				IOUtils.toByteArray (
					item.getInputStream ());

			String resultType;
			String extension;

			ChatUserImageType imageOrVideo;

			if (
				mediaLogic.isVideo (
					contentType)
			) {

				// resample the video

				data =
					mediaLogic.videoConvert (
						"3gpp",
						data);

				// set others

				resultType =
					"video/3gpp";

				imageOrVideo =
					ChatUserImageType.video;

				extension =
					"3gp";

			} else {

				// read the image

				BufferedImage image =
					mediaLogic.readImage (
						data,
						"image/jpeg");

				if (image == null) {

					requestContext.addError (
						stringFormat (
							"Error reading image (content type was %s)",
							item.getContentType ()));

					return null;

				}

				// resample image

				image =
					mediaLogic.resampleImage (
						image,
						320,
						240);

				data =
					mediaLogic.writeImage (
						image,
						"image/jpeg");

				// set others

				resultType =
					"image/jpeg";

				imageOrVideo =
					ChatUserImageType.image;

				extension =
					"jpg";

			}

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			ChatUserRec chatUser =
				chatUserHelper.find (
					requestContext.stuffInt ("chatUserId"));

			UserRec myUser =
				userHelper.find (
					requestContext.userId ());

			// create media

			MediaRec media =
				mediaLogic.createMedia (
					data,
					resultType,
					chatUser.getCode () + "." + extension,
					null);

			List<ChatUserImageRec> list;

			switch (imageOrVideo) {

			case image:
				list = chatUser.getChatUserImageList ();
				break;

			case video:
				list = chatUser.getChatUserVideoList ();
				break;

			default:
				throw new RuntimeException ();

			}

			// create chat user image

			ChatUserImageRec chatUserImage =
				chatUserImageHelper.insert (
					new ChatUserImageRec ()

				.setChatUser (
					chatUser)

				.setMedia (
					media)

				.setFullMedia (
					media)

				.setStatus (
					ChatUserInfoStatus.console)

				.setTimestamp (
					instantToDate (
						transaction.now ()))

				.setModerator (
					myUser)

				.setModerationTime (
					instantToDate (
						transaction.now ()))

				.setType (
					imageOrVideo)

				.setIndex (
					list.size ())

			);

			list.add (chatUserImage);

			transaction.commit ();

			requestContext.addNotice (
				"Photo uploaded");

			return responder ("chatUserImageListResponder");

		} catch (Exception exception) {

			throw rethrow (exception);

		}

	}

}
