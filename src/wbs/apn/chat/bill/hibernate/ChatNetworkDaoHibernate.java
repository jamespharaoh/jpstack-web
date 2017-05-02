package wbs.apn.chat.bill.hibernate;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.network.model.NetworkRec;

import wbs.apn.chat.bill.model.ChatNetworkDao;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatNetworkDaoHibernate")
public
class ChatNetworkDaoHibernate
	extends HibernateDao
	implements ChatNetworkDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatNetworkRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull NetworkRec network) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ChatNetworkRec.class,

				createCriteria (
					transaction,
					ChatNetworkRec.class,
					"_chatNetwork")

				.add (
					Restrictions.eq (
						"_chatNetwork.chat",
						chat))

				.add (
					Restrictions.eq (
						"_chatNetwork.network",
						network))

				.setCacheable (
					true)

				.setFlushMode (
					FlushMode.MANUAL)

			);

		}

	}

}
