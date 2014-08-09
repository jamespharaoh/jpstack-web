package wbs.apn.chat.bill.hibernate;

import java.util.Date;

import wbs.apn.chat.bill.model.ChatUserSpendDao;
import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserSpendDaoHibernate
	extends HibernateDao
	implements ChatUserSpendDao {

	@Override
	public
	ChatUserSpendRec findByDate (
			ChatUserRec chatUser,
			Date date) {

		return findOne (
			ChatUserSpendRec.class,

			createQuery (
				"FROM ChatUserSpendRec cus " +
				"WHERE cus.chatUser = :chatUser " +
				"AND cus.date = :date")

			.setEntity (
				"chatUser",
				chatUser)

			.setDate (
				"date",
				date)

			.list ());

	}

}
