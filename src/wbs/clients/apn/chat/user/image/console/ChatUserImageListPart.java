package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserImageListPart")
public
class ChatUserImageListPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// state

	ChatUserImageType type;
	ChatUserRec chatUser;
	List<ChatUserImageRec> chatUserImages;

	// implementation

	@Override
	public
	void prepare () {

		type =
			toEnum (
				ChatUserImageType.class,
				(String)
				requestContext.stuff ("chatUserImageType"));

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		switch (type) {

		case image:

			chatUserImages =
				chatUser.getChatUserImageList ();

			break;

		case video:

			chatUserImages =
				chatUser.getChatUserVideoList ();

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Unknown chat user image type: %s",
					type));

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				stringFormat (
					"/chatUser.%s.list",
					type.name ())),
			">\n");

		printFormat (
			"<table class=\"list\"");

		printFormat (
			"<tr>\n",
			"<th>I</th>\n",
			"<th>S</th>\n",
			"<th>Preview</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Moderator</th>\n",
			"<th>Classification</td>\n",
			"<th>Controls</th>\n",
			"</tr>\n");

		if (chatUserImages.isEmpty ()) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"7\">No photos/videos to show</td>\n",
				"</tr>");

		}

		int index = 0;

		for (
			ChatUserImageRec chatUserImage
				: chatUserImages
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getIndex () != null
					? chatUserImage.getIndex () + 1
					: "");

			printFormat (
				"<td>%h</td>\n",
				equal (
						chatUserLogic.getMainChatUserImageByType (
							chatUser,
							type),
						chatUserImage)
					? "Y"
					: "");

			printFormat (
				"<td style=\"text-align: center;\">",
				"<a href=\"%h\">%s</a>",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/chatUser.%u.view",
						type.toString (),
						"?chatUserImageId=%u",
						chatUserImage.getId ())),
				chatUserImage.getMedia () == null
					? "(none)"
					: mediaConsoleLogic.mediaThumb100 (
						chatUserImage.getMedia ()),
				"</td>\n");

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getTimestamp ());

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPathMini (
					chatUserImage.getModerator (),
					userConsoleLogic.sliceRequired ()));

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getClassification ());

			printFormat (
				"<td>\n",

				"<input",
				" type=\"submit\"",
				" name=\"remove_%h\"",
				index,
				" value=\"X\"",
				">\n",

				"<input",
				" type=\"submit\"",
				" name=\"rotate_ccw_%h\"",
				index,
				" value=\"&#x21b6;\"",
				">\n",

				"<input",
				" type=\"submit\"",
				" name=\"rotate_cw_%h\"",
				index,
				" value=\"&#x21b7;\"",
				">\n",

				"<input",
				" type=\"submit\"",
				" name=\"move_up_%h\"",
				index,
				" value=\"&#x2191;\"",
				">\n",

				"<input",
				" type=\"submit\"",
				" name=\"move_down_%h\"",
				index,
				" value=\"&#x2193;\"",
				">\n",

				"<input",
				" type=\"submit\"",
				" name=\"select_%h\"",
				index,
				" value=\"S\"",
				">\n",

				"</td>\n");

			printFormat (
				"</tr>\n");

			index ++;

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
