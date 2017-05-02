package wbs.apn.chat.broadcast.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.broadcast.model.ChatBroadcastNumberDao;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;

public
class ChatBroadcastNumberDaoHibernate
	extends HibernateDao
	implements ChatBroadcastNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatBroadcastNumberRec> findAcceptedLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAcceptedLimit");

		) {

			return findMany (
				transaction,
				ChatBroadcastNumberRec.class,

				createCriteria (
					transaction,
					ChatBroadcastNumberRec.class,
					"_chatBroadcastNumber")

				.add (
					Restrictions.eq (
						"_chatBroadcastNumber.chatBroadcast",
						chatBroadcast))

				.add (
					Restrictions.eq (
						"_chatBroadcastNumber.state",
						ChatBroadcastNumberState.accepted))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
