package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;

public
interface ChatMessageDaoMethods {

	// messages pending signup

	ChatMessageRec findSignup (
			ChatUserRec chatUser);

	List <ChatMessageRec> findSignupTimeout (
			ChatRec chat,
			Instant timestamp);

	// messages sent by console user

	List <ChatMessageRec> findBySenderAndTimestamp (
			ChatRec chat,
			UserRec senderUser,
			Interval timestampInterval);

	// all messages to/from user

	Long count (
			ChatUserRec chatUser);

	List <ChatMessageRec> find (
			ChatUserRec chatUser);

	List <ChatMessageRec> findLimit (
			ChatUserRec chatUser,
			Long maxResults);

	// all messages between two users

	List <ChatMessageRec> findFromTo (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

	List <ChatMessageRec> findLimit (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser,
			Long maxResults);

	// search

	List <ChatMessageRec> search (
			ChatMessageSearch search);

}