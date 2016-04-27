package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

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
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

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
	UserConsoleLogic userConsoleLogic;

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
				(String)
				requestContext.stuff (
					"chatUserImageType"));

		uploadForm =
			new ChatUserImageUploadForm ();

		formFieldLogic.update (
			requestContext,
			formFieldSet,
			uploadForm,
			ImmutableMap.of (),
			"upload");

		String resultType;
		String extension;

		byte[] resampledData;

		switch (chatUserImageType) {

		case video:

			// resample the video

			resampledData =
				mediaLogic.videoConvertRequired (
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

			Optional<BufferedImage> imageOptional =
				mediaLogic.readImage (
					uploadForm.upload ().data (),
					"image/jpeg");

			if (
				isNotPresent (
					imageOptional)
			) {

				requestContext.addError (
					stringFormat (
						"Error reading image (content type was %s)",
						ifNull (
							uploadForm.upload ().contentType (),
							"not specified")));

				return null;

			}

			BufferedImage image =
				imageOptional.get ();

			// resample image

			image =
				mediaLogic.resampleImageToFit (
					image,
					320l,
					240l);

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
				requestContext.stuffInt (
					"chatUserId"));

		// create media

		MediaRec media =
			mediaLogic.createMediaRequired (
				resampledData,
				resultType,
				chatUser.getCode () + "." + extension,
				Optional.<String>absent ());

		// create chat user image

		List<ChatUserImageRec> chatUserImageList =
			chatUserLogic.getChatUserImageListByType (
				chatUser,
				chatUserImageType);

		ChatUserImageRec chatUserImage =
			chatUserImageHelper.insert (
				chatUserImageHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setMedia (
				media)

			.setFullMedia (
				media)

			.setStatus (
				ChatUserInfoStatus.console)

			.setTimestamp (
				transaction.now ())

			.setModerator (
				userConsoleLogic.userRequired ())

			.setModerationTime (
				transaction.now ())

			.setType (
				chatUserImageType)

			.setIndex (
				(long)
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
