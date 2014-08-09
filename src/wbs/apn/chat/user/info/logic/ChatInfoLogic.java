package wbs.apn.chat.user.info.logic;

import java.util.Collection;
import java.util.Date;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.media.model.MediaRec;

public
interface ChatInfoLogic {

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection<ChatUserRec> getNearbyOnlineUsersForInfo (
			ChatUserRec thisUser,
			Date cutoffTime,
			int numToFind);

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection<ChatUserRec> getNearbyOnlineUsersForPic (
			ChatUserRec thisUser,
			Date cutoffTime,
			int numToFind);

	Collection<ChatUserRec> getNearbyOnlineUsersForVideo (
			ChatUserRec thisUser,
			Date cutoffTime,
			int numToFind);

	void sendUserInfo (
			ChatUserRec thisUser,
			ChatUserRec otherUser,
			Integer threadId,
			boolean asDating);

	int sendUserInfos (
			final ChatUserRec thisUser,
			final int numToSend,
			Integer threadId);

	String chatUserBlurb (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	MediaRec chatUserBlurbMedia (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	void sendUserPics (
			final ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers,
			Integer threadId,
			boolean asDating);

	void sendUserVideos (
			final ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers,
			Integer threadId,
			boolean asDating);

	int sendRequestedUserPicandOtherUserPics (
			final ChatUserRec thisUser,
			final ChatUserRec requestedUser,
			final int numToSend,
			Integer threadId);

	int sendUserPics (
			final ChatUserRec thisUser,
			final int numToSend,
			Integer threadId);

	int sendUserVideos (
			final ChatUserRec thisUser,
			final int numToSend,
			Integer threadId);

	void chatUserSetInfo (
			ChatUserRec chatUser,
			String info,
			Integer threadId);

	void sendNameHint (
			ChatUserRec chatUser);

	void sendPicHint (
			ChatUserRec chatUser);

	void sendPicHint2 (
			ChatUserRec chatUser);

}
