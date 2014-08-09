package wbs.apn.chat.affiliate.hibernate;

import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateDao;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatAffiliateDao")
public
class ChatAffiliateDaoHibernate
	extends HibernateDao
	implements ChatAffiliateDao {

	@Override
	public
	List<ChatAffiliateRec> find (
			ChatRec chat) {

		return findMany (
			ChatAffiliateRec.class,

			createQuery (
				"FROM ChatAffiliateRec chatAffiliate " +
				"WHERE chatAffiliate.chat = :chat")

			.setEntity (
				"chat",
				chat)

			.list ());

	}

}
