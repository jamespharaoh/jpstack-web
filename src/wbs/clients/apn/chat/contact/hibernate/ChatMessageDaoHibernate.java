package wbs.clients.apn.chat.contact.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.clients.apn.chat.contact.model.ChatMessageDao;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("chatMessageDao")
public
class ChatMessageDaoHibernate
	extends HibernateDao
	implements ChatMessageDao {

	@Override
	public
	ChatMessageRec findSignup (
			ChatUserRec chatUser) {

		return findOne (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec message " +
				"WHERE message.status = :status " +
					"AND message.fromUser = :chatUser")

			.setParameter (
				"status",
				ChatMessageStatus.signup,
				ChatMessageStatusType.INSTANCE)

			.setEntity (
				"chatUser",
				chatUser)

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> findSignupTimeout (
			ChatRec chat,
			Instant timestamp) {

		return findMany (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec message " +
				"WHERE message.status = :status " +
					"AND message.chat = :chat " +
					"AND message.timestamp < :timestamp")

			.setParameter (
				"status",
				ChatMessageStatus.signup,
				ChatMessageStatusType.INSTANCE)

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"timestamp",
				instantToDate (
					timestamp))

			.list ()

		);

	}

	@Override
	public
	List<ChatMessageRec> findLimit (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser,
			int maxResults) {

		return findMany (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec cm " +
				"WHERE cm.fromUser = :fromChatUser " +
					"AND cm.toUser = :toChatUser " +
				"ORDER BY cm.timestamp DESC")

			.setEntity (
				"fromChatUser",
				fromChatUser)

			.setEntity (
				"toChatUser",
				toChatUser)

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> findBySenderAndTimestamp (
			ChatRec chat,
			UserRec senderUser,
			Interval timestampInterval) {

		return findMany (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec chatMessage " +
				"WHERE chatMessage.chat = :chat " +
					"AND chatMessage.sender = :senderUser " +
					"AND chatMessage.timestamp >= :timestampStart " +
					"AND chatMessage.timestamp < :timestampEnd")

			.setEntity (
				"chat",
				chat)

			.setEntity (
				"senderUser",
				senderUser)

			.setTimestamp (
				"timestampStart",
				instantToDate (
					timestampInterval.getStart ()))

			.setTimestamp (
				"timestampEnd",
				instantToDate (
					timestampInterval.getEnd ()))

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> find (
			ChatUserRec chatUser) {

		return findMany (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec cm " +
				"WHERE cm.fromUser = :chatUser " +
					"OR cm.toUser = :chatUser " +
				"ORDER BY cm.timestamp DESC")

			.setEntity (
				"chatUser",
				chatUser)

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> find (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser) {

		return findMany (
			ChatMessageRec.class,

			createQuery (
				"FROM ChatMessageRec chatMessage " +
				"WHERE chatMessage.fromUser = :fromChatUser " +
					"AND chatMessage.toUser = :toChatUser " +
				"ORDER BY chatMessage.timestamp DESC")

			.setEntity (
				"fromChatUser",
				fromChatUser)

			.setEntity (
				"toChatUser",
				toChatUser)

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> search (
			ChatMessageSearch search) {

		Criteria criteria =
			createCriteria (ChatMessageRec.class)
				.createAlias ("chat", "_chat")
				.createAlias ("fromUser", "_fromUser")
				.createAlias ("toUser", "_toUser")
				.createAlias ("originalText", "_originalText");

		if (search.chatId () != null) {

			criteria.add (
				Restrictions.eq (
					"_chat.id",
					search.chatId ()));

		}

		if (search.fromUserId () != null) {

			criteria.add (
				Restrictions.eq (
					"_fromUser.id",
					search.fromUserId ()));

		}

		if (search.toUserId () != null) {

			criteria.add (
				Restrictions.eq (
					"_toUser.id",
					search.toUserId ()));

		}

		if (search.originalTextId () != null) {

			criteria.add (
				Restrictions.eq (
					"_originalText.id",
					search.originalTextId ()));

		}

		if (search.timestampAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"timestamp",
					search.timestampAfter ().toDate ()));

		}

		if (search.timestampBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"timestamp",
					search.timestampBefore ().toDate ()));

		}

		if (search.hasSender () != null) {

			if (search.hasSender ()) {

				criteria.add (
					Restrictions.isNotNull (
						"sender.id"));

			} else {

				criteria.add (
					Restrictions.isNull (
						"sender.id"));

			}

		}

		if (search.idGreaterThan () != null) {

			criteria.add (
				Restrictions.gt (
					"id",
					search.idGreaterThan ()));

		}

		if (search.deliveryId () != null) {

			criteria.add (
				Restrictions.eq (
					"deliveryId",
					search.deliveryId ()));

		}

		if (search.deliveryIdGreaterThan () != null) {

			criteria.add (
				Restrictions.gt (
					"deliveryId",
					search.deliveryIdGreaterThan ()));

		}

		if (search.method () != null) {

			criteria.add (
				Restrictions.eq (
					"method",
					search.method ()));

		}

		if (
			search.statusIn () != null
			&& ! search.statusIn ().isEmpty ()
		) {

			criteria.add (
				Restrictions.in (
					"status",
					search.statusIn ()));

		}

		if (search.orderBy () != null) {

			switch (search.orderBy ()) {

			case deliveryId:

				criteria.addOrder (
					Order.asc ("deliveryId"));

				break;

			}

		}

		return findMany (
			ChatMessageRec.class,
			criteria.list ());

	}

	@Override
	public
	int count (
			ChatUserRec chatUser) {

		return (int) (long) findOne (
			Long.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.or (
					Restrictions.eq (
						"_chatMessage.fromUser",
						chatUser),
					Restrictions.eq (
						"_chatMessage.toUser",
						chatUser)))

			.setProjection (
				Projections.rowCount ())

			.list ());

	}

	@Override
	public
	List<ChatMessageRec> findLimit (
			ChatUserRec chatUser,
			int maxResults) {

		return findMany (
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.or (
					Restrictions.eq (
						"_chatMessage.fromUser",
						chatUser),
					Restrictions.eq (
						"_chatMessage.toUser",
						chatUser)))

			.addOrder (
				Order.desc (
					"_chatMessage.timestamp"))

			.setMaxResults (
				maxResults)

			.list ());

	}

}
