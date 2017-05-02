package wbs.apn.chat.user.info.logic;

import java.util.Collection;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.platform.media.model.MediaRec;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatInfoLogic {

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection <ChatUserRec> getNearbyOnlineUsersForInfo (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	/**
	 * Returns the nearest numToFind users who qualify for a pic given
	 * cutoffTime.
	 */
	Collection <ChatUserRec> getNearbyOnlineUsersForPic (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	Collection <ChatUserRec> getNearbyOnlineUsersForVideo (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Instant cutoffTime,
			Long numToFind);

	void sendUserInfo (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			ChatUserRec otherUser,
			Optional <Long> threadIdOptional,
			Boolean asDating);

	long sendUserInfos (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadIdOptional);

	String chatUserBlurb (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	MediaRec chatUserBlurbMedia (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			ChatUserRec otherUser);

	void sendUserPics (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	void sendUserVideos (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Collection <ChatUserRec> otherUsers,
			Optional <Long> threadId,
			Boolean asDating);

	long sendRequestedUserPicandOtherUserPics (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			ChatUserRec requestedUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserPics (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	long sendUserVideos (
			Transaction parentTransaction,
			ChatUserRec thisUser,
			Long numToSend,
			Optional <Long> threadId);

	void chatUserSetInfo (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			String info,
			Optional <Long> threadId);

	void sendNameHint (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void sendPicHint (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void sendPicHint2 (
			Transaction parentTransaction,
			ChatUserRec chatUser);

}
