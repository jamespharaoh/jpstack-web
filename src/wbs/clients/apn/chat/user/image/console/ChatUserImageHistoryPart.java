package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserImageHistoryPart")
public
class ChatUserImageHistoryPart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	ChatUserImageType type;
	ChatUserRec chatUser;
	Set<ChatUserImageRec> chatUserImages;
	UserRec myUser;

	@Override
	public
	void prepare () {

		type =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		chatUserImages =
			new TreeSet<ChatUserImageRec> (
				chatUser.getChatUserImages ());

		Iterator<ChatUserImageRec> iterator =
			chatUserImages.iterator ();

		while (iterator.hasNext ()) {

			if (! equal (iterator.next ().getType (), type))
				iterator.remove ();

		}

		myUser =
			userHelper.find (
				requestContext.userId ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Type</th>",
			"<th>Index</th>\n",
			"<th>Preview</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Status</th>\n",
			"<th>Moderator</th>\n",
			"<th>Moderated</th>\n",
			"</tr>\n");

		if (chatUserImages.isEmpty ()) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"7\">No history to show</td>\n",
				"</tr>\n");

		}

		for (ChatUserImageRec chatUserImage
				: chatUserImages) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatUserImage.getType (),

				"<td>%h</td>\n",
				chatUserImage.getIndex () != null
					? chatUserImage.getIndex () + 1
					: "",

				"<td style=\"text-align: center;\">%s</td>\n",
				chatUserImage.getMedia () == null
					? "(none)"
					: mediaConsoleLogic.mediaThumb100 (
						chatUserImage.getMedia ()),

				"<td>%h</td>\n",
				chatUserImage.getTimestamp (),

				"<td>%h</td>\n",
				chatUserImage.getStatus (),

				"<td>%h</td>\n",
				objectManager.objectPath (
					chatUserImage.getModerator (),
					myUser.getSlice (),
					true,
					false),

				"<td>%h</td>\n",
				chatUserImage.getModerationTime (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
