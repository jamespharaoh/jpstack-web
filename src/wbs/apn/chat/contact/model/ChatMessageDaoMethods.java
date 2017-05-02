package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatMessageDaoMethods {

	// messages pending signup

	ChatMessageRec findSignup (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	List <ChatMessageRec> findSignupTimeout (
			Transaction parentTransaction,
			ChatRec chat,
			Instant timestamp);

	// messages sent by console user

	List <ChatMessageRec> findBySenderAndTimestamp (
			Transaction parentTransaction,
			ChatRec chat,
			UserRec senderUser,
			Interval timestampInterval);

	// all messages to/from user

	Long count (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	List <ChatMessageRec> find (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	List <ChatMessageRec> findLimit (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Long maxResults);

	// all messages between two users

	List <ChatMessageRec> findFromTo (
			Transaction parentTransaction,
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

	List <ChatMessageRec> findLimit (
			Transaction parentTransaction,
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser,
			Long maxResults);

	// search

	List <ChatMessageRec> search (
			Transaction parentTransaction,
			ChatMessageSearch search);

}