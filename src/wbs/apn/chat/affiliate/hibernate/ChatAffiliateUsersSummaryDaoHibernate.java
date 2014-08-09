package wbs.apn.chat.affiliate.hibernate;

import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateUsersSummaryDao;
import wbs.apn.chat.affiliate.model.ChatAffiliateUsersSummaryRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatAffiliateUsersSummaryDaoHibernate
	extends HibernateDao
	implements ChatAffiliateUsersSummaryDao {

	@Override
	public
	List<ChatAffiliateUsersSummaryRec> find (
			ChatRec chat) {

		return findMany (
			ChatAffiliateUsersSummaryRec.class,

			createQuery (
				"FROM ChatAffiliateUsersSummaryView caus " +
				"WHERE caus.chatAffiliate.chatScheme.chat = :chat")

			.setEntity (
				"chat",
				chat)

			.list ());

	}

}
