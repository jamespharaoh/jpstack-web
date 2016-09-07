package wbs.clients.apn.chat.contact.hibernate;

import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;
import org.joda.time.Interval;

import lombok.NonNull;
import wbs.clients.apn.chat.contact.model.ChatMessageDao;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
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
			@NonNull ChatUserRec chatUser) {

		return findOne (
			"findSignup (chatUser)",
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.eq (
					"_chatMessage.fromUser",
					chatUser))

			.add (
				Restrictions.eq (
					"_chatMessage.status",
					ChatMessageStatus.signup))

		);

	}

	@Override
	public
	List<ChatMessageRec> findSignupTimeout (
			@NonNull ChatRec chat,
			@NonNull Instant timestamp) {

		return findMany (
			"findSignupTimeout (chat, timestamp)",
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.eq (
					"_chatMessage.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatMessage.status",
					ChatMessageStatus.signup))

			.add (
				Restrictions.lt (
					"_chatMessage.timestamp",
					timestamp))

		);

	}

	@Override
	public
	List <ChatMessageRec> findLimit (
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser,
			Long maxResults) {

		return findMany (
			"findLimit (fromChatUser, toChatUser)",
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.eq (
					"_chatMessage.fromUser",
					fromChatUser))

			.add (
				Restrictions.eq (
					"_chatMessage.toUser",
					toChatUser))

			.addOrder (
				Order.desc (
					"_chatMessage.timestamp"))

			.setMaxResults (
				toJavaIntegerRequired (
					maxResults))

		);

	}

	@Override
	public
	List<ChatMessageRec> findBySenderAndTimestamp (
			@NonNull ChatRec chat,
			@NonNull UserRec senderUser,
			@NonNull Interval timestamp) {

		return findMany (
			"findBySenderAndTimestamp (chat, senderUser, timestampInterval)",
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.eq (
					"_chatMessage.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatMessage.sender",
					senderUser))

			.add (
				Restrictions.ge (
					"_chatMessage.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatMessage.timestamp",
					timestamp.getEnd ()))

		);

	}

	@Override
	public
	List<ChatMessageRec> find (
			@NonNull ChatUserRec chatUser) {

		return findMany (
			"find (chatUser)",
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
					chatUser)

			))

			.addOrder (
				Order.desc (
					"_chatMessage.timestamp"))

		);

	}

	@Override
	public
	List<ChatMessageRec> findFromTo (
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser) {

		return findMany (
			"findFromTo (fromChatUser, toChatUser)",
			ChatMessageRec.class,

			createCriteria (
				ChatMessageRec.class,
				"_chatMessage")

			.add (
				Restrictions.eq (
					"_chatMessage.fromUser",
					fromChatUser))

			.add (
				Restrictions.eq (
					"_chatMessage.toUser",
					toChatUser))

			.addOrder (
				Order.desc (
					"_chatMessage.timestamp"))

		);

	}

	@Override
	public
	List<ChatMessageRec> search (
			@NonNull ChatMessageSearch search) {

		Criteria criteria =
			createCriteria (
				ChatMessageRec.class)

			.createAlias (
				"chat",
				"_chat")

			.createAlias (
				"fromUser",
				"_fromUser")

			.createAlias (
				"toUser",
				"_toUser")

			.createAlias (
				"originalText",
				"_originalText");

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
					search.timestampAfter ()));

		}

		if (search.timestampBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"timestamp",
					search.timestampBefore ()));

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
			"search (search)",
			ChatMessageRec.class,
			criteria);

	}

	@Override
	public
	Long count (
			@NonNull ChatUserRec chatUser) {

		return findOne (
			"count (chatUser)",
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

		);

	}

	@Override
	public
	List <ChatMessageRec> findLimit (
			@NonNull ChatUserRec chatUser,
			Long maxResults) {

		return findMany (
			"findLimit (chatUser, maxResults)",
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
				toJavaIntegerRequired (
					maxResults))

		);

	}

}
