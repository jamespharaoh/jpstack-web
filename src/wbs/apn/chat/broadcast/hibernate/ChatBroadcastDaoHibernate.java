package wbs.apn.chat.broadcast.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.broadcast.model.ChatBroadcastDao;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatBroadcastDao")
public
class ChatBroadcastDaoHibernate
	extends HibernateDao
	implements ChatBroadcastDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatBroadcastRec> findRecentWindow (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Long firstResult,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRecentWindow");

		) {

			return findMany (
				transaction,
				ChatBroadcastRec.class,

				createCriteria (
					transaction,
					ChatBroadcastRec.class,
					"_chatBroadcast")

				.add (
					Restrictions.eq (
						"_chatBroadcast.chat",
						chat))

				.addOrder (
					Order.desc (
						"_chatBroadcast.createdTime"))

				.setFirstResult (
					toJavaIntegerRequired (
						firstResult))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

	@Override
	public
	List <ChatBroadcastRec> findSending (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSending");

		) {

			return findMany (
				transaction,
				ChatBroadcastRec.class,

				createCriteria (
					transaction,
					ChatBroadcastRec.class,
					"_chatBroadcast")

				.add (
					Restrictions.eq (
						"_chatBroadcast.state",
						ChatBroadcastState.sending))

			);

		}

	}

	@Override
	public
	List <ChatBroadcastRec> findScheduled (
			@NonNull Transaction parentTransaction,
			@NonNull Instant scheduledTime) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findScheduled");

		) {

			return findMany (
				transaction,
				ChatBroadcastRec.class,

				createCriteria (
					transaction,
					ChatBroadcastRec.class,
					"_chatBroadcast")

				.add (
					Restrictions.eq (
						"_chatBroadcast.state",
						ChatBroadcastState.scheduled))

				.add (
					Restrictions.le (
						"_chatBroadcast.scheduledTime",
						scheduledTime))

			);

		}

	}

}
