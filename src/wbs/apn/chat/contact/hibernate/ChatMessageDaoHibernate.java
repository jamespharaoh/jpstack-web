package wbs.apn.chat.contact.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.platform.user.model.UserRec;

import wbs.apn.chat.contact.model.ChatMessageDao;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.contact.model.ChatMessageStats;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMessageUserStats;
import wbs.apn.chat.contact.model.ChatMessageViewRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatMessageDao")
public
class ChatMessageDaoHibernate
	extends HibernateDao
	implements ChatMessageDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatMessageRec findSignup (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSignup");

		) {

			return findOneOrNull (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> findSignupTimeout (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Instant timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSignupTimeout");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> findLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLimit");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> findBySenderAndTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull UserRec senderUser,
			@NonNull Interval timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findBySenderAndTimestamp");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> findFromTo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findFromTo");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> search (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"search");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
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
					"_chat.senderUser",
					"_senderUser",
					JoinType.LEFT_OUTER_JOIN)

				.createAlias (
					"originalText",
					"_originalText");

			if (
				isNotNull (
					search.chatIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_chat.id",
						search.chatIds ()));

			}

			if (
				isNotNull (
					search.senderUserIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_senderUser.id",
						search.senderUserIds ()));

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

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_chatMessage.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_chatMessage.timestamp",
						search.timestamp ().end ()));

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
				transaction,
				ChatMessageRec.class,
				criteria);

		}

	}

	@Override
	public
	Long count (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"count");

		) {

			return findOneOrNull (
				transaction,
				Long.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <ChatMessageRec> findLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLimit");

		) {

			return findMany (
				transaction,
				ChatMessageRec.class,

				createCriteria (
					transaction,
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

	@Override
	public
	List <ChatMessageUserStats> searchUserStats (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchUserStats");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ChatMessageViewRec.class,
					"_chatMessageView")

				.createAlias (
					"chatMessageView.chat",
					"_chat")

				.createAlias (
					"_chatMessageView.senderUser",
					"_senderUser")

			;

			if (
				isNotNull (
					search.chatIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_chat.id",
						search.chatIds ()));

			}

			if (
				isNotNull (
					search.senderUserIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_senderUser.id",
						search.senderUserIds ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_chatMessageView.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_chatMessageView.timestamp",
						search.timestamp ().end ()));

			}

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.groupProperty (
						"_chatMessageView.chat"),
					"chat")

				.add (
					Projections.groupProperty (
						"_chatMessageView.senderUser"),
					"senderUser")

				.add (
					Projections.rowCount (),
					"numMessages")

				.add (
					Projections.sum (
						"_chatMessageView.numCharacters"),
					"numCharacters")

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ChatMessageUserStats.class));

			return findMany (
				transaction,
				ChatMessageUserStats.class,
				criteria);

		}

	}

	@Override
	public
	List <ChatMessageStats> searchStats (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchStats");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ChatMessageViewRec.class,
					"_chatMessageView")

				.createAlias (
					"chatMessageView.chat",
					"_chat")

			;

			if (
				isNotNull (
					search.chatIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_chat.id",
						search.chatIds ()));

			}

			if (
				isNotNull (
					search.senderUserIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_senderUser.id",
						search.senderUserIds ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_chatMessageView.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_chatMessageView.timestamp",
						search.timestamp ().end ()));

			}

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.groupProperty (
						"_chatMessageView.chat"),
					"chat")

				.add (
					Projections.rowCount (),
					"numMessages")

				.add (
					Projections.sum (
						"_chatMessageView.numCharacters"),
					"numCharacters")

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ChatMessageStats.class));

			return findMany (
				transaction,
				ChatMessageStats.class,
				criteria);

		}

	}

}
