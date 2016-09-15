package wbs.apn.chat.user.info.logic;

import java.util.Collection;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.media.model.MediaRec;

public
interface ChatInfoLogic {

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection <ChatUserRec> getNearbyOnlineUsersForInfo (
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection <ChatUserRec> getNearbyOnlineUsersForPic (
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	Collection <ChatUserRec> getNearbyOnlineUsersForVideo (
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	void sendUserInfo (
			ChatUserRec thisUser,
			ChatUserRec otherUser,
			Optional <Long> threadIdOptional,
			Boolean asDating);

	long sendUserInfos (
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadIdOptional);

	String chatUserBlurb (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	MediaRec chatUserBlurbMedia (
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	void sendUserPics (
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	void sendUserVideos (
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	long sendRequestedUserPicandOtherUserPics (
			ChatUserRec thisUser,
			ChatUserRec requestedUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserPics (
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserVideos (
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	void chatUserSetInfo (
			ChatUserRec chatUser,
			String info,
			Optional <Long> threadId);

	void sendNameHint (
			ChatUserRec chatUser);

	void sendPicHint (
			ChatUserRec chatUser);

	void sendPicHint2 (
			ChatUserRec chatUser);

}
