package wbs.apn.chat.affiliate.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.affiliate.model.ChatAffiliateDao;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatAffiliateDao")
public
class ChatAffiliateDaoHibernate
	extends HibernateDao
	implements ChatAffiliateDao {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatAffiliateRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findMany (
				transaction,
				ChatAffiliateRec.class,

				createCriteria (
					transaction,
					ChatAffiliateRec.class,
					"_chatAffiliate")

				.add (
					Restrictions.eq (
						"_chatAffiliate.chat",
						chat))

			);

		}

	}

}
