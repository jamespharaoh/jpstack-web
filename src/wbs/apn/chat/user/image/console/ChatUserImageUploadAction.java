package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.toEnumRequired;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.logic.RawMediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserImageUploadAction")
public
class ChatUserImageUploadAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserImageObjectHelper chatUserImageHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	@NamedDependency ("chatUserImageUploadFormType")
	ConsoleFormType <ChatUserImageUploadForm> formType;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RawMediaLogic rawMediaLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserImageListResponder")
	Provider <WebResponder> imageListResponderProvider;

	@PrototypeDependency
	@NamedDependency ("chatUserImageUploadResponder")
	Provider <WebResponder> imageUploadResponderProvider;

	// state

	ChatUserImageType chatUserImageType;

	ConsoleForm <ChatUserImageUploadForm> form;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return imageUploadResponderProvider.get ();

	}

	// implementation

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			chatUserImageType =
				toEnumRequired (
					ChatUserImageType.class,
					(String)
					requestContext.stuff (
						"chatUserImageType"));

			form =
				formType.buildAction (
					transaction,
					emptyMap (),
					new ChatUserImageUploadForm ());

			form.update (
				transaction);

			String resultType;
			String extension;

			byte[] resampledData;

			switch (chatUserImageType) {

			case video:

				// resample the video

				resampledData =
					rawMediaLogic.videoConvertRequired (
						transaction,
						"3gpp",
						form.value ().upload ().data ());

				// set others

				resultType =
					"video/3gpp";

				extension =
					"3gp";

				break;

			case image:

				// read the image

				Optional <BufferedImage> imageOptional =
					rawMediaLogic.readImage (
						transaction,
						form.value ().upload ().data (),
						"image/jpeg");

				if (
					optionalIsNotPresent (
						imageOptional)
				) {

					requestContext.addError (
						stringFormat (
							"Error reading image (content type was %s)",
							ifNull (
								form.value ().upload ().contentType (),
								"not specified")));

					return null;

				}

				BufferedImage image =
					imageOptional.get ();

				// resample image

				image =
					rawMediaLogic.resampleImageToFit (
						image,
						320l,
						240l);

				resampledData =
					rawMediaLogic.writeImage (
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

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// create media

			MediaRec media =
				mediaLogic.createMediaRequired (
					transaction,
					resampledData,
					resultType,
					chatUser.getCode () + "." + extension,
					optionalAbsent ());

			// create chat user image

			List <ChatUserImageRec> chatUserImageList =
				chatUserLogic.getChatUserImageListByType (
					chatUser,
					chatUserImageType);

			ChatUserImageRec chatUserImage =
				chatUserImageHelper.insert (
					transaction,
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
					userConsoleLogic.userRequired (
						transaction))

				.setModerationTime (
					transaction.now ())

				.setType (
					chatUserImageType)

				.setIndex (
					fromJavaInteger (
						chatUserImageList.size ()))

			);

			// add to chat user

			chatUserImageList.add (
				chatUserImage);

			if (form.value ().setAsPrimary ()) {

				chatUserLogic.setMainChatUserImageByType (
					chatUser,
					chatUserImageType,
					Optional.of (
						chatUserImage));

			}

			// commit and finish up

			transaction.commit ();

			requestContext.addNoticeFormat (
				"%s uploaded",
				capitalise (
					chatUserImageType.name ()));

			return imageListResponderProvider.get ();

		}

	}

}
