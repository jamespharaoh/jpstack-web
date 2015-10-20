package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserImageUploadAction")
public
class ChatUserImageUploadAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject @Named
	ConsoleModule chatUserImageConsoleModule;

	@Inject
	ChatUserImageObjectHelper chatUserImageHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// state

	FormFieldSet formFieldSet;

	ChatUserImageType chatUserImageType;

	ChatUserImageUploadForm uploadForm;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserImageUploadResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		formFieldSet =
			chatUserImageConsoleModule.formFieldSets ().get (
				"uploadForm");

		chatUserImageType =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff (
					"chatUserImageType"));

		uploadForm =
			new ChatUserImageUploadForm ();

		formFieldLogic.update (
			formFieldSet,
			uploadForm);

		String resultType;
		String extension;

		byte[] resampledData;

		switch (chatUserImageType) {

		case video:

			// resample the video

			resampledData =
				mediaLogic.videoConvert (
					"3gpp",
					uploadForm.upload ().data ());

			// set others

			resultType =
				"video/3gpp";

			extension =
				"3gp";

			break;

		case image:

			// read the image

			BufferedImage image =
				mediaLogic.readImage (
					uploadForm.upload ().data (),
					"image/jpeg");

			if (image == null) {

				requestContext.addError (
					stringFormat (
						"Error reading image (content type was %s)",
						uploadForm.upload ().contentType ()));

				return null;

			}

			// resample image

			image =
				mediaLogic.resampleImage (
					image,
					320,
					240);

			resampledData =
				mediaLogic.writeImage (
					image,
					"image/jpeg");

			// set others

			resultType =
				"image/jpeg";

			extension =
				"jpg";

			break;

		default:
			throw new RuntimeException ();

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// create media

		MediaRec media =
			mediaLogic.createMedia (
				resampledData,
				resultType,
				chatUser.getCode () + "." + extension,
				null);

		// create chat user image

		List<ChatUserImageRec> chatUserImageList =
			chatUserLogic.getChatUserImageListByType (
				chatUser,
				chatUserImageType);

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
				chatUserImageType)

			.setIndex (
				chatUserImageList.size ())

		);

		// add to chat user

		chatUserImageList.add (
			chatUserImage);

		if (uploadForm.setAsPrimary ()) {

			chatUserLogic.setMainChatUserImageByType (
				chatUser,
				chatUserImageType,
				Optional.of (
					chatUserImage));

		}

		// commit and finish up

		transaction.commit ();

		requestContext.addNotice (
			"%s uploaded",
			capitalise (
				chatUserImageType.name ()));

		return responder (
			"chatUserImageListResponder");

	}

}
