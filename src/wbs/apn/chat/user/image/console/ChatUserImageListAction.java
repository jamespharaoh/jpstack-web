package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.Misc.toEnumRequired;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualWithClass;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringInSafe;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.notice.ConsoleNotices;
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

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserImageListAction")
public
class ChatUserImageListAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RawMediaLogic rawMediaLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAudioListResponder")
	Provider <WebResponder> audioListResponderProvider;

	@PrototypeDependency
	@NamedDependency ("chatUserImageListResponder")
	Provider <WebResponder> imageListResponderProvider;

	@PrototypeDependency
	@NamedDependency ("chatUserVideoListResponder")
	Provider <WebResponder> videoListResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		ChatUserImageType type =
			toEnumRequired (
				ChatUserImageType.class,
				(String) requestContext.stuff (
					"chatUserImageType"));

		switch (type) {

		case audio:

			return audioListResponderProvider.get ();

		case image:

			return imageListResponderProvider.get ();

		case video:

			return videoListResponderProvider.get ();

		default:

			throw shouldNeverHappen ();

		}

	}

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

			ConsoleNotices notices =
				new ConsoleNotices ();

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			ChatUserImageType type =
				toEnumRequired (
					ChatUserImageType.class,
					requestContext.stuffString (
						"chatUserImageType"));

			List <ChatUserImageRec> list =
				chatUserLogic.getChatUserImageListByType (
					chatUser,
					type);

			for (
				String key
					: requestContext.parameterMap ().keySet ()
			) {

				Matcher matcher =
					keyPattern.matcher (
						key);

				if (! matcher.matches ())
					continue;

				String command =
					matcher.group (1);

				long index =
					parseIntegerRequired (
						matcher.group (2));

				if (index >= list.size ())
					throw new RuntimeException ();

				if (
					stringEqualSafe (
						command,
						"remove")
				) {

					ChatUserImageRec itemToRemove =
						listItemAtIndexRequired (
							list,
							index);

					if (
						optionalValueEqualWithClass (
							ChatUserImageRec.class,
							optionalFromNullable (
								chatUserLogic.getMainChatUserImageByType (
									chatUser,
									type)),
							itemToRemove)
					) {

						chatUserLogic.setMainChatUserImageByType (
							chatUser,
							type,
							Optional.absent ());

					}

					itemToRemove

						.setIndex (
							null);

					for (
						long otherIndex = index + 1;
						otherIndex < list.size ();
						otherIndex ++
					) {

						ChatUserImageRec otherItem =
							listItemAtIndexRequired (
								list,
								otherIndex);

						otherItem

							.setIndex (
								otherIndex - 1l);

					}

					notices.noticeFormat (
						"Image/video removed");

				}

				if (

					enumNotEqualSafe (
						type,
						ChatUserImageType.video)

					&& stringInSafe (
						command,
						"rotate_cw",
						"rotate_ccw")

				) {

					ChatUserImageRec chatUserImage =
						listItemAtIndexRequired (
							list,
							index);

					BufferedImage smallImage =
						mediaLogic.getImageRequired (
							transaction,
							chatUserImage.getMedia ());

					BufferedImage fullImage =
						mediaLogic.getImageRequired (
							transaction,
							chatUserImage.getFullMedia ());

					if (
						stringEqualSafe (
							command,
							"rotate_ccw")
					) {

						smallImage =
							rawMediaLogic.rotateImage270 (
								smallImage);

						fullImage =
							rawMediaLogic.rotateImage270 (
								fullImage);

					}

					if (
						stringEqualSafe (
							command,
							"rotate_cw")
					) {

						smallImage =
							rawMediaLogic.rotateImage90 (
								smallImage);

						fullImage =
							rawMediaLogic.rotateImage90 (
								fullImage);

					}

					String filename =
						chatUser.getCode () + ".jpg";

					chatUserImage.setMedia (
						mediaLogic.createMediaFromImage (
							transaction,
							smallImage,
							"image/jpeg",
							filename));

					chatUserImage.setFullMedia (
						mediaLogic.createMediaFromImage (
							transaction,
							fullImage,
							"image/jpeg",
							filename));

					notices.noticeFormat (
						"Image/video rotated");

				}

				if (
					stringInSafe (
						command,
						"move_up",
						"move_down")
				) {

					long diff =
						ifThenElse (
							stringEqualSafe (
								command,
								"move_up"),
							() -> -1l,
							() -> 1l);

					long otherIndex = (
						+ index
						+ list.size ()
						+ diff
					) % list.size ();

					ChatUserImageRec thisImage =
						listItemAtIndexRequired (
							list,
							index);

					ChatUserImageRec otherImage =
						listItemAtIndexRequired (
							list,
							otherIndex);

					thisImage.setIndex (null);
					otherImage.setIndex (null);

					transaction.flush ();

					thisImage.setIndex (
						otherIndex);

					otherImage.setIndex (
						index);

					notices.noticeFormat (
						"Image/video moved");

				}

				if (
					stringInSafe (
						command,
						"select")
				) {

					ChatUserImageRec chatUserImage =
						listItemAtIndexRequired (
							list,
							index);

					if (
						optionalValueEqualWithClass (
							ChatUserImageRec.class,
							optionalFromNullable (
								chatUserLogic.getMainChatUserImageByType (
									chatUser,
									type)),
							chatUserImage)
					) {

						chatUserLogic.setMainChatUserImageByType (
							chatUser,
							type,
							Optional.absent ());

						notices.noticeFormat (
							"Image unselected");

					} else {

						chatUserLogic.setMainChatUserImageByType (
							chatUser,
							type,
							Optional.of (
								chatUserImage));

						notices.noticeFormat (
							"Image selected");

					}

				}

			}

			transaction.commit ();

			requestContext.addNotices (
				notices);

			return null;

		}

	}

	static
	Pattern keyPattern =
		Pattern.compile (
			"(remove|rotate_ccw|rotate_cw|move_up|move_down|select)_([0-9]+)");

}
