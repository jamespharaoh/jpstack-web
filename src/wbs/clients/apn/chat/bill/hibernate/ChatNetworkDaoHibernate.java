package wbs.clients.apn.chat.bill.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.clients.apn.chat.bill.model.ChatNetworkDao;
import wbs.clients.apn.chat.bill.model.ChatNetworkRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.network.model.NetworkRec;

@SingletonComponent ("chatNetworkDaoHibernate")
public
class ChatNetworkDaoHibernate
	extends HibernateDao
	implements ChatNetworkDao {

	@Override
	public
	ChatNetworkRec find (
			ChatRec chat,
			NetworkRec network) {

		return findOne (
			ChatNetworkRec.class,

			createCriteria (
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

			.list ());

	}

}
