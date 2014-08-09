package wbs.apn.chat.user.pending.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserPendingSummaryPart")
public
class ChatUserPendingSummaryPart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt("chatUserId"));
	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Old</th>\n",
			"<th>New</th>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<td colspan=\"2\">%h</td>\n",
			stringFormat (
				"%s/%s",
				chatUser.getChat ().getCode (),
				chatUser.getCode ()),
			"</tr>\n");

		// name

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",
			"<td colspan=\"2\">%s</td>\n",
			ifNull (chatUser.getName (), "(none)"),
			"</tr>\n");

		// info

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n");

		if (chatUser.getNewChatUserInfo () != null) {

			printFormat (

				"<td>%h</td>\n",
				ifNull (
					chatUser.getInfoText (),
					"(none)"),

				"<td>%h</td>\n",
				chatUser.getNewChatUserInfo ().getOriginalText ().getText ());

		} else {

			printFormat (
				"<td colspan=\"2\">%s</td>\n",
				ifNull (
					chatUser.getInfoText (),
					"(none)"));

		}

		printFormat (
			"</tr>\n");

		// photo

		ChatUserImageRec newImage =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.image);

		printFormat (
			"<tr>\n",
			"<th>Photo</th>\n");

		if (newImage != null) {

			printFormat (
				"<td>%s</td>\n",
				chatUser.getChatUserImageList ().isEmpty ()
					? "(none)"
					: mediaConsoleLogic.mediaContent (
						chatUser.getChatUserImageList ().get (0).getMedia ()),

				"<td>%s</td>\n",
				mediaConsoleLogic.mediaContent (
					newImage.getMedia ()));

		} else {

			printFormat (
				"<td colspan=\"2\">%s</td>",
				chatUser.getChatUserImageList ().isEmpty ()
					? "(none)"
					: mediaConsoleLogic.mediaContent (
						chatUser.getChatUserImageList ().get (0).getMedia ()));

		}

		printFormat (
			"</tr>\n");

		// video

		String video =
			chatUser.getChatUserVideoList ().isEmpty ()
				? "(none)"
					: mediaConsoleLogic.mediaContent (
						chatUser.getChatUserVideoList ().get (0).getMedia ());

		ChatUserImageRec newVideo =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.video);

		String newVideoStr =
			newVideo == null
				? "(none)"
				: mediaConsoleLogic.mediaContent (
					newVideo.getMedia ());

		if (newVideo != null) {

			printFormat (
				"<tr>\n",
				"<th>Video</th>\n",
				"<td>%s</td>\n",
				video,
				"<td>%s</td>\n",
				newVideoStr,
				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Video</th>\n",
				"<td colspan=\"2\">%s</td>\n",
				video,
				"</tr>\n");

		}

		// audio

		String audio =
			chatUser.getChatUserAudioList ().isEmpty ()
				? "(none)"
				: mediaConsoleLogic.mediaContent (
					chatUser.getChatUserAudioList ().get (0).getMedia ());

		ChatUserImageRec newAudio =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.audio);

		String newAudioStr =
			newAudio == null
				? "(none)"
				: mediaConsoleLogic.mediaContent (
					newAudio.getMedia ());

		if (newAudio != null) {

			printFormat (
				"<tr>\n",
				"<th>Audio</th>\n",
				"<td>%s</td>\n",
				audio,
				"<td>%s</td>\n",
				newAudioStr,
				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Audio</th>\n",
				"<td colspan=\"2\">%s</td>\n",
				audio,
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
