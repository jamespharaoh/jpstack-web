package wbs.apn.chat.bill.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

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
			@NonNull ChatUserRec chatUser,
			@NonNull LocalDate date) {

		return findOneOrNull (
			"findByDate (chatUser, date)",
			ChatUserSpendRec.class,

			createCriteria (
				ChatUserSpendRec.class)

			.add (
				Restrictions.eq (
					"chatUser",
					chatUser))

			.add (
				Restrictions.eq (
					"date",
					date))

		);

	}

}
