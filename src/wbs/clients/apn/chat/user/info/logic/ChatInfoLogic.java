package wbs.clients.apn.chat.user.info.logic;

import java.util.Collection;

import org.joda.time.Instant;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.media.model.MediaRec;

public
interface ChatInfoLogic {

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection<ChatUserRec> getNearbyOnlineUsersForInfo (
			ChatUserRec thisUser,
			Instant cutoffTime,
			int numToFind);

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection<ChatUserRec> getNearbyOnlineUsersForPic (
			ChatUserRec thisUser,
			Instant cutoffTime,
			int numToFind);

	Collection<ChatUserRec> getNearbyOnlineUsersForVideo (
			ChatUserRec thisUser,
			Instant cutoffTime,
			int numToFind);

	void sendUserInfo (
			ChatUserRec thisUser,
			ChatUserRec otherUser,
			Long threadId,
			boolean asDating);

	int sendUserInfos (
			final ChatUserRec thisUser,
			final int numToSend,
			Long threadId);

	String chatUserBlurb (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	MediaRec chatUserBlurbMedia (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	void sendUserPics (
			ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers,
			Long threadId,
			boolean asDating);

	void sendUserVideos (
			ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers,
			Long threadId,
			boolean asDating);

	int sendRequestedUserPicandOtherUserPics (
			ChatUserRec thisUser,
			ChatUserRec requestedUser,
			int numToSend,
			Long threadId);

	int sendUserPics (
			ChatUserRec thisUser,
			int numToSend,
			Long threadId);

	int sendUserVideos (
			ChatUserRec thisUser,
			int numToSend,
			Long threadId);

	void chatUserSetInfo (
			ChatUserRec chatUser,
			String info,
			Long threadId);

	void sendNameHint (
			ChatUserRec chatUser);

	void sendPicHint (
			ChatUserRec chatUser);

	void sendPicHint2 (
			ChatUserRec chatUser);

}
