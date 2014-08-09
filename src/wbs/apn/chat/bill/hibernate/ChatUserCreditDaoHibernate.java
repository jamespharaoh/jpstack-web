package wbs.apn.chat.bill.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.bill.model.ChatUserCreditDao;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserCreditDaoHibernate
	extends HibernateDao
	implements ChatUserCreditDao {

	@Override
	public
	List<ChatUserCreditRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval) {

		return findMany (
			ChatUserCreditRec.class,

			createQuery (
				"FROM ChatUserCreditRec chatUserCredit " +
				"WHERE chatUserCredit.chatUser.chat = :chat " +
				"AND chatUserCredit.timestamp >= :timestampFrom " +
				"AND chatUserCredit.timestamp < :timestampTo")

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"timestampFrom",
				timestampInterval.getStart ().toDate ())

			.setTimestamp (
				"timestampTo",
				timestampInterval.getEnd ().toDate ())

			.list ());

	}

}
