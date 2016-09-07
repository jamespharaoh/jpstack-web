package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("chatUserImageHistoryPart")
public
class ChatUserImageHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	ChatUserImageType type;
	ChatUserRec chatUser;
	Set <ChatUserImageRec> chatUserImages;

	// implementation

	@Override
	public
	void prepare () {

		type =
			toEnum (
				ChatUserImageType.class,
				requestContext.stuffString (
					"chatUserImageType"));

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		chatUserImages =
			new TreeSet<> (
				chatUser.getChatUserImages ());

		Iterator <ChatUserImageRec> iterator =
			chatUserImages.iterator ();

		while (iterator.hasNext ()) {

			if (
				enumNotEqualSafe (
					iterator.next ().getType (),
					type)
			) {
				iterator.remove ();
			}

		}

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

		for (
			ChatUserImageRec chatUserImage
				: chatUserImages
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getType ());

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getIndex () != null
					? chatUserImage.getIndex () + 1
					: "");

			printFormat (
				"<td style=\"text-align: center;\">%s</td>\n",
				chatUserImage.getMedia () == null
					? "(none)"
					: mediaConsoleLogic.mediaThumb100 (
						chatUserImage.getMedia ()));

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getTimestamp ());

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getStatus ());

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPathMini (
					chatUserImage.getModerator (),
					userConsoleLogic.sliceRequired ()));

			printFormat (
				"<td>%h</td>\n",
				chatUserImage.getModerationTime ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
