package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualWithClass;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Cleanup;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.media.logic.MediaLogic;

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

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder () {

		ChatUserImageType type =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff (
					"chatUserImageType"));

		return responder (
			stringFormat (
				"chatUser%sListResponder",
				capitalise (
					type.toString ())));

	}

	@Override
	public
	Responder goReal () {

		String notice = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserImageListAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		ChatUserImageType type =
			toEnum (
				ChatUserImageType.class,
				requestContext.stuffString (
					"chatUserImageType"));

		List<ChatUserImageRec> list =
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

				notice =
					"Image/video removed";

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
					mediaLogic.getImage (
						chatUserImage.getMedia ());

				BufferedImage fullImage =
					mediaLogic.getImage (
						chatUserImage.getFullMedia ());

				if (
					stringEqualSafe (
						command,
						"rotate_ccw")
				) {

					smallImage =
						mediaLogic.rotateImage270 (smallImage);

					fullImage =
						mediaLogic.rotateImage270 (fullImage);

				}

				if (
					stringEqualSafe (
						command,
						"rotate_cw")
				) {

					smallImage =
						mediaLogic.rotateImage90 (smallImage);

					fullImage =
						mediaLogic.rotateImage90 (fullImage);

				}

				String filename =
					chatUser.getCode () + ".jpg";

				chatUserImage.setMedia (
					mediaLogic.createMediaFromImage (
						smallImage,
						"image/jpeg",
						filename));

				chatUserImage.setFullMedia (
					mediaLogic.createMediaFromImage (
						fullImage,
						"image/jpeg",
						filename));

				notice =
					"Image/video rotated";

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

				notice =
					"Image/video moved";

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

					notice =
						"Image unselected";

				} else {

					chatUserLogic.setMainChatUserImageByType (
						chatUser,
						type,
						Optional.of (
							chatUserImage));

					notice =
						"Image selected";

				}

			}

		}

		transaction.commit ();

		if (notice != null) {

			requestContext.addNotice (
				notice);

		}

		return null;

	}

	static
	Pattern keyPattern =
		Pattern.compile (
			"(remove|rotate_ccw|rotate_cw|move_up|move_down|select)_([0-9]+)");

}
