package wbs.clients.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;

public
interface ChatMessageDaoMethods {

	// messages pending signup

	ChatMessageRec findSignup (
			ChatUserRec chatUser);

	List<ChatMessageRec> findSignupTimeout (
			ChatRec chat,
			Instant timestamp);

	// messages sent by console user

	List<ChatMessageRec> findBySenderAndTimestamp (
			ChatRec chat,
			UserRec senderUser,
			Interval timestampInterval);

	// all messages to/from user

	int count (
			ChatUserRec chatUser);

	List<ChatMessageRec> find (
			ChatUserRec chatUser);

	List<ChatMessageRec> findLimit (
			ChatUserRec chatUser,
			int maxResults);

	// all messages between two users

	List<ChatMessageRec> find (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

	List<ChatMessageRec> findLimit (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser,
			int maxResults);

	// search

	List<ChatMessageRec> search (
			ChatMessageSearch search);

}