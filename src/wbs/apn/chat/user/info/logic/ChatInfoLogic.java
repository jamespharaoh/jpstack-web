package wbs.apn.chat.user.info.logic;

import java.util.Collection;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;

import wbs.apn.chat.user.core.model.ChatUserRec;

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
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			ChatUserRec otherUser,
			Optional <Long> threadIdOptional,
			Boolean asDating);

	long sendUserInfos (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadIdOptional);

	String chatUserBlurb (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	MediaRec chatUserBlurbMedia (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	void sendUserPics (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	void sendUserVideos (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	long sendRequestedUserPicandOtherUserPics (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			ChatUserRec requestedUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserPics (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserVideos (
			TaskLogger parentTaskLogger,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	void chatUserSetInfo (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			String info,
			Optional <Long> threadId);

	void sendNameHint (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser);

	void sendPicHint (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser);

	void sendPicHint2 (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser);

}
